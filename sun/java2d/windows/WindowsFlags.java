/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.windows;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.windows.WToolkit;
import sun.java2d.opengl.WGLGraphicsConfig;

public class WindowsFlags {
    private static boolean gdiBlitEnabled;
    private static boolean d3dEnabled;
    private static boolean d3dVerbose;
    private static boolean d3dSet;
    private static boolean d3dOnScreenEnabled;
    private static boolean oglEnabled;
    private static boolean oglVerbose;
    private static boolean offscreenSharingEnabled;
    private static boolean magPresent;
    private static boolean setHighDPIAware;

    private static native boolean initNativeFlags();

    public static void initFlags() {
    }

    private static boolean getBooleanProp(String p, boolean defaultVal) {
        String propString = System.getProperty(p);
        boolean returnVal = defaultVal;
        if (propString != null) {
            if (propString.equals("true") || propString.equals("t") || propString.equals("True") || propString.equals("T") || propString.isEmpty()) {
                returnVal = true;
            } else if (propString.equals("false") || propString.equals("f") || propString.equals("False") || propString.equals("F")) {
                returnVal = false;
            }
        }
        return returnVal;
    }

    private static boolean isBooleanPropTrueVerbose(String p) {
        String propString = System.getProperty(p);
        return propString != null && (propString.equals("True") || propString.equals("T"));
    }

    private static int getIntProp(String p, int defaultVal) {
        String propString = System.getProperty(p);
        int returnVal = defaultVal;
        if (propString != null) {
            try {
                returnVal = Integer.parseInt(propString);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return returnVal;
    }

    private static boolean getPropertySet(String p) {
        String propString = System.getProperty(p);
        return propString != null;
    }

    private static void initJavaFlags() {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                magPresent = WindowsFlags.getBooleanProp("javax.accessibility.screen_magnifier_present", false);
                boolean ddEnabled = !WindowsFlags.getBooleanProp("sun.java2d.noddraw", magPresent);
                boolean ddOffscreenEnabled = WindowsFlags.getBooleanProp("sun.java2d.ddoffscreen", ddEnabled);
                d3dEnabled = WindowsFlags.getBooleanProp("sun.java2d.d3d", ddEnabled && ddOffscreenEnabled);
                d3dOnScreenEnabled = WindowsFlags.getBooleanProp("sun.java2d.d3d.onscreen", d3dEnabled);
                oglEnabled = WindowsFlags.getBooleanProp("sun.java2d.opengl", false);
                if (oglEnabled) {
                    oglVerbose = WindowsFlags.isBooleanPropTrueVerbose("sun.java2d.opengl");
                    if (WGLGraphicsConfig.isWGLAvailable()) {
                        d3dEnabled = false;
                    } else {
                        if (oglVerbose) {
                            System.out.println("Could not enable OpenGL pipeline (WGL not available)");
                        }
                        oglEnabled = false;
                    }
                }
                gdiBlitEnabled = WindowsFlags.getBooleanProp("sun.java2d.gdiBlit", true);
                d3dSet = WindowsFlags.getPropertySet("sun.java2d.d3d");
                if (d3dSet) {
                    d3dVerbose = WindowsFlags.isBooleanPropTrueVerbose("sun.java2d.d3d");
                }
                offscreenSharingEnabled = WindowsFlags.getBooleanProp("sun.java2d.offscreenSharing", false);
                String dpiOverride = System.getProperty("sun.java2d.dpiaware");
                if (dpiOverride != null) {
                    setHighDPIAware = dpiOverride.equalsIgnoreCase("true");
                } else {
                    String sunLauncherProperty = System.getProperty("sun.java.launcher", "unknown");
                    setHighDPIAware = sunLauncherProperty.equalsIgnoreCase("SUN_STANDARD");
                }
                return null;
            }
        });
    }

    public static boolean isD3DEnabled() {
        return d3dEnabled;
    }

    public static boolean isD3DSet() {
        return d3dSet;
    }

    public static boolean isD3DOnScreenEnabled() {
        return d3dOnScreenEnabled;
    }

    public static boolean isD3DVerbose() {
        return d3dVerbose;
    }

    public static boolean isGdiBlitEnabled() {
        return gdiBlitEnabled;
    }

    public static boolean isOffscreenSharingEnabled() {
        return offscreenSharingEnabled;
    }

    public static boolean isMagPresent() {
        return magPresent;
    }

    public static boolean isOGLEnabled() {
        return oglEnabled;
    }

    public static boolean isOGLVerbose() {
        return oglVerbose;
    }

    static {
        WToolkit.loadLibraries();
        WindowsFlags.initJavaFlags();
        WindowsFlags.initNativeFlags();
    }
}

