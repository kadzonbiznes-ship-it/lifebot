/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.dnd;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.SortedMap;
import sun.awt.SunToolkit;
import sun.awt.datatransfer.DataTransferer;
import sun.awt.dnd.SunDropTargetContextPeer;
import sun.awt.dnd.SunDropTargetEvent;

public abstract class SunDragSourceContextPeer
implements DragSourceContextPeer {
    private DragGestureEvent trigger;
    private Component component;
    private Cursor cursor;
    private Image dragImage;
    private Point dragImageOffset;
    private long nativeCtxt;
    private DragSourceContext dragSourceContext;
    private int sourceActions;
    private static volatile boolean dragDropInProgress;
    private static volatile boolean discardingMouseEvents;
    protected static final int DISPATCH_ENTER = 1;
    protected static final int DISPATCH_MOTION = 2;
    protected static final int DISPATCH_CHANGED = 3;
    protected static final int DISPATCH_EXIT = 4;
    protected static final int DISPATCH_FINISH = 5;
    protected static final int DISPATCH_MOUSE_MOVED = 6;

    public SunDragSourceContextPeer(DragGestureEvent dge) {
        this.trigger = dge;
        this.component = this.trigger != null ? this.trigger.getComponent() : null;
    }

    public void startSecondaryEventLoop() {
    }

    public void quitSecondaryEventLoop() {
    }

    @Override
    public void startDrag(DragSourceContext dsc, Cursor c, Image di, Point p) throws InvalidDnDOperationException {
        if (this.getTrigger().getTriggerEvent() == null) {
            throw new InvalidDnDOperationException("DragGestureEvent has a null trigger");
        }
        this.dragSourceContext = dsc;
        this.cursor = c;
        this.sourceActions = this.getDragSourceContext().getSourceActions();
        this.dragImage = di;
        this.dragImageOffset = p;
        Transferable transferable = this.getDragSourceContext().getTransferable();
        SortedMap<Long, DataFlavor> formatMap = DataTransferer.getInstance().getFormatsForTransferable(transferable, DataTransferer.adaptFlavorMap(this.getTrigger().getDragSource().getFlavorMap()));
        long[] formats = DataTransferer.keysToLongArray(formatMap);
        this.startDrag(transferable, formats, formatMap);
        discardingMouseEvents = true;
        EventQueue.invokeLater(new Runnable(this){

            @Override
            public void run() {
                discardingMouseEvents = false;
            }
        });
    }

    protected abstract void startDrag(Transferable var1, long[] var2, Map<Long, DataFlavor> var3);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCursor(Cursor c) throws InvalidDnDOperationException {
        SunDragSourceContextPeer sunDragSourceContextPeer = this;
        synchronized (sunDragSourceContextPeer) {
            if (this.cursor == null || !this.cursor.equals(c)) {
                this.cursor = c;
                this.setNativeCursor(this.getNativeContext(), c, c != null ? c.getType() : 0);
            }
        }
    }

    @Override
    public Cursor getCursor() {
        return this.cursor;
    }

    public Image getDragImage() {
        return this.dragImage;
    }

    public Point getDragImageOffset() {
        if (this.dragImageOffset == null) {
            return new Point(0, 0);
        }
        return new Point(this.dragImageOffset);
    }

    protected abstract void setNativeCursor(long var1, Cursor var3, int var4);

    protected synchronized void setTrigger(DragGestureEvent dge) {
        this.trigger = dge;
        this.component = this.trigger != null ? this.trigger.getComponent() : null;
    }

    protected DragGestureEvent getTrigger() {
        return this.trigger;
    }

    protected Component getComponent() {
        return this.component;
    }

    protected synchronized void setNativeContext(long ctxt) {
        this.nativeCtxt = ctxt;
    }

    protected synchronized long getNativeContext() {
        return this.nativeCtxt;
    }

    protected DragSourceContext getDragSourceContext() {
        return this.dragSourceContext;
    }

    @Override
    public void transferablesFlavorsChanged() {
    }

    protected final void postDragSourceDragEvent(int targetAction, int modifiers, int x, int y, int dispatchType) {
        int dropAction = SunDragSourceContextPeer.convertModifiersToDropAction(modifiers, this.sourceActions);
        DragSourceDragEvent event = new DragSourceDragEvent(this.getDragSourceContext(), dropAction, targetAction & this.sourceActions, modifiers, x, y);
        EventDispatcher dispatcher = new EventDispatcher(dispatchType, event);
        SunToolkit.invokeLaterOnAppContext(SunToolkit.targetToAppContext(this.getComponent()), dispatcher);
        this.startSecondaryEventLoop();
    }

    protected void dragEnter(int targetActions, int modifiers, int x, int y) {
        this.postDragSourceDragEvent(targetActions, modifiers, x, y, 1);
    }

    private void dragMotion(int targetActions, int modifiers, int x, int y) {
        this.postDragSourceDragEvent(targetActions, modifiers, x, y, 2);
    }

    private void operationChanged(int targetActions, int modifiers, int x, int y) {
        this.postDragSourceDragEvent(targetActions, modifiers, x, y, 3);
    }

    protected final void dragExit(int x, int y) {
        DragSourceEvent event = new DragSourceEvent(this.getDragSourceContext(), x, y);
        EventDispatcher dispatcher = new EventDispatcher(4, event);
        SunToolkit.invokeLaterOnAppContext(SunToolkit.targetToAppContext(this.getComponent()), dispatcher);
        this.startSecondaryEventLoop();
    }

    private void dragMouseMoved(int targetActions, int modifiers, int x, int y) {
        this.postDragSourceDragEvent(targetActions, modifiers, x, y, 6);
    }

    protected final void dragDropFinished(boolean success, int operations, int x, int y) {
        DragSourceDropEvent event = new DragSourceDropEvent(this.getDragSourceContext(), operations & this.sourceActions, success, x, y);
        EventDispatcher dispatcher = new EventDispatcher(5, event);
        SunToolkit.invokeLaterOnAppContext(SunToolkit.targetToAppContext(this.getComponent()), dispatcher);
        this.startSecondaryEventLoop();
        this.setNativeContext(0L);
        this.dragImage = null;
        this.dragImageOffset = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setDragDropInProgress(boolean b) throws InvalidDnDOperationException {
        Class<SunDragSourceContextPeer> clazz = SunDragSourceContextPeer.class;
        synchronized (SunDragSourceContextPeer.class) {
            if (dragDropInProgress == b) {
                throw new InvalidDnDOperationException(SunDragSourceContextPeer.getExceptionMessage(b));
            }
            dragDropInProgress = b;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    public static boolean checkEvent(AWTEvent event) {
        MouseEvent mouseEvent;
        return !discardingMouseEvents || !(event instanceof MouseEvent) || (mouseEvent = (MouseEvent)event) instanceof SunDropTargetEvent;
    }

    public static void checkDragDropInProgress() throws InvalidDnDOperationException {
        if (dragDropInProgress) {
            throw new InvalidDnDOperationException(SunDragSourceContextPeer.getExceptionMessage(true));
        }
    }

    public static boolean isDragDropInProgress() {
        return dragDropInProgress;
    }

    private static String getExceptionMessage(boolean b) {
        return b ? "Drag and drop in progress" : "No drag in progress";
    }

    public static int convertModifiersToDropAction(int modifiers, int supportedActions) {
        int dropAction = 0;
        switch (modifiers & 0xC0) {
            case 192: {
                dropAction = 0x40000000;
                break;
            }
            case 128: {
                dropAction = 1;
                break;
            }
            case 64: {
                dropAction = 2;
                break;
            }
            default: {
                if ((supportedActions & 2) != 0) {
                    dropAction = 2;
                    break;
                }
                if ((supportedActions & 1) != 0) {
                    dropAction = 1;
                    break;
                }
                if ((supportedActions & 0x40000000) == 0) break;
                dropAction = 0x40000000;
            }
        }
        return dropAction & supportedActions;
    }

    private void cleanup() {
        this.trigger = null;
        this.component = null;
        this.cursor = null;
        this.dragSourceContext = null;
        SunDropTargetContextPeer.setCurrentJVMLocalSourceTransferable(null);
        SunDragSourceContextPeer.setDragDropInProgress(false);
    }

    private class EventDispatcher
    implements Runnable {
        private final int dispatchType;
        private final DragSourceEvent event;

        EventDispatcher(int dispatchType, DragSourceEvent event) {
            switch (dispatchType) {
                case 1: 
                case 2: 
                case 3: 
                case 6: {
                    if (event instanceof DragSourceDragEvent) break;
                    throw new IllegalArgumentException("Event: " + String.valueOf(event));
                }
                case 4: {
                    break;
                }
                case 5: {
                    if (event instanceof DragSourceDropEvent) break;
                    throw new IllegalArgumentException("Event: " + String.valueOf(event));
                }
                default: {
                    throw new IllegalArgumentException("Dispatch type: " + dispatchType);
                }
            }
            this.dispatchType = dispatchType;
            this.event = event;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public void run() {
            DragSourceContext dragSourceContext = SunDragSourceContextPeer.this.getDragSourceContext();
            try {
                switch (this.dispatchType) {
                    case 1: {
                        dragSourceContext.dragEnter((DragSourceDragEvent)this.event);
                        return;
                    }
                    case 2: {
                        dragSourceContext.dragOver((DragSourceDragEvent)this.event);
                        return;
                    }
                    case 3: {
                        dragSourceContext.dropActionChanged((DragSourceDragEvent)this.event);
                        return;
                    }
                    case 4: {
                        dragSourceContext.dragExit(this.event);
                        return;
                    }
                    case 6: {
                        dragSourceContext.dragMouseMoved((DragSourceDragEvent)this.event);
                        return;
                    }
                    case 5: {
                        try {
                            dragSourceContext.dragDropEnd((DragSourceDropEvent)this.event);
                            return;
                        }
                        finally {
                            SunDragSourceContextPeer.this.cleanup();
                        }
                    }
                    default: {
                        throw new IllegalStateException("Dispatch type: " + this.dispatchType);
                    }
                }
            }
            finally {
                SunDragSourceContextPeer.this.quitSecondaryEventLoop();
            }
        }
    }
}

