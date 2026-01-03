/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.DHClientKeyExchange;
import sun.security.ssl.DHKeyExchange;
import sun.security.ssl.DHServerKeyExchange;
import sun.security.ssl.ECDHClientKeyExchange;
import sun.security.ssl.ECDHKeyExchange;
import sun.security.ssl.ECDHServerKeyExchange;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.JsseJce;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.RSAClientKeyExchange;
import sun.security.ssl.RSAKeyExchange;
import sun.security.ssl.RSAServerKeyExchange;
import sun.security.ssl.SSLAuthentication;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLHandshakeBinding;
import sun.security.ssl.SSLKeyAgreement;
import sun.security.ssl.SSLKeyAgreementGenerator;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLPossessionGenerator;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.X509Authentication;

final class SSLKeyExchange
implements SSLKeyAgreementGenerator,
SSLHandshakeBinding {
    private final List<SSLAuthentication> authentication;
    private final SSLKeyAgreement keyAgreement;

    SSLKeyExchange(List<X509Authentication> authentication, SSLKeyAgreement keyAgreement) {
        this.authentication = authentication != null ? List.copyOf(authentication) : null;
        this.keyAgreement = keyAgreement;
    }

    SSLPossession[] createPossessions(HandshakeContext context) {
        SSLPossession[] sSLPossessionArray;
        SSLPossession kaPossession;
        SSLPossession authPossession = null;
        if (this.authentication != null) {
            SSLAuthentication authType;
            Iterator<SSLAuthentication> iterator = this.authentication.iterator();
            while (iterator.hasNext() && (authPossession = (authType = iterator.next()).createPossession(context)) == null) {
            }
            if (authPossession == null) {
                return new SSLPossession[0];
            }
            if (context instanceof ServerHandshakeContext) {
                ServerHandshakeContext shc = (ServerHandshakeContext)context;
                shc.interimAuthn = authPossession;
            }
        }
        if (this.keyAgreement == T12KeyAgreement.RSA_EXPORT) {
            SSLPossession[] sSLPossessionArray2;
            X509Authentication.X509Possession x509Possession = (X509Authentication.X509Possession)authPossession;
            if (JsseJce.getRSAKeyLength(x509Possession.popCerts[0].getPublicKey()) > 512) {
                SSLPossession[] sSLPossessionArray3;
                kaPossession = this.keyAgreement.createPossession(context);
                if (kaPossession == null) {
                    return new SSLPossession[0];
                }
                if (this.authentication != null) {
                    SSLPossession[] sSLPossessionArray4 = new SSLPossession[2];
                    sSLPossessionArray4[0] = authPossession;
                    sSLPossessionArray3 = sSLPossessionArray4;
                    sSLPossessionArray4[1] = kaPossession;
                } else {
                    SSLPossession[] sSLPossessionArray5 = new SSLPossession[1];
                    sSLPossessionArray3 = sSLPossessionArray5;
                    sSLPossessionArray5[0] = kaPossession;
                }
                return sSLPossessionArray3;
            }
            if (this.authentication != null) {
                SSLPossession[] sSLPossessionArray6 = new SSLPossession[1];
                sSLPossessionArray2 = sSLPossessionArray6;
                sSLPossessionArray6[0] = authPossession;
            } else {
                sSLPossessionArray2 = new SSLPossession[]{};
            }
            return sSLPossessionArray2;
        }
        kaPossession = this.keyAgreement.createPossession(context);
        if (kaPossession == null) {
            if (this.keyAgreement == T12KeyAgreement.RSA || this.keyAgreement == T12KeyAgreement.ECDH) {
                SSLPossession[] sSLPossessionArray7;
                if (this.authentication != null) {
                    SSLPossession[] sSLPossessionArray8 = new SSLPossession[1];
                    sSLPossessionArray7 = sSLPossessionArray8;
                    sSLPossessionArray8[0] = authPossession;
                } else {
                    sSLPossessionArray7 = new SSLPossession[]{};
                }
                return sSLPossessionArray7;
            }
            return new SSLPossession[0];
        }
        if (this.authentication != null) {
            SSLPossession[] sSLPossessionArray9 = new SSLPossession[2];
            sSLPossessionArray9[0] = authPossession;
            sSLPossessionArray = sSLPossessionArray9;
            sSLPossessionArray9[1] = kaPossession;
        } else {
            SSLPossession[] sSLPossessionArray10 = new SSLPossession[1];
            sSLPossessionArray = sSLPossessionArray10;
            sSLPossessionArray10[0] = kaPossession;
        }
        return sSLPossessionArray;
    }

    @Override
    public SSLKeyDerivation createKeyDerivation(HandshakeContext handshakeContext) throws IOException {
        return this.keyAgreement.createKeyDerivation(handshakeContext);
    }

    @Override
    public SSLHandshake[] getRelatedHandshakers(HandshakeContext handshakeContext) {
        SSLHandshake[] auHandshakes = null;
        if (this.authentication != null) {
            SSLAuthentication authType;
            Iterator<SSLAuthentication> iterator = this.authentication.iterator();
            while (iterator.hasNext() && ((auHandshakes = (authType = iterator.next()).getRelatedHandshakers(handshakeContext)) == null || auHandshakes.length <= 0)) {
            }
        }
        SSLHandshake[] kaHandshakes = this.keyAgreement.getRelatedHandshakers(handshakeContext);
        if (auHandshakes == null || auHandshakes.length == 0) {
            return kaHandshakes;
        }
        if (kaHandshakes == null || kaHandshakes.length == 0) {
            return auHandshakes;
        }
        SSLHandshake[] producers = Arrays.copyOf(auHandshakes, auHandshakes.length + kaHandshakes.length);
        System.arraycopy(kaHandshakes, 0, producers, auHandshakes.length, kaHandshakes.length);
        return producers;
    }

    @Override
    public Map.Entry<Byte, HandshakeProducer>[] getHandshakeProducers(HandshakeContext handshakeContext) {
        Map.Entry<Byte, HandshakeProducer>[] auProducers = null;
        if (this.authentication != null) {
            SSLAuthentication authType;
            Iterator<SSLAuthentication> iterator = this.authentication.iterator();
            while (iterator.hasNext() && ((auProducers = (authType = iterator.next()).getHandshakeProducers(handshakeContext)) == null || auProducers.length <= 0)) {
            }
        }
        Map.Entry<Byte, HandshakeProducer>[] kaProducers = this.keyAgreement.getHandshakeProducers(handshakeContext);
        if (auProducers == null || auProducers.length == 0) {
            return kaProducers;
        }
        if (kaProducers == null || kaProducers.length == 0) {
            return auProducers;
        }
        Map.Entry<Byte, HandshakeProducer>[] producers = Arrays.copyOf(auProducers, auProducers.length + kaProducers.length);
        System.arraycopy(kaProducers, 0, producers, auProducers.length, kaProducers.length);
        return producers;
    }

    @Override
    public Map.Entry<Byte, SSLConsumer>[] getHandshakeConsumers(HandshakeContext handshakeContext) {
        Map.Entry<Byte, SSLConsumer>[] auConsumers = null;
        if (this.authentication != null) {
            SSLAuthentication authType;
            Iterator<SSLAuthentication> iterator = this.authentication.iterator();
            while (iterator.hasNext() && ((auConsumers = (authType = iterator.next()).getHandshakeConsumers(handshakeContext)) == null || auConsumers.length <= 0)) {
            }
        }
        Map.Entry<Byte, SSLConsumer>[] kaConsumers = this.keyAgreement.getHandshakeConsumers(handshakeContext);
        if (auConsumers == null || auConsumers.length == 0) {
            return kaConsumers;
        }
        if (kaConsumers == null || kaConsumers.length == 0) {
            return auConsumers;
        }
        Map.Entry<Byte, SSLConsumer>[] producers = Arrays.copyOf(auConsumers, auConsumers.length + kaConsumers.length);
        System.arraycopy(kaConsumers, 0, producers, auConsumers.length, kaConsumers.length);
        return producers;
    }

    static SSLKeyExchange valueOf(CipherSuite.KeyExchange keyExchange, ProtocolVersion protocolVersion) {
        if (keyExchange == null || protocolVersion == null) {
            return null;
        }
        switch (keyExchange) {
            case K_RSA: {
                return SSLKeyExRSA.KE;
            }
            case K_RSA_EXPORT: {
                return SSLKeyExRSAExport.KE;
            }
            case K_DHE_DSS: {
                return SSLKeyExDHEDSS.KE;
            }
            case K_DHE_DSS_EXPORT: {
                return SSLKeyExDHEDSSExport.KE;
            }
            case K_DHE_RSA: {
                if (protocolVersion.useTLS12PlusSpec()) {
                    return SSLKeyExDHERSAOrPSS.KE;
                }
                return SSLKeyExDHERSA.KE;
            }
            case K_DHE_RSA_EXPORT: {
                return SSLKeyExDHERSAExport.KE;
            }
            case K_DH_ANON: {
                return SSLKeyExDHANON.KE;
            }
            case K_DH_ANON_EXPORT: {
                return SSLKeyExDHANONExport.KE;
            }
            case K_ECDH_ECDSA: {
                return SSLKeyExECDHECDSA.KE;
            }
            case K_ECDH_RSA: {
                return SSLKeyExECDHRSA.KE;
            }
            case K_ECDHE_ECDSA: {
                return SSLKeyExECDHEECDSA.KE;
            }
            case K_ECDHE_RSA: {
                if (protocolVersion.useTLS12PlusSpec()) {
                    return SSLKeyExECDHERSAOrPSS.KE;
                }
                return SSLKeyExECDHERSA.KE;
            }
            case K_ECDH_ANON: {
                return SSLKeyExECDHANON.KE;
            }
        }
        return null;
    }

    static SSLKeyExchange valueOf(NamedGroup namedGroup) {
        T13KeyAgreement ka = T13KeyAgreement.valueOf(namedGroup);
        if (ka != null) {
            return new SSLKeyExchange(null, ka);
        }
        return null;
    }

    private static enum T12KeyAgreement implements SSLKeyAgreement
    {
        RSA("rsa", null, RSAKeyExchange.kaGenerator),
        RSA_EXPORT("rsa_export", RSAKeyExchange.poGenerator, RSAKeyExchange.kaGenerator),
        DHE("dhe", DHKeyExchange.poGenerator, DHKeyExchange.kaGenerator),
        DHE_EXPORT("dhe_export", DHKeyExchange.poExportableGenerator, DHKeyExchange.kaGenerator),
        ECDH("ecdh", null, ECDHKeyExchange.ecdhKAGenerator),
        ECDHE("ecdhe", ECDHKeyExchange.poGenerator, ECDHKeyExchange.ecdheXdhKAGenerator);

        final String name;
        final SSLPossessionGenerator possessionGenerator;
        final SSLKeyAgreementGenerator keyAgreementGenerator;

        private T12KeyAgreement(String name, SSLPossessionGenerator possessionGenerator, SSLKeyAgreementGenerator keyAgreementGenerator) {
            this.name = name;
            this.possessionGenerator = possessionGenerator;
            this.keyAgreementGenerator = keyAgreementGenerator;
        }

        @Override
        public SSLPossession createPossession(HandshakeContext context) {
            if (this.possessionGenerator != null) {
                return this.possessionGenerator.createPossession(context);
            }
            return null;
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context) throws IOException {
            return this.keyAgreementGenerator.createKeyDerivation(context);
        }

        @Override
        public SSLHandshake[] getRelatedHandshakers(HandshakeContext handshakeContext) {
            if (!handshakeContext.negotiatedProtocol.useTLS13PlusSpec() && this.possessionGenerator != null) {
                return new SSLHandshake[]{SSLHandshake.SERVER_KEY_EXCHANGE};
            }
            return new SSLHandshake[0];
        }

        @Override
        public Map.Entry<Byte, HandshakeProducer>[] getHandshakeProducers(HandshakeContext handshakeContext) {
            if (handshakeContext.negotiatedProtocol.useTLS13PlusSpec()) {
                return new Map.Entry[0];
            }
            if (handshakeContext.sslConfig.isClientMode) {
                switch (this.ordinal()) {
                    case 0: 
                    case 1: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, HandshakeProducer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, RSAClientKeyExchange.rsaHandshakeProducer)};
                    }
                    case 2: 
                    case 3: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, DHClientKeyExchange.DHClientKeyExchangeProducer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, DHClientKeyExchange.dhHandshakeProducer)};
                    }
                    case 4: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, HandshakeProducer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, ECDHClientKeyExchange.ecdhHandshakeProducer)};
                    }
                    case 5: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, HandshakeProducer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, ECDHClientKeyExchange.ecdheHandshakeProducer)};
                    }
                }
            } else {
                switch (this.ordinal()) {
                    case 1: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, HandshakeProducer>(SSLHandshake.SERVER_KEY_EXCHANGE.id, RSAServerKeyExchange.rsaHandshakeProducer)};
                    }
                    case 2: 
                    case 3: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, HandshakeProducer>(SSLHandshake.SERVER_KEY_EXCHANGE.id, DHServerKeyExchange.dhHandshakeProducer)};
                    }
                    case 5: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, HandshakeProducer>(SSLHandshake.SERVER_KEY_EXCHANGE.id, ECDHServerKeyExchange.ecdheHandshakeProducer)};
                    }
                }
            }
            return new Map.Entry[0];
        }

        @Override
        public Map.Entry<Byte, SSLConsumer>[] getHandshakeConsumers(HandshakeContext handshakeContext) {
            if (handshakeContext.negotiatedProtocol.useTLS13PlusSpec()) {
                return new Map.Entry[0];
            }
            if (handshakeContext.sslConfig.isClientMode) {
                switch (this.ordinal()) {
                    case 1: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, SSLConsumer>(SSLHandshake.SERVER_KEY_EXCHANGE.id, RSAServerKeyExchange.rsaHandshakeConsumer)};
                    }
                    case 2: 
                    case 3: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, SSLConsumer>(SSLHandshake.SERVER_KEY_EXCHANGE.id, DHServerKeyExchange.dhHandshakeConsumer)};
                    }
                    case 5: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, SSLConsumer>(SSLHandshake.SERVER_KEY_EXCHANGE.id, ECDHServerKeyExchange.ecdheHandshakeConsumer)};
                    }
                }
            } else {
                switch (this.ordinal()) {
                    case 0: 
                    case 1: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, SSLConsumer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, RSAClientKeyExchange.rsaHandshakeConsumer)};
                    }
                    case 2: 
                    case 3: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, DHClientKeyExchange.DHClientKeyExchangeConsumer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, DHClientKeyExchange.dhHandshakeConsumer)};
                    }
                    case 4: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, SSLConsumer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, ECDHClientKeyExchange.ecdhHandshakeConsumer)};
                    }
                    case 5: {
                        return new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<Byte, SSLConsumer>(SSLHandshake.CLIENT_KEY_EXCHANGE.id, ECDHClientKeyExchange.ecdheHandshakeConsumer)};
                    }
                }
            }
            return new Map.Entry[0];
        }
    }

    private static class SSLKeyExRSA {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.RSA), T12KeyAgreement.RSA);

        private SSLKeyExRSA() {
        }
    }

    private static class SSLKeyExRSAExport {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.RSA), T12KeyAgreement.RSA_EXPORT);

        private SSLKeyExRSAExport() {
        }
    }

    private static class SSLKeyExDHEDSS {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.DSA), T12KeyAgreement.DHE);

        private SSLKeyExDHEDSS() {
        }
    }

    private static class SSLKeyExDHEDSSExport {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.DSA), T12KeyAgreement.DHE_EXPORT);

        private SSLKeyExDHEDSSExport() {
        }
    }

    private static class SSLKeyExDHERSAOrPSS {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.RSA_OR_PSS), T12KeyAgreement.DHE);

        private SSLKeyExDHERSAOrPSS() {
        }
    }

    private static class SSLKeyExDHERSA {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.RSA), T12KeyAgreement.DHE);

        private SSLKeyExDHERSA() {
        }
    }

    private static class SSLKeyExDHERSAExport {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.RSA), T12KeyAgreement.DHE_EXPORT);

        private SSLKeyExDHERSAExport() {
        }
    }

    private static class SSLKeyExDHANON {
        private static final SSLKeyExchange KE = new SSLKeyExchange(null, T12KeyAgreement.DHE);

        private SSLKeyExDHANON() {
        }
    }

    private static class SSLKeyExDHANONExport {
        private static final SSLKeyExchange KE = new SSLKeyExchange(null, T12KeyAgreement.DHE_EXPORT);

        private SSLKeyExDHANONExport() {
        }
    }

    private static class SSLKeyExECDHECDSA {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.EC), T12KeyAgreement.ECDH);

        private SSLKeyExECDHECDSA() {
        }
    }

    private static class SSLKeyExECDHRSA {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.EC), T12KeyAgreement.ECDH);

        private SSLKeyExECDHRSA() {
        }
    }

    private static class SSLKeyExECDHEECDSA {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.EC, X509Authentication.EDDSA), T12KeyAgreement.ECDHE);

        private SSLKeyExECDHEECDSA() {
        }
    }

    private static class SSLKeyExECDHERSAOrPSS {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.RSA_OR_PSS), T12KeyAgreement.ECDHE);

        private SSLKeyExECDHERSAOrPSS() {
        }
    }

    private static class SSLKeyExECDHERSA {
        private static final SSLKeyExchange KE = new SSLKeyExchange(List.of(X509Authentication.RSA), T12KeyAgreement.ECDHE);

        private SSLKeyExECDHERSA() {
        }
    }

    private static class SSLKeyExECDHANON {
        private static final SSLKeyExchange KE = new SSLKeyExchange(null, T12KeyAgreement.ECDHE);

        private SSLKeyExECDHANON() {
        }
    }

    private static final class T13KeyAgreement
    implements SSLKeyAgreement {
        private final NamedGroup namedGroup;

        private T13KeyAgreement(NamedGroup namedGroup) {
            this.namedGroup = namedGroup;
        }

        static T13KeyAgreement valueOf(NamedGroup namedGroup) {
            return new T13KeyAgreement(namedGroup);
        }

        @Override
        public SSLPossession createPossession(HandshakeContext hc) {
            return this.namedGroup.createPossession(hc.sslContext.getSecureRandom());
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext hc) throws IOException {
            return this.namedGroup.createKeyDerivation(hc);
        }
    }
}

