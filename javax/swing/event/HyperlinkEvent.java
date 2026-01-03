/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.awt.event.InputEvent;
import java.net.URL;
import java.util.EventObject;
import javax.swing.text.Element;

public class HyperlinkEvent
extends EventObject {
    private EventType type;
    private URL u;
    private String desc;
    private Element sourceElement;
    private InputEvent inputEvent;

    public HyperlinkEvent(Object source, EventType type, URL u) {
        this(source, type, u, null);
    }

    public HyperlinkEvent(Object source, EventType type, URL u, String desc) {
        this(source, type, u, desc, null);
    }

    public HyperlinkEvent(Object source, EventType type, URL u, String desc, Element sourceElement) {
        super(source);
        this.type = type;
        this.u = u;
        this.desc = desc;
        this.sourceElement = sourceElement;
    }

    public HyperlinkEvent(Object source, EventType type, URL u, String desc, Element sourceElement, InputEvent inputEvent) {
        super(source);
        this.type = type;
        this.u = u;
        this.desc = desc;
        this.sourceElement = sourceElement;
        this.inputEvent = inputEvent;
    }

    public EventType getEventType() {
        return this.type;
    }

    public String getDescription() {
        return this.desc;
    }

    public URL getURL() {
        return this.u;
    }

    public Element getSourceElement() {
        return this.sourceElement;
    }

    public InputEvent getInputEvent() {
        return this.inputEvent;
    }

    public static final class EventType {
        public static final EventType ENTERED = new EventType("ENTERED");
        public static final EventType EXITED = new EventType("EXITED");
        public static final EventType ACTIVATED = new EventType("ACTIVATED");
        private String typeString;

        private EventType(String s) {
            this.typeString = s;
        }

        public String toString() {
            return this.typeString;
        }
    }
}

