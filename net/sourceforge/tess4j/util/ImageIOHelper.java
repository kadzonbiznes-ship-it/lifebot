/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.recognition.software.jdeskew.ImageDeskew
 *  com.recognition.software.jdeskew.ImageUtil
 *  net.sourceforge.tess4j.util.ImageHelper
 *  org.apache.commons.io.FilenameUtils
 */
package net.sourceforge.tess4j.util;

import com.github.jaiimageio.plugins.tiff.BaselineTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.TIFFDirectory;
import com.github.jaiimageio.plugins.tiff.TIFFField;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import com.github.jaiimageio.plugins.tiff.TIFFTag;
import com.recognition.software.jdeskew.ImageDeskew;
import com.recognition.software.jdeskew.ImageUtil;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import net.sourceforge.tess4j.util.ImageHelper;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.NodeList;

public class ImageIOHelper {
    static final String OUTPUT_FILE_NAME = "Tesstmp";
    public static final String TIFF_EXT = ".tif";
    static final String TIFF_FORMAT = "tiff";
    public static final String JAI_IMAGE_WRITER_MESSAGE = "Need to install JAI Image I/O package.\nhttps://github.com/jai-imageio/jai-imageio-core";
    public static final String JAI_IMAGE_READER_MESSAGE = "Unsupported image format. May need to install JAI Image I/O package.\nhttps://github.com/jai-imageio/jai-imageio-core";

    public static List<File> createTiffFiles(File imageFile, int index) throws IOException {
        return ImageIOHelper.createTiffFiles(imageFile, index, false);
    }

