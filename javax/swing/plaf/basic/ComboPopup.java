/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JList;

public interface ComboPopup {
    public void show();

    public void hide();

    public boolean isVisible();

    public JList<Object> getList();

    public MouseListener getMouseListener();

    public MouseMotionListener getMouseMotionListener();

    public KeyListener getKeyListener();

    public void uninstallingUI();
}

