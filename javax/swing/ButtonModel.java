/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.ItemSelectable;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;

public interface ButtonModel
extends ItemSelectable {
    public boolean isArmed();

    public boolean isSelected();

    public boolean isEnabled();

    public boolean isPressed();

    public boolean isRollover();

    public void setArmed(boolean var1);

    public void setSelected(boolean var1);

    public void setEnabled(boolean var1);

    public void setPressed(boolean var1);

    public void setRollover(boolean var1);

    public void setMnemonic(int var1);

    public int getMnemonic();

    public void setActionCommand(String var1);

    public String getActionCommand();

    public void setGroup(ButtonGroup var1);

    default public ButtonGroup getGroup() {
        return null;
    }

    public void addActionListener(ActionListener var1);

    public void removeActionListener(ActionListener var1);

    @Override
    public void addItemListener(ItemListener var1);

    @Override
    public void removeItemListener(ItemListener var1);

    public void addChangeListener(ChangeListener var1);

    public void removeChangeListener(ChangeListener var1);
}

