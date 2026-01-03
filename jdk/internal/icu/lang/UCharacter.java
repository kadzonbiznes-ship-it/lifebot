/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.lang;

import jdk.internal.icu.impl.UBiDiProps;
import jdk.internal.icu.impl.UCharacterProperty;
import jdk.internal.icu.text.Normalizer2;
import jdk.internal.icu.text.UTF16;
import jdk.internal.icu.util.VersionInfo;

public final class UCharacter {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 0x10FFFF;
    public static final byte NON_SPACING_MARK = 6;
    public static final byte ENCLOSING_MARK = 7;
    public static final byte COMBINING_SPACING_MARK = 8;
    public static final byte CHAR_CATEGORY_COUNT = 30;
    public static final int RIGHT_TO_LEFT = 1;
    public static final int RIGHT_TO_LEFT_ARABIC = 13;

    public static int digit(int ch, int radix) {
        if (2 <= radix && radix <= 36) {
            int value = UCharacter.digit(ch);
            if (value < 0) {
                value = UCharacterProperty.getEuropeanDigit(ch);
            }
            return value < radix ? value : -1;
        }
        return -1;
    }

    public static int digit(int ch) {
        return UCharacterProperty.INSTANCE.digit(ch);
    }

    public static int getType(int ch) {
        return UCharacterProperty.INSTANCE.getType(ch);
    }

    public static int getDirection(int ch) {
        return UBiDiProps.INSTANCE.getClass(ch);
    }

    public static int getMirror(int ch) {
        return UBiDiProps.INSTANCE.getMirror(ch);
    }

    public static int getBidiPairedBracket(int c) {
        return UBiDiProps.INSTANCE.getPairedBracket(c);
    }

    public static int getCombiningClass(int ch) {
        return Normalizer2.getNFDInstance().getCombiningClass(ch);
    }

    public static VersionInfo getUnicodeVersion() {
        return UCharacterProperty.INSTANCE.m_unicodeVersion_;
    }

    public static int getCodePoint(char lead, char trail) {
        if (UTF16.isLeadSurrogate(lead) && UTF16.isTrailSurrogate(trail)) {
            return UCharacterProperty.getRawSupplementary(lead, trail);
        }
        throw new IllegalArgumentException("Illegal surrogate characters");
    }

    public static VersionInfo getAge(int ch) {
        if (ch < 0 || ch > 0x10FFFF) {
            throw new IllegalArgumentException("Codepoint out of bounds");
        }
        return UCharacterProperty.INSTANCE.getAge(ch);
    }

    public static int getIntPropertyValue(int ch, int type) {
        return UCharacterProperty.INSTANCE.getIntPropertyValue(ch, type);
    }

    private UCharacter() {
    }

    public static interface HangulSyllableType {
        public static final int NOT_APPLICABLE = 0;
        public static final int LEADING_JAMO = 1;
        public static final int VOWEL_JAMO = 2;
        public static final int TRAILING_JAMO = 3;
        public static final int LV_SYLLABLE = 4;
        public static final int LVT_SYLLABLE = 5;
        public static final int COUNT = 6;
    }

    public static interface NumericType {
        public static final int NONE = 0;
        public static final int DECIMAL = 1;
        public static final int DIGIT = 2;
        public static final int NUMERIC = 3;
        public static final int COUNT = 4;
    }

    public static interface JoiningGroup {
        public static final int NO_JOINING_GROUP = 0;
    }
}

