/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import javax.swing.ComboBoxEditor;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;
import sun.reflect.misc.MethodUtil;

public class BasicComboBoxEditor
implements ComboBoxEditor,
FocusListener {
    protected JTextField editor = this.createEditorComponent();
    private Object oldValue;

    @Override
    public Component getEditorComponent() {
        return this.editor;
    }

    protected JTextField createEditorComponent() {
        BorderlessTextField editor = new BorderlessTextField("", 9);
        ((JComponent)editor).setBorder(null);
        return editor;
    }

    @Override
    public void setItem(Object anObject) {
        String text;
        if (anObject != null) {
            text = anObject.toString();
            if (text == null) {
                text = "";
            }
            this.oldValue = anObject;
        } else {
            text = "";
        }
        if (!text.equals(this.editor.getText())) {
            this.editor.setText(text);
        }
    }

    @Override
    public Object getItem() {
        Object newValue = this.editor.getText();
        if (this.oldValue != null && !(this.oldValue instanceof String)) {
            if (newValue.equals(this.oldValue.toString())) {
                return this.oldValue;
            }
            Class<?> cls = this.oldValue.getClass();
            try {
                Method method = MethodUtil.getMethod(cls, "valueOf", new Class[]{String.class});
                newValue = MethodUtil.invoke(method, this.oldValue, new Object[]{this.editor.getText()});
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return newValue;
    }

    @Override
    public void selectAll() {
        this.editor.selectAll();
        this.editor.requestFocus();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public void addActionListener(ActionListener l) {
        this.editor.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        this.editor.removeActionListener(l);
    }

    static class BorderlessTextField
    extends JTextField {
        public BorderlessTextField(String value, int n) {
            super(value, n);
        }

        @Override
        public void setText(String s) {
            if (this.getText().equals(s)) {
                return;
            }
            super.setText(s);
        }

        @Override
        public void setBorder(Border b) {
            if (!(b instanceof UIResource)) {
                super.setBorder(b);
            }
        }
    }

    public static class UIResource
    extends BasicComboBoxEditor
    implements javax.swing.plaf.UIResource {
    }
}

