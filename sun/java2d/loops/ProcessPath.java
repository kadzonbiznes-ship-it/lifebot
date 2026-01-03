/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ProcessPath {
    public static final int PH_MODE_DRAW_CLIP = 0;
    public static final int PH_MODE_FILL_CLIP = 1;
    public static EndSubPathHandler noopEndSubPathHandler = new EndSubPathHandler(){

        @Override
        public void processEndSubPath() {
        }
    };
    private static final float UPPER_BND = 8.5070587E37f;
    private static final float LOWER_BND = -8.5070587E37f;
    private static final int FWD_PREC = 7;
    private static final int MDP_PREC = 10;
    private static final int MDP_MULT = 1024;
    private static final int MDP_HALF_MULT = 512;
    private static final int UPPER_OUT_BND = 0x100000;
    private static final int LOWER_OUT_BND = -1048576;
    private static final float CALC_UBND = 1048576.0f;
    private static final float CALC_LBND = -1048576.0f;
    public static final int EPSFX = 1;
    public static final float EPSF = 9.765625E-4f;
    private static final int MDP_W_MASK = -1024;
    private static final int MDP_F_MASK = 1023;
    private static final int MAX_CUB_SIZE = 256;
    private static final int MAX_QUAD_SIZE = 1024;
    private static final int DF_CUB_STEPS = 3;
    private static final int DF_QUAD_STEPS = 2;
    private static final int DF_CUB_SHIFT = 6;
    private static final int DF_QUAD_SHIFT = 1;
    private static final int DF_CUB_COUNT = 8;
    private static final int DF_QUAD_COUNT = 4;
    private static final int DF_CUB_DEC_BND = 262144;
    private static final int DF_CUB_INC_BND = 32768;
    private static final int DF_QUAD_DEC_BND = 8192;
    private static final int DF_QUAD_INC_BND = 1024;
    private static final int CUB_A_SHIFT = 7;
    private static final int CUB_B_SHIFT = 11;
    private static final int CUB_C_SHIFT = 13;
    private static final int CUB_A_MDP_MULT = 128;
    private static final int CUB_B_MDP_MULT = 2048;
    private static final int CUB_C_MDP_MULT = 8192;
    private static final int QUAD_A_SHIFT = 7;
    private static final int QUAD_B_SHIFT = 9;
    private static final int QUAD_A_MDP_MULT = 128;
    private static final int QUAD_B_MDP_MULT = 512;
    private static final int CRES_MIN_CLIPPED = 0;
    private static final int CRES_MAX_CLIPPED = 1;
    private static final int CRES_NOT_CLIPPED = 3;
    private static final int CRES_INVISIBLE = 4;
    private static final int DF_MAX_POINT = 256;

    public static boolean fillPath(DrawHandler dhnd, Path2D.Float p2df, int transX, int transY) {
        FillProcessHandler fhnd = new FillProcessHandler(dhnd);
        if (!ProcessPath.doProcessPath(fhnd, p2df, transX, transY)) {
            return false;
        }
        ProcessPath.FillPolygon(fhnd, p2df.getWindingRule());
        return true;
    }

    public static boolean drawPath(DrawHandler dhnd, EndSubPathHandler endSubPath, Path2D.Float p2df, int transX, int transY) {
        return ProcessPath.doProcessPath(new DrawProcessHandler(dhnd, endSubPath), p2df, transX, transY);
    }

    public static boolean drawPath(DrawHandler dhnd, Path2D.Float p2df, int transX, int transY) {
        return ProcessPath.doProcessPath(new DrawProcessHandler(dhnd, noopEndSubPathHandler), p2df, transX, transY);
    }

    private static float CLIP(float a1, float b1, float a2, float b2, double t) {
        return (float)((double)b1 + (t - (double)a1) * (double)(b2 - b1) / (double)(a2 - a1));
    }

    private static int CLIP(int a1, int b1, int a2, int b2, double t) {
        return (int)((double)b1 + (t - (double)a1) * (double)(b2 - b1) / (double)(a2 - a1));
    }

    private static boolean IS_CLIPPED(int res) {
        return res == 0 || res == 1;
    }

    private static int TESTANDCLIP(float LINE_MIN, float LINE_MAX, float[] c, int a1, int b1, int a2, int b2) {
        int res = 3;
        if (c[a1] < LINE_MIN || c[a1] > LINE_MAX) {
            double t;
            if (c[a1] < LINE_MIN) {
                if (c[a2] < LINE_MIN) {
                    return 4;
                }
                res = 0;
                t = LINE_MIN;
            } else {
                if (c[a2] > LINE_MAX) {
                    return 4;
                }
                res = 1;
                t = LINE_MAX;
            }
            c[b1] = ProcessPath.CLIP(c[a1], c[b1], c[a2], c[b2], t);
            c[a1] = (float)t;
        }
        return res;
    }

    private static int TESTANDCLIP(int LINE_MIN, int LINE_MAX, int[] c, int a1, int b1, int a2, int b2) {
        int res = 3;
        if (c[a1] < LINE_MIN || c[a1] > LINE_MAX) {
            double t;
            if (c[a1] < LINE_MIN) {
                if (c[a2] < LINE_MIN) {
                    return 4;
                }
                res = 0;
                t = LINE_MIN;
            } else {
                if (c[a2] > LINE_MAX) {
                    return 4;
                }
                res = 1;
                t = LINE_MAX;
            }
            c[b1] = ProcessPath.CLIP(c[a1], c[b1], c[a2], c[b2], t);
            c[a1] = (int)t;
        }
        return res;
    }

    private static int CLIPCLAMP(float LINE_MIN, float LINE_MAX, float[] c, int a1, int b1, int a2, int b2, int a3, int b3) {
        c[a3] = c[a1];
        c[b3] = c[b1];
        int res = ProcessPath.TESTANDCLIP(LINE_MIN, LINE_MAX, c, a1, b1, a2, b2);
        if (res == 0) {
            c[a3] = c[a1];
        } else if (res == 1) {
            c[a3] = c[a1];
            res = 1;
        } else if (res == 4) {
            if (c[a1] > LINE_MAX) {
                res = 4;
            } else {
                c[a1] = LINE_MIN;
                c[a2] = LINE_MIN;
                res = 3;
            }
        }
        return res;
    }

    private static int CLIPCLAMP(int LINE_MIN, int LINE_MAX, int[] c, int a1, int b1, int a2, int b2, int a3, int b3) {
        c[a3] = c[a1];
        c[b3] = c[b1];
        int res = ProcessPath.TESTANDCLIP(LINE_MIN, LINE_MAX, c, a1, b1, a2, b2);
        if (res == 0) {
            c[a3] = c[a1];
        } else if (res == 1) {
            c[a3] = c[a1];
            res = 1;
        } else if (res == 4) {
            if (c[a1] > LINE_MAX) {
                res = 4;
            } else {
                c[a1] = LINE_MIN;
                c[a2] = LINE_MIN;
                res = 3;
            }
        }
        return res;
    }

    private static void DrawMonotonicQuad(ProcessHandler hnd, float[] coords, boolean checkBounds, int[] pixelInfo) {
        int x0 = (int)(coords[0] * 1024.0f);
        int y0 = (int)(coords[1] * 1024.0f);
        int xe = (int)(coords[4] * 1024.0f);
        int ye = (int)(coords[5] * 1024.0f);
        int px = (x0 & 0x3FF) << 1;
        int py = (y0 & 0x3FF) << 1;
        int count = 4;
        int shift = 1;
        int ax = (int)((coords[0] - 2.0f * coords[2] + coords[4]) * 128.0f);
        int ay = (int)((coords[1] - 2.0f * coords[3] + coords[5]) * 128.0f);
        int bx = (int)((-2.0f * coords[0] + 2.0f * coords[2]) * 512.0f);
        int by = (int)((-2.0f * coords[1] + 2.0f * coords[3]) * 512.0f);
        int ddpx = 2 * ax;
        int ddpy = 2 * ay;
        int dpx = ax + bx;
        int dpy = ay + by;
        int x2 = x0;
        int y2 = y0;
        int maxDD = Math.max(Math.abs(ddpx), Math.abs(ddpy));
        int dx = xe - x0;
        int dy = ye - y0;
        int x0w = x0 & 0xFFFFFC00;
        int y0w = y0 & 0xFFFFFC00;
        while (maxDD > 8192) {
            dpx = (dpx << 1) - ax;
            dpy = (dpy << 1) - ay;
            count <<= 1;
            maxDD >>= 2;
            px <<= 2;
            py <<= 2;
            shift += 2;
        }
        while (count-- > 1) {
            px += dpx;
            py += dpy;
            dpx += ddpx;
            dpy += ddpy;
            int x1 = x2;
            int y1 = y2;
            x2 = x0w + (px >> shift);
            y2 = y0w + (py >> shift);
            if ((xe - x2 ^ dx) < 0) {
                x2 = xe;
            }
            if ((ye - y2 ^ dy) < 0) {
                y2 = ye;
            }
            hnd.processFixedLine(x1, y1, x2, y2, pixelInfo, checkBounds, false);
        }
        hnd.processFixedLine(x2, y2, xe, ye, pixelInfo, checkBounds, false);
    }

    private static void ProcessMonotonicQuad(ProcessHandler hnd, float[] coords, int[] pixelInfo) {
        float yMax;
        float xMax;
        float[] coords1 = new float[6];
        float xMin = xMax = coords[0];
        float yMin = yMax = coords[1];
        for (int i = 2; i < 6; i += 2) {
            xMin = xMin > coords[i] ? coords[i] : xMin;
            xMax = xMax < coords[i] ? coords[i] : xMax;
            yMin = yMin > coords[i + 1] ? coords[i + 1] : yMin;
            yMax = yMax < coords[i + 1] ? coords[i + 1] : yMax;
        }
        if (hnd.clipMode == 0) {
            if (hnd.dhnd.xMaxf < xMin || hnd.dhnd.xMinf > xMax || hnd.dhnd.yMaxf < yMin || hnd.dhnd.yMinf > yMax) {
                return;
            }
        } else {
            if (hnd.dhnd.yMaxf < yMin || hnd.dhnd.yMinf > yMax || hnd.dhnd.xMaxf < xMin) {
                return;
            }
            if (hnd.dhnd.xMinf > xMax) {
                coords[2] = coords[4] = hnd.dhnd.xMinf;
                coords[0] = coords[4];
            }
        }
        if (xMax - xMin > 1024.0f || yMax - yMin > 1024.0f) {
            coords1[4] = coords[4];
            coords1[5] = coords[5];
            coords1[2] = (coords[2] + coords[4]) / 2.0f;
            coords1[3] = (coords[3] + coords[5]) / 2.0f;
            coords[2] = (coords[0] + coords[2]) / 2.0f;
            coords[3] = (coords[1] + coords[3]) / 2.0f;
            coords[4] = coords1[0] = (coords[2] + coords1[2]) / 2.0f;
            coords[5] = coords1[1] = (coords[3] + coords1[3]) / 2.0f;
            ProcessPath.ProcessMonotonicQuad(hnd, coords, pixelInfo);
            ProcessPath.ProcessMonotonicQuad(hnd, coords1, pixelInfo);
        } else {
            ProcessPath.DrawMonotonicQuad(hnd, coords, hnd.dhnd.xMinf >= xMin || hnd.dhnd.xMaxf <= xMax || hnd.dhnd.yMinf >= yMin || hnd.dhnd.yMaxf <= yMax, pixelInfo);
        }
    }

    private static void ProcessQuad(ProcessHandler hnd, float[] coords, int[] pixelInfo) {
        double by;
        double ay;
        double bx;
        double param;
        double ax;
        double[] params = new double[2];
        int cnt = 0;
        if ((coords[0] > coords[2] || coords[2] > coords[4]) && (coords[0] < coords[2] || coords[2] < coords[4]) && (ax = (double)(coords[0] - 2.0f * coords[2] + coords[4])) != 0.0 && (param = (bx = (double)(coords[0] - coords[2])) / ax) < 1.0 && param > 0.0) {
            params[cnt++] = param;
        }
        if ((coords[1] > coords[3] || coords[3] > coords[5]) && (coords[1] < coords[3] || coords[3] < coords[5]) && (ay = (double)(coords[1] - 2.0f * coords[3] + coords[5])) != 0.0 && (param = (by = (double)(coords[1] - coords[3])) / ay) < 1.0 && param > 0.0) {
            if (cnt > 0) {
                if (params[0] > param) {
                    params[cnt++] = params[0];
                    params[0] = param;
                } else if (params[0] < param) {
                    params[cnt++] = param;
                }
            } else {
                params[cnt++] = param;
            }
        }
        switch (cnt) {
            case 0: {
                break;
            }
            case 1: {
                ProcessPath.ProcessFirstMonotonicPartOfQuad(hnd, coords, pixelInfo, (float)params[0]);
                break;
            }
            case 2: {
                ProcessPath.ProcessFirstMonotonicPartOfQuad(hnd, coords, pixelInfo, (float)params[0]);
                param = params[1] - params[0];
                if (!(param > 0.0)) break;
                ProcessPath.ProcessFirstMonotonicPartOfQuad(hnd, coords, pixelInfo, (float)(param / (1.0 - params[0])));
            }
        }
        ProcessPath.ProcessMonotonicQuad(hnd, coords, pixelInfo);
    }

    private static void ProcessFirstMonotonicPartOfQuad(ProcessHandler hnd, float[] coords, int[] pixelInfo, float t) {
        float[] coords1 = new float[6];
        coords1[0] = coords[0];
        coords1[1] = coords[1];
        coords1[2] = coords[0] + t * (coords[2] - coords[0]);
        coords1[3] = coords[1] + t * (coords[3] - coords[1]);
        coords[2] = coords[2] + t * (coords[4] - coords[2]);
        coords[3] = coords[3] + t * (coords[5] - coords[3]);
        coords[0] = coords1[4] = coords1[2] + t * (coords[2] - coords1[2]);
        coords[1] = coords1[5] = coords1[3] + t * (coords[3] - coords1[3]);
        ProcessPath.ProcessMonotonicQuad(hnd, coords1, pixelInfo);
    }

    private static void DrawMonotonicCubic(ProcessHandler hnd, float[] coords, boolean checkBounds, int[] pixelInfo) {
        int x0 = (int)(coords[0] * 1024.0f);
        int y0 = (int)(coords[1] * 1024.0f);
        int xe = (int)(coords[6] * 1024.0f);
        int ye = (int)(coords[7] * 1024.0f);
        int px = (x0 & 0x3FF) << 6;
        int py = (y0 & 0x3FF) << 6;
        int incStepBnd = 32768;
        int decStepBnd = 262144;
        int count = 8;
        int shift = 6;
        int ax = (int)((-coords[0] + 3.0f * coords[2] - 3.0f * coords[4] + coords[6]) * 128.0f);
        int ay = (int)((-coords[1] + 3.0f * coords[3] - 3.0f * coords[5] + coords[7]) * 128.0f);
        int bx = (int)((3.0f * coords[0] - 6.0f * coords[2] + 3.0f * coords[4]) * 2048.0f);
        int by = (int)((3.0f * coords[1] - 6.0f * coords[3] + 3.0f * coords[5]) * 2048.0f);
        int cx = (int)((-3.0f * coords[0] + 3.0f * coords[2]) * 8192.0f);
        int cy = (int)((-3.0f * coords[1] + 3.0f * coords[3]) * 8192.0f);
        int dddpx = 6 * ax;
        int dddpy = 6 * ay;
        int ddpx = dddpx + bx;
        int ddpy = dddpy + by;
        int dpx = ax + (bx >> 1) + cx;
        int dpy = ay + (by >> 1) + cy;
        int x2 = x0;
        int y2 = y0;
        int x0w = x0 & 0xFFFFFC00;
        int y0w = y0 & 0xFFFFFC00;
        int dx = xe - x0;
        int dy = ye - y0;
        while (count > 0) {
            while (Math.abs(ddpx) > decStepBnd || Math.abs(ddpy) > decStepBnd) {
                ddpx = (ddpx << 1) - dddpx;
                ddpy = (ddpy << 1) - dddpy;
                dpx = (dpx << 2) - (ddpx >> 1);
                dpy = (dpy << 2) - (ddpy >> 1);
                count <<= 1;
                decStepBnd <<= 3;
                incStepBnd <<= 3;
                px <<= 3;
                py <<= 3;
                shift += 3;
            }
            while ((count & 1) == 0 && shift > 6 && Math.abs(dpx) <= incStepBnd && Math.abs(dpy) <= incStepBnd) {
                dpx = (dpx >> 2) + (ddpx >> 3);
                dpy = (dpy >> 2) + (ddpy >> 3);
                ddpx = ddpx + dddpx >> 1;
                ddpy = ddpy + dddpy >> 1;
                count >>= 1;
                decStepBnd >>= 3;
                incStepBnd >>= 3;
                px >>= 3;
                py >>= 3;
                shift -= 3;
            }
            if (--count > 0) {
                px += dpx;
                py += dpy;
                dpx += ddpx;
                dpy += ddpy;
                ddpx += dddpx;
                ddpy += dddpy;
                int x1 = x2;
                int y1 = y2;
                x2 = x0w + (px >> shift);
                y2 = y0w + (py >> shift);
                if ((xe - x2 ^ dx) < 0) {
                    x2 = xe;
                }
                if ((ye - y2 ^ dy) < 0) {
                    y2 = ye;
                }
                hnd.processFixedLine(x1, y1, x2, y2, pixelInfo, checkBounds, false);
                continue;
            }
            hnd.processFixedLine(x2, y2, xe, ye, pixelInfo, checkBounds, false);
        }
    }

    private static void ProcessMonotonicCubic(ProcessHandler hnd, float[] coords, int[] pixelInfo) {
        float yMax;
        float xMax;
        float[] coords1 = new float[8];
        float xMin = xMax = coords[0];
        float yMin = yMax = coords[1];
        for (int i = 2; i < 8; i += 2) {
            xMin = xMin > coords[i] ? coords[i] : xMin;
            xMax = xMax < coords[i] ? coords[i] : xMax;
            yMin = yMin > coords[i + 1] ? coords[i + 1] : yMin;
            yMax = yMax < coords[i + 1] ? coords[i + 1] : yMax;
        }
        if (hnd.clipMode == 0) {
            if (hnd.dhnd.xMaxf < xMin || hnd.dhnd.xMinf > xMax || hnd.dhnd.yMaxf < yMin || hnd.dhnd.yMinf > yMax) {
                return;
            }
        } else {
            if (hnd.dhnd.yMaxf < yMin || hnd.dhnd.yMinf > yMax || hnd.dhnd.xMaxf < xMin) {
                return;
            }
            if (hnd.dhnd.xMinf > xMax) {
                coords[4] = coords[6] = hnd.dhnd.xMinf;
                coords[2] = coords[6];
                coords[0] = coords[6];
            }
        }
        if (xMax - xMin > 256.0f || yMax - yMin > 256.0f) {
            coords1[6] = coords[6];
            coords1[7] = coords[7];
            coords1[4] = (coords[4] + coords[6]) / 2.0f;
            coords1[5] = (coords[5] + coords[7]) / 2.0f;
            float tx = (coords[2] + coords[4]) / 2.0f;
            float ty = (coords[3] + coords[5]) / 2.0f;
            coords1[2] = (tx + coords1[4]) / 2.0f;
            coords1[3] = (ty + coords1[5]) / 2.0f;
            coords[2] = (coords[0] + coords[2]) / 2.0f;
            coords[3] = (coords[1] + coords[3]) / 2.0f;
            coords[4] = (coords[2] + tx) / 2.0f;
            coords[5] = (coords[3] + ty) / 2.0f;
            coords[6] = coords1[0] = (coords[4] + coords1[2]) / 2.0f;
            coords[7] = coords1[1] = (coords[5] + coords1[3]) / 2.0f;
            ProcessPath.ProcessMonotonicCubic(hnd, coords, pixelInfo);
            ProcessPath.ProcessMonotonicCubic(hnd, coords1, pixelInfo);
        } else {
            ProcessPath.DrawMonotonicCubic(hnd, coords, hnd.dhnd.xMinf > xMin || hnd.dhnd.xMaxf < xMax || hnd.dhnd.yMinf > yMin || hnd.dhnd.yMaxf < yMax, pixelInfo);
        }
    }

    private static void ProcessCubic(ProcessHandler hnd, float[] coords, int[] pixelInfo) {
        int i;
        int nr;
        double[] params = new double[4];
        double[] eqn = new double[3];
        double[] res = new double[2];
        int cnt = 0;
        if ((coords[0] > coords[2] || coords[2] > coords[4] || coords[4] > coords[6]) && (coords[0] < coords[2] || coords[2] < coords[4] || coords[4] < coords[6])) {
            eqn[2] = -coords[0] + 3.0f * coords[2] - 3.0f * coords[4] + coords[6];
            eqn[1] = 2.0f * (coords[0] - 2.0f * coords[2] + coords[4]);
            eqn[0] = -coords[0] + coords[2];
            nr = QuadCurve2D.solveQuadratic(eqn, res);
            for (i = 0; i < nr; ++i) {
                if (!(res[i] > 0.0) || !(res[i] < 1.0)) continue;
                params[cnt++] = res[i];
            }
        }
        if ((coords[1] > coords[3] || coords[3] > coords[5] || coords[5] > coords[7]) && (coords[1] < coords[3] || coords[3] < coords[5] || coords[5] < coords[7])) {
            eqn[2] = -coords[1] + 3.0f * coords[3] - 3.0f * coords[5] + coords[7];
            eqn[1] = 2.0f * (coords[1] - 2.0f * coords[3] + coords[5]);
            eqn[0] = -coords[1] + coords[3];
            nr = QuadCurve2D.solveQuadratic(eqn, res);
            for (i = 0; i < nr; ++i) {
                if (!(res[i] > 0.0) || !(res[i] < 1.0)) continue;
                params[cnt++] = res[i];
            }
        }
        if (cnt > 0) {
            Arrays.sort(params, 0, cnt);
            ProcessPath.ProcessFirstMonotonicPartOfCubic(hnd, coords, pixelInfo, (float)params[0]);
            for (int i2 = 1; i2 < cnt; ++i2) {
                double param = params[i2] - params[i2 - 1];
                if (!(param > 0.0)) continue;
                ProcessPath.ProcessFirstMonotonicPartOfCubic(hnd, coords, pixelInfo, (float)(param / (1.0 - params[i2 - 1])));
            }
        }
        ProcessPath.ProcessMonotonicCubic(hnd, coords, pixelInfo);
    }

    private static void ProcessFirstMonotonicPartOfCubic(ProcessHandler hnd, float[] coords, int[] pixelInfo, float t) {
        float[] coords1 = new float[8];
        coords1[0] = coords[0];
        coords1[1] = coords[1];
        float tx = coords[2] + t * (coords[4] - coords[2]);
        float ty = coords[3] + t * (coords[5] - coords[3]);
        coords1[2] = coords[0] + t * (coords[2] - coords[0]);
        coords1[3] = coords[1] + t * (coords[3] - coords[1]);
        coords1[4] = coords1[2] + t * (tx - coords1[2]);
        coords1[5] = coords1[3] + t * (ty - coords1[3]);
        coords[4] = coords[4] + t * (coords[6] - coords[4]);
        coords[5] = coords[5] + t * (coords[7] - coords[5]);
        coords[2] = tx + t * (coords[4] - tx);
        coords[3] = ty + t * (coords[5] - ty);
        coords[0] = coords1[6] = coords1[4] + t * (coords[2] - coords1[4]);
        coords[1] = coords1[7] = coords1[5] + t * (coords[3] - coords1[5]);
        ProcessPath.ProcessMonotonicCubic(hnd, coords1, pixelInfo);
    }

    private static void ProcessLine(ProcessHandler hnd, float x1, float y1, float x2, float y2, int[] pixelInfo) {
        boolean clipped = false;
        float[] c = new float[]{x1, y1, x2, y2, 0.0f, 0.0f};
        float xMin = hnd.dhnd.xMinf;
        float yMin = hnd.dhnd.yMinf;
        float xMax = hnd.dhnd.xMaxf;
        float yMax = hnd.dhnd.yMaxf;
        int res = ProcessPath.TESTANDCLIP(yMin, yMax, c, 1, 0, 3, 2);
        if (res == 4) {
            return;
        }
        clipped = ProcessPath.IS_CLIPPED(res);
        res = ProcessPath.TESTANDCLIP(yMin, yMax, c, 3, 2, 1, 0);
        if (res == 4) {
            return;
        }
        boolean lastClipped = ProcessPath.IS_CLIPPED(res);
        boolean bl = clipped = clipped || lastClipped;
        if (hnd.clipMode == 0) {
            res = ProcessPath.TESTANDCLIP(xMin, xMax, c, 0, 1, 2, 3);
            if (res == 4) {
                return;
            }
            clipped = clipped || ProcessPath.IS_CLIPPED(res);
            res = ProcessPath.TESTANDCLIP(xMin, xMax, c, 2, 3, 0, 1);
            if (res == 4) {
                return;
            }
            lastClipped = lastClipped || ProcessPath.IS_CLIPPED(res);
            clipped = clipped || lastClipped;
            int X1 = (int)(c[0] * 1024.0f);
            int Y1 = (int)(c[1] * 1024.0f);
            int X2 = (int)(c[2] * 1024.0f);
            int Y2 = (int)(c[3] * 1024.0f);
            hnd.processFixedLine(X1, Y1, X2, Y2, pixelInfo, clipped, lastClipped);
        } else {
            int Y3;
            int X3;
            res = ProcessPath.CLIPCLAMP(xMin, xMax, c, 0, 1, 2, 3, 4, 5);
            int X1 = (int)(c[0] * 1024.0f);
            int Y1 = (int)(c[1] * 1024.0f);
            if (res == 0) {
                X3 = (int)(c[4] * 1024.0f);
                Y3 = (int)(c[5] * 1024.0f);
                hnd.processFixedLine(X3, Y3, X1, Y1, pixelInfo, false, lastClipped);
            } else if (res == 4) {
                return;
            }
            res = ProcessPath.CLIPCLAMP(xMin, xMax, c, 2, 3, 0, 1, 4, 5);
            lastClipped = lastClipped || res == 1;
            int X2 = (int)(c[2] * 1024.0f);
            int Y2 = (int)(c[3] * 1024.0f);
            hnd.processFixedLine(X1, Y1, X2, Y2, pixelInfo, false, lastClipped);
            if (res == 0) {
                X3 = (int)(c[4] * 1024.0f);
                Y3 = (int)(c[5] * 1024.0f);
                hnd.processFixedLine(X2, Y2, X3, Y3, pixelInfo, false, lastClipped);
            }
        }
    }

    private static boolean doProcessPath(ProcessHandler hnd, Path2D.Float p2df, float transXf, float transYf) {
        float[] coords = new float[8];
        float[] tCoords = new float[8];
        float[] closeCoord = new float[]{0.0f, 0.0f};
        int[] pixelInfo = new int[5];
        boolean subpathStarted = false;
        boolean skip = false;
        pixelInfo[0] = 0;
        hnd.dhnd.adjustBounds(-1048576, -1048576, 0x100000, 0x100000);
        if (hnd.dhnd.strokeControl == 2) {
            closeCoord[0] = -0.5f;
            closeCoord[1] = -0.5f;
            transXf = (float)((double)transXf - 0.5);
            transYf = (float)((double)transYf - 0.5);
        }
        PathIterator pi = p2df.getPathIterator(null);
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case 0: {
                    if (subpathStarted && !skip) {
                        if (hnd.clipMode == 1 && (tCoords[0] != closeCoord[0] || tCoords[1] != closeCoord[1])) {
                            ProcessPath.ProcessLine(hnd, tCoords[0], tCoords[1], closeCoord[0], closeCoord[1], pixelInfo);
                        }
                        hnd.processEndSubPath();
                    }
                    tCoords[0] = coords[0] + transXf;
                    tCoords[1] = coords[1] + transYf;
                    if (tCoords[0] < 8.5070587E37f && tCoords[0] > -8.5070587E37f && tCoords[1] < 8.5070587E37f && tCoords[1] > -8.5070587E37f) {
                        subpathStarted = true;
                        skip = false;
                        closeCoord[0] = tCoords[0];
                        closeCoord[1] = tCoords[1];
                    } else {
                        skip = true;
                    }
                    pixelInfo[0] = 0;
                    break;
                }
                case 1: {
                    float lastX = tCoords[2] = coords[0] + transXf;
                    float lastY = tCoords[3] = coords[1] + transYf;
                    if (!(lastX < 8.5070587E37f) || !(lastX > -8.5070587E37f) || !(lastY < 8.5070587E37f) || !(lastY > -8.5070587E37f)) break;
                    if (skip) {
                        tCoords[0] = closeCoord[0] = lastX;
                        tCoords[1] = closeCoord[1] = lastY;
                        subpathStarted = true;
                        skip = false;
                        break;
                    }
                    ProcessPath.ProcessLine(hnd, tCoords[0], tCoords[1], tCoords[2], tCoords[3], pixelInfo);
                    tCoords[0] = lastX;
                    tCoords[1] = lastY;
                    break;
                }
                case 2: {
                    tCoords[2] = coords[0] + transXf;
                    tCoords[3] = coords[1] + transYf;
                    float lastX = tCoords[4] = coords[2] + transXf;
                    float lastY = tCoords[5] = coords[3] + transYf;
                    if (!(lastX < 8.5070587E37f) || !(lastX > -8.5070587E37f) || !(lastY < 8.5070587E37f) || !(lastY > -8.5070587E37f)) break;
                    if (skip) {
                        tCoords[0] = closeCoord[0] = lastX;
                        tCoords[1] = closeCoord[1] = lastY;
                        subpathStarted = true;
                        skip = false;
                        break;
                    }
                    if (tCoords[2] < 8.5070587E37f && tCoords[2] > -8.5070587E37f && tCoords[3] < 8.5070587E37f && tCoords[3] > -8.5070587E37f) {
                        ProcessPath.ProcessQuad(hnd, tCoords, pixelInfo);
                    } else {
                        ProcessPath.ProcessLine(hnd, tCoords[0], tCoords[1], tCoords[4], tCoords[5], pixelInfo);
                    }
                    tCoords[0] = lastX;
                    tCoords[1] = lastY;
                    break;
                }
                case 3: {
                    tCoords[2] = coords[0] + transXf;
                    tCoords[3] = coords[1] + transYf;
                    tCoords[4] = coords[2] + transXf;
                    tCoords[5] = coords[3] + transYf;
                    float lastX = tCoords[6] = coords[4] + transXf;
                    float lastY = tCoords[7] = coords[5] + transYf;
                    if (!(lastX < 8.5070587E37f) || !(lastX > -8.5070587E37f) || !(lastY < 8.5070587E37f) || !(lastY > -8.5070587E37f)) break;
                    if (skip) {
                        tCoords[0] = closeCoord[0] = tCoords[6];
                        tCoords[1] = closeCoord[1] = tCoords[7];
                        subpathStarted = true;
                        skip = false;
                        break;
                    }
                    if (tCoords[2] < 8.5070587E37f && tCoords[2] > -8.5070587E37f && tCoords[3] < 8.5070587E37f && tCoords[3] > -8.5070587E37f && tCoords[4] < 8.5070587E37f && tCoords[4] > -8.5070587E37f && tCoords[5] < 8.5070587E37f && tCoords[5] > -8.5070587E37f) {
                        ProcessPath.ProcessCubic(hnd, tCoords, pixelInfo);
                    } else {
                        ProcessPath.ProcessLine(hnd, tCoords[0], tCoords[1], tCoords[6], tCoords[7], pixelInfo);
                    }
                    tCoords[0] = lastX;
                    tCoords[1] = lastY;
                    break;
                }
                case 4: {
                    if (!subpathStarted || skip) break;
                    skip = false;
                    if (tCoords[0] != closeCoord[0] || tCoords[1] != closeCoord[1]) {
                        ProcessPath.ProcessLine(hnd, tCoords[0], tCoords[1], closeCoord[0], closeCoord[1], pixelInfo);
                        tCoords[0] = closeCoord[0];
                        tCoords[1] = closeCoord[1];
                    }
                    hnd.processEndSubPath();
                }
            }
            pi.next();
        }
        if (subpathStarted & !skip) {
            if (hnd.clipMode == 1 && (tCoords[0] != closeCoord[0] || tCoords[1] != closeCoord[1])) {
                ProcessPath.ProcessLine(hnd, tCoords[0], tCoords[1], closeCoord[0], closeCoord[1], pixelInfo);
            }
            hnd.processEndSubPath();
        }
        return true;
    }

    private static void FillPolygon(FillProcessHandler hnd, int fillRule) {
        int rightBnd = hnd.dhnd.xMax - 1;
        FillData fd = hnd.fd;
        int yMin = fd.plgYMin;
        int yMax = fd.plgYMax;
        int hashSize = (yMax - yMin >> 10) + 4;
        int hashOffset = yMin - 1 & 0xFFFFFC00;
        int counterMask = fillRule == 1 ? -1 : 1;
        List<Point> pnts = fd.plgPnts;
        int n = pnts.size();
        if (n <= 1) {
            return;
        }
        Point[] yHash = new Point[hashSize];
        Point curpt = pnts.get(0);
        curpt.prev = null;
        for (int i = 0; i < n - 1; ++i) {
            curpt = pnts.get(i);
            Point nextpt = pnts.get(i + 1);
            int curHashInd = curpt.y - hashOffset - 1 >> 10;
            curpt.nextByY = yHash[curHashInd];
            yHash[curHashInd] = curpt;
            curpt.next = nextpt;
            nextpt.prev = curpt;
        }
        Point ept = pnts.get(n - 1);
        int curHashInd = ept.y - hashOffset - 1 >> 10;
        ept.nextByY = yHash[curHashInd];
        yHash[curHashInd] = ept;
        ActiveEdgeList activeList = new ActiveEdgeList();
        int y = hashOffset + 1024;
        for (int k = 0; y <= yMax && k < hashSize; y += 1024, ++k) {
            int xr;
            Point pt = yHash[k];
            while (pt != null) {
                if (pt.prev != null && !pt.prev.lastPoint) {
                    if (pt.prev.edge != null && pt.prev.y <= y) {
                        activeList.delete(pt.prev.edge);
                        pt.prev.edge = null;
                    } else if (pt.prev.y > y) {
                        activeList.insert(pt.prev, y);
                    }
                }
                if (!pt.lastPoint && pt.next != null) {
                    if (pt.edge != null && pt.next.y <= y) {
                        activeList.delete(pt.edge);
                        pt.edge = null;
                    } else if (pt.next.y > y) {
                        activeList.insert(pt, y);
                    }
                }
                pt = pt.nextByY;
            }
            if (activeList.isEmpty()) continue;
            activeList.sort();
            int counter = 0;
            boolean drawing = false;
            int xl = xr = hnd.dhnd.xMin;
            Edge curEdge = activeList.head;
            while (curEdge != null) {
                if (((counter += curEdge.dir) & counterMask) != 0 && !drawing) {
                    xl = curEdge.x + 1024 - 1 >> 10;
                    drawing = true;
                }
                if ((counter & counterMask) == 0 && drawing) {
                    xr = curEdge.x - 1 >> 10;
                    if (xl <= xr) {
                        hnd.dhnd.drawScanline(xl, xr, y >> 10);
                    }
                    drawing = false;
                }
                curEdge.x += curEdge.dx;
                curEdge = curEdge.next;
            }
            if (!drawing || xl > rightBnd) continue;
            hnd.dhnd.drawScanline(xl, rightBnd, y >> 10);
        }
    }

    private static class FillProcessHandler
    extends ProcessHandler {
        FillData fd = new FillData();

        @Override
        public void processFixedLine(int x1, int y1, int x2, int y2, int[] pixelInfo, boolean checkBounds, boolean endSubPath) {
            if (checkBounds) {
                int[] c = new int[]{x1, y1, x2, y2, 0, 0};
                int outXMin = (int)(this.dhnd.xMinf * 1024.0f);
                int outXMax = (int)(this.dhnd.xMaxf * 1024.0f);
                int outYMin = (int)(this.dhnd.yMinf * 1024.0f);
                int outYMax = (int)(this.dhnd.yMaxf * 1024.0f);
                int res = ProcessPath.TESTANDCLIP(outYMin, outYMax, c, 1, 0, 3, 2);
                if (res == 4) {
                    return;
                }
                res = ProcessPath.TESTANDCLIP(outYMin, outYMax, c, 3, 2, 1, 0);
                if (res == 4) {
                    return;
                }
                boolean lastClipped = ProcessPath.IS_CLIPPED(res);
                res = ProcessPath.CLIPCLAMP(outXMin, outXMax, c, 0, 1, 2, 3, 4, 5);
                if (res == 0) {
                    this.processFixedLine(c[4], c[5], c[0], c[1], pixelInfo, false, lastClipped);
                } else if (res == 4) {
                    return;
                }
                res = ProcessPath.CLIPCLAMP(outXMin, outXMax, c, 2, 3, 0, 1, 4, 5);
                lastClipped = lastClipped || res == 1;
                this.processFixedLine(c[0], c[1], c[2], c[3], pixelInfo, false, lastClipped);
                if (res == 0) {
                    this.processFixedLine(c[2], c[3], c[4], c[5], pixelInfo, false, lastClipped);
                }
                return;
            }
            if (this.fd.isEmpty() || this.fd.isEnded()) {
                this.fd.addPoint(x1, y1, false);
            }
            this.fd.addPoint(x2, y2, false);
            if (endSubPath) {
                this.fd.setEnded();
            }
        }

        FillProcessHandler(DrawHandler dhnd) {
            super(dhnd, 1);
        }

        @Override
        public void processEndSubPath() {
            if (!this.fd.isEmpty()) {
                this.fd.setEnded();
            }
        }
    }

    public static abstract class DrawHandler {
        public int xMin;
        public int yMin;
        public int xMax;
        public int yMax;
        public float xMinf;
        public float yMinf;
        public float xMaxf;
        public float yMaxf;
        public int strokeControl;

        public DrawHandler(int xMin, int yMin, int xMax, int yMax, int strokeControl) {
            this.setBounds(xMin, yMin, xMax, yMax, strokeControl);
        }

        public void setBounds(int xMin, int yMin, int xMax, int yMax) {
            this.xMin = xMin;
            this.yMin = yMin;
            this.xMax = xMax;
            this.yMax = yMax;
            this.xMinf = (float)xMin - 0.5f;
            this.yMinf = (float)yMin - 0.5f;
            this.xMaxf = (float)xMax - 0.5f - 9.765625E-4f;
            this.yMaxf = (float)yMax - 0.5f - 9.765625E-4f;
        }

        public void setBounds(int xMin, int yMin, int xMax, int yMax, int strokeControl) {
            this.strokeControl = strokeControl;
            this.setBounds(xMin, yMin, xMax, yMax);
        }

        public void adjustBounds(int bxMin, int byMin, int bxMax, int byMax) {
            if (this.xMin > bxMin) {
                bxMin = this.xMin;
            }
            if (this.xMax < bxMax) {
                bxMax = this.xMax;
            }
            if (this.yMin > byMin) {
                byMin = this.yMin;
            }
            if (this.yMax < byMax) {
                byMax = this.yMax;
            }
            this.setBounds(bxMin, byMin, bxMax, byMax);
        }

        public DrawHandler(int xMin, int yMin, int xMax, int yMax) {
            this(xMin, yMin, xMax, yMax, 0);
        }

        public abstract void drawLine(int var1, int var2, int var3, int var4);

        public abstract void drawPixel(int var1, int var2);

        public abstract void drawScanline(int var1, int var2, int var3);
    }

    public static abstract class ProcessHandler
    implements EndSubPathHandler {
        DrawHandler dhnd;
        int clipMode;

        public ProcessHandler(DrawHandler dhnd, int clipMode) {
            this.dhnd = dhnd;
            this.clipMode = clipMode;
        }

        public abstract void processFixedLine(int var1, int var2, int var3, int var4, int[] var5, boolean var6, boolean var7);
    }

    private static class DrawProcessHandler
    extends ProcessHandler {
        EndSubPathHandler processESP;

        public DrawProcessHandler(DrawHandler dhnd, EndSubPathHandler processESP) {
            super(dhnd, 0);
            this.dhnd = dhnd;
            this.processESP = processESP;
        }

        @Override
        public void processEndSubPath() {
            this.processESP.processEndSubPath();
        }

        void PROCESS_LINE(int fX0, int fY0, int fX1, int fY1, boolean checkBounds, int[] pixelInfo) {
            int X0 = fX0 >> 10;
            int X1 = fX1 >> 10;
            int Y0 = fY0 >> 10;
            int Y1 = fY1 >> 10;
            if ((X0 ^ X1 | Y0 ^ Y1) == 0) {
                if (checkBounds && (this.dhnd.yMin > Y0 || this.dhnd.yMax <= Y0 || this.dhnd.xMin > X0 || this.dhnd.xMax <= X0)) {
                    return;
                }
                if (pixelInfo[0] == 0) {
                    pixelInfo[0] = 1;
                    pixelInfo[1] = X0;
                    pixelInfo[2] = Y0;
                    pixelInfo[3] = X0;
                    pixelInfo[4] = Y0;
                    this.dhnd.drawPixel(X0, Y0);
                } else if (!(X0 == pixelInfo[3] && Y0 == pixelInfo[4] || X0 == pixelInfo[1] && Y0 == pixelInfo[2])) {
                    this.dhnd.drawPixel(X0, Y0);
                    pixelInfo[3] = X0;
                    pixelInfo[4] = Y0;
                }
                return;
            }
            if ((!checkBounds || this.dhnd.yMin <= Y0 && this.dhnd.yMax > Y0 && this.dhnd.xMin <= X0 && this.dhnd.xMax > X0) && pixelInfo[0] == 1 && (pixelInfo[1] == X0 && pixelInfo[2] == Y0 || pixelInfo[3] == X0 && pixelInfo[4] == Y0)) {
                this.dhnd.drawPixel(X0, Y0);
            }
            this.dhnd.drawLine(X0, Y0, X1, Y1);
            if (pixelInfo[0] == 0) {
                pixelInfo[0] = 1;
                pixelInfo[1] = X0;
                pixelInfo[2] = Y0;
                pixelInfo[3] = X0;
                pixelInfo[4] = Y0;
            }
            if (pixelInfo[1] == X1 && pixelInfo[2] == Y1 || pixelInfo[3] == X1 && pixelInfo[4] == Y1) {
                if (checkBounds && (this.dhnd.yMin > Y1 || this.dhnd.yMax <= Y1 || this.dhnd.xMin > X1 || this.dhnd.xMax <= X1)) {
                    return;
                }
                this.dhnd.drawPixel(X1, Y1);
            }
            pixelInfo[3] = X1;
            pixelInfo[4] = Y1;
        }

        void PROCESS_POINT(int fX, int fY, boolean checkBounds, int[] pixelInfo) {
            int _X = fX >> 10;
            int _Y = fY >> 10;
            if (checkBounds && (this.dhnd.yMin > _Y || this.dhnd.yMax <= _Y || this.dhnd.xMin > _X || this.dhnd.xMax <= _X)) {
                return;
            }
            if (pixelInfo[0] == 0) {
                pixelInfo[0] = 1;
                pixelInfo[1] = _X;
                pixelInfo[2] = _Y;
                pixelInfo[3] = _X;
                pixelInfo[4] = _Y;
                this.dhnd.drawPixel(_X, _Y);
            } else if (!(_X == pixelInfo[3] && _Y == pixelInfo[4] || _X == pixelInfo[1] && _Y == pixelInfo[2])) {
                this.dhnd.drawPixel(_X, _Y);
                pixelInfo[3] = _X;
                pixelInfo[4] = _Y;
            }
        }

        @Override
        public void processFixedLine(int x1, int y1, int x2, int y2, int[] pixelInfo, boolean checkBounds, boolean endSubPath) {
            int ry2;
            int ry1;
            int rx2;
            int rx1;
            int c = x1 ^ x2 | y1 ^ y2;
            if ((c & 0xFFFFFC00) == 0) {
                if (c == 0) {
                    this.PROCESS_POINT(x1 + 512, y1 + 512, checkBounds, pixelInfo);
                }
                return;
            }
            if (x1 == x2 || y1 == y2) {
                rx1 = x1 + 512;
                rx2 = x2 + 512;
                ry1 = y1 + 512;
                ry2 = y2 + 512;
            } else {
                int cross;
                int dx = x2 - x1;
                int dy = y2 - y1;
                int fx1 = x1 & 0xFFFFFC00;
                int fy1 = y1 & 0xFFFFFC00;
                int fx2 = x2 & 0xFFFFFC00;
                int fy2 = y2 & 0xFFFFFC00;
                if (fx1 == x1 || fy1 == y1) {
                    rx1 = x1 + 512;
                    ry1 = y1 + 512;
                } else {
                    int bx1 = x1 < x2 ? fx1 + 1024 : fx1;
                    int by1 = y1 < y2 ? fy1 + 1024 : fy1;
                    cross = y1 + (bx1 - x1) * dy / dx;
                    if (cross >= fy1 && cross <= fy1 + 1024) {
                        rx1 = bx1;
                        ry1 = cross + 512;
                    } else {
                        cross = x1 + (by1 - y1) * dx / dy;
                        rx1 = cross + 512;
                        ry1 = by1;
                    }
                }
                if (fx2 == x2 || fy2 == y2) {
                    rx2 = x2 + 512;
                    ry2 = y2 + 512;
                } else {
                    int bx2 = x1 > x2 ? fx2 + 1024 : fx2;
                    int by2 = y1 > y2 ? fy2 + 1024 : fy2;
                    cross = y2 + (bx2 - x2) * dy / dx;
                    if (cross >= fy2 && cross <= fy2 + 1024) {
                        rx2 = bx2;
                        ry2 = cross + 512;
                    } else {
                        cross = x2 + (by2 - y2) * dx / dy;
                        rx2 = cross + 512;
                        ry2 = by2;
                    }
                }
            }
            this.PROCESS_LINE(rx1, ry1, rx2, ry2, checkBounds, pixelInfo);
        }
    }

    public static interface EndSubPathHandler {
        public void processEndSubPath();
    }

    private static class FillData {
        List<Point> plgPnts = new Vector<Point>(256);
        public int plgYMin;
        public int plgYMax;

        public void addPoint(int x, int y, boolean lastPoint) {
            if (this.plgPnts.size() == 0) {
                this.plgYMin = this.plgYMax = y;
            } else {
                this.plgYMin = this.plgYMin > y ? y : this.plgYMin;
                this.plgYMax = this.plgYMax < y ? y : this.plgYMax;
            }
            this.plgPnts.add(new Point(x, y, lastPoint));
        }

        public boolean isEmpty() {
            return this.plgPnts.size() == 0;
        }

        public boolean isEnded() {
            return this.plgPnts.get((int)(this.plgPnts.size() - 1)).lastPoint;
        }

        public boolean setEnded() {
            this.plgPnts.get((int)(this.plgPnts.size() - 1)).lastPoint = true;
            return true;
        }
    }

    private static class Point {
        public int x;
        public int y;
        public boolean lastPoint;
        public Point prev;
        public Point next;
        public Point nextByY;
        public Edge edge;

        public Point(int x, int y, boolean lastPoint) {
            this.x = x;
            this.y = y;
            this.lastPoint = lastPoint;
        }
    }

    private static class ActiveEdgeList {
        Edge head;

        private ActiveEdgeList() {
        }

        public boolean isEmpty() {
            return this.head == null;
        }

        public void insert(Point pnt, int cy) {
            int stepx;
            int dir;
            int dy;
            int x0;
            Point np = pnt.next;
            int X1 = pnt.x;
            int Y1 = pnt.y;
            int X2 = np.x;
            int Y2 = np.y;
            if (Y1 == Y2) {
                return;
            }
            int dX = X2 - X1;
            int dY = Y2 - Y1;
            if (Y1 < Y2) {
                x0 = X1;
                dy = cy - Y1;
                dir = -1;
            } else {
                x0 = X2;
                dy = cy - Y2;
                dir = 1;
            }
            if ((float)dX > 1048576.0f || (float)dX < -1048576.0f) {
                stepx = (int)((double)dX * 1024.0 / (double)dY);
                x0 += (int)((double)dX * (double)dy / (double)dY);
            } else {
                stepx = (dX << 10) / dY;
                x0 += dX * dy / dY;
            }
            Edge ne = new Edge(pnt, x0, stepx, dir);
            ne.next = this.head;
            ne.prev = null;
            if (this.head != null) {
                this.head.prev = ne;
            }
            this.head = pnt.edge = ne;
        }

        public void delete(Edge e) {
            Edge prevp = e.prev;
            Edge nextp = e.next;
            if (prevp != null) {
                prevp.next = nextp;
            } else {
                this.head = nextp;
            }
            if (nextp != null) {
                nextp.prev = prevp;
            }
        }

        public void sort() {
            Edge q;
            Edge p;
            Edge s = null;
            boolean wasSwap = true;
            while (s != this.head.next && wasSwap) {
                Edge r = p = this.head;
                q = p.next;
                wasSwap = false;
                while (p != s) {
                    if (p.x >= q.x) {
                        wasSwap = true;
                        if (p == this.head) {
                            temp = q.next;
                            q.next = p;
                            p.next = temp;
                            this.head = q;
                            r = q;
                        } else {
                            temp = q.next;
                            q.next = p;
                            p.next = temp;
                            r.next = q;
                            r = q;
                        }
                    } else {
                        r = p;
                        p = p.next;
                    }
                    if ((q = p.next) != s) continue;
                    s = p;
                }
            }
            p = this.head;
            q = null;
            while (p != null) {
                p.prev = q;
                q = p;
                p = p.next;
            }
        }
    }

    private static class Edge {
        int x;
        int dx;
        Point p;
        int dir;
        Edge prev;
        Edge next;

        public Edge(Point p, int x, int dx, int dir) {
            this.p = p;
            this.x = x;
            this.dx = dx;
            this.dir = dir;
        }
    }
}

