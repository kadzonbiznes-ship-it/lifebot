/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.map;

import lombok.Generated;

public class MarkerType {
    private final int id;
    private final String name;
    private final int width;
    private final int height;
    private final String resourcesImagePath;
    private int textureId;

    public MarkerType(int id, String name, int width, int height, String resourcesImagePath) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.resourcesImagePath = resourcesImagePath;
    }

    @Generated
    public int getId() {
        return this.id;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public String getResourcesImagePath() {
        return this.resourcesImagePath;
    }

    @Generated
    public int getTextureId() {
        return this.textureId;
    }

    @Generated
    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
}

