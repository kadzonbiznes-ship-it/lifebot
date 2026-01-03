/*
 * Decompiled with CFR 0.152.
 */
package java.awt.print;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

public interface Printable {
    public static final int PAGE_EXISTS = 0;
    public static final int NO_SUCH_PAGE = 1;

    public int print(Graphics var1, PageFormat var2, int var3) throws PrinterException;
}

