/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.netty.handler;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClientHandler {
    public void registerPing(UUID var1, long var2, CompletableFuture<Long> var4);

    default public void registerDLLFuture(CompletableFuture<byte[]> future) {
    }

    default public void registerDLLKeyFuture(CompletableFuture<String> future) {
    }

    default public void registerDLLUberFuture(CompletableFuture<byte[]> future) {
    }

    default public void registerDLLUberKeyFuture(CompletableFuture<String> future) {
    }

    default public void registerLoaderDLLFuture(CompletableFuture<byte[]> future) {
    }

    default public void registerMapperFuture(CompletableFuture<byte[]> future) {
    }

    default public void registerDriverFuture(CompletableFuture<byte[]> future) {
    }

    default public void registerSpoofTokenFuture(CompletableFuture<String> future) {
    }
}

