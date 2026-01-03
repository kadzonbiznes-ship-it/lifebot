/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  imgui.ImGuiInputTextCallbackData
 */
package imgui.callback;

import imgui.ImGuiInputTextCallbackData;
import java.util.function.Consumer;

public abstract class ImGuiInputTextCallback
implements Consumer<ImGuiInputTextCallbackData> {
    @Override
    public final void accept(long ptr) {
        this.accept(new ImGuiInputTextCallbackData(ptr));
    }
}

