/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.map;

import java.util.Objects;
import lombok.Generated;
import org.pbrands.map.MarkerType;

public class Marker {
    public int id;
    public boolean creator;
    private MarkerType type;
    public float x;
    public float y;
    public String description;
    private boolean underground = false;
    private int likeCount;
    private int dislikeCount;
    private boolean collected = false;
    private boolean liked;
    private boolean disliked;
    private float size = 16.0f;
    private float targetSize = 16.0f;

    public Marker(int id, boolean creator, MarkerType type, float x, float y, String description, boolean underground) {
        this.id = id;
        this.creator = creator;
        this.type = type;
        this.x = x;
        this.y = y;
        this.description = description;
        this.underground = underground;
    }

    public boolean like() {
        if (!this.liked) {
            this.liked = true;
            if (this.disliked) {
                this.disliked = false;
            }
            return true;
        }
        return false;
    }

    public boolean dislike() {
        if (!this.disliked) {
            this.disliked = true;
            if (this.liked) {
                this.liked = false;
            }
            return true;
        }
        return false;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Marker marker = (Marker)o;
        return this.id == marker.id;
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Generated
    public int getId() {
        return this.id;
    }

    @Generated
    public boolean isCreator() {
        return this.creator;
    }

    @Generated
    public MarkerType getType() {
        return this.type;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public boolean isUnderground() {
        return this.underground;
    }

    @Generated
    public int getLikeCount() {
        return this.likeCount;
    }

    @Generated
    public int getDislikeCount() {
        return this.dislikeCount;
    }

    @Generated
    public boolean isCollected() {
        return this.collected;
    }

    @Generated
    public boolean isLiked() {
        return this.liked;
    }

    @Generated
    public boolean isDisliked() {
        return this.disliked;
    }

    @Generated
    public float getSize() {
        return this.size;
    }

    @Generated
    public float getTargetSize() {
        return this.targetSize;
    }

    @Generated
    public void setId(int id) {
        this.id = id;
    }

    @Generated
    public void setCreator(boolean creator) {
        this.creator = creator;
    }

    @Generated
    public void setType(MarkerType type) {
        this.type = type;
    }

    @Generated
    public void setX(float x) {
        this.x = x;
    }

    @Generated
    public void setY(float y) {
        this.y = y;
    }

    @Generated
    public void setDescription(String description) {
        this.description = description;
    }

    @Generated
    public void setUnderground(boolean underground) {
        this.underground = underground;
    }

    @Generated
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    @Generated
    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    @Generated
    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    @Generated
    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    @Generated
    public void setDisliked(boolean disliked) {
        this.disliked = disliked;
    }

    @Generated
    public void setSize(float size) {
        this.size = size;
    }

    @Generated
    public void setTargetSize(float targetSize) {
        this.targetSize = targetSize;
    }

    @Generated
    private Marker() {
    }
}

