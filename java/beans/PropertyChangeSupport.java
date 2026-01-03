/*
 * Decompiled with CFR 0.152.
 */
package java.beans;

import java.beans.ChangeListenerMap;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

public class PropertyChangeSupport
implements Serializable {
    private PropertyChangeListenerMap map = new PropertyChangeListenerMap();
    private Object source;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("children", Hashtable.class), new ObjectStreamField("source", Object.class), new ObjectStreamField("propertyChangeSupportSerializedDataVersion", Integer.TYPE)};
    private static final long serialVersionUID = 6401253773779951803L;

    public PropertyChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        this.source = sourceBean;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof PropertyChangeListenerProxy) {
            PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
            this.addPropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener)proxy.getListener());
        } else {
            this.map.add(null, listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof PropertyChangeListenerProxy) {
            PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
            this.removePropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener)proxy.getListener());
        } else {
            this.map.remove(null, listener);
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return (PropertyChangeListener[])this.map.getListeners();
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        if ((listener = this.map.extract(listener)) != null) {
            this.map.add(propertyName, listener);
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        if ((listener = this.map.extract(listener)) != null) {
            this.map.remove(propertyName, listener);
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return (PropertyChangeListener[])this.map.getListeners(propertyName);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            this.firePropertyChange(new PropertyChangeEvent(this.source, propertyName, oldValue, newValue));
        }
    }

    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        if (oldValue != newValue) {
            this.firePropertyChange(propertyName, (Object)oldValue, (Object)newValue);
        }
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            this.firePropertyChange(propertyName, (Object)oldValue, (Object)newValue);
        }
    }

    public void firePropertyChange(PropertyChangeEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            String name = event.getPropertyName();
            PropertyChangeListener[] common = (PropertyChangeListener[])this.map.get(null);
            PropertyChangeListener[] named = name != null ? (PropertyChangeListener[])this.map.get(name) : null;
            PropertyChangeSupport.fire(common, event);
            PropertyChangeSupport.fire(named, event);
        }
    }

    private static void fire(PropertyChangeListener[] listeners, PropertyChangeEvent event) {
        if (listeners != null) {
            for (PropertyChangeListener listener : listeners) {
                listener.propertyChange(event);
            }
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            this.firePropertyChange(new IndexedPropertyChangeEvent(this.source, propertyName, oldValue, newValue, index));
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        if (oldValue != newValue) {
            this.fireIndexedPropertyChange(propertyName, index, (Object)oldValue, (Object)newValue);
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            this.fireIndexedPropertyChange(propertyName, index, (Object)oldValue, (Object)newValue);
        }
    }

    public boolean hasListeners(String propertyName) {
        return this.map.hasListeners(propertyName);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Hashtable<String, PropertyChangeSupport> children = null;
        PropertyChangeListener[] listeners = null;
        PropertyChangeListenerMap propertyChangeListenerMap = this.map;
        synchronized (propertyChangeListenerMap) {
            for (Map.Entry<String, L[]> entry : this.map.getEntries()) {
                String property = entry.getKey();
                if (property == null) {
                    listeners = (PropertyChangeListener[])entry.getValue();
                    continue;
                }
                if (children == null) {
                    children = new Hashtable<String, PropertyChangeSupport>();
                }
                PropertyChangeSupport pcs = new PropertyChangeSupport(this.source);
                pcs.map.set(null, (PropertyChangeListener[])entry.getValue());
                children.put(property, pcs);
            }
        }
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("children", children);
        fields.put("source", this.source);
        fields.put("propertyChangeSupportSerializedDataVersion", 2);
        s.writeFields();
        if (listeners != null) {
            for (PropertyChangeListener l : listeners) {
                if (!(l instanceof Serializable)) continue;
                s.writeObject(l);
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        Object listenerOrNull;
        this.map = new PropertyChangeListenerMap();
        ObjectInputStream.GetField fields = s.readFields();
        Hashtable children = (Hashtable)fields.get("children", null);
        this.source = fields.get("source", null);
        fields.get("propertyChangeSupportSerializedDataVersion", 2);
        while (null != (listenerOrNull = s.readObject())) {
            this.map.add(null, (PropertyChangeListener)listenerOrNull);
        }
        if (children != null) {
            for (Map.Entry entry : children.entrySet()) {
                for (PropertyChangeListener listener : ((PropertyChangeSupport)entry.getValue()).getPropertyChangeListeners()) {
                    this.map.add((String)entry.getKey(), listener);
                }
            }
        }
    }

    private static final class PropertyChangeListenerMap
    extends ChangeListenerMap<PropertyChangeListener> {
        private static final PropertyChangeListener[] EMPTY = new PropertyChangeListener[0];

        private PropertyChangeListenerMap() {
        }

        protected PropertyChangeListener[] newArray(int length) {
            return 0 < length ? new PropertyChangeListener[length] : EMPTY;
        }

        @Override
        protected PropertyChangeListener newProxy(String name, PropertyChangeListener listener) {
            return new PropertyChangeListenerProxy(name, listener);
        }

        @Override
        public PropertyChangeListener extract(PropertyChangeListener listener) {
            while (listener instanceof PropertyChangeListenerProxy) {
                listener = (PropertyChangeListener)((PropertyChangeListenerProxy)listener).getListener();
            }
            return listener;
        }
    }
}

