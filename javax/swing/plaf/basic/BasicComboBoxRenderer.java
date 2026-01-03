/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Dimension;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class BasicComboBoxRenderer
extends JLabel
implements ListCellRenderer<Object>,
Serializable {
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    public BasicComboBoxRenderer() {
        this.setOpaque(true);
        this.setBorder(BasicComboBoxRenderer.getNoFocusBorder());
    }

    private static Border getNoFocusBorder() {
        if (System.getSecurityManager() != null) {
            return SAFE_NO_FOCUS_BORDER;
        }
        return noFocusBorder;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size;
        if (this.getText() == null || this.getText().isEmpty()) {
            this.setText(" ");
            size = super.getPreferredSize();
            this.setText("");
        } else {
            size = super.getPreferredSize();
        }
        return size;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        } else {
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
        this.setFont(list.getFont());
        if (value instanceof Icon) {
            this.setIcon((Icon)value);
        } else {
            this.setText(value == null ? "" : value.toString());
        }
        return this;
    }

    public static class UIResource
    extends BasicComboBoxRenderer
    implements javax.swing.plaf.UIResource {
    }
}

