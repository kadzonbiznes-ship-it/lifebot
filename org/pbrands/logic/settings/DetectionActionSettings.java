/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.logic.settings;

import lombok.Generated;
import org.pbrands.logic.settings.SoundMode;

public class DetectionActionSettings {
    private boolean showNotification = true;
    private boolean playSound = true;
    private SoundMode soundMode = SoundMode.SINGLE;
    private int repeatIntervalMs = 3000;
    private String selectedSound = "builtin:NOTIFICATION";
    private int volume = 100;
    private boolean useGlobalVolume = true;
    private boolean pauseBot = false;
    private transient long lastSoundPlayedAt = 0L;

    public DetectionActionSettings() {
    }

    public DetectionActionSettings(boolean showNotification, boolean playSound, boolean pauseBot, String selectedSound) {
        this.showNotification = showNotification;
        this.playSound = playSound;
        this.pauseBot = pauseBot;
        this.selectedSound = selectedSound;
    }

    public int getEffectiveVolume(int globalVolume) {
        return this.useGlobalVolume ? globalVolume : this.volume;
    }

    public boolean canPlaySound() {
        if (!this.playSound) {
            return false;
        }
        if (this.soundMode == SoundMode.SINGLE) {
            return this.lastSoundPlayedAt == 0L;
        }
        long now = System.currentTimeMillis();
        return now - this.lastSoundPlayedAt >= (long)this.repeatIntervalMs;
    }

    public void markSoundPlayed() {
        this.lastSoundPlayedAt = System.currentTimeMillis();
    }

    public void reset() {
        this.lastSoundPlayedAt = 0L;
    }

    public void copyFrom(DetectionActionSettings other) {
        if (other == null) {
            return;
        }
        this.showNotification = other.showNotification;
        this.playSound = other.playSound;
        this.soundMode = other.soundMode;
        this.repeatIntervalMs = other.repeatIntervalMs;
        this.selectedSound = other.selectedSound;
        this.volume = other.volume;
        this.useGlobalVolume = other.useGlobalVolume;
        this.pauseBot = other.pauseBot;
    }

    @Generated
    public boolean isShowNotification() {
        return this.showNotification;
    }

    @Generated
    public boolean isPlaySound() {
        return this.playSound;
    }

    @Generated
    public SoundMode getSoundMode() {
        return this.soundMode;
    }

    @Generated
    public int getRepeatIntervalMs() {
        return this.repeatIntervalMs;
    }

    @Generated
    public String getSelectedSound() {
        return this.selectedSound;
    }

    @Generated
    public int getVolume() {
        return this.volume;
    }

    @Generated
    public boolean isUseGlobalVolume() {
        return this.useGlobalVolume;
    }

    @Generated
    public boolean isPauseBot() {
        return this.pauseBot;
    }

    @Generated
    public long getLastSoundPlayedAt() {
        return this.lastSoundPlayedAt;
    }

    @Generated
    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    @Generated
    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    @Generated
    public void setSoundMode(SoundMode soundMode) {
        this.soundMode = soundMode;
    }

    @Generated
    public void setRepeatIntervalMs(int repeatIntervalMs) {
        this.repeatIntervalMs = repeatIntervalMs;
    }

    @Generated
    public void setSelectedSound(String selectedSound) {
        this.selectedSound = selectedSound;
    }

    @Generated
    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Generated
    public void setUseGlobalVolume(boolean useGlobalVolume) {
        this.useGlobalVolume = useGlobalVolume;
    }

    @Generated
    public void setPauseBot(boolean pauseBot) {
        this.pauseBot = pauseBot;
    }

    @Generated
    public void setLastSoundPlayedAt(long lastSoundPlayedAt) {
        this.lastSoundPlayedAt = lastSoundPlayedAt;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DetectionActionSettings)) {
            return false;
        }
        DetectionActionSettings other = (DetectionActionSettings)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isShowNotification() != other.isShowNotification()) {
            return false;
        }
        if (this.isPlaySound() != other.isPlaySound()) {
            return false;
        }
        if (this.getRepeatIntervalMs() != other.getRepeatIntervalMs()) {
            return false;
        }
        if (this.getVolume() != other.getVolume()) {
            return false;
        }
        if (this.isUseGlobalVolume() != other.isUseGlobalVolume()) {
            return false;
        }
        if (this.isPauseBot() != other.isPauseBot()) {
            return false;
        }
        SoundMode this$soundMode = this.getSoundMode();
        SoundMode other$soundMode = other.getSoundMode();
        if (this$soundMode == null ? other$soundMode != null : !((Object)((Object)this$soundMode)).equals((Object)other$soundMode)) {
            return false;
        }
        String this$selectedSound = this.getSelectedSound();
        String other$selectedSound = other.getSelectedSound();
        return !(this$selectedSound == null ? other$selectedSound != null : !this$selectedSound.equals(other$selectedSound));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof DetectionActionSettings;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isShowNotification() ? 79 : 97);
        result = result * 59 + (this.isPlaySound() ? 79 : 97);
        result = result * 59 + this.getRepeatIntervalMs();
        result = result * 59 + this.getVolume();
        result = result * 59 + (this.isUseGlobalVolume() ? 79 : 97);
        result = result * 59 + (this.isPauseBot() ? 79 : 97);
        SoundMode $soundMode = this.getSoundMode();
        result = result * 59 + ($soundMode == null ? 43 : ((Object)((Object)$soundMode)).hashCode());
        String $selectedSound = this.getSelectedSound();
        result = result * 59 + ($selectedSound == null ? 43 : $selectedSound.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "DetectionActionSettings(showNotification=" + this.isShowNotification() + ", playSound=" + this.isPlaySound() + ", soundMode=" + String.valueOf((Object)this.getSoundMode()) + ", repeatIntervalMs=" + this.getRepeatIntervalMs() + ", selectedSound=" + this.getSelectedSound() + ", volume=" + this.getVolume() + ", useGlobalVolume=" + this.isUseGlobalVolume() + ", pauseBot=" + this.isPauseBot() + ", lastSoundPlayedAt=" + this.getLastSoundPlayedAt() + ")";
    }
}

