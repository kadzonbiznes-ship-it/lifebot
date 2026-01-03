/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.awt.GraphicsConfiguration;
import sun.awt.image.BufImgVolatileSurfaceManager;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.VolatileSurfaceManager;
import sun.java2d.SurfaceManagerFactory;
import sun.java2d.d3d.D3DGraphicsConfig;
import sun.java2d.d3d.D3DVolatileSurfaceManager;
import sun.java2d.opengl.WGLGraphicsConfig;
import sun.java2d.opengl.WGLVolatileSurfaceManager;

public class WindowsSurfaceManagerFactory
extends SurfaceManagerFactory {
    @Override
    public VolatileSurfaceManager createVolatileManager(SunVolatileImage vImg, Object context) {
        GraphicsConfiguration gc = vImg.getGraphicsConfig();
        if (gc instanceof D3DGraphicsConfig) {
            return new D3DVolatileSurfaceManager(vImg, context);
        }
        if (gc instanceof WGLGraphicsConfig) {
            return new WGLVolatileSurfaceManager(vImg, context);
        }
        return new BufImgVolatileSurfaceManager(vImg, context);
    }
}

