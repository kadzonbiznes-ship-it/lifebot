/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFuture<T>
implements Future<T>,
CompletionStage<T> {
    volatile Object result;
    volatile Completion stack;
    static final AltResult NIL = new AltResult(null);
    private static final boolean USE_COMMON_POOL = ForkJoinPool.getCommonPoolParallelism() > 1;
    private static final Executor ASYNC_POOL = USE_COMMON_POOL ? ForkJoinPool.commonPool() : new ThreadPerTaskExecutor();
    static final int SYNC = 0;
    static final int ASYNC = 1;
    static final int NESTED = -1;
    private static final VarHandle RESULT;
    private static final VarHandle STACK;
    private static final VarHandle NEXT;

    final boolean internalComplete(Object r) {
        return RESULT.compareAndSet(this, null, r);
    }

    final boolean tryPushStack(Completion c) {
        Completion h = this.stack;
        NEXT.set(c, h);
        return STACK.compareAndSet(this, h, c);
    }

    final void pushStack(Completion c) {
        while (!this.tryPushStack(c)) {
        }
    }

    final boolean completeNull() {
        return RESULT.compareAndSet(this, null, NIL);
    }

    final Object encodeValue(T t) {
        return t == null ? NIL : t;
    }

    final boolean completeValue(T t) {
        return RESULT.compareAndSet(this, null, t == null ? NIL : t);
    }

    static AltResult encodeThrowable(Throwable x) {
        return new AltResult(x instanceof CompletionException ? x : new CompletionException(x));
    }

    final boolean completeThrowable(Throwable x) {
        return RESULT.compareAndSet(this, null, CompletableFuture.encodeThrowable(x));
    }

    static Object encodeThrowable(Throwable x, Object r) {
        if (!(x instanceof CompletionException)) {
            x = new CompletionException(x);
        } else if (r instanceof AltResult && x == ((AltResult)r).ex) {
            return r;
        }
        return new AltResult(x);
    }

    final boolean completeThrowable(Throwable x, Object r) {
        return RESULT.compareAndSet(this, null, CompletableFuture.encodeThrowable(x, r));
    }

    Object encodeOutcome(T t, Throwable x) {
        return x == null ? (t == null ? NIL : t) : CompletableFuture.encodeThrowable(x);
    }

    static Object encodeRelay(Object r) {
        Throwable x;
        if (r instanceof AltResult && (x = ((AltResult)r).ex) != null && !(x instanceof CompletionException)) {
            r = new AltResult(new CompletionException(x));
        }
        return r;
    }

    final boolean completeRelay(Object r) {
        return RESULT.compareAndSet(this, null, CompletableFuture.encodeRelay(r));
    }

    private static Object reportGet(Object r) throws InterruptedException, ExecutionException {
        if (r == null) {
            throw new InterruptedException();
        }
        if (r instanceof AltResult) {
            Throwable cause;
            Throwable x = ((AltResult)r).ex;
            if (x == null) {
                return null;
            }
            if (x instanceof CancellationException) {
                throw (CancellationException)x;
            }
            if (x instanceof CompletionException && (cause = x.getCause()) != null) {
                x = cause;
            }
            throw new ExecutionException(x);
        }
        return r;
    }

    private static Object reportJoin(Object r) {
        if (r instanceof AltResult) {
            Throwable x = ((AltResult)r).ex;
            if (x == null) {
                return null;
            }
            if (x instanceof CancellationException) {
                throw (CancellationException)x;
            }
            if (x instanceof CompletionException) {
                throw (CompletionException)x;
            }
            throw new CompletionException(x);
        }
        return r;
    }

    static Executor screenExecutor(Executor e) {
        if (!USE_COMMON_POOL && e == ForkJoinPool.commonPool()) {
            return ASYNC_POOL;
        }
        if (e == null) {
            throw new NullPointerException();
        }
        return e;
    }

    final void postComplete() {
        CompletableFuture<?> f = this;
        while (true) {
            CompletableFuture<?> d;
            Completion t;
            Completion h;
            if ((h = f.stack) == null) {
                if (f == this) break;
                f = this;
                h = f.stack;
                if (h == null) break;
            }
            if (!STACK.compareAndSet(f, h, t = h.next)) continue;
            if (t != null) {
                if (f != this) {
                    this.pushStack(h);
                    continue;
                }
                NEXT.compareAndSet(h, t, null);
            }
            f = (d = h.tryFire(-1)) == null ? this : d;
        }
    }

    final void cleanStack() {
        Completion p = this.stack;
        boolean unlinked = false;
        while (true) {
            if (p == null) {
                return;
            }
            if (p.isLive()) {
                if (!unlinked) break;
                return;
            }
            if (STACK.weakCompareAndSet(this, p, p = p.next)) {
                unlinked = true;
                continue;
            }
            p = this.stack;
        }
        Completion q = p.next;
        while (q != null) {
            Completion s = q.next;
            if (q.isLive()) {
                p = q;
                q = s;
                continue;
            }
            if (NEXT.weakCompareAndSet(p, q, s)) break;
            q = p.next;
        }
    }

    final void unipush(Completion c) {
        if (c != null) {
            while (!this.tryPushStack(c)) {
                if (this.result == null) continue;
                NEXT.set(c, null);
                break;
            }
            if (this.result != null) {
                c.tryFire(0);
            }
        }
    }

    final CompletableFuture<T> postFire(CompletableFuture<?> a, int mode) {
        if (a != null && a.stack != null) {
            Object r = a.result;
            if (r == null) {
                a.cleanStack();
            }
            if (mode >= 0 && (r != null || a.result != null)) {
                a.postComplete();
            }
        }
        if (this.result != null && this.stack != null) {
            if (mode < 0) {
                return this;
            }
            this.postComplete();
        }
        return null;
    }

    private <V> CompletableFuture<V> uniApplyStage(Executor e, Function<? super T, ? extends V> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        Object r = this.result;
        if (r != null) {
            return this.uniApplyNow(r, e, f);
        }
        CompletableFuture d = this.newIncompleteFuture();
        this.unipush(new UniApply<T, V>(e, d, this, f));
        return d;
    }

    private <V> CompletableFuture<V> uniApplyNow(Object r, Executor e, Function<? super T, ? extends V> f) {
        CompletableFuture d = this.newIncompleteFuture();
        if (r instanceof AltResult) {
            Throwable x = ((AltResult)r).ex;
            if (x != null) {
                d.result = CompletableFuture.encodeThrowable(x, r);
                return d;
            }
            r = null;
        }
        try {
            if (e != null) {
                e.execute(new UniApply<T, V>(null, d, this, f));
            } else {
                Object t = r;
                d.result = d.encodeValue(f.apply(t));
            }
        }
        catch (Throwable ex) {
            d.result = CompletableFuture.encodeThrowable(ex);
        }
        return d;
    }

    private CompletableFuture<Void> uniAcceptStage(Executor e, Consumer<? super T> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        Object r = this.result;
        if (r != null) {
            return this.uniAcceptNow(r, e, f);
        }
        CompletableFuture<Void> d = this.newIncompleteFuture();
        this.unipush(new UniAccept<T>(e, d, this, f));
        return d;
    }

    private CompletableFuture<Void> uniAcceptNow(Object r, Executor e, Consumer<? super T> f) {
        CompletableFuture<Void> d = this.newIncompleteFuture();
        if (r instanceof AltResult) {
            Throwable x = ((AltResult)r).ex;
            if (x != null) {
                d.result = CompletableFuture.encodeThrowable(x, r);
                return d;
            }
            r = null;
        }
        try {
            if (e != null) {
                e.execute(new UniAccept<T>(null, d, this, f));
            } else {
                Object t = r;
                f.accept(t);
                d.result = NIL;
            }
        }
        catch (Throwable ex) {
            d.result = CompletableFuture.encodeThrowable(ex);
        }
        return d;
    }

    private CompletableFuture<Void> uniRunStage(Executor e, Runnable f) {
        if (f == null) {
            throw new NullPointerException();
        }
        Object r = this.result;
        if (r != null) {
            return this.uniRunNow(r, e, f);
        }
        CompletableFuture<Void> d = this.newIncompleteFuture();
        this.unipush(new UniRun(e, d, this, f));
        return d;
    }

    private CompletableFuture<Void> uniRunNow(Object r, Executor e, Runnable f) {
        Throwable x;
        CompletableFuture<Void> d = this.newIncompleteFuture();
        if (r instanceof AltResult && (x = ((AltResult)r).ex) != null) {
            d.result = CompletableFuture.encodeThrowable(x, r);
        } else {
            try {
                if (e != null) {
                    e.execute(new UniRun(null, d, this, f));
                } else {
                    f.run();
                    d.result = NIL;
                }
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    final boolean uniWhenComplete(Object r, BiConsumer<? super T, ? super Throwable> f, UniWhenComplete<T> c) {
        Throwable x = null;
        if (this.result == null) {
            block8: {
                try {
                    Object t;
                    if (c != null && !c.claim()) {
                        return false;
                    }
                    if (r instanceof AltResult) {
                        x = ((AltResult)r).ex;
                        t = null;
                    } else {
                        Object tr;
                        t = tr = r;
                    }
                    f.accept(t, x);
                    if (x == null) {
                        this.internalComplete(r);
                        return true;
                    }
                }
                catch (Throwable ex) {
                    if (x == null) {
                        x = ex;
                    }
                    if (x == ex) break block8;
                    x.addSuppressed(ex);
                }
            }
            this.completeThrowable(x, r);
        }
        return true;
    }

    private CompletableFuture<T> uniWhenCompleteStage(Executor e, BiConsumer<? super T, ? super Throwable> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null) {
            this.unipush(new UniWhenComplete<T>(e, d, this, f));
        } else if (e == null) {
            d.uniWhenComplete(r, f, null);
        } else {
            try {
                e.execute(new UniWhenComplete<T>(null, d, this, f));
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    final <S> boolean uniHandle(Object r, BiFunction<? super S, Throwable, ? extends T> f, UniHandle<S, T> c) {
        if (this.result == null) {
            try {
                Object s;
                Throwable x;
                if (c != null && !c.claim()) {
                    return false;
                }
                if (r instanceof AltResult) {
                    x = ((AltResult)r).ex;
                    s = null;
                } else {
                    Object ss;
                    x = null;
                    s = ss = r;
                }
                this.completeValue(f.apply(s, x));
            }
            catch (Throwable ex) {
                this.completeThrowable(ex);
            }
        }
        return true;
    }

    private <V> CompletableFuture<V> uniHandleStage(Executor e, BiFunction<? super T, Throwable, ? extends V> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null) {
            this.unipush(new UniHandle<T, V>(e, d, this, f));
        } else if (e == null) {
            d.uniHandle(r, f, null);
        } else {
            try {
                e.execute(new UniHandle<T, V>(null, d, this, f));
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    final boolean uniExceptionally(Object r, Function<? super Throwable, ? extends T> f, UniExceptionally<T> c) {
        if (this.result == null) {
            try {
                Throwable x;
                if (c != null && !c.claim()) {
                    return false;
                }
                if (r instanceof AltResult && (x = ((AltResult)r).ex) != null) {
                    this.completeValue(f.apply(x));
                } else {
                    this.internalComplete(r);
                }
            }
            catch (Throwable ex) {
                this.completeThrowable(ex);
            }
        }
        return true;
    }

    private CompletableFuture<T> uniExceptionallyStage(Executor e, Function<Throwable, ? extends T> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null) {
            this.unipush(new UniExceptionally<T>(e, d, this, f));
        } else if (e == null) {
            d.uniExceptionally(r, f, null);
        } else {
            try {
                e.execute(new UniExceptionally<T>(null, d, this, f));
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    private CompletableFuture<T> uniComposeExceptionallyStage(Executor e, Function<Throwable, ? extends CompletionStage<T>> f) {
        Throwable x;
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null) {
            this.unipush(new UniComposeExceptionally(e, d, this, f));
        } else if (!(r instanceof AltResult) || (x = ((AltResult)r).ex) == null) {
            d.internalComplete(r);
        } else {
            try {
                if (e != null) {
                    e.execute(new UniComposeExceptionally(null, d, this, f));
                } else {
                    CompletableFuture<T> g = f.apply(x).toCompletableFuture();
                    Object s = g.result;
                    if (s != null) {
                        d.result = CompletableFuture.encodeRelay(s);
                    } else {
                        g.unipush(new UniRelay(d, g));
                    }
                }
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    private static <U, T extends U> CompletableFuture<U> uniCopyStage(CompletableFuture<T> src) {
        CompletableFuture<U> d = src.newIncompleteFuture();
        Object r = src.result;
        if (r != null) {
            d.result = CompletableFuture.encodeRelay(r);
        } else {
            src.unipush(new UniRelay<U, T>(d, src));
        }
        return d;
    }

    private MinimalStage<T> uniAsMinimalStage() {
        Object r = this.result;
        if (r != null) {
            return new MinimalStage(CompletableFuture.encodeRelay(r));
        }
        MinimalStage d = new MinimalStage();
        this.unipush(new UniRelay(d, this));
        return d;
    }

    private <V> CompletableFuture<V> uniComposeStage(Executor e, Function<? super T, ? extends CompletionStage<V>> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null) {
            this.unipush(new UniCompose(e, d, this, f));
        } else {
            if (r instanceof AltResult) {
                Throwable x = ((AltResult)r).ex;
                if (x != null) {
                    d.result = CompletableFuture.encodeThrowable(x, r);
                    return d;
                }
                r = null;
            }
            try {
                if (e != null) {
                    e.execute(new UniCompose(null, d, this, f));
                } else {
                    Object t = r;
                    CompletableFuture<V> g = f.apply(t).toCompletableFuture();
                    Object s = g.result;
                    if (s != null) {
                        d.result = CompletableFuture.encodeRelay(s);
                    } else {
                        g.unipush(new UniRelay(d, g));
                    }
                }
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    final void bipush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (this.result == null) {
                if (!this.tryPushStack(c)) continue;
                if (b.result == null) {
                    b.unipush(new CoCompletion(c));
                } else if (this.result != null) {
                    c.tryFire(0);
                }
                return;
            }
            b.unipush(c);
        }
    }

    final CompletableFuture<T> postFire(CompletableFuture<?> a, CompletableFuture<?> b, int mode) {
        if (b != null && b.stack != null) {
            Object r = b.result;
            if (r == null) {
                b.cleanStack();
            }
            if (mode >= 0 && (r != null || b.result != null)) {
                b.postComplete();
            }
        }
        return this.postFire(a, mode);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    final <R, S> boolean biApply(Object r, Object s, BiFunction<? super R, ? super S, ? extends T> f, BiApply<R, S, T> c) {
        Throwable x;
        if (this.result != null) return true;
        if (r instanceof AltResult) {
            x = ((AltResult)r).ex;
            if (x != null) {
                this.completeThrowable(x, r);
                return true;
            }
            r = null;
        }
        if (s instanceof AltResult) {
            x = ((AltResult)s).ex;
            if (x != null) {
                this.completeThrowable(x, s);
                return true;
            }
            s = null;
        }
        try {
            if (c != null && !c.claim()) {
                return false;
            }
            Object rr = r;
            Object ss = s;
            this.completeValue(f.apply(rr, ss));
            return true;
        }
        catch (Throwable ex) {
            this.completeThrowable(ex);
        }
        return true;
    }

    private <U, V> CompletableFuture<V> biApplyStage(Executor e, CompletionStage<U> o, BiFunction<? super T, ? super U, ? extends V> f) {
        Object s;
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null) {
            throw new NullPointerException();
        }
        CompletableFuture<U> d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null || (s = b.result) == null) {
            this.bipush(b, new BiApply<T, U, V>(e, d, this, b, f));
        } else if (e == null) {
            d.biApply(r, s, f, null);
        } else {
            try {
                e.execute(new BiApply<T, U, V>(null, d, this, b, f));
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    final <R, S> boolean biAccept(Object r, Object s, BiConsumer<? super R, ? super S> f, BiAccept<R, S> c) {
        Throwable x;
        if (this.result != null) return true;
        if (r instanceof AltResult) {
            x = ((AltResult)r).ex;
            if (x != null) {
                this.completeThrowable(x, r);
                return true;
            }
            r = null;
        }
        if (s instanceof AltResult) {
            x = ((AltResult)s).ex;
            if (x != null) {
                this.completeThrowable(x, s);
                return true;
            }
            s = null;
        }
        try {
            if (c != null && !c.claim()) {
                return false;
            }
            Object rr = r;
            Object ss = s;
            f.accept(rr, ss);
            this.completeNull();
            return true;
        }
        catch (Throwable ex) {
            this.completeThrowable(ex);
        }
        return true;
    }

    private <U> CompletableFuture<Void> biAcceptStage(Executor e, CompletionStage<U> o, BiConsumer<? super T, ? super U> f) {
        Object s;
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null || (s = b.result) == null) {
            this.bipush(b, new BiAccept<T, U>(e, d, this, b, f));
        } else if (e == null) {
            d.biAccept(r, s, f, null);
        } else {
            try {
                e.execute(new BiAccept<T, U>(null, d, this, b, f));
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    final boolean biRun(Object r, Object s, Runnable f, BiRun<?, ?> c) {
        block6: {
            Throwable x;
            Object z;
            block5: {
                block4: {
                    if (this.result != null) return true;
                    if (!(r instanceof AltResult)) break block4;
                    z = r;
                    x = ((AltResult)z).ex;
                    if (x != null) break block5;
                }
                if (!(s instanceof AltResult)) break block6;
                z = s;
                x = ((AltResult)z).ex;
                if (x == null) break block6;
            }
            this.completeThrowable(x, z);
            return true;
        }
        try {
            if (c != null && !c.claim()) {
                return false;
            }
            f.run();
            this.completeNull();
            return true;
        }
        catch (Throwable ex) {
            this.completeThrowable(ex);
        }
        return true;
    }

    private CompletableFuture<Void> biRunStage(Executor e, CompletionStage<?> o, Runnable f) {
        Object s;
        CompletableFuture<?> b;
        if (f == null || (b = o.toCompletableFuture()) == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = this.newIncompleteFuture();
        Object r = this.result;
        if (r == null || (s = b.result) == null) {
            this.bipush(b, new BiRun(e, d, this, b, f));
        } else if (e == null) {
            d.biRun(r, s, f, null);
        } else {
            try {
                e.execute(new BiRun(null, d, this, b, f));
            }
            catch (Throwable ex) {
                d.result = CompletableFuture.encodeThrowable(ex);
            }
        }
        return d;
    }

    /*
     * Enabled aggressive block sorting
     */
    static CompletableFuture<Void> andTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<Void> d;
        block8: {
            Throwable x;
            Object z;
            block7: {
                Object s;
                block6: {
                    CompletableFuture<Object> b;
                    d = new CompletableFuture<Void>();
                    if (lo > hi) {
                        d.result = NIL;
                        return d;
                    }
                    int mid = lo + hi >>> 1;
                    CompletableFuture<Object> a = lo == mid ? cfs[lo] : CompletableFuture.andTree(cfs, lo, mid);
                    if (a == null) throw new NullPointerException();
                    if ((lo == hi ? a : (b = hi == mid + 1 ? cfs[hi] : CompletableFuture.andTree(cfs, mid + 1, hi))) == null) {
                        throw new NullPointerException();
                    }
                    Object r = a.result;
                    if (r == null || (s = b.result) == null) {
                        a.bipush(b, new BiRelay(d, a, b));
                        return d;
                    }
                    if (!(r instanceof AltResult)) break block6;
                    z = r;
                    x = ((AltResult)z).ex;
                    if (x != null) break block7;
                }
                if (!(s instanceof AltResult)) break block8;
                z = s;
                x = ((AltResult)z).ex;
                if (x == null) break block8;
            }
            d.result = CompletableFuture.encodeThrowable(x, z);
            return d;
        }
        d.result = NIL;
        return d;
    }

    final void orpush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (!this.tryPushStack(c)) {
                if (this.result == null) continue;
                NEXT.set(c, null);
                break;
            }
            if (this.result != null) {
                c.tryFire(0);
            } else {
                b.unipush(new CoCompletion(c));
            }
        }
    }

    private <U extends T, V> CompletableFuture<V> orApplyStage(Executor e, CompletionStage<U> o, Function<? super T, ? extends V> f) {
        CompletableFuture<U> b;
        block5: {
            Object r;
            CompletableFuture<U> z;
            block4: {
                if (f == null || (b = o.toCompletableFuture()) == null) {
                    throw new NullPointerException();
                }
                z = this;
                r = z.result;
                if (r != null) break block4;
                z = b;
                r = z.result;
                if (r == null) break block5;
            }
            return z.uniApplyNow(r, e, f);
        }
        CompletableFuture<U> d = this.newIncompleteFuture();
        this.orpush(b, new OrApply<T, U, V>(e, d, this, b, f));
        return d;
    }

    private <U extends T> CompletableFuture<Void> orAcceptStage(Executor e, CompletionStage<U> o, Consumer<? super T> f) {
        CompletableFuture<U> b;
        block5: {
            Object r;
            CompletableFuture<U> z;
            block4: {
                if (f == null || (b = o.toCompletableFuture()) == null) {
                    throw new NullPointerException();
                }
                z = this;
                r = z.result;
                if (r != null) break block4;
                z = b;
                r = z.result;
                if (r == null) break block5;
            }
            return z.uniAcceptNow(r, e, f);
        }
        CompletableFuture<Void> d = this.newIncompleteFuture();
        this.orpush(b, new OrAccept<T, U>(e, d, this, b, f));
        return d;
    }

    private CompletableFuture<Void> orRunStage(Executor e, CompletionStage<?> o, Runnable f) {
        CompletableFuture<?> b;
        block5: {
            Object r;
            CompletableFuture<?> z;
            block4: {
                if (f == null || (b = o.toCompletableFuture()) == null) {
                    throw new NullPointerException();
                }
                z = this;
                r = z.result;
                if (r != null) break block4;
                z = b;
                r = z.result;
                if (r == null) break block5;
            }
            return z.uniRunNow(r, e, f);
        }
        CompletableFuture<Void> d = this.newIncompleteFuture();
        this.orpush(b, new OrRun(e, d, this, b, f));
        return d;
    }

    static <U> CompletableFuture<U> asyncSupplyStage(Executor e, Supplier<U> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture d = new CompletableFuture();
        e.execute(new AsyncSupply(d, f));
        return d;
    }

    static CompletableFuture<Void> asyncRunStage(Executor e, Runnable f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        e.execute(new AsyncRun(d, f));
        return d;
    }

    private Object waitingGet(boolean interruptible) {
        Object r;
        if (interruptible && Thread.interrupted()) {
            return null;
        }
        Signaller q = null;
        boolean queued = false;
        while ((r = this.result) == null) {
            if (q == null) {
                q = new Signaller(interruptible, 0L, 0L);
                if (!(Thread.currentThread() instanceof ForkJoinWorkerThread)) continue;
                ForkJoinPool.helpAsyncBlocker(this.defaultExecutor(), q);
                continue;
            }
            if (!queued) {
                queued = this.tryPushStack(q);
                continue;
            }
            if (interruptible && q.interrupted) {
                q.thread = null;
                this.cleanStack();
                return null;
            }
            try {
                ForkJoinPool.managedBlock(q);
            }
            catch (InterruptedException ie) {
                q.interrupted = true;
            }
        }
        if (q != null) {
            q.thread = null;
            if (q.interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        this.postComplete();
        return r;
    }

    private Object timedGet(long nanos) throws TimeoutException {
        long d = System.nanoTime() + nanos;
        long deadline = d == 0L ? 1L : d;
        boolean interrupted = false;
        boolean queued = false;
        Signaller q = null;
        Object r = null;
        while (!interrupted && !(interrupted = Thread.interrupted()) && (r = this.result) == null && nanos > 0L) {
            if (q == null) {
                q = new Signaller(true, nanos, deadline);
                if (!(Thread.currentThread() instanceof ForkJoinWorkerThread)) continue;
                ForkJoinPool.helpAsyncBlocker(this.defaultExecutor(), q);
                continue;
            }
            if (!queued) {
                queued = this.tryPushStack(q);
                continue;
            }
            try {
                ForkJoinPool.managedBlock(q);
                interrupted = q.interrupted;
                nanos = q.nanos;
            }
            catch (InterruptedException ie) {
                interrupted = true;
            }
        }
        if (q != null) {
            q.thread = null;
            if (r == null) {
                this.cleanStack();
            }
        }
        if (r != null) {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            this.postComplete();
            return r;
        }
        if (interrupted) {
            return null;
        }
        throw new TimeoutException();
    }

    public CompletableFuture() {
    }

    CompletableFuture(Object r) {
        RESULT.setRelease(this, r);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return CompletableFuture.asyncSupplyStage(ASYNC_POOL, supplier);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return CompletableFuture.asyncSupplyStage(CompletableFuture.screenExecutor(executor), supplier);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.asyncRunStage(ASYNC_POOL, runnable);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return CompletableFuture.asyncRunStage(CompletableFuture.screenExecutor(executor), runnable);
    }

    public static <U> CompletableFuture<U> completedFuture(U value) {
        return new CompletableFuture(value == null ? NIL : value);
    }

    @Override
    public boolean isDone() {
        return this.result != null;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        Object r = this.result;
        if (r == null) {
            r = this.waitingGet(true);
        }
        return (T)CompletableFuture.reportGet(r);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        Object r = this.result;
        if (r == null) {
            r = this.timedGet(nanos);
        }
        return (T)CompletableFuture.reportGet(r);
    }

    public T join() {
        Object r = this.result;
        if (r == null) {
            r = this.waitingGet(false);
        }
        return (T)CompletableFuture.reportJoin(r);
    }

    public T getNow(T valueIfAbsent) {
        Object r = this.result;
        return (T)(r == null ? valueIfAbsent : CompletableFuture.reportJoin(r));
    }

    @Override
    public T resultNow() {
        Object r = this.result;
        if (r != null) {
            if (r instanceof AltResult) {
                AltResult alt = (AltResult)r;
                if (alt.ex == null) {
                    return null;
                }
            } else {
                Object t = r;
                return (T)t;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public Throwable exceptionNow() {
        Object r = this.result;
        if (r instanceof AltResult) {
            AltResult alt = (AltResult)r;
            Throwable x = alt.ex;
            if (x != null && !(x instanceof CancellationException)) {
                Throwable cause;
                if (x instanceof CompletionException && (cause = x.getCause()) != null) {
                    x = cause;
                }
                return x;
            }
        }
        throw new IllegalStateException();
    }

    public boolean complete(T value) {
        boolean triggered = this.completeValue(value);
        this.postComplete();
        return triggered;
    }

    public boolean completeExceptionally(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        boolean triggered = this.internalComplete(new AltResult(ex));
        this.postComplete();
        return triggered;
    }

    @Override
    public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return this.uniApplyStage(null, fn);
    }

    @Override
    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return this.uniApplyStage(this.defaultExecutor(), fn);
    }

    @Override
    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return this.uniApplyStage(CompletableFuture.screenExecutor(executor), fn);
    }

    public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return this.uniAcceptStage(null, action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
        return this.uniAcceptStage(this.defaultExecutor(), action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return this.uniAcceptStage(CompletableFuture.screenExecutor(executor), action);
    }

    public CompletableFuture<Void> thenRun(Runnable action) {
        return this.uniRunStage(null, action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return this.uniRunStage(this.defaultExecutor(), action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return this.uniRunStage(CompletableFuture.screenExecutor(executor), action);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return this.biApplyStage(null, other, fn);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return this.biApplyStage(this.defaultExecutor(), other, fn);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return this.biApplyStage(CompletableFuture.screenExecutor(executor), other, fn);
    }

    public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return this.biAcceptStage(null, other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return this.biAcceptStage(this.defaultExecutor(), other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return this.biAcceptStage(CompletableFuture.screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return this.biRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return this.biRunStage(this.defaultExecutor(), other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return this.biRunStage(CompletableFuture.screenExecutor(executor), other, action);
    }

    @Override
    public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return this.orApplyStage(null, other, fn);
    }

    @Override
    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return this.orApplyStage(this.defaultExecutor(), other, fn);
    }

    @Override
    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return this.orApplyStage(CompletableFuture.screenExecutor(executor), other, fn);
    }

    public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return this.orAcceptStage(null, other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return this.orAcceptStage(this.defaultExecutor(), other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return this.orAcceptStage(CompletableFuture.screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return this.orRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return this.orRunStage(this.defaultExecutor(), other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return this.orRunStage(CompletableFuture.screenExecutor(executor), other, action);
    }

    @Override
    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return this.uniComposeStage(null, fn);
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return this.uniComposeStage(this.defaultExecutor(), fn);
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return this.uniComposeStage(CompletableFuture.screenExecutor(executor), fn);
    }

    @Override
    public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return this.uniWhenCompleteStage(null, action);
    }

    @Override
    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return this.uniWhenCompleteStage(this.defaultExecutor(), action);
    }

    @Override
    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return this.uniWhenCompleteStage(CompletableFuture.screenExecutor(executor), action);
    }

    @Override
    public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return this.uniHandleStage(null, fn);
    }

    @Override
    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return this.uniHandleStage(this.defaultExecutor(), fn);
    }

    @Override
    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return this.uniHandleStage(CompletableFuture.screenExecutor(executor), fn);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return this;
    }

    @Override
    public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return this.uniExceptionallyStage(null, fn);
    }

    @Override
    public CompletableFuture<T> exceptionallyAsync(Function<Throwable, ? extends T> fn) {
        return this.uniExceptionallyStage(this.defaultExecutor(), fn);
    }

    @Override
    public CompletableFuture<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor) {
        return this.uniExceptionallyStage(CompletableFuture.screenExecutor(executor), fn);
    }

    @Override
    public CompletableFuture<T> exceptionallyCompose(Function<Throwable, ? extends CompletionStage<T>> fn) {
        return this.uniComposeExceptionallyStage(null, fn);
    }

    @Override
    public CompletableFuture<T> exceptionallyComposeAsync(Function<Throwable, ? extends CompletionStage<T>> fn) {
        return this.uniComposeExceptionallyStage(this.defaultExecutor(), fn);
    }

    @Override
    public CompletableFuture<T> exceptionallyComposeAsync(Function<Throwable, ? extends CompletionStage<T>> fn, Executor executor) {
        return this.uniComposeExceptionallyStage(CompletableFuture.screenExecutor(executor), fn);
    }

    public static CompletableFuture<Void> allOf(CompletableFuture<?> ... cfs) {
        return CompletableFuture.andTree(cfs, 0, cfs.length - 1);
    }

    public static CompletableFuture<Object> anyOf(CompletableFuture<?> ... cfs) {
        int n = cfs.length;
        if (n <= 1) {
            return n == 0 ? new CompletableFuture() : CompletableFuture.uniCopyStage(cfs[0]);
        }
        for (CompletableFuture<?> cf : cfs) {
            Object r = cf.result;
            if (r == null) continue;
            return new CompletableFuture<Object>(CompletableFuture.encodeRelay(r));
        }
        cfs = (CompletableFuture[])cfs.clone();
        CompletableFuture<Object> d = new CompletableFuture<Object>();
        for (CompletableFuture<?> cf : cfs) {
            cf.unipush(new AnyOf(d, cf, cfs));
        }
        if (d.result != null) {
            int len = cfs.length;
            for (int i = 0; i < len; ++i) {
                if (cfs[i].result == null) continue;
                ++i;
                while (i < len) {
                    if (cfs[i].result == null) {
                        cfs[i].cleanStack();
                    }
                    ++i;
                }
            }
        }
        return d;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = this.result == null && this.internalComplete(new AltResult(new CancellationException()));
        this.postComplete();
        return cancelled || this.isCancelled();
    }

    @Override
    public boolean isCancelled() {
        Object r = this.result;
        return r instanceof AltResult && ((AltResult)r).ex instanceof CancellationException;
    }

    public boolean isCompletedExceptionally() {
        Object r = this.result;
        return r instanceof AltResult && r != NIL;
    }

    @Override
    public Future.State state() {
        Object r = this.result;
        if (r == null) {
            return Future.State.RUNNING;
        }
        if (r != NIL && r instanceof AltResult) {
            AltResult alt = (AltResult)r;
            if (alt.ex instanceof CancellationException) {
                return Future.State.CANCELLED;
            }
            return Future.State.FAILED;
        }
        return Future.State.SUCCESS;
    }

    public void obtrudeValue(T value) {
        this.result = value == null ? NIL : value;
        this.postComplete();
    }

    public void obtrudeException(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        this.result = new AltResult(ex);
        this.postComplete();
    }

    public int getNumberOfDependents() {
        int count = 0;
        Completion p = this.stack;
        while (p != null) {
            ++count;
            p = p.next;
        }
        return count;
    }

    public String toString() {
        Object r = this.result;
        int count = 0;
        Completion p = this.stack;
        while (p != null) {
            ++count;
            p = p.next;
        }
        return super.toString() + (r == null ? (count == 0 ? "[Not completed]" : "[Not completed, " + count + " dependents]") : (r instanceof AltResult && ((AltResult)r).ex != null ? "[Completed exceptionally: " + ((AltResult)r).ex + "]" : "[Completed normally]"));
    }

    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new CompletableFuture<T>();
    }

    public Executor defaultExecutor() {
        return ASYNC_POOL;
    }

    public CompletableFuture<T> copy() {
        return CompletableFuture.uniCopyStage(this);
    }

    public CompletionStage<T> minimalCompletionStage() {
        return this.uniAsMinimalStage();
    }

    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
        if (supplier == null || executor == null) {
            throw new NullPointerException();
        }
        executor.execute(new AsyncSupply<T>(this, supplier));
        return this;
    }

    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
        return this.completeAsync(supplier, this.defaultExecutor());
    }

    public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException();
        }
        if (this.result == null) {
            this.whenComplete((BiConsumer)new Canceller(Delayer.delay(new Timeout(this), timeout, unit)));
        }
        return this;
    }

    public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException();
        }
        if (this.result == null) {
            this.whenComplete((BiConsumer)new Canceller(Delayer.delay(new DelayedCompleter<T>(this, value), timeout, unit)));
        }
        return this;
    }

    public static Executor delayedExecutor(long delay, TimeUnit unit, Executor executor) {
        if (unit == null || executor == null) {
            throw new NullPointerException();
        }
        return new DelayedExecutor(delay, unit, executor);
    }

    public static Executor delayedExecutor(long delay, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException();
        }
        return new DelayedExecutor(delay, unit, ASYNC_POOL);
    }

    public static <U> CompletionStage<U> completedStage(U value) {
        return new MinimalStage(value == null ? NIL : value);
    }

    public static <U> CompletableFuture<U> failedFuture(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        return new CompletableFuture(new AltResult(ex));
    }

    public static <U> CompletionStage<U> failedStage(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        return new MinimalStage(new AltResult(ex));
    }

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            RESULT = l.findVarHandle(CompletableFuture.class, "result", Object.class);
            STACK = l.findVarHandle(CompletableFuture.class, "stack", Completion.class);
            NEXT = l.findVarHandle(Completion.class, "next", Completion.class);
        }
        catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
        Class<LockSupport> clazz = LockSupport.class;
    }

    static abstract class Completion
    extends ForkJoinTask<Void>
    implements Runnable,
    AsynchronousCompletionTask {
        volatile Completion next;

        Completion() {
        }

        abstract CompletableFuture<?> tryFire(int var1);

        abstract boolean isLive();

        @Override
        public final void run() {
            this.tryFire(1);
        }

        @Override
        public final boolean exec() {
            this.tryFire(1);
            return false;
        }

        @Override
        public final Void getRawResult() {
            return null;
        }

        @Override
        public final void setRawResult(Void v) {
        }
    }

    static final class AltResult {
        final Throwable ex;

        AltResult(Throwable x) {
            this.ex = x;
        }
    }

    static final class UniApply<T, V>
    extends UniCompletion<T, V> {
        Function<? super T, ? extends V> fn;

        UniApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, Function<? super T, ? extends V> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        /*
         * Unable to fully structure code
         */
        final CompletableFuture<V> tryFire(int mode) {
            block6: {
                a = this.src;
                if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                    return null;
                }
                if (d.result != null) break block6;
                if (!(r instanceof AltResult)) ** GOTO lbl12
                x = ((AltResult)r).ex;
                if (x != null) {
                    d.completeThrowable(x, r);
                } else {
                    r = null;
lbl12:
                    // 2 sources

                    try {
                        if (mode <= 0 && !this.claim()) {
                            return null;
                        }
                        t = r;
                        d.completeValue(f.apply(t));
                    }
                    catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class UniAccept<T>
    extends UniCompletion<T, Void> {
        Consumer<? super T> fn;

        UniAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, Consumer<? super T> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        /*
         * Unable to fully structure code
         */
        final CompletableFuture<Void> tryFire(int mode) {
            block6: {
                a = this.src;
                if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                    return null;
                }
                if (d.result != null) break block6;
                if (!(r instanceof AltResult)) ** GOTO lbl12
                x = ((AltResult)r).ex;
                if (x != null) {
                    d.completeThrowable(x, r);
                } else {
                    r = null;
lbl12:
                    // 2 sources

                    try {
                        if (mode <= 0 && !this.claim()) {
                            return null;
                        }
                        t = r;
                        f.accept(t);
                        d.completeNull();
                    }
                    catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class UniRun<T>
    extends UniCompletion<T, Void> {
        Runnable fn;

        UniRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, Runnable fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            Runnable f;
            CompletableFuture d;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                return null;
            }
            if (d.result == null) {
                Throwable x;
                if (r instanceof AltResult && (x = ((AltResult)r).ex) != null) {
                    d.completeThrowable(x, r);
                } else {
                    try {
                        if (mode <= 0 && !this.claim()) {
                            return null;
                        }
                        f.run();
                        d.completeNull();
                    }
                    catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class UniWhenComplete<T>
    extends UniCompletion<T, T> {
        BiConsumer<? super T, ? super Throwable> fn;

        UniWhenComplete(Executor executor, CompletableFuture<T> dep, CompletableFuture<T> src, BiConsumer<? super T, ? super Throwable> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<T> tryFire(int mode) {
            BiConsumer<? super T, ? super Throwable> f;
            CompletableFuture d;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null || !d.uniWhenComplete(r, f, mode > 0 ? null : this)) {
                return null;
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class UniHandle<T, V>
    extends UniCompletion<T, V> {
        BiFunction<? super T, Throwable, ? extends V> fn;

        UniHandle(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, BiFunction<? super T, Throwable, ? extends V> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<V> tryFire(int mode) {
            BiFunction<? super T, Throwable, ? extends V> f;
            CompletableFuture d;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null || !d.uniHandle(r, f, mode > 0 ? null : this)) {
                return null;
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class UniExceptionally<T>
    extends UniCompletion<T, T> {
        Function<? super Throwable, ? extends T> fn;

        UniExceptionally(Executor executor, CompletableFuture<T> dep, CompletableFuture<T> src, Function<? super Throwable, ? extends T> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<T> tryFire(int mode) {
            Function<? super Throwable, ? extends T> f;
            CompletableFuture d;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null || !d.uniExceptionally(r, f, mode > 0 ? null : this)) {
                return null;
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class UniComposeExceptionally<T>
    extends UniCompletion<T, T> {
        Function<Throwable, ? extends CompletionStage<T>> fn;

        UniComposeExceptionally(Executor executor, CompletableFuture<T> dep, CompletableFuture<T> src, Function<Throwable, ? extends CompletionStage<T>> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<T> tryFire(int mode) {
            Function<Throwable, CompletionStage<Throwable>> f;
            CompletableFuture d;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                return null;
            }
            if (d.result == null) {
                Throwable x;
                if (r instanceof AltResult && (x = ((AltResult)r).ex) != null) {
                    try {
                        if (mode <= 0 && !this.claim()) {
                            return null;
                        }
                        CompletableFuture<T> g = f.apply(x).toCompletableFuture();
                        r = g.result;
                        if (r != null) {
                            d.completeRelay(r);
                        } else {
                            g.unipush(new UniRelay(d, g));
                            if (d.result == null) {
                                return null;
                            }
                        }
                    }
                    catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                } else {
                    d.internalComplete(r);
                }
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class UniRelay<U, T extends U>
    extends UniCompletion<T, U> {
        UniRelay(CompletableFuture<U> dep, CompletableFuture<T> src) {
            super(null, dep, src);
        }

        final CompletableFuture<U> tryFire(int mode) {
            CompletableFuture d;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (d = this.dep) == null) {
                return null;
            }
            if (d.result == null) {
                d.completeRelay(r);
            }
            this.src = null;
            this.dep = null;
            return d.postFire(a, mode);
        }
    }

    static final class MinimalStage<T>
    extends CompletableFuture<T> {
        MinimalStage() {
        }

        MinimalStage(Object r) {
            super(r);
        }

        @Override
        public <U> CompletableFuture<U> newIncompleteFuture() {
            return new MinimalStage<T>();
        }

        @Override
        public T get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T getNow(T valueIfAbsent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T join() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T resultNow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Throwable exceptionNow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean complete(T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean completeExceptionally(Throwable ex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void obtrudeValue(T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void obtrudeException(Throwable ex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCompletedExceptionally() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future.State state() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getNumberOfDependents() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<T> toCompletableFuture() {
            Object r = this.result;
            if (r != null) {
                return new CompletableFuture(MinimalStage.encodeRelay(r));
            }
            CompletableFuture d = new CompletableFuture();
            this.unipush(new UniRelay(d, this));
            return d;
        }
    }

    static final class UniCompose<T, V>
    extends UniCompletion<T, V> {
        Function<? super T, ? extends CompletionStage<V>> fn;

        UniCompose(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, Function<? super T, ? extends CompletionStage<V>> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        /*
         * Unable to fully structure code
         */
        final CompletableFuture<V> tryFire(int mode) {
            block9: {
                a = this.src;
                if (a == null || (r = a.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                    return null;
                }
                if (d.result != null) break block9;
                if (!(r instanceof AltResult)) ** GOTO lbl12
                x = ((AltResult)r).ex;
                if (x != null) {
                    d.completeThrowable(x, r);
                } else {
                    r = null;
lbl12:
                    // 2 sources

                    try {
                        if (mode <= 0 && !this.claim()) {
                            return null;
                        }
                        t = r;
                        g = f.apply(t).toCompletableFuture();
                        r = g.result;
                        if (r != null) {
                            d.completeRelay(r);
                        } else {
                            g.unipush(new UniRelay<U, V>(d, g));
                            if (d.result == null) {
                                return null;
                            }
                        }
                    }
                    catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
            }
            this.src = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, mode);
        }
    }

    static final class CoCompletion
    extends Completion {
        BiCompletion<?, ?, ?> base;

        CoCompletion(BiCompletion<?, ?, ?> base) {
            this.base = base;
        }

        @Override
        final CompletableFuture<?> tryFire(int mode) {
            CompletableFuture<?> d;
            BiCompletion<?, ?, ?> c = this.base;
            if (c == null || (d = c.tryFire(mode)) == null) {
                return null;
            }
            this.base = null;
            return d;
        }

        @Override
        final boolean isLive() {
            BiCompletion<?, ?, ?> c = this.base;
            return c != null && c.dep != null;
        }
    }

    static abstract class BiCompletion<T, U, V>
    extends UniCompletion<T, V> {
        CompletableFuture<U> snd;

        BiCompletion(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(executor, dep, src);
            this.snd = snd;
        }
    }

    static final class BiApply<T, U, V>
    extends BiCompletion<T, U, V> {
        BiFunction<? super T, ? super U, ? extends V> fn;

        BiApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd, BiFunction<? super T, ? super U, ? extends V> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<V> tryFire(int mode) {
            BiFunction<? super T, ? super U, ? extends V> f;
            CompletableFuture d;
            Object s;
            CompletableFuture b;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (b = this.snd) == null || (s = b.result) == null || (d = this.dep) == null || (f = this.fn) == null || !d.biApply(r, s, f, mode > 0 ? null : this)) {
                return null;
            }
            this.src = null;
            this.snd = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, b, mode);
        }
    }

    static final class BiAccept<T, U>
    extends BiCompletion<T, U, Void> {
        BiConsumer<? super T, ? super U> fn;

        BiAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, BiConsumer<? super T, ? super U> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            BiConsumer<? super T, ? super U> f;
            CompletableFuture d;
            Object s;
            CompletableFuture b;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (b = this.snd) == null || (s = b.result) == null || (d = this.dep) == null || (f = this.fn) == null || !d.biAccept(r, s, f, mode > 0 ? null : this)) {
                return null;
            }
            this.src = null;
            this.snd = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, b, mode);
        }
    }

    static final class BiRun<T, U>
    extends BiCompletion<T, U, Void> {
        Runnable fn;

        BiRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Runnable fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            Runnable f;
            CompletableFuture d;
            Object s;
            CompletableFuture b;
            Object r;
            CompletableFuture a = this.src;
            if (a == null || (r = a.result) == null || (b = this.snd) == null || (s = b.result) == null || (d = this.dep) == null || (f = this.fn) == null || !d.biRun(r, s, f, mode > 0 ? null : this)) {
                return null;
            }
            this.src = null;
            this.snd = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, b, mode);
        }
    }

    static final class BiRelay<T, U>
    extends BiCompletion<T, U, Void> {
        BiRelay(CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }

        /*
         * Unable to fully structure code
         */
        final CompletableFuture<Void> tryFire(int mode) {
            block4: {
                block5: {
                    a = this.src;
                    if (a == null || (r = a.result) == null || (b = this.snd) == null || (s = b.result) == null || (d = this.dep) == null) {
                        return null;
                    }
                    if (d.result != null) break block4;
                    if (!(r instanceof AltResult)) break block5;
                    z = r;
                    x = ((AltResult)z).ex;
                    if (x != null) ** GOTO lbl-1000
                }
                if (s instanceof AltResult) {
                    z = s;
                    x = ((AltResult)z).ex;
                    ** if (x == null) goto lbl-1000
                }
                ** GOTO lbl-1000
lbl-1000:
                // 2 sources

                {
                    d.completeThrowable(x, z);
                    ** GOTO lbl20
                }
lbl-1000:
                // 2 sources

                {
                    d.completeNull();
                }
            }
            this.src = null;
            this.snd = null;
            this.dep = null;
            return d.postFire(a, b, mode);
        }
    }

    static final class OrApply<T, U extends T, V>
    extends BiCompletion<T, U, V> {
        Function<? super T, ? extends V> fn;

        OrApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Function<? super T, ? extends V> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        /*
         * Unable to fully structure code
         */
        final CompletableFuture<V> tryFire(int mode) {
            block6: {
                a = this.src;
                if (a == null || (b = this.snd) == null || (r = a.result) == null && (r = b.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                    return null;
                }
                if (d.result != null) break block6;
                try {
                    if (mode <= 0 && !this.claim()) {
                        return null;
                    }
                    if (!(r instanceof AltResult)) ** GOTO lbl15
                    x = ((AltResult)r).ex;
                    if (x != null) {
                        d.completeThrowable(x, r);
                    } else {
                        r = null;
lbl15:
                        // 2 sources

                        t = r;
                        d.completeValue(f.apply(t));
                    }
                }
                catch (Throwable ex) {
                    d.completeThrowable(ex);
                }
            }
            this.src = null;
            this.snd = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, b, mode);
        }
    }

    static final class OrAccept<T, U extends T>
    extends BiCompletion<T, U, Void> {
        Consumer<? super T> fn;

        OrAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Consumer<? super T> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        /*
         * Unable to fully structure code
         */
        final CompletableFuture<Void> tryFire(int mode) {
            block6: {
                a = this.src;
                if (a == null || (b = this.snd) == null || (r = a.result) == null && (r = b.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                    return null;
                }
                if (d.result != null) break block6;
                try {
                    if (mode <= 0 && !this.claim()) {
                        return null;
                    }
                    if (!(r instanceof AltResult)) ** GOTO lbl15
                    x = ((AltResult)r).ex;
                    if (x != null) {
                        d.completeThrowable(x, r);
                    } else {
                        r = null;
lbl15:
                        // 2 sources

                        t = r;
                        f.accept(t);
                        d.completeNull();
                    }
                }
                catch (Throwable ex) {
                    d.completeThrowable(ex);
                }
            }
            this.src = null;
            this.snd = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, b, mode);
        }
    }

    static final class OrRun<T, U>
    extends BiCompletion<T, U, Void> {
        Runnable fn;

        OrRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Runnable fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            Runnable f;
            CompletableFuture d;
            Object r;
            CompletableFuture b;
            CompletableFuture a = this.src;
            if (a == null || (b = this.snd) == null || (r = a.result) == null && (r = b.result) == null || (d = this.dep) == null || (f = this.fn) == null) {
                return null;
            }
            if (d.result == null) {
                try {
                    Throwable x;
                    if (mode <= 0 && !this.claim()) {
                        return null;
                    }
                    if (r instanceof AltResult && (x = ((AltResult)r).ex) != null) {
                        d.completeThrowable(x, r);
                    } else {
                        f.run();
                        d.completeNull();
                    }
                }
                catch (Throwable ex) {
                    d.completeThrowable(ex);
                }
            }
            this.src = null;
            this.snd = null;
            this.dep = null;
            this.fn = null;
            return d.postFire(a, b, mode);
        }
    }

    static final class AsyncSupply<T>
    extends ForkJoinTask<Void>
    implements Runnable,
    AsynchronousCompletionTask {
        CompletableFuture<T> dep;
        Supplier<? extends T> fn;

        AsyncSupply(CompletableFuture<T> dep, Supplier<? extends T> fn) {
            this.dep = dep;
            this.fn = fn;
        }

        @Override
        public final Void getRawResult() {
            return null;
        }

        @Override
        public final void setRawResult(Void v) {
        }

        @Override
        public final boolean exec() {
            this.run();
            return false;
        }

        @Override
        public void run() {
            Supplier<T> f;
            CompletableFuture<T> d = this.dep;
            if (d != null && (f = this.fn) != null) {
                this.dep = null;
                this.fn = null;
                if (d.result == null) {
                    try {
                        d.completeValue(f.get());
                    }
                    catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
                d.postComplete();
            }
        }
    }

    static final class AsyncRun
    extends ForkJoinTask<Void>
    implements Runnable,
    AsynchronousCompletionTask {
        CompletableFuture<Void> dep;
        Runnable fn;

        AsyncRun(CompletableFuture<Void> dep, Runnable fn) {
            this.dep = dep;
            this.fn = fn;
        }

        @Override
        public final Void getRawResult() {
            return null;
        }

        @Override
        public final void setRawResult(Void v) {
        }

        @Override
        public final boolean exec() {
            this.run();
            return false;
        }

        @Override
        public void run() {
            Runnable f;
            CompletableFuture<Void> d = this.dep;
            if (d != null && (f = this.fn) != null) {
                this.dep = null;
                this.fn = null;
                if (d.result == null) {
                    try {
                        f.run();
                        d.completeNull();
                    }
                    catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
                d.postComplete();
            }
        }
    }

    static final class Signaller
    extends Completion
    implements ForkJoinPool.ManagedBlocker {
        long nanos;
        final long deadline;
        final boolean interruptible;
        boolean interrupted;
        volatile Thread thread = Thread.currentThread();

        Signaller(boolean interruptible, long nanos, long deadline) {
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.deadline = deadline;
        }

        @Override
        final CompletableFuture<?> tryFire(int ignore) {
            Thread w = this.thread;
            if (w != null) {
                this.thread = null;
                LockSupport.unpark(w);
            }
            return null;
        }

        @Override
        public boolean isReleasable() {
            if (Thread.interrupted()) {
                this.interrupted = true;
            }
            return this.interrupted && this.interruptible || this.deadline != 0L && (this.nanos <= 0L || (this.nanos = this.deadline - System.nanoTime()) <= 0L) || this.thread == null;
        }

        @Override
        public boolean block() {
            while (!this.isReleasable()) {
                if (this.deadline == 0L) {
                    LockSupport.park(this);
                    continue;
                }
                LockSupport.parkNanos(this, this.nanos);
            }
            return true;
        }

        @Override
        final boolean isLive() {
            return this.thread != null;
        }
    }

    static class AnyOf
    extends Completion {
        CompletableFuture<Object> dep;
        CompletableFuture<?> src;
        CompletableFuture<?>[] srcs;

        AnyOf(CompletableFuture<Object> dep, CompletableFuture<?> src, CompletableFuture<?>[] srcs) {
            this.dep = dep;
            this.src = src;
            this.srcs = srcs;
        }

        final CompletableFuture<Object> tryFire(int mode) {
            CompletableFuture<?>[] as;
            CompletableFuture<Object> d;
            Object r;
            CompletableFuture<?> a;
            block7: {
                block6: {
                    a = this.src;
                    if (a == null || (r = a.result) == null || (d = this.dep) == null) break block6;
                    as = this.srcs;
                    if (this.srcs != null) break block7;
                }
                return null;
            }
            this.src = null;
            this.dep = null;
            this.srcs = null;
            if (d.completeRelay(r)) {
                for (CompletableFuture<?> b : as) {
                    if (b == a) continue;
                    b.cleanStack();
                }
                if (mode < 0) {
                    return d;
                }
                d.postComplete();
            }
            return null;
        }

        @Override
        final boolean isLive() {
            CompletableFuture<Object> d = this.dep;
            return d != null && d.result == null;
        }
    }

    static final class Canceller
    implements BiConsumer<Object, Throwable> {
        final Future<?> f;

        Canceller(Future<?> f) {
            this.f = f;
        }

        @Override
        public void accept(Object ignore, Throwable ex) {
            if (this.f != null && !this.f.isDone()) {
                this.f.cancel(false);
            }
        }
    }

    static final class Timeout
    implements Runnable {
        final CompletableFuture<?> f;

        Timeout(CompletableFuture<?> f) {
            this.f = f;
        }

        @Override
        public void run() {
            if (this.f != null && !this.f.isDone()) {
                this.f.completeExceptionally(new TimeoutException());
            }
        }
    }

    static final class Delayer {
        static final ScheduledThreadPoolExecutor delayer = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory());

        Delayer() {
        }

        static ScheduledFuture<?> delay(Runnable command, long delay, TimeUnit unit) {
            return delayer.schedule(command, delay, unit);
        }

        static {
            delayer.setRemoveOnCancelPolicy(true);
        }

        static final class DaemonThreadFactory
        implements ThreadFactory {
            DaemonThreadFactory() {
            }

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("CompletableFutureDelayScheduler");
                return t;
            }
        }
    }

    static final class DelayedCompleter<U>
    implements Runnable {
        final CompletableFuture<U> f;
        final U u;

        DelayedCompleter(CompletableFuture<U> f, U u) {
            this.f = f;
            this.u = u;
        }

        @Override
        public void run() {
            if (this.f != null) {
                this.f.complete(this.u);
            }
        }
    }

    static final class DelayedExecutor
    implements Executor {
        final long delay;
        final TimeUnit unit;
        final Executor executor;

        DelayedExecutor(long delay, TimeUnit unit, Executor executor) {
            this.delay = delay;
            this.unit = unit;
            this.executor = executor;
        }

        @Override
        public void execute(Runnable r) {
            Delayer.delay(new TaskSubmitter(this.executor, r), this.delay, this.unit);
        }
    }

    private static final class ThreadPerTaskExecutor
    implements Executor {
        private ThreadPerTaskExecutor() {
        }

        @Override
        public void execute(Runnable r) {
            Objects.requireNonNull(r);
            new Thread(r).start();
        }
    }

    static final class TaskSubmitter
    implements Runnable {
        final Executor executor;
        final Runnable action;

        TaskSubmitter(Executor executor, Runnable action) {
            this.executor = executor;
            this.action = action;
        }

        @Override
        public void run() {
            this.executor.execute(this.action);
        }
    }

    static abstract class UniCompletion<T, V>
    extends Completion {
        Executor executor;
        CompletableFuture<V> dep;
        CompletableFuture<T> src;

        UniCompletion(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src) {
            this.executor = executor;
            this.dep = dep;
            this.src = src;
        }

        final boolean claim() {
            Executor e = this.executor;
            if (this.compareAndSetForkJoinTaskTag((short)0, (short)1)) {
                if (e == null) {
                    return true;
                }
                this.executor = null;
                e.execute(this);
            }
            return false;
        }

        @Override
        final boolean isLive() {
            return this.dep != null;
        }
    }

    public static interface AsynchronousCompletionTask {
    }
}

