/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.netty;

import org.pbrands.model.ProductType;
import org.pbrands.netty.ProductNettyBase;
import org.pbrands.netty.handler.FishingBotClientHandler;

public class NettyClient
extends ProductNettyBase<FishingBotClientHandler> {
    public NettyClient(String host, int port, String token, String hwid, ProductType productType) {
        super(host, port, token, hwid, productType);
    }

    @Override
    public boolean reconnectOnFailure() {
        return true;
    }

    @Override
    public FishingBotClientHandler createHandler() {
        return new FishingBotClientHandler();
    }
}

