/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.tess4j.ITessAPI$CANCEL_FUNC
 *  net.sourceforge.tess4j.ITessAPI$EANYCODE_CHAR
 *  net.sourceforge.tess4j.ITessAPI$TimeVal
 */
package net.sourceforge.tess4j;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.tess4j.ITessAPI;

public interface ITessAPI {
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public static interface TessProgressFunc
    extends Callback {
        public boolean apply(ETEXT_DESC var1, int var2, int var3, int var4, int var5);
    }

    public static interface TessCancelFunc
    extends Callback {
        public boolean apply(Pointer var1, int var2);
    }

    public static class ETEXT_DESC
    extends Structure {
        public short count;
        public short progress;
        public byte more_to_come;
        public byte ocr_alive;
        public byte err_code;
        public CANCEL_FUNC cancel;
        public Pointer cancel_this;
        public TimeVal end_time;
        public EANYCODE_CHAR[] text = new EANYCODE_CHAR[1];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("count", "progress", "more_to_come", "ocr_alive", "err_code", "cancel", "cancel_this", "end_time", "text");
        }
    }

    public static class TessResultRenderer
    extends PointerType {
        public TessResultRenderer(Pointer address) {
            super(address);
        }

        public TessResultRenderer() {
        }
    }

    public static class TessChoiceIterator
    extends PointerType {
        public TessChoiceIterator(Pointer address) {
            super(address);
        }

        public TessChoiceIterator() {
        }
    }

    public static class TessResultIterator
    extends PointerType {
        public TessResultIterator(Pointer address) {
            super(address);
        }

        public TessResultIterator() {
        }
    }

    public static class TessMutableIterator
    extends PointerType {
        public TessMutableIterator(Pointer address) {
            super(address);
        }

        public TessMutableIterator() {
        }
    }

    public static class TessPageIterator
    extends PointerType {
        public TessPageIterator(Pointer address) {
            super(address);
        }

        public TessPageIterator() {
        }
    }

    public static class TessBaseAPI
    extends PointerType {
        public TessBaseAPI(Pointer address) {
            super(address);
        }

        public TessBaseAPI() {
        }
    }
}

