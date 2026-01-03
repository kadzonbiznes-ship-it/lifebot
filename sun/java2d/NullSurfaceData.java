/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import sun.java2d.InvalidPipeException;
import sun.java2d.StateTrackable;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.NullPipe;

public class NullSurfaceData
extends SurfaceData {
    public static final SurfaceData theInstance = new NullSurfaceData();
    private static final NullPipe nullpipe = new NullPipe();

    private NullSurfaceData() {
        super(StateTrackable.State.IMMUTABLE, SurfaceType.Any, ColorModel.getRGBdefault());
    }

    @Override
    public void invalidate() {
    }

    @Override
    public SurfaceData getReplacement() {
        return this;
    }

    @Override
    public void validatePipe(SunGraphics2D sg2d) {
        sg2d.drawpipe = nullpipe;
        sg2d.fillpipe = nullpipe;
        sg2d.shapepipe = nullpipe;
        sg2d.textpipe = nullpipe;
        sg2d.imagepipe = nullpipe;
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h) {
        throw new InvalidPipeException("should be NOP");
    }

    @Override
    public boolean useTightBBoxes() {
        return false;
    }

    @Override
    public int pixelFor(int rgb) {
        return rgb;
    }

    @Override
    public int rgbFor(int pixel) {
        return pixel;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle();
    }

    @Override
    protected void checkCustomComposite() {
    }

    @Override
    public boolean copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
        return true;
    }

    @Override
    public Object getDestination() {
        return null;
    }
}

