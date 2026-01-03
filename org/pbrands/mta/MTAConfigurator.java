/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.ui.IProgressWindow
 */
package org.pbrands.mta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.Generated;
import org.pbrands.mta.ConfigLocator;
import org.pbrands.mta.MTAChatSettings;
import org.pbrands.ui.IProgressWindow;
import org.pbrands.util.Log;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class MTAConfigurator {
    private final File configFile;
    private Document xmlDocument;
    private MTAChatSettings chatSettings;

    public boolean isChatColorBlack() {
        if (this.chatSettings == null) {
            return false;
        }
        MTAChatSettings.Color color = this.chatSettings.getChatColor();
        return color.r() == 0 && color.g() == 0 && color.b() == 0;
    }

    public MTAConfigurator(IProgressWindow startupWindow) throws Exception {
        startupWindow.updateStatus("Szukanie plik\u00f3w konfiguracyjnych MTA");
        startupWindow.showSpinner();
        startupWindow.setVisible(true);
        ConfigLocator locator = new ConfigLocator();
        this.configFile = locator.findConfigFile();
        startupWindow.setVisible(false);
        if (this.configFile == null) {
            throw new FileNotFoundException("coreconfig.xml nie zosta\u0142 znaleziony.");
        }
        Log.info("Przetwarzanie pliku konfiguracyjnego: {}", this.configFile.getAbsolutePath());
        this.loadConfig();
    }

    public MTAConfigurator() throws Exception {
        ConfigLocator locator = new ConfigLocator();
        this.configFile = locator.findConfigFile();
        if (this.configFile == null) {
            throw new FileNotFoundException("coreconfig.xml nie zosta\u0142 znaleziony.");
        }
        Log.info("Przetwarzanie pliku konfiguracyjnego: {}", this.configFile.getAbsolutePath());
        this.loadConfig();
    }

    private void loadConfig() throws Exception {
        this.xmlDocument = MTAConfigurator.loadXml(this.configFile);
        this.chatSettings = MTAChatSettings.fromXml(this.xmlDocument);
    }

    public void reload() throws Exception {
        this.loadConfig();
        Log.info("Prze\u0142adowano konfiguracj\u0119 MTA");
    }

    private static Document loadXml(File file) throws Exception {
        try (FileInputStream is = new FileInputStream(file);){
            Document document;
            try (InputStreamReader reader = new InputStreamReader((InputStream)is, StandardCharsets.UTF_8);){
                InputSource source = new InputSource(reader);
                source.setEncoding("UTF-8");
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
                doc.getDocumentElement().normalize();
                document = doc;
            }
            return document;
        }
    }

    @Generated
    public File getConfigFile() {
        return this.configFile;
    }

    @Generated
    public MTAChatSettings getChatSettings() {
        return this.chatSettings;
    }
}

