/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import java.util.ArrayList;
import java.util.Arrays;

public final class ImFontGlyphRangesBuilder {
    private static final int UNICODE_CODEPOINT_MAX = 65535;
    private final long[] usedChars = new long[8192];

    public ImFontGlyphRangesBuilder() {
        this.clear();
    }

    public void addChar(char c) {
        this.setBit(c);
    }

    public void addText(String text) {
        for (int i = 0; i < text.length(); ++i) {
            this.addChar(text.charAt(i));
        }
    }

    public void addRanges(short[] ranges) {
        for (int i = 0; i < ranges.length && ranges[i] != 0; i += 2) {
            for (int k = ranges[i]; k <= ranges[i + 1]; ++k) {
                this.addChar((char)k);
            }
        }
    }

    public short[] buildRanges() {
        ArrayList<Short> out = new ArrayList<Short>();
        for (int n = 0; n <= 65535; ++n) {
            if (!this.getBit(n)) continue;
            out.add((short)n);
            while (n < 65535 && this.getBit(n + 1)) {
                ++n;
            }
            out.add((short)n);
        }
        short[] result = new short[out.size() + 1];
        for (int i = 0; i < out.size(); ++i) {
            result[i] = (Short)out.get(i);
        }
        result[result.length - 1] = 0;
        return result;
    }

    public void clear() {
        Arrays.fill(this.usedChars, 0L);
    }

    public void setBit(int n) {
        int off = n >> 5;
        long mask = 1L << (int)((long)n & 0x1FL);
        int n2 = off;
        this.usedChars[n2] = this.usedChars[n2] | mask;
    }

    public boolean getBit(int n) {
        int off = n >> 5;
        long mask = 1L << (int)((long)n & 0x1FL);
        return (this.usedChars[off] & mask) > 0L;
    }
}

