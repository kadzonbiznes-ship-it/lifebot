/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.png;

import com.sun.imageio.plugins.common.ReaderUtil;
import com.sun.imageio.plugins.png.PNGImageDataEnumeration;
import com.sun.imageio.plugins.png.PNGMetadata;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import sun.awt.image.ByteInterleavedRaster;

public class PNGImageReader
extends ImageReader {
    static final int IHDR_TYPE = 1229472850;
    static final int PLTE_TYPE = 1347179589;
    static final int IDAT_TYPE = 1229209940;
    static final int IEND_TYPE = 1229278788;
    static final int bKGD_TYPE = 1649100612;
    static final int cHRM_TYPE = 1665684045;
    static final int gAMA_TYPE = 1732332865;
    static final int hIST_TYPE = 1749635924;
    static final int iCCP_TYPE = 1766015824;
    static final int iTXt_TYPE = 1767135348;
    static final int pHYs_TYPE = 1883789683;
    static final int sBIT_TYPE = 1933723988;
    static final int sPLT_TYPE = 1934642260;
    static final int sRGB_TYPE = 1934772034;
    static final int tEXt_TYPE = 1950701684;
    static final int tIME_TYPE = 1950960965;
    static final int tRNS_TYPE = 1951551059;
    static final int zTXt_TYPE = 2052348020;
    static final int MAX_INFLATED_TEXT_LENGTH = 262144;
    static final int PNG_COLOR_GRAY = 0;
    static final int PNG_COLOR_RGB = 2;
    static final int PNG_COLOR_PALETTE = 3;
    static final int PNG_COLOR_GRAY_ALPHA = 4;
    static final int PNG_COLOR_RGB_ALPHA = 6;
    static final int[] inputBandsForColorType = new int[]{1, -1, 3, 1, 2, -1, 4};
    static final int PNG_FILTER_NONE = 0;
    static final int PNG_FILTER_SUB = 1;
    static final int PNG_FILTER_UP = 2;
    static final int PNG_FILTER_AVERAGE = 3;
    static final int PNG_FILTER_PAETH = 4;
    static final int[] adam7XOffset = new int[]{0, 4, 0, 2, 0, 1, 0};
    static final int[] adam7YOffset = new int[]{0, 0, 4, 0, 2, 0, 1};
    static final int[] adam7XSubsampling = new int[]{8, 8, 4, 4, 2, 2, 1, 1};
    static final int[] adam7YSubsampling = new int[]{8, 8, 8, 4, 4, 2, 2, 1};
    private static final boolean debug = true;
    ImageInputStream stream = null;
    boolean gotHeader = false;
    boolean gotMetadata = false;
    ImageReadParam lastParam = null;
    long imageStartPosition = -1L;
    Rectangle sourceRegion = null;
    int sourceXSubsampling = -1;
    int sourceYSubsampling = -1;
    int sourceMinProgressivePass = 0;
    int sourceMaxProgressivePass = 6;
    int[] sourceBands = null;
    int[] destinationBands = null;
    Point destinationOffset = new Point(0, 0);
    PNGMetadata metadata = new PNGMetadata();
    DataInputStream pixelStream = null;
    BufferedImage theImage = null;
    int pixelsDone = 0;
    int totalPixels;
    private static final int[][] bandOffsets = new int[][]{null, {0}, {0, 1}, {0, 1, 2}, {0, 1, 2, 3}};

    public PNGImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        this.stream = (ImageInputStream)input;
        this.resetStreamSettings();
    }

    private String readNullTerminatedString(Charset charset, int maxLen) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b = 0;
        int count = 0;
        while (maxLen > count++ && (b = this.stream.read()) != 0) {
            if (b == -1) {
                throw new EOFException();
            }
            baos.write(b);
        }
        if (b != 0) {
            throw new IIOException("Found non null terminated string");
        }
        return baos.toString(charset);
    }

    private void readHeader() throws IIOException {
        if (this.gotHeader) {
            return;
        }
        if (this.stream == null) {
            throw new IllegalStateException("Input source not set!");
        }
        try {
            byte[] signature = new byte[8];
            this.stream.readFully(signature);
            if (signature[0] != -119 || signature[1] != 80 || signature[2] != 78 || signature[3] != 71 || signature[4] != 13 || signature[5] != 10 || signature[6] != 26 || signature[7] != 10) {
                throw new IIOException("Bad PNG signature!");
            }
            int IHDR_length = this.stream.readInt();
            if (IHDR_length != 13) {
                throw new IIOException("Bad length for IHDR chunk!");
            }
            int IHDR_type = this.stream.readInt();
            if (IHDR_type != 1229472850) {
                throw new IIOException("Bad type for IHDR chunk!");
            }
            this.metadata = new PNGMetadata();
            int width = this.stream.readInt();
            int height = this.stream.readInt();
            this.stream.readFully(signature, 0, 5);
            int bitDepth = signature[0] & 0xFF;
            int colorType = signature[1] & 0xFF;
            int compressionMethod = signature[2] & 0xFF;
            int filterMethod = signature[3] & 0xFF;
            int interlaceMethod = signature[4] & 0xFF;
            this.stream.skipBytes(4);
            this.stream.flushBefore(this.stream.getStreamPosition());
            if (width <= 0) {
                throw new IIOException("Image width <= 0!");
            }
            if (height <= 0) {
                throw new IIOException("Image height <= 0!");
            }
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8 && bitDepth != 16) {
                throw new IIOException("Bit depth must be 1, 2, 4, 8, or 16!");
            }
            if (colorType != 0 && colorType != 2 && colorType != 3 && colorType != 4 && colorType != 6) {
                throw new IIOException("Color type must be 0, 2, 3, 4, or 6!");
            }
            if (colorType == 3 && bitDepth == 16) {
                throw new IIOException("Bad color type/bit depth combination!");
            }
            if ((colorType == 2 || colorType == 6 || colorType == 4) && bitDepth != 8 && bitDepth != 16) {
                throw new IIOException("Bad color type/bit depth combination!");
            }
            if (compressionMethod != 0) {
                throw new IIOException("Unknown compression method (not 0)!");
            }
            if (filterMethod != 0) {
                throw new IIOException("Unknown filter method (not 0)!");
            }
            if (interlaceMethod != 0 && interlaceMethod != 1) {
                throw new IIOException("Unknown interlace method (not 0 or 1)!");
            }
            this.metadata.IHDR_present = true;
            this.metadata.IHDR_width = width;
            this.metadata.IHDR_height = height;
            this.metadata.IHDR_bitDepth = bitDepth;
            this.metadata.IHDR_colorType = colorType;
            this.metadata.IHDR_compressionMethod = compressionMethod;
            this.metadata.IHDR_filterMethod = filterMethod;
            this.metadata.IHDR_interlaceMethod = interlaceMethod;
            this.gotHeader = true;
        }
        catch (IOException e) {
            throw new IIOException("I/O error reading PNG header!", e);
        }
    }

    private void parse_PLTE_chunk(int chunkLength) throws IOException {
        if (this.metadata.PLTE_present) {
            this.processWarningOccurred("A PNG image may not contain more than one PLTE chunk.\nThe chunk will be ignored.");
            return;
        }
        if (this.metadata.IHDR_colorType == 0 || this.metadata.IHDR_colorType == 4) {
            this.processWarningOccurred("A PNG gray or gray alpha image cannot have a PLTE chunk.\nThe chunk will be ignored.");
            return;
        }
        byte[] palette = new byte[chunkLength];
        this.stream.readFully(palette);
        int numEntries = chunkLength / 3;
        if (this.metadata.IHDR_colorType == 3) {
            int maxEntries = 1 << this.metadata.IHDR_bitDepth;
            if (numEntries > maxEntries) {
                this.processWarningOccurred("PLTE chunk contains too many entries for bit depth, ignoring extras.");
                numEntries = maxEntries;
            }
            numEntries = Math.min(numEntries, maxEntries);
        }
        int paletteEntries = numEntries > 16 ? 256 : (numEntries > 4 ? 16 : (numEntries > 2 ? 4 : 2));
        this.metadata.PLTE_present = true;
        this.metadata.PLTE_red = new byte[paletteEntries];
        this.metadata.PLTE_green = new byte[paletteEntries];
        this.metadata.PLTE_blue = new byte[paletteEntries];
        int index = 0;
        for (int i = 0; i < numEntries; ++i) {
            this.metadata.PLTE_red[i] = palette[index++];
            this.metadata.PLTE_green[i] = palette[index++];
            this.metadata.PLTE_blue[i] = palette[index++];
        }
    }

    private void parse_bKGD_chunk() throws IOException {
        if (this.metadata.IHDR_colorType == 3) {
            this.metadata.bKGD_colorType = 3;
            this.metadata.bKGD_index = this.stream.readUnsignedByte();
        } else if (this.metadata.IHDR_colorType == 0 || this.metadata.IHDR_colorType == 4) {
            this.metadata.bKGD_colorType = 0;
            this.metadata.bKGD_gray = this.stream.readUnsignedShort();
        } else {
            this.metadata.bKGD_colorType = 2;
            this.metadata.bKGD_red = this.stream.readUnsignedShort();
            this.metadata.bKGD_green = this.stream.readUnsignedShort();
            this.metadata.bKGD_blue = this.stream.readUnsignedShort();
        }
        this.metadata.bKGD_present = true;
    }

    private void parse_cHRM_chunk() throws IOException {
        this.metadata.cHRM_whitePointX = this.stream.readInt();
        this.metadata.cHRM_whitePointY = this.stream.readInt();
        this.metadata.cHRM_redX = this.stream.readInt();
        this.metadata.cHRM_redY = this.stream.readInt();
        this.metadata.cHRM_greenX = this.stream.readInt();
        this.metadata.cHRM_greenY = this.stream.readInt();
        this.metadata.cHRM_blueX = this.stream.readInt();
        this.metadata.cHRM_blueY = this.stream.readInt();
        this.metadata.cHRM_present = true;
    }

    private void parse_gAMA_chunk() throws IOException {
        int gamma;
        this.metadata.gAMA_gamma = gamma = this.stream.readInt();
        this.metadata.gAMA_present = true;
    }

    private void parse_hIST_chunk(int chunkLength) throws IOException, IIOException {
        if (!this.metadata.PLTE_present) {
            throw new IIOException("hIST chunk without prior PLTE chunk!");
        }
        this.metadata.hIST_histogram = new char[chunkLength / 2];
        this.stream.readFully(this.metadata.hIST_histogram, 0, this.metadata.hIST_histogram.length);
        this.metadata.hIST_present = true;
    }

    private void parse_iCCP_chunk(int chunkLength) throws IOException {
        String keyword = this.readNullTerminatedString(StandardCharsets.ISO_8859_1, 80);
        int compressedProfileLength = chunkLength - keyword.length() - 2;
        if (compressedProfileLength <= 0) {
            throw new IIOException("iCCP chunk length is not proper");
        }
        this.metadata.iCCP_profileName = keyword;
        this.metadata.iCCP_compressionMethod = this.stream.readUnsignedByte();
        byte[] compressedProfile = new byte[compressedProfileLength];
        this.stream.readFully(compressedProfile);
        this.metadata.iCCP_compressedProfile = compressedProfile;
        this.metadata.iCCP_present = true;
    }

    private void parse_iTXt_chunk(int chunkLength) throws IOException {
        long chunkStart = this.stream.getStreamPosition();
        String keyword = this.readNullTerminatedString(StandardCharsets.ISO_8859_1, 80);
        this.metadata.iTXt_keyword.add(keyword);
        int compressionFlag = this.stream.readUnsignedByte();
        this.metadata.iTXt_compressionFlag.add(compressionFlag == 1);
        int compressionMethod = this.stream.readUnsignedByte();
        this.metadata.iTXt_compressionMethod.add(compressionMethod);
        long pos = this.stream.getStreamPosition();
        int remainingLen = (int)(chunkStart + (long)chunkLength - pos);
        String languageTag = this.readNullTerminatedString(StandardCharsets.UTF_8, remainingLen);
        this.metadata.iTXt_languageTag.add(languageTag);
        pos = this.stream.getStreamPosition();
        remainingLen = (int)(chunkStart + (long)chunkLength - pos);
        if (remainingLen < 0) {
            throw new IIOException("iTXt chunk length is not proper");
        }
        String translatedKeyword = this.readNullTerminatedString(StandardCharsets.UTF_8, remainingLen);
        this.metadata.iTXt_translatedKeyword.add(translatedKeyword);
        pos = this.stream.getStreamPosition();
        int textLength = (int)(chunkStart + (long)chunkLength - pos);
        if (textLength < 0) {
            throw new IIOException("iTXt chunk length is not proper");
        }
        byte[] b = new byte[textLength];
        this.stream.readFully(b);
        String text = compressionFlag == 1 ? new String(PNGImageReader.inflate(b), StandardCharsets.UTF_8) : new String(b, StandardCharsets.UTF_8);
        this.metadata.iTXt_text.add(text);
        if (keyword.equals("Creation Time")) {
            int index = this.metadata.iTXt_text.size() - 1;
            this.metadata.decodeImageCreationTimeFromTextChunk(this.metadata.iTXt_text.listIterator(index));
        }
    }

    private void parse_pHYs_chunk() throws IOException {
        this.metadata.pHYs_pixelsPerUnitXAxis = this.stream.readInt();
        this.metadata.pHYs_pixelsPerUnitYAxis = this.stream.readInt();
        this.metadata.pHYs_unitSpecifier = this.stream.readUnsignedByte();
        this.metadata.pHYs_present = true;
    }

    private void parse_sBIT_chunk() throws IOException {
        int colorType = this.metadata.IHDR_colorType;
        if (colorType == 0 || colorType == 4) {
            this.metadata.sBIT_grayBits = this.stream.readUnsignedByte();
        } else if (colorType == 2 || colorType == 3 || colorType == 6) {
            this.metadata.sBIT_redBits = this.stream.readUnsignedByte();
            this.metadata.sBIT_greenBits = this.stream.readUnsignedByte();
            this.metadata.sBIT_blueBits = this.stream.readUnsignedByte();
        }
        if (colorType == 4 || colorType == 6) {
            this.metadata.sBIT_alphaBits = this.stream.readUnsignedByte();
        }
        this.metadata.sBIT_colorType = colorType;
        this.metadata.sBIT_present = true;
    }

    private void parse_sPLT_chunk(int chunkLength) throws IOException, IIOException {
        int sampleDepth;
        this.metadata.sPLT_paletteName = this.readNullTerminatedString(StandardCharsets.ISO_8859_1, 80);
        int remainingChunkLength = chunkLength - (this.metadata.sPLT_paletteName.length() + 1);
        if (remainingChunkLength <= 0) {
            throw new IIOException("sPLT chunk length is not proper");
        }
        this.metadata.sPLT_sampleDepth = sampleDepth = this.stream.readUnsignedByte();
        int numEntries = remainingChunkLength / (4 * (sampleDepth / 8) + 2);
        this.metadata.sPLT_red = new int[numEntries];
        this.metadata.sPLT_green = new int[numEntries];
        this.metadata.sPLT_blue = new int[numEntries];
        this.metadata.sPLT_alpha = new int[numEntries];
        this.metadata.sPLT_frequency = new int[numEntries];
        if (sampleDepth == 8) {
            for (int i = 0; i < numEntries; ++i) {
                this.metadata.sPLT_red[i] = this.stream.readUnsignedByte();
                this.metadata.sPLT_green[i] = this.stream.readUnsignedByte();
                this.metadata.sPLT_blue[i] = this.stream.readUnsignedByte();
                this.metadata.sPLT_alpha[i] = this.stream.readUnsignedByte();
                this.metadata.sPLT_frequency[i] = this.stream.readUnsignedShort();
            }
        } else if (sampleDepth == 16) {
            for (int i = 0; i < numEntries; ++i) {
                this.metadata.sPLT_red[i] = this.stream.readUnsignedShort();
                this.metadata.sPLT_green[i] = this.stream.readUnsignedShort();
                this.metadata.sPLT_blue[i] = this.stream.readUnsignedShort();
                this.metadata.sPLT_alpha[i] = this.stream.readUnsignedShort();
                this.metadata.sPLT_frequency[i] = this.stream.readUnsignedShort();
            }
        } else {
            throw new IIOException("sPLT sample depth not 8 or 16!");
        }
        this.metadata.sPLT_present = true;
    }

    private void parse_sRGB_chunk() throws IOException {
        this.metadata.sRGB_renderingIntent = this.stream.readUnsignedByte();
        this.metadata.sRGB_present = true;
    }

    private void parse_tEXt_chunk(int chunkLength) throws IOException {
        String keyword = this.readNullTerminatedString(StandardCharsets.ISO_8859_1, 80);
        int textLength = chunkLength - keyword.length() - 1;
        if (textLength < 0) {
            throw new IIOException("tEXt chunk length is not proper");
        }
        this.metadata.tEXt_keyword.add(keyword);
        byte[] b = new byte[textLength];
        this.stream.readFully(b);
        this.metadata.tEXt_text.add(new String(b, StandardCharsets.ISO_8859_1));
        if (keyword.equals("Creation Time")) {
            int index = this.metadata.tEXt_text.size() - 1;
            this.metadata.decodeImageCreationTimeFromTextChunk(this.metadata.tEXt_text.listIterator(index));
        }
    }

    private void parse_tIME_chunk() throws IOException {
        this.metadata.tIME_year = this.stream.readUnsignedShort();
        this.metadata.tIME_month = this.stream.readUnsignedByte();
        this.metadata.tIME_day = this.stream.readUnsignedByte();
        this.metadata.tIME_hour = this.stream.readUnsignedByte();
        this.metadata.tIME_minute = this.stream.readUnsignedByte();
        this.metadata.tIME_second = this.stream.readUnsignedByte();
        this.metadata.tIME_present = true;
    }

    private void parse_tRNS_chunk(int chunkLength) throws IOException {
        int colorType = this.metadata.IHDR_colorType;
        if (colorType == 3) {
            if (!this.metadata.PLTE_present) {
                this.processWarningOccurred("tRNS chunk without prior PLTE chunk, ignoring it.");
                return;
            }
            int numEntries = chunkLength;
            int maxEntries = this.metadata.PLTE_red.length;
            if (numEntries > maxEntries && maxEntries > 0) {
                this.processWarningOccurred("tRNS chunk has more entries than prior PLTE chunk, ignoring extras.");
                numEntries = maxEntries;
            }
            this.metadata.tRNS_alpha = new byte[numEntries];
            this.metadata.tRNS_colorType = 3;
            this.stream.read(this.metadata.tRNS_alpha, 0, numEntries);
            this.stream.skipBytes(chunkLength - numEntries);
        } else if (colorType == 0) {
            if (chunkLength != 2) {
                this.processWarningOccurred("tRNS chunk for gray image must have length 2, ignoring chunk.");
                this.stream.skipBytes(chunkLength);
                return;
            }
            this.metadata.tRNS_gray = this.stream.readUnsignedShort();
            this.metadata.tRNS_colorType = 0;
        } else if (colorType == 2) {
            if (chunkLength != 6) {
                this.processWarningOccurred("tRNS chunk for RGB image must have length 6, ignoring chunk.");
                this.stream.skipBytes(chunkLength);
                return;
            }
            this.metadata.tRNS_red = this.stream.readUnsignedShort();
            this.metadata.tRNS_green = this.stream.readUnsignedShort();
            this.metadata.tRNS_blue = this.stream.readUnsignedShort();
            this.metadata.tRNS_colorType = 2;
        } else {
            this.processWarningOccurred("Gray+Alpha and RGBS images may not have a tRNS chunk, ignoring it.");
            return;
        }
        this.metadata.tRNS_present = true;
    }

    private static byte[] inflate(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        try (InflaterInputStream iis = new InflaterInputStream(bais);){
            byte[] byArray = iis.readNBytes(262144);
            return byArray;
        }
    }

    private void parse_zTXt_chunk(int chunkLength) throws IOException {
        String keyword = this.readNullTerminatedString(StandardCharsets.ISO_8859_1, 80);
        int textLength = chunkLength - keyword.length() - 2;
        if (textLength < 0) {
            throw new IIOException("zTXt chunk length is not proper");
        }
        this.metadata.zTXt_keyword.add(keyword);
        int method = this.stream.readUnsignedByte();
        this.metadata.zTXt_compressionMethod.add(method);
        byte[] b = new byte[textLength];
        this.stream.readFully(b);
        this.metadata.zTXt_text.add(new String(PNGImageReader.inflate(b), StandardCharsets.ISO_8859_1));
        if (keyword.equals("Creation Time")) {
            int index = this.metadata.zTXt_text.size() - 1;
            this.metadata.decodeImageCreationTimeFromTextChunk(this.metadata.zTXt_text.listIterator(index));
        }
    }

    private void readMetadata() throws IIOException {
        block40: {
            if (this.gotMetadata) {
                return;
            }
            this.readHeader();
            int colorType = this.metadata.IHDR_colorType;
            if (this.ignoreMetadata && colorType != 3) {
                try {
                    while (true) {
                        int chunkLength;
                        if ((chunkLength = this.stream.readInt()) < 0 || chunkLength + 4 < 0) {
                            throw new IIOException("Invalid chunk length " + chunkLength);
                        }
                        int chunkType = this.stream.readInt();
                        if (chunkType == 1229209940) {
                            this.stream.skipBytes(-8);
                            this.imageStartPosition = this.stream.getStreamPosition();
                            break;
                        }
                        if (chunkType == 1951551059) {
                            this.parse_tRNS_chunk(chunkLength);
                            this.stream.skipBytes(4);
                            continue;
                        }
                        this.stream.skipBytes(chunkLength + 4);
                    }
                }
                catch (IOException e) {
                    throw new IIOException("Error skipping PNG metadata", e);
                }
                this.gotMetadata = true;
                return;
            }
            try {
                int chunkType;
                int chunkCRC;
                do {
                    int chunkLength = this.stream.readInt();
                    chunkType = this.stream.readInt();
                    chunkCRC = -1;
                    if (chunkLength < 0) {
                        throw new IIOException("Invalid chunk length " + chunkLength);
                    }
                    try {
                        if (chunkType != 1229278788) {
                            this.stream.mark();
                            this.stream.seek(this.stream.getStreamPosition() + (long)chunkLength);
                            chunkCRC = this.stream.readInt();
                            this.stream.reset();
                        }
                    }
                    catch (IOException e) {
                        throw new IIOException("Invalid chunk length " + chunkLength);
                    }
                    switch (chunkType) {
                        case 1229209940: {
                            if (this.imageStartPosition == -1L) {
                                if (colorType == 3 && !this.metadata.PLTE_present) {
                                    throw new IIOException("Required PLTE chunk missing");
                                }
                                this.imageStartPosition = this.stream.getStreamPosition() - 8L;
                            }
                            this.stream.skipBytes(chunkLength);
                            break;
                        }
                        case 1229278788: {
                            this.stream.seek(this.imageStartPosition);
                            this.stream.flushBefore(this.stream.getStreamPosition());
                            break block40;
                        }
                        case 1347179589: {
                            this.parse_PLTE_chunk(chunkLength);
                            break;
                        }
                        case 1649100612: {
                            this.parse_bKGD_chunk();
                            break;
                        }
                        case 1665684045: {
                            this.parse_cHRM_chunk();
                            break;
                        }
                        case 1732332865: {
                            this.parse_gAMA_chunk();
                            break;
                        }
                        case 1749635924: {
                            this.parse_hIST_chunk(chunkLength);
                            break;
                        }
                        case 1766015824: {
                            this.parse_iCCP_chunk(chunkLength);
                            break;
                        }
                        case 1767135348: {
                            if (this.ignoreMetadata) {
                                this.stream.skipBytes(chunkLength);
                                break;
                            }
                            this.parse_iTXt_chunk(chunkLength);
                            break;
                        }
                        case 1883789683: {
                            this.parse_pHYs_chunk();
                            break;
                        }
                        case 1933723988: {
                            this.parse_sBIT_chunk();
                            break;
                        }
                        case 1934642260: {
                            this.parse_sPLT_chunk(chunkLength);
                            break;
                        }
                        case 1934772034: {
                            this.parse_sRGB_chunk();
                            break;
                        }
                        case 1950701684: {
                            this.parse_tEXt_chunk(chunkLength);
                            break;
                        }
                        case 1950960965: {
                            this.parse_tIME_chunk();
                            break;
                        }
                        case 1951551059: {
                            this.parse_tRNS_chunk(chunkLength);
                            break;
                        }
                        case 2052348020: {
                            if (this.ignoreMetadata) {
                                this.stream.skipBytes(chunkLength);
                                break;
                            }
                            this.parse_zTXt_chunk(chunkLength);
                            break;
                        }
                        default: {
                            byte[] b = new byte[chunkLength];
                            this.stream.readFully(b);
                            StringBuilder chunkName = new StringBuilder(4);
                            chunkName.append((char)(chunkType >>> 24));
                            chunkName.append((char)(chunkType >> 16 & 0xFF));
                            chunkName.append((char)(chunkType >> 8 & 0xFF));
                            chunkName.append((char)(chunkType & 0xFF));
                            int ancillaryBit = chunkType >>> 28;
                            if (ancillaryBit == 0) {
                                this.processWarningOccurred("Encountered unknown chunk with critical bit set!");
                            }
                            this.metadata.unknownChunkType.add(chunkName.toString());
                            this.metadata.unknownChunkData.add(b);
                        }
                    }
                } while (chunkCRC == this.stream.readInt());
                throw new IIOException("Failed to read a chunk of type " + chunkType);
            }
            catch (IOException e) {
                throw new IIOException("Error reading PNG metadata", e);
            }
        }
        this.gotMetadata = true;
    }

    private static void decodeSubFilter(byte[] curr, int coff, int count, int bpp) {
        for (int i = bpp; i < count; ++i) {
            int val = curr[i + coff] & 0xFF;
            curr[i + coff] = (byte)(val += curr[i + coff - bpp] & 0xFF);
        }
    }

    private static void decodeUpFilter(byte[] curr, int coff, byte[] prev, int poff, int count) {
        for (int i = 0; i < count; ++i) {
            int raw = curr[i + coff] & 0xFF;
            int prior = prev[i + poff] & 0xFF;
            curr[i + coff] = (byte)(raw + prior);
        }
    }

    private static void decodeAverageFilter(byte[] curr, int coff, byte[] prev, int poff, int count, int bpp) {
        int priorRow;
        int raw;
        int i;
        for (i = 0; i < bpp; ++i) {
            raw = curr[i + coff] & 0xFF;
            priorRow = prev[i + poff] & 0xFF;
            curr[i + coff] = (byte)(raw + priorRow / 2);
        }
        for (i = bpp; i < count; ++i) {
            raw = curr[i + coff] & 0xFF;
            int priorPixel = curr[i + coff - bpp] & 0xFF;
            priorRow = prev[i + poff] & 0xFF;
            curr[i + coff] = (byte)(raw + (priorPixel + priorRow) / 2);
        }
    }

    private static int paethPredictor(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) {
            return a;
        }
        if (pb <= pc) {
            return b;
        }
        return c;
    }

    private static void decodePaethFilter(byte[] curr, int coff, byte[] prev, int poff, int count, int bpp) {
        int priorRow;
        int raw;
        int i;
        for (i = 0; i < bpp; ++i) {
            raw = curr[i + coff] & 0xFF;
            priorRow = prev[i + poff] & 0xFF;
            curr[i + coff] = (byte)(raw + priorRow);
        }
        for (i = bpp; i < count; ++i) {
            raw = curr[i + coff] & 0xFF;
            int priorPixel = curr[i + coff - bpp] & 0xFF;
            priorRow = prev[i + poff] & 0xFF;
            int priorRowPixel = prev[i + poff - bpp] & 0xFF;
            curr[i + coff] = (byte)(raw + PNGImageReader.paethPredictor(priorPixel, priorRow, priorRowPixel));
        }
    }

    private WritableRaster createRaster(int width, int height, int bands, int scanlineStride, int bitDepth) {
        WritableRaster ras = null;
        Point origin = new Point(0, 0);
        if (bitDepth < 8 && bands == 1) {
            DataBufferByte dataBuffer = new DataBufferByte(height * scanlineStride);
            ras = Raster.createPackedRaster(dataBuffer, width, height, bitDepth, origin);
        } else if (bitDepth <= 8) {
            DataBufferByte dataBuffer = new DataBufferByte(height * scanlineStride);
            ras = Raster.createInterleavedRaster(dataBuffer, width, height, scanlineStride, bands, bandOffsets[bands], origin);
        } else {
            DataBufferUShort dataBuffer = new DataBufferUShort(height * scanlineStride);
            ras = Raster.createInterleavedRaster(dataBuffer, width, height, scanlineStride, bands, bandOffsets[bands], origin);
        }
        return ras;
    }

    private void skipPass(int passWidth, int passHeight) throws IOException, IIOException {
        if (passWidth == 0 || passHeight == 0) {
            return;
        }
        int inputBands = inputBandsForColorType[this.metadata.IHDR_colorType];
        int bitsPerRow = Math.multiplyExact(inputBands * this.metadata.IHDR_bitDepth, passWidth);
        int bytesPerRow = (bitsPerRow + 7) / 8;
        for (int srcY = 0; srcY < passHeight; ++srcY) {
            this.pixelStream.skipBytes(1 + bytesPerRow);
        }
    }

    private void updateImageProgress(int newPixels) {
        this.pixelsDone += newPixels;
        this.processImageProgress(100.0f * (float)this.pixelsDone / (float)this.totalPixels);
    }

    private void decodePass(int passNum, int xStart, int yStart, int xStep, int yStep, int passWidth, int passHeight) throws IOException {
        boolean useSetRect;
        int eltsPerRow;
        if (passWidth == 0 || passHeight == 0) {
            return;
        }
        WritableRaster imRas = this.theImage.getWritableTile(0, 0);
        int dstMinX = imRas.getMinX();
        int dstMaxX = dstMinX + imRas.getWidth() - 1;
        int dstMinY = imRas.getMinY();
        int dstMaxY = dstMinY + imRas.getHeight() - 1;
        int[] vals = ReaderUtil.computeUpdatedPixels(this.sourceRegion, this.destinationOffset, dstMinX, dstMinY, dstMaxX, dstMaxY, this.sourceXSubsampling, this.sourceYSubsampling, xStart, yStart, passWidth, passHeight, xStep, yStep);
        int updateMinX = vals[0];
        int updateMinY = vals[1];
        int updateWidth = vals[2];
        int updateXStep = vals[4];
        int updateYStep = vals[5];
        int bitDepth = this.metadata.IHDR_bitDepth;
        int inputBands = inputBandsForColorType[this.metadata.IHDR_colorType];
        int bytesPerPixel = bitDepth == 16 ? 2 : 1;
        bytesPerPixel *= inputBands;
        int bitsPerRow = Math.multiplyExact(inputBands * bitDepth, passWidth);
        int bytesPerRow = (bitsPerRow + 7) / 8;
        int n = eltsPerRow = bitDepth == 16 ? bytesPerRow / 2 : bytesPerRow;
        if (updateWidth == 0) {
            for (int srcY = 0; srcY < passHeight; ++srcY) {
                this.updateImageProgress(passWidth);
                if (this.abortRequested()) {
                    return;
                }
                this.pixelStream.skipBytes(1 + bytesPerRow);
            }
            return;
        }
        int sourceX = (updateMinX - this.destinationOffset.x) * this.sourceXSubsampling + this.sourceRegion.x;
        int srcX = (sourceX - xStart) / xStep;
        int srcXStep = updateXStep * this.sourceXSubsampling / xStep;
        byte[] byteData = null;
        short[] shortData = null;
        byte[] curr = new byte[bytesPerRow];
        byte[] prior = new byte[bytesPerRow];
        WritableRaster passRow = this.createRaster(passWidth, 1, inputBands, eltsPerRow, bitDepth);
        int[] ps = passRow.getPixel(0, 0, (int[])null);
        DataBuffer dataBuffer = passRow.getDataBuffer();
        int type = dataBuffer.getDataType();
        if (type == 0) {
            byteData = ((DataBufferByte)dataBuffer).getData();
        } else {
            shortData = ((DataBufferUShort)dataBuffer).getData();
        }
        this.processPassStarted(this.theImage, passNum, this.sourceMinProgressivePass, this.sourceMaxProgressivePass, updateMinX, updateMinY, updateXStep, updateYStep, this.destinationBands);
        if (this.sourceBands != null) {
            passRow = passRow.createWritableChild(0, 0, passRow.getWidth(), 1, 0, 0, this.sourceBands);
        }
        if (this.destinationBands != null) {
            imRas = imRas.createWritableChild(0, 0, imRas.getWidth(), imRas.getHeight(), 0, 0, this.destinationBands);
        }
        boolean adjustBitDepths = false;
        int[] outputSampleSize = imRas.getSampleModel().getSampleSize();
        for (int b = 0; b < inputBands; ++b) {
            if (outputSampleSize[b] == bitDepth) continue;
            adjustBitDepths = true;
            break;
        }
        int[][] scale = null;
        if (adjustBitDepths) {
            int maxInSample = (1 << bitDepth) - 1;
            int halfMaxInSample = maxInSample / 2;
            scale = new int[inputBands][];
            for (int b = 0; b < inputBands; ++b) {
                int maxOutSample = (1 << outputSampleSize[b]) - 1;
                scale[b] = new int[maxInSample + 1];
                for (int s = 0; s <= maxInSample; ++s) {
                    scale[b][s] = (s * maxOutSample + halfMaxInSample) / maxInSample;
                }
            }
        }
        boolean bl = useSetRect = srcXStep == 1 && updateXStep == 1 && !adjustBitDepths && imRas instanceof ByteInterleavedRaster;
        if (useSetRect) {
            passRow = passRow.createWritableChild(srcX, 0, updateWidth, 1, 0, 0, null);
        }
        for (int srcY = 0; srcY < passHeight; ++srcY) {
            boolean tRNSTransparentPixelPresent;
            int dstY;
            this.updateImageProgress(passWidth);
            if (this.abortRequested()) {
                return;
            }
            int filter = this.pixelStream.read();
            byte[] tmp = prior;
            prior = curr;
            curr = tmp;
            this.pixelStream.readFully(curr, 0, bytesPerRow);
            switch (filter) {
                case 0: {
                    break;
                }
                case 1: {
                    PNGImageReader.decodeSubFilter(curr, 0, bytesPerRow, bytesPerPixel);
                    break;
                }
                case 2: {
                    PNGImageReader.decodeUpFilter(curr, 0, prior, 0, bytesPerRow);
                    break;
                }
                case 3: {
                    PNGImageReader.decodeAverageFilter(curr, 0, prior, 0, bytesPerRow, bytesPerPixel);
                    break;
                }
                case 4: {
                    PNGImageReader.decodePaethFilter(curr, 0, prior, 0, bytesPerRow, bytesPerPixel);
                    break;
                }
                default: {
                    throw new IIOException("Unknown row filter type (= " + filter + ")!");
                }
            }
            if (bitDepth < 16) {
                System.arraycopy(curr, 0, byteData, 0, bytesPerRow);
            } else {
                int idx = 0;
                for (int j = 0; j < eltsPerRow; ++j) {
                    shortData[j] = (short)(curr[idx] << 8 | curr[idx + 1] & 0xFF);
                    idx += 2;
                }
            }
            int sourceY = srcY * yStep + yStart;
            if (sourceY < this.sourceRegion.y || sourceY >= this.sourceRegion.y + this.sourceRegion.height || (sourceY - this.sourceRegion.y) % this.sourceYSubsampling != 0 || (dstY = this.destinationOffset.y + (sourceY - this.sourceRegion.y) / this.sourceYSubsampling) < dstMinY) continue;
            if (dstY > dstMaxY) break;
            boolean bl2 = tRNSTransparentPixelPresent = this.theImage.getSampleModel().getNumBands() == inputBands + 1 && this.metadata.hasTransparentColor();
            if (useSetRect && !tRNSTransparentPixelPresent) {
                imRas.setRect(updateMinX, dstY, passRow);
            } else {
                int newSrcX = srcX;
                int[] temp = new int[inputBands + 1];
                int opaque = bitDepth < 16 ? 255 : 65535;
                for (int dstX = updateMinX; dstX < updateMinX + updateWidth; dstX += updateXStep) {
                    passRow.getPixel(newSrcX, 0, ps);
                    if (adjustBitDepths) {
                        for (int b = 0; b < inputBands; ++b) {
                            ps[b] = scale[b][ps[b]];
                        }
                    }
                    if (tRNSTransparentPixelPresent) {
                        if (this.metadata.tRNS_colorType == 2) {
                            temp[0] = ps[0];
                            temp[1] = ps[1];
                            temp[2] = ps[2];
                            temp[3] = ps[0] == this.metadata.tRNS_red && ps[1] == this.metadata.tRNS_green && ps[2] == this.metadata.tRNS_blue ? 0 : opaque;
                        } else {
                            temp[0] = ps[0];
                            temp[1] = ps[0] == this.metadata.tRNS_gray ? 0 : opaque;
                        }
                        imRas.setPixel(dstX, dstY, temp);
                    } else {
                        imRas.setPixel(dstX, dstY, ps);
                    }
                    newSrcX += srcXStep;
                }
            }
            this.processImageUpdate(this.theImage, updateMinX, dstY, updateWidth, 1, updateXStep, updateYStep, this.destinationBands);
        }
        this.processPassComplete(this.theImage);
    }

    private void decodeImage() throws IOException, IIOException {
        int width = this.metadata.IHDR_width;
        int height = this.metadata.IHDR_height;
        this.pixelsDone = 0;
        this.totalPixels = width * height;
        if (this.metadata.IHDR_interlaceMethod == 0) {
            this.decodePass(0, 0, 0, 1, 1, width, height);
        } else {
            for (int i = 0; i <= this.sourceMaxProgressivePass; ++i) {
                int XOffset = adam7XOffset[i];
                int YOffset = adam7YOffset[i];
                int XSubsampling = adam7XSubsampling[i];
                int YSubsampling = adam7YSubsampling[i];
                int xbump = adam7XSubsampling[i + 1] - 1;
                int ybump = adam7YSubsampling[i + 1] - 1;
                if (i >= this.sourceMinProgressivePass) {
                    this.decodePass(i, XOffset, YOffset, XSubsampling, YSubsampling, (width + xbump) / XSubsampling, (height + ybump) / YSubsampling);
                } else {
                    this.skipPass((width + xbump) / XSubsampling, (height + ybump) / YSubsampling);
                }
                if (!this.abortRequested()) continue;
                return;
            }
        }
    }

    private void readImage(ImageReadParam param) throws IIOException {
        this.readMetadata();
        int width = this.metadata.IHDR_width;
        int height = this.metadata.IHDR_height;
        if ((long)width * (long)height > 0x7FFFFFFDL) {
            throw new IIOException("Can not read image of the size " + width + " by " + height);
        }
        this.sourceXSubsampling = 1;
        this.sourceYSubsampling = 1;
        this.sourceMinProgressivePass = 0;
        this.sourceMaxProgressivePass = 6;
        this.sourceBands = null;
        this.destinationBands = null;
        this.destinationOffset = new Point(0, 0);
        if (param != null) {
            this.sourceXSubsampling = param.getSourceXSubsampling();
            this.sourceYSubsampling = param.getSourceYSubsampling();
            this.sourceMinProgressivePass = Math.max(param.getSourceMinProgressivePass(), 0);
            this.sourceMaxProgressivePass = Math.min(param.getSourceMaxProgressivePass(), 6);
            this.sourceBands = param.getSourceBands();
            this.destinationBands = param.getDestinationBands();
            this.destinationOffset = param.getDestinationOffset();
        }
        Inflater inf = null;
        try {
            this.stream.seek(this.imageStartPosition);
            PNGImageDataEnumeration e = new PNGImageDataEnumeration(this.stream);
            InputStream is = new SequenceInputStream(e);
            inf = new Inflater();
            is = new InflaterInputStream(is, inf);
            is = new BufferedInputStream(is);
            this.pixelStream = new DataInputStream(is);
            this.theImage = PNGImageReader.getDestination(param, this.getImageTypes(0), width, height);
            Rectangle destRegion = new Rectangle(0, 0, 0, 0);
            this.sourceRegion = new Rectangle(0, 0, 0, 0);
            PNGImageReader.computeRegions(param, width, height, this.theImage, this.sourceRegion, destRegion);
            this.destinationOffset.setLocation(destRegion.getLocation());
            int colorType = this.metadata.IHDR_colorType;
            if (this.theImage.getSampleModel().getNumBands() == inputBandsForColorType[colorType] + 1 && this.metadata.hasTransparentColor()) {
                PNGImageReader.checkReadParamBandSettings(param, inputBandsForColorType[colorType] + 1, this.theImage.getSampleModel().getNumBands());
            } else {
                PNGImageReader.checkReadParamBandSettings(param, inputBandsForColorType[colorType], this.theImage.getSampleModel().getNumBands());
            }
            this.clearAbortRequest();
            this.processImageStarted(0);
            if (this.abortRequested()) {
                this.processReadAborted();
            } else {
                this.decodeImage();
                if (this.abortRequested()) {
                    this.processReadAborted();
                } else {
                    this.processImageComplete();
                }
            }
        }
        catch (IOException e) {
            throw new IIOException("Error reading PNG image data", e);
        }
        finally {
            if (inf != null) {
                inf.end();
            }
        }
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IIOException {
        if (this.stream == null) {
            throw new IllegalStateException("No input source set!");
        }
        if (this.seekForwardOnly && allowSearch) {
            throw new IllegalStateException("seekForwardOnly and allowSearch can't both be true!");
        }
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("imageIndex != 0!");
        }
        this.readHeader();
        return this.metadata.IHDR_width;
    }

    @Override
    public int getHeight(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("imageIndex != 0!");
        }
        this.readHeader();
        return this.metadata.IHDR_height;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("imageIndex != 0!");
        }
        this.readHeader();
        ArrayList<ImageTypeSpecifier> l = new ArrayList<ImageTypeSpecifier>(1);
        int bitDepth = this.metadata.IHDR_bitDepth;
        int colorType = this.metadata.IHDR_colorType;
        int dataType = bitDepth <= 8 ? 0 : 1;
        switch (colorType) {
            case 0: {
                this.readMetadata();
                if (this.metadata.hasTransparentColor()) {
                    ColorSpace gray = ColorSpace.getInstance(1003);
                    int[] bandOffsets = new int[]{0, 1};
                    l.add(ImageTypeSpecifier.createInterleaved(gray, bandOffsets, dataType, true, false));
                }
                l.add(ImageTypeSpecifier.createGrayscale(bitDepth, dataType, false));
                break;
            }
            case 2: {
                int[] bandOffsets;
                ColorSpace rgb;
                this.readMetadata();
                if (bitDepth == 8) {
                    if (this.metadata.hasTransparentColor()) {
                        l.add(ImageTypeSpecifier.createFromBufferedImageType(6));
                    }
                    l.add(ImageTypeSpecifier.createFromBufferedImageType(5));
                    l.add(ImageTypeSpecifier.createFromBufferedImageType(1));
                    l.add(ImageTypeSpecifier.createFromBufferedImageType(4));
                }
                if (this.metadata.hasTransparentColor()) {
                    rgb = ColorSpace.getInstance(1000);
                    bandOffsets = new int[]{0, 1, 2, 3};
                    l.add(ImageTypeSpecifier.createInterleaved(rgb, bandOffsets, dataType, true, false));
                }
                rgb = ColorSpace.getInstance(1000);
                bandOffsets = new int[]{0, 1, 2};
                l.add(ImageTypeSpecifier.createInterleaved(rgb, bandOffsets, dataType, false, false));
                break;
            }
            case 3: {
                this.readMetadata();
                int plength = 1 << bitDepth;
                byte[] red = this.metadata.PLTE_red;
                byte[] green = this.metadata.PLTE_green;
                byte[] blue = this.metadata.PLTE_blue;
                if (this.metadata.PLTE_red.length < plength) {
                    red = Arrays.copyOf(this.metadata.PLTE_red, plength);
                    Arrays.fill(red, this.metadata.PLTE_red.length, plength, this.metadata.PLTE_red[this.metadata.PLTE_red.length - 1]);
                    green = Arrays.copyOf(this.metadata.PLTE_green, plength);
                    Arrays.fill(green, this.metadata.PLTE_green.length, plength, this.metadata.PLTE_green[this.metadata.PLTE_green.length - 1]);
                    blue = Arrays.copyOf(this.metadata.PLTE_blue, plength);
                    Arrays.fill(blue, this.metadata.PLTE_blue.length, plength, this.metadata.PLTE_blue[this.metadata.PLTE_blue.length - 1]);
                }
                byte[] alpha = null;
                if (this.metadata.tRNS_present && this.metadata.tRNS_alpha != null) {
                    if (this.metadata.tRNS_alpha.length == red.length) {
                        alpha = this.metadata.tRNS_alpha;
                    } else {
                        alpha = Arrays.copyOf(this.metadata.tRNS_alpha, red.length);
                        Arrays.fill(alpha, this.metadata.tRNS_alpha.length, red.length, (byte)-1);
                    }
                }
                l.add(ImageTypeSpecifier.createIndexed(red, green, blue, alpha, bitDepth, 0));
                break;
            }
            case 4: {
                ColorSpace gray = ColorSpace.getInstance(1003);
                int[] bandOffsets = new int[]{0, 1};
                l.add(ImageTypeSpecifier.createInterleaved(gray, bandOffsets, dataType, true, false));
                break;
            }
            case 6: {
                if (bitDepth == 8) {
                    l.add(ImageTypeSpecifier.createFromBufferedImageType(6));
                    l.add(ImageTypeSpecifier.createFromBufferedImageType(2));
                }
                ColorSpace rgb = ColorSpace.getInstance(1000);
                int[] bandOffsets = new int[]{0, 1, 2, 3};
                l.add(ImageTypeSpecifier.createInterleaved(rgb, bandOffsets, dataType, true, false));
                break;
            }
        }
        return l.iterator();
    }

    @Override
    public ImageTypeSpecifier getRawImageType(int imageIndex) throws IOException {
        Iterator<ImageTypeSpecifier> types = this.getImageTypes(imageIndex);
        ImageTypeSpecifier raw = null;
        do {
            raw = types.next();
        } while (types.hasNext());
        return raw;
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IIOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("imageIndex != 0!");
        }
        this.readMetadata();
        return this.metadata;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("imageIndex != 0!");
        }
        try {
            this.readImage(param);
        }
        catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new IIOException("Caught exception during read: ", e);
        }
        return this.theImage;
    }

    @Override
    public void reset() {
        super.reset();
        this.resetStreamSettings();
    }

    private void resetStreamSettings() {
        this.gotHeader = false;
        this.gotMetadata = false;
        this.metadata = null;
        this.pixelStream = null;
        this.imageStartPosition = -1L;
    }
}

