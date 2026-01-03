/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.logic.settings;

public enum SoundMode {
    SINGLE("Jednorazowy"),
    REPEATING("Powtarzaj\u0105cy"),
    REPEATING_AUTO("Powtarzaj\u0105cy auto");

    private final String displayName;

    private SoundMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String toString() {
        return this.displayName;
    }
}

