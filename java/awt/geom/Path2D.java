/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import sun.awt.geom.Curve;

public abstract sealed class Path2D
implements Shape,
Cloneable {
    public static final int WIND_EVEN_ODD = 0;
    public static final int WIND_NON_ZERO = 1;
    private static final byte SEG_MOVETO = 0;
    private static final byte SEG_LINETO = 1;
    private static final byte SEG_QUADTO = 2;
    private static final byte SEG_CUBICTO = 3;
    private static final byte SEG_CLOSE = 4;
    transient byte[] pointTypes;
    transient int numTypes;
    transient int numCoords;
    transient int windingRule;
    static final int INIT_SIZE = 20;
    static final int EXPAND_MAX = 500;
    static final int EXPAND_MAX_COORDS = 1000;
    static final int EXPAND_MIN = 10;
    private static final byte SERIAL_STORAGE_FLT_ARRAY = 48;
    private static final byte SERIAL_STORAGE_DBL_ARRAY = 49;
    private static final byte SERIAL_SEG_FLT_MOVETO = 64;
    private static final byte SERIAL_SEG_FLT_LINETO = 65;
    private static final byte SERIAL_SEG_FLT_QUADTO = 66;
    private static final byte SERIAL_SEG_FLT_CUBICTO = 67;
    private static final byte SERIAL_SEG_DBL_MOVETO = 80;
    private static final byte SERIAL_SEG_DBL_LINETO = 81;
    private static final byte SERIAL_SEG_DBL_QUADTO = 82;
    private static final byte SERIAL_SEG_DBL_CUBICTO = 83;
    private static final byte SERIAL_SEG_CLOSE = 96;
    private static final byte SERIAL_PATH_END = 97;

    Path2D() {
    }

    Path2D(int rule, int initialTypes) {
        this.setWindingRule(rule);
        this.pointTypes = new byte[initialTypes];
    }

    abstract float[] cloneCoordsFloat(AffineTransform var1);

    abstract double[] cloneCoordsDouble(AffineTransform var1);

    abstract void append(float var1, float var2);

    abstract void append(double var1, double var3);

    abstract Point2D getPoint(int var1);

    abstract void needRoom(boolean var1, int var2);

    abstract int pointCrossings(double var1, double var3);

    abstract int rectCrossings(double var1, double var3, double var5, double var7);

    static byte[] expandPointTypes(byte[] oldPointTypes, int needed) {
        int oldSize = oldPointTypes.length;
        int newSizeMin = oldSize + needed;
        if (newSizeMin < oldSize) {
            throw new ArrayIndexOutOfBoundsException("pointTypes exceeds maximum capacity !");
        }
        int grow = oldSize;
        if (grow > 500) {
            grow = Math.max(500, oldSize >> 3);
        } else if (grow < 10) {
            grow = 10;
        }
        assert (grow > 0);
        int newSize = oldSize + grow;
        if (newSize < newSizeMin) {
            newSize = Integer.MAX_VALUE;
        }
        while (true) {
            try {
                return Arrays.copyOf(oldPointTypes, newSize);
            }
            catch (OutOfMemoryError oome) {
                if (newSize == newSizeMin) {
                    throw oome;
                }
                newSize = newSizeMin + (newSize - newSizeMin) / 2;
                continue;
            }
            break;
        }
    }

    public abstract void moveTo(double var1, double var3);

    public abstract void lineTo(double var1, double var3);

    public abstract void quadTo(double var1, double var3, double var5, double var7);

    public abstract void curveTo(double var1, double var3, double var5, double var7, double var9, double var11);

    public final synchronized void closePath() {
        if (this.numTypes == 0 || this.pointTypes[this.numTypes - 1] != 4) {
            this.needRoom(true, 0);
            this.pointTypes[this.numTypes++] = 4;
        }
    }

    public final void append(Shape s, boolean connect) {
        this.append(s.getPathIterator(null), connect);
    }

    public abstract void append(PathIterator var1, boolean var2);

    public final synchronized int getWindingRule() {
        return this.windingRule;
    }

    public final void setWindingRule(int rule) {
        if (rule != 0 && rule != 1) {
            throw new IllegalArgumentException("winding rule must be WIND_EVEN_ODD or WIND_NON_ZERO");
        }
        this.windingRule = rule;
    }

    /*
     * Enabled aggressive block sorting
     */
    public final synchronized Point2D getCurrentPoint() {
        int index = this.numCoords;
        if (this.numTypes < 1) return null;
        if (index < 1) {
            return null;
        }
        if (this.pointTypes[this.numTypes - 1] != 4) return this.getPoint(index - 2);
        int i = this.numTypes - 2;
        while (i > 0) {
            switch (this.pointTypes[i]) {
                case 0: {
                    return this.getPoint(index - 2);
                }
                case 1: {
                    index -= 2;
                    break;
                }
                case 2: {
                    index -= 4;
                    break;
                }
                case 3: {
                    index -= 6;
                }
            }
            --i;
        }
        return this.getPoint(index - 2);
    }

    public final synchronized void reset() {
        this.numCoords = 0;
        this.numTypes = 0;
    }

    public abstract void transform(AffineTransform var1);

    public final synchronized Shape createTransformedShape(AffineTransform at) {
        Path2D p2d = (Path2D)this.clone();
        if (at != null) {
            p2d.transform(at);
        }
        return p2d;
    }

    @Override
    public final Rectangle getBounds() {
        return this.getBounds2D().getBounds();
    }

    static Rectangle2D getBounds2D(PathIterator pi) {
        double[] coeff = new double[4];
        double[] deriv_coeff = new double[3];
        double[] coords = new double[6];
        double[] bounds = null;
        double lastX = 0.0;
        double lastY = 0.0;
        double endX = 0.0;
        double endY = 0.0;
        double startX = 0.0;
        double startY = 0.0;
        while (!pi.isDone()) {
            block18: {
                int type = pi.currentSegment(coords);
                switch (type) {
                    case 0: {
                        if (bounds == null) {
                            bounds = new double[]{coords[0], coords[0], coords[1], coords[1]};
                        }
                        startX = endX = coords[0];
                        startY = endY = coords[1];
                        break;
                    }
                    case 1: {
                        endX = coords[0];
                        endY = coords[1];
                        break;
                    }
                    case 2: {
                        endX = coords[2];
                        endY = coords[3];
                        break;
                    }
                    case 3: {
                        endX = coords[4];
                        endY = coords[5];
                        break;
                    }
                    case 4: {
                        endX = startX;
                        endY = startY;
                        break;
                    }
                    default: {
                        break block18;
                    }
                }
                if (endX < bounds[0]) {
                    bounds[0] = endX;
                }
                if (endX > bounds[1]) {
                    bounds[1] = endX;
                }
                if (endY < bounds[2]) {
                    bounds[2] = endY;
                }
                if (endY > bounds[3]) {
                    bounds[3] = endY;
                }
                switch (type) {
                    case 2: {
                        Curve.accumulateExtremaBoundsForQuad(bounds, 0, lastX, coords[0], coords[2], coeff, deriv_coeff);
                        Curve.accumulateExtremaBoundsForQuad(bounds, 2, lastY, coords[1], coords[3], coeff, deriv_coeff);
                        break;
                    }
                    case 3: {
                        Curve.accumulateExtremaBoundsForCubic(bounds, 0, lastX, coords[0], coords[2], coords[4], coeff, deriv_coeff);
                        Curve.accumulateExtremaBoundsForCubic(bounds, 2, lastY, coords[1], coords[3], coords[5], coeff, deriv_coeff);
                        break;
                    }
                }
                lastX = endX;
                lastY = endY;
            }
            pi.next();
        }
        if (bounds != null) {
            return new Rectangle2D.Double((double)bounds[0], (double)bounds[2], (double)(bounds[1] - bounds[0]), (double)(bounds[3] - bounds[2]));
        }
        return new Rectangle2D.Double();
    }

    public static boolean contains(PathIterator pi, double x, double y) {
        if (x * 0.0 + y * 0.0 == 0.0) {
            int mask = pi.getWindingRule() == 1 ? -1 : 1;
            int cross = Curve.pointCrossingsForPath(pi, x, y);
            return (cross & mask) != 0;
        }
        return false;
    }

    public static boolean contains(PathIterator pi, Point2D p) {
        return Path2D.contains(pi, p.getX(), p.getY());
    }

    @Override
    public final boolean contains(double x, double y) {
        if (x * 0.0 + y * 0.0 == 0.0) {
            if (this.numTypes < 2) {
                return false;
            }
            int mask = this.windingRule == 1 ? -1 : 1;
            return (this.pointCrossings(x, y) & mask) != 0;
        }
        return false;
    }

    @Override
    public final boolean contains(Point2D p) {
        return this.contains(p.getX(), p.getY());
    }

    public static boolean contains(PathIterator pi, double x, double y, double w, double h) {
        if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
            return false;
        }
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        int mask = pi.getWindingRule() == 1 ? -1 : 2;
        int crossings = Curve.rectCrossingsForPath(pi, x, y, x + w, y + h);
        return crossings != Integer.MIN_VALUE && (crossings & mask) != 0;
    }

    public static boolean contains(PathIterator pi, Rectangle2D r) {
        return Path2D.contains(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public final boolean contains(double x, double y, double w, double h) {
        if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
            return false;
        }
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        int mask = this.windingRule == 1 ? -1 : 2;
        int crossings = this.rectCrossings(x, y, x + w, y + h);
        return crossings != Integer.MIN_VALUE && (crossings & mask) != 0;
    }

    @Override
    public final boolean contains(Rectangle2D r) {
        return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public static boolean intersects(PathIterator pi, double x, double y, double w, double h) {
        if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
            return false;
        }
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        int mask = pi.getWindingRule() == 1 ? -1 : 2;
        int crossings = Curve.rectCrossingsForPath(pi, x, y, x + w, y + h);
        return crossings == Integer.MIN_VALUE || (crossings & mask) != 0;
    }

    public static boolean intersects(PathIterator pi, Rectangle2D r) {
        return Path2D.intersects(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public final boolean intersects(double x, double y, double w, double h) {
        if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
            return false;
        }
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        int mask = this.windingRule == 1 ? -1 : 2;
        int crossings = this.rectCrossings(x, y, x + w, y + h);
        return crossings == Integer.MIN_VALUE || (crossings & mask) != 0;
    }

    @Override
    public final boolean intersects(Rectangle2D r) {
        return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public final PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(this.getPathIterator(at), flatness);
    }

    public abstract Object clone();

    public abstract void trimToSize();

    final void writeObject(ObjectOutputStream s, boolean isdbl) throws IOException {
        float[] fCoords;
        double[] dCoords;
        s.defaultWriteObject();
        if (isdbl) {
            dCoords = ((Double)this).doubleCoords;
            fCoords = null;
        } else {
            fCoords = ((Float)this).floatCoords;
            dCoords = null;
        }
        int numTypes = this.numTypes;
        s.writeByte(isdbl ? 49 : 48);
        s.writeInt(numTypes);
        s.writeInt(this.numCoords);
        s.writeByte((byte)this.windingRule);
        int cindex = 0;
        for (int i = 0; i < numTypes; ++i) {
            int npoints;
            s.writeByte(switch (this.pointTypes[i]) {
                case 0 -> {
                    npoints = 1;
                    yield isdbl ? 80 : 64;
                }
                case 1 -> {
                    npoints = 1;
                    yield isdbl ? 81 : 65;
                }
                case 2 -> {
                    npoints = 2;
                    yield isdbl ? 82 : 66;
                }
                case 3 -> {
                    npoints = 3;
                    yield isdbl ? 83 : 67;
                }
                case 4 -> {
                    npoints = 0;
                    yield 96;
                }
                default -> throw new InternalError("unrecognized path type");
            });
            while (--npoints >= 0) {
                if (isdbl) {
                    s.writeDouble(dCoords[cindex++]);
                    s.writeDouble(dCoords[cindex++]);
                    continue;
                }
                s.writeFloat(fCoords[cindex++]);
                s.writeFloat(fCoords[cindex++]);
            }
        }
        s.writeByte(97);
    }

    final void readObject(ObjectInputStream s, boolean storedbl) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        s.readByte();
        int nT = s.readInt();
        int nC = s.readInt();
        try {
            this.setWindingRule(s.readByte());
        }
        catch (IllegalArgumentException iae) {
            throw new InvalidObjectException(iae.getMessage());
        }
        this.pointTypes = new byte[nT < 0 || nT > 20 ? 20 : nT];
        int initX2 = 40;
        if (nC < 0 || nC > 40) {
            nC = 40;
        }
        if (storedbl) {
            ((Double)this).doubleCoords = new double[nC];
        } else {
            ((Float)this).floatCoords = new float[nC];
        }
        block14: for (int i = 0; nT < 0 || i < nT; ++i) {
            int segtype;
            int npoints;
            boolean isdbl;
            byte serialtype = s.readByte();
            switch (serialtype) {
                case 64: {
                    isdbl = false;
                    npoints = 1;
                    segtype = 0;
                    break;
                }
                case 65: {
                    isdbl = false;
                    npoints = 1;
                    segtype = 1;
                    break;
                }
                case 66: {
                    isdbl = false;
                    npoints = 2;
                    segtype = 2;
                    break;
                }
                case 67: {
                    isdbl = false;
                    npoints = 3;
                    segtype = 3;
                    break;
                }
                case 80: {
                    isdbl = true;
                    npoints = 1;
                    segtype = 0;
                    break;
                }
                case 81: {
                    isdbl = true;
                    npoints = 1;
                    segtype = 1;
                    break;
                }
                case 82: {
                    isdbl = true;
                    npoints = 2;
                    segtype = 2;
                    break;
                }
                case 83: {
                    isdbl = true;
                    npoints = 3;
                    segtype = 3;
                    break;
                }
                case 96: {
                    isdbl = false;
                    npoints = 0;
                    segtype = 4;
                    break;
                }
                case 97: {
                    if (nT < 0) break block14;
                    throw new StreamCorruptedException("unexpected PATH_END");
                }
                default: {
                    throw new StreamCorruptedException("unrecognized path type");
                }
            }
            this.needRoom(segtype != 0, npoints * 2);
            if (isdbl) {
                while (--npoints >= 0) {
                    this.append(s.readDouble(), s.readDouble());
                }
            } else {
                while (--npoints >= 0) {
                    this.append(s.readFloat(), s.readFloat());
                }
            }
            this.pointTypes[this.numTypes++] = segtype;
        }
        if (nT >= 0 && s.readByte() != 97) {
            throw new StreamCorruptedException("missing PATH_END");
        }
    }

    public static non-sealed class Double
    extends Path2D
    implements Serializable {
        transient double[] doubleCoords;
        private static final long serialVersionUID = 1826762518450014216L;

        public Double() {
            this(1, 20);
        }

        public Double(int rule) {
            this(rule, 20);
        }

        public Double(int rule, int initialCapacity) {
            super(rule, initialCapacity);
            this.doubleCoords = new double[initialCapacity * 2];
        }

        public Double(Shape s) {
            this(s, null);
        }

        public Double(Shape s, AffineTransform at) {
            if (s instanceof Path2D) {
                Path2D p2d = (Path2D)s;
                this.setWindingRule(p2d.windingRule);
                this.numTypes = p2d.numTypes;
                this.pointTypes = Arrays.copyOf(p2d.pointTypes, p2d.numTypes);
                this.numCoords = p2d.numCoords;
                this.doubleCoords = p2d.cloneCoordsDouble(at);
            } else {
                PathIterator pi = s.getPathIterator(at);
                this.setWindingRule(pi.getWindingRule());
                this.pointTypes = new byte[20];
                this.doubleCoords = new double[40];
                this.append(pi, false);
            }
        }

        @Override
        public final void trimToSize() {
            if (this.numTypes < this.pointTypes.length) {
                this.pointTypes = Arrays.copyOf(this.pointTypes, this.numTypes);
            }
            if (this.numCoords < this.doubleCoords.length) {
                this.doubleCoords = Arrays.copyOf(this.doubleCoords, this.numCoords);
            }
        }

        @Override
        float[] cloneCoordsFloat(AffineTransform at) {
            float[] ret = new float[this.numCoords];
            if (at == null) {
                for (int i = 0; i < this.numCoords; ++i) {
                    ret[i] = (float)this.doubleCoords[i];
                }
            } else {
                at.transform(this.doubleCoords, 0, ret, 0, this.numCoords / 2);
            }
            return ret;
        }

        @Override
        double[] cloneCoordsDouble(AffineTransform at) {
            double[] ret;
            if (at == null) {
                ret = Arrays.copyOf(this.doubleCoords, this.numCoords);
            } else {
                ret = new double[this.numCoords];
                at.transform(this.doubleCoords, 0, ret, 0, this.numCoords / 2);
            }
            return ret;
        }

        @Override
        void append(float x, float y) {
            this.doubleCoords[this.numCoords++] = x;
            this.doubleCoords[this.numCoords++] = y;
        }

        @Override
        void append(double x, double y) {
            this.doubleCoords[this.numCoords++] = x;
            this.doubleCoords[this.numCoords++] = y;
        }

        @Override
        Point2D getPoint(int coordindex) {
            return new Point2D.Double(this.doubleCoords[coordindex], this.doubleCoords[coordindex + 1]);
        }

        @Override
        void needRoom(boolean needMove, int newCoords) {
            if (this.numTypes == 0 && needMove) {
                throw new IllegalPathStateException("missing initial moveto in path definition");
            }
            if (this.numTypes >= this.pointTypes.length) {
                this.pointTypes = Double.expandPointTypes(this.pointTypes, 1);
            }
            if (this.numCoords > this.doubleCoords.length - newCoords) {
                this.doubleCoords = Double.expandCoords(this.doubleCoords, newCoords);
            }
        }

        static double[] expandCoords(double[] oldCoords, int needed) {
            int oldSize = oldCoords.length;
            int newSizeMin = oldSize + needed;
            if (newSizeMin < oldSize) {
                throw new ArrayIndexOutOfBoundsException("coords exceeds maximum capacity !");
            }
            int grow = oldSize;
            if (grow > 1000) {
                grow = Math.max(1000, oldSize >> 3);
            } else if (grow < 10) {
                grow = 10;
            }
            assert (grow > needed);
            int newSize = oldSize + grow;
            if (newSize < newSizeMin) {
                newSize = Integer.MAX_VALUE;
            }
            while (true) {
                try {
                    return Arrays.copyOf(oldCoords, newSize);
                }
                catch (OutOfMemoryError oome) {
                    if (newSize == newSizeMin) {
                        throw oome;
                    }
                    newSize = newSizeMin + (newSize - newSizeMin) / 2;
                    continue;
                }
                break;
            }
        }

        @Override
        public final synchronized void moveTo(double x, double y) {
            if (this.numTypes > 0 && this.pointTypes[this.numTypes - 1] == 0) {
                this.doubleCoords[this.numCoords - 2] = x;
                this.doubleCoords[this.numCoords - 1] = y;
            } else {
                this.needRoom(false, 2);
                this.pointTypes[this.numTypes++] = 0;
                this.doubleCoords[this.numCoords++] = x;
                this.doubleCoords[this.numCoords++] = y;
            }
        }

        @Override
        public final synchronized void lineTo(double x, double y) {
            this.needRoom(true, 2);
            this.pointTypes[this.numTypes++] = 1;
            this.doubleCoords[this.numCoords++] = x;
            this.doubleCoords[this.numCoords++] = y;
        }

        @Override
        public final synchronized void quadTo(double x1, double y1, double x2, double y2) {
            this.needRoom(true, 4);
            this.pointTypes[this.numTypes++] = 2;
            this.doubleCoords[this.numCoords++] = x1;
            this.doubleCoords[this.numCoords++] = y1;
            this.doubleCoords[this.numCoords++] = x2;
            this.doubleCoords[this.numCoords++] = y2;
        }

        @Override
        public final synchronized void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
            this.needRoom(true, 6);
            this.pointTypes[this.numTypes++] = 3;
            this.doubleCoords[this.numCoords++] = x1;
            this.doubleCoords[this.numCoords++] = y1;
            this.doubleCoords[this.numCoords++] = x2;
            this.doubleCoords[this.numCoords++] = y2;
            this.doubleCoords[this.numCoords++] = x3;
            this.doubleCoords[this.numCoords++] = y3;
        }

        @Override
        int pointCrossings(double px, double py) {
            double movy;
            double movx;
            if (this.numTypes == 0) {
                return 0;
            }
            double[] coords = this.doubleCoords;
            double curx = movx = coords[0];
            double cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            block7: for (int i = 1; i < this.numTypes; ++i) {
                switch (this.pointTypes[i]) {
                    case 0: {
                        if (cury != movy) {
                            crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
                        }
                        movx = curx = coords[ci++];
                        movy = cury = coords[ci++];
                        continue block7;
                    }
                    case 1: {
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings += Curve.pointCrossingsForLine(px, py, curx, cury, endx, endy);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 2: {
                        int n = ci++;
                        int n2 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings += Curve.pointCrossingsForQuad(px, py, curx, cury, coords[n], coords[n2], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 3: {
                        int n = ci++;
                        int n3 = ci++;
                        int n4 = ci++;
                        int n5 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings += Curve.pointCrossingsForCubic(px, py, curx, cury, coords[n], coords[n3], coords[n4], coords[n5], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 4: {
                        if (cury != movy) {
                            crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
                        }
                        curx = movx;
                        cury = movy;
                    }
                }
            }
            if (cury != movy) {
                crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
            }
            return crossings;
        }

        @Override
        int rectCrossings(double rxmin, double rymin, double rxmax, double rymax) {
            double movy;
            double movx;
            if (this.numTypes == 0) {
                return 0;
            }
            double[] coords = this.doubleCoords;
            double curx = movx = coords[0];
            double cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            block7: for (int i = 1; crossings != Integer.MIN_VALUE && i < this.numTypes; ++i) {
                switch (this.pointTypes[i]) {
                    case 0: {
                        if (curx != movx || cury != movy) {
                            crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
                        }
                        movx = curx = coords[ci++];
                        movy = cury = coords[ci++];
                        continue block7;
                    }
                    case 1: {
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, endx, endy);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 2: {
                        int n = ci++;
                        int n2 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings = Curve.rectCrossingsForQuad(crossings, rxmin, rymin, rxmax, rymax, curx, cury, coords[n], coords[n2], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 3: {
                        int n = ci++;
                        int n3 = ci++;
                        int n4 = ci++;
                        int n5 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings = Curve.rectCrossingsForCubic(crossings, rxmin, rymin, rxmax, rymax, curx, cury, coords[n], coords[n3], coords[n4], coords[n5], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 4: {
                        if (curx != movx || cury != movy) {
                            crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
                        }
                        curx = movx;
                        cury = movy;
                    }
                }
            }
            if (crossings != Integer.MIN_VALUE && (curx != movx || cury != movy)) {
                crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
            }
            return crossings;
        }

        @Override
        public final void append(PathIterator pi, boolean connect) {
            double[] coords = new double[6];
            while (!pi.isDone()) {
                switch (pi.currentSegment(coords)) {
                    case 0: {
                        if (!connect || this.numTypes < 1 || this.numCoords < 1) {
                            this.moveTo(coords[0], coords[1]);
                            break;
                        }
                        if (this.pointTypes[this.numTypes - 1] != 4 && this.doubleCoords[this.numCoords - 2] == coords[0] && this.doubleCoords[this.numCoords - 1] == coords[1]) break;
                        this.lineTo(coords[0], coords[1]);
                        break;
                    }
                    case 1: {
                        this.lineTo(coords[0], coords[1]);
                        break;
                    }
                    case 2: {
                        this.quadTo(coords[0], coords[1], coords[2], coords[3]);
                        break;
                    }
                    case 3: {
                        this.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                        break;
                    }
                    case 4: {
                        this.closePath();
                    }
                }
                pi.next();
                connect = false;
            }
        }

        @Override
        public final void transform(AffineTransform at) {
            at.transform(this.doubleCoords, 0, this.doubleCoords, 0, this.numCoords / 2);
        }

        @Override
        public final synchronized Rectangle2D getBounds2D() {
            return Double.getBounds2D(this.getPathIterator(null));
        }

        @Override
        public final PathIterator getPathIterator(AffineTransform at) {
            if (at == null) {
                return new CopyIterator(this);
            }
            return new TxIterator(this, at);
        }

        @Override
        public final Object clone() {
            return new Double(this);
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            super.writeObject(s, true);
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            super.readObject(s, true);
        }

        static class CopyIterator
        extends Iterator {
            double[] doubleCoords;

            CopyIterator(Double p2dd) {
                super(p2dd);
                this.doubleCoords = p2dd.doubleCoords;
            }

            @Override
            public int currentSegment(float[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    for (int i = 0; i < numCoords; ++i) {
                        coords[i] = (float)this.doubleCoords[this.pointIdx + i];
                    }
                }
                return type;
            }

            @Override
            public int currentSegment(double[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    System.arraycopy(this.doubleCoords, this.pointIdx, coords, 0, numCoords);
                }
                return type;
            }
        }

        static class TxIterator
        extends Iterator {
            double[] doubleCoords;
            AffineTransform affine;

            TxIterator(Double p2dd, AffineTransform at) {
                super(p2dd);
                this.doubleCoords = p2dd.doubleCoords;
                this.affine = at;
            }

            @Override
            public int currentSegment(float[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    this.affine.transform(this.doubleCoords, this.pointIdx, coords, 0, numCoords / 2);
                }
                return type;
            }

            @Override
            public int currentSegment(double[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    this.affine.transform(this.doubleCoords, this.pointIdx, coords, 0, numCoords / 2);
                }
                return type;
            }
        }
    }

    public static non-sealed class Float
    extends Path2D
    implements Serializable {
        transient float[] floatCoords;
        private static final long serialVersionUID = 6990832515060788886L;

        public Float() {
            this(1, 20);
        }

        public Float(int rule) {
            this(rule, 20);
        }

        public Float(int rule, int initialCapacity) {
            super(rule, initialCapacity);
            this.floatCoords = new float[initialCapacity * 2];
        }

        public Float(Shape s) {
            this(s, null);
        }

        public Float(Shape s, AffineTransform at) {
            if (s instanceof Path2D) {
                Path2D p2d = (Path2D)s;
                this.setWindingRule(p2d.windingRule);
                this.numTypes = p2d.numTypes;
                this.pointTypes = Arrays.copyOf(p2d.pointTypes, p2d.numTypes);
                this.numCoords = p2d.numCoords;
                this.floatCoords = p2d.cloneCoordsFloat(at);
            } else {
                PathIterator pi = s.getPathIterator(at);
                this.setWindingRule(pi.getWindingRule());
                this.pointTypes = new byte[20];
                this.floatCoords = new float[40];
                this.append(pi, false);
            }
        }

        @Override
        public final void trimToSize() {
            if (this.numTypes < this.pointTypes.length) {
                this.pointTypes = Arrays.copyOf(this.pointTypes, this.numTypes);
            }
            if (this.numCoords < this.floatCoords.length) {
                this.floatCoords = Arrays.copyOf(this.floatCoords, this.numCoords);
            }
        }

        @Override
        float[] cloneCoordsFloat(AffineTransform at) {
            float[] ret;
            if (at == null) {
                ret = Arrays.copyOf(this.floatCoords, this.numCoords);
            } else {
                ret = new float[this.numCoords];
                at.transform(this.floatCoords, 0, ret, 0, this.numCoords / 2);
            }
            return ret;
        }

        @Override
        double[] cloneCoordsDouble(AffineTransform at) {
            double[] ret = new double[this.numCoords];
            if (at == null) {
                for (int i = 0; i < this.numCoords; ++i) {
                    ret[i] = this.floatCoords[i];
                }
            } else {
                at.transform(this.floatCoords, 0, ret, 0, this.numCoords / 2);
            }
            return ret;
        }

        @Override
        void append(float x, float y) {
            this.floatCoords[this.numCoords++] = x;
            this.floatCoords[this.numCoords++] = y;
        }

        @Override
        void append(double x, double y) {
            this.floatCoords[this.numCoords++] = (float)x;
            this.floatCoords[this.numCoords++] = (float)y;
        }

        @Override
        Point2D getPoint(int coordindex) {
            return new Point2D.Float(this.floatCoords[coordindex], this.floatCoords[coordindex + 1]);
        }

        @Override
        void needRoom(boolean needMove, int newCoords) {
            if (this.numTypes == 0 && needMove) {
                throw new IllegalPathStateException("missing initial moveto in path definition");
            }
            if (this.numTypes >= this.pointTypes.length) {
                this.pointTypes = Float.expandPointTypes(this.pointTypes, 1);
            }
            if (this.numCoords > this.floatCoords.length - newCoords) {
                this.floatCoords = Float.expandCoords(this.floatCoords, newCoords);
            }
        }

        static float[] expandCoords(float[] oldCoords, int needed) {
            int oldSize = oldCoords.length;
            int newSizeMin = oldSize + needed;
            if (newSizeMin < oldSize) {
                throw new ArrayIndexOutOfBoundsException("coords exceeds maximum capacity !");
            }
            int grow = oldSize;
            if (grow > 1000) {
                grow = Math.max(1000, oldSize >> 3);
            } else if (grow < 10) {
                grow = 10;
            }
            assert (grow > needed);
            int newSize = oldSize + grow;
            if (newSize < newSizeMin) {
                newSize = Integer.MAX_VALUE;
            }
            while (true) {
                try {
                    return Arrays.copyOf(oldCoords, newSize);
                }
                catch (OutOfMemoryError oome) {
                    if (newSize == newSizeMin) {
                        throw oome;
                    }
                    newSize = newSizeMin + (newSize - newSizeMin) / 2;
                    continue;
                }
                break;
            }
        }

        @Override
        public final synchronized void moveTo(double x, double y) {
            if (this.numTypes > 0 && this.pointTypes[this.numTypes - 1] == 0) {
                this.floatCoords[this.numCoords - 2] = (float)x;
                this.floatCoords[this.numCoords - 1] = (float)y;
            } else {
                this.needRoom(false, 2);
                this.pointTypes[this.numTypes++] = 0;
                this.floatCoords[this.numCoords++] = (float)x;
                this.floatCoords[this.numCoords++] = (float)y;
            }
        }

        public final synchronized void moveTo(float x, float y) {
            if (this.numTypes > 0 && this.pointTypes[this.numTypes - 1] == 0) {
                this.floatCoords[this.numCoords - 2] = x;
                this.floatCoords[this.numCoords - 1] = y;
            } else {
                this.needRoom(false, 2);
                this.pointTypes[this.numTypes++] = 0;
                this.floatCoords[this.numCoords++] = x;
                this.floatCoords[this.numCoords++] = y;
            }
        }

        @Override
        public final synchronized void lineTo(double x, double y) {
            this.needRoom(true, 2);
            this.pointTypes[this.numTypes++] = 1;
            this.floatCoords[this.numCoords++] = (float)x;
            this.floatCoords[this.numCoords++] = (float)y;
        }

        public final synchronized void lineTo(float x, float y) {
            this.needRoom(true, 2);
            this.pointTypes[this.numTypes++] = 1;
            this.floatCoords[this.numCoords++] = x;
            this.floatCoords[this.numCoords++] = y;
        }

        @Override
        public final synchronized void quadTo(double x1, double y1, double x2, double y2) {
            this.needRoom(true, 4);
            this.pointTypes[this.numTypes++] = 2;
            this.floatCoords[this.numCoords++] = (float)x1;
            this.floatCoords[this.numCoords++] = (float)y1;
            this.floatCoords[this.numCoords++] = (float)x2;
            this.floatCoords[this.numCoords++] = (float)y2;
        }

        public final synchronized void quadTo(float x1, float y1, float x2, float y2) {
            this.needRoom(true, 4);
            this.pointTypes[this.numTypes++] = 2;
            this.floatCoords[this.numCoords++] = x1;
            this.floatCoords[this.numCoords++] = y1;
            this.floatCoords[this.numCoords++] = x2;
            this.floatCoords[this.numCoords++] = y2;
        }

        @Override
        public final synchronized void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
            this.needRoom(true, 6);
            this.pointTypes[this.numTypes++] = 3;
            this.floatCoords[this.numCoords++] = (float)x1;
            this.floatCoords[this.numCoords++] = (float)y1;
            this.floatCoords[this.numCoords++] = (float)x2;
            this.floatCoords[this.numCoords++] = (float)y2;
            this.floatCoords[this.numCoords++] = (float)x3;
            this.floatCoords[this.numCoords++] = (float)y3;
        }

        public final synchronized void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            this.needRoom(true, 6);
            this.pointTypes[this.numTypes++] = 3;
            this.floatCoords[this.numCoords++] = x1;
            this.floatCoords[this.numCoords++] = y1;
            this.floatCoords[this.numCoords++] = x2;
            this.floatCoords[this.numCoords++] = y2;
            this.floatCoords[this.numCoords++] = x3;
            this.floatCoords[this.numCoords++] = y3;
        }

        @Override
        int pointCrossings(double px, double py) {
            double movy;
            double movx;
            if (this.numTypes == 0) {
                return 0;
            }
            float[] coords = this.floatCoords;
            double curx = movx = (double)coords[0];
            double cury = movy = (double)coords[1];
            int crossings = 0;
            int ci = 2;
            block7: for (int i = 1; i < this.numTypes; ++i) {
                switch (this.pointTypes[i]) {
                    case 0: {
                        if (cury != movy) {
                            crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
                        }
                        movx = curx = (double)coords[ci++];
                        movy = cury = (double)coords[ci++];
                        continue block7;
                    }
                    case 1: {
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings += Curve.pointCrossingsForLine(px, py, curx, cury, endx, endy);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 2: {
                        int n = ci++;
                        int n2 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings += Curve.pointCrossingsForQuad(px, py, curx, cury, coords[n], coords[n2], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 3: {
                        int n = ci++;
                        int n3 = ci++;
                        int n4 = ci++;
                        int n5 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings += Curve.pointCrossingsForCubic(px, py, curx, cury, coords[n], coords[n3], coords[n4], coords[n5], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 4: {
                        if (cury != movy) {
                            crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
                        }
                        curx = movx;
                        cury = movy;
                    }
                }
            }
            if (cury != movy) {
                crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
            }
            return crossings;
        }

        @Override
        int rectCrossings(double rxmin, double rymin, double rxmax, double rymax) {
            double movy;
            double movx;
            if (this.numTypes == 0) {
                return 0;
            }
            float[] coords = this.floatCoords;
            double curx = movx = (double)coords[0];
            double cury = movy = (double)coords[1];
            int crossings = 0;
            int ci = 2;
            block7: for (int i = 1; crossings != Integer.MIN_VALUE && i < this.numTypes; ++i) {
                switch (this.pointTypes[i]) {
                    case 0: {
                        if (curx != movx || cury != movy) {
                            crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
                        }
                        movx = curx = (double)coords[ci++];
                        movy = cury = (double)coords[ci++];
                        continue block7;
                    }
                    case 1: {
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, endx, endy);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 2: {
                        int n = ci++;
                        int n2 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings = Curve.rectCrossingsForQuad(crossings, rxmin, rymin, rxmax, rymax, curx, cury, coords[n], coords[n2], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 3: {
                        int n = ci++;
                        int n3 = ci++;
                        int n4 = ci++;
                        int n5 = ci++;
                        double endx = coords[ci++];
                        double endy = coords[ci++];
                        crossings = Curve.rectCrossingsForCubic(crossings, rxmin, rymin, rxmax, rymax, curx, cury, coords[n], coords[n3], coords[n4], coords[n5], endx, endy, 0);
                        curx = endx;
                        cury = endy;
                        continue block7;
                    }
                    case 4: {
                        if (curx != movx || cury != movy) {
                            crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
                        }
                        curx = movx;
                        cury = movy;
                    }
                }
            }
            if (crossings != Integer.MIN_VALUE && (curx != movx || cury != movy)) {
                crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
            }
            return crossings;
        }

        @Override
        public final void append(PathIterator pi, boolean connect) {
            float[] coords = new float[6];
            while (!pi.isDone()) {
                switch (pi.currentSegment(coords)) {
                    case 0: {
                        if (!connect || this.numTypes < 1 || this.numCoords < 1) {
                            this.moveTo(coords[0], coords[1]);
                            break;
                        }
                        if (this.pointTypes[this.numTypes - 1] != 4 && this.floatCoords[this.numCoords - 2] == coords[0] && this.floatCoords[this.numCoords - 1] == coords[1]) break;
                        this.lineTo(coords[0], coords[1]);
                        break;
                    }
                    case 1: {
                        this.lineTo(coords[0], coords[1]);
                        break;
                    }
                    case 2: {
                        this.quadTo(coords[0], coords[1], coords[2], coords[3]);
                        break;
                    }
                    case 3: {
                        this.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                        break;
                    }
                    case 4: {
                        this.closePath();
                    }
                }
                pi.next();
                connect = false;
            }
        }

        @Override
        public final void transform(AffineTransform at) {
            at.transform(this.floatCoords, 0, this.floatCoords, 0, this.numCoords / 2);
        }

        @Override
        public final synchronized Rectangle2D getBounds2D() {
            return Float.getBounds2D(this.getPathIterator(null));
        }

        @Override
        public final PathIterator getPathIterator(AffineTransform at) {
            if (at == null) {
                return new CopyIterator(this);
            }
            return new TxIterator(this, at);
        }

        @Override
        public final Object clone() {
            if (this instanceof GeneralPath) {
                return new GeneralPath(this);
            }
            return new Float(this);
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            super.writeObject(s, false);
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            super.readObject(s, false);
        }

        static class CopyIterator
        extends Iterator {
            float[] floatCoords;

            CopyIterator(Float p2df) {
                super(p2df);
                this.floatCoords = p2df.floatCoords;
            }

            @Override
            public int currentSegment(float[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    System.arraycopy(this.floatCoords, this.pointIdx, coords, 0, numCoords);
                }
                return type;
            }

            @Override
            public int currentSegment(double[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    for (int i = 0; i < numCoords; ++i) {
                        coords[i] = this.floatCoords[this.pointIdx + i];
                    }
                }
                return type;
            }
        }

        static class TxIterator
        extends Iterator {
            float[] floatCoords;
            AffineTransform affine;

            TxIterator(Float p2df, AffineTransform at) {
                super(p2df);
                this.floatCoords = p2df.floatCoords;
                this.affine = at;
            }

            @Override
            public int currentSegment(float[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    this.affine.transform(this.floatCoords, this.pointIdx, coords, 0, numCoords / 2);
                }
                return type;
            }

            @Override
            public int currentSegment(double[] coords) {
                byte type = this.path.pointTypes[this.typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    this.affine.transform(this.floatCoords, this.pointIdx, coords, 0, numCoords / 2);
                }
                return type;
            }
        }
    }

    static abstract class Iterator
    implements PathIterator {
        int typeIdx;
        int pointIdx;
        Path2D path;
        static final int[] curvecoords = new int[]{2, 2, 4, 6, 0};

        Iterator(Path2D path) {
            this.path = path;
        }

        @Override
        public int getWindingRule() {
            return this.path.getWindingRule();
        }

        @Override
        public boolean isDone() {
            return this.typeIdx >= this.path.numTypes;
        }

        @Override
        public void next() {
            byte type = this.path.pointTypes[this.typeIdx++];
            this.pointIdx += curvecoords[type];
        }
    }
}

