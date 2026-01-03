/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;

public class ActionEvent
extends AWTEvent {
    public static final int SHIFT_MASK = 1;
    public static final int CTRL_MASK = 2;
    public static final int META_MASK = 4;
    public static final int ALT_MASK = 8;
    public static final int ACTION_FIRST = 1001;
    public static final int ACTION_LAST = 1001;
    public static final int ACTION_PERFORMED = 1001;
    String actionCommand;
    long when;
    int modifiers;
    private static final long serialVersionUID = -7671078796273832149L;

    public ActionEvent(Object source, int id, String command) {
        this(source, id, command, 0);
    }

    public ActionEvent(Object source, int id, String command, int modifiers) {
        this(source, id, command, 0L, modifiers);
    }

    public ActionEvent(Object source, int id, String command, long when, int modifiers) {
        super(source, id);
        this.actionCommand = command;
        this.when = when;
        this.modifiers = modifiers;
    }

    public String getActionCommand() {
        return this.actionCommand;
    }

    public long getWhen() {
        return this.when;
    }

    public int getModifiers() {
        return this.modifiers;
    }

    @Override
    public String paramString() {
        return (switch (this.id) {
            case 1001 -> "ACTION_PERFORMED";
            default -> "unknown type";
        }) + ",cmd=" + this.actionCommand + ",when=" + this.when + ",modifiers=" + KeyEvent.getKeyModifiersText(this.modifiers);
    }
}

