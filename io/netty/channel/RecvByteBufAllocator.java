/*
 * Decompiled with CFR 0.152.
 */
package io.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.util.UncheckedBooleanSupplier;

public interface RecvByteBufAllocator {
    public Handle newHandle();

    public static interface ExtendedHandle
    extends Handle {
        public boolean continueReading(UncheckedBooleanSupplier var1);
    }

    @Deprecated
    public static interface Handle {
        public ByteBuf allocate(ByteBufAllocator var1);

        public int guess();

        public void reset(ChannelConfig var1);

        public void incMessagesRead(int var1);

        public void lastBytesRead(int var1);

        public int lastBytesRead();

        public void attemptedBytesRead(int var1);

        public int attemptedBytesRead();

        public boolean continueReading();

        public void readComplete();
    }
}

