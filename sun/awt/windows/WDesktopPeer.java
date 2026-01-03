/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.desktop.SystemEventListener;
import java.awt.desktop.SystemSleepEvent;
import java.awt.desktop.SystemSleepListener;
import java.awt.desktop.UserSessionEvent;
import java.awt.desktop.UserSessionListener;
import java.awt.peer.DesktopPeer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.swing.event.EventListenerList;

final class WDesktopPeer
implements DesktopPeer {
    private static String ACTION_OPEN_VERB = "open";
    private static String ACTION_EDIT_VERB = "edit";
    private static String ACTION_PRINT_VERB = "print";
    private static final EventListenerList listenerList = new EventListenerList();

    private static native void init();

    WDesktopPeer() {
        WDesktopPeer.init();
    }

    @Override
    public boolean isSupported(Desktop.Action action) {
        switch (action) {
            case OPEN: 
            case EDIT: 
            case PRINT: 
            case MAIL: 
            case BROWSE: 
            case MOVE_TO_TRASH: 
            case APP_SUDDEN_TERMINATION: 
            case APP_EVENT_SYSTEM_SLEEP: 
            case APP_EVENT_USER_SESSION: {
                return true;
            }
        }
        return false;
    }

    @Override
    public void open(File file) throws IOException {
        this.ShellExecute(file, ACTION_OPEN_VERB);
    }

    @Override
    public void edit(File file) throws IOException {
        this.ShellExecute(file, ACTION_EDIT_VERB);
    }

    @Override
    public void print(File file) throws IOException {
        this.ShellExecute(file, ACTION_PRINT_VERB);
    }

    @Override
    public void mail(URI uri) throws IOException {
        this.ShellExecute(uri, ACTION_OPEN_VERB);
    }

    @Override
    public void browse(URI uri) throws IOException {
        this.ShellExecute(uri, ACTION_OPEN_VERB);
    }

    private void ShellExecute(File file, String verb) throws IOException {
        String errMsg = WDesktopPeer.ShellExecute(file.getAbsolutePath(), verb);
        if (errMsg != null) {
            throw new IOException("Failed to " + verb + " " + String.valueOf(file) + ". Error message: " + errMsg);
        }
    }

    private void ShellExecute(URI uri, String verb) throws IOException {
        String errmsg = WDesktopPeer.ShellExecute(uri.toString(), verb);
        if (errmsg != null) {
            throw new IOException("Failed to " + verb + " " + String.valueOf(uri) + ". Error message: " + errmsg);
        }
    }

    private static native String ShellExecute(String var0, String var1);

    @Override
    public void disableSuddenTermination() {
        WDesktopPeer.setSuddenTerminationEnabled(false);
    }

    @Override
    public void enableSuddenTermination() {
        WDesktopPeer.setSuddenTerminationEnabled(true);
    }

    private static native void setSuddenTerminationEnabled(boolean var0);

    @Override
    public void addAppEventListener(SystemEventListener listener) {
        if (listener instanceof UserSessionListener) {
            listenerList.add(UserSessionListener.class, (UserSessionListener)listener);
        }
        if (listener instanceof SystemSleepListener) {
            listenerList.add(SystemSleepListener.class, (SystemSleepListener)listener);
        }
    }

    @Override
    public void removeAppEventListener(SystemEventListener listener) {
        if (listener instanceof UserSessionListener) {
            listenerList.remove(UserSessionListener.class, (UserSessionListener)listener);
        }
        if (listener instanceof SystemSleepListener) {
            listenerList.remove(SystemSleepListener.class, (SystemSleepListener)listener);
        }
    }

    private static void userSessionCallback(boolean activated, UserSessionEvent.Reason reason) {
        UserSessionListener[] listeners;
        for (UserSessionListener use : listeners = (UserSessionListener[])listenerList.getListeners(UserSessionListener.class)) {
            EventQueue.invokeLater(() -> {
                if (activated) {
                    use.userSessionActivated(new UserSessionEvent(reason));
                } else {
                    use.userSessionDeactivated(new UserSessionEvent(reason));
                }
            });
        }
    }

    private static void systemSleepCallback(boolean resumed) {
        SystemSleepListener[] listeners;
        for (SystemSleepListener ssl : listeners = (SystemSleepListener[])listenerList.getListeners(SystemSleepListener.class)) {
            EventQueue.invokeLater(() -> {
                if (resumed) {
                    ssl.systemAwoke(new SystemSleepEvent());
                } else {
                    ssl.systemAboutToSleep(new SystemSleepEvent());
                }
            });
        }
    }

    @Override
    public boolean moveToTrash(File file) {
        return WDesktopPeer.moveToTrash(file.getAbsolutePath());
    }

    private static native boolean moveToTrash(String var0);
}

