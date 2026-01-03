/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JPopupMenu;
import javax.swing.plaf.UIResource;
import sun.swing.MenuItemLayoutHelper;

public class DefaultMenuLayout
extends BoxLayout
implements UIResource {
    public DefaultMenuLayout(Container target, int axis) {
        super(target, axis);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        if (target instanceof JPopupMenu) {
            JPopupMenu popupMenu = (JPopupMenu)target;
            MenuItemLayoutHelper.clearUsedClientProperties(popupMenu);
            if (popupMenu.getComponentCount() == 0) {
                return new Dimension(0, 0);
            }
        }
        super.invalidateLayout(target);
        return super.preferredLayoutSize(target);
    }
}

