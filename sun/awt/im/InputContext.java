/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.im;

import java.awt.AWTEvent;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.im.InputMethodRequests;
import java.awt.im.spi.InputMethod;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import sun.awt.SunToolkit;
import sun.awt.im.InputMethodAdapter;
import sun.awt.im.InputMethodContext;
import sun.awt.im.InputMethodLocator;
import sun.awt.im.InputMethodManager;
import sun.awt.im.InputMethodWindow;
import sun.util.logging.PlatformLogger;

public class InputContext
extends java.awt.im.InputContext
implements ComponentListener,
WindowListener {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.im.InputContext");
    private InputMethodLocator inputMethodLocator;
    private InputMethod inputMethod;
    private boolean inputMethodCreationFailed;
    private HashMap<InputMethodLocator, InputMethod> usedInputMethods;
    private Component currentClientComponent;
    private Component awtFocussedComponent;
    private boolean isInputMethodActive;
    private Character.Subset[] characterSubsets = null;
    private boolean compositionAreaHidden = false;
    private static InputContext inputMethodWindowContext;
    private static InputMethod previousInputMethod;
    private boolean clientWindowNotificationEnabled = false;
    private Window clientWindowListened;
    private Rectangle clientWindowLocation = null;
    private HashMap<InputMethod, Boolean> perInputMethodState;
    private static AWTKeyStroke inputMethodSelectionKey;
    private static boolean inputMethodSelectionKeyInitialized;
    private static final String inputMethodSelectionKeyPath = "/java/awt/im/selectionKey";
    private static final String inputMethodSelectionKeyCodeName = "keyCode";
    private static final String inputMethodSelectionKeyModifiersName = "modifiers";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected InputContext() {
        InputMethodManager imm = InputMethodManager.getInstance();
        Class<InputContext> clazz = InputContext.class;
        synchronized (InputContext.class) {
            if (!inputMethodSelectionKeyInitialized) {
                inputMethodSelectionKeyInitialized = true;
                if (imm.hasMultipleInputMethods()) {
                    this.initializeInputMethodSelectionKey();
                }
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            this.selectInputMethod(imm.getDefaultKeyboardLocale());
            return;
        }
    }

    @Override
    public synchronized boolean selectInputMethod(Locale locale) {
        InputMethodLocator newLocator;
        if (locale == null) {
            throw new NullPointerException();
        }
        if (this.inputMethod != null) {
            if (this.inputMethod.setLocale(locale)) {
                return true;
            }
        } else if (this.inputMethodLocator != null && this.inputMethodLocator.isLocaleAvailable(locale)) {
            this.inputMethodLocator = this.inputMethodLocator.deriveLocator(locale);
            return true;
        }
        if ((newLocator = InputMethodManager.getInstance().findInputMethod(locale)) != null) {
            this.changeInputMethod(newLocator);
            return true;
        }
        if (this.inputMethod == null && this.inputMethodLocator != null) {
            this.inputMethod = this.getInputMethod();
            if (this.inputMethod != null) {
                return this.inputMethod.setLocale(locale);
            }
        }
        return false;
    }

    @Override
    public Locale getLocale() {
        if (this.inputMethod != null) {
            return this.inputMethod.getLocale();
        }
        if (this.inputMethodLocator != null) {
            return this.inputMethodLocator.getLocale();
        }
        return null;
    }

    @Override
    public void setCharacterSubsets(Character.Subset[] subsets) {
        if (subsets == null) {
            this.characterSubsets = null;
        } else {
            this.characterSubsets = new Character.Subset[subsets.length];
            System.arraycopy(subsets, 0, this.characterSubsets, 0, this.characterSubsets.length);
        }
        if (this.inputMethod != null) {
            this.inputMethod.setCharacterSubsets(subsets);
        }
    }

    @Override
    public synchronized void reconvert() {
        InputMethod inputMethod = this.getInputMethod();
        if (inputMethod == null) {
            throw new UnsupportedOperationException();
        }
        inputMethod.reconvert();
    }

    @Override
    public void dispatchEvent(AWTEvent event) {
        Component opposite;
        if (event instanceof InputMethodEvent) {
            return;
        }
        if (event instanceof FocusEvent && (opposite = ((FocusEvent)event).getOppositeComponent()) != null && InputContext.getComponentWindow(opposite) instanceof InputMethodWindow && opposite.getInputContext() == this) {
            return;
        }
        InputMethod inputMethod = this.getInputMethod();
        int id = event.getID();
        switch (id) {
            case 1004: {
                this.focusGained((Component)event.getSource());
                break;
            }
            case 1005: {
                this.focusLost((Component)event.getSource(), ((FocusEvent)event).isTemporary());
                break;
            }
            case 401: {
                if (this.checkInputMethodSelectionKey((KeyEvent)event)) {
                    InputMethodManager.getInstance().notifyChangeRequestByHotKey((Component)event.getSource());
                    break;
                }
            }
            default: {
                if (inputMethod == null || !(event instanceof InputEvent)) break;
                inputMethod.dispatchEvent(event);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void focusGained(Component source) {
        Object object = source.getTreeLock();
        synchronized (object) {
            InputContext inputContext = this;
            synchronized (inputContext) {
                InputMethodContext inputContext2;
                if (!"sun.awt.im.CompositionArea".equals(source.getClass().getName()) && !(InputContext.getComponentWindow(source) instanceof InputMethodWindow)) {
                    if (!source.isDisplayable()) {
                        return;
                    }
                    if (this.inputMethod != null && this.currentClientComponent != null && this.currentClientComponent != source) {
                        if (!this.isInputMethodActive) {
                            this.activateInputMethod(false);
                        }
                        this.endComposition();
                        this.deactivateInputMethod(false);
                    }
                    this.currentClientComponent = source;
                }
                this.awtFocussedComponent = source;
                if (this.inputMethod instanceof InputMethodAdapter) {
                    ((InputMethodAdapter)this.inputMethod).setAWTFocussedComponent(source);
                }
                if (!this.isInputMethodActive) {
                    this.activateInputMethod(true);
                }
                if (!(inputContext2 = (InputMethodContext)this).isCompositionAreaVisible()) {
                    InputMethodRequests req = source.getInputMethodRequests();
                    if (req != null && inputContext2.useBelowTheSpotInput()) {
                        inputContext2.setCompositionAreaUndecorated(true);
                    } else {
                        inputContext2.setCompositionAreaUndecorated(false);
                    }
                }
                if (this.compositionAreaHidden) {
                    ((InputMethodContext)this).setCompositionAreaVisible(true);
                    this.compositionAreaHidden = false;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void activateInputMethod(boolean updateCompositionArea) {
        if (inputMethodWindowContext != null && inputMethodWindowContext != this && InputContext.inputMethodWindowContext.inputMethodLocator != null && !InputContext.inputMethodWindowContext.inputMethodLocator.sameInputMethod(this.inputMethodLocator) && InputContext.inputMethodWindowContext.inputMethod != null) {
            InputContext.inputMethodWindowContext.inputMethod.hideWindows();
        }
        inputMethodWindowContext = this;
        if (this.inputMethod != null) {
            Boolean state;
            if (previousInputMethod != this.inputMethod && previousInputMethod instanceof InputMethodAdapter) {
                ((InputMethodAdapter)previousInputMethod).stopListening();
            }
            previousInputMethod = null;
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("Current client component " + String.valueOf(this.currentClientComponent));
            }
            if (this.inputMethod instanceof InputMethodAdapter) {
                ((InputMethodAdapter)this.inputMethod).setClientComponent(this.currentClientComponent);
            }
            this.inputMethod.activate();
            this.isInputMethodActive = true;
            if (this.perInputMethodState != null && (state = this.perInputMethodState.remove(this.inputMethod)) != null) {
                this.clientWindowNotificationEnabled = state;
            }
            if (this.clientWindowNotificationEnabled) {
                if (!this.addedClientWindowListeners()) {
                    this.addClientWindowListeners();
                }
                InputContext inputContext = this;
                synchronized (inputContext) {
                    if (this.clientWindowListened != null) {
                        this.notifyClientWindowChange(this.clientWindowListened);
                    }
                }
            } else if (this.addedClientWindowListeners()) {
                this.removeClientWindowListeners();
            }
        }
        InputMethodManager.getInstance().setInputContext(this);
        ((InputMethodContext)this).grabCompositionArea(updateCompositionArea);
    }

    static Window getComponentWindow(Component component) {
        while (component != null) {
            if (component instanceof Window) {
                return (Window)component;
            }
            component = component.getParent();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void focusLost(Component source, boolean isTemporary) {
        Object object = source.getTreeLock();
        synchronized (object) {
            InputContext inputContext = this;
            synchronized (inputContext) {
                InputMethodContext inputContext2;
                if (this.isInputMethodActive) {
                    this.deactivateInputMethod(isTemporary);
                }
                this.awtFocussedComponent = null;
                if (this.inputMethod instanceof InputMethodAdapter) {
                    ((InputMethodAdapter)this.inputMethod).setAWTFocussedComponent(null);
                }
                if ((inputContext2 = (InputMethodContext)this).isCompositionAreaVisible()) {
                    inputContext2.setCompositionAreaVisible(false);
                    this.compositionAreaHidden = true;
                }
            }
        }
    }

    private boolean checkInputMethodSelectionKey(KeyEvent event) {
        if (inputMethodSelectionKey != null) {
            AWTKeyStroke aKeyStroke = AWTKeyStroke.getAWTKeyStrokeForEvent(event);
            return inputMethodSelectionKey.equals(aKeyStroke);
        }
        return false;
    }

    private void deactivateInputMethod(boolean isTemporary) {
        InputMethodManager.getInstance().setInputContext(null);
        if (this.inputMethod != null) {
            this.isInputMethodActive = false;
            this.inputMethod.deactivate(isTemporary);
            previousInputMethod = this.inputMethod;
        }
    }

    synchronized void changeInputMethod(InputMethodLocator newLocator) {
        if (this.inputMethodLocator == null) {
            this.inputMethodLocator = newLocator;
            this.inputMethodCreationFailed = false;
            return;
        }
        if (this.inputMethodLocator.sameInputMethod(newLocator)) {
            Locale newLocale = newLocator.getLocale();
            if (newLocale != null && this.inputMethodLocator.getLocale() != newLocale) {
                if (this.inputMethod != null) {
                    this.inputMethod.setLocale(newLocale);
                }
                this.inputMethodLocator = newLocator;
            }
            return;
        }
        Locale savedLocale = this.inputMethodLocator.getLocale();
        boolean wasInputMethodActive = this.isInputMethodActive;
        boolean wasCompositionEnabledSupported = false;
        boolean wasCompositionEnabled = false;
        if (this.inputMethod != null) {
            try {
                wasCompositionEnabled = this.inputMethod.isCompositionEnabled();
                wasCompositionEnabledSupported = true;
            }
            catch (UnsupportedOperationException unsupportedOperationException) {
                // empty catch block
            }
            if (this.currentClientComponent != null) {
                if (!this.isInputMethodActive) {
                    this.activateInputMethod(false);
                }
                this.endComposition();
                this.deactivateInputMethod(false);
                if (this.inputMethod instanceof InputMethodAdapter) {
                    ((InputMethodAdapter)this.inputMethod).setClientComponent(null);
                }
                if (null == this.currentClientComponent.getInputMethodRequests()) {
                    wasCompositionEnabledSupported = false;
                }
            }
            savedLocale = this.inputMethod.getLocale();
            if (this.usedInputMethods == null) {
                this.usedInputMethods = new HashMap(5);
            }
            if (this.perInputMethodState == null) {
                this.perInputMethodState = new HashMap(5);
            }
            this.usedInputMethods.put(this.inputMethodLocator.deriveLocator(null), this.inputMethod);
            this.perInputMethodState.put(this.inputMethod, this.clientWindowNotificationEnabled);
            this.enableClientWindowNotification(this.inputMethod, false);
            if (this == inputMethodWindowContext) {
                this.inputMethod.hideWindows();
                this.inputMethod.removeNotify();
                inputMethodWindowContext = null;
            }
            this.inputMethodLocator = null;
            this.inputMethod = null;
            this.inputMethodCreationFailed = false;
        }
        if (newLocator.getLocale() == null && savedLocale != null && newLocator.isLocaleAvailable(savedLocale)) {
            newLocator = newLocator.deriveLocator(savedLocale);
        }
        this.inputMethodLocator = newLocator;
        this.inputMethodCreationFailed = false;
        if (wasInputMethodActive) {
            this.inputMethod = this.getInputMethodInstance();
            if (this.inputMethod instanceof InputMethodAdapter) {
                ((InputMethodAdapter)this.inputMethod).setAWTFocussedComponent(this.awtFocussedComponent);
            }
            this.activateInputMethod(true);
        }
        if (wasCompositionEnabledSupported) {
            this.inputMethod = this.getInputMethod();
            if (this.inputMethod != null) {
                try {
                    this.inputMethod.setCompositionEnabled(wasCompositionEnabled);
                }
                catch (UnsupportedOperationException unsupportedOperationException) {
                    // empty catch block
                }
            }
        }
    }

    Component getClientComponent() {
        return this.currentClientComponent;
    }

    @Override
    public synchronized void removeNotify(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (this.inputMethod == null) {
            if (component == this.currentClientComponent) {
                this.currentClientComponent = null;
            }
            return;
        }
        if (component == this.awtFocussedComponent) {
            this.focusLost(component, false);
        }
        if (component == this.currentClientComponent) {
            if (this.isInputMethodActive) {
                this.deactivateInputMethod(false);
            }
            this.inputMethod.removeNotify();
            if (this.clientWindowNotificationEnabled && this.addedClientWindowListeners()) {
                this.removeClientWindowListeners();
            }
            this.currentClientComponent = null;
            if (this.inputMethod instanceof InputMethodAdapter) {
                ((InputMethodAdapter)this.inputMethod).setClientComponent(null);
            }
            if (EventQueue.isDispatchThread()) {
                ((InputMethodContext)this).releaseCompositionArea();
            } else {
                EventQueue.invokeLater(new Runnable(){

                    @Override
                    public void run() {
                        ((InputMethodContext)InputContext.this).releaseCompositionArea();
                    }
                });
            }
        }
    }

    @Override
    public synchronized void dispose() {
        if (this.currentClientComponent != null) {
            throw new IllegalStateException("Can't dispose InputContext while it's active");
        }
        if (this.inputMethod != null) {
            if (this == inputMethodWindowContext) {
                this.inputMethod.hideWindows();
                inputMethodWindowContext = null;
            }
            if (this.inputMethod == previousInputMethod) {
                previousInputMethod = null;
            }
            if (this.clientWindowNotificationEnabled) {
                if (this.addedClientWindowListeners()) {
                    this.removeClientWindowListeners();
                }
                this.clientWindowNotificationEnabled = false;
            }
            this.inputMethod.dispose();
            if (this.clientWindowNotificationEnabled) {
                this.enableClientWindowNotification(this.inputMethod, false);
            }
            this.inputMethod = null;
        }
        this.inputMethodLocator = null;
        if (this.usedInputMethods != null && !this.usedInputMethods.isEmpty()) {
            Collection<InputMethod> methods = this.usedInputMethods.values();
            this.usedInputMethods = null;
            for (InputMethod method : methods) {
                method.dispose();
            }
        }
        this.clientWindowNotificationEnabled = false;
        this.clientWindowListened = null;
        this.perInputMethodState = null;
    }

    @Override
    public synchronized Object getInputMethodControlObject() {
        InputMethod inputMethod = this.getInputMethod();
        if (inputMethod != null) {
            return inputMethod.getControlObject();
        }
        return null;
    }

    @Override
    public void setCompositionEnabled(boolean enable) {
        InputMethod inputMethod = this.getInputMethod();
        if (inputMethod == null) {
            throw new UnsupportedOperationException();
        }
        inputMethod.setCompositionEnabled(enable);
    }

    @Override
    public boolean isCompositionEnabled() {
        InputMethod inputMethod = this.getInputMethod();
        if (inputMethod == null) {
            throw new UnsupportedOperationException();
        }
        return inputMethod.isCompositionEnabled();
    }

    public String getInputMethodInfo() {
        InputMethod inputMethod = this.getInputMethod();
        if (inputMethod == null) {
            throw new UnsupportedOperationException("Null input method");
        }
        String inputMethodInfo = null;
        if (inputMethod instanceof InputMethodAdapter) {
            inputMethodInfo = ((InputMethodAdapter)inputMethod).getNativeInputMethodInfo();
        }
        if (inputMethodInfo == null && this.inputMethodLocator != null) {
            inputMethodInfo = this.inputMethodLocator.getDescriptor().getInputMethodDisplayName(this.getLocale(), SunToolkit.getStartupLocale());
        }
        if (inputMethodInfo != null && !inputMethodInfo.isEmpty()) {
            return inputMethodInfo;
        }
        return inputMethod.toString() + "-" + inputMethod.getLocale().toString();
    }

    public void disableNativeIM() {
        InputMethod inputMethod = this.getInputMethod();
        if (inputMethod instanceof InputMethodAdapter) {
            InputMethodAdapter adapter = (InputMethodAdapter)inputMethod;
            adapter.stopListening();
        }
    }

    private synchronized InputMethod getInputMethod() {
        if (this.inputMethod != null) {
            return this.inputMethod;
        }
        if (this.inputMethodCreationFailed) {
            return null;
        }
        this.inputMethod = this.getInputMethodInstance();
        return this.inputMethod;
    }

    private InputMethod getInputMethodInstance() {
        InputMethodLocator locator = this.inputMethodLocator;
        if (locator == null) {
            this.inputMethodCreationFailed = true;
            return null;
        }
        Locale locale = locator.getLocale();
        InputMethod inputMethodInstance = null;
        if (this.usedInputMethods != null && (inputMethodInstance = this.usedInputMethods.remove(locator.deriveLocator(null))) != null) {
            if (locale != null) {
                inputMethodInstance.setLocale(locale);
            }
            inputMethodInstance.setCharacterSubsets(this.characterSubsets);
            Boolean state = this.perInputMethodState.remove(inputMethodInstance);
            if (state != null) {
                this.enableClientWindowNotification(inputMethodInstance, state);
            }
            ((InputMethodContext)this).setInputMethodSupportsBelowTheSpot(!(inputMethodInstance instanceof InputMethodAdapter) || ((InputMethodAdapter)inputMethodInstance).supportsBelowTheSpot());
            return inputMethodInstance;
        }
        try {
            inputMethodInstance = locator.getDescriptor().createInputMethod();
            if (locale != null) {
                inputMethodInstance.setLocale(locale);
            }
            inputMethodInstance.setInputMethodContext((InputMethodContext)this);
            inputMethodInstance.setCharacterSubsets(this.characterSubsets);
        }
        catch (Exception e) {
            this.logCreationFailed(e);
            this.inputMethodCreationFailed = true;
            if (inputMethodInstance != null) {
                inputMethodInstance = null;
            }
        }
        catch (LinkageError e) {
            this.logCreationFailed(e);
            this.inputMethodCreationFailed = true;
        }
        ((InputMethodContext)this).setInputMethodSupportsBelowTheSpot(!(inputMethodInstance instanceof InputMethodAdapter) || ((InputMethodAdapter)inputMethodInstance).supportsBelowTheSpot());
        return inputMethodInstance;
    }

    private void logCreationFailed(Throwable throwable) {
        PlatformLogger logger = PlatformLogger.getLogger("sun.awt.im");
        if (logger.isLoggable(PlatformLogger.Level.CONFIG)) {
            String errorTextFormat = Toolkit.getProperty("AWT.InputMethodCreationFailed", "Could not create {0}. Reason: {1}");
            Object[] args = new Object[]{this.inputMethodLocator.getDescriptor().getInputMethodDisplayName(null, Locale.getDefault()), throwable.getLocalizedMessage()};
            MessageFormat mf = new MessageFormat(errorTextFormat);
            logger.config(mf.format(args));
        }
    }

    InputMethodLocator getInputMethodLocator() {
        if (this.inputMethod != null) {
            return this.inputMethodLocator.deriveLocator(this.inputMethod.getLocale());
        }
        return this.inputMethodLocator;
    }

    @Override
    public synchronized void endComposition() {
        if (this.inputMethod != null) {
            this.inputMethod.endComposition();
        }
    }

    synchronized void enableClientWindowNotification(InputMethod requester, boolean enable) {
        if (requester != this.inputMethod) {
            if (this.perInputMethodState == null) {
                this.perInputMethodState = new HashMap(5);
            }
            this.perInputMethodState.put(requester, enable);
            return;
        }
        if (this.clientWindowNotificationEnabled != enable) {
            this.clientWindowLocation = null;
            this.clientWindowNotificationEnabled = enable;
        }
        if (this.clientWindowNotificationEnabled) {
            if (!this.addedClientWindowListeners()) {
                this.addClientWindowListeners();
            }
            if (this.clientWindowListened != null) {
                this.clientWindowLocation = null;
                this.notifyClientWindowChange(this.clientWindowListened);
            }
        } else if (this.addedClientWindowListeners()) {
            this.removeClientWindowListeners();
        }
    }

    private synchronized void notifyClientWindowChange(Window window) {
        if (this.inputMethod == null) {
            return;
        }
        if (!window.isVisible() || window instanceof Frame && ((Frame)window).getState() == 1) {
            this.clientWindowLocation = null;
            this.inputMethod.notifyClientWindowChange(null);
            return;
        }
        Rectangle location = window.getBounds();
        if (this.clientWindowLocation == null || !this.clientWindowLocation.equals(location)) {
            this.clientWindowLocation = location;
            this.inputMethod.notifyClientWindowChange(this.clientWindowLocation);
        }
    }

    private synchronized void addClientWindowListeners() {
        Component client = this.getClientComponent();
        if (client == null) {
            return;
        }
        Window window = InputContext.getComponentWindow(client);
        if (window == null) {
            return;
        }
        window.addComponentListener(this);
        window.addWindowListener(this);
        this.clientWindowListened = window;
    }

    private synchronized void removeClientWindowListeners() {
        this.clientWindowListened.removeComponentListener(this);
        this.clientWindowListened.removeWindowListener(this);
        this.clientWindowListened = null;
    }

    private boolean addedClientWindowListeners() {
        return this.clientWindowListened != null;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.notifyClientWindowChange((Window)e.getComponent());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        this.notifyClientWindowChange((Window)e.getComponent());
    }

    @Override
    public void componentShown(ComponentEvent e) {
        this.notifyClientWindowChange((Window)e.getComponent());
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        this.notifyClientWindowChange((Window)e.getComponent());
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
        this.notifyClientWindowChange(e.getWindow());
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        this.notifyClientWindowChange(e.getWindow());
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private void initializeInputMethodSelectionKey() {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                Preferences root = Preferences.userRoot();
                inputMethodSelectionKey = InputContext.this.getInputMethodSelectionKeyStroke(root);
                if (inputMethodSelectionKey == null) {
                    root = Preferences.systemRoot();
                    inputMethodSelectionKey = InputContext.this.getInputMethodSelectionKeyStroke(root);
                }
                return null;
            }
        });
    }

    private AWTKeyStroke getInputMethodSelectionKeyStroke(Preferences root) {
        try {
            Preferences node;
            int keyCode;
            if (root.nodeExists(inputMethodSelectionKeyPath) && (keyCode = (node = root.node(inputMethodSelectionKeyPath)).getInt(inputMethodSelectionKeyCodeName, 0)) != 0) {
                int modifiers = node.getInt(inputMethodSelectionKeyModifiersName, 0);
                return AWTKeyStroke.getAWTKeyStroke(keyCode, modifiers);
            }
        }
        catch (BackingStoreException backingStoreException) {
            // empty catch block
        }
        return null;
    }

    static {
        previousInputMethod = null;
        inputMethodSelectionKeyInitialized = false;
    }
}

