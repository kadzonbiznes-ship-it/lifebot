/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.NativeLibrary$3
 */
package com.sun.jna;

import com.sun.jna.Function;
import com.sun.jna.FunctionMapper;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.SymbolProvider;
import com.sun.jna.internal.Cleaner;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NativeLibrary
implements Closeable {
    private static final Logger LOG = Logger.getLogger(NativeLibrary.class.getName());
    private static final Level DEBUG_LOAD_LEVEL = Native.DEBUG_LOAD ? Level.INFO : Level.FINE;
    private static final SymbolProvider NATIVE_SYMBOL_PROVIDER = new SymbolProvider(){

        @Override
        public long getSymbolAddress(long handle, String name, SymbolProvider parent) {
            return Native.findSymbol(handle, name);
        }
    };
    private final Cleaner.Cleanable cleanable;
    private volatile long handle;
    private final String libraryName;
    private final String libraryPath;
    private final Map<String, Function> functions = new HashMap<String, Function>();
    private final SymbolProvider symbolProvider;
    private final int callFlags;
    private final String encoding;
    private final Map<String, ?> options;
    private static final Map<String, Reference<NativeLibrary>> libraries = new HashMap<String, Reference<NativeLibrary>>();
    private static final Map<String, List<String>> searchPaths = new ConcurrentHashMap<String, List<String>>();
    private static final LinkedHashSet<String> librarySearchPath = new LinkedHashSet();
    private static final int DEFAULT_OPEN_OPTIONS = -1;
    private static Method addSuppressedMethod;

    private static String functionKey(String name, int flags, String encoding) {
        return name + "|" + flags + "|" + encoding;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private NativeLibrary(String libraryName, String libraryPath, long handle, Map<String, ?> options) {
        int callingConvention;
        this.libraryName = this.getLibraryName(libraryName);
        this.libraryPath = libraryPath;
        this.handle = handle;
        this.cleanable = Cleaner.getCleaner().register(this, new NativeLibraryDisposer(handle));
        Object option = options.get("calling-convention");
        this.callFlags = callingConvention = option instanceof Number ? ((Number)option).intValue() : 0;
        this.options = options;
        SymbolProvider optionSymbolProvider = (SymbolProvider)options.get("symbol-provider");
        this.symbolProvider = optionSymbolProvider == null ? NATIVE_SYMBOL_PROVIDER : optionSymbolProvider;
        String encodingValue = (String)options.get("string-encoding");
        if (encodingValue == null) {
            encodingValue = Native.getDefaultStringEncoding();
        }
        this.encoding = encodingValue;
        if (Platform.isWindows() && "kernel32".equals(this.libraryName.toLowerCase())) {
            Map<String, Function> map = this.functions;
            synchronized (map) {
                Function f = new Function(this, "GetLastError", 63, this.encoding){

                    @Override
                    Object invoke(Object[] args, Class<?> returnType, boolean b, int fixedArgs) {
                        return Native.getLastError();
                    }

                    @Override
                    Object invoke(Method invokingMethod, Class<?>[] paramTypes, Class<?> returnType, Object[] inArgs, Map<String, ?> options) {
                        return Native.getLastError();
                    }
                };
                this.functions.put(NativeLibrary.functionKey("GetLastError", this.callFlags, this.encoding), f);
            }
        }
    }

    private static int openFlags(Map<String, ?> options) {
        Object opt = options.get("open-flags");
        if (opt instanceof Number) {
            return ((Number)opt).intValue();
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static NativeLibrary loadLibrary(String libraryName, Map<String, ?> options) {
        long handle;
        String libraryPath;
        block40: {
            String webstartPath;
            LOG.log(DEBUG_LOAD_LEVEL, "Looking for library '" + libraryName + "'");
            ArrayList<Throwable> exceptions = new ArrayList<Throwable>();
            boolean isAbsolutePath = new File(libraryName).isAbsolute();
            LinkedHashSet<String> searchPath = new LinkedHashSet<String>();
            int openFlags = NativeLibrary.openFlags(options);
            List<String> customPaths = searchPaths.get(libraryName);
            if (customPaths != null) {
                List<String> list = customPaths;
                synchronized (list) {
                    searchPath.addAll(customPaths);
                }
            }
            if ((webstartPath = Native.getWebStartLibraryPath(libraryName)) != null) {
                LOG.log(DEBUG_LOAD_LEVEL, "Adding web start path " + webstartPath);
                searchPath.add(webstartPath);
            }
            LOG.log(DEBUG_LOAD_LEVEL, "Adding paths from jna.library.path: " + System.getProperty("jna.library.path"));
            searchPath.addAll(NativeLibrary.initPaths("jna.library.path"));
            libraryPath = NativeLibrary.findLibraryPath(libraryName, searchPath);
            handle = 0L;
            try {
                LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
                handle = Native.open(libraryPath, openFlags);
            }
            catch (UnsatisfiedLinkError e) {
                LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e.getMessage());
                LOG.log(DEBUG_LOAD_LEVEL, "Adding system paths: " + librarySearchPath);
                exceptions.add(e);
                searchPath.addAll(librarySearchPath);
            }
            try {
                if (handle == 0L) {
                    libraryPath = NativeLibrary.findLibraryPath(libraryName, searchPath);
                    LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
                    handle = Native.open(libraryPath, openFlags);
                    if (handle == 0L) {
                        throw new UnsatisfiedLinkError("Failed to load library '" + libraryName + "'");
                    }
                }
            }
            catch (UnsatisfiedLinkError ule) {
                block39: {
                    LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + ule.getMessage());
                    exceptions.add(ule);
                    if (Platform.isAndroid()) {
                        try {
                            LOG.log(DEBUG_LOAD_LEVEL, "Preload (via System.loadLibrary) " + libraryName);
                            System.loadLibrary(libraryName);
                            handle = Native.open(libraryPath, openFlags);
                        }
                        catch (UnsatisfiedLinkError e2) {
                            LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
                            exceptions.add(e2);
                        }
                    } else if (Platform.isLinux() || Platform.isFreeBSD()) {
                        LOG.log(DEBUG_LOAD_LEVEL, "Looking for version variants");
                        libraryPath = NativeLibrary.matchLibrary(libraryName, searchPath);
                        if (libraryPath != null) {
                            LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
                            try {
                                handle = Native.open(libraryPath, openFlags);
                            }
                            catch (UnsatisfiedLinkError e2) {
                                LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
                                exceptions.add(e2);
                            }
                        }
                    } else if (Platform.isMac() && !libraryName.endsWith(".dylib")) {
                        for (String frameworkName : NativeLibrary.matchFramework(libraryName)) {
                            try {
                                LOG.log(DEBUG_LOAD_LEVEL, "Trying " + frameworkName);
                                handle = Native.open(frameworkName, openFlags);
                                break;
                            }
                            catch (UnsatisfiedLinkError e2) {
                                LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
                                exceptions.add(e2);
                            }
                        }
                    } else if (Platform.isWindows() && !isAbsolutePath) {
                        LOG.log(DEBUG_LOAD_LEVEL, "Looking for lib- prefix");
                        libraryPath = NativeLibrary.findLibraryPath("lib" + libraryName, searchPath);
                        if (libraryPath != null) {
                            LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
                            try {
                                handle = Native.open(libraryPath, openFlags);
                            }
                            catch (UnsatisfiedLinkError e2) {
                                LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
                                exceptions.add(e2);
                            }
                        }
                    }
                    if (handle == 0L) {
                        try {
                            File embedded = Native.extractFromResourcePath(libraryName, (ClassLoader)options.get("classloader"));
                            if (embedded == null) break block39;
                            try {
                                handle = Native.open(embedded.getAbsolutePath(), openFlags);
                                libraryPath = embedded.getAbsolutePath();
                            }
                            finally {
                                if (Native.isUnpacked(embedded)) {
                                    Native.deleteLibrary(embedded);
                                }
                            }
                        }
                        catch (IOException e2) {
                            LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
                            exceptions.add(e2);
                        }
                    }
                }
                if (handle != 0L) break block40;
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to load library '");
                sb.append(libraryName);
                sb.append("':");
                for (Throwable t : exceptions) {
                    sb.append("\n");
                    sb.append(t.getMessage());
                }
                UnsatisfiedLinkError res = new UnsatisfiedLinkError(sb.toString());
                for (Throwable t : exceptions) {
                    NativeLibrary.addSuppressedReflected(res, t);
                }
                throw res;
            }
        }
        LOG.log(DEBUG_LOAD_LEVEL, "Found library '" + libraryName + "' at " + libraryPath);
        return new NativeLibrary(libraryName, libraryPath, handle, options);
    }

    private static void addSuppressedReflected(Throwable target, Throwable suppressed) {
        if (addSuppressedMethod == null) {
            return;
        }
        try {
            addSuppressedMethod.invoke((Object)target, suppressed);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Failed to call addSuppressedMethod", ex);
        }
    }

    static String[] matchFramework(String libraryName) {
        LinkedHashSet<String> paths = new LinkedHashSet<String>();
        File framework = new File(libraryName);
        if (framework.isAbsolute()) {
            if (libraryName.contains(".framework")) {
                if (framework.exists()) {
                    return new String[]{framework.getAbsolutePath()};
                }
                paths.add(framework.getAbsolutePath());
            } else {
                if ((framework = new File(new File(framework.getParentFile(), framework.getName() + ".framework"), framework.getName())).exists()) {
                    return new String[]{framework.getAbsolutePath()};
                }
                paths.add(framework.getAbsolutePath());
            }
        } else {
            String[] PREFIXES = new String[]{System.getProperty("user.home"), "", "/System"};
            String suffix = !libraryName.contains(".framework") ? libraryName + ".framework/" + libraryName : libraryName;
            for (String prefix : PREFIXES) {
                framework = new File(prefix + "/Library/Frameworks/" + suffix);
                if (framework.exists()) {
                    return new String[]{framework.getAbsolutePath()};
                }
                paths.add(framework.getAbsolutePath());
            }
        }
        return paths.toArray(new String[0]);
    }

    private String getLibraryName(String libraryName) {
        String suffix;
        int suffixStart;
        String simplified = libraryName;
        String BASE = "---";
        String template = NativeLibrary.mapSharedLibraryName("---");
        int prefixEnd = template.indexOf("---");
        if (prefixEnd > 0 && simplified.startsWith(template.substring(0, prefixEnd))) {
            simplified = simplified.substring(prefixEnd);
        }
        if ((suffixStart = simplified.indexOf(suffix = template.substring(prefixEnd + "---".length()))) != -1) {
            simplified = simplified.substring(0, suffixStart);
        }
        return simplified;
    }

    public static final NativeLibrary getInstance(String libraryName) {
        return NativeLibrary.getInstance(libraryName, Collections.emptyMap());
    }

    public static final NativeLibrary getInstance(String libraryName, ClassLoader classLoader) {
        return NativeLibrary.getInstance(libraryName, Collections.singletonMap("classloader", classLoader));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final NativeLibrary getInstance(String libraryName, Map<String, ?> libraryOptions) {
        HashMap options = new HashMap(libraryOptions);
        if (options.get("calling-convention") == null) {
            options.put("calling-convention", 0);
        }
        if ((Platform.isLinux() || Platform.isFreeBSD() || Platform.isAIX()) && Platform.C_LIBRARY_NAME.equals(libraryName)) {
            libraryName = null;
        }
        Map<String, Reference<NativeLibrary>> map = libraries;
        synchronized (map) {
            NativeLibrary library;
            Reference<NativeLibrary> ref = libraries.get(libraryName + options);
            NativeLibrary nativeLibrary = library = ref != null ? ref.get() : null;
            if (library == null) {
                library = libraryName == null ? new NativeLibrary("<process>", null, Native.open(null, NativeLibrary.openFlags(options)), options) : NativeLibrary.loadLibrary(libraryName, options);
                ref = new WeakReference<NativeLibrary>(library);
                libraries.put(library.getName() + options, ref);
                File file = library.getFile();
                if (file != null) {
                    libraries.put(file.getAbsolutePath() + options, ref);
                    libraries.put(file.getName() + options, ref);
                }
            }
            return library;
        }
    }

    public static final synchronized NativeLibrary getProcess() {
        return NativeLibrary.getInstance(null);
    }

    public static final synchronized NativeLibrary getProcess(Map<String, ?> options) {
        return NativeLibrary.getInstance(null, options);
    }

    public static final void addSearchPath(String libraryName, String path) {
        List<String> customPaths = searchPaths.get(libraryName);
        if (customPaths == null) {
            customPaths = Collections.synchronizedList(new ArrayList());
            searchPaths.put(libraryName, customPaths);
        }
        customPaths.add(path);
    }

    public Function getFunction(String functionName) {
        return this.getFunction(functionName, this.callFlags);
    }

    Function getFunction(String name, Method method) {
        String prefix;
        FunctionMapper mapper = (FunctionMapper)this.options.get("function-mapper");
        if (mapper != null) {
            name = mapper.getFunctionName(this, method);
        }
        if (name.startsWith(prefix = System.getProperty("jna.profiler.prefix", "$$YJP$$"))) {
            name = name.substring(prefix.length());
        }
        int flags = this.callFlags;
        Class<?>[] etypes = method.getExceptionTypes();
        for (int i = 0; i < etypes.length; ++i) {
            if (!LastErrorException.class.isAssignableFrom(etypes[i])) continue;
            flags |= 0x40;
        }
        return this.getFunction(name, flags);
    }

    public Function getFunction(String functionName, int callFlags) {
        return this.getFunction(functionName, callFlags, this.encoding);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Function getFunction(String functionName, int callFlags, String encoding) {
        if (functionName == null) {
            throw new NullPointerException("Function name may not be null");
        }
        Map<String, Function> map = this.functions;
        synchronized (map) {
            String key = NativeLibrary.functionKey(functionName, callFlags, encoding);
            Function function = this.functions.get(key);
            if (function == null) {
                function = new Function(this, functionName, callFlags, encoding);
                this.functions.put(key, function);
            }
            return function;
        }
    }

    public Map<String, ?> getOptions() {
        return this.options;
    }

    public Pointer getGlobalVariableAddress(String symbolName) {
        try {
            return new Pointer(this.getSymbolAddress(symbolName));
        }
        catch (UnsatisfiedLinkError e) {
            throw new UnsatisfiedLinkError("Error looking up '" + symbolName + "': " + e.getMessage());
        }
    }

    long getSymbolAddress(String name) {
        if (this.handle == 0L) {
            throw new UnsatisfiedLinkError("Library has been unloaded");
        }
        return this.symbolProvider.getSymbolAddress(this.handle, name, NATIVE_SYMBOL_PROVIDER);
    }

    public String toString() {
        return "Native Library <" + this.libraryPath + "@" + this.handle + ">";
    }

    public String getName() {
        return this.libraryName;
    }

    public File getFile() {
        if (this.libraryPath == null) {
            return null;
        }
        return new File(this.libraryPath);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void disposeAll() {
        LinkedHashSet<Reference<NativeLibrary>> values;
        Map<String, Reference<NativeLibrary>> map = libraries;
        synchronized (map) {
            values = new LinkedHashSet<Reference<NativeLibrary>>(libraries.values());
        }
        for (Reference reference : values) {
            NativeLibrary lib = (NativeLibrary)reference.get();
            if (lib == null) continue;
            lib.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        HashSet<String> keys = new HashSet<String>();
        Object object = libraries;
        synchronized (object) {
            for (Map.Entry<String, Reference<NativeLibrary>> e : libraries.entrySet()) {
                Reference<NativeLibrary> ref = e.getValue();
                if (ref.get() != this) continue;
                keys.add(e.getKey());
            }
            for (String k : keys) {
                libraries.remove(k);
            }
        }
        object = this;
        synchronized (object) {
            if (this.handle != 0L) {
                this.handle = 0L;
                this.cleanable.clean();
            }
        }
    }

    @Deprecated
    public void dispose() {
        this.close();
    }

    private static List<String> initPaths(String key) {
        String value = System.getProperty(key, "");
        if ("".equals(value)) {
            return Collections.emptyList();
        }
        StringTokenizer st = new StringTokenizer(value, File.pathSeparator);
        ArrayList<String> list = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
            if ("".equals(path)) continue;
            list.add(path);
        }
        return list;
    }

    private static String findLibraryPath(String libName, Collection<String> searchPath) {
        if (new File(libName).isAbsolute()) {
            return libName;
        }
        String name = NativeLibrary.mapSharedLibraryName(libName);
        for (String path : searchPath) {
            File file = new File(path, name);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            if (!Platform.isMac() || !name.endsWith(".dylib") || !(file = new File(path, name.substring(0, name.lastIndexOf(".dylib")) + ".jnilib")).exists()) continue;
            return file.getAbsolutePath();
        }
        return name;
    }

    static String mapSharedLibraryName(String libName) {
        if (Platform.isMac()) {
            if (libName.startsWith("lib") && (libName.endsWith(".dylib") || libName.endsWith(".jnilib"))) {
                return libName;
            }
            String name = System.mapLibraryName(libName);
            if (name.endsWith(".jnilib")) {
                return name.substring(0, name.lastIndexOf(".jnilib")) + ".dylib";
            }
            return name;
        }
        if (Platform.isLinux() || Platform.isFreeBSD() ? NativeLibrary.isVersionedName(libName) || libName.endsWith(".so") : (Platform.isAIX() ? NativeLibrary.isVersionedName(libName) || libName.endsWith(".so") || libName.startsWith("lib") || libName.endsWith(".a") : Platform.isWindows() && (libName.endsWith(".drv") || libName.endsWith(".dll") || libName.endsWith(".ocx")))) {
            return libName;
        }
        String mappedName = System.mapLibraryName(libName);
        if (Platform.isAIX() && mappedName.endsWith(".so")) {
            return mappedName.replaceAll(".so$", ".a");
        }
        return mappedName;
    }

    private static boolean isVersionedName(String name) {
        int so;
        if (name.startsWith("lib") && (so = name.lastIndexOf(".so.")) != -1 && so + 4 < name.length()) {
            for (int i = so + 4; i < name.length(); ++i) {
                char ch = name.charAt(i);
                if (Character.isDigit(ch) || ch == '.') continue;
                return false;
            }
            return true;
        }
        return false;
    }

    static String matchLibrary(String libName, Collection<String> searchPath) {
        File lib = new File(libName);
        if (lib.isAbsolute()) {
            searchPath = Arrays.asList(lib.getParent());
        }
        3 filter = new /* Unavailable Anonymous Inner Class!! */;
        LinkedList<File> matches = new LinkedList<File>();
        for (String path : searchPath) {
            File[] files = new File(path).listFiles((FilenameFilter)filter);
            if (files == null || files.length <= 0) continue;
            matches.addAll(Arrays.asList(files));
        }
        double bestVersion = -1.0;
        String bestMatch = null;
        for (File f : matches) {
            String path = f.getAbsolutePath();
            String ver = path.substring(path.lastIndexOf(".so.") + 4);
            double version = NativeLibrary.parseVersion(ver);
            if (!(version > bestVersion)) continue;
            bestVersion = version;
            bestMatch = path;
        }
        return bestMatch;
    }

    static double parseVersion(String ver) {
        double v = 0.0;
        double divisor = 1.0;
        int dot = ver.indexOf(".");
        while (ver != null) {
            String num;
            if (dot != -1) {
                num = ver.substring(0, dot);
                ver = ver.substring(dot + 1);
                dot = ver.indexOf(".");
            } else {
                num = ver;
                ver = null;
            }
            try {
                v += (double)Integer.parseInt(num) / divisor;
            }
            catch (NumberFormatException e) {
                return 0.0;
            }
            divisor *= 100.0;
        }
        return v;
    }

    private static String getMultiArchPath() {
        String cpu = Platform.ARCH;
        String kernel = Platform.iskFreeBSD() ? "-kfreebsd" : (Platform.isGNU() ? "" : "-linux");
        String libc = "-gnu";
        if (Platform.isIntel()) {
            cpu = Platform.is64Bit() ? "x86_64" : "i386";
        } else if (Platform.isPPC()) {
            cpu = Platform.is64Bit() ? "powerpc64" : "powerpc";
        } else if (Platform.isARM()) {
            cpu = "arm";
            libc = "-gnueabi";
        } else if (Platform.ARCH.equals("mips64el")) {
            libc = "-gnuabi64";
        }
        return cpu + kernel + libc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ArrayList<String> getLinuxLdPaths() {
        ArrayList<String> ldPaths = new ArrayList<String>();
        Process process = null;
        BufferedReader reader = null;
        try {
            String buffer;
            process = Runtime.getRuntime().exec("/sbin/ldconfig -p");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((buffer = reader.readLine()) != null) {
                String path;
                int startPath = buffer.indexOf(" => ");
                int endPath = buffer.lastIndexOf(47);
                if (startPath == -1 || endPath == -1 || startPath >= endPath || ldPaths.contains(path = buffer.substring(startPath + 4, endPath))) continue;
                ldPaths.add(path);
            }
        }
        catch (Exception exception) {
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException iOException) {}
            }
            if (process != null) {
                try {
                    process.waitFor();
                }
                catch (InterruptedException interruptedException) {}
            }
        }
        return ldPaths;
    }

    static /* synthetic */ boolean access$000(String x0) {
        return NativeLibrary.isVersionedName(x0);
    }

    static {
        if (Native.POINTER_SIZE == 0) {
            throw new Error("Native library not initialized");
        }
        addSuppressedMethod = null;
        try {
            addSuppressedMethod = Throwable.class.getMethod("addSuppressed", Throwable.class);
        }
        catch (NoSuchMethodException noSuchMethodException) {
        }
        catch (SecurityException ex) {
            Logger.getLogger(NativeLibrary.class.getName()).log(Level.SEVERE, "Failed to initialize 'addSuppressed' method", ex);
        }
        String webstartPath = Native.getWebStartLibraryPath("jnidispatch");
        if (webstartPath != null) {
            librarySearchPath.add(webstartPath);
        }
        if (System.getProperty("jna.platform.library.path") == null && !Platform.isWindows()) {
            String platformPath = "";
            String sep = "";
            String archPath = "";
            if (Platform.isLinux() || Platform.isSolaris() || Platform.isFreeBSD() || Platform.iskFreeBSD()) {
                archPath = (Platform.isSolaris() ? "/" : "") + Native.POINTER_SIZE * 8;
            }
            String[] paths = new String[]{"/usr/lib" + archPath, "/lib" + archPath, "/usr/lib", "/lib"};
            if (Platform.isLinux() || Platform.iskFreeBSD() || Platform.isGNU()) {
                String multiArchPath = NativeLibrary.getMultiArchPath();
                paths = new String[]{"/usr/lib/" + multiArchPath, "/lib/" + multiArchPath, "/usr/lib" + archPath, "/lib" + archPath, "/usr/lib", "/lib"};
            }
            if (Platform.isLinux()) {
                ArrayList<String> ldPaths = NativeLibrary.getLinuxLdPaths();
                for (int i = paths.length - 1; 0 <= i; --i) {
                    int found = ldPaths.indexOf(paths[i]);
                    if (found != -1) {
                        ldPaths.remove(found);
                    }
                    ldPaths.add(0, paths[i]);
                }
                paths = ldPaths.toArray(new String[0]);
            }
            for (int i = 0; i < paths.length; ++i) {
                File dir = new File(paths[i]);
                if (!dir.exists() || !dir.isDirectory()) continue;
                platformPath = platformPath + sep + paths[i];
                sep = File.pathSeparator;
            }
            if (!"".equals(platformPath)) {
                System.setProperty("jna.platform.library.path", platformPath);
            }
        }
        librarySearchPath.addAll(NativeLibrary.initPaths("jna.platform.library.path"));
    }

    private static final class NativeLibraryDisposer
    implements Runnable {
        private long handle;

        public NativeLibraryDisposer(long handle) {
            this.handle = handle;
        }

        @Override
        public synchronized void run() {
            if (this.handle != 0L) {
                try {
                    Native.close(this.handle);
                }
                finally {
                    this.handle = 0L;
                }
            }
        }
    }
}

