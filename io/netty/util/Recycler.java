/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.Recycler$BlockingMessageQueue
 *  org.jetbrains.annotations.VisibleForTesting
 */
package io.netty.util;

import io.netty.util.Recycler;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.ObjectPool;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.jetbrains.annotations.VisibleForTesting;

public abstract class Recycler<T> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Recycler.class);
    private static final EnhancedHandle<?> NOOP_HANDLE = new EnhancedHandle<Object>(){

        @Override
        public void recycle(Object object) {
        }

        @Override
        public void unguardedRecycle(Object object) {
        }

        public String toString() {
            return "NOOP_HANDLE";
        }
    };
    private static final int DEFAULT_INITIAL_MAX_CAPACITY_PER_THREAD = 4096;
    private static final int DEFAULT_MAX_CAPACITY_PER_THREAD;
    private static final int RATIO;
    private static final int DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD;
    private static final boolean BLOCKING_POOL;
    private static final boolean BATCH_FAST_TL_ONLY;
    private final int maxCapacityPerThread;
    private final int interval;
    private final int chunkSize;
    private final FastThreadLocal<LocalPool<T>> threadLocal = new FastThreadLocal<LocalPool<T>>(){

        @Override
        protected LocalPool<T> initialValue() {
            return new LocalPool(Recycler.this.maxCapacityPerThread, Recycler.this.interval, Recycler.this.chunkSize);
        }

        @Override
        protected void onRemoval(LocalPool<T> value) throws Exception {
            super.onRemoval(value);
            MessagePassingQueue handles = value.pooledHandles;
            value.pooledHandles = null;
            value.owner = null;
            handles.clear();
        }
    };

    protected Recycler() {
        this(DEFAULT_MAX_CAPACITY_PER_THREAD);
    }

    protected Recycler(int maxCapacityPerThread) {
        this(maxCapacityPerThread, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    @Deprecated
    protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor) {
        this(maxCapacityPerThread, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    @Deprecated
    protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor, int ratio, int maxDelayedQueuesPerThread) {
        this(maxCapacityPerThread, ratio, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    @Deprecated
    protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor, int ratio, int maxDelayedQueuesPerThread, int delayedQueueRatio) {
        this(maxCapacityPerThread, ratio, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    protected Recycler(int maxCapacityPerThread, int ratio, int chunkSize) {
        this.interval = Math.max(0, ratio);
        if (maxCapacityPerThread <= 0) {
            this.maxCapacityPerThread = 0;
            this.chunkSize = 0;
        } else {
            this.maxCapacityPerThread = Math.max(4, maxCapacityPerThread);
            this.chunkSize = Math.max(2, Math.min(chunkSize, this.maxCapacityPerThread >> 1));
        }
    }

    public final T get() {
        Object obj;
        if (this.maxCapacityPerThread == 0) {
            return (T)this.newObject(NOOP_HANDLE);
        }
        LocalPool<T> localPool = this.threadLocal.get();
        DefaultHandle<T> handle = localPool.claim();
        if (handle == null) {
            handle = localPool.newHandle();
            if (handle != null) {
                obj = this.newObject(handle);
                handle.set(obj);
            } else {
                obj = this.newObject(NOOP_HANDLE);
            }
        } else {
            obj = handle.get();
        }
        return obj;
    }

    @Deprecated
    public final boolean recycle(T o, Handle<T> handle) {
        if (handle == NOOP_HANDLE) {
            return false;
        }
        handle.recycle(o);
        return true;
    }

    @VisibleForTesting
    final int threadLocalSize() {
        LocalPool<T> localPool = this.threadLocal.getIfExists();
        return localPool == null ? 0 : ((LocalPool)localPool).pooledHandles.size() + ((LocalPool)localPool).batch.size();
    }

    protected abstract T newObject(Handle<T> var1);

    static {
        int maxCapacityPerThread = SystemPropertyUtil.getInt("io.netty.recycler.maxCapacityPerThread", SystemPropertyUtil.getInt("io.netty.recycler.maxCapacity", 4096));
        if (maxCapacityPerThread < 0) {
            maxCapacityPerThread = 4096;
        }
        DEFAULT_MAX_CAPACITY_PER_THREAD = maxCapacityPerThread;
        DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD = SystemPropertyUtil.getInt("io.netty.recycler.chunkSize", 32);
        RATIO = Math.max(0, SystemPropertyUtil.getInt("io.netty.recycler.ratio", 8));
        BLOCKING_POOL = SystemPropertyUtil.getBoolean("io.netty.recycler.blocking", false);
        BATCH_FAST_TL_ONLY = SystemPropertyUtil.getBoolean("io.netty.recycler.batchFastThreadLocalOnly", true);
        if (logger.isDebugEnabled()) {
            if (DEFAULT_MAX_CAPACITY_PER_THREAD == 0) {
                logger.debug("-Dio.netty.recycler.maxCapacityPerThread: disabled");
                logger.debug("-Dio.netty.recycler.ratio: disabled");
                logger.debug("-Dio.netty.recycler.chunkSize: disabled");
                logger.debug("-Dio.netty.recycler.blocking: disabled");
                logger.debug("-Dio.netty.recycler.batchFastThreadLocalOnly: disabled");
            } else {
                logger.debug("-Dio.netty.recycler.maxCapacityPerThread: {}", (Object)DEFAULT_MAX_CAPACITY_PER_THREAD);
                logger.debug("-Dio.netty.recycler.ratio: {}", (Object)RATIO);
                logger.debug("-Dio.netty.recycler.chunkSize: {}", (Object)DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
                logger.debug("-Dio.netty.recycler.blocking: {}", (Object)BLOCKING_POOL);
                logger.debug("-Dio.netty.recycler.batchFastThreadLocalOnly: {}", (Object)BATCH_FAST_TL_ONLY);
            }
        }
    }

    private static final class LocalPool<T>
    implements MessagePassingQueue.Consumer<DefaultHandle<T>> {
        private final int ratioInterval;
        private final int chunkSize;
        private final ArrayDeque<DefaultHandle<T>> batch;
        private volatile Thread owner;
        private volatile MessagePassingQueue<DefaultHandle<T>> pooledHandles;
        private int ratioCounter;

        LocalPool(int maxCapacity, int ratioInterval, int chunkSize) {
            this.ratioInterval = ratioInterval;
            this.chunkSize = chunkSize;
            this.batch = new ArrayDeque(chunkSize);
            Thread currentThread = Thread.currentThread();
            this.owner = !BATCH_FAST_TL_ONLY || currentThread instanceof FastThreadLocalThread ? currentThread : null;
            this.pooledHandles = BLOCKING_POOL ? new BlockingMessageQueue(maxCapacity) : (MessagePassingQueue)((Object)PlatformDependent.newMpscQueue(chunkSize, maxCapacity));
            this.ratioCounter = ratioInterval;
        }

        DefaultHandle<T> claim() {
            DefaultHandle<T> handle;
            MessagePassingQueue<DefaultHandle<T>> handles = this.pooledHandles;
            if (handles == null) {
                return null;
            }
            if (this.batch.isEmpty()) {
                handles.drain(this, this.chunkSize);
            }
            if (null != (handle = this.batch.pollLast())) {
                handle.toClaimed();
            }
            return handle;
        }

        void release(DefaultHandle<T> handle, boolean guarded) {
            if (guarded) {
                handle.toAvailable();
            } else {
                handle.unguardedToAvailable();
            }
            Thread owner = this.owner;
            if (owner != null && Thread.currentThread() == owner && this.batch.size() < this.chunkSize) {
                this.accept(handle);
            } else if (owner != null && LocalPool.isTerminated(owner)) {
                this.owner = null;
                this.pooledHandles = null;
            } else {
                MessagePassingQueue<DefaultHandle<DefaultHandle<T>>> handles = this.pooledHandles;
                if (handles != null) {
                    handles.relaxedOffer(handle);
                }
            }
        }

        private static boolean isTerminated(Thread owner) {
            return PlatformDependent.isJ9Jvm() ? !owner.isAlive() : owner.getState() == Thread.State.TERMINATED;
        }

        DefaultHandle<T> newHandle() {
            if (++this.ratioCounter >= this.ratioInterval) {
                this.ratioCounter = 0;
                return new DefaultHandle(this);
            }
            return null;
        }

        @Override
        public void accept(DefaultHandle<T> e) {
            this.batch.addLast(e);
        }
    }

    private static final class DefaultHandle<T>
    extends EnhancedHandle<T> {
        private static final int STATE_CLAIMED = 0;
        private static final int STATE_AVAILABLE = 1;
        private static final AtomicIntegerFieldUpdater<DefaultHandle<?>> STATE_UPDATER;
        private volatile int state;
        private final LocalPool<T> localPool;
        private T value;

        DefaultHandle(LocalPool<T> localPool) {
            this.localPool = localPool;
        }

        @Override
        public void recycle(Object object) {
            if (object != this.value) {
                throw new IllegalArgumentException("object does not belong to handle");
            }
            this.localPool.release(this, true);
        }

        @Override
        public void unguardedRecycle(Object object) {
            if (object != this.value) {
                throw new IllegalArgumentException("object does not belong to handle");
            }
            this.localPool.release(this, false);
        }

        T get() {
            return this.value;
        }

        void set(T value) {
            this.value = value;
        }

        void toClaimed() {
            assert (this.state == 1);
            STATE_UPDATER.lazySet(this, 0);
        }

        void toAvailable() {
            int prev = STATE_UPDATER.getAndSet(this, 1);
            if (prev == 1) {
                throw new IllegalStateException("Object has been recycled already.");
            }
        }

        void unguardedToAvailable() {
            int prev = this.state;
            if (prev == 1) {
                throw new IllegalStateException("Object has been recycled already.");
            }
            STATE_UPDATER.lazySet(this, 1);
        }

        static {
            AtomicIntegerFieldUpdater<DefaultHandle> updater = AtomicIntegerFieldUpdater.newUpdater(DefaultHandle.class, "state");
            STATE_UPDATER = updater;
        }
    }

    public static abstract class EnhancedHandle<T>
    implements Handle<T> {
        public abstract void unguardedRecycle(Object var1);

        private EnhancedHandle() {
        }
    }

    public static interface Handle<T>
    extends ObjectPool.Handle<T> {
    }
}

