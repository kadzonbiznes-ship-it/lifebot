/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.glfw;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallbackI;
import org.lwjgl.system.Callback;

public abstract class GLFWMonitorCallback
extends Callback
implements GLFWMonitorCallbackI {
    public static GLFWMonitorCallback create(long functionPointer) {
        GLFWMonitorCallbackI instance = (GLFWMonitorCallbackI)Callback.get(functionPointer);
        return instance instanceof GLFWMonitorCallback ? (GLFWMonitorCallback)instance : new Container(functionPointer, instance);
    }

    public static @Nullable GLFWMonitorCallback createSafe(long functionPointer) {
        return functionPointer == 0L ? null : GLFWMonitorCallback.create(functionPointer);
    }

    public static GLFWMonitorCallback create(GLFWMonitorCallbackI instance) {
        return instance instanceof GLFWMonitorCallback ? (GLFWMonitorCallback)instance : new Container(instance.address(), instance);
    }

    protected GLFWMonitorCallback() {
        super(CIF);
    }

    GLFWMonitorCallback(long functionPointer) {
        super(functionPointer);
    }

    public GLFWMonitorCallback set() {
        GLFW.glfwSetMonitorCallback(this);
        return this;
    }

    private static final class Container
    extends GLFWMonitorCallback {
        private final GLFWMonitorCallbackI delegate;

        Container(long functionPointer, GLFWMonitorCallbackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(long monitor, int event) {
            this.delegate.invoke(monitor, event);
        }
    }
}

