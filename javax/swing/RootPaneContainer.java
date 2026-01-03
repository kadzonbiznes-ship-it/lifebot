/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;

public interface RootPaneContainer {
    public JRootPane getRootPane();

    public void setContentPane(Container var1);

    public Container getContentPane();

    public void setLayeredPane(JLayeredPane var1);

    public JLayeredPane getLayeredPane();

    public void setGlassPane(Component var1);

    public Component getGlassPane();
}

