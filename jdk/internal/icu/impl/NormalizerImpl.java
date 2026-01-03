/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import jdk.internal.icu.impl.ICUBinary;
import jdk.internal.icu.lang.UCharacter;
import jdk.internal.icu.text.Normalizer2;
import jdk.internal.icu.text.UTF16;
import jdk.internal.icu.util.CodePointTrie;
import jdk.internal.icu.util.VersionInfo;

public final class NormalizerImpl {
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    private static final int DATA_FORMAT = 1316121906;
    public static final int MIN_YES_YES_WITH_CC = 65026;
    public static final int JAMO_VT = 65024;
    public static final int MIN_NORMAL_MAYBE_YES = 64512;
    public static final int JAMO_L = 2;
    public static final int INERT = 1;
    public static final int HAS_COMP_BOUNDARY_AFTER = 1;
    public static final int OFFSET_SHIFT = 1;
    public static final int DELTA_TCCC_0 = 0;
    public static final int DELTA_TCCC_1 = 2;
    public static final int DELTA_TCCC_GT_1 = 4;
    public static final int DELTA_TCCC_MASK = 6;
    public static final int DELTA_SHIFT = 3;
    public static final int MAX_DELTA = 64;
    public static final int IX_NORM_TRIE_OFFSET = 0;
    public static final int IX_EXTRA_DATA_OFFSET = 1;
    public static final int IX_SMALL_FCD_OFFSET = 2;
    public static final int IX_RESERVED3_OFFSET = 3;
    public static final int IX_TOTAL_SIZE = 7;
    public static final int MIN_CCC_LCCC_CP = 768;
    public static final int IX_MIN_DECOMP_NO_CP = 8;
    public static final int IX_MIN_COMP_NO_MAYBE_CP = 9;
    public static final int IX_MIN_YES_NO = 10;
    public static final int IX_MIN_NO_NO = 11;
    public static final int IX_LIMIT_NO_NO = 12;
    public static final int IX_MIN_MAYBE_YES = 13;
    public static final int IX_MIN_YES_NO_MAPPINGS_ONLY = 14;
    public static final int IX_MIN_NO_NO_COMP_BOUNDARY_BEFORE = 15;
    public static final int IX_MIN_NO_NO_COMP_NO_MAYBE_CC = 16;
    public static final int IX_MIN_NO_NO_EMPTY = 17;
    public static final int IX_MIN_LCCC_CP = 18;
    public static final int IX_COUNT = 20;
    public static final int MAPPING_HAS_CCC_LCCC_WORD = 128;
    public static final int MAPPING_HAS_RAW_MAPPING = 64;
    public static final int MAPPING_LENGTH_MASK = 31;
    public static final int COMP_1_LAST_TUPLE = 32768;
    public static final int COMP_1_TRIPLE = 1;
    public static final int COMP_1_TRAIL_LIMIT = 13312;
    public static final int COMP_1_TRAIL_MASK = 32766;
    public static final int COMP_1_TRAIL_SHIFT = 9;
    public static final int COMP_2_TRAIL_SHIFT = 6;
    public static final int COMP_2_TRAIL_MASK = 65472;
    private VersionInfo dataVersion;
    private int minDecompNoCP;
    private int minCompNoMaybeCP;
    private int minLcccCP;
    private int minYesNo;
    private int minYesNoMappingsOnly;
    private int minNoNo;
    private int minNoNoCompBoundaryBefore;
    private int minNoNoCompNoMaybeCC;
    private int minNoNoEmpty;
    private int limitNoNo;
    private int centerNoNoDelta;
    private int minMaybeYes;
    private CodePointTrie.Fast16 normTrie;
    private String maybeYesCompositions;
    private String extraData;
    private byte[] smallFCD;

    public NormalizerImpl load(ByteBuffer bytes) {
        try {
            this.dataVersion = ICUBinary.readHeaderAndDataVersion(bytes, 1316121906, IS_ACCEPTABLE);
            int indexesLength = bytes.getInt() / 4;
            if (indexesLength <= 18) {
                throw new InternalError("Normalizer2 data: not enough indexes");
            }
            int[] inIndexes = new int[indexesLength];
            inIndexes[0] = indexesLength * 4;
            for (int i = 1; i < indexesLength; ++i) {
                inIndexes[i] = bytes.getInt();
            }
            this.minDecompNoCP = inIndexes[8];
            this.minCompNoMaybeCP = inIndexes[9];
            this.minLcccCP = inIndexes[18];
            this.minYesNo = inIndexes[10];
            this.minYesNoMappingsOnly = inIndexes[14];
            this.minNoNo = inIndexes[11];
            this.minNoNoCompBoundaryBefore = inIndexes[15];
            this.minNoNoCompNoMaybeCC = inIndexes[16];
            this.minNoNoEmpty = inIndexes[17];
            this.limitNoNo = inIndexes[12];
            this.minMaybeYes = inIndexes[13];
            assert ((this.minMaybeYes & 7) == 0);
            this.centerNoNoDelta = (this.minMaybeYes >> 3) - 64 - 1;
            int offset = inIndexes[0];
            int nextOffset = inIndexes[1];
            int triePosition = bytes.position();
            this.normTrie = CodePointTrie.Fast16.fromBinary(bytes);
            int trieLength = bytes.position() - triePosition;
            if (trieLength > nextOffset - offset) {
                throw new InternalError("Normalizer2 data: not enough bytes for normTrie");
            }
            ICUBinary.skipBytes(bytes, nextOffset - offset - trieLength);
            offset = nextOffset;
            nextOffset = inIndexes[2];
            int numChars = (nextOffset - offset) / 2;
            if (numChars != 0) {
                this.maybeYesCompositions = ICUBinary.getString(bytes, numChars, 0);
                this.extraData = this.maybeYesCompositions.substring(64512 - this.minMaybeYes >> 1);
            }
            offset = nextOffset;
            this.smallFCD = new byte[256];
            bytes.get(this.smallFCD);
            return this;
        }
        catch (IOException e) {
            throw new InternalError(e);
        }
    }

    public NormalizerImpl load(String name) {
        return this.load(ICUBinary.getRequiredData(name));
    }

    public int getNorm16(int c) {
        return UTF16Plus.isLeadSurrogate(c) ? 1 : this.normTrie.get(c);
    }

    public int getRawNorm16(int c) {
        return this.normTrie.get(c);
    }

    public boolean isAlgorithmicNoNo(int norm16) {
        return this.limitNoNo <= norm16 && norm16 < this.minMaybeYes;
    }

    public boolean isCompNo(int norm16) {
        return this.minNoNo <= norm16 && norm16 < this.minMaybeYes;
    }

    public boolean isDecompYes(int norm16) {
        return norm16 < this.minYesNo || this.minMaybeYes <= norm16;
    }

    public int getCC(int norm16) {
        if (norm16 >= 64512) {
            return NormalizerImpl.getCCFromNormalYesOrMaybe(norm16);
        }
        if (norm16 < this.minNoNo || this.limitNoNo <= norm16) {
            return 0;
        }
        return this.getCCFromNoNo(norm16);
    }

