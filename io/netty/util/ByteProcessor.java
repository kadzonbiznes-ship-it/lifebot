/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.ByteProcessor$IndexNotOfProcessor
 *  io.netty.util.ByteProcessor$IndexOfProcessor
 */
package io.netty.util;

import io.netty.util.ByteProcessor;

public interface ByteProcessor {
    public static final ByteProcessor FIND_NUL = new IndexOfProcessor(0);
    public static final ByteProcessor FIND_NON_NUL = new IndexNotOfProcessor(0);
    public static final ByteProcessor FIND_CR = new IndexOfProcessor(13);
    public static final ByteProcessor FIND_NON_CR = new IndexNotOfProcessor(13);
    public static final ByteProcessor FIND_LF = new IndexOfProcessor(10);
    public static final ByteProcessor FIND_NON_LF = new IndexNotOfProcessor(10);
    public static final ByteProcessor FIND_SEMI_COLON = new IndexOfProcessor(59);
    public static final ByteProcessor FIND_COMMA = new IndexOfProcessor(44);
    public static final ByteProcessor FIND_ASCII_SPACE = new IndexOfProcessor(32);
    public static final ByteProcessor FIND_CRLF = new /* Unavailable Anonymous Inner Class!! */;
    public static final ByteProcessor FIND_NON_CRLF = new /* Unavailable Anonymous Inner Class!! */;
    public static final ByteProcessor FIND_LINEAR_WHITESPACE = new /* Unavailable Anonymous Inner Class!! */;
    public static final ByteProcessor FIND_NON_LINEAR_WHITESPACE = new /* Unavailable Anonymous Inner Class!! */;

    public boolean process(byte var1) throws Exception;
}

