/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.FramePeer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.AWTAccessor;
import sun.awt.EmbeddedFrame;
import sun.awt.image.ByteInterleavedRaster;
import sun.awt.windows.WEmbeddedFramePeer;
import sun.awt.windows.WToolkit;
import sun.awt.windows.WWindowPeer;
import sun.security.action.GetPropertyAction;

public class WEmbeddedFrame
extends EmbeddedFrame {
    private long handle;
    private int bandWidth = 0;
    private int bandHeight = 0;
    private int imgWid = 0;
    private int imgHgt = 0;
    private static int pScale;
    private static final int MAX_BAND_SIZE = 30720;
    private boolean isEmbeddedInIE = false;
    private static String printScale;

    public WEmbeddedFrame() {
        this(0L);
    }

    @Deprecated
    public WEmbeddedFrame(int handle) {
        this((long)handle);
    }

    public WEmbeddedFrame(long handle) {
        this.handle = handle;
        if (handle != 0L) {
            this.addNotify();
            this.show();
        }
    }

    @Override
    public void addNotify() {
        if (!this.isDisplayable()) {
            WToolkit toolkit = (WToolkit)Toolkit.getDefaultToolkit();
            this.setPeer(toolkit.createEmbeddedFrame(this));
        }
        super.addNotify();
    }

    public long getEmbedderHandle() {
        return this.handle;
    }

    void print(long hdc) {
        BufferedImage bandImage = null;
        int xscale = 1;
        int yscale = 1;
        if (this.isPrinterDC(hdc)) {
            xscale = yscale = WEmbeddedFrame.getPrintScaleFactor();
        }
        int frameHeight = this.getHeight();
        if (bandImage == null) {
            this.bandWidth = this.getWidth();
            if (this.bandWidth % 4 != 0) {
                this.bandWidth += 4 - this.bandWidth % 4;
            }
            if (this.bandWidth <= 0) {
                return;
            }
            this.bandHeight = Math.min(30720 / this.bandWidth, frameHeight);
            this.imgWid = this.bandWidth * xscale;
            this.imgHgt = this.bandHeight * yscale;
            bandImage = new BufferedImage(this.imgWid, this.imgHgt, 5);
        }
        Graphics clearGraphics = bandImage.getGraphics();
        clearGraphics.setColor(Color.white);
        Graphics2D g2d = (Graphics2D)bandImage.getGraphics();
        g2d.translate(0, this.imgHgt);
        g2d.scale(xscale, -yscale);
        ByteInterleavedRaster ras = (ByteInterleavedRaster)bandImage.getRaster();
        byte[] data = ras.getDataStorage();
        for (int bandTop = 0; bandTop < frameHeight; bandTop += this.bandHeight) {
            clearGraphics.fillRect(0, 0, this.bandWidth, this.bandHeight);
            this.printComponents(g2d);
            int imageOffset = 0;
            int currBandHeight = this.bandHeight;
            int currImgHeight = this.imgHgt;
            if (bandTop + this.bandHeight > frameHeight) {
                currBandHeight = frameHeight - bandTop;
                currImgHeight = currBandHeight * yscale;
                imageOffset = this.imgWid * (this.imgHgt - currImgHeight) * 3;
            }
            this.printBand(hdc, data, imageOffset, 0, 0, this.imgWid, currImgHeight, 0, bandTop, this.bandWidth, currBandHeight);
            g2d.translate(0, -this.bandHeight);
        }
    }

    protected static int getPrintScaleFactor() {
        int default_printDC_scale;
        if (pScale != 0) {
            return pScale;
        }
        if (printScale == null) {
            printScale = AccessController.doPrivileged(new PrivilegedAction<String>(){

                @Override
                public String run() {
                    return System.getenv("JAVA2D_PLUGIN_PRINT_SCALE");
                }
            });
        }
        int scale = default_printDC_scale = 4;
        if (printScale != null) {
            try {
                scale = Integer.parseInt(printScale);
                if (scale > 8 || scale < 1) {
                    scale = default_printDC_scale;
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        pScale = scale;
        return pScale;
    }

    private native boolean isPrinterDC(long var1);

    private native void printBand(long var1, byte[] var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12);

    private static native void initIDs();

    public void activateEmbeddingTopLevel() {
    }

    @Override
    public void synthesizeWindowActivation(boolean activate) {
        final FramePeer peer = (FramePeer)AWTAccessor.getComponentAccessor().getPeer(this);
        if (!activate || EventQueue.isDispatchThread()) {
            peer.emulateActivation(activate);
        } else {
            Runnable r = new Runnable(){

                @Override
                public void run() {
                    peer.emulateActivation(true);
                }
            };
            WToolkit.postEvent(WToolkit.targetToAppContext(this), new InvocationEvent((Object)this, r));
        }
    }

    @Override
    public void registerAccelerator(AWTKeyStroke stroke) {
    }

    @Override
    public void unregisterAccelerator(AWTKeyStroke stroke) {
    }

    @Override
    public void notifyModalBlocked(Dialog blocker, boolean blocked) {
        try {
            ComponentPeer thisPeer = (ComponentPeer)WToolkit.targetToPeer(this);
            ComponentPeer blockerPeer = (ComponentPeer)WToolkit.targetToPeer(blocker);
            this.notifyModalBlockedImpl((WEmbeddedFramePeer)thisPeer, (WWindowPeer)blockerPeer, blocked);
        }
        catch (Exception z) {
            z.printStackTrace(System.err);
        }
    }

    native void notifyModalBlockedImpl(WEmbeddedFramePeer var1, WWindowPeer var2, boolean var3);

    static {
        WEmbeddedFrame.initIDs();
        pScale = 0;
        printScale = AccessController.doPrivileged(new GetPropertyAction("sun.java2d.print.pluginscalefactor"));
    }
}

