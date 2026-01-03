/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.ColorModel;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

public abstract class GraphicsDevice {
    private Window fullScreenWindow;
    private AppContext fullScreenAppContext;
    private final Object fsAppContextLock = new Object();
    private Rectangle windowedModeBounds;
    public static final int TYPE_RASTER_SCREEN = 0;
    public static final int TYPE_PRINTER = 1;
    public static final int TYPE_IMAGE_BUFFER = 2;

    protected GraphicsDevice() {
    }

    public abstract int getType();

    public abstract String getIDstring();

    public abstract GraphicsConfiguration[] getConfigurations();

    public abstract GraphicsConfiguration getDefaultConfiguration();

    public GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate gct) {
        GraphicsConfiguration[] configs = this.getConfigurations();
        return gct.getBestConfiguration(configs);
    }

    public boolean isFullScreenSupported() {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFullScreenWindow(Window w) {
        Object gc;
        if (w != null) {
            if (w.getShape() != null) {
                w.setShape(null);
            }
            if (w.getOpacity() < 1.0f) {
                w.setOpacity(1.0f);
            }
            if (!w.isOpaque()) {
                Color bgColor = w.getBackground();
                bgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 255);
                w.setBackground(bgColor);
            }
            if ((gc = w.getGraphicsConfiguration()) != null && ((GraphicsConfiguration)gc).getDevice() != this && ((GraphicsConfiguration)gc).getDevice().getFullScreenWindow() == w) {
                ((GraphicsConfiguration)gc).getDevice().setFullScreenWindow(null);
            }
        }
        if (this.fullScreenWindow != null && this.windowedModeBounds != null) {
            if (this.windowedModeBounds.width == 0) {
                this.windowedModeBounds.width = 1;
            }
            if (this.windowedModeBounds.height == 0) {
                this.windowedModeBounds.height = 1;
            }
            this.fullScreenWindow.setBounds(this.windowedModeBounds);
        }
        gc = this.fsAppContextLock;
        synchronized (gc) {
            this.fullScreenAppContext = w == null ? null : AppContext.getAppContext();
            this.fullScreenWindow = w;
        }
        if (this.fullScreenWindow != null) {
            this.windowedModeBounds = this.fullScreenWindow.getBounds();
            gc = this.getDefaultConfiguration();
            Rectangle screenBounds = ((GraphicsConfiguration)gc).getBounds();
            if (SunToolkit.isDispatchThreadForAppContext(this.fullScreenWindow)) {
                this.fullScreenWindow.setGraphicsConfiguration((GraphicsConfiguration)gc);
            }
            this.fullScreenWindow.setBounds(screenBounds.x, screenBounds.y, screenBounds.width, screenBounds.height);
            this.fullScreenWindow.setVisible(true);
            this.fullScreenWindow.toFront();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Window getFullScreenWindow() {
        Window returnWindow = null;
        Object object = this.fsAppContextLock;
        synchronized (object) {
            if (this.fullScreenAppContext == AppContext.getAppContext()) {
                returnWindow = this.fullScreenWindow;
            }
        }
        return returnWindow;
    }

    public boolean isDisplayChangeSupported() {
        return false;
    }

    public void setDisplayMode(DisplayMode dm) {
        throw new UnsupportedOperationException("Cannot change display mode");
    }

    public DisplayMode getDisplayMode() {
        GraphicsConfiguration gc = this.getDefaultConfiguration();
        Rectangle r = gc.getBounds();
        ColorModel cm = gc.getColorModel();
        return new DisplayMode(r.width, r.height, cm.getPixelSize(), 0);
    }

    public DisplayMode[] getDisplayModes() {
        return new DisplayMode[]{this.getDisplayMode()};
    }

    public int getAvailableAcceleratedMemory() {
        return -1;
    }

    public boolean isWindowTranslucencySupported(WindowTranslucency translucencyKind) {
        switch (translucencyKind.ordinal()) {
            case 0: {
                return GraphicsDevice.isWindowShapingSupported();
            }
            case 1: {
                return GraphicsDevice.isWindowOpacitySupported();
            }
            case 2: {
                return this.isWindowPerpixelTranslucencySupported();
            }
        }
        return false;
    }

    static boolean isWindowShapingSupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowShapingSupported();
    }

    static boolean isWindowOpacitySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowOpacitySupported();
    }

    boolean isWindowPerpixelTranslucencySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        if (!((SunToolkit)curToolkit).isWindowTranslucencySupported()) {
            return false;
        }
        return this.getTranslucencyCapableGC() != null;
    }

    GraphicsConfiguration getTranslucencyCapableGC() {
        GraphicsConfiguration defaultGC = this.getDefaultConfiguration();
        if (defaultGC.isTranslucencyCapable()) {
            return defaultGC;
        }
        GraphicsConfiguration[] configs = this.getConfigurations();
        for (int j = 0; j < configs.length; ++j) {
            if (!configs[j].isTranslucencyCapable()) continue;
            return configs[j];
        }
        return null;
    }

    public static enum WindowTranslucency {
        PERPIXEL_TRANSPARENT,
        TRANSLUCENT,
        PERPIXEL_TRANSLUCENT;

    }
}

