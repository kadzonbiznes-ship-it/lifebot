/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import sun.awt.FontConfiguration;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.windows.WFontConfiguration;
import sun.font.SunFontManager;
import sun.font.TrueTypeFont;

public final class Win32FontManager
extends SunFontManager {
    private static final TrueTypeFont eudcFont = AccessController.doPrivileged(new PrivilegedAction<TrueTypeFont>(){

        @Override
        public TrueTypeFont run() {
            String eudcFile = Win32FontManager.getEUDCFontFile();
            if (eudcFile != null) {
                try {
                    return new TrueTypeFont(eudcFile, null, 0, true, false);
                }
                catch (FontFormatException fontFormatException) {
                    // empty catch block
                }
            }
            return null;
        }
    });
    static String fontsForPrinting = null;

    private static native String getEUDCFontFile();

    @Override
    public TrueTypeFont getEUDCFont() {
        return eudcFont;
    }

    public Win32FontManager() {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                Win32FontManager.this.registerJREFontsWithPlatform(SunFontManager.jreFontDirName);
                return null;
            }
        });
    }

    @Override
    protected boolean useAbsoluteFontFileNames() {
        return false;
    }

    @Override
    protected void registerFontFile(String fontFileName, String[] nativeNames, int fontRank, boolean defer) {
        int fontFormat;
        if (this.registeredFontFiles.contains(fontFileName)) {
            return;
        }
        this.registeredFontFiles.add(fontFileName);
        if (this.getTrueTypeFilter().accept(null, fontFileName)) {
            fontFormat = 0;
        } else if (this.getType1Filter().accept(null, fontFileName)) {
            fontFormat = 1;
        } else {
            return;
        }
        if (this.fontPath == null) {
            this.fontPath = this.getPlatformFontPath(noType1Font);
        }
        String tmpFontPath = jreFontDirName + File.pathSeparator + this.fontPath;
        StringTokenizer parser = new StringTokenizer(tmpFontPath, File.pathSeparator);
        boolean found = false;
        try {
            while (!found && parser.hasMoreTokens()) {
                String newPath = parser.nextToken();
                boolean isJREFont = newPath.equals(jreFontDirName);
                File theFile = new File(newPath, fontFileName);
                if (!theFile.canRead()) continue;
                found = true;
                String path = theFile.getAbsolutePath();
                if (defer) {
                    this.registerDeferredFont(fontFileName, path, nativeNames, fontFormat, isJREFont, fontRank);
                } else {
                    this.registerFontFile(path, nativeNames, fontFormat, isJREFont, fontRank);
                }
                break;
            }
        }
        catch (NoSuchElementException e) {
            System.err.println(e);
        }
        if (!found) {
            this.addToMissingFontFileList(fontFileName);
        }
    }

    @Override
    protected FontConfiguration createFontConfiguration() {
        WFontConfiguration fc = new WFontConfiguration(this);
        fc.init();
        return fc;
    }

    @Override
    public FontConfiguration createFontConfiguration(boolean preferLocaleFonts, boolean preferPropFonts) {
        return new WFontConfiguration(this, preferLocaleFonts, preferPropFonts);
    }

    @Override
    protected void populateFontFileNameMap(HashMap<String, String> fontToFileMap, HashMap<String, String> fontToFamilyNameMap, HashMap<String, ArrayList<String>> familyToFontListMap, Locale locale) {
        Win32FontManager.populateFontFileNameMap0(fontToFileMap, fontToFamilyNameMap, familyToFontListMap, locale);
    }

    private static native void populateFontFileNameMap0(HashMap<String, String> var0, HashMap<String, String> var1, HashMap<String, ArrayList<String>> var2, Locale var3);

    @Override
    protected synchronized native String getFontPath(boolean var1);

    @Override
    protected String[] getDefaultPlatformFont() {
        String[] info = new String[]{"Arial", "c:\\windows\\fonts"};
        final String[] dirs = this.getPlatformFontDirs(true);
        if (dirs.length > 1) {
            String dir = (String)AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    for (int i = 0; i < dirs.length; ++i) {
                        String path = dirs[i] + File.separator + "arial.ttf";
                        File file = new File(path);
                        if (!file.exists()) continue;
                        return dirs[i];
                    }
                    return null;
                }
            });
            if (dir != null) {
                info[1] = dir;
            }
        } else {
            info[1] = dirs[0];
        }
        info[1] = info[1] + File.separator + "arial.ttf";
        return info;
    }

    protected void registerJREFontsWithPlatform(String pathName) {
        fontsForPrinting = pathName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registerJREFontsForPrinting() {
        Class<Win32GraphicsEnvironment> clazz = Win32GraphicsEnvironment.class;
        synchronized (Win32GraphicsEnvironment.class) {
            GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (fontsForPrinting == null) {
                // ** MonitorExit[var1] (shouldn't be in output)
                return;
            }
            final String pathName = fontsForPrinting;
            fontsForPrinting = null;
            // ** MonitorExit[var1] (shouldn't be in output)
            AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    File f1 = new File(pathName);
                    String[] ls = f1.list(SunFontManager.getInstance().getTrueTypeFilter());
                    if (ls == null) {
                        return null;
                    }
                    for (int i = 0; i < ls.length; ++i) {
                        File fontFile = new File(f1, ls[i]);
                        Win32FontManager.registerFontWithPlatform(fontFile.getAbsolutePath());
                    }
                    return null;
                }
            });
            return;
        }
    }

    private static native void registerFontWithPlatform(String var0);

    private static native void deRegisterFontWithPlatform(String var0);

    @Override
    public HashMap<String, SunFontManager.FamilyDescription> populateHardcodedFileNameMap() {
        HashMap<String, SunFontManager.FamilyDescription> platformFontMap = new HashMap<String, SunFontManager.FamilyDescription>();
        SunFontManager.FamilyDescription fd = new SunFontManager.FamilyDescription();
        fd.familyName = "Segoe UI";
        fd.plainFullName = "Segoe UI";
        fd.plainFileName = "segoeui.ttf";
        fd.boldFullName = "Segoe UI Bold";
        fd.boldFileName = "segoeuib.ttf";
        fd.italicFullName = "Segoe UI Italic";
        fd.italicFileName = "segoeuii.ttf";
        fd.boldItalicFullName = "Segoe UI Bold Italic";
        fd.boldItalicFileName = "segoeuiz.ttf";
        platformFontMap.put("segoe", fd);
        fd = new SunFontManager.FamilyDescription();
        fd.familyName = "Tahoma";
        fd.plainFullName = "Tahoma";
        fd.plainFileName = "tahoma.ttf";
        fd.boldFullName = "Tahoma Bold";
        fd.boldFileName = "tahomabd.ttf";
        platformFontMap.put("tahoma", fd);
        fd = new SunFontManager.FamilyDescription();
        fd.familyName = "Verdana";
        fd.plainFullName = "Verdana";
        fd.plainFileName = "verdana.TTF";
        fd.boldFullName = "Verdana Bold";
        fd.boldFileName = "verdanab.TTF";
        fd.italicFullName = "Verdana Italic";
        fd.italicFileName = "verdanai.TTF";
        fd.boldItalicFullName = "Verdana Bold Italic";
        fd.boldItalicFileName = "verdanaz.TTF";
        platformFontMap.put("verdana", fd);
        fd = new SunFontManager.FamilyDescription();
        fd.familyName = "Arial";
        fd.plainFullName = "Arial";
        fd.plainFileName = "ARIAL.TTF";
        fd.boldFullName = "Arial Bold";
        fd.boldFileName = "ARIALBD.TTF";
        fd.italicFullName = "Arial Italic";
        fd.italicFileName = "ARIALI.TTF";
        fd.boldItalicFullName = "Arial Bold Italic";
        fd.boldItalicFileName = "ARIALBI.TTF";
        platformFontMap.put("arial", fd);
        fd = new SunFontManager.FamilyDescription();
        fd.familyName = "Symbol";
        fd.plainFullName = "Symbol";
        fd.plainFileName = "Symbol.TTF";
        platformFontMap.put("symbol", fd);
        fd = new SunFontManager.FamilyDescription();
        fd.familyName = "WingDings";
        fd.plainFullName = "WingDings";
        fd.plainFileName = "WINGDING.TTF";
        platformFontMap.put("wingdings", fd);
        return platformFontMap;
    }
}

