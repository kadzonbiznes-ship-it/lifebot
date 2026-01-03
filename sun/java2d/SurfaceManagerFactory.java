/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import sun.awt.image.SunVolatileImage;
import sun.awt.image.VolatileSurfaceManager;

public abstract class SurfaceManagerFactory {
    private static SurfaceManagerFactory instance;

    public static synchronized SurfaceManagerFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("No SurfaceManagerFactory set.");
        }
        return instance;
    }

    public static synchronized void setInstance(SurfaceManagerFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must be non-null");
        }
        if (instance != null) {
            throw new IllegalStateException("The surface manager factory is already initialized");
        }
        instance = factory;
    }

    public abstract VolatileSurfaceManager createVolatileManager(SunVolatileImage var1, Object var2);
}

