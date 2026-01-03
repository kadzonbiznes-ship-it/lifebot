/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.LookAndFeel;
import javax.swing.MultiUIDefaults;
import javax.swing.RepaintManager;
import javax.swing.SwingPaintEventDispatcher;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.OSInfo;
import sun.awt.PaintEventDispatcher;
import sun.awt.SunToolkit;
import sun.security.action.GetPropertyAction;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;

public class UIManager
implements Serializable {
    private static final Object classLock = new Object();
    private static final String defaultLAFKey = "swing.defaultlaf";
    private static final String auxiliaryLAFsKey = "swing.auxiliarylaf";
    private static final String multiplexingLAFKey = "swing.plaf.multiplexinglaf";
    private static final String installedLAFsKey = "swing.installedlafs";
    private static final String disableMnemonicKey = "swing.disablenavaids";
    private static LookAndFeelInfo[] installedLAFs;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static LAFState getLAFState() {
        LAFState rv = (LAFState)SwingUtilities.appContextGet(SwingUtilities2.LAF_STATE_KEY);
        if (rv == null) {
            Object object = classLock;
            synchronized (object) {
                rv = (LAFState)SwingUtilities.appContextGet(SwingUtilities2.LAF_STATE_KEY);
                if (rv == null) {
                    rv = new LAFState();
                    SwingUtilities.appContextPut(SwingUtilities2.LAF_STATE_KEY, rv);
                }
            }
        }
        return rv;
    }

    private static String makeInstalledLAFKey(String laf, String attr) {
        return "swing.installedlaf." + laf + "." + attr;
    }

    private static String makeSwingPropertiesFilename() {
        String sep = File.separator;
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            javaHome = "<java.home undefined>";
        }
        return javaHome + sep + "conf" + sep + "swing.properties";
    }

    public static LookAndFeelInfo[] getInstalledLookAndFeels() {
        UIManager.maybeInitialize();
        LookAndFeelInfo[] ilafs = UIManager.getLAFState().installedLAFs;
        if (ilafs == null) {
            ilafs = installedLAFs;
        }
        LookAndFeelInfo[] rv = new LookAndFeelInfo[ilafs.length];
        System.arraycopy(ilafs, 0, rv, 0, ilafs.length);
        return rv;
    }

    public static void setInstalledLookAndFeels(LookAndFeelInfo[] infos) throws SecurityException {
        UIManager.maybeInitialize();
        LookAndFeelInfo[] newInfos = new LookAndFeelInfo[infos.length];
        System.arraycopy(infos, 0, newInfos, 0, infos.length);
        UIManager.getLAFState().installedLAFs = newInfos;
    }

    public static void installLookAndFeel(LookAndFeelInfo info) {
        LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        LookAndFeelInfo[] newInfos = new LookAndFeelInfo[infos.length + 1];
        System.arraycopy(infos, 0, newInfos, 0, infos.length);
        newInfos[infos.length] = info;
        UIManager.setInstalledLookAndFeels(newInfos);
    }

    public static void installLookAndFeel(String name, String className) {
        UIManager.installLookAndFeel(new LookAndFeelInfo(name, className));
    }

    public static LookAndFeel getLookAndFeel() {
        UIManager.maybeInitialize();
        return UIManager.getLAFState().lookAndFeel;
    }

    public static LookAndFeel createLookAndFeel(String name) throws UnsupportedLookAndFeelException {
        Objects.requireNonNull(name);
        if ("GTK look and feel".equals(name)) {
            name = "GTK+";
        }
        try {
            for (LookAndFeelInfo info : installedLAFs) {
                if (!info.getName().equals(name)) continue;
                Class<?> cls = Class.forName(UIManager.class.getModule(), info.getClassName());
                LookAndFeel laf = (LookAndFeel)cls.newInstance();
                if (laf.isSupportedLookAndFeel()) {
                    return laf;
                }
                break;
            }
        }
        catch (IllegalArgumentException | ReflectiveOperationException exception) {
            // empty catch block
        }
        throw new UnsupportedLookAndFeelException(name);
    }

    public static void setLookAndFeel(LookAndFeel newLookAndFeel) throws UnsupportedLookAndFeelException {
        if (newLookAndFeel != null && !newLookAndFeel.isSupportedLookAndFeel()) {
            String s = newLookAndFeel.toString() + " not supported on this platform";
            throw new UnsupportedLookAndFeelException(s);
        }
        LAFState lafState = UIManager.getLAFState();
        LookAndFeel oldLookAndFeel = lafState.lookAndFeel;
        if (oldLookAndFeel != null) {
            oldLookAndFeel.uninitialize();
        }
        lafState.lookAndFeel = newLookAndFeel;
        if (newLookAndFeel != null) {
            DefaultLookup.setDefaultLookup(null);
            newLookAndFeel.initialize();
            lafState.setLookAndFeelDefaults(newLookAndFeel.getDefaults());
        } else {
            lafState.setLookAndFeelDefaults(null);
        }
        SwingPropertyChangeSupport changeSupport = lafState.getPropertyChangeSupport(false);
        if (changeSupport != null) {
            changeSupport.firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
        }
    }

    public static void setLookAndFeel(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        if ("javax.swing.plaf.metal.MetalLookAndFeel".equals(className)) {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } else {
            Class<?> lnfClass = SwingUtilities.loadSystemClass(className);
            UIManager.setLookAndFeel((LookAndFeel)lnfClass.newInstance());
        }
    }

    public static String getSystemLookAndFeelClassName() {
        String systemLAF = AccessController.doPrivileged(new GetPropertyAction("swing.systemlaf"));
        if (systemLAF != null) {
            return systemLAF;
        }
        OSInfo.OSType osType = OSInfo.getOSType();
        if (osType == OSInfo.OSType.WINDOWS) {
            return "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof SunToolkit) {
            SunToolkit suntk = (SunToolkit)toolkit;
            String desktop = suntk.getDesktop();
            boolean gtkAvailable = suntk.isNativeGTKAvailable();
            if ("gnome".equals(desktop) && gtkAvailable) {
                return "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            }
        }
        if (osType == OSInfo.OSType.MACOSX && toolkit.getClass().getName().equals("sun.lwawt.macosx.LWCToolkit")) {
            return "com.apple.laf.AquaLookAndFeel";
        }
        return UIManager.getCrossPlatformLookAndFeelClassName();
    }

    public static String getCrossPlatformLookAndFeelClassName() {
        String laf = AccessController.doPrivileged(new GetPropertyAction("swing.crossplatformlaf"));
        if (laf != null) {
            return laf;
        }
        return "javax.swing.plaf.metal.MetalLookAndFeel";
    }

    public static UIDefaults getDefaults() {
        UIManager.maybeInitialize();
        return UIManager.getLAFState().multiUIDefaults;
    }

    public static Font getFont(Object key) {
        return UIManager.getDefaults().getFont(key);
    }

    public static Font getFont(Object key, Locale l) {
        return UIManager.getDefaults().getFont(key, l);
    }

    public static Color getColor(Object key) {
        return UIManager.getDefaults().getColor(key);
    }

    public static Color getColor(Object key, Locale l) {
        return UIManager.getDefaults().getColor(key, l);
    }

    public static Icon getIcon(Object key) {
        return UIManager.getDefaults().getIcon(key);
    }

    public static Icon getIcon(Object key, Locale l) {
        return UIManager.getDefaults().getIcon(key, l);
    }

    public static Border getBorder(Object key) {
        return UIManager.getDefaults().getBorder(key);
    }

    public static Border getBorder(Object key, Locale l) {
        return UIManager.getDefaults().getBorder(key, l);
    }

    public static String getString(Object key) {
        return UIManager.getDefaults().getString(key);
    }

    public static String getString(Object key, Locale l) {
        return UIManager.getDefaults().getString(key, l);
    }

    static String getString(Object key, Component c) {
        Locale l = c == null ? Locale.getDefault() : c.getLocale();
        return UIManager.getString(key, l);
    }

    public static int getInt(Object key) {
        return UIManager.getDefaults().getInt(key);
    }

    public static int getInt(Object key, Locale l) {
        return UIManager.getDefaults().getInt(key, l);
    }

    public static boolean getBoolean(Object key) {
        return UIManager.getDefaults().getBoolean(key);
    }

    public static boolean getBoolean(Object key, Locale l) {
        return UIManager.getDefaults().getBoolean(key, l);
    }

    public static Insets getInsets(Object key) {
        return UIManager.getDefaults().getInsets(key);
    }

    public static Insets getInsets(Object key, Locale l) {
        return UIManager.getDefaults().getInsets(key, l);
    }

    public static Dimension getDimension(Object key) {
        return UIManager.getDefaults().getDimension(key);
    }

    public static Dimension getDimension(Object key, Locale l) {
        return UIManager.getDefaults().getDimension(key, l);
    }

    public static Object get(Object key) {
        return UIManager.getDefaults().get(key);
    }

    public static Object get(Object key, Locale l) {
        return UIManager.getDefaults().get(key, l);
    }

    public static Object put(Object key, Object value) {
        return UIManager.getDefaults().put(key, value);
    }

    public static ComponentUI getUI(JComponent target) {
        UIManager.maybeInitialize();
        UIManager.maybeInitializeFocusPolicy(target);
        ComponentUI ui = null;
        LookAndFeel multiLAF = UIManager.getLAFState().multiLookAndFeel;
        if (multiLAF != null) {
            ui = multiLAF.getDefaults().getUI(target);
        }
        if (ui == null) {
            ui = UIManager.getDefaults().getUI(target);
        }
        return ui;
    }

    public static UIDefaults getLookAndFeelDefaults() {
        UIManager.maybeInitialize();
        return UIManager.getLAFState().getLookAndFeelDefaults();
    }

    private static LookAndFeel getMultiLookAndFeel() {
        LookAndFeel multiLookAndFeel = UIManager.getLAFState().multiLookAndFeel;
        if (multiLookAndFeel == null) {
            String defaultName = "javax.swing.plaf.multi.MultiLookAndFeel";
            String className = UIManager.getLAFState().swingProps.getProperty(multiplexingLAFKey, defaultName);
            try {
                Class<?> lnfClass = SwingUtilities.loadSystemClass(className);
                multiLookAndFeel = (LookAndFeel)lnfClass.newInstance();
            }
            catch (Exception exc) {
                System.err.println("UIManager: failed loading " + className);
            }
        }
        return multiLookAndFeel;
    }

    public static void addAuxiliaryLookAndFeel(LookAndFeel laf) {
        UIManager.maybeInitialize();
        if (!laf.isSupportedLookAndFeel()) {
            return;
        }
        Vector<LookAndFeel> v = UIManager.getLAFState().auxLookAndFeels;
        if (v == null) {
            v = new Vector();
        }
        if (!v.contains(laf)) {
            v.addElement(laf);
            laf.initialize();
            UIManager.getLAFState().auxLookAndFeels = v;
            if (UIManager.getLAFState().multiLookAndFeel == null) {
                UIManager.getLAFState().multiLookAndFeel = UIManager.getMultiLookAndFeel();
            }
        }
    }

    public static boolean removeAuxiliaryLookAndFeel(LookAndFeel laf) {
        UIManager.maybeInitialize();
        Vector<LookAndFeel> v = UIManager.getLAFState().auxLookAndFeels;
        if (v == null || v.size() == 0) {
            return false;
        }
        boolean result = v.removeElement(laf);
        if (result) {
            if (v.size() == 0) {
                UIManager.getLAFState().auxLookAndFeels = null;
                UIManager.getLAFState().multiLookAndFeel = null;
            } else {
                UIManager.getLAFState().auxLookAndFeels = v;
            }
        }
        laf.uninitialize();
        return result;
    }

    public static LookAndFeel[] getAuxiliaryLookAndFeels() {
        UIManager.maybeInitialize();
        Vector<LookAndFeel> v = UIManager.getLAFState().auxLookAndFeels;
        if (v == null || v.size() == 0) {
            return null;
        }
        LookAndFeel[] rv = new LookAndFeel[v.size()];
        for (int i = 0; i < rv.length; ++i) {
            rv[i] = v.elementAt(i);
        }
        return rv;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        Object object = classLock;
        synchronized (object) {
            UIManager.getLAFState().getPropertyChangeSupport(true).addPropertyChangeListener(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        Object object = classLock;
        synchronized (object) {
            UIManager.getLAFState().getPropertyChangeSupport(true).removePropertyChangeListener(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PropertyChangeListener[] getPropertyChangeListeners() {
        Object object = classLock;
        synchronized (object) {
            return UIManager.getLAFState().getPropertyChangeSupport(true).getPropertyChangeListeners();
        }
    }

    private static Properties loadSwingProperties() {
        if (UIManager.class.getClassLoader() != null) {
            return new Properties();
        }
        final Properties props = new Properties();
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                block8: {
                    if (OSInfo.getOSType() == OSInfo.OSType.MACOSX) {
                        props.put(UIManager.defaultLAFKey, UIManager.getSystemLookAndFeelClassName());
                    }
                    try {
                        File file = new File(UIManager.makeSwingPropertiesFilename());
                        if (!file.exists()) break block8;
                        try (FileInputStream ins = new FileInputStream(file);){
                            props.load(ins);
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                UIManager.checkProperty(props, UIManager.defaultLAFKey);
                UIManager.checkProperty(props, UIManager.auxiliaryLAFsKey);
                UIManager.checkProperty(props, UIManager.multiplexingLAFKey);
                UIManager.checkProperty(props, UIManager.installedLAFsKey);
                UIManager.checkProperty(props, UIManager.disableMnemonicKey);
                return null;
            }
        });
        return props;
    }

    private static void checkProperty(Properties props, String key) {
        String value = System.getProperty(key);
        if (value != null) {
            props.put(key, value);
        }
    }

    private static void initializeInstalledLAFs(Properties swingProps) {
        String ilafsString = swingProps.getProperty(installedLAFsKey);
        if (ilafsString == null) {
            return;
        }
        ArrayList<String> lafs = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(ilafsString, ",", false);
        while (st.hasMoreTokens()) {
            lafs.add(st.nextToken());
        }
        ArrayList<LookAndFeelInfo> ilafs = new ArrayList<LookAndFeelInfo>(lafs.size());
        for (String laf : lafs) {
            String name = swingProps.getProperty(UIManager.makeInstalledLAFKey(laf, "name"), laf);
            String cls = swingProps.getProperty(UIManager.makeInstalledLAFKey(laf, "class"));
            if (cls == null) continue;
            ilafs.add(new LookAndFeelInfo(name, cls));
        }
        LookAndFeelInfo[] installedLAFs = new LookAndFeelInfo[ilafs.size()];
        for (int i = 0; i < ilafs.size(); ++i) {
            installedLAFs[i] = (LookAndFeelInfo)ilafs.get(i);
        }
        UIManager.getLAFState().installedLAFs = installedLAFs;
    }

    private static void initializeDefaultLAF(Properties swingProps) {
        if (UIManager.getLAFState().lookAndFeel != null) {
            return;
        }
        String lafName = null;
        HashMap lafData = (HashMap)AppContext.getAppContext().remove("swing.lafdata");
        if (lafData != null) {
            lafName = (String)lafData.remove("defaultlaf");
        }
        if (lafName == null) {
            lafName = UIManager.getCrossPlatformLookAndFeelClassName();
        }
        lafName = swingProps.getProperty(defaultLAFKey, lafName);
        try {
            UIManager.setLookAndFeel(lafName);
        }
        catch (Exception e) {
            throw new Error("Cannot load " + lafName);
        }
        if (lafData != null) {
            for (Object key : lafData.keySet()) {
                UIManager.put(key, lafData.get(key));
            }
        }
    }

    private static void initializeAuxiliaryLAFs(Properties swingProps) {
        String auxLookAndFeelNames = swingProps.getProperty(auxiliaryLAFsKey);
        if (auxLookAndFeelNames == null) {
            return;
        }
        Vector<LookAndFeel> auxLookAndFeels = new Vector<LookAndFeel>();
        StringTokenizer p = new StringTokenizer(auxLookAndFeelNames, ",");
        while (p.hasMoreTokens()) {
            String className = p.nextToken();
            try {
                Class<?> lnfClass = SwingUtilities.loadSystemClass(className);
                LookAndFeel newLAF = (LookAndFeel)lnfClass.newInstance();
                newLAF.initialize();
                auxLookAndFeels.addElement(newLAF);
            }
            catch (Exception e) {
                System.err.println("UIManager: failed loading auxiliary look and feel " + className);
            }
        }
        if (auxLookAndFeels.size() == 0) {
            auxLookAndFeels = null;
        } else {
            UIManager.getLAFState().multiLookAndFeel = UIManager.getMultiLookAndFeel();
            if (UIManager.getLAFState().multiLookAndFeel == null) {
                auxLookAndFeels = null;
            }
        }
        UIManager.getLAFState().auxLookAndFeels = auxLookAndFeels;
    }

    private static void initializeSystemDefaults(Properties swingProps) {
        UIManager.getLAFState().swingProps = swingProps;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void maybeInitialize() {
        Object object = classLock;
        synchronized (object) {
            if (!UIManager.getLAFState().initialized) {
                UIManager.getLAFState().initialized = true;
                UIManager.initialize();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void maybeInitializeFocusPolicy(JComponent comp) {
        if (comp instanceof JRootPane) {
            Object object = classLock;
            synchronized (object) {
                if (!UIManager.getLAFState().focusPolicyInitialized) {
                    UIManager.getLAFState().focusPolicyInitialized = true;
                    if (FocusManager.isFocusManagerEnabled()) {
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
                    }
                }
            }
        }
    }

    private static void initialize() {
        Properties swingProps = UIManager.loadSwingProperties();
        UIManager.initializeSystemDefaults(swingProps);
        UIManager.initializeDefaultLAF(swingProps);
        UIManager.initializeAuxiliaryLAFs(swingProps);
        UIManager.initializeInstalledLAFs(swingProps);
        if (RepaintManager.HANDLE_TOP_LEVEL_PAINT) {
            PaintEventDispatcher.setPaintEventDispatcher(new SwingPaintEventDispatcher());
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new KeyEventPostProcessor(){

            @Override
            public boolean postProcessKeyEvent(KeyEvent e) {
                Component c = e.getComponent();
                if ((!(c instanceof JComponent) || c != null && !c.isEnabled()) && JComponent.KeyboardState.shouldProcess(e) && SwingUtilities.processKeyBindings(e)) {
                    e.consume();
                    return true;
                }
                return false;
            }
        });
        AWTAccessor.getComponentAccessor().setRequestFocusController(JComponent.focusController);
    }

    static {
        ArrayList<LookAndFeelInfo> iLAFs = new ArrayList<LookAndFeelInfo>(4);
        iLAFs.add(new LookAndFeelInfo("Metal", "javax.swing.plaf.metal.MetalLookAndFeel"));
        iLAFs.add(new LookAndFeelInfo("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel"));
        iLAFs.add(new LookAndFeelInfo("CDE/Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
        OSInfo.OSType osType = OSInfo.getOSType();
        if (osType == OSInfo.OSType.WINDOWS) {
            iLAFs.add(new LookAndFeelInfo("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"));
            if (Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive") != null) {
                iLAFs.add(new LookAndFeelInfo("Windows Classic", "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"));
            }
        } else if (osType == OSInfo.OSType.MACOSX) {
            iLAFs.add(new LookAndFeelInfo("Mac OS X", "com.apple.laf.AquaLookAndFeel"));
        } else {
            iLAFs.add(new LookAndFeelInfo("GTK+", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"));
        }
        installedLAFs = iLAFs.toArray(new LookAndFeelInfo[iLAFs.size()]);
    }

    private static class LAFState {
        Properties swingProps;
        private UIDefaults[] tables = new UIDefaults[2];
        boolean initialized = false;
        boolean focusPolicyInitialized = false;
        MultiUIDefaults multiUIDefaults = new MultiUIDefaults(this.tables);
        LookAndFeel lookAndFeel;
        LookAndFeel multiLookAndFeel = null;
        Vector<LookAndFeel> auxLookAndFeels = null;
        SwingPropertyChangeSupport changeSupport;
        LookAndFeelInfo[] installedLAFs;

        private LAFState() {
        }

        UIDefaults getLookAndFeelDefaults() {
            return this.tables[0];
        }

        void setLookAndFeelDefaults(UIDefaults x) {
            this.tables[0] = x;
        }

        UIDefaults getSystemDefaults() {
            return this.tables[1];
        }

        void setSystemDefaults(UIDefaults x) {
            this.tables[1] = x;
        }

        public synchronized SwingPropertyChangeSupport getPropertyChangeSupport(boolean create) {
            if (create && this.changeSupport == null) {
                this.changeSupport = new SwingPropertyChangeSupport(UIManager.class);
            }
            return this.changeSupport;
        }
    }

    public static class LookAndFeelInfo {
        private String name;
        private String className;

        public LookAndFeelInfo(String name, String className) {
            this.name = name;
            this.className = className;
        }

        public String getName() {
            return this.name;
        }

        public String getClassName() {
            return this.className;
        }

        public String toString() {
            return this.getClass().getName() + "[" + this.getName() + " " + this.getClassName() + "]";
        }
    }
}

