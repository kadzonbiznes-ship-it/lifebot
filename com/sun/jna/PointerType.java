/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Klass;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;

public abstract class PointerType
implements NativeMapped {
    private Pointer pointer;

    protected PointerType() {
        this.pointer = Pointer.NULL;
    }

    protected PointerType(Pointer p) {
        this.pointer = p;
    }

    @Override
    public Class<?> nativeType() {
        return Pointer.class;
    }

    @Override
    public Object toNative() {
        return this.getPointer();
    }

    public Pointer getPointer() {
        return this.pointer;
    }

    public void setPointer(Pointer p) {
        this.pointer = p;
    }

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        if (nativeValue == null) {
            return null;
        }
        PointerType pt = (PointerType)Klass.newInstance(this.getClass());
        pt.pointer = (Pointer)nativeValue;
        return pt;
    }

    public int hashCode() {
        return this.pointer != null ? this.pointer.hashCode() : 0;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof PointerType) {
            Pointer p = ((PointerType)o).getPointer();
            if (this.pointer == null) {
                return p == null;
            }
            return this.pointer.equals(p);
        }
        return false;
    }

    public String toString() {
        return this.pointer == null ? "NULL" : this.pointer.toString() + " (" + super.toString() + ")";
    }
}

