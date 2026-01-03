/*
 * Decompiled with CFR 0.152.
 */
package imgui.assertion;

public abstract class ImAssertCallback {
    public void imAssert(String assertion, int line, String file) {
        try {
            this.imAssertCallback(assertion, line, file);
        }
        catch (Exception ex) {
            System.err.println("WARNING: Exception thrown in Dear ImGui Assertion Callback!");
            System.err.println("Dear ImGui Assertion Failed: " + assertion);
            System.err.println("Assertion Located At: " + file + ":" + line);
            ex.printStackTrace();
        }
        System.exit(1);
    }

    public abstract void imAssertCallback(String var1, int var2, String var3);
}

