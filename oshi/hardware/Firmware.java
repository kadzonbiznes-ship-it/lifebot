/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.Immutable
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface Firmware {
    public String getManufacturer();

    public String getName();

    public String getDescription();

    public String getVersion();

    public String getReleaseDate();
}

