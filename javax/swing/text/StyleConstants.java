/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.TabSet;

public sealed class StyleConstants {
    public static final String ComponentElementName = "component";
    public static final String IconElementName = "icon";
    public static final Object NameAttribute = new StyleConstants("name");
    public static final Object ResolveAttribute = new StyleConstants("resolver");
    public static final Object ModelAttribute = new StyleConstants("model");
    public static final Object BidiLevel = new CharacterConstants("bidiLevel");
    public static final Object FontFamily;
    public static final Object Family;
    public static final Object FontSize;
    public static final Object Size;
    public static final Object Bold;
    public static final Object Italic;
    public static final Object Underline;
    public static final Object StrikeThrough;
    public static final Object Superscript;
    public static final Object Subscript;
    public static final Object Foreground;
    public static final Object Background;
    public static final Object ComponentAttribute;
    public static final Object IconAttribute;
    public static final Object ComposedTextAttribute;
    public static final Object FirstLineIndent;
    public static final Object LeftIndent;
    public static final Object RightIndent;
    public static final Object LineSpacing;
    public static final Object SpaceAbove;
    public static final Object SpaceBelow;
    public static final Object Alignment;
    public static final Object TabSet;
    public static final Object Orientation;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_JUSTIFIED = 3;
    static Object[] keys;
    private String representation;

    public String toString() {
        return this.representation;
    }

    public static int getBidiLevel(AttributeSet a) {
        Integer o = (Integer)a.getAttribute(BidiLevel);
        if (o != null) {
            return o;
        }
        return 0;
    }

    public static void setBidiLevel(MutableAttributeSet a, int o) {
        a.addAttribute(BidiLevel, o);
    }

    public static Component getComponent(AttributeSet a) {
        return (Component)a.getAttribute(ComponentAttribute);
    }

    public static void setComponent(MutableAttributeSet a, Component c) {
        a.addAttribute("$ename", ComponentElementName);
        a.addAttribute(ComponentAttribute, c);
    }

    public static Icon getIcon(AttributeSet a) {
        return (Icon)a.getAttribute(IconAttribute);
    }

    public static void setIcon(MutableAttributeSet a, Icon c) {
        a.addAttribute("$ename", IconElementName);
        a.addAttribute(IconAttribute, c);
    }

    public static String getFontFamily(AttributeSet a) {
        String family = (String)a.getAttribute(FontFamily);
        if (family == null) {
            family = "Monospaced";
        }
        return family;
    }

    public static void setFontFamily(MutableAttributeSet a, String fam) {
        a.addAttribute(FontFamily, fam);
    }

    public static int getFontSize(AttributeSet a) {
        Integer size = (Integer)a.getAttribute(FontSize);
        if (size != null) {
            return size;
        }
        return 12;
    }

    public static void setFontSize(MutableAttributeSet a, int s) {
        a.addAttribute(FontSize, s);
    }

    public static boolean isBold(AttributeSet a) {
        Boolean bold = (Boolean)a.getAttribute(Bold);
        if (bold != null) {
            return bold;
        }
        return false;
    }

    public static void setBold(MutableAttributeSet a, boolean b) {
        a.addAttribute(Bold, b);
    }

    public static boolean isItalic(AttributeSet a) {
        Boolean italic = (Boolean)a.getAttribute(Italic);
        if (italic != null) {
            return italic;
        }
        return false;
    }

    public static void setItalic(MutableAttributeSet a, boolean b) {
        a.addAttribute(Italic, b);
    }

    public static boolean isUnderline(AttributeSet a) {
        Boolean underline = (Boolean)a.getAttribute(Underline);
        if (underline != null) {
            return underline;
        }
        return false;
    }

    public static boolean isStrikeThrough(AttributeSet a) {
        Boolean strike = (Boolean)a.getAttribute(StrikeThrough);
        if (strike != null) {
            return strike;
        }
        return false;
    }

    public static boolean isSuperscript(AttributeSet a) {
        Boolean superscript = (Boolean)a.getAttribute(Superscript);
        if (superscript != null) {
            return superscript;
        }
        return false;
    }

    public static boolean isSubscript(AttributeSet a) {
        Boolean subscript = (Boolean)a.getAttribute(Subscript);
        if (subscript != null) {
            return subscript;
        }
        return false;
    }

    public static void setUnderline(MutableAttributeSet a, boolean b) {
        a.addAttribute(Underline, b);
    }

    public static void setStrikeThrough(MutableAttributeSet a, boolean b) {
        a.addAttribute(StrikeThrough, b);
    }

    public static void setSuperscript(MutableAttributeSet a, boolean b) {
        a.addAttribute(Superscript, b);
    }

    public static void setSubscript(MutableAttributeSet a, boolean b) {
        a.addAttribute(Subscript, b);
    }

    public static Color getForeground(AttributeSet a) {
        Color fg = (Color)a.getAttribute(Foreground);
        if (fg == null) {
            fg = Color.black;
        }
        return fg;
    }

    public static void setForeground(MutableAttributeSet a, Color fg) {
        a.addAttribute(Foreground, fg);
    }

    public static Color getBackground(AttributeSet a) {
        Color fg = (Color)a.getAttribute(Background);
        if (fg == null) {
            fg = Color.black;
        }
        return fg;
    }

    public static void setBackground(MutableAttributeSet a, Color fg) {
        a.addAttribute(Background, fg);
    }

