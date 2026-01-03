/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImFont;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.binding.ImGuiStruct;

public final class ImDrawList
extends ImGuiStruct {
    public ImDrawList(long ptr) {
        super(ptr);
    }

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

    public void pushClipRect(ImVec2 clipRectMin, ImVec2 clipRectMax) {
        this.nPushClipRect(clipRectMin.x, clipRectMin.y, clipRectMax.x, clipRectMax.y);
    }

    public void pushClipRect(float clipRectMinX, float clipRectMinY, float clipRectMaxX, float clipRectMaxY) {
        this.nPushClipRect(clipRectMinX, clipRectMinY, clipRectMaxX, clipRectMaxY);
    }

    public void pushClipRect(ImVec2 clipRectMin, ImVec2 clipRectMax, boolean intersectWithCurrentClipRect) {
        this.nPushClipRect(clipRectMin.x, clipRectMin.y, clipRectMax.x, clipRectMax.y, intersectWithCurrentClipRect);
    }

    public void pushClipRect(float clipRectMinX, float clipRectMinY, float clipRectMaxX, float clipRectMaxY, boolean intersectWithCurrentClipRect) {
        this.nPushClipRect(clipRectMinX, clipRectMinY, clipRectMaxX, clipRectMaxY, intersectWithCurrentClipRect);
    }

    private native void nPushClipRect(float var1, float var2, float var3, float var4);

    private native void nPushClipRect(float var1, float var2, float var3, float var4, boolean var5);

    public void pushClipRectFullScreen() {
        this.nPushClipRectFullScreen();
    }

    private native void nPushClipRectFullScreen();

    public void popClipRect() {
        this.nPopClipRect();
    }

    private native void nPopClipRect();

    public void pushTextureID(long textureId) {
        this.nPushTextureID(textureId);
    }

    private native void nPushTextureID(long var1);

    public void popTextureID() {
        this.nPopTextureID();
    }

    private native void nPopTextureID();

    public ImVec2 getClipRectMin() {
        ImVec2 dst = new ImVec2();
        this.nGetClipRectMin(dst);
        return dst;
    }

    public float getClipRectMinX() {
        return this.nGetClipRectMinX();
    }

    public float getClipRectMinY() {
        return this.nGetClipRectMinY();
    }

    public void getClipRectMin(ImVec2 dst) {
        this.nGetClipRectMin(dst);
    }

    private native void nGetClipRectMin(ImVec2 var1);

    private native float nGetClipRectMinX();

    private native float nGetClipRectMinY();

    public ImVec2 getClipRectMax() {
        ImVec2 dst = new ImVec2();
        this.nGetClipRectMax(dst);
        return dst;
    }

    public float getClipRectMaxX() {
        return this.nGetClipRectMaxX();
    }

    public float getClipRectMaxY() {
        return this.nGetClipRectMaxY();
    }

    public void getClipRectMax(ImVec2 dst) {
        this.nGetClipRectMax(dst);
    }

    private native void nGetClipRectMax(ImVec2 var1);

    private native float nGetClipRectMaxX();

    private native float nGetClipRectMaxY();

    public void addLine(ImVec2 p1, ImVec2 p2, int col) {
        this.nAddLine(p1.x, p1.y, p2.x, p2.y, col);
    }

    public void addLine(float p1X, float p1Y, float p2X, float p2Y, int col) {
        this.nAddLine(p1X, p1Y, p2X, p2Y, col);
    }

    public void addLine(ImVec2 p1, ImVec2 p2, int col, float thickness) {
        this.nAddLine(p1.x, p1.y, p2.x, p2.y, col, thickness);
    }

    public void addLine(float p1X, float p1Y, float p2X, float p2Y, int col, float thickness) {
        this.nAddLine(p1X, p1Y, p2X, p2Y, col, thickness);
    }

    private native void nAddLine(float var1, float var2, float var3, float var4, int var5);

    private native void nAddLine(float var1, float var2, float var3, float var4, int var5, float var6);

    public void addRect(ImVec2 pMin, ImVec2 pMax, int col) {
        this.nAddRect(pMin.x, pMin.y, pMax.x, pMax.y, col);
    }

    public void addRect(float pMinX, float pMinY, float pMaxX, float pMaxY, int col) {
        this.nAddRect(pMinX, pMinY, pMaxX, pMaxY, col);
    }

    public void addRect(ImVec2 pMin, ImVec2 pMax, int col, float rounding) {
        this.nAddRect(pMin.x, pMin.y, pMax.x, pMax.y, col, rounding);
    }

    public void addRect(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, float rounding) {
        this.nAddRect(pMinX, pMinY, pMaxX, pMaxY, col, rounding);
    }

    public void addRect(ImVec2 pMin, ImVec2 pMax, int col, float rounding, int flags) {
        this.nAddRect(pMin.x, pMin.y, pMax.x, pMax.y, col, rounding, flags);
    }

    public void addRect(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, float rounding, int flags) {
        this.nAddRect(pMinX, pMinY, pMaxX, pMaxY, col, rounding, flags);
    }

    public void addRect(ImVec2 pMin, ImVec2 pMax, int col, float rounding, int flags, float thickness) {
        this.nAddRect(pMin.x, pMin.y, pMax.x, pMax.y, col, rounding, flags, thickness);
    }

    public void addRect(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, float rounding, int flags, float thickness) {
        this.nAddRect(pMinX, pMinY, pMaxX, pMaxY, col, rounding, flags, thickness);
    }

    public void addRect(ImVec2 pMin, ImVec2 pMax, int col, int flags, float thickness) {
        this.nAddRect(pMin.x, pMin.y, pMax.x, pMax.y, col, flags, thickness);
    }

    public void addRect(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, int flags, float thickness) {
        this.nAddRect(pMinX, pMinY, pMaxX, pMaxY, col, flags, thickness);
    }

    public void addRect(ImVec2 pMin, ImVec2 pMax, int col, float rounding, float thickness) {
        this.nAddRect(pMin.x, pMin.y, pMax.x, pMax.y, col, rounding, thickness);
    }

    public void addRect(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, float rounding, float thickness) {
        this.nAddRect(pMinX, pMinY, pMaxX, pMaxY, col, rounding, thickness);
    }

    private native void nAddRect(float var1, float var2, float var3, float var4, int var5);

    private native void nAddRect(float var1, float var2, float var3, float var4, int var5, float var6);

    private native void nAddRect(float var1, float var2, float var3, float var4, int var5, float var6, int var7);

    private native void nAddRect(float var1, float var2, float var3, float var4, int var5, float var6, int var7, float var8);

    private native void nAddRect(float var1, float var2, float var3, float var4, int var5, int var6, float var7);

    private native void nAddRect(float var1, float var2, float var3, float var4, int var5, float var6, float var7);

    public void addRectFilled(ImVec2 pMin, ImVec2 pMax, int col) {
        this.nAddRectFilled(pMin.x, pMin.y, pMax.x, pMax.y, col);
    }

    public void addRectFilled(float pMinX, float pMinY, float pMaxX, float pMaxY, int col) {
        this.nAddRectFilled(pMinX, pMinY, pMaxX, pMaxY, col);
    }

    public void addRectFilled(ImVec2 pMin, ImVec2 pMax, int col, float rounding) {
        this.nAddRectFilled(pMin.x, pMin.y, pMax.x, pMax.y, col, rounding);
    }

    public void addRectFilled(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, float rounding) {
        this.nAddRectFilled(pMinX, pMinY, pMaxX, pMaxY, col, rounding);
    }

    public void addRectFilled(ImVec2 pMin, ImVec2 pMax, int col, float rounding, int flags) {
        this.nAddRectFilled(pMin.x, pMin.y, pMax.x, pMax.y, col, rounding, flags);
    }

    public void addRectFilled(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, float rounding, int flags) {
        this.nAddRectFilled(pMinX, pMinY, pMaxX, pMaxY, col, rounding, flags);
    }

    public void addRectFilled(ImVec2 pMin, ImVec2 pMax, int col, int flags) {
        this.nAddRectFilled(pMin.x, pMin.y, pMax.x, pMax.y, col, flags);
    }

    public void addRectFilled(float pMinX, float pMinY, float pMaxX, float pMaxY, int col, int flags) {
        this.nAddRectFilled(pMinX, pMinY, pMaxX, pMaxY, col, flags);
    }

    private native void nAddRectFilled(float var1, float var2, float var3, float var4, int var5);

    private native void nAddRectFilled(float var1, float var2, float var3, float var4, int var5, float var6);

    private native void nAddRectFilled(float var1, float var2, float var3, float var4, int var5, float var6, int var7);

    private native void nAddRectFilled(float var1, float var2, float var3, float var4, int var5, int var6);

    public void addRectFilledMultiColor(ImVec2 pMin, ImVec2 pMax, int colUprLeft, int colUprRight, int colBotRight, int colBotLeft) {
        this.nAddRectFilledMultiColor(pMin.x, pMin.y, pMax.x, pMax.y, colUprLeft, colUprRight, colBotRight, colBotLeft);
    }

    public void addRectFilledMultiColor(float pMinX, float pMinY, float pMaxX, float pMaxY, int colUprLeft, int colUprRight, int colBotRight, int colBotLeft) {
        this.nAddRectFilledMultiColor(pMinX, pMinY, pMaxX, pMaxY, colUprLeft, colUprRight, colBotRight, colBotLeft);
    }

    private native void nAddRectFilledMultiColor(float var1, float var2, float var3, float var4, int var5, int var6, int var7, int var8);

    public void addQuad(ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, int col) {
        this.nAddQuad(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, col);
    }

    public void addQuad(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, int col) {
        this.nAddQuad(p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, col);
    }

    public void addQuad(ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, int col, float thickness) {
        this.nAddQuad(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, col, thickness);
    }

    public void addQuad(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, int col, float thickness) {
        this.nAddQuad(p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, col, thickness);
    }

    private native void nAddQuad(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9);

    private native void nAddQuad(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10);

    public void addQuadFilled(ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, int col) {
        this.nAddQuadFilled(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, col);
    }

    public void addQuadFilled(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, int col) {
        this.nAddQuadFilled(p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, col);
    }

    private native void nAddQuadFilled(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9);

    public void addTriangle(ImVec2 p1, ImVec2 p2, ImVec2 p3, int col) {
        this.nAddTriangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, col);
    }

    public void addTriangle(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, int col) {
        this.nAddTriangle(p1X, p1Y, p2X, p2Y, p3X, p3Y, col);
    }

    public void addTriangle(ImVec2 p1, ImVec2 p2, ImVec2 p3, int col, float thickness) {
        this.nAddTriangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, col, thickness);
    }

    public void addTriangle(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, int col, float thickness) {
        this.nAddTriangle(p1X, p1Y, p2X, p2Y, p3X, p3Y, col, thickness);
    }

    private native void nAddTriangle(float var1, float var2, float var3, float var4, float var5, float var6, int var7);

    private native void nAddTriangle(float var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8);

    public void addTriangleFilled(ImVec2 p1, ImVec2 p2, ImVec2 p3, int col) {
        this.nAddTriangleFilled(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, col);
    }

    public void addTriangleFilled(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, int col) {
        this.nAddTriangleFilled(p1X, p1Y, p2X, p2Y, p3X, p3Y, col);
    }

    private native void nAddTriangleFilled(float var1, float var2, float var3, float var4, float var5, float var6, int var7);

    public void addCircle(ImVec2 center, float radius, int col) {
        this.nAddCircle(center.x, center.y, radius, col);
    }

    public void addCircle(float centerX, float centerY, float radius, int col) {
        this.nAddCircle(centerX, centerY, radius, col);
    }

    public void addCircle(ImVec2 center, float radius, int col, int numSegments) {
        this.nAddCircle(center.x, center.y, radius, col, numSegments);
    }

    public void addCircle(float centerX, float centerY, float radius, int col, int numSegments) {
        this.nAddCircle(centerX, centerY, radius, col, numSegments);
    }

    public void addCircle(ImVec2 center, float radius, int col, int numSegments, float thickness) {
        this.nAddCircle(center.x, center.y, radius, col, numSegments, thickness);
    }

    public void addCircle(float centerX, float centerY, float radius, int col, int numSegments, float thickness) {
        this.nAddCircle(centerX, centerY, radius, col, numSegments, thickness);
    }

    public void addCircle(ImVec2 center, float radius, int col, float thickness) {
        this.nAddCircle(center.x, center.y, radius, col, thickness);
    }

    public void addCircle(float centerX, float centerY, float radius, int col, float thickness) {
        this.nAddCircle(centerX, centerY, radius, col, thickness);
    }

    private native void nAddCircle(float var1, float var2, float var3, int var4);

    private native void nAddCircle(float var1, float var2, float var3, int var4, int var5);

    private native void nAddCircle(float var1, float var2, float var3, int var4, int var5, float var6);

    private native void nAddCircle(float var1, float var2, float var3, int var4, float var5);

    public void addCircleFilled(ImVec2 center, float radius, int col) {
        this.nAddCircleFilled(center.x, center.y, radius, col);
    }

    public void addCircleFilled(float centerX, float centerY, float radius, int col) {
        this.nAddCircleFilled(centerX, centerY, radius, col);
    }

    public void addCircleFilled(ImVec2 center, float radius, int col, int numSegments) {
        this.nAddCircleFilled(center.x, center.y, radius, col, numSegments);
    }

    public void addCircleFilled(float centerX, float centerY, float radius, int col, int numSegments) {
        this.nAddCircleFilled(centerX, centerY, radius, col, numSegments);
    }

    private native void nAddCircleFilled(float var1, float var2, float var3, int var4);

    private native void nAddCircleFilled(float var1, float var2, float var3, int var4, int var5);

    public void addNgon(ImVec2 center, float radius, int col, int num_segments) {
        this.nAddNgon(center.x, center.y, radius, col, num_segments);
    }

    public void addNgon(float centerX, float centerY, float radius, int col, int num_segments) {
        this.nAddNgon(centerX, centerY, radius, col, num_segments);
    }

    public void addNgon(ImVec2 center, float radius, int col, int num_segments, float thickness) {
        this.nAddNgon(center.x, center.y, radius, col, num_segments, thickness);
    }

    public void addNgon(float centerX, float centerY, float radius, int col, int num_segments, float thickness) {
        this.nAddNgon(centerX, centerY, radius, col, num_segments, thickness);
    }

    private native void nAddNgon(float var1, float var2, float var3, int var4, int var5);

    private native void nAddNgon(float var1, float var2, float var3, int var4, int var5, float var6);

    public void addNgonFilled(ImVec2 center, float radius, int col, int num_segments) {
        this.nAddNgonFilled(center.x, center.y, radius, col, num_segments);
    }

    public void addNgonFilled(float centerX, float centerY, float radius, int col, int num_segments) {
        this.nAddNgonFilled(centerX, centerY, radius, col, num_segments);
    }

    private native void nAddNgonFilled(float var1, float var2, float var3, int var4, int var5);

    public void addText(ImVec2 pos, int col, String textBegin) {
        this.nAddText(pos.x, pos.y, col, textBegin);
    }

    public void addText(float posX, float posY, int col, String textBegin) {
        this.nAddText(posX, posY, col, textBegin);
    }

    public void addText(ImVec2 pos, int col, String textBegin, String textEnd) {
        this.nAddText(pos.x, pos.y, col, textBegin, textEnd);
    }

    public void addText(float posX, float posY, int col, String textBegin, String textEnd) {
        this.nAddText(posX, posY, col, textBegin, textEnd);
    }

    private native void nAddText(float var1, float var2, int var3, String var4);

    private native void nAddText(float var1, float var2, int var3, String var4, String var5);

    public void addText(ImFont font, int fontSize, ImVec2 pos, int col, String textBegin) {
        this.nAddText(font.ptr, fontSize, pos.x, pos.y, col, textBegin);
    }

    public void addText(ImFont font, int fontSize, float posX, float posY, int col, String textBegin) {
        this.nAddText(font.ptr, fontSize, posX, posY, col, textBegin);
    }

    public void addText(ImFont font, int fontSize, ImVec2 pos, int col, String textBegin, String textEnd) {
        this.nAddText(font.ptr, fontSize, pos.x, pos.y, col, textBegin, textEnd);
    }

    public void addText(ImFont font, int fontSize, float posX, float posY, int col, String textBegin, String textEnd) {
        this.nAddText(font.ptr, fontSize, posX, posY, col, textBegin, textEnd);
    }

    public void addText(ImFont font, int fontSize, ImVec2 pos, int col, String textBegin, String textEnd, float wrapWidth) {
        this.nAddText(font.ptr, fontSize, pos.x, pos.y, col, textBegin, textEnd, wrapWidth);
    }

    public void addText(ImFont font, int fontSize, float posX, float posY, int col, String textBegin, String textEnd, float wrapWidth) {
        this.nAddText(font.ptr, fontSize, posX, posY, col, textBegin, textEnd, wrapWidth);
    }

    public void addText(ImFont font, int fontSize, ImVec2 pos, int col, String textBegin, String textEnd, float wrapWidth, ImVec4 cpuFineClipRect) {
        this.nAddText(font.ptr, fontSize, pos.x, pos.y, col, textBegin, textEnd, wrapWidth, cpuFineClipRect.x, cpuFineClipRect.y, cpuFineClipRect.z, cpuFineClipRect.w);
    }

    public void addText(ImFont font, int fontSize, float posX, float posY, int col, String textBegin, String textEnd, float wrapWidth, float cpuFineClipRectX, float cpuFineClipRectY, float cpuFineClipRectZ, float cpuFineClipRectW) {
        this.nAddText(font.ptr, fontSize, posX, posY, col, textBegin, textEnd, wrapWidth, cpuFineClipRectX, cpuFineClipRectY, cpuFineClipRectZ, cpuFineClipRectW);
    }

    public void addText(ImFont font, int fontSize, ImVec2 pos, int col, String textBegin, float wrapWidth, ImVec4 cpuFineClipRect) {
        this.nAddText(font.ptr, fontSize, pos.x, pos.y, col, textBegin, wrapWidth, cpuFineClipRect.x, cpuFineClipRect.y, cpuFineClipRect.z, cpuFineClipRect.w);
    }

    public void addText(ImFont font, int fontSize, float posX, float posY, int col, String textBegin, float wrapWidth, float cpuFineClipRectX, float cpuFineClipRectY, float cpuFineClipRectZ, float cpuFineClipRectW) {
        this.nAddText(font.ptr, fontSize, posX, posY, col, textBegin, wrapWidth, cpuFineClipRectX, cpuFineClipRectY, cpuFineClipRectZ, cpuFineClipRectW);
    }

    public void addText(ImFont font, int fontSize, ImVec2 pos, int col, String textBegin, ImVec4 cpuFineClipRect) {
        this.nAddText(font.ptr, fontSize, pos.x, pos.y, col, textBegin, cpuFineClipRect.x, cpuFineClipRect.y, cpuFineClipRect.z, cpuFineClipRect.w);
    }

    public void addText(ImFont font, int fontSize, float posX, float posY, int col, String textBegin, float cpuFineClipRectX, float cpuFineClipRectY, float cpuFineClipRectZ, float cpuFineClipRectW) {
        this.nAddText(font.ptr, fontSize, posX, posY, col, textBegin, cpuFineClipRectX, cpuFineClipRectY, cpuFineClipRectZ, cpuFineClipRectW);
    }

    public void addText(ImFont font, int fontSize, ImVec2 pos, int col, String textBegin, String textEnd, ImVec4 cpuFineClipRect) {
        this.nAddText(font.ptr, fontSize, pos.x, pos.y, col, textBegin, textEnd, cpuFineClipRect.x, cpuFineClipRect.y, cpuFineClipRect.z, cpuFineClipRect.w);
    }

    public void addText(ImFont font, int fontSize, float posX, float posY, int col, String textBegin, String textEnd, float cpuFineClipRectX, float cpuFineClipRectY, float cpuFineClipRectZ, float cpuFineClipRectW) {
        this.nAddText(font.ptr, fontSize, posX, posY, col, textBegin, textEnd, cpuFineClipRectX, cpuFineClipRectY, cpuFineClipRectZ, cpuFineClipRectW);
    }

    private native void nAddText(long var1, int var3, float var4, float var5, int var6, String var7);

    private native void nAddText(long var1, int var3, float var4, float var5, int var6, String var7, String var8);

    private native void nAddText(long var1, int var3, float var4, float var5, int var6, String var7, String var8, float var9);

    private native void nAddText(long var1, int var3, float var4, float var5, int var6, String var7, String var8, float var9, float var10, float var11, float var12, float var13);

    private native void nAddText(long var1, int var3, float var4, float var5, int var6, String var7, float var8, float var9, float var10, float var11, float var12);

    private native void nAddText(long var1, int var3, float var4, float var5, int var6, String var7, float var8, float var9, float var10, float var11);

    private native void nAddText(long var1, int var3, float var4, float var5, int var6, String var7, String var8, float var9, float var10, float var11, float var12);

    public void addPolyline(ImVec2[] points, int numPoint, int col, int imDrawFlags, float thickness) {
        this.nAddPolyline(points, numPoint, col, imDrawFlags, thickness);
    }

    private native void nAddPolyline(ImVec2[] var1, int var2, int var3, int var4, float var5);

    public void addConvexPolyFilled(ImVec2[] points, int numPoints, int col) {
        this.nAddConvexPolyFilled(points, numPoints, col);
    }

    private native void nAddConvexPolyFilled(ImVec2[] var1, int var2, int var3);

    public void addBezierCubic(ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, int col, float thickness) {
        this.nAddBezierCubic(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, col, thickness);
    }

    public void addBezierCubic(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, int col, float thickness) {
        this.nAddBezierCubic(p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, col, thickness);
    }

    public void addBezierCubic(ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, int col, float thickness, int numSegments) {
        this.nAddBezierCubic(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, col, thickness, numSegments);
    }

    public void addBezierCubic(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, int col, float thickness, int numSegments) {
        this.nAddBezierCubic(p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, col, thickness, numSegments);
    }

    private native void nAddBezierCubic(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10);

    private native void nAddBezierCubic(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10, int var11);

    public void addBezierQuadratic(ImVec2 p1, ImVec2 p2, ImVec2 p3, int col, float thickness) {
        this.nAddBezierQuadratic(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, col, thickness);
    }

    public void addBezierQuadratic(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, int col, float thickness) {
        this.nAddBezierQuadratic(p1X, p1Y, p2X, p2Y, p3X, p3Y, col, thickness);
    }

    public void addBezierQuadratic(ImVec2 p1, ImVec2 p2, ImVec2 p3, int col, float thickness, int numSegments) {
        this.nAddBezierQuadratic(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, col, thickness, numSegments);
    }

    public void addBezierQuadratic(float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, int col, float thickness, int numSegments) {
        this.nAddBezierQuadratic(p1X, p1Y, p2X, p2Y, p3X, p3Y, col, thickness, numSegments);
    }

    private native void nAddBezierQuadratic(float var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8);

    private native void nAddBezierQuadratic(float var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8, int var9);

    public void addImage(long textureID, ImVec2 pMin, ImVec2 pMax) {
        this.nAddImage(textureID, pMin.x, pMin.y, pMax.x, pMax.y);
    }

    public void addImage(long textureID, float pMinX, float pMinY, float pMaxX, float pMaxY) {
        this.nAddImage(textureID, pMinX, pMinY, pMaxX, pMaxY);
    }

    public void addImage(long textureID, ImVec2 pMin, ImVec2 pMax, ImVec2 uvMin) {
        this.nAddImage(textureID, pMin.x, pMin.y, pMax.x, pMax.y, uvMin.x, uvMin.y);
    }

    public void addImage(long textureID, float pMinX, float pMinY, float pMaxX, float pMaxY, float uvMinX, float uvMinY) {
        this.nAddImage(textureID, pMinX, pMinY, pMaxX, pMaxY, uvMinX, uvMinY);
    }

    public void addImage(long textureID, ImVec2 pMin, ImVec2 pMax, ImVec2 uvMin, ImVec2 uvMax) {
        this.nAddImage(textureID, pMin.x, pMin.y, pMax.x, pMax.y, uvMin.x, uvMin.y, uvMax.x, uvMax.y);
    }

    public void addImage(long textureID, float pMinX, float pMinY, float pMaxX, float pMaxY, float uvMinX, float uvMinY, float uvMaxX, float uvMaxY) {
        this.nAddImage(textureID, pMinX, pMinY, pMaxX, pMaxY, uvMinX, uvMinY, uvMaxX, uvMaxY);
    }

    public void addImage(long textureID, ImVec2 pMin, ImVec2 pMax, ImVec2 uvMin, ImVec2 uvMax, int col) {
        this.nAddImage(textureID, pMin.x, pMin.y, pMax.x, pMax.y, uvMin.x, uvMin.y, uvMax.x, uvMax.y, col);
    }

    public void addImage(long textureID, float pMinX, float pMinY, float pMaxX, float pMaxY, float uvMinX, float uvMinY, float uvMaxX, float uvMaxY, int col) {
        this.nAddImage(textureID, pMinX, pMinY, pMaxX, pMaxY, uvMinX, uvMinY, uvMaxX, uvMaxY, col);
    }

    private native void nAddImage(long var1, float var3, float var4, float var5, float var6);

    private native void nAddImage(long var1, float var3, float var4, float var5, float var6, float var7, float var8);

    private native void nAddImage(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10);

    private native void nAddImage(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, int var11);

    public void addImageQuad(long textureID, ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4) {
        this.nAddImageQuad(textureID, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }

    public void addImageQuad(long textureID, float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y) {
        this.nAddImageQuad(textureID, p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y);
    }

    public void addImageQuad(long textureID, ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, ImVec2 uv1) {
        this.nAddImageQuad(textureID, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, uv1.x, uv1.y);
    }

    public void addImageQuad(long textureID, float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, float uv1X, float uv1Y) {
        this.nAddImageQuad(textureID, p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, uv1X, uv1Y);
    }

    public void addImageQuad(long textureID, ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, ImVec2 uv1, ImVec2 uv2) {
        this.nAddImageQuad(textureID, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, uv1.x, uv1.y, uv2.x, uv2.y);
    }

    public void addImageQuad(long textureID, float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, float uv1X, float uv1Y, float uv2X, float uv2Y) {
        this.nAddImageQuad(textureID, p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, uv1X, uv1Y, uv2X, uv2Y);
    }

    public void addImageQuad(long textureID, ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, ImVec2 uv1, ImVec2 uv2, ImVec2 uv3) {
        this.nAddImageQuad(textureID, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, uv1.x, uv1.y, uv2.x, uv2.y, uv3.x, uv3.y);
    }

    public void addImageQuad(long textureID, float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, float uv1X, float uv1Y, float uv2X, float uv2Y, float uv3X, float uv3Y) {
        this.nAddImageQuad(textureID, p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, uv1X, uv1Y, uv2X, uv2Y, uv3X, uv3Y);
    }

    public void addImageQuad(long textureID, ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, ImVec2 uv1, ImVec2 uv2, ImVec2 uv3, ImVec2 uv4) {
        this.nAddImageQuad(textureID, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, uv1.x, uv1.y, uv2.x, uv2.y, uv3.x, uv3.y, uv4.x, uv4.y);
    }

    public void addImageQuad(long textureID, float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, float uv1X, float uv1Y, float uv2X, float uv2Y, float uv3X, float uv3Y, float uv4X, float uv4Y) {
        this.nAddImageQuad(textureID, p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, uv1X, uv1Y, uv2X, uv2Y, uv3X, uv3Y, uv4X, uv4Y);
    }

    public void addImageQuad(long textureID, ImVec2 p1, ImVec2 p2, ImVec2 p3, ImVec2 p4, ImVec2 uv1, ImVec2 uv2, ImVec2 uv3, ImVec2 uv4, int col) {
        this.nAddImageQuad(textureID, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, uv1.x, uv1.y, uv2.x, uv2.y, uv3.x, uv3.y, uv4.x, uv4.y, col);
    }

    public void addImageQuad(long textureID, float p1X, float p1Y, float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, float uv1X, float uv1Y, float uv2X, float uv2Y, float uv3X, float uv3Y, float uv4X, float uv4Y, int col) {
        this.nAddImageQuad(textureID, p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y, uv1X, uv1Y, uv2X, uv2Y, uv3X, uv3Y, uv4X, uv4Y, col);
    }

    private native void nAddImageQuad(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10);

    private native void nAddImageQuad(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12);

    private native void nAddImageQuad(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14);

    private native void nAddImageQuad(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16);

    private native void nAddImageQuad(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16, float var17, float var18);

    private native void nAddImageQuad(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16, float var17, float var18, int var19);

    public void addImageRounded(long textureID, ImVec2 pMin, ImVec2 pMax, ImVec2 uvMin, ImVec2 uvMax, int col, float rounding) {
        this.nAddImageRounded(textureID, pMin.x, pMin.y, pMax.x, pMax.y, uvMin.x, uvMin.y, uvMax.x, uvMax.y, col, rounding);
    }

    public void addImageRounded(long textureID, float pMinX, float pMinY, float pMaxX, float pMaxY, float uvMinX, float uvMinY, float uvMaxX, float uvMaxY, int col, float rounding) {
        this.nAddImageRounded(textureID, pMinX, pMinY, pMaxX, pMaxY, uvMinX, uvMinY, uvMaxX, uvMaxY, col, rounding);
    }

    public void addImageRounded(long textureID, ImVec2 pMin, ImVec2 pMax, ImVec2 uvMin, ImVec2 uvMax, int col, float rounding, int imDrawFlags) {
        this.nAddImageRounded(textureID, pMin.x, pMin.y, pMax.x, pMax.y, uvMin.x, uvMin.y, uvMax.x, uvMax.y, col, rounding, imDrawFlags);
    }

    public void addImageRounded(long textureID, float pMinX, float pMinY, float pMaxX, float pMaxY, float uvMinX, float uvMinY, float uvMaxX, float uvMaxY, int col, float rounding, int imDrawFlags) {
        this.nAddImageRounded(textureID, pMinX, pMinY, pMaxX, pMaxY, uvMinX, uvMinY, uvMaxX, uvMaxY, col, rounding, imDrawFlags);
    }

    private native void nAddImageRounded(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, int var11, float var12);

    private native void nAddImageRounded(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, int var11, float var12, int var13);

    public void pathClear() {
        this.nPathClear();
    }

    private native void nPathClear();

    public void pathLineTo(ImVec2 pos) {
        this.nPathLineTo(pos.x, pos.y);
    }

    public void pathLineTo(float posX, float posY) {
        this.nPathLineTo(posX, posY);
    }

    private native void nPathLineTo(float var1, float var2);

    public void pathLineToMergeDuplicate(ImVec2 pos) {
        this.nPathLineToMergeDuplicate(pos.x, pos.y);
    }

    public void pathLineToMergeDuplicate(float posX, float posY) {
        this.nPathLineToMergeDuplicate(posX, posY);
    }

    private native void nPathLineToMergeDuplicate(float var1, float var2);

    public void pathFillConvex(int col) {
        this.nPathFillConvex(col);
    }

    private native void nPathFillConvex(int var1);

    public void pathStroke(int col) {
        this.nPathStroke(col);
    }

    public void pathStroke(int col, int imDrawFlags) {
        this.nPathStroke(col, imDrawFlags);
    }

    public void pathStroke(int col, int imDrawFlags, float thickness) {
        this.nPathStroke(col, imDrawFlags, thickness);
    }

    public void pathStroke(int col, float thickness) {
        this.nPathStroke(col, thickness);
    }

    private native void nPathStroke(int var1);

    private native void nPathStroke(int var1, int var2);

    private native void nPathStroke(int var1, int var2, float var3);

    private native void nPathStroke(int var1, float var2);

    public void pathArcTo(ImVec2 center, float radius, float aMin, float aMax) {
        this.nPathArcTo(center.x, center.y, radius, aMin, aMax);
    }

    public void pathArcTo(float centerX, float centerY, float radius, float aMin, float aMax) {
        this.nPathArcTo(centerX, centerY, radius, aMin, aMax);
    }

    public void pathArcTo(ImVec2 center, float radius, float aMin, float aMax, int numSegments) {
        this.nPathArcTo(center.x, center.y, radius, aMin, aMax, numSegments);
    }

    public void pathArcTo(float centerX, float centerY, float radius, float aMin, float aMax, int numSegments) {
        this.nPathArcTo(centerX, centerY, radius, aMin, aMax, numSegments);
    }

    private native void nPathArcTo(float var1, float var2, float var3, float var4, float var5);

    private native void nPathArcTo(float var1, float var2, float var3, float var4, float var5, int var6);

    public void pathArcToFast(ImVec2 center, float radius, int aMinOf12, int aMaxOf12) {
        this.nPathArcToFast(center.x, center.y, radius, aMinOf12, aMaxOf12);
    }

    public void pathArcToFast(float centerX, float centerY, float radius, int aMinOf12, int aMaxOf12) {
        this.nPathArcToFast(centerX, centerY, radius, aMinOf12, aMaxOf12);
    }

    private native void nPathArcToFast(float var1, float var2, float var3, int var4, int var5);

    public void pathBezierCubicCurveTo(ImVec2 p2, ImVec2 p3, ImVec2 p4) {
        this.nPathBezierCubicCurveTo(p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }

    public void pathBezierCubicCurveTo(float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y) {
        this.nPathBezierCubicCurveTo(p2X, p2Y, p3X, p3Y, p4X, p4Y);
    }

    public void pathBezierCubicCurveTo(ImVec2 p2, ImVec2 p3, ImVec2 p4, int numSegments) {
        this.nPathBezierCubicCurveTo(p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, numSegments);
    }

    public void pathBezierCubicCurveTo(float p2X, float p2Y, float p3X, float p3Y, float p4X, float p4Y, int numSegments) {
        this.nPathBezierCubicCurveTo(p2X, p2Y, p3X, p3Y, p4X, p4Y, numSegments);
    }

    private native void nPathBezierCubicCurveTo(float var1, float var2, float var3, float var4, float var5, float var6);

    private native void nPathBezierCubicCurveTo(float var1, float var2, float var3, float var4, float var5, float var6, int var7);

    public void pathBezierQuadraticCurveTo(ImVec2 p2, ImVec2 p3) {
        this.nPathBezierQuadraticCurveTo(p2.x, p2.y, p3.x, p3.y);
    }

    public void pathBezierQuadraticCurveTo(float p2X, float p2Y, float p3X, float p3Y) {
        this.nPathBezierQuadraticCurveTo(p2X, p2Y, p3X, p3Y);
    }

    public void pathBezierQuadraticCurveTo(ImVec2 p2, ImVec2 p3, int numSegments) {
        this.nPathBezierQuadraticCurveTo(p2.x, p2.y, p3.x, p3.y, numSegments);
    }

    public void pathBezierQuadraticCurveTo(float p2X, float p2Y, float p3X, float p3Y, int numSegments) {
        this.nPathBezierQuadraticCurveTo(p2X, p2Y, p3X, p3Y, numSegments);
    }

    private native void nPathBezierQuadraticCurveTo(float var1, float var2, float var3, float var4);

    private native void nPathBezierQuadraticCurveTo(float var1, float var2, float var3, float var4, int var5);

    public void pathRect(ImVec2 rectMin, ImVec2 rectMax) {
        this.nPathRect(rectMin.x, rectMin.y, rectMax.x, rectMax.y);
    }

    public void pathRect(float rectMinX, float rectMinY, float rectMaxX, float rectMaxY) {
        this.nPathRect(rectMinX, rectMinY, rectMaxX, rectMaxY);
    }

    public void pathRect(ImVec2 rectMin, ImVec2 rectMax, float rounding) {
        this.nPathRect(rectMin.x, rectMin.y, rectMax.x, rectMax.y, rounding);
    }

    public void pathRect(float rectMinX, float rectMinY, float rectMaxX, float rectMaxY, float rounding) {
        this.nPathRect(rectMinX, rectMinY, rectMaxX, rectMaxY, rounding);
    }

    public void pathRect(ImVec2 rectMin, ImVec2 rectMax, float rounding, int imDrawFlags) {
        this.nPathRect(rectMin.x, rectMin.y, rectMax.x, rectMax.y, rounding, imDrawFlags);
    }

    public void pathRect(float rectMinX, float rectMinY, float rectMaxX, float rectMaxY, float rounding, int imDrawFlags) {
        this.nPathRect(rectMinX, rectMinY, rectMaxX, rectMaxY, rounding, imDrawFlags);
    }

    public void pathRect(ImVec2 rectMin, ImVec2 rectMax, int imDrawFlags) {
        this.nPathRect(rectMin.x, rectMin.y, rectMax.x, rectMax.y, imDrawFlags);
    }

    public void pathRect(float rectMinX, float rectMinY, float rectMaxX, float rectMaxY, int imDrawFlags) {
        this.nPathRect(rectMinX, rectMinY, rectMaxX, rectMaxY, imDrawFlags);
    }

    private native void nPathRect(float var1, float var2, float var3, float var4);

    private native void nPathRect(float var1, float var2, float var3, float var4, float var5);

    private native void nPathRect(float var1, float var2, float var3, float var4, float var5, int var6);

    private native void nPathRect(float var1, float var2, float var3, float var4, int var5);

    public void channelsSplit(int count) {
        this.nChannelsSplit(count);
    }

    private native void nChannelsSplit(int var1);

    public void channelsMerge() {
        this.nChannelsMerge();
    }

    private native void nChannelsMerge();

    public void channelsSetCurrent(int n) {
        this.nChannelsSetCurrent(n);
    }

    private native void nChannelsSetCurrent(int var1);

    public void primReserve(int idxCount, int vtxCount) {
        this.nPrimReserve(idxCount, vtxCount);
    }

    private native void nPrimReserve(int var1, int var2);

    public void primUnreserve(int idxCount, int vtxCount) {
        this.nPrimUnreserve(idxCount, vtxCount);
    }

    private native void nPrimUnreserve(int var1, int var2);

    public void primRect(ImVec2 a, ImVec2 b, int col) {
        this.nPrimRect(a.x, a.y, b.x, b.y, col);
    }

    public void primRect(float aX, float aY, float bX, float bY, int col) {
        this.nPrimRect(aX, aY, bX, bY, col);
    }

    private native void nPrimRect(float var1, float var2, float var3, float var4, int var5);

    public void primRectUV(ImVec2 a, ImVec2 b, ImVec2 uvA, ImVec2 uvB, int col) {
        this.nPrimRectUV(a.x, a.y, b.x, b.y, uvA.x, uvA.y, uvB.x, uvB.y, col);
    }

    public void primRectUV(float aX, float aY, float bX, float bY, float uvAX, float uvAY, float uvBX, float uvBY, int col) {
        this.nPrimRectUV(aX, aY, bX, bY, uvAX, uvAY, uvBX, uvBY, col);
    }

    private native void nPrimRectUV(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9);

    public void primQuadUV(ImVec2 a, ImVec2 b, ImVec2 c, ImVec2 d, ImVec2 uvA, ImVec2 uvB, ImVec2 uvC, ImVec2 uvD, int col) {
        this.nPrimQuadUV(a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y, uvA.x, uvA.y, uvB.x, uvB.y, uvC.x, uvC.y, uvD.x, uvD.y, col);
    }

    public void primQuadUV(float aX, float aY, float bX, float bY, float cX, float cY, float dX, float dY, float uvAX, float uvAY, float uvBX, float uvBY, float uvCX, float uvCY, float uvDX, float uvDY, int col) {
        this.nPrimQuadUV(aX, aY, bX, bY, cX, cY, dX, dY, uvAX, uvAY, uvBX, uvBY, uvCX, uvCY, uvDX, uvDY, col);
    }

    private native void nPrimQuadUV(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16, int var17);

    public void primWriteVtx(ImVec2 pos, ImVec2 uv, int col) {
        this.nPrimWriteVtx(pos.x, pos.y, uv.x, uv.y, col);
    }

    public void primWriteVtx(float posX, float posY, float uvX, float uvY, int col) {
        this.nPrimWriteVtx(posX, posY, uvX, uvY, col);
    }

    private native void nPrimWriteVtx(float var1, float var2, float var3, float var4, int var5);

    public void primWriteIdx(int idx) {
        this.nPrimWriteIdx(idx);
    }

    private native void nPrimWriteIdx(int var1);

    public void primVtx(ImVec2 pos, ImVec2 uv, int col) {
        this.nPrimVtx(pos.x, pos.y, uv.x, uv.y, col);
    }

    public void primVtx(float posX, float posY, float uvX, float uvY, int col) {
        this.nPrimVtx(posX, posY, uvX, uvY, col);
    }

    private native void nPrimVtx(float var1, float var2, float var3, float var4, int var5);
}

