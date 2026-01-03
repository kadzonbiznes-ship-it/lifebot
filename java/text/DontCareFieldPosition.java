/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.text.FieldPosition;
import java.text.Format;

class DontCareFieldPosition
extends FieldPosition {
    static final FieldPosition INSTANCE = new DontCareFieldPosition();
    private final Format.FieldDelegate noDelegate = new Format.FieldDelegate(this){

        @Override
        public void formatted(Format.Field attr, Object value, int start, int end, StringBuffer buffer) {
        }

        @Override
        public void formatted(int fieldID, Format.Field attr, Object value, int start, int end, StringBuffer buffer) {
        }
    };

    private DontCareFieldPosition() {
        super(0);
    }

    @Override
    Format.FieldDelegate getFieldDelegate() {
        return this.noDelegate;
    }
}

