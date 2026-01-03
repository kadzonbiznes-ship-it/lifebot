/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.io.Serializable;
import javax.swing.JViewport;
import javax.swing.Scrollable;

public class ViewportLayout
implements LayoutManager,
Serializable {
    static ViewportLayout SHARED_INSTANCE = new ViewportLayout();

    @Override
    public void addLayoutComponent(String name, Component c) {
    }

    @Override
    public void removeLayoutComponent(Component c) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Component view = ((JViewport)parent).getView();
        if (view == null) {
            return new Dimension(0, 0);
        }
        if (view instanceof Scrollable) {
            return ((Scrollable)((Object)view)).getPreferredScrollableViewportSize();
        }
        return view.getPreferredSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(4, 4);
    }

    @Override
    public void layoutContainer(Container parent) {
        JViewport vp = (JViewport)parent;
        Component view = vp.getView();
        Scrollable scrollableView = null;
        if (view == null) {
            return;
        }
        if (view instanceof Scrollable) {
            scrollableView = (Scrollable)((Object)view);
        }
        Insets insets = vp.getInsets();
        Dimension viewPrefSize = view.getPreferredSize();
        Dimension vpSize = vp.getSize();
        Dimension extentSize = vp.toViewCoordinates(vpSize);
        Dimension viewSize = new Dimension(viewPrefSize);
        if (scrollableView != null) {
            if (scrollableView.getScrollableTracksViewportWidth()) {
                viewSize.width = vpSize.width;
            }
            if (scrollableView.getScrollableTracksViewportHeight()) {
                viewSize.height = vpSize.height;
            }
        }
        Point viewPosition = vp.getViewPosition();
        if (scrollableView == null || vp.getParent() == null || vp.getParent().getComponentOrientation().isLeftToRight()) {
            if (viewPosition.x + extentSize.width > viewSize.width) {
                viewPosition.x = Math.max(0, viewSize.width - extentSize.width);
            }
        } else {
            viewPosition.x = extentSize.width > viewSize.width ? viewSize.width - extentSize.width : Math.max(0, Math.min(viewSize.width - extentSize.width, viewPosition.x));
        }
        if (viewPosition.y + extentSize.height > viewSize.height) {
            viewPosition.y = Math.max(0, viewSize.height - extentSize.height);
        }
        if (scrollableView == null) {
            if (viewPosition.x == 0 && vpSize.width > viewPrefSize.width) {
                viewSize.width = vpSize.width;
            }
            if (viewPosition.y == 0 && vpSize.height > viewPrefSize.height) {
                viewSize.height = vpSize.height;
            }
        }
        vp.setViewPosition(viewPosition);
        vp.setViewSize(viewSize);
    }
}

