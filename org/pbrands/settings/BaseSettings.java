/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.settings;

import com.google.gson.GsonBuilder;
import lombok.Generated;

public class BaseSettings {
    private int ping;
    private int delay;
    private int randomizer;
    private boolean humanizer;
    private int overlayKeyCode = 3666;
    private int toggleKeyCode = 3663;

    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    @Generated
    public BaseSettings() {
    }

    @Generated
    public int getPing() {
        return this.ping;
    }

    @Generated
    public int getDelay() {
        return this.delay;
    }

    @Generated
    public int getRandomizer() {
        return this.randomizer;
    }

    @Generated
    public boolean isHumanizer() {
        return this.humanizer;
    }

    @Generated
    public int getOverlayKeyCode() {
        return this.overlayKeyCode;
    }

    @Generated
    public int getToggleKeyCode() {
        return this.toggleKeyCode;
    }

    @Generated
    public void setPing(int ping) {
        this.ping = ping;
    }

    @Generated
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Generated
    public void setRandomizer(int randomizer) {
        this.randomizer = randomizer;
    }

    @Generated
    public void setHumanizer(boolean humanizer) {
        this.humanizer = humanizer;
    }

    @Generated
    public void setOverlayKeyCode(int overlayKeyCode) {
        this.overlayKeyCode = overlayKeyCode;
    }

    @Generated
    public void setToggleKeyCode(int toggleKeyCode) {
        this.toggleKeyCode = toggleKeyCode;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BaseSettings)) {
            return false;
        }
        BaseSettings other = (BaseSettings)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getPing() != other.getPing()) {
            return false;
        }
        if (this.getDelay() != other.getDelay()) {
            return false;
        }
        if (this.getRandomizer() != other.getRandomizer()) {
            return false;
        }
        if (this.isHumanizer() != other.isHumanizer()) {
            return false;
        }
        if (this.getOverlayKeyCode() != other.getOverlayKeyCode()) {
            return false;
        }
        return this.getToggleKeyCode() == other.getToggleKeyCode();
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof BaseSettings;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getPing();
        result = result * 59 + this.getDelay();
        result = result * 59 + this.getRandomizer();
        result = result * 59 + (this.isHumanizer() ? 79 : 97);
        result = result * 59 + this.getOverlayKeyCode();
        result = result * 59 + this.getToggleKeyCode();
        return result;
    }
}

