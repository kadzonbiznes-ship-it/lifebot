/*
 * Decompiled with CFR 0.152.
 */
package imgui.binding;

import imgui.ImGui;

public abstract class ImGuiStruct {
    public long ptr;

    public ImGuiStruct(long ptr) {
        ImGui.init();
        this.ptr = ptr;
    }

    public final boolean isValidPtr() {
        return this.ptr != 0L;
    }

    public final boolean isNotValidPtr() {
        return !this.isValidPtr();
    }
}

