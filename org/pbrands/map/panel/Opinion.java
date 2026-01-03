/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.map.panel;

import lombok.Generated;

public enum Opinion {
    ALL(0),
    LIKE(1),
    DISLIKE(2);

    private final int id;

    private Opinion(int id) {
        this.id = id;
    }

    public static Opinion getOpinion(int id) {
        for (Opinion opinion : Opinion.values()) {
            if (opinion.id != id) continue;
            return opinion;
        }
        return null;
    }

    @Generated
    public int getId() {
        return this.id;
    }
}

