/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.IOException;
import sun.security.provider.SeedGenerator;

class NativeSeedGenerator
extends SeedGenerator {
    NativeSeedGenerator(String seedFile) throws IOException {
        if (!NativeSeedGenerator.nativeGenerateSeed(new byte[2])) {
            throw new IOException("Required native CryptoAPI features not  available on this machine");
        }
    }

    private static native boolean nativeGenerateSeed(byte[] var0);

    @Override
    void getSeedBytes(byte[] result) {
        if (!NativeSeedGenerator.nativeGenerateSeed(result)) {
            throw new InternalError("Unexpected CryptoAPI failure generating seed");
        }
    }
}

