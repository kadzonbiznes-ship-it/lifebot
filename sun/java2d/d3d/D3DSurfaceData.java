/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.AlphaComposite;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import sun.awt.SunToolkit;
import sun.awt.image.DataBufferNative;
import sun.awt.image.PixelConverter;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.image.WritableRasterNative;
import sun.awt.windows.WComponentPeer;
import sun.awt.windows.WWindowPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.StateTracker;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.SurfaceDataProxy;
import sun.java2d.d3d.D3DBlitLoops;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DDrawImage;
import sun.java2d.d3d.D3DGraphicsConfig;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.d3d.D3DMaskBlit;
import sun.java2d.d3d.D3DMaskFill;
import sun.java2d.d3d.D3DPaints;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.d3d.D3DRenderer;
import sun.java2d.d3d.D3DSurfaceDataProxy;
import sun.java2d.d3d.D3DTextRenderer;
import sun.java2d.d3d.D3DVolatileSurfaceManager;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.PixelToParallelogramConverter;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.hw.AccelSurface;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities;

public class D3DSurfaceData
extends SurfaceData
implements AccelSurface {
    public static final int D3D_DEVICE_RESOURCE = 100;
    public static final int ST_INT_ARGB = 0;
    public static final int ST_INT_ARGB_PRE = 1;
    public static final int ST_INT_ARGB_BM = 2;
    public static final int ST_INT_RGB = 3;
    public static final int ST_INT_BGR = 4;
    public static final int ST_USHORT_565_RGB = 5;
    public static final int ST_USHORT_555_RGB = 6;
    public static final int ST_BYTE_INDEXED = 7;
    public static final int ST_BYTE_INDEXED_BM = 8;
    public static final int ST_3BYTE_BGR = 9;
    public static final int SWAP_DISCARD = 1;
    public static final int SWAP_FLIP = 2;
    public static final int SWAP_COPY = 3;
    private static final String DESC_D3D_SURFACE = "D3D Surface";
    private static final String DESC_D3D_SURFACE_RTT = "D3D Surface (render-to-texture)";
    private static final String DESC_D3D_TEXTURE = "D3D Texture";
    static final SurfaceType D3DSurface = SurfaceType.Any.deriveSubType("D3D Surface", PixelConverter.ArgbPre.instance);
    static final SurfaceType D3DSurfaceRTT = D3DSurface.deriveSubType("D3D Surface (render-to-texture)");
    static final SurfaceType D3DTexture = SurfaceType.Any.deriveSubType("D3D Texture");
    private int type;
    private int width;
    private int height;
    private final double scaleX;
    private final double scaleY;
    private int nativeWidth;
    private int nativeHeight;
    protected WComponentPeer peer;
    private Image offscreenImage;
    protected D3DGraphicsDevice graphicsDevice;
    private int swapEffect;
    private ExtendedBufferCapabilities.VSyncType syncType;
    private int backBuffersNum;
    private WritableRasterNative wrn;
    protected static D3DRenderer d3dRenderPipe;
    protected static PixelToParallelogramConverter d3dTxRenderPipe;
    protected static ParallelogramPipe d3dAAPgramPipe;
    protected static D3DTextRenderer d3dTextPipe;
    protected static D3DDrawImage d3dImagePipe;

    private native boolean initTexture(long var1, boolean var3, boolean var4);

    private native boolean initFlipBackbuffer(long var1, long var3, int var5, int var6, int var7);

    private native boolean initRTSurface(long var1, boolean var3);

    private native void initOps(int var1, int var2, int var3);

    protected D3DSurfaceData(WComponentPeer peer, D3DGraphicsConfig gc, int width, int height, Image image, ColorModel cm, int numBackBuffers, int swapEffect, ExtendedBufferCapabilities.VSyncType vSyncType, int type) {
        super(D3DSurfaceData.getCustomSurfaceType(type), cm);
        this.graphicsDevice = gc.getD3DDevice();
        this.scaleX = type == 3 ? 1.0 : (double)this.graphicsDevice.getDefaultScaleX();
        this.scaleY = type == 3 ? 1.0 : (double)this.graphicsDevice.getDefaultScaleY();
        this.peer = peer;
        this.type = type;
        if (this.scaleX == 1.0 && this.scaleY == 1.0) {
            this.width = width;
            this.height = height;
        } else if (peer instanceof WWindowPeer) {
            Dimension scaledSize = ((WWindowPeer)peer).getScaledWindowSize();
            this.width = scaledSize.width;
            this.height = scaledSize.height;
        } else {
            this.width = Region.clipRound((double)width * this.scaleX);
            this.height = Region.clipRound((double)height * this.scaleY);
        }
        this.offscreenImage = image;
        this.backBuffersNum = numBackBuffers;
        this.swapEffect = swapEffect;
        this.syncType = vSyncType;
        this.initOps(this.graphicsDevice.getScreen(), this.width, this.height);
        if (type == 1) {
            this.setSurfaceLost(true);
        } else {
            this.initSurface();
        }
        this.setBlitProxyKey(gc.getProxyKey());
    }

    @Override
    public double getDefaultScaleX() {
        return this.scaleX;
    }

    @Override
    public double getDefaultScaleY() {
        return this.scaleY;
    }

    @Override
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return D3DSurfaceDataProxy.createProxy(srcData, (D3DGraphicsConfig)this.graphicsDevice.getDefaultConfiguration());
    }

    public static D3DSurfaceData createData(WComponentPeer peer, Image image) {
        D3DGraphicsConfig gc = D3DSurfaceData.getGC(peer);
        if (gc == null || !peer.isAccelCapable()) {
            return null;
        }
        BufferCapabilities caps = peer.getBackBufferCaps();
        ExtendedBufferCapabilities.VSyncType vSyncType = ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT;
        if (caps instanceof ExtendedBufferCapabilities) {
            vSyncType = ((ExtendedBufferCapabilities)caps).getVSync();
        }
        Rectangle r = peer.getBounds();
        BufferCapabilities.FlipContents flip = caps.getFlipContents();
        int swapEffect = flip == BufferCapabilities.FlipContents.COPIED ? 3 : (flip == BufferCapabilities.FlipContents.PRIOR ? 2 : 1);
        return new D3DSurfaceData(peer, gc, r.width, r.height, image, peer.getColorModel(), peer.getBackBuffersNum(), swapEffect, vSyncType, 4);
    }

    public static D3DSurfaceData createData(WComponentPeer peer) {
        D3DGraphicsConfig gc = D3DSurfaceData.getGC(peer);
        if (gc == null || !peer.isAccelCapable()) {
            return null;
        }
        return new D3DWindowSurfaceData(peer, gc);
    }

    public static D3DSurfaceData createData(D3DGraphicsConfig gc, int width, int height, ColorModel cm, Image image, int type) {
        D3DSurfaceData ret;
        block4: {
            if (type == 5) {
                int cap;
                boolean isOpaque = cm.getTransparency() == 1;
                int n = cap = isOpaque ? 8 : 4;
                if (!gc.getD3DDevice().isCapPresent(cap)) {
                    type = 2;
                }
            }
            ret = null;
            try {
                ret = new D3DSurfaceData(null, gc, width, height, image, cm, 0, 1, ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT, type);
            }
            catch (InvalidPipeException ipe) {
                if (type != 5 || ((SunVolatileImage)image).getForcedAccelSurfaceType() == 5) break block4;
                type = 2;
                ret = new D3DSurfaceData(null, gc, width, height, image, cm, 0, 1, ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT, type);
            }
        }
        return ret;
    }

    private static SurfaceType getCustomSurfaceType(int d3dType) {
        switch (d3dType) {
            case 3: {
                return D3DTexture;
            }
            case 5: {
                return D3DSurfaceRTT;
            }
        }
        return D3DSurface;
    }

    private boolean initSurfaceNow() {
        boolean isOpaque = this.getTransparency() == 1;
        switch (this.type) {
            case 2: {
                return this.initRTSurface(this.getNativeOps(), isOpaque);
            }
            case 3: {
                return this.initTexture(this.getNativeOps(), false, isOpaque);
            }
            case 5: {
                return this.initTexture(this.getNativeOps(), true, isOpaque);
            }
            case 1: 
            case 4: {
                return this.initFlipBackbuffer(this.getNativeOps(), this.peer.getData(), this.backBuffersNum, this.swapEffect, this.syncType.id());
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void initSurface() {
        D3DSurfaceData d3DSurfaceData = this;
        synchronized (d3DSurfaceData) {
            this.wrn = null;
        }
        class Status {
            boolean success = false;

            Status(D3DSurfaceData this$0) {
            }
        }
        final Status status = new Status(this);
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.flushAndInvokeNow(new Runnable(){
                {
                }

                @Override
                public void run() {
                    status.success = D3DSurfaceData.this.initSurfaceNow();
                }
            });
            if (!status.success) {
                throw new InvalidPipeException("Error creating D3DSurface");
            }
        }
        finally {
            rq.unlock();
        }
    }

    @Override
    public final D3DContext getContext() {
        return this.graphicsDevice.getContext();
    }

    @Override
    public final int getType() {
        return this.type;
    }

    private static native int dbGetPixelNative(long var0, int var2, int var3);

    private static native void dbSetPixelNative(long var0, int var2, int var3, int var4);

    @Override
    public synchronized Raster getRaster(int x, int y, int w, int h) {
        if (this.wrn == null) {
            DirectColorModel dcm = (DirectColorModel)this.getColorModel();
            int dataType = 0;
            int scanStride = this.width;
            dataType = dcm.getPixelSize() > 16 ? 3 : 1;
            SinglePixelPackedSampleModel smHw = new SinglePixelPackedSampleModel(dataType, this.width, this.height, scanStride, dcm.getMasks());
            D3DDataBufferNative dbn = new D3DDataBufferNative(this, dataType, this.width, this.height);
            this.wrn = WritableRasterNative.createNativeRaster(smHw, dbn);
        }
        return this.wrn;
    }

    @Override
    public boolean canRenderLCDText(SunGraphics2D sg2d) {
        return this.graphicsDevice.isCapPresent(65536) && sg2d.compositeState <= 0 && sg2d.paintState <= 0 && sg2d.surfaceData.getTransparency() == 1;
    }

    void disableAccelerationForSurface() {
        SurfaceManager sm;
        if (this.offscreenImage != null && (sm = SurfaceManager.getManager(this.offscreenImage)) instanceof D3DVolatileSurfaceManager) {
            this.setSurfaceLost(true);
            ((D3DVolatileSurfaceManager)sm).setAccelerationEnabled(false);
        }
    }

    @Override
    public void validatePipe(SunGraphics2D sg2d) {
        TextPipe textpipe;
        boolean validated = false;
        if (sg2d.compositeState >= 2) {
            super.validatePipe(sg2d);
            sg2d.imagepipe = d3dImagePipe;
            this.disableAccelerationForSurface();
            return;
        }
        if (sg2d.compositeState <= 0 && sg2d.paintState <= 1 || sg2d.compositeState == 1 && sg2d.paintState <= 1 && ((AlphaComposite)sg2d.composite).getRule() == 3 || sg2d.compositeState == 2 && sg2d.paintState <= 1) {
            textpipe = d3dTextPipe;
        } else {
            super.validatePipe(sg2d);
            textpipe = sg2d.textpipe;
            validated = true;
        }
        PixelToParallelogramConverter txPipe = null;
        D3DRenderer nonTxPipe = null;
        if (sg2d.antialiasHint != 2) {
            if (sg2d.paintState <= 1) {
                if (sg2d.compositeState <= 2) {
                    txPipe = d3dTxRenderPipe;
                    nonTxPipe = d3dRenderPipe;
                }
            } else if (sg2d.compositeState <= 1 && D3DPaints.isValid(sg2d)) {
                txPipe = d3dTxRenderPipe;
                nonTxPipe = d3dRenderPipe;
            }
        } else if (sg2d.paintState <= 1) {
            if (this.graphicsDevice.isCapPresent(524288) && (sg2d.imageComp == CompositeType.SrcOverNoEa || sg2d.imageComp == CompositeType.SrcOver)) {
                if (!validated) {
                    super.validatePipe(sg2d);
                    validated = true;
                }
                PixelToParallelogramConverter aaConverter = new PixelToParallelogramConverter(sg2d.shapepipe, d3dAAPgramPipe, 0.125, 0.499, false);
                sg2d.drawpipe = aaConverter;
                sg2d.fillpipe = aaConverter;
                sg2d.shapepipe = aaConverter;
            } else if (sg2d.compositeState == 2) {
                txPipe = d3dTxRenderPipe;
                nonTxPipe = d3dRenderPipe;
            }
        }
        if (txPipe != null) {
            if (sg2d.transformState >= 3) {
                sg2d.drawpipe = txPipe;
                sg2d.fillpipe = txPipe;
            } else if (sg2d.strokeState != 0) {
                sg2d.drawpipe = txPipe;
                sg2d.fillpipe = nonTxPipe;
            } else {
                sg2d.drawpipe = nonTxPipe;
                sg2d.fillpipe = nonTxPipe;
            }
            sg2d.shapepipe = txPipe;
        } else if (!validated) {
            super.validatePipe(sg2d);
        }
        sg2d.textpipe = textpipe;
        sg2d.imagepipe = d3dImagePipe;
    }

    @Override
    protected MaskFill getMaskFill(SunGraphics2D sg2d) {
        if (!(sg2d.paintState <= 1 || D3DPaints.isValid(sg2d) && this.graphicsDevice.isCapPresent(16))) {
            return null;
        }
        return super.getMaskFill(sg2d);
    }

    @Override
    public boolean copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
        if (sg2d.compositeState >= 2) {
            return false;
        }
        d3dRenderPipe.copyArea(sg2d, x, y, w, h, dx, dy);
        return true;
    }

    @Override
    public void flush() {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(72);
            buf.putLong(this.getNativeOps());
            rq.flushNow();
        }
        finally {
            rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void dispose(long pData) {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(73);
            buf.putLong(pData);
            rq.flushNow();
        }
        finally {
            rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void swapBuffers(final D3DSurfaceData sd, final int x1, final int y1, final int x2, final int y2) {
        long pData = sd.getNativeOps();
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        if (D3DRenderQueue.isRenderQueueThread()) {
            if (!rq.tryLock()) {
                final Component target = (Component)sd.getPeer().getTarget();
                SunToolkit.executeOnEventHandlerThread(target, new Runnable(){

                    @Override
                    public void run() {
                        double scaleX = sd.getDefaultScaleX();
                        double scaleY = sd.getDefaultScaleY();
                        if (scaleX > 1.0 || scaleY > 1.0) {
                            int sx1 = (int)Math.floor((double)x1 / scaleX);
                            int sy1 = (int)Math.floor((double)y1 / scaleY);
                            int sx2 = (int)Math.ceil((double)x2 / scaleX);
                            int sy2 = (int)Math.ceil((double)y2 / scaleY);
                            target.repaint(sx1, sy1, sx2 - sx1, sy2 - sy1);
                        } else {
                            target.repaint(x1, y1, x2 - x1, y2 - y1);
                        }
                    }
                });
                return;
            }
        } else {
            rq.lock();
        }
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(28, 4);
            buf.putInt(80);
            buf.putLong(pData);
            buf.putInt(x1);
            buf.putInt(y1);
            buf.putInt(x2);
            buf.putInt(y2);
            rq.flushNow();
        }
        finally {
            rq.unlock();
        }
    }

    @Override
    public Object getDestination() {
        return this.offscreenImage;
    }

    @Override
    public Rectangle getBounds() {
        if (this.type == 4 || this.type == 1) {
            double scaleX = this.getDefaultScaleX();
            double scaleY = this.getDefaultScaleY();
            Rectangle r = this.peer.getBounds();
            r.y = 0;
            r.x = 0;
            r.width = Region.clipRound((double)r.width * scaleX);
            r.height = Region.clipRound((double)r.height * scaleY);
            return r;
        }
        return new Rectangle(this.width, this.height);
    }

    @Override
    public Rectangle getNativeBounds() {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            Rectangle rectangle = new Rectangle(this.nativeWidth, this.nativeHeight);
            return rectangle;
        }
        finally {
            rq.unlock();
        }
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return this.graphicsDevice.getDefaultConfiguration();
    }

    @Override
    public SurfaceData getReplacement() {
        return D3DSurfaceData.restoreContents(this.offscreenImage);
    }

    private static D3DGraphicsConfig getGC(WComponentPeer peer) {
        GraphicsConfiguration gc;
        if (peer != null) {
            gc = peer.getGraphicsConfiguration();
        } else {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            gc = gd.getDefaultConfiguration();
        }
        return gc instanceof D3DGraphicsConfig ? (D3DGraphicsConfig)gc : null;
    }

    void restoreSurface() {
        this.initSurface();
    }

    WComponentPeer getPeer() {
        return this.peer;
    }

    @Override
    public void setSurfaceLost(boolean lost) {
        super.setSurfaceLost(lost);
        if (lost && this.offscreenImage != null) {
            SurfaceManager sm = SurfaceManager.getManager(this.offscreenImage);
            sm.acceleratedSurfaceLost();
        }
    }

    private static native long getNativeResourceNative(long var0, int var2);

    @Override
    public long getNativeResource(int resType) {
        return D3DSurfaceData.getNativeResourceNative(this.getNativeOps(), resType);
    }

    public static native boolean updateWindowAccelImpl(long var0, long var2, int var4, int var5);

    static {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        d3dImagePipe = new D3DDrawImage();
        d3dTextPipe = new D3DTextRenderer(rq);
        d3dRenderPipe = new D3DRenderer(rq);
        if (GraphicsPrimitive.tracingEnabled()) {
            d3dTextPipe = d3dTextPipe.traceWrap();
            d3dRenderPipe = d3dRenderPipe.traceWrap();
        }
        d3dAAPgramPipe = d3dRenderPipe.getAAParallelogramPipe();
        d3dTxRenderPipe = new PixelToParallelogramConverter(d3dRenderPipe, d3dRenderPipe, 1.0, 0.25, true);
        D3DBlitLoops.register();
        D3DMaskFill.register();
        D3DMaskBlit.register();
    }

    public static class D3DWindowSurfaceData
    extends D3DSurfaceData {
        StateTracker dirtyTracker = this.getStateTracker();

        public D3DWindowSurfaceData(WComponentPeer peer, D3DGraphicsConfig gc) {
            super(peer, gc, peer.getBounds().width, peer.getBounds().height, null, peer.getColorModel(), 1, 3, ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT, 1);
        }

        @Override
        public SurfaceData getReplacement() {
            ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
            return mgr.getReplacementScreenSurface(this.peer, this);
        }

        @Override
        public Object getDestination() {
            return this.peer.getTarget();
        }

        @Override
        void disableAccelerationForSurface() {
            this.setSurfaceLost(true);
            this.invalidate();
            this.flush();
            this.peer.disableAcceleration();
            ScreenUpdateManager.getInstance().dropScreenSurface(this);
        }

        @Override
        void restoreSurface() {
            if (!this.peer.isAccelCapable()) {
                throw new InvalidPipeException("Onscreen acceleration disabled for this surface");
            }
            Window fsw = this.graphicsDevice.getFullScreenWindow();
            if (fsw != null && fsw != this.peer.getTarget()) {
                throw new InvalidPipeException("Can't restore onscreen surface when in full-screen mode");
            }
            super.restoreSurface();
            this.setSurfaceLost(false);
            D3DRenderQueue rq = D3DRenderQueue.getInstance();
            rq.lock();
            try {
                this.getContext().invalidateContext();
            }
            finally {
                rq.unlock();
            }
        }

        public boolean isDirty() {
            return !this.dirtyTracker.isCurrent();
        }

        public void markClean() {
            this.dirtyTracker = this.getStateTracker();
        }
    }

    static class D3DDataBufferNative
    extends DataBufferNative {
        int pixel;

        protected D3DDataBufferNative(SurfaceData sData, int type, int w, int h) {
            super(sData, type, w, h);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected int getElem(final int x, final int y, final SurfaceData sData) {
            int retPixel;
            if (sData.isSurfaceLost()) {
                return 0;
            }
            D3DRenderQueue rq = D3DRenderQueue.getInstance();
            rq.lock();
            try {
                rq.flushAndInvokeNow(new Runnable(){
                    final /* synthetic */ D3DDataBufferNative this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public void run() {
                        this.this$0.pixel = D3DSurfaceData.dbGetPixelNative(sData.getNativeOps(), x, y);
                    }
                });
            }
            finally {
                retPixel = this.pixel;
                rq.unlock();
            }
            return retPixel;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void setElem(final int x, final int y, final int pixel, final SurfaceData sData) {
            if (sData.isSurfaceLost()) {
                return;
            }
            D3DRenderQueue rq = D3DRenderQueue.getInstance();
            rq.lock();
            try {
                rq.flushAndInvokeNow(new Runnable(){

                    @Override
                    public void run() {
                        D3DSurfaceData.dbSetPixelNative(sData.getNativeOps(), x, y, pixel);
                    }
                });
                sData.markDirty();
            }
            finally {
                rq.unlock();
            }
        }
    }
}

