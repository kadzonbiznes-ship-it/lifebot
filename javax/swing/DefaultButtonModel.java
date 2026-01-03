/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.EventListener;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class DefaultButtonModel
implements ButtonModel,
Serializable {
    protected int stateMask = 0;
    protected String actionCommand = null;
    protected ButtonGroup group = null;
    protected int mnemonic = 0;
    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();
    private boolean menuItem = false;
    public static final int ARMED = 1;
    public static final int SELECTED = 2;
    public static final int PRESSED = 4;
    public static final int ENABLED = 8;
    public static final int ROLLOVER = 16;

    public DefaultButtonModel() {
        this.setEnabled(true);
    }

    @Override
    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

    @Override
    public String getActionCommand() {
        return this.actionCommand;
    }

    @Override
    public boolean isArmed() {
        return (this.stateMask & 1) != 0;
    }

    @Override
    public boolean isSelected() {
        return (this.stateMask & 2) != 0;
    }

    @Override
    public boolean isEnabled() {
        return (this.stateMask & 8) != 0;
    }

    @Override
    public boolean isPressed() {
        return (this.stateMask & 4) != 0;
    }

    @Override
    public boolean isRollover() {
        return (this.stateMask & 0x10) != 0;
    }

    @Override
    public void setArmed(boolean b) {
        if (this.isMenuItem() && UIManager.getBoolean("MenuItem.disabledAreNavigable") ? this.isArmed() == b : this.isArmed() == b || !this.isEnabled()) {
            return;
        }
        this.stateMask = b ? (this.stateMask |= 1) : (this.stateMask &= 0xFFFFFFFE);
        this.fireStateChanged();
    }

    @Override
    public void setEnabled(boolean b) {
        if (this.isEnabled() == b) {
            return;
        }
        if (b) {
            this.stateMask |= 8;
        } else {
            this.stateMask &= 0xFFFFFFF7;
            this.stateMask &= 0xFFFFFFFE;
            this.stateMask &= 0xFFFFFFFB;
        }
        this.fireStateChanged();
    }

    @Override
    public void setSelected(boolean b) {
        if (this.isSelected() == b) {
            return;
        }
        this.stateMask = b ? (this.stateMask |= 2) : (this.stateMask &= 0xFFFFFFFD);
        this.fireItemStateChanged(new ItemEvent(this, 701, this, b ? 1 : 2));
        this.fireStateChanged();
    }

    @Override
    public void setPressed(boolean b) {
        if (this.isPressed() == b || !this.isEnabled()) {
            return;
        }
        this.stateMask = b ? (this.stateMask |= 4) : (this.stateMask &= 0xFFFFFFFB);
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
        this.fireStateChanged();
    }

    @Override
    public void setRollover(boolean b) {
        if (this.isRollover() == b || !this.isEnabled()) {
            return;
        }
        this.stateMask = b ? (this.stateMask |= 0x10) : (this.stateMask &= 0xFFFFFFEF);
        this.fireStateChanged();
    }

    @Override
    public void setMnemonic(int key) {
        if (this.mnemonic != key) {
            this.mnemonic = key;
            this.fireStateChanged();
        }
    }

    @Override
    public int getMnemonic() {
        return this.mnemonic;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

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

    @Override
    public void addActionListener(ActionListener l) {
        this.listenerList.add(ActionListener.class, l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        this.listenerList.remove(ActionListener.class, l);
    }

    public ActionListener[] getActionListeners() {
        return (ActionListener[])this.listenerList.getListeners(ActionListener.class);
    }

    protected void fireActionPerformed(ActionEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ActionListener.class) continue;
            ((ActionListener)listeners[i + 1]).actionPerformed(e);
        }
    }

    @Override
    public void addItemListener(ItemListener l) {
        this.listenerList.add(ItemListener.class, l);
    }

    @Override
    public void removeItemListener(ItemListener l) {
        this.listenerList.remove(ItemListener.class, l);
    }

    public ItemListener[] getItemListeners() {
        return (ItemListener[])this.listenerList.getListeners(ItemListener.class);
    }

    protected void fireItemStateChanged(ItemEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ItemListener.class) continue;
            ((ItemListener)listeners[i + 1]).itemStateChanged(e);
        }
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return this.listenerList.getListeners(listenerType);
    }

    @Override
    public Object[] getSelectedObjects() {
        return null;
    }

    @Override
    public void setGroup(ButtonGroup group) {
        this.group = group;
    }

    @Override
    public ButtonGroup getGroup() {
        return this.group;
    }

    boolean isMenuItem() {
        return this.menuItem;
    }

    void setMenuItem(boolean menuItem) {
        this.menuItem = menuItem;
    }
}

