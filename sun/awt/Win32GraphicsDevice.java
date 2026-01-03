/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTPermission;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ColorModel;
import java.awt.peer.WindowPeer;
import java.security.AccessController;
import java.util.ArrayList;
import sun.awt.AWTAccessor;
import sun.awt.DisplayChangedListener;
import sun.awt.SunDisplayChanger;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.windows.WWindowPeer;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.opengl.WGLGraphicsConfig;
import sun.java2d.windows.WindowsFlags;
import sun.security.action.GetPropertyAction;

public class Win32GraphicsDevice
extends GraphicsDevice
implements DisplayChangedListener {
    int screen;
    ColorModel dynamicColorModel;
    ColorModel colorModel;
    protected GraphicsConfiguration[] configs;
    protected GraphicsConfiguration defaultConfig;
    private final String idString;
    protected String descString;
    private boolean valid;
    private SunDisplayChanger topLevels = new SunDisplayChanger();
    protected static boolean pfDisabled;
    private static AWTPermission fullScreenExclusivePermission;
    private DisplayMode defaultDisplayMode;
    private WindowListener fsWindowListener;
    private float scaleX;
    private float scaleY;

    private static native void initIDs();

    native void initDevice(int var1);

    native void initNativeScale(int var1);

    native void setNativeScale(int var1, float var2, float var3);

    native float getNativeScaleX(int var1);

    native float getNativeScaleY(int var1);

    public Win32GraphicsDevice(int screennum) {
        this.screen = screennum;
        this.idString = "\\Display" + this.screen;
        this.descString = "Win32GraphicsDevice[screen=" + this.screen;
        this.valid = true;
        this.initDevice(screennum);
        this.initScaleFactors();
    }

    @Override
    public int getType() {
        return 0;
    }

    public int getScreen() {
        return this.screen;
    }

    public float getDefaultScaleX() {
        return this.scaleX;
    }

    public float getDefaultScaleY() {
        return this.scaleY;
    }

    private void initScaleFactors() {
        if (SunGraphicsEnvironment.isUIScaleEnabled()) {
            if (Win32GraphicsEnvironment.debugScaleX > 0.0f && Win32GraphicsEnvironment.debugScaleY > 0.0f) {
                this.scaleX = Win32GraphicsEnvironment.debugScaleX;
                this.scaleY = Win32GraphicsEnvironment.debugScaleY;
                this.setNativeScale(this.screen, this.scaleX, this.scaleY);
            } else {
                this.initNativeScale(this.screen);
                this.scaleX = this.getNativeScaleX(this.screen);
                this.scaleY = this.getNativeScaleY(this.screen);
            }
        } else {
            this.scaleX = 1.0f;
            this.scaleY = 1.0f;
        }
    }

    public boolean isValid() {
        return this.valid;
    }

    protected void invalidate(int defaultScreen) {
        this.valid = false;
        this.screen = defaultScreen;
    }

    @Override
    public String getIDstring() {
        return this.idString;
    }

    @Override
    public GraphicsConfiguration[] getConfigurations() {
        if (this.configs == null) {
            if (WindowsFlags.isOGLEnabled() && this.isDefaultDevice()) {
                this.defaultConfig = this.getDefaultConfiguration();
                if (this.defaultConfig != null) {
                    this.configs = new GraphicsConfiguration[1];
                    this.configs[0] = this.defaultConfig;
                    return (GraphicsConfiguration[])this.configs.clone();
                }
            }
            int max = this.getMaxConfigs(this.screen);
            int defaultPixID = this.getDefaultPixID(this.screen);
            ArrayList<GraphicsConfiguration> v = new ArrayList<GraphicsConfiguration>(max);
            if (defaultPixID == 0) {
                this.defaultConfig = Win32GraphicsConfig.getConfig(this, defaultPixID);
                v.add(this.defaultConfig);
            } else {
                for (int i = 1; i <= max; ++i) {
                    if (!this.isPixFmtSupported(i, this.screen)) continue;
                    if (i == defaultPixID) {
                        this.defaultConfig = Win32GraphicsConfig.getConfig(this, i);
                        v.add(this.defaultConfig);
                        continue;
                    }
                    v.add(Win32GraphicsConfig.getConfig(this, i));
                }
            }
            this.configs = v.toArray(new GraphicsConfiguration[0]);
        }
        return (GraphicsConfiguration[])this.configs.clone();
    }

    protected int getMaxConfigs(int screen) {
        if (pfDisabled) {
            return 1;
        }
        return this.getMaxConfigsImpl(screen);
    }

    private native int getMaxConfigsImpl(int var1);

    private native boolean isPixFmtSupported(int var1, int var2);

    protected int getDefaultPixID(int screen) {
        if (pfDisabled) {
            return 0;
        }
        return this.getDefaultPixIDImpl(screen);
    }

    private native int getDefaultPixIDImpl(int var1);

    @Override
    public GraphicsConfiguration getDefaultConfiguration() {
        if (this.defaultConfig == null) {
            if (WindowsFlags.isOGLEnabled() && this.isDefaultDevice()) {
                int defPixID = WGLGraphicsConfig.getDefaultPixFmt(this.screen);
                this.defaultConfig = WGLGraphicsConfig.getConfig(this, defPixID);
                if (WindowsFlags.isOGLVerbose()) {
                    if (this.defaultConfig != null) {
                        System.out.print("OpenGL pipeline enabled");
                    } else {
                        System.out.print("Could not enable OpenGL pipeline");
                    }
                    System.out.println(" for default config on screen " + this.screen);
                }
            }
            if (this.defaultConfig == null) {
                this.defaultConfig = Win32GraphicsConfig.getConfig(this, 0);
            }
        }
        return this.defaultConfig;
    }

    public String toString() {
        return this.valid ? this.descString + "]" : this.descString + ", removed]";
    }

    private boolean isDefaultDevice() {
        return this == GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    private static boolean isFSExclusiveModeAllowed() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (fullScreenExclusivePermission == null) {
                fullScreenExclusivePermission = new AWTPermission("fullScreenExclusive");
            }
            try {
                security.checkPermission(fullScreenExclusivePermission);
            }
            catch (SecurityException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isFullScreenSupported() {
        return Win32GraphicsDevice.isFSExclusiveModeAllowed();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void setFullScreenWindow(Window w) {
        WWindowPeer wWindowPeer;
        WWindowPeer peer;
        Window old = this.getFullScreenWindow();
        if (w == old) {
            return;
        }
        if (!this.isFullScreenSupported()) {
            super.setFullScreenWindow(w);
            return;
        }
        if (old != null) {
            if (this.defaultDisplayMode != null) {
                this.setDisplayMode(this.defaultDisplayMode);
                this.defaultDisplayMode = null;
            }
            if ((peer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(old)) != null) {
                peer.setFullScreenExclusiveModeState(false);
                wWindowPeer = peer;
                synchronized (wWindowPeer) {
                    this.exitFullScreenExclusive(this.screen, peer);
                }
            }
            this.removeFSWindowListener(old);
        }
        super.setFullScreenWindow(w);
        if (w != null) {
            this.defaultDisplayMode = this.getDisplayMode();
            this.addFSWindowListener(w);
            peer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(w);
            if (peer != null) {
                wWindowPeer = peer;
                synchronized (wWindowPeer) {
                    this.enterFullScreenExclusive(this.screen, peer);
                }
                peer.setFullScreenExclusiveModeState(true);
            }
            peer.updateGC();
        }
    }

    protected native void enterFullScreenExclusive(int var1, WindowPeer var2);

    protected native void exitFullScreenExclusive(int var1, WindowPeer var2);

    private static void resizeFSWindow(Window w, Rectangle b) {
        WindowPeer peer;
        if (w != null && (peer = (WindowPeer)AWTAccessor.getComponentAccessor().getPeer(w)) != null) {
            peer.setBounds(b.x, b.y, b.width, b.height, 3);
        }
    }

    @Override
    public boolean isDisplayChangeSupported() {
        return this.isFullScreenSupported() && this.getFullScreenWindow() != null;
    }

    @Override
    public synchronized void setDisplayMode(DisplayMode dm) {
        if (!this.isDisplayChangeSupported()) {
            super.setDisplayMode(dm);
            return;
        }
        if (dm == null || (dm = this.getMatchingDisplayMode(dm)) == null) {
            throw new IllegalArgumentException("Invalid display mode");
        }
        if (this.getDisplayMode().equals(dm)) {
            return;
        }
        Window w = this.getFullScreenWindow();
        if (w == null) {
            throw new IllegalStateException("Must be in fullscreen mode in order to set display mode");
        }
        WWindowPeer peer = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(w);
        this.configDisplayMode(this.screen, peer, dm.getWidth(), dm.getHeight(), dm.getBitDepth(), dm.getRefreshRate());
    }

    protected native DisplayMode getCurrentDisplayMode(int var1);

    protected native void configDisplayMode(int var1, WindowPeer var2, int var3, int var4, int var5, int var6);

    protected native void enumDisplayModes(int var1, ArrayList<DisplayMode> var2);

    @Override
    public synchronized DisplayMode getDisplayMode() {
        DisplayMode res = this.getCurrentDisplayMode(this.screen);
        return res;
    }

    @Override
    public synchronized DisplayMode[] getDisplayModes() {
        ArrayList<DisplayMode> modes = new ArrayList<DisplayMode>();
        this.enumDisplayModes(this.screen, modes);
        int listSize = modes.size();
        DisplayMode[] retArray = new DisplayMode[listSize];
        for (int i = 0; i < listSize; ++i) {
            retArray[i] = modes.get(i);
        }
        return retArray;
    }

    protected synchronized DisplayMode getMatchingDisplayMode(DisplayMode dm) {
        DisplayMode[] modes;
        if (!this.isDisplayChangeSupported()) {
            return null;
        }
        for (DisplayMode mode : modes = this.getDisplayModes()) {
            if (!dm.equals(mode) && (dm.getRefreshRate() != 0 || dm.getWidth() != mode.getWidth() || dm.getHeight() != mode.getHeight() || dm.getBitDepth() != mode.getBitDepth())) continue;
            return mode;
        }
        return null;
    }

    @Override
    public void displayChanged() {
        this.dynamicColorModel = null;
        this.defaultConfig = null;
        this.configs = null;
        this.initScaleFactors();
        Rectangle screenBounds = this.getDefaultConfiguration().getBounds();
        Win32GraphicsDevice.resizeFSWindow(this.getFullScreenWindow(), screenBounds);
        this.topLevels.notifyListeners();
    }

    @Override
    public void paletteChanged() {
    }

    public void addDisplayChangedListener(DisplayChangedListener client) {
        this.topLevels.add(client);
    }

    public void removeDisplayChangedListener(DisplayChangedListener client) {
        this.topLevels.remove(client);
    }

    private native ColorModel makeColorModel(int var1, boolean var2);

    public ColorModel getDynamicColorModel() {
        if (this.dynamicColorModel == null) {
            this.dynamicColorModel = this.makeColorModel(this.screen, true);
        }
        return this.dynamicColorModel;
    }

    public ColorModel getColorModel() {
        if (this.colorModel == null) {
            this.colorModel = this.makeColorModel(this.screen, false);
        }
        return this.colorModel;
    }

    protected void addFSWindowListener(final Window w) {
        this.fsWindowListener = new Win32FSWindowAdapter(this);
        EventQueue.invokeLater(new Runnable(){
            final /* synthetic */ Win32GraphicsDevice this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public void run() {
                w.addWindowListener(this.this$0.fsWindowListener);
            }
        });
    }

    protected void removeFSWindowListener(Window w) {
        w.removeWindowListener(this.fsWindowListener);
        this.fsWindowListener = null;
    }

    static {
        String nopixfmt = AccessController.doPrivileged(new GetPropertyAction("sun.awt.nopixfmt"));
        pfDisabled = nopixfmt != null;
        Win32GraphicsDevice.initIDs();
    }

    private static class Win32FSWindowAdapter
    extends WindowAdapter {
        private Win32GraphicsDevice device;
        private DisplayMode dm;

        Win32FSWindowAdapter(Win32GraphicsDevice device) {
            this.device = device;
        }

        private void setFSWindowsState(Window other, int state) {
            GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (other != null) {
                for (GraphicsDevice gd : gds) {
                    if (other != gd.getFullScreenWindow()) continue;
                    return;
                }
            }
            for (GraphicsDevice gd : gds) {
                Window fsw = gd.getFullScreenWindow();
                if (!(fsw instanceof Frame)) continue;
                ((Frame)fsw).setExtendedState(state);
            }
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            this.setFSWindowsState(e.getOppositeWindow(), 1);
        }

        @Override
        public void windowActivated(WindowEvent e) {
            this.setFSWindowsState(e.getOppositeWindow(), 0);
        }

        @Override
        public void windowIconified(WindowEvent e) {
            DisplayMode ddm = this.device.defaultDisplayMode;
            if (ddm != null) {
                this.dm = this.device.getDisplayMode();
                this.device.setDisplayMode(ddm);
            }
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            if (this.dm != null) {
                this.device.setDisplayMode(this.dm);
                this.dm = null;
            }
        }
    }
}

