/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.AWTKeyStroke;
import java.awt.AWTPermission;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyEventPostProcessor;
import java.awt.SequencedEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LightweightPeer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.KeyboardFocusManagerPeerProvider;
import sun.awt.SunToolkit;
import sun.util.logging.PlatformLogger;

public abstract class KeyboardFocusManager
implements KeyEventDispatcher,
KeyEventPostProcessor {
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("java.awt.focus.KeyboardFocusManager");
    transient KeyboardFocusManagerPeer peer;
    private static final PlatformLogger log;
    public static final int FORWARD_TRAVERSAL_KEYS = 0;
    public static final int BACKWARD_TRAVERSAL_KEYS = 1;
    public static final int UP_CYCLE_TRAVERSAL_KEYS = 2;
    public static final int DOWN_CYCLE_TRAVERSAL_KEYS = 3;
    static final int TRAVERSAL_KEY_LENGTH = 4;
    private static Component focusOwner;
    private static Component permanentFocusOwner;
    private static Window focusedWindow;
    private static Window activeWindow;
    private FocusTraversalPolicy defaultPolicy = new DefaultFocusTraversalPolicy();
    private static final String[] defaultFocusTraversalKeyPropertyNames;
    private Set<AWTKeyStroke>[] defaultFocusTraversalKeys = new Set[4];
    private static Container currentFocusCycleRoot;
    private VetoableChangeSupport vetoableSupport;
    private PropertyChangeSupport changeSupport;
    private LinkedList<KeyEventDispatcher> keyEventDispatchers;
    private LinkedList<KeyEventPostProcessor> keyEventPostProcessors;
    private static Map<Window, WeakReference<Component>> mostRecentFocusOwners;
    private static AWTPermission replaceKeyboardFocusManagerPermission;
    transient SequencedEvent currentSequencedEvent = null;
    private static LinkedList<HeavyweightFocusRequest> heavyweightRequests;
    private static LinkedList<LightweightFocusRequest> currentLightweightRequests;
    private static boolean clearingCurrentLightweightRequests;
    private static boolean allowSyncFocusRequests;
    private static Component newFocusOwner;
    private static volatile boolean disableRestoreFocus;
    static final int SNFH_FAILURE = 0;
    static final int SNFH_SUCCESS_HANDLED = 1;
    static final int SNFH_SUCCESS_PROCEED = 2;

    private static native void initIDs();

    public static KeyboardFocusManager getCurrentKeyboardFocusManager() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager(AppContext.getAppContext());
    }

    static synchronized KeyboardFocusManager getCurrentKeyboardFocusManager(AppContext appcontext) {
        KeyboardFocusManager manager = (KeyboardFocusManager)appcontext.get(KeyboardFocusManager.class);
        if (manager == null) {
            manager = new DefaultKeyboardFocusManager();
            appcontext.put(KeyboardFocusManager.class, manager);
        }
        return manager;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setCurrentKeyboardFocusManager(KeyboardFocusManager newManager) throws SecurityException {
        KeyboardFocusManager.checkReplaceKFMPermission();
        KeyboardFocusManager oldManager = null;
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            AppContext appcontext = AppContext.getAppContext();
            if (newManager != null) {
                oldManager = KeyboardFocusManager.getCurrentKeyboardFocusManager(appcontext);
                appcontext.put(KeyboardFocusManager.class, newManager);
            } else {
                oldManager = KeyboardFocusManager.getCurrentKeyboardFocusManager(appcontext);
                appcontext.remove(KeyboardFocusManager.class);
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            if (oldManager != null) {
                oldManager.firePropertyChange("managingFocus", Boolean.TRUE, Boolean.FALSE);
            }
            if (newManager != null) {
                newManager.firePropertyChange("managingFocus", Boolean.FALSE, Boolean.TRUE);
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void setCurrentSequencedEvent(SequencedEvent current) {
        Class<SequencedEvent> clazz = SequencedEvent.class;
        synchronized (SequencedEvent.class) {
            assert (current == null || this.currentSequencedEvent == null);
            this.currentSequencedEvent = current;
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final SequencedEvent getCurrentSequencedEvent() {
        Class<SequencedEvent> clazz = SequencedEvent.class;
        synchronized (SequencedEvent.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return this.currentSequencedEvent;
        }
    }

    static Set<AWTKeyStroke> initFocusTraversalKeysSet(String value, Set<AWTKeyStroke> targetSet) {
        StringTokenizer tokens = new StringTokenizer(value, ",");
        while (tokens.hasMoreTokens()) {
            targetSet.add(AWTKeyStroke.getAWTKeyStroke(tokens.nextToken()));
        }
        return targetSet.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(targetSet);
    }

    public KeyboardFocusManager() {
        AWTKeyStroke[][] defaultFocusTraversalKeyStrokes = new AWTKeyStroke[][]{{AWTKeyStroke.getAWTKeyStroke(9, 0, false), AWTKeyStroke.getAWTKeyStroke(9, 130, false)}, {AWTKeyStroke.getAWTKeyStroke(9, 65, false), AWTKeyStroke.getAWTKeyStroke(9, 195, false)}, new AWTKeyStroke[0], new AWTKeyStroke[0]};
        for (int i = 0; i < 4; ++i) {
            HashSet<AWTKeyStroke> work_set = new HashSet<AWTKeyStroke>();
            for (int j = 0; j < defaultFocusTraversalKeyStrokes[i].length; ++j) {
                work_set.add(defaultFocusTraversalKeyStrokes[i][j]);
            }
            this.defaultFocusTraversalKeys[i] = work_set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(work_set);
        }
        this.initPeer();
    }

    private void initPeer() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        KeyboardFocusManagerPeerProvider peerProvider = (KeyboardFocusManagerPeerProvider)((Object)tk);
        this.peer = peerProvider.getKeyboardFocusManagerPeer();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Component getFocusOwner() {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            if (focusOwner == null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return null;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return KeyboardFocusManager.focusOwner.appContext == AppContext.getAppContext() ? focusOwner : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Component getGlobalFocusOwner() throws SecurityException {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            this.checkKFMSecurity();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return focusOwner;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    protected void setGlobalFocusOwner(Component focusOwner) throws SecurityException {
        Component oldFocusOwner = null;
        boolean shouldFire = false;
        if (focusOwner == null || focusOwner.isFocusable()) {
            Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
            // MONITORENTER : java.awt.KeyboardFocusManager.class
            this.checkKFMSecurity();
            oldFocusOwner = this.getFocusOwner();
            try {
                this.fireVetoableChange("focusOwner", oldFocusOwner, focusOwner);
            }
            catch (PropertyVetoException e) {
                // MONITOREXIT : clazz
                return;
            }
            KeyboardFocusManager.focusOwner = focusOwner;
            if (!(focusOwner == null || this.getCurrentFocusCycleRoot() != null && focusOwner.isFocusCycleRoot(this.getCurrentFocusCycleRoot()))) {
                Container rootAncestor = focusOwner.getFocusCycleRootAncestor();
                if (rootAncestor == null && focusOwner instanceof Window) {
                    rootAncestor = (Container)focusOwner;
                }
                if (rootAncestor != null) {
                    this.setGlobalCurrentFocusCycleRootPriv(rootAncestor);
                }
            }
            shouldFire = true;
            // MONITOREXIT : clazz
        }
        if (!shouldFire) return;
        this.firePropertyChange("focusOwner", oldFocusOwner, focusOwner);
    }

    public void clearFocusOwner() {
        if (this.getFocusOwner() != null) {
            this.clearGlobalFocusOwner();
        }
    }

    public void clearGlobalFocusOwner() throws SecurityException {
        KeyboardFocusManager.checkReplaceKFMPermission();
        if (!GraphicsEnvironment.isHeadless()) {
            Toolkit.getDefaultToolkit();
            this._clearGlobalFocusOwner();
        }
    }

    private void _clearGlobalFocusOwner() {
        Window activeWindow = KeyboardFocusManager.markClearGlobalFocusOwner();
        this.peer.clearGlobalFocusOwner(activeWindow);
    }

    void clearGlobalFocusOwnerPriv() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                KeyboardFocusManager.this.clearGlobalFocusOwner();
                return null;
            }
        });
    }

    Component getNativeFocusOwner() {
        return this.peer.getCurrentFocusOwner();
    }

    void setNativeFocusOwner(Component comp) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("Calling peer {0} setCurrentFocusOwner for {1}", String.valueOf(this.peer), String.valueOf(comp));
        }
        this.peer.setCurrentFocusOwner(comp);
    }

    Window getNativeFocusedWindow() {
        return this.peer.getCurrentFocusedWindow();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Component getPermanentFocusOwner() {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            if (permanentFocusOwner == null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return null;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return KeyboardFocusManager.permanentFocusOwner.appContext == AppContext.getAppContext() ? permanentFocusOwner : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Component getGlobalPermanentFocusOwner() throws SecurityException {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            this.checkKFMSecurity();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return permanentFocusOwner;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    protected void setGlobalPermanentFocusOwner(Component permanentFocusOwner) throws SecurityException {
        Component oldPermanentFocusOwner = null;
        boolean shouldFire = false;
        if (permanentFocusOwner == null || permanentFocusOwner.isFocusable()) {
            Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
            // MONITORENTER : java.awt.KeyboardFocusManager.class
            this.checkKFMSecurity();
            oldPermanentFocusOwner = this.getPermanentFocusOwner();
            try {
                this.fireVetoableChange("permanentFocusOwner", oldPermanentFocusOwner, permanentFocusOwner);
            }
            catch (PropertyVetoException e) {
                // MONITOREXIT : clazz
                return;
            }
            KeyboardFocusManager.permanentFocusOwner = permanentFocusOwner;
            KeyboardFocusManager.setMostRecentFocusOwner(permanentFocusOwner);
            shouldFire = true;
            // MONITOREXIT : clazz
        }
        if (!shouldFire) return;
        this.firePropertyChange("permanentFocusOwner", oldPermanentFocusOwner, permanentFocusOwner);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Window getFocusedWindow() {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            if (focusedWindow == null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return null;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return KeyboardFocusManager.focusedWindow.appContext == AppContext.getAppContext() ? focusedWindow : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Window getGlobalFocusedWindow() throws SecurityException {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            this.checkKFMSecurity();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return focusedWindow;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    protected void setGlobalFocusedWindow(Window focusedWindow) throws SecurityException {
        Window oldFocusedWindow = null;
        boolean shouldFire = false;
        if (focusedWindow == null || focusedWindow.isFocusableWindow()) {
            Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
            // MONITORENTER : java.awt.KeyboardFocusManager.class
            this.checkKFMSecurity();
            oldFocusedWindow = this.getFocusedWindow();
            try {
                this.fireVetoableChange("focusedWindow", oldFocusedWindow, focusedWindow);
            }
            catch (PropertyVetoException e) {
                // MONITOREXIT : clazz
                return;
            }
            KeyboardFocusManager.focusedWindow = focusedWindow;
            shouldFire = true;
            // MONITOREXIT : clazz
        }
        if (!shouldFire) return;
        this.firePropertyChange("focusedWindow", oldFocusedWindow, focusedWindow);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Window getActiveWindow() {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            if (activeWindow == null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return null;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return KeyboardFocusManager.activeWindow.appContext == AppContext.getAppContext() ? activeWindow : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Window getGlobalActiveWindow() throws SecurityException {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            this.checkKFMSecurity();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return activeWindow;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setGlobalActiveWindow(Window activeWindow) throws SecurityException {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            this.checkKFMSecurity();
            Window oldActiveWindow = this.getActiveWindow();
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("Setting global active window to " + String.valueOf(activeWindow) + ", old active " + String.valueOf(oldActiveWindow));
            }
            try {
                this.fireVetoableChange("activeWindow", oldActiveWindow, activeWindow);
            }
            catch (PropertyVetoException e) {
                // ** MonitorExit[var3_2] (shouldn't be in output)
                return;
            }
            KeyboardFocusManager.activeWindow = activeWindow;
            // ** MonitorExit[var3_2] (shouldn't be in output)
            this.firePropertyChange("activeWindow", oldActiveWindow, activeWindow);
            return;
        }
    }

    public synchronized FocusTraversalPolicy getDefaultFocusTraversalPolicy() {
        return this.defaultPolicy;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDefaultFocusTraversalPolicy(FocusTraversalPolicy defaultPolicy) {
        FocusTraversalPolicy oldPolicy;
        if (defaultPolicy == null) {
            throw new IllegalArgumentException("default focus traversal policy cannot be null");
        }
        KeyboardFocusManager keyboardFocusManager = this;
        synchronized (keyboardFocusManager) {
            oldPolicy = this.defaultPolicy;
            this.defaultPolicy = defaultPolicy;
        }
        this.firePropertyChange("defaultFocusTraversalPolicy", oldPolicy, defaultPolicy);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDefaultFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
        Set<AWTKeyStroke> oldKeys;
        if (id < 0 || id >= 4) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        if (keystrokes == null) {
            throw new IllegalArgumentException("cannot set null Set of default focus traversal keys");
        }
        KeyboardFocusManager keyboardFocusManager = this;
        synchronized (keyboardFocusManager) {
            for (AWTKeyStroke aWTKeyStroke : keystrokes) {
                if (aWTKeyStroke == null) {
                    throw new IllegalArgumentException("cannot set null focus traversal key");
                }
                if (aWTKeyStroke.getKeyChar() != '\uffff') {
                    throw new IllegalArgumentException("focus traversal keys cannot map to KEY_TYPED events");
                }
                for (int i = 0; i < 4; ++i) {
                    if (i == id || !this.defaultFocusTraversalKeys[i].contains(aWTKeyStroke)) continue;
                    throw new IllegalArgumentException("focus traversal keys must be unique for a Component");
                }
            }
            oldKeys = this.defaultFocusTraversalKeys[id];
            this.defaultFocusTraversalKeys[id] = Collections.unmodifiableSet(new HashSet<AWTKeyStroke>(keystrokes));
        }
        this.firePropertyChange(defaultFocusTraversalKeyPropertyNames[id], oldKeys, keystrokes);
    }

    public Set<AWTKeyStroke> getDefaultFocusTraversalKeys(int id) {
        if (id < 0 || id >= 4) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        return this.defaultFocusTraversalKeys[id];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Container getCurrentFocusCycleRoot() {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            if (currentFocusCycleRoot == null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return null;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return KeyboardFocusManager.currentFocusCycleRoot.appContext == AppContext.getAppContext() ? currentFocusCycleRoot : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Container getGlobalCurrentFocusCycleRoot() throws SecurityException {
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            this.checkKFMSecurity();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return currentFocusCycleRoot;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setGlobalCurrentFocusCycleRoot(Container newFocusCycleRoot) throws SecurityException {
        KeyboardFocusManager.checkReplaceKFMPermission();
        Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            Container oldFocusCycleRoot = this.getCurrentFocusCycleRoot();
            currentFocusCycleRoot = newFocusCycleRoot;
            // ** MonitorExit[var3_2] (shouldn't be in output)
            this.firePropertyChange("currentFocusCycleRoot", oldFocusCycleRoot, newFocusCycleRoot);
            return;
        }
    }

    void setGlobalCurrentFocusCycleRootPriv(final Container newFocusCycleRoot) {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                KeyboardFocusManager.this.setGlobalCurrentFocusCycleRoot(newFocusCycleRoot);
                return null;
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.changeSupport == null) {
                    this.changeSupport = new PropertyChangeSupport(this);
                }
                this.changeSupport.addPropertyChangeListener(listener);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.changeSupport != null) {
                    this.changeSupport.removePropertyChangeListener(listener);
                }
            }
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (this.changeSupport == null) {
            this.changeSupport = new PropertyChangeSupport(this);
        }
        return this.changeSupport.getPropertyChangeListeners();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.changeSupport == null) {
                    this.changeSupport = new PropertyChangeSupport(this);
                }
                this.changeSupport.addPropertyChangeListener(propertyName, listener);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.changeSupport != null) {
                    this.changeSupport.removePropertyChangeListener(propertyName, listener);
                }
            }
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        if (this.changeSupport == null) {
            this.changeSupport = new PropertyChangeSupport(this);
        }
        return this.changeSupport.getPropertyChangeListeners(propertyName);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == newValue) {
            return;
        }
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport != null) {
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.vetoableSupport == null) {
                    this.vetoableSupport = new VetoableChangeSupport(this);
                }
                this.vetoableSupport.addVetoableChangeListener(listener);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.vetoableSupport != null) {
                    this.vetoableSupport.removeVetoableChangeListener(listener);
                }
            }
        }
    }

    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
        if (this.vetoableSupport == null) {
            this.vetoableSupport = new VetoableChangeSupport(this);
        }
        return this.vetoableSupport.getVetoableChangeListeners();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.vetoableSupport == null) {
                    this.vetoableSupport = new VetoableChangeSupport(this);
                }
                this.vetoableSupport.addVetoableChangeListener(propertyName, listener);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        if (listener != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.vetoableSupport != null) {
                    this.vetoableSupport.removeVetoableChangeListener(propertyName, listener);
                }
            }
        }
    }

    public synchronized VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        if (this.vetoableSupport == null) {
            this.vetoableSupport = new VetoableChangeSupport(this);
        }
        return this.vetoableSupport.getVetoableChangeListeners(propertyName);
    }

    protected void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        if (oldValue == newValue) {
            return;
        }
        VetoableChangeSupport vetoableSupport = this.vetoableSupport;
        if (vetoableSupport != null) {
            vetoableSupport.fireVetoableChange(propertyName, oldValue, newValue);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addKeyEventDispatcher(KeyEventDispatcher dispatcher) {
        if (dispatcher != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.keyEventDispatchers == null) {
                    this.keyEventDispatchers = new LinkedList();
                }
                this.keyEventDispatchers.add(dispatcher);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeKeyEventDispatcher(KeyEventDispatcher dispatcher) {
        if (dispatcher != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.keyEventDispatchers != null) {
                    this.keyEventDispatchers.remove(dispatcher);
                }
            }
        }
    }

    protected synchronized List<KeyEventDispatcher> getKeyEventDispatchers() {
        return this.keyEventDispatchers != null ? (List)this.keyEventDispatchers.clone() : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addKeyEventPostProcessor(KeyEventPostProcessor processor) {
        if (processor != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.keyEventPostProcessors == null) {
                    this.keyEventPostProcessors = new LinkedList();
                }
                this.keyEventPostProcessors.add(processor);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeKeyEventPostProcessor(KeyEventPostProcessor processor) {
        if (processor != null) {
            KeyboardFocusManager keyboardFocusManager = this;
            synchronized (keyboardFocusManager) {
                if (this.keyEventPostProcessors != null) {
                    this.keyEventPostProcessors.remove(processor);
                }
            }
        }
    }

    protected List<KeyEventPostProcessor> getKeyEventPostProcessors() {
        return this.keyEventPostProcessors != null ? (List)this.keyEventPostProcessors.clone() : null;
    }

    static void setMostRecentFocusOwner(Component component) {
        Component window = component;
        while (window != null && !(window instanceof Window)) {
            window = window.parent;
        }
        if (window != null) {
            KeyboardFocusManager.setMostRecentFocusOwner((Window)window, component);
        }
    }

    static synchronized void setMostRecentFocusOwner(Window window, Component component) {
        WeakReference<Component> weakValue = null;
        if (component != null) {
            weakValue = new WeakReference<Component>(component);
        }
        mostRecentFocusOwners.put(window, weakValue);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void clearMostRecentFocusOwner(Component comp) {
        Container window;
        if (comp == null) {
            return;
        }
        Class<KeyboardFocusManager> clazz = comp.getTreeLock();
        synchronized (clazz) {
            for (window = comp.getParent(); window != null && !(window instanceof Window); window = window.getParent()) {
            }
        }
        clazz = KeyboardFocusManager.class;
        synchronized (KeyboardFocusManager.class) {
            Window realWindow;
            if (window != null && KeyboardFocusManager.getMostRecentFocusOwner((Window)window) == comp) {
                KeyboardFocusManager.setMostRecentFocusOwner((Window)window, null);
            }
            if (window != null && (realWindow = (Window)window).getTemporaryLostComponent() == comp) {
                realWindow.setTemporaryLostComponent(null);
            }
            // ** MonitorExit[var2_1] (shouldn't be in output)
            return;
        }
    }

    static synchronized Component getMostRecentFocusOwner(Window window) {
        WeakReference<Component> weakValue = mostRecentFocusOwners.get(window);
        return weakValue == null ? null : (Component)weakValue.get();
    }

    public abstract boolean dispatchEvent(AWTEvent var1);

    public final void redispatchEvent(Component target, AWTEvent e) {
        e.focusManagerIsDispatching = true;
        target.dispatchEvent(e);
        e.focusManagerIsDispatching = false;
    }

    @Override
    public abstract boolean dispatchKeyEvent(KeyEvent var1);

    @Override
    public abstract boolean postProcessKeyEvent(KeyEvent var1);

    public abstract void processKeyEvent(Component var1, KeyEvent var2);

    protected abstract void enqueueKeyEvents(long var1, Component var3);

    protected abstract void dequeueKeyEvents(long var1, Component var3);

    protected abstract void discardKeyEvents(Component var1);

    public abstract void focusNextComponent(Component var1);

    public abstract void focusPreviousComponent(Component var1);

    public abstract void upFocusCycle(Component var1);

    public abstract void downFocusCycle(Container var1);

    public final void focusNextComponent() {
        Component focusOwner = this.getFocusOwner();
        if (focusOwner != null) {
            this.focusNextComponent(focusOwner);
        }
    }

    public final void focusPreviousComponent() {
        Component focusOwner = this.getFocusOwner();
        if (focusOwner != null) {
            this.focusPreviousComponent(focusOwner);
        }
    }

    public final void upFocusCycle() {
        Component focusOwner = this.getFocusOwner();
        if (focusOwner != null) {
            this.upFocusCycle(focusOwner);
        }
    }

    public final void downFocusCycle() {
        Component focusOwner = this.getFocusOwner();
        if (focusOwner instanceof Container) {
            this.downFocusCycle((Container)focusOwner);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void dumpRequests() {
        System.err.println(">>> Requests dump, time: " + System.currentTimeMillis());
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            for (HeavyweightFocusRequest req : heavyweightRequests) {
                System.err.println(">>> Req: " + String.valueOf(req));
            }
        }
        System.err.println("");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean processSynchronousLightweightTransfer(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time) {
        Window parentWindow = SunToolkit.getContainingWindow(heavyweight);
        if (parentWindow == null || !parentWindow.syncLWRequests) {
            return false;
        }
        if (descendant == null) {
            descendant = heavyweight;
        }
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager(SunToolkit.targetToAppContext(descendant));
        FocusEvent currentFocusOwnerEvent = null;
        FocusEvent newFocusOwnerEvent = null;
        Component currentFocusOwner = manager.getGlobalFocusOwner();
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getLastHWRequest();
            if (hwFocusRequest == null && heavyweight == manager.getNativeFocusOwner() && allowSyncFocusRequests) {
                if (descendant == currentFocusOwner) {
                    return true;
                }
                manager.enqueueKeyEvents(time, descendant);
                hwFocusRequest = new HeavyweightFocusRequest(heavyweight, descendant, temporary, FocusEvent.Cause.UNKNOWN);
                heavyweightRequests.add(hwFocusRequest);
                if (currentFocusOwner != null) {
                    currentFocusOwnerEvent = new FocusEvent(currentFocusOwner, 1005, temporary, descendant);
                }
                newFocusOwnerEvent = new FocusEvent(descendant, 1004, temporary, currentFocusOwner);
            }
        }
        boolean result = false;
        boolean clearing = clearingCurrentLightweightRequests;
        Throwable caughtEx = null;
        try {
            clearingCurrentLightweightRequests = false;
            Object object = Component.LOCK;
            synchronized (object) {
                if (currentFocusOwnerEvent != null && currentFocusOwner != null) {
                    currentFocusOwnerEvent.isPosted = true;
                    caughtEx = KeyboardFocusManager.dispatchAndCatchException(caughtEx, currentFocusOwner, currentFocusOwnerEvent);
                    result = true;
                }
                if (newFocusOwnerEvent != null && descendant != null) {
                    newFocusOwnerEvent.isPosted = true;
                    caughtEx = KeyboardFocusManager.dispatchAndCatchException(caughtEx, descendant, newFocusOwnerEvent);
                    result = true;
                }
            }
        }
        finally {
            clearingCurrentLightweightRequests = clearing;
        }
        if (caughtEx instanceof RuntimeException) {
            throw (RuntimeException)caughtEx;
        }
        if (caughtEx instanceof Error) {
            throw (Error)caughtEx;
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static int shouldNativelyFocusHeavyweight(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEvent.Cause cause) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            if (heavyweight == null) {
                log.fine("Assertion (heavyweight != null) failed");
            }
            if (time == 0L) {
                log.fine("Assertion (time != 0) failed");
            }
        }
        if (descendant == null) {
            descendant = heavyweight;
        }
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager(SunToolkit.targetToAppContext(descendant));
        KeyboardFocusManager thisManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Component currentFocusOwner = thisManager.getGlobalFocusOwner();
        Component nativeFocusOwner = thisManager.getNativeFocusOwner();
        Window nativeFocusedWindow = thisManager.getNativeFocusedWindow();
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("SNFH for {0} in {1}", String.valueOf(descendant), String.valueOf(heavyweight));
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("0. Current focus owner {0}", String.valueOf(currentFocusOwner));
            focusLog.finest("0. Native focus owner {0}", String.valueOf(nativeFocusOwner));
            focusLog.finest("0. Native focused window {0}", String.valueOf(nativeFocusedWindow));
        }
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getLastHWRequest();
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Request {0}", String.valueOf(hwFocusRequest));
            }
            if (hwFocusRequest == null && heavyweight == nativeFocusOwner && heavyweight.getContainingWindow() == nativeFocusedWindow) {
                if (descendant == currentFocusOwner) {
                    if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                        focusLog.finest("1. SNFH_FAILURE for {0}", String.valueOf(descendant));
                    }
                    return 0;
                }
                manager.enqueueKeyEvents(time, descendant);
                hwFocusRequest = new HeavyweightFocusRequest(heavyweight, descendant, temporary, cause);
                heavyweightRequests.add(hwFocusRequest);
                if (currentFocusOwner != null) {
                    FocusEvent currentFocusOwnerEvent = new FocusEvent(currentFocusOwner, 1005, temporary, descendant, cause);
                    SunToolkit.postEvent(currentFocusOwner.appContext, currentFocusOwnerEvent);
                }
                FocusEvent newFocusOwnerEvent = new FocusEvent(descendant, 1004, temporary, currentFocusOwner, cause);
                SunToolkit.postEvent(descendant.appContext, newFocusOwnerEvent);
                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("2. SNFH_HANDLED for {0}", String.valueOf(descendant));
                }
                return 1;
            }
            if (hwFocusRequest != null && hwFocusRequest.heavyweight == heavyweight) {
                if (hwFocusRequest.addLightweightRequest(descendant, temporary, cause)) {
                    manager.enqueueKeyEvents(time, descendant);
                }
                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("3. SNFH_HANDLED for lightweight" + String.valueOf(descendant) + " in " + String.valueOf(heavyweight));
                }
                return 1;
            }
            if (!focusedWindowChangeAllowed) {
                if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) {
                    int size = heavyweightRequests.size();
                    hwFocusRequest = size >= 2 ? heavyweightRequests.get(size - 2) : null;
                }
                if (KeyboardFocusManager.focusedWindowChanged(heavyweight, hwFocusRequest != null ? hwFocusRequest.heavyweight : nativeFocusedWindow)) {
                    if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                        focusLog.finest("4. SNFH_FAILURE for " + String.valueOf(descendant));
                    }
                    return 0;
                }
            }
            manager.enqueueKeyEvents(time, descendant);
            heavyweightRequests.add(new HeavyweightFocusRequest(heavyweight, descendant, temporary, cause));
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("5. SNFH_PROCEED for " + String.valueOf(descendant));
            }
            return 2;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Window markClearGlobalFocusOwner() {
        Window nativeFocusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getNativeFocusedWindow();
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            Container activeWindow;
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getLastHWRequest();
            if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) {
                return null;
            }
            heavyweightRequests.add(HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER);
            Window window = activeWindow = hwFocusRequest != null ? SunToolkit.getContainingWindow(hwFocusRequest.heavyweight) : nativeFocusedWindow;
            while (activeWindow != null && !(activeWindow instanceof Frame) && !(activeWindow instanceof Dialog)) {
                activeWindow = activeWindow.getParent_NoClientCode();
            }
            return activeWindow;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Component getCurrentWaitingRequest(Component parent) {
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            LightweightFocusRequest lwFocusRequest;
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getFirstHWRequest();
            if (hwFocusRequest != null && hwFocusRequest.heavyweight == parent && (lwFocusRequest = hwFocusRequest.lightweightRequests.getFirst()) != null) {
                return lwFocusRequest.component;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean isAutoFocusTransferEnabled() {
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            return heavyweightRequests.size() == 0 && !disableRestoreFocus && null == currentLightweightRequests;
        }
    }

    static boolean isAutoFocusTransferEnabledFor(Component comp) {
        return KeyboardFocusManager.isAutoFocusTransferEnabled() && comp.isAutoFocusTransferOnDisposal();
    }

    private static Throwable dispatchAndCatchException(Throwable ex, Component comp, FocusEvent event) {
        Throwable retEx = null;
        try {
            comp.dispatchEvent(event);
        }
        catch (Error | RuntimeException e) {
            retEx = e;
        }
        if (retEx != null) {
            if (ex != null) {
                KeyboardFocusManager.handleException(ex);
            }
            return retEx;
        }
        return ex;
    }

    private static void handleException(Throwable ex) {
        ex.printStackTrace();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void processCurrentLightweightRequests() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        LinkedList<LightweightFocusRequest> localLightweightRequests = null;
        Component globalFocusOwner = manager.getGlobalFocusOwner();
        if (globalFocusOwner != null && globalFocusOwner.appContext != AppContext.getAppContext()) {
            return;
        }
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            if (currentLightweightRequests == null) {
                return;
            }
            clearingCurrentLightweightRequests = true;
            disableRestoreFocus = true;
            localLightweightRequests = currentLightweightRequests;
            allowSyncFocusRequests = localLightweightRequests.size() < 2;
            currentLightweightRequests = null;
        }
        Throwable caughtEx = null;
        try {
            if (localLightweightRequests != null) {
                Component lastFocusOwner = null;
                Component currentFocusOwner = null;
                Iterator iter = localLightweightRequests.iterator();
                while (iter.hasNext()) {
                    currentFocusOwner = manager.getGlobalFocusOwner();
                    LightweightFocusRequest lwFocusRequest = (LightweightFocusRequest)iter.next();
                    if (!iter.hasNext()) {
                        disableRestoreFocus = false;
                    }
                    FocusEvent currentFocusOwnerEvent = null;
                    if (currentFocusOwner != null) {
                        currentFocusOwnerEvent = new FocusEvent(currentFocusOwner, 1005, lwFocusRequest.temporary, lwFocusRequest.component, lwFocusRequest.cause);
                    }
                    FocusEvent newFocusOwnerEvent = new FocusEvent(lwFocusRequest.component, 1004, lwFocusRequest.temporary, currentFocusOwner == null ? lastFocusOwner : currentFocusOwner, lwFocusRequest.cause);
                    if (currentFocusOwner != null) {
                        currentFocusOwnerEvent.isPosted = true;
                        caughtEx = KeyboardFocusManager.dispatchAndCatchException(caughtEx, currentFocusOwner, currentFocusOwnerEvent);
                    }
                    newFocusOwnerEvent.isPosted = true;
                    caughtEx = KeyboardFocusManager.dispatchAndCatchException(caughtEx, lwFocusRequest.component, newFocusOwnerEvent);
                    if (manager.getGlobalFocusOwner() != lwFocusRequest.component) continue;
                    lastFocusOwner = lwFocusRequest.component;
                }
            }
        }
        finally {
            clearingCurrentLightweightRequests = false;
            disableRestoreFocus = false;
            localLightweightRequests = null;
            allowSyncFocusRequests = true;
        }
        if (caughtEx instanceof RuntimeException) {
            throw (RuntimeException)caughtEx;
        }
        if (caughtEx instanceof Error) {
            throw (Error)caughtEx;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static FocusEvent retargetUnexpectedFocusEvent(FocusEvent fe) {
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            if (KeyboardFocusManager.removeFirstRequest()) {
                return (FocusEvent)KeyboardFocusManager.retargetFocusEvent(fe);
            }
            Component source = fe.getComponent();
            Component opposite = fe.getOppositeComponent();
            boolean temporary = false;
            if (fe.getID() == 1005 && (opposite == null || KeyboardFocusManager.isTemporary(opposite, source))) {
                temporary = true;
            }
            return new FocusEvent(source, fe.getID(), temporary, opposite, FocusEvent.Cause.UNEXPECTED);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static FocusEvent retargetFocusGained(FocusEvent fe) {
        assert (fe.getID() == 1004);
        Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getGlobalFocusOwner();
        Component source = fe.getComponent();
        Component opposite = fe.getOppositeComponent();
        Component nativeSource = KeyboardFocusManager.getHeavyweight(source);
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getFirstHWRequest();
            if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) {
                return KeyboardFocusManager.retargetUnexpectedFocusEvent(fe);
            }
            if (source != null && nativeSource == null && hwFocusRequest != null && source == hwFocusRequest.getFirstLightweightRequest().component) {
                nativeSource = source = hwFocusRequest.heavyweight;
            }
            if (hwFocusRequest != null && nativeSource == hwFocusRequest.heavyweight) {
                boolean temporary;
                heavyweightRequests.removeFirst();
                LightweightFocusRequest lwFocusRequest = hwFocusRequest.lightweightRequests.removeFirst();
                Component newSource = lwFocusRequest.component;
                if (currentFocusOwner != null) {
                    newFocusOwner = newSource;
                }
                boolean bl = temporary = opposite == null || KeyboardFocusManager.isTemporary(newSource, opposite) ? false : lwFocusRequest.temporary;
                if (hwFocusRequest.lightweightRequests.size() > 0) {
                    currentLightweightRequests = hwFocusRequest.lightweightRequests;
                    EventQueue.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            KeyboardFocusManager.processCurrentLightweightRequests();
                        }
                    });
                }
                return new FocusEvent(newSource, 1004, temporary, opposite, lwFocusRequest.cause);
            }
            if (currentFocusOwner != null && currentFocusOwner.getContainingWindow() == source && (hwFocusRequest == null || source != hwFocusRequest.heavyweight)) {
                return new FocusEvent(currentFocusOwner, 1004, false, null, FocusEvent.Cause.ACTIVATION);
            }
            return KeyboardFocusManager.retargetUnexpectedFocusEvent(fe);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static FocusEvent retargetFocusLost(FocusEvent fe) {
        assert (fe.getID() == 1005);
        Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getGlobalFocusOwner();
        Component opposite = fe.getOppositeComponent();
        Component nativeOpposite = KeyboardFocusManager.getHeavyweight(opposite);
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getFirstHWRequest();
            if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) {
                if (currentFocusOwner != null) {
                    heavyweightRequests.removeFirst();
                    return new FocusEvent(currentFocusOwner, 1005, false, null, FocusEvent.Cause.CLEAR_GLOBAL_FOCUS_OWNER);
                }
            } else {
                if (opposite == null) {
                    if (currentFocusOwner != null) {
                        return new FocusEvent(currentFocusOwner, 1005, true, null, FocusEvent.Cause.ACTIVATION);
                    }
                    return fe;
                }
                if (hwFocusRequest != null && (nativeOpposite == hwFocusRequest.heavyweight || nativeOpposite == null && opposite == hwFocusRequest.getFirstLightweightRequest().component)) {
                    if (currentFocusOwner == null) {
                        return fe;
                    }
                    LightweightFocusRequest lwFocusRequest = hwFocusRequest.lightweightRequests.getFirst();
                    boolean temporary = KeyboardFocusManager.isTemporary(opposite, currentFocusOwner) ? true : lwFocusRequest.temporary;
                    return new FocusEvent(currentFocusOwner, 1005, temporary, lwFocusRequest.component, lwFocusRequest.cause);
                }
                if (KeyboardFocusManager.focusedWindowChanged(opposite, currentFocusOwner)) {
                    if (!fe.isTemporary() && currentFocusOwner != null) {
                        fe = new FocusEvent(currentFocusOwner, 1005, true, opposite, FocusEvent.Cause.ACTIVATION);
                    }
                    return fe;
                }
            }
            return KeyboardFocusManager.retargetUnexpectedFocusEvent(fe);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static AWTEvent retargetFocusEvent(AWTEvent event) {
        if (clearingCurrentLightweightRequests) {
            return event;
        }
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            if (event instanceof FocusEvent || event instanceof WindowEvent) {
                focusLog.finer(">>> {0}", String.valueOf(event));
            }
            if (focusLog.isLoggable(PlatformLogger.Level.FINER) && event instanceof KeyEvent) {
                focusLog.finer("    focus owner is {0}", String.valueOf(manager.getGlobalFocusOwner()));
                focusLog.finer(">>> {0}", String.valueOf(event));
            }
        }
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            if (newFocusOwner != null && event.getID() == 1005) {
                FocusEvent fe = (FocusEvent)event;
                if (manager.getGlobalFocusOwner() == fe.getComponent() && fe.getOppositeComponent() == newFocusOwner) {
                    newFocusOwner = null;
                    return event;
                }
            }
        }
        KeyboardFocusManager.processCurrentLightweightRequests();
        switch (event.getID()) {
            case 1004: {
                event = KeyboardFocusManager.retargetFocusGained((FocusEvent)event);
                break;
            }
            case 1005: {
                event = KeyboardFocusManager.retargetFocusLost((FocusEvent)event);
                break;
            }
        }
        return event;
    }

    void clearMarkers() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean removeFirstRequest() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getFirstHWRequest();
            if (hwFocusRequest != null) {
                heavyweightRequests.removeFirst();
                if (hwFocusRequest.lightweightRequests != null) {
                    for (LightweightFocusRequest lwFocusRequest : hwFocusRequest.lightweightRequests) {
                        manager.dequeueKeyEvents(-1L, lwFocusRequest.component);
                    }
                }
            }
            if (heavyweightRequests.size() == 0) {
                manager.clearMarkers();
            }
            return heavyweightRequests.size() > 0;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void removeLastFocusRequest(Component heavyweight) {
        if (log.isLoggable(PlatformLogger.Level.FINE) && heavyweight == null) {
            log.fine("Assertion (heavyweight != null) failed");
        }
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            HeavyweightFocusRequest hwFocusRequest = KeyboardFocusManager.getLastHWRequest();
            if (hwFocusRequest != null && hwFocusRequest.heavyweight == heavyweight) {
                heavyweightRequests.removeLast();
            }
            if (heavyweightRequests.size() == 0) {
                manager.clearMarkers();
            }
        }
    }

    private static boolean focusedWindowChanged(Component to, Component from) {
        Window wto = SunToolkit.getContainingWindow(to);
        Window wfrom = SunToolkit.getContainingWindow(from);
        if (wto == null && wfrom == null) {
            return true;
        }
        if (wto == null) {
            return true;
        }
        if (wfrom == null) {
            return true;
        }
        return wto != wfrom;
    }

    private static boolean isTemporary(Component to, Component from) {
        Window wto = SunToolkit.getContainingWindow(to);
        Window wfrom = SunToolkit.getContainingWindow(from);
        if (wto == null && wfrom == null) {
            return false;
        }
        if (wto == null) {
            return true;
        }
        if (wfrom == null) {
            return false;
        }
        return wto != wfrom;
    }

    static Component getHeavyweight(Component comp) {
        if (comp == null || comp.peer == null) {
            return null;
        }
        if (comp.peer instanceof LightweightPeer) {
            return comp.getNativeContainer();
        }
        return comp;
    }

    private static boolean isProxyActiveImpl(KeyEvent e) {
        return AWTAccessor.getKeyEventAccessor().isProxyActive(e);
    }

    static boolean isProxyActive(KeyEvent e) {
        if (!GraphicsEnvironment.isHeadless()) {
            return KeyboardFocusManager.isProxyActiveImpl(e);
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static HeavyweightFocusRequest getLastHWRequest() {
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            return heavyweightRequests.size() > 0 ? heavyweightRequests.getLast() : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static HeavyweightFocusRequest getFirstHWRequest() {
        LinkedList<HeavyweightFocusRequest> linkedList = heavyweightRequests;
        synchronized (linkedList) {
            return heavyweightRequests.size() > 0 ? heavyweightRequests.getFirst() : null;
        }
    }

    private static void checkReplaceKFMPermission() throws SecurityException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (replaceKeyboardFocusManagerPermission == null) {
                replaceKeyboardFocusManagerPermission = new AWTPermission("replaceKeyboardFocusManager");
            }
            security.checkPermission(replaceKeyboardFocusManagerPermission);
        }
    }

    private void checkKFMSecurity() throws SecurityException {
        if (this != KeyboardFocusManager.getCurrentKeyboardFocusManager()) {
            KeyboardFocusManager.checkReplaceKFMPermission();
        }
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            KeyboardFocusManager.initIDs();
        }
        AWTAccessor.setKeyboardFocusManagerAccessor(new AWTAccessor.KeyboardFocusManagerAccessor(){

            @Override
            public int shouldNativelyFocusHeavyweight(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEvent.Cause cause) {
                return KeyboardFocusManager.shouldNativelyFocusHeavyweight(heavyweight, descendant, temporary, focusedWindowChangeAllowed, time, cause);
            }

            @Override
            public boolean processSynchronousLightweightTransfer(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time) {
                return KeyboardFocusManager.processSynchronousLightweightTransfer(heavyweight, descendant, temporary, focusedWindowChangeAllowed, time);
            }

            @Override
            public void removeLastFocusRequest(Component heavyweight) {
                KeyboardFocusManager.removeLastFocusRequest(heavyweight);
            }

            @Override
            public Component getMostRecentFocusOwner(Window window) {
                return KeyboardFocusManager.getMostRecentFocusOwner(window);
            }

            @Override
            public void setMostRecentFocusOwner(Window window, Component component) {
                KeyboardFocusManager.setMostRecentFocusOwner(window, component);
            }

            @Override
            public KeyboardFocusManager getCurrentKeyboardFocusManager(AppContext ctx) {
                return KeyboardFocusManager.getCurrentKeyboardFocusManager(ctx);
            }

            @Override
            public Container getCurrentFocusCycleRoot() {
                return currentFocusCycleRoot;
            }
        });
        log = PlatformLogger.getLogger("java.awt.KeyboardFocusManager");
        defaultFocusTraversalKeyPropertyNames = new String[]{"forwardDefaultFocusTraversalKeys", "backwardDefaultFocusTraversalKeys", "upCycleDefaultFocusTraversalKeys", "downCycleDefaultFocusTraversalKeys"};
        mostRecentFocusOwners = new WeakHashMap<Window, WeakReference<Component>>();
        heavyweightRequests = new LinkedList();
        allowSyncFocusRequests = true;
        newFocusOwner = null;
    }

    private static final class HeavyweightFocusRequest {
        final Component heavyweight;
        final LinkedList<LightweightFocusRequest> lightweightRequests;
        static final HeavyweightFocusRequest CLEAR_GLOBAL_FOCUS_OWNER = new HeavyweightFocusRequest();

        private HeavyweightFocusRequest() {
            this.heavyweight = null;
            this.lightweightRequests = null;
        }

        HeavyweightFocusRequest(Component heavyweight, Component descendant, boolean temporary, FocusEvent.Cause cause) {
            if (log.isLoggable(PlatformLogger.Level.FINE) && heavyweight == null) {
                log.fine("Assertion (heavyweight != null) failed");
            }
            this.heavyweight = heavyweight;
            this.lightweightRequests = new LinkedList();
            this.addLightweightRequest(descendant, temporary, cause);
        }

        boolean addLightweightRequest(Component descendant, boolean temporary, FocusEvent.Cause cause) {
            Component lastDescendant;
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                if (this == CLEAR_GLOBAL_FOCUS_OWNER) {
                    log.fine("Assertion (this != HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) failed");
                }
                if (descendant == null) {
                    log.fine("Assertion (descendant != null) failed");
                }
            }
            Component component = lastDescendant = this.lightweightRequests.size() > 0 ? this.lightweightRequests.getLast().component : null;
            if (descendant != lastDescendant) {
                this.lightweightRequests.add(new LightweightFocusRequest(descendant, temporary, cause));
                return true;
            }
            return false;
        }

        LightweightFocusRequest getFirstLightweightRequest() {
            if (this == CLEAR_GLOBAL_FOCUS_OWNER) {
                return null;
            }
            return this.lightweightRequests.getFirst();
        }

        public String toString() {
            boolean first = true;
            String str = "HeavyweightFocusRequest[heavyweight=" + String.valueOf(this.heavyweight) + ",lightweightRequests=";
            if (this.lightweightRequests == null) {
                str = str + "null";
            } else {
                str = str + "[";
                for (LightweightFocusRequest lwRequest : this.lightweightRequests) {
                    if (first) {
                        first = false;
                    } else {
                        str = str + ",";
                    }
                    str = str + String.valueOf(lwRequest);
                }
                str = str + "]";
            }
            str = str + "]";
            return str;
        }
    }

    private static final class LightweightFocusRequest {
        final Component component;
        final boolean temporary;
        final FocusEvent.Cause cause;

        LightweightFocusRequest(Component component, boolean temporary, FocusEvent.Cause cause) {
            this.component = component;
            this.temporary = temporary;
            this.cause = cause;
        }

        public String toString() {
            return "LightweightFocusRequest[component=" + String.valueOf(this.component) + ",temporary=" + this.temporary + ", cause=" + String.valueOf((Object)this.cause) + "]";
        }
    }
}

