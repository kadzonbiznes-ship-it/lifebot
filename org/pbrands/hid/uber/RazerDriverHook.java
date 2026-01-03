/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.hid.uber;

import javax.crypto.SecretKey;
import org.pbrands.hid.uber.api.UberInput;
import org.pbrands.util.DllEncryptorUtil;
import org.pbrands.util.LoaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RazerDriverHook
implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RazerDriverHook.class);
    private final UberInput lib = LoaderUtil.FORTNITE_INSTANCE;

    public RazerDriverHook(byte[] encodedDll, SecretKey key) {
        try {
            byte[] decrypt = DllEncryptorUtil.decrypt(encodedDll, key);
            byte load = this.lib.LoadUber(decrypt, decrypt.length);
            if (load == 1) {
                logger.debug("DLL injected (Code " + load + ")");
            } else {
                logger.error("Failed to inject DLL (Code " + load + ")");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDriverAvailable() {
        int rc = LoaderUtil.FORTNITE_INSTANCE.UberSendInit(3, 0, null, 0);
        System.out.println("rc = " + rc);
        if (rc == 0) {
            LoaderUtil.FORTNITE_INSTANCE.UberSendDestroy();
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        LoaderUtil.FORTNITE_INSTANCE.UberSendDestroy();
    }
}

