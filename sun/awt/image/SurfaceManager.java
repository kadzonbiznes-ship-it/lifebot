/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import sun.awt.image.BufImgSurfaceManager;
import sun.java2d.InvalidPipeException;
import sun.java2d.SurfaceData;
import sun.java2d.SurfaceDataProxy;

public abstract class SurfaceManager {
    private static ImageAccessor imgaccessor;
    private volatile ConcurrentHashMap<Object, Object> cacheMap;

    public static void setImageAccessor(ImageAccessor ia) {
        if (imgaccessor != null) {
            throw new InternalError("Attempt to set ImageAccessor twice");
        }
        imgaccessor = ia;
    }

    public static SurfaceManager getManager(Image img) {
        SurfaceManager sMgr = imgaccessor.getSurfaceManager(img);
        if (sMgr == null) {
            try {
                BufferedImage bi = (BufferedImage)img;
                sMgr = new BufImgSurfaceManager(bi);
                SurfaceManager.setManager(bi, sMgr);
            }
            catch (ClassCastException e) {
                throw new InvalidPipeException("Invalid Image variant");
            }
        }
        return sMgr;
    }

    public static void setManager(Image img, SurfaceManager mgr) {
        imgaccessor.setSurfaceManager(img, mgr);
    }

    public Object getCacheData(Object key) {
        return this.cacheMap == null ? null : this.cacheMap.get(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setCacheData(Object key, Object value) {
        if (this.cacheMap == null) {
            SurfaceManager surfaceManager = this;
            synchronized (surfaceManager) {
                if (this.cacheMap == null) {
                    this.cacheMap = new ConcurrentHashMap(2);
                }
            }
        }
        this.cacheMap.put(key, value);
    }

    public abstract SurfaceData getPrimarySurfaceData();

    public abstract SurfaceData restoreContents();

    public void acceleratedSurfaceLost() {
    }

    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        return new ImageCapabilitiesGc(gc);
    }

    public synchronized void flush() {
        this.flush(false);
    }

    synchronized void flush(boolean deaccelerate) {
        if (this.cacheMap != null) {
            Iterator<Object> i = this.cacheMap.values().iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof FlushableCacheData) || !((FlushableCacheData)o).flush(deaccelerate)) continue;
                i.remove();
            }
        }
    }

    public void setAccelerationPriority(float priority) {
        if (priority == 0.0f) {
            this.flush(true);
        }
    }

    public static double getImageScaleX(Image img) {
        if (!(img instanceof VolatileImage)) {
            return 1.0;
        }
        SurfaceManager sm = SurfaceManager.getManager(img);
        return sm.getPrimarySurfaceData().getDefaultScaleX();
    }

    public static double getImageScaleY(Image img) {
        if (!(img instanceof VolatileImage)) {
            return 1.0;
        }
        SurfaceManager sm = SurfaceManager.getManager(img);
        return sm.getPrimarySurfaceData().getDefaultScaleY();
    }

    public static abstract class ImageAccessor {
        public abstract SurfaceManager getSurfaceManager(Image var1);

        public abstract void setSurfaceManager(Image var1, SurfaceManager var2);
    }

    class ImageCapabilitiesGc
    extends ImageCapabilities {
        GraphicsConfiguration gc;

        public ImageCapabilitiesGc(GraphicsConfiguration gc) {
            super(false);
            this.gc = gc;
        }

        @Override
        public boolean isAccelerated() {
            Object proxyKey;
            GraphicsConfiguration tmpGc = this.gc;
            if (tmpGc == null) {
                tmpGc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            }
            if (tmpGc instanceof ProxiedGraphicsConfig && (proxyKey = ((ProxiedGraphicsConfig)((Object)tmpGc)).getProxyKey()) != null) {
                SurfaceDataProxy sdp = (SurfaceDataProxy)SurfaceManager.this.getCacheData(proxyKey);
                return sdp != null && sdp.isAccelerated();
            }
            return false;
        }
    }

    public static interface FlushableCacheData {
        public boolean flush(boolean var1);
    }

    public static interface ProxiedGraphicsConfig {
        public Object getProxyKey();
    }
}

