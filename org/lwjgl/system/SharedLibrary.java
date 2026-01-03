/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.system;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.Pointer;

public interface SharedLibrary
extends FunctionProvider,
NativeResource,
Pointer {
    public String getName();

    public @Nullable String getPath();

    public static abstract class Delegate
    implements SharedLibrary {
        protected final SharedLibrary library;

        protected Delegate(SharedLibrary library) {
            this.library = library;
        }

        @Override
        public String getName() {
            return this.library.getName();
        }

        @Override
        public @Nullable String getPath() {
            return this.library.getPath();
        }

        @Override
        public long address() {
            return this.library.address();
        }

        @Override
        public void free() {
            this.library.free();
        }
    }

    public static abstract class Default
    extends Pointer.Default
    implements SharedLibrary {
        private final String name;

        protected Default(String name, long handle) {
            super(handle);
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}

