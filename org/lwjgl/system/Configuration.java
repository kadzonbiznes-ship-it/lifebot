/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.system;

import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Platform;

public class Configuration<T> {
    public static final Configuration<String> LIBRARY_PATH = new Configuration<String>("org.lwjgl.librarypath", StateInit.STRING);
    public static final Configuration<Object> BUNDLED_LIBRARY_NAME_MAPPER = new Configuration<String>("org.lwjgl.system.bundledLibrary.nameMapper", StateInit.STRING);
    public static final Configuration<Object> BUNDLED_LIBRARY_PATH_MAPPER = new Configuration<String>("org.lwjgl.system.bundledLibrary.pathMapper", StateInit.STRING);
    public static final Configuration<String> SHARED_LIBRARY_EXTRACT_DIRECTORY = new Configuration<String>("org.lwjgl.system.SharedLibraryExtractDirectory", StateInit.STRING);
    public static final Configuration<String> SHARED_LIBRARY_EXTRACT_PATH = new Configuration<String>("org.lwjgl.system.SharedLibraryExtractPath", StateInit.STRING);
    public static final Configuration<Boolean> SHARED_LIBRARY_EXTRACT_FORCE = new Configuration<Boolean>("org.lwjgl.system.SharedLibraryExtractForce", StateInit.BOOLEAN);
    public static final Configuration<Boolean> EMULATE_SYSTEM_LOADLIBRARY = new Configuration<Boolean>("org.lwjgl.system.EmulateSystemLoadLibrary", StateInit.BOOLEAN);
    public static final Configuration<String> LIBRARY_NAME = new Configuration<String>("org.lwjgl.libname", StateInit.STRING);
    public static final Configuration<Object> MEMORY_ALLOCATOR = new Configuration<String>("org.lwjgl.system.allocator", StateInit.STRING);
    public static final Configuration<Integer> STACK_SIZE = new Configuration<Integer>("org.lwjgl.system.stackSize", StateInit.INT);
    public static final Configuration<Integer> ARRAY_TLC_SIZE = new Configuration<Integer>("org.lwjgl.system.arrayTLCSize", StateInit.INT);
    public static final Configuration<Integer> JNI_NATIVE_INTERFACE_FUNCTION_COUNT = new Configuration<Integer>("org.lwjgl.system.JNINativeInterfaceSize", StateInit.INT);
    public static final Configuration<Boolean> DISABLE_CHECKS = new Configuration<Boolean>("org.lwjgl.util.NoChecks", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DISABLE_FUNCTION_CHECKS = new Configuration<Boolean>("org.lwjgl.util.NoFunctionChecks", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DISABLE_HASH_CHECKS = new Configuration<Boolean>("org.lwjgl.util.NoHashChecks", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DEBUG = new Configuration<Boolean>("org.lwjgl.util.Debug", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DEBUG_LOADER = new Configuration<Boolean>("org.lwjgl.util.DebugLoader", StateInit.BOOLEAN);
    public static final Configuration<Object> DEBUG_STREAM = new Configuration<String>("org.lwjgl.util.DebugStream", StateInit.STRING);
    public static final Configuration<Boolean> DEBUG_MEMORY_ALLOCATOR = new Configuration<Boolean>("org.lwjgl.util.DebugAllocator", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DEBUG_MEMORY_ALLOCATOR_INTERNAL = new Configuration<Boolean>("org.lwjgl.util.DebugAllocator.internal", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DEBUG_MEMORY_ALLOCATOR_FAST = new Configuration<Boolean>("org.lwjgl.util.DebugAllocator.fast", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DEBUG_STACK = new Configuration<Boolean>("org.lwjgl.util.DebugStack", StateInit.BOOLEAN);
    public static final Configuration<Boolean> DEBUG_FUNCTIONS = new Configuration<Boolean>("org.lwjgl.util.DebugFunctions", StateInit.BOOLEAN);
    public static final Configuration<String> ASSIMP_LIBRARY_NAME = new Configuration<String>("org.lwjgl.assimp.libname", StateInit.STRING);
    public static final Configuration<String> ASSIMP_DRACO_LIBRARY_NAME = new Configuration<String>("org.lwjgl.assimp.draco.libname", StateInit.STRING);
    public static final Configuration<String> BGFX_LIBRARY_NAME = new Configuration<String>("org.lwjgl.bgfx.libname", StateInit.STRING);
    public static final Configuration<String> CUDA_LIBRARY_NAME = new Configuration<String>("org.lwjgl.cuda.libname", StateInit.STRING);
    public static final Configuration<String> CUDA_TOOLKIT_VERSION = new Configuration<String>("org.lwjgl.cuda.toolkit.version", StateInit.STRING);
    public static final Configuration<String> CUDA_TOOLKIT_PATH = new Configuration<String>("org.lwjgl.cuda.toolkit.path", StateInit.STRING);
    public static final Configuration<String> CUDA_NVRTC_LIBRARY_NAME = new Configuration<String>("org.lwjgl.cuda.nvrtc.libname", StateInit.STRING);
    public static final Configuration<String> CUDA_NVRTC_BUILTINS_LIBRARY_NAME = new Configuration<String>("org.lwjgl.cuda.nvrtc-builtins.libname", StateInit.STRING);
    public static final Configuration<Boolean> CUDA_API_PER_THREAD_DEFAULT_STREAM = new Configuration<Boolean>("org.lwjgl.cuda.ptds", StateInit.BOOLEAN);
    public static final Configuration<Boolean> EGL_EXPLICIT_INIT = new Configuration<Boolean>("org.lwjgl.egl.explicitInit", StateInit.BOOLEAN);
    public static final Configuration<String> EGL_LIBRARY_NAME = new Configuration<String>("org.lwjgl.egl.libname", StateInit.STRING);
    public static final Configuration<Object> EGL_EXTENSION_FILTER = new Configuration<String>("org.lwjgl.egl.extensionFilter", StateInit.STRING);
    public static final Configuration<String> FMOD_LIBRARY_NAME = new Configuration<String>("org.lwjgl.fmod.libname", StateInit.STRING);
    public static final Configuration<String> FMOD_STUDIO_LIBRARY_NAME = new Configuration<String>("org.lwjgl.fmod.studio.libname", StateInit.STRING);
    public static final Configuration<String> FMOD_FSBANK_LIBRARY_NAME = new Configuration<String>("org.lwjgl.fmod.fsbank.libname", StateInit.STRING);
    public static final Configuration<String> FREETYPE_LIBRARY_NAME = new Configuration<String>("org.lwjgl.freetype.libname", StateInit.STRING);
    public static final Configuration<String> GLFW_LIBRARY_NAME = new Configuration<String>("org.lwjgl.glfw.libname", StateInit.STRING);
    public static final Configuration<Boolean> GLFW_CHECK_THREAD0 = new Configuration<Boolean>("org.lwjgl.glfw.checkThread0", StateInit.BOOLEAN);
    public static final Configuration<Object> HARFBUZZ_LIBRARY_NAME = new Configuration<String>("org.lwjgl.harfbuzz.libname", StateInit.STRING);
    public static final Configuration<String> HWLOC_LIBRARY_NAME = new Configuration<String>("org.lwjgl.hwloc.libname", StateInit.STRING);
    public static final Configuration<String> JAWT_LIBRARY_NAME = new Configuration<String>("org.lwjgl.system.jawt.libname", StateInit.STRING);
    public static final Configuration<String> JEMALLOC_LIBRARY_NAME = new Configuration<String>("org.lwjgl.system.jemalloc.libname", StateInit.STRING);
    public static final Configuration<String> KTX_LIBRARY_NAME = new Configuration<String>("org.lwjgl.ktx.libname", StateInit.STRING);
    public static final Configuration<String> LLVM_LIBRARY_NAME = new Configuration<String>("org.lwjgl.llvm.libname", StateInit.STRING);
    public static final Configuration<String> LLVM_CLANG_LIBRARY_NAME = new Configuration<String>("org.lwjgl.llvm.clang.libname", StateInit.STRING);
    public static final Configuration<String> LLVM_LTO_LIBRARY_NAME = new Configuration<String>("org.lwjgl.llvm.lto.libname", StateInit.STRING);
    public static final Configuration<Boolean> NFD_LINUX_PORTAL = new Configuration<Boolean>("org.lwjgl.nfd.linux.portal", StateInit.BOOLEAN);
    public static final Configuration<String> ODBC_LIBRARY_NAME = new Configuration<String>("org.lwjgl.odbc.libname", StateInit.STRING);
    public static final Configuration<Boolean> OPENAL_EXPLICIT_INIT = new Configuration<Boolean>("org.lwjgl.openal.explicitInit", StateInit.BOOLEAN);
    public static final Configuration<String> OPENAL_LIBRARY_NAME = new Configuration<String>("org.lwjgl.openal.libname", StateInit.STRING);
    public static final Configuration<Object> OPENAL_EXTENSION_FILTER = new Configuration<String>("org.lwjgl.openal.extensionFilter", StateInit.STRING);
    public static final Configuration<Boolean> OPENCL_EXPLICIT_INIT = new Configuration<Boolean>("org.lwjgl.opencl.explicitInit", StateInit.BOOLEAN);
    public static final Configuration<String> OPENCL_LIBRARY_NAME = new Configuration<String>("org.lwjgl.opencl.libname", StateInit.STRING);
    public static final Configuration<Object> OPENCL_EXTENSION_FILTER = new Configuration<String>("org.lwjgl.opencl.extensionFilter", StateInit.STRING);
    public static final Configuration<Boolean> OPENGL_EXPLICIT_INIT = new Configuration<Boolean>("org.lwjgl.opengl.explicitInit", StateInit.BOOLEAN);
    public static final Configuration<String> OPENGL_LIBRARY_NAME = new Configuration<String>("org.lwjgl.opengl.libname", StateInit.STRING);
    public static final Configuration<String> OPENGL_OSMESA_LIBRARY_NAME = new Configuration<String>("org.lwjgl.opengl.osmesa.libname", StateInit.STRING);
    public static final Configuration<Object> OPENGL_MAXVERSION = new Configuration<String>("org.lwjgl.opengl.maxVersion", StateInit.STRING);
    public static final Configuration<Object> OPENGL_EXTENSION_FILTER = new Configuration<String>("org.lwjgl.opengl.extensionFilter", StateInit.STRING);
    public static final Configuration<String> OPENGL_CONTEXT_API = new Configuration<String>("org.lwjgl.opengl.contextAPI", StateInit.STRING);
    public static final Configuration<Boolean> OPENGLES_EXPLICIT_INIT = new Configuration<Boolean>("org.lwjgl.opengles.explicitInit", StateInit.BOOLEAN);
    public static final Configuration<String> OPENGLES_LIBRARY_NAME = new Configuration<String>("org.lwjgl.opengles.libname", StateInit.STRING);
    public static final Configuration<Object> OPENGLES_MAXVERSION = new Configuration<String>("org.lwjgl.opengles.maxVersion", StateInit.STRING);
    public static final Configuration<Object> OPENGLES_EXTENSION_FILTER = new Configuration<String>("org.lwjgl.opengles.extensionFilter", StateInit.STRING);
    public static final Configuration<String> OPENGLES_CONTEXT_API = new Configuration<String>("org.lwjgl.opengles.contextAPI", StateInit.STRING);
    public static final Configuration<String> OPENVR_LIBRARY_NAME = new Configuration<String>("org.lwjgl.openvr.libname", StateInit.STRING);
    public static final Configuration<Boolean> OPENXR_EXPLICIT_INIT = new Configuration<Boolean>("org.lwjgl.openxr.explicitInit", StateInit.BOOLEAN);
    public static final Configuration<String> OPENXR_LIBRARY_NAME = new Configuration<String>("org.lwjgl.openxr.libname", StateInit.STRING);
    public static final Configuration<String> OPUS_LIBRARY_NAME = new Configuration<String>("org.lwjgl.opus.libname", StateInit.STRING);
    public static final Configuration<String> SHADERC_LIBRARY_NAME = new Configuration<String>("org.lwjgl.shaderc.libname", StateInit.STRING);
    public static final Configuration<String> SPVC_LIBRARY_NAME = new Configuration<String>("org.lwjgl.spvc.libname", StateInit.STRING);
    public static final Configuration<Boolean> VULKAN_EXPLICIT_INIT = new Configuration<Boolean>("org.lwjgl.vulkan.explicitInit", StateInit.BOOLEAN);
    public static final Configuration<String> VULKAN_LIBRARY_NAME = new Configuration<String>("org.lwjgl.vulkan.libname", StateInit.STRING);
    private final String property;
    private volatile @Nullable T state;

    public static String[] EGL_LIBRARY_NAME_DEFAULTS() {
        switch (Platform.get()) {
            case FREEBSD: 
            case LINUX: {
                return new String[]{"libEGL.so.1"};
            }
            case MACOSX: {
                return new String[]{"EGL"};
            }
            case WINDOWS: {
                return new String[]{"libEGL", "EGL"};
            }
        }
        throw new IllegalStateException();
    }

    public static String[] OPENGL_LIBRARY_NAME_DEFAULTS() {
        switch (Platform.get()) {
            case FREEBSD: 
            case LINUX: {
                return new String[]{"libGLX.so.0", "libGL.so.1", "libGL.so"};
            }
            case MACOSX: {
                return new String[]{"/System/Library/Frameworks/OpenGL.framework/Versions/Current/OpenGL"};
            }
            case WINDOWS: {
                return new String[]{"opengl32"};
            }
        }
        throw new IllegalStateException();
    }

    public static String[] OPENGL_OSMESA_LIBRARY_NAME_DEFAULTS() {
        switch (Platform.get()) {
            case FREEBSD: 
            case LINUX: {
                return new String[]{"libOSMesa.so.8", "libOSMesa.so.6", "libOSMesa.so"};
            }
            case MACOSX: {
                return new String[]{"libOSMesa.8.dylib"};
            }
            case WINDOWS: {
                return new String[]{"libOSMesa", "OSMesa"};
            }
        }
        throw new IllegalStateException();
    }

    public static String[] OPENGLES_LIBRARY_NAME_DEFAULTS() {
        switch (Platform.get()) {
            case FREEBSD: 
            case LINUX: {
                return new String[]{"libGLESv2.so.2"};
            }
            case MACOSX: {
                return new String[]{"GLESv2"};
            }
            case WINDOWS: {
                return new String[]{"libGLESv2", "GLESv2"};
            }
        }
        throw new IllegalStateException();
    }

    Configuration(String property, StateInit<? extends T> init) {
        this.property = property;
        this.state = init.apply(property);
    }

    public String getProperty() {
        return this.property;
    }

    public void set(@Nullable T value) {
        this.state = value;
    }

    public @Nullable T get() {
        return this.state;
    }

    public T get(T defaultValue) {
        T state = this.state;
        if (state == null) {
            state = defaultValue;
        }
        return state;
    }

    private static interface StateInit<T>
    extends Function<String, T> {
        public static final StateInit<Boolean> BOOLEAN = property -> {
            String value = System.getProperty(property);
            return value == null ? null : Boolean.valueOf(Boolean.parseBoolean(value));
        };
        public static final StateInit<Integer> INT = Integer::getInteger;
        public static final StateInit<String> STRING = System::getProperty;
    }
}

