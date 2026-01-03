/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.tiff;

import com.sun.imageio.plugins.tiff.TIFFIFD;
import com.sun.imageio.plugins.tiff.TIFFImageWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TIFFImageMetadata
extends IIOMetadata {
    public static final String NATIVE_METADATA_FORMAT_NAME = "javax_imageio_tiff_image_1.0";
    public static final String NATIVE_METADATA_FORMAT_CLASS_NAME = "javax.imageio.plugins.tiff.TIFFImageMetadataFormat";
    private List<TIFFTagSet> tagSets;
    TIFFIFD rootIFD;
    private static final String[] colorSpaceNames = new String[]{"GRAY", "GRAY", "RGB", "RGB", "GRAY", "CMYK", "YCbCr", "Lab", "Lab"};
    private static final String[] orientationNames = new String[]{null, "Normal", "FlipH", "Rotate180", "FlipV", "FlipHRotate90", "Rotate270", "FlipVRotate90", "Rotate90"};

    public TIFFImageMetadata(List<TIFFTagSet> tagSets) {
        super(true, NATIVE_METADATA_FORMAT_NAME, NATIVE_METADATA_FORMAT_CLASS_NAME, null, null);
        this.tagSets = tagSets;
        this.rootIFD = new TIFFIFD(tagSets);
    }

    public TIFFImageMetadata(TIFFIFD ifd) {
        super(true, NATIVE_METADATA_FORMAT_NAME, NATIVE_METADATA_FORMAT_CLASS_NAME, null, null);
        this.tagSets = ifd.getTagSetList();
        this.rootIFD = ifd;
    }

    public void initializeFromStream(ImageInputStream stream, boolean ignoreMetadata, boolean readUnknownTags) throws IOException {
        this.rootIFD.initialize(stream, true, ignoreMetadata, readUnknownTags);
    }

    public void addShortOrLongField(int tagNumber, long value) {
        TIFFField field = new TIFFField(this.rootIFD.getTag(tagNumber), value);
        this.rootIFD.addTIFFField(field);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    private Node getIFDAsTree(TIFFIFD ifd, String parentTagName, int parentTagNumber) {
        Iterator<Object> iter;
        List<TIFFTagSet> tagSets;
        IIOMetadataNode IFDRoot = new IIOMetadataNode("TIFFIFD");
        if (parentTagNumber != 0) {
            IFDRoot.setAttribute("parentTagNumber", Integer.toString(parentTagNumber));
        }
        if (parentTagName != null) {
            IFDRoot.setAttribute("parentTagName", parentTagName);
        }
        if ((tagSets = ifd.getTagSetList()).size() > 0) {
            iter = tagSets.iterator();
            StringBuilder tagSetNames = new StringBuilder();
            while (iter.hasNext()) {
                TIFFTagSet tagSet = (TIFFTagSet)iter.next();
                tagSetNames.append(tagSet.getClass().getName());
                if (!iter.hasNext()) continue;
                tagSetNames.append(",");
            }
            IFDRoot.setAttribute("tagSets", tagSetNames.toString());
        }
        iter = ifd.iterator();
        while (iter.hasNext()) {
            TIFFField f = (TIFFField)iter.next();
            int tagNumber = f.getTagNumber();
            TIFFTag tag = TIFFIFD.getTag(tagNumber, tagSets);
            Node node = null;
            if (tag == null) {
                node = f.getAsNativeNode();
            } else if (tag.isIFDPointer() && f.hasDirectory()) {
                TIFFIFD subIFD = TIFFIFD.getDirectoryAsIFD(f.getDirectory());
                node = this.getIFDAsTree(subIFD, tag.getName(), tag.getNumber());
            } else {
                node = f.getAsNativeNode();
            }
            if (node == null) continue;
            IFDRoot.appendChild(node);
        }
        return IFDRoot;
    }

    @Override
    public Node getAsTree(String formatName) {
        if (formatName.equals(this.nativeMetadataFormatName)) {
            return this.getNativeTree();
        }
        if (formatName.equals("javax_imageio_1.0")) {
            return this.getStandardTree();
        }
        throw new IllegalArgumentException("Not a recognized format!");
    }

    private Node getNativeTree() {
        IIOMetadataNode root = new IIOMetadataNode(this.nativeMetadataFormatName);
        Node IFDNode = this.getIFDAsTree(this.rootIFD, null, 0);
        root.appendChild(IFDNode);
        return root;
    }

    @Override
    public IIOMetadataNode getStandardChromaNode() {
        IIOMetadataNode chroma_node = new IIOMetadataNode("Chroma");
        IIOMetadataNode node = null;
        int photometricInterpretation = -1;
        boolean isPaletteColor = false;
        TIFFField f = this.getTIFFField(262);
        if (f != null) {
            photometricInterpretation = f.getAsInt(0);
            isPaletteColor = photometricInterpretation == 3;
        }
        int numChannels = -1;
        if (isPaletteColor) {
            numChannels = 3;
        } else {
            f = this.getTIFFField(277);
            if (f != null) {
                numChannels = f.getAsInt(0);
            } else {
                f = this.getTIFFField(258);
                if (f != null) {
                    numChannels = f.getCount();
                }
            }
        }
        if (photometricInterpretation != -1) {
            if (photometricInterpretation >= 0 && photometricInterpretation < colorSpaceNames.length) {
                node = new IIOMetadataNode("ColorSpaceType");
                String csName = photometricInterpretation == 5 && numChannels == 3 ? "CMY" : colorSpaceNames[photometricInterpretation];
                node.setAttribute("name", csName);
                chroma_node.appendChild(node);
            }
            node = new IIOMetadataNode("BlackIsZero");
            node.setAttribute("value", photometricInterpretation == 0 ? "FALSE" : "TRUE");
            chroma_node.appendChild(node);
        }
        if (numChannels != -1) {
            node = new IIOMetadataNode("NumChannels");
            node.setAttribute("value", Integer.toString(numChannels));
            chroma_node.appendChild(node);
        }
        if ((f = this.getTIFFField(320)) != null) {
            boolean hasAlpha = false;
            node = new IIOMetadataNode("Palette");
            int len = f.getCount() / (hasAlpha ? 4 : 3);
            for (int i = 0; i < len; ++i) {
                IIOMetadataNode entry = new IIOMetadataNode("PaletteEntry");
                entry.setAttribute("index", Integer.toString(i));
                int r = f.getAsInt(i) * 255 / 65535;
                int g = f.getAsInt(len + i) * 255 / 65535;
                int b = f.getAsInt(2 * len + i) * 255 / 65535;
                entry.setAttribute("red", Integer.toString(r));
                entry.setAttribute("green", Integer.toString(g));
                entry.setAttribute("blue", Integer.toString(b));
                if (hasAlpha) {
                    int alpha = 0;
                    entry.setAttribute("alpha", Integer.toString(alpha));
                }
                node.appendChild(entry);
            }
            chroma_node.appendChild(node);
        }
        return chroma_node;
    }

    @Override
    public IIOMetadataNode getStandardCompressionNode() {
        IIOMetadataNode compression_node = new IIOMetadataNode("Compression");
        IIOMetadataNode node = null;
        TIFFField f = this.getTIFFField(259);
        if (f != null) {
            String compressionTypeName = null;
            int compression = f.getAsInt(0);
            boolean isLossless = true;
            if (compression == 1) {
                compressionTypeName = "None";
                isLossless = true;
            } else {
                int[] compressionNumbers = TIFFImageWriter.compressionNumbers;
                for (int i = 0; i < compressionNumbers.length; ++i) {
                    if (compression != compressionNumbers[i]) continue;
                    compressionTypeName = TIFFImageWriter.compressionTypes[i];
                    isLossless = TIFFImageWriter.isCompressionLossless[i];
                    break;
                }
            }
            if (compressionTypeName != null) {
                node = new IIOMetadataNode("CompressionTypeName");
                node.setAttribute("value", compressionTypeName);
                compression_node.appendChild(node);
                node = new IIOMetadataNode("Lossless");
                node.setAttribute("value", isLossless ? "TRUE" : "FALSE");
                compression_node.appendChild(node);
            }
        }
        node = new IIOMetadataNode("NumProgressiveScans");
        node.setAttribute("value", "1");
        compression_node.appendChild(node);
        return compression_node;
    }

    private String repeat(String s, int times) {
        if (times == 1) {
            return s;
        }
        StringBuilder sb = new StringBuilder((s.length() + 1) * times - 1);
        sb.append(s);
        for (int i = 1; i < times; ++i) {
            sb.append(" ");
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public IIOMetadataNode getStandardDataNode() {
        IIOMetadataNode data_node = new IIOMetadataNode("Data");
        IIOMetadataNode node = null;
        boolean isPaletteColor = false;
        TIFFField f = this.getTIFFField(262);
        if (f != null) {
            isPaletteColor = f.getAsInt(0) == 3;
        }
        f = this.getTIFFField(284);
        String planarConfiguration = "PixelInterleaved";
        if (f != null && f.getAsInt(0) == 2) {
            planarConfiguration = "PlaneInterleaved";
        }
        node = new IIOMetadataNode("PlanarConfiguration");
        node.setAttribute("value", planarConfiguration);
        data_node.appendChild(node);
        f = this.getTIFFField(262);
        if (f != null) {
            int photometricInterpretation = f.getAsInt(0);
            String sampleFormat = "UnsignedIntegral";
            if (photometricInterpretation == 3) {
                sampleFormat = "Index";
            } else {
                f = this.getTIFFField(339);
                if (f != null) {
                    int format = f.getAsInt(0);
                    sampleFormat = format == 2 ? "SignedIntegral" : (format == 1 ? "UnsignedIntegral" : (format == 3 ? "Real" : null));
                }
            }
            if (sampleFormat != null) {
                node = new IIOMetadataNode("SampleFormat");
                node.setAttribute("value", sampleFormat);
                data_node.appendChild(node);
            }
        }
        f = this.getTIFFField(258);
        int[] bitsPerSample = null;
        if (f != null) {
            bitsPerSample = f.getAsInts();
        } else {
            int compression;
            f = this.getTIFFField(259);
            int n = compression = f != null ? f.getAsInt(0) : 1;
            bitsPerSample = this.getTIFFField(34665) != null || compression == 7 || compression == 6 || this.getTIFFField(513) != null ? ((f = this.getTIFFField(262)) != null && (f.getAsInt(0) == 0 || f.getAsInt(0) == 1) ? new int[]{8} : new int[]{8, 8, 8}) : new int[]{1};
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitsPerSample.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(bitsPerSample[i]);
        }
        node = new IIOMetadataNode("BitsPerSample");
        if (isPaletteColor) {
            node.setAttribute("value", this.repeat(sb.toString(), 3));
        } else {
            node.setAttribute("value", sb.toString());
        }
        data_node.appendChild(node);
        f = this.getTIFFField(266);
        int fillOrder = f != null ? f.getAsInt(0) : 1;
        sb = new StringBuilder();
        for (int i = 0; i < bitsPerSample.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int maxBitIndex = bitsPerSample[i] == 1 ? 7 : bitsPerSample[i] - 1;
            int msb = fillOrder == 1 ? maxBitIndex : 0;
            sb.append(msb);
        }
        node = new IIOMetadataNode("SampleMSB");
        if (isPaletteColor) {
            node.setAttribute("value", this.repeat(sb.toString(), 3));
        } else {
            node.setAttribute("value", sb.toString());
        }
        data_node.appendChild(node);
        return data_node;
    }

    @Override
    public IIOMetadataNode getStandardDimensionNode() {
        int o;
        int resolutionUnit;
        IIOMetadataNode dimension_node = new IIOMetadataNode("Dimension");
        IIOMetadataNode node = null;
        long[] xres = null;
        long[] yres = null;
        TIFFField f = this.getTIFFField(282);
        if (f != null) {
            xres = (long[])f.getAsRational(0).clone();
        }
        if ((f = this.getTIFFField(283)) != null) {
            yres = (long[])f.getAsRational(0).clone();
        }
        if (xres != null && yres != null) {
            node = new IIOMetadataNode("PixelAspectRatio");
            float ratio = (float)((double)xres[1] * (double)yres[0]) / (float)(xres[0] * yres[1]);
            node.setAttribute("value", Float.toString(ratio));
            dimension_node.appendChild(node);
        }
        if (xres != null || yres != null) {
            boolean gotPixelSize;
            f = this.getTIFFField(296);
            int resolutionUnit2 = f != null ? f.getAsInt(0) : 2;
            boolean bl = gotPixelSize = resolutionUnit2 != 1;
            if (resolutionUnit2 == 2) {
                if (xres != null) {
                    xres[0] = xres[0] * 100L;
                    xres[1] = xres[1] * 254L;
                }
                if (yres != null) {
                    yres[0] = yres[0] * 100L;
                    yres[1] = yres[1] * 254L;
                }
            }
            if (gotPixelSize) {
                if (xres != null) {
                    float horizontalPixelSize = (float)(10.0 * (double)xres[1] / (double)xres[0]);
                    node = new IIOMetadataNode("HorizontalPixelSize");
                    node.setAttribute("value", Float.toString(horizontalPixelSize));
                    dimension_node.appendChild(node);
                }
                if (yres != null) {
                    float verticalPixelSize = (float)(10.0 * (double)yres[1] / (double)yres[0]);
                    node = new IIOMetadataNode("VerticalPixelSize");
                    node.setAttribute("value", Float.toString(verticalPixelSize));
                    dimension_node.appendChild(node);
                }
            }
        }
        int n = resolutionUnit = (f = this.getTIFFField(296)) != null ? f.getAsInt(0) : 2;
        if (resolutionUnit == 2 || resolutionUnit == 3) {
            f = this.getTIFFField(286);
            if (f != null) {
                long[] xpos = f.getAsRational(0);
                float xPosition = (float)xpos[0] / (float)xpos[1];
                xPosition = resolutionUnit == 2 ? (xPosition *= 254.0f) : (xPosition *= 10.0f);
                node = new IIOMetadataNode("HorizontalPosition");
                node.setAttribute("value", Float.toString(xPosition));
                dimension_node.appendChild(node);
            }
            if ((f = this.getTIFFField(287)) != null) {
                long[] ypos = f.getAsRational(0);
                float yPosition = (float)ypos[0] / (float)ypos[1];
                yPosition = resolutionUnit == 2 ? (yPosition *= 254.0f) : (yPosition *= 10.0f);
                node = new IIOMetadataNode("VerticalPosition");
                node.setAttribute("value", Float.toString(yPosition));
                dimension_node.appendChild(node);
            }
        }
        if ((f = this.getTIFFField(274)) != null && (o = f.getAsInt(0)) >= 0 && o < orientationNames.length) {
            node = new IIOMetadataNode("ImageOrientation");
            node.setAttribute("value", orientationNames[o]);
            dimension_node.appendChild(node);
        }
        return dimension_node;
    }

    @Override
    public IIOMetadataNode getStandardDocumentNode() {
        String s;
        IIOMetadataNode document_node = new IIOMetadataNode("Document");
        IIOMetadataNode node = null;
        node = new IIOMetadataNode("FormatVersion");
        node.setAttribute("value", "6.0");
        document_node.appendChild(node);
        TIFFField f = this.getTIFFField(254);
        if (f != null) {
            int newSubFileType = f.getAsInt(0);
            String value = null;
            if ((newSubFileType & 4) != 0) {
                value = "TransparencyMask";
            } else if ((newSubFileType & 1) != 0) {
                value = "ReducedResolution";
            } else if ((newSubFileType & 2) != 0) {
                value = "SinglePage";
            }
            if (value != null) {
                node = new IIOMetadataNode("SubimageInterpretation");
                node.setAttribute("value", value);
                document_node.appendChild(node);
            }
        }
        if ((f = this.getTIFFField(306)) != null && (s = f.getAsString(0)).length() == 19) {
            boolean appendNode;
            node = new IIOMetadataNode("ImageCreationTime");
            try {
                node.setAttribute("year", s.substring(0, 4));
                node.setAttribute("month", s.substring(5, 7));
                node.setAttribute("day", s.substring(8, 10));
                node.setAttribute("hour", s.substring(11, 13));
                node.setAttribute("minute", s.substring(14, 16));
                node.setAttribute("second", s.substring(17, 19));
                appendNode = true;
            }
            catch (IndexOutOfBoundsException e) {
                appendNode = false;
            }
            if (appendNode) {
                document_node.appendChild(node);
            }
        }
        return document_node;
    }

    @Override
    public IIOMetadataNode getStandardTextNode() {
        IIOMetadataNode text_node = null;
        IIOMetadataNode node = null;
        int[] textFieldTagNumbers = new int[]{269, 270, 271, 272, 285, 305, 315, 316, 333, 33432};
        for (int i = 0; i < textFieldTagNumbers.length; ++i) {
            TIFFField f = this.getTIFFField(textFieldTagNumbers[i]);
            if (f == null) continue;
            String value = f.getAsString(0);
            if (text_node == null) {
                text_node = new IIOMetadataNode("Text");
            }
            node = new IIOMetadataNode("TextEntry");
            node.setAttribute("keyword", f.getTag().getName());
            node.setAttribute("value", value);
            text_node.appendChild(node);
        }
        return text_node;
    }

    @Override
    public IIOMetadataNode getStandardTransparencyNode() {
        IIOMetadataNode transparency_node = new IIOMetadataNode("Transparency");
        IIOMetadataNode node = null;
        node = new IIOMetadataNode("Alpha");
        String value = "none";
        TIFFField f = this.getTIFFField(338);
        if (f != null) {
            int[] extraSamples = f.getAsInts();
            for (int i = 0; i < extraSamples.length; ++i) {
                if (extraSamples[i] == 1) {
                    value = "premultiplied";
                    break;
                }
                if (extraSamples[i] != 2) continue;
                value = "nonpremultiplied";
                break;
            }
        }
        node.setAttribute("value", value);
        transparency_node.appendChild(node);
        return transparency_node;
    }

    private static void fatal(Node node, String reason) throws IIOInvalidTreeException {
        throw new IIOInvalidTreeException(reason, node);
    }

    private int[] listToIntArray(String list) {
        StringTokenizer st = new StringTokenizer(list, " ");
        ArrayList<Integer> intList = new ArrayList<Integer>();
        while (st.hasMoreTokens()) {
            String nextInteger = st.nextToken();
            Integer nextInt = Integer.valueOf(nextInteger);
            intList.add(nextInt);
        }
        int[] intArray = new int[intList.size()];
        for (int i = 0; i < intArray.length; ++i) {
            intArray[i] = (Integer)intList.get(i);
        }
        return intArray;
    }

    private char[] listToCharArray(String list) {
        StringTokenizer st = new StringTokenizer(list, " ");
        ArrayList<Integer> intList = new ArrayList<Integer>();
        while (st.hasMoreTokens()) {
            String nextInteger = st.nextToken();
            Integer nextInt = Integer.valueOf(nextInteger);
            intList.add(nextInt);
        }
        char[] charArray = new char[intList.size()];
        for (int i = 0; i < charArray.length; ++i) {
            charArray[i] = (char)((Integer)intList.get(i)).intValue();
        }
        return charArray;
    }

    private void mergeStandardTree(Node root) throws IIOInvalidTreeException {
        TIFFField f;
        TIFFTag tag;
        Node chromaNode;
        Node sampleFormatNode;
        Node node = root;
        if (!node.getNodeName().equals("javax_imageio_1.0")) {
            TIFFImageMetadata.fatal(node, "Root must be javax_imageio_1.0");
        }
        String sampleFormat = null;
        Node dataNode = this.getChildNode(root, "Data");
        boolean isPaletteColor = false;
        if (dataNode != null && (sampleFormatNode = this.getChildNode(dataNode, "SampleFormat")) != null) {
            sampleFormat = TIFFImageMetadata.getAttribute(sampleFormatNode, "value");
            isPaletteColor = sampleFormat.equals("Index");
        }
        if (!isPaletteColor && (chromaNode = this.getChildNode(root, "Chroma")) != null && this.getChildNode(chromaNode, "Palette") != null) {
            isPaletteColor = true;
        }
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
            String childName;
            String name = node.getNodeName();
            if (name.equals("Chroma")) {
                String colorSpaceType = null;
                String blackIsZero = null;
                boolean gotPalette = false;
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    String childName2 = child.getNodeName();
                    if (childName2.equals("ColorSpaceType")) {
                        colorSpaceType = TIFFImageMetadata.getAttribute(child, "name");
                        continue;
                    }
                    if (childName2.equals("NumChannels")) {
                        tag = this.rootIFD.getTag(277);
                        int samplesPerPixel = isPaletteColor ? 1 : Integer.parseInt(TIFFImageMetadata.getAttribute(child, "value"));
                        f = new TIFFField(tag, samplesPerPixel);
                        this.rootIFD.addTIFFField(f);
                        continue;
                    }
                    if (childName2.equals("BlackIsZero")) {
                        blackIsZero = TIFFImageMetadata.getAttribute(child, "value");
                        continue;
                    }
                    if (!childName2.equals("Palette")) continue;
                    HashMap<Integer, char[]> palette = new HashMap<Integer, char[]>();
                    int maxIndex = -1;
                    for (Node entry = child.getFirstChild(); entry != null; entry = entry.getNextSibling()) {
                        String entryName = entry.getNodeName();
                        if (!entryName.equals("PaletteEntry")) continue;
                        String idx = TIFFImageMetadata.getAttribute(entry, "index");
                        int id = Integer.parseInt(idx);
                        if (id > maxIndex) {
                            maxIndex = id;
                        }
                        char red = (char)Integer.parseInt(TIFFImageMetadata.getAttribute(entry, "red"));
                        char green = (char)Integer.parseInt(TIFFImageMetadata.getAttribute(entry, "green"));
                        char blue = (char)Integer.parseInt(TIFFImageMetadata.getAttribute(entry, "blue"));
                        palette.put(id, new char[]{red, green, blue});
                        gotPalette = true;
                    }
                    if (!gotPalette) continue;
                    int mapSize = maxIndex + 1;
                    int paletteLength = 3 * mapSize;
                    char[] paletteEntries = new char[paletteLength];
                    for (Map.Entry paletteEntry : palette.entrySet()) {
                        int index = (Integer)paletteEntry.getKey();
                        char[] rgb = (char[])paletteEntry.getValue();
                        paletteEntries[index] = (char)(rgb[0] * 65535 / 255);
                        paletteEntries[mapSize + index] = (char)(rgb[1] * 65535 / 255);
                        paletteEntries[2 * mapSize + index] = (char)(rgb[2] * 65535 / 255);
                    }
                    tag = this.rootIFD.getTag(320);
                    f = new TIFFField(tag, 3, paletteLength, paletteEntries);
                    this.rootIFD.addTIFFField(f);
                }
                int photometricInterpretation = -1;
                if ((colorSpaceType == null || colorSpaceType.equals("GRAY")) && blackIsZero != null && blackIsZero.equalsIgnoreCase("FALSE")) {
                    photometricInterpretation = 0;
                } else if (colorSpaceType != null) {
                    if (colorSpaceType.equals("GRAY")) {
                        Node siNode;
                        String value;
                        IIOMetadataNode iioRoot;
                        NodeList siNodeList;
                        boolean isTransparency = false;
                        if (root instanceof IIOMetadataNode && (siNodeList = (iioRoot = (IIOMetadataNode)root).getElementsByTagName("SubimageInterpretation")).getLength() == 1 && (value = TIFFImageMetadata.getAttribute(siNode = siNodeList.item(0), "value")).equals("TransparencyMask")) {
                            isTransparency = true;
                        }
                        photometricInterpretation = isTransparency ? 4 : 1;
                    } else if (colorSpaceType.equals("RGB")) {
                        photometricInterpretation = gotPalette ? 3 : 2;
                    } else if (colorSpaceType.equals("YCbCr")) {
                        photometricInterpretation = 6;
                    } else if (colorSpaceType.equals("CMYK")) {
                        photometricInterpretation = 5;
                    } else if (colorSpaceType.equals("Lab")) {
                        photometricInterpretation = 8;
                    }
                }
                if (photometricInterpretation == -1) continue;
                tag = this.rootIFD.getTag(262);
                f = new TIFFField(tag, photometricInterpretation);
                this.rootIFD.addTIFFField(f);
                continue;
            }
            if (name.equals("Compression")) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    childName = child.getNodeName();
                    if (!childName.equals("CompressionTypeName")) continue;
                    int compression = -1;
                    String compressionTypeName = TIFFImageMetadata.getAttribute(child, "value");
                    if (compressionTypeName.equalsIgnoreCase("None")) {
                        compression = 1;
                    } else {
                        String[] compressionNames = TIFFImageWriter.compressionTypes;
                        for (int i = 0; i < compressionNames.length; ++i) {
                            if (!compressionNames[i].equalsIgnoreCase(compressionTypeName)) continue;
                            compression = TIFFImageWriter.compressionNumbers[i];
                            break;
                        }
                    }
                    if (compression == -1) continue;
                    tag = this.rootIFD.getTag(259);
                    f = new TIFFField(tag, compression);
                    this.rootIFD.addTIFFField(f);
                }
                continue;
            }
            if (name.equals("Data")) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    childName = child.getNodeName();
                    if (childName.equals("PlanarConfiguration")) {
                        String pc = TIFFImageMetadata.getAttribute(child, "value");
                        int planarConfiguration = -1;
                        if (pc.equals("PixelInterleaved")) {
                            planarConfiguration = 1;
                        } else if (pc.equals("PlaneInterleaved")) {
                            planarConfiguration = 2;
                        }
                        if (planarConfiguration == -1) continue;
                        tag = this.rootIFD.getTag(284);
                        f = new TIFFField(tag, planarConfiguration);
                        this.rootIFD.addTIFFField(f);
                        continue;
                    }
                    if (childName.equals("BitsPerSample")) {
                        String bps = TIFFImageMetadata.getAttribute(child, "value");
                        char[] bitsPerSample = this.listToCharArray(bps);
                        tag = this.rootIFD.getTag(258);
                        f = isPaletteColor ? new TIFFField(tag, 3, 1, new char[]{bitsPerSample[0]}) : new TIFFField(tag, 3, bitsPerSample.length, bitsPerSample);
                        this.rootIFD.addTIFFField(f);
                        continue;
                    }
                    if (!childName.equals("SampleMSB")) continue;
                    String sMSB = TIFFImageMetadata.getAttribute(child, "value");
                    int[] sampleMSB = this.listToIntArray(sMSB);
                    boolean isRightToLeft = true;
                    for (int i = 0; i < sampleMSB.length; ++i) {
                        if (sampleMSB[i] == 0) continue;
                        isRightToLeft = false;
                        break;
                    }
                    int fillOrder = isRightToLeft ? 2 : 1;
                    tag = this.rootIFD.getTag(266);
                    f = new TIFFField(tag, fillOrder);
                    this.rootIFD.addTIFFField(f);
                }
                continue;
            }
            if (name.equals("Dimension")) {
                long[][] vData;
                long[][] hData;
                float pixelAspectRatio = -1.0f;
                boolean gotPixelAspectRatio = false;
                float horizontalPixelSize = -1.0f;
                boolean gotHorizontalPixelSize = false;
                float verticalPixelSize = -1.0f;
                boolean gotVerticalPixelSize = false;
                boolean sizeIsAbsolute = false;
                float horizontalPosition = -1.0f;
                boolean gotHorizontalPosition = false;
                float verticalPosition = -1.0f;
                boolean gotVerticalPosition = false;
                block8: for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    String childName3 = child.getNodeName();
                    if (childName3.equals("PixelAspectRatio")) {
                        String par = TIFFImageMetadata.getAttribute(child, "value");
                        pixelAspectRatio = Float.parseFloat(par);
                        gotPixelAspectRatio = true;
                        continue;
                    }
                    if (childName3.equals("ImageOrientation")) {
                        String orientation = TIFFImageMetadata.getAttribute(child, "value");
                        for (int i = 0; i < orientationNames.length; ++i) {
                            if (!orientation.equals(orientationNames[i])) continue;
                            char[] oData = new char[]{(char)i};
                            f = new TIFFField(this.rootIFD.getTag(274), 3, 1, oData);
                            this.rootIFD.addTIFFField(f);
                            continue block8;
                        }
                        continue;
                    }
                    if (childName3.equals("HorizontalPixelSize")) {
                        String hps = TIFFImageMetadata.getAttribute(child, "value");
                        horizontalPixelSize = Float.parseFloat(hps);
                        gotHorizontalPixelSize = true;
                        continue;
                    }
                    if (childName3.equals("VerticalPixelSize")) {
                        String vps = TIFFImageMetadata.getAttribute(child, "value");
                        verticalPixelSize = Float.parseFloat(vps);
                        gotVerticalPixelSize = true;
                        continue;
                    }
                    if (childName3.equals("HorizontalPosition")) {
                        String hp = TIFFImageMetadata.getAttribute(child, "value");
                        horizontalPosition = Float.parseFloat(hp);
                        gotHorizontalPosition = true;
                        continue;
                    }
                    if (!childName3.equals("VerticalPosition")) continue;
                    String vp = TIFFImageMetadata.getAttribute(child, "value");
                    verticalPosition = Float.parseFloat(vp);
                    gotVerticalPosition = true;
                }
                boolean bl = sizeIsAbsolute = gotHorizontalPixelSize || gotVerticalPixelSize;
                if (gotPixelAspectRatio) {
                    if (gotHorizontalPixelSize && !gotVerticalPixelSize) {
                        verticalPixelSize = horizontalPixelSize / pixelAspectRatio;
                        gotVerticalPixelSize = true;
                    } else if (gotVerticalPixelSize && !gotHorizontalPixelSize) {
                        horizontalPixelSize = verticalPixelSize * pixelAspectRatio;
                        gotHorizontalPixelSize = true;
                    } else if (!gotHorizontalPixelSize && !gotVerticalPixelSize) {
                        horizontalPixelSize = pixelAspectRatio;
                        verticalPixelSize = 1.0f;
                        gotHorizontalPixelSize = true;
                        gotVerticalPixelSize = true;
                    }
                }
                if (gotHorizontalPixelSize) {
                    float xResolution = (sizeIsAbsolute ? 10.0f : 1.0f) / horizontalPixelSize;
                    hData = new long[1][2];
                    hData[0] = new long[2];
                    hData[0][0] = (long)(xResolution * 10000.0f);
                    hData[0][1] = 10000L;
                    f = new TIFFField(this.rootIFD.getTag(282), 5, 1, hData);
                    this.rootIFD.addTIFFField(f);
                }
                if (gotVerticalPixelSize) {
                    float yResolution = (sizeIsAbsolute ? 10.0f : 1.0f) / verticalPixelSize;
                    vData = new long[1][2];
                    vData[0] = new long[2];
                    vData[0][0] = (long)(yResolution * 10000.0f);
                    vData[0][1] = 10000L;
                    f = new TIFFField(this.rootIFD.getTag(283), 5, 1, vData);
                    this.rootIFD.addTIFFField(f);
                }
                char[] res = new char[]{(char)(sizeIsAbsolute ? 3 : 1)};
                f = new TIFFField(this.rootIFD.getTag(296), 3, 1, res);
                this.rootIFD.addTIFFField(f);
                if (!sizeIsAbsolute) continue;
                if (gotHorizontalPosition) {
                    hData = new long[1][2];
                    hData[0][0] = (long)(horizontalPosition * 10000.0f);
                    hData[0][1] = 100000L;
                    f = new TIFFField(this.rootIFD.getTag(286), 5, 1, hData);
                    this.rootIFD.addTIFFField(f);
                }
                if (!gotVerticalPosition) continue;
                vData = new long[1][2];
                vData[0][0] = (long)(verticalPosition * 10000.0f);
                vData[0][1] = 100000L;
                f = new TIFFField(this.rootIFD.getTag(287), 5, 1, vData);
                this.rootIFD.addTIFFField(f);
                continue;
            }
            if (name.equals("Document")) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    childName = child.getNodeName();
                    if (childName.equals("SubimageInterpretation")) {
                        String si = TIFFImageMetadata.getAttribute(child, "value");
                        int newSubFileType = -1;
                        if (si.equals("TransparencyMask")) {
                            newSubFileType = 4;
                        } else if (si.equals("ReducedResolution")) {
                            newSubFileType = 1;
                        } else if (si.equals("SinglePage")) {
                            newSubFileType = 2;
                        }
                        if (newSubFileType != -1) {
                            tag = this.rootIFD.getTag(254);
                            f = new TIFFField(tag, newSubFileType);
                            this.rootIFD.addTIFFField(f);
                        }
                    }
                    if (!childName.equals("ImageCreationTime")) continue;
                    String year = TIFFImageMetadata.getAttribute(child, "year");
                    String month = TIFFImageMetadata.getAttribute(child, "month");
                    String day = TIFFImageMetadata.getAttribute(child, "day");
                    String hour = TIFFImageMetadata.getAttribute(child, "hour");
                    String minute = TIFFImageMetadata.getAttribute(child, "minute");
                    String second = TIFFImageMetadata.getAttribute(child, "second");
                    StringBuilder sb = new StringBuilder();
                    sb.append(year);
                    sb.append(":");
                    if (month.length() == 1) {
                        sb.append("0");
                    }
                    sb.append(month);
                    sb.append(":");
                    if (day.length() == 1) {
                        sb.append("0");
                    }
                    sb.append(day);
                    sb.append(" ");
                    if (hour.length() == 1) {
                        sb.append("0");
                    }
                    sb.append(hour);
                    sb.append(":");
                    if (minute.length() == 1) {
                        sb.append("0");
                    }
                    sb.append(minute);
                    sb.append(":");
                    if (second.length() == 1) {
                        sb.append("0");
                    }
                    sb.append(second);
                    String[] dt = new String[]{sb.toString()};
                    f = new TIFFField(this.rootIFD.getTag(306), 2, 1, dt);
                    this.rootIFD.addTIFFField(f);
                }
                continue;
            }
            if (name.equals("Text")) {
                String theAuthor = null;
                String theDescription = null;
                String theTitle = null;
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    String childName4 = child.getNodeName();
                    if (!childName4.equals("TextEntry")) continue;
                    int tagNumber = -1;
                    NamedNodeMap childAttrs = child.getAttributes();
                    Node keywordNode = childAttrs.getNamedItem("keyword");
                    if (keywordNode == null) continue;
                    String keyword = keywordNode.getNodeValue();
                    String value = TIFFImageMetadata.getAttribute(child, "value");
                    if (keyword.isEmpty() || value.isEmpty()) continue;
                    if (keyword.equalsIgnoreCase("DocumentName")) {
                        tagNumber = 269;
                    } else if (keyword.equalsIgnoreCase("ImageDescription")) {
                        tagNumber = 270;
                    } else if (keyword.equalsIgnoreCase("Make")) {
                        tagNumber = 271;
                    } else if (keyword.equalsIgnoreCase("Model")) {
                        tagNumber = 272;
                    } else if (keyword.equalsIgnoreCase("PageName")) {
                        tagNumber = 285;
                    } else if (keyword.equalsIgnoreCase("Software")) {
                        tagNumber = 305;
                    } else if (keyword.equalsIgnoreCase("Artist")) {
                        tagNumber = 315;
                    } else if (keyword.equalsIgnoreCase("HostComputer")) {
                        tagNumber = 316;
                    } else if (keyword.equalsIgnoreCase("InkNames")) {
                        tagNumber = 333;
                    } else if (keyword.equalsIgnoreCase("Copyright")) {
                        tagNumber = 33432;
                    } else if (keyword.equalsIgnoreCase("author")) {
                        theAuthor = value;
                    } else if (keyword.equalsIgnoreCase("description")) {
                        theDescription = value;
                    } else if (keyword.equalsIgnoreCase("title")) {
                        theTitle = value;
                    }
                    if (tagNumber == -1) continue;
                    f = new TIFFField(this.rootIFD.getTag(tagNumber), 2, 1, new String[]{value});
                    this.rootIFD.addTIFFField(f);
                }
                if (theAuthor != null && this.getTIFFField(315) == null) {
                    f = new TIFFField(this.rootIFD.getTag(315), 2, 1, new String[]{theAuthor});
                    this.rootIFD.addTIFFField(f);
                }
                if (theDescription != null && this.getTIFFField(270) == null) {
                    f = new TIFFField(this.rootIFD.getTag(270), 2, 1, new String[]{theDescription});
                    this.rootIFD.addTIFFField(f);
                }
                if (theTitle == null || this.getTIFFField(269) != null) continue;
                f = new TIFFField(this.rootIFD.getTag(269), 2, 1, new String[]{theTitle});
                this.rootIFD.addTIFFField(f);
                continue;
            }
            if (!name.equals("Transparency")) continue;
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                childName = child.getNodeName();
                if (!childName.equals("Alpha")) continue;
                String alpha = TIFFImageMetadata.getAttribute(child, "value");
                f = null;
                if (alpha.equals("premultiplied")) {
                    f = new TIFFField(this.rootIFD.getTag(338), 1L);
                } else if (alpha.equals("nonpremultiplied")) {
                    f = new TIFFField(this.rootIFD.getTag(338), 2L);
                }
                if (f == null) continue;
                this.rootIFD.addTIFFField(f);
            }
        }
        if (sampleFormat != null) {
            int sf = -1;
            if (sampleFormat.equals("SignedIntegral")) {
                sf = 2;
            } else if (sampleFormat.equals("UnsignedIntegral")) {
                sf = 1;
            } else if (sampleFormat.equals("Real")) {
                sf = 3;
            } else if (sampleFormat.equals("Index")) {
                sf = 1;
            }
            if (sf != -1) {
                int count = 1;
                f = this.getTIFFField(277);
                if (f != null) {
                    count = f.getAsInt(0);
                } else {
                    f = this.getTIFFField(258);
                    if (f != null) {
                        count = f.getCount();
                    }
                }
                char[] sampleFormatArray = new char[count];
                Arrays.fill(sampleFormatArray, (char)sf);
                tag = this.rootIFD.getTag(339);
                f = new TIFFField(tag, 3, sampleFormatArray.length, sampleFormatArray);
                this.rootIFD.addTIFFField(f);
            }
        }
    }

    private static String getAttribute(Node node, String attrName) {
        NamedNodeMap attrs = node.getAttributes();
        Node attr = attrs.getNamedItem(attrName);
        return attr != null ? attr.getNodeValue() : null;
    }

    private Node getChildNode(Node node, String childName) {
        Node childNode = null;
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            int length = childNodes.getLength();
            for (int i = 0; i < length; ++i) {
                Node item = childNodes.item(i);
                if (!item.getNodeName().equals(childName)) continue;
                childNode = item;
                break;
            }
        }
        return childNode;
    }

    public static TIFFIFD parseIFD(Node node) throws IIOInvalidTreeException {
        if (!node.getNodeName().equals("TIFFIFD")) {
            TIFFImageMetadata.fatal(node, "Expected \"TIFFIFD\" node");
        }
        String tagSetNames = TIFFImageMetadata.getAttribute(node, "tagSets");
        ArrayList<TIFFTagSet> tagSets = new ArrayList<TIFFTagSet>(5);
        if (tagSetNames != null) {
            StringTokenizer st = new StringTokenizer(tagSetNames, ",");
            while (st.hasMoreTokens()) {
                String className = st.nextToken();
                Object o = null;
                Class<?> setClass = null;
                try {
                    ClassLoader cl = TIFFImageMetadata.class.getClassLoader();
                    setClass = Class.forName(className, false, cl);
                    if (!TIFFTagSet.class.isAssignableFrom(setClass)) {
                        TIFFImageMetadata.fatal(node, "TagSets in IFD must be subset of TIFFTagSet class");
                    }
                    Method getInstanceMethod = setClass.getMethod("getInstance", null);
                    o = getInstanceMethod.invoke(null, (Object[])null);
                }
                catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                if (!(o instanceof TIFFTagSet)) {
                    TIFFImageMetadata.fatal(node, "Specified tag set class \"" + className + "\" is not an instance of TIFFTagSet");
                    continue;
                }
                tagSets.add((TIFFTagSet)o);
            }
        }
        TIFFIFD ifd = new TIFFIFD(tagSets);
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
            String name = node.getNodeName();
            TIFFField f = null;
            if (name.equals("TIFFIFD")) {
                int type;
                TIFFIFD subIFD = TIFFImageMetadata.parseIFD(node);
                String parentTagName = TIFFImageMetadata.getAttribute(node, "parentTagName");
                String parentTagNumber = TIFFImageMetadata.getAttribute(node, "parentTagNumber");
                TIFFTag tag = null;
                if (parentTagName != null) {
                    tag = TIFFIFD.getTag(parentTagName, tagSets);
                } else if (parentTagNumber != null) {
                    int tagNumber = Integer.parseUnsignedInt(parentTagNumber);
                    tag = TIFFIFD.getTag(tagNumber, tagSets);
                }
                if (tag == null) {
                    type = 4;
                    tag = new TIFFTag("UnknownTag", 0, 1 << type);
                } else if (tag.isDataTypeOK(13)) {
                    type = 13;
                } else if (tag.isDataTypeOK(4)) {
                    type = 4;
                } else {
                    for (type = 13; type >= 1 && !tag.isDataTypeOK(type); --type) {
                    }
                }
                f = new TIFFField(tag, type, 1L, subIFD);
            } else if (name.equals("TIFFField")) {
                int number = Integer.parseInt(TIFFImageMetadata.getAttribute(node, "number"));
                TIFFTagSet tagSet = null;
                for (TIFFTagSet t : tagSets) {
                    if (t.getTag(number) == null) continue;
                    tagSet = t;
                    break;
                }
                f = TIFFField.createFromMetadataNode(tagSet, node);
            } else {
                TIFFImageMetadata.fatal(node, "Expected either \"TIFFIFD\" or \"TIFFField\" node, got " + name);
            }
            ifd.addTIFFField(f);
        }
        return ifd;
    }

    private void mergeNativeTree(Node root) throws IIOInvalidTreeException {
        Node node = root;
        if (!node.getNodeName().equals(this.nativeMetadataFormatName)) {
            TIFFImageMetadata.fatal(node, "Root must be " + this.nativeMetadataFormatName);
        }
        if ((node = node.getFirstChild()) == null || !node.getNodeName().equals("TIFFIFD")) {
            TIFFImageMetadata.fatal(root, "Root must have \"TIFFIFD\" child");
        }
        TIFFIFD ifd = TIFFImageMetadata.parseIFD(node);
        List<TIFFTagSet> rootIFDTagSets = this.rootIFD.getTagSetList();
        for (TIFFTagSet o : ifd.getTagSetList()) {
            if (!(o instanceof TIFFTagSet) || rootIFDTagSets.contains(o)) continue;
            this.rootIFD.addTagSet(o);
        }
        Iterator<TIFFField> ifdIter = ifd.iterator();
        while (ifdIter.hasNext()) {
            TIFFField field = ifdIter.next();
            this.rootIFD.addTIFFField(field);
        }
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        if (formatName.equals(this.nativeMetadataFormatName)) {
            if (root == null) {
                throw new NullPointerException("root == null!");
            }
            this.mergeNativeTree(root);
        } else if (formatName.equals("javax_imageio_1.0")) {
            if (root == null) {
                throw new NullPointerException("root == null!");
            }
            this.mergeStandardTree(root);
        } else {
            throw new IllegalArgumentException("Not a recognized format!");
        }
    }

    @Override
    public void reset() {
        this.rootIFD = new TIFFIFD(this.tagSets);
    }

    public TIFFIFD getRootIFD() {
        return this.rootIFD;
    }

    public TIFFField getTIFFField(int tagNumber) {
        return this.rootIFD.getTIFFField(tagNumber);
    }

    public void removeTIFFField(int tagNumber) {
        this.rootIFD.removeTIFFField(tagNumber);
    }

    public TIFFImageMetadata getShallowClone() {
        return new TIFFImageMetadata(this.rootIFD.getShallowClone());
    }
}

