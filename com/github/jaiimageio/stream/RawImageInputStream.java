/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.stream.I18N
 */
package com.github.jaiimageio.stream;

import com.github.jaiimageio.impl.common.ImageUtil;
import com.github.jaiimageio.stream.I18N;
import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.StringTokenizer;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RawImageInputStream
implements ImageInputStream {
    private static final String[] preDefinedColorSpaces = new String[]{"GRAY", "sRGB", "LINEAR_RGB", "PYCC", "CIEXYZ"};
    private static final int[] preDefinedTypes = new int[]{1003, 1000, 1004, 1002, 1001};
    private ImageInputStream source;
    private ImageTypeSpecifier type;
    private long[] imageOffsets;
    private Dimension[] imageDimensions;

    private static String getAttribute(Node node, String name) {
        NamedNodeMap map = node.getAttributes();
        return (node = map.getNamedItem(name)) != null ? node.getNodeValue() : null;
    }

    private static boolean getBoolean(Node node, String name) {
        String s = RawImageInputStream.getAttribute(node, name);
        return s == null ? false : new Boolean(s);
    }

    private static int getInt(Node node, String name) {
        String s = RawImageInputStream.getAttribute(node, name);
        return s == null ? 0 : new Integer(s);
    }

    private static byte[] getByteArray(Node node, String name) {
        String s = RawImageInputStream.getAttribute(node, name);
        if (s == null) {
            return null;
        }
        StringTokenizer token = new StringTokenizer(s);
        int count = token.countTokens();
        if (count == 0) {
            return null;
        }
        byte[] buf = new byte[count];
        int i = 0;
        while (token.hasMoreElements()) {
            buf[i++] = new Byte(token.nextToken());
        }
        return buf;
    }

    private static int[] getIntArray(Node node, String name) {
        String s = RawImageInputStream.getAttribute(node, name);
        if (s == null) {
            return null;
        }
        StringTokenizer token = new StringTokenizer(s);
        int count = token.countTokens();
        if (count == 0) {
            return null;
        }
        int[] buf = new int[count];
        int i = 0;
        while (token.hasMoreElements()) {
            buf[i++] = new Integer(token.nextToken());
        }
        return buf;
    }

    private static int getTransparency(String s) {
        if ("BITMASK".equals(s)) {
            return 2;
        }
        if ("OPAQUE".equals(s)) {
            return 1;
        }
        if ("TRANSLUCENT".equals(s)) {
            return 3;
        }
        return 0;
    }

    private static ColorSpace getColorSpace(Node node) throws IOException {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node child = nodes.item(i);
            if (!"colorSpace".equals(child.getNodeName())) continue;
            String s = child.getNodeValue();
            for (int j = 0; j < preDefinedColorSpaces.length; ++j) {
                if (!preDefinedColorSpaces[j].equals(s)) continue;
                return ColorSpace.getInstance(preDefinedTypes[j]);
            }
            InputStream stm = new URL(s).openStream();
            ICC_ColorSpace cp = new ICC_ColorSpace(ICC_Profile.getInstance(stm));
            stm.close();
            return cp;
        }
        return null;
    }

    public RawImageInputStream(ImageInputStream source, ImageTypeSpecifier type, long[] imageOffsets, Dimension[] imageDimensions) {
        if (imageOffsets == null || imageDimensions == null || imageOffsets.length != imageDimensions.length) {
            throw new IllegalArgumentException(I18N.getString((String)"RawImageInputStream0"));
        }
        this.source = source;
        this.type = type;
        this.imageOffsets = imageOffsets;
        this.imageDimensions = imageDimensions;
    }

    public RawImageInputStream(ImageInputStream source, SampleModel sampleModel, long[] imageOffsets, Dimension[] imageDimensions) {
        if (imageOffsets == null || imageDimensions == null || imageOffsets.length != imageDimensions.length) {
            throw new IllegalArgumentException(I18N.getString((String)"RawImageInputStream0"));
        }
        this.source = source;
        ColorModel colorModel = ImageUtil.createColorModel(sampleModel);
        if (colorModel == null) {
            throw new IllegalArgumentException(I18N.getString((String)"RawImageInputStream4"));
        }
        this.type = new ImageTypeSpecifier(colorModel, sampleModel);
        this.imageOffsets = imageOffsets;
        this.imageDimensions = imageDimensions;
    }

    public RawImageInputStream(ImageInputStream source, InputSource xmlSource) throws SAXException, IOException {
        Node node;
        Node node2;
        this.source = source;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        dbf.setNamespaceAware(true);
        dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException ex) {
            throw new RuntimeException(I18N.getString((String)"RawImageInputStream1"), ex);
        }
        Document doc = db.parse(xmlSource);
        NodeList nodes = doc.getElementsByTagName("byteOrder");
        String byteOrder = nodes.item(0).getNodeValue();
        if ("NETWORK".equals(byteOrder)) {
            this.setByteOrder(ByteOrder.BIG_ENDIAN);
            this.source.setByteOrder(ByteOrder.BIG_ENDIAN);
        } else if ("REVERSE".equals(byteOrder)) {
            this.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            this.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        }
        nodes = doc.getElementsByTagName("offset");
        int length = nodes.getLength();
        this.imageOffsets = new long[length];
        for (int i = 0; i < length; ++i) {
            this.imageOffsets[i] = new Long(nodes.item(i).getNodeValue());
        }
        nodes = doc.getElementsByTagName("width");
        NodeList nodes1 = doc.getElementsByTagName("height");
        length = nodes.getLength();
        if (length != nodes1.getLength()) {
            throw new IllegalArgumentException(I18N.getString((String)"RawImageInputStream2"));
        }
        this.imageDimensions = new Dimension[length];
        for (int i = 0; i < length; ++i) {
            String w = nodes.item(i).getNodeValue();
            String h = nodes1.item(i).getNodeValue();
            this.imageDimensions[i] = new Dimension(new Integer(w), new Integer(h));
        }
        SampleModel sampleModel = null;
        nodes = doc.getElementsByTagName("ComponentSampleModel");
        if (nodes.getLength() > 0) {
            node2 = nodes.item(0);
            int[] bankIndices = RawImageInputStream.getIntArray(node2, "bankIndices");
            sampleModel = bankIndices == null ? new ComponentSampleModel(RawImageInputStream.getInt(node2, "dataType"), RawImageInputStream.getInt(node2, "w"), RawImageInputStream.getInt(node2, "h"), RawImageInputStream.getInt(node2, "pixelStride"), RawImageInputStream.getInt(node2, "scanlineStride"), RawImageInputStream.getIntArray(node2, "bandOffsets")) : new ComponentSampleModel(RawImageInputStream.getInt(node2, "dataType"), RawImageInputStream.getInt(node2, "w"), RawImageInputStream.getInt(node2, "h"), RawImageInputStream.getInt(node2, "pixelStride"), RawImageInputStream.getInt(node2, "scanlineStride"), bankIndices, RawImageInputStream.getIntArray(node2, "bandOffsets"));
        }
        if ((nodes = doc.getElementsByTagName("MultiPixelPackedSampleModel")).getLength() > 0) {
            node2 = nodes.item(0);
            sampleModel = new MultiPixelPackedSampleModel(RawImageInputStream.getInt(node2, "dataType"), RawImageInputStream.getInt(node2, "w"), RawImageInputStream.getInt(node2, "h"), RawImageInputStream.getInt(node2, "numberOfBits"), RawImageInputStream.getInt(node2, "scanlineStride"), RawImageInputStream.getInt(node2, "dataBitOffset"));
        }
        if ((nodes = doc.getElementsByTagName("SinglePixelPackedSampleModel")).getLength() > 0) {
            node2 = nodes.item(0);
            sampleModel = new SinglePixelPackedSampleModel(RawImageInputStream.getInt(node2, "dataType"), RawImageInputStream.getInt(node2, "w"), RawImageInputStream.getInt(node2, "h"), RawImageInputStream.getInt(node2, "scanlineStride"), RawImageInputStream.getIntArray(node2, "bitMasks"));
        }
        ColorModel colorModel = null;
        nodes = doc.getElementsByTagName("ComponentColorModel");
        if (nodes.getLength() > 0) {
            node = nodes.item(0);
            colorModel = new ComponentColorModel(RawImageInputStream.getColorSpace(node), RawImageInputStream.getIntArray(node, "bits"), RawImageInputStream.getBoolean(node, "hasAlpha"), RawImageInputStream.getBoolean(node, "isAlphaPremultiplied"), RawImageInputStream.getTransparency(RawImageInputStream.getAttribute(node, "transparency")), RawImageInputStream.getInt(node, "transferType"));
        }
        if ((nodes = doc.getElementsByTagName("DirectColorModel")).getLength() > 0) {
            node = nodes.item(0);
            colorModel = new DirectColorModel(RawImageInputStream.getColorSpace(node), RawImageInputStream.getInt(node, "bits"), RawImageInputStream.getInt(node, "rmask"), RawImageInputStream.getInt(node, "gmask"), RawImageInputStream.getInt(node, "bmask"), RawImageInputStream.getInt(node, "amask"), false, 1);
        }
        if ((nodes = doc.getElementsByTagName("IndexColorModel")).getLength() > 0) {
            node = nodes.item(0);
            byte[] alpha = RawImageInputStream.getByteArray(node, "a");
            colorModel = alpha == null ? new IndexColorModel(RawImageInputStream.getInt(node, "bits"), RawImageInputStream.getInt(node, "size"), RawImageInputStream.getByteArray(node, "r"), RawImageInputStream.getByteArray(node, "g"), RawImageInputStream.getByteArray(node, "b")) : new IndexColorModel(RawImageInputStream.getInt(node, "bits"), RawImageInputStream.getInt(node, "size"), RawImageInputStream.getByteArray(node, "r"), RawImageInputStream.getByteArray(node, "g"), RawImageInputStream.getByteArray(node, "b"), alpha);
        }
        this.type = new ImageTypeSpecifier(colorModel, sampleModel);
        if (this.imageDimensions.length == 0) {
            this.imageDimensions = new Dimension[this.imageOffsets.length];
            this.imageDimensions[0] = new Dimension(sampleModel.getWidth(), sampleModel.getHeight());
            for (int i = 1; i < this.imageDimensions.length; ++i) {
                this.imageDimensions[i] = this.imageDimensions[0];
            }
        }
    }

    public ImageTypeSpecifier getImageType() {
        return this.type;
    }

    public long getImageOffset(int imageIndex) {
        if (imageIndex < 0 || imageIndex >= this.imageOffsets.length) {
            throw new IllegalArgumentException(I18N.getString((String)"RawImageInputStream3"));
        }
        return this.imageOffsets[imageIndex];
    }

    public Dimension getImageDimension(int imageIndex) {
        if (imageIndex < 0 || imageIndex >= this.imageOffsets.length) {
            throw new IllegalArgumentException(I18N.getString((String)"RawImageInputStream3"));
        }
        return this.imageDimensions[imageIndex];
    }

    public int getNumImages() {
        return this.imageOffsets.length;
    }

    @Override
    public void setByteOrder(ByteOrder byteOrder) {
        this.source.setByteOrder(byteOrder);
    }

    @Override
    public ByteOrder getByteOrder() {
        return this.source.getByteOrder();
    }

    @Override
    public int read() throws IOException {
        return this.source.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.source.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.source.read(b, off, len);
    }

    @Override
    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        this.source.readBytes(buf, len);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return this.source.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return this.source.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return this.source.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return this.source.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return this.source.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return this.source.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return this.source.readInt();
    }

    @Override
    public long readUnsignedInt() throws IOException {
        return this.source.readUnsignedInt();
    }

    @Override
    public long readLong() throws IOException {
        return this.source.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return this.source.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return this.source.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return this.source.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return this.source.readUTF();
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        this.source.readFully(b, off, len);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        this.source.readFully(b);
    }

    @Override
    public void readFully(short[] s, int off, int len) throws IOException {
        this.source.readFully(s, off, len);
    }

    @Override
    public void readFully(char[] c, int off, int len) throws IOException {
        this.source.readFully(c, off, len);
    }

    @Override
    public void readFully(int[] i, int off, int len) throws IOException {
        this.source.readFully(i, off, len);
    }

    @Override
    public void readFully(long[] l, int off, int len) throws IOException {
        this.source.readFully(l, off, len);
    }

    @Override
    public void readFully(float[] f, int off, int len) throws IOException {
        this.source.readFully(f, off, len);
    }

    @Override
    public void readFully(double[] d, int off, int len) throws IOException {
        this.source.readFully(d, off, len);
    }

    @Override
    public long getStreamPosition() throws IOException {
        return this.source.getStreamPosition();
    }

    @Override
    public int getBitOffset() throws IOException {
        return this.source.getBitOffset();
    }

    @Override
    public void setBitOffset(int bitOffset) throws IOException {
        this.source.setBitOffset(bitOffset);
    }

    @Override
    public int readBit() throws IOException {
        return this.source.readBit();
    }

    @Override
    public long readBits(int numBits) throws IOException {
        return this.source.readBits(numBits);
    }

    @Override
    public long length() throws IOException {
        return this.source.length();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return this.source.skipBytes(n);
    }

    @Override
    public long skipBytes(long n) throws IOException {
        return this.source.skipBytes(n);
    }

    @Override
    public void seek(long pos) throws IOException {
        this.source.seek(pos);
    }

    @Override
    public void mark() {
        this.source.mark();
    }

    @Override
    public void reset() throws IOException {
        this.source.reset();
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        this.source.flushBefore(pos);
    }

    @Override
    public void flush() throws IOException {
        this.source.flush();
    }

    @Override
    public long getFlushedPosition() {
        return this.source.getFlushedPosition();
    }

    @Override
    public boolean isCached() {
        return this.source.isCached();
    }

    @Override
    public boolean isCachedMemory() {
        return this.source.isCachedMemory();
    }

    @Override
    public boolean isCachedFile() {
        return this.source.isCachedFile();
    }

    @Override
    public void close() throws IOException {
        this.source.close();
    }
}