    public static int getCCFromNormalYesOrMaybe(int norm16) {
        return norm16 >> 1 & 0xFF;
    }

    public static int getCCFromYesOrMaybe(int norm16) {
        return norm16 >= 64512 ? NormalizerImpl.getCCFromNormalYesOrMaybe(norm16) : 0;
    }

    public int getCCFromYesOrMaybeCP(int c) {
        if (c < this.minCompNoMaybeCP) {
            return 0;
        }
        return NormalizerImpl.getCCFromYesOrMaybe(this.getNorm16(c));
    }

    public int getFCD16(int c) {
        if (c < this.minDecompNoCP) {
            return 0;
        }
        if (c <= 65535 && !this.singleLeadMightHaveNonZeroFCD16(c)) {
            return 0;
        }
        return this.getFCD16FromNormData(c);
    }

    public boolean singleLeadMightHaveNonZeroFCD16(int lead) {
        byte bits = this.smallFCD[lead >> 8];
        if (bits == 0) {
            return false;
        }
        return (bits >> (lead >> 5 & 7) & 1) != 0;
    }

    public int getFCD16FromNormData(int c) {
        int norm16 = this.getNorm16(c);
        if (norm16 >= this.limitNoNo) {
            if (norm16 >= 64512) {
                norm16 = NormalizerImpl.getCCFromNormalYesOrMaybe(norm16);
                return norm16 | norm16 << 8;
            }
            if (norm16 >= this.minMaybeYes) {
                return 0;
            }
            int deltaTrailCC = norm16 & 6;
            if (deltaTrailCC <= 2) {
                return deltaTrailCC >> 1;
            }
            c = this.mapAlgorithmic(c, norm16);
            norm16 = this.getRawNorm16(c);
        }
        if (norm16 <= this.minYesNo || this.isHangulLVT(norm16)) {
            return 0;
        }
        int mapping = norm16 >> 1;
        char firstUnit = this.extraData.charAt(mapping);
        int fcd16 = firstUnit >> 8;
        if ((firstUnit & 0x80) != 0) {
            fcd16 |= this.extraData.charAt(mapping - 1) & 0xFF00;
        }
        return fcd16;
    }

    public String getDecomposition(int c) {
        int norm16;
        if (c < this.minDecompNoCP || this.isMaybeOrNonZeroCC(norm16 = this.getNorm16(c))) {
            return null;
        }
        int decomp = -1;
        if (this.isDecompNoAlgorithmic(norm16)) {
            decomp = c = this.mapAlgorithmic(c, norm16);
            norm16 = this.getRawNorm16(c);
        }
        if (norm16 < this.minYesNo) {
            if (decomp < 0) {
                return null;
            }
            return UTF16.valueOf(decomp);
        }
        if (this.isHangulLV(norm16) || this.isHangulLVT(norm16)) {
            StringBuilder buffer = new StringBuilder();
            Hangul.decompose(c, buffer);
            return buffer.toString();
        }
        int mapping = norm16 >> 1;
        int length = this.extraData.charAt(mapping++) & 0x1F;
        return this.extraData.substring(mapping, mapping + length);
    }

    public void decompose(CharSequence s, int src, int limit, StringBuilder dest, int destLengthEstimate) {
        if (destLengthEstimate < 0) {
            destLengthEstimate = limit - src;
        }
        dest.setLength(0);
        ReorderingBuffer buffer = new ReorderingBuffer(this, dest, destLengthEstimate);
        this.decompose(s, src, limit, buffer);
    }

    public int decompose(CharSequence s, int src, int limit, ReorderingBuffer buffer) {
        block8: {
            int minNoCP = this.minDecompNoCP;
            int c = 0;
            int norm16 = 0;
            int prevBoundary = src;
            int prevCC = 0;
            while (true) {
                int cc;
                int prevSrc = src;
                while (src != limit) {
                    char c2;
                    c = s.charAt(src);
                    if (c < minNoCP || this.isMostDecompYesAndZeroCC(norm16 = this.normTrie.bmpGet(c))) {
                        ++src;
                        continue;
                    }
                    if (!UTF16Plus.isLeadSurrogate(c)) break;
                    if (src + 1 != limit && Character.isLowSurrogate(c2 = s.charAt(src + 1))) {
                        norm16 = this.normTrie.suppGet(c = Character.toCodePoint((char)c, c2));
                        if (!this.isMostDecompYesAndZeroCC(norm16)) break;
                        src += 2;
                        continue;
                    }
                    ++src;
                }
                if (src != prevSrc) {
                    if (buffer != null) {
                        buffer.flushAndAppendZeroCC(s, prevSrc, src);
                    } else {
                        prevCC = 0;
                        prevBoundary = src;
                    }
                }
                if (src == limit) break block8;
                src += Character.charCount(c);
                if (buffer != null) {
                    this.decompose(c, norm16, buffer);
                    continue;
                }
                if (!this.isDecompYes(norm16) || prevCC > (cc = NormalizerImpl.getCCFromYesOrMaybe(norm16)) && cc != 0) break;
                prevCC = cc;
                if (cc > 1) continue;
                prevBoundary = src;
            }
            return prevBoundary;
        }
        return src;
    }

    public void decomposeAndAppend(CharSequence s, boolean doDecompose, ReorderingBuffer buffer) {
        int cc;
        int limit = s.length();
        if (limit == 0) {
            return;
        }
        if (doDecompose) {
            this.decompose(s, 0, limit, buffer);
            return;
        }
        int c = Character.codePointAt(s, 0);
        int src = 0;
        int prevCC = cc = this.getCC(this.getNorm16(c));
        int firstCC = cc;
        while (cc != 0) {
            prevCC = cc;
            if ((src += Character.charCount(c)) >= limit) break;
            c = Character.codePointAt(s, src);
            cc = this.getCC(this.getNorm16(c));
        }
        buffer.append(s, 0, src, false, firstCC, prevCC);
        buffer.append(s, src, limit);
    }

