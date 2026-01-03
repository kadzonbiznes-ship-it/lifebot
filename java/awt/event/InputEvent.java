/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.NativeLibLoader;
import java.util.Arrays;
import sun.awt.AWTAccessor;
import sun.awt.AWTPermissions;
import sun.util.logging.PlatformLogger;

public abstract sealed class InputEvent
extends ComponentEvent
permits KeyEvent, MouseEvent {
    private static final PlatformLogger logger = PlatformLogger.getLogger("java.awt.event.InputEvent");
    @Deprecated(since="9")
    public static final int SHIFT_MASK = 1;
    @Deprecated(since="9")
    public static final int CTRL_MASK = 2;
    @Deprecated(since="9")
    public static final int META_MASK = 4;
    @Deprecated(since="9")
    public static final int ALT_MASK = 8;
    @Deprecated(since="9")
    public static final int ALT_GRAPH_MASK = 32;
    @Deprecated(since="9")
    public static final int BUTTON1_MASK = 16;
    @Deprecated(since="9")
    public static final int BUTTON2_MASK = 8;
    @Deprecated(since="9")
    public static final int BUTTON3_MASK = 4;
    public static final int SHIFT_DOWN_MASK = 64;
    public static final int CTRL_DOWN_MASK = 128;
    public static final int META_DOWN_MASK = 256;
    public static final int ALT_DOWN_MASK = 512;
    public static final int BUTTON1_DOWN_MASK = 1024;
    public static final int BUTTON2_DOWN_MASK = 2048;
    public static final int BUTTON3_DOWN_MASK = 4096;
    public static final int ALT_GRAPH_DOWN_MASK = 8192;
    private static final int[] BUTTON_DOWN_MASK = new int[]{1024, 2048, 4096, 16384, 32768, 65536, 131072, 262144, 524288, 0x100000, 0x200000, 0x400000, 0x800000, 0x1000000, 0x2000000, 0x4000000, 0x8000000, 0x10000000, 0x20000000, 0x40000000};
    static final int FIRST_HIGH_BIT = Integer.MIN_VALUE;
    static final int JDK_1_3_MODIFIERS = 63;
    static final int HIGH_MODIFIERS = Integer.MIN_VALUE;
    long when;
    int modifiers;
    private transient boolean canAccessSystemClipboard;
    private static final long serialVersionUID = -2482525981698309786L;

    private static int[] getButtonDownMasks() {
        return Arrays.copyOf(BUTTON_DOWN_MASK, BUTTON_DOWN_MASK.length);
    }

    public static int getMaskForButton(int button) {
        if (button <= 0 || button > BUTTON_DOWN_MASK.length) {
            throw new IllegalArgumentException("button doesn't exist " + button);
        }
        return BUTTON_DOWN_MASK[button - 1];
    }

    private static native void initIDs();

    InputEvent(Component source, int id, long when, int modifiers) {
        super(source, id);
        this.when = when;
        this.modifiers = modifiers;
        this.canAccessSystemClipboard = this.canAccessSystemClipboard();
    }

    private boolean canAccessSystemClipboard() {
        boolean b = false;
        if (!GraphicsEnvironment.isHeadless()) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                try {
                    sm.checkPermission(AWTPermissions.ACCESS_CLIPBOARD_PERMISSION);
                    b = true;
                }
                catch (SecurityException se) {
                    if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                        logger.fine("InputEvent.canAccessSystemClipboard() got SecurityException ", se);
                    }
                }
            } else {
                b = true;
            }
        }
        return b;
    }

    public boolean isShiftDown() {
        return (this.modifiers & 0x40) != 0;
    }

    public boolean isControlDown() {
        return (this.modifiers & 0x80) != 0;
    }

    public boolean isMetaDown() {
        return (this.modifiers & 0x100) != 0;
    }

    public boolean isAltDown() {
        return (this.modifiers & 0x200) != 0;
    }

    public boolean isAltGraphDown() {
        return (this.modifiers & 0x2000) != 0;
    }

    public long getWhen() {
        return this.when;
    }

    @Deprecated(since="9")
    public int getModifiers() {
        return this.modifiers & 0x8000003F;
    }

    public int getModifiersEx() {
        return this.modifiers & 0xFFFFFFC0;
    }

    @Override
    public void consume() {
        this.consumed = true;
    }

    @Override
    public boolean isConsumed() {
        return this.consumed;
    }

    public static String getModifiersExText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & 0x100) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & 0x80) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & 0x200) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append("+");
        }
        if ((modifiers & 0x40) != 0) {
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append("+");
        }
        if ((modifiers & 0x2000) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append("+");
        }
        int buttonNumber = 1;
        for (int mask : BUTTON_DOWN_MASK) {
            if ((modifiers & mask) != 0) {
                buf.append(Toolkit.getProperty("AWT.button" + buttonNumber, "Button" + buttonNumber));
                buf.append("+");
            }
            ++buttonNumber;
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    static {
        NativeLibLoader.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            InputEvent.initIDs();
        }
        AWTAccessor.setInputEventAccessor(new AWTAccessor.InputEventAccessor(){

            @Override
            public int[] getButtonDownMasks() {
                return InputEvent.getButtonDownMasks();
            }

            @Override
            public boolean canAccessSystemClipboard(InputEvent event) {
                return event.canAccessSystemClipboard;
            }

            @Override
            public void setCanAccessSystemClipboard(InputEvent event, boolean canAccessSystemClipboard) {
                event.canAccessSystemClipboard = canAccessSystemClipboard;
            }
        });
    }
}

