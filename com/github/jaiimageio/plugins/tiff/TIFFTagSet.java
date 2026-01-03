/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.plugins.tiff;

import com.github.jaiimageio.plugins.tiff.TIFFTag;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class TIFFTagSet {
    private SortedMap allowedTagsByNumber = new TreeMap();
    private SortedMap allowedTagsByName = new TreeMap();

    private TIFFTagSet() {
    }

    public TIFFTagSet(List tags) {
        if (tags == null) {
            throw new IllegalArgumentException("tags == null!");
        }
        for (Object o : tags) {
            if (!(o instanceof TIFFTag)) {
                throw new IllegalArgumentException("tags contains a non-TIFFTag!");
            }
            TIFFTag tag = (TIFFTag)o;
            this.allowedTagsByNumber.put(new Integer(tag.getNumber()), tag);
            this.allowedTagsByName.put(tag.getName(), tag);
        }
    }

    public TIFFTag getTag(int tagNumber) {
        return (TIFFTag)this.allowedTagsByNumber.get(new Integer(tagNumber));
    }

    public TIFFTag getTag(String tagName) {
        if (tagName == null) {
            throw new IllegalArgumentException("tagName == null!");
        }
        return (TIFFTag)this.allowedTagsByName.get(tagName);
    }

    public SortedSet getTagNumbers() {
        Set tagNumbers = this.allowedTagsByNumber.keySet();
        TreeSet sortedTagNumbers = tagNumbers instanceof SortedSet ? (TreeSet)tagNumbers : new TreeSet(tagNumbers);
        return Collections.unmodifiableSortedSet(sortedTagNumbers);
    }

    public SortedSet getTagNames() {
        Set tagNames = this.allowedTagsByName.keySet();
        TreeSet sortedTagNames = tagNames instanceof SortedSet ? (TreeSet)tagNames : new TreeSet(tagNames);
        return Collections.unmodifiableSortedSet(sortedTagNames);
    }
}

