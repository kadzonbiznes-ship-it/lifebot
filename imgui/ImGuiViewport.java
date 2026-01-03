/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImDrawData;
import imgui.ImVec2;
import imgui.binding.ImGuiStruct;

public final class ImGuiViewport
extends ImGuiStruct {
    public ImGuiViewport(long ptr) {
        super(ptr);
    }

    public int getID() {
        return this.nGetID();
    }

    public void setID(int value) {
        this.nSetID(value);
    }

    private native int nGetID();

    private native void nSetID(int var1);

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

    public ImVec2 getPos() {
        ImVec2 dst = new ImVec2();
        this.nGetPos(dst);
        return dst;
    }

    public float getPosX() {
        return this.nGetPosX();
    }

    public float getPosY() {
        return this.nGetPosY();
    }

    public void getPos(ImVec2 dst) {
        this.nGetPos(dst);
    }

    public void setPos(ImVec2 value) {
        this.nSetPos(value.x, value.y);
    }

    public void setPos(float valueX, float valueY) {
        this.nSetPos(valueX, valueY);
    }

    private native void nGetPos(ImVec2 var1);

    private native float nGetPosX();

    private native float nGetPosY();

    private native void nSetPos(float var1, float var2);

    public ImVec2 getSize() {
        ImVec2 dst = new ImVec2();
        this.nGetSize(dst);
        return dst;
    }

    public float getSizeX() {
        return this.nGetSizeX();
    }

    public float getSizeY() {
        return this.nGetSizeY();
    }

    public void getSize(ImVec2 dst) {
        this.nGetSize(dst);
    }

    public void setSize(ImVec2 value) {
        this.nSetSize(value.x, value.y);
    }

    public void setSize(float valueX, float valueY) {
        this.nSetSize(valueX, valueY);
    }

    private native void nGetSize(ImVec2 var1);

    private native float nGetSizeX();

    private native float nGetSizeY();

    private native void nSetSize(float var1, float var2);

    public ImVec2 getWorkPos() {
        ImVec2 dst = new ImVec2();
        this.nGetWorkPos(dst);
        return dst;
    }

    public float getWorkPosX() {
        return this.nGetWorkPosX();
    }

    public float getWorkPosY() {
        return this.nGetWorkPosY();
    }

    public void getWorkPos(ImVec2 dst) {
        this.nGetWorkPos(dst);
    }

    public void setWorkPos(ImVec2 value) {
        this.nSetWorkPos(value.x, value.y);
    }

    public void setWorkPos(float valueX, float valueY) {
        this.nSetWorkPos(valueX, valueY);
    }

    private native void nGetWorkPos(ImVec2 var1);

    private native float nGetWorkPosX();

    private native float nGetWorkPosY();

    private native void nSetWorkPos(float var1, float var2);

    public ImVec2 getWorkSize() {
        ImVec2 dst = new ImVec2();
        this.nGetWorkSize(dst);
        return dst;
    }

    public float getWorkSizeX() {
        return this.nGetWorkSizeX();
    }

    public float getWorkSizeY() {
        return this.nGetWorkSizeY();
    }

    public void getWorkSize(ImVec2 dst) {
        this.nGetWorkSize(dst);
    }

    public void setWorkSize(ImVec2 value) {
        this.nSetWorkSize(value.x, value.y);
    }

    public void setWorkSize(float valueX, float valueY) {
        this.nSetWorkSize(valueX, valueY);
    }

    private native void nGetWorkSize(ImVec2 var1);

    private native float nGetWorkSizeX();

    private native float nGetWorkSizeY();

    private native void nSetWorkSize(float var1, float var2);

    public float getDpiScale() {
        return this.nGetDpiScale();
    }

    public void setDpiScale(float value) {
        this.nSetDpiScale(value);
    }

    private native float nGetDpiScale();

    private native void nSetDpiScale(float var1);

    public int getParentViewportId() {
        return this.nGetParentViewportId();
    }

    public void setParentViewportId(int value) {
        this.nSetParentViewportId(value);
    }

    private native int nGetParentViewportId();

    private native void nSetParentViewportId(int var1);

    public ImDrawData getDrawData() {
        return new ImDrawData(this.nGetDrawData());
    }

    public void setDrawData(ImDrawData value) {
        this.nSetDrawData(value.ptr);
    }

    private native long nGetDrawData();

    private native void nSetDrawData(long var1);

    public native void setRendererUserData(Object var1);

    public native Object getRendererUserData();

    public native void setPlatformUserData(Object var1);

    public native Object getPlatformUserData();

    public native void setPlatformHandle(long var1);

    public native long getPlatformHandle();

    public native void setPlatformHandleRaw(long var1);

    public native long getPlatformHandleRaw();

    public boolean getPlatformWindowCreated() {
        return this.nGetPlatformWindowCreated();
    }

    public void setPlatformWindowCreated(boolean value) {
        this.nSetPlatformWindowCreated(value);
    }

    private native boolean nGetPlatformWindowCreated();

    private native void nSetPlatformWindowCreated(boolean var1);

    public boolean getPlatformRequestMove() {
        return this.nGetPlatformRequestMove();
    }

    public void setPlatformRequestMove(boolean value) {
        this.nSetPlatformRequestMove(value);
    }

    private native boolean nGetPlatformRequestMove();

    private native void nSetPlatformRequestMove(boolean var1);

    public boolean getPlatformRequestResize() {
        return this.nGetPlatformRequestResize();
    }

    public void setPlatformRequestResize(boolean value) {
        this.nSetPlatformRequestResize(value);
    }

    private native boolean nGetPlatformRequestResize();

    private native void nSetPlatformRequestResize(boolean var1);

    public boolean getPlatformRequestClose() {
        return this.nGetPlatformRequestClose();
    }

    public void setPlatformRequestClose(boolean value) {
        this.nSetPlatformRequestClose(value);
    }

    private native boolean nGetPlatformRequestClose();

    private native void nSetPlatformRequestClose(boolean var1);

    public ImVec2 getCenter() {
        ImVec2 dst = new ImVec2();
        this.nGetCenter(dst);
        return dst;
    }

    public float getCenterX() {
        return this.nGetCenterX();
    }

    public float getCenterY() {
        return this.nGetCenterY();
    }

    public void getCenter(ImVec2 dst) {
        this.nGetCenter(dst);
    }

    private native void nGetCenter(ImVec2 var1);

    private native float nGetCenterX();

    private native float nGetCenterY();

    public ImVec2 getWorkCenter() {
        ImVec2 dst = new ImVec2();
        this.nGetWorkCenter(dst);
        return dst;
    }

    public float getWorkCenterX() {
        return this.nGetWorkCenterX();
    }

    public float getWorkCenterY() {
        return this.nGetWorkCenterY();
    }

    public void getWorkCenter(ImVec2 dst) {
        this.nGetWorkCenter(dst);
    }

    private native void nGetWorkCenter(ImVec2 var1);

    private native float nGetWorkCenterX();

    private native float nGetWorkCenterY();
}

