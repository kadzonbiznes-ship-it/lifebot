/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.logic;

import java.awt.Point;
import lombok.Generated;
import org.pbrands.model.Resolution;
import org.pbrands.util.ResolutionScaler;

public class Sizes {
    private final ResolutionScaler originScaler;
    private final Resolution resolution;
    private final int originBarWidth = 818;
    private final int hdpBarWidth = 533;
    private final int originDarkBarSize = 240;
    private final int hdpDarkBarSize = 157;
    private final Point originBarColorPoint = new Point(880, 135);
    private final Point hdpBarColorPoint = new Point(540, 90);
    private final int originStartFishingLineYOffset = 119;
    private final int hdpStartFishingLineYOffset = 79;
    private final int originStartFishingLineXOffset = 81;
    private final int hdpStartFishingLineXOffset = 53;
    private final BarSizes barSizes = new BarSizes();

    public int getDarkBarSize() {
        if (this.resolution.isAtLeastFHD()) {
            return this.originScaler.rescaleSize(240);
        }
        return 157;
    }

    public int getBarStartXOffset() {
        if (this.resolution.isAtLeastFHD()) {
            return (this.resolution.getWidth() - this.originScaler.rescaleX(818)) / 2;
        }
        return (this.resolution.getWidth() - 533) / 2;
    }

    public Point getBarColorPoint() {
        int barStartXOffset = this.getBarStartXOffset();
        if (this.resolution.isAtLeastFHD()) {
            return new Point(barStartXOffset + 20, this.originScaler.rescaleY(this.originBarColorPoint.y));
        }
        return new Point(barStartXOffset + 12, this.hdpBarColorPoint.y);
    }

    public int getStartFishingLineYOffset() {
        if (this.resolution.isAtLeastFHD()) {
            return this.originScaler.rescaleY(119);
        }
        return 79;
    }

    public int getStartFishingLineXOffset() {
        if (this.resolution.isAtLeastFHD()) {
            return this.originScaler.rescaleX(81);
        }
        return 53;
    }

    @Generated
    public Sizes(ResolutionScaler originScaler, Resolution resolution) {
        this.originScaler = originScaler;
        this.resolution = resolution;
    }

    @Generated
    public BarSizes getBarSizes() {
        return this.barSizes;
    }

    public class BarSizes {
        private final int originXOffset = 1120;
        private final int hdpXOffset = 694;
        private final int originXSize = 320;
        private final int hdpXSize = 213;
        private final int originYOffset = 38;
        private final int hdpYOffset = 24;
        private final int originYSize = 23;
        private final int hdpYSize = 14;

        public int getXOffset() {
            if (Sizes.this.resolution.isAtLeastFHD()) {
                return Sizes.this.originScaler.rescaleSize(1120);
            }
            return 694;
        }

        public int getXSize() {
            if (Sizes.this.resolution.isAtLeastFHD()) {
                return Sizes.this.originScaler.rescaleSize(320);
            }
            return 213;
        }

        public int getYOffset() {
            if (Sizes.this.resolution.isAtLeastFHD()) {
                return Sizes.this.originScaler.rescaleSize(38);
            }
            return 24;
        }

        public int getYSize() {
            if (Sizes.this.resolution.isAtLeastFHD()) {
                return Sizes.this.originScaler.rescaleSize(23);
            }
            return 14;
        }
    }
}

