/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.MissingResourceException;
import jdk.internal.icu.impl.ICUBinary;
import jdk.internal.icu.impl.Trie2;
import jdk.internal.icu.impl.Trie2_16;
import jdk.internal.icu.impl.UBiDiProps;
import jdk.internal.icu.text.UnicodeSet;
import jdk.internal.icu.util.VersionInfo;

public final class UCharacterProperty {
    public static final UCharacterProperty INSTANCE;
    public Trie2_16 m_trie_;
    public VersionInfo m_unicodeVersion_;
    public static final int TYPE_MASK = 31;
    public static final int SRC_CHAR = 1;
    public static final int SRC_PROPSVEC = 2;
    public static final int SRC_BIDI = 5;
    public static final int SRC_NFC = 8;
    public static final int SRC_NFKC = 9;
    private static final int[] gcbToHst;
    private IntProperty intProp = new BiDiIntProperty(this){

        @Override
        int getValue(int c) {
            return UBiDiProps.INSTANCE.getPairedBracketType(c);
        }
    };
    Trie2_16 m_additionalTrie_;
    int[] m_additionalVectors_;
    int m_additionalColumnsCount_;
    int m_maxBlockScriptValue_;
    int m_maxJTGValue_;
    public char[] m_scriptExtensions_;
    private static final String DATA_FILE_NAME_ = "/jdk/internal/icu/impl/data/icudt72b/uprops.icu";
    private static final int LEAD_SURROGATE_SHIFT_ = 10;
    private static final int SURROGATE_OFFSET_ = -56613888;
    private static final int NUMERIC_TYPE_VALUE_SHIFT_ = 6;
    private static final int NTV_NONE_ = 0;
    private static final int NTV_DECIMAL_START_ = 1;
    private static final int NTV_DIGIT_START_ = 11;
    private static final int NTV_NUMERIC_START_ = 21;
    public static final int SCRIPT_X_MASK = 0xF000FF;
    public static final int SCRIPT_HIGH_MASK = 0x300000;
    public static final int SCRIPT_HIGH_SHIFT = 12;
    public static final int MAX_SCRIPT = 1023;
    private static final int EAST_ASIAN_MASK_ = 917504;
    private static final int EAST_ASIAN_SHIFT_ = 17;
    private static final int BLOCK_MASK_ = 130816;
    private static final int BLOCK_SHIFT_ = 8;
    public static final int SCRIPT_LOW_MASK = 255;
    private static final int WHITE_SPACE_PROPERTY_ = 0;
    private static final int DASH_PROPERTY_ = 1;
    private static final int HYPHEN_PROPERTY_ = 2;
    private static final int QUOTATION_MARK_PROPERTY_ = 3;
    private static final int TERMINAL_PUNCTUATION_PROPERTY_ = 4;
    private static final int MATH_PROPERTY_ = 5;
    private static final int HEX_DIGIT_PROPERTY_ = 6;
    private static final int ASCII_HEX_DIGIT_PROPERTY_ = 7;
    private static final int ALPHABETIC_PROPERTY_ = 8;
    private static final int IDEOGRAPHIC_PROPERTY_ = 9;
    private static final int DIACRITIC_PROPERTY_ = 10;
    private static final int EXTENDER_PROPERTY_ = 11;
    private static final int NONCHARACTER_CODE_POINT_PROPERTY_ = 12;
    private static final int GRAPHEME_EXTEND_PROPERTY_ = 13;
    private static final int GRAPHEME_LINK_PROPERTY_ = 14;
    private static final int IDS_BINARY_OPERATOR_PROPERTY_ = 15;
    private static final int IDS_TRINARY_OPERATOR_PROPERTY_ = 16;
    private static final int RADICAL_PROPERTY_ = 17;
    private static final int UNIFIED_IDEOGRAPH_PROPERTY_ = 18;
    private static final int DEFAULT_IGNORABLE_CODE_POINT_PROPERTY_ = 19;
    private static final int DEPRECATED_PROPERTY_ = 20;
    private static final int LOGICAL_ORDER_EXCEPTION_PROPERTY_ = 21;
    private static final int XID_START_PROPERTY_ = 22;
    private static final int XID_CONTINUE_PROPERTY_ = 23;
    private static final int ID_START_PROPERTY_ = 24;
    private static final int ID_CONTINUE_PROPERTY_ = 25;
    private static final int GRAPHEME_BASE_PROPERTY_ = 26;
    private static final int S_TERM_PROPERTY_ = 27;
    private static final int VARIATION_SELECTOR_PROPERTY_ = 28;
    private static final int PATTERN_SYNTAX = 29;
    private static final int PATTERN_WHITE_SPACE = 30;
    private static final int LB_MASK = 0x3F00000;
    private static final int LB_SHIFT = 20;
    private static final int SB_MASK = 1015808;
    private static final int SB_SHIFT = 15;
    private static final int WB_MASK = 31744;
    private static final int WB_SHIFT = 10;
    private static final int GCB_MASK = 992;
    private static final int GCB_SHIFT = 5;
    private static final int DECOMPOSITION_TYPE_MASK_ = 31;
    private static final int FIRST_NIBBLE_SHIFT_ = 4;
    private static final int LAST_NIBBLE_MASK_ = 15;
    private static final int AGE_SHIFT_ = 24;
    private static final int DATA_FORMAT = 1431335535;
    public static final int BIDI_PAIRED_BRACKET_TYPE = 4117;

    public final int getProperty(int ch) {
        return this.m_trie_.get(ch);
    }

    public int getAdditional(int codepoint, int column) {
        assert (column >= 0);
        if (column >= this.m_additionalColumnsCount_) {
            return 0;
        }
        return this.m_additionalVectors_[this.m_additionalTrie_.get(codepoint) + column];
    }

