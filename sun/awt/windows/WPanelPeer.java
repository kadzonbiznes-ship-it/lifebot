/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.peer.PanelPeer;
import sun.awt.SunGraphicsCallback;
import sun.awt.windows.WCanvasPeer;

class WPanelPeer
extends WCanvasPeer
implements PanelPeer {
    Insets insets_;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        SunGraphicsCallback.PaintHeavyweightComponentsCallback.getInstance().runComponents(((Container)this.target).getComponents(), g, 3);
    }

    @Override
    public void print(Graphics g) {
        super.print(g);
        SunGraphicsCallback.PrintHeavyweightComponentsCallback.getInstance().runComponents(((Container)this.target).getComponents(), g, 3);
    }

    @Override
    public Insets getInsets() {
        return this.insets_;
    }

    private static native void initIDs();

    WPanelPeer(Component target) {
        super(target);
    }

    @Override
    void initialize() {
        super.initialize();
        this.insets_ = new Insets(0, 0, 0, 0);
        Color c = ((Component)this.target).getBackground();
        if (c == null) {
            c = SystemColor.window;
            ((Component)this.target).setBackground(c);
            this.setBackground(c);
        }
        if ((c = ((Component)this.target).getForeground()) == null) {
            c = SystemColor.windowText;
            ((Component)this.target).setForeground(c);
            this.setForeground(c);
        }
    }

    static {
        WPanelPeer.initIDs();
    }
}

