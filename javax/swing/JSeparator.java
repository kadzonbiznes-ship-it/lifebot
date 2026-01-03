/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.UIManager;
import javax.swing.plaf.SeparatorUI;

@JavaBean(defaultProperty="UI", description="A divider between menu items.")
@SwingContainer(value=false)
public class JSeparator
extends JComponent
implements SwingConstants,
Accessible {
    private static final String uiClassID = "SeparatorUI";
    private int orientation = 0;

    public JSeparator() {
        this(0);
    }

    public JSeparator(int orientation) {
        this.checkOrientation(orientation);
        this.orientation = orientation;
        this.setFocusable(false);
        this.updateUI();
    }

    @Override
    public SeparatorUI getUI() {
        return (SeparatorUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(SeparatorUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((SeparatorUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false)
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

    public int getOrientation() {
        return this.orientation;
    }

    @BeanProperty(preferred=true, visualUpdate=true, enumerationValues={"SwingConstants.HORIZONTAL", "SwingConstants.VERTICAL"}, description="The orientation of the separator.")
    public void setOrientation(int orientation) {
        if (this.orientation == orientation) {
            return;
        }
        int oldValue = this.orientation;
        this.checkOrientation(orientation);
        this.orientation = orientation;
        this.firePropertyChange("orientation", oldValue, orientation);
        this.revalidate();
        this.repaint();
    }

    private void checkOrientation(int orientation) {
        switch (orientation) {
            case 0: 
            case 1: {
                break;
            }
            default: {
                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
            }
        }
    }

    @Override
    protected String paramString() {
        String orientationString = this.orientation == 0 ? "HORIZONTAL" : "VERTICAL";
        return super.paramString() + ",orientation=" + orientationString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJSeparator();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJSeparator
    extends JComponent.AccessibleJComponent {
        protected AccessibleJSeparator() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SEPARATOR;
        }
    }
}

