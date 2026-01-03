/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.util.Vector;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

public class PlainDocument
extends AbstractDocument {
    public static final String tabSizeAttribute = "tabSize";
    public static final String lineLimitAttribute = "lineLimit";
    private AbstractDocument.AbstractElement defaultRoot;
    private Vector<Element> added = new Vector();
    private Vector<Element> removed = new Vector();
    private transient Segment s;

    public PlainDocument() {
        this(new GapContent());
    }

    public PlainDocument(AbstractDocument.Content c) {
        super(c);
        this.putProperty(tabSizeAttribute, 8);
        this.defaultRoot = this.createDefaultRoot();
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        Object filterNewlines = this.getProperty("filterNewlines");
        if (filterNewlines instanceof Boolean && filterNewlines.equals(Boolean.TRUE) && str != null && str.indexOf(10) >= 0) {
            StringBuilder filtered = new StringBuilder(str);
            int n = filtered.length();
            for (int i = 0; i < n; ++i) {
                if (filtered.charAt(i) != '\n') continue;
                filtered.setCharAt(i, ' ');
            }
            str = filtered.toString();
        }
        super.insertString(offs, str, a);
    }

    @Override
    public Element getDefaultRootElement() {
        return this.defaultRoot;
    }

    protected AbstractDocument.AbstractElement createDefaultRoot() {
        AbstractDocument.BranchElement map = (AbstractDocument.BranchElement)this.createBranchElement(null, null);
        Element line = this.createLeafElement(map, null, 0, 1);
        Element[] lines = new Element[]{line};
        map.replace(0, 0, lines);
        return map;
    }

    @Override
    public Element getParagraphElement(int pos) {
        Element lineMap = this.getDefaultRootElement();
        return lineMap.getElement(lineMap.getElementIndex(pos));
    }

    @Override
    protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
        this.removed.removeAllElements();
        this.added.removeAllElements();
        AbstractDocument.BranchElement lineMap = (AbstractDocument.BranchElement)this.getDefaultRootElement();
        int offset = chng.getOffset();
        int length = chng.getLength();
        if (offset > 0) {
            --offset;
            ++length;
        }
        int index = lineMap.getElementIndex(offset);
        Element rmCandidate = lineMap.getElement(index);
        int rmOffs0 = rmCandidate.getStartOffset();
        int rmOffs1 = rmCandidate.getEndOffset();
        int lastOffset = rmOffs0;
        try {
            if (this.s == null) {
                this.s = new Segment();
            }
            this.getContent().getChars(offset, length, this.s);
            boolean hasBreaks = false;
            for (int i = 0; i < length; ++i) {
                char c = this.s.array[this.s.offset + i];
                if (c != '\n') continue;
                int breakOffset = offset + i + 1;
                this.added.addElement(this.createLeafElement(lineMap, null, lastOffset, breakOffset));
                lastOffset = breakOffset;
                hasBreaks = true;
            }
            if (hasBreaks) {
                this.removed.addElement(rmCandidate);
                if (offset + length == rmOffs1 && lastOffset != rmOffs1 && index + 1 < lineMap.getElementCount()) {
                    Element e = lineMap.getElement(index + 1);
                    this.removed.addElement(e);
                    rmOffs1 = e.getEndOffset();
                }
                if (lastOffset < rmOffs1) {
                    this.added.addElement(this.createLeafElement(lineMap, null, lastOffset, rmOffs1));
                }
                Object[] aelems = new Element[this.added.size()];
                this.added.copyInto(aelems);
                Object[] relems = new Element[this.removed.size()];
                this.removed.copyInto(relems);
                AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(lineMap, index, (Element[])relems, (Element[])aelems);
                chng.addEdit(ee);
                lineMap.replace(index, relems.length, (Element[])aelems);
            }
            if (Utilities.isComposedTextAttributeDefined(attr)) {
                this.insertComposedTextUpdate(chng, attr);
            }
        }
        catch (BadLocationException e) {
            throw new Error("Internal error: " + e.toString());
        }
        super.insertUpdate(chng, attr);
    }

    @Override
    protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
        this.removed.removeAllElements();
        AbstractDocument.BranchElement map = (AbstractDocument.BranchElement)this.getDefaultRootElement();
        int offset = chng.getOffset();
        int length = chng.getLength();
        int line0 = map.getElementIndex(offset);
        int line1 = map.getElementIndex(offset + length);
        if (line0 != line1) {
            for (int i = line0; i <= line1; ++i) {
                this.removed.addElement(map.getElement(i));
            }
            int p0 = map.getElement(line0).getStartOffset();
            int p1 = map.getElement(line1).getEndOffset();
            Element[] aelems = new Element[]{this.createLeafElement(map, null, p0, p1)};
            Object[] relems = new Element[this.removed.size()];
            this.removed.copyInto(relems);
            AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(map, line0, (Element[])relems, aelems);
            chng.addEdit(ee);
            map.replace(line0, relems.length, aelems);
        } else {
            Element leaf;
            Element line = map.getElement(line0);
            if (!line.isLeaf() && Utilities.isComposedTextElement(leaf = line.getElement(line.getElementIndex(offset)))) {
                Element[] aelem = new Element[]{this.createLeafElement(map, null, line.getStartOffset(), line.getEndOffset())};
                Element[] relem = new Element[]{line};
                AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(map, line0, relem, aelem);
                chng.addEdit(ee);
                map.replace(line0, 1, aelem);
            }
        }
        super.removeUpdate(chng);
    }

    private void insertComposedTextUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
        this.added.removeAllElements();
        AbstractDocument.BranchElement lineMap = (AbstractDocument.BranchElement)this.getDefaultRootElement();
        int offset = chng.getOffset();
        int length = chng.getLength();
        int index = lineMap.getElementIndex(offset);
        Element elem = lineMap.getElement(index);
        int elemStart = elem.getStartOffset();
        int elemEnd = elem.getEndOffset();
        Element[] abelem = new AbstractDocument.BranchElement[]{(AbstractDocument.BranchElement)this.createBranchElement(lineMap, null)};
        Element[] relem = new Element[]{elem};
        if (elemStart != offset) {
            this.added.addElement(this.createLeafElement(abelem[0], null, elemStart, offset));
        }
        this.added.addElement(this.createLeafElement(abelem[0], attr, offset, offset + length));
        if (elemEnd != offset + length) {
            this.added.addElement(this.createLeafElement(abelem[0], null, offset + length, elemEnd));
        }
        Object[] alelem = new Element[this.added.size()];
        this.added.copyInto(alelem);
        AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(lineMap, index, relem, abelem);
        chng.addEdit(ee);
        ((AbstractDocument.BranchElement)abelem[0]).replace(0, 0, (Element[])alelem);
        lineMap.replace(index, 1, abelem);
    }
}

