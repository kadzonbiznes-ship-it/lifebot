/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.image.SunWritableRaster;
import sun.util.logging.PlatformLogger;

public final class SplashScreen {
    private BufferedImage image;
    private final long splashPtr;
    private static boolean wasClosed = false;
    private URL imageURL;
    private static SplashScreen theInstance = null;
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.SplashScreen");

    SplashScreen(long ptr) {
        this.splashPtr = ptr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static SplashScreen getSplashScreen() {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            if (GraphicsEnvironment.isHeadless()) {
                throw new HeadlessException();
            }
            if (!wasClosed && theInstance == null) {
                AccessController.doPrivileged(new PrivilegedAction<Void>(){

                    @Override
                    public Void run() {
                        System.loadLibrary("splashscreen");
                        return null;
                    }
                });
                long ptr = SplashScreen._getInstance();
                if (ptr != 0L && SplashScreen._isVisible(ptr)) {
                    theInstance = new SplashScreen(ptr);
                }
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return theInstance;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setImageURL(URL imageURL) throws NullPointerException, IOException, IllegalStateException {
        this.checkVisible();
        URLConnection connection = imageURL.openConnection();
        connection.connect();
        int length = connection.getContentLength();
        InputStream stream = connection.getInputStream();
        byte[] buf = new byte[length];
        int off = 0;
        while (true) {
            int result;
            int available;
            if ((available = stream.available()) <= 0) {
                available = 1;
            }
            if (off + available > length) {
                length = off * 2;
                if (off + available > length) {
                    length = available + off;
                }
                byte[] oldBuf = buf;
                buf = new byte[length];
                System.arraycopy(oldBuf, 0, buf, 0, off);
            }
            if ((result = stream.read(buf, off, available)) < 0) break;
            off += result;
        }
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            this.checkVisible();
            if (!SplashScreen._setImageData(this.splashPtr, buf)) {
                throw new IOException("Bad image format or i/o error when loading image");
            }
            this.imageURL = imageURL;
            // ** MonitorExit[var7_8] (shouldn't be in output)
            return;
        }
    }

    private void checkVisible() {
        if (!this.isVisible()) {
            throw new IllegalStateException("no splash screen available");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public URL getImageURL() throws IllegalStateException {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            block9: {
                this.checkVisible();
                if (this.imageURL == null) {
                    try {
                        String fileName = SplashScreen._getImageFileName(this.splashPtr);
                        String jarName = SplashScreen._getImageJarName(this.splashPtr);
                        if (fileName != null) {
                            if (jarName != null) {
                                URL uRL = this.imageURL = new URL("jar:" + new File(jarName).toURL().toString() + "!/" + fileName);
                            } else {
                                this.imageURL = new File(fileName).toURL();
                            }
                        }
                    }
                    catch (MalformedURLException e) {
                        if (!log.isLoggable(PlatformLogger.Level.FINE)) break block9;
                        log.fine("MalformedURLException caught in the getImageURL() method", e);
                    }
                }
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return this.imageURL;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Rectangle getBounds() throws IllegalStateException {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            this.checkVisible();
            float scale = SplashScreen._getScaleFactor(this.splashPtr);
            Rectangle bounds = SplashScreen._getBounds(this.splashPtr);
            assert (scale > 0.0f);
            if (scale > 0.0f && scale != 1.0f) {
                bounds.setSize((int)(bounds.getWidth() / (double)scale), (int)(bounds.getHeight() / (double)scale));
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return bounds;
        }
    }

    public Dimension getSize() throws IllegalStateException {
        return this.getBounds().getSize();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Graphics2D createGraphics() throws IllegalStateException {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            this.checkVisible();
            if (this.image == null) {
                Dimension dim = SplashScreen._getBounds(this.splashPtr).getSize();
                this.image = new BufferedImage(dim.width, dim.height, 2);
            }
            float scale = SplashScreen._getScaleFactor(this.splashPtr);
            Graphics2D g = this.image.createGraphics();
            assert (scale > 0.0f);
            if (scale <= 0.0f) {
                scale = 1.0f;
            }
            g.scale(scale, scale);
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return g;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void update() throws IllegalStateException {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            this.checkVisible();
            BufferedImage image = this.image;
            // ** MonitorExit[var2_1] (shouldn't be in output)
            if (image == null) {
                throw new IllegalStateException("no overlay image available");
            }
            DataBuffer buf = image.getRaster().getDataBuffer();
            if (!(buf instanceof DataBufferInt)) {
                throw new AssertionError((Object)("Overlay image DataBuffer is of invalid type == " + buf.getClass().getName()));
            }
            int numBanks = buf.getNumBanks();
            if (numBanks != 1) {
                throw new AssertionError((Object)("Invalid number of banks ==" + numBanks + " in overlay image DataBuffer"));
            }
            if (!(image.getSampleModel() instanceof SinglePixelPackedSampleModel)) {
                throw new AssertionError((Object)("Overlay image has invalid sample model == " + image.getSampleModel().getClass().getName()));
            }
            SinglePixelPackedSampleModel sm = (SinglePixelPackedSampleModel)image.getSampleModel();
            int scanlineStride = sm.getScanlineStride();
            Rectangle rect = image.getRaster().getBounds();
            int[] data = SunWritableRaster.stealData((DataBufferInt)buf, 0);
            Class<SplashScreen> clazz2 = SplashScreen.class;
            synchronized (SplashScreen.class) {
                this.checkVisible();
                SplashScreen._update(this.splashPtr, data, rect.x, rect.y, rect.width, rect.height, scanlineStride);
                // ** MonitorExit[var8_9] (shouldn't be in output)
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws IllegalStateException {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            this.checkVisible();
            SplashScreen._close(this.splashPtr);
            this.image = null;
            SplashScreen.markClosed();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void markClosed() {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            wasClosed = true;
            theInstance = null;
            // ** MonitorExit[var0] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isVisible() {
        Class<SplashScreen> clazz = SplashScreen.class;
        synchronized (SplashScreen.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return !wasClosed && SplashScreen._isVisible(this.splashPtr);
        }
    }

    private static native void _update(long var0, int[] var2, int var3, int var4, int var5, int var6, int var7);

    private static native boolean _isVisible(long var0);

    private static native Rectangle _getBounds(long var0);

    private static native long _getInstance();

    private static native void _close(long var0);

    private static native String _getImageFileName(long var0);

    private static native String _getImageJarName(long var0);

    private static native boolean _setImageData(long var0, byte[] var2);

    private static native float _getScaleFactor(long var0);
}

