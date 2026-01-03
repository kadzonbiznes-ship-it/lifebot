/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.mta;

import java.io.File;
import org.pbrands.util.Log;
import org.pbrands.util.WindowsUtil;

public class ConfigLocator {
    private static final int MAX_WAIT_SECONDS = 120;

    public File findConfigFile() {
        File mtaDir = this.waitForMtaAndGetDirectory();
        if (mtaDir == null) {
            Log.error("Nie uda\u0142o si\u0119 wykry\u0107 katalogu MTA z procesu");
            return null;
        }
        Log.info("Automatycznie wykryto katalog MTA: {}", mtaDir.getAbsolutePath());
        File configPath = new File(mtaDir, "MTA\\config\\coreconfig.xml");
        if (configPath.exists()) {
            Log.info("Znaleziono plik konfiguracyjny: {}", configPath.getAbsolutePath());
            return configPath;
        }
        Log.error("Plik coreconfig.xml nie zosta\u0142 znaleziony w katalogu: {}", mtaDir.getAbsolutePath());
        return null;
    }

    private File waitForMtaAndGetDirectory() {
        File mtaDir = WindowsUtil.detectMtaDirectoryFromProcess();
        if (mtaDir != null) {
            return mtaDir;
        }
        Log.info("Oczekiwanie na uruchomienie MTA...");
        for (int waited = 0; waited < 120; ++waited) {
            try {
                Thread.sleep(500L);
                continue;
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        Log.warn("Timeout - MTA nie zosta\u0142o uruchomione w ci\u0105gu {} sekund", 120);
        return null;
    }
}

