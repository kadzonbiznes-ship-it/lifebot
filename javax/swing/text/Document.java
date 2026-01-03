/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

public interface Document {
    public static final String StreamDescriptionProperty = "stream";
    public static final String TitleProperty = "title";

    public int getLength();

    public void addDocumentListener(DocumentListener var1);

    public void removeDocumentListener(DocumentListener var1);

    public void addUndoableEditListener(UndoableEditListener var1);

    public void removeUndoableEditListener(UndoableEditListener var1);

    public Object getProperty(Object var1);

    public void putProperty(Object var1, Object var2);

    public void remove(int var1, int var2) throws BadLocationException;

    public void insertString(int var1, String var2, AttributeSet var3) throws BadLocationException;

    public String getText(int var1, int var2) throws BadLocationException;

    public void getText(int var1, int var2, Segment var3) throws BadLocationException;

    public Position getStartPosition();

    public Position getEndPosition();

    public Position createPosition(int var1) throws BadLocationException;

    public Element[] getRootElements();

    public Element getDefaultRootElement();

    public void render(Runnable var1);
}

