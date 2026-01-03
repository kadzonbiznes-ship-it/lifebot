/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferStrategy;
import java.awt.peer.CanvasPeer;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class Canvas
extends Component
implements Accessible {
    private static final String base = "canvas";
    private static int nameCounter = 0;
    private static final long serialVersionUID = -2284879212465893870L;

    public Canvas() {
    }

    public Canvas(GraphicsConfiguration config) {
        this();
        this.setGraphicsConfiguration(config);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        Object object = this.getTreeLock();
        synchronized (object) {
            CanvasPeer peer = (CanvasPeer)this.peer;
            if (peer != null) {
                gc = peer.getAppropriateGraphicsConfiguration(gc);
            }
            super.setGraphicsConfiguration(gc);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    String constructComponentName() {
        Class<Canvas> clazz = Canvas.class;
        synchronized (Canvas.class) {
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
                this.peer = this.getComponentFactory().createCanvas(this);
            }
            super.addNotify();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, this.width, this.height);
    }

    @Override
    public void update(Graphics g) {
        g.clearRect(0, 0, this.width, this.height);
        this.paint(g);
    }

    @Override
    boolean postsOldMouseEvents() {
        return true;
    }

    @Override
    public void createBufferStrategy(int numBuffers) {
        super.createBufferStrategy(numBuffers);
    }

    @Override
    public void createBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {
        super.createBufferStrategy(numBuffers, caps);
    }

    @Override
    public BufferStrategy getBufferStrategy() {
        return super.getBufferStrategy();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleAWTCanvas();
        }
        return this.accessibleContext;
    }

    protected class AccessibleAWTCanvas
    extends Component.AccessibleAWTComponent {
        private static final long serialVersionUID = -6325592262103146699L;

        protected AccessibleAWTCanvas() {
            super(Canvas.this);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CANVAS;
        }
    }
}

