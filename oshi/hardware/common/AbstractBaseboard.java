/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.Immutable
 */
package oshi.hardware.common;

import oshi.annotation.concurrent.Immutable;
import oshi.hardware.Baseboard;

@Immutable
public abstract class AbstractBaseboard
implements Baseboard {
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("manufacturer=").append(this.getManufacturer()).append(", ");
        sb.append("model=").append(this.getModel()).append(", ");
        sb.append("version=").append(this.getVersion()).append(", ");
        sb.append("serial number=").append(this.getSerialNumber());
        return sb.toString();
    }
}

