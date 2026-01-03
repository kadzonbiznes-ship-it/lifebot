/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class CardLayout
implements LayoutManager2,
Serializable {
    private static final long serialVersionUID = -4328196481005934313L;
    Vector<Card> vector = new Vector();
    int currentCard = 0;
    int hgap;
    int vgap;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("tab", Hashtable.class), new ObjectStreamField("hgap", Integer.TYPE), new ObjectStreamField("vgap", Integer.TYPE), new ObjectStreamField("vector", Vector.class), new ObjectStreamField("currentCard", Integer.TYPE)};

    public CardLayout() {
        this(0, 0);
    }

    public CardLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    public int getHgap() {
        return this.hgap;
    }

    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    public int getVgap() {
        return this.vgap;
    }

    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        Object object = comp.getTreeLock();
        synchronized (object) {
            if (constraints == null) {
                constraints = "";
            }
            if (!(constraints instanceof String)) {
                throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
            }
            this.addLayoutComponent((String)constraints, comp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public void addLayoutComponent(String name, Component comp) {
        Object object = comp.getTreeLock();
        synchronized (object) {
            if (!this.vector.isEmpty()) {
                comp.setVisible(false);
            }
            for (int i = 0; i < this.vector.size(); ++i) {
                if (!this.vector.get((int)i).name.equals(name)) continue;
                this.vector.get((int)i).comp = comp;
                return;
            }
            this.vector.add(new Card(name, comp));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeLayoutComponent(Component comp) {
        Object object = comp.getTreeLock();
        synchronized (object) {
            for (int i = 0; i < this.vector.size(); ++i) {
                if (this.vector.get((int)i).comp != comp) continue;
                if (comp.isVisible() && comp.getParent() != null) {
                    this.next(comp.getParent());
                }
                this.vector.remove(i);
                if (this.currentCard <= i) break;
                --this.currentCard;
                break;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int w = 0;
            int h = 0;
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (d.width > w) {
                    w = d.width;
                }
                if (d.height <= h) continue;
                h = d.height;
            }
            return new Dimension(insets.left + insets.right + w + this.hgap * 2, insets.top + insets.bottom + h + this.vgap * 2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int w = 0;
            int h = 0;
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getMinimumSize();
                if (d.width > w) {
                    w = d.width;
                }
                if (d.height <= h) continue;
                h = d.height;
            }
            return new Dimension(insets.left + insets.right + w + this.hgap * 2, insets.top + insets.bottom + h + this.vgap * 2);
        }
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container parent) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container parent) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void layoutContainer(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            Component comp = null;
            boolean currentFound = false;
            for (int i = 0; i < ncomponents; ++i) {
                comp = parent.getComponent(i);
                comp.setBounds(this.hgap + insets.left, this.vgap + insets.top, parent.width - (this.hgap * 2 + insets.left + insets.right), parent.height - (this.vgap * 2 + insets.top + insets.bottom));
                if (!comp.isVisible()) continue;
                currentFound = true;
            }
            if (!currentFound && ncomponents > 0) {
                parent.getComponent(0).setVisible(true);
            }
        }
    }

    void checkLayout(Container parent) {
        if (parent.getLayout() != this) {
            throw new IllegalArgumentException("wrong parent for CardLayout");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void first(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            this.checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                if (!comp.isVisible()) continue;
                comp.setVisible(false);
                break;
            }
            if (ncomponents > 0) {
                this.currentCard = 0;
                parent.getComponent(0).setVisible(true);
                parent.validate();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void next(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            this.checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                if (!comp.isVisible()) continue;
                comp.setVisible(false);
                this.currentCard = (i + 1) % ncomponents;
                comp = parent.getComponent(this.currentCard);
                comp.setVisible(true);
                parent.validate();
                return;
            }
            this.showDefaultComponent(parent);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void previous(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            this.checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                if (!comp.isVisible()) continue;
                comp.setVisible(false);
                this.currentCard = i > 0 ? i - 1 : ncomponents - 1;
                comp = parent.getComponent(this.currentCard);
                comp.setVisible(true);
                parent.validate();
                return;
            }
            this.showDefaultComponent(parent);
        }
    }

    void showDefaultComponent(Container parent) {
        if (parent.getComponentCount() > 0) {
            this.currentCard = 0;
            parent.getComponent(0).setVisible(true);
            parent.validate();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void last(Container parent) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            this.checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                if (!comp.isVisible()) continue;
                comp.setVisible(false);
                break;
            }
            if (ncomponents > 0) {
                this.currentCard = ncomponents - 1;
                parent.getComponent(this.currentCard).setVisible(true);
                parent.validate();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void show(Container parent, String name) {
        Object object = parent.getTreeLock();
        synchronized (object) {
            int i;
            this.checkLayout(parent);
            Component next = null;
            int ncomponents = this.vector.size();
            for (i = 0; i < ncomponents; ++i) {
                Card card = this.vector.get(i);
                if (!card.name.equals(name)) continue;
                next = card.comp;
                this.currentCard = i;
                break;
            }
            if (next != null && !next.isVisible()) {
                ncomponents = parent.getComponentCount();
                for (i = 0; i < ncomponents; ++i) {
                    Component comp = parent.getComponent(i);
                    if (!comp.isVisible()) continue;
                    comp.setVisible(false);
                    break;
                }
                next.setVisible(true);
                parent.validate();
            }
        }
    }

    public String toString() {
        return this.getClass().getName() + "[hgap=" + this.hgap + ",vgap=" + this.vgap + "]";
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField f = s.readFields();
        this.hgap = f.get("hgap", 0);
        this.vgap = f.get("vgap", 0);
        if (f.defaulted("vector")) {
            Hashtable tab = (Hashtable)f.get("tab", null);
            this.vector = new Vector();
            if (tab != null && !tab.isEmpty()) {
                Enumeration e = tab.keys();
                while (e.hasMoreElements()) {
                    String key = (String)e.nextElement();
                    Component comp = (Component)tab.get(key);
                    this.vector.add(new Card(key, comp));
                    if (!comp.isVisible()) continue;
                    this.currentCard = this.vector.size() - 1;
                }
            }
        } else {
            this.vector = (Vector)f.get("vector", null);
            this.currentCard = f.get("currentCard", 0);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Hashtable<String, Component> tab = new Hashtable<String, Component>();
        int ncomponents = this.vector.size();
        for (int i = 0; i < ncomponents; ++i) {
            Card card = this.vector.get(i);
            tab.put(card.name, card.comp);
        }
        ObjectOutputStream.PutField f = s.putFields();
        f.put("hgap", this.hgap);
        f.put("vgap", this.vgap);
        f.put("vector", this.vector);
        f.put("currentCard", this.currentCard);
        f.put("tab", tab);
        s.writeFields();
    }

    static class Card
    implements Serializable {
        private static final long serialVersionUID = 6640330810709497518L;
        public String name;
        public Component comp;

        public Card(String cardName, Component cardComponent) {
            this.name = cardName;
            this.comp = cardComponent;
        }
    }
}

