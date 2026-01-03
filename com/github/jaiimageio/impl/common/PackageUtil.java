/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.impl.common;

import java.security.AccessController;

public class PackageUtil {
    private static boolean isCodecLibAvailable = false;
    private static String version = "1.0";
    private static String vendor = "Sun Microsystems, Inc.";
    private static String specTitle = "Java Advanced Imaging Image I/O Tools";

    public static final boolean isCodecLibAvailable() {
        Boolean result = (Boolean)AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        boolean isCodecLibDisabled = result;
        return isCodecLibAvailable && !isCodecLibDisabled;
    }

    public static final String getVersion() {
        return version;
    }

    public static final String getVendor() {
        return vendor;
    }

    public static final String getSpecificationTitle() {
        return specTitle;
    }

    static {
        isCodecLibAvailable = false;
        try {
            Class<?> thisClass = Class.forName("com.github.jaiimageio.impl.common.PackageUtil");
            Package thisPackage = thisClass.getPackage();
            if (thisPackage.getImplementationVersion() != null && thisPackage.getImplementationVendor() != null) {
                version = thisPackage.getImplementationVersion();
                vendor = thisPackage.getImplementationVendor();
                specTitle = thisPackage.getSpecificationTitle();
            }
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        if (vendor == null) {
            vendor = "Unknown";
        }
        if (version == null) {
            version = "Unknown";
        }
    }
}

