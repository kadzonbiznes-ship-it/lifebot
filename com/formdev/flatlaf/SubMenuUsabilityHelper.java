/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.SubMenuUsabilityHelper$SafeTrianglePainter
 *  com.formdev.flatlaf.SubMenuUsabilityHelper$SubMenuEventQueue
 */
package com.formdev.flatlaf;

import com.formdev.flatlaf.SubMenuUsabilityHelper;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class SubMenuUsabilityHelper
implements ChangeListener {
    private static final String KEY_USE_SAFE_TRIANGLE = "Menu.useSafeTriangle";
    private static final String KEY_SHOW_SAFE_TRIANGLE = "FlatLaf.debug.menu.showSafeTriangle";
    private static SubMenuUsabilityHelper instance;
    private SubMenuEventQueue subMenuEventQueue;
    private SafeTrianglePainter safeTrianglePainter;
    private boolean changePending;
    private int mouseX;
    private int mouseY;
    private int targetX;
    private int targetTopY;
    private int targetBottomY;
    private Rectangle invokerBounds;

    SubMenuUsabilityHelper() {
    }

    static synchronized boolean install() {
        if (instance != null) {
            return false;
        }
        instance = new SubMenuUsabilityHelper();
        MenuSelectionManager.defaultManager().addChangeListener(instance);
        return true;
    }

    static synchronized void uninstall() {
        if (instance == null) {
            return;
        }
        MenuSelectionManager.defaultManager().removeChangeListener(instance);
        instance.uninstallEventQueue();
        instance = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (!FlatUIUtils.getUIBoolean(KEY_USE_SAFE_TRIANGLE, true)) {
            return;
        }
        SubMenuUsabilityHelper subMenuUsabilityHelper = this;
        synchronized (subMenuUsabilityHelper) {
            if (this.changePending) {
                return;
            }
            this.changePending = true;
        }
        EventQueue.invokeLater(() -> {
            SubMenuUsabilityHelper subMenuUsabilityHelper = this;
            synchronized (subMenuUsabilityHelper) {
                this.changePending = false;
            }
            this.menuSelectionChanged();
        });
    }

    private void menuSelectionChanged() {
        MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
        int subMenuIndex = this.findSubMenu(path);
        if (subMenuIndex < 0 || subMenuIndex != path.length - 1) {
            this.uninstallEventQueue();
            return;
        }
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point mouseLocation = pointerInfo != null ? pointerInfo.getLocation() : new Point();
        this.mouseX = mouseLocation.x;
        this.mouseY = mouseLocation.y;
        JPopupMenu popup = (JPopupMenu)path[subMenuIndex];
        if (!popup.isShowing()) {
            this.uninstallEventQueue();
            return;
        }
        Component invoker = popup.getInvoker();
        Rectangle rectangle = this.invokerBounds = invoker != null && invoker.isShowing() ? new Rectangle(invoker.getLocationOnScreen(), invoker.getSize()) : null;
        if (this.invokerBounds != null && !this.invokerBounds.contains(this.mouseX, this.mouseY)) {
            this.uninstallEventQueue();
            return;
        }
        Point popupLocation = popup.getLocationOnScreen();
        Dimension popupSize = popup.getSize();
        this.targetX = this.mouseX < popupLocation.x + popupSize.width / 2 ? popupLocation.x : popupLocation.x + popupSize.width;
        this.targetTopY = popupLocation.y;
        this.targetBottomY = popupLocation.y + popupSize.height;
        if (this.subMenuEventQueue == null) {
            this.subMenuEventQueue = new SubMenuEventQueue(this);
        }
        if (this.safeTrianglePainter == null && UIManager.getBoolean(KEY_SHOW_SAFE_TRIANGLE)) {
            this.safeTrianglePainter = new SafeTrianglePainter(this, popup);
        }
    }

    private void uninstallEventQueue() {
        if (this.subMenuEventQueue != null) {
            this.subMenuEventQueue.uninstall();
            this.subMenuEventQueue = null;
        }
        if (this.safeTrianglePainter != null) {
            this.safeTrianglePainter.uninstall();
            this.safeTrianglePainter = null;
        }
    }

    private int findSubMenu(MenuElement[] path) {
        for (int i = path.length - 1; i >= 1; --i) {
            if (!(path[i] instanceof JPopupMenu) || !(path[i - 1] instanceof JMenu) || ((JMenu)path[i - 1]).isTopLevelMenu()) continue;
            return i;
        }
        return -1;
    }

    private Polygon createSafeTriangle() {
        return new Polygon(new int[]{this.mouseX, this.targetX, this.targetX}, new int[]{this.mouseY, this.targetTopY, this.targetBottomY}, 3);
    }

    static /* synthetic */ SafeTrianglePainter access$000(SubMenuUsabilityHelper x0) {
        return x0.safeTrianglePainter;
    }

    static /* synthetic */ Polygon access$100(SubMenuUsabilityHelper x0) {
        return x0.createSafeTriangle();
    }

    static /* synthetic */ int access$202(SubMenuUsabilityHelper x0, int x1) {
        x0.mouseX = x1;
        return x0.mouseX;
    }

    static /* synthetic */ int access$302(SubMenuUsabilityHelper x0, int x1) {
        x0.mouseY = x1;
        return x0.mouseY;
    }

    static /* synthetic */ Rectangle access$400(SubMenuUsabilityHelper x0) {
        return x0.invokerBounds;
    }

    static /* synthetic */ void access$500(SubMenuUsabilityHelper x0) {
        x0.uninstallEventQueue();
    }
}

