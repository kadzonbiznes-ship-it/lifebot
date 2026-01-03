/*
 * Decompiled with CFR 0.152.
 */
package java.math;

public enum RoundingMode {
    UP(0),
    DOWN(1),
    CEILING(2),
    FLOOR(3),
    HALF_UP(4),
    HALF_DOWN(5),
    HALF_EVEN(6),
    UNNECESSARY(7);

    final int oldMode;

    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }

    public static RoundingMode valueOf(int rm) {
        return switch (rm) {
            case 0 -> UP;
            case 1 -> DOWN;
            case 2 -> CEILING;
            case 3 -> FLOOR;
            case 4 -> HALF_UP;
            case 5 -> HALF_DOWN;
            case 6 -> HALF_EVEN;
            case 7 -> UNNECESSARY;
            default -> throw new IllegalArgumentException("argument out of range");
        };
    }
}