    public static float getFirstLineIndent(AttributeSet a) {
        Float indent = (Float)a.getAttribute(FirstLineIndent);
        if (indent != null) {
            return indent.floatValue();
        }
        return 0.0f;
    }

    public static void setFirstLineIndent(MutableAttributeSet a, float i) {
        a.addAttribute(FirstLineIndent, Float.valueOf(i));
    }

    public static float getRightIndent(AttributeSet a) {
        Float indent = (Float)a.getAttribute(RightIndent);
        if (indent != null) {
            return indent.floatValue();
        }
        return 0.0f;
    }

    public static void setRightIndent(MutableAttributeSet a, float i) {
        a.addAttribute(RightIndent, Float.valueOf(i));
    }

    public static float getLeftIndent(AttributeSet a) {
        Float indent = (Float)a.getAttribute(LeftIndent);
        if (indent != null) {
            return indent.floatValue();
        }
        return 0.0f;
    }

    public static void setLeftIndent(MutableAttributeSet a, float i) {
        a.addAttribute(LeftIndent, Float.valueOf(i));
    }

    public static float getLineSpacing(AttributeSet a) {
        Float space = (Float)a.getAttribute(LineSpacing);
        if (space != null) {
            return space.floatValue();
        }
        return 0.0f;
    }

    public static void setLineSpacing(MutableAttributeSet a, float i) {
        a.addAttribute(LineSpacing, Float.valueOf(i));
    }

    public static float getSpaceAbove(AttributeSet a) {
        Float space = (Float)a.getAttribute(SpaceAbove);
        if (space != null) {
            return space.floatValue();
        }
        return 0.0f;
    }

    public static void setSpaceAbove(MutableAttributeSet a, float i) {
        a.addAttribute(SpaceAbove, Float.valueOf(i));
    }

    public static float getSpaceBelow(AttributeSet a) {
        Float space = (Float)a.getAttribute(SpaceBelow);
        if (space != null) {
            return space.floatValue();
        }
        return 0.0f;
    }

    public static void setSpaceBelow(MutableAttributeSet a, float i) {
        a.addAttribute(SpaceBelow, Float.valueOf(i));
    }

    public static int getAlignment(AttributeSet a) {
        Integer align = (Integer)a.getAttribute(Alignment);
        if (align != null) {
            return align;
        }
        return 0;
    }

    public static void setAlignment(MutableAttributeSet a, int align) {
        a.addAttribute(Alignment, align);
    }

    public static TabSet getTabSet(AttributeSet a) {
        TabSet tabs = (TabSet)a.getAttribute(TabSet);
        return tabs;
    }

    public static void setTabSet(MutableAttributeSet a, TabSet tabs) {
        a.addAttribute(TabSet, tabs);
    }

    StyleConstants(String representation) {
        this.representation = representation;
    }

    static {
        Family = FontFamily = new FontConstants("family");
        Size = FontSize = new FontConstants("size");
        Bold = new FontConstants("bold");
        Italic = new FontConstants("italic");
        Underline = new CharacterConstants("underline");
        StrikeThrough = new CharacterConstants("strikethrough");
        Superscript = new CharacterConstants("superscript");
        Subscript = new CharacterConstants("subscript");
        Foreground = new ColorConstants("foreground");
        Background = new ColorConstants("background");
        ComponentAttribute = new CharacterConstants(ComponentElementName);
        IconAttribute = new CharacterConstants(IconElementName);
        ComposedTextAttribute = new StyleConstants("composed text");
        FirstLineIndent = new ParagraphConstants("FirstLineIndent");
        LeftIndent = new ParagraphConstants("LeftIndent");
        RightIndent = new ParagraphConstants("RightIndent");
        LineSpacing = new ParagraphConstants("LineSpacing");
        SpaceAbove = new ParagraphConstants("SpaceAbove");
        SpaceBelow = new ParagraphConstants("SpaceBelow");
        Alignment = new ParagraphConstants("Alignment");
        TabSet = new ParagraphConstants("TabSet");
        Orientation = new ParagraphConstants("Orientation");
        keys = new Object[]{NameAttribute, ResolveAttribute, BidiLevel, FontFamily, FontSize, Bold, Italic, Underline, StrikeThrough, Superscript, Subscript, Foreground, Background, ComponentAttribute, IconAttribute, FirstLineIndent, LeftIndent, RightIndent, LineSpacing, SpaceAbove, SpaceBelow, Alignment, TabSet, Orientation, ModelAttribute, ComposedTextAttribute};
    }

    public static final class CharacterConstants
    extends StyleConstants
    implements AttributeSet.CharacterAttribute {
        private CharacterConstants(String representation) {
            super(representation);
        }
    }

    public static final class FontConstants
    extends StyleConstants
    implements AttributeSet.FontAttribute,
    AttributeSet.CharacterAttribute {
        private FontConstants(String representation) {
            super(representation);
        }
    }

    public static final class ColorConstants
    extends StyleConstants
    implements AttributeSet.ColorAttribute,
    AttributeSet.CharacterAttribute {
        private ColorConstants(String representation) {
            super(representation);
        }
    }

    public static final class ParagraphConstants
    extends StyleConstants
    implements AttributeSet.ParagraphAttribute {
        private ParagraphConstants(String representation) {
            super(representation);
        }
    }
}

