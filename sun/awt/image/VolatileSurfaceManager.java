/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.ImageCapabilities;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import sun.awt.DisplayChangedListener;
import sun.awt.image.BufImgSurfaceData;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SunWritableRaster;
import sun.awt.image.SurfaceManager;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SurfaceData;

public abstract class VolatileSurfaceManager
extends SurfaceManager
implements DisplayChangedListener {
    protected SunVolatileImage vImg;
    protected AffineTransform atCurrent;
    protected SurfaceData sdAccel;
    protected SurfaceData sdBackup;
    protected SurfaceData sdCurrent;
    protected SurfaceData sdPrevious;
    protected boolean lostSurface;
    protected Object context;

    protected VolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        this.vImg = vImg;
        this.context = context;
        this.atCurrent = vImg.getGraphicsConfig().getDefaultTransform();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            ((SunGraphicsEnvironment)ge).addDisplayChangedListener(this);
        }
    }

    public void initialize() {
        if (this.isAccelerationEnabled()) {
            this.sdAccel = this.initAcceleratedSurface();
            if (this.sdAccel != null) {
                this.sdCurrent = this.sdAccel;
            }
        }
        if (this.sdCurrent == null && this.vImg.getForcedAccelSurfaceType() == 0) {
            this.sdCurrent = this.getBackupSurface();
        }
    }

    @Override
    public SurfaceData getPrimarySurfaceData() {
        return this.sdCurrent;
    }

    protected abstract boolean isAccelerationEnabled();

    public int validate(GraphicsConfiguration gc) {
        int returnCode = 0;
        boolean lostSurfaceTmp = this.lostSurface;
        this.lostSurface = false;
        if (this.isAccelerationEnabled()) {
            if (!this.isConfigValid(gc)) {
                returnCode = 2;
            } else if (this.sdAccel == null) {
                this.sdAccel = this.initAcceleratedSurface();
                if (this.sdAccel != null) {
                    this.sdCurrent = this.sdAccel;
                    this.sdBackup = null;
                    returnCode = 1;
                } else {
                    this.sdCurrent = this.getBackupSurface();
                }
            } else if (this.sdAccel.isSurfaceLost()) {
                try {
                    this.restoreAcceleratedSurface();
                    this.sdCurrent = this.sdAccel;
                    this.sdAccel.setSurfaceLost(false);
                    this.sdBackup = null;
                    returnCode = 1;
                }
                catch (InvalidPipeException e) {
                    this.sdCurrent = this.getBackupSurface();
                }
            } else if (lostSurfaceTmp) {
                returnCode = 1;
            }
        } else if (this.sdAccel != null) {
            this.sdCurrent = this.getBackupSurface();
            this.sdAccel = null;
            returnCode = 1;
        } else if (lostSurfaceTmp) {
            returnCode = 1;
        }
        if (returnCode != 2 && this.sdCurrent != this.sdPrevious) {
            this.sdPrevious = this.sdCurrent;
            returnCode = 1;
        }
        if (returnCode == 1) {
            this.initContents();
        }
        return returnCode;
    }

    public boolean contentsLost() {
        return this.lostSurface;
    }

    protected abstract SurfaceData initAcceleratedSurface();

    protected SurfaceData getBackupSurface() {
        if (this.sdBackup == null) {
            GraphicsConfiguration gc = this.vImg.getGraphicsConfig();
            AffineTransform tx = gc.getDefaultTransform();
            double scaleX = tx.getScaleX();
            double scaleY = tx.getScaleY();
            BufferedImage bImg = this.vImg.getBackupImage(scaleX, scaleY);
            SunWritableRaster.stealTrackable(bImg.getRaster().getDataBuffer()).setUntrackable();
            this.sdBackup = BufImgSurfaceData.createData(bImg, scaleX, scaleY);
        }
        return this.sdBackup;
    }

    public void initContents() {
        if (this.sdCurrent != null) {
            Graphics2D g = this.vImg.createGraphics();
            g.clearRect(0, 0, this.vImg.getWidth(), this.vImg.getHeight());
            g.dispose();
        }
    }

    @Override
    public SurfaceData restoreContents() {
        this.acceleratedSurfaceLost();
        return this.getBackupSurface();
    }

    @Override
    public void acceleratedSurfaceLost() {
        if (this.isAccelerationEnabled() && this.sdCurrent == this.sdAccel) {
            this.lostSurface = true;
        }
    }

    protected void restoreAcceleratedSurface() {
    }

    @Override
    public void displayChanged() {
        this.lostSurface = true;
        if (this.sdAccel != null) {
            this.sdBackup = null;
            SurfaceData oldData = this.sdAccel;
            this.sdAccel = null;
            oldData.invalidate();
            this.sdCurrent = this.getBackupSurface();
        }
        this.vImg.updateGraphicsConfig();
        AffineTransform atUpdated = this.vImg.getGraphicsConfig().getDefaultTransform();
        if (!this.isAccelerationEnabled()) {
            if (!atUpdated.equals(this.atCurrent)) {
                this.sdBackup = null;
                this.sdCurrent = this.getBackupSurface();
            } else {
                this.lostSurface = false;
            }
        }
        this.atCurrent = atUpdated;
    }

    @Override
    public void paletteChanged() {
        this.lostSurface = true;
    }

    protected boolean isConfigValid(GraphicsConfiguration gc) {
        return gc == null || gc.getDevice() == this.vImg.getGraphicsConfig().getDevice();
    }

    @Override
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        if (this.isConfigValid(gc)) {
            return this.isAccelerationEnabled() ? new AcceleratedImageCapabilities() : new ImageCapabilities(false);
        }
        return super.getCapabilities(gc);
    }

    @Override
    public void flush() {
        this.lostSurface = true;
        SurfaceData oldSD = this.sdAccel;
        this.sdAccel = null;
        if (oldSD != null) {
            oldSD.flush();
        }
    }

    private class AcceleratedImageCapabilities
    extends ImageCapabilities {
        AcceleratedImageCapabilities() {
            super(false);
        }

        @Override
        public boolean isAccelerated() {
            return VolatileSurfaceManager.this.sdCurrent == VolatileSurfaceManager.this.sdAccel;
        }

        @Override
        public boolean isTrueVolatile() {
            return this.isAccelerated();
        }
    }
}

