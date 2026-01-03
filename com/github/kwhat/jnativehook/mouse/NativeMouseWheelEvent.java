/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.mouse;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

public class NativeMouseWheelEvent
extends NativeMouseEvent {
    private static final long serialVersionUID = 2112217673594181259L;
    public static final int WHEEL_UNIT_SCROLL = 1;
    public static final int WHEEL_BLOCK_SCROLL = 2;
    public static final int WHEEL_VERTICAL_DIRECTION = 3;
    public static final int WHEEL_HORIZONTAL_DIRECTION = 4;
    private final int scrollAmount;
    private final int scrollType;
    private final int wheelRotation;
    private final int wheelDirection;

    public NativeMouseWheelEvent(int id, int modifiers, int x, int y, int clickCount, int scrollType, int scrollAmount, int wheelRotation) {
        this(id, modifiers, x, y, clickCount, scrollType, scrollAmount, wheelRotation, 3);
    }

    public NativeMouseWheelEvent(int id, int modifiers, int x, int y, int clickCount, int scrollType, int scrollAmount, int wheelRotation, int wheelDirection) {
        super(id, modifiers, x, y, clickCount);
        this.scrollType = scrollType;
        this.scrollAmount = scrollAmount;
        this.wheelRotation = wheelRotation;
        this.wheelDirection = wheelDirection;
    }

    public int getScrollAmount() {
        return this.scrollAmount;
    }

    public int getScrollType() {
        return this.scrollType;
    }

    public int getWheelRotation() {
        return this.wheelRotation;
    }

    public int getWheelDirection() {
        return this.wheelDirection;
    }

    @Override
    public String paramString() {
        StringBuilder param = new StringBuilder(super.paramString());
        param.append(",scrollType=");
        switch (this.getScrollType()) {
            case 1: {
                param.append("WHEEL_UNIT_SCROLL");
                break;
            }
            case 2: {
                param.append("WHEEL_BLOCK_SCROLL");
                break;
            }
            default: {
                param.append("unknown scroll type");
            }
        }
        param.append(",scrollAmount=");
        param.append(this.getScrollAmount());
        param.append(",wheelRotation=");
        param.append(this.getWheelRotation());
        param.append(",wheelDirection=");
        switch (this.getWheelDirection()) {
            case 3: {
                param.append("WHEEL_VERTICAL_DIRECTION");
                break;
            }
            case 4: {
                param.append("WHEEL_HORIZONTAL_DIRECTION");
                break;
            }
            default: {
                param.append("unknown scroll direction");
            }
        }
        return param.toString();
    }
}

