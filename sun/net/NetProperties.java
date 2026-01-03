/*
 * Decompiled with CFR 0.152.
 */
package sun.net;

import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import jdk.internal.util.StaticProperty;

public class NetProperties {
    private static Properties props = new Properties();

    private NetProperties() {
    }

    private static void loadDefaultProperties() {
        String fname = StaticProperty.javaHome();
        if (fname == null) {
            throw new Error("Can't find java.home ??");
        }
        try {
            File f = new File(fname, "conf");
            f = new File(f, "net.properties");
            fname = f.getCanonicalPath();
            try (FileInputStream in = new FileInputStream(fname);){
                props.load(in);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static String get(String key) {
        String def = props.getProperty(key);
        try {
            return System.getProperty(key, def);
        }
        catch (IllegalArgumentException | NullPointerException runtimeException) {
            return null;
        }
    }

    public static Integer getInteger(String key, int defval) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        }
        catch (IllegalArgumentException | NullPointerException runtimeException) {
            // empty catch block
        }
        if (val != null) {
            try {
                return Integer.decode(val);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return defval;
    }

    public static Boolean getBoolean(String key) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        }
        catch (IllegalArgumentException | NullPointerException runtimeException) {
            // empty catch block
        }
        if (val != null) {
            try {
                return Boolean.valueOf(val);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return null;
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                NetProperties.loadDefaultProperties();
                return null;
            }
        });
    }
}

