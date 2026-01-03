/*
 * Decompiled with CFR 0.152.
 */
package java.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListenerProxy;

public class PropertyChangeListenerProxy
extends EventListenerProxy<PropertyChangeListener>
implements PropertyChangeListener {
    private final String propertyName;

    public PropertyChangeListenerProxy(String propertyName, PropertyChangeListener listener) {
        super(listener);
        this.propertyName = propertyName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        ((PropertyChangeListener)this.getListener()).propertyChange(event);
    }

    public String getPropertyName() {
        return this.propertyName;
    }
}

