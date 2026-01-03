/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.ImageCapabilities;
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.ReplicateScaleFilter;
import sun.awt.image.SurfaceManager;

public abstract class Image {
    private static ImageCapabilities defaultImageCaps = new ImageCapabilities(false);
    protected float accelerationPriority = 0.5f;
    public static final Object UndefinedProperty = new Object();
    public static final int SCALE_DEFAULT = 1;
    public static final int SCALE_FAST = 2;
    public static final int SCALE_SMOOTH = 4;
    public static final int SCALE_REPLICATE = 8;
    public static final int SCALE_AREA_AVERAGING = 16;
    SurfaceManager surfaceManager;

    protected Image() {
    }

    public abstract int getWidth(ImageObserver var1);

    public abstract int getHeight(ImageObserver var1);

    public abstract ImageProducer getSource();

    public abstract Graphics getGraphics();

    public abstract Object getProperty(String var1, ImageObserver var2);

    public Image getScaledInstance(int width, int height, int hints) {
        ReplicateScaleFilter filter = (hints & 0x14) != 0 ? new AreaAveragingScaleFilter(width, height) : new ReplicateScaleFilter(width, height);
        FilteredImageSource prod = new FilteredImageSource(this.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(prod);
    }

    public void flush() {
        if (this.surfaceManager != null) {
            this.surfaceManager.flush();
        }
    }

    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        if (this.surfaceManager != null) {
            return this.surfaceManager.getCapabilities(gc);
        }
        return defaultImageCaps;
    }

    public void setAccelerationPriority(float priority) {
        if (priority < 0.0f || priority > 1.0f) {
            throw new IllegalArgumentException("Priority must be a value between 0 and 1, inclusive");
        }
        this.accelerationPriority = priority;
        if (this.surfaceManager != null) {
            this.surfaceManager.setAccelerationPriority(this.accelerationPriority);
        }
    }

    public float getAccelerationPriority() {
        return this.accelerationPriority;
    }

    static {
        SurfaceManager.setImageAccessor(new SurfaceManager.ImageAccessor(){

            @Override
            public SurfaceManager getSurfaceManager(Image img) {
                return img.surfaceManager;
            }

            @Override
            public void setSurfaceManager(Image img, SurfaceManager mgr) {
                img.surfaceManager = mgr;
            }
        });
    }
}

