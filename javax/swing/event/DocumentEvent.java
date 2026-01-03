/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import javax.swing.text.Document;
import javax.swing.text.Element;

public interface DocumentEvent {
    public int getOffset();

    public int getLength();

    public Document getDocument();

    public EventType getType();

    public ElementChange getChange(Element var1);

    public static interface ElementChange {
        public Element getElement();

        public int getIndex();

        public Element[] getChildrenRemoved();

        public Element[] getChildrenAdded();
    }

    public static final class EventType {
        public static final EventType INSERT = new EventType("INSERT");
        public static final EventType REMOVE = new EventType("REMOVE");
        public static final EventType CHANGE = new EventType("CHANGE");
        private String typeString;

        private EventType(String s) {
            this.typeString = s;
        }

        public String toString() {
            return this.typeString;
        }
    }
}

