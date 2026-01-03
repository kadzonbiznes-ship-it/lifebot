/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;

public interface TableColumnModelListener
extends EventListener {
    public void columnAdded(TableColumnModelEvent var1);

    public void columnRemoved(TableColumnModelEvent var1);

    public void columnMoved(TableColumnModelEvent var1);

    public void columnMarginChanged(ChangeEvent var1);

    public void columnSelectionChanged(ListSelectionEvent var1);
}

