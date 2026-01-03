/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSessionContext;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLSessionContextImpl;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.Utilities;
import sun.security.util.HexDumpEncoder;

final class SessionTicketExtension {
    static final HandshakeProducer chNetworkProducer = new T12CHSessionTicketProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new T12CHSessionTicketConsumer();
    static final HandshakeProducer shNetworkProducer = new T12SHSessionTicketProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new T12SHSessionTicketConsumer();
    static final SSLStringizer steStringizer = new SessionTicketStringizer();
    private static final int TIMEOUT_DEFAULT = 3600000;
    private static final int keyTimeout;
    private static final int KEYLEN = 256;

    SessionTicketExtension() {
    }

    static {
        String s = GetPropertyAction.privilegedGetProperty("jdk.tls.server.statelessKeyTimeout");
        if (s != null) {
            int kt;
            block6: {
                try {
                    kt = Integer.parseInt(s) * 1000;
                    if (kt < 0 || kt > 604800) {
                        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                            SSLLogger.warning("Invalid timeout for jdk.tls.server.statelessKeyTimeout: " + kt + ".  Set to default value " + 3600000 + "sec", new Object[0]);
                        }
                        kt = 3600000;
                    }
                }
                catch (NumberFormatException e) {
                    kt = 3600000;
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block6;
                    SSLLogger.warning("Invalid timeout for jdk.tls.server.statelessKeyTimeout: " + s + ".  Set to default value " + 3600000 + "sec", new Object[0]);
                }
            }
            keyTimeout = kt;
        } else {
            keyTimeout = 3600000;
        }
    }

    private static final class T12CHSessionTicketProducer
    implements HandshakeProducer {
        T12CHSessionTicketProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!((SSLSessionContextImpl)chc.sslContext.engineGetClientSessionContext()).statelessEnabled()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Stateless resumption not supported", new Object[0]);
                }
                return null;
            }
            chc.statelessResumption = true;
            if (!chc.isResumption || chc.resumingSession == null || chc.resumingSession.getPskIdentity() == null || chc.resumingSession.getProtocolVersion().useTLS13PlusSpec()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Stateless resumption supported", new Object[0]);
                }
                return new byte[0];
            }
            SignatureScheme.updateHandshakeLocalSupportedAlgs(chc);
            return chc.resumingSession.getPskIdentity();
        }
    }

    private static final class T12CHSessionTicketConsumer
    implements SSLExtension.ExtensionConsumer {
        T12CHSessionTicketConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_SESSION_TICKET)) {
                return;
            }
            if (shc.statelessResumption) {
                return;
            }
            SSLSessionContextImpl cache = (SSLSessionContextImpl)shc.sslContext.engineGetServerSessionContext();
            if (!cache.statelessEnabled()) {
                return;
            }
            shc.statelessResumption = true;
            if (buffer.remaining() == 0) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Client accepts session tickets.", new Object[0]);
                }
                return;
            }
            SessionTicketSpec spec = new SessionTicketSpec((HandshakeContext)shc, buffer);
            ByteBuffer b = spec.decrypt(shc);
            if (b != null) {
                shc.resumingSession = new SSLSessionImpl((HandshakeContext)shc, b);
                shc.isResumption = true;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Valid stateless session ticket found", new Object[0]);
                }
            } else if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Invalid stateless session ticket found", new Object[0]);
            }
        }
    }

    private static final class T12SHSessionTicketProducer
    implements HandshakeProducer {
        T12SHSessionTicketProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.statelessResumption) {
                return null;
            }
            SSLSessionContextImpl cache = (SSLSessionContextImpl)shc.sslContext.engineGetServerSessionContext();
            if (cache.statelessEnabled()) {
                return new byte[0];
            }
            shc.statelessResumption = false;
            return null;
        }
    }

    private static final class T12SHSessionTicketConsumer
    implements SSLExtension.ExtensionConsumer {
        T12SHSessionTicketConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.SH_SESSION_TICKET)) {
                chc.statelessResumption = false;
                return;
            }
            if (!((SSLSessionContextImpl)chc.sslContext.engineGetClientSessionContext()).statelessEnabled()) {
                chc.statelessResumption = false;
                return;
            }
            SessionTicketSpec spec = new SessionTicketSpec((HandshakeContext)chc, buffer);
            chc.statelessResumption = true;
        }
    }

    static final class SessionTicketStringizer
    implements SSLStringizer {
        SessionTicketStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new SessionTicketSpec(hc, buffer).toString();
            }
            catch (IOException e) {
                return e.getMessage();
            }
        }
    }

    static final class SessionTicketSpec
    implements SSLExtension.SSLExtensionSpec {
        private static final int GCM_TAG_LEN = 128;
        ByteBuffer data;
        static final ByteBuffer zero = ByteBuffer.wrap(new byte[0]);

        SessionTicketSpec() {
            this.data = zero;
        }

        SessionTicketSpec(HandshakeContext hc, byte[] b) throws IOException {
            this(hc, ByteBuffer.wrap(b));
        }

        SessionTicketSpec(HandshakeContext hc, ByteBuffer buf) throws IOException {
            if (buf == null) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("SessionTicket buffer too small"));
            }
            if (buf.remaining() > 65536) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("SessionTicket buffer too large. " + buf.remaining()));
            }
            this.data = buf;
        }

        public byte[] encrypt(HandshakeContext hc, SSLSessionImpl session) {
            if (!hc.statelessResumption || !hc.handshakeSession.isStatelessable()) {
                return new byte[0];
            }
            try {
                StatelessKey key = KeyState.getCurrentKey(hc);
                byte[] iv = new byte[16];
                SecureRandom random = hc.sslContext.getSecureRandom();
                random.nextBytes(iv);
                Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
                c.init(1, (Key)key.key, new GCMParameterSpec(128, iv));
                c.updateAAD(new byte[]{(byte)(key.num >>> 24), (byte)(key.num >>> 16), (byte)(key.num >>> 8), (byte)key.num});
                byte[] data = session.write();
                if (data.length == 0) {
                    return data;
                }
                byte[] encrypted = c.doFinal(data);
                byte[] result = new byte[encrypted.length + 4 + iv.length];
                result[0] = (byte)(key.num >>> 24);
                result[1] = (byte)(key.num >>> 16);
                result[2] = (byte)(key.num >>> 8);
                result[3] = (byte)key.num;
                System.arraycopy(iv, 0, result, 4, iv.length);
                System.arraycopy(encrypted, 0, result, 4 + iv.length, encrypted.length);
                return result;
            }
            catch (Exception e) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Encryption failed." + e, new Object[0]);
                }
                return new byte[0];
            }
        }

        ByteBuffer decrypt(HandshakeContext hc) {
            try {
                int keyID = this.data.getInt();
                StatelessKey key = KeyState.getKey(hc, keyID);
                if (key == null) {
                    return null;
                }
                byte[] iv = new byte[16];
                this.data.get(iv);
                Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
                c.init(2, (Key)key.key, new GCMParameterSpec(128, iv));
                c.updateAAD(new byte[]{(byte)(keyID >>> 24), (byte)(keyID >>> 16), (byte)(keyID >>> 8), (byte)keyID});
                ByteBuffer out = ByteBuffer.allocate(this.data.remaining() - 16);
                c.doFinal(this.data, out);
                out.flip();
                return out;
            }
            catch (Exception e) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Decryption failed." + e.getMessage(), new Object[0]);
                }
                return null;
            }
        }

        byte[] getEncoded() {
            byte[] out = new byte[this.data.capacity()];
            this.data.duplicate().get(out);
            return out;
        }

        public String toString() {
            if (this.data == null) {
                return "<null>";
            }
            if (this.data.capacity() == 0) {
                return "<empty>";
            }
            MessageFormat messageFormat = new MessageFormat("  \"ticket\" : '{'\n{0}\n  '}'", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            Object[] messageFields = new Object[]{Utilities.indent(hexEncoder.encode(this.data.duplicate()), "    ")};
            return messageFormat.format(messageFields);
        }
    }

    private static final class KeyState {
        private KeyState() {
        }

        static StatelessKey getKey(HandshakeContext hc, int num) {
            SSLSessionContextImpl serverCache = (SSLSessionContextImpl)hc.sslContext.engineGetServerSessionContext();
            StatelessKey ssk = serverCache.getKey(num);
            if (ssk == null || ssk.isInvalid(serverCache)) {
                return null;
            }
            return ssk;
        }

        static StatelessKey getCurrentKey(HandshakeContext hc) {
            SSLSessionContextImpl serverCache = (SSLSessionContextImpl)hc.sslContext.engineGetServerSessionContext();
            return serverCache.getKey(hc);
        }
    }

    static final class StatelessKey {
        final long timeout;
        final SecretKey key;
        final int num;

        StatelessKey(HandshakeContext hc, int num) {
            SecretKey k = null;
            try {
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                kg.init(256, hc.sslContext.getSecureRandom());
                k = kg.generateKey();
            }
            catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                // empty catch block
            }
            this.key = k;
            this.timeout = System.currentTimeMillis() + (long)keyTimeout;
            this.num = num;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > this.timeout;
        }

        boolean isInvalid(SSLSessionContext sslSessionContext) {
            int sessionTimeout = sslSessionContext.getSessionTimeout() * 1000;
            return System.currentTimeMillis() > this.timeout + (long)sessionTimeout;
        }
    }
}