    public boolean compose(CharSequence s, int src, int limit, boolean onlyContiguous, boolean doCompose, ReorderingBuffer buffer) {
        int prevBoundary = src;
        int minNoMaybeCP = this.minCompNoMaybeCP;
        while (true) {
            int prevSrc;
            int c = 0;
            int norm16 = 0;
            while (true) {
                char c2;
                if (src == limit) {
                    if (prevBoundary != limit && doCompose) {
                        buffer.append(s, prevBoundary, limit);
                    }
                    return true;
                }
                c = s.charAt(src);
                if (c < minNoMaybeCP || this.isCompYesAndZeroCC(norm16 = this.normTrie.bmpGet(c))) {
                    ++src;
                    continue;
                }
                prevSrc = src++;
                if (!UTF16Plus.isLeadSurrogate(c)) break;
                if (src == limit || !Character.isLowSurrogate(c2 = s.charAt(src))) continue;
                ++src;
                norm16 = this.normTrie.suppGet(c = Character.toCodePoint((char)c, c2));
                if (!this.isCompYesAndZeroCC(norm16)) break;
            }
            if (!this.isMaybeOrNonZeroCC(norm16)) {
                if (!doCompose) {
                    return false;
                }
                if (this.isDecompNoAlgorithmic(norm16)) {
                    if (this.norm16HasCompBoundaryAfter(norm16, onlyContiguous) || this.hasCompBoundaryBefore(s, src, limit)) {
                        if (prevBoundary != prevSrc) {
                            buffer.append(s, prevBoundary, prevSrc);
                        }
                        buffer.append(this.mapAlgorithmic(c, norm16), 0);
                        prevBoundary = src;
                        continue;
                    }
                } else if (norm16 < this.minNoNoCompBoundaryBefore) {
                    if (this.norm16HasCompBoundaryAfter(norm16, onlyContiguous) || this.hasCompBoundaryBefore(s, src, limit)) {
                        if (prevBoundary != prevSrc) {
                            buffer.append(s, prevBoundary, prevSrc);
                        }
                        int mapping = norm16 >> 1;
                        int length = this.extraData.charAt(mapping++) & 0x1F;
                        buffer.append(this.extraData, mapping, mapping + length);
                        prevBoundary = src;
                        continue;
                    }
                } else if (norm16 >= this.minNoNoEmpty && (this.hasCompBoundaryBefore(s, src, limit) || this.hasCompBoundaryAfter(s, prevBoundary, prevSrc, onlyContiguous))) {
                    if (prevBoundary != prevSrc) {
                        buffer.append(s, prevBoundary, prevSrc);
                    }
                    prevBoundary = src;
                    continue;
                }
            } else if (NormalizerImpl.isJamoVT(norm16) && prevBoundary != prevSrc) {
                char prev = s.charAt(prevSrc - 1);
                if (c < 4519) {
                    char l = (char)(prev - 4352);
                    if (l < '\u0013') {
                        int t;
                        if (!doCompose) {
                            return false;
                        }
                        if (src != limit && 0 < (t = s.charAt(src) - 4519) && t < 28) {
                            ++src;
                        } else {
                            t = this.hasCompBoundaryBefore(s, src, limit) ? 0 : -1;
                        }
                        if (t >= 0) {
                            int syllable = 44032 + (l * 21 + (c - 4449)) * 28 + t;
                            if (prevBoundary != --prevSrc) {
                                buffer.append(s, prevBoundary, prevSrc);
                            }
                            buffer.append((char)syllable);
                            prevBoundary = src;
                            continue;
                        }
                    }
                } else if (Hangul.isHangulLV(prev)) {
                    if (!doCompose) {
                        return false;
                    }
                    int syllable = prev + c - 4519;
                    if (prevBoundary != --prevSrc) {
                        buffer.append(s, prevBoundary, prevSrc);
                    }
                    buffer.append((char)syllable);
                    prevBoundary = src;
                    continue;
                }
            } else if (norm16 > 65024) {
                int cc = NormalizerImpl.getCCFromNormalYesOrMaybe(norm16);
                if (onlyContiguous && this.getPreviousTrailCC(s, prevBoundary, prevSrc) > cc) {
                    if (!doCompose) {
                        return false;
                    }
                } else {
                    int n16;
                    while (true) {
                        if (src == limit) {
                            if (doCompose) {
                                buffer.append(s, prevBoundary, limit);
                            }
                            return true;
                        }
                        int prevCC = cc;
                        c = Character.codePointAt(s, src);
                        n16 = this.normTrie.get(c);
                        if (n16 < 65026) break;
                        cc = NormalizerImpl.getCCFromNormalYesOrMaybe(n16);
                        if (prevCC > cc) {
                            if (doCompose) break;
                            return false;
                        }
                        src += Character.charCount(c);
                    }
                    if (this.norm16HasCompBoundaryBefore(n16)) {
                        if (!this.isCompYesAndZeroCC(n16)) continue;
                        src += Character.charCount(c);
                        continue;
                    }
                }
            }
            if (prevBoundary != prevSrc && !this.norm16HasCompBoundaryBefore(norm16) && !this.norm16HasCompBoundaryAfter(norm16 = this.normTrie.get(c = Character.codePointBefore(s, prevSrc)), onlyContiguous)) {
                prevSrc -= Character.charCount(c);
            }
            if (doCompose && prevBoundary != prevSrc) {
                buffer.append(s, prevBoundary, prevSrc);
            }
            int recomposeStartIndex = buffer.length();
            this.decomposeShort(s, prevSrc, src, false, onlyContiguous, buffer);
            src = this.decomposeShort(s, src, limit, true, onlyContiguous, buffer);
            this.recompose(buffer, recomposeStartIndex, onlyContiguous);
            if (!doCompose) {
                if (!buffer.equals(s, prevSrc, src)) {
                    return false;
                }
                buffer.remove();
            }
            prevBoundary = src;
        }
    }

    public int composeQuickCheck(CharSequence s, int src, int limit, boolean onlyContiguous, boolean doSpan) {
        int qcResult = 0;
        int prevBoundary = src;
        int minNoMaybeCP = this.minCompNoMaybeCP;
        while (true) {
            int prevSrc;
            int c = 0;
            int norm16 = 0;
            while (true) {
                char c2;
                if (src == limit) {
                    return src << 1 | qcResult;
                }
                c = s.charAt(src);
                if (c < minNoMaybeCP || this.isCompYesAndZeroCC(norm16 = this.normTrie.bmpGet(c))) {
                    ++src;
                    continue;
                }
                prevSrc = src++;
                if (!UTF16Plus.isLeadSurrogate(c)) break;
                if (src == limit || !Character.isLowSurrogate(c2 = s.charAt(src))) continue;
                ++src;
                norm16 = this.normTrie.suppGet(c = Character.toCodePoint((char)c, c2));
                if (!this.isCompYesAndZeroCC(norm16)) break;
            }
            int prevNorm16 = 1;
            if (prevBoundary != prevSrc) {
                int n16;
                prevBoundary = prevSrc;
                if (!this.norm16HasCompBoundaryBefore(norm16) && !this.norm16HasCompBoundaryAfter(n16 = this.getNorm16(c = Character.codePointBefore(s, prevSrc)), onlyContiguous)) {
                    prevBoundary -= Character.charCount(c);
                    prevNorm16 = n16;
                }
            }
            if (!this.isMaybeOrNonZeroCC(norm16)) break;
            int cc = NormalizerImpl.getCCFromYesOrMaybe(norm16);
            if (onlyContiguous && cc != 0 && this.getTrailCCFromCompYesAndZeroCC(prevNorm16) > cc) break;
            while (true) {
                if (norm16 < 65026) {
                    if (!doSpan) {
                        qcResult = 1;
                    } else {
                        return prevBoundary << 1;
                    }
                }
                if (src == limit) {
                    return src << 1 | qcResult;
                }
                int prevCC = cc;
                c = Character.codePointAt(s, src);
                norm16 = this.getNorm16(c);
                if (!this.isMaybeOrNonZeroCC(norm16) || prevCC > (cc = NormalizerImpl.getCCFromYesOrMaybe(norm16)) && cc != 0) break;
                src += Character.charCount(c);
            }
            if (!this.isCompYesAndZeroCC(norm16)) break;
            prevBoundary = src;
            src += Character.charCount(c);
        }
        return prevBoundary << 1;
    }

