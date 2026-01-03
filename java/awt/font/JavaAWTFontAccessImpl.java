/*
 * Decompiled with CFR 0.152.
 */
package java.awt.font;

import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import jdk.internal.access.JavaAWTFontAccess;

class JavaAWTFontAccessImpl
implements JavaAWTFontAccess {
    JavaAWTFontAccessImpl() {
    }

    @Override
    public Object getTextAttributeConstant(String name) {
        switch (name) {
            case "RUN_DIRECTION": {
                return TextAttribute.RUN_DIRECTION;
            }
            case "NUMERIC_SHAPING": {
                return TextAttribute.NUMERIC_SHAPING;
            }
            case "BIDI_EMBEDDING": {
                return TextAttribute.BIDI_EMBEDDING;
            }
            case "RUN_DIRECTION_LTR": {
                return TextAttribute.RUN_DIRECTION_LTR;
            }
        }
        throw new AssertionError((Object)"Constant name is not recognized");
    }

    @Override
    public void shape(Object shaper, char[] text, int start, int count) {
        assert (shaper instanceof NumericShaper);
        ((NumericShaper)shaper).shape(text, start, count);
    }
}

