/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.settings;

import lombok.Generated;
import org.pbrands.settings.BaseSettings;

public class Settings
extends BaseSettings {
    private int fishingStartDelay;
    private int fishingStartDelayRandomizer;
    private int fishingBeginDelay;
    private int fishingBeginRandomizer;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Settings)) {
            return false;
        }
        Settings other = (Settings)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (this.getFishingStartDelay() != other.getFishingStartDelay()) {
            return false;
        }
        if (this.getFishingStartDelayRandomizer() != other.getFishingStartDelayRandomizer()) {
            return false;
        }
        if (this.getFishingBeginDelay() != other.getFishingBeginDelay()) {
            return false;
        }
        return this.getFishingBeginRandomizer() == other.getFishingBeginRandomizer();
    }

    @Override
    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof Settings;
    }

    @Override
    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        result = result * 59 + this.getFishingStartDelay();
        result = result * 59 + this.getFishingStartDelayRandomizer();
        result = result * 59 + this.getFishingBeginDelay();
        result = result * 59 + this.getFishingBeginRandomizer();
        return result;
    }

    @Generated
    public Settings() {
    }

    @Generated
    public int getFishingStartDelay() {
        return this.fishingStartDelay;
    }

    @Generated
    public int getFishingStartDelayRandomizer() {
        return this.fishingStartDelayRandomizer;
    }

    @Generated
    public int getFishingBeginDelay() {
        return this.fishingBeginDelay;
    }

    @Generated
    public int getFishingBeginRandomizer() {
        return this.fishingBeginRandomizer;
    }

    @Generated
    public void setFishingStartDelay(int fishingStartDelay) {
        this.fishingStartDelay = fishingStartDelay;
    }

    @Generated
    public void setFishingStartDelayRandomizer(int fishingStartDelayRandomizer) {
        this.fishingStartDelayRandomizer = fishingStartDelayRandomizer;
    }

    @Generated
    public void setFishingBeginDelay(int fishingBeginDelay) {
        this.fishingBeginDelay = fishingBeginDelay;
    }

    @Generated
    public void setFishingBeginRandomizer(int fishingBeginRandomizer) {
        this.fishingBeginRandomizer = fishingBeginRandomizer;
    }

    @Override
    @Generated
    public String toString() {
        return "Settings(fishingStartDelay=" + this.getFishingStartDelay() + ", fishingStartDelayRandomizer=" + this.getFishingStartDelayRandomizer() + ", fishingBeginDelay=" + this.getFishingBeginDelay() + ", fishingBeginRandomizer=" + this.getFishingBeginRandomizer() + ")";
    }
}

