/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import sun.awt.FontDescriptor;
import sun.awt.OSInfo;
import sun.awt.SunToolkit;
import sun.font.CompositeFontDescriptor;
import sun.font.FontUtilities;
import sun.font.SunFontManager;
import sun.util.logging.PlatformLogger;

public abstract class FontConfiguration {
    protected static String osVersion;
    protected static String osName;
    protected static String encoding;
    protected static Locale startupLocale;
    protected static Hashtable<String, String> localeMap;
    private static FontConfiguration fontConfig;
    private static PlatformLogger logger;
    protected static boolean isProperties;
    protected SunFontManager fontManager;
    protected boolean preferLocaleFonts;
    protected boolean preferPropFonts;
    private File fontConfigFile;
    private boolean foundOsSpecificFile;
    private boolean inited;
    private String javaLib;
    private static short stringIDNum;
    private static short[] stringIDs;
    private static StringBuilder stringTable;
    public static boolean verbose;
    private short initELC = (short)-1;
    private Locale initLocale;
    private String initEncoding;
    private String alphabeticSuffix;
    private short[][][] compFontNameIDs = new short[5][4][];
    private int[][][] compExclusions = new int[5][][];
    private int[] compCoreNum = new int[5];
    private Set<Short> coreFontNameIDs = new HashSet<Short>();
    private Set<Short> fallbackFontNameIDs = new HashSet<Short>();
    protected static final int NUM_FONTS = 5;
    protected static final int NUM_STYLES = 4;
    protected static final String[] fontNames;
    protected static final String[] publicFontNames;
    protected static final String[] styleNames;
    protected static String[] installedFallbackFontFiles;
    protected HashMap<String, Object> reorderMap = null;
    private Hashtable<String, Charset> charsetRegistry = new Hashtable(5);
    private FontDescriptor[][][] fontDescriptors = new FontDescriptor[5][4][];
    HashMap<String, Boolean> existsMap;
    private int numCoreFonts = -1;
    private String[] componentFonts = null;
    HashMap<String, String> filenamesMap = new HashMap();
    HashSet<String> coreFontFileNames = new HashSet();
    private static final String fontconfigErrorMessage = "Fontconfig head is null, check your fonts or fonts configuration";
    private static final int HEAD_LENGTH = 20;
    private static final int INDEX_scriptIDs = 0;
    private static final int INDEX_scriptFonts = 1;
    private static final int INDEX_elcIDs = 2;
    private static final int INDEX_sequences = 3;
    private static final int INDEX_fontfileNameIDs = 4;
    private static final int INDEX_componentFontNameIDs = 5;
    private static final int INDEX_filenames = 6;
    private static final int INDEX_awtfontpaths = 7;
    private static final int INDEX_exclusions = 8;
    private static final int INDEX_proportionals = 9;
    private static final int INDEX_scriptFontsMotif = 10;
    private static final int INDEX_alphabeticSuffix = 11;
    private static final int INDEX_stringIDs = 12;
    private static final int INDEX_stringTable = 13;
    private static final int INDEX_TABLEEND = 14;
    private static final int INDEX_fallbackScripts = 15;
    private static final int INDEX_appendedfontpath = 16;
    private static final int INDEX_version = 17;
    private static short[] head;
    private static short[] table_scriptIDs;
    private static short[] table_scriptFonts;
    private static short[] table_elcIDs;
    private static short[] table_sequences;
    private static short[] table_fontfileNameIDs;
    private static short[] table_componentFontNameIDs;
    private static short[] table_filenames;
    protected static short[] table_awtfontpaths;
    private static short[] table_exclusions;
    private static short[] table_proportionals;
    private static short[] table_scriptFontsMotif;
    private static short[] table_alphabeticSuffix;
    private static short[] table_stringIDs;
    private static char[] table_stringTable;
    private HashMap<String, Short> reorderScripts;
    private static String[] stringCache;
    private static final int[] EMPTY_INT_ARRAY;
    private static final String[] EMPTY_STRING_ARRAY;
    private static final short[] EMPTY_SHORT_ARRAY;
    private static final String UNDEFINED_COMPONENT_FONT = "unknown";

    public FontConfiguration(SunFontManager fm) {
        if (FontUtilities.debugFonts()) {
            FontUtilities.logInfo("Creating standard Font Configuration");
        }
        if (FontUtilities.debugFonts() && logger == null) {
            logger = PlatformLogger.getLogger("sun.awt.FontConfiguration");
        }
        this.fontManager = fm;
        this.setOsNameAndVersion();
        this.setEncoding();
        this.findFontConfigFile();
    }

    public synchronized boolean init() {
        if (!this.inited) {
            this.preferLocaleFonts = false;
            this.preferPropFonts = false;
            this.setFontConfiguration();
            this.readFontConfigFile(this.fontConfigFile);
            this.initFontConfig();
            this.inited = true;
        }
        return true;
    }

    public FontConfiguration(SunFontManager fm, boolean preferLocaleFonts, boolean preferPropFonts) {
        this.fontManager = fm;
        if (FontUtilities.debugFonts()) {
            FontUtilities.logInfo("Creating alternate Font Configuration");
        }
        this.preferLocaleFonts = preferLocaleFonts;
        this.preferPropFonts = preferPropFonts;
        this.initFontConfig();
    }

    protected void setOsNameAndVersion() {
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
    }

    private void setEncoding() {
        encoding = Charset.defaultCharset().name();
        startupLocale = SunToolkit.getStartupLocale();
    }

    public boolean foundOsSpecificFile() {
        return this.foundOsSpecificFile;
    }

