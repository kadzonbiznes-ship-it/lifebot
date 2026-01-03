/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.plugins.tiff;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class TIFFTagSet {
    private SortedMap<Integer, TIFFTag> allowedTagsByNumber = new TreeMap<Integer, TIFFTag>();
    private SortedMap<String, TIFFTag> allowedTagsByName = new TreeMap<String, TIFFTag>();

    private TIFFTagSet() {
    }

    public TIFFTagSet(List<TIFFTag> tags) {
        if (tags == null) {
            throw new IllegalArgumentException("tags == null!");
        }
        for (TIFFTag o : tags) {
            if (!(o instanceof TIFFTag)) {
                throw new IllegalArgumentException("tags contains a non-TIFFTag!");
            }
            TIFFTag tag = o;
            this.allowedTagsByNumber.put(tag.getNumber(), tag);
            this.allowedTagsByName.put(tag.getName(), tag);
        }
    }

    public TIFFTag getTag(int tagNumber) {
        return (TIFFTag)this.allowedTagsByNumber.get(tagNumber);
    }

    public TIFFTag getTag(String tagName) {
        if (tagName == null) {
            throw new IllegalArgumentException("tagName == null!");
        }
        return (TIFFTag)this.allowedTagsByName.get(tagName);
    }

    public SortedSet<Integer> getTagNumbers() {
        Set<Integer> tagNumbers = this.allowedTagsByNumber.keySet();
        TreeSet<Integer> sortedTagNumbers = tagNumbers instanceof SortedSet ? (TreeSet<Integer>)tagNumbers : new TreeSet<Integer>(tagNumbers);
        return Collections.unmodifiableSortedSet(sortedTagNumbers);
    }

    public SortedSet<String> getTagNames() {
        Set<String> tagNames = this.allowedTagsByName.keySet();
        TreeSet<String> sortedTagNames = tagNames instanceof SortedSet ? (TreeSet<String>)tagNames : new TreeSet<String>(tagNames);
        return Collections.unmodifiableSortedSet(sortedTagNames);
    }
}

