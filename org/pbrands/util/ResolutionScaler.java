/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.util;

import java.awt.Point;
import java.awt.Rectangle;
import lombok.Generated;
import org.pbrands.model.Resolution;

public class ResolutionScaler {
    public static final int DEFAULT_BASE_WIDTH = 2560;
    public static final int DEFAULT_BASE_HEIGHT = 1440;
    private static final double ASPECT_RATIO = 1.7777777777777777;
    private final double widthRatio;
    private final double heightRatio;
    private final double sizeRatio;
    private boolean ui;
    private final Resolution resolution;

    public ResolutionScaler(Resolution resolution) {
        this(2560, 1440, resolution.getWidth(), resolution.getHeight(), false);
    }

    public ResolutionScaler(int baseWidth, int baseHeight, int targetWidth, int targetHeight, boolean ui) {
        this.resolution = new Resolution(targetWidth, targetHeight);
        this.ui = ui;
        this.widthRatio = (double)targetWidth / (double)baseWidth;
        this.heightRatio = (double)targetHeight / (double)baseHeight;
        this.sizeRatio = Math.sqrt(this.widthRatio * this.widthRatio + this.heightRatio * this.heightRatio) / Math.sqrt(2.0);
    }

    public boolean isWQHD() {
        return this.resolution.getWidth() == 2560 && this.resolution.getHeight() == 1440;
    }

    public static boolean isResolutionSupported(int width, int height) {
        double ratio = (double)width / (double)height;
        return Math.abs(ratio - 1.7777777777777777) < 0.01;
    }

    public static String getSupportedResolutions() {
        return "Supports resolutions with a 16:9 aspect ratio";
    }

    public Rectangle rescale(Rectangle original) {
        if (this.ui) {
            return original;
        }
        int newX = (int)((double)original.x * this.widthRatio);
        int newY = (int)((double)original.y * this.heightRatio);
        int newWidth = (int)((double)original.width * this.widthRatio);
        int newHeight = (int)((double)original.height * this.heightRatio);
        return new Rectangle(newX, newY, newWidth, newHeight);
    }

    public Point rescale(Point point) {
        if (this.ui) {
            return point;
        }
        int newX = (int)((double)point.x * this.widthRatio);
        int newY = (int)((double)point.y * this.heightRatio);
        return new Point(newX, newY);
    }

    public Point rescalePoint(int x, int y) {
        if (this.ui) {
            return new Point(x, y);
        }
        int newX = (int)((double)x * this.widthRatio);
        int newY = (int)((double)y * this.heightRatio);
        return new Point(newX, newY);
    }

    public int rescaleX(int x) {
        if (this.ui) {
            return x;
        }
        return (int)((double)x * this.widthRatio);
    }

    public int rescaleY(int y) {
        if (this.ui) {
            return y;
        }
        return (int)((double)y * this.heightRatio);
    }

    public int rescaleSize(int originalSize) {
        if (this.ui) {
            return originalSize;
        }
        return (int)((double)originalSize * this.sizeRatio);
    }

    public int getWidth() {
        return this.resolution.getWidth();
    }

    public int getHeight() {
        return this.resolution.getHeight();
    }

    @Generated
    public void setUi(boolean ui) {
        this.ui = ui;
    }

    @Generated
    public Resolution getResolution() {
        return this.resolution;
    }
}