    public boolean fontFilesArePresent() {
        this.init();
        short fontNameID = this.compFontNameIDs[0][0][0];
        short fileNameID = FontConfiguration.getComponentFileID(fontNameID);
        final String fileName = this.mapFileName(FontConfiguration.getComponentFileName(fileNameID));
        Boolean exists = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                try {
                    File f = new File(fileName);
                    return f.exists();
                }
                catch (Exception e) {
                    return Boolean.FALSE;
                }
            }
        });
        return exists;
    }

    private void findFontConfigFile() {
        this.foundOsSpecificFile = true;
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new Error("java.home property not set");
        }
        this.javaLib = javaHome + File.separator + "lib";
        String javaConfFonts = javaHome + File.separator + "conf" + File.separator + "fonts";
        String userConfigFile = System.getProperty("sun.awt.fontconfig");
        if (userConfigFile != null) {
            this.fontConfigFile = new File(userConfigFile);
        } else {
            this.fontConfigFile = this.findFontConfigFile(javaConfFonts);
            if (this.fontConfigFile == null) {
                this.fontConfigFile = this.findFontConfigFile(this.javaLib);
            }
        }
    }

    private void readFontConfigFile(File f) {
        String version;
        block12: {
            this.getInstalledFallbackFonts(this.javaLib);
            if (f != null) {
                try (FileInputStream in = new FileInputStream(f.getPath());){
                    if (isProperties) {
                        FontConfiguration.loadProperties(in);
                    } else {
                        FontConfiguration.loadBinary(in);
                    }
                    if (FontUtilities.debugFonts()) {
                        logger.config("Read logical font configuration from " + String.valueOf(f));
                    }
                }
                catch (IOException e) {
                    if (!FontUtilities.debugFonts()) break block12;
                    logger.config("Failed to read logical font configuration from " + String.valueOf(f));
                }
            }
        }
        if (!"1".equals(version = this.getVersion()) && FontUtilities.debugFonts()) {
            logger.config("Unsupported fontconfig version: " + version);
        }
    }

    protected void getInstalledFallbackFonts(String javaLib) {
        String fallbackDirName = javaLib + File.separator + "fonts" + File.separator + "fallback";
        File fallbackDir = new File(fallbackDirName);
        if (fallbackDir.exists() && fallbackDir.isDirectory()) {
            int i;
            String[] ttfs = fallbackDir.list(this.fontManager.getTrueTypeFilter());
            String[] t1s = fallbackDir.list(this.fontManager.getType1Filter());
            int numTTFs = ttfs == null ? 0 : ttfs.length;
            int numT1s = t1s == null ? 0 : t1s.length;
            int len = numTTFs + numT1s;
            if (numTTFs + numT1s == 0) {
                return;
            }
            installedFallbackFontFiles = new String[len];
            for (i = 0; i < numTTFs; ++i) {
                FontConfiguration.installedFallbackFontFiles[i] = String.valueOf(fallbackDir) + File.separator + ttfs[i];
            }
            for (i = 0; i < numT1s; ++i) {
                FontConfiguration.installedFallbackFontFiles[i + numTTFs] = String.valueOf(fallbackDir) + File.separator + t1s[i];
            }
            this.fontManager.registerFontsInDir(fallbackDirName);
        }
    }

    private File findImpl(String fname) {
        File f = new File(fname + ".properties");
        if (FontUtilities.debugFonts()) {
            logger.info("Looking for text fontconfig file : " + String.valueOf(f));
        }
        if (f.canRead()) {
            if (FontUtilities.debugFonts()) {
                logger.info("Found file : " + String.valueOf(f));
            }
            isProperties = true;
            return f;
        }
        f = new File(fname + ".bfc");
        if (FontUtilities.debugFonts()) {
            logger.info("Looking for binary fontconfig file : " + String.valueOf(f));
        }
        if (f.canRead()) {
            if (FontUtilities.debugFonts()) {
                logger.info("Found file : " + String.valueOf(f));
            }
            isProperties = false;
            return f;
        }
        return null;
    }

    private File findFontConfigFile(String dir) {
        File configFile;
        if (!new File(dir).exists()) {
            return null;
        }
        String baseName = dir + File.separator + "fontconfig";
        String osMajorVersion = null;
        if (osVersion != null && osName != null) {
            configFile = this.findImpl(baseName + "." + osName + "." + osVersion);
            if (configFile != null) {
                return configFile;
            }
            int decimalPointIndex = osVersion.indexOf(46);
            if (decimalPointIndex != -1 && (configFile = this.findImpl(baseName + "." + osName + "." + (osMajorVersion = osVersion.substring(0, osVersion.indexOf(46))))) != null) {
                return configFile;
            }
        }
        if (osName != null && (configFile = this.findImpl(baseName + "." + osName)) != null) {
            return configFile;
        }
        if (osVersion != null) {
            configFile = this.findImpl(baseName + "." + osVersion);
            if (configFile != null) {
                return configFile;
            }
            if (osMajorVersion != null && (configFile = this.findImpl(baseName + "." + osMajorVersion)) != null) {
                return configFile;
            }
        }
        this.foundOsSpecificFile = false;
        configFile = this.findImpl(baseName);
        if (configFile != null) {
            return configFile;
        }
        if (FontUtilities.debugFonts()) {
            logger.info("Did not find a fontconfig file.");
        }
        return null;
    }

    public static void loadBinary(InputStream inStream) throws IOException {
        DataInputStream in = new DataInputStream(inStream);
        head = FontConfiguration.readShortTable(in, 20);
        int[] tableSizes = new int[14];
        for (int i = 0; i < 14; ++i) {
            tableSizes[i] = head[i + 1] - head[i];
        }
        table_scriptIDs = FontConfiguration.readShortTable(in, tableSizes[0]);
        table_scriptFonts = FontConfiguration.readShortTable(in, tableSizes[1]);
        table_elcIDs = FontConfiguration.readShortTable(in, tableSizes[2]);
        table_sequences = FontConfiguration.readShortTable(in, tableSizes[3]);
        table_fontfileNameIDs = FontConfiguration.readShortTable(in, tableSizes[4]);
        table_componentFontNameIDs = FontConfiguration.readShortTable(in, tableSizes[5]);
        table_filenames = FontConfiguration.readShortTable(in, tableSizes[6]);
        table_awtfontpaths = FontConfiguration.readShortTable(in, tableSizes[7]);
        table_exclusions = FontConfiguration.readShortTable(in, tableSizes[8]);
        table_proportionals = FontConfiguration.readShortTable(in, tableSizes[9]);
        table_scriptFontsMotif = FontConfiguration.readShortTable(in, tableSizes[10]);
        table_alphabeticSuffix = FontConfiguration.readShortTable(in, tableSizes[11]);
        table_stringIDs = FontConfiguration.readShortTable(in, tableSizes[12]);
        stringCache = new String[table_stringIDs.length + 1];
        int len = tableSizes[13];
        byte[] bb = new byte[len * 2];
        table_stringTable = new char[len];
        in.read(bb);
        int i = 0;
        int j = 0;
        while (i < len) {
            FontConfiguration.table_stringTable[i++] = (char)(bb[j++] << 8 | bb[j++] & 0xFF);
        }
        if (verbose) {
            FontConfiguration.dump();
        }
    }

    public static void saveBinary(OutputStream out) throws IOException {
        FontConfiguration.sanityCheck();
        DataOutputStream dataOut = new DataOutputStream(out);
        FontConfiguration.writeShortTable(dataOut, head);
        FontConfiguration.writeShortTable(dataOut, table_scriptIDs);
        FontConfiguration.writeShortTable(dataOut, table_scriptFonts);
        FontConfiguration.writeShortTable(dataOut, table_elcIDs);
        FontConfiguration.writeShortTable(dataOut, table_sequences);
        FontConfiguration.writeShortTable(dataOut, table_fontfileNameIDs);
        FontConfiguration.writeShortTable(dataOut, table_componentFontNameIDs);
        FontConfiguration.writeShortTable(dataOut, table_filenames);
        FontConfiguration.writeShortTable(dataOut, table_awtfontpaths);
        FontConfiguration.writeShortTable(dataOut, table_exclusions);
        FontConfiguration.writeShortTable(dataOut, table_proportionals);
        FontConfiguration.writeShortTable(dataOut, table_scriptFontsMotif);
        FontConfiguration.writeShortTable(dataOut, table_alphabeticSuffix);
        FontConfiguration.writeShortTable(dataOut, table_stringIDs);
        dataOut.writeChars(new String(table_stringTable));
        out.close();
        if (verbose) {
            FontConfiguration.dump();
        }
    }

    public static void loadProperties(InputStream in) throws IOException {
        stringIDNum = 1;
        stringIDs = new short[1000];
        stringTable = new StringBuilder(4096);
        if (verbose && logger == null) {
            logger = PlatformLogger.getLogger("sun.awt.FontConfiguration");
        }
        new PropertiesHandler().load(in);
        stringIDs = null;
        stringTable = null;
    }

    private void initFontConfig() {
        this.initLocale = startupLocale;
        this.initEncoding = encoding;
        if (this.preferLocaleFonts && !FontConfiguration.willReorderForStartupLocale()) {
            this.preferLocaleFonts = false;
        }
        this.initELC = this.getInitELC();
        this.initAllComponentFonts();
    }

    private short getInitELC() {
        String country;
        if (this.initELC != -1) {
            return this.initELC;
        }
        HashMap<String, Integer> elcIDs = new HashMap<String, Integer>();
        for (int i = 0; i < table_elcIDs.length; ++i) {
            elcIDs.put(FontConfiguration.getString(table_elcIDs[i]), i);
        }
        String language = this.initLocale.getLanguage();
        Object elc = this.initEncoding + "." + language + "." + (country = this.initLocale.getCountry());
        this.initELC = elcIDs.containsKey(elc) || elcIDs.containsKey(elc = this.initEncoding + "." + language) || elcIDs.containsKey(elc = this.initEncoding) ? ((Integer)elcIDs.get(elc)).shortValue() : ((Integer)elcIDs.get("NULL.NULL.NULL")).shortValue();
        for (int i = 0; i < table_alphabeticSuffix.length; i += 2) {
            if (this.initELC != table_alphabeticSuffix[i]) continue;
            this.alphabeticSuffix = FontConfiguration.getString(table_alphabeticSuffix[i + 1]);
            return this.initELC;
        }
        return this.initELC;
    }

    private void initAllComponentFonts() {
        short[] fallbackScripts = FontConfiguration.getFallbackScripts();
        for (int fontIndex = 0; fontIndex < 5; ++fontIndex) {
            short[] coreScripts = this.getCoreScripts(fontIndex);
            this.compCoreNum[fontIndex] = coreScripts.length;
            int[][] exclusions = new int[coreScripts.length][];
            for (int i = 0; i < coreScripts.length; ++i) {
                exclusions[i] = FontConfiguration.getExclusionRanges(coreScripts[i]);
            }
            this.compExclusions[fontIndex] = exclusions;
            for (int styleIndex = 0; styleIndex < 4; ++styleIndex) {
                int index;
                short[] nameIDs = new short[coreScripts.length + fallbackScripts.length];
                for (index = 0; index < coreScripts.length; ++index) {
                    nameIDs[index] = FontConfiguration.getComponentFontID(coreScripts[index], fontIndex, styleIndex);
                    if (this.preferLocaleFonts && localeMap != null && this.fontManager.usingAlternateFontforJALocales()) {
                        nameIDs[index] = this.remapLocaleMap(fontIndex, styleIndex, coreScripts[index], nameIDs[index]);
                    }
                    if (this.preferPropFonts) {
                        nameIDs[index] = this.remapProportional(fontIndex, nameIDs[index]);
                    }
                    this.coreFontNameIDs.add(nameIDs[index]);
                }
                for (int i = 0; i < fallbackScripts.length; ++i) {
                    short id = FontConfiguration.getComponentFontID(fallbackScripts[i], fontIndex, styleIndex);
                    if (this.preferLocaleFonts && localeMap != null && this.fontManager.usingAlternateFontforJALocales()) {
                        id = this.remapLocaleMap(fontIndex, styleIndex, fallbackScripts[i], id);
                    }
                    if (this.preferPropFonts) {
                        id = this.remapProportional(fontIndex, id);
                    }
                    if (FontConfiguration.contains(nameIDs, id, index)) continue;
                    this.fallbackFontNameIDs.add(id);
                    nameIDs[index++] = id;
                }
                if (index < nameIDs.length) {
                    short[] newNameIDs = new short[index];
                    System.arraycopy(nameIDs, 0, newNameIDs, 0, index);
                    nameIDs = newNameIDs;
                }
                this.compFontNameIDs[fontIndex][styleIndex] = nameIDs;
            }
        }
    }

    private short remapLocaleMap(int fontIndex, int styleIndex, short scriptID, short fontID) {
        String scriptName = FontConfiguration.getString(table_scriptIDs[scriptID]);
        String value = localeMap.get(scriptName);
        if (value == null) {
            String fontName = fontNames[fontIndex];
            String styleName = styleNames[styleIndex];
            value = localeMap.get(fontName + "." + styleName + "." + scriptName);
        }
        if (value == null) {
            return fontID;
        }
        for (int i = 0; i < table_componentFontNameIDs.length; ++i) {
            String name = FontConfiguration.getString(table_componentFontNameIDs[i]);
            if (!value.equalsIgnoreCase(name)) continue;
            fontID = (short)i;
            break;
        }
        return fontID;
    }

    public static boolean hasMonoToPropMap() {
        return table_proportionals != null && table_proportionals.length != 0;
    }

    private short remapProportional(int fontIndex, short id) {
        if (this.preferPropFonts && table_proportionals.length != 0 && fontIndex != 2 && fontIndex != 4) {
            for (int i = 0; i < table_proportionals.length; i += 2) {
                if (table_proportionals[i] != id) continue;
                return table_proportionals[i + 1];
            }
        }
        return id;
    }

    public static boolean isLogicalFontFamilyName(String fontName) {
        return FontConfiguration.isLogicalFontFamilyNameLC(fontName.toLowerCase(Locale.ENGLISH));
    }

    public static boolean isLogicalFontFamilyNameLC(String fontName) {
        for (int i = 0; i < fontNames.length; ++i) {
            if (!fontName.equals(fontNames[i])) continue;
            return true;
        }
        return false;
    }

    private static boolean isLogicalFontStyleName(String styleName) {
        for (int i = 0; i < styleNames.length; ++i) {
            if (!styleName.equals(styleNames[i])) continue;
            return true;
        }
        return false;
    }

    public static boolean isLogicalFontFaceName(String fontName) {
        return FontConfiguration.isLogicalFontFaceNameLC(fontName.toLowerCase(Locale.ENGLISH));
    }

    public static boolean isLogicalFontFaceNameLC(String fontName) {
        int period = fontName.indexOf(46);
        if (period >= 0) {
            String familyName = fontName.substring(0, period);
            String styleName = fontName.substring(period + 1);
            return FontConfiguration.isLogicalFontFamilyName(familyName) && FontConfiguration.isLogicalFontStyleName(styleName);
        }
        return FontConfiguration.isLogicalFontFamilyName(fontName);
    }

    protected static int getFontIndex(String fontName) {
        return FontConfiguration.getArrayIndex(fontNames, fontName);
    }

    protected static int getStyleIndex(String styleName) {
        return FontConfiguration.getArrayIndex(styleNames, styleName);
    }

    private static int getArrayIndex(String[] names, String name) {
        for (int i = 0; i < names.length; ++i) {
            if (!name.equals(names[i])) continue;
            return i;
        }
        assert (false);
        return 0;
    }

    protected static int getStyleIndex(int style) {
        switch (style) {
            case 0: {
                return 0;
            }
            case 1: {
                return 1;
            }
            case 2: {
                return 2;
            }
            case 3: {
                return 3;
            }
        }
        return 0;
    }

    protected static String getFontName(int fontIndex) {
        return fontNames[fontIndex];
    }

    protected static String getStyleName(int styleIndex) {
        return styleNames[styleIndex];
    }

    public static String getLogicalFontFaceName(String familyName, int style) {
        assert (FontConfiguration.isLogicalFontFamilyName(familyName));
        return familyName.toLowerCase(Locale.ENGLISH) + "." + FontConfiguration.getStyleString(style);
    }

    public static String getStyleString(int style) {
        return FontConfiguration.getStyleName(FontConfiguration.getStyleIndex(style));
    }

    public abstract String getFallbackFamilyName(String var1, String var2);

    protected String getCompatibilityFamilyName(String fontName) {
        if ((fontName = fontName.toLowerCase(Locale.ENGLISH)).equals("timesroman")) {
            return "serif";
        }
        if (fontName.equals("helvetica")) {
            return "sansserif";
        }
        if (fontName.equals("courier")) {
            return "monospaced";
        }
        return null;
    }

    protected String mapFileName(String fileName) {
        return fileName;
    }

    protected abstract void initReorderMap();

    private void shuffle(String[] seq, int src, int dst) {
        if (dst >= src) {
            return;
        }
        String tmp = seq[src];
        for (int i = src; i > dst; --i) {
            seq[i] = seq[i - 1];
        }
        seq[dst] = tmp;
    }

    public static boolean willReorderForStartupLocale() {
        return FontConfiguration.getReorderSequence() != null;
    }

    private static Object getReorderSequence() {
        String country;
        String language;
        HashMap<String, Object> reorderMap;
        Object val;
        if (FontConfiguration.fontConfig.reorderMap == null) {
            fontConfig.initReorderMap();
        }
        if ((val = (reorderMap = FontConfiguration.fontConfig.reorderMap).get(encoding + "." + (language = startupLocale.getLanguage()) + "." + (country = startupLocale.getCountry()))) == null) {
            val = reorderMap.get(encoding + "." + language);
        }
        if (val == null) {
            val = reorderMap.get(encoding);
        }
        return val;
    }

    private void reorderSequenceForLocale(String[] seq) {
        block4: {
            Object val;
            block3: {
                val = FontConfiguration.getReorderSequence();
                if (!(val instanceof String)) break block3;
                for (int i = 0; i < seq.length; ++i) {
                    if (!seq[i].equals(val)) continue;
                    this.shuffle(seq, i, 0);
                    return;
                }
                break block4;
            }
            if (!(val instanceof String[])) break block4;
            String[] fontLangs = (String[])val;
            for (int l = 0; l < fontLangs.length; ++l) {
                for (int i = 0; i < seq.length; ++i) {
                    if (!seq[i].equals(fontLangs[l])) continue;
                    this.shuffle(seq, i, l);
                }
            }
        }
    }

    private static Vector<String> splitSequence(String sequence) {
        int end;
        Vector<String> parts = new Vector<String>();
        int start = 0;
        while ((end = sequence.indexOf(44, start)) >= 0) {
            parts.add(sequence.substring(start, end));
            start = end + 1;
        }
        if (sequence.length() > start) {
            parts.add(sequence.substring(start));
        }
        return parts;
    }

    public FontDescriptor[] getFontDescriptors(String fontName, int style) {
        assert (FontConfiguration.isLogicalFontFamilyName(fontName));
        fontName = fontName.toLowerCase(Locale.ENGLISH);
        int fontIndex = FontConfiguration.getFontIndex(fontName);
        int styleIndex = FontConfiguration.getStyleIndex(style);
        return this.getFontDescriptors(fontIndex, styleIndex);
    }

    private FontDescriptor[] getFontDescriptors(int fontIndex, int styleIndex) {
        FontDescriptor[] descriptors = this.fontDescriptors[fontIndex][styleIndex];
        if (descriptors == null) {
            descriptors = this.buildFontDescriptors(fontIndex, styleIndex);
            this.fontDescriptors[fontIndex][styleIndex] = descriptors;
        }
        return descriptors;
    }

    protected FontDescriptor[] buildFontDescriptors(int fontIndex, int styleIndex) {
        String fontName = fontNames[fontIndex];
        String styleName = styleNames[styleIndex];
        short[] scriptIDs = this.getCoreScripts(fontIndex);
        short[] nameIDs = this.compFontNameIDs[fontIndex][styleIndex];
        String[] sequence = new String[scriptIDs.length];
        String[] names = new String[scriptIDs.length];
        for (int i = 0; i < sequence.length; ++i) {
            names[i] = FontConfiguration.getComponentFontName(nameIDs[i]);
            sequence[i] = FontConfiguration.getScriptName(scriptIDs[i]);
            if (this.alphabeticSuffix == null || !"alphabetic".equals(sequence[i])) continue;
            sequence[i] = sequence[i] + "/" + this.alphabeticSuffix;
        }
        int[][] fontExclusionRanges = this.compExclusions[fontIndex];
        FontDescriptor[] descriptors = new FontDescriptor[names.length];
        for (int i = 0; i < names.length; ++i) {
            String awtFontName = this.makeAWTFontName(names[i], sequence[i]);
            String encoding = this.getEncoding(names[i], sequence[i]);
            if (encoding == null) {
                encoding = "default";
            }
            CharsetEncoder enc = this.getFontCharsetEncoder(encoding.trim(), awtFontName);
            int[] exclusionRanges = fontExclusionRanges[i];
            descriptors[i] = new FontDescriptor(awtFontName, enc, exclusionRanges);
        }
        return descriptors;
    }

    protected String makeAWTFontName(String platformFontName, String characterSubsetName) {
        return platformFontName;
    }

    protected abstract String getEncoding(String var1, String var2);

    private CharsetEncoder getFontCharsetEncoder(final String charsetName, String fontName) {
        Charset fc = null;
        fc = charsetName.equals("default") ? this.charsetRegistry.get(fontName) : this.charsetRegistry.get(charsetName);
        if (fc != null) {
            return fc.newEncoder();
        }
        if (!(charsetName.startsWith("sun.awt.") || charsetName.equals("default") || charsetName.startsWith("sun.font."))) {
            fc = Charset.forName(charsetName);
        } else {
            Class fcc = (Class)AccessController.doPrivileged(new PrivilegedAction<Class<?>>(){

                @Override
                public Class<?> run() {
                    try {
                        return Class.forName(charsetName, true, ClassLoader.getSystemClassLoader());
                    }
                    catch (ClassNotFoundException classNotFoundException) {
                        return null;
                    }
                }
            });
            if (fcc != null) {
                try {
                    fc = (Charset)fcc.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        if (fc == null) {
            fc = this.getDefaultFontCharset(fontName);
        }
        if (charsetName.equals("default")) {
            this.charsetRegistry.put(fontName, fc);
        } else {
            this.charsetRegistry.put(charsetName, fc);
        }
        return fc.newEncoder();
    }

    protected abstract Charset getDefaultFontCharset(String var1);

    public HashSet<String> getAWTFontPathSet() {
        return null;
    }

    public CompositeFontDescriptor[] get2DCompositeFontInfo() {
        CompositeFontDescriptor[] result = new CompositeFontDescriptor[20];
        String defaultFontFile = this.fontManager.getDefaultFontFile();
        String defaultFontFaceName = this.fontManager.getDefaultFontFaceName();
        for (int fontIndex = 0; fontIndex < 5; ++fontIndex) {
            String fontName = publicFontNames[fontIndex];
            int[][] exclusions = this.compExclusions[fontIndex];
            int numExclusionRanges = 0;
            for (int i = 0; i < exclusions.length; ++i) {
                numExclusionRanges += exclusions[i].length;
            }
            int[] exclusionRanges = new int[numExclusionRanges];
            int[] exclusionRangeLimits = new int[exclusions.length];
            int exclusionRangeIndex = 0;
            boolean exclusionRangeLimitIndex = false;
            for (int i = 0; i < exclusions.length; ++i) {
                int[] componentRanges = exclusions[i];
                int j = 0;
                while (j < componentRanges.length) {
                    int value = componentRanges[j];
                    exclusionRanges[exclusionRangeIndex++] = componentRanges[j++];
                    exclusionRanges[exclusionRangeIndex++] = componentRanges[j++];
                }
                exclusionRangeLimits[i] = exclusionRangeIndex;
            }
            for (int styleIndex = 0; styleIndex < 4; ++styleIndex) {
                int[] clippedExclusionRangeLimits;
                int index;
                int maxComponentFontCount = this.compFontNameIDs[fontIndex][styleIndex].length;
                if (installedFallbackFontFiles != null) {
                    maxComponentFontCount += installedFallbackFontFiles.length;
                }
                String faceName = fontName + "." + styleNames[styleIndex];
                String[] componentFaceNames = new String[maxComponentFontCount];
                String[] componentFileNames = new String[maxComponentFontCount];
                for (index = 0; index < this.compFontNameIDs[fontIndex][styleIndex].length; ++index) {
                    short fontNameID = this.compFontNameIDs[fontIndex][styleIndex][index];
                    short fileNameID = FontConfiguration.getComponentFileID(fontNameID);
                    componentFaceNames[index] = this.getFaceNameFromComponentFontName(FontConfiguration.getComponentFontName(fontNameID));
                    componentFileNames[index] = this.mapFileName(FontConfiguration.getComponentFileName(fileNameID));
                    if (componentFileNames[index] != null && !this.needToSearchForFile(componentFileNames[index])) continue;
                    componentFileNames[index] = this.getFileNameFromComponentFontName(FontConfiguration.getComponentFontName(fontNameID));
                }
                if (installedFallbackFontFiles != null) {
                    for (int ifb = 0; ifb < installedFallbackFontFiles.length; ++ifb) {
                        componentFaceNames[index] = null;
                        componentFileNames[index] = installedFallbackFontFiles[ifb];
                        ++index;
                    }
                }
                if (index < maxComponentFontCount) {
                    String[] newComponentFaceNames = new String[index];
                    System.arraycopy(componentFaceNames, 0, newComponentFaceNames, 0, index);
                    componentFaceNames = newComponentFaceNames;
                    String[] newComponentFileNames = new String[index];
                    System.arraycopy(componentFileNames, 0, newComponentFileNames, 0, index);
                    componentFileNames = newComponentFileNames;
                }
                if (index != (clippedExclusionRangeLimits = exclusionRangeLimits).length) {
                    int len = exclusionRangeLimits.length;
                    clippedExclusionRangeLimits = new int[index];
                    System.arraycopy(exclusionRangeLimits, 0, clippedExclusionRangeLimits, 0, len);
                    for (int i = len; i < index; ++i) {
                        clippedExclusionRangeLimits[i] = exclusionRanges.length;
                    }
                }
                result[fontIndex * 4 + styleIndex] = new CompositeFontDescriptor(faceName, this.compCoreNum[fontIndex], componentFaceNames, componentFileNames, exclusionRanges, clippedExclusionRangeLimits);
            }
        }
        return result;
    }

    protected abstract String getFaceNameFromComponentFontName(String var1);

    protected abstract String getFileNameFromComponentFontName(String var1);

    public boolean needToSearchForFile(String fileName) {
        Boolean exists;
        if (!FontUtilities.isLinux) {
            return false;
        }
        if (this.existsMap == null) {
            this.existsMap = new HashMap();
        }
        if ((exists = this.existsMap.get(fileName)) == null) {
            this.getNumberCoreFonts();
            if (!this.coreFontFileNames.contains(fileName)) {
                exists = Boolean.TRUE;
            } else {
                exists = new File(fileName).exists();
                this.existsMap.put(fileName, exists);
                if (FontUtilities.debugFonts() && exists == Boolean.FALSE) {
                    logger.warning("Couldn't locate font file " + fileName);
                }
            }
        }
        return exists == Boolean.FALSE;
    }

    public int getNumberCoreFonts() {
        if (this.numCoreFonts == -1) {
            int i;
            this.numCoreFonts = this.coreFontNameIDs.size();
            Short[] emptyShortArray = new Short[]{};
            Short[] core = this.coreFontNameIDs.toArray(emptyShortArray);
            Short[] fallback = this.fallbackFontNameIDs.toArray(emptyShortArray);
            int numFallbackFonts = 0;
            for (i = 0; i < fallback.length; ++i) {
                if (this.coreFontNameIDs.contains(fallback[i])) {
                    fallback[i] = null;
                    continue;
                }
                ++numFallbackFonts;
            }
            this.componentFonts = new String[this.numCoreFonts + numFallbackFonts];
            Object filename = null;
            for (i = 0; i < core.length; ++i) {
                short fontid = core[i];
                short fileid = FontConfiguration.getComponentFileID(fontid);
                this.componentFonts[i] = FontConfiguration.getComponentFontName(fontid);
                String compFileName = FontConfiguration.getComponentFileName(fileid);
                if (compFileName != null) {
                    this.coreFontFileNames.add(compFileName);
                }
                this.filenamesMap.put(this.componentFonts[i], this.mapFileName(compFileName));
            }
            for (int j = 0; j < fallback.length; ++j) {
                if (fallback[j] == null) continue;
                short fontid = fallback[j];
                short fileid = FontConfiguration.getComponentFileID(fontid);
                this.componentFonts[i] = FontConfiguration.getComponentFontName(fontid);
                this.filenamesMap.put(this.componentFonts[i], this.mapFileName(FontConfiguration.getComponentFileName(fileid)));
                ++i;
            }
        }
        return this.numCoreFonts;
    }

    public String[] getPlatformFontNames() {
        if (this.numCoreFonts == -1) {
            this.getNumberCoreFonts();
        }
        return this.componentFonts;
    }

    public String getFileNameFromPlatformName(String platformName) {
        return this.filenamesMap.get(platformName);
    }

    public String getExtraFontPath() {
        if (head == null) {
            throw new RuntimeException(fontconfigErrorMessage);
        }
        return FontConfiguration.getString(head[16]);
    }

    public String getVersion() {
        if (head == null) {
            throw new RuntimeException(fontconfigErrorMessage);
        }
        return FontConfiguration.getString(head[17]);
    }

    protected static FontConfiguration getFontConfiguration() {
        return fontConfig;
    }

    protected void setFontConfiguration() {
        fontConfig = this;
    }

    private static void sanityCheck() {
        int ii;
        int errors = 0;
        for (ii = 1; ii < table_filenames.length; ++ii) {
            if (table_filenames[ii] != -1) continue;
            if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS) {
                System.err.println("\n Error: <filename." + FontConfiguration.getString(table_componentFontNameIDs[ii]) + "> entry is missing!!!");
                ++errors;
                continue;
            }
            if (!verbose || FontConfiguration.isEmpty(table_filenames)) continue;
            System.err.println("\n Note: 'filename' entry is undefined for \"" + FontConfiguration.getString(table_componentFontNameIDs[ii]) + "\"");
        }
        for (ii = 0; ii < table_scriptIDs.length; ++ii) {
            short fid = table_scriptFonts[ii];
            if (fid == 0) {
                System.out.println("\n Error: <allfonts." + FontConfiguration.getString(table_scriptIDs[ii]) + "> entry is missing!!!");
                ++errors;
                continue;
            }
            if (fid >= 0) continue;
            fid = -fid;
            for (int iii = 0; iii < 5; ++iii) {
                for (int iij = 0; iij < 4; ++iij) {
                    int jj = iii * 4 + iij;
                    short ffid = table_scriptFonts[fid + jj];
                    if (ffid != 0) continue;
                    System.err.println("\n Error: <" + FontConfiguration.getFontName(iii) + "." + FontConfiguration.getStyleName(iij) + "." + FontConfiguration.getString(table_scriptIDs[ii]) + "> entry is missing!!!");
                    ++errors;
                }
            }
        }
        if (errors != 0) {
            System.err.println("!!THERE ARE " + errors + " ERROR(S) IN THE FONTCONFIG FILE, PLEASE CHECK ITS CONTENT!!\n");
            System.exit(1);
        }
    }

    private static boolean isEmpty(short[] a) {
        for (short s : a) {
            if (s == -1) continue;
            return false;
        }
        return true;
    }

    private static void dump() {
        short fid;
        int ii;
        System.out.println("\n----Head Table------------");
        for (ii = 0; ii < 20; ++ii) {
            System.out.println("  " + ii + " : " + head[ii]);
        }
        System.out.println("\n----scriptIDs-------------");
        FontConfiguration.printTable(table_scriptIDs, 0);
        System.out.println("\n----scriptFonts----------------");
        for (ii = 0; ii < table_scriptIDs.length; ++ii) {
            fid = table_scriptFonts[ii];
            if (fid < 0) continue;
            System.out.println("  allfonts." + FontConfiguration.getString(table_scriptIDs[ii]) + "=" + FontConfiguration.getString(table_componentFontNameIDs[fid]));
        }
        for (ii = 0; ii < table_scriptIDs.length; ++ii) {
            fid = table_scriptFonts[ii];
            if (fid >= 0) continue;
            fid = -fid;
            for (int iii = 0; iii < 5; ++iii) {
                for (int iij = 0; iij < 4; ++iij) {
                    int jj = iii * 4 + iij;
                    short ffid = table_scriptFonts[fid + jj];
                    System.out.println("  " + FontConfiguration.getFontName(iii) + "." + FontConfiguration.getStyleName(iij) + "." + FontConfiguration.getString(table_scriptIDs[ii]) + "=" + FontConfiguration.getString(table_componentFontNameIDs[ffid]));
                }
            }
        }
        System.out.println("\n----elcIDs----------------");
        FontConfiguration.printTable(table_elcIDs, 0);
        System.out.println("\n----sequences-------------");
        for (ii = 0; ii < table_elcIDs.length; ++ii) {
            System.out.println("  " + ii + "/" + FontConfiguration.getString(table_elcIDs[ii]));
            short[] ss = FontConfiguration.getShortArray(table_sequences[ii * 5 + 0]);
            for (int jj = 0; jj < ss.length; ++jj) {
                System.out.println("     " + FontConfiguration.getString(table_scriptIDs[ss[jj]]));
            }
        }
        System.out.println("\n----fontfileNameIDs-------");
        FontConfiguration.printTable(table_fontfileNameIDs, 0);
        System.out.println("\n----componentFontNameIDs--");
        FontConfiguration.printTable(table_componentFontNameIDs, 1);
        System.out.println("\n----filenames-------------");
        for (ii = 0; ii < table_filenames.length; ++ii) {
            if (table_filenames[ii] == -1) {
                System.out.println("  " + ii + " : null");
                continue;
            }
            System.out.println("  " + ii + " : " + FontConfiguration.getString(table_fontfileNameIDs[table_filenames[ii]]));
        }
        System.out.println("\n----awtfontpaths---------");
        for (ii = 0; ii < table_awtfontpaths.length; ++ii) {
            System.out.println("  " + FontConfiguration.getString(table_scriptIDs[ii]) + " : " + FontConfiguration.getString(table_awtfontpaths[ii]));
        }
        System.out.println("\n----proportionals--------");
        for (ii = 0; ii < table_proportionals.length; ++ii) {
            System.out.println("  " + FontConfiguration.getString(table_componentFontNameIDs[table_proportionals[ii++]]) + " -> " + FontConfiguration.getString(table_componentFontNameIDs[table_proportionals[ii]]));
        }
        int i = 0;
        System.out.println("\n----alphabeticSuffix----");
        while (i < table_alphabeticSuffix.length) {
            System.out.println("    " + FontConfiguration.getString(table_elcIDs[table_alphabeticSuffix[i++]]) + " -> " + FontConfiguration.getString(table_alphabeticSuffix[i++]));
        }
        System.out.println("\n----String Table---------");
        System.out.println("    stringID:    Num =" + table_stringIDs.length);
        System.out.println("    stringTable: Size=" + table_stringTable.length * 2);
        System.out.println("\n----fallbackScriptIDs---");
        short[] fbsIDs = FontConfiguration.getShortArray(head[15]);
        for (int ii2 = 0; ii2 < fbsIDs.length; ++ii2) {
            System.out.println("  " + FontConfiguration.getString(table_scriptIDs[fbsIDs[ii2]]));
        }
        System.out.println("\n----appendedfontpath-----");
        System.out.println("  " + FontConfiguration.getString(head[16]));
        System.out.println("\n----Version--------------");
        System.out.println("  " + FontConfiguration.getString(head[17]));
    }

    protected static short getComponentFontID(short scriptID, int fontIndex, int styleIndex) {
        short fid = table_scriptFonts[scriptID];
        if (fid >= 0) {
            return fid;
        }
        return table_scriptFonts[-fid + fontIndex * 4 + styleIndex];
    }

    protected static short getComponentFontIDMotif(short scriptID, int fontIndex, int styleIndex) {
        if (table_scriptFontsMotif.length == 0) {
            return 0;
        }
        short fid = table_scriptFontsMotif[scriptID];
        if (fid >= 0) {
            return fid;
        }
        return table_scriptFontsMotif[-fid + fontIndex * 4 + styleIndex];
    }

    private static int[] getExclusionRanges(short scriptID) {
        short exID = table_exclusions[scriptID];
        if (exID == 0) {
            return EMPTY_INT_ARRAY;
        }
        char[] exChar = FontConfiguration.getString(exID).toCharArray();
        int[] exInt = new int[exChar.length / 2];
        int i = 0;
        for (int j = 0; j < exInt.length; ++j) {
            exInt[j] = (exChar[i++] << 16) + (exChar[i++] & 0xFFFF);
        }
        return exInt;
    }

    private static boolean contains(short[] IDs, short id, int limit) {
        for (int i = 0; i < limit; ++i) {
            if (IDs[i] != id) continue;
            return true;
        }
        return false;
    }

    protected static String getComponentFontName(short id) {
        if (id < 0) {
            return null;
        }
        return FontConfiguration.getString(table_componentFontNameIDs[id]);
    }

    private static String getComponentFileName(short id) {
        if (id < 0) {
            return null;
        }
        return FontConfiguration.getString(table_fontfileNameIDs[id]);
    }

    private static short getComponentFileID(short nameID) {
        return table_filenames[nameID];
    }

    private static String getScriptName(short scriptID) {
        return FontConfiguration.getString(table_scriptIDs[scriptID]);
    }

    protected short[] getCoreScripts(int fontIndex) {
        short elc = this.getInitELC();
        short[] scripts = FontConfiguration.getShortArray(table_sequences[elc * 5 + fontIndex]);
        if (this.preferLocaleFonts) {
            int i;
            if (this.reorderScripts == null) {
                this.reorderScripts = new HashMap();
            }
            String[] ss = new String[scripts.length];
            for (i = 0; i < ss.length; ++i) {
                ss[i] = FontConfiguration.getScriptName(scripts[i]);
                this.reorderScripts.put(ss[i], scripts[i]);
            }
            this.reorderSequenceForLocale(ss);
            for (i = 0; i < ss.length; ++i) {
                scripts[i] = this.reorderScripts.get(ss[i]);
            }
        }
        return scripts;
    }

    private static short[] getFallbackScripts() {
        return FontConfiguration.getShortArray(head[15]);
    }

    private static void printTable(short[] list, int start) {
        for (int i = start; i < list.length; ++i) {
            System.out.println("  " + i + " : " + FontConfiguration.getString(list[i]));
        }
    }

    private static short[] readShortTable(DataInputStream in, int len) throws IOException {
        if (len == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        short[] data = new short[len];
        byte[] bb = new byte[len * 2];
        in.read(bb);
        int i = 0;
        int j = 0;
        while (i < len) {
            data[i++] = (short)(bb[j++] << 8 | bb[j++] & 0xFF);
        }
        return data;
    }

    private static void writeShortTable(DataOutputStream out, short[] data) throws IOException {
        for (short val : data) {
            out.writeShort(val);
        }
    }

    private static short[] toList(HashMap<String, Short> map) {
        short[] list = new short[map.size()];
        Arrays.fill(list, (short)-1);
        for (Map.Entry<String, Short> entry : map.entrySet()) {
            list[entry.getValue().shortValue()] = FontConfiguration.getStringID(entry.getKey());
        }
        return list;
    }

    protected static String getString(short stringID) {
        if (stringID == 0) {
            return null;
        }
        if (stringCache[stringID] == null) {
            FontConfiguration.stringCache[stringID] = new String(table_stringTable, (int)table_stringIDs[stringID], table_stringIDs[stringID + 1] - table_stringIDs[stringID]);
        }
        return stringCache[stringID];
    }

    private static short[] getShortArray(short shortArrayID) {
        String s = FontConfiguration.getString(shortArrayID);
        char[] cc = s.toCharArray();
        short[] ss = new short[cc.length];
        for (int i = 0; i < cc.length; ++i) {
            ss[i] = (short)(cc[i] & 0xFFFF);
        }
        return ss;
    }

    private static short getStringID(String s) {
        if (s == null) {
            return 0;
        }
        short pos0 = (short)stringTable.length();
        stringTable.append(s);
        short pos1 = (short)stringTable.length();
        FontConfiguration.stringIDs[FontConfiguration.stringIDNum] = pos0;
        FontConfiguration.stringIDs[FontConfiguration.stringIDNum + 1] = pos1;
        stringIDNum = (short)(stringIDNum + 1);
        if (stringIDNum + 1 >= stringIDs.length) {
            short[] tmp = new short[stringIDNum + 1000];
            System.arraycopy(stringIDs, 0, tmp, 0, stringIDNum);
            stringIDs = tmp;
        }
        return (short)(stringIDNum - 1);
    }

    private static short getShortArrayID(short[] sa) {
        char[] cc = new char[sa.length];
        for (int i = 0; i < sa.length; ++i) {
            cc[i] = (char)sa[i];
        }
        String s = new String(cc);
        return FontConfiguration.getStringID(s);
    }

    static {
        startupLocale = null;
        localeMap = null;
        isProperties = true;
        fontNames = new String[]{"serif", "sansserif", "monospaced", "dialog", "dialoginput"};
        publicFontNames = new String[]{"Serif", "SansSerif", "Monospaced", "Dialog", "DialogInput"};
        styleNames = new String[]{"plain", "bold", "italic", "bolditalic"};
        installedFallbackFontFiles = null;
        EMPTY_INT_ARRAY = new int[0];
        EMPTY_STRING_ARRAY = new String[0];
        EMPTY_SHORT_ARRAY = new short[0];
    }

    static class PropertiesHandler {
        private HashMap<String, Short> scriptIDs;
        private HashMap<String, Short> elcIDs;
        private HashMap<String, Short> componentFontNameIDs;
        private HashMap<String, Short> fontfileNameIDs;
        private HashMap<String, Integer> logicalFontIDs;
        private HashMap<String, Integer> fontStyleIDs;
        private HashMap<Short, Short> filenames;
        private HashMap<Short, short[]> sequences;
        private HashMap<Short, Short[]> scriptFonts;
        private HashMap<Short, Short> scriptAllfonts;
        private HashMap<Short, int[]> exclusions;
        private HashMap<Short, Short> awtfontpaths;
        private HashMap<Short, Short> proportionals;
        private HashMap<Short, Short> scriptAllfontsMotif;
        private HashMap<Short, Short[]> scriptFontsMotif;
        private HashMap<Short, Short> alphabeticSuffix;
        private short[] fallbackScriptIDs;
        private String version;
        private String appendedfontpath;

        PropertiesHandler() {
        }

        public void load(InputStream in) throws IOException {
            this.initLogicalNameStyle();
            this.initHashMaps();
            FontProperties fp = new FontProperties();
            fp.load(in);
            this.initBinaryTable();
        }

        private void initBinaryTable() {
            int i;
            head = new short[20];
            FontConfiguration.head[0] = 20;
            table_scriptIDs = FontConfiguration.toList(this.scriptIDs);
            FontConfiguration.head[1] = (short)(head[0] + table_scriptIDs.length);
            int len = table_scriptIDs.length + this.scriptFonts.size() * 20;
            table_scriptFonts = new short[len];
            for (Map.Entry<Short, Short> entry : this.scriptAllfonts.entrySet()) {
                FontConfiguration.table_scriptFonts[entry.getKey().intValue()] = entry.getValue();
            }
            int off = table_scriptIDs.length;
            for (Map.Entry<Short, Short[]> entry : this.scriptFonts.entrySet()) {
                FontConfiguration.table_scriptFonts[entry.getKey().intValue()] = (short)(-off);
                Short[] shortArray = entry.getValue();
                for (int i2 = 0; i2 < 20; ++i2) {
                    FontConfiguration.table_scriptFonts[off++] = shortArray[i2] != null ? shortArray[i2] : (short)0;
                }
            }
            FontConfiguration.head[2] = (short)(head[1] + table_scriptFonts.length);
            table_elcIDs = FontConfiguration.toList(this.elcIDs);
            FontConfiguration.head[3] = (short)(head[2] + table_elcIDs.length);
            table_sequences = new short[this.elcIDs.size() * 5];
            for (Map.Entry<Short, short[]> entry : this.sequences.entrySet()) {
                int n = entry.getKey().intValue();
                short[] v = entry.getValue();
                if (v.length == 1) {
                    for (i = 0; i < 5; ++i) {
                        FontConfiguration.table_sequences[n * 5 + i] = v[0];
                    }
                    continue;
                }
                for (i = 0; i < 5; ++i) {
                    FontConfiguration.table_sequences[n * 5 + i] = v[i];
                }
            }
            FontConfiguration.head[4] = (short)(head[3] + table_sequences.length);
            table_fontfileNameIDs = FontConfiguration.toList(this.fontfileNameIDs);
            FontConfiguration.head[5] = (short)(head[4] + table_fontfileNameIDs.length);
            table_componentFontNameIDs = FontConfiguration.toList(this.componentFontNameIDs);
            FontConfiguration.head[6] = (short)(head[5] + table_componentFontNameIDs.length);
            table_filenames = new short[table_componentFontNameIDs.length];
            Arrays.fill(table_filenames, (short)-1);
            for (Map.Entry<Short, Short> entry : this.filenames.entrySet()) {
                FontConfiguration.table_filenames[entry.getKey().shortValue()] = entry.getValue();
            }
            FontConfiguration.head[7] = (short)(head[6] + table_filenames.length);
            table_awtfontpaths = new short[table_scriptIDs.length];
            for (Map.Entry<Short, Short> entry : this.awtfontpaths.entrySet()) {
                FontConfiguration.table_awtfontpaths[entry.getKey().shortValue()] = entry.getValue();
            }
            FontConfiguration.head[8] = (short)(head[7] + table_awtfontpaths.length);
            table_exclusions = new short[this.scriptIDs.size()];
            for (Map.Entry<Short, int[]> entry : this.exclusions.entrySet()) {
                int[] nArray = entry.getValue();
                char[] exC = new char[nArray.length * 2];
                int j = 0;
                for (int i3 = 0; i3 < nArray.length; ++i3) {
                    exC[j++] = (char)(nArray[i3] >> 16);
                    exC[j++] = (char)(nArray[i3] & 0xFFFF);
                }
                FontConfiguration.table_exclusions[entry.getKey().shortValue()] = FontConfiguration.getStringID(new String(exC));
            }
            FontConfiguration.head[9] = (short)(head[8] + table_exclusions.length);
            table_proportionals = new short[this.proportionals.size() * 2];
            boolean bl = false;
            for (Map.Entry<Short, Short> entry : this.proportionals.entrySet()) {
                FontConfiguration.table_proportionals[var3_11++] = entry.getKey();
                FontConfiguration.table_proportionals[var3_11++] = entry.getValue();
            }
            FontConfiguration.head[10] = (short)(head[9] + table_proportionals.length);
            if (this.scriptAllfontsMotif.size() != 0 || this.scriptFontsMotif.size() != 0) {
                len = table_scriptIDs.length + this.scriptFontsMotif.size() * 20;
                table_scriptFontsMotif = new short[len];
                for (Map.Entry<Short, Short> entry : this.scriptAllfontsMotif.entrySet()) {
                    FontConfiguration.table_scriptFontsMotif[entry.getKey().intValue()] = entry.getValue();
                }
                off = table_scriptIDs.length;
                for (Map.Entry<Short, Short[]> entry : this.scriptFontsMotif.entrySet()) {
                    FontConfiguration.table_scriptFontsMotif[entry.getKey().intValue()] = (short)(-off);
                    Short[] v = entry.getValue();
                    for (i = 0; i < 20; ++i) {
                        FontConfiguration.table_scriptFontsMotif[off++] = v[i] != null ? v[i] : (short)0;
                    }
                }
            } else {
                table_scriptFontsMotif = EMPTY_SHORT_ARRAY;
            }
            FontConfiguration.head[11] = (short)(head[10] + table_scriptFontsMotif.length);
            table_alphabeticSuffix = new short[this.alphabeticSuffix.size() * 2];
            boolean bl2 = false;
            for (Map.Entry<Short, Short> entry : this.alphabeticSuffix.entrySet()) {
                FontConfiguration.table_alphabeticSuffix[var3_13++] = entry.getKey();
                FontConfiguration.table_alphabeticSuffix[var3_13++] = entry.getValue();
            }
            FontConfiguration.head[15] = FontConfiguration.getShortArrayID(this.fallbackScriptIDs);
            FontConfiguration.head[16] = FontConfiguration.getStringID(this.appendedfontpath);
            FontConfiguration.head[17] = FontConfiguration.getStringID(this.version);
            FontConfiguration.head[12] = (short)(head[11] + table_alphabeticSuffix.length);
            table_stringIDs = new short[stringIDNum + 1];
            System.arraycopy(stringIDs, 0, table_stringIDs, 0, stringIDNum + 1);
            FontConfiguration.head[13] = (short)(head[12] + stringIDNum + 1);
            table_stringTable = stringTable.toString().toCharArray();
            FontConfiguration.head[14] = (short)(head[13] + stringTable.length());
            stringCache = new String[table_stringIDs.length];
        }

        private void initLogicalNameStyle() {
            this.logicalFontIDs = new HashMap();
            this.fontStyleIDs = new HashMap();
            this.logicalFontIDs.put("serif", 0);
            this.logicalFontIDs.put("sansserif", 1);
            this.logicalFontIDs.put("monospaced", 2);
            this.logicalFontIDs.put("dialog", 3);
            this.logicalFontIDs.put("dialoginput", 4);
            this.fontStyleIDs.put("plain", 0);
            this.fontStyleIDs.put("bold", 1);
            this.fontStyleIDs.put("italic", 2);
            this.fontStyleIDs.put("bolditalic", 3);
        }

        private void initHashMaps() {
            this.scriptIDs = new HashMap();
            this.elcIDs = new HashMap();
            this.componentFontNameIDs = new HashMap();
            this.componentFontNameIDs.put("", (short)0);
            this.fontfileNameIDs = new HashMap();
            this.filenames = new HashMap();
            this.sequences = new HashMap();
            this.scriptFonts = new HashMap();
            this.scriptAllfonts = new HashMap();
            this.exclusions = new HashMap();
            this.awtfontpaths = new HashMap();
            this.proportionals = new HashMap();
            this.scriptFontsMotif = new HashMap();
            this.scriptAllfontsMotif = new HashMap();
            this.alphabeticSuffix = new HashMap();
            this.fallbackScriptIDs = EMPTY_SHORT_ARRAY;
        }

        private int[] parseExclusions(String key, String exclusions) {
            if (exclusions == null) {
                return EMPTY_INT_ARRAY;
            }
            int numExclusions = 1;
            int pos = 0;
            while ((pos = exclusions.indexOf(44, pos)) != -1) {
                ++numExclusions;
                ++pos;
            }
            int[] exclusionRanges = new int[numExclusions * 2];
            pos = 0;
            int newPos = 0;
            int j = 0;
            while (j < numExclusions * 2) {
                int lo = 0;
                int up = 0;
                try {
                    newPos = exclusions.indexOf(45, pos);
                    String lower = exclusions.substring(pos, newPos);
                    pos = newPos + 1;
                    newPos = exclusions.indexOf(44, pos);
                    if (newPos == -1) {
                        newPos = exclusions.length();
                    }
                    String upper = exclusions.substring(pos, newPos);
                    pos = newPos + 1;
                    int lowerLength = lower.length();
                    int upperLength = upper.length();
                    if (lowerLength != 4 && lowerLength != 6 || upperLength != 4 && upperLength != 6) {
                        throw new Exception();
                    }
                    lo = Integer.parseInt(lower, 16);
                    if (lo > (up = Integer.parseInt(upper, 16))) {
                        throw new Exception();
                    }
                }
                catch (Exception e) {
                    if (FontUtilities.debugFonts() && logger != null) {
                        logger.config("Failed parsing " + key + " property of font configuration.");
                    }
                    return EMPTY_INT_ARRAY;
                }
                exclusionRanges[j++] = lo;
                exclusionRanges[j++] = up;
            }
            return exclusionRanges;
        }

        private Short getID(HashMap<String, Short> map, String key) {
            Short ret = map.get(key);
            if (ret == null) {
                map.put(key, (short)map.size());
                return map.get(key);
            }
            return ret;
        }

        /*
         * Enabled aggressive block sorting
         */
        private void parseProperty(String key, String value) {
            short[] sa;
            String[] ss;
            boolean has1252;
            boolean hasDefault;
            if (key.startsWith("filename.")) {
                if (!"MingLiU_HKSCS".equals(key = key.substring(9))) {
                    key = key.replace('_', ' ');
                }
                Short faceID = this.getID(this.componentFontNameIDs, key);
                Short fileID = this.getID(this.fontfileNameIDs, value);
                this.filenames.put(faceID, fileID);
                return;
            }
            if (key.startsWith("exclusion.")) {
                key = key.substring(10);
                this.exclusions.put(this.getID(this.scriptIDs, key), this.parseExclusions(key, value));
                return;
            }
            if (key.startsWith("sequence.")) {
                key = key.substring(9);
                hasDefault = false;
                has1252 = false;
                ss = FontConfiguration.splitSequence(value).toArray(EMPTY_STRING_ARRAY);
                sa = new short[ss.length];
            } else {
                if (key.startsWith("allfonts.")) {
                    if ((key = key.substring(9)).endsWith(".motif")) {
                        key = key.substring(0, key.length() - 6);
                        this.scriptAllfontsMotif.put(this.getID(this.scriptIDs, key), this.getID(this.componentFontNameIDs, value));
                        return;
                    }
                    this.scriptAllfonts.put(this.getID(this.scriptIDs, key), this.getID(this.componentFontNameIDs, value));
                    return;
                }
                if (key.startsWith("awtfontpath.")) {
                    key = key.substring(12);
                    this.awtfontpaths.put(this.getID(this.scriptIDs, key), FontConfiguration.getStringID(value));
                    return;
                }
                if ("version".equals(key)) {
                    this.version = value;
                    return;
                }
                if ("appendedfontpath".equals(key)) {
                    this.appendedfontpath = value;
                    return;
                }
                if (key.startsWith("proportional.")) {
                    key = key.substring(13).replace('_', ' ');
                    this.proportionals.put(this.getID(this.componentFontNameIDs, key), this.getID(this.componentFontNameIDs, value));
                    return;
                }
                boolean isMotif = false;
                int dot1 = key.indexOf(46);
                if (dot1 == -1) {
                    if (logger == null) return;
                    logger.config("Failed parsing " + key + " property of font configuration.");
                    return;
                }
                int dot2 = key.indexOf(46, dot1 + 1);
                if (dot2 == -1) {
                    if (logger == null) return;
                    logger.config("Failed parsing " + key + " property of font configuration.");
                    return;
                }
                if (key.endsWith(".motif")) {
                    key = key.substring(0, key.length() - 6);
                    isMotif = true;
                }
                Integer nameID = this.logicalFontIDs.get(key.substring(0, dot1));
                Integer styleID = this.fontStyleIDs.get(key.substring(dot1 + 1, dot2));
                Short scriptID = this.getID(this.scriptIDs, key.substring(dot2 + 1));
                if (nameID == null || styleID == null) {
                    if (logger == null) return;
                    logger.config("unrecognizable logicfont name/style at " + key);
                    return;
                }
                Short[] pnids = isMotif ? this.scriptFontsMotif.get(scriptID) : this.scriptFonts.get(scriptID);
                if (pnids == null) {
                    pnids = new Short[20];
                }
                pnids[nameID.intValue() * 4 + styleID.intValue()] = this.getID(this.componentFontNameIDs, value);
                if (isMotif) {
                    this.scriptFontsMotif.put(scriptID, pnids);
                    return;
                }
                this.scriptFonts.put(scriptID, pnids);
                return;
            }
            for (int i = 0; i < ss.length; ++i) {
                if ("alphabetic/default".equals(ss[i])) {
                    ss[i] = "alphabetic";
                    hasDefault = true;
                } else if ("alphabetic/1252".equals(ss[i])) {
                    ss[i] = "alphabetic";
                    has1252 = true;
                }
                sa[i] = this.getID(this.scriptIDs, ss[i]);
            }
            short scriptArrayID = FontConfiguration.getShortArrayID(sa);
            Short elcID = null;
            int dot = key.indexOf(46);
            if (dot == -1) {
                if ("fallback".equals(key)) {
                    this.fallbackScriptIDs = sa;
                    return;
                }
                if (!"allfonts".equals(key)) {
                    if (logger == null) return;
                    logger.config("Error sequence def: <sequence." + key + ">");
                    return;
                }
                elcID = this.getID(this.elcIDs, "NULL.NULL.NULL");
            } else {
                elcID = this.getID(this.elcIDs, key.substring(dot + 1));
                key = key.substring(0, dot);
            }
            short[] scriptArrayIDs = null;
            if ("allfonts".equals(key)) {
                scriptArrayIDs = new short[]{scriptArrayID};
            } else {
                Integer fid;
                scriptArrayIDs = this.sequences.get(elcID);
                if (scriptArrayIDs == null) {
                    scriptArrayIDs = new short[5];
                }
                if ((fid = this.logicalFontIDs.get(key)) == null) {
                    if (logger == null) return;
                    logger.config("Unrecognizable logicfont name " + key);
                    return;
                }
                scriptArrayIDs[fid.intValue()] = scriptArrayID;
            }
            this.sequences.put(elcID, scriptArrayIDs);
            if (hasDefault) {
                this.alphabeticSuffix.put(elcID, FontConfiguration.getStringID("default"));
                return;
            }
            if (!has1252) return;
            this.alphabeticSuffix.put(elcID, FontConfiguration.getStringID("1252"));
        }

        class FontProperties
        extends Properties {
            FontProperties() {
            }

            @Override
            public synchronized Object put(Object k, Object v) {
                PropertiesHandler.this.parseProperty((String)k, (String)v);
                return null;
            }
        }
    }
}

