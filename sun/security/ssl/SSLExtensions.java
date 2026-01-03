/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import sun.security.ssl.Alert;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.Utilities;
import sun.security.util.HexDumpEncoder;

final class SSLExtensions {
    private final SSLHandshake.HandshakeMessage handshakeMessage;
    private final Map<SSLExtension, byte[]> extMap = new LinkedHashMap<SSLExtension, byte[]>();
    private int encodedLength;
    private final Map<Integer, byte[]> logMap = SSLLogger.isOn ? new LinkedHashMap() : null;

    SSLExtensions(SSLHandshake.HandshakeMessage handshakeMessage) {
        this.handshakeMessage = handshakeMessage;
        this.encodedLength = 2;
    }

    SSLExtensions(SSLHandshake.HandshakeMessage hm, ByteBuffer m, SSLExtension[] extensions) throws IOException {
        this.handshakeMessage = hm;
        if (m.remaining() < 2) {
            throw hm.handshakeContext.conContext.fatal(Alert.DECODE_ERROR, "Incorrect extensions: no length field");
        }
        int len = Record.getInt16(m);
        if (len > m.remaining()) {
            throw hm.handshakeContext.conContext.fatal(Alert.DECODE_ERROR, "Insufficient extensions data");
        }
        this.encodedLength = len + 2;
        while (len > 0) {
            int extId = Record.getInt16(m);
            int extLen = Record.getInt16(m);
            if (extLen > m.remaining()) {
                throw hm.handshakeContext.conContext.fatal(Alert.DECODE_ERROR, "Error parsing extension (" + extId + "): no sufficient data");
            }
            boolean isSupported = true;
            SSLHandshake handshakeType = hm.handshakeType();
            if (SSLExtension.isConsumable(extId) && SSLExtension.valueOf(handshakeType, extId) == null) {
                if (extId == SSLExtension.CH_SUPPORTED_GROUPS.id && handshakeType == SSLHandshake.SERVER_HELLO) {
                    isSupported = false;
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.warning("Received buggy supported_groups extension in the ServerHello handshake message", new Object[0]);
                    }
                } else {
                    if (handshakeType == SSLHandshake.SERVER_HELLO) {
                        throw hm.handshakeContext.conContext.fatal(Alert.UNSUPPORTED_EXTENSION, "extension (" + extId + ") should not be presented in " + handshakeType.name);
                    }
                    isSupported = false;
                }
            }
            if (isSupported) {
                isSupported = false;
                for (SSLExtension extension : extensions) {
                    if (extension.id != extId || extension.onLoadConsumer == null) continue;
                    if (extension.handshakeType != handshakeType) {
                        throw hm.handshakeContext.conContext.fatal(Alert.UNSUPPORTED_EXTENSION, "extension (" + extId + ") should not be presented in " + handshakeType.name);
                    }
                    byte[] extData = new byte[extLen];
                    m.get(extData);
                    this.extMap.put(extension, extData);
                    if (this.logMap != null) {
                        this.logMap.put(extId, extData);
                    }
                    isSupported = true;
                    break;
                }
            }
            if (!isSupported) {
                if (this.logMap != null) {
                    byte[] extData = new byte[extLen];
                    m.get(extData);
                    this.logMap.put(extId, extData);
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("Ignore unknown or unsupported extension", SSLExtensions.toString(extId, extData));
                    }
                } else {
                    int pos = m.position() + extLen;
                    m.position(pos);
                }
            }
            len -= extLen + 4;
        }
    }

    byte[] get(SSLExtension ext) {
        return this.extMap.get(ext);
    }

    void consumeOnLoad(HandshakeContext context, SSLExtension[] extensions) throws IOException {
        for (SSLExtension extension : extensions) {
            if (context.negotiatedProtocol != null && !extension.isAvailable(context.negotiatedProtocol)) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.fine("Ignore unsupported extension: " + extension.name, new Object[0]);
                continue;
            }
            if (!this.extMap.containsKey(extension)) {
                if (extension.onLoadAbsence != null) {
                    extension.absentOnLoad(context, this.handshakeMessage);
                    continue;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.fine("Ignore unavailable extension: " + extension.name, new Object[0]);
                continue;
            }
            if (extension.onLoadConsumer == null) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.warning("Ignore unsupported extension: " + extension.name, new Object[0]);
                continue;
            }
            ByteBuffer m = ByteBuffer.wrap(this.extMap.get(extension));
            extension.consumeOnLoad(context, this.handshakeMessage, m);
            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
            SSLLogger.fine("Consumed extension: " + extension.name, new Object[0]);
        }
    }

    void consumeOnTrade(HandshakeContext context, SSLExtension[] extensions) throws IOException {
        for (SSLExtension extension : extensions) {
            if (!this.extMap.containsKey(extension)) {
                if (extension.onTradeAbsence != null) {
                    extension.absentOnTrade(context, this.handshakeMessage);
                    continue;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.fine("Ignore unavailable extension: " + extension.name, new Object[0]);
                continue;
            }
            if (extension.onTradeConsumer == null) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.warning("Ignore impact of unsupported extension: " + extension.name, new Object[0]);
                continue;
            }
            extension.consumeOnTrade(context, this.handshakeMessage);
            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
            SSLLogger.fine("Populated with extension: " + extension.name, new Object[0]);
        }
    }

    void produce(HandshakeContext context, SSLExtension[] extensions) throws IOException {
        for (SSLExtension extension : extensions) {
            if (this.extMap.containsKey(extension)) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.fine("Ignore, duplicated extension: " + extension.name, new Object[0]);
                continue;
            }
            if (extension.networkProducer == null) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.warning("Ignore, no extension producer defined: " + extension.name, new Object[0]);
                continue;
            }
            byte[] encoded = extension.produce(context, this.handshakeMessage);
            if (encoded != null) {
                this.extMap.put(extension, encoded);
                this.encodedLength += encoded.length + 4;
                continue;
            }
            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
            SSLLogger.fine("Ignore, context unavailable extension: " + extension.name, new Object[0]);
        }
    }

    void reproduce(HandshakeContext context, SSLExtension[] extensions) throws IOException {
        for (SSLExtension extension : extensions) {
            if (extension.networkProducer == null) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.warning("Ignore, no extension producer defined: " + extension.name, new Object[0]);
                continue;
            }
            byte[] encoded = extension.produce(context, this.handshakeMessage);
            if (encoded != null) {
                if (this.extMap.containsKey(extension)) {
                    byte[] old = this.extMap.replace(extension, encoded);
                    if (old != null) {
                        this.encodedLength -= old.length + 4;
                    }
                } else {
                    this.extMap.put(extension, encoded);
                }
                this.encodedLength += encoded.length + 4;
                continue;
            }
            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
            SSLLogger.fine("Ignore, context unavailable extension: " + extension.name, new Object[0]);
        }
    }

    int length() {
        if (this.extMap.isEmpty()) {
            return 0;
        }
        return this.encodedLength;
    }

    void send(HandshakeOutStream hos) throws IOException {
        int extsLen = this.length();
        if (extsLen == 0) {
            return;
        }
        hos.putInt16(extsLen - 2);
        for (SSLExtension ext : SSLExtension.values()) {
            byte[] extData = this.extMap.get(ext);
            if (extData == null) continue;
            hos.putInt16(ext.id);
            hos.putBytes16(extData);
        }
    }

    public String toString() {
        if (this.extMap.isEmpty() && (this.logMap == null || this.logMap.isEmpty())) {
            return "<no extension>";
        }
        StringBuilder builder = new StringBuilder(512);
        if (this.logMap != null && !this.logMap.isEmpty()) {
            for (Map.Entry<Integer, byte[]> en : this.logMap.entrySet()) {
                SSLExtension ext = SSLExtension.valueOf(this.handshakeMessage.handshakeType(), en.getKey());
                if (builder.length() != 0) {
                    builder.append(",\n");
                }
                if (ext != null) {
                    builder.append(ext.toString(this.handshakeMessage.handshakeContext, ByteBuffer.wrap(en.getValue())));
                    continue;
                }
                builder.append(SSLExtensions.toString(en.getKey(), en.getValue()));
            }
        } else {
            for (Map.Entry<SSLExtension, byte[]> en : this.extMap.entrySet()) {
                if (builder.length() != 0) {
                    builder.append(",\n");
                }
                builder.append(en.getKey().toString(this.handshakeMessage.handshakeContext, ByteBuffer.wrap(en.getValue())));
            }
        }
        return builder.toString();
    }

    private static String toString(int extId, byte[] extData) {
        String extName = SSLExtension.nameOf(extId);
        MessageFormat messageFormat = new MessageFormat("\"{0} ({1})\": '{'\n{2}\n'}'", Locale.ENGLISH);
        HexDumpEncoder hexEncoder = new HexDumpEncoder();
        String encoded = hexEncoder.encodeBuffer(extData);
        Object[] messageFields = new Object[]{extName, extId, Utilities.indent(encoded)};
        return messageFormat.format(messageFields);
    }
}

