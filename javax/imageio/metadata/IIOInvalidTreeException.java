/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.metadata;

import javax.imageio.IIOException;
import org.w3c.dom.Node;

public class IIOInvalidTreeException
extends IIOException {
    private static final long serialVersionUID = -1314083172544132777L;
    protected Node offendingNode = null;

    public IIOInvalidTreeException(String message, Node offendingNode) {
        super(message);
        this.offendingNode = offendingNode;
    }

    public IIOInvalidTreeException(String message, Throwable cause, Node offendingNode) {
        super(message, cause);
        this.offendingNode = offendingNode;
    }

    public Node getOffendingNode() {
        return this.offendingNode;
    }
}

