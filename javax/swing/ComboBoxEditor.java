/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.event.ActionListener;

public interface ComboBoxEditor {
    public Component getEditorComponent();

    public void setItem(Object var1);

    public Object getItem();

    public void selectAll();

    public void addActionListener(ActionListener var1);

    public void removeActionListener(ActionListener var1);
}

