/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.io.Serializable;

public class SizeRequirements
implements Serializable {
    public int minimum;
    public int preferred;
    public int maximum;
    public float alignment;

    public SizeRequirements() {
        this.minimum = 0;
        this.preferred = 0;
        this.maximum = 0;
        this.alignment = 0.5f;
    }

    public SizeRequirements(int min, int pref, int max, float a) {
        this.minimum = min;
        this.preferred = pref;
        this.maximum = max;
        this.alignment = a > 1.0f ? 1.0f : (a < 0.0f ? 0.0f : a);
    }

    public String toString() {
        return "[" + this.minimum + "," + this.preferred + "," + this.maximum + "]@" + this.alignment;
    }

    public static SizeRequirements getTiledSizeRequirements(SizeRequirements[] children) {
        SizeRequirements total = new SizeRequirements();
        for (int i = 0; i < children.length; ++i) {
            SizeRequirements req = children[i];
            total.minimum = (int)Math.min((long)total.minimum + (long)req.minimum, Integer.MAX_VALUE);
            total.preferred = (int)Math.min((long)total.preferred + (long)req.preferred, Integer.MAX_VALUE);
            total.maximum = (int)Math.min((long)total.maximum + (long)req.maximum, Integer.MAX_VALUE);
        }
        return total;
    }

    public static SizeRequirements getAlignedSizeRequirements(SizeRequirements[] children) {
        SizeRequirements totalAscent = new SizeRequirements();
        SizeRequirements totalDescent = new SizeRequirements();
        for (int i = 0; i < children.length; ++i) {
            SizeRequirements req = children[i];
            int ascent = (int)(req.alignment * (float)req.minimum);
            int descent = req.minimum - ascent;
            totalAscent.minimum = Math.max(ascent, totalAscent.minimum);
            totalDescent.minimum = Math.max(descent, totalDescent.minimum);
            ascent = (int)(req.alignment * (float)req.preferred);
            descent = req.preferred - ascent;
            totalAscent.preferred = Math.max(ascent, totalAscent.preferred);
            totalDescent.preferred = Math.max(descent, totalDescent.preferred);
            ascent = (int)(req.alignment * (float)req.maximum);
            descent = req.maximum - ascent;
            totalAscent.maximum = Math.max(ascent, totalAscent.maximum);
            totalDescent.maximum = Math.max(descent, totalDescent.maximum);
        }
        int min = (int)Math.min((long)totalAscent.minimum + (long)totalDescent.minimum, Integer.MAX_VALUE);
        int pref = (int)Math.min((long)totalAscent.preferred + (long)totalDescent.preferred, Integer.MAX_VALUE);
        int max = (int)Math.min((long)totalAscent.maximum + (long)totalDescent.maximum, Integer.MAX_VALUE);
        float alignment = 0.0f;
        if (min > 0) {
            alignment = (float)totalAscent.minimum / (float)min;
            alignment = alignment > 1.0f ? 1.0f : (alignment < 0.0f ? 0.0f : alignment);
        }
        return new SizeRequirements(min, pref, max, alignment);
    }

    public static void calculateTiledPositions(int allocated, SizeRequirements total, SizeRequirements[] children, int[] offsets, int[] spans) {
        SizeRequirements.calculateTiledPositions(allocated, total, children, offsets, spans, true);
    }

    public static void calculateTiledPositions(int allocated, SizeRequirements total, SizeRequirements[] children, int[] offsets, int[] spans, boolean forward) {
        long min = 0L;
        long pref = 0L;
        long max = 0L;
        for (int i = 0; i < children.length; ++i) {
            min += (long)children[i].minimum;
            pref += (long)children[i].preferred;
            max += (long)children[i].maximum;
        }
        if ((long)allocated >= pref) {
            SizeRequirements.expandedTile(allocated, min, pref, max, children, offsets, spans, forward);
        } else {
            SizeRequirements.compressedTile(allocated, min, pref, max, children, offsets, spans, forward);
        }
    }

    private static void compressedTile(int allocated, long min, long pref, long max, SizeRequirements[] request, int[] offsets, int[] spans, boolean forward) {
        float factor;
        float totalPlay = Math.min(pref - (long)allocated, pref - min);
        float f = factor = pref - min == 0L ? 0.0f : totalPlay / (float)(pref - min);
        if (forward) {
            int totalOffset = 0;
            for (int i = 0; i < spans.length; ++i) {
                offsets[i] = totalOffset;
                SizeRequirements req = request[i];
                float play = factor * (float)(req.preferred - req.minimum);
                spans[i] = (int)((float)req.preferred - play);
                totalOffset = (int)Math.min((long)totalOffset + (long)spans[i], Integer.MAX_VALUE);
            }
        } else {
            int totalOffset = allocated;
            for (int i = 0; i < spans.length; ++i) {
                SizeRequirements req = request[i];
                float play = factor * (float)(req.preferred - req.minimum);
                spans[i] = (int)((float)req.preferred - play);
                offsets[i] = totalOffset - spans[i];
                totalOffset = (int)Math.max((long)totalOffset - (long)spans[i], 0L);
            }
        }
    }

    private static void expandedTile(int allocated, long min, long pref, long max, SizeRequirements[] request, int[] offsets, int[] spans, boolean forward) {
        float factor;
        float totalPlay = Math.min((long)allocated - pref, max - pref);
        float f = factor = max - pref == 0L ? 0.0f : totalPlay / (float)(max - pref);
        if (forward) {
            int totalOffset = 0;
            for (int i = 0; i < spans.length; ++i) {
                offsets[i] = totalOffset;
                SizeRequirements req = request[i];
                int play = (int)(factor * (float)(req.maximum - req.preferred));
                spans[i] = (int)Math.min((long)req.preferred + (long)play, Integer.MAX_VALUE);
                totalOffset = (int)Math.min((long)totalOffset + (long)spans[i], Integer.MAX_VALUE);
            }
        } else {
            int totalOffset = allocated;
            for (int i = 0; i < spans.length; ++i) {
                SizeRequirements req = request[i];
                int play = (int)(factor * (float)(req.maximum - req.preferred));
                spans[i] = (int)Math.min((long)req.preferred + (long)play, Integer.MAX_VALUE);
                offsets[i] = totalOffset - spans[i];
                totalOffset = (int)Math.max((long)totalOffset - (long)spans[i], 0L);
            }
        }
    }

    public static void calculateAlignedPositions(int allocated, SizeRequirements total, SizeRequirements[] children, int[] offsets, int[] spans) {
        SizeRequirements.calculateAlignedPositions(allocated, total, children, offsets, spans, true);
    }

    public static void calculateAlignedPositions(int allocated, SizeRequirements total, SizeRequirements[] children, int[] offsets, int[] spans, boolean normal) {
        float totalAlignment = normal ? total.alignment : 1.0f - total.alignment;
        int totalAscent = (int)((float)allocated * totalAlignment);
        int totalDescent = allocated - totalAscent;
        for (int i = 0; i < children.length; ++i) {
            SizeRequirements req = children[i];
            float alignment = normal ? req.alignment : 1.0f - req.alignment;
            int maxAscent = (int)((float)req.maximum * alignment);
            int maxDescent = req.maximum - maxAscent;
            int ascent = Math.min(totalAscent, maxAscent);
            int descent = Math.min(totalDescent, maxDescent);
            offsets[i] = totalAscent - ascent;
            spans[i] = (int)Math.min((long)ascent + (long)descent, Integer.MAX_VALUE);
        }
    }

    public static int[] adjustSizes(int delta, SizeRequirements[] children) {
        return new int[0];
    }
}

