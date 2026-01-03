/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.FontFormatException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import sun.font.CharToGlyphMapper;
import sun.font.FileFont;
import sun.font.FontScaler;
import sun.font.FontScalerException;
import sun.font.Type1GlyphMapper;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public class Type1Font
extends FileFont {
    WeakReference<ByteBuffer> bufferRef = new WeakReference<Object>(null);
    private String psName = null;
    private static final HashMap<String, String> styleAbbreviationsMapping;
    private static final HashSet<String> styleNameTokes;
    private static final int PSEOFTOKEN = 0;
    private static final int PSNAMETOKEN = 1;
    private static final int PSSTRINGTOKEN = 2;

    public Type1Font(String platname, Object nativeNames) throws FontFormatException {
        this(platname, nativeNames, false);
    }

    public Type1Font(String platname, Object nativeNames, boolean createdCopy) throws FontFormatException {
        super(platname, nativeNames);
        this.fontRank = 4;
        try {
            this.verify();
        }
        catch (Throwable t) {
            if (createdCopy) {
                T1DisposerRecord ref = new T1DisposerRecord(platname);
                Disposer.addObjectRecord(this.bufferRef, ref);
                this.bufferRef = null;
            }
            if (t instanceof FontFormatException) {
                throw (FontFormatException)t;
            }
            throw new FontFormatException("Unexpected runtime exception.");
        }
    }

    private synchronized ByteBuffer getBuffer() throws FontFormatException {
        ByteBuffer bbuf = (ByteBuffer)this.bufferRef.get();
        if (bbuf == null) {
            try {
                RandomAccessFile raf = (RandomAccessFile)AccessController.doPrivileged(new PrivilegedAction<Object>(){

                    @Override
                    public Object run() {
                        try {
                            return new RandomAccessFile(Type1Font.this.platName, "r");
                        }
                        catch (FileNotFoundException fileNotFoundException) {
                            return null;
                        }
                    }
                });
                FileChannel fc = raf.getChannel();
                this.fileSize = (int)fc.size();
                bbuf = ByteBuffer.allocate(this.fileSize);
                fc.read(bbuf);
                bbuf.position(0);
                this.bufferRef = new WeakReference<ByteBuffer>(bbuf);
                fc.close();
            }
            catch (ClosedChannelException e) {
                Thread.interrupted();
                return this.getBuffer();
            }
            catch (IOException | NullPointerException e) {
                throw new FontFormatException(e.toString());
            }
        }
        return bbuf;
    }

    @Override
    protected void close() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void readFile(ByteBuffer buffer) {
        RandomAccessFile raf = null;
        try {
            raf = (RandomAccessFile)AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    try {
                        return new RandomAccessFile(Type1Font.this.platName, "r");
                    }
                    catch (FileNotFoundException fileNotFoundException) {
                        return null;
                    }
                }
            });
            FileChannel fc = raf.getChannel();
            while (buffer.remaining() > 0 && fc.read(buffer) != -1) {
            }
        }
        catch (ClosedChannelException e) {
            try {
                if (raf != null) {
                    raf.close();
                    raf = null;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            Thread.interrupted();
            this.readFile(buffer);
        }
        catch (IOException | NullPointerException exception) {
        }
        finally {
            if (raf != null) {
                try {
                    raf.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    @Override
    public synchronized ByteBuffer readBlock(int offset, int length) {
        ByteBuffer bbuf = null;
        try {
            bbuf = this.getBuffer();
            if (offset > this.fileSize) {
                offset = this.fileSize;
            }
            bbuf.position(offset);
            return bbuf.slice();
        }
        catch (FontFormatException e) {
            return null;
        }
    }

    private void verify() throws FontFormatException {
        ByteBuffer bb = this.getBuffer();
        if (bb.capacity() < 6) {
            throw new FontFormatException("short file");
        }
        int val = bb.get(0) & 0xFF;
        if ((bb.get(0) & 0xFF) == 128) {
            this.verifyPFB(bb);
            bb.position(6);
        } else {
            this.verifyPFA(bb);
            bb.position(0);
        }
        this.initNames(bb);
        if (this.familyName == null || this.fullName == null) {
            throw new FontFormatException("Font name not found");
        }
        this.setStyle();
    }

    public int getFileSize() {
        if (this.fileSize == 0) {
            try {
                this.getBuffer();
            }
            catch (FontFormatException fontFormatException) {
                // empty catch block
            }
        }
        return this.fileSize;
    }

    private void verifyPFA(ByteBuffer bb) throws FontFormatException {
        if (bb.getShort() != 9505) {
            throw new FontFormatException("bad pfa font");
        }
    }

    private void verifyPFB(ByteBuffer bb) throws FontFormatException {
        int pos = 0;
        try {
            int segType;
            while ((segType = bb.getShort(pos) & 0xFFFF) == 32769 || segType == 32770) {
                bb.order(ByteOrder.LITTLE_ENDIAN);
                int segLen = bb.getInt(pos + 2);
                bb.order(ByteOrder.BIG_ENDIAN);
                if (segLen <= 0) {
                    throw new FontFormatException("bad segment length");
                }
                pos += segLen + 6;
            }
            if (segType == 32771) {
                return;
            }
            throw new FontFormatException("bad pfb file");
        }
        catch (Exception e) {
            throw new FontFormatException(e.toString());
        }
    }

    /*
     * Unable to fully structure code
     */
    private void initNames(ByteBuffer bb) throws FontFormatException {
        eof = false;
        fontType = null;
lbl3:
        // 2 sources

        try {
            while (!(this.fullName != null && this.familyName != null && this.psName != null && fontType != null || eof)) {
                block17: {
                    tokenType = this.nextTokenType(bb);
                    if (tokenType != 1) break block17;
                    pos = bb.position();
                    if (bb.get(pos) == 70) {
                        s = this.getSimpleToken(bb);
                        if ("FullName".equals(s)) {
                            if (this.nextTokenType(bb) != 2) continue;
                            this.fullName = this.getString(bb);
                            continue;
                        }
                        if ("FamilyName".equals(s)) {
                            if (this.nextTokenType(bb) != 2) continue;
                            this.familyName = this.getString(bb);
                            continue;
                        }
                        if ("FontName".equals(s)) {
                            if (this.nextTokenType(bb) != 1) continue;
                            this.psName = this.getSimpleToken(bb);
                            continue;
                        }
                        if (!"FontType".equals(s)) continue;
                        token = this.getSimpleToken(bb);
                        if (!"def".equals(this.getSimpleToken(bb))) continue;
                        fontType = token;
                        continue;
                    }
                    while (bb.get() > 32) {
                    }
                    ** GOTO lbl3
                }
                if (tokenType != 0) continue;
                eof = true;
            }
        }
        catch (Exception e) {
            throw new FontFormatException(e.toString());
        }
        if (!"1".equals(fontType)) {
            throw new FontFormatException("Unsupported font type");
        }
        if (this.psName == null) {
            bb.position(0);
            if (bb.getShort() != 9505) {
                bb.position(8);
            }
            if (!(formatType = this.getSimpleToken(bb)).startsWith("FontType1-") && !formatType.startsWith("PS-AdobeFont-")) {
                throw new FontFormatException("Unsupported font format [" + formatType + "]");
            }
            this.psName = this.getSimpleToken(bb);
        }
        if (eof) {
            if (this.fullName != null) {
                this.familyName = this.fullName2FamilyName(this.fullName);
            } else if (this.familyName != null) {
                this.fullName = this.familyName;
            } else {
                this.fullName = this.psName2FullName(this.psName);
                this.familyName = this.psName2FamilyName(this.psName);
            }
        }
    }

    private String fullName2FamilyName(String name) {
        int end = name.length();
        while (end > 0) {
            int start;
            for (start = end - 1; start > 0 && name.charAt(start) != ' '; --start) {
            }
            if (!this.isStyleToken(name.substring(start + 1, end))) {
                return name.substring(0, end);
            }
            end = start;
        }
        return name;
    }

    private String expandAbbreviation(String abbr) {
        return styleAbbreviationsMapping.getOrDefault(abbr, abbr);
    }

    private boolean isStyleToken(String token) {
        return styleNameTokes.contains(token);
    }

    private String psName2FullName(String name) {
        Object res;
        int pos = name.indexOf(45);
        if (pos >= 0) {
            res = this.expandName(name.substring(0, pos), false);
            res = (String)res + " " + this.expandName(name.substring(pos + 1), true);
        } else {
            res = this.expandName(name, false);
        }
        return res;
    }

    private String psName2FamilyName(String name) {
        String tmp = name;
        if (tmp.indexOf(45) > 0) {
            tmp = tmp.substring(0, tmp.indexOf(45));
        }
        return this.expandName(tmp, false);
    }

    private int nextCapitalLetter(String s, int off) {
        while (off >= 0 && off < s.length()) {
            if (s.charAt(off) >= 'A' && s.charAt(off) <= 'Z') {
                return off;
            }
            ++off;
        }
        return -1;
    }

    private String expandName(String s, boolean tryExpandAbbreviations) {
        StringBuilder res = new StringBuilder(s.length() + 10);
        int start = 0;
        while (start < s.length()) {
            int end = this.nextCapitalLetter(s, start + 1);
            if (end < 0) {
                end = s.length();
            }
            if (start != 0) {
                res.append(" ");
            }
            if (tryExpandAbbreviations) {
                res.append(this.expandAbbreviation(s.substring(start, end)));
            } else {
                res.append(s.substring(start, end));
            }
            start = end;
        }
        return res.toString();
    }

    private byte skip(ByteBuffer bb) {
        byte b = bb.get();
        while (b == 37) {
            while ((b = bb.get()) != 13 && b != 10) {
            }
        }
        while (b <= 32) {
            b = bb.get();
        }
        return b;
    }

    private int nextTokenType(ByteBuffer bb) {
        try {
            byte b = this.skip(bb);
            while (true) {
                if (b == 47) {
                    return 1;
                }
                if (b == 40) {
                    return 2;
                }
                if (b == 13 || b == 10) {
                    b = this.skip(bb);
                    continue;
                }
                b = bb.get();
            }
        }
        catch (BufferUnderflowException e) {
            return 0;
        }
    }

    private String getSimpleToken(ByteBuffer bb) {
        while (bb.get() <= 32) {
        }
        int pos1 = bb.position() - 1;
        while (bb.get() > 32) {
        }
        int pos2 = bb.position();
        byte[] nameBytes = new byte[pos2 - pos1 - 1];
        bb.position(pos1);
        bb.get(nameBytes);
        return new String(nameBytes, StandardCharsets.US_ASCII);
    }

    private String getString(ByteBuffer bb) {
        int pos1 = bb.position();
        while (bb.get() != 41) {
        }
        int pos2 = bb.position();
        byte[] nameBytes = new byte[pos2 - pos1 - 1];
        bb.position(pos1);
        bb.get(nameBytes);
        return new String(nameBytes, StandardCharsets.US_ASCII);
    }

    @Override
    public String getPostscriptName() {
        return this.psName;
    }

    @Override
    protected synchronized FontScaler getScaler() {
        if (this.scaler == null) {
            this.scaler = FontScaler.getScaler(this, 0, false, this.fileSize);
        }
        return this.scaler;
    }

    @Override
    CharToGlyphMapper getMapper() {
        if (this.mapper == null) {
            this.mapper = new Type1GlyphMapper(this);
        }
        return this.mapper;
    }

    @Override
    public int getNumGlyphs() {
        try {
            return this.getScaler().getNumGlyphs();
        }
        catch (FontScalerException e) {
            this.scaler = FontScaler.getNullScaler();
            return this.getNumGlyphs();
        }
    }

    @Override
    public int getMissingGlyphCode() {
        try {
            return this.getScaler().getMissingGlyphCode();
        }
        catch (FontScalerException e) {
            this.scaler = FontScaler.getNullScaler();
            return this.getMissingGlyphCode();
        }
    }

    public int getGlyphCode(char charCode) {
        try {
            return this.getScaler().getGlyphCode(charCode);
        }
        catch (FontScalerException e) {
            this.scaler = FontScaler.getNullScaler();
            return this.getGlyphCode(charCode);
        }
    }

    public String toString() {
        return "** Type1 Font: Family=" + this.familyName + " Name=" + this.fullName + " style=" + this.style + " fileName=" + this.getPublicFileName();
    }

    static {
        int i;
        styleAbbreviationsMapping = new HashMap();
        styleNameTokes = new HashSet();
        String[] nm = new String[]{"Black", "Bold", "Book", "Demi", "Heavy", "Light", "Meduium", "Nord", "Poster", "Regular", "Super", "Thin", "Compressed", "Condensed", "Compact", "Extended", "Narrow", "Inclined", "Italic", "Kursiv", "Oblique", "Upright", "Sloped", "Semi", "Ultra", "Extra", "Alternate", "Alternate", "Deutsche Fraktur", "Expert", "Inline", "Ornaments", "Outline", "Roman", "Rounded", "Script", "Shaded", "Swash", "Titling", "Typewriter"};
        String[] abbrv = new String[]{"Blk", "Bd", "Bk", "Dm", "Hv", "Lt", "Md", "Nd", "Po", "Rg", "Su", "Th", "Cm", "Cn", "Ct", "Ex", "Nr", "Ic", "It", "Ks", "Obl", "Up", "Sl", "Sm", "Ult", "X", "A", "Alt", "Dfr", "Exp", "In", "Or", "Ou", "Rm", "Rd", "Scr", "Sh", "Sw", "Ti", "Typ"};
        String[] styleTokens = new String[]{"Black", "Bold", "Book", "Demi", "Heavy", "Light", "Medium", "Nord", "Poster", "Regular", "Super", "Thin", "Compressed", "Condensed", "Compact", "Extended", "Narrow", "Inclined", "Italic", "Kursiv", "Oblique", "Upright", "Sloped", "Slanted", "Semi", "Ultra", "Extra"};
        for (i = 0; i < nm.length; ++i) {
            styleAbbreviationsMapping.put(abbrv[i], nm[i]);
        }
        for (i = 0; i < styleTokens.length; ++i) {
            styleNameTokes.add(styleTokens[i]);
        }
    }

    private static class T1DisposerRecord
    implements DisposerRecord {
        String fileName = null;

        T1DisposerRecord(String name) {
            this.fileName = name;
        }

        @Override
        public synchronized void dispose() {
            AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    if (fileName != null) {
                        new File(fileName).delete();
                    }
                    return null;
                }
            });
        }
    }
}

