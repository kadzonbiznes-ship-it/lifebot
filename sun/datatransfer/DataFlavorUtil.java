/*
 * Decompiled with CFR 0.152.
 */
package sun.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import sun.datatransfer.DesktopDatatransferService;

public class DataFlavorUtil {
    private static final Map<String, Boolean> textMIMESubtypeCharsetSupport;

    private DataFlavorUtil() {
    }

    private static Comparator<String> getCharsetComparator() {
        return CharsetComparator.INSTANCE;
    }

    public static Comparator<DataFlavor> getDataFlavorComparator() {
        return DataFlavorComparator.INSTANCE;
    }

    public static Comparator<Long> getIndexOrderComparator(Map<Long, Integer> indexMap) {
        return new IndexOrderComparator(indexMap);
    }

    public static Comparator<DataFlavor> getTextFlavorComparator() {
        return TextFlavorComparator.INSTANCE;
    }

    public static Set<String> standardEncodings() {
        return StandardEncodingsHolder.standardEncodings;
    }

    public static String canonicalName(String encoding) {
        if (encoding == null) {
            return null;
        }
        try {
            return Charset.forName(encoding).name();
        }
        catch (IllegalCharsetNameException icne) {
            return encoding;
        }
        catch (UnsupportedCharsetException uce) {
            return encoding;
        }
    }

    public static boolean doesSubtypeSupportCharset(DataFlavor flavor) {
        String subType = flavor.getSubType();
        if (subType == null) {
            return false;
        }
        Boolean support = textMIMESubtypeCharsetSupport.get(subType);
        if (support != null) {
            return support;
        }
        boolean ret_val = flavor.getParameter("charset") != null;
        textMIMESubtypeCharsetSupport.put(subType, ret_val);
        return ret_val;
    }

    public static boolean doesSubtypeSupportCharset(String subType, String charset) {
        Boolean support = textMIMESubtypeCharsetSupport.get(subType);
        if (support != null) {
            return support;
        }
        boolean ret_val = charset != null;
        textMIMESubtypeCharsetSupport.put(subType, ret_val);
        return ret_val;
    }

    public static boolean isFlavorCharsetTextType(DataFlavor flavor) {
        if (DataFlavor.stringFlavor.equals(flavor)) {
            return true;
        }
        if (!"text".equals(flavor.getPrimaryType()) || !DataFlavorUtil.doesSubtypeSupportCharset(flavor)) {
            return false;
        }
        Class<?> rep_class = flavor.getRepresentationClass();
        if (flavor.isRepresentationClassReader() || String.class.equals(rep_class) || flavor.isRepresentationClassCharBuffer() || char[].class.equals(rep_class)) {
            return true;
        }
        if (!(flavor.isRepresentationClassInputStream() || flavor.isRepresentationClassByteBuffer() || byte[].class.equals(rep_class))) {
            return false;
        }
        String charset = flavor.getParameter("charset");
        return charset == null || DataFlavorUtil.isEncodingSupported(charset);
    }

    public static boolean isFlavorNoncharsetTextType(DataFlavor flavor) {
        if (!"text".equals(flavor.getPrimaryType()) || DataFlavorUtil.doesSubtypeSupportCharset(flavor)) {
            return false;
        }
        return flavor.isRepresentationClassInputStream() || flavor.isRepresentationClassByteBuffer() || byte[].class.equals(flavor.getRepresentationClass());
    }

    public static String getTextCharset(DataFlavor flavor) {
        if (!DataFlavorUtil.isFlavorCharsetTextType(flavor)) {
            return null;
        }
        String encoding = flavor.getParameter("charset");
        return encoding != null ? encoding : Charset.defaultCharset().name();
    }

    private static boolean isEncodingSupported(String encoding) {
        if (encoding == null) {
            return false;
        }
        try {
            return Charset.isSupported(encoding);
        }
        catch (IllegalCharsetNameException icne) {
            return false;
        }
    }

