/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.ConstructorProperties;
import java.io.InputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import sun.awt.AWTAccessor;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.util.logging.PlatformLogger;

public class Cursor
implements Serializable {
    public static final int DEFAULT_CURSOR = 0;
    public static final int CROSSHAIR_CURSOR = 1;
    public static final int TEXT_CURSOR = 2;
    public static final int WAIT_CURSOR = 3;
    public static final int SW_RESIZE_CURSOR = 4;
    public static final int SE_RESIZE_CURSOR = 5;
    public static final int NW_RESIZE_CURSOR = 6;
    public static final int NE_RESIZE_CURSOR = 7;
    public static final int N_RESIZE_CURSOR = 8;
    public static final int S_RESIZE_CURSOR = 9;
    public static final int W_RESIZE_CURSOR = 10;
    public static final int E_RESIZE_CURSOR = 11;
    public static final int HAND_CURSOR = 12;
    public static final int MOVE_CURSOR = 13;
    @Deprecated
    protected static Cursor[] predefined = new Cursor[14];
    private static final Cursor[] predefinedPrivate = new Cursor[14];
    static final String[][] cursorProperties = new String[][]{{"AWT.DefaultCursor", "Default Cursor"}, {"AWT.CrosshairCursor", "Crosshair Cursor"}, {"AWT.TextCursor", "Text Cursor"}, {"AWT.WaitCursor", "Wait Cursor"}, {"AWT.SWResizeCursor", "Southwest Resize Cursor"}, {"AWT.SEResizeCursor", "Southeast Resize Cursor"}, {"AWT.NWResizeCursor", "Northwest Resize Cursor"}, {"AWT.NEResizeCursor", "Northeast Resize Cursor"}, {"AWT.NResizeCursor", "North Resize Cursor"}, {"AWT.SResizeCursor", "South Resize Cursor"}, {"AWT.WResizeCursor", "West Resize Cursor"}, {"AWT.EResizeCursor", "East Resize Cursor"}, {"AWT.HandCursor", "Hand Cursor"}, {"AWT.MoveCursor", "Move Cursor"}};
    int type = 0;
    public static final int CUSTOM_CURSOR = -1;
    private static final Hashtable<String, Cursor> systemCustomCursors = new Hashtable(1);
    private static final String RESOURCE_PREFIX = "/sun/awt/resources/cursors/";
    private static final String PROPERTIES_FILE = "/sun/awt/resources/cursors/cursors.properties";
    private static Properties systemCustomCursorProperties = null;
    private static final String CURSOR_DOT_PREFIX = "Cursor.";
    private static final String DOT_FILE_SUFFIX = ".File";
    private static final String DOT_HOTSPOT_SUFFIX = ".HotSpot";
    private static final String DOT_NAME_SUFFIX = ".Name";
    private static final long serialVersionUID = 8028237497568985504L;
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Cursor");
    private transient long pData;
    private transient Object anchor = new Object();
    transient CursorDisposer disposer;
    protected String name;

    private static native void initIDs();

    private void setPData(long pData) {
        this.pData = pData;
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        if (this.disposer == null) {
            this.disposer = new CursorDisposer(pData);
            if (this.anchor == null) {
                this.anchor = new Object();
            }
            Disposer.addRecord(this.anchor, this.disposer);
        } else {
            this.disposer.pData = pData;
        }
    }

    public static Cursor getPredefinedCursor(int type) {
        if (type < 0 || type > 13) {
            throw new IllegalArgumentException("illegal cursor type");
        }
        Cursor c = predefinedPrivate[type];
        if (c == null) {
            Cursor.predefinedPrivate[type] = c = new Cursor(type);
        }
        if (predefined[type] == null) {
            Cursor.predefined[type] = c;
        }
        return c;
    }

    public static Cursor getSystemCustomCursor(String name) throws AWTException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        Cursor cursor = systemCustomCursors.get(name);
        if (cursor == null) {
            Point hotPoint;
            Cursor.loadSystemCustomCursorProperties();
            String prefix = CURSOR_DOT_PREFIX + name;
            String key = prefix + DOT_FILE_SUFFIX;
            if (!systemCustomCursorProperties.containsKey(key)) {
                if (log.isLoggable(PlatformLogger.Level.FINER)) {
                    log.finer("Cursor.getSystemCustomCursor(" + name + ") returned null");
                }
                return null;
            }
            String fileName = systemCustomCursorProperties.getProperty(key);
            String localized = systemCustomCursorProperties.getProperty(prefix + DOT_NAME_SUFFIX, name);
            String hotspot = systemCustomCursorProperties.getProperty(prefix + DOT_HOTSPOT_SUFFIX);
            if (hotspot == null) {
                throw new AWTException("no hotspot property defined for cursor: " + name);
            }
            StringTokenizer st = new StringTokenizer(hotspot, ",");
            if (st.countTokens() != 2) {
                throw new AWTException("failed to parse hotspot property for cursor: " + name);
            }
            try {
                hotPoint = new Point(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
            }
            catch (NumberFormatException nfe) {
                throw new AWTException("failed to parse hotspot property for cursor: " + name);
            }
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            String file = RESOURCE_PREFIX + fileName;
            InputStream in = AccessController.doPrivileged(() -> Cursor.class.getResourceAsStream(file));
            try (InputStream inputStream = in;){
                Image image = toolkit.createImage(in.readAllBytes());
                cursor = toolkit.createCustomCursor(image, hotPoint, localized);
            }
            catch (Exception e) {
                throw new AWTException("Exception: " + String.valueOf(e.getClass()) + " " + e.getMessage() + " occurred while creating cursor " + name);
            }
            if (cursor == null) {
                if (log.isLoggable(PlatformLogger.Level.FINER)) {
                    log.finer("Cursor.getSystemCustomCursor(" + name + ") returned null");
                }
            } else {
                systemCustomCursors.put(name, cursor);
            }
        }
        return cursor;
    }

    public static Cursor getDefaultCursor() {
        return Cursor.getPredefinedCursor(0);
    }

    @ConstructorProperties(value={"type"})
    public Cursor(int type) {
        if (type < 0 || type > 13) {
            throw new IllegalArgumentException("illegal cursor type");
        }
        this.type = type;
        this.name = Toolkit.getProperty(cursorProperties[type][0], cursorProperties[type][1]);
    }

    protected Cursor(String name) {
        this.type = -1;
        this.name = name;
    }

    public int getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.getName() + "]";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadSystemCustomCursorProperties() throws AWTException {
        Hashtable<String, Cursor> hashtable = systemCustomCursors;
        synchronized (hashtable) {
            if (systemCustomCursorProperties != null) {
                return;
            }
            systemCustomCursorProperties = new Properties();
            try {
                AccessController.doPrivileged(() -> {
                    try (InputStream is = Cursor.class.getResourceAsStream(PROPERTIES_FILE);){
                        systemCustomCursorProperties.load(is);
                    }
                    return null;
                });
            }
            catch (Exception e) {
                systemCustomCursorProperties = null;
                throw new AWTException("Exception: " + String.valueOf(e.getClass()) + " " + e.getMessage() + " occurred while loading: /sun/awt/resources/cursors/cursors.properties");
            }
        }
    }

    private static native void finalizeImpl(long var0);

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Cursor.initIDs();
        }
        AWTAccessor.setCursorAccessor(new AWTAccessor.CursorAccessor(){

            @Override
            public long getPData(Cursor cursor) {
                return cursor.pData;
            }

            @Override
            public void setPData(Cursor cursor, long pData) {
                cursor.pData = pData;
            }

            @Override
            public int getType(Cursor cursor) {
                return cursor.type;
            }
        });
    }

    static class CursorDisposer
    implements DisposerRecord {
        volatile long pData;

        public CursorDisposer(long pData) {
            this.pData = pData;
        }

        @Override
        public void dispose() {
            if (this.pData != 0L) {
                Cursor.finalizeImpl(this.pData);
            }
        }
    }
}

