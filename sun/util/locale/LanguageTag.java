/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import sun.util.locale.BaseLocale;
import sun.util.locale.Extension;
import sun.util.locale.LocaleExtensions;
import sun.util.locale.LocaleUtils;
import sun.util.locale.ParseStatus;
import sun.util.locale.StringTokenIterator;

public class LanguageTag {
    public static final String SEP = "-";
    public static final String PRIVATEUSE = "x";
    public static final String UNDETERMINED = "und";
    public static final String PRIVUSE_VARIANT_PREFIX = "lvariant";
    private String language = "";
    private String script = "";
    private String region = "";
    private String privateuse = "";
    private List<String> extlangs = Collections.emptyList();
    private List<String> variants = Collections.emptyList();
    private List<String> extensions = Collections.emptyList();
    private static final Map<String, String[]> LEGACY;

    private LanguageTag() {
    }

    public static LanguageTag parse(String languageTag, ParseStatus sts) {
        if (sts == null) {
            sts = new ParseStatus();
        } else {
            sts.reset();
        }
        String[] gfmap = LEGACY.get(LocaleUtils.toLowerString(languageTag));
        StringTokenIterator itr = gfmap != null ? new StringTokenIterator(gfmap[1], SEP) : new StringTokenIterator(languageTag, SEP);
        LanguageTag tag = new LanguageTag();
        if (tag.parseLanguage(itr, sts)) {
            tag.parseExtlangs(itr, sts);
            tag.parseScript(itr, sts);
            tag.parseRegion(itr, sts);
            tag.parseVariants(itr, sts);
            tag.parseExtensions(itr, sts);
        }
        tag.parsePrivateuse(itr, sts);
        if (!itr.isDone() && !sts.isError()) {
            String s = itr.current();
            sts.errorIndex = itr.currentStart();
            sts.errorMsg = s.isEmpty() ? "Empty subtag" : "Invalid subtag: " + s;
        }
        return tag;
    }

