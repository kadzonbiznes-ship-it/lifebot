/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.util;

import lombok.Generated;

public class ConnectionConstants {
    private static ConnectionConstants instance;
    private final String apiUrl;
    private final String jarDownloadUrl;
    private final String serverHost;
    private final String alternativeServerHost;
    private final int serverPort;
    private final boolean debug;

    public ConnectionConstants(boolean debug) {
        this.debug = debug;
        if (debug) {
            this.apiUrl = "http://localhost:8081";
            this.jarDownloadUrl = "https://download.life-bot.pl";
            this.serverHost = "127.0.0.1";
            this.alternativeServerHost = "127.0.0.1";
            this.serverPort = 8080;
        } else {
            this.apiUrl = "https://api.life-bot.pl";
            this.jarDownloadUrl = "https://download.life-bot.pl";
            this.serverHost = "beta.life-bot.pl";
            this.alternativeServerHost = "alpha.life-bot.pl";
            this.serverPort = 8080;
        }
    }

    public static void initialize(boolean debug) {
        instance = new ConnectionConstants(debug);
    }

    @Generated
    public String getApiUrl() {
        return this.apiUrl;
    }

    @Generated
    public String getJarDownloadUrl() {
        return this.jarDownloadUrl;
    }

    @Generated
    public String getServerHost() {
        return this.serverHost;
    }

    @Generated
    public String getAlternativeServerHost() {
        return this.alternativeServerHost;
    }

    @Generated
    public int getServerPort() {
        return this.serverPort;
    }

    @Generated
    public boolean isDebug() {
        return this.debug;
    }

    @Generated
    public static ConnectionConstants getInstance() {
        return instance;
    }
}

