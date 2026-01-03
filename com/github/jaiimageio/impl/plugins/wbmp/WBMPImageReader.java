/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.impl.plugins.wbmp.I18N
 *  com.github.jaiimageio.impl.plugins.wbmp.WBMPMetadata
 */
package com.github.jaiimageio.impl.plugins.wbmp;

import com.github.jaiimageio.impl.common.ImageUtil;
import com.github.jaiimageio.impl.plugins.wbmp.I18N;
import com.github.jaiimageio.impl.plugins.wbmp.WBMPMetadata;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class WBMPImageReader
extends ImageReader {
    private ImageInputStream iis = null;
    private boolean gotHeader = false;
    private long imageDataOffset;
    private int width;
    private int height;
    private int wbmpType;
    private WBMPMetadata metadata;

    public WBMPImageReader(ImageReaderSpi originator) {
        super(originator);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        this.iis = (ImageInputStream)input;
        this.gotHeader = false;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        if (this.iis == null) {
            throw new IllegalStateException(I18N.getString((String)"GetNumImages0"));
        }
        if (this.seekForwardOnly && allowSearch) {
            throw new IllegalStateException(I18N.getString((String)"GetNumImages1"));
        }
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        return this.width;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        return this.height;
    }

    @Override
    public boolean isRandomAccessEasy(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        return true;
    }

    private void checkIndex(int imageIndex) {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(I18N.getString((String)"WBMPImageReader0"));
        }
    }

    public void readHeader() throws IOException {
        if (this.gotHeader) {
            this.iis.seek(this.imageDataOffset);
            return;
        }
        if (this.iis == null) {
            throw new IllegalStateException(I18N.getString((String)"WBMPImageReader1"));
        }
        this.metadata = new WBMPMetadata();
        this.wbmpType = this.iis.readByte();
        byte fixHeaderField = this.iis.readByte();
        if (fixHeaderField != 0 || !this.isValidWbmpType(this.wbmpType)) {
            throw new IIOException(I18N.getString((String)"WBMPImageReader2"));
        }
        this.metadata.wbmpType = this.wbmpType;
        this.metadata.width = this.width = ImageUtil.readMultiByteInteger(this.iis);
        this.metadata.height = this.height = ImageUtil.readMultiByteInteger(this.iis);
        this.gotHeader = true;
        this.imageDataOffset = this.iis.getStreamPosition();
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        BufferedImage bi = new BufferedImage(1, 1, 12);
        ArrayList<ImageTypeSpecifier> list = new ArrayList<ImageTypeSpecifier>(1);
        list.add(new ImageTypeSpecifier(bi));
        return list.iterator();
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        if (this.metadata == null) {
            this.readHeader();
        }
        return this.metadata;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (this.iis == null) {
            throw new IllegalStateException(I18N.getString((String)"WBMPImageReader1"));
        }
        this.checkIndex(imageIndex);
        this.clearAbortRequest();
        this.processImageStarted(imageIndex);
        if (param == null) {
            param = this.getDefaultReadParam();
        }
        this.readHeader();
        Rectangle sourceRegion = new Rectangle(0, 0, 0, 0);
        Rectangle destinationRegion = new Rectangle(0, 0, 0, 0);
        WBMPImageReader.computeRegions(param, this.width, this.height, param.getDestination(), sourceRegion, destinationRegion);
        int scaleX = param.getSourceXSubsampling();
        int scaleY = param.getSourceYSubsampling();
        int xOffset = param.getSubsamplingXOffset();
        int yOffset = param.getSubsamplingYOffset();
        BufferedImage bi = param.getDestination();
        if (bi == null) {
            bi = new BufferedImage(destinationRegion.x + destinationRegion.width, destinationRegion.y + destinationRegion.height, 12);
        }
        boolean noTransform = destinationRegion.equals(new Rectangle(0, 0, this.width, this.height)) && destinationRegion.equals(new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
        WritableRaster tile = bi.getWritableTile(0, 0);
        MultiPixelPackedSampleModel sm = (MultiPixelPackedSampleModel)bi.getSampleModel();
        if (noTransform) {
            if (this.abortRequested()) {
                this.processReadAborted();
                return bi;
            }
            this.iis.read(((DataBufferByte)tile.getDataBuffer()).getData(), 0, this.height * sm.getScanlineStride());
            this.processImageUpdate(bi, 0, 0, this.width, this.height, 1, 1, new int[]{0});
            this.processImageProgress(100.0f);
        } else {
            int len = (this.width + 7) / 8;
            byte[] buf = new byte[len];
            byte[] data = ((DataBufferByte)tile.getDataBuffer()).getData();
            int lineStride = sm.getScanlineStride();
            this.iis.skipBytes(len * sourceRegion.y);
            int skipLength = len * (scaleY - 1);
            int[] srcOff = new int[destinationRegion.width];
            int[] destOff = new int[destinationRegion.width];
            int[] srcPos = new int[destinationRegion.width];
            int[] destPos = new int[destinationRegion.width];
            int i = destinationRegion.x;
            int x = sourceRegion.x;
            int j = 0;
            while (i < destinationRegion.x + destinationRegion.width) {
                srcPos[j] = x >> 3;
                srcOff[j] = 7 - (x & 7);
                destPos[j] = i >> 3;
                destOff[j] = 7 - (i & 7);
                ++i;
                ++j;
                x += scaleX;
            }
            int j2 = 0;
            int y = sourceRegion.y;
            int k = destinationRegion.y * lineStride;
            while (j2 < destinationRegion.height && !this.abortRequested()) {
                this.iis.read(buf, 0, len);
                for (int i2 = 0; i2 < destinationRegion.width; ++i2) {
                    int v = buf[srcPos[i2]] >> srcOff[i2] & 1;
                    int n = k + destPos[i2];
                    data[n] = (byte)(data[n] | v << destOff[i2]);
                }
                k += lineStride;
                this.iis.skipBytes(skipLength);
                this.processImageUpdate(bi, 0, j2, destinationRegion.width, 1, 1, 1, new int[]{0});
                this.processImageProgress(100.0f * (float)j2 / (float)destinationRegion.height);
                ++j2;
                y += scaleY;
            }
        }
        if (this.abortRequested()) {
            this.processReadAborted();
        } else {
            this.processImageComplete();
        }
        return bi;
    }

    @Override
    public boolean canReadRaster() {
        return true;
    }

    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param) throws IOException {
        BufferedImage bi = this.read(imageIndex, param);
        return bi.getData();
    }

    @Override
    public void reset() {
        super.reset();
        this.iis = null;
        this.gotHeader = false;
    }

    boolean isValidWbmpType(int type) {
        return type == 0;
    }
}

