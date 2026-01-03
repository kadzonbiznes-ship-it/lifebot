/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.Immutable
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface Baseboard {
    public String getManufacturer();

    public String getModel();

    public String getVersion();

    public String getSerialNumber();
}

