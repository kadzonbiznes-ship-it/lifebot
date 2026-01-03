/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.mta;

import lombok.Generated;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MTAChatSettings {
    private final int chatFont;
    private final int chatLines;
    private final Color chatColor;
    private final Color chatTextColor;
    private final int chatTextOutline;
    private final double chatScaleX;
    private final double chatScaleY;
    private final double chatWidth;
    private final int chatCssStyleText;
    private final int chatCssStyleBackground;
    private final int chatLineLife;
    private final int chatLineFadeOut;
    private final int chatUseCegui;
    private final int chatNickCompletion;
    private final double chatPositionOffsetX;
    private final double chatPositionOffsetY;
    private final int chatPositionHorizontal;
    private final int chatPositionVertical;
    private final int chatTextAlignment;
    private final int displayFullscreenStyle;
    private final int displayWindowed;

    public static MTAChatSettings fromXml(Document doc) {
        Element settings = (Element)doc.getElementsByTagName("settings").item(0);
        if (settings == null) {
            throw new IllegalArgumentException("<settings> section not found in XML");
        }
        String scaleStr = MTAChatSettings.getString(settings, "chat_scale", "1.0 1.0");
        String[] scaleParts = scaleStr.split(" ");
        return MTAChatSettings.builder().chatFont(MTAChatSettings.getInt(settings, "chat_font", 0)).chatLines(MTAChatSettings.getInt(settings, "chat_lines", 7)).chatColor(Color.parse(MTAChatSettings.getString(settings, "chat_color", "0 0 0 128"))).chatTextColor(Color.parse(MTAChatSettings.getString(settings, "chat_text_color", "255 255 255 255"))).chatTextOutline(MTAChatSettings.getInt(settings, "chat_text_outline", 0)).chatScaleX(Double.parseDouble(scaleParts[0])).chatScaleY(Double.parseDouble(scaleParts.length > 1 ? scaleParts[1] : scaleParts[0])).chatWidth(MTAChatSettings.getDouble(settings, "chat_width", 1.0)).chatCssStyleText(MTAChatSettings.getInt(settings, "chat_css_style_text", 0)).chatCssStyleBackground(MTAChatSettings.getInt(settings, "chat_css_style_background", 0)).chatLineLife(MTAChatSettings.getInt(settings, "chat_line_life", 12000)).chatLineFadeOut(MTAChatSettings.getInt(settings, "chat_line_fade_out", 3000)).chatUseCegui(MTAChatSettings.getInt(settings, "chat_use_cegui", 0)).chatNickCompletion(MTAChatSettings.getInt(settings, "chat_nickcompletion", 0)).chatPositionOffsetX(MTAChatSettings.getDouble(settings, "chat_position_offset_x", 0.0125)).chatPositionOffsetY(MTAChatSettings.getDouble(settings, "chat_position_offset_y", 0.015)).chatPositionHorizontal(MTAChatSettings.getInt(settings, "chat_position_horizontal", 0)).chatPositionVertical(MTAChatSettings.getInt(settings, "chat_position_vertical", 0)).chatTextAlignment(MTAChatSettings.getInt(settings, "chat_text_alignment", 0)).displayFullscreenStyle(MTAChatSettings.getInt(settings, "display_fullscreen_style", 0)).displayWindowed(MTAChatSettings.getInt(settings, "display_windowed", 0)).build();
    }

    private static String getString(Element parent, String tagName, String defaultValue) {
        String value;
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0 && (value = nodes.item(0).getTextContent()) != null && !value.isBlank()) {
            return value;
        }
        return defaultValue;
    }

    private static int getInt(Element parent, String tagName, int defaultValue) {
        try {
            return Integer.parseInt(MTAChatSettings.getString(parent, tagName, String.valueOf(defaultValue)).trim());
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double getDouble(Element parent, String tagName, double defaultValue) {
        try {
            return Double.parseDouble(MTAChatSettings.getString(parent, tagName, String.valueOf(defaultValue)).trim());
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Generated
    MTAChatSettings(int chatFont, int chatLines, Color chatColor, Color chatTextColor, int chatTextOutline, double chatScaleX, double chatScaleY, double chatWidth, int chatCssStyleText, int chatCssStyleBackground, int chatLineLife, int chatLineFadeOut, int chatUseCegui, int chatNickCompletion, double chatPositionOffsetX, double chatPositionOffsetY, int chatPositionHorizontal, int chatPositionVertical, int chatTextAlignment, int displayFullscreenStyle, int displayWindowed) {
        this.chatFont = chatFont;
        this.chatLines = chatLines;
        this.chatColor = chatColor;
        this.chatTextColor = chatTextColor;
        this.chatTextOutline = chatTextOutline;
        this.chatScaleX = chatScaleX;
        this.chatScaleY = chatScaleY;
        this.chatWidth = chatWidth;
        this.chatCssStyleText = chatCssStyleText;
        this.chatCssStyleBackground = chatCssStyleBackground;
        this.chatLineLife = chatLineLife;
        this.chatLineFadeOut = chatLineFadeOut;
        this.chatUseCegui = chatUseCegui;
        this.chatNickCompletion = chatNickCompletion;
        this.chatPositionOffsetX = chatPositionOffsetX;
        this.chatPositionOffsetY = chatPositionOffsetY;
        this.chatPositionHorizontal = chatPositionHorizontal;
        this.chatPositionVertical = chatPositionVertical;
        this.chatTextAlignment = chatTextAlignment;
        this.displayFullscreenStyle = displayFullscreenStyle;
        this.displayWindowed = displayWindowed;
    }

    @Generated
    public static MTAChatSettingsBuilder builder() {
        return new MTAChatSettingsBuilder();
    }

    @Generated
    public MTAChatSettingsBuilder toBuilder() {
        return new MTAChatSettingsBuilder().chatFont(this.chatFont).chatLines(this.chatLines).chatColor(this.chatColor).chatTextColor(this.chatTextColor).chatTextOutline(this.chatTextOutline).chatScaleX(this.chatScaleX).chatScaleY(this.chatScaleY).chatWidth(this.chatWidth).chatCssStyleText(this.chatCssStyleText).chatCssStyleBackground(this.chatCssStyleBackground).chatLineLife(this.chatLineLife).chatLineFadeOut(this.chatLineFadeOut).chatUseCegui(this.chatUseCegui).chatNickCompletion(this.chatNickCompletion).chatPositionOffsetX(this.chatPositionOffsetX).chatPositionOffsetY(this.chatPositionOffsetY).chatPositionHorizontal(this.chatPositionHorizontal).chatPositionVertical(this.chatPositionVertical).chatTextAlignment(this.chatTextAlignment).displayFullscreenStyle(this.displayFullscreenStyle).displayWindowed(this.displayWindowed);
    }

    @Generated
    public int getChatFont() {
        return this.chatFont;
    }

    @Generated
    public int getChatLines() {
        return this.chatLines;
    }

    @Generated
    public Color getChatColor() {
        return this.chatColor;
    }

    @Generated
    public Color getChatTextColor() {
        return this.chatTextColor;
    }

    @Generated
    public int getChatTextOutline() {
        return this.chatTextOutline;
    }

    @Generated
    public double getChatScaleX() {
        return this.chatScaleX;
    }

    @Generated
    public double getChatScaleY() {
        return this.chatScaleY;
    }

    @Generated
    public double getChatWidth() {
        return this.chatWidth;
    }

    @Generated
    public int getChatCssStyleText() {
        return this.chatCssStyleText;
    }

    @Generated
    public int getChatCssStyleBackground() {
        return this.chatCssStyleBackground;
    }

    @Generated
    public int getChatLineLife() {
        return this.chatLineLife;
    }

    @Generated
    public int getChatLineFadeOut() {
        return this.chatLineFadeOut;
    }

    @Generated
    public int getChatUseCegui() {
        return this.chatUseCegui;
    }

    @Generated
    public int getChatNickCompletion() {
        return this.chatNickCompletion;
    }

    @Generated
    public double getChatPositionOffsetX() {
        return this.chatPositionOffsetX;
    }

    @Generated
    public double getChatPositionOffsetY() {
        return this.chatPositionOffsetY;
    }

    @Generated
    public int getChatPositionHorizontal() {
        return this.chatPositionHorizontal;
    }

    @Generated
    public int getChatPositionVertical() {
        return this.chatPositionVertical;
    }

    @Generated
    public int getChatTextAlignment() {
        return this.chatTextAlignment;
    }

    @Generated
    public int getDisplayFullscreenStyle() {
        return this.displayFullscreenStyle;
    }

    @Generated
    public int getDisplayWindowed() {
        return this.displayWindowed;
    }

    @Generated
    public static class MTAChatSettingsBuilder {
        @Generated
        private int chatFont;
        @Generated
        private int chatLines;
        @Generated
        private Color chatColor;
        @Generated
        private Color chatTextColor;
        @Generated
        private int chatTextOutline;
        @Generated
        private double chatScaleX;
        @Generated
        private double chatScaleY;
        @Generated
        private double chatWidth;
        @Generated
        private int chatCssStyleText;
        @Generated
        private int chatCssStyleBackground;
        @Generated
        private int chatLineLife;
        @Generated
        private int chatLineFadeOut;
        @Generated
        private int chatUseCegui;
        @Generated
        private int chatNickCompletion;
        @Generated
        private double chatPositionOffsetX;
        @Generated
        private double chatPositionOffsetY;
        @Generated
        private int chatPositionHorizontal;
        @Generated
        private int chatPositionVertical;
        @Generated
        private int chatTextAlignment;
        @Generated
        private int displayFullscreenStyle;
        @Generated
        private int displayWindowed;

        @Generated
        MTAChatSettingsBuilder() {
        }

        @Generated
        public MTAChatSettingsBuilder chatFont(int chatFont) {
            this.chatFont = chatFont;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatLines(int chatLines) {
            this.chatLines = chatLines;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatColor(Color chatColor) {
            this.chatColor = chatColor;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatTextColor(Color chatTextColor) {
            this.chatTextColor = chatTextColor;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatTextOutline(int chatTextOutline) {
            this.chatTextOutline = chatTextOutline;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatScaleX(double chatScaleX) {
            this.chatScaleX = chatScaleX;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatScaleY(double chatScaleY) {
            this.chatScaleY = chatScaleY;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatWidth(double chatWidth) {
            this.chatWidth = chatWidth;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatCssStyleText(int chatCssStyleText) {
            this.chatCssStyleText = chatCssStyleText;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatCssStyleBackground(int chatCssStyleBackground) {
            this.chatCssStyleBackground = chatCssStyleBackground;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatLineLife(int chatLineLife) {
            this.chatLineLife = chatLineLife;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatLineFadeOut(int chatLineFadeOut) {
            this.chatLineFadeOut = chatLineFadeOut;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatUseCegui(int chatUseCegui) {
            this.chatUseCegui = chatUseCegui;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatNickCompletion(int chatNickCompletion) {
            this.chatNickCompletion = chatNickCompletion;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatPositionOffsetX(double chatPositionOffsetX) {
            this.chatPositionOffsetX = chatPositionOffsetX;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatPositionOffsetY(double chatPositionOffsetY) {
            this.chatPositionOffsetY = chatPositionOffsetY;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatPositionHorizontal(int chatPositionHorizontal) {
            this.chatPositionHorizontal = chatPositionHorizontal;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatPositionVertical(int chatPositionVertical) {
            this.chatPositionVertical = chatPositionVertical;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder chatTextAlignment(int chatTextAlignment) {
            this.chatTextAlignment = chatTextAlignment;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder displayFullscreenStyle(int displayFullscreenStyle) {
            this.displayFullscreenStyle = displayFullscreenStyle;
            return this;
        }

        @Generated
        public MTAChatSettingsBuilder displayWindowed(int displayWindowed) {
            this.displayWindowed = displayWindowed;
            return this;
        }

        @Generated
        public MTAChatSettings build() {
            return new MTAChatSettings(this.chatFont, this.chatLines, this.chatColor, this.chatTextColor, this.chatTextOutline, this.chatScaleX, this.chatScaleY, this.chatWidth, this.chatCssStyleText, this.chatCssStyleBackground, this.chatLineLife, this.chatLineFadeOut, this.chatUseCegui, this.chatNickCompletion, this.chatPositionOffsetX, this.chatPositionOffsetY, this.chatPositionHorizontal, this.chatPositionVertical, this.chatTextAlignment, this.displayFullscreenStyle, this.displayWindowed);
        }

        @Generated
        public String toString() {
            return "MTAChatSettings.MTAChatSettingsBuilder(chatFont=" + this.chatFont + ", chatLines=" + this.chatLines + ", chatColor=" + String.valueOf(this.chatColor) + ", chatTextColor=" + String.valueOf(this.chatTextColor) + ", chatTextOutline=" + this.chatTextOutline + ", chatScaleX=" + this.chatScaleX + ", chatScaleY=" + this.chatScaleY + ", chatWidth=" + this.chatWidth + ", chatCssStyleText=" + this.chatCssStyleText + ", chatCssStyleBackground=" + this.chatCssStyleBackground + ", chatLineLife=" + this.chatLineLife + ", chatLineFadeOut=" + this.chatLineFadeOut + ", chatUseCegui=" + this.chatUseCegui + ", chatNickCompletion=" + this.chatNickCompletion + ", chatPositionOffsetX=" + this.chatPositionOffsetX + ", chatPositionOffsetY=" + this.chatPositionOffsetY + ", chatPositionHorizontal=" + this.chatPositionHorizontal + ", chatPositionVertical=" + this.chatPositionVertical + ", chatTextAlignment=" + this.chatTextAlignment + ", displayFullscreenStyle=" + this.displayFullscreenStyle + ", displayWindowed=" + this.displayWindowed + ")";
        }
    }

    public record Color(int r, int g, int b, int a) {
        public static Color parse(String value) {
            if (value == null || value.isBlank()) {
                return new Color(255, 255, 255, 255);
            }
            String[] parts = value.split(" ");
            return new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        }

        @Override
        public String toString() {
            return this.r + " " + this.g + " " + this.b + " " + this.a;
        }
    }
}

