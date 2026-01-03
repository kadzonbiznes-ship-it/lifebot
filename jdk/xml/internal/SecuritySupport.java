/*
 * Decompiled with CFR 0.152.
 */
package jdk.xml.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedActionException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class SecuritySupport {
    public static final String NEWLINE = System.lineSeparator();
    static final Properties cacheProps = new Properties();
    static volatile boolean firstTime = true;

    private SecuritySupport() {
    }

    public static String getErrorMessage(Locale locale, String bundle, String key, Object[] arguments) {
        ResourceBundle rb = locale != null ? ResourceBundle.getBundle(bundle, locale) : ResourceBundle.getBundle(bundle);
        String msg = rb.getString(key);
        if (arguments != null) {
            msg = MessageFormat.format(msg, arguments);
        }
        return msg;
    }

    public static String getSystemProperty(String propName) {
        return AccessController.doPrivileged(() -> System.getProperty(propName));
    }

    public static String getSystemProperty(String propName, String defValue) {
        String value = SecuritySupport.getSystemProperty(propName);
        if (value == null) {
            return defValue;
        }
        return value;
    }

    public static <T> T getSystemProperty(Class<T> type, String propName, String defValue) {
        String value = SecuritySupport.getSystemProperty(propName);
        if (value == null) {
            value = defValue;
        }
        if (Integer.class == type) {
            return type.cast(Integer.parseInt(value));
        }
        if (Boolean.class == type) {
            return type.cast(Boolean.parseBoolean(value));
        }
        return type.cast(value);
    }

    public static <T> T getJAXPSystemProperty(Class<T> type, String propName, String defValue) {
        String value = SecuritySupport.getJAXPSystemProperty(propName);
        if (value == null) {
            value = defValue;
        }
        if (Integer.class.isAssignableFrom(type)) {
            return type.cast(Integer.parseInt(value));
        }
        if (Boolean.class.isAssignableFrom(type)) {
            return type.cast(Boolean.parseBoolean(value));
        }
        return type.cast(value);
    }

    public static String getJAXPSystemProperty(String propName) {
        String value = SecuritySupport.getSystemProperty(propName);
        if (value == null) {
            value = SecuritySupport.readConfig(propName);
        }
        return value;
    }

    public static String readConfig(String propName) {
        return SecuritySupport.readConfig(propName, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String readConfig(String propName, boolean stax) {
        if (firstTime) {
            Properties properties = cacheProps;
            synchronized (properties) {
                if (firstTime) {
                    String configFile;
                    boolean found = SecuritySupport.loadProperties(Paths.get(SecuritySupport.getSystemProperty("java.home"), "conf", "jaxp.properties").toAbsolutePath().normalize().toString());
                    if (stax && !found) {
                        found = SecuritySupport.loadProperties(Paths.get(SecuritySupport.getSystemProperty("java.home"), "conf", "stax.properties").toAbsolutePath().normalize().toString());
                    }
                    if ((configFile = SecuritySupport.getSystemProperty("java.xml.config.file")) != null) {
                        SecuritySupport.loadProperties(configFile);
                    }
                    firstTime = false;
                }
            }
        }
        return cacheProps.getProperty(propName);
    }

    private static boolean loadProperties(String file) {
        File f = new File(file);
        if (SecuritySupport.doesFileExist(f)) {
            boolean bl;
            block9: {
                FileInputStream in = SecuritySupport.getFileInputStream(f);
                try {
                    cacheProps.load(in);
                    bl = true;
                    if (in == null) break block9;
                }
                catch (Throwable throwable) {
                    try {
                        if (in != null) {
                            try {
                                ((InputStream)in).close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                ((InputStream)in).close();
            }
            return bl;
        }
        return false;
    }

    public static boolean isDirectory(File f) {
        return AccessController.doPrivileged(() -> f.isDirectory());
    }

    public static boolean isFileExists(File f) {
        return AccessController.doPrivileged(() -> f.exists());
    }

    public static FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        try {
            return AccessController.doPrivileged(() -> new FileInputStream(file));
        }
        catch (PrivilegedActionException e) {
            throw (FileNotFoundException)e.getException();
        }
    }

    public static InputStream getResourceAsStream(String name) {
        return AccessController.doPrivileged(() -> SecuritySupport.class.getResourceAsStream("/" + name));
    }

    public static ResourceBundle getResourceBundle(String bundle) {
        return SecuritySupport.getResourceBundle(bundle, Locale.getDefault());
    }

    public static ResourceBundle getResourceBundle(String bundle, Locale locale) {
        return AccessController.doPrivileged(() -> {
            try {
                return ResourceBundle.getBundle(bundle, locale);
            }
            catch (MissingResourceException e) {
                try {
                    return ResourceBundle.getBundle(bundle, Locale.US);
                }
                catch (MissingResourceException e2) {
                    throw new MissingResourceException("Could not load any resource bundle by " + bundle, bundle, "");
                }
            }
        });
    }

    public static boolean doesFileExist(File f) {
        return AccessController.doPrivileged(() -> f.exists());
    }

    static long getLastModified(File f) {
        return AccessController.doPrivileged(() -> f.lastModified());
    }

    public static String sanitizePath(String uri) {
        if (uri == null) {
            return "";
        }
        int i = uri.lastIndexOf("/");
        if (i > 0) {
            return uri.substring(i + 1, uri.length());
        }
        return "";
    }

    public static String checkAccess(String systemId, String allowedProtocols, String accessAny) throws IOException {
        String protocol;
        if (systemId == null || allowedProtocols != null && allowedProtocols.equalsIgnoreCase(accessAny)) {
            return null;
        }
        if (!systemId.contains(":")) {
            protocol = "file";
        } else {
            URL url = new URL(systemId);
            protocol = url.getProtocol();
            if (protocol.equalsIgnoreCase("jar")) {
                String path = url.getPath();
                protocol = path.substring(0, path.indexOf(":"));
            } else if (protocol.equalsIgnoreCase("jrt")) {
                protocol = "file";
            }
        }
        if (SecuritySupport.isProtocolAllowed(protocol, allowedProtocols)) {
            return null;
        }
        return protocol;
    }

    private static boolean isProtocolAllowed(String protocol, String allowedProtocols) {
        String[] temp;
        if (allowedProtocols == null) {
            return false;
        }
        for (String t : temp = allowedProtocols.split(",")) {
            if (!(t = t.trim()).equalsIgnoreCase(protocol)) continue;
            return true;
        }
        return false;
    }

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(() -> {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            return cl;
        });
    }

    public static ClassLoader getSystemClassLoader() {
        return AccessController.doPrivileged(() -> {
            ClassLoader cl = null;
            try {
                cl = ClassLoader.getSystemClassLoader();
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
            return cl;
        });
    }

    public static ClassLoader getParentClassLoader(ClassLoader cl) {
        return AccessController.doPrivileged(() -> {
            ClassLoader parent = null;
            try {
                parent = cl.getParent();
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
            return parent == cl ? null : parent;
        });
    }

    public static String getClassSource(Class<?> cls) {
        return AccessController.doPrivileged(() -> {
            CodeSource cs = cls.getProtectionDomain().getCodeSource();
            if (cs != null) {
                URL loc = cs.getLocation();
                return loc != null ? loc.toString() : "(no location)";
            }
            return "(no code source)";
        });
    }

    public static ClassLoader getClassLoader() throws SecurityException {
        return AccessController.doPrivileged(() -> {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            return cl;
        });
    }

    public static InputStream getResourceAsStream(ClassLoader cl, String name) {
        return AccessController.doPrivileged(() -> {
            InputStream ris = cl == null ? SecuritySupport.class.getResourceAsStream(name) : cl.getResourceAsStream(name);
            return ris;
        });
    }
}

