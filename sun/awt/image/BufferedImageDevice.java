/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import sun.awt.image.BufferedImageGraphicsConfig;

public final class BufferedImageDevice
extends GraphicsDevice {
    private final GraphicsConfiguration config;

    public BufferedImageDevice(BufferedImageGraphicsConfig config) {
        this.config = config;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public String getIDstring() {
        return "BufferedImage";
    }

    @Override
    public GraphicsConfiguration[] getConfigurations() {
        return new GraphicsConfiguration[]{this.config};
    }

    @Override
    public GraphicsConfiguration getDefaultConfiguration() {
        return this.config;
    }
}

