/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImVec2;
import imgui.binding.ImGuiStruct;

public final class ImGuiPlatformMonitor
extends ImGuiStruct {
    public ImGuiPlatformMonitor(long ptr) {
        super(ptr);
    }

    public ImVec2 getMainPos() {
        ImVec2 dst = new ImVec2();
        this.nGetMainPos(dst);
        return dst;
    }

    public float getMainPosX() {
        return this.nGetMainPosX();
    }

    public float getMainPosY() {
        return this.nGetMainPosY();
    }

    public void getMainPos(ImVec2 dst) {
        this.nGetMainPos(dst);
    }

    public void setMainPos(ImVec2 value) {
        this.nSetMainPos(value.x, value.y);
    }

    public void setMainPos(float valueX, float valueY) {
        this.nSetMainPos(valueX, valueY);
    }

    private native void nGetMainPos(ImVec2 var1);

    private native float nGetMainPosX();

    private native float nGetMainPosY();

    private native void nSetMainPos(float var1, float var2);

    public ImVec2 getMainSize() {
        ImVec2 dst = new ImVec2();
        this.nGetMainSize(dst);
        return dst;
    }

    public float getMainSizeX() {
        return this.nGetMainSizeX();
    }

    public float getMainSizeY() {
        return this.nGetMainSizeY();
    }

    public void getMainSize(ImVec2 dst) {
        this.nGetMainSize(dst);
    }

    public void setMainSize(ImVec2 value) {
        this.nSetMainSize(value.x, value.y);
    }

    public void setMainSize(float valueX, float valueY) {
        this.nSetMainSize(valueX, valueY);
    }

    private native void nGetMainSize(ImVec2 var1);

    private native float nGetMainSizeX();

    private native float nGetMainSizeY();

    private native void nSetMainSize(float var1, float var2);

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

    public native void setPlatformHandle(long var1);

    public native long getPlatformHandle();
}

