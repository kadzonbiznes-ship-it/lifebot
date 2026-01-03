/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImDrawList;
import imgui.ImFontGlyph;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.binding.ImGuiStructDestroyable;

public final class ImFont
extends ImGuiStructDestroyable {
    private static final ImFontGlyph _GETFALLBACKGLYPH_1 = new ImFontGlyph(0L);

    public ImFont() {
    }

    public ImFont(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public float getFallbackAdvanceX() {
        return this.nGetFallbackAdvanceX();
    }

    public void setFallbackAdvanceX(float value) {
        this.nSetFallbackAdvanceX(value);
    }

    private native float nGetFallbackAdvanceX();

    private native void nSetFallbackAdvanceX(float var1);

    public float getFontSize() {
        return this.nGetFontSize();
    }

    public void setFontSize(float value) {
        this.nSetFontSize(value);
    }

    private native float nGetFontSize();

    private native void nSetFontSize(float var1);

    public ImFontGlyph getFallbackGlyph() {
        ImFont._GETFALLBACKGLYPH_1.ptr = this.nGetFallbackGlyph();
        return _GETFALLBACKGLYPH_1;
    }

    public void setFallbackGlyph(ImFontGlyph value) {
        this.nSetFallbackGlyph(value.ptr);
    }

    private native long nGetFallbackGlyph();

    private native void nSetFallbackGlyph(long var1);

    public short getConfigDataCount() {
        return this.nGetConfigDataCount();
    }

    public void setConfigDataCount(short value) {
        this.nSetConfigDataCount(value);
    }

    private native short nGetConfigDataCount();

    private native void nSetConfigDataCount(short var1);

    public short getEllipsisChar() {
        return this.nGetEllipsisChar();
    }

    public void setEllipsisChar(short value) {
        this.nSetEllipsisChar(value);
    }

    private native short nGetEllipsisChar();

    private native void nSetEllipsisChar(short var1);

    public short getEllipsisCharCount() {
        return this.nGetEllipsisCharCount();
    }

    public void setEllipsisCharCount(short value) {
        this.nSetEllipsisCharCount(value);
    }

    private native short nGetEllipsisCharCount();

    private native void nSetEllipsisCharCount(short var1);

    public float getEllipsisWidth() {
        return this.nGetEllipsisWidth();
    }

    public void setEllipsisWidth(float value) {
        this.nSetEllipsisWidth(value);
    }

    private native float nGetEllipsisWidth();

    private native void nSetEllipsisWidth(float var1);

    public float getEllipsisCharStep() {
        return this.nGetEllipsisCharStep();
    }

    public void setEllipsisCharStep(float value) {
        this.nSetEllipsisCharStep(value);
    }

    private native float nGetEllipsisCharStep();

    private native void nSetEllipsisCharStep(float var1);

    public boolean getDirtyLookupTables() {
        return this.nGetDirtyLookupTables();
    }

    public void setDirtyLookupTables(boolean value) {
        this.nSetDirtyLookupTables(value);
    }

    private native boolean nGetDirtyLookupTables();

    private native void nSetDirtyLookupTables(boolean var1);

    public float getScale() {
        return this.nGetScale();
    }

    public void setScale(float value) {
        this.nSetScale(value);
    }

    private native float nGetScale();

    private native void nSetScale(float var1);

    public float getAscent() {
        return this.nGetAscent();
    }

    public void setAscent(float value) {
        this.nSetAscent(value);
    }

    private native float nGetAscent();

    private native void nSetAscent(float var1);

    public float getDescent() {
        return this.nGetDescent();
    }

    public void setDescent(float value) {
        this.nSetDescent(value);
    }

    private native float nGetDescent();

    private native void nSetDescent(float var1);

    public int getMetricsTotalSurface() {
        return this.nGetMetricsTotalSurface();
    }

    public void setMetricsTotalSurface(int value) {
        this.nSetMetricsTotalSurface(value);
    }

    private native int nGetMetricsTotalSurface();

    private native void nSetMetricsTotalSurface(int var1);

    public ImFontGlyph findGlyph(int c) {
        return new ImFontGlyph(this.nFindGlyph(c));
    }

    private native long nFindGlyph(int var1);

    public ImFontGlyph findGlyphNoFallback(int c) {
        return new ImFontGlyph(this.nFindGlyphNoFallback(c));
    }

    private native long nFindGlyphNoFallback(int var1);

    public float getCharAdvance(int c) {
        return this.nGetCharAdvance(c);
    }

    private native float nGetCharAdvance(int var1);

    public boolean isLoaded() {
        return this.nIsLoaded();
    }

    private native boolean nIsLoaded();

    public String getDebugName() {
        return this.nGetDebugName();
    }

    private native String nGetDebugName();

    public ImVec2 calcTextSizeA(float size, float maxWidth, float wrapWidth, String textBegin) {
        ImVec2 dst = new ImVec2();
        this.nCalcTextSizeA(dst, size, maxWidth, wrapWidth, textBegin);
        return dst;
    }

    public float calcTextSizeAX(float size, float maxWidth, float wrapWidth, String textBegin) {
        return this.nCalcTextSizeAX(size, maxWidth, wrapWidth, textBegin);
    }

    public float calcTextSizeAY(float size, float maxWidth, float wrapWidth, String textBegin) {
        return this.nCalcTextSizeAY(size, maxWidth, wrapWidth, textBegin);
    }

    public void calcTextSizeA(ImVec2 dst, float size, float maxWidth, float wrapWidth, String textBegin) {
        this.nCalcTextSizeA(dst, size, maxWidth, wrapWidth, textBegin);
    }

    public ImVec2 calcTextSizeA(float size, float maxWidth, float wrapWidth, String textBegin, String textEnd) {
        ImVec2 dst = new ImVec2();
        this.nCalcTextSizeA(dst, size, maxWidth, wrapWidth, textBegin, textEnd);
        return dst;
    }

    public float calcTextSizeAX(float size, float maxWidth, float wrapWidth, String textBegin, String textEnd) {
        return this.nCalcTextSizeAX(size, maxWidth, wrapWidth, textBegin, textEnd);
    }

    public float calcTextSizeAY(float size, float maxWidth, float wrapWidth, String textBegin, String textEnd) {
        return this.nCalcTextSizeAY(size, maxWidth, wrapWidth, textBegin, textEnd);
    }

    public void calcTextSizeA(ImVec2 dst, float size, float maxWidth, float wrapWidth, String textBegin, String textEnd) {
        this.nCalcTextSizeA(dst, size, maxWidth, wrapWidth, textBegin, textEnd);
    }

    private native void nCalcTextSizeA(ImVec2 var1, float var2, float var3, float var4, String var5);

    private native float nCalcTextSizeAX(float var1, float var2, float var3, String var4);

    private native float nCalcTextSizeAY(float var1, float var2, float var3, String var4);

    private native void nCalcTextSizeA(ImVec2 var1, float var2, float var3, float var4, String var5, String var6);

    private native float nCalcTextSizeAX(float var1, float var2, float var3, String var4, String var5);

    private native float nCalcTextSizeAY(float var1, float var2, float var3, String var4, String var5);

    public String calcWordWrapPositionA(float scale, String text, String textEnd, float wrapWidth) {
        return this.nCalcWordWrapPositionA(scale, text, textEnd, wrapWidth);
    }

    private native String nCalcWordWrapPositionA(float var1, String var2, String var3, float var4);

    public void renderChar(ImDrawList drawList, float size, ImVec2 pos, int col, int c) {
        this.nRenderChar(drawList.ptr, size, pos.x, pos.y, col, c);
    }

    public void renderChar(ImDrawList drawList, float size, float posX, float posY, int col, int c) {
        this.nRenderChar(drawList.ptr, size, posX, posY, col, c);
    }

    private native void nRenderChar(long var1, float var3, float var4, float var5, int var6, int var7);

    public void renderText(ImDrawList drawList, float size, ImVec2 pos, int col, ImVec4 clipRect, String textBegin, String textEnd) {
        this.nRenderText(drawList.ptr, size, pos.x, pos.y, col, clipRect.x, clipRect.y, clipRect.z, clipRect.w, textBegin, textEnd);
    }

    public void renderText(ImDrawList drawList, float size, float posX, float posY, int col, float clipRectX, float clipRectY, float clipRectZ, float clipRectW, String textBegin, String textEnd) {
        this.nRenderText(drawList.ptr, size, posX, posY, col, clipRectX, clipRectY, clipRectZ, clipRectW, textBegin, textEnd);
    }

    public void renderText(ImDrawList drawList, float size, ImVec2 pos, int col, ImVec4 clipRect, String textBegin, String textEnd, float wrapWidth) {
        this.nRenderText(drawList.ptr, size, pos.x, pos.y, col, clipRect.x, clipRect.y, clipRect.z, clipRect.w, textBegin, textEnd, wrapWidth);
    }

    public void renderText(ImDrawList drawList, float size, float posX, float posY, int col, float clipRectX, float clipRectY, float clipRectZ, float clipRectW, String textBegin, String textEnd, float wrapWidth) {
        this.nRenderText(drawList.ptr, size, posX, posY, col, clipRectX, clipRectY, clipRectZ, clipRectW, textBegin, textEnd, wrapWidth);
    }

    public void renderText(ImDrawList drawList, float size, ImVec2 pos, int col, ImVec4 clipRect, String textBegin, String textEnd, float wrapWidth, boolean cpuFineClip) {
        this.nRenderText(drawList.ptr, size, pos.x, pos.y, col, clipRect.x, clipRect.y, clipRect.z, clipRect.w, textBegin, textEnd, wrapWidth, cpuFineClip);
    }

    public void renderText(ImDrawList drawList, float size, float posX, float posY, int col, float clipRectX, float clipRectY, float clipRectZ, float clipRectW, String textBegin, String textEnd, float wrapWidth, boolean cpuFineClip) {
        this.nRenderText(drawList.ptr, size, posX, posY, col, clipRectX, clipRectY, clipRectZ, clipRectW, textBegin, textEnd, wrapWidth, cpuFineClip);
    }

    public void renderText(ImDrawList drawList, float size, ImVec2 pos, int col, ImVec4 clipRect, String textBegin, String textEnd, boolean cpuFineClip) {
        this.nRenderText(drawList.ptr, size, pos.x, pos.y, col, clipRect.x, clipRect.y, clipRect.z, clipRect.w, textBegin, textEnd, cpuFineClip);
    }

    public void renderText(ImDrawList drawList, float size, float posX, float posY, int col, float clipRectX, float clipRectY, float clipRectZ, float clipRectW, String textBegin, String textEnd, boolean cpuFineClip) {
        this.nRenderText(drawList.ptr, size, posX, posY, col, clipRectX, clipRectY, clipRectZ, clipRectW, textBegin, textEnd, cpuFineClip);
    }

    private native void nRenderText(long var1, float var3, float var4, float var5, int var6, float var7, float var8, float var9, float var10, String var11, String var12);

    private native void nRenderText(long var1, float var3, float var4, float var5, int var6, float var7, float var8, float var9, float var10, String var11, String var12, float var13);

    private native void nRenderText(long var1, float var3, float var4, float var5, int var6, float var7, float var8, float var9, float var10, String var11, String var12, float var13, boolean var14);

    private native void nRenderText(long var1, float var3, float var4, float var5, int var6, float var7, float var8, float var9, float var10, String var11, String var12, boolean var13);
}

