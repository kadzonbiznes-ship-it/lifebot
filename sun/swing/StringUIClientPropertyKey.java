/*
 * Decompiled with CFR 0.152.
 */
package sun.swing;

import javax.swing.UIClientPropertyKey;

public class StringUIClientPropertyKey
implements UIClientPropertyKey {
    private final String key;

    public StringUIClientPropertyKey(String key) {
        this.key = key;
    }

    public String toString() {
        return this.key;
    }
}

