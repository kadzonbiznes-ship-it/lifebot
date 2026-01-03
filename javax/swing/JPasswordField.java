/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleTextSequence;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingContainer;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Segment;

@JavaBean(description="Allows the editing of a line of text but doesn't show the characters.")
@SwingContainer(value=false)
public class JPasswordField
extends JTextField {
    private static final String uiClassID = "PasswordFieldUI";
    private char echoChar;
    private boolean echoCharSet = false;

    public JPasswordField() {
        this(null, null, 0);
    }

    public JPasswordField(String text) {
        this(null, text, 0);
    }

    public JPasswordField(int columns) {
        this(null, null, columns);
    }

    public JPasswordField(String text, int columns) {
        this(null, text, columns);
    }

    public JPasswordField(Document doc, String txt, int columns) {
        super(doc, txt, columns);
        this.enableInputMethods(false);
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public void updateUI() {
        if (!this.echoCharSet) {
            this.echoChar = (char)42;
        }
        super.updateUI();
    }

    public char getEchoChar() {
        return this.echoChar;
    }

    @BeanProperty(bound=false, visualUpdate=true, description="character to display in place of the real characters")
    public void setEchoChar(char c) {
        this.echoChar = c;
        this.echoCharSet = true;
        this.repaint();
        this.revalidate();
    }

    public boolean echoCharIsSet() {
        return this.echoChar != '\u0000';
    }

    @Override
    public void cut() {
        if (this.getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        } else {
            super.cut();
        }
    }

    @Override
    public void copy() {
        if (this.getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        } else {
            super.copy();
        }
    }

    @Override
    @Deprecated
    public String getText() {
        return super.getText();
    }

    @Override
    @Deprecated
    public String getText(int offs, int len) throws BadLocationException {
        return super.getText(offs, len);
    }

    @Override
    @BeanProperty(bound=false, description="the text of this component")
    public void setText(String t) {
        Document doc = this.getDocument();
        DocumentFilter filter = null;
        if (doc instanceof AbstractDocument) {
            AbstractDocument adoc = (AbstractDocument)doc;
            filter = adoc.getDocumentFilter();
        }
        if (filter == null) {
            int nleft = doc.getLength();
            Segment text = new Segment();
            text.setPartialReturn(true);
            int offs = 0;
            try {
                while (nleft > 0) {
                    doc.getText(offs, nleft, text);
                    Arrays.fill(text.array, text.offset, text.count + text.offset, '\u0000');
                    nleft -= text.count;
                    offs += text.count;
                }
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
        }
        super.setText(t);
    }

    @BeanProperty(bound=false)
    public char[] getPassword() {
        Document doc = this.getDocument();
        Segment txt = new Segment();
        try {
            doc.getText(0, doc.getLength(), txt);
        }
        catch (BadLocationException e) {
            return null;
        }
        char[] retValue = new char[txt.count];
        System.arraycopy(txt.array, txt.offset, retValue, 0, txt.count);
        return retValue;
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
    protected String paramString() {
        return super.paramString() + ",echoChar=" + this.echoChar;
    }

    boolean customSetUIProperty(String propertyName, Object value) {
        if (propertyName == "echoChar") {
            if (!this.echoCharSet) {
                this.setEchoChar(((Character)value).charValue());
                this.echoCharSet = false;
            }
            return true;
        }
        return false;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJPasswordField();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJPasswordField
    extends JTextField.AccessibleJTextField {
        protected AccessibleJPasswordField() {
            super(JPasswordField.this);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PASSWORD_TEXT;
        }

        @Override
        public AccessibleText getAccessibleText() {
            return this;
        }

        private String getEchoString(String str) {
            if (str == null) {
                return null;
            }
            char[] buffer = new char[str.length()];
            Arrays.fill(buffer, JPasswordField.this.getEchoChar());
            return new String(buffer);
        }

        @Override
        public String getAtIndex(int part, int index) {
            if (part == 1) {
                return this.getEchoString(super.getAtIndex(part, index));
            }
            int length = JPasswordField.this.getDocument().getLength();
            if (index < 0 || index >= length) {
                return null;
            }
            char[] password = new char[length];
            Arrays.fill(password, JPasswordField.this.getEchoChar());
            return new String(password);
        }

        @Override
        public String getAfterIndex(int part, int index) {
            if (part == 1) {
                return this.getEchoString(super.getAfterIndex(part, index));
            }
            return null;
        }

        @Override
        public String getBeforeIndex(int part, int index) {
            if (part == 1) {
                return this.getEchoString(super.getBeforeIndex(part, index));
            }
            return null;
        }

        @Override
        public String getTextRange(int startIndex, int endIndex) {
            String str = super.getTextRange(startIndex, endIndex);
            return this.getEchoString(str);
        }

        @Override
        public AccessibleTextSequence getTextSequenceAt(int part, int index) {
            if (part == 1) {
                AccessibleTextSequence seq = super.getTextSequenceAt(part, index);
                if (seq == null) {
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex, seq.endIndex, this.getEchoString(seq.text));
            }
            int length = JPasswordField.this.getDocument().getLength();
            if (index < 0 || index >= length) {
                return null;
            }
            char[] password = new char[length];
            Arrays.fill(password, JPasswordField.this.getEchoChar());
            String text = new String(password);
            return new AccessibleTextSequence(0, password.length - 1, text);
        }

        @Override
        public AccessibleTextSequence getTextSequenceAfter(int part, int index) {
            if (part == 1) {
                AccessibleTextSequence seq = super.getTextSequenceAfter(part, index);
                if (seq == null) {
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex, seq.endIndex, this.getEchoString(seq.text));
            }
            return null;
        }

        @Override
        public AccessibleTextSequence getTextSequenceBefore(int part, int index) {
            if (part == 1) {
                AccessibleTextSequence seq = super.getTextSequenceBefore(part, index);
                if (seq == null) {
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex, seq.endIndex, this.getEchoString(seq.text));
            }
            return null;
        }
    }
}

