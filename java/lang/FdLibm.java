/*
 * Decompiled with CFR 0.152.
 */
package java.lang;

final class FdLibm {
    private static final double INFINITY = Double.POSITIVE_INFINITY;
    private static final double TWO24 = 1.6777216E7;
    private static final double TWO54 = 1.8014398509481984E16;
    private static final double HUGE = 1.0E300;
    private static final int SIGN_BIT = Integer.MIN_VALUE;
    private static final int EXP_BITS = 0x7FF00000;
    private static final int EXP_SIGNIF_BITS = Integer.MAX_VALUE;

    private FdLibm() {
        throw new UnsupportedOperationException("No FdLibm instances for you.");
    }

    private static int __LO(double x) {
        long transducer = Double.doubleToRawLongBits(x);
        return (int)transducer;
    }

    private static double __LO(double x, int low) {
        long transX = Double.doubleToRawLongBits(x);
        return Double.longBitsToDouble(transX & 0xFFFFFFFF00000000L | (long)low & 0xFFFFFFFFL);
    }

    private static int __HI(double x) {
        long transducer = Double.doubleToRawLongBits(x);
        return (int)(transducer >> 32);
    }

    private static double __HI(double x, int high) {
        long transX = Double.doubleToRawLongBits(x);
        return Double.longBitsToDouble(transX & 0xFFFFFFFFL | (long)high << 32);
    }

    private static double __HI_LO(int high, int low) {
        return Double.longBitsToDouble((long)high << 32 | (long)low & 0xFFFFFFFFL);
    }

    static final class IEEEremainder {
        private IEEEremainder() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x, double p) {
            int hx = FdLibm.__HI(x);
            int lx = FdLibm.__LO(x);
            int hp = FdLibm.__HI(p);
            int lp = FdLibm.__LO(p);
            int sx = hx & Integer.MIN_VALUE;
            hx &= Integer.MAX_VALUE;
            if (((hp &= Integer.MAX_VALUE) | lp) == 0) {
                return x * p / (x * p);
            }
            if (hx >= 0x7FF00000 || hp >= 0x7FF00000 && (hp - 0x7FF00000 | lp) != 0) {
                return x * p / (x * p);
            }
            if (hp <= 0x7FDFFFFF) {
                x = IEEEremainder.__ieee754_fmod(x, p + p);
            }
            if ((hx - hp | lx - lp) == 0) {
                return 0.0 * x;
            }
            x = Math.abs(x);
            p = Math.abs(p);
            if (hp < 0x200000) {
                if (x + x > p && (x -= p) + x >= p) {
                    x -= p;
                }
            } else {
                double p_half = 0.5 * p;
                if (x > p_half && (x -= p) >= p_half) {
                    x -= p;
                }
            }
            return FdLibm.__HI(x, FdLibm.__HI(x) ^ sx);
        }

        private static double __ieee754_fmod(double x, double y) {
            int lz;
            int hz;
            int n;
            int hx = FdLibm.__HI(x);
            int lx = FdLibm.__LO(x);
            int hy = FdLibm.__HI(y);
            int ly = FdLibm.__LO(y);
            int sx = hx & Integer.MIN_VALUE;
            if (((hy &= Integer.MAX_VALUE) | ly) == 0 || (hx ^= sx) >= 0x7FF00000 || (hy | (ly | -ly) >>> 31) > 0x7FF00000) {
                return x * y / (x * y);
            }
            if (hx <= hy) {
                if (hx < hy || Integer.compareUnsigned(lx, ly) < 0) {
                    return x;
                }
                if (lx == ly) {
                    return IEEEremainder.signedZero(sx);
                }
            }
            int ix = IEEEremainder.ilogb(hx, lx);
            int iy = IEEEremainder.ilogb(hy, ly);
            if (ix >= -1022) {
                hx = 0x100000 | 0xFFFFF & hx;
            } else {
                n = -1022 - ix;
                if (n <= 31) {
                    hx = hx << n | lx >>> 32 - n;
                    lx <<= n;
                } else {
                    hx = lx << n - 32;
                    lx = 0;
                }
            }
            if (iy >= -1022) {
                hy = 0x100000 | 0xFFFFF & hy;
            } else {
                n = -1022 - iy;
                if (n <= 31) {
                    hy = hy << n | ly >>> 32 - n;
                    ly <<= n;
                } else {
                    hy = ly << n - 32;
                    ly = 0;
                }
            }
            n = ix - iy;
            while (n-- != 0) {
                hz = hx - hy;
                lz = lx - ly;
                if (Integer.compareUnsigned(lx, ly) < 0) {
                    --hz;
                }
                if (hz < 0) {
                    hx = hx + hx + (lx >>> 31);
                    lx += lx;
                    continue;
                }
                if ((hz | lz) == 0) {
                    return IEEEremainder.signedZero(sx);
                }
                hx = hz + hz + (lz >>> 31);
                lx = lz + lz;
            }
            hz = hx - hy;
            lz = lx - ly;
            if (Integer.compareUnsigned(lx, ly) < 0) {
                --hz;
            }
            if (hz >= 0) {
                hx = hz;
                lx = lz;
            }
            if ((hx | lx) == 0) {
                return IEEEremainder.signedZero(sx);
            }
            while (hx < 0x100000) {
                hx = hx + hx + (lx >>> 31);
                lx += lx;
                --iy;
            }
            if (iy >= -1022) {
                hx = hx - 0x100000 | iy + 1023 << 20;
                x = FdLibm.__HI_LO(hx | sx, lx);
            } else {
                n = -1022 - iy;
                if (n <= 20) {
                    lx = lx >>> n | hx << 32 - n;
                    hx >>= n;
                } else if (n <= 31) {
                    lx = hx << 32 - n | lx >>> n;
                    hx = sx;
                } else {
                    lx = hx >> n - 32;
                    hx = sx;
                }
                x = FdLibm.__HI_LO(hx | sx, lx);
                x *= 1.0;
            }
            return x;
        }

        private static double signedZero(int sign) {
            return 0.0 * (double)sign;
        }

