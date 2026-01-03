/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.tiff;

import com.sun.imageio.plugins.common.ImageUtil;
import com.sun.imageio.plugins.common.SingleTileRenderedImage;
import com.sun.imageio.plugins.tiff.EmptyImage;
import com.sun.imageio.plugins.tiff.TIFFCIELabColorConverter;
import com.sun.imageio.plugins.tiff.TIFFColorConverter;
import com.sun.imageio.plugins.tiff.TIFFCompressor;
import com.sun.imageio.plugins.tiff.TIFFDeflateCompressor;
import com.sun.imageio.plugins.tiff.TIFFExifJPEGCompressor;
import com.sun.imageio.plugins.tiff.TIFFIFD;
import com.sun.imageio.plugins.tiff.TIFFImageMetadata;
import com.sun.imageio.plugins.tiff.TIFFImageReader;
import com.sun.imageio.plugins.tiff.TIFFImageReaderSpi;
import com.sun.imageio.plugins.tiff.TIFFImageWriteParam;
import com.sun.imageio.plugins.tiff.TIFFJPEGCompressor;
import com.sun.imageio.plugins.tiff.TIFFLSBCompressor;
import com.sun.imageio.plugins.tiff.TIFFLZWCompressor;
import com.sun.imageio.plugins.tiff.TIFFNullCompressor;
import com.sun.imageio.plugins.tiff.TIFFPackBitsCompressor;
import com.sun.imageio.plugins.tiff.TIFFRLECompressor;
import com.sun.imageio.plugins.tiff.TIFFStreamMetadata;
import com.sun.imageio.plugins.tiff.TIFFT4Compressor;
import com.sun.imageio.plugins.tiff.TIFFT6Compressor;
import com.sun.imageio.plugins.tiff.TIFFYCbCrColorConverter;
import com.sun.imageio.plugins.tiff.TIFFZLibCompressor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.ExifParentTIFFTagSet;
import javax.imageio.plugins.tiff.ExifTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.Node;

