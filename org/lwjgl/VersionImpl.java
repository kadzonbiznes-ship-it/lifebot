/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl;

import org.lwjgl.Version;

final class VersionImpl {
    VersionImpl() {
    }

    static String find() {
        Package org_lwjgl = Version.class.getPackage();
        String specVersion = org_lwjgl.getSpecificationVersion();
        String implVersion = org_lwjgl.getImplementationVersion();
        if (specVersion != null && implVersion != null) {
            return Version.createImplementation(specVersion, implVersion);
        }
        String version = Version.findImplementationFromManifest();
        if (version != null) {
            return version;
        }
        return "-snapshot";
    }
}

