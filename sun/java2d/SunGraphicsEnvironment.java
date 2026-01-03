/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Locale;
import java.util.TreeMap;
import sun.awt.DisplayChangedListener;
import sun.awt.SunDisplayChanger;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontManagerForSGE;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;

public abstract class SunGraphicsEnvironment
extends GraphicsEnvironment
implements DisplayChangedListener {
    private final Font defaultFont = new Font("Dialog", 0, 12);
    private static final boolean uiScaleEnabled = "true".equals(AccessController.doPrivileged(new GetPropertyAction("sun.java2d.uiScale.enabled", "true")));
    private static final double debugScale = uiScaleEnabled ? SunGraphicsEnvironment.getScaleFactor("sun.java2d.uiScale") : -1.0;
    protected GraphicsDevice[] screens;
    protected SunDisplayChanger displayChanger = new SunDisplayChanger();

    @Override
    public synchronized GraphicsDevice[] getScreenDevices() {
        GraphicsDevice[] ret = this.screens;
        if (ret == null) {
            int num = this.getNumScreens();
            ret = new GraphicsDevice[num];
            for (int i = 0; i < num; ++i) {
                ret[i] = this.makeScreenDevice(i);
            }
            this.screens = ret;
        }
        return ret;
    }

    protected abstract int getNumScreens();

    protected abstract GraphicsDevice makeScreenDevice(int var1);

    @Override
    public GraphicsDevice getDefaultScreenDevice() {
        GraphicsDevice[] screens = this.getScreenDevices();
        if (screens.length == 0) {
            throw new AWTError("no screen devices");
        }
        return screens[0];
    }

    @Override
    public Graphics2D createGraphics(BufferedImage img) {
        if (img == null) {
            throw new NullPointerException("BufferedImage cannot be null");
        }
        SurfaceData sd = SurfaceData.getPrimarySurfaceData(img);
        return new SunGraphics2D(sd, Color.white, Color.black, this.defaultFont);
    }

    public static FontManagerForSGE getFontManagerForSGE() {
        FontManager fm = FontManagerFactory.getInstance();
        return (FontManagerForSGE)fm;
    }

    @Deprecated
    public static void useAlternateFontforJALocales() {
        SunGraphicsEnvironment.getFontManagerForSGE().useAlternateFontforJALocales();
    }

    @Override
    public Font[] getAllFonts() {
        FontManagerForSGE fm = SunGraphicsEnvironment.getFontManagerForSGE();
        Font[] installedFonts = fm.getAllInstalledFonts();
        Font[] created = fm.getCreatedFonts();
        if (created == null || created.length == 0) {
            return installedFonts;
        }
        int newlen = installedFonts.length + created.length;
        Font[] fonts = Arrays.copyOf(installedFonts, newlen);
        System.arraycopy(created, 0, fonts, installedFonts.length, created.length);
        return fonts;
    }

    @Override
    public String[] getAvailableFontFamilyNames(Locale requestedLocale) {
        FontManagerForSGE fm = SunGraphicsEnvironment.getFontManagerForSGE();
        String[] installed = fm.getInstalledFontFamilyNames(requestedLocale);
        TreeMap<String, String> map = fm.getCreatedFontFamilyNames();
        if (map == null || map.size() == 0) {
            return installed;
        }
        for (int i = 0; i < installed.length; ++i) {
            map.put(installed[i].toLowerCase(requestedLocale), installed[i]);
        }
        String[] retval = map.values().toArray(new String[0]);
        return retval;
    }

    @Override
    public String[] getAvailableFontFamilyNames() {
        return this.getAvailableFontFamilyNames(Locale.getDefault());
    }

    public static Rectangle getUsableBounds(GraphicsDevice gd) {
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle usableBounds = gc.getBounds();
        usableBounds.x += insets.left;
        usableBounds.y += insets.top;
        usableBounds.width -= insets.left + insets.right;
        usableBounds.height -= insets.top + insets.bottom;
        return usableBounds;
    }

    @Override
    public void displayChanged() {
        for (GraphicsDevice gd : this.getScreenDevices()) {
            if (!(gd instanceof DisplayChangedListener)) continue;
            ((DisplayChangedListener)((Object)gd)).displayChanged();
        }
        this.displayChanger.notifyListeners();
    }

    @Override
    public void paletteChanged() {
        this.displayChanger.notifyPaletteChanged();
    }

    public abstract boolean isDisplayLocal();

    public void addDisplayChangedListener(DisplayChangedListener client) {
        this.displayChanger.add(client);
    }

    public void removeDisplayChangedListener(DisplayChangedListener client) {
        this.displayChanger.remove(client);
    }

    public boolean isFlipStrategyPreferred(ComponentPeer peer) {
        return false;
    }

    public static boolean isUIScaleEnabled() {
        return uiScaleEnabled;
    }

    public static double getDebugScale() {
        return debugScale;
    }

    public static double getScaleFactor(String propertyName) {
        String scaleFactor = AccessController.doPrivileged(new GetPropertyAction(propertyName, "-1"));
        if (scaleFactor == null || scaleFactor.equals("-1")) {
            return -1.0;
        }
        try {
            double units = 1.0;
            if (scaleFactor.endsWith("x")) {
                scaleFactor = scaleFactor.substring(0, scaleFactor.length() - 1);
            } else if (scaleFactor.endsWith("dpi")) {
                units = 96.0;
                scaleFactor = scaleFactor.substring(0, scaleFactor.length() - 3);
            } else if (scaleFactor.endsWith("%")) {
                units = 100.0;
                scaleFactor = scaleFactor.substring(0, scaleFactor.length() - 1);
            }
            double scale = Double.parseDouble(scaleFactor);
            return scale <= 0.0 ? -1.0 : scale / units;
        }
        catch (NumberFormatException ignored) {
            return -1.0;
        }
    }

    public static GraphicsConfiguration getGraphicsConfigurationAtPoint(GraphicsConfiguration current, double x, double y) {
        if (current.getBounds().contains(x, y)) {
            return current;
        }
        GraphicsEnvironment env = SunGraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : env.getScreenDevices()) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            if (!config.getBounds().contains(x, y)) continue;
            return config;
        }
        return current;
    }

    public static Rectangle getGCDeviceBounds(GraphicsConfiguration config) {
        AffineTransform tx = config.getDefaultTransform();
        Rectangle bounds = config.getBounds();
        bounds.width = (int)((double)bounds.width * tx.getScaleX());
        bounds.height = (int)((double)bounds.height * tx.getScaleY());
        return bounds;
    }

    public static Dimension toUserSpace(GraphicsConfiguration gc, int w, int h) {
        AffineTransform tx = gc.getDefaultTransform();
        return new Dimension(Region.clipRound((double)w / tx.getScaleX()), Region.clipRound((double)h / tx.getScaleY()));
    }

    public static Point toDeviceSpaceAbs(int x, int y) {
        GraphicsConfiguration gc = SunGraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        gc = SunGraphicsEnvironment.getGraphicsConfigurationAtPoint(gc, x, y);
        return SunGraphicsEnvironment.toDeviceSpaceAbs(gc, x, y, 0, 0).getLocation();
    }

    public static Rectangle toDeviceSpaceAbs(Rectangle rect) {
        GraphicsConfiguration gc = SunGraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        gc = SunGraphicsEnvironment.getGraphicsConfigurationAtPoint(gc, rect.x, rect.y);
        return SunGraphicsEnvironment.toDeviceSpaceAbs(gc, rect.x, rect.y, rect.width, rect.height);
    }

    public static Rectangle toDeviceSpaceAbs(GraphicsConfiguration gc, int x, int y, int w, int h) {
        AffineTransform tx = gc.getDefaultTransform();
        Rectangle screen = gc.getBounds();
        return new Rectangle(screen.x + Region.clipRound((double)(x - screen.x) * tx.getScaleX()), screen.y + Region.clipRound((double)(y - screen.y) * tx.getScaleY()), Region.clipRound((double)w * tx.getScaleX()), Region.clipRound((double)h * tx.getScaleY()));
    }

    public static Point toDeviceSpace(int x, int y) {
        GraphicsConfiguration gc = SunGraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        gc = SunGraphicsEnvironment.getGraphicsConfigurationAtPoint(gc, x, y);
        return SunGraphicsEnvironment.toDeviceSpace(gc, x, y, 0, 0).getLocation();
    }

    public static Rectangle toDeviceSpace(GraphicsConfiguration gc, int x, int y, int w, int h) {
        AffineTransform tx = gc.getDefaultTransform();
        return new Rectangle(Region.clipRound((double)x * tx.getScaleX()), Region.clipRound((double)y * tx.getScaleY()), Region.clipRound((double)w * tx.getScaleX()), Region.clipRound((double)h * tx.getScaleY()));
    }
}

