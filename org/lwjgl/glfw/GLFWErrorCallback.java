/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.glfw;

import java.io.PrintStream;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

public abstract class GLFWErrorCallback
extends Callback
implements GLFWErrorCallbackI {
    public static GLFWErrorCallback create(long functionPointer) {
        GLFWErrorCallbackI instance = (GLFWErrorCallbackI)Callback.get(functionPointer);
        return instance instanceof GLFWErrorCallback ? (GLFWErrorCallback)instance : new Container(functionPointer, instance);
    }

    public static @Nullable GLFWErrorCallback createSafe(long functionPointer) {
        return functionPointer == 0L ? null : GLFWErrorCallback.create(functionPointer);
    }

    public static GLFWErrorCallback create(GLFWErrorCallbackI instance) {
        return instance instanceof GLFWErrorCallback ? (GLFWErrorCallback)instance : new Container(instance.address(), instance);
    }

    protected GLFWErrorCallback() {
        super(CIF);
    }

    GLFWErrorCallback(long functionPointer) {
        super(functionPointer);
    }

    public static String getDescription(long description) {
        return MemoryUtil.memUTF8(description);
    }

    public static GLFWErrorCallback createPrint() {
        return GLFWErrorCallback.createPrint(APIUtil.DEBUG_STREAM);
    }

    public static GLFWErrorCallback createPrint(final PrintStream stream) {
        return new GLFWErrorCallback(){
            private Map<Integer, String> ERROR_CODES = APIUtil.apiClassTokens((field, value) -> 65536 < value && value < 131072, null, GLFW.class);

            @Override
            public void invoke(int error, long description) {
                String msg = 1.getDescription(description);
                StringBuilder sb = new StringBuilder(512);
                sb.append("[LWJGL] ").append(this.ERROR_CODES.get(error)).append(" error\n").append("\tDescription : ").append(msg).append("\n").append("\tStacktrace  :\n");
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 4; i < stack.length; ++i) {
                    sb.append("\t\t");
                    sb.append(stack[i]);
                    sb.append("\n");
                }
                stream.print(sb);
            }
        };
    }

    public static GLFWErrorCallback createThrow() {
        return new GLFWErrorCallback(){

            @Override
            public void invoke(int error, long description) {
                throw new IllegalStateException(String.format("GLFW error [0x%X]: %s", error, 2.getDescription(description)));
            }
        };
    }

    public GLFWErrorCallback set() {
        GLFW.glfwSetErrorCallback(this);
        return this;
    }

    private static final class Container
    extends GLFWErrorCallback {
        private final GLFWErrorCallbackI delegate;

        Container(long functionPointer, GLFWErrorCallbackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(int error, long description) {
            this.delegate.invoke(error, description);
        }
    }
}

