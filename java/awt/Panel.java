/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class Panel
extends Container
implements Accessible {
    private static final String base = "panel";
    private static int nameCounter = 0;
    private static final long serialVersionUID = -2728009084054400034L;

    public Panel() {
        this(new FlowLayout());
    }

    public Panel(LayoutManager layout) {
        this.setLayout(layout);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    String constructComponentName() {
        Class<Panel> clazz = Panel.class;
        synchronized (Panel.class) {
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
                this.peer = this.getComponentFactory().createPanel(this);
            }
            super.addNotify();
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleAWTPanel();
        }
        return this.accessibleContext;
    }

    protected class AccessibleAWTPanel
    extends Container.AccessibleAWTContainer {
        private static final long serialVersionUID = -6409552226660031050L;

        protected AccessibleAWTPanel() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PANEL;
        }
    }
}

