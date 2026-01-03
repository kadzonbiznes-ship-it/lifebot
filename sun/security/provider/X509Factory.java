/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;
import sun.security.provider.certpath.X509CertPath;
import sun.security.provider.certpath.X509CertificatePair;
import sun.security.util.Cache;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public class X509Factory
extends CertificateFactorySpi {
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    private static final int ENC_MAX_LENGTH = 0x400000;
    private static final Cache<Object, X509CertImpl> certCache = Cache.newSoftMemoryCache(750);
    private static final Cache<Object, X509CRLImpl> crlCache = Cache.newSoftMemoryCache(750);

    @Override
    public Certificate engineGenerateCertificate(InputStream is) throws CertificateException {
        if (is == null) {
            certCache.clear();
            X509CertificatePair.clearCache();
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] encoding = X509Factory.readOneBlock(is);
            if (encoding != null) {
                return X509Factory.cachedGetX509Cert(encoding);
            }
            throw new IOException("Empty input");
        }
        catch (IOException ioe) {
            throw new CertificateException("Could not parse certificate: " + ioe.toString(), ioe);
        }
    }

    public static X509CertImpl cachedGetX509Cert(byte[] encoding) throws CertificateException {
        X509CertImpl cert = X509Factory.getFromCache(certCache, encoding);
        if (cert != null) {
            return cert;
        }
        cert = new X509CertImpl(encoding);
        X509Factory.addToCache(certCache, cert.getEncodedInternal(), cert);
        return cert;
    }

    private static int readFully(InputStream in, ByteArrayOutputStream bout, int length) throws IOException {
        int n;
        int read = 0;
        byte[] buffer = new byte[2048];
        while (length > 0 && (n = in.read(buffer, 0, Math.min(length, 2048))) > 0) {
            bout.write(buffer, 0, n);
            read += n;
            length -= n;
        }
        return read;
    }

    public static synchronized X509CertImpl intern(X509Certificate c) throws CertificateException {
        if (c == null) {
            return null;
        }
        boolean isImpl = c instanceof X509CertImpl;
        byte[] encoding = isImpl ? ((X509CertImpl)c).getEncodedInternal() : c.getEncoded();
        X509CertImpl newC = X509Factory.getFromCache(certCache, encoding);
        if (newC != null) {
            return newC;
        }
        if (isImpl) {
            newC = (X509CertImpl)c;
        } else {
            newC = new X509CertImpl(encoding);
            encoding = newC.getEncodedInternal();
        }
        X509Factory.addToCache(certCache, encoding, newC);
        return newC;
    }

    public static synchronized X509CRLImpl intern(X509CRL c) throws CRLException {
        if (c == null) {
            return null;
        }
        boolean isImpl = c instanceof X509CRLImpl;
        byte[] encoding = isImpl ? ((X509CRLImpl)c).getEncodedInternal() : c.getEncoded();
        X509CRLImpl newC = X509Factory.getFromCache(crlCache, encoding);
        if (newC != null) {
            return newC;
        }
        if (isImpl) {
            newC = (X509CRLImpl)c;
        } else {
            newC = new X509CRLImpl(encoding);
            encoding = newC.getEncodedInternal();
        }
        X509Factory.addToCache(crlCache, encoding, newC);
        return newC;
    }

    private static synchronized <K, V> V getFromCache(Cache<K, V> cache, byte[] encoding) {
        Cache.EqualByteArray key = new Cache.EqualByteArray(encoding);
        return cache.get(key);
    }

    private static synchronized <V> void addToCache(Cache<Object, V> cache, byte[] encoding, V value) {
        if (encoding.length > 0x400000) {
            return;
        }
        Cache.EqualByteArray key = new Cache.EqualByteArray(encoding);
        cache.put(key, value);
    }

    @Override
    public CertPath engineGenerateCertPath(InputStream inStream) throws CertificateException {
        if (inStream == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] encoding = X509Factory.readOneBlock(inStream);
            if (encoding != null) {
                return new X509CertPath(new ByteArrayInputStream(encoding));
            }
            throw new IOException("Empty input");
        }
        catch (IOException ioe) {
            throw new CertificateException(ioe.getMessage());
        }
    }

    @Override
    public CertPath engineGenerateCertPath(InputStream inStream, String encoding) throws CertificateException {
        if (inStream == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] data = X509Factory.readOneBlock(inStream);
            if (data != null) {
                return new X509CertPath(new ByteArrayInputStream(data), encoding);
            }
            throw new IOException("Empty input");
        }
        catch (IOException ioe) {
            throw new CertificateException(ioe.getMessage());
        }
    }

    @Override
    public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) throws CertificateException {
        return new X509CertPath(certificates);
    }

    @Override
    public Iterator<String> engineGetCertPathEncodings() {
        return X509CertPath.getEncodingsStatic();
    }

    @Override
    public Collection<? extends Certificate> engineGenerateCertificates(InputStream is) throws CertificateException {
        if (is == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            return this.parseX509orPKCS7Cert(is);
        }
        catch (IOException ioe) {
            throw new CertificateException(ioe);
        }
    }

    @Override
    public CRL engineGenerateCRL(InputStream is) throws CRLException {
        if (is == null) {
            crlCache.clear();
            throw new CRLException("Missing input stream");
        }
        try {
            byte[] encoding = X509Factory.readOneBlock(is);
            if (encoding != null) {
                X509CRLImpl crl = X509Factory.getFromCache(crlCache, encoding);
                if (crl != null) {
                    return crl;
                }
                crl = new X509CRLImpl(encoding);
                X509Factory.addToCache(crlCache, crl.getEncodedInternal(), crl);
                return crl;
            }
            throw new IOException("Empty input");
        }
        catch (IOException ioe) {
            throw new CRLException(ioe.getMessage());
        }
    }

    @Override
    public Collection<? extends CRL> engineGenerateCRLs(InputStream is) throws CRLException {
        if (is == null) {
            throw new CRLException("Missing input stream");
        }
        try {
            return this.parseX509orPKCS7CRL(is);
        }
        catch (IOException ioe) {
            throw new CRLException(ioe.getMessage());
        }
    }

    private Collection<? extends Certificate> parseX509orPKCS7Cert(InputStream is) throws CertificateException, IOException {
        PushbackInputStream pbis = new PushbackInputStream(is);
        ArrayList<X509CertImpl> coll = new ArrayList<X509CertImpl>();
        int peekByte = pbis.read();
        if (peekByte == -1) {
            return new ArrayList(0);
        }
        pbis.unread(peekByte);
        byte[] data = X509Factory.readOneBlock(pbis);
        if (data == null) {
            throw new CertificateException("No certificate data found");
        }
        try {
            PKCS7 pkcs7 = new PKCS7(data);
            X509Certificate[] certs = pkcs7.getCertificates();
            if (certs != null) {
                return Arrays.asList(certs);
            }
            return new ArrayList(0);
        }
        catch (ParsingException e) {
            while (data != null) {
                coll.add(X509CertImpl.newX509CertImpl(data));
                data = X509Factory.readOneBlock(pbis);
            }
            return coll;
        }
    }

    private Collection<? extends CRL> parseX509orPKCS7CRL(InputStream is) throws CRLException, IOException {
        PushbackInputStream pbis = new PushbackInputStream(is);
        ArrayList<X509CRLImpl> coll = new ArrayList<X509CRLImpl>();
        int peekByte = pbis.read();
        if (peekByte == -1) {
            return new ArrayList(0);
        }
        pbis.unread(peekByte);
        byte[] data = X509Factory.readOneBlock(pbis);
        if (data == null) {
            throw new CRLException("No CRL data found");
        }
        try {
            PKCS7 pkcs7 = new PKCS7(data);
            X509CRL[] crls = pkcs7.getCRLs();
            if (crls != null) {
                return Arrays.asList(crls);
            }
            return new ArrayList(0);
        }
        catch (ParsingException e) {
            while (data != null) {
                coll.add(new X509CRLImpl(data));
                data = X509Factory.readOneBlock(pbis);
            }
            return coll;
        }
    }

    private static byte[] readOneBlock(InputStream is) throws IOException {
        int next;
        int end;
        int next2;
        int last;
        int c = is.read();
        if (c == -1) {
            return null;
        }
        if (c == 48) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
            bout.write(c);
            X509Factory.readBERInternal(is, bout, c);
            return bout.toByteArray();
        }
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int hyphen = c == 45 ? 1 : 0;
        int n = last = c == 45 ? -1 : c;
        do {
            int next3;
            if ((next3 = is.read()) == -1) {
                return null;
            }
            if (next3 == 45) {
                ++hyphen;
                continue;
            }
            hyphen = 0;
            last = next3;
        } while (hyphen != 5 || last != -1 && last != 13 && last != 10);
        StringBuilder header = new StringBuilder("-----");
        while (true) {
            if ((next2 = is.read()) == -1) {
                throw new IOException("Incomplete data");
            }
            if (next2 == 10) {
                end = 10;
                break;
            }
            if (next2 == 13) {
                next2 = is.read();
                if (next2 == -1) {
                    throw new IOException("Incomplete data");
                }
                if (next2 == 10) {
                    end = 10;
                    break;
                }
                end = 13;
                if (next2 == 9 || next2 == 10 || next2 == 13 || next2 == 32) break;
                data.write(next2);
                break;
            }
            header.append((char)next2);
        }
        while (true) {
            if ((next2 = is.read()) == -1) {
                throw new IOException("Incomplete data");
            }
            if (next2 == 45) break;
            if (next2 == 9 || next2 == 10 || next2 == 13 || next2 == 32) continue;
            data.write(next2);
        }
        StringBuilder footer = new StringBuilder("-");
        while ((next = is.read()) != -1 && next != end && next != 10) {
            if (next == 13) continue;
            footer.append((char)next);
        }
        X509Factory.checkHeaderFooter(header.toString().stripTrailing(), footer.toString().stripTrailing());
        try {
            return Base64.getDecoder().decode(data.toByteArray());
        }
        catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    private static void checkHeaderFooter(String header, String footer) throws IOException {
        String footerType;
        if (header.length() < 16 || !header.startsWith("-----BEGIN ") || !header.endsWith("-----")) {
            throw new IOException("Illegal header: " + header);
        }
        if (footer.length() < 14 || !footer.startsWith("-----END ") || !footer.endsWith("-----")) {
            throw new IOException("Illegal footer: " + footer);
        }
        String headerType = header.substring(11, header.length() - 5);
        if (!headerType.equals(footerType = footer.substring(9, footer.length() - 5))) {
            throw new IOException("Header and footer do not match: " + header + " " + footer);
        }
    }

    private static int readBERInternal(InputStream is, ByteArrayOutputStream bout, int tag) throws IOException {
        int n;
        if (tag == -1) {
            tag = is.read();
            if (tag == -1) {
                throw new IOException("BER/DER tag info absent");
            }
            if ((tag & 0x1F) == 31) {
                throw new IOException("Multi octets tag not supported");
            }
            bout.write(tag);
        }
        if ((n = is.read()) == -1) {
            throw new IOException("BER/DER length info absent");
        }
        bout.write(n);
        if (n == 128) {
            int subTag;
            if ((tag & 0x20) != 32) {
                throw new IOException("Non constructed encoding must have definite length");
            }
            while ((subTag = X509Factory.readBERInternal(is, bout, -1)) != 0) {
            }
        } else {
            int length;
            if (n < 128) {
                length = n;
            } else if (n == 129) {
                length = is.read();
                if (length == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(length);
            } else if (n == 130) {
                int highByte = is.read();
                int lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(highByte);
                bout.write(lowByte);
                length = highByte << 8 | lowByte;
            } else if (n == 131) {
                int highByte = is.read();
                int midByte = is.read();
                int lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(highByte);
                bout.write(midByte);
                bout.write(lowByte);
                length = highByte << 16 | midByte << 8 | lowByte;
            } else if (n == 132) {
                int highByte = is.read();
                int nextByte = is.read();
                int midByte = is.read();
                int lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                if (highByte > 127) {
                    throw new IOException("Invalid BER/DER data (a little huge?)");
                }
                bout.write(highByte);
                bout.write(nextByte);
                bout.write(midByte);
                bout.write(lowByte);
                length = highByte << 24 | nextByte << 16 | midByte << 8 | lowByte;
            } else {
                throw new IOException("Invalid BER/DER data (too huge?)");
            }
            if (X509Factory.readFully(is, bout, length) != length) {
                throw new IOException("Incomplete BER/DER data");
            }
        }
        return tag;
    }
}

