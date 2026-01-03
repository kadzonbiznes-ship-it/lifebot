/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.security.ssl.JsseJce;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.Utilities;

enum CipherSuite {
    TLS_AES_256_GCM_SHA384(4866, true, "TLS_AES_256_GCM_SHA384", ProtocolVersion.PROTOCOLS_OF_13, SSLCipher.B_AES_256_GCM_IV, HashAlg.H_SHA384),
    TLS_AES_128_GCM_SHA256(4865, true, "TLS_AES_128_GCM_SHA256", ProtocolVersion.PROTOCOLS_OF_13, SSLCipher.B_AES_128_GCM_IV, HashAlg.H_SHA256),
    TLS_CHACHA20_POLY1305_SHA256(4867, true, "TLS_CHACHA20_POLY1305_SHA256", ProtocolVersion.PROTOCOLS_OF_13, SSLCipher.B_CC20_P1305, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384(49196, true, "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256(49195, true, "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256(52393, true, "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_CC20_P1305, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384(49200, true, "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256(52392, true, "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_CC20_P1305, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256(49199, true, "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384(159, true, "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_RSA, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256(52394, true, "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_RSA, SSLCipher.B_CC20_P1305, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_DHE_DSS_WITH_AES_256_GCM_SHA384(163, true, "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_DSS, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256(158, true, "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_RSA, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_DHE_DSS_WITH_AES_128_GCM_SHA256(162, true, "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_DSS, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384(49188, true, "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_AES_256, MacAlg.M_SHA384, HashAlg.H_SHA384),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384(49192, true, "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA384, HashAlg.H_SHA384),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256(49187, true, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256(49191, true, "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256(107, true, "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA256(106, true, "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_DSS, SSLCipher.B_AES_256, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256(103, true, "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA256(64, true, "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DHE_DSS, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384(49198, true, "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384(49202, true, "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256(49197, true, "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256(49201, true, "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384(49190, true, "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_AES_256, MacAlg.M_SHA384, HashAlg.H_SHA384),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384(49194, true, "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA384, HashAlg.H_SHA384),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256(49189, true, "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256(49193, true, "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA(49162, true, "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA(49172, true, "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA(49161, true, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA(49171, true, "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA(57, true, "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DHE_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA(56, true, "TLS_DHE_DSS_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DHE_DSS, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA(51, true, "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DHE_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA(50, true, "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DHE_DSS, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA(49157, true, "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA(49167, true, "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA(49156, true, "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA(49166, true, "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_RSA_WITH_AES_256_GCM_SHA384(157, true, "TLS_RSA_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_RSA, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_RSA_WITH_AES_128_GCM_SHA256(156, true, "TLS_RSA_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_RSA, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_RSA_WITH_AES_256_CBC_SHA256(61, true, "TLS_RSA_WITH_AES_256_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_RSA_WITH_AES_128_CBC_SHA256(60, true, "TLS_RSA_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_RSA_WITH_AES_256_CBC_SHA(53, true, "TLS_RSA_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_RSA, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_RSA_WITH_AES_128_CBC_SHA(47, true, "TLS_RSA_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_RSA, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_EMPTY_RENEGOTIATION_INFO_SCSV(255, true, "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_SCSV, SSLCipher.B_NULL, MacAlg.M_NULL, HashAlg.H_NONE),
    TLS_DH_anon_WITH_AES_256_GCM_SHA384(167, false, "TLS_DH_anon_WITH_AES_256_GCM_SHA384", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DH_ANON, SSLCipher.B_AES_256_GCM, MacAlg.M_NULL, HashAlg.H_SHA384),
    TLS_DH_anon_WITH_AES_128_GCM_SHA256(166, false, "TLS_DH_anon_WITH_AES_128_GCM_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DH_ANON, SSLCipher.B_AES_128_GCM, MacAlg.M_NULL, HashAlg.H_SHA256),
    TLS_DH_anon_WITH_AES_256_CBC_SHA256(109, false, "TLS_DH_anon_WITH_AES_256_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DH_ANON, SSLCipher.B_AES_256, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_ECDH_anon_WITH_AES_256_CBC_SHA(49177, false, "TLS_ECDH_anon_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ANON, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_DH_anon_WITH_AES_256_CBC_SHA(58, false, "TLS_DH_anon_WITH_AES_256_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DH_ANON, SSLCipher.B_AES_256, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_DH_anon_WITH_AES_128_CBC_SHA256(108, false, "TLS_DH_anon_WITH_AES_128_CBC_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_DH_ANON, SSLCipher.B_AES_128, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_ECDH_anon_WITH_AES_128_CBC_SHA(49176, false, "TLS_ECDH_anon_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ANON, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_DH_anon_WITH_AES_128_CBC_SHA(52, false, "TLS_DH_anon_WITH_AES_128_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DH_ANON, SSLCipher.B_AES_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA(49160, false, "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA(49170, false, "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA(22, false, "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DHE_RSA, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA(19, false, "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DHE_DSS, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA(49155, false, "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA(49165, false, "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_RSA_WITH_3DES_EDE_CBC_SHA(10, false, "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_RSA_WITH_3DES_EDE_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_RSA, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA(49175, false, "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ANON, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_DH_anon_WITH_3DES_EDE_CBC_SHA(27, false, "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", "TLS_DH_anon_WITH_3DES_EDE_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_DH_ANON, SSLCipher.B_3DES, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_RC4_128_SHA(49159, false, "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", "", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_RC4_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_RC4_128_SHA(49169, false, "TLS_ECDHE_RSA_WITH_RC4_128_SHA", "", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_RC4_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_RSA_WITH_RC4_128_SHA(5, false, "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_RC4_128_SHA", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_RSA, SSLCipher.B_RC4_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_ECDSA_WITH_RC4_128_SHA(49154, false, "TLS_ECDH_ECDSA_WITH_RC4_128_SHA", "", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_RC4_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_RSA_WITH_RC4_128_SHA(49164, false, "TLS_ECDH_RSA_WITH_RC4_128_SHA", "", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_ECDH_RSA, SSLCipher.B_RC4_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_RSA_WITH_RC4_128_MD5(4, false, "SSL_RSA_WITH_RC4_128_MD5", "TLS_RSA_WITH_RC4_128_MD5", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_RSA, SSLCipher.B_RC4_128, MacAlg.M_MD5, HashAlg.H_SHA256),
    TLS_ECDH_anon_WITH_RC4_128_SHA(49174, false, "TLS_ECDH_anon_WITH_RC4_128_SHA", "", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_ECDH_ANON, SSLCipher.B_RC4_128, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_DH_anon_WITH_RC4_128_MD5(24, false, "SSL_DH_anon_WITH_RC4_128_MD5", "TLS_DH_anon_WITH_RC4_128_MD5", ProtocolVersion.PROTOCOLS_TO_TLS12, KeyExchange.K_DH_ANON, SSLCipher.B_RC4_128, MacAlg.M_MD5, HashAlg.H_SHA256),
    SSL_RSA_WITH_DES_CBC_SHA(9, false, "SSL_RSA_WITH_DES_CBC_SHA", "TLS_RSA_WITH_DES_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_11, KeyExchange.K_RSA, SSLCipher.B_DES, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_DHE_RSA_WITH_DES_CBC_SHA(21, false, "SSL_DHE_RSA_WITH_DES_CBC_SHA", "TLS_DHE_RSA_WITH_DES_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_11, KeyExchange.K_DHE_RSA, SSLCipher.B_DES, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_DHE_DSS_WITH_DES_CBC_SHA(18, false, "SSL_DHE_DSS_WITH_DES_CBC_SHA", "TLS_DHE_DSS_WITH_DES_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_11, KeyExchange.K_DHE_DSS, SSLCipher.B_DES, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_DH_anon_WITH_DES_CBC_SHA(26, false, "SSL_DH_anon_WITH_DES_CBC_SHA", "TLS_DH_anon_WITH_DES_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_11, KeyExchange.K_DH_ANON, SSLCipher.B_DES, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_RSA_EXPORT_WITH_DES40_CBC_SHA(8, false, "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "TLS_RSA_EXPORT_WITH_DES40_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_10, KeyExchange.K_RSA_EXPORT, SSLCipher.B_DES_40, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA(20, false, "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_10, KeyExchange.K_DHE_RSA_EXPORT, SSLCipher.B_DES_40, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA(17, false, "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_10, KeyExchange.K_DHE_DSS_EXPORT, SSLCipher.B_DES_40, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA(25, false, "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", "TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA", ProtocolVersion.PROTOCOLS_TO_10, KeyExchange.K_DH_ANON_EXPORT, SSLCipher.B_DES_40, MacAlg.M_SHA, HashAlg.H_NONE),
    SSL_RSA_EXPORT_WITH_RC4_40_MD5(3, false, "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "TLS_RSA_EXPORT_WITH_RC4_40_MD5", ProtocolVersion.PROTOCOLS_TO_10, KeyExchange.K_RSA_EXPORT, SSLCipher.B_RC4_40, MacAlg.M_MD5, HashAlg.H_NONE),
    SSL_DH_anon_EXPORT_WITH_RC4_40_MD5(23, false, "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", "TLS_DH_anon_EXPORT_WITH_RC4_40_MD5", ProtocolVersion.PROTOCOLS_TO_10, KeyExchange.K_DH_ANON, SSLCipher.B_RC4_40, MacAlg.M_MD5, HashAlg.H_NONE),
    TLS_RSA_WITH_NULL_SHA256(59, false, "TLS_RSA_WITH_NULL_SHA256", "", ProtocolVersion.PROTOCOLS_OF_12, KeyExchange.K_RSA, SSLCipher.B_NULL, MacAlg.M_SHA256, HashAlg.H_SHA256),
    TLS_ECDHE_ECDSA_WITH_NULL_SHA(49158, false, "TLS_ECDHE_ECDSA_WITH_NULL_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_ECDSA, SSLCipher.B_NULL, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDHE_RSA_WITH_NULL_SHA(49168, false, "TLS_ECDHE_RSA_WITH_NULL_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDHE_RSA, SSLCipher.B_NULL, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_RSA_WITH_NULL_SHA(2, false, "SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_NULL_SHA", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_RSA, SSLCipher.B_NULL, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_ECDSA_WITH_NULL_SHA(49153, false, "TLS_ECDH_ECDSA_WITH_NULL_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ECDSA, SSLCipher.B_NULL, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_RSA_WITH_NULL_SHA(49163, false, "TLS_ECDH_RSA_WITH_NULL_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_RSA, SSLCipher.B_NULL, MacAlg.M_SHA, HashAlg.H_SHA256),
    TLS_ECDH_anon_WITH_NULL_SHA(49173, false, "TLS_ECDH_anon_WITH_NULL_SHA", "", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_ECDH_ANON, SSLCipher.B_NULL, MacAlg.M_SHA, HashAlg.H_SHA256),
    SSL_RSA_WITH_NULL_MD5(1, false, "SSL_RSA_WITH_NULL_MD5", "TLS_RSA_WITH_NULL_MD5", ProtocolVersion.PROTOCOLS_TO_12, KeyExchange.K_RSA, SSLCipher.B_NULL, MacAlg.M_MD5, HashAlg.H_SHA256),
    TLS_AES_128_CCM_SHA256("TLS_AES_128_CCM_SHA256", 4868),
    TLS_AES_128_CCM_8_SHA256("TLS_AES_128_CCM_8_SHA256", 4869),
    CS_0006("SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5", 6),
    CS_0007("SSL_RSA_WITH_IDEA_CBC_SHA", 7),
    CS_000B("SSL_DH_DSS_EXPORT_WITH_DES40_CBC_SHA", 11),
    CS_000C("SSL_DH_DSS_WITH_DES_CBC_SHA", 12),
    CS_000D("SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA", 13),
    CS_000E("SSL_DH_RSA_EXPORT_WITH_DES40_CBC_SHA", 14),
    CS_000F("SSL_DH_RSA_WITH_DES_CBC_SHA", 15),
    CS_0010("SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA", 16),
    CS_001C("SSL_FORTEZZA_DMS_WITH_NULL_SHA", 28),
    CS_001D("SSL_FORTEZZA_DMS_WITH_FORTEZZA_CBC_SHA", 29),
    CS_0062("SSL_RSA_EXPORT1024_WITH_DES_CBC_SHA", 98),
    CS_0063("SSL_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA", 99),
    CS_0064("SSL_RSA_EXPORT1024_WITH_RC4_56_SHA", 100),
    CS_0065("SSL_DHE_DSS_EXPORT1024_WITH_RC4_56_SHA", 101),
    CS_0066("SSL_DHE_DSS_WITH_RC4_128_SHA", 102),
    CS_FFE0("NETSCAPE_RSA_FIPS_WITH_3DES_EDE_CBC_SHA", 65504),
    CS_FFE1("NETSCAPE_RSA_FIPS_WITH_DES_CBC_SHA", 65505),
    CS_FEFE("SSL_RSA_FIPS_WITH_DES_CBC_SHA", 65278),
    CS_FEFF("SSL_RSA_FIPS_WITH_3DES_EDE_CBC_SHA", 65279),
    CS_001E("TLS_KRB5_WITH_DES_CBC_SHA", 30),
    CS_001F("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", 31),
    CS_0020("TLS_KRB5_WITH_RC4_128_SHA", 32),
    CS_0021("TLS_KRB5_WITH_IDEA_CBC_SHA", 33),
    CS_0022("TLS_KRB5_WITH_DES_CBC_MD5", 34),
    CS_0023("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", 35),
    CS_0024("TLS_KRB5_WITH_RC4_128_MD5", 36),
    CS_0025("TLS_KRB5_WITH_IDEA_CBC_MD5", 37),
    CS_0026("TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", 38),
    CS_0027("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA", 39),
    CS_0028("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", 40),
    CS_0029("TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", 41),
    CS_002A("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5", 42),
    CS_002B("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", 43),
    CS_0096("TLS_RSA_WITH_SEED_CBC_SHA", 150),
    CS_0097("TLS_DH_DSS_WITH_SEED_CBC_SHA", 151),
    CS_0098("TLS_DH_RSA_WITH_SEED_CBC_SHA", 152),
    CS_0099("TLS_DHE_DSS_WITH_SEED_CBC_SHA", 153),
    CS_009A("TLS_DHE_RSA_WITH_SEED_CBC_SHA", 154),
    CS_009B("TLS_DH_anon_WITH_SEED_CBC_SHA", 155),
    CS_008A("TLS_PSK_WITH_RC4_128_SHA", 138),
    CS_008B("TLS_PSK_WITH_3DES_EDE_CBC_SHA", 139),
    CS_008C("TLS_PSK_WITH_AES_128_CBC_SHA", 140),
    CS_008D("TLS_PSK_WITH_AES_256_CBC_SHA", 141),
    CS_008E("TLS_DHE_PSK_WITH_RC4_128_SHA", 142),
    CS_008F("TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA", 143),
    CS_0090("TLS_DHE_PSK_WITH_AES_128_CBC_SHA", 144),
    CS_0091("TLS_DHE_PSK_WITH_AES_256_CBC_SHA", 145),
    CS_0092("TLS_RSA_PSK_WITH_RC4_128_SHA", 146),
    CS_0093("TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA", 147),
    CS_0094("TLS_RSA_PSK_WITH_AES_128_CBC_SHA", 148),
    CS_0095("TLS_RSA_PSK_WITH_AES_256_CBC_SHA", 149),
    CS_002C("TLS_PSK_WITH_NULL_SHA", 44),
    CS_002D("TLS_DHE_PSK_WITH_NULL_SHA", 45),
    CS_002E("TLS_RSA_PSK_WITH_NULL_SHA", 46),
    CS_0030("TLS_DH_DSS_WITH_AES_128_CBC_SHA", 48),
    CS_0031("TLS_DH_RSA_WITH_AES_128_CBC_SHA", 49),
    CS_0036("TLS_DH_DSS_WITH_AES_256_CBC_SHA", 54),
    CS_0037("TLS_DH_RSA_WITH_AES_256_CBC_SHA", 55),
    CS_003E("TLS_DH_DSS_WITH_AES_128_CBC_SHA256", 62),
    CS_003F("TLS_DH_RSA_WITH_AES_128_CBC_SHA256", 63),
    CS_0068("TLS_DH_DSS_WITH_AES_256_CBC_SHA256", 104),
    CS_0069("TLS_DH_RSA_WITH_AES_256_CBC_SHA256", 105),
    CS_00A0("TLS_DH_RSA_WITH_AES_128_GCM_SHA256", 160),
    CS_00A1("TLS_DH_RSA_WITH_AES_256_GCM_SHA384", 161),
    CS_00A4("TLS_DH_DSS_WITH_AES_128_GCM_SHA256", 164),
    CS_00A5("TLS_DH_DSS_WITH_AES_256_GCM_SHA384", 165),
    CS_00A8("TLS_PSK_WITH_AES_128_GCM_SHA256", 168),
    CS_00A9("TLS_PSK_WITH_AES_256_GCM_SHA384", 169),
    CS_00AA("TLS_DHE_PSK_WITH_AES_128_GCM_SHA256", 170),
    CS_00AB("TLS_DHE_PSK_WITH_AES_256_GCM_SHA384", 171),
    CS_00AC("TLS_RSA_PSK_WITH_AES_128_GCM_SHA256", 172),
    CS_00AD("TLS_RSA_PSK_WITH_AES_256_GCM_SHA384", 173),
    CS_00AE("TLS_PSK_WITH_AES_128_CBC_SHA256", 174),
    CS_00AF("TLS_PSK_WITH_AES_256_CBC_SHA384", 175),
    CS_00B0("TLS_PSK_WITH_NULL_SHA256", 176),
    CS_00B1("TLS_PSK_WITH_NULL_SHA384", 177),
    CS_00B2("TLS_DHE_PSK_WITH_AES_128_CBC_SHA256", 178),
    CS_00B3("TLS_DHE_PSK_WITH_AES_256_CBC_SHA384", 179),
    CS_00B4("TLS_DHE_PSK_WITH_NULL_SHA256", 180),
    CS_00B5("TLS_DHE_PSK_WITH_NULL_SHA384", 181),
    CS_00B6("TLS_RSA_PSK_WITH_AES_128_CBC_SHA256", 182),
    CS_00B7("TLS_RSA_PSK_WITH_AES_256_CBC_SHA384", 183),
    CS_00B8("TLS_RSA_PSK_WITH_NULL_SHA256", 184),
    CS_00B9("TLS_RSA_PSK_WITH_NULL_SHA384", 185),
    CS_0041("TLS_RSA_WITH_CAMELLIA_128_CBC_SHA", 65),
    CS_0042("TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA", 66),
    CS_0043("TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA", 67),
    CS_0044("TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA", 68),
    CS_0045("TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA", 69),
    CS_0046("TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA", 70),
    CS_0084("TLS_RSA_WITH_CAMELLIA_256_CBC_SHA", 132),
    CS_0085("TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA", 133),
    CS_0086("TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA", 134),
    CS_0087("TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA", 135),
    CS_0088("TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA", 136),
    CS_0089("TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA", 137),
    CS_00BA("TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256", 186),
    CS_00BB("TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256", 187),
    CS_00BC("TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA256", 188),
    CS_00BD("TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256", 189),
    CS_00BE("TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256", 190),
    CS_00BF("TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256", 191),
    CS_00C0("TLS_RSA_WITH_CAMELLIA_256_CBC_SHA256", 192),
    CS_00C1("TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256", 193),
    CS_00C2("TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256", 194),
    CS_00C3("TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256", 195),
    CS_00C4("TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256", 196),
    CS_00C5("TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256", 197),
    CS_5600("TLS_FALLBACK_SCSV", 22016),
    CS_C01A("TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA", 49178),
    CS_C01B("TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA", 49179),
    CS_C01C("TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA", 49180),
    CS_C01D("TLS_SRP_SHA_WITH_AES_128_CBC_SHA", 49181),
    CS_C01E("TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA", 49182),
    CS_C01F("TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA", 49183),
    CS_C020("TLS_SRP_SHA_WITH_AES_256_CBC_SHA", 49184),
    CS_C021("TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA", 49185),
    CS_C022("TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA", 49186),
    CS_C033("TLS_ECDHE_PSK_WITH_RC4_128_SHA", 49203),
    CS_C034("TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA", 49204),
    CS_C035("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA", 49205),
    CS_C036("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA", 49206),
    CS_C037("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256", 49207),
    CS_C038("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384", 49208),
    CS_C039("TLS_ECDHE_PSK_WITH_NULL_SHA", 49209),
    CS_C03A("TLS_ECDHE_PSK_WITH_NULL_SHA256", 49210),
    CS_C03B("TLS_ECDHE_PSK_WITH_NULL_SHA384", 49211),
    CS_C03C("TLS_RSA_WITH_ARIA_128_CBC_SHA256", 49212),
    CS_C03D("TLS_RSA_WITH_ARIA_256_CBC_SHA384", 49213),
    CS_C03E("TLS_DH_DSS_WITH_ARIA_128_CBC_SHA256", 49214),
    CS_C03F("TLS_DH_DSS_WITH_ARIA_256_CBC_SHA384", 49215),
    CS_C040("TLS_DH_RSA_WITH_ARIA_128_CBC_SHA256", 49216),
    CS_C041("TLS_DH_RSA_WITH_ARIA_256_CBC_SHA384", 49217),
    CS_C042("TLS_DHE_DSS_WITH_ARIA_128_CBC_SHA256", 49218),
    CS_C043("TLS_DHE_DSS_WITH_ARIA_256_CBC_SHA384", 49219),
    CS_C044("TLS_DHE_RSA_WITH_ARIA_128_CBC_SHA256", 49220),
    CS_C045("TLS_DHE_RSA_WITH_ARIA_256_CBC_SHA384", 49221),
    CS_C046("TLS_DH_anon_WITH_ARIA_128_CBC_SHA256", 49222),
    CS_C047("TLS_DH_anon_WITH_ARIA_256_CBC_SHA384", 49223),
    CS_C048("TLS_ECDHE_ECDSA_WITH_ARIA_128_CBC_SHA256", 49224),
    CS_C049("TLS_ECDHE_ECDSA_WITH_ARIA_256_CBC_SHA384", 49225),
    CS_C04A("TLS_ECDH_ECDSA_WITH_ARIA_128_CBC_SHA256", 49226),
    CS_C04B("TLS_ECDH_ECDSA_WITH_ARIA_256_CBC_SHA384", 49227),
    CS_C04C("TLS_ECDHE_RSA_WITH_ARIA_128_CBC_SHA256", 49228),
    CS_C04D("TLS_ECDHE_RSA_WITH_ARIA_256_CBC_SHA384", 49229),
    CS_C04E("TLS_ECDH_RSA_WITH_ARIA_128_CBC_SHA256", 49230),
    CS_C04F("TLS_ECDH_RSA_WITH_ARIA_256_CBC_SHA384", 49231),
    CS_C050("TLS_RSA_WITH_ARIA_128_GCM_SHA256", 49232),
    CS_C051("TLS_RSA_WITH_ARIA_256_GCM_SHA384", 49233),
    CS_C052("TLS_DHE_RSA_WITH_ARIA_128_GCM_SHA256", 49234),
    CS_C053("TLS_DHE_RSA_WITH_ARIA_256_GCM_SHA384", 49235),
    CS_C054("TLS_DH_RSA_WITH_ARIA_128_GCM_SHA256", 49236),
    CS_C055("TLS_DH_RSA_WITH_ARIA_256_GCM_SHA384", 49237),
    CS_C056("TLS_DHE_DSS_WITH_ARIA_128_GCM_SHA256", 49238),
    CS_C057("TLS_DHE_DSS_WITH_ARIA_256_GCM_SHA384", 49239),
    CS_C058("TLS_DH_DSS_WITH_ARIA_128_GCM_SHA256", 49240),
    CS_C059("TLS_DH_DSS_WITH_ARIA_256_GCM_SHA384", 49241),
    CS_C05A("TLS_DH_anon_WITH_ARIA_128_GCM_SHA256", 49242),
    CS_C05B("TLS_DH_anon_WITH_ARIA_256_GCM_SHA384", 49243),
    CS_C05C("TLS_ECDHE_ECDSA_WITH_ARIA_128_GCM_SHA256", 49244),
    CS_C05D("TLS_ECDHE_ECDSA_WITH_ARIA_256_GCM_SHA384", 49245),
    CS_C05E("TLS_ECDH_ECDSA_WITH_ARIA_128_GCM_SHA256", 49246),
    CS_C05F("TLS_ECDH_ECDSA_WITH_ARIA_256_GCM_SHA384", 49247),
    CS_C060("TLS_ECDHE_RSA_WITH_ARIA_128_GCM_SHA256", 49248),
    CS_C061("TLS_ECDHE_RSA_WITH_ARIA_256_GCM_SHA384", 49249),
    CS_C062("TLS_ECDH_RSA_WITH_ARIA_128_GCM_SHA256", 49250),
    CS_C063("TLS_ECDH_RSA_WITH_ARIA_256_GCM_SHA384", 49251),
    CS_C064("TLS_PSK_WITH_ARIA_128_CBC_SHA256", 49252),
    CS_C065("TLS_PSK_WITH_ARIA_256_CBC_SHA384", 49253),
    CS_C066("TLS_DHE_PSK_WITH_ARIA_128_CBC_SHA256", 49254),
    CS_C067("TLS_DHE_PSK_WITH_ARIA_256_CBC_SHA384", 49255),
    CS_C068("TLS_RSA_PSK_WITH_ARIA_128_CBC_SHA256", 49256),
    CS_C069("TLS_RSA_PSK_WITH_ARIA_256_CBC_SHA384", 49257),
    CS_C06A("TLS_PSK_WITH_ARIA_128_GCM_SHA256", 49258),
    CS_C06B("TLS_PSK_WITH_ARIA_256_GCM_SHA384", 49259),
    CS_C06C("TLS_DHE_PSK_WITH_ARIA_128_GCM_SHA256", 49260),
    CS_C06D("TLS_DHE_PSK_WITH_ARIA_256_GCM_SHA384", 49261),
    CS_C06E("TLS_RSA_PSK_WITH_ARIA_128_GCM_SHA256", 49262),
    CS_C06F("TLS_RSA_PSK_WITH_ARIA_256_GCM_SHA384", 49263),
    CS_C070("TLS_ECDHE_PSK_WITH_ARIA_128_CBC_SHA256", 49264),
    CS_C071("TLS_ECDHE_PSK_WITH_ARIA_256_CBC_SHA384", 49265),
    CS_C072("TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_CBC_SHA256", 49266),
    CS_C073("TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_CBC_SHA384", 49267),
    CS_C074("TLS_ECDH_ECDSA_WITH_CAMELLIA_128_CBC_SHA256", 49268),
    CS_C075("TLS_ECDH_ECDSA_WITH_CAMELLIA_256_CBC_SHA384", 49269),
    CS_C076("TLS_ECDHE_RSA_WITH_CAMELLIA_128_CBC_SHA256", 49270),
    CS_C077("TLS_ECDHE_RSA_WITH_CAMELLIA_256_CBC_SHA384", 49271),
    CS_C078("TLS_ECDH_RSA_WITH_CAMELLIA_128_CBC_SHA256", 49272),
    CS_C079("TLS_ECDH_RSA_WITH_CAMELLIA_256_CBC_SHA384", 49273),
    CS_C07A("TLS_RSA_WITH_CAMELLIA_128_GCM_SHA256", 49274),
    CS_C07B("TLS_RSA_WITH_CAMELLIA_256_GCM_SHA384", 49275),
    CS_C07C("TLS_DHE_RSA_WITH_CAMELLIA_128_GCM_SHA256", 49276),
    CS_C07D("TLS_DHE_RSA_WITH_CAMELLIA_256_GCM_SHA384", 49277),
    CS_C07E("TLS_DH_RSA_WITH_CAMELLIA_128_GCM_SHA256", 49278),
    CS_C07F("TLS_DH_RSA_WITH_CAMELLIA_256_GCM_SHA384", 49279),
    CS_C080("TLS_DHE_DSS_WITH_CAMELLIA_128_GCM_SHA256", 49280),
    CS_C081("TLS_DHE_DSS_WITH_CAMELLIA_256_GCM_SHA384", 49281),
    CS_C082("TLS_DH_DSS_WITH_CAMELLIA_128_GCM_SHA256", 49282),
    CS_C083("TLS_DH_DSS_WITH_CAMELLIA_256_GCM_SHA384", 49283),
    CS_C084("TLS_DH_anon_WITH_CAMELLIA_128_GCM_SHA256", 49284),
    CS_C085("TLS_DH_anon_WITH_CAMELLIA_256_GCM_SHA384", 49285),
    CS_C086("TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_GCM_SHA256", 49286),
    CS_C087("TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_GCM_SHA384", 49287),
    CS_C088("TLS_ECDH_ECDSA_WITH_CAMELLIA_128_GCM_SHA256", 49288),
    CS_C089("TLS_ECDH_ECDSA_WITH_CAMELLIA_256_GCM_SHA384", 49289),
    CS_C08A("TLS_ECDHE_RSA_WITH_CAMELLIA_128_GCM_SHA256", 49290),
    CS_C08B("TLS_ECDHE_RSA_WITH_CAMELLIA_256_GCM_SHA384", 49291),
    CS_C08C("TLS_ECDH_RSA_WITH_CAMELLIA_128_GCM_SHA256", 49292),
    CS_C08D("TLS_ECDH_RSA_WITH_CAMELLIA_256_GCM_SHA384", 49293),
    CS_C08E("TLS_PSK_WITH_CAMELLIA_128_GCM_SHA256", 49294),
    CS_C08F("TLS_PSK_WITH_CAMELLIA_256_GCM_SHA384", 49295),
    CS_C090("TLS_DHE_PSK_WITH_CAMELLIA_128_GCM_SHA256", 49296),
    CS_C091("TLS_DHE_PSK_WITH_CAMELLIA_256_GCM_SHA384", 49297),
    CS_C092("TLS_RSA_PSK_WITH_CAMELLIA_128_GCM_SHA256", 49298),
    CS_C093("TLS_RSA_PSK_WITH_CAMELLIA_256_GCM_SHA384", 49299),
    CS_C094("TLS_PSK_WITH_CAMELLIA_128_CBC_SHA256", 49300),
    CS_C095("TLS_PSK_WITH_CAMELLIA_256_CBC_SHA384", 49301),
    CS_C096("TLS_DHE_PSK_WITH_CAMELLIA_128_CBC_SHA256", 49302),
    CS_C097("TLS_DHE_PSK_WITH_CAMELLIA_256_CBC_SHA384", 49303),
    CS_C098("TLS_RSA_PSK_WITH_CAMELLIA_128_CBC_SHA256", 49304),
    CS_C099("TLS_RSA_PSK_WITH_CAMELLIA_256_CBC_SHA384", 49305),
    CS_C09A("TLS_ECDHE_PSK_WITH_CAMELLIA_128_CBC_SHA256", 49306),
    CS_C09B("TLS_ECDHE_PSK_WITH_CAMELLIA_256_CBC_SHA384", 49307),
    CS_C09C("TLS_RSA_WITH_AES_128_CCM", 49308),
    CS_C09D("TLS_RSA_WITH_AES_256_CCM", 49309),
    CS_C09E("TLS_DHE_RSA_WITH_AES_128_CCM", 49310),
    CS_C09F("TLS_DHE_RSA_WITH_AES_256_CCM", 49311),
    CS_C0A0("TLS_RSA_WITH_AES_128_CCM_8", 49312),
    CS_C0A1("TLS_RSA_WITH_AES_256_CCM_8", 49313),
    CS_C0A2("TLS_DHE_RSA_WITH_AES_128_CCM_8", 49314),
    CS_C0A3("TLS_DHE_RSA_WITH_AES_256_CCM_8", 49315),
    CS_C0A4("TLS_PSK_WITH_AES_128_CCM", 49316),
    CS_C0A5("TLS_PSK_WITH_AES_256_CCM", 49317),
    CS_C0A6("TLS_DHE_PSK_WITH_AES_128_CCM", 49318),
    CS_C0A7("TLS_DHE_PSK_WITH_AES_256_CCM", 49319),
    CS_C0A8("TLS_PSK_WITH_AES_128_CCM_8", 49320),
    CS_C0A9("TLS_PSK_WITH_AES_256_CCM_8", 49321),
    CS_C0AA("TLS_PSK_DHE_WITH_AES_128_CCM_8", 49322),
    CS_C0AB("TLS_PSK_DHE_WITH_AES_256_CCM_8", 49323),
    CS_C0AC("TLS_ECDHE_ECDSA_WITH_AES_128_CCM", 49324),
    CS_C0AD("TLS_ECDHE_ECDSA_WITH_AES_256_CCM", 49325),
    CS_C0AE("TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8", 49326),
    CS_C0AF("TLS_ECDHE_ECDSA_WITH_AES_256_CCM_8", 49327),
    C_NULL("SSL_NULL_WITH_NULL_NULL", 0);

    final int id;
    final boolean isDefaultEnabled;
    final String name;
    final List<String> aliases;
    final List<ProtocolVersion> supportedProtocols;
    final KeyExchange keyExchange;
    final SSLCipher bulkCipher;
    final MacAlg macAlg;
    final HashAlg hashAlg;
    final boolean exportable;
    private static final Map<Integer, CipherSuite> cipherSuiteIds;
    private static final Map<String, CipherSuite> cipherSuiteNames;
    private static final List<CipherSuite> allowedCipherSuites;
    private static final List<CipherSuite> defaultCipherSuites;

    private CipherSuite(String name, int id) {
        this(id, false, name, "", ProtocolVersion.PROTOCOLS_EMPTY, null, null, null, null);
    }

    private CipherSuite(int id, boolean isDefaultEnabled, String name, ProtocolVersion[] supportedProtocols, SSLCipher bulkCipher, HashAlg hashAlg) {
        this(id, isDefaultEnabled, name, "", supportedProtocols, null, bulkCipher, MacAlg.M_NULL, hashAlg);
    }

    private CipherSuite(int id, boolean isDefaultEnabled, String name, String aliases, ProtocolVersion[] supportedProtocols, KeyExchange keyExchange, SSLCipher cipher, MacAlg macAlg, HashAlg hashAlg) {
        this.id = id;
        this.isDefaultEnabled = isDefaultEnabled;
        this.name = name;
        this.aliases = !aliases.isEmpty() ? Arrays.asList(aliases.split(",")) : Collections.emptyList();
        this.supportedProtocols = Arrays.asList(supportedProtocols);
        this.keyExchange = keyExchange;
        this.bulkCipher = cipher;
        this.macAlg = macAlg;
        this.hashAlg = hashAlg;
        this.exportable = cipher != null && cipher.exportable;
    }

    static CipherSuite nameOf(String ciperSuiteName) {
        return cipherSuiteNames.get(ciperSuiteName);
    }

    static CipherSuite valueOf(int id) {
        return cipherSuiteIds.get(id);
    }

    static String nameOf(int id) {
        CipherSuite cs = cipherSuiteIds.get(id);
        if (cs != null) {
            return cs.name;
        }
        return "UNKNOWN-CIPHER-SUITE(" + Utilities.byte16HexString(id) + ")";
    }

    static Collection<CipherSuite> allowedCipherSuites() {
        return allowedCipherSuites;
    }

    static Collection<CipherSuite> defaultCipherSuites() {
        return defaultCipherSuites;
    }

    static List<CipherSuite> validValuesOf(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("CipherSuites cannot be null");
        }
        ArrayList<CipherSuite> cipherSuites = new ArrayList<CipherSuite>(names.length);
        for (String name : names) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("The specified CipherSuites array contains invalid null or empty string elements");
            }
            boolean found = false;
            CipherSuite cs = cipherSuiteNames.get(name);
            if (cs != null && !cs.supportedProtocols.isEmpty()) {
                cipherSuites.add(cs);
                found = true;
            }
            if (found) continue;
            throw new IllegalArgumentException("Unsupported CipherSuite: " + name);
        }
        return Collections.unmodifiableList(cipherSuites);
    }

    static String[] namesOf(List<CipherSuite> cipherSuites) {
        String[] names = new String[cipherSuites.size()];
        int i = 0;
        for (CipherSuite cipherSuite : cipherSuites) {
            names[i++] = cipherSuite.name;
        }
        return names;
    }

    boolean isAvailable() {
        return !this.supportedProtocols.isEmpty() && (this.keyExchange == null || this.keyExchange.isAvailable()) && this.bulkCipher != null && this.bulkCipher.isAvailable();
    }

    public boolean supports(ProtocolVersion protocolVersion) {
        return this.supportedProtocols.contains((Object)protocolVersion);
    }

    boolean isNegotiable() {
        return this != TLS_EMPTY_RENEGOTIATION_INFO_SCSV && this.isAvailable();
    }

    boolean isAnonymous() {
        return this.keyExchange != null && this.keyExchange.isAnonymous;
    }

    int calculatePacketSize(int fragmentSize, ProtocolVersion protocolVersion, boolean isDTLS) {
        int packetSize = fragmentSize;
        if (this.bulkCipher != null && this.bulkCipher != SSLCipher.B_NULL) {
            int blockSize = this.bulkCipher.ivSize;
            switch (this.bulkCipher.cipherType) {
                case BLOCK_CIPHER: {
                    packetSize += this.macAlg.size;
                    ++packetSize;
                    packetSize += (blockSize - packetSize % blockSize) % blockSize;
                    if (!protocolVersion.useTLS11PlusSpec()) break;
                    packetSize += blockSize;
                    break;
                }
                case AEAD_CIPHER: {
                    if (protocolVersion == ProtocolVersion.TLS12 || protocolVersion == ProtocolVersion.DTLS12) {
                        packetSize += this.bulkCipher.ivSize - this.bulkCipher.fixedIvSize;
                    }
                    packetSize += this.bulkCipher.tagSize;
                    break;
                }
                default: {
                    packetSize += this.macAlg.size;
                }
            }
        }
        return packetSize + (isDTLS ? 13 : 5);
    }

    int calculateFragSize(int packetLimit, ProtocolVersion protocolVersion, boolean isDTLS) {
        int fragSize = packetLimit - (isDTLS ? 13 : 5);
        if (this.bulkCipher != null && this.bulkCipher != SSLCipher.B_NULL) {
            int blockSize = this.bulkCipher.ivSize;
            switch (this.bulkCipher.cipherType) {
                case BLOCK_CIPHER: {
                    if (protocolVersion.useTLS11PlusSpec()) {
                        fragSize -= blockSize;
                    }
                    fragSize -= fragSize % blockSize;
                    --fragSize;
                    fragSize -= this.macAlg.size;
                    break;
                }
                case AEAD_CIPHER: {
                    fragSize -= this.bulkCipher.tagSize;
                    fragSize -= this.bulkCipher.ivSize - this.bulkCipher.fixedIvSize;
                    break;
                }
                default: {
                    fragSize -= this.macAlg.size;
                }
            }
        }
        return fragSize;
    }

    static {
        HashMap<Integer, CipherSuite> ids = new HashMap<Integer, CipherSuite>();
        HashMap<String, CipherSuite> names = new HashMap<String, CipherSuite>();
        ArrayList<CipherSuite> allowedCS = new ArrayList<CipherSuite>();
        ArrayList<CipherSuite> defaultCS = new ArrayList<CipherSuite>();
        for (CipherSuite cs : CipherSuite.values()) {
            ids.put(cs.id, cs);
            names.put(cs.name, cs);
            for (String alias : cs.aliases) {
                names.put(alias, cs);
            }
            if (!cs.supportedProtocols.isEmpty()) {
                allowedCS.add(cs);
            }
            if (!cs.isDefaultEnabled) continue;
            defaultCS.add(cs);
        }
        cipherSuiteIds = Map.copyOf(ids);
        cipherSuiteNames = Map.copyOf(names);
        allowedCipherSuites = List.copyOf(allowedCS);
        defaultCipherSuites = List.copyOf(defaultCS);
    }

    static enum KeyExchange {
        K_NULL("NULL", false, true, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_RSA("RSA", true, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_RSA_EXPORT("RSA_EXPORT", true, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_DH_RSA("DH_RSA", false, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_DH_DSS("DH_DSS", false, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_DHE_DSS("DHE_DSS", true, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_FFDHE),
        K_DHE_DSS_EXPORT("DHE_DSS_EXPORT", true, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_DHE_RSA("DHE_RSA", true, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_FFDHE),
        K_DHE_RSA_EXPORT("DHE_RSA_EXPORT", true, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_DH_ANON("DH_anon", true, true, NamedGroup.NamedGroupSpec.NAMED_GROUP_FFDHE),
        K_DH_ANON_EXPORT("DH_anon_EXPORT", true, true, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE),
        K_ECDH_ECDSA("ECDH_ECDSA", JsseJce.ALLOW_ECC, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE, NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH),
        K_ECDH_RSA("ECDH_RSA", JsseJce.ALLOW_ECC, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE, NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH),
        K_ECDHE_ECDSA("ECDHE_ECDSA", JsseJce.ALLOW_ECC, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE, NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH),
        K_ECDHE_RSA("ECDHE_RSA", JsseJce.ALLOW_ECC, false, NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE, NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH),
        K_ECDH_ANON("ECDH_anon", JsseJce.ALLOW_ECC, true, NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE, NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH),
        K_SCSV("SCSV", true, true, NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE);

        final String name;
        final boolean allowed;
        final NamedGroup.NamedGroupSpec[] groupTypes;
        private final boolean alwaysAvailable;
        private final boolean isAnonymous;

        private KeyExchange(String name, boolean allowed, boolean isAnonymous, NamedGroup.NamedGroupSpec ... groupTypes) {
            this.name = name;
            this.groupTypes = groupTypes;
            this.allowed = allowed;
            this.alwaysAvailable = allowed && !name.startsWith("EC");
            this.isAnonymous = isAnonymous;
        }

        boolean isAvailable() {
            if (this.alwaysAvailable) {
                return true;
            }
            if (NamedGroup.NamedGroupSpec.arrayContains(this.groupTypes, NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE)) {
                return this.allowed && JsseJce.isEcAvailable();
            }
            return this.allowed;
        }

        public String toString() {
            return this.name;
        }
    }

    static enum MacAlg {
        M_NULL("NULL", 0, 0, 0),
        M_MD5("MD5", 16, 64, 9),
        M_SHA("SHA", 20, 64, 9),
        M_SHA256("SHA256", 32, 64, 9),
        M_SHA384("SHA384", 48, 128, 17);

        final String name;
        final int size;
        final int hashBlockSize;
        final int minimalPaddingSize;

        private MacAlg(String name, int size, int hashBlockSize, int minimalPaddingSize) {
            this.name = name;
            this.size = size;
            this.hashBlockSize = hashBlockSize;
            this.minimalPaddingSize = minimalPaddingSize;
        }

        public String toString() {
            return this.name;
        }
    }

    static enum HashAlg {
        H_NONE("NONE", 0, 0),
        H_SHA256("SHA-256", 32, 64),
        H_SHA384("SHA-384", 48, 128);

        final String name;
        final int hashLength;
        final int blockSize;

        private HashAlg(String hashAlg, int hashLength, int blockSize) {
            this.name = hashAlg;
            this.hashLength = hashLength;
            this.blockSize = blockSize;
        }

        public String toString() {
            return this.name;
        }
    }
}

