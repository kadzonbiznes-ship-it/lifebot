/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import javax.swing.event.ChangeListener;
import javax.swing.text.MutableAttributeSet;

public interface Style
extends MutableAttributeSet {
    public String getName();

    public void addChangeListener(ChangeListener var1);

    public void removeChangeListener(ChangeListener var1);
}

