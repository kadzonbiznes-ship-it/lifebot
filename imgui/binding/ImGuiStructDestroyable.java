/*
 * Decompiled with CFR 0.152.
 */
package imgui.binding;

import imgui.binding.ImGuiStruct;

public abstract class ImGuiStructDestroyable
extends ImGuiStruct {
    public ImGuiStructDestroyable() {
        this(0L);
        this.ptr = this.create();
    }

    public ImGuiStructDestroyable(long ptr) {
        super(ptr);
    }

    protected abstract long create();

    public void destroy() {
        this.nDestroy(this.ptr);
    }

    private native void nDestroy(long var1);
}

