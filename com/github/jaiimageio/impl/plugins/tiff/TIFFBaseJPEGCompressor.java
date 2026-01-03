/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.impl.plugins.tiff.TIFFBaseJPEGCompressor$IIOByteArrayOutputStream
 */
package com.github.jaiimageio.impl.plugins.tiff;

import com.github.jaiimageio.impl.plugins.tiff.TIFFBaseJPEGCompressor;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.plugins.tiff.TIFFCompressor;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.w3c.dom.Node;

public abstract class TIFFBaseJPEGCompressor
extends TIFFCompressor {
    private static final boolean DEBUG = false;
    protected static final String STREAM_METADATA_NAME = "javax_imageio_jpeg_stream_1.0";
    protected static final String IMAGE_METADATA_NAME = "javax_imageio_jpeg_image_1.0";
    private ImageWriteParam param = null;
    protected JPEGImageWriteParam JPEGParam = null;
    protected ImageWriter JPEGWriter = null;
    protected boolean writeAbbreviatedStream = false;
    protected IIOMetadata JPEGStreamMetadata = null;
    private IIOMetadata JPEGImageMetadata = null;
    private boolean usingCodecLib;
    private IIOByteArrayOutputStream baos;

    private static void pruneNodes(Node tree, boolean pruneTables) {
        if (tree == null) {
            throw new IllegalArgumentException("tree == null!");
        }
        if (!tree.getNodeName().equals(IMAGE_METADATA_NAME)) {
            throw new IllegalArgumentException("root node name is not javax_imageio_jpeg_image_1.0!");
        }
        ArrayList<String> wantedNodes = new ArrayList<String>();
        wantedNodes.addAll(Arrays.asList("JPEGvariety", "markerSequence", "sof", "componentSpec", "sos", "scanComponentSpec"));
        if (!pruneTables) {
            wantedNodes.add("dht");
            wantedNodes.add("dhtable");
            wantedNodes.add("dqt");
            wantedNodes.add("dqtable");
        }
        IIOMetadataNode iioTree = (IIOMetadataNode)tree;
        List nodes = TIFFBaseJPEGCompressor.getAllNodes(iioTree, null);
        int numNodes = nodes.size();
        for (int i = 0; i < numNodes; ++i) {
            Node node = (Node)nodes.get(i);
            if (wantedNodes.contains(node.getNodeName())) continue;
            node.getParentNode().removeChild(node);
        }
    }

    private static List getAllNodes(IIOMetadataNode root, List nodes) {
        if (nodes == null) {
            nodes = new ArrayList<Node>();
        }
        if (root.hasChildNodes()) {
            for (Node sibling = root.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
                nodes.add(sibling);
                nodes = TIFFBaseJPEGCompressor.getAllNodes((IIOMetadataNode)sibling, nodes);
            }
        }
        return nodes;
    }

    public TIFFBaseJPEGCompressor(String compressionType, int compressionTagValue, boolean isCompressionLossless, ImageWriteParam param) {
        super(compressionType, compressionTagValue, isCompressionLossless);
        this.param = param;
    }

    protected void initJPEGWriter(boolean supportsStreamMetadata, boolean supportsImageMetadata) {
        if (this.JPEGWriter != null && (supportsStreamMetadata || supportsImageMetadata)) {
            String imName;
            String smName;
            ImageWriterSpi spi = this.JPEGWriter.getOriginatingProvider();
            if (supportsStreamMetadata && ((smName = spi.getNativeStreamMetadataFormatName()) == null || !smName.equals(STREAM_METADATA_NAME))) {
                this.JPEGWriter = null;
            }
            if (this.JPEGWriter != null && supportsImageMetadata && ((imName = spi.getNativeImageMetadataFormatName()) == null || !imName.equals(IMAGE_METADATA_NAME))) {
                this.JPEGWriter = null;
            }
        }
        if (this.JPEGWriter == null) {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            while (iter.hasNext()) {
                ImageWriter writer = iter.next();
                if (supportsStreamMetadata || supportsImageMetadata) {
                    String imName;
                    String smName;
                    ImageWriterSpi spi = writer.getOriginatingProvider();
                    if (supportsStreamMetadata && ((smName = spi.getNativeStreamMetadataFormatName()) == null || !smName.equals(STREAM_METADATA_NAME)) || supportsImageMetadata && ((imName = spi.getNativeImageMetadataFormatName()) == null || !imName.equals(IMAGE_METADATA_NAME))) continue;
                }
                this.JPEGWriter = writer;
                break;
            }
            if (this.JPEGWriter == null) {
                throw new IllegalStateException("No appropriate JPEG writers found!");
            }
        }
        this.usingCodecLib = this.JPEGWriter.getClass().getName().startsWith("com.sun.media");
        if (this.JPEGParam == null) {
            if (this.param != null && this.param instanceof JPEGImageWriteParam) {
                this.JPEGParam = (JPEGImageWriteParam)this.param;
            } else {
                this.JPEGParam = new JPEGImageWriteParam(this.writer != null ? this.writer.getLocale() : null);
                if (this.param.getCompressionMode() == 2) {
                    this.JPEGParam.setCompressionMode(2);
                    this.JPEGParam.setCompressionQuality(this.param.getCompressionQuality());
                }
            }
        }
    }

    private IIOMetadata getImageMetadata(boolean pruneTables) throws IIOException {
        if (this.JPEGImageMetadata == null && IMAGE_METADATA_NAME.equals(this.JPEGWriter.getOriginatingProvider().getNativeImageMetadataFormatName())) {
            TIFFImageWriter tiffWriter = (TIFFImageWriter)this.writer;
            this.JPEGImageMetadata = this.JPEGWriter.getDefaultImageMetadata(tiffWriter.imageType, this.JPEGParam);
            Node tree = this.JPEGImageMetadata.getAsTree(IMAGE_METADATA_NAME);
            try {
                TIFFBaseJPEGCompressor.pruneNodes(tree, pruneTables);
            }
            catch (IllegalArgumentException e) {
                throw new IIOException("Error pruning unwanted nodes", e);
            }
            try {
                this.JPEGImageMetadata.setFromTree(IMAGE_METADATA_NAME, tree);
            }
            catch (IIOInvalidTreeException e) {
                throw new IIOException("Cannot set pruned image metadata!", e);
            }
        }
        return this.JPEGImageMetadata;
    }

    @Override
    public final int encode(byte[] b, int off, int width, int height, int[] bitsPerSample, int scanlineStride) throws IOException {
        int compDataLength;
        ColorSpace cs;
        int[] offsets;
        DataBufferByte dbb;
        long initialStreamPosition;
        ImageOutputStream ios;
        if (this.JPEGWriter == null) {
            throw new IIOException("JPEG writer has not been initialized!");
        }
        if (!(bitsPerSample.length == 3 && bitsPerSample[0] == 8 && bitsPerSample[1] == 8 && bitsPerSample[2] == 8 || bitsPerSample.length == 1 && bitsPerSample[0] == 8)) {
            throw new IIOException("Can only JPEG compress 8- and 24-bit images!");
        }
        if (this.usingCodecLib && !this.writeAbbreviatedStream) {
            ios = this.stream;
            initialStreamPosition = this.stream.getStreamPosition();
        } else {
            if (this.baos == null) {
                this.baos = new IIOByteArrayOutputStream();
            } else {
                this.baos.reset();
            }
            ios = new MemoryCacheImageOutputStream((OutputStream)this.baos);
            initialStreamPosition = 0L;
        }
        this.JPEGWriter.setOutput(ios);
        if (off == 0 || this.usingCodecLib) {
            dbb = new DataBufferByte(b, b.length);
        } else {
            int bytesPerSegment = scanlineStride * height;
            byte[] btmp = new byte[bytesPerSegment];
            System.arraycopy(b, off, btmp, 0, bytesPerSegment);
            dbb = new DataBufferByte(btmp, bytesPerSegment);
            off = 0;
        }
        if (bitsPerSample.length == 3) {
            offsets = new int[]{off, off + 1, off + 2};
            cs = ColorSpace.getInstance(1000);
        } else {
            offsets = new int[]{off};
            cs = ColorSpace.getInstance(1003);
        }
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, 1, 0);
        PixelInterleavedSampleModel sm = new PixelInterleavedSampleModel(0, width, height, bitsPerSample.length, scanlineStride, offsets);
        WritableRaster wras = Raster.createWritableRaster(sm, dbb, new Point(0, 0));
        BufferedImage bi = new BufferedImage(cm, wras, false, null);
        IIOMetadata imageMetadata = this.getImageMetadata(this.writeAbbreviatedStream);
        if (this.usingCodecLib && !this.writeAbbreviatedStream) {
            this.JPEGWriter.write(null, new IIOImage(bi, null, imageMetadata), this.JPEGParam);
            compDataLength = (int)(this.stream.getStreamPosition() - initialStreamPosition);
        } else {
            if (this.writeAbbreviatedStream) {
                this.JPEGWriter.prepareWriteSequence(this.JPEGStreamMetadata);
                ios.flush();
                this.baos.reset();
                IIOImage image = new IIOImage(bi, null, imageMetadata);
                this.JPEGWriter.writeToSequence(image, this.JPEGParam);
                this.JPEGWriter.endWriteSequence();
            } else {
                this.JPEGWriter.write(null, new IIOImage(bi, null, imageMetadata), this.JPEGParam);
            }
            compDataLength = this.baos.size();
            this.baos.writeTo(this.stream);
            this.baos.reset();
        }
        return compDataLength;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (this.JPEGWriter != null) {
            this.JPEGWriter.dispose();
        }
    }
}

