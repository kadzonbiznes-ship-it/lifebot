/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;
import sun.security.ssl.Alert;
import sun.security.ssl.CertificateStatus;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.JsseJce;
import sun.security.ssl.Record;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLEngineImpl;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLExtensions;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLSocketImpl;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.Utilities;
import sun.security.ssl.X509Authentication;

final class CertificateRequest {
    static final SSLConsumer t10HandshakeConsumer = new T10CertificateRequestConsumer();
    static final HandshakeProducer t10HandshakeProducer = new T10CertificateRequestProducer();
    static final SSLConsumer t12HandshakeConsumer = new T12CertificateRequestConsumer();
    static final HandshakeProducer t12HandshakeProducer = new T12CertificateRequestProducer();
    static final SSLConsumer t13HandshakeConsumer = new T13CertificateRequestConsumer();
    static final HandshakeProducer t13HandshakeProducer = new T13CertificateRequestProducer();

    CertificateRequest() {
    }

    private static final class T10CertificateRequestConsumer
    implements SSLConsumer {
        private T10CertificateRequestConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_REQUEST.id);
            chc.receivedCertReq = true;
            if (chc.handshakeConsumers.containsKey(SSLHandshake.CERTIFICATE.id)) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected CertificateRequest handshake message");
            }
            SSLConsumer certStatCons = (SSLConsumer)chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_STATUS.id);
            if (certStatCons != null) {
                CertificateStatus.handshakeAbsence.absent(context, null);
            }
            T10CertificateRequestMessage crm = new T10CertificateRequestMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming CertificateRequest handshake message", crm);
            }
            chc.handshakeProducers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
            X509ExtendedKeyManager km = chc.sslContext.getX509KeyManager();
            String clientAlias = null;
            if (chc.conContext.transport instanceof SSLSocketImpl) {
                clientAlias = km.chooseClientAlias(crm.getKeyTypes(), crm.getAuthorities(), (SSLSocket)((Object)chc.conContext.transport));
            } else if (chc.conContext.transport instanceof SSLEngineImpl) {
                clientAlias = km.chooseEngineClientAlias(crm.getKeyTypes(), crm.getAuthorities(), (SSLEngine)((Object)chc.conContext.transport));
            }
            if (clientAlias == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No available client authentication", new Object[0]);
                }
                return;
            }
            PrivateKey clientPrivateKey = km.getPrivateKey(clientAlias);
            if (clientPrivateKey == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No available client private key", new Object[0]);
                }
                return;
            }
            X509Certificate[] clientCerts = km.getCertificateChain(clientAlias);
            if (clientCerts == null || clientCerts.length == 0) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No available client certificate", new Object[0]);
                }
                return;
            }
            chc.handshakePossessions.add(new X509Authentication.X509Possession(clientPrivateKey, clientCerts));
            chc.handshakeProducers.put(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
        }
    }

    private static final class T10CertificateRequestProducer
    implements HandshakeProducer {
        private T10CertificateRequestProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            X509Certificate[] caCerts = shc.sslContext.getX509TrustManager().getAcceptedIssuers();
            T10CertificateRequestMessage crm = new T10CertificateRequestMessage(shc, caCerts, shc.negotiatedCipherSuite.keyExchange);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced CertificateRequest handshake message", crm);
            }
            crm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.handshakeConsumers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
            shc.handshakeConsumers.put(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
            return null;
        }
    }

    private static final class T12CertificateRequestConsumer
    implements SSLConsumer {
        private T12CertificateRequestConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_REQUEST.id);
            chc.receivedCertReq = true;
            if (chc.handshakeConsumers.containsKey(SSLHandshake.CERTIFICATE.id)) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected CertificateRequest handshake message");
            }
            SSLConsumer certStatCons = (SSLConsumer)chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_STATUS.id);
            if (certStatCons != null) {
                CertificateStatus.handshakeAbsence.absent(context, null);
            }
            T12CertificateRequestMessage crm = new T12CertificateRequestMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming CertificateRequest handshake message", crm);
            }
            chc.handshakeProducers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
            List<SignatureScheme> signAlgs = SignatureScheme.getSupportedAlgorithms(chc.sslConfig, chc.algorithmConstraints, chc.negotiatedProtocol, crm.algorithmIds, SignatureScheme.HANDSHAKE_SCOPE);
            List<SignatureScheme> signCertAlgs = SignatureScheme.getSupportedAlgorithms(chc.sslConfig, chc.algorithmConstraints, chc.negotiatedProtocol, crm.algorithmIds, SignatureScheme.CERTIFICATE_SCOPE);
            if (signAlgs.isEmpty() || signCertAlgs.isEmpty()) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No supported signature algorithm");
            }
            chc.peerRequestedSignatureSchemes = signAlgs;
            chc.peerRequestedCertSignSchemes = signCertAlgs;
            chc.handshakeSession.setPeerSupportedSignatureAlgorithms(signCertAlgs);
            chc.peerSupportedAuthorities = crm.getAuthorities();
            SSLPossession pos = T12CertificateRequestConsumer.choosePossession(chc, crm);
            if (pos == null) {
                return;
            }
            chc.handshakePossessions.add(pos);
            chc.handshakeProducers.put(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
        }

        private static SSLPossession choosePossession(HandshakeContext hc, T12CertificateRequestMessage crm) {
            String[] supportedKeyTypes;
            SSLPossession pos;
            if (hc.peerRequestedCertSignSchemes == null || hc.peerRequestedCertSignSchemes.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No signature and hash algorithms in CertificateRequest", new Object[0]);
                }
                return null;
            }
            ArrayList<String> crKeyTypes = new ArrayList<String>(Arrays.asList(crm.getKeyTypes()));
            if (crKeyTypes.contains("RSA")) {
                crKeyTypes.add("RSASSA-PSS");
            }
            if ((pos = X509Authentication.createPossession(hc, supportedKeyTypes = (String[])hc.peerRequestedCertSignSchemes.stream().map(ss -> ss.keyAlgorithm).distinct().filter(ka -> SignatureScheme.getPreferableAlgorithm(hc.algorithmConstraints, hc.peerRequestedSignatureSchemes, ka, hc.negotiatedProtocol) != null || SSLLogger.logWarning("ssl,handshake", "Unable to produce CertificateVerify for key algorithm: " + ka)).filter(ka -> {
                X509Authentication xa = X509Authentication.valueOfKeyAlgorithm(ka);
                return xa != null && !Collections.disjoint(crKeyTypes, Arrays.asList(xa.keyTypes)) || SSLLogger.logWarning("ssl,handshake", "Unsupported key algorithm: " + ka);
            }).toArray(String[]::new))) == null && SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.warning("No available authentication scheme", new Object[0]);
            }
            return pos;
        }
    }

    private static final class T12CertificateRequestProducer
    implements HandshakeProducer {
        private T12CertificateRequestProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ArrayList<SignatureScheme> certReqSignAlgs = new ArrayList<SignatureScheme>(shc.localSupportedSignAlgs);
            certReqSignAlgs.retainAll(shc.localSupportedCertSignAlgs);
            if (certReqSignAlgs.isEmpty()) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No supported signature algorithm");
            }
            X509Certificate[] caCerts = shc.sslContext.getX509TrustManager().getAcceptedIssuers();
            T12CertificateRequestMessage crm = new T12CertificateRequestMessage(shc, caCerts, shc.negotiatedCipherSuite.keyExchange, certReqSignAlgs);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced CertificateRequest handshake message", crm);
            }
            crm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.handshakeConsumers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
            shc.handshakeConsumers.put(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
            return null;
        }
    }

    private static final class T13CertificateRequestConsumer
    implements SSLConsumer {
        private T13CertificateRequestConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_REQUEST.id);
            chc.receivedCertReq = true;
            if (chc.handshakeConsumers.containsKey(SSLHandshake.ENCRYPTED_EXTENSIONS.id)) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected CertificateRequest handshake message");
            }
            T13CertificateRequestMessage crm = new T13CertificateRequestMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming CertificateRequest handshake message", crm);
            }
            SSLExtension[] extTypes = chc.sslConfig.getEnabledExtensions(SSLHandshake.CERTIFICATE_REQUEST);
            crm.extensions.consumeOnLoad(chc, extTypes);
            crm.extensions.consumeOnTrade(chc, extTypes);
            chc.certRequestContext = (byte[])crm.requestContext.clone();
            chc.handshakeProducers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
            chc.handshakeProducers.put(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
        }
    }

    private static final class T13CertificateRequestProducer
    implements HandshakeProducer {
        private T13CertificateRequestProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            T13CertificateRequestMessage crm = new T13CertificateRequestMessage(shc);
            SSLExtension[] extTypes = shc.sslConfig.getEnabledExtensions(SSLHandshake.CERTIFICATE_REQUEST, shc.negotiatedProtocol);
            crm.extensions.produce(shc, extTypes);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced CertificateRequest message", crm);
            }
            crm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.certRequestContext = (byte[])crm.requestContext.clone();
            shc.handshakeConsumers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
            shc.handshakeConsumers.put(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
            return null;
        }
    }

    static final class T13CertificateRequestMessage
    extends SSLHandshake.HandshakeMessage {
        private final byte[] requestContext;
        private final SSLExtensions extensions;

        T13CertificateRequestMessage(HandshakeContext handshakeContext) {
            super(handshakeContext);
            this.requestContext = new byte[0];
            this.extensions = new SSLExtensions(this);
        }

        T13CertificateRequestMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            if (m.remaining() < 5) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid CertificateRequest handshake message: no sufficient data");
            }
            this.requestContext = Record.getBytes8(m);
            if (m.remaining() < 4) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid CertificateRequest handshake message: no sufficient extensions data");
            }
            SSLExtension[] enabledExtensions = handshakeContext.sslConfig.getEnabledExtensions(SSLHandshake.CERTIFICATE_REQUEST);
            this.extensions = new SSLExtensions(this, m, enabledExtensions);
        }

        @Override
        SSLHandshake handshakeType() {
            return SSLHandshake.CERTIFICATE_REQUEST;
        }

        @Override
        int messageLength() {
            return 1 + this.requestContext.length + this.extensions.length();
        }

        @Override
        void send(HandshakeOutStream hos) throws IOException {
            hos.putBytes8(this.requestContext);
            this.extensions.send(hos);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"CertificateRequest\": '{'\n  \"certificate_request_context\": \"{0}\",\n  \"extensions\": [\n{1}\n  ]\n'}'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{Utilities.toHexString(this.requestContext), Utilities.indent(Utilities.indent(this.extensions.toString()))};
            return messageFormat.format(messageFields);
        }
    }

    static final class T12CertificateRequestMessage
    extends SSLHandshake.HandshakeMessage {
        final byte[] types;
        final int[] algorithmIds;
        final List<byte[]> authorities;

        T12CertificateRequestMessage(HandshakeContext handshakeContext, X509Certificate[] trustedCerts, CipherSuite.KeyExchange keyExchange, List<SignatureScheme> signatureSchemes) throws IOException {
            super(handshakeContext);
            this.types = ClientCertificateType.CERT_TYPES;
            if (signatureSchemes == null || signatureSchemes.isEmpty()) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "No signature algorithms specified for CertificateRequest handshake message");
            }
            this.algorithmIds = new int[signatureSchemes.size()];
            int i = 0;
            for (SignatureScheme scheme : signatureSchemes) {
                this.algorithmIds[i++] = scheme.id;
            }
            this.authorities = new ArrayList<byte[]>(trustedCerts.length);
            for (X509Certificate cert : trustedCerts) {
                X500Principal x500Principal = cert.getSubjectX500Principal();
                this.authorities.add(x500Principal.getEncoded());
            }
        }

        T12CertificateRequestMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            if (m.remaining() < 8) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid CertificateRequest handshake message: no sufficient data");
            }
            this.types = Record.getBytes8(m);
            if (m.remaining() < 6) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid CertificateRequest handshake message: no sufficient data");
            }
            byte[] algs = Record.getBytes16(m);
            if (algs.length == 0 || (algs.length & 1) != 0) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid CertificateRequest handshake message: incomplete signature algorithms");
            }
            this.algorithmIds = new int[algs.length >> 1];
            int i = 0;
            int j = 0;
            while (i < algs.length) {
                byte hash = algs[i++];
                byte sign = algs[i++];
                this.algorithmIds[j++] = (hash & 0xFF) << 8 | sign & 0xFF;
            }
            if (m.remaining() < 2) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid CertificateRequest handshake message: no sufficient data");
            }
            int listLen = Record.getInt16(m);
            if (listLen > m.remaining()) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid CertificateRequest message: no sufficient data");
            }
            if (listLen > 0) {
                this.authorities = new LinkedList<byte[]>();
                while (listLen > 0) {
                    byte[] encoded = Record.getBytes16(m);
                    listLen -= 2 + encoded.length;
                    this.authorities.add(encoded);
                }
            } else {
                this.authorities = Collections.emptyList();
            }
        }

        String[] getKeyTypes() {
            return ClientCertificateType.getKeyTypes(this.types);
        }

        X500Principal[] getAuthorities() {
            X500Principal[] principals = new X500Principal[this.authorities.size()];
            int i = 0;
            for (byte[] encoded : this.authorities) {
                principals[i++] = new X500Principal(encoded);
            }
            return principals;
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.CERTIFICATE_REQUEST;
        }

        @Override
        public int messageLength() {
            int len = 1 + this.types.length + 2 + (this.algorithmIds.length << 1) + 2;
            for (byte[] encoded : this.authorities) {
                len += encoded.length + 2;
            }
            return len;
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            hos.putBytes8(this.types);
            int listLen = 0;
            for (byte[] encoded : this.authorities) {
                listLen += encoded.length + 2;
            }
            hos.putInt16(this.algorithmIds.length << 1);
            for (Object algorithmId : (Iterator<byte[]>)this.algorithmIds) {
                hos.putInt16((int)algorithmId);
            }
            hos.putInt16(listLen);
            for (byte[] encoded : this.authorities) {
                hos.putBytes16(encoded);
            }
        }

        /*
         * WARNING - void declaration
         */
        public String toString() {
            void var6_10;
            MessageFormat messageFormat = new MessageFormat("\"CertificateRequest\": '{'\n  \"certificate types\": {0}\n  \"supported signature algorithms\": {1}\n  \"certificate authorities\": {2}\n'}'", Locale.ENGLISH);
            ArrayList<String> typeNames = new ArrayList<String>(this.types.length);
            for (byte by : this.types) {
                typeNames.add(ClientCertificateType.nameOf(by));
            }
            ArrayList<String> algorithmNames = new ArrayList<String>(this.algorithmIds.length);
            int[] nArray = this.algorithmIds;
            int n = nArray.length;
            boolean bl = false;
            while (var6_10 < n) {
                int algorithmId = nArray[var6_10];
                algorithmNames.add(SignatureScheme.nameOf(algorithmId));
                ++var6_10;
            }
            ArrayList<String> authorityNames = new ArrayList<String>(this.authorities.size());
            for (byte[] byArray : this.authorities) {
                try {
                    X500Principal principal = new X500Principal(byArray);
                    authorityNames.add(principal.toString());
                }
                catch (IllegalArgumentException iae) {
                    authorityNames.add("unparseable distinguished name: " + iae);
                }
            }
            Object[] messageFields = new Object[]{typeNames, algorithmNames, authorityNames};
            return messageFormat.format(messageFields);
        }
    }

    static final class T10CertificateRequestMessage
    extends SSLHandshake.HandshakeMessage {
        final byte[] types;
        final List<byte[]> authorities;

        T10CertificateRequestMessage(HandshakeContext handshakeContext, X509Certificate[] trustedCerts, CipherSuite.KeyExchange keyExchange) {
            super(handshakeContext);
            this.authorities = new ArrayList<byte[]>(trustedCerts.length);
            for (X509Certificate cert : trustedCerts) {
                X500Principal x500Principal = cert.getSubjectX500Principal();
                this.authorities.add(x500Principal.getEncoded());
            }
            this.types = ClientCertificateType.CERT_TYPES;
        }

        T10CertificateRequestMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            if (m.remaining() < 4) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Incorrect CertificateRequest message: no sufficient data");
            }
            this.types = Record.getBytes8(m);
            int listLen = Record.getInt16(m);
            if (listLen > m.remaining()) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Incorrect CertificateRequest message:no sufficient data");
            }
            if (listLen > 0) {
                this.authorities = new LinkedList<byte[]>();
                while (listLen > 0) {
                    byte[] encoded = Record.getBytes16(m);
                    listLen -= 2 + encoded.length;
                    this.authorities.add(encoded);
                }
            } else {
                this.authorities = Collections.emptyList();
            }
        }

        String[] getKeyTypes() {
            return ClientCertificateType.getKeyTypes(this.types);
        }

        X500Principal[] getAuthorities() {
            X500Principal[] principals = new X500Principal[this.authorities.size()];
            int i = 0;
            for (byte[] encoded : this.authorities) {
                principals[i++] = new X500Principal(encoded);
            }
            return principals;
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.CERTIFICATE_REQUEST;
        }

        @Override
        public int messageLength() {
            int len = 1 + this.types.length + 2;
            for (byte[] encoded : this.authorities) {
                len += encoded.length + 2;
            }
            return len;
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            hos.putBytes8(this.types);
            int listLen = 0;
            for (byte[] encoded : this.authorities) {
                listLen += encoded.length + 2;
            }
            hos.putInt16(listLen);
            for (byte[] encoded : this.authorities) {
                hos.putBytes16(encoded);
            }
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"CertificateRequest\": '{'\n  \"certificate types\": {0}\n  \"certificate authorities\": {1}\n'}'", Locale.ENGLISH);
            ArrayList<String> typeNames = new ArrayList<String>(this.types.length);
            for (byte type : this.types) {
                typeNames.add(ClientCertificateType.nameOf(type));
            }
            ArrayList<String> authorityNames = new ArrayList<String>(this.authorities.size());
            for (byte[] encoded : this.authorities) {
                try {
                    X500Principal principal = new X500Principal(encoded);
                    authorityNames.add(principal.toString());
                }
                catch (IllegalArgumentException iae) {
                    authorityNames.add("unparseable distinguished name: " + iae);
                }
            }
            Object[] messageFields = new Object[]{typeNames, authorityNames};
            return messageFormat.format(messageFields);
        }
    }

    private static final class ClientCertificateType
    extends Enum<ClientCertificateType> {
        public static final /* enum */ ClientCertificateType RSA_SIGN;
        public static final /* enum */ ClientCertificateType DSS_SIGN;
        public static final /* enum */ ClientCertificateType RSA_FIXED_DH;
        public static final /* enum */ ClientCertificateType DSS_FIXED_DH;
        public static final /* enum */ ClientCertificateType RSA_EPHEMERAL_DH;
        public static final /* enum */ ClientCertificateType DSS_EPHEMERAL_DH;
        public static final /* enum */ ClientCertificateType FORTEZZA_DMS;
        public static final /* enum */ ClientCertificateType ECDSA_SIGN;
        public static final /* enum */ ClientCertificateType RSA_FIXED_ECDH;
        public static final /* enum */ ClientCertificateType ECDSA_FIXED_ECDH;
        private static final byte[] CERT_TYPES;
        final byte id;
        final String name;
        final List<String> keyAlgorithm;
        final boolean isAvailable;
        private static final /* synthetic */ ClientCertificateType[] $VALUES;

        public static ClientCertificateType[] values() {
            return (ClientCertificateType[])$VALUES.clone();
        }

        public static ClientCertificateType valueOf(String name) {
            return Enum.valueOf(ClientCertificateType.class, name);
        }

        private ClientCertificateType(byte id, String name) {
            this(id, name, null, false);
        }

        private ClientCertificateType(byte id, String name, List<String> keyAlgorithm, boolean isAvailable) {
            this.id = id;
            this.name = name;
            this.keyAlgorithm = keyAlgorithm;
            this.isAvailable = isAvailable;
        }

        private static String nameOf(byte id) {
            for (ClientCertificateType cct : ClientCertificateType.values()) {
                if (cct.id != id) continue;
                return cct.name;
            }
            return "UNDEFINED-CLIENT-CERTIFICATE-TYPE(" + id + ")";
        }

        private static ClientCertificateType valueOf(byte id) {
            for (ClientCertificateType cct : ClientCertificateType.values()) {
                if (cct.id != id) continue;
                return cct;
            }
            return null;
        }

        private static String[] getKeyTypes(byte[] ids) {
            ArrayList keyTypes = new ArrayList(3);
            for (byte id : ids) {
                ClientCertificateType cct = ClientCertificateType.valueOf(id);
                if (cct == null || !cct.isAvailable) continue;
                cct.keyAlgorithm.forEach(key -> {
                    if (!keyTypes.contains(key)) {
                        keyTypes.add(key);
                    }
                });
            }
            return keyTypes.toArray(new String[0]);
        }

        private static /* synthetic */ ClientCertificateType[] $values() {
            return new ClientCertificateType[]{RSA_SIGN, DSS_SIGN, RSA_FIXED_DH, DSS_FIXED_DH, RSA_EPHEMERAL_DH, DSS_EPHEMERAL_DH, FORTEZZA_DMS, ECDSA_SIGN, RSA_FIXED_ECDH, ECDSA_FIXED_ECDH};
        }

        static {
            byte[] byArray;
            RSA_SIGN = new ClientCertificateType(1, "rsa_sign", List.of("RSA"), true);
            DSS_SIGN = new ClientCertificateType(2, "dss_sign", List.of("DSA"), true);
            RSA_FIXED_DH = new ClientCertificateType(3, "rsa_fixed_dh");
            DSS_FIXED_DH = new ClientCertificateType(4, "dss_fixed_dh");
            RSA_EPHEMERAL_DH = new ClientCertificateType(5, "rsa_ephemeral_dh");
            DSS_EPHEMERAL_DH = new ClientCertificateType(6, "dss_ephemeral_dh");
            FORTEZZA_DMS = new ClientCertificateType(20, "fortezza_dms");
            ECDSA_SIGN = new ClientCertificateType(64, "ecdsa_sign", List.of("EC", "EdDSA"), JsseJce.isEcAvailable());
            RSA_FIXED_ECDH = new ClientCertificateType(65, "rsa_fixed_ecdh");
            ECDSA_FIXED_ECDH = new ClientCertificateType(66, "ecdsa_fixed_ecdh");
            $VALUES = ClientCertificateType.$values();
            if (JsseJce.isEcAvailable()) {
                byte[] byArray2 = new byte[3];
                byArray2[0] = ClientCertificateType.ECDSA_SIGN.id;
                byArray2[1] = ClientCertificateType.RSA_SIGN.id;
                byArray = byArray2;
                byArray2[2] = ClientCertificateType.DSS_SIGN.id;
            } else {
                byte[] byArray3 = new byte[2];
                byArray3[0] = ClientCertificateType.RSA_SIGN.id;
                byArray = byArray3;
                byArray3[1] = ClientCertificateType.DSS_SIGN.id;
            }
            CERT_TYPES = byArray;
        }
    }
}

