/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.map.panel;

import lombok.Generated;

public enum Status {
    ALL(0),
    COLLECTED(1),
    NOT_COLLECTED(2);

    private final int id;

    private Status(int id) {
        this.id = id;
    }

    public static Status getStatus(int id) {
        for (Status status : Status.values()) {
            if (status.id != id) continue;
            return status;
        }
        return null;
    }

    @Generated
    public int getId() {
        return this.id;
    }
}

