/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import javax.swing.JComponent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.CompositeView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SegmentCache;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabExpander;
import javax.swing.text.View;
import sun.swing.SwingUtilities2;

public class Utilities {
    static JComponent getJComponent(View view) {
        Container component;
        if (view != null && (component = view.getContainer()) instanceof JComponent) {
            return (JComponent)component;
        }
        return null;
    }

    @Deprecated(since="9")
    public static final int drawTabbedText(Segment s, int x, int y, Graphics g, TabExpander e, int startOffset) {
        return Utilities.drawTabbedText(null, s, x, y, g, e, startOffset);
    }

    public static final float drawTabbedText(Segment s, float x, float y, Graphics2D g, TabExpander e, int startOffset) {
        return Utilities.drawTabbedText(null, s, x, y, g, e, startOffset, null, true);
    }

    static final int drawTabbedText(View view, Segment s, int x, int y, Graphics g, TabExpander e, int startOffset) {
        return Utilities.drawTabbedText(view, s, x, y, g, e, startOffset, null);
    }

    static final int drawTabbedText(View view, Segment s, int x, int y, Graphics g, TabExpander e, int startOffset, int[] justificationData) {
        return (int)Utilities.drawTabbedText(view, s, x, y, g, e, startOffset, justificationData, false);
    }

