/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StateInvariantError;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class DefaultStyledDocument
extends AbstractDocument
implements StyledDocument {
    public static final int BUFFER_SIZE_DEFAULT = 4096;
    protected ElementBuffer buffer;
    private transient Vector<Style> listeningStyles = new Vector();
    private transient ChangeListener styleChangeListener;
    private transient ChangeListener styleContextChangeListener;
    private transient ChangeUpdateRunnable updateRunnable;

    public DefaultStyledDocument(AbstractDocument.Content c, StyleContext styles) {
        super(c, styles);
        this.buffer = new ElementBuffer(this.createDefaultRoot());
        Style defaultStyle = styles.getStyle("default");
        this.setLogicalStyle(0, defaultStyle);
    }

    public DefaultStyledDocument(StyleContext styles) {
        this((AbstractDocument.Content)new GapContent(4096), styles);
    }

    public DefaultStyledDocument() {
        this((AbstractDocument.Content)new GapContent(4096), new StyleContext());
    }

    @Override
    public Element getDefaultRootElement() {
        return this.buffer.getRootElement();
    }

    protected void create(ElementSpec[] data) {
        try {
            if (this.getLength() != 0) {
                this.remove(0, this.getLength());
            }
            this.writeLock();
            AbstractDocument.Content c = this.getContent();
            int n = data.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; ++i) {
                ElementSpec es = data[i];
                if (es.getLength() <= 0) continue;
                sb.append(es.getArray(), es.getOffset(), es.getLength());
            }
            UndoableEdit cEdit = c.insertString(0, sb.toString());
            int length = sb.length();
            AbstractDocument.DefaultDocumentEvent evnt = new AbstractDocument.DefaultDocumentEvent(0, length, DocumentEvent.EventType.INSERT);
            evnt.addEdit(cEdit);
            this.buffer.create(length, data, evnt);
            super.insertUpdate(evnt, null);
            evnt.end();
            this.fireInsertUpdate(evnt);
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, evnt));
        }
        catch (BadLocationException ble) {
            throw new StateInvariantError("problem initializing");
        }
        finally {
            this.writeUnlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            this.writeLock();
            AbstractDocument.Content c = this.getContent();
            int n = data.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; ++i) {
                ElementSpec es = data[i];
                if (es.getLength() <= 0) continue;
                sb.append(es.getArray(), es.getOffset(), es.getLength());
            }
            if (sb.length() == 0) {
                return;
            }
            UndoableEdit cEdit = c.insertString(offset, sb.toString());
            int length = sb.length();
            AbstractDocument.DefaultDocumentEvent evnt = new AbstractDocument.DefaultDocumentEvent(offset, length, DocumentEvent.EventType.INSERT);
            evnt.addEdit(cEdit);
            this.buffer.insert(offset, length, data, evnt);
            super.insertUpdate(evnt, null);
            evnt.end();
            this.fireInsertUpdate(evnt);
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, evnt));
        }
        finally {
            this.writeUnlock();
        }
    }

    public void removeElement(Element elem) {
        try {
            this.writeLock();
            this.removeElementImpl(elem);
        }
        finally {
            this.writeUnlock();
        }
    }

    private void removeElementImpl(Element elem) {
        int endOffset;
        int startOffset;
        if (elem.getDocument() != this) {
            throw new IllegalArgumentException("element doesn't belong to document");
        }
        AbstractDocument.BranchElement parent = (AbstractDocument.BranchElement)elem.getParentElement();
        if (parent == null) {
            throw new IllegalArgumentException("can't remove the root element");
        }
        int removeFrom = startOffset = elem.getStartOffset();
        int removeTo = endOffset = elem.getEndOffset();
        int lastEndOffset = this.getLength() + 1;
        AbstractDocument.Content content = this.getContent();
        boolean atEnd = false;
        boolean isComposedText = Utilities.isComposedTextElement(elem);
        if (endOffset >= lastEndOffset) {
            if (startOffset <= 0) {
                throw new IllegalArgumentException("can't remove the whole content");
            }
            removeTo = lastEndOffset - 1;
            try {
                if (content.getString(startOffset - 1, 1).charAt(0) == '\n') {
                    --removeFrom;
                }
            }
            catch (BadLocationException ble) {
                throw new IllegalStateException(ble);
            }
            atEnd = true;
        }
        int length = removeTo - removeFrom;
        AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(removeFrom, length, DocumentEvent.EventType.REMOVE);
        UndoableEdit ue = null;
        while (parent.getElementCount() == 1) {
            elem = parent;
            if ((parent = (AbstractDocument.BranchElement)parent.getParentElement()) != null) continue;
            throw new IllegalStateException("invalid element structure");
        }
        Element[] removed = new Element[]{elem};
        Element[] added = new Element[]{};
        int index = parent.getElementIndex(startOffset);
        parent.replace(index, 1, added);
        dde.addEdit(new AbstractDocument.ElementEdit(parent, index, removed, added));
        if (length > 0) {
            try {
                ue = content.remove(removeFrom, length);
                if (ue != null) {
                    dde.addEdit(ue);
                }
            }
            catch (BadLocationException ble) {
                throw new IllegalStateException(ble);
            }
            lastEndOffset -= length;
        }
        if (atEnd) {
            Element prevLeaf;
            for (prevLeaf = parent.getElement(parent.getElementCount() - 1); prevLeaf != null && !prevLeaf.isLeaf(); prevLeaf = prevLeaf.getElement(prevLeaf.getElementCount() - 1)) {
            }
            if (prevLeaf == null) {
                throw new IllegalStateException("invalid element structure");
            }
            int prevStartOffset = prevLeaf.getStartOffset();
            AbstractDocument.BranchElement prevParent = (AbstractDocument.BranchElement)prevLeaf.getParentElement();
            int prevIndex = prevParent.getElementIndex(prevStartOffset);
            Element newElem = this.createLeafElement(prevParent, prevLeaf.getAttributes(), prevStartOffset, lastEndOffset);
            Element[] prevRemoved = new Element[]{prevLeaf};
            Element[] prevAdded = new Element[]{newElem};
            prevParent.replace(prevIndex, 1, prevAdded);
            dde.addEdit(new AbstractDocument.ElementEdit(prevParent, prevIndex, prevRemoved, prevAdded));
        }
        this.postRemoveUpdate(dde);
        dde.end();
        this.fireRemoveUpdate(dde);
        if (!isComposedText || ue == null) {
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
        }
    }

    @Override
    public Style addStyle(String nm, Style parent) {
        StyleContext styles = (StyleContext)this.getAttributeContext();
        return styles.addStyle(nm, parent);
    }

    @Override
    public void removeStyle(String nm) {
        StyleContext styles = (StyleContext)this.getAttributeContext();
        styles.removeStyle(nm);
    }

    @Override
    public Style getStyle(String nm) {
        StyleContext styles = (StyleContext)this.getAttributeContext();
        return styles.getStyle(nm);
    }

    public Enumeration<?> getStyleNames() {
        return ((StyleContext)this.getAttributeContext()).getStyleNames();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLogicalStyle(int pos, Style s) {
        Element paragraph = this.getParagraphElement(pos);
        if (paragraph instanceof AbstractDocument.AbstractElement) {
            AbstractDocument.AbstractElement abstractElement = (AbstractDocument.AbstractElement)paragraph;
            try {
                this.writeLock();
                StyleChangeUndoableEdit edit = new StyleChangeUndoableEdit(abstractElement, s);
                abstractElement.setResolveParent(s);
                int p0 = paragraph.getStartOffset();
                int p1 = paragraph.getEndOffset();
                AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(p0, p1 - p0, DocumentEvent.EventType.CHANGE);
                e.addEdit(edit);
                e.end();
                this.fireChangedUpdate(e);
                this.fireUndoableEditUpdate(new UndoableEditEvent(this, e));
            }
            finally {
                this.writeUnlock();
            }
        }
    }

    @Override
    public Style getLogicalStyle(int p) {
        AttributeSet a;
        AttributeSet parent;
        Style s = null;
        Element paragraph = this.getParagraphElement(p);
        if (paragraph != null && (parent = (a = paragraph.getAttributes()).getResolveParent()) instanceof Style) {
            s = (Style)parent;
        }
        return s;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
        if (length <= 0) {
            return;
        }
        try {
            Element run;
            int lastEnd;
            this.writeLock();
            AbstractDocument.DefaultDocumentEvent changes = new AbstractDocument.DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
            this.buffer.change(offset, length, changes);
            AttributeSet sCopy = s.copyAttributes();
            int pos = offset;
            while (pos < offset + length && pos != (lastEnd = (run = this.getCharacterElement(pos)).getEndOffset())) {
                MutableAttributeSet attr = (MutableAttributeSet)run.getAttributes();
                changes.addEdit(new AttributeUndoableEdit(run, sCopy, replace));
                if (replace) {
                    attr.removeAttributes(attr);
                }
                attr.addAttributes(s);
                pos = lastEnd;
            }
            changes.end();
            this.fireChangedUpdate(changes);
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        }
        finally {
            this.writeUnlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
        try {
            this.writeLock();
            AbstractDocument.DefaultDocumentEvent changes = new AbstractDocument.DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
            AttributeSet sCopy = s.copyAttributes();
            Element section = this.getDefaultRootElement();
            int index0 = section.getElementIndex(offset);
            int index1 = section.getElementIndex(offset + (length > 0 ? length - 1 : 0));
            boolean isI18N = Boolean.TRUE.equals(this.getProperty("i18n"));
            boolean hasRuns = false;
            for (int i = index0; i <= index1; ++i) {
                Element paragraph = section.getElement(i);
                MutableAttributeSet attr = (MutableAttributeSet)paragraph.getAttributes();
                changes.addEdit(new AttributeUndoableEdit(paragraph, sCopy, replace));
                if (replace) {
                    attr.removeAttributes(attr);
                }
                attr.addAttributes(s);
                if (!isI18N || hasRuns) continue;
                hasRuns = attr.getAttribute(TextAttribute.RUN_DIRECTION) != null;
            }
            if (hasRuns) {
                this.updateBidi(changes);
            }
            changes.end();
            this.fireChangedUpdate(changes);
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        }
        finally {
            this.writeUnlock();
        }
    }

    @Override
    public Element getParagraphElement(int pos) {
        Element e = this.getDefaultRootElement();
        while (!e.isLeaf()) {
            int index = e.getElementIndex(pos);
            e = e.getElement(index);
        }
        if (e != null) {
            return e.getParentElement();
        }
        return e;
    }

    @Override
    public Element getCharacterElement(int pos) {
        Element e = this.getDefaultRootElement();
        while (!e.isLeaf()) {
            int index = e.getElementIndex(pos);
            e = e.getElement(index);
        }
        return e;
    }

    @Override
    protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
        int offset = chng.getOffset();
        int length = chng.getLength();
        if (attr == null) {
            attr = SimpleAttributeSet.EMPTY;
        }
        Element paragraph = this.getParagraphElement(offset + length);
        AttributeSet pattr = paragraph.getAttributes();
        Element pParagraph = this.getParagraphElement(offset);
        Element run = pParagraph.getElement(pParagraph.getElementIndex(offset));
        int endOffset = offset + length;
        boolean insertingAtBoundry = run.getEndOffset() == endOffset;
        AttributeSet cattr = run.getAttributes();
        try {
            ElementSpec last;
            Segment s = new Segment();
            Vector<ElementSpec> parseBuffer = new Vector<ElementSpec>();
            ElementSpec lastStartSpec = null;
            boolean insertingAfterNewline = false;
            short lastStartDirection = 6;
            if (offset > 0) {
                this.getText(offset - 1, 1, s);
                if (s.array[s.offset] == '\n') {
                    insertingAfterNewline = true;
                    lastStartDirection = this.createSpecsForInsertAfterNewline(paragraph, pParagraph, pattr, parseBuffer, offset, endOffset);
                    for (int counter = parseBuffer.size() - 1; counter >= 0; --counter) {
                        ElementSpec spec = parseBuffer.elementAt(counter);
                        if (spec.getType() != 1) continue;
                        lastStartSpec = spec;
                        break;
                    }
                }
            }
            if (!insertingAfterNewline) {
                pattr = pParagraph.getAttributes();
            }
            this.getText(offset, length, s);
            char[] txt = s.array;
            int n = s.offset + s.count;
            int lastOffset = s.offset;
            for (int i = s.offset; i < n; ++i) {
                if (txt[i] != '\n') continue;
                int breakOffset = i + 1;
                parseBuffer.addElement(new ElementSpec(attr, 3, breakOffset - lastOffset));
                parseBuffer.addElement(new ElementSpec(null, 2));
                lastStartSpec = new ElementSpec(pattr, 1);
                parseBuffer.addElement(lastStartSpec);
                lastOffset = breakOffset;
            }
            if (lastOffset < n) {
                parseBuffer.addElement(new ElementSpec(attr, 3, n - lastOffset));
            }
            ElementSpec first = parseBuffer.firstElement();
            int docLength = this.getLength();
            if (first.getType() == 3 && cattr.isEqual(attr)) {
                first.setDirection((short)4);
            }
            if (lastStartSpec != null) {
                if (insertingAfterNewline) {
                    lastStartSpec.setDirection(lastStartDirection);
                } else if (pParagraph.getEndOffset() != endOffset) {
                    lastStartSpec.setDirection((short)7);
                } else {
                    Element parent = pParagraph.getParentElement();
                    int pParagraphIndex = parent.getElementIndex(offset);
                    if (pParagraphIndex + 1 < parent.getElementCount() && !parent.getElement(pParagraphIndex + 1).isLeaf()) {
                        lastStartSpec.setDirection((short)5);
                    }
                }
            }
            if (insertingAtBoundry && endOffset < docLength) {
                Element nextRun;
                last = parseBuffer.lastElement();
                if (last.getType() == 3 && last.getDirection() != 4 && (lastStartSpec == null && (paragraph == pParagraph || insertingAfterNewline) || lastStartSpec != null && lastStartSpec.getDirection() != 6) && (nextRun = paragraph.getElement(paragraph.getElementIndex(endOffset))).isLeaf() && attr.isEqual(nextRun.getAttributes())) {
                    last.setDirection((short)5);
                }
            } else if (!insertingAtBoundry && lastStartSpec != null && lastStartSpec.getDirection() == 7 && (last = parseBuffer.lastElement()).getType() == 3 && last.getDirection() != 4 && attr.isEqual(cattr)) {
                last.setDirection((short)5);
            }
            if (Utilities.isComposedTextAttributeDefined(attr)) {
                MutableAttributeSet mattr = (MutableAttributeSet)attr;
                mattr.addAttributes(cattr);
                mattr.addAttribute("$ename", "content");
                mattr.addAttribute(StyleConstants.NameAttribute, "content");
                if (mattr.isDefined("CR")) {
                    mattr.removeAttribute("CR");
                }
            }
            Object[] spec = new ElementSpec[parseBuffer.size()];
            parseBuffer.copyInto(spec);
            this.buffer.insert(offset, length, (ElementSpec[])spec, chng);
        }
        catch (BadLocationException badLocationException) {
            // empty catch block
        }
        super.insertUpdate(chng, attr);
    }

    short createSpecsForInsertAfterNewline(Element paragraph, Element pParagraph, AttributeSet pattr, Vector<ElementSpec> parseBuffer, int offset, int endOffset) {
        if (paragraph.getParentElement() == pParagraph.getParentElement()) {
            ElementSpec spec = new ElementSpec(pattr, 2);
            parseBuffer.addElement(spec);
            spec = new ElementSpec(pattr, 1);
            parseBuffer.addElement(spec);
            if (pParagraph.getEndOffset() != endOffset) {
                return 7;
            }
            Element parent = pParagraph.getParentElement();
            if (parent.getElementIndex(offset) + 1 < parent.getElementCount()) {
                return 5;
            }
        } else {
            Element e;
            ArrayList<Element> leftParents = new ArrayList<Element>();
            ArrayList<Element> rightParents = new ArrayList<Element>();
            for (e = pParagraph; e != null; e = e.getParentElement()) {
                leftParents.add(e);
            }
            int leftIndex = -1;
            for (e = paragraph; e != null && (leftIndex = leftParents.indexOf(e)) == -1; e = e.getParentElement()) {
                rightParents.add(e);
            }
            if (e != null) {
                for (int counter = 0; counter < leftIndex; ++counter) {
                    parseBuffer.addElement(new ElementSpec(null, 2));
                }
                for (int counter = rightParents.size() - 1; counter >= 0; --counter) {
                    ElementSpec spec = new ElementSpec(((Element)rightParents.get(counter)).getAttributes(), 1);
                    if (counter > 0) {
                        spec.setDirection((short)5);
                    }
                    parseBuffer.addElement(spec);
                }
                if (rightParents.size() > 0) {
                    return 5;
                }
                return 7;
            }
        }
        return 6;
    }

    @Override
    protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
        super.removeUpdate(chng);
        this.buffer.remove(chng.getOffset(), chng.getLength(), chng);
    }

    protected AbstractDocument.AbstractElement createDefaultRoot() {
        this.writeLock();
        SectionElement section = new SectionElement();
        AbstractDocument.BranchElement paragraph = new AbstractDocument.BranchElement(section, null);
        AbstractDocument.LeafElement brk = new AbstractDocument.LeafElement(paragraph, null, 0, 1);
        Element[] buff = new Element[]{brk};
        paragraph.replace(0, 0, buff);
        buff[0] = paragraph;
        section.replace(0, 0, buff);
        this.writeUnlock();
        return section;
    }

    @Override
    public Color getForeground(AttributeSet attr) {
        StyleContext styles = (StyleContext)this.getAttributeContext();
        return styles.getForeground(attr);
    }

    @Override
    public Color getBackground(AttributeSet attr) {
        StyleContext styles = (StyleContext)this.getAttributeContext();
        return styles.getBackground(attr);
    }

    @Override
    public Font getFont(AttributeSet attr) {
        StyleContext styles = (StyleContext)this.getAttributeContext();
        return styles.getFont(attr);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void styleChanged(Style style) {
        if (this.getLength() != 0) {
            if (this.updateRunnable == null) {
                this.updateRunnable = new ChangeUpdateRunnable();
            }
            ChangeUpdateRunnable changeUpdateRunnable = this.updateRunnable;
            synchronized (changeUpdateRunnable) {
                if (!this.updateRunnable.isPending) {
                    SwingUtilities.invokeLater(this.updateRunnable);
                    this.updateRunnable.isPending = true;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addDocumentListener(DocumentListener listener) {
        Vector<Style> vector = this.listeningStyles;
        synchronized (vector) {
            int oldDLCount = this.listenerList.getListenerCount(DocumentListener.class);
            super.addDocumentListener(listener);
            if (oldDLCount == 0) {
                if (this.styleContextChangeListener == null) {
                    this.styleContextChangeListener = this.createStyleContextChangeListener();
                }
                if (this.styleContextChangeListener != null) {
                    StyleContext styles = (StyleContext)this.getAttributeContext();
                    List<ChangeListener> staleListeners = AbstractChangeHandler.getStaleListeners(this.styleContextChangeListener);
                    for (ChangeListener l : staleListeners) {
                        styles.removeChangeListener(l);
                    }
                    styles.addChangeListener(this.styleContextChangeListener);
                }
                this.updateStylesListeningTo();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeDocumentListener(DocumentListener listener) {
        Vector<Style> vector = this.listeningStyles;
        synchronized (vector) {
            super.removeDocumentListener(listener);
            if (this.listenerList.getListenerCount(DocumentListener.class) == 0) {
                for (int counter = this.listeningStyles.size() - 1; counter >= 0; --counter) {
                    this.listeningStyles.elementAt(counter).removeChangeListener(this.styleChangeListener);
                }
                this.listeningStyles.removeAllElements();
                if (this.styleContextChangeListener != null) {
                    StyleContext styles = (StyleContext)this.getAttributeContext();
                    styles.removeChangeListener(this.styleContextChangeListener);
                }
            }
        }
    }

    ChangeListener createStyleChangeListener() {
        return new StyleChangeHandler(this);
    }

    ChangeListener createStyleContextChangeListener() {
        return new StyleContextChangeHandler(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void updateStylesListeningTo() {
        Vector<Style> vector = this.listeningStyles;
        synchronized (vector) {
            StyleContext styles = (StyleContext)this.getAttributeContext();
            if (this.styleChangeListener == null) {
                this.styleChangeListener = this.createStyleChangeListener();
            }
            if (this.styleChangeListener != null && styles != null) {
                Style aStyle;
                Enumeration<?> styleNames = styles.getStyleNames();
                Vector v = (Vector)this.listeningStyles.clone();
                this.listeningStyles.removeAllElements();
                List<ChangeListener> staleListeners = AbstractChangeHandler.getStaleListeners(this.styleChangeListener);
                while (styleNames.hasMoreElements()) {
                    String name = (String)styleNames.nextElement();
                    aStyle = styles.getStyle(name);
                    int index = v.indexOf(aStyle);
                    this.listeningStyles.addElement(aStyle);
                    if (index == -1) {
                        for (ChangeListener l : staleListeners) {
                            aStyle.removeChangeListener(l);
                        }
                        aStyle.addChangeListener(this.styleChangeListener);
                        continue;
                    }
                    v.removeElementAt(index);
                }
                for (int counter = v.size() - 1; counter >= 0; --counter) {
                    aStyle = (Style)v.elementAt(counter);
                    aStyle.removeChangeListener(this.styleChangeListener);
                }
                if (this.listeningStyles.size() == 0) {
                    this.styleChangeListener = null;
                }
            }
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        this.listeningStyles = new Vector();
        ObjectInputStream.GetField f = s.readFields();
        this.buffer = (ElementBuffer)f.get("buffer", null);
        if (this.styleContextChangeListener == null && this.listenerList.getListenerCount(DocumentListener.class) > 0) {
            this.styleContextChangeListener = this.createStyleContextChangeListener();
            if (this.styleContextChangeListener != null) {
                StyleContext styles = (StyleContext)this.getAttributeContext();
                styles.addChangeListener(this.styleContextChangeListener);
            }
            this.updateStylesListeningTo();
        }
    }

    public class ElementBuffer
    implements Serializable {
        Element root;
        transient int pos;
        transient int offset;
        transient int length;
        transient int endOffset;
        transient Vector<ElemChanges> changes;
        transient Stack<ElemChanges> path;
        transient boolean insertOp;
        transient boolean recreateLeafs;
        transient ElemChanges[] insertPath;
        transient boolean createdFracture;
        transient Element fracturedParent;
        transient Element fracturedChild;
        transient boolean offsetLastIndex;
        transient boolean offsetLastIndexOnReplace;

        public ElementBuffer(Element root) {
            this.root = root;
            this.changes = new Vector();
            this.path = new Stack();
        }

        public Element getRootElement() {
            return this.root;
        }

        public void insert(int offset, int length, ElementSpec[] data, AbstractDocument.DefaultDocumentEvent de) {
            if (length == 0) {
                return;
            }
            this.insertOp = true;
            this.beginEdits(offset, length);
            this.insertUpdate(data);
            this.endEdits(de);
            this.insertOp = false;
        }

        void create(int length, ElementSpec[] data, AbstractDocument.DefaultDocumentEvent de) {
            this.insertOp = true;
            this.beginEdits(this.offset, length);
            Element elem = this.root;
            int index = elem.getElementIndex(0);
            while (!elem.isLeaf()) {
                Element child = elem.getElement(index);
                this.push(elem, index);
                elem = child;
                index = elem.getElementIndex(0);
            }
            ElemChanges ec = this.path.peek();
            Element child = ec.parent.getElement(ec.index);
            ec.added.addElement(DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), DefaultStyledDocument.this.getLength(), child.getEndOffset()));
            ec.removed.addElement(child);
            while (this.path.size() > 1) {
                this.pop();
            }
            int n = data.length;
            AttributeSet newAttrs = null;
            if (n > 0 && data[0].getType() == 1) {
                newAttrs = data[0].getAttributes();
            }
            if (newAttrs == null) {
                newAttrs = SimpleAttributeSet.EMPTY;
            }
            MutableAttributeSet attr = (MutableAttributeSet)this.root.getAttributes();
            de.addEdit(new AttributeUndoableEdit(this.root, newAttrs, true));
            attr.removeAttributes(attr);
            attr.addAttributes(newAttrs);
            for (int i = 1; i < n; ++i) {
                this.insertElement(data[i]);
            }
            while (this.path.size() != 0) {
                this.pop();
            }
            this.endEdits(de);
            this.insertOp = false;
        }

        public void remove(int offset, int length, AbstractDocument.DefaultDocumentEvent de) {
            this.beginEdits(offset, length);
            this.removeUpdate();
            this.endEdits(de);
        }

        public void change(int offset, int length, AbstractDocument.DefaultDocumentEvent de) {
            this.beginEdits(offset, length);
            this.changeUpdate();
            this.endEdits(de);
        }

        protected void insertUpdate(ElementSpec[] data) {
            ElemChanges change;
            int counter;
            int i;
            Element elem = this.root;
            int index = elem.getElementIndex(this.offset);
            while (!elem.isLeaf()) {
                Element child = elem.getElement(index);
                this.push(elem, child.isLeaf() ? index : index + 1);
                elem = child;
                index = elem.getElementIndex(this.offset);
            }
            this.insertPath = new ElemChanges[this.path.size()];
            this.path.copyInto(this.insertPath);
            this.createdFracture = false;
            this.recreateLeafs = false;
            if (data[0].getType() == 3) {
                this.insertFirstContent(data);
                this.pos += data[0].getLength();
                i = 1;
            } else {
                this.fractureDeepestLeaf(data);
                i = 0;
            }
            int n = data.length;
            while (i < n) {
                this.insertElement(data[i]);
                ++i;
            }
            if (!this.createdFracture) {
                this.fracture(-1);
            }
            while (this.path.size() != 0) {
                this.pop();
            }
            if (this.offsetLastIndex && this.offsetLastIndexOnReplace) {
                ++this.insertPath[this.insertPath.length - 1].index;
            }
            for (counter = this.insertPath.length - 1; counter >= 0; --counter) {
                change = this.insertPath[counter];
                if (change.parent == this.fracturedParent) {
                    change.added.addElement(this.fracturedChild);
                }
                if (change.added.size() <= 0 && change.removed.size() <= 0 || this.changes.contains(change)) continue;
                this.changes.addElement(change);
            }
            if (this.offset == 0 && this.fracturedParent != null && data[0].getType() == 2) {
                for (counter = 0; counter < data.length && data[counter].getType() == 2; ++counter) {
                }
                change = this.insertPath[this.insertPath.length - counter - 1];
                change.removed.insertElementAt(change.parent.getElement(--change.index), 0);
            }
        }

        protected void removeUpdate() {
            this.removeElements(this.root, this.offset, this.offset + this.length);
        }

        protected void changeUpdate() {
            boolean didEnd = this.split(this.offset, this.length);
            if (!didEnd) {
                while (this.path.size() != 0) {
                    this.pop();
                }
                this.split(this.offset + this.length, 0);
            }
            while (this.path.size() != 0) {
                this.pop();
            }
        }

        boolean split(int offs, int len) {
            boolean splitEnd = false;
            Element e = this.root;
            int index = e.getElementIndex(offs);
            while (!e.isLeaf()) {
                this.push(e, index);
                e = e.getElement(index);
                index = e.getElementIndex(offs);
            }
            ElemChanges ec = this.path.peek();
            Element child = ec.parent.getElement(ec.index);
            if (child.getStartOffset() < offs && offs < child.getEndOffset()) {
                int index0;
                int index1 = index0 = ec.index;
                if (offs + len < ec.parent.getEndOffset() && len != 0) {
                    index1 = ec.parent.getElementIndex(offs + len);
                    if (index1 == index0) {
                        ec.removed.addElement(child);
                        e = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), child.getStartOffset(), offs);
                        ec.added.addElement(e);
                        e = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), offs, offs + len);
                        ec.added.addElement(e);
                        e = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), offs + len, child.getEndOffset());
                        ec.added.addElement(e);
                        return true;
                    }
                    child = ec.parent.getElement(index1);
                    if (offs + len == child.getStartOffset()) {
                        index1 = index0;
                    }
                    splitEnd = true;
                }
                this.pos = offs;
                child = ec.parent.getElement(index0);
                ec.removed.addElement(child);
                e = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), child.getStartOffset(), this.pos);
                ec.added.addElement(e);
                e = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), this.pos, child.getEndOffset());
                ec.added.addElement(e);
                for (int i = index0 + 1; i < index1; ++i) {
                    child = ec.parent.getElement(i);
                    ec.removed.addElement(child);
                    ec.added.addElement(child);
                }
                if (index1 != index0) {
                    child = ec.parent.getElement(index1);
                    this.pos = offs + len;
                    ec.removed.addElement(child);
                    e = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), child.getStartOffset(), this.pos);
                    ec.added.addElement(e);
                    e = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), this.pos, child.getEndOffset());
                    ec.added.addElement(e);
                }
            }
            return splitEnd;
        }

        void endEdits(AbstractDocument.DefaultDocumentEvent de) {
            int n = this.changes.size();
            for (int i = 0; i < n; ++i) {
                ElemChanges ec = this.changes.elementAt(i);
                Object[] removed = new Element[ec.removed.size()];
                ec.removed.copyInto(removed);
                Object[] added = new Element[ec.added.size()];
                ec.added.copyInto(added);
                int index = ec.index;
                ((AbstractDocument.BranchElement)ec.parent).replace(index, removed.length, (Element[])added);
                AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(ec.parent, index, (Element[])removed, (Element[])added);
                de.addEdit(ee);
            }
            this.changes.removeAllElements();
            this.path.removeAllElements();
        }

        void beginEdits(int offset, int length) {
            this.offset = offset;
            this.length = length;
            this.endOffset = offset + length;
            this.pos = offset;
            if (this.changes == null) {
                this.changes = new Vector();
            } else {
                this.changes.removeAllElements();
            }
            if (this.path == null) {
                this.path = new Stack();
            } else {
                this.path.removeAllElements();
            }
            this.fracturedParent = null;
            this.fracturedChild = null;
            this.offsetLastIndexOnReplace = false;
            this.offsetLastIndex = false;
        }

        void push(Element e, int index, boolean isFracture) {
            ElemChanges ec = new ElemChanges(this, e, index, isFracture);
            this.path.push(ec);
        }

        void push(Element e, int index) {
            this.push(e, index, false);
        }

        void pop() {
            Element e;
            ElemChanges ec = this.path.peek();
            this.path.pop();
            if (ec.added.size() > 0 || ec.removed.size() > 0) {
                this.changes.addElement(ec);
            } else if (!this.path.isEmpty() && (e = ec.parent).getElementCount() == 0) {
                ec = this.path.peek();
                ec.added.removeElement(e);
            }
        }

        void advance(int n) {
            this.pos += n;
        }

        void insertElement(ElementSpec es) {
            ElemChanges ec = this.path.peek();
            block0 : switch (es.getType()) {
                case 1: {
                    switch (es.getDirection()) {
                        case 5: {
                            Element parent = ec.parent.getElement(ec.index);
                            if (parent.isLeaf()) {
                                if (ec.index + 1 < ec.parent.getElementCount()) {
                                    parent = ec.parent.getElement(ec.index + 1);
                                } else {
                                    throw new StateInvariantError("Join next to leaf");
                                }
                            }
                            this.push(parent, 0, true);
                            break block0;
                        }
                        case 7: {
                            if (!this.createdFracture) {
                                this.fracture(this.path.size() - 1);
                            }
                            if (!ec.isFracture) {
                                this.push(this.fracturedChild, 0, true);
                                break block0;
                            }
                            this.push(ec.parent.getElement(0), 0, true);
                            break block0;
                        }
                    }
                    Element belem = DefaultStyledDocument.this.createBranchElement(ec.parent, es.getAttributes());
                    ec.added.addElement(belem);
                    this.push(belem, 0);
                    break;
                }
                case 2: {
                    this.pop();
                    break;
                }
                case 3: {
                    int len = es.getLength();
                    if (es.getDirection() != 5) {
                        Element leaf = DefaultStyledDocument.this.createLeafElement(ec.parent, es.getAttributes(), this.pos, this.pos + len);
                        ec.added.addElement(leaf);
                    } else if (!ec.isFracture) {
                        Element first = null;
                        if (this.insertPath != null) {
                            for (int counter = this.insertPath.length - 1; counter >= 0; --counter) {
                                if (this.insertPath[counter] != ec) continue;
                                if (counter == this.insertPath.length - 1) break;
                                first = ec.parent.getElement(ec.index);
                                break;
                            }
                        }
                        if (first == null) {
                            first = ec.parent.getElement(ec.index + 1);
                        }
                        Element leaf = DefaultStyledDocument.this.createLeafElement(ec.parent, first.getAttributes(), this.pos, first.getEndOffset());
                        ec.added.addElement(leaf);
                        ec.removed.addElement(first);
                    } else {
                        Element first = ec.parent.getElement(0);
                        Element leaf = DefaultStyledDocument.this.createLeafElement(ec.parent, first.getAttributes(), this.pos, first.getEndOffset());
                        ec.added.addElement(leaf);
                        ec.removed.addElement(first);
                    }
                    this.pos += len;
                }
            }
        }

        boolean removeElements(Element elem, int rmOffs0, int rmOffs1) {
            if (!elem.isLeaf()) {
                int index0 = elem.getElementIndex(rmOffs0);
                int index1 = elem.getElementIndex(rmOffs1);
                this.push(elem, index0);
                ElemChanges ec = this.path.peek();
                if (index0 == index1) {
                    Element child0 = elem.getElement(index0);
                    if (rmOffs0 <= child0.getStartOffset() && rmOffs1 >= child0.getEndOffset()) {
                        ec.removed.addElement(child0);
                    } else if (this.removeElements(child0, rmOffs0, rmOffs1)) {
                        ec.removed.addElement(child0);
                    }
                } else {
                    boolean containsOffs1;
                    Element child0 = elem.getElement(index0);
                    Element child1 = elem.getElement(index1);
                    boolean bl = containsOffs1 = rmOffs1 < elem.getEndOffset();
                    if (containsOffs1 && this.canJoin(child0, child1)) {
                        for (int i = index0; i <= index1; ++i) {
                            ec.removed.addElement(elem.getElement(i));
                        }
                        Element e = this.join(elem, child0, child1, rmOffs0, rmOffs1);
                        ec.added.addElement(e);
                    } else {
                        int rmIndex0 = index0 + 1;
                        int rmIndex1 = index1 - 1;
                        if (child0.getStartOffset() == rmOffs0 || index0 == 0 && child0.getStartOffset() > rmOffs0 && child0.getEndOffset() <= rmOffs1) {
                            child0 = null;
                            rmIndex0 = index0;
                        }
                        if (!containsOffs1) {
                            child1 = null;
                            ++rmIndex1;
                        } else if (child1.getStartOffset() == rmOffs1) {
                            child1 = null;
                        }
                        if (rmIndex0 <= rmIndex1) {
                            ec.index = rmIndex0;
                        }
                        for (int i = rmIndex0; i <= rmIndex1; ++i) {
                            ec.removed.addElement(elem.getElement(i));
                        }
                        if (child0 != null && this.removeElements(child0, rmOffs0, rmOffs1)) {
                            ec.removed.insertElementAt(child0, 0);
                            ec.index = index0;
                        }
                        if (child1 != null && this.removeElements(child1, rmOffs0, rmOffs1)) {
                            ec.removed.addElement(child1);
                        }
                    }
                }
                this.pop();
                if (elem.getElementCount() == ec.removed.size() - ec.added.size()) {
                    return true;
                }
            }
            return false;
        }

        boolean canJoin(Element e0, Element e1) {
            boolean leaf1;
            if (e0 == null || e1 == null) {
                return false;
            }
            boolean leaf0 = e0.isLeaf();
            if (leaf0 != (leaf1 = e1.isLeaf())) {
                return false;
            }
            if (leaf0) {
                return e0.getAttributes().isEqual(e1.getAttributes());
            }
            String name0 = e0.getName();
            String name1 = e1.getName();
            if (name0 != null) {
                return name0.equals(name1);
            }
            if (name1 != null) {
                return name1.equals(name0);
            }
            return true;
        }

        Element join(Element p, Element left, Element right, int rmOffs0, int rmOffs1) {
            if (left.isLeaf() && right.isLeaf()) {
                return DefaultStyledDocument.this.createLeafElement(p, left.getAttributes(), left.getStartOffset(), right.getEndOffset());
            }
            if (!left.isLeaf() && !right.isLeaf()) {
                int i;
                Element rj;
                Element to = DefaultStyledDocument.this.createBranchElement(p, left.getAttributes());
                int ljIndex = left.getElementIndex(rmOffs0);
                int rjIndex = right.getElementIndex(rmOffs1);
                Element lj = left.getElement(ljIndex);
                if (lj.getStartOffset() >= rmOffs0) {
                    lj = null;
                }
                if ((rj = right.getElement(rjIndex)).getStartOffset() == rmOffs1) {
                    rj = null;
                }
                ArrayList<Element> children = new ArrayList<Element>();
                for (int i2 = 0; i2 < ljIndex; ++i2) {
                    children.add(this.clone(to, left.getElement(i2)));
                }
                if (this.canJoin(lj, rj)) {
                    Element e = this.join(to, lj, rj, rmOffs0, rmOffs1);
                    children.add(e);
                } else {
                    if (lj != null) {
                        children.add(this.cloneAsNecessary(to, lj, rmOffs0, rmOffs1));
                    }
                    if (rj != null) {
                        children.add(this.cloneAsNecessary(to, rj, rmOffs0, rmOffs1));
                    }
                }
                int n = right.getElementCount();
                int n2 = i = rj == null ? rjIndex : rjIndex + 1;
                while (i < n) {
                    children.add(this.clone(to, right.getElement(i)));
                    ++i;
                }
                Element[] c = children.toArray(new Element[0]);
                ((AbstractDocument.BranchElement)to).replace(0, 0, c);
                return to;
            }
            throw new StateInvariantError("No support to join leaf element with non-leaf element");
        }

        public Element clone(Element parent, Element clonee) {
            if (clonee.isLeaf()) {
                return DefaultStyledDocument.this.createLeafElement(parent, clonee.getAttributes(), clonee.getStartOffset(), clonee.getEndOffset());
            }
            Element e = DefaultStyledDocument.this.createBranchElement(parent, clonee.getAttributes());
            int n = clonee.getElementCount();
            Element[] children = new Element[n];
            for (int i = 0; i < n; ++i) {
                children[i] = this.clone(e, clonee.getElement(i));
            }
            ((AbstractDocument.BranchElement)e).replace(0, 0, children);
            return e;
        }

        Element cloneAsNecessary(Element parent, Element clonee, int rmOffs0, int rmOffs1) {
            if (clonee.isLeaf()) {
                return DefaultStyledDocument.this.createLeafElement(parent, clonee.getAttributes(), clonee.getStartOffset(), clonee.getEndOffset());
            }
            Element e = DefaultStyledDocument.this.createBranchElement(parent, clonee.getAttributes());
            int n = clonee.getElementCount();
            ArrayList<Element> childrenList = new ArrayList<Element>(n);
            for (int i = 0; i < n; ++i) {
                Element elem = clonee.getElement(i);
                if (elem.getStartOffset() >= rmOffs0 && elem.getEndOffset() <= rmOffs1) continue;
                childrenList.add(this.cloneAsNecessary(e, elem, rmOffs0, rmOffs1));
            }
            Element[] children = new Element[childrenList.size()];
            children = childrenList.toArray(children);
            ((AbstractDocument.BranchElement)e).replace(0, 0, children);
            return e;
        }

        void fracture(int depth) {
            int cLength = this.insertPath.length;
            int lastIndex = -1;
            boolean needRecreate = this.recreateLeafs;
            ElemChanges lastChange = this.insertPath[cLength - 1];
            boolean childAltered = lastChange.index + 1 < lastChange.parent.getElementCount();
            int deepestAlteredIndex = needRecreate ? cLength : -1;
            int lastAlteredIndex = cLength - 1;
            this.createdFracture = true;
            for (int counter = cLength - 2; counter >= 0; --counter) {
                ElemChanges change = this.insertPath[counter];
                if (change.added.size() > 0 || counter == depth) {
                    lastIndex = counter;
                    if (!needRecreate && childAltered) {
                        needRecreate = true;
                        if (deepestAlteredIndex == -1) {
                            deepestAlteredIndex = lastAlteredIndex + 1;
                        }
                    }
                }
                if (childAltered || change.index >= change.parent.getElementCount()) continue;
                childAltered = true;
                lastAlteredIndex = counter;
            }
            if (needRecreate) {
                if (lastIndex == -1) {
                    lastIndex = cLength - 1;
                }
                this.fractureFrom(this.insertPath, lastIndex, deepestAlteredIndex);
            }
        }

        void fractureFrom(ElemChanges[] changed, int startIndex, int endFractureIndex) {
            ElemChanges change = changed[startIndex];
            int changeLength = changed.length;
            Object child = startIndex + 1 == changeLength ? change.parent.getElement(change.index) : change.parent.getElement(change.index - 1);
            Element newChild = child.isLeaf() ? DefaultStyledDocument.this.createLeafElement(change.parent, child.getAttributes(), Math.max(this.endOffset, child.getStartOffset()), child.getEndOffset()) : DefaultStyledDocument.this.createBranchElement(change.parent, child.getAttributes());
            this.fracturedParent = change.parent;
            this.fracturedChild = newChild;
            Element parent = newChild;
            while (++startIndex < endFractureIndex) {
                Element[] kids;
                int moveStartIndex;
                boolean isEnd = startIndex + 1 == endFractureIndex;
                boolean isEndLeaf = startIndex + 1 == changeLength;
                change = changed[startIndex];
                child = isEnd ? (this.offsetLastIndex || !isEndLeaf ? null : change.parent.getElement(change.index)) : change.parent.getElement(change.index - 1);
                newChild = child != null ? (child.isLeaf() ? DefaultStyledDocument.this.createLeafElement(parent, child.getAttributes(), Math.max(this.endOffset, child.getStartOffset()), child.getEndOffset()) : DefaultStyledDocument.this.createBranchElement(parent, child.getAttributes())) : null;
                int kidsToMove = change.parent.getElementCount() - change.index;
                int kidStartIndex = 1;
                if (newChild == null) {
                    if (isEndLeaf) {
                        --kidsToMove;
                        moveStartIndex = change.index + 1;
                    } else {
                        moveStartIndex = change.index;
                    }
                    kidStartIndex = 0;
                    kids = new Element[kidsToMove];
                } else {
                    if (!isEnd) {
                        ++kidsToMove;
                        moveStartIndex = change.index;
                    } else {
                        moveStartIndex = change.index + 1;
                    }
                    kids = new Element[kidsToMove];
                    kids[0] = newChild;
                }
                for (int counter = kidStartIndex; counter < kidsToMove; ++counter) {
                    Element toMove = change.parent.getElement(moveStartIndex++);
                    kids[counter] = this.recreateFracturedElement(parent, toMove);
                    change.removed.addElement(toMove);
                }
                ((AbstractDocument.BranchElement)parent).replace(0, 0, kids);
                parent = newChild;
            }
        }

        Element recreateFracturedElement(Element parent, Element toDuplicate) {
            if (toDuplicate.isLeaf()) {
                return DefaultStyledDocument.this.createLeafElement(parent, toDuplicate.getAttributes(), Math.max(toDuplicate.getStartOffset(), this.endOffset), toDuplicate.getEndOffset());
            }
            Element newParent = DefaultStyledDocument.this.createBranchElement(parent, toDuplicate.getAttributes());
            int childCount = toDuplicate.getElementCount();
            Element[] newKids = new Element[childCount];
            for (int counter = 0; counter < childCount; ++counter) {
                newKids[counter] = this.recreateFracturedElement(newParent, toDuplicate.getElement(counter));
            }
            ((AbstractDocument.BranchElement)newParent).replace(0, 0, newKids);
            return newParent;
        }

        void fractureDeepestLeaf(ElementSpec[] specs) {
            ElemChanges ec = this.path.peek();
            Element child = ec.parent.getElement(ec.index);
            if (this.offset != 0) {
                Element newChild = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), child.getStartOffset(), this.offset);
                ec.added.addElement(newChild);
            }
            ec.removed.addElement(child);
            if (child.getEndOffset() != this.endOffset) {
                this.recreateLeafs = true;
            } else {
                this.offsetLastIndex = true;
            }
        }

        void insertFirstContent(ElementSpec[] specs) {
            ElementSpec firstSpec = specs[0];
            ElemChanges ec = this.path.peek();
            Element child = ec.parent.getElement(ec.index);
            int firstEndOffset = this.offset + firstSpec.getLength();
            boolean isOnlyContent = specs.length == 1;
            switch (firstSpec.getDirection()) {
                case 4: {
                    if (child.getEndOffset() != firstEndOffset && !isOnlyContent) {
                        Element newE = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), child.getStartOffset(), firstEndOffset);
                        ec.added.addElement(newE);
                        ec.removed.addElement(child);
                        if (child.getEndOffset() != this.endOffset) {
                            this.recreateLeafs = true;
                            break;
                        }
                        this.offsetLastIndex = true;
                        break;
                    }
                    this.offsetLastIndex = true;
                    this.offsetLastIndexOnReplace = true;
                    break;
                }
                case 5: {
                    if (this.offset == 0) break;
                    Element newE = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), child.getStartOffset(), this.offset);
                    ec.added.addElement(newE);
                    Element nextChild = ec.parent.getElement(ec.index + 1);
                    newE = isOnlyContent ? DefaultStyledDocument.this.createLeafElement(ec.parent, nextChild.getAttributes(), this.offset, nextChild.getEndOffset()) : DefaultStyledDocument.this.createLeafElement(ec.parent, nextChild.getAttributes(), this.offset, firstEndOffset);
                    ec.added.addElement(newE);
                    ec.removed.addElement(child);
                    ec.removed.addElement(nextChild);
                    break;
                }
                default: {
                    Element newE;
                    if (child.getStartOffset() != this.offset) {
                        newE = DefaultStyledDocument.this.createLeafElement(ec.parent, child.getAttributes(), child.getStartOffset(), this.offset);
                        ec.added.addElement(newE);
                    }
                    ec.removed.addElement(child);
                    newE = DefaultStyledDocument.this.createLeafElement(ec.parent, firstSpec.getAttributes(), this.offset, firstEndOffset);
                    ec.added.addElement(newE);
                    if (child.getEndOffset() != this.endOffset) {
                        this.recreateLeafs = true;
                        break;
                    }
                    this.offsetLastIndex = true;
                }
            }
        }

        class ElemChanges {
            Element parent;
            int index;
            Vector<Element> added;
            Vector<Element> removed;
            boolean isFracture;

            ElemChanges(ElementBuffer this$1, Element parent, int index, boolean isFracture) {
                this.parent = parent;
                this.index = index;
                this.isFracture = isFracture;
                this.added = new Vector();
                this.removed = new Vector();
            }

            public String toString() {
                return "added: " + String.valueOf(this.added) + "\nremoved: " + String.valueOf(this.removed) + "\n";
            }
        }
    }

    public static class ElementSpec {
        public static final short StartTagType = 1;
        public static final short EndTagType = 2;
        public static final short ContentType = 3;
        public static final short JoinPreviousDirection = 4;
        public static final short JoinNextDirection = 5;
        public static final short OriginateDirection = 6;
        public static final short JoinFractureDirection = 7;
        private AttributeSet attr;
        private int len;
        private short type;
        private short direction;
        private int offs;
        private char[] data;

        public ElementSpec(AttributeSet a, short type) {
            this(a, type, null, 0, 0);
        }

        public ElementSpec(AttributeSet a, short type, int len) {
            this(a, type, null, 0, len);
        }

        public ElementSpec(AttributeSet a, short type, char[] txt, int offs, int len) {
            this.attr = a;
            this.type = type;
            this.data = txt == null ? null : Arrays.copyOf(txt, txt.length);
            this.offs = offs;
            this.len = len;
            this.direction = (short)6;
        }

        public void setType(short type) {
            this.type = type;
        }

        public short getType() {
            return this.type;
        }

        public void setDirection(short direction) {
            this.direction = direction;
        }

        public short getDirection() {
            return this.direction;
        }

        public AttributeSet getAttributes() {
            return this.attr;
        }

        public char[] getArray() {
            return this.data == null ? null : Arrays.copyOf(this.data, this.data.length);
        }

        public int getOffset() {
            return this.offs;
        }

        public int getLength() {
            return this.len;
        }

        public String toString() {
            String tlbl = "??";
            String plbl = "??";
            switch (this.type) {
                case 1: {
                    tlbl = "StartTag";
                    break;
                }
                case 3: {
                    tlbl = "Content";
                    break;
                }
                case 2: {
                    tlbl = "EndTag";
                }
            }
            switch (this.direction) {
                case 4: {
                    plbl = "JoinPrevious";
                    break;
                }
                case 5: {
                    plbl = "JoinNext";
                    break;
                }
                case 6: {
                    plbl = "Originate";
                    break;
                }
                case 7: {
                    plbl = "Fracture";
                }
            }
            return tlbl + ":" + plbl + ":" + this.getLength();
        }
    }

    static class StyleChangeUndoableEdit
    extends AbstractUndoableEdit {
        protected AbstractDocument.AbstractElement element;
        protected Style newStyle;
        protected AttributeSet oldStyle;

        public StyleChangeUndoableEdit(AbstractDocument.AbstractElement element, Style newStyle) {
            this.element = element;
            this.newStyle = newStyle;
            this.oldStyle = element.getResolveParent();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            this.element.setResolveParent(this.newStyle);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            this.element.setResolveParent(this.oldStyle);
        }
    }

    public static class AttributeUndoableEdit
    extends AbstractUndoableEdit {
        protected AttributeSet newAttributes;
        protected AttributeSet copy;
        protected boolean isReplacing;
        protected Element element;

        public AttributeUndoableEdit(Element element, AttributeSet newAttributes, boolean isReplacing) {
            this.element = element;
            this.newAttributes = newAttributes;
            this.isReplacing = isReplacing;
            this.copy = element.getAttributes().copyAttributes();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            MutableAttributeSet as = (MutableAttributeSet)this.element.getAttributes();
            if (this.isReplacing) {
                as.removeAttributes(as);
            }
            as.addAttributes(this.newAttributes);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            MutableAttributeSet as = (MutableAttributeSet)this.element.getAttributes();
            as.removeAttributes(as);
            as.addAttributes(this.copy);
        }
    }

    protected class SectionElement
    extends AbstractDocument.BranchElement {
        public SectionElement() {
            super(null, null);
        }

        @Override
        public String getName() {
            return "section";
        }
    }

    class ChangeUpdateRunnable
    implements Runnable {
        boolean isPending = false;

        ChangeUpdateRunnable() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            ChangeUpdateRunnable changeUpdateRunnable = this;
            synchronized (changeUpdateRunnable) {
                this.isPending = false;
            }
            try {
                DefaultStyledDocument.this.writeLock();
                AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(0, DefaultStyledDocument.this.getLength(), DocumentEvent.EventType.CHANGE);
                dde.end();
                DefaultStyledDocument.this.fireChangedUpdate(dde);
            }
            finally {
                DefaultStyledDocument.this.writeUnlock();
            }
        }
    }

    static abstract class AbstractChangeHandler
    implements ChangeListener {
        private static final Map<Class<?>, ReferenceQueue<DefaultStyledDocument>> queueMap = new HashMap();
        private DocReference doc;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        AbstractChangeHandler(DefaultStyledDocument d) {
            ReferenceQueue<DefaultStyledDocument> q;
            Class<?> c = this.getClass();
            Map<Class<?>, ReferenceQueue<DefaultStyledDocument>> map = queueMap;
            synchronized (map) {
                q = queueMap.get(c);
                if (q == null) {
                    q = new ReferenceQueue();
                    queueMap.put(c, q);
                }
            }
            this.doc = new DocReference(d, q);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static List<ChangeListener> getStaleListeners(ChangeListener l) {
            ArrayList<ChangeListener> staleListeners = new ArrayList<ChangeListener>();
            ReferenceQueue<DefaultStyledDocument> q = queueMap.get(l.getClass());
            if (q != null) {
                ReferenceQueue<DefaultStyledDocument> referenceQueue = q;
                synchronized (referenceQueue) {
                    DocReference r;
                    while ((r = (DocReference)q.poll()) != null) {
                        staleListeners.add(r.getListener());
                    }
                }
            }
            return staleListeners;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            DefaultStyledDocument d = (DefaultStyledDocument)this.doc.get();
            if (d != null) {
                this.fireStateChanged(d, e);
            }
        }

        abstract void fireStateChanged(DefaultStyledDocument var1, ChangeEvent var2);

        private class DocReference
        extends WeakReference<DefaultStyledDocument> {
            DocReference(DefaultStyledDocument d, ReferenceQueue<DefaultStyledDocument> q) {
                super(d, q);
            }

            ChangeListener getListener() {
                return AbstractChangeHandler.this;
            }
        }
    }

    static class StyleChangeHandler
    extends AbstractChangeHandler {
        StyleChangeHandler(DefaultStyledDocument d) {
            super(d);
        }

        @Override
        void fireStateChanged(DefaultStyledDocument d, ChangeEvent e) {
            Object source = e.getSource();
            if (source instanceof Style) {
                d.styleChanged((Style)source);
            } else {
                d.styleChanged(null);
            }
        }
    }

    static class StyleContextChangeHandler
    extends AbstractChangeHandler {
        StyleContextChangeHandler(DefaultStyledDocument d) {
            super(d);
        }

        @Override
        void fireStateChanged(DefaultStyledDocument d, ChangeEvent e) {
            d.updateStylesListeningTo();
        }
    }
}

