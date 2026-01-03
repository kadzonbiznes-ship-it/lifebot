/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import sun.awt.SunHints;

public class RenderingHints
implements Map<Object, Object>,
Cloneable {
    HashMap<Object, Object> hintmap = new HashMap(7);
    public static final Key KEY_ANTIALIASING = SunHints.KEY_ANTIALIASING;
    public static final Object VALUE_ANTIALIAS_ON = SunHints.VALUE_ANTIALIAS_ON;
    public static final Object VALUE_ANTIALIAS_OFF = SunHints.VALUE_ANTIALIAS_OFF;
    public static final Object VALUE_ANTIALIAS_DEFAULT = SunHints.VALUE_ANTIALIAS_DEFAULT;
    public static final Key KEY_RENDERING = SunHints.KEY_RENDERING;
    public static final Object VALUE_RENDER_SPEED = SunHints.VALUE_RENDER_SPEED;
    public static final Object VALUE_RENDER_QUALITY = SunHints.VALUE_RENDER_QUALITY;
    public static final Object VALUE_RENDER_DEFAULT = SunHints.VALUE_RENDER_DEFAULT;
    public static final Key KEY_DITHERING = SunHints.KEY_DITHERING;
    public static final Object VALUE_DITHER_DISABLE = SunHints.VALUE_DITHER_DISABLE;
    public static final Object VALUE_DITHER_ENABLE = SunHints.VALUE_DITHER_ENABLE;
    public static final Object VALUE_DITHER_DEFAULT = SunHints.VALUE_DITHER_DEFAULT;
    public static final Key KEY_TEXT_ANTIALIASING = SunHints.KEY_TEXT_ANTIALIASING;
    public static final Object VALUE_TEXT_ANTIALIAS_ON = SunHints.VALUE_TEXT_ANTIALIAS_ON;
    public static final Object VALUE_TEXT_ANTIALIAS_OFF = SunHints.VALUE_TEXT_ANTIALIAS_OFF;
    public static final Object VALUE_TEXT_ANTIALIAS_DEFAULT = SunHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
    public static final Object VALUE_TEXT_ANTIALIAS_GASP = SunHints.VALUE_TEXT_ANTIALIAS_GASP;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_HRGB = SunHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_HBGR = SunHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_VRGB = SunHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_VBGR = SunHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
    public static final Key KEY_TEXT_LCD_CONTRAST = SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST;
    public static final Key KEY_FRACTIONALMETRICS = SunHints.KEY_FRACTIONALMETRICS;
    public static final Object VALUE_FRACTIONALMETRICS_OFF = SunHints.VALUE_FRACTIONALMETRICS_OFF;
    public static final Object VALUE_FRACTIONALMETRICS_ON = SunHints.VALUE_FRACTIONALMETRICS_ON;
    public static final Object VALUE_FRACTIONALMETRICS_DEFAULT = SunHints.VALUE_FRACTIONALMETRICS_DEFAULT;
    public static final Key KEY_INTERPOLATION = SunHints.KEY_INTERPOLATION;
    public static final Object VALUE_INTERPOLATION_NEAREST_NEIGHBOR = SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    public static final Object VALUE_INTERPOLATION_BILINEAR = SunHints.VALUE_INTERPOLATION_BILINEAR;
    public static final Object VALUE_INTERPOLATION_BICUBIC = SunHints.VALUE_INTERPOLATION_BICUBIC;
    public static final Key KEY_ALPHA_INTERPOLATION = SunHints.KEY_ALPHA_INTERPOLATION;
    public static final Object VALUE_ALPHA_INTERPOLATION_SPEED = SunHints.VALUE_ALPHA_INTERPOLATION_SPEED;
    public static final Object VALUE_ALPHA_INTERPOLATION_QUALITY = SunHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
    public static final Object VALUE_ALPHA_INTERPOLATION_DEFAULT = SunHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
    public static final Key KEY_COLOR_RENDERING = SunHints.KEY_COLOR_RENDERING;
    public static final Object VALUE_COLOR_RENDER_SPEED = SunHints.VALUE_COLOR_RENDER_SPEED;
    public static final Object VALUE_COLOR_RENDER_QUALITY = SunHints.VALUE_COLOR_RENDER_QUALITY;
    public static final Object VALUE_COLOR_RENDER_DEFAULT = SunHints.VALUE_COLOR_RENDER_DEFAULT;
    public static final Key KEY_STROKE_CONTROL = SunHints.KEY_STROKE_CONTROL;
    public static final Object VALUE_STROKE_DEFAULT = SunHints.VALUE_STROKE_DEFAULT;
    public static final Object VALUE_STROKE_NORMALIZE = SunHints.VALUE_STROKE_NORMALIZE;
    public static final Object VALUE_STROKE_PURE = SunHints.VALUE_STROKE_PURE;
    public static final Key KEY_RESOLUTION_VARIANT = SunHints.KEY_RESOLUTION_VARIANT;
    public static final Object VALUE_RESOLUTION_VARIANT_DEFAULT = SunHints.VALUE_RESOLUTION_VARIANT_DEFAULT;
    public static final Object VALUE_RESOLUTION_VARIANT_BASE = SunHints.VALUE_RESOLUTION_VARIANT_BASE;
    public static final Object VALUE_RESOLUTION_VARIANT_SIZE_FIT = SunHints.VALUE_RESOLUTION_VARIANT_SIZE_FIT;
    public static final Object VALUE_RESOLUTION_VARIANT_DPI_FIT = SunHints.VALUE_RESOLUTION_VARIANT_DPI_FIT;

    public RenderingHints(Map<Key, ?> init) {
        if (init != null) {
            this.hintmap.putAll(init);
        }
    }

    public RenderingHints(Key key, Object value) {
        this.hintmap.put(key, value);
    }

    @Override
    public int size() {
        return this.hintmap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.hintmap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.hintmap.containsKey((Key)key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.hintmap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return this.hintmap.get((Key)key);
    }

    @Override
    public Object put(Object key, Object value) {
        if (!((Key)key).isCompatibleValue(value)) {
            throw new IllegalArgumentException(String.valueOf(value) + " incompatible with " + String.valueOf(key));
        }
        return this.hintmap.put((Key)key, value);
    }

    public void add(RenderingHints hints) {
        this.hintmap.putAll(hints.hintmap);
    }

    @Override
    public void clear() {
        this.hintmap.clear();
    }

    @Override
    public Object remove(Object key) {
        return this.hintmap.remove((Key)key);
    }

    @Override
    public void putAll(Map<?, ?> m) {
        if (RenderingHints.class.isInstance(m)) {
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                this.hintmap.put(entry.getKey(), entry.getValue());
            }
        } else {
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Set<Object> keySet() {
        return this.hintmap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return this.hintmap.values();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return Collections.unmodifiableMap(this.hintmap).entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RenderingHints) {
            return this.hintmap.equals(((RenderingHints)o).hintmap);
        }
        if (o instanceof Map) {
            return this.hintmap.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.hintmap.hashCode();
    }

    public Object clone() {
        RenderingHints rh;
        try {
            rh = (RenderingHints)super.clone();
            if (this.hintmap != null) {
                rh.hintmap = (HashMap)this.hintmap.clone();
            }
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        return rh;
    }

    public String toString() {
        if (this.hintmap == null) {
            return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode()) + " (0 hints)";
        }
        return this.hintmap.toString();
    }

    public static abstract class Key {
        private static HashMap<Object, Object> identitymap = new HashMap(17);
        private int privatekey;

        private String getIdentity() {
            return this.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this.getClass())) + ":" + Integer.toHexString(this.privatekey);
        }

        private static synchronized void recordIdentity(Key k) {
            Key otherkey;
            String identity = k.getIdentity();
            Object otherref = identitymap.get(identity);
            if (otherref != null && (otherkey = (Key)((WeakReference)otherref).get()) != null && otherkey.getClass() == k.getClass()) {
                throw new IllegalArgumentException(String.valueOf(identity) + " already registered");
            }
            identitymap.put(identity, new WeakReference<Key>(k));
        }

        protected Key(int privatekey) {
            this.privatekey = privatekey;
            Key.recordIdentity(this);
        }

        public abstract boolean isCompatibleValue(Object var1);

        protected final int intKey() {
            return this.privatekey;
        }

        public final int hashCode() {
            return super.hashCode();
        }

        public final boolean equals(Object o) {
            return this == o;
        }
    }
}

