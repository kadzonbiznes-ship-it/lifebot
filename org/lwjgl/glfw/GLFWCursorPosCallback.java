/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.glfw;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.system.Callback;

public abstract class GLFWCursorPosCallback
extends Callback
implements GLFWCursorPosCallbackI {
    public static GLFWCursorPosCallback create(long functionPointer) {
        GLFWCursorPosCallbackI instance = (GLFWCursorPosCallbackI)Callback.get(functionPointer);
        return instance instanceof GLFWCursorPosCallback ? (GLFWCursorPosCallback)instance : new Container(functionPointer, instance);
    }

    public static @Nullable GLFWCursorPosCallback createSafe(long functionPointer) {
        return functionPointer == 0L ? null : GLFWCursorPosCallback.create(functionPointer);
    }

    public static GLFWCursorPosCallback create(GLFWCursorPosCallbackI instance) {
        return instance instanceof GLFWCursorPosCallback ? (GLFWCursorPosCallback)instance : new Container(instance.address(), instance);
    }

    protected GLFWCursorPosCallback() {
        super(CIF);
    }

    GLFWCursorPosCallback(long functionPointer) {
        super(functionPointer);
    }

    public GLFWCursorPosCallback set(long window) {
        GLFW.glfwSetCursorPosCallback(window, this);
        return this;
    }

    private static final class Container
    extends GLFWCursorPosCallback {
        private final GLFWCursorPosCallbackI delegate;

        Container(long functionPointer, GLFWCursorPosCallbackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(long window, double xpos, double ypos) {
            this.delegate.invoke(window, xpos, ypos);
        }
    }
}

