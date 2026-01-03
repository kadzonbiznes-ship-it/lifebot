/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class L_RegParams
extends Structure {
    public PointerByReference fp;
    public Pointer testname;
    public Pointer tempfile;
    public int mode;
    public int index;
    public int success;
    public int display;
    public Pointer tstart;

    public L_RegParams() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("fp", "testname", "tempfile", "mode", "index", "success", "display", "tstart");
    }

    public L_RegParams(PointerByReference pointerByReference, Pointer pointer, Pointer pointer2, int n, int n2, int n3, int n4, Pointer pointer3) {
        this.fp = pointerByReference;
        this.testname = pointer;
        this.tempfile = pointer2;
        this.mode = n;
        this.index = n2;
        this.success = n3;
        this.display = n4;
        this.tstart = pointer3;
    }

    public L_RegParams(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

