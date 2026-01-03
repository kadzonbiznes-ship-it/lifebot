/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.keyboard;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import java.util.EventListener;

public interface NativeKeyListener
extends EventListener {
    default public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
    }

    default public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
    }

    default public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
    }
}

