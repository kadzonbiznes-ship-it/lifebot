/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.system;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryAccessJNI;
import org.lwjgl.system.Platform;

public interface Pointer {
    public static final int POINTER_SIZE = MemoryAccessJNI.getPointerSize();
    public static final int POINTER_SHIFT = POINTER_SIZE == 8 ? 3 : 2;
    public static final int CLONG_SIZE = POINTER_SIZE == 8 && Platform.get() == Platform.WINDOWS ? 4 : POINTER_SIZE;
    public static final int CLONG_SHIFT = CLONG_SIZE == 8 ? 3 : 2;
    public static final boolean BITS32 = POINTER_SIZE * 8 == 32;
    public static final boolean BITS64 = POINTER_SIZE * 8 == 64;

    public long address();

    public static abstract class Default
    implements Pointer {
        protected long address;

        protected Default(long address) {
            if (Checks.CHECKS && address == 0L) {
                throw new NullPointerException();
            }
            this.address = address;
        }

        @Override
        public long address() {
            return this.address;
        }

        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pointer)) {
                return false;
            }
            Pointer that = (Pointer)o;
            return this.address == that.address();
        }

        public int hashCode() {
            return (int)(this.address ^ this.address >>> 32);
        }

        public String toString() {
            return String.format("%s pointer [0x%X]", this.getClass().getSimpleName(), this.address);
        }
    }
}