    public static List<File> createTiffFiles(File imageFile, int index, boolean preserve) throws IOException {
        ArrayList<File> tiffFiles = new ArrayList<File>();
        String imageFormat = ImageIOHelper.getImageFileFormat(imageFile);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imageFormat);
        if (!readers.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_READER_MESSAGE);
        }
        ImageReader reader = readers.next();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(TIFF_FORMAT);
        if (!writers.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_WRITER_MESSAGE);
        }
        ImageWriter writer = writers.next();
        try {
            ArrayList<File> arrayList;
            block21: {
                ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
                try {
                    reader.setInput(iis);
                    TIFFImageWriteParam tiffWriteParam = new TIFFImageWriteParam(Locale.US);
                    if (!preserve) {
                        tiffWriteParam.setCompressionMode(0);
                    }
                    IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(tiffWriteParam);
                    int imageTotal = reader.getNumImages(true);
                    for (int i = 0; i < imageTotal; ++i) {
                        if (index != -1 && i != index) continue;
                        IIOImage oimage = reader.readAll(i, reader.getDefaultReadParam());
                        File tiffFile = File.createTempFile(OUTPUT_FILE_NAME, TIFF_EXT);
                        try (ImageOutputStream ios = ImageIO.createImageOutputStream(tiffFile);){
                            writer.setOutput(ios);
                            writer.write(streamMetadata, oimage, tiffWriteParam);
                            tiffFiles.add(tiffFile);
                            continue;
                        }
                    }
                    arrayList = tiffFiles;
                    if (iis == null) break block21;
                }
                catch (Throwable throwable) {
                    if (iis != null) {
                        try {
                            iis.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                iis.close();
            }
            return arrayList;
        }
        finally {
            if (reader != null) {
                reader.dispose();
            }
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    public static List<File> createTiffFiles(List<IIOImage> imageList, int index) throws IOException {
        return ImageIOHelper.createTiffFiles(imageList, index, 0, 0);
    }

    public static List<File> createTiffFiles(List<IIOImage> imageList, int index, int dpiX, int dpiY) throws IOException {
        ArrayList<File> tiffFiles = new ArrayList<File>();
        TIFFImageWriteParam tiffWriteParam = new TIFFImageWriteParam(Locale.US);
        tiffWriteParam.setCompressionMode(0);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(TIFF_FORMAT);
        if (!writers.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_WRITER_MESSAGE);
        }
        ImageWriter writer = writers.next();
        IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(tiffWriteParam);
        for (IIOImage oimage : index == -1 ? imageList : imageList.subList(index, index + 1)) {
            if (dpiX != 0 && dpiY != 0) {
                ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromRenderedImage(oimage.getRenderedImage());
                ImageWriteParam param = writer.getDefaultWriteParam();
                IIOMetadata imageMetadata = writer.getDefaultImageMetadata(imageType, param);
                imageMetadata = ImageIOHelper.setDPIViaAPI(imageMetadata, dpiX, dpiY);
                oimage.setMetadata(imageMetadata);
            }
            File tiffFile = File.createTempFile(OUTPUT_FILE_NAME, TIFF_EXT);
            ImageOutputStream ios = ImageIO.createImageOutputStream(tiffFile);
            try {
                writer.setOutput(ios);
                writer.write(streamMetadata, oimage, tiffWriteParam);
                tiffFiles.add(tiffFile);
            }
            finally {
                if (ios == null) continue;
                ios.close();
            }
        }
        writer.dispose();
        return tiffFiles;
    }

    private static IIOMetadata setDPIViaAPI(IIOMetadata imageMetadata, int dpiX, int dpiY) throws IIOInvalidTreeException {
        TIFFDirectory dir = TIFFDirectory.createFromMetadata(imageMetadata);
        BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
        TIFFTag tagXRes = base.getTag(282);
        TIFFTag tagYRes = base.getTag(283);
        TIFFField fieldXRes = new TIFFField(tagXRes, 5, 1, new long[][]{{dpiX, 1L}});
        TIFFField fieldYRes = new TIFFField(tagYRes, 5, 1, new long[][]{{dpiY, 1L}});
        dir.addTIFFField(fieldXRes);
        dir.addTIFFField(fieldYRes);
        IIOMetadata metadata = dir.getAsMetadata();
        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(25.4f / (float)dpiX));
        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(25.4f / (float)dpiY));
        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);
        root.appendChild(dim);
        metadata.mergeTree("javax_imageio_1.0", root);
        return metadata;
    }

    public static ByteBuffer getImageByteBuffer(IIOImage image) {
        return ImageIOHelper.getImageByteBuffer(image.getRenderedImage());
    }

    public static ByteBuffer getImageByteBuffer(RenderedImage image) {
        ColorModel cm = image.getColorModel();
        WritableRaster wr = image.getData().createCompatibleWritableRaster(image.getWidth(), image.getHeight());
        image.copyData(wr);
        BufferedImage bi = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
        return ImageIOHelper.convertImageData(bi);
    }

    public static ByteBuffer convertImageData(BufferedImage bi) {
        DataBuffer buff = bi.getRaster().getDataBuffer();
        if (!(buff instanceof DataBufferByte)) {
            BufferedImage grayscaleImage = ImageHelper.convertImageToGrayscale((BufferedImage)bi);
            buff = grayscaleImage.getRaster().getDataBuffer();
        }
        byte[] pixelData = ((DataBufferByte)buff).getData();
        ByteBuffer buf = ByteBuffer.allocateDirect(pixelData.length);
        buf.order(ByteOrder.nativeOrder());
        buf.put(pixelData);
        ((Buffer)buf).flip();
        return buf;
    }

    public static BufferedImage convertRenderedImage(RenderedImage image) {
        if (image == null) {
            return null;
        }
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
        ColorModel cm = image.getColorModel();
        WritableRaster raster = cm.createCompatibleWritableRaster(image.getWidth(), image.getHeight());
        image.copyData(raster);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        String[] keys = image.getPropertyNames();
        if (keys != null) {
            for (String key : keys) {
                properties.put(key, image.getProperty(key));
            }
        }
        return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), properties);
    }

    public static String getImageFileFormat(File imageFile) {
        String imageFileName = imageFile.getName();
        String imageFormat = imageFileName.substring(imageFileName.lastIndexOf(46) + 1);
        if (imageFormat.matches("(pbm|pgm|ppm)")) {
            imageFormat = "pnm";
        } else if (imageFormat.matches("(jp2|j2k|jpf|jpx|jpm)")) {
            imageFormat = "jpeg2000";
        }
        return imageFormat;
    }

    public static File getImageFile(File inputFile) throws IOException {
        File imageFile = inputFile;
        if (inputFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new UnsupportedOperationException("PDF support has been removed");
        }
        return imageFile;
    }

    public static List<BufferedImage> getImageList(File inputFile) throws IOException {
        File imageFile = ImageIOHelper.getImageFile(inputFile);
        ArrayList<BufferedImage> biList = new ArrayList<BufferedImage>();
        String imageFormat = ImageIOHelper.getImageFileFormat(imageFile);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imageFormat);
        if (!readers.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_READER_MESSAGE);
        }
        ImageReader reader = readers.next();
        try {
            ArrayList<BufferedImage> arrayList;
            block13: {
                ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
                try {
                    reader.setInput(iis);
                    int imageTotal = reader.getNumImages(true);
                    for (int i = 0; i < imageTotal; ++i) {
                        BufferedImage bi = reader.read(i);
                        biList.add(bi);
                    }
                    arrayList = biList;
                    if (iis == null) break block13;
                }
                catch (Throwable throwable) {
                    if (iis != null) {
                        try {
                            iis.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                iis.close();
            }
            return arrayList;
        }
        finally {
            if (reader != null) {
                reader.dispose();
            }
            if (imageFile != null && imageFile.exists() && imageFile != inputFile && imageFile.getName().startsWith("multipage") && imageFile.getName().endsWith(TIFF_EXT)) {
                imageFile.delete();
            }
        }
    }

    public static List<IIOImage> getIIOImageList(File inputFile) throws IOException {
        File imageFile = ImageIOHelper.getImageFile(inputFile);
        ArrayList<IIOImage> iioImageList = new ArrayList<IIOImage>();
        String imageFormat = ImageIOHelper.getImageFileFormat(imageFile);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imageFormat);
        if (!readers.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_READER_MESSAGE);
        }
        ImageReader reader = readers.next();
        try {
            ArrayList<IIOImage> arrayList;
            block13: {
                ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
                try {
                    reader.setInput(iis);
                    int imageTotal = reader.getNumImages(true);
                    for (int i = 0; i < imageTotal; ++i) {
                        IIOImage oimage = reader.readAll(i, reader.getDefaultReadParam());
                        iioImageList.add(oimage);
                    }
                    arrayList = iioImageList;
                    if (iis == null) break block13;
                }
                catch (Throwable throwable) {
                    if (iis != null) {
                        try {
                            iis.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                iis.close();
            }
            return arrayList;
        }
        finally {
            if (reader != null) {
                reader.dispose();
            }
            if (imageFile != null && imageFile.exists() && imageFile != inputFile && imageFile.getName().startsWith("multipage") && imageFile.getName().endsWith(TIFF_EXT)) {
                imageFile.delete();
            }
        }
    }

    public static IIOImage getIIOImage(BufferedImage bi) {
        return new IIOImage(bi, null, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void mergeTiff(File[] inputImages, File outputTiff) throws IOException {
        if (inputImages.length == 0) {
            return;
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(TIFF_FORMAT);
        if (!writers.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_WRITER_MESSAGE);
        }
        ImageWriter writer = writers.next();
        TIFFImageWriteParam tiffWriteParam = new TIFFImageWriteParam(Locale.US);
        IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(tiffWriteParam);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputTiff);){
            writer.setOutput(ios);
            boolean firstPage = true;
            int index = 1;
            for (File inputImage : inputImages) {
                String imageFileFormat = ImageIOHelper.getImageFileFormat(inputImage);
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imageFileFormat);
                if (!readers.hasNext()) {
                    throw new RuntimeException(JAI_IMAGE_READER_MESSAGE);
                }
                ImageReader reader = readers.next();
                try (ImageInputStream iis = ImageIO.createImageInputStream(inputImage);){
                    reader.setInput(iis);
                    int imageTotal = reader.getNumImages(true);
                    for (int i = 0; i < imageTotal; ++i) {
                        IIOImage oimage = reader.readAll(i, reader.getDefaultReadParam());
                        if (firstPage) {
                            writer.write(streamMetadata, oimage, tiffWriteParam);
                            firstPage = false;
                            continue;
                        }
                        writer.writeInsert(index++, oimage, tiffWriteParam);
                    }
                }
                finally {
                    if (reader != null) {
                        reader.dispose();
                    }
                }
            }
        }
        finally {
            writer.dispose();
        }
    }

    public static void mergeTiff(BufferedImage[] inputImages, File outputTiff) throws IOException {
        ImageIOHelper.mergeTiff(inputImages, outputTiff, null);
    }

    public static void mergeTiff(BufferedImage[] inputImages, File outputTiff, String compressionType) throws IOException {
        ArrayList<IIOImage> imageList = new ArrayList<IIOImage>();
        for (BufferedImage inputImage : inputImages) {
            imageList.add(new IIOImage(inputImage, null, null));
        }
        ImageIOHelper.mergeTiff(imageList, outputTiff, compressionType);
    }

    public static void mergeTiff(List<IIOImage> imageList, File outputTiff) throws IOException {
        ImageIOHelper.mergeTiff(imageList, outputTiff, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void mergeTiff(List<IIOImage> imageList, File outputTiff, String compressionType) throws IOException {
        if (imageList == null || imageList.isEmpty()) {
            return;
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(TIFF_FORMAT);
        if (!writers.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_WRITER_MESSAGE);
        }
        ImageWriter writer = writers.next();
        TIFFImageWriteParam tiffWriteParam = new TIFFImageWriteParam(Locale.US);
        if (compressionType != null) {
            tiffWriteParam.setCompressionMode(2);
            tiffWriteParam.setCompressionType(compressionType);
        }
        IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(tiffWriteParam);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputTiff);){
            writer.setOutput(ios);
            int dpiX = 300;
            int dpiY = 300;
            for (IIOImage iioImage : imageList) {
                ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromRenderedImage(iioImage.getRenderedImage());
                ImageWriteParam param = writer.getDefaultWriteParam();
                IIOMetadata imageMetadata = writer.getDefaultImageMetadata(imageType, param);
                imageMetadata = ImageIOHelper.setDPIViaAPI(imageMetadata, dpiX, dpiY);
                iioImage.setMetadata(imageMetadata);
            }
            IIOImage firstIioImage = imageList.remove(0);
            writer.write(streamMetadata, firstIioImage, tiffWriteParam);
            int i = 1;
            for (IIOImage iioImage : imageList) {
                writer.writeInsert(i++, iioImage, tiffWriteParam);
            }
        }
        finally {
            writer.dispose();
        }
    }

    public static File deskewImage(File imageFile, double minimumDeskewThreshold) throws IOException {
        List<BufferedImage> imageList = ImageIOHelper.getImageList(imageFile);
        for (int i = 0; i < imageList.size(); ++i) {
            BufferedImage bi = imageList.get(i);
            ImageDeskew deskew = new ImageDeskew(bi);
            double imageSkewAngle = deskew.getSkewAngle();
            if (!(imageSkewAngle > minimumDeskewThreshold) && !(imageSkewAngle < -minimumDeskewThreshold)) continue;
            bi = ImageUtil.rotate((BufferedImage)bi, (double)(-imageSkewAngle), (int)(bi.getWidth() / 2), (int)(bi.getHeight() / 2));
            imageList.set(i, bi);
        }
        File tempImageFile = File.createTempFile(FilenameUtils.getBaseName((String)imageFile.getName()), TIFF_EXT);
        ImageIOHelper.mergeTiff(imageList.toArray(new BufferedImage[0]), tempImageFile);
        return tempImageFile;
    }

    public static Map<String, String> readImageData(IIOImage oimage) {
        HashMap<String, String> dict = new HashMap<String, String>();
        IIOMetadata imageMetadata = oimage.getMetadata();
        if (imageMetadata != null) {
            int dpiY;
            int dpiX;
            IIOMetadataNode dimNode = (IIOMetadataNode)imageMetadata.getAsTree("javax_imageio_1.0");
            NodeList nodes = dimNode.getElementsByTagName("HorizontalPixelSize");
            if (nodes.getLength() > 0) {
                float dpcWidth = Float.parseFloat(nodes.item(0).getAttributes().item(0).getNodeValue());
                dpiX = Math.round(25.4f / dpcWidth);
            } else {
                dpiX = Toolkit.getDefaultToolkit().getScreenResolution();
            }
            dict.put("dpiX", String.valueOf(dpiX));
            nodes = dimNode.getElementsByTagName("VerticalPixelSize");
            if (nodes.getLength() > 0) {
                float dpcHeight = Float.parseFloat(nodes.item(0).getAttributes().item(0).getNodeValue());
                dpiY = Math.round(25.4f / dpcHeight);
            } else {
                dpiY = Toolkit.getDefaultToolkit().getScreenResolution();
            }
            dict.put("dpiY", String.valueOf(dpiY));
        }
        return dict;
    }
}

