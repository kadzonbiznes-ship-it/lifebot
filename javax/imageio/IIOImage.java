/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.List;
import javax.imageio.metadata.IIOMetadata;

public class IIOImage {
    protected RenderedImage image;
    protected Raster raster;
    protected List<? extends BufferedImage> thumbnails = null;
    protected IIOMetadata metadata;

    public IIOImage(RenderedImage image, List<? extends BufferedImage> thumbnails, IIOMetadata metadata) {
        if (image == null) {
            throw new IllegalArgumentException("image == null!");
        }
        this.image = image;
        this.raster = null;
        this.thumbnails = thumbnails;
        this.metadata = metadata;
    }

    public IIOImage(Raster raster, List<? extends BufferedImage> thumbnails, IIOMetadata metadata) {
        if (raster == null) {
            throw new IllegalArgumentException("raster == null!");
        }
        this.raster = raster;
        this.image = null;
        this.thumbnails = thumbnails;
        this.metadata = metadata;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public RenderedImage getRenderedImage() {
        IIOImage iIOImage = this;
        synchronized (iIOImage) {
            return this.image;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setRenderedImage(RenderedImage image) {
        IIOImage iIOImage = this;
        synchronized (iIOImage) {
            if (image == null) {
                throw new IllegalArgumentException("image == null!");
            }
            this.image = image;
            this.raster = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasRaster() {
        IIOImage iIOImage = this;
        synchronized (iIOImage) {
            return this.raster != null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Raster getRaster() {
        IIOImage iIOImage = this;
        synchronized (iIOImage) {
            return this.raster;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setRaster(Raster raster) {
        IIOImage iIOImage = this;
        synchronized (iIOImage) {
            if (raster == null) {
                throw new IllegalArgumentException("raster == null!");
            }
            this.raster = raster;
            this.image = null;
        }
    }

    public int getNumThumbnails() {
        return this.thumbnails == null ? 0 : this.thumbnails.size();
    }

    public BufferedImage getThumbnail(int index) {
        if (this.thumbnails == null) {
            throw new IndexOutOfBoundsException("No thumbnails available!");
        }
        return this.thumbnails.get(index);
    }

    public List<? extends BufferedImage> getThumbnails() {
        return this.thumbnails;
    }

    public void setThumbnails(List<? extends BufferedImage> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public IIOMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(IIOMetadata metadata) {
        this.metadata = metadata;
    }
}

