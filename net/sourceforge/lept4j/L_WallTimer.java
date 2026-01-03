/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class L_WallTimer
extends Structure {
    public int start_sec;
    public int start_usec;
    public int stop_sec;
    public int stop_usec;

    public L_WallTimer() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("start_sec", "start_usec", "stop_sec", "stop_usec");
    }

    public L_WallTimer(int n, int n2, int n3, int n4) {
        this.start_sec = n;
        this.start_usec = n2;
        this.stop_sec = n3;
        this.stop_usec = n4;
    }

    public L_WallTimer(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