        private static int ilogb(int hz, int lz) {
            int iz;
            if (hz < 0x100000) {
                if (hz == 0) {
                    iz = -1043;
                    for (int i = lz; i > 0; i <<= 1) {
                        --iz;
                    }
                } else {
                    iz = -1022;
                    for (int i = hz << 11; i > 0; i <<= 1) {
                        --iz;
                    }
                }
            } else {
                iz = (hz >> 20) - 1023;
            }
            return iz;
        }
    }

    static final class Tanh {
        private static final double tiny = 1.0E-300;

        private Tanh() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            double z;
            int jx = FdLibm.__HI(x);
            int ix = jx & Integer.MAX_VALUE;
            if (ix >= 0x7FF00000) {
                if (jx >= 0) {
                    return 1.0 / x + 1.0;
                }
                return 1.0 / x - 1.0;
            }
            if (ix < 1077280768) {
                if (ix < 1015021568) {
                    return x * (1.0 + x);
                }
                if (ix >= 0x3FF00000) {
                    double t = StrictMath.expm1(2.0 * Math.abs(x));
                    z = 1.0 - 2.0 / (t + 2.0);
                } else {
                    double t = StrictMath.expm1(-2.0 * Math.abs(x));
                    z = -t / (t + 2.0);
                }
            } else {
                z = 1.0;
            }
            return jx >= 0 ? z : -z;
        }
    }

    static final class Cosh {
        private static final double huge = 1.0E300;

        private Cosh() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            int ix = FdLibm.__HI(x);
            if ((ix &= Integer.MAX_VALUE) >= 0x7FF00000) {
                return x * x;
            }
            if (ix < 1071001155) {
                double t = StrictMath.expm1(Math.abs(x));
                double w = 1.0 + t;
                if (ix < 1015021568) {
                    return w;
                }
                return 1.0 + t * t / (w + w);
            }
            if (ix < 1077280768) {
                double t = StrictMath.exp(Math.abs(x));
                return 0.5 * t + 0.5 / t;
            }
            if (ix < 1082535490) {
                return 0.5 * StrictMath.exp(Math.abs(x));
            }
            int lx = FdLibm.__LO(x);
            if (ix < 1082536910 || ix == 1082536910 && Integer.compareUnsigned(lx, -1883637635) <= 0) {
                double w = StrictMath.exp(0.5 * Math.abs(x));
                double t = 0.5 * w;
                return t * w;
            }
            return Double.POSITIVE_INFINITY;
        }
    }

    static final class Sinh {
        private static final double shuge = 1.0E307;

        private Sinh() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            int jx = FdLibm.__HI(x);
            int ix = jx & Integer.MAX_VALUE;
            if (ix >= 0x7FF00000) {
                return x + x;
            }
            double h = 0.5;
            if (jx < 0) {
                h = -h;
            }
            if (ix < 1077280768) {
                if (ix < 0x3E300000 && 1.0E307 + x > 1.0) {
                    return x;
                }
                double t = StrictMath.expm1(Math.abs(x));
                if (ix < 0x3FF00000) {
                    return h * (2.0 * t - t * t / (t + 1.0));
                }
                return h * (t + t / (t + 1.0));
            }
            if (ix < 1082535490) {
                return h * StrictMath.exp(Math.abs(x));
            }
            int lx = FdLibm.__LO(x);
            if (ix < 1082536910 || ix == 1082536910 && Long.compareUnsigned(lx, -1883637635L) <= 0) {
                double w = StrictMath.exp(0.5 * Math.abs(x));
                double t = h * w;
                return t * w;
            }
            return x * 1.0E307;
        }
    }

    static final class Expm1 {
        private static final double huge = 1.0E300;
        private static final double tiny = 1.0E-300;
        private static final double o_threshold = 709.782712893384;
        private static final double ln2_hi = 0.6931471803691238;
        private static final double ln2_lo = 1.9082149292705877E-10;
        private static final double invln2 = 1.4426950408889634;
        private static final double Q1 = -0.03333333333333313;
        private static final double Q2 = 0.0015873015872548146;
        private static final double Q3 = -7.93650757867488E-5;
        private static final double Q4 = 4.008217827329362E-6;
        private static final double Q5 = -2.0109921818362437E-7;

        Expm1() {
        }

        static double compute(double x) {
            double t;
            int k;
            double c = 0.0;
            int hx = FdLibm.__HI(x);
            int xsb = hx & Integer.MIN_VALUE;
            double y = Math.abs(x);
            if ((hx &= Integer.MAX_VALUE) >= 1078159482) {
                if (hx >= 1082535490) {
                    if (hx >= 0x7FF00000) {
                        if ((hx & 0xFFFFF | FdLibm.__LO(x)) != 0) {
                            return x + x;
                        }
                        return xsb == 0 ? x : -1.0;
                    }
                    if (x > 709.782712893384) {
                        return Double.POSITIVE_INFINITY;
                    }
                }
                if (xsb != 0 && x + 1.0E-300 < 0.0) {
                    return -1.0;
                }
            }
            if (hx > 1071001154) {
                double lo;
                double hi;
                if (hx < 1072734898) {
                    if (xsb == 0) {
                        hi = x - 0.6931471803691238;
                        lo = 1.9082149292705877E-10;
                        k = 1;
                    } else {
                        hi = x + 0.6931471803691238;
                        lo = -1.9082149292705877E-10;
                        k = -1;
                    }
                } else {
                    k = (int)(1.4426950408889634 * x + (xsb == 0 ? 0.5 : -0.5));
                    t = k;
                    hi = x - t * 0.6931471803691238;
                    lo = t * 1.9082149292705877E-10;
                }
                x = hi - lo;
                c = hi - x - lo;
            } else {
                if (hx < 1016070144) {
                    double t2 = 1.0E300 + x;
                    return x - (t2 - (1.0E300 + x));
                }
                k = 0;
            }
            double hfx = 0.5 * x;
            double hxs = x * hfx;
            double r1 = 1.0 + hxs * (-0.03333333333333313 + hxs * (0.0015873015872548146 + hxs * (-7.93650757867488E-5 + hxs * (4.008217827329362E-6 + hxs * -2.0109921818362437E-7))));
            t = 3.0 - r1 * hfx;
            double e = hxs * ((r1 - t) / (6.0 - x * t));
            if (k == 0) {
                return x - (x * e - hxs);
            }
            e = x * (e - c) - c;
            e -= hxs;
            if (k == -1) {
                return 0.5 * (x - e) - 0.5;
            }
            if (k == 1) {
                if (x < -0.25) {
                    return -2.0 * (e - (x + 0.5));
                }
                return 1.0 + 2.0 * (x - e);
            }
            if (k <= -2 || k > 56) {
                y = 1.0 - (e - x);
                y = FdLibm.__HI(y, FdLibm.__HI(y) + (k << 20));
                return y - 1.0;
            }
            t = 1.0;
            if (k < 20) {
                t = FdLibm.__HI(t, 0x3FF00000 - (0x200000 >> k));
                y = t - (e - x);
                y = FdLibm.__HI(y, FdLibm.__HI(y) + (k << 20));
            } else {
                t = FdLibm.__HI(t, 1023 - k << 20);
                y = x - (e + t);
                y += 1.0;
                y = FdLibm.__HI(y, FdLibm.__HI(y) + (k << 20));
            }
            return y;
        }
    }

    static final class Log1p {
        private static final double ln2_hi = 0.6931471803691238;
        private static final double ln2_lo = 1.9082149292705877E-10;
        private static final double Lp1 = 0.6666666666666735;
        private static final double Lp2 = 0.3999999999940942;
        private static final double Lp3 = 0.2857142874366239;
        private static final double Lp4 = 0.22222198432149784;
        private static final double Lp5 = 0.1818357216161805;
        private static final double Lp6 = 0.15313837699209373;
        private static final double Lp7 = 0.14798198605116586;

        Log1p() {
        }

        public static double compute(double x) {
            double f = 0.0;
            double c = 0.0;
            int hu = 0;
            int hx = FdLibm.__HI(x);
            int ax = hx & Integer.MAX_VALUE;
            int k = 1;
            if (hx < 1071284858) {
                if (ax >= 0x3FF00000) {
                    if (x == -1.0) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    return Double.NaN;
                }
                if (ax < 1042284544) {
                    if (1.8014398509481984E16 + x > 0.0 && ax < 1016070144) {
                        return x;
                    }
                    return x - x * x * 0.5;
                }
                if (hx > 0 || hx <= -1076707645) {
                    k = 0;
                    f = x;
                    hu = 1;
                }
            }
            if (hx >= 0x7FF00000) {
                return x + x;
            }
            if (k != 0) {
                double u;
                if (hx < 0x43400000) {
                    u = 1.0 + x;
                    hu = FdLibm.__HI(u);
                    k = (hu >> 20) - 1023;
                    c = k > 0 ? 1.0 - (u - x) : x - (u - 1.0);
                    c /= u;
                } else {
                    u = x;
                    hu = FdLibm.__HI(u);
                    k = (hu >> 20) - 1023;
                    c = 0.0;
                }
                if ((hu &= 0xFFFFF) < 434334) {
                    u = FdLibm.__HI(u, hu | 0x3FF00000);
                } else {
                    ++k;
                    u = FdLibm.__HI(u, hu | 0x3FE00000);
                    hu = 0x100000 - hu >> 2;
                }
                f = u - 1.0;
            }
            double hfsq = 0.5 * f * f;
            if (hu == 0) {
                if (f == 0.0) {
                    if (k == 0) {
                        return 0.0;
                    }
                    return (double)k * 0.6931471803691238 + (c += (double)k * 1.9082149292705877E-10);
                }
                double R = hfsq * (1.0 - 0.6666666666666666 * f);
                if (k == 0) {
                    return f - R;
                }
                return (double)k * 0.6931471803691238 - (R - ((double)k * 1.9082149292705877E-10 + c) - f);
            }
            double s = f / (2.0 + f);
            double z = s * s;
            double R = z * (0.6666666666666735 + z * (0.3999999999940942 + z * (0.2857142874366239 + z * (0.22222198432149784 + z * (0.1818357216161805 + z * (0.15313837699209373 + z * 0.14798198605116586))))));
            if (k == 0) {
                return f - (hfsq - s * (hfsq + R));
            }
            return (double)k * 0.6931471803691238 - (hfsq - (s * (hfsq + R) + ((double)k * 1.9082149292705877E-10 + c)) - f);
        }
    }

    static final class Log10 {
        private static final double ivln10 = 0.4342944819032518;
        private static final double log10_2hi = 0.30102999566361177;
        private static final double log10_2lo = 3.694239077158931E-13;

        private Log10() {
            throw new UnsupportedOperationException();
        }

        public static double compute(double x) {
            int hx = FdLibm.__HI(x);
            int lx = FdLibm.__LO(x);
            int k = 0;
            if (hx < 0x100000) {
                if ((hx & Integer.MAX_VALUE | lx) == 0) {
                    return Double.NEGATIVE_INFINITY;
                }
                if (hx < 0) {
                    return (x - x) / 0.0;
                }
                k -= 54;
                hx = FdLibm.__HI(x *= 1.8014398509481984E16);
            }
            if (hx >= 0x7FF00000) {
                return x + x;
            }
            int i = ((k += (hx >> 20) - 1023) & Integer.MIN_VALUE) >>> 31;
            hx = hx & 0xFFFFF | 1023 - i << 20;
            double y = k + i;
            x = FdLibm.__HI(x, hx);
            double z = y * 3.694239077158931E-13 + 0.4342944819032518 * StrictMath.log(x);
            return z + y * 0.30102999566361177;
        }
    }

    static final class Log {
        private static final double ln2_hi = 0.6931471803691238;
        private static final double ln2_lo = 1.9082149292705877E-10;
        private static final double Lg1 = 0.6666666666666735;
        private static final double Lg2 = 0.3999999999940942;
        private static final double Lg3 = 0.2857142874366239;
        private static final double Lg4 = 0.22222198432149784;
        private static final double Lg5 = 0.1818357216161805;
        private static final double Lg6 = 0.15313837699209373;
        private static final double Lg7 = 0.14798198605116586;

        private Log() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            int hx = FdLibm.__HI(x);
            int lx = FdLibm.__LO(x);
            int k = 0;
            if (hx < 0x100000) {
                if ((hx & Integer.MAX_VALUE | lx) == 0) {
                    return Double.NEGATIVE_INFINITY;
                }
                if (hx < 0) {
                    return (x - x) / 0.0;
                }
                k -= 54;
                hx = FdLibm.__HI(x *= 1.8014398509481984E16);
            }
            if (hx >= 0x7FF00000) {
                return x + x;
            }
            k += (hx >> 20) - 1023;
            int i = (hx &= 0xFFFFF) + 614244 & 0x100000;
            x = FdLibm.__HI(x, hx | i ^ 0x3FF00000);
            k += i >> 20;
            double f = x - 1.0;
            if ((0xFFFFF & 2 + hx) < 3) {
                if (f == 0.0) {
                    if (k == 0) {
                        return 0.0;
                    }
                    double dk = k;
                    return dk * 0.6931471803691238 + dk * 1.9082149292705877E-10;
                }
                double R = f * f * (0.5 - 0.3333333333333333 * f);
                if (k == 0) {
                    return f - R;
                }
                double dk = k;
                return dk * 0.6931471803691238 - (R - dk * 1.9082149292705877E-10 - f);
            }
            double s = f / (2.0 + f);
            double dk = k;
            double z = s * s;
            i = hx - 398458;
            double w = z * z;
            int j = 440401 - hx;
            double t1 = w * (0.3999999999940942 + w * (0.22222198432149784 + w * 0.15313837699209373));
            double t2 = z * (0.6666666666666735 + w * (0.2857142874366239 + w * (0.1818357216161805 + w * 0.14798198605116586)));
            double R = t2 + t1;
            if ((i |= j) > 0) {
                double hfsq = 0.5 * f * f;
                if (k == 0) {
                    return f - (hfsq - s * (hfsq + R));
                }
                return dk * 0.6931471803691238 - (hfsq - (s * (hfsq + R) + dk * 1.9082149292705877E-10) - f);
            }
            if (k == 0) {
                return f - s * (f - R);
            }
            return dk * 0.6931471803691238 - (s * (f - R) - dk * 1.9082149292705877E-10 - f);
        }
    }

    static final class Exp {
        private static final double[] half = new double[]{0.5, -0.5};
        private static final double huge = 1.0E300;
        private static final double twom1000 = 9.332636185032189E-302;
        private static final double o_threshold = 709.782712893384;
        private static final double u_threshold = -745.1332191019411;
        private static final double[] ln2HI = new double[]{0.6931471803691238, -0.6931471803691238};
        private static final double[] ln2LO = new double[]{1.9082149292705877E-10, -1.9082149292705877E-10};
        private static final double invln2 = 1.4426950408889634;
        private static final double P1 = 0.16666666666666602;
        private static final double P2 = -0.0027777777777015593;
        private static final double P3 = 6.613756321437934E-5;
        private static final double P4 = -1.6533902205465252E-6;
        private static final double P5 = 4.1381367970572385E-8;

        private Exp() {
            throw new UnsupportedOperationException();
        }

        public static double compute(double x) {
            double t;
            double hi = 0.0;
            double lo = 0.0;
            int k = 0;
            int hx = FdLibm.__HI(x);
            int xsb = hx >> 31 & 1;
            if ((hx &= Integer.MAX_VALUE) >= 1082535490) {
                if (hx >= 0x7FF00000) {
                    if ((hx & 0xFFFFF | FdLibm.__LO(x)) != 0) {
                        return x + x;
                    }
                    return xsb == 0 ? x : 0.0;
                }
                if (x > 709.782712893384) {
                    return Double.POSITIVE_INFINITY;
                }
                if (x < -745.1332191019411) {
                    return 0.0;
                }
            }
            if (hx > 1071001154) {
                if (hx < 1072734898) {
                    hi = x - ln2HI[xsb];
                    lo = ln2LO[xsb];
                    k = 1 - xsb - xsb;
                } else {
                    k = (int)(1.4426950408889634 * x + half[xsb]);
                    t = k;
                    hi = x - t * ln2HI[0];
                    lo = t * ln2LO[0];
                }
                x = hi - lo;
            } else if (hx < 0x3E300000) {
                if (1.0E300 + x > 1.0) {
                    return 1.0 + x;
                }
            } else {
                k = 0;
            }
            t = x * x;
            double c = x - t * (0.16666666666666602 + t * (-0.0027777777777015593 + t * (6.613756321437934E-5 + t * (-1.6533902205465252E-6 + t * 4.1381367970572385E-8))));
            if (k == 0) {
                return 1.0 - (x * c / (c - 2.0) - x);
            }
            double y = 1.0 - (lo - x * c / (2.0 - c) - hi);
            if (k >= -1021) {
                y = FdLibm.__HI(y, FdLibm.__HI(y) + (k << 20));
                return y;
            }
            y = FdLibm.__HI(y, FdLibm.__HI(y) + (k + 1000 << 20));
            return y * 9.332636185032189E-302;
        }
    }

    static final class Pow {
        private Pow() {
            throw new UnsupportedOperationException();
        }

        public static double compute(double x, double y) {
            double p_l;
            double r;
            int k;
            int j;
            double t2;
            double t1;
            double v;
            double u;
            double w;
            double t;
            if (y == 0.0) {
                return 1.0;
            }
            if (Double.isNaN(x) || Double.isNaN(y)) {
                return x + y;
            }
            double y_abs = Math.abs(y);
            double x_abs = Math.abs(x);
            if (y == 2.0) {
                return x * x;
            }
            if (y == 0.5) {
                if (x >= -1.7976931348623157E308) {
                    return Math.sqrt(x + 0.0);
                }
            } else {
                if (y_abs == 1.0) {
                    return y == 1.0 ? x : 1.0 / x;
                }
                if (y_abs == Double.POSITIVE_INFINITY) {
                    if (x_abs == 1.0) {
                        return y - y;
                    }
                    if (x_abs > 1.0) {
                        return y >= 0.0 ? y : 0.0;
                    }
                    return y < 0.0 ? -y : 0.0;
                }
            }
            int hx = FdLibm.__HI(x);
            int ix = hx & Integer.MAX_VALUE;
            int y_is_int = 0;
            if (hx < 0) {
                long y_abs_as_long;
                if (y_abs >= 9.007199254740992E15) {
                    y_is_int = 2;
                } else if (y_abs >= 1.0 && (double)(y_abs_as_long = (long)y_abs) == y_abs) {
                    y_is_int = 2 - (int)(y_abs_as_long & 1L);
                }
            }
            if (x_abs == 0.0 || x_abs == Double.POSITIVE_INFINITY || x_abs == 1.0) {
                double z = x_abs;
                if (y < 0.0) {
                    z = 1.0 / z;
                }
                if (hx < 0) {
                    if ((ix - 0x3FF00000 | y_is_int) == 0) {
                        z = (z - z) / (z - z);
                    } else if (y_is_int == 1) {
                        z = -1.0 * z;
                    }
                }
                return z;
            }
            int n = (hx >> 31) + 1;
            if ((n | y_is_int) == 0) {
                return (x - x) / (x - x);
            }
            double s = 1.0;
            if ((n | y_is_int - 1) == 0) {
                s = -1.0;
            }
            if (y_abs > 2.1474856959999995E9) {
                double INV_LN2 = 1.4426950408889634;
                double INV_LN2_H = 1.442695f;
                double INV_LN2_L = 1.9259629911266175E-8;
                if (x_abs < 0.9999995231628418) {
                    return y < 0.0 ? s * Double.POSITIVE_INFINITY : s * 0.0;
                }
                if (x_abs > 1.0000009536743162) {
                    return y > 0.0 ? s * Double.POSITIVE_INFINITY : s * 0.0;
                }
                t = x_abs - 1.0;
                w = t * t * (0.5 - t * (0.3333333333333333 - t * 0.25));
                u = (double)1.442695f * t;
                v = t * 1.9259629911266175E-8 - w * 1.4426950408889634;
                t1 = u + v;
                t1 = FdLibm.__LO(t1, 0);
                t2 = v - (t1 - u);
            } else {
                double ss;
                double CP = 0.9617966939259756;
                double CP_H = 0.9617967009544373;
                double CP_L = -7.028461650952758E-9;
                n = 0;
                if (ix < 0x100000) {
                    n -= 53;
                    ix = FdLibm.__HI(x_abs *= 9.007199254740992E15);
                }
                n += (ix >> 20) - 1023;
                j = ix & 0xFFFFF;
                ix = j | 0x3FF00000;
                if (j <= 235662) {
                    k = 0;
                } else if (j < 767610) {
                    k = 1;
                } else {
                    k = 0;
                    ++n;
                    ix -= 0x100000;
                }
                x_abs = FdLibm.__HI(x_abs, ix);
                double[] BP = new double[]{1.0, 1.5};
                double[] DP_H = new double[]{0.0, 0.5849624872207642};
                double[] DP_L = new double[]{0.0, 1.350039202129749E-8};
                double L1 = 0.5999999999999946;
                double L2 = 0.4285714285785502;
                double L3 = 0.33333332981837743;
                double L4 = 0.272728123808534;
                double L5 = 0.23066074577556175;
                double L6 = 0.20697501780033842;
                u = x_abs - BP[k];
                v = 1.0 / (x_abs + BP[k]);
                double s_h = ss = u * v;
                s_h = FdLibm.__LO(s_h, 0);
                double t_h = 0.0;
                t_h = FdLibm.__HI(t_h, (ix >> 1 | 0x20000000) + 524288 + (k << 18));
                double t_l = x_abs - (t_h - BP[k]);
                double s_l = v * (u - s_h * t_h - s_h * t_l);
                double s2 = ss * ss;
                r = s2 * s2 * (0.5999999999999946 + s2 * (0.4285714285785502 + s2 * (0.33333332981837743 + s2 * (0.272728123808534 + s2 * (0.23066074577556175 + s2 * 0.20697501780033842)))));
                s2 = s_h * s_h;
                t_h = 3.0 + s2 + (r += s_l * (s_h + ss));
                t_h = FdLibm.__LO(t_h, 0);
                t_l = r - (t_h - 3.0 - s2);
                u = s_h * t_h;
                v = s_l * t_h + t_l * ss;
                double p_h = u + v;
                p_h = FdLibm.__LO(p_h, 0);
                p_l = v - (p_h - u);
                double z_h = 0.9617967009544373 * p_h;
                double z_l = -7.028461650952758E-9 * p_h + p_l * 0.9617966939259756 + DP_L[k];
                t = n;
                t1 = z_h + z_l + DP_H[k] + t;
                t1 = FdLibm.__LO(t1, 0);
                t2 = z_l - (t1 - t - DP_H[k] - z_h);
            }
            double y1 = y;
            y1 = FdLibm.__LO(y1, 0);
            p_l = (y - y1) * t1 + y * t2;
            double p_h = y1 * t1;
            double z = p_l + p_h;
            j = FdLibm.__HI(z);
            int i = FdLibm.__LO(z);
            if (j >= 0x40900000) {
                if ((j - 0x40900000 | i) != 0) {
                    return s * Double.POSITIVE_INFINITY;
                }
                double OVT = 8.008566259537294E-17;
                if (p_l + 8.008566259537294E-17 > z - p_h) {
                    return s * Double.POSITIVE_INFINITY;
                }
            } else if ((j & Integer.MAX_VALUE) >= 1083231232) {
                if ((j - -1064252416 | i) != 0) {
                    return s * 0.0;
                }
                if (p_l <= z - p_h) {
                    return s * 0.0;
                }
            }
            double P1 = 0.16666666666666602;
            double P2 = -0.0027777777777015593;
            double P3 = 6.613756321437934E-5;
            double P4 = -1.6533902205465252E-6;
            double P5 = 4.1381367970572385E-8;
            double LG2 = 0.6931471805599453;
            double LG2_H = 0.6931471824645996;
            double LG2_L = -1.904654299957768E-9;
            i = j & Integer.MAX_VALUE;
            k = (i >> 20) - 1023;
            n = 0;
            if (i > 1071644672) {
                n = j + (0x100000 >> k + 1);
                k = ((n & Integer.MAX_VALUE) >> 20) - 1023;
                t = 0.0;
                t = FdLibm.__HI(t, n & ~(1048575 >> k));
                n = (n & 0xFFFFF | 0x100000) >> 20 - k;
                if (j < 0) {
                    n = -n;
                }
                p_h -= t;
            }
            t = p_l + p_h;
            t = FdLibm.__LO(t, 0);
            u = t * 0.6931471824645996;
            v = (p_l - (t - p_h)) * 0.6931471805599453 + t * -1.904654299957768E-9;
            z = u + v;
            w = v - (z - u);
            t = z * z;
            t1 = z - t * (0.16666666666666602 + t * (-0.0027777777777015593 + t * (6.613756321437934E-5 + t * (-1.6533902205465252E-6 + t * 4.1381367970572385E-8))));
            r = z * t1 / (t1 - 2.0) - (w + z * w);
            z = 1.0 - (r - z);
            j = FdLibm.__HI(z);
            if ((j += n << 20) >> 20 <= 0) {
                z = Math.scalb(z, n);
            } else {
                int z_hi = FdLibm.__HI(z);
                z = FdLibm.__HI(z, z_hi += n << 20);
            }
            return s * z;
        }
    }

    static final class Hypot {
        public static final double TWO_MINUS_600 = 2.409919865102884E-181;
        public static final double TWO_PLUS_600 = 4.149515568880993E180;

        private Hypot() {
            throw new UnsupportedOperationException();
        }

        public static double compute(double x, double y) {
            double w;
            double t1;
            int hb;
            double a = Math.abs(x);
            double b = Math.abs(y);
            if (!Double.isFinite(a) || !Double.isFinite(b)) {
                if (a == Double.POSITIVE_INFINITY || b == Double.POSITIVE_INFINITY) {
                    return Double.POSITIVE_INFINITY;
                }
                return a + b;
            }
            if (b > a) {
                double tmp = a;
                a = b;
                b = tmp;
            }
            assert (a >= b);
            int ha = FdLibm.__HI(a);
            if (ha - (hb = FdLibm.__HI(b)) > 0x3C00000) {
                return a + b;
            }
            int k = 0;
            if (a > 3.2733937296446915E150) {
                ha -= 629145600;
                hb -= 629145600;
                a *= 2.409919865102884E-181;
                b *= 2.409919865102884E-181;
                k += 600;
            }
            if (b < 3.054936363499605E-151) {
                if (b < Double.MIN_NORMAL) {
                    if (b == 0.0) {
                        return a;
                    }
                    t1 = 4.49423283715579E307;
                    b *= t1;
                    a *= t1;
                    k -= 1022;
                } else {
                    ha += 629145600;
                    hb += 629145600;
                    a *= 4.149515568880993E180;
                    b *= 4.149515568880993E180;
                    k -= 600;
                }
            }
            if ((w = a - b) > b) {
                t1 = 0.0;
                t1 = FdLibm.__HI(t1, ha);
                double t2 = a - t1;
                w = Math.sqrt(t1 * t1 - (b * -b - t2 * (a + t1)));
            } else {
                a += a;
                double y1 = 0.0;
                y1 = FdLibm.__HI(y1, hb);
                double y2 = b - y1;
                t1 = 0.0;
                t1 = FdLibm.__HI(t1, ha + 0x100000);
                double t2 = a - t1;
                w = Math.sqrt(t1 * y1 - (w * -w - (t1 * y2 + t2 * b)));
            }
            if (k != 0) {
                return Math.powerOfTwoD(k) * w;
            }
            return w;
        }
    }

    static final class Cbrt {
        private static final int B1 = 715094163;
        private static final int B2 = 696219795;
        private static final double C = 0.5428571428571428;
        private static final double D = -0.7053061224489796;
        private static final double E = 1.4142857142857144;
        private static final double F = 1.6071428571428572;
        private static final double G = 0.35714285714285715;

        private Cbrt() {
            throw new UnsupportedOperationException();
        }

        public static double compute(double x) {
            double t = 0.0;
            if (x == 0.0 || !Double.isFinite(x)) {
                return x;
            }
            double sign = x < 0.0 ? -1.0 : 1.0;
            if ((x = Math.abs(x)) < Double.MIN_NORMAL) {
                t = 1.8014398509481984E16;
                t *= x;
                t = FdLibm.__HI(t, FdLibm.__HI(t) / 3 + 696219795);
            } else {
                int hx = FdLibm.__HI(x);
                t = FdLibm.__HI(t, hx / 3 + 715094163);
            }
            double r = t * t / x;
            double s = 0.5428571428571428 + r * t;
            t *= 0.35714285714285715 + 1.6071428571428572 / (s + 1.4142857142857144 + -0.7053061224489796 / s);
            t = FdLibm.__LO(t, 0);
            t = FdLibm.__HI(t, FdLibm.__HI(t) + 1);
            s = t * t;
            r = x / s;
            double w = t + t;
            r = (r - t) / (w + r);
            t += t * r;
            return sign * t;
        }
    }

    static final class Sqrt {
        private static final double tiny = 1.0E-300;

        private Sqrt() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            int t;
            int r;
            int m;
            double z = 0.0;
            int sign = Integer.MIN_VALUE;
            int ix0 = FdLibm.__HI(x);
            int ix1 = FdLibm.__LO(x);
            if ((ix0 & 0x7FF00000) == 0x7FF00000) {
                return x * x + x;
            }
            if (ix0 <= 0) {
                if ((ix0 & ~sign | ix1) == 0) {
                    return x;
                }
                if (ix0 < 0) {
                    return (x - x) / (x - x);
                }
            }
            if ((m = ix0 >> 20) == 0) {
                while (ix0 == 0) {
                    m -= 21;
                    ix0 |= ix1 >>> 11;
                    ix1 <<= 21;
                }
                int i = 0;
                while ((ix0 & 0x100000) == 0) {
                    ix0 <<= 1;
                    ++i;
                }
                m -= i - 1;
                ix0 |= ix1 >>> 32 - i;
                ix1 <<= i;
            }
            ix0 = ix0 & 0xFFFFF | 0x100000;
            if (((m -= 1023) & 1) != 0) {
                ix0 += ix0 + ((ix1 & sign) >>> 31);
                ix1 += ix1;
            }
            m >>= 1;
            ix0 += ix0 + ((ix1 & sign) >>> 31);
            ix1 += ix1;
            int s1 = 0;
            int s0 = 0;
            int q1 = 0;
            int q = 0;
            for (r = 0x200000; r != 0; r >>>= 1) {
                t = s0 + r;
                if (t <= ix0) {
                    s0 = t + r;
                    ix0 -= t;
                    q += r;
                }
                ix0 += ix0 + ((ix1 & sign) >>> 31);
                ix1 += ix1;
            }
            for (r = sign; r != 0; r >>>= 1) {
                int t1 = s1 + r;
                t = s0;
                if (t < ix0 || t == ix0 && Integer.compareUnsigned(t1, ix1) <= 0) {
                    s1 = t1 + r;
                    if ((t1 & sign) == sign && (s1 & sign) == 0) {
                        ++s0;
                    }
                    ix0 -= t;
                    if (Integer.compareUnsigned(ix1, t1) < 0) {
                        --ix0;
                    }
                    ix1 -= t1;
                    q1 += r;
                }
                ix0 += ix0 + ((ix1 & sign) >>> 31);
                ix1 += ix1;
            }
            if ((ix0 | ix1) != 0 && (z = 1.0) >= 1.0) {
                z = 1.0;
                if (q1 == -1) {
                    q1 = 0;
                    ++q;
                } else if (z > 1.0) {
                    if (q1 == -2) {
                        ++q;
                    }
                    q1 += 2;
                } else {
                    q1 += q1 & 1;
                }
            }
            ix0 = (q >> 1) + 1071644672;
            ix1 = q1 >>> 1;
            if ((q & 1) == 1) {
                ix1 |= sign;
            }
            return FdLibm.__HI_LO(ix0 += m << 20, ix1);
        }
    }

    static final class Atan2 {
        private static final double tiny = 1.0E-300;
        private static final double pi_o_4 = 0.7853981633974483;
        private static final double pi_o_2 = 1.5707963267948966;
        private static final double pi_lo = 1.2246467991473532E-16;

        private Atan2() {
            throw new UnsupportedOperationException();
        }

        static double compute(double y, double x) {
            int hx = FdLibm.__HI(x);
            int ix = hx & Integer.MAX_VALUE;
            int lx = FdLibm.__LO(x);
            int hy = FdLibm.__HI(y);
            int iy = hy & Integer.MAX_VALUE;
            int ly = FdLibm.__LO(y);
            if (Double.isNaN(x) || Double.isNaN(y)) {
                return x + y;
            }
            if ((hx - 0x3FF00000 | lx) == 0) {
                return StrictMath.atan(y);
            }
            int m = hy >> 31 & 1 | hx >> 30 & 2;
            if ((iy | ly) == 0) {
                switch (m) {
                    case 0: 
                    case 1: {
                        return y;
                    }
                    case 2: {
                        return Math.PI;
                    }
                    case 3: {
                        return -Math.PI;
                    }
                }
            }
            if ((ix | lx) == 0) {
                return hy < 0 ? -1.5707963267948966 : 1.5707963267948966;
            }
            if (ix == 0x7FF00000) {
                if (iy == 0x7FF00000) {
                    switch (m) {
                        case 0: {
                            return 0.7853981633974483;
                        }
                        case 1: {
                            return -0.7853981633974483;
                        }
                        case 2: {
                            return 2.356194490192345;
                        }
                        case 3: {
                            return -2.356194490192345;
                        }
                    }
                } else {
                    switch (m) {
                        case 0: {
                            return 0.0;
                        }
                        case 1: {
                            return -0.0;
                        }
                        case 2: {
                            return Math.PI;
                        }
                        case 3: {
                            return -Math.PI;
                        }
                    }
                }
            }
            if (iy == 0x7FF00000) {
                return hy < 0 ? -1.5707963267948966 : 1.5707963267948966;
            }
            int k = iy - ix >> 20;
            double z = k > 60 ? 1.5707963267948966 : (hx < 0 && k < -60 ? 0.0 : StrictMath.atan(Math.abs(y / x)));
            switch (m) {
                case 0: {
                    return z;
                }
                case 1: {
                    return -z;
                }
                case 2: {
                    return Math.PI - (z - 1.2246467991473532E-16);
                }
            }
            return z - 1.2246467991473532E-16 - Math.PI;
        }
    }

    static final class Atan {
        private static final double[] atanhi = new double[]{0.4636476090008061, 0.7853981633974483, 0.982793723247329, 1.5707963267948966};
        private static final double[] atanlo = new double[]{2.2698777452961687E-17, 3.061616997868383E-17, 1.3903311031230998E-17, 6.123233995736766E-17};
        private static final double[] aT = new double[]{0.3333333333333293, -0.19999999999876483, 0.14285714272503466, -0.11111110405462356, 0.09090887133436507, -0.0769187620504483, 0.06661073137387531, -0.058335701337905735, 0.049768779946159324, -0.036531572744216916, 0.016285820115365782};

        private Atan() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            int id;
            int hx = FdLibm.__HI(x);
            int ix = hx & Integer.MAX_VALUE;
            if (ix >= 0x44100000) {
                if (ix > 0x7FF00000 || ix == 0x7FF00000 && FdLibm.__LO(x) != 0) {
                    return x + x;
                }
                if (hx > 0) {
                    return atanhi[3] + atanlo[3];
                }
                return -atanhi[3] - atanlo[3];
            }
            if (ix < 1071382528) {
                if (ix < 1042284544 && 1.0E300 + x > 1.0) {
                    return x;
                }
                id = -1;
            } else {
                x = Math.abs(x);
                if (ix < 0x3FF30000) {
                    if (ix < 1072037888) {
                        id = 0;
                        x = (2.0 * x - 1.0) / (2.0 + x);
                    } else {
                        id = 1;
                        x = (x - 1.0) / (x + 1.0);
                    }
                } else if (ix < 1073971200) {
                    id = 2;
                    x = (x - 1.5) / (1.0 + 1.5 * x);
                } else {
                    id = 3;
                    x = -1.0 / x;
                }
            }
            double z = x * x;
            double w = z * z;
            double s1 = z * (aT[0] + w * (aT[2] + w * (aT[4] + w * (aT[6] + w * (aT[8] + w * aT[10])))));
            double s2 = w * (aT[1] + w * (aT[3] + w * (aT[5] + w * (aT[7] + w * aT[9]))));
            if (id < 0) {
                return x - x * (s1 + s2);
            }
            z = atanhi[id] - (x * (s1 + s2) - atanlo[id] - x);
            return hx < 0 ? -z : z;
        }
    }

    static final class Acos {
        private static final double pio2_hi = 1.5707963267948966;
        private static final double pio2_lo = 6.123233995736766E-17;
        private static final double pS0 = 0.16666666666666666;
        private static final double pS1 = -0.3255658186224009;
        private static final double pS2 = 0.20121253213486293;
        private static final double pS3 = -0.04005553450067941;
        private static final double pS4 = 7.915349942898145E-4;
        private static final double pS5 = 3.479331075960212E-5;
        private static final double qS1 = -2.403394911734414;
        private static final double qS2 = 2.0209457602335057;
        private static final double qS3 = -0.6882839716054533;
        private static final double qS4 = 0.07703815055590194;

        private Acos() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            double s;
            int hx = FdLibm.__HI(x);
            int ix = hx & Integer.MAX_VALUE;
            if (ix >= 0x3FF00000) {
                if ((ix - 0x3FF00000 | FdLibm.__LO(x)) == 0) {
                    if (hx > 0) {
                        return 0.0;
                    }
                    return Math.PI;
                }
                return (x - x) / (x - x);
            }
            if (ix < 1071644672) {
                if (ix <= 1012924416) {
                    return 1.5707963267948966;
                }
                double z = x * x;
                double p = z * (0.16666666666666666 + z * (-0.3255658186224009 + z * (0.20121253213486293 + z * (-0.04005553450067941 + z * (7.915349942898145E-4 + z * 3.479331075960212E-5)))));
                double q = 1.0 + z * (-2.403394911734414 + z * (2.0209457602335057 + z * (-0.6882839716054533 + z * 0.07703815055590194)));
                double r = p / q;
                return 1.5707963267948966 - (x - (6.123233995736766E-17 - x * r));
            }
            if (hx < 0) {
                double z = (1.0 + x) * 0.5;
                double p = z * (0.16666666666666666 + z * (-0.3255658186224009 + z * (0.20121253213486293 + z * (-0.04005553450067941 + z * (7.915349942898145E-4 + z * 3.479331075960212E-5)))));
                double q = 1.0 + z * (-2.403394911734414 + z * (2.0209457602335057 + z * (-0.6882839716054533 + z * 0.07703815055590194)));
                double s2 = Math.sqrt(z);
                double r = p / q;
                double w = r * s2 - 6.123233995736766E-17;
                return Math.PI - 2.0 * (s2 + w);
            }
            double z = (1.0 - x) * 0.5;
            double df = s = Math.sqrt(z);
            df = FdLibm.__LO(df, 0);
            double c = (z - df * df) / (s + df);
            double p = z * (0.16666666666666666 + z * (-0.3255658186224009 + z * (0.20121253213486293 + z * (-0.04005553450067941 + z * (7.915349942898145E-4 + z * 3.479331075960212E-5)))));
            double q = 1.0 + z * (-2.403394911734414 + z * (2.0209457602335057 + z * (-0.6882839716054533 + z * 0.07703815055590194)));
            double r = p / q;
            double w = r * s + c;
            return 2.0 * (df + w);
        }
    }

    static final class Asin {
        private static final double pio2_hi = 1.5707963267948966;
        private static final double pio2_lo = 6.123233995736766E-17;
        private static final double pio4_hi = 0.7853981633974483;
        private static final double pS0 = 0.16666666666666666;
        private static final double pS1 = -0.3255658186224009;
        private static final double pS2 = 0.20121253213486293;
        private static final double pS3 = -0.04005553450067941;
        private static final double pS4 = 7.915349942898145E-4;
        private static final double pS5 = 3.479331075960212E-5;
        private static final double qS1 = -2.403394911734414;
        private static final double qS2 = 2.0209457602335057;
        private static final double qS3 = -0.6882839716054533;
        private static final double qS4 = 0.07703815055590194;

        private Asin() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            double t = 0.0;
            int hx = FdLibm.__HI(x);
            int ix = hx & Integer.MAX_VALUE;
            if (ix >= 0x3FF00000) {
                if ((ix - 0x3FF00000 | FdLibm.__LO(x)) == 0) {
                    return x * 1.5707963267948966 + x * 6.123233995736766E-17;
                }
                return (x - x) / (x - x);
            }
            if (ix < 1071644672) {
                if (ix < 1044381696) {
                    if (1.0E300 + x > 1.0) {
                        return x;
                    }
                } else {
                    t = x * x;
                }
                double p = t * (0.16666666666666666 + t * (-0.3255658186224009 + t * (0.20121253213486293 + t * (-0.04005553450067941 + t * (7.915349942898145E-4 + t * 3.479331075960212E-5)))));
                double q = 1.0 + t * (-2.403394911734414 + t * (2.0209457602335057 + t * (-0.6882839716054533 + t * 0.07703815055590194)));
                double w = p / q;
                return x + x * w;
            }
            double w = 1.0 - Math.abs(x);
            t = w * 0.5;
            double p = t * (0.16666666666666666 + t * (-0.3255658186224009 + t * (0.20121253213486293 + t * (-0.04005553450067941 + t * (7.915349942898145E-4 + t * 3.479331075960212E-5)))));
            double q = 1.0 + t * (-2.403394911734414 + t * (2.0209457602335057 + t * (-0.6882839716054533 + t * 0.07703815055590194)));
            double s = Math.sqrt(t);
            if (ix >= 0x3FEF3333) {
                w = p / q;
                t = 1.5707963267948966 - (2.0 * (s + s * w) - 6.123233995736766E-17);
            } else {
                w = s;
                w = FdLibm.__LO(w, 0);
                double c = (t - w * w) / (s + w);
                double r = p / q;
                p = 2.0 * s * r - (6.123233995736766E-17 - 2.0 * c);
                q = 0.7853981633974483 - 2.0 * w;
                t = 0.7853981633974483 - (p - q);
            }
            return hx > 0 ? t : -t;
        }
    }

    static final class KernelRemPio2 {
        private static final int[] init_jk = new int[]{2, 3, 4, 6};
        private static final double[] PIo2 = new double[]{1.570796251296997, 7.549789415861596E-8, 5.390302529957765E-15, 3.282003415807913E-22, 1.270655753080676E-29, 1.2293330898111133E-36, 2.7337005381646456E-44, 2.1674168387780482E-51};
        static final double twon24 = 5.960464477539063E-8;

        KernelRemPio2() {
        }

        static int __kernel_rem_pio2(double[] x, double[] y, int e0, int nx, int prec, int[] ipio2) {
            int k;
            int ih;
            int n;
            double z;
            double fw;
            int jk;
            int[] iq = new int[20];
            double[] f = new double[20];
            double[] fq = new double[20];
            double[] q = new double[20];
            int jp = jk = init_jk[prec];
            int jx = nx - 1;
            int jv = (e0 - 3) / 24;
            if (jv < 0) {
                jv = 0;
            }
            int q0 = e0 - 24 * (jv + 1);
            int j = jv - jx;
            int m = jx + jk;
            int i = 0;
            while (i <= m) {
                f[i] = j < 0 ? 0.0 : (double)ipio2[j];
                ++i;
                ++j;
            }
            for (i = 0; i <= jk; ++i) {
                fw = 0.0;
                for (j = 0; j <= jx; ++j) {
                    fw += x[j] * f[jx + i - j];
                }
                q[i] = fw;
            }
            int jz = jk;
            while (true) {
                i = 0;
                z = q[jz];
                for (j = jz; j > 0; --j) {
                    fw = (int)(5.960464477539063E-8 * z);
                    iq[i] = (int)(z - 1.6777216E7 * fw);
                    z = q[j - 1] + fw;
                    ++i;
                }
                z = Math.scalb(z, q0);
                z -= 8.0 * Math.floor(z * 0.125);
                n = (int)z;
                z -= (double)n;
                ih = 0;
                if (q0 > 0) {
                    i = iq[jz - 1] >> 24 - q0;
                    n += i;
                    int n2 = jz - 1;
                    iq[n2] = iq[n2] - (i << 24 - q0);
                    ih = iq[jz - 1] >> 23 - q0;
                } else if (q0 == 0) {
                    ih = iq[jz - 1] >> 23;
                } else if (z >= 0.5) {
                    ih = 2;
                }
                if (ih > 0) {
                    ++n;
                    boolean carry = false;
                    for (i = 0; i < jz; ++i) {
                        j = iq[i];
                        if (!carry) {
                            if (j == 0) continue;
                            carry = true;
                            iq[i] = 0x1000000 - j;
                            continue;
                        }
                        iq[i] = 0xFFFFFF - j;
                    }
                    if (q0 > 0) {
                        switch (q0) {
                            case 1: {
                                int n3 = jz - 1;
                                iq[n3] = iq[n3] & 0x7FFFFF;
                                break;
                            }
                            case 2: {
                                int n4 = jz - 1;
                                iq[n4] = iq[n4] & 0x3FFFFF;
                            }
                        }
                    }
                    if (ih == 2) {
                        z = 1.0 - z;
                        if (carry) {
                            z -= Math.scalb(1.0, q0);
                        }
                    }
                }
                if (z != 0.0) break;
                j = 0;
                for (i = jz - 1; i >= jk; --i) {
                    j |= iq[i];
                }
                if (j != 0) break;
                k = 1;
                while (iq[jk - k] == 0) {
                    ++k;
                }
                for (i = jz + 1; i <= jz + k; ++i) {
                    f[jx + i] = ipio2[jv + i];
                    fw = 0.0;
                    for (j = 0; j <= jx; ++j) {
                        fw += x[j] * f[jx + i - j];
                    }
                    q[i] = fw;
                }
                jz += k;
            }
            if (z == 0.0) {
                --jz;
                q0 -= 24;
                while (iq[jz] == 0) {
                    --jz;
                    q0 -= 24;
                }
            } else if ((z = Math.scalb(z, -q0)) >= 1.6777216E7) {
                fw = (int)(5.960464477539063E-8 * z);
                iq[jz] = (int)(z - 1.6777216E7 * fw);
                q0 += 24;
                iq[++jz] = (int)fw;
            } else {
                iq[jz] = (int)z;
            }
            fw = Math.scalb(1.0, q0);
            for (i = jz; i >= 0; --i) {
                q[i] = fw * (double)iq[i];
                fw *= 5.960464477539063E-8;
            }
            for (i = jz; i >= 0; --i) {
                fw = 0.0;
                for (k = 0; k <= jp && k <= jz - i; ++k) {
                    fw += PIo2[k] * q[i + k];
                }
                fq[jz - i] = fw;
            }
            switch (prec) {
                case 0: {
                    fw = 0.0;
                    for (i = jz; i >= 0; --i) {
                        fw += fq[i];
                    }
                    y[0] = ih == 0 ? fw : -fw;
                    break;
                }
                case 1: 
                case 2: {
                    fw = 0.0;
                    for (i = jz; i >= 0; --i) {
                        fw += fq[i];
                    }
                    y[0] = ih == 0 ? fw : -fw;
                    fw = fq[0] - fw;
                    for (i = 1; i <= jz; ++i) {
                        fw += fq[i];
                    }
                    y[1] = ih == 0 ? fw : -fw;
                    break;
                }
                case 3: {
                    for (i = jz; i > 0; --i) {
                        fw = fq[i - 1] + fq[i];
                        int n5 = i;
                        fq[n5] = fq[n5] + (fq[i - 1] - fw);
                        fq[i - 1] = fw;
                    }
                    for (i = jz; i > 1; --i) {
                        fw = fq[i - 1] + fq[i];
                        int n6 = i;
                        fq[n6] = fq[n6] + (fq[i - 1] - fw);
                        fq[i - 1] = fw;
                    }
                    fw = 0.0;
                    for (i = jz; i >= 2; --i) {
                        fw += fq[i];
                    }
                    if (ih == 0) {
                        y[0] = fq[0];
                        y[1] = fq[1];
                        y[2] = fw;
                        break;
                    }
                    y[0] = -fq[0];
                    y[1] = -fq[1];
                    y[2] = -fw;
                }
            }
            return n & 7;
        }
    }

    static final class RemPio2 {
        private static final int[] two_over_pi = new int[]{10680707, 0x6E4E44, 1387004, 2578385, 16069853, 12639074, 9804092, 4427841, 16666979, 11263675, 12935607, 2387514, 0x424DD2, 14681673, 3074569, 13734428, 16653803, 1880361, 10960616, 8533493, 3062596, 8710556, 7349940, 6258241, 3772886, 3769171, 3798172, 8675211, 12450088, 3874808, 9961438, 366607, 15675153, 9132554, 7151469, 3571407, 2607881, 12013382, 4155038, 6285869, 7677882, 13102053, 15825725, 473591, 9065106, 15363067, 6271263, 9264392, 5636912, 4652155, 7056368, 13614112, 10155062, 1944035, 9527646, 15080200, 6658437, 6231200, 6832269, 16767104, 5075751, 3212806, 1398474, 7579849, 6349435, 12618859};
        private static final int[] npio2_hw = new int[]{1073291771, 1074340347, 1074977148, 1075388923, 1075800698, 1076025724, 1076231611, 1076437499, 1076643386, 1076849274, 1076971356, 1077074300, 1077177244, 1077280187, 1077383131, 1077486075, 1077589019, 1077691962, 1077794906, 1077897850, 1077968460, 1078019932, 1078071404, 1078122876, 1078174348, 1078225820, 1078277292, 1078328763, 1078380235, 1078431707, 1078483179, 1078534651};
        private static final double invpio2 = 0.6366197723675814;
        private static final double pio2_1 = 1.5707963267341256;
        private static final double pio2_1t = 6.077100506506192E-11;
        private static final double pio2_2 = 6.077100506303966E-11;
        private static final double pio2_2t = 2.0222662487959506E-21;
        private static final double pio2_3 = 2.0222662487111665E-21;
        private static final double pio2_3t = 8.4784276603689E-32;

        RemPio2() {
        }

        static int __ieee754_rem_pio2(double x, double[] y) {
            double z = 0.0;
            double[] tx = new double[3];
            int hx = FdLibm.__HI(x);
            int ix = hx & Integer.MAX_VALUE;
            if (ix <= 1072243195) {
                y[0] = x;
                y[1] = 0.0;
                return 0;
            }
            if (ix < 1073928572) {
                if (hx > 0) {
                    z = x - 1.5707963267341256;
                    if (ix != 1073291771) {
                        y[0] = z - 6.077100506506192E-11;
                        y[1] = z - y[0] - 6.077100506506192E-11;
                    } else {
                        y[0] = (z -= 6.077100506303966E-11) - 2.0222662487959506E-21;
                        y[1] = z - y[0] - 2.0222662487959506E-21;
                    }
                    return 1;
                }
                z = x + 1.5707963267341256;
                if (ix != 1073291771) {
                    y[0] = z + 6.077100506506192E-11;
                    y[1] = z - y[0] + 6.077100506506192E-11;
                } else {
                    y[0] = (z += 6.077100506303966E-11) + 2.0222662487959506E-21;
                    y[1] = z - y[0] + 2.0222662487959506E-21;
                }
                return -1;
            }
            if (ix <= 1094263291) {
                double t = Math.abs(x);
                int n = (int)(t * 0.6366197723675814 + 0.5);
                double fn = n;
                double r = t - fn * 1.5707963267341256;
                double w = fn * 6.077100506506192E-11;
                if (n < 32 && ix != npio2_hw[n - 1]) {
                    y[0] = r - w;
                } else {
                    int j = ix >> 20;
                    y[0] = r - w;
                    int i = j - (FdLibm.__HI(y[0]) >> 20 & 0x7FF);
                    if (i > 16) {
                        t = r;
                        w = fn * 6.077100506303966E-11;
                        r = t - w;
                        w = fn * 2.0222662487959506E-21 - (t - r - w);
                        y[0] = r - w;
                        i = j - (FdLibm.__HI(y[0]) >> 20 & 0x7FF);
                        if (i > 49) {
                            t = r;
                            w = fn * 2.0222662487111665E-21;
                            r = t - w;
                            w = fn * 8.4784276603689E-32 - (t - r - w);
                            y[0] = r - w;
                        }
                    }
                }
                y[1] = r - y[0] - w;
                if (hx < 0) {
                    y[0] = -y[0];
                    y[1] = -y[1];
                    return -n;
                }
                return n;
            }
            if (ix >= 0x7FF00000) {
                y[0] = y[1] = x - x;
                return 0;
            }
            z = FdLibm.__LO(z, FdLibm.__LO(x));
            int e0 = (ix >> 20) - 1046;
            z = FdLibm.__HI(z, ix - (e0 << 20));
            for (int i = 0; i < 2; ++i) {
                tx[i] = (int)z;
                z = (z - tx[i]) * 1.6777216E7;
            }
            tx[2] = z;
            int nx = 3;
            while (tx[nx - 1] == 0.0) {
                --nx;
            }
            int n = KernelRemPio2.__kernel_rem_pio2(tx, y, e0, nx, 2, two_over_pi);
            if (hx < 0) {
                y[0] = -y[0];
                y[1] = -y[1];
                return -n;
            }
            return n;
        }
    }

    static final class Tan {
        private static final double pio4 = 0.7853981633974483;
        private static final double pio4lo = 3.061616997868383E-17;
        private static final double[] T = new double[]{0.3333333333333341, 0.13333333333320124, 0.05396825397622605, 0.021869488294859542, 0.0088632398235993, 0.0035920791075913124, 0.0014562094543252903, 5.880412408202641E-4, 2.464631348184699E-4, 7.817944429395571E-5, 7.140724913826082E-5, -1.8558637485527546E-5, 2.590730518636337E-5};

        private Tan() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            double[] y = new double[2];
            double z = 0.0;
            int ix = FdLibm.__HI(x);
            if ((ix &= Integer.MAX_VALUE) <= 1072243195) {
                return Tan.__kernel_tan(x, z, 1);
            }
            if (ix >= 0x7FF00000) {
                return x - x;
            }
            int n = RemPio2.__ieee754_rem_pio2(x, y);
            return Tan.__kernel_tan(y[0], y[1], 1 - ((n & 1) << 1));
        }

        static double __kernel_tan(double x, double y, int iy) {
            double a;
            double w;
            double z;
            int hx = FdLibm.__HI(x);
            int ix = hx & Integer.MAX_VALUE;
            if (ix < 0x3E300000 && (int)x == 0) {
                double a2;
                double w2;
                if ((ix | FdLibm.__LO(x) | iy + 1) == 0) {
                    return 1.0 / Math.abs(x);
                }
                if (iy == 1) {
                    return x;
                }
                double z2 = w2 = x + y;
                z2 = FdLibm.__LO(z2, 0);
                double v = y - (z2 - x);
                double t = a2 = -1.0 / w2;
                t = FdLibm.__LO(t, 0);
                double s = 1.0 + t * z2;
                return t + a2 * (s + t * v);
            }
            if (ix >= 1072010280) {
                if (hx < 0) {
                    x = -x;
                    y = -y;
                }
                z = 0.7853981633974483 - x;
                w = 3.061616997868383E-17 - y;
                x = z + w;
                y = 0.0;
            }
            z = x * x;
            w = z * z;
            double r = T[1] + w * (T[3] + w * (T[5] + w * (T[7] + w * (T[9] + w * T[11]))));
            double v = z * (T[2] + w * (T[4] + w * (T[6] + w * (T[8] + w * (T[10] + w * T[12])))));
            double s = z * x;
            r = y + z * (s * (r + v) + y);
            w = x + (r += T[0] * s);
            if (ix >= 1072010280) {
                v = iy;
                return (double)(1 - (hx >> 30 & 2)) * (v - 2.0 * (x - (w * w / (w + v) - r)));
            }
            if (iy == 1) {
                return w;
            }
            z = w;
            z = FdLibm.__LO(z, 0);
            v = r - (z - x);
            double t = a = -1.0 / w;
            t = FdLibm.__LO(t, 0);
            s = 1.0 + t * z;
            return t + a * (s + t * v);
        }
    }

    static final class Cos {
        private static final double C1 = 0.0416666666666666;
        private static final double C2 = -0.001388888888887411;
        private static final double C3 = 2.480158728947673E-5;
        private static final double C4 = -2.7557314351390663E-7;
        private static final double C5 = 2.087572321298175E-9;
        private static final double C6 = -1.1359647557788195E-11;

        private Cos() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            double[] y = new double[2];
            double z = 0.0;
            int ix = FdLibm.__HI(x);
            if ((ix &= Integer.MAX_VALUE) <= 1072243195) {
                return Cos.__kernel_cos(x, z);
            }
            if (ix >= 0x7FF00000) {
                return x - x;
            }
            int n = RemPio2.__ieee754_rem_pio2(x, y);
            switch (n & 3) {
                case 0: {
                    return Cos.__kernel_cos(y[0], y[1]);
                }
                case 1: {
                    return -Sin.__kernel_sin(y[0], y[1], 1);
                }
                case 2: {
                    return -Cos.__kernel_cos(y[0], y[1]);
                }
            }
            return Sin.__kernel_sin(y[0], y[1], 1);
        }

        static double __kernel_cos(double x, double y) {
            double qx = 0.0;
            int ix = FdLibm.__HI(x) & Integer.MAX_VALUE;
            if (ix < 1044381696 && (int)x == 0) {
                return 1.0;
            }
            double z = x * x;
            double r = z * (0.0416666666666666 + z * (-0.001388888888887411 + z * (2.480158728947673E-5 + z * (-2.7557314351390663E-7 + z * (2.087572321298175E-9 + z * -1.1359647557788195E-11)))));
            if (ix < 0x3FD33333) {
                return 1.0 - (0.5 * z - (z * r - x * y));
            }
            qx = ix > 1072234496 ? 0.28125 : FdLibm.__HI_LO(ix - 0x200000, 0);
            double hz = 0.5 * z - qx;
            double a = 1.0 - qx;
            return a - (hz - (z * r - x * y));
        }
    }

    static final class Sin {
        private static final double S1 = -0.16666666666666632;
        private static final double S2 = 0.00833333333332249;
        private static final double S3 = -1.984126982985795E-4;
        private static final double S4 = 2.7557313707070068E-6;
        private static final double S5 = -2.5050760253406863E-8;
        private static final double S6 = 1.58969099521155E-10;

        private Sin() {
            throw new UnsupportedOperationException();
        }

        static double compute(double x) {
            double[] y = new double[2];
            double z = 0.0;
            int ix = FdLibm.__HI(x);
            if ((ix &= Integer.MAX_VALUE) <= 1072243195) {
                return Sin.__kernel_sin(x, z, 0);
            }
            if (ix >= 0x7FF00000) {
                return x - x;
            }
            int n = RemPio2.__ieee754_rem_pio2(x, y);
            switch (n & 3) {
                case 0: {
                    return Sin.__kernel_sin(y[0], y[1], 1);
                }
                case 1: {
                    return Cos.__kernel_cos(y[0], y[1]);
                }
                case 2: {
                    return -Sin.__kernel_sin(y[0], y[1], 1);
                }
            }
            return -Cos.__kernel_cos(y[0], y[1]);
        }

        static double __kernel_sin(double x, double y, int iy) {
            int ix = FdLibm.__HI(x) & Integer.MAX_VALUE;
            if (ix < 1044381696 && (int)x == 0) {
                return x;
            }
            double z = x * x;
            double v = z * x;
            double r = 0.00833333333332249 + z * (-1.984126982985795E-4 + z * (2.7557313707070068E-6 + z * (-2.5050760253406863E-8 + z * 1.58969099521155E-10)));
            if (iy == 0) {
                return x + v * (-0.16666666666666632 + z * r);
            }
            return x - (z * (0.5 * y - v * r) - y - v * -0.16666666666666632);
        }
    }
}

