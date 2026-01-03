/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingContainer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

@JavaBean(defaultProperty="UIClassID", description="A multi-line area that displays plain text.")
@SwingContainer(value=false)
public class JTextArea
extends JTextComponent {
    private static final String uiClassID = "TextAreaUI";
    private int rows;
    private int columns;
    private int columnWidth;
    private int rowHeight;
    private boolean wrap;
    private boolean word;

    public JTextArea() {
        this(null, null, 0, 0);
    }

    public JTextArea(String text) {
        this(null, text, 0, 0);
    }

    public JTextArea(int rows, int columns) {
        this(null, null, rows, columns);
    }

    public JTextArea(String text, int rows, int columns) {
        this(null, text, rows, columns);
    }

    public JTextArea(Document doc) {
        this(doc, null, 0, 0);
    }

    public JTextArea(Document doc, String text, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        if (doc == null) {
            doc = this.createDefaultModel();
        }
        this.setDocument(doc);
        if (text != null) {
            this.setText(text);
            this.select(0, 0);
        }
        if (rows < 0) {
            throw new IllegalArgumentException("rows: " + rows);
        }
        if (columns < 0) {
            throw new IllegalArgumentException("columns: " + columns);
        }
        LookAndFeel.installProperty(this, "focusTraversalKeysForward", JComponent.getManagingFocusForwardTraversalKeys());
        LookAndFeel.installProperty(this, "focusTraversalKeysBackward", JComponent.getManagingFocusBackwardTraversalKeys());
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    protected Document createDefaultModel() {
        return new PlainDocument();
    }

    @BeanProperty(preferred=true, description="the number of characters to expand tabs to")
    public void setTabSize(int size) {
        Document doc = this.getDocument();
        if (doc != null) {
            int old = this.getTabSize();
            doc.putProperty("tabSize", size);
            this.firePropertyChange("tabSize", old, size);
        }
    }

    public int getTabSize() {
        Integer i;
        int size = 8;
        Document doc = this.getDocument();
        if (doc != null && (i = (Integer)doc.getProperty("tabSize")) != null) {
            size = i;
        }
        return size;
    }

    @BeanProperty(preferred=true, description="should lines be wrapped")
    public void setLineWrap(boolean wrap) {
        boolean old = this.wrap;
        this.wrap = wrap;
        this.firePropertyChange("lineWrap", old, wrap);
    }

    public boolean getLineWrap() {
        return this.wrap;
    }

    @BeanProperty(description="should wrapping occur at word boundaries")
    public void setWrapStyleWord(boolean word) {
        boolean old = this.word;
        this.word = word;
        this.firePropertyChange("wrapStyleWord", old, word);
    }

    public boolean getWrapStyleWord() {
        return this.word;
    }

    public int getLineOfOffset(int offset) throws BadLocationException {
        Document doc = this.getDocument();
        if (offset < 0) {
            throw new BadLocationException("Can't translate offset to line", -1);
        }
        if (offset > doc.getLength()) {
            throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
        }
        Element map = this.getDocument().getDefaultRootElement();
        return map.getElementIndex(offset);
    }

    @BeanProperty(bound=false)
    public int getLineCount() {
        Element map = this.getDocument().getDefaultRootElement();
        return map.getElementCount();
    }

    public int getLineStartOffset(int line) throws BadLocationException {
        int lineCount = this.getLineCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        }
        if (line >= lineCount) {
            throw new BadLocationException("No such line", this.getDocument().getLength() + 1);
        }
        Element map = this.getDocument().getDefaultRootElement();
        Element lineElem = map.getElement(line);
        return lineElem.getStartOffset();
    }

    public int getLineEndOffset(int line) throws BadLocationException {
        int lineCount = this.getLineCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        }
        if (line >= lineCount) {
            throw new BadLocationException("No such line", this.getDocument().getLength() + 1);
        }
        Element map = this.getDocument().getDefaultRootElement();
        Element lineElem = map.getElement(line);
        int endOffset = lineElem.getEndOffset();
        return line == lineCount - 1 ? endOffset - 1 : endOffset;
    }

    public void insert(String str, int pos) {
        Document doc = this.getDocument();
        if (doc != null) {
            try {
                doc.insertString(pos, str, null);
            }
            catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public void append(String str) {
        Document doc = this.getDocument();
        if (doc != null) {
            try {
                doc.insertString(doc.getLength(), str, null);
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
        }
    }

    public void replaceRange(String str, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end before start");
        }
        Document doc = this.getDocument();
        if (doc != null) {
            try {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).replace(start, end - start, str, null);
                } else {
                    doc.remove(start, end - start);
                    doc.insertString(start, str, null);
                }
            }
            catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public int getRows() {
        return this.rows;
    }

    @BeanProperty(bound=false, description="the number of rows preferred for display")
    public void setRows(int rows) {
        int oldVal = this.rows;
        if (rows < 0) {
            throw new IllegalArgumentException("rows less than zero.");
        }
        if (rows != oldVal) {
            this.rows = rows;
            this.invalidate();
        }
    }

    protected int getRowHeight() {
        if (this.rowHeight == 0) {
            FontMetrics metrics = this.getFontMetrics(this.getFont());
            this.rowHeight = metrics.getHeight();
        }
        return this.rowHeight;
    }

    public int getColumns() {
        return this.columns;
    }

    @BeanProperty(bound=false, description="the number of columns preferred for display")
    public void setColumns(int columns) {
        int oldVal = this.columns;
        if (columns < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        if (columns != oldVal) {
            this.columns = columns;
            this.invalidate();
        }
    }

    protected int getColumnWidth() {
        if (this.columnWidth == 0) {
            FontMetrics metrics = this.getFontMetrics(this.getFont());
            this.columnWidth = metrics.charWidth('m');
        }
        return this.columnWidth;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d = d == null ? new Dimension(400, 400) : d;
        Insets insets = this.getInsets();
        if (this.columns != 0) {
            d.width = Math.max(d.width, this.columns * this.getColumnWidth() + insets.left + insets.right);
        }
        if (this.rows != 0) {
            d.height = Math.max(d.height, this.rows * this.getRowHeight() + insets.top + insets.bottom);
        }
        return d;
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        this.rowHeight = 0;
        this.columnWidth = 0;
    }

    @Override
    protected String paramString() {
        String wrapString = this.wrap ? "true" : "false";
        String wordString = this.word ? "true" : "false";
        return super.paramString() + ",columns=" + this.columns + ",columWidth=" + this.columnWidth + ",rows=" + this.rows + ",rowHeight=" + this.rowHeight + ",word=" + wordString + ",wrap=" + wrapString;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportWidth() {
        return this.wrap ? true : super.getScrollableTracksViewportWidth();
    }

    @Override
    @BeanProperty(bound=false)
    public Dimension getPreferredScrollableViewportSize() {
        Dimension size = super.getPreferredScrollableViewportSize();
        size = size == null ? new Dimension(400, 400) : size;
        Insets insets = this.getInsets();
        size.width = this.columns == 0 ? size.width : this.columns * this.getColumnWidth() + insets.left + insets.right;
        size.height = this.rows == 0 ? size.height : this.rows * this.getRowHeight() + insets.top + insets.bottom;
        return size;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
            case 1: {
                return this.getRowHeight();
            }
            case 0: {
                return this.getColumnWidth();
            }
        }
        throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (this.getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            count = (byte)(count - 1);
            JComponent.setWriteObjCounter(this, count);
            if (count == 0 && this.ui != null) {
                this.ui.installUI(this);
            }
        }
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJTextArea();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJTextArea
    extends JTextComponent.AccessibleJTextComponent {
        protected AccessibleJTextArea() {
            super(JTextArea.this);
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            return states;
        }
    }
}

