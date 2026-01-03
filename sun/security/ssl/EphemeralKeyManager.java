/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;

final class EphemeralKeyManager {
    private static final int INDEX_RSA512 = 0;
    private static final int INDEX_RSA1024 = 1;
    private final EphemeralKeyPair[] keys = new EphemeralKeyPair[]{new EphemeralKeyPair(null), new EphemeralKeyPair(null)};
    private final ReentrantLock cachedKeysLock = new ReentrantLock();

    EphemeralKeyManager() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    KeyPair getRSAKeyPair(boolean export, SecureRandom random) {
        int index;
        int length;
        if (export) {
            length = 512;
            index = 0;
        } else {
            length = 1024;
            index = 1;
        }
        KeyPair kp = this.keys[index].getKeyPair();
        if (kp != null) {
            return kp;
        }
        this.cachedKeysLock.lock();
        try {
            kp = this.keys[index].getKeyPair();
            if (kp != null) {
                KeyPair keyPair = kp;
                return keyPair;
            }
            try {
                KeyPairGenerator kgen = KeyPairGenerator.getInstance("RSA");
                kgen.initialize(length, random);
                this.keys[index] = new EphemeralKeyPair(kgen.genKeyPair());
                kp = this.keys[index].getKeyPair();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        finally {
            this.cachedKeysLock.unlock();
        }
        return kp;
    }

    private static class EphemeralKeyPair {
        private static final int MAX_USE = 200;
        private static final long USE_INTERVAL = 3600000L;
        private KeyPair keyPair;
        private int uses;
        private final long expirationTime;

        private EphemeralKeyPair(KeyPair keyPair) {
            this.keyPair = keyPair;
            this.expirationTime = System.currentTimeMillis() + 3600000L;
        }

        private boolean isValid() {
            return this.keyPair != null && this.uses < 200 && System.currentTimeMillis() < this.expirationTime;
        }

        private KeyPair getKeyPair() {
            if (!this.isValid()) {
                this.keyPair = null;
                return null;
            }
            ++this.uses;
            return this.keyPair;
        }
    }
}

