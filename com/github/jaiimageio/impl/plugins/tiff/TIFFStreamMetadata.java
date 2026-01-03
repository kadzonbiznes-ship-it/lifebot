/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.impl.plugins.tiff;

import java.nio.ByteOrder;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TIFFStreamMetadata
extends IIOMetadata {
    static final String nativeMetadataFormatName = "com_sun_media_imageio_plugins_tiff_stream_1.0";
    static final String nativeMetadataFormatClassName = "com.github.jaiimageio.impl.plugins.tiff.TIFFStreamMetadataFormat";
    private static final String bigEndianString = ByteOrder.BIG_ENDIAN.toString();
    private static final String littleEndianString = ByteOrder.LITTLE_ENDIAN.toString();
    public ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    public TIFFStreamMetadata() {
        super(false, nativeMetadataFormatName, nativeMetadataFormatClassName, null, null);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    private static void fatal(Node node, String reason) throws IIOInvalidTreeException {
        throw new IIOInvalidTreeException(reason, node);
    }

    @Override
    public Node getAsTree(String formatName) {
        IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);
        IIOMetadataNode byteOrderNode = new IIOMetadataNode("ByteOrder");
        byteOrderNode.setAttribute("value", this.byteOrder.toString());
        root.appendChild(byteOrderNode);
        return root;
    }

    private void mergeNativeTree(Node root) throws IIOInvalidTreeException {
        NamedNodeMap attrs;
        String order;
        Node node = root;
        if (!node.getNodeName().equals(nativeMetadataFormatName)) {
            TIFFStreamMetadata.fatal(node, "Root must be com_sun_media_imageio_plugins_tiff_stream_1.0");
        }
        if ((node = node.getFirstChild()) == null || !node.getNodeName().equals("ByteOrder")) {
            TIFFStreamMetadata.fatal(node, "Root must have \"ByteOrder\" child");
        }
        if ((order = (attrs = node.getAttributes()).getNamedItem("value").getNodeValue()) == null) {
            TIFFStreamMetadata.fatal(node, "ByteOrder node must have a \"value\" attribute");
        }
        if (order.equals(bigEndianString)) {
            this.byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (order.equals(littleEndianString)) {
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            TIFFStreamMetadata.fatal(node, "Incorrect value for ByteOrder \"value\" attribute");
        }
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        if (formatName.equals(nativeMetadataFormatName)) {
            if (root == null) {
                throw new IllegalArgumentException("root == null!");
            }
        } else {
            throw new IllegalArgumentException("Not a recognized format!");
        }
        this.mergeNativeTree(root);
    }

    @Override
    public void reset() {
        this.byteOrder = ByteOrder.BIG_ENDIAN;
    }
}