    static final float drawTabbedText(View view, Segment s, float x, float y, Graphics g, TabExpander e, int startOffset, int[] justificationData, boolean useFPAPI) {
        JComponent component = Utilities.getJComponent(view);
        FontMetrics metrics = SwingUtilities2.getFontMetrics(component, g);
        float nextX = x;
        char[] txt = s.array;
        int txtOffset = s.offset;
        int flushLen = 0;
        int flushIndex = s.offset;
        int spaceAddon = 0;
        int spaceAddonLeftoverEnd = -1;
        int startJustifiableContent = 0;
        int endJustifiableContent = 0;
        if (justificationData != null) {
            int offset = -startOffset + txtOffset;
            View parent = null;
            if (view != null && (parent = view.getParent()) != null) {
                offset += parent.getStartOffset();
            }
            spaceAddon = justificationData[0];
            spaceAddonLeftoverEnd = justificationData[1] + offset;
            startJustifiableContent = justificationData[2] + offset;
            endJustifiableContent = justificationData[3] + offset;
        }
        int n = s.offset + s.count;
        for (int i = txtOffset; i < n; ++i) {
            if (txt[i] == '\t' || (spaceAddon != 0 || i <= spaceAddonLeftoverEnd) && txt[i] == ' ' && startJustifiableContent <= i && i <= endJustifiableContent) {
                if (flushLen > 0) {
                    nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y);
                    flushLen = 0;
                }
                flushIndex = i + 1;
                if (txt[i] == '\t') {
                    nextX = e != null ? e.nextTabStop(nextX, startOffset + i - txtOffset) : (nextX += SwingUtilities2.getFontCharWidth(' ', metrics, useFPAPI));
                } else if (txt[i] == ' ') {
                    float spaceWidth = SwingUtilities2.getFontCharWidth(' ', metrics, useFPAPI);
                    nextX += spaceWidth + (float)spaceAddon;
                    if (i <= spaceAddonLeftoverEnd) {
                        nextX += 1.0f;
                    }
                }
                x = nextX;
                continue;
            }
            if (txt[i] == '\n' || txt[i] == '\r') {
                if (flushLen > 0) {
                    nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y, useFPAPI);
                    flushLen = 0;
                }
                flushIndex = i + 1;
                x = nextX;
                continue;
            }
            ++flushLen;
        }
        if (flushLen > 0) {
            nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y, useFPAPI);
        }
        return nextX;
    }

    @Deprecated(since="9")
    public static final int getTabbedTextWidth(Segment s, FontMetrics metrics, int x, TabExpander e, int startOffset) {
        return Utilities.getTabbedTextWidth(null, s, metrics, x, e, startOffset, null);
    }

    public static final float getTabbedTextWidth(Segment s, FontMetrics metrics, float x, TabExpander e, int startOffset) {
        return Utilities.getTabbedTextWidth(null, s, metrics, x, e, startOffset, null);
    }

    static final int getTabbedTextWidth(View view, Segment s, FontMetrics metrics, int x, TabExpander e, int startOffset, int[] justificationData) {
        return (int)Utilities.getTabbedTextWidth(view, s, metrics, x, e, startOffset, justificationData, false);
    }

    static final float getTabbedTextWidth(View view, Segment s, FontMetrics metrics, float x, TabExpander e, int startOffset, int[] justificationData) {
        return Utilities.getTabbedTextWidth(view, s, metrics, x, e, startOffset, justificationData, true);
    }

    static final float getTabbedTextWidth(View view, Segment s, FontMetrics metrics, float x, TabExpander e, int startOffset, int[] justificationData, boolean useFPAPI) {
        float nextX = x;
        char[] txt = s.array;
        int txtOffset = s.offset;
        int n = s.offset + s.count;
        int charCount = 0;
        int spaceAddon = 0;
        int spaceAddonLeftoverEnd = -1;
        int startJustifiableContent = 0;
        int endJustifiableContent = 0;
        if (justificationData != null) {
            int offset = -startOffset + txtOffset;
            View parent = null;
            if (view != null && (parent = view.getParent()) != null) {
                offset += parent.getStartOffset();
            }
            spaceAddon = justificationData[0];
            spaceAddonLeftoverEnd = justificationData[1] + offset;
            startJustifiableContent = justificationData[2] + offset;
            endJustifiableContent = justificationData[3] + offset;
        }
        for (int i = txtOffset; i < n; ++i) {
            if (txt[i] == '\t' || (spaceAddon != 0 || i <= spaceAddonLeftoverEnd) && txt[i] == ' ' && startJustifiableContent <= i && i <= endJustifiableContent) {
                nextX += (float)metrics.charsWidth(txt, i - charCount, charCount);
                charCount = 0;
                if (txt[i] == '\t') {
                    if (e != null) {
                        nextX = e.nextTabStop(nextX, startOffset + i - txtOffset);
                        continue;
                    }
                    nextX += SwingUtilities2.getFontCharWidth(' ', metrics, useFPAPI);
                    continue;
                }
                if (txt[i] != ' ') continue;
                float spaceWidth = SwingUtilities2.getFontCharWidth(' ', metrics, useFPAPI);
                nextX += spaceWidth + (float)spaceAddon;
                if (i > spaceAddonLeftoverEnd) continue;
                nextX += 1.0f;
                continue;
            }
            if (txt[i] == '\n') {
                nextX += SwingUtilities2.getFontCharsWidth(txt, i - charCount, charCount, metrics, useFPAPI);
                charCount = 0;
                continue;
            }
            ++charCount;
        }
        return (nextX += SwingUtilities2.getFontCharsWidth(txt, n - charCount, charCount, metrics, useFPAPI)) - x;
    }

    @Deprecated(since="9")
    public static final int getTabbedTextOffset(Segment s, FontMetrics metrics, int x0, int x, TabExpander e, int startOffset) {
        return Utilities.getTabbedTextOffset(s, metrics, x0, x, e, startOffset, true);
    }

    static final int getTabbedTextOffset(View view, Segment s, FontMetrics metrics, int x0, int x, TabExpander e, int startOffset, int[] justificationData) {
        return Utilities.getTabbedTextOffset(view, s, metrics, x0, x, e, startOffset, true, justificationData, false);
    }

    static final int getTabbedTextOffset(View view, Segment s, FontMetrics metrics, float x0, float x, TabExpander e, int startOffset, int[] justificationData) {
        return Utilities.getTabbedTextOffset(view, s, metrics, x0, x, e, startOffset, true, justificationData, true);
    }

    @Deprecated(since="9")
    public static final int getTabbedTextOffset(Segment s, FontMetrics metrics, int x0, int x, TabExpander e, int startOffset, boolean round) {
        return Utilities.getTabbedTextOffset(null, s, metrics, x0, x, e, startOffset, round, null, false);
    }

    public static final int getTabbedTextOffset(Segment s, FontMetrics metrics, float x0, float x, TabExpander e, int startOffset, boolean round) {
        return Utilities.getTabbedTextOffset(null, s, metrics, x0, x, e, startOffset, round, null, true);
    }

    static final int getTabbedTextOffset(View view, Segment s, FontMetrics metrics, float x0, float x, TabExpander e, int startOffset, boolean round, int[] justificationData, boolean useFPAPI) {
        if (x0 >= x) {
            return 0;
        }
        float nextX = x0;
        char[] txt = s.array;
        int txtOffset = s.offset;
        int txtCount = s.count;
        int spaceAddon = 0;
        int spaceAddonLeftoverEnd = -1;
        int startJustifiableContent = 0;
        int endJustifiableContent = 0;
        if (justificationData != null) {
            int offset = -startOffset + txtOffset;
            View parent = null;
            if (view != null && (parent = view.getParent()) != null) {
                offset += parent.getStartOffset();
            }
            spaceAddon = justificationData[0];
            spaceAddonLeftoverEnd = justificationData[1] + offset;
            startJustifiableContent = justificationData[2] + offset;
            endJustifiableContent = justificationData[3] + offset;
        }
        int n = s.offset + s.count;
        for (int i = s.offset; i < n; ++i) {
            int offset;
            if (txt[i] == '\t' || (spaceAddon != 0 || i <= spaceAddonLeftoverEnd) && txt[i] == ' ' && startJustifiableContent <= i && i <= endJustifiableContent) {
                if (txt[i] == '\t') {
                    nextX = e != null ? e.nextTabStop(nextX, startOffset + i - txtOffset) : (nextX += SwingUtilities2.getFontCharWidth(' ', metrics, useFPAPI));
                } else if (txt[i] == ' ') {
                    nextX += SwingUtilities2.getFontCharWidth(' ', metrics, useFPAPI);
                    nextX += (float)spaceAddon;
                    if (i <= spaceAddonLeftoverEnd) {
                        nextX += 1.0f;
                    }
                }
            } else {
                nextX += SwingUtilities2.getFontCharWidth(txt[i], metrics, useFPAPI);
            }
            if (!(x < nextX)) continue;
            if (round) {
                float span = x - x0;
                offset = i + 1 - txtOffset;
                float width = SwingUtilities2.getFontCharsWidth(txt, txtOffset, offset, metrics, useFPAPI);
                if (span < width) {
                    while (offset > 0) {
                        float nextWidth;
                        float charsWidth = SwingUtilities2.getFontCharsWidth(txt, txtOffset, offset - 1, metrics, useFPAPI);
                        float f = nextWidth = offset > 1 ? charsWidth : 0.0f;
                        if (span >= nextWidth) {
                            if (!(span - nextWidth < width - span)) break;
                            --offset;
                            break;
                        }
                        width = nextWidth;
                        --offset;
                    }
                }
            } else {
                for (offset = i - txtOffset; offset > 0 && SwingUtilities2.getFontCharsWidth(txt, txtOffset, offset, metrics, useFPAPI) > x - x0; --offset) {
                }
            }
            return offset;
        }
        return txtCount;
    }

    @Deprecated(since="9")
    public static final int getBreakLocation(Segment s, FontMetrics metrics, int x0, int x, TabExpander e, int startOffset) {
        return Utilities.getBreakLocation(s, metrics, x0, x, e, startOffset, false);
    }

    static final int getBreakLocation(Segment s, FontMetrics metrics, float x0, float x, TabExpander e, int startOffset, boolean useFPIAPI) {
        char[] txt = s.array;
        int txtOffset = s.offset;
        int txtCount = s.count;
        int index = Utilities.getTabbedTextOffset(null, s, metrics, x0, x, e, startOffset, false, null, useFPIAPI);
        if (index >= txtCount - 1) {
            return txtCount;
        }
        for (int i = txtOffset + index; i >= txtOffset; --i) {
            char ch = txt[i];
            if (ch < '\u0100') {
                if (!Character.isWhitespace(ch)) continue;
                index = i - txtOffset + 1;
                break;
            }
            BreakIterator bit = BreakIterator.getLineInstance();
            bit.setText(s);
            int breakPos = bit.preceding(i + 1);
            if (breakPos <= txtOffset) break;
            index = breakPos - txtOffset;
            break;
        }
        return index;
    }

    public static final int getBreakLocation(Segment s, FontMetrics metrics, float x0, float x, TabExpander e, int startOffset) {
        return Utilities.getBreakLocation(s, metrics, x0, x, e, startOffset, true);
    }

    public static final int getRowStart(JTextComponent c, int offs) throws BadLocationException {
        Rectangle r = c.modelToView(offs);
        if (r == null) {
            return -1;
        }
        int lastOffs = offs;
        int y = r.y;
        while (r != null && y == r.y) {
            if (r.height != 0) {
                offs = lastOffs;
            }
            r = --lastOffs >= 0 ? c.modelToView(lastOffs) : null;
        }
        return offs;
    }

    public static final int getRowEnd(JTextComponent c, int offs) throws BadLocationException {
        Rectangle2D r = c.modelToView2D(offs);
        if (r == null) {
            return -1;
        }
        int n = c.getDocument().getLength();
        int lastOffs = offs;
        double y = r.getY();
        while (r != null && y == r.getY()) {
            if (r.getHeight() != 0.0) {
                offs = lastOffs;
            }
            r = ++lastOffs <= n ? c.modelToView(lastOffs) : null;
        }
        return offs;
    }

    @Deprecated(since="9")
    public static final int getPositionAbove(JTextComponent c, int offs, int x) throws BadLocationException {
        return Utilities.getPositionAbove(c, offs, x, false);
    }

    static final int getPositionAbove(JTextComponent c, int offs, float x, boolean useFPAPI) throws BadLocationException {
        int lastOffs = Utilities.getRowStart(c, offs) - 1;
        if (lastOffs < 0) {
            return -1;
        }
        double bestSpan = 2.147483647E9;
        double y = 0.0;
        RectangularShape r = null;
        if (lastOffs >= 0) {
            r = useFPAPI ? c.modelToView2D(lastOffs) : c.modelToView(lastOffs);
            y = r.getY();
        }
        while (r != null && y == r.getY()) {
            double span = Math.abs(r.getX() - (double)x);
            if (span < bestSpan) {
                offs = lastOffs;
                bestSpan = span;
            }
            if (--lastOffs >= 0) {
                r = useFPAPI ? c.modelToView2D(lastOffs) : c.modelToView(lastOffs);
                continue;
            }
            r = null;
        }
        return offs;
    }

    public static final int getPositionAbove(JTextComponent c, int offs, float x) throws BadLocationException {
        return Utilities.getPositionAbove(c, offs, x, true);
    }

    @Deprecated(since="9")
    public static final int getPositionBelow(JTextComponent c, int offs, int x) throws BadLocationException {
        return Utilities.getPositionBelow(c, offs, x, false);
    }

    static final int getPositionBelow(JTextComponent c, int offs, float x, boolean useFPAPI) throws BadLocationException {
        int lastOffs = Utilities.getRowEnd(c, offs) + 1;
        if (lastOffs <= 0) {
            return -1;
        }
        double bestSpan = 2.147483647E9;
        int n = c.getDocument().getLength();
        double y = 0.0;
        RectangularShape r = null;
        if (lastOffs <= n) {
            r = useFPAPI ? c.modelToView2D(lastOffs) : c.modelToView(lastOffs);
            y = r.getY();
        }
        while (r != null && y == r.getY()) {
            double span = Math.abs((double)x - r.getX());
            if (span < bestSpan) {
                offs = lastOffs;
                bestSpan = span;
            }
            if (++lastOffs <= n) {
                r = useFPAPI ? c.modelToView2D(lastOffs) : c.modelToView(lastOffs);
                continue;
            }
            r = null;
        }
        return offs;
    }

    public static final int getPositionBelow(JTextComponent c, int offs, float x) throws BadLocationException {
        return Utilities.getPositionBelow(c, offs, x, true);
    }

    public static final int getWordStart(JTextComponent c, int offs) throws BadLocationException {
        Document doc = c.getDocument();
        Element line = Utilities.getParagraphElement(c, offs);
        if (line == null) {
            throw new BadLocationException("No word at " + offs, offs);
        }
        int lineStart = line.getStartOffset();
        int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
        Segment seg = SegmentCache.getSharedSegment();
        doc.getText(lineStart, lineEnd - lineStart, seg);
        if (seg.count > 0) {
            BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
            words.setText(seg);
            int wordPosition = seg.offset + offs - lineStart;
            if (wordPosition >= words.last()) {
                wordPosition = words.last() - 1;
            }
            words.following(wordPosition);
            offs = lineStart + words.previous() - seg.offset;
        }
        SegmentCache.releaseSharedSegment(seg);
        return offs;
    }

    public static final int getWordEnd(JTextComponent c, int offs) throws BadLocationException {
        Document doc = c.getDocument();
        Element line = Utilities.getParagraphElement(c, offs);
        if (line == null) {
            throw new BadLocationException("No word at " + offs, offs);
        }
        int lineStart = line.getStartOffset();
        int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
        Segment seg = SegmentCache.getSharedSegment();
        doc.getText(lineStart, lineEnd - lineStart, seg);
        if (seg.count > 0) {
            BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
            words.setText(seg);
            int wordPosition = offs - lineStart + seg.offset;
            if (wordPosition >= words.last()) {
                wordPosition = words.last() - 1;
            }
            offs = lineStart + words.following(wordPosition) - seg.offset;
        }
        SegmentCache.releaseSharedSegment(seg);
        return offs;
    }

    public static final int getNextWord(JTextComponent c, int offs) throws BadLocationException {
        Element line = Utilities.getParagraphElement(c, offs);
        int nextWord = Utilities.getNextWordInParagraph(c, line, offs, false);
        while (nextWord == -1) {
            offs = line.getEndOffset();
            line = Utilities.getParagraphElement(c, offs);
            nextWord = Utilities.getNextWordInParagraph(c, line, offs, true);
        }
        return nextWord;
    }

    static int getNextWordInParagraph(JTextComponent c, Element line, int offs, boolean first) throws BadLocationException {
        if (line == null) {
            throw new BadLocationException("No more words", offs);
        }
        Document doc = line.getDocument();
        int lineStart = line.getStartOffset();
        int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
        if (offs >= lineEnd || offs < lineStart) {
            throw new BadLocationException("No more words", offs);
        }
        Segment seg = SegmentCache.getSharedSegment();
        doc.getText(lineStart, lineEnd - lineStart, seg);
        BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
        words.setText(seg);
        if (first && words.first() == seg.offset + offs - lineStart && !Character.isWhitespace(seg.array[words.first()])) {
            return offs;
        }
        int wordPosition = words.following(seg.offset + offs - lineStart);
        if (wordPosition == -1 || wordPosition >= seg.offset + seg.count) {
            return -1;
        }
        char ch = seg.array[wordPosition];
        if (!Character.isWhitespace(ch)) {
            return lineStart + wordPosition - seg.offset;
        }
        wordPosition = words.next();
        if (wordPosition != -1 && (offs = lineStart + wordPosition - seg.offset) != lineEnd) {
            return offs;
        }
        SegmentCache.releaseSharedSegment(seg);
        return -1;
    }

    public static final int getPreviousWord(JTextComponent c, int offs) throws BadLocationException {
        Element line = Utilities.getParagraphElement(c, offs);
        int prevWord = Utilities.getPrevWordInParagraph(c, line, offs);
        while (prevWord == -1) {
            offs = line.getStartOffset() - 1;
            line = Utilities.getParagraphElement(c, offs);
            prevWord = Utilities.getPrevWordInParagraph(c, line, offs);
        }
        return prevWord;
    }

    static int getPrevWordInParagraph(JTextComponent c, Element line, int offs) throws BadLocationException {
        int wordPosition;
        if (line == null) {
            throw new BadLocationException("No more words", offs);
        }
        Document doc = line.getDocument();
        int lineStart = line.getStartOffset();
        int lineEnd = line.getEndOffset();
        if (offs > lineEnd || offs < lineStart) {
            throw new BadLocationException("No more words", offs);
        }
        Segment seg = SegmentCache.getSharedSegment();
        doc.getText(lineStart, lineEnd - lineStart, seg);
        BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
        words.setText(seg);
        if (words.following(seg.offset + offs - lineStart) == -1) {
            words.last();
        }
        if ((wordPosition = words.previous()) == seg.offset + offs - lineStart) {
            wordPosition = words.previous();
        }
        if (wordPosition == -1) {
            return -1;
        }
        char ch = seg.array[wordPosition];
        if (!Character.isWhitespace(ch)) {
            return lineStart + wordPosition - seg.offset;
        }
        wordPosition = words.previous();
        if (wordPosition != -1) {
            return lineStart + wordPosition - seg.offset;
        }
        SegmentCache.releaseSharedSegment(seg);
        return -1;
    }

    public static final Element getParagraphElement(JTextComponent c, int offs) {
        int index;
        Document doc = c.getDocument();
        if (doc instanceof StyledDocument) {
            return ((StyledDocument)doc).getParagraphElement(offs);
        }
        Element map = doc.getDefaultRootElement();
        Element paragraph = map.getElement(index = map.getElementIndex(offs));
        if (offs >= paragraph.getStartOffset() && offs < paragraph.getEndOffset()) {
            return paragraph;
        }
        return null;
    }

    static boolean isComposedTextElement(Document doc, int offset) {
        Element elem = doc.getDefaultRootElement();
        while (!elem.isLeaf()) {
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        return Utilities.isComposedTextElement(elem);
    }

    static boolean isComposedTextElement(Element elem) {
        AttributeSet as = elem.getAttributes();
        return Utilities.isComposedTextAttributeDefined(as);
    }

    static boolean isComposedTextAttributeDefined(AttributeSet as) {
        return as != null && as.isDefined(StyleConstants.ComposedTextAttribute);
    }

    static int drawComposedText(View view, AttributeSet attr, Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return (int)Utilities.drawComposedText(view, attr, g, x, y, p0, p1, false);
    }

    static float drawComposedText(View view, AttributeSet attr, Graphics g, float x, float y, int p0, int p1) throws BadLocationException {
        return Utilities.drawComposedText(view, attr, g, x, y, p0, p1, true);
    }

    static float drawComposedText(View view, AttributeSet attr, Graphics g, float x, float y, int p0, int p1, boolean useFPAPI) throws BadLocationException {
        Graphics2D g2d = (Graphics2D)g;
        AttributedString as = (AttributedString)attr.getAttribute(StyleConstants.ComposedTextAttribute);
        as.addAttribute(TextAttribute.FONT, g.getFont());
        if (p0 >= p1) {
            return x;
        }
        AttributedCharacterIterator aci = as.getIterator(null, p0, p1);
        return x + SwingUtilities2.drawString(Utilities.getJComponent(view), (Graphics)g2d, aci, x, y);
    }

    static void paintComposedText(Graphics g, Rectangle alloc, GlyphView v) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D)g;
            int p0 = v.getStartOffset();
            int p1 = v.getEndOffset();
            AttributeSet attrSet = v.getElement().getAttributes();
            AttributedString as = (AttributedString)attrSet.getAttribute(StyleConstants.ComposedTextAttribute);
            int start = v.getElement().getStartOffset();
            int y = alloc.y + alloc.height - (int)v.getGlyphPainter().getDescent(v);
            int x = alloc.x;
            as.addAttribute(TextAttribute.FONT, v.getFont());
            as.addAttribute(TextAttribute.FOREGROUND, v.getForeground());
            if (StyleConstants.isBold(v.getAttributes())) {
                as.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            }
            if (StyleConstants.isItalic(v.getAttributes())) {
                as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
            }
            if (v.isUnderline()) {
                as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            }
            if (v.isStrikeThrough()) {
                as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            }
            if (v.isSuperscript()) {
                as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER);
            }
            if (v.isSubscript()) {
                as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
            }
            AttributedCharacterIterator aci = as.getIterator(null, p0 - start, p1 - start);
            SwingUtilities2.drawString(Utilities.getJComponent(v), (Graphics)g2d, aci, x, y);
        }
    }

    static boolean isLeftToRight(Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }

    static int getNextVisualPositionFrom(View v, int pos, Position.Bias b, Shape alloc, int direction, Position.Bias[] biasRet) throws BadLocationException {
        int retValue;
        boolean top;
        if (v.getViewCount() == 0) {
            return pos;
        }
        boolean bl = top = direction == 1 || direction == 7;
        if (pos == -1) {
            Shape childBounds;
            int childIndex = top ? v.getViewCount() - 1 : 0;
            View child = v.getView(childIndex);
            retValue = child.getNextVisualPositionFrom(pos, b, childBounds = v.getChildAllocation(childIndex, alloc), direction, biasRet);
            if (retValue == -1 && !top && v.getViewCount() > 1) {
                child = v.getView(1);
                childBounds = v.getChildAllocation(1, alloc);
                retValue = child.getNextVisualPositionFrom(-1, biasRet[0], childBounds, direction, biasRet);
            }
        } else {
            int increment = top ? -1 : 1;
            int childIndex = b == Position.Bias.Backward && pos > 0 ? v.getViewIndex(pos - 1, Position.Bias.Forward) : v.getViewIndex(pos, Position.Bias.Forward);
            View child = v.getView(childIndex);
            Shape childBounds = v.getChildAllocation(childIndex, alloc);
            retValue = child.getNextVisualPositionFrom(pos, b, childBounds, direction, biasRet);
            if ((direction == 3 || direction == 7) && v instanceof CompositeView && ((CompositeView)v).flipEastAndWestAtEnds(pos, b)) {
                increment *= -1;
            }
            if (retValue == -1 && (childIndex += increment) >= 0 && childIndex < v.getViewCount()) {
                child = v.getView(childIndex);
                retValue = child.getNextVisualPositionFrom(-1, b, childBounds = v.getChildAllocation(childIndex, alloc), direction, biasRet);
                if (retValue == pos && biasRet[0] != b) {
                    return Utilities.getNextVisualPositionFrom(v, pos, biasRet[0], alloc, direction, biasRet);
                }
            } else if (retValue != -1 && biasRet[0] != b && (increment == 1 && child.getEndOffset() == retValue || increment == -1 && child.getStartOffset() == retValue) && childIndex >= 0 && childIndex < v.getViewCount()) {
                child = v.getView(childIndex);
                childBounds = v.getChildAllocation(childIndex, alloc);
                Position.Bias originalBias = biasRet[0];
                int nextPos = child.getNextVisualPositionFrom(-1, b, childBounds, direction, biasRet);
                if (biasRet[0] == b) {
                    retValue = nextPos;
                } else {
                    biasRet[0] = originalBias;
                }
            }
        }
        return retValue;
    }
}

