/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;

public abstract class TextAction
extends AbstractAction {
    public TextAction(String name) {
        super(name);
    }

    protected final JTextComponent getTextComponent(ActionEvent e) {
        Object o;
        if (e != null && (o = e.getSource()) instanceof JTextComponent) {
            return (JTextComponent)o;
        }
        return this.getFocusedComponent();
    }

    public static final Action[] augmentList(Action[] list1, Action[] list2) {
        String value;
        Hashtable<String, Action> h = new Hashtable<String, Action>();
        for (Action a : list1) {
            value = (String)a.getValue("Name");
            h.put(value != null ? value : "", a);
        }
        for (Action a : list2) {
            value = (String)a.getValue("Name");
            h.put(value != null ? value : "", a);
        }
        Action[] actions = new Action[h.size()];
        int index = 0;
        Enumeration e = h.elements();
        while (e.hasMoreElements()) {
            actions[index++] = (Action)e.nextElement();
        }
        return actions;
    }

    protected final JTextComponent getFocusedComponent() {
        return JTextComponent.getFocusedComponent();
    }
}