    public void composeAndAppend(CharSequence s, boolean doCompose, boolean onlyContiguous, ReorderingBuffer buffer) {
        int firstStarterInSrc;
        int src = 0;
        int limit = s.length();
        if (!buffer.isEmpty() && 0 != (firstStarterInSrc = this.findNextCompBoundary(s, 0, limit, onlyContiguous))) {
            int lastStarterInDest = this.findPreviousCompBoundary(buffer.getStringBuilder(), buffer.length(), onlyContiguous);
            StringBuilder middle = new StringBuilder(buffer.length() - lastStarterInDest + firstStarterInSrc + 16);
            middle.append(buffer.getStringBuilder(), lastStarterInDest, buffer.length());
            buffer.removeSuffix(buffer.length() - lastStarterInDest);
            middle.append(s, 0, firstStarterInSrc);
            this.compose(middle, 0, middle.length(), onlyContiguous, true, buffer);
            src = firstStarterInSrc;
        }
        if (doCompose) {
            this.compose(s, src, limit, onlyContiguous, true, buffer);
        } else {
            buffer.append(s, src, limit);
        }
    }

    public int makeFCD(CharSequence s, int src, int limit, ReorderingBuffer buffer) {
        int prevBoundary = src;
        int c = 0;
        int prevFCD16 = 0;
        int fcd16 = 0;
        while (true) {
            int prevSrc = src;
            while (src != limit) {
                char c2;
                c = s.charAt(src);
                if (c < this.minLcccCP) {
                    prevFCD16 = ~c;
                    ++src;
                    continue;
                }
                if (!this.singleLeadMightHaveNonZeroFCD16(c)) {
                    prevFCD16 = 0;
                    ++src;
                    continue;
                }
                if (UTF16Plus.isLeadSurrogate(c) && src + 1 != limit && Character.isLowSurrogate(c2 = s.charAt(src + 1))) {
                    c = Character.toCodePoint((char)c, c2);
                }
                if ((fcd16 = this.getFCD16FromNormData(c)) > 255) break;
                prevFCD16 = fcd16;
                src += Character.charCount(c);
            }
            if (src != prevSrc) {
                if (src == limit) {
                    if (buffer == null) break;
                    buffer.flushAndAppendZeroCC(s, prevSrc, src);
                    break;
                }
                prevBoundary = src;
                if (prevFCD16 < 0) {
                    int prev = ~prevFCD16;
                    if (prev < this.minDecompNoCP) {
                        prevFCD16 = 0;
                    } else {
                        prevFCD16 = this.getFCD16FromNormData(prev);
                        if (prevFCD16 > 1) {
                            --prevBoundary;
                        }
                    }
                } else {
                    int p = src - 1;
                    if (Character.isLowSurrogate(s.charAt(p)) && prevSrc < p && Character.isHighSurrogate(s.charAt(p - 1))) {
                        prevFCD16 = this.getFCD16FromNormData(Character.toCodePoint(s.charAt(--p), s.charAt(p + 1)));
                    }
                    if (prevFCD16 > 1) {
                        prevBoundary = p;
                    }
                }
                if (buffer != null) {
                    buffer.flushAndAppendZeroCC(s, prevSrc, prevBoundary);
                    buffer.append(s, prevBoundary, src);
                }
                prevSrc = src;
            } else if (src == limit) break;
            src += Character.charCount(c);
            if ((prevFCD16 & 0xFF) <= fcd16 >> 8) {
                if ((fcd16 & 0xFF) <= 1) {
                    prevBoundary = src;
                }
                if (buffer != null) {
                    buffer.appendZeroCC(c);
                }
                prevFCD16 = fcd16;
                continue;
            }
            if (buffer == null) {
                return prevBoundary;
            }
            buffer.removeSuffix(prevSrc - prevBoundary);
            src = this.findNextFCDBoundary(s, src, limit);
            this.decomposeShort(s, prevBoundary, src, false, false, buffer);
            prevBoundary = src;
            prevFCD16 = 0;
        }
        return src;
    }

    public boolean hasDecompBoundaryBefore(int c) {
        return c < this.minLcccCP || c <= 65535 && !this.singleLeadMightHaveNonZeroFCD16(c) || this.norm16HasDecompBoundaryBefore(this.getNorm16(c));
    }

    public boolean norm16HasDecompBoundaryBefore(int norm16) {
        if (norm16 < this.minNoNoCompNoMaybeCC) {
            return true;
        }
        if (norm16 >= this.limitNoNo) {
            return norm16 <= 64512 || norm16 == 65024;
        }
        int mapping = norm16 >> 1;
        char firstUnit = this.extraData.charAt(mapping);
        return (firstUnit & 0x80) == 0 || (this.extraData.charAt(mapping - 1) & 0xFF00) == 0;
    }

    public boolean hasDecompBoundaryAfter(int c) {
        if (c < this.minDecompNoCP) {
            return true;
        }
        if (c <= 65535 && !this.singleLeadMightHaveNonZeroFCD16(c)) {
            return true;
        }
        return this.norm16HasDecompBoundaryAfter(this.getNorm16(c));
    }

    public boolean norm16HasDecompBoundaryAfter(int norm16) {
        if (norm16 <= this.minYesNo || this.isHangulLVT(norm16)) {
            return true;
        }
        if (norm16 >= this.limitNoNo) {
            if (this.isMaybeOrNonZeroCC(norm16)) {
                return norm16 <= 64512 || norm16 == 65024;
            }
            return (norm16 & 6) <= 2;
        }
        int mapping = norm16 >> 1;
        char firstUnit = this.extraData.charAt(mapping);
        if (firstUnit > '\u01ff') {
            return false;
        }
        if (firstUnit <= '\u00ff') {
            return true;
        }
        return (firstUnit & 0x80) == 0 || (this.extraData.charAt(mapping - 1) & 0xFF00) == 0;
    }

    public boolean isDecompInert(int c) {
        return this.isDecompYesAndZeroCC(this.getNorm16(c));
    }

    public boolean hasCompBoundaryBefore(int c) {
        return c < this.minCompNoMaybeCP || this.norm16HasCompBoundaryBefore(this.getNorm16(c));
    }

    public boolean hasCompBoundaryAfter(int c, boolean onlyContiguous) {
        return this.norm16HasCompBoundaryAfter(this.getNorm16(c), onlyContiguous);
    }

    private boolean isMaybe(int norm16) {
        return this.minMaybeYes <= norm16 && norm16 <= 65024;
    }

    private boolean isMaybeOrNonZeroCC(int norm16) {
        return norm16 >= this.minMaybeYes;
    }

