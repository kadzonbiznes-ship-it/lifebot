/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.AttrImpl;
import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.DOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.dom.LCount;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import com.sun.org.apache.xerces.internal.dom.NodeIteratorImpl;
import com.sun.org.apache.xerces.internal.dom.RangeImpl;
import com.sun.org.apache.xerces.internal.dom.TreeWalkerImpl;
import com.sun.org.apache.xerces.internal.dom.events.EventImpl;
import com.sun.org.apache.xerces.internal.dom.events.MutationEventImpl;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

public class DocumentImpl
extends CoreDocumentImpl
implements DocumentTraversal,
DocumentEvent,
DocumentRange {
    static final long serialVersionUID = 515687835542616694L;
    protected List<NodeIterator> iterators;
    protected List<Range> ranges;
    protected Map<NodeImpl, List<LEntry>> eventListeners;
    protected boolean mutationEvents = false;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("iterators", Vector.class), new ObjectStreamField("ranges", Vector.class), new ObjectStreamField("eventListeners", Hashtable.class), new ObjectStreamField("mutationEvents", Boolean.TYPE)};
    EnclosingAttr savedEnclosingAttr;

    public DocumentImpl() {
    }

    public DocumentImpl(boolean grammarAccess) {
        super(grammarAccess);
    }

    public DocumentImpl(DocumentType doctype) {
        super(doctype);
    }

    public DocumentImpl(DocumentType doctype, boolean grammarAccess) {
        super(doctype, grammarAccess);
    }

    @Override
    public Node cloneNode(boolean deep) {
        DocumentImpl newdoc = new DocumentImpl();
        this.callUserDataHandlers(this, newdoc, (short)1);
        this.cloneNode(newdoc, deep);
        newdoc.mutationEvents = this.mutationEvents;
        return newdoc;
    }

    @Override
    public DOMImplementation getImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }

    public NodeIterator createNodeIterator(Node root, short whatToShow, NodeFilter filter) {
        return this.createNodeIterator(root, whatToShow, filter, true);
    }

    @Override
    public NodeIterator createNodeIterator(Node root, int whatToShow, NodeFilter filter, boolean entityReferenceExpansion) {
        if (root == null) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
            throw new DOMException(9, msg);
        }
        NodeIteratorImpl iterator = new NodeIteratorImpl(this, root, whatToShow, filter, entityReferenceExpansion);
        if (this.iterators == null) {
            this.iterators = new ArrayList<NodeIterator>();
        }
        this.iterators.add(iterator);
        return iterator;
    }

    public TreeWalker createTreeWalker(Node root, short whatToShow, NodeFilter filter) {
        return this.createTreeWalker(root, whatToShow, filter, true);
    }

    @Override
    public TreeWalker createTreeWalker(Node root, int whatToShow, NodeFilter filter, boolean entityReferenceExpansion) {
        if (root == null) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
            throw new DOMException(9, msg);
        }
        return new TreeWalkerImpl(root, whatToShow, filter, entityReferenceExpansion);
    }

    void removeNodeIterator(NodeIterator nodeIterator) {
        if (nodeIterator == null) {
            return;
        }
        if (this.iterators == null) {
            return;
        }
        this.iterators.remove(nodeIterator);
    }

    @Override
    public Range createRange() {
        if (this.ranges == null) {
            this.ranges = new ArrayList<Range>();
        }
        RangeImpl range = new RangeImpl(this);
        this.ranges.add(range);
        return range;
    }

    void removeRange(Range range) {
        if (range == null) {
            return;
        }
        if (this.ranges == null) {
            return;
        }
        this.ranges.remove(range);
    }

    @Override
    void replacedText(NodeImpl node) {
        if (this.ranges != null) {
            int size = this.ranges.size();
            for (int i = 0; i != size; ++i) {
                ((RangeImpl)this.ranges.get(i)).receiveReplacedText(node);
            }
        }
    }

    @Override
    void deletedText(NodeImpl node, int offset, int count) {
        if (this.ranges != null) {
            int size = this.ranges.size();
            for (int i = 0; i != size; ++i) {
                ((RangeImpl)this.ranges.get(i)).receiveDeletedText(node, offset, count);
            }
        }
    }

    @Override
    void insertedText(NodeImpl node, int offset, int count) {
        if (this.ranges != null) {
            int size = this.ranges.size();
            for (int i = 0; i != size; ++i) {
                ((RangeImpl)this.ranges.get(i)).receiveInsertedText(node, offset, count);
            }
        }
    }

    void splitData(Node node, Node newNode, int offset) {
        if (this.ranges != null) {
            int size = this.ranges.size();
            for (int i = 0; i != size; ++i) {
                ((RangeImpl)this.ranges.get(i)).receiveSplitData(node, newNode, offset);
            }
        }
    }

    @Override
    public Event createEvent(String type) throws DOMException {
        if (type.equalsIgnoreCase("Events") || "Event".equals(type)) {
            return new EventImpl();
        }
        if (type.equalsIgnoreCase("MutationEvents") || "MutationEvent".equals(type)) {
            return new MutationEventImpl();
        }
        String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
        throw new DOMException(9, msg);
    }

    @Override
    void setMutationEvents(boolean set) {
        this.mutationEvents = set;
    }

    @Override
    boolean getMutationEvents() {
        return this.mutationEvents;
    }

    protected void setEventListeners(NodeImpl n, List<LEntry> listeners) {
        if (this.eventListeners == null) {
            this.eventListeners = new HashMap<NodeImpl, List<LEntry>>();
        }
        if (listeners == null) {
            this.eventListeners.remove(n);
            if (this.eventListeners.isEmpty()) {
                this.mutationEvents = false;
            }
        } else {
            this.eventListeners.put(n, listeners);
            this.mutationEvents = true;
        }
    }

    protected List<LEntry> getEventListeners(NodeImpl n) {
        if (this.eventListeners == null) {
            return null;
        }
        return this.eventListeners.get(n);
    }

    @Override
    protected void addEventListener(NodeImpl node, String type, EventListener listener, boolean useCapture) {
        if (type == null || type.equals("") || listener == null) {
            return;
        }
        this.removeEventListener(node, type, listener, useCapture);
        List<LEntry> nodeListeners = this.getEventListeners(node);
        if (nodeListeners == null) {
            nodeListeners = new ArrayList<LEntry>();
            this.setEventListeners(node, nodeListeners);
        }
        nodeListeners.add(new LEntry(type, listener, useCapture));
        LCount lc = LCount.lookup(type);
        if (useCapture) {
            ++lc.captures;
            ++lc.total;
        } else {
            ++lc.bubbles;
            ++lc.total;
        }
    }

    @Override
    protected void removeEventListener(NodeImpl node, String type, EventListener listener, boolean useCapture) {
        if (type == null || type.equals("") || listener == null) {
            return;
        }
        List<LEntry> nodeListeners = this.getEventListeners(node);
        if (nodeListeners == null) {
            return;
        }
        for (int i = nodeListeners.size() - 1; i >= 0; --i) {
            LEntry le = nodeListeners.get(i);
            if (le.useCapture != useCapture || le.listener != listener || !le.type.equals(type)) continue;
            nodeListeners.remove(i);
            if (nodeListeners.isEmpty()) {
                this.setEventListeners(node, null);
            }
            LCount lc = LCount.lookup(type);
            if (useCapture) {
                --lc.captures;
                --lc.total;
                break;
            }
            --lc.bubbles;
            --lc.total;
            break;
        }
    }

    @Override
    protected void copyEventListeners(NodeImpl src, NodeImpl tgt) {
        List<LEntry> nodeListeners = this.getEventListeners(src);
        if (nodeListeners == null) {
            return;
        }
        this.setEventListeners(tgt, new ArrayList<LEntry>(nodeListeners));
    }

    @Override
    protected boolean dispatchEvent(NodeImpl node, Event event) {
        if (event == null) {
            return false;
        }
        EventImpl evt = (EventImpl)event;
        if (!evt.initialized || evt.type == null || evt.type.equals("")) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "UNSPECIFIED_EVENT_TYPE_ERR", null);
            throw new EventException(0, msg);
        }
        LCount lc = LCount.lookup(evt.getType());
        if (lc.total == 0) {
            return evt.preventDefault;
        }
        evt.target = node;
        evt.stopPropagation = false;
        evt.preventDefault = false;
        ArrayList<Node> pv = new ArrayList<Node>(10);
        Node p = node;
        for (Node n = p.getParentNode(); n != null; n = n.getParentNode()) {
            pv.add(n);
            p = n;
        }
        if (lc.captures > 0) {
            evt.eventPhase = 1;
            for (int j = pv.size() - 1; j >= 0 && !evt.stopPropagation; --j) {
                NodeImpl nn = (NodeImpl)pv.get(j);
                evt.currentTarget = nn;
                ArrayList nodeListeners = (ArrayList)this.getEventListeners(nn);
                if (nodeListeners == null) continue;
                ArrayList nl = (ArrayList)nodeListeners.clone();
                int nlsize = nl.size();
                for (int i2 = 0; i2 < nlsize; ++i2) {
                    LEntry le = (LEntry)nl.get(i2);
                    if (!le.useCapture || !le.type.equals(evt.type) || !nodeListeners.contains(le)) continue;
                    try {
                        le.listener.handleEvent(evt);
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        if (lc.bubbles > 0) {
            evt.eventPhase = (short)2;
            evt.currentTarget = node;
            ArrayList nodeListeners = (ArrayList)this.getEventListeners(node);
            if (!evt.stopPropagation && nodeListeners != null) {
                ArrayList nl = (ArrayList)nodeListeners.clone();
                int nlsize = nl.size();
                for (int i = 0; i < nlsize; ++i) {
                    LEntry le = (LEntry)nl.get(i);
                    if (le.useCapture || !le.type.equals(evt.type) || !nodeListeners.contains(le)) continue;
                    try {
                        le.listener.handleEvent(evt);
                        continue;
                    }
                    catch (Exception i2) {
                        // empty catch block
                    }
                }
            }
            if (evt.bubbles) {
                evt.eventPhase = (short)3;
                int pvsize = pv.size();
                for (int j = 0; j < pvsize && !evt.stopPropagation; ++j) {
                    NodeImpl nn = (NodeImpl)pv.get(j);
                    evt.currentTarget = nn;
                    nodeListeners = (ArrayList)this.getEventListeners(nn);
                    if (nodeListeners == null) continue;
                    ArrayList nl = (ArrayList)nodeListeners.clone();
                    int nlsize = nl.size();
                    for (int i = 0; i < nlsize; ++i) {
                        LEntry le = (LEntry)nl.get(i);
                        if (le.useCapture || !le.type.equals(evt.type) || !nodeListeners.contains(le)) continue;
                        try {
                            le.listener.handleEvent(evt);
                            continue;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
            }
        }
        if (lc.defaults <= 0 || !evt.cancelable || !evt.preventDefault) {
            // empty if block
        }
        return evt.preventDefault;
    }

    protected void dispatchEventToSubtree(Node n, Event e) {
        ((NodeImpl)n).dispatchEvent(e);
        if (n.getNodeType() == 1) {
            NamedNodeMap a = n.getAttributes();
            for (int i = a.getLength() - 1; i >= 0; --i) {
                this.dispatchingEventToSubtree(a.item(i), e);
            }
        }
        this.dispatchingEventToSubtree(n.getFirstChild(), e);
    }

    protected void dispatchingEventToSubtree(Node n, Event e) {
        if (n == null) {
            return;
        }
        ((NodeImpl)n).dispatchEvent(e);
        if (n.getNodeType() == 1) {
            NamedNodeMap a = n.getAttributes();
            for (int i = a.getLength() - 1; i >= 0; --i) {
                this.dispatchingEventToSubtree(a.item(i), e);
            }
        }
        this.dispatchingEventToSubtree(n.getFirstChild(), e);
        this.dispatchingEventToSubtree(n.getNextSibling(), e);
    }

    protected void dispatchAggregateEvents(NodeImpl node, EnclosingAttr ea) {
        if (ea != null) {
            this.dispatchAggregateEvents(node, ea.node, ea.oldvalue, (short)1);
        } else {
            this.dispatchAggregateEvents(node, null, null, (short)0);
        }
    }

    protected void dispatchAggregateEvents(NodeImpl node, AttrImpl enclosingAttr, String oldvalue, short change) {
        MutationEventImpl me;
        LCount lc;
        NodeImpl owner = null;
        if (enclosingAttr != null) {
            lc = LCount.lookup("DOMAttrModified");
            owner = (NodeImpl)((Object)enclosingAttr.getOwnerElement());
            if (lc.total > 0 && owner != null) {
                me = new MutationEventImpl();
                me.initMutationEvent("DOMAttrModified", true, false, enclosingAttr, oldvalue, enclosingAttr.getNodeValue(), enclosingAttr.getNodeName(), change);
                owner.dispatchEvent(me);
            }
        }
        lc = LCount.lookup("DOMSubtreeModified");
        if (lc.total > 0) {
            me = new MutationEventImpl();
            me.initMutationEvent("DOMSubtreeModified", true, false, null, null, null, null, (short)0);
            if (enclosingAttr != null) {
                this.dispatchEvent(enclosingAttr, me);
                if (owner != null) {
                    this.dispatchEvent(owner, me);
                }
            } else {
                this.dispatchEvent(node, me);
            }
        }
    }

    protected void saveEnclosingAttr(NodeImpl node) {
        this.savedEnclosingAttr = null;
        LCount lc = LCount.lookup("DOMAttrModified");
        if (lc.total > 0) {
            NodeImpl eventAncestor = node;
            while (true) {
                if (eventAncestor == null) {
                    return;
                }
                short type = eventAncestor.getNodeType();
                if (type == 2) {
                    EnclosingAttr retval = new EnclosingAttr();
                    retval.node = (AttrImpl)eventAncestor;
                    retval.oldvalue = retval.node.getNodeValue();
                    this.savedEnclosingAttr = retval;
                    return;
                }
                if (type == 5) {
                    eventAncestor = eventAncestor.parentNode();
                    continue;
                }
                if (type != 3) break;
                eventAncestor = eventAncestor.parentNode();
            }
            return;
        }
    }

    @Override
    void modifyingCharacterData(NodeImpl node, boolean replace) {
        if (this.mutationEvents && !replace) {
            this.saveEnclosingAttr(node);
        }
    }

    @Override
    void modifiedCharacterData(NodeImpl node, String oldvalue, String value, boolean replace) {
        if (this.mutationEvents && !replace) {
            LCount lc = LCount.lookup("DOMCharacterDataModified");
            if (lc.total > 0) {
                MutationEventImpl me = new MutationEventImpl();
                me.initMutationEvent("DOMCharacterDataModified", true, false, null, oldvalue, value, null, (short)0);
                this.dispatchEvent(node, me);
            }
            this.dispatchAggregateEvents(node, this.savedEnclosingAttr);
        }
    }

    @Override
    void replacedCharacterData(NodeImpl node, String oldvalue, String value) {
        this.modifiedCharacterData(node, oldvalue, value, false);
    }

    @Override
    void insertingNode(NodeImpl node, boolean replace) {
        if (this.mutationEvents && !replace) {
            this.saveEnclosingAttr(node);
        }
    }

    @Override
    void insertedNode(NodeImpl node, NodeImpl newInternal, boolean replace) {
        if (this.mutationEvents) {
            LCount lc = LCount.lookup("DOMNodeInserted");
            if (lc.total > 0) {
                MutationEventImpl me = new MutationEventImpl();
                me.initMutationEvent("DOMNodeInserted", true, false, node, null, null, null, (short)0);
                this.dispatchEvent(newInternal, me);
            }
            lc = LCount.lookup("DOMNodeInsertedIntoDocument");
            if (lc.total > 0) {
                NodeImpl eventAncestor = node;
                if (this.savedEnclosingAttr != null) {
                    eventAncestor = (NodeImpl)((Object)this.savedEnclosingAttr.node.getOwnerElement());
                }
                if (eventAncestor != null) {
                    NodeImpl p = eventAncestor;
                    while (p != null) {
                        eventAncestor = p;
                        if (p.getNodeType() == 2) {
                            p = (NodeImpl)((Object)((AttrImpl)p).getOwnerElement());
                            continue;
                        }
                        p = p.parentNode();
                    }
                    if (eventAncestor.getNodeType() == 9) {
                        MutationEventImpl me = new MutationEventImpl();
                        me.initMutationEvent("DOMNodeInsertedIntoDocument", false, false, null, null, null, null, (short)0);
                        this.dispatchEventToSubtree(newInternal, me);
                    }
                }
            }
            if (!replace) {
                this.dispatchAggregateEvents(node, this.savedEnclosingAttr);
            }
        }
        if (this.ranges != null) {
            int size = this.ranges.size();
            for (int i = 0; i != size; ++i) {
                ((RangeImpl)this.ranges.get(i)).insertedNodeFromDOM(newInternal);
            }
        }
    }

    @Override
    void removingNode(NodeImpl node, NodeImpl oldChild, boolean replace) {
        int i;
        int size;
        if (this.iterators != null) {
            size = this.iterators.size();
            for (i = 0; i != size; ++i) {
                ((NodeIteratorImpl)this.iterators.get(i)).removeNode(oldChild);
            }
        }
        if (this.ranges != null) {
            size = this.ranges.size();
            for (i = 0; i != size; ++i) {
                ((RangeImpl)this.ranges.get(i)).removeNode(oldChild);
            }
        }
        if (this.mutationEvents) {
            if (!replace) {
                this.saveEnclosingAttr(node);
            }
            LCount lc = LCount.lookup("DOMNodeRemoved");
            if (lc.total > 0) {
                MutationEventImpl me = new MutationEventImpl();
                me.initMutationEvent("DOMNodeRemoved", true, false, node, null, null, null, (short)0);
                this.dispatchEvent(oldChild, me);
            }
            lc = LCount.lookup("DOMNodeRemovedFromDocument");
            if (lc.total > 0) {
                NodeImpl eventAncestor = this;
                if (this.savedEnclosingAttr != null) {
                    eventAncestor = (NodeImpl)((Object)this.savedEnclosingAttr.node.getOwnerElement());
                }
                if (eventAncestor != null) {
                    for (NodeImpl p = ((NodeImpl)eventAncestor).parentNode(); p != null; p = p.parentNode()) {
                        eventAncestor = p;
                    }
                    if (((NodeImpl)eventAncestor).getNodeType() == 9) {
                        MutationEventImpl me = new MutationEventImpl();
                        me.initMutationEvent("DOMNodeRemovedFromDocument", false, false, null, null, null, null, (short)0);
                        this.dispatchEventToSubtree(oldChild, me);
                    }
                }
            }
        }
    }

    @Override
    void removedNode(NodeImpl node, boolean replace) {
        if (this.mutationEvents && !replace) {
            this.dispatchAggregateEvents(node, this.savedEnclosingAttr);
        }
    }

    @Override
    void replacingNode(NodeImpl node) {
        if (this.mutationEvents) {
            this.saveEnclosingAttr(node);
        }
    }

    @Override
    void replacingData(NodeImpl node) {
        if (this.mutationEvents) {
            this.saveEnclosingAttr(node);
        }
    }

    @Override
    void replacedNode(NodeImpl node) {
        if (this.mutationEvents) {
            this.dispatchAggregateEvents(node, this.savedEnclosingAttr);
        }
    }

    @Override
    void modifiedAttrValue(AttrImpl attr, String oldvalue) {
        if (this.mutationEvents) {
            this.dispatchAggregateEvents(attr, attr, oldvalue, (short)1);
        }
    }

    @Override
    void setAttrNode(AttrImpl attr, AttrImpl previous) {
        if (this.mutationEvents) {
            if (previous == null) {
                this.dispatchAggregateEvents(attr.ownerNode, attr, null, (short)2);
            } else {
                this.dispatchAggregateEvents(attr.ownerNode, attr, previous.getNodeValue(), (short)1);
            }
        }
    }

    @Override
    void removedAttrNode(AttrImpl attr, NodeImpl oldOwner, String name) {
        if (this.mutationEvents) {
            LCount lc = LCount.lookup("DOMAttrModified");
            if (lc.total > 0) {
                MutationEventImpl me = new MutationEventImpl();
                me.initMutationEvent("DOMAttrModified", true, false, attr, attr.getNodeValue(), null, name, (short)3);
                this.dispatchEvent(oldOwner, me);
            }
            this.dispatchAggregateEvents(oldOwner, null, null, (short)0);
        }
    }

    @Override
    void renamedAttrNode(Attr oldAt, Attr newAt) {
    }

    @Override
    void renamedElement(Element oldEl, Element newEl) {
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Vector<NodeIterator> it = this.iterators == null ? null : new Vector<NodeIterator>(this.iterators);
        Vector<Range> r = this.ranges == null ? null : new Vector<Range>(this.ranges);
        Hashtable el = null;
        if (this.eventListeners != null) {
            el = new Hashtable();
            for (Map.Entry<NodeImpl, List<LEntry>> e : this.eventListeners.entrySet()) {
                el.put(e.getKey(), new Vector(e.getValue()));
            }
        }
        ObjectOutputStream.PutField pf = out.putFields();
        pf.put("iterators", it);
        pf.put("ranges", r);
        pf.put("eventListeners", el);
        pf.put("mutationEvents", this.mutationEvents);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = in.readFields();
        Vector it = (Vector)gf.get("iterators", null);
        Vector r = (Vector)gf.get("ranges", null);
        Hashtable el = (Hashtable)gf.get("eventListeners", null);
        this.mutationEvents = gf.get("mutationEvents", false);
        if (it != null) {
            this.iterators = new ArrayList<NodeIterator>(it);
        }
        if (r != null) {
            this.ranges = new ArrayList<Range>(r);
        }
        if (el != null) {
            this.eventListeners = new HashMap<NodeImpl, List<LEntry>>();
            for (Map.Entry e : el.entrySet()) {
                this.eventListeners.put((NodeImpl)e.getKey(), new ArrayList((Collection)e.getValue()));
            }
        }
    }

    class LEntry
    implements Serializable {
        private static final long serialVersionUID = -8426757059492421631L;
        String type;
        EventListener listener;
        boolean useCapture;

        LEntry(String type, EventListener listener, boolean useCapture) {
            this.type = type;
            this.listener = listener;
            this.useCapture = useCapture;
        }
    }

    class EnclosingAttr
    implements Serializable {
        private static final long serialVersionUID = 5208387723391647216L;
        AttrImpl node;
        String oldvalue;

        EnclosingAttr() {
        }
    }
}

