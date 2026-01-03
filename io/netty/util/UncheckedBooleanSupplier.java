/*
 * Decompiled with CFR 0.152.
 */
package io.netty.util;

import io.netty.util.BooleanSupplier;

public interface UncheckedBooleanSupplier
extends BooleanSupplier {
    public static final UncheckedBooleanSupplier FALSE_SUPPLIER = new /* Unavailable Anonymous Inner Class!! */;
    public static final UncheckedBooleanSupplier TRUE_SUPPLIER = new /* Unavailable Anonymous Inner Class!! */;

    @Override
    public boolean get();
}

