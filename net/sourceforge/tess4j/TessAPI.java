/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.tess4j;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.util.LoadLibs;

public interface TessAPI
extends Library,
ITessAPI {
    public static final TessAPI INSTANCE = LoadLibs.getTessAPIInstance();

    public String TessVersion();

    public void TessDeleteText(Pointer var1);

    public void TessDeleteTextArray(PointerByReference var1);

    public void TessDeleteIntArray(IntBuffer var1);

    public ITessAPI.TessResultRenderer TessTextRendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessHOcrRendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessHOcrRendererCreate2(String var1, int var2);

    public ITessAPI.TessResultRenderer TessAltoRendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessPAGERendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessTsvRendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessPDFRendererCreate(String var1, String var2, int var3);

    public ITessAPI.TessResultRenderer TessUnlvRendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessBoxTextRendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessLSTMBoxRendererCreate(String var1);

    public ITessAPI.TessResultRenderer TessWordStrBoxRendererCreate(String var1);

    public void TessDeleteResultRenderer(ITessAPI.TessResultRenderer var1);

    public void TessResultRendererInsert(ITessAPI.TessResultRenderer var1, ITessAPI.TessResultRenderer var2);

    public ITessAPI.TessResultRenderer TessResultRendererNext(ITessAPI.TessResultRenderer var1);

    public int TessResultRendererBeginDocument(ITessAPI.TessResultRenderer var1, String var2);

    public int TessResultRendererAddImage(ITessAPI.TessResultRenderer var1, PointerByReference var2);

    public int TessResultRendererEndDocument(ITessAPI.TessResultRenderer var1);

    public Pointer TessResultRendererExtention(ITessAPI.TessResultRenderer var1);

    public Pointer TessResultRendererTitle(ITessAPI.TessResultRenderer var1);

    public int TessResultRendererImageNum(ITessAPI.TessResultRenderer var1);

    public ITessAPI.TessBaseAPI TessBaseAPICreate();

    public void TessBaseAPIDelete(ITessAPI.TessBaseAPI var1);

    public void TessBaseAPISetInputName(ITessAPI.TessBaseAPI var1, String var2);

    public String TessBaseAPIGetInputName(ITessAPI.TessBaseAPI var1);

    public void TessBaseAPISetInputImage(ITessAPI.TessBaseAPI var1, Pix var2);

    public Pix TessBaseAPIGetInputImage(ITessAPI.TessBaseAPI var1);

    public int TessBaseAPIGetSourceYResolution(ITessAPI.TessBaseAPI var1);

    public String TessBaseAPIGetDatapath(ITessAPI.TessBaseAPI var1);

    public void TessBaseAPISetOutputName(ITessAPI.TessBaseAPI var1, String var2);

    public int TessBaseAPISetVariable(ITessAPI.TessBaseAPI var1, String var2, String var3);

    public int TessBaseAPIGetIntVariable(ITessAPI.TessBaseAPI var1, String var2, IntBuffer var3);

    public int TessBaseAPIGetBoolVariable(ITessAPI.TessBaseAPI var1, String var2, IntBuffer var3);

    public int TessBaseAPIGetDoubleVariable(ITessAPI.TessBaseAPI var1, String var2, DoubleBuffer var3);

    public String TessBaseAPIGetStringVariable(ITessAPI.TessBaseAPI var1, String var2);

    public void TessBaseAPIPrintVariablesToFile(ITessAPI.TessBaseAPI var1, String var2);

    public int TessBaseAPIInit1(ITessAPI.TessBaseAPI var1, String var2, String var3, int var4, PointerByReference var5, int var6);

    public int TessBaseAPIInit2(ITessAPI.TessBaseAPI var1, String var2, String var3, int var4);

    public int TessBaseAPIInit3(ITessAPI.TessBaseAPI var1, String var2, String var3);

    public int TessBaseAPIInit4(ITessAPI.TessBaseAPI var1, String var2, String var3, int var4, PointerByReference var5, int var6, PointerByReference var7, PointerByReference var8, NativeSize var9, int var10);

    public int TessBaseAPIInit5(ITessAPI.TessBaseAPI var1, String var2, int var3, String var4, int var5, PointerByReference var6, int var7, PointerByReference var8, PointerByReference var9, NativeSize var10, int var11);

    public String TessBaseAPIGetInitLanguagesAsString(ITessAPI.TessBaseAPI var1);

    public PointerByReference TessBaseAPIGetLoadedLanguagesAsVector(ITessAPI.TessBaseAPI var1);

    public PointerByReference TessBaseAPIGetAvailableLanguagesAsVector(ITessAPI.TessBaseAPI var1);

    public void TessBaseAPIInitForAnalysePage(ITessAPI.TessBaseAPI var1);

    public void TessBaseAPIReadConfigFile(ITessAPI.TessBaseAPI var1, String var2, int var3);

    public void TessBaseAPISetPageSegMode(ITessAPI.TessBaseAPI var1, int var2);

    public int TessBaseAPIGetPageSegMode(ITessAPI.TessBaseAPI var1);

    public Pointer TessBaseAPIRect(ITessAPI.TessBaseAPI var1, ByteBuffer var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public void TessBaseAPIClearAdaptiveClassifier(ITessAPI.TessBaseAPI var1);

    public void TessBaseAPISetImage(ITessAPI.TessBaseAPI var1, ByteBuffer var2, int var3, int var4, int var5, int var6);

    public void TessBaseAPISetImage2(ITessAPI.TessBaseAPI var1, Pix var2);

    public void TessBaseAPISetSourceResolution(ITessAPI.TessBaseAPI var1, int var2);

    public void TessBaseAPISetRectangle(ITessAPI.TessBaseAPI var1, int var2, int var3, int var4, int var5);

    public Pix TessBaseAPIGetThresholdedImage(ITessAPI.TessBaseAPI var1);

    public float TessBaseAPIGetGradient(ITessAPI.TessBaseAPI var1);

    public Boxa TessBaseAPIGetRegions(ITessAPI.TessBaseAPI var1, PointerByReference var2);

    public Boxa TessBaseAPIGetTextlines(ITessAPI.TessBaseAPI var1, PointerByReference var2, PointerByReference var3);

    public Boxa TessBaseAPIGetTextlines1(ITessAPI.TessBaseAPI var1, int var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6);

    public Boxa TessBaseAPIGetStrips(ITessAPI.TessBaseAPI var1, PointerByReference var2, PointerByReference var3);

    public Boxa TessBaseAPIGetWords(ITessAPI.TessBaseAPI var1, PointerByReference var2);

    public Boxa TessBaseAPIGetConnectedComponents(ITessAPI.TessBaseAPI var1, PointerByReference var2);

    public Boxa TessBaseAPIGetComponentImages(ITessAPI.TessBaseAPI var1, int var2, int var3, PointerByReference var4, PointerByReference var5);

    public Boxa TessBaseAPIGetComponentImages1(ITessAPI.TessBaseAPI var1, int var2, int var3, int var4, int var5, PointerByReference var6, PointerByReference var7, PointerByReference var8);

    public int TessBaseAPIGetThresholdedImageScaleFactor(ITessAPI.TessBaseAPI var1);

    public ITessAPI.TessPageIterator TessBaseAPIAnalyseLayout(ITessAPI.TessBaseAPI var1);

    public int TessBaseAPIRecognize(ITessAPI.TessBaseAPI var1, ITessAPI.ETEXT_DESC var2);

    public ITessAPI.TessResultIterator TessBaseAPIGetIterator(ITessAPI.TessBaseAPI var1);

    public ITessAPI.TessMutableIterator TessBaseAPIGetMutableIterator(ITessAPI.TessBaseAPI var1);

    public int TessBaseAPIProcessPages(ITessAPI.TessBaseAPI var1, String var2, String var3, int var4, ITessAPI.TessResultRenderer var5);

    public int TessBaseAPIProcessPage(ITessAPI.TessBaseAPI var1, Pix var2, int var3, String var4, String var5, int var6, ITessAPI.TessResultRenderer var7);

    public Pointer TessBaseAPIGetUTF8Text(ITessAPI.TessBaseAPI var1);

    public Pointer TessBaseAPIGetHOCRText(ITessAPI.TessBaseAPI var1, int var2);

    public Pointer TessBaseAPIGetAltoText(ITessAPI.TessBaseAPI var1, int var2);

    public Pointer TessBaseAPIGetPAGEText(ITessAPI.TessBaseAPI var1, int var2);

    public Pointer TessBaseAPIGetTsvText(ITessAPI.TessBaseAPI var1, int var2);

    public Pointer TessBaseAPIGetBoxText(ITessAPI.TessBaseAPI var1, int var2);

    public Pointer TessBaseAPIGetLSTMBoxText(ITessAPI.TessBaseAPI var1, int var2);

    public Pointer TessBaseAPIGetWordStrBoxText(ITessAPI.TessBaseAPI var1, int var2);

    public Pointer TessBaseAPIGetUNLVText(ITessAPI.TessBaseAPI var1);

    public int TessBaseAPIMeanTextConf(ITessAPI.TessBaseAPI var1);

    public IntByReference TessBaseAPIAllWordConfidences(ITessAPI.TessBaseAPI var1);

    public int TessBaseAPIAdaptToWordStr(ITessAPI.TessBaseAPI var1, int var2, String var3);

    public void TessBaseAPIClear(ITessAPI.TessBaseAPI var1);

    public void TessBaseAPIEnd(ITessAPI.TessBaseAPI var1);

    public int TessBaseAPIIsValidWord(ITessAPI.TessBaseAPI var1, String var2);

    public int TessBaseAPIGetTextDirection(ITessAPI.TessBaseAPI var1, IntBuffer var2, FloatBuffer var3);

    public void TessBaseAPIClearPersistentCache(ITessAPI.TessBaseAPI var1);

    public int TessBaseAPIDetectOrientationScript(ITessAPI.TessBaseAPI var1, IntBuffer var2, FloatBuffer var3, PointerByReference var4, FloatBuffer var5);

    public String TessBaseAPIGetUnichar(ITessAPI.TessBaseAPI var1, int var2);

    public void TessPageIteratorDelete(ITessAPI.TessPageIterator var1);

    public ITessAPI.TessPageIterator TessPageIteratorCopy(ITessAPI.TessPageIterator var1);

    public void TessPageIteratorBegin(ITessAPI.TessPageIterator var1);

    public int TessPageIteratorNext(ITessAPI.TessPageIterator var1, int var2);

    public int TessPageIteratorIsAtBeginningOf(ITessAPI.TessPageIterator var1, int var2);

    public int TessPageIteratorIsAtFinalElement(ITessAPI.TessPageIterator var1, int var2, int var3);

    public int TessPageIteratorBoundingBox(ITessAPI.TessPageIterator var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public int TessPageIteratorBlockType(ITessAPI.TessPageIterator var1);

    public Pix TessPageIteratorGetBinaryImage(ITessAPI.TessPageIterator var1, int var2);

    public Pix TessPageIteratorGetImage(ITessAPI.TessPageIterator var1, int var2, int var3, Pix var4, IntBuffer var5, IntBuffer var6);

    public int TessPageIteratorBaseline(ITessAPI.TessPageIterator var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public void TessPageIteratorOrientation(ITessAPI.TessPageIterator var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, FloatBuffer var5);

    public void TessPageIteratorParagraphInfo(ITessAPI.TessPageIterator var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public void TessResultIteratorDelete(ITessAPI.TessResultIterator var1);

    public ITessAPI.TessResultIterator TessResultIteratorCopy(ITessAPI.TessResultIterator var1);

    public ITessAPI.TessPageIterator TessResultIteratorGetPageIterator(ITessAPI.TessResultIterator var1);

    public ITessAPI.TessPageIterator TessResultIteratorGetPageIteratorConst(ITessAPI.TessResultIterator var1);

    public int TessResultIteratorNext(ITessAPI.TessResultIterator var1, int var2);

    public Pointer TessResultIteratorGetUTF8Text(ITessAPI.TessResultIterator var1, int var2);

    public float TessResultIteratorConfidence(ITessAPI.TessResultIterator var1, int var2);

    public String TessResultIteratorWordRecognitionLanguage(ITessAPI.TessResultIterator var1);

    public String TessResultIteratorWordFontAttributes(ITessAPI.TessResultIterator var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7, IntBuffer var8, IntBuffer var9);

    public int TessResultIteratorWordIsFromDictionary(ITessAPI.TessResultIterator var1);

    public int TessResultIteratorWordIsNumeric(ITessAPI.TessResultIterator var1);

    public int TessResultIteratorSymbolIsSuperscript(ITessAPI.TessResultIterator var1);

    public int TessResultIteratorSymbolIsSubscript(ITessAPI.TessResultIterator var1);

    public int TessResultIteratorSymbolIsDropcap(ITessAPI.TessResultIterator var1);

    public ITessAPI.TessChoiceIterator TessResultIteratorGetChoiceIterator(ITessAPI.TessResultIterator var1);

    public void TessChoiceIteratorDelete(ITessAPI.TessChoiceIterator var1);

    public int TessChoiceIteratorNext(ITessAPI.TessChoiceIterator var1);

    public String TessChoiceIteratorGetUTF8Text(ITessAPI.TessChoiceIterator var1);

    public float TessChoiceIteratorConfidence(ITessAPI.TessChoiceIterator var1);

    public ITessAPI.ETEXT_DESC TessMonitorCreate();

    public void TessMonitorDelete(ITessAPI.ETEXT_DESC var1);

    public void TessMonitorSetCancelFunc(ITessAPI.ETEXT_DESC var1, ITessAPI.TessCancelFunc var2);

    public void TessMonitorSetCancelThis(ITessAPI.ETEXT_DESC var1, Pointer var2);

    public Pointer TessMonitorGetCancelThis(ITessAPI.ETEXT_DESC var1);

    public void TessMonitorSetProgressFunc(ITessAPI.ETEXT_DESC var1, ITessAPI.TessProgressFunc var2);

    public int TessMonitorGetProgress(ITessAPI.ETEXT_DESC var1);

    public void TessMonitorSetDeadlineMSecs(ITessAPI.ETEXT_DESC var1, int var2);
}

