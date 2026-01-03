/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.Caret;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TextAction;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class StyledEditorKit
extends DefaultEditorKit {
    private static final ViewFactory defaultFactory = new StyledViewFactory();
    Element currentRun;
    Element currentParagraph;
    MutableAttributeSet inputAttributes;
    private AttributeTracker inputAttributeUpdater;
    private static final Action[] defaultActions = new Action[]{new FontFamilyAction("font-family-SansSerif", "SansSerif"), new FontFamilyAction("font-family-Monospaced", "Monospaced"), new FontFamilyAction("font-family-Serif", "Serif"), new FontSizeAction("font-size-8", 8), new FontSizeAction("font-size-10", 10), new FontSizeAction("font-size-12", 12), new FontSizeAction("font-size-14", 14), new FontSizeAction("font-size-16", 16), new FontSizeAction("font-size-18", 18), new FontSizeAction("font-size-24", 24), new FontSizeAction("font-size-36", 36), new FontSizeAction("font-size-48", 48), new AlignmentAction("left-justify", 0), new AlignmentAction("center-justify", 1), new AlignmentAction("right-justify", 2), new BoldAction(), new ItalicAction(), new StyledInsertBreakAction(), new UnderlineAction()};

    public StyledEditorKit() {
        this.createInputAttributeUpdated();
        this.createInputAttributes();
    }

    @Override
    public MutableAttributeSet getInputAttributes() {
        return this.inputAttributes;
    }

    public Element getCharacterAttributeRun() {
        return this.currentRun;
    }

    @Override
    public Action[] getActions() {
        return TextAction.augmentList(super.getActions(), defaultActions);
    }

    @Override
    public Document createDefaultDocument() {
        return new DefaultStyledDocument();
    }

    @Override
    public void install(JEditorPane c) {
        c.addCaretListener(this.inputAttributeUpdater);
        c.addPropertyChangeListener(this.inputAttributeUpdater);
        Caret caret = c.getCaret();
        if (caret != null) {
            this.inputAttributeUpdater.updateInputAttributes(caret.getDot(), caret.getMark(), c);
        }
    }

    @Override
    public void deinstall(JEditorPane c) {
        c.removeCaretListener(this.inputAttributeUpdater);
        c.removePropertyChangeListener(this.inputAttributeUpdater);
        this.currentRun = null;
        this.currentParagraph = null;
    }

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    @Override
    public Object clone() {
        StyledEditorKit o = (StyledEditorKit)super.clone();
        o.currentParagraph = null;
        o.currentRun = null;
        o.createInputAttributeUpdated();
        o.createInputAttributes();
        return o;
    }

    private void createInputAttributes() {
        this.inputAttributes = new SimpleAttributeSet(){

            @Override
            public AttributeSet getResolveParent() {
                return StyledEditorKit.this.currentParagraph != null ? StyledEditorKit.this.currentParagraph.getAttributes() : null;
            }

            @Override
            public Object clone() {
                return new SimpleAttributeSet(this);
            }
        };
    }

    private void createInputAttributeUpdated() {
        this.inputAttributeUpdater = new AttributeTracker();
    }

    protected void createInputAttributes(Element element, MutableAttributeSet set) {
        if (element.getAttributes().getAttributeCount() > 0 || element.getEndOffset() - element.getStartOffset() > 1 || element.getEndOffset() < element.getDocument().getLength()) {
            set.removeAttributes(set);
            set.addAttributes(element.getAttributes());
            set.removeAttribute(StyleConstants.ComponentAttribute);
            set.removeAttribute(StyleConstants.IconAttribute);
            set.removeAttribute("$ename");
            set.removeAttribute(StyleConstants.ComposedTextAttribute);
        }
    }

    class AttributeTracker
    implements CaretListener,
    PropertyChangeListener,
    Serializable {
        AttributeTracker() {
        }

        void updateInputAttributes(int dot, int mark, JTextComponent c) {
            Document aDoc = c.getDocument();
            if (!(aDoc instanceof StyledDocument)) {
                return;
            }
            int start = Math.min(dot, mark);
            StyledDocument doc = (StyledDocument)aDoc;
            StyledEditorKit.this.currentParagraph = doc.getParagraphElement(start);
            Element run = StyledEditorKit.this.currentParagraph.getStartOffset() == start || dot != mark ? doc.getCharacterElement(start) : doc.getCharacterElement(Math.max(start - 1, 0));
            if (run != StyledEditorKit.this.currentRun) {
                StyledEditorKit.this.currentRun = run;
                StyledEditorKit.this.createInputAttributes(StyledEditorKit.this.currentRun, StyledEditorKit.this.getInputAttributes());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object newValue = evt.getNewValue();
            Object source = evt.getSource();
            if (source instanceof JTextComponent && newValue instanceof Document) {
                this.updateInputAttributes(0, 0, (JTextComponent)source);
            }
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            this.updateInputAttributes(e.getDot(), e.getMark(), (JTextComponent)e.getSource());
        }
    }

    static class StyledViewFactory
    implements ViewFactory {
        StyledViewFactory() {
        }

        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals("content")) {
                    return new LabelView(elem);
                }
                if (kind.equals("paragraph")) {
                    return new ParagraphView(elem);
                }
                if (kind.equals("section")) {
                    return new BoxView(elem, 1);
                }
                if (kind.equals("component")) {
                    return new ComponentView(elem);
                }
                if (kind.equals("icon")) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    public static class FontFamilyAction
    extends StyledTextAction {
        private String family;

        public FontFamilyAction(String nm, String family) {
            super(nm);
            this.family = family;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane editor = this.getEditor(e);
            if (editor != null) {
                String s;
                String family = this.family;
                if (e != null && e.getSource() == editor && (s = e.getActionCommand()) != null) {
                    family = s;
                }
                if (family != null) {
                    SimpleAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setFontFamily(attr, family);
                    this.setCharacterAttributes(editor, attr, false);
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(editor);
                }
            }
        }
    }

    public static class FontSizeAction
    extends StyledTextAction {
        private int size;

        public FontSizeAction(String nm, int size) {
            super(nm);
            this.size = size;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane editor = this.getEditor(e);
            if (editor != null) {
                int size = this.size;
                if (e != null && e.getSource() == editor) {
                    String s = e.getActionCommand();
                    try {
                        size = Integer.parseInt(s, 10);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if (size != 0) {
                    SimpleAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setFontSize(attr, size);
                    this.setCharacterAttributes(editor, attr, false);
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(editor);
                }
            }
        }
    }

    public static class AlignmentAction
    extends StyledTextAction {
        private int a;

        public AlignmentAction(String nm, int a) {
            super(nm);
            this.a = a;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane editor = this.getEditor(e);
            if (editor != null) {
                int a = this.a;
                if (e != null && e.getSource() == editor) {
                    String s = e.getActionCommand();
                    try {
                        a = Integer.parseInt(s, 10);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setAlignment(attr, a);
                this.setParagraphAttributes(editor, attr, false);
            }
        }
    }

    public static class BoldAction
    extends StyledTextAction {
        public BoldAction() {
            super("font-bold");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane editor = this.getEditor(e);
            if (editor != null) {
                StyledEditorKit kit = this.getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean bold = !StyleConstants.isBold(attr);
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setBold(sas, bold);
                this.setCharacterAttributes(editor, sas, false);
            }
        }
    }

    public static class ItalicAction
    extends StyledTextAction {
        public ItalicAction() {
            super("font-italic");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane editor = this.getEditor(e);
            if (editor != null) {
                StyledEditorKit kit = this.getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean italic = !StyleConstants.isItalic(attr);
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setItalic(sas, italic);
                this.setCharacterAttributes(editor, sas, false);
            }
        }
    }

    static class StyledInsertBreakAction
    extends StyledTextAction {
        private SimpleAttributeSet tempSet;

        StyledInsertBreakAction() {
            super("insert-break");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane target = this.getEditor(e);
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                StyledEditorKit sek = this.getStyledEditorKit(target);
                if (this.tempSet != null) {
                    this.tempSet.removeAttributes(this.tempSet);
                } else {
                    this.tempSet = new SimpleAttributeSet();
                }
                this.tempSet.addAttributes(sek.getInputAttributes());
                target.replaceSelection("\n");
                MutableAttributeSet ia = sek.getInputAttributes();
                ia.removeAttributes(ia);
                ia.addAttributes(this.tempSet);
                this.tempSet.removeAttributes(this.tempSet);
            } else {
                JTextComponent text = this.getTextComponent(e);
                if (text != null) {
                    if (!text.isEditable() || !text.isEnabled()) {
                        UIManager.getLookAndFeel().provideErrorFeedback(target);
                        return;
                    }
                    text.replaceSelection("\n");
                }
            }
        }
    }

    public static class UnderlineAction
    extends StyledTextAction {
        public UnderlineAction() {
            super("font-underline");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane editor = this.getEditor(e);
            if (editor != null) {
                StyledEditorKit kit = this.getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean underline = !StyleConstants.isUnderline(attr);
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setUnderline(sas, underline);
                this.setCharacterAttributes(editor, sas, false);
            }
        }
    }

    public static class ForegroundAction
    extends StyledTextAction {
        private Color fg;

        public ForegroundAction(String nm, Color fg) {
            super(nm);
            this.fg = fg;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JEditorPane editor = this.getEditor(e);
            if (editor != null) {
                Color fg = this.fg;
                if (e != null && e.getSource() == editor) {
                    String s = e.getActionCommand();
                    try {
                        fg = Color.decode(s);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if (fg != null) {
                    SimpleAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setForeground(attr, fg);
                    this.setCharacterAttributes(editor, attr, false);
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(editor);
                }
            }
        }
    }

    public static abstract class StyledTextAction
    extends TextAction {
        public StyledTextAction(String nm) {
            super(nm);
        }

        protected final JEditorPane getEditor(ActionEvent e) {
            JTextComponent tcomp = this.getTextComponent(e);
            if (tcomp instanceof JEditorPane) {
                return (JEditorPane)tcomp;
            }
            return null;
        }

        protected final StyledDocument getStyledDocument(JEditorPane e) {
            Document d = e.getDocument();
            if (d instanceof StyledDocument) {
                return (StyledDocument)d;
            }
            throw new IllegalArgumentException("document must be StyledDocument");
        }

        protected final StyledEditorKit getStyledEditorKit(JEditorPane e) {
            EditorKit k = e.getEditorKit();
            if (k instanceof StyledEditorKit) {
                return (StyledEditorKit)k;
            }
            throw new IllegalArgumentException("EditorKit must be StyledEditorKit");
        }

        protected final void setCharacterAttributes(JEditorPane editor, AttributeSet attr, boolean replace) {
            int p1;
            int p0 = editor.getSelectionStart();
            if (p0 != (p1 = editor.getSelectionEnd())) {
                StyledDocument doc = this.getStyledDocument(editor);
                doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
            }
            StyledEditorKit k = this.getStyledEditorKit(editor);
            MutableAttributeSet inputAttributes = k.getInputAttributes();
            if (replace) {
                inputAttributes.removeAttributes(inputAttributes);
            }
            inputAttributes.addAttributes(attr);
        }

        protected final void setParagraphAttributes(JEditorPane editor, AttributeSet attr, boolean replace) {
            int p0 = editor.getSelectionStart();
            int p1 = editor.getSelectionEnd();
            StyledDocument doc = this.getStyledDocument(editor);
            doc.setParagraphAttributes(p0, p1 - p0, attr, replace);
        }
    }
}

