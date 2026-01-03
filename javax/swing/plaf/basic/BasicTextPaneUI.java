/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicEditorPaneUI;

public class BasicTextPaneUI
extends BasicEditorPaneUI {
    public static ComponentUI createUI(JComponent c) {
        return new BasicTextPaneUI();
    }

    @Override
    protected String getPropertyPrefix() {
        return "TextPane";
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
    }

    @Override
    protected void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
    }
}

