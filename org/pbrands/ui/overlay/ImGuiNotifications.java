/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.ui.overlay.ImGuiNotifications$Notification
 *  org.pbrands.ui.overlay.ImGuiNotifications$Type
 */
package org.pbrands.ui.overlay;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.pbrands.ui.overlay.ImGuiNotifications;

public class ImGuiNotifications {
    private static ImGuiNotifications instance;
    private final List<Notification> notifications = new CopyOnWriteArrayList<Notification>();
    private Location defaultLocation = Location.TOP_CENTER;
    private float globalOpacity = 1.0f;
    private static final int MAX_NOTIFICATIONS = 4;
    private static final long MIN_NOTIFICATION_INTERVAL = 100L;
    private long lastNotificationTime = 0L;
    private int droppedCount = 0;
    private static final float MIN_WIDTH = 300.0f;
    private static final float MAX_WIDTH = 420.0f;
    private static final float PADDING_H = 14.0f;
    private static final float PADDING_V = 12.0f;
    private static final float ICON_SIZE = 22.0f;
    private static final float ICON_MARGIN = 10.0f;
    private static final float TITLE_MARGIN = 6.0f;
    private static final float CORNER_RADIUS = 8.0f;
    private static final float ACCENT_BAR_WIDTH = 4.0f;
    private static final float PROGRESS_BAR_HEIGHT = 3.0f;
    private static final float SCREEN_MARGIN = 20.0f;
    private static final float NOTIFICATION_SPACING = 10.0f;
    private static final float LINE_SPACING = 2.0f;
    private static final ImVec4 COLOR_TITLE;
    private static final ImVec4 COLOR_MESSAGE;

    private ImGuiNotifications() {
    }

    public static synchronized ImGuiNotifications getInstance() {
        if (instance == null) {
            instance = new ImGuiNotifications();
        }
        return instance;
    }

    public void show(Type type, String message) {
        this.show(type, this.defaultLocation, 5000L, message);
    }

    public void show(Type type, Location location, String message) {
        this.show(type, location, 5000L, message);
    }

