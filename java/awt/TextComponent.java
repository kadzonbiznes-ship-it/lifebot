/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextComponentPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.BreakIterator;
import java.util.EventListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.swing.text.AttributeSet;
import sun.awt.AWTPermissions;
import sun.awt.InputMethodSupport;

public sealed class TextComponent
extends Component
implements Accessible
permits TextArea, TextField {
    String text;
    boolean editable = true;
    int selectionStart;
    int selectionEnd;
    boolean backgroundSetByClientCode = false;
    protected transient TextListener textListener;
    private static final long serialVersionUID = -2214773872412987419L;
    private int textComponentSerializedDataVersion = 1;
    private boolean checkForEnableIM = true;

    TextComponent(String text) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.text = text != null ? text : "";
        this.setCursor(Cursor.getPredefinedCursor(2));
    }

    private void enableInputMethodsIfNecessary() {
        if (this.checkForEnableIM) {
            this.checkForEnableIM = false;
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                boolean shouldEnable = false;
                if (toolkit instanceof InputMethodSupport) {
                    shouldEnable = ((InputMethodSupport)((Object)toolkit)).enableInputMethodsForTextComponent();
                }
                this.enableInputMethods(shouldEnable);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    public void enableInputMethods(boolean enable) {
        this.checkForEnableIM = false;
        super.enableInputMethods(enable);
    }

    @Override
    boolean areInputMethodsEnabled() {
        if (this.checkForEnableIM) {
            this.enableInputMethodsIfNecessary();
        }
        return (this.eventMask & 0x1000L) != 0L;
    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            return peer.getInputMethodRequests();
        }
        return null;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.enableInputMethodsIfNecessary();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            TextComponentPeer peer = (TextComponentPeer)this.peer;
            if (peer != null) {
                this.text = peer.getText();
                this.selectionStart = peer.getSelectionStart();
                this.selectionEnd = peer.getSelectionEnd();
            }
            super.removeNotify();
        }
    }

    public synchronized void setText(String t) {
        this.text = t != null ? t : "";
        int selectionStart = this.getSelectionStart();
        int selectionEnd = this.getSelectionEnd();
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null && !this.text.equals(peer.getText())) {
            peer.setText(this.text);
        }
        if (selectionStart != selectionEnd) {
            this.select(selectionStart, selectionEnd);
        }
    }

    public synchronized String getText() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            this.text = peer.getText();
        }
        return this.text;
    }

    public synchronized String getSelectedText() {
        return this.getText().substring(this.getSelectionStart(), this.getSelectionEnd());
    }

    public boolean isEditable() {
        return this.editable;
    }

    public synchronized void setEditable(boolean b) {
        if (this.editable == b) {
            return;
        }
        this.editable = b;
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.setEditable(b);
        }
    }

    @Override
    public Color getBackground() {
        if (!this.editable && !this.backgroundSetByClientCode) {
            return SystemColor.control;
        }
        return super.getBackground();
    }

    @Override
    public void setBackground(Color c) {
        this.backgroundSetByClientCode = true;
        super.setBackground(c);
    }

    public synchronized int getSelectionStart() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            this.selectionStart = peer.getSelectionStart();
        }
        return this.selectionStart;
    }

    public synchronized void setSelectionStart(int selectionStart) {
        this.select(selectionStart, this.getSelectionEnd());
    }

    public synchronized int getSelectionEnd() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            this.selectionEnd = peer.getSelectionEnd();
        }
        return this.selectionEnd;
    }

    public synchronized void setSelectionEnd(int selectionEnd) {
        this.select(this.getSelectionStart(), selectionEnd);
    }

    public synchronized void select(int selectionStart, int selectionEnd) {
        String text = this.getText();
        if (selectionStart < 0) {
            selectionStart = 0;
        }
        if (selectionStart > text.length()) {
            selectionStart = text.length();
        }
        if (selectionEnd > text.length()) {
            selectionEnd = text.length();
        }
        if (selectionEnd < selectionStart) {
            selectionEnd = selectionStart;
        }
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.select(selectionStart, selectionEnd);
        }
    }

    public synchronized void selectAll() {
        this.selectionStart = 0;
        this.selectionEnd = this.getText().length();
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.select(this.selectionStart, this.selectionEnd);
        }
    }

    public synchronized void setCaretPosition(int position) {
        TextComponentPeer peer;
        if (position < 0) {
            throw new IllegalArgumentException("position less than zero.");
        }
        int maxposition = this.getText().length();
        if (position > maxposition) {
            position = maxposition;
        }
        if ((peer = (TextComponentPeer)this.peer) != null) {
            peer.setCaretPosition(position);
        } else {
            this.select(position, position);
        }
    }

    public synchronized int getCaretPosition() {
        int maxposition;
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        int position = 0;
        position = peer != null ? peer.getCaretPosition() : this.selectionStart;
        if (position > (maxposition = this.getText().length())) {
            position = maxposition;
        }
        return position;
    }

    public synchronized void addTextListener(TextListener l) {
        if (l == null) {
            return;
        }
        this.textListener = AWTEventMulticaster.add(this.textListener, l);
        this.newEventsOnly = true;
    }

    public synchronized void removeTextListener(TextListener l) {
        if (l == null) {
            return;
        }
        this.textListener = AWTEventMulticaster.remove(this.textListener, l);
    }

    public synchronized TextListener[] getTextListeners() {
        return (TextListener[])this.getListeners(TextListener.class);
    }

    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        TextListener l = null;
        if (listenerType != TextListener.class) {
            return super.getListeners(listenerType);
        }
        l = this.textListener;
        return AWTEventMulticaster.getListeners((EventListener)l, listenerType);
    }

    @Override
    boolean eventEnabled(AWTEvent e) {
        if (e.id == 900) {
            return (this.eventMask & 0x400L) != 0L || this.textListener != null;
        }
        return super.eventEnabled(e);
    }

    @Override
    protected void processEvent(AWTEvent e) {
        if (e instanceof TextEvent) {
            this.processTextEvent((TextEvent)e);
            return;
        }
        super.processEvent(e);
    }

    protected void processTextEvent(TextEvent e) {
        TextListener listener = this.textListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
                case 900: {
                    listener.textValueChanged(e);
                }
            }
        }
    }

    @Override
    protected String paramString() {
        String str = super.paramString() + ",text=" + this.getText();
        if (this.editable) {
            str = str + ",editable";
        }
        return str + ",selection=" + this.getSelectionStart() + "-" + this.getSelectionEnd();
    }

    private boolean canAccessClipboard() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return true;
        }
        try {
            sm.checkPermission(AWTPermissions.ACCESS_CLIPBOARD_PERMISSION);
            return true;
        }
        catch (SecurityException securityException) {
            return false;
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            this.text = peer.getText();
            this.selectionStart = peer.getSelectionStart();
            this.selectionEnd = peer.getSelectionEnd();
        }
        s.defaultWriteObject();
        AWTEventMulticaster.save(s, "textL", this.textListener);
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        Object keyOrNull;
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
        this.text = this.text != null ? this.text : "";
        this.select(this.selectionStart, this.selectionEnd);
        while (null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();
            if ("textL" == key) {
                this.addTextListener((TextListener)s.readObject());
                continue;
            }
            s.readObject();
        }
        this.enableInputMethodsIfNecessary();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleAWTTextComponent();
        }
        return this.accessibleContext;
    }

    protected class AccessibleAWTTextComponent
    extends Component.AccessibleAWTComponent
    implements AccessibleText,
    TextListener {
        private static final long serialVersionUID = 3631432373506317811L;
        private static final boolean NEXT = true;
        private static final boolean PREVIOUS = false;

        public AccessibleAWTTextComponent() {
            TextComponent.this.addTextListener(this);
        }

        @Override
        public void textValueChanged(TextEvent textEvent) {
            Integer cpos = TextComponent.this.getCaretPosition();
            this.firePropertyChange("AccessibleText", null, cpos);
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (TextComponent.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }

        @Override
        public AccessibleText getAccessibleText() {
            return this;
        }

        @Override
        public int getIndexAtPoint(Point p) {
            return -1;
        }

        @Override
        public Rectangle getCharacterBounds(int i) {
            return null;
        }

        @Override
        public int getCharCount() {
            return TextComponent.this.getText().length();
        }

        @Override
        public int getCaretPosition() {
            return TextComponent.this.getCaretPosition();
        }

        @Override
        public AttributeSet getCharacterAttribute(int i) {
            return null;
        }

        @Override
        public int getSelectionStart() {
            return TextComponent.this.getSelectionStart();
        }

        @Override
        public int getSelectionEnd() {
            return TextComponent.this.getSelectionEnd();
        }

        @Override
        public String getSelectedText() {
            String selText = TextComponent.this.getSelectedText();
            if (selText == null || selText.isEmpty()) {
                return null;
            }
            return selText;
        }

        @Override
        public String getAtIndex(int part, int index) {
            if (index < 0 || index >= TextComponent.this.getText().length()) {
                return null;
            }
            switch (part) {
                case 1: {
                    return TextComponent.this.getText().substring(index, index + 1);
                }
                case 2: {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int end = words.following(index);
                    return s.substring(words.previous(), end);
                }
                case 3: {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int end = sentence.following(index);
                    return s.substring(sentence.previous(), end);
                }
            }
            return null;
        }

        private int findWordLimit(int index, BreakIterator words, boolean direction, String s) {
            int current;
            int last = direction ? words.following(index) : words.preceding(index);
            int n = current = direction ? words.next() : words.previous();
            while (current != -1) {
                for (int p = Math.min(last, current); p < Math.max(last, current); ++p) {
                    if (!Character.isLetter(s.charAt(p))) continue;
                    return last;
                }
                last = current;
                current = direction ? words.next() : words.previous();
            }
            return -1;
        }

        @Override
        public String getAfterIndex(int part, int index) {
            if (index < 0 || index >= TextComponent.this.getText().length()) {
                return null;
            }
            switch (part) {
                case 1: {
                    if (index + 1 >= TextComponent.this.getText().length()) {
                        return null;
                    }
                    return TextComponent.this.getText().substring(index + 1, index + 2);
                }
                case 2: {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int start = this.findWordLimit(index, words, true, s);
                    if (start == -1 || start >= s.length()) {
                        return null;
                    }
                    int end = words.following(start);
                    if (end == -1 || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                }
                case 3: {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int start = sentence.following(index);
                    if (start == -1 || start >= s.length()) {
                        return null;
                    }
                    int end = sentence.following(start);
                    if (end == -1 || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            }
            return null;
        }

        @Override
        public String getBeforeIndex(int part, int index) {
            if (index < 0 || index > TextComponent.this.getText().length() - 1) {
                return null;
            }
            switch (part) {
                case 1: {
                    if (index == 0) {
                        return null;
                    }
                    return TextComponent.this.getText().substring(index - 1, index);
                }
                case 2: {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int end = this.findWordLimit(index, words, false, s);
                    if (end == -1) {
                        return null;
                    }
                    int start = words.preceding(end);
                    if (start == -1) {
                        return null;
                    }
                    return s.substring(start, end);
                }
                case 3: {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int end = sentence.following(index);
                    end = sentence.previous();
                    int start = sentence.previous();
                    if (start == -1) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            }
            return null;
        }
    }
}