public class TIFFImageWriter
extends ImageWriter {
    static final String EXIF_JPEG_COMPRESSION_TYPE = "Exif JPEG";
    private static final int DEFAULT_BYTES_PER_STRIP = 8192;
    static final String[] TIFFCompressionTypes = new String[]{"CCITT RLE", "CCITT T.4", "CCITT T.6", "LZW", "JPEG", "ZLib", "PackBits", "Deflate", "Exif JPEG"};
    static final String[] compressionTypes = new String[]{"CCITT RLE", "CCITT T.4", "CCITT T.6", "LZW", "Old JPEG", "JPEG", "ZLib", "PackBits", "Deflate", "Exif JPEG"};
    static final boolean[] isCompressionLossless = new boolean[]{true, true, true, true, false, false, true, true, true, false};
    static final int[] compressionNumbers = new int[]{2, 3, 4, 5, 6, 7, 8, 32773, 32946, 6};
    private ImageOutputStream stream;
    private long headerPosition;
    private RenderedImage image;
    private ImageTypeSpecifier imageType;
    private ByteOrder byteOrder;
    private ImageWriteParam param;
    private TIFFCompressor compressor;
    private TIFFColorConverter colorConverter;
    private TIFFStreamMetadata streamMetadata;
    private TIFFImageMetadata imageMetadata;
    private int sourceXOffset;
    private int sourceYOffset;
    private int sourceWidth;
    private int sourceHeight;
    private int[] sourceBands;
    private int periodX;
    private int periodY;
    private int bitDepth;
    private int numBands;
    private int tileWidth;
    private int tileLength;
    private int tilesAcross;
    private int tilesDown;
    private int[] sampleSize = null;
    private int scalingBitDepth = -1;
    private boolean isRescaling = false;
    private boolean isBilevel;
    private boolean isImageSimple;
    private boolean isInverted;
    private boolean isTiled;
    private int nativePhotometricInterpretation;
    private int photometricInterpretation;
    private char[] bitsPerSample;
    private int sampleFormat = 4;
    private byte[][] scale = null;
    private byte[] scale0 = null;
    private byte[][] scaleh = null;
    private byte[][] scalel = null;
    private int compression;
    private int predictor;
    private int totalPixels;
    private int pixelsDone;
    private long nextIFDPointerPos;
    private long nextSpace = 0L;
    private long prevStreamPosition;
    private long prevHeaderPosition;
    private long prevNextSpace;
    private boolean isWritingSequence = false;
    private boolean isInsertingEmpty = false;
    private boolean isWritingEmpty = false;
    private int currentImage = 0;
    private Object replacePixelsLock = new Object();
    private int replacePixelsIndex = -1;
    private TIFFImageMetadata replacePixelsMetadata = null;
    private long[] replacePixelsTileOffsets = null;
    private long[] replacePixelsByteCounts = null;
    private long replacePixelsOffsetsPosition = 0L;
    private long replacePixelsByteCountsPosition = 0L;
    private Rectangle replacePixelsRegion = null;
    private boolean inReplacePixelsNest = false;
    private TIFFImageReader reader = null;

    public static int XToTileX(int x, int tileGridXOffset, int tileWidth) {
        if ((x -= tileGridXOffset) < 0) {
            x += 1 - tileWidth;
        }
        return x / tileWidth;
    }

    public static int YToTileY(int y, int tileGridYOffset, int tileHeight) {
        if ((y -= tileGridYOffset) < 0) {
            y += 1 - tileHeight;
        }
        return y / tileHeight;
    }

    public TIFFImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new TIFFImageWriteParam(this.getLocale());
    }

    @Override
    public void setOutput(Object output) {
        if (output != null) {
            if (!(output instanceof ImageOutputStream)) {
                throw new IllegalArgumentException("output not an ImageOutputStream!");
            }
            this.reset();
            this.stream = (ImageOutputStream)output;
            try {
                this.headerPosition = this.stream.getStreamPosition();
                try {
                    byte[] b = new byte[4];
                    this.stream.readFully(b);
                    this.nextSpace = b[0] == 73 && b[1] == 73 && b[2] == 42 && b[3] == 0 || b[0] == 77 && b[1] == 77 && b[2] == 0 && b[3] == 42 ? this.stream.length() : this.headerPosition;
                }
                catch (IOException io) {
                    this.nextSpace = this.headerPosition;
                }
                this.stream.seek(this.headerPosition);
            }
            catch (IOException ioe) {
                this.headerPosition = 0L;
                this.nextSpace = 0L;
            }
        } else {
            this.stream = null;
        }
        super.setOutput(output);
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return new TIFFStreamMetadata();
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        TIFFImageMetadata im;
        ArrayList<TIFFTagSet> tagSets = new ArrayList<TIFFTagSet>(1);
        tagSets.add(BaselineTIFFTagSet.getInstance());
        TIFFImageMetadata imageMetadata = new TIFFImageMetadata(tagSets);
        if (imageType != null && (im = (TIFFImageMetadata)this.convertImageMetadata(imageMetadata, imageType, param)) != null) {
            imageMetadata = im;
        }
        return imageMetadata;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        if (inData == null) {
            throw new NullPointerException("inData == null!");
        }
        TIFFStreamMetadata outData = null;
        if (inData instanceof TIFFStreamMetadata) {
            outData = new TIFFStreamMetadata();
            outData.byteOrder = ((TIFFStreamMetadata)inData).byteOrder;
            return outData;
        }
        if (Arrays.asList(inData.getMetadataFormatNames()).contains("javax_imageio_tiff_stream_1.0")) {
            outData = new TIFFStreamMetadata();
            String format = "javax_imageio_tiff_stream_1.0";
            try {
                outData.mergeTree(format, inData.getAsTree(format));
            }
            catch (IIOInvalidTreeException e) {
                return null;
            }
        }
        return outData;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        if (inData == null) {
            throw new NullPointerException("inData == null!");
        }
        if (imageType == null) {
            throw new NullPointerException("imageType == null!");
        }
        TIFFImageMetadata outData = null;
        if (inData instanceof TIFFImageMetadata) {
            TIFFIFD inIFD = ((TIFFImageMetadata)inData).getRootIFD();
            outData = new TIFFImageMetadata(inIFD.getShallowClone());
        } else {
            if (Arrays.asList(inData.getMetadataFormatNames()).contains("javax_imageio_tiff_image_1.0")) {
                try {
                    outData = this.convertNativeImageMetadata(inData);
                }
                catch (IIOInvalidTreeException e) {
                    return null;
                }
            }
            if (inData.isStandardMetadataFormatSupported()) {
                try {
                    outData = this.convertStandardImageMetadata(inData);
                }
                catch (IIOInvalidTreeException e) {
                    return null;
                }
            }
        }
        if (outData != null) {
            TIFFImageWriter bogusWriter = new TIFFImageWriter(this.originatingProvider);
            bogusWriter.imageMetadata = outData;
            bogusWriter.param = param;
            SampleModel sm = imageType.getSampleModel();
            try {
                bogusWriter.setupMetadata(imageType.getColorModel(), sm, sm.getWidth(), sm.getHeight());
                return bogusWriter.imageMetadata;
            }
            catch (IIOException e) {
                return null;
            }
        }
        return outData;
    }

    private TIFFImageMetadata convertStandardImageMetadata(IIOMetadata inData) throws IIOInvalidTreeException {
        if (inData == null) {
            throw new NullPointerException("inData == null!");
        }
        if (!inData.isStandardMetadataFormatSupported()) {
            throw new IllegalArgumentException("inData does not support standard metadata format!");
        }
        TIFFImageMetadata outData = null;
        String formatName = "javax_imageio_1.0";
        Node tree = inData.getAsTree(formatName);
        if (tree != null) {
            ArrayList<TIFFTagSet> tagSets = new ArrayList<TIFFTagSet>(1);
            tagSets.add(BaselineTIFFTagSet.getInstance());
            outData = new TIFFImageMetadata(tagSets);
            outData.setFromTree(formatName, tree);
        }
        return outData;
    }

    private TIFFImageMetadata convertNativeImageMetadata(IIOMetadata inData) throws IIOInvalidTreeException {
        if (inData == null) {
            throw new NullPointerException("inData == null!");
        }
        if (!Arrays.asList(inData.getMetadataFormatNames()).contains("javax_imageio_tiff_image_1.0")) {
            throw new IllegalArgumentException("inData does not support native metadata format!");
        }
        TIFFImageMetadata outData = null;
        String formatName = "javax_imageio_tiff_image_1.0";
        Node tree = inData.getAsTree(formatName);
        if (tree != null) {
            ArrayList<TIFFTagSet> tagSets = new ArrayList<TIFFTagSet>(1);
            tagSets.add(BaselineTIFFTagSet.getInstance());
            outData = new TIFFImageMetadata(tagSets);
            outData.setFromTree(formatName, tree);
        }
        return outData;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    void setupMetadata(ColorModel cm, SampleModel sm, int destWidth, int destHeight) throws IIOException {
        int rowsPerStrip;
        int i;
        TIFFIFD rootIFD = this.imageMetadata.getRootIFD();
        BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
        TIFFField f = rootIFD.getTIFFField(284);
        if (f != null && f.getAsInt(0) != 1) {
            TIFFField planarConfigurationField = new TIFFField(base.getTag(284), 1L);
            rootIFD.addTIFFField(planarConfigurationField);
        }
        char[] extraSamples = null;
        this.photometricInterpretation = -1;
        boolean forcePhotometricInterpretation = false;
        f = rootIFD.getTIFFField(262);
        if (f != null) {
            this.photometricInterpretation = f.getAsInt(0);
            if (this.photometricInterpretation == 3 && !(cm instanceof IndexColorModel)) {
                this.photometricInterpretation = -1;
            } else {
                forcePhotometricInterpretation = true;
            }
        }
        int[] sampleSize = sm.getSampleSize();
        int numBands = sm.getNumBands();
        int numExtraSamples = 0;
        if (numBands > 1 && cm != null && cm.hasAlpha()) {
            --numBands;
            numExtraSamples = 1;
            extraSamples = new char[]{cm.isAlphaPremultiplied() ? (char)'\u0001' : '\u0002'};
        }
        if (numBands == 3) {
            this.nativePhotometricInterpretation = 2;
            if (this.photometricInterpretation == -1) {
                this.photometricInterpretation = 2;
            }
        } else if (sm.getNumBands() == 1 && cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            int r0 = icm.getRed(0);
            int r1 = icm.getRed(1);
            if (!(icm.getMapSize() != 2 || r0 != icm.getGreen(0) || r0 != icm.getBlue(0) || r1 != icm.getGreen(1) || r1 != icm.getBlue(1) || r0 != 0 && r0 != 255 || r1 != 0 && r1 != 255 || r0 == r1)) {
                this.nativePhotometricInterpretation = r0 == 0 ? 1 : 0;
                if (this.photometricInterpretation != 1 && this.photometricInterpretation != 0) {
                    this.photometricInterpretation = r0 == 0 ? 1 : 0;
                }
            } else {
                this.photometricInterpretation = 3;
                this.nativePhotometricInterpretation = 3;
            }
        } else {
            if (cm != null) {
                switch (cm.getColorSpace().getType()) {
                    case 1: {
                        this.nativePhotometricInterpretation = 8;
                        break;
                    }
                    case 3: {
                        this.nativePhotometricInterpretation = 6;
                        break;
                    }
                    case 9: {
                        this.nativePhotometricInterpretation = 5;
                        break;
                    }
                    default: {
                        this.nativePhotometricInterpretation = 1;
                        break;
                    }
                }
            } else {
                this.nativePhotometricInterpretation = 1;
            }
            if (this.photometricInterpretation == -1) {
                this.photometricInterpretation = this.nativePhotometricInterpretation;
            }
        }
        int compressionMode = this.param.getCompressionMode();
        switch (compressionMode) {
            case 2: {
                String compressionType = this.param.getCompressionType();
                if (compressionType == null) {
                    this.compression = 1;
                    break;
                }
                int len = compressionTypes.length;
                for (int i2 = 0; i2 < len; ++i2) {
                    if (!compressionType.equals(compressionTypes[i2])) continue;
                    this.compression = compressionNumbers[i2];
                }
                break;
            }
            case 3: {
                TIFFField compField = rootIFD.getTIFFField(259);
                if (compField != null) {
                    this.compression = compField.getAsInt(0);
                    break;
                }
                this.compression = 1;
                break;
            }
            default: {
                this.compression = 1;
            }
        }
        TIFFField predictorField = rootIFD.getTIFFField(317);
        if (predictorField != null) {
            this.predictor = predictorField.getAsInt(0);
            if (sampleSize[0] != 8 || this.predictor != 1 && this.predictor != 2) {
                this.predictor = 1;
                TIFFField newPredictorField = new TIFFField(base.getTag(317), this.predictor);
                rootIFD.addTIFFField(newPredictorField);
            }
        }
        TIFFField compressionField = new TIFFField(base.getTag(259), this.compression);
        rootIFD.addTIFFField(compressionField);
        boolean isExif = false;
        if (numBands == 3 && sampleSize[0] == 8 && sampleSize[1] == 8 && sampleSize[2] == 8) {
            if (rootIFD.getTIFFField(34665) != null) {
                if (this.compression == 1 && (this.photometricInterpretation == 2 || this.photometricInterpretation == 6)) {
                    isExif = true;
                } else if (this.compression == 6) {
                    isExif = true;
                }
            } else if (compressionMode == 2 && EXIF_JPEG_COMPRESSION_TYPE.equals(this.param.getCompressionType())) {
                isExif = true;
            }
        }
        boolean isJPEGInterchange = isExif && this.compression == 6;
        this.compressor = null;
        if (this.compression == 2) {
            this.compressor = new TIFFRLECompressor();
            if (!forcePhotometricInterpretation) {
                this.photometricInterpretation = 0;
            }
        } else if (this.compression == 3) {
            this.compressor = new TIFFT4Compressor();
            if (!forcePhotometricInterpretation) {
                this.photometricInterpretation = 0;
            }
        } else if (this.compression == 4) {
            this.compressor = new TIFFT6Compressor();
            if (!forcePhotometricInterpretation) {
                this.photometricInterpretation = 0;
            }
        } else if (this.compression == 5) {
            this.compressor = new TIFFLZWCompressor(this.predictor);
        } else if (this.compression == 6) {
            if (!isExif) throw new IIOException("Old JPEG compression not supported!");
            this.compressor = new TIFFExifJPEGCompressor(this.param);
        } else if (this.compression == 7) {
            if (numBands == 3 && sampleSize[0] == 8 && sampleSize[1] == 8 && sampleSize[2] == 8) {
                this.photometricInterpretation = 6;
            } else {
                if (numBands != 1 || sampleSize[0] != 8) throw new IIOException("JPEG compression supported for 1- and 3-band byte images only!");
                this.photometricInterpretation = 1;
            }
            this.compressor = new TIFFJPEGCompressor(this.param);
        } else if (this.compression == 8) {
            this.compressor = new TIFFZLibCompressor(this.param, this.predictor);
        } else if (this.compression == 32773) {
            this.compressor = new TIFFPackBitsCompressor();
        } else if (this.compression == 32946) {
            this.compressor = new TIFFDeflateCompressor(this.param, this.predictor);
        } else {
            f = rootIFD.getTIFFField(266);
            boolean inverseFill = f != null && f.getAsInt(0) == 2;
            this.compressor = inverseFill ? new TIFFLSBCompressor() : new TIFFNullCompressor();
        }
        this.colorConverter = null;
        if (cm != null && cm.getColorSpace().getType() == 5) {
            if (this.photometricInterpretation == 6 && this.compression != 7) {
                this.colorConverter = new TIFFYCbCrColorConverter(this.imageMetadata);
            } else if (this.photometricInterpretation == 8) {
                this.colorConverter = new TIFFCIELabColorConverter();
            }
        }
        if (this.photometricInterpretation == 6 && this.compression != 7) {
            rootIFD.removeTIFFField(530);
            rootIFD.removeTIFFField(531);
            rootIFD.addTIFFField(new TIFFField(base.getTag(530), 3, 2, new char[]{'\u0001', '\u0001'}));
            rootIFD.addTIFFField(new TIFFField(base.getTag(531), 3, 1, new char[]{'\u0002'}));
        }
        TIFFField photometricInterpretationField = new TIFFField(base.getTag(262), this.photometricInterpretation);
        rootIFD.addTIFFField(photometricInterpretationField);
        this.bitsPerSample = new char[numBands + numExtraSamples];
        this.bitDepth = 0;
        for (i = 0; i < numBands; ++i) {
            this.bitDepth = Math.max(this.bitDepth, sampleSize[i]);
        }
        if (this.bitDepth == 3) {
            this.bitDepth = 4;
        } else if (this.bitDepth > 4 && this.bitDepth < 8) {
            this.bitDepth = 8;
        } else if (this.bitDepth > 8 && this.bitDepth < 16) {
            this.bitDepth = 16;
        } else if (this.bitDepth > 16 && this.bitDepth < 32) {
            this.bitDepth = 32;
        } else if (this.bitDepth > 32) {
            this.bitDepth = 64;
        }
        for (i = 0; i < this.bitsPerSample.length; ++i) {
            this.bitsPerSample[i] = (char)this.bitDepth;
        }
        if (this.bitsPerSample.length != 1 || this.bitsPerSample[0] != '\u0001') {
            bitsPerSampleField = new TIFFField(base.getTag(258), 3, this.bitsPerSample.length, this.bitsPerSample);
            rootIFD.addTIFFField(bitsPerSampleField);
        } else {
            int[] bps;
            bitsPerSampleField = rootIFD.getTIFFField(258);
            if (bitsPerSampleField != null && ((bps = bitsPerSampleField.getAsInts()) == null || bps.length != 1 || bps[0] != 1)) {
                rootIFD.removeTIFFField(258);
            }
        }
        f = rootIFD.getTIFFField(339);
        if (f == null && (this.bitDepth == 16 || this.bitDepth == 32 || this.bitDepth == 64)) {
            int dataType = sm.getDataType();
            char sampleFormatValue = this.bitDepth == 16 && dataType == 1 ? (char)'\u0001' : (this.bitDepth == 32 && dataType == 4 || this.bitDepth == 64 && dataType == 5 ? (char)'\u0003' : '\u0002');
            this.sampleFormat = sampleFormatValue;
            char[] sampleFormatArray = new char[this.bitsPerSample.length];
            Arrays.fill(sampleFormatArray, sampleFormatValue);
            TIFFTag sampleFormatTag = base.getTag(339);
            TIFFField sampleFormatField = new TIFFField(sampleFormatTag, 3, sampleFormatArray.length, sampleFormatArray);
            rootIFD.addTIFFField(sampleFormatField);
        } else {
            this.sampleFormat = f != null ? f.getAsInt(0) : 4;
        }
        if (extraSamples != null) {
            TIFFField extraSamplesField = new TIFFField(base.getTag(338), 3, extraSamples.length, extraSamples);
            rootIFD.addTIFFField(extraSamplesField);
        } else {
            rootIFD.removeTIFFField(338);
        }
        TIFFField samplesPerPixelField = new TIFFField(base.getTag(277), this.bitsPerSample.length);
        rootIFD.addTIFFField(samplesPerPixelField);
        if (this.photometricInterpretation == 3 && cm instanceof IndexColorModel) {
            char[] colorMap = new char[3 * ('\u0001' << this.bitsPerSample[0])];
            IndexColorModel icm = (IndexColorModel)cm;
            int mapSize = '\u0001' << this.bitsPerSample[0];
            int indexBound = Math.min(mapSize, icm.getMapSize());
            for (int i3 = 0; i3 < indexBound; ++i3) {
                colorMap[i3] = (char)(icm.getRed(i3) * 65535 / 255);
                colorMap[mapSize + i3] = (char)(icm.getGreen(i3) * 65535 / 255);
                colorMap[2 * mapSize + i3] = (char)(icm.getBlue(i3) * 65535 / 255);
            }
            TIFFField colorMapField = new TIFFField(base.getTag(320), 3, colorMap.length, colorMap);
            rootIFD.addTIFFField(colorMapField);
        } else {
            rootIFD.removeTIFFField(320);
        }
        if (cm != null && rootIFD.getTIFFField(34675) == null && ImageUtil.isNonStandardICCColorSpace(cm.getColorSpace())) {
            ICC_ColorSpace iccColorSpace = (ICC_ColorSpace)cm.getColorSpace();
            byte[] iccProfileData = iccColorSpace.getProfile().getData();
            TIFFField iccProfileField = new TIFFField(base.getTag(34675), 7, iccProfileData.length, iccProfileData);
            rootIFD.addTIFFField(iccProfileField);
        }
        TIFFField XResolutionField = rootIFD.getTIFFField(282);
        TIFFField YResolutionField = rootIFD.getTIFFField(283);
        if (XResolutionField == null && YResolutionField == null) {
            long[][] resRational = new long[1][2];
            resRational[0] = new long[2];
            TIFFField ResolutionUnitField = rootIFD.getTIFFField(296);
            if (ResolutionUnitField == null && rootIFD.getTIFFField(286) == null && rootIFD.getTIFFField(287) == null) {
                resRational[0][0] = 1L;
                resRational[0][1] = 1L;
                ResolutionUnitField = new TIFFField(rootIFD.getTag(296), 1L);
                rootIFD.addTIFFField(ResolutionUnitField);
            } else {
                int resolutionUnit = ResolutionUnitField != null ? ResolutionUnitField.getAsInt(0) : 2;
                int maxDimension = Math.max(destWidth, destHeight);
                switch (resolutionUnit) {
                    case 2: {
                        resRational[0][0] = maxDimension;
                        resRational[0][1] = 4L;
                        break;
                    }
                    case 3: {
                        resRational[0][0] = 100L * (long)maxDimension;
                        resRational[0][1] = 1016L;
                        break;
                    }
                    default: {
                        resRational[0][0] = 1L;
                        resRational[0][1] = 1L;
                    }
                }
            }
            XResolutionField = new TIFFField(rootIFD.getTag(282), 5, 1, resRational);
            rootIFD.addTIFFField(XResolutionField);
            YResolutionField = new TIFFField(rootIFD.getTag(283), 5, 1, resRational);
            rootIFD.addTIFFField(YResolutionField);
        } else if (XResolutionField == null && YResolutionField != null) {
            long[] yResolution = (long[])YResolutionField.getAsRational(0).clone();
            XResolutionField = new TIFFField(rootIFD.getTag(282), 5, 1, yResolution);
            rootIFD.addTIFFField(XResolutionField);
        } else if (XResolutionField != null && YResolutionField == null) {
            long[] xResolution = (long[])XResolutionField.getAsRational(0).clone();
            YResolutionField = new TIFFField(rootIFD.getTag(283), 5, 1, xResolution);
            rootIFD.addTIFFField(YResolutionField);
        }
        int width = destWidth;
        TIFFField imageWidthField = new TIFFField(base.getTag(256), width);
        rootIFD.addTIFFField(imageWidthField);
        int height = destHeight;
        TIFFField imageLengthField = new TIFFField(base.getTag(257), height);
        rootIFD.addTIFFField(imageLengthField);
        TIFFField rowsPerStripField = rootIFD.getTIFFField(278);
        if (rowsPerStripField != null) {
            rowsPerStrip = rowsPerStripField.getAsInt(0);
            if (rowsPerStrip < 0) {
                rowsPerStrip = height;
            }
        } else {
            int bitsPerPixel = this.bitDepth * (numBands + numExtraSamples);
            int bytesPerRow = (bitsPerPixel * width + 7) / 8;
            rowsPerStrip = Math.max(Math.max(8192 / bytesPerRow, 1), 8);
        }
        rowsPerStrip = Math.min(rowsPerStrip, height);
        boolean useTiling = false;
        int tilingMode = this.param.getTilingMode();
        if (tilingMode == 0 || tilingMode == 1) {
            this.tileWidth = width;
            this.tileLength = rowsPerStrip;
            useTiling = false;
        } else if (tilingMode == 2) {
            this.tileWidth = this.param.getTileWidth();
            this.tileLength = this.param.getTileHeight();
            useTiling = true;
        } else {
            if (tilingMode != 3) throw new IIOException("Illegal value of tilingMode!");
            f = rootIFD.getTIFFField(322);
            if (f == null) {
                this.tileWidth = width;
                useTiling = false;
            } else {
                this.tileWidth = f.getAsInt(0);
                useTiling = true;
            }
            f = rootIFD.getTIFFField(323);
            if (f == null) {
                this.tileLength = rowsPerStrip;
            } else {
                this.tileLength = f.getAsInt(0);
                useTiling = true;
            }
        }
        if (this.compression == 7) {
            int subX;
            int subY;
            if (numBands == 1) {
                subY = 1;
                subX = 1;
            } else {
                subY = 2;
                subX = 2;
            }
            if (useTiling) {
                int MCUMultipleX = 8 * subX;
                int MCUMultipleY = 8 * subY;
                this.tileWidth = Math.max(MCUMultipleX * ((this.tileWidth + MCUMultipleX / 2) / MCUMultipleX), MCUMultipleX);
                this.tileLength = Math.max(MCUMultipleY * ((this.tileLength + MCUMultipleY / 2) / MCUMultipleY), MCUMultipleY);
            } else if (rowsPerStrip < height) {
                int MCUMultiple = 8 * Math.max(subX, subY);
                rowsPerStrip = this.tileLength = Math.max(MCUMultiple * ((this.tileLength + MCUMultiple / 2) / MCUMultiple), MCUMultiple);
            }
            rootIFD.removeTIFFField(513);
            rootIFD.removeTIFFField(514);
            rootIFD.removeTIFFField(512);
            rootIFD.removeTIFFField(515);
            rootIFD.removeTIFFField(517);
            rootIFD.removeTIFFField(518);
            rootIFD.removeTIFFField(519);
            rootIFD.removeTIFFField(520);
            rootIFD.removeTIFFField(521);
        } else if (isJPEGInterchange) {
            this.tileWidth = width;
            this.tileLength = height;
        } else if (useTiling) {
            int tileLengthRemainder;
            int tileWidthRemainder = this.tileWidth % 16;
            if (tileWidthRemainder != 0) {
                this.tileWidth = Math.max(16 * ((this.tileWidth + 8) / 16), 16);
                this.processWarningOccurred(this.currentImage, "Tile width rounded to multiple of 16.");
            }
            if ((tileLengthRemainder = this.tileLength % 16) != 0) {
                this.tileLength = Math.max(16 * ((this.tileLength + 8) / 16), 16);
                this.processWarningOccurred(this.currentImage, "Tile height rounded to multiple of 16.");
            }
        }
        this.tilesAcross = (width + this.tileWidth - 1) / this.tileWidth;
        this.tilesDown = (height + this.tileLength - 1) / this.tileLength;
        if (!useTiling) {
            this.isTiled = false;
            rootIFD.removeTIFFField(322);
            rootIFD.removeTIFFField(323);
            rootIFD.removeTIFFField(324);
            rootIFD.removeTIFFField(325);
            rowsPerStripField = new TIFFField(base.getTag(278), rowsPerStrip);
            rootIFD.addTIFFField(rowsPerStripField);
            TIFFField stripOffsetsField = new TIFFField(base.getTag(273), 4, this.tilesDown);
            rootIFD.addTIFFField(stripOffsetsField);
            TIFFField stripByteCountsField = new TIFFField(base.getTag(279), 4, this.tilesDown);
            rootIFD.addTIFFField(stripByteCountsField);
        } else {
            this.isTiled = true;
            rootIFD.removeTIFFField(278);
            rootIFD.removeTIFFField(273);
            rootIFD.removeTIFFField(279);
            TIFFField tileWidthField = new TIFFField(base.getTag(322), this.tileWidth);
            rootIFD.addTIFFField(tileWidthField);
            TIFFField tileLengthField = new TIFFField(base.getTag(323), this.tileLength);
            rootIFD.addTIFFField(tileLengthField);
            TIFFField tileOffsetsField = new TIFFField(base.getTag(324), 4, this.tilesDown * this.tilesAcross);
            rootIFD.addTIFFField(tileOffsetsField);
            TIFFField tileByteCountsField = new TIFFField(base.getTag(325), 4, this.tilesDown * this.tilesAcross);
            rootIFD.addTIFFField(tileByteCountsField);
        }
        if (!isExif) return;
        boolean isPrimaryIFD = this.isEncodingEmpty();
        if (this.compression == 6) {
            rootIFD.removeTIFFField(256);
            rootIFD.removeTIFFField(257);
            rootIFD.removeTIFFField(258);
            if (isPrimaryIFD) {
                rootIFD.removeTIFFField(259);
            }
            rootIFD.removeTIFFField(262);
            rootIFD.removeTIFFField(273);
            rootIFD.removeTIFFField(277);
            rootIFD.removeTIFFField(278);
            rootIFD.removeTIFFField(279);
            rootIFD.removeTIFFField(284);
            if (rootIFD.getTIFFField(296) == null) {
                f = new TIFFField(base.getTag(296), 2L);
                rootIFD.addTIFFField(f);
            }
            if (isPrimaryIFD) {
                rootIFD.removeTIFFField(513);
                rootIFD.removeTIFFField(514);
                rootIFD.removeTIFFField(530);
                if (rootIFD.getTIFFField(531) == null) {
                    f = new TIFFField(base.getTag(531), 3, 1, new char[]{'\u0001'});
                    rootIFD.addTIFFField(f);
                }
            } else {
                f = new TIFFField(base.getTag(513), 4, 1);
                rootIFD.addTIFFField(f);
                f = new TIFFField(base.getTag(514), 4, 1);
                rootIFD.addTIFFField(f);
                rootIFD.removeTIFFField(530);
            }
        } else {
            if (rootIFD.getTIFFField(296) == null) {
                f = new TIFFField(base.getTag(296), 2L);
                rootIFD.addTIFFField(f);
            }
            rootIFD.removeTIFFField(513);
            rootIFD.removeTIFFField(514);
            if (this.photometricInterpretation == 2) {
                rootIFD.removeTIFFField(529);
                rootIFD.removeTIFFField(530);
                rootIFD.removeTIFFField(531);
            }
        }
        ExifTIFFTagSet exifTags = ExifTIFFTagSet.getInstance();
        TIFFIFD exifIFD = null;
        f = rootIFD.getTIFFField(34665);
        if (f != null && f.hasDirectory()) {
            exifIFD = TIFFIFD.getDirectoryAsIFD(f.getDirectory());
        } else if (isPrimaryIFD) {
            ArrayList<TIFFTagSet> exifTagSets = new ArrayList<TIFFTagSet>(1);
            exifTagSets.add(exifTags);
            exifIFD = new TIFFIFD(exifTagSets);
            ExifParentTIFFTagSet tagSet = ExifParentTIFFTagSet.getInstance();
            TIFFTag exifIFDTag = tagSet.getTag(34665);
            rootIFD.addTIFFField(new TIFFField(exifIFDTag, 4, 1L, exifIFD));
        }
        if (exifIFD == null) return;
        if (exifIFD.getTIFFField(36864) == null) {
            f = new TIFFField(exifTags.getTag(36864), 7, 4, "0220".getBytes(StandardCharsets.US_ASCII));
            exifIFD.addTIFFField(f);
        }
        if (this.compression == 6) {
            if (exifIFD.getTIFFField(37121) == null) {
                f = new TIFFField(exifTags.getTag(37121), 7, 4, new byte[]{1, 2, 3, 0});
                exifIFD.addTIFFField(f);
            }
        } else {
            exifIFD.removeTIFFField(37121);
            exifIFD.removeTIFFField(37122);
        }
        if (exifIFD.getTIFFField(40960) == null) {
            f = new TIFFField(exifTags.getTag(40960), 7, 4, new byte[]{48, 49, 48, 48});
            exifIFD.addTIFFField(f);
        }
        if (exifIFD.getTIFFField(40961) == null) {
            f = new TIFFField(exifTags.getTag(40961), 3, 1, new char[]{'\u0001'});
            exifIFD.addTIFFField(f);
        }
        if (this.compression == 6) {
            if (exifIFD.getTIFFField(40962) == null) {
                f = new TIFFField(exifTags.getTag(40962), width);
                exifIFD.addTIFFField(f);
            }
            if (exifIFD.getTIFFField(40963) != null) return;
            f = new TIFFField(exifTags.getTag(40963), height);
            exifIFD.addTIFFField(f);
            return;
        } else {
            exifIFD.removeTIFFField(40965);
        }
    }

    ImageTypeSpecifier getImageType() {
        return this.imageType;
    }

    private int writeTile(Rectangle tileRect, TIFFCompressor compressor) throws IOException {
        SampleModel sm;
        boolean isPadded;
        Rectangle activeRect;
        Rectangle imageBounds = new Rectangle(this.image.getMinX(), this.image.getMinY(), this.image.getWidth(), this.image.getHeight());
        if (!this.isTiled) {
            tileRect = activeRect = tileRect.intersection(imageBounds);
            isPadded = false;
        } else if (imageBounds.contains(tileRect)) {
            activeRect = tileRect;
            isPadded = false;
        } else {
            activeRect = imageBounds.intersection(tileRect);
            isPadded = true;
        }
        if (activeRect.isEmpty()) {
            return 0;
        }
        int minX = tileRect.x;
        int minY = tileRect.y;
        int width = tileRect.width;
        int height = tileRect.height;
        if (this.isImageSimple) {
            SampleModel sm2 = this.image.getSampleModel();
            Raster raster = this.image.getData(activeRect);
            if (isPadded) {
                WritableRaster wr = raster.createCompatibleWritableRaster(minX, minY, width, height);
                wr.setRect(raster);
                raster = wr;
            }
            if (this.isBilevel) {
                byte[] buf = ImageUtil.getPackedBinaryData(raster, tileRect);
                if (this.isInverted) {
                    DataBuffer dbb = raster.getDataBuffer();
                    if (dbb instanceof DataBufferByte && buf == ((DataBufferByte)dbb).getData()) {
                        byte[] bbuf = new byte[buf.length];
                        int len = buf.length;
                        for (int i = 0; i < len; ++i) {
                            bbuf[i] = (byte)(buf[i] ^ 0xFF);
                        }
                        buf = bbuf;
                    } else {
                        int len = buf.length;
                        int i = 0;
                        while (i < len) {
                            int n = i++;
                            buf[n] = (byte)(buf[n] ^ 0xFF);
                        }
                    }
                }
                return compressor.encode(buf, 0, width, height, this.sampleSize, (tileRect.width + 7) / 8);
            }
            if (this.bitDepth == 8 && sm2.getDataType() == 0) {
                ComponentSampleModel csm = (ComponentSampleModel)raster.getSampleModel();
                byte[] buf = ((DataBufferByte)raster.getDataBuffer()).getData();
                int off = csm.getOffset(minX - raster.getSampleModelTranslateX(), minY - raster.getSampleModelTranslateY());
                return compressor.encode(buf, off, width, height, this.sampleSize, csm.getScanlineStride());
            }
        }
        int xOffset = minX;
        int xSkip = this.periodX;
        int yOffset = minY;
        int ySkip = this.periodY;
        int hpixels = (width + xSkip - 1) / xSkip;
        int vpixels = (height + ySkip - 1) / ySkip;
        if (hpixels == 0 || vpixels == 0) {
            return 0;
        }
        xOffset *= this.numBands;
        xSkip *= this.numBands;
        int samplesPerByte = 8 / this.bitDepth;
        int numSamples = width * this.numBands;
        int bytesPerRow = hpixels * this.numBands;
        if (this.bitDepth < 8) {
            bytesPerRow = (bytesPerRow + samplesPerByte - 1) / samplesPerByte;
        } else if (this.bitDepth == 16) {
            bytesPerRow *= 2;
        } else if (this.bitDepth == 32) {
            bytesPerRow *= 4;
        } else if (this.bitDepth == 64) {
            bytesPerRow *= 8;
        }
        int[] samples = null;
        float[] fsamples = null;
        double[] dsamples = null;
        if (this.sampleFormat == 3) {
            if (this.bitDepth == 32) {
                fsamples = new float[numSamples];
            } else {
                dsamples = new double[numSamples];
            }
        } else {
            samples = new int[numSamples];
        }
        byte[] currTile = new byte[bytesPerRow * vpixels];
        if (!this.isInverted && !this.isRescaling && this.sourceBands == null && this.periodX == 1 && this.periodY == 1 && this.colorConverter == null && (sm = this.image.getSampleModel()) instanceof ComponentSampleModel && this.bitDepth == 8 && sm.getDataType() == 0) {
            Raster raster = this.image.getData(activeRect);
            if (isPadded) {
                WritableRaster wr = raster.createCompatibleWritableRaster(minX, minY, width, height);
                wr.setRect(raster);
                raster = wr;
            }
            ComponentSampleModel csm = (ComponentSampleModel)raster.getSampleModel();
            int[] bankIndices = csm.getBankIndices();
            byte[][] bankData = ((DataBufferByte)raster.getDataBuffer()).getBankData();
            int lineStride = csm.getScanlineStride();
            int pixelStride = csm.getPixelStride();
            for (int k = 0; k < this.numBands; ++k) {
                byte[] bandData = bankData[bankIndices[k]];
                int lineOffset = csm.getOffset(raster.getMinX() - raster.getSampleModelTranslateX(), raster.getMinY() - raster.getSampleModelTranslateY(), k);
                int idx = k;
                for (int j = 0; j < vpixels; ++j) {
                    int offset = lineOffset;
                    for (int i = 0; i < hpixels; ++i) {
                        currTile[idx] = bandData[offset];
                        idx += this.numBands;
                        offset += pixelStride;
                    }
                    lineOffset += lineStride;
                }
            }
            return compressor.encode(currTile, 0, width, height, this.sampleSize, width * this.numBands);
        }
        int tcount = 0;
        int activeMinX = activeRect.x;
        int activeMinY = activeRect.y;
        int activeMaxY = activeMinY + activeRect.height - 1;
        int activeWidth = activeRect.width;
        SampleModel rowSampleModel = null;
        if (isPadded) {
            rowSampleModel = this.image.getSampleModel().createCompatibleSampleModel(width, 1);
        }
        block12: for (int row = yOffset; row < yOffset + height; row += ySkip) {
            Raster ras = null;
            if (isPadded) {
                WritableRaster wr = Raster.createWritableRaster(rowSampleModel, new Point(minX, row));
                if (row >= activeMinY && row <= activeMaxY) {
                    Rectangle rect = new Rectangle(activeMinX, row, activeWidth, 1);
                    ras = this.image.getData(rect);
                    wr.setRect(ras);
                }
                ras = wr;
            } else {
                Rectangle rect = new Rectangle(minX, row, width, 1);
                ras = this.image.getData(rect);
            }
            if (this.sourceBands != null) {
                ras = ras.createChild(minX, row, width, 1, minX, row, this.sourceBands);
            }
            if (this.sampleFormat == 3) {
                if (fsamples != null) {
                    ras.getPixels(minX, row, width, 1, fsamples);
                } else {
                    ras.getPixels(minX, row, width, 1, dsamples);
                }
            } else {
                ras.getPixels(minX, row, width, 1, samples);
                if (this.nativePhotometricInterpretation == 1 && this.photometricInterpretation == 0 || this.nativePhotometricInterpretation == 0 && this.photometricInterpretation == 1) {
                    int bitMask = (1 << this.bitDepth) - 1;
                    int s = 0;
                    while (s < numSamples) {
                        int n = s++;
                        samples[n] = samples[n] ^ bitMask;
                    }
                }
            }
            if (this.colorConverter != null) {
                int idx = 0;
                float[] result = new float[3];
                if (this.sampleFormat == 3) {
                    if (this.bitDepth == 32) {
                        for (int i = 0; i < width; ++i) {
                            float r = fsamples[idx];
                            float g = fsamples[idx + 1];
                            b = fsamples[idx + 2];
                            this.colorConverter.fromRGB(r, g, b, result);
                            fsamples[idx] = result[0];
                            fsamples[idx + 1] = result[1];
                            fsamples[idx + 2] = result[2];
                            idx += 3;
                        }
                    } else {
                        for (int i = 0; i < width; ++i) {
                            float r = (float)dsamples[idx];
                            float g = (float)dsamples[idx + 1];
                            b = (float)dsamples[idx + 2];
                            this.colorConverter.fromRGB(r, g, b, result);
                            dsamples[idx] = result[0];
                            dsamples[idx + 1] = result[1];
                            dsamples[idx + 2] = result[2];
                            idx += 3;
                        }
                    }
                } else {
                    for (int i = 0; i < width; ++i) {
                        float r = samples[idx];
                        float g = samples[idx + 1];
                        b = samples[idx + 2];
                        this.colorConverter.fromRGB(r, g, b, result);
                        samples[idx] = (int)result[0];
                        samples[idx + 1] = (int)result[1];
                        samples[idx + 2] = (int)result[2];
                        idx += 3;
                    }
                }
            }
            int tmp = 0;
            int pos = 0;
            switch (this.bitDepth) {
                case 1: 
                case 2: 
                case 4: {
                    if (this.isRescaling) {
                        for (s = 0; s < numSamples; s += xSkip) {
                            byte val = this.scale0[samples[s]];
                            tmp = tmp << this.bitDepth | val;
                            if (++pos != samplesPerByte) continue;
                            currTile[tcount++] = (byte)tmp;
                            tmp = 0;
                            pos = 0;
                        }
                    } else {
                        for (s = 0; s < numSamples; s += xSkip) {
                            byte val = (byte)samples[s];
                            tmp = tmp << this.bitDepth | val;
                            if (++pos != samplesPerByte) continue;
                            currTile[tcount++] = (byte)tmp;
                            tmp = 0;
                            pos = 0;
                        }
                    }
                    if (pos == 0) continue block12;
                    currTile[tcount++] = (byte)(tmp <<= (8 / this.bitDepth - pos) * this.bitDepth);
                    continue block12;
                }
                case 8: {
                    if (this.numBands == 1) {
                        if (this.isRescaling) {
                            for (int s = 0; s < numSamples; s += xSkip) {
                                currTile[tcount++] = this.scale0[samples[s]];
                            }
                            continue block12;
                        }
                        for (int s = 0; s < numSamples; s += xSkip) {
                            currTile[tcount++] = (byte)samples[s];
                        }
                        continue block12;
                    }
                    if (this.isRescaling) {
                        for (int s = 0; s < numSamples; s += xSkip) {
                            for (int b = 0; b < this.numBands; ++b) {
                                currTile[tcount++] = this.scale[b][samples[s + b]];
                            }
                        }
                        continue block12;
                    }
                    for (int s = 0; s < numSamples; s += xSkip) {
                        for (int b = 0; b < this.numBands; ++b) {
                            currTile[tcount++] = (byte)samples[s + b];
                        }
                    }
                    continue block12;
                }
                case 16: {
                    if (this.isRescaling) {
                        if (this.stream.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                            for (int s = 0; s < numSamples; s += xSkip) {
                                for (int b = 0; b < this.numBands; ++b) {
                                    int sample = samples[s + b];
                                    currTile[tcount++] = this.scaleh[b][sample];
                                    currTile[tcount++] = this.scalel[b][sample];
                                }
                            }
                            continue block12;
                        }
                        for (int s = 0; s < numSamples; s += xSkip) {
                            for (int b = 0; b < this.numBands; ++b) {
                                int sample = samples[s + b];
                                currTile[tcount++] = this.scalel[b][sample];
                                currTile[tcount++] = this.scaleh[b][sample];
                            }
                        }
                        continue block12;
                    }
                    if (this.stream.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                        for (int s = 0; s < numSamples; s += xSkip) {
                            for (int b = 0; b < this.numBands; ++b) {
                                int sample = samples[s + b];
                                currTile[tcount++] = (byte)(sample >>> 8 & 0xFF);
                                currTile[tcount++] = (byte)(sample & 0xFF);
                            }
                        }
                        continue block12;
                    }
                    for (int s = 0; s < numSamples; s += xSkip) {
                        for (int b = 0; b < this.numBands; ++b) {
                            int sample = samples[s + b];
                            currTile[tcount++] = (byte)(sample & 0xFF);
                            currTile[tcount++] = (byte)(sample >>> 8 & 0xFF);
                        }
                    }
                    continue block12;
                }
                case 32: {
                    if (this.sampleFormat == 3) {
                        if (this.stream.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                            for (int s = 0; s < numSamples; s += xSkip) {
                                for (int b = 0; b < this.numBands; ++b) {
                                    float fsample = fsamples[s + b];
                                    int isample = Float.floatToIntBits(fsample);
                                    currTile[tcount++] = (byte)((isample & 0xFF000000) >> 24);
                                    currTile[tcount++] = (byte)((isample & 0xFF0000) >> 16);
                                    currTile[tcount++] = (byte)((isample & 0xFF00) >> 8);
                                    currTile[tcount++] = (byte)(isample & 0xFF);
                                }
                            }
                            continue block12;
                        }
                        for (int s = 0; s < numSamples; s += xSkip) {
                            for (int b = 0; b < this.numBands; ++b) {
                                float fsample = fsamples[s + b];
                                int isample = Float.floatToIntBits(fsample);
                                currTile[tcount++] = (byte)(isample & 0xFF);
                                currTile[tcount++] = (byte)((isample & 0xFF00) >> 8);
                                currTile[tcount++] = (byte)((isample & 0xFF0000) >> 16);
                                currTile[tcount++] = (byte)((isample & 0xFF000000) >> 24);
                            }
                        }
                        continue block12;
                    }
                    if (this.isRescaling) {
                        long sampleOut;
                        int b;
                        int s;
                        long[] maxIn = new long[this.numBands];
                        long[] halfIn = new long[this.numBands];
                        long maxOut = (1L << (int)((long)this.bitDepth)) - 1L;
                        for (int b2 = 0; b2 < this.numBands; ++b2) {
                            maxIn[b2] = (1L << (int)((long)this.sampleSize[b2])) - 1L;
                            halfIn[b2] = maxIn[b2] / 2L;
                        }
                        if (this.stream.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                            for (s = 0; s < numSamples; s += xSkip) {
                                for (b = 0; b < this.numBands; ++b) {
                                    sampleOut = ((long)samples[s + b] * maxOut + halfIn[b]) / maxIn[b];
                                    currTile[tcount++] = (byte)((sampleOut & 0xFFFFFFFFFF000000L) >> 24);
                                    currTile[tcount++] = (byte)((sampleOut & 0xFF0000L) >> 16);
                                    currTile[tcount++] = (byte)((sampleOut & 0xFF00L) >> 8);
                                    currTile[tcount++] = (byte)(sampleOut & 0xFFL);
                                }
                            }
                            continue block12;
                        }
                        for (s = 0; s < numSamples; s += xSkip) {
                            for (b = 0; b < this.numBands; ++b) {
                                sampleOut = ((long)samples[s + b] * maxOut + halfIn[b]) / maxIn[b];
                                currTile[tcount++] = (byte)(sampleOut & 0xFFL);
                                currTile[tcount++] = (byte)((sampleOut & 0xFF00L) >> 8);
                                currTile[tcount++] = (byte)((sampleOut & 0xFF0000L) >> 16);
                                currTile[tcount++] = (byte)((sampleOut & 0xFFFFFFFFFF000000L) >> 24);
                            }
                        }
                        continue block12;
                    }
                    if (this.stream.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                        for (int s = 0; s < numSamples; s += xSkip) {
                            for (int b = 0; b < this.numBands; ++b) {
                                int isample = samples[s + b];
                                currTile[tcount++] = (byte)((isample & 0xFF000000) >> 24);
                                currTile[tcount++] = (byte)((isample & 0xFF0000) >> 16);
                                currTile[tcount++] = (byte)((isample & 0xFF00) >> 8);
                                currTile[tcount++] = (byte)(isample & 0xFF);
                            }
                        }
                        continue block12;
                    }
                    for (int s = 0; s < numSamples; s += xSkip) {
                        for (int b = 0; b < this.numBands; ++b) {
                            int isample = samples[s + b];
                            currTile[tcount++] = (byte)(isample & 0xFF);
                            currTile[tcount++] = (byte)((isample & 0xFF00) >> 8);
                            currTile[tcount++] = (byte)((isample & 0xFF0000) >> 16);
                            currTile[tcount++] = (byte)((isample & 0xFF000000) >> 24);
                        }
                    }
                    continue block12;
                }
                case 64: {
                    if (this.sampleFormat != 3) continue block12;
                    if (this.stream.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                        for (int s = 0; s < numSamples; s += xSkip) {
                            for (int b = 0; b < this.numBands; ++b) {
                                double dsample = dsamples[s + b];
                                long lsample = Double.doubleToLongBits(dsample);
                                currTile[tcount++] = (byte)((lsample & 0xFF00000000000000L) >> 56);
                                currTile[tcount++] = (byte)((lsample & 0xFF000000000000L) >> 48);
                                currTile[tcount++] = (byte)((lsample & 0xFF0000000000L) >> 40);
                                currTile[tcount++] = (byte)((lsample & 0xFF00000000L) >> 32);
                                currTile[tcount++] = (byte)((lsample & 0xFF000000L) >> 24);
                                currTile[tcount++] = (byte)((lsample & 0xFF0000L) >> 16);
                                currTile[tcount++] = (byte)((lsample & 0xFF00L) >> 8);
                                currTile[tcount++] = (byte)(lsample & 0xFFL);
                            }
                        }
                        continue block12;
                    }
                    for (int s = 0; s < numSamples; s += xSkip) {
                        for (int b = 0; b < this.numBands; ++b) {
                            double dsample = dsamples[s + b];
                            long lsample = Double.doubleToLongBits(dsample);
                            currTile[tcount++] = (byte)(lsample & 0xFFL);
                            currTile[tcount++] = (byte)((lsample & 0xFF00L) >> 8);
                            currTile[tcount++] = (byte)((lsample & 0xFF0000L) >> 16);
                            currTile[tcount++] = (byte)((lsample & 0xFF000000L) >> 24);
                            currTile[tcount++] = (byte)((lsample & 0xFF00000000L) >> 32);
                            currTile[tcount++] = (byte)((lsample & 0xFF0000000000L) >> 40);
                            currTile[tcount++] = (byte)((lsample & 0xFF000000000000L) >> 48);
                            currTile[tcount++] = (byte)((lsample & 0xFF00000000000000L) >> 56);
                        }
                    }
                    continue block12;
                }
            }
        }
        int[] bitsPerSample = new int[this.numBands];
        for (int i = 0; i < bitsPerSample.length; ++i) {
            bitsPerSample[i] = this.bitDepth;
        }
        int byteCount = compressor.encode(currTile, 0, hpixels, vpixels, bitsPerSample, bytesPerRow);
        return byteCount;
    }

    private boolean equals(int[] s0, int[] s1) {
        if (s0 == null || s1 == null) {
            return false;
        }
        if (s0.length != s1.length) {
            return false;
        }
        for (int i = 0; i < s0.length; ++i) {
            if (s0[i] == s1[i]) continue;
            return false;
        }
        return true;
    }

    private void initializeScaleTables(int[] sampleSize) {
        if (this.bitDepth == this.scalingBitDepth && this.equals(sampleSize, this.sampleSize)) {
            return;
        }
        this.isRescaling = false;
        this.scalingBitDepth = -1;
        this.scaleh = null;
        this.scalel = null;
        this.scale = null;
        this.scale0 = null;
        this.sampleSize = sampleSize;
        if (this.bitDepth <= 16) {
            for (int b = 0; b < this.numBands; ++b) {
                if (sampleSize[b] == this.bitDepth) continue;
                this.isRescaling = true;
                break;
            }
        }
        if (!this.isRescaling) {
            return;
        }
        this.scalingBitDepth = this.bitDepth;
        int maxOutSample = (1 << this.bitDepth) - 1;
        if (this.bitDepth <= 8) {
            this.scale = new byte[this.numBands][];
            for (int b = 0; b < this.numBands; ++b) {
                int maxInSample = (1 << sampleSize[b]) - 1;
                int halfMaxInSample = maxInSample / 2;
                this.scale[b] = new byte[maxInSample + 1];
                for (int s = 0; s <= maxInSample; ++s) {
                    this.scale[b][s] = (byte)((s * maxOutSample + halfMaxInSample) / maxInSample);
                }
            }
            this.scale0 = this.scale[0];
            this.scalel = null;
            this.scaleh = null;
        } else if (this.bitDepth <= 16) {
            this.scaleh = new byte[this.numBands][];
            this.scalel = new byte[this.numBands][];
            for (int b = 0; b < this.numBands; ++b) {
                int maxInSample = (1 << sampleSize[b]) - 1;
                int halfMaxInSample = maxInSample / 2;
                this.scaleh[b] = new byte[maxInSample + 1];
                this.scalel[b] = new byte[maxInSample + 1];
                for (int s = 0; s <= maxInSample; ++s) {
                    int val = (s * maxOutSample + halfMaxInSample) / maxInSample;
                    this.scaleh[b][s] = (byte)(val >> 8);
                    this.scalel[b][s] = (byte)(val & 0xFF);
                }
            }
            this.scale = null;
            this.scale0 = null;
        }
    }

    @Override
    public void write(IIOMetadata sm, IIOImage iioimage, ImageWriteParam p) throws IOException {
        if (this.stream == null) {
            throw new IllegalStateException("output == null!");
        }
        this.markPositions();
        this.write(sm, iioimage, p, true, true);
        if (this.abortRequested()) {
            this.resetPositions();
        }
    }

    private void writeHeader() throws IOException {
        this.byteOrder = this.streamMetadata != null ? this.streamMetadata.byteOrder : ByteOrder.BIG_ENDIAN;
        this.stream.setByteOrder(this.byteOrder);
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            this.stream.writeShort(19789);
        } else {
            this.stream.writeShort(18761);
        }
        this.stream.writeShort(42);
        this.stream.writeInt(0);
        this.nextSpace = this.stream.getStreamPosition();
        this.headerPosition = this.nextSpace - 8L;
    }

    private void write(IIOMetadata sm, IIOImage iioimage, ImageWriteParam p, boolean writeHeader, boolean writeData) throws IOException {
        if (this.stream == null) {
            throw new IllegalStateException("output == null!");
        }
        if (iioimage == null) {
            throw new IllegalArgumentException("image == null!");
        }
        if (iioimage.hasRaster() && !this.canWriteRasters()) {
            throw new UnsupportedOperationException("TIFF ImageWriter cannot write Rasters!");
        }
        this.image = iioimage.getRenderedImage();
        SampleModel sampleModel = this.image.getSampleModel();
        this.sourceXOffset = this.image.getMinX();
        this.sourceYOffset = this.image.getMinY();
        this.sourceWidth = this.image.getWidth();
        this.sourceHeight = this.image.getHeight();
        Rectangle imageBounds = new Rectangle(this.sourceXOffset, this.sourceYOffset, this.sourceWidth, this.sourceHeight);
        ColorModel colorModel = null;
        if (p == null) {
            this.param = this.getDefaultWriteParam();
            this.sourceBands = null;
            this.periodX = 1;
            this.periodY = 1;
            this.numBands = sampleModel.getNumBands();
            colorModel = this.image.getColorModel();
        } else {
            ColorModel cm;
            this.param = p;
            Rectangle sourceRegion = this.param.getSourceRegion();
            if (sourceRegion != null) {
                sourceRegion = sourceRegion.intersection(imageBounds);
                this.sourceXOffset = sourceRegion.x;
                this.sourceYOffset = sourceRegion.y;
                this.sourceWidth = sourceRegion.width;
                this.sourceHeight = sourceRegion.height;
            }
            int gridX = this.param.getSubsamplingXOffset();
            int gridY = this.param.getSubsamplingYOffset();
            this.sourceXOffset += gridX;
            this.sourceYOffset += gridY;
            this.sourceWidth -= gridX;
            this.sourceHeight -= gridY;
            this.periodX = this.param.getSourceXSubsampling();
            this.periodY = this.param.getSourceYSubsampling();
            int[] sBands = this.param.getSourceBands();
            if (sBands != null) {
                this.sourceBands = sBands;
                this.numBands = this.sourceBands.length;
            } else {
                this.numBands = sampleModel.getNumBands();
            }
            ImageTypeSpecifier destType = p.getDestinationType();
            if (destType != null && (cm = destType.getColorModel()).getNumComponents() == this.numBands) {
                colorModel = cm;
            }
            if (colorModel == null) {
                colorModel = this.image.getColorModel();
            }
        }
        this.imageType = new ImageTypeSpecifier(colorModel, sampleModel);
        ImageUtil.canEncodeImage(this, this.imageType);
        int destWidth = (this.sourceWidth + this.periodX - 1) / this.periodX;
        int destHeight = (this.sourceHeight + this.periodY - 1) / this.periodY;
        if (destWidth <= 0 || destHeight <= 0) {
            throw new IllegalArgumentException("Empty source region!");
        }
        this.clearAbortRequest();
        this.processImageStarted(0);
        if (this.abortRequested()) {
            this.processWriteAborted();
            return;
        }
        if (writeHeader) {
            this.streamMetadata = null;
            if (sm != null) {
                this.streamMetadata = (TIFFStreamMetadata)this.convertStreamMetadata(sm, this.param);
            }
            if (this.streamMetadata == null) {
                this.streamMetadata = (TIFFStreamMetadata)this.getDefaultStreamMetadata(this.param);
            }
            this.writeHeader();
            this.stream.seek(this.headerPosition + 4L);
            this.nextSpace = this.nextSpace + 3L & 0xFFFFFFFFFFFFFFFCL;
            this.stream.writeInt((int)this.nextSpace);
        }
        this.imageMetadata = null;
        IIOMetadata im = iioimage.getMetadata();
        if (im != null) {
            if (im instanceof TIFFImageMetadata) {
                this.imageMetadata = ((TIFFImageMetadata)im).getShallowClone();
            } else if (Arrays.asList(im.getMetadataFormatNames()).contains("javax_imageio_tiff_image_1.0")) {
                this.imageMetadata = this.convertNativeImageMetadata(im);
            } else if (im.isStandardMetadataFormatSupported()) {
                this.imageMetadata = this.convertStandardImageMetadata(im);
            }
            if (this.imageMetadata == null) {
                this.processWarningOccurred(this.currentImage, "Could not initialize image metadata");
            }
        }
        if (this.imageMetadata == null) {
            this.imageMetadata = (TIFFImageMetadata)this.getDefaultImageMetadata(this.imageType, this.param);
        }
        this.setupMetadata(colorModel, sampleModel, destWidth, destHeight);
        this.compressor.setWriter(this);
        this.compressor.setMetadata(this.imageMetadata);
        this.compressor.setStream(this.stream);
        this.sampleSize = sampleModel.getSampleSize();
        this.initializeScaleTables(sampleModel.getSampleSize());
        this.isBilevel = ImageUtil.isBinary(this.image.getSampleModel());
        this.isInverted = this.nativePhotometricInterpretation == 1 && this.photometricInterpretation == 0 || this.nativePhotometricInterpretation == 0 && this.photometricInterpretation == 1;
        this.isImageSimple = (this.isBilevel || !this.isInverted && ImageUtil.imageIsContiguous(this.image)) && !this.isRescaling && this.sourceBands == null && this.periodX == 1 && this.periodY == 1 && this.colorConverter == null;
        TIFFIFD rootIFD = this.imageMetadata.getRootIFD();
        rootIFD.writeToStream(this.stream);
        this.nextIFDPointerPos = this.stream.getStreamPosition();
        this.stream.writeInt(0);
        long lastIFDPosition = rootIFD.getLastPosition();
        this.stream.seek(lastIFDPosition);
        if (lastIFDPosition > this.nextSpace) {
            this.nextSpace = lastIFDPosition;
        }
        if (!writeData) {
            return;
        }
        long stripOrTileByteCountsPosition = rootIFD.getStripOrTileByteCountsPosition();
        long stripOrTileOffsetsPosition = rootIFD.getStripOrTileOffsetsPosition();
        this.totalPixels = this.tileWidth * this.tileLength * this.tilesDown * this.tilesAcross;
        this.pixelsDone = 0;
        for (int tj = 0; tj < this.tilesDown; ++tj) {
            for (int ti = 0; ti < this.tilesAcross; ++ti) {
                long pos = this.stream.getStreamPosition();
                Rectangle tileRect = new Rectangle(this.sourceXOffset + ti * this.tileWidth * this.periodX, this.sourceYOffset + tj * this.tileLength * this.periodY, this.tileWidth * this.periodX, this.tileLength * this.periodY);
                try {
                    int byteCount = this.writeTile(tileRect, this.compressor);
                    if (pos + (long)byteCount > this.nextSpace) {
                        this.nextSpace = pos + (long)byteCount;
                    }
                    this.stream.mark();
                    this.stream.seek(stripOrTileOffsetsPosition);
                    this.stream.writeInt((int)pos);
                    stripOrTileOffsetsPosition += 4L;
                    this.stream.seek(stripOrTileByteCountsPosition);
                    this.stream.writeInt(byteCount);
                    stripOrTileByteCountsPosition += 4L;
                    this.stream.reset();
                    this.pixelsDone += tileRect.width * tileRect.height;
                    this.processImageProgress(100.0f * (float)this.pixelsDone / (float)this.totalPixels);
                    if (!this.abortRequested()) continue;
                    this.processWriteAborted();
                    return;
                }
                catch (IOException e) {
                    throw new IIOException("I/O error writing TIFF file!", e);
                }
            }
        }
        this.processImageComplete();
        ++this.currentImage;
    }

    @Override
    public boolean canWriteSequence() {
        return true;
    }

    @Override
    public void prepareWriteSequence(IIOMetadata streamMetadata) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        if (streamMetadata != null) {
            streamMetadata = this.convertStreamMetadata(streamMetadata, null);
        }
        if (streamMetadata == null) {
            streamMetadata = this.getDefaultStreamMetadata(null);
        }
        this.streamMetadata = (TIFFStreamMetadata)streamMetadata;
        this.writeHeader();
        this.isWritingSequence = true;
    }

    @Override
    public void writeToSequence(IIOImage image, ImageWriteParam param) throws IOException {
        if (!this.isWritingSequence) {
            throw new IllegalStateException("prepareWriteSequence() has not been called!");
        }
        this.writeInsert(-1, image, param);
    }

    @Override
    public void endWriteSequence() throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        if (!this.isWritingSequence) {
            throw new IllegalStateException("prepareWriteSequence() has not been called!");
        }
        this.isWritingSequence = false;
        long streamLength = this.stream.length();
        if (streamLength != -1L) {
            this.stream.seek(streamLength);
        }
    }

    @Override
    public boolean canInsertImage(int imageIndex) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        this.stream.mark();
        long[] ifdpos = new long[1];
        long[] ifd = new long[1];
        this.locateIFD(imageIndex, ifdpos, ifd);
        this.stream.reset();
        return true;
    }

    private void locateIFD(int imageIndex, long[] ifdpos, long[] ifd) throws IOException {
        if (imageIndex < -1) {
            throw new IndexOutOfBoundsException("imageIndex < -1!");
        }
        long startPos = this.stream.getStreamPosition();
        this.stream.seek(this.headerPosition);
        int byteOrder = this.stream.readUnsignedShort();
        if (byteOrder == 19789) {
            this.stream.setByteOrder(ByteOrder.BIG_ENDIAN);
        } else if (byteOrder == 18761) {
            this.stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else {
            this.stream.seek(startPos);
            throw new IIOException("Illegal byte order");
        }
        if (this.stream.readUnsignedShort() != 42) {
            this.stream.seek(startPos);
            throw new IIOException("Illegal magic number");
        }
        ifdpos[0] = this.stream.getStreamPosition();
        ifd[0] = this.stream.readUnsignedInt();
        if (ifd[0] == 0L) {
            if (imageIndex > 0) {
                this.stream.seek(startPos);
                throw new IndexOutOfBoundsException("imageIndex is greater than the largest available index!");
            }
            return;
        }
        this.stream.seek(ifd[0]);
        for (int i = 0; imageIndex == -1 || i < imageIndex; ++i) {
            short numFields;
            try {
                numFields = this.stream.readShort();
            }
            catch (EOFException eof) {
                this.stream.seek(startPos);
                ifd[0] = 0L;
                return;
            }
            this.stream.skipBytes(12 * numFields);
            ifdpos[0] = this.stream.getStreamPosition();
            ifd[0] = this.stream.readUnsignedInt();
            if (ifd[0] == 0L) {
                if (imageIndex == -1 || i >= imageIndex - 1) break;
                this.stream.seek(startPos);
                throw new IndexOutOfBoundsException("imageIndex is greater than the largest available index!");
            }
            this.stream.seek(ifd[0]);
        }
    }

    @Override
    public void writeInsert(int imageIndex, IIOImage image, ImageWriteParam param) throws IOException {
        int currentImageCached = this.currentImage;
        try {
            this.insert(imageIndex, image, param, true);
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            this.currentImage = currentImageCached;
        }
    }

    private void insert(int imageIndex, IIOImage image, ImageWriteParam param, boolean writeData) throws IOException {
        if (this.stream == null) {
            throw new IllegalStateException("Output not set!");
        }
        if (image == null) {
            throw new IllegalArgumentException("image == null!");
        }
        long[] ifdpos = new long[1];
        long[] ifd = new long[1];
        this.locateIFD(imageIndex, ifdpos, ifd);
        this.markPositions();
        this.stream.seek(ifdpos[0]);
        this.stream.mark();
        long prevPointerValue = this.stream.readUnsignedInt();
        this.stream.reset();
        if (ifdpos[0] + 4L > this.nextSpace) {
            this.nextSpace = ifdpos[0] + 4L;
        }
        this.nextSpace = this.nextSpace + 3L & 0xFFFFFFFFFFFFFFFCL;
        this.stream.writeInt((int)this.nextSpace);
        this.stream.seek(this.nextSpace);
        this.write(null, image, param, false, writeData);
        this.stream.seek(this.nextIFDPointerPos);
        this.stream.writeInt((int)ifd[0]);
        if (this.abortRequested()) {
            this.stream.seek(ifdpos[0]);
            this.stream.writeInt((int)prevPointerValue);
            this.resetPositions();
        }
    }

    private boolean isEncodingEmpty() {
        return this.isInsertingEmpty || this.isWritingEmpty;
    }

    @Override
    public boolean canInsertEmpty(int imageIndex) throws IOException {
        return this.canInsertImage(imageIndex);
    }

    @Override
    public boolean canWriteEmpty() throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return true;
    }

    private void checkParamsEmpty(ImageTypeSpecifier imageType, int width, int height, List<? extends BufferedImage> thumbnails) {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        if (imageType == null) {
            throw new IllegalArgumentException("imageType == null!");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width < 1 || height < 1!");
        }
        if (thumbnails != null) {
            int numThumbs = thumbnails.size();
            for (int i = 0; i < numThumbs; ++i) {
                BufferedImage thumb = thumbnails.get(i);
                if (thumb instanceof BufferedImage) continue;
                throw new IllegalArgumentException("thumbnails contains null references or objects other than BufferedImages!");
            }
        }
        if (this.isInsertingEmpty) {
            throw new IllegalStateException("Previous call to prepareInsertEmpty() without corresponding call to endInsertEmpty()!");
        }
        if (this.isWritingEmpty) {
            throw new IllegalStateException("Previous call to prepareWriteEmpty() without corresponding call to endWriteEmpty()!");
        }
    }

    @Override
    public void prepareInsertEmpty(int imageIndex, ImageTypeSpecifier imageType, int width, int height, IIOMetadata imageMetadata, List<? extends BufferedImage> thumbnails, ImageWriteParam param) throws IOException {
        this.checkParamsEmpty(imageType, width, height, thumbnails);
        this.isInsertingEmpty = true;
        SampleModel emptySM = imageType.getSampleModel();
        EmptyImage emptyImage = new EmptyImage(0, 0, width, height, 0, 0, emptySM.getWidth(), emptySM.getHeight(), emptySM, imageType.getColorModel());
        this.insert(imageIndex, new IIOImage(emptyImage, null, imageMetadata), param, false);
    }

    @Override
    public void prepareWriteEmpty(IIOMetadata streamMetadata, ImageTypeSpecifier imageType, int width, int height, IIOMetadata imageMetadata, List<? extends BufferedImage> thumbnails, ImageWriteParam param) throws IOException {
        if (this.stream == null) {
            throw new IllegalStateException("output == null!");
        }
        this.checkParamsEmpty(imageType, width, height, thumbnails);
        this.isWritingEmpty = true;
        SampleModel emptySM = imageType.getSampleModel();
        EmptyImage emptyImage = new EmptyImage(0, 0, width, height, 0, 0, emptySM.getWidth(), emptySM.getHeight(), emptySM, imageType.getColorModel());
        this.markPositions();
        this.write(streamMetadata, new IIOImage(emptyImage, null, imageMetadata), param, true, false);
        if (this.abortRequested()) {
            this.resetPositions();
        }
    }

    @Override
    public void endInsertEmpty() throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        if (!this.isInsertingEmpty) {
            throw new IllegalStateException("No previous call to prepareInsertEmpty()!");
        }
        if (this.isWritingEmpty) {
            throw new IllegalStateException("Previous call to prepareWriteEmpty() without corresponding call to endWriteEmpty()!");
        }
        if (this.inReplacePixelsNest) {
            throw new IllegalStateException("In nested call to prepareReplacePixels!");
        }
        this.isInsertingEmpty = false;
    }

    @Override
    public void endWriteEmpty() throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        if (!this.isWritingEmpty) {
            throw new IllegalStateException("No previous call to prepareWriteEmpty()!");
        }
        if (this.isInsertingEmpty) {
            throw new IllegalStateException("Previous call to prepareInsertEmpty() without corresponding call to endInsertEmpty()!");
        }
        if (this.inReplacePixelsNest) {
            throw new IllegalStateException("In nested call to prepareReplacePixels!");
        }
        this.isWritingEmpty = false;
    }

    private TIFFIFD readIFD(int imageIndex) throws IOException {
        if (this.stream == null) {
            throw new IllegalStateException("Output not set!");
        }
        if (imageIndex < 0) {
            throw new IndexOutOfBoundsException("imageIndex < 0!");
        }
        this.stream.mark();
        long[] ifdpos = new long[1];
        long[] ifd = new long[1];
        this.locateIFD(imageIndex, ifdpos, ifd);
        if (ifd[0] == 0L) {
            this.stream.reset();
            throw new IndexOutOfBoundsException("imageIndex out of bounds!");
        }
        ArrayList<TIFFTagSet> tagSets = new ArrayList<TIFFTagSet>(1);
        tagSets.add(BaselineTIFFTagSet.getInstance());
        TIFFIFD rootIFD = new TIFFIFD(tagSets);
        rootIFD.initialize(this.stream, true, false, false);
        this.stream.reset();
        return rootIFD;
    }

    @Override
    public boolean canReplacePixels(int imageIndex) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        TIFFIFD rootIFD = this.readIFD(imageIndex);
        TIFFField f = rootIFD.getTIFFField(259);
        int compression = f.getAsInt(0);
        return compression == 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void prepareReplacePixels(int imageIndex, Rectangle region) throws IOException {
        Object object = this.replacePixelsLock;
        synchronized (object) {
            if (this.stream == null) {
                throw new IllegalStateException("Output not set!");
            }
            if (region == null) {
                throw new IllegalArgumentException("region == null!");
            }
            if (region.getWidth() < 1.0) {
                throw new IllegalArgumentException("region.getWidth() < 1!");
            }
            if (region.getHeight() < 1.0) {
                throw new IllegalArgumentException("region.getHeight() < 1!");
            }
            if (this.inReplacePixelsNest) {
                throw new IllegalStateException("In nested call to prepareReplacePixels!");
            }
            TIFFIFD replacePixelsIFD = this.readIFD(imageIndex);
            TIFFField f = replacePixelsIFD.getTIFFField(259);
            int compression = f.getAsInt(0);
            if (compression != 1) {
                throw new UnsupportedOperationException("canReplacePixels(imageIndex) == false!");
            }
            f = replacePixelsIFD.getTIFFField(256);
            if (f == null) {
                throw new IIOException("Cannot read ImageWidth field.");
            }
            int w = f.getAsInt(0);
            f = replacePixelsIFD.getTIFFField(257);
            if (f == null) {
                throw new IIOException("Cannot read ImageHeight field.");
            }
            int h = f.getAsInt(0);
            Rectangle bounds = new Rectangle(0, 0, w, h);
            if ((region = region.intersection(bounds)).isEmpty()) {
                throw new IIOException("Region does not intersect image bounds");
            }
            this.replacePixelsRegion = region;
            f = replacePixelsIFD.getTIFFField(324);
            if (f == null) {
                f = replacePixelsIFD.getTIFFField(273);
            }
            this.replacePixelsTileOffsets = f.getAsLongs();
            f = replacePixelsIFD.getTIFFField(325);
            if (f == null) {
                f = replacePixelsIFD.getTIFFField(279);
            }
            this.replacePixelsByteCounts = f.getAsLongs();
            this.replacePixelsOffsetsPosition = replacePixelsIFD.getStripOrTileOffsetsPosition();
            this.replacePixelsByteCountsPosition = replacePixelsIFD.getStripOrTileByteCountsPosition();
            this.replacePixelsMetadata = new TIFFImageMetadata(replacePixelsIFD);
            this.replacePixelsIndex = imageIndex;
            this.inReplacePixelsNest = true;
        }
    }

    private Raster subsample(Raster raster, int[] sourceBands, int subOriginX, int subOriginY, int subPeriodX, int subPeriodY, int dstOffsetX, int dstOffsetY, Rectangle target) {
        int x = raster.getMinX();
        int y = raster.getMinY();
        int w = raster.getWidth();
        int h = raster.getHeight();
        int b = raster.getSampleModel().getNumBands();
        int t = raster.getSampleModel().getDataType();
        int outMinX = TIFFImageWriter.XToTileX(x, subOriginX, subPeriodX) + dstOffsetX;
        int outMinY = TIFFImageWriter.YToTileY(y, subOriginY, subPeriodY) + dstOffsetY;
        int outMaxX = TIFFImageWriter.XToTileX(x + w - 1, subOriginX, subPeriodX) + dstOffsetX;
        int outMaxY = TIFFImageWriter.YToTileY(y + h - 1, subOriginY, subPeriodY) + dstOffsetY;
        int outWidth = outMaxX - outMinX + 1;
        int outHeight = outMaxY - outMinY + 1;
        if (outWidth <= 0 || outHeight <= 0) {
            return null;
        }
        int inMinX = (outMinX - dstOffsetX) * subPeriodX + subOriginX;
        int inMaxX = (outMaxX - dstOffsetX) * subPeriodX + subOriginX;
        int inWidth = inMaxX - inMinX + 1;
        int inMinY = (outMinY - dstOffsetY) * subPeriodY + subOriginY;
        int inMaxY = (outMaxY - dstOffsetY) * subPeriodY + subOriginY;
        int inHeight = inMaxY - inMinY + 1;
        WritableRaster wr = raster.createCompatibleWritableRaster(outMinX, outMinY, outWidth, outHeight);
        int jMax = inMinY + inHeight;
        if (t == 4) {
            float[] fsamples = new float[inWidth];
            float[] fsubsamples = new float[outWidth];
            for (int k = 0; k < b; ++k) {
                int outY = outMinY;
                for (int j = inMinY; j < jMax; j += subPeriodY) {
                    raster.getSamples(inMinX, j, inWidth, 1, k, fsamples);
                    int s = 0;
                    for (int i = 0; i < inWidth; i += subPeriodX) {
                        fsubsamples[s++] = fsamples[i];
                    }
                    wr.setSamples(outMinX, outY++, outWidth, 1, k, fsubsamples);
                }
            }
        } else if (t == 5) {
            double[] dsamples = new double[inWidth];
            double[] dsubsamples = new double[outWidth];
            for (int k = 0; k < b; ++k) {
                int outY = outMinY;
                for (int j = inMinY; j < jMax; j += subPeriodY) {
                    raster.getSamples(inMinX, j, inWidth, 1, k, dsamples);
                    int s = 0;
                    for (int i = 0; i < inWidth; i += subPeriodX) {
                        dsubsamples[s++] = dsamples[i];
                    }
                    wr.setSamples(outMinX, outY++, outWidth, 1, k, dsubsamples);
                }
            }
        } else {
            int[] samples = new int[inWidth];
            int[] subsamples = new int[outWidth];
            for (int k = 0; k < b; ++k) {
                int outY = outMinY;
                for (int j = inMinY; j < jMax; j += subPeriodY) {
                    raster.getSamples(inMinX, j, inWidth, 1, k, samples);
                    int s = 0;
                    for (int i = 0; i < inWidth; i += subPeriodX) {
                        subsamples[s++] = samples[i];
                    }
                    wr.setSamples(outMinX, outY++, outWidth, 1, k, subsamples);
                }
            }
        }
        return wr.createChild(outMinX, outMinY, target.width, target.height, target.x, target.y, sourceBands);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void replacePixels(RenderedImage image, ImageWriteParam param) throws IOException {
        Object object = this.replacePixelsLock;
        synchronized (object) {
            if (this.stream == null) {
                throw new IllegalStateException("stream == null!");
            }
            if (image == null) {
                throw new IllegalArgumentException("image == null!");
            }
            if (!this.inReplacePixelsNest) {
                throw new IllegalStateException("No previous call to prepareReplacePixels!");
            }
            int stepX = 1;
            int stepY = 1;
            int gridX = 0;
            int gridY = 0;
            if (param == null) {
                param = this.getDefaultWriteParam();
            } else {
                ImageWriteParam paramCopy = this.getDefaultWriteParam();
                paramCopy.setCompressionMode(0);
                paramCopy.setTilingMode(3);
                paramCopy.setDestinationOffset(param.getDestinationOffset());
                paramCopy.setSourceBands(param.getSourceBands());
                paramCopy.setSourceRegion(param.getSourceRegion());
                stepX = param.getSourceXSubsampling();
                stepY = param.getSourceYSubsampling();
                gridX = param.getSubsamplingXOffset();
                gridY = param.getSubsamplingYOffset();
                param = paramCopy;
            }
            TIFFField f = this.replacePixelsMetadata.getTIFFField(258);
            if (f == null) {
                throw new IIOException("Cannot read destination BitsPerSample");
            }
            int[] dstBitsPerSample = f.getAsInts();
            int[] srcBitsPerSample = image.getSampleModel().getSampleSize();
            int[] sourceBands = param.getSourceBands();
            if (sourceBands != null) {
                if (sourceBands.length != dstBitsPerSample.length) {
                    throw new IIOException("Source and destination have different SamplesPerPixel");
                }
                for (int i = 0; i < sourceBands.length; ++i) {
                    if (dstBitsPerSample[i] == srcBitsPerSample[sourceBands[i]]) continue;
                    throw new IIOException("Source and destination have different BitsPerSample");
                }
            } else {
                int srcNumBands = image.getSampleModel().getNumBands();
                if (srcNumBands != dstBitsPerSample.length) {
                    throw new IIOException("Source and destination have different SamplesPerPixel");
                }
                for (int i = 0; i < srcNumBands; ++i) {
                    if (dstBitsPerSample[i] == srcBitsPerSample[i]) continue;
                    throw new IIOException("Source and destination have different BitsPerSample");
                }
            }
            Rectangle srcImageBounds = new Rectangle(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight());
            Rectangle srcRect = param.getSourceRegion();
            if (srcRect == null) {
                srcRect = srcImageBounds;
            }
            int subPeriodX = stepX;
            int subPeriodY = stepY;
            int subOriginX = gridX + srcRect.x;
            int subOriginY = gridY + srcRect.y;
            if (!srcRect.equals(srcImageBounds) && (srcRect = srcRect.intersection(srcImageBounds)).isEmpty()) {
                throw new IllegalArgumentException("Source region does not intersect source image!");
            }
            Point dstOffset = param.getDestinationOffset();
            int dMinX = TIFFImageWriter.XToTileX(srcRect.x, subOriginX, subPeriodX) + dstOffset.x;
            int dMinY = TIFFImageWriter.YToTileY(srcRect.y, subOriginY, subPeriodY) + dstOffset.y;
            int dMaxX = TIFFImageWriter.XToTileX(srcRect.x + srcRect.width, subOriginX, subPeriodX) + dstOffset.x;
            int dMaxY = TIFFImageWriter.YToTileY(srcRect.y + srcRect.height, subOriginY, subPeriodY) + dstOffset.y;
            Rectangle dstRect = new Rectangle(dstOffset.x, dstOffset.y, dMaxX - dMinX, dMaxY - dMinY);
            if ((dstRect = dstRect.intersection(this.replacePixelsRegion)).isEmpty()) {
                throw new IllegalArgumentException("Forward mapped source region does not intersect destination region!");
            }
            int activeSrcMinX = (dstRect.x - dstOffset.x) * subPeriodX + subOriginX;
            int activeSrcMinY = (dstRect.y - dstOffset.y) * subPeriodY + subOriginY;
            int sxmax = (dstRect.x + dstRect.width - 1 - dstOffset.x) * subPeriodX + subOriginX;
            int activeSrcWidth = sxmax - activeSrcMinX + 1;
            int symax = (dstRect.y + dstRect.height - 1 - dstOffset.y) * subPeriodY + subOriginY;
            int activeSrcHeight = symax - activeSrcMinY + 1;
            Rectangle activeSrcRect = new Rectangle(activeSrcMinX, activeSrcMinY, activeSrcWidth, activeSrcHeight);
            if (activeSrcRect.intersection(srcImageBounds).isEmpty()) {
                throw new IllegalArgumentException("Backward mapped destination region does not intersect source image!");
            }
            if (this.reader == null) {
                this.reader = new TIFFImageReader(new TIFFImageReaderSpi());
            } else {
                this.reader.reset();
            }
            this.stream.mark();
            try {
                this.stream.seek(this.headerPosition);
                this.reader.setInput(this.stream);
                this.imageMetadata = this.replacePixelsMetadata;
                this.param = param;
                SampleModel sm = image.getSampleModel();
                ColorModel cm = image.getColorModel();
                this.numBands = sm.getNumBands();
                this.imageType = new ImageTypeSpecifier(image);
                this.periodX = param.getSourceXSubsampling();
                this.periodY = param.getSourceYSubsampling();
                this.sourceBands = null;
                int[] sBands = param.getSourceBands();
                if (sBands != null) {
                    this.sourceBands = sBands;
                    this.numBands = sourceBands.length;
                }
                this.setupMetadata(cm, sm, this.reader.getWidth(this.replacePixelsIndex), this.reader.getHeight(this.replacePixelsIndex));
                int[] scaleSampleSize = sm.getSampleSize();
                this.initializeScaleTables(scaleSampleSize);
                this.isBilevel = ImageUtil.isBinary(image.getSampleModel());
                this.isInverted = this.nativePhotometricInterpretation == 1 && this.photometricInterpretation == 0 || this.nativePhotometricInterpretation == 0 && this.photometricInterpretation == 1;
                this.isImageSimple = (this.isBilevel || !this.isInverted && ImageUtil.imageIsContiguous(image)) && !this.isRescaling && sourceBands == null && this.periodX == 1 && this.periodY == 1 && this.colorConverter == null;
                int minTileX = TIFFImageWriter.XToTileX(dstRect.x, 0, this.tileWidth);
                int minTileY = TIFFImageWriter.YToTileY(dstRect.y, 0, this.tileLength);
                int maxTileX = TIFFImageWriter.XToTileX(dstRect.x + dstRect.width - 1, 0, this.tileWidth);
                int maxTileY = TIFFImageWriter.YToTileY(dstRect.y + dstRect.height - 1, 0, this.tileLength);
                TIFFNullCompressor encoder = new TIFFNullCompressor();
                encoder.setWriter(this);
                encoder.setStream(this.stream);
                encoder.setMetadata(this.imageMetadata);
                Rectangle tileRect = new Rectangle();
                for (int ty = minTileY; ty <= maxTileY; ++ty) {
                    for (int tx = minTileX; tx <= maxTileX; ++tx) {
                        WritableRaster raster;
                        boolean isEmpty;
                        int tileIndex = ty * this.tilesAcross + tx;
                        boolean bl = isEmpty = this.replacePixelsByteCounts[tileIndex] == 0L;
                        if (isEmpty) {
                            SampleModel tileSM = sm.createCompatibleSampleModel(this.tileWidth, this.tileLength);
                            raster = Raster.createWritableRaster(tileSM, null);
                        } else {
                            BufferedImage tileImage = this.reader.readTile(this.replacePixelsIndex, tx, ty);
                            raster = tileImage.getRaster();
                        }
                        tileRect.setLocation(tx * this.tileWidth, ty * this.tileLength);
                        tileRect.setSize(raster.getWidth(), raster.getHeight());
                        raster = raster.createWritableTranslatedChild(tileRect.x, tileRect.y);
                        Rectangle replacementRect = tileRect.intersection(dstRect);
                        int srcMinX = (replacementRect.x - dstOffset.x) * subPeriodX + subOriginX;
                        int srcXmax = (replacementRect.x + replacementRect.width - 1 - dstOffset.x) * subPeriodX + subOriginX;
                        int srcWidth = srcXmax - srcMinX + 1;
                        int srcMinY = (replacementRect.y - dstOffset.y) * subPeriodY + subOriginY;
                        int srcYMax = (replacementRect.y + replacementRect.height - 1 - dstOffset.y) * subPeriodY + subOriginY;
                        int srcHeight = srcYMax - srcMinY + 1;
                        Rectangle srcTileRect = new Rectangle(srcMinX, srcMinY, srcWidth, srcHeight);
                        Raster replacementData = image.getData(srcTileRect);
                        if (subPeriodX == 1 && subPeriodY == 1 && subOriginX == 0 && subOriginY == 0) {
                            replacementData = replacementData.createChild(srcTileRect.x, srcTileRect.y, srcTileRect.width, srcTileRect.height, replacementRect.x, replacementRect.y, sourceBands);
                        } else if ((replacementData = this.subsample(replacementData, sourceBands, subOriginX, subOriginY, subPeriodX, subPeriodY, dstOffset.x, dstOffset.y, replacementRect)) == null) continue;
                        raster.setRect(replacementData);
                        if (isEmpty) {
                            this.stream.seek(this.nextSpace);
                        } else {
                            this.stream.seek(this.replacePixelsTileOffsets[tileIndex]);
                        }
                        this.image = new SingleTileRenderedImage(raster, cm);
                        int numBytes = this.writeTile(tileRect, encoder);
                        if (!isEmpty) continue;
                        this.stream.mark();
                        this.stream.seek(this.replacePixelsOffsetsPosition + (long)(4 * tileIndex));
                        this.stream.writeInt((int)this.nextSpace);
                        this.stream.seek(this.replacePixelsByteCountsPosition + (long)(4 * tileIndex));
                        this.stream.writeInt(numBytes);
                        this.stream.reset();
                        this.nextSpace += (long)numBytes;
                    }
                }
            }
            catch (IOException e) {
                throw e;
            }
            finally {
                this.stream.reset();
            }
        }
    }

    @Override
    public void replacePixels(Raster raster, ImageWriteParam param) throws IOException {
        if (raster == null) {
            throw new NullPointerException("raster == null!");
        }
        this.replacePixels(new SingleTileRenderedImage(raster, this.image.getColorModel()), param);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void endReplacePixels() throws IOException {
        Object object = this.replacePixelsLock;
        synchronized (object) {
            if (!this.inReplacePixelsNest) {
                throw new IllegalStateException("No previous call to prepareReplacePixels()!");
            }
            this.replacePixelsIndex = -1;
            this.replacePixelsMetadata = null;
            this.replacePixelsTileOffsets = null;
            this.replacePixelsByteCounts = null;
            this.replacePixelsOffsetsPosition = 0L;
            this.replacePixelsByteCountsPosition = 0L;
            this.replacePixelsRegion = null;
            this.inReplacePixelsNest = false;
        }
    }

    private void markPositions() throws IOException {
        this.prevStreamPosition = this.stream.getStreamPosition();
        this.prevHeaderPosition = this.headerPosition;
        this.prevNextSpace = this.nextSpace;
    }

    private void resetPositions() throws IOException {
        this.stream.seek(this.prevStreamPosition);
        this.headerPosition = this.prevHeaderPosition;
        this.nextSpace = this.prevNextSpace;
    }

    @Override
    public void reset() {
        super.reset();
        this.stream = null;
        this.image = null;
        this.imageType = null;
        this.byteOrder = null;
        this.param = null;
        this.compressor = null;
        this.colorConverter = null;
        this.streamMetadata = null;
        this.imageMetadata = null;
        this.isRescaling = false;
        this.isWritingSequence = false;
        this.isWritingEmpty = false;
        this.isInsertingEmpty = false;
        this.replacePixelsIndex = -1;
        this.replacePixelsMetadata = null;
        this.replacePixelsTileOffsets = null;
        this.replacePixelsByteCounts = null;
        this.replacePixelsOffsetsPosition = 0L;
        this.replacePixelsByteCountsPosition = 0L;
        this.replacePixelsRegion = null;
        this.inReplacePixelsNest = false;
    }
}

