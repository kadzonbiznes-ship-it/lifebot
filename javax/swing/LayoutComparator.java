/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

final class LayoutComparator
implements Comparator<Component>,
Serializable {
    private static final int ROW_TOLERANCE = 10;
    private boolean horizontal = true;
    private boolean leftToRight = true;

    LayoutComparator() {
    }

    void setComponentOrientation(ComponentOrientation orientation) {
        this.horizontal = orientation.isHorizontal();
        this.leftToRight = orientation.isLeftToRight();
    }

    @Override
    public int compare(Component a, Component b) {
        if (a == b) {
            return 0;
        }
        if (a.getParent() != b.getParent()) {
            ArrayList<Component> aAncestory = new ArrayList<Component>();
            while (a != null) {
                aAncestory.add(a);
                if (a instanceof Window) break;
                a = a.getParent();
            }
            if (a == null) {
                throw new ClassCastException();
            }
            ArrayList<Component> bAncestory = new ArrayList<Component>();
            while (b != null) {
                bAncestory.add(b);
                if (b instanceof Window) break;
                b = b.getParent();
            }
            if (b == null) {
                throw new ClassCastException();
            }
            ListIterator aIter = aAncestory.listIterator(aAncestory.size());
            ListIterator bIter = bAncestory.listIterator(bAncestory.size());
            do {
                if (!aIter.hasPrevious()) {
                    return -1;
                }
                a = (Component)aIter.previous();
                if (!bIter.hasPrevious()) {
                    return 1;
                }
                b = (Component)bIter.previous();
            } while (a == b);
        }
        int ax = a.getX();
        int ay = a.getY();
        int bx = b.getX();
        int by = b.getY();
        int zOrder = a.getParent().getComponentZOrder(a) - b.getParent().getComponentZOrder(b);
        if (this.horizontal) {
            if (this.leftToRight) {
                if (Math.abs(ay - by) < 10) {
                    return ax < bx ? -1 : (ax > bx ? 1 : zOrder);
                }
                return ay < by ? -1 : 1;
            }
            if (Math.abs(ay - by) < 10) {
                return ax > bx ? -1 : (ax < bx ? 1 : zOrder);
            }
            return ay < by ? -1 : 1;
        }
        if (this.leftToRight) {
            if (Math.abs(ax - bx) < 10) {
                return ay < by ? -1 : (ay > by ? 1 : zOrder);
            }
            return ax < bx ? -1 : 1;
        }
        if (Math.abs(ax - bx) < 10) {
            return ay < by ? -1 : (ay > by ? 1 : zOrder);
        }
        return ax > bx ? -1 : 1;
    }
}

