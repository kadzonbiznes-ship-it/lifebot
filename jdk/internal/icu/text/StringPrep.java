/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.text;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.ParseException;
import jdk.internal.icu.impl.CharTrie;
import jdk.internal.icu.impl.StringPrepDataReader;
import jdk.internal.icu.impl.Trie;
import jdk.internal.icu.lang.UCharacter;
import jdk.internal.icu.text.UCharacterIterator;
import jdk.internal.icu.text.UTF16;
import jdk.internal.icu.util.VersionInfo;
import sun.text.Normalizer;

public final class StringPrep {
    public static final int DEFAULT = 0;
    public static final int ALLOW_UNASSIGNED = 1;
    private static final int UNASSIGNED = 0;
    private static final int MAP = 1;
    private static final int PROHIBITED = 2;
    private static final int DELETE = 3;
    private static final int TYPE_LIMIT = 4;
    private static final int NORMALIZATION_ON = 1;
    private static final int CHECK_BIDI_ON = 2;
    private static final int TYPE_THRESHOLD = 65520;
    private static final int MAX_INDEX_VALUE = 16319;
    private static final int MAX_INDEX_TOP_LENGTH = 3;
    private static final int INDEX_TRIE_SIZE = 0;
    private static final int INDEX_MAPPING_DATA_SIZE = 1;
    private static final int NORM_CORRECTNS_LAST_UNI_VERSION = 2;
    private static final int ONE_UCHAR_MAPPING_INDEX_START = 3;
    private static final int TWO_UCHARS_MAPPING_INDEX_START = 4;
    private static final int THREE_UCHARS_MAPPING_INDEX_START = 5;
    private static final int FOUR_UCHARS_MAPPING_INDEX_START = 6;
    private static final int OPTIONS = 7;
    private static final int INDEX_TOP = 16;
    private static final int DATA_BUFFER_SIZE = 25000;
    private StringPrepTrieImpl sprepTrieImpl;
    private int[] indexes;
    private char[] mappingData;
    private byte[] formatVersion;
    private VersionInfo sprepUniVer;
    private VersionInfo normCorrVer;
    private boolean doNFKC;
    private boolean checkBiDi;

    private char getCodePointValue(int ch) {
        return this.sprepTrieImpl.sprepTrie.getCodePointValue(ch);
    }

    private static VersionInfo getVersionInfo(int comp) {
        int micro = comp & 0xFF;
        int milli = comp >> 8 & 0xFF;
        int minor = comp >> 16 & 0xFF;
        int major = comp >> 24 & 0xFF;
        return VersionInfo.getInstance(major, minor, milli, micro);
    }

    private static VersionInfo getVersionInfo(byte[] version) {
        if (version.length != 4) {
            return null;
        }
        return VersionInfo.getInstance(version[0], version[1], version[2], version[3]);
    }

    public StringPrep(InputStream inputStream) throws IOException {
        BufferedInputStream b = new BufferedInputStream(inputStream, 25000);
        StringPrepDataReader reader = new StringPrepDataReader(b);
        this.indexes = reader.readIndexes(16);
        byte[] sprepBytes = new byte[this.indexes[0]];
        this.mappingData = new char[this.indexes[1] / 2];
        reader.read(sprepBytes, this.mappingData);
        this.sprepTrieImpl = new StringPrepTrieImpl();
        this.sprepTrieImpl.sprepTrie = new CharTrie(new ByteArrayInputStream(sprepBytes), this.sprepTrieImpl);
        this.formatVersion = reader.getDataFormatVersion();
        this.doNFKC = (this.indexes[7] & 1) > 0;
        this.checkBiDi = (this.indexes[7] & 2) > 0;
        this.sprepUniVer = StringPrep.getVersionInfo(reader.getUnicodeVersion());
        this.normCorrVer = StringPrep.getVersionInfo(this.indexes[2]);
        VersionInfo normUniVer = UCharacter.getUnicodeVersion();
        if (normUniVer.compareTo(this.sprepUniVer) < 0 && normUniVer.compareTo(this.normCorrVer) < 0 && (this.indexes[7] & 1) > 0) {
            throw new IOException("Normalization Correction version not supported");
        }
        b.close();
    }

