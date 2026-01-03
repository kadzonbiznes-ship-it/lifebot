/*
 * Decompiled with CFR 0.152.
 */
package java.util.prefs;

import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Base64;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.prefs.XmlSupport;

public abstract class AbstractPreferences
extends Preferences {
    static final int CODE_POINT_U0000 = 0;
    private final String name;
    private final String absolutePath;
    final AbstractPreferences parent;
    private final AbstractPreferences root;
    protected boolean newNode = false;
    private Map<String, AbstractPreferences> kidCache = new HashMap<String, AbstractPreferences>();
    private boolean removed = false;
    private PreferenceChangeListener[] prefListeners = new PreferenceChangeListener[0];
    private NodeChangeListener[] nodeListeners = new NodeChangeListener[0];
    protected final Object lock = new Object();
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final AbstractPreferences[] EMPTY_ABSTRACT_PREFS_ARRAY = new AbstractPreferences[0];
    private static final List<EventObject> eventQueue = new LinkedList<EventObject>();
    private static Thread eventDispatchThread = null;

    protected AbstractPreferences(AbstractPreferences parent, String name) {
        if (parent == null) {
            if (!name.isEmpty()) {
                throw new IllegalArgumentException("Root name '" + name + "' must be \"\"");
            }
            this.absolutePath = "/";
            this.root = this;
        } else {
            if (name.indexOf(47) != -1) {
                throw new IllegalArgumentException("Name '" + name + "' contains '/'");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Illegal name: empty string");
            }
            this.root = parent.root;
            this.absolutePath = parent == this.root ? "/" + name : parent.absolutePath() + "/" + name;
        }
        this.name = name;
        this.parent = parent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void put(String key, String value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        if (key.length() > 80) {
            throw new IllegalArgumentException("Key too long: " + key);
        }
        if (value.length() > 8192) {
            throw new IllegalArgumentException("Value too long: " + value);
        }
        if (key.indexOf(0) != -1) {
            throw new IllegalArgumentException("Key contains code point U+0000");
        }
        if (value.indexOf(0) != -1) {
            throw new IllegalArgumentException("Value contains code point U+0000");
        }
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            this.putSpi(key, value);
            this.enqueuePreferenceChangeEvent(key, value);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String get(String key, String def) {
        if (key == null) {
            throw new NullPointerException("Null key");
        }
        if (key.indexOf(0) != -1) {
            throw new IllegalArgumentException("Key contains code point U+0000");
        }
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            String result = null;
            try {
                result = this.getSpi(key);
            }
            catch (Exception exception) {
                // empty catch block
            }
            return result == null ? def : result;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, "Specified key cannot be null");
        if (key.indexOf(0) != -1) {
            throw new IllegalArgumentException("Key contains code point U+0000");
        }
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            this.removeSpi(key);
            this.enqueuePreferenceChangeEvent(key, null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clear() throws BackingStoreException {
        Object object = this.lock;
        synchronized (object) {
            for (String key : this.keys()) {
                this.remove(key);
            }
        }
    }

    @Override
    public void putInt(String key, int value) {
        this.put(key, Integer.toString(value));
    }

    @Override
    public int getInt(String key, int def) {
        int result = def;
        try {
            String value = this.get(key, null);
            if (value != null) {
                result = Integer.parseInt(value);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return result;
    }

    @Override
    public void putLong(String key, long value) {
        this.put(key, Long.toString(value));
    }

    @Override
    public long getLong(String key, long def) {
        long result = def;
        try {
            String value = this.get(key, null);
            if (value != null) {
                result = Long.parseLong(value);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return result;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        this.put(key, String.valueOf(value));
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        boolean result = def;
        String value = this.get(key, null);
        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                result = true;
            } else if (value.equalsIgnoreCase("false")) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public void putFloat(String key, float value) {
        this.put(key, Float.toString(value));
    }

    @Override
    public float getFloat(String key, float def) {
        float result = def;
        try {
            String value = this.get(key, null);
            if (value != null) {
                result = Float.parseFloat(value);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return result;
    }

    @Override
    public void putDouble(String key, double value) {
        this.put(key, Double.toString(value));
    }

    @Override
    public double getDouble(String key, double def) {
        double result = def;
        try {
            String value = this.get(key, null);
            if (value != null) {
                result = Double.parseDouble(value);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return result;
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        this.put(key, Base64.byteArrayToBase64(value));
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        byte[] result = def;
        String value = this.get(key, null);
        try {
            if (value != null) {
                result = Base64.base64ToByteArray(value);
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String[] keys() throws BackingStoreException {
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            return this.keysSpi();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String[] childrenNames() throws BackingStoreException {
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            TreeSet<String> s = new TreeSet<String>(this.kidCache.keySet());
            for (String kid : this.childrenNamesSpi()) {
                s.add(kid);
            }
            return s.toArray(EMPTY_STRING_ARRAY);
        }
    }

    protected final AbstractPreferences[] cachedChildren() {
        return this.kidCache.values().toArray(EMPTY_ABSTRACT_PREFS_ARRAY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Preferences parent() {
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            return this.parent;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Preferences node(String path) {
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            if (path.isEmpty()) {
                return this;
            }
            if (path.equals("/")) {
                return this.root;
            }
            if (path.charAt(0) != '/') {
                return this.node(new StringTokenizer(path, "/", true));
            }
        }
        return this.root.node(new StringTokenizer(path.substring(1), "/", true));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Preferences node(StringTokenizer path) {
        String token = path.nextToken();
        if (token.equals("/")) {
            throw new IllegalArgumentException("Consecutive slashes in path");
        }
        Object object = this.lock;
        synchronized (object) {
            AbstractPreferences child = this.kidCache.get(token);
            if (child == null) {
                if (token.length() > 80) {
                    throw new IllegalArgumentException("Node name " + token + " too long");
                }
                child = this.childSpi(token);
                if (child.newNode) {
                    this.enqueueNodeAddedEvent(child);
                }
                this.kidCache.put(token, child);
            }
            if (!path.hasMoreTokens()) {
                return child;
            }
            path.nextToken();
            if (!path.hasMoreTokens()) {
                throw new IllegalArgumentException("Path ends with slash");
            }
            return child.node(path);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean nodeExists(String path) throws BackingStoreException {
        Object object = this.lock;
        synchronized (object) {
            if (path.isEmpty()) {
                return !this.removed;
            }
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            if (path.equals("/")) {
                return true;
            }
            if (path.charAt(0) != '/') {
                return this.nodeExists(new StringTokenizer(path, "/", true));
            }
        }
        return this.root.nodeExists(new StringTokenizer(path.substring(1), "/", true));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean nodeExists(StringTokenizer path) throws BackingStoreException {
        String token = path.nextToken();
        if (token.equals("/")) {
            throw new IllegalArgumentException("Consecutive slashes in path");
        }
        Object object = this.lock;
        synchronized (object) {
            AbstractPreferences child = this.kidCache.get(token);
            if (child == null) {
                child = this.getChild(token);
            }
            if (child == null) {
                return false;
            }
            if (!path.hasMoreTokens()) {
                return true;
            }
            path.nextToken();
            if (!path.hasMoreTokens()) {
                throw new IllegalArgumentException("Path ends with slash");
            }
            return child.nodeExists(path);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeNode() throws BackingStoreException {
        if (this == this.root) {
            throw new UnsupportedOperationException("Can't remove the root!");
        }
        Object object = this.parent.lock;
        synchronized (object) {
            this.removeNode2();
            this.parent.kidCache.remove(this.name);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeNode2() throws BackingStoreException {
        Object object = this.lock;
        synchronized (object) {
            String[] kidNames;
            if (this.removed) {
                throw new IllegalStateException("Node already removed.");
            }
            for (String kidName : kidNames = this.childrenNamesSpi()) {
                if (this.kidCache.containsKey(kidName)) continue;
                this.kidCache.put(kidName, this.childSpi(kidName));
            }
            Iterator<AbstractPreferences> i = this.kidCache.values().iterator();
            while (i.hasNext()) {
                try {
                    i.next().removeNode2();
                    i.remove();
                }
                catch (BackingStoreException backingStoreException) {}
            }
            this.removeNodeSpi();
            this.removed = true;
            this.parent.enqueueNodeRemovedEvent(this);
        }
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String absolutePath() {
        return this.absolutePath;
    }

    @Override
    public boolean isUserNode() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                return AbstractPreferences.this.root == Preferences.userRoot();
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        if (pcl == null) {
            throw new NullPointerException("Change listener is null.");
        }
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            PreferenceChangeListener[] old = this.prefListeners;
            this.prefListeners = new PreferenceChangeListener[old.length + 1];
            System.arraycopy(old, 0, this.prefListeners, 0, old.length);
            this.prefListeners[old.length] = pcl;
        }
        AbstractPreferences.startEventDispatchThreadIfNecessary();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            if (this.prefListeners == null || this.prefListeners.length == 0) {
                throw new IllegalArgumentException("Listener not registered.");
            }
            PreferenceChangeListener[] newPl = new PreferenceChangeListener[this.prefListeners.length - 1];
            int i = 0;
            while (i < newPl.length && this.prefListeners[i] != pcl) {
                newPl[i] = this.prefListeners[i++];
            }
            if (i == newPl.length && this.prefListeners[i] != pcl) {
                throw new IllegalArgumentException("Listener not registered.");
            }
            while (i < newPl.length) {
                newPl[i++] = this.prefListeners[i];
            }
            this.prefListeners = newPl;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        if (ncl == null) {
            throw new NullPointerException("Change listener is null.");
        }
        Object object = this.lock;
        synchronized (object) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            if (this.nodeListeners == null) {
                this.nodeListeners = new NodeChangeListener[1];
                this.nodeListeners[0] = ncl;
            } else {
                NodeChangeListener[] old = this.nodeListeners;
                this.nodeListeners = new NodeChangeListener[old.length + 1];
                System.arraycopy(old, 0, this.nodeListeners, 0, old.length);
                this.nodeListeners[old.length] = ncl;
            }
        }
        AbstractPreferences.startEventDispatchThreadIfNecessary();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        Object object = this.lock;
        synchronized (object) {
            int i;
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            if (this.nodeListeners == null || this.nodeListeners.length == 0) {
                throw new IllegalArgumentException("Listener not registered.");
            }
            for (i = 0; i < this.nodeListeners.length && this.nodeListeners[i] != ncl; ++i) {
            }
            if (i == this.nodeListeners.length) {
                throw new IllegalArgumentException("Listener not registered.");
            }
            NodeChangeListener[] newNl = new NodeChangeListener[this.nodeListeners.length - 1];
            if (i != 0) {
                System.arraycopy(this.nodeListeners, 0, newNl, 0, i);
            }
            if (i != newNl.length) {
                System.arraycopy(this.nodeListeners, i + 1, newNl, i, newNl.length - i);
            }
            this.nodeListeners = newNl;
        }
    }

    protected abstract void putSpi(String var1, String var2);

    protected abstract String getSpi(String var1);

    protected abstract void removeSpi(String var1);

    protected abstract void removeNodeSpi() throws BackingStoreException;

    protected abstract String[] keysSpi() throws BackingStoreException;

    protected abstract String[] childrenNamesSpi() throws BackingStoreException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected AbstractPreferences getChild(String nodeName) throws BackingStoreException {
        Object object = this.lock;
        synchronized (object) {
            String[] kidNames;
            for (String kidName : kidNames = this.childrenNames()) {
                if (!kidName.equals(nodeName)) continue;
                return this.childSpi(kidName);
            }
        }
        return null;
    }

    protected abstract AbstractPreferences childSpi(String var1);

    @Override
    public String toString() {
        return (this.isUserNode() ? "User" : "System") + " Preference Node: " + this.absolutePath();
    }

    @Override
    public void sync() throws BackingStoreException {
        this.sync2();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sync2() throws BackingStoreException {
        AbstractPreferences[] abstractPreferencesArray = this.lock;
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed");
            }
            this.syncSpi();
            AbstractPreferences[] cachedKids = this.cachedChildren();
            // ** MonitorExit[var2_1] (shouldn't be in output)
            for (AbstractPreferences cachedKid : cachedKids) {
                cachedKid.sync2();
            }
            return;
        }
    }

    protected abstract void syncSpi() throws BackingStoreException;

    @Override
    public void flush() throws BackingStoreException {
        this.flush2();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void flush2() throws BackingStoreException {
        AbstractPreferences[] abstractPreferencesArray = this.lock;
        synchronized (this.lock) {
            this.flushSpi();
            if (this.removed) {
                // ** MonitorExit[var2_1] (shouldn't be in output)
                return;
            }
            AbstractPreferences[] cachedKids = this.cachedChildren();
            // ** MonitorExit[var2_1] (shouldn't be in output)
            for (AbstractPreferences cachedKid : cachedKids) {
                cachedKid.flush2();
            }
            return;
        }
    }

    protected abstract void flushSpi() throws BackingStoreException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean isRemoved() {
        Object object = this.lock;
        synchronized (object) {
            return this.removed;
        }
    }

    private static synchronized void startEventDispatchThreadIfNecessary() {
        if (eventDispatchThread == null) {
            eventDispatchThread = new EventDispatchThread();
            eventDispatchThread.setDaemon(true);
            eventDispatchThread.start();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    PreferenceChangeListener[] prefListeners() {
        Object object = this.lock;
        synchronized (object) {
            return this.prefListeners;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    NodeChangeListener[] nodeListeners() {
        Object object = this.lock;
        synchronized (object) {
            return this.nodeListeners;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void enqueuePreferenceChangeEvent(String key, String newValue) {
        if (this.prefListeners.length != 0) {
            List<EventObject> list = eventQueue;
            synchronized (list) {
                eventQueue.add(new PreferenceChangeEvent(this, key, newValue));
                eventQueue.notify();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void enqueueNodeAddedEvent(Preferences child) {
        if (this.nodeListeners.length != 0) {
            List<EventObject> list = eventQueue;
            synchronized (list) {
                eventQueue.add(new NodeAddedEvent(this, child));
                eventQueue.notify();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void enqueueNodeRemovedEvent(Preferences child) {
        if (this.nodeListeners.length != 0) {
            List<EventObject> list = eventQueue;
            synchronized (list) {
                eventQueue.add(new NodeRemovedEvent(this, child));
                eventQueue.notify();
            }
        }
    }

    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        XmlSupport.export(os, this, false);
    }

    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        XmlSupport.export(os, this, true);
    }

    private static class EventDispatchThread
    extends Thread {
        private EventDispatchThread() {
            super(null, null, "Event Dispatch Thread", 0L, false);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Unable to fully structure code
         */
        @Override
        public void run() {
            block5: while (true) lbl-1000:
            // 4 sources

            {
                block11: {
                    block10: {
                        event = null;
                        var2_2 = AbstractPreferences.eventQueue;
                        synchronized (var2_2) {
                            try {
                                while (AbstractPreferences.eventQueue.isEmpty()) {
                                    AbstractPreferences.eventQueue.wait();
                                }
                                event = AbstractPreferences.eventQueue.remove(0);
                            }
                            catch (InterruptedException e) {
                                return;
                            }
                        }
                        src = (AbstractPreferences)event.getSource();
                        if (!(event instanceof PreferenceChangeEvent)) break block10;
                        pce = (PreferenceChangeEvent)event;
                        var5_7 = listeners = src.prefListeners();
                        var6_8 = var5_7.length;
                        var7_9 = 0;
                        while (true) {
                            if (var7_9 >= var6_8) ** GOTO lbl-1000
                            listener = var5_7[var7_9];
                            listener.preferenceChange(pce);
                            ++var7_9;
                        }
                    }
                    nce = (NodeChangeEvent)event;
                    listeners = src.nodeListeners();
                    if (!(nce instanceof NodeAddedEvent)) break block11;
                    var5_7 = listeners;
                    var6_8 = var5_7.length;
                    var7_9 = 0;
                    while (true) {
                        if (var7_9 >= var6_8) ** GOTO lbl-1000
                        listener = var5_7[var7_9];
                        listener.childAdded(nce);
                        ++var7_9;
                    }
                }
                var5_7 = listeners;
                var6_8 = var5_7.length;
                var7_9 = 0;
                while (true) {
                    if (var7_9 >= var6_8) continue block5;
                    listener = var5_7[var7_9];
                    listener.childRemoved(nce);
                    ++var7_9;
                }
                break;
            }
        }
    }

    private static class NodeAddedEvent
    extends NodeChangeEvent {
        private static final long serialVersionUID = -6743557530157328528L;

        NodeAddedEvent(Preferences parent, Preferences child) {
            super(parent, child);
        }
    }

    private static class NodeRemovedEvent
    extends NodeChangeEvent {
        private static final long serialVersionUID = 8735497392918824837L;

        NodeRemovedEvent(Preferences parent, Preferences child) {
            super(parent, child);
        }
    }
}

