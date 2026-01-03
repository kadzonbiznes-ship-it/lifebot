/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.tess4j.util;

public class LoggHelper
extends Exception {
    @Override
    public String toString() {
        StackTraceElement[] sTrace = this.getStackTrace();
        String className = sTrace[0].getClassName();
        return className;
    }
}

