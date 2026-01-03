/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import sun.awt.AWTAccessor;
import sun.awt.Win32GraphicsConfig;
import sun.awt.util.ThreadGroupUtils;
import sun.awt.windows.WComponentPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DGraphicsConfig;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.windows.GDIWindowSurfaceData;
import sun.java2d.windows.WindowsFlags;

public class D3DScreenUpdateManager
extends ScreenUpdateManager
implements Runnable {
    private static final int MIN_WIN_SIZE = 150;
    private volatile boolean done = false;
    private volatile Thread screenUpdater;
    private boolean needsUpdateNow;
    private Object runLock = new Object();
    private ArrayList<D3DSurfaceData.D3DWindowSurfaceData> d3dwSurfaces;
    private HashMap<D3DSurfaceData.D3DWindowSurfaceData, GDIWindowSurfaceData> gdiSurfaces;

    public D3DScreenUpdateManager() {
        AccessController.doPrivileged(() -> {
            Runnable shutdownRunnable = () -> {
                this.done = true;
                this.wakeUpUpdateThread();
            };
            Thread shutdown = new Thread(ThreadGroupUtils.getRootThreadGroup(), shutdownRunnable, "ScreenUpdater", 0L, false);
            shutdown.setContextClassLoader(null);
            try {
                Runtime.getRuntime().addShutdownHook(shutdown);
            }
            catch (Exception e) {
                this.done = true;
            }
            return null;
        });
    }

    @Override
    public SurfaceData createScreenSurface(Win32GraphicsConfig gc, WComponentPeer peer, int bbNum, boolean isResize) {
        if (this.done || !(gc instanceof D3DGraphicsConfig)) {
            return super.createScreenSurface(gc, peer, bbNum, isResize);
        }
        SurfaceData sd = null;
        if (D3DScreenUpdateManager.canUseD3DOnScreen(peer, gc, bbNum)) {
            try {
                sd = D3DSurfaceData.createData(peer);
            }
            catch (InvalidPipeException ipe) {
                sd = null;
            }
        }
        if (sd == null) {
            sd = GDIWindowSurfaceData.createData(peer);
        }
        if (isResize) {
            this.repaintPeerTarget(peer);
        }
        return sd;
    }

    public static boolean canUseD3DOnScreen(WComponentPeer peer, Win32GraphicsConfig gc, int bbNum) {
        if (!(gc instanceof D3DGraphicsConfig)) {
            return false;
        }
        D3DGraphicsConfig d3dgc = (D3DGraphicsConfig)gc;
        D3DGraphicsDevice d3dgd = d3dgc.getD3DDevice();
        String peerName = peer.getClass().getName();
        Rectangle r = peer.getBounds();
        Component target = (Component)peer.getTarget();
        Window fsw = d3dgd.getFullScreenWindow();
        return !(!WindowsFlags.isD3DOnScreenEnabled() || !d3dgd.isD3DEnabledOnDevice() || !peer.isAccelCapable() || r.width <= 150 && r.height <= 150 || bbNum != 0 || fsw != null && (fsw != target || D3DScreenUpdateManager.hasHWChildren(target)) || !peerName.equals("sun.awt.windows.WCanvasPeer") && !peerName.equals("sun.awt.windows.WDialogPeer") && !peerName.equals("sun.awt.windows.WPanelPeer") && !peerName.equals("sun.awt.windows.WWindowPeer") && !peerName.equals("sun.awt.windows.WFramePeer") && !peerName.equals("sun.awt.windows.WEmbeddedFramePeer"));
    }

    @Override
    public Graphics2D createGraphics(SurfaceData sd, WComponentPeer peer, Color fgColor, Color bgColor, Font font) {
        if (!this.done && sd instanceof D3DSurfaceData.D3DWindowSurfaceData) {
            D3DSurfaceData.D3DWindowSurfaceData d3dw = (D3DSurfaceData.D3DWindowSurfaceData)sd;
            if (!d3dw.isSurfaceLost() || this.validate(d3dw)) {
                this.trackScreenSurface(d3dw);
                return new SunGraphics2D(sd, fgColor, bgColor, font);
            }
            sd = this.getGdiSurface(d3dw);
        }
        return super.createGraphics(sd, peer, fgColor, bgColor, font);
    }

    private void repaintPeerTarget(WComponentPeer peer) {
        Component target = (Component)peer.getTarget();
        Rectangle bounds = AWTAccessor.getComponentAccessor().getBounds(target);
        peer.handlePaint(0, 0, bounds.width, bounds.height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void trackScreenSurface(SurfaceData sd) {
        if (!this.done && sd instanceof D3DSurfaceData.D3DWindowSurfaceData) {
            D3DScreenUpdateManager d3DScreenUpdateManager = this;
            synchronized (d3DScreenUpdateManager) {
                D3DSurfaceData.D3DWindowSurfaceData d3dw;
                if (this.d3dwSurfaces == null) {
                    this.d3dwSurfaces = new ArrayList();
                }
                if (!this.d3dwSurfaces.contains(d3dw = (D3DSurfaceData.D3DWindowSurfaceData)sd)) {
                    this.d3dwSurfaces.add(d3dw);
                }
            }
            this.startUpdateThread();
        }
    }

    @Override
    public synchronized void dropScreenSurface(SurfaceData sd) {
        if (this.d3dwSurfaces != null && sd instanceof D3DSurfaceData.D3DWindowSurfaceData) {
            D3DSurfaceData.D3DWindowSurfaceData d3dw = (D3DSurfaceData.D3DWindowSurfaceData)sd;
            this.removeGdiSurface(d3dw);
            this.d3dwSurfaces.remove(d3dw);
        }
    }

    @Override
    public SurfaceData getReplacementScreenSurface(WComponentPeer peer, SurfaceData sd) {
        SurfaceData newSurface = super.getReplacementScreenSurface(peer, sd);
        this.trackScreenSurface(newSurface);
        return newSurface;
    }

    private void removeGdiSurface(D3DSurfaceData.D3DWindowSurfaceData d3dw) {
        GDIWindowSurfaceData gdisd;
        if (this.gdiSurfaces != null && (gdisd = this.gdiSurfaces.get(d3dw)) != null) {
            gdisd.invalidate();
            this.gdiSurfaces.remove(d3dw);
        }
    }

    private synchronized void startUpdateThread() {
        if (this.screenUpdater == null) {
            this.screenUpdater = AccessController.doPrivileged(() -> {
                String name = "D3D Screen Updater";
                Thread t = new Thread(ThreadGroupUtils.getRootThreadGroup(), this, name, 0L, false);
                t.setPriority(7);
                t.setDaemon(true);
                return t;
            });
            this.screenUpdater.start();
        } else {
            this.wakeUpUpdateThread();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void wakeUpUpdateThread() {
        Object object = this.runLock;
        synchronized (object) {
            this.runLock.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void runUpdateNow() {
        Object object = this;
        synchronized (object) {
            if (this.done || this.screenUpdater == null || this.d3dwSurfaces == null || this.d3dwSurfaces.size() == 0) {
                return;
            }
        }
        object = this.runLock;
        synchronized (object) {
            this.needsUpdateNow = true;
            this.runLock.notifyAll();
            while (this.needsUpdateNow) {
                try {
                    this.runLock.wait();
                }
                catch (InterruptedException interruptedException) {}
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        while (!this.done) {
            Object object = this.runLock;
            synchronized (object) {
                long l;
                long l2 = l = this.d3dwSurfaces.size() > 0 ? 100L : 0L;
                if (!this.needsUpdateNow) {
                    try {
                        this.runLock.wait(l);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
            }
            D3DSurfaceData.D3DWindowSurfaceData[] surfaces = new D3DSurfaceData.D3DWindowSurfaceData[]{};
            D3DScreenUpdateManager d3DScreenUpdateManager = this;
            synchronized (d3DScreenUpdateManager) {
                surfaces = this.d3dwSurfaces.toArray(surfaces);
            }
            for (D3DSurfaceData.D3DWindowSurfaceData sd : surfaces) {
                if (!sd.isValid() || !sd.isDirty() && !sd.isSurfaceLost()) continue;
                if (!sd.isSurfaceLost()) {
                    D3DRenderQueue rq = D3DRenderQueue.getInstance();
                    rq.lock();
                    try {
                        Rectangle r = sd.getBounds();
                        D3DSurfaceData.swapBuffers(sd, 0, 0, r.width, r.height);
                        sd.markClean();
                        continue;
                    }
                    finally {
                        rq.unlock();
                    }
                }
                if (this.validate(sd)) continue;
                sd.getPeer().replaceSurfaceDataLater();
            }
            Object object2 = this.runLock;
            synchronized (object2) {
                this.needsUpdateNow = false;
                this.runLock.notifyAll();
            }
        }
    }

    private boolean validate(D3DSurfaceData.D3DWindowSurfaceData sd) {
        if (sd.isSurfaceLost()) {
            try {
                sd.restoreSurface();
                Color bg = sd.getPeer().getBackgroundNoSync();
                SunGraphics2D sg2d = new SunGraphics2D(sd, bg, bg, null);
                sg2d.fillRect(0, 0, sd.getBounds().width, sd.getBounds().height);
                sg2d.dispose();
                sd.markClean();
                this.repaintPeerTarget(sd.getPeer());
            }
            catch (InvalidPipeException ipe) {
                return false;
            }
        }
        return true;
    }

    private synchronized SurfaceData getGdiSurface(D3DSurfaceData.D3DWindowSurfaceData d3dw) {
        GDIWindowSurfaceData gdisd;
        if (this.gdiSurfaces == null) {
            this.gdiSurfaces = new HashMap();
        }
        if ((gdisd = this.gdiSurfaces.get(d3dw)) == null) {
            gdisd = GDIWindowSurfaceData.createData(d3dw.getPeer());
            this.gdiSurfaces.put(d3dw, gdisd);
        }
        return gdisd;
    }

    private static boolean hasHWChildren(Component comp) {
        AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
        if (comp instanceof Container) {
            for (Component c : ((Container)comp).getComponents()) {
                if (!(acc.getPeer(c) instanceof WComponentPeer) && !D3DScreenUpdateManager.hasHWChildren(c)) continue;
                return true;
            }
        }
        return false;
    }
}

