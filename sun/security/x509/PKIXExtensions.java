/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;

public class PKIXExtensions {
    public static final ObjectIdentifier AuthorityKey_Id = ObjectIdentifier.of(KnownOIDs.AuthorityKeyID);
    public static final ObjectIdentifier SubjectKey_Id = ObjectIdentifier.of(KnownOIDs.SubjectKeyID);
    public static final ObjectIdentifier KeyUsage_Id = ObjectIdentifier.of(KnownOIDs.KeyUsage);
    public static final ObjectIdentifier PrivateKeyUsage_Id = ObjectIdentifier.of(KnownOIDs.PrivateKeyUsage);
    public static final ObjectIdentifier CertificatePolicies_Id = ObjectIdentifier.of(KnownOIDs.CertificatePolicies);
    public static final ObjectIdentifier PolicyMappings_Id = ObjectIdentifier.of(KnownOIDs.PolicyMappings);
    public static final ObjectIdentifier SubjectAlternativeName_Id = ObjectIdentifier.of(KnownOIDs.SubjectAlternativeName);
    public static final ObjectIdentifier IssuerAlternativeName_Id = ObjectIdentifier.of(KnownOIDs.IssuerAlternativeName);
    public static final ObjectIdentifier SubjectDirectoryAttributes_Id = ObjectIdentifier.of(KnownOIDs.SubjectDirectoryAttributes);
    public static final ObjectIdentifier BasicConstraints_Id = ObjectIdentifier.of(KnownOIDs.BasicConstraints);
    public static final ObjectIdentifier NameConstraints_Id = ObjectIdentifier.of(KnownOIDs.NameConstraints);
    public static final ObjectIdentifier PolicyConstraints_Id = ObjectIdentifier.of(KnownOIDs.PolicyConstraints);
    public static final ObjectIdentifier CRLDistributionPoints_Id = ObjectIdentifier.of(KnownOIDs.CRLDistributionPoints);
    public static final ObjectIdentifier CRLNumber_Id = ObjectIdentifier.of(KnownOIDs.CRLNumber);
    public static final ObjectIdentifier IssuingDistributionPoint_Id = ObjectIdentifier.of(KnownOIDs.IssuingDistributionPoint);
    public static final ObjectIdentifier DeltaCRLIndicator_Id = ObjectIdentifier.of(KnownOIDs.DeltaCRLIndicator);
    public static final ObjectIdentifier ReasonCode_Id = ObjectIdentifier.of(KnownOIDs.ReasonCode);
    public static final ObjectIdentifier HoldInstructionCode_Id = ObjectIdentifier.of(KnownOIDs.HoldInstructionCode);
    public static final ObjectIdentifier InvalidityDate_Id = ObjectIdentifier.of(KnownOIDs.InvalidityDate);
    public static final ObjectIdentifier ExtendedKeyUsage_Id = ObjectIdentifier.of(KnownOIDs.extendedKeyUsage);
    public static final ObjectIdentifier InhibitAnyPolicy_Id = ObjectIdentifier.of(KnownOIDs.InhibitAnyPolicy);
    public static final ObjectIdentifier CertificateIssuer_Id = ObjectIdentifier.of(KnownOIDs.CertificateIssuer);
    public static final ObjectIdentifier AuthInfoAccess_Id = ObjectIdentifier.of(KnownOIDs.AuthInfoAccess);
    public static final ObjectIdentifier SubjectInfoAccess_Id = ObjectIdentifier.of(KnownOIDs.SubjectInfoAccess);
    public static final ObjectIdentifier FreshestCRL_Id = ObjectIdentifier.of(KnownOIDs.FreshestCRL);
    public static final ObjectIdentifier OCSPNoCheck_Id = ObjectIdentifier.of(KnownOIDs.OCSPNoCheck);
    public static final ObjectIdentifier OCSPNonce_Id = ObjectIdentifier.of(KnownOIDs.OCSPNonceExt);
}

