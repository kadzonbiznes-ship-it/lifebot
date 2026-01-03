/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.peer.TrayIconPeer;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.EventListener;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.HeadlessToolkit;
import sun.awt.SunToolkit;

public class TrayIcon {
    private Image image;
    private String tooltip;
    private PopupMenu popup;
    private boolean autosize;
    private int id;
    private String actionCommand;
    private transient TrayIconPeer peer;
    transient MouseListener mouseListener;
    transient MouseMotionListener mouseMotionListener;
    transient ActionListener actionListener;
    private final AccessControlContext acc = AccessController.getContext();

    final AccessControlContext getAccessControlContext() {
        if (this.acc == null) {
            throw new SecurityException("TrayIcon is missing AccessControlContext");
        }
        return this.acc;
    }

    private TrayIcon() throws UnsupportedOperationException, HeadlessException, SecurityException {
        SystemTray.checkSystemTrayAllowed();
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException();
        }
        SunToolkit.insertTargetMapping(this, AppContext.getAppContext());
    }

    public TrayIcon(Image image) {
        this();
        if (image == null) {
            throw new IllegalArgumentException("creating TrayIcon with null Image");
        }
        this.setImage(image);
    }

    public TrayIcon(Image image, String tooltip) {
        this(image);
        this.setToolTip(tooltip);
    }

    public TrayIcon(Image image, String tooltip, PopupMenu popup) {
        this(image, tooltip);
        this.setPopupMenu(popup);
    }

    public void setImage(Image image) {
        if (image == null) {
            throw new NullPointerException("setting null Image");
        }
        this.image = image;
        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.updateImage();
        }
    }

    public Image getImage() {
        return this.image;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPopupMenu(PopupMenu popup) {
        if (popup == this.popup) {
            return;
        }
        Class<TrayIcon> clazz = TrayIcon.class;
        synchronized (TrayIcon.class) {
            if (popup != null) {
                if (popup.isTrayIconPopup) {
                    throw new IllegalArgumentException("the PopupMenu is already set for another TrayIcon");
                }
                popup.isTrayIconPopup = true;
            }
            if (this.popup != null) {
                this.popup.isTrayIconPopup = false;
            }
            this.popup = popup;
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    public PopupMenu getPopupMenu() {
        return this.popup;
    }

    public void setToolTip(String tooltip) {
        this.tooltip = tooltip;
        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.setToolTip(tooltip);
        }
    }

    public String getToolTip() {
        return this.tooltip;
    }

    public void setImageAutoSize(boolean autosize) {
        this.autosize = autosize;
        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.updateImage();
        }
    }

    public boolean isImageAutoSize() {
        return this.autosize;
    }

    public synchronized void addMouseListener(MouseListener listener) {
        if (listener == null) {
            return;
        }
        this.mouseListener = AWTEventMulticaster.add(this.mouseListener, listener);
    }

    public synchronized void removeMouseListener(MouseListener listener) {
        if (listener == null) {
            return;
        }
        this.mouseListener = AWTEventMulticaster.remove(this.mouseListener, listener);
    }

    public synchronized MouseListener[] getMouseListeners() {
        return (MouseListener[])AWTEventMulticaster.getListeners((EventListener)this.mouseListener, MouseListener.class);
    }

    public synchronized void addMouseMotionListener(MouseMotionListener listener) {
        if (listener == null) {
            return;
        }
        this.mouseMotionListener = AWTEventMulticaster.add(this.mouseMotionListener, listener);
    }

    public synchronized void removeMouseMotionListener(MouseMotionListener listener) {
        if (listener == null) {
            return;
        }
        this.mouseMotionListener = AWTEventMulticaster.remove(this.mouseMotionListener, listener);
    }

    public synchronized MouseMotionListener[] getMouseMotionListeners() {
        return (MouseMotionListener[])AWTEventMulticaster.getListeners((EventListener)this.mouseMotionListener, MouseMotionListener.class);
    }

    public String getActionCommand() {
        return this.actionCommand;
    }

    public void setActionCommand(String command) {
        this.actionCommand = command;
    }

    public synchronized void addActionListener(ActionListener listener) {
        if (listener == null) {
            return;
        }
        this.actionListener = AWTEventMulticaster.add(this.actionListener, listener);
    }

    public synchronized void removeActionListener(ActionListener listener) {
        if (listener == null) {
            return;
        }
        this.actionListener = AWTEventMulticaster.remove(this.actionListener, listener);
    }

    public synchronized ActionListener[] getActionListeners() {
        return (ActionListener[])AWTEventMulticaster.getListeners((EventListener)this.actionListener, ActionListener.class);
    }

    public void displayMessage(String caption, String text, MessageType messageType) {
        if (caption == null && text == null) {
            throw new NullPointerException("displaying the message with both caption and text being null");
        }
        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.displayMessage(caption, text, messageType.name());
        }
    }

    public Dimension getSize() {
        return SystemTray.getSystemTray().getTrayIconSize();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addNotify() throws AWTException {
        TrayIcon trayIcon = this;
        synchronized (trayIcon) {
            if (this.peer == null) {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                if (toolkit instanceof SunToolkit) {
                    this.peer = ((SunToolkit)Toolkit.getDefaultToolkit()).createTrayIcon(this);
                } else if (toolkit instanceof HeadlessToolkit) {
                    this.peer = ((HeadlessToolkit)Toolkit.getDefaultToolkit()).createTrayIcon(this);
                }
            }
        }
        this.peer.setToolTip(this.tooltip);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void removeNotify() {
        TrayIconPeer p = null;
        TrayIcon trayIcon = this;
        synchronized (trayIcon) {
            p = this.peer;
            this.peer = null;
            if (this.popup != null) {
                this.popup.removeNotify();
            }
        }
        if (p != null) {
            p.dispose();
        }
    }

    void setID(int id) {
        this.id = id;
    }

    int getID() {
        return this.id;
    }

    void dispatchEvent(AWTEvent e) {
        EventQueue.setCurrentEventAndMostRecentTime(e);
        Toolkit.getDefaultToolkit().notifyAWTEventListeners(e);
        this.processEvent(e);
    }

    void processEvent(AWTEvent e) {
        block6: {
            block5: {
                if (!(e instanceof MouseEvent)) break block5;
                switch (e.getID()) {
                    case 500: 
                    case 501: 
                    case 502: {
                        this.processMouseEvent((MouseEvent)e);
                        break block6;
                    }
                    case 503: {
                        this.processMouseMotionEvent((MouseEvent)e);
                        break block6;
                    }
                    default: {
                        return;
                    }
                }
            }
            if (e instanceof ActionEvent) {
                this.processActionEvent((ActionEvent)e);
            }
        }
    }

    void processMouseEvent(MouseEvent e) {
        MouseListener listener = this.mouseListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 501: {
                    listener.mousePressed(e);
                    break;
                }
                case 502: {
                    listener.mouseReleased(e);
                    break;
                }
                case 500: {
                    listener.mouseClicked(e);
                    break;
                }
                default: {
                    return;
                }
            }
        }
    }

    void processMouseMotionEvent(MouseEvent e) {
        MouseMotionListener listener = this.mouseMotionListener;
        if (listener != null && e.getID() == 503) {
            listener.mouseMoved(e);
        }
    }

    void processActionEvent(ActionEvent e) {
        ActionListener listener = this.actionListener;
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }

    private static native void initIDs();

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            TrayIcon.initIDs();
        }
        AWTAccessor.setTrayIconAccessor(new AWTAccessor.TrayIconAccessor(){

            @Override
            public void addNotify(TrayIcon trayIcon) throws AWTException {
                trayIcon.addNotify();
            }

            @Override
            public void removeNotify(TrayIcon trayIcon) {
                trayIcon.removeNotify();
            }
        });
    }

    public static enum MessageType {
        ERROR,
        WARNING,
        INFO,
        NONE;

    }
}

