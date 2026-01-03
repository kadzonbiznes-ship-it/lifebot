/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.IllegalComponentStateException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import sun.awt.DisplayChangedListener;
import sun.util.logging.PlatformLogger;

public class SunDisplayChanger {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.multiscreen.SunDisplayChanger");
    private Map<DisplayChangedListener, Void> listeners = Collections.synchronizedMap(new WeakHashMap(1));

    public void add(DisplayChangedListener theListener) {
        if (log.isLoggable(PlatformLogger.Level.FINE) && theListener == null) {
            log.fine("Assertion (theListener != null) failed");
        }
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("Adding listener: " + String.valueOf(theListener));
        }
        this.listeners.put(theListener, null);
    }

    public void remove(DisplayChangedListener theListener) {
        if (log.isLoggable(PlatformLogger.Level.FINE) && theListener == null) {
            log.fine("Assertion (theListener != null) failed");
        }
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("Removing listener: " + String.valueOf(theListener));
        }
        this.listeners.remove(theListener);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyListeners() {
        HashSet<DisplayChangedListener> cloneSet;
        if (log.isLoggable(PlatformLogger.Level.FINEST)) {
            log.finest("notifyListeners");
        }
        Map<DisplayChangedListener, Void> map = this.listeners;
        synchronized (map) {
            cloneSet = new HashSet<DisplayChangedListener>(this.listeners.keySet());
        }
        for (DisplayChangedListener current : cloneSet) {
            try {
                if (log.isLoggable(PlatformLogger.Level.FINEST)) {
                    log.finest("displayChanged for listener: " + String.valueOf(current));
                }
                current.displayChanged();
            }
            catch (IllegalComponentStateException e) {
                this.listeners.remove(current);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyPaletteChanged() {
        HashSet<DisplayChangedListener> cloneSet;
        if (log.isLoggable(PlatformLogger.Level.FINEST)) {
            log.finest("notifyPaletteChanged");
        }
        Map<DisplayChangedListener, Void> map = this.listeners;
        synchronized (map) {
            cloneSet = new HashSet<DisplayChangedListener>(this.listeners.keySet());
        }
        for (DisplayChangedListener current : cloneSet) {
            try {
                if (log.isLoggable(PlatformLogger.Level.FINEST)) {
                    log.finest("paletteChanged for listener: " + String.valueOf(current));
                }
                current.paletteChanged();
            }
            catch (IllegalComponentStateException e) {
                this.listeners.remove(current);
            }
        }
    }
}

