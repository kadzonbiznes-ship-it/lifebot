/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.AlphaComposite;
import java.util.HashMap;

public final class CompositeType {
    private static int unusedUID = 1;
    private static final HashMap<String, Integer> compositeUIDMap = new HashMap(100);
    public static final String DESC_ANY = "Any CompositeContext";
    public static final String DESC_XOR = "XOR mode";
    public static final String DESC_CLEAR = "Porter-Duff Clear";
    public static final String DESC_SRC = "Porter-Duff Src";
    public static final String DESC_DST = "Porter-Duff Dst";
    public static final String DESC_SRC_OVER = "Porter-Duff Src Over Dst";
    public static final String DESC_DST_OVER = "Porter-Duff Dst Over Src";
    public static final String DESC_SRC_IN = "Porter-Duff Src In Dst";
    public static final String DESC_DST_IN = "Porter-Duff Dst In Src";
    public static final String DESC_SRC_OUT = "Porter-Duff Src HeldOutBy Dst";
    public static final String DESC_DST_OUT = "Porter-Duff Dst HeldOutBy Src";
    public static final String DESC_SRC_ATOP = "Porter-Duff Src Atop Dst";
    public static final String DESC_DST_ATOP = "Porter-Duff Dst Atop Src";
    public static final String DESC_ALPHA_XOR = "Porter-Duff Xor";
    public static final String DESC_SRC_NO_EA = "Porter-Duff Src, No Extra Alpha";
    public static final String DESC_SRC_OVER_NO_EA = "Porter-Duff SrcOverDst, No Extra Alpha";
    public static final String DESC_ANY_ALPHA = "Any AlphaComposite Rule";
    public static final CompositeType Any;
    public static final CompositeType General;
    public static final CompositeType AnyAlpha;
    public static final CompositeType Xor;
    public static final CompositeType Clear;
    public static final CompositeType Src;
    public static final CompositeType Dst;
    public static final CompositeType SrcOver;
    public static final CompositeType DstOver;
    public static final CompositeType SrcIn;
    public static final CompositeType DstIn;
    public static final CompositeType SrcOut;
    public static final CompositeType DstOut;
    public static final CompositeType SrcAtop;
    public static final CompositeType DstAtop;
    public static final CompositeType AlphaXor;
    public static final CompositeType SrcNoEa;
    public static final CompositeType SrcOverNoEa;
    public static final CompositeType OpaqueSrcOverNoEa;
    private int uniqueID;
    private String desc;
    private CompositeType next;

    public CompositeType deriveSubType(String desc) {
        return new CompositeType(this, desc);
    }

    public static CompositeType forAlphaComposite(AlphaComposite ac) {
        switch (ac.getRule()) {
            case 1: {
                return Clear;
            }
            case 2: {
                if (ac.getAlpha() >= 1.0f) {
                    return SrcNoEa;
                }
                return Src;
            }
            case 9: {
                return Dst;
            }
            case 3: {
                if (ac.getAlpha() >= 1.0f) {
                    return SrcOverNoEa;
                }
                return SrcOver;
            }
            case 4: {
                return DstOver;
            }
            case 5: {
                return SrcIn;
            }
            case 6: {
                return DstIn;
            }
            case 7: {
                return SrcOut;
            }
            case 8: {
                return DstOut;
            }
            case 10: {
                return SrcAtop;
            }
            case 11: {
                return DstAtop;
            }
            case 12: {
                return AlphaXor;
            }
        }
        throw new InternalError("Unrecognized alpha rule");
    }

    private CompositeType(CompositeType parent, String desc) {
        this.next = parent;
        this.desc = desc;
        this.uniqueID = CompositeType.makeUniqueID(desc);
    }

    public static synchronized int makeUniqueID(String desc) {
        Integer i = compositeUIDMap.get(desc);
        if (i == null) {
            if (unusedUID > 255) {
                throw new InternalError("composite type id overflow");
            }
            i = unusedUID++;
            compositeUIDMap.put(desc, i);
        }
        return i;
    }

    public int getUniqueID() {
        return this.uniqueID;
    }

    public String getDescriptor() {
        return this.desc;
    }

    public CompositeType getSuperType() {
        return this.next;
    }

    public int hashCode() {
        return this.desc.hashCode();
    }

    public boolean isDerivedFrom(CompositeType other) {
        CompositeType comptype = this;
        do {
            if (comptype.desc != other.desc) continue;
            return true;
        } while ((comptype = comptype.next) != null);
        return false;
    }

    public boolean equals(Object o) {
        if (o instanceof CompositeType) {
            return ((CompositeType)o).uniqueID == this.uniqueID;
        }
        return false;
    }

    public String toString() {
        return this.desc;
    }

    static {
        General = Any = new CompositeType(null, DESC_ANY);
        AnyAlpha = General.deriveSubType(DESC_ANY_ALPHA);
        Xor = General.deriveSubType(DESC_XOR);
        Clear = AnyAlpha.deriveSubType(DESC_CLEAR);
        Src = AnyAlpha.deriveSubType(DESC_SRC);
        Dst = AnyAlpha.deriveSubType(DESC_DST);
        SrcOver = AnyAlpha.deriveSubType(DESC_SRC_OVER);
        DstOver = AnyAlpha.deriveSubType(DESC_DST_OVER);
        SrcIn = AnyAlpha.deriveSubType(DESC_SRC_IN);
        DstIn = AnyAlpha.deriveSubType(DESC_DST_IN);
        SrcOut = AnyAlpha.deriveSubType(DESC_SRC_OUT);
        DstOut = AnyAlpha.deriveSubType(DESC_DST_OUT);
        SrcAtop = AnyAlpha.deriveSubType(DESC_SRC_ATOP);
        DstAtop = AnyAlpha.deriveSubType(DESC_DST_ATOP);
        AlphaXor = AnyAlpha.deriveSubType(DESC_ALPHA_XOR);
        SrcNoEa = Src.deriveSubType(DESC_SRC_NO_EA);
        SrcOverNoEa = SrcOver.deriveSubType(DESC_SRC_OVER_NO_EA);
        OpaqueSrcOverNoEa = SrcOverNoEa.deriveSubType(DESC_SRC).deriveSubType(DESC_SRC_NO_EA);
    }
}