    private static boolean isInert(int norm16) {
        return norm16 == 1;
    }

    private static boolean isJamoVT(int norm16) {
        return norm16 == 65024;
    }

    private int hangulLVT() {
        return this.minYesNoMappingsOnly | 1;
    }

    private boolean isHangulLV(int norm16) {
        return norm16 == this.minYesNo;
    }

    private boolean isHangulLVT(int norm16) {
        return norm16 == this.hangulLVT();
    }

    private boolean isCompYesAndZeroCC(int norm16) {
        return norm16 < this.minNoNo;
    }

    private boolean isDecompYesAndZeroCC(int norm16) {
        return norm16 < this.minYesNo || norm16 == 65024 || this.minMaybeYes <= norm16 && norm16 <= 64512;
    }

    private boolean isMostDecompYesAndZeroCC(int norm16) {
        return norm16 < this.minYesNo || norm16 == 64512 || norm16 == 65024;
    }

    private boolean isDecompNoAlgorithmic(int norm16) {
        return norm16 >= this.limitNoNo;
    }

    private int getCCFromNoNo(int norm16) {
        int mapping = norm16 >> 1;
        if ((this.extraData.charAt(mapping) & 0x80) != 0) {
            return this.extraData.charAt(mapping - 1) & 0xFF;
        }
        return 0;
    }

    int getTrailCCFromCompYesAndZeroCC(int norm16) {
        if (norm16 <= this.minYesNo) {
            return 0;
        }
        return this.extraData.charAt(norm16 >> 1) >> 8;
    }

    private int mapAlgorithmic(int c, int norm16) {
        return c + (norm16 >> 3) - this.centerNoNoDelta;
    }

    private int getCompositionsListForDecompYes(int norm16) {
        if (norm16 < 2 || 64512 <= norm16) {
            return -1;
        }
        if ((norm16 -= this.minMaybeYes) < 0) {
            norm16 += 64512;
        }
        return norm16 >> 1;
    }

    private int getCompositionsListForComposite(int norm16) {
        int list = 64512 - this.minMaybeYes + norm16 >> 1;
        char firstUnit = this.maybeYesCompositions.charAt(list);
        return list + 1 + (firstUnit & 0x1F);
    }

    private int decomposeShort(CharSequence s, int src, int limit, boolean stopAtCompBoundary, boolean onlyContiguous, ReorderingBuffer buffer) {
        while (src < limit) {
            int c = Character.codePointAt(s, src);
            if (stopAtCompBoundary && c < this.minCompNoMaybeCP) {
                return src;
            }
            int norm16 = this.getNorm16(c);
            if (stopAtCompBoundary && this.norm16HasCompBoundaryBefore(norm16)) {
                return src;
            }
            src += Character.charCount(c);
            this.decompose(c, norm16, buffer);
            if (!stopAtCompBoundary || !this.norm16HasCompBoundaryAfter(norm16, onlyContiguous)) continue;
            return src;
        }
        return src;
    }

    private void decompose(int c, int norm16, ReorderingBuffer buffer) {
        if (norm16 >= this.limitNoNo) {
            if (this.isMaybeOrNonZeroCC(norm16)) {
                buffer.append(c, NormalizerImpl.getCCFromYesOrMaybe(norm16));
                return;
            }
            c = this.mapAlgorithmic(c, norm16);
            norm16 = this.getRawNorm16(c);
        }
        if (norm16 < this.minYesNo) {
            buffer.append(c, 0);
        } else if (this.isHangulLV(norm16) || this.isHangulLVT(norm16)) {
            Hangul.decompose(c, buffer);
        } else {
            int mapping = norm16 >> 1;
            char firstUnit = this.extraData.charAt(mapping);
            int length = firstUnit & 0x1F;
            int trailCC = firstUnit >> 8;
            int leadCC = (firstUnit & 0x80) != 0 ? this.extraData.charAt(mapping - 1) >> 8 : 0;
            buffer.append(this.extraData, ++mapping, mapping + length, true, leadCC, trailCC);
        }
    }

    private static int combine(String compositions, int list, int trail) {
        block9: {
            if (trail < 13312) {
                char firstUnit;
                int key1 = trail << 1;
                while (key1 > (firstUnit = compositions.charAt(list))) {
                    list += 2 + (firstUnit & '\u0001');
                }
                if (key1 == (firstUnit & 0x7FFE)) {
                    if ((firstUnit & '\u0001') != 0) {
                        return compositions.charAt(list + 1) << 16 | compositions.charAt(list + 2);
                    }
                    return compositions.charAt(list + 1);
                }
            } else {
                char secondUnit;
                int key1 = 13312 + (trail >> 9 & 0xFFFFFFFE);
                int key2 = trail << 6 & 0xFFFF;
                while (true) {
                    char firstUnit;
                    if (key1 > (firstUnit = compositions.charAt(list))) {
                        list += 2 + (firstUnit & '\u0001');
                        continue;
                    }
                    if (key1 != (firstUnit & 0x7FFE)) break block9;
                    secondUnit = compositions.charAt(list + 1);
                    if (key2 <= secondUnit) break;
                    if ((firstUnit & 0x8000) == 0) {
                        list += 3;
                        continue;
                    }
                    break block9;
                    break;
                }
                if (key2 == (secondUnit & 0xFFC0)) {
                    return (secondUnit & 0xFFFF003F) << 16 | compositions.charAt(list + 2);
                }
            }
        }
        return -1;
    }

