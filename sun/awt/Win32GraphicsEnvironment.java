/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTError;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.peer.ComponentPeer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ListIterator;
import sun.awt.DisplayChangedListener;
import sun.awt.Win32GraphicsDevice;
import sun.awt.windows.WToolkit;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SurfaceManagerFactory;
import sun.java2d.WindowsSurfaceManagerFactory;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.windows.WindowsFlags;

public final class Win32GraphicsEnvironment
extends SunGraphicsEnvironment {
    static final float debugScaleX;
    static final float debugScaleY;
    private static boolean displayInitialized;
    private ArrayList<WeakReference<Win32GraphicsDevice>> oldDevices;
    private static volatile boolean isDWMCompositionEnabled;

    private static native void initDisplay();

    public static void initDisplayWrapper() {
        if (!displayInitialized) {
            displayInitialized = true;
            Win32GraphicsEnvironment.initDisplay();
        }
    }

    @Override
    protected native int getNumScreens();

    private native int getDefaultScreen();

    @Override
    public GraphicsDevice getDefaultScreenDevice() {
        GraphicsDevice[] screens = this.getScreenDevices();
        if (screens.length == 0) {
            throw new AWTError("no screen devices");
        }
        int index = this.getDefaultScreen();
        return screens[0 < index && index < screens.length ? index : 0];
    }

    public native int getXResolution();

    public native int getYResolution();

    @Override
    public void displayChanged() {
        int i;
        GraphicsDevice[] newDevices = new GraphicsDevice[this.getNumScreens()];
        GraphicsDevice[] oldScreens = this.screens;
        if (oldScreens != null) {
            for (i = 0; i < oldScreens.length; ++i) {
                if (!(this.screens[i] instanceof Win32GraphicsDevice)) {
                    assert (false) : oldScreens[i];
                    continue;
                }
                Win32GraphicsDevice gd = (Win32GraphicsDevice)oldScreens[i];
                if (!gd.isValid()) {
                    if (this.oldDevices == null) {
                        this.oldDevices = new ArrayList();
                    }
                    this.oldDevices.add(new WeakReference<Win32GraphicsDevice>(gd));
                    continue;
                }
                if (i >= newDevices.length) continue;
                newDevices[i] = gd;
            }
            oldScreens = null;
        }
        for (i = 0; i < newDevices.length; ++i) {
            if (newDevices[i] != null) continue;
            newDevices[i] = this.makeScreenDevice(i);
        }
        for (GraphicsDevice gd : this.screens = newDevices) {
            if (!(gd instanceof DisplayChangedListener)) continue;
            ((DisplayChangedListener)((Object)gd)).displayChanged();
        }
        if (this.oldDevices != null) {
            int defScreen = this.getDefaultScreen();
            ListIterator<WeakReference<Win32GraphicsDevice>> it = this.oldDevices.listIterator();
            while (it.hasNext()) {
                Win32GraphicsDevice gd = (Win32GraphicsDevice)it.next().get();
                if (gd != null) {
                    gd.invalidate(defScreen);
                    gd.displayChanged();
                    continue;
                }
                it.remove();
            }
        }
        this.displayChanger.notifyListeners();
    }

    @Override
    protected GraphicsDevice makeScreenDevice(int screennum) {
        Win32GraphicsDevice device = null;
        if (WindowsFlags.isD3DEnabled()) {
            device = D3DGraphicsDevice.createDevice(screennum);
        }
        if (device == null) {
            device = new Win32GraphicsDevice(screennum);
        }
        return device;
    }

    @Override
    public boolean isDisplayLocal() {
        return true;
    }

    @Override
    public boolean isFlipStrategyPreferred(ComponentPeer peer) {
        GraphicsDevice gd;
        GraphicsConfiguration gc;
        if (peer != null && (gc = peer.getGraphicsConfiguration()) != null && (gd = gc.getDevice()) instanceof D3DGraphicsDevice) {
            return ((D3DGraphicsDevice)gd).isD3DEnabledOnDevice();
        }
        return false;
    }

    public static boolean isDWMCompositionEnabled() {
        return isDWMCompositionEnabled;
    }

    private static void dwmCompositionChanged(boolean enabled) {
        isDWMCompositionEnabled = enabled;
    }

    public static native boolean isVistaOS();

    static {
        WToolkit.loadLibraries();
        WindowsFlags.initFlags();
        Win32GraphicsEnvironment.initDisplayWrapper();
        SurfaceManagerFactory.setInstance(new WindowsSurfaceManagerFactory());
        double sx = -1.0;
        double sy = -1.0;
        if (Win32GraphicsEnvironment.isUIScaleEnabled()) {
            sx = Win32GraphicsEnvironment.getScaleFactor("sun.java2d.win.uiScaleX");
            sy = Win32GraphicsEnvironment.getScaleFactor("sun.java2d.win.uiScaleY");
            if (sx <= 0.0 || sy <= 0.0) {
                double s;
                sx = s = Win32GraphicsEnvironment.getDebugScale();
                sy = s;
            }
        }
        debugScaleX = (float)sx;
        debugScaleY = (float)sy;
    }
}

