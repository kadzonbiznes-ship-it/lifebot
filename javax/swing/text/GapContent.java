/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapVector;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class GapContent
extends GapVector
implements AbstractDocument.Content,
Serializable {
    private static final char[] empty = new char[0];
    private transient MarkVector marks;
    private transient MarkData search;
    private transient int unusedMarks = 0;
    private transient ReferenceQueue<StickyPosition> queue;
    static final int GROWTH_SIZE = 524288;

    public GapContent() {
        this(10);
    }

    public GapContent(int initialLength) {
        super(Math.max(initialLength, 2));
        char[] implied = new char[]{'\n'};
        this.replace(0, 0, implied, implied.length);
        this.marks = new MarkVector();
        this.search = new MarkData(0);
        this.queue = new ReferenceQueue();
    }

    @Override
    protected Object allocateArray(int len) {
        return new char[len];
    }

    @Override
    protected int getArrayLength() {
        char[] carray = (char[])this.getArray();
        return carray.length;
    }

    @Override
    void resize(int nsize) {
        char[] carray = (char[])this.getArray();
        super.resize(nsize);
        Arrays.fill(carray, '\u0000');
    }

    @Override
    public int length() {
        int len = this.getArrayLength() - (this.getGapEnd() - this.getGapStart());
        return len;
    }

    @Override
    public UndoableEdit insertString(int where, String str) throws BadLocationException {
        if (where > this.length() || where < 0) {
            throw new BadLocationException("Invalid insert", this.length());
        }
        char[] chars = str.toCharArray();
        this.replace(where, 0, chars, chars.length);
        return new InsertUndo(where, str.length());
    }

    @Override
    public UndoableEdit remove(int where, int nitems) throws BadLocationException {
        if (where + nitems >= this.length()) {
            throw new BadLocationException("Invalid remove", this.length() + 1);
        }
        String removedString = this.getString(where, nitems);
        RemoveUndo edit = new RemoveUndo(where, removedString);
        this.replace(where, nitems, empty, 0);
        return edit;
    }

    @Override
    public String getString(int where, int len) throws BadLocationException {
        Segment s = new Segment();
        this.getChars(where, len, s);
        return new String(s.array, s.offset, s.count);
    }

    @Override
    public void getChars(int where, int len, Segment chars) throws BadLocationException {
        int end = where + len;
        if (where < 0 || end < 0) {
            throw new BadLocationException("Invalid location", -1);
        }
        if (end > this.length() || where > this.length()) {
            throw new BadLocationException("Invalid location", this.length() + 1);
        }
        int g0 = this.getGapStart();
        int g1 = this.getGapEnd();
        char[] array = (char[])this.getArray();
        if (where + len <= g0) {
            chars.array = array;
            chars.copy = false;
            chars.offset = where;
        } else if (where >= g0) {
            chars.array = array;
            chars.copy = false;
            chars.offset = g1 + where - g0;
        } else {
            int before = g0 - where;
            if (chars.isPartialReturn()) {
                chars.array = array;
                chars.copy = false;
                chars.offset = where;
                chars.count = before;
                return;
            }
            chars.array = new char[len];
            chars.copy = true;
            chars.offset = 0;
            System.arraycopy(array, where, chars.array, 0, before);
            System.arraycopy(array, g1, chars.array, before, len - before);
        }
        chars.count = len;
    }

    @Override
    public Position createPosition(int offset) throws BadLocationException {
        StickyPosition position;
        block6: {
            MarkData m;
            int sortIndex;
            int index;
            block5: {
                while (this.queue.poll() != null) {
                    ++this.unusedMarks;
                }
                if (this.unusedMarks > Math.max(5, this.marks.size() / 10)) {
                    this.removeUnusedMarks();
                }
                int g0 = this.getGapStart();
                int g1 = this.getGapEnd();
                this.search.index = index = offset < g0 ? offset : offset + (g1 - g0);
                sortIndex = this.findSortIndex(this.search);
                if (sortIndex >= this.marks.size()) break block5;
                m = this.marks.elementAt(sortIndex);
                if (m.index == index && (position = m.getPosition()) != null) break block6;
            }
            position = new StickyPosition(this);
            m = new MarkData(index, position, this.queue);
            position.setMark(m);
            this.marks.insertElementAt(m, sortIndex);
        }
        return position;
    }

    @Override
    protected void shiftEnd(int newSize) {
        int oldGapEnd = this.getGapEnd();
        super.shiftEnd(newSize);
        int dg = this.getGapEnd() - oldGapEnd;
        int adjustIndex = this.findMarkAdjustIndex(oldGapEnd);
        int n = this.marks.size();
        for (int i = adjustIndex; i < n; ++i) {
            MarkData mark = this.marks.elementAt(i);
            mark.index += dg;
        }
    }

    @Override
    int getNewArraySize(int reqSize) {
        if (reqSize < 524288) {
            return super.getNewArraySize(reqSize);
        }
        return reqSize + 524288;
    }

    @Override
    protected void shiftGap(int newGapStart) {
        int oldGapStart = this.getGapStart();
        int dg = newGapStart - oldGapStart;
        int oldGapEnd = this.getGapEnd();
        int newGapEnd = oldGapEnd + dg;
        int gapSize = oldGapEnd - oldGapStart;
        super.shiftGap(newGapStart);
        if (dg > 0) {
            int adjustIndex = this.findMarkAdjustIndex(oldGapStart);
            int n = this.marks.size();
            for (int i = adjustIndex; i < n; ++i) {
                MarkData mark = this.marks.elementAt(i);
                if (mark.index < newGapEnd) {
                    mark.index -= gapSize;
                    continue;
                }
                break;
            }
        } else if (dg < 0) {
            int adjustIndex = this.findMarkAdjustIndex(newGapStart);
            int n = this.marks.size();
            for (int i = adjustIndex; i < n; ++i) {
                MarkData mark = this.marks.elementAt(i);
                if (mark.index < oldGapEnd) {
                    mark.index += gapSize;
                    continue;
                }
                break;
            }
        }
        this.resetMarksAtZero();
    }

    protected void resetMarksAtZero() {
        if (this.marks != null && this.getGapStart() == 0) {
            int g1 = this.getGapEnd();
            int maxCounter = this.marks.size();
            for (int counter = 0; counter < maxCounter; ++counter) {
                MarkData mark = this.marks.elementAt(counter);
                if (mark.index > g1) break;
                mark.index = 0;
            }
        }
    }

    @Override
    protected void shiftGapStartDown(int newGapStart) {
        int adjustIndex = this.findMarkAdjustIndex(newGapStart);
        int n = this.marks.size();
        int g0 = this.getGapStart();
        int g1 = this.getGapEnd();
        for (int i = adjustIndex; i < n; ++i) {
            MarkData mark = this.marks.elementAt(i);
            if (mark.index > g0) break;
            mark.index = g1;
        }
        super.shiftGapStartDown(newGapStart);
        this.resetMarksAtZero();
    }

    @Override
    protected void shiftGapEndUp(int newGapEnd) {
        int adjustIndex = this.findMarkAdjustIndex(this.getGapEnd());
        int n = this.marks.size();
        for (int i = adjustIndex; i < n; ++i) {
            MarkData mark = this.marks.elementAt(i);
            if (mark.index >= newGapEnd) break;
            mark.index = newGapEnd;
        }
        super.shiftGapEndUp(newGapEnd);
        this.resetMarksAtZero();
    }

    final int compare(MarkData o1, MarkData o2) {
        return Integer.compare(o1.index, o2.index);
    }

    final int findMarkAdjustIndex(int searchIndex) {
        this.search.index = Math.max(searchIndex, 1);
        int index = this.findSortIndex(this.search);
        for (int i = index - 1; i >= 0; --i) {
            MarkData d = this.marks.elementAt(i);
            if (d.index != this.search.index) break;
            --index;
        }
        return index;
    }

    final int findSortIndex(MarkData o) {
        int lower = 0;
        int upper = this.marks.size() - 1;
        int mid = 0;
        if (upper == -1) {
            return 0;
        }
        MarkData last = this.marks.elementAt(upper);
        int cmp = this.compare(o, last);
        if (cmp > 0) {
            return upper + 1;
        }
        while (lower <= upper) {
            mid = lower + (upper - lower) / 2;
            MarkData entry = this.marks.elementAt(mid);
            cmp = this.compare(o, entry);
            if (cmp == 0) {
                return mid;
            }
            if (cmp < 0) {
                upper = mid - 1;
                continue;
            }
            lower = mid + 1;
        }
        return cmp < 0 ? mid : mid + 1;
    }

    final void removeUnusedMarks() {
        int n = this.marks.size();
        MarkVector cleaned = new MarkVector(n);
        for (int i = 0; i < n; ++i) {
            MarkData mark = this.marks.elementAt(i);
            if (mark.get() == null) continue;
            cleaned.addElement(mark);
        }
        this.marks = cleaned;
        this.unusedMarks = 0;
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        this.marks = new MarkVector();
        this.search = new MarkData(0);
        this.queue = new ReferenceQueue();
    }

    protected Vector getPositionsInRange(Vector v, int offset, int length) {
        int endIndex;
        int startIndex;
        int endOffset = offset + length;
        int g0 = this.getGapStart();
        int g1 = this.getGapEnd();
        if (offset < g0) {
            startIndex = offset == 0 ? 0 : this.findMarkAdjustIndex(offset);
            endIndex = endOffset >= g0 ? this.findMarkAdjustIndex(endOffset + (g1 - g0) + 1) : this.findMarkAdjustIndex(endOffset + 1);
        } else {
            startIndex = this.findMarkAdjustIndex(offset + (g1 - g0));
            endIndex = this.findMarkAdjustIndex(endOffset + (g1 - g0) + 1);
        }
        Vector placeIn = v == null ? new Vector(Math.max(1, endIndex - startIndex)) : v;
        for (int counter = startIndex; counter < endIndex; ++counter) {
            placeIn.addElement(new UndoPosRef(this, this.marks.elementAt(counter)));
        }
        return placeIn;
    }

    protected void updateUndoPositions(Vector positions, int offset, int length) {
        int endOffset = offset + length;
        int g1 = this.getGapEnd();
        int endIndex = this.findMarkAdjustIndex(g1 + 1);
        int startIndex = offset != 0 ? this.findMarkAdjustIndex(g1) : 0;
        for (int counter = positions.size() - 1; counter >= 0; --counter) {
            UndoPosRef ref = (UndoPosRef)positions.elementAt(counter);
            ref.resetLocation(endOffset, g1);
        }
        if (startIndex < endIndex) {
            Object[] sorted = new Object[endIndex - startIndex];
            int addIndex = 0;
            if (offset == 0) {
                MarkData mark;
                int counter;
                for (counter = startIndex; counter < endIndex; ++counter) {
                    mark = this.marks.elementAt(counter);
                    if (mark.index != 0) continue;
                    sorted[addIndex++] = mark;
                }
                for (counter = startIndex; counter < endIndex; ++counter) {
                    mark = this.marks.elementAt(counter);
                    if (mark.index == 0) continue;
                    sorted[addIndex++] = mark;
                }
            } else {
                MarkData mark;
                int counter;
                for (counter = startIndex; counter < endIndex; ++counter) {
                    mark = this.marks.elementAt(counter);
                    if (mark.index == g1) continue;
                    sorted[addIndex++] = mark;
                }
                for (counter = startIndex; counter < endIndex; ++counter) {
                    mark = this.marks.elementAt(counter);
                    if (mark.index != g1) continue;
                    sorted[addIndex++] = mark;
                }
            }
            this.marks.replaceRange(startIndex, endIndex, sorted);
        }
    }

    static class MarkVector
    extends GapVector {
        MarkData[] oneMark = new MarkData[1];

        MarkVector() {
        }

        MarkVector(int size) {
            super(size);
        }

        @Override
        protected Object allocateArray(int len) {
            return new MarkData[len];
        }

        @Override
        protected int getArrayLength() {
            MarkData[] marks = (MarkData[])this.getArray();
            return marks.length;
        }

        public int size() {
            int len = this.getArrayLength() - (this.getGapEnd() - this.getGapStart());
            return len;
        }

        public void insertElementAt(MarkData m, int index) {
            this.oneMark[0] = m;
            this.replace(index, 0, this.oneMark, 1);
        }

        public void addElement(MarkData m) {
            this.insertElementAt(m, this.size());
        }

        public MarkData elementAt(int index) {
            int g0 = this.getGapStart();
            int g1 = this.getGapEnd();
            MarkData[] array = (MarkData[])this.getArray();
            if (index < g0) {
                return array[index];
            }
            return array[index += g1 - g0];
        }

        protected void replaceRange(int start, int end, Object[] marks) {
            int g0 = this.getGapStart();
            int g1 = this.getGapEnd();
            int index = start;
            int newIndex = 0;
            Object[] array = (Object[])this.getArray();
            if (start >= g0) {
                index += g1 - g0;
                end += g1 - g0;
            } else if (end >= g0) {
                end += g1 - g0;
                while (index < g0) {
                    array[index++] = marks[newIndex++];
                }
                index = g1;
            } else {
                while (index < end) {
                    array[index++] = marks[newIndex++];
                }
            }
            while (index < end) {
                array[index++] = marks[newIndex++];
            }
        }
    }

    final class MarkData
    extends WeakReference<StickyPosition> {
        int index;

        MarkData(int index) {
            super(null);
            this.index = index;
        }

        MarkData(int index, StickyPosition position, ReferenceQueue<? super StickyPosition> queue) {
            super(position, queue);
            this.index = index;
        }

        public int getOffset() {
            int g0 = GapContent.this.getGapStart();
            int g1 = GapContent.this.getGapEnd();
            int offs = this.index < g0 ? this.index : this.index - (g1 - g0);
            return Math.max(offs, 0);
        }

        StickyPosition getPosition() {
            return (StickyPosition)this.get();
        }
    }

    class InsertUndo
    extends AbstractUndoableEdit {
        protected int offset;
        protected int length;
        protected String string;
        protected Vector posRefs;

        protected InsertUndo(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            try {
                this.posRefs = GapContent.this.getPositionsInRange(null, this.offset, this.length);
                this.string = GapContent.this.getString(this.offset, this.length);
                GapContent.this.remove(this.offset, this.length);
            }
            catch (BadLocationException bl) {
                throw new CannotUndoException();
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            try {
                GapContent.this.insertString(this.offset, this.string);
                this.string = null;
                if (this.posRefs != null) {
                    GapContent.this.updateUndoPositions(this.posRefs, this.offset, this.length);
                    this.posRefs = null;
                }
            }
            catch (BadLocationException bl) {
                throw new CannotRedoException();
            }
        }
    }

    class RemoveUndo
    extends AbstractUndoableEdit {
        protected int offset;
        protected int length;
        protected String string;
        protected Vector<UndoPosRef> posRefs;

        protected RemoveUndo(int offset, String string) {
            this.offset = offset;
            this.string = string;
            this.length = string.length();
            this.posRefs = GapContent.this.getPositionsInRange(null, offset, this.length);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            try {
                GapContent.this.insertString(this.offset, this.string);
                if (this.posRefs != null) {
                    GapContent.this.updateUndoPositions(this.posRefs, this.offset, this.length);
                    this.posRefs = null;
                }
                this.string = null;
            }
            catch (BadLocationException bl) {
                throw new CannotUndoException();
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            try {
                this.string = GapContent.this.getString(this.offset, this.length);
                this.posRefs = GapContent.this.getPositionsInRange(null, this.offset, this.length);
                GapContent.this.remove(this.offset, this.length);
            }
            catch (BadLocationException bl) {
                throw new CannotRedoException();
            }
        }
    }

    final class StickyPosition
    implements Position {
        MarkData mark;

        StickyPosition(GapContent this$0) {
        }

        void setMark(MarkData mark) {
            this.mark = mark;
        }

        @Override
        public int getOffset() {
            return this.mark.getOffset();
        }

        public String toString() {
            return Integer.toString(this.getOffset());
        }
    }

    final class UndoPosRef {
        protected int undoLocation;
        protected MarkData rec;

        UndoPosRef(GapContent this$0, MarkData rec) {
            this.rec = rec;
            this.undoLocation = rec.getOffset();
        }

        protected void resetLocation(int endOffset, int g1) {
            this.rec.index = this.undoLocation != endOffset ? this.undoLocation : g1;
        }
    }
}