    private void recompose(ReorderingBuffer buffer, int recomposeStartIndex, boolean onlyContiguous) {
        int p = recomposeStartIndex;
        StringBuilder sb = buffer.getStringBuilder();
        if (p == sb.length()) {
            return;
        }
        int compositionsList = -1;
        int starter = -1;
        boolean starterIsSupplementary = false;
        int prevCC = 0;
        while (true) {
            int c = sb.codePointAt(p);
            p += Character.charCount(c);
            int norm16 = this.getNorm16(c);
            int cc = NormalizerImpl.getCCFromYesOrMaybe(norm16);
            if (this.isMaybe(norm16) && compositionsList >= 0 && (prevCC < cc || prevCC == 0)) {
                int pRemove;
                if (NormalizerImpl.isJamoVT(norm16)) {
                    char prev;
                    if (c < 4519 && (prev = (char)(sb.charAt(starter) - 4352)) < '\u0013') {
                        char t;
                        pRemove = p - 1;
                        char syllable = (char)(44032 + (prev * 21 + (c - 4449)) * 28);
                        if (p != sb.length() && (t = (char)(sb.charAt(p) - 4519)) < '\u001c') {
                            ++p;
                            syllable = (char)(syllable + t);
                        }
                        sb.setCharAt(starter, syllable);
                        sb.delete(pRemove, p);
                        p = pRemove;
                    }
                    if (p == sb.length()) break;
                    compositionsList = -1;
                    continue;
                }
                int compositeAndFwd = NormalizerImpl.combine(this.maybeYesCompositions, compositionsList, c);
                if (compositeAndFwd >= 0) {
                    int composite = compositeAndFwd >> 1;
                    pRemove = p - Character.charCount(c);
                    sb.delete(pRemove, p);
                    p = pRemove;
                    if (starterIsSupplementary) {
                        if (composite > 65535) {
                            sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                            sb.setCharAt(starter + 1, UTF16.getTrailSurrogate(composite));
                        } else {
                            sb.setCharAt(starter, (char)c);
                            sb.deleteCharAt(starter + 1);
                            starterIsSupplementary = false;
                            --p;
                        }
                    } else if (composite > 65535) {
                        starterIsSupplementary = true;
                        sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                        sb.insert(starter + 1, UTF16.getTrailSurrogate(composite));
                        ++p;
                    } else {
                        sb.setCharAt(starter, (char)composite);
                    }
                    if (p == sb.length()) break;
                    if ((compositeAndFwd & 1) != 0) {
                        compositionsList = this.getCompositionsListForComposite(this.getRawNorm16(composite));
                        continue;
                    }
                    compositionsList = -1;
                    continue;
                }
            }
            prevCC = cc;
            if (p == sb.length()) break;
            if (cc == 0) {
                compositionsList = this.getCompositionsListForDecompYes(norm16);
                if (compositionsList < 0) continue;
                if (c <= 65535) {
                    starterIsSupplementary = false;
                    starter = p - 1;
                    continue;
                }
                starterIsSupplementary = true;
                starter = p - 2;
                continue;
            }
            if (!onlyContiguous) continue;
            compositionsList = -1;
        }
        buffer.flush();
    }

    private boolean hasCompBoundaryBefore(int c, int norm16) {
        return c < this.minCompNoMaybeCP || this.norm16HasCompBoundaryBefore(norm16);
    }

    private boolean norm16HasCompBoundaryBefore(int norm16) {
        return norm16 < this.minNoNoCompNoMaybeCC || this.isAlgorithmicNoNo(norm16);
    }

    private boolean hasCompBoundaryBefore(CharSequence s, int src, int limit) {
        return src == limit || this.hasCompBoundaryBefore(Character.codePointAt(s, src));
    }

    private boolean norm16HasCompBoundaryAfter(int norm16, boolean onlyContiguous) {
        return (norm16 & 1) != 0 && (!onlyContiguous || this.isTrailCC01ForCompBoundaryAfter(norm16));
    }

    private boolean hasCompBoundaryAfter(CharSequence s, int start, int p, boolean onlyContiguous) {
        return start == p || this.hasCompBoundaryAfter(Character.codePointBefore(s, p), onlyContiguous);
    }

    private boolean isTrailCC01ForCompBoundaryAfter(int norm16) {
        return NormalizerImpl.isInert(norm16) || (this.isDecompNoAlgorithmic(norm16) ? (norm16 & 6) <= 2 : this.extraData.charAt(norm16 >> 1) <= '\u01ff');
    }

    private int findPreviousCompBoundary(CharSequence s, int p, boolean onlyContiguous) {
        int c;
        int norm16;
        while (p > 0 && !this.norm16HasCompBoundaryAfter(norm16 = this.getNorm16(c = Character.codePointBefore(s, p)), onlyContiguous)) {
            p -= Character.charCount(c);
            if (!this.hasCompBoundaryBefore(c, norm16)) continue;
            break;
        }
        return p;
    }

    private int findNextCompBoundary(CharSequence s, int p, int limit, boolean onlyContiguous) {
        int norm16;
        int c;
        while (p < limit && !this.hasCompBoundaryBefore(c = Character.codePointAt(s, p), norm16 = this.normTrie.get(c))) {
            p += Character.charCount(c);
            if (!this.norm16HasCompBoundaryAfter(norm16, onlyContiguous)) continue;
            break;
        }
        return p;
    }

    private int findNextFCDBoundary(CharSequence s, int p, int limit) {
        int norm16;
        int c;
        while (p < limit && (c = Character.codePointAt(s, p)) >= this.minLcccCP && !this.norm16HasDecompBoundaryBefore(norm16 = this.getNorm16(c))) {
            p += Character.charCount(c);
            if (!this.norm16HasDecompBoundaryAfter(norm16)) continue;
            break;
        }
        return p;
    }

    public static int getDecompose(int[] chars, String[] decomps) {
        Normalizer2 impl = Normalizer2.getNFDInstance();
        boolean length = false;
        boolean norm16 = false;
        int ch = -1;
        int i = 0;
        while (++ch < 195102) {
            String s;
            if (ch == 12543) {
                ch = 63744;
            } else if (ch == 71100) {
                ch = 119134;
            } else if (ch == 119233) {
                ch = 194560;
            }
            if ((s = impl.getDecomposition(ch)) == null || i >= chars.length) continue;
            chars[i] = ch;
            decomps[i++] = s;
        }
        return i;
    }

    private static boolean needSingleQuotation(char c) {
        return c >= '\t' && c <= '\r' || c >= ' ' && c <= '/' || c >= ':' && c <= '@' || c >= '[' && c <= '`' || c >= '{' && c <= '~';
    }

