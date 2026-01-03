/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImVec2;
import imgui.ImVec4;
import imgui.binding.ImGuiStructDestroyable;

public final class ImGuiStyle
extends ImGuiStructDestroyable {
    public ImGuiStyle() {
    }

    public ImGuiStyle(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public float getAlpha() {
        return this.nGetAlpha();
    }

    public void setAlpha(float value) {
        this.nSetAlpha(value);
    }

    private native float nGetAlpha();

    private native void nSetAlpha(float var1);

    public float getDisabledAlpha() {
        return this.nGetDisabledAlpha();
    }

    public void setDisabledAlpha(float value) {
        this.nSetDisabledAlpha(value);
    }

    private native float nGetDisabledAlpha();

    private native void nSetDisabledAlpha(float var1);

    public ImVec2 getWindowPadding() {
        ImVec2 dst = new ImVec2();
        this.nGetWindowPadding(dst);
        return dst;
    }

    public float getWindowPaddingX() {
        return this.nGetWindowPaddingX();
    }

    public float getWindowPaddingY() {
        return this.nGetWindowPaddingY();
    }

    public void getWindowPadding(ImVec2 dst) {
        this.nGetWindowPadding(dst);
    }

    public void setWindowPadding(ImVec2 value) {
        this.nSetWindowPadding(value.x, value.y);
    }

    public void setWindowPadding(float valueX, float valueY) {
        this.nSetWindowPadding(valueX, valueY);
    }

    private native void nGetWindowPadding(ImVec2 var1);

    private native float nGetWindowPaddingX();

    private native float nGetWindowPaddingY();

    private native void nSetWindowPadding(float var1, float var2);

    public float getWindowRounding() {
        return this.nGetWindowRounding();
    }

    public void setWindowRounding(float value) {
        this.nSetWindowRounding(value);
    }

    private native float nGetWindowRounding();

    private native void nSetWindowRounding(float var1);

    public float getWindowBorderSize() {
        return this.nGetWindowBorderSize();
    }

    public void setWindowBorderSize(float value) {
        this.nSetWindowBorderSize(value);
    }

    private native float nGetWindowBorderSize();

    private native void nSetWindowBorderSize(float var1);

    public ImVec2 getWindowMinSize() {
        ImVec2 dst = new ImVec2();
        this.nGetWindowMinSize(dst);
        return dst;
    }

    public float getWindowMinSizeX() {
        return this.nGetWindowMinSizeX();
    }

    public float getWindowMinSizeY() {
        return this.nGetWindowMinSizeY();
    }

    public void getWindowMinSize(ImVec2 dst) {
        this.nGetWindowMinSize(dst);
    }

    public void setWindowMinSize(ImVec2 value) {
        this.nSetWindowMinSize(value.x, value.y);
    }

    public void setWindowMinSize(float valueX, float valueY) {
        this.nSetWindowMinSize(valueX, valueY);
    }

    private native void nGetWindowMinSize(ImVec2 var1);

    private native float nGetWindowMinSizeX();

    private native float nGetWindowMinSizeY();

    private native void nSetWindowMinSize(float var1, float var2);

    public ImVec2 getWindowTitleAlign() {
        ImVec2 dst = new ImVec2();
        this.nGetWindowTitleAlign(dst);
        return dst;
    }

    public float getWindowTitleAlignX() {
        return this.nGetWindowTitleAlignX();
    }

    public float getWindowTitleAlignY() {
        return this.nGetWindowTitleAlignY();
    }

    public void getWindowTitleAlign(ImVec2 dst) {
        this.nGetWindowTitleAlign(dst);
    }

    public void setWindowTitleAlign(ImVec2 value) {
        this.nSetWindowTitleAlign(value.x, value.y);
    }

    public void setWindowTitleAlign(float valueX, float valueY) {
        this.nSetWindowTitleAlign(valueX, valueY);
    }

    private native void nGetWindowTitleAlign(ImVec2 var1);

    private native float nGetWindowTitleAlignX();

    private native float nGetWindowTitleAlignY();

    private native void nSetWindowTitleAlign(float var1, float var2);

    public int getWindowMenuButtonPosition() {
        return this.nGetWindowMenuButtonPosition();
    }

    public void setWindowMenuButtonPosition(int value) {
        this.nSetWindowMenuButtonPosition(value);
    }

    private native int nGetWindowMenuButtonPosition();

    private native void nSetWindowMenuButtonPosition(int var1);

    public float getChildRounding() {
        return this.nGetChildRounding();
    }

    public void setChildRounding(float value) {
        this.nSetChildRounding(value);
    }

    private native float nGetChildRounding();

    private native void nSetChildRounding(float var1);

    public float getChildBorderSize() {
        return this.nGetChildBorderSize();
    }

    public void setChildBorderSize(float value) {
        this.nSetChildBorderSize(value);
    }

    private native float nGetChildBorderSize();

    private native void nSetChildBorderSize(float var1);

    public float getPopupRounding() {
        return this.nGetPopupRounding();
    }

    public void setPopupRounding(float value) {
        this.nSetPopupRounding(value);
    }

    private native float nGetPopupRounding();

    private native void nSetPopupRounding(float var1);

    public float getPopupBorderSize() {
        return this.nGetPopupBorderSize();
    }

    public void setPopupBorderSize(float value) {
        this.nSetPopupBorderSize(value);
    }

    private native float nGetPopupBorderSize();

    private native void nSetPopupBorderSize(float var1);

    public ImVec2 getFramePadding() {
        ImVec2 dst = new ImVec2();
        this.nGetFramePadding(dst);
        return dst;
    }

    public float getFramePaddingX() {
        return this.nGetFramePaddingX();
    }

    public float getFramePaddingY() {
        return this.nGetFramePaddingY();
    }

    public void getFramePadding(ImVec2 dst) {
        this.nGetFramePadding(dst);
    }

    public void setFramePadding(ImVec2 value) {
        this.nSetFramePadding(value.x, value.y);
    }

    public void setFramePadding(float valueX, float valueY) {
        this.nSetFramePadding(valueX, valueY);
    }

    private native void nGetFramePadding(ImVec2 var1);

    private native float nGetFramePaddingX();

    private native float nGetFramePaddingY();

    private native void nSetFramePadding(float var1, float var2);

    public float getFrameRounding() {
        return this.nGetFrameRounding();
    }

    public void setFrameRounding(float value) {
        this.nSetFrameRounding(value);
    }

    private native float nGetFrameRounding();

    private native void nSetFrameRounding(float var1);

    public float getFrameBorderSize() {
        return this.nGetFrameBorderSize();
    }

    public void setFrameBorderSize(float value) {
        this.nSetFrameBorderSize(value);
    }

    private native float nGetFrameBorderSize();

    private native void nSetFrameBorderSize(float var1);

    public ImVec2 getItemSpacing() {
        ImVec2 dst = new ImVec2();
        this.nGetItemSpacing(dst);
        return dst;
    }

    public float getItemSpacingX() {
        return this.nGetItemSpacingX();
    }

    public float getItemSpacingY() {
        return this.nGetItemSpacingY();
    }

    public void getItemSpacing(ImVec2 dst) {
        this.nGetItemSpacing(dst);
    }

    public void setItemSpacing(ImVec2 value) {
        this.nSetItemSpacing(value.x, value.y);
    }

    public void setItemSpacing(float valueX, float valueY) {
        this.nSetItemSpacing(valueX, valueY);
    }

    private native void nGetItemSpacing(ImVec2 var1);

    private native float nGetItemSpacingX();

    private native float nGetItemSpacingY();

    private native void nSetItemSpacing(float var1, float var2);

    public ImVec2 getItemInnerSpacing() {
        ImVec2 dst = new ImVec2();
        this.nGetItemInnerSpacing(dst);
        return dst;
    }

    public float getItemInnerSpacingX() {
        return this.nGetItemInnerSpacingX();
    }

    public float getItemInnerSpacingY() {
        return this.nGetItemInnerSpacingY();
    }

    public void getItemInnerSpacing(ImVec2 dst) {
        this.nGetItemInnerSpacing(dst);
    }

    public void setItemInnerSpacing(ImVec2 value) {
        this.nSetItemInnerSpacing(value.x, value.y);
    }

    public void setItemInnerSpacing(float valueX, float valueY) {
        this.nSetItemInnerSpacing(valueX, valueY);
    }

    private native void nGetItemInnerSpacing(ImVec2 var1);

    private native float nGetItemInnerSpacingX();

    private native float nGetItemInnerSpacingY();

    private native void nSetItemInnerSpacing(float var1, float var2);

    public ImVec2 getCellPadding() {
        ImVec2 dst = new ImVec2();
        this.nGetCellPadding(dst);
        return dst;
    }

    public float getCellPaddingX() {
        return this.nGetCellPaddingX();
    }

    public float getCellPaddingY() {
        return this.nGetCellPaddingY();
    }

    public void getCellPadding(ImVec2 dst) {
        this.nGetCellPadding(dst);
    }

    public void setCellPadding(ImVec2 value) {
        this.nSetCellPadding(value.x, value.y);
    }

    public void setCellPadding(float valueX, float valueY) {
        this.nSetCellPadding(valueX, valueY);
    }

    private native void nGetCellPadding(ImVec2 var1);

    private native float nGetCellPaddingX();

    private native float nGetCellPaddingY();

    private native void nSetCellPadding(float var1, float var2);

    public ImVec2 getTouchExtraPadding() {
        ImVec2 dst = new ImVec2();
        this.nGetTouchExtraPadding(dst);
        return dst;
    }

    public float getTouchExtraPaddingX() {
        return this.nGetTouchExtraPaddingX();
    }

    public float getTouchExtraPaddingY() {
        return this.nGetTouchExtraPaddingY();
    }

    public void getTouchExtraPadding(ImVec2 dst) {
        this.nGetTouchExtraPadding(dst);
    }

    public void setTouchExtraPadding(ImVec2 value) {
        this.nSetTouchExtraPadding(value.x, value.y);
    }

    public void setTouchExtraPadding(float valueX, float valueY) {
        this.nSetTouchExtraPadding(valueX, valueY);
    }

    private native void nGetTouchExtraPadding(ImVec2 var1);

    private native float nGetTouchExtraPaddingX();

    private native float nGetTouchExtraPaddingY();

    private native void nSetTouchExtraPadding(float var1, float var2);

    public float getIndentSpacing() {
        return this.nGetIndentSpacing();
    }

    public void setIndentSpacing(float value) {
        this.nSetIndentSpacing(value);
    }

    private native float nGetIndentSpacing();

    private native void nSetIndentSpacing(float var1);

    public float getColumnsMinSpacing() {
        return this.nGetColumnsMinSpacing();
    }

    public void setColumnsMinSpacing(float value) {
        this.nSetColumnsMinSpacing(value);
    }

    private native float nGetColumnsMinSpacing();

    private native void nSetColumnsMinSpacing(float var1);

    public float getScrollbarSize() {
        return this.nGetScrollbarSize();
    }

    public void setScrollbarSize(float value) {
        this.nSetScrollbarSize(value);
    }

    private native float nGetScrollbarSize();

    private native void nSetScrollbarSize(float var1);

    public float getScrollbarRounding() {
        return this.nGetScrollbarRounding();
    }

    public void setScrollbarRounding(float value) {
        this.nSetScrollbarRounding(value);
    }

    private native float nGetScrollbarRounding();

    private native void nSetScrollbarRounding(float var1);

    public float getGrabMinSize() {
        return this.nGetGrabMinSize();
    }

    public void setGrabMinSize(float value) {
        this.nSetGrabMinSize(value);
    }

    private native float nGetGrabMinSize();

    private native void nSetGrabMinSize(float var1);

    public float getGrabRounding() {
        return this.nGetGrabRounding();
    }

    public void setGrabRounding(float value) {
        this.nSetGrabRounding(value);
    }

    private native float nGetGrabRounding();

    private native void nSetGrabRounding(float var1);

    public float getLogSliderDeadzone() {
        return this.nGetLogSliderDeadzone();
    }

    public void setLogSliderDeadzone(float value) {
        this.nSetLogSliderDeadzone(value);
    }

    private native float nGetLogSliderDeadzone();

    private native void nSetLogSliderDeadzone(float var1);

    public float getTabRounding() {
        return this.nGetTabRounding();
    }

    public void setTabRounding(float value) {
        this.nSetTabRounding(value);
    }

    private native float nGetTabRounding();

    private native void nSetTabRounding(float var1);

    public float getTabBorderSize() {
        return this.nGetTabBorderSize();
    }

    public void setTabBorderSize(float value) {
        this.nSetTabBorderSize(value);
    }

    private native float nGetTabBorderSize();

    private native void nSetTabBorderSize(float var1);

    public float getTabMinWidthForCloseButton() {
        return this.nGetTabMinWidthForCloseButton();
    }

    public void setTabMinWidthForCloseButton(float value) {
        this.nSetTabMinWidthForCloseButton(value);
    }

    private native float nGetTabMinWidthForCloseButton();

    private native void nSetTabMinWidthForCloseButton(float var1);

    public int getColorButtonPosition() {
        return this.nGetColorButtonPosition();
    }

    public void setColorButtonPosition(int value) {
        this.nSetColorButtonPosition(value);
    }

    private native int nGetColorButtonPosition();

    private native void nSetColorButtonPosition(int var1);

    public ImVec2 getButtonTextAlign() {
        ImVec2 dst = new ImVec2();
        this.nGetButtonTextAlign(dst);
        return dst;
    }

    public float getButtonTextAlignX() {
        return this.nGetButtonTextAlignX();
    }

    public float getButtonTextAlignY() {
        return this.nGetButtonTextAlignY();
    }

    public void getButtonTextAlign(ImVec2 dst) {
        this.nGetButtonTextAlign(dst);
    }

    public void setButtonTextAlign(ImVec2 value) {
        this.nSetButtonTextAlign(value.x, value.y);
    }

    public void setButtonTextAlign(float valueX, float valueY) {
        this.nSetButtonTextAlign(valueX, valueY);
    }

    private native void nGetButtonTextAlign(ImVec2 var1);

    private native float nGetButtonTextAlignX();

    private native float nGetButtonTextAlignY();

    private native void nSetButtonTextAlign(float var1, float var2);

    public ImVec2 getSelectableTextAlign() {
        ImVec2 dst = new ImVec2();
        this.nGetSelectableTextAlign(dst);
        return dst;
    }

    public float getSelectableTextAlignX() {
        return this.nGetSelectableTextAlignX();
    }

    public float getSelectableTextAlignY() {
        return this.nGetSelectableTextAlignY();
    }

    public void getSelectableTextAlign(ImVec2 dst) {
        this.nGetSelectableTextAlign(dst);
    }

    public void setSelectableTextAlign(ImVec2 value) {
        this.nSetSelectableTextAlign(value.x, value.y);
    }

    public void setSelectableTextAlign(float valueX, float valueY) {
        this.nSetSelectableTextAlign(valueX, valueY);
    }

    private native void nGetSelectableTextAlign(ImVec2 var1);

    private native float nGetSelectableTextAlignX();

    private native float nGetSelectableTextAlignY();

    private native void nSetSelectableTextAlign(float var1, float var2);

    public float getSeparatorTextBorderSize() {
        return this.nGetSeparatorTextBorderSize();
    }

    public void setSeparatorTextBorderSize(float value) {
        this.nSetSeparatorTextBorderSize(value);
    }

    private native float nGetSeparatorTextBorderSize();

    private native void nSetSeparatorTextBorderSize(float var1);

    public ImVec2 getSeparatorTextAlign() {
        ImVec2 dst = new ImVec2();
        this.nGetSeparatorTextAlign(dst);
        return dst;
    }

    public float getSeparatorTextAlignX() {
        return this.nGetSeparatorTextAlignX();
    }

    public float getSeparatorTextAlignY() {
        return this.nGetSeparatorTextAlignY();
    }

    public void getSeparatorTextAlign(ImVec2 dst) {
        this.nGetSeparatorTextAlign(dst);
    }

    public void setSeparatorTextAlign(ImVec2 value) {
        this.nSetSeparatorTextAlign(value.x, value.y);
    }

    public void setSeparatorTextAlign(float valueX, float valueY) {
        this.nSetSeparatorTextAlign(valueX, valueY);
    }

    private native void nGetSeparatorTextAlign(ImVec2 var1);

    private native float nGetSeparatorTextAlignX();

    private native float nGetSeparatorTextAlignY();

    private native void nSetSeparatorTextAlign(float var1, float var2);

    public ImVec2 getSeparatorTextPadding() {
        ImVec2 dst = new ImVec2();
        this.nGetSeparatorTextPadding(dst);
        return dst;
    }

    public float getSeparatorTextPaddingX() {
        return this.nGetSeparatorTextPaddingX();
    }

    public float getSeparatorTextPaddingY() {
        return this.nGetSeparatorTextPaddingY();
    }

    public void getSeparatorTextPadding(ImVec2 dst) {
        this.nGetSeparatorTextPadding(dst);
    }

    public void setSeparatorTextPadding(ImVec2 value) {
        this.nSetSeparatorTextPadding(value.x, value.y);
    }

    public void setSeparatorTextPadding(float valueX, float valueY) {
        this.nSetSeparatorTextPadding(valueX, valueY);
    }

    private native void nGetSeparatorTextPadding(ImVec2 var1);

    private native float nGetSeparatorTextPaddingX();

    private native float nGetSeparatorTextPaddingY();

    private native void nSetSeparatorTextPadding(float var1, float var2);

    public ImVec2 getDisplayWindowPadding() {
        ImVec2 dst = new ImVec2();
        this.nGetDisplayWindowPadding(dst);
        return dst;
    }

    public float getDisplayWindowPaddingX() {
        return this.nGetDisplayWindowPaddingX();
    }

    public float getDisplayWindowPaddingY() {
        return this.nGetDisplayWindowPaddingY();
    }

    public void getDisplayWindowPadding(ImVec2 dst) {
        this.nGetDisplayWindowPadding(dst);
    }

    public void setDisplayWindowPadding(ImVec2 value) {
        this.nSetDisplayWindowPadding(value.x, value.y);
    }

    public void setDisplayWindowPadding(float valueX, float valueY) {
        this.nSetDisplayWindowPadding(valueX, valueY);
    }

    private native void nGetDisplayWindowPadding(ImVec2 var1);

    private native float nGetDisplayWindowPaddingX();

    private native float nGetDisplayWindowPaddingY();

    private native void nSetDisplayWindowPadding(float var1, float var2);

    public ImVec2 getDisplaySafeAreaPadding() {
        ImVec2 dst = new ImVec2();
        this.nGetDisplaySafeAreaPadding(dst);
        return dst;
    }

    public float getDisplaySafeAreaPaddingX() {
        return this.nGetDisplaySafeAreaPaddingX();
    }

    public float getDisplaySafeAreaPaddingY() {
        return this.nGetDisplaySafeAreaPaddingY();
    }

    public void getDisplaySafeAreaPadding(ImVec2 dst) {
        this.nGetDisplaySafeAreaPadding(dst);
    }

    public void setDisplaySafeAreaPadding(ImVec2 value) {
        this.nSetDisplaySafeAreaPadding(value.x, value.y);
    }

    public void setDisplaySafeAreaPadding(float valueX, float valueY) {
        this.nSetDisplaySafeAreaPadding(valueX, valueY);
    }

    private native void nGetDisplaySafeAreaPadding(ImVec2 var1);

    private native float nGetDisplaySafeAreaPaddingX();

    private native float nGetDisplaySafeAreaPaddingY();

    private native void nSetDisplaySafeAreaPadding(float var1, float var2);

    public float getDockingSeparatorSize() {
        return this.nGetDockingSeparatorSize();
    }

    public void setDockingSeparatorSize(float value) {
        this.nSetDockingSeparatorSize(value);
    }

    private native float nGetDockingSeparatorSize();

    private native void nSetDockingSeparatorSize(float var1);

    public float getMouseCursorScale() {
        return this.nGetMouseCursorScale();
    }

    public void setMouseCursorScale(float value) {
        this.nSetMouseCursorScale(value);
    }

    private native float nGetMouseCursorScale();

    private native void nSetMouseCursorScale(float var1);

    public boolean getAntiAliasedLines() {
        return this.nGetAntiAliasedLines();
    }

    public void setAntiAliasedLines(boolean value) {
        this.nSetAntiAliasedLines(value);
    }

    private native boolean nGetAntiAliasedLines();

    private native void nSetAntiAliasedLines(boolean var1);

    public boolean getAntiAliasedLinesUseTex() {
        return this.nGetAntiAliasedLinesUseTex();
    }

    public void setAntiAliasedLinesUseTex(boolean value) {
        this.nSetAntiAliasedLinesUseTex(value);
    }

    private native boolean nGetAntiAliasedLinesUseTex();

    private native void nSetAntiAliasedLinesUseTex(boolean var1);

    public boolean getAntiAliasedFill() {
        return this.nGetAntiAliasedFill();
    }

    public void setAntiAliasedFill(boolean value) {
        this.nSetAntiAliasedFill(value);
    }

    private native boolean nGetAntiAliasedFill();

    private native void nSetAntiAliasedFill(boolean var1);

    public float getCurveTessellationTol() {
        return this.nGetCurveTessellationTol();
    }

    public void setCurveTessellationTol(float value) {
        this.nSetCurveTessellationTol(value);
    }

    private native float nGetCurveTessellationTol();

    private native void nSetCurveTessellationTol(float var1);

    public float getCircleTessellationMaxError() {
        return this.nGetCircleTessellationMaxError();
    }

    public void setCircleTessellationMaxError(float value) {
        this.nSetCircleTessellationMaxError(value);
    }

    private native float nGetCircleTessellationMaxError();

    private native void nSetCircleTessellationMaxError(float var1);

    public ImVec4[] getColors() {
        return this.nGetColors();
    }

    public void setColors(ImVec4[] value) {
        this.nSetColors(value);
    }

    private native ImVec4[] nGetColors();

    private native void nSetColors(ImVec4[] var1);

    public ImVec4 getColor(int col) {
        ImVec4 dst = new ImVec4();
        this.getColor(col, dst);
        return dst;
    }

    public native void getColor(int var1, ImVec4 var2);

    public native void setColor(int var1, float var2, float var3, float var4, float var5);

    public native void setColor(int var1, int var2, int var3, int var4, int var5);

    public native void setColor(int var1, int var2);

    public float getHoverStationaryDelay() {
        return this.nGetHoverStationaryDelay();
    }

    public void setHoverStationaryDelay(float value) {
        this.nSetHoverStationaryDelay(value);
    }

    private native float nGetHoverStationaryDelay();

    private native void nSetHoverStationaryDelay(float var1);

    public float getHoverDelayShort() {
        return this.nGetHoverDelayShort();
    }

    public void setHoverDelayShort(float value) {
        this.nSetHoverDelayShort(value);
    }

    private native float nGetHoverDelayShort();

    private native void nSetHoverDelayShort(float var1);

    public float getHoverDelayNormal() {
        return this.nGetHoverDelayNormal();
    }

    public void setHoverDelayNormal(float value) {
        this.nSetHoverDelayNormal(value);
    }

    private native float nGetHoverDelayNormal();

    private native void nSetHoverDelayNormal(float var1);

    public int getHoverFlagsForTooltipMouse() {
        return this.nGetHoverFlagsForTooltipMouse();
    }

    public void setHoverFlagsForTooltipMouse(int value) {
        this.nSetHoverFlagsForTooltipMouse(value);
    }

    private native int nGetHoverFlagsForTooltipMouse();

    private native void nSetHoverFlagsForTooltipMouse(int var1);

    public int getHoverFlagsForTooltipNav() {
        return this.nGetHoverFlagsForTooltipNav();
    }

    public void setHoverFlagsForTooltipNav(int value) {
        this.nSetHoverFlagsForTooltipNav(value);
    }

    private native int nGetHoverFlagsForTooltipNav();

    private native void nSetHoverFlagsForTooltipNav(int var1);

    public void scaleAllSizes(float scaleFactor) {
        this.nScaleAllSizes(scaleFactor);
    }

    private native void nScaleAllSizes(float var1);
}

