/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatWindowsNativeWindowBorder$WndProc
 *  com.formdev.flatlaf.util.LoggingFacade
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.ui.FlatWindowsNativeWindowBorder;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/*
 * Exception performing whole class analysis ignored.
 */
class FlatWindowsNativeWindowBorder
implements FlatNativeWindowBorder.Provider {
    private final Map<Window, WndProc> windowsMap = Collections.synchronizedMap(new IdentityHashMap());
    private final EventListenerList listenerList = new EventListenerList();
    private Timer fireStateChangedTimer;
    private boolean colorizationUpToDate;
    private boolean colorizationColorAffectsBorders;
    private Color colorizationColor;
    private int colorizationColorBalance;
    private static FlatWindowsNativeWindowBorder instance;

    static FlatNativeWindowBorder.Provider getInstance() {
        if (!SystemInfo.isWindows_10_orLater) {
            return null;
        }
        if (!FlatNativeLibrary.isLoaded()) {
            return null;
        }
        if (instance == null) {
            instance = new FlatWindowsNativeWindowBorder();
        }
        return instance;
    }

    private FlatWindowsNativeWindowBorder() {
    }

    @Override
    public boolean hasCustomDecoration(Window window) {
        return this.windowsMap.containsKey(window);
    }

    @Override
    public void setHasCustomDecoration(Window window, boolean hasCustomDecoration) {
        if (hasCustomDecoration) {
            this.install(window);
        } else {
            this.uninstall(window);
        }
    }

    private void install(Window window) {
        if (!SystemInfo.isWindows_10_orLater) {
            return;
        }
        if (!(window instanceof JFrame) && !(window instanceof JDialog)) {
            return;
        }
        if (window instanceof Frame && ((Frame)window).isUndecorated() || window instanceof Dialog && ((Dialog)window).isUndecorated()) {
            return;
        }
        if (this.windowsMap.containsKey(window)) {
            return;
        }
        try {
            WndProc wndProc = new WndProc(this, window);
            if (WndProc.access$000((WndProc)wndProc) == 0L) {
                return;
            }
            this.windowsMap.put(window, wndProc);
        }
        catch (UnsatisfiedLinkError ex) {
            LoggingFacade.INSTANCE.logSevere(null, (Throwable)ex);
        }
    }

    private void uninstall(Window window) {
        WndProc wndProc = this.windowsMap.remove(window);
        if (wndProc != null) {
            wndProc.uninstall();
        }
    }

    @Override
    public void updateTitleBarInfo(Window window, int titleBarHeight, List<Rectangle> hitTestSpots, Rectangle appIconBounds, Rectangle minimizeButtonBounds, Rectangle maximizeButtonBounds, Rectangle closeButtonBounds) {
        WndProc wndProc = this.windowsMap.get(window);
        if (wndProc == null) {
            return;
        }
        WndProc.access$102((WndProc)wndProc, (int)titleBarHeight);
        WndProc.access$202((WndProc)wndProc, (Rectangle[])hitTestSpots.toArray(new Rectangle[hitTestSpots.size()]));
        WndProc.access$302((WndProc)wndProc, (Rectangle)FlatWindowsNativeWindowBorder.cloneRectange(appIconBounds));
        WndProc.access$402((WndProc)wndProc, (Rectangle)FlatWindowsNativeWindowBorder.cloneRectange(minimizeButtonBounds));
        WndProc.access$502((WndProc)wndProc, (Rectangle)FlatWindowsNativeWindowBorder.cloneRectange(maximizeButtonBounds));
        WndProc.access$602((WndProc)wndProc, (Rectangle)FlatWindowsNativeWindowBorder.cloneRectange(closeButtonBounds));
    }

    private static Rectangle cloneRectange(Rectangle rect) {
        return rect != null ? new Rectangle(rect) : null;
    }

    @Override
    public boolean showWindow(Window window, int cmd) {
        WndProc wndProc = this.windowsMap.get(window);
        if (wndProc == null) {
            return false;
        }
        WndProc.access$700((WndProc)wndProc, (long)WndProc.access$000((WndProc)wndProc), (int)cmd);
        return true;
    }

    @Override
    public boolean isColorizationColorAffectsBorders() {
        this.updateColorization();
        return this.colorizationColorAffectsBorders;
    }

    @Override
    public Color getColorizationColor() {
        this.updateColorization();
        return this.colorizationColor;
    }

    @Override
    public int getColorizationColorBalance() {
        this.updateColorization();
        return this.colorizationColorBalance;
    }

    private void updateColorization() {
        if (this.colorizationUpToDate) {
            return;
        }
        this.colorizationUpToDate = true;
        String subKey = "SOFTWARE\\Microsoft\\Windows\\DWM";
        int value = FlatWindowsNativeWindowBorder.registryGetIntValue(subKey, "ColorPrevalence", -1);
        this.colorizationColorAffectsBorders = value > 0;
        value = FlatWindowsNativeWindowBorder.registryGetIntValue(subKey, "ColorizationColor", -1);
        this.colorizationColor = value != -1 ? new Color(value) : null;
        this.colorizationColorBalance = FlatWindowsNativeWindowBorder.registryGetIntValue(subKey, "ColorizationColorBalance", -1);
    }

    private static native int registryGetIntValue(String var0, String var1, int var2);

    @Override
    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    private void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        if (listeners.length == 0) {
            return;
        }
        ChangeEvent e = new ChangeEvent(this);
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] != ChangeListener.class) continue;
            ((ChangeListener)listeners[i + 1]).stateChanged(e);
        }
    }

    void fireStateChangedLaterOnce() {
        EventQueue.invokeLater(() -> {
            if (this.fireStateChangedTimer != null) {
                this.fireStateChangedTimer.restart();
                return;
            }
            this.fireStateChangedTimer = new Timer(300, e -> {
                this.fireStateChangedTimer = null;
                this.colorizationUpToDate = false;
                this.fireStateChanged();
            });
            this.fireStateChangedTimer.setRepeats(false);
            this.fireStateChangedTimer.start();
        });
    }
}

