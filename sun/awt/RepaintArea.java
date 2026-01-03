/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

public class RepaintArea {
    private static final int MAX_BENEFIT_RATIO = 4;
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    private static final int UPDATE = 2;
    private static final int RECT_COUNT = 3;
    private Rectangle[] paintRects = new Rectangle[3];

    public RepaintArea() {
    }

    private RepaintArea(RepaintArea ra) {
        for (int i = 0; i < 3; ++i) {
            this.paintRects[i] = ra.paintRects[i];
        }
    }

    public synchronized void add(Rectangle r, int id) {
        if (r.isEmpty()) {
            return;
        }
        int addTo = 2;
        if (id == 800) {
            int n = addTo = r.width > r.height ? 0 : 1;
        }
        if (this.paintRects[addTo] != null) {
            this.paintRects[addTo].add(r);
        } else {
            this.paintRects[addTo] = new Rectangle(r);
        }
    }

    private synchronized RepaintArea cloneAndReset() {
        RepaintArea ra = new RepaintArea(this);
        for (int i = 0; i < 3; ++i) {
            this.paintRects[i] = null;
        }
        return ra;
    }

    public boolean isEmpty() {
        for (int i = 0; i < 3; ++i) {
            if (this.paintRects[i] == null) continue;
            return false;
        }
        return true;
    }

    public synchronized void constrain(int x, int y, int w, int h) {
        for (int i = 0; i < 3; ++i) {
            int yDelta;
            int xDelta;
            Rectangle rect = this.paintRects[i];
            if (rect == null) continue;
            if (rect.x < x) {
                rect.width -= x - rect.x;
                rect.x = x;
            }
            if (rect.y < y) {
                rect.height -= y - rect.y;
                rect.y = y;
            }
            if ((xDelta = rect.x + rect.width - x - w) > 0) {
                rect.width -= xDelta;
            }
            if ((yDelta = rect.y + rect.height - y - h) > 0) {
                rect.height -= yDelta;
            }
            if (rect.width > 0 && rect.height > 0) continue;
            this.paintRects[i] = null;
        }
    }

    public synchronized void subtract(int x, int y, int w, int h) {
        Rectangle subtract = new Rectangle(x, y, w, h);
        for (int i = 0; i < 3; ++i) {
            if (!RepaintArea.subtract(this.paintRects[i], subtract) || this.paintRects[i] == null || !this.paintRects[i].isEmpty()) continue;
            this.paintRects[i] = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void paint(Object target, boolean shouldClearRectBeforePaint) {
        Component comp = (Component)target;
        if (this.isEmpty()) {
            return;
        }
        if (!comp.isVisible()) {
            return;
        }
        RepaintArea ra = this.cloneAndReset();
        if (!RepaintArea.subtract(ra.paintRects[1], ra.paintRects[0])) {
            RepaintArea.subtract(ra.paintRects[0], ra.paintRects[1]);
        }
        if (ra.paintRects[0] != null && ra.paintRects[1] != null) {
            Rectangle paintRect = ra.paintRects[0].union(ra.paintRects[1]);
            int square = paintRect.width * paintRect.height;
            int benefit = square - ra.paintRects[0].width * ra.paintRects[0].height - ra.paintRects[1].width * ra.paintRects[1].height;
            if (4 * benefit < square) {
                ra.paintRects[0] = paintRect;
                ra.paintRects[1] = null;
            }
        }
        for (int i = 0; i < this.paintRects.length; ++i) {
            Graphics g;
            if (ra.paintRects[i] == null || ra.paintRects[i].isEmpty() || (g = comp.getGraphics()) == null) continue;
            try {
                g.setClip(ra.paintRects[i]);
                if (i == 2) {
                    this.updateComponent(comp, g);
                    continue;
                }
                if (shouldClearRectBeforePaint) {
                    g.clearRect(ra.paintRects[i].x, ra.paintRects[i].y, ra.paintRects[i].width, ra.paintRects[i].height);
                }
                this.paintComponent(comp, g);
                continue;
            }
            finally {
                g.dispose();
            }
        }
    }

    protected void updateComponent(Component comp, Graphics g) {
        if (comp != null) {
            comp.update(g);
        }
    }

    protected void paintComponent(Component comp, Graphics g) {
        if (comp != null) {
            comp.paint(g);
        }
    }

    static boolean subtract(Rectangle rect, Rectangle subtr) {
        if (rect == null || subtr == null) {
            return true;
        }
        Rectangle common = rect.intersection(subtr);
        if (common.isEmpty()) {
            return true;
        }
        if (rect.x == common.x && rect.y == common.y) {
            if (rect.width == common.width) {
                rect.y += common.height;
                rect.height -= common.height;
                return true;
            }
            if (rect.height == common.height) {
                rect.x += common.width;
                rect.width -= common.width;
                return true;
            }
        } else if (rect.x + rect.width == common.x + common.width && rect.y + rect.height == common.y + common.height) {
            if (rect.width == common.width) {
                rect.height -= common.height;
                return true;
            }
            if (rect.height == common.height) {
                rect.width -= common.width;
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return super.toString() + "[ horizontal=" + String.valueOf(this.paintRects[0]) + " vertical=" + String.valueOf(this.paintRects[1]) + " update=" + String.valueOf(this.paintRects[2]) + "]";
    }
}

