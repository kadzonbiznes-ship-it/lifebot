/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImVec2;
import imgui.binding.ImGuiStructDestroyable;
import imgui.type.ImInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImFontAtlas
extends ImGuiStructDestroyable {
    private ByteBuffer alpha8pixels = null;
    private ByteBuffer rgba32pixels = null;

    public ImFontAtlas() {
    }

    public ImFontAtlas(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    static native void nInit();

    private native long nCreate();

    public ImFont addFont(ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFont(imFontConfig.ptr));
    }

    private native long nAddFont(long var1);

    public ImFont addFontDefault() {
        return new ImFont(this.nAddFontDefault());
    }

    public ImFont addFontDefault(ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontDefault(imFontConfig.ptr));
    }

    private native long nAddFontDefault();

    private native long nAddFontDefault(long var1);

    public ImFont addFontFromFileTTF(String filename, float sizePixels) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels));
    }

    public ImFont addFontFromFileTTF(String filename, float sizePixels, ImFontConfig fontConfig) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels, fontConfig.ptr));
    }

    public ImFont addFontFromFileTTF(String filename, float sizePixels, ImFontConfig fontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels, fontConfig.ptr, glyphRanges));
    }

    public ImFont addFontFromFileTTF(String filename, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels, glyphRanges));
    }

    private native long nAddFontFromFileTTF(String var1, float var2);

    private native long nAddFontFromFileTTF(String var1, float var2, long var3);

    private native long nAddFontFromFileTTF(String var1, float var2, long var3, short[] var5);

    private native long nAddFontFromFileTTF(String var1, float var2, short[] var3);

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, sizePixels));
    }

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels, ImFontConfig fontConfig) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, sizePixels, fontConfig.ptr));
    }

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels, ImFontConfig fontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, sizePixels, fontConfig.ptr, glyphRanges));
    }

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, sizePixels, glyphRanges));
    }

    private native long nAddFontFromMemoryTTF(byte[] var1, float var2);

    private native long nAddFontFromMemoryTTF(byte[] var1, float var2, long var3);

    private native long nAddFontFromMemoryTTF(byte[] var1, float var2, long var3, short[] var5);

    private native long nAddFontFromMemoryTTF(byte[] var1, float var2, short[] var3);

    public ImFont addFontFromMemoryTTF(byte[] fontData, int fontSize, float sizePixels) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontSize, sizePixels));
    }

    public ImFont addFontFromMemoryTTF(byte[] fontData, int fontSize, float sizePixels, ImFontConfig fontConfig) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontSize, sizePixels, fontConfig.ptr));
    }

    public ImFont addFontFromMemoryTTF(byte[] fontData, int fontSize, float sizePixels, ImFontConfig fontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontSize, sizePixels, fontConfig.ptr, glyphRanges));
    }

    public ImFont addFontFromMemoryTTF(byte[] fontData, int fontSize, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontSize, sizePixels, glyphRanges));
    }

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3);

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3, long var4);

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3, long var4, short[] var6);

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3, short[] var4);

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, sizePixels));
    }

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels, ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, sizePixels, imFontConfig.ptr));
    }

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels, ImFontConfig imFontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, sizePixels, imFontConfig.ptr, glyphRanges));
    }

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, sizePixels, glyphRanges));
    }

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, float var2);

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, float var2, long var3);

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, float var2, long var3, short[] var5);

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, float var2, short[] var3);

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, int compressedFontSize, float sizePixels) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontSize, sizePixels));
    }

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, int compressedFontSize, float sizePixels, ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontSize, sizePixels, imFontConfig.ptr));
    }

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, int compressedFontSize, float sizePixels, ImFontConfig imFontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontSize, sizePixels, imFontConfig.ptr, glyphRanges));
    }

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, int compressedFontSize, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontSize, sizePixels, glyphRanges));
    }

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3);

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3, long var4);

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3, long var4, short[] var6);

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3, short[] var4);

    public ImFont addFontFromMemoryCompressedBase85TTF(String compressedFontDataBase85, float sizePixels, ImFontConfig fontConfig) {
        return new ImFont(this.nAddFontFromMemoryCompressedBase85TTF(compressedFontDataBase85, sizePixels, fontConfig.ptr));
    }

    public ImFont addFontFromMemoryCompressedBase85TTF(String compressedFontDataBase85, float sizePixels, ImFontConfig fontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedBase85TTF(compressedFontDataBase85, sizePixels, fontConfig.ptr, glyphRanges));
    }

    private native long nAddFontFromMemoryCompressedBase85TTF(String var1, float var2, long var3);

    private native long nAddFontFromMemoryCompressedBase85TTF(String var1, float var2, long var3, short[] var5);

    public void clearInputData() {
        this.nClearInputData();
    }

    private native void nClearInputData();

    public void clearTexData() {
        this.nClearTexData();
    }

    private native void nClearTexData();

    public void clearFonts() {
        this.nClearFonts();
    }

    private native void nClearFonts();

    public void clear() {
        this.nClear();
    }

    private native void nClear();

    public native void setFreeTypeRenderer(boolean var1);

    public boolean build() {
        return this.nBuild();
    }

    private native boolean nBuild();

    public ByteBuffer getTexDataAsAlpha8(ImInt outWidth, ImInt outHeight) {
        return this.getTexDataAsAlpha8(outWidth, outHeight, new ImInt());
    }

    public ByteBuffer getTexDataAsAlpha8(ImInt outWidth, ImInt outHeight, ImInt outBytesPerPixel) {
        this.getTexDataAsAlpha8(outWidth.getData(), outHeight.getData(), outBytesPerPixel.getData());
        return this.alpha8pixels;
    }

    private ByteBuffer createAlpha8Pixels(int size) {
        if (this.alpha8pixels == null || this.alpha8pixels.limit() != size) {
            this.alpha8pixels = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        } else {
            this.alpha8pixels.clear();
        }
        return this.alpha8pixels;
    }

    private native void getTexDataAsAlpha8(int[] var1, int[] var2, int[] var3);

    public ByteBuffer getTexDataAsRGBA32(ImInt outWidth, ImInt outHeight) {
        return this.getTexDataAsRGBA32(outWidth, outHeight, new ImInt());
    }

    public ByteBuffer getTexDataAsRGBA32(ImInt outWidth, ImInt outHeight, ImInt outBytesPerPixel) {
        this.nGetTexDataAsRGBA32(outWidth.getData(), outHeight.getData(), outBytesPerPixel.getData());
        return this.rgba32pixels;
    }

    private ByteBuffer createRgba32Pixels(int size) {
        if (this.rgba32pixels == null || this.rgba32pixels.limit() != size) {
            this.rgba32pixels = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        } else {
            this.rgba32pixels.clear();
        }
        return this.rgba32pixels;
    }

    private native void nGetTexDataAsRGBA32(int[] var1, int[] var2, int[] var3);

    public boolean isBuilt() {
        return this.nIsBuilt();
    }

    private native boolean nIsBuilt();

    public void setTexID(long textureID) {
        this.nSetTexID(textureID);
    }

    private native void nSetTexID(long var1);

    public native short[] getGlyphRangesDefault();

    public native short[] getGlyphRangesGreek();

    public native short[] getGlyphRangesKorean();

    public native short[] getGlyphRangesJapanese();

    public native short[] getGlyphRangesChineseFull();

    public native short[] getGlyphRangesChineseSimplifiedCommon();

    public native short[] getGlyphRangesCyrillic();

    public native short[] getGlyphRangesThai();

    public native short[] getGlyphRangesVietnamese();

    public int addCustomRectRegular(int width, int height) {
        return this.nAddCustomRectRegular(width, height);
    }

    private native int nAddCustomRectRegular(int var1, int var2);

    public int addCustomRectFontGlyph(ImFont imFont, short id, int width, int height, float advanceX) {
        return this.nAddCustomRectFontGlyph(imFont.ptr, id, width, height, advanceX);
    }

    public int addCustomRectFontGlyph(ImFont imFont, short id, int width, int height, float advanceX, ImVec2 offset) {
        return this.nAddCustomRectFontGlyph(imFont.ptr, id, width, height, advanceX, offset.x, offset.y);
    }

    public int addCustomRectFontGlyph(ImFont imFont, short id, int width, int height, float advanceX, float offsetX, float offsetY) {
        return this.nAddCustomRectFontGlyph(imFont.ptr, id, width, height, advanceX, offsetX, offsetY);
    }

    private native int nAddCustomRectFontGlyph(long var1, short var3, int var4, int var5, float var6);

    private native int nAddCustomRectFontGlyph(long var1, short var3, int var4, int var5, float var6, float var7, float var8);

    public int getFlags() {
        return this.nGetFlags();
    }

    public void setFlags(int value) {
        this.nSetFlags(value);
    }

    public void addFlags(int flags) {
        this.setFlags(this.getFlags() | flags);
    }

    public void removeFlags(int flags) {
        this.setFlags(this.getFlags() & ~flags);
    }

    public boolean hasFlags(int flags) {
        return (this.getFlags() & flags) != 0;
    }

    private native int nGetFlags();

    private native void nSetFlags(int var1);

    public int getTexDesiredWidth() {
        return this.nGetTexDesiredWidth();
    }

    public void setTexDesiredWidth(int value) {
        this.nSetTexDesiredWidth(value);
    }

    private native int nGetTexDesiredWidth();

    private native void nSetTexDesiredWidth(int var1);

    public int getTexGlyphPadding() {
        return this.nGetTexGlyphPadding();
    }

    public void setTexGlyphPadding(int value) {
        this.nSetTexGlyphPadding(value);
    }

    private native int nGetTexGlyphPadding();

    private native void nSetTexGlyphPadding(int var1);

    public boolean getLocked() {
        return this.nGetLocked();
    }

    public void setLocked(boolean value) {
        this.nSetLocked(value);
    }

    private native boolean nGetLocked();

    private native void nSetLocked(boolean var1);
}

