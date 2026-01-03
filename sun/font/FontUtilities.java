/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.plaf.FontUIResource;
import sun.awt.OSInfo;
import sun.font.CompositeFont;
import sun.font.Font2D;
import sun.font.FontAccess;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.PhysicalFont;
import sun.font.SunFontManager;
import sun.font.TrueTypeFont;
import sun.util.logging.PlatformLogger;

public final class FontUtilities {
    public static boolean isLinux;
    public static boolean isMacOSX;
    public static boolean isMacOSX14;
    public static boolean useJDKScaler;
    public static boolean isWindows;
    private static boolean debugFonts;
    private static PlatformLogger logger;
    private static boolean logging;
    public static final int MIN_LAYOUT_CHARCODE = 768;
    public static final int MAX_LAYOUT_CHARCODE = 8303;
    private static volatile SoftReference<ConcurrentHashMap<PhysicalFont, CompositeFont>> compMapRef;
    private static final String[][] nameMap;

    private static void initStatic() {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                String scalerStr;
                isLinux = OSInfo.getOSType() == OSInfo.OSType.LINUX;
                boolean bl = isMacOSX = OSInfo.getOSType() == OSInfo.OSType.MACOSX;
                if (isMacOSX) {
                    isMacOSX14 = true;
                    String version = System.getProperty("os.version", "");
                    if (version.startsWith("10.")) {
                        int periodIndex = (version = version.substring(3)).indexOf(46);
                        if (periodIndex != -1) {
                            version = version.substring(0, periodIndex);
                        }
                        try {
                            int v = Integer.parseInt(version);
                            isMacOSX14 = v >= 14;
                        }
                        catch (NumberFormatException numberFormatException) {
                            // empty catch block
                        }
                    }
                }
                useJDKScaler = (scalerStr = System.getProperty("sun.java2d.font.scaler")) != null ? "jdk".equals(scalerStr) : false;
                isWindows = OSInfo.getOSType() == OSInfo.OSType.WINDOWS;
                String debugLevel = System.getProperty("sun.java2d.debugfonts");
                if (debugLevel != null && !debugLevel.equals("false")) {
                    debugFonts = true;
                    logger = PlatformLogger.getLogger("sun.java2d");
                    if (debugLevel.equals("warning")) {
                        logger.setLevel(PlatformLogger.Level.WARNING);
                    } else if (debugLevel.equals("severe")) {
                        logger.setLevel(PlatformLogger.Level.SEVERE);
                    }
                    logging = logger.isEnabled();
                }
                return null;
            }
        });
    }

    public static Font2D getFont2D(Font font) {
        return FontAccess.getFontAccess().getFont2D(font);
    }

    public static boolean isComplexScript(char[] chs, int start, int limit) {
        for (int i = start; i < limit; ++i) {
            if (chs[i] < '\u0300' || !FontUtilities.isComplexCharCode(chs[i])) continue;
            return true;
        }
        return false;
    }

    public static boolean isComplexText(char[] chs, int start, int limit) {
        for (int i = start; i < limit; ++i) {
            if (chs[i] < '\u0300' || !FontUtilities.isNonSimpleChar(chs[i])) continue;
            return true;
        }
        return false;
    }

    public static boolean isNonSimpleChar(char ch) {
        return FontUtilities.isComplexCharCode(ch) || ch >= '\ud800' && ch <= '\udfff';
    }

    public static boolean isComplexCharCode(int code) {
        if (code < 768 || code > 8303) {
            return false;
        }
        if (code <= 879) {
            return true;
        }
        if (code < 1424) {
            return false;
        }
        if (code <= 1791) {
            return true;
        }
        if (code < 2304) {
            return false;
        }
        if (code <= 3711) {
            return true;
        }
        if (code < 3840) {
            return false;
        }
        if (code <= 4095) {
            return true;
        }
        if (code < 4256) {
            return true;
        }
        if (code < 4352) {
            return false;
        }
        if (code < 4607) {
            return true;
        }
        if (code < 6016) {
            return false;
        }
        if (code <= 6143) {
            return true;
        }
        if (code < 8204) {
            return false;
        }
        if (code <= 8205) {
            return true;
        }
        if (code >= 8234 && code <= 8238) {
            return true;
        }
        return code >= 8298 && code <= 8303;
    }

    public static PlatformLogger getLogger() {
        return logger;
    }

    public static boolean isLogging() {
        return logging;
    }

    public static boolean debugFonts() {
        return debugFonts;
    }

    public static void logWarning(String s) {
        FontUtilities.getLogger().warning(s);
    }

    public static void logInfo(String s) {
        FontUtilities.getLogger().info(s);
    }

    public static void logSevere(String s) {
        FontUtilities.getLogger().severe(s);
    }

    public static boolean fontSupportsDefaultEncoding(Font font) {
        return FontUtilities.getFont2D(font) instanceof CompositeFont;
    }

    public static FontUIResource getCompositeFontUIResource(Font font) {
        CompositeFont compFont;
        FontUIResource fuir = new FontUIResource(font);
        Font2D font2D = FontUtilities.getFont2D(font);
        if (!(font2D instanceof PhysicalFont)) {
            return fuir;
        }
        FontManager fm = FontManagerFactory.getInstance();
        Font2D dialog = fm.findFont2D("dialog", font.getStyle(), 0);
        if (!(dialog instanceof CompositeFont)) {
            return fuir;
        }
        CompositeFont dialog2D = (CompositeFont)dialog;
        PhysicalFont physicalFont = (PhysicalFont)font2D;
        ConcurrentHashMap<PhysicalFont, CompositeFont> compMap = compMapRef.get();
        if (compMap == null) {
            compMap = new ConcurrentHashMap();
            compMapRef = new SoftReference<ConcurrentHashMap<PhysicalFont, CompositeFont>>(compMap);
        }
        if ((compFont = compMap.get(physicalFont)) == null) {
            compFont = new CompositeFont(physicalFont, dialog2D);
            compMap.put(physicalFont, compFont);
        }
        FontAccess.getFontAccess().setFont2D(fuir, compFont.handle);
        FontAccess.getFontAccess().setCreatedFont(fuir);
        return fuir;
    }

    public static String mapFcName(String name) {
        for (int i = 0; i < nameMap.length; ++i) {
            if (!name.equals(nameMap[i][0])) continue;
            return nameMap[i][1];
        }
        return null;
    }

    public static FontUIResource getFontConfigFUIR(String fcFamily, int style, int size) {
        FontUIResource fuir;
        FontManager fm;
        String mapped = FontUtilities.mapFcName(fcFamily);
        if (mapped == null) {
            mapped = "sansserif";
        }
        if ((fm = FontManagerFactory.getInstance()) instanceof SunFontManager) {
            SunFontManager sfm = (SunFontManager)fm;
            fuir = sfm.getFontConfigFUIR(mapped, style, size);
        } else {
            fuir = new FontUIResource(mapped, style, size);
        }
        return fuir;
    }

    public static boolean textLayoutIsCompatible(Font font) {
        Font2D font2D = FontUtilities.getFont2D(font);
        if (font2D instanceof TrueTypeFont) {
            TrueTypeFont ttf = (TrueTypeFont)font2D;
            return ttf.getDirectoryEntry(1196643650) == null || ttf.getDirectoryEntry(1196445523) != null;
        }
        return false;
    }

    static {
        debugFonts = false;
        logger = null;
        FontUtilities.initStatic();
        compMapRef = new SoftReference<Object>(null);
        nameMap = new String[][]{{"sans", "sansserif"}, {"sans-serif", "sansserif"}, {"serif", "serif"}, {"monospace", "monospaced"}};
    }
}

