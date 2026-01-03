/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.binding.ImGuiStructDestroyable;

public final class ImFontGlyph
extends ImGuiStructDestroyable {
    public ImFontGlyph() {
    }

    public ImFontGlyph(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public int getColored() {
        return this.nGetColored();
    }

    public void setColored(int value) {
        this.nSetColored(value);
    }

    private native int nGetColored();

    private native void nSetColored(int var1);

    public int getVisible() {
        return this.nGetVisible();
    }

    public void setVisible(int value) {
        this.nSetVisible(value);
    }

    private native int nGetVisible();

    private native void nSetVisible(int var1);

    public int getCodepoint() {
        return this.nGetCodepoint();
    }

    public void setCodepoint(int value) {
        this.nSetCodepoint(value);
    }

    private native int nGetCodepoint();

    private native void nSetCodepoint(int var1);

    public float getAdvanceX() {
        return this.nGetAdvanceX();
    }

    public void setAdvanceX(float value) {
        this.nSetAdvanceX(value);
    }

    private native float nGetAdvanceX();

    private native void nSetAdvanceX(float var1);

    public float getX0() {
        return this.nGetX0();
    }

    public void setX0(float value) {
        this.nSetX0(value);
    }

    private native float nGetX0();

    private native void nSetX0(float var1);

    public float getY0() {
        return this.nGetY0();
    }

    public void setY0(float value) {
        this.nSetY0(value);
    }

    private native float nGetY0();

    private native void nSetY0(float var1);

    public float getX1() {
        return this.nGetX1();
    }

    public void setX1(float value) {
        this.nSetX1(value);
    }

    private native float nGetX1();

    private native void nSetX1(float var1);

    public float getY1() {
        return this.nGetY1();
    }

    public void setY1(float value) {
        this.nSetY1(value);
    }

    private native float nGetY1();

    private native void nSetY1(float var1);

    public float getU0() {
        return this.nGetU0();
    }

    public void setU0(float value) {
        this.nSetU0(value);
    }

    private native float nGetU0();

    private native void nSetU0(float var1);

    public float getV0() {
        return this.nGetV0();
    }

    public void setV0(float value) {
        this.nSetV0(value);
    }

    private native float nGetV0();

    private native void nSetV0(float var1);

    public float getU1() {
        return this.nGetU1();
    }

    public void setU1(float value) {
        this.nSetU1(value);
    }

    private native float nGetU1();

    private native void nSetU1(float var1);

    public float getV1() {
        return this.nGetV1();
    }

    public void setV1(float value) {
        this.nSetV1(value);
    }

    private native float nGetV1();

    private native void nSetV1(float var1);
}

