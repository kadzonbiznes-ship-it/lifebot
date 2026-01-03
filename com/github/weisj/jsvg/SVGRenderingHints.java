/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg;

import java.awt.RenderingHints;
import org.jetbrains.annotations.Nullable;

public final class SVGRenderingHints {
    private static final int P_KEY_IMAGE_ANTIALIASING = 1;
    public static final RenderingHints.Key KEY_IMAGE_ANTIALIASING = new Key(1);
    public static final Object VALUE_IMAGE_ANTIALIASING_ON = Value.ON;
    public static final Object VALUE_IMAGE_ANTIALIASING_OFF = Value.OFF;

    private SVGRenderingHints() {
    }

    private static final class Key
    extends RenderingHints.Key {
        private Key(int privateKey) {
            super(privateKey);
        }

        @Override
        public boolean isCompatibleValue(@Nullable Object val) {
            return val instanceof Value;
        }
    }

    private static enum Value {
        ON,
        OFF;

    }
}

