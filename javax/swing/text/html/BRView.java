/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import javax.swing.text.Element;
import javax.swing.text.html.InlineView;

class BRView
extends InlineView {
    public BRView(Element elem) {
        super(elem);
    }

    @Override
    public int getBreakWeight(int axis, float pos, float len) {
        if (axis == 0) {
            return 3000;
        }
        return super.getBreakWeight(axis, pos, len);
    }
}

