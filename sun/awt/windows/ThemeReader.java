/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import com.sun.java.swing.plaf.windows.TMSchema;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ThemeReader {
    private static final int defaultDPI = 96;
    private static final List<String> partSizeWidgets = List.of("MENU", "BUTTON");
    private static final List<Integer> partSizeWidgetParts = List.of(Integer.valueOf(TMSchema.Part.BP_RADIOBUTTON.getValue()), Integer.valueOf(TMSchema.Part.BP_CHECKBOX.getValue()), Integer.valueOf(TMSchema.Part.MP_POPUPCHECK.getValue()));
    private static final Map<Integer, Map<String, Long>> dpiAwareWidgetToTheme = new HashMap<Integer, Map<String, Long>>();
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final Lock readLock = readWriteLock.readLock();
    private static final Lock writeLock = readWriteLock.writeLock();
    private static volatile boolean valid;
    private static volatile boolean isThemed;
    static volatile boolean xpStyleEnabled;

    static void flush() {
        valid = false;
    }

    private static native boolean initThemes();

    public static boolean isThemed() {
        writeLock.lock();
        try {
            boolean bl = isThemed = ThemeReader.initThemes();
            return bl;
        }
        finally {
            writeLock.unlock();
        }
    }

    public static boolean isXPStyleEnabled() {
        return xpStyleEnabled;
    }

    private static Long openThemeImpl(String widget, int dpi) {
        Long theme;
        int i = widget.indexOf("::");
        if (i > 0) {
            ThemeReader.setWindowTheme(widget.substring(0, i));
            theme = ThemeReader.openTheme(widget.substring(i + 2), dpi);
            ThemeReader.setWindowTheme(null);
        } else {
            theme = ThemeReader.openTheme(widget, dpi);
        }
        return theme;
    }

    private static Long getThemeImpl(String widget, int dpi) {
        return dpiAwareWidgetToTheme.computeIfAbsent(dpi, key -> new HashMap()).computeIfAbsent(widget, w -> ThemeReader.openThemeImpl(widget, dpi));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Long getTheme(String widget, int dpi) {
        if (!isThemed) {
            throw new IllegalStateException("Themes are not loaded");
        }
        if (!valid) {
            readLock.unlock();
            writeLock.lock();
            try {
                if (!valid) {
                    for (Map<String, Long> dpiVal : dpiAwareWidgetToTheme.values()) {
                        for (Long value : dpiVal.values()) {
                            ThemeReader.closeTheme(value);
                        }
                    }
                    dpiAwareWidgetToTheme.clear();
                    valid = true;
                }
            }
            finally {
                readLock.lock();
                writeLock.unlock();
            }
        }
        Long theme = null;
        Map<String, Long> widgetToTheme = dpiAwareWidgetToTheme.get(dpi);
        if (widgetToTheme != null) {
            theme = widgetToTheme.get(widget);
        }
        if (theme == null) {
            readLock.unlock();
            writeLock.lock();
            try {
                theme = ThemeReader.getThemeImpl(widget, dpi);
            }
            finally {
                readLock.lock();
                writeLock.unlock();
            }
        }
        return theme;
    }

    private static native void paintBackground(int[] var0, long var1, int var3, int var4, int var5, int var6, int var7, int var8, int var9);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void paintBackground(int[] buffer, String widget, int part, int state, int x, int y, int w, int h, int stride, int dpi) {
        readLock.lock();
        try {
            Dimension d = partSizeWidgets.contains(widget) && partSizeWidgetParts.contains(part) ? ThemeReader.getPartSize(ThemeReader.getTheme(widget, dpi), part, state) : new Dimension(w, h);
            ThemeReader.paintBackground(buffer, ThemeReader.getTheme(widget, dpi), part, state, d.width, d.height, w, h, stride);
        }
        finally {
            readLock.unlock();
        }
    }

    private static native Insets getThemeMargins(long var0, int var2, int var3, int var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Insets getThemeMargins(String widget, int part, int state, int marginType) {
        readLock.lock();
        try {
            Insets insets = ThemeReader.getThemeMargins(ThemeReader.getTheme(widget, 96), part, state, marginType);
            return insets;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native boolean isThemePartDefined(long var0, int var2, int var3);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isThemePartDefined(String widget, int part, int state) {
        readLock.lock();
        try {
            boolean bl = ThemeReader.isThemePartDefined(ThemeReader.getTheme(widget, 96), part, state);
            return bl;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native Color getColor(long var0, int var2, int var3, int var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Color getColor(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            Color color = ThemeReader.getColor(ThemeReader.getTheme(widget, 96), part, state, property);
            return color;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native int getInt(long var0, int var2, int var3, int var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getInt(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            int n = ThemeReader.getInt(ThemeReader.getTheme(widget, 96), part, state, property);
            return n;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native int getEnum(long var0, int var2, int var3, int var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getEnum(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            int n = ThemeReader.getEnum(ThemeReader.getTheme(widget, 96), part, state, property);
            return n;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native boolean getBoolean(long var0, int var2, int var3, int var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean getBoolean(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            boolean bl = ThemeReader.getBoolean(ThemeReader.getTheme(widget, 96), part, state, property);
            return bl;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native boolean getSysBoolean(long var0, int var2);

    public static boolean getSysBoolean(String widget, int property) {
        readLock.lock();
        try {
            boolean bl = ThemeReader.getSysBoolean(ThemeReader.getTheme(widget, 96), property);
            return bl;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native Point getPoint(long var0, int var2, int var3, int var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Point getPoint(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            Point point = ThemeReader.getPoint(ThemeReader.getTheme(widget, 96), part, state, property);
            return point;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native Dimension getPosition(long var0, int var2, int var3, int var4);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Dimension getPosition(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            Dimension dimension = ThemeReader.getPosition(ThemeReader.getTheme(widget, 96), part, state, property);
            return dimension;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native Dimension getPartSize(long var0, int var2, int var3);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Dimension getPartSize(String widget, int part, int state) {
        readLock.lock();
        try {
            Dimension dimension = ThemeReader.getPartSize(ThemeReader.getTheme(widget, 96), part, state);
            return dimension;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native long openTheme(String var0, int var1);

    private static native void closeTheme(long var0);

    private static native void setWindowTheme(String var0);

    private static native long getThemeTransitionDuration(long var0, int var2, int var3, int var4, int var5);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long getThemeTransitionDuration(String widget, int part, int stateFrom, int stateTo, int propId) {
        readLock.lock();
        try {
            long l = ThemeReader.getThemeTransitionDuration(ThemeReader.getTheme(widget, 96), part, stateFrom, stateTo, propId);
            return l;
        }
        finally {
            readLock.unlock();
        }
    }

    private static native Insets getThemeBackgroundContentMargins(long var0, int var2, int var3, int var4, int var5);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Insets getThemeBackgroundContentMargins(String widget, int part, int state, int boundingWidth, int boundingHeight) {
        readLock.lock();
        try {
            Insets insets = ThemeReader.getThemeBackgroundContentMargins(ThemeReader.getTheme(widget, 96), part, state, boundingWidth, boundingHeight);
            return insets;
        }
        finally {
            readLock.unlock();
        }
    }
}

