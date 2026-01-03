/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLHandshakeException;
import sun.security.provider.certpath.OCSPResponse;
import sun.security.ssl.Alert;
import sun.security.ssl.CertStatusExtension;
import sun.security.ssl.CertificateMessage;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.Record;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.StatusResponseManager;
import sun.security.ssl.Utilities;

final class CertificateStatus {
    static final SSLConsumer handshakeConsumer = new CertificateStatusConsumer();
    static final HandshakeProducer handshakeProducer = new CertificateStatusProducer();
    static final HandshakeAbsence handshakeAbsence = new CertificateStatusAbsence();

    CertificateStatus() {
    }

    private static final class CertificateStatusConsumer
    implements SSLConsumer {
        private CertificateStatusConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            CertificateStatusMessage cst = new CertificateStatusMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming server CertificateStatus handshake message", cst);
            }
            chc.handshakeSession.setStatusResponses(cst.encodedResponses);
            CertificateMessage.T12CertificateConsumer.checkServerCerts(chc, chc.deferredCerts);
            chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_STATUS.id);
        }
    }

    private static final class CertificateStatusProducer
    implements HandshakeProducer {
        private CertificateStatusProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.staplingActive) {
                return null;
            }
            CertificateStatusMessage csm = new CertificateStatusMessage(shc);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced server CertificateStatus handshake message", csm);
            }
            csm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            return null;
        }
    }

    private static final class CertificateStatusAbsence
    implements HandshakeAbsence {
        private CertificateStatusAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (chc.staplingActive) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Server did not send CertificateStatus, checking cert chain without status info.", new Object[0]);
                }
                CertificateMessage.T12CertificateConsumer.checkServerCerts(chc, chc.deferredCerts);
            }
        }
    }

    static final class CertificateStatusMessage
    extends SSLHandshake.HandshakeMessage {
        final CertStatusExtension.CertStatusRequestType statusType;
        final int encodedResponsesLen;
        final int messageLength;
        final List<byte[]> encodedResponses = new ArrayList<byte[]>();

        CertificateStatusMessage(HandshakeContext handshakeContext) {
            super(handshakeContext);
            ServerHandshakeContext shc = (ServerHandshakeContext)handshakeContext;
            StatusResponseManager.StaplingParameters stapleParams = shc.stapleParams;
            if (stapleParams == null) {
                throw new IllegalArgumentException("Unexpected null stapling parameters");
            }
            X509Certificate[] certChain = (X509Certificate[])shc.handshakeSession.getLocalCertificates();
            if (certChain == null) {
                throw new IllegalArgumentException("Unexpected null certificate chain");
            }
            this.statusType = stapleParams.statReqType;
            int encodedLen = 0;
            if (this.statusType == CertStatusExtension.CertStatusRequestType.OCSP) {
                byte[] resp = stapleParams.responseMap.get(certChain[0]);
                if (resp == null) {
                    resp = new byte[]{};
                }
                this.encodedResponses.add(resp);
                encodedLen += resp.length + 3;
            } else if (this.statusType == CertStatusExtension.CertStatusRequestType.OCSP_MULTI) {
                for (X509Certificate cert : certChain) {
                    byte[] resp = stapleParams.responseMap.get(cert);
                    if (resp == null) {
                        resp = new byte[]{};
                    }
                    this.encodedResponses.add(resp);
                    encodedLen += resp.length + 3;
                }
            } else {
                throw new IllegalArgumentException("Unsupported StatusResponseType: " + (Object)((Object)this.statusType));
            }
            this.encodedResponsesLen = encodedLen;
            this.messageLength = CertificateStatusMessage.messageLength(this.statusType, this.encodedResponsesLen);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        CertificateStatusMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            this.statusType = CertStatusExtension.CertStatusRequestType.valueOf((byte)Record.getInt8(m));
            if (this.statusType == CertStatusExtension.CertStatusRequestType.OCSP) {
                byte[] respDER = Record.getBytes24(m);
                if (respDER.length <= 0) throw handshakeContext.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Zero-length OCSP Response");
                this.encodedResponses.add(respDER);
                this.encodedResponsesLen = 3 + respDER.length;
            } else {
                int respListLen;
                if (this.statusType != CertStatusExtension.CertStatusRequestType.OCSP_MULTI) throw handshakeContext.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Unsupported StatusResponseType: " + (Object)((Object)this.statusType));
                this.encodedResponsesLen = respListLen = Record.getInt24(m);
                while (respListLen > 0) {
                    byte[] respDER = Record.getBytes24(m);
                    this.encodedResponses.add(respDER);
                    respListLen -= respDER.length + 3;
                }
                if (respListLen != 0) {
                    throw handshakeContext.conContext.fatal(Alert.INTERNAL_ERROR, "Bad OCSP response list length");
                }
            }
            this.messageLength = CertificateStatusMessage.messageLength(this.statusType, this.encodedResponsesLen);
        }

        private static int messageLength(CertStatusExtension.CertStatusRequestType statusType, int encodedResponsesLen) {
            if (statusType == CertStatusExtension.CertStatusRequestType.OCSP) {
                return 1 + encodedResponsesLen;
            }
            if (statusType == CertStatusExtension.CertStatusRequestType.OCSP_MULTI) {
                return 4 + encodedResponsesLen;
            }
            return -1;
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.CERTIFICATE_STATUS;
        }

        @Override
        public int messageLength() {
            return this.messageLength;
        }

        @Override
        public void send(HandshakeOutStream s) throws IOException {
            s.putInt8(this.statusType.id);
            if (this.statusType == CertStatusExtension.CertStatusRequestType.OCSP) {
                s.putBytes24(this.encodedResponses.get(0));
            } else if (this.statusType == CertStatusExtension.CertStatusRequestType.OCSP_MULTI) {
                s.putInt24(this.encodedResponsesLen);
                for (byte[] respBytes : this.encodedResponses) {
                    s.putBytes24(respBytes);
                }
            } else {
                throw new SSLHandshakeException("Unsupported status_type: " + this.statusType.id);
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (byte[] respDER : this.encodedResponses) {
                if (respDER.length > 0) {
                    try {
                        OCSPResponse oResp = new OCSPResponse(respDER);
                        sb.append(oResp.toString()).append("\n");
                    }
                    catch (IOException ioe) {
                        sb.append("OCSP Response Exception: ").append(ioe).append("\n");
                    }
                    continue;
                }
                sb.append("<Zero-length entry>\n");
            }
            MessageFormat messageFormat = new MessageFormat("\"CertificateStatus\": '{'\n  \"type\"                : \"{0}\",\n  \"responses \"          : [\n{1}\n  ]\n'}'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{this.statusType.name, Utilities.indent(Utilities.indent(sb.toString()))};
            return messageFormat.format(messageFields);
        }
    }
}

