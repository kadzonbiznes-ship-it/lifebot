/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelException$StacklessChannelException
 *  io.netty.util.internal.SuppressJava6Requirement
 */
package io.netty.channel;

import io.netty.channel.ChannelException;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.ThrowableUtil;

public class ChannelException
extends RuntimeException {
    private static final long serialVersionUID = 2908618315971075004L;

    public ChannelException() {
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelException(String message) {
        super(message);
    }

    public ChannelException(Throwable cause) {
        super(cause);
    }

    @SuppressJava6Requirement(reason="uses Java 7+ RuntimeException.<init>(String, Throwable, boolean, boolean) but is guarded by version checks")
    protected ChannelException(String message, Throwable cause, boolean shared) {
        super(message, cause, false, true);
        assert (shared);
    }

    static ChannelException newStatic(String message, Class<?> clazz, String method) {
        StacklessChannelException exception = PlatformDependent.javaVersion() >= 7 ? new StacklessChannelException(message, null, true) : new StacklessChannelException(message, null);
        return (ChannelException)ThrowableUtil.unknownStackTrace(exception, clazz, method);
    }
}

