/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Locale;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLHandshakeException;
import sun.security.ssl.Alert;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HKDF;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.PskKeyExchangeModesExtension;
import sun.security.ssl.Record;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLExtensions;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLProducer;
import sun.security.ssl.SSLSecretDerivation;
import sun.security.ssl.SSLSessionContextImpl;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SessionId;
import sun.security.ssl.SessionTicketExtension;
import sun.security.ssl.Utilities;
import sun.security.util.HexDumpEncoder;

final class NewSessionTicket {
    static final int MAX_TICKET_LIFETIME = 604800;
    static final SSLConsumer handshakeConsumer = new T13NewSessionTicketConsumer();
    static final SSLConsumer handshake12Consumer = new T12NewSessionTicketConsumer();
    static final SSLProducer t13PosthandshakeProducer = new T13NewSessionTicketProducer();
    static final HandshakeProducer handshake12Producer = new T12NewSessionTicketProducer();

    NewSessionTicket() {
    }

    private static SecretKey derivePreSharedKey(CipherSuite.HashAlg hashAlg, SecretKey resumptionMasterSecret, byte[] nonce) throws IOException {
        try {
            HKDF hkdf = new HKDF(hashAlg.name);
            byte[] hkdfInfo = SSLSecretDerivation.createHkdfInfo("tls13 resumption".getBytes(), nonce, hashAlg.hashLength);
            return hkdf.expand(resumptionMasterSecret, hkdfInfo, hashAlg.hashLength, "TlsPreSharedKey");
        }
        catch (GeneralSecurityException gse) {
            throw new SSLHandshakeException("Could not derive PSK", gse);
        }
    }

