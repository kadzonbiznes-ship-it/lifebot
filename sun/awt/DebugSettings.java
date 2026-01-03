/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import sun.awt.NativeLibLoader;
import sun.util.logging.PlatformLogger;

public final class DebugSettings {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.debug.DebugSettings");
    static final String PREFIX = "awtdebug";
    static final String PROP_FILE = "properties";
    private static final String[] DEFAULT_PROPS = new String[]{"awtdebug.assert=true", "awtdebug.trace=false", "awtdebug.on=true", "awtdebug.ctrace=false"};
    private static final DebugSettings instance = new DebugSettings();
    private final Properties props = new Properties();
    private static final String PROP_CTRACE = "ctrace";
    private static final int PROP_CTRACE_LEN = "ctrace".length();

    static synchronized void init() {
        if (!DebugSettings.instance.props.isEmpty()) {
            return;
        }
        NativeLibLoader.loadLibraries();
        instance.loadProperties();
        instance.loadNativeSettings();
    }

    public static DebugSettings getInstance() {
        return instance;
    }

    private synchronized void loadProperties() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                DebugSettings.this.loadDefaultProperties();
                DebugSettings.this.loadFileProperties();
                DebugSettings.this.loadSystemProperties();
                return null;
            }
        });
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("DebugSettings:\n{0}", this);
        }
    }

    public String toString() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(bout);
        for (String key : this.props.stringPropertyNames()) {
            String value = this.props.getProperty(key, "");
            pout.println(key + " = " + value);
        }
        return bout.toString();
    }

    private void loadDefaultProperties() {
        try {
            for (int nprop = 0; nprop < DEFAULT_PROPS.length; ++nprop) {
                StringBufferInputStream in = new StringBufferInputStream(DEFAULT_PROPS[nprop]);
                this.props.load(in);
                in.close();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void loadFileProperties() {
        Object propPath = System.getProperty("awtdebug.properties", "");
        if (((String)propPath).isEmpty()) {
            propPath = System.getProperty("user.home", "") + File.separator + "awtdebug.properties";
        }
        File propFile = new File((String)propPath);
        try {
            this.println("Reading debug settings from '" + propFile.getCanonicalPath() + "'...");
            try (FileInputStream fin = new FileInputStream(propFile);){
                this.props.load(fin);
            }
        }
        catch (FileNotFoundException fne) {
            this.println("Did not find settings file.");
        }
        catch (IOException ioe) {
            this.println("Problem reading settings, IOException: " + ioe.getMessage());
        }
    }

    private void loadSystemProperties() {
        Properties sysProps = System.getProperties();
        for (String key : sysProps.stringPropertyNames()) {
            String value = sysProps.getProperty(key, "");
            if (!key.startsWith(PREFIX)) continue;
            this.props.setProperty(key, value);
        }
    }

    public synchronized boolean getBoolean(String key, boolean defval) {
        String value = this.getString(key, String.valueOf(defval));
        return value.equalsIgnoreCase("true");
    }

    public synchronized int getInt(String key, int defval) {
        String value = this.getString(key, String.valueOf(defval));
        return Integer.parseInt(value);
    }

    public synchronized String getString(String key, String defval) {
        String actualKeyName = "awtdebug." + key;
        String value = this.props.getProperty(actualKeyName, defval);
        return value;
    }

    private synchronized List<String> getPropertyNames() {
        LinkedList<String> propNames = new LinkedList<String>();
        for (String propName : this.props.stringPropertyNames()) {
            propName = propName.substring(PREFIX.length() + 1);
            propNames.add(propName);
        }
        return propNames;
    }

    private void println(Object object) {
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer(object.toString());
        }
    }

    private synchronized native void setCTracingOn(boolean var1);

    private synchronized native void setCTracingOn(boolean var1, String var2);

    private synchronized native void setCTracingOn(boolean var1, String var2, int var3);

    private void loadNativeSettings() {
        boolean ctracingOn = this.getBoolean(PROP_CTRACE, false);
        this.setCTracingOn(ctracingOn);
        LinkedList<String> traces = new LinkedList<String>();
        for (String key : this.getPropertyNames()) {
            if (!key.startsWith(PROP_CTRACE) || key.length() <= PROP_CTRACE_LEN) continue;
            traces.add(key);
        }
        Collections.sort(traces);
        for (String key : traces) {
            String trace = key.substring(PROP_CTRACE_LEN + 1);
            int delim = trace.indexOf(64);
            String filespec = delim != -1 ? trace.substring(0, delim) : trace;
            String linespec = delim != -1 ? trace.substring(delim + 1) : "";
            boolean enabled = this.getBoolean(key, false);
            if (linespec.length() == 0) {
                this.setCTracingOn(enabled, filespec);
                continue;
            }
            int linenum = Integer.parseInt(linespec, 10);
            this.setCTracingOn(enabled, filespec, linenum);
        }
    }
}

