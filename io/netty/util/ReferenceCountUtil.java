/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.ReferenceCountUtil$ReleasingTask
 *  io.netty.util.ThreadDeathWatcher
 */
package io.netty.util;

import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ThreadDeathWatcher;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public final class ReferenceCountUtil {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountUtil.class);

    public static <T> T retain(T msg) {
        if (msg instanceof ReferenceCounted) {
            return (T)((ReferenceCounted)msg).retain();
        }
        return msg;
    }

    public static <T> T retain(T msg, int increment) {
        ObjectUtil.checkPositive(increment, "increment");
        if (msg instanceof ReferenceCounted) {
            return (T)((ReferenceCounted)msg).retain(increment);
        }
        return msg;
    }

    public static <T> T touch(T msg) {
        if (msg instanceof ReferenceCounted) {
            return (T)((ReferenceCounted)msg).touch();
        }
        return msg;
    }

    public static <T> T touch(T msg, Object hint) {
        if (msg instanceof ReferenceCounted) {
            return (T)((ReferenceCounted)msg).touch(hint);
        }
        return msg;
    }

    public static boolean release(Object msg) {
        if (msg instanceof ReferenceCounted) {
            return ((ReferenceCounted)msg).release();
        }
        return false;
    }

    public static boolean release(Object msg, int decrement) {
        ObjectUtil.checkPositive(decrement, "decrement");
        if (msg instanceof ReferenceCounted) {
            return ((ReferenceCounted)msg).release(decrement);
        }
        return false;
    }

    public static void safeRelease(Object msg) {
        try {
            ReferenceCountUtil.release(msg);
        }
        catch (Throwable t) {
            logger.warn("Failed to release a message: {}", msg, (Object)t);
        }
    }

    public static void safeRelease(Object msg, int decrement) {
        block2: {
            try {
                ObjectUtil.checkPositive(decrement, "decrement");
                ReferenceCountUtil.release(msg, decrement);
            }
            catch (Throwable t) {
                if (!logger.isWarnEnabled()) break block2;
                logger.warn("Failed to release a message: {} (decrement: {})", msg, decrement, t);
            }
        }
    }

    @Deprecated
    public static <T> T releaseLater(T msg) {
        return ReferenceCountUtil.releaseLater(msg, 1);
    }

    @Deprecated
    public static <T> T releaseLater(T msg, int decrement) {
        ObjectUtil.checkPositive(decrement, "decrement");
        if (msg instanceof ReferenceCounted) {
            ThreadDeathWatcher.watch((Thread)Thread.currentThread(), (Runnable)new ReleasingTask((ReferenceCounted)msg, decrement));
        }
        return msg;
    }

    public static int refCnt(Object msg) {
        return msg instanceof ReferenceCounted ? ((ReferenceCounted)msg).refCnt() : -1;
    }

    private ReferenceCountUtil() {
    }

    static /* synthetic */ InternalLogger access$000() {
        return logger;
    }

    static {
        ResourceLeakDetector.addExclusions(ReferenceCountUtil.class, "touch");
    }
}

