/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.synth.SynthListUI;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;

public class DefaultListCellRenderer
extends JLabel
implements ListCellRenderer<Object>,
Serializable {
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER;
    protected static Border noFocusBorder;

    public DefaultListCellRenderer() {
        this.setOpaque(true);
        this.setBorder(this.getNoFocusBorder());
        this.setName("List.cellRenderer");
    }

    private Border getNoFocusBorder() {
        Border border = DefaultLookup.getBorder(this, this.ui, "List.cellNoFocusBorder");
        if (System.getSecurityManager() != null) {
            if (border != null) {
                return border;
            }
            return SAFE_NO_FOCUS_BORDER;
        }
        if (border != null && (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
            return border;
        }
        return noFocusBorder;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        this.setComponentOrientation(list.getComponentOrientation());
        Color bg = null;
        Color fg = null;
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {
            bg = DefaultLookup.getColor(this, this.ui, "List.dropCellBackground");
            fg = DefaultLookup.getColor(this, this.ui, "List.dropCellForeground");
            isSelected = true;
        }
        if (isSelected) {
            this.setBackground(bg == null ? list.getSelectionBackground() : bg);
            this.setForeground(fg == null ? list.getSelectionForeground() : fg);
        } else {
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
        if (value instanceof Icon) {
            this.setIcon((Icon)value);
            this.setText("");
        } else {
            this.setIcon(null);
            this.setText(value == null ? "" : value.toString());
        }
        if (list.getName() == null || !list.getName().equals("ComboBox.list") || !(list.getUI() instanceof SynthListUI)) {
            this.setEnabled(list.isEnabled());
        }
        this.setFont(list.getFont());
        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = DefaultLookup.getBorder(this, this.ui, "List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = DefaultLookup.getBorder(this, this.ui, "List.focusCellHighlightBorder");
            }
        } else {
            border = this.getNoFocusBorder();
        }
        this.setBorder(border);
        return this;
    }

    @Override
    public boolean isOpaque() {
        Color back = this.getBackground();
        Container p = this.getParent();
        if (p != null) {
            p = p.getParent();
        }
        boolean colorMatch = back != null && p != null && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }

    @Override
    public void validate() {
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void repaint() {
    }

    @Override
    public void revalidate() {
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    @Override
    public void repaint(Rectangle r) {
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (propertyName == "text" || (SwingUtilities2.isScaleChanged(propertyName, oldValue, newValue) || propertyName == "font" || propertyName == "foreground") && oldValue != newValue && this.getClientProperty("html") != null) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    static {
        noFocusBorder = DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    }

    public static class UIResource
    extends DefaultListCellRenderer
    implements javax.swing.plaf.UIResource {
    }
}

