/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.peer.FramePeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;

public class Frame
extends Window
implements MenuContainer {
    @Deprecated
    public static final int DEFAULT_CURSOR = 0;
    @Deprecated
    public static final int CROSSHAIR_CURSOR = 1;
    @Deprecated
    public static final int TEXT_CURSOR = 2;
    @Deprecated
    public static final int WAIT_CURSOR = 3;
    @Deprecated
    public static final int SW_RESIZE_CURSOR = 4;
    @Deprecated
    public static final int SE_RESIZE_CURSOR = 5;
    @Deprecated
    public static final int NW_RESIZE_CURSOR = 6;
    @Deprecated
    public static final int NE_RESIZE_CURSOR = 7;
    @Deprecated
    public static final int N_RESIZE_CURSOR = 8;
    @Deprecated
    public static final int S_RESIZE_CURSOR = 9;
    @Deprecated
    public static final int W_RESIZE_CURSOR = 10;
    @Deprecated
    public static final int E_RESIZE_CURSOR = 11;
    @Deprecated
    public static final int HAND_CURSOR = 12;
    @Deprecated
    public static final int MOVE_CURSOR = 13;
    public static final int NORMAL = 0;
    public static final int ICONIFIED = 1;
    public static final int MAXIMIZED_HORIZ = 2;
    public static final int MAXIMIZED_VERT = 4;
    public static final int MAXIMIZED_BOTH = 6;
    Rectangle maximizedBounds;
    String title = "Untitled";
    MenuBar menuBar;
    boolean resizable = true;
    boolean undecorated = false;
    boolean mbManagement = false;
    private int state = 0;
    Vector<Window> ownedWindows;
    private static final String base = "frame";
    private static int nameCounter = 0;
    private static final long serialVersionUID = 2673458971256075116L;
    private int frameSerializedDataVersion = 1;

    public Frame() throws HeadlessException {
        this("");
    }

    public Frame(GraphicsConfiguration gc) {
        this("", gc);
    }

    public Frame(String title) throws HeadlessException {
        this.init(title, null);
    }

    public Frame(String title, GraphicsConfiguration gc) {
        super(gc);
        this.init(title, gc);
    }

    private void init(String title, GraphicsConfiguration gc) {
        this.title = title;
        SunToolkit.checkAndSetPolicy(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    String constructComponentName() {
        Class<Frame> clazz = Frame.class;
        synchronized (Frame.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return base + nameCounter++;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.peer == null) {
                this.peer = this.getComponentFactory().createFrame(this);
            }
            FramePeer p = (FramePeer)this.peer;
            MenuBar menuBar = this.menuBar;
            if (menuBar != null) {
                this.mbManagement = true;
                menuBar.addNotify();
                p.setMenuBar(menuBar);
            }
            p.setMaximizedBounds(this.maximizedBounds);
            super.addNotify();
        }
    }

    public String getTitle() {
        return this.title;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTitle(String title) {
        String oldTitle = this.title;
        if (title == null) {
            title = "";
        }
        Frame frame = this;
        synchronized (frame) {
            this.title = title;
            FramePeer peer = (FramePeer)this.peer;
            if (peer != null) {
                peer.setTitle(title);
            }
        }
        this.firePropertyChange("title", oldTitle, title);
    }

    public Image getIconImage() {
        List icons = this.icons;
        if (icons != null && icons.size() > 0) {
            return (Image)icons.get(0);
        }
        return null;
    }

    @Override
    public void setIconImage(Image image) {
        super.setIconImage(image);
    }

    public MenuBar getMenuBar() {
        return this.menuBar;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setMenuBar(MenuBar mb) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.menuBar == mb) {
                return;
            }
            if (mb != null && mb.parent != null) {
                mb.parent.remove(mb);
            }
            if (this.menuBar != null) {
                this.remove(this.menuBar);
            }
            this.menuBar = mb;
            if (this.menuBar != null) {
                this.menuBar.parent = this;
                FramePeer peer = (FramePeer)this.peer;
                if (peer != null) {
                    this.mbManagement = true;
                    this.menuBar.addNotify();
                    this.invalidateIfValid();
                    peer.setMenuBar(this.menuBar);
                }
            }
        }
    }

    public boolean isResizable() {
        return this.resizable;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setResizable(boolean resizable) {
        boolean oldResizable = this.resizable;
        boolean testvalid = false;
        Frame frame = this;
        synchronized (frame) {
            this.resizable = resizable;
            FramePeer peer = (FramePeer)this.peer;
            if (peer != null) {
                peer.setResizable(resizable);
                testvalid = true;
            }
        }
        if (testvalid) {
            this.invalidateIfValid();
        }
        this.firePropertyChange("resizable", oldResizable, resizable);
    }

    public synchronized void setState(int state) {
        int current = this.getExtendedState();
        if (state == 1 && (current & 1) == 0) {
            this.setExtendedState(current | 1);
        } else if (state == 0 && (current & 1) != 0) {
            this.setExtendedState(current & 0xFFFFFFFE);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setExtendedState(int state) {
        if (!this.isFrameStateSupported(state)) {
            return;
        }
        Object object = this.getObjectLock();
        synchronized (object) {
            this.state = state;
        }
        FramePeer peer = (FramePeer)this.peer;
        if (peer != null) {
            peer.setState(state);
        }
    }

    private boolean isFrameStateSupported(int state) {
        if (!this.getToolkit().isFrameStateSupported(state)) {
            if ((state & 1) != 0 && !this.getToolkit().isFrameStateSupported(1)) {
                return false;
            }
            return this.getToolkit().isFrameStateSupported(state &= 0xFFFFFFFE);
        }
        return true;
    }

    public synchronized int getState() {
        return (this.getExtendedState() & 1) != 0 ? 1 : 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getExtendedState() {
        Object object = this.getObjectLock();
        synchronized (object) {
            return this.state;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setMaximizedBounds(Rectangle bounds) {
        Object object = this.getObjectLock();
        synchronized (object) {
            this.maximizedBounds = bounds;
        }
        FramePeer peer = (FramePeer)this.peer;
        if (peer != null) {
            peer.setMaximizedBounds(bounds);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Rectangle getMaximizedBounds() {
        Object object = this.getObjectLock();
        synchronized (object) {
            return this.maximizedBounds;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setUndecorated(boolean undecorated) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.isDisplayable()) {
                throw new IllegalComponentStateException("The frame is displayable.");
            }
            if (!undecorated) {
                if (this.getOpacity() < 1.0f) {
                    throw new IllegalComponentStateException("The frame is not opaque");
                }
                if (this.getShape() != null) {
                    throw new IllegalComponentStateException("The frame does not have a default shape");
                }
                Color bg = this.getBackground();
                if (bg != null && bg.getAlpha() < 255) {
                    throw new IllegalComponentStateException("The frame background color is not opaque");
                }
            }
            this.undecorated = undecorated;
        }
    }

    public boolean isUndecorated() {
        return this.undecorated;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOpacity(float opacity) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (opacity < 1.0f && !this.isUndecorated()) {
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setOpacity(opacity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setShape(Shape shape) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (shape != null && !this.isUndecorated()) {
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setShape(shape);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBackground(Color bgColor) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (bgColor != null && bgColor.getAlpha() < 255 && !this.isUndecorated()) {
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setBackground(bgColor);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(MenuComponent m) {
        if (m == null) {
            return;
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            if (m == this.menuBar) {
                this.menuBar = null;
                FramePeer peer = (FramePeer)this.peer;
                if (peer != null) {
                    this.mbManagement = true;
                    this.invalidateIfValid();
                    peer.setMenuBar(null);
                    m.removeNotify();
                }
                m.parent = null;
            } else {
                super.remove(m);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            FramePeer peer = (FramePeer)this.peer;
            if (peer != null) {
                this.getState();
                if (this.menuBar != null) {
                    this.mbManagement = true;
                    peer.setMenuBar(null);
                    this.menuBar.removeNotify();
                }
            }
            super.removeNotify();
        }
    }

    @Override
    void postProcessKeyEvent(KeyEvent e) {
        if (this.menuBar != null && this.menuBar.handleShortcut(e)) {
            e.consume();
            return;
        }
        super.postProcessKeyEvent(e);
    }

    @Override
    protected String paramString() {
        int state;
        Object str = super.paramString();
        if (this.title != null) {
            str = (String)str + ",title=" + this.title;
        }
        if (this.resizable) {
            str = (String)str + ",resizable";
        }
        if ((state = this.getExtendedState()) == 0) {
            str = (String)str + ",normal";
        } else {
            if ((state & 1) != 0) {
                str = (String)str + ",iconified";
            }
            if ((state & 6) == 6) {
                str = (String)str + ",maximized";
            } else if ((state & 2) != 0) {
                str = (String)str + ",maximized_horiz";
            } else if ((state & 4) != 0) {
                str = (String)str + ",maximized_vert";
            }
        }
        return str;
    }

    @Deprecated
    public void setCursor(int cursorType) {
        if (cursorType < 0 || cursorType > 13) {
            throw new IllegalArgumentException("illegal cursor type");
        }
        this.setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    @Deprecated
    public int getCursorType() {
        return this.getCursor().getType();
    }

    public static Frame[] getFrames() {
        Window[] allWindows = Window.getWindows();
        int frameCount = 0;
        for (Window w : allWindows) {
            if (!(w instanceof Frame)) continue;
            ++frameCount;
        }
        Frame[] frames = new Frame[frameCount];
        int c = 0;
        for (Window w : allWindows) {
            if (!(w instanceof Frame)) continue;
            frames[c++] = (Frame)w;
        }
        return frames;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Image icon1;
        s.defaultWriteObject();
        if (this.icons != null && this.icons.size() > 0 && (icon1 = (Image)this.icons.get(0)) instanceof Serializable) {
            s.writeObject(icon1);
            return;
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        block6: {
            s.defaultReadObject();
            try {
                Image icon = (Image)s.readObject();
                if (this.icons == null) {
                    this.icons = new ArrayList();
                    this.icons.add(icon);
                }
            }
            catch (OptionalDataException e) {
                if (e.eof) break block6;
                throw e;
            }
        }
        if (this.menuBar != null) {
            this.menuBar.parent = this;
        }
        if (this.ownedWindows != null) {
            for (int i = 0; i < this.ownedWindows.size(); ++i) {
                this.connectOwnedWindow(this.ownedWindows.elementAt(i));
            }
            this.ownedWindows = null;
        }
    }

    private static native void initIDs();

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleAWTFrame();
        }
        return this.accessibleContext;
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Frame.initIDs();
        }
        AWTAccessor.setFrameAccessor(new AWTAccessor.FrameAccessor(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void setExtendedState(Frame frame, int state) {
                Object object = frame.getObjectLock();
                synchronized (object) {
                    frame.state = state;
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public int getExtendedState(Frame frame) {
                Object object = frame.getObjectLock();
                synchronized (object) {
                    return frame.state;
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public Rectangle getMaximizedBounds(Frame frame) {
                Object object = frame.getObjectLock();
                synchronized (object) {
                    return frame.maximizedBounds;
                }
            }
        });
    }

    protected class AccessibleAWTFrame
    extends Window.AccessibleAWTWindow {
        private static final long serialVersionUID = -6172960752956030250L;

        protected AccessibleAWTFrame() {
            super(Frame.this);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.FRAME;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (Frame.this.getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            if (Frame.this.isResizable()) {
                states.add(AccessibleState.RESIZABLE);
            }
            return states;
        }
    }
}

