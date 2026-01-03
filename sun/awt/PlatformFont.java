/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.peer.FontPeer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Locale;
import sun.awt.CharsetString;
import sun.awt.FontConfiguration;
import sun.awt.FontDescriptor;
import sun.awt.NativeLibLoader;
import sun.font.SunFontManager;
import sun.java2d.FontSupport;

public abstract class PlatformFont
implements FontPeer {
    protected FontDescriptor[] componentFonts;
    protected char defaultChar;
    protected FontConfiguration fontConfig;
    protected FontDescriptor defaultFont;
    protected String familyName;
    private Object[] fontCache;
    protected static int FONTCACHESIZE;
    protected static int FONTCACHEMASK;
    protected static String osVersion;

    public PlatformFont(String name, int style) {
        SunFontManager sfm = SunFontManager.getInstance();
        if (sfm instanceof FontSupport) {
            this.fontConfig = sfm.getFontConfiguration();
        }
        if (this.fontConfig == null) {
            return;
        }
        this.familyName = name.toLowerCase(Locale.ENGLISH);
        if (!FontConfiguration.isLogicalFontFamilyName(this.familyName)) {
            this.familyName = this.fontConfig.getFallbackFamilyName(this.familyName, "sansserif");
        }
        this.componentFonts = this.fontConfig.getFontDescriptors(this.familyName, style);
        char missingGlyphCharacter = this.getMissingGlyphCharacter();
        this.defaultChar = (char)63;
        if (this.componentFonts.length > 0) {
            this.defaultFont = this.componentFonts[0];
        }
        for (int i = 0; i < this.componentFonts.length; ++i) {
            if (this.componentFonts[i].isExcluded(missingGlyphCharacter) || !this.componentFonts[i].encoder.canEncode(missingGlyphCharacter)) continue;
            this.defaultFont = this.componentFonts[i];
            this.defaultChar = missingGlyphCharacter;
            break;
        }
    }

    protected abstract char getMissingGlyphCharacter();

    public CharsetString[] makeMultiCharsetString(String str) {
        return this.makeMultiCharsetString(str.toCharArray(), 0, str.length(), true);
    }

    public CharsetString[] makeMultiCharsetString(String str, boolean allowdefault) {
        return this.makeMultiCharsetString(str.toCharArray(), 0, str.length(), allowdefault);
    }

    public CharsetString[] makeMultiCharsetString(char[] str, int offset, int len) {
        return this.makeMultiCharsetString(str, offset, len, true);
    }

    public CharsetString[] makeMultiCharsetString(char[] str, int offset, int len, boolean allowDefault) {
        CharsetString[] result;
        if (len < 1) {
            return new CharsetString[0];
        }
        ArrayList<CharsetString> mcs = null;
        char[] tmpStr = new char[len];
        char tmpChar = this.defaultChar;
        boolean encoded = false;
        FontDescriptor currentFont = this.defaultFont;
        for (int i = 0; i < this.componentFonts.length; ++i) {
            if (this.componentFonts[i].isExcluded(str[offset]) || !this.componentFonts[i].encoder.canEncode(str[offset])) continue;
            currentFont = this.componentFonts[i];
            tmpChar = str[offset];
            encoded = true;
            break;
        }
        if (!allowDefault && !encoded) {
            return null;
        }
        tmpStr[0] = tmpChar;
        int lastIndex = 0;
        for (int i = 1; i < len; ++i) {
            char ch = str[offset + i];
            FontDescriptor fd = this.defaultFont;
            tmpChar = this.defaultChar;
            encoded = false;
            for (int j = 0; j < this.componentFonts.length; ++j) {
                if (this.componentFonts[j].isExcluded(ch) || !this.componentFonts[j].encoder.canEncode(ch)) continue;
                fd = this.componentFonts[j];
                tmpChar = ch;
                encoded = true;
                break;
            }
            if (!allowDefault && !encoded) {
                return null;
            }
            tmpStr[i] = tmpChar;
            if (currentFont == fd) continue;
            if (mcs == null) {
                mcs = new ArrayList<CharsetString>(3);
            }
            mcs.add(new CharsetString(tmpStr, lastIndex, i - lastIndex, currentFont));
            currentFont = fd;
            fd = this.defaultFont;
            lastIndex = i;
        }
        CharsetString cs = new CharsetString(tmpStr, lastIndex, len - lastIndex, currentFont);
        if (mcs == null) {
            result = new CharsetString[]{cs};
        } else {
            mcs.add(cs);
            result = mcs.toArray(new CharsetString[mcs.size()]);
        }
        return result;
    }

    public boolean mightHaveMultiFontMetrics() {
        return this.fontConfig != null;
    }

    public Object[] makeConvertedMultiFontString(String str) {
        return this.makeConvertedMultiFontChars(str.toCharArray(), 0, str.length());
    }

    public Object[] makeConvertedMultiFontChars(char[] data, int start, int len) {
        int stringIndex;
        Object[] result = new Object[2];
        byte[] convertedData = null;
        int convertedDataIndex = 0;
        int resultIndex = 0;
        FontDescriptor currentFontDescriptor = null;
        FontDescriptor lastFontDescriptor = null;
        int end = start + len;
        if (start < 0 || end > data.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (stringIndex >= end) {
            return null;
        }
        for (stringIndex = start; stringIndex < end; ++stringIndex) {
            char currentDefaultChar = data[stringIndex];
            int cacheIndex = currentDefaultChar & FONTCACHEMASK;
            PlatformFontCache theChar = (PlatformFontCache)this.getFontCache()[cacheIndex];
            if (theChar == null || theChar.uniChar != currentDefaultChar) {
                currentFontDescriptor = this.defaultFont;
                currentDefaultChar = this.defaultChar;
                char ch = data[stringIndex];
                for (FontDescriptor fontDescriptor : this.componentFonts) {
                    fontDescriptor.encoder.reset();
                    if (fontDescriptor.isExcluded(ch) || !fontDescriptor.encoder.canEncode(ch)) continue;
                    currentFontDescriptor = fontDescriptor;
                    currentDefaultChar = ch;
                    break;
                }
                try {
                    char[] input = new char[]{currentDefaultChar};
                    theChar = new PlatformFontCache();
                    if (currentFontDescriptor.useUnicode()) {
                        if (FontDescriptor.isLE) {
                            theChar.bb.put((byte)(input[0] & 0xFF));
                            theChar.bb.put((byte)(input[0] >> 8));
                        } else {
                            theChar.bb.put((byte)(input[0] >> 8));
                            theChar.bb.put((byte)(input[0] & 0xFF));
                        }
                    } else {
                        currentFontDescriptor.encoder.encode(CharBuffer.wrap(input), theChar.bb, true);
                    }
                    theChar.fontDescriptor = currentFontDescriptor;
                    theChar.uniChar = data[stringIndex];
                    this.getFontCache()[cacheIndex] = theChar;
                }
                catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                    return null;
                }
            }
            if (lastFontDescriptor != theChar.fontDescriptor) {
                if (lastFontDescriptor != null) {
                    result[resultIndex++] = lastFontDescriptor;
                    result[resultIndex++] = convertedData;
                    if (convertedData != null) {
                        convertedData[0] = (byte)((convertedDataIndex -= 4) >> 24);
                        convertedData[1] = (byte)(convertedDataIndex >> 16);
                        convertedData[2] = (byte)(convertedDataIndex >> 8);
                        convertedData[3] = (byte)convertedDataIndex;
                    }
                    if (resultIndex >= result.length) {
                        Object[] newResult = new Object[result.length * 2];
                        System.arraycopy(result, 0, newResult, 0, result.length);
                        result = newResult;
                    }
                }
                convertedData = theChar.fontDescriptor.useUnicode() ? new byte[(end - stringIndex + 1) * (int)theChar.fontDescriptor.unicodeEncoder.maxBytesPerChar() + 4] : new byte[(end - stringIndex + 1) * (int)theChar.fontDescriptor.encoder.maxBytesPerChar() + 4];
                convertedDataIndex = 4;
                lastFontDescriptor = theChar.fontDescriptor;
            }
            byte[] ba = theChar.bb.array();
            int size = theChar.bb.position();
            if (size == 1) {
                convertedData[convertedDataIndex++] = ba[0];
                continue;
            }
            if (size == 2) {
                convertedData[convertedDataIndex++] = ba[0];
                convertedData[convertedDataIndex++] = ba[1];
                continue;
            }
            if (size == 3) {
                convertedData[convertedDataIndex++] = ba[0];
                convertedData[convertedDataIndex++] = ba[1];
                convertedData[convertedDataIndex++] = ba[2];
                continue;
            }
            if (size != 4) continue;
            convertedData[convertedDataIndex++] = ba[0];
            convertedData[convertedDataIndex++] = ba[1];
            convertedData[convertedDataIndex++] = ba[2];
            convertedData[convertedDataIndex++] = ba[3];
        }
        result[resultIndex++] = lastFontDescriptor;
        result[resultIndex] = convertedData;
        if (convertedData != null) {
            convertedData[0] = (byte)((convertedDataIndex -= 4) >> 24);
            convertedData[1] = (byte)(convertedDataIndex >> 16);
            convertedData[2] = (byte)(convertedDataIndex >> 8);
            convertedData[3] = (byte)convertedDataIndex;
        }
        return result;
    }

    protected final Object[] getFontCache() {
        if (this.fontCache == null) {
            this.fontCache = new Object[FONTCACHESIZE];
        }
        return this.fontCache;
    }

    private static native void initIDs();

    static {
        NativeLibLoader.loadLibraries();
        PlatformFont.initIDs();
        FONTCACHESIZE = 256;
        FONTCACHEMASK = FONTCACHESIZE - 1;
    }

    static class PlatformFontCache {
        char uniChar;
        FontDescriptor fontDescriptor;
        ByteBuffer bb = ByteBuffer.allocate(4);

        PlatformFontCache() {
        }
    }
}

