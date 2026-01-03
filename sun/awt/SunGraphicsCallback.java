/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import sun.awt.ConstrainableGraphics;
import sun.util.logging.PlatformLogger;

public abstract class SunGraphicsCallback {
    public static final int HEAVYWEIGHTS = 1;
    public static final int LIGHTWEIGHTS = 2;
    public static final int TWO_PASSES = 4;
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.SunGraphicsCallback");

    public abstract void run(Component var1, Graphics var2);

    protected void constrainGraphics(Graphics g, Rectangle bounds) {
        if (g instanceof ConstrainableGraphics) {
            ((ConstrainableGraphics)((Object)g)).constrain(bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            g.translate(bounds.x, bounds.y);
        }
        g.clipRect(0, 0, bounds.width, bounds.height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void runOneComponent(Component comp, Rectangle bounds, Graphics g, Shape clip, int weightFlags) {
        if (comp == null || !comp.isDisplayable() || !comp.isVisible()) {
            return;
        }
        boolean lightweight = comp.isLightweight();
        if (lightweight && (weightFlags & 2) == 0 || !lightweight && (weightFlags & 1) == 0) {
            return;
        }
        if (bounds == null) {
            bounds = comp.getBounds();
        }
        if (clip == null || clip.intersects(bounds)) {
            Graphics cg = g.create();
            try {
                this.constrainGraphics(cg, bounds);
                cg.setFont(comp.getFont());
                cg.setColor(comp.getForeground());
                if (cg instanceof Graphics2D) {
                    ((Graphics2D)cg).setBackground(comp.getBackground());
                }
                this.run(comp, cg);
            }
            finally {
                cg.dispose();
            }
        }
    }

    public final void runComponents(Component[] comps, Graphics g, int weightFlags) {
        int ncomponents = comps.length;
        Shape clip = g.getClip();
        if (log.isLoggable(PlatformLogger.Level.FINER) && clip != null) {
            Rectangle newrect = clip.getBounds();
            log.finer("x = " + newrect.x + ", y = " + newrect.y + ", width = " + newrect.width + ", height = " + newrect.height);
        }
        if ((weightFlags & 4) != 0) {
            int i;
            for (i = ncomponents - 1; i >= 0; --i) {
                this.runOneComponent(comps[i], null, g, clip, 2);
            }
            for (i = ncomponents - 1; i >= 0; --i) {
                this.runOneComponent(comps[i], null, g, clip, 1);
            }
        } else {
            for (int i = ncomponents - 1; i >= 0; --i) {
                this.runOneComponent(comps[i], null, g, clip, weightFlags);
            }
        }
    }

    public static final class PrintHeavyweightComponentsCallback
    extends SunGraphicsCallback {
        private static PrintHeavyweightComponentsCallback instance = new PrintHeavyweightComponentsCallback();

        private PrintHeavyweightComponentsCallback() {
        }

        @Override
        public void run(Component comp, Graphics cg) {
            if (!comp.isLightweight()) {
                comp.printAll(cg);
            } else if (comp instanceof Container) {
                this.runComponents(((Container)comp).getComponents(), cg, 3);
            }
        }

        public static PrintHeavyweightComponentsCallback getInstance() {
            return instance;
        }
    }

    public static final class PaintHeavyweightComponentsCallback
    extends SunGraphicsCallback {
        private static PaintHeavyweightComponentsCallback instance = new PaintHeavyweightComponentsCallback();

        private PaintHeavyweightComponentsCallback() {
        }

        @Override
        public void run(Component comp, Graphics cg) {
            if (!comp.isLightweight()) {
                comp.paintAll(cg);
            } else if (comp instanceof Container) {
                this.runComponents(((Container)comp).getComponents(), cg, 3);
            }
        }

        public static PaintHeavyweightComponentsCallback getInstance() {
            return instance;
        }
    }
}

