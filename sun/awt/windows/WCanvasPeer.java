/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.peer.CanvasPeer;
import sun.awt.PaintEventDispatcher;
import sun.awt.SunToolkit;
import sun.awt.windows.WComponentPeer;

class WCanvasPeer
extends WComponentPeer
implements CanvasPeer {
    private boolean eraseBackground;

    WCanvasPeer(Component target) {
        super(target);
    }

    @Override
    native void create(WComponentPeer var1);

    @Override
    void initialize() {
        this.eraseBackground = !SunToolkit.getSunAwtNoerasebackground();
        boolean eraseBackgroundOnResize = SunToolkit.getSunAwtErasebackgroundonresize();
        if (!PaintEventDispatcher.getPaintEventDispatcher().shouldDoNativeBackgroundErase((Component)this.target)) {
            this.eraseBackground = false;
        }
        this.setNativeBackgroundErase(this.eraseBackground, eraseBackgroundOnResize);
        super.initialize();
        Color bg = ((Component)this.target).getBackground();
        if (bg != null) {
            this.setBackground(bg);
        }
    }

    @Override
    public void paint(Graphics g) {
        Dimension d = ((Component)this.target).getSize();
        if (g instanceof Graphics2D) {
            g.clearRect(0, 0, d.width, d.height);
        } else {
            g.setColor(((Component)this.target).getBackground());
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(((Component)this.target).getForeground());
        }
        super.paint(g);
    }

    @Override
    public boolean shouldClearRectBeforePaint() {
        return this.eraseBackground;
    }

    void disableBackgroundErase() {
        this.eraseBackground = false;
        this.setNativeBackgroundErase(false, false);
    }

    private native void setNativeBackgroundErase(boolean var1, boolean var2);

    @Override
    public GraphicsConfiguration getAppropriateGraphicsConfiguration(GraphicsConfiguration gc) {
        return gc;
    }
}

