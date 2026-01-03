/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public abstract class ImageReader {
    protected ImageReaderSpi originatingProvider;
    protected Object input = null;
    protected boolean seekForwardOnly = false;
    protected boolean ignoreMetadata = false;
    protected int minIndex = 0;
    protected Locale[] availableLocales = null;
    protected Locale locale = null;
    protected List<IIOReadWarningListener> warningListeners = null;
    protected List<Locale> warningLocales = null;
    protected List<IIOReadProgressListener> progressListeners = null;
    protected List<IIOReadUpdateListener> updateListeners = null;
    private boolean abortFlag = false;

    protected ImageReader(ImageReaderSpi originatingProvider) {
        this.originatingProvider = originatingProvider;
    }

    public String getFormatName() throws IOException {
        return this.originatingProvider.getFormatNames()[0];
    }

    public ImageReaderSpi getOriginatingProvider() {
        return this.originatingProvider;
    }

    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        if (input != null) {
            boolean found = false;
            if (this.originatingProvider != null) {
                Class<?>[] classes = this.originatingProvider.getInputTypes();
                for (int i = 0; i < classes.length; ++i) {
                    if (!classes[i].isInstance(input)) continue;
                    found = true;
                    break;
                }
            } else if (input instanceof ImageInputStream) {
                found = true;
            }
            if (!found) {
                throw new IllegalArgumentException("Incorrect input type!");
            }
            this.seekForwardOnly = seekForwardOnly;
            this.ignoreMetadata = ignoreMetadata;
            this.minIndex = 0;
        }
        this.input = input;
    }

    public void setInput(Object input, boolean seekForwardOnly) {
        this.setInput(input, seekForwardOnly, false);
    }

    public void setInput(Object input) {
        this.setInput(input, false, false);
    }

    public Object getInput() {
        return this.input;
    }

    public boolean isSeekForwardOnly() {
        return this.seekForwardOnly;
    }

    public boolean isIgnoringMetadata() {
        return this.ignoreMetadata;
    }

    public int getMinIndex() {
        return this.minIndex;
    }

    public Locale[] getAvailableLocales() {
        if (this.availableLocales == null) {
            return null;
        }
        return (Locale[])this.availableLocales.clone();
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            Locale[] locales = this.getAvailableLocales();
            boolean found = false;
            if (locales != null) {
                for (int i = 0; i < locales.length; ++i) {
                    if (!locale.equals(locales[i])) continue;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Invalid locale!");
            }
        }
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public abstract int getNumImages(boolean var1) throws IOException;

    public abstract int getWidth(int var1) throws IOException;

    public abstract int getHeight(int var1) throws IOException;

    public boolean isRandomAccessEasy(int imageIndex) throws IOException {
        return false;
    }

    public float getAspectRatio(int imageIndex) throws IOException {
        return (float)this.getWidth(imageIndex) / (float)this.getHeight(imageIndex);
    }

    public ImageTypeSpecifier getRawImageType(int imageIndex) throws IOException {
        return this.getImageTypes(imageIndex).next();
    }

    public abstract Iterator<ImageTypeSpecifier> getImageTypes(int var1) throws IOException;

    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    public abstract IIOMetadata getStreamMetadata() throws IOException;

    public IIOMetadata getStreamMetadata(String formatName, Set<String> nodeNames) throws IOException {
        return this.getMetadata(formatName, nodeNames, true, 0);
    }

    private IIOMetadata getMetadata(String formatName, Set<String> nodeNames, boolean wantStream, int imageIndex) throws IOException {
        IIOMetadata metadata;
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (nodeNames == null) {
            throw new IllegalArgumentException("nodeNames == null!");
        }
        IIOMetadata iIOMetadata = metadata = wantStream ? this.getStreamMetadata() : this.getImageMetadata(imageIndex);
        if (metadata != null) {
            if (metadata.isStandardMetadataFormatSupported() && formatName.equals("javax_imageio_1.0")) {
                return metadata;
            }
            String nativeName = metadata.getNativeMetadataFormatName();
            if (nativeName != null && formatName.equals(nativeName)) {
                return metadata;
            }
            String[] extraNames = metadata.getExtraMetadataFormatNames();
            if (extraNames != null) {
                for (int i = 0; i < extraNames.length; ++i) {
                    if (!formatName.equals(extraNames[i])) continue;
                    return metadata;
                }
            }
        }
        return null;
    }

    public abstract IIOMetadata getImageMetadata(int var1) throws IOException;

    public IIOMetadata getImageMetadata(int imageIndex, String formatName, Set<String> nodeNames) throws IOException {
        return this.getMetadata(formatName, nodeNames, false, imageIndex);
    }

    public BufferedImage read(int imageIndex) throws IOException {
        return this.read(imageIndex, null);
    }

    public abstract BufferedImage read(int var1, ImageReadParam var2) throws IOException;

    public IIOImage readAll(int imageIndex, ImageReadParam param) throws IOException {
        if (imageIndex < this.getMinIndex()) {
            throw new IndexOutOfBoundsException("imageIndex < getMinIndex()!");
        }
        BufferedImage im = this.read(imageIndex, param);
        ArrayList<BufferedImage> thumbnails = null;
        int numThumbnails = this.getNumThumbnails(imageIndex);
        if (numThumbnails > 0) {
            thumbnails = new ArrayList<BufferedImage>();
            for (int j = 0; j < numThumbnails; ++j) {
                thumbnails.add(this.readThumbnail(imageIndex, j));
            }
        }
        IIOMetadata metadata = this.getImageMetadata(imageIndex);
        return new IIOImage(im, thumbnails, metadata);
    }

    public Iterator<IIOImage> readAll(Iterator<? extends ImageReadParam> params) throws IOException {
        ArrayList<IIOImage> output = new ArrayList<IIOImage>();
        int imageIndex = this.getMinIndex();
        this.processSequenceStarted(imageIndex);
        while (true) {
            ImageReadParam o;
            ImageReadParam param = null;
            if (params != null && params.hasNext() && (o = params.next()) != null) {
                if (o instanceof ImageReadParam) {
                    param = o;
                } else {
                    throw new IllegalArgumentException("Non-ImageReadParam supplied as part of params!");
                }
            }
            BufferedImage bi = null;
            try {
                bi = this.read(imageIndex, param);
            }
            catch (IndexOutOfBoundsException e) {
                break;
            }
            ArrayList<BufferedImage> thumbnails = null;
            int numThumbnails = this.getNumThumbnails(imageIndex);
            if (numThumbnails > 0) {
                thumbnails = new ArrayList<BufferedImage>();
                for (int j = 0; j < numThumbnails; ++j) {
                    thumbnails.add(this.readThumbnail(imageIndex, j));
                }
            }
            IIOMetadata metadata = this.getImageMetadata(imageIndex);
            IIOImage im = new IIOImage(bi, thumbnails, metadata);
            output.add(im);
            ++imageIndex;
        }
        this.processSequenceComplete();
        return output.iterator();
    }

    public boolean canReadRaster() {
        return false;
    }

    public Raster readRaster(int imageIndex, ImageReadParam param) throws IOException {
        throw new UnsupportedOperationException("readRaster not supported!");
    }

    public boolean isImageTiled(int imageIndex) throws IOException {
        return false;
    }

    public int getTileWidth(int imageIndex) throws IOException {
        return this.getWidth(imageIndex);
    }

    public int getTileHeight(int imageIndex) throws IOException {
        return this.getHeight(imageIndex);
    }

    public int getTileGridXOffset(int imageIndex) throws IOException {
        return 0;
    }

    public int getTileGridYOffset(int imageIndex) throws IOException {
        return 0;
    }

    public BufferedImage readTile(int imageIndex, int tileX, int tileY) throws IOException {
        if (tileX != 0 || tileY != 0) {
            throw new IllegalArgumentException("Invalid tile indices");
        }
        return this.read(imageIndex);
    }

    public Raster readTileRaster(int imageIndex, int tileX, int tileY) throws IOException {
        if (!this.canReadRaster()) {
            throw new UnsupportedOperationException("readTileRaster not supported!");
        }
        if (tileX != 0 || tileY != 0) {
            throw new IllegalArgumentException("Invalid tile indices");
        }
        return this.readRaster(imageIndex, null);
    }

    public RenderedImage readAsRenderedImage(int imageIndex, ImageReadParam param) throws IOException {
        return this.read(imageIndex, param);
    }

    public boolean readerSupportsThumbnails() {
        return false;
    }

    public boolean hasThumbnails(int imageIndex) throws IOException {
        return this.getNumThumbnails(imageIndex) > 0;
    }

    public int getNumThumbnails(int imageIndex) throws IOException {
        return 0;
    }

    public int getThumbnailWidth(int imageIndex, int thumbnailIndex) throws IOException {
        return this.readThumbnail(imageIndex, thumbnailIndex).getWidth();
    }

    public int getThumbnailHeight(int imageIndex, int thumbnailIndex) throws IOException {
        return this.readThumbnail(imageIndex, thumbnailIndex).getHeight();
    }

    public BufferedImage readThumbnail(int imageIndex, int thumbnailIndex) throws IOException {
        throw new UnsupportedOperationException("Thumbnails not supported!");
    }

    public synchronized void abort() {
        this.abortFlag = true;
    }

    protected synchronized boolean abortRequested() {
        return this.abortFlag;
    }

    protected synchronized void clearAbortRequest() {
        this.abortFlag = false;
    }

    static <T> List<T> addToList(List<T> l, T elt) {
        if (l == null) {
            l = new ArrayList<T>();
        }
        l.add(elt);
        return l;
    }

    static <T> List<T> removeFromList(List<T> l, T elt) {
        if (l == null) {
            return l;
        }
        l.remove(elt);
        if (l.size() == 0) {
            l = null;
        }
        return l;
    }

    public void addIIOReadWarningListener(IIOReadWarningListener listener) {
        if (listener == null) {
            return;
        }
        this.warningListeners = ImageReader.addToList(this.warningListeners, listener);
        this.warningLocales = ImageReader.addToList(this.warningLocales, this.getLocale());
    }

    public void removeIIOReadWarningListener(IIOReadWarningListener listener) {
        if (listener == null || this.warningListeners == null) {
            return;
        }
        int index = this.warningListeners.indexOf(listener);
        if (index != -1) {
            this.warningListeners.remove(index);
            this.warningLocales.remove(index);
            if (this.warningListeners.size() == 0) {
                this.warningListeners = null;
                this.warningLocales = null;
            }
        }
    }

    public void removeAllIIOReadWarningListeners() {
        this.warningListeners = null;
        this.warningLocales = null;
    }

    public void addIIOReadProgressListener(IIOReadProgressListener listener) {
        if (listener == null) {
            return;
        }
        this.progressListeners = ImageReader.addToList(this.progressListeners, listener);
    }

    public void removeIIOReadProgressListener(IIOReadProgressListener listener) {
        if (listener == null || this.progressListeners == null) {
            return;
        }
        this.progressListeners = ImageReader.removeFromList(this.progressListeners, listener);
    }

    public void removeAllIIOReadProgressListeners() {
        this.progressListeners = null;
    }

    public void addIIOReadUpdateListener(IIOReadUpdateListener listener) {
        if (listener == null) {
            return;
        }
        this.updateListeners = ImageReader.addToList(this.updateListeners, listener);
    }

    public void removeIIOReadUpdateListener(IIOReadUpdateListener listener) {
        if (listener == null || this.updateListeners == null) {
            return;
        }
        this.updateListeners = ImageReader.removeFromList(this.updateListeners, listener);
    }

    public void removeAllIIOReadUpdateListeners() {
        this.updateListeners = null;
    }

    protected void processSequenceStarted(int minIndex) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.sequenceStarted(this, minIndex);
        }
    }

    protected void processSequenceComplete() {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.sequenceComplete(this);
        }
    }

    protected void processImageStarted(int imageIndex) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.imageStarted(this, imageIndex);
        }
    }

    protected void processImageProgress(float percentageDone) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.imageProgress(this, percentageDone);
        }
    }

    protected void processImageComplete() {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.imageComplete(this);
        }
    }

    protected void processThumbnailStarted(int imageIndex, int thumbnailIndex) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.thumbnailStarted(this, imageIndex, thumbnailIndex);
        }
    }

    protected void processThumbnailProgress(float percentageDone) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.thumbnailProgress(this, percentageDone);
        }
    }

    protected void processThumbnailComplete() {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.thumbnailComplete(this);
        }
    }

    protected void processReadAborted() {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadProgressListener listener = this.progressListeners.get(i);
            listener.readAborted(this);
        }
    }

    protected void processPassStarted(BufferedImage theImage, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
        if (this.updateListeners == null) {
            return;
        }
        int numListeners = this.updateListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadUpdateListener listener = this.updateListeners.get(i);
            listener.passStarted(this, theImage, pass, minPass, maxPass, minX, minY, periodX, periodY, bands);
        }
    }

    protected void processImageUpdate(BufferedImage theImage, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
        if (this.updateListeners == null) {
            return;
        }
        int numListeners = this.updateListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadUpdateListener listener = this.updateListeners.get(i);
            listener.imageUpdate(this, theImage, minX, minY, width, height, periodX, periodY, bands);
        }
    }

    protected void processPassComplete(BufferedImage theImage) {
        if (this.updateListeners == null) {
            return;
        }
        int numListeners = this.updateListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadUpdateListener listener = this.updateListeners.get(i);
            listener.passComplete(this, theImage);
        }
    }

    protected void processThumbnailPassStarted(BufferedImage theThumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
        if (this.updateListeners == null) {
            return;
        }
        int numListeners = this.updateListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadUpdateListener listener = this.updateListeners.get(i);
            listener.thumbnailPassStarted(this, theThumbnail, pass, minPass, maxPass, minX, minY, periodX, periodY, bands);
        }
    }

    protected void processThumbnailUpdate(BufferedImage theThumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
        if (this.updateListeners == null) {
            return;
        }
        int numListeners = this.updateListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadUpdateListener listener = this.updateListeners.get(i);
            listener.thumbnailUpdate(this, theThumbnail, minX, minY, width, height, periodX, periodY, bands);
        }
    }

    protected void processThumbnailPassComplete(BufferedImage theThumbnail) {
        if (this.updateListeners == null) {
            return;
        }
        int numListeners = this.updateListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadUpdateListener listener = this.updateListeners.get(i);
            listener.thumbnailPassComplete(this, theThumbnail);
        }
    }

    protected void processWarningOccurred(String warning) {
        if (this.warningListeners == null) {
            return;
        }
        if (warning == null) {
            throw new IllegalArgumentException("warning == null!");
        }
        int numListeners = this.warningListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadWarningListener listener = this.warningListeners.get(i);
            listener.warningOccurred(this, warning);
        }
    }

    protected void processWarningOccurred(String baseName, String keyword) {
        if (this.warningListeners == null) {
            return;
        }
        if (baseName == null) {
            throw new IllegalArgumentException("baseName == null!");
        }
        if (keyword == null) {
            throw new IllegalArgumentException("keyword == null!");
        }
        int numListeners = this.warningListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOReadWarningListener listener = this.warningListeners.get(i);
            Locale locale = this.warningLocales.get(i);
            if (locale == null) {
                locale = Locale.getDefault();
            }
            ResourceBundle bundle = null;
            try {
                bundle = ResourceBundle.getBundle(baseName, locale, this.getClass().getModule());
            }
            catch (MissingResourceException mre) {
                throw new IllegalArgumentException("Bundle not found!", mre);
            }
            String warning = null;
            try {
                warning = bundle.getString(keyword);
            }
            catch (ClassCastException cce) {
                throw new IllegalArgumentException("Resource is not a String!", cce);
            }
            catch (MissingResourceException mre) {
                throw new IllegalArgumentException("Resource is missing!", mre);
            }
            listener.warningOccurred(this, warning);
        }
    }

    public void reset() {
        this.setInput(null, false, false);
        this.setLocale(null);
        this.removeAllIIOReadUpdateListeners();
        this.removeAllIIOReadProgressListeners();
        this.removeAllIIOReadWarningListeners();
        this.clearAbortRequest();
    }

    public void dispose() {
    }

    protected static Rectangle getSourceRegion(ImageReadParam param, int srcWidth, int srcHeight) {
        Rectangle sourceRegion = new Rectangle(0, 0, srcWidth, srcHeight);
        if (param != null) {
            Rectangle region = param.getSourceRegion();
            if (region != null) {
                sourceRegion = sourceRegion.intersection(region);
            }
            int subsampleXOffset = param.getSubsamplingXOffset();
            int subsampleYOffset = param.getSubsamplingYOffset();
            sourceRegion.x += subsampleXOffset;
            sourceRegion.y += subsampleYOffset;
            sourceRegion.width -= subsampleXOffset;
            sourceRegion.height -= subsampleYOffset;
        }
        return sourceRegion;
    }

    protected static void computeRegions(ImageReadParam param, int srcWidth, int srcHeight, BufferedImage image, Rectangle srcRegion, Rectangle destRegion) {
        if (srcRegion == null) {
            throw new IllegalArgumentException("srcRegion == null!");
        }
        if (destRegion == null) {
            throw new IllegalArgumentException("destRegion == null!");
        }
        srcRegion.setBounds(0, 0, srcWidth, srcHeight);
        destRegion.setBounds(0, 0, srcWidth, srcHeight);
        int periodX = 1;
        int periodY = 1;
        int gridX = 0;
        int gridY = 0;
        if (param != null) {
            Rectangle paramSrcRegion = param.getSourceRegion();
            if (paramSrcRegion != null) {
                srcRegion.setBounds(srcRegion.intersection(paramSrcRegion));
            }
            periodX = param.getSourceXSubsampling();
            periodY = param.getSourceYSubsampling();
            gridX = param.getSubsamplingXOffset();
            gridY = param.getSubsamplingYOffset();
            srcRegion.translate(gridX, gridY);
            srcRegion.width -= gridX;
            srcRegion.height -= gridY;
            destRegion.setLocation(param.getDestinationOffset());
        }
        if (destRegion.x < 0) {
            int delta = -destRegion.x * periodX;
            srcRegion.x += delta;
            srcRegion.width -= delta;
            destRegion.x = 0;
        }
        if (destRegion.y < 0) {
            int delta = -destRegion.y * periodY;
            srcRegion.y += delta;
            srcRegion.height -= delta;
            destRegion.y = 0;
        }
        int subsampledWidth = (srcRegion.width + periodX - 1) / periodX;
        int subsampledHeight = (srcRegion.height + periodY - 1) / periodY;
        destRegion.width = subsampledWidth;
        destRegion.height = subsampledHeight;
        if (image != null) {
            int deltaY;
            Rectangle destImageRect = new Rectangle(0, 0, image.getWidth(), image.getHeight());
            destRegion.setBounds(destRegion.intersection(destImageRect));
            if (destRegion.isEmpty()) {
                throw new IllegalArgumentException("Empty destination region!");
            }
            int deltaX = destRegion.x + subsampledWidth - image.getWidth();
            if (deltaX > 0) {
                srcRegion.width -= deltaX * periodX;
            }
            if ((deltaY = destRegion.y + subsampledHeight - image.getHeight()) > 0) {
                srcRegion.height -= deltaY * periodY;
            }
        }
        if (srcRegion.isEmpty() || destRegion.isEmpty()) {
            throw new IllegalArgumentException("Empty region!");
        }
    }

    protected static void checkReadParamBandSettings(ImageReadParam param, int numSrcBands, int numDstBands) {
        int i;
        int paramDstBandLength;
        int[] srcBands = null;
        int[] dstBands = null;
        if (param != null) {
            srcBands = param.getSourceBands();
            dstBands = param.getDestinationBands();
        }
        int paramSrcBandLength = srcBands == null ? numSrcBands : srcBands.length;
        int n = paramDstBandLength = dstBands == null ? numDstBands : dstBands.length;
        if (paramSrcBandLength != paramDstBandLength) {
            throw new IllegalArgumentException("ImageReadParam num source & dest bands differ!");
        }
        if (srcBands != null) {
            for (i = 0; i < srcBands.length; ++i) {
                if (srcBands[i] < numSrcBands) continue;
                throw new IllegalArgumentException("ImageReadParam source bands contains a value >= the number of source bands!");
            }
        }
        if (dstBands != null) {
            for (i = 0; i < dstBands.length; ++i) {
                if (dstBands[i] < numDstBands) continue;
                throw new IllegalArgumentException("ImageReadParam dest bands contains a value >= the number of dest bands!");
            }
        }
    }

    protected static BufferedImage getDestination(ImageReadParam param, Iterator<ImageTypeSpecifier> imageTypes, int width, int height) throws IIOException {
        if (imageTypes == null || !imageTypes.hasNext()) {
            throw new IllegalArgumentException("imageTypes null or empty!");
        }
        if ((long)width * (long)height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("width*height > Integer.MAX_VALUE!");
        }
        BufferedImage dest = null;
        ImageTypeSpecifier imageType = null;
        if (param != null) {
            dest = param.getDestination();
            if (dest != null) {
                return dest;
            }
            imageType = param.getDestinationType();
        }
        if (imageType == null) {
            ImageTypeSpecifier o = imageTypes.next();
            if (!(o instanceof ImageTypeSpecifier)) {
                throw new IllegalArgumentException("Non-ImageTypeSpecifier retrieved from imageTypes!");
            }
            imageType = o;
        } else {
            boolean foundIt = false;
            while (imageTypes.hasNext()) {
                ImageTypeSpecifier type = imageTypes.next();
                if (!type.equals(imageType)) continue;
                foundIt = true;
                break;
            }
            if (!foundIt) {
                throw new IIOException("Destination type from ImageReadParam does not match!");
            }
        }
        Rectangle srcRegion = new Rectangle(0, 0, 0, 0);
        Rectangle destRegion = new Rectangle(0, 0, 0, 0);
        ImageReader.computeRegions(param, width, height, null, srcRegion, destRegion);
        int destWidth = destRegion.x + destRegion.width;
        int destHeight = destRegion.y + destRegion.height;
        return imageType.createBufferedImage(destWidth, destHeight);
    }
}

