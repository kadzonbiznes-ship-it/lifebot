/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

public interface AATileGenerator {
    public int getTileWidth();

    public int getTileHeight();

    public int getTypicalAlpha();

    public void nextTile();

    public void getAlpha(byte[] var1, int var2, int var3);

    public void dispose();
}

