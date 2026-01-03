/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Dialog;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.peer.WindowPeer;
import java.util.ArrayList;
import sun.awt.AWTAccessor;
import sun.awt.Win32GraphicsDevice;
import sun.awt.windows.WWindowPeer;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DGraphicsConfig;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.pipe.hw.ContextCapabilities;
import sun.java2d.windows.WindowsFlags;

public final class D3DGraphicsDevice
extends Win32GraphicsDevice {
    private D3DContext context;
    private static boolean d3dAvailable;
    private ContextCapabilities d3dCaps;
    private boolean fsStatus;
    private Rectangle ownerOrigBounds = null;
    private boolean ownerWasVisible;
    private Window realFSWindow;
    private WindowListener fsWindowListener;
    private boolean fsWindowWasAlwaysOnTop;

    private static native boolean initD3D();

    public static D3DGraphicsDevice createDevice(int screen) {
        if (!d3dAvailable) {
            return null;
        }
        ContextCapabilities d3dCaps = D3DGraphicsDevice.getDeviceCaps(screen);
        if ((d3dCaps.getCaps() & 0x40000) == 0) {
            if (WindowsFlags.isD3DVerbose()) {
                System.out.println("Could not enable Direct3D pipeline on screen " + screen);
            }
            return null;
        }
        if (WindowsFlags.isD3DVerbose()) {
            System.out.println("Direct3D pipeline enabled on screen " + screen);
        }
        D3DGraphicsDevice gd = new D3DGraphicsDevice(screen, d3dCaps);
        return gd;
    }

    private static native int getDeviceCapsNative(int var0);

    private static native String getDeviceIdNative(int var0);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ContextCapabilities getDeviceCaps(final int screen) {
        D3DContext.D3DContextCaps d3dCaps = null;
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            class Result {
                int caps;
                String id;

                Result() {
                }
            }
            final Result res = new Result();
            rq.flushAndInvokeNow(new Runnable(){
                {
                }

                @Override
                public void run() {
                    res.caps = D3DGraphicsDevice.getDeviceCapsNative(screen);
                    res.id = D3DGraphicsDevice.getDeviceIdNative(screen);
                }
            });
            d3dCaps = new D3DContext.D3DContextCaps(res.caps, res.id);
        }
        finally {
            rq.unlock();
        }
        return d3dCaps != null ? d3dCaps : new D3DContext.D3DContextCaps(0, null);
    }

    public final boolean isCapPresent(int cap) {
        return (this.d3dCaps.getCaps() & cap) != 0;
    }

    private D3DGraphicsDevice(int screennum, ContextCapabilities d3dCaps) {
        super(screennum);
        this.descString = "D3DGraphicsDevice[screen=" + screennum;
        this.d3dCaps = d3dCaps;
        this.context = new D3DContext(D3DRenderQueue.getInstance(), this);
    }

    public boolean isD3DEnabledOnDevice() {
        return this.isValid() && this.isCapPresent(262144);
    }

    public static boolean isD3DAvailable() {
        return d3dAvailable;
    }

    private Frame getToplevelOwner(Window w) {
        Window owner = w;
        while (owner != null) {
            if (!((owner = owner.getOwner()) instanceof Frame)) continue;
            return (Frame)owner;
        }
        return null;
    }

    private static native boolean enterFullScreenExclusiveNative(int var0, long var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void enterFullScreenExclusive(final int screen, WindowPeer wp) {
        final WWindowPeer wpeer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(this.realFSWindow);
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.flushAndInvokeNow(new Runnable(){
                final /* synthetic */ D3DGraphicsDevice this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public void run() {
                    long hwnd = wpeer.getHWnd();
                    if (hwnd == 0L) {
                        this.this$0.fsStatus = false;
                        return;
                    }
                    this.this$0.fsStatus = D3DGraphicsDevice.enterFullScreenExclusiveNative(screen, hwnd);
                }
            });
        }
        finally {
            rq.unlock();
        }
        if (!this.fsStatus) {
            super.enterFullScreenExclusive(screen, wp);
        }
    }

    private static native boolean exitFullScreenExclusiveNative(int var0);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void exitFullScreenExclusive(final int screen, WindowPeer w) {
        if (this.fsStatus) {
            D3DRenderQueue rq = D3DRenderQueue.getInstance();
            rq.lock();
            try {
                rq.flushAndInvokeNow(new Runnable(){

                    @Override
                    public void run() {
                        D3DGraphicsDevice.exitFullScreenExclusiveNative(screen);
                    }
                });
            }
            finally {
                rq.unlock();
            }
        } else {
            super.exitFullScreenExclusive(screen, w);
        }
    }

    @Override
    protected void addFSWindowListener(Window w) {
        AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
        if (!(w instanceof Frame) && !(w instanceof Dialog) && (this.realFSWindow = this.getToplevelOwner(w)) != null) {
            this.ownerOrigBounds = this.realFSWindow.getBounds();
            WWindowPeer fp = (WWindowPeer)acc.getPeer(this.realFSWindow);
            this.ownerWasVisible = this.realFSWindow.isVisible();
            Rectangle r = w.getBounds();
            fp.reshape(r.x, r.y, r.width, r.height);
            fp.setVisible(true);
        } else {
            this.realFSWindow = w;
        }
        this.fsWindowWasAlwaysOnTop = this.realFSWindow.isAlwaysOnTop();
        ((WWindowPeer)acc.getPeer(this.realFSWindow)).setAlwaysOnTop(true);
        this.fsWindowListener = new D3DFSWindowAdapter();
        this.realFSWindow.addWindowListener(this.fsWindowListener);
    }

    @Override
    protected void removeFSWindowListener(Window w) {
        this.realFSWindow.removeWindowListener(this.fsWindowListener);
        this.fsWindowListener = null;
        WWindowPeer wpeer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(this.realFSWindow);
        if (wpeer != null) {
            if (this.ownerOrigBounds != null) {
                if (this.ownerOrigBounds.width == 0) {
                    this.ownerOrigBounds.width = 1;
                }
                if (this.ownerOrigBounds.height == 0) {
                    this.ownerOrigBounds.height = 1;
                }
                wpeer.reshape(this.ownerOrigBounds.x, this.ownerOrigBounds.y, this.ownerOrigBounds.width, this.ownerOrigBounds.height);
                if (!this.ownerWasVisible) {
                    wpeer.setVisible(false);
                }
                this.ownerOrigBounds = null;
            }
            if (!this.fsWindowWasAlwaysOnTop) {
                wpeer.setAlwaysOnTop(false);
            }
        }
        this.realFSWindow = null;
    }

    private static native DisplayMode getCurrentDisplayModeNative(int var0);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected DisplayMode getCurrentDisplayMode(final int screen) {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            class Result {
                DisplayMode dm = null;

                Result(D3DGraphicsDevice this$0) {
                }
            }
            final Result res = new Result(this);
            rq.flushAndInvokeNow(new Runnable(){
                {
                }

                @Override
                public void run() {
                    res.dm = D3DGraphicsDevice.getCurrentDisplayModeNative(screen);
                }
            });
            if (res.dm == null) {
                DisplayMode displayMode = super.getCurrentDisplayMode(screen);
                return displayMode;
            }
            DisplayMode displayMode = res.dm;
            return displayMode;
        }
        finally {
            rq.unlock();
        }
    }

    private static native void configDisplayModeNative(int var0, long var1, int var3, int var4, int var5, int var6);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void configDisplayMode(final int screen, WindowPeer w, final int width, final int height, final int bitDepth, final int refreshRate) {
        if (!this.fsStatus) {
            super.configDisplayMode(screen, w, width, height, bitDepth, refreshRate);
            return;
        }
        final WWindowPeer wpeer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(this.realFSWindow);
        if (this.getFullScreenWindow() != this.realFSWindow) {
            Rectangle screenBounds = this.getDefaultConfiguration().getBounds();
            wpeer.reshape(screenBounds.x, screenBounds.y, width, height);
        }
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.flushAndInvokeNow(new Runnable(){

                @Override
                public void run() {
                    long hwnd = wpeer.getHWnd();
                    if (hwnd == 0L) {
                        return;
                    }
                    D3DGraphicsDevice.configDisplayModeNative(screen, hwnd, width, height, bitDepth, refreshRate);
                }
            });
        }
        finally {
            rq.unlock();
        }
    }

    private static native void enumDisplayModesNative(int var0, ArrayList<DisplayMode> var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void enumDisplayModes(final int screen, final ArrayList<DisplayMode> modes) {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.flushAndInvokeNow(new Runnable(){

                @Override
                public void run() {
                    D3DGraphicsDevice.enumDisplayModesNative(screen, modes);
                }
            });
            if (modes.size() == 0) {
                modes.add(D3DGraphicsDevice.getCurrentDisplayModeNative(screen));
            }
        }
        finally {
            rq.unlock();
        }
    }

    private static native long getAvailableAcceleratedMemoryNative(int var0);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getAvailableAcceleratedMemory() {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            class Result {
                long mem = 0L;

                Result(D3DGraphicsDevice this$0) {
                }
            }
            final Result res = new Result(this);
            rq.flushAndInvokeNow(new Runnable(){
                final /* synthetic */ D3DGraphicsDevice this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public void run() {
                    res.mem = D3DGraphicsDevice.getAvailableAcceleratedMemoryNative(this.this$0.getScreen());
                }
            });
            int n = (int)res.mem;
            return n;
        }
        finally {
            rq.unlock();
        }
    }

    @Override
    public GraphicsConfiguration[] getConfigurations() {
        if (this.configs == null && this.isD3DEnabledOnDevice()) {
            this.defaultConfig = this.getDefaultConfiguration();
            if (this.defaultConfig != null) {
                this.configs = new GraphicsConfiguration[1];
                this.configs[0] = this.defaultConfig;
                return (GraphicsConfiguration[])this.configs.clone();
            }
        }
        return super.getConfigurations();
    }

    @Override
    public GraphicsConfiguration getDefaultConfiguration() {
        if (this.defaultConfig == null) {
            this.defaultConfig = this.isD3DEnabledOnDevice() ? new D3DGraphicsConfig(this) : super.getDefaultConfiguration();
        }
        return this.defaultConfig;
    }

    private static native boolean isD3DAvailableOnDeviceNative(int var0);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isD3DAvailableOnDevice(final int screen) {
        if (!d3dAvailable) {
            return false;
        }
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            class Result {
                boolean avail = false;

                Result() {
                }
            }
            final Result res = new Result();
            rq.flushAndInvokeNow(new Runnable(){
                {
                }

                @Override
                public void run() {
                    res.avail = D3DGraphicsDevice.isD3DAvailableOnDeviceNative(screen);
                }
            });
            boolean bl = res.avail;
            return bl;
        }
        finally {
            rq.unlock();
        }
    }

    D3DContext getContext() {
        return this.context;
    }

    ContextCapabilities getContextCapabilities() {
        return this.d3dCaps;
    }

    @Override
    public void displayChanged() {
        super.displayChanged();
        if (d3dAvailable) {
            this.d3dCaps = D3DGraphicsDevice.getDeviceCaps(this.getScreen());
        }
    }

    @Override
    protected void invalidate(int defaultScreen) {
        super.invalidate(defaultScreen);
        this.d3dCaps = new D3DContext.D3DContextCaps(0, null);
    }

    static {
        Toolkit.getDefaultToolkit();
        d3dAvailable = D3DGraphicsDevice.initD3D();
        if (d3dAvailable) {
            pfDisabled = true;
        }
    }

    private static class D3DFSWindowAdapter
    extends WindowAdapter {
        private D3DFSWindowAdapter() {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            D3DRenderQueue.getInstance();
            D3DRenderQueue.restoreDevices();
        }

        @Override
        public void windowActivated(WindowEvent e) {
            D3DRenderQueue.getInstance();
            D3DRenderQueue.restoreDevices();
        }
    }
}

