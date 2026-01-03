/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.DefaultChannelProgressivePromise
 *  io.netty.channel.FailedChannelFuture
 */
package io.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerMask;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.channel.DefaultChannelProgressivePromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.FailedChannelFuture;
import io.netty.channel.SucceededChannelFuture;
import io.netty.channel.VoidChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakHint;
import io.netty.util.concurrent.AbstractEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.OrderedEventExecutor;
import io.netty.util.internal.ObjectPool;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PromiseNotificationUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

abstract class AbstractChannelHandlerContext
implements ChannelHandlerContext,
ResourceLeakHint {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannelHandlerContext.class);
    volatile AbstractChannelHandlerContext next;
    volatile AbstractChannelHandlerContext prev;
    private static final AtomicIntegerFieldUpdater<AbstractChannelHandlerContext> HANDLER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractChannelHandlerContext.class, "handlerState");
    private static final int ADD_PENDING = 1;
    private static final int ADD_COMPLETE = 2;
    private static final int REMOVE_COMPLETE = 3;
    private static final int INIT = 0;
    private final DefaultChannelPipeline pipeline;
    private final String name;
    private final boolean ordered;
    private final int executionMask;
    final EventExecutor executor;
    private ChannelFuture succeededFuture;
    private Tasks invokeTasks;
    private volatile int handlerState = 0;

    AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutor executor, String name, Class<? extends ChannelHandler> handlerClass) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.pipeline = pipeline;
        this.executor = executor;
        this.executionMask = ChannelHandlerMask.mask(handlerClass);
        this.ordered = executor == null || executor instanceof OrderedEventExecutor;
    }

    @Override
    public Channel channel() {
        return this.pipeline.channel();
    }

    @Override
    public ChannelPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.channel().config().getAllocator();
    }

    @Override
    public EventExecutor executor() {
        if (this.executor == null) {
            return this.channel().eventLoop();
        }
        return this.executor;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        AbstractChannelHandlerContext.invokeChannelRegistered(this.findContextInbound(2));
        return this;
    }

    static void invokeChannelRegistered(AbstractChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelRegistered();
        } else {
            executor.execute((Runnable)new /* Unavailable Anonymous Inner Class!! */);
        }
    }

    private void invokeChannelRegistered() {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.channelRegistered(this);
                        break block6;
                    }
                    if (handler instanceof ChannelInboundHandlerAdapter) {
                        ((ChannelInboundHandlerAdapter)handler).channelRegistered(this);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).channelRegistered(this);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireChannelRegistered();
            }
        }
    }

    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        AbstractChannelHandlerContext.invokeChannelUnregistered(this.findContextInbound(4));
        return this;
    }

    static void invokeChannelUnregistered(AbstractChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelUnregistered();
        } else {
            executor.execute((Runnable)new /* Unavailable Anonymous Inner Class!! */);
        }
    }

    private void invokeChannelUnregistered() {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.channelUnregistered(this);
                        break block6;
                    }
                    if (handler instanceof ChannelInboundHandlerAdapter) {
                        ((ChannelInboundHandlerAdapter)handler).channelUnregistered(this);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).channelUnregistered(this);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireChannelUnregistered();
            }
        }
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        AbstractChannelHandlerContext.invokeChannelActive(this.findContextInbound(8));
        return this;
    }

    static void invokeChannelActive(AbstractChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelActive();
        } else {
            executor.execute((Runnable)new /* Unavailable Anonymous Inner Class!! */);
        }
    }

    private void invokeChannelActive() {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.channelActive(this);
                        break block6;
                    }
                    if (handler instanceof ChannelInboundHandlerAdapter) {
                        ((ChannelInboundHandlerAdapter)handler).channelActive(this);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).channelActive(this);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireChannelActive();
            }
        }
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        AbstractChannelHandlerContext.invokeChannelInactive(this.findContextInbound(16));
        return this;
    }

    static void invokeChannelInactive(AbstractChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelInactive();
        } else {
            executor.execute((Runnable)new /* Unavailable Anonymous Inner Class!! */);
        }
    }

    private void invokeChannelInactive() {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.channelInactive(this);
                        break block6;
                    }
                    if (handler instanceof ChannelInboundHandlerAdapter) {
                        ((ChannelInboundHandlerAdapter)handler).channelInactive(this);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).channelInactive(this);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireChannelInactive();
            }
        }
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        AbstractChannelHandlerContext.invokeExceptionCaught(this.findContextInbound(1), cause);
        return this;
    }

    static void invokeExceptionCaught(AbstractChannelHandlerContext next, Throwable cause) {
        block4: {
            ObjectUtil.checkNotNull(cause, "cause");
            EventExecutor executor = next.executor();
            if (executor.inEventLoop()) {
                next.invokeExceptionCaught(cause);
            } else {
                try {
                    executor.execute((Runnable)new /* Unavailable Anonymous Inner Class!! */);
                }
                catch (Throwable t) {
                    if (!logger.isWarnEnabled()) break block4;
                    logger.warn("Failed to submit an exceptionCaught() event.", t);
                    logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
                }
            }
        }
    }

    private void invokeExceptionCaught(Throwable cause) {
        if (this.invokeHandler()) {
            try {
                this.handler().exceptionCaught(this, cause);
            }
            catch (Throwable error) {
                if (logger.isDebugEnabled()) {
                    logger.debug("An exception {}was thrown by a user handler's exceptionCaught() method while handling the following exception:", (Object)ThrowableUtil.stackTraceToString(error), (Object)cause);
                } else if (logger.isWarnEnabled()) {
                    logger.warn("An exception '{}' [enable DEBUG level for full stacktrace] was thrown by a user handler's exceptionCaught() method while handling the following exception:", (Object)error, (Object)cause);
                }
            }
        } else {
            this.fireExceptionCaught(cause);
        }
    }

    @Override
    public ChannelHandlerContext fireUserEventTriggered(Object event) {
        AbstractChannelHandlerContext.invokeUserEventTriggered(this.findContextInbound(128), event);
        return this;
    }

    static void invokeUserEventTriggered(AbstractChannelHandlerContext next, Object event) {
        ObjectUtil.checkNotNull(event, "event");
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeUserEventTriggered(event);
        } else {
            executor.execute((Runnable)new /* Unavailable Anonymous Inner Class!! */);
        }
    }

    private void invokeUserEventTriggered(Object event) {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.userEventTriggered(this, event);
                        break block6;
                    }
                    if (handler instanceof ChannelInboundHandlerAdapter) {
                        ((ChannelInboundHandlerAdapter)handler).userEventTriggered(this, event);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).userEventTriggered(this, event);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireUserEventTriggered(event);
            }
        }
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object msg) {
        AbstractChannelHandlerContext.invokeChannelRead(this.findContextInbound(32), msg);
        return this;
    }

    static void invokeChannelRead(AbstractChannelHandlerContext next, Object msg) {
        Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelRead(m);
        } else {
            executor.execute((Runnable)new /* Unavailable Anonymous Inner Class!! */);
        }
    }

    private void invokeChannelRead(Object msg) {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.channelRead(this, msg);
                        break block6;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).channelRead(this, msg);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).channelRead(this, msg);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireChannelRead(msg);
            }
        }
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        AbstractChannelHandlerContext.invokeChannelReadComplete(this.findContextInbound(64));
        return this;
    }

    static void invokeChannelReadComplete(AbstractChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelReadComplete();
        } else {
            Tasks tasks = next.invokeTasks;
            if (tasks == null) {
                next.invokeTasks = tasks = new Tasks(next);
            }
            executor.execute(tasks.invokeChannelReadCompleteTask);
        }
    }

    private void invokeChannelReadComplete() {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.channelReadComplete(this);
                        break block6;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).channelReadComplete(this);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).channelReadComplete(this);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireChannelReadComplete();
            }
        }
    }

    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        AbstractChannelHandlerContext.invokeChannelWritabilityChanged(this.findContextInbound(256));
        return this;
    }

    static void invokeChannelWritabilityChanged(AbstractChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelWritabilityChanged();
        } else {
            Tasks tasks = next.invokeTasks;
            if (tasks == null) {
                next.invokeTasks = tasks = new Tasks(next);
            }
            executor.execute(tasks.invokeChannelWritableStateChangedTask);
        }
    }

    private void invokeChannelWritabilityChanged() {
        block6: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.channelWritabilityChanged(this);
                        break block6;
                    }
                    if (handler instanceof ChannelInboundHandlerAdapter) {
                        ((ChannelInboundHandlerAdapter)handler).channelWritabilityChanged(this);
                        break block6;
                    }
                    ((ChannelInboundHandler)handler).channelWritabilityChanged(this);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.fireChannelWritabilityChanged();
            }
        }
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return this.bind(localAddress, this.newPromise());
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return this.connect(remoteAddress, this.newPromise());
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return this.connect(remoteAddress, localAddress, this.newPromise());
    }

    @Override
    public ChannelFuture disconnect() {
        return this.disconnect(this.newPromise());
    }

    @Override
    public ChannelFuture close() {
        return this.close(this.newPromise());
    }

    @Override
    public ChannelFuture deregister() {
        return this.deregister(this.newPromise());
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        ObjectUtil.checkNotNull(localAddress, "localAddress");
        if (this.isNotValidPromise(promise, false)) {
            return promise;
        }
        AbstractChannelHandlerContext next = this.findContextOutbound(512);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeBind(localAddress, promise);
        } else {
            AbstractChannelHandlerContext.safeExecute(executor, (Runnable)new /* Unavailable Anonymous Inner Class!! */, promise, null, false);
        }
        return promise;
    }

    private void invokeBind(SocketAddress localAddress, ChannelPromise promise) {
        block7: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.bind(this, localAddress, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).bind(this, localAddress, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelOutboundHandlerAdapter) {
                        ((ChannelOutboundHandlerAdapter)handler).bind((ChannelHandlerContext)this, localAddress, promise);
                        break block7;
                    }
                    ((ChannelOutboundHandler)handler).bind(this, localAddress, promise);
                }
                catch (Throwable t) {
                    AbstractChannelHandlerContext.notifyOutboundHandlerException(t, promise);
                }
            } else {
                this.bind(localAddress, promise);
            }
        }
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return this.connect(remoteAddress, null, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ObjectUtil.checkNotNull(remoteAddress, "remoteAddress");
        if (this.isNotValidPromise(promise, false)) {
            return promise;
        }
        AbstractChannelHandlerContext next = this.findContextOutbound(1024);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeConnect(remoteAddress, localAddress, promise);
        } else {
            AbstractChannelHandlerContext.safeExecute(executor, (Runnable)new /* Unavailable Anonymous Inner Class!! */, promise, null, false);
        }
        return promise;
    }

    private void invokeConnect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        block7: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.connect(this, remoteAddress, localAddress, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).connect(this, remoteAddress, localAddress, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelOutboundHandlerAdapter) {
                        ((ChannelOutboundHandlerAdapter)handler).connect((ChannelHandlerContext)this, remoteAddress, localAddress, promise);
                        break block7;
                    }
                    ((ChannelOutboundHandler)handler).connect(this, remoteAddress, localAddress, promise);
                }
                catch (Throwable t) {
                    AbstractChannelHandlerContext.notifyOutboundHandlerException(t, promise);
                }
            } else {
                this.connect(remoteAddress, localAddress, promise);
            }
        }
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        if (!this.channel().metadata().hasDisconnect()) {
            return this.close(promise);
        }
        if (this.isNotValidPromise(promise, false)) {
            return promise;
        }
        AbstractChannelHandlerContext next = this.findContextOutbound(2048);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeDisconnect(promise);
        } else {
            AbstractChannelHandlerContext.safeExecute(executor, (Runnable)new /* Unavailable Anonymous Inner Class!! */, promise, null, false);
        }
        return promise;
    }

    private void invokeDisconnect(ChannelPromise promise) {
        block7: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.disconnect(this, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).disconnect(this, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelOutboundHandlerAdapter) {
                        ((ChannelOutboundHandlerAdapter)handler).disconnect((ChannelHandlerContext)this, promise);
                        break block7;
                    }
                    ((ChannelOutboundHandler)handler).disconnect(this, promise);
                }
                catch (Throwable t) {
                    AbstractChannelHandlerContext.notifyOutboundHandlerException(t, promise);
                }
            } else {
                this.disconnect(promise);
            }
        }
    }

    @Override
    public ChannelFuture close(final ChannelPromise promise) {
        if (this.isNotValidPromise(promise, false)) {
            return promise;
        }
        final AbstractChannelHandlerContext next = this.findContextOutbound(4096);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeClose(promise);
        } else {
            AbstractChannelHandlerContext.safeExecute(executor, new Runnable(){

                @Override
                public void run() {
                    next.invokeClose(promise);
                }
            }, promise, null, false);
        }
        return promise;
    }

    private void invokeClose(ChannelPromise promise) {
        block7: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.close(this, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).close(this, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelOutboundHandlerAdapter) {
                        ((ChannelOutboundHandlerAdapter)handler).close((ChannelHandlerContext)this, promise);
                        break block7;
                    }
                    ((ChannelOutboundHandler)handler).close(this, promise);
                }
                catch (Throwable t) {
                    AbstractChannelHandlerContext.notifyOutboundHandlerException(t, promise);
                }
            } else {
                this.close(promise);
            }
        }
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        if (this.isNotValidPromise(promise, false)) {
            return promise;
        }
        AbstractChannelHandlerContext next = this.findContextOutbound(8192);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeDeregister(promise);
        } else {
            AbstractChannelHandlerContext.safeExecute(executor, (Runnable)new /* Unavailable Anonymous Inner Class!! */, promise, null, false);
        }
        return promise;
    }

    private void invokeDeregister(ChannelPromise promise) {
        block7: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.deregister(this, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).deregister(this, promise);
                        break block7;
                    }
                    if (handler instanceof ChannelOutboundHandlerAdapter) {
                        ((ChannelOutboundHandlerAdapter)handler).deregister((ChannelHandlerContext)this, promise);
                        break block7;
                    }
                    ((ChannelOutboundHandler)handler).deregister(this, promise);
                }
                catch (Throwable t) {
                    AbstractChannelHandlerContext.notifyOutboundHandlerException(t, promise);
                }
            } else {
                this.deregister(promise);
            }
        }
    }

    @Override
    public ChannelHandlerContext read() {
        AbstractChannelHandlerContext next = this.findContextOutbound(16384);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeRead();
        } else {
            Tasks tasks = next.invokeTasks;
            if (tasks == null) {
                next.invokeTasks = tasks = new Tasks(next);
            }
            executor.execute(tasks.invokeReadTask);
        }
        return this;
    }

    private void invokeRead() {
        block7: {
            if (this.invokeHandler()) {
                try {
                    ChannelHandler handler = this.handler();
                    DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
                    if (handler == headContext) {
                        headContext.read(this);
                        break block7;
                    }
                    if (handler instanceof ChannelDuplexHandler) {
                        ((ChannelDuplexHandler)handler).read(this);
                        break block7;
                    }
                    if (handler instanceof ChannelOutboundHandlerAdapter) {
                        ((ChannelOutboundHandlerAdapter)handler).read((ChannelHandlerContext)this);
                        break block7;
                    }
                    ((ChannelOutboundHandler)handler).read(this);
                }
                catch (Throwable t) {
                    this.invokeExceptionCaught(t);
                }
            } else {
                this.read();
            }
        }
    }

    @Override
    public ChannelFuture write(Object msg) {
        return this.write(msg, this.newPromise());
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        this.write(msg, false, promise);
        return promise;
    }

    void invokeWrite(Object msg, ChannelPromise promise) {
        if (this.invokeHandler()) {
            this.invokeWrite0(msg, promise);
        } else {
            this.write(msg, promise);
        }
    }

    private void invokeWrite0(Object msg, ChannelPromise promise) {
        try {
            ChannelHandler handler = this.handler();
            DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
            if (handler == headContext) {
                headContext.write(this, msg, promise);
            } else if (handler instanceof ChannelDuplexHandler) {
                ((ChannelDuplexHandler)handler).write(this, msg, promise);
            } else if (handler instanceof ChannelOutboundHandlerAdapter) {
                ((ChannelOutboundHandlerAdapter)handler).write((ChannelHandlerContext)this, msg, promise);
            } else {
                ((ChannelOutboundHandler)handler).write(this, msg, promise);
            }
        }
        catch (Throwable t) {
            AbstractChannelHandlerContext.notifyOutboundHandlerException(t, promise);
        }
    }

    @Override
    public ChannelHandlerContext flush() {
        AbstractChannelHandlerContext next = this.findContextOutbound(65536);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeFlush();
        } else {
            Tasks tasks = next.invokeTasks;
            if (tasks == null) {
                next.invokeTasks = tasks = new Tasks(next);
            }
            AbstractChannelHandlerContext.safeExecute(executor, tasks.invokeFlushTask, this.channel().voidPromise(), null, false);
        }
        return this;
    }

    private void invokeFlush() {
        if (this.invokeHandler()) {
            this.invokeFlush0();
        } else {
            this.flush();
        }
    }

    private void invokeFlush0() {
        try {
            ChannelHandler handler = this.handler();
            DefaultChannelPipeline.HeadContext headContext = this.pipeline.head;
            if (handler == headContext) {
                headContext.flush(this);
            } else if (handler instanceof ChannelDuplexHandler) {
                ((ChannelDuplexHandler)handler).flush(this);
            } else if (handler instanceof ChannelOutboundHandlerAdapter) {
                ((ChannelOutboundHandlerAdapter)handler).flush((ChannelHandlerContext)this);
            } else {
                ((ChannelOutboundHandler)handler).flush(this);
            }
        }
        catch (Throwable t) {
            this.invokeExceptionCaught(t);
        }
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        this.write(msg, true, promise);
        return promise;
    }

    void invokeWriteAndFlush(Object msg, ChannelPromise promise) {
        if (this.invokeHandler()) {
            this.invokeWrite0(msg, promise);
            this.invokeFlush0();
        } else {
            this.writeAndFlush(msg, promise);
        }
    }

    private void write(Object msg, boolean flush, ChannelPromise promise) {
        ObjectUtil.checkNotNull(msg, "msg");
        try {
            if (this.isNotValidPromise(promise, true)) {
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        catch (RuntimeException e) {
            ReferenceCountUtil.release(msg);
            throw e;
        }
        AbstractChannelHandlerContext next = this.findContextOutbound(flush ? 98304 : 32768);
        Object m = this.pipeline.touch(msg, next);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            if (flush) {
                next.invokeWriteAndFlush(m, promise);
            } else {
                next.invokeWrite(m, promise);
            }
        } else {
            WriteTask task = WriteTask.newInstance(next, m, promise, flush);
            if (!AbstractChannelHandlerContext.safeExecute(executor, task, promise, m, !flush)) {
                task.cancel();
            }
        }
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return this.writeAndFlush(msg, this.newPromise());
    }

    private static void notifyOutboundHandlerException(Throwable cause, ChannelPromise promise) {
        PromiseNotificationUtil.tryFailure(promise, cause, promise instanceof VoidChannelPromise ? null : logger);
    }

    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(this.channel(), this.executor());
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return new DefaultChannelProgressivePromise(this.channel(), this.executor());
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        ChannelFuture succeededFuture = this.succeededFuture;
        if (succeededFuture == null) {
            this.succeededFuture = succeededFuture = new SucceededChannelFuture(this.channel(), this.executor());
        }
        return succeededFuture;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return new FailedChannelFuture(this.channel(), this.executor(), cause);
    }

    private boolean isNotValidPromise(ChannelPromise promise, boolean allowVoidPromise) {
        ObjectUtil.checkNotNull(promise, "promise");
        if (promise.isDone()) {
            if (promise.isCancelled()) {
                return true;
            }
            throw new IllegalArgumentException("promise already done: " + promise);
        }
        if (promise.channel() != this.channel()) {
            throw new IllegalArgumentException(String.format("promise.channel does not match: %s (expected: %s)", promise.channel(), this.channel()));
        }
        if (promise.getClass() == DefaultChannelPromise.class) {
            return false;
        }
        if (!allowVoidPromise && promise instanceof VoidChannelPromise) {
            throw new IllegalArgumentException(StringUtil.simpleClassName(VoidChannelPromise.class) + " not allowed for this operation");
        }
        if (promise instanceof AbstractChannel.CloseFuture) {
            throw new IllegalArgumentException(StringUtil.simpleClassName(AbstractChannel.CloseFuture.class) + " not allowed in a pipeline");
        }
        return false;
    }

    private AbstractChannelHandlerContext findContextInbound(int mask) {
        AbstractChannelHandlerContext ctx = this;
        EventExecutor currentExecutor = this.executor();
        while (AbstractChannelHandlerContext.skipContext(ctx = ctx.next, currentExecutor, mask, 510)) {
        }
        return ctx;
    }

    private AbstractChannelHandlerContext findContextOutbound(int mask) {
        AbstractChannelHandlerContext ctx = this;
        EventExecutor currentExecutor = this.executor();
        while (AbstractChannelHandlerContext.skipContext(ctx = ctx.prev, currentExecutor, mask, 130560)) {
        }
        return ctx;
    }

    private static boolean skipContext(AbstractChannelHandlerContext ctx, EventExecutor currentExecutor, int mask, int onlyMask) {
        return (ctx.executionMask & (onlyMask | mask)) == 0 || ctx.executor() == currentExecutor && (ctx.executionMask & mask) == 0;
    }

    @Override
    public ChannelPromise voidPromise() {
        return this.channel().voidPromise();
    }

    final void setRemoved() {
        this.handlerState = 3;
    }

    final boolean setAddComplete() {
        int oldState;
        do {
            if ((oldState = this.handlerState) != 3) continue;
            return false;
        } while (!HANDLER_STATE_UPDATER.compareAndSet(this, oldState, 2));
        return true;
    }

    final void setAddPending() {
        boolean updated = HANDLER_STATE_UPDATER.compareAndSet(this, 0, 1);
        assert (updated);
    }

    final void callHandlerAdded() throws Exception {
        if (this.setAddComplete()) {
            this.handler().handlerAdded(this);
        }
    }

    final void callHandlerRemoved() throws Exception {
        try {
            if (this.handlerState == 2) {
                this.handler().handlerRemoved(this);
            }
        }
        finally {
            this.setRemoved();
        }
    }

    private boolean invokeHandler() {
        int handlerState = this.handlerState;
        return handlerState == 2 || !this.ordered && handlerState == 1;
    }

    @Override
    public boolean isRemoved() {
        return this.handlerState == 3;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return this.channel().attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return this.channel().hasAttr(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean safeExecute(EventExecutor executor, Runnable runnable, ChannelPromise promise, Object msg, boolean lazy) {
        try {
            if (lazy && executor instanceof AbstractEventExecutor) {
                ((AbstractEventExecutor)executor).lazyExecute(runnable);
            } else {
                executor.execute(runnable);
            }
            return true;
        }
        catch (Throwable cause) {
            try {
                if (msg != null) {
                    ReferenceCountUtil.release(msg);
                }
            }
            finally {
                promise.setFailure(cause);
            }
            return false;
        }
    }

    @Override
    public String toHintString() {
        return '\'' + this.name + "' will handle the message from this point.";
    }

    public String toString() {
        return StringUtil.simpleClassName(ChannelHandlerContext.class) + '(' + this.name + ", " + this.channel() + ')';
    }

    static /* synthetic */ void access$000(AbstractChannelHandlerContext x0) {
        x0.invokeChannelRegistered();
    }

    static /* synthetic */ void access$100(AbstractChannelHandlerContext x0) {
        x0.invokeChannelUnregistered();
    }

    static /* synthetic */ void access$200(AbstractChannelHandlerContext x0) {
        x0.invokeChannelActive();
    }

    static /* synthetic */ void access$300(AbstractChannelHandlerContext x0) {
        x0.invokeChannelInactive();
    }

    static /* synthetic */ void access$400(AbstractChannelHandlerContext x0, Throwable x1) {
        x0.invokeExceptionCaught(x1);
    }

    static /* synthetic */ void access$500(AbstractChannelHandlerContext x0, Object x1) {
        x0.invokeUserEventTriggered(x1);
    }

    static /* synthetic */ void access$600(AbstractChannelHandlerContext x0, Object x1) {
        x0.invokeChannelRead(x1);
    }

    static /* synthetic */ void access$900(AbstractChannelHandlerContext x0, SocketAddress x1, ChannelPromise x2) {
        x0.invokeBind(x1, x2);
    }

    static /* synthetic */ void access$1000(AbstractChannelHandlerContext x0, SocketAddress x1, SocketAddress x2, ChannelPromise x3) {
        x0.invokeConnect(x1, x2, x3);
    }

    static /* synthetic */ void access$1100(AbstractChannelHandlerContext x0, ChannelPromise x1) {
        x0.invokeDisconnect(x1);
    }

    static /* synthetic */ void access$1300(AbstractChannelHandlerContext x0, ChannelPromise x1) {
        x0.invokeDeregister(x1);
    }

    static /* synthetic */ void access$1900(AbstractChannelHandlerContext x0) {
        x0.invokeChannelReadComplete();
    }

    static /* synthetic */ void access$2000(AbstractChannelHandlerContext x0) {
        x0.invokeRead();
    }

    static /* synthetic */ void access$2100(AbstractChannelHandlerContext x0) {
        x0.invokeChannelWritabilityChanged();
    }

    static /* synthetic */ void access$2200(AbstractChannelHandlerContext x0) {
        x0.invokeFlush();
    }

    private static final class Tasks {
        private final AbstractChannelHandlerContext next;
        private final Runnable invokeChannelReadCompleteTask = new /* Unavailable Anonymous Inner Class!! */;
        private final Runnable invokeReadTask = new /* Unavailable Anonymous Inner Class!! */;
        private final Runnable invokeChannelWritableStateChangedTask = new /* Unavailable Anonymous Inner Class!! */;
        private final Runnable invokeFlushTask = new /* Unavailable Anonymous Inner Class!! */;

        Tasks(AbstractChannelHandlerContext next) {
            this.next = next;
        }

        static /* synthetic */ AbstractChannelHandlerContext access$1800(Tasks x0) {
            return x0.next;
        }
    }

    static final class WriteTask
    implements Runnable {
        private static final ObjectPool<WriteTask> RECYCLER = ObjectPool.newPool(new ObjectPool.ObjectCreator<WriteTask>(){

            @Override
            public WriteTask newObject(ObjectPool.Handle<WriteTask> handle) {
                return new WriteTask(handle);
            }
        });
        private static final boolean ESTIMATE_TASK_SIZE_ON_SUBMIT = SystemPropertyUtil.getBoolean("io.netty.transport.estimateSizeOnSubmit", true);
        private static final int WRITE_TASK_OVERHEAD = SystemPropertyUtil.getInt("io.netty.transport.writeTaskSizeOverhead", 32);
        private final ObjectPool.Handle<WriteTask> handle;
        private AbstractChannelHandlerContext ctx;
        private Object msg;
        private ChannelPromise promise;
        private int size;

        static WriteTask newInstance(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise, boolean flush) {
            WriteTask task = RECYCLER.get();
            WriteTask.init(task, ctx, msg, promise, flush);
            return task;
        }

        private WriteTask(ObjectPool.Handle<? extends WriteTask> handle) {
            this.handle = handle;
        }

        protected static void init(WriteTask task, AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise, boolean flush) {
            task.ctx = ctx;
            task.msg = msg;
            task.promise = promise;
            if (ESTIMATE_TASK_SIZE_ON_SUBMIT) {
                task.size = ctx.pipeline.estimatorHandle().size(msg) + WRITE_TASK_OVERHEAD;
                ctx.pipeline.incrementPendingOutboundBytes(task.size);
            } else {
                task.size = 0;
            }
            if (flush) {
                task.size |= Integer.MIN_VALUE;
            }
        }

        @Override
        public void run() {
            try {
                this.decrementPendingOutboundBytes();
                if (this.size >= 0) {
                    this.ctx.invokeWrite(this.msg, this.promise);
                } else {
                    this.ctx.invokeWriteAndFlush(this.msg, this.promise);
                }
            }
            finally {
                this.recycle();
            }
        }

        void cancel() {
            try {
                this.decrementPendingOutboundBytes();
            }
            finally {
                this.recycle();
            }
        }

        private void decrementPendingOutboundBytes() {
            if (ESTIMATE_TASK_SIZE_ON_SUBMIT) {
                this.ctx.pipeline.decrementPendingOutboundBytes(this.size & Integer.MAX_VALUE);
            }
        }

        private void recycle() {
            this.ctx = null;
            this.msg = null;
            this.promise = null;
            this.handle.recycle(this);
        }
    }
}

