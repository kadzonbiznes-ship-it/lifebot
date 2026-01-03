/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.Serializable;

public class EventObject
implements Serializable {
    private static final long serialVersionUID = 5516075349620653480L;
    protected transient Object source;

    public EventObject(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("null source");
        }
        this.source = source;
    }

    public Object getSource() {
        return this.source;
    }

    public String toString() {
        return this.getClass().getName() + "[source=" + this.source + "]";
    }
}

