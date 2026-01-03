/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.Arrays;
import java.util.Objects;
import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;

public final class StringJoiner {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private final String prefix;
    private final String delimiter;
    private final String suffix;
    private String[] elts;
    private int size;
    private int len;
    private String emptyValue;
    private static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();

    public StringJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

    public StringJoiner(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        Objects.requireNonNull(prefix, "The prefix must not be null");
        Objects.requireNonNull(delimiter, "The delimiter must not be null");
        Objects.requireNonNull(suffix, "The suffix must not be null");
        this.prefix = prefix.toString();
        this.delimiter = delimiter.toString();
        this.suffix = suffix.toString();
        this.checkAddLength(0, 0);
    }

    public StringJoiner setEmptyValue(CharSequence emptyValue) {
        this.emptyValue = Objects.requireNonNull(emptyValue, "The empty value must not be null").toString();
        return this;
    }

    public String toString() {
        int size = this.size;
        String[] elts = this.elts;
        if (size == 0) {
            if (this.emptyValue != null) {
                return this.emptyValue;
            }
            elts = EMPTY_STRING_ARRAY;
        }
        return JLA.join(this.prefix, this.suffix, this.delimiter, elts, size);
    }

    public StringJoiner add(CharSequence newElement) {
        String elt = String.valueOf(newElement);
        if (this.elts == null) {
            this.elts = new String[8];
        } else {
            if (this.size == this.elts.length) {
                this.elts = Arrays.copyOf(this.elts, 2 * this.size);
            }
            this.len = this.checkAddLength(this.len, this.delimiter.length());
        }
        this.len = this.checkAddLength(this.len, elt.length());
        this.elts[this.size++] = elt;
        return this;
    }

    private int checkAddLength(int oldLen, int inc) {
        long newLen = (long)oldLen + (long)inc;
        long tmpLen = newLen + (long)this.prefix.length() + (long)this.suffix.length();
        if (tmpLen != (long)((int)tmpLen)) {
            throw new OutOfMemoryError("Requested array size exceeds VM limit");
        }
        return (int)newLen;
    }

    public StringJoiner merge(StringJoiner other) {
        Objects.requireNonNull(other);
        if (other.size == 0) {
            return this;
        }
        other.compactElts();
        return this.add(other.elts[0]);
    }

    private void compactElts() {
        int sz = this.size;
        if (sz > 1) {
            this.elts[0] = JLA.join("", "", this.delimiter, this.elts, sz);
            Arrays.fill(this.elts, 1, sz, null);
            this.size = 1;
        }
    }

    public int length() {
        return this.size == 0 && this.emptyValue != null ? this.emptyValue.length() : this.len + this.prefix.length() + this.suffix.length();
    }
}

