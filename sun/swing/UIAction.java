/*
 * Decompiled with CFR 0.152.
 */
package sun.swing;

import java.beans.PropertyChangeListener;
import javax.swing.Action;

public abstract class UIAction
implements Action {
    private String name;

    public UIAction(String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    @Override
    public Object getValue(String key) {
        if (key == "Name") {
            return this.name;
        }
        return null;
    }

    @Override
    public void putValue(String key, Object value) {
    }

    @Override
    public void setEnabled(boolean b) {
    }

    @Override
    public final boolean isEnabled() {
        return this.accept(null);
    }

    @Override
    public boolean accept(Object sender) {
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}

