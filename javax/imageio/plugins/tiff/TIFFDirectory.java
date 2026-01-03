/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.plugins.tiff;

import com.sun.imageio.plugins.tiff.TIFFIFD;
import com.sun.imageio.plugins.tiff.TIFFImageMetadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFDirectory
implements Cloneable {
    private static final int MAX_LOW_FIELD_TAG_NUM = 532;
    private List<TIFFTagSet> tagSets;
    private TIFFTag parentTag;
    private TIFFField[] lowFields = new TIFFField[533];
    private int numLowFields = 0;
    private Map<Integer, TIFFField> highFields = new TreeMap<Integer, TIFFField>();

    public static TIFFDirectory createFromMetadata(IIOMetadata tiffImageMetadata) throws IIOInvalidTreeException {
        TIFFImageMetadata tim;
        if (tiffImageMetadata == null) {
            throw new NullPointerException("tiffImageMetadata == null");
        }
        if (tiffImageMetadata instanceof TIFFImageMetadata) {
            tim = (TIFFImageMetadata)tiffImageMetadata;
        } else {
            ArrayList<TIFFTagSet> l = new ArrayList<TIFFTagSet>(1);
            l.add(BaselineTIFFTagSet.getInstance());
            tim = new TIFFImageMetadata(l);
            String formatName = null;
            if ("javax_imageio_tiff_image_1.0".equals(tiffImageMetadata.getNativeMetadataFormatName())) {
                formatName = "javax_imageio_tiff_image_1.0";
            } else {
                String[] extraNames = tiffImageMetadata.getExtraMetadataFormatNames();
                if (extraNames != null) {
                    for (int i = 0; i < extraNames.length; ++i) {
                        if (!"javax_imageio_tiff_image_1.0".equals(extraNames[i])) continue;
                        formatName = extraNames[i];
                        break;
                    }
                }
                if (formatName == null) {
                    if (tiffImageMetadata.isStandardMetadataFormatSupported()) {
                        formatName = "javax_imageio_1.0";
                    } else {
                        throw new IllegalArgumentException("Parameter does not support required metadata format!");
                    }
                }
            }
            tim.setFromTree(formatName, tiffImageMetadata.getAsTree(formatName));
        }
        return tim.getRootIFD();
    }

    public TIFFDirectory(TIFFTagSet[] tagSets, TIFFTag parentTag) {
        if (tagSets == null) {
            throw new NullPointerException("tagSets == null!");
        }
        this.tagSets = new ArrayList<TIFFTagSet>(tagSets.length);
        int numTagSets = tagSets.length;
        for (int i = 0; i < numTagSets; ++i) {
            this.tagSets.add(tagSets[i]);
        }
        this.parentTag = parentTag;
    }

    public TIFFTagSet[] getTagSets() {
        return this.tagSets.toArray(new TIFFTagSet[this.tagSets.size()]);
    }

    public void addTagSet(TIFFTagSet tagSet) {
        if (tagSet == null) {
            throw new NullPointerException("tagSet == null");
        }
        if (!this.tagSets.contains(tagSet)) {
            this.tagSets.add(tagSet);
        }
    }

    public void removeTagSet(TIFFTagSet tagSet) {
        if (tagSet == null) {
            throw new NullPointerException("tagSet == null");
        }
        if (this.tagSets.contains(tagSet)) {
            this.tagSets.remove(tagSet);
        }
    }

    public TIFFTag getParentTag() {
        return this.parentTag;
    }

    public TIFFTag getTag(int tagNumber) {
        return TIFFIFD.getTag(tagNumber, this.tagSets);
    }

    public int getNumTIFFFields() {
        return this.numLowFields + this.highFields.size();
    }

    public boolean containsTIFFField(int tagNumber) {
        return tagNumber >= 0 && tagNumber <= 532 && this.lowFields[tagNumber] != null || this.highFields.containsKey(tagNumber);
    }

    public void addTIFFField(TIFFField f) {
        if (f == null) {
            throw new NullPointerException("f == null");
        }
        int tagNumber = f.getTagNumber();
        if (tagNumber >= 0 && tagNumber <= 532) {
            if (this.lowFields[tagNumber] == null) {
                ++this.numLowFields;
            }
            this.lowFields[tagNumber] = f;
        } else {
            this.highFields.put(tagNumber, f);
        }
    }

    public TIFFField getTIFFField(int tagNumber) {
        TIFFField f = tagNumber >= 0 && tagNumber <= 532 ? this.lowFields[tagNumber] : this.highFields.get(tagNumber);
        return f;
    }

    public void removeTIFFField(int tagNumber) {
        if (tagNumber >= 0 && tagNumber <= 532) {
            if (this.lowFields[tagNumber] != null) {
                --this.numLowFields;
                this.lowFields[tagNumber] = null;
            }
        } else {
            this.highFields.remove(tagNumber);
        }
    }

    public TIFFField[] getTIFFFields() {
        TIFFField[] fields = new TIFFField[this.numLowFields + this.highFields.size()];
        int nextIndex = 0;
        for (int i = 0; i <= 532; ++i) {
            if (this.lowFields[i] == null) continue;
            fields[nextIndex++] = this.lowFields[i];
            if (nextIndex == this.numLowFields) break;
        }
        if (!this.highFields.isEmpty()) {
            for (Integer tagNumber : this.highFields.keySet()) {
                fields[nextIndex++] = this.highFields.get(tagNumber);
            }
        }
        return fields;
    }

    public void removeTIFFFields() {
        Arrays.fill(this.lowFields, null);
        this.numLowFields = 0;
        this.highFields.clear();
    }

    public IIOMetadata getAsMetadata() {
        return new TIFFImageMetadata(TIFFIFD.getDirectoryAsIFD(this));
    }

    public TIFFDirectory clone() throws CloneNotSupportedException {
        TIFFField[] fields;
        TIFFDirectory dir = (TIFFDirectory)super.clone();
        dir.tagSets = new ArrayList<TIFFTagSet>(this.tagSets);
        dir.parentTag = this.getParentTag();
        for (TIFFField field : fields = this.getTIFFFields()) {
            dir.addTIFFField(field.clone());
        }
        return dir;
    }
}

