/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.jspecify.annotations.Nullable;
import org.lwjgl.VersionImpl;

public final class Version {
    public static final int VERSION_MAJOR = 3;
    public static final int VERSION_MINOR = 3;
    public static final int VERSION_REVISION = 6;
    public static final BuildType BUILD_TYPE = BuildType.STABLE;
    private static final String versionPlain = String.valueOf(3) + '.' + 3 + '.' + 6 + Version.BUILD_TYPE.postfix;
    private static final String version = versionPlain + VersionImpl.find();

    private Version() {
    }

    public static void main(String[] args) {
        System.out.println(version);
        System.err.println(versionPlain);
    }

    public static String getVersion() {
        return version;
    }

    static String createImplementation(String specVersion, String implVersion) {
        String build = "+" + (implVersion.startsWith("build ") && 6 < implVersion.length() ? implVersion.substring(6) : implVersion);
        if (specVersion.contains("SNAPSHOT") || specVersion.contains("snapshot")) {
            return "-snapshot" + build;
        }
        return build;
    }

    static @Nullable String findImplementationFromManifest() {
        ClassLoader classLoader = Version.class.getClassLoader();
        URL url = classLoader.getResource("org/lwjgl/Version.class");
        if (url != null) {
            String classURL = url.toString();
            try {
                if (classURL.startsWith("jar:")) {
                    URL manifest = Version.class.getResource("/META-INF/MANIFEST.MF");
                    String version = Version.readImplementationFromManifest(Objects.requireNonNull(manifest));
                    if (version != null) {
                        return version;
                    }
                } else if (classURL.startsWith("resource:")) {
                    Enumeration<URL> e = classLoader.getResources("META-INF/MANIFEST.MF");
                    while (e.hasMoreElements()) {
                        String version = Version.readImplementationFromManifest(e.nextElement());
                        if (version == null) continue;
                        return version;
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static @Nullable String readImplementationFromManifest(URL url) {
        try (InputStream stream = url.openStream();){
            Attributes attribs = new Manifest(stream).getMainAttributes();
            if (!"lwjgl".equals(attribs.getValue(Attributes.Name.IMPLEMENTATION_TITLE))) {
                String string = null;
                return string;
            }
            if (!"lwjgl.org".equals(attribs.getValue(Attributes.Name.IMPLEMENTATION_VENDOR))) {
                String string = null;
                return string;
            }
            String specVersion = attribs.getValue(Attributes.Name.SPECIFICATION_VERSION);
            String implVersion = attribs.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            if (specVersion == null || implVersion == null) {
                String string = null;
                return string;
            }
            String string = Version.createImplementation(specVersion, implVersion);
            return string;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    public static enum BuildType {
        ALPHA("a"),
        BETA("b"),
        STABLE("");

        public final String postfix;

        private BuildType(String postfix) {
            this.postfix = postfix;
        }
    }
}

