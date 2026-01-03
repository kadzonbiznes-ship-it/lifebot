/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.font.GraphicAttribute;
import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.awt.font.TransformAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.im.InputMethodHighlight;
import java.io.Serializable;
import java.text.Annotation;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import sun.font.AttributeMap;
import sun.font.EAttribute;

public final class AttributeValues
implements Cloneable {
    private int defined;
    private int nondefault;
    private String family = "Default";
    private float weight = 1.0f;
    private float width = 1.0f;
    private float posture;
    private float size = 12.0f;
    private float tracking;
    private NumericShaper numericShaping;
    private AffineTransform transform;
    private GraphicAttribute charReplacement;
    private Paint foreground;
    private Paint background;
    private float justification = 1.0f;
    private Object imHighlight;
    private Font font;
    private byte imUnderline = (byte)-1;
    private byte superscript;
    private byte underline = (byte)-1;
    private byte runDirection = (byte)-2;
    private byte bidiEmbedding;
    private byte kerning;
    private byte ligatures;
    private boolean strikethrough;
    private boolean swapColors;
    private AffineTransform baselineTransform;
    private AffineTransform charTransform;
    private static final AttributeValues DEFAULT = new AttributeValues();
    public static final int MASK_ALL = AttributeValues.getMask(EAttribute.values());
    private static final String DEFINED_KEY = "sun.font.attributevalues.defined_key";

    public String getFamily() {
        return this.family;
    }

    public void setFamily(String f) {
        this.family = f;
        this.update(EAttribute.EFAMILY);
    }

    public float getWeight() {
        return this.weight;
    }

    public void setWeight(float f) {
        this.weight = f;
        this.update(EAttribute.EWEIGHT);
    }

    public float getWidth() {
        return this.width;
    }

    public void setWidth(float f) {
        this.width = f;
        this.update(EAttribute.EWIDTH);
    }

    public float getPosture() {
        return this.posture;
    }

    public void setPosture(float f) {
        this.posture = f;
        this.update(EAttribute.EPOSTURE);
    }

    public float getSize() {
        return this.size;
    }

    public void setSize(float f) {
        this.size = f;
        this.update(EAttribute.ESIZE);
    }

    public AffineTransform getTransform() {
        return this.transform;
    }

    public void setTransform(AffineTransform f) {
        this.transform = f == null || f.isIdentity() ? AttributeValues.DEFAULT.transform : new AffineTransform(f);
        this.updateDerivedTransforms();
        this.update(EAttribute.ETRANSFORM);
    }

    public void setTransform(TransformAttribute f) {
        this.transform = f == null || f.isIdentity() ? AttributeValues.DEFAULT.transform : f.getTransform();
        this.updateDerivedTransforms();
        this.update(EAttribute.ETRANSFORM);
    }

    public int getSuperscript() {
        return this.superscript;
    }

    public void setSuperscript(int f) {
        this.superscript = (byte)f;
        this.update(EAttribute.ESUPERSCRIPT);
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(Font f) {
        this.font = f;
        this.update(EAttribute.EFONT);
    }

    public GraphicAttribute getCharReplacement() {
        return this.charReplacement;
    }

    public void setCharReplacement(GraphicAttribute f) {
        this.charReplacement = f;
        this.update(EAttribute.ECHAR_REPLACEMENT);
    }

    public Paint getForeground() {
        return this.foreground;
    }

    public void setForeground(Paint f) {
        this.foreground = f;
        this.update(EAttribute.EFOREGROUND);
    }

    public Paint getBackground() {
        return this.background;
    }

    public void setBackground(Paint f) {
        this.background = f;
        this.update(EAttribute.EBACKGROUND);
    }

    public int getUnderline() {
        return this.underline;
    }

    public void setUnderline(int f) {
        this.underline = (byte)f;
        this.update(EAttribute.EUNDERLINE);
    }

    public boolean getStrikethrough() {
        return this.strikethrough;
    }

    public void setStrikethrough(boolean f) {
        this.strikethrough = f;
        this.update(EAttribute.ESTRIKETHROUGH);
    }

    public int getRunDirection() {
        return this.runDirection;
    }

    public void setRunDirection(int f) {
        this.runDirection = (byte)f;
        this.update(EAttribute.ERUN_DIRECTION);
    }

    public int getBidiEmbedding() {
        return this.bidiEmbedding;
    }

    public void setBidiEmbedding(int f) {
        this.bidiEmbedding = (byte)f;
        this.update(EAttribute.EBIDI_EMBEDDING);
    }

    public float getJustification() {
        return this.justification;
    }

    public void setJustification(float f) {
        this.justification = f;
        this.update(EAttribute.EJUSTIFICATION);
    }

    public Object getInputMethodHighlight() {
        return this.imHighlight;
    }

    public void setInputMethodHighlight(Annotation f) {
        this.imHighlight = f;
        this.update(EAttribute.EINPUT_METHOD_HIGHLIGHT);
    }

    public void setInputMethodHighlight(InputMethodHighlight f) {
        this.imHighlight = f;
        this.update(EAttribute.EINPUT_METHOD_HIGHLIGHT);
    }

    public int getInputMethodUnderline() {
        return this.imUnderline;
    }

    public void setInputMethodUnderline(int f) {
        this.imUnderline = (byte)f;
        this.update(EAttribute.EINPUT_METHOD_UNDERLINE);
    }

    public boolean getSwapColors() {
        return this.swapColors;
    }

    public void setSwapColors(boolean f) {
        this.swapColors = f;
        this.update(EAttribute.ESWAP_COLORS);
    }

    public NumericShaper getNumericShaping() {
        return this.numericShaping;
    }

    public void setNumericShaping(NumericShaper f) {
        this.numericShaping = f;
        this.update(EAttribute.ENUMERIC_SHAPING);
    }

    public int getKerning() {
        return this.kerning;
    }

    public void setKerning(int f) {
        this.kerning = (byte)f;
        this.update(EAttribute.EKERNING);
    }

    public float getTracking() {
        return this.tracking;
    }

    public void setTracking(float f) {
        this.tracking = (byte)f;
        this.update(EAttribute.ETRACKING);
    }

    public int getLigatures() {
        return this.ligatures;
    }

    public void setLigatures(int f) {
        this.ligatures = (byte)f;
        this.update(EAttribute.ELIGATURES);
    }

    public AffineTransform getBaselineTransform() {
        return this.baselineTransform;
    }

    public AffineTransform getCharTransform() {
        return this.charTransform;
    }

    public static int getMask(EAttribute att) {
        return att.mask;
    }

    public static int getMask(EAttribute ... atts) {
        int mask = 0;
        for (EAttribute a : atts) {
            mask |= a.mask;
        }
        return mask;
    }

    public void unsetDefault() {
        this.defined &= this.nondefault;
    }

    public void defineAll(int mask) {
        this.defined |= mask;
        if ((this.defined & EAttribute.EBASELINE_TRANSFORM.mask) != 0) {
            throw new InternalError("can't define derived attribute");
        }
    }

    public boolean allDefined(int mask) {
        return (this.defined & mask) == mask;
    }

    public boolean anyDefined(int mask) {
        return (this.defined & mask) != 0;
    }

    public boolean anyNonDefault(int mask) {
        return (this.nondefault & mask) != 0;
    }

    public boolean isDefined(EAttribute a) {
        return (this.defined & a.mask) != 0;
    }

    public boolean isNonDefault(EAttribute a) {
        return (this.nondefault & a.mask) != 0;
    }

    public void setDefault(EAttribute a) {
        if (a.att == null) {
            throw new InternalError("can't set default derived attribute: " + String.valueOf((Object)a));
        }
        this.i_set(a, DEFAULT);
        this.defined |= a.mask;
        this.nondefault &= ~a.mask;
    }

    public void unset(EAttribute a) {
        if (a.att == null) {
            throw new InternalError("can't unset derived attribute: " + String.valueOf((Object)a));
        }
        this.i_set(a, DEFAULT);
        this.defined &= ~a.mask;
        this.nondefault &= ~a.mask;
    }

    public void set(EAttribute a, AttributeValues src) {
        if (a.att == null) {
            throw new InternalError("can't set derived attribute: " + String.valueOf((Object)a));
        }
        if (src == null || src == DEFAULT) {
            this.setDefault(a);
        } else if ((src.defined & a.mask) != 0) {
            this.i_set(a, src);
            this.update(a);
        }
    }

    public void set(EAttribute a, Object o) {
        if (a.att == null) {
            throw new InternalError("can't set derived attribute: " + String.valueOf((Object)a));
        }
        if (o != null) {
            try {
                this.i_set(a, o);
                this.update(a);
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.setDefault(a);
    }

    public Object get(EAttribute a) {
        if (a.att == null) {
            throw new InternalError("can't get derived attribute: " + String.valueOf((Object)a));
        }
        if ((this.nondefault & a.mask) != 0) {
            return this.i_get(a);
        }
        return null;
    }

    public AttributeValues merge(Map<? extends AttributedCharacterIterator.Attribute, ?> map) {
        return this.merge(map, MASK_ALL);
    }

    public AttributeValues merge(Map<? extends AttributedCharacterIterator.Attribute, ?> map, int mask) {
        if (map instanceof AttributeMap && ((AttributeMap)map).getValues() != null) {
            this.merge(((AttributeMap)map).getValues(), mask);
        } else if (map != null && !map.isEmpty()) {
            for (Map.Entry<AttributedCharacterIterator.Attribute, ?> e : map.entrySet()) {
                try {
                    EAttribute ea = EAttribute.forAttribute(e.getKey());
                    if (ea == null || (mask & ea.mask) == 0) continue;
                    this.set(ea, e.getValue());
                }
                catch (ClassCastException classCastException) {}
            }
        }
        return this;
    }

    public AttributeValues merge(AttributeValues src) {
        return this.merge(src, MASK_ALL);
    }

    public AttributeValues merge(AttributeValues src, int mask) {
        int m = mask & src.defined;
        for (EAttribute ea : EAttribute.atts) {
            if (m == 0) break;
            if ((m & ea.mask) == 0) continue;
            m &= ~ea.mask;
            this.i_set(ea, src);
            this.update(ea);
        }
        return this;
    }

    public static AttributeValues fromMap(Map<? extends AttributedCharacterIterator.Attribute, ?> map) {
        return AttributeValues.fromMap(map, MASK_ALL);
    }

    public static AttributeValues fromMap(Map<? extends AttributedCharacterIterator.Attribute, ?> map, int mask) {
        return new AttributeValues().merge(map, mask);
    }

    public Map<TextAttribute, Object> toMap(Map<TextAttribute, Object> fill) {
        if (fill == null) {
            fill = new HashMap<TextAttribute, Object>();
        }
        int m = this.defined;
        int i = 0;
        while (m != 0) {
            EAttribute ea = EAttribute.atts[i];
            if ((m & ea.mask) != 0) {
                m &= ~ea.mask;
                fill.put(ea.att, this.get(ea));
            }
            ++i;
        }
        return fill;
    }

    public static boolean is16Hashtable(Hashtable<Object, Object> ht) {
        return ht.containsKey(DEFINED_KEY);
    }

    public static AttributeValues fromSerializableHashtable(Hashtable<Object, Object> ht) {
        AttributeValues result = new AttributeValues();
        if (ht != null && !ht.isEmpty()) {
            for (Map.Entry<Object, Object> e : ht.entrySet()) {
                Object key = e.getKey();
                Object val = e.getValue();
                if (key.equals(DEFINED_KEY)) {
                    result.defineAll((Integer)val);
                    continue;
                }
                try {
                    EAttribute ea = EAttribute.forAttribute((AttributedCharacterIterator.Attribute)key);
                    if (ea == null) continue;
                    result.set(ea, val);
                }
                catch (ClassCastException classCastException) {}
            }
        }
        return result;
    }

    public Hashtable<Object, Object> toSerializableHashtable() {
        Hashtable<Object, Object> ht = new Hashtable<Object, Object>();
        int hashkey = this.defined;
        int m = this.defined;
        int i = 0;
        while (m != 0) {
            EAttribute ea = EAttribute.atts[i];
            if ((m & ea.mask) != 0) {
                m &= ~ea.mask;
                Object o = this.get(ea);
                if (o != null) {
                    if (o instanceof Serializable) {
                        ht.put(ea.att, o);
                    } else {
                        hashkey &= ~ea.mask;
                    }
                }
            }
            ++i;
        }
        ht.put(DEFINED_KEY, hashkey);
        return ht;
    }

    public int hashCode() {
        return this.defined << 8 ^ this.nondefault;
    }

    public boolean equals(Object rhs) {
        try {
            return this.equals((AttributeValues)rhs);
        }
        catch (ClassCastException classCastException) {
            return false;
        }
    }

    public boolean equals(AttributeValues rhs) {
        if (rhs == null) {
            return false;
        }
        if (rhs == this) {
            return true;
        }
        return this.defined == rhs.defined && this.nondefault == rhs.nondefault && this.underline == rhs.underline && this.strikethrough == rhs.strikethrough && this.superscript == rhs.superscript && this.width == rhs.width && this.kerning == rhs.kerning && this.tracking == rhs.tracking && this.ligatures == rhs.ligatures && this.runDirection == rhs.runDirection && this.bidiEmbedding == rhs.bidiEmbedding && this.swapColors == rhs.swapColors && AttributeValues.equals(this.transform, rhs.transform) && AttributeValues.equals(this.foreground, rhs.foreground) && AttributeValues.equals(this.background, rhs.background) && AttributeValues.equals(this.numericShaping, rhs.numericShaping) && AttributeValues.equals(Float.valueOf(this.justification), Float.valueOf(rhs.justification)) && AttributeValues.equals(this.charReplacement, rhs.charReplacement) && this.size == rhs.size && this.weight == rhs.weight && this.posture == rhs.posture && AttributeValues.equals(this.family, rhs.family) && AttributeValues.equals(this.font, rhs.font) && this.imUnderline == rhs.imUnderline && AttributeValues.equals(this.imHighlight, rhs.imHighlight);
    }

    public AttributeValues clone() {
        try {
            AttributeValues result = (AttributeValues)super.clone();
            if (this.transform != null) {
                result.transform = new AffineTransform(this.transform);
                result.updateDerivedTransforms();
            }
            return result;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append('{');
        int m = this.defined;
        int i = 0;
        while (m != 0) {
            EAttribute ea = EAttribute.atts[i];
            if ((m & ea.mask) != 0) {
                m &= ~ea.mask;
                if (b.length() > 1) {
                    b.append(", ");
                }
                b.append((Object)ea);
                b.append('=');
                switch (ea) {
                    case EFAMILY: {
                        b.append('\"');
                        b.append(this.family);
                        b.append('\"');
                        break;
                    }
                    case EWEIGHT: {
                        b.append(this.weight);
                        break;
                    }
                    case EWIDTH: {
                        b.append(this.width);
                        break;
                    }
                    case EPOSTURE: {
                        b.append(this.posture);
                        break;
                    }
                    case ESIZE: {
                        b.append(this.size);
                        break;
                    }
                    case ETRANSFORM: {
                        b.append(this.transform);
                        break;
                    }
                    case ESUPERSCRIPT: {
                        b.append(this.superscript);
                        break;
                    }
                    case EFONT: {
                        b.append(this.font);
                        break;
                    }
                    case ECHAR_REPLACEMENT: {
                        b.append(this.charReplacement);
                        break;
                    }
                    case EFOREGROUND: {
                        b.append(this.foreground);
                        break;
                    }
                    case EBACKGROUND: {
                        b.append(this.background);
                        break;
                    }
                    case EUNDERLINE: {
                        b.append(this.underline);
                        break;
                    }
                    case ESTRIKETHROUGH: {
                        b.append(this.strikethrough);
                        break;
                    }
                    case ERUN_DIRECTION: {
                        b.append(this.runDirection);
                        break;
                    }
                    case EBIDI_EMBEDDING: {
                        b.append(this.bidiEmbedding);
                        break;
                    }
                    case EJUSTIFICATION: {
                        b.append(this.justification);
                        break;
                    }
                    case EINPUT_METHOD_HIGHLIGHT: {
                        b.append(this.imHighlight);
                        break;
                    }
                    case EINPUT_METHOD_UNDERLINE: {
                        b.append(this.imUnderline);
                        break;
                    }
                    case ESWAP_COLORS: {
                        b.append(this.swapColors);
                        break;
                    }
                    case ENUMERIC_SHAPING: {
                        b.append(this.numericShaping);
                        break;
                    }
                    case EKERNING: {
                        b.append(this.kerning);
                        break;
                    }
                    case ELIGATURES: {
                        b.append(this.ligatures);
                        break;
                    }
                    case ETRACKING: {
                        b.append(this.tracking);
                        break;
                    }
                    default: {
                        throw new InternalError();
                    }
                }
                if ((this.nondefault & ea.mask) == 0) {
                    b.append('*');
                }
            }
            ++i;
        }
        b.append("[btx=" + String.valueOf(this.baselineTransform) + ", ctx=" + String.valueOf(this.charTransform) + "]");
        b.append('}');
        return b.toString();
    }

    private static boolean equals(Object lhs, Object rhs) {
        return lhs == null ? rhs == null : lhs.equals(rhs);
    }

    private void update(EAttribute a) {
        this.defined |= a.mask;
        if (this.i_validate(a)) {
            this.nondefault = this.i_equals(a, DEFAULT) ? (this.nondefault &= ~a.mask) : (this.nondefault |= a.mask);
        } else {
            this.setDefault(a);
        }
    }

    private void i_set(EAttribute a, AttributeValues src) {
        switch (a) {
            case EFAMILY: {
                this.family = src.family;
                break;
            }
            case EWEIGHT: {
                this.weight = src.weight;
                break;
            }
            case EWIDTH: {
                this.width = src.width;
                break;
            }
            case EPOSTURE: {
                this.posture = src.posture;
                break;
            }
            case ESIZE: {
                this.size = src.size;
                break;
            }
            case ETRANSFORM: {
                this.transform = src.transform;
                this.updateDerivedTransforms();
                break;
            }
            case ESUPERSCRIPT: {
                this.superscript = src.superscript;
                break;
            }
            case EFONT: {
                this.font = src.font;
                break;
            }
            case ECHAR_REPLACEMENT: {
                this.charReplacement = src.charReplacement;
                break;
            }
            case EFOREGROUND: {
                this.foreground = src.foreground;
                break;
            }
            case EBACKGROUND: {
                this.background = src.background;
                break;
            }
            case EUNDERLINE: {
                this.underline = src.underline;
                break;
            }
            case ESTRIKETHROUGH: {
                this.strikethrough = src.strikethrough;
                break;
            }
            case ERUN_DIRECTION: {
                this.runDirection = src.runDirection;
                break;
            }
            case EBIDI_EMBEDDING: {
                this.bidiEmbedding = src.bidiEmbedding;
                break;
            }
            case EJUSTIFICATION: {
                this.justification = src.justification;
                break;
            }
            case EINPUT_METHOD_HIGHLIGHT: {
                this.imHighlight = src.imHighlight;
                break;
            }
            case EINPUT_METHOD_UNDERLINE: {
                this.imUnderline = src.imUnderline;
                break;
            }
            case ESWAP_COLORS: {
                this.swapColors = src.swapColors;
                break;
            }
            case ENUMERIC_SHAPING: {
                this.numericShaping = src.numericShaping;
                break;
            }
            case EKERNING: {
                this.kerning = src.kerning;
                break;
            }
            case ELIGATURES: {
                this.ligatures = src.ligatures;
                break;
            }
            case ETRACKING: {
                this.tracking = src.tracking;
                break;
            }
            default: {
                throw new InternalError();
            }
        }
    }

    private boolean i_equals(EAttribute a, AttributeValues src) {
        switch (a) {
            case EFAMILY: {
                return AttributeValues.equals(this.family, src.family);
            }
            case EWEIGHT: {
                return this.weight == src.weight;
            }
            case EWIDTH: {
                return this.width == src.width;
            }
            case EPOSTURE: {
                return this.posture == src.posture;
            }
            case ESIZE: {
                return this.size == src.size;
            }
            case ETRANSFORM: {
                return AttributeValues.equals(this.transform, src.transform);
            }
            case ESUPERSCRIPT: {
                return this.superscript == src.superscript;
            }
            case EFONT: {
                return AttributeValues.equals(this.font, src.font);
            }
            case ECHAR_REPLACEMENT: {
                return AttributeValues.equals(this.charReplacement, src.charReplacement);
            }
            case EFOREGROUND: {
                return AttributeValues.equals(this.foreground, src.foreground);
            }
            case EBACKGROUND: {
                return AttributeValues.equals(this.background, src.background);
            }
            case EUNDERLINE: {
                return this.underline == src.underline;
            }
            case ESTRIKETHROUGH: {
                return this.strikethrough == src.strikethrough;
            }
            case ERUN_DIRECTION: {
                return this.runDirection == src.runDirection;
            }
            case EBIDI_EMBEDDING: {
                return this.bidiEmbedding == src.bidiEmbedding;
            }
            case EJUSTIFICATION: {
                return this.justification == src.justification;
            }
            case EINPUT_METHOD_HIGHLIGHT: {
                return AttributeValues.equals(this.imHighlight, src.imHighlight);
            }
            case EINPUT_METHOD_UNDERLINE: {
                return this.imUnderline == src.imUnderline;
            }
            case ESWAP_COLORS: {
                return this.swapColors == src.swapColors;
            }
            case ENUMERIC_SHAPING: {
                return AttributeValues.equals(this.numericShaping, src.numericShaping);
            }
            case EKERNING: {
                return this.kerning == src.kerning;
            }
            case ELIGATURES: {
                return this.ligatures == src.ligatures;
            }
            case ETRACKING: {
                return this.tracking == src.tracking;
            }
        }
        throw new InternalError();
    }

    private void i_set(EAttribute a, Object o) {
        switch (a) {
            case EFAMILY: {
                this.family = ((String)o).trim();
                break;
            }
            case EWEIGHT: {
                this.weight = ((Number)o).floatValue();
                break;
            }
            case EWIDTH: {
                this.width = ((Number)o).floatValue();
                break;
            }
            case EPOSTURE: {
                this.posture = ((Number)o).floatValue();
                break;
            }
            case ESIZE: {
                this.size = ((Number)o).floatValue();
                break;
            }
            case ETRANSFORM: {
                TransformAttribute ta;
                this.transform = o instanceof TransformAttribute ? ((ta = (TransformAttribute)o).isIdentity() ? null : ta.getTransform()) : new AffineTransform((AffineTransform)o);
                this.updateDerivedTransforms();
                break;
            }
            case ESUPERSCRIPT: {
                this.superscript = (byte)((Integer)o).intValue();
                break;
            }
            case EFONT: {
                this.font = (Font)o;
                break;
            }
            case ECHAR_REPLACEMENT: {
                this.charReplacement = (GraphicAttribute)o;
                break;
            }
            case EFOREGROUND: {
                this.foreground = (Paint)o;
                break;
            }
            case EBACKGROUND: {
                this.background = (Paint)o;
                break;
            }
            case EUNDERLINE: {
                this.underline = (byte)((Integer)o).intValue();
                break;
            }
            case ESTRIKETHROUGH: {
                this.strikethrough = (Boolean)o;
                break;
            }
            case ERUN_DIRECTION: {
                if (o instanceof Boolean) {
                    this.runDirection = (byte)(!TextAttribute.RUN_DIRECTION_LTR.equals(o) ? 1 : 0);
                    break;
                }
                this.runDirection = (byte)((Integer)o).intValue();
                break;
            }
            case EBIDI_EMBEDDING: {
                this.bidiEmbedding = (byte)((Integer)o).intValue();
                break;
            }
            case EJUSTIFICATION: {
                this.justification = ((Number)o).floatValue();
                break;
            }
            case EINPUT_METHOD_HIGHLIGHT: {
                if (o instanceof Annotation) {
                    Annotation at = (Annotation)o;
                    this.imHighlight = (InputMethodHighlight)at.getValue();
                    break;
                }
                this.imHighlight = (InputMethodHighlight)o;
                break;
            }
            case EINPUT_METHOD_UNDERLINE: {
                this.imUnderline = (byte)((Integer)o).intValue();
                break;
            }
            case ESWAP_COLORS: {
                this.swapColors = (Boolean)o;
                break;
            }
            case ENUMERIC_SHAPING: {
                this.numericShaping = (NumericShaper)o;
                break;
            }
            case EKERNING: {
                this.kerning = (byte)((Integer)o).intValue();
                break;
            }
            case ELIGATURES: {
                this.ligatures = (byte)((Integer)o).intValue();
                break;
            }
            case ETRACKING: {
                this.tracking = ((Number)o).floatValue();
                break;
            }
            default: {
                throw new InternalError();
            }
        }
    }

    private Object i_get(EAttribute a) {
        switch (a) {
            case EFAMILY: {
                return this.family;
            }
            case EWEIGHT: {
                return Float.valueOf(this.weight);
            }
            case EWIDTH: {
                return Float.valueOf(this.width);
            }
            case EPOSTURE: {
                return Float.valueOf(this.posture);
            }
            case ESIZE: {
                return Float.valueOf(this.size);
            }
            case ETRANSFORM: {
                return this.transform == null ? TransformAttribute.IDENTITY : new TransformAttribute(this.transform);
            }
            case ESUPERSCRIPT: {
                return (int)this.superscript;
            }
            case EFONT: {
                return this.font;
            }
            case ECHAR_REPLACEMENT: {
                return this.charReplacement;
            }
            case EFOREGROUND: {
                return this.foreground;
            }
            case EBACKGROUND: {
                return this.background;
            }
            case EUNDERLINE: {
                return (int)this.underline;
            }
            case ESTRIKETHROUGH: {
                return this.strikethrough;
            }
            case ERUN_DIRECTION: {
                switch (this.runDirection) {
                    case 0: {
                        return TextAttribute.RUN_DIRECTION_LTR;
                    }
                    case 1: {
                        return TextAttribute.RUN_DIRECTION_RTL;
                    }
                }
                return null;
            }
            case EBIDI_EMBEDDING: {
                return (int)this.bidiEmbedding;
            }
            case EJUSTIFICATION: {
                return Float.valueOf(this.justification);
            }
            case EINPUT_METHOD_HIGHLIGHT: {
                return this.imHighlight;
            }
            case EINPUT_METHOD_UNDERLINE: {
                return (int)this.imUnderline;
            }
            case ESWAP_COLORS: {
                return this.swapColors;
            }
            case ENUMERIC_SHAPING: {
                return this.numericShaping;
            }
            case EKERNING: {
                return (int)this.kerning;
            }
            case ELIGATURES: {
                return (int)this.ligatures;
            }
            case ETRACKING: {
                return Float.valueOf(this.tracking);
            }
        }
        throw new InternalError();
    }

    private boolean i_validate(EAttribute a) {
        switch (a) {
            case EFAMILY: {
                if (this.family == null || this.family.length() == 0) {
                    this.family = AttributeValues.DEFAULT.family;
                }
                return true;
            }
            case EWEIGHT: {
                return this.weight > 0.0f && this.weight < 10.0f;
            }
            case EWIDTH: {
                return this.width >= 0.5f && this.width < 10.0f;
            }
            case EPOSTURE: {
                return this.posture >= -1.0f && this.posture <= 1.0f;
            }
            case ESIZE: {
                return this.size >= 0.0f;
            }
            case ETRANSFORM: {
                if (this.transform != null && this.transform.isIdentity()) {
                    this.transform = AttributeValues.DEFAULT.transform;
                }
                return true;
            }
            case ESUPERSCRIPT: {
                return this.superscript >= -7 && this.superscript <= 7;
            }
            case EFONT: {
                return true;
            }
            case ECHAR_REPLACEMENT: {
                return true;
            }
            case EFOREGROUND: {
                return true;
            }
            case EBACKGROUND: {
                return true;
            }
            case EUNDERLINE: {
                return this.underline >= -1 && this.underline < 6;
            }
            case ESTRIKETHROUGH: {
                return true;
            }
            case ERUN_DIRECTION: {
                return this.runDirection >= -2 && this.runDirection <= 1;
            }
            case EBIDI_EMBEDDING: {
                return this.bidiEmbedding >= -61 && this.bidiEmbedding < 62;
            }
            case EJUSTIFICATION: {
                this.justification = Math.max(0.0f, Math.min(this.justification, 1.0f));
                return true;
            }
            case EINPUT_METHOD_HIGHLIGHT: {
                return true;
            }
            case EINPUT_METHOD_UNDERLINE: {
                return this.imUnderline >= -1 && this.imUnderline < 6;
            }
            case ESWAP_COLORS: {
                return true;
            }
            case ENUMERIC_SHAPING: {
                return true;
            }
            case EKERNING: {
                return this.kerning >= 0 && this.kerning <= 1;
            }
            case ELIGATURES: {
                return this.ligatures >= 0 && this.ligatures <= 1;
            }
            case ETRACKING: {
                return this.tracking >= -1.0f && this.tracking <= 10.0f;
            }
        }
        throw new InternalError("unknown attribute: " + String.valueOf((Object)a));
    }

    public static float getJustification(Map<?, ?> map) {
        if (map != null) {
            if (map instanceof AttributeMap && ((AttributeMap)map).getValues() != null) {
                return ((AttributeMap)map).getValues().justification;
            }
            Object obj = map.get(TextAttribute.JUSTIFICATION);
            if (obj instanceof Number) {
                Number number = (Number)obj;
                return Math.max(0.0f, Math.min(1.0f, number.floatValue()));
            }
        }
        return AttributeValues.DEFAULT.justification;
    }

    public static NumericShaper getNumericShaping(Map<?, ?> map) {
        if (map != null) {
            if (map instanceof AttributeMap && ((AttributeMap)map).getValues() != null) {
                return ((AttributeMap)map).getValues().numericShaping;
            }
            Object obj = map.get(TextAttribute.NUMERIC_SHAPING);
            if (obj instanceof NumericShaper) {
                NumericShaper shaper = (NumericShaper)obj;
                return shaper;
            }
        }
        return AttributeValues.DEFAULT.numericShaping;
    }

    public AttributeValues applyIMHighlight() {
        if (this.imHighlight != null) {
            InputMethodHighlight hl = null;
            hl = this.imHighlight instanceof InputMethodHighlight ? (InputMethodHighlight)this.imHighlight : (InputMethodHighlight)((Annotation)this.imHighlight).getValue();
            Map<TextAttribute, ?> imStyles = hl.getStyle();
            if (imStyles == null) {
                Toolkit tk = Toolkit.getDefaultToolkit();
                imStyles = tk.mapInputMethodHighlight(hl);
            }
            if (imStyles != null) {
                return this.clone().merge(imStyles);
            }
        }
        return this;
    }

    public static AffineTransform getBaselineTransform(Map<?, ?> map) {
        if (map != null) {
            AttributeValues av = null;
            if (map instanceof AttributeMap && ((AttributeMap)map).getValues() != null) {
                av = ((AttributeMap)map).getValues();
            } else if (map.get(TextAttribute.TRANSFORM) != null) {
                av = AttributeValues.fromMap(map);
            }
            if (av != null) {
                return av.baselineTransform;
            }
        }
        return null;
    }

    public static AffineTransform getCharTransform(Map<?, ?> map) {
        if (map != null) {
            AttributeValues av = null;
            if (map instanceof AttributeMap && ((AttributeMap)map).getValues() != null) {
                av = ((AttributeMap)map).getValues();
            } else if (map.get(TextAttribute.TRANSFORM) != null) {
                av = AttributeValues.fromMap(map);
            }
            if (av != null) {
                return av.charTransform;
            }
        }
        return null;
    }

    public static float getTracking(Map<?, ?> map) {
        if (map != null) {
            AttributeValues av = null;
            if (map instanceof AttributeMap && ((AttributeMap)map).getValues() != null) {
                av = ((AttributeMap)map).getValues();
            } else if (map.get(TextAttribute.TRACKING) != null) {
                av = AttributeValues.fromMap(map);
            }
            if (av != null) {
                return av.tracking;
            }
        }
        return 0.0f;
    }

    public void updateDerivedTransforms() {
        if (this.transform == null) {
            this.baselineTransform = null;
            this.charTransform = null;
        } else {
            this.charTransform = new AffineTransform(this.transform);
            this.baselineTransform = AttributeValues.extractXRotation(this.charTransform, true);
            if (this.charTransform.isIdentity()) {
                this.charTransform = null;
            }
            if (this.baselineTransform.isIdentity()) {
                this.baselineTransform = null;
            }
        }
        this.nondefault = this.baselineTransform == null ? (this.nondefault &= ~EAttribute.EBASELINE_TRANSFORM.mask) : (this.nondefault |= EAttribute.EBASELINE_TRANSFORM.mask);
    }

    public static AffineTransform extractXRotation(AffineTransform tx, boolean andTranslation) {
        return AttributeValues.extractRotation(new Point2D.Double(1.0, 0.0), tx, andTranslation);
    }

    public static AffineTransform extractYRotation(AffineTransform tx, boolean andTranslation) {
        return AttributeValues.extractRotation(new Point2D.Double(0.0, 1.0), tx, andTranslation);
    }

    private static AffineTransform extractRotation(Point2D.Double pt, AffineTransform tx, boolean andTranslation) {
        tx.deltaTransform(pt, pt);
        AffineTransform rtx = AffineTransform.getRotateInstance(pt.x, pt.y);
        try {
            AffineTransform rtxi = rtx.createInverse();
            double dx = tx.getTranslateX();
            double dy = tx.getTranslateY();
            tx.preConcatenate(rtxi);
            if (andTranslation && (dx != 0.0 || dy != 0.0)) {
                tx.setTransform(tx.getScaleX(), tx.getShearY(), tx.getShearX(), tx.getScaleY(), 0.0, 0.0);
                rtx.setTransform(rtx.getScaleX(), rtx.getShearY(), rtx.getShearX(), rtx.getScaleY(), dx, dy);
            }
        }
        catch (NoninvertibleTransformException e) {
            return null;
        }
        return rtx;
    }
}

