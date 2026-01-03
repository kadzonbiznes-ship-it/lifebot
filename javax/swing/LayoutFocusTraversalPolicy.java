/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.LayoutComparator;
import javax.swing.SortingFocusTraversalPolicy;
import javax.swing.SwingDefaultFocusTraversalPolicy;
import sun.awt.SunToolkit;

public class LayoutFocusTraversalPolicy
extends SortingFocusTraversalPolicy
implements Serializable {
    private static final SwingDefaultFocusTraversalPolicy fitnessTestPolicy = new SwingDefaultFocusTraversalPolicy();

    public LayoutFocusTraversalPolicy() {
        super(new LayoutComparator());
    }

    LayoutFocusTraversalPolicy(Comparator<? super Component> c) {
        super(c);
    }

    @Override
    public Component getComponentAfter(Container aContainer, Component aComponent) {
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        Comparator<? super Component> comparator = this.getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).setComponentOrientation(aContainer.getComponentOrientation());
        }
        return super.getComponentAfter(aContainer, aComponent);
    }

    @Override
    public Component getComponentBefore(Container aContainer, Component aComponent) {
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        Comparator<? super Component> comparator = this.getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).setComponentOrientation(aContainer.getComponentOrientation());
        }
        return super.getComponentBefore(aContainer, aComponent);
    }

    @Override
    public Component getFirstComponent(Container aContainer) {
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Comparator<? super Component> comparator = this.getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).setComponentOrientation(aContainer.getComponentOrientation());
        }
        return super.getFirstComponent(aContainer);
    }

    @Override
    public Component getLastComponent(Container aContainer) {
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Comparator<? super Component> comparator = this.getComparator();
        if (comparator instanceof LayoutComparator) {
            ((LayoutComparator)comparator).setComponentOrientation(aContainer.getComponentOrientation());
        }
        return super.getLastComponent(aContainer);
    }

    @Override
    protected boolean accept(Component aComponent) {
        if (!super.accept(aComponent)) {
            return false;
        }
        if (SunToolkit.isInstanceOf(aComponent, "javax.swing.JTable")) {
            return true;
        }
        if (SunToolkit.isInstanceOf(aComponent, "javax.swing.JComboBox")) {
            JComboBox box = (JComboBox)aComponent;
            return box.getUI().isFocusTraversable(box);
        }
        if (aComponent instanceof JComponent) {
            InputMap inputMap;
            ButtonGroup group;
            ButtonModel model;
            if (SunToolkit.isInstanceOf(aComponent, "javax.swing.JToggleButton") && (model = ((JToggleButton)aComponent).getModel()) != null && (group = model.getGroup()) != null) {
                Enumeration<AbstractButton> elements = group.getElements();
                int idx = 0;
                while (elements.hasMoreElements()) {
                    AbstractButton member = elements.nextElement();
                    if (!(member instanceof JToggleButton) || !member.isVisible() || !member.isDisplayable() || !member.isEnabled() || !member.isFocusable()) continue;
                    if (member == aComponent) {
                        return idx == 0;
                    }
                    ++idx;
                }
            }
            JComponent jComponent = (JComponent)aComponent;
            for (inputMap = jComponent.getInputMap(0, false); inputMap != null && inputMap.size() == 0; inputMap = inputMap.getParent()) {
            }
            if (inputMap != null) {
                return true;
            }
        }
        return fitnessTestPolicy.accept(aComponent);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.getComparator());
        out.writeBoolean(this.getImplicitDownCycleTraversal());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.setComparator((Comparator)in.readObject());
        this.setImplicitDownCycleTraversal(in.readBoolean());
    }
}

