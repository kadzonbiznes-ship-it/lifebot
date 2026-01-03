/*
 * Decompiled with CFR 0.152.
 */
package java.util.jar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarVerifier;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import sun.security.util.ManifestEntryVerifier;

public class JarInputStream
extends ZipInputStream {
    private Manifest man;
    private JarEntry first;
    private JarVerifier jv;
    private ManifestEntryVerifier mev;
    private final boolean doVerify;
    private boolean tryManifest;

    public JarInputStream(InputStream in) throws IOException {
        this(in, true);
    }

    public JarInputStream(InputStream in, boolean verify) throws IOException {
        super(in);
        this.doVerify = verify;
        JarEntry e = (JarEntry)super.getNextEntry();
        if (e != null && e.getName().equalsIgnoreCase("META-INF/")) {
            e = (JarEntry)super.getNextEntry();
        }
        this.first = this.checkManifest(e);
    }

    private JarEntry checkManifest(JarEntry e) throws IOException {
        if (e != null && "META-INF/MANIFEST.MF".equalsIgnoreCase(e.getName())) {
            JarEntry nextEntry;
            this.man = new Manifest();
            byte[] bytes = this.readAllBytes();
            this.man.read(new ByteArrayInputStream(bytes));
            this.closeEntry();
            if (this.doVerify) {
                this.jv = new JarVerifier(e.getName(), bytes);
                this.mev = new ManifestEntryVerifier(this.man, this.jv.manifestName);
            }
            if ((nextEntry = (JarEntry)super.getNextEntry()) != null && "META-INF/MANIFEST.MF".equalsIgnoreCase(nextEntry.getName())) {
                if (JarVerifier.debug != null) {
                    JarVerifier.debug.println("WARNING: Multiple MANIFEST.MF found. Treat JAR file as unsigned.");
                }
                this.jv = null;
                this.mev = null;
            }
            return nextEntry;
        }
        return e;
    }

    public Manifest getManifest() {
        return this.man;
    }

    @Override
    public ZipEntry getNextEntry() throws IOException {
        JarEntry e;
        if (this.first == null) {
            e = (JarEntry)super.getNextEntry();
            if (this.tryManifest) {
                e = this.checkManifest(e);
                this.tryManifest = false;
            }
        } else {
            e = this.first;
            if (this.first.getName().equalsIgnoreCase("META-INF/INDEX.LIST")) {
                this.tryManifest = true;
            }
            this.first = null;
        }
        if (this.jv != null && e != null) {
            if (this.jv.nothingToVerify()) {
                this.jv = null;
                this.mev = null;
            } else {
                this.jv.beginEntry(e, this.mev);
            }
        }
        return e;
    }

    public JarEntry getNextJarEntry() throws IOException {
        return (JarEntry)this.getNextEntry();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = this.first == null ? super.read(b, off, len) : -1;
        if (this.jv != null) {
            this.jv.update(n, b, off, len, this.mev);
        }
        return n;
    }

    @Override
    protected ZipEntry createZipEntry(String name) {
        JarEntry e = new JarEntry(name);
        if (this.man != null) {
            e.attr = this.man.getAttributes(name);
        }
        return e;
    }
}

