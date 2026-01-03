/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import sun.font.CharToGlyphMapper;
import sun.font.CompositeGlyphMapper;
import sun.font.CompositeStrike;
import sun.font.Font2D;
import sun.font.Font2DHandle;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontStrike;
import sun.font.FontStrikeDesc;
import sun.font.PhysicalFont;
import sun.font.SunFontManager;

public final class CompositeFont
extends Font2D {
    private boolean[] deferredInitialisation;
    String[] componentFileNames;
    String[] componentNames;
    private PhysicalFont[] components;
    int numSlots;
    int numMetricsSlots;
    int[] exclusionRanges;
    int[] maxIndices;
    int numGlyphs = 0;
    int localeSlot = -1;
    boolean isStdComposite = true;

    public CompositeFont(String name, String[] compFileNames, String[] compNames, int metricsSlotCnt, int[] exclRanges, int[] maxIndexes, boolean defer, SunFontManager fm) {
        this.handle = new Font2DHandle(this);
        this.fullName = name;
        this.componentFileNames = compFileNames;
        this.componentNames = compNames;
        this.numSlots = compNames == null ? this.componentFileNames.length : this.componentNames.length;
        this.numSlots = this.numSlots <= 254 ? this.numSlots : 254;
        this.numMetricsSlots = metricsSlotCnt;
        this.exclusionRanges = exclRanges;
        this.maxIndices = maxIndexes;
        if (fm.getEUDCFont() != null) {
            int msCnt = this.numMetricsSlots;
            int fbCnt = this.numSlots - msCnt;
            ++this.numSlots;
            if (this.componentNames != null) {
                this.componentNames = new String[this.numSlots];
                System.arraycopy(compNames, 0, this.componentNames, 0, msCnt);
                this.componentNames[msCnt] = fm.getEUDCFont().getFontName(null);
                System.arraycopy(compNames, msCnt, this.componentNames, msCnt + 1, fbCnt);
            }
            if (this.componentFileNames != null) {
                this.componentFileNames = new String[this.numSlots];
                System.arraycopy(compFileNames, 0, this.componentFileNames, 0, msCnt);
                System.arraycopy(compFileNames, msCnt, this.componentFileNames, msCnt + 1, fbCnt);
            }
            this.components = new PhysicalFont[this.numSlots];
            this.components[msCnt] = fm.getEUDCFont();
            this.deferredInitialisation = new boolean[this.numSlots];
            if (defer) {
                for (int i = 0; i < this.numSlots - 1; ++i) {
                    this.deferredInitialisation[i] = true;
                }
            }
        } else {
            this.components = new PhysicalFont[this.numSlots];
            this.deferredInitialisation = new boolean[this.numSlots];
            if (defer) {
                for (int i = 0; i < this.numSlots; ++i) {
                    this.deferredInitialisation[i] = true;
                }
            }
        }
        this.fontRank = 2;
        int index = this.fullName.indexOf(46);
        if (index > 0) {
            this.familyName = this.fullName.substring(0, index);
            if (index + 1 < this.fullName.length()) {
                String styleStr = this.fullName.substring(index + 1);
                if ("plain".equals(styleStr)) {
                    this.style = 0;
                } else if ("bold".equals(styleStr)) {
                    this.style = 1;
                } else if ("italic".equals(styleStr)) {
                    this.style = 2;
                } else if ("bolditalic".equals(styleStr)) {
                    this.style = 3;
                }
            }
        } else {
            this.familyName = this.fullName;
        }
    }

    CompositeFont(PhysicalFont[] slotFonts) {
        this.isStdComposite = false;
        this.handle = new Font2DHandle(this);
        this.fullName = slotFonts[0].fullName;
        this.familyName = slotFonts[0].familyName;
        this.style = slotFonts[0].style;
        this.numMetricsSlots = 1;
        this.numSlots = slotFonts.length;
        this.components = new PhysicalFont[this.numSlots];
        System.arraycopy(slotFonts, 0, this.components, 0, this.numSlots);
        this.deferredInitialisation = new boolean[this.numSlots];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    CompositeFont(PhysicalFont physFont, CompositeFont compFont) {
        this.isStdComposite = false;
        this.handle = new Font2DHandle(this);
        this.fullName = physFont.fullName;
        this.familyName = physFont.familyName;
        this.style = physFont.style;
        this.numMetricsSlots = 1;
        this.numSlots = compFont.numSlots + 1;
        FontManager fontManager = FontManagerFactory.getInstance();
        synchronized (fontManager) {
            this.components = new PhysicalFont[this.numSlots];
            this.components[0] = physFont;
            System.arraycopy(compFont.components, 0, this.components, 1, compFont.numSlots);
            if (compFont.componentNames != null) {
                this.componentNames = new String[this.numSlots];
                this.componentNames[0] = physFont.fullName;
                System.arraycopy(compFont.componentNames, 0, this.componentNames, 1, compFont.numSlots);
            }
            if (compFont.componentFileNames != null) {
                this.componentFileNames = new String[this.numSlots];
                this.componentFileNames[0] = null;
                System.arraycopy(compFont.componentFileNames, 0, this.componentFileNames, 1, compFont.numSlots);
            }
            this.deferredInitialisation = new boolean[this.numSlots];
            this.deferredInitialisation[0] = false;
            System.arraycopy(compFont.deferredInitialisation, 0, this.deferredInitialisation, 1, compFont.numSlots);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doDeferredInitialisation(int slot) {
        SunFontManager fm;
        if (!this.deferredInitialisation[slot]) {
            return;
        }
        SunFontManager sunFontManager = fm = SunFontManager.getInstance();
        synchronized (sunFontManager) {
            if (this.componentNames == null) {
                this.componentNames = new String[this.numSlots];
            }
            if (this.components[slot] == null) {
                if (this.componentFileNames != null && this.componentFileNames[slot] != null) {
                    this.components[slot] = fm.initialiseDeferredFont(this.componentFileNames[slot]);
                }
                if (this.components[slot] == null) {
                    this.components[slot] = fm.getDefaultPhysicalFont();
                }
                String name = this.components[slot].getFontName(null);
                if (this.componentNames[slot] == null) {
                    this.componentNames[slot] = name;
                } else if (!this.componentNames[slot].equalsIgnoreCase(name)) {
                    try {
                        this.components[slot] = (PhysicalFont)fm.findFont2D(this.componentNames[slot], this.style, 1);
                    }
                    catch (ClassCastException cce) {
                        this.components[slot] = fm.getDefaultPhysicalFont();
                    }
                }
            }
            this.deferredInitialisation[slot] = false;
        }
    }

    void replaceComponentFont(PhysicalFont oldFont, PhysicalFont newFont) {
        if (this.components == null) {
            return;
        }
        for (int slot = 0; slot < this.numSlots; ++slot) {
            if (this.components[slot] != oldFont) continue;
            this.components[slot] = newFont;
            if (this.componentNames == null) continue;
            this.componentNames[slot] = newFont.getFontName(null);
        }
    }

    public boolean isExcludedChar(int slot, int charcode) {
        if (this.exclusionRanges == null || this.maxIndices == null || slot >= this.numMetricsSlots) {
            return false;
        }
        int minIndex = 0;
        int maxIndex = this.maxIndices[slot];
        if (slot > 0) {
            minIndex = this.maxIndices[slot - 1];
        }
        for (int curIndex = minIndex; maxIndex > curIndex; curIndex += 2) {
            if (charcode < this.exclusionRanges[curIndex] || charcode > this.exclusionRanges[curIndex + 1]) continue;
            return true;
        }
        return false;
    }

    @Override
    public void getStyleMetrics(float pointSize, float[] metrics, int offset) {
        PhysicalFont font = this.getSlotFont(0);
        if (font == null) {
            super.getStyleMetrics(pointSize, metrics, offset);
        } else {
            font.getStyleMetrics(pointSize, metrics, offset);
        }
    }

    public int getNumSlots() {
        return this.numSlots;
    }

    public PhysicalFont getSlotFont(int slot) {
        if (this.deferredInitialisation[slot]) {
            this.doDeferredInitialisation(slot);
        }
        SunFontManager fm = SunFontManager.getInstance();
        try {
            PhysicalFont font = this.components[slot];
            if (font == null) {
                try {
                    this.components[slot] = font = (PhysicalFont)fm.findFont2D(this.componentNames[slot], this.style, 1);
                }
                catch (ClassCastException cce) {
                    font = fm.getDefaultPhysicalFont();
                }
            }
            return font;
        }
        catch (Exception e) {
            return fm.getDefaultPhysicalFont();
        }
    }

    @Override
    FontStrike createStrike(FontStrikeDesc desc) {
        return new CompositeStrike(this, desc);
    }

    public boolean isStdComposite() {
        return this.isStdComposite;
    }

    @Override
    protected int getValidatedGlyphCode(int glyphCode) {
        int slot = glyphCode >>> 24;
        if (slot >= this.numSlots) {
            return this.getMapper().getMissingGlyphCode();
        }
        int slotglyphCode = glyphCode & 0xFFFFFF;
        PhysicalFont slotFont = this.getSlotFont(slot);
        if (slotFont.getValidatedGlyphCode(slotglyphCode) == slotFont.getMissingGlyphCode()) {
            return this.getMapper().getMissingGlyphCode();
        }
        return glyphCode;
    }

    @Override
    public CharToGlyphMapper getMapper() {
        if (this.mapper == null) {
            this.mapper = new CompositeGlyphMapper(this);
        }
        return this.mapper;
    }

    @Override
    public boolean hasSupplementaryChars() {
        for (int i = 0; i < this.numSlots; ++i) {
            if (!this.getSlotFont(i).hasSupplementaryChars()) continue;
            return true;
        }
        return false;
    }

    @Override
    public int getNumGlyphs() {
        if (this.numGlyphs == 0) {
            this.numGlyphs = this.getMapper().getNumGlyphs();
        }
        return this.numGlyphs;
    }

    @Override
    public int getMissingGlyphCode() {
        return this.getMapper().getMissingGlyphCode();
    }

    @Override
    public boolean canDisplay(char c) {
        return this.getMapper().canDisplay(c);
    }

    @Override
    public boolean useAAForPtSize(int ptsize) {
        if (this.localeSlot == -1) {
            int numCoreSlots = this.numMetricsSlots;
            if (numCoreSlots == 1 && !this.isStdComposite()) {
                numCoreSlots = this.numSlots;
            }
            for (int slot = 0; slot < numCoreSlots; ++slot) {
                if (!this.getSlotFont(slot).supportsEncoding(null)) continue;
                this.localeSlot = slot;
                break;
            }
            if (this.localeSlot == -1) {
                this.localeSlot = 0;
            }
        }
        return this.getSlotFont(this.localeSlot).useAAForPtSize(ptsize);
    }

    public String toString() {
        String ls = System.lineSeparator();
        Object componentsStr = "";
        for (int i = 0; i < this.numSlots; ++i) {
            componentsStr = (String)componentsStr + "    Slot[" + i + "]=" + String.valueOf(this.getSlotFont(i)) + ls;
        }
        return "** Composite Font: Family=" + this.familyName + " Name=" + this.fullName + " style=" + this.style + ls + (String)componentsStr;
    }
}

