/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.model;

import io.netty.util.AttributeKey;
import org.pbrands.model.ProductType;

public final class ChannelAttributes {
    public static final AttributeKey<Integer> USER_ID = AttributeKey.valueOf("USER_ID");
    public static final AttributeKey<Long> SESSION_ID = AttributeKey.valueOf("SESSION_ID");
    public static final AttributeKey<String> TOKEN = AttributeKey.valueOf("TOKEN");
    public static final AttributeKey<String> HWID = AttributeKey.valueOf("HWID");
    public static final AttributeKey<ProductType> PRODUCT_ID = AttributeKey.valueOf("PRODUCT_ID");
    public static final AttributeKey<Integer> MAP_ID = AttributeKey.valueOf("MAP_ID");
    public static final AttributeKey<String> ROLE = AttributeKey.valueOf("ROLE");
    public static final AttributeKey<String> USERNAME = AttributeKey.valueOf("USERNAME");
}

