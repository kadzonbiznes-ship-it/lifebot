/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.text.CharacterIterator;

public class Segment
implements Cloneable,
CharacterIterator,
CharSequence {
    public char[] array;
    public int offset;
    public int count;
    boolean copy;
    private boolean partialReturn;
    private int pos;

    public Segment() {
        this(null, 0, 0);
    }

    public Segment(char[] array, int offset, int count) {
        this.array = array;
        this.offset = offset;
        this.count = count;
        this.partialReturn = false;
    }

    public void setPartialReturn(boolean p) {
        this.partialReturn = p;
    }

    public boolean isPartialReturn() {
        return this.partialReturn;
    }

    @Override
    public String toString() {
        if (this.array != null) {
            return new String(this.array, this.offset, this.count);
        }
        return "";
    }

    @Override
    public char first() {
        this.pos = this.offset;
        if (this.count != 0) {
            return this.array[this.pos];
        }
        return '\uffff';
    }

    @Override
    public char last() {
        this.pos = this.offset + this.count;
        if (this.count != 0) {
            --this.pos;
            return this.array[this.pos];
        }
        return '\uffff';
    }

    @Override
    public char current() {
        if (this.count != 0 && this.pos < this.offset + this.count) {
            return this.array[this.pos];
        }
        return '\uffff';
    }

    @Override
    public char next() {
        ++this.pos;
        int end = this.offset + this.count;
        if (this.pos >= end) {
            this.pos = end;
            return '\uffff';
        }
        return this.current();
    }

    @Override
    public char previous() {
        if (this.pos == this.offset) {
            return '\uffff';
        }
        --this.pos;
        return this.current();
    }

    @Override
    public char setIndex(int position) {
        int end = this.offset + this.count;
        if (position < this.offset || position > end) {
            throw new IllegalArgumentException("bad position: " + position);
        }
        this.pos = position;
        if (this.pos != end && this.count != 0) {
            return this.array[this.pos];
        }
        return '\uffff';
    }

    @Override
    public int getBeginIndex() {
        return this.offset;
    }

    @Override
    public int getEndIndex() {
        return this.offset + this.count;
    }

    @Override
    public int getIndex() {
        return this.pos;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= this.count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return this.array[this.offset + index];
    }

    @Override
    public int length() {
        return this.count;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > this.count) {
            throw new StringIndexOutOfBoundsException(end);
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException(end - start);
        }
        Segment segment = new Segment();
        segment.array = this.array;
        segment.offset = this.offset + start;
        segment.count = end - start;
        return segment;
    }

    @Override
    public Object clone() {
        Object o;
        try {
            o = super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            o = null;
        }
        return o;
    }
}

