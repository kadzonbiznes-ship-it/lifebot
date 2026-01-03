/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.PanelUI;

@JavaBean(defaultProperty="UI", description="A generic lightweight container.")
public class JPanel
extends JComponent
implements Accessible {
    private static final String uiClassID = "PanelUI";

    public JPanel(LayoutManager layout, boolean isDoubleBuffered) {
        this.setLayout(layout);
        this.setDoubleBuffered(isDoubleBuffered);
        this.setUIProperty("opaque", Boolean.TRUE);
        this.updateUI();
    }

    public JPanel(LayoutManager layout) {
        this(layout, true);
    }

    public JPanel(boolean isDoubleBuffered) {
        this(new FlowLayout(), isDoubleBuffered);
    }

    public JPanel() {
        this(true);
    }

    @Override
    public void updateUI() {
        this.setUI((PanelUI)UIManager.getUI(this));
    }

    @Override
    public PanelUI getUI() {
        return (PanelUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(PanelUI ui) {
        super.setUI(ui);
    }

    @Override
    @BeanProperty(bound=false, expert=true, description="A string that specifies the name of the L&F class.")
    public String getUIClassID() {
        return uiClassID;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (this.getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            count = (byte)(count - 1);
            JComponent.setWriteObjCounter(this, count);
            if (count == 0 && this.ui != null) {
                this.ui.installUI(this);
            }
        }
    }

    @Override
    protected String paramString() {
        return super.paramString();
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJPanel();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJPanel
    extends JComponent.AccessibleJComponent {
        protected AccessibleJPanel() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PANEL;
        }
    }
}

