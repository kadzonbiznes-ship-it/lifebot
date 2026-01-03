/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Base64;
import java.util.prefs.Preferences;
import org.pbrands.model.ProductType;
import org.pbrands.model.StartupParams;
import org.pbrands.util.HWID;
import org.pbrands.util.RemoteLogStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseStartup {
    private static final Logger logger = LoggerFactory.getLogger(BaseStartup.class);
    protected static final Gson gson = new GsonBuilder().create();
    public static final File LIFEBOT_FOLDER = new File(BaseStartup.getLocalAppData(), "LifeBot");
    public static final File DATA_FOLDER = new File(LIFEBOT_FOLDER, "data");
    private static final Preferences PREFS = Preferences.userRoot().node("org/pbrands/lifebot");
    private static final String PROD_SERVER_HOST = "beta.life-bot.pl";
    private static final int PROD_SERVER_PORT = 8080;
    private static final String DEBUG_SERVER_HOST = "127.0.0.1";
    private static final int DEBUG_SERVER_PORT = 8080;
    protected StartupParams startupParams;
    protected boolean devMode;

    private static String getLocalAppData() {
        Object localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || ((String)localAppData).isEmpty()) {
            localAppData = System.getProperty("user.home") + "\\AppData\\Local";
        }
        return localAppData;
    }

    protected void start(String[] args) throws Exception {
        this.devMode = RemoteLogStream.hasDevFlag(args);
        RemoteLogStream.initialize(this.devMode);
        this.startupParams = this.parseStartupParams(args);
        if (this.startupParams == null) {
            System.err.println("Failed to parse startup parameters.");
            System.exit(-1);
            return;
        }
        DATA_FOLDER.mkdirs();
        this.setupLogger();
        if (this.startupParams.token == null || this.startupParams.token.isEmpty()) {
            System.err.println("Token is invalid!");
            System.exit(-1);
            return;
        }
        this.onStart();
    }

    protected StartupParams parseStartupParams(String[] args) {
        String encodedParams = RemoteLogStream.findEncodedParams(args);
        if (encodedParams != null) {
            try {
                byte[] decoded = Base64.getDecoder().decode(encodedParams);
                return gson.fromJson(new String(decoded), StartupParams.class);
            }
            catch (Exception e) {
                System.err.println("Failed to decode startup params: " + e.getMessage());
            }
        }
        System.out.println("Checking registry for auth token...");
        String token = PREFS.get("authToken", null);
        String device = PREFS.get("deviceType", "UBER");
        String folderName = PREFS.get("folderName", null);
        if (token != null && !token.isEmpty()) {
            System.out.println("Loaded token from registry (device=" + device + ", folderName=" + folderName + ", devMode=" + this.devMode + ")");
            StartupParams params = new StartupParams();
            params.token = token;
            params.device = device;
            params.folderName = folderName;
            params.serverHost = this.devMode ? DEBUG_SERVER_HOST : PROD_SERVER_HOST;
            params.serverPort = this.devMode ? 8080 : 8080;
            return params;
        }
        File clientJson = new File("client.json");
        if (clientJson.exists()) {
            StartupParams startupParams;
            FileReader reader = new FileReader(clientJson);
            try {
                startupParams = gson.fromJson((Reader)reader, StartupParams.class);
            }
            catch (Throwable throwable) {
                try {
                    try {
                        reader.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    System.err.println("Failed to read client.json: " + e.getMessage());
                    return null;
                }
            }
            reader.close();
            return startupParams;
        }
        System.err.println("No startup params found. Checked: args, registry, client.json");
        return null;
    }

    protected void setupLogger() {
    }

    protected abstract ProductType getProductType();

    protected abstract void onStart() throws Exception;

    protected String getHWID() {
        return HWID.generate();
    }

    protected File getAppDataFolder() {
        return new File(DATA_FOLDER, this.startupParams.folderName);
    }
}

