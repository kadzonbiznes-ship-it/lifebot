/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;

public class UIDefaults
extends Hashtable<Object, Object> {
    private static final Object PENDING = new Object();
    private SwingPropertyChangeSupport changeSupport;
    private Vector<String> resourceBundles;
    private Locale defaultLocale = Locale.getDefault();
    private Map<Locale, Map<String, Object>> resourceCache;

    public UIDefaults() {
        this(700, 0.75f);
    }

    public UIDefaults(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.resourceCache = new HashMap<Locale, Map<String, Object>>();
    }

    public UIDefaults(Object[] keyValueList) {
        super(keyValueList.length / 2);
        for (int i = 0; i < keyValueList.length; i += 2) {
            super.put(keyValueList[i], keyValueList[i + 1]);
        }
    }

    @Override
    public Object get(Object key) {
        Object value = this.getFromHashtable(key);
        return value != null ? value : this.getFromResourceBundle(key, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Object getFromHashtable(Object key) {
        Object value = super.get(key);
        if (value != PENDING && !(value instanceof ActiveValue) && !(value instanceof LazyValue)) {
            return value;
        }
        UIDefaults uIDefaults = this;
        synchronized (uIDefaults) {
            value = super.get(key);
            if (value == PENDING) {
                do {
                    try {
                        this.wait();
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                } while ((value = super.get(key)) == PENDING);
                return value;
            }
            if (value instanceof LazyValue) {
                super.put(key, PENDING);
            } else if (!(value instanceof ActiveValue)) {
                return value;
            }
        }
        if (value instanceof LazyValue) {
            try {
                value = ((LazyValue)value).createValue(this);
            }
            finally {
                uIDefaults = this;
                synchronized (uIDefaults) {
                    if (value == null) {
                        super.remove(key);
                    } else {
                        super.put(key, value);
                    }
                    this.notifyAll();
                }
            }
        }
        value = ((ActiveValue)value).createValue(this);
        return value;
    }

    public Object get(Object key, Locale l) {
        Object value = this.getFromHashtable(key);
        return value != null ? value : this.getFromResourceBundle(key, l);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Object getFromResourceBundle(Object key, Locale l) {
        if (this.resourceBundles == null || this.resourceBundles.isEmpty() || !(key instanceof String)) {
            return null;
        }
        if (l == null) {
            if (this.defaultLocale == null) {
                return null;
            }
            l = this.defaultLocale;
        }
        UIDefaults uIDefaults = this;
        synchronized (uIDefaults) {
            return this.getResourceCache(l).get(key);
        }
    }

    private Map<String, Object> getResourceCache(Locale l) {
        TextAndMnemonicHashMap values = this.resourceCache.get(l);
        if (values == null) {
            values = new TextAndMnemonicHashMap();
            for (int i = this.resourceBundles.size() - 1; i >= 0; --i) {
                String bundleName = this.resourceBundles.get(i);
                try {
                    ResourceBundle b = UIDefaults.isDesktopResourceBundle(bundleName) ? ResourceBundle.getBundle(bundleName, l, UIDefaults.class.getModule()) : ResourceBundle.getBundle(bundleName, l, ClassLoader.getSystemClassLoader());
                    Enumeration<String> keys = b.getKeys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement();
                        if (values.get(key) != null) continue;
                        Object value = b.getObject(key);
                        values.put(key, value);
                    }
                    continue;
                }
                catch (MissingResourceException missingResourceException) {
                    // empty catch block
                }
            }
            this.resourceCache.put(l, values);
        }
        return values;
    }

    private static boolean isDesktopResourceBundle(final String baseName) {
        final Module thisModule = UIDefaults.class.getModule();
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                Boolean bl;
                block9: {
                    Class<?> c = Class.forName(thisModule, baseName);
                    if (c != null) {
                        return true;
                    }
                    String resourceName = baseName.replace('.', '/') + ".properties";
                    InputStream in = thisModule.getResourceAsStream(resourceName);
                    try {
                        bl = in != null;
                        if (in == null) break block9;
                    }
                    catch (Throwable throwable) {
                        try {
                            if (in != null) {
                                try {
                                    in.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                    in.close();
                }
                return bl;
            }
        });
    }

    @Override
    public Object put(Object key, Object value) {
        Object oldValue;
        Object object = oldValue = value == null ? super.remove(key) : super.put(key, value);
        if (key instanceof String) {
            this.firePropertyChange((String)key, oldValue, value);
        }
        return oldValue;
    }

    public void putDefaults(Object[] keyValueList) {
        int max = keyValueList.length;
        for (int i = 0; i < max; i += 2) {
            Object value = keyValueList[i + 1];
            if (value == null) {
                super.remove(keyValueList[i]);
                continue;
            }
            super.put(keyValueList[i], value);
        }
        this.firePropertyChange("UIDefaults", null, null);
    }

    public Font getFont(Object key) {
        Object value = this.get(key);
        return value instanceof Font ? (Font)value : null;
    }

    public Font getFont(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Font ? (Font)value : null;
    }

    public Color getColor(Object key) {
        Object value = this.get(key);
        return value instanceof Color ? (Color)value : null;
    }

    public Color getColor(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Color ? (Color)value : null;
    }

    public Icon getIcon(Object key) {
        Object value = this.get(key);
        return value instanceof Icon ? (Icon)value : null;
    }

    public Icon getIcon(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Icon ? (Icon)value : null;
    }

    public Border getBorder(Object key) {
        Object value = this.get(key);
        return value instanceof Border ? (Border)value : null;
    }

    public Border getBorder(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Border ? (Border)value : null;
    }

    public String getString(Object key) {
        Object value = this.get(key);
        return value instanceof String ? (String)value : null;
    }

    public String getString(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof String ? (String)value : null;
    }

    public int getInt(Object key) {
        Object value = this.get(key);
        return value instanceof Integer ? (Integer)value : 0;
    }

    public int getInt(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Integer ? (Integer)value : 0;
    }

    public boolean getBoolean(Object key) {
        Object value = this.get(key);
        return value instanceof Boolean ? (Boolean)value : false;
    }

    public boolean getBoolean(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Boolean ? (Boolean)value : false;
    }

    public Insets getInsets(Object key) {
        Object value = this.get(key);
        return value instanceof Insets ? (Insets)value : null;
    }

    public Insets getInsets(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Insets ? (Insets)value : null;
    }

    public Dimension getDimension(Object key) {
        Object value = this.get(key);
        return value instanceof Dimension ? (Dimension)value : null;
    }

    public Dimension getDimension(Object key, Locale l) {
        Object value = this.get(key, l);
        return value instanceof Dimension ? (Dimension)value : null;
    }

    public Class<? extends ComponentUI> getUIClass(String uiClassID, ClassLoader uiClassLoader) {
        try {
            String className = (String)this.get(uiClassID);
            if (className != null) {
                ReflectUtil.checkPackageAccess(className);
                Class<?> cls = (Class<?>)this.get(className);
                if (cls == null && (cls = uiClassLoader == null ? SwingUtilities.loadSystemClass(className) : uiClassLoader.loadClass(className)) != null) {
                    this.put(className, cls);
                }
                Class<?> tmp = cls;
                return tmp;
            }
        }
        catch (ClassCastException | ClassNotFoundException e) {
            return null;
        }
        return null;
    }

    public Class<? extends ComponentUI> getUIClass(String uiClassID) {
        return this.getUIClass(uiClassID, null);
    }

    protected void getUIError(String msg) {
        try {
            throw new Error(msg);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return;
        }
    }

    public ComponentUI getUI(JComponent target) {
        Object cl = this.get("ClassLoader");
        ClassLoader uiClassLoader = cl != null ? (ClassLoader)cl : target.getClass().getClassLoader();
        Class<? extends ComponentUI> uiClass = this.getUIClass(target.getUIClassID(), uiClassLoader);
        Object uiObject = null;
        if (uiClass == null) {
            this.getUIError("no ComponentUI class for: " + String.valueOf(target));
        } else {
            try {
                Method m = (Method)this.get(uiClass);
                if (m == null) {
                    m = uiClass.getMethod("createUI", JComponent.class);
                    this.put(uiClass, m);
                }
                uiObject = uiClass.getModule() == ComponentUI.class.getModule() ? m.invoke(null, target) : MethodUtil.invoke(m, null, new Object[]{target});
            }
            catch (NoSuchMethodException e) {
                this.getUIError("static createUI() method not found in " + String.valueOf(uiClass));
            }
            catch (Exception e) {
                StringWriter w = new StringWriter();
                PrintWriter pw = new PrintWriter(w);
                e.printStackTrace(pw);
                pw.flush();
                this.getUIError("createUI() failed for " + String.valueOf(target) + "\n" + String.valueOf(w));
            }
        }
        return (ComponentUI)uiObject;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.changeSupport == null) {
            this.changeSupport = new SwingPropertyChangeSupport(this);
        }
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.changeSupport != null) {
            this.changeSupport.removePropertyChangeListener(listener);
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (this.changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return this.changeSupport.getPropertyChangeListeners();
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (this.changeSupport != null) {
            this.changeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    public synchronized void addResourceBundle(String bundleName) {
        if (bundleName == null) {
            return;
        }
        if (UIDefaults.isDesktopResourceBundle(bundleName)) {
            return;
        }
        this.addInternalBundle(bundleName);
    }

    private synchronized void addInternalBundle(String bundleName) {
        if (bundleName == null) {
            return;
        }
        if (this.resourceBundles == null) {
            this.resourceBundles = new Vector(5);
        }
        if (!this.resourceBundles.contains(bundleName)) {
            this.resourceBundles.add(bundleName);
            this.resourceCache.clear();
        }
    }

    public synchronized void removeResourceBundle(String bundleName) {
        if (this.resourceBundles != null) {
            this.resourceBundles.remove(bundleName);
        }
        this.resourceCache.clear();
    }

    public void setDefaultLocale(Locale l) {
        this.defaultLocale = l;
    }

    public Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    static {
        SwingAccessor.setUIDefaultsAccessor(UIDefaults::addInternalBundle);
    }

    public static interface ActiveValue {
        public Object createValue(UIDefaults var1);
    }

    public static interface LazyValue {
        public Object createValue(UIDefaults var1);
    }

    private static class TextAndMnemonicHashMap
    extends HashMap<String, Object> {
        static final String AND_MNEMONIC = "AndMnemonic";
        static final String TITLE_SUFFIX = ".titleAndMnemonic";
        static final String TEXT_SUFFIX = ".textAndMnemonic";

        private TextAndMnemonicHashMap() {
        }

        @Override
        public Object get(Object key) {
            Object value = super.get(key);
            if (value == null) {
                boolean checkTitle = false;
                String stringKey = key.toString();
                String compositeKey = null;
                if (stringKey.endsWith(AND_MNEMONIC)) {
                    return null;
                }
                if (stringKey.endsWith(".mnemonic")) {
                    compositeKey = this.composeKey(stringKey, 9, TEXT_SUFFIX);
                } else if (stringKey.endsWith("NameMnemonic")) {
                    compositeKey = this.composeKey(stringKey, 12, TEXT_SUFFIX);
                } else if (stringKey.endsWith("Mnemonic")) {
                    compositeKey = this.composeKey(stringKey, 8, TEXT_SUFFIX);
                    checkTitle = true;
                }
                if (compositeKey != null) {
                    value = super.get(compositeKey);
                    if (value == null && checkTitle) {
                        compositeKey = this.composeKey(stringKey, 8, TITLE_SUFFIX);
                        value = super.get(compositeKey);
                    }
                    return value == null ? null : this.getMnemonicFromProperty(value.toString());
                }
                if (stringKey.endsWith("NameText")) {
                    compositeKey = this.composeKey(stringKey, 8, TEXT_SUFFIX);
                } else if (stringKey.endsWith(".nameText")) {
                    compositeKey = this.composeKey(stringKey, 9, TEXT_SUFFIX);
                } else if (stringKey.endsWith("Text")) {
                    compositeKey = this.composeKey(stringKey, 4, TEXT_SUFFIX);
                } else if (stringKey.endsWith("Title")) {
                    compositeKey = this.composeKey(stringKey, 5, TITLE_SUFFIX);
                }
                if (compositeKey != null) {
                    value = super.get(compositeKey);
                    return value == null ? null : this.getTextFromProperty(value.toString());
                }
                if (stringKey.endsWith("DisplayedMnemonicIndex")) {
                    compositeKey = this.composeKey(stringKey, 22, TEXT_SUFFIX);
                    value = super.get(compositeKey);
                    if (value == null) {
                        compositeKey = this.composeKey(stringKey, 22, TITLE_SUFFIX);
                        value = super.get(compositeKey);
                    }
                    return value == null ? null : this.getIndexFromProperty(value.toString());
                }
            }
            return value;
        }

        String composeKey(String key, int reduce, String sufix) {
            return key.substring(0, key.length() - reduce) + sufix;
        }

        String getTextFromProperty(String text) {
            return text.replace("&", "");
        }

        String getMnemonicFromProperty(String text) {
            int index = text.indexOf(38);
            if (0 <= index && index < text.length() - 1) {
                char c = text.charAt(index + 1);
                return Integer.toString(Character.toUpperCase(c));
            }
            return null;
        }

        String getIndexFromProperty(String text) {
            int index = text.indexOf(38);
            return index == -1 ? null : Integer.toString(index);
        }
    }

    public static class LazyInputMap
    implements LazyValue {
        private Object[] bindings;

        public LazyInputMap(Object[] bindings) {
            this.bindings = bindings;
        }

        @Override
        public Object createValue(UIDefaults table) {
            if (this.bindings != null) {
                InputMap km = LookAndFeel.makeInputMap(this.bindings);
                return km;
            }
            return null;
        }
    }

    public static class ProxyLazyValue
    implements LazyValue {
        private AccessControlContext acc = AccessController.getContext();
        private String className;
        private String methodName;
        private Object[] args;

        public ProxyLazyValue(String c) {
            this(c, (String)null);
        }

        public ProxyLazyValue(String c, String m) {
            this(c, m, null);
        }

        public ProxyLazyValue(String c, Object[] o) {
            this(c, null, o);
        }

        public ProxyLazyValue(String c, String m, Object[] o) {
            this.className = c;
            this.methodName = m;
            if (o != null) {
                this.args = (Object[])o.clone();
            }
        }

        @Override
        public Object createValue(final UIDefaults table) {
            if (this.acc == null && System.getSecurityManager() != null) {
                throw new SecurityException("null AccessControlContext");
            }
            return AccessController.doPrivileged(new PrivilegedAction<Object>(){
                final /* synthetic */ ProxyLazyValue this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public Object run() {
                    try {
                        Object cl;
                        if (!(table != null && (cl = table.get("ClassLoader")) instanceof ClassLoader || (cl = Thread.currentThread().getContextClassLoader()) != null)) {
                            cl = ClassLoader.getSystemClassLoader();
                        }
                        ReflectUtil.checkPackageAccess(this.this$0.className);
                        Class<?> c = Class.forName(this.this$0.className, true, (ClassLoader)cl);
                        SwingUtilities2.checkAccess(c.getModifiers());
                        if (this.this$0.methodName != null) {
                            Class<?>[] types = this.this$0.getClassArray(this.this$0.args);
                            Method m = c.getMethod(this.this$0.methodName, types);
                            return MethodUtil.invoke(m, c, this.this$0.args);
                        }
                        Class<?>[] types = this.this$0.getClassArray(this.this$0.args);
                        Constructor<?> constructor = c.getConstructor(types);
                        SwingUtilities2.checkAccess(constructor.getModifiers());
                        return constructor.newInstance(this.this$0.args);
                    }
                    catch (Exception exception) {
                        return null;
                    }
                }
            }, this.acc);
        }

        private Class<?>[] getClassArray(Object[] args) {
            Class[] types = null;
            if (args != null) {
                types = new Class[args.length];
                for (int i = 0; i < args.length; ++i) {
                    types[i] = args[i] instanceof Integer ? Integer.TYPE : (args[i] instanceof Boolean ? Boolean.TYPE : (args[i] instanceof ColorUIResource ? Color.class : args[i].getClass()));
                }
            }
            return types;
        }

        private String printArgs(Object[] array) {
            String s = "{";
            if (array != null) {
                for (int i = 0; i < array.length - 1; ++i) {
                    s = s.concat(String.valueOf(array[i]) + ",");
                }
                s = s.concat(String.valueOf(array[array.length - 1]) + "}");
            } else {
                s = s.concat("}");
            }
            return s;
        }
    }
}

