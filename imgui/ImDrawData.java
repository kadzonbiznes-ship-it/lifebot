/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImDrawList;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.binding.ImGuiStruct;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImDrawData
extends ImGuiStruct {
    private static final int RESIZE_FACTOR = 5000;
    private static ByteBuffer dataBuffer = ByteBuffer.allocateDirect(25000).order(ByteOrder.nativeOrder());

    public ImDrawData(long ptr) {
        super(ptr);
    }

    public native int getCmdListCmdBufferSize(int var1);

    public native int getCmdListCmdBufferElemCount(int var1, int var2);

    public ImVec4 getCmdListCmdBufferClipRect(int cmdListIdx, int cmdBufferIdx) {
        ImVec4 dst = new ImVec4();
        this.getCmdListCmdBufferClipRect(dst, cmdListIdx, cmdBufferIdx);
        return dst;
    }

    public native void getCmdListCmdBufferClipRect(ImVec4 var1, int var2, int var3);

    public native long getCmdListCmdBufferTextureId(int var1, int var2);

    public native int getCmdListCmdBufferVtxOffset(int var1, int var2);

    public native int getCmdListCmdBufferIdxOffset(int var1, int var2);

    public native int getCmdListIdxBufferSize(int var1);

    public ByteBuffer getCmdListIdxBufferData(int cmdListIdx) {
        int idxBufferCapacity = this.getCmdListIdxBufferSize(cmdListIdx) * ImDrawData.sizeOfImDrawIdx();
        if (dataBuffer.capacity() < idxBufferCapacity) {
            dataBuffer.clear();
            dataBuffer = ByteBuffer.allocateDirect(idxBufferCapacity + 5000).order(ByteOrder.nativeOrder());
        }
        this.nGetCmdListIdxBufferData(cmdListIdx, dataBuffer, idxBufferCapacity);
        dataBuffer.position(0);
        dataBuffer.limit(idxBufferCapacity);
        return dataBuffer;
    }

    private native void nGetCmdListIdxBufferData(int var1, ByteBuffer var2, int var3);

    public native int getCmdListVtxBufferSize(int var1);

    public ByteBuffer getCmdListVtxBufferData(int cmdListIdx) {
        int vtxBufferCapacity = this.getCmdListVtxBufferSize(cmdListIdx) * ImDrawData.sizeOfImDrawVert();
        if (dataBuffer.capacity() < vtxBufferCapacity) {
            dataBuffer.clear();
            dataBuffer = ByteBuffer.allocateDirect(vtxBufferCapacity + 5000).order(ByteOrder.nativeOrder());
        }
        this.nGetCmdListVtxBufferData(cmdListIdx, dataBuffer, vtxBufferCapacity);
        dataBuffer.position(0);
        dataBuffer.limit(vtxBufferCapacity);
        return dataBuffer;
    }

    private native void nGetCmdListVtxBufferData(int var1, ByteBuffer var2, int var3);

    public static native int sizeOfImDrawVert();

    public static native int sizeOfImDrawIdx();

    public boolean getValid() {
        return this.nGetValid();
    }

    private native boolean nGetValid();

    public int getCmdListsCount() {
        return this.nGetCmdListsCount();
    }

    private native int nGetCmdListsCount();

    public int getTotalIdxCount() {
        return this.nGetTotalIdxCount();
    }

    private native int nGetTotalIdxCount();

    public int getTotalVtxCount() {
        return this.nGetTotalVtxCount();
    }

    private native int nGetTotalVtxCount();

    public ImVec2 getDisplayPos() {
        ImVec2 dst = new ImVec2();
        this.nGetDisplayPos(dst);
        return dst;
    }

    public float getDisplayPosX() {
        return this.nGetDisplayPosX();
    }

    public float getDisplayPosY() {
        return this.nGetDisplayPosY();
    }

    public void getDisplayPos(ImVec2 dst) {
        this.nGetDisplayPos(dst);
    }

    private native void nGetDisplayPos(ImVec2 var1);

    private native float nGetDisplayPosX();

    private native float nGetDisplayPosY();

    public ImVec2 getDisplaySize() {
        ImVec2 dst = new ImVec2();
        this.nGetDisplaySize(dst);
        return dst;
    }

    public float getDisplaySizeX() {
        return this.nGetDisplaySizeX();
    }

    public float getDisplaySizeY() {
        return this.nGetDisplaySizeY();
    }

    public void getDisplaySize(ImVec2 dst) {
        this.nGetDisplaySize(dst);
    }

    private native void nGetDisplaySize(ImVec2 var1);

    private native float nGetDisplaySizeX();

    private native float nGetDisplaySizeY();

    public ImVec2 getFramebufferScale() {
        ImVec2 dst = new ImVec2();
        this.nGetFramebufferScale(dst);
        return dst;
    }

    public float getFramebufferScaleX() {
        return this.nGetFramebufferScaleX();
    }

    public float getFramebufferScaleY() {
        return this.nGetFramebufferScaleY();
    }

    public void getFramebufferScale(ImVec2 dst) {
        this.nGetFramebufferScale(dst);
    }

    private native void nGetFramebufferScale(ImVec2 var1);

    private native float nGetFramebufferScaleX();

    private native float nGetFramebufferScaleY();

    public ImGuiViewport getOwnerViewport() {
        return new ImGuiViewport(this.nGetOwnerViewport());
    }

    private native long nGetOwnerViewport();

    public void clear() {
        this.nClear();
    }

    private native void nClear();

    public void addDrawList(ImDrawList drawList) {
        this.nAddDrawList(drawList.ptr);
    }

    private native void nAddDrawList(long var1);

    public void deIndexAllBuffers() {
        this.nDeIndexAllBuffers();
    }

    private native void nDeIndexAllBuffers();

    public void scaleClipRects(ImVec2 fbScale) {
        this.nScaleClipRects(fbScale.x, fbScale.y);
    }

    public void scaleClipRects(float fbScaleX, float fbScaleY) {
        this.nScaleClipRects(fbScaleX, fbScaleY);
    }

    private native void nScaleClipRects(float var1, float var2);
}

