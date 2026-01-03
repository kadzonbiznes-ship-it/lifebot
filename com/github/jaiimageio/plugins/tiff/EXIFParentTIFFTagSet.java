/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.plugins.tiff.EXIFParentTIFFTagSet$EXIFIFDPointer
 *  com.github.jaiimageio.plugins.tiff.EXIFParentTIFFTagSet$GPSInfoIFDPointer
 */
package com.github.jaiimageio.plugins.tiff;

import com.github.jaiimageio.plugins.tiff.EXIFParentTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.TIFFTagSet;
import java.util.ArrayList;
import java.util.List;

public class EXIFParentTIFFTagSet
extends TIFFTagSet {
    private static EXIFParentTIFFTagSet theInstance = null;
    public static final int TAG_EXIF_IFD_POINTER = 34665;
    public static final int TAG_GPS_INFO_IFD_POINTER = 34853;
    private static List tags;

    private static void initTags() {
        tags = new ArrayList(1);
        tags.add(new EXIFIFDPointer());
        tags.add(new GPSInfoIFDPointer());
    }

    private EXIFParentTIFFTagSet() {
        super(tags);
    }

    public static synchronized EXIFParentTIFFTagSet getInstance() {
        if (theInstance == null) {
            EXIFParentTIFFTagSet.initTags();
            theInstance = new EXIFParentTIFFTagSet();
            tags = null;
        }
        return theInstance;
    }
}

