/*
 * Decompiled with CFR 0.152.
 */
package java.time.format;

public enum TextStyle {
    FULL(2, 0),
    FULL_STANDALONE(32770, 0),
    SHORT(1, 1),
    SHORT_STANDALONE(32769, 1),
    NARROW(4, 1),
    NARROW_STANDALONE(32772, 1);

    private final int calendarStyle;
    private final int zoneNameStyleIndex;

    private TextStyle(int calendarStyle, int zoneNameStyleIndex) {
        this.calendarStyle = calendarStyle;
        this.zoneNameStyleIndex = zoneNameStyleIndex;
    }

    public boolean isStandalone() {
        return (this.ordinal() & 1) == 1;
    }

    public TextStyle asStandalone() {
        return TextStyle.values()[this.ordinal() | 1];
    }

    public TextStyle asNormal() {
        return TextStyle.values()[this.ordinal() & 0xFFFFFFFE];
    }

    int toCalendarStyle() {
        return this.calendarStyle;
    }

    int zoneNameStyleIndex() {
        return this.zoneNameStyleIndex;
    }
}

