/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.logic.FishingColor
 *  org.pbrands.logic.FishingState
 */
package org.pbrands.logic;

import java.awt.Color;
import java.awt.Point;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.pbrands.logic.Application;
import org.pbrands.logic.FishingColor;
import org.pbrands.logic.FishingState;
import org.pbrands.logic.Sizes;

public class ScreenHelper {
    private final Application application;

    public ScreenHelper(Application application) {
        this.application = application;
    }

    public boolean isBlackCorners(Robot robot) {
        int width = this.application.resolution.getWidth();
        int height = this.application.resolution.getHeight();
        int tolerance = 10;
        Color topLeftColor = robot.getPixelColor(0, 0);
        Color topRightColor = robot.getPixelColor(width - 1, 0);
        Color bottomLeftColor = robot.getPixelColor(0, height - 1);
        Color bottomRightColor = robot.getPixelColor(width - 1, height - 1);
        String status = String.format("TopLeft: (%d, %d, %d), TopRight: (%d, %d, %d), BottomLeft: (%d, %d, %d), BottomRight: (%d, %d, %d)", topLeftColor.getRed(), topLeftColor.getGreen(), topLeftColor.getBlue(), topRightColor.getRed(), topRightColor.getGreen(), topRightColor.getBlue(), bottomLeftColor.getRed(), bottomLeftColor.getGreen(), bottomLeftColor.getBlue(), bottomRightColor.getRed(), bottomRightColor.getGreen(), bottomRightColor.getBlue());
        this.application.overlayWindow.updateStatus(status);
        return this.isColorCloseToBlack(topLeftColor, tolerance) && this.isColorCloseToBlack(topRightColor, tolerance) && this.isColorCloseToBlack(bottomLeftColor, tolerance) && this.isColorCloseToBlack(bottomRightColor, tolerance);
    }

    private boolean isColorCloseToBlack(Color color, int tolerance) {
        return Math.abs(color.getRed()) <= tolerance && Math.abs(color.getGreen()) <= tolerance && Math.abs(color.getBlue()) <= tolerance;
    }

    private static boolean isColorMatchBrightnessInvariant(int pixel, Color targetColor, float hueTolerance) {
        float[] hsbTarget;
        Color c = new Color(pixel, true);
        float[] hsbPixel = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        float hueDiff = Math.abs(hsbPixel[0] - (hsbTarget = Color.RGBtoHSB(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(), null))[0]);
        if (hueDiff > 0.5f) {
            hueDiff = 1.0f - hueDiff;
        }
        boolean saturationOk = hsbPixel[1] > 0.2f;
        return hueDiff <= hueTolerance && saturationOk;
    }

    public FishingColor getFishingColor(BufferedImage lowerBar) {
        Point basePoint = this.application.sizes.getBarColorPoint();
        int redCount = 0;
        int greenCount = 0;
        int grayCount = 0;
        int blueCount = 0;
        ArrayList checkedPixels = new ArrayList();
        int yOffset = this.application.resolution.getHeight() - this.application.sizes.getDarkBarSize();
        for (int i = 0; i < 5; ++i) {
            int x = basePoint.x + i * this.application.scaler.rescaleX(50);
            int y = basePoint.y;
            int screenX = x;
            int screenY = yOffset + y;
            int pixel = lowerBar.getRGB(x, y);
            Color pixelColor = new Color(pixel, true);
            if (ScreenHelper.isColorMatchBrightnessInvariant(pixel, FishingColor.RED.getColor(), 0.05f)) {
                ++redCount;
                continue;
            }
            if (ScreenHelper.isColorMatchBrightnessInvariant(pixel, FishingColor.GREEN.getColor(), 0.05f)) {
                ++greenCount;
                continue;
            }
            if (ScreenHelper.isColorMatchBrightnessInvariant(pixel, FishingColor.GRAY.getColor(), 0.05f)) {
                ++grayCount;
                continue;
            }
            if (!ScreenHelper.isColorMatchBrightnessInvariant(pixel, FishingColor.BLUE.getColor(), 0.05f)) continue;
            ++blueCount;
        }
        if (redCount > 0) {
            return FishingColor.RED;
        }
        if (greenCount == 0) {
            return FishingColor.RED;
        }
        if (blueCount > 0 && grayCount > 0 && redCount == 0) {
            return FishingColor.RED;
        }
        if (greenCount >= blueCount && greenCount >= grayCount) {
            return FishingColor.GREEN;
        }
        if (blueCount >= greenCount && blueCount >= grayCount) {
            return FishingColor.BLUE;
        }
        if (grayCount > 0) {
            return FishingColor.GRAY;
        }
        return FishingColor.UNDEFINED;
    }

    public FishingState getFishingState(BufferedImage lowerBar) {
        Sizes.BarSizes barSizes = this.application.sizes.getBarSizes();
        BufferedImage subimage = lowerBar.getSubimage(barSizes.getXOffset(), lowerBar.getHeight() - barSizes.getYOffset() - barSizes.getYSize(), barSizes.getXSize(), barSizes.getYSize());
        Color color = new Color(189, 189, 189);
        int count = 0;
        boolean lineBreak = false;
        for (int y = 0; y < subimage.getHeight(); ++y) {
            for (int x = 0; x < subimage.getWidth(); ++x) {
                int currentRGB = subimage.getRGB(x, y);
                int red = currentRGB >> 16 & 0xFF;
                int green = currentRGB >> 8 & 0xFF;
                int blue = currentRGB & 0xFF;
                if (red < color.getRed() || green < color.getGreen() || blue < color.getBlue()) continue;
                if ((double)x < (double)subimage.getWidth() * 0.1) {
                    lineBreak = true;
                }
                ++count;
            }
        }
        if (count > 0 && !lineBreak) {
            return FishingState.ALMOST_CAUGHT;
        }
        if (lineBreak) {
            return FishingState.LINE_BREAK;
        }
        return FishingState.UNDEFINED;
    }

    private static boolean isColorMatch(int pixel, Color targetColor, int tolerance) {
        Color color = new Color(pixel, true);
        return Math.abs(color.getRed() - targetColor.getRed()) <= tolerance && Math.abs(color.getGreen() - targetColor.getGreen()) <= tolerance && Math.abs(color.getBlue() - targetColor.getBlue()) <= tolerance;
    }
}

