/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.ComponentOrientation;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.NavigationFilter;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;
import javax.swing.text.ViewFactory;
import sun.awt.SunToolkit;

public class DefaultEditorKit
extends EditorKit {
    public static final String EndOfLineStringProperty = "__EndOfLine__";
    public static final String insertContentAction = "insert-content";
    public static final String insertBreakAction = "insert-break";
    public static final String insertTabAction = "insert-tab";
    public static final String deletePrevCharAction = "delete-previous";
    public static final String deleteNextCharAction = "delete-next";
    public static final String deleteNextWordAction = "delete-next-word";
    public static final String deletePrevWordAction = "delete-previous-word";
    public static final String readOnlyAction = "set-read-only";
    public static final String writableAction = "set-writable";
    public static final String cutAction = "cut-to-clipboard";
    public static final String copyAction = "copy-to-clipboard";
    public static final String pasteAction = "paste-from-clipboard";
    public static final String beepAction = "beep";
    public static final String pageUpAction = "page-up";
    public static final String pageDownAction = "page-down";
    static final String selectionPageUpAction = "selection-page-up";
    static final String selectionPageDownAction = "selection-page-down";
    static final String selectionPageLeftAction = "selection-page-left";
    static final String selectionPageRightAction = "selection-page-right";
    public static final String forwardAction = "caret-forward";
    public static final String backwardAction = "caret-backward";
    public static final String selectionForwardAction = "selection-forward";
    public static final String selectionBackwardAction = "selection-backward";
    public static final String upAction = "caret-up";
    public static final String downAction = "caret-down";
    public static final String selectionUpAction = "selection-up";
    public static final String selectionDownAction = "selection-down";
    public static final String beginWordAction = "caret-begin-word";
    public static final String endWordAction = "caret-end-word";
    public static final String selectionBeginWordAction = "selection-begin-word";
    public static final String selectionEndWordAction = "selection-end-word";
    public static final String previousWordAction = "caret-previous-word";
    public static final String nextWordAction = "caret-next-word";
    public static final String selectionPreviousWordAction = "selection-previous-word";
    public static final String selectionNextWordAction = "selection-next-word";
    public static final String beginLineAction = "caret-begin-line";
    public static final String endLineAction = "caret-end-line";
    public static final String beginLineUpAction = "caret-begin-line-and-up";
    public static final String endLineDownAction = "caret-end-line-and-down";
    public static final String selectionBeginLineAction = "selection-begin-line";
    public static final String selectionEndLineAction = "selection-end-line";
    public static final String beginParagraphAction = "caret-begin-paragraph";
    public static final String endParagraphAction = "caret-end-paragraph";
    public static final String selectionBeginParagraphAction = "selection-begin-paragraph";
    public static final String selectionEndParagraphAction = "selection-end-paragraph";
    public static final String beginAction = "caret-begin";
    public static final String endAction = "caret-end";
    public static final String selectionBeginAction = "selection-begin";
    public static final String selectionEndAction = "selection-end";
    public static final String selectWordAction = "select-word";
    public static final String selectLineAction = "select-line";
    public static final String selectParagraphAction = "select-paragraph";
    public static final String selectAllAction = "select-all";
    static final String unselectAction = "unselect";
    static final String toggleComponentOrientationAction = "toggle-componentOrientation";
    public static final String defaultKeyTypedAction = "default-typed";
    private static final Action[] defaultActions = new Action[]{new InsertContentAction(), new DeletePrevCharAction(), new DeleteNextCharAction(), new ReadOnlyAction(), new DeleteWordAction("delete-previous-word"), new DeleteWordAction("delete-next-word"), new WritableAction(), new CutAction(), new CopyAction(), new PasteAction(), new VerticalPageAction("page-up", -1, false), new VerticalPageAction("page-down", 1, false), new VerticalPageAction("selection-page-up", -1, true), new VerticalPageAction("selection-page-down", 1, true), new PageAction("selection-page-left", true, true), new PageAction("selection-page-right", false, true), new InsertBreakAction(), new BeepAction(), new NextVisualPositionAction("caret-forward", false, 3), new NextVisualPositionAction("caret-backward", false, 7), new NextVisualPositionAction("selection-forward", true, 3), new NextVisualPositionAction("selection-backward", true, 7), new NextVisualPositionAction("caret-up", false, 1), new NextVisualPositionAction("caret-down", false, 5), new NextVisualPositionAction("selection-up", true, 1), new NextVisualPositionAction("selection-down", true, 5), new BeginWordAction("caret-begin-word", false), new EndWordAction("caret-end-word", false), new BeginWordAction("selection-begin-word", true), new EndWordAction("selection-end-word", true), new PreviousWordAction("caret-previous-word", false), new NextWordAction("caret-next-word", false), new PreviousWordAction("selection-previous-word", true), new NextWordAction("selection-next-word", true), new BeginLineAction("caret-begin-line", false), new BeginLineUpAction("caret-begin-line-and-up", false, 1), new EndLineAction("caret-end-line", false), new EndLineDownAction("caret-end-line-and-down", false, 5), new BeginLineAction("selection-begin-line", true), new EndLineAction("selection-end-line", true), new BeginParagraphAction("caret-begin-paragraph", false), new EndParagraphAction("caret-end-paragraph", false), new BeginParagraphAction("selection-begin-paragraph", true), new EndParagraphAction("selection-end-paragraph", true), new BeginAction("caret-begin", false), new EndAction("caret-end", false), new BeginAction("selection-begin", true), new EndAction("selection-end", true), new DefaultKeyTypedAction(), new InsertTabAction(), new SelectWordAction(), new SelectLineAction(), new SelectParagraphAction(), new SelectAllAction(), new UnselectAction(), new ToggleComponentOrientationAction(), new DumpModelAction()};

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public ViewFactory getViewFactory() {
        return null;
    }

    @Override
    public Action[] getActions() {
        return (Action[])defaultActions.clone();
    }

    @Override
    public Caret createCaret() {
        return null;
    }

    @Override
    public Document createDefaultDocument() {
        return new PlainDocument();
    }

    @Override
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        this.read(new InputStreamReader(in), doc, pos);
    }

    @Override
    public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
        OutputStreamWriter osw = new OutputStreamWriter(out);
        this.write(osw, doc, pos, len);
        osw.flush();
    }

    MutableAttributeSet getInputAttributes() {
        return null;
    }

    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        int nch;
        char[] buff = new char[4096];
        boolean lastWasCR = false;
        boolean isCRLF = false;
        boolean isCR = false;
        boolean wasEmpty = doc.getLength() == 0;
        MutableAttributeSet attr = this.getInputAttributes();
        while ((nch = in.read(buff, 0, buff.length)) != -1) {
            int last = 0;
            block5: for (int counter = 0; counter < nch; ++counter) {
                switch (buff[counter]) {
                    case '\r': {
                        if (lastWasCR) {
                            isCR = true;
                            if (counter == 0) {
                                doc.insertString(pos, "\n", attr);
                                ++pos;
                                continue block5;
                            }
                            buff[counter - 1] = 10;
                            continue block5;
                        }
                        lastWasCR = true;
                        continue block5;
                    }
                    case '\n': {
                        if (!lastWasCR) continue block5;
                        if (counter > last + 1) {
                            doc.insertString(pos, new String(buff, last, counter - last - 1), attr);
                            pos += counter - last - 1;
                        }
                        lastWasCR = false;
                        last = counter;
                        isCRLF = true;
                        continue block5;
                    }
                    default: {
                        if (!lastWasCR) continue block5;
                        isCR = true;
                        if (counter == 0) {
                            doc.insertString(pos, "\n", attr);
                            ++pos;
                        } else {
                            buff[counter - 1] = 10;
                        }
                        lastWasCR = false;
                    }
                }
            }
            if (last >= nch) continue;
            if (lastWasCR) {
                if (last >= nch - 1) continue;
                doc.insertString(pos, new String(buff, last, nch - last - 1), attr);
                pos += nch - last - 1;
                continue;
            }
            doc.insertString(pos, new String(buff, last, nch - last), attr);
            pos += nch - last;
        }
        if (lastWasCR) {
            doc.insertString(pos, "\n", attr);
            isCR = true;
        }
        if (wasEmpty) {
            if (isCRLF) {
                doc.putProperty(EndOfLineStringProperty, "\r\n");
            } else if (isCR) {
                doc.putProperty(EndOfLineStringProperty, "\r");
            } else {
                doc.putProperty(EndOfLineStringProperty, "\n");
            }
        }
    }

    @Override
    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        if (pos < 0 || pos + len > doc.getLength()) {
            throw new BadLocationException("DefaultEditorKit.write", pos);
        }
        Segment data = new Segment();
        int nleft = len;
        int offs = pos;
        Object endOfLineProperty = doc.getProperty(EndOfLineStringProperty);
        if (endOfLineProperty == null) {
            endOfLineProperty = System.lineSeparator();
        }
        String endOfLine = endOfLineProperty instanceof String ? (String)endOfLineProperty : null;
        if (endOfLineProperty != null && !endOfLine.equals("\n")) {
            while (nleft > 0) {
                int n = Math.min(nleft, 4096);
                doc.getText(offs, n, data);
                int last = data.offset;
                char[] array = data.array;
                int maxCounter = last + data.count;
                for (int counter = last; counter < maxCounter; ++counter) {
                    if (array[counter] != '\n') continue;
                    if (counter > last) {
                        out.write(array, last, counter - last);
                    }
                    out.write(endOfLine);
                    last = counter + 1;
                }
                if (maxCounter > last) {
                    out.write(array, last, maxCounter - last);
                }
                offs += n;
                nleft -= n;
            }
        } else {
            while (nleft > 0) {
                int n = Math.min(nleft, 4096);
                doc.getText(offs, n, data);
                out.write(data.array, data.offset, data.count);
                offs += n;
                nleft -= n;
            }
        }
        out.flush();
    }

    public static class InsertContentAction
    extends TextAction {
        public InsertContentAction() {
            super(DefaultEditorKit.insertContentAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null && e != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                String content = e.getActionCommand();
                if (content != null) {
                    target.replaceSelection(content);
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class DeletePrevCharAction
    extends TextAction {
        DeletePrevCharAction() {
            super(DefaultEditorKit.deletePrevCharAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            boolean beep = true;
            if (target != null && target.isEditable()) {
                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    } else if (dot > 0) {
                        int delChars = 1;
                        if (dot > 1) {
                            String dotChars = doc.getText(dot - 2, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);
                            if (c0 >= '\ud800' && c0 <= '\udbff' && c1 >= '\udc00' && c1 <= '\udfff') {
                                delChars = 2;
                            }
                        }
                        doc.remove(dot - delChars, delChars);
                        beep = false;
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
            if (beep) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }

    static class DeleteNextCharAction
    extends TextAction {
        DeleteNextCharAction() {
            super(DefaultEditorKit.deleteNextCharAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            boolean beep = true;
            if (target != null && target.isEditable()) {
                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    } else if (dot < doc.getLength()) {
                        int delChars = 1;
                        if (dot < doc.getLength() - 1) {
                            String dotChars = doc.getText(dot, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);
                            if (c0 >= '\ud800' && c0 <= '\udbff' && c1 >= '\udc00' && c1 <= '\udfff') {
                                delChars = 2;
                            }
                        }
                        doc.remove(dot, delChars);
                        beep = false;
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
            if (beep) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }

    static class ReadOnlyAction
    extends TextAction {
        ReadOnlyAction() {
            super(DefaultEditorKit.readOnlyAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                target.setEditable(false);
            }
        }
    }

    static class DeleteWordAction
    extends TextAction {
        DeleteWordAction(String name) {
            super(name);
            assert (name == DefaultEditorKit.deletePrevWordAction || name == DefaultEditorKit.deleteNextWordAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null && e != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                boolean beep = true;
                try {
                    int end;
                    int start = target.getSelectionStart();
                    Element line = Utilities.getParagraphElement(target, start);
                    if (DefaultEditorKit.deleteNextWordAction == this.getValue("Name")) {
                        end = Utilities.getNextWordInParagraph(target, line, start, false);
                        if (end == -1) {
                            int endOfLine = line.getEndOffset();
                            end = start == endOfLine - 1 ? endOfLine : endOfLine - 1;
                        }
                    } else {
                        end = Utilities.getPrevWordInParagraph(target, line, start);
                        if (end == -1) {
                            int startOfLine = line.getStartOffset();
                            end = start == startOfLine ? startOfLine - 1 : startOfLine;
                        }
                    }
                    int offs = Math.min(start, end);
                    int len = Math.abs(end - start);
                    if (offs >= 0) {
                        target.getDocument().remove(offs, len);
                        beep = false;
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
                if (beep) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class WritableAction
    extends TextAction {
        WritableAction() {
            super(DefaultEditorKit.writableAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                target.setEditable(true);
            }
        }
    }

    public static class CutAction
    extends TextAction {
        public CutAction() {
            super(DefaultEditorKit.cutAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                target.cut();
            }
        }
    }

    public static class CopyAction
    extends TextAction {
        public CopyAction() {
            super(DefaultEditorKit.copyAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                target.copy();
            }
        }
    }

    public static class PasteAction
    extends TextAction {
        public PasteAction() {
            super(DefaultEditorKit.pasteAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                target.paste();
            }
        }
    }

    static class VerticalPageAction
    extends TextAction {
        private boolean select;
        private int direction;

        public VerticalPageAction(String nm, int direction, boolean select) {
            super(nm);
            this.select = select;
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                Rectangle visible = target.getVisibleRect();
                Rectangle newVis = new Rectangle(visible);
                int selectedIndex = target.getCaretPosition();
                int scrollAmount = this.direction * target.getScrollableBlockIncrement(visible, 1, this.direction);
                int initialY = visible.y;
                Caret caret = target.getCaret();
                Point magicPosition = caret.getMagicCaretPosition();
                if (selectedIndex != -1) {
                    try {
                        Rectangle dotBounds = target.modelToView(selectedIndex);
                        int x = magicPosition != null ? magicPosition.x : dotBounds.x;
                        int h = dotBounds.height;
                        if (h > 0) {
                            scrollAmount = scrollAmount / h * h;
                        }
                        newVis.y = this.constrainY(target, initialY + scrollAmount, visible.height);
                        int newIndex = visible.contains(dotBounds.x, dotBounds.y) ? target.viewToModel(new Point(x, this.constrainY(target, dotBounds.y + scrollAmount, 0))) : (this.direction == -1 ? target.viewToModel(new Point(x, newVis.y)) : target.viewToModel(new Point(x, newVis.y + visible.height)));
                        if ((newIndex = this.constrainOffset(target, newIndex)) != selectedIndex) {
                            int newY = this.getAdjustedY(target, newVis, newIndex);
                            if (this.direction == -1 && newY <= initialY || this.direction == 1 && newY >= initialY) {
                                newVis.y = newY;
                                if (this.select) {
                                    target.moveCaretPosition(newIndex);
                                } else {
                                    target.setCaretPosition(newIndex);
                                }
                            }
                        } else if (this.direction == -1 && newVis.y <= initialY || this.direction == 1 && newVis.y >= initialY) {
                            newVis.y = initialY;
                        }
                    }
                    catch (BadLocationException badLocationException) {}
                } else {
                    newVis.y = this.constrainY(target, initialY + scrollAmount, visible.height);
                }
                if (magicPosition != null) {
                    caret.setMagicCaretPosition(magicPosition);
                }
                target.scrollRectToVisible(newVis);
            }
        }

        private int constrainY(JTextComponent target, int y, int vis) {
            if (y < 0) {
                y = 0;
            } else if (y + vis > target.getHeight()) {
                y = Math.max(0, target.getHeight() - vis);
            }
            return y;
        }

        private int constrainOffset(JTextComponent text, int offset) {
            Document doc = text.getDocument();
            if (offset != 0 && offset > doc.getLength()) {
                offset = doc.getLength();
            }
            if (offset < 0) {
                offset = 0;
            }
            return offset;
        }

        private int getAdjustedY(JTextComponent text, Rectangle visible, int index) {
            int result = visible.y;
            try {
                Rectangle dotBounds = text.modelToView(index);
                if (dotBounds.y < visible.y) {
                    result = dotBounds.y;
                } else if (dotBounds.y > visible.y + visible.height || dotBounds.y + dotBounds.height > visible.y + visible.height) {
                    result = dotBounds.y + dotBounds.height - visible.height;
                }
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
            return result;
        }
    }

    static class PageAction
    extends TextAction {
        private boolean select;
        private boolean left;

        public PageAction(String nm, boolean left, boolean select) {
            super(nm);
            this.select = select;
            this.left = left;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                Rectangle visible = new Rectangle();
                target.computeVisibleRect(visible);
                visible.x = this.left ? Math.max(0, visible.x - visible.width) : (visible.x += visible.width);
                int selectedIndex = target.getCaretPosition();
                if (selectedIndex != -1) {
                    selectedIndex = this.left ? target.viewToModel(new Point(visible.x, visible.y)) : target.viewToModel(new Point(visible.x + visible.width - 1, visible.y + visible.height - 1));
                    Document doc = target.getDocument();
                    if (selectedIndex != 0 && selectedIndex > doc.getLength() - 1) {
                        selectedIndex = doc.getLength() - 1;
                    } else if (selectedIndex < 0) {
                        selectedIndex = 0;
                    }
                    if (this.select) {
                        target.moveCaretPosition(selectedIndex);
                    } else {
                        target.setCaretPosition(selectedIndex);
                    }
                }
            }
        }
    }

    public static class InsertBreakAction
    extends TextAction {
        public InsertBreakAction() {
            super(DefaultEditorKit.insertBreakAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                target.replaceSelection("\n");
            }
        }
    }

    public static class BeepAction
    extends TextAction {
        public BeepAction() {
            super(DefaultEditorKit.beepAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            UIManager.getLookAndFeel().provideErrorFeedback(target);
        }
    }

    static class NextVisualPositionAction
    extends TextAction {
        private boolean select;
        private int direction;

        NextVisualPositionAction(String nm, boolean select, int direction) {
            super(nm);
            this.select = select;
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                Caret caret = target.getCaret();
                DefaultCaret bidiCaret = caret instanceof DefaultCaret ? (DefaultCaret)caret : null;
                int dot = caret.getDot();
                Position.Bias[] bias = new Position.Bias[1];
                Point magicPosition = caret.getMagicCaretPosition();
                try {
                    NavigationFilter filter;
                    if (magicPosition == null && (this.direction == 1 || this.direction == 5)) {
                        Rectangle r = bidiCaret != null ? target.getUI().modelToView(target, dot, bidiCaret.getDotBias()) : target.modelToView(dot);
                        magicPosition = new Point(r.x, r.y);
                    }
                    dot = (filter = target.getNavigationFilter()) != null ? filter.getNextVisualPositionFrom(target, dot, bidiCaret != null ? bidiCaret.getDotBias() : Position.Bias.Forward, this.direction, bias) : target.getUI().getNextVisualPositionFrom(target, dot, bidiCaret != null ? bidiCaret.getDotBias() : Position.Bias.Forward, this.direction, bias);
                    if (bias[0] == null) {
                        bias[0] = Position.Bias.Forward;
                    }
                    if (bidiCaret != null) {
                        if (this.select) {
                            bidiCaret.moveDot(dot, bias[0]);
                        } else {
                            bidiCaret.setDot(dot, bias[0]);
                        }
                    } else if (this.select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                    if (magicPosition != null && (this.direction == 1 || this.direction == 5)) {
                        target.getCaret().setMagicCaretPosition(magicPosition);
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }
    }

    static class BeginWordAction
    extends TextAction {
        private boolean select;

        BeginWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int begOffs = Utilities.getWordStart(target, offs);
                    if (this.select) {
                        target.moveCaretPosition(begOffs);
                    } else {
                        target.setCaretPosition(begOffs);
                    }
                }
                catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class EndWordAction
    extends TextAction {
        private boolean select;

        EndWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int endOffs = Utilities.getWordEnd(target, offs);
                    if (this.select) {
                        target.moveCaretPosition(endOffs);
                    } else {
                        target.setCaretPosition(endOffs);
                    }
                }
                catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class PreviousWordAction
    extends TextAction {
        private boolean select;

        PreviousWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                boolean failed = false;
                try {
                    Element curPara = Utilities.getParagraphElement(target, offs);
                    offs = Utilities.getPreviousWord(target, offs);
                    if (offs < curPara.getStartOffset()) {
                        offs = Utilities.getParagraphElement(target, offs).getEndOffset() - 1;
                    }
                }
                catch (BadLocationException bl) {
                    if (offs != 0) {
                        offs = 0;
                    }
                    failed = true;
                }
                if (!failed) {
                    if (this.select) {
                        target.moveCaretPosition(offs);
                    } else {
                        target.setCaretPosition(offs);
                    }
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class NextWordAction
    extends TextAction {
        private boolean select;

        NextWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                boolean failed = false;
                int oldOffs = offs;
                Element curPara = Utilities.getParagraphElement(target, offs);
                try {
                    offs = Utilities.getNextWord(target, offs);
                    if (offs >= curPara.getEndOffset() && oldOffs != curPara.getEndOffset() - 1) {
                        offs = curPara.getEndOffset() - 1;
                    }
                }
                catch (BadLocationException bl) {
                    int end = target.getDocument().getLength();
                    if (offs != end) {
                        offs = oldOffs != curPara.getEndOffset() - 1 ? curPara.getEndOffset() - 1 : end;
                    }
                    failed = true;
                }
                if (!failed) {
                    if (this.select) {
                        target.moveCaretPosition(offs);
                    } else {
                        target.setCaretPosition(offs);
                    }
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class BeginLineAction
    extends TextAction {
        private boolean select;

        BeginLineAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int begOffs = Utilities.getRowStart(target, offs);
                    if (this.select) {
                        target.moveCaretPosition(begOffs);
                    } else {
                        target.setCaretPosition(begOffs);
                    }
                }
                catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class BeginLineUpAction
    extends TextAction {
        private boolean select;
        private int direction;
        private boolean firstLine;

        BeginLineUpAction(String nm, boolean select, int direction) {
            super(nm);
            this.select = select;
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int begOffs = Utilities.getRowStart(target, offs);
                    if (offs != begOffs) {
                        if (this.select) {
                            target.moveCaretPosition(begOffs);
                        } else {
                            target.setCaretPosition(begOffs);
                        }
                    } else {
                        NavigationFilter filter;
                        if (this.select) {
                            target.moveCaretPosition(begOffs);
                        } else {
                            target.setCaretPosition(begOffs);
                        }
                        Caret caret = target.getCaret();
                        DefaultCaret bidiCaret = caret instanceof DefaultCaret ? (DefaultCaret)caret : null;
                        int dot = caret.getDot();
                        Position.Bias[] bias = new Position.Bias[1];
                        Point magicPosition = caret.getMagicCaretPosition();
                        if (magicPosition == null && (this.direction == 1 || this.direction == 5)) {
                            Rectangle r = bidiCaret != null ? target.getUI().modelToView(target, dot, bidiCaret.getDotBias()) : target.modelToView(dot);
                            magicPosition = new Point(r.x, r.y);
                        }
                        dot = (filter = target.getNavigationFilter()) != null ? filter.getNextVisualPositionFrom(target, dot, bidiCaret != null ? bidiCaret.getDotBias() : Position.Bias.Forward, this.direction, bias) : target.getUI().getNextVisualPositionFrom(target, dot, bidiCaret != null ? bidiCaret.getDotBias() : Position.Bias.Forward, this.direction, bias);
                        if (bias[0] == null) {
                            bias[0] = Position.Bias.Forward;
                        }
                        if (bidiCaret != null) {
                            if (this.select) {
                                bidiCaret.moveDot(dot, bias[0]);
                            } else {
                                bidiCaret.setDot(dot, bias[0]);
                            }
                        } else if (this.select) {
                            caret.moveDot(dot);
                        } else {
                            caret.setDot(dot);
                        }
                        if (magicPosition != null && (this.direction == 1 || this.direction == 5)) {
                            target.getCaret().setMagicCaretPosition(magicPosition);
                        }
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }
    }

    static class EndLineAction
    extends TextAction {
        private boolean select;

        EndLineAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int endOffs = Utilities.getRowEnd(target, offs);
                    if (this.select) {
                        target.moveCaretPosition(endOffs);
                    } else {
                        target.setCaretPosition(endOffs);
                    }
                }
                catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }

    static class EndLineDownAction
    extends TextAction {
        private boolean select;
        private int direction;
        private boolean firstTime;

        EndLineDownAction(String nm, boolean select, int direction) {
            super(nm);
            this.select = select;
            this.direction = direction;
            this.firstTime = true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int endOffs = Utilities.getRowEnd(target, offs);
                    if (offs != endOffs) {
                        if (this.select) {
                            target.moveCaretPosition(endOffs);
                        } else {
                            target.setCaretPosition(endOffs);
                        }
                    } else {
                        NavigationFilter filter;
                        Caret caret = target.getCaret();
                        DefaultCaret bidiCaret = caret instanceof DefaultCaret ? (DefaultCaret)caret : null;
                        int dot = caret.getDot();
                        Position.Bias[] bias = new Position.Bias[1];
                        Point magicPosition = caret.getMagicCaretPosition();
                        if (magicPosition == null && (this.direction == 1 || this.direction == 5)) {
                            Rectangle r = bidiCaret != null ? target.getUI().modelToView(target, dot, bidiCaret.getDotBias()) : target.modelToView(dot);
                            magicPosition = new Point(r.x, r.y);
                        }
                        dot = (filter = target.getNavigationFilter()) != null ? filter.getNextVisualPositionFrom(target, dot, bidiCaret != null ? bidiCaret.getDotBias() : Position.Bias.Forward, this.direction, bias) : target.getUI().getNextVisualPositionFrom(target, dot, bidiCaret != null ? bidiCaret.getDotBias() : Position.Bias.Forward, this.direction, bias);
                        if (bias[0] == null) {
                            bias[0] = Position.Bias.Forward;
                        }
                        if (bidiCaret != null) {
                            if (this.select) {
                                bidiCaret.moveDot(dot, bias[0]);
                            } else {
                                bidiCaret.setDot(dot, bias[0]);
                            }
                        } else if (this.select) {
                            caret.moveDot(dot);
                        } else {
                            caret.setDot(dot);
                        }
                        if (magicPosition != null && (this.direction == 1 || this.direction == 5)) {
                            target.getCaret().setMagicCaretPosition(magicPosition);
                        }
                        offs = target.getCaretPosition();
                        endOffs = Utilities.getRowEnd(target, offs);
                        if (this.select) {
                            target.moveCaretPosition(endOffs);
                        } else {
                            target.setCaretPosition(endOffs);
                        }
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }
    }

    static class BeginParagraphAction
    extends TextAction {
        private boolean select;

        BeginParagraphAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                Element elem = Utilities.getParagraphElement(target, offs);
                offs = elem.getStartOffset();
                if (this.select) {
                    target.moveCaretPosition(offs);
                } else {
                    target.setCaretPosition(offs);
                }
            }
        }
    }

    static class EndParagraphAction
    extends TextAction {
        private boolean select;

        EndParagraphAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                Element elem = Utilities.getParagraphElement(target, offs);
                offs = Math.min(target.getDocument().getLength(), elem.getEndOffset());
                if (this.select) {
                    target.moveCaretPosition(offs);
                } else {
                    target.setCaretPosition(offs);
                }
            }
        }
    }

    static class BeginAction
    extends TextAction {
        private boolean select;

        BeginAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                if (this.select) {
                    target.moveCaretPosition(0);
                } else {
                    target.setCaretPosition(0);
                }
            }
        }
    }

    static class EndAction
    extends TextAction {
        private boolean select;

        EndAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                int dot = doc.getLength();
                if (this.select) {
                    target.moveCaretPosition(dot);
                } else {
                    target.setCaretPosition(dot);
                }
            }
        }
    }

    public static class DefaultKeyTypedAction
    extends TextAction {
        public DefaultKeyTypedAction() {
            super(DefaultEditorKit.defaultKeyTypedAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null && e != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    return;
                }
                String content = e.getActionCommand();
                int mod = e.getModifiers();
                if (content != null && content.length() > 0) {
                    boolean isPrintableMask = true;
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    if (tk instanceof SunToolkit) {
                        isPrintableMask = ((SunToolkit)tk).isPrintableCharacterModifiersMask(mod);
                    }
                    char c = content.charAt(0);
                    if (isPrintableMask && c >= ' ' && c != '\u007f' || !isPrintableMask && c >= '\u200c' && c <= '\u200d') {
                        target.replaceSelection(content);
                    }
                }
            }
        }
    }

    public static class InsertTabAction
    extends TextAction {
        public InsertTabAction() {
            super(DefaultEditorKit.insertTabAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                target.replaceSelection("\t");
            }
        }
    }

    static class SelectWordAction
    extends TextAction {
        private Action start = new BeginWordAction("pigdog", false);
        private Action end = new EndWordAction("pigdog", true);

        SelectWordAction() {
            super(DefaultEditorKit.selectWordAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.start.actionPerformed(e);
            this.end.actionPerformed(e);
        }
    }

    static class SelectLineAction
    extends TextAction {
        private Action start = new BeginLineAction("pigdog", false);
        private Action end = new EndLineAction("pigdog", true);

        SelectLineAction() {
            super(DefaultEditorKit.selectLineAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.start.actionPerformed(e);
            this.end.actionPerformed(e);
        }
    }

    static class SelectParagraphAction
    extends TextAction {
        private Action start = new BeginParagraphAction("pigdog", false);
        private Action end = new EndParagraphAction("pigdog", true);

        SelectParagraphAction() {
            super(DefaultEditorKit.selectParagraphAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.start.actionPerformed(e);
            this.end.actionPerformed(e);
        }
    }

    static class SelectAllAction
    extends TextAction {
        SelectAllAction() {
            super(DefaultEditorKit.selectAllAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                target.setCaretPosition(0);
                target.moveCaretPosition(doc.getLength());
            }
        }
    }

    static class UnselectAction
    extends TextAction {
        UnselectAction() {
            super(DefaultEditorKit.unselectAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                target.setCaretPosition(target.getCaretPosition());
            }
        }
    }

    static class ToggleComponentOrientationAction
    extends TextAction {
        ToggleComponentOrientationAction() {
            super(DefaultEditorKit.toggleComponentOrientationAction);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            if (target != null) {
                ComponentOrientation last = target.getComponentOrientation();
                ComponentOrientation next = last == ComponentOrientation.RIGHT_TO_LEFT ? ComponentOrientation.LEFT_TO_RIGHT : ComponentOrientation.RIGHT_TO_LEFT;
                target.setComponentOrientation(next);
                target.repaint();
            }
        }
    }

    static class DumpModelAction
    extends TextAction {
        DumpModelAction() {
            super("dump-model");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Document d;
            JTextComponent target = this.getTextComponent(e);
            if (target != null && (d = target.getDocument()) instanceof AbstractDocument) {
                ((AbstractDocument)d).dump(System.err);
            }
        }
    }
}

