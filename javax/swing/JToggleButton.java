/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingContainer;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;

@JavaBean(defaultProperty="UIClassID", description="An implementation of a two-state button.")
@SwingContainer(value=false)
public class JToggleButton
extends AbstractButton
implements Accessible {
    private static final String uiClassID = "ToggleButtonUI";

    public JToggleButton() {
        this(null, null, false);
    }

    public JToggleButton(Icon icon) {
        this(null, icon, false);
    }

    public JToggleButton(Icon icon, boolean selected) {
        this(null, icon, selected);
    }

    public JToggleButton(String text) {
        this(text, null, false);
    }

    public JToggleButton(String text, boolean selected) {
        this(text, null, selected);
    }

    public JToggleButton(Action a) {
        this();
        this.setAction(a);
    }

    public JToggleButton(String text, Icon icon) {
        this(text, icon, false);
    }

    public JToggleButton(String text, Icon icon, boolean selected) {
        this.setModel(new ToggleButtonModel());
        this.model.setSelected(selected);
        this.init(text, icon);
    }

    @Override
    public void updateUI() {
        this.setUI((ButtonUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false, description="A string that specifies the name of the L&F class")
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    boolean shouldUpdateSelectedStateFromAction() {
        return true;
    }

    private JToggleButton getGroupSelection(FocusEvent.Cause cause) {
        switch (cause) {
            case ACTIVATION: 
            case TRAVERSAL: 
            case TRAVERSAL_UP: 
            case TRAVERSAL_DOWN: 
            case TRAVERSAL_FORWARD: 
            case TRAVERSAL_BACKWARD: {
                ButtonGroup group;
                ButtonModel model = this.getModel();
                JToggleButton selection = this;
                if (model != null && (group = model.getGroup()) != null && group.getSelection() != null && !group.isSelected(model)) {
                    Iterator<AbstractButton> iterator = group.getElements().asIterator();
                    while (iterator.hasNext()) {
                        AbstractButton member = iterator.next();
                        if (!group.isSelected(member.getModel())) continue;
                        if (!(member instanceof JToggleButton) || !member.isVisible() || !member.isDisplayable() || !member.isEnabled() || !member.isFocusable()) break;
                        selection = (JToggleButton)member;
                        break;
                    }
                }
                return selection;
            }
        }
        return this;
    }

    @Override
    public void requestFocus(FocusEvent.Cause cause) {
        this.getGroupSelection(cause).requestFocusUnconditionally(cause);
    }

    private void requestFocusUnconditionally(FocusEvent.Cause cause) {
        super.requestFocus(cause);
    }

    @Override
    public boolean requestFocusInWindow(FocusEvent.Cause cause) {
        return this.getGroupSelection(cause).requestFocusInWindowUnconditionally(cause);
    }

    private boolean requestFocusInWindowUnconditionally(FocusEvent.Cause cause) {
        return super.requestFocusInWindow(cause);
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
    @BeanProperty(bound=false, expert=true, description="The AccessibleContext associated with this ToggleButton.")
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJToggleButton();
        }
        return this.accessibleContext;
    }

    public static class ToggleButtonModel
    extends DefaultButtonModel {
        @Override
        public boolean isSelected() {
            return (this.stateMask & 2) != 0;
        }

        @Override
        public void setSelected(boolean b) {
            ButtonGroup group = this.getGroup();
            if (group != null) {
                group.setSelected(this, b);
                b = group.isSelected(this);
            }
            if (this.isSelected() == b) {
                return;
            }
            this.stateMask = b ? (this.stateMask |= 2) : (this.stateMask &= 0xFFFFFFFD);
            this.fireStateChanged();
            this.fireItemStateChanged(new ItemEvent(this, 701, this, this.isSelected() ? 1 : 2));
        }

        @Override
        public void setPressed(boolean b) {
            if (this.isPressed() == b || !this.isEnabled()) {
                return;
            }
            if (!b && this.isArmed()) {
                this.setSelected(!this.isSelected());
            }
            this.stateMask = b ? (this.stateMask |= 4) : (this.stateMask &= 0xFFFFFFFB);
            this.fireStateChanged();
            if (!this.isPressed() && this.isArmed()) {
                int modifiers = 0;
                AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if (currentEvent instanceof InputEvent) {
                    modifiers = ((InputEvent)currentEvent).getModifiers();
                } else if (currentEvent instanceof ActionEvent) {
                    modifiers = ((ActionEvent)currentEvent).getModifiers();
                }
                this.fireActionPerformed(new ActionEvent(this, 1001, this.getActionCommand(), EventQueue.getMostRecentEventTime(), modifiers));
            }
        }
    }

    protected class AccessibleJToggleButton
    extends AbstractButton.AccessibleAbstractButton
    implements ItemListener {
        public AccessibleJToggleButton() {
            JToggleButton.this.addItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            JToggleButton tb = (JToggleButton)e.getSource();
            if (JToggleButton.this.accessibleContext != null) {
                if (tb.isSelected()) {
                    JToggleButton.this.accessibleContext.firePropertyChange("AccessibleState", null, AccessibleState.CHECKED);
                } else {
                    JToggleButton.this.accessibleContext.firePropertyChange("AccessibleState", AccessibleState.CHECKED, null);
                }
            }
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TOGGLE_BUTTON;
        }
    }
}

