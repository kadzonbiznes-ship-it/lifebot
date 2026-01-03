/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.FontFormatException;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import sun.awt.SunToolkit;
import sun.font.CharToGlyphMapper;
import sun.font.FileFont;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontScaler;
import sun.font.FontScalerException;
import sun.font.FontUtilities;
import sun.font.SunFontManager;
import sun.font.TrueTypeGlyphMapper;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.security.action.GetPropertyAction;

public class TrueTypeFont
extends FileFont {
    public static final int cmapTag = 1668112752;
    public static final int glyfTag = 1735162214;
    public static final int headTag = 1751474532;
    public static final int hheaTag = 1751672161;
    public static final int hmtxTag = 1752003704;
    public static final int locaTag = 1819239265;
    public static final int maxpTag = 1835104368;
    public static final int nameTag = 1851878757;
    public static final int postTag = 1886352244;
    public static final int os_2Tag = 1330851634;
    public static final int GDEFTag = 1195656518;
    public static final int GPOSTag = 1196445523;
    public static final int GSUBTag = 1196643650;
    public static final int mortTag = 1836020340;
    public static final int morxTag = 1836020344;
    public static final int fdscTag = 1717859171;
    public static final int fvarTag = 1719034226;
    public static final int featTag = 1717920116;
    public static final int EBLCTag = 1161972803;
    public static final int gaspTag = 1734439792;
    public static final int ttcfTag = 1953784678;
    public static final int v1ttTag = 65536;
    public static final int trueTag = 1953658213;
    public static final int ottoTag = 0x4F54544F;
    public static final int MAC_PLATFORM_ID = 1;
    public static final int MACROMAN_SPECIFIC_ID = 0;
    public static final int MACROMAN_ENGLISH_LANG = 0;
    public static final int MS_PLATFORM_ID = 3;
    public static final short ENGLISH_LOCALE_ID = 1033;
    public static final int FAMILY_NAME_ID = 1;
    public static final int FULL_NAME_ID = 4;
    public static final int POSTSCRIPT_NAME_ID = 6;
    private static final short US_LCID = 1033;
    private static Map<String, Short> lcidMap;
    TTDisposerRecord disposerRecord = new TTDisposerRecord();
    int fontIndex = 0;
    int directoryCount = 1;
    int directoryOffset;
    int numTables;
    DirectoryEntry[] tableDirectory;
    private boolean supportsJA;
    private boolean supportsCJK;
    private Locale nameLocale;
    private String localeFamilyName;
    private String localeFullName;
    int fontDataSize;
    private static final int TTCHEADERSIZE = 12;
    private static final int DIRECTORYHEADERSIZE = 12;
    private static final int DIRECTORYENTRYSIZE = 16;
    static final String[] encoding_mapping;
    private static final String[][] languages;
    private static final String[] codePages;
    private static String defaultCodePage;
    public static final int reserved_bits1 = Integer.MIN_VALUE;
    public static final int reserved_bits2 = 65535;
    private int fontWidth = 0;
    private int fontWeight = 0;
    private static final int fsSelectionItalicBit = 1;
    private static final int fsSelectionBoldBit = 32;
    private static final int fsSelectionRegularBit = 64;
    private float stSize;
    private float stPos;
    private float ulSize;
    private float ulPos;
    private char[] gaspTable;
    private static Map<String, short[]> lcidLanguageCompatibilityMap;
    private static final short[] EMPTY_COMPATIBLE_LCIDS;
    private short[] languageCompatibleLCIDs;

    public TrueTypeFont(String platname, Object nativeNames, int fIndex, boolean javaRasterizer) throws FontFormatException {
        this(platname, nativeNames, fIndex, javaRasterizer, true);
    }

    public TrueTypeFont(String platname, Object nativeNames, int fIndex, boolean javaRasterizer, boolean useFilePool) throws FontFormatException {
        super(platname, nativeNames);
        this.useJavaRasterizer = javaRasterizer;
        this.fontRank = 3;
        try {
            this.verify(useFilePool);
            this.init(fIndex);
            if (!useFilePool) {
                this.close();
            }
        }
        catch (Throwable t) {
            this.close();
            if (t instanceof FontFormatException) {
                throw (FontFormatException)t;
            }
            throw new FontFormatException("Unexpected runtime exception.");
        }
        Disposer.addObjectRecord(this, this.disposerRecord);
    }

    private synchronized FileChannel open() throws FontFormatException {
        return this.open(true);
    }

    private synchronized FileChannel open(boolean usePool) throws FontFormatException {
        if (this.disposerRecord.channel == null) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logInfo("open TTF: " + this.platName);
            }
            try {
                FontManager fm;
                RandomAccessFile raf = AccessController.doPrivileged(new PrivilegedExceptionAction<RandomAccessFile>(){

                    @Override
                    public RandomAccessFile run() throws FileNotFoundException {
                        return new RandomAccessFile(TrueTypeFont.this.platName, "r");
                    }
                });
                this.disposerRecord.channel = raf.getChannel();
                this.fileSize = (int)this.disposerRecord.channel.size();
                if (usePool && (fm = FontManagerFactory.getInstance()) instanceof SunFontManager) {
                    ((SunFontManager)fm).addToPool(this);
                }
            }
            catch (PrivilegedActionException e) {
                this.close();
                Throwable reason = e.getCause();
                if (reason == null) {
                    reason = e;
                }
                throw new FontFormatException(reason.toString());
            }
            catch (ClosedChannelException e) {
                Thread.interrupted();
                this.close();
                this.open();
            }
            catch (IOException e) {
                this.close();
                throw new FontFormatException(e.toString());
            }
        }
        return this.disposerRecord.channel;
    }

    @Override
    protected synchronized void close() {
        this.disposerRecord.dispose();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int readBlock(ByteBuffer buffer, int offset, int length) {
        int bread;
        block21: {
            bread = 0;
            try {
                TrueTypeFont trueTypeFont = this;
                synchronized (trueTypeFont) {
                    if (this.disposerRecord.channel == null) {
                        this.open();
                    }
                    if (offset + length > this.fileSize) {
                        if (offset >= this.fileSize) {
                            if (FontUtilities.isLogging()) {
                                String msg = "Read offset is " + offset + " file size is " + this.fileSize + " file is " + this.platName;
                                FontUtilities.logSevere(msg);
                            }
                            return -1;
                        }
                        length = this.fileSize - offset;
                    }
                    buffer.clear();
                    this.disposerRecord.channel.position(offset);
                    while (bread < length) {
                        int cnt = this.disposerRecord.channel.read(buffer);
                        if (cnt == -1) {
                            String msg = "Unexpected EOF " + String.valueOf(this);
                            int currSize = (int)this.disposerRecord.channel.size();
                            if (currSize != this.fileSize) {
                                msg = msg + " File size was " + this.fileSize + " and now is " + currSize;
                            }
                            if (FontUtilities.isLogging()) {
                                FontUtilities.logSevere(msg);
                            }
                            if (bread > length / 2 || bread > 16384) {
                                buffer.flip();
                                if (FontUtilities.isLogging()) {
                                    msg = "Returning " + bread + " bytes instead of " + length;
                                    FontUtilities.logSevere(msg);
                                }
                            } else {
                                bread = -1;
                            }
                            throw new IOException(msg);
                        }
                        bread += cnt;
                    }
                    buffer.flip();
                    if (bread > length) {
                        bread = length;
                    }
                }
            }
            catch (FontFormatException e) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger().severe("While reading " + this.platName, e);
                }
                bread = -1;
                this.deregisterFontAndClearStrikeCache();
            }
            catch (ClosedChannelException e) {
                Thread.interrupted();
                this.close();
                return this.readBlock(buffer, offset, length);
            }
            catch (IOException e) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger().severe("While reading " + this.platName, e);
                }
                if (bread != 0) break block21;
                bread = -1;
                this.deregisterFontAndClearStrikeCache();
            }
        }
        return bread;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    ByteBuffer readBlock(int offset, int length) {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        try {
            TrueTypeFont trueTypeFont = this;
            synchronized (trueTypeFont) {
                if (this.disposerRecord.channel == null) {
                    this.open();
                }
                if (offset + length > this.fileSize) {
                    if (offset > this.fileSize) {
                        return null;
                    }
                    buffer = ByteBuffer.allocate(this.fileSize - offset);
                }
                this.disposerRecord.channel.position(offset);
                this.disposerRecord.channel.read(buffer);
                buffer.flip();
            }
        }
        catch (ClosedChannelException e) {
            Thread.interrupted();
            this.close();
            this.readBlock(buffer, offset, length);
        }
        catch (FontFormatException | IOException e) {
            return null;
        }
        return buffer;
    }

    byte[] readBytes(int offset, int length) {
        ByteBuffer buffer = this.readBlock(offset, length);
        if (buffer.hasArray()) {
            return buffer.array();
        }
        byte[] bufferBytes = new byte[buffer.limit()];
        buffer.get(bufferBytes);
        return bufferBytes;
    }

    private void verify(boolean usePool) throws FontFormatException {
        this.open(usePool);
    }

    protected void init(int fIndex) throws FontFormatException {
        int headerOffset = 0;
        ByteBuffer buffer = this.readBlock(0, 12);
        try {
            switch (buffer.getInt()) {
                case 1953784678: {
                    buffer.getInt();
                    this.directoryCount = buffer.getInt();
                    if (fIndex >= this.directoryCount) {
                        throw new FontFormatException("Bad collection index");
                    }
                    this.fontIndex = fIndex;
                    buffer = this.readBlock(12 + 4 * fIndex, 4);
                    headerOffset = buffer.getInt();
                    this.fontDataSize = Math.max(0, this.fileSize - headerOffset);
                    break;
                }
                case 65536: 
                case 0x4F54544F: 
                case 1953658213: {
                    this.fontDataSize = this.fileSize;
                    break;
                }
                default: {
                    throw new FontFormatException("Unsupported sfnt " + this.getPublicFileName());
                }
            }
            buffer = this.readBlock(headerOffset + 4, 2);
            this.numTables = buffer.getShort();
            this.directoryOffset = headerOffset + 12;
            ByteBuffer bbuffer = this.readBlock(this.directoryOffset, this.numTables * 16);
            IntBuffer ibuffer = bbuffer.asIntBuffer();
            this.tableDirectory = new DirectoryEntry[this.numTables];
            for (int i = 0; i < this.numTables; ++i) {
                DirectoryEntry table;
                this.tableDirectory[i] = table = new DirectoryEntry();
                table.tag = ibuffer.get();
                ibuffer.get();
                table.offset = ibuffer.get() & Integer.MAX_VALUE;
                table.length = ibuffer.get() & Integer.MAX_VALUE;
                if (table.offset + table.length >= table.length && table.offset + table.length <= this.fileSize) continue;
                throw new FontFormatException("bad table, tag=" + table.tag);
            }
            if (this.getDirectoryEntry(1751474532) == null) {
                throw new FontFormatException("missing head table");
            }
            if (this.getDirectoryEntry(1835104368) == null) {
                throw new FontFormatException("missing maxp table");
            }
            if (this.getDirectoryEntry(1752003704) != null && this.getDirectoryEntry(1751672161) == null) {
                throw new FontFormatException("missing hhea table");
            }
            ByteBuffer maxpTable = this.getTableBuffer(1835104368);
            if (maxpTable.getChar(4) == '\u0000') {
                throw new FontFormatException("zero glyphs");
            }
            this.initNames();
        }
        catch (Exception e) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logSevere(e.toString());
            }
            if (e instanceof FontFormatException) {
                throw (FontFormatException)e;
            }
            throw new FontFormatException(e.toString());
        }
        if (this.familyName == null || this.fullName == null) {
            throw new FontFormatException("Font name not found");
        }
        ByteBuffer os2_Table = this.getTableBuffer(1330851634);
        this.setStyle(os2_Table);
        this.setCJKSupport(os2_Table);
    }

    static String getCodePage() {
        if (defaultCodePage != null) {
            return defaultCodePage;
        }
        if (FontUtilities.isWindows) {
            defaultCodePage = AccessController.doPrivileged(new GetPropertyAction("file.encoding"));
        } else {
            if (languages.length != codePages.length) {
                throw new InternalError("wrong code pages array length");
            }
            Locale locale = SunToolkit.getStartupLocale();
            Object language = locale.getLanguage();
            if (language != null) {
                String country;
                if (((String)language).equals("zh") && (country = locale.getCountry()) != null) {
                    language = (String)language + "_" + country;
                }
                for (int i = 0; i < languages.length; ++i) {
                    for (int l = 0; l < languages[i].length; ++l) {
                        if (!((String)language).equals(languages[i][l])) continue;
                        defaultCodePage = codePages[i];
                        return defaultCodePage;
                    }
                }
            }
        }
        if (defaultCodePage == null) {
            defaultCodePage = "";
        }
        return defaultCodePage;
    }

    @Override
    boolean supportsEncoding(String encoding) {
        if (encoding == null) {
            encoding = TrueTypeFont.getCodePage();
        }
        if ("".equals(encoding)) {
            return false;
        }
        if ((encoding = encoding.toLowerCase()).equals("gb18030")) {
            encoding = "gbk";
        } else if (encoding.equals("ms950_hkscs")) {
            encoding = "ms950";
        }
        ByteBuffer buffer = this.getTableBuffer(1330851634);
        if (buffer == null || buffer.capacity() < 86) {
            return false;
        }
        int range1 = buffer.getInt(78);
        for (int em = 0; em < encoding_mapping.length; ++em) {
            if (!encoding_mapping[em].equals(encoding) || (1 << em & range1) == 0) continue;
            return true;
        }
        return false;
    }

    private void setCJKSupport(ByteBuffer os2Table) {
        if (os2Table == null || os2Table.capacity() < 50) {
            return;
        }
        int range2 = os2Table.getInt(46);
        this.supportsCJK = (range2 & 0x29BF0000) != 0;
        this.supportsJA = (range2 & 0x60000) != 0;
    }

    boolean supportsJA() {
        return this.supportsJA;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ByteBuffer getTableBuffer(int tag) {
        DirectoryEntry entry = null;
        for (int i = 0; i < this.numTables; ++i) {
            if (this.tableDirectory[i].tag != tag) continue;
            entry = this.tableDirectory[i];
            break;
        }
        if (entry == null || entry.length == 0 || entry.offset + entry.length < entry.length || entry.offset + entry.length > this.fileSize) {
            return null;
        }
        int bread = 0;
        ByteBuffer buffer = ByteBuffer.allocate(entry.length);
        TrueTypeFont trueTypeFont = this;
        synchronized (trueTypeFont) {
            try {
                if (this.disposerRecord.channel == null) {
                    this.open();
                }
                this.disposerRecord.channel.position(entry.offset);
                bread = this.disposerRecord.channel.read(buffer);
                buffer.flip();
            }
            catch (ClosedChannelException e) {
                Thread.interrupted();
                this.close();
                return this.getTableBuffer(tag);
            }
            catch (FontFormatException | IOException e) {
                return null;
            }
            if (bread < entry.length) {
                return null;
            }
            return buffer;
        }
    }

    @Override
    protected byte[] getTableBytes(int tag) {
        ByteBuffer buffer = this.getTableBuffer(tag);
        if (buffer == null) {
            return null;
        }
        if (buffer.hasArray()) {
            try {
                return buffer.array();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        byte[] data = new byte[this.getTableSize(tag)];
        buffer.get(data);
        return data;
    }

    int getTableSize(int tag) {
        for (int i = 0; i < this.numTables; ++i) {
            if (this.tableDirectory[i].tag != tag) continue;
            return this.tableDirectory[i].length;
        }
        return 0;
    }

    int getTableOffset(int tag) {
        for (int i = 0; i < this.numTables; ++i) {
            if (this.tableDirectory[i].tag != tag) continue;
            return this.tableDirectory[i].offset;
        }
        return 0;
    }

    DirectoryEntry getDirectoryEntry(int tag) {
        for (int i = 0; i < this.numTables; ++i) {
            if (this.tableDirectory[i].tag != tag) continue;
            return this.tableDirectory[i];
        }
        return null;
    }

    boolean useEmbeddedBitmapsForSize(int ptSize) {
        if (!this.supportsCJK) {
            return false;
        }
        if (this.getDirectoryEntry(1161972803) == null) {
            return false;
        }
        ByteBuffer eblcTable = this.getTableBuffer(1161972803);
        if (eblcTable == null) {
            return false;
        }
        int numSizes = eblcTable.getInt(4);
        for (int i = 0; i < numSizes; ++i) {
            int ppemY = eblcTable.get(8 + i * 48 + 45) & 0xFF;
            if (ppemY != ptSize) continue;
            return true;
        }
        return false;
    }

    public String getFullName() {
        return this.fullName;
    }

    @Override
    protected void setStyle() {
        this.setStyle(this.getTableBuffer(1330851634));
    }

    @Override
    public int getWidth() {
        return this.fontWidth > 0 ? this.fontWidth : super.getWidth();
    }

    @Override
    public int getWeight() {
        return this.fontWeight > 0 ? this.fontWeight : super.getWeight();
    }

    private void setStyle(ByteBuffer os_2Table) {
        if (os_2Table == null) {
            return;
        }
        if (os_2Table.capacity() >= 8) {
            this.fontWeight = os_2Table.getChar(4) & 0xFFFF;
            this.fontWidth = os_2Table.getChar(6) & 0xFFFF;
        }
        if (os_2Table.capacity() < 64) {
            super.setStyle();
            return;
        }
        int fsSelection = os_2Table.getChar(62) & 0xFFFF;
        int italic = fsSelection & 1;
        int bold = fsSelection & 0x20;
        int regular = fsSelection & 0x40;
        if (regular != 0 && (italic | bold) != 0) {
            super.setStyle();
            return;
        }
        if ((regular | italic | bold) == 0) {
            super.setStyle();
            return;
        }
        switch (bold | italic) {
            case 1: {
                this.style = 2;
                break;
            }
            case 32: {
                this.style = 1;
                break;
            }
            case 33: {
                this.style = 3;
            }
        }
    }

    private void setStrikethroughMetrics(ByteBuffer os_2Table, int upem) {
        if (os_2Table == null || os_2Table.capacity() < 30 || upem < 0) {
            this.stSize = 0.05f;
            this.stPos = -0.4f;
            return;
        }
        ShortBuffer sb = os_2Table.asShortBuffer();
        this.stSize = (float)sb.get(13) / (float)upem;
        this.stPos = (float)(-sb.get(14)) / (float)upem;
        if (this.stSize < 0.0f) {
            this.stSize = 0.05f;
        }
        if (Math.abs(this.stPos) > 2.0f) {
            this.stPos = -0.4f;
        }
    }

    private void setUnderlineMetrics(ByteBuffer postTable, int upem) {
        if (postTable == null || postTable.capacity() < 12 || upem < 0) {
            this.ulSize = 0.05f;
            this.ulPos = 0.1f;
            return;
        }
        ShortBuffer sb = postTable.asShortBuffer();
        this.ulSize = (float)sb.get(5) / (float)upem;
        this.ulPos = (float)(-sb.get(4)) / (float)upem;
        if (this.ulSize < 0.0f) {
            this.ulSize = 0.05f;
        }
        if (Math.abs(this.ulPos) > 2.0f) {
            this.ulPos = 0.1f;
        }
    }

    @Override
    public void getStyleMetrics(float pointSize, float[] metrics, int offset) {
        if (this.ulSize == 0.0f && this.ulPos == 0.0f) {
            ShortBuffer sb;
            ByteBuffer head_Table = this.getTableBuffer(1751474532);
            int upem = -1;
            if (head_Table != null && head_Table.capacity() >= 18 && ((upem = (sb = head_Table.asShortBuffer()).get(9) & 0xFFFF) < 16 || upem > 16384)) {
                upem = 2048;
            }
            ByteBuffer os2_Table = this.getTableBuffer(1330851634);
            this.setStrikethroughMetrics(os2_Table, upem);
            ByteBuffer post_Table = this.getTableBuffer(1886352244);
            this.setUnderlineMetrics(post_Table, upem);
        }
        metrics[offset] = this.stPos * pointSize;
        metrics[offset + 1] = this.stSize * pointSize;
        metrics[offset + 2] = this.ulPos * pointSize;
        metrics[offset + 3] = this.ulSize * pointSize;
    }

    private String makeString(byte[] bytes, int len, short platformID, short encoding) {
        if (platformID == 1) {
            encoding = (short)-1;
        }
        if (encoding >= 2 && encoding <= 6) {
            byte[] oldbytes = bytes;
            int oldlen = len;
            bytes = new byte[oldlen];
            len = 0;
            for (int i = 0; i < oldlen; ++i) {
                if (oldbytes[i] == 0) continue;
                bytes[len++] = oldbytes[i];
            }
        }
        String charset = switch (encoding) {
            case -1 -> "US-ASCII";
            case 1 -> "UTF-16";
            case 0 -> "UTF-16";
            case 2 -> "SJIS";
            case 3 -> "GBK";
            case 4 -> "MS950";
            case 5 -> "EUC_KR";
            case 6 -> "Johab";
            default -> "UTF-16";
        };
        try {
            return new String(bytes, 0, len, charset);
        }
        catch (UnsupportedEncodingException e) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logWarning(String.valueOf(e) + " EncodingID=" + encoding);
            }
            return new String(bytes, 0, len);
        }
        catch (Throwable t) {
            return null;
        }
    }

    protected void initNames() {
        byte[] name = new byte[256];
        ByteBuffer buffer = this.getTableBuffer(1851878757);
        if (buffer != null) {
            ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.get();
            int numRecords = sbuffer.get();
            int stringPtr = sbuffer.get() & 0xFFFF;
            this.nameLocale = SunToolkit.getStartupLocale();
            short nameLocaleID = TrueTypeFont.getLCIDFromLocale(this.nameLocale);
            this.languageCompatibleLCIDs = TrueTypeFont.getLanguageCompatibleLCIDsFromLocale(this.nameLocale);
            block4: for (int i = 0; i < numRecords; ++i) {
                short platformID = sbuffer.get();
                if (platformID != 3 && platformID != 1) {
                    sbuffer.position(sbuffer.position() + 5);
                    continue;
                }
                short encodingID = sbuffer.get();
                short langID = sbuffer.get();
                short nameID = sbuffer.get();
                int nameLen = sbuffer.get() & 0xFFFF;
                int namePtr = (sbuffer.get() & 0xFFFF) + stringPtr;
                String tmpName = null;
                if (platformID == 1 && (encodingID != 0 || langID != 0)) continue;
                switch (nameID) {
                    case 1: {
                        boolean compatible = false;
                        if (this.familyName != null && langID != 1033 && langID != nameLocaleID && (this.localeFamilyName != null || !(compatible = this.isLanguageCompatible(langID)))) continue block4;
                        buffer.position(namePtr);
                        buffer.get(name, 0, nameLen);
                        tmpName = this.makeString(name, nameLen, platformID, encodingID);
                        if (this.familyName == null || langID == 1033) {
                            this.familyName = tmpName;
                        }
                        if (langID != nameLocaleID && (this.localeFamilyName != null || !compatible)) continue block4;
                        this.localeFamilyName = tmpName;
                        continue block4;
                    }
                    case 4: {
                        boolean compatible = false;
                        if (this.fullName != null && langID != 1033 && langID != nameLocaleID && (this.localeFullName != null || !(compatible = this.isLanguageCompatible(langID)))) continue block4;
                        buffer.position(namePtr);
                        buffer.get(name, 0, nameLen);
                        tmpName = this.makeString(name, nameLen, platformID, encodingID);
                        if (this.fullName == null || langID == 1033) {
                            this.fullName = tmpName;
                        }
                        if (langID != nameLocaleID && (this.localeFullName != null || !compatible)) continue block4;
                        this.localeFullName = tmpName;
                    }
                }
            }
            if (this.localeFamilyName == null) {
                this.localeFamilyName = this.familyName;
            }
            if (this.localeFullName == null) {
                this.localeFullName = this.fullName;
            }
        }
    }

    protected String lookupName(short findLocaleID, int findNameID) {
        String foundName = null;
        byte[] name = new byte[1024];
        ByteBuffer buffer = this.getTableBuffer(1851878757);
        if (buffer != null) {
            ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.get();
            int numRecords = sbuffer.get();
            int stringPtr = sbuffer.get() & 0xFFFF;
            for (int i = 0; i < numRecords; ++i) {
                short platformID = sbuffer.get();
                if (platformID != 3) {
                    sbuffer.position(sbuffer.position() + 5);
                    continue;
                }
                short encodingID = sbuffer.get();
                short langID = sbuffer.get();
                short nameID = sbuffer.get();
                int nameLen = sbuffer.get() & 0xFFFF;
                int namePtr = (sbuffer.get() & 0xFFFF) + stringPtr;
                if (nameID != findNameID || (foundName != null || langID != 1033) && langID != findLocaleID) continue;
                buffer.position(namePtr);
                buffer.get(name, 0, nameLen);
                foundName = this.makeString(name, nameLen, platformID, encodingID);
                if (langID != findLocaleID) continue;
                return foundName;
            }
        }
        return foundName;
    }

    public int getFontCount() {
        return this.directoryCount;
    }

    @Override
    protected synchronized FontScaler getScaler() {
        if (this.scaler == null) {
            this.scaler = FontScaler.getScaler(this, this.fontIndex, this.supportsCJK, this.fileSize);
        }
        return this.scaler;
    }

    @Override
    public String getPostscriptName() {
        String name = this.lookupName((short)1033, 6);
        if (name == null) {
            return this.fullName;
        }
        return name;
    }

    @Override
    public String getFontName(Locale locale) {
        if (locale == null) {
            return this.fullName;
        }
        if (locale.equals(this.nameLocale) && this.localeFullName != null) {
            return this.localeFullName;
        }
        short localeID = TrueTypeFont.getLCIDFromLocale(locale);
        String name = this.lookupName(localeID, 4);
        if (name == null) {
            return this.fullName;
        }
        return name;
    }

    private static void addLCIDMapEntry(Map<String, Short> map, String key, short value) {
        map.put(key, value);
    }

    private static synchronized void createLCIDMap() {
        if (lcidMap != null) {
            return;
        }
        HashMap<String, Short> map = new HashMap<String, Short>(200);
        TrueTypeFont.addLCIDMapEntry(map, "ar", (short)1025);
        TrueTypeFont.addLCIDMapEntry(map, "bg", (short)1026);
        TrueTypeFont.addLCIDMapEntry(map, "ca", (short)1027);
        TrueTypeFont.addLCIDMapEntry(map, "zh", (short)1028);
        TrueTypeFont.addLCIDMapEntry(map, "cs", (short)1029);
        TrueTypeFont.addLCIDMapEntry(map, "da", (short)1030);
        TrueTypeFont.addLCIDMapEntry(map, "de", (short)1031);
        TrueTypeFont.addLCIDMapEntry(map, "el", (short)1032);
        TrueTypeFont.addLCIDMapEntry(map, "es", (short)1034);
        TrueTypeFont.addLCIDMapEntry(map, "fi", (short)1035);
        TrueTypeFont.addLCIDMapEntry(map, "fr", (short)1036);
        TrueTypeFont.addLCIDMapEntry(map, "iw", (short)1037);
        TrueTypeFont.addLCIDMapEntry(map, "hu", (short)1038);
        TrueTypeFont.addLCIDMapEntry(map, "is", (short)1039);
        TrueTypeFont.addLCIDMapEntry(map, "it", (short)1040);
        TrueTypeFont.addLCIDMapEntry(map, "ja", (short)1041);
        TrueTypeFont.addLCIDMapEntry(map, "ko", (short)1042);
        TrueTypeFont.addLCIDMapEntry(map, "nl", (short)1043);
        TrueTypeFont.addLCIDMapEntry(map, "no", (short)1044);
        TrueTypeFont.addLCIDMapEntry(map, "pl", (short)1045);
        TrueTypeFont.addLCIDMapEntry(map, "pt", (short)1046);
        TrueTypeFont.addLCIDMapEntry(map, "rm", (short)1047);
        TrueTypeFont.addLCIDMapEntry(map, "ro", (short)1048);
        TrueTypeFont.addLCIDMapEntry(map, "ru", (short)1049);
        TrueTypeFont.addLCIDMapEntry(map, "hr", (short)1050);
        TrueTypeFont.addLCIDMapEntry(map, "sk", (short)1051);
        TrueTypeFont.addLCIDMapEntry(map, "sq", (short)1052);
        TrueTypeFont.addLCIDMapEntry(map, "sv", (short)1053);
        TrueTypeFont.addLCIDMapEntry(map, "th", (short)1054);
        TrueTypeFont.addLCIDMapEntry(map, "tr", (short)1055);
        TrueTypeFont.addLCIDMapEntry(map, "ur", (short)1056);
        TrueTypeFont.addLCIDMapEntry(map, "in", (short)1057);
        TrueTypeFont.addLCIDMapEntry(map, "uk", (short)1058);
        TrueTypeFont.addLCIDMapEntry(map, "be", (short)1059);
        TrueTypeFont.addLCIDMapEntry(map, "sl", (short)1060);
        TrueTypeFont.addLCIDMapEntry(map, "et", (short)1061);
        TrueTypeFont.addLCIDMapEntry(map, "lv", (short)1062);
        TrueTypeFont.addLCIDMapEntry(map, "lt", (short)1063);
        TrueTypeFont.addLCIDMapEntry(map, "fa", (short)1065);
        TrueTypeFont.addLCIDMapEntry(map, "vi", (short)1066);
        TrueTypeFont.addLCIDMapEntry(map, "hy", (short)1067);
        TrueTypeFont.addLCIDMapEntry(map, "eu", (short)1069);
        TrueTypeFont.addLCIDMapEntry(map, "mk", (short)1071);
        TrueTypeFont.addLCIDMapEntry(map, "tn", (short)1074);
        TrueTypeFont.addLCIDMapEntry(map, "xh", (short)1076);
        TrueTypeFont.addLCIDMapEntry(map, "zu", (short)1077);
        TrueTypeFont.addLCIDMapEntry(map, "af", (short)1078);
        TrueTypeFont.addLCIDMapEntry(map, "ka", (short)1079);
        TrueTypeFont.addLCIDMapEntry(map, "fo", (short)1080);
        TrueTypeFont.addLCIDMapEntry(map, "hi", (short)1081);
        TrueTypeFont.addLCIDMapEntry(map, "mt", (short)1082);
        TrueTypeFont.addLCIDMapEntry(map, "se", (short)1083);
        TrueTypeFont.addLCIDMapEntry(map, "gd", (short)1084);
        TrueTypeFont.addLCIDMapEntry(map, "ms", (short)1086);
        TrueTypeFont.addLCIDMapEntry(map, "kk", (short)1087);
        TrueTypeFont.addLCIDMapEntry(map, "ky", (short)1088);
        TrueTypeFont.addLCIDMapEntry(map, "sw", (short)1089);
        TrueTypeFont.addLCIDMapEntry(map, "tt", (short)1092);
        TrueTypeFont.addLCIDMapEntry(map, "bn", (short)1093);
        TrueTypeFont.addLCIDMapEntry(map, "pa", (short)1094);
        TrueTypeFont.addLCIDMapEntry(map, "gu", (short)1095);
        TrueTypeFont.addLCIDMapEntry(map, "ta", (short)1097);
        TrueTypeFont.addLCIDMapEntry(map, "te", (short)1098);
        TrueTypeFont.addLCIDMapEntry(map, "kn", (short)1099);
        TrueTypeFont.addLCIDMapEntry(map, "ml", (short)1100);
        TrueTypeFont.addLCIDMapEntry(map, "mr", (short)1102);
        TrueTypeFont.addLCIDMapEntry(map, "sa", (short)1103);
        TrueTypeFont.addLCIDMapEntry(map, "mn", (short)1104);
        TrueTypeFont.addLCIDMapEntry(map, "cy", (short)1106);
        TrueTypeFont.addLCIDMapEntry(map, "gl", (short)1110);
        TrueTypeFont.addLCIDMapEntry(map, "dv", (short)1125);
        TrueTypeFont.addLCIDMapEntry(map, "qu", (short)1131);
        TrueTypeFont.addLCIDMapEntry(map, "mi", (short)1153);
        TrueTypeFont.addLCIDMapEntry(map, "ar_IQ", (short)2049);
        TrueTypeFont.addLCIDMapEntry(map, "zh_CN", (short)2052);
        TrueTypeFont.addLCIDMapEntry(map, "de_CH", (short)2055);
        TrueTypeFont.addLCIDMapEntry(map, "en_GB", (short)2057);
        TrueTypeFont.addLCIDMapEntry(map, "es_MX", (short)2058);
        TrueTypeFont.addLCIDMapEntry(map, "fr_BE", (short)2060);
        TrueTypeFont.addLCIDMapEntry(map, "it_CH", (short)2064);
        TrueTypeFont.addLCIDMapEntry(map, "nl_BE", (short)2067);
        TrueTypeFont.addLCIDMapEntry(map, "no_NO_NY", (short)2068);
        TrueTypeFont.addLCIDMapEntry(map, "pt_PT", (short)2070);
        TrueTypeFont.addLCIDMapEntry(map, "ro_MD", (short)2072);
        TrueTypeFont.addLCIDMapEntry(map, "ru_MD", (short)2073);
        TrueTypeFont.addLCIDMapEntry(map, "sr_CS", (short)2074);
        TrueTypeFont.addLCIDMapEntry(map, "sv_FI", (short)2077);
        TrueTypeFont.addLCIDMapEntry(map, "az_AZ", (short)2092);
        TrueTypeFont.addLCIDMapEntry(map, "se_SE", (short)2107);
        TrueTypeFont.addLCIDMapEntry(map, "ga_IE", (short)2108);
        TrueTypeFont.addLCIDMapEntry(map, "ms_BN", (short)2110);
        TrueTypeFont.addLCIDMapEntry(map, "uz_UZ", (short)2115);
        TrueTypeFont.addLCIDMapEntry(map, "qu_EC", (short)2155);
        TrueTypeFont.addLCIDMapEntry(map, "ar_EG", (short)3073);
        TrueTypeFont.addLCIDMapEntry(map, "zh_HK", (short)3076);
        TrueTypeFont.addLCIDMapEntry(map, "de_AT", (short)3079);
        TrueTypeFont.addLCIDMapEntry(map, "en_AU", (short)3081);
        TrueTypeFont.addLCIDMapEntry(map, "fr_CA", (short)3084);
        TrueTypeFont.addLCIDMapEntry(map, "sr_CS", (short)3098);
        TrueTypeFont.addLCIDMapEntry(map, "se_FI", (short)3131);
        TrueTypeFont.addLCIDMapEntry(map, "qu_PE", (short)3179);
        TrueTypeFont.addLCIDMapEntry(map, "ar_LY", (short)4097);
        TrueTypeFont.addLCIDMapEntry(map, "zh_SG", (short)4100);
        TrueTypeFont.addLCIDMapEntry(map, "de_LU", (short)4103);
        TrueTypeFont.addLCIDMapEntry(map, "en_CA", (short)4105);
        TrueTypeFont.addLCIDMapEntry(map, "es_GT", (short)4106);
        TrueTypeFont.addLCIDMapEntry(map, "fr_CH", (short)4108);
        TrueTypeFont.addLCIDMapEntry(map, "hr_BA", (short)4122);
        TrueTypeFont.addLCIDMapEntry(map, "ar_DZ", (short)5121);
        TrueTypeFont.addLCIDMapEntry(map, "zh_MO", (short)5124);
        TrueTypeFont.addLCIDMapEntry(map, "de_LI", (short)5127);
        TrueTypeFont.addLCIDMapEntry(map, "en_NZ", (short)5129);
        TrueTypeFont.addLCIDMapEntry(map, "es_CR", (short)5130);
        TrueTypeFont.addLCIDMapEntry(map, "fr_LU", (short)5132);
        TrueTypeFont.addLCIDMapEntry(map, "bs_BA", (short)5146);
        TrueTypeFont.addLCIDMapEntry(map, "ar_MA", (short)6145);
        TrueTypeFont.addLCIDMapEntry(map, "en_IE", (short)6153);
        TrueTypeFont.addLCIDMapEntry(map, "es_PA", (short)6154);
        TrueTypeFont.addLCIDMapEntry(map, "fr_MC", (short)6156);
        TrueTypeFont.addLCIDMapEntry(map, "sr_BA", (short)6170);
        TrueTypeFont.addLCIDMapEntry(map, "ar_TN", (short)7169);
        TrueTypeFont.addLCIDMapEntry(map, "en_ZA", (short)7177);
        TrueTypeFont.addLCIDMapEntry(map, "es_DO", (short)7178);
        TrueTypeFont.addLCIDMapEntry(map, "sr_BA", (short)7194);
        TrueTypeFont.addLCIDMapEntry(map, "ar_OM", (short)8193);
        TrueTypeFont.addLCIDMapEntry(map, "en_JM", (short)8201);
        TrueTypeFont.addLCIDMapEntry(map, "es_VE", (short)8202);
        TrueTypeFont.addLCIDMapEntry(map, "ar_YE", (short)9217);
        TrueTypeFont.addLCIDMapEntry(map, "es_CO", (short)9226);
        TrueTypeFont.addLCIDMapEntry(map, "ar_SY", (short)10241);
        TrueTypeFont.addLCIDMapEntry(map, "en_BZ", (short)10249);
        TrueTypeFont.addLCIDMapEntry(map, "es_PE", (short)10250);
        TrueTypeFont.addLCIDMapEntry(map, "ar_JO", (short)11265);
        TrueTypeFont.addLCIDMapEntry(map, "en_TT", (short)11273);
        TrueTypeFont.addLCIDMapEntry(map, "es_AR", (short)11274);
        TrueTypeFont.addLCIDMapEntry(map, "ar_LB", (short)12289);
        TrueTypeFont.addLCIDMapEntry(map, "en_ZW", (short)12297);
        TrueTypeFont.addLCIDMapEntry(map, "es_EC", (short)12298);
        TrueTypeFont.addLCIDMapEntry(map, "ar_KW", (short)13313);
        TrueTypeFont.addLCIDMapEntry(map, "en_PH", (short)13321);
        TrueTypeFont.addLCIDMapEntry(map, "es_CL", (short)13322);
        TrueTypeFont.addLCIDMapEntry(map, "ar_AE", (short)14337);
        TrueTypeFont.addLCIDMapEntry(map, "es_UY", (short)14346);
        TrueTypeFont.addLCIDMapEntry(map, "ar_BH", (short)15361);
        TrueTypeFont.addLCIDMapEntry(map, "es_PY", (short)15370);
        TrueTypeFont.addLCIDMapEntry(map, "ar_QA", (short)16385);
        TrueTypeFont.addLCIDMapEntry(map, "es_BO", (short)16394);
        TrueTypeFont.addLCIDMapEntry(map, "es_SV", (short)17418);
        TrueTypeFont.addLCIDMapEntry(map, "es_HN", (short)18442);
        TrueTypeFont.addLCIDMapEntry(map, "es_NI", (short)19466);
        TrueTypeFont.addLCIDMapEntry(map, "es_PR", (short)20490);
        lcidMap = map;
    }

    private static short getLCIDFromLocale(Locale locale) {
        if (locale.equals(Locale.US)) {
            return 1033;
        }
        if (lcidMap == null) {
            TrueTypeFont.createLCIDMap();
        }
        String key = locale.toString();
        while (!"".equals(key)) {
            Short lcidObject = lcidMap.get(key);
            if (lcidObject != null) {
                return lcidObject;
            }
            int pos = key.lastIndexOf(95);
            if (pos < 1) {
                return 1033;
            }
            key = key.substring(0, pos);
        }
        return 1033;
    }

    @Override
    public String getFamilyName(Locale locale) {
        if (locale == null) {
            return this.familyName;
        }
        if (locale.equals(this.nameLocale) && this.localeFamilyName != null) {
            return this.localeFamilyName;
        }
        short localeID = TrueTypeFont.getLCIDFromLocale(locale);
        String name = this.lookupName(localeID, 1);
        if (name == null) {
            return this.familyName;
        }
        return name;
    }

    @Override
    public CharToGlyphMapper getMapper() {
        if (this.mapper == null) {
            this.mapper = new TrueTypeGlyphMapper(this);
        }
        return this.mapper;
    }

    protected void initAllNames(int requestedID, HashSet<String> names) {
        byte[] name = new byte[256];
        ByteBuffer buffer = this.getTableBuffer(1851878757);
        if (buffer != null) {
            ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.get();
            int numRecords = sbuffer.get();
            int stringPtr = sbuffer.get() & 0xFFFF;
            for (int i = 0; i < numRecords; ++i) {
                short platformID = sbuffer.get();
                if (platformID != 3) {
                    sbuffer.position(sbuffer.position() + 5);
                    continue;
                }
                short encodingID = sbuffer.get();
                sbuffer.get();
                short nameID = sbuffer.get();
                int nameLen = sbuffer.get() & 0xFFFF;
                int namePtr = (sbuffer.get() & 0xFFFF) + stringPtr;
                if (nameID != requestedID) continue;
                buffer.position(namePtr);
                buffer.get(name, 0, nameLen);
                names.add(this.makeString(name, nameLen, platformID, encodingID));
            }
        }
    }

    String[] getAllFamilyNames() {
        HashSet<String> aSet = new HashSet<String>();
        try {
            this.initAllNames(1, aSet);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return aSet.toArray(new String[0]);
    }

    String[] getAllFullNames() {
        HashSet<String> aSet = new HashSet<String>();
        try {
            this.initAllNames(4, aSet);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return aSet.toArray(new String[0]);
    }

    @Override
    Point2D.Float getGlyphPoint(long pScalerContext, int glyphCode, int ptNumber) {
        try {
            return this.getScaler().getGlyphPoint(pScalerContext, glyphCode, ptNumber);
        }
        catch (FontScalerException fe) {
            return null;
        }
    }

    private char[] getGaspTable() {
        if (this.gaspTable != null) {
            return this.gaspTable;
        }
        ByteBuffer buffer = this.getTableBuffer(1734439792);
        if (buffer == null) {
            this.gaspTable = new char[0];
            return this.gaspTable;
        }
        CharBuffer cbuffer = buffer.asCharBuffer();
        char format = cbuffer.get();
        if (format > '\u0001') {
            this.gaspTable = new char[0];
            return this.gaspTable;
        }
        char numRanges = cbuffer.get();
        if (4 + numRanges * 4 > this.getTableSize(1734439792)) {
            this.gaspTable = new char[0];
            return this.gaspTable;
        }
        this.gaspTable = new char[2 * numRanges];
        cbuffer.get(this.gaspTable);
        return this.gaspTable;
    }

    @Override
    public boolean useAAForPtSize(int ptsize) {
        char[] gasp = this.getGaspTable();
        if (gasp.length > 0) {
            for (int i = 0; i < gasp.length; i += 2) {
                if (ptsize > gasp[i]) continue;
                return (gasp[i + 1] & 2) != 0;
            }
            return true;
        }
        if (this.style == 1) {
            return true;
        }
        return ptsize <= 8 || ptsize >= 18;
    }

    @Override
    public boolean hasSupplementaryChars() {
        return ((TrueTypeGlyphMapper)this.getMapper()).hasSupplementaryChars();
    }

    public String toString() {
        return "** TrueType Font: Family=" + this.familyName + " Name=" + this.fullName + " style=" + this.style + " fileName=" + this.getPublicFileName();
    }

    private boolean isLanguageCompatible(short lcid) {
        for (short s : this.languageCompatibleLCIDs) {
            if (s != lcid) continue;
            return true;
        }
        return false;
    }

    private static short[] getLanguageCompatibleLCIDsFromLocale(Locale locale) {
        String language;
        short[] result;
        if (lcidLanguageCompatibilityMap == null) {
            TrueTypeFont.createLCIDMap();
            TrueTypeFont.createLCIDLanguageCompatibilityMap();
        }
        return (result = lcidLanguageCompatibilityMap.get(language = locale.getLanguage())) == null ? EMPTY_COMPATIBLE_LCIDS : result;
    }

    private static void createLCIDLanguageCompatibilityMap() {
        HashMap<String, short[]> map = new HashMap<String, short[]>();
        short[] sarr = new short[]{1031, 3079, 5127, 2055, 4103};
        map.put("de", sarr);
        sarr = new short[]{1044, 2068};
        map.put("no", sarr);
        sarr = new short[]{1049, 2073};
        map.put("ru", sarr);
        sarr = new short[]{1053, 2077};
        map.put("sv", sarr);
        sarr = new short[]{1046, 2070};
        map.put("pt", sarr);
        sarr = new short[]{1131, 3179, 2155};
        map.put("qu", sarr);
        sarr = new short[]{1086, 2110};
        map.put("ms", sarr);
        sarr = new short[]{11273, 3081, 12297, 8201, 10249, 4105, 13321, 6153, 7177, 5129, 2057};
        map.put("en", sarr);
        sarr = new short[]{1050, 4122};
        map.put("hr", sarr);
        sarr = new short[]{1040, 2064};
        map.put("it", sarr);
        sarr = new short[]{1036, 5132, 6156, 2060, 3084, 4108};
        map.put("fr", sarr);
        sarr = new short[]{1034, 12298, 14346, 2058, 8202, 19466, 17418, 9226, 13322, 5130, 7178, 11274, 16394, 4106, 10250, 6154, 18442, 20490, 15370};
        map.put("es", sarr);
        sarr = new short[]{1028, 3076, 5124, 4100, 2052};
        map.put("zh", sarr);
        sarr = new short[]{1025, 8193, 16385, 9217, 2049, 14337, 15361, 11265, 13313, 10241, 7169, 12289, 4097, 5121, 6145, 3073};
        map.put("ar", sarr);
        sarr = new short[]{1083, 3131, 2107};
        map.put("se", sarr);
        sarr = new short[]{1048, 2072};
        map.put("ro", sarr);
        sarr = new short[]{1043, 2067};
        map.put("nl", sarr);
        sarr = new short[]{7194, 3098};
        map.put("sr", sarr);
        lcidLanguageCompatibilityMap = map;
    }

    static {
        encoding_mapping = new String[]{"cp1252", "cp1250", "cp1251", "cp1253", "cp1254", "cp1255", "cp1256", "cp1257", "", "", "", "", "", "", "", "", "ms874", "ms932", "gbk", "ms949", "ms950", "ms1361", "", "", "", "", "", "", "", "", "", ""};
        languages = new String[][]{{"en", "ca", "da", "de", "es", "fi", "fr", "is", "it", "nl", "no", "pt", "sq", "sv"}, {"cs", "cz", "et", "hr", "hu", "nr", "pl", "ro", "sk", "sl", "sq", "sr"}, {"bg", "mk", "ru", "sh", "uk"}, {"el"}, {"tr"}, {"he"}, {"ar"}, {"et", "lt", "lv"}, {"th"}, {"ja"}, {"zh", "zh_CN"}, {"ko"}, {"zh_HK", "zh_TW"}, {"ko"}};
        codePages = new String[]{"cp1252", "cp1250", "cp1251", "cp1253", "cp1254", "cp1255", "cp1256", "cp1257", "ms874", "ms932", "gbk", "ms949", "ms950", "ms1361"};
        defaultCodePage = null;
        EMPTY_COMPATIBLE_LCIDS = new short[0];
    }

    private static class TTDisposerRecord
    implements DisposerRecord {
        FileChannel channel = null;

        private TTDisposerRecord() {
        }

        @Override
        public synchronized void dispose() {
            try {
                if (this.channel != null) {
                    this.channel.close();
                }
            }
            catch (IOException iOException) {
            }
            finally {
                this.channel = null;
            }
        }
    }

    static class DirectoryEntry {
        int tag;
        int offset;
        int length;

        DirectoryEntry() {
        }
    }
}

