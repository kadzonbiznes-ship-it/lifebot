/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

interface SSLPossession {
    default public byte[] encode() {
        return new byte[0];
    }
}

