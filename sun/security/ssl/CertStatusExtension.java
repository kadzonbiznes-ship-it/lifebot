/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.Extension;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLProtocolException;
import sun.security.provider.certpath.OCSPResponse;
import sun.security.provider.certpath.ResponderId;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.Utilities;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;

final class CertStatusExtension {
    static final HandshakeProducer chNetworkProducer = new CHCertStatusReqProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHCertStatusReqConsumer();
    static final HandshakeProducer shNetworkProducer = new SHCertStatusReqProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHCertStatusReqConsumer();
    static final HandshakeProducer ctNetworkProducer = new CTCertStatusResponseProducer();
    static final SSLExtension.ExtensionConsumer ctOnLoadConsumer = new CTCertStatusResponseConsumer();
    static final SSLStringizer certStatusReqStringizer = new CertStatusRequestStringizer();
    static final HandshakeProducer chV2NetworkProducer = new CHCertStatusReqV2Producer();
    static final SSLExtension.ExtensionConsumer chV2OnLoadConsumer = new CHCertStatusReqV2Consumer();
    static final HandshakeProducer shV2NetworkProducer = new SHCertStatusReqV2Producer();
    static final SSLExtension.ExtensionConsumer shV2OnLoadConsumer = new SHCertStatusReqV2Consumer();
    static final SSLStringizer certStatusReqV2Stringizer = new CertStatusRequestsStringizer();
    static final SSLStringizer certStatusRespStringizer = new CertStatusRespStringizer();

    CertStatusExtension() {
    }

