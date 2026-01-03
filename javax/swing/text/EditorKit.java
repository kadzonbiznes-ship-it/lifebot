/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;

public abstract class EditorKit
implements Cloneable,
Serializable {
    public Object clone() {
        Object o;
        try {
            o = super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            o = null;
        }
        return o;
    }

    public void install(JEditorPane c) {
    }

    public void deinstall(JEditorPane c) {
    }

    public abstract String getContentType();

    public abstract ViewFactory getViewFactory();

    public abstract Action[] getActions();

    public abstract Caret createCaret();

    public abstract Document createDefaultDocument();

    public abstract void read(InputStream var1, Document var2, int var3) throws IOException, BadLocationException;

    public abstract void write(OutputStream var1, Document var2, int var3, int var4) throws IOException, BadLocationException;

    public abstract void read(Reader var1, Document var2, int var3) throws IOException, BadLocationException;

    public abstract void write(Writer var1, Document var2, int var3, int var4) throws IOException, BadLocationException;
}

