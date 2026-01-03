/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.sound;

import lombok.Generated;

public enum Sound {
    NOTIFICATION("Default", "notification.wav", true),
    NOTIFICATION_DIGITAL_STRUM("Notification Digital Strum", "notification_digital_strum.wav", true),
    SCREENSHOT("Screenshot", "screenshot.wav", false),
    HELLO_BELLS("Hello Bells", "hello_bells.wav", true),
    CARTOON_GAME("Cartoon Game", "cartoon_game.wav", true),
    SWIPE_WOOSH_DING_BETACUT("Swipe Woosh Ding Betacut", "swipe_woosh_ding_betacut.wav", true),
    PHONE_NOTIFICATION_BELL("Phone Notification Bell", "phone_notification_bell.wav", true),
    POP_BOTTLE_OPENING("Pop Bottle Opening", "pop_bottle_opening.wav", false),
    NUCLEAR_ALARM("Nuclear Alarm", "nulcear_alarm.wav", true),
    BAZA_AVAST("Baza", "baza.wav", true),
    TENNIS_BALL_HIT("Tennis Ball Hit", "tennis_ball_hit.wav", false),
    WIN_SOUND("Win Sound", "win_sound.wav", false),
    PONG_FAILURE("Pong Failure", "pong_failure.wav", false),
    SNAKE_FAILED("Snake Failed", "snake_failed.wav", false),
    SELECT_MENU("Select Menu", "select_menu.wav", false);

    private final String name;
    private final String filename;
    private final boolean userSelectable;

    private Sound(String name, String filename, boolean userSelectable) {
        this.name = name;
        this.filename = filename;
        this.userSelectable = userSelectable;
    }

    public String toString() {
        return this.name;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getFilename() {
        return this.filename;
    }

    @Generated
    public boolean isUserSelectable() {
        return this.userSelectable;
    }
}

