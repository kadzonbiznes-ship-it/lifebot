/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook;

import com.github.kwhat.jnativehook.GlobalScreen;
import java.awt.Toolkit;
import java.util.EventObject;

public class NativeInputEvent
extends EventObject {
    private static final long serialVersionUID = 2306729722565226621L;
    private final int id;
    private final long when;
    private int modifiers;
    private short reserved;
    public static final int SHIFT_L_MASK = 1;
    public static final int CTRL_L_MASK = 2;
    public static final int META_L_MASK = 4;
    public static final int ALT_L_MASK = 8;
    public static final int SHIFT_R_MASK = 16;
    public static final int CTRL_R_MASK = 32;
    public static final int META_R_MASK = 64;
    public static final int ALT_R_MASK = 128;
    public static final int SHIFT_MASK = 17;
    public static final int CTRL_MASK = 34;
    public static final int META_MASK = 68;
    public static final int ALT_MASK = 136;
    public static final int BUTTON1_MASK = 256;
    public static final int BUTTON2_MASK = 512;
    public static final int BUTTON3_MASK = 1024;
    public static final int BUTTON4_MASK = 2048;
    public static final int BUTTON5_MASK = 4096;
    public static final int NUM_LOCK_MASK = 8192;
    public static final int CAPS_LOCK_MASK = 16384;
    public static final int SCROLL_LOCK_MASK = 32768;

    public NativeInputEvent(Class<GlobalScreen> source, int id, int modifiers) {
        super(source);
        this.id = id;
        this.when = 0L;
        this.modifiers = modifiers;
        this.reserved = 0;
    }

    public int getID() {
        return this.id;
    }

    public long getWhen() {
        return this.when;
    }

    public int getModifiers() {
        return this.modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    private void setReserved(short reserved) {
        this.reserved = reserved;
    }

    public static String getModifiersText(int modifiers) {
        StringBuilder param = new StringBuilder(255);
        if ((modifiers & 0x11) != 0) {
            param.append(Toolkit.getProperty("AWT.shift", "Shift"));
            param.append('+');
        }
        if ((modifiers & 0x22) != 0) {
            param.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            param.append('+');
        }
        if ((modifiers & 0x44) != 0) {
            param.append(Toolkit.getProperty("AWT.meta", "Meta"));
            param.append('+');
        }
        if ((modifiers & 0x88) != 0) {
            param.append(Toolkit.getProperty("AWT.alt", "Alt"));
            param.append('+');
        }
        if ((modifiers & 0x100) != 0) {
            param.append(Toolkit.getProperty("AWT.button1", "Button1"));
            param.append('+');
        }
        if ((modifiers & 0x200) != 0) {
            param.append(Toolkit.getProperty("AWT.button2", "Button2"));
            param.append('+');
        }
        if ((modifiers & 0x400) != 0) {
            param.append(Toolkit.getProperty("AWT.button3", "Button3"));
            param.append('+');
        }
        if ((modifiers & 0x800) != 0) {
            param.append(Toolkit.getProperty("AWT.button4", "Button4"));
            param.append('+');
        }
        if ((modifiers & 0x1000) != 0) {
            param.append(Toolkit.getProperty("AWT.button5", "Button5"));
            param.append('+');
        }
        if ((modifiers & 0x2000) != 0) {
            param.append(Toolkit.getProperty("AWT.numLock", "Num Lock"));
            param.append('+');
        }
        if ((modifiers & 0x4000) != 0) {
            param.append(Toolkit.getProperty("AWT.capsLock", "Caps Lock"));
            param.append('+');
        }
        if ((modifiers & 0x8000) != 0) {
            param.append(Toolkit.getProperty("AWT.scrollLock", "Scroll Lock"));
            param.append('+');
        }
        if (param.length() > 0) {
            param.deleteCharAt(param.length() - 1);
        }
        return param.toString();
    }

    public String paramString() {
        String param = "id=" + this.getID() + ',' + "when=" + this.getWhen() + ',' + "mask=" + Integer.toBinaryString(this.getModifiers()) + ',' + "modifiers=" + NativeInputEvent.getModifiersText(this.getModifiers());
        return param;
    }
}

