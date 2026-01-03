/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui.overlay;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec4;

public final class LifebotTheme {
    public static final ImVec4 BG_DARKEST = new ImVec4(0.08f, 0.09f, 0.1f, 0.97f);
    public static final ImVec4 BG_DARK = new ImVec4(0.11f, 0.12f, 0.13f, 1.0f);
    public static final ImVec4 BG_MEDIUM = new ImVec4(0.15f, 0.16f, 0.18f, 1.0f);
    public static final ImVec4 BG_LIGHT = new ImVec4(0.2f, 0.21f, 0.24f, 1.0f);
    public static final ImVec4 BG_LIGHTEST = new ImVec4(0.26f, 0.28f, 0.32f, 1.0f);
    public static final ImVec4 ACCENT_PRIMARY = new ImVec4(0.25f, 0.56f, 0.87f, 1.0f);
    public static final ImVec4 ACCENT_PRIMARY_HOVER = new ImVec4(0.35f, 0.65f, 0.95f, 1.0f);
    public static final ImVec4 ACCENT_PRIMARY_MUTED = new ImVec4(0.25f, 0.56f, 0.87f, 0.15f);
    public static final ImVec4 ACCENT_PRIMARY_GLOW = new ImVec4(0.25f, 0.56f, 0.87f, 0.3f);
    public static final ImVec4 ACCENT_SECONDARY = new ImVec4(0.0f, 0.75f, 0.85f, 1.0f);
    public static final ImVec4 ACCENT_SECONDARY_MUTED = new ImVec4(0.0f, 0.75f, 0.85f, 0.2f);
    public static final ImVec4 SUCCESS = new ImVec4(0.3f, 0.78f, 0.47f, 1.0f);
    public static final ImVec4 SUCCESS_MUTED = new ImVec4(0.3f, 0.78f, 0.47f, 0.15f);
    public static final ImVec4 ERROR = new ImVec4(0.9f, 0.35f, 0.38f, 1.0f);
    public static final ImVec4 ERROR_MUTED = new ImVec4(0.9f, 0.35f, 0.38f, 0.15f);
    public static final ImVec4 WARNING = new ImVec4(0.95f, 0.75f, 0.25f, 1.0f);
    public static final ImVec4 WARNING_MUTED = new ImVec4(0.95f, 0.75f, 0.25f, 0.15f);
    public static final ImVec4 PURPLE = new ImVec4(0.58f, 0.44f, 0.86f, 1.0f);
    public static final ImVec4 PURPLE_MUTED = new ImVec4(0.58f, 0.44f, 0.86f, 0.15f);
    public static final ImVec4 TEXT_PRIMARY = new ImVec4(0.95f, 0.96f, 0.98f, 1.0f);
    public static final ImVec4 TEXT_SECONDARY = new ImVec4(0.62f, 0.66f, 0.74f, 1.0f);
    public static final ImVec4 TEXT_DISABLED = new ImVec4(0.42f, 0.45f, 0.52f, 1.0f);
    public static final ImVec4 TEXT_HINT = new ImVec4(0.35f, 0.38f, 0.45f, 1.0f);
    public static final ImVec4 BORDER = new ImVec4(0.22f, 0.24f, 0.28f, 1.0f);
    public static final ImVec4 BORDER_HIGHLIGHT = new ImVec4(0.25f, 0.56f, 0.87f, 0.5f);
    public static final ImVec4 OVERLAY_DIM = new ImVec4(0.0f, 0.0f, 0.0f, 0.6f);
    public static final ImVec4 SHADOW = new ImVec4(0.0f, 0.0f, 0.0f, 0.4f);

    private LifebotTheme() {
    }

    public static int toU32(ImVec4 color) {
        return ImGui.colorConvertFloat4ToU32(color.x, color.y, color.z, color.w);
    }

    public static int toU32(ImVec4 color, float alpha) {
        return ImGui.colorConvertFloat4ToU32(color.x, color.y, color.z, alpha);
    }

