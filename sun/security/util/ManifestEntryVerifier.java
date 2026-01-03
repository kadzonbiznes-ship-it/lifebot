/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.Manifest;
import sun.security.jca.Providers;
import sun.security.util.Debug;
import sun.security.util.DisabledAlgorithmConstraints;
import sun.security.util.JarConstraintsParameters;

public class ManifestEntryVerifier {
    private static final Debug debug = Debug.getInstance("jar");
    HashMap<String, MessageDigest> createdDigests = new HashMap(11);
    ArrayList<MessageDigest> digests = new ArrayList();
    ArrayList<byte[]> manifestHashes = new ArrayList();
    private String name = null;
    private final String manifestFileName;
    private final Manifest man;
    private boolean skip = true;
    private JarEntry entry;
    private CodeSigner[] signers = null;

    public ManifestEntryVerifier(Manifest man, String manifestFileName) {
        this.manifestFileName = manifestFileName;
        this.man = man;
    }

    public void setEntry(String name, JarEntry entry) throws IOException {
        this.digests.clear();
        this.manifestHashes.clear();
        this.name = name;
        this.entry = entry;
        this.skip = true;
        this.signers = null;
        if (this.man == null || name == null) {
            return;
        }
        this.skip = false;
        Attributes attr = this.man.getAttributes(name);
        if (attr == null && (attr = this.man.getAttributes("./" + name)) == null && (attr = this.man.getAttributes("/" + name)) == null) {
            return;
        }
        for (Map.Entry<Object, Object> se : attr.entrySet()) {
            String key = se.getKey().toString();
            if (!key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) continue;
            String algorithm = key.substring(0, key.length() - 7);
            MessageDigest digest = this.createdDigests.get(algorithm);
            if (digest == null) {
                try {
                    digest = MessageDigest.getInstance(algorithm, SunProviderHolder.instance);
                    this.createdDigests.put(algorithm, digest);
                }
                catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                    // empty catch block
                }
            }
            if (digest == null) continue;
            digest.reset();
            this.digests.add(digest);
            this.manifestHashes.add(Base64.getMimeDecoder().decode((String)se.getValue()));
        }
    }

    public void update(byte buffer) {
        if (this.skip) {
            return;
        }
        for (int i = 0; i < this.digests.size(); ++i) {
            this.digests.get(i).update(buffer);
        }
    }

    public void update(byte[] buffer, int off, int len) {
        if (this.skip) {
            return;
        }
        for (int i = 0; i < this.digests.size(); ++i) {
            this.digests.get(i).update(buffer, off, len);
        }
    }

    public JarEntry getEntry() {
        return this.entry;
    }

    public CodeSigner[] verify(Hashtable<String, CodeSigner[]> verifiedSigners, Hashtable<String, CodeSigner[]> sigFileSigners, Map<CodeSigner[], Map<String, Boolean>> signersToAlgs) throws JarException {
        if (this.skip) {
            return null;
        }
        if (this.digests.isEmpty()) {
            throw new SecurityException("digest missing for " + this.name);
        }
        if (this.signers != null) {
            return this.signers;
        }
        CodeSigner[] entrySigners = sigFileSigners.get(this.name);
        Map<String, Boolean> algsPermittedStatus = ManifestEntryVerifier.algsPermittedStatusForSigners(signersToAlgs, entrySigners);
        boolean disabledAlgs = true;
        JarConstraintsParameters params = null;
        for (int i = 0; i < this.digests.size(); ++i) {
            MessageDigest digest = this.digests.get(i);
            String digestAlg = digest.getAlgorithm();
            if (algsPermittedStatus != null) {
                Boolean permitted = algsPermittedStatus.get(digestAlg);
                if (permitted == null) {
                    if (params == null) {
                        params = new JarConstraintsParameters(entrySigners);
                    }
                    if (!this.checkConstraints(digestAlg, params)) {
                        algsPermittedStatus.put(digestAlg, Boolean.FALSE);
                        continue;
                    }
                    algsPermittedStatus.put(digestAlg, Boolean.TRUE);
                } else if (!permitted.booleanValue()) continue;
            }
            disabledAlgs = false;
            byte[] manHash = this.manifestHashes.get(i);
            byte[] theHash = digest.digest();
            if (debug != null) {
                debug.println("Manifest Entry: " + this.name + " digest=" + digestAlg);
                debug.println("  manifest " + HexFormat.of().formatHex(manHash));
                debug.println("  computed " + HexFormat.of().formatHex(theHash));
                debug.println();
            }
            if (MessageDigest.isEqual(theHash, manHash)) continue;
            throw new SecurityException(digestAlg + " digest error for " + this.name);
        }
        if (disabledAlgs) {
            return null;
        }
        this.signers = sigFileSigners.remove(this.name);
        if (this.signers != null) {
            verifiedSigners.put(this.name, this.signers);
        }
        return this.signers;
    }

    private static Map<String, Boolean> algsPermittedStatusForSigners(Map<CodeSigner[], Map<String, Boolean>> signersToAlgs, CodeSigner[] signers) {
        if (signers != null) {
            Map<String, Boolean> algs = signersToAlgs.get(signers);
            if (algs == null) {
                algs = new HashMap<String, Boolean>();
                signersToAlgs.put(signers, algs);
            }
            return algs;
        }
        return null;
    }

    private boolean checkConstraints(String algorithm, JarConstraintsParameters params) {
        try {
            params.setExtendedExceptionMsg("META-INF/MANIFEST.MF", this.name + " entry");
            DisabledAlgorithmConstraints.jarConstraints().permits(algorithm, params, false);
            return true;
        }
        catch (GeneralSecurityException e) {
            if (debug != null) {
                debug.println("Digest algorithm is restricted: " + e);
            }
            return false;
        }
    }

    private static class SunProviderHolder {
        private static final Provider instance = Providers.getSunProvider();

        private SunProviderHolder() {
        }
    }
}

