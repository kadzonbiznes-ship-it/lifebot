/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.impl.plugins.tiff;

import com.github.jaiimageio.plugins.tiff.TIFFCompressor;
import java.io.IOException;

public class TIFFPackBitsCompressor
extends TIFFCompressor {
    public TIFFPackBitsCompressor() {
        super("PackBits", 32773, true);
    }

    private static int packBits(byte[] input, int inOffset, int inCount, byte[] output, int outOffset) {
        int inMax = inOffset + inCount - 1;
        int inMaxMinus1 = inMax - 1;
        while (inOffset <= inMax) {
            int run;
            byte replicate = input[inOffset];
            for (run = 1; run < 127 && inOffset < inMax && input[inOffset] == input[inOffset + 1]; ++run, ++inOffset) {
            }
            if (run > 1) {
                ++inOffset;
                output[outOffset++] = (byte)(-(run - 1));
                output[outOffset++] = replicate;
            }
            int saveOffset = outOffset;
            for (run = 0; run < 128 && (inOffset < inMax && input[inOffset] != input[inOffset + 1] || inOffset < inMaxMinus1 && input[inOffset] != input[inOffset + 2]); ++run) {
                output[++outOffset] = input[inOffset++];
            }
            if (run > 0) {
                output[saveOffset] = (byte)(run - 1);
                ++outOffset;
            }
            if (inOffset != inMax) continue;
            if (run > 0 && run < 128) {
                int n = saveOffset;
                output[n] = (byte)(output[n] + 1);
                output[outOffset++] = input[inOffset++];
                continue;
            }
            output[outOffset++] = 0;
            output[outOffset++] = input[inOffset++];
        }
        return outOffset;
    }

    @Override
    public int encode(byte[] b, int off, int width, int height, int[] bitsPerSample, int scanlineStride) throws IOException {
        int bitsPerPixel = 0;
        for (int i = 0; i < bitsPerSample.length; ++i) {
            bitsPerPixel += bitsPerSample[i];
        }
        int bytesPerRow = (bitsPerPixel * width + 7) / 8;
        int bufSize = bytesPerRow + (bytesPerRow + 127) / 128;
        byte[] compData = new byte[bufSize];
        int bytesWritten = 0;
        for (int i = 0; i < height; ++i) {
            int bytes = TIFFPackBitsCompressor.packBits(b, off, scanlineStride, compData, 0);
            off += scanlineStride;
            bytesWritten += bytes;
            this.stream.write(compData, 0, bytes);
        }
        return bytesWritten;
    }
}

