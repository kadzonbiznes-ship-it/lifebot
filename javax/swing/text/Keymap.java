/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import javax.swing.Action;
import javax.swing.KeyStroke;

public interface Keymap {
    public String getName();

    public Action getDefaultAction();

    public void setDefaultAction(Action var1);

    public Action getAction(KeyStroke var1);

    public KeyStroke[] getBoundKeyStrokes();

    public Action[] getBoundActions();

    public KeyStroke[] getKeyStrokesForAction(Action var1);

    public boolean isLocallyDefined(KeyStroke var1);

    public void addActionForKeyStroke(KeyStroke var1, Action var2);

    public void removeKeyStrokeBinding(KeyStroke var1);

    public void removeBindings();

    public Keymap getResolveParent();

    public void setResolveParent(Keymap var1);
}

