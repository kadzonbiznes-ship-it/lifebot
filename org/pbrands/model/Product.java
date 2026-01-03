/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.model;

import lombok.Generated;

public class Product {
    private int id;
    private String name;
    private String downloadUrl;
    private String fileName;
    private String folderName;
    private String description;
    private String subscriptionValidity;
    private String lastUpdate;
    private boolean isAuthorized;
    private boolean hidRequired;
    private String currentChecksum;
    private boolean beta;

    public Product(int id, String name, String downloadUrl, String fileName, String folderName, String description, String subscriptionValidity, String lastUpdate, boolean isAuthorized, boolean hidRequired, String currentChecksum, boolean beta) {
        this.id = id;
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.folderName = folderName;
        this.description = description;
        this.subscriptionValidity = subscriptionValidity;
        this.lastUpdate = lastUpdate;
        this.isAuthorized = isAuthorized;
        this.hidRequired = hidRequired;
        this.currentChecksum = currentChecksum;
        this.beta = beta;
    }

    public String toString() {
        return this.name;
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
    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    @Generated
    public String getFileName() {
        return this.fileName;
    }

    @Generated
    public String getFolderName() {
        return this.folderName;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public String getSubscriptionValidity() {
        return this.subscriptionValidity;
    }

    @Generated
    public String getLastUpdate() {
        return this.lastUpdate;
    }

    @Generated
    public boolean isAuthorized() {
        return this.isAuthorized;
    }

    @Generated
    public boolean isHidRequired() {
        return this.hidRequired;
    }

    @Generated
    public String getCurrentChecksum() {
        return this.currentChecksum;
    }

    @Generated
    public boolean isBeta() {
        return this.beta;
    }
}

