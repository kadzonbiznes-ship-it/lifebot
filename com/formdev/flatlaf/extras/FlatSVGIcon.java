/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.LoggingFacade
 *  com.formdev.flatlaf.util.MultiResolutionImageSupport
 */
package com.formdev.flatlaf.extras;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.GrayFilter;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.MultiResolutionImageSupport;
import com.formdev.flatlaf.util.SoftCache;
import com.formdev.flatlaf.util.UIScale;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.SVGLoader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class FlatSVGIcon
extends ImageIcon
implements FlatLaf.DisabledIconProvider {
    private static final SoftCache<String, SVGDocument> svgCache = new SoftCache();
    private static final SVGLoader svgLoader = new SVGLoader();
    private final String name;
    private final int width;
    private final int height;
    private final float scale;
    private final boolean disabled;
    private final ClassLoader classLoader;
    private final URL url;
    private ColorFilter colorFilter;
    private SVGDocument document;
    private boolean dark;
    private boolean loadFailed;
    private static Boolean darkLaf;

    public FlatSVGIcon(String name) {
        this(name, -1, -1, 1.0f, false, null, null);
    }

    public FlatSVGIcon(String name, ClassLoader classLoader) {
        this(name, -1, -1, 1.0f, false, classLoader, null);
    }

    public FlatSVGIcon(String name, int width, int height) {
        this(name, width, height, 1.0f, false, null, null);
    }

    public FlatSVGIcon(String name, int width, int height, ClassLoader classLoader) {
        this(name, width, height, 1.0f, false, classLoader, null);
    }

    public FlatSVGIcon(String name, float scale) {
        this(name, -1, -1, scale, false, null, null);
    }

    public FlatSVGIcon(String name, float scale, ClassLoader classLoader) {
        this(name, -1, -1, scale, false, classLoader, null);
    }

    public FlatSVGIcon(URL url) {
        this(null, -1, -1, 1.0f, false, null, url);
    }

    public FlatSVGIcon(URI uri) {
        this(null, -1, -1, 1.0f, false, null, FlatSVGIcon.uri2url(uri));
    }

    public FlatSVGIcon(File file) {
        this(null, -1, -1, 1.0f, false, null, FlatSVGIcon.uri2url(file.toURI()));
    }

    public FlatSVGIcon(InputStream in) throws IOException {
        this(null, -1, -1, 1.0f, false, null, null);
        try (InputStream in2 = in;){
            this.document = svgLoader.load(in2);
            if (this.document == null) {
                this.loadFailed = true;
                LoggingFacade.INSTANCE.logSevere("FlatSVGIcon: failed to load SVG icon from input stream", null);
            }
        }
    }

    public FlatSVGIcon(FlatSVGIcon icon) {
        this(icon.name, icon.width, icon.height, icon.scale, icon.disabled, icon.classLoader, icon.url);
        this.colorFilter = icon.colorFilter;
        this.document = icon.document;
        this.dark = icon.dark;
    }

    protected FlatSVGIcon(String name, int width, int height, float scale, boolean disabled, ClassLoader classLoader, URL url) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.disabled = disabled;
        this.classLoader = classLoader;
        this.url = url;
    }

    public String getName() {
        return this.name;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public float getScale() {
        return this.scale;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public FlatSVGIcon derive(int width, int height) {
        if (width == this.width && height == this.height) {
            return this;
        }
        FlatSVGIcon icon = new FlatSVGIcon(this.name, width, height, this.scale, this.disabled, this.classLoader, this.url);
        icon.colorFilter = this.colorFilter;
        icon.document = this.document;
        icon.dark = this.dark;
        return icon;
    }

    public FlatSVGIcon derive(float scale) {
        if (scale == this.scale) {
            return this;
        }
        FlatSVGIcon icon = new FlatSVGIcon(this.name, this.width, this.height, scale, this.disabled, this.classLoader, this.url);
        icon.colorFilter = this.colorFilter;
        icon.document = this.document;
        icon.dark = this.dark;
        return icon;
    }

    @Override
    public Icon getDisabledIcon() {
        if (this.disabled) {
            return this;
        }
        FlatSVGIcon icon = new FlatSVGIcon(this.name, this.width, this.height, this.scale, true, this.classLoader, this.url);
        icon.colorFilter = this.colorFilter;
        icon.document = this.document;
        icon.dark = this.dark;
        return icon;
    }

    public ColorFilter getColorFilter() {
        return this.colorFilter;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
    }

    private void update() {
        if (this.loadFailed) {
            return;
        }
        if (this.dark == FlatSVGIcon.isDarkLaf() && this.document != null) {
            return;
        }
        this.dark = FlatSVGIcon.isDarkLaf();
        if (this.document != null && this.name == null) {
            return;
        }
        URL url = this.url;
        if (url == null) {
            url = this.getIconURL(this.name, this.dark);
            if (url == null && this.dark) {
                url = this.getIconURL(this.name, false);
            }
            if (url == null) {
                this.loadFailed = true;
                LoggingFacade.INSTANCE.logConfig("FlatSVGIcon: resource '" + this.name + "' not found (if using Java modules, check whether icon package is opened in module-info.java)", null);
                return;
            }
        }
        this.document = FlatSVGIcon.loadSVG(url);
        this.loadFailed = this.document == null;
    }

    static synchronized SVGDocument loadSVG(URL url) {
        String cacheKey = url.toString();
        SVGDocument document = svgCache.get(cacheKey);
        if (document != null) {
            return document;
        }
        document = svgLoader.load(url);
        if (document == null) {
            LoggingFacade.INSTANCE.logSevere("FlatSVGIcon: failed to load '" + url + "'", null);
            return null;
        }
        svgCache.put(cacheKey, document);
        return document;
    }

    private URL getIconURL(String name, boolean dark) {
        if (dark) {
            int dotIndex = name.lastIndexOf(46);
            name = name.substring(0, dotIndex) + "_dark" + name.substring(dotIndex);
        }
        ClassLoader cl = this.classLoader != null ? this.classLoader : FlatSVGIcon.class.getClassLoader();
        return cl.getResource(name);
    }

    public boolean hasFound() {
        this.update();
        return this.document != null;
    }

    @Override
    public int getIconWidth() {
        if (this.width > 0) {
            return this.scaleSize(this.width);
        }
        this.update();
        return this.scaleSize(this.document != null ? Math.round(this.document.size().width) : 16);
    }

    @Override
    public int getIconHeight() {
        if (this.height > 0) {
            return this.scaleSize(this.height);
        }
        this.update();
        return this.scaleSize(this.document != null ? Math.round(this.document.size().height) : 16);
    }

    private int scaleSize(int size) {
        int scaledSize = UIScale.scale(size);
        if (this.scale != 1.0f) {
            scaledSize = Math.round((float)scaledSize * this.scale);
        }
        return scaledSize;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        this.update();
        Rectangle clipBounds = g.getClipBounds();
        if (clipBounds != null && !clipBounds.intersects(new Rectangle(x, y, this.getIconWidth(), this.getIconHeight()))) {
            return;
        }
        RGBImageFilter grayFilter = null;
        if (this.disabled) {
            Object grayFilterObj = UIManager.get("Component.grayFilter");
            grayFilter = grayFilterObj instanceof RGBImageFilter ? (RGBImageFilter)grayFilterObj : GrayFilter.createDisabledIconFilter(this.dark);
        }
        GraphicsFilter g2 = new GraphicsFilter((Graphics2D)g.create(), this.colorFilter, ColorFilter.getInstance(), grayFilter);
        try {
            FlatSVGIcon.setRenderingHints(g2);
            this.paintSvg(g2, x, y);
        }
        finally {
            ((Graphics)g2).dispose();
        }
    }

    private void paintSvg(Graphics2D g, int x, int y) {
        if (this.document == null) {
            this.paintSvgError(g, x, y);
            return;
        }
        g.translate(x, y);
        g.clipRect(0, 0, this.getIconWidth(), this.getIconHeight());
        UIScale.scaleGraphics(g);
        if (this.width > 0 || this.height > 0) {
            double sy;
            FloatSize svgSize = this.document.size();
            double sx = this.width > 0 ? (double)((float)this.width / svgSize.width) : 1.0;
            double d = sy = this.height > 0 ? (double)((float)this.height / svgSize.height) : 1.0;
            if (sx != 1.0 || sy != 1.0) {
                g.scale(sx, sy);
            }
        }
        if (this.scale != 1.0f) {
            g.scale(this.scale, this.scale);
        }
        try {
            this.document.render(null, g);
        }
        catch (Exception ex) {
            this.paintSvgError(g, 0, 0);
        }
    }

    private void paintSvgError(Graphics2D g, int x, int y) {
        g.setColor(Color.red);
        g.fillRect(x, y, this.getIconWidth(), this.getIconHeight());
    }

    @Override
    public Image getImage() {
        this.update();
        int iconWidth = this.getIconWidth();
        int iconHeight = this.getIconHeight();
        Dimension[] dimensions = new Dimension[]{new Dimension(iconWidth, iconHeight), new Dimension(iconWidth * 2, iconHeight * 2)};
        Function<Dimension, Image> producer = size -> {
            BufferedImage image = new BufferedImage(size.width, size.height, 2);
            Graphics2D g = image.createGraphics();
            try {
                double sy;
                double sx = size.width > 0 ? (double)((float)size.width / (float)iconWidth) : 1.0;
                double d = sy = size.height > 0 ? (double)((float)size.height / (float)iconHeight) : 1.0;
                if (sx != 1.0 || sy != 1.0) {
                    g.scale(sx, sy);
                }
                this.paintIcon(null, g, 0, 0);
            }
            finally {
                g.dispose();
            }
            return image;
        };
        return MultiResolutionImageSupport.create((int)0, (Dimension[])dimensions, producer);
    }

    static void setRenderingHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    static URL uri2url(URI uri) {
        try {
            return uri.toURL();
        }
        catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static boolean isDarkLaf() {
        if (darkLaf == null) {
            FlatSVGIcon.lafChanged();
            UIManager.addPropertyChangeListener(e -> FlatSVGIcon.lafChanged());
        }
        return darkLaf;
    }

    private static void lafChanged() {
        darkLaf = FlatLaf.isLafDark();
    }

    private static class GraphicsFilter
    extends Graphics2DProxy {
        private final ColorFilter colorFilter;
        private final ColorFilter globalColorFilter;
        private final RGBImageFilter grayFilter;

        GraphicsFilter(Graphics2D delegate, ColorFilter colorFilter, ColorFilter globalColorFilter, RGBImageFilter grayFilter) {
            super(delegate);
            this.colorFilter = colorFilter;
            this.globalColorFilter = globalColorFilter;
            this.grayFilter = grayFilter;
        }

        @Override
        public Graphics create() {
            return new GraphicsFilter((Graphics2D)super.create(), this.colorFilter, this.globalColorFilter, this.grayFilter);
        }

        @Override
        public Graphics create(int x, int y, int width, int height) {
            return new GraphicsFilter((Graphics2D)super.create(x, y, width, height), this.colorFilter, this.globalColorFilter, this.grayFilter);
        }

        @Override
        public void setColor(Color c) {
            super.setColor(this.filterColor(c));
        }

        @Override
        public void setPaint(Paint paint) {
            if (paint instanceof Color) {
                paint = this.filterColor((Color)paint);
            }
            super.setPaint(paint);
        }

        private Color filterColor(Color color) {
            Color newColor;
            color = this.colorFilter != null ? ((newColor = this.colorFilter.filter(color)) != color ? newColor : this.globalColorFilter.filter(color)) : this.globalColorFilter.filter(color);
            if (this.grayFilter != null) {
                int oldRGB = color.getRGB();
                int newRGB = this.grayFilter.filterRGB(0, 0, oldRGB);
                color = newRGB != oldRGB ? new Color(newRGB, true) : color;
            }
            return color;
        }
    }

    public static class ColorFilter {
        private static ColorFilter instance;
        private Map<Integer, String> rgb2keyMap;
        private Map<Color, Color> colorMap;
        private Map<Color, Color> darkColorMap;
        private Function<Color, Color> mapper;

        public static ColorFilter getInstance() {
            if (instance == null) {
                instance = new ColorFilter();
                ColorFilter.instance.rgb2keyMap = new HashMap<Integer, String>();
                for (FlatIconColors c : FlatIconColors.values()) {
                    ColorFilter.instance.rgb2keyMap.put(c.rgb, c.key);
                }
            }
            return instance;
        }

        public ColorFilter() {
        }

        public ColorFilter(Function<Color, Color> mapper) {
            this.setMapper(mapper);
        }

        public Function<Color, Color> getMapper() {
            return this.mapper;
        }

        public void setMapper(Function<Color, Color> mapper) {
            this.mapper = mapper;
        }

        public Map<Color, Color> getLightColorMap() {
            return this.colorMap != null ? Collections.unmodifiableMap(this.colorMap) : Collections.emptyMap();
        }

        public Map<Color, Color> getDarkColorMap() {
            return this.darkColorMap != null ? Collections.unmodifiableMap(this.darkColorMap) : this.getLightColorMap();
        }

        public ColorFilter addAll(Map<Color, Color> from2toMap) {
            this.ensureColorMap();
            this.colorMap.putAll(from2toMap);
            if (this.darkColorMap != null) {
                this.darkColorMap.putAll(from2toMap);
            }
            return this;
        }

        public ColorFilter addAll(Map<Color, Color> from2toLightMap, Map<Color, Color> from2toDarkMap) {
            this.ensureColorMap();
            this.ensureDarkColorMap();
            this.colorMap.putAll(from2toLightMap);
            this.darkColorMap.putAll(from2toDarkMap);
            return this;
        }

        public ColorFilter add(Color from, Color to) {
            this.ensureColorMap();
            this.colorMap.put(from, to);
            if (this.darkColorMap != null) {
                this.darkColorMap.put(from, to);
            }
            return this;
        }

        public ColorFilter add(Color from, Color toLight, Color toDark) {
            this.ensureColorMap();
            this.ensureDarkColorMap();
            if (toLight != null) {
                this.colorMap.put(from, toLight);
            }
            if (toDark != null) {
                this.darkColorMap.put(from, toDark);
            }
            return this;
        }

        public ColorFilter remove(Color from) {
            if (this.colorMap != null) {
                this.colorMap.remove(from);
            }
            if (this.darkColorMap != null) {
                this.darkColorMap.remove(from);
            }
            return this;
        }

        public ColorFilter removeAll() {
            this.colorMap = null;
            this.darkColorMap = null;
            return this;
        }

        private void ensureColorMap() {
            if (this.colorMap == null) {
                this.colorMap = new HashMap<Color, Color>();
            }
        }

        private void ensureDarkColorMap() {
            if (this.darkColorMap == null) {
                this.darkColorMap = new HashMap<Color, Color>(this.colorMap);
            }
        }

        public Color filter(Color color) {
            color = this.applyMappings(color);
            if (this.mapper != null) {
                color = this.mapper.apply(color);
            }
            return color;
        }

        private Color applyMappings(Color color) {
            Map<Color, Color> map;
            Color newColor;
            if (this.colorMap != null && (newColor = (map = this.darkColorMap != null && FlatSVGIcon.isDarkLaf() ? this.darkColorMap : this.colorMap).get(color)) != null) {
                return newColor;
            }
            if (this.rgb2keyMap != null) {
                String colorKey = this.rgb2keyMap.get(color.getRGB() & 0xFFFFFF);
                if (colorKey == null) {
                    return color;
                }
                newColor = UIManager.getColor(colorKey);
                if (newColor == null) {
                    return color;
                }
                return newColor.getAlpha() != color.getAlpha() ? new Color(newColor.getRGB() & 0xFFFFFF | color.getRGB() & 0xFF000000) : newColor;
            }
            return color;
        }

        public static Function<Color, Color> createRGBImageFilterFunction(RGBImageFilter rgbImageFilter) {
            return color -> {
                int oldRGB = color.getRGB();
                int newRGB = rgbImageFilter.filterRGB(0, 0, oldRGB);
                return newRGB != oldRGB ? new Color(newRGB, true) : color;
            };
        }
    }
}

