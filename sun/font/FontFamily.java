/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import sun.font.CompositeFont;
import sun.font.FileFont;
import sun.font.Font2D;
import sun.font.FontUtilities;

public class FontFamily {
    private static ConcurrentHashMap<String, FontFamily> familyNameMap = new ConcurrentHashMap();
    private static HashMap<String, FontFamily> allLocaleNames;
    protected String familyName;
    protected Font2D plain;
    protected Font2D bold;
    protected Font2D italic;
    protected Font2D bolditalic;
    protected boolean logicalFont = false;
    protected int familyRank;
    private int familyWidth = 0;

    public static FontFamily getFamily(String name) {
        return familyNameMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static String[] getAllFamilyNames() {
        return null;
    }

    static void remove(Font2D font2D) {
        String name = font2D.getFamilyName(Locale.ENGLISH);
        FontFamily family = FontFamily.getFamily(name);
        if (family == null) {
            return;
        }
        if (family.plain == font2D) {
            family.plain = null;
        }
        if (family.bold == font2D) {
            family.bold = null;
        }
        if (family.italic == font2D) {
            family.italic = null;
        }
        if (family.bolditalic == font2D) {
            family.bolditalic = null;
        }
        if (family.plain == null && family.bold == null && family.italic == null && family.bolditalic == null) {
            familyNameMap.remove(name);
        }
    }

    public FontFamily(String name, boolean isLogFont, int rank) {
        this.logicalFont = isLogFont;
        this.familyName = name;
        this.familyRank = rank;
        familyNameMap.put(name.toLowerCase(Locale.ENGLISH), this);
    }

    FontFamily(String name) {
        this.logicalFont = false;
        this.familyName = name;
        this.familyRank = 4;
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public int getRank() {
        return this.familyRank;
    }

    private boolean isFromSameSource(Font2D font) {
        if (!(font instanceof FileFont)) {
            return false;
        }
        FileFont existingFont = null;
        if (this.plain instanceof FileFont) {
            existingFont = (FileFont)this.plain;
        } else if (this.bold instanceof FileFont) {
            existingFont = (FileFont)this.bold;
        } else if (this.italic instanceof FileFont) {
            existingFont = (FileFont)this.italic;
        } else if (this.bolditalic instanceof FileFont) {
            existingFont = (FileFont)this.bolditalic;
        }
        if (existingFont == null) {
            return false;
        }
        File existDir = new File(existingFont.platName).getParentFile();
        FileFont newFont = (FileFont)font;
        File newDir = new File(newFont.platName).getParentFile();
        if (existDir != null) {
            try {
                existDir = existDir.getCanonicalFile();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        if (newDir != null) {
            try {
                newDir = newDir.getCanonicalFile();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return Objects.equals(newDir, existDir);
    }

    private boolean preferredWidth(Font2D font) {
        int newWidth = font.getWidth();
        if (this.familyWidth == 0) {
            this.familyWidth = newWidth;
            return true;
        }
        if (newWidth == this.familyWidth) {
            return true;
        }
        if (Math.abs(5 - newWidth) < Math.abs(5 - this.familyWidth)) {
            if (FontUtilities.debugFonts()) {
                FontUtilities.logInfo("Found more preferred width. New width = " + newWidth + " Old width = " + this.familyWidth + " in font " + String.valueOf(font) + " nulling out fonts plain: " + String.valueOf(this.plain) + " bold: " + String.valueOf(this.bold) + " italic: " + String.valueOf(this.italic) + " bolditalic: " + String.valueOf(this.bolditalic));
            }
            this.familyWidth = newWidth;
            this.bolditalic = null;
            this.italic = null;
            this.bold = null;
            this.plain = null;
            return true;
        }
        if (FontUtilities.debugFonts()) {
            FontUtilities.logInfo("Family rejecting font " + String.valueOf(font) + " of less preferred width " + newWidth);
        }
        return false;
    }

    private boolean closerWeight(Font2D currFont, Font2D font, int style) {
        if (this.familyWidth != font.getWidth()) {
            return false;
        }
        if (currFont == null) {
            return true;
        }
        if (FontUtilities.debugFonts()) {
            FontUtilities.logInfo("New weight for style " + style + ". Curr.font=" + String.valueOf(currFont) + " New font=" + String.valueOf(font) + " Curr.weight=" + currFont.getWeight() + " New weight=" + font.getWeight());
        }
        int newWeight = font.getWeight();
        switch (style) {
            case 0: 
            case 2: {
                return newWeight <= 400 && newWeight > currFont.getWeight();
            }
            case 1: 
            case 3: {
                return Math.abs(newWeight - 700) < Math.abs(currFont.getWeight() - 700);
            }
        }
        return false;
    }

    public void setFont(Font2D font, int style) {
        if (FontUtilities.isLogging()) {
            String msg = font instanceof CompositeFont ? "Request to add " + font.getFamilyName(null) + " with style " + style + " to family " + this.familyName : "Request to add " + String.valueOf(font) + " with style " + style + " to family " + String.valueOf(this);
            FontUtilities.logInfo(msg);
        }
        if (font.getRank() > this.familyRank && !this.isFromSameSource(font)) {
            if (FontUtilities.isLogging()) {
                FontUtilities.logWarning("Rejecting adding " + String.valueOf(font) + " of lower rank " + font.getRank() + " to family " + String.valueOf(this) + " of rank " + this.familyRank);
            }
            return;
        }
        switch (style) {
            case 0: {
                if (!this.preferredWidth(font) || !this.closerWeight(this.plain, font, style)) break;
                this.plain = font;
                break;
            }
            case 1: {
                if (!this.preferredWidth(font) || !this.closerWeight(this.bold, font, style)) break;
                this.bold = font;
                break;
            }
            case 2: {
                if (!this.preferredWidth(font) || !this.closerWeight(this.italic, font, style)) break;
                this.italic = font;
                break;
            }
            case 3: {
                if (!this.preferredWidth(font) || !this.closerWeight(this.bolditalic, font, style)) break;
                this.bolditalic = font;
                break;
            }
        }
    }

    public Font2D getFontWithExactStyleMatch(int style) {
        switch (style) {
            case 0: {
                return this.plain;
            }
            case 1: {
                return this.bold;
            }
            case 2: {
                return this.italic;
            }
            case 3: {
                return this.bolditalic;
            }
        }
        return null;
    }

    public Font2D getFont(int style) {
        switch (style) {
            case 0: {
                return this.plain;
            }
            case 1: {
                if (this.bold != null) {
                    return this.bold;
                }
                if (this.plain != null && this.plain.canDoStyle(style)) {
                    return this.plain;
                }
                return null;
            }
            case 2: {
                if (this.italic != null) {
                    return this.italic;
                }
                if (this.plain != null && this.plain.canDoStyle(style)) {
                    return this.plain;
                }
                return null;
            }
            case 3: {
                if (this.bolditalic != null) {
                    return this.bolditalic;
                }
                if (this.bold != null && this.bold.canDoStyle(style)) {
                    return this.bold;
                }
                if (this.italic != null && this.italic.canDoStyle(style)) {
                    return this.italic;
                }
                if (this.plain != null && this.plain.canDoStyle(style)) {
                    return this.plain;
                }
                return null;
            }
        }
        return null;
    }

    Font2D getClosestStyle(int style) {
        switch (style) {
            case 0: {
                if (this.bold != null) {
                    return this.bold;
                }
                if (this.italic != null) {
                    return this.italic;
                }
                return this.bolditalic;
            }
            case 1: {
                if (this.plain != null) {
                    return this.plain;
                }
                if (this.bolditalic != null) {
                    return this.bolditalic;
                }
                return this.italic;
            }
            case 2: {
                if (this.bolditalic != null) {
                    return this.bolditalic;
                }
                if (this.plain != null) {
                    return this.plain;
                }
                return this.bold;
            }
            case 3: {
                if (this.italic != null) {
                    return this.italic;
                }
                if (this.bold != null) {
                    return this.bold;
                }
                return this.plain;
            }
        }
        return null;
    }

    static synchronized void addLocaleNames(FontFamily family, String[] names) {
        if (allLocaleNames == null) {
            allLocaleNames = new HashMap();
        }
        for (int i = 0; i < names.length; ++i) {
            allLocaleNames.put(names[i].toLowerCase(), family);
        }
    }

    public static synchronized FontFamily getLocaleFamily(String name) {
        if (allLocaleNames == null) {
            return null;
        }
        return allLocaleNames.get(name.toLowerCase());
    }

    public static FontFamily[] getAllFontFamilies() {
        Collection<FontFamily> families = familyNameMap.values();
        return families.toArray(new FontFamily[0]);
    }

    public String toString() {
        return "Font family: " + this.familyName + " plain=" + String.valueOf(this.plain) + " bold=" + String.valueOf(this.bold) + " italic=" + String.valueOf(this.italic) + " bolditalic=" + String.valueOf(this.bolditalic);
    }
}

