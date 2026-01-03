/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.mouse;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import java.util.EventListener;

public interface NativeMouseListener
extends EventListener {
    default public void nativeMouseClicked(NativeMouseEvent nativeEvent) {
    }

    default public void nativeMousePressed(NativeMouseEvent nativeEvent) {
    }

    default public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
    }
}

