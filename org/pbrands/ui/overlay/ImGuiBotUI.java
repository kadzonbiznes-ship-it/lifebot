/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.logic.data.DigLevel
 */
package org.pbrands.ui.overlay;

import imgui.ImVec4;
import java.awt.Color;
import org.pbrands.logic.data.DigLevel;
import org.pbrands.logic.listeners.CoalPriceListener;
import org.pbrands.ui.BotUI;
import org.pbrands.ui.overlay.LifebotImGuiUI;

public class ImGuiBotUI
implements BotUI,
CoalPriceListener {
    private final LifebotImGuiUI ui;
    private boolean visible = true;
    private static final ImVec4 COLOR_TEXT = new ImVec4(0.95f, 0.95f, 0.97f, 1.0f);

    public ImGuiBotUI(LifebotImGuiUI ui) {
        this.ui = ui;
    }

    @Override
    public void updateStatusLabel(boolean running) {
        this.ui.setBotRunning(running);
    }

    @Override
    public void updateStatusLabel(String text, Color color) {
        this.ui.setStatus(text, this.awtColorToImVec4(color));
    }

    @Override
    public void updateYourLevelLabel(DigLevel level) {
        if (level == null) {
            this.ui.setYourLevel("N/A", COLOR_TEXT);
        } else {
            ImVec4 color = this.awtColorToImVec4(level.getDisplayColor());
            this.ui.setYourLevel(level.getDisplayName(), color);
        }
    }

    @Override
    public void updateRequiredLevelLabel(DigLevel level) {
        if (level == null) {
            this.ui.setRequiredLevel("N/A", COLOR_TEXT);
        } else {
            ImVec4 color = this.awtColorToImVec4(level.getDisplayColor());
            this.ui.setRequiredLevel(level.getDisplayName(), color);
        }
    }

    @Override
    public void updateRecognizedLabel(char[] letters, boolean[] correct) {
        this.ui.setRecognizedLetters(letters, correct);
    }

    @Override
    public void updateAccuracyLabel(String accuracy) {
        this.ui.setAccuracy(accuracy);
    }

    @Override
    public void updateCoalPriceLabel(String price) {
        try {
            double priceValue = Double.parseDouble(price.replaceAll("[^0-9.]", ""));
            this.ui.setCoalPrice(priceValue);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    @Override
    public void updateLearningProgress(int collected, int required, boolean isRecording, boolean isReady) {
        this.ui.setStatusLearning(collected, required);
        this.ui.setLearningReady(isReady);
    }

    @Override
    public void updateLearnedStats(double[] delayMeans, double[] delayStdDevs, double[] durationMeans, double[] durationStdDevs) {
        this.ui.setLearnedStatistics(delayMeans, delayStdDevs, durationMeans, durationStdDevs);
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int getPing() {
        return 50;
    }

    @Override
    public int getNotificationVolume() {
        return this.ui.getNotificationVolume();
    }

    @Override
    public boolean isCheckSoundEnabled(String checkType) {
        return this.ui.isSoundEnabled();
    }

    @Override
    public boolean isPauseOnAdminDetectionEnabled() {
        return true;
    }

    @Override
    public boolean isIgnoreWarnings() {
        return false;
    }

    @Override
    public boolean isIgnoreAdministration() {
        return false;
    }

    @Override
    public boolean isIgnoreFullInventory() {
        return false;
    }

    @Override
    public int getDigToggleKeyCode() {
        return 35;
    }

    @Override
    public int getOverlayKeyCode() {
        return 36;
    }

    @Override
    public int getRecognitionSuccessRate() {
        return 1000;
    }

    @Override
    public void coalPriceUpdated(double coalPrice) {
        this.ui.setCoalPrice(coalPrice);
    }

    private ImVec4 awtColorToImVec4(Color color) {
        if (color == null) {
            return COLOR_TEXT;
        }
        return new ImVec4((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    public LifebotImGuiUI getUI() {
        return this.ui;
    }
}

