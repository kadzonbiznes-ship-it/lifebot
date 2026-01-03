/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.logic.settings;

public enum DetectionType {
    ADMIN("Wykrycie admina"),
    ADMIN_WARNING("Ostrze\u017cenie od admina"),
    FULL_INVENTORY("Pe\u0142ny ekwipunek"),
    PRIVATE_MESSAGE("Prywatna wiadomo\u015b\u0107"),
    MENTION("Wzmianka nicku"),
    COAL_EMPTY("Pusta \u015bciana"),
    DIG_LEVEL_TOO_HIGH("Za wysoki lvl \u015bciany"),
    DIG_LEVEL_CHANGE("Zmiana lvl \u015bciany");

    private final String displayName;

    private DetectionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String toString() {
        return this.displayName;
    }
}

