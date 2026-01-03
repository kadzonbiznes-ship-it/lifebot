/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AffineTransform
implements Cloneable,
Serializable {
    private static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_IDENTITY = 0;
    public static final int TYPE_TRANSLATION = 1;
    public static final int TYPE_UNIFORM_SCALE = 2;
    public static final int TYPE_GENERAL_SCALE = 4;
    public static final int TYPE_MASK_SCALE = 6;
    public static final int TYPE_FLIP = 64;
    public static final int TYPE_QUADRANT_ROTATION = 8;
    public static final int TYPE_GENERAL_ROTATION = 16;
    public static final int TYPE_MASK_ROTATION = 24;
    public static final int TYPE_GENERAL_TRANSFORM = 32;
    static final int APPLY_IDENTITY = 0;
    static final int APPLY_TRANSLATE = 1;
    static final int APPLY_SCALE = 2;
    static final int APPLY_SHEAR = 4;
    private static final int HI_SHIFT = 3;
    private static final int HI_IDENTITY = 0;
    private static final int HI_TRANSLATE = 8;
    private static final int HI_SCALE = 16;
    private static final int HI_SHEAR = 32;
    double m00;
    double m10;
    double m01;
    double m11;
    double m02;
    double m12;
    transient int state;
    private transient int type;
    private static final int[] rot90conversion = new int[]{4, 5, 4, 5, 2, 3, 6, 7};
    private static final long serialVersionUID = 1330973210523860834L;

    private AffineTransform(double m00, double m10, double m01, double m11, double m02, double m12, int state) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.state = state;
        this.type = -1;
    }

    public AffineTransform() {
        this.m11 = 1.0;
        this.m00 = 1.0;
    }

    public AffineTransform(AffineTransform Tx) {
        this.m00 = Tx.m00;
        this.m10 = Tx.m10;
        this.m01 = Tx.m01;
        this.m11 = Tx.m11;
        this.m02 = Tx.m02;
        this.m12 = Tx.m12;
        this.state = Tx.state;
        this.type = Tx.type;
    }

    @ConstructorProperties(value={"scaleX", "shearY", "shearX", "scaleY", "translateX", "translateY"})
    public AffineTransform(float m00, float m10, float m01, float m11, float m02, float m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.updateState();
    }

    public AffineTransform(float[] flatmatrix) {
        this.m00 = flatmatrix[0];
        this.m10 = flatmatrix[1];
        this.m01 = flatmatrix[2];
        this.m11 = flatmatrix[3];
        if (flatmatrix.length > 5) {
            this.m02 = flatmatrix[4];
            this.m12 = flatmatrix[5];
        }
        this.updateState();
    }

    public AffineTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.updateState();
    }

    public AffineTransform(double[] flatmatrix) {
        this.m00 = flatmatrix[0];
        this.m10 = flatmatrix[1];
        this.m01 = flatmatrix[2];
        this.m11 = flatmatrix[3];
        if (flatmatrix.length > 5) {
            this.m02 = flatmatrix[4];
            this.m12 = flatmatrix[5];
        }
        this.updateState();
    }

    public static AffineTransform getTranslateInstance(double tx, double ty) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToTranslation(tx, ty);
        return Tx;
    }

    public static AffineTransform getRotateInstance(double theta) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(theta);
        return Tx;
    }

    public static AffineTransform getRotateInstance(double theta, double anchorx, double anchory) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(theta, anchorx, anchory);
        return Tx;
    }

    public static AffineTransform getRotateInstance(double vecx, double vecy) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(vecx, vecy);
        return Tx;
    }

    public static AffineTransform getRotateInstance(double vecx, double vecy, double anchorx, double anchory) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(vecx, vecy, anchorx, anchory);
        return Tx;
    }

    public static AffineTransform getQuadrantRotateInstance(int numquadrants) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToQuadrantRotation(numquadrants);
        return Tx;
    }

    public static AffineTransform getQuadrantRotateInstance(int numquadrants, double anchorx, double anchory) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToQuadrantRotation(numquadrants, anchorx, anchory);
        return Tx;
    }

    public static AffineTransform getScaleInstance(double sx, double sy) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToScale(sx, sy);
        return Tx;
    }

    public static AffineTransform getShearInstance(double shx, double shy) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToShear(shx, shy);
        return Tx;
    }

    public int getType() {
        if (this.type == -1) {
            this.calculateType();
        }
        return this.type;
    }

    private void calculateType() {
        int ret = 0;
        this.updateState();
        switch (this.state) {
            default: {
                this.stateError();
            }
            case 7: {
                ret = 1;
            }
            case 6: {
                boolean sgn1;
                double M0 = this.m00;
                double M2 = this.m01;
                double M3 = this.m10;
                double M1 = this.m11;
                if (M0 * M2 + M3 * M1 != 0.0) {
                    this.type = 32;
                    return;
                }
                boolean sgn0 = M0 >= 0.0;
                boolean bl = sgn1 = M1 >= 0.0;
                if (sgn0 == sgn1) {
                    if (M0 != M1 || M2 != -M3) {
                        ret |= 0x14;
                        break;
                    }
                    if (M0 * M1 - M2 * M3 != 1.0) {
                        ret |= 0x12;
                        break;
                    }
                    ret |= 0x10;
                    break;
                }
                if (M0 != -M1 || M2 != M3) {
                    ret |= 0x54;
                    break;
                }
                if (M0 * M1 - M2 * M3 != 1.0) {
                    ret |= 0x52;
                    break;
                }
                ret |= 0x50;
                break;
            }
            case 5: {
                ret = 1;
            }
            case 4: {
                double d;
                boolean sgn1;
                double d2;
                double M0 = this.m01;
                boolean sgn0 = d2 >= 0.0;
                double M1 = this.m10;
                boolean bl = sgn1 = d >= 0.0;
                if (sgn0 != sgn1) {
                    if (M0 != -M1) {
                        ret |= 0xC;
                        break;
                    }
                    if (M0 != 1.0 && M0 != -1.0) {
                        ret |= 0xA;
                        break;
                    }
                    ret |= 8;
                    break;
                }
                if (M0 == M1) {
                    ret |= 0x4A;
                    break;
                }
                ret |= 0x4C;
                break;
            }
            case 3: {
                ret = 1;
            }
            case 2: {
                double d;
                boolean sgn1;
                double d3;
                double M0 = this.m00;
                boolean sgn0 = d3 >= 0.0;
                double M1 = this.m11;
                boolean bl = sgn1 = d >= 0.0;
                if (sgn0 == sgn1) {
                    if (sgn0) {
                        if (M0 == M1) {
                            ret |= 2;
                            break;
                        }
                        ret |= 4;
                        break;
                    }
                    if (M0 != M1) {
                        ret |= 0xC;
                        break;
                    }
                    if (M0 != -1.0) {
                        ret |= 0xA;
                        break;
                    }
                    ret |= 8;
                    break;
                }
                if (M0 == -M1) {
                    if (M0 == 1.0 || M0 == -1.0) {
                        ret |= 0x40;
                        break;
                    }
                    ret |= 0x42;
                    break;
                }
                ret |= 0x44;
                break;
            }
            case 1: {
                ret = 1;
            }
            case 0: 
        }
        this.type = ret;
    }

    public double getDeterminant() {
        switch (this.state) {
            default: {
                this.stateError();
            }
            case 6: 
            case 7: {
                return this.m00 * this.m11 - this.m01 * this.m10;
            }
            case 4: 
            case 5: {
                return -(this.m01 * this.m10);
            }
            case 2: 
            case 3: {
                return this.m00 * this.m11;
            }
            case 0: 
            case 1: 
        }
        return 1.0;
    }

    void updateState() {
        if (this.m01 == 0.0 && this.m10 == 0.0) {
            if (this.m00 == 1.0 && this.m11 == 1.0) {
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 0;
                    this.type = 0;
                } else {
                    this.state = 1;
                    this.type = 1;
                }
            } else if (this.m02 == 0.0 && this.m12 == 0.0) {
                this.state = 2;
                this.type = -1;
            } else {
                this.state = 3;
                this.type = -1;
            }
        } else if (this.m00 == 0.0 && this.m11 == 0.0) {
            if (this.m02 == 0.0 && this.m12 == 0.0) {
                this.state = 4;
                this.type = -1;
            } else {
                this.state = 5;
                this.type = -1;
            }
        } else if (this.m02 == 0.0 && this.m12 == 0.0) {
            this.state = 6;
            this.type = -1;
        } else {
            this.state = 7;
            this.type = -1;
        }
    }

    private void stateError() {
        throw new InternalError("missing case in transform state switch");
    }

    public void getMatrix(double[] flatmatrix) {
        flatmatrix[0] = this.m00;
        flatmatrix[1] = this.m10;
        flatmatrix[2] = this.m01;
        flatmatrix[3] = this.m11;
        if (flatmatrix.length > 5) {
            flatmatrix[4] = this.m02;
            flatmatrix[5] = this.m12;
        }
    }

    public double getScaleX() {
        return this.m00;
    }

    public double getScaleY() {
        return this.m11;
    }

    public double getShearX() {
        return this.m01;
    }

    public double getShearY() {
        return this.m10;
    }

    public double getTranslateX() {
        return this.m02;
    }

    public double getTranslateY() {
        return this.m12;
    }

    public void translate(double tx, double ty) {
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 7: {
                this.m02 = tx * this.m00 + ty * this.m01 + this.m02;
                this.m12 = tx * this.m10 + ty * this.m11 + this.m12;
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 6;
                    if (this.type != -1) {
                        --this.type;
                    }
                }
                return;
            }
            case 6: {
                this.m02 = tx * this.m00 + ty * this.m01;
                this.m12 = tx * this.m10 + ty * this.m11;
                if (this.m02 != 0.0 || this.m12 != 0.0) {
                    this.state = 7;
                    this.type |= 1;
                }
                return;
            }
            case 5: {
                this.m02 = ty * this.m01 + this.m02;
                this.m12 = tx * this.m10 + this.m12;
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 4;
                    if (this.type != -1) {
                        --this.type;
                    }
                }
                return;
            }
            case 4: {
                this.m02 = ty * this.m01;
                this.m12 = tx * this.m10;
                if (this.m02 != 0.0 || this.m12 != 0.0) {
                    this.state = 5;
                    this.type |= 1;
                }
                return;
            }
            case 3: {
                this.m02 = tx * this.m00 + this.m02;
                this.m12 = ty * this.m11 + this.m12;
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 2;
                    if (this.type != -1) {
                        --this.type;
                    }
                }
                return;
            }
            case 2: {
                this.m02 = tx * this.m00;
                this.m12 = ty * this.m11;
                if (this.m02 != 0.0 || this.m12 != 0.0) {
                    this.state = 3;
                    this.type |= 1;
                }
                return;
            }
            case 1: {
                this.m02 = tx + this.m02;
                this.m12 = ty + this.m12;
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 0;
                    this.type = 0;
                }
                return;
            }
            case 0: 
        }
        this.m02 = tx;
        this.m12 = ty;
        if (tx != 0.0 || ty != 0.0) {
            this.state = 1;
            this.type = 1;
        }
    }

    private void rotate90() {
        double M0 = this.m00;
        this.m00 = this.m01;
        this.m01 = -M0;
        M0 = this.m10;
        this.m10 = this.m11;
        this.m11 = -M0;
        int state = rot90conversion[this.state];
        if ((state & 6) == 2 && this.m00 == 1.0 && this.m11 == 1.0) {
            state -= 2;
        }
        this.state = state;
        this.type = -1;
    }

    private void rotate180() {
        this.m00 = -this.m00;
        this.m11 = -this.m11;
        int state = this.state;
        if ((state & 4) != 0) {
            this.m01 = -this.m01;
            this.m10 = -this.m10;
        } else {
            this.state = this.m00 == 1.0 && this.m11 == 1.0 ? state & 0xFFFFFFFD : state | 2;
        }
        this.type = -1;
    }

    private void rotate270() {
        double M0 = this.m00;
        this.m00 = -this.m01;
        this.m01 = M0;
        M0 = this.m10;
        this.m10 = -this.m11;
        this.m11 = M0;
        int state = rot90conversion[this.state];
        if ((state & 6) == 2 && this.m00 == 1.0 && this.m11 == 1.0) {
            state -= 2;
        }
        this.state = state;
        this.type = -1;
    }

    public void rotate(double theta) {
        double sin = Math.sin(theta);
        if (sin == 1.0) {
            this.rotate90();
        } else if (sin == -1.0) {
            this.rotate270();
        } else {
            double cos = Math.cos(theta);
            if (cos == -1.0) {
                this.rotate180();
            } else if (cos != 1.0) {
                double M0 = this.m00;
                double M1 = this.m01;
                this.m00 = cos * M0 + sin * M1;
                this.m01 = -sin * M0 + cos * M1;
                M0 = this.m10;
                M1 = this.m11;
                this.m10 = cos * M0 + sin * M1;
                this.m11 = -sin * M0 + cos * M1;
                this.updateState();
            }
        }
    }

    public void rotate(double theta, double anchorx, double anchory) {
        this.translate(anchorx, anchory);
        this.rotate(theta);
        this.translate(-anchorx, -anchory);
    }

    public void rotate(double vecx, double vecy) {
        if (vecy == 0.0) {
            if (vecx < 0.0) {
                this.rotate180();
            }
        } else if (vecx == 0.0) {
            if (vecy > 0.0) {
                this.rotate90();
            } else {
                this.rotate270();
            }
        } else {
            double len = Math.sqrt(vecx * vecx + vecy * vecy);
            double sin = vecy / len;
            double cos = vecx / len;
            double M0 = this.m00;
            double M1 = this.m01;
            this.m00 = cos * M0 + sin * M1;
            this.m01 = -sin * M0 + cos * M1;
            M0 = this.m10;
            M1 = this.m11;
            this.m10 = cos * M0 + sin * M1;
            this.m11 = -sin * M0 + cos * M1;
            this.updateState();
        }
    }

    public void rotate(double vecx, double vecy, double anchorx, double anchory) {
        this.translate(anchorx, anchory);
        this.rotate(vecx, vecy);
        this.translate(-anchorx, -anchory);
    }

    public void quadrantRotate(int numquadrants) {
        switch (numquadrants & 3) {
            case 0: {
                break;
            }
            case 1: {
                this.rotate90();
                break;
            }
            case 2: {
                this.rotate180();
                break;
            }
            case 3: {
                this.rotate270();
            }
        }
    }

    public void quadrantRotate(int numquadrants, double anchorx, double anchory) {
        switch (numquadrants & 3) {
            case 0: {
                return;
            }
            case 1: {
                this.m02 += anchorx * (this.m00 - this.m01) + anchory * (this.m01 + this.m00);
                this.m12 += anchorx * (this.m10 - this.m11) + anchory * (this.m11 + this.m10);
                this.rotate90();
                break;
            }
            case 2: {
                this.m02 += anchorx * (this.m00 + this.m00) + anchory * (this.m01 + this.m01);
                this.m12 += anchorx * (this.m10 + this.m10) + anchory * (this.m11 + this.m11);
                this.rotate180();
                break;
            }
            case 3: {
                this.m02 += anchorx * (this.m00 + this.m01) + anchory * (this.m01 - this.m00);
                this.m12 += anchorx * (this.m10 + this.m11) + anchory * (this.m11 - this.m10);
                this.rotate270();
            }
        }
        this.state = this.m02 == 0.0 && this.m12 == 0.0 ? (this.state &= 0xFFFFFFFE) : (this.state |= 1);
    }

    public void scale(double sx, double sy) {
        int state = this.state;
        switch (state) {
            default: {
                this.stateError();
            }
            case 6: 
            case 7: {
                this.m00 *= sx;
                this.m11 *= sy;
            }
            case 4: 
            case 5: {
                this.m01 *= sy;
                this.m10 *= sx;
                if (this.m01 == 0.0 && this.m10 == 0.0) {
                    state &= 1;
                    if (this.m00 == 1.0 && this.m11 == 1.0) {
                        this.type = state == 0 ? 0 : 1;
                    } else {
                        state |= 2;
                        this.type = -1;
                    }
                    this.state = state;
                }
                return;
            }
            case 2: 
            case 3: {
                this.m00 *= sx;
                this.m11 *= sy;
                if (this.m00 == 1.0 && this.m11 == 1.0) {
                    this.state = state &= 1;
                    this.type = state == 0 ? 0 : 1;
                } else {
                    this.type = -1;
                }
                return;
            }
            case 0: 
            case 1: 
        }
        this.m00 = sx;
        this.m11 = sy;
        if (sx != 1.0 || sy != 1.0) {
            this.state = state | 2;
            this.type = -1;
        }
    }

    public void shear(double shx, double shy) {
        int state = this.state;
        switch (state) {
            default: {
                this.stateError();
                return;
            }
            case 6: 
            case 7: {
                double M0 = this.m00;
                double M1 = this.m01;
                this.m00 = M0 + M1 * shy;
                this.m01 = M0 * shx + M1;
                M0 = this.m10;
                M1 = this.m11;
                this.m10 = M0 + M1 * shy;
                this.m11 = M0 * shx + M1;
                this.updateState();
                return;
            }
            case 4: 
            case 5: {
                this.m00 = this.m01 * shy;
                this.m11 = this.m10 * shx;
                if (this.m00 != 0.0 || this.m11 != 0.0) {
                    this.state = state | 2;
                }
                this.type = -1;
                return;
            }
            case 2: 
            case 3: {
                this.m01 = this.m00 * shx;
                this.m10 = this.m11 * shy;
                if (this.m01 != 0.0 || this.m10 != 0.0) {
                    this.state = state | 4;
                }
                this.type = -1;
                return;
            }
            case 0: 
            case 1: 
        }
        this.m01 = shx;
        this.m10 = shy;
        if (this.m01 != 0.0 || this.m10 != 0.0) {
            this.state = state | 2 | 4;
            this.type = -1;
        }
    }

    public void setToIdentity() {
        this.m11 = 1.0;
        this.m00 = 1.0;
        this.m12 = 0.0;
        this.m02 = 0.0;
        this.m01 = 0.0;
        this.m10 = 0.0;
        this.state = 0;
        this.type = 0;
    }

    public void setToTranslation(double tx, double ty) {
        this.m00 = 1.0;
        this.m10 = 0.0;
        this.m01 = 0.0;
        this.m11 = 1.0;
        this.m02 = tx;
        this.m12 = ty;
        if (tx != 0.0 || ty != 0.0) {
            this.state = 1;
            this.type = 1;
        } else {
            this.state = 0;
            this.type = 0;
        }
    }

    public void setToRotation(double theta) {
        double cos;
        double sin = Math.sin(theta);
        if (sin == 1.0 || sin == -1.0) {
            cos = 0.0;
            this.state = 4;
            this.type = 8;
        } else {
            cos = Math.cos(theta);
            if (cos == -1.0) {
                sin = 0.0;
                this.state = 2;
                this.type = 8;
            } else if (cos == 1.0) {
                sin = 0.0;
                this.state = 0;
                this.type = 0;
            } else {
                this.state = 6;
                this.type = 16;
            }
        }
        this.m00 = cos;
        this.m10 = sin;
        this.m01 = -sin;
        this.m11 = cos;
        this.m02 = 0.0;
        this.m12 = 0.0;
    }

    public void setToRotation(double theta, double anchorx, double anchory) {
        this.setToRotation(theta);
        double sin = this.m10;
        double oneMinusCos = 1.0 - this.m00;
        this.m02 = anchorx * oneMinusCos + anchory * sin;
        this.m12 = anchory * oneMinusCos - anchorx * sin;
        if (this.m02 != 0.0 || this.m12 != 0.0) {
            this.state |= 1;
            this.type |= 1;
        }
    }

    public void setToRotation(double vecx, double vecy) {
        double cos;
        double sin;
        if (vecy == 0.0) {
            sin = 0.0;
            if (vecx < 0.0) {
                cos = -1.0;
                this.state = 2;
                this.type = 8;
            } else {
                cos = 1.0;
                this.state = 0;
                this.type = 0;
            }
        } else if (vecx == 0.0) {
            cos = 0.0;
            sin = vecy > 0.0 ? 1.0 : -1.0;
            this.state = 4;
            this.type = 8;
        } else {
            double len = Math.sqrt(vecx * vecx + vecy * vecy);
            cos = vecx / len;
            sin = vecy / len;
            this.state = 6;
            this.type = 16;
        }
        this.m00 = cos;
        this.m10 = sin;
        this.m01 = -sin;
        this.m11 = cos;
        this.m02 = 0.0;
        this.m12 = 0.0;
    }

    public void setToRotation(double vecx, double vecy, double anchorx, double anchory) {
        this.setToRotation(vecx, vecy);
        double sin = this.m10;
        double oneMinusCos = 1.0 - this.m00;
        this.m02 = anchorx * oneMinusCos + anchory * sin;
        this.m12 = anchory * oneMinusCos - anchorx * sin;
        if (this.m02 != 0.0 || this.m12 != 0.0) {
            this.state |= 1;
            this.type |= 1;
        }
    }

    public void setToQuadrantRotation(int numquadrants) {
        switch (numquadrants & 3) {
            case 0: {
                this.m00 = 1.0;
                this.m10 = 0.0;
                this.m01 = 0.0;
                this.m11 = 1.0;
                this.m02 = 0.0;
                this.m12 = 0.0;
                this.state = 0;
                this.type = 0;
                break;
            }
            case 1: {
                this.m00 = 0.0;
                this.m10 = 1.0;
                this.m01 = -1.0;
                this.m11 = 0.0;
                this.m02 = 0.0;
                this.m12 = 0.0;
                this.state = 4;
                this.type = 8;
                break;
            }
            case 2: {
                this.m00 = -1.0;
                this.m10 = 0.0;
                this.m01 = 0.0;
                this.m11 = -1.0;
                this.m02 = 0.0;
                this.m12 = 0.0;
                this.state = 2;
                this.type = 8;
                break;
            }
            case 3: {
                this.m00 = 0.0;
                this.m10 = -1.0;
                this.m01 = 1.0;
                this.m11 = 0.0;
                this.m02 = 0.0;
                this.m12 = 0.0;
                this.state = 4;
                this.type = 8;
            }
        }
    }

    public void setToQuadrantRotation(int numquadrants, double anchorx, double anchory) {
        switch (numquadrants & 3) {
            case 0: {
                this.m00 = 1.0;
                this.m10 = 0.0;
                this.m01 = 0.0;
                this.m11 = 1.0;
                this.m02 = 0.0;
                this.m12 = 0.0;
                this.state = 0;
                this.type = 0;
                break;
            }
            case 1: {
                this.m00 = 0.0;
                this.m10 = 1.0;
                this.m01 = -1.0;
                this.m11 = 0.0;
                this.m02 = anchorx + anchory;
                this.m12 = anchory - anchorx;
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 4;
                    this.type = 8;
                    break;
                }
                this.state = 5;
                this.type = 9;
                break;
            }
            case 2: {
                this.m00 = -1.0;
                this.m10 = 0.0;
                this.m01 = 0.0;
                this.m11 = -1.0;
                this.m02 = anchorx + anchorx;
                this.m12 = anchory + anchory;
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 2;
                    this.type = 8;
                    break;
                }
                this.state = 3;
                this.type = 9;
                break;
            }
            case 3: {
                this.m00 = 0.0;
                this.m10 = -1.0;
                this.m01 = 1.0;
                this.m11 = 0.0;
                this.m02 = anchorx - anchory;
                this.m12 = anchory + anchorx;
                if (this.m02 == 0.0 && this.m12 == 0.0) {
                    this.state = 4;
                    this.type = 8;
                    break;
                }
                this.state = 5;
                this.type = 9;
            }
        }
    }

    public void setToScale(double sx, double sy) {
        this.m00 = sx;
        this.m10 = 0.0;
        this.m01 = 0.0;
        this.m11 = sy;
        this.m02 = 0.0;
        this.m12 = 0.0;
        if (sx != 1.0 || sy != 1.0) {
            this.state = 2;
            this.type = -1;
        } else {
            this.state = 0;
            this.type = 0;
        }
    }

    public void setToShear(double shx, double shy) {
        this.m00 = 1.0;
        this.m01 = shx;
        this.m10 = shy;
        this.m11 = 1.0;
        this.m02 = 0.0;
        this.m12 = 0.0;
        if (shx != 0.0 || shy != 0.0) {
            this.state = 6;
            this.type = -1;
        } else {
            this.state = 0;
            this.type = 0;
        }
    }

    public void setTransform(AffineTransform Tx) {
        this.m00 = Tx.m00;
        this.m10 = Tx.m10;
        this.m01 = Tx.m01;
        this.m11 = Tx.m11;
        this.m02 = Tx.m02;
        this.m12 = Tx.m12;
        this.state = Tx.state;
        this.type = Tx.type;
    }

    public void setTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.updateState();
    }

    public void concatenate(AffineTransform Tx) {
        int mystate = this.state;
        int txstate = Tx.state;
        switch (txstate << 3 | mystate) {
            case 0: 
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: {
                return;
            }
            case 56: {
                this.m01 = Tx.m01;
                this.m10 = Tx.m10;
            }
            case 24: {
                this.m00 = Tx.m00;
                this.m11 = Tx.m11;
            }
            case 8: {
                this.m02 = Tx.m02;
                this.m12 = Tx.m12;
                this.state = txstate;
                this.type = Tx.type;
                return;
            }
            case 48: {
                this.m01 = Tx.m01;
                this.m10 = Tx.m10;
            }
            case 16: {
                this.m00 = Tx.m00;
                this.m11 = Tx.m11;
                this.state = txstate;
                this.type = Tx.type;
                return;
            }
            case 40: {
                this.m02 = Tx.m02;
                this.m12 = Tx.m12;
            }
            case 32: {
                this.m01 = Tx.m01;
                this.m10 = Tx.m10;
                this.m11 = 0.0;
                this.m00 = 0.0;
                this.state = txstate;
                this.type = Tx.type;
                return;
            }
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 15: {
                this.translate(Tx.m02, Tx.m12);
                return;
            }
            case 17: 
            case 18: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: {
                this.scale(Tx.m00, Tx.m11);
                return;
            }
            case 38: 
            case 39: {
                double T01 = Tx.m01;
                double T10 = Tx.m10;
                double M0 = this.m00;
                this.m00 = this.m01 * T10;
                this.m01 = M0 * T01;
                M0 = this.m10;
                this.m10 = this.m11 * T10;
                this.m11 = M0 * T01;
                this.type = -1;
                return;
            }
            case 36: 
            case 37: {
                this.m00 = this.m01 * Tx.m10;
                this.m01 = 0.0;
                this.m11 = this.m10 * Tx.m01;
                this.m10 = 0.0;
                this.state = mystate ^ 6;
                this.type = -1;
                return;
            }
            case 34: 
            case 35: {
                this.m01 = this.m00 * Tx.m01;
                this.m00 = 0.0;
                this.m10 = this.m11 * Tx.m10;
                this.m11 = 0.0;
                this.state = mystate ^ 6;
                this.type = -1;
                return;
            }
            case 33: {
                this.m00 = 0.0;
                this.m01 = Tx.m01;
                this.m10 = Tx.m10;
                this.m11 = 0.0;
                this.state = 5;
                this.type = -1;
                return;
            }
        }
        double T00 = Tx.m00;
        double T01 = Tx.m01;
        double T02 = Tx.m02;
        double T10 = Tx.m10;
        double T11 = Tx.m11;
        double T12 = Tx.m12;
        switch (mystate) {
            default: {
                this.stateError();
            }
            case 6: {
                this.state = mystate | txstate;
            }
            case 7: {
                double M0 = this.m00;
                double M1 = this.m01;
                this.m00 = T00 * M0 + T10 * M1;
                this.m01 = T01 * M0 + T11 * M1;
                this.m02 += T02 * M0 + T12 * M1;
                M0 = this.m10;
                M1 = this.m11;
                this.m10 = T00 * M0 + T10 * M1;
                this.m11 = T01 * M0 + T11 * M1;
                this.m12 += T02 * M0 + T12 * M1;
                this.type = -1;
                return;
            }
            case 4: 
            case 5: {
                double M0 = this.m01;
                this.m00 = T10 * M0;
                this.m01 = T11 * M0;
                this.m02 += T12 * M0;
                M0 = this.m10;
                this.m10 = T00 * M0;
                this.m11 = T01 * M0;
                this.m12 += T02 * M0;
                break;
            }
            case 2: 
            case 3: {
                double M0 = this.m00;
                this.m00 = T00 * M0;
                this.m01 = T01 * M0;
                this.m02 += T02 * M0;
                M0 = this.m11;
                this.m10 = T10 * M0;
                this.m11 = T11 * M0;
                this.m12 += T12 * M0;
                break;
            }
            case 1: {
                this.m00 = T00;
                this.m01 = T01;
                this.m02 += T02;
                this.m10 = T10;
                this.m11 = T11;
                this.m12 += T12;
                this.state = txstate | 1;
                this.type = -1;
                return;
            }
        }
        this.updateState();
    }

    public void preConcatenate(AffineTransform Tx) {
        int mystate = this.state;
        int txstate = Tx.state;
        switch (txstate << 3 | mystate) {
            case 0: 
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: {
                return;
            }
            case 8: 
            case 10: 
            case 12: 
            case 14: {
                this.m02 = Tx.m02;
                this.m12 = Tx.m12;
                this.state = mystate | 1;
                this.type |= 1;
                return;
            }
            case 9: 
            case 11: 
            case 13: 
            case 15: {
                this.m02 += Tx.m02;
                this.m12 += Tx.m12;
                return;
            }
            case 16: 
            case 17: {
                this.state = mystate | 2;
            }
            case 18: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: {
                double T00 = Tx.m00;
                double T11 = Tx.m11;
                if ((mystate & 4) != 0) {
                    this.m01 *= T00;
                    this.m10 *= T11;
                    if ((mystate & 2) != 0) {
                        this.m00 *= T00;
                        this.m11 *= T11;
                    }
                } else {
                    this.m00 *= T00;
                    this.m11 *= T11;
                }
                if ((mystate & 1) != 0) {
                    this.m02 *= T00;
                    this.m12 *= T11;
                }
                this.type = -1;
                return;
            }
            case 36: 
            case 37: {
                mystate |= 2;
            }
            case 32: 
            case 33: 
            case 34: 
            case 35: {
                this.state = mystate ^ 4;
            }
            case 38: 
            case 39: {
                double T01 = Tx.m01;
                double T10 = Tx.m10;
                double M0 = this.m00;
                this.m00 = this.m10 * T01;
                this.m10 = M0 * T10;
                M0 = this.m01;
                this.m01 = this.m11 * T01;
                this.m11 = M0 * T10;
                M0 = this.m02;
                this.m02 = this.m12 * T01;
                this.m12 = M0 * T10;
                this.type = -1;
                return;
            }
        }
        double T00 = Tx.m00;
        double T01 = Tx.m01;
        double T02 = Tx.m02;
        double T10 = Tx.m10;
        double T11 = Tx.m11;
        double T12 = Tx.m12;
        switch (mystate) {
            default: {
                this.stateError();
            }
            case 7: {
                double M0 = this.m02;
                double M1 = this.m12;
                T02 += M0 * T00 + M1 * T01;
                T12 += M0 * T10 + M1 * T11;
            }
            case 6: {
                this.m02 = T02;
                this.m12 = T12;
                double M0 = this.m00;
                double M1 = this.m10;
                this.m00 = M0 * T00 + M1 * T01;
                this.m10 = M0 * T10 + M1 * T11;
                M0 = this.m01;
                M1 = this.m11;
                this.m01 = M0 * T00 + M1 * T01;
                this.m11 = M0 * T10 + M1 * T11;
                break;
            }
            case 5: {
                double M0 = this.m02;
                double M1 = this.m12;
                T02 += M0 * T00 + M1 * T01;
                T12 += M0 * T10 + M1 * T11;
            }
            case 4: {
                this.m02 = T02;
                this.m12 = T12;
                double M0 = this.m10;
                this.m00 = M0 * T01;
                this.m10 = M0 * T11;
                M0 = this.m01;
                this.m01 = M0 * T00;
                this.m11 = M0 * T10;
                break;
            }
            case 3: {
                double M0 = this.m02;
                double M1 = this.m12;
                T02 += M0 * T00 + M1 * T01;
                T12 += M0 * T10 + M1 * T11;
            }
            case 2: {
                this.m02 = T02;
                this.m12 = T12;
                double M0 = this.m00;
                this.m00 = M0 * T00;
                this.m10 = M0 * T10;
                M0 = this.m11;
                this.m01 = M0 * T01;
                this.m11 = M0 * T11;
                break;
            }
            case 1: {
                double M0 = this.m02;
                double M1 = this.m12;
                T02 += M0 * T00 + M1 * T01;
                T12 += M0 * T10 + M1 * T11;
            }
            case 0: {
                this.m02 = T02;
                this.m12 = T12;
                this.m00 = T00;
                this.m10 = T10;
                this.m01 = T01;
                this.m11 = T11;
                this.state = mystate | txstate;
                this.type = -1;
                return;
            }
        }
        this.updateState();
    }

    public AffineTransform createInverse() throws NoninvertibleTransformException {
        switch (this.state) {
            default: {
                this.stateError();
                return null;
            }
            case 7: {
                double det = this.m00 * this.m11 - this.m01 * this.m10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new NoninvertibleTransformException("Determinant is " + det);
                }
                return new AffineTransform(this.m11 / det, -this.m10 / det, -this.m01 / det, this.m00 / det, (this.m01 * this.m12 - this.m11 * this.m02) / det, (this.m10 * this.m02 - this.m00 * this.m12) / det, 7);
            }
            case 6: {
                double det = this.m00 * this.m11 - this.m01 * this.m10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new NoninvertibleTransformException("Determinant is " + det);
                }
                return new AffineTransform(this.m11 / det, -this.m10 / det, -this.m01 / det, this.m00 / det, 0.0, 0.0, 6);
            }
            case 5: {
                if (this.m01 == 0.0 || this.m10 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                return new AffineTransform(0.0, 1.0 / this.m01, 1.0 / this.m10, 0.0, -this.m12 / this.m10, -this.m02 / this.m01, 5);
            }
            case 4: {
                if (this.m01 == 0.0 || this.m10 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                return new AffineTransform(0.0, 1.0 / this.m01, 1.0 / this.m10, 0.0, 0.0, 0.0, 4);
            }
            case 3: {
                if (this.m00 == 0.0 || this.m11 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                return new AffineTransform(1.0 / this.m00, 0.0, 0.0, 1.0 / this.m11, -this.m02 / this.m00, -this.m12 / this.m11, 3);
            }
            case 2: {
                if (this.m00 == 0.0 || this.m11 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                return new AffineTransform(1.0 / this.m00, 0.0, 0.0, 1.0 / this.m11, 0.0, 0.0, 2);
            }
            case 1: {
                return new AffineTransform(1.0, 0.0, 0.0, 1.0, -this.m02, -this.m12, 1);
            }
            case 0: 
        }
        return new AffineTransform();
    }

    public void invert() throws NoninvertibleTransformException {
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 7: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M11 = this.m11;
                double M12 = this.m12;
                double det = M00 * M11 - M01 * M10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new NoninvertibleTransformException("Determinant is " + det);
                }
                this.m00 = M11 / det;
                this.m10 = -M10 / det;
                this.m01 = -M01 / det;
                this.m11 = M00 / det;
                this.m02 = (M01 * M12 - M11 * M02) / det;
                this.m12 = (M10 * M02 - M00 * M12) / det;
                break;
            }
            case 6: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M10 = this.m10;
                double M11 = this.m11;
                double det = M00 * M11 - M01 * M10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new NoninvertibleTransformException("Determinant is " + det);
                }
                this.m00 = M11 / det;
                this.m10 = -M10 / det;
                this.m01 = -M01 / det;
                this.m11 = M00 / det;
                break;
            }
            case 5: {
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M12 = this.m12;
                if (M01 == 0.0 || M10 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                this.m10 = 1.0 / M01;
                this.m01 = 1.0 / M10;
                this.m02 = -M12 / M10;
                this.m12 = -M02 / M01;
                break;
            }
            case 4: {
                double M01 = this.m01;
                double M10 = this.m10;
                if (M01 == 0.0 || M10 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                this.m10 = 1.0 / M01;
                this.m01 = 1.0 / M10;
                break;
            }
            case 3: {
                double M00 = this.m00;
                double M02 = this.m02;
                double M11 = this.m11;
                double M12 = this.m12;
                if (M00 == 0.0 || M11 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                this.m00 = 1.0 / M00;
                this.m11 = 1.0 / M11;
                this.m02 = -M02 / M00;
                this.m12 = -M12 / M11;
                break;
            }
            case 2: {
                double M00 = this.m00;
                double M11 = this.m11;
                if (M00 == 0.0 || M11 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                this.m00 = 1.0 / M00;
                this.m11 = 1.0 / M11;
                break;
            }
            case 1: {
                this.m02 = -this.m02;
                this.m12 = -this.m12;
            }
            case 0: 
        }
    }

    public Point2D transform(Point2D ptSrc, Point2D ptDst) {
        if (ptDst == null) {
            ptDst = ptSrc instanceof Point2D.Double ? new Point2D.Double() : new Point2D.Float();
        }
        double x = ptSrc.getX();
        double y = ptSrc.getY();
        switch (this.state) {
            default: {
                this.stateError();
                return null;
            }
            case 7: {
                ptDst.setLocation(x * this.m00 + y * this.m01 + this.m02, x * this.m10 + y * this.m11 + this.m12);
                return ptDst;
            }
            case 6: {
                ptDst.setLocation(x * this.m00 + y * this.m01, x * this.m10 + y * this.m11);
                return ptDst;
            }
            case 5: {
                ptDst.setLocation(y * this.m01 + this.m02, x * this.m10 + this.m12);
                return ptDst;
            }
            case 4: {
                ptDst.setLocation(y * this.m01, x * this.m10);
                return ptDst;
            }
            case 3: {
                ptDst.setLocation(x * this.m00 + this.m02, y * this.m11 + this.m12);
                return ptDst;
            }
            case 2: {
                ptDst.setLocation(x * this.m00, y * this.m11);
                return ptDst;
            }
            case 1: {
                ptDst.setLocation(x + this.m02, y + this.m12);
                return ptDst;
            }
            case 0: 
        }
        ptDst.setLocation(x, y);
        return ptDst;
    }

    public void transform(Point2D[] ptSrc, int srcOff, Point2D[] ptDst, int dstOff, int numPts) {
        int state = this.state;
        block10: while (--numPts >= 0) {
            Point2D dst;
            Point2D src = ptSrc[srcOff++];
            double x = src.getX();
            double y = src.getY();
            if ((dst = ptDst[dstOff++]) == null) {
                dst = src instanceof Point2D.Double ? new Point2D.Double() : new Point2D.Float();
                ptDst[dstOff - 1] = dst;
            }
            switch (state) {
                default: {
                    this.stateError();
                    return;
                }
                case 7: {
                    dst.setLocation(x * this.m00 + y * this.m01 + this.m02, x * this.m10 + y * this.m11 + this.m12);
                    continue block10;
                }
                case 6: {
                    dst.setLocation(x * this.m00 + y * this.m01, x * this.m10 + y * this.m11);
                    continue block10;
                }
                case 5: {
                    dst.setLocation(y * this.m01 + this.m02, x * this.m10 + this.m12);
                    continue block10;
                }
                case 4: {
                    dst.setLocation(y * this.m01, x * this.m10);
                    continue block10;
                }
                case 3: {
                    dst.setLocation(x * this.m00 + this.m02, y * this.m11 + this.m12);
                    continue block10;
                }
                case 2: {
                    dst.setLocation(x * this.m00, y * this.m11);
                    continue block10;
                }
                case 1: {
                    dst.setLocation(x + this.m02, y + this.m12);
                    continue block10;
                }
                case 0: 
            }
            dst.setLocation(x, y);
        }
    }

    public void transform(float[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
        if (dstPts == srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            srcOff = dstOff;
        }
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 7: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M00 * x + M01 * y + M02);
                    dstPts[dstOff++] = (float)(M10 * x + M11 * y + M12);
                }
                return;
            }
            case 6: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M10 = this.m10;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M00 * x + M01 * y);
                    dstPts[dstOff++] = (float)(M10 * x + M11 * y);
                }
                return;
            }
            case 5: {
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M01 * (double)srcPts[srcOff++] + M02);
                    dstPts[dstOff++] = (float)(M10 * x + M12);
                }
                return;
            }
            case 4: {
                double M01 = this.m01;
                double M10 = this.m10;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M01 * (double)srcPts[srcOff++]);
                    dstPts[dstOff++] = (float)(M10 * x);
                }
                return;
            }
            case 3: {
                double M00 = this.m00;
                double M02 = this.m02;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (float)(M00 * (double)srcPts[srcOff++] + M02);
                    dstPts[dstOff++] = (float)(M11 * (double)srcPts[srcOff++] + M12);
                }
                return;
            }
            case 2: {
                double M00 = this.m00;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (float)(M00 * (double)srcPts[srcOff++]);
                    dstPts[dstOff++] = (float)(M11 * (double)srcPts[srcOff++]);
                }
                return;
            }
            case 1: {
                double M02 = this.m02;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (float)((double)srcPts[srcOff++] + M02);
                    dstPts[dstOff++] = (float)((double)srcPts[srcOff++] + M12);
                }
                return;
            }
            case 0: 
        }
        if (srcPts != dstPts || srcOff != dstOff) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
        }
    }

    public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        if (dstPts == srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            srcOff = dstOff;
        }
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 7: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y + M02;
                    dstPts[dstOff++] = M10 * x + M11 * y + M12;
                }
                return;
            }
            case 6: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M10 = this.m10;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y;
                    dstPts[dstOff++] = M10 * x + M11 * y;
                }
                return;
            }
            case 5: {
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M10 * x + M12;
                }
                return;
            }
            case 4: {
                double M01 = this.m01;
                double M10 = this.m10;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * srcPts[srcOff++];
                    dstPts[dstOff++] = M10 * x;
                }
                return;
            }
            case 3: {
                double M00 = this.m00;
                double M02 = this.m02;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M11 * srcPts[srcOff++] + M12;
                }
                return;
            }
            case 2: {
                double M00 = this.m00;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++];
                    dstPts[dstOff++] = M11 * srcPts[srcOff++];
                }
                return;
            }
            case 1: {
                double M02 = this.m02;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = srcPts[srcOff++] + M12;
                }
                return;
            }
            case 0: 
        }
        if (srcPts != dstPts || srcOff != dstOff) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
        }
    }

    public void transform(float[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 7: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y + M02;
                    dstPts[dstOff++] = M10 * x + M11 * y + M12;
                }
                return;
            }
            case 6: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M10 = this.m10;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y;
                    dstPts[dstOff++] = M10 * x + M11 * y;
                }
                return;
            }
            case 5: {
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * (double)srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M10 * x + M12;
                }
                return;
            }
            case 4: {
                double M01 = this.m01;
                double M10 = this.m10;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * (double)srcPts[srcOff++];
                    dstPts[dstOff++] = M10 * x;
                }
                return;
            }
            case 3: {
                double M00 = this.m00;
                double M02 = this.m02;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * (double)srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M11 * (double)srcPts[srcOff++] + M12;
                }
                return;
            }
            case 2: {
                double M00 = this.m00;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * (double)srcPts[srcOff++];
                    dstPts[dstOff++] = M11 * (double)srcPts[srcOff++];
                }
                return;
            }
            case 1: {
                double M02 = this.m02;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (double)srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = (double)srcPts[srcOff++] + M12;
                }
                return;
            }
            case 0: 
        }
        while (--numPts >= 0) {
            dstPts[dstOff++] = srcPts[srcOff++];
            dstPts[dstOff++] = srcPts[srcOff++];
        }
    }

    public void transform(double[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 7: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M00 * x + M01 * y + M02);
                    dstPts[dstOff++] = (float)(M10 * x + M11 * y + M12);
                }
                return;
            }
            case 6: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M10 = this.m10;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M00 * x + M01 * y);
                    dstPts[dstOff++] = (float)(M10 * x + M11 * y);
                }
                return;
            }
            case 5: {
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M01 * srcPts[srcOff++] + M02);
                    dstPts[dstOff++] = (float)(M10 * x + M12);
                }
                return;
            }
            case 4: {
                double M01 = this.m01;
                double M10 = this.m10;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = (float)(M01 * srcPts[srcOff++]);
                    dstPts[dstOff++] = (float)(M10 * x);
                }
                return;
            }
            case 3: {
                double M00 = this.m00;
                double M02 = this.m02;
                double M11 = this.m11;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (float)(M00 * srcPts[srcOff++] + M02);
                    dstPts[dstOff++] = (float)(M11 * srcPts[srcOff++] + M12);
                }
                return;
            }
            case 2: {
                double M00 = this.m00;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (float)(M00 * srcPts[srcOff++]);
                    dstPts[dstOff++] = (float)(M11 * srcPts[srcOff++]);
                }
                return;
            }
            case 1: {
                double M02 = this.m02;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (float)(srcPts[srcOff++] + M02);
                    dstPts[dstOff++] = (float)(srcPts[srcOff++] + M12);
                }
                return;
            }
            case 0: 
        }
        while (--numPts >= 0) {
            dstPts[dstOff++] = (float)srcPts[srcOff++];
            dstPts[dstOff++] = (float)srcPts[srcOff++];
        }
    }

    public Point2D inverseTransform(Point2D ptSrc, Point2D ptDst) throws NoninvertibleTransformException {
        if (ptDst == null) {
            ptDst = ptSrc instanceof Point2D.Double ? new Point2D.Double() : new Point2D.Float();
        }
        double x = ptSrc.getX();
        double y = ptSrc.getY();
        switch (this.state) {
            default: {
                this.stateError();
            }
            case 7: {
                x -= this.m02;
                y -= this.m12;
            }
            case 6: {
                double det = this.m00 * this.m11 - this.m01 * this.m10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new NoninvertibleTransformException("Determinant is " + det);
                }
                ptDst.setLocation((x * this.m11 - y * this.m01) / det, (y * this.m00 - x * this.m10) / det);
                return ptDst;
            }
            case 5: {
                x -= this.m02;
                y -= this.m12;
            }
            case 4: {
                if (this.m01 == 0.0 || this.m10 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                ptDst.setLocation(y / this.m10, x / this.m01);
                return ptDst;
            }
            case 3: {
                x -= this.m02;
                y -= this.m12;
            }
            case 2: {
                if (this.m00 == 0.0 || this.m11 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                ptDst.setLocation(x / this.m00, y / this.m11);
                return ptDst;
            }
            case 1: {
                ptDst.setLocation(x - this.m02, y - this.m12);
                return ptDst;
            }
            case 0: 
        }
        ptDst.setLocation(x, y);
        return ptDst;
    }

    public void inverseTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) throws NoninvertibleTransformException {
        if (dstPts == srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            srcOff = dstOff;
        }
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 7: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M11 = this.m11;
                double M12 = this.m12;
                double det = M00 * M11 - M01 * M10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new NoninvertibleTransformException("Determinant is " + det);
                }
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++] - M02;
                    double y = srcPts[srcOff++] - M12;
                    dstPts[dstOff++] = (x * M11 - y * M01) / det;
                    dstPts[dstOff++] = (y * M00 - x * M10) / det;
                }
                return;
            }
            case 6: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M10 = this.m10;
                double M11 = this.m11;
                double det = M00 * M11 - M01 * M10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new NoninvertibleTransformException("Determinant is " + det);
                }
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = (x * M11 - y * M01) / det;
                    dstPts[dstOff++] = (y * M00 - x * M10) / det;
                }
                return;
            }
            case 5: {
                double M01 = this.m01;
                double M02 = this.m02;
                double M10 = this.m10;
                double M12 = this.m12;
                if (M01 == 0.0 || M10 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++] - M02;
                    dstPts[dstOff++] = (srcPts[srcOff++] - M12) / M10;
                    dstPts[dstOff++] = x / M01;
                }
                return;
            }
            case 4: {
                double M01 = this.m01;
                double M10 = this.m10;
                if (M01 == 0.0 || M10 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = srcPts[srcOff++] / M10;
                    dstPts[dstOff++] = x / M01;
                }
                return;
            }
            case 3: {
                double M00 = this.m00;
                double M02 = this.m02;
                double M11 = this.m11;
                double M12 = this.m12;
                if (M00 == 0.0 || M11 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (srcPts[srcOff++] - M02) / M00;
                    dstPts[dstOff++] = (srcPts[srcOff++] - M12) / M11;
                }
                return;
            }
            case 2: {
                double M00 = this.m00;
                double M11 = this.m11;
                if (M00 == 0.0 || M11 == 0.0) {
                    throw new NoninvertibleTransformException("Determinant is 0");
                }
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] / M00;
                    dstPts[dstOff++] = srcPts[srcOff++] / M11;
                }
                return;
            }
            case 1: {
                double M02 = this.m02;
                double M12 = this.m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] - M02;
                    dstPts[dstOff++] = srcPts[srcOff++] - M12;
                }
                return;
            }
            case 0: 
        }
        if (srcPts != dstPts || srcOff != dstOff) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
        }
    }

    public Point2D deltaTransform(Point2D ptSrc, Point2D ptDst) {
        if (ptDst == null) {
            ptDst = ptSrc instanceof Point2D.Double ? new Point2D.Double() : new Point2D.Float();
        }
        double x = ptSrc.getX();
        double y = ptSrc.getY();
        switch (this.state) {
            default: {
                this.stateError();
                return null;
            }
            case 6: 
            case 7: {
                ptDst.setLocation(x * this.m00 + y * this.m01, x * this.m10 + y * this.m11);
                return ptDst;
            }
            case 4: 
            case 5: {
                ptDst.setLocation(y * this.m01, x * this.m10);
                return ptDst;
            }
            case 2: 
            case 3: {
                ptDst.setLocation(x * this.m00, y * this.m11);
                return ptDst;
            }
            case 0: 
            case 1: 
        }
        ptDst.setLocation(x, y);
        return ptDst;
    }

    public void deltaTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        if (dstPts == srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            srcOff = dstOff;
        }
        switch (this.state) {
            default: {
                this.stateError();
                return;
            }
            case 6: 
            case 7: {
                double M00 = this.m00;
                double M01 = this.m01;
                double M10 = this.m10;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = x * M00 + y * M01;
                    dstPts[dstOff++] = x * M10 + y * M11;
                }
                return;
            }
            case 4: 
            case 5: {
                double M01 = this.m01;
                double M10 = this.m10;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = srcPts[srcOff++] * M01;
                    dstPts[dstOff++] = x * M10;
                }
                return;
            }
            case 2: 
            case 3: {
                double M00 = this.m00;
                double M11 = this.m11;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] * M00;
                    dstPts[dstOff++] = srcPts[srcOff++] * M11;
                }
                return;
            }
            case 0: 
            case 1: 
        }
        if (srcPts != dstPts || srcOff != dstOff) {
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
        }
    }

    public Shape createTransformedShape(Shape pSrc) {
        if (pSrc == null) {
            return null;
        }
        return new Path2D.Double(pSrc, this);
    }

    private static double _matround(double matval) {
        return Math.rint(matval * 1.0E15) / 1.0E15;
    }

    public String toString() {
        return "AffineTransform[[" + AffineTransform._matround(this.m00) + ", " + AffineTransform._matround(this.m01) + ", " + AffineTransform._matround(this.m02) + "], [" + AffineTransform._matround(this.m10) + ", " + AffineTransform._matround(this.m11) + ", " + AffineTransform._matround(this.m12) + "]]";
    }

    public boolean isIdentity() {
        return this.state == 0 || this.getType() == 0;
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public int hashCode() {
        long bits = AffineTransform.hash(this.m00);
        bits = bits * 31L + AffineTransform.hash(this.m01);
        bits = bits * 31L + AffineTransform.hash(this.m02);
        bits = bits * 31L + AffineTransform.hash(this.m10);
        bits = bits * 31L + AffineTransform.hash(this.m11);
        bits = bits * 31L + AffineTransform.hash(this.m12);
        return (int)bits ^ (int)(bits >> 32);
    }

    private static long hash(double m) {
        long h = Double.doubleToLongBits(m);
        if (h == Long.MIN_VALUE) {
            h = 0L;
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AffineTransform)) {
            return false;
        }
        AffineTransform a = (AffineTransform)obj;
        return AffineTransform.equals(this.m00, a.m00) && AffineTransform.equals(this.m01, a.m01) && AffineTransform.equals(this.m02, a.m02) && AffineTransform.equals(this.m10, a.m10) && AffineTransform.equals(this.m11, a.m11) && AffineTransform.equals(this.m12, a.m12);
    }

    private static boolean equals(double a, double b) {
        return a == b || Double.isNaN(a) && Double.isNaN(b);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        this.updateState();
    }
}