    public static int toU32Scaled(ImVec4 color, float scale) {
        return ImGui.colorConvertFloat4ToU32(color.x * scale, color.y * scale, color.z * scale, color.w);
    }

    public static int toU32Scaled(ImVec4 color, float scale, float alpha) {
        return ImGui.colorConvertFloat4ToU32(color.x * scale, color.y * scale, color.z * scale, alpha);
    }

    public static void apply() {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(8.0f);
        style.setFrameRounding(5.0f);
        style.setPopupRounding(6.0f);
        style.setScrollbarRounding(4.0f);
        style.setGrabRounding(4.0f);
        style.setTabRounding(5.0f);
        style.setWindowPadding(10.0f, 10.0f);
        style.setFramePadding(8.0f, 5.0f);
        style.setItemSpacing(8.0f, 6.0f);
        style.setItemInnerSpacing(6.0f, 4.0f);
        style.setWindowBorderSize(1.0f);
        style.setFrameBorderSize(0.0f);
        style.setPopupBorderSize(1.0f);
        style.setScrollbarSize(12.0f);
        style.setGrabMinSize(10.0f);
        style.setColor(2, LifebotTheme.BG_DARKEST.x, LifebotTheme.BG_DARKEST.y, LifebotTheme.BG_DARKEST.z, LifebotTheme.BG_DARKEST.w);
        style.setColor(3, LifebotTheme.BG_DARK.x, LifebotTheme.BG_DARK.y, LifebotTheme.BG_DARK.z, 0.0f);
        style.setColor(4, LifebotTheme.BG_DARK.x, LifebotTheme.BG_DARK.y, LifebotTheme.BG_DARK.z, 0.98f);
        style.setColor(13, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 1.0f);
        style.setColor(5, LifebotTheme.BORDER.x, LifebotTheme.BORDER.y, LifebotTheme.BORDER.z, LifebotTheme.BORDER.w);
        style.setColor(6, 0, 0, 0, 0);
        style.setColor(0, LifebotTheme.TEXT_PRIMARY.x, LifebotTheme.TEXT_PRIMARY.y, LifebotTheme.TEXT_PRIMARY.z, LifebotTheme.TEXT_PRIMARY.w);
        style.setColor(1, LifebotTheme.TEXT_DISABLED.x, LifebotTheme.TEXT_DISABLED.y, LifebotTheme.TEXT_DISABLED.z, LifebotTheme.TEXT_DISABLED.w);
        style.setColor(24, LifebotTheme.ACCENT_PRIMARY_MUTED.x, LifebotTheme.ACCENT_PRIMARY_MUTED.y, LifebotTheme.ACCENT_PRIMARY_MUTED.z, 0.4f);
        style.setColor(25, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 0.5f);
        style.setColor(26, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 0.7f);
        style.setColor(21, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 1.0f);
        style.setColor(22, LifebotTheme.BG_LIGHT.x, LifebotTheme.BG_LIGHT.y, LifebotTheme.BG_LIGHT.z, 1.0f);
        style.setColor(23, LifebotTheme.BG_LIGHTEST.x, LifebotTheme.BG_LIGHTEST.y, LifebotTheme.BG_LIGHTEST.z, 1.0f);
        style.setColor(7, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 1.0f);
        style.setColor(8, LifebotTheme.BG_LIGHT.x, LifebotTheme.BG_LIGHT.y, LifebotTheme.BG_LIGHT.z, 1.0f);
        style.setColor(9, LifebotTheme.BG_LIGHTEST.x, LifebotTheme.BG_LIGHTEST.y, LifebotTheme.BG_LIGHTEST.z, 1.0f);
        style.setColor(18, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 1.0f);
        style.setColor(19, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 0.85f);
        style.setColor(20, LifebotTheme.ACCENT_PRIMARY_HOVER.x, LifebotTheme.ACCENT_PRIMARY_HOVER.y, LifebotTheme.ACCENT_PRIMARY_HOVER.z, 1.0f);
        style.setColor(14, LifebotTheme.BG_DARK.x, LifebotTheme.BG_DARK.y, LifebotTheme.BG_DARK.z, 0.5f);
        style.setColor(15, LifebotTheme.BG_LIGHT.x, LifebotTheme.BG_LIGHT.y, LifebotTheme.BG_LIGHT.z, 0.8f);
        style.setColor(16, LifebotTheme.BG_LIGHTEST.x, LifebotTheme.BG_LIGHTEST.y, LifebotTheme.BG_LIGHTEST.z, 1.0f);
        style.setColor(17, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 1.0f);
        style.setColor(27, LifebotTheme.BORDER.x, LifebotTheme.BORDER.y, LifebotTheme.BORDER.z, 0.6f);
        style.setColor(28, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 0.6f);
        style.setColor(29, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 1.0f);
        style.setColor(30, LifebotTheme.BORDER.x, LifebotTheme.BORDER.y, LifebotTheme.BORDER.z, 0.3f);
        style.setColor(31, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 0.5f);
        style.setColor(32, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 0.9f);
        style.setColor(33, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 1.0f);
        style.setColor(34, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 0.6f);
        style.setColor(35, LifebotTheme.ACCENT_PRIMARY_MUTED.x, LifebotTheme.ACCENT_PRIMARY_MUTED.y, LifebotTheme.ACCENT_PRIMARY_MUTED.z, 1.0f);
        style.setColor(36, LifebotTheme.BG_DARK.x, LifebotTheme.BG_DARK.y, LifebotTheme.BG_DARK.z, 1.0f);
        style.setColor(37, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 1.0f);
        style.setColor(10, LifebotTheme.BG_DARK.x, LifebotTheme.BG_DARK.y, LifebotTheme.BG_DARK.z, 1.0f);
        style.setColor(11, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 1.0f);
        style.setColor(12, LifebotTheme.BG_DARKEST.x, LifebotTheme.BG_DARKEST.y, LifebotTheme.BG_DARKEST.z, 0.9f);
        style.setColor(42, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 1.0f);
        style.setColor(43, LifebotTheme.ACCENT_PRIMARY_HOVER.x, LifebotTheme.ACCENT_PRIMARY_HOVER.y, LifebotTheme.ACCENT_PRIMARY_HOVER.z, 1.0f);
        style.setColor(44, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 1.0f);
        style.setColor(45, LifebotTheme.BORDER.x, LifebotTheme.BORDER.y, LifebotTheme.BORDER.z, 1.0f);
        style.setColor(46, LifebotTheme.BORDER.x, LifebotTheme.BORDER.y, LifebotTheme.BORDER.z, 0.5f);
        style.setColor(47, 0, 0, 0, 0);
        style.setColor(48, LifebotTheme.BG_MEDIUM.x, LifebotTheme.BG_MEDIUM.y, LifebotTheme.BG_MEDIUM.z, 0.3f);
        style.setColor(51, LifebotTheme.ACCENT_PRIMARY.x, LifebotTheme.ACCENT_PRIMARY.y, LifebotTheme.ACCENT_PRIMARY.z, 1.0f);
        style.setColor(54, LifebotTheme.OVERLAY_DIM.x, LifebotTheme.OVERLAY_DIM.y, LifebotTheme.OVERLAY_DIM.z, LifebotTheme.OVERLAY_DIM.w);
    }

    public static void textCentered(String text) {
        float textWidth = ImGui.calcTextSize((String)text).x;
        float availWidth = ImGui.getContentRegionAvailX();
        float cursorX = (availWidth - textWidth) / 2.0f;
        if (cursorX > 0.0f) {
            ImGui.setCursorPosX(ImGui.getCursorPosX() + cursorX);
        }
        ImGui.text(text);
    }
}

