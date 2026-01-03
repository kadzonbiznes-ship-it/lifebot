/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import sun.awt.Win32GraphicsConfig;
import sun.awt.windows.WComponentPeer;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DScreenUpdateManager;
import sun.java2d.windows.WindowsFlags;

public class ScreenUpdateManager {
    private static ScreenUpdateManager theInstance;

    protected ScreenUpdateManager() {
    }

    public synchronized Graphics2D createGraphics(SurfaceData sd, WComponentPeer peer, Color fgColor, Color bgColor, Font font) {
        return new SunGraphics2D(sd, fgColor, bgColor, font);
    }

    public SurfaceData createScreenSurface(Win32GraphicsConfig gc, WComponentPeer peer, int bbNum, boolean isResize) {
        return gc.createSurfaceData(peer, bbNum);
    }

    public void dropScreenSurface(SurfaceData sd) {
    }

    public SurfaceData getReplacementScreenSurface(WComponentPeer peer, SurfaceData oldsd) {
        SurfaceData surfaceData = peer.getSurfaceData();
        if (surfaceData == null || surfaceData.isValid()) {
            return surfaceData;
        }
        peer.replaceSurfaceData();
        return peer.getSurfaceData();
    }

    public static synchronized ScreenUpdateManager getInstance() {
        if (theInstance == null) {
            theInstance = WindowsFlags.isD3DEnabled() ? new D3DScreenUpdateManager() : new ScreenUpdateManager();
        }
        return theInstance;
    }
}

