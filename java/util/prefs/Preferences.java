/*
 * Decompiled with CFR 0.152.
 */
package java.util.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferencesFactory;
import java.util.prefs.XmlSupport;
import jdk.internal.util.OperatingSystem;

public abstract class Preferences {
    private static final PreferencesFactory factory = Preferences.factory();
    public static final int MAX_KEY_LENGTH = 80;
    public static final int MAX_VALUE_LENGTH = 8192;
    public static final int MAX_NAME_LENGTH = 80;
    private static Permission prefsPerm = new RuntimePermission("preferences");

    private static PreferencesFactory factory() {
        String factoryName = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return System.getProperty("java.util.prefs.PreferencesFactory");
            }
        });
        if (factoryName != null) {
            try {
                Object result = Class.forName(factoryName, false, ClassLoader.getSystemClassLoader()).newInstance();
                return (PreferencesFactory)result;
            }
            catch (Exception ex) {
                try {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(new AllPermission());
                    }
                    Object result = Class.forName(factoryName, false, Thread.currentThread().getContextClassLoader()).newInstance();
                    return (PreferencesFactory)result;
                }
                catch (Exception e) {
                    throw new InternalError("Can't instantiate Preferences factory " + factoryName, e);
                }
            }
        }
        return AccessController.doPrivileged(new PrivilegedAction<PreferencesFactory>(){

            @Override
            public PreferencesFactory run() {
                return Preferences.factory1();
            }
        });
    }

    private static PreferencesFactory factory1() {
        Iterator<PreferencesFactory> itr = ServiceLoader.load(PreferencesFactory.class, ClassLoader.getSystemClassLoader()).iterator();
        while (itr.hasNext()) {
            try {
                return itr.next();
            }
            catch (ServiceConfigurationError sce) {
                if (sce.getCause() instanceof SecurityException) continue;
                throw sce;
            }
        }
        String platformFactory = switch (OperatingSystem.current()) {
            case OperatingSystem.WINDOWS -> "java.util.prefs.WindowsPreferencesFactory";
            case OperatingSystem.MACOS -> "java.util.prefs.MacOSXPreferencesFactory";
            default -> "java.util.prefs.FileSystemPreferencesFactory";
        };
        try {
            Object result = Class.forName(platformFactory, false, Preferences.class.getClassLoader()).newInstance();
            return (PreferencesFactory)result;
        }
        catch (Exception e) {
            throw new InternalError("Can't instantiate platform default Preferences factory " + platformFactory, e);
        }
    }

    public static Preferences userNodeForPackage(Class<?> c) {
        return Preferences.userRoot().node(Preferences.nodeName(c));
    }

    public static Preferences systemNodeForPackage(Class<?> c) {
        return Preferences.systemRoot().node(Preferences.nodeName(c));
    }

    private static String nodeName(Class<?> c) {
        if (c.isArray()) {
            throw new IllegalArgumentException("Arrays have no associated preferences node.");
        }
        String className = c.getName();
        int pkgEndIndex = className.lastIndexOf(46);
        if (pkgEndIndex < 0) {
            return "/<unnamed>";
        }
        String packageName = className.substring(0, pkgEndIndex);
        return "/" + packageName.replace('.', '/');
    }

    public static Preferences userRoot() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(prefsPerm);
        }
        return factory.userRoot();
    }

    public static Preferences systemRoot() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(prefsPerm);
        }
        return factory.systemRoot();
    }

    protected Preferences() {
    }

    public abstract void put(String var1, String var2);

    public abstract String get(String var1, String var2);

    public abstract void remove(String var1);

    public abstract void clear() throws BackingStoreException;

    public abstract void putInt(String var1, int var2);

    public abstract int getInt(String var1, int var2);

    public abstract void putLong(String var1, long var2);

    public abstract long getLong(String var1, long var2);

    public abstract void putBoolean(String var1, boolean var2);

    public abstract boolean getBoolean(String var1, boolean var2);

    public abstract void putFloat(String var1, float var2);

    public abstract float getFloat(String var1, float var2);

    public abstract void putDouble(String var1, double var2);

    public abstract double getDouble(String var1, double var2);

    public abstract void putByteArray(String var1, byte[] var2);

    public abstract byte[] getByteArray(String var1, byte[] var2);

    public abstract String[] keys() throws BackingStoreException;

    public abstract String[] childrenNames() throws BackingStoreException;

    public abstract Preferences parent();

    public abstract Preferences node(String var1);

    public abstract boolean nodeExists(String var1) throws BackingStoreException;

    public abstract void removeNode() throws BackingStoreException;

    public abstract String name();

    public abstract String absolutePath();

    public abstract boolean isUserNode();

    public abstract String toString();

    public abstract void flush() throws BackingStoreException;

    public abstract void sync() throws BackingStoreException;

    public abstract void addPreferenceChangeListener(PreferenceChangeListener var1);

    public abstract void removePreferenceChangeListener(PreferenceChangeListener var1);

    public abstract void addNodeChangeListener(NodeChangeListener var1);

    public abstract void removeNodeChangeListener(NodeChangeListener var1);

    public abstract void exportNode(OutputStream var1) throws IOException, BackingStoreException;

    public abstract void exportSubtree(OutputStream var1) throws IOException, BackingStoreException;

    public static void importPreferences(InputStream is) throws IOException, InvalidPreferencesFormatException {
        XmlSupport.importPreferences(is);
    }
}

