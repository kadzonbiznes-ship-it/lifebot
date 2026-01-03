/*
 * Decompiled with CFR 0.152.
 */
package com.sun.java.swing.plaf.windows;

import java.awt.Graphics;
import java.awt.Window;
import javax.swing.JWindow;

class WindowsPopupWindow
extends JWindow {
    static final int UNDEFINED_WINDOW_TYPE = 0;
    static final int TOOLTIP_WINDOW_TYPE = 1;
    static final int MENU_WINDOW_TYPE = 2;
    static final int SUBMENU_WINDOW_TYPE = 3;
    static final int POPUPMENU_WINDOW_TYPE = 4;
    static final int COMBOBOX_POPUP_WINDOW_TYPE = 5;
    private int windowType;

    WindowsPopupWindow(Window parent) {
        super(parent);
        this.setFocusableWindowState(false);
    }

    void setWindowType(int type) {
        this.windowType = type;
    }

    int getWindowType() {
        return this.windowType;
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }

    @Override
    public void hide() {
        super.hide();
        this.removeNotify();
    }

    @Override
    public void show() {
        super.show();
        this.pack();
    }
}

