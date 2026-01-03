/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.im;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.lang.ref.WeakReference;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import sun.awt.im.CompositionArea;
import sun.awt.im.InputMethodContext;

class CompositionAreaHandler
implements InputMethodListener,
InputMethodRequests {
    private static CompositionArea compositionArea;
    private static Object compositionAreaLock;
    private static CompositionAreaHandler compositionAreaOwner;
    private AttributedCharacterIterator composedText;
    private TextHitInfo caret = null;
    private WeakReference<Component> clientComponent = new WeakReference<Object>(null);
    private InputMethodContext inputMethodContext;
    private static final AttributedCharacterIterator.Attribute[] IM_ATTRIBUTES;
    private static final AttributedCharacterIterator EMPTY_TEXT;

    CompositionAreaHandler(InputMethodContext context) {
        this.inputMethodContext = context;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createCompositionArea() {
        Object object = compositionAreaLock;
        synchronized (object) {
            InputMethodRequests req;
            Component client;
            compositionArea = new CompositionArea();
            if (compositionAreaOwner != null) {
                compositionArea.setHandlerInfo(compositionAreaOwner, this.inputMethodContext);
            }
            if ((client = (Component)this.clientComponent.get()) != null && (req = client.getInputMethodRequests()) != null && this.inputMethodContext.useBelowTheSpotInput()) {
                this.setCompositionAreaUndecorated(true);
            }
        }
    }

    void setClientComponent(Component clientComponent) {
        this.clientComponent = new WeakReference<Component>(clientComponent);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void grabCompositionArea(boolean doUpdate) {
        Object object = compositionAreaLock;
        synchronized (object) {
            if (compositionAreaOwner != this) {
                compositionAreaOwner = this;
                if (compositionArea != null) {
                    compositionArea.setHandlerInfo(this, this.inputMethodContext);
                }
                if (doUpdate) {
                    if (this.composedText != null && compositionArea == null) {
                        this.createCompositionArea();
                    }
                    if (compositionArea != null) {
                        compositionArea.setText(this.composedText, this.caret);
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void releaseCompositionArea() {
        Object object = compositionAreaLock;
        synchronized (object) {
            if (compositionAreaOwner == this) {
                compositionAreaOwner = null;
                if (compositionArea != null) {
                    compositionArea.setHandlerInfo(null, null);
                    compositionArea.setText(null, null);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void closeCompositionArea() {
        if (compositionArea != null) {
            Object object = compositionAreaLock;
            synchronized (object) {
                compositionAreaOwner = null;
                compositionArea.setHandlerInfo(null, null);
                compositionArea.setText(null, null);
            }
        }
    }

    boolean isCompositionAreaVisible() {
        if (compositionArea != null) {
            return compositionArea.isCompositionAreaVisible();
        }
        return false;
    }

    void setCompositionAreaVisible(boolean visible) {
        if (compositionArea != null) {
            compositionArea.setCompositionAreaVisible(visible);
        }
    }

    void processInputMethodEvent(InputMethodEvent event) {
        if (event.getID() == 1100) {
            this.inputMethodTextChanged(event);
        } else {
            this.caretPositionChanged(event);
        }
    }

    void setCompositionAreaUndecorated(boolean undecorated) {
        if (compositionArea != null) {
            compositionArea.setCompositionAreaUndecorated(undecorated);
        }
    }

    @Override
    public void inputMethodTextChanged(InputMethodEvent event) {
        AttributedCharacterIterator text = event.getText();
        int committedCharacterCount = event.getCommittedCharacterCount();
        this.composedText = null;
        this.caret = null;
        if (text != null && committedCharacterCount < text.getEndIndex() - text.getBeginIndex()) {
            if (compositionArea == null) {
                this.createCompositionArea();
            }
            AttributedString composedTextString = new AttributedString(text, text.getBeginIndex() + committedCharacterCount, text.getEndIndex(), IM_ATTRIBUTES);
            composedTextString.addAttribute(TextAttribute.FONT, compositionArea.getFont());
            this.composedText = composedTextString.getIterator();
            this.caret = event.getCaret();
        }
        if (compositionArea != null) {
            compositionArea.setText(this.composedText, this.caret);
        }
        if (committedCharacterCount > 0) {
            this.inputMethodContext.dispatchCommittedText((Component)event.getSource(), text, committedCharacterCount);
            if (this.isCompositionAreaVisible()) {
                compositionArea.updateWindowLocation();
            }
        }
        event.consume();
    }

    @Override
    public void caretPositionChanged(InputMethodEvent event) {
        if (compositionArea != null) {
            compositionArea.setCaret(event.getCaret());
        }
        event.consume();
    }

    InputMethodRequests getClientInputMethodRequests() {
        Component client = (Component)this.clientComponent.get();
        if (client != null) {
            return client.getInputMethodRequests();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Rectangle getTextLocation(TextHitInfo offset) {
        Object object = compositionAreaLock;
        synchronized (object) {
            if (compositionAreaOwner == this && this.isCompositionAreaVisible()) {
                return compositionArea.getTextLocation(offset);
            }
            if (this.composedText != null) {
                return new Rectangle(0, 0, 0, 10);
            }
            InputMethodRequests requests = this.getClientInputMethodRequests();
            if (requests != null) {
                return requests.getTextLocation(offset);
            }
            return new Rectangle(0, 0, 0, 10);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public TextHitInfo getLocationOffset(int x, int y) {
        Object object = compositionAreaLock;
        synchronized (object) {
            if (compositionAreaOwner == this && this.isCompositionAreaVisible()) {
                return compositionArea.getLocationOffset(x, y);
            }
            return null;
        }
    }

    @Override
    public int getInsertPositionOffset() {
        InputMethodRequests req = this.getClientInputMethodRequests();
        if (req != null) {
            return req.getInsertPositionOffset();
        }
        return 0;
    }

    @Override
    public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
        InputMethodRequests req = this.getClientInputMethodRequests();
        if (req != null) {
            return req.getCommittedText(beginIndex, endIndex, attributes);
        }
        return EMPTY_TEXT;
    }

    @Override
    public int getCommittedTextLength() {
        InputMethodRequests req = this.getClientInputMethodRequests();
        if (req != null) {
            return req.getCommittedTextLength();
        }
        return 0;
    }

    @Override
    public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
        InputMethodRequests req = this.getClientInputMethodRequests();
        if (req != null) {
            return req.cancelLatestCommittedText(attributes);
        }
        return null;
    }

    @Override
    public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
        InputMethodRequests req = this.getClientInputMethodRequests();
        if (req != null) {
            return req.getSelectedText(attributes);
        }
        return EMPTY_TEXT;
    }

    static {
        compositionAreaLock = new Object();
        IM_ATTRIBUTES = new AttributedCharacterIterator.Attribute[]{TextAttribute.INPUT_METHOD_HIGHLIGHT};
        EMPTY_TEXT = new AttributedString("").getIterator();
    }
}

