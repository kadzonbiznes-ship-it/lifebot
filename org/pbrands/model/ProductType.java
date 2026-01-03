/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.model;

import java.util.Arrays;
import lombok.Generated;

public enum ProductType {
    ONLY_PING(-1, "ping"),
    MINER_BOT_4LIFE(0, "kopalnia"),
    FISHING_BOT_SANTORI(1, "rybak-sant"),
    MAP(2, "mapy"),
    FISHING_BOT_PROJECTRPG(3, "rybak-prpg"),
    MINER_BOT_MEGARPG(4, "kopalnia-mega"),
    FISHING_BOT_OUTMTA(5, "rybak-out"),
    SPOOFY(6, "spoofy"),
    DECOMPILER(7, "decompiler"),
    LOADER(8, "loader");

    private final byte productId;
    private final String shortName;

    private ProductType(byte productId, String shortName) {
        this.productId = productId;
        this.shortName = shortName;
    }

    public static ProductType getProductType(byte productId) {
        return Arrays.stream(ProductType.values()).filter(productType -> productType.getProductId() == productId).findFirst().orElse(null);
    }

    @Generated
    public byte getProductId() {
        return this.productId;
    }

    @Generated
    public String getShortName() {
        return this.shortName;
    }
}

