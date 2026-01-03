/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.logic.data.DigLevel
 */
package org.pbrands.ui;

import java.awt.Color;
import org.pbrands.logic.data.DigLevel;

public interface BotUI {
    public void updateStatusLabel(boolean var1);

    public void updateStatusLabel(String var1, Color var2);

    public void updateYourLevelLabel(DigLevel var1);

    public void updateRequiredLevelLabel(DigLevel var1);

    public void updateRecognizedLabel(char[] var1, boolean[] var2);

    public void updateAccuracyLabel(String var1);

    public void updateCoalPriceLabel(String var1);

    public void updateLearningProgress(int var1, int var2, boolean var3, boolean var4);

    public void updateLearnedStats(double[] var1, double[] var2, double[] var3, double[] var4);

    public boolean isVisible();

    public void setVisible(boolean var1);

    public int getPing();

    public int getNotificationVolume();

    public boolean isCheckSoundEnabled(String var1);

    public boolean isPauseOnAdminDetectionEnabled();

    public boolean isIgnoreWarnings();

    public boolean isIgnoreAdministration();

    public boolean isIgnoreFullInventory();

    public int getDigToggleKeyCode();

    public int getOverlayKeyCode();

    public int getRecognitionSuccessRate();
}

