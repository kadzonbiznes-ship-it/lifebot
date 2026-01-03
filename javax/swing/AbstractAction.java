/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessController;
import javax.swing.Action;
import javax.swing.ArrayTable;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.SwingPropertyChangeSupport;
import sun.security.action.GetPropertyAction;

public abstract class AbstractAction
implements Action,
Cloneable,
Serializable {
    private static Boolean RECONFIGURE_ON_NULL;
    protected boolean enabled = true;
    private transient ArrayTable arrayTable;
    protected SwingPropertyChangeSupport changeSupport;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean shouldReconfigure(PropertyChangeEvent e) {
        if (e.getPropertyName() == null) {
            Class<AbstractAction> clazz = AbstractAction.class;
            synchronized (AbstractAction.class) {
                if (RECONFIGURE_ON_NULL == null) {
                    RECONFIGURE_ON_NULL = Boolean.valueOf(AccessController.doPrivileged(new GetPropertyAction("swing.actions.reconfigureOnNull", "false")));
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return RECONFIGURE_ON_NULL;
            }
        }
        return false;
    }

    static void setEnabledFromAction(JComponent c, Action a) {
        c.setEnabled(a != null ? a.isEnabled() : true);
    }

    static void setToolTipTextFromAction(JComponent c, Action a) {
        c.setToolTipText(a != null ? (String)a.getValue("ShortDescription") : null);
    }

    static boolean hasSelectedKey(Action a) {
        return a != null && a.getValue("SwingSelectedKey") != null;
    }

    static boolean isSelected(Action a) {
        return Boolean.TRUE.equals(a.getValue("SwingSelectedKey"));
    }

    public AbstractAction() {
    }

    public AbstractAction(String name) {
        this.putValue("Name", name);
    }

    public AbstractAction(String name, Icon icon) {
        this(name);
        this.putValue("SmallIcon", icon);
    }

    @Override
    public Object getValue(String key) {
        if (key == "enabled") {
            return this.enabled;
        }
        if (this.arrayTable == null) {
            return null;
        }
        return this.arrayTable.get(key);
    }

    @Override
    public void putValue(String key, Object newValue) {
        Object oldValue = null;
        if (key == "enabled") {
            if (!(newValue instanceof Boolean)) {
                newValue = false;
            }
            oldValue = this.enabled;
            this.enabled = (Boolean)newValue;
        } else {
            if (this.arrayTable == null) {
                this.arrayTable = new ArrayTable();
            }
            if (this.arrayTable.containsKey(key)) {
                oldValue = this.arrayTable.get(key);
            }
            if (newValue == null) {
                this.arrayTable.remove(key);
            } else {
                this.arrayTable.put(key, newValue);
            }
        }
        this.firePropertyChange(key, oldValue, newValue);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean newValue) {
        boolean oldValue = this.enabled;
        if (oldValue != newValue) {
            this.enabled = newValue;
            this.firePropertyChange("enabled", oldValue, newValue);
        }
    }

    public Object[] getKeys() {
        if (this.arrayTable == null) {
            return null;
        }
        Object[] keys = new Object[this.arrayTable.size()];
        this.arrayTable.getKeys(keys);
        return keys;
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (this.changeSupport == null || oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }
        this.changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.changeSupport == null) {
            this.changeSupport = new SwingPropertyChangeSupport(this);
        }
        this.changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.changeSupport == null) {
            return;
        }
        this.changeSupport.removePropertyChangeListener(listener);
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (this.changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return this.changeSupport.getPropertyChangeListeners();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Object clone() throws CloneNotSupportedException {
        AbstractAction newAction = (AbstractAction)super.clone();
        AbstractAction abstractAction = this;
        synchronized (abstractAction) {
            if (this.arrayTable != null) {
                newAction.arrayTable = (ArrayTable)this.arrayTable.clone();
            }
        }
        return newAction;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        ArrayTable.writeArrayTable(s, this.arrayTable);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        for (int counter = s.readInt() - 1; counter >= 0; --counter) {
            this.putValue((String)s.readObject(), s.readObject());
        }
    }
}

