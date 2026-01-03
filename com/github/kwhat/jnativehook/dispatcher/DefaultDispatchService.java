/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.dispatcher;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultDispatchService
extends ThreadPoolExecutor {
    public DefaultDispatchService() {
        super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory(){

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("JNativeHook Dispatch Thread");
                t.setDaemon(true);
                return t;
            }
        });
    }
}

