/*
 * Decompiled with CFR 0.152.
 */
package java.awt.dnd;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.SerializationTester;
import java.awt.dnd.peer.DropTargetPeer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TooManyListenersException;
import javax.swing.Timer;
import sun.awt.AWTAccessor;

public class DropTarget
implements DropTargetListener,
Serializable {
    private static final long serialVersionUID = -6283860791671019047L;
    private DropTargetContext dropTargetContext = this.createDropTargetContext();
    private Component component;
    private transient ComponentPeer componentPeer;
    private transient DropTargetPeer nativePeer;
    int actions = 3;
    boolean active = true;
    private transient DropTargetAutoScroller autoScroller;
    private transient DropTargetListener dtListener;
    private transient FlavorMap flavorMap;
    private transient boolean isDraggingInside;

    public DropTarget(Component c, int ops, DropTargetListener dtl, boolean act, FlavorMap fm) throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        this.component = c;
        this.setDefaultActions(ops);
        if (dtl != null) {
            try {
                this.addDropTargetListener(dtl);
            }
            catch (TooManyListenersException tooManyListenersException) {
                // empty catch block
            }
        }
        if (c != null) {
            c.setDropTarget(this);
            this.setActive(act);
        }
        this.flavorMap = fm != null ? fm : SystemFlavorMap.getDefaultFlavorMap();
    }

    public DropTarget(Component c, int ops, DropTargetListener dtl, boolean act) throws HeadlessException {
        this(c, ops, dtl, act, null);
    }

    public DropTarget() throws HeadlessException {
        this(null, 3, null, true, null);
    }

    public DropTarget(Component c, DropTargetListener dtl) throws HeadlessException {
        this(c, 3, dtl, true, null);
    }

    public DropTarget(Component c, int ops, DropTargetListener dtl) throws HeadlessException {
        this(c, ops, dtl, true);
    }

    public synchronized void setComponent(Component c) {
        block5: {
            if (this.component == c || this.component != null && this.component.equals(c)) {
                return;
            }
            Component old = this.component;
            if (old != null) {
                this.clearAutoscroll();
                this.component = null;
                this.removeNotify();
                old.setDropTarget(null);
            }
            if ((this.component = c) != null) {
                try {
                    c.setDropTarget(this);
                }
                catch (Exception e) {
                    if (old == null) break block5;
                    old.setDropTarget(this);
                    this.addNotify();
                }
            }
        }
    }

    public synchronized Component getComponent() {
        return this.component;
    }

    public void setDefaultActions(int ops) {
        this.getDropTargetContext().setTargetActions(ops & 0x40000003);
    }

    void doSetDefaultActions(int ops) {
        this.actions = ops;
    }

    public int getDefaultActions() {
        return this.actions;
    }

    public synchronized void setActive(boolean isActive) {
        if (isActive != this.active) {
            this.active = isActive;
        }
        if (!this.active) {
            this.clearAutoscroll();
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public synchronized void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException {
        if (dtl == null) {
            return;
        }
        if (this.equals(dtl)) {
            throw new IllegalArgumentException("DropTarget may not be its own Listener");
        }
        if (this.dtListener != null) {
            throw new TooManyListenersException();
        }
        this.dtListener = dtl;
    }

    public synchronized void removeDropTargetListener(DropTargetListener dtl) {
        if (dtl != null && this.dtListener != null) {
            if (this.dtListener.equals(dtl)) {
                this.dtListener = null;
            } else {
                throw new IllegalArgumentException("listener mismatch");
            }
        }
    }

    @Override
    public synchronized void dragEnter(DropTargetDragEvent dtde) {
        this.isDraggingInside = true;
        if (!this.active) {
            return;
        }
        if (this.dtListener != null) {
            this.dtListener.dragEnter(dtde);
        } else {
            dtde.getDropTargetContext().setTargetActions(0);
        }
        this.initializeAutoscrolling(dtde.getLocation());
    }

    @Override
    public synchronized void dragOver(DropTargetDragEvent dtde) {
        if (!this.active) {
            return;
        }
        if (this.dtListener != null && this.active) {
            this.dtListener.dragOver(dtde);
        }
        this.updateAutoscroll(dtde.getLocation());
    }

    @Override
    public synchronized void dropActionChanged(DropTargetDragEvent dtde) {
        if (!this.active) {
            return;
        }
        if (this.dtListener != null) {
            this.dtListener.dropActionChanged(dtde);
        }
        this.updateAutoscroll(dtde.getLocation());
    }

    @Override
    public synchronized void dragExit(DropTargetEvent dte) {
        this.isDraggingInside = false;
        if (!this.active) {
            return;
        }
        if (this.dtListener != null && this.active) {
            this.dtListener.dragExit(dte);
        }
        this.clearAutoscroll();
    }

    @Override
    public synchronized void drop(DropTargetDropEvent dtde) {
        this.isDraggingInside = false;
        this.clearAutoscroll();
        if (this.dtListener != null && this.active) {
            this.dtListener.drop(dtde);
        } else {
            dtde.rejectDrop();
        }
    }

    public FlavorMap getFlavorMap() {
        return this.flavorMap;
    }

    public void setFlavorMap(FlavorMap fm) {
        this.flavorMap = fm == null ? SystemFlavorMap.getDefaultFlavorMap() : fm;
    }

    public void addNotify() {
        AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
        Object peer = acc.getPeer(this.component);
        if (peer == null || peer == this.componentPeer) {
            return;
        }
        this.componentPeer = peer;
        for (Component c = this.component; c != null && peer instanceof LightweightPeer; c = c.getParent()) {
            peer = acc.getPeer(c);
        }
        if (peer instanceof DropTargetPeer) {
            this.nativePeer = (DropTargetPeer)peer;
            ((DropTargetPeer)peer).addDropTarget(this);
        } else {
            this.nativePeer = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeNotify() {
        if (this.nativePeer != null) {
            this.nativePeer.removeDropTarget(this);
        }
        this.componentPeer = null;
        this.nativePeer = null;
        DropTarget dropTarget = this;
        synchronized (dropTarget) {
            if (this.isDraggingInside) {
                this.dragExit(new DropTargetEvent(this.getDropTargetContext()));
            }
        }
    }

    public DropTargetContext getDropTargetContext() {
        return this.dropTargetContext;
    }

    protected DropTargetContext createDropTargetContext() {
        return new DropTargetContext(this);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(SerializationTester.test(this.dtListener) ? this.dtListener : null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField f = s.readFields();
        try {
            this.dropTargetContext = (DropTargetContext)f.get("dropTargetContext", null);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        if (this.dropTargetContext == null) {
            this.dropTargetContext = this.createDropTargetContext();
        }
        this.component = (Component)f.get("component", null);
        this.actions = f.get("actions", 3);
        this.active = f.get("active", true);
        try {
            this.dtListener = (DropTargetListener)f.get("dtListener", null);
        }
        catch (IllegalArgumentException e) {
            this.dtListener = (DropTargetListener)s.readObject();
        }
    }

    protected DropTargetAutoScroller createDropTargetAutoScroller(Component c, Point p) {
        return new DropTargetAutoScroller(c, p);
    }

    protected void initializeAutoscrolling(Point p) {
        if (!(this.component instanceof Autoscroll)) {
            return;
        }
        this.autoScroller = this.createDropTargetAutoScroller(this.component, p);
    }

    protected void updateAutoscroll(Point dragCursorLocn) {
        if (this.autoScroller != null) {
            this.autoScroller.updateLocation(dragCursorLocn);
        }
    }

    protected void clearAutoscroll() {
        if (this.autoScroller != null) {
            this.autoScroller.stop();
            this.autoScroller = null;
        }
    }

    protected static class DropTargetAutoScroller
    implements ActionListener {
        private Component component;
        private Autoscroll autoScroll;
        private Timer timer;
        private Point locn;
        private Point prev;
        private Rectangle outer = new Rectangle();
        private Rectangle inner = new Rectangle();
        private int hysteresis = 10;

        protected DropTargetAutoScroller(Component c, Point p) {
            this.component = c;
            this.autoScroll = (Autoscroll)((Object)this.component);
            Toolkit t = Toolkit.getDefaultToolkit();
            Integer initial = 100;
            Integer interval = 100;
            try {
                initial = (Integer)t.getDesktopProperty("DnD.Autoscroll.initialDelay");
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                interval = (Integer)t.getDesktopProperty("DnD.Autoscroll.interval");
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.timer = new Timer(interval, this);
            this.timer.setCoalesce(true);
            this.timer.setInitialDelay(initial);
            this.locn = p;
            this.prev = p;
            try {
                this.hysteresis = (Integer)t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis");
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.timer.start();
        }

        private void updateRegion() {
            Insets i = this.autoScroll.getAutoscrollInsets();
            Dimension size = this.component.getSize();
            if (size.width != this.outer.width || size.height != this.outer.height) {
                this.outer.reshape(0, 0, size.width, size.height);
            }
            if (this.inner.x != i.left || this.inner.y != i.top) {
                this.inner.setLocation(i.left, i.top);
            }
            int newWidth = size.width - (i.left + i.right);
            int newHeight = size.height - (i.top + i.bottom);
            if (newWidth != this.inner.width || newHeight != this.inner.height) {
                this.inner.setSize(newWidth, newHeight);
            }
        }

        protected synchronized void updateLocation(Point newLocn) {
            this.prev = this.locn;
            this.locn = newLocn;
            if (Math.abs(this.locn.x - this.prev.x) > this.hysteresis || Math.abs(this.locn.y - this.prev.y) > this.hysteresis) {
                if (this.timer.isRunning()) {
                    this.timer.stop();
                }
            } else if (!this.timer.isRunning()) {
                this.timer.start();
            }
        }

        protected void stop() {
            this.timer.stop();
        }

        @Override
        public synchronized void actionPerformed(ActionEvent e) {
            this.updateRegion();
            if (this.outer.contains(this.locn) && !this.inner.contains(this.locn)) {
                this.autoScroll.autoscroll(this.locn);
            }
        }
    }
}