    static <T> int compareIndices(Map<T, Integer> indexMap, T obj1, T obj2, Integer fallbackIndex) {
        Integer index1 = indexMap.getOrDefault(obj1, fallbackIndex);
        Integer index2 = indexMap.getOrDefault(obj2, fallbackIndex);
        return index1.compareTo(index2);
    }

    public static DesktopDatatransferService getDesktopService() {
        return DefaultDesktopDatatransferService.INSTANCE;
    }

    static {
        HashMap<String, Boolean> tempMap = new HashMap<String, Boolean>(17);
        tempMap.put("sgml", Boolean.TRUE);
        tempMap.put("xml", Boolean.TRUE);
        tempMap.put("html", Boolean.TRUE);
        tempMap.put("enriched", Boolean.TRUE);
        tempMap.put("richtext", Boolean.TRUE);
        tempMap.put("uri-list", Boolean.TRUE);
        tempMap.put("directory", Boolean.TRUE);
        tempMap.put("css", Boolean.TRUE);
        tempMap.put("calendar", Boolean.TRUE);
        tempMap.put("plain", Boolean.TRUE);
        tempMap.put("rtf", Boolean.FALSE);
        tempMap.put("tab-separated-values", Boolean.FALSE);
        tempMap.put("t140", Boolean.FALSE);
        tempMap.put("rfc822-headers", Boolean.FALSE);
        tempMap.put("parityfec", Boolean.FALSE);
        textMIMESubtypeCharsetSupport = Collections.synchronizedMap(tempMap);
    }

    private static class CharsetComparator
    implements Comparator<String> {
        static final CharsetComparator INSTANCE = new CharsetComparator();
        private static final Map<String, Integer> charsets;
        private static final Integer DEFAULT_CHARSET_INDEX;
        private static final Integer OTHER_CHARSET_INDEX;
        private static final Integer WORST_CHARSET_INDEX;
        private static final Integer UNSUPPORTED_CHARSET_INDEX;
        private static final String UNSUPPORTED_CHARSET = "UNSUPPORTED";

        private CharsetComparator() {
        }

        @Override
        public int compare(String charset1, String charset2) {
            int comp = DataFlavorUtil.compareIndices(charsets, charset1 = CharsetComparator.getEncoding(charset1), charset2 = CharsetComparator.getEncoding(charset2), OTHER_CHARSET_INDEX);
            if (comp == 0) {
                return charset2.compareTo(charset1);
            }
            return comp;
        }

        static String getEncoding(String charset) {
            if (charset == null) {
                return null;
            }
            if (!DataFlavorUtil.isEncodingSupported(charset)) {
                return UNSUPPORTED_CHARSET;
            }
            String canonicalName = DataFlavorUtil.canonicalName(charset);
            return charsets.containsKey(canonicalName) ? canonicalName : charset;
        }

        static {
            DEFAULT_CHARSET_INDEX = 2;
            OTHER_CHARSET_INDEX = 1;
            WORST_CHARSET_INDEX = 0;
            UNSUPPORTED_CHARSET_INDEX = Integer.MIN_VALUE;
            HashMap<String, Integer> charsetsMap = new HashMap<String, Integer>(8, 1.0f);
            charsetsMap.put(StandardCharsets.UTF_16LE.name(), 4);
            charsetsMap.put(StandardCharsets.UTF_16BE.name(), 5);
            charsetsMap.put(StandardCharsets.UTF_8.name(), 6);
            charsetsMap.put(StandardCharsets.UTF_16.name(), 7);
            charsetsMap.put(StandardCharsets.US_ASCII.name(), WORST_CHARSET_INDEX);
            charsetsMap.putIfAbsent(Charset.defaultCharset().name(), DEFAULT_CHARSET_INDEX);
            charsetsMap.put(UNSUPPORTED_CHARSET, UNSUPPORTED_CHARSET_INDEX);
            charsets = Collections.unmodifiableMap(charsetsMap);
        }
    }

