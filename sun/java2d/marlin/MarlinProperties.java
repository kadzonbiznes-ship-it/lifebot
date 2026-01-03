/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.security.AccessController;
import sun.java2d.marlin.FloatMath;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinUtils;
import sun.security.action.GetPropertyAction;

public final class MarlinProperties {
    private MarlinProperties() {
    }

    public static boolean isUseThreadLocal() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.useThreadLocal", "true");
    }

    public static int getInitialEdges() {
        return MarlinProperties.align(MarlinProperties.getInteger("sun.java2d.renderer.edges", 4096, 64, 65536), 64);
    }

    public static int getInitialPixelWidth() {
        return MarlinProperties.align(MarlinProperties.getInteger("sun.java2d.renderer.pixelWidth", 4096, 64, 32768), 64);
    }

    public static int getInitialPixelHeight() {
        return MarlinProperties.align(MarlinProperties.getInteger("sun.java2d.renderer.pixelHeight", 2176, 64, 32768), 64);
    }

    public static boolean isProfileQuality() {
        String key = "sun.java2d.renderer.profile";
        String profile = MarlinProperties.getString("sun.java2d.renderer.profile", "quality");
        if ("quality".equals(profile)) {
            return true;
        }
        if ("speed".equals(profile)) {
            return false;
        }
        MarlinUtils.logInfo("Invalid value for sun.java2d.renderer.profile = " + profile + "; expect value in [quality, speed] !");
        return true;
    }

    public static int getSubPixel_Log2_X() {
        return MarlinProperties.getInteger("sun.java2d.renderer.subPixel_log2_X", 8, 0, 8);
    }

    public static int getSubPixel_Log2_Y() {
        int def = MarlinProperties.isProfileQuality() ? 3 : 2;
        return MarlinProperties.getInteger("sun.java2d.renderer.subPixel_log2_Y", def, 0, 8);
    }

    public static int getTileSize_Log2() {
        return MarlinProperties.getInteger("sun.java2d.renderer.tileSize_log2", 5, 3, 10);
    }

    public static int getTileWidth_Log2() {
        return MarlinProperties.getInteger("sun.java2d.renderer.tileWidth_log2", 5, 3, 10);
    }

    public static int getBlockSize_Log2() {
        return MarlinProperties.getInteger("sun.java2d.renderer.blockSize_log2", 5, 3, 8);
    }

    public static boolean isForceRLE() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.forceRLE", "false");
    }

    public static boolean isForceNoRLE() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.forceNoRLE", "false");
    }

    public static boolean isUseTileFlags() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.useTileFlags", "true");
    }

    public static boolean isUseTileFlagsWithHeuristics() {
        return MarlinProperties.isUseTileFlags() && MarlinProperties.getBoolean("sun.java2d.renderer.useTileFlags.useHeuristics", "true");
    }

    public static int getRLEMinWidth() {
        return MarlinProperties.getInteger("sun.java2d.renderer.rleMinWidth", 64, 0, Integer.MAX_VALUE);
    }

    public static boolean isUseSimplifier() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.useSimplifier", "false");
    }

    public static boolean isUsePathSimplifier() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.usePathSimplifier", "false");
    }

    public static float getPathSimplifierPixelTolerance() {
        return MarlinProperties.getFloat("sun.java2d.renderer.pathSimplifier.pixTol", 1.0f / MarlinConst.MIN_SUBPIXELS, 0.001f, 10.0f);
    }

    public static float getStrokerJoinError() {
        float def = 1.0f / MarlinConst.MIN_SUBPIXELS;
        float err = MarlinProperties.getFloat("sun.java2d.renderer.stroker.joinError", def, -1.0f, 10.0f);
        return err < 0.0f ? def : err;
    }

    public static int getStrokerJoinStyle() {
        return MarlinProperties.getInteger("sun.java2d.renderer.stroker.joinStyle", -1, -1, 2);
    }

    public static boolean isDoClip() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.clip", "true");
    }

    public static boolean isDoClipRuntimeFlag() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.clip.runtime.enable", "false");
    }

    public static boolean isDoClipAtRuntime() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.clip.runtime", "true");
    }

    public static boolean isDoClipSubdivider() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.clip.subdivider", "true");
    }

    public static float getSubdividerMinLength() {
        return MarlinProperties.getFloat("sun.java2d.renderer.clip.subdivider.minLength", 100.0f, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    public static boolean isUseDPQS() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.useDPQS", "true");
    }

    public static boolean isDoStats() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.doStats", "false");
    }

    public static boolean isDoMonitors() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.doMonitors", "false");
    }

    public static boolean isDoChecks() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.doChecks", "false");
    }

    public static boolean isSkipRenderer() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.skip_rdr", "false");
    }

    public static boolean isSkipRenderTiles() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.skip_pipe", "false");
    }

    public static boolean isLoggingEnabled() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.log", "false");
    }

    public static boolean isUseLogger() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.useLogger", "false");
    }

    public static boolean isLogCreateContext() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.logCreateContext", "false");
    }

    public static boolean isLogUnsafeMalloc() {
        return MarlinProperties.getBoolean("sun.java2d.renderer.logUnsafeMalloc", "false");
    }

    public static float getCurveLengthError() {
        return MarlinProperties.getFloat("sun.java2d.renderer.curve_len_err", 0.01f, 1.0E-6f, 1.0f);
    }

    public static float getCubicDecD2() {
        float def = MarlinProperties.isProfileQuality() ? 1.0f : 2.5f;
        return MarlinProperties.getFloat("sun.java2d.renderer.cubic_dec_d2", def, 1.0E-5f, 4.0f);
    }

    public static float getCubicIncD1() {
        float def = MarlinProperties.isProfileQuality() ? 0.2f : 0.5f;
        return MarlinProperties.getFloat("sun.java2d.renderer.cubic_inc_d1", def, 1.0E-6f, 1.0f);
    }

    public static float getQuadDecD2() {
        float def = MarlinProperties.isProfileQuality() ? 0.5f : 1.0f;
        return MarlinProperties.getFloat("sun.java2d.renderer.quad_dec_d2", def, 1.0E-5f, 4.0f);
    }

    static String getString(String key, String def) {
        return AccessController.doPrivileged(new GetPropertyAction(key, def));
    }

    static boolean getBoolean(String key, String def) {
        return Boolean.parseBoolean(AccessController.doPrivileged(new GetPropertyAction(key, def)));
    }

    static int getInteger(String key, int def, int min, int max) {
        String property = AccessController.doPrivileged(new GetPropertyAction(key));
        int value = def;
        if (property != null) {
            try {
                value = Integer.decode(property);
            }
            catch (NumberFormatException e) {
                MarlinUtils.logInfo("Invalid integer value for " + key + " = " + property);
            }
        }
        if (value < min || value > max) {
            MarlinUtils.logInfo("Invalid value for " + key + " = " + value + "; expected value in range[" + min + ", " + max + "] !");
            value = def;
        }
        return value;
    }

    static int align(int val, int norm) {
        int ceil = FloatMath.ceil_int((double)val / (double)norm);
        return ceil * norm;
    }

    public static double getDouble(String key, double def, double min, double max) {
        double value = def;
        String property = AccessController.doPrivileged(new GetPropertyAction(key));
        if (property != null) {
            try {
                value = Double.parseDouble(property);
            }
            catch (NumberFormatException nfe) {
                MarlinUtils.logInfo("Invalid value for " + key + " = " + property + " !");
            }
        }
        if (value < min || value > max) {
            MarlinUtils.logInfo("Invalid value for " + key + " = " + value + "; expect value in range[" + min + ", " + max + "] !");
            value = def;
        }
        return value;
    }

    public static float getFloat(String key, float def, float min, float max) {
        return (float)MarlinProperties.getDouble(key, def, min, max);
    }
}

