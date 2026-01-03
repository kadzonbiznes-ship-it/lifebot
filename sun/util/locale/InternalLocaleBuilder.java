/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sun.util.locale.BaseLocale;
import sun.util.locale.Extension;
import sun.util.locale.LanguageTag;
import sun.util.locale.LocaleExtensions;
import sun.util.locale.LocaleSyntaxException;
import sun.util.locale.LocaleUtils;
import sun.util.locale.StringTokenIterator;
import sun.util.locale.UnicodeLocaleExtension;

public final class InternalLocaleBuilder {
    private static final CaseInsensitiveChar PRIVATEUSE_KEY = new CaseInsensitiveChar("x");
    private String language = "";
    private String script = "";
    private String region = "";
    private String variant = "";
    private Map<CaseInsensitiveChar, String> extensions;
    private Set<CaseInsensitiveString> uattributes;
    private Map<CaseInsensitiveString, String> ukeywords;

    public InternalLocaleBuilder setLanguage(String language) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(language)) {
            this.language = "";
        } else {
            if (!LanguageTag.isLanguage(language)) {
                throw new LocaleSyntaxException("Ill-formed language: " + language, 0);
            }
            this.language = language;
        }
        return this;
    }

    public InternalLocaleBuilder setScript(String script) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(script)) {
            this.script = "";
        } else {
            if (!LanguageTag.isScript(script)) {
                throw new LocaleSyntaxException("Ill-formed script: " + script, 0);
            }
            this.script = script;
        }
        return this;
    }

    public InternalLocaleBuilder setRegion(String region) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(region)) {
            this.region = "";
        } else {
            if (!LanguageTag.isRegion(region)) {
                throw new LocaleSyntaxException("Ill-formed region: " + region, 0);
            }
            this.region = region;
        }
        return this;
    }

    public InternalLocaleBuilder setVariant(String variant) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(variant)) {
            this.variant = "";
        } else {
            String var = variant.replaceAll("-", "_");
            int errIdx = this.checkVariants(var, "_");
            if (errIdx != -1) {
                throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
            }
            this.variant = var;
        }
        return this;
    }

    public InternalLocaleBuilder addUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (!UnicodeLocaleExtension.isAttribute(attribute)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
        }
        if (this.uattributes == null) {
            this.uattributes = new HashSet<CaseInsensitiveString>(4);
        }
        this.uattributes.add(new CaseInsensitiveString(attribute));
        return this;
    }

    public InternalLocaleBuilder removeUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (attribute == null || !UnicodeLocaleExtension.isAttribute(attribute)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
        }
        if (this.uattributes != null) {
            this.uattributes.remove(new CaseInsensitiveString(attribute));
        }
        return this;
    }

    public InternalLocaleBuilder setUnicodeLocaleKeyword(String key, String type) throws LocaleSyntaxException {
        if (!UnicodeLocaleExtension.isKey(key)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale keyword key: " + key);
        }
        CaseInsensitiveString cikey = new CaseInsensitiveString(key);
        if (type == null) {
            if (this.ukeywords != null) {
                this.ukeywords.remove(cikey);
            }
        } else {
            if (type.length() != 0) {
                String tp = type.replaceAll("_", "-");
                StringTokenIterator itr = new StringTokenIterator(tp, "-");
                while (!itr.isDone()) {
                    String s = itr.current();
                    if (!UnicodeLocaleExtension.isTypeSubtag(s)) {
                        throw new LocaleSyntaxException("Ill-formed Unicode locale keyword type: " + type, itr.currentStart());
                    }
                    itr.next();
                }
            }
            if (this.ukeywords == null) {
                this.ukeywords = new HashMap<CaseInsensitiveString, String>(4);
            }
            this.ukeywords.put(cikey, type);
        }
        return this;
    }

    public InternalLocaleBuilder setExtension(char singleton, String value) throws LocaleSyntaxException {
        boolean isBcpPrivateuse = LanguageTag.isPrivateusePrefixChar(singleton);
        if (!isBcpPrivateuse && !LanguageTag.isExtensionSingletonChar(singleton)) {
            throw new LocaleSyntaxException("Ill-formed extension key: " + singleton);
        }
        boolean remove = LocaleUtils.isEmpty(value);
        CaseInsensitiveChar key = new CaseInsensitiveChar(singleton);
        if (remove) {
            if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                if (this.uattributes != null) {
                    this.uattributes.clear();
                }
                if (this.ukeywords != null) {
                    this.ukeywords.clear();
                }
            } else if (this.extensions != null) {
                this.extensions.remove(key);
            }
        } else {
            String val = value.replaceAll("_", "-");
            StringTokenIterator itr = new StringTokenIterator(val, "-");
            while (!itr.isDone()) {
                String s = itr.current();
                boolean validSubtag = isBcpPrivateuse ? LanguageTag.isPrivateuseSubtag(s) : LanguageTag.isExtensionSubtag(s);
                if (!validSubtag) {
                    throw new LocaleSyntaxException("Ill-formed extension value: " + s, itr.currentStart());
                }
                itr.next();
            }
            if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                this.setUnicodeLocaleExtension(val);
            } else {
                if (this.extensions == null) {
                    this.extensions = new HashMap<CaseInsensitiveChar, String>(4);
                }
                this.extensions.put(key, val);
            }
        }
        return this;
    }

    public InternalLocaleBuilder setExtensions(String subtags) throws LocaleSyntaxException {
        int start;
        String s;
        if (LocaleUtils.isEmpty(subtags)) {
            this.clearExtensions();
            return this;
        }
        subtags = subtags.replaceAll("_", "-");
        StringTokenIterator itr = new StringTokenIterator(subtags, "-");
        ArrayList<String> extensions = null;
        String privateuse = null;
        int parsed = 0;
        while (!itr.isDone() && LanguageTag.isExtensionSingleton(s = itr.current())) {
            start = itr.currentStart();
            String singleton = s;
            StringBuilder sb = new StringBuilder(singleton);
            itr.next();
            while (!itr.isDone() && LanguageTag.isExtensionSubtag(s = itr.current())) {
                sb.append("-").append(s);
                parsed = itr.currentEnd();
                itr.next();
            }
            if (parsed < start) {
                throw new LocaleSyntaxException("Incomplete extension '" + singleton + "'", start);
            }
            if (extensions == null) {
                extensions = new ArrayList<String>(4);
            }
            extensions.add(sb.toString());
        }
        if (!itr.isDone() && LanguageTag.isPrivateusePrefix(s = itr.current())) {
            start = itr.currentStart();
            StringBuilder sb = new StringBuilder(s);
            itr.next();
            while (!itr.isDone() && LanguageTag.isPrivateuseSubtag(s = itr.current())) {
                sb.append("-").append(s);
                parsed = itr.currentEnd();
                itr.next();
            }
            if (parsed <= start) {
                throw new LocaleSyntaxException("Incomplete privateuse:" + subtags.substring(start), start);
            }
            privateuse = sb.toString();
        }
        if (!itr.isDone()) {
            throw new LocaleSyntaxException("Ill-formed extension subtags:" + subtags.substring(itr.currentStart()), itr.currentStart());
        }
        return this.setExtensions(extensions, privateuse);
    }

    private InternalLocaleBuilder setExtensions(List<String> bcpExtensions, String privateuse) {
        this.clearExtensions();
        if (!LocaleUtils.isEmpty(bcpExtensions)) {
            HashSet done = HashSet.newHashSet(bcpExtensions.size());
            for (String bcpExt : bcpExtensions) {
                CaseInsensitiveChar key = new CaseInsensitiveChar(bcpExt);
                if (!done.contains(key)) {
                    if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                        this.setUnicodeLocaleExtension(bcpExt.substring(2));
                    } else {
                        if (this.extensions == null) {
                            this.extensions = new HashMap<CaseInsensitiveChar, String>(4);
                        }
                        this.extensions.put(key, bcpExt.substring(2));
                    }
                }
                done.add(key);
            }
        }
        if (privateuse != null && !privateuse.isEmpty()) {
            if (this.extensions == null) {
                this.extensions = new HashMap<CaseInsensitiveChar, String>(1);
            }
            this.extensions.put(new CaseInsensitiveChar(privateuse), privateuse.substring(2));
        }
        return this;
    }

    public InternalLocaleBuilder setLanguageTag(LanguageTag langtag) {
        this.clear();
        if (!langtag.getExtlangs().isEmpty()) {
            this.language = langtag.getExtlangs().get(0);
        } else {
            String lang = langtag.getLanguage();
            if (!lang.equals("und")) {
                this.language = lang;
            }
        }
        this.script = langtag.getScript();
        this.region = langtag.getRegion();
        List<String> bcpVariants = langtag.getVariants();
        if (!bcpVariants.isEmpty()) {
            StringBuilder var = new StringBuilder(bcpVariants.get(0));
            int size = bcpVariants.size();
            for (int i = 1; i < size; ++i) {
                var.append("_").append(bcpVariants.get(i));
            }
            this.variant = var.toString();
        }
        this.setExtensions(langtag.getExtensions(), langtag.getPrivateuse());
        return this;
    }

    public InternalLocaleBuilder setLocale(BaseLocale base, LocaleExtensions localeExtensions) throws LocaleSyntaxException {
        Set<Character> extKeys;
        int errIdx;
        String language = base.getLanguage();
        String script = base.getScript();
        String region = base.getRegion();
        String variant = base.getVariant();
        if (language.equals("ja") && region.equals("JP") && variant.equals("JP")) {
            assert ("japanese".equals(localeExtensions.getUnicodeLocaleType("ca")));
            variant = "";
        } else if (language.equals("th") && region.equals("TH") && variant.equals("TH")) {
            assert ("thai".equals(localeExtensions.getUnicodeLocaleType("nu")));
            variant = "";
        } else if (language.equals("no") && region.equals("NO") && variant.equals("NY")) {
            language = "nn";
            variant = "";
        }
        if (!language.isEmpty() && !LanguageTag.isLanguage(language)) {
            throw new LocaleSyntaxException("Ill-formed language: " + language);
        }
        if (!script.isEmpty() && !LanguageTag.isScript(script)) {
            throw new LocaleSyntaxException("Ill-formed script: " + script);
        }
        if (!region.isEmpty() && !LanguageTag.isRegion(region)) {
            throw new LocaleSyntaxException("Ill-formed region: " + region);
        }
        if (!variant.isEmpty() && (errIdx = this.checkVariants(variant, "_")) != -1) {
            throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
        }
        this.language = language;
        this.script = script;
        this.region = region;
        this.variant = variant;
        this.clearExtensions();
        Set<Character> set = extKeys = localeExtensions == null ? null : localeExtensions.getKeys();
        if (extKeys != null) {
            for (Character key : extKeys) {
                Extension e = localeExtensions.getExtension(key);
                if (e instanceof UnicodeLocaleExtension) {
                    UnicodeLocaleExtension ue = (UnicodeLocaleExtension)e;
                    for (String uatr : ue.getUnicodeLocaleAttributes()) {
                        if (this.uattributes == null) {
                            this.uattributes = new HashSet<CaseInsensitiveString>(4);
                        }
                        this.uattributes.add(new CaseInsensitiveString(uatr));
                    }
                    for (String ukey : ue.getUnicodeLocaleKeys()) {
                        if (this.ukeywords == null) {
                            this.ukeywords = new HashMap<CaseInsensitiveString, String>(4);
                        }
                        this.ukeywords.put(new CaseInsensitiveString(ukey), ue.getUnicodeLocaleType(ukey));
                    }
                    continue;
                }
                if (this.extensions == null) {
                    this.extensions = new HashMap<CaseInsensitiveChar, String>(4);
                }
                this.extensions.put(new CaseInsensitiveChar(key.charValue()), e.getValue());
            }
        }
        return this;
    }

    public InternalLocaleBuilder clear() {
        this.language = "";
        this.script = "";
        this.region = "";
        this.variant = "";
        this.clearExtensions();
        return this;
    }

    public InternalLocaleBuilder clearExtensions() {
        if (this.extensions != null) {
            this.extensions.clear();
        }
        if (this.uattributes != null) {
            this.uattributes.clear();
        }
        if (this.ukeywords != null) {
            this.ukeywords.clear();
        }
        return this;
    }

    public BaseLocale getBaseLocale() {
        String privuse;
        String language = this.language;
        String script = this.script;
        String region = this.region;
        String variant = this.variant;
        if (this.extensions != null && (privuse = this.extensions.get(PRIVATEUSE_KEY)) != null) {
            StringTokenIterator itr = new StringTokenIterator(privuse, "-");
            boolean sawPrefix = false;
            int privVarStart = -1;
            while (!itr.isDone()) {
                if (sawPrefix) {
                    privVarStart = itr.currentStart();
                    break;
                }
                if (LocaleUtils.caseIgnoreMatch(itr.current(), "lvariant")) {
                    sawPrefix = true;
                }
                itr.next();
            }
            if (privVarStart != -1) {
                StringBuilder sb = new StringBuilder(variant);
                if (sb.length() != 0) {
                    sb.append("_");
                }
                sb.append(privuse.substring(privVarStart).replaceAll("-", "_"));
                variant = sb.toString();
            }
        }
        return BaseLocale.getInstance(language, script, region, variant);
    }

    public LocaleExtensions getLocaleExtensions() {
        if (LocaleUtils.isEmpty(this.extensions) && LocaleUtils.isEmpty(this.uattributes) && LocaleUtils.isEmpty(this.ukeywords)) {
            return null;
        }
        LocaleExtensions lext = new LocaleExtensions(this.extensions, this.uattributes, this.ukeywords);
        return lext.isEmpty() ? null : lext;
    }

    static String removePrivateuseVariant(String privuseVal) {
        StringTokenIterator itr = new StringTokenIterator(privuseVal, "-");
        int prefixStart = -1;
        boolean sawPrivuseVar = false;
        while (!itr.isDone()) {
            if (prefixStart != -1) {
                sawPrivuseVar = true;
                break;
            }
            if (LocaleUtils.caseIgnoreMatch(itr.current(), "lvariant")) {
                prefixStart = itr.currentStart();
            }
            itr.next();
        }
        if (!sawPrivuseVar) {
            return privuseVal;
        }
        assert (prefixStart == 0 || prefixStart > 1);
        return prefixStart == 0 ? null : privuseVal.substring(0, prefixStart - 1);
    }

    private int checkVariants(String variants, String sep) {
        StringTokenIterator itr = new StringTokenIterator(variants, sep);
        while (!itr.isDone()) {
            String s = itr.current();
            if (!LanguageTag.isVariant(s)) {
                return itr.currentStart();
            }
            itr.next();
        }
        return -1;
    }

    private void setUnicodeLocaleExtension(String subtags) {
        if (this.uattributes != null) {
            this.uattributes.clear();
        }
        if (this.ukeywords != null) {
            this.ukeywords.clear();
        }
        StringTokenIterator itr = new StringTokenIterator(subtags, "-");
        while (!itr.isDone() && UnicodeLocaleExtension.isAttribute(itr.current())) {
            if (this.uattributes == null) {
                this.uattributes = new HashSet<CaseInsensitiveString>(4);
            }
            this.uattributes.add(new CaseInsensitiveString(itr.current()));
            itr.next();
        }
        CaseInsensitiveString key = null;
        int typeStart = -1;
        int typeEnd = -1;
        while (!itr.isDone()) {
            String type;
            if (key != null) {
                if (UnicodeLocaleExtension.isKey(itr.current())) {
                    assert (typeStart == -1 || typeEnd != -1);
                    String string = type = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                    if (this.ukeywords == null) {
                        this.ukeywords = new HashMap<CaseInsensitiveString, String>(4);
                    }
                    this.ukeywords.put(key, type);
                    CaseInsensitiveString tmpKey = new CaseInsensitiveString(itr.current());
                    key = this.ukeywords.containsKey(tmpKey) ? null : tmpKey;
                    typeEnd = -1;
                    typeStart = -1;
                } else {
                    if (typeStart == -1) {
                        typeStart = itr.currentStart();
                    }
                    typeEnd = itr.currentEnd();
                }
            } else if (UnicodeLocaleExtension.isKey(itr.current())) {
                key = new CaseInsensitiveString(itr.current());
                if (this.ukeywords != null && this.ukeywords.containsKey(key)) {
                    key = null;
                }
            }
            if (!itr.hasNext()) {
                if (key == null) break;
                assert (typeStart == -1 || typeEnd != -1);
                String string = type = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                if (this.ukeywords == null) {
                    this.ukeywords = new HashMap<CaseInsensitiveString, String>(4);
                }
                this.ukeywords.put(key, type);
                break;
            }
            itr.next();
        }
    }

    static final class CaseInsensitiveString {
        private final String str;
        private final String lowerStr;

        CaseInsensitiveString(String s) {
            this.str = s;
            this.lowerStr = LocaleUtils.toLowerString(s);
        }

        public String value() {
            return this.str;
        }

        public int hashCode() {
            return this.lowerStr.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveString)) {
                return false;
            }
            return this.lowerStr.equals(((CaseInsensitiveString)obj).lowerStr);
        }
    }

    static final class CaseInsensitiveChar {
        private final char ch;
        private final char lowerCh;

        private CaseInsensitiveChar(String s) {
            this(s.charAt(0));
        }

        CaseInsensitiveChar(char c) {
            this.ch = c;
            this.lowerCh = LocaleUtils.toLower(this.ch);
        }

        public char value() {
            return this.ch;
        }

        public int hashCode() {
            return this.lowerCh;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveChar)) {
                return false;
            }
            return this.lowerCh == ((CaseInsensitiveChar)obj).lowerCh;
        }
    }
}

