/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI;

public final class $Proxy18
extends Proxy
implements TessAPI {
    private static final Method m0;
    private static final Method m1;
    private static final Method m2;
    private static final Method m3;
    private static final Method m4;
    private static final Method m5;
    private static final Method m6;
    private static final Method m7;
    private static final Method m8;
    private static final Method m9;
    private static final Method m10;
    private static final Method m11;
    private static final Method m12;
    private static final Method m13;
    private static final Method m14;
    private static final Method m15;
    private static final Method m16;
    private static final Method m17;
    private static final Method m18;
    private static final Method m19;
    private static final Method m20;
    private static final Method m21;
    private static final Method m22;
    private static final Method m23;
    private static final Method m24;
    private static final Method m25;
    private static final Method m26;
    private static final Method m27;
    private static final Method m28;
    private static final Method m29;
    private static final Method m30;
    private static final Method m31;
    private static final Method m32;
    private static final Method m33;
    private static final Method m34;
    private static final Method m35;
    private static final Method m36;
    private static final Method m37;
    private static final Method m38;
    private static final Method m39;
    private static final Method m40;
    private static final Method m41;
    private static final Method m42;
    private static final Method m43;
    private static final Method m44;
    private static final Method m45;
    private static final Method m46;
    private static final Method m47;
    private static final Method m48;
    private static final Method m49;
    private static final Method m50;
    private static final Method m51;
    private static final Method m52;
    private static final Method m53;
    private static final Method m54;
    private static final Method m55;
    private static final Method m56;
    private static final Method m57;
    private static final Method m58;
    private static final Method m59;
    private static final Method m60;
    private static final Method m61;
    private static final Method m62;
    private static final Method m63;
    private static final Method m64;
    private static final Method m65;
    private static final Method m66;
    private static final Method m67;
    private static final Method m68;
    private static final Method m69;
    private static final Method m70;
    private static final Method m71;
    private static final Method m72;
    private static final Method m73;
    private static final Method m74;
    private static final Method m75;
    private static final Method m76;
    private static final Method m77;
    private static final Method m78;
    private static final Method m79;
    private static final Method m80;
    private static final Method m81;
    private static final Method m82;
    private static final Method m83;
    private static final Method m84;
    private static final Method m85;
    private static final Method m86;
    private static final Method m87;
    private static final Method m88;
    private static final Method m89;
    private static final Method m90;
    private static final Method m91;
    private static final Method m92;
    private static final Method m93;
    private static final Method m94;
    private static final Method m95;
    private static final Method m96;
    private static final Method m97;
    private static final Method m98;
    private static final Method m99;
    private static final Method m100;
    private static final Method m101;
    private static final Method m102;
    private static final Method m103;
    private static final Method m104;
    private static final Method m105;
    private static final Method m106;
    private static final Method m107;
    private static final Method m108;
    private static final Method m109;
    private static final Method m110;
    private static final Method m111;
    private static final Method m112;
    private static final Method m113;
    private static final Method m114;
    private static final Method m115;
    private static final Method m116;
    private static final Method m117;
    private static final Method m118;
    private static final Method m119;
    private static final Method m120;
    private static final Method m121;
    private static final Method m122;
    private static final Method m123;
    private static final Method m124;
    private static final Method m125;
    private static final Method m126;
    private static final Method m127;
    private static final Method m128;
    private static final Method m129;
    private static final Method m130;
    private static final Method m131;
    private static final Method m132;
    private static final Method m133;
    private static final Method m134;
    private static final Method m135;

    public $Proxy18(InvocationHandler invocationHandler) {
        super(invocationHandler);
    }

    public final int hashCode() {
        try {
            return (Integer)this.h.invoke(this, m0, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public final boolean equals(Object object) {
        try {
            return (Boolean)this.h.invoke(this, m1, new Object[]{object});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public final String toString() {
        try {
            return (String)this.h.invoke(this, m2, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final IntByReference TessBaseAPIAllWordConfidences(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (IntByReference)this.h.invoke(this, m3, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIClearPersistentCache(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            this.h.invoke(this, m4, new Object[]{tessBaseAPI});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessMutableIterator TessBaseAPIGetMutableIterator(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (ITessAPI.TessMutableIterator)this.h.invoke(this, m5, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessPageIteratorIsAtBeginningOf(ITessAPI.TessPageIterator tessPageIterator, int n) {
        try {
            return (Integer)this.h.invoke(this, m6, new Object[]{tessPageIterator, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessPageIteratorIsAtFinalElement(ITessAPI.TessPageIterator tessPageIterator, int n, int n2) {
        try {
            return (Integer)this.h.invoke(this, m7, new Object[]{tessPageIterator, n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pix TessPageIteratorGetBinaryImage(ITessAPI.TessPageIterator tessPageIterator, int n) {
        try {
            return (Pix)this.h.invoke(this, m8, new Object[]{tessPageIterator, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIGetTextDirection(ITessAPI.TessBaseAPI tessBaseAPI, IntBuffer intBuffer, FloatBuffer floatBuffer) {
        try {
            return (Integer)this.h.invoke(this, m9, new Object[]{tessBaseAPI, intBuffer, floatBuffer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessResultRendererInsert(ITessAPI.TessResultRenderer tessResultRenderer, ITessAPI.TessResultRenderer tessResultRenderer2) {
        try {
            this.h.invoke(this, m10, new Object[]{tessResultRenderer, tessResultRenderer2});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessAltoRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m11, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessPAGERendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m12, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessLSTMBoxRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m13, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIProcessPages(ITessAPI.TessBaseAPI tessBaseAPI, String string, String string2, int n, ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            return (Integer)this.h.invoke(this, m14, new Object[]{tessBaseAPI, string, string2, n, tessResultRenderer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIMeanTextConf(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Integer)this.h.invoke(this, m15, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetLSTMBoxText(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (Pointer)this.h.invoke(this, m16, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetPAGEText(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (Pointer)this.h.invoke(this, m17, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessTextRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m18, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessPDFRendererCreate(String string, String string2, int n) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m19, new Object[]{string, string2, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessBoxTextRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m20, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetUTF8Text(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Pointer)this.h.invoke(this, m21, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetTsvText(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (Pointer)this.h.invoke(this, m22, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessUnlvRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m23, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessTsvRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m24, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessDeleteResultRenderer(ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            this.h.invoke(this, m25, new Object[]{tessResultRenderer});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIProcessPage(ITessAPI.TessBaseAPI tessBaseAPI, Pix pix, int n, String string, String string2, int n2, ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            return (Integer)this.h.invoke(this, m26, new Object[]{tessBaseAPI, pix, n, string, string2, n2, tessResultRenderer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIRecognize(ITessAPI.TessBaseAPI tessBaseAPI, ITessAPI.ETEXT_DESC eTEXT_DESC) {
        try {
            return (Integer)this.h.invoke(this, m27, new Object[]{tessBaseAPI, eTEXT_DESC});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessHOcrRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m28, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessPageIteratorParagraphInfo(ITessAPI.TessPageIterator tessPageIterator, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3, IntBuffer intBuffer4) {
        try {
            this.h.invoke(this, m29, new Object[]{tessPageIterator, intBuffer, intBuffer2, intBuffer3, intBuffer4});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultIteratorWordIsNumeric(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (Integer)this.h.invoke(this, m30, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultIteratorSymbolIsDropcap(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (Integer)this.h.invoke(this, m31, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessChoiceIteratorGetUTF8Text(ITessAPI.TessChoiceIterator tessChoiceIterator) {
        try {
            return (String)this.h.invoke(this, m32, new Object[]{tessChoiceIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final float TessChoiceIteratorConfidence(ITessAPI.TessChoiceIterator tessChoiceIterator) {
        try {
            return ((Float)this.h.invoke(this, m33, new Object[]{tessChoiceIterator})).floatValue();
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessPageIteratorOrientation(ITessAPI.TessPageIterator tessPageIterator, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3, FloatBuffer floatBuffer) {
        try {
            this.h.invoke(this, m34, new Object[]{tessPageIterator, intBuffer, intBuffer2, intBuffer3, floatBuffer});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessMonitorSetDeadlineMSecs(ITessAPI.ETEXT_DESC eTEXT_DESC, int n) {
        try {
            this.h.invoke(this, m35, new Object[]{eTEXT_DESC, n});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetWordStrBoxText(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (Pointer)this.h.invoke(this, m36, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessWordStrBoxRendererCreate(String string) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m37, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessBaseAPIGetStringVariable(ITessAPI.TessBaseAPI tessBaseAPI, String string) {
        try {
            return (String)this.h.invoke(this, m38, new Object[]{tessBaseAPI, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultRendererBeginDocument(ITessAPI.TessResultRenderer tessResultRenderer, String string) {
        try {
            return (Integer)this.h.invoke(this, m39, new Object[]{tessResultRenderer, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultRendererEndDocument(ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            return (Integer)this.h.invoke(this, m40, new Object[]{tessResultRenderer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetComponentImages(ITessAPI.TessBaseAPI tessBaseAPI, int n, int n2, PointerByReference pointerByReference, PointerByReference pointerByReference2) {
        try {
            return (Boxa)this.h.invoke(this, m41, new Object[]{tessBaseAPI, n, n2, pointerByReference, pointerByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIDetectOrientationScript(ITessAPI.TessBaseAPI tessBaseAPI, IntBuffer intBuffer, FloatBuffer floatBuffer, PointerByReference pointerByReference, FloatBuffer floatBuffer2) {
        try {
            return (Integer)this.h.invoke(this, m42, new Object[]{tessBaseAPI, intBuffer, floatBuffer, pointerByReference, floatBuffer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessResultIteratorGetUTF8Text(ITessAPI.TessResultIterator tessResultIterator, int n) {
        try {
            return (Pointer)this.h.invoke(this, m43, new Object[]{tessResultIterator, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final float TessResultIteratorConfidence(ITessAPI.TessResultIterator tessResultIterator, int n) {
        try {
            return ((Float)this.h.invoke(this, m44, new Object[]{tessResultIterator, n})).floatValue();
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessPageIterator TessResultIteratorGetPageIterator(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (ITessAPI.TessPageIterator)this.h.invoke(this, m45, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessPageIteratorBoundingBox(ITessAPI.TessPageIterator tessPageIterator, int n, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3, IntBuffer intBuffer4) {
        try {
            return (Integer)this.h.invoke(this, m46, new Object[]{tessPageIterator, n, intBuffer, intBuffer2, intBuffer3, intBuffer4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final PointerByReference TessBaseAPIGetAvailableLanguagesAsVector(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (PointerByReference)this.h.invoke(this, m47, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final PointerByReference TessBaseAPIGetLoadedLanguagesAsVector(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (PointerByReference)this.h.invoke(this, m48, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessPageIteratorBegin(ITessAPI.TessPageIterator tessPageIterator) {
        try {
            this.h.invoke(this, m49, new Object[]{tessPageIterator});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessPageIteratorNext(ITessAPI.TessPageIterator tessPageIterator, int n) {
        try {
            return (Integer)this.h.invoke(this, m50, new Object[]{tessPageIterator, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultIterator TessBaseAPIGetIterator(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (ITessAPI.TessResultIterator)this.h.invoke(this, m51, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessResultRendererExtention(ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            return (Pointer)this.h.invoke(this, m52, new Object[]{tessResultRenderer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIGetSourceYResolution(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Integer)this.h.invoke(this, m53, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIGetBoolVariable(ITessAPI.TessBaseAPI tessBaseAPI, String string, IntBuffer intBuffer) {
        try {
            return (Integer)this.h.invoke(this, m54, new Object[]{tessBaseAPI, string, intBuffer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetRegions(ITessAPI.TessBaseAPI tessBaseAPI, PointerByReference pointerByReference) {
        try {
            return (Boxa)this.h.invoke(this, m55, new Object[]{tessBaseAPI, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessBaseAPIGetUnichar(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (String)this.h.invoke(this, m56, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessResultRendererNext(ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m57, new Object[]{tessResultRenderer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultRendererImageNum(ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            return (Integer)this.h.invoke(this, m58, new Object[]{tessResultRenderer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessResultIteratorDelete(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            this.h.invoke(this, m59, new Object[]{tessResultIterator});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessResultRendererTitle(ITessAPI.TessResultRenderer tessResultRenderer) {
        try {
            return (Pointer)this.h.invoke(this, m60, new Object[]{tessResultRenderer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessBaseAPIGetInputName(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (String)this.h.invoke(this, m61, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetInputImage(ITessAPI.TessBaseAPI tessBaseAPI, Pix pix) {
        try {
            this.h.invoke(this, m62, new Object[]{tessBaseAPI, pix});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetOutputName(ITessAPI.TessBaseAPI tessBaseAPI, String string) {
        try {
            this.h.invoke(this, m63, new Object[]{tessBaseAPI, string});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIGetPageSegMode(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Integer)this.h.invoke(this, m64, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetTextlines1(ITessAPI.TessBaseAPI tessBaseAPI, int n, int n2, PointerByReference pointerByReference, PointerByReference pointerByReference2, PointerByReference pointerByReference3) {
        try {
            return (Boxa)this.h.invoke(this, m65, new Object[]{tessBaseAPI, n, n2, pointerByReference, pointerByReference2, pointerByReference3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetWords(ITessAPI.TessBaseAPI tessBaseAPI, PointerByReference pointerByReference) {
        try {
            return (Boxa)this.h.invoke(this, m66, new Object[]{tessBaseAPI, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessPageIterator TessBaseAPIAnalyseLayout(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (ITessAPI.TessPageIterator)this.h.invoke(this, m67, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetBoxText(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (Pointer)this.h.invoke(this, m68, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessPageIteratorDelete(ITessAPI.TessPageIterator tessPageIterator) {
        try {
            this.h.invoke(this, m69, new Object[]{tessPageIterator});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final float TessBaseAPIGetGradient(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return ((Float)this.h.invoke(this, m70, new Object[]{tessBaseAPI})).floatValue();
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessPageIteratorBlockType(ITessAPI.TessPageIterator tessPageIterator) {
        try {
            return (Integer)this.h.invoke(this, m71, new Object[]{tessPageIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessPageIteratorBaseline(ITessAPI.TessPageIterator tessPageIterator, int n, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3, IntBuffer intBuffer4) {
        try {
            return (Integer)this.h.invoke(this, m72, new Object[]{tessPageIterator, n, intBuffer, intBuffer2, intBuffer3, intBuffer4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIReadConfigFile(ITessAPI.TessBaseAPI tessBaseAPI, String string, int n) {
        try {
            this.h.invoke(this, m73, new Object[]{tessBaseAPI, string, n});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIAdaptToWordStr(ITessAPI.TessBaseAPI tessBaseAPI, int n, String string) {
        try {
            return (Integer)this.h.invoke(this, m74, new Object[]{tessBaseAPI, n, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultIterator TessResultIteratorCopy(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (ITessAPI.TessResultIterator)this.h.invoke(this, m75, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultIteratorNext(ITessAPI.TessResultIterator tessResultIterator, int n) {
        try {
            return (Integer)this.h.invoke(this, m76, new Object[]{tessResultIterator, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessChoiceIteratorDelete(ITessAPI.TessChoiceIterator tessChoiceIterator) {
        try {
            this.h.invoke(this, m77, new Object[]{tessChoiceIterator});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessChoiceIteratorNext(ITessAPI.TessChoiceIterator tessChoiceIterator) {
        try {
            return (Integer)this.h.invoke(this, m78, new Object[]{tessChoiceIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pix TessPageIteratorGetImage(ITessAPI.TessPageIterator tessPageIterator, int n, int n2, Pix pix, IntBuffer intBuffer, IntBuffer intBuffer2) {
        try {
            return (Pix)this.h.invoke(this, m79, new Object[]{tessPageIterator, n, n2, pix, intBuffer, intBuffer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultRendererAddImage(ITessAPI.TessResultRenderer tessResultRenderer, PointerByReference pointerByReference) {
        try {
            return (Integer)this.h.invoke(this, m80, new Object[]{tessResultRenderer, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIIsValidWord(ITessAPI.TessBaseAPI tessBaseAPI, String string) {
        try {
            return (Integer)this.h.invoke(this, m81, new Object[]{tessBaseAPI, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pix TessBaseAPIGetInputImage(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Pix)this.h.invoke(this, m82, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIGetIntVariable(ITessAPI.TessBaseAPI tessBaseAPI, String string, IntBuffer intBuffer) {
        try {
            return (Integer)this.h.invoke(this, m83, new Object[]{tessBaseAPI, string, intBuffer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetStrips(ITessAPI.TessBaseAPI tessBaseAPI, PointerByReference pointerByReference, PointerByReference pointerByReference2) {
        try {
            return (Boxa)this.h.invoke(this, m84, new Object[]{tessBaseAPI, pointerByReference, pointerByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessPageIterator TessPageIteratorCopy(ITessAPI.TessPageIterator tessPageIterator) {
        try {
            return (ITessAPI.TessPageIterator)this.h.invoke(this, m85, new Object[]{tessPageIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessResultRenderer TessHOcrRendererCreate2(String string, int n) {
        try {
            return (ITessAPI.TessResultRenderer)this.h.invoke(this, m86, new Object[]{string, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetTextlines(ITessAPI.TessBaseAPI tessBaseAPI, PointerByReference pointerByReference, PointerByReference pointerByReference2) {
        try {
            return (Boxa)this.h.invoke(this, m87, new Object[]{tessBaseAPI, pointerByReference, pointerByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessMonitorSetCancelFunc(ITessAPI.ETEXT_DESC eTEXT_DESC, ITessAPI.TessCancelFunc tessCancelFunc) {
        try {
            this.h.invoke(this, m88, new Object[]{eTEXT_DESC, tessCancelFunc});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessMonitorGetCancelThis(ITessAPI.ETEXT_DESC eTEXT_DESC) {
        try {
            return (Pointer)this.h.invoke(this, m89, new Object[]{eTEXT_DESC});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessMonitorGetProgress(ITessAPI.ETEXT_DESC eTEXT_DESC) {
        try {
            return (Integer)this.h.invoke(this, m90, new Object[]{eTEXT_DESC});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessMonitorSetProgressFunc(ITessAPI.ETEXT_DESC eTEXT_DESC, ITessAPI.TessProgressFunc tessProgressFunc) {
        try {
            this.h.invoke(this, m91, new Object[]{eTEXT_DESC, tessProgressFunc});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessMonitorSetCancelThis(ITessAPI.ETEXT_DESC eTEXT_DESC, Pointer pointer) {
        try {
            this.h.invoke(this, m92, new Object[]{eTEXT_DESC, pointer});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultIteratorSymbolIsSuperscript(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (Integer)this.h.invoke(this, m93, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIGetThresholdedImageScaleFactor(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Integer)this.h.invoke(this, m94, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultIteratorWordIsFromDictionary(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (Integer)this.h.invoke(this, m95, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessPageIterator TessResultIteratorGetPageIteratorConst(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (ITessAPI.TessPageIterator)this.h.invoke(this, m96, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessResultIteratorWordRecognitionLanguage(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (String)this.h.invoke(this, m97, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessResultIteratorWordFontAttributes(ITessAPI.TessResultIterator tessResultIterator, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3, IntBuffer intBuffer4, IntBuffer intBuffer5, IntBuffer intBuffer6, IntBuffer intBuffer7, IntBuffer intBuffer8) {
        try {
            return (String)this.h.invoke(this, m98, new Object[]{tessResultIterator, intBuffer, intBuffer2, intBuffer3, intBuffer4, intBuffer5, intBuffer6, intBuffer7, intBuffer8});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessChoiceIterator TessResultIteratorGetChoiceIterator(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (ITessAPI.TessChoiceIterator)this.h.invoke(this, m99, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessResultIteratorSymbolIsSubscript(ITessAPI.TessResultIterator tessResultIterator) {
        try {
            return (Integer)this.h.invoke(this, m100, new Object[]{tessResultIterator});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessBaseAPIGetInitLanguagesAsString(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (String)this.h.invoke(this, m101, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIInit4(ITessAPI.TessBaseAPI tessBaseAPI, String string, String string2, int n, PointerByReference pointerByReference, int n2, PointerByReference pointerByReference2, PointerByReference pointerByReference3, NativeSize nativeSize, int n3) {
        try {
            return (Integer)this.h.invoke(this, m102, new Object[]{tessBaseAPI, string, string2, n, pointerByReference, n2, pointerByReference2, pointerByReference3, nativeSize, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessVersion() {
        try {
            return (String)this.h.invoke(this, m103, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessDeleteIntArray(IntBuffer intBuffer) {
        try {
            this.h.invoke(this, m104, new Object[]{intBuffer});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIInit2(ITessAPI.TessBaseAPI tessBaseAPI, String string, String string2, int n) {
        try {
            return (Integer)this.h.invoke(this, m105, new Object[]{tessBaseAPI, string, string2, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIClear(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            this.h.invoke(this, m106, new Object[]{tessBaseAPI});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIEnd(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            this.h.invoke(this, m107, new Object[]{tessBaseAPI});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.ETEXT_DESC TessMonitorCreate() {
        try {
            return (ITessAPI.ETEXT_DESC)this.h.invoke(this, m108, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessMonitorDelete(ITessAPI.ETEXT_DESC eTEXT_DESC) {
        try {
            this.h.invoke(this, m109, new Object[]{eTEXT_DESC});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIInit5(ITessAPI.TessBaseAPI tessBaseAPI, String string, int n, String string2, int n2, PointerByReference pointerByReference, int n3, PointerByReference pointerByReference2, PointerByReference pointerByReference3, NativeSize nativeSize, int n4) {
        try {
            return (Integer)this.h.invoke(this, m110, new Object[]{tessBaseAPI, string, n, string2, n2, pointerByReference, n3, pointerByReference2, pointerByReference3, nativeSize, n4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIRect(ITessAPI.TessBaseAPI tessBaseAPI, ByteBuffer byteBuffer, int n, int n2, int n3, int n4, int n5, int n6) {
        try {
            return (Pointer)this.h.invoke(this, m111, new Object[]{tessBaseAPI, byteBuffer, n, n2, n3, n4, n5, n6});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final ITessAPI.TessBaseAPI TessBaseAPICreate() {
        try {
            return (ITessAPI.TessBaseAPI)this.h.invoke(this, m112, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessDeleteText(Pointer pointer) {
        try {
            this.h.invoke(this, m113, new Object[]{pointer});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIInit1(ITessAPI.TessBaseAPI tessBaseAPI, String string, String string2, int n, PointerByReference pointerByReference, int n2) {
        try {
            return (Integer)this.h.invoke(this, m114, new Object[]{tessBaseAPI, string, string2, n, pointerByReference, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIDelete(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            this.h.invoke(this, m115, new Object[]{tessBaseAPI});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIInit3(ITessAPI.TessBaseAPI tessBaseAPI, String string, String string2) {
        try {
            return (Integer)this.h.invoke(this, m116, new Object[]{tessBaseAPI, string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final String TessBaseAPIGetDatapath(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (String)this.h.invoke(this, m117, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetImage2(ITessAPI.TessBaseAPI tessBaseAPI, Pix pix) {
        try {
            this.h.invoke(this, m118, new Object[]{tessBaseAPI, pix});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessDeleteTextArray(PointerByReference pointerByReference) {
        try {
            this.h.invoke(this, m119, new Object[]{pointerByReference});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetRectangle(ITessAPI.TessBaseAPI tessBaseAPI, int n, int n2, int n3, int n4) {
        try {
            this.h.invoke(this, m120, new Object[]{tessBaseAPI, n, n2, n3, n4});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetAltoText(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (Pointer)this.h.invoke(this, m121, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetImage(ITessAPI.TessBaseAPI tessBaseAPI, ByteBuffer byteBuffer, int n, int n2, int n3, int n4) {
        try {
            this.h.invoke(this, m122, new Object[]{tessBaseAPI, byteBuffer, n, n2, n3, n4});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPISetVariable(ITessAPI.TessBaseAPI tessBaseAPI, String string, String string2) {
        try {
            return (Integer)this.h.invoke(this, m123, new Object[]{tessBaseAPI, string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetHOCRText(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            return (Pointer)this.h.invoke(this, m124, new Object[]{tessBaseAPI, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetInputName(ITessAPI.TessBaseAPI tessBaseAPI, String string) {
        try {
            this.h.invoke(this, m125, new Object[]{tessBaseAPI, string});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer TessBaseAPIGetUNLVText(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Pointer)this.h.invoke(this, m126, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetPageSegMode(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            this.h.invoke(this, m127, new Object[]{tessBaseAPI, n});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIClearAdaptiveClassifier(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            this.h.invoke(this, m128, new Object[]{tessBaseAPI});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIInitForAnalysePage(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            this.h.invoke(this, m129, new Object[]{tessBaseAPI});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPISetSourceResolution(ITessAPI.TessBaseAPI tessBaseAPI, int n) {
        try {
            this.h.invoke(this, m130, new Object[]{tessBaseAPI, n});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pix TessBaseAPIGetThresholdedImage(ITessAPI.TessBaseAPI tessBaseAPI) {
        try {
            return (Pix)this.h.invoke(this, m131, new Object[]{tessBaseAPI});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetConnectedComponents(ITessAPI.TessBaseAPI tessBaseAPI, PointerByReference pointerByReference) {
        try {
            return (Boxa)this.h.invoke(this, m132, new Object[]{tessBaseAPI, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Boxa TessBaseAPIGetComponentImages1(ITessAPI.TessBaseAPI tessBaseAPI, int n, int n2, int n3, int n4, PointerByReference pointerByReference, PointerByReference pointerByReference2, PointerByReference pointerByReference3) {
        try {
            return (Boxa)this.h.invoke(this, m133, new Object[]{tessBaseAPI, n, n2, n3, n4, pointerByReference, pointerByReference2, pointerByReference3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int TessBaseAPIGetDoubleVariable(ITessAPI.TessBaseAPI tessBaseAPI, String string, DoubleBuffer doubleBuffer) {
        try {
            return (Integer)this.h.invoke(this, m134, new Object[]{tessBaseAPI, string, doubleBuffer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void TessBaseAPIPrintVariablesToFile(ITessAPI.TessBaseAPI tessBaseAPI, String string) {
        try {
            this.h.invoke(this, m135, new Object[]{tessBaseAPI, string});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy18.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIAllWordConfidences", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m4 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIClearPersistentCache", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m5 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetMutableIterator", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m6 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorIsAtBeginningOf", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Integer.TYPE);
            m7 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorIsAtFinalElement", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Integer.TYPE, Integer.TYPE);
            m8 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorGetBinaryImage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Integer.TYPE);
            m9 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetTextDirection", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.FloatBuffer", false, classLoader));
            m10 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererInsert", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader), Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m11 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessAltoRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m12 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPAGERendererCreate", Class.forName("java.lang.String", false, classLoader));
            m13 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessLSTMBoxRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m14 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIProcessPages", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m15 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIMeanTextConf", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m16 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetLSTMBoxText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m17 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetPAGEText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m18 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessTextRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m19 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPDFRendererCreate", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE);
            m20 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBoxTextRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m21 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetUTF8Text", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m22 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetTsvText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m23 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessUnlvRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m24 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessTsvRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m25 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessDeleteResultRenderer", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m26 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIProcessPage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("net.sourceforge.lept4j.Pix", false, classLoader), Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m27 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIRecognize", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader));
            m28 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessHOcrRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m29 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorParagraphInfo", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader));
            m30 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorWordIsNumeric", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m31 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorSymbolIsDropcap", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m32 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessChoiceIteratorGetUTF8Text", Class.forName("net.sourceforge.tess4j.ITessAPI$TessChoiceIterator", false, classLoader));
            m33 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessChoiceIteratorConfidence", Class.forName("net.sourceforge.tess4j.ITessAPI$TessChoiceIterator", false, classLoader));
            m34 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorOrientation", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.FloatBuffer", false, classLoader));
            m35 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorSetDeadlineMSecs", Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader), Integer.TYPE);
            m36 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetWordStrBoxText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m37 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessWordStrBoxRendererCreate", Class.forName("java.lang.String", false, classLoader));
            m38 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetStringVariable", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m39 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererBeginDocument", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m40 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererEndDocument", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m41 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetComponentImages", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m42 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIDetectOrientationScript", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.FloatBuffer", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("java.nio.FloatBuffer", false, classLoader));
            m43 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorGetUTF8Text", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader), Integer.TYPE);
            m44 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorConfidence", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader), Integer.TYPE);
            m45 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorGetPageIterator", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m46 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorBoundingBox", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Integer.TYPE, Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader));
            m47 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetAvailableLanguagesAsVector", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m48 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetLoadedLanguagesAsVector", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m49 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorBegin", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader));
            m50 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorNext", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Integer.TYPE);
            m51 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetIterator", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m52 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererExtention", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m53 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetSourceYResolution", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m54 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetBoolVariable", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader));
            m55 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetRegions", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m56 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetUnichar", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m57 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererNext", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m58 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererImageNum", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m59 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorDelete", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m60 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererTitle", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader));
            m61 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetInputName", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m62 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetInputImage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("net.sourceforge.lept4j.Pix", false, classLoader));
            m63 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetOutputName", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m64 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetPageSegMode", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m65 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetTextlines1", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m66 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetWords", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m67 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIAnalyseLayout", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m68 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetBoxText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m69 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorDelete", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader));
            m70 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetGradient", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m71 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorBlockType", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader));
            m72 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorBaseline", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Integer.TYPE, Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader));
            m73 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIReadConfigFile", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE);
            m74 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIAdaptToWordStr", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE, Class.forName("java.lang.String", false, classLoader));
            m75 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorCopy", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m76 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorNext", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader), Integer.TYPE);
            m77 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessChoiceIteratorDelete", Class.forName("net.sourceforge.tess4j.ITessAPI$TessChoiceIterator", false, classLoader));
            m78 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessChoiceIteratorNext", Class.forName("net.sourceforge.tess4j.ITessAPI$TessChoiceIterator", false, classLoader));
            m79 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorGetImage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("net.sourceforge.lept4j.Pix", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader));
            m80 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultRendererAddImage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultRenderer", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m81 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIIsValidWord", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m82 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetInputImage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m83 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetIntVariable", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader));
            m84 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetStrips", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m85 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessPageIteratorCopy", Class.forName("net.sourceforge.tess4j.ITessAPI$TessPageIterator", false, classLoader));
            m86 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessHOcrRendererCreate2", Class.forName("java.lang.String", false, classLoader), Integer.TYPE);
            m87 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetTextlines", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m88 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorSetCancelFunc", Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader), Class.forName("net.sourceforge.tess4j.ITessAPI$TessCancelFunc", false, classLoader));
            m89 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorGetCancelThis", Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader));
            m90 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorGetProgress", Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader));
            m91 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorSetProgressFunc", Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader), Class.forName("net.sourceforge.tess4j.ITessAPI$TessProgressFunc", false, classLoader));
            m92 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorSetCancelThis", Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m93 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorSymbolIsSuperscript", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m94 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetThresholdedImageScaleFactor", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m95 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorWordIsFromDictionary", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m96 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorGetPageIteratorConst", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m97 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorWordRecognitionLanguage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m98 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorWordFontAttributes", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader), Class.forName("java.nio.IntBuffer", false, classLoader));
            m99 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorGetChoiceIterator", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m100 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessResultIteratorSymbolIsSubscript", Class.forName("net.sourceforge.tess4j.ITessAPI$TessResultIterator", false, classLoader));
            m101 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetInitLanguagesAsString", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m102 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIInit4", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.ochafik.lang.jnaerator.runtime.NativeSize", false, classLoader), Integer.TYPE);
            m103 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessVersion", new Class[0]);
            m104 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessDeleteIntArray", Class.forName("java.nio.IntBuffer", false, classLoader));
            m105 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIInit2", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE);
            m106 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIClear", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m107 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIEnd", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m108 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorCreate", new Class[0]);
            m109 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessMonitorDelete", Class.forName("net.sourceforge.tess4j.ITessAPI$ETEXT_DESC", false, classLoader));
            m110 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIInit5", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.ochafik.lang.jnaerator.runtime.NativeSize", false, classLoader), Integer.TYPE);
            m111 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIRect", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.nio.ByteBuffer", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m112 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPICreate", new Class[0]);
            m113 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessDeleteText", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m114 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIInit1", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Integer.TYPE);
            m115 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIDelete", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m116 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIInit3", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m117 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetDatapath", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m118 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetImage2", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("net.sourceforge.lept4j.Pix", false, classLoader));
            m119 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessDeleteTextArray", Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m120 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetRectangle", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m121 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetAltoText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m122 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetImage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.nio.ByteBuffer", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m123 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetVariable", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m124 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetHOCRText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m125 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetInputName", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m126 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetUNLVText", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m127 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetPageSegMode", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m128 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIClearAdaptiveClassifier", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m129 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIInitForAnalysePage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m130 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPISetSourceResolution", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE);
            m131 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetThresholdedImage", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader));
            m132 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetConnectedComponents", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m133 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetComponentImages1", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m134 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIGetDoubleVariable", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.nio.DoubleBuffer", false, classLoader));
            m135 = Class.forName("net.sourceforge.tess4j.TessAPI", false, classLoader).getMethod("TessBaseAPIPrintVariablesToFile", Class.forName("net.sourceforge.tess4j.ITessAPI$TessBaseAPI", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            return;
        }
        catch (NoSuchMethodException noSuchMethodException) {
            throw new NoSuchMethodError(noSuchMethodException.getMessage());
        }
        catch (ClassNotFoundException classNotFoundException) {
            throw new NoClassDefFoundError(classNotFoundException.getMessage());
        }
    }

    private static MethodHandles.Lookup proxyClassLookup(MethodHandles.Lookup lookup) throws IllegalAccessException {
        if (lookup.lookupClass() == Proxy.class && lookup.hasFullPrivilegeAccess()) {
            return MethodHandles.lookup();
        }
        throw new IllegalAccessException(lookup.toString());
    }
}

