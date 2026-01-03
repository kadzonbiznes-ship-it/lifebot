/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import sun.font.FontUtilities;
import sun.font.TrueTypeFont;

abstract class CMap {
    static final short ShiftJISEncoding = 2;
    static final short GBKEncoding = 3;
    static final short Big5Encoding = 4;
    static final short WansungEncoding = 5;
    static final short JohabEncoding = 6;
    static final short MSUnicodeSurrogateEncoding = 10;
    static final char noSuchChar = '\ufffd';
    static final int SHORTMASK = 65535;
    static final int INTMASK = Integer.MAX_VALUE;
    static final char[][] converterMaps = new char[7][];
    char[] xlat;
    UVS uvs = null;
    public static final NullCMapClass theNullCmap = new NullCMapClass();

    CMap() {
    }

    static CMap initialize(TrueTypeFont font) {
        CMap cmap = null;
        int encodingID = -1;
        int three0 = 0;
        int three1 = 0;
        int three2 = 0;
        int three3 = 0;
        int three4 = 0;
        int three5 = 0;
        int three6 = 0;
        int three10 = 0;
        int zero5 = 0;
        boolean threeStar = false;
        ByteBuffer cmapBuffer = font.getTableBuffer(1668112752);
        int cmapTableOffset = font.getTableSize(1668112752);
        int numberSubTables = cmapBuffer.getShort(2);
        for (int i = 0; i < numberSubTables; ++i) {
            int offset;
            cmapBuffer.position(i * 8 + 4);
            short platformID = cmapBuffer.getShort();
            if (platformID == 3) {
                threeStar = true;
                encodingID = cmapBuffer.getShort();
                offset = cmapBuffer.getInt();
                switch (encodingID) {
                    case 0: {
                        three0 = offset;
                        break;
                    }
                    case 1: {
                        three1 = offset;
                        break;
                    }
                    case 2: {
                        three2 = offset;
                        break;
                    }
                    case 3: {
                        three3 = offset;
                        break;
                    }
                    case 4: {
                        three4 = offset;
                        break;
                    }
                    case 5: {
                        three5 = offset;
                        break;
                    }
                    case 6: {
                        three6 = offset;
                        break;
                    }
                    case 10: {
                        three10 = offset;
                    }
                }
                continue;
            }
            if (platformID != 0) continue;
            encodingID = cmapBuffer.getShort();
            offset = cmapBuffer.getInt();
            if (encodingID != 5) continue;
            zero5 = offset;
        }
        if (threeStar) {
            if (three10 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three10, null);
            } else if (three0 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three0, null);
            } else if (three1 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three1, null);
            } else if (three2 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three2, CMap.getConverterMap((short)2));
            } else if (three3 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three3, CMap.getConverterMap((short)3));
            } else if (three4 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three4, CMap.getConverterMap((short)4));
            } else if (three5 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three5, CMap.getConverterMap((short)5));
            } else if (three6 != 0) {
                cmap = CMap.createCMap(cmapBuffer, three6, CMap.getConverterMap((short)6));
            }
        } else {
            cmap = CMap.createCMap(cmapBuffer, cmapBuffer.getInt(8), null);
        }
        if (cmap != null && zero5 != 0) {
            cmap.createUVS(cmapBuffer, zero5);
        }
        return cmap;
    }

    static char[] getConverter(short encodingID) {
        String encoding;
        int dBegin = 32768;
        int dEnd = 65535;
        switch (encodingID) {
            case 2: {
                dBegin = 33088;
                dEnd = 64764;
                encoding = "SJIS";
                break;
            }
            case 3: {
                dBegin = 33088;
                dEnd = 65184;
                encoding = "GBK";
                break;
            }
            case 4: {
                dBegin = 41280;
                dEnd = 65278;
                encoding = "Big5";
                break;
            }
            case 5: {
                dBegin = 41377;
                dEnd = 65246;
                encoding = "EUC_KR";
                break;
            }
            case 6: {
                dBegin = 33089;
                dEnd = 65022;
                encoding = "Johab";
                break;
            }
            default: {
                return null;
            }
        }
        try {
            int i;
            char[] convertedChars = new char[65536];
            for (int i2 = 0; i2 < 65536; ++i2) {
                convertedChars[i2] = 65533;
            }
            byte[] inputBytes = new byte[(dEnd - dBegin + 1) * 2];
            char[] outputChars = new char[dEnd - dBegin + 1];
            int j = 0;
            if (encodingID == 2) {
                for (i = dBegin; i <= dEnd; ++i) {
                    int firstByte = i >> 8 & 0xFF;
                    if (firstByte >= 161 && firstByte <= 223) {
                        inputBytes[j++] = -1;
                        inputBytes[j++] = -1;
                        continue;
                    }
                    inputBytes[j++] = (byte)firstByte;
                    inputBytes[j++] = (byte)(i & 0xFF);
                }
            } else {
                for (i = dBegin; i <= dEnd; ++i) {
                    inputBytes[j++] = (byte)(i >> 8 & 0xFF);
                    inputBytes[j++] = (byte)(i & 0xFF);
                }
            }
            Charset.forName(encoding).newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("\u0000").decode(ByteBuffer.wrap(inputBytes, 0, inputBytes.length), CharBuffer.wrap(outputChars, 0, outputChars.length), true);
            for (i = 32; i <= 126; ++i) {
                convertedChars[i] = (char)i;
            }
            if (encodingID == 2) {
                for (i = 161; i <= 223; ++i) {
                    convertedChars[i] = (char)(i - 161 + 65377);
                }
            }
            System.arraycopy(outputChars, 0, convertedChars, dBegin, outputChars.length);
            char[] invertedChars = new char[65536];
            for (int i3 = 0; i3 < 65536; ++i3) {
                if (convertedChars[i3] == '\ufffd') continue;
                invertedChars[convertedChars[i3]] = (char)i3;
            }
            return invertedChars;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static char[] getConverterMap(short encodingID) {
        if (converterMaps[encodingID] == null) {
            CMap.converterMaps[encodingID] = CMap.getConverter(encodingID);
        }
        return converterMaps[encodingID];
    }

    static CMap createCMap(ByteBuffer buffer, int offset, char[] xlat) {
        char subtableFormat = buffer.getChar(offset);
        long subtableLength = subtableFormat < '\b' ? (long)buffer.getChar(offset + 2) : (long)(buffer.getInt(offset + 4) & Integer.MAX_VALUE);
        if (FontUtilities.isLogging() && (long)offset + subtableLength > (long)buffer.capacity()) {
            FontUtilities.logWarning("Cmap subtable overflows buffer.");
        }
        switch (subtableFormat) {
            case '\u0000': {
                return new CMapFormat0(buffer, offset);
            }
            case '\u0002': {
                return new CMapFormat2(buffer, offset, xlat);
            }
            case '\u0004': {
                return new CMapFormat4(buffer, offset, xlat);
            }
            case '\u0006': {
                return new CMapFormat6(buffer, offset, xlat);
            }
            case '\b': {
                return new CMapFormat8(buffer, offset, xlat);
            }
            case '\n': {
                return new CMapFormat10(buffer, offset, xlat);
            }
            case '\f': {
                return new CMapFormat12(buffer, offset, xlat);
            }
        }
        throw new RuntimeException("Cmap format unimplemented: " + buffer.getChar(offset));
    }

    private void createUVS(ByteBuffer buffer, int offset) {
        char subtableFormat = buffer.getChar(offset);
        if (subtableFormat == '\u000e') {
            long subtableLength = buffer.getInt(offset + 2) & Integer.MAX_VALUE;
            if (FontUtilities.isLogging() && (long)offset + subtableLength > (long)buffer.capacity()) {
                FontUtilities.logWarning("Cmap UVS subtable overflows buffer.");
            }
            try {
                this.uvs = new UVS(buffer, offset);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    abstract char getGlyph(int var1);

    final int getControlCodeGlyph(int charCode, boolean noSurrogates) {
        if (charCode < 16) {
            switch (charCode) {
                case 9: 
                case 10: 
                case 13: {
                    return 65535;
                }
            }
        } else if (noSurrogates && charCode >= 65535) {
            return 0;
        }
        return -1;
    }

    final char getFormatCharGlyph(int charCode) {
        if (charCode >= 8204 && (charCode <= 8207 || charCode >= 8232 && charCode <= 8238 || charCode >= 8298 && charCode <= 8303)) {
            return '\uffff';
        }
        return '\u0000';
    }

    char getVariationGlyph(int charCode, int variationSelector) {
        int result;
        char glyph = '\u0000';
        glyph = this.uvs == null ? this.getGlyph(charCode) : ((result = this.uvs.getGlyph(charCode, variationSelector)) > 0 ? (char)(result & 0xFFFF) : this.getGlyph(charCode));
        return glyph;
    }

    static class UVS {
        int numSelectors;
        int[] selector;
        int[] numUVSMapping;
        int[][] unicodeValue;
        char[][] glyphID;
        static final int VS_NOGLYPH = 0;

        UVS(ByteBuffer buffer, int offset) {
            buffer.position(offset + 6);
            this.numSelectors = buffer.getInt() & Integer.MAX_VALUE;
            if ((long)buffer.remaining() < 11L * (long)this.numSelectors) {
                throw new RuntimeException("Variations exceed buffer");
            }
            this.selector = new int[this.numSelectors];
            this.numUVSMapping = new int[this.numSelectors];
            this.unicodeValue = new int[this.numSelectors][];
            this.glyphID = new char[this.numSelectors][];
            for (int i = 0; i < this.numSelectors; ++i) {
                buffer.position(offset + 10 + i * 11);
                this.selector[i] = (buffer.get() & 0xFF) << 16;
                int n = i;
                this.selector[n] = this.selector[n] + ((buffer.get() & 0xFF) << 8);
                int n2 = i;
                this.selector[n2] = this.selector[n2] + (buffer.get() & 0xFF);
                int tableOffset = buffer.getInt(offset + 10 + i * 11 + 7);
                if (tableOffset == 0) {
                    this.numUVSMapping[i] = 0;
                    continue;
                }
                if (tableOffset <= 0) continue;
                buffer.position(offset + tableOffset);
                this.numUVSMapping[i] = buffer.getInt() & Integer.MAX_VALUE;
                if ((long)buffer.remaining() < 5L * (long)this.numUVSMapping[i]) {
                    throw new RuntimeException("Variations exceed buffer");
                }
                this.unicodeValue[i] = new int[this.numUVSMapping[i]];
                this.glyphID[i] = new char[this.numUVSMapping[i]];
                for (int j = 0; j < this.numUVSMapping[i]; ++j) {
                    int temp = (buffer.get() & 0xFF) << 16;
                    temp += (buffer.get() & 0xFF) << 8;
                    this.unicodeValue[i][j] = temp += buffer.get() & 0xFF;
                    this.glyphID[i][j] = buffer.getChar();
                }
            }
        }

        private int getGlyph(int charCode, int variationSelector) {
            int index;
            int targetSelector = -1;
            for (int i = 0; i < this.numSelectors; ++i) {
                if (this.selector[i] != variationSelector) continue;
                targetSelector = i;
                break;
            }
            if (targetSelector == -1) {
                return 0;
            }
            if (this.numUVSMapping[targetSelector] > 0 && (index = Arrays.binarySearch(this.unicodeValue[targetSelector], charCode)) >= 0) {
                return this.glyphID[targetSelector][index];
            }
            return 0;
        }
    }

    static class CMapFormat0
    extends CMap {
        byte[] cmap;

        CMapFormat0(ByteBuffer buffer, int offset) {
            char len = buffer.getChar(offset + 2);
            this.cmap = new byte[len - 6];
            buffer.position(offset + 6);
            buffer.get(this.cmap);
        }

        @Override
        char getGlyph(int charCode) {
            if (charCode < 256) {
                if (charCode < 16) {
                    switch (charCode) {
                        case 9: 
                        case 10: 
                        case 13: {
                            return '\uffff';
                        }
                    }
                }
                return (char)(0xFF & this.cmap[charCode]);
            }
            return '\u0000';
        }
    }

    static class CMapFormat2
    extends CMap {
        char[] subHeaderKey = new char[256];
        char[] firstCodeArray;
        char[] entryCountArray;
        short[] idDeltaArray;
        char[] idRangeOffSetArray;
        char[] glyphIndexArray;

        CMapFormat2(ByteBuffer buffer, int offset, char[] xlat) {
            this.xlat = xlat;
            char tableLen = buffer.getChar(offset + 2);
            buffer.position(offset + 6);
            CharBuffer cBuffer = buffer.asCharBuffer();
            char maxSubHeader = '\u0000';
            for (int i = 0; i < 256; ++i) {
                this.subHeaderKey[i] = cBuffer.get();
                if (this.subHeaderKey[i] <= maxSubHeader) continue;
                maxSubHeader = this.subHeaderKey[i];
            }
            int numSubHeaders = (maxSubHeader >> 3) + 1;
            this.firstCodeArray = new char[numSubHeaders];
            this.entryCountArray = new char[numSubHeaders];
            this.idDeltaArray = new short[numSubHeaders];
            this.idRangeOffSetArray = new char[numSubHeaders];
            for (int i = 0; i < numSubHeaders; ++i) {
                this.firstCodeArray[i] = cBuffer.get();
                this.entryCountArray[i] = cBuffer.get();
                this.idDeltaArray[i] = (short)cBuffer.get();
                this.idRangeOffSetArray[i] = cBuffer.get();
            }
            int glyphIndexArrSize = (tableLen - 518 - numSubHeaders * 8) / 2;
            this.glyphIndexArray = new char[glyphIndexArrSize];
            for (int i = 0; i < glyphIndexArrSize; ++i) {
                this.glyphIndexArray[i] = cBuffer.get();
            }
        }

        @Override
        char getGlyph(int charCode) {
            int glyphArrayOffset;
            int glyphSubArrayStart;
            char glyphCode;
            char mapMe;
            int origCharCode = charCode;
            int controlGlyph = this.getControlCodeGlyph(charCode, true);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
            if (this.xlat != null) {
                charCode = this.xlat[charCode];
            }
            char highByte = (char)(charCode >> 8);
            char lowByte = (char)(charCode & 0xFF);
            int key = this.subHeaderKey[highByte] >> 3;
            if (key != 0) {
                mapMe = lowByte;
            } else {
                mapMe = highByte;
                if (mapMe == '\u0000') {
                    mapMe = lowByte;
                }
            }
            char firstCode = this.firstCodeArray[key];
            if (mapMe < firstCode) {
                return '\u0000';
            }
            if ((mapMe = (char)(mapMe - firstCode)) < this.entryCountArray[key] && (glyphCode = this.glyphIndexArray[(glyphSubArrayStart = (this.idRangeOffSetArray[key] - (glyphArrayOffset = (this.idRangeOffSetArray.length - key) * 8 - 6)) / 2) + mapMe]) != '\u0000') {
                glyphCode = (char)(glyphCode + this.idDeltaArray[key]);
                return glyphCode;
            }
            return this.getFormatCharGlyph(origCharCode);
        }
    }

    static class CMapFormat4
    extends CMap {
        int segCount;
        int entrySelector;
        int rangeShift;
        char[] endCount;
        char[] startCount;
        short[] idDelta;
        char[] idRangeOffset;
        char[] glyphIds;

        CMapFormat4(ByteBuffer bbuffer, int offset, char[] xlat) {
            int i;
            this.xlat = xlat;
            bbuffer.position(offset);
            CharBuffer buffer = bbuffer.asCharBuffer();
            buffer.get();
            int subtableLength = buffer.get();
            if (offset + subtableLength > bbuffer.capacity()) {
                subtableLength = bbuffer.capacity() - offset;
            }
            buffer.get();
            this.segCount = buffer.get() / 2;
            char searchRange = buffer.get();
            this.entrySelector = buffer.get();
            this.rangeShift = buffer.get() / 2;
            this.startCount = new char[this.segCount];
            this.endCount = new char[this.segCount];
            this.idDelta = new short[this.segCount];
            this.idRangeOffset = new char[this.segCount];
            for (i = 0; i < this.segCount; ++i) {
                this.endCount[i] = buffer.get();
            }
            buffer.get();
            for (i = 0; i < this.segCount; ++i) {
                this.startCount[i] = buffer.get();
            }
            for (i = 0; i < this.segCount; ++i) {
                this.idDelta[i] = (short)buffer.get();
            }
            for (i = 0; i < this.segCount; ++i) {
                char ctmp = buffer.get();
                this.idRangeOffset[i] = (char)(ctmp >> 1 & 0xFFFF);
            }
            int pos = (this.segCount * 8 + 16) / 2;
            buffer.position(pos);
            int numGlyphIds = subtableLength / 2 - pos;
            this.glyphIds = new char[numGlyphIds];
            for (int i2 = 0; i2 < numGlyphIds; ++i2) {
                this.glyphIds[i2] = buffer.get();
            }
        }

        @Override
        char getGlyph(int charCode) {
            int origCharCode = charCode;
            int index = 0;
            char glyphCode = '\u0000';
            int controlGlyph = this.getControlCodeGlyph(charCode, true);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
            if (this.xlat != null) {
                charCode = this.xlat[charCode];
            }
            int left = 0;
            int right = this.startCount.length;
            index = this.startCount.length >> 1;
            while (left < right) {
                if (this.endCount[index] < charCode) {
                    left = index + 1;
                } else {
                    right = index;
                }
                index = left + right >> 1;
            }
            if (charCode >= this.startCount[index] && charCode <= this.endCount[index]) {
                char rangeOffset = this.idRangeOffset[index];
                if (rangeOffset == '\u0000') {
                    glyphCode = (char)(charCode + this.idDelta[index]);
                } else {
                    int glyphIDIndex = rangeOffset - this.segCount + index + (charCode - this.startCount[index]);
                    glyphCode = this.glyphIds[glyphIDIndex];
                    if (glyphCode != '\u0000') {
                        glyphCode = (char)(glyphCode + this.idDelta[index]);
                    }
                }
            }
            if (glyphCode == '\u0000') {
                glyphCode = this.getFormatCharGlyph(origCharCode);
            }
            return glyphCode;
        }
    }

    static class CMapFormat6
    extends CMap {
        char firstCode;
        char entryCount;
        char[] glyphIdArray;

        CMapFormat6(ByteBuffer bbuffer, int offset, char[] xlat) {
            bbuffer.position(offset + 6);
            CharBuffer buffer = bbuffer.asCharBuffer();
            this.firstCode = buffer.get();
            this.entryCount = buffer.get();
            this.glyphIdArray = new char[this.entryCount];
            for (int i = 0; i < this.entryCount; ++i) {
                this.glyphIdArray[i] = buffer.get();
            }
        }

        @Override
        char getGlyph(int charCode) {
            int origCharCode = charCode;
            int controlGlyph = this.getControlCodeGlyph(charCode, true);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
            if (this.xlat != null) {
                charCode = this.xlat[charCode];
            }
            if ((charCode -= this.firstCode) < 0 || charCode >= this.entryCount) {
                return this.getFormatCharGlyph(origCharCode);
            }
            return this.glyphIdArray[charCode];
        }
    }

    static class CMapFormat8
    extends CMap {
        byte[] is32 = new byte[8192];
        int nGroups;
        int[] startCharCode;
        int[] endCharCode;
        int[] startGlyphID;

        CMapFormat8(ByteBuffer bbuffer, int offset, char[] xlat) {
            bbuffer.position(12);
            bbuffer.get(this.is32);
            this.nGroups = bbuffer.getInt() & Integer.MAX_VALUE;
            if ((long)bbuffer.remaining() < 12L * (long)this.nGroups) {
                throw new RuntimeException("Format 8 table exceeded");
            }
            this.startCharCode = new int[this.nGroups];
            this.endCharCode = new int[this.nGroups];
            this.startGlyphID = new int[this.nGroups];
        }

        @Override
        char getGlyph(int charCode) {
            if (this.xlat != null) {
                throw new RuntimeException("xlat array for cmap fmt=8");
            }
            return '\u0000';
        }
    }

    static class CMapFormat10
    extends CMap {
        long firstCode;
        int entryCount;
        char[] glyphIdArray;

        CMapFormat10(ByteBuffer bbuffer, int offset, char[] xlat) {
            bbuffer.position(offset + 12);
            this.firstCode = bbuffer.getInt() & Integer.MAX_VALUE;
            this.entryCount = bbuffer.getInt() & Integer.MAX_VALUE;
            if ((long)bbuffer.remaining() < 2L * (long)this.entryCount) {
                throw new RuntimeException("Format 10 table exceeded");
            }
            CharBuffer buffer = bbuffer.asCharBuffer();
            this.glyphIdArray = new char[this.entryCount];
            for (int i = 0; i < this.entryCount; ++i) {
                this.glyphIdArray[i] = buffer.get();
            }
        }

        @Override
        char getGlyph(int charCode) {
            if (this.xlat != null) {
                throw new RuntimeException("xlat array for cmap fmt=10");
            }
            int code = (int)((long)charCode - this.firstCode);
            if (code < 0 || code >= this.entryCount) {
                return '\u0000';
            }
            return this.glyphIdArray[code];
        }
    }

    static class CMapFormat12
    extends CMap {
        int numGroups;
        int highBit = 0;
        int power;
        int extra;
        long[] startCharCode;
        long[] endCharCode;
        int[] startGlyphID;

        CMapFormat12(ByteBuffer buffer, int offset, char[] xlat) {
            if (xlat != null) {
                throw new RuntimeException("xlat array for cmap fmt=12");
            }
            buffer.position(offset + 12);
            this.numGroups = buffer.getInt() & Integer.MAX_VALUE;
            if ((long)buffer.remaining() < 12L * (long)this.numGroups) {
                throw new RuntimeException("Format 12 table exceeded");
            }
            this.startCharCode = new long[this.numGroups];
            this.endCharCode = new long[this.numGroups];
            this.startGlyphID = new int[this.numGroups];
            buffer = buffer.slice();
            IntBuffer ibuffer = buffer.asIntBuffer();
            for (int i = 0; i < this.numGroups; ++i) {
                this.startCharCode[i] = ibuffer.get() & Integer.MAX_VALUE;
                this.endCharCode[i] = ibuffer.get() & Integer.MAX_VALUE;
                this.startGlyphID[i] = ibuffer.get() & Integer.MAX_VALUE;
            }
            int value = this.numGroups;
            if (value >= 65536) {
                value >>= 16;
                this.highBit += 16;
            }
            if (value >= 256) {
                value >>= 8;
                this.highBit += 8;
            }
            if (value >= 16) {
                value >>= 4;
                this.highBit += 4;
            }
            if (value >= 4) {
                value >>= 2;
                this.highBit += 2;
            }
            if (value >= 2) {
                value >>= 1;
                ++this.highBit;
            }
            this.power = 1 << this.highBit;
            this.extra = this.numGroups - this.power;
        }

        @Override
        char getGlyph(int charCode) {
            int origCharCode = charCode;
            int controlGlyph = this.getControlCodeGlyph(charCode, false);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
            int probe = this.power;
            int range = 0;
            if (this.startCharCode[this.extra] <= (long)charCode) {
                range = this.extra;
            }
            while (probe > 1) {
                if (this.startCharCode[range + (probe >>= 1)] > (long)charCode) continue;
                range += probe;
            }
            if (this.startCharCode[range] <= (long)charCode && this.endCharCode[range] >= (long)charCode) {
                return (char)((long)this.startGlyphID[range] + ((long)charCode - this.startCharCode[range]));
            }
            return this.getFormatCharGlyph(origCharCode);
        }
    }

    static class NullCMapClass
    extends CMap {
        NullCMapClass() {
        }

        @Override
        char getGlyph(int charCode) {
            return '\u0000';
        }
    }
}

