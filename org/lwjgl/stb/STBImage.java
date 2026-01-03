/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.stb.STBIIOCallbacks
 *  org.lwjgl.system.NativeType
 */
package org.lwjgl.stb;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.LibSTB;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class STBImage {
    public static final int STBI_default = 0;
    public static final int STBI_grey = 1;
    public static final int STBI_grey_alpha = 2;
    public static final int STBI_rgb = 3;
    public static final int STBI_rgb_alpha = 4;

    protected STBImage() {
        throw new UnsupportedOperationException();
    }

    public static native long nstbi_load(long var0, long var2, long var4, long var6, int var8);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load(MemoryUtil.memAddress(filename), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            long __result = STBImage.nstbi_load(filenameEncoded, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
            ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_load_from_memory(long var0, int var2, long var3, long var5, long var7, int var9);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    public static native long nstbi_load_from_callbacks(long var0, long var2, long var4, long var6, long var8, int var10);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        long __result = STBImage.nstbi_load_from_callbacks(clbk.address(), user, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    public static native long nstbi_load_gif_from_memory(long var0, int var2, long var3, long var5, long var7, long var9, long var11, int var13);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load_gif_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int **") PointerBuffer delays, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer z, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(delays, 1);
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)z, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_gif_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(delays), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(z), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * z.get(z.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    public static native long nstbi_load_16(long var0, long var2, long var4, long var6, int var8);

    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_16(MemoryUtil.memAddress(filename), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memShortBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            long __result = STBImage.nstbi_load_16(filenameEncoded, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
            ShortBuffer shortBuffer = MemoryUtil.memShortBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
            return shortBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_load_16_from_memory(long var0, int var2, long var3, long var5, long var7, int var9);

    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_16_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memShortBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    public static native long nstbi_load_16_from_callbacks(long var0, long var2, long var4, long var6, long var8, int var10);

    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        long __result = STBImage.nstbi_load_16_from_callbacks(clbk.address(), user, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memShortBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    public static native long nstbi_loadf(long var0, long var2, long var4, long var6, int var8);

    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        long __result = STBImage.nstbi_loadf(MemoryUtil.memAddress(filename), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memFloatBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            long __result = STBImage.nstbi_loadf(filenameEncoded, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
            FloatBuffer floatBuffer = MemoryUtil.memFloatBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
            return floatBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_loadf_from_memory(long var0, int var2, long var3, long var5, long var7, int var9);

    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
        }
        long __result = STBImage.nstbi_loadf_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memFloatBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    public static native long nstbi_loadf_from_callbacks(long var0, long var2, long var4, long var6, long var8, int var10);

    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)channels_in_file, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        long __result = STBImage.nstbi_loadf_from_callbacks(clbk.address(), user, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(channels_in_file), desired_channels);
        return MemoryUtil.memFloatBufferSafe(__result, x.get(x.position()) * y.get(y.position()) * (desired_channels != 0 ? desired_channels : channels_in_file.get(channels_in_file.position())));
    }

    public static native void stbi_hdr_to_ldr_gamma(float var0);

    public static native void stbi_hdr_to_ldr_scale(float var0);

    public static native void stbi_ldr_to_hdr_gamma(float var0);

    public static native void stbi_ldr_to_hdr_scale(float var0);

    public static native int nstbi_is_hdr(long var0);

    @NativeType(value="int")
    public static boolean stbi_is_hdr(@NativeType(value="char const *") ByteBuffer filename) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
        }
        return STBImage.nstbi_is_hdr(MemoryUtil.memAddress(filename)) != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="int")
    public static boolean stbi_is_hdr(@NativeType(value="char const *") CharSequence filename) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            boolean bl = STBImage.nstbi_is_hdr(filenameEncoded) != 0;
            return bl;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbi_is_hdr_from_memory(long var0, int var2);

    @NativeType(value="int")
    public static boolean stbi_is_hdr_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer) {
        return STBImage.nstbi_is_hdr_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining()) != 0;
    }

    public static native int nstbi_is_hdr_from_callbacks(long var0, long var2);

    @NativeType(value="int")
    public static boolean stbi_is_hdr_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user) {
        if (Checks.CHECKS) {
            STBIIOCallbacks.validate((long)clbk.address());
        }
        return STBImage.nstbi_is_hdr_from_callbacks(clbk.address(), user) != 0;
    }

    public static native long nstbi_failure_reason();

    @NativeType(value="char const *")
    public static @Nullable String stbi_failure_reason() {
        long __result = STBImage.nstbi_failure_reason();
        return MemoryUtil.memASCIISafe(__result);
    }

    public static native void nstbi_image_free(long var0);

    public static void stbi_image_free(@NativeType(value="void *") ByteBuffer retval_from_stbi_load) {
        STBImage.nstbi_image_free(MemoryUtil.memAddress(retval_from_stbi_load));
    }

    public static void stbi_image_free(@NativeType(value="void *") ShortBuffer retval_from_stbi_load) {
        STBImage.nstbi_image_free(MemoryUtil.memAddress(retval_from_stbi_load));
    }

    public static void stbi_image_free(@NativeType(value="void *") FloatBuffer retval_from_stbi_load) {
        STBImage.nstbi_image_free(MemoryUtil.memAddress(retval_from_stbi_load));
    }

    public static native int nstbi_info(long var0, long var2, long var4, long var6);

    @NativeType(value="int")
    public static boolean stbi_info(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer comp) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)comp, 1);
        }
        return STBImage.nstbi_info(MemoryUtil.memAddress(filename), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(comp)) != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="int")
    public static boolean stbi_info(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer comp) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)comp, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            boolean bl = STBImage.nstbi_info(filenameEncoded, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(comp)) != 0;
            return bl;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbi_info_from_memory(long var0, int var2, long var3, long var5, long var7);

    @NativeType(value="int")
    public static boolean stbi_info_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer comp) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)comp, 1);
        }
        return STBImage.nstbi_info_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(comp)) != 0;
    }

    public static native int nstbi_info_from_callbacks(long var0, long var2, long var4, long var6, long var8);

    @NativeType(value="int")
    public static boolean stbi_info_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") IntBuffer x, @NativeType(value="int *") IntBuffer y, @NativeType(value="int *") IntBuffer comp) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x, 1);
            Checks.check((Buffer)y, 1);
            Checks.check((Buffer)comp, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        return STBImage.nstbi_info_from_callbacks(clbk.address(), user, MemoryUtil.memAddress(x), MemoryUtil.memAddress(y), MemoryUtil.memAddress(comp)) != 0;
    }

    public static native int nstbi_is_16_bit(long var0);

    @NativeType(value="int")
    public static boolean stbi_is_16_bit(@NativeType(value="char const *") ByteBuffer filename) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
        }
        return STBImage.nstbi_is_16_bit(MemoryUtil.memAddress(filename)) != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="int")
    public static boolean stbi_is_16_bit(@NativeType(value="char const *") CharSequence filename) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            boolean bl = STBImage.nstbi_is_16_bit(filenameEncoded) != 0;
            return bl;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbi_is_16_bit_from_memory(long var0, int var2);

    @NativeType(value="int")
    public static boolean stbi_is_16_bit_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer) {
        return STBImage.nstbi_is_16_bit_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining()) != 0;
    }

    public static native int nstbi_is_16_bit_from_callbacks(long var0, long var2);

    @NativeType(value="int")
    public static boolean stbi_is_16_bit_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user) {
        if (Checks.CHECKS) {
            STBIIOCallbacks.validate((long)clbk.address());
        }
        return STBImage.nstbi_is_16_bit_from_callbacks(clbk.address(), user) != 0;
    }

    public static native void nstbi_set_unpremultiply_on_load(int var0);

    public static void stbi_set_unpremultiply_on_load(@NativeType(value="int") boolean flag_true_if_should_unpremultiply) {
        STBImage.nstbi_set_unpremultiply_on_load(flag_true_if_should_unpremultiply ? 1 : 0);
    }

    public static native void nstbi_convert_iphone_png_to_rgb(int var0);

    public static void stbi_convert_iphone_png_to_rgb(@NativeType(value="int") boolean flag_true_if_should_convert) {
        STBImage.nstbi_convert_iphone_png_to_rgb(flag_true_if_should_convert ? 1 : 0);
    }

    public static native void nstbi_set_flip_vertically_on_load(int var0);

    public static void stbi_set_flip_vertically_on_load(@NativeType(value="int") boolean flag_true_if_should_flip) {
        STBImage.nstbi_set_flip_vertically_on_load(flag_true_if_should_flip ? 1 : 0);
    }

    public static native void nstbi_set_unpremultiply_on_load_thread(int var0);

    public static void stbi_set_unpremultiply_on_load_thread(@NativeType(value="int") boolean flag_true_if_should_unpremultiply) {
        STBImage.nstbi_set_unpremultiply_on_load_thread(flag_true_if_should_unpremultiply ? 1 : 0);
    }

    public static native void nstbi_convert_iphone_png_to_rgb_thread(int var0);

    public static void stbi_convert_iphone_png_to_rgb_thread(@NativeType(value="int") boolean flag_true_if_should_convert) {
        STBImage.nstbi_convert_iphone_png_to_rgb_thread(flag_true_if_should_convert ? 1 : 0);
    }

    public static native void stbi_set_flip_vertically_on_load_thread(int var0);

    public static native long nstbi_zlib_decode_malloc_guesssize(long var0, int var2, int var3, long var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char *")
    public static @Nullable ByteBuffer stbi_zlib_decode_malloc_guesssize(@NativeType(value="char const *") ByteBuffer buffer, int initial_size) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            IntBuffer outlen = stack.callocInt(1);
            long __result = STBImage.nstbi_zlib_decode_malloc_guesssize(MemoryUtil.memAddress(buffer), buffer.remaining(), initial_size, MemoryUtil.memAddress(outlen));
            ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(__result, outlen.get(0));
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_zlib_decode_malloc_guesssize_headerflag(long var0, int var2, int var3, long var4, int var6);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char *")
    public static @Nullable ByteBuffer stbi_zlib_decode_malloc_guesssize_headerflag(@NativeType(value="char const *") ByteBuffer buffer, int initial_size, @NativeType(value="int") boolean parse_header) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            IntBuffer outlen = stack.callocInt(1);
            long __result = STBImage.nstbi_zlib_decode_malloc_guesssize_headerflag(MemoryUtil.memAddress(buffer), buffer.remaining(), initial_size, MemoryUtil.memAddress(outlen), parse_header ? 1 : 0);
            ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(__result, outlen.get(0));
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_zlib_decode_malloc(long var0, int var2, long var3);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char *")
    public static @Nullable ByteBuffer stbi_zlib_decode_malloc(@NativeType(value="char const *") ByteBuffer buffer) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            IntBuffer outlen = stack.callocInt(1);
            long __result = STBImage.nstbi_zlib_decode_malloc(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(outlen));
            ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(__result, outlen.get(0));
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbi_zlib_decode_buffer(long var0, int var2, long var3, int var5);

    public static int stbi_zlib_decode_buffer(@NativeType(value="char *") ByteBuffer obuffer, @NativeType(value="char const *") ByteBuffer ibuffer) {
        return STBImage.nstbi_zlib_decode_buffer(MemoryUtil.memAddress(obuffer), obuffer.remaining(), MemoryUtil.memAddress(ibuffer), ibuffer.remaining());
    }

    public static native long nstbi_zlib_decode_noheader_malloc(long var0, int var2, long var3);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char *")
    public static @Nullable ByteBuffer stbi_zlib_decode_noheader_malloc(@NativeType(value="char const *") ByteBuffer buffer) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            IntBuffer outlen = stack.callocInt(1);
            long __result = STBImage.nstbi_zlib_decode_noheader_malloc(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(outlen));
            ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(__result, outlen.get(0));
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbi_zlib_decode_noheader_buffer(long var0, int var2, long var3, int var5);

    public static int stbi_zlib_decode_noheader_buffer(@NativeType(value="char *") ByteBuffer obuffer, @NativeType(value="char const *") ByteBuffer ibuffer) {
        return STBImage.nstbi_zlib_decode_noheader_buffer(MemoryUtil.memAddress(obuffer), obuffer.remaining(), MemoryUtil.memAddress(ibuffer), ibuffer.remaining());
    }

    public static native long nstbi_load(long var0, int[] var2, int[] var3, int[] var4, int var5);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load(MemoryUtil.memAddress(filename), x, y, channels_in_file, desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            long __result = STBImage.nstbi_load(filenameEncoded, x, y, channels_in_file, desired_channels);
            ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_load_from_memory(long var0, int var2, int[] var3, int[] var4, int[] var5, int var6);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), x, y, channels_in_file, desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    public static native long nstbi_load_from_callbacks(long var0, long var2, int[] var4, int[] var5, int[] var6, int var7);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        long __result = STBImage.nstbi_load_from_callbacks(clbk.address(), user, x, y, channels_in_file, desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    public static native long nstbi_load_gif_from_memory(long var0, int var2, long var3, int[] var5, int[] var6, int[] var7, int[] var8, int var9);

    @NativeType(value="stbi_uc *")
    public static @Nullable ByteBuffer stbi_load_gif_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int **") PointerBuffer delays, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] z, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(delays, 1);
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(z, 1);
            Checks.check(channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_gif_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), MemoryUtil.memAddress(delays), x, y, z, channels_in_file, desired_channels);
        return MemoryUtil.memByteBufferSafe(__result, x[0] * y[0] * z[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    public static native long nstbi_load_16(long var0, int[] var2, int[] var3, int[] var4, int var5);

    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_16(MemoryUtil.memAddress(filename), x, y, channels_in_file, desired_channels);
        return MemoryUtil.memShortBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            long __result = STBImage.nstbi_load_16(filenameEncoded, x, y, channels_in_file, desired_channels);
            ShortBuffer shortBuffer = MemoryUtil.memShortBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
            return shortBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_load_16_from_memory(long var0, int var2, int[] var3, int[] var4, int[] var5, int var6);

    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        long __result = STBImage.nstbi_load_16_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), x, y, channels_in_file, desired_channels);
        return MemoryUtil.memShortBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    public static native long nstbi_load_16_from_callbacks(long var0, long var2, int[] var4, int[] var5, int[] var6, int var7);

    @NativeType(value="stbi_us *")
    public static @Nullable ShortBuffer stbi_load_16_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        long __result = STBImage.nstbi_load_16_from_callbacks(clbk.address(), user, x, y, channels_in_file, desired_channels);
        return MemoryUtil.memShortBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    public static native long nstbi_loadf(long var0, int[] var2, int[] var3, int[] var4, int var5);

    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        long __result = STBImage.nstbi_loadf(MemoryUtil.memAddress(filename), x, y, channels_in_file, desired_channels);
        return MemoryUtil.memFloatBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            long __result = STBImage.nstbi_loadf(filenameEncoded, x, y, channels_in_file, desired_channels);
            FloatBuffer floatBuffer = MemoryUtil.memFloatBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
            return floatBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nstbi_loadf_from_memory(long var0, int var2, int[] var3, int[] var4, int[] var5, int var6);

    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
        }
        long __result = STBImage.nstbi_loadf_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), x, y, channels_in_file, desired_channels);
        return MemoryUtil.memFloatBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    public static native long nstbi_loadf_from_callbacks(long var0, long var2, int[] var4, int[] var5, int[] var6, int var7);

    @NativeType(value="float *")
    public static @Nullable FloatBuffer stbi_loadf_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] channels_in_file, int desired_channels) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(channels_in_file, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        long __result = STBImage.nstbi_loadf_from_callbacks(clbk.address(), user, x, y, channels_in_file, desired_channels);
        return MemoryUtil.memFloatBufferSafe(__result, x[0] * y[0] * (desired_channels != 0 ? desired_channels : channels_in_file[0]));
    }

    public static native int nstbi_info(long var0, int[] var2, int[] var3, int[] var4);

    @NativeType(value="int")
    public static boolean stbi_info(@NativeType(value="char const *") ByteBuffer filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] comp) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filename);
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(comp, 1);
        }
        return STBImage.nstbi_info(MemoryUtil.memAddress(filename), x, y, comp) != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="int")
    public static boolean stbi_info(@NativeType(value="char const *") CharSequence filename, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] comp) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(comp, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filename, true);
            long filenameEncoded = stack.getPointerAddress();
            boolean bl = STBImage.nstbi_info(filenameEncoded, x, y, comp) != 0;
            return bl;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbi_info_from_memory(long var0, int var2, int[] var3, int[] var4, int[] var5);

    @NativeType(value="int")
    public static boolean stbi_info_from_memory(@NativeType(value="stbi_uc const *") ByteBuffer buffer, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] comp) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(comp, 1);
        }
        return STBImage.nstbi_info_from_memory(MemoryUtil.memAddress(buffer), buffer.remaining(), x, y, comp) != 0;
    }

    public static native int nstbi_info_from_callbacks(long var0, long var2, int[] var4, int[] var5, int[] var6);

    @NativeType(value="int")
    public static boolean stbi_info_from_callbacks(@NativeType(value="stbi_io_callbacks const *") STBIIOCallbacks clbk, @NativeType(value="void *") long user, @NativeType(value="int *") int[] x, @NativeType(value="int *") int[] y, @NativeType(value="int *") int[] comp) {
        if (Checks.CHECKS) {
            Checks.check(x, 1);
            Checks.check(y, 1);
            Checks.check(comp, 1);
            STBIIOCallbacks.validate((long)clbk.address());
        }
        return STBImage.nstbi_info_from_callbacks(clbk.address(), user, x, y, comp) != 0;
    }

    static {
        LibSTB.initialize();
    }
}