    private static final class CHCertStatusReqProducer
    implements HandshakeProducer {
        private CHCertStatusReqProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslContext.isStaplingEnabled(true)) {
                return null;
            }
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_STATUS_REQUEST)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_STATUS_REQUEST.name, new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{1, 0, 0, 0, 0};
            chc.handshakeExtensions.put(SSLExtension.CH_STATUS_REQUEST, CertStatusRequestSpec.DEFAULT);
            return extData;
        }
    }

    private static final class CHCertStatusReqConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHCertStatusReqConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_STATUS_REQUEST)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_STATUS_REQUEST.name, new Object[0]);
                }
                return;
            }
            CertStatusRequestSpec spec = new CertStatusRequestSpec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.CH_STATUS_REQUEST, spec);
            if (!shc.isResumption && !shc.negotiatedProtocol.useTLS13PlusSpec()) {
                shc.handshakeProducers.put(SSLHandshake.CERTIFICATE_STATUS.id, SSLHandshake.CERTIFICATE_STATUS);
            }
        }
    }

    private static final class SHCertStatusReqProducer
    implements HandshakeProducer {
        private SHCertStatusReqProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.stapleParams == null || shc.stapleParams.statusRespExt != SSLExtension.CH_STATUS_REQUEST) {
                return null;
            }
            CertStatusRequestSpec spec = (CertStatusRequestSpec)shc.handshakeExtensions.get(SSLExtension.CH_STATUS_REQUEST);
            if (spec == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable extension: " + SSLExtension.CH_STATUS_REQUEST.name, new Object[0]);
                }
                return null;
            }
            if (shc.isResumption) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("No status_request response for session resuming", new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{};
            shc.handshakeExtensions.put(SSLExtension.SH_STATUS_REQUEST, CertStatusRequestSpec.DEFAULT);
            return extData;
        }
    }

    private static final class SHCertStatusReqConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHCertStatusReqConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            CertStatusRequestSpec requestedCsr = (CertStatusRequestSpec)chc.handshakeExtensions.get(SSLExtension.CH_STATUS_REQUEST);
            if (requestedCsr == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected status_request extension in ServerHello");
            }
            if (buffer.hasRemaining()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid status_request extension in ServerHello message: the extension data must be empty");
            }
            chc.handshakeExtensions.put(SSLExtension.SH_STATUS_REQUEST, CertStatusRequestSpec.DEFAULT);
            chc.staplingActive = chc.sslContext.isStaplingEnabled(true);
            if (chc.staplingActive) {
                chc.handshakeConsumers.put(SSLHandshake.CERTIFICATE_STATUS.id, SSLHandshake.CERTIFICATE_STATUS);
            }
        }
    }

    private static final class CTCertStatusResponseProducer
    implements HandshakeProducer {
        private CTCertStatusResponseProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            byte[] producedData;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.stapleParams == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Stapling is disabled for this connection", new Object[0]);
                }
                return null;
            }
            if (shc.currentCertEntry == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Found null CertificateEntry in context", new Object[0]);
                }
                return null;
            }
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate x509Cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(shc.currentCertEntry.encoded));
                byte[] respBytes = shc.stapleParams.responseMap.get(x509Cert);
                if (respBytes == null) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("No status response found for " + x509Cert.getSubjectX500Principal(), new Object[0]);
                    }
                    shc.currentCertEntry = null;
                    return null;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                    SSLLogger.finest("Found status response for " + x509Cert.getSubjectX500Principal() + ", response length: " + respBytes.length, new Object[0]);
                }
                CertStatusResponse certResp = shc.stapleParams.statReqType == CertStatusRequestType.OCSP ? new OCSPStatusResponse(shc.stapleParams.statReqType.id, respBytes) : new CertStatusResponse(shc.stapleParams.statReqType.id, respBytes);
                producedData = certResp.toByteArray();
            }
            catch (CertificateException ce) {
                throw shc.conContext.fatal(Alert.BAD_CERTIFICATE, "Failed to parse server certificates", ce);
            }
            catch (IOException ioe) {
                throw shc.conContext.fatal(Alert.BAD_CERT_STATUS_RESPONSE, "Failed to parse certificate status response", ioe);
            }
            shc.currentCertEntry = null;
            return producedData;
        }
    }

    private static final class CTCertStatusResponseConsumer
    implements SSLExtension.ExtensionConsumer {
        private CTCertStatusResponseConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            CertStatusResponseSpec spec = new CertStatusResponseSpec(chc, buffer);
            if (!chc.sslContext.isStaplingEnabled(true)) {
                return;
            }
            chc.staplingActive = true;
            if (chc.handshakeSession != null && !chc.isResumption) {
                ArrayList<byte[]> respList = new ArrayList<byte[]>(chc.handshakeSession.getStatusResponses());
                respList.add(spec.statusResponse.encodedResponse);
                chc.handshakeSession.setStatusResponses(respList);
            } else if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                SSLLogger.finest("Ignoring stapled data on resumed session", new Object[0]);
            }
        }
    }

    private static final class CertStatusRequestStringizer
    implements SSLStringizer {
        private CertStatusRequestStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new CertStatusRequestSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class CHCertStatusReqV2Producer
    implements HandshakeProducer {
        private CHCertStatusReqV2Producer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslContext.isStaplingEnabled(true)) {
                return null;
            }
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_STATUS_REQUEST_V2)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable status_request_v2 extension", new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{0, 7, 2, 0, 4, 0, 0, 0, 0};
            chc.handshakeExtensions.put(SSLExtension.CH_STATUS_REQUEST_V2, CertStatusRequestV2Spec.DEFAULT);
            return extData;
        }
    }

    private static final class CHCertStatusReqV2Consumer
    implements SSLExtension.ExtensionConsumer {
        private CHCertStatusReqV2Consumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_STATUS_REQUEST_V2)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable status_request_v2 extension", new Object[0]);
                }
                return;
            }
            CertStatusRequestV2Spec spec = new CertStatusRequestV2Spec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.CH_STATUS_REQUEST_V2, spec);
            if (!shc.isResumption) {
                shc.handshakeProducers.putIfAbsent(SSLHandshake.CERTIFICATE_STATUS.id, SSLHandshake.CERTIFICATE_STATUS);
            }
        }
    }

    private static final class SHCertStatusReqV2Producer
    implements HandshakeProducer {
        private SHCertStatusReqV2Producer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.stapleParams == null || shc.stapleParams.statusRespExt != SSLExtension.CH_STATUS_REQUEST_V2) {
                return null;
            }
            CertStatusRequestV2Spec spec = (CertStatusRequestV2Spec)shc.handshakeExtensions.get(SSLExtension.CH_STATUS_REQUEST_V2);
            if (spec == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable status_request_v2 extension", new Object[0]);
                }
                return null;
            }
            if (shc.isResumption) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("No status_request_v2 response for session resumption", new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{};
            shc.handshakeExtensions.put(SSLExtension.SH_STATUS_REQUEST_V2, CertStatusRequestV2Spec.DEFAULT);
            return extData;
        }
    }

    private static final class SHCertStatusReqV2Consumer
    implements SSLExtension.ExtensionConsumer {
        private SHCertStatusReqV2Consumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            CertStatusRequestV2Spec requestedCsr = (CertStatusRequestV2Spec)chc.handshakeExtensions.get(SSLExtension.CH_STATUS_REQUEST_V2);
            if (requestedCsr == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected status_request_v2 extension in ServerHello");
            }
            if (buffer.hasRemaining()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid status_request_v2 extension in ServerHello: the extension data must be empty");
            }
            chc.handshakeExtensions.put(SSLExtension.SH_STATUS_REQUEST_V2, CertStatusRequestV2Spec.DEFAULT);
            chc.staplingActive = chc.sslContext.isStaplingEnabled(true);
            if (chc.staplingActive) {
                chc.handshakeConsumers.put(SSLHandshake.CERTIFICATE_STATUS.id, SSLHandshake.CERTIFICATE_STATUS);
            }
        }
    }

    private static final class CertStatusRequestsStringizer
    implements SSLStringizer {
        private CertStatusRequestsStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new CertStatusRequestV2Spec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class CertStatusRespStringizer
    implements SSLStringizer {
        private CertStatusRespStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new CertStatusResponseSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static final class CertStatusRequestV2Spec
    implements SSLExtension.SSLExtensionSpec {
        static final CertStatusRequestV2Spec DEFAULT = new CertStatusRequestV2Spec(new CertStatusRequest[]{OCSPStatusRequest.EMPTY_OCSP_MULTI});
        final CertStatusRequest[] certStatusRequests;

        private CertStatusRequestV2Spec(CertStatusRequest[] certStatusRequests) {
            this.certStatusRequests = certStatusRequests;
        }

        private CertStatusRequestV2Spec(HandshakeContext hc, ByteBuffer message) throws IOException {
            if (message.remaining() == 0) {
                this.certStatusRequests = new CertStatusRequest[0];
                return;
            }
            if (message.remaining() < 5) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid status_request_v2 extension: insufficient data"));
            }
            int listLen = Record.getInt16(message);
            if (listLen <= 0) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("certificate_status_req_list length must be positive (received length: " + listLen + ")"));
            }
            int remaining = listLen;
            ArrayList<CertStatusRequest> statusRequests = new ArrayList<CertStatusRequest>();
            while (remaining > 0) {
                byte statusType = (byte)Record.getInt8(message);
                int requestLen = Record.getInt16(message);
                if (message.remaining() < requestLen) {
                    throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid status_request_v2 extension: insufficient data (request_length=" + requestLen + ", remaining=" + message.remaining() + ")"));
                }
                byte[] encoded = new byte[requestLen];
                if (encoded.length != 0) {
                    message.get(encoded);
                }
                remaining -= 3;
                remaining -= requestLen;
                if (statusType == CertStatusRequestType.OCSP.id || statusType == CertStatusRequestType.OCSP_MULTI.id) {
                    if (encoded.length < 4) {
                        throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid status_request_v2 extension: insufficient data"));
                    }
                    statusRequests.add(new OCSPStatusRequest(statusType, encoded));
                    continue;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.info("Unknown certificate status request (status type: " + statusType + ")", new Object[0]);
                }
                statusRequests.add(new CertStatusRequest(statusType, encoded));
            }
            this.certStatusRequests = statusRequests.toArray(new CertStatusRequest[0]);
        }

        public String toString() {
            if (this.certStatusRequests == null || this.certStatusRequests.length == 0) {
                return "<empty>";
            }
            MessageFormat messageFormat = new MessageFormat("\"cert status request\": '{'\n{0}\n'}'", Locale.ENGLISH);
            StringBuilder builder = new StringBuilder(512);
            boolean isFirst = true;
            for (CertStatusRequest csr : this.certStatusRequests) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                Object[] messageFields = new Object[]{Utilities.indent(csr.toString())};
                builder.append(messageFormat.format(messageFields));
            }
            return builder.toString();
        }
    }

    static final class OCSPStatusResponse
    extends CertStatusResponse {
        final OCSPResponse ocspResponse;

        private OCSPStatusResponse(byte statusType, byte[] encoded) throws IOException {
            super(statusType, encoded);
            if (encoded == null || encoded.length < 1) {
                throw new SSLProtocolException("Invalid OCSP status response: insufficient data");
            }
            this.ocspResponse = new OCSPResponse(encoded);
        }

        @Override
        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"certificate status response type\": {0}\n\"OCSP status response\": '{'\n{1}\n'}'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{CertStatusRequestType.nameOf(this.statusType), Utilities.indent(this.ocspResponse.toString())};
            return messageFormat.format(messageFields);
        }
    }

    static class CertStatusResponse {
        final byte statusType;
        final byte[] encodedResponse;

        protected CertStatusResponse(byte statusType, byte[] respDer) {
            this.statusType = statusType;
            this.encodedResponse = respDer;
        }

        byte[] toByteArray() throws IOException {
            byte[] outData = new byte[this.encodedResponse.length + 4];
            ByteBuffer buf = ByteBuffer.wrap(outData);
            Record.putInt8(buf, this.statusType);
            Record.putBytes24(buf, this.encodedResponse);
            return buf.array();
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"certificate status response type\": {0}\n\"encoded certificate status\": '{'\n{1}\n'}'", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            String encoded = hexEncoder.encodeBuffer(this.encodedResponse);
            Object[] messageFields = new Object[]{CertStatusRequestType.nameOf(this.statusType), Utilities.indent(encoded)};
            return messageFormat.format(messageFields);
        }
    }

    static final class OCSPStatusRequest
    extends CertStatusRequest {
        static final OCSPStatusRequest EMPTY_OCSP;
        static final OCSPStatusRequest EMPTY_OCSP_MULTI;
        final List<ResponderId> responderIds;
        final List<Extension> extensions;

        private OCSPStatusRequest(byte statusType, byte[] encoded) throws IOException {
            super(statusType, encoded);
            int ridListBytesRemaining;
            byte[] ridBytes;
            if (encoded == null || encoded.length < 4) {
                throw new SSLProtocolException("Invalid OCSP status request: insufficient data");
            }
            ArrayList<ResponderId> rids = new ArrayList<ResponderId>();
            ArrayList<Extension> exts = new ArrayList<Extension>();
            ByteBuffer m = ByteBuffer.wrap(encoded);
            int ridListLen = Record.getInt16(m);
            if (m.remaining() < ridListLen + 2) {
                throw new SSLProtocolException("Invalid OCSP status request: insufficient data");
            }
            for (ridListBytesRemaining = ridListLen; ridListBytesRemaining >= 2; ridListBytesRemaining -= ridBytes.length + 2) {
                ridBytes = Record.getBytes16(m);
                try {
                    rids.add(new ResponderId(ridBytes));
                    continue;
                }
                catch (IOException ioe) {
                    throw new SSLProtocolException("Invalid OCSP status request: invalid responder ID");
                }
            }
            if (ridListBytesRemaining != 0) {
                throw new SSLProtocolException("Invalid OCSP status request: incomplete data");
            }
            byte[] extListBytes = Record.getBytes16(m);
            int extListLen = extListBytes.length;
            if (extListLen > 0) {
                try {
                    DerValue[] extSeqContents;
                    DerInputStream dis = new DerInputStream(extListBytes);
                    for (DerValue extDerVal : extSeqContents = dis.getSequence(extListBytes.length)) {
                        exts.add(new sun.security.x509.Extension(extDerVal));
                    }
                }
                catch (IOException ioe) {
                    throw new SSLProtocolException("Invalid OCSP status request: invalid extension");
                }
            }
            this.responderIds = rids;
            this.extensions = exts;
        }

        @Override
        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"certificate status type\": {0}\n\"OCSP status request\": '{'\n{1}\n'}'", Locale.ENGLISH);
            MessageFormat requestFormat = new MessageFormat("\"responder_id\": {0}\n\"request extensions\": '{'\n{1}\n'}'", Locale.ENGLISH);
            String ridStr = "<empty>";
            if (!this.responderIds.isEmpty()) {
                ridStr = this.responderIds.toString();
            }
            String extsStr = "<empty>";
            if (!this.extensions.isEmpty()) {
                StringBuilder extBuilder = new StringBuilder(512);
                boolean isFirst = true;
                for (Extension ext : this.extensions) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        extBuilder.append(",\n");
                    }
                    extBuilder.append("{\n").append(Utilities.indent(ext.toString())).append("}");
                }
                extsStr = extBuilder.toString();
            }
            Object[] requestFields = new Object[]{ridStr, Utilities.indent(extsStr)};
            String ocspStatusRequest = requestFormat.format(requestFields);
            Object[] messageFields = new Object[]{CertStatusRequestType.nameOf(this.statusType), Utilities.indent(ocspStatusRequest)};
            return messageFormat.format(messageFields);
        }

        static {
            OCSPStatusRequest ocspReq = null;
            OCSPStatusRequest multiReq = null;
            try {
                ocspReq = new OCSPStatusRequest(CertStatusRequestType.OCSP.id, new byte[]{0, 0, 0, 0});
                multiReq = new OCSPStatusRequest(CertStatusRequestType.OCSP_MULTI.id, new byte[]{0, 0, 0, 0});
            }
            catch (IOException iOException) {
                // empty catch block
            }
            EMPTY_OCSP = ocspReq;
            EMPTY_OCSP_MULTI = multiReq;
        }
    }

    static class CertStatusRequest {
        final byte statusType;
        final byte[] encodedRequest;

        protected CertStatusRequest(byte statusType, byte[] encodedRequest) {
            this.statusType = statusType;
            this.encodedRequest = encodedRequest;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"certificate status type\": {0}\n\"encoded certificate status\": '{'\n{1}\n'}'", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            String encoded = hexEncoder.encodeBuffer(this.encodedRequest);
            Object[] messageFields = new Object[]{CertStatusRequestType.nameOf(this.statusType), Utilities.indent(encoded)};
            return messageFormat.format(messageFields);
        }
    }

    static enum CertStatusRequestType {
        OCSP(1, "ocsp"),
        OCSP_MULTI(2, "ocsp_multi");

        final byte id;
        final String name;

        private CertStatusRequestType(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        static CertStatusRequestType valueOf(byte id) {
            for (CertStatusRequestType srt : CertStatusRequestType.values()) {
                if (srt.id != id) continue;
                return srt;
            }
            return null;
        }

        static String nameOf(byte id) {
            for (CertStatusRequestType srt : CertStatusRequestType.values()) {
                if (srt.id != id) continue;
                return srt.name;
            }
            return "UNDEFINED-CERT-STATUS-TYPE(" + id + ")";
        }
    }

    static final class CertStatusResponseSpec
    implements SSLExtension.SSLExtensionSpec {
        final CertStatusResponse statusResponse;

        private CertStatusResponseSpec(CertStatusResponse resp) {
            this.statusResponse = resp;
        }

        private CertStatusResponseSpec(HandshakeContext hc, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() < 2) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid status_request extension: insufficient data"));
            }
            byte type = (byte)Record.getInt8(buffer);
            byte[] respData = Record.getBytes24(buffer);
            if (type == CertStatusRequestType.OCSP.id) {
                this.statusResponse = new OCSPStatusResponse(type, respData);
            } else {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.info("Unknown certificate status response (status type: " + type + ")", new Object[0]);
                }
                this.statusResponse = new CertStatusResponse(type, respData);
            }
        }

        public String toString() {
            return this.statusResponse == null ? "<empty>" : this.statusResponse.toString();
        }
    }

    static final class CertStatusRequestSpec
    implements SSLExtension.SSLExtensionSpec {
        static final CertStatusRequestSpec DEFAULT = new CertStatusRequestSpec(OCSPStatusRequest.EMPTY_OCSP);
        final CertStatusRequest statusRequest;

        private CertStatusRequestSpec(CertStatusRequest statusRequest) {
            this.statusRequest = statusRequest;
        }

        private CertStatusRequestSpec(HandshakeContext hc, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() == 0) {
                this.statusRequest = null;
                return;
            }
            if (buffer.remaining() < 1) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid status_request extension: insufficient data"));
            }
            byte statusType = (byte)Record.getInt8(buffer);
            byte[] encoded = new byte[buffer.remaining()];
            if (encoded.length != 0) {
                buffer.get(encoded);
            }
            if (statusType == CertStatusRequestType.OCSP.id) {
                this.statusRequest = new OCSPStatusRequest(statusType, encoded);
            } else {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.info("Unknown certificate status request (status type: " + statusType + ")", new Object[0]);
                }
                this.statusRequest = new CertStatusRequest(statusType, encoded);
            }
        }

        public String toString() {
            return this.statusRequest == null ? "<empty>" : this.statusRequest.toString();
        }
    }
}