    private static final class T13NewSessionTicketConsumer
    implements SSLConsumer {
        private T13NewSessionTicketConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            HandshakeContext hc = (HandshakeContext)context;
            T13NewSessionTicketMessage nstm = new T13NewSessionTicketMessage(hc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming NewSessionTicket message", nstm);
            }
            SSLSessionContextImpl sessionCache = (SSLSessionContextImpl)hc.sslContext.engineGetClientSessionContext();
            if (nstm.ticketLifetime <= 0 || nstm.ticketLifetime > 604800) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Discarding NewSessionTicket with lifetime " + nstm.ticketLifetime, nstm);
                }
                sessionCache.remove(hc.handshakeSession.getSessionId());
                return;
            }
            if (sessionCache.getSessionTimeout() > 604800) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Session cache lifetime is too long. Discarding ticket.", new Object[0]);
                }
                return;
            }
            SSLSessionImpl sessionToSave = hc.conContext.conSession;
            SecretKey resumptionMasterSecret = sessionToSave.getResumptionMasterSecret();
            if (resumptionMasterSecret == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Session has no resumption master secret. Ignoring ticket.", new Object[0]);
                }
                return;
            }
            SecretKey psk = NewSessionTicket.derivePreSharedKey(sessionToSave.getSuite().hashAlg, resumptionMasterSecret, ((NewSessionTicketMessage)nstm).getTicketNonce());
            SessionId newId = new SessionId(true, hc.sslContext.getSecureRandom());
            SSLSessionImpl sessionCopy = new SSLSessionImpl(sessionToSave, newId);
            sessionToSave.addChild(sessionCopy);
            sessionCopy.setPreSharedKey(psk);
            sessionCopy.setTicketAgeAdd(((NewSessionTicketMessage)nstm).getTicketAgeAdd());
            sessionCopy.setPskIdentity(nstm.ticket);
            sessionCache.put(sessionCopy);
            hc.conContext.finishPostHandshake();
        }
    }

    private static final class T12NewSessionTicketConsumer
    implements SSLConsumer {
        private T12NewSessionTicketConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            HandshakeContext hc = (HandshakeContext)context;
            hc.handshakeConsumers.remove(SSLHandshake.NEW_SESSION_TICKET.id);
            T12NewSessionTicketMessage nstm = new T12NewSessionTicketMessage(hc, message);
            if (nstm.ticket.length == 0) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("NewSessionTicket ticket was empty", new Object[0]);
                }
                return;
            }
            if (nstm.ticketLifetime <= 0 || nstm.ticketLifetime > 604800) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Discarding NewSessionTicket with lifetime " + nstm.ticketLifetime, nstm);
                }
                return;
            }
            SSLSessionContextImpl sessionCache = (SSLSessionContextImpl)hc.sslContext.engineGetClientSessionContext();
            if (sessionCache.getSessionTimeout() > 604800) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Session cache lifetime is too long. Discarding ticket.", new Object[0]);
                }
                return;
            }
            hc.handshakeSession.setPskIdentity(nstm.ticket);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming NewSessionTicket\n" + ((Object)nstm).toString(), new Object[0]);
            }
        }
    }

    private static final class T13NewSessionTicketProducer
    implements SSLProducer {
        private T13NewSessionTicketProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context) throws IOException {
            HandshakeContext hc = (HandshakeContext)context;
            if (hc.conContext.hasDelegatedFinished) {
                hc.conContext.hasDelegatedFinished = false;
                hc.conContext.needHandshakeFinishedStatus = true;
            }
            if (hc instanceof ServerHandshakeContext) {
                if (!hc.handshakeSession.isRejoinable()) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("No session ticket produced: session is not resumable", new Object[0]);
                    }
                    return null;
                }
                PskKeyExchangeModesExtension.PskKeyExchangeModesSpec pkemSpec = (PskKeyExchangeModesExtension.PskKeyExchangeModesSpec)hc.handshakeExtensions.get(SSLExtension.PSK_KEY_EXCHANGE_MODES);
                if (pkemSpec == null || !pkemSpec.contains(PskKeyExchangeModesExtension.PskKeyExchangeMode.PSK_DHE_KE)) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("No session ticket produced: client does not support psk_dhe_ke", new Object[0]);
                    }
                    return null;
                }
            } else if (!hc.handshakeSession.isPSKable()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("No session ticket produced: No session ticket allowed in this session", new Object[0]);
                }
                return null;
            }
            SSLSessionContextImpl sessionCache = (SSLSessionContextImpl)hc.sslContext.engineGetServerSessionContext();
            SessionId newId = new SessionId(true, hc.sslContext.getSecureRandom());
            SecretKey resumptionMasterSecret = hc.handshakeSession.getResumptionMasterSecret();
            if (resumptionMasterSecret == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("No session ticket produced: no resumption secret", new Object[0]);
                }
                return null;
            }
            BigInteger nonce = hc.handshakeSession.incrTicketNonceCounter();
            byte[] nonceArr = nonce.toByteArray();
            SecretKey psk = NewSessionTicket.derivePreSharedKey(hc.negotiatedCipherSuite.hashAlg, resumptionMasterSecret, nonceArr);
            int sessionTimeoutSeconds = sessionCache.getSessionTimeout();
            if (sessionTimeoutSeconds > 604800) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("No session ticket produced: session timeout", new Object[0]);
                }
                return null;
            }
            SSLHandshake.HandshakeMessage nstm = null;
            SSLSessionImpl sessionCopy = new SSLSessionImpl(hc.handshakeSession, newId);
            sessionCopy.setPreSharedKey(psk);
            sessionCopy.setPskIdentity(newId.getId());
            if (hc.statelessResumption && hc.handshakeSession.isStatelessable()) {
                nstm = new T13NewSessionTicketMessage(hc, sessionTimeoutSeconds, hc.sslContext.getSecureRandom(), nonceArr, new SessionTicketExtension.SessionTicketSpec().encrypt(hc, sessionCopy));
                if (!((NewSessionTicketMessage)nstm).isValid()) {
                    hc.statelessResumption = false;
                } else if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Produced NewSessionTicket stateless post-handshake message", nstm);
                }
            }
            if (!hc.statelessResumption || !hc.handshakeSession.isStatelessable()) {
                nstm = new T13NewSessionTicketMessage(hc, sessionTimeoutSeconds, hc.sslContext.getSecureRandom(), nonceArr, newId.getId());
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Produced NewSessionTicket post-handshake message", nstm);
                }
                hc.handshakeSession.addChild(sessionCopy);
                sessionCopy.setTicketAgeAdd(((NewSessionTicketMessage)nstm).getTicketAgeAdd());
                sessionCache.put(sessionCopy);
            }
            if (nstm != null) {
                nstm.write(hc.handshakeOutput);
                hc.handshakeOutput.flush();
                if (hc.conContext.needHandshakeFinishedStatus) {
                    hc.conContext.needHandshakeFinishedStatus = false;
                }
            }
            hc.conContext.finishPostHandshake();
            return null;
        }
    }

    private static final class T12NewSessionTicketProducer
    implements HandshakeProducer {
        private T12NewSessionTicketProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.handshakeSession.isRejoinable()) {
                return null;
            }
            SessionId newId = shc.handshakeSession.getSessionId();
            SSLSessionContextImpl sessionCache = (SSLSessionContextImpl)shc.sslContext.engineGetServerSessionContext();
            int sessionTimeoutSeconds = sessionCache.getSessionTimeout();
            if (sessionTimeoutSeconds > 604800) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Session timeout is too long. No ticket sent.", new Object[0]);
                }
                return null;
            }
            SSLSessionImpl sessionCopy = new SSLSessionImpl(shc.handshakeSession, newId);
            sessionCopy.setPskIdentity(newId.getId());
            T12NewSessionTicketMessage nstm = new T12NewSessionTicketMessage(shc, sessionTimeoutSeconds, new SessionTicketExtension.SessionTicketSpec().encrypt(shc, sessionCopy));
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced NewSessionTicket stateless handshake message", nstm);
            }
            nstm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            return null;
        }
    }

    static final class T13NewSessionTicketMessage
    extends NewSessionTicketMessage {
        int ticketAgeAdd;
        byte[] ticketNonce;
        SSLExtensions extensions;

        T13NewSessionTicketMessage(HandshakeContext context, int ticketLifetime, SecureRandom generator, byte[] ticketNonce, byte[] ticket) {
            super(context);
            this.ticketLifetime = ticketLifetime;
            this.ticketAgeAdd = generator.nextInt();
            this.ticketNonce = ticketNonce;
            this.ticket = ticket;
            this.extensions = new SSLExtensions(this);
        }

        T13NewSessionTicketMessage(HandshakeContext context, ByteBuffer m) throws IOException {
            super(context);
            if (m.remaining() < 14) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid NewSessionTicket message: insufficient data");
            }
            this.ticketLifetime = Record.getInt32(m);
            this.ticketAgeAdd = Record.getInt32(m);
            this.ticketNonce = Record.getBytes8(m);
            if (m.remaining() < 5) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid NewSessionTicket message: insufficient ticket data");
            }
            this.ticket = Record.getBytes16(m);
            if (this.ticket.length == 0 && SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("No ticket in the NewSessionTicket handshake message", new Object[0]);
            }
            if (m.remaining() < 2) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid NewSessionTicket message: extra data");
            }
            SSLExtension[] supportedExtensions = context.sslConfig.getEnabledExtensions(SSLHandshake.NEW_SESSION_TICKET);
            this.extensions = new SSLExtensions(this, m, supportedExtensions);
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.NEW_SESSION_TICKET;
        }

        @Override
        int getTicketAgeAdd() {
            return this.ticketAgeAdd;
        }

        @Override
        byte[] getTicketNonce() {
            return this.ticketNonce;
        }

        @Override
        public int messageLength() {
            int extLen = this.extensions.length();
            if (extLen == 0) {
                extLen = 2;
            }
            return 9 + this.ticketNonce.length + 2 + this.ticket.length + extLen;
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            hos.putInt32(this.ticketLifetime);
            hos.putInt32(this.ticketAgeAdd);
            hos.putBytes8(this.ticketNonce);
            hos.putBytes16(this.ticket);
            if (this.extensions.length() == 0) {
                hos.putInt16(0);
            } else {
                this.extensions.send(hos);
            }
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"NewSessionTicket\": '{'\n  \"ticket_lifetime\"      : \"{0}\",\n  \"ticket_age_add\"       : \"{1}\",\n  \"ticket_nonce\"         : \"{2}\",\n  \"ticket\"               : '{'\n{3}\n  '}'  \"extensions\"           : [\n{4}\n  ]\n'}'", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            Object[] messageFields = new Object[]{this.ticketLifetime, "<omitted>", Utilities.toHexString(this.ticketNonce), Utilities.indent(hexEncoder.encode(this.ticket), "    "), Utilities.indent(this.extensions.toString(), "    ")};
            return messageFormat.format(messageFields);
        }
    }

    static final class T12NewSessionTicketMessage
    extends NewSessionTicketMessage {
        T12NewSessionTicketMessage(HandshakeContext context, int ticketLifetime, byte[] ticket) {
            super(context);
            this.ticketLifetime = ticketLifetime;
            this.ticket = ticket;
        }

        T12NewSessionTicketMessage(HandshakeContext context, ByteBuffer m) throws IOException {
            super(context);
            if (m.remaining() < 6) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid NewSessionTicket message: insufficient data");
            }
            this.ticketLifetime = Record.getInt32(m);
            this.ticket = Record.getBytes16(m);
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.NEW_SESSION_TICKET;
        }

        @Override
        public int messageLength() {
            return 6 + this.ticket.length;
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            hos.putInt32(this.ticketLifetime);
            hos.putBytes16(this.ticket);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"NewSessionTicket\": '{'\n  \"ticket_lifetime\"      : \"{0}\",\n  \"ticket\"               : '{'\n{1}\n  '}''}'", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            Object[] messageFields = new Object[]{this.ticketLifetime, Utilities.indent(hexEncoder.encode(this.ticket), "    ")};
            return messageFormat.format(messageFields);
        }
    }

    static abstract class NewSessionTicketMessage
    extends SSLHandshake.HandshakeMessage {
        int ticketLifetime;
        byte[] ticket = new byte[0];

        NewSessionTicketMessage(HandshakeContext context) {
            super(context);
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.NEW_SESSION_TICKET;
        }

        int getTicketAgeAdd() throws IOException {
            throw this.handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "TicketAgeAdd not part of RFC 5077.");
        }

        byte[] getTicketNonce() throws IOException {
            throw this.handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "TicketNonce not part of RFC 5077.");
        }

        boolean isValid() {
            return this.ticket.length > 0;
        }
    }
}

