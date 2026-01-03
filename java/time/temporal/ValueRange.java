/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.temporal.TemporalField;

public final class ValueRange
implements Serializable {
    private static final long serialVersionUID = -7317881728594519368L;
    private final long minSmallest;
    private final long minLargest;
    private final long maxSmallest;
    private final long maxLargest;

    public static ValueRange of(long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value must be less than maximum value");
        }
        return new ValueRange(min, min, max, max);
    }

    public static ValueRange of(long min, long maxSmallest, long maxLargest) {
        if (min > maxSmallest) {
            throw new IllegalArgumentException("Minimum value must be less than smallest maximum value");
        }
        return ValueRange.of(min, min, maxSmallest, maxLargest);
    }

    public static ValueRange of(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
        if (minSmallest > minLargest) {
            throw new IllegalArgumentException("Smallest minimum value must be less than largest minimum value");
        }
        if (maxSmallest > maxLargest) {
            throw new IllegalArgumentException("Smallest maximum value must be less than largest maximum value");
        }
        if (minLargest > maxLargest) {
            throw new IllegalArgumentException("Largest minimum value must be less than largest maximum value");
        }
        if (minSmallest > maxSmallest) {
            throw new IllegalArgumentException("Smallest minimum value must be less than smallest maximum value");
        }
        return new ValueRange(minSmallest, minLargest, maxSmallest, maxLargest);
    }

    private ValueRange(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
        this.minSmallest = minSmallest;
        this.minLargest = minLargest;
        this.maxSmallest = maxSmallest;
        this.maxLargest = maxLargest;
    }

    public boolean isFixed() {
        return this.minSmallest == this.minLargest && this.maxSmallest == this.maxLargest;
    }

    public long getMinimum() {
        return this.minSmallest;
    }

    public long getLargestMinimum() {
        return this.minLargest;
    }

    public long getSmallestMaximum() {
        return this.maxSmallest;
    }

    public long getMaximum() {
        return this.maxLargest;
    }

    public boolean isIntValue() {
        return this.getMinimum() >= Integer.MIN_VALUE && this.getMaximum() <= Integer.MAX_VALUE;
    }

    public boolean isValidValue(long value) {
        return value >= this.getMinimum() && value <= this.getMaximum();
    }

    public boolean isValidIntValue(long value) {
        return this.isIntValue() && this.isValidValue(value);
    }

    public long checkValidValue(long value, TemporalField field) {
        if (!this.isValidValue(value)) {
            throw new DateTimeException(this.genInvalidFieldMessage(field, value));
        }
        return value;
    }

    public int checkValidIntValue(long value, TemporalField field) {
        if (!this.isValidIntValue(value)) {
            throw new DateTimeException(this.genInvalidFieldMessage(field, value));
        }
        return (int)value;
    }

    private String genInvalidFieldMessage(TemporalField field, long value) {
        if (field != null) {
            return "Invalid value for " + field + " (valid values " + this + "): " + value;
        }
        return "Invalid value (valid values " + this + "): " + value;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException, InvalidObjectException {
        s.defaultReadObject();
        if (this.minSmallest > this.minLargest) {
            throw new InvalidObjectException("Smallest minimum value must be less than largest minimum value");
        }
        if (this.maxSmallest > this.maxLargest) {
            throw new InvalidObjectException("Smallest maximum value must be less than largest maximum value");
        }
        if (this.minLargest > this.maxLargest) {
            throw new InvalidObjectException("Minimum value must be less than maximum value");
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ValueRange)) return false;
        ValueRange other = (ValueRange)obj;
        if (this.minSmallest != other.minSmallest) return false;
        if (this.minLargest != other.minLargest) return false;
        if (this.maxSmallest != other.maxSmallest) return false;
        if (this.maxLargest != other.maxLargest) return false;
        return true;
    }

    public int hashCode() {
        long hash = this.minSmallest + (this.minLargest << 16) + (this.minLargest >> 48) + (this.maxSmallest << 32) + (this.maxSmallest >> 32) + (this.maxLargest << 48) + (this.maxLargest >> 16);
        return Long.hashCode(hash);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.minSmallest);
        if (this.minSmallest != this.minLargest) {
            buf.append('/').append(this.minLargest);
        }
        buf.append(" - ").append(this.maxSmallest);
        if (this.maxSmallest != this.maxLargest) {
            buf.append('/').append(this.maxLargest);
        }
        return buf.toString();
    }
}

