/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.common;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.stream.ImageInputStream;

public class ReaderUtil {
    private static void computeUpdatedPixels(int sourceOffset, int sourceExtent, int destinationOffset, int dstMin, int dstMax, int sourceSubsampling, int passStart, int passExtent, int passPeriod, int[] vals, int offset) {
        boolean gotPixel = false;
        int firstDst = -1;
        int secondDst = -1;
        int lastDst = -1;
        for (int i = 0; i < passExtent; ++i) {
            int src = passStart + i * passPeriod;
            if (src < sourceOffset || (src - sourceOffset) % sourceSubsampling != 0) continue;
            if (src >= sourceOffset + sourceExtent) break;
            int dst = destinationOffset + (src - sourceOffset) / sourceSubsampling;
            if (dst < dstMin) continue;
            if (dst > dstMax) break;
            if (!gotPixel) {
                firstDst = dst;
                gotPixel = true;
            } else if (secondDst == -1) {
                secondDst = dst;
            }
            lastDst = dst;
        }
        vals[offset] = firstDst;
        vals[offset + 2] = !gotPixel ? 0 : lastDst - firstDst + 1;
        vals[offset + 4] = Math.max(secondDst - firstDst, 1);
    }

    public static int[] computeUpdatedPixels(Rectangle sourceRegion, Point destinationOffset, int dstMinX, int dstMinY, int dstMaxX, int dstMaxY, int sourceXSubsampling, int sourceYSubsampling, int passXStart, int passYStart, int passWidth, int passHeight, int passPeriodX, int passPeriodY) {
        int[] vals = new int[6];
        ReaderUtil.computeUpdatedPixels(sourceRegion.x, sourceRegion.width, destinationOffset.x, dstMinX, dstMaxX, sourceXSubsampling, passXStart, passWidth, passPeriodX, vals, 0);
        ReaderUtil.computeUpdatedPixels(sourceRegion.y, sourceRegion.height, destinationOffset.y, dstMinY, dstMaxY, sourceYSubsampling, passYStart, passHeight, passPeriodY, vals, 1);
        return vals;
    }

    public static int readMultiByteInteger(ImageInputStream iis) throws IOException {
        byte value = iis.readByte();
        int result = value & 0x7F;
        while ((value & 0x80) == 128) {
            result <<= 7;
            value = iis.readByte();
            result |= value & 0x7F;
        }
        return result;
    }

    public static byte[] staggeredReadByteStream(ImageInputStream iis, int length) throws IOException {
        byte[] decodedData;
        int UNIT_SIZE = 1024000;
        if (length < 1024000) {
            decodedData = new byte[length];
            iis.readFully(decodedData, 0, length);
        } else {
            int sz;
            int bytesRead = 0;
            ArrayList<byte[]> bufs = new ArrayList<byte[]>();
            for (int bytesToRead = length; bytesToRead != 0; bytesToRead -= sz) {
                sz = Math.min(bytesToRead, 1024000);
                byte[] unit = new byte[sz];
                iis.readFully(unit, 0, sz);
                bufs.add(unit);
                bytesRead += sz;
            }
            decodedData = new byte[bytesRead];
            int copiedBytes = 0;
            for (byte[] ba : bufs) {
                System.arraycopy(ba, 0, decodedData, copiedBytes, ba.length);
                copiedBytes += ba.length;
            }
        }
        return decodedData;
    }

    public static boolean tryReadFully(ImageInputStream iis, byte[] b) throws IOException {
        int n;
        int offset = 0;
        do {
            if ((n = iis.read(b, offset, b.length - offset)) >= 0) continue;
            return false;
        } while ((offset += n) < b.length);
        return true;
    }
}

