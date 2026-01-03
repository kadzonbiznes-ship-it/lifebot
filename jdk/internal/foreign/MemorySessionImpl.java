/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.foreign;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner;
import java.util.Objects;
import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.foreign.ConfinedSession;
import jdk.internal.foreign.GlobalSession;
import jdk.internal.foreign.ImplicitSession;
import jdk.internal.foreign.NativeMemorySegmentImpl;
import jdk.internal.foreign.SharedSession;
import jdk.internal.foreign.Utils;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.vm.annotation.ForceInline;

public abstract sealed class MemorySessionImpl
implements MemorySegment.Scope
permits ConfinedSession, GlobalSession, SharedSession {
    static final int OPEN = 0;
    static final int CLOSING = -1;
    static final int CLOSED = -2;
    static final VarHandle STATE;
    static final int MAX_FORKS = Integer.MAX_VALUE;
    public static final MemorySessionImpl GLOBAL;
    static final ScopedMemoryAccess.ScopedAccessError ALREADY_CLOSED;
    static final ScopedMemoryAccess.ScopedAccessError WRONG_THREAD;
    final ResourceList resourceList;
    final Thread owner;
    int state = 0;

    public Arena asArena() {
        return new /* Unavailable Anonymous Inner Class!! */;
    }

    @ForceInline
    public static final MemorySessionImpl toMemorySession(Arena arena) {
        return (MemorySessionImpl)arena.scope();
    }

    public final boolean isCloseableBy(Thread thread) {
        Objects.requireNonNull(thread);
        return this.isCloseable() && (this.owner == null || this.owner == thread);
    }

    public void addCloseAction(Runnable runnable) {
        Objects.requireNonNull(runnable);
        this.addInternal(ResourceList.ResourceCleanup.ofRunnable(runnable));
    }

    public void addOrCleanupIfFail(ResourceList.ResourceCleanup resource) {
        try {
            this.addInternal(resource);
        }
        catch (Throwable ex) {
            resource.cleanup();
            throw ex;
        }
    }

    void addInternal(ResourceList.ResourceCleanup resource) {
        this.checkValidState();
        this.resourceList.add(resource);
    }

    protected MemorySessionImpl(Thread owner, ResourceList resourceList) {
        this.owner = owner;
        this.resourceList = resourceList;
    }

    public static MemorySessionImpl createConfined(Thread thread) {
        return new ConfinedSession(thread);
    }

    public static MemorySessionImpl createShared() {
        return new SharedSession();
    }

    public static MemorySessionImpl createImplicit(Cleaner cleaner) {
        return new ImplicitSession(cleaner);
    }

    public MemorySegment allocate(long byteSize, long byteAlignment) {
        Utils.checkAllocationSizeAndAlign(byteSize, byteAlignment);
        return NativeMemorySegmentImpl.makeNativeSegment((long)byteSize, (long)byteAlignment, (MemorySessionImpl)this);
    }

    public abstract void release0();

    public abstract void acquire0();

    public void whileAlive(Runnable action) {
        Objects.requireNonNull(action);
        this.acquire0();
        try {
            action.run();
        }
        finally {
            this.release0();
        }
    }

    public final Thread ownerThread() {
        return this.owner;
    }

    public final boolean isAccessibleBy(Thread thread) {
        Objects.requireNonNull(thread);
        return this.owner == null || this.owner == thread;
    }

    @Override
    public boolean isAlive() {
        return this.state >= 0;
    }

    @ForceInline
    public void checkValidStateRaw() {
        if (this.owner != null && this.owner != Thread.currentThread()) {
            throw WRONG_THREAD;
        }
        if (this.state < 0) {
            throw ALREADY_CLOSED;
        }
    }

    public void checkValidState() {
        try {
            this.checkValidStateRaw();
        }
        catch (ScopedMemoryAccess.ScopedAccessError error) {
            throw error.newRuntimeException();
        }
    }

    public static final void checkValidState(MemorySegment segment) {
        ((AbstractMemorySegmentImpl)segment).sessionImpl().checkValidState();
    }

    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public boolean isCloseable() {
        return true;
    }

    public void close() {
        this.justClose();
        this.resourceList.cleanup();
    }

    abstract void justClose();

    public static MemorySessionImpl heapSession(Object ref) {
        return new GlobalSession(ref);
    }

    static IllegalStateException tooManyAcquires() {
        return new IllegalStateException("Session acquire limit exceeded");
    }

    static IllegalStateException alreadyAcquired(int acquires) {
        return new IllegalStateException(String.format("Session is acquired by %d clients", acquires));
    }

    static IllegalStateException alreadyClosed() {
        return new IllegalStateException("Already closed");
    }

    static WrongThreadException wrongThread() {
        return new WrongThreadException("Attempted access outside owning thread");
    }

    static UnsupportedOperationException nonCloseable() {
        return new UnsupportedOperationException("Attempted to close a non-closeable session");
    }

    static {
        GLOBAL = new GlobalSession(null);
        ALREADY_CLOSED = new ScopedMemoryAccess.ScopedAccessError(MemorySessionImpl::alreadyClosed);
        WRONG_THREAD = new ScopedMemoryAccess.ScopedAccessError(MemorySessionImpl::wrongThread);
        try {
            STATE = MethodHandles.lookup().findVarHandle(MemorySessionImpl.class, "state", Integer.TYPE);
        }
        catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static abstract class ResourceList
    implements Runnable {
        ResourceCleanup fst;

        abstract void add(ResourceCleanup var1);

        abstract void cleanup();

        @Override
        public final void run() {
            this.cleanup();
        }

        static void cleanup(ResourceCleanup first) {
            RuntimeException pendingException = null;
            ResourceCleanup current = first;
            while (current != null) {
                block5: {
                    try {
                        current.cleanup();
                    }
                    catch (RuntimeException ex) {
                        if (pendingException == null) {
                            pendingException = ex;
                        }
                        if (ex == pendingException) break block5;
                        pendingException.addSuppressed(ex);
                    }
                }
                current = current.next;
            }
            if (pendingException != null) {
                throw pendingException;
            }
        }

        public static abstract class ResourceCleanup {
            ResourceCleanup next;
            static final ResourceCleanup CLOSED_LIST = new ResourceCleanup(){

                @Override
                public void cleanup() {
                    throw new IllegalStateException("This resource list has already been closed!");
                }
            };

            public abstract void cleanup();

            static ResourceCleanup ofRunnable(final Runnable cleanupAction) {
                return new ResourceCleanup(){

                    @Override
                    public void cleanup() {
                        cleanupAction.run();
                    }
                };
            }
        }
    }
}

