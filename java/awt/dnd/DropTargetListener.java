/*
 * Decompiled with CFR 0.152.
 */
package java.awt.dnd;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.EventListener;

public interface DropTargetListener
extends EventListener {
    public void dragEnter(DropTargetDragEvent var1);

    public void dragOver(DropTargetDragEvent var1);

    public void dropActionChanged(DropTargetDragEvent var1);

    public void dragExit(DropTargetEvent var1);

    public void drop(DropTargetDropEvent var1);
}

