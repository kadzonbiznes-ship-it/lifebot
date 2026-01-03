/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.spi.DateFormatProvider;
import java.text.spi.NumberFormatProvider;
import java.util.Date;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleEditableText;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.SpinnerUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

@JavaBean(defaultProperty="UI", description="A single line input field that lets the user select a number or an object value from an ordered set.")
@SwingContainer(value=false)
public class JSpinner
extends JComponent
implements Accessible {
    private static final String uiClassID = "SpinnerUI";
    private static final Action DISABLED_ACTION = new DisabledAction();
    private SpinnerModel model;
    private JComponent editor;
    private ChangeListener modelListener;
    private transient ChangeEvent changeEvent;
    private boolean editorExplicitlySet = false;

    public JSpinner(SpinnerModel model) {
        if (model == null) {
            throw new NullPointerException("model cannot be null");
        }
        this.model = model;
        this.editor = this.createEditor(model);
        this.setUIProperty("opaque", true);
        this.updateUI();
    }

    public JSpinner() {
        this(new SpinnerNumberModel());
    }

    @Override
    public SpinnerUI getUI() {
        return (SpinnerUI)this.ui;
    }

    public void setUI(SpinnerUI ui) {
        super.setUI(ui);
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public void updateUI() {
        this.setUI((SpinnerUI)UIManager.getUI(this));
        this.invalidate();
    }

    protected JComponent createEditor(SpinnerModel model) {
        if (model instanceof SpinnerDateModel) {
            return new DateEditor(this);
        }
        if (model instanceof SpinnerListModel) {
            return new ListEditor(this);
        }
        if (model instanceof SpinnerNumberModel) {
            return new NumberEditor(this);
        }
        return new DefaultEditor(this);
    }

    @BeanProperty(visualUpdate=true, description="Model that represents the value of this spinner.")
    public void setModel(SpinnerModel model) {
        if (model == null) {
            throw new IllegalArgumentException("null model");
        }
        if (!model.equals(this.model)) {
            SpinnerModel oldModel = this.model;
            this.model = model;
            if (this.modelListener != null) {
                oldModel.removeChangeListener(this.modelListener);
                this.model.addChangeListener(this.modelListener);
            }
            this.firePropertyChange("model", oldModel, model);
            if (!this.editorExplicitlySet) {
                this.setEditor(this.createEditor(model));
                this.editorExplicitlySet = false;
            }
            this.repaint();
            this.revalidate();
        }
    }

    public SpinnerModel getModel() {
        return this.model;
    }

    public Object getValue() {
        return this.getModel().getValue();
    }

    public void setValue(Object value) {
        this.getModel().setValue(value);
    }

    @BeanProperty(bound=false)
    public Object getNextValue() {
        return this.getModel().getNextValue();
    }

    public void addChangeListener(ChangeListener listener) {
        if (this.modelListener == null) {
            this.modelListener = new ModelListener();
            this.getModel().addChangeListener(this.modelListener);
        }
        this.listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        this.listenerList.remove(ChangeListener.class, listener);
    }

    @BeanProperty(bound=false)
    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    @BeanProperty(bound=false)
    public Object getPreviousValue() {
        return this.getModel().getPreviousValue();
    }

    @BeanProperty(visualUpdate=true, description="JComponent that displays the current value of the model")
    public void setEditor(JComponent editor) {
        if (editor == null) {
            throw new IllegalArgumentException("null editor");
        }
        if (!editor.equals(this.editor)) {
            JComponent oldEditor = this.editor;
            this.editor = editor;
            if (oldEditor instanceof DefaultEditor) {
                ((DefaultEditor)oldEditor).dismiss(this);
            }
            this.editorExplicitlySet = true;
            this.firePropertyChange("editor", oldEditor, editor);
            this.revalidate();
            this.repaint();
        }
    }

    public JComponent getEditor() {
        return this.editor;
    }

    public void commitEdit() throws ParseException {
        JComponent editor = this.getEditor();
        if (editor instanceof DefaultEditor) {
            ((DefaultEditor)editor).commitEdit();
        }
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
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJSpinner();
        }
        return this.accessibleContext;
    }

    public static class DateEditor
    extends DefaultEditor {
        private static String getDefaultPattern(Locale loc) {
            LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(DateFormatProvider.class, loc);
            LocaleResources lr = adapter.getLocaleResources(loc);
            if (lr == null) {
                lr = LocaleProviderAdapter.forJRE().getLocaleResources(loc);
            }
            return lr.getDateTimePattern(3, 3, null);
        }

        public DateEditor(JSpinner spinner) {
            this(spinner, DateEditor.getDefaultPattern(spinner.getLocale()));
        }

        public DateEditor(JSpinner spinner, String dateFormatPattern) {
            this(spinner, new SimpleDateFormat(dateFormatPattern, spinner.getLocale()));
        }

        private DateEditor(JSpinner spinner, DateFormat format) {
            super(spinner);
            if (!(spinner.getModel() instanceof SpinnerDateModel)) {
                throw new IllegalArgumentException("model not a SpinnerDateModel");
            }
            SpinnerDateModel model = (SpinnerDateModel)spinner.getModel();
            DateEditorFormatter formatter = new DateEditorFormatter(model, format);
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
            JFormattedTextField ftf = this.getTextField();
            ftf.setEditable(true);
            ftf.setFormatterFactory(factory);
            try {
                String maxString = formatter.valueToString(model.getStart());
                String minString = formatter.valueToString(model.getEnd());
                ftf.setColumns(Math.max(maxString.length(), minString.length()));
            }
            catch (ParseException parseException) {
                // empty catch block
            }
        }

        public SimpleDateFormat getFormat() {
            return (SimpleDateFormat)((DateFormatter)this.getTextField().getFormatter()).getFormat();
        }

        public SpinnerDateModel getModel() {
            return (SpinnerDateModel)this.getSpinner().getModel();
        }
    }

    public static class ListEditor
    extends DefaultEditor {
        public ListEditor(JSpinner spinner) {
            super(spinner);
            if (!(spinner.getModel() instanceof SpinnerListModel)) {
                throw new IllegalArgumentException("model not a SpinnerListModel");
            }
            this.getTextField().setEditable(true);
            this.getTextField().setFormatterFactory(new DefaultFormatterFactory(new ListFormatter()));
        }

        public SpinnerListModel getModel() {
            return (SpinnerListModel)this.getSpinner().getModel();
        }

        private class ListFormatter
        extends JFormattedTextField.AbstractFormatter {
            private DocumentFilter filter;

            private ListFormatter() {
            }

            @Override
            public String valueToString(Object value) throws ParseException {
                if (value == null) {
                    return "";
                }
                return value.toString();
            }

            @Override
            public Object stringToValue(String string) throws ParseException {
                return string;
            }

            @Override
            protected DocumentFilter getDocumentFilter() {
                if (this.filter == null) {
                    this.filter = new Filter();
                }
                return this.filter;
            }

            private class Filter
            extends DocumentFilter {
                private Filter() {
                }

                @Override
                public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                    if (string != null && offset + length == fb.getDocument().getLength()) {
                        String value;
                        Object next = ListEditor.this.getModel().findNextMatch(fb.getDocument().getText(0, offset) + string);
                        String string2 = value = next != null ? next.toString() : null;
                        if (value != null) {
                            fb.remove(0, offset + length);
                            fb.insertString(0, value, null);
                            ListFormatter.this.getFormattedTextField().select(offset + string.length(), value.length());
                            return;
                        }
                    }
                    super.replace(fb, offset, length, string, attrs);
                }

                @Override
                public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    this.replace(fb, offset, 0, string, attr);
                }
            }
        }
    }

    public static class NumberEditor
    extends DefaultEditor {
        private static String getDefaultPattern(Locale locale) {
            LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(NumberFormatProvider.class, locale);
            LocaleResources lr = adapter.getLocaleResources(locale);
            if (lr == null) {
                lr = LocaleProviderAdapter.forJRE().getLocaleResources(locale);
            }
            String[] all = lr.getNumberPatterns();
            return all[0];
        }

        public NumberEditor(JSpinner spinner) {
            this(spinner, NumberEditor.getDefaultPattern(spinner.getLocale()));
        }

        public NumberEditor(JSpinner spinner, String decimalFormatPattern) {
            this(spinner, new DecimalFormat(decimalFormatPattern));
        }

        private NumberEditor(JSpinner spinner, DecimalFormat format) {
            super(spinner);
            if (!(spinner.getModel() instanceof SpinnerNumberModel)) {
                throw new IllegalArgumentException("model not a SpinnerNumberModel");
            }
            SpinnerNumberModel model = (SpinnerNumberModel)spinner.getModel();
            NumberEditorFormatter formatter = new NumberEditorFormatter(model, format);
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
            JFormattedTextField ftf = this.getTextField();
            ftf.setEditable(true);
            ftf.setFormatterFactory(factory);
            ftf.setHorizontalAlignment(4);
            try {
                String maxString = formatter.valueToString(model.getMinimum());
                String minString = formatter.valueToString(model.getMaximum());
                ftf.setColumns(Math.max(maxString.length(), minString.length()));
            }
            catch (ParseException parseException) {
                // empty catch block
            }
        }

        public DecimalFormat getFormat() {
            return (DecimalFormat)((NumberFormatter)this.getTextField().getFormatter()).getFormat();
        }

        public SpinnerNumberModel getModel() {
            return (SpinnerNumberModel)this.getSpinner().getModel();
        }

        @Override
        public void setComponentOrientation(ComponentOrientation o) {
            super.setComponentOrientation(o);
            this.getTextField().setHorizontalAlignment(o.isLeftToRight() ? 4 : 2);
        }
    }

    public static class DefaultEditor
    extends JPanel
    implements ChangeListener,
    PropertyChangeListener,
    LayoutManager {
        public DefaultEditor(JSpinner spinner) {
            super(null);
            JFormattedTextField ftf = new JFormattedTextField();
            ftf.setName("Spinner.formattedTextField");
            ftf.setValue(spinner.getValue());
            ftf.addPropertyChangeListener(this);
            ftf.setEditable(false);
            ftf.setInheritsPopupMenu(true);
            String toolTipText = spinner.getToolTipText();
            if (toolTipText != null) {
                ftf.setToolTipText(toolTipText);
            }
            this.add(ftf);
            this.setLayout(this);
            spinner.addChangeListener(this);
            ActionMap ftfMap = ftf.getActionMap();
            if (ftfMap != null) {
                ftfMap.put("increment", DISABLED_ACTION);
                ftfMap.put("decrement", DISABLED_ACTION);
            }
        }

        public void dismiss(JSpinner spinner) {
            spinner.removeChangeListener(this);
        }

        public JSpinner getSpinner() {
            for (Container c = this; c != null; c = c.getParent()) {
                if (!(c instanceof JSpinner)) continue;
                return (JSpinner)c;
            }
            return null;
        }

        public JFormattedTextField getTextField() {
            return (JFormattedTextField)this.getComponent(0);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner spinner = (JSpinner)e.getSource();
            this.getTextField().setValue(spinner.getValue());
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            JSpinner spinner = this.getSpinner();
            if (spinner == null) {
                return;
            }
            Object source = e.getSource();
            String name = e.getPropertyName();
            if (source instanceof JFormattedTextField) {
                Font font;
                Object newfont;
                if ("value".equals(name)) {
                    Object lastValue = spinner.getValue();
                    try {
                        spinner.setValue(this.getTextField().getValue());
                    }
                    catch (IllegalArgumentException iae) {
                        try {
                            ((JFormattedTextField)source).setValue(lastValue);
                        }
                        catch (IllegalArgumentException illegalArgumentException) {}
                    }
                } else if ("font".equals(name) && (newfont = e.getNewValue()) instanceof UIResource && !newfont.equals(font = spinner.getFont())) {
                    this.getTextField().setFont(font == null ? null : new FontUIResource(font));
                }
            }
        }

        @Override
        public void addLayoutComponent(String name, Component child) {
        }

        @Override
        public void removeLayoutComponent(Component child) {
        }

        private Dimension insetSize(Container parent) {
            Insets insets = parent.getInsets();
            int w = insets.left + insets.right;
            int h = insets.top + insets.bottom;
            return new Dimension(w, h);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension preferredSize = this.insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = this.getComponent(0).getPreferredSize();
                preferredSize.width += childSize.width;
                preferredSize.height += childSize.height;
            }
            return preferredSize;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Dimension minimumSize = this.insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = this.getComponent(0).getMinimumSize();
                minimumSize.width += childSize.width;
                minimumSize.height += childSize.height;
            }
            return minimumSize;
        }

        @Override
        public void layoutContainer(Container parent) {
            if (parent.getComponentCount() > 0) {
                Insets insets = parent.getInsets();
                int w = parent.getWidth() - (insets.left + insets.right);
                int h = parent.getHeight() - (insets.top + insets.bottom);
                this.getComponent(0).setBounds(insets.left, insets.top, w, h);
            }
        }

        public void commitEdit() throws ParseException {
            JFormattedTextField ftf = this.getTextField();
            ftf.commitEdit();
        }

        @Override
        public int getBaseline(int width, int height) {
            super.getBaseline(width, height);
            Insets insets = this.getInsets();
            width = width - insets.left - insets.right;
            height = height - insets.top - insets.bottom;
            int baseline = this.getComponent(0).getBaseline(width, height);
            if (baseline >= 0) {
                return baseline + insets.top;
            }
            return -1;
        }

        @Override
        public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
            return this.getComponent(0).getBaselineResizeBehavior();
        }
    }

    private class ModelListener
    implements ChangeListener,
    Serializable {
        private ModelListener() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner.this.fireStateChanged();
        }
    }

    protected class AccessibleJSpinner
    extends JComponent.AccessibleJComponent
    implements AccessibleValue,
    AccessibleAction,
    AccessibleText,
    AccessibleEditableText,
    ChangeListener {
        private Object oldModelValue = null;

        protected AccessibleJSpinner() {
            this.oldModelValue = JSpinner.this.model.getValue();
            JSpinner.this.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e == null) {
                throw new NullPointerException();
            }
            Object newModelValue = JSpinner.this.model.getValue();
            this.firePropertyChange("AccessibleValue", this.oldModelValue, newModelValue);
            this.firePropertyChange("AccessibleText", null, 0);
            this.oldModelValue = newModelValue;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SPIN_BOX;
        }

        @Override
        public int getAccessibleChildrenCount() {
            if (JSpinner.this.editor.getAccessibleContext() != null) {
                return 1;
            }
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            if (i != 0) {
                return null;
            }
            if (JSpinner.this.editor.getAccessibleContext() != null) {
                return (Accessible)((Object)JSpinner.this.editor);
            }
            return null;
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public AccessibleText getAccessibleText() {
            return this;
        }

        private AccessibleContext getEditorAccessibleContext() {
            if (JSpinner.this.editor instanceof DefaultEditor) {
                JFormattedTextField textField = ((DefaultEditor)JSpinner.this.editor).getTextField();
                if (textField != null) {
                    return textField.getAccessibleContext();
                }
            } else if (JSpinner.this.editor instanceof Accessible) {
                return JSpinner.this.editor.getAccessibleContext();
            }
            return null;
        }

        private AccessibleText getEditorAccessibleText() {
            AccessibleContext ac = this.getEditorAccessibleContext();
            if (ac != null) {
                return ac.getAccessibleText();
            }
            return null;
        }

        private AccessibleEditableText getEditorAccessibleEditableText() {
            AccessibleText at = this.getEditorAccessibleText();
            if (at instanceof AccessibleEditableText) {
                return (AccessibleEditableText)at;
            }
            return null;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            Object o = JSpinner.this.model.getValue();
            if (o instanceof Number) {
                return (Number)o;
            }
            return null;
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            try {
                JSpinner.this.model.setValue(n);
                return true;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return false;
            }
        }

        @Override
        public Number getMinimumAccessibleValue() {
            SpinnerNumberModel numberModel;
            Comparable<?> o;
            if (JSpinner.this.model instanceof SpinnerNumberModel && (o = (numberModel = (SpinnerNumberModel)JSpinner.this.model).getMinimum()) instanceof Number) {
                return (Number)((Object)o);
            }
            return null;
        }

        @Override
        public Number getMaximumAccessibleValue() {
            SpinnerNumberModel numberModel;
            Comparable<?> o;
            if (JSpinner.this.model instanceof SpinnerNumberModel && (o = (numberModel = (SpinnerNumberModel)JSpinner.this.model).getMaximum()) instanceof Number) {
                return (Number)((Object)o);
            }
            return null;
        }

        @Override
        public int getAccessibleActionCount() {
            return 2;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return AccessibleAction.INCREMENT;
            }
            if (i == 1) {
                return AccessibleAction.DECREMENT;
            }
            return null;
        }

        @Override
        public boolean doAccessibleAction(int i) {
            if (i < 0 || i > 1) {
                return false;
            }
            Object o = i == 0 ? JSpinner.this.getNextValue() : JSpinner.this.getPreviousValue();
            try {
                JSpinner.this.model.setValue(o);
                return true;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return false;
            }
        }

        private boolean sameWindowAncestor(Component src, Component dest) {
            if (src == null || dest == null) {
                return false;
            }
            return SwingUtilities.getWindowAncestor(src) == SwingUtilities.getWindowAncestor(dest);
        }

        @Override
        public int getIndexAtPoint(Point p) {
            Point editorPoint;
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null && this.sameWindowAncestor(JSpinner.this, JSpinner.this.editor) && (editorPoint = SwingUtilities.convertPoint(JSpinner.this, p, JSpinner.this.editor)) != null) {
                return at.getIndexAtPoint(editorPoint);
            }
            return -1;
        }

        @Override
        public Rectangle getCharacterBounds(int i) {
            Rectangle editorRect;
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null && (editorRect = at.getCharacterBounds(i)) != null && this.sameWindowAncestor(JSpinner.this, JSpinner.this.editor)) {
                return SwingUtilities.convertRectangle(JSpinner.this.editor, editorRect, JSpinner.this);
            }
            return null;
        }

        @Override
        public int getCharCount() {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getCharCount();
            }
            return -1;
        }

        @Override
        public int getCaretPosition() {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getCaretPosition();
            }
            return -1;
        }

        @Override
        public String getAtIndex(int part, int index) {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getAtIndex(part, index);
            }
            return null;
        }

        @Override
        public String getAfterIndex(int part, int index) {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getAfterIndex(part, index);
            }
            return null;
        }

        @Override
        public String getBeforeIndex(int part, int index) {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getBeforeIndex(part, index);
            }
            return null;
        }

        @Override
        public AttributeSet getCharacterAttribute(int i) {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getCharacterAttribute(i);
            }
            return null;
        }

        @Override
        public int getSelectionStart() {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getSelectionStart();
            }
            return -1;
        }

        @Override
        public int getSelectionEnd() {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getSelectionEnd();
            }
            return -1;
        }

        @Override
        public String getSelectedText() {
            AccessibleText at = this.getEditorAccessibleText();
            if (at != null) {
                return at.getSelectedText();
            }
            return null;
        }

        @Override
        public void setTextContents(String s) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.setTextContents(s);
            }
        }

        @Override
        public void insertTextAtIndex(int index, String s) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.insertTextAtIndex(index, s);
            }
        }

        @Override
        public String getTextRange(int startIndex, int endIndex) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                return at.getTextRange(startIndex, endIndex);
            }
            return null;
        }

        @Override
        public void delete(int startIndex, int endIndex) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.delete(startIndex, endIndex);
            }
        }

        @Override
        public void cut(int startIndex, int endIndex) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.cut(startIndex, endIndex);
            }
        }

        @Override
        public void paste(int startIndex) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.paste(startIndex);
            }
        }

        @Override
        public void replaceText(int startIndex, int endIndex, String s) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.replaceText(startIndex, endIndex, s);
            }
        }

        @Override
        public void selectText(int startIndex, int endIndex) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.selectText(startIndex, endIndex);
            }
        }

        @Override
        public void setAttributes(int startIndex, int endIndex, AttributeSet as) {
            AccessibleEditableText at = this.getEditorAccessibleEditableText();
            if (at != null) {
                at.setAttributes(startIndex, endIndex, as);
            }
        }
    }

    private static class DisabledAction
    implements Action {
        private DisabledAction() {
        }

        @Override
        public Object getValue(String key) {
            return null;
        }

        @Override
        public void putValue(String key, Object value) {
        }

        @Override
        public void setEnabled(boolean b) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
        }
    }

    private static class NumberEditorFormatter
    extends NumberFormatter {
        private final SpinnerNumberModel model;

        NumberEditorFormatter(SpinnerNumberModel model, NumberFormat format) {
            super(format);
            this.model = model;
            this.setValueClass(model.getValue().getClass());
        }

        @Override
        public void setMinimum(Comparable<?> min) {
            this.model.setMinimum(min);
        }

        @Override
        public Comparable<?> getMinimum() {
            return this.model.getMinimum();
        }

        @Override
        public void setMaximum(Comparable<?> max) {
            this.model.setMaximum(max);
        }

        @Override
        public Comparable<?> getMaximum() {
            return this.model.getMaximum();
        }
    }

    private static class DateEditorFormatter
    extends DateFormatter {
        private final SpinnerDateModel model;

        DateEditorFormatter(SpinnerDateModel model, DateFormat format) {
            super(format);
            this.model = model;
        }

        @Override
        public void setMinimum(Comparable<?> min) {
            this.model.setStart(min);
        }

        public Comparable<Date> getMinimum() {
            return this.model.getStart();
        }

        @Override
        public void setMaximum(Comparable<?> max) {
            this.model.setEnd(max);
        }

        public Comparable<Date> getMaximum() {
            return this.model.getEnd();
        }
    }
}

