/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.logic.hid.KfcHIDSimulator
 */
package org.pbrands;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.UIManager;
import org.pbrands.BaseStartup;
import org.pbrands.hid.HIDSimulator;
import org.pbrands.logic.Application;
import org.pbrands.logic.hid.KfcHIDSimulator;
import org.pbrands.logic.hid.UberHIDSimulator;
import org.pbrands.model.ProductType;
import org.pbrands.netty.NettyClient;
import org.pbrands.netty.handler.FishingBotClientHandler;
import org.pbrands.ui.ProgressWindow;
import org.pbrands.util.FontUtil;
import org.pbrands.util.LoaderUtil;
import org.pbrands.util.ObfuscatedStorage;
import org.pbrands.util.RemoteLogStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Startup
extends BaseStartup {
    private static final Logger logger = LoggerFactory.getLogger(Startup.class);

    public static void main(String[] args) throws Exception {
        new Startup().start(args);
    }

    @Override
    protected ProductType getProductType() {
        return ProductType.FISHING_BOT_PROJECTRPG;
    }

    @Override
    protected void onStart() {
        UIManager.put("defaultFont", FontUtil.NUNITO_REGULAR);
        FlatMacDarkLaf.setup();
        NettyClient nettyClient = new NettyClient(this.startupParams.serverHost, this.startupParams.serverPort, this.startupParams.token, this.getHWID(), this.getProductType());
        FishingBotClientHandler.setEncryptionKeyCallback(key -> {
            ObfuscatedStorage.initialize(key);
            logger.debug("ObfuscatedStorage initialized with server key");
        });
        FishingBotClientHandler.setRemoteLogCallback((userId, channel) -> {
            RemoteLogStream.setChannel(channel, userId);
            logger.debug("RemoteLogStream connected for user {}", userId);
        });
        nettyClient.start().sync();
        if (!nettyClient.isConnected()) {
            System.err.println("Failed to authorize.");
            System.exit(-1);
            return;
        }
        try {
            byte[] loaderDllBytes = nettyClient.requestLoaderDLL().get();
            LoaderUtil.load(loaderDllBytes);
        }
        catch (Exception e) {
            System.err.println("Failed to load loader.dll: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
            return;
        }
        ProgressWindow progressWindow = new ProgressWindow();
        Object hidSimulator = switch (this.startupParams.device.toUpperCase()) {
            case "KFC" -> {
                byte[] decodedKey = Base64.getDecoder().decode(nettyClient.requestDLLKey().get());
                SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");
                byte[] dllBytes = nettyClient.requestDLL().get();
                yield new KfcHIDSimulator(dllBytes, (SecretKey)secretKey);
            }
            case "UBER" -> {
                byte[] decodedKey = Base64.getDecoder().decode(nettyClient.requestDLLUberKey().get());
                SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");
                byte[] dllBytes = nettyClient.requestDLLUber().get();
                yield new UberHIDSimulator(dllBytes, secretKey);
            }
            default -> throw new IllegalStateException("Unknown HID device: " + this.startupParams.device.toUpperCase());
        };
        new Application(nettyClient, this.startupParams, progressWindow, (HIDSimulator)hidSimulator);
    }
}