    public static String canonicalDecomposeWithSingleQuotation(String string) {
        Normalizer2 impl = Normalizer2.getNFDInstance();
        char[] src = string.toCharArray();
        int srcIndex = 0;
        int srcLimit = src.length;
        char[] dest = new char[src.length * 3];
        int destIndex = 0;
        int destLimit = dest.length;
        char minNoMaybe = '\u00c0';
        int reorderStartIndex = 0;
        int prevCC = 0;
        String norm = null;
        int cp = 0;
        int pStart = 0;
        int trailCC = -1;
        int cc = -1;
        char c1 = '\u0000';
        while (true) {
            char[] p;
            char c2;
            int length;
            int prevSrc = srcIndex;
            while (srcIndex != srcLimit && ((c1 = src[srcIndex]) < minNoMaybe || (norm = impl.getDecomposition(cp = string.codePointAt(srcIndex))) == null || c1 >= '\uac00' && c1 <= '\ud7a3')) {
                prevCC = 0;
                srcIndex += cp < 65536 ? 1 : 2;
            }
            if (srcIndex != prevSrc) {
                length = srcIndex - prevSrc;
                if (destIndex + length <= destLimit) {
                    System.arraycopy(src, prevSrc, dest, destIndex, length);
                }
                reorderStartIndex = destIndex += length;
            }
            if (srcIndex == srcLimit) break;
            srcIndex += cp < 65536 ? 1 : 2;
            if (cp < 65536) {
                c2 = '\u0000';
                length = 1;
                if (Character.isHighSurrogate(c1) || Character.isLowSurrogate(c1)) {
                    norm = null;
                }
            } else {
                length = 2;
                c2 = src[srcIndex - 1];
            }
            if (norm == null) {
                cc = trailCC = UCharacter.getCombiningClass(cp);
                p = null;
                pStart = -1;
            } else {
                pStart = 0;
                p = norm.toCharArray();
                length = p.length;
                int cpNum = norm.codePointCount(0, length);
                cc = UCharacter.getCombiningClass(norm.codePointAt(0));
                trailCC = UCharacter.getCombiningClass(norm.codePointAt(cpNum - 1));
                if (length == 1) {
                    c1 = p[pStart];
                    c2 = '\u0000';
                    p = null;
                    pStart = -1;
                }
            }
            if (destIndex + length * 3 >= destLimit) {
                char[] tmpBuf = new char[destLimit * 2];
                System.arraycopy(dest, 0, tmpBuf, 0, destIndex);
                dest = tmpBuf;
                destLimit = dest.length;
            }
            int reorderSplit = destIndex;
            if (p == null) {
                if (NormalizerImpl.needSingleQuotation(c1)) {
                    dest[destIndex++] = 39;
                    dest[destIndex++] = c1;
                    dest[destIndex++] = 39;
                    trailCC = 0;
                } else if (cc != 0 && cc < prevCC) {
                    trailCC = NormalizerImpl.insertOrdered(dest, reorderStartIndex, reorderSplit, destIndex += length, c1, c2, cc);
                } else {
                    dest[destIndex++] = c1;
                    if (c2 != '\u0000') {
                        dest[destIndex++] = c2;
                    }
                }
            } else if (NormalizerImpl.needSingleQuotation(p[pStart])) {
                dest[destIndex++] = 39;
                dest[destIndex++] = p[pStart++];
                dest[destIndex++] = 39;
                --length;
                do {
                    dest[destIndex++] = p[pStart++];
                } while (--length > 0);
            } else if (cc != 0 && cc < prevCC) {
                destIndex += length;
                trailCC = NormalizerImpl.mergeOrdered(dest, reorderStartIndex, reorderSplit, p, pStart, pStart + length);
            } else {
                do {
                    dest[destIndex++] = p[pStart++];
                } while (--length > 0);
            }
            if ((prevCC = trailCC) != 0) continue;
            reorderStartIndex = destIndex;
        }
        return new String(dest, 0, destIndex);
    }

    private static int insertOrdered(char[] source, int start, int current, int p, char c1, char c2, int cc) {
        int trailCC = cc;
        if (start < current && cc != 0) {
            int back;
            int preBack = back = current;
            PrevArgs prevArgs = new PrevArgs();
            prevArgs.current = current;
            prevArgs.start = start;
            prevArgs.src = source;
            prevArgs.c1 = c1;
            prevArgs.c2 = c2;
            int prevCC = NormalizerImpl.getPrevCC(prevArgs);
            preBack = prevArgs.current;
            if (cc < prevCC) {
                trailCC = prevCC;
                back = preBack;
                while (start < preBack) {
                    prevCC = NormalizerImpl.getPrevCC(prevArgs);
                    preBack = prevArgs.current;
                    if (cc >= prevCC) break;
                    back = preBack;
                }
                int r = p;
                do {
                    source[--r] = source[--current];
                } while (back != current);
            }
        }
        source[current] = c1;
        if (c2 != '\u0000') {
            source[current + 1] = c2;
        }
        return trailCC;
    }

    private static int mergeOrdered(char[] source, int start, int current, char[] data, int next, int limit) {
        int trailCC = 0;
        boolean adjacent = current == next;
        NextCCArgs ncArgs = new NextCCArgs();
        ncArgs.source = data;
        ncArgs.next = next;
        ncArgs.limit = limit;
        if (start != current) {
            while (ncArgs.next < ncArgs.limit) {
                int cc = NormalizerImpl.getNextCC(ncArgs);
                if (cc == 0) {
                    trailCC = 0;
                    if (adjacent) {
                        current = ncArgs.next;
                        break;
                    }
                    data[current++] = ncArgs.c1;
                    if (ncArgs.c2 == '\u0000') break;
                    data[current++] = ncArgs.c2;
                    break;
                }
                int r = current + (ncArgs.c2 == '\u0000' ? 1 : 2);
                trailCC = NormalizerImpl.insertOrdered(source, start, current, r, ncArgs.c1, ncArgs.c2, cc);
                current = r;
            }
        }
        if (ncArgs.next == ncArgs.limit) {
            return trailCC;
        }
        if (!adjacent) {
            do {
                source[current++] = data[ncArgs.next++];
            } while (ncArgs.next != ncArgs.limit);
            ncArgs.limit = current;
        }
        PrevArgs prevArgs = new PrevArgs();
        prevArgs.src = data;
        prevArgs.start = start;
        prevArgs.current = ncArgs.limit;
        return NormalizerImpl.getPrevCC(prevArgs);
    }

    private static int getNextCC(NextCCArgs args) {
        args.c1 = args.source[args.next++];
        args.c2 = '\u0000';
        if (UTF16.isTrailSurrogate(args.c1)) {
            return 0;
        }
        if (!UTF16.isLeadSurrogate(args.c1)) {
            return UCharacter.getCombiningClass(args.c1);
        }
        if (args.next != args.limit && UTF16.isTrailSurrogate(args.c2 = args.source[args.next])) {
            ++args.next;
            return UCharacter.getCombiningClass(Character.toCodePoint(args.c1, args.c2));
        }
        args.c2 = '\u0000';
        return 0;
    }

    private static int getPrevCC(PrevArgs args) {
        args.c1 = args.src[--args.current];
        args.c2 = '\u0000';
        if (args.c1 < '\u0300') {
            return 0;
        }
        if (UTF16.isLeadSurrogate(args.c1)) {
            return 0;
        }
        if (!UTF16.isTrailSurrogate(args.c1)) {
            return UCharacter.getCombiningClass(args.c1);
        }
        if (args.current != args.start && UTF16.isLeadSurrogate(args.c2 = args.src[args.current - 1])) {
            --args.current;
            return UCharacter.getCombiningClass(Character.toCodePoint(args.c2, args.c1));
        }
        args.c2 = '\u0000';
        return 0;
    }

    private int getPreviousTrailCC(CharSequence s, int start, int p) {
        if (start == p) {
            return 0;
        }
        return this.getFCD16(Character.codePointBefore(s, p));
    }

