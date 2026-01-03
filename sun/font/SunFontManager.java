/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.plaf.FontUIResource;
import sun.awt.FontConfiguration;
import sun.awt.SunToolkit;
import sun.awt.util.ThreadGroupUtils;
import sun.font.CompositeFont;
import sun.font.CompositeFontDescriptor;
import sun.font.CreatedFontTracker;
import sun.font.FileFont;
import sun.font.Font2D;
import sun.font.Font2DHandle;
import sun.font.FontAccess;
import sun.font.FontFamily;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontManagerForSGE;
import sun.font.FontManagerNativeLibrary;
import sun.font.FontUtilities;
import sun.font.NativeFont;
import sun.font.PhysicalFont;
import sun.font.StrikeCache;
import sun.font.TrueTypeFont;
import sun.font.Type1Font;
import sun.java2d.FontSupport;
import sun.util.logging.PlatformLogger;

public abstract class SunFontManager
implements FontSupport,
FontManagerForSGE {
    private static Font2DHandle FONT_HANDLE_NULL = new Font2DHandle(null);
    public static final int FONTFORMAT_NONE = -1;
    public static final int FONTFORMAT_TRUETYPE = 0;
    public static final int FONTFORMAT_TYPE1 = 1;
    public static final int FONTFORMAT_TTC = 2;
    public static final int FONTFORMAT_COMPOSITE = 3;
    public static final int FONTFORMAT_NATIVE = 4;
    protected static final int CHANNELPOOLSIZE = 20;
    protected FileFont[] fontFileCache = new FileFont[20];
    private int lastPoolIndex = 0;
    private int maxCompFont = 0;
    private CompositeFont[] compFonts = new CompositeFont[20];
    private ConcurrentHashMap<String, CompositeFont> compositeFonts = new ConcurrentHashMap();
    private ConcurrentHashMap<String, PhysicalFont> physicalFonts = new ConcurrentHashMap();
    private ConcurrentHashMap<String, PhysicalFont> registeredFonts = new ConcurrentHashMap();
    protected ConcurrentHashMap<String, Font2D> fullNameToFont = new ConcurrentHashMap();
    private HashMap<String, TrueTypeFont> localeFullNamesToFont;
    private PhysicalFont defaultPhysicalFont;
    static boolean longAddresses;
    private boolean loaded1dot0Fonts = false;
    boolean loadedAllFonts = false;
    boolean loadedAllFontFiles = false;
    String[] jreOtherFontFiles;
    boolean noOtherJREFontFiles = false;
    public static String jreLibDirName;
    public static String jreFontDirName;
    private static HashSet<String> missingFontFiles;
    private String defaultFontName;
    private String defaultFontFileName;
    protected HashSet<String> registeredFontFiles = new HashSet();
    private ArrayList<String> badFonts;
    protected String fontPath;
    private FontConfiguration fontConfig;
    private boolean discoveredAllFonts = false;
    private static final FilenameFilter ttFilter;
    private static final FilenameFilter t1Filter;
    private Font[] allFonts;
    private String[] allFamilies;
    private Locale lastDefaultLocale;
    public static boolean noType1Font;
    private static String[] STR_ARRAY;
    private static int maxSoftRefCnt;
    private final ConcurrentHashMap<String, FontRegistrationInfo> deferredFontFiles = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, Font2DHandle> initialisedFonts = new ConcurrentHashMap();
    private HashMap<String, String> fontToFileMap = null;
    private HashMap<String, String> fontToFamilyNameMap = null;
    private HashMap<String, ArrayList<String>> familyToFontListMap = null;
    private String[] pathDirs = null;
    private boolean haveCheckedUnreferencedFontFiles;
    static volatile HashMap<String, FamilyDescription> platformFontMap;
    private ConcurrentHashMap<String, Font2D> fontNameCache = new ConcurrentHashMap();
    Thread fileCloser = null;
    Vector<File> tmpFontFiles = null;
    private int createdFontCount = 0;
    private boolean _usingAlternateComposites = false;
    private static boolean gAltJAFont;
    private boolean gLocalePref = false;
    private boolean gPropPref = false;
    private static HashSet<String> installedNames;
    private static final Object regFamilyLock;
    private Hashtable<String, FontFamily> createdByFamilyName;
    private Hashtable<String, Font2D> createdByFullName;
    private boolean fontsAreRegistered = false;
    private static Locale systemLocale;

    public static SunFontManager getInstance() {
        FontManager fm = FontManagerFactory.getInstance();
        return (SunFontManager)fm;
    }

    public FilenameFilter getTrueTypeFilter() {
        return ttFilter;
    }

    public FilenameFilter getType1Filter() {
        return t1Filter;
    }

    private static void initStatic() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                FontManagerNativeLibrary.load();
                SunFontManager.initIDs();
                switch (StrikeCache.nativeAddressSize) {
                    case 8: {
                        longAddresses = true;
                        break;
                    }
                    case 4: {
                        longAddresses = false;
                        break;
                    }
                    default: {
                        throw new RuntimeException("Unexpected address size");
                    }
                }
                noType1Font = "true".equals(System.getProperty("sun.java2d.noType1Font"));
                jreLibDirName = System.getProperty("java.home", "") + File.separator + "lib";
                jreFontDirName = jreLibDirName + File.separator + "fonts";
                maxSoftRefCnt = Integer.getInteger("sun.java2d.font.maxSoftRefs", 10);
                return null;
            }
        });
    }

    public static final String getJDKFontDir() {
        return jreFontDirName;
    }

    public TrueTypeFont getEUDCFont() {
        return null;
    }

    private static native void initIDs();

    protected SunFontManager() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                File badFontFile = new File(jreFontDirName + File.separator + "badfonts.txt");
                if (badFontFile.exists()) {
                    SunFontManager.this.badFonts = new ArrayList();
                    try (FileInputStream fis2 = new FileInputStream(badFontFile);
                         BufferedReader br = new BufferedReader(new InputStreamReader(fis2));){
                        String name;
                        while ((name = br.readLine()) != null) {
                            if (FontUtilities.debugFonts()) {
                                FontUtilities.logWarning("read bad font: " + name);
                            }
                            SunFontManager.this.badFonts.add(name);
                        }
                    }
                    catch (IOException fis2) {
                        // empty catch block
                    }
                }
                if (FontUtilities.isLinux) {
                    SunFontManager.this.registerFontDir(jreFontDirName);
                }
                SunFontManager.this.registerFontsInDir(jreFontDirName, true, 2, true, false);
                SunFontManager.this.fontConfig = SunFontManager.this.createFontConfiguration();
                String[] fontInfo = SunFontManager.this.getDefaultPlatformFont();
                SunFontManager.this.defaultFontName = fontInfo[0];
                if (SunFontManager.this.defaultFontName == null && FontUtilities.debugFonts()) {
                    FontUtilities.logWarning("defaultFontName is null");
                }
                SunFontManager.this.defaultFontFileName = fontInfo[1];
                String extraFontPath = SunFontManager.this.fontConfig.getExtraFontPath();
                boolean prependToPath = false;
                boolean appendToPath = false;
                String dbgFontPath = System.getProperty("sun.java2d.fontpath");
                if (dbgFontPath != null) {
                    if (dbgFontPath.startsWith("prepend:")) {
                        prependToPath = true;
                        dbgFontPath = dbgFontPath.substring("prepend:".length());
                    } else if (dbgFontPath.startsWith("append:")) {
                        appendToPath = true;
                        dbgFontPath = dbgFontPath.substring("append:".length());
                    }
                }
                if (FontUtilities.debugFonts()) {
                    FontUtilities.logInfo("JRE font directory: " + jreFontDirName);
                    FontUtilities.logInfo("Extra font path: " + extraFontPath);
                    FontUtilities.logInfo("Debug font path: " + dbgFontPath);
                }
                if (dbgFontPath != null) {
                    SunFontManager.this.fontPath = SunFontManager.this.getPlatformFontPath(noType1Font);
                    if (extraFontPath != null) {
                        SunFontManager.this.fontPath = extraFontPath + File.pathSeparator + SunFontManager.this.fontPath;
                    }
                    SunFontManager.this.fontPath = appendToPath ? SunFontManager.this.fontPath + File.pathSeparator + dbgFontPath : (prependToPath ? dbgFontPath + File.pathSeparator + SunFontManager.this.fontPath : dbgFontPath);
                    SunFontManager.this.registerFontDirs(SunFontManager.this.fontPath);
                } else if (extraFontPath != null) {
                    SunFontManager.this.registerFontDirs(extraFontPath);
                }
                SunFontManager.this.initCompositeFonts(SunFontManager.this.fontConfig, null);
                return null;
            }
        });
    }

    @Override
    public Font2DHandle getNewComposite(String family, int style, Font2DHandle handle) {
        Font2D newFont;
        if (!(handle.font2D instanceof CompositeFont)) {
            return handle;
        }
        CompositeFont oldComp = (CompositeFont)handle.font2D;
        PhysicalFont oldFont = oldComp.getSlotFont(0);
        if (family == null) {
            family = oldFont.getFamilyName(null);
        }
        if (style == -1) {
            style = oldComp.getStyle();
        }
        if (!((newFont = this.findFont2D(family, style, 0)) instanceof PhysicalFont)) {
            newFont = oldFont;
        }
        PhysicalFont physicalFont = (PhysicalFont)newFont;
        CompositeFont dialog2D = (CompositeFont)this.findFont2D("dialog", style, 0);
        if (dialog2D == null) {
            return handle;
        }
        CompositeFont compFont = new CompositeFont(physicalFont, dialog2D);
        Font2DHandle newHandle = new Font2DHandle(compFont);
        return newHandle;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void registerCompositeFont(String compositeName, String[] componentFileNames, String[] componentNames, int numMetricsSlots, int[] exclusionRanges, int[] exclusionMaxIndex, boolean defer) {
        CompositeFont cf = new CompositeFont(compositeName, componentFileNames, componentNames, numMetricsSlots, exclusionRanges, exclusionMaxIndex, defer, this);
        this.addCompositeToFontList(cf, 2);
        CompositeFont[] compositeFontArray = this.compFonts;
        synchronized (this.compFonts) {
            this.compFonts[this.maxCompFont++] = cf;
            // ** MonitorExit[var9_9] (shouldn't be in output)
            return;
        }
    }

    protected static void registerCompositeFont(String compositeName, String[] componentFileNames, String[] componentNames, int numMetricsSlots, int[] exclusionRanges, int[] exclusionMaxIndex, boolean defer, ConcurrentHashMap<String, Font2D> altNameCache) {
        CompositeFont cf = new CompositeFont(compositeName, componentFileNames, componentNames, numMetricsSlots, exclusionRanges, exclusionMaxIndex, defer, SunFontManager.getInstance());
        Font2D oldFont = altNameCache.get(compositeName.toLowerCase(Locale.ENGLISH));
        if (oldFont instanceof CompositeFont) {
            oldFont.handle.font2D = cf;
        }
        altNameCache.put(compositeName.toLowerCase(Locale.ENGLISH), cf);
    }

    private void addCompositeToFontList(CompositeFont f, int rank) {
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Add to Family " + f.familyName + ", Font " + f.fullName + " rank=" + rank);
        }
        f.setRank(rank);
        this.compositeFonts.put(f.fullName, f);
        this.fullNameToFont.put(f.fullName.toLowerCase(Locale.ENGLISH), f);
        FontFamily family = FontFamily.getFamily(f.familyName);
        if (family == null) {
            family = new FontFamily(f.familyName, true, rank);
        }
        family.setFont(f, f.style);
    }

    protected PhysicalFont addToFontList(PhysicalFont f, int rank) {
        String fontName = f.fullName;
        String familyName = f.familyName;
        if (fontName == null || fontName.isEmpty()) {
            return null;
        }
        if (this.compositeFonts.containsKey(fontName)) {
            return null;
        }
        f.setRank(rank);
        if (!this.physicalFonts.containsKey(fontName)) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Add to Family " + familyName + ", Font " + fontName + " rank=" + rank);
            }
            this.physicalFonts.put(fontName, f);
            FontFamily family = FontFamily.getFamily(familyName);
            if (family == null) {
                family = new FontFamily(familyName, false, rank);
                family.setFont(f, f.style);
            } else {
                family.setFont(f, f.style);
            }
            this.fullNameToFont.put(fontName.toLowerCase(Locale.ENGLISH), f);
            return f;
        }
        PhysicalFont newFont = f;
        PhysicalFont oldFont = this.physicalFonts.get(fontName);
        if (oldFont == null) {
            return null;
        }
        if (oldFont.getRank() >= rank) {
            if (oldFont.mapper != null && rank > 2) {
                return oldFont;
            }
            if (oldFont.getRank() == rank) {
                if (oldFont instanceof TrueTypeFont && newFont instanceof TrueTypeFont) {
                    TrueTypeFont oldTTFont = (TrueTypeFont)oldFont;
                    TrueTypeFont newTTFont = (TrueTypeFont)newFont;
                    if (oldTTFont.fileSize >= newTTFont.fileSize) {
                        return oldFont;
                    }
                } else {
                    return oldFont;
                }
            }
            if (oldFont.platName.startsWith(jreFontDirName)) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.logWarning("Unexpected attempt to replace a JRE  font " + fontName + " from " + oldFont.platName + " with " + newFont.platName);
                }
                return oldFont;
            }
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Replace in Family " + familyName + ",Font " + fontName + " new rank=" + rank + " from " + oldFont.platName + " with " + newFont.platName);
            }
            this.replaceFont(oldFont, newFont);
            this.physicalFonts.put(fontName, newFont);
            this.fullNameToFont.put(fontName.toLowerCase(Locale.ENGLISH), newFont);
            FontFamily family = FontFamily.getFamily(familyName);
            if (family == null) {
                family = new FontFamily(familyName, false, rank);
                family.setFont(newFont, newFont.style);
            } else {
                family.setFont(newFont, newFont.style);
            }
            return newFont;
        }
        return oldFont;
    }

    public Font2D[] getRegisteredFonts() {
        PhysicalFont[] physFonts = this.getPhysicalFonts();
        int mcf = this.maxCompFont;
        Font2D[] regFonts = new Font2D[physFonts.length + mcf];
        System.arraycopy(this.compFonts, 0, regFonts, 0, mcf);
        System.arraycopy(physFonts, 0, regFonts, mcf, physFonts.length);
        return regFonts;
    }

    protected PhysicalFont[] getPhysicalFonts() {
        return this.physicalFonts.values().toArray(new PhysicalFont[0]);
    }

    protected synchronized void initialiseDeferredFonts() {
        for (String fileName : this.deferredFontFiles.keySet()) {
            this.initialiseDeferredFont(fileName);
        }
    }

    protected synchronized void registerDeferredJREFonts(String jreDir) {
        for (FontRegistrationInfo info : this.deferredFontFiles.values()) {
            if (info.fontFilePath == null || !info.fontFilePath.startsWith(jreDir)) continue;
            this.initialiseDeferredFont(info.fontFilePath);
        }
    }

    public boolean isDeferredFont(String fileName) {
        return this.deferredFontFiles.containsKey(fileName);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    PhysicalFont findJREDeferredFont(String name, int style) {
        if (this.noOtherJREFontFiles) {
            return null;
        }
        String string = jreFontDirName;
        synchronized (string) {
            if (this.jreOtherFontFiles == null) {
                HashSet<String> otherFontFiles = new HashSet<String>();
                for (String deferredFile : this.deferredFontFiles.keySet()) {
                    File file = new File(deferredFile);
                    String dir = file.getParent();
                    if (dir == null || !dir.equals(jreFontDirName)) continue;
                    otherFontFiles.add(deferredFile);
                }
                this.jreOtherFontFiles = otherFontFiles.toArray(STR_ARRAY);
                if (this.jreOtherFontFiles.length == 0) {
                    this.noOtherJREFontFiles = true;
                }
            }
            for (int i = 0; i < this.jreOtherFontFiles.length; ++i) {
                String fileName = this.jreOtherFontFiles[i];
                if (fileName == null) continue;
                this.jreOtherFontFiles[i] = null;
                PhysicalFont physicalFont = this.initialiseDeferredFont(fileName);
                if (physicalFont == null || !physicalFont.getFontName(null).equalsIgnoreCase(name) && !physicalFont.getFamilyName(null).equalsIgnoreCase(name) || physicalFont.style != style) continue;
                return physicalFont;
            }
        }
        return null;
    }

    private PhysicalFont findOtherDeferredFont(String name, int style) {
        for (String fileName : this.deferredFontFiles.keySet()) {
            PhysicalFont physicalFont = this.initialiseDeferredFont(fileName);
            if (physicalFont == null || !physicalFont.getFontName(null).equalsIgnoreCase(name) && !physicalFont.getFamilyName(null).equalsIgnoreCase(name) || physicalFont.style != style) continue;
            return physicalFont;
        }
        return null;
    }

    private PhysicalFont findDeferredFont(String name, int style) {
        PhysicalFont physicalFont = this.findJREDeferredFont(name, style);
        if (physicalFont != null) {
            return physicalFont;
        }
        return this.findOtherDeferredFont(name, style);
    }

    public void registerDeferredFont(String fileNameKey, String fullPathName, String[] nativeNames, int fontFormat, boolean useJavaRasterizer, int fontRank) {
        FontRegistrationInfo regInfo = new FontRegistrationInfo(fullPathName, nativeNames, fontFormat, useJavaRasterizer, fontRank);
        this.deferredFontFiles.put(fileNameKey, regInfo);
    }

    public synchronized PhysicalFont initialiseDeferredFont(String fileNameKey) {
        if (fileNameKey == null) {
            return null;
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Opening deferred font file " + fileNameKey);
        }
        PhysicalFont physicalFont = null;
        FontRegistrationInfo regInfo = this.deferredFontFiles.get(fileNameKey);
        if (regInfo != null) {
            this.deferredFontFiles.remove(fileNameKey);
            physicalFont = this.registerFontFile(regInfo.fontFilePath, regInfo.nativeNames, regInfo.fontFormat, regInfo.javaRasterizer, regInfo.fontRank);
            if (physicalFont != null) {
                this.initialisedFonts.put(fileNameKey, physicalFont.handle);
            } else {
                this.initialisedFonts.put(fileNameKey, FONT_HANDLE_NULL);
            }
        } else {
            Font2DHandle handle = this.initialisedFonts.get(fileNameKey);
            if (handle == null) {
                this.initialisedFonts.put(fileNameKey, FONT_HANDLE_NULL);
            } else {
                physicalFont = (PhysicalFont)handle.font2D;
            }
        }
        return physicalFont;
    }

    public boolean isRegisteredFontFile(String name) {
        return this.registeredFonts.containsKey(name);
    }

    public PhysicalFont getRegisteredFontFile(String name) {
        return this.registeredFonts.get(name);
    }

    public PhysicalFont registerFontFile(String fileName, String[] nativeNames, int fontFormat, boolean useJavaRasterizer, int fontRank) {
        PhysicalFont physicalFont;
        block11: {
            PhysicalFont regFont = this.registeredFonts.get(fileName);
            if (regFont != null) {
                return regFont;
            }
            physicalFont = null;
            try {
                switch (fontFormat) {
                    case 0: {
                        TrueTypeFont ttf;
                        int fn = 0;
                        do {
                            ttf = new TrueTypeFont(fileName, nativeNames, fn++, useJavaRasterizer);
                            PhysicalFont pf = this.addToFontList(ttf, fontRank);
                            if (physicalFont != null) continue;
                            physicalFont = pf;
                        } while (fn < ttf.getFontCount());
                        break;
                    }
                    case 1: {
                        Type1Font t1f = new Type1Font(fileName, nativeNames);
                        physicalFont = this.addToFontList(t1f, fontRank);
                        break;
                    }
                    case 4: {
                        NativeFont nf = new NativeFont(fileName, false);
                        physicalFont = this.addToFontList(nf, fontRank);
                        break;
                    }
                }
                if (FontUtilities.isLogging()) {
                    FontUtilities.logInfo("Registered file " + fileName + " as font " + String.valueOf(physicalFont) + " rank=" + fontRank);
                }
            }
            catch (FontFormatException ffe) {
                if (!FontUtilities.isLogging()) break block11;
                FontUtilities.logInfo("Unusable font: " + fileName + " " + ffe.toString());
            }
        }
        if (physicalFont != null && fontFormat != 4) {
            this.registeredFonts.put(fileName, physicalFont);
        }
        return physicalFont;
    }

    public void registerFonts(String[] fileNames, String[][] nativeNames, int fontCount, int fontFormat, boolean useJavaRasterizer, int fontRank, boolean defer) {
        for (int i = 0; i < fontCount; ++i) {
            if (defer) {
                this.registerDeferredFont(fileNames[i], fileNames[i], nativeNames[i], fontFormat, useJavaRasterizer, fontRank);
                continue;
            }
            this.registerFontFile(fileNames[i], nativeNames[i], fontFormat, useJavaRasterizer, fontRank);
        }
    }

    public PhysicalFont getDefaultPhysicalFont() {
        if (this.defaultPhysicalFont == null) {
            String defaultFontName = this.getDefaultFontFaceName();
            Font2D font2d = this.findFont2D(defaultFontName, 0, 0);
            if (font2d != null) {
                if (font2d instanceof PhysicalFont) {
                    this.defaultPhysicalFont = (PhysicalFont)font2d;
                } else if (FontUtilities.isLogging()) {
                    FontUtilities.logWarning("Font returned by findFont2D for default font name " + defaultFontName + " is not a physical font: " + font2d.getFontName(null));
                }
            }
            if (this.defaultPhysicalFont == null) {
                this.defaultPhysicalFont = this.physicalFonts.values().stream().findFirst().orElseThrow(() -> new Error("Probable fatal error: No physical fonts found."));
            }
        }
        return this.defaultPhysicalFont;
    }

    public Font2D getDefaultLogicalFont(int style) {
        return this.findFont2D("dialog", style, 0);
    }

    private static String dotStyleStr(int num) {
        switch (num) {
            case 1: {
                return ".bold";
            }
            case 2: {
                return ".italic";
            }
            case 3: {
                return ".bolditalic";
            }
        }
        return ".plain";
    }

    protected void populateFontFileNameMap(HashMap<String, String> fontToFileMap, HashMap<String, String> fontToFamilyNameMap, HashMap<String, ArrayList<String>> familyToFontListMap, Locale locale) {
    }

    private String[] getFontFilesFromPath(boolean noType1) {
        final FilenameFilter filter = noType1 ? ttFilter : new TTorT1Filter();
        return AccessController.doPrivileged(new PrivilegedAction<String[]>(){

            @Override
            public String[] run() {
                if (SunFontManager.this.pathDirs.length == 1) {
                    File dir = new File(SunFontManager.this.pathDirs[0]);
                    String[] files = dir.list(filter);
                    if (files == null) {
                        return new String[0];
                    }
                    for (int f = 0; f < files.length; ++f) {
                        files[f] = files[f].toLowerCase();
                    }
                    return files;
                }
                ArrayList<String> fileList = new ArrayList<String>();
                for (int i = 0; i < SunFontManager.this.pathDirs.length; ++i) {
                    File dir = new File(SunFontManager.this.pathDirs[i]);
                    String[] files = dir.list(filter);
                    if (files == null) continue;
                    for (int f = 0; f < files.length; ++f) {
                        fileList.add(files[f].toLowerCase());
                    }
                }
                return fileList.toArray(STR_ARRAY);
            }
        });
    }

    /*
     * WARNING - void declaration
     */
    private void resolveWindowsFonts() {
        ArrayList<String> unmappedFontNames = null;
        for (String font : this.fontToFamilyNameMap.keySet()) {
            Object file = this.fontToFileMap.get(font);
            if (file != null) continue;
            if (font.indexOf("  ") > 0) {
                String string = font.replaceFirst("  ", " ");
                file = this.fontToFileMap.get(string);
                if (file == null || this.fontToFamilyNameMap.containsKey(string)) continue;
                this.fontToFileMap.remove(string);
                this.fontToFileMap.put(font, (String)file);
                continue;
            }
            if (font.equals("marlett")) {
                this.fontToFileMap.put(font, "marlett.ttf");
                continue;
            }
            if (font.equals("david")) {
                file = this.fontToFileMap.get("david regular");
                if (file == null) continue;
                this.fontToFileMap.remove("david regular");
                this.fontToFileMap.put("david", (String)file);
                continue;
            }
            if (unmappedFontNames == null) {
                unmappedFontNames = new ArrayList<String>();
            }
            unmappedFontNames.add(font);
        }
        if (unmappedFontNames != null) {
            HashSet<String> unmappedFontFiles = new HashSet<String>();
            HashMap ffmapCopy = (HashMap)this.fontToFileMap.clone();
            for (String string : this.fontToFamilyNameMap.keySet()) {
                ffmapCopy.remove(string);
            }
            for (String string : ffmapCopy.keySet()) {
                unmappedFontFiles.add((String)ffmapCopy.get(string));
                this.fontToFileMap.remove(string);
            }
            this.resolveFontFiles(unmappedFontFiles, unmappedFontNames);
            if (unmappedFontNames.size() > 0) {
                ArrayList<String> registryFiles = new ArrayList<String>();
                for (String regFile : this.fontToFileMap.values()) {
                    registryFiles.add(regFile.toLowerCase());
                }
                for (String pathFile : this.getFontFilesFromPath(true)) {
                    if (registryFiles.contains(pathFile)) continue;
                    unmappedFontFiles.add(pathFile);
                }
                this.resolveFontFiles(unmappedFontFiles, unmappedFontNames);
            }
            if (unmappedFontNames.size() > 0) {
                void var5_15;
                int sz = unmappedFontNames.size();
                boolean bl = false;
                while (var5_15 < sz) {
                    ArrayList<String> family;
                    String name = unmappedFontNames.get((int)var5_15);
                    String familyName = this.fontToFamilyNameMap.get(name);
                    if (familyName != null && (family = this.familyToFontListMap.get(familyName)) != null && family.size() <= 1) {
                        this.familyToFontListMap.remove(familyName);
                    }
                    this.fontToFamilyNameMap.remove(name);
                    if (FontUtilities.isLogging()) {
                        FontUtilities.logInfo("No file for font:" + name);
                    }
                    ++var5_15;
                }
            }
        }
    }

    private synchronized void checkForUnreferencedFontFiles() {
        if (this.haveCheckedUnreferencedFontFiles) {
            return;
        }
        this.haveCheckedUnreferencedFontFiles = true;
        if (!FontUtilities.isWindows) {
            return;
        }
        ArrayList<String> registryFiles = new ArrayList<String>();
        for (String regFile : this.fontToFileMap.values()) {
            registryFiles.add(regFile.toLowerCase());
        }
        HashMap<String, String> fontToFileMap2 = null;
        HashMap<String, String> fontToFamilyNameMap2 = null;
        HashMap<String, ArrayList<String>> familyToFontListMap2 = null;
        for (String pathFile : this.getFontFilesFromPath(false)) {
            PhysicalFont f;
            if (registryFiles.contains(pathFile)) continue;
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Found non-registry file : " + pathFile);
            }
            if ((f = this.registerFontFile(this.getPathName(pathFile))) == null) continue;
            if (fontToFileMap2 == null) {
                fontToFileMap2 = new HashMap<String, String>(this.fontToFileMap);
                fontToFamilyNameMap2 = new HashMap<String, String>(this.fontToFamilyNameMap);
                familyToFontListMap2 = new HashMap<String, ArrayList<String>>(this.familyToFontListMap);
            }
            String fontName = f.getFontName(null);
            String family = f.getFamilyName(null);
            String familyLC = family.toLowerCase();
            fontToFamilyNameMap2.put(fontName, family);
            fontToFileMap2.put(fontName, pathFile);
            ArrayList<String> fonts = familyToFontListMap2.get(familyLC);
            fonts = fonts == null ? new ArrayList() : new ArrayList<String>(fonts);
            fonts.add(fontName);
            familyToFontListMap2.put(familyLC, fonts);
        }
        if (fontToFileMap2 != null) {
            this.fontToFileMap = fontToFileMap2;
            this.familyToFontListMap = familyToFontListMap2;
            this.fontToFamilyNameMap = fontToFamilyNameMap2;
        }
    }

    private void resolveFontFiles(HashSet<String> unmappedFiles, ArrayList<String> unmappedFonts) {
        Locale l = SunToolkit.getStartupLocale();
        for (String file : unmappedFiles) {
            try {
                TrueTypeFont ttf;
                int fn = 0;
                String fullPath = this.getPathName(file);
                if (FontUtilities.isLogging()) {
                    FontUtilities.logInfo("Trying to resolve file " + fullPath);
                }
                do {
                    String fontName;
                    if (!unmappedFonts.contains(fontName = (ttf = new TrueTypeFont(fullPath, null, fn++, false)).getFontName(l).toLowerCase())) continue;
                    this.fontToFileMap.put(fontName, file);
                    unmappedFonts.remove(fontName);
                    if (!FontUtilities.isLogging()) continue;
                    FontUtilities.logInfo("Resolved absent registry entry for " + fontName + " located in " + fullPath);
                } while (fn < ttf.getFontCount());
            }
            catch (Exception exception) {}
        }
    }

    public HashMap<String, FamilyDescription> populateHardcodedFileNameMap() {
        return new HashMap<String, FamilyDescription>(0);
    }

    Font2D findFontFromPlatformMap(String lcName, int style) {
        FamilyDescription fd;
        HashMap<String, FamilyDescription> platformFontMap = SunFontManager.platformFontMap;
        if (platformFontMap == null) {
            SunFontManager.platformFontMap = platformFontMap = this.populateHardcodedFileNameMap();
        }
        if (platformFontMap == null || platformFontMap.size() == 0) {
            return null;
        }
        int spaceIndex = lcName.indexOf(32);
        String firstWord = lcName;
        if (spaceIndex > 0) {
            firstWord = lcName.substring(0, spaceIndex);
        }
        if ((fd = platformFontMap.get(firstWord)) == null || fd.failed) {
            return null;
        }
        int styleIndex = -1;
        if (lcName.equalsIgnoreCase(fd.plainFullName)) {
            styleIndex = 0;
        } else if (lcName.equalsIgnoreCase(fd.boldFullName)) {
            styleIndex = 1;
        } else if (lcName.equalsIgnoreCase(fd.italicFullName)) {
            styleIndex = 2;
        } else if (lcName.equalsIgnoreCase(fd.boldItalicFullName)) {
            styleIndex = 3;
        }
        if (styleIndex == -1 && !lcName.equalsIgnoreCase(fd.familyName)) {
            return null;
        }
        String plainFile = null;
        String boldFile = null;
        String italicFile = null;
        String boldItalicFile = null;
        boolean failure = false;
        this.getPlatformFontDirs(noType1Font);
        if (fd.plainFileName != null && (plainFile = this.getPathName(fd.plainFileName)) == null) {
            failure = true;
        }
        if (fd.boldFileName != null && (boldFile = this.getPathName(fd.boldFileName)) == null) {
            failure = true;
        }
        if (fd.italicFileName != null && (italicFile = this.getPathName(fd.italicFileName)) == null) {
            failure = true;
        }
        if (fd.boldItalicFileName != null && (boldItalicFile = this.getPathName(fd.boldItalicFileName)) == null) {
            failure = true;
        }
        if (failure) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Hardcoded file missing looking for " + lcName);
            }
            fd.failed = true;
            return null;
        }
        final String[] files = new String[]{plainFile, boldFile, italicFile, boldItalicFile};
        failure = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                for (int i = 0; i < files.length; ++i) {
                    File f;
                    if (files[i] == null || (f = new File(files[i])).exists()) continue;
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
        if (failure) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Hardcoded file missing looking for " + lcName);
            }
            fd.failed = true;
            return null;
        }
        Font2D font = null;
        for (int f = 0; f < files.length; ++f) {
            if (files[f] == null) continue;
            PhysicalFont pf = this.registerFontFile(files[f], null, 0, false, 3);
            if (f != styleIndex) continue;
            font = pf;
        }
        FontFamily fontFamily = FontFamily.getFamily(fd.familyName);
        if (fontFamily != null) {
            if (font == null) {
                font = fontFamily.getFont(style);
                if (font == null) {
                    font = fontFamily.getClosestStyle(style);
                }
            } else if (style > 0 && style != font.style && (font = fontFamily.getFont(style |= font.style)) == null) {
                font = fontFamily.getClosestStyle(style);
            }
        }
        return font;
    }

    private synchronized HashMap<String, String> getFullNameToFileMap() {
        if (this.fontToFileMap == null) {
            this.pathDirs = this.getPlatformFontDirs(noType1Font);
            this.fontToFileMap = new HashMap(100);
            this.fontToFamilyNameMap = new HashMap(100);
            this.familyToFontListMap = new HashMap(50);
            this.populateFontFileNameMap(this.fontToFileMap, this.fontToFamilyNameMap, this.familyToFontListMap, Locale.ENGLISH);
            if (FontUtilities.isWindows) {
                this.resolveWindowsFonts();
            }
            if (FontUtilities.isLogging()) {
                this.logPlatformFontInfo();
            }
        }
        return this.fontToFileMap;
    }

    private void logPlatformFontInfo() {
        PlatformLogger logger = FontUtilities.getLogger();
        for (int i = 0; i < this.pathDirs.length; ++i) {
            logger.info("fontdir=" + this.pathDirs[i]);
        }
        for (String keyName : this.fontToFileMap.keySet()) {
            logger.info("font=" + keyName + " file=" + this.fontToFileMap.get(keyName));
        }
        for (String keyName : this.fontToFamilyNameMap.keySet()) {
            logger.info("font=" + keyName + " family=" + this.fontToFamilyNameMap.get(keyName));
        }
        for (String keyName : this.familyToFontListMap.keySet()) {
            logger.info("family=" + keyName + " fonts=" + String.valueOf(this.familyToFontListMap.get(keyName)));
        }
    }

    protected String[] getFontNamesFromPlatform() {
        if (this.getFullNameToFileMap().size() == 0) {
            return null;
        }
        this.checkForUnreferencedFontFiles();
        ArrayList<String> fontNames = new ArrayList<String>();
        for (ArrayList<String> a : this.familyToFontListMap.values()) {
            for (String s : a) {
                fontNames.add(s);
            }
        }
        return fontNames.toArray(STR_ARRAY);
    }

    public boolean gotFontsFromPlatform() {
        return this.getFullNameToFileMap().size() != 0;
    }

    public String getFileNameForFontName(String fontName) {
        String fontNameLC = fontName.toLowerCase(Locale.ENGLISH);
        return this.fontToFileMap.get(fontNameLC);
    }

    private PhysicalFont registerFontFile(String file) {
        if (new File(file).isAbsolute() && !this.registeredFonts.containsKey(file)) {
            int fontFormat = -1;
            int fontRank = 6;
            if (ttFilter.accept(null, file)) {
                fontFormat = 0;
                fontRank = 3;
            } else if (t1Filter.accept(null, file)) {
                fontFormat = 1;
                fontRank = 4;
            }
            if (fontFormat == -1) {
                return null;
            }
            return this.registerFontFile(file, null, fontFormat, false, fontRank);
        }
        return null;
    }

    protected void registerOtherFontFiles(HashSet<String> registeredFontFiles) {
        if (this.getFullNameToFileMap().size() == 0) {
            return;
        }
        for (String file : this.fontToFileMap.values()) {
            this.registerFontFile(file);
        }
    }

    public boolean getFamilyNamesFromPlatform(TreeMap<String, String> familyNames, Locale requestedLocale) {
        if (this.getFullNameToFileMap().size() == 0) {
            return false;
        }
        this.checkForUnreferencedFontFiles();
        for (String name : this.fontToFamilyNameMap.values()) {
            familyNames.put(name.toLowerCase(requestedLocale), name);
        }
        return true;
    }

    private String getPathName(final String s) {
        File f = new File(s);
        if (f.isAbsolute()) {
            return s;
        }
        if (this.pathDirs.length == 1) {
            return this.pathDirs[0] + File.separator + s;
        }
        String path = AccessController.doPrivileged(new PrivilegedAction<String>(){
            final /* synthetic */ SunFontManager this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public String run() {
                for (int p = 0; p < this.this$0.pathDirs.length; ++p) {
                    File f = new File(this.this$0.pathDirs[p] + File.separator + s);
                    if (!f.exists()) continue;
                    return f.getAbsolutePath();
                }
                return null;
            }
        });
        if (path != null) {
            return path;
        }
        return s;
    }

    private Font2D findFontFromPlatform(String lcName, int style) {
        if (this.getFullNameToFileMap().size() == 0) {
            return null;
        }
        ArrayList<String> family = null;
        String fontFile = null;
        String familyName = this.fontToFamilyNameMap.get(lcName);
        if (familyName != null) {
            fontFile = this.fontToFileMap.get(lcName);
            family = this.familyToFontListMap.get(familyName.toLowerCase(Locale.ENGLISH));
        } else {
            String lcFontName;
            family = this.familyToFontListMap.get(lcName);
            if (family != null && family.size() > 0 && (lcFontName = family.get(0).toLowerCase(Locale.ENGLISH)) != null) {
                familyName = this.fontToFamilyNameMap.get(lcFontName);
            }
        }
        if (family == null || familyName == null) {
            return null;
        }
        String[] fontList = family.toArray(STR_ARRAY);
        if (fontList.length == 0) {
            return null;
        }
        for (int f = 0; f < fontList.length; ++f) {
            String fontNameLC = fontList[f].toLowerCase(Locale.ENGLISH);
            String fileName = this.fontToFileMap.get(fontNameLC);
            if (fileName != null) continue;
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Platform lookup : No file for font " + fontList[f] + " in family " + familyName);
            }
            return null;
        }
        PhysicalFont physicalFont = null;
        if (fontFile != null) {
            physicalFont = this.registerFontFile(this.getPathName(fontFile), null, 0, false, 3);
        }
        for (int f = 0; f < fontList.length; ++f) {
            String fontNameLC = fontList[f].toLowerCase(Locale.ENGLISH);
            String fileName = this.fontToFileMap.get(fontNameLC);
            if (fontFile != null && fontFile.equals(fileName)) continue;
            this.registerFontFile(this.getPathName(fileName), null, 0, false, 3);
        }
        Font2D font = null;
        FontFamily fontFamily = FontFamily.getFamily(familyName);
        if (physicalFont != null) {
            style |= physicalFont.style;
        }
        if (fontFamily != null && (font = fontFamily.getFont(style)) == null) {
            font = fontFamily.getClosestStyle(style);
        }
        return font;
    }

    @Override
    public Font2D findFont2D(String name, int style, int fallback) {
        FontFamily family;
        if (name == null) {
            return null;
        }
        String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
        String mapName = lowerCaseName + SunFontManager.dotStyleStr(style);
        Font2D font = this.fontNameCache.get(mapName);
        if (font != null) {
            return font;
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Search for font: " + name);
        }
        if (FontUtilities.isWindows) {
            if (lowerCaseName.equals("ms sans serif")) {
                name = "sansserif";
            } else if (lowerCaseName.equals("ms serif")) {
                name = "serif";
            }
        }
        if (lowerCaseName.equals("default")) {
            name = "dialog";
        }
        if ((family = FontFamily.getFamily(name)) != null) {
            font = family.getFontWithExactStyleMatch(style);
            if (font == null) {
                font = this.findDeferredFont(name, style);
            }
            if (font == null) {
                font = this.findFontFromPlatform(lowerCaseName, style);
            }
            if (font == null) {
                font = family.getFont(style);
            }
            if (font == null) {
                font = family.getClosestStyle(style);
            }
            if (font != null) {
                this.fontNameCache.put(mapName, font);
                return font;
            }
        }
        if ((font = this.fullNameToFont.get(lowerCaseName)) != null) {
            if (font.style == style || style == 0) {
                this.fontNameCache.put(mapName, font);
                return font;
            }
            family = FontFamily.getFamily(font.getFamilyName(null));
            if (family != null) {
                Font2D familyFont = family.getFont(style | font.style);
                if (familyFont != null) {
                    this.fontNameCache.put(mapName, familyFont);
                    return familyFont;
                }
                familyFont = family.getClosestStyle(style | font.style);
                if (familyFont != null && familyFont.canDoStyle(style | font.style)) {
                    this.fontNameCache.put(mapName, familyFont);
                    return familyFont;
                }
            }
        }
        if (FontUtilities.isWindows) {
            font = this.findFontFromPlatformMap(lowerCaseName, style);
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("findFontFromPlatformMap returned " + String.valueOf(font));
            }
            if (font != null) {
                this.fontNameCache.put(mapName, font);
                return font;
            }
            if (this.deferredFontFiles.size() > 0 && (font = this.findJREDeferredFont(lowerCaseName, style)) != null) {
                this.fontNameCache.put(mapName, font);
                return font;
            }
            font = this.findFontFromPlatform(lowerCaseName, style);
            if (font != null) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.logInfo("Found font via platform API for request:\"" + name + "\":, style=" + style + " found font: " + String.valueOf(font));
                }
                this.fontNameCache.put(mapName, font);
                return font;
            }
        }
        if (this.deferredFontFiles.size() > 0 && (font = this.findDeferredFont(name, style)) != null) {
            this.fontNameCache.put(mapName, font);
            return font;
        }
        if (this.fontsAreRegistered) {
            Hashtable<String, FontFamily> familyTable = this.createdByFamilyName;
            Hashtable<String, Font2D> nameTable = this.createdByFullName;
            family = familyTable.get(lowerCaseName);
            if (family != null) {
                font = family.getFontWithExactStyleMatch(style);
                if (font == null) {
                    font = family.getFont(style);
                }
                if (font == null) {
                    font = family.getClosestStyle(style);
                }
                if (font != null) {
                    if (this.fontsAreRegistered) {
                        this.fontNameCache.put(mapName, font);
                    }
                    return font;
                }
            }
            if ((font = nameTable.get(lowerCaseName)) != null) {
                if (this.fontsAreRegistered) {
                    this.fontNameCache.put(mapName, font);
                }
                return font;
            }
        }
        if (!this.loadedAllFonts) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Load fonts looking for:" + name);
            }
            this.loadFonts();
            this.loadedAllFonts = true;
            return this.findFont2D(name, style, fallback);
        }
        if (!this.loadedAllFontFiles) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("Load font files looking for:" + name);
            }
            this.loadFontFiles();
            this.loadedAllFontFiles = true;
            return this.findFont2D(name, style, fallback);
        }
        font = this.findFont2DAllLocales(name, style);
        if (font != null) {
            this.fontNameCache.put(mapName, font);
            return font;
        }
        if (FontUtilities.isWindows) {
            String compatName = this.getFontConfiguration().getFallbackFamilyName(name, null);
            if (compatName != null) {
                font = this.findFont2D(compatName, style, fallback);
                this.fontNameCache.put(mapName, font);
                return font;
            }
        } else {
            if (lowerCaseName.equals("timesroman")) {
                font = this.findFont2D("serif", style, fallback);
                this.fontNameCache.put(mapName, font);
                return font;
            }
            if (lowerCaseName.equals("helvetica")) {
                font = this.findFont2D("sansserif", style, fallback);
                this.fontNameCache.put(mapName, font);
                return font;
            }
            if (lowerCaseName.equals("courier")) {
                font = this.findFont2D("monospaced", style, fallback);
                this.fontNameCache.put(mapName, font);
                return font;
            }
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("No font found for:" + name);
        }
        switch (fallback) {
            case 1: {
                return this.getDefaultPhysicalFont();
            }
            case 2: {
                return this.getDefaultLogicalFont(style);
            }
        }
        return null;
    }

    public int getNumFonts() {
        return this.physicalFonts.size() + this.maxCompFont;
    }

    private static boolean fontSupportsEncoding(Font font, String encoding) {
        return FontUtilities.getFont2D(font).supportsEncoding(encoding);
    }

    protected abstract String getFontPath(boolean var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Font2D[] createFont2D(File fontFile, int fontFormat, boolean all, boolean isCopy, CreatedFontTracker tracker) throws FontFormatException {
        ArrayList<Font2D> fList = new ArrayList<Font2D>();
        int cnt = 1;
        String fontFilePath = fontFile.getPath();
        FileFont font2D = null;
        final File fFile = fontFile;
        final CreatedFontTracker _tracker = tracker;
        boolean weakRefs = false;
        int maxStrikes = 0;
        SunFontManager sunFontManager = this;
        synchronized (sunFontManager) {
            if (this.createdFontCount < maxSoftRefCnt) {
                ++this.createdFontCount;
            } else {
                weakRefs = true;
                maxStrikes = 10;
            }
        }
        try {
            switch (fontFormat) {
                case 0: {
                    font2D = new TrueTypeFont(fontFilePath, null, 0, true);
                    font2D.setUseWeakRefs(weakRefs, maxStrikes);
                    fList.add(font2D);
                    if (!all) break;
                    cnt = ((TrueTypeFont)font2D).getFontCount();
                    int index = 1;
                    while (index < cnt) {
                        font2D = new TrueTypeFont(fontFilePath, null, index++, true);
                        font2D.setUseWeakRefs(weakRefs, maxStrikes);
                        fList.add(font2D);
                    }
                    break;
                }
                case 1: {
                    font2D = new Type1Font(fontFilePath, null, isCopy);
                    font2D.setUseWeakRefs(weakRefs, maxStrikes);
                    fList.add(font2D);
                    break;
                }
                default: {
                    throw new FontFormatException("Unrecognised Font Format");
                }
            }
        }
        catch (FontFormatException e) {
            if (!isCopy) throw e;
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    if (_tracker != null) {
                        _tracker.subBytes((int)fFile.length());
                    }
                    fFile.delete();
                    return null;
                }
            });
            throw e;
        }
        if (!isCopy) return fList.toArray(new Font2D[0]);
        FileFont.setFileToRemove(fList, fontFile, cnt, tracker);
        Class<FontManager> clazz = FontManager.class;
        synchronized (FontManager.class) {
            if (this.tmpFontFiles == null) {
                this.tmpFontFiles = new Vector();
            }
            this.tmpFontFiles.add(fontFile);
            if (this.fileCloser != null) return fList.toArray(new Font2D[0]);
            Runnable fileCloserRunnable = new Runnable(){

                @Override
                public void run() {
                    AccessController.doPrivileged(new PrivilegedAction<Void>(){

                        @Override
                        public Void run() {
                            for (int i = 0; i < 20; ++i) {
                                if (SunFontManager.this.fontFileCache[i] == null) continue;
                                try {
                                    SunFontManager.this.fontFileCache[i].close();
                                    continue;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            if (SunFontManager.this.tmpFontFiles != null) {
                                File[] files = new File[SunFontManager.this.tmpFontFiles.size()];
                                files = SunFontManager.this.tmpFontFiles.toArray(files);
                                for (int f = 0; f < files.length; ++f) {
                                    try {
                                        files[f].delete();
                                        continue;
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                }
                            }
                            return null;
                        }
                    });
                }
            };
            AccessController.doPrivileged(() -> {
                ThreadGroup rootTG = ThreadGroupUtils.getRootThreadGroup();
                this.fileCloser = new Thread(rootTG, fileCloserRunnable, "FileCloser", 0L, false);
                this.fileCloser.setContextClassLoader(null);
                Runtime.getRuntime().addShutdownHook(this.fileCloser);
                return null;
            });
            // ** MonitorExit[var14_17] (shouldn't be in output)
            return fList.toArray(new Font2D[0]);
        }
    }

    public synchronized String getFullNameByFileName(String fileName) {
        PhysicalFont[] physFonts = this.getPhysicalFonts();
        for (int i = 0; i < physFonts.length; ++i) {
            if (!physFonts[i].platName.equals(fileName)) continue;
            return physFonts[i].getFontName(null);
        }
        return null;
    }

    @Override
    public synchronized void deRegisterBadFont(Font2D font2D) {
        if (!(font2D instanceof PhysicalFont)) {
            return;
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.logSevere("Deregister bad font: " + String.valueOf(font2D));
        }
        this.replaceFont((PhysicalFont)font2D, this.getDefaultPhysicalFont());
    }

    public synchronized void replaceFont(PhysicalFont oldFont, PhysicalFont newFont) {
        int i;
        if (oldFont.handle.font2D != oldFont) {
            return;
        }
        if (oldFont == newFont) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logSevere("Can't replace bad font with itself " + String.valueOf(oldFont));
            }
            PhysicalFont[] physFonts = this.getPhysicalFonts();
            for (i = 0; i < physFonts.length; ++i) {
                if (physFonts[i] == newFont) continue;
                newFont = physFonts[i];
                break;
            }
            if (oldFont == newFont) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.logSevere("This is bad. No good physicalFonts found.");
                }
                return;
            }
        }
        oldFont.handle.font2D = newFont;
        this.physicalFonts.remove(oldFont.fullName);
        this.fullNameToFont.remove(oldFont.fullName.toLowerCase(Locale.ENGLISH));
        FontFamily.remove(oldFont);
        if (this.localeFullNamesToFont != null) {
            Map.Entry[] mapEntries = this.localeFullNamesToFont.entrySet().toArray(new Map.Entry[0]);
            for (i = 0; i < mapEntries.length; ++i) {
                if (mapEntries[i].getValue() != oldFont) continue;
                try {
                    Map.Entry tmp = mapEntries[i];
                    tmp.setValue(newFont);
                    continue;
                }
                catch (Exception e) {
                    this.localeFullNamesToFont.remove(mapEntries[i].getKey());
                }
            }
        }
        for (int i2 = 0; i2 < this.maxCompFont; ++i2) {
            if (newFont.getRank() <= 2) continue;
            this.compFonts[i2].replaceComponentFont(oldFont, newFont);
        }
    }

    private synchronized void loadLocaleNames() {
        if (this.localeFullNamesToFont != null) {
            return;
        }
        this.localeFullNamesToFont = new HashMap();
        Font2D[] fonts = this.getRegisteredFonts();
        for (int i = 0; i < fonts.length; ++i) {
            if (!(fonts[i] instanceof TrueTypeFont)) continue;
            TrueTypeFont ttf = (TrueTypeFont)fonts[i];
            String[] fullNames = ttf.getAllFullNames();
            for (int n = 0; n < fullNames.length; ++n) {
                this.localeFullNamesToFont.put(fullNames[n], ttf);
            }
            FontFamily family = FontFamily.getFamily(ttf.familyName);
            if (family == null) continue;
            FontFamily.addLocaleNames(family, ttf.getAllFamilyNames());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Font2D findFont2DAllLocales(String name, int style) {
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Searching localised font names for:" + name);
        }
        if (this.localeFullNamesToFont == null) {
            this.loadLocaleNames();
        }
        String lowerCaseName = name.toLowerCase();
        Font2D font = null;
        FontFamily family = FontFamily.getLocaleFamily(lowerCaseName);
        if (family != null) {
            font = family.getFont(style);
            if (font == null) {
                font = family.getClosestStyle(style);
            }
            if (font != null) {
                return font;
            }
        }
        SunFontManager sunFontManager = this;
        synchronized (sunFontManager) {
            font = this.localeFullNamesToFont.get(name);
        }
        if (font != null) {
            if (font.style == style || style == 0) {
                return font;
            }
            family = FontFamily.getFamily(font.getFamilyName(null));
            if (family != null) {
                Font2D familyFont = family.getFont(style);
                if (familyFont != null) {
                    return familyFont;
                }
                familyFont = family.getClosestStyle(style);
                if (familyFont != null) {
                    if (!familyFont.canDoStyle(style)) {
                        familyFont = null;
                    }
                    return familyFont;
                }
            }
        }
        return font;
    }

    public boolean usingAlternateCompositeFonts() {
        return this._usingAlternateComposites;
    }

    @Override
    public synchronized void useAlternateFontforJALocales() {
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Entered useAlternateFontforJALocales().");
        }
        if (!FontUtilities.isWindows) {
            return;
        }
        gAltJAFont = true;
    }

    public boolean usingAlternateFontforJALocales() {
        return gAltJAFont;
    }

    @Override
    public synchronized void preferLocaleFonts() {
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Entered preferLocaleFonts().");
        }
        if (!FontConfiguration.willReorderForStartupLocale()) {
            return;
        }
        if (this.gLocalePref) {
            return;
        }
        this.gLocalePref = true;
        this.createCompositeFonts(this.fontNameCache, this.gLocalePref, this.gPropPref);
        this._usingAlternateComposites = true;
    }

    @Override
    public synchronized void preferProportionalFonts() {
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Entered preferProportionalFonts().");
        }
        if (!FontConfiguration.hasMonoToPropMap()) {
            return;
        }
        if (this.gPropPref) {
            return;
        }
        this.gPropPref = true;
        this.createCompositeFonts(this.fontNameCache, this.gLocalePref, this.gPropPref);
        this._usingAlternateComposites = true;
    }

    private static HashSet<String> getInstalledNames() {
        if (installedNames == null) {
            int i;
            Locale l = SunFontManager.getSystemStartupLocale();
            SunFontManager fontManager = SunFontManager.getInstance();
            String[] installedFamilies = fontManager.getInstalledFontFamilyNames(l);
            Font[] installedFonts = fontManager.getAllInstalledFonts();
            HashSet<String> names = new HashSet<String>();
            for (i = 0; i < installedFamilies.length; ++i) {
                names.add(installedFamilies[i].toLowerCase(l));
            }
            for (i = 0; i < installedFonts.length; ++i) {
                names.add(installedFonts[i].getFontName(l).toLowerCase(l));
            }
            installedNames = names;
        }
        return installedNames;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean registerFont(Font font) {
        if (font == null) {
            return false;
        }
        Object object = regFamilyLock;
        synchronized (object) {
            if (this.createdByFamilyName == null) {
                this.createdByFamilyName = new Hashtable();
                this.createdByFullName = new Hashtable();
            }
        }
        if (!FontAccess.getFontAccess().isCreatedFont(font)) {
            return false;
        }
        HashSet<String> names = SunFontManager.getInstalledNames();
        Locale l = SunFontManager.getSystemStartupLocale();
        String familyName = font.getFamily(l).toLowerCase();
        String fullName = font.getFontName(l).toLowerCase();
        if (names.contains(familyName) || names.contains(fullName)) {
            return false;
        }
        Hashtable<String, FontFamily> familyTable = this.createdByFamilyName;
        Hashtable<String, Font2D> fullNameTable = this.createdByFullName;
        this.fontsAreRegistered = true;
        Font2D font2D = FontUtilities.getFont2D(font);
        int style = font2D.getStyle();
        FontFamily family = familyTable.get(familyName);
        if (family == null) {
            family = new FontFamily(font.getFamily(l));
            familyTable.put(familyName, family);
        }
        if (this.fontsAreRegistered) {
            this.removeFromCache(family.getFont(0));
            this.removeFromCache(family.getFont(1));
            this.removeFromCache(family.getFont(2));
            this.removeFromCache(family.getFont(3));
            this.removeFromCache(fullNameTable.get(fullName));
        }
        family.setFont(font2D, style);
        fullNameTable.put(fullName, font2D);
        return true;
    }

    private void removeFromCache(Font2D font) {
        if (font == null) {
            return;
        }
        String[] keys = ((ConcurrentHashMap.CollectionView)((Object)this.fontNameCache.keySet())).toArray(STR_ARRAY);
        for (int k = 0; k < keys.length; ++k) {
            if (this.fontNameCache.get(keys[k]) != font) continue;
            this.fontNameCache.remove(keys[k]);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public TreeMap<String, String> getCreatedFontFamilyNames() {
        if (!this.fontsAreRegistered) {
            return null;
        }
        Hashtable<String, FontFamily> familyTable = this.createdByFamilyName;
        Locale l = SunFontManager.getSystemStartupLocale();
        Hashtable<String, FontFamily> hashtable = familyTable;
        synchronized (hashtable) {
            TreeMap<String, String> map = new TreeMap<String, String>();
            for (FontFamily f : familyTable.values()) {
                Font2D font2D = f.getFont(0);
                if (font2D == null) {
                    font2D = f.getClosestStyle(0);
                }
                String name = font2D.getFamilyName(l);
                map.put(name.toLowerCase(l), name);
            }
            return map;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Font[] getCreatedFonts() {
        if (!this.fontsAreRegistered) {
            return null;
        }
        Hashtable<String, Font2D> nameTable = this.createdByFullName;
        Locale l = SunFontManager.getSystemStartupLocale();
        Hashtable<String, Font2D> hashtable = nameTable;
        synchronized (hashtable) {
            Font[] fonts = new Font[nameTable.size()];
            int i = 0;
            for (Font2D font2D : nameTable.values()) {
                fonts[i++] = new Font(font2D.getFontName(l), 0, 1);
            }
            return fonts;
        }
    }

    protected String[] getPlatformFontDirs(boolean noType1Fonts) {
        if (this.pathDirs != null) {
            return this.pathDirs;
        }
        String path = this.getPlatformFontPath(noType1Fonts);
        StringTokenizer parser = new StringTokenizer(path, File.pathSeparator);
        ArrayList<String> pathList = new ArrayList<String>();
        try {
            while (parser.hasMoreTokens()) {
                pathList.add(parser.nextToken());
            }
        }
        catch (NoSuchElementException noSuchElementException) {
            // empty catch block
        }
        this.pathDirs = pathList.toArray(new String[0]);
        return this.pathDirs;
    }

    protected abstract String[] getDefaultPlatformFont();

    private void addDirFonts(String dirName, File dirFile, FilenameFilter filter, int fontFormat, boolean useJavaRasterizer, int fontRank, boolean defer, boolean resolveSymLinks) {
        String[] ls = dirFile.list(filter);
        if (ls == null || ls.length == 0) {
            return;
        }
        String[] fontNames = new String[ls.length];
        String[][] nativeNames = new String[ls.length][];
        int fontCount = 0;
        for (int i = 0; i < ls.length; ++i) {
            File theFile = new File(dirFile, ls[i]);
            Object fullName = null;
            if (resolveSymLinks) {
                try {
                    fullName = theFile.getCanonicalPath();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
            if (fullName == null) {
                fullName = dirName + File.separator + ls[i];
            }
            if (this.registeredFontFiles.contains(fullName)) continue;
            if (this.badFonts != null && this.badFonts.contains(fullName)) {
                if (!FontUtilities.debugFonts()) continue;
                FontUtilities.logWarning("skip bad font " + (String)fullName);
                continue;
            }
            this.registeredFontFiles.add((String)fullName);
            if (FontUtilities.debugFonts() && FontUtilities.getLogger().isLoggable(PlatformLogger.Level.INFO)) {
                String message = "Registering font " + (String)fullName;
                String[] natNames = this.getNativeNames((String)fullName, null);
                if (natNames == null) {
                    message = message + " with no native name";
                } else {
                    message = message + " with native name(s) " + natNames[0];
                    for (int nn = 1; nn < natNames.length; ++nn) {
                        message = message + ", " + natNames[nn];
                    }
                }
                FontUtilities.logInfo(message);
            }
            fontNames[fontCount] = fullName;
            nativeNames[fontCount++] = this.getNativeNames((String)fullName, null);
        }
        this.registerFonts(fontNames, nativeNames, fontCount, fontFormat, useJavaRasterizer, fontRank, defer);
    }

    protected String[] getNativeNames(String fontFileName, String platformName) {
        return null;
    }

    protected String getFileNameFromPlatformName(String platformFontName) {
        return this.fontConfig.getFileNameFromPlatformName(platformFontName);
    }

    @Override
    public FontConfiguration getFontConfiguration() {
        return this.fontConfig;
    }

    public String getPlatformFontPath(boolean noType1Font) {
        if (this.fontPath == null) {
            this.fontPath = this.getFontPath(noType1Font);
        }
        return this.fontPath;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void loadFonts() {
        if (this.discoveredAllFonts) {
            return;
        }
        SunFontManager sunFontManager = this;
        synchronized (sunFontManager) {
            if (FontUtilities.debugFonts()) {
                Thread.dumpStack();
                FontUtilities.logInfo("SunGraphicsEnvironment.loadFonts() called");
            }
            this.initialiseDeferredFonts();
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    if (SunFontManager.this.fontPath == null) {
                        SunFontManager.this.fontPath = SunFontManager.this.getPlatformFontPath(noType1Font);
                        SunFontManager.this.registerFontDirs(SunFontManager.this.fontPath);
                    }
                    if (SunFontManager.this.fontPath != null && !SunFontManager.this.gotFontsFromPlatform()) {
                        SunFontManager.this.registerFontsOnPath(SunFontManager.this.fontPath, false, 6, false, true);
                        SunFontManager.this.loadedAllFontFiles = true;
                    }
                    SunFontManager.this.registerOtherFontFiles(SunFontManager.this.registeredFontFiles);
                    SunFontManager.this.discoveredAllFonts = true;
                    return null;
                }
            });
        }
    }

    protected void registerFontDirs(String pathName) {
    }

    private void registerFontsOnPath(String pathName, boolean useJavaRasterizer, int fontRank, boolean defer, boolean resolveSymLinks) {
        StringTokenizer parser = new StringTokenizer(pathName, File.pathSeparator);
        try {
            while (parser.hasMoreTokens()) {
                this.registerFontsInDir(parser.nextToken(), useJavaRasterizer, fontRank, defer, resolveSymLinks);
            }
        }
        catch (NoSuchElementException noSuchElementException) {
            // empty catch block
        }
    }

    public void registerFontsInDir(String dirName) {
        this.registerFontsInDir(dirName, true, 2, true, false);
    }

    protected void registerFontsInDir(String dirName, boolean useJavaRasterizer, int fontRank, boolean defer, boolean resolveSymLinks) {
        File pathFile = new File(dirName);
        this.addDirFonts(dirName, pathFile, ttFilter, 0, useJavaRasterizer, fontRank == 6 ? 3 : fontRank, defer, resolveSymLinks);
        this.addDirFonts(dirName, pathFile, t1Filter, 1, useJavaRasterizer, fontRank == 6 ? 4 : fontRank, defer, resolveSymLinks);
    }

    protected void registerFontDir(String path) {
    }

    public synchronized String getDefaultFontFile() {
        return this.defaultFontFileName;
    }

    protected boolean useAbsoluteFontFileNames() {
        return true;
    }

    protected abstract FontConfiguration createFontConfiguration();

    public abstract FontConfiguration createFontConfiguration(boolean var1, boolean var2);

    public synchronized String getDefaultFontFaceName() {
        return this.defaultFontName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadFontFiles() {
        this.loadFonts();
        if (this.loadedAllFontFiles) {
            return;
        }
        SunFontManager sunFontManager = this;
        synchronized (sunFontManager) {
            if (FontUtilities.debugFonts()) {
                Thread.dumpStack();
                FontUtilities.logInfo("loadAllFontFiles() called");
            }
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    if (SunFontManager.this.fontPath == null) {
                        SunFontManager.this.fontPath = SunFontManager.this.getPlatformFontPath(noType1Font);
                    }
                    if (SunFontManager.this.fontPath != null) {
                        SunFontManager.this.registerFontsOnPath(SunFontManager.this.fontPath, false, 6, false, true);
                    }
                    SunFontManager.this.loadedAllFontFiles = true;
                    return null;
                }
            });
        }
    }

    private void initCompositeFonts(FontConfiguration fontConfig, ConcurrentHashMap<String, Font2D> altNameCache) {
        if (FontUtilities.isLogging()) {
            FontUtilities.logInfo("Initialising composite fonts");
        }
        int numCoreFonts = fontConfig.getNumberCoreFonts();
        String[] fcFonts = fontConfig.getPlatformFontNames();
        for (int f = 0; f < fcFonts.length; ++f) {
            String platformFontName = fcFonts[f];
            String fontFileName = this.getFileNameFromPlatformName(platformFontName);
            String[] nativeNames = null;
            if (fontFileName == null || fontFileName.equals(platformFontName)) {
                fontFileName = platformFontName;
            } else {
                if (f < numCoreFonts) {
                    this.addFontToPlatformFontPath(platformFontName);
                }
                nativeNames = this.getNativeNames(fontFileName, platformFontName);
            }
            this.registerFontFile(fontFileName, nativeNames, 2, true);
        }
        this.registerPlatformFontsUsedByFontConfiguration();
        CompositeFontDescriptor[] compositeFontInfo = fontConfig.get2DCompositeFontInfo();
        for (int i = 0; i < compositeFontInfo.length; ++i) {
            CompositeFontDescriptor descriptor = compositeFontInfo[i];
            String[] componentFileNames = descriptor.getComponentFileNames();
            String[] componentFaceNames = descriptor.getComponentFaceNames();
            if (missingFontFiles != null) {
                for (int ii = 0; ii < componentFileNames.length; ++ii) {
                    if (!missingFontFiles.contains(componentFileNames[ii])) continue;
                    componentFileNames[ii] = this.getDefaultFontFile();
                    componentFaceNames[ii] = this.getDefaultFontFaceName();
                }
            }
            if (altNameCache != null) {
                SunFontManager.registerCompositeFont(descriptor.getFaceName(), componentFileNames, componentFaceNames, descriptor.getCoreComponentCount(), descriptor.getExclusionRanges(), descriptor.getExclusionRangeLimits(), true, altNameCache);
            } else {
                this.registerCompositeFont(descriptor.getFaceName(), componentFileNames, componentFaceNames, descriptor.getCoreComponentCount(), descriptor.getExclusionRanges(), descriptor.getExclusionRangeLimits(), true);
            }
            if (!FontUtilities.debugFonts()) continue;
            FontUtilities.logInfo("registered " + descriptor.getFaceName());
        }
    }

    protected void addFontToPlatformFontPath(String platformFontName) {
    }

    protected void registerFontFile(String fontFileName, String[] nativeNames, int fontRank, boolean defer) {
        if (this.registeredFontFiles.contains(fontFileName)) {
            return;
        }
        int fontFormat = ttFilter.accept(null, fontFileName) ? 0 : (t1Filter.accept(null, fontFileName) ? 1 : 4);
        this.registeredFontFiles.add(fontFileName);
        if (defer) {
            this.registerDeferredFont(fontFileName, fontFileName, nativeNames, fontFormat, false, fontRank);
        } else {
            this.registerFontFile(fontFileName, nativeNames, fontFormat, false, fontRank);
        }
    }

    protected void registerPlatformFontsUsedByFontConfiguration() {
    }

    protected void addToMissingFontFileList(String fileName) {
        if (missingFontFiles == null) {
            missingFontFiles = new HashSet();
        }
        missingFontFiles.add(fileName);
    }

    private boolean isNameForRegisteredFile(String fontName) {
        String fileName = this.getFileNameForFontName(fontName);
        if (fileName == null) {
            return false;
        }
        return this.registeredFontFiles.contains(fileName);
    }

    public void createCompositeFonts(ConcurrentHashMap<String, Font2D> altNameCache, boolean preferLocale, boolean preferProportional) {
        FontConfiguration fontConfig = this.createFontConfiguration(preferLocale, preferProportional);
        this.initCompositeFonts(fontConfig, altNameCache);
    }

    @Override
    public Font[] getAllInstalledFonts() {
        if (this.allFonts == null) {
            this.loadFonts();
            TreeMap<String, Font2D> fontMapNames = new TreeMap<String, Font2D>();
            Font2D[] allfonts = this.getRegisteredFonts();
            for (int i = 0; i < allfonts.length; ++i) {
                if (allfonts[i] instanceof NativeFont) continue;
                fontMapNames.put(allfonts[i].getFontName(null), allfonts[i]);
            }
            String[] platformNames = this.getFontNamesFromPlatform();
            if (platformNames != null) {
                for (int i = 0; i < platformNames.length; ++i) {
                    if (this.isNameForRegisteredFile(platformNames[i])) continue;
                    fontMapNames.put(platformNames[i], null);
                }
            }
            String[] fontNames = fontMapNames.keySet().toArray(new String[0]);
            Font[] fonts = new Font[fontNames.length];
            for (int i = 0; i < fontNames.length; ++i) {
                fonts[i] = new Font(fontNames[i], 0, 1);
                Font2D f2d = (Font2D)fontMapNames.get(fontNames[i]);
                if (f2d == null) continue;
                FontAccess.getFontAccess().setFont2D(fonts[i], f2d.handle);
            }
            this.allFonts = fonts;
        }
        Font[] copyFonts = new Font[this.allFonts.length];
        System.arraycopy(this.allFonts, 0, copyFonts, 0, this.allFonts.length);
        return copyFonts;
    }

    @Override
    public String[] getInstalledFontFamilyNames(Locale requestedLocale) {
        if (requestedLocale == null) {
            requestedLocale = Locale.getDefault();
        }
        if (this.allFamilies != null && this.lastDefaultLocale != null && requestedLocale.equals(this.lastDefaultLocale)) {
            String[] copyFamilies = new String[this.allFamilies.length];
            System.arraycopy(this.allFamilies, 0, copyFamilies, 0, this.allFamilies.length);
            return copyFamilies;
        }
        TreeMap<String, String> familyNames = new TreeMap<String, String>();
        String str = "Serif";
        familyNames.put(str.toLowerCase(), str);
        str = "SansSerif";
        familyNames.put(str.toLowerCase(), str);
        str = "Monospaced";
        familyNames.put(str.toLowerCase(), str);
        str = "Dialog";
        familyNames.put(str.toLowerCase(), str);
        str = "DialogInput";
        familyNames.put(str.toLowerCase(), str);
        if (requestedLocale.equals(SunFontManager.getSystemStartupLocale()) && this.getFamilyNamesFromPlatform(familyNames, requestedLocale)) {
            this.getJREFontFamilyNames(familyNames, requestedLocale);
        } else {
            this.loadFontFiles();
            PhysicalFont[] physicalfonts = this.getPhysicalFonts();
            for (int i = 0; i < physicalfonts.length; ++i) {
                if (physicalfonts[i] instanceof NativeFont) continue;
                String name = physicalfonts[i].getFamilyName(requestedLocale);
                familyNames.put(name.toLowerCase(requestedLocale), name);
            }
        }
        this.addNativeFontFamilyNames(familyNames, requestedLocale);
        String[] retval = familyNames.values().toArray(new String[0]);
        if (requestedLocale.equals(Locale.getDefault())) {
            this.lastDefaultLocale = requestedLocale;
            this.allFamilies = new String[retval.length];
            System.arraycopy(retval, 0, this.allFamilies, 0, this.allFamilies.length);
        }
        return retval;
    }

    protected void addNativeFontFamilyNames(TreeMap<String, String> familyNames, Locale requestedLocale) {
    }

    public void register1dot0Fonts() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                String type1Dir = "/usr/openwin/lib/X11/fonts/Type1";
                SunFontManager.this.registerFontsInDir(type1Dir, true, 4, false, false);
                return null;
            }
        });
    }

    protected void getJREFontFamilyNames(TreeMap<String, String> familyNames, Locale requestedLocale) {
        this.registerDeferredJREFonts(jreFontDirName);
        PhysicalFont[] physicalfonts = this.getPhysicalFonts();
        for (int i = 0; i < physicalfonts.length; ++i) {
            if (physicalfonts[i] instanceof NativeFont) continue;
            String name = physicalfonts[i].getFamilyName(requestedLocale);
            familyNames.put(name.toLowerCase(requestedLocale), name);
        }
    }

    private static Locale getSystemStartupLocale() {
        if (systemLocale == null) {
            systemLocale = AccessController.doPrivileged(new PrivilegedAction<Locale>(){

                @Override
                public Locale run() {
                    String fileEncoding = System.getProperty("file.encoding", "");
                    String sysEncoding = System.getProperty("sun.jnu.encoding");
                    if (sysEncoding != null && !sysEncoding.equals(fileEncoding)) {
                        return Locale.ROOT;
                    }
                    String language = System.getProperty("user.language", "en");
                    String country = System.getProperty("user.country", "");
                    String variant = System.getProperty("user.variant", "");
                    return Locale.of(language, country, variant);
                }
            });
        }
        return systemLocale;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addToPool(FileFont font) {
        FileFont fontFileToClose = null;
        int freeSlot = -1;
        FileFont[] fileFontArray = this.fontFileCache;
        synchronized (this.fontFileCache) {
            for (int i = 0; i < 20; ++i) {
                if (this.fontFileCache[i] == font) {
                    // ** MonitorExit[var4_4] (shouldn't be in output)
                    return;
                }
                if (this.fontFileCache[i] != null || freeSlot >= 0) continue;
                freeSlot = i;
            }
            if (freeSlot >= 0) {
                this.fontFileCache[freeSlot] = font;
                // ** MonitorExit[var4_4] (shouldn't be in output)
                return;
            }
            fontFileToClose = this.fontFileCache[this.lastPoolIndex];
            this.fontFileCache[this.lastPoolIndex] = font;
            this.lastPoolIndex = (this.lastPoolIndex + 1) % 20;
            // ** MonitorExit[var4_4] (shouldn't be in output)
            if (fontFileToClose != null) {
                fontFileToClose.close();
            }
            return;
        }
    }

    protected FontUIResource getFontConfigFUIR(String family, int style, int size) {
        return new FontUIResource(family, style, size);
    }

    static {
        missingFontFiles = null;
        ttFilter = new TTFilter();
        t1Filter = new T1Filter();
        STR_ARRAY = new String[0];
        maxSoftRefCnt = 10;
        SunFontManager.initStatic();
        gAltJAFont = false;
        installedNames = null;
        regFamilyLock = new Object();
        systemLocale = null;
    }

    private static final class FontRegistrationInfo {
        String fontFilePath;
        String[] nativeNames;
        int fontFormat;
        boolean javaRasterizer;
        int fontRank;

        FontRegistrationInfo(String fontPath, String[] names, int format, boolean useJavaRasterizer, int rank) {
            this.fontFilePath = fontPath;
            this.nativeNames = names;
            this.fontFormat = format;
            this.javaRasterizer = useJavaRasterizer;
            this.fontRank = rank;
        }
    }

    private static class TTorT1Filter
    implements FilenameFilter {
        private TTorT1Filter() {
        }

        @Override
        public boolean accept(File dir, String name) {
            boolean isTT;
            int offset = name.length() - 4;
            if (offset <= 0) {
                return false;
            }
            boolean bl = isTT = name.startsWith(".ttf", offset) || name.startsWith(".TTF", offset) || name.startsWith(".ttc", offset) || name.startsWith(".TTC", offset) || name.startsWith(".otf", offset) || name.startsWith(".OTF", offset);
            if (isTT) {
                return true;
            }
            if (noType1Font) {
                return false;
            }
            return name.startsWith(".pfa", offset) || name.startsWith(".pfb", offset) || name.startsWith(".PFA", offset) || name.startsWith(".PFB", offset);
        }
    }

    public static class FamilyDescription {
        public String familyName;
        public String plainFullName;
        public String boldFullName;
        public String italicFullName;
        public String boldItalicFullName;
        public String plainFileName;
        public String boldFileName;
        public String italicFileName;
        public String boldItalicFileName;
        boolean failed;
    }

    private static class TTFilter
    implements FilenameFilter {
        private TTFilter() {
        }

        @Override
        public boolean accept(File dir, String name) {
            int offset = name.length() - 4;
            if (offset <= 0) {
                return false;
            }
            return name.startsWith(".ttf", offset) || name.startsWith(".TTF", offset) || name.startsWith(".ttc", offset) || name.startsWith(".TTC", offset) || name.startsWith(".otf", offset) || name.startsWith(".OTF", offset);
        }
    }

    private static class T1Filter
    implements FilenameFilter {
        private T1Filter() {
        }

        @Override
        public boolean accept(File dir, String name) {
            if (noType1Font) {
                return false;
            }
            int offset = name.length() - 4;
            if (offset <= 0) {
                return false;
            }
            return name.startsWith(".pfa", offset) || name.startsWith(".pfb", offset) || name.startsWith(".PFA", offset) || name.startsWith(".PFB", offset);
        }
    }
}

