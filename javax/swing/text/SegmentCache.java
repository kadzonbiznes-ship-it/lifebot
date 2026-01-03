/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.Segment;

class SegmentCache {
    private static SegmentCache sharedCache = new SegmentCache();
    private List<Segment> segments = new ArrayList<Segment>(11);

    public static SegmentCache getSharedInstance() {
        return sharedCache;
    }

    public static Segment getSharedSegment() {
        return SegmentCache.getSharedInstance().getSegment();
    }

    public static void releaseSharedSegment(Segment segment) {
        SegmentCache.getSharedInstance().releaseSegment(segment);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Segment getSegment() {
        SegmentCache segmentCache = this;
        synchronized (segmentCache) {
            int size = this.segments.size();
            if (size > 0) {
                return this.segments.remove(size - 1);
            }
        }
        return new CachedSegment();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void releaseSegment(Segment segment) {
        if (segment instanceof CachedSegment) {
            SegmentCache segmentCache = this;
            synchronized (segmentCache) {
                if (segment.copy) {
                    Arrays.fill(segment.array, '\u0000');
                }
                segment.array = null;
                segment.copy = false;
                segment.count = 0;
                this.segments.add(segment);
            }
        }
    }

    private static class CachedSegment
    extends Segment {
        private CachedSegment() {
        }
    }
}