    private static final class IsAcceptable
    implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override
        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 4;
        }
    }

    public static final class UTF16Plus {
        public static boolean isLeadSurrogate(int c) {
            return (c & 0xFFFFFC00) == 55296;
        }

        public static boolean isSurrogateLead(int c) {
            return (c & 0x400) == 0;
        }

        public static boolean equal(CharSequence s1, int start1, int limit1, CharSequence s2, int start2, int limit2) {
            if (limit1 - start1 != limit2 - start2) {
                return false;
            }
            if (s1 == s2 && start1 == start2) {
                return true;
            }
            while (start1 < limit1) {
                if (s1.charAt(start1++) == s2.charAt(start2++)) continue;
                return false;
            }
            return true;
        }
    }

    public static final class Hangul {
        public static final int JAMO_L_BASE = 4352;
        public static final int JAMO_V_BASE = 4449;
        public static final int JAMO_T_BASE = 4519;
        public static final int HANGUL_BASE = 44032;
        public static final int HANGUL_END = 55203;
        public static final int JAMO_L_COUNT = 19;
        public static final int JAMO_V_COUNT = 21;
        public static final int JAMO_T_COUNT = 28;
        public static final int HANGUL_COUNT = 11172;
        public static final int HANGUL_LIMIT = 55204;

        public static boolean isHangul(int c) {
            return 44032 <= c && c < 55204;
        }

        public static boolean isHangulLV(int c) {
            return 0 <= (c -= 44032) && c < 11172 && c % 28 == 0;
        }

        public static int decompose(int c, Appendable buffer) {
            try {
                int c2 = (c -= 44032) % 28;
                buffer.append((char)(4352 + (c /= 28) / 21));
                buffer.append((char)(4449 + c % 21));
                if (c2 == 0) {
                    return 2;
                }
                buffer.append((char)(4519 + c2));
                return 3;
            }
            catch (IOException e) {
                throw new InternalError(e);
            }
        }
    }

    public static final class ReorderingBuffer
    implements Appendable {
        private final NormalizerImpl impl;
        private final Appendable app;
        private final StringBuilder str;
        private final boolean appIsStringBuilder;
        private int reorderStart;
        private int lastCC;
        private int codePointStart;
        private int codePointLimit;

        public ReorderingBuffer(NormalizerImpl ni, Appendable dest, int destCapacity) {
            this.impl = ni;
            this.app = dest;
            if (this.app instanceof StringBuilder) {
                this.appIsStringBuilder = true;
                this.str = (StringBuilder)dest;
                this.str.ensureCapacity(destCapacity);
                this.reorderStart = 0;
                if (this.str.length() == 0) {
                    this.lastCC = 0;
                } else {
                    this.setIterator();
                    this.lastCC = this.previousCC();
                    if (this.lastCC > 1) {
                        while (this.previousCC() > 1) {
                        }
                    }
                    this.reorderStart = this.codePointLimit;
                }
            } else {
                this.appIsStringBuilder = false;
                this.str = new StringBuilder();
                this.reorderStart = 0;
                this.lastCC = 0;
            }
        }

        public boolean isEmpty() {
            return this.str.length() == 0;
        }

        public int length() {
            return this.str.length();
        }

        public int getLastCC() {
            return this.lastCC;
        }

        public StringBuilder getStringBuilder() {
            return this.str;
        }

        public boolean equals(CharSequence s, int start, int limit) {
            return UTF16Plus.equal(this.str, 0, this.str.length(), s, start, limit);
        }

        public void append(int c, int cc) {
            if (this.lastCC <= cc || cc == 0) {
                this.str.appendCodePoint(c);
                this.lastCC = cc;
                if (cc <= 1) {
                    this.reorderStart = this.str.length();
                }
            } else {
                this.insert(c, cc);
            }
        }

        public void append(CharSequence s, int start, int limit, boolean isNFD, int leadCC, int trailCC) {
            if (start == limit) {
                return;
            }
            if (this.lastCC <= leadCC || leadCC == 0) {
                if (trailCC <= 1) {
                    this.reorderStart = this.str.length() + (limit - start);
                } else if (leadCC <= 1) {
                    this.reorderStart = this.str.length() + 1;
                }
                this.str.append(s, start, limit);
                this.lastCC = trailCC;
            } else {
                int c = Character.codePointAt(s, start);
                start += Character.charCount(c);
                this.insert(c, leadCC);
                while (start < limit) {
                    leadCC = (start += Character.charCount(c = Character.codePointAt(s, start))) < limit ? (isNFD ? NormalizerImpl.getCCFromYesOrMaybe(this.impl.getNorm16(c)) : this.impl.getCC(this.impl.getNorm16(c))) : trailCC;
                    this.append(c, leadCC);
                }
            }
        }

        @Override
        public ReorderingBuffer append(char c) {
            this.str.append(c);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
            return this;
        }

        public void appendZeroCC(int c) {
            this.str.appendCodePoint(c);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
        }

        @Override
        public ReorderingBuffer append(CharSequence s) {
            if (s.length() != 0) {
                this.str.append(s);
                this.lastCC = 0;
                this.reorderStart = this.str.length();
            }
            return this;
        }

        @Override
        public ReorderingBuffer append(CharSequence s, int start, int limit) {
            if (start != limit) {
                this.str.append(s, start, limit);
                this.lastCC = 0;
                this.reorderStart = this.str.length();
            }
            return this;
        }

        public void flush() {
            if (this.appIsStringBuilder) {
                this.reorderStart = this.str.length();
            } else {
                try {
                    this.app.append(this.str);
                    this.str.setLength(0);
                    this.reorderStart = 0;
                }
                catch (IOException e) {
                    throw new InternalError(e);
                }
            }
            this.lastCC = 0;
        }

        public ReorderingBuffer flushAndAppendZeroCC(CharSequence s, int start, int limit) {
            if (this.appIsStringBuilder) {
                this.str.append(s, start, limit);
                this.reorderStart = this.str.length();
            } else {
                try {
                    this.app.append(this.str).append(s, start, limit);
                    this.str.setLength(0);
                    this.reorderStart = 0;
                }
                catch (IOException e) {
                    throw new InternalError(e);
                }
            }
            this.lastCC = 0;
            return this;
        }

        public void remove() {
            this.str.setLength(0);
            this.lastCC = 0;
            this.reorderStart = 0;
        }

        public void removeSuffix(int suffixLength) {
            int oldLength = this.str.length();
            this.str.delete(oldLength - suffixLength, oldLength);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
        }

        private void insert(int c, int cc) {
            this.setIterator();
            this.skipPrevious();
            while (this.previousCC() > cc) {
            }
            if (c <= 65535) {
                this.str.insert(this.codePointLimit, (char)c);
                if (cc <= 1) {
                    this.reorderStart = this.codePointLimit + 1;
                }
            } else {
                this.str.insert(this.codePointLimit, Character.toChars(c));
                if (cc <= 1) {
                    this.reorderStart = this.codePointLimit + 2;
                }
            }
        }

        private void setIterator() {
            this.codePointStart = this.str.length();
        }

        private void skipPrevious() {
            this.codePointLimit = this.codePointStart;
            this.codePointStart = this.str.offsetByCodePoints(this.codePointStart, -1);
        }

        private int previousCC() {
            this.codePointLimit = this.codePointStart;
            if (this.reorderStart >= this.codePointStart) {
                return 0;
            }
            int c = this.str.codePointBefore(this.codePointStart);
            this.codePointStart -= Character.charCount(c);
            return this.impl.getCCFromYesOrMaybeCP(c);
        }
    }

    private static final class PrevArgs {
        char[] src;
        int start;
        int current;
        char c1;
        char c2;

        private PrevArgs() {
        }
    }

    private static final class NextCCArgs {
        char[] source;
        int next;
        int limit;
        char c1;
        char c2;

        private NextCCArgs() {
        }
    }
}

