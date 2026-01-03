/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.mouse;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeInputEvent;
import java.awt.Point;

public class NativeMouseEvent
extends NativeInputEvent {
    private static final long serialVersionUID = -1093048990695350863L;
    private final int x;
    private final int y;
    private final int clickCount;
    private final int button;
    public static final int NATIVE_MOUSE_FIRST = 2500;
    public static final int NATIVE_MOUSE_LAST = 2505;
    public static final int NATIVE_MOUSE_CLICKED = 2500;
    public static final int NATIVE_MOUSE_PRESSED = 2501;
    public static final int NATIVE_MOUSE_RELEASED = 2502;
    public static final int NATIVE_MOUSE_MOVED = 2503;
    public static final int NATIVE_MOUSE_DRAGGED = 2504;
    public static final int NATIVE_MOUSE_WHEEL = 2505;
    public static final int NOBUTTON = 0;
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;
    public static final int BUTTON4 = 4;
    public static final int BUTTON5 = 5;

    public NativeMouseEvent(int id, int modifiers, int x, int y, int clickCount) {
        this(id, modifiers, x, y, clickCount, 0);
    }

    public NativeMouseEvent(int id, int modifiers, int x, int y, int clickCount, int button) {
        super(GlobalScreen.class, id, modifiers);
        this.x = x;
        this.y = y;
        this.clickCount = clickCount;
        this.button = button;
    }

    public int getButton() {
        return this.button;
    }

    public int getClickCount() {
        return this.clickCount;
    }

    public Point getPoint() {
        return new Point(this.x, this.y);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String paramString() {
        StringBuilder param = new StringBuilder(super.paramString());
        switch (this.getID()) {
            case 2500: {
                param.append("NATIVE_MOUSE_CLICKED");
                break;
            }
            case 2501: {
                param.append("NATIVE_MOUSE_PRESSED");
                break;
            }
            case 2502: {
                param.append("NATIVE_MOUSE_RELEASED");
                break;
            }
            case 2503: {
                param.append("NATIVE_MOUSE_MOVED");
                break;
            }
            case 2504: {
                param.append("NATIVE_MOUSE_DRAGGED");
                break;
            }
            case 2505: {
                param.append("NATIVE_MOUSE_WHEEL");
                break;
            }
            default: {
                param.append("unknown type");
            }
        }
        param.append(",(");
        param.append(this.x);
        param.append(',');
        param.append(this.y);
        param.append("),");
        param.append("button=");
        param.append(this.button);
        if (this.getModifiers() != 0) {
            param.append(",modifiers=");
            param.append(NativeMouseEvent.getModifiersText(this.getModifiers()));
        }
        param.append(",clickCount=");
        param.append(this.getClickCount());
        return param.toString();
    }
}

