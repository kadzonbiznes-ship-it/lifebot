/*
 * Decompiled with CFR 0.152.
 */
package java.time.format;

public enum SignStyle {
    NORMAL,
    ALWAYS,
    NEVER,
    NOT_NEGATIVE,
    EXCEEDS_PAD;


    boolean parse(boolean positive, boolean strict, boolean fixedWidth) {
        return switch (this.ordinal()) {
            case 0 -> {
                if (!positive || !strict) {
                    yield true;
                }
                yield false;
            }
            case 1, 4 -> true;
            default -> !strict && !fixedWidth;
        };
    }
}

