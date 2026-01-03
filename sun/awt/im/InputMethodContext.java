/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.im;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.awt.im.spi.InputMethod;
import java.security.AccessController;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import javax.swing.JFrame;
import sun.awt.InputMethodSupport;
import sun.awt.im.CompositionAreaHandler;
import sun.awt.im.InputContext;
import sun.awt.im.InputMethodJFrame;
import sun.security.action.GetPropertyAction;

public class InputMethodContext
extends InputContext
implements java.awt.im.spi.InputMethodContext {
    private boolean dispatchingCommittedText;
    private CompositionAreaHandler compositionAreaHandler;
    private Object compositionAreaHandlerLock = new Object();
    private static boolean belowTheSpotInputRequested;
    private boolean inputMethodSupportsBelowTheSpot;

    void setInputMethodSupportsBelowTheSpot(boolean supported) {
        this.inputMethodSupportsBelowTheSpot = supported;
    }

    boolean useBelowTheSpotInput() {
        return belowTheSpotInputRequested && this.inputMethodSupportsBelowTheSpot;
    }

    private boolean haveActiveClient() {
        Component client = this.getClientComponent();
        return client != null && client.getInputMethodRequests() != null;
    }

    @Override
    public void dispatchInputMethodEvent(int id, AttributedCharacterIterator text, int committedCharacterCount, TextHitInfo caret, TextHitInfo visiblePosition) {
        Component source = this.getClientComponent();
        if (source != null) {
            InputMethodEvent event = new InputMethodEvent(source, id, text, committedCharacterCount, caret, visiblePosition);
            if (this.haveActiveClient() && !this.useBelowTheSpotInput()) {
                source.dispatchEvent(event);
            } else {
                this.getCompositionAreaHandler(true).processInputMethodEvent(event);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    synchronized void dispatchCommittedText(Component client, AttributedCharacterIterator text, int committedCharacterCount) {
        if (committedCharacterCount == 0 || text.getEndIndex() <= text.getBeginIndex()) {
            return;
        }
        long time = System.currentTimeMillis();
        this.dispatchingCommittedText = true;
        try {
            InputMethodRequests req = client.getInputMethodRequests();
            if (req != null) {
                int beginIndex = text.getBeginIndex();
                AttributedCharacterIterator toBeCommitted = new AttributedString(text, beginIndex, beginIndex + committedCharacterCount).getIterator();
                InputMethodEvent inputEvent = new InputMethodEvent(client, 1100, toBeCommitted, committedCharacterCount, null, null);
                client.dispatchEvent(inputEvent);
            } else {
                char keyChar = text.first();
                while (committedCharacterCount-- > 0 && keyChar != '\uffff') {
                    KeyEvent keyEvent = new KeyEvent(client, 400, time, 0, 0, keyChar);
                    client.dispatchEvent(keyEvent);
                    keyChar = text.next();
                }
            }
        }
        finally {
            this.dispatchingCommittedText = false;
        }
    }

    @Override
    public void dispatchEvent(AWTEvent event) {
        if (event instanceof InputMethodEvent) {
            if (((Component)event.getSource()).getInputMethodRequests() == null || this.useBelowTheSpotInput() && !this.dispatchingCommittedText) {
                this.getCompositionAreaHandler(true).processInputMethodEvent((InputMethodEvent)event);
            }
        } else if (!this.dispatchingCommittedText) {
            super.dispatchEvent(event);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CompositionAreaHandler getCompositionAreaHandler(boolean grab) {
        Object object = this.compositionAreaHandlerLock;
        synchronized (object) {
            if (this.compositionAreaHandler == null) {
                this.compositionAreaHandler = new CompositionAreaHandler(this);
            }
            this.compositionAreaHandler.setClientComponent(this.getClientComponent());
            if (grab) {
                this.compositionAreaHandler.grabCompositionArea(false);
            }
            return this.compositionAreaHandler;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void grabCompositionArea(boolean doUpdate) {
        Object object = this.compositionAreaHandlerLock;
        synchronized (object) {
            if (this.compositionAreaHandler != null) {
                this.compositionAreaHandler.grabCompositionArea(doUpdate);
            } else {
                CompositionAreaHandler.closeCompositionArea();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void releaseCompositionArea() {
        Object object = this.compositionAreaHandlerLock;
        synchronized (object) {
            if (this.compositionAreaHandler != null) {
                this.compositionAreaHandler.releaseCompositionArea();
            }
        }
    }

    boolean isCompositionAreaVisible() {
        if (this.compositionAreaHandler != null) {
            return this.compositionAreaHandler.isCompositionAreaVisible();
        }
        return false;
    }

    void setCompositionAreaVisible(boolean visible) {
        if (this.compositionAreaHandler != null) {
            this.compositionAreaHandler.setCompositionAreaVisible(visible);
        }
    }

    @Override
    public Rectangle getTextLocation(TextHitInfo offset) {
        return this.getReq().getTextLocation(offset);
    }

    @Override
    public TextHitInfo getLocationOffset(int x, int y) {
        return this.getReq().getLocationOffset(x, y);
    }

    @Override
    public int getInsertPositionOffset() {
        return this.getReq().getInsertPositionOffset();
    }

    @Override
    public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
        return this.getReq().getCommittedText(beginIndex, endIndex, attributes);
    }

    @Override
    public int getCommittedTextLength() {
        return this.getReq().getCommittedTextLength();
    }

    @Override
    public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
        return this.getReq().cancelLatestCommittedText(attributes);
    }

    @Override
    public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
        return this.getReq().getSelectedText(attributes);
    }

    private InputMethodRequests getReq() {
        if (this.haveActiveClient() && !this.useBelowTheSpotInput()) {
            return this.getClientComponent().getInputMethodRequests();
        }
        return this.getCompositionAreaHandler(false);
    }

    @Override
    public Window createInputMethodWindow(String title, boolean attachToInputContext) {
        InputMethodContext context = attachToInputContext ? this : null;
        return InputMethodContext.createInputMethodWindow(title, context, false);
    }

    @Override
    public JFrame createInputMethodJFrame(String title, boolean attachToInputContext) {
        InputMethodContext context = attachToInputContext ? this : null;
        return (JFrame)InputMethodContext.createInputMethodWindow(title, context, true);
    }

    static Window createInputMethodWindow(String title, InputContext context, boolean isSwing) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        if (isSwing) {
            return new InputMethodJFrame(title, context);
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof InputMethodSupport) {
            return ((InputMethodSupport)((Object)toolkit)).createInputMethodWindow(title, context);
        }
        throw new InternalError("Input methods must be supported");
    }

    @Override
    public void enableClientWindowNotification(InputMethod inputMethod, boolean enable) {
        super.enableClientWindowNotification(inputMethod, enable);
    }

    void setCompositionAreaUndecorated(boolean undecorated) {
        if (this.compositionAreaHandler != null) {
            this.compositionAreaHandler.setCompositionAreaUndecorated(undecorated);
        }
    }

    static {
        String inputStyle = AccessController.doPrivileged(new GetPropertyAction("java.awt.im.style", null));
        if (inputStyle == null) {
            inputStyle = Toolkit.getProperty("java.awt.im.style", null);
        }
        belowTheSpotInputRequested = "below-the-spot".equals(inputStyle);
    }
}

