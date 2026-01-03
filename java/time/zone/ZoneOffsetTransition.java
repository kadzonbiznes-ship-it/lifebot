/*
 * Decompiled with CFR 0.152.
 */
package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.Ser;
import java.util.List;
import java.util.Objects;

public final class ZoneOffsetTransition
implements Comparable<ZoneOffsetTransition>,
Serializable {
    private static final long serialVersionUID = -6946044323557704546L;
    private final long epochSecond;
    private final LocalDateTime transition;
    private final ZoneOffset offsetBefore;
    private final ZoneOffset offsetAfter;

    public static ZoneOffsetTransition of(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        Objects.requireNonNull(transition, "transition");
        Objects.requireNonNull(offsetBefore, "offsetBefore");
        Objects.requireNonNull(offsetAfter, "offsetAfter");
        if (offsetBefore.equals(offsetAfter)) {
            throw new IllegalArgumentException("Offsets must not be equal");
        }
        if (transition.getNano() != 0) {
            throw new IllegalArgumentException("Nano-of-second must be zero");
        }
        return new ZoneOffsetTransition(transition, offsetBefore, offsetAfter);
    }

    ZoneOffsetTransition(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        assert (transition.getNano() == 0);
        this.epochSecond = transition.toEpochSecond(offsetBefore);
        this.transition = transition;
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    ZoneOffsetTransition(long epochSecond, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        this.epochSecond = epochSecond;
        this.transition = LocalDateTime.ofEpochSecond(epochSecond, 0, offsetBefore);
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser(2, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        Ser.writeEpochSec(this.epochSecond, out);
        Ser.writeOffset(this.offsetBefore, out);
        Ser.writeOffset(this.offsetAfter, out);
    }

    static ZoneOffsetTransition readExternal(DataInput in) throws IOException {
        ZoneOffset after;
        long epochSecond = Ser.readEpochSec(in);
        ZoneOffset before = Ser.readOffset(in);
        if (before.equals(after = Ser.readOffset(in))) {
            throw new IllegalArgumentException("Offsets must not be equal");
        }
        return new ZoneOffsetTransition(epochSecond, before, after);
    }

    public Instant getInstant() {
        return Instant.ofEpochSecond(this.epochSecond);
    }

    public long toEpochSecond() {
        return this.epochSecond;
    }

    public LocalDateTime getDateTimeBefore() {
        return this.transition;
    }

    public LocalDateTime getDateTimeAfter() {
        return this.transition.plusSeconds(this.getDurationSeconds());
    }

    public ZoneOffset getOffsetBefore() {
        return this.offsetBefore;
    }

    public ZoneOffset getOffsetAfter() {
        return this.offsetAfter;
    }

    public Duration getDuration() {
        return Duration.ofSeconds(this.getDurationSeconds());
    }

    private int getDurationSeconds() {
        return this.getOffsetAfter().getTotalSeconds() - this.getOffsetBefore().getTotalSeconds();
    }

    public boolean isGap() {
        return this.getOffsetAfter().getTotalSeconds() > this.getOffsetBefore().getTotalSeconds();
    }

    public boolean isOverlap() {
        return this.getOffsetAfter().getTotalSeconds() < this.getOffsetBefore().getTotalSeconds();
    }

    public boolean isValidOffset(ZoneOffset offset) {
        return this.isGap() ? false : this.getOffsetBefore().equals(offset) || this.getOffsetAfter().equals(offset);
    }

    List<ZoneOffset> getValidOffsets() {
        if (this.isGap()) {
            return List.of();
        }
        return List.of(this.getOffsetBefore(), this.getOffsetAfter());
    }

    @Override
    public int compareTo(ZoneOffsetTransition otherTransition) {
        return Long.compare(this.epochSecond, otherTransition.epochSecond);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ZoneOffsetTransition)) return false;
        ZoneOffsetTransition d = (ZoneOffsetTransition)other;
        if (this.epochSecond != d.epochSecond) return false;
        if (!this.offsetBefore.equals(d.offsetBefore)) return false;
        if (!this.offsetAfter.equals(d.offsetAfter)) return false;
        return true;
    }

    public int hashCode() {
        return this.transition.hashCode() ^ this.offsetBefore.hashCode() ^ Integer.rotateLeft(this.offsetAfter.hashCode(), 16);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Transition[").append(this.isGap() ? "Gap" : "Overlap").append(" at ").append(this.transition).append(this.offsetBefore).append(" to ").append(this.offsetAfter).append(']');
        return buf.toString();
    }
}

