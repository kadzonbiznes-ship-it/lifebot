/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImFont;
import imgui.ImVec2;
import imgui.binding.ImGuiStructDestroyable;

public final class ImFontConfig
extends ImGuiStructDestroyable {
    private short[] glyphRanges;

    public ImFontConfig() {
    }

    ImFontConfig(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public native byte[] getFontData();

    public native void setFontData(byte[] var1);

    public int getFontDataSize() {
        return this.nGetFontDataSize();
    }

    public void setFontDataSize(int value) {
        this.nSetFontDataSize(value);
    }

    private native int nGetFontDataSize();

    private native void nSetFontDataSize(int var1);

    public boolean getFontDataOwnedByAtlas() {
        return this.nGetFontDataOwnedByAtlas();
    }

    public void setFontDataOwnedByAtlas(boolean value) {
        this.nSetFontDataOwnedByAtlas(value);
    }

    private native boolean nGetFontDataOwnedByAtlas();

    private native void nSetFontDataOwnedByAtlas(boolean var1);

    public int getFontNo() {
        return this.nGetFontNo();
    }

    public void setFontNo(int value) {
        this.nSetFontNo(value);
    }

    private native int nGetFontNo();

    private native void nSetFontNo(int var1);

    public float getSizePixels() {
        return this.nGetSizePixels();
    }

    public void setSizePixels(float value) {
        this.nSetSizePixels(value);
    }

    private native float nGetSizePixels();

    private native void nSetSizePixels(float var1);

    public int getOversampleH() {
        return this.nGetOversampleH();
    }

    public void setOversampleH(int value) {
        this.nSetOversampleH(value);
    }

    private native int nGetOversampleH();

    private native void nSetOversampleH(int var1);

    public int getOversampleV() {
        return this.nGetOversampleV();
    }

    public void setOversampleV(int value) {
        this.nSetOversampleV(value);
    }

    private native int nGetOversampleV();

    private native void nSetOversampleV(int var1);

    public boolean getPixelSnapH() {
        return this.nGetPixelSnapH();
    }

    public void setPixelSnapH(boolean value) {
        this.nSetPixelSnapH(value);
    }

    private native boolean nGetPixelSnapH();

    private native void nSetPixelSnapH(boolean var1);

    public ImVec2 getGlyphExtraSpacing() {
        ImVec2 dst = new ImVec2();
        this.nGetGlyphExtraSpacing(dst);
        return dst;
    }

    public float getGlyphExtraSpacingX() {
        return this.nGetGlyphExtraSpacingX();
    }

    public float getGlyphExtraSpacingY() {
        return this.nGetGlyphExtraSpacingY();
    }

    public void getGlyphExtraSpacing(ImVec2 dst) {
        this.nGetGlyphExtraSpacing(dst);
    }

    public void setGlyphExtraSpacing(ImVec2 value) {
        this.nSetGlyphExtraSpacing(value.x, value.y);
    }

    public void setGlyphExtraSpacing(float valueX, float valueY) {
        this.nSetGlyphExtraSpacing(valueX, valueY);
    }

    private native void nGetGlyphExtraSpacing(ImVec2 var1);

    private native float nGetGlyphExtraSpacingX();

    private native float nGetGlyphExtraSpacingY();

    private native void nSetGlyphExtraSpacing(float var1, float var2);

    public ImVec2 getGlyphOffset() {
        ImVec2 dst = new ImVec2();
        this.nGetGlyphOffset(dst);
        return dst;
    }

    public float getGlyphOffsetX() {
        return this.nGetGlyphOffsetX();
    }

    public float getGlyphOffsetY() {
        return this.nGetGlyphOffsetY();
    }

    public void getGlyphOffset(ImVec2 dst) {
        this.nGetGlyphOffset(dst);
    }

    public void setGlyphOffset(ImVec2 value) {
        this.nSetGlyphOffset(value.x, value.y);
    }

    public void setGlyphOffset(float valueX, float valueY) {
        this.nSetGlyphOffset(valueX, valueY);
    }

    private native void nGetGlyphOffset(ImVec2 var1);

    private native float nGetGlyphOffsetX();

    private native float nGetGlyphOffsetY();

    private native void nSetGlyphOffset(float var1, float var2);

    public short[] getGlyphRanges() {
        return this.glyphRanges;
    }

    public void setGlyphRanges(short[] glyphRanges) {
        this.glyphRanges = glyphRanges;
        this.nSetGlyphRanges(glyphRanges);
    }

    private native void nSetGlyphRanges(short[] var1);

    public float getGlyphMinAdvanceX() {
        return this.nGetGlyphMinAdvanceX();
    }

    public void setGlyphMinAdvanceX(float value) {
        this.nSetGlyphMinAdvanceX(value);
    }

    private native float nGetGlyphMinAdvanceX();

    private native void nSetGlyphMinAdvanceX(float var1);

    public float getGlyphMaxAdvanceX() {
        return this.nGetGlyphMaxAdvanceX();
    }

    public void setGlyphMaxAdvanceX(float value) {
        this.nSetGlyphMaxAdvanceX(value);
    }

    private native float nGetGlyphMaxAdvanceX();

    private native void nSetGlyphMaxAdvanceX(float var1);

    public boolean getMergeMode() {
        return this.nGetMergeMode();
    }

    public void setMergeMode(boolean value) {
        this.nSetMergeMode(value);
    }

    private native boolean nGetMergeMode();

    private native void nSetMergeMode(boolean var1);

    public int getFontBuilderFlags() {
        return this.nGetFontBuilderFlags();
    }

    public void setFontBuilderFlags(int value) {
        this.nSetFontBuilderFlags(value);
    }

    public void addFontBuilderFlags(int flags) {
        this.setFontBuilderFlags(this.getFontBuilderFlags() | flags);
    }

    public void removeFontBuilderFlags(int flags) {
        this.setFontBuilderFlags(this.getFontBuilderFlags() & ~flags);
    }

    public boolean hasFontBuilderFlags(int flags) {
        return (this.getFontBuilderFlags() & flags) != 0;
    }

    private native int nGetFontBuilderFlags();

    private native void nSetFontBuilderFlags(int var1);

    public float getRasterizerMultiply() {
        return this.nGetRasterizerMultiply();
    }

    public void setRasterizerMultiply(float value) {
        this.nSetRasterizerMultiply(value);
    }

    private native float nGetRasterizerMultiply();

    private native void nSetRasterizerMultiply(float var1);

    public short getEllipsisChar() {
        return this.nGetEllipsisChar();
    }

    public void setEllipsisChar(short value) {
        this.nSetEllipsisChar(value);
    }

    private native short nGetEllipsisChar();

    private native void nSetEllipsisChar(short var1);

    public native void setName(String var1);

    public ImFont getDstFont() {
        return new ImFont(this.nGetDstFont());
    }

    public void setDstFont(ImFont value) {
        this.nSetDstFont(value.ptr);
    }

    private native long nGetDstFont();

    private native void nSetDstFont(long var1);
}

