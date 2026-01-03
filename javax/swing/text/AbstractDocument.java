/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.Bidi;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SegmentCache;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StateInvariantError;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.Utilities;
import javax.swing.tree.TreeNode;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import sun.font.BidiUtils;
import sun.swing.SwingUtilities2;
import sun.swing.text.UndoableEditLockSupport;

public abstract class AbstractDocument
implements Document,
Serializable {
    private transient int numReaders;
    private transient Thread currWriter;
    private transient int numWriters;
    private transient boolean notifyingListeners;
    private static Boolean defaultI18NProperty;
    private Dictionary<Object, Object> documentProperties = null;
    protected EventListenerList listenerList = new EventListenerList();
    private Content data;
    private AttributeContext context;
    private transient BranchElement bidiRoot;
    private DocumentFilter documentFilter;
    private transient DocumentFilter.FilterBypass filterBypass;
    private static final String BAD_LOCK_STATE = "document lock failure";
    protected static final String BAD_LOCATION = "document location failure";
    public static final String ParagraphElementName = "paragraph";
    public static final String ContentElementName = "content";
    public static final String SectionElementName = "section";
    public static final String BidiElementName = "bidi level";
    public static final String ElementNameAttribute = "$ename";
    static final String I18NProperty = "i18n";
    static final Object MultiByteProperty;
    static final String AsyncLoadPriority = "load priority";

    protected AbstractDocument(Content data) {
        this(data, StyleContext.getDefaultStyleContext());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected AbstractDocument(Content data, AttributeContext context) {
        this.data = data;
        this.context = context;
        this.bidiRoot = new BidiRootElement();
        if (defaultI18NProperty == null) {
            String o = AccessController.doPrivileged(new PrivilegedAction<String>(this){

                @Override
                public String run() {
                    return System.getProperty(AbstractDocument.I18NProperty);
                }
            });
            defaultI18NProperty = o != null ? Boolean.valueOf(o) : Boolean.FALSE;
        }
        this.putProperty(I18NProperty, defaultI18NProperty);
        this.writeLock();
        try {
            Element[] p = new Element[]{new BidiElement((Element)this.bidiRoot, 0, 1, 0)};
            this.bidiRoot.replace(0, 0, p);
        }
        finally {
            this.writeUnlock();
        }
    }

    public Dictionary<Object, Object> getDocumentProperties() {
        if (this.documentProperties == null) {
            this.documentProperties = new Hashtable<Object, Object>(2);
        }
        return this.documentProperties;
    }

    public void setDocumentProperties(Dictionary<Object, Object> x) {
        this.documentProperties = x;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void fireInsertUpdate(DocumentEvent e) {
        this.notifyingListeners = true;
        try {
            Object[] listeners = this.listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] != DocumentListener.class) continue;
                ((DocumentListener)listeners[i + 1]).insertUpdate(e);
            }
        }
        finally {
            this.notifyingListeners = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void fireChangedUpdate(DocumentEvent e) {
        this.notifyingListeners = true;
        try {
            Object[] listeners = this.listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] != DocumentListener.class) continue;
                ((DocumentListener)listeners[i + 1]).changedUpdate(e);
            }
        }
        finally {
            this.notifyingListeners = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void fireRemoveUpdate(DocumentEvent e) {
        this.notifyingListeners = true;
        try {
            Object[] listeners = this.listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] != DocumentListener.class) continue;
                ((DocumentListener)listeners[i + 1]).removeUpdate(e);
            }
        }
        finally {
            this.notifyingListeners = false;
        }
    }

    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        if (e.getEdit() instanceof DefaultDocumentEvent) {
            e = new UndoableEditEvent(e.getSource(), new DefaultDocumentEventUndoableWrapper((DefaultDocumentEvent)e.getEdit()));
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != UndoableEditListener.class) continue;
            ((UndoableEditListener)listeners[i + 1]).undoableEditHappened(e);
        }
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return this.listenerList.getListeners(listenerType);
    }

    public int getAsynchronousLoadPriority() {
        Integer loadPriority = (Integer)this.getProperty(AsyncLoadPriority);
        if (loadPriority != null) {
            return loadPriority;
        }
        return -1;
    }

    public void setAsynchronousLoadPriority(int p) {
        Integer loadPriority = p >= 0 ? Integer.valueOf(p) : null;
        this.putProperty(AsyncLoadPriority, loadPriority);
    }

    public void setDocumentFilter(DocumentFilter filter) {
        this.documentFilter = filter;
    }

    public DocumentFilter getDocumentFilter() {
        return this.documentFilter;
    }

    @Override
    public void render(Runnable r) {
        this.readLock();
        try {
            r.run();
        }
        finally {
            this.readUnlock();
        }
    }

    @Override
    public int getLength() {
        return this.data.length() - 1;
    }

    @Override
    public void addDocumentListener(DocumentListener listener) {
        this.listenerList.add(DocumentListener.class, listener);
    }

    @Override
    public void removeDocumentListener(DocumentListener listener) {
        this.listenerList.remove(DocumentListener.class, listener);
    }

    public DocumentListener[] getDocumentListeners() {
        return (DocumentListener[])this.listenerList.getListeners(DocumentListener.class);
    }

    @Override
    public void addUndoableEditListener(UndoableEditListener listener) {
        this.listenerList.add(UndoableEditListener.class, listener);
    }

    @Override
    public void removeUndoableEditListener(UndoableEditListener listener) {
        this.listenerList.remove(UndoableEditListener.class, listener);
    }

    public UndoableEditListener[] getUndoableEditListeners() {
        return (UndoableEditListener[])this.listenerList.getListeners(UndoableEditListener.class);
    }

    @Override
    public final Object getProperty(Object key) {
        return this.getDocumentProperties().get(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void putProperty(Object key, Object value) {
        if (value != null) {
            this.getDocumentProperties().put(key, value);
        } else {
            this.getDocumentProperties().remove(key);
        }
        if (key == TextAttribute.RUN_DIRECTION && Boolean.TRUE.equals(this.getProperty(I18NProperty))) {
            this.writeLock();
            try {
                DefaultDocumentEvent e = new DefaultDocumentEvent(0, this.getLength(), DocumentEvent.EventType.INSERT);
                this.updateBidi(e);
            }
            finally {
                this.writeUnlock();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(int offs, int len) throws BadLocationException {
        DocumentFilter filter = this.getDocumentFilter();
        this.writeLock();
        try {
            if (filter != null) {
                filter.remove(this.getFilterBypass(), offs, len);
            } else {
                this.handleRemove(offs, len);
            }
        }
        finally {
            this.writeUnlock();
        }
    }

    void handleRemove(int offs, int len) throws BadLocationException {
        if (len > 0) {
            if (offs < 0 || offs + len > this.getLength()) {
                throw new BadLocationException("Invalid remove", this.getLength() + 1);
            }
            DefaultDocumentEvent chng = new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE);
            boolean isComposedTextElement = Utilities.isComposedTextElement(this, offs);
            this.removeUpdate(chng);
            UndoableEdit u = this.data.remove(offs, len);
            if (u != null) {
                chng.addEdit(u);
            }
            this.postRemoveUpdate(chng);
            chng.end();
            this.fireRemoveUpdate(chng);
            if (u != null && !isComposedTextElement) {
                this.fireUndoableEditUpdate(new UndoableEditEvent(this, chng));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (length == 0 && (text == null || text.length() == 0)) {
            return;
        }
        DocumentFilter filter = this.getDocumentFilter();
        this.writeLock();
        try {
            if (filter != null) {
                filter.replace(this.getFilterBypass(), offset, length, text, attrs);
            } else {
                if (length > 0) {
                    this.remove(offset, length);
                }
                if (text != null && text.length() > 0) {
                    this.insertString(offset, text, attrs);
                }
            }
        }
        finally {
            this.writeUnlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null || str.length() == 0) {
            return;
        }
        if (offs > this.getLength()) {
            throw new BadLocationException("Invalid insert", this.getLength());
        }
        DocumentFilter filter = this.getDocumentFilter();
        this.writeLock();
        try {
            if (filter != null) {
                filter.insertString(this.getFilterBypass(), offs, str, a);
            } else {
                this.handleInsertString(offs, str, a);
            }
        }
        finally {
            this.writeUnlock();
        }
    }

    private void handleInsertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null || str.length() == 0) {
            return;
        }
        UndoableEdit u = this.data.insertString(offs, str);
        DefaultDocumentEvent e = new DefaultDocumentEvent(offs, str.length(), DocumentEvent.EventType.INSERT);
        if (u != null) {
            e.addEdit(u);
        }
        if (this.getProperty(I18NProperty).equals(Boolean.FALSE)) {
            Object d = this.getProperty(TextAttribute.RUN_DIRECTION);
            if (d != null && d.equals(TextAttribute.RUN_DIRECTION_RTL)) {
                this.putProperty(I18NProperty, Boolean.TRUE);
            } else {
                char[] chars = str.toCharArray();
                if (SwingUtilities2.isComplexLayout(chars, 0, chars.length)) {
                    this.putProperty(I18NProperty, Boolean.TRUE);
                }
            }
        }
        this.insertUpdate(e, a);
        e.end();
        this.fireInsertUpdate(e);
        if (!(u == null || a != null && a.isDefined(StyleConstants.ComposedTextAttribute))) {
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, e));
        }
    }

    @Override
    public String getText(int offset, int length) throws BadLocationException {
        if (length < 0) {
            throw new BadLocationException("Length must be positive", length);
        }
        String str = this.data.getString(offset, length);
        return str;
    }

    @Override
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        if (length < 0) {
            throw new BadLocationException("Length must be positive", length);
        }
        this.data.getChars(offset, length, txt);
    }

    @Override
    public synchronized Position createPosition(int offs) throws BadLocationException {
        return this.data.createPosition(offs);
    }

    @Override
    public final Position getStartPosition() {
        Position p;
        try {
            p = this.createPosition(0);
        }
        catch (BadLocationException bl) {
            p = null;
        }
        return p;
    }

    @Override
    public final Position getEndPosition() {
        Position p;
        try {
            p = this.createPosition(this.data.length());
        }
        catch (BadLocationException bl) {
            p = null;
        }
        return p;
    }

    @Override
    public Element[] getRootElements() {
        Element[] elems = new Element[]{this.getDefaultRootElement(), this.getBidiRootElement()};
        return elems;
    }

    @Override
    public abstract Element getDefaultRootElement();

    private DocumentFilter.FilterBypass getFilterBypass() {
        if (this.filterBypass == null) {
            this.filterBypass = new DefaultFilterBypass();
        }
        return this.filterBypass;
    }

    public Element getBidiRootElement() {
        return this.bidiRoot;
    }

    static boolean isLeftToRight(Document doc, int p0, int p1) {
        int index;
        AbstractDocument adoc;
        Element bidiRoot;
        Element bidiElem;
        if (Boolean.TRUE.equals(doc.getProperty(I18NProperty)) && doc instanceof AbstractDocument && (bidiElem = (bidiRoot = (adoc = (AbstractDocument)doc).getBidiRootElement()).getElement(index = bidiRoot.getElementIndex(p0))).getEndOffset() >= p1) {
            AttributeSet bidiAttrs = bidiElem.getAttributes();
            return StyleConstants.getBidiLevel(bidiAttrs) % 2 == 0;
        }
        return true;
    }

    public abstract Element getParagraphElement(int var1);

    protected final AttributeContext getAttributeContext() {
        return this.context;
    }

    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        if (this.getProperty(I18NProperty).equals(Boolean.TRUE)) {
            this.updateBidi(chng);
        }
        if (chng.type == DocumentEvent.EventType.INSERT && chng.getLength() > 0 && !Boolean.TRUE.equals(this.getProperty(MultiByteProperty))) {
            Segment segment = SegmentCache.getSharedSegment();
            try {
                this.getText(chng.getOffset(), chng.getLength(), segment);
                segment.first();
                do {
                    if (segment.current() <= '\u00ff') continue;
                    this.putProperty(MultiByteProperty, Boolean.TRUE);
                    break;
                } while (segment.next() != '\uffff');
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
            SegmentCache.releaseSharedSegment(segment);
        }
    }

    protected void removeUpdate(DefaultDocumentEvent chng) {
    }

    protected void postRemoveUpdate(DefaultDocumentEvent chng) {
        if (this.getProperty(I18NProperty).equals(Boolean.TRUE)) {
            this.updateBidi(chng);
        }
    }

    void updateBidi(DefaultDocumentEvent chng) {
        int lastSpanStart;
        int firstSpanEnd;
        int lastPEnd;
        int firstPStart;
        if (chng.type == DocumentEvent.EventType.INSERT || chng.type == DocumentEvent.EventType.CHANGE) {
            int chngStart = chng.getOffset();
            int chngEnd = chngStart + chng.getLength();
            firstPStart = this.getParagraphElement(chngStart).getStartOffset();
            lastPEnd = this.getParagraphElement(chngEnd).getEndOffset();
        } else if (chng.type == DocumentEvent.EventType.REMOVE) {
            Element paragraph = this.getParagraphElement(chng.getOffset());
            firstPStart = paragraph.getStartOffset();
            lastPEnd = paragraph.getEndOffset();
        } else {
            throw new Error("Internal error: unknown event type.");
        }
        byte[] levels = this.calculateBidiLevels(firstPStart, lastPEnd);
        ArrayList<BidiElement> newElements = new ArrayList<BidiElement>();
        int firstSpanStart = firstPStart;
        int removeFromIndex = 0;
        if (firstSpanStart > 0) {
            int prevElemIndex;
            removeFromIndex = prevElemIndex = this.bidiRoot.getElementIndex(firstPStart - 1);
            Element prevElem = this.bidiRoot.getElement(prevElemIndex);
            int prevLevel = StyleConstants.getBidiLevel(prevElem.getAttributes());
            if (prevLevel == levels[0]) {
                firstSpanStart = prevElem.getStartOffset();
            } else if (prevElem.getEndOffset() > firstPStart) {
                newElements.add(new BidiElement((Element)this.bidiRoot, prevElem.getStartOffset(), firstPStart, prevLevel));
            } else {
                ++removeFromIndex;
            }
        }
        for (firstSpanEnd = 0; firstSpanEnd < levels.length && levels[firstSpanEnd] == levels[0]; ++firstSpanEnd) {
        }
        int lastSpanEnd = lastPEnd;
        BidiElement newNextElem = null;
        int removeToIndex = this.bidiRoot.getElementCount() - 1;
        if (lastSpanEnd <= this.getLength()) {
            int nextElemIndex;
            removeToIndex = nextElemIndex = this.bidiRoot.getElementIndex(lastPEnd);
            Element nextElem = this.bidiRoot.getElement(nextElemIndex);
            int nextLevel = StyleConstants.getBidiLevel(nextElem.getAttributes());
            if (nextLevel == levels[levels.length - 1]) {
                lastSpanEnd = nextElem.getEndOffset();
            } else if (nextElem.getStartOffset() < lastPEnd) {
                newNextElem = new BidiElement((Element)this.bidiRoot, lastPEnd, nextElem.getEndOffset(), nextLevel);
            } else {
                --removeToIndex;
            }
        }
        for (lastSpanStart = levels.length; lastSpanStart > firstSpanEnd && levels[lastSpanStart - 1] == levels[levels.length - 1]; --lastSpanStart) {
        }
        if (firstSpanEnd == lastSpanStart && levels[0] == levels[levels.length - 1]) {
            newElements.add(new BidiElement((Element)this.bidiRoot, firstSpanStart, lastSpanEnd, (int)levels[0]));
        } else {
            newElements.add(new BidiElement((Element)this.bidiRoot, firstSpanStart, firstSpanEnd + firstPStart, (int)levels[0]));
            int i = firstSpanEnd;
            while (i < lastSpanStart) {
                int j;
                for (j = i; j < levels.length && levels[j] == levels[i]; ++j) {
                }
                newElements.add(new BidiElement((Element)this.bidiRoot, firstPStart + i, firstPStart + j, (int)levels[i]));
                i = j;
            }
            newElements.add(new BidiElement((Element)this.bidiRoot, lastSpanStart + firstPStart, lastSpanEnd, (int)levels[levels.length - 1]));
        }
        if (newNextElem != null) {
            newElements.add(newNextElem);
        }
        int removedElemCount = 0;
        if (this.bidiRoot.getElementCount() > 0) {
            removedElemCount = removeToIndex - removeFromIndex + 1;
        }
        Element[] removedElems = new Element[removedElemCount];
        for (int i = 0; i < removedElemCount; ++i) {
            removedElems[i] = this.bidiRoot.getElement(removeFromIndex + i);
        }
        Element[] addedElems = newElements.toArray(new Element[0]);
        ElementEdit ee = new ElementEdit(this.bidiRoot, removeFromIndex, removedElems, addedElems);
        chng.addEdit(ee);
        this.bidiRoot.replace(removeFromIndex, removedElems.length, addedElems);
    }

    private byte[] calculateBidiLevels(int firstPStart, int lastPEnd) {
        byte[] levels = new byte[lastPEnd - firstPStart];
        int levelsEnd = 0;
        Boolean defaultDirection = null;
        Object d = this.getProperty(TextAttribute.RUN_DIRECTION);
        if (d instanceof Boolean) {
            defaultDirection = (Boolean)d;
        }
        int o = firstPStart;
        while (o < lastPEnd) {
            Element p = this.getParagraphElement(o);
            int pStart = p.getStartOffset();
            int pEnd = p.getEndOffset();
            Boolean direction = defaultDirection;
            d = p.getAttributes().getAttribute(TextAttribute.RUN_DIRECTION);
            if (d instanceof Boolean) {
                direction = (Boolean)d;
            }
            Segment seg = SegmentCache.getSharedSegment();
            try {
                this.getText(pStart, pEnd - pStart, seg);
            }
            catch (BadLocationException e) {
                throw new Error("Internal error: " + e.toString());
            }
            int bidiflag = -2;
            if (direction != null) {
                bidiflag = TextAttribute.RUN_DIRECTION_LTR.equals(direction) ? 0 : 1;
            }
            Bidi bidiAnalyzer = new Bidi(seg.array, seg.offset, null, 0, seg.count, bidiflag);
            BidiUtils.getLevels(bidiAnalyzer, levels, levelsEnd);
            levelsEnd += bidiAnalyzer.getLength();
            o = p.getEndOffset();
            SegmentCache.releaseSharedSegment(seg);
        }
        if (levelsEnd != levels.length) {
            throw new Error("levelsEnd assertion failed.");
        }
        return levels;
    }

    public void dump(PrintStream out) {
        Element root = this.getDefaultRootElement();
        if (root instanceof AbstractElement) {
            ((AbstractElement)root).dump(out, 0);
        }
        this.bidiRoot.dump(out, 0);
    }

    protected final Content getContent() {
        return this.data;
    }

    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        return new LeafElement(parent, a, p0, p1);
    }

    protected Element createBranchElement(Element parent, AttributeSet a) {
        return new BranchElement(parent, a);
    }

    protected final synchronized Thread getCurrentWriter() {
        return this.currWriter;
    }

    protected final synchronized void writeLock() {
        try {
            while (this.numReaders > 0 || this.currWriter != null) {
                if (Thread.currentThread() == this.currWriter) {
                    if (this.notifyingListeners) {
                        throw new IllegalStateException("Attempt to mutate in notification");
                    }
                    ++this.numWriters;
                    return;
                }
                this.wait();
            }
            this.currWriter = Thread.currentThread();
            this.numWriters = 1;
        }
        catch (InterruptedException e) {
            throw new Error("Interrupted attempt to acquire write lock");
        }
    }

    protected final synchronized void writeUnlock() {
        if (--this.numWriters <= 0) {
            this.numWriters = 0;
            this.currWriter = null;
            this.notifyAll();
        }
    }

    public final synchronized void readLock() {
        try {
            while (this.currWriter != null) {
                if (this.currWriter == Thread.currentThread()) {
                    return;
                }
                this.wait();
            }
            ++this.numReaders;
        }
        catch (InterruptedException e) {
            throw new Error("Interrupted attempt to acquire read lock");
        }
    }

    public final synchronized void readUnlock() {
        if (this.currWriter == Thread.currentThread()) {
            return;
        }
        if (this.numReaders <= 0) {
            throw new StateInvariantError(BAD_LOCK_STATE);
        }
        --this.numReaders;
        this.notify();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField f = s.readFields();
        this.documentProperties = (Dictionary)f.get("documentProperties", null);
        this.listenerList = new EventListenerList();
        this.data = (Content)f.get("data", null);
        this.context = (AttributeContext)f.get("context", null);
        this.documentFilter = (DocumentFilter)f.get("documentFilter", null);
        this.bidiRoot = new BidiRootElement();
        try {
            this.writeLock();
            Element[] p = new Element[]{new BidiElement((Element)this.bidiRoot, 0, 1, 0)};
            this.bidiRoot.replace(0, 0, p);
        }
        finally {
            this.writeUnlock();
        }
        s.registerValidation(new ObjectInputValidation(){

            @Override
            public void validateObject() {
                try {
                    AbstractDocument.this.writeLock();
                    DefaultDocumentEvent e = new DefaultDocumentEvent(0, AbstractDocument.this.getLength(), DocumentEvent.EventType.INSERT);
                    AbstractDocument.this.updateBidi(e);
                }
                finally {
                    AbstractDocument.this.writeUnlock();
                }
            }
        }, 0);
    }

    static {
        MultiByteProperty = "multiByte";
    }

    public static interface Content {
        public Position createPosition(int var1) throws BadLocationException;

        public int length();

        public UndoableEdit insertString(int var1, String var2) throws BadLocationException;

        public UndoableEdit remove(int var1, int var2) throws BadLocationException;

        public String getString(int var1, int var2) throws BadLocationException;

        public void getChars(int var1, int var2, Segment var3) throws BadLocationException;
    }

    public static interface AttributeContext {
        public AttributeSet addAttribute(AttributeSet var1, Object var2, Object var3);

        public AttributeSet addAttributes(AttributeSet var1, AttributeSet var2);

        public AttributeSet removeAttribute(AttributeSet var1, Object var2);

        public AttributeSet removeAttributes(AttributeSet var1, Enumeration<?> var2);

        public AttributeSet removeAttributes(AttributeSet var1, AttributeSet var2);

        public AttributeSet getEmptySet();

        public void reclaim(AttributeSet var1);
    }

    class BidiRootElement
    extends BranchElement {
        BidiRootElement() {
            super(null, null);
        }

        @Override
        public String getName() {
            return "bidi root";
        }
    }

    public class BranchElement
    extends AbstractElement {
        private AbstractElement[] children;
        private int nchildren;
        private int lastIndex;

        public BranchElement(Element parent, AttributeSet a) {
            super(parent, a);
            this.children = new AbstractElement[1];
            this.nchildren = 0;
            this.lastIndex = -1;
        }

        public Element positionToElement(int pos) {
            int index = this.getElementIndex(pos);
            AbstractElement child = this.children[index];
            int p0 = child.getStartOffset();
            int p1 = child.getEndOffset();
            if (pos >= p0 && pos < p1) {
                return child;
            }
            return null;
        }

        public void replace(int offset, int length, Element[] elems) {
            int delta = elems.length - length;
            int src = offset + length;
            int nmove = this.nchildren - src;
            int dest = src + delta;
            if (this.nchildren + delta >= this.children.length) {
                int newLength = Math.max(2 * this.children.length, this.nchildren + delta);
                AbstractElement[] newChildren = new AbstractElement[newLength];
                System.arraycopy(this.children, 0, newChildren, 0, offset);
                System.arraycopy(elems, 0, newChildren, offset, elems.length);
                System.arraycopy(this.children, src, newChildren, dest, nmove);
                this.children = newChildren;
            } else {
                System.arraycopy(this.children, src, this.children, dest, nmove);
                System.arraycopy(elems, 0, this.children, offset, elems.length);
            }
            this.nchildren += delta;
        }

        public String toString() {
            return "BranchElement(" + this.getName() + ") " + this.getStartOffset() + "," + this.getEndOffset() + "\n";
        }

        @Override
        public String getName() {
            String nm = super.getName();
            if (nm == null) {
                nm = AbstractDocument.ParagraphElementName;
            }
            return nm;
        }

        @Override
        public int getStartOffset() {
            return this.children[0].getStartOffset();
        }

        @Override
        public int getEndOffset() {
            AbstractElement child = this.nchildren > 0 ? this.children[this.nchildren - 1] : this.children[0];
            return child.getEndOffset();
        }

        @Override
        public Element getElement(int index) {
            if (index < this.nchildren) {
                return this.children[index];
            }
            return null;
        }

        @Override
        public int getElementCount() {
            return this.nchildren;
        }

        @Override
        public int getElementIndex(int offset) {
            int p1;
            int lower = 0;
            int upper = this.nchildren - 1;
            int mid = 0;
            int p0 = this.getStartOffset();
            if (this.nchildren == 0) {
                return 0;
            }
            if (offset >= this.getEndOffset()) {
                return this.nchildren - 1;
            }
            if (this.lastIndex >= lower && this.lastIndex <= upper) {
                AbstractElement lastHit = this.children[this.lastIndex];
                p0 = lastHit.getStartOffset();
                p1 = lastHit.getEndOffset();
                if (offset >= p0 && offset < p1) {
                    return this.lastIndex;
                }
                if (offset < p0) {
                    upper = this.lastIndex;
                } else {
                    lower = this.lastIndex;
                }
            }
            while (lower <= upper) {
                mid = lower + (upper - lower) / 2;
                AbstractElement elem = this.children[mid];
                p0 = elem.getStartOffset();
                p1 = elem.getEndOffset();
                if (offset >= p0 && offset < p1) {
                    int index;
                    this.lastIndex = index = mid;
                    return index;
                }
                if (offset < p0) {
                    upper = mid - 1;
                    continue;
                }
                lower = mid + 1;
            }
            int index = offset < p0 ? mid : mid + 1;
            this.lastIndex = index;
            return index;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public Enumeration<TreeNode> children() {
            if (this.nchildren == 0) {
                return null;
            }
            Vector<AbstractElement> tempVector = new Vector<AbstractElement>(this.nchildren);
            for (int counter = 0; counter < this.nchildren; ++counter) {
                tempVector.addElement(this.children[counter]);
            }
            return tempVector.elements();
        }
    }

    class BidiElement
    extends LeafElement {
        BidiElement(Element parent, int start, int end, int level) {
            super(parent, new SimpleAttributeSet(), start, end);
            this.addAttribute(StyleConstants.BidiLevel, level);
        }

        @Override
        public String getName() {
            return AbstractDocument.BidiElementName;
        }

        int getLevel() {
            Integer o = (Integer)this.getAttribute(StyleConstants.BidiLevel);
            if (o != null) {
                return o;
            }
            return 0;
        }

        boolean isLeftToRight() {
            return this.getLevel() % 2 == 0;
        }
    }

    public class DefaultDocumentEvent
    extends CompoundEdit
    implements DocumentEvent {
        private int offset;
        private int length;
        private Hashtable<Element, DocumentEvent.ElementChange> changeLookup;
        private DocumentEvent.EventType type;

        public DefaultDocumentEvent(int offs, int len, DocumentEvent.EventType type) {
            this.offset = offs;
            this.length = len;
            this.type = type;
        }

        @Override
        public String toString() {
            return this.edits.toString();
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (this.changeLookup == null && this.edits.size() > 10) {
                this.changeLookup = new Hashtable();
                int n = this.edits.size();
                for (int i = 0; i < n; ++i) {
                    Object o = this.edits.elementAt(i);
                    if (!(o instanceof DocumentEvent.ElementChange)) continue;
                    DocumentEvent.ElementChange ec = (DocumentEvent.ElementChange)o;
                    this.changeLookup.put(ec.getElement(), ec);
                }
            }
            if (this.changeLookup != null && anEdit instanceof DocumentEvent.ElementChange) {
                DocumentEvent.ElementChange ec = (DocumentEvent.ElementChange)((Object)anEdit);
                this.changeLookup.put(ec.getElement(), ec);
            }
            return super.addEdit(anEdit);
        }

        @Override
        public void redo() throws CannotRedoException {
            AbstractDocument.this.writeLock();
            try {
                super.redo();
                UndoRedoDocumentEvent ev = new UndoRedoDocumentEvent(AbstractDocument.this, this, false);
                if (this.type == DocumentEvent.EventType.INSERT) {
                    AbstractDocument.this.fireInsertUpdate(ev);
                } else if (this.type == DocumentEvent.EventType.REMOVE) {
                    AbstractDocument.this.fireRemoveUpdate(ev);
                } else {
                    AbstractDocument.this.fireChangedUpdate(ev);
                }
            }
            finally {
                AbstractDocument.this.writeUnlock();
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            AbstractDocument.this.writeLock();
            try {
                super.undo();
                UndoRedoDocumentEvent ev = new UndoRedoDocumentEvent(AbstractDocument.this, this, true);
                if (this.type == DocumentEvent.EventType.REMOVE) {
                    AbstractDocument.this.fireInsertUpdate(ev);
                } else if (this.type == DocumentEvent.EventType.INSERT) {
                    AbstractDocument.this.fireRemoveUpdate(ev);
                } else {
                    AbstractDocument.this.fireChangedUpdate(ev);
                }
            }
            finally {
                AbstractDocument.this.writeUnlock();
            }
        }

        @Override
        public boolean isSignificant() {
            return true;
        }

        @Override
        public String getPresentationName() {
            DocumentEvent.EventType type = this.getType();
            if (type == DocumentEvent.EventType.INSERT) {
                return UIManager.getString("AbstractDocument.additionText");
            }
            if (type == DocumentEvent.EventType.REMOVE) {
                return UIManager.getString("AbstractDocument.deletionText");
            }
            return UIManager.getString("AbstractDocument.styleChangeText");
        }

        @Override
        public String getUndoPresentationName() {
            return UIManager.getString("AbstractDocument.undoText") + " " + this.getPresentationName();
        }

        @Override
        public String getRedoPresentationName() {
            return UIManager.getString("AbstractDocument.redoText") + " " + this.getPresentationName();
        }

        @Override
        public DocumentEvent.EventType getType() {
            return this.type;
        }

        @Override
        public int getOffset() {
            return this.offset;
        }

        @Override
        public int getLength() {
            return this.length;
        }

        @Override
        public Document getDocument() {
            return AbstractDocument.this;
        }

        @Override
        public DocumentEvent.ElementChange getChange(Element elem) {
            if (this.changeLookup != null) {
                return this.changeLookup.get(elem);
            }
            int n = this.edits.size();
            for (int i = 0; i < n; ++i) {
                DocumentEvent.ElementChange c;
                Object o = this.edits.elementAt(i);
                if (!(o instanceof DocumentEvent.ElementChange) || !elem.equals((c = (DocumentEvent.ElementChange)o).getElement())) continue;
                return c;
            }
            return null;
        }
    }

    class DefaultDocumentEventUndoableWrapper
    extends DefaultDocumentEvent
    implements UndoableEdit,
    UndoableEditLockSupport {
        final DefaultDocumentEvent dde;

        public DefaultDocumentEventUndoableWrapper(DefaultDocumentEvent dde) {
            super(dde.getOffset(), dde.getLength(), dde.type);
            this.dde = dde;
        }

        @Override
        public void undo() throws CannotUndoException {
            this.dde.undo();
        }

        @Override
        public boolean canUndo() {
            return this.dde.canUndo();
        }

        @Override
        public void redo() throws CannotRedoException {
            this.dde.redo();
        }

        @Override
        public boolean canRedo() {
            return this.dde.canRedo();
        }

        @Override
        public void die() {
            this.dde.die();
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            return this.dde.addEdit(anEdit);
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            return this.dde.replaceEdit(anEdit);
        }

        @Override
        public boolean isSignificant() {
            return this.dde.isSignificant();
        }

        @Override
        public String getPresentationName() {
            return this.dde.getPresentationName();
        }

        @Override
        public String getUndoPresentationName() {
            return this.dde.getUndoPresentationName();
        }

        @Override
        public String getRedoPresentationName() {
            return this.dde.getRedoPresentationName();
        }

        @Override
        public void lockEdit() {
            ((AbstractDocument)this.dde.getDocument()).writeLock();
        }

        @Override
        public void unlockEdit() {
            ((AbstractDocument)this.dde.getDocument()).writeUnlock();
        }
    }

    private class DefaultFilterBypass
    extends DocumentFilter.FilterBypass {
        private DefaultFilterBypass() {
        }

        @Override
        public Document getDocument() {
            return AbstractDocument.this;
        }

        @Override
        public void remove(int offset, int length) throws BadLocationException {
            AbstractDocument.this.handleRemove(offset, length);
        }

        @Override
        public void insertString(int offset, String string, AttributeSet attr) throws BadLocationException {
            AbstractDocument.this.handleInsertString(offset, string, attr);
        }

        @Override
        public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            AbstractDocument.this.handleRemove(offset, length);
            AbstractDocument.this.handleInsertString(offset, text, attrs);
        }
    }

    public static class ElementEdit
    extends AbstractUndoableEdit
    implements DocumentEvent.ElementChange {
        private Element e;
        private int index;
        private Element[] removed;
        private Element[] added;

        public ElementEdit(Element e, int index, Element[] removed, Element[] added) {
            this.e = e;
            this.index = index;
            this.removed = removed;
            this.added = added;
        }

        @Override
        public Element getElement() {
            return this.e;
        }

        @Override
        public int getIndex() {
            return this.index;
        }

        @Override
        public Element[] getChildrenRemoved() {
            return this.removed;
        }

        @Override
        public Element[] getChildrenAdded() {
            return this.added;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            Element[] tmp = this.removed;
            this.removed = this.added;
            this.added = tmp;
            ((BranchElement)this.e).replace(this.index, this.removed.length, this.added);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            ((BranchElement)this.e).replace(this.index, this.added.length, this.removed);
            Element[] tmp = this.removed;
            this.removed = this.added;
            this.added = tmp;
        }
    }

    public abstract class AbstractElement
    implements Element,
    MutableAttributeSet,
    Serializable,
    TreeNode {
        private Element parent;
        private transient AttributeSet attributes;

        public AbstractElement(Element parent, AttributeSet a) {
            this.parent = parent;
            this.attributes = AbstractDocument.this.getAttributeContext().getEmptySet();
            if (a != null) {
                this.addAttributes(a);
            }
        }

        private void indent(PrintWriter out, int n) {
            for (int i = 0; i < n; ++i) {
                out.print("  ");
            }
        }

        public void dump(PrintStream psOut, int indentAmount) {
            PrintWriter out;
            try {
                out = new PrintWriter((Writer)new OutputStreamWriter((OutputStream)psOut, "JavaEsc"), true);
            }
            catch (UnsupportedEncodingException e) {
                out = new PrintWriter(psOut, true);
            }
            this.indent(out, indentAmount);
            if (this.getName() == null) {
                out.print("<??");
            } else {
                out.print("<" + this.getName());
            }
            if (this.getAttributeCount() > 0) {
                out.println("");
                Enumeration<?> names = this.attributes.getAttributeNames();
                while (names.hasMoreElements()) {
                    Object name = names.nextElement();
                    this.indent(out, indentAmount + 1);
                    out.println(String.valueOf(name) + "=" + String.valueOf(this.getAttribute(name)));
                }
                this.indent(out, indentAmount);
            }
            out.println(">");
            if (this.isLeaf()) {
                this.indent(out, indentAmount + 1);
                out.print("[" + this.getStartOffset() + "," + this.getEndOffset() + "]");
                Content c = AbstractDocument.this.getContent();
                try {
                    Object contentStr = c.getString(this.getStartOffset(), this.getEndOffset() - this.getStartOffset());
                    if (((String)contentStr).length() > 40) {
                        contentStr = ((String)contentStr).substring(0, 40) + "...";
                    }
                    out.println("[" + (String)contentStr + "]");
                }
                catch (BadLocationException contentStr) {}
            } else {
                int n = this.getElementCount();
                for (int i = 0; i < n; ++i) {
                    AbstractElement e = (AbstractElement)this.getElement(i);
                    e.dump(psOut, indentAmount + 1);
                }
            }
        }

        @Override
        public int getAttributeCount() {
            return this.attributes.getAttributeCount();
        }

        @Override
        public boolean isDefined(Object attrName) {
            return this.attributes.isDefined(attrName);
        }

        @Override
        public boolean isEqual(AttributeSet attr) {
            return this.attributes.isEqual(attr);
        }

        @Override
        public AttributeSet copyAttributes() {
            return this.attributes.copyAttributes();
        }

        @Override
        public Object getAttribute(Object attrName) {
            Object value = this.attributes.getAttribute(attrName);
            if (value == null) {
                AttributeSet a;
                AttributeSet attributeSet = a = this.parent != null ? this.parent.getAttributes() : null;
                if (a != null) {
                    value = a.getAttribute(attrName);
                }
            }
            return value;
        }

        @Override
        public Enumeration<?> getAttributeNames() {
            return this.attributes.getAttributeNames();
        }

        @Override
        public boolean containsAttribute(Object name, Object value) {
            return this.attributes.containsAttribute(name, value);
        }

        @Override
        public boolean containsAttributes(AttributeSet attrs) {
            return this.attributes.containsAttributes(attrs);
        }

        @Override
        public AttributeSet getResolveParent() {
            AttributeSet a = this.attributes.getResolveParent();
            if (a == null && this.parent != null) {
                a = this.parent.getAttributes();
            }
            return a;
        }

        @Override
        public void addAttribute(Object name, Object value) {
            this.checkForIllegalCast();
            AttributeContext context = AbstractDocument.this.getAttributeContext();
            this.attributes = context.addAttribute(this.attributes, name, value);
        }

        @Override
        public void addAttributes(AttributeSet attr) {
            this.checkForIllegalCast();
            AttributeContext context = AbstractDocument.this.getAttributeContext();
            this.attributes = context.addAttributes(this.attributes, attr);
        }

        @Override
        public void removeAttribute(Object name) {
            this.checkForIllegalCast();
            AttributeContext context = AbstractDocument.this.getAttributeContext();
            this.attributes = context.removeAttribute(this.attributes, name);
        }

        @Override
        public void removeAttributes(Enumeration<?> names) {
            this.checkForIllegalCast();
            AttributeContext context = AbstractDocument.this.getAttributeContext();
            this.attributes = context.removeAttributes(this.attributes, names);
        }

        @Override
        public void removeAttributes(AttributeSet attrs) {
            this.checkForIllegalCast();
            AttributeContext context = AbstractDocument.this.getAttributeContext();
            this.attributes = attrs == this ? context.getEmptySet() : context.removeAttributes(this.attributes, attrs);
        }

        @Override
        public void setResolveParent(AttributeSet parent) {
            this.checkForIllegalCast();
            AttributeContext context = AbstractDocument.this.getAttributeContext();
            this.attributes = parent != null ? context.addAttribute(this.attributes, StyleConstants.ResolveAttribute, parent) : context.removeAttribute(this.attributes, StyleConstants.ResolveAttribute);
        }

        private void checkForIllegalCast() {
            Thread t = AbstractDocument.this.getCurrentWriter();
            if (t == null || t != Thread.currentThread()) {
                throw new StateInvariantError("Illegal cast to MutableAttributeSet");
            }
        }

        @Override
        public Document getDocument() {
            return AbstractDocument.this;
        }

        @Override
        public Element getParentElement() {
            return this.parent;
        }

        @Override
        public AttributeSet getAttributes() {
            return this;
        }

        @Override
        public String getName() {
            if (this.attributes.isDefined(AbstractDocument.ElementNameAttribute)) {
                return (String)this.attributes.getAttribute(AbstractDocument.ElementNameAttribute);
            }
            return null;
        }

        @Override
        public abstract int getStartOffset();

        @Override
        public abstract int getEndOffset();

        @Override
        public abstract Element getElement(int var1);

        @Override
        public abstract int getElementCount();

        @Override
        public abstract int getElementIndex(int var1);

        @Override
        public abstract boolean isLeaf();

        @Override
        public TreeNode getChildAt(int childIndex) {
            return (TreeNode)((Object)this.getElement(childIndex));
        }

        @Override
        public int getChildCount() {
            return this.getElementCount();
        }

        @Override
        public TreeNode getParent() {
            return (TreeNode)((Object)this.getParentElement());
        }

        @Override
        public int getIndex(TreeNode node) {
            for (int counter = this.getChildCount() - 1; counter >= 0; --counter) {
                if (this.getChildAt(counter) != node) continue;
                return counter;
            }
            return -1;
        }

        @Override
        public abstract boolean getAllowsChildren();

        public abstract Enumeration<TreeNode> children();

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            StyleContext.writeAttributeSet(s, this.attributes);
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleContext.readAttributeSet(s, attr);
            AttributeContext context = AbstractDocument.this.getAttributeContext();
            this.attributes = context.addAttributes(SimpleAttributeSet.EMPTY, attr);
        }
    }

    public class LeafElement
    extends AbstractElement {
        private transient Position p0;
        private transient Position p1;

        public LeafElement(Element parent, AttributeSet a, int offs0, int offs1) {
            super(parent, a);
            try {
                this.p0 = AbstractDocument.this.createPosition(offs0);
                this.p1 = AbstractDocument.this.createPosition(offs1);
            }
            catch (BadLocationException e) {
                this.p0 = null;
                this.p1 = null;
                throw new StateInvariantError("Can't create Position references");
            }
        }

        public String toString() {
            return "LeafElement(" + this.getName() + ") " + String.valueOf(this.p0) + "," + String.valueOf(this.p1) + "\n";
        }

        @Override
        public int getStartOffset() {
            return this.p0.getOffset();
        }

        @Override
        public int getEndOffset() {
            return this.p1.getOffset();
        }

        @Override
        public String getName() {
            String nm = super.getName();
            if (nm == null) {
                nm = AbstractDocument.ContentElementName;
            }
            return nm;
        }

        @Override
        public int getElementIndex(int pos) {
            return -1;
        }

        @Override
        public Element getElement(int index) {
            return null;
        }

        @Override
        public int getElementCount() {
            return 0;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

        @Override
        public Enumeration<TreeNode> children() {
            return null;
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(this.p0.getOffset());
            s.writeInt(this.p1.getOffset());
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            int off0 = s.readInt();
            int off1 = s.readInt();
            try {
                this.p0 = AbstractDocument.this.createPosition(off0);
                this.p1 = AbstractDocument.this.createPosition(off1);
            }
            catch (BadLocationException e) {
                this.p0 = null;
                this.p1 = null;
                throw new IOException("Can't restore Position references");
            }
        }
    }

    class UndoRedoDocumentEvent
    implements DocumentEvent {
        private DefaultDocumentEvent src = null;
        private DocumentEvent.EventType type = null;

        public UndoRedoDocumentEvent(AbstractDocument this$0, DefaultDocumentEvent src, boolean isUndo) {
            this.src = src;
            this.type = isUndo ? (src.getType().equals(DocumentEvent.EventType.INSERT) ? DocumentEvent.EventType.REMOVE : (src.getType().equals(DocumentEvent.EventType.REMOVE) ? DocumentEvent.EventType.INSERT : src.getType())) : src.getType();
        }

        public DefaultDocumentEvent getSource() {
            return this.src;
        }

        @Override
        public int getOffset() {
            return this.src.getOffset();
        }

        @Override
        public int getLength() {
            return this.src.getLength();
        }

        @Override
        public Document getDocument() {
            return this.src.getDocument();
        }

        @Override
        public DocumentEvent.EventType getType() {
            return this.type;
        }

        @Override
        public DocumentEvent.ElementChange getChange(Element elem) {
            return this.src.getChange(elem);
        }
    }
}

