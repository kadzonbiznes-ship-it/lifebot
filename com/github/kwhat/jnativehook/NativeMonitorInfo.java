/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook;

public class NativeMonitorInfo {
    private short number;
    private int x;
    private int y;
    private short width;
    private short height;

    public NativeMonitorInfo(short number, int x, int y, short width, short height) {
        this.number = number;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public short getNumber() {
        return this.number;
    }

    public void setNumber(short number) {
        this.number = number;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public short getWidth() {
        return this.width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public short getHeight() {
        return this.height;
    }

    public void setHeight(short height) {
        this.height = height;
    }
}

