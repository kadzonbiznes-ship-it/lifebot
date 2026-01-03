/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import sun.security.ssl.ProtocolVersion;

enum ContentType {
    INVALID(0, "invalid", ProtocolVersion.PROTOCOLS_OF_13),
    CHANGE_CIPHER_SPEC(20, "change_cipher_spec", ProtocolVersion.PROTOCOLS_TO_12),
    ALERT(21, "alert", ProtocolVersion.PROTOCOLS_TO_13),
    HANDSHAKE(22, "handshake", ProtocolVersion.PROTOCOLS_TO_13),
    APPLICATION_DATA(23, "application_data", ProtocolVersion.PROTOCOLS_TO_13);

    final byte id;
    final String name;
    final ProtocolVersion[] supportedProtocols;

    private ContentType(byte id, String name, ProtocolVersion[] supportedProtocols) {
        this.id = id;
        this.name = name;
        this.supportedProtocols = supportedProtocols;
    }

    static ContentType valueOf(byte id) {
        for (ContentType ct : ContentType.values()) {
            if (ct.id != id) continue;
            return ct;
        }
        return null;
    }

    static String nameOf(byte id) {
        for (ContentType ct : ContentType.values()) {
            if (ct.id != id) continue;
            return ct.name;
        }
        return "<UNKNOWN CONTENT TYPE: " + (id & 0xFF) + ">";
    }
}

