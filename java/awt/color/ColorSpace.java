/*
 * Decompiled with CFR 0.152.
 */
package java.awt.color;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.Serializable;

public abstract class ColorSpace
implements Serializable {
    private static final long serialVersionUID = -409452704308689724L;
    private final int type;
    private final int numComponents;
    private volatile transient String[] compName;
    public static final int TYPE_XYZ = 0;
    public static final int TYPE_Lab = 1;
    public static final int TYPE_Luv = 2;
    public static final int TYPE_YCbCr = 3;
    public static final int TYPE_Yxy = 4;
    public static final int TYPE_RGB = 5;
    public static final int TYPE_GRAY = 6;
    public static final int TYPE_HSV = 7;
    public static final int TYPE_HLS = 8;
    public static final int TYPE_CMYK = 9;
    public static final int TYPE_CMY = 11;
    public static final int TYPE_2CLR = 12;
    public static final int TYPE_3CLR = 13;
    public static final int TYPE_4CLR = 14;
    public static final int TYPE_5CLR = 15;
    public static final int TYPE_6CLR = 16;
    public static final int TYPE_7CLR = 17;
    public static final int TYPE_8CLR = 18;
    public static final int TYPE_9CLR = 19;
    public static final int TYPE_ACLR = 20;
    public static final int TYPE_BCLR = 21;
    public static final int TYPE_CCLR = 22;
    public static final int TYPE_DCLR = 23;
    public static final int TYPE_ECLR = 24;
    public static final int TYPE_FCLR = 25;
    public static final int CS_sRGB = 1000;
    public static final int CS_LINEAR_RGB = 1004;
    public static final int CS_CIEXYZ = 1001;
    public static final int CS_PYCC = 1002;
    public static final int CS_GRAY = 1003;

    protected ColorSpace(int type, int numComponents) {
        this.type = type;
        this.numComponents = numComponents;
    }

    public static ColorSpace getInstance(int cspace) {
        return switch (cspace) {
            case 1000 -> BuiltInSpace.SRGB;
            case 1004 -> BuiltInSpace.LRGB;
            case 1001 -> BuiltInSpace.XYZ;
            case 1002 -> BuiltInSpace.PYCC;
            case 1003 -> BuiltInSpace.GRAY;
            default -> throw new IllegalArgumentException("Unknown color space");
        };
    }

    public boolean isCS_sRGB() {
        return this == BuiltInSpace.SRGB;
    }

    public abstract float[] toRGB(float[] var1);

    public abstract float[] fromRGB(float[] var1);

    public abstract float[] toCIEXYZ(float[] var1);

    public abstract float[] fromCIEXYZ(float[] var1);

    public int getType() {
        return this.type;
    }

    public int getNumComponents() {
        return this.numComponents;
    }

    public String getName(int component) {
        this.rangeCheck(component);
        if (this.compName == null) {
            String[] stringArray;
            switch (this.type) {
                case 0: {
                    String[] stringArray2 = new String[3];
                    stringArray2[0] = "X";
                    stringArray2[1] = "Y";
                    stringArray = stringArray2;
                    stringArray2[2] = "Z";
                    break;
                }
                case 1: {
                    String[] stringArray3 = new String[3];
                    stringArray3[0] = "L";
                    stringArray3[1] = "a";
                    stringArray = stringArray3;
                    stringArray3[2] = "b";
                    break;
                }
                case 2: {
                    String[] stringArray4 = new String[3];
                    stringArray4[0] = "L";
                    stringArray4[1] = "u";
                    stringArray = stringArray4;
                    stringArray4[2] = "v";
                    break;
                }
                case 3: {
                    String[] stringArray5 = new String[3];
                    stringArray5[0] = "Y";
                    stringArray5[1] = "Cb";
                    stringArray = stringArray5;
                    stringArray5[2] = "Cr";
                    break;
                }
                case 4: {
                    String[] stringArray6 = new String[3];
                    stringArray6[0] = "Y";
                    stringArray6[1] = "x";
                    stringArray = stringArray6;
                    stringArray6[2] = "y";
                    break;
                }
                case 5: {
                    String[] stringArray7 = new String[3];
                    stringArray7[0] = "Red";
                    stringArray7[1] = "Green";
                    stringArray = stringArray7;
                    stringArray7[2] = "Blue";
                    break;
                }
                case 6: {
                    String[] stringArray8 = new String[1];
                    stringArray = stringArray8;
                    stringArray8[0] = "Gray";
                    break;
                }
                case 7: {
                    String[] stringArray9 = new String[3];
                    stringArray9[0] = "Hue";
                    stringArray9[1] = "Saturation";
                    stringArray = stringArray9;
                    stringArray9[2] = "Value";
                    break;
                }
                case 8: {
                    String[] stringArray10 = new String[3];
                    stringArray10[0] = "Hue";
                    stringArray10[1] = "Lightness";
                    stringArray = stringArray10;
                    stringArray10[2] = "Saturation";
                    break;
                }
                case 9: {
                    String[] stringArray11 = new String[4];
                    stringArray11[0] = "Cyan";
                    stringArray11[1] = "Magenta";
                    stringArray11[2] = "Yellow";
                    stringArray = stringArray11;
                    stringArray11[3] = "Black";
                    break;
                }
                case 11: {
                    String[] stringArray12 = new String[3];
                    stringArray12[0] = "Cyan";
                    stringArray12[1] = "Magenta";
                    stringArray = stringArray12;
                    stringArray12[2] = "Yellow";
                    break;
                }
                default: {
                    String[] tmp = new String[this.getNumComponents()];
                    for (int i = 0; i < tmp.length; ++i) {
                        tmp[i] = "Unnamed color component(" + i + ")";
                    }
                    stringArray = tmp;
                    break;
                }
            }
            this.compName = stringArray;
        }
        return this.compName[component];
    }

    public float getMinValue(int component) {
        this.rangeCheck(component);
        return 0.0f;
    }

    public float getMaxValue(int component) {
        this.rangeCheck(component);
        return 1.0f;
    }

    final void rangeCheck(int component) {
        if (component < 0 || component > this.getNumComponents() - 1) {
            throw new IllegalArgumentException("Component index out of range: " + component);
        }
    }

    private static interface BuiltInSpace {
        public static final ColorSpace SRGB = new ICC_ColorSpace(ICC_Profile.getInstance(1000));
        public static final ColorSpace LRGB = new ICC_ColorSpace(ICC_Profile.getInstance(1004));
        public static final ColorSpace XYZ = new ICC_ColorSpace(ICC_Profile.getInstance(1001));
        public static final ColorSpace PYCC = new ICC_ColorSpace(ICC_Profile.getInstance(1002));
        public static final ColorSpace GRAY = new ICC_ColorSpace(ICC_Profile.getInstance(1003));
    }
}

