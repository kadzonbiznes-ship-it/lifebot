/*
 * Decompiled with CFR 0.152.
 */
package imgui.internal;

import imgui.binding.ImGuiStruct;
import imgui.internal.ImGui;

public class ImGuiContext
extends ImGuiStruct {
    public ImGuiContext(long ptr) {
        super(ptr);
        ImGui.init();
    }
}

