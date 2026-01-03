/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import sun.awt.image.ImageDecoder;
import sun.awt.image.ImageFormatException;
import sun.awt.image.InputStreamImageSource;
import sun.awt.image.PNGFilterInputStream;

public class PNGImageDecoder
extends ImageDecoder {
    private static final int GRAY = 0;
    private static final int PALETTE = 1;
    private static final int COLOR = 2;
    private static final int ALPHA = 4;
    private static final int bKGDChunk = 1649100612;
    private static final int cHRMChunk = 1665684045;
    private static final int gAMAChunk = 1732332865;
    private static final int hISTChunk = 1749635924;
    private static final int IDATChunk = 1229209940;
    private static final int IENDChunk = 1229278788;
    private static final int IHDRChunk = 1229472850;
    private static final int PLTEChunk = 1347179589;
    private static final int pHYsChunk = 1883789683;
    private static final int sBITChunk = 1933723988;
    private static final int tEXtChunk = 1950701684;
    private static final int tIMEChunk = 1950960965;
    private static final int tRNSChunk = 1951551059;
    private static final int zTXtChunk = 2052348020;
    private int width;
    private int height;
    private int bitDepth;
    private int colorType;
    private int compressionMethod;
    private int filterMethod;
    private int interlaceMethod;
    private int gamma = 100000;
    private Hashtable<String, Object> properties;
    private ColorModel cm;
    private byte[] red_map;
    private byte[] green_map;
    private byte[] blue_map;
    private byte[] alpha_map;
    private int transparentPixel = -1;
    private byte[] transparentPixel_16 = null;
    private static ColorModel[] greyModels = new ColorModel[4];
    private static final byte[] startingRow = new byte[]{0, 0, 0, 4, 0, 2, 0, 1};
    private static final byte[] startingCol = new byte[]{0, 0, 4, 0, 2, 0, 1, 0};
    private static final byte[] rowIncrement = new byte[]{1, 8, 8, 8, 4, 4, 2, 2};
    private static final byte[] colIncrement = new byte[]{1, 8, 8, 4, 4, 2, 2, 1};
    private static final byte[] blockHeight = new byte[]{1, 8, 8, 4, 4, 2, 2, 1};
    private static final byte[] blockWidth = new byte[]{1, 8, 4, 4, 2, 2, 1, 1};
    int pos;
    int limit;
    int chunkStart;
    int chunkKey;
    int chunkLength;
    int chunkCRC;
    boolean seenEOF;
    private static final byte[] signature = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
    PNGFilterInputStream inputStream;
    InputStream underlyingInputStream;
    byte[] inbuf = new byte[4096];
    private static boolean checkCRC = true;
    private static final int[] crc_table = new int[256];

    private void property(String key, Object value) {
        if (value == null) {
            return;
        }
        if (this.properties == null) {
            this.properties = new Hashtable();
        }
        this.properties.put(key, value);
    }

    private void property(String key, float value) {
        this.property(key, Float.valueOf(value));
    }

    private void pngassert(boolean b) throws IOException {
        if (!b) {
            PNGException e = new PNGException("Broken file");
            e.printStackTrace();
            throw e;
        }
    }

    protected boolean handleChunk(int key, byte[] buf, int st, int len) throws IOException {
        block33: {
            block0 : switch (key) {
                case 1649100612: {
                    Color c = null;
                    switch (this.colorType) {
                        case 2: 
                        case 6: {
                            this.pngassert(len == 6);
                            c = new Color(buf[st] & 0xFF, buf[st + 2] & 0xFF, buf[st + 4] & 0xFF);
                            break;
                        }
                        case 3: 
                        case 7: {
                            this.pngassert(len == 1);
                            int ix = buf[st] & 0xFF;
                            this.pngassert(this.red_map != null && ix < this.red_map.length);
                            c = new Color(this.red_map[ix] & 0xFF, this.green_map[ix] & 0xFF, this.blue_map[ix] & 0xFF);
                            break;
                        }
                        case 0: 
                        case 4: {
                            this.pngassert(len == 2);
                            int t = buf[st] & 0xFF;
                            c = new Color(t, t, t);
                        }
                    }
                    if (c == null) break;
                    this.property("background", c);
                    break;
                }
                case 1665684045: {
                    this.property("chromaticities", new Chromaticities(this.getInt(st), this.getInt(st + 4), this.getInt(st + 8), this.getInt(st + 12), this.getInt(st + 16), this.getInt(st + 20), this.getInt(st + 24), this.getInt(st + 28)));
                    break;
                }
                case 1732332865: {
                    if (len != 4) {
                        throw new PNGException("bogus gAMA");
                    }
                    this.gamma = this.getInt(st);
                    if (this.gamma == 100000) break;
                    this.property("gamma", (float)this.gamma / 100000.0f);
                    break;
                }
                case 1749635924: {
                    break;
                }
                case 1229209940: {
                    return false;
                }
                case 1229278788: {
                    break;
                }
                case 1229472850: {
                    if (len != 13 || (this.width = this.getInt(st)) == 0 || (this.height = this.getInt(st + 4)) == 0) {
                        throw new PNGException("bogus IHDR");
                    }
                    this.bitDepth = this.getByte(st + 8);
                    this.colorType = this.getByte(st + 9);
                    this.compressionMethod = this.getByte(st + 10);
                    this.filterMethod = this.getByte(st + 11);
                    this.interlaceMethod = this.getByte(st + 12);
                    break;
                }
                case 1347179589: {
                    int tsize = len / 3;
                    this.red_map = new byte[tsize];
                    this.green_map = new byte[tsize];
                    this.blue_map = new byte[tsize];
                    int i = 0;
                    int j = st;
                    while (i < tsize) {
                        this.red_map[i] = buf[j];
                        this.green_map[i] = buf[j + 1];
                        this.blue_map[i] = buf[j + 2];
                        ++i;
                        j += 3;
                    }
                    break;
                }
                case 1883789683: {
                    break;
                }
                case 1933723988: {
                    break;
                }
                case 1950701684: {
                    int klen;
                    for (klen = 0; klen < len && buf[st + klen] != 0; ++klen) {
                    }
                    if (klen >= len) break;
                    String tkey = new String(buf, st, klen);
                    String tvalue = new String(buf, st + klen + 1, len - klen - 1);
                    this.property(tkey, tvalue);
                    break;
                }
                case 1950960965: {
                    this.property("modtime", new GregorianCalendar(this.getShort(st + 0), this.getByte(st + 2) - 1, this.getByte(st + 3), this.getByte(st + 4), this.getByte(st + 5), this.getByte(st + 6)).getTime());
                    break;
                }
                case 1951551059: {
                    switch (this.colorType) {
                        case 3: 
                        case 7: {
                            int alen = len;
                            if (this.red_map != null) {
                                alen = this.red_map.length;
                            }
                            this.alpha_map = new byte[alen];
                            System.arraycopy(buf, st, this.alpha_map, 0, len < alen ? len : alen);
                            while (--alen >= len) {
                                this.alpha_map[alen] = -1;
                            }
                            break block33;
                        }
                        case 2: 
                        case 6: {
                            this.pngassert(len == 6);
                            if (this.bitDepth == 16) {
                                this.transparentPixel_16 = new byte[6];
                                for (int i = 0; i < 6; ++i) {
                                    this.transparentPixel_16[i] = (byte)this.getByte(st + i);
                                }
                                break block0;
                            }
                            this.transparentPixel = (this.getShort(st + 0) & 0xFF) << 16 | (this.getShort(st + 2) & 0xFF) << 8 | this.getShort(st + 4) & 0xFF;
                            break;
                        }
                        case 0: 
                        case 4: {
                            this.pngassert(len == 2);
                            int t = this.getShort(st);
                            t = 0xFF & (this.bitDepth == 16 ? t >> 8 : t);
                            this.transparentPixel = t << 16 | t << 8 | t;
                        }
                    }
                    break;
                }
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void produceImage() throws IOException, ImageFormatException {
        try {
            int passLimit;
            int pass;
            int rowStride;
            for (int i = 0; i < signature.length; ++i) {
                if ((signature[i] & 0xFF) == this.underlyingInputStream.read()) continue;
                throw new PNGException("Chunk signature mismatch");
            }
            BufferedInputStream is = new BufferedInputStream(new InflaterInputStream(this.inputStream, new Inflater()));
            this.getData();
            byte[] bPixels = null;
            int[] wPixels = null;
            int pixSize = this.width;
            int logDepth = 0;
            switch (this.bitDepth) {
                case 1: {
                    logDepth = 0;
                    break;
                }
                case 2: {
                    logDepth = 1;
                    break;
                }
                case 4: {
                    logDepth = 2;
                    break;
                }
                case 8: {
                    logDepth = 3;
                    break;
                }
                case 16: {
                    logDepth = 4;
                    break;
                }
                default: {
                    throw new PNGException("invalid depth");
                }
            }
            if (this.interlaceMethod != 0) {
                pixSize *= this.height;
                rowStride = this.width;
            } else {
                rowStride = 0;
            }
            int combinedType = this.colorType | this.bitDepth << 3;
            int bitMask = (1 << (this.bitDepth >= 8 ? 8 : this.bitDepth)) - 1;
            switch (this.colorType) {
                case 3: 
                case 7: {
                    if (this.red_map == null) {
                        throw new PNGException("palette expected");
                    }
                    this.cm = this.alpha_map == null ? new IndexColorModel(this.bitDepth, this.red_map.length, this.red_map, this.green_map, this.blue_map) : new IndexColorModel(this.bitDepth, this.red_map.length, this.red_map, this.green_map, this.blue_map, this.alpha_map);
                    bPixels = new byte[pixSize];
                    break;
                }
                case 0: {
                    int llog = logDepth >= 4 ? 3 : logDepth;
                    this.cm = greyModels[llog];
                    if (this.cm == null) {
                        int size = 1 << (1 << llog);
                        byte[] ramp = new byte[size];
                        for (int i = 0; i < size; ++i) {
                            ramp[i] = (byte)(255 * i / (size - 1));
                        }
                        this.cm = this.transparentPixel == -1 ? new IndexColorModel(this.bitDepth, ramp.length, ramp, ramp, ramp) : new IndexColorModel(this.bitDepth, ramp.length, ramp, ramp, ramp, this.transparentPixel & 0xFF);
                        PNGImageDecoder.greyModels[llog] = this.cm;
                    }
                    bPixels = new byte[pixSize];
                    break;
                }
                case 2: 
                case 4: 
                case 6: {
                    this.cm = ColorModel.getRGBdefault();
                    wPixels = new int[pixSize];
                    break;
                }
                default: {
                    throw new PNGException("invalid color type");
                }
            }
            this.setDimensions(this.width, this.height);
            this.setColorModel(this.cm);
            int flags = this.interlaceMethod != 0 ? 6 : 30;
            this.setHints(flags);
            this.headerComplete();
            int samplesPerPixel = (this.colorType & 1) != 0 ? 1 : ((this.colorType & 2) != 0 ? 3 : 1) + ((this.colorType & 4) != 0 ? 1 : 0);
            int bitsPerPixel = samplesPerPixel * this.bitDepth;
            int bytesPerPixel = bitsPerPixel + 7 >> 3;
            if (this.interlaceMethod == 0) {
                pass = -1;
                passLimit = 0;
            } else {
                pass = 0;
                passLimit = 7;
            }
            while (++pass <= passLimit) {
                int row = startingRow[pass];
                byte rowInc = rowIncrement[pass];
                byte colInc = colIncrement[pass];
                byte bWidth = blockWidth[pass];
                byte bHeight = blockHeight[pass];
                int sCol = startingCol[pass];
                int rowPixelWidth = (this.width - sCol + (colInc - 1)) / colInc;
                int rowByteWidth = rowPixelWidth * bitsPerPixel + 7 >> 3;
                if (rowByteWidth == 0) continue;
                int pixelBufferInc = this.interlaceMethod == 0 ? rowInc * this.width : 0;
                int rowOffset = rowStride * row;
                boolean firstRow = true;
                byte[] rowByteBuffer = new byte[rowByteWidth];
                byte[] prevRowByteBuffer = new byte[rowByteWidth];
                while (row < this.height) {
                    int n;
                    int rowFilter = ((InputStream)is).read();
                    for (int rowFillPos = 0; rowFillPos < rowByteWidth; rowFillPos += n) {
                        n = ((InputStream)is).read(rowByteBuffer, rowFillPos, rowByteWidth - rowFillPos);
                        if (n > 0) continue;
                        throw new PNGException("missing data");
                    }
                    this.filterRow(rowByteBuffer, firstRow ? null : prevRowByteBuffer, rowFilter, rowByteWidth, bytesPerPixel);
                    int spos = 0;
                    int pixel = 0;
                    block43: for (int col = sCol; col < this.width; col += colInc) {
                        if (wPixels != null) {
                            switch (combinedType) {
                                case 70: {
                                    wPixels[col + rowOffset] = (rowByteBuffer[spos] & 0xFF) << 16 | (rowByteBuffer[spos + 1] & 0xFF) << 8 | rowByteBuffer[spos + 2] & 0xFF | (rowByteBuffer[spos + 3] & 0xFF) << 24;
                                    spos += 4;
                                    continue block43;
                                }
                                case 134: {
                                    wPixels[col + rowOffset] = (rowByteBuffer[spos] & 0xFF) << 16 | (rowByteBuffer[spos + 2] & 0xFF) << 8 | rowByteBuffer[spos + 4] & 0xFF | (rowByteBuffer[spos + 6] & 0xFF) << 24;
                                    spos += 8;
                                    continue block43;
                                }
                                case 66: {
                                    pixel = (rowByteBuffer[spos] & 0xFF) << 16 | (rowByteBuffer[spos + 1] & 0xFF) << 8 | rowByteBuffer[spos + 2] & 0xFF;
                                    if (pixel != this.transparentPixel) {
                                        pixel |= 0xFF000000;
                                    }
                                    wPixels[col + rowOffset] = pixel;
                                    spos += 3;
                                    continue block43;
                                }
                                case 130: {
                                    pixel = (rowByteBuffer[spos] & 0xFF) << 16 | (rowByteBuffer[spos + 2] & 0xFF) << 8 | rowByteBuffer[spos + 4] & 0xFF;
                                    boolean isTransparent = this.transparentPixel_16 != null;
                                    for (int i = 0; isTransparent && i < 6; isTransparent &= (rowByteBuffer[spos + i] & 0xFF) == (this.transparentPixel_16[i] & 0xFF), ++i) {
                                    }
                                    if (!isTransparent) {
                                        pixel |= 0xFF000000;
                                    }
                                    wPixels[col + rowOffset] = pixel;
                                    spos += 6;
                                    continue block43;
                                }
                                case 68: {
                                    int tx = rowByteBuffer[spos] & 0xFF;
                                    wPixels[col + rowOffset] = tx << 16 | tx << 8 | tx | (rowByteBuffer[spos + 1] & 0xFF) << 24;
                                    spos += 2;
                                    continue block43;
                                }
                                case 132: {
                                    int tx = rowByteBuffer[spos] & 0xFF;
                                    wPixels[col + rowOffset] = tx << 16 | tx << 8 | tx | (rowByteBuffer[spos + 2] & 0xFF) << 24;
                                    spos += 4;
                                    continue block43;
                                }
                                default: {
                                    throw new PNGException("illegal type/depth");
                                }
                            }
                        }
                        switch (this.bitDepth) {
                            case 1: {
                                bPixels[col + rowOffset] = (byte)(rowByteBuffer[spos >> 3] >> 7 - (spos & 7) & 1);
                                ++spos;
                                continue block43;
                            }
                            case 2: {
                                bPixels[col + rowOffset] = (byte)(rowByteBuffer[spos >> 2] >> (3 - (spos & 3)) * 2 & 3);
                                ++spos;
                                continue block43;
                            }
                            case 4: {
                                bPixels[col + rowOffset] = (byte)(rowByteBuffer[spos >> 1] >> (1 - (spos & 1)) * 4 & 0xF);
                                ++spos;
                                continue block43;
                            }
                            case 8: {
                                bPixels[col + rowOffset] = rowByteBuffer[spos++];
                                continue block43;
                            }
                            case 16: {
                                bPixels[col + rowOffset] = rowByteBuffer[spos];
                                spos += 2;
                                continue block43;
                            }
                            default: {
                                throw new PNGException("illegal type/depth");
                            }
                        }
                    }
                    if (this.interlaceMethod == 0) {
                        if (wPixels != null) {
                            this.sendPixels(0, row, this.width, 1, wPixels, 0, this.width);
                        } else {
                            this.sendPixels(0, row, this.width, 1, bPixels, 0, this.width);
                        }
                    }
                    row += rowInc;
                    rowOffset += rowInc * rowStride;
                    byte[] T = rowByteBuffer;
                    rowByteBuffer = prevRowByteBuffer;
                    prevRowByteBuffer = T;
                    firstRow = false;
                }
                if (this.interlaceMethod == 0) continue;
                if (wPixels != null) {
                    this.sendPixels(0, 0, this.width, this.height, wPixels, 0, this.width);
                    continue;
                }
                this.sendPixels(0, 0, this.width, this.height, bPixels, 0, this.width);
            }
            this.imageComplete(3, true);
        }
        catch (IOException e) {
            if (!this.aborted) {
                this.property("error", e);
                this.imageComplete(3, true);
                throw e;
            }
        }
        finally {
            try {
                this.close();
            }
            catch (Throwable throwable) {}
        }
    }

    private boolean sendPixels(int x, int y, int w, int h, int[] pixels, int offset, int pixlength) {
        int count = this.setPixels(x, y, w, h, this.cm, pixels, offset, pixlength);
        if (count <= 0) {
            this.aborted = true;
        }
        return !this.aborted;
    }

    private boolean sendPixels(int x, int y, int w, int h, byte[] pixels, int offset, int pixlength) {
        int count = this.setPixels(x, y, w, h, this.cm, pixels, offset, pixlength);
        if (count <= 0) {
            this.aborted = true;
        }
        return !this.aborted;
    }

    private void filterRow(byte[] rowByteBuffer, byte[] prevRow, int rowFilter, int rowByteWidth, int bytesPerSample) throws IOException {
        switch (rowFilter) {
            case 0: {
                break;
            }
            case 1: {
                int x;
                for (x = bytesPerSample; x < rowByteWidth; ++x) {
                    int n = x;
                    rowByteBuffer[n] = (byte)(rowByteBuffer[n] + rowByteBuffer[x - bytesPerSample]);
                }
                break;
            }
            case 2: {
                int x;
                if (prevRow == null) break;
                for (x = 0; x < rowByteWidth; ++x) {
                    int n = x;
                    rowByteBuffer[n] = (byte)(rowByteBuffer[n] + prevRow[x]);
                }
                break;
            }
            case 3: {
                int x;
                if (prevRow != null) {
                    while (x < bytesPerSample) {
                        int n = x;
                        rowByteBuffer[n] = (byte)(rowByteBuffer[n] + ((0xFF & prevRow[x]) >> 1));
                        ++x;
                    }
                    while (x < rowByteWidth) {
                        int n = x;
                        rowByteBuffer[n] = (byte)(rowByteBuffer[n] + ((prevRow[x] & 0xFF) + (rowByteBuffer[x - bytesPerSample] & 0xFF) >> 1));
                        ++x;
                    }
                } else {
                    for (x = bytesPerSample; x < rowByteWidth; ++x) {
                        int n = x;
                        rowByteBuffer[n] = (byte)(rowByteBuffer[n] + ((rowByteBuffer[x - bytesPerSample] & 0xFF) >> 1));
                    }
                }
                break;
            }
            case 4: {
                int x;
                if (prevRow != null) {
                    while (x < bytesPerSample) {
                        int n = x;
                        rowByteBuffer[n] = (byte)(rowByteBuffer[n] + prevRow[x]);
                        ++x;
                    }
                    while (x < rowByteWidth) {
                        int a = rowByteBuffer[x - bytesPerSample] & 0xFF;
                        int b = prevRow[x] & 0xFF;
                        int c = prevRow[x - bytesPerSample] & 0xFF;
                        int p = a + b - c;
                        int pa = p > a ? p - a : a - p;
                        int pb = p > b ? p - b : b - p;
                        int pc = p > c ? p - c : c - p;
                        int n = x;
                        rowByteBuffer[n] = (byte)(rowByteBuffer[n] + (pa <= pb && pa <= pc ? a : (pb <= pc ? b : c)));
                        ++x;
                    }
                } else {
                    for (x = bytesPerSample; x < rowByteWidth; ++x) {
                        int n = x;
                        rowByteBuffer[n] = (byte)(rowByteBuffer[n] + rowByteBuffer[x - bytesPerSample]);
                    }
                }
                break;
            }
            default: {
                throw new PNGException("Illegal filter");
            }
        }
    }

    public PNGImageDecoder(InputStreamImageSource src, InputStream input) throws IOException {
        super(src, input);
        this.inputStream = new PNGFilterInputStream(this, input);
        this.underlyingInputStream = this.inputStream.underlyingInputStream;
    }

    private void fill() throws IOException {
        if (!this.seenEOF) {
            if (this.pos > 0 && this.pos < this.limit) {
                System.arraycopy(this.inbuf, this.pos, this.inbuf, 0, this.limit - this.pos);
                this.limit -= this.pos;
                this.pos = 0;
            } else if (this.pos >= this.limit) {
                this.pos = 0;
                this.limit = 0;
            }
            int bsize = this.inbuf.length;
            while (this.limit < bsize) {
                int n = this.underlyingInputStream.read(this.inbuf, this.limit, bsize - this.limit);
                if (n <= 0) {
                    this.seenEOF = true;
                    break;
                }
                this.limit += n;
            }
        }
    }

    private boolean need(int n) throws IOException {
        if (this.limit - this.pos >= n) {
            return true;
        }
        this.fill();
        if (this.limit - this.pos >= n) {
            return true;
        }
        if (this.seenEOF) {
            return false;
        }
        byte[] nin = new byte[n + 100];
        System.arraycopy(this.inbuf, this.pos, nin, 0, this.limit - this.pos);
        this.limit -= this.pos;
        this.pos = 0;
        this.inbuf = nin;
        this.fill();
        return this.limit - this.pos >= n;
    }

    private int getInt(int pos) {
        return (this.inbuf[pos] & 0xFF) << 24 | (this.inbuf[pos + 1] & 0xFF) << 16 | (this.inbuf[pos + 2] & 0xFF) << 8 | this.inbuf[pos + 3] & 0xFF;
    }

    private int getShort(int pos) {
        return (short)((this.inbuf[pos] & 0xFF) << 8 | this.inbuf[pos + 1] & 0xFF);
    }

    private int getByte(int pos) {
        return this.inbuf[pos] & 0xFF;
    }

    private boolean getChunk() throws IOException {
        this.chunkLength = 0;
        if (!this.need(8)) {
            return false;
        }
        this.chunkLength = this.getInt(this.pos);
        this.chunkKey = this.getInt(this.pos + 4);
        if (this.chunkLength < 0) {
            throw new PNGException("bogus length: " + this.chunkLength);
        }
        if (!this.need(this.chunkLength + 12)) {
            return false;
        }
        this.chunkCRC = this.getInt(this.pos + 8 + this.chunkLength);
        this.chunkStart = this.pos + 8;
        int calcCRC = PNGImageDecoder.crc(this.inbuf, this.pos + 4, this.chunkLength + 4);
        if (this.chunkCRC != calcCRC && checkCRC) {
            throw new PNGException("crc corruption");
        }
        this.pos += this.chunkLength + 12;
        return true;
    }

    private void readAll() throws IOException {
        while (this.getChunk()) {
            this.handleChunk(this.chunkKey, this.inbuf, this.chunkStart, this.chunkLength);
        }
    }

    boolean getData() throws IOException {
        while (this.chunkLength == 0 && this.getChunk()) {
            if (!this.handleChunk(this.chunkKey, this.inbuf, this.chunkStart, this.chunkLength)) continue;
            this.chunkLength = 0;
        }
        return this.chunkLength > 0;
    }

    public static boolean getCheckCRC() {
        return checkCRC;
    }

    public static void setCheckCRC(boolean c) {
        checkCRC = c;
    }

    protected void wrc(int c) {
        if ((c &= 0xFF) <= 32 || c > 122) {
            c = 63;
        }
        System.out.write(c);
    }

    protected void wrk(int n) {
        this.wrc(n >> 24);
        this.wrc(n >> 16);
        this.wrc(n >> 8);
        this.wrc(n);
    }

    public void print() {
        this.wrk(this.chunkKey);
        System.out.print(" " + this.chunkLength + "\n");
    }

    private static int update_crc(int crc, byte[] buf, int offset, int len) {
        int c = crc;
        while (--len >= 0) {
            c = crc_table[(c ^ buf[offset++]) & 0xFF] ^ c >>> 8;
        }
        return c;
    }

    private static int crc(byte[] buf, int offset, int len) {
        return ~PNGImageDecoder.update_crc(-1, buf, offset, len);
    }

    static {
        for (int n = 0; n < 256; ++n) {
            int c = n;
            for (int k = 0; k < 8; ++k) {
                if ((c & 1) != 0) {
                    c = 0xEDB88320 ^ c >>> 1;
                    continue;
                }
                c >>>= 1;
            }
            PNGImageDecoder.crc_table[n] = c;
        }
    }

    public class PNGException
    extends IOException {
        PNGException(String s) {
            super(s);
        }
    }

    public static class Chromaticities {
        public float whiteX;
        public float whiteY;
        public float redX;
        public float redY;
        public float greenX;
        public float greenY;
        public float blueX;
        public float blueY;

        Chromaticities(int wx, int wy, int rx, int ry, int gx, int gy, int bx, int by) {
            this.whiteX = (float)wx / 100000.0f;
            this.whiteY = (float)wy / 100000.0f;
            this.redX = (float)rx / 100000.0f;
            this.redY = (float)ry / 100000.0f;
            this.greenX = (float)gx / 100000.0f;
            this.greenY = (float)gy / 100000.0f;
            this.blueX = (float)bx / 100000.0f;
            this.blueY = (float)by / 100000.0f;
        }

        public String toString() {
            return "Chromaticities(white=" + this.whiteX + "," + this.whiteY + ";red=" + this.redX + "," + this.redY + ";green=" + this.greenX + "," + this.greenY + ";blue=" + this.blueX + "," + this.blueY + ")";
        }
    }
}

