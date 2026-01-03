/*
 * Decompiled with CFR 0.152.
 */
package java.awt.dnd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DropTargetContextPeer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import sun.awt.AWTAccessor;

public final class DropTargetContext
implements Serializable {
    private static final long serialVersionUID = -634158968993743371L;
    private final DropTarget dropTarget;
    private transient DropTargetContextPeer dropTargetContextPeer;
    private transient Transferable transferable;

    DropTargetContext(DropTarget dt) {
        this.dropTarget = dt;
    }

    public DropTarget getDropTarget() {
        return this.dropTarget;
    }

    public Component getComponent() {
        return this.dropTarget.getComponent();
    }

    void reset() {
        this.dropTargetContextPeer = null;
        this.transferable = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setTargetActions(int actions) {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        if (peer != null) {
            DropTargetContextPeer dropTargetContextPeer = peer;
            synchronized (dropTargetContextPeer) {
                peer.setTargetActions(actions);
                this.getDropTarget().doSetDefaultActions(actions);
            }
        } else {
            this.getDropTarget().doSetDefaultActions(actions);
        }
    }

    protected int getTargetActions() {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        return peer != null ? peer.getTargetActions() : this.dropTarget.getDefaultActions();
    }

    public void dropComplete(boolean success) throws InvalidDnDOperationException {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        if (peer != null) {
            peer.dropComplete(success);
        }
    }

    protected void acceptDrag(int dragOperation) {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        if (peer != null) {
            peer.acceptDrag(dragOperation);
        }
    }

    protected void rejectDrag() {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        if (peer != null) {
            peer.rejectDrag();
        }
    }

    protected void acceptDrop(int dropOperation) {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        if (peer != null) {
            peer.acceptDrop(dropOperation);
        }
    }

    protected void rejectDrop() {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        if (peer != null) {
            peer.rejectDrop();
        }
    }

    protected DataFlavor[] getCurrentDataFlavors() {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        return peer != null ? peer.getTransferDataFlavors() : new DataFlavor[]{};
    }

    protected List<DataFlavor> getCurrentDataFlavorsAsList() {
        return Arrays.asList(this.getCurrentDataFlavors());
    }

    protected boolean isDataFlavorSupported(DataFlavor df) {
        return this.getCurrentDataFlavorsAsList().contains(df);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Transferable getTransferable() throws InvalidDnDOperationException {
        DropTargetContextPeer peer = this.getDropTargetContextPeer();
        if (peer == null) {
            throw new InvalidDnDOperationException();
        }
        if (this.transferable == null) {
            Transferable t = peer.getTransferable();
            boolean isLocal = peer.isTransferableJVMLocal();
            DropTargetContext dropTargetContext = this;
            synchronized (dropTargetContext) {
                if (this.transferable == null) {
                    this.transferable = this.createTransferableProxy(t, isLocal);
                }
            }
        }
        return this.transferable;
    }

    DropTargetContextPeer getDropTargetContextPeer() {
        return this.dropTargetContextPeer;
    }

    void setDropTargetContextPeer(DropTargetContextPeer dtcp) {
        this.dropTargetContextPeer = dtcp;
    }

    protected Transferable createTransferableProxy(Transferable t, boolean local) {
        return new TransferableProxy(this, t, local);
    }

    static {
        AWTAccessor.setDropTargetContextAccessor(new AWTAccessor.DropTargetContextAccessor(){

            @Override
            public void reset(DropTargetContext dtc) {
                dtc.reset();
            }

            @Override
            public void setDropTargetContextPeer(DropTargetContext dtc, DropTargetContextPeer dtcp) {
                dtc.setDropTargetContextPeer(dtcp);
            }
        });
    }

    protected class TransferableProxy
    implements Transferable {
        protected Transferable transferable;
        protected boolean isLocal;
        private sun.awt.datatransfer.TransferableProxy proxy;

        TransferableProxy(DropTargetContext this$0, Transferable t, boolean local) {
            this.proxy = new sun.awt.datatransfer.TransferableProxy(t, local);
            this.transferable = t;
            this.isLocal = local;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return this.proxy.getTransferDataFlavors();
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return this.proxy.isDataFlavorSupported(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
            return this.proxy.getTransferData(df);
        }
    }
}

