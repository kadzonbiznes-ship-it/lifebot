/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import sun.java2d.ReentrantContext;
import sun.java2d.ReentrantContextProvider;
import sun.java2d.ReentrantContextProviderTL;
import sun.java2d.SunGraphics2D;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.pipe.AATileGenerator;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.RenderingEngine;
import sun.java2d.pipe.ShapeDrawPipe;

public final class AAShapePipe
implements ShapeDrawPipe,
ParallelogramPipe {
    static final RenderingEngine RDR_ENGINE = RenderingEngine.getInstance();
    private static final boolean DO_RENDER = !MarlinProperties.isSkipRenderTiles();
    private static final ReentrantContextProvider<TileState> TILE_STATE_PROVIDER = new ReentrantContextProviderTL<TileState>(0){

        @Override
        protected TileState newContext() {
            return new TileState();
        }
    };
    final CompositePipe outpipe;

    public AAShapePipe(CompositePipe pipe) {
        this.outpipe = pipe;
    }

    @Override
    public void draw(SunGraphics2D sg, Shape s) {
        BasicStroke bs;
        if (sg.stroke instanceof BasicStroke) {
            bs = (BasicStroke)sg.stroke;
        } else {
            s = sg.stroke.createStrokedShape(s);
            bs = null;
        }
        this.renderPath(sg, s, bs);
    }

    @Override
    public void fill(SunGraphics2D sg, Shape s) {
        this.renderPath(sg, s, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillParallelogram(SunGraphics2D sg, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2) {
        TileState ts = TILE_STATE_PROVIDER.acquire();
        try {
            int[] abox = ts.abox;
            AATileGenerator aatg = RDR_ENGINE.getAATileGenerator(x, y, dx1, dy1, dx2, dy2, 0.0, 0.0, sg.getCompClip(), abox);
            if (aatg != null) {
                this.renderTiles(sg, ts.computeBBox(ux1, uy1, ux2, uy2), aatg, abox, ts);
            }
        }
        finally {
            TILE_STATE_PROVIDER.release(ts);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawParallelogram(SunGraphics2D sg, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
        TileState ts = TILE_STATE_PROVIDER.acquire();
        try {
            int[] abox = ts.abox;
            AATileGenerator aatg = RDR_ENGINE.getAATileGenerator(x, y, dx1, dy1, dx2, dy2, lw1, lw2, sg.getCompClip(), abox);
            if (aatg != null) {
                this.renderTiles(sg, ts.computeBBox(ux1, uy1, ux2, uy2), aatg, abox, ts);
            }
        }
        finally {
            TILE_STATE_PROVIDER.release(ts);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderPath(SunGraphics2D sg, Shape s, BasicStroke bs) {
        boolean adjust = bs != null && sg.strokeHint != 2;
        boolean thin = sg.strokeState <= 1;
        TileState ts = TILE_STATE_PROVIDER.acquire();
        try {
            int[] abox = ts.abox;
            AATileGenerator aatg = RDR_ENGINE.getAATileGenerator(s, sg.transform, sg.getCompClip(), bs, thin, adjust, abox);
            if (aatg != null) {
                this.renderTiles(sg, s, aatg, abox, ts);
            }
        }
        finally {
            TILE_STATE_PROVIDER.release(ts);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderTiles(SunGraphics2D sg, Shape s, AATileGenerator aatg, int[] abox, TileState ts) {
        Object context = null;
        try {
            if (DO_RENDER) {
                context = this.outpipe.startSequence(sg, s, ts.computeDevBox(abox), abox);
            }
            int x0 = abox[0];
            int y0 = abox[1];
            int x1 = abox[2];
            int y1 = abox[3];
            int tw = aatg.getTileWidth();
            int th = aatg.getTileHeight();
            byte[] alpha = ts.getAlphaTile(tw * th);
            for (int y = y0; y < y1; y += th) {
                int h = Math.min(th, y1 - y);
                for (int x = x0; x < x1; x += tw) {
                    byte[] atile;
                    int w = Math.min(tw, x1 - x);
                    int a = aatg.getTypicalAlpha();
                    if (a == 0 || DO_RENDER && !this.outpipe.needTile(context, x, y, w, h)) {
                        aatg.nextTile();
                        if (!DO_RENDER) continue;
                        this.outpipe.skipTile(context, x, y);
                        continue;
                    }
                    if (a == 255) {
                        atile = null;
                        aatg.nextTile();
                    } else {
                        atile = alpha;
                        aatg.getAlpha(alpha, 0, tw);
                    }
                    if (!DO_RENDER) continue;
                    this.outpipe.renderPathTile(context, atile, 0, tw, x, y, w, h);
                }
            }
            aatg.dispose();
            if (context != null) {
                this.outpipe.endSequence(context);
            }
        }
        catch (Throwable throwable) {
            aatg.dispose();
            if (context != null) {
                this.outpipe.endSequence(context);
            }
            throw throwable;
        }
    }

    static final class TileState
    extends ReentrantContext {
        private byte[] theTile = new byte[1024];
        final int[] abox = new int[4];
        private final Rectangle dev = new Rectangle();
        private final Rectangle2D.Double bbox2D = new Rectangle2D.Double();

        TileState() {
        }

        byte[] getAlphaTile(int len) {
            byte[] t = this.theTile;
            if (t.length < len) {
                this.theTile = t = new byte[len];
            }
            return t;
        }

        Rectangle computeDevBox(int[] abox) {
            Rectangle box = this.dev;
            box.x = abox[0];
            box.y = abox[1];
            box.width = abox[2] - abox[0];
            box.height = abox[3] - abox[1];
            return box;
        }

        Rectangle2D computeBBox(double ux1, double uy1, double ux2, double uy2) {
            double d;
            double d2;
            ux2 -= ux1;
            if (d2 < 0.0) {
                ux1 += ux2;
                ux2 = -ux2;
            }
            uy2 -= uy1;
            if (d < 0.0) {
                uy1 += uy2;
                uy2 = -uy2;
            }
            Rectangle2D.Double box = this.bbox2D;
            box.x = ux1;
            box.y = uy1;
            box.width = ux2;
            box.height = uy2;
            return box;
        }
    }
}