    public VersionInfo getAge(int codepoint) {
        int version = this.getAdditional(codepoint, 0) >> 24;
        return VersionInfo.getInstance(version >> 4 & 0xF, version & 0xF, 0, 0);
    }

    public int getType(int c) {
        return this.getProperty(c) & 0x1F;
    }

    public int getIntPropertyValue(int c, int which) {
        if (which == 4117) {
            return this.intProp.getValue(c);
        }
        return 0;
    }

    public static int getRawSupplementary(char lead, char trail) {
        return (lead << 10) + trail + -56613888;
    }

    public static final int getMask(int type) {
        return 1 << type;
    }

    public static int getEuropeanDigit(int ch) {
        if (ch > 122 && ch < 65313 || ch < 65 || ch > 90 && ch < 97 || ch > 65370 || ch > 65338 && ch < 65345) {
            return -1;
        }
        if (ch <= 122) {
            return ch + 10 - (ch <= 90 ? 65 : 97);
        }
        if (ch <= 65338) {
            return ch + 10 - 65313;
        }
        return ch + 10 - 65345;
    }

    public int digit(int c) {
        int value = UCharacterProperty.getNumericTypeValue(this.getProperty(c)) - 1;
        if (value <= 9) {
            return value;
        }
        return -1;
    }

    private static final int getNumericTypeValue(int props) {
        return props >> 6;
    }

    private static final int ntvGetType(int ntv) {
        return ntv == 0 ? 0 : (ntv < 11 ? 1 : (ntv < 21 ? 2 : 3));
    }

    public static final int mergeScriptCodeOrIndex(int scriptX) {
        return (scriptX & 0x300000) >> 12 | scriptX & 0xFF;
    }

    private UCharacterProperty() throws IOException {
        int numChars;
        int i;
        ByteBuffer bytes = ICUBinary.getRequiredData(DATA_FILE_NAME_);
        this.m_unicodeVersion_ = ICUBinary.readHeaderAndDataVersion(bytes, 1431335535, new IsAcceptable());
        int propertyOffset = bytes.getInt();
        bytes.getInt();
        bytes.getInt();
        int additionalOffset = bytes.getInt();
        int additionalVectorsOffset = bytes.getInt();
        this.m_additionalColumnsCount_ = bytes.getInt();
        int scriptExtensionsOffset = bytes.getInt();
        int reservedOffset7 = bytes.getInt();
        bytes.getInt();
        bytes.getInt();
        this.m_maxBlockScriptValue_ = bytes.getInt();
        this.m_maxJTGValue_ = bytes.getInt();
        ICUBinary.skipBytes(bytes, 16);
        this.m_trie_ = Trie2_16.createFromSerialized(bytes);
        int expectedTrieLength = (propertyOffset - 16) * 4;
        int trieLength = this.m_trie_.getSerializedLength();
        if (trieLength > expectedTrieLength) {
            throw new IOException("uprops.icu: not enough bytes for main trie");
        }
        ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
        ICUBinary.skipBytes(bytes, (additionalOffset - propertyOffset) * 4);
        if (this.m_additionalColumnsCount_ > 0) {
            this.m_additionalTrie_ = Trie2_16.createFromSerialized(bytes);
            expectedTrieLength = (additionalVectorsOffset - additionalOffset) * 4;
            trieLength = this.m_additionalTrie_.getSerializedLength();
            if (trieLength > expectedTrieLength) {
                throw new IOException("uprops.icu: not enough bytes for additional-properties trie");
            }
            ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
            int size = scriptExtensionsOffset - additionalVectorsOffset;
            this.m_additionalVectors_ = new int[size];
            for (i = 0; i < size; ++i) {
                this.m_additionalVectors_[i] = bytes.getInt();
            }
        }
        if ((numChars = (reservedOffset7 - scriptExtensionsOffset) * 2) > 0) {
            this.m_scriptExtensions_ = new char[numChars];
            for (i = 0; i < numChars; ++i) {
                this.m_scriptExtensions_[i] = bytes.getChar();
            }
        }
    }

    public void upropsvec_addPropertyStarts(UnicodeSet set) {
        if (this.m_additionalColumnsCount_ > 0) {
            for (Trie2.Range range : this.m_additionalTrie_) {
                if (range.leadSurrogate) break;
                set.add(range.startCodePoint);
            }
        }
    }

    static {
        gcbToHst = new int[]{0, 0, 0, 0, 1, 0, 4, 5, 3, 2};
        try {
            INSTANCE = new UCharacterProperty();
        }
        catch (IOException e) {
            throw new MissingResourceException(e.getMessage(), DATA_FILE_NAME_, "");
        }
    }

    private class IntProperty {
        int column;
        int mask;
        int shift;

        IntProperty(int column, int mask, int shift) {
            this.column = column;
            this.mask = mask;
            this.shift = shift;
        }

        IntProperty(int source) {
            this.column = source;
            this.mask = 0;
        }

        int getValue(int c) {
            return (UCharacterProperty.this.getAdditional(c, this.column) & this.mask) >>> this.shift;
        }
    }

    private static final class IsAcceptable
    implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override
        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 7;
        }
    }

    private class NormQuickCheckIntProperty
    extends IntProperty {
        int which;
        int max;

        NormQuickCheckIntProperty(UCharacterProperty uCharacterProperty, int source, int which, int max) {
            super(source);
            this.which = which;
            this.max = max;
        }
    }

    private class CombiningClassIntProperty
    extends IntProperty {
        CombiningClassIntProperty(UCharacterProperty uCharacterProperty, int source) {
            super(source);
        }
    }

    private class BiDiIntProperty
    extends IntProperty {
        BiDiIntProperty(UCharacterProperty uCharacterProperty) {
            super(5);
        }
    }
}

