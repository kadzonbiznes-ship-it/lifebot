/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.ImageCapabilities;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.VolatileImage;
import sun.awt.Win32GraphicsConfig;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.windows.WComponentPeer;
import sun.java2d.Surface;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.pipe.hw.AccelGraphicsConfig;
import sun.java2d.pipe.hw.AccelSurface;
import sun.java2d.pipe.hw.AccelTypedVolatileImage;
import sun.java2d.pipe.hw.ContextCapabilities;

public final class D3DGraphicsConfig
extends Win32GraphicsConfig
implements AccelGraphicsConfig {
    private static ImageCapabilities imageCaps = new D3DImageCaps();
    private BufferCapabilities bufferCaps;
    private final D3DGraphicsDevice device;

    protected D3DGraphicsConfig(D3DGraphicsDevice device) {
        super(device, 0);
        this.device = device;
    }

    public SurfaceData createManagedSurface(int w, int h, int transparency) {
        return D3DSurfaceData.createData(this, w, h, this.getColorModel(transparency), null, 3);
    }

    @Override
    public synchronized void displayChanged() {
        super.displayChanged();
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            D3DContext.invalidateCurrentContext();
        }
        finally {
            rq.unlock();
        }
    }

    @Override
    public ColorModel getColorModel(int transparency) {
        switch (transparency) {
            case 1: {
                return new DirectColorModel(24, 0xFF0000, 65280, 255);
            }
            case 2: {
                return new DirectColorModel(25, 0xFF0000, 65280, 255, 0x1000000);
            }
            case 3: {
                ColorSpace cs = ColorSpace.getInstance(1000);
                return new DirectColorModel(cs, 32, 0xFF0000, 65280, 255, -16777216, true, 3);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "D3DGraphicsConfig[dev=" + String.valueOf(this.device) + ",pixfmt=" + this.visual + "]";
    }

    @Override
    public SurfaceData createSurfaceData(WComponentPeer peer, int numBackBuffers) {
        return super.createSurfaceData(peer, numBackBuffers);
    }

    @Override
    public void assertOperationSupported(Component target, int numBuffers, BufferCapabilities caps) throws AWTException {
        if (numBuffers < 2 || numBuffers > 4) {
            throw new AWTException("Only 2-4 buffers supported");
        }
        if (caps.getFlipContents() == BufferCapabilities.FlipContents.COPIED && numBuffers != 2) {
            throw new AWTException("FlipContents.COPIED is onlysupported for 2 buffers");
        }
    }

    @Override
    public VolatileImage createBackBuffer(WComponentPeer peer) {
        Component target = (Component)peer.getTarget();
        int w = Math.max(1, target.getWidth());
        int h = Math.max(1, target.getHeight());
        return new SunVolatileImage(target, w, h, Boolean.TRUE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void flip(WComponentPeer peer, Component target, VolatileImage backBuffer, int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction) {
        Graphics g;
        SurfaceManager d3dvsm = SurfaceManager.getManager(backBuffer);
        SurfaceData sd = d3dvsm.getPrimarySurfaceData();
        if (sd instanceof D3DSurfaceData) {
            D3DSurfaceData d3dsd = (D3DSurfaceData)sd;
            double scaleX = sd.getDefaultScaleX();
            double scaleY = sd.getDefaultScaleY();
            if (scaleX > 1.0 || scaleY > 1.0) {
                int sx1 = (int)Math.floor((double)x1 * scaleX);
                int sy1 = (int)Math.floor((double)y1 * scaleY);
                int sx2 = (int)Math.ceil((double)x2 * scaleX);
                int sy2 = (int)Math.ceil((double)y2 * scaleY);
                D3DSurfaceData.swapBuffers(d3dsd, sx1, sy1, sx2, sy2);
            } else {
                D3DSurfaceData.swapBuffers(d3dsd, x1, y1, x2, y2);
            }
        } else {
            g = peer.getGraphics();
            try {
                g.drawImage(backBuffer, x1, y1, x2, y2, x1, y1, x2, y2, null);
            }
            finally {
                g.dispose();
            }
        }
        if (flipAction == BufferCapabilities.FlipContents.BACKGROUND) {
            g = backBuffer.getGraphics();
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
    public BufferCapabilities getBufferCapabilities() {
        if (this.bufferCaps == null) {
            this.bufferCaps = new D3DBufferCaps();
        }
        return this.bufferCaps;
    }

    @Override
    public ImageCapabilities getImageCapabilities() {
        return imageCaps;
    }

    D3DGraphicsDevice getD3DDevice() {
        return this.device;
    }

    @Override
    public D3DContext getContext() {
        return this.device.getContext();
    }

    @Override
    public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency, int type) {
        AccelTypedVolatileImage vi;
        Surface sd;
        boolean isOpaque;
        if (type == 4 || type == 1 || type == 0 || transparency == 2) {
            return null;
        }
        boolean bl = isOpaque = transparency == 1;
        if (type == 5) {
            int cap;
            int n = cap = isOpaque ? 8 : 4;
            if (!this.device.isCapPresent(cap)) {
                return null;
            }
        } else if (type == 2 && !isOpaque && !this.device.isCapPresent(2)) {
            return null;
        }
        if (!((sd = (vi = new AccelTypedVolatileImage((GraphicsConfiguration)this, width, height, transparency, type)).getDestSurface()) instanceof AccelSurface) || ((AccelSurface)sd).getType() != type) {
            vi.flush();
            vi = null;
        }
        return vi;
    }

    @Override
    public ContextCapabilities getContextCapabilities() {
        return this.device.getContextCapabilities();
    }

    private static class D3DBufferCaps
    extends BufferCapabilities {
        public D3DBufferCaps() {
            super(imageCaps, imageCaps, BufferCapabilities.FlipContents.UNDEFINED);
        }

        @Override
        public boolean isMultiBufferAvailable() {
            return true;
        }
    }

    private static class D3DImageCaps
    extends ImageCapabilities {
        private D3DImageCaps() {
            super(true);
        }

        @Override
        public boolean isTrueVolatile() {
            return true;
        }
    }
}

