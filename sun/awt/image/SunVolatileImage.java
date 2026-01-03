/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;
import sun.awt.image.BufImgVolatileSurfaceManager;
import sun.awt.image.BufferedImageGraphicsConfig;
import sun.awt.image.SurfaceManager;
import sun.awt.image.VolatileSurfaceManager;
import sun.java2d.DestSurfaceProvider;
import sun.java2d.SunGraphics2D;
import sun.java2d.Surface;
import sun.java2d.SurfaceManagerFactory;
import sun.java2d.pipe.Region;
import sun.print.PrinterGraphicsConfig;

public class SunVolatileImage
extends VolatileImage
implements DestSurfaceProvider {
    protected VolatileSurfaceManager volSurfaceManager;
    protected Component comp;
    private GraphicsConfiguration graphicsConfig;
    private Font defaultFont;
    private int width;
    private int height;
    private int forcedAccelSurfaceType;

    protected SunVolatileImage(Component comp, GraphicsConfiguration graphicsConfig, int width, int height, Object context, int transparency, ImageCapabilities caps, int accType) {
        this.comp = comp;
        this.graphicsConfig = graphicsConfig;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width (" + width + ") and height (" + height + ") cannot be <= 0");
        }
        this.width = width;
        this.height = height;
        this.forcedAccelSurfaceType = accType;
        if (transparency != 1 && transparency != 2 && transparency != 3) {
            throw new IllegalArgumentException("Unknown transparency type:" + transparency);
        }
        this.transparency = transparency;
        this.volSurfaceManager = this.createSurfaceManager(context, caps);
        SurfaceManager.setManager(this, this.volSurfaceManager);
        this.volSurfaceManager.initialize();
        this.volSurfaceManager.initContents();
    }

    private SunVolatileImage(Component comp, GraphicsConfiguration graphicsConfig, int width, int height, Object context, ImageCapabilities caps) {
        this(comp, graphicsConfig, width, height, context, 1, caps, 0);
    }

    public SunVolatileImage(Component comp, int width, int height) {
        this(comp, width, height, null);
    }

    public SunVolatileImage(Component comp, int width, int height, Object context) {
        this(comp, comp.getGraphicsConfiguration(), width, height, context, null);
    }

    public SunVolatileImage(GraphicsConfiguration graphicsConfig, int width, int height, int transparency, ImageCapabilities caps) {
        this(null, graphicsConfig, width, height, null, transparency, caps, 0);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public GraphicsConfiguration getGraphicsConfig() {
        return this.graphicsConfig;
    }

    public void updateGraphicsConfig() {
        GraphicsConfiguration gc;
        if (this.comp != null && (gc = this.comp.getGraphicsConfiguration()) != null) {
            this.graphicsConfig = gc;
        }
    }

    public Component getComponent() {
        return this.comp;
    }

    public int getForcedAccelSurfaceType() {
        return this.forcedAccelSurfaceType;
    }

    protected VolatileSurfaceManager createSurfaceManager(Object context, ImageCapabilities caps) {
        if (this.graphicsConfig instanceof BufferedImageGraphicsConfig || this.graphicsConfig instanceof PrinterGraphicsConfig || caps != null && !caps.isAccelerated()) {
            return new BufImgVolatileSurfaceManager(this, context);
        }
        SurfaceManagerFactory smf = SurfaceManagerFactory.getInstance();
        return smf.createVolatileManager(this, context);
    }

    private Color getForeground() {
        if (this.comp != null) {
            return this.comp.getForeground();
        }
        return Color.black;
    }

    private Color getBackground() {
        if (this.comp != null) {
            return this.comp.getBackground();
        }
        return Color.white;
    }

    private Font getFont() {
        if (this.comp != null) {
            return this.comp.getFont();
        }
        if (this.defaultFont == null) {
            this.defaultFont = new Font("Dialog", 0, 12);
        }
        return this.defaultFont;
    }

    @Override
    public Graphics2D createGraphics() {
        return new SunGraphics2D(this.volSurfaceManager.getPrimarySurfaceData(), this.getForeground(), this.getBackground(), this.getFont());
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
        if (name == null) {
            throw new NullPointerException("null property name is not allowed");
        }
        return Image.UndefinedProperty;
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return this.getWidth();
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return this.getHeight();
    }

    public BufferedImage getBackupImage() {
        return this.getBackupImage(1.0, 1.0);
    }

    public BufferedImage getBackupImage(double scaleX, double scaleY) {
        int w = Region.clipRound((double)this.getWidth() * scaleX);
        int h = Region.clipRound((double)this.getHeight() * scaleY);
        return this.graphicsConfig.createCompatibleImage(w, h, this.getTransparency());
    }

    @Override
    public BufferedImage getSnapshot() {
        BufferedImage bi = this.getBackupImage();
        Graphics2D g = bi.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(this, 0, 0, null);
        g.dispose();
        return bi;
    }

    @Override
    public int validate(GraphicsConfiguration gc) {
        return this.volSurfaceManager.validate(gc);
    }

    @Override
    public boolean contentsLost() {
        return this.volSurfaceManager.contentsLost();
    }

    @Override
    public ImageCapabilities getCapabilities() {
        return this.volSurfaceManager.getCapabilities(this.graphicsConfig);
    }

    @Override
    public Surface getDestSurface() {
        return this.volSurfaceManager.getPrimarySurfaceData();
    }
}

