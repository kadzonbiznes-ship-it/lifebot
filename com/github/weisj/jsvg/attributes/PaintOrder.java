/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.attributes.AttributeParser;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.parser.SeparatorMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PaintOrder {
    public static final PaintOrder NORMAL = new PaintOrder(Phase.FILL, Phase.STROKE, Phase.MARKERS);
    @NotNull
    private final Phase[] phases;

    public PaintOrder(Phase ... phases) {
        this.phases = phases;
    }

    @NotNull
    public Phase[] phases() {
        return this.phases;
    }

    @NotNull
    public static PaintOrder parse(@NotNull AttributeNode attributeNode) {
        @Nullable String value = attributeNode.getValue("paint-order");
        @NotNull AttributeParser parser = attributeNode.parser();
        if (value == null || "normal".equals(value)) {
            return NORMAL;
        }
        String[] rawPhases = parser.parseStringList(value, SeparatorMode.COMMA_AND_WHITESPACE);
        Phase[] phases = new Phase[3];
        int length = Math.min(phases.length, rawPhases.length);
        int i = 0;
        while (i < length) {
            phases[i] = parser.parseEnum(rawPhases[i], Phase.class);
            if (phases[i] == null) continue;
            ++i;
        }
        while (i < 3) {
            phases[i] = PaintOrder.findNextInNormalOrder(phases, i);
            ++i;
        }
        return new PaintOrder(phases);
    }

    @NotNull
    private static Phase findNextInNormalOrder(@NotNull Phase[] phases, int maxIndex) {
        for (Phase phase : NORMAL.phases()) {
            boolean found = false;
            for (int i = 0; i < maxIndex; ++i) {
                if (phases[i] != phase) continue;
                found = true;
                break;
            }
            if (found) continue;
            return phase;
        }
        throw new IllegalStateException();
    }

    public static enum Phase {
        FILL,
        STROKE,
        MARKERS;

    }
}

