/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.mouse;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import java.util.EventListener;

public interface NativeMouseMotionListener
extends EventListener {
    default public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
    }

    default public void nativeMouseDragged(NativeMouseEvent nativeEvent) {
    }
}

