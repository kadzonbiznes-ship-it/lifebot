/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.beans.BeanProperty;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ToolTipUI;

public class JToolTip
extends JComponent
implements Accessible {
    private static final String uiClassID = "ToolTipUI";
    String tipText;
    JComponent component;

    public JToolTip() {
        this.updateUI();
    }

    @Override
    public ToolTipUI getUI() {
        return (ToolTipUI)this.ui;
    }

    @Override
    public void updateUI() {
        this.setUI((ToolTipUI)UIManager.getUI(this));
    }

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    @BeanProperty(preferred=true, description="Sets the text of the tooltip")
    public void setTipText(String tipText) {
        String oldValue = this.tipText;
        this.tipText = tipText;
        this.firePropertyChange("tiptext", oldValue, tipText);
        if (!Objects.equals(oldValue, tipText)) {
            this.revalidate();
            this.repaint();
        }
    }

    public String getTipText() {
        return this.tipText;
    }

    @BeanProperty(description="Sets the component that the tooltip describes.")
    public void setComponent(JComponent c) {
        JComponent oldValue = this.component;
        this.component = c;
        this.firePropertyChange("component", oldValue, c);
    }

    public JComponent getComponent() {
        return this.component;
    }

    @Override
    boolean alwaysOnTop() {
        return true;
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
        String tipTextString = this.tipText != null ? this.tipText : "";
        return super.paramString() + ",tipText=" + tipTextString;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJToolTip();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJToolTip
    extends JComponent.AccessibleJComponent {
        protected AccessibleJToolTip() {
        }

        @Override
        public String getAccessibleDescription() {
            String description = this.accessibleDescription;
            if (description == null) {
                description = (String)JToolTip.this.getClientProperty("AccessibleDescription");
            }
            if (description == null) {
                description = JToolTip.this.getTipText();
            }
            return description;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TOOL_TIP;
        }
    }
}

