/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.beans.BeanProperty;
import java.beans.ConstructorProperties;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;

@JavaBean(defaultProperty="UIClassID", description="An implementation of a \"push\" button.")
@SwingContainer(value=false)
public class JButton
extends AbstractButton
implements Accessible {
    private static final String uiClassID = "ButtonUI";

    public JButton() {
        this(null, null);
    }

    public JButton(Icon icon) {
        this(null, icon);
    }

    @ConstructorProperties(value={"text"})
    public JButton(String text) {
        this(text, null);
    }

    public JButton(Action a) {
        this();
        this.setAction(a);
    }

    public JButton(String text, Icon icon) {
        this.setModel(new DefaultButtonModel());
        this.init(text, icon);
    }

    @Override
    public void updateUI() {
        this.setUI((ButtonUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false, expert=true, description="A string that specifies the name of the L&F class.")
    public String getUIClassID() {
        return uiClassID;
    }

    @BeanProperty(bound=false, description="Whether or not this button is the default button")
    public boolean isDefaultButton() {
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) {
            return root.getDefaultButton() == this;
        }
        return false;
    }

    public boolean isDefaultCapable() {
        return this.defaultCapable;
    }

    @BeanProperty(visualUpdate=true, description="Whether or not this button can be the default button")
    public void setDefaultCapable(boolean defaultCapable) {
        boolean oldDefaultCapable = this.defaultCapable;
        this.defaultCapable = defaultCapable;
        this.firePropertyChange("defaultCapable", oldDefaultCapable, defaultCapable);
    }

    @Override
    public void removeNotify() {
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null && root.getDefaultButton() == this) {
            root.setDefaultButton(null);
        }
        super.removeNotify();
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
        String defaultCapableString = this.defaultCapable ? "true" : "false";
        return super.paramString() + ",defaultCapable=" + defaultCapableString;
    }

    @Override
    @BeanProperty(bound=false, expert=true, description="The AccessibleContext associated with this Button.")
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJButton();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJButton
    extends AbstractButton.AccessibleAbstractButton {
        protected AccessibleJButton() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PUSH_BUTTON;
        }
    }
}