    public void show(Type type, Location location, long durationMs, String message) {
        String title = switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Informacja";
            case 1 -> "Sukces";
            case 2 -> "Uwaga";
            case 3 -> "Blad";
        };
        this.show(type, location, durationMs, title, message);
    }

    public void show(Type type, Location location, long durationMs, String title, String message) {
        long now = System.currentTimeMillis();
        if (now - this.lastNotificationTime < 100L) {
            ++this.droppedCount;
            return;
        }
        this.lastNotificationTime = now;
        while (this.notifications.size() >= 4) {
            this.notifications.remove(0);
        }
        Object finalMessage = message;
        if (this.droppedCount > 0) {
            finalMessage = message + " (+ " + this.droppedCount + " pominietyche)";
            this.droppedCount = 0;
        }
        this.notifications.add(new Notification(type, location, title, (String)finalMessage, durationMs));
    }

    private List<String> wrapText(String text, float maxWidth) {
        String[] paragraphs;
        ArrayList<String> result = new ArrayList<String>();
        for (String paragraph : paragraphs = text.split("\n")) {
            if (paragraph.isEmpty()) {
                result.add("");
                continue;
            }
            float paragraphWidth = ImGui.calcTextSize((String)paragraph).x;
            if (paragraphWidth <= maxWidth) {
                result.add(paragraph);
                continue;
            }
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                if (currentLine.length() == 0) {
                    float wordWidth = ImGui.calcTextSize((String)word).x;
                    if (wordWidth > maxWidth) {
                        StringBuilder charLine = new StringBuilder();
                        for (char c : word.toCharArray()) {
                            String test = charLine.toString() + c;
                            if (ImGui.calcTextSize((String)test).x > maxWidth && charLine.length() > 0) {
                                result.add(charLine.toString());
                                charLine = new StringBuilder();
                            }
                            charLine.append(c);
                        }
                        if (charLine.length() <= 0) continue;
                        currentLine = charLine;
                        continue;
                    }
                    currentLine.append(word);
                    continue;
                }
                String test = String.valueOf(currentLine) + " " + word;
                if (ImGui.calcTextSize((String)test).x <= maxWidth) {
                    currentLine.append(" ").append(word);
                    continue;
                }
                result.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
            if (currentLine.length() <= 0) continue;
            result.add(currentLine.toString());
        }
        return result;
    }

    private void calculateDimensions(Notification notif) {
        float lineHeight;
        float titleWidth;
        if (notif.dimensionsCalculated) {
            return;
        }
        float contentStartX = 46.0f;
        float availableWidth = 420.0f - contentStartX - 14.0f;
        notif.wrappedLines = this.wrapText(notif.message, availableWidth);
        float maxLineWidth = titleWidth = ImGui.calcTextSize((String)notif.title).x;
        for (String line : notif.wrappedLines) {
            float lineWidth = ImGui.calcTextSize((String)line).x;
            maxLineWidth = Math.max(maxLineWidth, lineWidth);
        }
        float neededWidth = contentStartX + maxLineWidth + 14.0f;
        notif.calculatedWidth = Math.max(300.0f, Math.min(420.0f, neededWidth));
        float titleHeight = lineHeight = ImGui.getTextLineHeight();
        float messageHeight = (float)notif.wrappedLines.size() * (lineHeight + 2.0f);
        float progressBarSpacing = 8.0f;
        notif.calculatedHeight = 12.0f + titleHeight + 6.0f + messageHeight + progressBarSpacing + 3.0f;
        notif.calculatedHeight = Math.max(70.0f, notif.calculatedHeight);
        notif.dimensionsCalculated = true;
    }

    public void render() {
        if (this.notifications.isEmpty()) {
            return;
        }
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float displayHeight = ImGui.getIO().getDisplaySizeY();
        ArrayList<Notification> toRemove = new ArrayList<Notification>();
        float[] offsetByLocation = new float[Location.values().length];
        for (Notification notif : this.notifications) {
            float posX;
            if (notif.isExpired()) {
                notif.startFadeOut();
            }
            notif.updateAnimation();
            if (notif.isFadeOutComplete()) {
                toRemove.add(notif);
                continue;
            }
            this.calculateDimensions(notif);
            float width = notif.calculatedWidth;
            float height = notif.calculatedHeight;
            int locIndex = notif.location.ordinal();
            float currentOffset = offsetByLocation[locIndex];
            float posY = switch (notif.location.ordinal()) {
                case 0 -> {
                    posX = 20.0f;
                    yield 20.0f + currentOffset + notif.slideOffset;
                }
                case 1 -> {
                    posX = (displayWidth - width) / 2.0f;
                    yield 20.0f + currentOffset + notif.slideOffset;
                }
                case 2 -> {
                    posX = displayWidth - width - 20.0f;
                    yield 20.0f + currentOffset + notif.slideOffset;
                }
                case 3 -> {
                    posX = 20.0f;
                    yield displayHeight - height - 20.0f - currentOffset - notif.slideOffset;
                }
                case 4 -> {
                    posX = (displayWidth - width) / 2.0f;
                    yield displayHeight - height - 20.0f - currentOffset - notif.slideOffset;
                }
                case 5 -> {
                    posX = displayWidth - width - 20.0f;
                    yield displayHeight - height - 20.0f - currentOffset - notif.slideOffset;
                }
                default -> {
                    posX = (displayWidth - width) / 2.0f;
                    yield 20.0f + currentOffset;
                }
            };
            int n = locIndex;
            offsetByLocation[n] = offsetByLocation[n] + (height + 10.0f);
            this.renderNotification(notif, posX, posY, width, height);
        }
        this.notifications.removeAll(toRemove);
    }

    private void renderNotification(Notification notif, float posX, float posY, float width, float height) {
        ImGui.setNextWindowPos(posX, posY);
        ImGui.setNextWindowSize(width, height);
        int windowFlags = 799503;
        float effectiveAlpha = notif.alpha * this.globalOpacity;
        ImGui.pushStyleVar(0, effectiveAlpha);
        ImGui.pushStyleVar(3, 8.0f);
        ImGui.pushStyleVar(4, 0.0f);
        ImGui.pushStyleVar(2, 0.0f, 0.0f);
        ImGui.pushStyleColor(2, 0.0f, 0.0f, 0.0f, 0.0f);
        String windowId = "##notif_" + notif.hashCode();
        if (ImGui.begin(windowId, windowFlags)) {
            ImDrawList drawList = ImGui.getWindowDrawList();
            float winX = ImGui.getWindowPosX();
            float winY = ImGui.getWindowPosY();
            int shadowColor = ImGui.colorConvertFloat4ToU32(0.0f, 0.0f, 0.0f, 0.4f * effectiveAlpha);
            drawList.addRectFilled(winX + 2.0f, winY + 2.0f, winX + width + 2.0f, winY + height + 2.0f, shadowColor, 8.0f);
            int bgColor = ImGui.colorConvertFloat4ToU32(notif.type.bgColor.x, notif.type.bgColor.y, notif.type.bgColor.z, notif.type.bgColor.w * effectiveAlpha);
            drawList.addRectFilled(winX, winY, winX + width, winY + height, bgColor, 8.0f);
            float textLineHeight = ImGui.getTextLineHeight();
            int messageLines = notif.wrappedLines != null ? notif.wrappedLines.size() : 1;
            float contentHeight = textLineHeight + 6.0f + (float)messageLines * (textLineHeight + 2.0f);
            float iconCenterX = winX + 14.0f + 11.0f;
            float iconCenterY = winY + 12.0f + contentHeight / 2.0f;
            float iconRadius = 11.0f;
            int iconColor = ImGui.colorConvertFloat4ToU32(notif.type.accentColor.x, notif.type.accentColor.y, notif.type.accentColor.z, effectiveAlpha);
            this.drawCustomIcon(drawList, notif.type, iconCenterX, iconCenterY, iconRadius, iconColor, effectiveAlpha);
            float titleX = winX + 14.0f + 22.0f + 10.0f;
            float titleY = winY + 12.0f;
            int titleColor = ImGui.colorConvertFloat4ToU32(ImGuiNotifications.COLOR_TITLE.x, ImGuiNotifications.COLOR_TITLE.y, ImGuiNotifications.COLOR_TITLE.z, effectiveAlpha);
            drawList.addText(titleX, titleY, titleColor, notif.title);
            float messageX = titleX;
            float messageY = titleY + textLineHeight + 6.0f;
            int messageColor = ImGui.colorConvertFloat4ToU32(ImGuiNotifications.COLOR_MESSAGE.x, ImGuiNotifications.COLOR_MESSAGE.y, ImGuiNotifications.COLOR_MESSAGE.z, effectiveAlpha);
            float lineHeight = textLineHeight + 2.0f;
            if (notif.wrappedLines != null) {
                for (String line : notif.wrappedLines) {
                    drawList.addText(messageX, messageY, messageColor, line);
                    messageY += lineHeight;
                }
            }
            float progress = notif.getProgress();
            float barMargin = 8.0f;
            float barY = winY + height - 3.0f - 2.0f;
            float barMaxWidth = width - barMargin * 2.0f;
            float barWidth = barMaxWidth * (1.0f - progress);
            int barBgColor = ImGui.colorConvertFloat4ToU32(0.0f, 0.0f, 0.0f, 0.3f * effectiveAlpha);
            drawList.addRectFilled(winX + barMargin, barY, winX + width - barMargin, barY + 3.0f, barBgColor, 2.0f);
            if (barWidth > 1.0f) {
                int barColor = ImGui.colorConvertFloat4ToU32(notif.type.accentColor.x, notif.type.accentColor.y, notif.type.accentColor.z, 0.9f * effectiveAlpha);
                drawList.addRectFilled(winX + barMargin, barY, winX + barMargin + barWidth, barY + 3.0f, barColor, 2.0f);
            }
            int highlightColor = ImGui.colorConvertFloat4ToU32(1.0f, 1.0f, 1.0f, 0.05f * effectiveAlpha);
            drawList.addLine(winX + 8.0f, winY + 1.0f, winX + width - 8.0f, winY + 1.0f, highlightColor, 1.0f);
        }
        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar(4);
    }

    private void drawCustomIcon(ImDrawList drawList, Type type, float cx, float cy, float radius, int color, float alpha) {
        int white = ImGui.colorConvertFloat4ToU32(1.0f, 1.0f, 1.0f, alpha);
        switch (type.ordinal()) {
            case 0: {
                drawList.addCircleFilled(cx, cy, radius, color);
                float dotY = cy - radius * 0.35f;
                drawList.addCircleFilled(cx, dotY, radius * 0.15f, white);
                float lineTop = cy - radius * 0.1f;
                float lineBot = cy + radius * 0.5f;
                float lineW = radius * 0.18f;
                drawList.addRectFilled(cx - lineW, lineTop, cx + lineW, lineBot, white, lineW);
                break;
            }
            case 1: {
                drawList.addCircleFilled(cx, cy, radius, color);
                float s = radius * 0.7f;
                float thickness = radius * 0.25f;
                drawList.addLine(cx - s * 0.5f, cy, cx - s * 0.05f, cy + s * 0.45f, white, thickness);
                drawList.addLine(cx - s * 0.05f, cy + s * 0.45f, cx + s * 0.55f, cy - s * 0.35f, white, thickness);
                break;
            }
            case 2: {
                float triH = radius * 1.7f;
                float triW = radius * 1.6f;
                float topY = cy - triH * 0.5f;
                float botY = cy + triH * 0.5f;
                drawList.addTriangleFilled(cx, topY, cx - triW / 2.0f, botY, cx + triW / 2.0f, botY, color);
                int dark = ImGui.colorConvertFloat4ToU32(0.12f, 0.1f, 0.04f, alpha);
                float exclTop = cy - triH * 0.22f;
                float exclBot = cy + triH * 0.15f;
                float exclW = radius * 0.14f;
                drawList.addRectFilled(cx - exclW, exclTop, cx + exclW, exclBot, dark, exclW);
                drawList.addCircleFilled(cx, cy + triH * 0.32f, radius * 0.13f, dark);
                break;
            }
            case 3: {
                drawList.addCircleFilled(cx, cy, radius, color);
                float s = radius * 0.38f;
                float thickness = radius * 0.22f;
                float xOff = -1.0f;
                drawList.addLine(cx - s + xOff, cy - s, cx + s + xOff, cy + s, white, thickness);
                drawList.addLine(cx - s + xOff, cy + s, cx + s + xOff, cy - s, white, thickness);
            }
        }
    }

    public void clear() {
        this.notifications.clear();
    }

    public boolean hasNotifications() {
        return !this.notifications.isEmpty();
    }

    public void setDefaultLocation(Location location) {
        this.defaultLocation = location;
    }

    public void setGlobalOpacity(float opacity) {
        this.globalOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
    }

    public float getGlobalOpacity() {
        return this.globalOpacity;
    }

    static {
        COLOR_TITLE = new ImVec4(1.0f, 1.0f, 1.0f, 1.0f);
        COLOR_MESSAGE = new ImVec4(0.8f, 0.82f, 0.88f, 1.0f);
    }

    public static enum Location {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT;

    }
}

