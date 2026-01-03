/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import sun.font.CompositeFont;
import sun.font.Font2D;
import sun.font.FontStrike;
import sun.font.FontUtilities;
import sun.font.StrikeMetrics;
import sun.font.SunFontManager;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public final class FontDesignMetrics
extends FontMetrics {
    private static final long serialVersionUID = 4480069578560887773L;
    private static final float UNKNOWN_WIDTH = -1.0f;
    private static final int CURRENT_VERSION = 1;
    private static float roundingUpValue = 0.95f;
    private Font font;
    private float ascent;
    private float descent;
    private float leading;
    private float maxAdvance;
    private double[] matrix;
    private int[] cache;
    private int serVersion = 0;
    private boolean isAntiAliased;
    private boolean usesFractionalMetrics;
    private AffineTransform frcTx;
    private transient float[] advCache;
    private transient int height = -1;
    private transient FontRenderContext frc;
    private transient double[] devmatrix = null;
    private transient FontStrike fontStrike;
    private static FontRenderContext DEFAULT_FRC = null;
    private static final ConcurrentHashMap<Object, KeyReference> metricsCache = new ConcurrentHashMap();
    private static final int MAXRECENT = 5;
    private static final FontDesignMetrics[] recentMetrics = new FontDesignMetrics[5];
    private static int recentIndex = 0;

    private static FontRenderContext getDefaultFrc() {
        if (DEFAULT_FRC == null) {
            AffineTransform tx = GraphicsEnvironment.isHeadless() ? new AffineTransform() : GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
            DEFAULT_FRC = new FontRenderContext(tx, false, false);
        }
        return DEFAULT_FRC;
    }

    public static FontDesignMetrics getMetrics(Font font) {
        return FontDesignMetrics.getMetrics(font, FontDesignMetrics.getDefaultFrc());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static FontDesignMetrics getMetrics(Font font, FontRenderContext frc) {
        KeyReference r;
        SunFontManager fm = SunFontManager.getInstance();
        if (fm.usingAlternateCompositeFonts() && FontUtilities.getFont2D(font) instanceof CompositeFont) {
            return new FontDesignMetrics(font, frc);
        }
        FontDesignMetrics m = null;
        boolean usefontkey = frc.equals(FontDesignMetrics.getDefaultFrc());
        if (usefontkey) {
            r = metricsCache.get(font);
        } else {
            Class<MetricsKey> clazz = MetricsKey.class;
            // MONITORENTER : sun.font.FontDesignMetrics$MetricsKey.class
            MetricsKey.key.init(font, frc);
            r = metricsCache.get(MetricsKey.key);
            // MONITOREXIT : clazz
        }
        if (r != null) {
            m = (FontDesignMetrics)r.get();
        }
        if (m == null) {
            m = new FontDesignMetrics(font, frc);
            if (usefontkey) {
                metricsCache.put(font, new KeyReference(font, m));
            } else {
                MetricsKey newKey = new MetricsKey(font, frc);
                metricsCache.put(newKey, new KeyReference(newKey, m));
            }
        }
        for (int i = 0; i < recentMetrics.length; ++i) {
            if (recentMetrics[i] != m) continue;
            return m;
        }
        FontDesignMetrics[] fontDesignMetricsArray = recentMetrics;
        // MONITORENTER : recentMetrics
        FontDesignMetrics.recentMetrics[FontDesignMetrics.recentIndex++] = m;
        if (recentIndex == 5) {
            recentIndex = 0;
        }
        // MONITOREXIT : fontDesignMetricsArray
        return m;
    }

    private FontDesignMetrics(Font font) {
        this(font, FontDesignMetrics.getDefaultFrc());
    }

    private FontDesignMetrics(Font font, FontRenderContext frc) {
        super(font);
        this.font = font;
        this.frc = frc;
        this.isAntiAliased = frc.isAntiAliased();
        this.usesFractionalMetrics = frc.usesFractionalMetrics();
        this.frcTx = frc.getTransform();
        this.matrix = new double[4];
        this.initMatrixAndMetrics();
        this.initAdvCache();
    }

    private void initMatrixAndMetrics() {
        Font2D font2D = FontUtilities.getFont2D(this.font);
        this.fontStrike = font2D.getStrike(this.font, this.frc);
        StrikeMetrics metrics = this.fontStrike.getFontMetrics();
        this.ascent = metrics.getAscent();
        this.descent = metrics.getDescent();
        this.leading = metrics.getLeading();
        this.maxAdvance = metrics.getMaxAdvance();
        this.devmatrix = new double[4];
        this.frcTx.getMatrix(this.devmatrix);
    }

    private void initAdvCache() {
        this.advCache = new float[256];
        for (int i = 0; i < 256; ++i) {
            this.advCache[i] = -1.0f;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.serVersion != 1) {
            this.frc = FontDesignMetrics.getDefaultFrc();
            this.isAntiAliased = this.frc.isAntiAliased();
            this.usesFractionalMetrics = this.frc.usesFractionalMetrics();
            this.frcTx = this.frc.getTransform();
        } else {
            this.frc = new FontRenderContext(this.frcTx, this.isAntiAliased, this.usesFractionalMetrics);
        }
        this.height = -1;
        this.cache = null;
        this.initMatrixAndMetrics();
        this.initAdvCache();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.cache = new int[256];
        for (int i = 0; i < 256; ++i) {
            this.cache[i] = -1;
        }
        this.serVersion = 1;
        out.defaultWriteObject();
        this.cache = null;
    }

    private float handleCharWidth(int ch) {
        return this.fontStrike.getCodePointAdvance(ch);
    }

    private float getLatinCharWidth(char ch) {
        float w = this.advCache[ch];
        if (w == -1.0f) {
            this.advCache[ch] = w = this.handleCharWidth(ch);
        }
        return w;
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return this.frc;
    }

    @Override
    public int charWidth(char ch) {
        float w = ch < '\u0100' ? this.getLatinCharWidth(ch) : this.handleCharWidth(ch);
        return (int)(0.5 + (double)w);
    }

    @Override
    public int charWidth(int ch) {
        if (!Character.isValidCodePoint(ch)) {
            ch = 65535;
        }
        float w = this.handleCharWidth(ch);
        return (int)(0.5 + (double)w);
    }

    @Override
    public int stringWidth(String str) {
        float width = 0.0f;
        if (this.font.hasLayoutAttributes()) {
            if (str == null) {
                throw new NullPointerException("str is null");
            }
            if (str.length() == 0) {
                return 0;
            }
            width = new TextLayout(str, this.font, this.frc).getAdvance();
        } else {
            int length = str.length();
            for (int i = 0; i < length; ++i) {
                char ch = str.charAt(i);
                if (ch < '\u0100') {
                    width += this.getLatinCharWidth(ch);
                    continue;
                }
                if (FontUtilities.isNonSimpleChar(ch)) {
                    width = new TextLayout(str, this.font, this.frc).getAdvance();
                    break;
                }
                width += this.handleCharWidth(ch);
            }
        }
        return (int)(0.5 + (double)width);
    }

    @Override
    public int charsWidth(char[] data, int off, int len) {
        float width = 0.0f;
        if (this.font.hasLayoutAttributes()) {
            if (len == 0) {
                return 0;
            }
            String str = new String(data, off, len);
            width = new TextLayout(str, this.font, this.frc).getAdvance();
        } else {
            if (len < 0) {
                throw new IndexOutOfBoundsException("len=" + len);
            }
            int limit = off + len;
            for (int i = off; i < limit; ++i) {
                char ch = data[i];
                if (ch < '\u0100') {
                    width += this.getLatinCharWidth(ch);
                    continue;
                }
                if (FontUtilities.isNonSimpleChar(ch)) {
                    String str = new String(data, off, len);
                    width = new TextLayout(str, this.font, this.frc).getAdvance();
                    break;
                }
                width += this.handleCharWidth(ch);
            }
        }
        return (int)(0.5 + (double)width);
    }

    public Rectangle2D getSimpleBounds(char[] data, int off, int len) {
        float width = 0.0f;
        int limit = off + len;
        for (int i = off; i < limit; ++i) {
            char ch = data[i];
            if (ch < '\u0100') {
                width += this.getLatinCharWidth(ch);
                continue;
            }
            width += this.handleCharWidth(ch);
        }
        float height = this.ascent + this.descent + this.leading;
        return new Rectangle2D.Float(0.0f, -this.ascent, width, height);
    }

    @Override
    public int[] getWidths() {
        int[] widths = new int[256];
        for (int ch = 0; ch < 256; ch = (int)((char)(ch + 1))) {
            float w = this.advCache[ch];
            if (w == -1.0f) {
                w = this.advCache[ch] = this.handleCharWidth(ch);
            }
            widths[ch] = (int)(0.5 + (double)w);
        }
        return widths;
    }

    @Override
    public int getMaxAdvance() {
        return (int)(0.99f + this.maxAdvance);
    }

    @Override
    public int getAscent() {
        return (int)(roundingUpValue + this.ascent);
    }

    @Override
    public int getDescent() {
        return (int)(roundingUpValue + this.descent);
    }

    @Override
    public int getLeading() {
        return (int)(roundingUpValue + this.descent + this.leading) - (int)(roundingUpValue + this.descent);
    }

    @Override
    public int getHeight() {
        if (this.height < 0) {
            this.height = this.getAscent() + (int)(roundingUpValue + this.descent + this.leading);
        }
        return this.height;
    }

    private static class KeyReference
    extends SoftReference<Object>
    implements DisposerRecord,
    Disposer.PollDisposable {
        static ReferenceQueue<Object> queue = Disposer.getQueue();
        Object key;

        KeyReference(Object key, Object value) {
            super(value, queue);
            this.key = key;
            Disposer.addReference(this, this);
        }

        @Override
        public void dispose() {
            metricsCache.remove(this.key, this);
        }
    }

    private static class MetricsKey {
        Font font;
        FontRenderContext frc;
        int hash;
        static final MetricsKey key = new MetricsKey();

        MetricsKey() {
        }

        MetricsKey(Font font, FontRenderContext frc) {
            this.init(font, frc);
        }

        void init(Font font, FontRenderContext frc) {
            this.font = font;
            this.frc = frc;
            this.hash = font.hashCode() + frc.hashCode();
        }

        public boolean equals(Object key) {
            if (!(key instanceof MetricsKey)) {
                return false;
            }
            return this.font.equals(((MetricsKey)key).font) && this.frc.equals(((MetricsKey)key).frc);
        }

        public int hashCode() {
            return this.hash;
        }
    }
}

