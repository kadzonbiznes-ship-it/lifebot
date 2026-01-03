/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import sun.awt.DisplayChangedListener;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.image.OffScreenImage;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.windows.WComponentPeer;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.windows.GDIWindowSurfaceData;

public class Win32GraphicsConfig
extends GraphicsConfiguration
implements DisplayChangedListener,
SurfaceManager.ProxiedGraphicsConfig {
    private final Win32GraphicsDevice device;
    protected int visual;
    protected RenderLoops solidloops;
    private SurfaceType sTypeOrig = null;

    private static native void initIDs();

    public static Win32GraphicsConfig getConfig(Win32GraphicsDevice device, int pixFormatID) {
        return new Win32GraphicsConfig(device, pixFormatID);
    }

    @Deprecated
    public Win32GraphicsConfig(GraphicsDevice device, int visualnum) {
        this.device = (Win32GraphicsDevice)device;
        this.visual = visualnum;
        ((Win32GraphicsDevice)device).addDisplayChangedListener(this);
    }

    @Override
    public Win32GraphicsDevice getDevice() {
        return this.device;
    }

    public int getVisual() {
        return this.visual;
    }

    @Override
    public Object getProxyKey() {
        return this.device;
    }

    public synchronized RenderLoops getSolidLoops(SurfaceType stype) {
        if (this.solidloops == null || this.sTypeOrig != stype) {
            this.solidloops = SurfaceData.makeRenderLoops(SurfaceType.OpaqueColor, CompositeType.SrcNoEa, stype);
            this.sTypeOrig = stype;
        }
        return this.solidloops;
    }

    @Override
    public synchronized ColorModel getColorModel() {
        return this.device.getColorModel();
    }

    public ColorModel getDeviceColorModel() {
        return this.device.getDynamicColorModel();
    }

    @Override
    public ColorModel getColorModel(int transparency) {
        switch (transparency) {
            case 1: {
                return this.getColorModel();
            }
            case 2: {
                return new DirectColorModel(25, 0xFF0000, 65280, 255, 0x1000000);
            }
            case 3: {
                return ColorModel.getRGBdefault();
            }
        }
        return null;
    }

    @Override
    public AffineTransform getDefaultTransform() {
        double scaleX = this.device.getDefaultScaleX();
        double scaleY = this.device.getDefaultScaleY();
        return AffineTransform.getScaleInstance(scaleX, scaleY);
    }

    @Override
    public AffineTransform getNormalizingTransform() {
        Win32GraphicsEnvironment ge = (Win32GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment();
        double xscale = (double)ge.getXResolution() / 72.0;
        double yscale = (double)ge.getYResolution() / 72.0;
        return new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
    }

    public String toString() {
        return super.toString() + "[dev=" + String.valueOf(this.device) + ",pixfmt=" + this.visual + "]";
    }

    private native Rectangle getBounds(int var1);

    @Override
    public Rectangle getBounds() {
        return this.getBounds(this.device.getScreen());
    }

    @Override
    public synchronized void displayChanged() {
        this.solidloops = null;
    }

    @Override
    public void paletteChanged() {
    }

    public SurfaceData createSurfaceData(WComponentPeer peer, int numBackBuffers) {
        return GDIWindowSurfaceData.createData(peer);
    }

    public Image createAcceleratedImage(Component target, int width, int height) {
        ColorModel model = this.getColorModel(1);
        WritableRaster wr = model.createCompatibleWritableRaster(width, height);
        return new OffScreenImage(target, model, wr, model.isAlphaPremultiplied());
    }

    public void assertOperationSupported(Component target, int numBuffers, BufferCapabilities caps) throws AWTException {
        throw new AWTException("The operation requested is not supported");
    }

    public VolatileImage createBackBuffer(WComponentPeer peer) {
        Component target = (Component)peer.getTarget();
        return new SunVolatileImage(target, target.getWidth(), target.getHeight(), Boolean.TRUE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void flip(WComponentPeer peer, Component target, VolatileImage backBuffer, int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction) {
        if (flipAction == BufferCapabilities.FlipContents.COPIED || flipAction == BufferCapabilities.FlipContents.UNDEFINED) {
            Graphics g = peer.getGraphics();
            try {
                g.drawImage(backBuffer, x1, y1, x2, y2, x1, y1, x2, y2, null);
            }
            finally {
                g.dispose();
            }
        }
        if (flipAction == BufferCapabilities.FlipContents.BACKGROUND) {
            Graphics g = backBuffer.getGraphics();
            try {
                g.setColor(target.getBackground());
                g.fillRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());
            }
            finally {
                g.dispose();
            }
        }
    }

    @Override
    public boolean isTranslucencyCapable() {
        return true;
    }

    static {
        Win32GraphicsConfig.initIDs();
    }
}

