/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.mouse;

import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import java.util.EventListener;

public interface NativeMouseWheelListener
extends EventListener {
    default public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeEvent) {
    }
}

