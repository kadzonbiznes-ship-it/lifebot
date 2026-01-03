/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class X509CertPath
extends CertPath {
    private static final long serialVersionUID = 4989800333263052980L;
    private final List<X509Certificate> certs;
    private static final String COUNT_ENCODING = "count";
    private static final String PKCS7_ENCODING = "PKCS7";
    private static final String PKIPATH_ENCODING = "PkiPath";
    private static final Collection<String> encodingList;

    public X509CertPath(List<? extends Certificate> certs) throws CertificateException {
        super("X.509");
        for (Certificate certificate : certs) {
            if (certificate instanceof X509Certificate) continue;
            throw new CertificateException("List is not all X509Certificates: " + certificate.getClass().getName());
        }
        this.certs = Collections.unmodifiableList(new ArrayList<Certificate>(certs));
    }

    public X509CertPath(InputStream is) throws CertificateException {
        this(is, PKIPATH_ENCODING);
    }

    public X509CertPath(InputStream is, String encoding) throws CertificateException {
        super("X.509");
        switch (encoding) {
            case "PkiPath": {
                this.certs = X509CertPath.parsePKIPATH(is);
                break;
            }
            case "PKCS7": {
                this.certs = X509CertPath.parsePKCS7(is);
                break;
            }
            default: {
                throw new CertificateException("unsupported encoding");
            }
        }
    }

    private static List<X509Certificate> parsePKIPATH(InputStream is) throws CertificateException {
        if (is == null) {
            throw new CertificateException("input stream is null");
        }
        try {
            DerInputStream dis = new DerInputStream(is.readAllBytes());
            DerValue[] seq = dis.getSequence(3);
            if (seq.length == 0) {
                return Collections.emptyList();
            }
            CertificateFactory certFac = CertificateFactory.getInstance("X.509");
            ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>(seq.length);
            for (int i = seq.length - 1; i >= 0; --i) {
                certList.add((X509Certificate)certFac.generateCertificate(new ByteArrayInputStream(seq[i].toByteArray())));
            }
            return Collections.unmodifiableList(certList);
        }
        catch (IOException ioe) {
            throw new CertificateException("IOException parsing PkiPath data: " + ioe, ioe);
        }
    }

    private static List<X509Certificate> parsePKCS7(InputStream is) throws CertificateException {
        List<X509Certificate> certList;
        if (is == null) {
            throw new CertificateException("input stream is null");
        }
        try {
            PKCS7 pkcs7;
            X509Certificate[] certArray;
            if (!is.markSupported()) {
                is = new ByteArrayInputStream(is.readAllBytes());
            }
            certList = (certArray = (pkcs7 = new PKCS7(is)).getCertificates()) != null ? Arrays.asList(certArray) : new ArrayList<X509Certificate>(0);
        }
        catch (IOException ioe) {
            throw new CertificateException("IOException parsing PKCS7 data: " + ioe);
        }
        return Collections.unmodifiableList(certList);
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return this.encodePKIPATH();
    }

    private byte[] encodePKIPATH() throws CertificateEncodingException {
        ListIterator<X509Certificate> li = this.certs.listIterator(this.certs.size());
        try {
            DerOutputStream bytes = new DerOutputStream();
            while (li.hasPrevious()) {
                X509Certificate cert = li.previous();
                if (this.certs.lastIndexOf(cert) != this.certs.indexOf(cert)) {
                    throw new CertificateEncodingException("Duplicate Certificate");
                }
                byte[] encoded = cert.getEncoded();
                bytes.write(encoded);
            }
            DerOutputStream derout = new DerOutputStream();
            derout.write((byte)48, bytes);
            return derout.toByteArray();
        }
        catch (IOException ioe) {
            throw new CertificateEncodingException("IOException encoding PkiPath data: " + ioe, ioe);
        }
    }

    private byte[] encodePKCS7() throws CertificateEncodingException {
        PKCS7 p7 = new PKCS7(new AlgorithmId[0], new ContentInfo(ContentInfo.DATA_OID, null), this.certs.toArray(new X509Certificate[0]), new SignerInfo[0]);
        DerOutputStream derout = new DerOutputStream();
        try {
            p7.encodeSignedData(derout);
        }
        catch (IOException ioe) {
            throw new CertificateEncodingException(ioe.getMessage());
        }
        return derout.toByteArray();
    }

    @Override
    public byte[] getEncoded(String encoding) throws CertificateEncodingException {
        switch (encoding) {
            case "PkiPath": {
                return this.encodePKIPATH();
            }
            case "PKCS7": {
                return this.encodePKCS7();
            }
        }
        throw new CertificateEncodingException("unsupported encoding");
    }

    public static Iterator<String> getEncodingsStatic() {
        return encodingList.iterator();
    }

    @Override
    public Iterator<String> getEncodings() {
        return X509CertPath.getEncodingsStatic();
    }

    public List<X509Certificate> getCertificates() {
        return this.certs;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("X509CertPaths are not directly deserializable");
    }

    static {
        ArrayList<String> list = new ArrayList<String>(2);
        list.add(PKIPATH_ENCODING);
        list.add(PKCS7_ENCODING);
        encodingList = Collections.unmodifiableCollection(list);
    }
}

