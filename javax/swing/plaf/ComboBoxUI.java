/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf;

import javax.swing.JComboBox;
import javax.swing.plaf.ComponentUI;

public abstract class ComboBoxUI
extends ComponentUI {
    protected ComboBoxUI() {
    }

    public abstract void setPopupVisible(JComboBox<?> var1, boolean var2);

    public abstract boolean isPopupVisible(JComboBox<?> var1);

    public abstract boolean isFocusTraversable(JComboBox<?> var1);
}

