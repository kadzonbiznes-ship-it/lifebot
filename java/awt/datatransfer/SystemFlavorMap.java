/*
 * Decompiled with CFR 0.152.
 */
package java.awt.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.FlavorTable;
import java.awt.datatransfer.MimeType;
import java.awt.datatransfer.MimeTypeParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import sun.datatransfer.DataFlavorUtil;
import sun.datatransfer.DesktopDatatransferService;

public final class SystemFlavorMap
implements FlavorMap,
FlavorTable {
    private static String JavaMIME = "JAVA_DATAFLAVOR:";
    private static final Object FLAVOR_MAP_KEY = new Object();
    private static final String[] UNICODE_TEXT_CLASSES = new String[]{"java.io.Reader", "java.lang.String", "java.nio.CharBuffer", "\"[C\""};
    private static final String[] ENCODED_TEXT_CLASSES = new String[]{"java.io.InputStream", "java.nio.ByteBuffer", "\"[B\""};
    private static final String TEXT_PLAIN_BASE_TYPE = "text/plain";
    private static final String HTML_TEXT_BASE_TYPE = "text/html";
    private final Map<String, LinkedHashSet<DataFlavor>> nativeToFlavor = new HashMap<String, LinkedHashSet<DataFlavor>>();
    private final Map<DataFlavor, LinkedHashSet<String>> flavorToNative = new HashMap<DataFlavor, LinkedHashSet<String>>();
    private Map<String, LinkedHashSet<String>> textTypeToNative = new HashMap<String, LinkedHashSet<String>>();
    private boolean isMapInitialized = false;
    private final SoftCache<DataFlavor, String> nativesForFlavorCache = new SoftCache();
    private final SoftCache<String, DataFlavor> flavorsForNativeCache = new SoftCache();
    private Set<Object> disabledMappingGenerationKeys = new HashSet<Object>();
    private static final String[] htmlDocumentTypes = new String[]{"all", "selection", "fragment"};

    private Map<String, LinkedHashSet<DataFlavor>> getNativeToFlavor() {
        if (!this.isMapInitialized) {
            this.initSystemFlavorMap();
        }
        return this.nativeToFlavor;
    }

    private synchronized Map<DataFlavor, LinkedHashSet<String>> getFlavorToNative() {
        if (!this.isMapInitialized) {
            this.initSystemFlavorMap();
        }
        return this.flavorToNative;
    }

    private synchronized Map<String, LinkedHashSet<String>> getTextTypeToNative() {
        if (!this.isMapInitialized) {
            this.initSystemFlavorMap();
            this.textTypeToNative = Collections.unmodifiableMap(this.textTypeToNative);
        }
        return this.textTypeToNative;
    }

    public static FlavorMap getDefaultFlavorMap() {
        return DataFlavorUtil.getDesktopService().getFlavorMap(SystemFlavorMap::new);
    }

    private SystemFlavorMap() {
    }

    private void initSystemFlavorMap() {
        if (this.isMapInitialized) {
            return;
        }
        this.isMapInitialized = true;
        InputStream is = AccessController.doPrivileged(() -> SystemFlavorMap.class.getResourceAsStream("/sun/datatransfer/resources/flavormap.properties"));
        if (is == null) {
            throw new InternalError("Default flavor mapping not found");
        }
        try (InputStreamReader isr = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isr);){
            Object line;
            while ((line = reader.readLine()) != null) {
                String[] values;
                if (((String)(line = ((String)line).trim())).startsWith("#") || ((String)line).isEmpty()) continue;
                while (((String)line).endsWith("\\")) {
                    line = ((String)line).substring(0, ((String)line).length() - 1) + reader.readLine().trim();
                }
                int delimiterPosition = ((String)line).indexOf(61);
                String key = ((String)line).substring(0, delimiterPosition).replace("\\ ", " ");
                for (String value : values = ((String)line).substring(delimiterPosition + 1, ((String)line).length()).split(",")) {
                    DataFlavor flavor;
                    block26: {
                        try {
                            DesktopDatatransferService desktopService;
                            value = SystemFlavorMap.loadConvert(value);
                            MimeType mime = new MimeType(value);
                            if (!"text".equals(mime.getPrimaryType())) break block26;
                            String charset = mime.getParameter("charset");
                            if (DataFlavorUtil.doesSubtypeSupportCharset(mime.getSubType(), charset) && (desktopService = DataFlavorUtil.getDesktopService()).isDesktopPresent()) {
                                desktopService.registerTextFlavorProperties(key, charset, mime.getParameter("eoln"), mime.getParameter("terminators"));
                            }
                            mime.removeParameter("charset");
                            mime.removeParameter("class");
                            mime.removeParameter("eoln");
                            mime.removeParameter("terminators");
                            value = mime.toString();
                        }
                        catch (MimeTypeParseException e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                    try {
                        flavor = new DataFlavor(value);
                    }
                    catch (Exception e) {
                        try {
                            flavor = new DataFlavor(value, null);
                        }
                        catch (Exception ee) {
                            ee.printStackTrace();
                            continue;
                        }
                    }
                    LinkedHashSet<DataFlavor> dfs = new LinkedHashSet<DataFlavor>();
                    dfs.add(flavor);
                    if ("text".equals(flavor.getPrimaryType())) {
                        dfs.addAll(SystemFlavorMap.convertMimeTypeToDataFlavors(value));
                        this.store(flavor.mimeType.getBaseType(), key, this.getTextTypeToNative());
                    }
                    for (DataFlavor df : dfs) {
                        this.store(df, key, this.getFlavorToNative());
                        this.store(key, df, this.getNativeToFlavor());
                    }
                }
            }
        }
        catch (IOException e) {
            throw new InternalError("Error reading default flavor mapping", e);
        }
    }

    private static String loadConvert(String theString) {
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);
        int x = 0;
        while (x < len) {
            int aChar;
            if ((aChar = theString.charAt(x++)) == 92) {
                if ((aChar = theString.charAt(x++)) == 117) {
                    int value = 0;
                    block6: for (int i = 0; i < 4; ++i) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case 48: 
                            case 49: 
                            case 50: 
                            case 51: 
                            case 52: 
                            case 53: 
                            case 54: 
                            case 55: 
                            case 56: 
                            case 57: {
                                value = (value << 4) + aChar - 48;
                                continue block6;
                            }
                            case 97: 
                            case 98: 
                            case 99: 
                            case 100: 
                            case 101: 
                            case 102: {
                                value = (value << 4) + 10 + aChar - 97;
                                continue block6;
                            }
                            case 65: 
                            case 66: 
                            case 67: 
                            case 68: 
                            case 69: 
                            case 70: {
                                value = (value << 4) + 10 + aChar - 65;
                                continue block6;
                            }
                            default: {
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                            }
                        }
                    }
                    outBuffer.append((char)value);
                    continue;
                }
                if (aChar == 116) {
                    aChar = 9;
                } else if (aChar == 114) {
                    aChar = 13;
                } else if (aChar == 110) {
                    aChar = 10;
                } else if (aChar == 102) {
                    aChar = 12;
                }
                outBuffer.append((char)aChar);
                continue;
            }
            outBuffer.append((char)aChar);
        }
        return outBuffer.toString();
    }

    private <H, L> void store(H hashed, L listed, Map<H, LinkedHashSet<L>> map) {
        LinkedHashSet<Object> list = map.get(hashed);
        if (list == null) {
            list = new LinkedHashSet(1);
            map.put(hashed, list);
        }
        if (!list.contains(listed)) {
            list.add(listed);
        }
    }

    private LinkedHashSet<DataFlavor> nativeToFlavorLookup(String nat) {
        LinkedHashSet<DataFlavor> platformFlavors;
        DesktopDatatransferService desktopService;
        LinkedHashSet<Object> flavors = this.getNativeToFlavor().get(nat);
        if (nat != null && !this.disabledMappingGenerationKeys.contains(nat) && (desktopService = DataFlavorUtil.getDesktopService()).isDesktopPresent() && !(platformFlavors = desktopService.getPlatformMappingsForNative(nat)).isEmpty()) {
            if (flavors != null) {
                platformFlavors.addAll(flavors);
            }
            flavors = platformFlavors;
        }
        if (flavors == null && SystemFlavorMap.isJavaMIMEType(nat)) {
            String decoded = SystemFlavorMap.decodeJavaMIMEType(nat);
            DataFlavor flavor = null;
            try {
                flavor = new DataFlavor(decoded);
            }
            catch (Exception e) {
                System.err.println("Exception \"" + e.getClass().getName() + ": " + e.getMessage() + "\"while constructing DataFlavor for: " + decoded);
            }
            if (flavor != null) {
                flavors = new LinkedHashSet(1);
                this.getNativeToFlavor().put(nat, flavors);
                flavors.add(flavor);
                this.flavorsForNativeCache.remove(nat);
                LinkedHashSet<String> natives = this.getFlavorToNative().get(flavor);
                if (natives == null) {
                    natives = new LinkedHashSet(1);
                    this.getFlavorToNative().put(flavor, natives);
                }
                natives.add(nat);
                this.nativesForFlavorCache.remove(flavor);
            }
        }
        return flavors != null ? flavors : new LinkedHashSet(0);
    }

    private LinkedHashSet<String> flavorToNativeLookup(DataFlavor flav, boolean synthesize) {
        LinkedHashSet<String> platformNatives;
        DesktopDatatransferService desktopService;
        LinkedHashSet<String> natives = this.getFlavorToNative().get(flav);
        if (flav != null && !this.disabledMappingGenerationKeys.contains(flav) && (desktopService = DataFlavorUtil.getDesktopService()).isDesktopPresent() && !(platformNatives = desktopService.getPlatformMappingsForFlavor(flav)).isEmpty()) {
            if (natives != null) {
                platformNatives.addAll(natives);
            }
            natives = platformNatives;
        }
        if (natives == null) {
            if (synthesize) {
                String encoded = SystemFlavorMap.encodeDataFlavor(flav);
                natives = new LinkedHashSet(1);
                this.getFlavorToNative().put(flav, natives);
                natives.add(encoded);
                LinkedHashSet<DataFlavor> flavors = this.getNativeToFlavor().get(encoded);
                if (flavors == null) {
                    flavors = new LinkedHashSet(1);
                    this.getNativeToFlavor().put(encoded, flavors);
                }
                flavors.add(flav);
                this.nativesForFlavorCache.remove(flav);
                this.flavorsForNativeCache.remove(encoded);
            } else {
                natives = new LinkedHashSet(0);
            }
        }
        return new LinkedHashSet<String>(natives);
    }

    @Override
    public synchronized List<String> getNativesForFlavor(DataFlavor flav) {
        LinkedHashSet<String> retval = this.nativesForFlavorCache.check(flav);
        if (retval != null) {
            return new ArrayList<String>(retval);
        }
        if (flav == null) {
            retval = new LinkedHashSet<String>(this.getNativeToFlavor().keySet());
        } else if (this.disabledMappingGenerationKeys.contains(flav)) {
            retval = this.flavorToNativeLookup(flav, false);
        } else if (DataFlavorUtil.isFlavorCharsetTextType(flav)) {
            LinkedHashSet<String> textTypeNatives;
            retval = new LinkedHashSet(0);
            if ("text".equals(flav.getPrimaryType()) && (textTypeNatives = this.getTextTypeToNative().get(flav.mimeType.getBaseType())) != null) {
                retval.addAll(textTypeNatives);
            }
            if ((textTypeNatives = this.getTextTypeToNative().get(TEXT_PLAIN_BASE_TYPE)) != null) {
                retval.addAll(textTypeNatives);
            }
            if (retval.isEmpty()) {
                retval = this.flavorToNativeLookup(flav, true);
            } else {
                retval.addAll(this.flavorToNativeLookup(flav, false));
            }
        } else if (DataFlavorUtil.isFlavorNoncharsetTextType(flav)) {
            retval = this.getTextTypeToNative().get(flav.mimeType.getBaseType());
            if (retval == null || retval.isEmpty()) {
                retval = this.flavorToNativeLookup(flav, true);
            } else {
                retval.addAll(this.flavorToNativeLookup(flav, false));
            }
        } else {
            retval = this.flavorToNativeLookup(flav, true);
        }
        this.nativesForFlavorCache.put(flav, retval);
        return new ArrayList<String>(retval);
    }

    @Override
    public synchronized List<DataFlavor> getFlavorsForNative(String nat) {
        LinkedHashSet<DataFlavor> returnValue = this.flavorsForNativeCache.check(nat);
        if (returnValue != null) {
            return new ArrayList<DataFlavor>(returnValue);
        }
        returnValue = new LinkedHashSet();
        if (nat == null) {
            for (String n : this.getNativesForFlavor(null)) {
                returnValue.addAll(this.getFlavorsForNative(n));
            }
        } else {
            LinkedHashSet<DataFlavor> flavors = this.nativeToFlavorLookup(nat);
            if (this.disabledMappingGenerationKeys.contains(nat)) {
                return new ArrayList<DataFlavor>(flavors);
            }
            LinkedHashSet<DataFlavor> flavorsWithSynthesized = this.nativeToFlavorLookup(nat);
            for (DataFlavor df : flavorsWithSynthesized) {
                returnValue.add(df);
                if (!"text".equals(df.getPrimaryType())) continue;
                String baseType = df.mimeType.getBaseType();
                returnValue.addAll(SystemFlavorMap.convertMimeTypeToDataFlavors(baseType));
            }
        }
        this.flavorsForNativeCache.put(nat, returnValue);
        return new ArrayList<DataFlavor>(returnValue);
    }

    private static Set<DataFlavor> convertMimeTypeToDataFlavors(String baseType) {
        LinkedHashSet<DataFlavor> returnValue = new LinkedHashSet<DataFlavor>();
        String subType = null;
        try {
            MimeType mimeType = new MimeType(baseType);
            subType = mimeType.getSubType();
        }
        catch (MimeTypeParseException mimeTypeParseException) {
            // empty catch block
        }
        if (DataFlavorUtil.doesSubtypeSupportCharset(subType, null)) {
            if (TEXT_PLAIN_BASE_TYPE.equals(baseType)) {
                returnValue.add(DataFlavor.stringFlavor);
            }
            for (String unicodeClassName : UNICODE_TEXT_CLASSES) {
                String mimeType = baseType + ";charset=Unicode;class=" + unicodeClassName;
                LinkedHashSet<String> mimeTypes = SystemFlavorMap.handleHtmlMimeTypes(baseType, mimeType);
                for (String mt : mimeTypes) {
                    DataFlavor toAdd = null;
                    try {
                        toAdd = new DataFlavor(mt);
                    }
                    catch (ClassNotFoundException classNotFoundException) {
                        // empty catch block
                    }
                    returnValue.add(toAdd);
                }
            }
            for (String charset : DataFlavorUtil.standardEncodings()) {
                for (String encodedTextClass : ENCODED_TEXT_CLASSES) {
                    String mimeType = baseType + ";charset=" + charset + ";class=" + encodedTextClass;
                    LinkedHashSet<String> mimeTypes = SystemFlavorMap.handleHtmlMimeTypes(baseType, mimeType);
                    for (String mt : mimeTypes) {
                        DataFlavor df = null;
                        try {
                            df = new DataFlavor(mt);
                            if (df.equals(DataFlavor.plainTextFlavor)) {
                                df = DataFlavor.plainTextFlavor;
                            }
                        }
                        catch (ClassNotFoundException classNotFoundException) {
                            // empty catch block
                        }
                        returnValue.add(df);
                    }
                }
            }
            if (TEXT_PLAIN_BASE_TYPE.equals(baseType)) {
                returnValue.add(DataFlavor.plainTextFlavor);
            }
        } else {
            for (String encodedTextClassName : ENCODED_TEXT_CLASSES) {
                DataFlavor toAdd = null;
                try {
                    toAdd = new DataFlavor(baseType + ";class=" + encodedTextClassName);
                }
                catch (ClassNotFoundException classNotFoundException) {
                    // empty catch block
                }
                returnValue.add(toAdd);
            }
        }
        return returnValue;
    }

    private static LinkedHashSet<String> handleHtmlMimeTypes(String baseType, String mimeType) {
        LinkedHashSet<String> returnValues = new LinkedHashSet<String>();
        if (HTML_TEXT_BASE_TYPE.equals(baseType)) {
            for (String documentType : htmlDocumentTypes) {
                returnValues.add(mimeType + ";document=" + documentType);
            }
        } else {
            returnValues.add(mimeType);
        }
        return returnValues;
    }

    @Override
    public synchronized Map<DataFlavor, String> getNativesForFlavors(DataFlavor[] flavors) {
        if (flavors == null) {
            List<DataFlavor> flavor_list = this.getFlavorsForNative(null);
            flavors = new DataFlavor[flavor_list.size()];
            flavor_list.toArray(flavors);
        }
        HashMap<DataFlavor, String> retval = new HashMap<DataFlavor, String>(flavors.length, 1.0f);
        for (DataFlavor flavor : flavors) {
            List<String> natives = this.getNativesForFlavor(flavor);
            String nat = natives.isEmpty() ? null : natives.get(0);
            retval.put(flavor, nat);
        }
        return retval;
    }

    @Override
    public synchronized Map<String, DataFlavor> getFlavorsForNatives(String[] natives) {
        if (natives == null) {
            List<String> nativesList = this.getNativesForFlavor(null);
            natives = new String[nativesList.size()];
            nativesList.toArray(natives);
        }
        HashMap<String, DataFlavor> retval = new HashMap<String, DataFlavor>(natives.length, 1.0f);
        for (String aNative : natives) {
            List<DataFlavor> flavors = this.getFlavorsForNative(aNative);
            DataFlavor flav = flavors.isEmpty() ? null : flavors.get(0);
            retval.put(aNative, flav);
        }
        return retval;
    }

    public synchronized void addUnencodedNativeForFlavor(DataFlavor flav, String nat) {
        Objects.requireNonNull(nat, "Null native not permitted");
        Objects.requireNonNull(flav, "Null flavor not permitted");
        LinkedHashSet<String> natives = this.getFlavorToNative().get(flav);
        if (natives == null) {
            natives = new LinkedHashSet(1);
            this.getFlavorToNative().put(flav, natives);
        }
        natives.add(nat);
        this.nativesForFlavorCache.remove(flav);
    }

    public synchronized void setNativesForFlavor(DataFlavor flav, String[] natives) {
        Objects.requireNonNull(natives, "Null natives not permitted");
        Objects.requireNonNull(flav, "Null flavors not permitted");
        this.getFlavorToNative().remove(flav);
        for (String aNative : natives) {
            this.addUnencodedNativeForFlavor(flav, aNative);
        }
        this.disabledMappingGenerationKeys.add(flav);
        this.nativesForFlavorCache.remove(flav);
    }

    public synchronized void addFlavorForUnencodedNative(String nat, DataFlavor flav) {
        Objects.requireNonNull(nat, "Null native not permitted");
        Objects.requireNonNull(flav, "Null flavor not permitted");
        LinkedHashSet<DataFlavor> flavors = this.getNativeToFlavor().get(nat);
        if (flavors == null) {
            flavors = new LinkedHashSet(1);
            this.getNativeToFlavor().put(nat, flavors);
        }
        flavors.add(flav);
        this.flavorsForNativeCache.remove(nat);
    }

    public synchronized void setFlavorsForNative(String nat, DataFlavor[] flavors) {
        Objects.requireNonNull(nat, "Null native not permitted");
        Objects.requireNonNull(flavors, "Null flavors not permitted");
        this.getNativeToFlavor().remove(nat);
        for (DataFlavor flavor : flavors) {
            this.addFlavorForUnencodedNative(nat, flavor);
        }
        this.disabledMappingGenerationKeys.add(nat);
        this.flavorsForNativeCache.remove(nat);
    }

    public static String encodeJavaMIMEType(String mimeType) {
        return mimeType != null ? JavaMIME + mimeType : null;
    }

    public static String encodeDataFlavor(DataFlavor flav) {
        return flav != null ? SystemFlavorMap.encodeJavaMIMEType(flav.getMimeType()) : null;
    }

    public static boolean isJavaMIMEType(String str) {
        return str != null && str.startsWith(JavaMIME, 0);
    }

    public static String decodeJavaMIMEType(String nat) {
        return SystemFlavorMap.isJavaMIMEType(nat) ? nat.substring(JavaMIME.length(), nat.length()).trim() : null;
    }

    public static DataFlavor decodeDataFlavor(String nat) throws ClassNotFoundException {
        String retval_str = SystemFlavorMap.decodeJavaMIMEType(nat);
        return retval_str != null ? new DataFlavor(retval_str) : null;
    }

    private static final class SoftCache<K, V> {
        Map<K, SoftReference<LinkedHashSet<V>>> cache;

        private SoftCache() {
        }

        public void put(K key, LinkedHashSet<V> value) {
            if (this.cache == null) {
                this.cache = new HashMap<K, SoftReference<LinkedHashSet<V>>>(1);
            }
            this.cache.put(key, new SoftReference<LinkedHashSet<V>>(value));
        }

        public void remove(K key) {
            if (this.cache == null) {
                return;
            }
            this.cache.remove(null);
            this.cache.remove(key);
        }

        public LinkedHashSet<V> check(K key) {
            if (this.cache == null) {
                return null;
            }
            SoftReference<LinkedHashSet<V>> ref = this.cache.get(key);
            if (ref != null) {
                return ref.get();
            }
            return null;
        }
    }
}

