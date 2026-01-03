/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Hashtable;
import sun.awt.FontConfiguration;
import sun.awt.FontDescriptor;
import sun.awt.windows.WDefaultFontCharset;
import sun.font.SunFontManager;

public final class WFontConfiguration
extends FontConfiguration {
    private boolean useCompatibilityFallbacks = "windows-1252".equals(encoding);
    private static HashMap<String, String> subsetCharsetMap = new HashMap();
    private static HashMap<String, String> subsetEncodingMap = new HashMap();
    private static String textInputCharset = "DEFAULT_CHARSET";

    public WFontConfiguration(SunFontManager fm) {
        super(fm);
        this.initTables(encoding);
    }

    public WFontConfiguration(SunFontManager fm, boolean preferLocaleFonts, boolean preferPropFonts) {
        super(fm, preferLocaleFonts, preferPropFonts);
    }

    @Override
    protected void initReorderMap() {
        if (encoding.equalsIgnoreCase("windows-31j") || encoding.equalsIgnoreCase("UTF-8") && startupLocale.getLanguage().equals("ja")) {
            localeMap = new Hashtable();
            localeMap.put("dialoginput.plain.japanese", "MS Mincho");
            localeMap.put("dialoginput.bold.japanese", "MS Mincho");
            localeMap.put("dialoginput.italic.japanese", "MS Mincho");
            localeMap.put("dialoginput.bolditalic.japanese", "MS Mincho");
        }
        this.reorderMap = new HashMap();
        this.reorderMap.put("UTF-8.hi", "devanagari");
        this.reorderMap.put("windows-1255", "hebrew");
        this.reorderMap.put("x-windows-874", "thai");
        this.reorderMap.put("windows-31j", "japanese");
        this.reorderMap.put("x-windows-949", "korean");
        this.reorderMap.put("GBK", "chinese-ms936");
        this.reorderMap.put("GB18030", "chinese-gb18030");
        this.reorderMap.put("x-windows-950", "chinese-ms950");
        this.reorderMap.put("x-MS950-HKSCS", new String[]{"chinese-ms950", "chinese-hkscs"});
    }

    @Override
    protected void setOsNameAndVersion() {
        super.setOsNameAndVersion();
        if (osName.startsWith("Windows")) {
            int q;
            int p = osName.indexOf(32);
            osName = p == -1 ? null : ((q = osName.indexOf(32, p + 1)) == -1 ? osName.substring(p + 1) : osName.substring(p + 1, q));
            osVersion = null;
        }
    }

    @Override
    public String getFallbackFamilyName(String fontName, String defaultFallback) {
        String compatibilityName;
        if (this.useCompatibilityFallbacks && (compatibilityName = this.getCompatibilityFamilyName(fontName)) != null) {
            return compatibilityName;
        }
        return defaultFallback;
    }

    @Override
    protected String makeAWTFontName(String platformFontName, String characterSubsetName) {
        String windowsCharset = subsetCharsetMap.get(characterSubsetName);
        if (windowsCharset == null) {
            windowsCharset = "DEFAULT_CHARSET";
        }
        return platformFontName + "," + windowsCharset;
    }

    @Override
    protected String getEncoding(String awtFontName, String characterSubsetName) {
        String encoding = subsetEncodingMap.get(characterSubsetName);
        if (encoding == null) {
            encoding = "default";
        }
        return encoding;
    }

    @Override
    protected Charset getDefaultFontCharset(String fontName) {
        return new WDefaultFontCharset(fontName);
    }

    @Override
    public String getFaceNameFromComponentFontName(String componentFontName) {
        return componentFontName;
    }

    @Override
    protected String getFileNameFromComponentFontName(String componentFontName) {
        return this.getFileNameFromPlatformName(componentFontName);
    }

    public String getTextComponentFontName(String familyName, int style) {
        FontDescriptor[] fontDescriptors = this.getFontDescriptors(familyName, style);
        String fontName = this.findFontWithCharset(fontDescriptors, textInputCharset);
        if (fontName == null && !textInputCharset.equals("DEFAULT_CHARSET")) {
            fontName = this.findFontWithCharset(fontDescriptors, "DEFAULT_CHARSET");
        }
        if (fontName == null) {
            fontName = fontDescriptors.length > 0 ? fontDescriptors[0].getNativeName() : "Arial,ANSI_CHARSET";
        }
        return fontName;
    }

    private String findFontWithCharset(FontDescriptor[] fontDescriptors, String charset) {
        String fontName = null;
        for (int i = 0; i < fontDescriptors.length; ++i) {
            String componentFontName = fontDescriptors[i].getNativeName();
            if (!componentFontName.endsWith(charset)) continue;
            fontName = componentFontName;
            break;
        }
        return fontName;
    }

    private void initTables(String defaultEncoding) {
        subsetCharsetMap.put("alphabetic", "ANSI_CHARSET");
        subsetCharsetMap.put("alphabetic/1252", "ANSI_CHARSET");
        subsetCharsetMap.put("alphabetic/default", "DEFAULT_CHARSET");
        subsetCharsetMap.put("arabic", "ARABIC_CHARSET");
        subsetCharsetMap.put("chinese-ms936", "GB2312_CHARSET");
        subsetCharsetMap.put("chinese-gb18030", "GB2312_CHARSET");
        subsetCharsetMap.put("chinese-ms950", "CHINESEBIG5_CHARSET");
        subsetCharsetMap.put("chinese-hkscs", "CHINESEBIG5_CHARSET");
        subsetCharsetMap.put("cyrillic", "RUSSIAN_CHARSET");
        subsetCharsetMap.put("devanagari", "DEFAULT_CHARSET");
        subsetCharsetMap.put("dingbats", "SYMBOL_CHARSET");
        subsetCharsetMap.put("greek", "GREEK_CHARSET");
        subsetCharsetMap.put("hebrew", "HEBREW_CHARSET");
        subsetCharsetMap.put("japanese", "SHIFTJIS_CHARSET");
        subsetCharsetMap.put("korean", "HANGEUL_CHARSET");
        subsetCharsetMap.put("latin", "ANSI_CHARSET");
        subsetCharsetMap.put("symbol", "SYMBOL_CHARSET");
        subsetCharsetMap.put("thai", "THAI_CHARSET");
        subsetEncodingMap.put("alphabetic", "default");
        subsetEncodingMap.put("alphabetic/1252", "windows-1252");
        subsetEncodingMap.put("alphabetic/default", defaultEncoding);
        subsetEncodingMap.put("arabic", "windows-1256");
        subsetEncodingMap.put("chinese-ms936", "GBK");
        subsetEncodingMap.put("chinese-gb18030", "GB18030");
        if ("x-MS950-HKSCS".equals(defaultEncoding)) {
            subsetEncodingMap.put("chinese-ms950", "x-MS950-HKSCS");
        } else {
            subsetEncodingMap.put("chinese-ms950", "x-windows-950");
        }
        subsetEncodingMap.put("chinese-hkscs", "sun.awt.HKSCS");
        subsetEncodingMap.put("cyrillic", "windows-1251");
        subsetEncodingMap.put("devanagari", "UTF-16LE");
        subsetEncodingMap.put("dingbats", "sun.awt.windows.WingDings");
        subsetEncodingMap.put("greek", "windows-1253");
        subsetEncodingMap.put("hebrew", "windows-1255");
        subsetEncodingMap.put("japanese", "windows-31j");
        subsetEncodingMap.put("korean", "x-windows-949");
        subsetEncodingMap.put("latin", "windows-1252");
        subsetEncodingMap.put("symbol", "sun.awt.Symbol");
        subsetEncodingMap.put("thai", "x-windows-874");
        if ("windows-1256".equals(defaultEncoding)) {
            textInputCharset = "ARABIC_CHARSET";
        } else if ("GBK".equals(defaultEncoding)) {
            textInputCharset = "GB2312_CHARSET";
        } else if ("GB18030".equals(defaultEncoding)) {
            textInputCharset = "GB2312_CHARSET";
        } else if ("x-windows-950".equals(defaultEncoding)) {
            textInputCharset = "CHINESEBIG5_CHARSET";
        } else if ("x-MS950-HKSCS".equals(defaultEncoding)) {
            textInputCharset = "CHINESEBIG5_CHARSET";
        } else if ("windows-1251".equals(defaultEncoding)) {
            textInputCharset = "RUSSIAN_CHARSET";
        } else if ("windows-1253".equals(defaultEncoding)) {
            textInputCharset = "GREEK_CHARSET";
        } else if ("windows-1255".equals(defaultEncoding)) {
            textInputCharset = "HEBREW_CHARSET";
        } else if ("windows-31j".equals(defaultEncoding)) {
            textInputCharset = "SHIFTJIS_CHARSET";
        } else if ("x-windows-949".equals(defaultEncoding)) {
            textInputCharset = "HANGEUL_CHARSET";
        } else if ("x-windows-874".equals(defaultEncoding)) {
            textInputCharset = "THAI_CHARSET";
        } else if (defaultEncoding.startsWith("UTF-8")) {
            String lang = startupLocale.getLanguage();
            String country = startupLocale.getCountry();
            textInputCharset = switch (lang) {
                case "ar" -> "ARABIC_CHARSET";
                case "zh" -> {
                    switch (country) {
                        case "TW": 
                        case "HK": {
                            yield "CHINESEBIG5_CHARSET";
                        }
                    }
                    yield "GB2312_CHARSET";
                }
                case "ru" -> "RUSSIAN_CHARSET";
                case "el" -> "GREEK_CHARSET";
                case "iw", "he" -> "HEBREW_CHARSET";
                case "ja" -> "SHIFTJIS_CHARSET";
                case "ko" -> "HANGEUL_CHARSET";
                case "th" -> "THAI_CHARSET";
                default -> "DEFAULT_CHARSET";
            };
        }
    }
}

