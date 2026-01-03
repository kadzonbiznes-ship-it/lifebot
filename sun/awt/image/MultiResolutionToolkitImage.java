/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.MultiResolutionImage;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import sun.awt.image.ToolkitImage;

public class MultiResolutionToolkitImage
extends ToolkitImage
implements MultiResolutionImage {
    Image resolutionVariant;
    private static final int BITS_INFO = 56;

    public MultiResolutionToolkitImage(Image lowResolutionImage, Image resolutionVariant) {
        super(lowResolutionImage.getSource());
        this.resolutionVariant = resolutionVariant;
    }

    @Override
    public Image getResolutionVariant(double destWidth, double destHeight) {
        MultiResolutionToolkitImage.checkSize(destWidth, destHeight);
        return destWidth <= (double)this.getWidth() && destHeight <= (double)this.getHeight() ? this : this.resolutionVariant;
    }

    public static Image map(MultiResolutionToolkitImage mrImage, Function<Image, Image> mapper) {
        Image baseImage = mapper.apply(mrImage);
        Image rvImage = mapper.apply(mrImage.resolutionVariant);
        return new MultiResolutionToolkitImage(baseImage, rvImage);
    }

    private static void checkSize(double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException(String.format("Width (%s) or height (%s) cannot be <= 0", width, height));
        }
        if (!Double.isFinite(width) || !Double.isFinite(height)) {
            throw new IllegalArgumentException(String.format("Width (%s) or height (%s) is not finite", width, height));
        }
    }

    public Image getResolutionVariant() {
        return this.resolutionVariant;
    }

    @Override
    public List<Image> getResolutionVariants() {
        return Arrays.asList(this, this.resolutionVariant);
    }

    public static ImageObserver getResolutionVariantObserver(Image image, ImageObserver observer, int imgWidth, int imgHeight, int rvWidth, int rvHeight) {
        return MultiResolutionToolkitImage.getResolutionVariantObserver(image, observer, imgWidth, imgHeight, rvWidth, rvHeight, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ImageObserver getResolutionVariantObserver(Image image, ImageObserver observer, int imgWidth, int imgHeight, int rvWidth, int rvHeight, boolean concatenateInfo) {
        if (observer == null) {
            return null;
        }
        Map<ImageObserver, ImageObserver> map = ObserverCache.INSTANCE;
        synchronized (map) {
            return ObserverCache.INSTANCE.computeIfAbsent(observer, key -> new ObserverCache((ImageObserver)key, concatenateInfo, image));
        }
    }

    private static final class ObserverCache
    implements ImageObserver {
        private static final Map<ImageObserver, ImageObserver> INSTANCE = new WeakHashMap<ImageObserver, ImageObserver>();
        private final boolean concat;
        private final WeakReference<Image> imageRef;
        private final WeakReference<ImageObserver> observerRef;

        private ObserverCache(ImageObserver obs, boolean concat, Image img) {
            this.concat = concat;
            this.imageRef = new WeakReference<Image>(img);
            this.observerRef = new WeakReference<ImageObserver>(obs);
        }

        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            ImageObserver observer = (ImageObserver)this.observerRef.get();
            Image image = (Image)this.imageRef.get();
            if (observer == null || image == null) {
                return false;
            }
            if ((infoflags & 0x39) != 0) {
                width = (width + 1) / 2;
            }
            if ((infoflags & 0x3A) != 0) {
                height = (height + 1) / 2;
            }
            if ((infoflags & 0x38) != 0) {
                x /= 2;
                y /= 2;
            }
            if (this.concat) {
                infoflags &= ((ToolkitImage)image).getImageRep().check(null);
            }
            return observer.imageUpdate(image, infoflags, x, y, width, height);
        }
    }
}

