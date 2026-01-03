/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

public class L_Sudoku
extends Structure {
    public int num;
    public IntByReference locs;
    public int current;
    public IntByReference init;
    public IntByReference state;
    public int nguess;
    public int finished;
    public int failure;

    public L_Sudoku() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("num", "locs", "current", "init", "state", "nguess", "finished", "failure");
    }

    public L_Sudoku(int n, IntByReference intByReference, int n2, IntByReference intByReference2, IntByReference intByReference3, int n3, int n4, int n5) {
        this.num = n;
        this.locs = intByReference;
        this.current = n2;
        this.init = intByReference2;
        this.state = intByReference3;
        this.nguess = n3;
        this.finished = n4;
        this.failure = n5;
    }

    public L_Sudoku(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

