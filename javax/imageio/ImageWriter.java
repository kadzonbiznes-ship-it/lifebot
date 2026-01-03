/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;

public abstract class ImageWriter
implements ImageTranscoder {
    protected ImageWriterSpi originatingProvider = null;
    protected Object output = null;
    protected Locale[] availableLocales = null;
    protected Locale locale = null;
    protected List<IIOWriteWarningListener> warningListeners = null;
    protected List<Locale> warningLocales = null;
    protected List<IIOWriteProgressListener> progressListeners = null;
    private boolean abortFlag = false;

    protected ImageWriter(ImageWriterSpi originatingProvider) {
        this.originatingProvider = originatingProvider;
    }

    public ImageWriterSpi getOriginatingProvider() {
        return this.originatingProvider;
    }

    public void setOutput(Object output) {
        ImageWriterSpi provider;
        if (output != null && (provider = this.getOriginatingProvider()) != null) {
            Class<?>[] classes = provider.getOutputTypes();
            boolean found = false;
            for (int i = 0; i < classes.length; ++i) {
                if (!classes[i].isInstance(output)) continue;
                found = true;
                break;
            }
            if (!found) {
                throw new IllegalArgumentException("Illegal output type!");
            }
        }
        this.output = output;
    }

    public Object getOutput() {
        return this.output;
    }

    public Locale[] getAvailableLocales() {
        return this.availableLocales == null ? null : (Locale[])this.availableLocales.clone();
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

    public ImageWriteParam getDefaultWriteParam() {
        return new ImageWriteParam(this.getLocale());
    }

    public abstract IIOMetadata getDefaultStreamMetadata(ImageWriteParam var1);

    public abstract IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier var1, ImageWriteParam var2);

    @Override
    public abstract IIOMetadata convertStreamMetadata(IIOMetadata var1, ImageWriteParam var2);

    @Override
    public abstract IIOMetadata convertImageMetadata(IIOMetadata var1, ImageTypeSpecifier var2, ImageWriteParam var3);

    public int getNumThumbnailsSupported(ImageTypeSpecifier imageType, ImageWriteParam param, IIOMetadata streamMetadata, IIOMetadata imageMetadata) {
        return 0;
    }

    public Dimension[] getPreferredThumbnailSizes(ImageTypeSpecifier imageType, ImageWriteParam param, IIOMetadata streamMetadata, IIOMetadata imageMetadata) {
        return null;
    }

    public boolean canWriteRasters() {
        return false;
    }

    public abstract void write(IIOMetadata var1, IIOImage var2, ImageWriteParam var3) throws IOException;

    public void write(IIOImage image) throws IOException {
        this.write(null, image, null);
    }

    public void write(RenderedImage image) throws IOException {
        this.write(null, new IIOImage(image, null, null), null);
    }

    private void unsupported() {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        throw new UnsupportedOperationException("Unsupported write variant!");
    }

    public boolean canWriteSequence() {
        return false;
    }

    public void prepareWriteSequence(IIOMetadata streamMetadata) throws IOException {
        this.unsupported();
    }

    public void writeToSequence(IIOImage image, ImageWriteParam param) throws IOException {
        this.unsupported();
    }

    public void endWriteSequence() throws IOException {
        this.unsupported();
    }

    public boolean canReplaceStreamMetadata() throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void replaceStreamMetadata(IIOMetadata streamMetadata) throws IOException {
        this.unsupported();
    }

    public boolean canReplaceImageMetadata(int imageIndex) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void replaceImageMetadata(int imageIndex, IIOMetadata imageMetadata) throws IOException {
        this.unsupported();
    }

    public boolean canInsertImage(int imageIndex) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void writeInsert(int imageIndex, IIOImage image, ImageWriteParam param) throws IOException {
        this.unsupported();
    }

    public boolean canRemoveImage(int imageIndex) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void removeImage(int imageIndex) throws IOException {
        this.unsupported();
    }

    public boolean canWriteEmpty() throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void prepareWriteEmpty(IIOMetadata streamMetadata, ImageTypeSpecifier imageType, int width, int height, IIOMetadata imageMetadata, List<? extends BufferedImage> thumbnails, ImageWriteParam param) throws IOException {
        this.unsupported();
    }

    public void endWriteEmpty() throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        throw new IllegalStateException("No call to prepareWriteEmpty!");
    }

    public boolean canInsertEmpty(int imageIndex) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void prepareInsertEmpty(int imageIndex, ImageTypeSpecifier imageType, int width, int height, IIOMetadata imageMetadata, List<? extends BufferedImage> thumbnails, ImageWriteParam param) throws IOException {
        this.unsupported();
    }

    public void endInsertEmpty() throws IOException {
        this.unsupported();
    }

    public boolean canReplacePixels(int imageIndex) throws IOException {
        if (this.getOutput() == null) {
            throw new IllegalStateException("getOutput() == null!");
        }
        return false;
    }

    public void prepareReplacePixels(int imageIndex, Rectangle region) throws IOException {
        this.unsupported();
    }

    public void replacePixels(RenderedImage image, ImageWriteParam param) throws IOException {
        this.unsupported();
    }

    public void replacePixels(Raster raster, ImageWriteParam param) throws IOException {
        this.unsupported();
    }

    public void endReplacePixels() throws IOException {
        this.unsupported();
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

    public void addIIOWriteWarningListener(IIOWriteWarningListener listener) {
        if (listener == null) {
            return;
        }
        this.warningListeners = ImageReader.addToList(this.warningListeners, listener);
        this.warningLocales = ImageReader.addToList(this.warningLocales, this.getLocale());
    }

    public void removeIIOWriteWarningListener(IIOWriteWarningListener listener) {
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

    public void removeAllIIOWriteWarningListeners() {
        this.warningListeners = null;
        this.warningLocales = null;
    }

    public void addIIOWriteProgressListener(IIOWriteProgressListener listener) {
        if (listener == null) {
            return;
        }
        this.progressListeners = ImageReader.addToList(this.progressListeners, listener);
    }

    public void removeIIOWriteProgressListener(IIOWriteProgressListener listener) {
        if (listener == null || this.progressListeners == null) {
            return;
        }
        this.progressListeners = ImageReader.removeFromList(this.progressListeners, listener);
    }

    public void removeAllIIOWriteProgressListeners() {
        this.progressListeners = null;
    }

    protected void processImageStarted(int imageIndex) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteProgressListener listener = this.progressListeners.get(i);
            listener.imageStarted(this, imageIndex);
        }
    }

    protected void processImageProgress(float percentageDone) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteProgressListener listener = this.progressListeners.get(i);
            listener.imageProgress(this, percentageDone);
        }
    }

    protected void processImageComplete() {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteProgressListener listener = this.progressListeners.get(i);
            listener.imageComplete(this);
        }
    }

    protected void processThumbnailStarted(int imageIndex, int thumbnailIndex) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteProgressListener listener = this.progressListeners.get(i);
            listener.thumbnailStarted(this, imageIndex, thumbnailIndex);
        }
    }

    protected void processThumbnailProgress(float percentageDone) {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteProgressListener listener = this.progressListeners.get(i);
            listener.thumbnailProgress(this, percentageDone);
        }
    }

    protected void processThumbnailComplete() {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteProgressListener listener = this.progressListeners.get(i);
            listener.thumbnailComplete(this);
        }
    }

    protected void processWriteAborted() {
        if (this.progressListeners == null) {
            return;
        }
        int numListeners = this.progressListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteProgressListener listener = this.progressListeners.get(i);
            listener.writeAborted(this);
        }
    }

    protected void processWarningOccurred(int imageIndex, String warning) {
        if (this.warningListeners == null) {
            return;
        }
        if (warning == null) {
            throw new IllegalArgumentException("warning == null!");
        }
        int numListeners = this.warningListeners.size();
        for (int i = 0; i < numListeners; ++i) {
            IIOWriteWarningListener listener = this.warningListeners.get(i);
            listener.warningOccurred(this, imageIndex, warning);
        }
    }

    protected void processWarningOccurred(int imageIndex, String baseName, String keyword) {
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
            IIOWriteWarningListener listener = this.warningListeners.get(i);
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
            listener.warningOccurred(this, imageIndex, warning);
        }
    }

    public void reset() {
        this.setOutput(null);
        this.setLocale(null);
        this.removeAllIIOWriteWarningListeners();
        this.removeAllIIOWriteProgressListeners();
        this.clearAbortRequest();
    }

    public void dispose() {
    }
}

