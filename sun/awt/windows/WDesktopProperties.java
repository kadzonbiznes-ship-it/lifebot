/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import sun.awt.SunToolkit;
import sun.awt.windows.ThemeReader;
import sun.awt.windows.WToolkit;
import sun.util.logging.PlatformLogger;

final class WDesktopProperties {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WDesktopProperties");
    private static final String PREFIX = "win.";
    private static final String FILE_PREFIX = "awt.file.";
    private static final String PROP_NAMES = "win.propNames";
    private long pData;
    private WToolkit wToolkit;
    private HashMap<String, Object> map = new HashMap();
    static HashMap<String, String> fontNameMap;

    private static native void initIDs();

    static boolean isWindowsProperty(String name) {
        return name.startsWith(PREFIX) || name.startsWith(FILE_PREFIX) || name.equals("awt.font.desktophints");
    }

    WDesktopProperties(WToolkit wToolkit) {
        this.wToolkit = wToolkit;
        this.init();
    }

    private native void init();

    private String[] getKeyNames() {
        Object[] sortedKeys = this.map.keySet().toArray(new String[0]);
        Arrays.sort(sortedKeys);
        return sortedKeys;
    }

    private native void getWindowsParameters();

    private synchronized void setBooleanProperty(String key, boolean value) {
        assert (key != null);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(key + "=" + value);
        }
        this.map.put(key, value);
    }

    private synchronized void setIntegerProperty(String key, int value) {
        assert (key != null);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(key + "=" + value);
        }
        this.map.put(key, value);
    }

    private synchronized void setStringProperty(String key, String value) {
        assert (key != null);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(key + "=" + value);
        }
        this.map.put(key, value);
    }

    private synchronized void setColorProperty(String key, int r, int g, int b) {
        assert (key != null && r <= 255 && g <= 255 && b <= 255);
        Color color = new Color(r, g, b);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(key + "=" + String.valueOf(color));
        }
        this.map.put(key, color);
    }

    private synchronized void setFontProperty(String key, String name, int style, int size) {
        assert (key != null && style <= 3 && size >= 0);
        String mappedName = fontNameMap.get(name);
        if (mappedName != null) {
            name = mappedName;
        }
        Font font = new Font(name, style, size);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(key + "=" + String.valueOf(font));
        }
        this.map.put(key, font);
        String sizeKey = key + ".height";
        Integer iSize = size;
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(sizeKey + "=" + iSize);
        }
        this.map.put(sizeKey, iSize);
    }

    private synchronized void setSoundProperty(String key, String winEventName) {
        assert (key != null && winEventName != null);
        WinPlaySound soundRunnable = new WinPlaySound(winEventName);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(key + "=" + String.valueOf(soundRunnable));
        }
        this.map.put(key, soundRunnable);
    }

    private native void playWindowsSound(String var1);

    synchronized Map<String, Object> getProperties() {
        ThemeReader.flush();
        this.map = new HashMap();
        this.getWindowsParameters();
        this.map.put("awt.font.desktophints", SunToolkit.getDesktopFontHints());
        this.map.put(PROP_NAMES, this.getKeyNames());
        this.map.put("DnD.Autoscroll.cursorHysteresis", this.map.get("win.drag.x"));
        return (Map)this.map.clone();
    }

    synchronized RenderingHints getDesktopAAHints() {
        Object fontSmoothingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
        Integer fontSmoothingContrast = null;
        Boolean smoothingOn = (Boolean)this.map.get("win.text.fontSmoothingOn");
        if (Boolean.TRUE.equals(smoothingOn)) {
            Integer typeID = (Integer)this.map.get("win.text.fontSmoothingType");
            if (typeID == null || typeID <= 1 || typeID > 2) {
                fontSmoothingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
            } else {
                Integer orientID = (Integer)this.map.get("win.text.fontSmoothingOrientation");
                fontSmoothingHint = orientID == null || orientID != 0 ? RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB : RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
                fontSmoothingContrast = (Integer)this.map.get("win.text.fontSmoothingContrast");
                fontSmoothingContrast = fontSmoothingContrast == null ? Integer.valueOf(140) : Integer.valueOf(fontSmoothingContrast / 10);
            }
        }
        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, fontSmoothingHint);
        if (fontSmoothingContrast != null) {
            hints.put(RenderingHints.KEY_TEXT_LCD_CONTRAST, fontSmoothingContrast);
        }
        return hints;
    }

    static {
        WDesktopProperties.initIDs();
        fontNameMap = new HashMap();
        fontNameMap.put("Courier", "Monospaced");
        fontNameMap.put("MS Serif", "Microsoft Serif");
        fontNameMap.put("MS Sans Serif", "Microsoft Sans Serif");
        fontNameMap.put("Terminal", "Dialog");
        fontNameMap.put("FixedSys", "Monospaced");
        fontNameMap.put("System", "Dialog");
    }

    class WinPlaySound
    implements Runnable {
        String winEventName;

        WinPlaySound(String winEventName) {
            this.winEventName = winEventName;
        }

        @Override
        public void run() {
            WDesktopProperties.this.playWindowsSound(this.winEventName);
        }

        public String toString() {
            return "WinPlaySound(" + this.winEventName + ")";
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            try {
                return this.winEventName.equals(((WinPlaySound)o).winEventName);
            }
            catch (Exception e) {
                return false;
            }
        }

        public int hashCode() {
            return this.winEventName.hashCode();
        }
    }
}

