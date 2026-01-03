/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.security.AccessController;
import java.util.Locale;
import sun.awt.PlatformGraphicsInfo;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.java2d.HeadlessGraphicsEnvironment;
import sun.java2d.SunGraphicsEnvironment;

public abstract class GraphicsEnvironment {
    private static Boolean headless;
    private static Boolean defaultHeadless;

    protected GraphicsEnvironment() {
    }

    public static GraphicsEnvironment getLocalGraphicsEnvironment() {
        return LocalGE.INSTANCE;
    }

    public static boolean isHeadless() {
        return GraphicsEnvironment.getHeadlessProperty();
    }

    static String getHeadlessMessage() {
        if (headless == null) {
            GraphicsEnvironment.getHeadlessProperty();
        }
        return defaultHeadless != Boolean.TRUE ? null : PlatformGraphicsInfo.getDefaultHeadlessMessage();
    }

    private static boolean getHeadlessProperty() {
        if (headless == null) {
            AccessController.doPrivileged(() -> {
                String nm = System.getProperty("java.awt.headless");
                headless = nm == null ? (defaultHeadless = Boolean.valueOf(PlatformGraphicsInfo.getDefaultHeadlessProperty())) : Boolean.valueOf(nm);
                return null;
            });
        }
        return headless;
    }

    static void checkHeadless() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
    }

    public boolean isHeadlessInstance() {
        return GraphicsEnvironment.getHeadlessProperty();
    }

    public abstract GraphicsDevice[] getScreenDevices() throws HeadlessException;

    public abstract GraphicsDevice getDefaultScreenDevice() throws HeadlessException;

    public abstract Graphics2D createGraphics(BufferedImage var1);

    public abstract Font[] getAllFonts();

    public abstract String[] getAvailableFontFamilyNames();

    public abstract String[] getAvailableFontFamilyNames(Locale var1);

    public boolean registerFont(Font font) {
        if (font == null) {
            throw new NullPointerException("font cannot be null.");
        }
        FontManager fm = FontManagerFactory.getInstance();
        return fm.registerFont(font);
    }

    public void preferLocaleFonts() {
        FontManager fm = FontManagerFactory.getInstance();
        fm.preferLocaleFonts();
    }

    public void preferProportionalFonts() {
        FontManager fm = FontManagerFactory.getInstance();
        fm.preferProportionalFonts();
    }

    public Point getCenterPoint() throws HeadlessException {
        Rectangle usableBounds = SunGraphicsEnvironment.getUsableBounds(this.getDefaultScreenDevice());
        return new Point(usableBounds.width / 2 + usableBounds.x, usableBounds.height / 2 + usableBounds.y);
    }

    public Rectangle getMaximumWindowBounds() throws HeadlessException {
        return SunGraphicsEnvironment.getUsableBounds(this.getDefaultScreenDevice());
    }

    private static final class LocalGE {
        static final GraphicsEnvironment INSTANCE = LocalGE.createGE();

        private LocalGE() {
        }

        private static GraphicsEnvironment createGE() {
            GraphicsEnvironment ge = PlatformGraphicsInfo.createGE();
            if (GraphicsEnvironment.isHeadless()) {
                ge = new HeadlessGraphicsEnvironment(ge);
            }
            return ge;
        }
    }
}

