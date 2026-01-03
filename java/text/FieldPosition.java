/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.text.Format;

public class FieldPosition {
    int field = 0;
    int endIndex = 0;
    int beginIndex = 0;
    private Format.Field attribute;

    public FieldPosition(int field) {
        this.field = field;
    }

    public FieldPosition(Format.Field attribute) {
        this(attribute, -1);
    }

    public FieldPosition(Format.Field attribute, int fieldID) {
        this.attribute = attribute;
        this.field = fieldID;
    }

    public Format.Field getFieldAttribute() {
        return this.attribute;
    }

    public int getField() {
        return this.field;
    }

    public int getBeginIndex() {
        return this.beginIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public void setBeginIndex(int bi) {
        this.beginIndex = bi;
    }

    public void setEndIndex(int ei) {
        this.endIndex = ei;
    }

    Format.FieldDelegate getFieldDelegate() {
        return new Delegate();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FieldPosition)) {
            return false;
        }
        FieldPosition other = (FieldPosition)obj;
        if (this.attribute == null ? other.attribute != null : !this.attribute.equals(other.attribute)) {
            return false;
        }
        return this.beginIndex == other.beginIndex && this.endIndex == other.endIndex && this.field == other.field;
    }

    public int hashCode() {
        return this.field << 24 | this.beginIndex << 16 | this.endIndex;
    }

    public String toString() {
        return this.getClass().getName() + "[field=" + this.field + ",attribute=" + this.attribute + ",beginIndex=" + this.beginIndex + ",endIndex=" + this.endIndex + ']';
    }

    private boolean matchesField(Format.Field attribute) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return false;
    }

    private boolean matchesField(Format.Field attribute, int field) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return field == this.field;
    }

    private class Delegate
    implements Format.FieldDelegate {
        private boolean encounteredField;

        private Delegate() {
        }

        @Override
        public void formatted(Format.Field attr, Object value, int start, int end, StringBuffer buffer) {
            if (!this.encounteredField && FieldPosition.this.matchesField(attr)) {
                FieldPosition.this.setBeginIndex(start);
                FieldPosition.this.setEndIndex(end);
                this.encounteredField = start != end;
            }
        }

        @Override
        public void formatted(int fieldID, Format.Field attr, Object value, int start, int end, StringBuffer buffer) {
            if (!this.encounteredField && FieldPosition.this.matchesField(attr, fieldID)) {
                FieldPosition.this.setBeginIndex(start);
                FieldPosition.this.setEndIndex(end);
                this.encounteredField = start != end;
            }
        }
    }
}

