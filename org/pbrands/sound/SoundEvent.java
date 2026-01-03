/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.sound;

import lombok.Generated;

public enum SoundEvent {
    ADMIN_DETECTED("Wykryto admina", "Dzwiek gdy wykryty zostanie administrator"),
    FULL_INVENTORY("Pelny ekwipunek", "Dzwiek gdy ekwipunek sie zapelni"),
    PRIVATE_MESSAGE("Prywatna wiadomosc", "Dzwiek gdy otrzymasz PM"),
    MENTION("Wzmianka", "Dzwiek gdy ktos wspomni Twoj nick"),
    AFK_WARNING("Ostrzezenie AFK", "Dzwiek przy ostrzezeniu o AFK"),
    BOT_STARTED("Bot uruchomiony", "Dzwiek przy starcie bota"),
    BOT_STOPPED("Bot zatrzymany", "Dzwiek przy zatrzymaniu bota"),
    ERROR("Blad", "Dzwiek przy bledzie");

    private final String displayName;
    private final String description;

    private SoundEvent(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    @Generated
    public String getDisplayName() {
        return this.displayName;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }
}