    private boolean parseLanguage(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (LanguageTag.isLanguage(s)) {
            found = true;
            this.language = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseExtlangs(StringTokenIterator itr, ParseStatus sts) {
        String s;
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone() && LanguageTag.isExtlang(s = itr.current())) {
            found = true;
            if (this.extlangs.isEmpty()) {
                this.extlangs = new ArrayList<String>(3);
            }
            this.extlangs.add(s);
            sts.parseLength = itr.currentEnd();
            itr.next();
            if (this.extlangs.size() != 3) continue;
            break;
        }
        return found;
    }

    private boolean parseScript(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (LanguageTag.isScript(s)) {
            found = true;
            this.script = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseRegion(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (LanguageTag.isRegion(s)) {
            found = true;
            this.region = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseVariants(StringTokenIterator itr, ParseStatus sts) {
        String s;
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone() && LanguageTag.isVariant(s = itr.current())) {
            found = true;
            if (this.variants.isEmpty()) {
                this.variants = new ArrayList<String>(3);
            }
            this.variants.add(s);
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseExtensions(StringTokenIterator itr, ParseStatus sts) {
        String s;
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone() && LanguageTag.isExtensionSingleton(s = itr.current())) {
            int start = itr.currentStart();
            String singleton = s;
            StringBuilder sb = new StringBuilder(singleton);
            itr.next();
            while (!itr.isDone() && LanguageTag.isExtensionSubtag(s = itr.current())) {
                sb.append(SEP).append(s);
                sts.parseLength = itr.currentEnd();
                itr.next();
            }
            if (sts.parseLength <= start) {
                sts.errorIndex = start;
                sts.errorMsg = "Incomplete extension '" + singleton + "'";
                break;
            }
            if (this.extensions.isEmpty()) {
                this.extensions = new ArrayList<String>(4);
            }
            this.extensions.add(sb.toString());
            found = true;
        }
        return found;
    }

    private boolean parsePrivateuse(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (LanguageTag.isPrivateusePrefix(s)) {
            int start = itr.currentStart();
            StringBuilder sb = new StringBuilder(s);
            itr.next();
            while (!itr.isDone() && LanguageTag.isPrivateuseSubtag(s = itr.current())) {
                sb.append(SEP).append(s);
                sts.parseLength = itr.currentEnd();
                itr.next();
            }
            if (sts.parseLength <= start) {
                sts.errorIndex = start;
                sts.errorMsg = "Incomplete privateuse";
            } else {
                this.privateuse = sb.toString();
                found = true;
            }
        }
        return found;
    }

    public static String caseFoldTag(String tag) {
        ParseStatus sts = new ParseStatus();
        LanguageTag.parse(tag, sts);
        if (sts.errorMsg != null) {
            throw new IllformedLocaleException(String.format("Ill formed tag: %s", sts.errorMsg));
        }
        String potentialLegacy = tag.toLowerCase(Locale.ROOT);
        if (LEGACY.containsKey(potentialLegacy)) {
            return LEGACY.get(potentialLegacy)[0];
        }
        StringBuilder bldr = new StringBuilder(tag.length());
        String[] subtags = tag.split(SEP);
        boolean privateFound = false;
        boolean singletonFound = false;
        boolean privUseVarFound = false;
        for (int i = 0; i < subtags.length; ++i) {
            String subtag = subtags[i];
            if (privUseVarFound) {
                bldr.append(subtag);
            } else if (i > 0 && LanguageTag.isVariant(subtag) && !singletonFound && !privateFound) {
                bldr.append(subtag);
            } else if (i > 0 && LanguageTag.isRegion(subtag) && !singletonFound && !privateFound) {
                bldr.append(LanguageTag.canonicalizeRegion(subtag));
            } else if (i > 0 && LanguageTag.isScript(subtag) && !singletonFound && !privateFound) {
                bldr.append(LanguageTag.canonicalizeScript(subtag));
            } else {
                if (LanguageTag.isPrivateusePrefix(subtag)) {
                    privateFound = true;
                } else if (LanguageTag.isExtensionSingleton(subtag)) {
                    singletonFound = true;
                } else if (subtag.equals(PRIVUSE_VARIANT_PREFIX)) {
                    privUseVarFound = true;
                }
                bldr.append(subtag.toLowerCase(Locale.ROOT));
            }
            if (i == subtags.length - 1) continue;
            bldr.append(SEP);
        }
        return bldr.substring(0);
    }

    public static LanguageTag parseLocale(BaseLocale baseLocale, LocaleExtensions localeExtensions) {
        LanguageTag tag = new LanguageTag();
        String language = baseLocale.getLanguage();
        String script = baseLocale.getScript();
        String region = baseLocale.getRegion();
        String variant = baseLocale.getVariant();
        boolean hasSubtag = false;
        String privuseVar = null;
        if (LanguageTag.isLanguage(language)) {
            if (language.equals("iw")) {
                language = "he";
            } else if (language.equals("ji")) {
                language = "yi";
            } else if (language.equals("in")) {
                language = "id";
            }
            tag.language = language;
        }
        if (LanguageTag.isScript(script)) {
            tag.script = LanguageTag.canonicalizeScript(script);
            hasSubtag = true;
        }
        if (LanguageTag.isRegion(region)) {
            tag.region = LanguageTag.canonicalizeRegion(region);
            hasSubtag = true;
        }
        if (tag.language.equals("no") && tag.region.equals("NO") && variant.equals("NY")) {
            tag.language = "nn";
            variant = "";
        }
        if (!variant.isEmpty()) {
            String var;
            ArrayList<String> variants = null;
            StringTokenIterator varitr = new StringTokenIterator(variant, "_");
            while (!varitr.isDone() && LanguageTag.isVariant(var = varitr.current())) {
                if (variants == null) {
                    variants = new ArrayList<String>();
                }
                variants.add(var);
                varitr.next();
            }
            if (variants != null) {
                tag.variants = variants;
                hasSubtag = true;
            }
            if (!varitr.isDone()) {
                String prvv;
                StringJoiner sj = new StringJoiner(SEP);
                while (!varitr.isDone() && LanguageTag.isPrivateuseSubtag(prvv = varitr.current())) {
                    sj.add(prvv);
                    varitr.next();
                }
                if (sj.length() > 0) {
                    privuseVar = sj.toString();
                }
            }
        }
        ArrayList<String> extensions = null;
        String privateuse = null;
        if (localeExtensions != null) {
            Set<Character> locextKeys = localeExtensions.getKeys();
            for (Character locextKey : locextKeys) {
                Extension ext = localeExtensions.getExtension(locextKey);
                if (LanguageTag.isPrivateusePrefixChar(locextKey.charValue())) {
                    privateuse = ext.getValue();
                    continue;
                }
                if (extensions == null) {
                    extensions = new ArrayList<String>();
                }
                extensions.add(locextKey.toString() + SEP + ext.getValue());
            }
        }
        if (extensions != null) {
            tag.extensions = extensions;
            hasSubtag = true;
        }
        if (privuseVar != null) {
            privateuse = privateuse == null ? "lvariant-" + privuseVar : privateuse + SEP + PRIVUSE_VARIANT_PREFIX + SEP + privuseVar.replace("_", SEP);
        }
        if (privateuse != null) {
            tag.privateuse = privateuse;
        }
        if (tag.language.isEmpty() && (hasSubtag || privateuse == null)) {
            tag.language = UNDETERMINED;
        }
        return tag;
    }

    public String getLanguage() {
        return this.language;
    }

    public List<String> getExtlangs() {
        if (this.extlangs.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.extlangs);
    }

    public String getScript() {
        return this.script;
    }

    public String getRegion() {
        return this.region;
    }

    public List<String> getVariants() {
        if (this.variants.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.variants);
    }

    public List<String> getExtensions() {
        if (this.extensions.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.extensions);
    }

    public String getPrivateuse() {
        return this.privateuse;
    }

    public static boolean isLanguage(String s) {
        int len = s.length();
        return len >= 2 && len <= 8 && LocaleUtils.isAlphaString(s);
    }

    public static boolean isExtlang(String s) {
        return s.length() == 3 && LocaleUtils.isAlphaString(s);
    }

    public static boolean isScript(String s) {
        return s.length() == 4 && LocaleUtils.isAlphaString(s);
    }

    public static boolean isRegion(String s) {
        return s.length() == 2 && LocaleUtils.isAlphaString(s) || s.length() == 3 && LocaleUtils.isNumericString(s);
    }

    public static boolean isVariant(String s) {
        int len = s.length();
        if (len >= 5 && len <= 8) {
            return LocaleUtils.isAlphaNumericString(s);
        }
        if (len == 4) {
            return LocaleUtils.isNumeric(s.charAt(0)) && LocaleUtils.isAlphaNumeric(s.charAt(1)) && LocaleUtils.isAlphaNumeric(s.charAt(2)) && LocaleUtils.isAlphaNumeric(s.charAt(3));
        }
        return false;
    }

    public static boolean isExtensionSingleton(String s) {
        return s.length() == 1 && LocaleUtils.isAlphaString(s) && !LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s);
    }

    public static boolean isExtensionSingletonChar(char c) {
        return LanguageTag.isExtensionSingleton(String.valueOf(c));
    }

    public static boolean isExtensionSubtag(String s) {
        int len = s.length();
        return len >= 2 && len <= 8 && LocaleUtils.isAlphaNumericString(s);
    }

    public static boolean isPrivateusePrefix(String s) {
        return s.length() == 1 && LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s);
    }

    public static boolean isPrivateusePrefixChar(char c) {
        return LocaleUtils.caseIgnoreMatch(PRIVATEUSE, String.valueOf(c));
    }

    public static boolean isPrivateuseSubtag(String s) {
        int len = s.length();
        return len >= 1 && len <= 8 && LocaleUtils.isAlphaNumericString(s);
    }

    public static String canonicalizeLanguage(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtlang(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeScript(String s) {
        return LocaleUtils.toTitleString(s);
    }

    public static String canonicalizeRegion(String s) {
        return LocaleUtils.toUpperString(s);
    }

    public static String canonicalizeVariant(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtension(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtensionSingleton(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtensionSubtag(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizePrivateuse(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizePrivateuseSubtag(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!this.language.isEmpty()) {
            sb.append(this.language);
            for (String extlang : this.extlangs) {
                sb.append(SEP).append(extlang);
            }
            if (!this.script.isEmpty()) {
                sb.append(SEP).append(this.script);
            }
            if (!this.region.isEmpty()) {
                sb.append(SEP).append(this.region);
            }
            for (String variant : this.variants) {
                sb.append(SEP).append(variant);
            }
            for (String extension : this.extensions) {
                sb.append(SEP).append(extension);
            }
        }
        if (!this.privateuse.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(SEP);
            }
            sb.append(this.privateuse);
        }
        return sb.toString();
    }

    static {
        String[][] entries;
        LEGACY = new HashMap<String, String[]>();
        for (String[] e : entries = new String[][]{{"art-lojban", "jbo"}, {"cel-gaulish", "xtg-x-cel-gaulish"}, {"en-GB-oed", "en-GB-x-oed"}, {"i-ami", "ami"}, {"i-bnn", "bnn"}, {"i-default", "en-x-i-default"}, {"i-enochian", "und-x-i-enochian"}, {"i-hak", "hak"}, {"i-klingon", "tlh"}, {"i-lux", "lb"}, {"i-mingo", "see-x-i-mingo"}, {"i-navajo", "nv"}, {"i-pwn", "pwn"}, {"i-tao", "tao"}, {"i-tay", "tay"}, {"i-tsu", "tsu"}, {"no-bok", "nb"}, {"no-nyn", "nn"}, {"sgn-BE-FR", "sfb"}, {"sgn-BE-NL", "vgt"}, {"sgn-CH-DE", "sgg"}, {"zh-guoyu", "cmn"}, {"zh-hakka", "hak"}, {"zh-min", "nan-x-zh-min"}, {"zh-min-nan", "nan"}, {"zh-xiang", "hsn"}}) {
            LEGACY.put(LocaleUtils.toLowerString(e[0]), e);
        }
    }
}

