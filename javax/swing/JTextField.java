/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionPropertyChangeListener;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.TextAction;

@JavaBean(defaultProperty="UIClassID", description="A component which allows for the editing of a single line of text.")
@SwingContainer(value=false)
public class JTextField
extends JTextComponent
implements SwingConstants {
    private Action action;
    private PropertyChangeListener actionPropertyChangeListener;
    public static final String notifyAction = "notify-field-accept";
    private BoundedRangeModel visibility;
    private int horizontalAlignment = 10;
    private int columns;
    private int columnWidth;
    private String command;
    private static final Action[] defaultActions = new Action[]{new NotifyAction()};
    private static final String uiClassID = "TextFieldUI";

    public JTextField() {
        this(null, null, 0);
    }

    public JTextField(String text) {
        this(null, text, 0);
    }

    public JTextField(int columns) {
        this(null, null, columns);
    }

    public JTextField(String text, int columns) {
        this(null, text, columns);
    }

    public JTextField(Document doc, String text, int columns) {
        if (columns < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        this.visibility = new DefaultBoundedRangeModel();
        this.visibility.addChangeListener(new ScrollRepainter());
        this.columns = columns;
        if (doc == null) {
            doc = this.createDefaultModel();
        }
        this.setDocument(doc);
        if (text != null) {
            this.setText(text);
        }
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    @BeanProperty(expert=true, description="the text document model")
    public void setDocument(Document doc) {
        if (doc != null) {
            doc.putProperty("filterNewlines", Boolean.TRUE);
        }
        super.setDocument(doc);
    }

    @Override
    public boolean isValidateRoot() {
        return !(SwingUtilities.getUnwrappedParent(this) instanceof JViewport);
    }

    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    @BeanProperty(preferred=true, enumerationValues={"JTextField.LEFT", "JTextField.CENTER", "JTextField.RIGHT", "JTextField.LEADING", "JTextField.TRAILING"}, description="Set the field alignment to LEFT, CENTER, RIGHT, LEADING (the default) or TRAILING")
    public void setHorizontalAlignment(int alignment) {
        if (alignment == this.horizontalAlignment) {
            return;
        }
        int oldValue = this.horizontalAlignment;
        if (alignment != 2 && alignment != 0 && alignment != 4 && alignment != 10 && alignment != 11) {
            throw new IllegalArgumentException("horizontalAlignment");
        }
        this.horizontalAlignment = alignment;
        this.firePropertyChange("horizontalAlignment", oldValue, this.horizontalAlignment);
        this.invalidate();
        this.repaint();
    }

    protected Document createDefaultModel() {
        return new PlainDocument();
    }

    public int getColumns() {
        return this.columns;
    }

    @BeanProperty(bound=false, description="the number of columns preferred for display")
    public void setColumns(int columns) {
        int oldVal = this.columns;
        if (columns < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        if (columns != oldVal) {
            this.columns = columns;
            this.invalidate();
        }
    }

    protected int getColumnWidth() {
        if (this.columnWidth == 0) {
            FontMetrics metrics = this.getFontMetrics(this.getFont());
            this.columnWidth = metrics.charWidth('m');
        }
        return this.columnWidth;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        if (this.columns != 0) {
            Insets insets = this.getInsets();
            size.width = this.columns * this.getColumnWidth() + insets.left + insets.right;
        }
        return size;
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        this.columnWidth = 0;
    }

    public synchronized void addActionListener(ActionListener l) {
        this.listenerList.add(ActionListener.class, l);
    }

    public synchronized void removeActionListener(ActionListener l) {
        if (l != null && this.getAction() == l) {
            this.setAction(null);
        } else {
            this.listenerList.remove(ActionListener.class, l);
        }
    }

    @BeanProperty(bound=false)
    public synchronized ActionListener[] getActionListeners() {
        return (ActionListener[])this.listenerList.getListeners(ActionListener.class);
    }

    protected void fireActionPerformed() {
        Object[] listeners = this.listenerList.getListenerList();
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent)currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent)currentEvent).getModifiers();
        }
        ActionEvent e = new ActionEvent(this, 1001, this.command != null ? this.command : this.getText(), EventQueue.getMostRecentEventTime(), modifiers);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ActionListener.class) continue;
            ((ActionListener)listeners[i + 1]).actionPerformed(e);
        }
    }

    public void setActionCommand(String command) {
        this.command = command;
    }

    @BeanProperty(visualUpdate=true, description="the Action instance connected with this ActionEvent source")
    public void setAction(Action a) {
        Action oldValue = this.getAction();
        if (this.action == null || !this.action.equals(a)) {
            this.action = a;
            if (oldValue != null) {
                this.removeActionListener(oldValue);
                oldValue.removePropertyChangeListener(this.actionPropertyChangeListener);
                this.actionPropertyChangeListener = null;
            }
            this.configurePropertiesFromAction(this.action);
            if (this.action != null) {
                if (!this.isListener(ActionListener.class, this.action)) {
                    this.addActionListener(this.action);
                }
                this.actionPropertyChangeListener = this.createActionPropertyChangeListener(this.action);
                this.action.addPropertyChangeListener(this.actionPropertyChangeListener);
            }
            this.firePropertyChange("action", oldValue, this.action);
        }
    }

    private boolean isListener(Class<?> c, ActionListener a) {
        boolean isListener = false;
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != c || listeners[i + 1] != a) continue;
            isListener = true;
        }
        return isListener;
    }

    public Action getAction() {
        return this.action;
    }

    protected void configurePropertiesFromAction(Action a) {
        AbstractAction.setEnabledFromAction(this, a);
        AbstractAction.setToolTipTextFromAction(this, a);
        this.setActionCommandFromAction(a);
    }

    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == "ActionCommandKey") {
            this.setActionCommandFromAction(action);
        } else if (propertyName == "enabled") {
            AbstractAction.setEnabledFromAction(this, action);
        } else if (propertyName == "ShortDescription") {
            AbstractAction.setToolTipTextFromAction(this, action);
        }
    }

    private void setActionCommandFromAction(Action action) {
        this.setActionCommand(action == null ? null : (String)action.getValue("ActionCommandKey"));
    }

    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new TextFieldActionPropertyChangeListener(this, a);
    }

    @Override
    @BeanProperty(bound=false)
    public Action[] getActions() {
        return TextAction.augmentList(super.getActions(), defaultActions);
    }

    public void postActionEvent() {
        this.fireActionPerformed();
    }

    @BeanProperty(bound=false)
    public BoundedRangeModel getHorizontalVisibility() {
        return this.visibility;
    }

    public int getScrollOffset() {
        return this.visibility.getValue();
    }

    public void setScrollOffset(int scrollOffset) {
        this.visibility.setValue(scrollOffset);
    }

    @Override
    public void scrollRectToVisible(Rectangle r) {
        Insets i = this.getInsets();
        int x0 = r.x + this.visibility.getValue() - i.left;
        int x1 = x0 + r.width;
        if (x0 < this.visibility.getValue()) {
            this.visibility.setValue(x0);
        } else if (x1 > this.visibility.getValue() + this.visibility.getExtent()) {
            this.visibility.setValue(x1 - this.visibility.getExtent());
        }
    }

    boolean hasActionListener() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ActionListener.class) continue;
            return true;
        }
        return false;
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
        String horizontalAlignmentString = this.horizontalAlignment == 2 ? "LEFT" : (this.horizontalAlignment == 0 ? "CENTER" : (this.horizontalAlignment == 4 ? "RIGHT" : (this.horizontalAlignment == 10 ? "LEADING" : (this.horizontalAlignment == 11 ? "TRAILING" : ""))));
        String commandString = this.command != null ? this.command : "";
        return super.paramString() + ",columns=" + this.columns + ",columnWidth=" + this.columnWidth + ",command=" + commandString + ",horizontalAlignment=" + horizontalAlignmentString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJTextField();
        }
        return this.accessibleContext;
    }

    class ScrollRepainter
    implements ChangeListener,
    Serializable {
        ScrollRepainter() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JTextField.this.repaint();
        }
    }

    private static class TextFieldActionPropertyChangeListener
    extends ActionPropertyChangeListener<JTextField> {
        TextFieldActionPropertyChangeListener(JTextField tf, Action a) {
            super(tf, a);
        }

        @Override
        protected void actionPropertyChanged(JTextField textField, Action action, PropertyChangeEvent e) {
            if (AbstractAction.shouldReconfigure(e)) {
                textField.configurePropertiesFromAction(action);
            } else {
                textField.actionPropertyChanged(action, e.getPropertyName());
            }
        }
    }

    protected class AccessibleJTextField
    extends JTextComponent.AccessibleJTextComponent {
        protected AccessibleJTextField() {
            super(JTextField.this);
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.SINGLE_LINE);
            return states;
        }
    }

    static class NotifyAction
    extends TextAction {
        NotifyAction() {
            super(JTextField.notifyAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getFocusedComponent();
            if (target instanceof JTextField) {
                JTextField field = (JTextField)target;
                field.postActionEvent();
            }
        }

        @Override
        public boolean isEnabled() {
            JTextComponent target = this.getFocusedComponent();
            if (target instanceof JTextField) {
                return ((JTextField)target).hasActionListener();
            }
            return false;
        }
    }
}

