/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.model;

import lombok.Generated;

public class MapDefinition {
    private int mapId;
    private String displayName;
    private int maxCollectibles;

    @Generated
    public int getMapId() {
        return this.mapId;
    }

    @Generated
    public String getDisplayName() {
        return this.displayName;
    }

    @Generated
    public int getMaxCollectibles() {
        return this.maxCollectibles;
    }

    @Generated
    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    @Generated
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Generated
    public void setMaxCollectibles(int maxCollectibles) {
        this.maxCollectibles = maxCollectibles;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MapDefinition)) {
            return false;
        }
        MapDefinition other = (MapDefinition)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getMapId() != other.getMapId()) {
            return false;
        }
        if (this.getMaxCollectibles() != other.getMaxCollectibles()) {
            return false;
        }
        String this$displayName = this.getDisplayName();
        String other$displayName = other.getDisplayName();
        return !(this$displayName == null ? other$displayName != null : !this$displayName.equals(other$displayName));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof MapDefinition;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getMapId();
        result = result * 59 + this.getMaxCollectibles();
        String $displayName = this.getDisplayName();
        result = result * 59 + ($displayName == null ? 43 : $displayName.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "MapDefinition(mapId=" + this.getMapId() + ", displayName=" + this.getDisplayName() + ", maxCollectibles=" + this.getMaxCollectibles() + ")";
    }

    @Generated
    public MapDefinition(int mapId, String displayName, int maxCollectibles) {
        this.mapId = mapId;
        this.displayName = displayName;
        this.maxCollectibles = maxCollectibles;
    }

    @Generated
    public MapDefinition() {
    }
}

