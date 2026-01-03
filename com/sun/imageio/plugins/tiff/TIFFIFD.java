/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.tiff;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import javax.imageio.IIOException;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class TIFFIFD
extends TIFFDirectory {
    private static final long MAX_SAMPLES_PER_PIXEL = 65535L;
    private static final long MAX_ASCII_SIZE = 65535L;
    private long stripOrTileByteCountsPosition = -1L;
    private long stripOrTileOffsetsPosition = -1L;
    private long lastPosition = -1L;
    private static volatile Set<Integer> essentialTags;

    private static void initializeEssentialTags() {
        Set<Integer> tags = essentialTags;
        if (tags == null) {
            essentialTags = Set.of(258, 320, 259, 338, 266, 34675, 257, 256, 521, 520, 513, 514, 512, 519, 515, 347, 262, 284, 317, 532, 278, 277, 339, 279, 273, 292, 293, 325, 323, 324, 322, 529, 530);
        }
    }

    public static TIFFIFD getDirectoryAsIFD(TIFFDirectory dir) {
        if (dir instanceof TIFFIFD) {
            return (TIFFIFD)dir;
        }
        TIFFIFD ifd = new TIFFIFD(Arrays.asList(dir.getTagSets()), dir.getParentTag());
        for (TIFFField f : dir.getTIFFFields()) {
            TIFFTag tag = f.getTag();
            if (tag.isIFDPointer()) {
                TIFFDirectory subDir = null;
                if (f.hasDirectory()) {
                    subDir = f.getDirectory();
                } else if (f.getData() instanceof TIFFDirectory) {
                    subDir = (TIFFDirectory)f.getData();
                }
                if (subDir != null) {
                    TIFFIFD subIFD = TIFFIFD.getDirectoryAsIFD(subDir);
                    f = new TIFFField(tag, f.getType(), (long)f.getCount(), subIFD);
                } else {
                    f = null;
                }
            }
            if (f == null) continue;
            ifd.addTIFFField(f);
        }
        return ifd;
    }

    public static TIFFTag getTag(int tagNumber, List<TIFFTagSet> tagSets) {
        for (TIFFTagSet tagSet : tagSets) {
            TIFFTag tag = tagSet.getTag(tagNumber);
            if (tag == null) continue;
            return tag;
        }
        return null;
    }

    public static TIFFTag getTag(String tagName, List<TIFFTagSet> tagSets) {
        for (TIFFTagSet tagSet : tagSets) {
            TIFFTag tag = tagSet.getTag(tagName);
            if (tag == null) continue;
            return tag;
        }
        return null;
    }

    private static void writeTIFFFieldToStream(TIFFField field, ImageOutputStream stream) throws IOException {
        int count = field.getCount();
        Object data = field.getData();
        switch (field.getType()) {
            case 2: {
                for (int i = 0; i < count; ++i) {
                    String s = ((String[])data)[i];
                    int length = s.length();
                    for (int j = 0; j < length; ++j) {
                        stream.writeByte(s.charAt(j) & 0xFF);
                    }
                    stream.writeByte(0);
                }
                break;
            }
            case 1: 
            case 6: 
            case 7: {
                stream.write((byte[])data);
                break;
            }
            case 3: {
                stream.writeChars((char[])data, 0, ((char[])data).length);
                break;
            }
            case 8: {
                stream.writeShorts((short[])data, 0, ((short[])data).length);
                break;
            }
            case 9: {
                stream.writeInts((int[])data, 0, ((int[])data).length);
                break;
            }
            case 4: {
                for (int i = 0; i < count; ++i) {
                    stream.writeInt((int)((long[])data)[i]);
                }
                break;
            }
            case 13: {
                stream.writeInt(0);
                break;
            }
            case 11: {
                stream.writeFloats((float[])data, 0, ((float[])data).length);
                break;
            }
            case 12: {
                stream.writeDoubles((double[])data, 0, ((double[])data).length);
                break;
            }
            case 10: {
                for (int i = 0; i < count; ++i) {
                    stream.writeInt(((int[][])data)[i][0]);
                    stream.writeInt(((int[][])data)[i][1]);
                }
                break;
            }
            case 5: {
                for (int i = 0; i < count; ++i) {
                    long num = ((long[][])data)[i][0];
                    long den = ((long[][])data)[i][1];
                    stream.writeInt((int)num);
                    stream.writeInt((int)den);
                }
                break;
            }
        }
    }

    public TIFFIFD(List<TIFFTagSet> tagSets, TIFFTag parentTag) {
        super(tagSets.toArray(new TIFFTagSet[tagSets.size()]), parentTag);
    }

    public TIFFIFD(List<TIFFTagSet> tagSets) {
        this(tagSets, null);
    }

    public List<TIFFTagSet> getTagSetList() {
        return Arrays.asList(this.getTagSets());
    }

    public Iterator<TIFFField> iterator() {
        return Arrays.asList(this.getTIFFFields()).iterator();
    }

    private static int readFieldValue(ImageInputStream stream, int type, int count, Object[] data) throws IOException {
        int UNIT_SIZE = 1024000;
        data[0] = switch (type) {
            case 1, 2, 6, 7 -> {
                int sz;
                if (type == 2) {
                    String[] strings;
                    byte[] bvalues = new byte[count];
                    stream.readFully(bvalues, 0, count);
                    ArrayList<String> v = new ArrayList<String>();
                    boolean inString = false;
                    int prevIndex = 0;
                    for (int index = 0; index <= count; ++index) {
                        if (index < count && bvalues[index] != 0) {
                            if (inString) continue;
                            prevIndex = index;
                            inString = true;
                            continue;
                        }
                        if (!inString) continue;
                        String s = new String(bvalues, prevIndex, index - prevIndex, StandardCharsets.US_ASCII);
                        v.add(s);
                        inString = false;
                    }
                    count = v.size();
                    if (count != 0) {
                        strings = new String[count];
                        for (int c = 0; c < count; ++c) {
                            strings[c] = (String)v.get(c);
                        }
                    } else {
                        count = 1;
                        strings = new String[]{""};
                    }
                    yield (Object)strings;
                }
                if (count < 1024000) {
                    byte[] bvalues = new byte[count];
                    stream.readFully(bvalues, 0, count);
                    yield bvalues;
                }
                int bytesRead = 0;
                ArrayList<byte[]> bufs = new ArrayList<byte[]>();
                for (int bytesToRead = count; bytesToRead != 0; bytesToRead -= sz) {
                    sz = Math.min(bytesToRead, 1024000);
                    byte[] unit = new byte[sz];
                    stream.readFully(unit, 0, sz);
                    bufs.add(unit);
                    bytesRead += sz;
                }
                byte[] tagData = new byte[bytesRead];
                int copiedBytes = 0;
                for (byte[] ba : bufs) {
                    System.arraycopy(ba, 0, tagData, copiedBytes, ba.length);
                    copiedBytes += ba.length;
                }
                yield tagData;
            }
            case 3 -> {
                int sz;
                int SHORT_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(3);
                if (count < SHORT_TILE_SIZE) {
                    char[] cvalues = new char[count];
                    for (int j = 0; j < count; ++j) {
                        cvalues[j] = (char)stream.readUnsignedShort();
                    }
                    yield (Object)cvalues;
                }
                int charsRead = 0;
                ArrayList<char[]> bufs = new ArrayList<char[]>();
                for (int charsToRead = count; charsToRead != 0; charsToRead -= sz) {
                    sz = Math.min(charsToRead, SHORT_TILE_SIZE);
                    char[] unit = new char[sz];
                    for (int i = 0; i < sz; ++i) {
                        unit[i] = (char)stream.readUnsignedShort();
                    }
                    bufs.add(unit);
                    charsRead += sz;
                }
                char[] tagData = new char[charsRead];
                int copiedChars = 0;
                for (char[] ca : bufs) {
                    System.arraycopy(ca, 0, tagData, copiedChars, ca.length);
                    copiedChars += ca.length;
                }
                yield (Object)tagData;
            }
            case 4, 13 -> {
                int sz;
                int LONG_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(4);
                if (count < LONG_TILE_SIZE) {
                    long[] lvalues = new long[count];
                    for (int j = 0; j < count; ++j) {
                        lvalues[j] = stream.readUnsignedInt();
                    }
                    yield (Object)lvalues;
                }
                int longsRead = 0;
                ArrayList<long[]> bufs = new ArrayList<long[]>();
                for (int longsToRead = count; longsToRead != 0; longsToRead -= sz) {
                    sz = Math.min(longsToRead, LONG_TILE_SIZE);
                    long[] unit = new long[sz];
                    for (int i = 0; i < sz; ++i) {
                        unit[i] = stream.readUnsignedInt();
                    }
                    bufs.add(unit);
                    longsRead += sz;
                }
                long[] tagData = new long[longsRead];
                int copiedLongs = 0;
                for (long[] la : bufs) {
                    System.arraycopy(la, 0, tagData, copiedLongs, la.length);
                    copiedLongs += la.length;
                }
                yield (Object)tagData;
            }
            case 5 -> {
                int sz;
                int RATIONAL_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(5);
                if (count < RATIONAL_TILE_SIZE) {
                    long[][] llvalues = new long[count][2];
                    for (int j = 0; j < count; ++j) {
                        llvalues[j][0] = stream.readUnsignedInt();
                        llvalues[j][1] = stream.readUnsignedInt();
                    }
                    yield (Object)llvalues;
                }
                int rationalsRead = 0;
                ArrayList<long[]> bufs = new ArrayList<long[]>();
                for (int rationalsToRead = count; rationalsToRead != 0; rationalsToRead -= sz) {
                    sz = Math.min(rationalsToRead, RATIONAL_TILE_SIZE);
                    long[] unit = new long[sz * 2];
                    for (int i = 0; i < sz * 2; ++i) {
                        unit[i] = stream.readUnsignedInt();
                    }
                    bufs.add(unit);
                    rationalsRead += sz;
                }
                long[][] tagData = new long[rationalsRead][2];
                int copiedRationals = 0;
                for (long[] la : bufs) {
                    for (int i = 0; i < la.length; i += 2) {
                        tagData[copiedRationals + i][0] = la[i];
                        tagData[copiedRationals + i][1] = la[i + 1];
                    }
                    copiedRationals += la.length / 2;
                }
                yield (Object)tagData;
            }
            case 8 -> {
                int sz;
                int SSHORT_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(8);
                if (count < SSHORT_TILE_SIZE) {
                    short[] svalues = new short[count];
                    for (int j = 0; j < count; ++j) {
                        svalues[j] = stream.readShort();
                    }
                    yield (Object)svalues;
                }
                int shortsRead = 0;
                ArrayList<short[]> bufs = new ArrayList<short[]>();
                for (int shortsToRead = count; shortsToRead != 0; shortsToRead -= sz) {
                    sz = Math.min(shortsToRead, SSHORT_TILE_SIZE);
                    short[] unit = new short[sz];
                    stream.readFully(unit, 0, sz);
                    bufs.add(unit);
                    shortsRead += sz;
                }
                short[] tagData = new short[shortsRead];
                int copiedShorts = 0;
                for (short[] sa : bufs) {
                    System.arraycopy(sa, 0, tagData, copiedShorts, sa.length);
                    copiedShorts += sa.length;
                }
                yield (Object)tagData;
            }
            case 9 -> {
                int sz;
                int INT_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(9);
                if (count < INT_TILE_SIZE) {
                    int[] ivalues = new int[count];
                    for (int j = 0; j < count; ++j) {
                        ivalues[j] = stream.readInt();
                    }
                    yield (Object)ivalues;
                }
                int intsRead = 0;
                ArrayList<int[]> bufs = new ArrayList<int[]>();
                for (int intsToRead = count; intsToRead != 0; intsToRead -= sz) {
                    sz = Math.min(intsToRead, INT_TILE_SIZE);
                    int[] unit = new int[sz];
                    stream.readFully(unit, 0, sz);
                    bufs.add(unit);
                    intsRead += sz;
                }
                int[] tagData = new int[intsRead];
                int copiedInts = 0;
                for (int[] ia : bufs) {
                    System.arraycopy(ia, 0, tagData, copiedInts, ia.length);
                    copiedInts += ia.length;
                }
                yield (Object)tagData;
            }
            case 10 -> {
                int sz;
                int SRATIONAL_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(10);
                if (count < SRATIONAL_TILE_SIZE) {
                    int[][] iivalues = new int[count][2];
                    for (int j = 0; j < count; ++j) {
                        iivalues[j][0] = stream.readInt();
                        iivalues[j][1] = stream.readInt();
                    }
                    yield (Object)iivalues;
                }
                int srationalsRead = 0;
                ArrayList<int[]> bufs = new ArrayList<int[]>();
                for (int srationalsToRead = count; srationalsToRead != 0; srationalsToRead -= sz) {
                    sz = Math.min(srationalsToRead, SRATIONAL_TILE_SIZE);
                    int[] unit = new int[sz * 2];
                    stream.readFully(unit, 0, sz * 2);
                    bufs.add(unit);
                    srationalsRead += sz;
                }
                int[][] tagData = new int[srationalsRead][2];
                int copiedSrationals = 0;
                for (int[] ia : bufs) {
                    for (int i = 0; i < ia.length; i += 2) {
                        tagData[copiedSrationals + i][0] = ia[i];
                        tagData[copiedSrationals + i][1] = ia[i + 1];
                    }
                    copiedSrationals += ia.length / 2;
                }
                yield (Object)tagData;
            }
            case 11 -> {
                int sz;
                int FLOAT_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(11);
                if (count < FLOAT_TILE_SIZE) {
                    float[] fvalues = new float[count];
                    for (int j = 0; j < count; ++j) {
                        fvalues[j] = stream.readFloat();
                    }
                    yield (Object)fvalues;
                }
                int floatsRead = 0;
                ArrayList<float[]> bufs = new ArrayList<float[]>();
                for (int floatsToRead = count; floatsToRead != 0; floatsToRead -= sz) {
                    sz = Math.min(floatsToRead, FLOAT_TILE_SIZE);
                    float[] unit = new float[sz];
                    stream.readFully(unit, 0, sz);
                    bufs.add(unit);
                    floatsRead += sz;
                }
                float[] tagData = new float[floatsRead];
                int copiedFloats = 0;
                for (float[] fa : bufs) {
                    System.arraycopy(fa, 0, tagData, copiedFloats, fa.length);
                    copiedFloats += fa.length;
                }
                yield (Object)tagData;
            }
            case 12 -> {
                int sz;
                int DOUBLE_TILE_SIZE = 1024000 / TIFFTag.getSizeOfType(12);
                if (count < DOUBLE_TILE_SIZE) {
                    double[] dvalues = new double[count];
                    for (int j = 0; j < count; ++j) {
                        dvalues[j] = stream.readDouble();
                    }
                    yield (Object)dvalues;
                }
                int doublesRead = 0;
                ArrayList<double[]> bufs = new ArrayList<double[]>();
                for (int doublesToRead = count; doublesToRead != 0; doublesToRead -= sz) {
                    sz = Math.min(doublesToRead, DOUBLE_TILE_SIZE);
                    double[] unit = new double[sz];
                    stream.readFully(unit, 0, sz);
                    bufs.add(unit);
                    doublesRead += sz;
                }
                double[] tagData = new double[doublesRead];
                int copiedDoubles = 0;
                for (double[] da : bufs) {
                    System.arraycopy(da, 0, tagData, copiedDoubles, da.length);
                    copiedDoubles += da.length;
                }
                yield (Object)tagData;
            }
            default -> null;
        };
        return count;
    }

    private long getFieldAsLong(int tagNumber) {
        TIFFField f = this.getTIFFField(tagNumber);
        return f == null ? -1L : f.getAsLong(0);
    }

    private int getFieldAsInt(int tagNumber) {
        TIFFField f = this.getTIFFField(tagNumber);
        return f == null ? -1 : f.getAsInt(0);
    }

    private boolean calculateByteCounts(int expectedSize, List<TIFFField> byteCounts) {
        long th;
        if (!byteCounts.isEmpty()) {
            throw new IllegalArgumentException("byteCounts is not empty");
        }
        if (this.getFieldAsInt(284) == 2) {
            return false;
        }
        if (this.getFieldAsInt(259) != 1) {
            return false;
        }
        long w = this.getFieldAsLong(256);
        if (w < 0L) {
            return false;
        }
        long h = this.getFieldAsLong(257);
        if (h < 0L) {
            return false;
        }
        long tw = this.getFieldAsLong(322);
        if (tw < 0L) {
            tw = w;
        }
        if ((th = this.getFieldAsLong(323)) < 0L && (th = this.getFieldAsLong(278)) < 0L) {
            th = h;
        }
        int[] bitsPerSample = null;
        TIFFField f = this.getTIFFField(258);
        if (f != null) {
            bitsPerSample = f.getAsInts();
        } else {
            int samplesPerPixel = this.getFieldAsInt(277);
            if (samplesPerPixel < 0) {
                samplesPerPixel = 1;
            }
            bitsPerSample = new int[samplesPerPixel];
            Arrays.fill(bitsPerSample, 8);
        }
        int bitsPerPixel = 0;
        for (int bps : bitsPerSample) {
            bitsPerPixel += bps;
        }
        int bytesPerRow = (int)(tw * (long)bitsPerPixel + 7L) / 8;
        int bytesPerPacket = (int)th * bytesPerRow;
        long nx = (w + tw - 1L) / tw;
        long ny = (h + th - 1L) / th;
        if (nx * ny != (long)expectedSize) {
            return false;
        }
        boolean isTiled = this.getTIFFField(325) != null;
        int tagNumber = isTiled ? 325 : 279;
        TIFFTag t = BaselineTIFFTagSet.getInstance().getTag(tagNumber);
        f = this.getTIFFField(tagNumber);
        if (f != null) {
            this.removeTIFFField(tagNumber);
        }
        int numPackets = (int)(nx * ny);
        long[] packetByteCounts = new long[numPackets];
        Arrays.fill(packetByteCounts, (long)bytesPerPacket);
        if (tw <= w && h % th != 0L) {
            int numRowsInLastStrip = (int)(h - (ny - 1L) * th);
            packetByteCounts[numPackets - 1] = numRowsInLastStrip * bytesPerRow;
        }
        f = new TIFFField(t, 4, numPackets, packetByteCounts);
        this.addTIFFField(f);
        byteCounts.add(f);
        return true;
    }

    private void checkFieldOffsets(long streamLength) throws IIOException {
        long off2;
        int n;
        TIFFField jpegLength;
        TIFFField jpegOffset;
        if (streamLength < 0L) {
            return;
        }
        ArrayList<TIFFField> offsets = new ArrayList<TIFFField>();
        TIFFField f = this.getTIFFField(273);
        int count = 0;
        if (f != null) {
            count = f.getCount();
            offsets.add(f);
        }
        if ((f = this.getTIFFField(324)) != null) {
            int sz = offsets.size();
            int newCount = f.getCount();
            if (sz > 0 && newCount != count) {
                throw new IIOException("StripOffsets count != TileOffsets count");
            }
            if (sz == 0) {
                count = newCount;
            }
            offsets.add(f);
        }
        ArrayList<TIFFField> byteCounts = new ArrayList<TIFFField>();
        if (offsets.size() > 0) {
            f = this.getTIFFField(279);
            if (f != null) {
                if (f.getCount() != count) {
                    throw new IIOException("StripByteCounts count != number of offsets");
                }
                byteCounts.add(f);
            }
            if ((f = this.getTIFFField(325)) != null) {
                if (f.getCount() != count) {
                    throw new IIOException("TileByteCounts count != number of offsets");
                }
                byteCounts.add(f);
            }
            if (byteCounts.size() > 0) {
                for (TIFFField offset : offsets) {
                    for (TIFFField byteCount : byteCounts) {
                        for (int i = 0; i < count; ++i) {
                            long dataByteCount;
                            long dataOffset = offset.getAsLong(i);
                            if (dataOffset + (dataByteCount = byteCount.getAsLong(i)) <= streamLength) continue;
                            throw new IIOException("Data segment out of stream");
                        }
                    }
                }
            }
        }
        if ((jpegOffset = this.getTIFFField(513)) != null && (jpegLength = this.getTIFFField(514)) != null && jpegOffset.getAsLong(0) + jpegLength.getAsLong(0) > streamLength) {
            throw new IIOException("JPEGInterchangeFormat data out of stream");
        }
        if (jpegOffset == null && (offsets.size() == 0 || byteCounts.size() == 0)) {
            boolean throwException = true;
            if (offsets.size() != 0 && byteCounts.size() == 0) {
                int expectedSize = ((TIFFField)offsets.get(0)).getCount();
                boolean bl = throwException = !this.calculateByteCounts(expectedSize, byteCounts);
            }
            if (throwException) {
                throw new IIOException("Insufficient data offsets or byte counts");
            }
        }
        if ((f = this.getTIFFField(519)) != null) {
            long[] tableOffsets;
            long[] lArray = tableOffsets = f.getAsLongs();
            int n2 = lArray.length;
            for (n = 0; n < n2; ++n) {
                off2 = lArray[n];
                if (off2 + 64L <= streamLength) continue;
                throw new IIOException("JPEGQTables data out of stream");
            }
        }
        if ((f = this.getTIFFField(520)) != null) {
            long[] tableOffsets;
            long[] lArray = tableOffsets = f.getAsLongs();
            int n3 = lArray.length;
            for (n = 0; n < n3; ++n) {
                off2 = lArray[n];
                if (off2 + 16L <= streamLength) continue;
                throw new IIOException("JPEGDCTables data out of stream");
            }
        }
        if ((f = this.getTIFFField(521)) != null) {
            long[] tableOffsets;
            for (long off2 : tableOffsets = f.getAsLongs()) {
                if (off2 + 16L <= streamLength) continue;
                throw new IIOException("JPEGACTables data out of stream");
            }
        }
    }

    public void initialize(ImageInputStream stream, boolean isPrimaryIFD, boolean ignoreMetadata, boolean readUnknownTags) throws IOException {
        TIFFTag tag;
        this.removeTIFFFields();
        long streamLength = stream.length();
        boolean haveStreamLength = streamLength != -1L;
        List<TIFFTagSet> tagSetList = this.getTagSetList();
        boolean ensureEssentialTags = false;
        BaselineTIFFTagSet baselineTagSet = null;
        if (isPrimaryIFD && (ignoreMetadata || !readUnknownTags && !tagSetList.contains(BaselineTIFFTagSet.getInstance()))) {
            ensureEssentialTags = true;
            TIFFIFD.initializeEssentialTags();
            baselineTagSet = BaselineTIFFTagSet.getInstance();
        }
        ArrayList<Object> entries = new ArrayList<Object>();
        Object[] entryData = new Object[1];
        int numEntries = stream.readUnsignedShort();
        for (int i = 0; i < numEntries; ++i) {
            Object obj;
            int size;
            int count;
            int type;
            block26: {
                int sizeOfType;
                int tagNumber = stream.readUnsignedShort();
                type = stream.readUnsignedShort();
                try {
                    sizeOfType = TIFFTag.getSizeOfType(type);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    stream.skipBytes(4);
                    continue;
                }
                long l = stream.readUnsignedInt();
                tag = TIFFIFD.getTag(tagNumber, tagSetList);
                if (tag == null && ensureEssentialTags && essentialTags.contains(tagNumber)) {
                    tag = baselineTagSet.getTag(tagNumber);
                }
                if (ignoreMetadata && (!ensureEssentialTags || !essentialTags.contains(tagNumber)) || tag == null && !readUnknownTags || tag != null && !tag.isDataTypeOK(type) || l > Integer.MAX_VALUE) {
                    stream.skipBytes(4);
                    continue;
                }
                count = (int)l;
                if (tag == null) {
                    tag = new TIFFTag("UnknownTag", tagNumber, 1 << type, count);
                } else {
                    int asciiSize;
                    int expectedCount = tag.getCount();
                    if (expectedCount > 0) {
                        if (count != expectedCount) {
                            throw new IIOException("Unexpected count " + count + " for " + tag.getName() + " field");
                        }
                    } else if (type == 2 && (long)(count * (asciiSize = TIFFTag.getSizeOfType(2))) > 65535L) {
                        count = (int)(65535L / (long)asciiSize);
                    }
                }
                long longSize = l * (long)sizeOfType;
                if (longSize > Integer.MAX_VALUE) {
                    stream.skipBytes(4);
                    continue;
                }
                size = (int)longSize;
                if (size > 4 || tag.isIFDPointer()) {
                    long offset = stream.readUnsignedInt();
                    if (haveStreamLength && offset + (long)size > streamLength) continue;
                    entries.add(new TIFFIFDEntry(tag, type, count, offset));
                    continue;
                }
                obj = null;
                try {
                    count = TIFFIFD.readFieldValue(stream, type, count, entryData);
                    obj = entryData[0];
                }
                catch (EOFException eofe) {
                    if (BaselineTIFFTagSet.getInstance().getTag(tagNumber) != null) break block26;
                    throw eofe;
                }
            }
            if (size < 4) {
                stream.skipBytes(4 - size);
            }
            entries.add(new TIFFField(tag, type, count, obj));
        }
        long nextIFDOffset = stream.getStreamPosition();
        Object[] fieldData = new Object[1];
        for (Object e : entries) {
            Object obj;
            int count;
            int type;
            block27: {
                if (e instanceof TIFFField) {
                    this.addTIFFField((TIFFField)e);
                    continue;
                }
                TIFFIFDEntry e2 = (TIFFIFDEntry)e;
                tag = e2.tag;
                int tagNumber = tag.getNumber();
                type = e2.type;
                count = e2.count;
                stream.seek(e2.offset);
                if (tag.isIFDPointer()) {
                    ArrayList<TIFFTagSet> tagSets = new ArrayList<TIFFTagSet>(1);
                    tagSets.add(tag.getTagSet());
                    TIFFIFD subIFD = new TIFFIFD(tagSets);
                    subIFD.initialize(stream, false, ignoreMetadata, readUnknownTags);
                    TIFFField f = new TIFFField(tag, type, e2.offset, subIFD);
                    this.addTIFFField(f);
                    continue;
                }
                if (tagNumber == 279 || tagNumber == 325 || tagNumber == 514) {
                    this.stripOrTileByteCountsPosition = stream.getStreamPosition();
                } else if (tagNumber == 273 || tagNumber == 324 || tagNumber == 513) {
                    this.stripOrTileOffsetsPosition = stream.getStreamPosition();
                }
                obj = null;
                try {
                    count = TIFFIFD.readFieldValue(stream, type, count, fieldData);
                    obj = fieldData[0];
                }
                catch (EOFException eofe) {
                    if (BaselineTIFFTagSet.getInstance().getTag(tagNumber) == null) break block27;
                    throw eofe;
                }
            }
            if (obj == null) continue;
            TIFFField f = new TIFFField(tag, type, count, obj);
            this.addTIFFField(f);
        }
        if (isPrimaryIFD && haveStreamLength) {
            this.checkFieldOffsets(streamLength);
        }
        stream.seek(nextIFDOffset);
        this.lastPosition = stream.getStreamPosition();
    }

    public void writeToStream(ImageOutputStream stream) throws IOException {
        int numFields = this.getNumTIFFFields();
        stream.writeShort(numFields);
        long nextSpace = stream.getStreamPosition() + (long)(12 * numFields) + 4L;
        Iterator<TIFFField> iter = this.iterator();
        while (iter.hasNext()) {
            long pos;
            TIFFField f = iter.next();
            TIFFTag tag = f.getTag();
            int type = f.getType();
            int count = f.getCount();
            if (type == 0) {
                type = 7;
            }
            int size = count * TIFFTag.getSizeOfType(type);
            if (type == 2) {
                int chars = 0;
                for (int i = 0; i < count; ++i) {
                    chars += f.getAsString(i).length() + 1;
                }
                size = count = chars;
            }
            int tagNumber = f.getTagNumber();
            stream.writeShort(tagNumber);
            stream.writeShort(type);
            stream.writeInt(count);
            stream.writeInt(0);
            stream.mark();
            stream.skipBytes(-4);
            if (size > 4 || tag.isIFDPointer()) {
                nextSpace = nextSpace + 3L & 0xFFFFFFFFFFFFFFFCL;
                stream.writeInt((int)nextSpace);
                stream.seek(nextSpace);
                pos = nextSpace;
                if (tag.isIFDPointer() && f.hasDirectory()) {
                    TIFFIFD subIFD = TIFFIFD.getDirectoryAsIFD(f.getDirectory());
                    subIFD.writeToStream(stream);
                    nextSpace = subIFD.lastPosition;
                } else {
                    TIFFIFD.writeTIFFFieldToStream(f, stream);
                    nextSpace = stream.getStreamPosition();
                }
            } else {
                pos = stream.getStreamPosition();
                TIFFIFD.writeTIFFFieldToStream(f, stream);
            }
            if (tagNumber == 279 || tagNumber == 325 || tagNumber == 514) {
                this.stripOrTileByteCountsPosition = pos;
            } else if (tagNumber == 273 || tagNumber == 324 || tagNumber == 513) {
                this.stripOrTileOffsetsPosition = pos;
            }
            stream.reset();
        }
        this.lastPosition = nextSpace;
    }

    public long getStripOrTileByteCountsPosition() {
        return this.stripOrTileByteCountsPosition;
    }

    public long getStripOrTileOffsetsPosition() {
        return this.stripOrTileOffsetsPosition;
    }

    public long getLastPosition() {
        return this.lastPosition;
    }

    void setPositions(long stripOrTileOffsetsPosition, long stripOrTileByteCountsPosition, long lastPosition) {
        this.stripOrTileOffsetsPosition = stripOrTileOffsetsPosition;
        this.stripOrTileByteCountsPosition = stripOrTileByteCountsPosition;
        this.lastPosition = lastPosition;
    }

    public TIFFIFD getShallowClone() {
        BaselineTIFFTagSet baselineTagSet = BaselineTIFFTagSet.getInstance();
        List<TIFFTagSet> tagSetList = this.getTagSetList();
        if (!tagSetList.contains(baselineTagSet)) {
            return this;
        }
        TIFFIFD shallowClone = new TIFFIFD(tagSetList, this.getParentTag());
        SortedSet<Integer> baselineTagNumbers = baselineTagSet.getTagNumbers();
        Iterator<TIFFField> fields = this.iterator();
        while (fields.hasNext()) {
            TIFFField fieldClone;
            TIFFField field = fields.next();
            Integer tagNumber = field.getTagNumber();
            if (baselineTagNumbers.contains(tagNumber)) {
                Object fieldData = field.getData();
                int fieldType = field.getType();
                try {
                    switch (fieldType) {
                        case 1: 
                        case 6: 
                        case 7: {
                            fieldData = ((byte[])fieldData).clone();
                            break;
                        }
                        case 2: {
                            fieldData = ((String[])fieldData).clone();
                            break;
                        }
                        case 3: {
                            fieldData = ((char[])fieldData).clone();
                            break;
                        }
                        case 4: 
                        case 13: {
                            fieldData = ((long[])fieldData).clone();
                            break;
                        }
                        case 5: {
                            fieldData = ((long[][])fieldData).clone();
                            break;
                        }
                        case 8: {
                            fieldData = ((short[])fieldData).clone();
                            break;
                        }
                        case 9: {
                            fieldData = ((int[])fieldData).clone();
                            break;
                        }
                        case 10: {
                            fieldData = ((int[][])fieldData).clone();
                            break;
                        }
                        case 11: {
                            fieldData = ((float[])fieldData).clone();
                            break;
                        }
                        case 12: {
                            fieldData = ((double[])fieldData).clone();
                            break;
                        }
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                fieldClone = new TIFFField(field.getTag(), fieldType, field.getCount(), fieldData);
            } else {
                fieldClone = field;
            }
            shallowClone.addTIFFField(fieldClone);
        }
        shallowClone.setPositions(this.stripOrTileOffsetsPosition, this.stripOrTileByteCountsPosition, this.lastPosition);
        return shallowClone;
    }

    private static class TIFFIFDEntry {
        public final TIFFTag tag;
        public final int type;
        public final int count;
        public final long offset;

        TIFFIFDEntry(TIFFTag tag, int type, int count, long offset) {
            this.tag = tag;
            this.type = type;
            this.count = count;
            this.offset = offset;
        }
    }
}

