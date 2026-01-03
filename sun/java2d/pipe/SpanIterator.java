/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

public interface SpanIterator {
    public void getPathBox(int[] var1);

    public void intersectClipBox(int var1, int var2, int var3, int var4);

    public boolean nextSpan(int[] var1);

    public void skipDownTo(int var1);

    public long getNativeIterator();
}

