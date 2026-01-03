/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.font.TextHitInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

public class InputMethodEvent
extends AWTEvent {
    private static final long serialVersionUID = 4727190874778922661L;
    public static final int INPUT_METHOD_FIRST = 1100;
    public static final int INPUT_METHOD_TEXT_CHANGED = 1100;
    public static final int CARET_POSITION_CHANGED = 1101;
    public static final int INPUT_METHOD_LAST = 1101;
    long when;
    private transient AttributedCharacterIterator text;
    private transient int committedCharacterCount;
    private transient TextHitInfo caret;
    private transient TextHitInfo visiblePosition;

    public InputMethodEvent(Component source, int id, long when, AttributedCharacterIterator text, int committedCharacterCount, TextHitInfo caret, TextHitInfo visiblePosition) {
        super(source, id);
        if (id < 1100 || id > 1101) {
            throw new IllegalArgumentException("id outside of valid range");
        }
        if (id == 1101 && text != null) {
            throw new IllegalArgumentException("text must be null for CARET_POSITION_CHANGED");
        }
        this.when = when;
        this.text = text;
        int textLength = 0;
        if (text != null) {
            textLength = text.getEndIndex() - text.getBeginIndex();
        }
        if (committedCharacterCount < 0 || committedCharacterCount > textLength) {
            throw new IllegalArgumentException("committedCharacterCount outside of valid range");
        }
        this.committedCharacterCount = committedCharacterCount;
        this.caret = caret;
        this.visiblePosition = visiblePosition;
    }

    public InputMethodEvent(Component source, int id, AttributedCharacterIterator text, int committedCharacterCount, TextHitInfo caret, TextHitInfo visiblePosition) {
        this(source, id, InputMethodEvent.getMostRecentEventTimeForSource(source), text, committedCharacterCount, caret, visiblePosition);
    }

    public InputMethodEvent(Component source, int id, TextHitInfo caret, TextHitInfo visiblePosition) {
        this(source, id, InputMethodEvent.getMostRecentEventTimeForSource(source), null, 0, caret, visiblePosition);
    }

    public AttributedCharacterIterator getText() {
        return this.text;
    }

    public int getCommittedCharacterCount() {
        return this.committedCharacterCount;
    }

    public TextHitInfo getCaret() {
        return this.caret;
    }

    public TextHitInfo getVisiblePosition() {
        return this.visiblePosition;
    }

    @Override
    public void consume() {
        this.consumed = true;
    }

    @Override
    public boolean isConsumed() {
        return this.consumed;
    }

    public long getWhen() {
        return this.when;
    }

    @Override
    public String paramString() {
        String textString;
        String typeStr = switch (this.id) {
            case 1100 -> "INPUT_METHOD_TEXT_CHANGED";
            case 1101 -> "CARET_POSITION_CHANGED";
            default -> "unknown type";
        };
        if (this.text == null) {
            textString = "no text";
        } else {
            StringBuilder textBuffer = new StringBuilder("\"");
            int committedCharacterCount = this.committedCharacterCount;
            char c = this.text.first();
            while (committedCharacterCount-- > 0) {
                textBuffer.append(c);
                c = this.text.next();
            }
            textBuffer.append("\" + \"");
            while (c != '\uffff') {
                textBuffer.append(c);
                c = this.text.next();
            }
            textBuffer.append("\"");
            textString = textBuffer.toString();
        }
        String countString = this.committedCharacterCount + " characters committed";
        Object caretString = this.caret == null ? "no caret" : "caret: " + this.caret.toString();
        Object visiblePositionString = this.visiblePosition == null ? "no visible position" : "visible position: " + this.visiblePosition.toString();
        return typeStr + ", " + textString + ", " + countString + ", " + (String)caretString + ", " + (String)visiblePositionString;
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        if (this.when == 0L) {
            this.when = EventQueue.getMostRecentEventTime();
        }
    }

    private static long getMostRecentEventTimeForSource(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("null source");
        }
        AppContext appContext = SunToolkit.targetToAppContext(source);
        EventQueue eventQueue = SunToolkit.getSystemEventQueueImplPP(appContext);
        return AWTAccessor.getEventQueueAccessor().getMostRecentEventTime(eventQueue);
    }
}

