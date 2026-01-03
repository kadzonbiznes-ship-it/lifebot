/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.ColorModel;
import sun.awt.AWTAccessor;
import sun.awt.Win32GraphicsConfig;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.image.VolatileSurfaceManager;
import sun.awt.windows.WComponentPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DGraphicsConfig;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.d3d.D3DScreenUpdateManager;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.windows.GDIWindowSurfaceData;

public class D3DVolatileSurfaceManager
extends VolatileSurfaceManager {
    private boolean accelerationEnabled;
    private int restoreCountdown;

    public D3DVolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        super(vImg, context);
        int transparency = vImg.getTransparency();
        D3DGraphicsDevice gd = (D3DGraphicsDevice)vImg.getGraphicsConfig().getDevice();
        this.accelerationEnabled = transparency == 1 || transparency == 3 && (gd.isCapPresent(2) || gd.isCapPresent(4));
    }

    @Override
    protected boolean isAccelerationEnabled() {
        return this.accelerationEnabled;
    }

    public void setAccelerationEnabled(boolean accelerationEnabled) {
        this.accelerationEnabled = accelerationEnabled;
    }

    @Override
    protected SurfaceData initAcceleratedSurface() {
        D3DSurfaceData sData;
        Component comp = this.vImg.getComponent();
        AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
        WComponentPeer peer = comp != null ? (WComponentPeer)acc.getPeer(comp) : null;
        try {
            boolean forceback = false;
            if (this.context instanceof Boolean) {
                forceback = (Boolean)this.context;
            }
            if (forceback) {
                sData = D3DSurfaceData.createData(peer, this.vImg);
            } else {
                D3DGraphicsConfig gc = (D3DGraphicsConfig)this.vImg.getGraphicsConfig();
                ColorModel cm = gc.getColorModel(this.vImg.getTransparency());
                int type = this.vImg.getForcedAccelSurfaceType();
                if (type == 0) {
                    type = 5;
                }
                sData = D3DSurfaceData.createData(gc, this.vImg.getWidth(), this.vImg.getHeight(), cm, this.vImg, type);
            }
        }
        catch (NullPointerException | OutOfMemoryError | InvalidPipeException e) {
            sData = null;
        }
        return sData;
    }

    @Override
    protected boolean isConfigValid(GraphicsConfiguration gc) {
        return gc == null || gc == this.vImg.getGraphicsConfig();
    }

    private synchronized void setRestoreCountdown(int count) {
        this.restoreCountdown = count;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void restoreAcceleratedSurface() {
        D3DVolatileSurfaceManager d3DVolatileSurfaceManager = this;
        synchronized (d3DVolatileSurfaceManager) {
            if (this.restoreCountdown > 0) {
                --this.restoreCountdown;
                throw new InvalidPipeException("Will attempt to restore surface  in " + this.restoreCountdown);
            }
        }
        SurfaceData sData = this.initAcceleratedSurface();
        if (sData == null) {
            throw new InvalidPipeException("could not restore surface");
        }
        this.sdAccel = sData;
    }

    static void handleVItoScreenOp(SurfaceData src, SurfaceData dst) {
        D3DVolatileSurfaceManager vsm;
        D3DSurfaceData d3dsd;
        SurfaceManager mgr;
        if (src instanceof D3DSurfaceData && dst instanceof GDIWindowSurfaceData && (mgr = SurfaceManager.getManager((Image)(d3dsd = (D3DSurfaceData)src).getDestination())) instanceof D3DVolatileSurfaceManager && (vsm = (D3DVolatileSurfaceManager)mgr) != null) {
            d3dsd.setSurfaceLost(true);
            GDIWindowSurfaceData wsd = (GDIWindowSurfaceData)dst;
            WComponentPeer p = wsd.getPeer();
            if (D3DScreenUpdateManager.canUseD3DOnScreen(p, (Win32GraphicsConfig)p.getGraphicsConfiguration(), p.getBackBuffersNum())) {
                vsm.setRestoreCountdown(10);
            } else {
                vsm.setAccelerationEnabled(false);
            }
        }
    }

    @Override
    public void initContents() {
        if (this.vImg.getForcedAccelSurfaceType() != 3) {
            super.initContents();
        }
    }
}

