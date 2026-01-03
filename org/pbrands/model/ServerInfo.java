/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.model;

import lombok.Generated;

public class ServerInfo {
    private int id;
    private String label;
    private String host;
    private int port;
    private boolean online;
    private int responseTime;

    private ServerInfo() {
    }

    public ServerInfo(int id, String label, String host, int port) {
        this.id = id;
        this.label = label;
        this.host = host;
        this.port = port;
    }

    @Generated
    public int getId() {
        return this.id;
    }

    @Generated
    public String getLabel() {
        return this.label;
    }

    @Generated
    public String getHost() {
        return this.host;
    }

    @Generated
    public int getPort() {
        return this.port;
    }

    @Generated
    public boolean isOnline() {
        return this.online;
    }

    @Generated
    public int getResponseTime() {
        return this.responseTime;
    }

    @Generated
    public void setId(int id) {
        this.id = id;
    }

    @Generated
    public void setLabel(String label) {
        this.label = label;
    }

    @Generated
    public void setHost(String host) {
        this.host = host;
    }

    @Generated
    public void setPort(int port) {
        this.port = port;
    }

    @Generated
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Generated
    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ServerInfo)) {
            return false;
        }
        ServerInfo other = (ServerInfo)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getId() != other.getId()) {
            return false;
        }
        if (this.getPort() != other.getPort()) {
            return false;
        }
        if (this.isOnline() != other.isOnline()) {
            return false;
        }
        if (this.getResponseTime() != other.getResponseTime()) {
            return false;
        }
        String this$label = this.getLabel();
        String other$label = other.getLabel();
        if (this$label == null ? other$label != null : !this$label.equals(other$label)) {
            return false;
        }
        String this$host = this.getHost();
        String other$host = other.getHost();
        return !(this$host == null ? other$host != null : !this$host.equals(other$host));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof ServerInfo;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getId();
        result = result * 59 + this.getPort();
        result = result * 59 + (this.isOnline() ? 79 : 97);
        result = result * 59 + this.getResponseTime();
        String $label = this.getLabel();
        result = result * 59 + ($label == null ? 43 : $label.hashCode());
        String $host = this.getHost();
        result = result * 59 + ($host == null ? 43 : $host.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "ServerInfo(id=" + this.getId() + ", label=" + this.getLabel() + ", host=" + this.getHost() + ", port=" + this.getPort() + ", online=" + this.isOnline() + ", responseTime=" + this.getResponseTime() + ")";
    }
}