    private static class DataFlavorComparator
    implements Comparator<DataFlavor> {
        static final DataFlavorComparator INSTANCE = new DataFlavorComparator();
        private static final Map<String, Integer> exactTypes;
        private static final Map<String, Integer> primaryTypes;
        private static final Map<Class<?>, Integer> nonTextRepresentations;
        private static final Map<String, Integer> textTypes;
        private static final Map<Class<?>, Integer> decodedTextRepresentations;
        private static final Map<Class<?>, Integer> encodedTextRepresentations;
        private static final Integer UNKNOWN_OBJECT_LOSES;
        private static final Integer UNKNOWN_OBJECT_WINS;

        private DataFlavorComparator() {
        }

        @Override
        public int compare(DataFlavor flavor1, DataFlavor flavor2) {
            if (flavor1.equals(flavor2)) {
                return 0;
            }
            String primaryType1 = flavor1.getPrimaryType();
            String subType1 = flavor1.getSubType();
            String mimeType1 = primaryType1 + "/" + subType1;
            Class<?> class1 = flavor1.getRepresentationClass();
            String primaryType2 = flavor2.getPrimaryType();
            String subType2 = flavor2.getSubType();
            String mimeType2 = primaryType2 + "/" + subType2;
            Class<?> class2 = flavor2.getRepresentationClass();
            if (flavor1.isFlavorTextType() && flavor2.isFlavorTextType()) {
                int comp = DataFlavorUtil.compareIndices(textTypes, mimeType1, mimeType2, UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
                if (DataFlavorUtil.doesSubtypeSupportCharset(flavor1)) {
                    comp = DataFlavorUtil.compareIndices(decodedTextRepresentations, class1, class2, UNKNOWN_OBJECT_LOSES);
                    if (comp != 0) {
                        return comp;
                    }
                    comp = CharsetComparator.INSTANCE.compare(DataFlavorUtil.getTextCharset(flavor1), DataFlavorUtil.getTextCharset(flavor2));
                    if (comp != 0) {
                        return comp;
                    }
                }
                if ((comp = DataFlavorUtil.compareIndices(encodedTextRepresentations, class1, class2, UNKNOWN_OBJECT_LOSES)) != 0) {
                    return comp;
                }
            } else {
                if (flavor1.isFlavorTextType()) {
                    return 1;
                }
                if (flavor2.isFlavorTextType()) {
                    return -1;
                }
                int comp = DataFlavorUtil.compareIndices(primaryTypes, primaryType1, primaryType2, UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
                comp = DataFlavorUtil.compareIndices(exactTypes, mimeType1, mimeType2, UNKNOWN_OBJECT_WINS);
                if (comp != 0) {
                    return comp;
                }
                comp = DataFlavorUtil.compareIndices(nonTextRepresentations, class1, class2, UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
            }
            return flavor1.getMimeType().compareTo(flavor2.getMimeType());
        }

        static {
            UNKNOWN_OBJECT_LOSES = Integer.MIN_VALUE;
            UNKNOWN_OBJECT_WINS = Integer.MAX_VALUE;
            HashMap<String, Integer> exactTypesMap = new HashMap<String, Integer>(4, 1.0f);
            exactTypesMap.put("application/x-java-file-list", 0);
            exactTypesMap.put("application/x-java-serialized-object", 1);
            exactTypesMap.put("application/x-java-jvm-local-objectref", 2);
            exactTypesMap.put("application/x-java-remote-object", 3);
            exactTypes = Collections.unmodifiableMap(exactTypesMap);
            HashMap<String, Integer> primaryTypesMap = new HashMap<String, Integer>(1, 1.0f);
            primaryTypesMap.put("application", 0);
            primaryTypes = Collections.unmodifiableMap(primaryTypesMap);
            HashMap nonTextRepresentationsMap = new HashMap(3, 1.0f);
            nonTextRepresentationsMap.put(InputStream.class, 0);
            nonTextRepresentationsMap.put(Serializable.class, 1);
            nonTextRepresentationsMap.put(RMI.remoteClass(), 2);
            nonTextRepresentations = Collections.unmodifiableMap(nonTextRepresentationsMap);
            HashMap<String, Integer> textTypesMap = new HashMap<String, Integer>(16, 1.0f);
            textTypesMap.put("text/plain", 0);
            textTypesMap.put("application/x-java-serialized-object", 1);
            textTypesMap.put("text/calendar", 2);
            textTypesMap.put("text/css", 3);
            textTypesMap.put("text/directory", 4);
            textTypesMap.put("text/parityfec", 5);
            textTypesMap.put("text/rfc822-headers", 6);
            textTypesMap.put("text/t140", 7);
            textTypesMap.put("text/tab-separated-values", 8);
            textTypesMap.put("text/uri-list", 9);
            textTypesMap.put("text/richtext", 10);
            textTypesMap.put("text/enriched", 11);
            textTypesMap.put("text/rtf", 12);
            textTypesMap.put("text/html", 13);
            textTypesMap.put("text/xml", 14);
            textTypesMap.put("text/sgml", 15);
            textTypes = Collections.unmodifiableMap(textTypesMap);
            HashMap<Class, Integer> decodedTextRepresentationsMap = new HashMap<Class, Integer>(4, 1.0f);
            decodedTextRepresentationsMap.put(char[].class, 0);
            decodedTextRepresentationsMap.put(CharBuffer.class, 1);
            decodedTextRepresentationsMap.put(String.class, 2);
            decodedTextRepresentationsMap.put(Reader.class, 3);
            decodedTextRepresentations = Collections.unmodifiableMap(decodedTextRepresentationsMap);
            HashMap<Class<InputStream>, Integer> encodedTextRepresentationsMap = new HashMap<Class<InputStream>, Integer>(3, 1.0f);
            encodedTextRepresentationsMap.put(byte[].class, 0);
            encodedTextRepresentationsMap.put(ByteBuffer.class, 1);
            encodedTextRepresentationsMap.put(InputStream.class, 2);
            encodedTextRepresentations = Collections.unmodifiableMap(encodedTextRepresentationsMap);
        }
    }

    private static class IndexOrderComparator
    implements Comparator<Long> {
        private final Map<Long, Integer> indexMap;
        private static final Integer FALLBACK_INDEX = Integer.MIN_VALUE;

        public IndexOrderComparator(Map<Long, Integer> indexMap) {
            this.indexMap = indexMap;
        }

        @Override
        public int compare(Long obj1, Long obj2) {
            return DataFlavorUtil.compareIndices(this.indexMap, obj1, obj2, FALLBACK_INDEX);
        }
    }

    private static class TextFlavorComparator
    extends DataFlavorComparator {
        static final TextFlavorComparator INSTANCE = new TextFlavorComparator();

        private TextFlavorComparator() {
        }

        @Override
        public int compare(DataFlavor flavor1, DataFlavor flavor2) {
            if (flavor1.isFlavorTextType()) {
                if (flavor2.isFlavorTextType()) {
                    return super.compare(flavor1, flavor2);
                }
                return 1;
            }
            if (flavor2.isFlavorTextType()) {
                return -1;
            }
            return 0;
        }
    }

    private static class StandardEncodingsHolder {
        private static final SortedSet<String> standardEncodings = StandardEncodingsHolder.load();

        private StandardEncodingsHolder() {
        }

        private static SortedSet<String> load() {
            TreeSet<String> tempSet = new TreeSet<String>(DataFlavorUtil.getCharsetComparator().reversed());
            tempSet.add(StandardCharsets.US_ASCII.name());
            tempSet.add(StandardCharsets.ISO_8859_1.name());
            tempSet.add(StandardCharsets.UTF_8.name());
            tempSet.add(StandardCharsets.UTF_16BE.name());
            tempSet.add(StandardCharsets.UTF_16LE.name());
            tempSet.add(StandardCharsets.UTF_16.name());
            tempSet.add(Charset.defaultCharset().name());
            return Collections.unmodifiableSortedSet(tempSet);
        }
    }

    private static final class DefaultDesktopDatatransferService
    implements DesktopDatatransferService {
        static final DesktopDatatransferService INSTANCE = DefaultDesktopDatatransferService.getDesktopService();
        private volatile FlavorMap flavorMap;

        private DefaultDesktopDatatransferService() {
        }

        private static DesktopDatatransferService getDesktopService() {
            ServiceLoader<DesktopDatatransferService> loader = ServiceLoader.load(DesktopDatatransferService.class, null);
            Iterator<DesktopDatatransferService> iterator = loader.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return new DefaultDesktopDatatransferService();
        }

        @Override
        public void invokeOnEventThread(Runnable r) {
            r.run();
        }

        @Override
        public String getDefaultUnicodeEncoding() {
            return StandardCharsets.UTF_8.name();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public FlavorMap getFlavorMap(Supplier<FlavorMap> supplier) {
            FlavorMap map = this.flavorMap;
            if (map == null) {
                DefaultDesktopDatatransferService defaultDesktopDatatransferService = this;
                synchronized (defaultDesktopDatatransferService) {
                    map = this.flavorMap;
                    if (map == null) {
                        this.flavorMap = map = supplier.get();
                    }
                }
            }
            return map;
        }

        @Override
        public boolean isDesktopPresent() {
            return false;
        }

        @Override
        public LinkedHashSet<DataFlavor> getPlatformMappingsForNative(String nat) {
            return new LinkedHashSet<DataFlavor>();
        }

        @Override
        public LinkedHashSet<String> getPlatformMappingsForFlavor(DataFlavor df) {
            return new LinkedHashSet<String>();
        }

        @Override
        public void registerTextFlavorProperties(String nat, String charset, String eoln, String terminators) {
        }
    }

    public static class RMI {
        private static final Class<?> remoteClass = RMI.getClass("java.rmi.Remote");
        private static final Class<?> marshallObjectClass = RMI.getClass("java.rmi.MarshalledObject");
        private static final Constructor<?> marshallCtor = RMI.getConstructor(marshallObjectClass, Object.class);
        private static final Method marshallGet = RMI.getMethod(marshallObjectClass, "get", new Class[0]);

        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, null);
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static Constructor<?> getConstructor(Class<?> c, Class<?> ... types) {
            try {
                return c == null ? null : c.getDeclaredConstructor(types);
            }
            catch (NoSuchMethodException x) {
                throw new AssertionError((Object)x);
            }
        }

        private static Method getMethod(Class<?> c, String name, Class<?> ... types) {
            try {
                return c == null ? null : c.getMethod(name, types);
            }
            catch (NoSuchMethodException e) {
                throw new AssertionError((Object)e);
            }
        }

        static Class<?> remoteClass() {
            return remoteClass;
        }

        public static boolean isRemote(Class<?> c) {
            return remoteClass != null && remoteClass.isAssignableFrom(c);
        }

        public static Object newMarshalledObject(Object obj) throws IOException {
            try {
                return marshallCtor == null ? null : marshallCtor.newInstance(obj);
            }
            catch (IllegalAccessException | InstantiationException x) {
                throw new AssertionError((Object)x);
            }
            catch (InvocationTargetException x) {
                Throwable cause = x.getCause();
                if (cause instanceof IOException) {
                    throw (IOException)cause;
                }
                throw new AssertionError((Object)x);
            }
        }

        public static Object getMarshalledObject(Object obj) throws IOException, ClassNotFoundException {
            try {
                return marshallGet == null ? null : marshallGet.invoke(obj, new Object[0]);
            }
            catch (IllegalAccessException x) {
                throw new AssertionError((Object)x);
            }
            catch (InvocationTargetException x) {
                Throwable cause = x.getCause();
                if (cause instanceof IOException) {
                    throw (IOException)cause;
                }
                if (cause instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException)cause;
                }
                throw new AssertionError((Object)x);
            }
        }
    }
}