    private static final void getValues(char trieWord, Values values) {
        values.reset();
        if (trieWord == '\u0000') {
            values.type = 4;
        } else if (trieWord >= '\ufff0') {
            values.type = trieWord - 65520;
        } else {
            values.type = 1;
            if ((trieWord & 2) > 0) {
                values.isIndex = true;
                values.value = trieWord >> 2;
            } else {
                values.isIndex = false;
                values.value = trieWord << 16 >> 16;
                values.value >>= 2;
            }
            if (trieWord >> 2 == 16319) {
                values.type = 3;
                values.isIndex = false;
                values.value = 0;
            }
        }
    }

    private StringBuffer map(UCharacterIterator iter, int options) throws ParseException {
        boolean allowUnassigned;
        Values val = new Values();
        char result = '\u0000';
        int ch = -1;
        StringBuffer dest = new StringBuffer();
        boolean bl = allowUnassigned = (options & 1) > 0;
        while ((ch = iter.nextCodePoint()) != -1) {
            result = this.getCodePointValue(ch);
            StringPrep.getValues(result, val);
            if (val.type == 0 && !allowUnassigned) {
                throw new ParseException("An unassigned code point was found in the input " + iter.getText(), iter.getIndex());
            }
            if (val.type == 1) {
                if (val.isIndex) {
                    int index = val.value;
                    int length = index >= this.indexes[3] && index < this.indexes[4] ? 1 : (index >= this.indexes[4] && index < this.indexes[5] ? 2 : (index >= this.indexes[5] && index < this.indexes[6] ? 3 : this.mappingData[index++]));
                    dest.append(this.mappingData, index, length);
                    continue;
                }
                ch -= val.value;
            } else if (val.type == 3) continue;
            UTF16.append(dest, ch);
        }
        return dest;
    }

    private StringBuffer normalize(StringBuffer src) {
        return new StringBuffer(Normalizer.normalize(src.toString(), Normalizer.Form.NFKC, 32));
    }

    public StringBuffer prepare(UCharacterIterator src, int options) throws ParseException {
        int ch;
        StringBuffer mapOut;
        StringBuffer normOut = mapOut = this.map(src, options);
        if (this.doNFKC) {
            normOut = this.normalize(mapOut);
        }
        UCharacterIterator iter = UCharacterIterator.getInstance(normOut);
        Values val = new Values();
        int direction = 19;
        int firstCharDir = 19;
        int rtlPos = -1;
        int ltrPos = -1;
        boolean rightToLeft = false;
        boolean leftToRight = false;
        while ((ch = iter.nextCodePoint()) != -1) {
            char result = this.getCodePointValue(ch);
            StringPrep.getValues(result, val);
            if (val.type == 2) {
                throw new ParseException("A prohibited code point was found in the input" + iter.getText(), val.value);
            }
            direction = UCharacter.getDirection(ch);
            if (firstCharDir == 19) {
                firstCharDir = direction;
            }
            if (direction == 0) {
                leftToRight = true;
                ltrPos = iter.getIndex() - 1;
            }
            if (direction != 1 && direction != 13) continue;
            rightToLeft = true;
            rtlPos = iter.getIndex() - 1;
        }
        if (this.checkBiDi) {
            if (leftToRight && rightToLeft) {
                throw new ParseException("The input does not conform to the rules for BiDi code points." + iter.getText(), rtlPos > ltrPos ? rtlPos : ltrPos);
            }
            if (rightToLeft && (firstCharDir != 1 && firstCharDir != 13 || direction != 1 && direction != 13)) {
                throw new ParseException("The input does not conform to the rules for BiDi code points." + iter.getText(), rtlPos > ltrPos ? rtlPos : ltrPos);
            }
        }
        return normOut;
    }

    private static final class StringPrepTrieImpl
    implements Trie.DataManipulate {
        private CharTrie sprepTrie = null;

        private StringPrepTrieImpl() {
        }

        @Override
        public int getFoldingOffset(int value) {
            return value;
        }
    }

    private static final class Values {
        boolean isIndex;
        int value;
        int type;

        private Values() {
        }

        public void reset() {
            this.isIndex = false;
            this.value = 0;
            this.type = -1;
        }
    }
}

