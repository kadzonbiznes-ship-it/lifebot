/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class ComponentInputMap
extends InputMap {
    private JComponent component;

    public ComponentInputMap(JComponent component) {
        this.component = component;
        if (component == null) {
            throw new IllegalArgumentException("ComponentInputMaps must be associated with a non-null JComponent");
        }
    }

    @Override
    public void setParent(InputMap map) {
        if (this.getParent() == map) {
            return;
        }
        if (!(map == null || map instanceof ComponentInputMap && ((ComponentInputMap)map).getComponent() == this.getComponent())) {
            throw new IllegalArgumentException("ComponentInputMaps must have a parent ComponentInputMap associated with the same component");
        }
        super.setParent(map);
        this.getComponent().componentInputMapChanged(this);
    }

    public JComponent getComponent() {
        return this.component;
    }

    @Override
    public void put(KeyStroke keyStroke, Object actionMapKey) {
        super.put(keyStroke, actionMapKey);
        if (this.getComponent() != null) {
            this.getComponent().componentInputMapChanged(this);
        }
    }

    @Override
    public void remove(KeyStroke key) {
        super.remove(key);
        if (this.getComponent() != null) {
            this.getComponent().componentInputMapChanged(this);
        }
    }

    @Override
    public void clear() {
        int oldSize = this.size();
        super.clear();
        if (oldSize > 0 && this.getComponent() != null) {
            this.getComponent().componentInputMapChanged(this);
        }
    }
}

