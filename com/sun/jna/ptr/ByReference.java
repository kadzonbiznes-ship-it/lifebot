/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.ptr;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import java.lang.reflect.Method;

public abstract class ByReference
extends PointerType {
    protected ByReference(int dataSize) {
        this.setPointer(new Memory(dataSize));
    }

    @Override
    public String toString() {
        try {
            Method getValue = this.getClass().getMethod("getValue", new Class[0]);
            Object value = getValue.invoke((Object)this, new Object[0]);
            if (value == null) {
                return String.format("null@0x%x", Pointer.nativeValue(this.getPointer()));
            }
            return String.format("%s@0x%x=%s", value.getClass().getSimpleName(), Pointer.nativeValue(this.getPointer()), value);
        }
        catch (Exception ex) {
            return String.format("ByReference Contract violated - %s#getValue raised exception: %s", this.getClass().getName(), ex.getMessage());
        }
    }
}

