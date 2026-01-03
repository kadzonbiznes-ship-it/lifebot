/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.logic.checks;

public enum CheckSound {
    ADMIN("Wykrycie administracji"),
    WARNING("Ostrze\u017cenie"),
    COAL_EMPTY("Pusta \u015bciana"),
    DIG_LEVEL_TOO_HIGH("Za wysoki lvl \u015bciany"),
    DIG_LEVEL_CHANGE("Zmiana lvl \u015bciany"),
    FULL_COAL("Pe\u0142ny ekwipunek");

    private final String name;

    private CheckSound(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}

