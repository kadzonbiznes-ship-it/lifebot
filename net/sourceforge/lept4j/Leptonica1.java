/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import net.sourceforge.lept4j.Box;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Boxaa;
import net.sourceforge.lept4j.CCBord;
import net.sourceforge.lept4j.CCBorda;
import net.sourceforge.lept4j.DPix;
import net.sourceforge.lept4j.DoubleLinkedList;
import net.sourceforge.lept4j.FPix;
import net.sourceforge.lept4j.FPixa;
import net.sourceforge.lept4j.GPlot;
import net.sourceforge.lept4j.ILeptonica;
import net.sourceforge.lept4j.JbClasser;
import net.sourceforge.lept4j.JbData;
import net.sourceforge.lept4j.L_Bmf;
import net.sourceforge.lept4j.L_ByteBuffer;
import net.sourceforge.lept4j.L_Bytea;
import net.sourceforge.lept4j.L_Colorfill;
import net.sourceforge.lept4j.L_Compressed_Data;
import net.sourceforge.lept4j.L_Dewarp;
import net.sourceforge.lept4j.L_Dewarpa;
import net.sourceforge.lept4j.L_Dna;
import net.sourceforge.lept4j.L_DnaHash;
import net.sourceforge.lept4j.L_Dnaa;
import net.sourceforge.lept4j.L_Hashitem;
import net.sourceforge.lept4j.L_Hashmap;
import net.sourceforge.lept4j.L_Heap;
import net.sourceforge.lept4j.L_Kernel;
import net.sourceforge.lept4j.L_Ptra;
import net.sourceforge.lept4j.L_Ptraa;
import net.sourceforge.lept4j.L_Queue;
import net.sourceforge.lept4j.L_Rbtree;
import net.sourceforge.lept4j.L_Rbtree_Node;
import net.sourceforge.lept4j.L_Rch;
import net.sourceforge.lept4j.L_Rcha;
import net.sourceforge.lept4j.L_Rdid;
import net.sourceforge.lept4j.L_Recog;
import net.sourceforge.lept4j.L_RegParams;
import net.sourceforge.lept4j.L_Stack;
import net.sourceforge.lept4j.L_StrCode;
import net.sourceforge.lept4j.L_Sudoku;
import net.sourceforge.lept4j.L_WShed;
import net.sourceforge.lept4j.L_WallTimer;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Numaa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.PixColormap;
import net.sourceforge.lept4j.PixComp;
import net.sourceforge.lept4j.PixTiling;
import net.sourceforge.lept4j.Pixa;
import net.sourceforge.lept4j.PixaComp;
import net.sourceforge.lept4j.Pixaa;
import net.sourceforge.lept4j.Pixacc;
import net.sourceforge.lept4j.Pta;
import net.sourceforge.lept4j.Ptaa;
import net.sourceforge.lept4j.Rb_Type;
import net.sourceforge.lept4j.Sarray;
import net.sourceforge.lept4j.Sel;
import net.sourceforge.lept4j.Sela;
import net.sourceforge.lept4j.util.LoadLibs;

public class Leptonica1
implements Library,
ILeptonica {
    public static native Pix pixCleanBackgroundToWhite(Pix var0, Pix var1, Pix var2, float var3, int var4, int var5);

    public static native Pix pixBackgroundNormSimple(Pix var0, Pix var1, Pix var2);

    public static native Pix pixBackgroundNorm(Pix var0, Pix var1, Pix var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9);

    public static native Pix pixBackgroundNormMorph(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native int pixBackgroundNormGrayArray(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, PointerByReference var9);

    public static native int pixBackgroundNormRGBArrays(Pix var0, Pix var1, Pix var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, PointerByReference var10, PointerByReference var11, PointerByReference var12);

    public static native int pixBackgroundNormGrayArrayMorph(Pix var0, Pix var1, int var2, int var3, int var4, PointerByReference var5);

    public static native int pixBackgroundNormRGBArraysMorph(Pix var0, Pix var1, int var2, int var3, int var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native int pixGetBackgroundGrayMap(Pix var0, Pix var1, int var2, int var3, int var4, int var5, PointerByReference var6);

    public static native int pixGetBackgroundRGBMap(Pix var0, Pix var1, Pix var2, int var3, int var4, int var5, int var6, PointerByReference var7, PointerByReference var8, PointerByReference var9);

    public static native int pixGetBackgroundGrayMapMorph(Pix var0, Pix var1, int var2, int var3, PointerByReference var4);

    public static native int pixGetBackgroundRGBMapMorph(Pix var0, Pix var1, int var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6);

    public static native int pixFillMapHoles(Pix var0, int var1, int var2, int var3);

    public static native Pix pixExtendByReplication(Pix var0, int var1, int var2);

    public static native int pixSmoothConnectedRegions(Pix var0, Pix var1, int var2);

    public static native Pix pixGetInvBackgroundMap(Pix var0, int var1, int var2, int var3);

    public static native Pix pixApplyInvBackgroundGrayMap(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixApplyInvBackgroundRGBMap(Pix var0, Pix var1, Pix var2, Pix var3, int var4, int var5);

    public static native Pix pixApplyVariableGrayMap(Pix var0, Pix var1, int var2);

    public static native Pix pixGlobalNormRGB(Pix var0, Pix var1, int var2, int var3, int var4, int var5);

    public static native Pix pixGlobalNormNoSatRGB(Pix var0, Pix var1, int var2, int var3, int var4, int var5, float var6);

    public static native int pixThresholdSpreadNorm(Pix var0, int var1, int var2, int var3, int var4, float var5, int var6, int var7, int var8, PointerByReference var9, PointerByReference var10, PointerByReference var11);

    public static native Pix pixBackgroundNormFlex(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixContrastNorm(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixBackgroundNormTo1MinMax(Pix var0, int var1, int var2);

    public static native Pix pixConvertTo8MinMax(Pix var0);

    public static native Pix pixAffineSampledPta(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixAffineSampled(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixAffinePta(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixAffine(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixAffinePtaColor(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixAffineColor(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixAffinePtaGray(Pix var0, Pta var1, Pta var2, byte var3);

    public static native Pix pixAffineGray(Pix var0, FloatBuffer var1, byte var2);

    public static native Pix pixAffinePtaWithAlpha(Pix var0, Pta var1, Pta var2, Pix var3, float var4, int var5);

    public static native int getAffineXformCoeffs(Pta var0, Pta var1, PointerByReference var2);

    public static native int affineInvertXform(FloatBuffer var0, PointerByReference var1);

    public static native int affineXformSampledPt(FloatBuffer var0, int var1, int var2, IntBuffer var3, IntBuffer var4);

    public static native int affineXformPt(FloatBuffer var0, int var1, int var2, FloatBuffer var3, FloatBuffer var4);

    public static native int linearInterpolatePixelColor(IntBuffer var0, int var1, int var2, int var3, float var4, float var5, int var6, IntBuffer var7);

    public static native int linearInterpolatePixelGray(IntBuffer var0, int var1, int var2, int var3, float var4, float var5, int var6, IntBuffer var7);

    public static native int gaussjordan(PointerByReference var0, FloatBuffer var1, int var2);

    public static native Pix pixAffineSequential(Pix var0, Pta var1, Pta var2, int var3, int var4);

    public static native FloatByReference createMatrix2dTranslate(float var0, float var1);

    public static native FloatByReference createMatrix2dScale(float var0, float var1);

    public static native FloatByReference createMatrix2dRotate(float var0, float var1, float var2);

    public static native Pta ptaTranslate(Pta var0, float var1, float var2);

    public static native Pta ptaScale(Pta var0, float var1, float var2);

    public static native Pta ptaRotate(Pta var0, float var1, float var2, float var3);

    public static native Boxa boxaTranslate(Boxa var0, float var1, float var2);

    public static native Boxa boxaScale(Boxa var0, float var1, float var2);

    public static native Boxa boxaRotate(Boxa var0, float var1, float var2, float var3);

    public static native Pta ptaAffineTransform(Pta var0, FloatBuffer var1);

    public static native Boxa boxaAffineTransform(Boxa var0, FloatBuffer var1);

    public static native int l_productMatVec(FloatBuffer var0, FloatBuffer var1, FloatBuffer var2, int var3);

    public static native int l_productMat2(FloatBuffer var0, FloatBuffer var1, FloatBuffer var2, int var3);

    public static native int l_productMat3(FloatBuffer var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, int var4);

    public static native int l_productMat4(FloatBuffer var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, FloatBuffer var4, int var5);

    public static native int l_getDataBit(Pointer var0, int var1);

    public static native void l_setDataBit(Pointer var0, int var1);

    public static native void l_clearDataBit(Pointer var0, int var1);

    public static native void l_setDataBitVal(Pointer var0, int var1, int var2);

    public static native int l_getDataDibit(Pointer var0, int var1);

    public static native void l_setDataDibit(Pointer var0, int var1, int var2);

    public static native void l_clearDataDibit(Pointer var0, int var1);

    public static native int l_getDataQbit(Pointer var0, int var1);

    public static native void l_setDataQbit(Pointer var0, int var1, int var2);

    public static native void l_clearDataQbit(Pointer var0, int var1);

    public static native int l_getDataByte(Pointer var0, int var1);

    public static native void l_setDataByte(Pointer var0, int var1, int var2);

    public static native int l_getDataTwoBytes(Pointer var0, int var1);

    public static native void l_setDataTwoBytes(Pointer var0, int var1, int var2);

    public static native int l_getDataFourBytes(Pointer var0, int var1);

    public static native void l_setDataFourBytes(Pointer var0, int var1, int var2);

    public static native Pointer barcodeDispatchDecoder(ByteBuffer var0, int var1, int var2);

    public static native int barcodeFormatIsSupported(int var0);

    public static native Numa pixFindBaselines(Pix var0, PointerByReference var1, Pixa var2);

    public static native Numa pixFindBaselinesGen(Pix var0, int var1, PointerByReference var2, Pixa var3);

    public static native Pix pixDeskewLocal(Pix var0, int var1, int var2, int var3, float var4, float var5, float var6);

    public static native int pixGetLocalSkewTransform(Pix var0, int var1, int var2, int var3, float var4, float var5, float var6, PointerByReference var7, PointerByReference var8);

    public static native Numa pixGetLocalSkewAngles(Pix var0, int var1, int var2, int var3, float var4, float var5, float var6, FloatBuffer var7, FloatBuffer var8, int var9);

    public static native L_ByteBuffer bbufferCreate(ByteBuffer var0, int var1);

    public static native void bbufferDestroy(PointerByReference var0);

    public static native Pointer bbufferDestroyAndSaveData(PointerByReference var0, NativeSizeByReference var1);

    public static native int bbufferRead(L_ByteBuffer var0, ByteBuffer var1, int var2);

    public static native int bbufferReadStream(L_ByteBuffer var0, ILeptonica.FILE var1, int var2);

    public static native int bbufferExtendArray(L_ByteBuffer var0, int var1);

    public static native int bbufferWrite(L_ByteBuffer var0, ByteBuffer var1, NativeSize var2, NativeSizeByReference var3);

    public static native int bbufferWriteStream(L_ByteBuffer var0, ILeptonica.FILE var1, NativeSize var2, NativeSizeByReference var3);

    public static native Pix pixBilateral(Pix var0, float var1, float var2, int var3, int var4);

    public static native Pix pixBilateralGray(Pix var0, float var1, float var2, int var3, int var4);

    public static native Pix pixBilateralExact(Pix var0, L_Kernel var1, L_Kernel var2);

    public static native Pix pixBilateralGrayExact(Pix var0, L_Kernel var1, L_Kernel var2);

    public static native Pix pixBlockBilateralExact(Pix var0, float var1, float var2);

    public static native L_Kernel makeRangeKernel(float var0);

    public static native Pix pixBilinearSampledPta(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixBilinearSampled(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixBilinearPta(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixBilinear(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixBilinearPtaColor(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixBilinearColor(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixBilinearPtaGray(Pix var0, Pta var1, Pta var2, byte var3);

    public static native Pix pixBilinearGray(Pix var0, FloatBuffer var1, byte var2);

    public static native Pix pixBilinearPtaWithAlpha(Pix var0, Pta var1, Pta var2, Pix var3, float var4, int var5);

    public static native int getBilinearXformCoeffs(Pta var0, Pta var1, PointerByReference var2);

    public static native int bilinearXformSampledPt(FloatBuffer var0, int var1, int var2, IntBuffer var3, IntBuffer var4);

    public static native int bilinearXformPt(FloatBuffer var0, int var1, int var2, FloatBuffer var3, FloatBuffer var4);

    public static native int pixOtsuAdaptiveThreshold(Pix var0, int var1, int var2, int var3, int var4, float var5, PointerByReference var6, PointerByReference var7);

    public static native Pix pixOtsuThreshOnBackgroundNorm(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, float var9, IntBuffer var10);

    public static native Pix pixMaskedThreshOnBackgroundNorm(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, int var7, float var8, IntBuffer var9);

    public static native int pixSauvolaBinarizeTiled(Pix var0, int var1, float var2, int var3, int var4, PointerByReference var5, PointerByReference var6);

    public static native int pixSauvolaBinarize(Pix var0, int var1, float var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native Pix pixSauvolaOnContrastNorm(Pix var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixThreshOnDoubleNorm(Pix var0, int var1);

    public static native int pixThresholdByConnComp(Pix var0, Pix var1, int var2, int var3, int var4, float var5, float var6, IntBuffer var7, PointerByReference var8, int var9);

    public static native int pixThresholdByConnComp(Pix var0, Pix var1, int var2, int var3, int var4, float var5, float var6, IntByReference var7, PointerByReference var8, int var9);

    public static native int pixThresholdByHisto(Pix var0, int var1, int var2, int var3, IntBuffer var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native int pixThresholdByHisto(Pix var0, int var1, int var2, int var3, IntByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native Pix pixExpandBinaryReplicate(Pix var0, int var1, int var2);

    public static native Pix pixExpandBinaryPower2(Pix var0, int var1);

    public static native Pix pixReduceBinary2(Pix var0, ByteBuffer var1);

    public static native Pix pixReduceRankBinaryCascade(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixReduceRankBinary2(Pix var0, int var1, ByteBuffer var2);

    public static native Pointer makeSubsampleTab2x();

    public static native Pix pixBlend(Pix var0, Pix var1, int var2, int var3, float var4);

    public static native Pix pixBlendMask(Pix var0, Pix var1, Pix var2, int var3, int var4, float var5, int var6);

    public static native Pix pixBlendGray(Pix var0, Pix var1, Pix var2, int var3, int var4, float var5, int var6, int var7, int var8);

    public static native Pix pixBlendGrayInverse(Pix var0, Pix var1, Pix var2, int var3, int var4, float var5);

    public static native Pix pixBlendColor(Pix var0, Pix var1, Pix var2, int var3, int var4, float var5, int var6, int var7);

    public static native Pix pixBlendColorByChannel(Pix var0, Pix var1, Pix var2, int var3, int var4, float var5, float var6, float var7, int var8, int var9);

    public static native Pix pixBlendGrayAdapt(Pix var0, Pix var1, Pix var2, int var3, int var4, float var5, int var6);

    public static native Pix pixFadeWithGray(Pix var0, Pix var1, float var2, int var3);

    public static native Pix pixBlendHardLight(Pix var0, Pix var1, Pix var2, int var3, int var4, float var5);

    public static native int pixBlendCmap(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native Pix pixBlendWithGrayMask(Pix var0, Pix var1, Pix var2, int var3, int var4);

    public static native Pix pixBlendBackgroundToColor(Pix var0, Pix var1, Box var2, int var3, float var4, int var5, int var6);

    public static native Pix pixMultiplyByColor(Pix var0, Pix var1, Box var2, int var3);

    public static native Pix pixAlphaBlendUniform(Pix var0, int var1);

    public static native Pix pixAddAlphaToBlend(Pix var0, float var1, int var2);

    public static native Pix pixSetAlphaOverWhite(Pix var0);

    public static native int pixLinearEdgeFade(Pix var0, int var1, int var2, float var3, float var4);

    public static native L_Bmf bmfCreate(String var0, int var1);

    public static native void bmfDestroy(PointerByReference var0);

    public static native Pix bmfGetPix(L_Bmf var0, byte var1);

    public static native int bmfGetWidth(L_Bmf var0, byte var1, IntBuffer var2);

    public static native int bmfGetBaseline(L_Bmf var0, byte var1, IntBuffer var2);

    public static native Pixa pixaGetFont(String var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixaSaveFont(String var0, String var1, int var2);

    public static native Pix pixReadStreamBmp(ILeptonica.FILE var0);

    public static native Pix pixReadMemBmp(ByteBuffer var0, NativeSize var1);

    public static native int pixWriteStreamBmp(ILeptonica.FILE var0, Pix var1);

    public static native int pixWriteMemBmp(PointerByReference var0, NativeSizeByReference var1, Pix var2);

    public static native Pixa l_bootnum_gen1();

    public static native Pixa l_bootnum_gen2();

    public static native Pixa l_bootnum_gen3();

    public static native Pixa l_bootnum_gen4(int var0);

    public static native Box boxCreate(int var0, int var1, int var2, int var3);

    public static native Box boxCreateValid(int var0, int var1, int var2, int var3);

    public static native Box boxCopy(Box var0);

    public static native Box boxClone(Box var0);

    public static native void boxDestroy(PointerByReference var0);

    public static native int boxGetGeometry(Box var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int boxSetGeometry(Box var0, int var1, int var2, int var3, int var4);

    public static native int boxGetSideLocations(Box var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int boxSetSideLocations(Box var0, int var1, int var2, int var3, int var4);

    public static native int boxIsValid(Box var0, IntBuffer var1);

    public static native Boxa boxaCreate(int var0);

    public static native Boxa boxaCopy(Boxa var0, int var1);

    public static native void boxaDestroy(PointerByReference var0);

    public static native int boxaAddBox(Boxa var0, Box var1, int var2);

    public static native int boxaExtendArray(Boxa var0);

    public static native int boxaExtendArrayToSize(Boxa var0, NativeSize var1);

    public static native int boxaGetCount(Boxa var0);

    public static native int boxaGetValidCount(Boxa var0);

    public static native Box boxaGetBox(Boxa var0, int var1, int var2);

    public static native Box boxaGetValidBox(Boxa var0, int var1, int var2);

    public static native Numa boxaFindInvalidBoxes(Boxa var0);

    public static native int boxaGetBoxGeometry(Boxa var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int boxaIsFull(Boxa var0, IntBuffer var1);

    public static native int boxaReplaceBox(Boxa var0, int var1, Box var2);

    public static native int boxaInsertBox(Boxa var0, int var1, Box var2);

    public static native int boxaRemoveBox(Boxa var0, int var1);

    public static native int boxaRemoveBoxAndSave(Boxa var0, int var1, PointerByReference var2);

    public static native Boxa boxaSaveValid(Boxa var0, int var1);

    public static native int boxaInitFull(Boxa var0, Box var1);

    public static native int boxaClear(Boxa var0);

    public static native Boxaa boxaaCreate(int var0);

    public static native Boxaa boxaaCopy(Boxaa var0, int var1);

    public static native void boxaaDestroy(PointerByReference var0);

    public static native int boxaaAddBoxa(Boxaa var0, Boxa var1, int var2);

    public static native int boxaaExtendArray(Boxaa var0);

    public static native int boxaaExtendArrayToSize(Boxaa var0, int var1);

    public static native int boxaaGetCount(Boxaa var0);

    public static native int boxaaGetBoxCount(Boxaa var0);

    public static native Boxa boxaaGetBoxa(Boxaa var0, int var1, int var2);

    public static native Box boxaaGetBox(Boxaa var0, int var1, int var2, int var3);

    public static native int boxaaInitFull(Boxaa var0, Boxa var1);

    public static native int boxaaExtendWithInit(Boxaa var0, int var1, Boxa var2);

    public static native int boxaaReplaceBoxa(Boxaa var0, int var1, Boxa var2);

    public static native int boxaaInsertBoxa(Boxaa var0, int var1, Boxa var2);

    public static native int boxaaRemoveBoxa(Boxaa var0, int var1);

    public static native int boxaaAddBox(Boxaa var0, int var1, Box var2, int var3);

    public static native Boxaa boxaaReadFromFiles(String var0, String var1, int var2, int var3);

    public static native Boxaa boxaaRead(String var0);

    public static native Boxaa boxaaReadStream(ILeptonica.FILE var0);

    public static native Boxaa boxaaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int boxaaWrite(String var0, Boxaa var1);

    public static native int boxaaWriteStream(ILeptonica.FILE var0, Boxaa var1);

    public static native int boxaaWriteMem(PointerByReference var0, NativeSizeByReference var1, Boxaa var2);

    public static native Boxa boxaRead(String var0);

    public static native Boxa boxaReadStream(ILeptonica.FILE var0);

    public static native Boxa boxaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int boxaWriteDebug(String var0, Boxa var1);

    public static native int boxaWrite(String var0, Boxa var1);

    public static native int boxaWriteStream(ILeptonica.FILE var0, Boxa var1);

    public static native int boxaWriteStderr(Boxa var0);

    public static native int boxaWriteMem(PointerByReference var0, NativeSizeByReference var1, Boxa var2);

    public static native int boxPrintStreamInfo(ILeptonica.FILE var0, Box var1);

    public static native int boxContains(Box var0, Box var1, IntBuffer var2);

    public static native int boxIntersects(Box var0, Box var1, IntBuffer var2);

    public static native Boxa boxaContainedInBox(Boxa var0, Box var1);

    public static native int boxaContainedInBoxCount(Boxa var0, Box var1, IntBuffer var2);

    public static native int boxaContainedInBoxa(Boxa var0, Boxa var1, IntBuffer var2);

    public static native Boxa boxaIntersectsBox(Boxa var0, Box var1);

    public static native int boxaIntersectsBoxCount(Boxa var0, Box var1, IntBuffer var2);

    public static native Boxa boxaClipToBox(Boxa var0, Box var1);

    public static native Boxa boxaCombineOverlaps(Boxa var0, Pixa var1);

    public static native int boxaCombineOverlapsInPair(Boxa var0, Boxa var1, PointerByReference var2, PointerByReference var3, Pixa var4);

    public static native Box boxOverlapRegion(Box var0, Box var1);

    public static native Box boxBoundingRegion(Box var0, Box var1);

    public static native int boxOverlapFraction(Box var0, Box var1, FloatBuffer var2);

    public static native int boxOverlapArea(Box var0, Box var1, IntBuffer var2);

    public static native Boxa boxaHandleOverlaps(Boxa var0, int var1, int var2, float var3, float var4, PointerByReference var5);

    public static native int boxOverlapDistance(Box var0, Box var1, IntBuffer var2, IntBuffer var3);

    public static native int boxSeparationDistance(Box var0, Box var1, IntBuffer var2, IntBuffer var3);

    public static native int boxCompareSize(Box var0, Box var1, int var2, IntBuffer var3);

    public static native int boxContainsPt(Box var0, float var1, float var2, IntBuffer var3);

    public static native Box boxaGetNearestToPt(Boxa var0, int var1, int var2);

    public static native Box boxaGetNearestToLine(Boxa var0, int var1, int var2);

    public static native int boxaFindNearestBoxes(Boxa var0, int var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native int boxaGetNearestByDirection(Boxa var0, int var1, int var2, int var3, int var4, IntBuffer var5, IntBuffer var6);

    public static native int boxGetCenter(Box var0, FloatBuffer var1, FloatBuffer var2);

    public static native int boxIntersectByLine(Box var0, int var1, int var2, float var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7, IntBuffer var8);

    public static native Box boxClipToRectangle(Box var0, int var1, int var2);

    public static native int boxClipToRectangleParams(Box var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7, IntBuffer var8);

    public static native Box boxRelocateOneSide(Box var0, Box var1, int var2, int var3);

    public static native Boxa boxaAdjustSides(Boxa var0, int var1, int var2, int var3, int var4);

    public static native int boxaAdjustBoxSides(Boxa var0, int var1, int var2, int var3, int var4, int var5);

    public static native Box boxAdjustSides(Box var0, Box var1, int var2, int var3, int var4, int var5);

    public static native Boxa boxaSetSide(Boxa var0, Boxa var1, int var2, int var3, int var4);

    public static native int boxSetSide(Box var0, int var1, int var2, int var3);

    public static native Boxa boxaAdjustWidthToTarget(Boxa var0, Boxa var1, int var2, int var3, int var4);

    public static native Boxa boxaAdjustHeightToTarget(Boxa var0, Boxa var1, int var2, int var3, int var4);

    public static native int boxEqual(Box var0, Box var1, IntBuffer var2);

    public static native int boxaEqual(Boxa var0, Boxa var1, int var2, PointerByReference var3, IntBuffer var4);

    public static native int boxaEqual(Boxa var0, Boxa var1, int var2, PointerByReference var3, IntByReference var4);

    public static native int boxSimilar(Box var0, Box var1, int var2, int var3, int var4, int var5, IntBuffer var6);

    public static native int boxaSimilar(Boxa var0, Boxa var1, int var2, int var3, int var4, int var5, int var6, IntBuffer var7, PointerByReference var8);

    public static native int boxaSimilar(Boxa var0, Boxa var1, int var2, int var3, int var4, int var5, int var6, IntByReference var7, PointerByReference var8);

    public static native int boxaJoin(Boxa var0, Boxa var1, int var2, int var3);

    public static native int boxaaJoin(Boxaa var0, Boxaa var1, int var2, int var3);

    public static native int boxaSplitEvenOdd(Boxa var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Boxa boxaMergeEvenOdd(Boxa var0, Boxa var1, int var2);

    public static native Boxa boxaTransform(Boxa var0, int var1, int var2, float var3, float var4);

    public static native Box boxTransform(Box var0, int var1, int var2, float var3, float var4);

    public static native Boxa boxaTransformOrdered(Boxa var0, int var1, int var2, float var3, float var4, int var5, int var6, float var7, int var8);

    public static native Box boxTransformOrdered(Box var0, int var1, int var2, float var3, float var4, int var5, int var6, float var7, int var8);

    public static native Boxa boxaRotateOrth(Boxa var0, int var1, int var2, int var3);

    public static native Box boxRotateOrth(Box var0, int var1, int var2, int var3);

    public static native Boxa boxaShiftWithPta(Boxa var0, Pta var1, int var2);

    public static native Boxa boxaSort(Boxa var0, int var1, int var2, PointerByReference var3);

    public static native Boxa boxaBinSort(Boxa var0, int var1, int var2, PointerByReference var3);

    public static native Boxa boxaSortByIndex(Boxa var0, Numa var1);

    public static native Boxaa boxaSort2d(Boxa var0, PointerByReference var1, int var2, int var3, int var4);

    public static native Boxaa boxaSort2dByIndex(Boxa var0, Numaa var1);

    public static native int boxaExtractAsNuma(Boxa var0, PointerByReference var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, int var7);

    public static native int boxaExtractAsPta(Boxa var0, PointerByReference var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, int var7);

    public static native Pta boxaExtractCorners(Boxa var0, int var1);

    public static native int boxaGetRankVals(Boxa var0, float var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7);

    public static native int boxaGetMedianVals(Boxa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int boxaGetAverageSize(Boxa var0, FloatBuffer var1, FloatBuffer var2);

    public static native int boxaaGetExtent(Boxaa var0, IntBuffer var1, IntBuffer var2, PointerByReference var3, PointerByReference var4);

    public static native int boxaaGetExtent(Boxaa var0, IntByReference var1, IntByReference var2, PointerByReference var3, PointerByReference var4);

    public static native Boxa boxaaFlattenToBoxa(Boxaa var0, PointerByReference var1, int var2);

    public static native Boxa boxaaFlattenAligned(Boxaa var0, int var1, Box var2, int var3);

    public static native Boxaa boxaEncapsulateAligned(Boxa var0, int var1, int var2);

    public static native Boxaa boxaaTranspose(Boxaa var0);

    public static native int boxaaAlignBox(Boxaa var0, Box var1, int var2, IntBuffer var3);

    public static native Pix pixMaskConnComp(Pix var0, int var1, PointerByReference var2);

    public static native Pix pixMaskBoxa(Pix var0, Pix var1, Boxa var2, int var3);

    public static native Pix pixPaintBoxa(Pix var0, Boxa var1, int var2);

    public static native Pix pixSetBlackOrWhiteBoxa(Pix var0, Boxa var1, int var2);

    public static native Pix pixPaintBoxaRandom(Pix var0, Boxa var1);

    public static native Pix pixBlendBoxaRandom(Pix var0, Boxa var1, float var2);

    public static native Pix pixDrawBoxa(Pix var0, Boxa var1, int var2, int var3);

    public static native Pix pixDrawBoxaRandom(Pix var0, Boxa var1, int var2);

    public static native Pix boxaaDisplay(Pix var0, Boxaa var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native Pixa pixaDisplayBoxaa(Pixa var0, Boxaa var1, int var2, int var3);

    public static native Boxa pixSplitIntoBoxa(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Boxa pixSplitComponentIntoBoxa(Pix var0, Box var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native Boxa makeMosaicStrips(int var0, int var1, int var2, int var3);

    public static native int boxaCompareRegions(Boxa var0, Boxa var1, int var2, IntBuffer var3, FloatBuffer var4, FloatBuffer var5, PointerByReference var6);

    public static native int boxaCompareRegions(Boxa var0, Boxa var1, int var2, IntByReference var3, FloatByReference var4, FloatByReference var5, PointerByReference var6);

    public static native Box pixSelectLargeULComp(Pix var0, float var1, int var2, int var3);

    public static native Box boxaSelectLargeULBox(Boxa var0, float var1, int var2);

    public static native Boxa boxaSelectRange(Boxa var0, int var1, int var2, int var3);

    public static native Boxaa boxaaSelectRange(Boxaa var0, int var1, int var2, int var3);

    public static native Boxa boxaSelectBySize(Boxa var0, int var1, int var2, int var3, int var4, IntBuffer var5);

    public static native Numa boxaMakeSizeIndicator(Boxa var0, int var1, int var2, int var3, int var4);

    public static native Boxa boxaSelectByArea(Boxa var0, int var1, int var2, IntBuffer var3);

    public static native Numa boxaMakeAreaIndicator(Boxa var0, int var1, int var2);

    public static native Boxa boxaSelectByWHRatio(Boxa var0, float var1, int var2, IntBuffer var3);

    public static native Numa boxaMakeWHRatioIndicator(Boxa var0, float var1, int var2);

    public static native Boxa boxaSelectWithIndicator(Boxa var0, Numa var1, IntBuffer var2);

    public static native Boxa boxaPermutePseudorandom(Boxa var0);

    public static native Boxa boxaPermuteRandom(Boxa var0, Boxa var1);

    public static native int boxaSwapBoxes(Boxa var0, int var1, int var2);

    public static native Pta boxaConvertToPta(Boxa var0, int var1);

    public static native Boxa ptaConvertToBoxa(Pta var0, int var1);

    public static native Pta boxConvertToPta(Box var0, int var1);

    public static native Box ptaConvertToBox(Pta var0);

    public static native int boxaGetExtent(Boxa var0, IntBuffer var1, IntBuffer var2, PointerByReference var3);

    public static native int boxaGetExtent(Boxa var0, IntByReference var1, IntByReference var2, PointerByReference var3);

    public static native int boxaGetCoverage(Boxa var0, int var1, int var2, int var3, FloatBuffer var4);

    public static native int boxaaSizeRange(Boxaa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int boxaSizeRange(Boxa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int boxaLocationRange(Boxa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int boxaGetSizes(Boxa var0, PointerByReference var1, PointerByReference var2);

    public static native int boxaGetArea(Boxa var0, IntBuffer var1);

    public static native Pix boxaDisplayTiled(Boxa var0, Pixa var1, int var2, int var3, int var4, int var5, float var6, int var7, int var8, int var9);

    public static native Boxa boxaSmoothSequenceMedian(Boxa var0, int var1, int var2, int var3, int var4, int var5);

    public static native Boxa boxaWindowedMedian(Boxa var0, int var1, int var2);

    public static native Boxa boxaModifyWithBoxa(Boxa var0, Boxa var1, int var2, int var3, int var4);

    public static native Boxa boxaReconcilePairWidth(Boxa var0, int var1, int var2, float var3, Numa var4);

    public static native int boxaSizeConsistency(Boxa var0, int var1, float var2, float var3, FloatBuffer var4, FloatBuffer var5, IntBuffer var6);

    public static native Boxa boxaReconcileAllByMedian(Boxa var0, int var1, int var2, int var3, int var4, Pixa var5);

    public static native Boxa boxaReconcileSidesByMedian(Boxa var0, int var1, int var2, int var3, Pixa var4);

    public static native Boxa boxaReconcileSizeByMedian(Boxa var0, int var1, float var2, float var3, float var4, PointerByReference var5, PointerByReference var6, FloatBuffer var7);

    public static native Boxa boxaReconcileSizeByMedian(Boxa var0, int var1, float var2, float var3, float var4, PointerByReference var5, PointerByReference var6, FloatByReference var7);

    public static native int boxaPlotSides(Boxa var0, String var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6);

    public static native int boxaPlotSides(Boxa var0, Pointer var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6);

    public static native int boxaPlotSizes(Boxa var0, String var1, PointerByReference var2, PointerByReference var3, PointerByReference var4);

    public static native int boxaPlotSizes(Boxa var0, Pointer var1, PointerByReference var2, PointerByReference var3, PointerByReference var4);

    public static native Boxa boxaFillSequence(Boxa var0, int var1, int var2);

    public static native int boxaSizeVariation(Boxa var0, int var1, FloatBuffer var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5);

    public static native int boxaMedianDimensions(Boxa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, PointerByReference var7, PointerByReference var8);

    public static native int boxaMedianDimensions(Boxa var0, IntByReference var1, IntByReference var2, IntByReference var3, IntByReference var4, IntByReference var5, IntByReference var6, PointerByReference var7, PointerByReference var8);

    public static native L_Bytea l_byteaCreate(NativeSize var0);

    public static native L_Bytea l_byteaInitFromMem(ByteBuffer var0, NativeSize var1);

    public static native L_Bytea l_byteaInitFromFile(String var0);

    public static native L_Bytea l_byteaInitFromStream(ILeptonica.FILE var0);

    public static native L_Bytea l_byteaCopy(L_Bytea var0, int var1);

    public static native void l_byteaDestroy(PointerByReference var0);

    public static native NativeSize l_byteaGetSize(L_Bytea var0);

    public static native Pointer l_byteaGetData(L_Bytea var0, NativeSizeByReference var1);

    public static native Pointer l_byteaCopyData(L_Bytea var0, NativeSizeByReference var1);

    public static native int l_byteaAppendData(L_Bytea var0, ByteBuffer var1, NativeSize var2);

    public static native int l_byteaAppendString(L_Bytea var0, String var1);

    public static native int l_byteaJoin(L_Bytea var0, PointerByReference var1);

    public static native int l_byteaSplit(L_Bytea var0, NativeSize var1, PointerByReference var2);

    public static native int l_byteaFindEachSequence(L_Bytea var0, ByteBuffer var1, NativeSize var2, PointerByReference var3);

    public static native int l_byteaFindEachSequence(L_Bytea var0, Pointer var1, NativeSize var2, PointerByReference var3);

    public static native int l_byteaWrite(String var0, L_Bytea var1, NativeSize var2, NativeSize var3);

    public static native int l_byteaWriteStream(ILeptonica.FILE var0, L_Bytea var1, NativeSize var2, NativeSize var3);

    public static native void ccbaDestroy(PointerByReference var0);

    public static native CCBorda pixGetAllCCBorders(Pix var0);

    public static native Ptaa pixGetOuterBordersPtaa(Pix var0);

    public static native int pixGetOuterBorder(CCBord var0, Pix var1, Box var2);

    public static native int ccbaGenerateGlobalLocs(CCBorda var0);

    public static native int ccbaGenerateStepChains(CCBorda var0);

    public static native int ccbaStepChainsToPixCoords(CCBorda var0, int var1);

    public static native int ccbaGenerateSPGlobalLocs(CCBorda var0, int var1);

    public static native int ccbaGenerateSinglePath(CCBorda var0);

    public static native Pix ccbaDisplayBorder(CCBorda var0);

    public static native Pix ccbaDisplaySPBorder(CCBorda var0);

    public static native Pix ccbaDisplayImage1(CCBorda var0);

    public static native Pix ccbaDisplayImage2(CCBorda var0);

    public static native int ccbaWrite(String var0, CCBorda var1);

    public static native int ccbaWriteStream(ILeptonica.FILE var0, CCBorda var1);

    public static native CCBorda ccbaRead(String var0);

    public static native CCBorda ccbaReadStream(ILeptonica.FILE var0);

    public static native int ccbaWriteSVG(String var0, CCBorda var1);

    public static native Pointer ccbaWriteSVGString(CCBorda var0);

    public static native Pixa pixaThinConnected(Pixa var0, int var1, int var2, int var3);

    public static native Pix pixThinConnected(Pix var0, int var1, int var2, int var3);

    public static native Pix pixThinConnectedBySet(Pix var0, int var1, Sela var2, int var3);

    public static native Sela selaMakeThinSets(int var0, int var1);

    public static native int pixFindCheckerboardCorners(Pix var0, int var1, int var2, int var3, PointerByReference var4, PointerByReference var5, Pixa var6);

    public static native int jbCorrelation(String var0, float var1, float var2, int var3, String var4, int var5, int var6, int var7);

    public static native int jbRankHaus(String var0, int var1, float var2, int var3, String var4, int var5, int var6, int var7);

    public static native JbClasser jbWordsInTextlines(String var0, int var1, int var2, int var3, float var4, float var5, PointerByReference var6, int var7, int var8);

    public static native JbClasser jbWordsInTextlines(Pointer var0, int var1, int var2, int var3, float var4, float var5, PointerByReference var6, int var7, int var8);

    public static native int pixGetWordsInTextlines(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native int pixGetWordBoxesInTextlines(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5, PointerByReference var6);

    public static native int pixFindWordAndCharacterBoxes(Pix var0, Box var1, int var2, PointerByReference var3, PointerByReference var4, String var5);

    public static native int pixFindWordAndCharacterBoxes(Pix var0, Box var1, int var2, PointerByReference var3, PointerByReference var4, Pointer var5);

    public static native Numaa boxaExtractSortedPattern(Boxa var0, Numa var1);

    public static native int numaaCompareImagesByBoxes(Numaa var0, Numaa var1, int var2, int var3, int var4, int var5, int var6, int var7, IntBuffer var8, int var9);

    public static native int pixColorContent(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native Pix pixColorMagnitude(Pix var0, int var1, int var2, int var3, int var4);

    public static native int pixColorFraction(Pix var0, int var1, int var2, int var3, int var4, FloatBuffer var5, FloatBuffer var6);

    public static native Pix pixColorShiftWhitePoint(Pix var0, int var1, int var2, int var3);

    public static native Pix pixMaskOverColorPixels(Pix var0, int var1, int var2);

    public static native Pix pixMaskOverGrayPixels(Pix var0, int var1, int var2);

    public static native Pix pixMaskOverColorRange(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native int pixFindColorRegions(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, float var7, FloatBuffer var8, PointerByReference var9, PointerByReference var10, Pixa var11);

    public static native int pixFindColorRegions(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, float var7, FloatByReference var8, PointerByReference var9, PointerByReference var10, Pixa var11);

    public static native int pixNumSignificantGrayColors(Pix var0, int var1, int var2, float var3, int var4, IntBuffer var5);

    public static native int pixColorsForQuantization(Pix var0, int var1, IntBuffer var2, IntBuffer var3, int var4);

    public static native int pixNumColors(Pix var0, int var1, IntBuffer var2);

    public static native Pix pixConvertRGBToCmapLossless(Pix var0);

    public static native int pixGetMostPopulatedColors(Pix var0, int var1, int var2, int var3, PointerByReference var4, PointerByReference var5);

    public static native Pix pixSimpleColorQuantize(Pix var0, int var1, int var2, int var3);

    public static native Numa pixGetRGBHistogram(Pix var0, int var1, int var2);

    public static native int makeRGBIndexTables(PointerByReference var0, PointerByReference var1, PointerByReference var2, int var3);

    public static native int getRGBFromIndex(int var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixHasHighlightRed(Pix var0, int var1, float var2, float var3, IntBuffer var4, FloatBuffer var5, PointerByReference var6);

    public static native int pixHasHighlightRed(Pix var0, int var1, float var2, float var3, IntByReference var4, FloatByReference var5, PointerByReference var6);

    public static native L_Colorfill l_colorfillCreate(Pix var0, int var1, int var2);

    public static native void l_colorfillDestroy(PointerByReference var0);

    public static native int pixColorContentByLocation(L_Colorfill var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public static native Pix pixColorFill(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pixa makeColorfillTestData(int var0, int var1, int var2, int var3);

    public static native Pix pixColorGrayRegions(Pix var0, Boxa var1, int var2, int var3, int var4, int var5, int var6);

    public static native int pixColorGray(Pix var0, Box var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixColorGrayMasked(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixSnapColor(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native Pix pixSnapColorCmap(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native Pix pixLinearMapToTargetColor(Pix var0, Pix var1, int var2, int var3);

    public static native int pixelLinearMapToTargetColor(int var0, int var1, int var2, IntBuffer var3);

    public static native Pix pixShiftByComponent(Pix var0, Pix var1, int var2, int var3);

    public static native int pixelShiftByComponent(int var0, int var1, int var2, int var3, int var4, IntBuffer var5);

    public static native int pixelFractionalShift(int var0, int var1, int var2, float var3, IntBuffer var4);

    public static native Pix pixMapWithInvariantHue(Pix var0, Pix var1, int var2, float var3);

    public static native PixColormap pixcmapCreate(int var0);

    public static native PixColormap pixcmapCreateRandom(int var0, int var1, int var2);

    public static native PixColormap pixcmapCreateLinear(int var0, int var1);

    public static native PixColormap pixcmapCopy(PixColormap var0);

    public static native void pixcmapDestroy(PointerByReference var0);

    public static native int pixcmapIsValid(PixColormap var0, Pix var1, IntBuffer var2);

    public static native int pixcmapAddColor(PixColormap var0, int var1, int var2, int var3);

    public static native int pixcmapAddRGBA(PixColormap var0, int var1, int var2, int var3, int var4);

    public static native int pixcmapAddNewColor(PixColormap var0, int var1, int var2, int var3, IntBuffer var4);

    public static native int pixcmapAddNearestColor(PixColormap var0, int var1, int var2, int var3, IntBuffer var4);

    public static native int pixcmapUsableColor(PixColormap var0, int var1, int var2, int var3, IntBuffer var4);

    public static native int pixcmapAddBlackOrWhite(PixColormap var0, int var1, IntBuffer var2);

    public static native int pixcmapSetBlackAndWhite(PixColormap var0, int var1, int var2);

    public static native int pixcmapGetCount(PixColormap var0);

    public static native int pixcmapGetFreeCount(PixColormap var0);

    public static native int pixcmapGetDepth(PixColormap var0);

    public static native int pixcmapGetMinDepth(PixColormap var0, IntBuffer var1);

    public static native int pixcmapClear(PixColormap var0);

    public static native int pixcmapGetColor(PixColormap var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixcmapGetColor32(PixColormap var0, int var1, IntBuffer var2);

    public static native int pixcmapGetRGBA(PixColormap var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int pixcmapGetRGBA32(PixColormap var0, int var1, IntBuffer var2);

    public static native int pixcmapResetColor(PixColormap var0, int var1, int var2, int var3, int var4);

    public static native int pixcmapSetAlpha(PixColormap var0, int var1, int var2);

    public static native int pixcmapGetIndex(PixColormap var0, int var1, int var2, int var3, IntBuffer var4);

    public static native int pixcmapHasColor(PixColormap var0, IntBuffer var1);

    public static native int pixcmapIsOpaque(PixColormap var0, IntBuffer var1);

    public static native int pixcmapNonOpaqueColorsInfo(PixColormap var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int pixcmapIsBlackAndWhite(PixColormap var0, IntBuffer var1);

    public static native int pixcmapCountGrayColors(PixColormap var0, IntBuffer var1);

    public static native int pixcmapGetRankIntensity(PixColormap var0, float var1, IntBuffer var2);

    public static native int pixcmapGetNearestIndex(PixColormap var0, int var1, int var2, int var3, IntBuffer var4);

    public static native int pixcmapGetNearestGrayIndex(PixColormap var0, int var1, IntBuffer var2);

    public static native int pixcmapGetDistanceToColor(PixColormap var0, int var1, int var2, int var3, int var4, IntBuffer var5);

    public static native int pixcmapGetRangeValues(PixColormap var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native PixColormap pixcmapGrayToFalseColor(float var0);

    public static native PixColormap pixcmapGrayToColor(int var0);

    public static native PixColormap pixcmapColorToGray(PixColormap var0, float var1, float var2, float var3);

    public static native PixColormap pixcmapConvertTo4(PixColormap var0);

    public static native PixColormap pixcmapConvertTo8(PixColormap var0);

    public static native PixColormap pixcmapRead(String var0);

    public static native PixColormap pixcmapReadStream(ILeptonica.FILE var0);

    public static native PixColormap pixcmapReadMem(ByteBuffer var0, NativeSize var1);

    public static native int pixcmapWrite(String var0, PixColormap var1);

    public static native int pixcmapWriteStream(ILeptonica.FILE var0, PixColormap var1);

    public static native int pixcmapWriteMem(PointerByReference var0, NativeSizeByReference var1, PixColormap var2);

    public static native int pixcmapToArrays(PixColormap var0, PointerByReference var1, PointerByReference var2, PointerByReference var3, PointerByReference var4);

    public static native int pixcmapToRGBTable(PixColormap var0, PointerByReference var1, IntBuffer var2);

    public static native int pixcmapSerializeToMemory(PixColormap var0, int var1, IntBuffer var2, PointerByReference var3);

    public static native PixColormap pixcmapDeserializeFromMemory(ByteBuffer var0, int var1, int var2);

    public static native Pointer pixcmapConvertToHex(ByteBuffer var0, int var1);

    public static native int pixcmapGammaTRC(PixColormap var0, float var1, int var2, int var3);

    public static native int pixcmapContrastTRC(PixColormap var0, float var1);

    public static native int pixcmapShiftIntensity(PixColormap var0, float var1);

    public static native int pixcmapShiftByComponent(PixColormap var0, int var1, int var2);

    public static native Pix pixColorMorph(Pix var0, int var1, int var2, int var3);

    public static native Pix pixOctreeColorQuant(Pix var0, int var1, int var2);

    public static native Pix pixOctreeColorQuantGeneral(Pix var0, int var1, int var2, float var3, float var4);

    public static native int makeRGBToIndexTables(int var0, PointerByReference var1, PointerByReference var2, PointerByReference var3);

    public static native void getOctcubeIndexFromRGB(int var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native Pix pixOctreeQuantByPopulation(Pix var0, int var1, int var2);

    public static native Pix pixOctreeQuantNumColors(Pix var0, int var1, int var2);

    public static native Pix pixOctcubeQuantMixedWithGray(Pix var0, int var1, int var2, int var3);

    public static native Pix pixFixedOctcubeQuant256(Pix var0, int var1);

    public static native Pix pixFewColorsOctcubeQuant1(Pix var0, int var1);

    public static native Pix pixFewColorsOctcubeQuant2(Pix var0, int var1, Numa var2, int var3, IntBuffer var4);

    public static native Pix pixFewColorsOctcubeQuantMixed(Pix var0, int var1, int var2, int var3, int var4, float var5, int var6);

    public static native Pix pixFixedOctcubeQuantGenRGB(Pix var0, int var1);

    public static native Pix pixQuantFromCmap(Pix var0, PixColormap var1, int var2, int var3, int var4);

    public static native Pix pixOctcubeQuantFromCmap(Pix var0, PixColormap var1, int var2, int var3, int var4);

    public static native Numa pixOctcubeHistogram(Pix var0, int var1, IntBuffer var2);

    public static native IntByReference pixcmapToOctcubeLUT(PixColormap var0, int var1, int var2);

    public static native int pixRemoveUnusedColors(Pix var0);

    public static native int pixNumberOccupiedOctcubes(Pix var0, int var1, int var2, float var3, IntBuffer var4);

    public static native Pix pixMedianCutQuant(Pix var0, int var1);

    public static native Pix pixMedianCutQuantGeneral(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixMedianCutQuantMixed(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixFewColorsMedianCutQuantMixed(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native IntByReference pixMedianCutHisto(Pix var0, int var1, int var2);

    public static native Pix pixColorSegment(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixColorSegmentCluster(Pix var0, int var1, int var2, int var3);

    public static native int pixAssignToNearestColor(Pix var0, Pix var1, Pix var2, int var3, IntBuffer var4);

    public static native int pixColorSegmentClean(Pix var0, int var1, IntBuffer var2);

    public static native int pixColorSegmentRemoveColors(Pix var0, Pix var1, int var2);

    public static native Pix pixConvertRGBToHSV(Pix var0, Pix var1);

    public static native Pix pixConvertHSVToRGB(Pix var0, Pix var1);

    public static native int convertRGBToHSV(int var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int convertHSVToRGB(int var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int pixcmapConvertRGBToHSV(PixColormap var0);

    public static native int pixcmapConvertHSVToRGB(PixColormap var0);

    public static native Pix pixConvertRGBToHue(Pix var0);

    public static native Pix pixConvertRGBToSaturation(Pix var0);

    public static native Pix pixConvertRGBToValue(Pix var0);

    public static native Pix pixMakeRangeMaskHS(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixMakeRangeMaskHV(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixMakeRangeMaskSV(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixMakeHistoHS(Pix var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixMakeHistoHV(Pix var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixMakeHistoSV(Pix var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native int pixFindHistoPeaksHSV(Pix var0, int var1, int var2, int var3, int var4, float var5, PointerByReference var6, PointerByReference var7, PointerByReference var8);

    public static native Pix displayHSVColorRange(int var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixConvertRGBToYUV(Pix var0, Pix var1);

    public static native Pix pixConvertYUVToRGB(Pix var0, Pix var1);

    public static native int convertRGBToYUV(int var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int convertYUVToRGB(int var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int pixcmapConvertRGBToYUV(PixColormap var0);

    public static native int pixcmapConvertYUVToRGB(PixColormap var0);

    public static native FPixa pixConvertRGBToXYZ(Pix var0);

    public static native Pix fpixaConvertXYZToRGB(FPixa var0);

    public static native int convertRGBToXYZ(int var0, int var1, int var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5);

    public static native int convertXYZToRGB(float var0, float var1, float var2, int var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native FPixa fpixaConvertXYZToLAB(FPixa var0);

    public static native FPixa fpixaConvertLABToXYZ(FPixa var0);

    public static native int convertXYZToLAB(float var0, float var1, float var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5);

    public static native int convertLABToXYZ(float var0, float var1, float var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5);

    public static native FPixa pixConvertRGBToLAB(Pix var0);

    public static native Pix fpixaConvertLABToRGB(FPixa var0);

    public static native int convertRGBToLAB(int var0, int var1, int var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5);

    public static native int convertLABToRGB(float var0, float var1, float var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native Pix pixMakeGamutRGB(int var0);

    public static native int pixEqual(Pix var0, Pix var1, IntBuffer var2);

    public static native int pixEqualWithAlpha(Pix var0, Pix var1, int var2, IntBuffer var3);

    public static native int pixEqualWithCmap(Pix var0, Pix var1, IntBuffer var2);

    public static native int cmapEqual(PixColormap var0, PixColormap var1, int var2, IntBuffer var3);

    public static native int pixUsesCmapColor(Pix var0, IntBuffer var1);

    public static native int pixCorrelationBinary(Pix var0, Pix var1, FloatBuffer var2);

    public static native Pix pixDisplayDiff(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native Pix pixDisplayDiffBinary(Pix var0, Pix var1);

    public static native int pixCompareBinary(Pix var0, Pix var1, int var2, FloatBuffer var3, PointerByReference var4);

    public static native int pixCompareBinary(Pix var0, Pix var1, int var2, FloatByReference var3, PointerByReference var4);

    public static native int pixCompareGrayOrRGB(Pix var0, Pix var1, int var2, int var3, IntBuffer var4, FloatBuffer var5, FloatBuffer var6, PointerByReference var7);

    public static native int pixCompareGrayOrRGB(Pix var0, Pix var1, int var2, int var3, IntByReference var4, FloatByReference var5, FloatByReference var6, PointerByReference var7);

    public static native int pixCompareGray(Pix var0, Pix var1, int var2, int var3, IntBuffer var4, FloatBuffer var5, FloatBuffer var6, PointerByReference var7);

    public static native int pixCompareGray(Pix var0, Pix var1, int var2, int var3, IntByReference var4, FloatByReference var5, FloatByReference var6, PointerByReference var7);

    public static native int pixCompareRGB(Pix var0, Pix var1, int var2, int var3, IntBuffer var4, FloatBuffer var5, FloatBuffer var6, PointerByReference var7);

    public static native int pixCompareRGB(Pix var0, Pix var1, int var2, int var3, IntByReference var4, FloatByReference var5, FloatByReference var6, PointerByReference var7);

    public static native int pixCompareTiled(Pix var0, Pix var1, int var2, int var3, int var4, PointerByReference var5);

    public static native Numa pixCompareRankDifference(Pix var0, Pix var1, int var2);

    public static native int pixTestForSimilarity(Pix var0, Pix var1, int var2, int var3, float var4, float var5, IntBuffer var6, int var7);

    public static native int pixGetDifferenceStats(Pix var0, Pix var1, int var2, int var3, FloatBuffer var4, FloatBuffer var5, int var6);

    public static native Numa pixGetDifferenceHistogram(Pix var0, Pix var1, int var2);

    public static native int pixGetPerceptualDiff(Pix var0, Pix var1, int var2, int var3, int var4, FloatBuffer var5, PointerByReference var6, PointerByReference var7);

    public static native int pixGetPerceptualDiff(Pix var0, Pix var1, int var2, int var3, int var4, FloatByReference var5, PointerByReference var6, PointerByReference var7);

    public static native int pixGetPSNR(Pix var0, Pix var1, int var2, FloatBuffer var3);

    public static native int pixaComparePhotoRegionsByHisto(Pixa var0, float var1, float var2, int var3, int var4, float var5, PointerByReference var6, PointerByReference var7, PointerByReference var8, int var9);

    public static native int pixComparePhotoRegionsByHisto(Pix var0, Pix var1, Box var2, Box var3, float var4, int var5, int var6, FloatBuffer var7, int var8);

    public static native int pixGenPhotoHistos(Pix var0, Box var1, int var2, float var3, int var4, PointerByReference var5, IntBuffer var6, IntBuffer var7, int var8);

    public static native int pixGenPhotoHistos(Pix var0, Box var1, int var2, float var3, int var4, PointerByReference var5, IntByReference var6, IntByReference var7, int var8);

    public static native Pix pixPadToCenterCentroid(Pix var0, int var1);

    public static native int pixCentroid8(Pix var0, int var1, FloatBuffer var2, FloatBuffer var3);

    public static native int pixDecideIfPhotoImage(Pix var0, int var1, float var2, int var3, PointerByReference var4, Pixa var5);

    public static native int compareTilesByHisto(Numaa var0, Numaa var1, float var2, int var3, int var4, int var5, int var6, FloatBuffer var7, Pixa var8);

    public static native int pixCompareGrayByHisto(Pix var0, Pix var1, Box var2, Box var3, float var4, int var5, int var6, int var7, FloatBuffer var8, int var9);

    public static native int pixCropAlignedToCentroid(Pix var0, Pix var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native Pointer l_compressGrayHistograms(Numaa var0, int var1, int var2, NativeSizeByReference var3);

    public static native Numaa l_uncompressGrayHistograms(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3);

    public static native int pixCompareWithTranslation(Pix var0, Pix var1, int var2, IntBuffer var3, IntBuffer var4, FloatBuffer var5, int var6);

    public static native int pixBestCorrelation(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, IntBuffer var7, IntBuffer var8, IntBuffer var9, FloatBuffer var10, int var11);

    public static native Boxa pixConnComp(Pix var0, PointerByReference var1, int var2);

    public static native Boxa pixConnCompPixa(Pix var0, PointerByReference var1, int var2);

    public static native Boxa pixConnCompBB(Pix var0, int var1);

    public static native int pixCountConnComp(Pix var0, int var1, IntBuffer var2);

    public static native int nextOnPixelInRaster(Pix var0, int var1, int var2, IntBuffer var3, IntBuffer var4);

    public static native Box pixSeedfillBB(Pix var0, L_Stack var1, int var2, int var3, int var4);

    public static native Box pixSeedfill4BB(Pix var0, L_Stack var1, int var2, int var3);

    public static native Box pixSeedfill8BB(Pix var0, L_Stack var1, int var2, int var3);

    public static native int pixSeedfill(Pix var0, L_Stack var1, int var2, int var3, int var4);

    public static native int pixSeedfill4(Pix var0, L_Stack var1, int var2, int var3);

    public static native int pixSeedfill8(Pix var0, L_Stack var1, int var2, int var3);

    public static native int convertFilesTo1bpp(String var0, String var1, int var2, int var3, int var4, int var5, String var6, int var7);

    public static native Pix pixBlockconv(Pix var0, int var1, int var2);

    public static native Pix pixBlockconvGray(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixBlockconvAccum(Pix var0);

    public static native Pix pixBlockconvGrayUnnormalized(Pix var0, int var1, int var2);

    public static native Pix pixBlockconvTiled(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixBlockconvGrayTile(Pix var0, Pix var1, int var2, int var3);

    public static native int pixWindowedStats(Pix var0, int var1, int var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native Pix pixWindowedMean(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixWindowedMeanSquare(Pix var0, int var1, int var2, int var3);

    public static native int pixWindowedVariance(Pix var0, Pix var1, PointerByReference var2, PointerByReference var3);

    public static native DPix pixMeanSquareAccum(Pix var0);

    public static native Pix pixBlockrank(Pix var0, Pix var1, int var2, int var3, float var4);

    public static native Pix pixBlocksum(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCensusTransform(Pix var0, int var1, Pix var2);

    public static native Pix pixConvolve(Pix var0, L_Kernel var1, int var2, int var3);

    public static native Pix pixConvolveSep(Pix var0, L_Kernel var1, L_Kernel var2, int var3, int var4);

    public static native Pix pixConvolveRGB(Pix var0, L_Kernel var1);

    public static native Pix pixConvolveRGBSep(Pix var0, L_Kernel var1, L_Kernel var2);

    public static native FPix fpixConvolve(FPix var0, L_Kernel var1, int var2);

    public static native FPix fpixConvolveSep(FPix var0, L_Kernel var1, L_Kernel var2, int var3);

    public static native Pix pixConvolveWithBias(Pix var0, L_Kernel var1, L_Kernel var2, int var3, IntBuffer var4);

    public static native void l_setConvolveSampling(int var0, int var1);

    public static native Pix pixAddGaussianNoise(Pix var0, float var1);

    public static native float gaussDistribSampling();

    public static native int pixCorrelationScore(Pix var0, Pix var1, int var2, int var3, float var4, float var5, int var6, int var7, IntBuffer var8, FloatBuffer var9);

    public static native int pixCorrelationScoreThresholded(Pix var0, Pix var1, int var2, int var3, float var4, float var5, int var6, int var7, IntBuffer var8, IntBuffer var9, float var10);

    public static native int pixCorrelationScoreSimple(Pix var0, Pix var1, int var2, int var3, float var4, float var5, int var6, int var7, IntBuffer var8, FloatBuffer var9);

    public static native int pixCorrelationScoreShifted(Pix var0, Pix var1, int var2, int var3, int var4, int var5, IntBuffer var6, FloatBuffer var7);

    public static native L_Dewarp dewarpCreate(Pix var0, int var1);

    public static native L_Dewarp dewarpCreateRef(int var0, int var1);

    public static native void dewarpDestroy(PointerByReference var0);

    public static native L_Dewarpa dewarpaCreate(int var0, int var1, int var2, int var3, int var4);

    public static native L_Dewarpa dewarpaCreateFromPixacomp(PixaComp var0, int var1, int var2, int var3, int var4);

    public static native void dewarpaDestroy(PointerByReference var0);

    public static native int dewarpaDestroyDewarp(L_Dewarpa var0, int var1);

    public static native int dewarpaInsertDewarp(L_Dewarpa var0, L_Dewarp var1);

    public static native L_Dewarp dewarpaGetDewarp(L_Dewarpa var0, int var1);

    public static native int dewarpaSetCurvatures(L_Dewarpa var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native int dewarpaUseBothArrays(L_Dewarpa var0, int var1);

    public static native int dewarpaSetCheckColumns(L_Dewarpa var0, int var1);

    public static native int dewarpaSetMaxDistance(L_Dewarpa var0, int var1);

    public static native L_Dewarp dewarpRead(String var0);

    public static native L_Dewarp dewarpReadStream(ILeptonica.FILE var0);

    public static native L_Dewarp dewarpReadMem(ByteBuffer var0, NativeSize var1);

    public static native int dewarpWrite(String var0, L_Dewarp var1);

    public static native int dewarpWriteStream(ILeptonica.FILE var0, L_Dewarp var1);

    public static native int dewarpWriteMem(PointerByReference var0, NativeSizeByReference var1, L_Dewarp var2);

    public static native L_Dewarpa dewarpaRead(String var0);

    public static native L_Dewarpa dewarpaReadStream(ILeptonica.FILE var0);

    public static native L_Dewarpa dewarpaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int dewarpaWrite(String var0, L_Dewarpa var1);

    public static native int dewarpaWriteStream(ILeptonica.FILE var0, L_Dewarpa var1);

    public static native int dewarpaWriteMem(PointerByReference var0, NativeSizeByReference var1, L_Dewarpa var2);

    public static native int dewarpBuildPageModel(L_Dewarp var0, String var1);

    public static native int dewarpFindVertDisparity(L_Dewarp var0, Ptaa var1, int var2);

    public static native int dewarpFindHorizDisparity(L_Dewarp var0, Ptaa var1);

    public static native Ptaa dewarpGetTextlineCenters(Pix var0, int var1);

    public static native Ptaa dewarpRemoveShortLines(Pix var0, Ptaa var1, float var2, int var3);

    public static native int dewarpFindHorizSlopeDisparity(L_Dewarp var0, Pix var1, float var2, int var3);

    public static native int dewarpBuildLineModel(L_Dewarp var0, int var1, String var2);

    public static native int dewarpaModelStatus(L_Dewarpa var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native int dewarpaApplyDisparity(L_Dewarpa var0, int var1, Pix var2, int var3, int var4, int var5, PointerByReference var6, String var7);

    public static native int dewarpaApplyDisparity(L_Dewarpa var0, int var1, Pix var2, int var3, int var4, int var5, PointerByReference var6, Pointer var7);

    public static native int dewarpaApplyDisparityBoxa(L_Dewarpa var0, int var1, Pix var2, Boxa var3, int var4, int var5, int var6, PointerByReference var7, String var8);

    public static native int dewarpaApplyDisparityBoxa(L_Dewarpa var0, int var1, Pix var2, Boxa var3, int var4, int var5, int var6, PointerByReference var7, Pointer var8);

    public static native int dewarpMinimize(L_Dewarp var0);

    public static native int dewarpPopulateFullRes(L_Dewarp var0, Pix var1, int var2, int var3);

    public static native int dewarpSinglePage(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5, PointerByReference var6, int var7);

    public static native int dewarpSinglePageInit(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5, PointerByReference var6);

    public static native int dewarpSinglePageRun(Pix var0, Pix var1, L_Dewarpa var2, PointerByReference var3, int var4);

    public static native int dewarpaListPages(L_Dewarpa var0);

    public static native int dewarpaSetValidModels(L_Dewarpa var0, int var1, int var2);

    public static native int dewarpaInsertRefModels(L_Dewarpa var0, int var1, int var2);

    public static native int dewarpaStripRefModels(L_Dewarpa var0);

    public static native int dewarpaRestoreModels(L_Dewarpa var0);

    public static native int dewarpaInfo(ILeptonica.FILE var0, L_Dewarpa var1);

    public static native int dewarpaModelStats(L_Dewarpa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int dewarpaShowArrays(L_Dewarpa var0, float var1, int var2, int var3);

    public static native int dewarpDebug(L_Dewarp var0, String var1, int var2);

    public static native int dewarpShowResults(L_Dewarpa var0, Sarray var1, Boxa var2, int var3, int var4, String var5);

    public static native L_Dna l_dnaCreate(int var0);

    public static native L_Dna l_dnaCreateFromIArray(IntBuffer var0, int var1);

    public static native L_Dna l_dnaCreateFromDArray(DoubleBuffer var0, int var1, int var2);

    public static native L_Dna l_dnaMakeSequence(double var0, double var2, int var4);

    public static native void l_dnaDestroy(PointerByReference var0);

    public static native L_Dna l_dnaCopy(L_Dna var0);

    public static native L_Dna l_dnaClone(L_Dna var0);

    public static native int l_dnaEmpty(L_Dna var0);

    public static native int l_dnaAddNumber(L_Dna var0, double var1);

    public static native int l_dnaInsertNumber(L_Dna var0, int var1, double var2);

    public static native int l_dnaRemoveNumber(L_Dna var0, int var1);

    public static native int l_dnaReplaceNumber(L_Dna var0, int var1, double var2);

    public static native int l_dnaGetCount(L_Dna var0);

    public static native int l_dnaSetCount(L_Dna var0, int var1);

    public static native int l_dnaGetDValue(L_Dna var0, int var1, DoubleBuffer var2);

    public static native int l_dnaGetIValue(L_Dna var0, int var1, IntBuffer var2);

    public static native int l_dnaSetValue(L_Dna var0, int var1, double var2);

    public static native int l_dnaShiftValue(L_Dna var0, int var1, double var2);

    public static native IntByReference l_dnaGetIArray(L_Dna var0);

    public static native DoubleByReference l_dnaGetDArray(L_Dna var0, int var1);

    public static native int l_dnaGetParameters(L_Dna var0, DoubleBuffer var1, DoubleBuffer var2);

    public static native int l_dnaSetParameters(L_Dna var0, double var1, double var3);

    public static native int l_dnaCopyParameters(L_Dna var0, L_Dna var1);

    public static native L_Dna l_dnaRead(String var0);

    public static native L_Dna l_dnaReadStream(ILeptonica.FILE var0);

    public static native L_Dna l_dnaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int l_dnaWrite(String var0, L_Dna var1);

    public static native int l_dnaWriteStream(ILeptonica.FILE var0, L_Dna var1);

    public static native int l_dnaWriteStderr(L_Dna var0);

    public static native int l_dnaWriteMem(PointerByReference var0, NativeSizeByReference var1, L_Dna var2);

    public static native L_Dnaa l_dnaaCreate(int var0);

    public static native L_Dnaa l_dnaaCreateFull(int var0, int var1);

    public static native int l_dnaaTruncate(L_Dnaa var0);

    public static native void l_dnaaDestroy(PointerByReference var0);

    public static native int l_dnaaAddDna(L_Dnaa var0, L_Dna var1, int var2);

    public static native int l_dnaaGetCount(L_Dnaa var0);

    public static native int l_dnaaGetDnaCount(L_Dnaa var0, int var1);

    public static native int l_dnaaGetNumberCount(L_Dnaa var0);

    public static native L_Dna l_dnaaGetDna(L_Dnaa var0, int var1, int var2);

    public static native int l_dnaaReplaceDna(L_Dnaa var0, int var1, L_Dna var2);

    public static native int l_dnaaGetValue(L_Dnaa var0, int var1, int var2, DoubleBuffer var3);

    public static native int l_dnaaAddNumber(L_Dnaa var0, int var1, double var2);

    public static native L_Dnaa l_dnaaRead(String var0);

    public static native L_Dnaa l_dnaaReadStream(ILeptonica.FILE var0);

    public static native L_Dnaa l_dnaaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int l_dnaaWrite(String var0, L_Dnaa var1);

    public static native int l_dnaaWriteStream(ILeptonica.FILE var0, L_Dnaa var1);

    public static native int l_dnaaWriteMem(PointerByReference var0, NativeSizeByReference var1, L_Dnaa var2);

    public static native int l_dnaJoin(L_Dna var0, L_Dna var1, int var2, int var3);

    public static native L_Dna l_dnaaFlattenToDna(L_Dnaa var0);

    public static native L_Dna l_dnaSelectRange(L_Dna var0, int var1, int var2);

    public static native Numa l_dnaConvertToNuma(L_Dna var0);

    public static native L_Dna numaConvertToDna(Numa var0);

    public static native L_Dna pixConvertDataToDna(Pix var0);

    public static native L_Rbtree l_asetCreateFromDna(L_Dna var0);

    public static native int l_dnaRemoveDupsByAset(L_Dna var0, PointerByReference var1);

    public static native int l_dnaUnionByAset(L_Dna var0, L_Dna var1, PointerByReference var2);

    public static native int l_dnaIntersectionByAset(L_Dna var0, L_Dna var1, PointerByReference var2);

    public static native L_Hashmap l_hmapCreateFromDna(L_Dna var0);

    public static native int l_dnaRemoveDupsByHmap(L_Dna var0, PointerByReference var1, PointerByReference var2);

    public static native int l_dnaUnionByHmap(L_Dna var0, L_Dna var1, PointerByReference var2);

    public static native int l_dnaIntersectionByHmap(L_Dna var0, L_Dna var1, PointerByReference var2);

    public static native int l_dnaMakeHistoByHmap(L_Dna var0, PointerByReference var1, PointerByReference var2);

    public static native L_Dna l_dnaDiffAdjValues(L_Dna var0);

    public static native L_DnaHash l_dnaHashCreate(int var0, int var1);

    public static native void l_dnaHashDestroy(PointerByReference var0);

    public static native L_Dna l_dnaHashGetDna(L_DnaHash var0, long var1, int var3);

    public static native int l_dnaHashAdd(L_DnaHash var0, long var1, double var3);

    public static native Pix pixMorphDwa_2(Pix var0, Pix var1, int var2, ByteBuffer var3);

    public static native Pix pixFMorphopGen_2(Pix var0, Pix var1, int var2, ByteBuffer var3);

    public static native int fmorphopgen_low_2(IntBuffer var0, int var1, int var2, int var3, IntBuffer var4, int var5, int var6);

    public static native Pix pixSobelEdgeFilter(Pix var0, int var1);

    public static native Pix pixTwoSidedEdgeFilter(Pix var0, int var1);

    public static native int pixMeasureEdgeSmoothness(Pix var0, int var1, int var2, int var3, FloatBuffer var4, FloatBuffer var5, FloatBuffer var6, String var7);

    public static native Numa pixGetEdgeProfile(Pix var0, int var1, String var2);

    public static native int pixGetLastOffPixelInRun(Pix var0, int var1, int var2, int var3, IntBuffer var4);

    public static native int pixGetLastOnPixelInRun(Pix var0, int var1, int var2, int var3, IntBuffer var4);

    public static native Pointer encodeBase64(ByteBuffer var0, int var1, IntBuffer var2);

    public static native Pointer decodeBase64(String var0, int var1, IntBuffer var2);

    public static native Pointer encodeAscii85(ByteBuffer var0, NativeSize var1, NativeSizeByReference var2);

    public static native Pointer decodeAscii85(String var0, NativeSize var1, NativeSizeByReference var2);

    public static native Pointer encodeAscii85WithComp(ByteBuffer var0, NativeSize var1, NativeSizeByReference var2);

    public static native Pointer decodeAscii85WithComp(String var0, NativeSize var1, NativeSizeByReference var2);

    public static native Pointer reformatPacked64(String var0, int var1, int var2, int var3, int var4, IntBuffer var5);

    public static native Pix pixGammaTRC(Pix var0, Pix var1, float var2, int var3, int var4);

    public static native Pix pixGammaTRCMasked(Pix var0, Pix var1, Pix var2, float var3, int var4, int var5);

    public static native Pix pixGammaTRCWithAlpha(Pix var0, Pix var1, float var2, int var3, int var4);

    public static native Numa numaGammaTRC(float var0, int var1, int var2);

    public static native Pix pixContrastTRC(Pix var0, Pix var1, float var2);

    public static native Pix pixContrastTRCMasked(Pix var0, Pix var1, Pix var2, float var3);

    public static native Numa numaContrastTRC(float var0);

    public static native Pix pixEqualizeTRC(Pix var0, Pix var1, float var2, int var3);

    public static native Numa numaEqualizeTRC(Pix var0, float var1, int var2);

    public static native int pixTRCMap(Pix var0, Pix var1, Numa var2);

    public static native int pixTRCMapGeneral(Pix var0, Pix var1, Numa var2, Numa var3, Numa var4);

    public static native Pix pixUnsharpMasking(Pix var0, int var1, float var2);

    public static native Pix pixUnsharpMaskingGray(Pix var0, int var1, float var2);

    public static native Pix pixUnsharpMaskingFast(Pix var0, int var1, float var2, int var3);

    public static native Pix pixUnsharpMaskingGrayFast(Pix var0, int var1, float var2, int var3);

    public static native Pix pixUnsharpMaskingGray1D(Pix var0, int var1, float var2, int var3);

    public static native Pix pixUnsharpMaskingGray2D(Pix var0, int var1, float var2);

    public static native Pix pixModifyHue(Pix var0, Pix var1, float var2);

    public static native Pix pixModifySaturation(Pix var0, Pix var1, float var2);

    public static native int pixMeasureSaturation(Pix var0, int var1, FloatBuffer var2);

    public static native Pix pixModifyBrightness(Pix var0, Pix var1, float var2);

    public static native Pix pixMosaicColorShiftRGB(Pix var0, float var1, float var2, float var3, float var4, int var5);

    public static native Pix pixColorShiftRGB(Pix var0, float var1, float var2, float var3);

    public static native Pix pixDarkenGray(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixMultConstantColor(Pix var0, float var1, float var2, float var3);

    public static native Pix pixMultMatrixColor(Pix var0, L_Kernel var1);

    public static native Pix pixHalfEdgeByBandpass(Pix var0, int var1, int var2, int var3, int var4);

    public static native int fhmtautogen(Sela var0, int var1, String var2);

    public static native int fhmtautogen1(Sela var0, int var1, String var2);

    public static native int fhmtautogen2(Sela var0, int var1, String var2);

    public static native Pix pixHMTDwa_1(Pix var0, Pix var1, String var2);

    public static native Pix pixFHMTGen_1(Pix var0, Pix var1, String var2);

    public static native int fhmtgen_low_1(IntBuffer var0, int var1, int var2, int var3, IntBuffer var4, int var5, int var6);

    public static native int pixItalicWords(Pix var0, Boxa var1, Pix var2, PointerByReference var3, int var4);

    public static native Pix pixOrientCorrect(Pix var0, float var1, float var2, FloatBuffer var3, FloatBuffer var4, IntBuffer var5, int var6);

    public static native int pixOrientDetect(Pix var0, FloatBuffer var1, FloatBuffer var2, int var3, int var4);

    public static native int makeOrientDecision(float var0, float var1, float var2, float var3, IntBuffer var4, int var5);

    public static native int pixUpDownDetect(Pix var0, FloatBuffer var1, int var2, int var3, int var4);

    public static native int pixMirrorDetect(Pix var0, FloatBuffer var1, int var2, int var3);

    public static native int fmorphautogen(Sela var0, int var1, String var2);

    public static native int fmorphautogen1(Sela var0, int var1, String var2);

    public static native int fmorphautogen2(Sela var0, int var1, String var2);

    public static native Pix pixMorphDwa_1(Pix var0, Pix var1, int var2, ByteBuffer var3);

    public static native Pix pixFMorphopGen_1(Pix var0, Pix var1, int var2, ByteBuffer var3);

    public static native int fmorphopgen_low_1(IntBuffer var0, int var1, int var2, int var3, IntBuffer var4, int var5, int var6);

    public static native FPix fpixCreate(int var0, int var1);

    public static native FPix fpixCreateTemplate(FPix var0);

    public static native FPix fpixClone(FPix var0);

    public static native FPix fpixCopy(FPix var0);

    public static native void fpixDestroy(PointerByReference var0);

    public static native int fpixGetDimensions(FPix var0, IntBuffer var1, IntBuffer var2);

    public static native int fpixSetDimensions(FPix var0, int var1, int var2);

    public static native int fpixGetWpl(FPix var0);

    public static native int fpixSetWpl(FPix var0, int var1);

    public static native int fpixGetResolution(FPix var0, IntBuffer var1, IntBuffer var2);

    public static native int fpixSetResolution(FPix var0, int var1, int var2);

    public static native int fpixCopyResolution(FPix var0, FPix var1);

    public static native FloatByReference fpixGetData(FPix var0);

    public static native int fpixSetData(FPix var0, FloatBuffer var1);

    public static native int fpixGetPixel(FPix var0, int var1, int var2, FloatBuffer var3);

    public static native int fpixSetPixel(FPix var0, int var1, int var2, float var3);

    public static native FPixa fpixaCreate(int var0);

    public static native FPixa fpixaCopy(FPixa var0, int var1);

    public static native void fpixaDestroy(PointerByReference var0);

    public static native int fpixaAddFPix(FPixa var0, FPix var1, int var2);

    public static native int fpixaGetCount(FPixa var0);

    public static native FPix fpixaGetFPix(FPixa var0, int var1, int var2);

    public static native int fpixaGetFPixDimensions(FPixa var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native FloatByReference fpixaGetData(FPixa var0, int var1);

    public static native int fpixaGetPixel(FPixa var0, int var1, int var2, int var3, FloatBuffer var4);

    public static native int fpixaSetPixel(FPixa var0, int var1, int var2, int var3, float var4);

    public static native DPix dpixCreate(int var0, int var1);

    public static native DPix dpixCreateTemplate(DPix var0);

    public static native DPix dpixClone(DPix var0);

    public static native DPix dpixCopy(DPix var0);

    public static native void dpixDestroy(PointerByReference var0);

    public static native int dpixGetDimensions(DPix var0, IntBuffer var1, IntBuffer var2);

    public static native int dpixSetDimensions(DPix var0, int var1, int var2);

    public static native int dpixGetWpl(DPix var0);

    public static native int dpixSetWpl(DPix var0, int var1);

    public static native int dpixGetResolution(DPix var0, IntBuffer var1, IntBuffer var2);

    public static native int dpixSetResolution(DPix var0, int var1, int var2);

    public static native int dpixCopyResolution(DPix var0, DPix var1);

    public static native DoubleByReference dpixGetData(DPix var0);

    public static native int dpixSetData(DPix var0, DoubleBuffer var1);

    public static native int dpixGetPixel(DPix var0, int var1, int var2, DoubleBuffer var3);

    public static native int dpixSetPixel(DPix var0, int var1, int var2, double var3);

    public static native FPix fpixRead(String var0);

    public static native FPix fpixReadStream(ILeptonica.FILE var0);

    public static native FPix fpixReadMem(ByteBuffer var0, NativeSize var1);

    public static native int fpixWrite(String var0, FPix var1);

    public static native int fpixWriteStream(ILeptonica.FILE var0, FPix var1);

    public static native int fpixWriteMem(PointerByReference var0, NativeSizeByReference var1, FPix var2);

    public static native FPix fpixEndianByteSwap(FPix var0, FPix var1);

    public static native DPix dpixRead(String var0);

    public static native DPix dpixReadStream(ILeptonica.FILE var0);

    public static native DPix dpixReadMem(ByteBuffer var0, NativeSize var1);

    public static native int dpixWrite(String var0, DPix var1);

    public static native int dpixWriteStream(ILeptonica.FILE var0, DPix var1);

    public static native int dpixWriteMem(PointerByReference var0, NativeSizeByReference var1, DPix var2);

    public static native DPix dpixEndianByteSwap(DPix var0, DPix var1);

    public static native int fpixPrintStream(ILeptonica.FILE var0, FPix var1, int var2);

    public static native FPix pixConvertToFPix(Pix var0, int var1);

    public static native DPix pixConvertToDPix(Pix var0, int var1);

    public static native Pix fpixConvertToPix(FPix var0, int var1, int var2, int var3);

    public static native Pix fpixDisplayMaxDynamicRange(FPix var0);

    public static native DPix fpixConvertToDPix(FPix var0);

    public static native Pix dpixConvertToPix(DPix var0, int var1, int var2, int var3);

    public static native FPix dpixConvertToFPix(DPix var0);

    public static native int fpixGetMin(FPix var0, FloatBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int fpixGetMax(FPix var0, FloatBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int dpixGetMin(DPix var0, DoubleBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int dpixGetMax(DPix var0, DoubleBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native FPix fpixScaleByInteger(FPix var0, int var1);

    public static native DPix dpixScaleByInteger(DPix var0, int var1);

    public static native FPix fpixLinearCombination(FPix var0, FPix var1, FPix var2, float var3, float var4);

    public static native int fpixAddMultConstant(FPix var0, float var1, float var2);

    public static native DPix dpixLinearCombination(DPix var0, DPix var1, DPix var2, float var3, float var4);

    public static native int dpixAddMultConstant(DPix var0, double var1, double var3);

    public static native int fpixSetAllArbitrary(FPix var0, float var1);

    public static native int dpixSetAllArbitrary(DPix var0, double var1);

    public static native FPix fpixAddBorder(FPix var0, int var1, int var2, int var3, int var4);

    public static native FPix fpixRemoveBorder(FPix var0, int var1, int var2, int var3, int var4);

    public static native FPix fpixAddMirroredBorder(FPix var0, int var1, int var2, int var3, int var4);

    public static native FPix fpixAddContinuedBorder(FPix var0, int var1, int var2, int var3, int var4);

    public static native FPix fpixAddSlopeBorder(FPix var0, int var1, int var2, int var3, int var4);

    public static native int fpixRasterop(FPix var0, int var1, int var2, int var3, int var4, FPix var5, int var6, int var7);

    public static native FPix fpixRotateOrth(FPix var0, int var1);

    public static native FPix fpixRotate180(FPix var0, FPix var1);

    public static native FPix fpixRotate90(FPix var0, int var1);

    public static native FPix fpixFlipLR(FPix var0, FPix var1);

    public static native FPix fpixFlipTB(FPix var0, FPix var1);

    public static native FPix fpixAffinePta(FPix var0, Pta var1, Pta var2, int var3, float var4);

    public static native FPix fpixAffine(FPix var0, FloatBuffer var1, float var2);

    public static native FPix fpixProjectivePta(FPix var0, Pta var1, Pta var2, int var3, float var4);

    public static native FPix fpixProjective(FPix var0, FloatBuffer var1, float var2);

    public static native int linearInterpolatePixelFloat(FloatBuffer var0, int var1, int var2, float var3, float var4, float var5, FloatBuffer var6);

    public static native Pix fpixThresholdToPix(FPix var0, float var1);

    public static native FPix pixComponentFunction(Pix var0, float var1, float var2, float var3, float var4, float var5, float var6);

    public static native Pix pixReadStreamGif(ILeptonica.FILE var0);

    public static native Pix pixReadMemGif(ByteBuffer var0, NativeSize var1);

    public static native int pixWriteStreamGif(ILeptonica.FILE var0, Pix var1);

    public static native int pixWriteMemGif(PointerByReference var0, NativeSizeByReference var1, Pix var2);

    public static native GPlot gplotCreate(String var0, int var1, String var2, String var3, String var4);

    public static native void gplotDestroy(PointerByReference var0);

    public static native int gplotAddPlot(GPlot var0, Numa var1, Numa var2, int var3, String var4);

    public static native int gplotSetScaling(GPlot var0, int var1);

    public static native Pix gplotMakeOutputPix(GPlot var0);

    public static native int gplotMakeOutput(GPlot var0);

    public static native int gplotGenCommandFile(GPlot var0);

    public static native int gplotGenDataFiles(GPlot var0);

    public static native int gplotSimple1(Numa var0, int var1, String var2, String var3);

    public static native int gplotSimple2(Numa var0, Numa var1, int var2, String var3, String var4);

    public static native int gplotSimpleN(Numaa var0, int var1, String var2, String var3);

    public static native Pix gplotSimplePix1(Numa var0, String var1);

    public static native Pix gplotSimplePix2(Numa var0, Numa var1, String var2);

    public static native Pix gplotSimplePixN(Numaa var0, String var1);

    public static native GPlot gplotSimpleXY1(Numa var0, Numa var1, int var2, int var3, String var4, String var5);

    public static native GPlot gplotSimpleXY2(Numa var0, Numa var1, Numa var2, int var3, int var4, String var5, String var6);

    public static native GPlot gplotSimpleXYN(Numa var0, Numaa var1, int var2, int var3, String var4, String var5);

    public static native Pix gplotGeneralPix1(Numa var0, int var1, String var2, String var3, String var4, String var5);

    public static native Pix gplotGeneralPix2(Numa var0, Numa var1, int var2, String var3, String var4, String var5, String var6);

    public static native Pix gplotGeneralPixN(Numa var0, Numaa var1, int var2, String var3, String var4, String var5, String var6);

    public static native GPlot gplotRead(String var0);

    public static native int gplotWrite(String var0, GPlot var1);

    public static native Pta generatePtaLine(int var0, int var1, int var2, int var3);

    public static native Pta generatePtaWideLine(int var0, int var1, int var2, int var3, int var4);

    public static native Pta generatePtaBox(Box var0, int var1);

    public static native Pta generatePtaBoxa(Boxa var0, int var1, int var2);

    public static native Pta generatePtaHashBox(Box var0, int var1, int var2, int var3, int var4);

    public static native Pta generatePtaHashBoxa(Boxa var0, int var1, int var2, int var3, int var4, int var5);

    public static native Ptaa generatePtaaBoxa(Boxa var0);

    public static native Ptaa generatePtaaHashBoxa(Boxa var0, int var1, int var2, int var3, int var4);

    public static native Pta generatePtaPolyline(Pta var0, int var1, int var2, int var3);

    public static native Pta generatePtaGrid(int var0, int var1, int var2, int var3, int var4);

    public static native Pta convertPtaLineTo4cc(Pta var0);

    public static native Pta generatePtaFilledCircle(int var0);

    public static native Pta generatePtaFilledSquare(int var0);

    public static native Pta generatePtaLineFromPt(int var0, int var1, double var2, double var4);

    public static native int locatePtRadially(int var0, int var1, double var2, double var4, DoubleBuffer var6, DoubleBuffer var7);

    public static native int pixRenderPlotFromNuma(PointerByReference var0, Numa var1, int var2, int var3, int var4, int var5);

    public static native Pta makePlotPtaFromNuma(Numa var0, int var1, int var2, int var3, int var4);

    public static native int pixRenderPlotFromNumaGen(PointerByReference var0, Numa var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native Pta makePlotPtaFromNumaGen(Numa var0, int var1, int var2, int var3, int var4, int var5);

    public static native int pixRenderPta(Pix var0, Pta var1, int var2);

    public static native int pixRenderPtaArb(Pix var0, Pta var1, byte var2, byte var3, byte var4);

    public static native int pixRenderPtaBlend(Pix var0, Pta var1, byte var2, byte var3, byte var4, float var5);

    public static native int pixRenderLine(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native int pixRenderLineArb(Pix var0, int var1, int var2, int var3, int var4, int var5, byte var6, byte var7, byte var8);

    public static native int pixRenderLineBlend(Pix var0, int var1, int var2, int var3, int var4, int var5, byte var6, byte var7, byte var8, float var9);

    public static native int pixRenderBox(Pix var0, Box var1, int var2, int var3);

    public static native int pixRenderBoxArb(Pix var0, Box var1, int var2, byte var3, byte var4, byte var5);

    public static native int pixRenderBoxBlend(Pix var0, Box var1, int var2, byte var3, byte var4, byte var5, float var6);

    public static native int pixRenderBoxa(Pix var0, Boxa var1, int var2, int var3);

    public static native int pixRenderBoxaArb(Pix var0, Boxa var1, int var2, byte var3, byte var4, byte var5);

    public static native int pixRenderBoxaBlend(Pix var0, Boxa var1, int var2, byte var3, byte var4, byte var5, float var6, int var7);

    public static native int pixRenderHashBox(Pix var0, Box var1, int var2, int var3, int var4, int var5, int var6);

    public static native int pixRenderHashBoxArb(Pix var0, Box var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public static native int pixRenderHashBoxBlend(Pix var0, Box var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, float var9);

    public static native int pixRenderHashMaskArb(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    public static native int pixRenderHashBoxa(Pix var0, Boxa var1, int var2, int var3, int var4, int var5, int var6);

    public static native int pixRenderHashBoxaArb(Pix var0, Boxa var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public static native int pixRenderHashBoxaBlend(Pix var0, Boxa var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, float var9);

    public static native int pixRenderPolyline(Pix var0, Pta var1, int var2, int var3, int var4);

    public static native int pixRenderPolylineArb(Pix var0, Pta var1, int var2, byte var3, byte var4, byte var5, int var6);

    public static native int pixRenderPolylineBlend(Pix var0, Pta var1, int var2, byte var3, byte var4, byte var5, float var6, int var7, int var8);

    public static native int pixRenderGridArb(Pix var0, int var1, int var2, int var3, byte var4, byte var5, byte var6);

    public static native Pix pixRenderRandomCmapPtaa(Pix var0, Ptaa var1, int var2, int var3, int var4);

    public static native Pix pixRenderPolygon(Pta var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native Pix pixFillPolygon(Pix var0, Pta var1, int var2, int var3);

    public static native Pix pixRenderContours(Pix var0, int var1, int var2, int var3);

    public static native Pix fpixAutoRenderContours(FPix var0, int var1);

    public static native Pix fpixRenderContours(FPix var0, float var1, float var2);

    public static native Pta pixGeneratePtaBoundary(Pix var0, int var1);

    public static native Pix pixErodeGray(Pix var0, int var1, int var2);

    public static native Pix pixDilateGray(Pix var0, int var1, int var2);

    public static native Pix pixOpenGray(Pix var0, int var1, int var2);

    public static native Pix pixCloseGray(Pix var0, int var1, int var2);

    public static native Pix pixErodeGray3(Pix var0, int var1, int var2);

    public static native Pix pixDilateGray3(Pix var0, int var1, int var2);

    public static native Pix pixOpenGray3(Pix var0, int var1, int var2);

    public static native Pix pixCloseGray3(Pix var0, int var1, int var2);

    public static native Pix pixDitherToBinary(Pix var0);

    public static native Pix pixDitherToBinarySpec(Pix var0, int var1, int var2);

    public static native void ditherToBinaryLineLow(IntBuffer var0, int var1, IntBuffer var2, IntBuffer var3, int var4, int var5, int var6);

    public static native Pix pixThresholdToBinary(Pix var0, int var1);

    public static native void thresholdToBinaryLineLow(IntBuffer var0, int var1, IntBuffer var2, int var3, int var4);

    public static native Pix pixVarThresholdToBinary(Pix var0, Pix var1);

    public static native Pix pixAdaptThresholdToBinary(Pix var0, Pix var1, float var2);

    public static native Pix pixAdaptThresholdToBinaryGen(Pix var0, Pix var1, float var2, int var3, int var4, int var5);

    public static native Pix pixGenerateMaskByValue(Pix var0, int var1, int var2);

    public static native Pix pixGenerateMaskByBand(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixDitherTo2bpp(Pix var0, int var1);

    public static native Pix pixDitherTo2bppSpec(Pix var0, int var1, int var2, int var3);

    public static native Pix pixThresholdTo2bpp(Pix var0, int var1, int var2);

    public static native Pix pixThresholdTo4bpp(Pix var0, int var1, int var2);

    public static native Pix pixThresholdOn8bpp(Pix var0, int var1, int var2);

    public static native Pix pixThresholdGrayArb(Pix var0, String var1, int var2, int var3, int var4, int var5);

    public static native IntByReference makeGrayQuantIndexTable(int var0);

    public static native int makeGrayQuantTableArb(Numa var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixGenerateMaskByBand32(Pix var0, int var1, int var2, int var3, float var4, float var5);

    public static native Pix pixGenerateMaskByDiscr32(Pix var0, int var1, int var2, int var3);

    public static native Pix pixGrayQuantFromHisto(Pix var0, Pix var1, Pix var2, float var3, int var4);

    public static native Pix pixGrayQuantFromCmap(Pix var0, PixColormap var1, int var2);

    public static native L_Hashmap l_hmapCreate(int var0, int var1);

    public static native void l_hmapDestroy(PointerByReference var0);

    public static native L_Hashitem l_hmapLookup(L_Hashmap var0, long var1, long var3, int var5);

    public static native int l_hmapRehash(L_Hashmap var0);

    public static native L_Heap lheapCreate(int var0, int var1);

    public static native void lheapDestroy(PointerByReference var0, int var1);

    public static native int lheapAdd(L_Heap var0, Pointer var1);

    public static native Pointer lheapRemove(L_Heap var0);

    public static native int lheapGetCount(L_Heap var0);

    public static native Pointer lheapGetElement(L_Heap var0, int var1);

    public static native int lheapSort(L_Heap var0);

    public static native int lheapSortStrictOrder(L_Heap var0);

    public static native int lheapPrint(ILeptonica.FILE var0, L_Heap var1);

    public static native JbClasser jbRankHausInit(int var0, int var1, int var2, int var3, float var4);

    public static native JbClasser jbCorrelationInit(int var0, int var1, int var2, float var3, float var4);

    public static native JbClasser jbCorrelationInitWithoutComponents(int var0, int var1, int var2, float var3, float var4);

    public static native int jbAddPages(JbClasser var0, Sarray var1);

    public static native int jbAddPage(JbClasser var0, Pix var1);

    public static native int jbAddPageComponents(JbClasser var0, Pix var1, Boxa var2, Pixa var3);

    public static native int jbClassifyRankHaus(JbClasser var0, Boxa var1, Pixa var2);

    public static native int pixHaustest(Pix var0, Pix var1, Pix var2, Pix var3, float var4, float var5, int var6, int var7);

    public static native int pixRankHaustest(Pix var0, Pix var1, Pix var2, Pix var3, float var4, float var5, int var6, int var7, int var8, int var9, float var10, IntBuffer var11);

    public static native int jbClassifyCorrelation(JbClasser var0, Boxa var1, Pixa var2);

    public static native int jbGetComponents(Pix var0, int var1, int var2, int var3, PointerByReference var4, PointerByReference var5);

    public static native int pixWordMaskByDilation(Pix var0, PointerByReference var1, IntBuffer var2, Pixa var3);

    public static native int pixWordMaskByDilation(Pix var0, PointerByReference var1, IntByReference var2, Pixa var3);

    public static native int pixWordBoxesByDilation(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5, IntBuffer var6, Pixa var7);

    public static native int pixWordBoxesByDilation(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5, IntByReference var6, Pixa var7);

    public static native Pixa jbAccumulateComposites(Pixaa var0, PointerByReference var1, PointerByReference var2);

    public static native Pixa jbTemplatesFromComposites(Pixa var0, Numa var1);

    public static native JbClasser jbClasserCreate(int var0, int var1);

    public static native void jbClasserDestroy(PointerByReference var0);

    public static native JbData jbDataSave(JbClasser var0);

    public static native void jbDataDestroy(PointerByReference var0);

    public static native int jbDataWrite(String var0, JbData var1);

    public static native JbData jbDataRead(String var0);

    public static native Pixa jbDataRender(JbData var0, int var1);

    public static native int jbGetULCorners(JbClasser var0, Pix var1, Boxa var2);

    public static native int jbGetLLCorners(JbClasser var0);

    public static native int readHeaderJp2k(String var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int freadHeaderJp2k(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int readHeaderMemJp2k(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int fgetJp2kResolution(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2);

    public static native int readResolutionMemJp2k(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3);

    public static native Pix pixReadJp2k(String var0, int var1, Box var2, int var3, int var4);

    public static native Pix pixReadStreamJp2k(ILeptonica.FILE var0, int var1, Box var2, int var3, int var4);

    public static native int pixWriteJp2k(String var0, Pix var1, int var2, int var3, int var4, int var5);

    public static native int pixWriteStreamJp2k(ILeptonica.FILE var0, Pix var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixReadMemJp2k(ByteBuffer var0, NativeSize var1, int var2, Box var3, int var4, int var5);

    public static native int pixWriteMemJp2k(PointerByReference var0, NativeSizeByReference var1, Pix var2, int var3, int var4, int var5, int var6);

    public static native Pix pixReadJpeg(String var0, int var1, int var2, IntBuffer var3, int var4);

    public static native Pix pixReadStreamJpeg(ILeptonica.FILE var0, int var1, int var2, IntBuffer var3, int var4);

    public static native int readHeaderJpeg(String var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int freadHeaderJpeg(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int fgetJpegResolution(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2);

    public static native int fgetJpegComment(ILeptonica.FILE var0, PointerByReference var1);

    public static native int pixWriteJpeg(String var0, Pix var1, int var2, int var3);

    public static native int pixWriteStreamJpeg(ILeptonica.FILE var0, Pix var1, int var2, int var3);

    public static native Pix pixReadMemJpeg(ByteBuffer var0, NativeSize var1, int var2, int var3, IntBuffer var4, int var5);

    public static native int readHeaderMemJpeg(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int readResolutionMemJpeg(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3);

    public static native int pixWriteMemJpeg(PointerByReference var0, NativeSizeByReference var1, Pix var2, int var3, int var4);

    public static native int pixSetChromaSampling(Pix var0, int var1);

    public static native L_Kernel kernelCreate(int var0, int var1);

    public static native void kernelDestroy(PointerByReference var0);

    public static native L_Kernel kernelCopy(L_Kernel var0);

    public static native int kernelGetElement(L_Kernel var0, int var1, int var2, FloatBuffer var3);

    public static native int kernelSetElement(L_Kernel var0, int var1, int var2, float var3);

    public static native int kernelGetParameters(L_Kernel var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int kernelSetOrigin(L_Kernel var0, int var1, int var2);

    public static native int kernelGetSum(L_Kernel var0, FloatBuffer var1);

    public static native int kernelGetMinMax(L_Kernel var0, FloatBuffer var1, FloatBuffer var2);

    public static native L_Kernel kernelNormalize(L_Kernel var0, float var1);

    public static native L_Kernel kernelInvert(L_Kernel var0);

    public static native PointerByReference create2dFloatArray(int var0, int var1);

    public static native L_Kernel kernelRead(String var0);

    public static native L_Kernel kernelReadStream(ILeptonica.FILE var0);

    public static native int kernelWrite(String var0, L_Kernel var1);

    public static native int kernelWriteStream(ILeptonica.FILE var0, L_Kernel var1);

    public static native L_Kernel kernelCreateFromString(int var0, int var1, int var2, int var3, String var4);

    public static native L_Kernel kernelCreateFromFile(String var0);

    public static native L_Kernel kernelCreateFromPix(Pix var0, int var1, int var2);

    public static native Pix kernelDisplayInPix(L_Kernel var0, int var1, int var2);

    public static native Numa parseStringForNumbers(String var0, String var1);

    public static native L_Kernel makeFlatKernel(int var0, int var1, int var2, int var3);

    public static native L_Kernel makeGaussianKernel(int var0, int var1, float var2, float var3);

    public static native int makeGaussianKernelSep(int var0, int var1, float var2, float var3, PointerByReference var4, PointerByReference var5);

    public static native L_Kernel makeDoGKernel(int var0, int var1, float var2, float var3);

    public static native Pointer getImagelibVersions();

    public static native void listDestroy(PointerByReference var0);

    public static native int listAddToHead(PointerByReference var0, Pointer var1);

    public static native int listAddToTail(PointerByReference var0, PointerByReference var1, Pointer var2);

    public static native int listInsertBefore(PointerByReference var0, DoubleLinkedList var1, Pointer var2);

    public static native int listInsertAfter(PointerByReference var0, DoubleLinkedList var1, Pointer var2);

    public static native Pointer listRemoveElement(PointerByReference var0, DoubleLinkedList var1);

    public static native Pointer listRemoveFromHead(PointerByReference var0);

    public static native Pointer listRemoveFromTail(PointerByReference var0, PointerByReference var1);

    public static native DoubleLinkedList listFindElement(DoubleLinkedList var0, Pointer var1);

    public static native DoubleLinkedList listFindTail(DoubleLinkedList var0);

    public static native int listGetCount(DoubleLinkedList var0);

    public static native int listReverse(PointerByReference var0);

    public static native int listJoin(PointerByReference var0, PointerByReference var1);

    public static native L_Rbtree l_amapCreate(int var0);

    public static native Rb_Type l_amapFind(L_Rbtree var0, Rb_Type.ByValue var1);

    public static native void l_amapInsert(L_Rbtree var0, Rb_Type.ByValue var1, Rb_Type.ByValue var2);

    public static native void l_amapDelete(L_Rbtree var0, Rb_Type.ByValue var1);

    public static native void l_amapDestroy(PointerByReference var0);

    public static native L_Rbtree_Node l_amapGetFirst(L_Rbtree var0);

    public static native L_Rbtree_Node l_amapGetNext(L_Rbtree_Node var0);

    public static native L_Rbtree_Node l_amapGetLast(L_Rbtree var0);

    public static native L_Rbtree_Node l_amapGetPrev(L_Rbtree_Node var0);

    public static native int l_amapSize(L_Rbtree var0);

    public static native L_Rbtree l_asetCreate(int var0);

    public static native Rb_Type l_asetFind(L_Rbtree var0, Rb_Type.ByValue var1);

    public static native void l_asetInsert(L_Rbtree var0, Rb_Type.ByValue var1);

    public static native void l_asetDelete(L_Rbtree var0, Rb_Type.ByValue var1);

    public static native void l_asetDestroy(PointerByReference var0);

    public static native L_Rbtree_Node l_asetGetFirst(L_Rbtree var0);

    public static native L_Rbtree_Node l_asetGetNext(L_Rbtree_Node var0);

    public static native L_Rbtree_Node l_asetGetLast(L_Rbtree var0);

    public static native L_Rbtree_Node l_asetGetPrev(L_Rbtree_Node var0);

    public static native int l_asetSize(L_Rbtree var0);

    public static native Pix generateBinaryMaze(int var0, int var1, int var2, int var3, float var4, float var5);

    public static native Pta pixSearchBinaryMaze(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5);

    public static native Pta pixSearchGrayMaze(Pix var0, int var1, int var2, int var3, int var4, PointerByReference var5);

    public static native Pix pixDilate(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixErode(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixHMT(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixOpen(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixClose(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixCloseSafe(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixOpenGeneralized(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixCloseGeneralized(Pix var0, Pix var1, Pointer var2);

    public static native Pix pixDilateBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixErodeBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixOpenBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCloseBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCloseSafeBrick(Pix var0, Pix var1, int var2, int var3);

    public static native int selectComposableSels(int var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native int selectComposableSizes(int var0, IntBuffer var1, IntBuffer var2);

    public static native Pix pixDilateCompBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixErodeCompBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixOpenCompBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCloseCompBrick(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCloseSafeCompBrick(Pix var0, Pix var1, int var2, int var3);

    public static native void resetMorphBoundaryCondition(int var0);

    public static native int getMorphBorderPixelColor(int var0, int var1);

    public static native Pix pixExtractBoundary(Pix var0, int var1);

    public static native Pix pixMorphSequenceMasked(Pix var0, Pix var1, String var2, int var3);

    public static native Pix pixMorphSequenceByComponent(Pix var0, String var1, int var2, int var3, int var4, PointerByReference var5);

    public static native Pix pixMorphSequenceByComponent(Pix var0, Pointer var1, int var2, int var3, int var4, PointerByReference var5);

    public static native Pixa pixaMorphSequenceByComponent(Pixa var0, String var1, int var2, int var3);

    public static native Pix pixMorphSequenceByRegion(Pix var0, Pix var1, String var2, int var3, int var4, int var5, PointerByReference var6);

    public static native Pix pixMorphSequenceByRegion(Pix var0, Pix var1, Pointer var2, int var3, int var4, int var5, PointerByReference var6);

    public static native Pixa pixaMorphSequenceByRegion(Pix var0, Pixa var1, String var2, int var3, int var4);

    public static native Pix pixUnionOfMorphOps(Pix var0, Sela var1, int var2);

    public static native Pix pixIntersectionOfMorphOps(Pix var0, Sela var1, int var2);

    public static native Pix pixSelectiveConnCompFill(Pix var0, int var1, int var2, int var3);

    public static native int pixRemoveMatchedPattern(Pix var0, Pix var1, Pix var2, int var3, int var4, int var5);

    public static native Pix pixDisplayMatchedPattern(Pix var0, Pix var1, Pix var2, int var3, int var4, int var5, float var6, int var7);

    public static native Pixa pixaExtendByMorph(Pixa var0, int var1, int var2, Pointer var3, int var4);

    public static native Pixa pixaExtendByScaling(Pixa var0, Numa var1, int var2, int var3);

    public static native Pix pixSeedfillMorph(Pix var0, Pix var1, int var2, int var3);

    public static native Numa pixRunHistogramMorph(Pix var0, int var1, int var2, int var3);

    public static native Pix pixTophat(Pix var0, int var1, int var2, int var3);

    public static native Pix pixHDome(Pix var0, int var1, int var2);

    public static native Pix pixFastTophat(Pix var0, int var1, int var2, int var3);

    public static native Pix pixMorphGradient(Pix var0, int var1, int var2, int var3);

    public static native Pta pixaCentroids(Pixa var0);

    public static native int pixCentroid(Pix var0, IntBuffer var1, IntBuffer var2, FloatBuffer var3, FloatBuffer var4);

    public static native Pix pixDilateBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixErodeBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixOpenBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCloseBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixDilateCompBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixErodeCompBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixOpenCompBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCloseCompBrickDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixDilateCompBrickExtendDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixErodeCompBrickExtendDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixOpenCompBrickExtendDwa(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixCloseCompBrickExtendDwa(Pix var0, Pix var1, int var2, int var3);

    public static native int getExtendedCompositeParameters(int var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native Pix pixMorphSequence(Pix var0, String var1, int var2);

    public static native Pix pixMorphCompSequence(Pix var0, String var1, int var2);

    public static native Pix pixMorphSequenceDwa(Pix var0, String var1, int var2);

    public static native Pix pixMorphCompSequenceDwa(Pix var0, String var1, int var2);

    public static native int morphSequenceVerify(Sarray var0);

    public static native Pix pixGrayMorphSequence(Pix var0, String var1, int var2, int var3);

    public static native Pix pixColorMorphSequence(Pix var0, String var1, int var2, int var3);

    public static native Numa numaCreate(int var0);

    public static native Numa numaCreateFromIArray(IntBuffer var0, int var1);

    public static native Numa numaCreateFromFArray(FloatBuffer var0, int var1, int var2);

    public static native Numa numaCreateFromString(String var0);

    public static native void numaDestroy(PointerByReference var0);

    public static native Numa numaCopy(Numa var0);

    public static native Numa numaClone(Numa var0);

    public static native int numaEmpty(Numa var0);

    public static native int numaAddNumber(Numa var0, float var1);

    public static native int numaInsertNumber(Numa var0, int var1, float var2);

    public static native int numaRemoveNumber(Numa var0, int var1);

    public static native int numaReplaceNumber(Numa var0, int var1, float var2);

    public static native int numaGetCount(Numa var0);

    public static native int numaSetCount(Numa var0, int var1);

    public static native int numaGetFValue(Numa var0, int var1, FloatBuffer var2);

    public static native int numaGetIValue(Numa var0, int var1, IntBuffer var2);

    public static native int numaSetValue(Numa var0, int var1, float var2);

    public static native int numaShiftValue(Numa var0, int var1, float var2);

    public static native IntByReference numaGetIArray(Numa var0);

    public static native FloatByReference numaGetFArray(Numa var0, int var1);

    public static native int numaGetParameters(Numa var0, FloatBuffer var1, FloatBuffer var2);

    public static native int numaSetParameters(Numa var0, float var1, float var2);

    public static native int numaCopyParameters(Numa var0, Numa var1);

    public static native Sarray numaConvertToSarray(Numa var0, int var1, int var2, int var3, int var4);

    public static native Numa numaRead(String var0);

    public static native Numa numaReadStream(ILeptonica.FILE var0);

    public static native Numa numaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int numaWriteDebug(String var0, Numa var1);

    public static native int numaWrite(String var0, Numa var1);

    public static native int numaWriteStream(ILeptonica.FILE var0, Numa var1);

    public static native int numaWriteStderr(Numa var0);

    public static native int numaWriteMem(PointerByReference var0, NativeSizeByReference var1, Numa var2);

    public static native Numaa numaaCreate(int var0);

    public static native Numaa numaaCreateFull(int var0, int var1);

    public static native int numaaTruncate(Numaa var0);

    public static native void numaaDestroy(PointerByReference var0);

    public static native int numaaAddNuma(Numaa var0, Numa var1, int var2);

    public static native int numaaGetCount(Numaa var0);

    public static native int numaaGetNumaCount(Numaa var0, int var1);

    public static native int numaaGetNumberCount(Numaa var0);

    public static native PointerByReference numaaGetPtrArray(Numaa var0);

    public static native Numa numaaGetNuma(Numaa var0, int var1, int var2);

    public static native int numaaReplaceNuma(Numaa var0, int var1, Numa var2);

    public static native int numaaGetValue(Numaa var0, int var1, int var2, FloatBuffer var3, IntBuffer var4);

    public static native int numaaAddNumber(Numaa var0, int var1, float var2);

    public static native Numaa numaaRead(String var0);

    public static native Numaa numaaReadStream(ILeptonica.FILE var0);

    public static native Numaa numaaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int numaaWrite(String var0, Numaa var1);

    public static native int numaaWriteStream(ILeptonica.FILE var0, Numaa var1);

    public static native int numaaWriteMem(PointerByReference var0, NativeSizeByReference var1, Numaa var2);

    public static native Numa numaArithOp(Numa var0, Numa var1, Numa var2, int var3);

    public static native Numa numaLogicalOp(Numa var0, Numa var1, Numa var2, int var3);

    public static native Numa numaInvert(Numa var0, Numa var1);

    public static native int numaSimilar(Numa var0, Numa var1, float var2, IntBuffer var3);

    public static native int numaAddToNumber(Numa var0, int var1, float var2);

    public static native int numaGetMin(Numa var0, FloatBuffer var1, IntBuffer var2);

    public static native int numaGetMax(Numa var0, FloatBuffer var1, IntBuffer var2);

    public static native int numaGetSum(Numa var0, FloatBuffer var1);

    public static native Numa numaGetPartialSums(Numa var0);

    public static native int numaGetSumOnInterval(Numa var0, int var1, int var2, FloatBuffer var3);

    public static native int numaHasOnlyIntegers(Numa var0, IntBuffer var1);

    public static native int numaGetMean(Numa var0, FloatBuffer var1);

    public static native int numaGetMeanAbsval(Numa var0, FloatBuffer var1);

    public static native Numa numaSubsample(Numa var0, int var1);

    public static native Numa numaMakeDelta(Numa var0);

    public static native Numa numaMakeSequence(float var0, float var1, int var2);

    public static native Numa numaMakeConstant(float var0, int var1);

    public static native Numa numaMakeAbsval(Numa var0, Numa var1);

    public static native Numa numaAddBorder(Numa var0, int var1, int var2, float var3);

    public static native Numa numaAddSpecifiedBorder(Numa var0, int var1, int var2, int var3);

    public static native Numa numaRemoveBorder(Numa var0, int var1, int var2);

    public static native int numaCountNonzeroRuns(Numa var0, IntBuffer var1);

    public static native int numaGetNonzeroRange(Numa var0, float var1, IntBuffer var2, IntBuffer var3);

    public static native int numaGetCountRelativeToZero(Numa var0, int var1, IntBuffer var2);

    public static native Numa numaClipToInterval(Numa var0, int var1, int var2);

    public static native Numa numaMakeThresholdIndicator(Numa var0, float var1, int var2);

    public static native Numa numaUniformSampling(Numa var0, int var1);

    public static native Numa numaReverse(Numa var0, Numa var1);

    public static native Numa numaLowPassIntervals(Numa var0, float var1, float var2);

    public static native Numa numaThresholdEdges(Numa var0, float var1, float var2, float var3);

    public static native int numaGetSpanValues(Numa var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native int numaGetEdgeValues(Numa var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int numaInterpolateEqxVal(float var0, float var1, Numa var2, int var3, float var4, FloatBuffer var5);

    public static native int numaInterpolateArbxVal(Numa var0, Numa var1, int var2, float var3, FloatBuffer var4);

    public static native int numaInterpolateEqxInterval(float var0, float var1, Numa var2, int var3, float var4, float var5, int var6, PointerByReference var7, PointerByReference var8);

    public static native int numaInterpolateArbxInterval(Numa var0, Numa var1, int var2, float var3, float var4, int var5, PointerByReference var6, PointerByReference var7);

    public static native int numaFitMax(Numa var0, FloatBuffer var1, Numa var2, FloatBuffer var3);

    public static native int numaDifferentiateInterval(Numa var0, Numa var1, float var2, float var3, int var4, PointerByReference var5, PointerByReference var6);

    public static native int numaIntegrateInterval(Numa var0, Numa var1, float var2, float var3, int var4, FloatBuffer var5);

    public static native int numaSortGeneral(Numa var0, PointerByReference var1, PointerByReference var2, PointerByReference var3, int var4, int var5);

    public static native Numa numaSortAutoSelect(Numa var0, int var1);

    public static native Numa numaSortIndexAutoSelect(Numa var0, int var1);

    public static native int numaChooseSortType(Numa var0);

    public static native Numa numaSort(Numa var0, Numa var1, int var2);

    public static native Numa numaBinSort(Numa var0, int var1);

    public static native Numa numaGetSortIndex(Numa var0, int var1);

    public static native Numa numaGetBinSortIndex(Numa var0, int var1);

    public static native Numa numaSortByIndex(Numa var0, Numa var1);

    public static native int numaIsSorted(Numa var0, int var1, IntBuffer var2);

    public static native int numaSortPair(Numa var0, Numa var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native Numa numaInvertMap(Numa var0);

    public static native int numaAddSorted(Numa var0, float var1);

    public static native int numaFindSortedLoc(Numa var0, float var1, IntBuffer var2);

    public static native Numa numaPseudorandomSequence(int var0, int var1);

    public static native Numa numaRandomPermutation(Numa var0, int var1);

    public static native int numaGetRankValue(Numa var0, float var1, Numa var2, int var3, FloatBuffer var4);

    public static native int numaGetMedian(Numa var0, FloatBuffer var1);

    public static native int numaGetBinnedMedian(Numa var0, IntBuffer var1);

    public static native int numaGetMeanDevFromMedian(Numa var0, float var1, FloatBuffer var2);

    public static native int numaGetMedianDevFromMedian(Numa var0, FloatBuffer var1, FloatBuffer var2);

    public static native int numaGetMode(Numa var0, FloatBuffer var1, IntBuffer var2);

    public static native int numaJoin(Numa var0, Numa var1, int var2, int var3);

    public static native int numaaJoin(Numaa var0, Numaa var1, int var2, int var3);

    public static native Numa numaaFlattenToNuma(Numaa var0);

    public static native Numa numaErode(Numa var0, int var1);

    public static native Numa numaDilate(Numa var0, int var1);

    public static native Numa numaOpen(Numa var0, int var1);

    public static native Numa numaClose(Numa var0, int var1);

    public static native Numa numaTransform(Numa var0, float var1, float var2);

    public static native int numaSimpleStats(Numa var0, int var1, int var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5);

    public static native int numaWindowedStats(Numa var0, int var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5);

    public static native Numa numaWindowedMean(Numa var0, int var1);

    public static native Numa numaWindowedMeanSquare(Numa var0, int var1);

    public static native int numaWindowedVariance(Numa var0, Numa var1, PointerByReference var2, PointerByReference var3);

    public static native Numa numaWindowedMedian(Numa var0, int var1);

    public static native Numa numaConvertToInt(Numa var0);

    public static native Numa numaMakeHistogram(Numa var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native Numa numaMakeHistogramAuto(Numa var0, int var1);

    public static native Numa numaMakeHistogramClipped(Numa var0, float var1, float var2);

    public static native Numa numaRebinHistogram(Numa var0, int var1);

    public static native Numa numaNormalizeHistogram(Numa var0, float var1);

    public static native int numaGetStatsUsingHistogram(Numa var0, int var1, FloatBuffer var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5, FloatBuffer var6, float var7, FloatBuffer var8, PointerByReference var9);

    public static native int numaGetStatsUsingHistogram(Numa var0, int var1, FloatByReference var2, FloatByReference var3, FloatByReference var4, FloatByReference var5, FloatByReference var6, float var7, FloatByReference var8, PointerByReference var9);

    public static native int numaGetHistogramStats(Numa var0, float var1, float var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5, FloatBuffer var6);

    public static native int numaGetHistogramStatsOnInterval(Numa var0, float var1, float var2, int var3, int var4, FloatBuffer var5, FloatBuffer var6, FloatBuffer var7, FloatBuffer var8);

    public static native int numaMakeRankFromHistogram(float var0, float var1, Numa var2, int var3, PointerByReference var4, PointerByReference var5);

    public static native int numaHistogramGetRankFromVal(Numa var0, float var1, FloatBuffer var2);

    public static native int numaHistogramGetValFromRank(Numa var0, float var1, FloatBuffer var2);

    public static native int numaDiscretizeSortedInBins(Numa var0, int var1, PointerByReference var2);

    public static native int numaDiscretizeHistoInBins(Numa var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native int numaGetRankBinValues(Numa var0, int var1, PointerByReference var2);

    public static native Numa numaGetUniformBinSizes(int var0, int var1);

    public static native int numaSplitDistribution(Numa var0, float var1, IntBuffer var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5, FloatBuffer var6, PointerByReference var7);

    public static native int numaSplitDistribution(Numa var0, float var1, IntByReference var2, FloatByReference var3, FloatByReference var4, FloatByReference var5, FloatByReference var6, PointerByReference var7);

    public static native int grayHistogramsToEMD(Numaa var0, Numaa var1, PointerByReference var2);

    public static native int numaEarthMoverDistance(Numa var0, Numa var1, FloatBuffer var2);

    public static native int grayInterHistogramStats(Numaa var0, int var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5);

    public static native Numa numaFindPeaks(Numa var0, int var1, float var2, float var3);

    public static native Numa numaFindExtrema(Numa var0, float var1, PointerByReference var2);

    public static native int numaFindLocForThreshold(Numa var0, int var1, IntBuffer var2, FloatBuffer var3);

    public static native int numaCountReversals(Numa var0, float var1, IntBuffer var2, FloatBuffer var3);

    public static native int numaSelectCrossingThreshold(Numa var0, Numa var1, float var2, FloatBuffer var3);

    public static native Numa numaCrossingsByThreshold(Numa var0, Numa var1, float var2);

    public static native Numa numaCrossingsByPeaks(Numa var0, Numa var1, float var2);

    public static native int numaEvalBestHaarParameters(Numa var0, float var1, int var2, int var3, float var4, float var5, FloatBuffer var6, FloatBuffer var7, FloatBuffer var8);

    public static native int numaEvalHaarSum(Numa var0, float var1, float var2, float var3, FloatBuffer var4);

    public static native Numa genConstrainedNumaInRange(int var0, int var1, int var2, int var3);

    public static native int pixGetRegionsBinary(Pix var0, PointerByReference var1, PointerByReference var2, PointerByReference var3, Pixa var4);

    public static native Pix pixGenHalftoneMask(Pix var0, PointerByReference var1, IntBuffer var2, int var3);

    public static native Pix pixGenHalftoneMask(Pix var0, PointerByReference var1, IntByReference var2, int var3);

    public static native Pix pixGenerateHalftoneMask(Pix var0, PointerByReference var1, IntBuffer var2, Pixa var3);

    public static native Pix pixGenerateHalftoneMask(Pix var0, PointerByReference var1, IntByReference var2, Pixa var3);

    public static native Pix pixGenTextlineMask(Pix var0, PointerByReference var1, IntBuffer var2, Pixa var3);

    public static native Pix pixGenTextlineMask(Pix var0, PointerByReference var1, IntByReference var2, Pixa var3);

    public static native Pix pixGenTextblockMask(Pix var0, Pix var1, Pixa var2);

    public static native Pix pixCropImage(Pix var0, int var1, int var2, int var3, int var4, int var5, float var6, int var7, String var8, PointerByReference var9);

    public static native Pix pixCleanImage(Pix var0, int var1, int var2, int var3, int var4);

    public static native Box pixFindPageForeground(Pix var0, int var1, int var2, int var3, int var4, PixaComp var5);

    public static native int pixSplitIntoCharacters(Pix var0, int var1, int var2, PointerByReference var3, PointerByReference var4, PointerByReference var5);

    public static native Boxa pixSplitComponentWithProfile(Pix var0, int var1, int var2, PointerByReference var3);

    public static native Pixa pixExtractTextlines(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6, Pixa var7);

    public static native Pixa pixExtractRawTextlines(Pix var0, int var1, int var2, int var3, int var4, Pixa var5);

    public static native int pixCountTextColumns(Pix var0, float var1, float var2, float var3, IntBuffer var4, Pixa var5);

    public static native int pixDecideIfText(Pix var0, Box var1, IntBuffer var2, Pixa var3);

    public static native int pixFindThreshFgExtent(Pix var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native int pixDecideIfTable(Pix var0, Box var1, int var2, IntBuffer var3, Pixa var4);

    public static native Pix pixPrepare1bpp(Pix var0, Box var1, float var2, int var3);

    public static native int pixEstimateBackground(Pix var0, int var1, float var2, IntBuffer var3);

    public static native int pixFindLargeRectangles(Pix var0, int var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native int pixFindLargestRectangle(Pix var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Box pixFindRectangleInCC(Pix var0, Box var1, float var2, int var3, int var4, int var5);

    public static native Pix pixAutoPhotoinvert(Pix var0, int var1, PointerByReference var2, Pixa var3);

    public static native int pixSetSelectCmap(Pix var0, Box var1, int var2, int var3, int var4, int var5);

    public static native int pixColorGrayRegionsCmap(Pix var0, Boxa var1, int var2, int var3, int var4, int var5);

    public static native int pixColorGrayCmap(Pix var0, Box var1, int var2, int var3, int var4, int var5);

    public static native int pixColorGrayMaskedCmap(Pix var0, Pix var1, int var2, int var3, int var4, int var5);

    public static native int addColorizedGrayToCmap(PixColormap var0, int var1, int var2, int var3, int var4, PointerByReference var5);

    public static native int pixSetSelectMaskedCmap(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native int pixSetMaskedCmap(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pointer parseForProtos(String var0, String var1);

    public static native int partifyFiles(String var0, String var1, int var2, String var3, String var4);

    public static native int partifyPixac(PixaComp var0, int var1, String var2, Pixa var3);

    public static native Boxa boxaGetWhiteblocks(Boxa var0, Box var1, int var2, int var3, float var4, int var5, float var6, int var7);

    public static native Boxa boxaPruneSortedOnOverlap(Boxa var0, float var1);

    public static native int compressFilesToPdf(Sarray var0, int var1, int var2, float var3, int var4, String var5, String var6);

    public static native int cropFilesToPdf(Sarray var0, int var1, int var2, int var3, int var4, int var5, float var6, int var7, String var8, String var9);

    public static native int cleanTo1bppFilesToPdf(Sarray var0, int var1, int var2, int var3, int var4, String var5, String var6);

    public static native int convertFilesToPdf(String var0, String var1, int var2, float var3, int var4, int var5, String var6, String var7);

    public static native int saConvertFilesToPdf(Sarray var0, int var1, float var2, int var3, int var4, String var5, String var6);

    public static native int saConvertFilesToPdfData(Sarray var0, int var1, float var2, int var3, int var4, String var5, PointerByReference var6, NativeSizeByReference var7);

    public static native int selectDefaultPdfEncoding(Pix var0, IntBuffer var1);

    public static native int convertUnscaledFilesToPdf(String var0, String var1, String var2, String var3);

    public static native int saConvertUnscaledFilesToPdf(Sarray var0, String var1, String var2);

    public static native int saConvertUnscaledFilesToPdfData(Sarray var0, String var1, PointerByReference var2, NativeSizeByReference var3);

    public static native int convertUnscaledToPdfData(String var0, String var1, PointerByReference var2, NativeSizeByReference var3);

    public static native int pixaConvertToPdf(Pixa var0, int var1, float var2, int var3, int var4, String var5, String var6);

    public static native int pixaConvertToPdfData(Pixa var0, int var1, float var2, int var3, int var4, String var5, PointerByReference var6, NativeSizeByReference var7);

    public static native int convertToPdf(String var0, int var1, int var2, String var3, int var4, int var5, int var6, String var7, PointerByReference var8, int var9);

    public static native int convertToPdf(Pointer var0, int var1, int var2, Pointer var3, int var4, int var5, int var6, Pointer var7, PointerByReference var8, int var9);

    public static native int convertImageDataToPdf(ByteBuffer var0, NativeSize var1, int var2, int var3, String var4, int var5, int var6, int var7, String var8, PointerByReference var9, int var10);

    public static native int convertImageDataToPdf(Pointer var0, NativeSize var1, int var2, int var3, Pointer var4, int var5, int var6, int var7, Pointer var8, PointerByReference var9, int var10);

    public static native int convertToPdfData(String var0, int var1, int var2, PointerByReference var3, NativeSizeByReference var4, int var5, int var6, int var7, String var8, PointerByReference var9, int var10);

    public static native int convertToPdfData(Pointer var0, int var1, int var2, PointerByReference var3, NativeSizeByReference var4, int var5, int var6, int var7, Pointer var8, PointerByReference var9, int var10);

    public static native int convertImageDataToPdfData(ByteBuffer var0, NativeSize var1, int var2, int var3, PointerByReference var4, NativeSizeByReference var5, int var6, int var7, int var8, String var9, PointerByReference var10, int var11);

    public static native int convertImageDataToPdfData(Pointer var0, NativeSize var1, int var2, int var3, PointerByReference var4, NativeSizeByReference var5, int var6, int var7, int var8, Pointer var9, PointerByReference var10, int var11);

    public static native int pixConvertToPdf(Pix var0, int var1, int var2, String var3, int var4, int var5, int var6, String var7, PointerByReference var8, int var9);

    public static native int pixConvertToPdf(Pix var0, int var1, int var2, Pointer var3, int var4, int var5, int var6, Pointer var7, PointerByReference var8, int var9);

    public static native int pixWriteStreamPdf(ILeptonica.FILE var0, Pix var1, int var2, String var3);

    public static native int pixWriteMemPdf(PointerByReference var0, NativeSizeByReference var1, Pix var2, int var3, String var4);

    public static native int convertSegmentedFilesToPdf(String var0, String var1, int var2, int var3, int var4, Boxaa var5, int var6, float var7, String var8, String var9);

    public static native Boxaa convertNumberedMasksToBoxaa(String var0, String var1, int var2, int var3);

    public static native int convertToPdfSegmented(String var0, int var1, int var2, int var3, Boxa var4, int var5, float var6, String var7, String var8);

    public static native int pixConvertToPdfSegmented(Pix var0, int var1, int var2, int var3, Boxa var4, int var5, float var6, String var7, String var8);

    public static native int convertToPdfDataSegmented(String var0, int var1, int var2, int var3, Boxa var4, int var5, float var6, String var7, PointerByReference var8, NativeSizeByReference var9);

    public static native int pixConvertToPdfDataSegmented(Pix var0, int var1, int var2, int var3, Boxa var4, int var5, float var6, String var7, PointerByReference var8, NativeSizeByReference var9);

    public static native int concatenatePdf(String var0, String var1, String var2);

    public static native int saConcatenatePdf(Sarray var0, String var1);

    public static native int ptraConcatenatePdf(L_Ptra var0, String var1);

    public static native int concatenatePdfToData(String var0, String var1, PointerByReference var2, NativeSizeByReference var3);

    public static native int saConcatenatePdfToData(Sarray var0, PointerByReference var1, NativeSizeByReference var2);

    public static native int pixConvertToPdfData(Pix var0, int var1, int var2, PointerByReference var3, NativeSizeByReference var4, int var5, int var6, int var7, String var8, PointerByReference var9, int var10);

    public static native int pixConvertToPdfData(Pix var0, int var1, int var2, PointerByReference var3, NativeSizeByReference var4, int var5, int var6, int var7, Pointer var8, PointerByReference var9, int var10);

    public static native int ptraConcatenatePdfToData(L_Ptra var0, Sarray var1, PointerByReference var2, NativeSizeByReference var3);

    public static native int convertTiffMultipageToPdf(String var0, String var1);

    public static native int l_generateCIDataForPdf(String var0, Pix var1, int var2, PointerByReference var3);

    public static native int l_generateCIDataForPdf(Pointer var0, Pix var1, int var2, PointerByReference var3);

    public static native L_Compressed_Data l_generateFlateDataPdf(String var0, Pix var1);

    public static native L_Compressed_Data l_generateJpegData(String var0, int var1);

    public static native L_Compressed_Data l_generateJpegDataMem(ByteBuffer var0, NativeSize var1, int var2);

    public static native L_Compressed_Data l_generateG4Data(String var0, int var1);

    public static native int l_generateCIData(String var0, int var1, int var2, int var3, PointerByReference var4);

    public static native int l_generateCIData(Pointer var0, int var1, int var2, int var3, PointerByReference var4);

    public static native int pixGenerateCIData(Pix var0, int var1, int var2, int var3, PointerByReference var4);

    public static native L_Compressed_Data l_generateFlateData(String var0, int var1);

    public static native int cidConvertToPdfData(L_Compressed_Data var0, String var1, PointerByReference var2, NativeSizeByReference var3);

    public static native void l_CIDataDestroy(PointerByReference var0);

    public static native int getPdfPageCount(String var0, IntBuffer var1);

    public static native int getPdfPageSizes(String var0, PointerByReference var1, PointerByReference var2, IntBuffer var3, IntBuffer var4);

    public static native int getPdfMediaBoxSizes(String var0, PointerByReference var1, PointerByReference var2, IntBuffer var3, IntBuffer var4);

    public static native int getPdfRendererResolution(String var0, String var1, IntBuffer var2);

    public static native void l_pdfSetG4ImageMask(int var0);

    public static native void l_pdfSetDateAndVersion(int var0);

    public static native void setPixMemoryManager(ILeptonica.alloc_fn var0, ILeptonica.dealloc_fn var1);

    public static native Pix pixCreate(int var0, int var1, int var2);

    public static native Pix pixCreateNoInit(int var0, int var1, int var2);

    public static native Pix pixCreateTemplate(Pix var0);

    public static native Pix pixCreateTemplateNoInit(Pix var0);

    public static native Pix pixCreateWithCmap(int var0, int var1, int var2, int var3);

    public static native Pix pixCreateHeader(int var0, int var1, int var2);

    public static native Pix pixClone(Pix var0);

    public static native void pixDestroy(PointerByReference var0);

    public static native Pix pixCopy(Pix var0, Pix var1);

    public static native int pixResizeImageData(Pix var0, Pix var1);

    public static native int pixCopyColormap(Pix var0, Pix var1);

    public static native int pixTransferAllData(Pix var0, PointerByReference var1, int var2, int var3);

    public static native int pixSwapAndDestroy(PointerByReference var0, PointerByReference var1);

    public static native int pixGetWidth(Pix var0);

    public static native int pixSetWidth(Pix var0, int var1);

    public static native int pixGetHeight(Pix var0);

    public static native int pixSetHeight(Pix var0, int var1);

    public static native int pixGetDepth(Pix var0);

    public static native int pixSetDepth(Pix var0, int var1);

    public static native int pixGetDimensions(Pix var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int pixSetDimensions(Pix var0, int var1, int var2, int var3);

    public static native int pixCopyDimensions(Pix var0, Pix var1);

    public static native int pixGetSpp(Pix var0);

    public static native int pixSetSpp(Pix var0, int var1);

    public static native int pixCopySpp(Pix var0, Pix var1);

    public static native int pixGetWpl(Pix var0);

    public static native int pixSetWpl(Pix var0, int var1);

    public static native int pixGetXRes(Pix var0);

    public static native int pixSetXRes(Pix var0, int var1);

    public static native int pixGetYRes(Pix var0);

    public static native int pixSetYRes(Pix var0, int var1);

    public static native int pixGetResolution(Pix var0, IntBuffer var1, IntBuffer var2);

    public static native int pixSetResolution(Pix var0, int var1, int var2);

    public static native int pixCopyResolution(Pix var0, Pix var1);

    public static native int pixScaleResolution(Pix var0, float var1, float var2);

    public static native int pixGetInputFormat(Pix var0);

    public static native int pixSetInputFormat(Pix var0, int var1);

    public static native int pixCopyInputFormat(Pix var0, Pix var1);

    public static native int pixSetSpecial(Pix var0, int var1);

    public static native Pointer pixGetText(Pix var0);

    public static native int pixSetText(Pix var0, String var1);

    public static native int pixAddText(Pix var0, String var1);

    public static native int pixCopyText(Pix var0, Pix var1);

    public static native Pointer pixGetTextCompNew(Pix var0, NativeSizeByReference var1);

    public static native int pixSetTextCompNew(Pix var0, ByteBuffer var1, NativeSize var2);

    public static native PixColormap pixGetColormap(Pix var0);

    public static native int pixSetColormap(Pix var0, PixColormap var1);

    public static native int pixDestroyColormap(Pix var0);

    public static native IntByReference pixGetData(Pix var0);

    public static native int pixFreeAndSetData(Pix var0, IntBuffer var1);

    public static native int pixFreeAndSetData(Pix var0, IntByReference var1);

    public static native int pixSetData(Pix var0, IntBuffer var1);

    public static native int pixSetData(Pix var0, IntByReference var1);

    public static native IntByReference pixExtractData(Pix var0);

    public static native int pixFreeData(Pix var0);

    public static native PointerByReference pixGetLinePtrs(Pix var0, IntBuffer var1);

    public static native int pixSizesEqual(Pix var0, Pix var1);

    public static native int pixMaxAspectRatio(Pix var0, FloatBuffer var1);

    public static native int pixPrintStreamInfo(ILeptonica.FILE var0, Pix var1, String var2);

    public static native int pixGetPixel(Pix var0, int var1, int var2, IntBuffer var3);

    public static native int pixGetPixel(Pix var0, int var1, int var2, IntByReference var3);

    public static native int pixSetPixel(Pix var0, int var1, int var2, int var3);

    public static native int pixGetRGBPixel(Pix var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int pixGetRGBPixel(Pix var0, int var1, int var2, IntByReference var3, IntByReference var4, IntByReference var5);

    public static native int pixSetRGBPixel(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native int pixSetCmapPixel(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native int pixGetRandomPixel(Pix var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int pixClearPixel(Pix var0, int var1, int var2);

    public static native int pixFlipPixel(Pix var0, int var1, int var2);

    public static native void setPixelLow(IntBuffer var0, int var1, int var2, int var3);

    public static native int pixGetBlackOrWhiteVal(Pix var0, int var1, IntBuffer var2);

    public static native int pixClearAll(Pix var0);

    public static native int pixSetAll(Pix var0);

    public static native int pixSetAllGray(Pix var0, int var1);

    public static native int pixSetAllArbitrary(Pix var0, int var1);

    public static native int pixSetBlackOrWhite(Pix var0, int var1);

    public static native int pixSetComponentArbitrary(Pix var0, int var1, int var2);

    public static native int pixClearInRect(Pix var0, Box var1);

    public static native int pixSetInRect(Pix var0, Box var1);

    public static native int pixSetInRectArbitrary(Pix var0, Box var1, int var2);

    public static native int pixBlendInRect(Pix var0, Box var1, int var2, float var3);

    public static native int pixSetPadBits(Pix var0, int var1);

    public static native int pixSetPadBitsBand(Pix var0, int var1, int var2, int var3);

    public static native int pixSetOrClearBorder(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native int pixSetBorderVal(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native int pixSetBorderRingVal(Pix var0, int var1, int var2);

    public static native int pixSetMirroredBorder(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixCopyBorder(Pix var0, Pix var1, int var2, int var3, int var4, int var5);

    public static native Pix pixAddBorder(Pix var0, int var1, int var2);

    public static native Pix pixAddBlackOrWhiteBorder(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixAddBorderGeneral(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixAddMultipleBlackWhiteBorders(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixRemoveBorder(Pix var0, int var1);

    public static native Pix pixRemoveBorderGeneral(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixRemoveBorderToSize(Pix var0, int var1, int var2);

    public static native Pix pixAddMirroredBorder(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixAddRepeatedBorder(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixAddMixedBorder(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixAddContinuedBorder(Pix var0, int var1, int var2, int var3, int var4);

    public static native int pixShiftAndTransferAlpha(Pix var0, Pix var1, float var2, float var3);

    public static native Pix pixDisplayLayersRGBA(Pix var0, int var1, int var2);

    public static native Pix pixCreateRGBImage(Pix var0, Pix var1, Pix var2);

    public static native Pix pixGetRGBComponent(Pix var0, int var1);

    public static native int pixSetRGBComponent(Pix var0, Pix var1, int var2);

    public static native Pix pixGetRGBComponentCmap(Pix var0, int var1);

    public static native int pixCopyRGBComponent(Pix var0, Pix var1, int var2);

    public static native int composeRGBPixel(int var0, int var1, int var2, IntBuffer var3);

    public static native int composeRGBAPixel(int var0, int var1, int var2, int var3, IntBuffer var4);

    public static native void extractRGBValues(int var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native void extractRGBAValues(int var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int extractMinMaxComponent(int var0, int var1);

    public static native int pixGetRGBLine(Pix var0, int var1, ByteBuffer var2, ByteBuffer var3, ByteBuffer var4);

    public static native int setLineDataVal(IntBuffer var0, int var1, int var2, int var3);

    public static native Pix pixEndianByteSwapNew(Pix var0);

    public static native int pixEndianByteSwap(Pix var0);

    public static native int lineEndianByteSwap(IntBuffer var0, IntBuffer var1, int var2);

    public static native Pix pixEndianTwoByteSwapNew(Pix var0);

    public static native int pixEndianTwoByteSwap(Pix var0);

    public static native int pixGetRasterData(Pix var0, PointerByReference var1, NativeSizeByReference var2);

    public static native int pixInferResolution(Pix var0, float var1, IntBuffer var2);

    public static native int pixAlphaIsOpaque(Pix var0, IntBuffer var1);

    public static native PointerByReference pixSetupByteProcessing(Pix var0, IntBuffer var1, IntBuffer var2);

    public static native int pixCleanupByteProcessing(Pix var0, PointerByReference var1);

    public static native void l_setAlphaMaskBorder(float var0, float var1);

    public static native int pixSetMasked(Pix var0, Pix var1, int var2);

    public static native int pixSetMaskedGeneral(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native int pixCombineMasked(Pix var0, Pix var1, Pix var2);

    public static native int pixCombineMaskedGeneral(Pix var0, Pix var1, Pix var2, int var3, int var4);

    public static native int pixPaintThroughMask(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native Pix pixCopyWithBoxa(Pix var0, Boxa var1, int var2);

    public static native int pixPaintSelfThroughMask(Pix var0, Pix var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public static native Pix pixMakeMaskFromVal(Pix var0, int var1);

    public static native Pix pixMakeMaskFromLUT(Pix var0, IntBuffer var1);

    public static native Pix pixMakeArbMaskFromRGB(Pix var0, float var1, float var2, float var3, float var4);

    public static native Pix pixSetUnderTransparency(Pix var0, int var1, int var2);

    public static native Pix pixMakeAlphaFromMask(Pix var0, int var1, PointerByReference var2);

    public static native int pixGetColorNearMaskBoundary(Pix var0, Pix var1, Box var2, int var3, IntBuffer var4, int var5);

    public static native Pix pixDisplaySelectedPixels(Pix var0, Pix var1, Pointer var2, int var3);

    public static native Pix pixInvert(Pix var0, Pix var1);

    public static native Pix pixOr(Pix var0, Pix var1, Pix var2);

    public static native Pix pixAnd(Pix var0, Pix var1, Pix var2);

    public static native Pix pixXor(Pix var0, Pix var1, Pix var2);

    public static native Pix pixSubtract(Pix var0, Pix var1, Pix var2);

    public static native int pixZero(Pix var0, IntBuffer var1);

    public static native int pixForegroundFraction(Pix var0, FloatBuffer var1);

    public static native Numa pixaCountPixels(Pixa var0);

    public static native int pixCountPixels(Pix var0, IntBuffer var1, IntBuffer var2);

    public static native int pixCountPixelsInRect(Pix var0, Box var1, IntBuffer var2, IntBuffer var3);

    public static native Numa pixCountByRow(Pix var0, Box var1);

    public static native Numa pixCountByColumn(Pix var0, Box var1);

    public static native Numa pixCountPixelsByRow(Pix var0, IntBuffer var1);

    public static native Numa pixCountPixelsByColumn(Pix var0);

    public static native int pixCountPixelsInRow(Pix var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native Numa pixGetMomentByColumn(Pix var0, int var1);

    public static native int pixThresholdPixelSum(Pix var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native IntByReference makePixelSumTab8();

    public static native IntByReference makePixelCentroidTab8();

    public static native Numa pixAverageByRow(Pix var0, Box var1, int var2);

    public static native Numa pixAverageByColumn(Pix var0, Box var1, int var2);

    public static native int pixAverageInRect(Pix var0, Pix var1, Box var2, int var3, int var4, int var5, FloatBuffer var6);

    public static native int pixAverageInRectRGB(Pix var0, Pix var1, Box var2, int var3, IntBuffer var4);

    public static native Numa pixVarianceByRow(Pix var0, Box var1);

    public static native Numa pixVarianceByColumn(Pix var0, Box var1);

    public static native int pixVarianceInRect(Pix var0, Box var1, FloatBuffer var2);

    public static native Numa pixAbsDiffByRow(Pix var0, Box var1);

    public static native Numa pixAbsDiffByColumn(Pix var0, Box var1);

    public static native int pixAbsDiffInRect(Pix var0, Box var1, int var2, FloatBuffer var3);

    public static native int pixAbsDiffOnLine(Pix var0, int var1, int var2, int var3, int var4, FloatBuffer var5);

    public static native int pixCountArbInRect(Pix var0, Box var1, int var2, int var3, IntBuffer var4);

    public static native Pix pixMirroredTiling(Pix var0, int var1, int var2);

    public static native int pixFindRepCloseTile(Pix var0, Box var1, int var2, int var3, int var4, int var5, PointerByReference var6, int var7);

    public static native Numa pixGetGrayHistogram(Pix var0, int var1);

    public static native Numa pixGetGrayHistogramMasked(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native Numa pixGetGrayHistogramInRect(Pix var0, Box var1, int var2);

    public static native Numaa pixGetGrayHistogramTiled(Pix var0, int var1, int var2, int var3);

    public static native int pixGetColorHistogram(Pix var0, int var1, PointerByReference var2, PointerByReference var3, PointerByReference var4);

    public static native int pixGetColorHistogramMasked(Pix var0, Pix var1, int var2, int var3, int var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native Numa pixGetCmapHistogram(Pix var0, int var1);

    public static native Numa pixGetCmapHistogramMasked(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native Numa pixGetCmapHistogramInRect(Pix var0, Box var1, int var2);

    public static native int pixCountRGBColorsByHash(Pix var0, IntBuffer var1);

    public static native int pixCountRGBColors(Pix var0, int var1, IntBuffer var2);

    public static native L_Rbtree pixGetColorAmapHistogram(Pix var0, int var1);

    public static native int amapGetCountForColor(L_Rbtree var0, int var1);

    public static native int pixGetRankValue(Pix var0, int var1, float var2, IntBuffer var3);

    public static native int pixGetRankValueMaskedRGB(Pix var0, Pix var1, int var2, int var3, int var4, float var5, FloatBuffer var6, FloatBuffer var7, FloatBuffer var8);

    public static native int pixGetRankValueMasked(Pix var0, Pix var1, int var2, int var3, int var4, float var5, FloatBuffer var6, PointerByReference var7);

    public static native int pixGetRankValueMasked(Pix var0, Pix var1, int var2, int var3, int var4, float var5, FloatByReference var6, PointerByReference var7);

    public static native int pixGetPixelAverage(Pix var0, Pix var1, int var2, int var3, int var4, IntBuffer var5);

    public static native int pixGetPixelStats(Pix var0, int var1, int var2, IntBuffer var3);

    public static native int pixGetAverageMaskedRGB(Pix var0, Pix var1, int var2, int var3, int var4, int var5, FloatBuffer var6, FloatBuffer var7, FloatBuffer var8);

    public static native int pixGetAverageMasked(Pix var0, Pix var1, int var2, int var3, int var4, int var5, FloatBuffer var6);

    public static native int pixGetAverageTiledRGB(Pix var0, int var1, int var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6);

    public static native Pix pixGetAverageTiled(Pix var0, int var1, int var2, int var3);

    public static native int pixRowStats(Pix var0, Box var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native int pixColumnStats(Pix var0, Box var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native int pixGetRangeValues(Pix var0, int var1, int var2, IntBuffer var3, IntBuffer var4);

    public static native int pixGetExtremeValue(Pix var0, int var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int pixGetMaxValueInRect(Pix var0, Box var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixGetMaxColorIndex(Pix var0, IntBuffer var1);

    public static native int pixGetBinnedComponentRange(Pix var0, int var1, int var2, int var3, IntBuffer var4, IntBuffer var5, PointerByReference var6, int var7);

    public static native int pixGetRankColorArray(Pix var0, int var1, int var2, int var3, PointerByReference var4, Pixa var5, int var6);

    public static native int pixGetBinnedColor(Pix var0, Pix var1, int var2, int var3, PointerByReference var4, Pixa var5);

    public static native Pix pixDisplayColorArray(IntBuffer var0, int var1, int var2, int var3, int var4);

    public static native Pix pixRankBinByStrip(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixaGetAlignedStats(Pixa var0, int var1, int var2, int var3);

    public static native int pixaExtractColumnFromEachPix(Pixa var0, int var1, Pix var2);

    public static native int pixGetRowStats(Pix var0, int var1, int var2, int var3, FloatBuffer var4);

    public static native int pixGetColumnStats(Pix var0, int var1, int var2, int var3, FloatBuffer var4);

    public static native int pixSetPixelColumn(Pix var0, int var1, FloatBuffer var2);

    public static native int pixThresholdForFgBg(Pix var0, int var1, int var2, IntBuffer var3, IntBuffer var4);

    public static native int pixSplitDistributionFgBg(Pix var0, float var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, PointerByReference var6);

    public static native int pixSplitDistributionFgBg(Pix var0, float var1, int var2, IntByReference var3, IntByReference var4, IntByReference var5, PointerByReference var6);

    public static native int pixaFindDimensions(Pixa var0, PointerByReference var1, PointerByReference var2);

    public static native int pixFindAreaPerimRatio(Pix var0, IntBuffer var1, FloatBuffer var2);

    public static native Numa pixaFindPerimToAreaRatio(Pixa var0);

    public static native int pixFindPerimToAreaRatio(Pix var0, IntBuffer var1, FloatBuffer var2);

    public static native Numa pixaFindPerimSizeRatio(Pixa var0);

    public static native int pixFindPerimSizeRatio(Pix var0, IntBuffer var1, FloatBuffer var2);

    public static native Numa pixaFindAreaFraction(Pixa var0);

    public static native int pixFindAreaFraction(Pix var0, IntBuffer var1, FloatBuffer var2);

    public static native Numa pixaFindAreaFractionMasked(Pixa var0, Pix var1, int var2);

    public static native int pixFindAreaFractionMasked(Pix var0, Box var1, Pix var2, IntBuffer var3, FloatBuffer var4);

    public static native Numa pixaFindWidthHeightRatio(Pixa var0);

    public static native Numa pixaFindWidthHeightProduct(Pixa var0);

    public static native int pixFindOverlapFraction(Pix var0, Pix var1, int var2, int var3, IntBuffer var4, FloatBuffer var5, IntBuffer var6);

    public static native Boxa pixFindRectangleComps(Pix var0, int var1, int var2, int var3);

    public static native int pixConformsToRectangle(Pix var0, Box var1, int var2, IntBuffer var3);

    public static native Pix pixExtractRectangularRegions(Pix var0, Boxa var1);

    public static native Pixa pixClipRectangles(Pix var0, Boxa var1);

    public static native Pix pixClipRectangle(Pix var0, Box var1, PointerByReference var2);

    public static native Pix pixClipRectangleWithBorder(Pix var0, Box var1, int var2, PointerByReference var3);

    public static native Pix pixClipMasked(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native int pixCropToMatch(Pix var0, Pix var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixCropToSize(Pix var0, int var1, int var2);

    public static native Pix pixResizeToMatch(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixSelectComponentBySize(Pix var0, int var1, int var2, int var3, PointerByReference var4);

    public static native Pix pixFilterComponentBySize(Pix var0, int var1, int var2, int var3, PointerByReference var4);

    public static native Pix pixMakeSymmetricMask(int var0, int var1, float var2, float var3, int var4);

    public static native Pix pixMakeFrameMask(int var0, int var1, float var2, float var3, float var4, float var5);

    public static native Pix pixMakeCoveringOfRectangles(Pix var0, int var1);

    public static native int pixFractionFgInMask(Pix var0, Pix var1, FloatBuffer var2);

    public static native int pixClipToForeground(Pix var0, PointerByReference var1, PointerByReference var2);

    public static native int pixTestClipToForeground(Pix var0, IntBuffer var1);

    public static native int pixClipBoxToForeground(Pix var0, Box var1, PointerByReference var2, PointerByReference var3);

    public static native int pixScanForForeground(Pix var0, Box var1, int var2, IntBuffer var3);

    public static native int pixClipBoxToEdges(Pix var0, Box var1, int var2, int var3, int var4, int var5, PointerByReference var6, PointerByReference var7);

    public static native int pixScanForEdge(Pix var0, Box var1, int var2, int var3, int var4, int var5, int var6, IntBuffer var7);

    public static native Numa pixExtractOnLine(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native float pixAverageOnLine(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Numa pixAverageIntensityProfile(Pix var0, float var1, int var2, int var3, int var4, int var5, int var6);

    public static native Numa pixReversalProfile(Pix var0, float var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native int pixWindowedVarianceOnLine(Pix var0, int var1, int var2, int var3, int var4, int var5, PointerByReference var6);

    public static native int pixMinMaxNearLine(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6, PointerByReference var7, PointerByReference var8, FloatBuffer var9, FloatBuffer var10);

    public static native int pixMinMaxNearLine(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6, PointerByReference var7, PointerByReference var8, FloatByReference var9, FloatByReference var10);

    public static native Pix pixRankRowTransform(Pix var0);

    public static native Pix pixRankColumnTransform(Pix var0);

    public static native Pixa pixaCreate(int var0);

    public static native Pixa pixaCreateFromPix(Pix var0, int var1, int var2, int var3);

    public static native Pixa pixaCreateFromBoxa(Pix var0, Boxa var1, int var2, int var3, IntBuffer var4);

    public static native Pixa pixaSplitPix(Pix var0, int var1, int var2, int var3, int var4);

    public static native void pixaDestroy(PointerByReference var0);

    public static native Pixa pixaCopy(Pixa var0, int var1);

    public static native int pixaAddPix(Pixa var0, Pix var1, int var2);

    public static native int pixaAddBox(Pixa var0, Box var1, int var2);

    public static native int pixaExtendArrayToSize(Pixa var0, NativeSize var1);

    public static native int pixaGetCount(Pixa var0);

    public static native Pix pixaGetPix(Pixa var0, int var1, int var2);

    public static native int pixaGetPixDimensions(Pixa var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native Boxa pixaGetBoxa(Pixa var0, int var1);

    public static native int pixaGetBoxaCount(Pixa var0);

    public static native Box pixaGetBox(Pixa var0, int var1, int var2);

    public static native int pixaGetBoxGeometry(Pixa var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int pixaSetBoxa(Pixa var0, Boxa var1, int var2);

    public static native PointerByReference pixaGetPixArray(Pixa var0);

    public static native int pixaVerifyDepth(Pixa var0, IntBuffer var1, IntBuffer var2);

    public static native int pixaVerifyDimensions(Pixa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int pixaIsFull(Pixa var0, IntBuffer var1, IntBuffer var2);

    public static native int pixaCountText(Pixa var0, IntBuffer var1);

    public static native int pixaSetText(Pixa var0, String var1, Sarray var2);

    public static native PointerByReference pixaGetLinePtrs(Pixa var0, IntBuffer var1);

    public static native int pixaWriteStreamInfo(ILeptonica.FILE var0, Pixa var1);

    public static native int pixaReplacePix(Pixa var0, int var1, Pix var2, Box var3);

    public static native int pixaInsertPix(Pixa var0, int var1, Pix var2, Box var3);

    public static native int pixaRemovePix(Pixa var0, int var1);

    public static native int pixaRemovePixAndSave(Pixa var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native int pixaRemoveSelected(Pixa var0, Numa var1);

    public static native int pixaInitFull(Pixa var0, Pix var1, Box var2);

    public static native int pixaClear(Pixa var0);

    public static native int pixaJoin(Pixa var0, Pixa var1, int var2, int var3);

    public static native Pixa pixaInterleave(Pixa var0, Pixa var1, int var2);

    public static native int pixaaJoin(Pixaa var0, Pixaa var1, int var2, int var3);

    public static native Pixaa pixaaCreate(int var0);

    public static native Pixaa pixaaCreateFromPixa(Pixa var0, int var1, int var2, int var3);

    public static native void pixaaDestroy(PointerByReference var0);

    public static native int pixaaAddPixa(Pixaa var0, Pixa var1, int var2);

    public static native int pixaaAddPix(Pixaa var0, int var1, Pix var2, Box var3, int var4);

    public static native int pixaaAddBox(Pixaa var0, Box var1, int var2);

    public static native int pixaaGetCount(Pixaa var0, PointerByReference var1);

    public static native Pixa pixaaGetPixa(Pixaa var0, int var1, int var2);

    public static native Boxa pixaaGetBoxa(Pixaa var0, int var1);

    public static native Pix pixaaGetPix(Pixaa var0, int var1, int var2, int var3);

    public static native int pixaaVerifyDepth(Pixaa var0, IntBuffer var1, IntBuffer var2);

    public static native int pixaaVerifyDimensions(Pixaa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int pixaaIsFull(Pixaa var0, IntBuffer var1);

    public static native int pixaaInitFull(Pixaa var0, Pixa var1);

    public static native int pixaaReplacePixa(Pixaa var0, int var1, Pixa var2);

    public static native int pixaaClear(Pixaa var0);

    public static native int pixaaTruncate(Pixaa var0);

    public static native Pixa pixaRead(String var0);

    public static native Pixa pixaReadStream(ILeptonica.FILE var0);

    public static native Pixa pixaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int pixaWriteDebug(String var0, Pixa var1);

    public static native int pixaWrite(String var0, Pixa var1);

    public static native int pixaWriteStream(ILeptonica.FILE var0, Pixa var1);

    public static native int pixaWriteMem(PointerByReference var0, NativeSizeByReference var1, Pixa var2);

    public static native Pixa pixaReadBoth(String var0);

    public static native Pixaa pixaaReadFromFiles(String var0, String var1, int var2, int var3);

    public static native Pixaa pixaaRead(String var0);

    public static native Pixaa pixaaReadStream(ILeptonica.FILE var0);

    public static native Pixaa pixaaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int pixaaWrite(String var0, Pixaa var1);

    public static native int pixaaWriteStream(ILeptonica.FILE var0, Pixaa var1);

    public static native int pixaaWriteMem(PointerByReference var0, NativeSizeByReference var1, Pixaa var2);

    public static native Pixacc pixaccCreate(int var0, int var1, int var2);

    public static native Pixacc pixaccCreateFromPix(Pix var0, int var1);

    public static native void pixaccDestroy(PointerByReference var0);

    public static native Pix pixaccFinal(Pixacc var0, int var1);

    public static native Pix pixaccGetPix(Pixacc var0);

    public static native int pixaccGetOffset(Pixacc var0);

    public static native int pixaccAdd(Pixacc var0, Pix var1);

    public static native int pixaccSubtract(Pixacc var0, Pix var1);

    public static native int pixaccMultConst(Pixacc var0, float var1);

    public static native int pixaccMultConstAccumulate(Pixacc var0, Pix var1, float var2);

    public static native Pix pixSelectBySize(Pix var0, int var1, int var2, int var3, int var4, int var5, IntBuffer var6);

    public static native Pixa pixaSelectBySize(Pixa var0, int var1, int var2, int var3, int var4, IntBuffer var5);

    public static native Numa pixaMakeSizeIndicator(Pixa var0, int var1, int var2, int var3, int var4);

    public static native Pix pixSelectByPerimToAreaRatio(Pix var0, float var1, int var2, int var3, IntBuffer var4);

    public static native Pixa pixaSelectByPerimToAreaRatio(Pixa var0, float var1, int var2, IntBuffer var3);

    public static native Pix pixSelectByPerimSizeRatio(Pix var0, float var1, int var2, int var3, IntBuffer var4);

    public static native Pixa pixaSelectByPerimSizeRatio(Pixa var0, float var1, int var2, IntBuffer var3);

    public static native Pix pixSelectByAreaFraction(Pix var0, float var1, int var2, int var3, IntBuffer var4);

    public static native Pixa pixaSelectByAreaFraction(Pixa var0, float var1, int var2, IntBuffer var3);

    public static native Pix pixSelectByArea(Pix var0, float var1, int var2, int var3, IntBuffer var4);

    public static native Pixa pixaSelectByArea(Pixa var0, float var1, int var2, IntBuffer var3);

    public static native Pix pixSelectByWidthHeightRatio(Pix var0, float var1, int var2, int var3, IntBuffer var4);

    public static native Pixa pixaSelectByWidthHeightRatio(Pixa var0, float var1, int var2, IntBuffer var3);

    public static native Pixa pixaSelectByNumConnComp(Pixa var0, int var1, int var2, int var3, IntBuffer var4);

    public static native Pixa pixaSelectWithIndicator(Pixa var0, Numa var1, IntBuffer var2);

    public static native int pixRemoveWithIndicator(Pix var0, Pixa var1, Numa var2);

    public static native int pixAddWithIndicator(Pix var0, Pixa var1, Numa var2);

    public static native Pixa pixaSelectWithString(Pixa var0, String var1, IntBuffer var2);

    public static native Pix pixaRenderComponent(Pix var0, Pixa var1, int var2);

    public static native Pixa pixaSort(Pixa var0, int var1, int var2, PointerByReference var3, int var4);

    public static native Pixa pixaBinSort(Pixa var0, int var1, int var2, PointerByReference var3, int var4);

    public static native Pixa pixaSortByIndex(Pixa var0, Numa var1, int var2);

    public static native Pixaa pixaSort2dByIndex(Pixa var0, Numaa var1, int var2);

    public static native Pixa pixaSelectRange(Pixa var0, int var1, int var2, int var3);

    public static native Pixaa pixaaSelectRange(Pixaa var0, int var1, int var2, int var3);

    public static native Pixaa pixaaScaleToSize(Pixaa var0, int var1, int var2);

    public static native Pixaa pixaaScaleToSizeVar(Pixaa var0, Numa var1, Numa var2);

    public static native Pixa pixaScaleToSize(Pixa var0, int var1, int var2);

    public static native Pixa pixaScaleToSizeRel(Pixa var0, int var1, int var2);

    public static native Pixa pixaScale(Pixa var0, float var1, float var2);

    public static native Pixa pixaScaleBySampling(Pixa var0, float var1, float var2);

    public static native Pixa pixaRotate(Pixa var0, float var1, int var2, int var3, int var4, int var5);

    public static native Pixa pixaRotateOrth(Pixa var0, int var1);

    public static native Pixa pixaTranslate(Pixa var0, int var1, int var2, int var3);

    public static native Pixa pixaAddBorderGeneral(Pixa var0, Pixa var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pixa pixaaFlattenToPixa(Pixaa var0, PointerByReference var1, int var2);

    public static native int pixaaSizeRange(Pixaa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixaSizeRange(Pixa var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native Pixa pixaClipToPix(Pixa var0, Pix var1);

    public static native int pixaClipToForeground(Pixa var0, PointerByReference var1, PointerByReference var2);

    public static native int pixaGetRenderingDepth(Pixa var0, IntBuffer var1);

    public static native int pixaHasColor(Pixa var0, IntBuffer var1);

    public static native int pixaAnyColormaps(Pixa var0, IntBuffer var1);

    public static native int pixaGetDepthInfo(Pixa var0, IntBuffer var1, IntBuffer var2);

    public static native Pixa pixaConvertToSameDepth(Pixa var0);

    public static native Pixa pixaConvertToGivenDepth(Pixa var0, int var1);

    public static native int pixaEqual(Pixa var0, Pixa var1, int var2, PointerByReference var3, IntBuffer var4);

    public static native int pixaEqual(Pixa var0, Pixa var1, int var2, PointerByReference var3, IntByReference var4);

    public static native int pixaSetFullSizeBoxa(Pixa var0);

    public static native Pix pixaDisplay(Pixa var0, int var1, int var2);

    public static native Pix pixaDisplayRandomCmap(Pixa var0, int var1, int var2);

    public static native Pix pixaDisplayLinearly(Pixa var0, int var1, float var2, int var3, int var4, int var5, PointerByReference var6);

    public static native Pix pixaDisplayOnLattice(Pixa var0, int var1, int var2, IntBuffer var3, PointerByReference var4);

    public static native Pix pixaDisplayOnLattice(Pixa var0, int var1, int var2, IntByReference var3, PointerByReference var4);

    public static native Pix pixaDisplayUnsplit(Pixa var0, int var1, int var2, int var3, int var4);

    public static native Pix pixaDisplayTiled(Pixa var0, int var1, int var2, int var3);

    public static native Pix pixaDisplayTiledInRows(Pixa var0, int var1, int var2, float var3, int var4, int var5, int var6);

    public static native Pix pixaDisplayTiledInColumns(Pixa var0, int var1, float var2, int var3, int var4);

    public static native Pix pixaDisplayTiledAndScaled(Pixa var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixaDisplayTiledWithText(Pixa var0, int var1, float var2, int var3, int var4, int var5, int var6);

    public static native Pix pixaDisplayTiledByIndex(Pixa var0, Numa var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixaDisplayPairTiledInColumns(Pixa var0, Pixa var1, int var2, float var3, int var4, int var5, int var6, int var7, int var8, int var9, Sarray var10);

    public static native Pix pixaaDisplay(Pixaa var0, int var1, int var2);

    public static native Pix pixaaDisplayByPixa(Pixaa var0, int var1, float var2, int var3, int var4, int var5);

    public static native Pixa pixaaDisplayTiledAndScaled(Pixaa var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pixa pixaConvertTo1(Pixa var0, int var1);

    public static native Pixa pixaConvertTo8(Pixa var0, int var1);

    public static native Pixa pixaConvertTo8Colormap(Pixa var0, int var1);

    public static native Pixa pixaConvertTo32(Pixa var0);

    public static native Pixa pixaConstrainedSelect(Pixa var0, int var1, int var2, int var3, int var4, int var5);

    public static native int pixaSelectToPdf(Pixa var0, int var1, int var2, int var3, float var4, int var5, int var6, int var7, int var8, String var9);

    public static native Pixa pixaMakeFromTiledPixa(Pixa var0, int var1, int var2, int var3);

    public static native Pixa pixaMakeFromTiledPix(Pix var0, int var1, int var2, int var3, int var4, Boxa var5);

    public static native int pixGetTileCount(Pix var0, IntBuffer var1);

    public static native Pixa pixaDisplayMultiTiled(Pixa var0, int var1, int var2, int var3, int var4, float var5, int var6, int var7);

    public static native int pixaSplitIntoFiles(Pixa var0, int var1, float var2, int var3, int var4, int var5, int var6);

    public static native int convertToNUpFiles(String var0, String var1, int var2, int var3, int var4, int var5, int var6, int var7, String var8);

    public static native Pixa convertToNUpPixa(String var0, String var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native Pixa pixaConvertToNUpPixa(Pixa var0, Sarray var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native int pixaCompareInPdf(Pixa var0, Pixa var1, int var2, int var3, int var4, int var5, int var6, int var7, String var8);

    public static native int pmsCreate(NativeSize var0, NativeSize var1, Numa var2, String var3);

    public static native void pmsDestroy();

    public static native Pointer pmsCustomAlloc(NativeSize var0);

    public static native void pmsCustomDealloc(Pointer var0);

    public static native Pointer pmsGetAlloc(NativeSize var0);

    public static native int pmsGetLevelForAlloc(NativeSize var0, IntBuffer var1);

    public static native int pmsGetLevelForDealloc(Pointer var0, IntBuffer var1);

    public static native void pmsLogInfo();

    public static native int pixAddConstantGray(Pix var0, int var1);

    public static native int pixMultConstantGray(Pix var0, float var1);

    public static native Pix pixAddGray(Pix var0, Pix var1, Pix var2);

    public static native Pix pixSubtractGray(Pix var0, Pix var1, Pix var2);

    public static native Pix pixMultiplyGray(Pix var0, Pix var1, float var2);

    public static native Pix pixThresholdToValue(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixInitAccumulate(int var0, int var1, int var2);

    public static native Pix pixFinalAccumulate(Pix var0, int var1, int var2);

    public static native Pix pixFinalAccumulateThreshold(Pix var0, int var1, int var2);

    public static native int pixAccumulate(Pix var0, Pix var1, int var2);

    public static native int pixMultConstAccumulate(Pix var0, float var1, int var2);

    public static native Pix pixAbsDifference(Pix var0, Pix var1);

    public static native Pix pixAddRGB(Pix var0, Pix var1);

    public static native Pix pixMinOrMax(Pix var0, Pix var1, Pix var2, int var3);

    public static native Pix pixMaxDynamicRange(Pix var0, int var1);

    public static native Pix pixMaxDynamicRangeRGB(Pix var0, int var1);

    public static native int linearScaleRGBVal(int var0, float var1);

    public static native int logScaleRGBVal(int var0, FloatBuffer var1, float var2);

    public static native FloatByReference makeLogBase2Tab();

    public static native float getLogBase2(int var0, FloatBuffer var1);

    public static native PixComp pixcompCreateFromPix(Pix var0, int var1);

    public static native PixComp pixcompCreateFromString(ByteBuffer var0, NativeSize var1, int var2);

    public static native PixComp pixcompCreateFromFile(String var0, int var1);

    public static native void pixcompDestroy(PointerByReference var0);

    public static native PixComp pixcompCopy(PixComp var0);

    public static native int pixcompGetDimensions(PixComp var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int pixcompGetParameters(PixComp var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixcompDetermineFormat(int var0, int var1, int var2, IntBuffer var3);

    public static native Pix pixCreateFromPixcomp(PixComp var0);

    public static native PixaComp pixacompCreate(int var0);

    public static native PixaComp pixacompCreateWithInit(int var0, int var1, Pix var2, int var3);

    public static native PixaComp pixacompCreateFromPixa(Pixa var0, int var1, int var2);

    public static native PixaComp pixacompCreateFromFiles(String var0, String var1, int var2);

    public static native PixaComp pixacompCreateFromSA(Sarray var0, int var1);

    public static native void pixacompDestroy(PointerByReference var0);

    public static native int pixacompAddPix(PixaComp var0, Pix var1, int var2);

    public static native int pixacompAddPixcomp(PixaComp var0, PixComp var1, int var2);

    public static native int pixacompReplacePix(PixaComp var0, int var1, Pix var2, int var3);

    public static native int pixacompReplacePixcomp(PixaComp var0, int var1, PixComp var2);

    public static native int pixacompAddBox(PixaComp var0, Box var1, int var2);

    public static native int pixacompGetCount(PixaComp var0);

    public static native PixComp pixacompGetPixcomp(PixaComp var0, int var1, int var2);

    public static native Pix pixacompGetPix(PixaComp var0, int var1);

    public static native int pixacompGetPixDimensions(PixaComp var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native Boxa pixacompGetBoxa(PixaComp var0, int var1);

    public static native int pixacompGetBoxaCount(PixaComp var0);

    public static native Box pixacompGetBox(PixaComp var0, int var1, int var2);

    public static native int pixacompGetBoxGeometry(PixaComp var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int pixacompGetOffset(PixaComp var0);

    public static native int pixacompSetOffset(PixaComp var0, int var1);

    public static native Pixa pixaCreateFromPixacomp(PixaComp var0, int var1);

    public static native int pixacompJoin(PixaComp var0, PixaComp var1, int var2, int var3);

    public static native PixaComp pixacompInterleave(PixaComp var0, PixaComp var1);

    public static native PixaComp pixacompRead(String var0);

    public static native PixaComp pixacompReadStream(ILeptonica.FILE var0);

    public static native PixaComp pixacompReadMem(ByteBuffer var0, NativeSize var1);

    public static native int pixacompWrite(String var0, PixaComp var1);

    public static native int pixacompWriteStream(ILeptonica.FILE var0, PixaComp var1);

    public static native int pixacompWriteMem(PointerByReference var0, NativeSizeByReference var1, PixaComp var2);

    public static native int pixacompConvertToPdf(PixaComp var0, int var1, float var2, int var3, int var4, String var5, String var6);

    public static native int pixacompConvertToPdfData(PixaComp var0, int var1, float var2, int var3, int var4, String var5, PointerByReference var6, NativeSizeByReference var7);

    public static native int pixacompFastConvertToPdfData(PixaComp var0, String var1, PointerByReference var2, NativeSizeByReference var3);

    public static native int pixacompWriteStreamInfo(ILeptonica.FILE var0, PixaComp var1, String var2);

    public static native int pixcompWriteStreamInfo(ILeptonica.FILE var0, PixComp var1, String var2);

    public static native Pix pixacompDisplayTiledAndScaled(PixaComp var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native int pixacompWriteFiles(PixaComp var0, String var1);

    public static native int pixcompWriteFile(String var0, PixComp var1);

    public static native Pix pixThreshold8(Pix var0, int var1, int var2, int var3);

    public static native Pix pixRemoveColormapGeneral(Pix var0, int var1, int var2);

    public static native Pix pixRemoveColormap(Pix var0, int var1);

    public static native int pixAddGrayColormap8(Pix var0);

    public static native Pix pixAddMinimalGrayColormap8(Pix var0);

    public static native Pix pixConvertRGBToLuminance(Pix var0);

    public static native Pix pixConvertRGBToGrayGeneral(Pix var0, int var1, float var2, float var3, float var4);

    public static native Pix pixConvertRGBToGray(Pix var0, float var1, float var2, float var3);

    public static native Pix pixConvertRGBToGrayFast(Pix var0);

    public static native Pix pixConvertRGBToGrayMinMax(Pix var0, int var1);

    public static native Pix pixConvertRGBToGraySatBoost(Pix var0, int var1);

    public static native Pix pixConvertRGBToGrayArb(Pix var0, float var1, float var2, float var3);

    public static native Pix pixConvertRGBToBinaryArb(Pix var0, float var1, float var2, float var3, int var4, int var5);

    public static native Pix pixConvertGrayToColormap(Pix var0);

    public static native Pix pixConvertGrayToColormap8(Pix var0, int var1);

    public static native Pix pixColorizeGray(Pix var0, int var1, int var2);

    public static native Pix pixConvertRGBToColormap(Pix var0, int var1);

    public static native Pix pixConvertCmapTo1(Pix var0);

    public static native int pixQuantizeIfFewColors(Pix var0, int var1, int var2, int var3, PointerByReference var4);

    public static native Pix pixConvert16To8(Pix var0, int var1);

    public static native Pix pixConvertGrayToFalseColor(Pix var0, float var1);

    public static native Pix pixUnpackBinary(Pix var0, int var1, int var2);

    public static native Pix pixConvert1To16(Pix var0, Pix var1, short var2, short var3);

    public static native Pix pixConvert1To32(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixConvert1To2Cmap(Pix var0);

    public static native Pix pixConvert1To2(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixConvert1To4Cmap(Pix var0);

    public static native Pix pixConvert1To4(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixConvert1To8Cmap(Pix var0);

    public static native Pix pixConvert1To8(Pix var0, Pix var1, byte var2, byte var3);

    public static native Pix pixConvert2To8(Pix var0, byte var1, byte var2, byte var3, byte var4, int var5);

    public static native Pix pixConvert4To8(Pix var0, int var1);

    public static native Pix pixConvert8To16(Pix var0, int var1);

    public static native Pix pixConvertTo2(Pix var0);

    public static native Pix pixConvert8To2(Pix var0);

    public static native Pix pixConvertTo4(Pix var0);

    public static native Pix pixConvert8To4(Pix var0);

    public static native Pix pixConvertTo1Adaptive(Pix var0);

    public static native Pix pixConvertTo1(Pix var0, int var1);

    public static native Pix pixConvertTo1BySampling(Pix var0, int var1, int var2);

    public static native Pix pixConvertTo8(Pix var0, int var1);

    public static native Pix pixConvertTo8BySampling(Pix var0, int var1, int var2);

    public static native Pix pixConvertTo8Colormap(Pix var0, int var1);

    public static native Pix pixConvertTo16(Pix var0);

    public static native Pix pixConvertTo32(Pix var0);

    public static native Pix pixConvertTo32BySampling(Pix var0, int var1);

    public static native Pix pixConvert8To32(Pix var0);

    public static native Pix pixConvertTo8Or32(Pix var0, int var1, int var2);

    public static native Pix pixConvert24To32(Pix var0);

    public static native Pix pixConvert32To24(Pix var0);

    public static native Pix pixConvert32To16(Pix var0, int var1);

    public static native Pix pixConvert32To8(Pix var0, int var1, int var2);

    public static native Pix pixRemoveAlpha(Pix var0);

    public static native Pix pixAddAlphaTo1bpp(Pix var0, Pix var1);

    public static native Pix pixConvertLossless(Pix var0, int var1);

    public static native Pix pixConvertForPSWrap(Pix var0);

    public static native Pix pixConvertToSubpixelRGB(Pix var0, float var1, float var2, int var3);

    public static native Pix pixConvertGrayToSubpixelRGB(Pix var0, float var1, float var2, int var3);

    public static native Pix pixConvertColorToSubpixelRGB(Pix var0, float var1, float var2, int var3);

    public static native void l_setNeutralBoostVal(int var0);

    public static native Pix pixConnCompTransform(Pix var0, int var1, int var2);

    public static native Pix pixConnCompAreaTransform(Pix var0, int var1);

    public static native int pixConnCompIncrInit(Pix var0, int var1, PointerByReference var2, PointerByReference var3, IntBuffer var4);

    public static native int pixConnCompIncrInit(Pix var0, int var1, PointerByReference var2, PointerByReference var3, IntByReference var4);

    public static native int pixConnCompIncrAdd(Pix var0, Ptaa var1, IntBuffer var2, float var3, float var4, int var5);

    public static native int pixGetSortedNeighborValues(Pix var0, int var1, int var2, int var3, PointerByReference var4, IntBuffer var5);

    public static native Pix pixLocToColorTransform(Pix var0);

    public static native PixTiling pixTilingCreate(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native void pixTilingDestroy(PointerByReference var0);

    public static native int pixTilingGetCount(PixTiling var0, IntBuffer var1, IntBuffer var2);

    public static native int pixTilingGetSize(PixTiling var0, IntBuffer var1, IntBuffer var2);

    public static native Pix pixTilingGetTile(PixTiling var0, int var1, int var2);

    public static native int pixTilingNoStripOnPaint(PixTiling var0);

    public static native int pixTilingPaintTile(Pix var0, int var1, int var2, Pix var3, PixTiling var4);

    public static native Pix pixReadStreamPng(ILeptonica.FILE var0);

    public static native int readHeaderPng(String var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int freadHeaderPng(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int readHeaderMemPng(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int fgetPngResolution(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2);

    public static native int isPngInterlaced(String var0, IntBuffer var1);

    public static native int fgetPngColormapInfo(ILeptonica.FILE var0, PointerByReference var1, IntBuffer var2);

    public static native int fgetPngColormapInfo(ILeptonica.FILE var0, PointerByReference var1, IntByReference var2);

    public static native int pixWritePng(String var0, Pix var1, float var2);

    public static native int pixWriteStreamPng(ILeptonica.FILE var0, Pix var1, float var2);

    public static native int pixSetZlibCompression(Pix var0, int var1);

    public static native void l_pngSetReadStrip16To8(int var0);

    public static native Pix pixReadMemPng(ByteBuffer var0, NativeSize var1);

    public static native int pixWriteMemPng(PointerByReference var0, NativeSizeByReference var1, Pix var2, float var3);

    public static native Pix pixReadStreamPnm(ILeptonica.FILE var0);

    public static native int readHeaderPnm(String var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int freadHeaderPnm(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int pixWriteStreamPnm(ILeptonica.FILE var0, Pix var1);

    public static native int pixWriteStreamAsciiPnm(ILeptonica.FILE var0, Pix var1);

    public static native int pixWriteStreamPam(ILeptonica.FILE var0, Pix var1);

    public static native Pix pixReadMemPnm(ByteBuffer var0, NativeSize var1);

    public static native int readHeaderMemPnm(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7);

    public static native int pixWriteMemPnm(PointerByReference var0, NativeSizeByReference var1, Pix var2);

    public static native int pixWriteMemPam(PointerByReference var0, NativeSizeByReference var1, Pix var2);

    public static native Pix pixProjectiveSampledPta(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixProjectiveSampled(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixProjectivePta(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixProjective(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixProjectivePtaColor(Pix var0, Pta var1, Pta var2, int var3);

    public static native Pix pixProjectiveColor(Pix var0, FloatBuffer var1, int var2);

    public static native Pix pixProjectivePtaGray(Pix var0, Pta var1, Pta var2, byte var3);

    public static native Pix pixProjectiveGray(Pix var0, FloatBuffer var1, byte var2);

    public static native Pix pixProjectivePtaWithAlpha(Pix var0, Pta var1, Pta var2, Pix var3, float var4, int var5);

    public static native int getProjectiveXformCoeffs(Pta var0, Pta var1, PointerByReference var2);

    public static native int projectiveXformSampledPt(FloatBuffer var0, int var1, int var2, IntBuffer var3, IntBuffer var4);

    public static native int projectiveXformPt(FloatBuffer var0, int var1, int var2, FloatBuffer var3, FloatBuffer var4);

    public static native int convertFilesToPS(String var0, String var1, int var2, String var3);

    public static native int sarrayConvertFilesToPS(Sarray var0, int var1, String var2);

    public static native int convertFilesFittedToPS(String var0, String var1, float var2, float var3, String var4);

    public static native int sarrayConvertFilesFittedToPS(Sarray var0, float var1, float var2, String var3);

    public static native int writeImageCompressedToPSFile(String var0, String var1, int var2, IntBuffer var3);

    public static native int convertSegmentedPagesToPS(String var0, String var1, int var2, String var3, String var4, int var5, int var6, int var7, float var8, float var9, int var10, String var11);

    public static native int pixWriteSegmentedPageToPS(Pix var0, Pix var1, float var2, float var3, int var4, int var5, String var6);

    public static native int pixWriteMixedToPS(Pix var0, Pix var1, float var2, int var3, String var4);

    public static native int convertToPSEmbed(String var0, String var1, int var2);

    public static native int pixaWriteCompressedToPS(Pixa var0, String var1, int var2, int var3);

    public static native int pixWriteCompressedToPS(Pix var0, String var1, int var2, int var3, IntBuffer var4);

    public static native int pixWritePSEmbed(String var0, String var1);

    public static native int pixWriteStreamPS(ILeptonica.FILE var0, Pix var1, Box var2, int var3, float var4);

    public static native Pointer pixWriteStringPS(Pix var0, Box var1, int var2, float var3);

    public static native Pointer generateUncompressedPS(ByteBuffer var0, int var1, int var2, int var3, int var4, int var5, float var6, float var7, float var8, float var9, int var10);

    public static native int convertJpegToPSEmbed(String var0, String var1);

    public static native int convertJpegToPS(String var0, String var1, String var2, int var3, int var4, int var5, float var6, int var7, int var8);

    public static native int convertG4ToPSEmbed(String var0, String var1);

    public static native int convertG4ToPS(String var0, String var1, String var2, int var3, int var4, int var5, float var6, int var7, int var8, int var9);

    public static native int convertTiffMultipageToPS(String var0, String var1, float var2);

    public static native int convertFlateToPSEmbed(String var0, String var1);

    public static native int convertFlateToPS(String var0, String var1, String var2, int var3, int var4, int var5, float var6, int var7, int var8);

    public static native int pixWriteMemPS(PointerByReference var0, NativeSizeByReference var1, Pix var2, Box var3, int var4, float var5);

    public static native int getResLetterPage(int var0, int var1, float var2);

    public static native int getResA4Page(int var0, int var1, float var2);

    public static native void l_psWriteBoundingBox(int var0);

    public static native Pta ptaCreate(int var0);

    public static native Pta ptaCreateFromNuma(Numa var0, Numa var1);

    public static native void ptaDestroy(PointerByReference var0);

    public static native Pta ptaCopy(Pta var0);

    public static native Pta ptaCopyRange(Pta var0, int var1, int var2);

    public static native Pta ptaClone(Pta var0);

    public static native int ptaEmpty(Pta var0);

    public static native int ptaAddPt(Pta var0, float var1, float var2);

    public static native int ptaInsertPt(Pta var0, int var1, int var2, int var3);

    public static native int ptaRemovePt(Pta var0, int var1);

    public static native int ptaGetCount(Pta var0);

    public static native int ptaGetPt(Pta var0, int var1, FloatBuffer var2, FloatBuffer var3);

    public static native int ptaGetIPt(Pta var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native int ptaSetPt(Pta var0, int var1, float var2, float var3);

    public static native int ptaGetArrays(Pta var0, PointerByReference var1, PointerByReference var2);

    public static native Pta ptaRead(String var0);

    public static native Pta ptaReadStream(ILeptonica.FILE var0);

    public static native Pta ptaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int ptaWriteDebug(String var0, Pta var1, int var2);

    public static native int ptaWrite(String var0, Pta var1, int var2);

    public static native int ptaWriteStream(ILeptonica.FILE var0, Pta var1, int var2);

    public static native int ptaWriteMem(PointerByReference var0, NativeSizeByReference var1, Pta var2, int var3);

    public static native Ptaa ptaaCreate(int var0);

    public static native void ptaaDestroy(PointerByReference var0);

    public static native int ptaaAddPta(Ptaa var0, Pta var1, int var2);

    public static native int ptaaGetCount(Ptaa var0);

    public static native Pta ptaaGetPta(Ptaa var0, int var1, int var2);

    public static native int ptaaGetPt(Ptaa var0, int var1, int var2, FloatBuffer var3, FloatBuffer var4);

    public static native int ptaaInitFull(Ptaa var0, Pta var1);

    public static native int ptaaReplacePta(Ptaa var0, int var1, Pta var2);

    public static native int ptaaAddPt(Ptaa var0, int var1, float var2, float var3);

    public static native int ptaaTruncate(Ptaa var0);

    public static native Ptaa ptaaRead(String var0);

    public static native Ptaa ptaaReadStream(ILeptonica.FILE var0);

    public static native Ptaa ptaaReadMem(ByteBuffer var0, NativeSize var1);

    public static native int ptaaWriteDebug(String var0, Ptaa var1, int var2);

    public static native int ptaaWrite(String var0, Ptaa var1, int var2);

    public static native int ptaaWriteStream(ILeptonica.FILE var0, Ptaa var1, int var2);

    public static native int ptaaWriteMem(PointerByReference var0, NativeSizeByReference var1, Ptaa var2, int var3);

    public static native Pta ptaSubsample(Pta var0, int var1);

    public static native int ptaJoin(Pta var0, Pta var1, int var2, int var3);

    public static native int ptaaJoin(Ptaa var0, Ptaa var1, int var2, int var3);

    public static native Pta ptaReverse(Pta var0, int var1);

    public static native Pta ptaTranspose(Pta var0);

    public static native Pta ptaCyclicPerm(Pta var0, int var1, int var2);

    public static native Pta ptaSelectRange(Pta var0, int var1, int var2);

    public static native Box ptaGetBoundingRegion(Pta var0);

    public static native int ptaGetRange(Pta var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, FloatBuffer var4);

    public static native Pta ptaGetInsideBox(Pta var0, Box var1);

    public static native Pta pixFindCornerPixels(Pix var0);

    public static native int ptaContainsPt(Pta var0, int var1, int var2);

    public static native int ptaTestIntersection(Pta var0, Pta var1);

    public static native Pta ptaTransform(Pta var0, int var1, int var2, float var3, float var4);

    public static native int ptaPtInsidePolygon(Pta var0, float var1, float var2, IntBuffer var3);

    public static native float l_angleBetweenVectors(float var0, float var1, float var2, float var3);

    public static native int ptaPolygonIsConvex(Pta var0, IntBuffer var1);

    public static native int ptaGetMinMax(Pta var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, FloatBuffer var4);

    public static native Pta ptaSelectByValue(Pta var0, float var1, float var2, int var3, int var4);

    public static native Pta ptaCropToMask(Pta var0, Pix var1);

    public static native int ptaGetLinearLSF(Pta var0, FloatBuffer var1, FloatBuffer var2, PointerByReference var3);

    public static native int ptaGetLinearLSF(Pta var0, FloatByReference var1, FloatByReference var2, PointerByReference var3);

    public static native int ptaGetQuadraticLSF(Pta var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, PointerByReference var4);

    public static native int ptaGetQuadraticLSF(Pta var0, FloatByReference var1, FloatByReference var2, FloatByReference var3, PointerByReference var4);

    public static native int ptaGetCubicLSF(Pta var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, FloatBuffer var4, PointerByReference var5);

    public static native int ptaGetCubicLSF(Pta var0, FloatByReference var1, FloatByReference var2, FloatByReference var3, FloatByReference var4, PointerByReference var5);

    public static native int ptaGetQuarticLSF(Pta var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5, PointerByReference var6);

    public static native int ptaGetQuarticLSF(Pta var0, FloatByReference var1, FloatByReference var2, FloatByReference var3, FloatByReference var4, FloatByReference var5, PointerByReference var6);

    public static native int ptaNoisyLinearLSF(Pta var0, float var1, PointerByReference var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5, PointerByReference var6);

    public static native int ptaNoisyLinearLSF(Pta var0, float var1, PointerByReference var2, FloatByReference var3, FloatByReference var4, FloatByReference var5, PointerByReference var6);

    public static native int ptaNoisyQuadraticLSF(Pta var0, float var1, PointerByReference var2, FloatBuffer var3, FloatBuffer var4, FloatBuffer var5, FloatBuffer var6, PointerByReference var7);

    public static native int ptaNoisyQuadraticLSF(Pta var0, float var1, PointerByReference var2, FloatByReference var3, FloatByReference var4, FloatByReference var5, FloatByReference var6, PointerByReference var7);

    public static native int applyLinearFit(float var0, float var1, float var2, FloatBuffer var3);

    public static native int applyQuadraticFit(float var0, float var1, float var2, float var3, FloatBuffer var4);

    public static native int applyCubicFit(float var0, float var1, float var2, float var3, float var4, FloatBuffer var5);

    public static native int applyQuarticFit(float var0, float var1, float var2, float var3, float var4, float var5, FloatBuffer var6);

    public static native int pixPlotAlongPta(Pix var0, Pta var1, int var2, String var3);

    public static native Pta ptaGetPixelsFromPix(Pix var0, Box var1);

    public static native Pix pixGenerateFromPta(Pta var0, int var1, int var2);

    public static native Pta ptaGetBoundaryPixels(Pix var0, int var1);

    public static native Ptaa ptaaGetBoundaryPixels(Pix var0, int var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native Ptaa ptaaIndexLabeledPixels(Pix var0, IntBuffer var1);

    public static native Pta ptaGetNeighborPixLocs(Pix var0, int var1, int var2, int var3);

    public static native Pta numaConvertToPta1(Numa var0);

    public static native Pta numaConvertToPta2(Numa var0, Numa var1);

    public static native int ptaConvertToNuma(Pta var0, PointerByReference var1, PointerByReference var2);

    public static native Pix pixDisplayPta(Pix var0, Pix var1, Pta var2);

    public static native Pix pixDisplayPtaaPattern(Pix var0, Pix var1, Ptaa var2, Pix var3, int var4, int var5);

    public static native Pix pixDisplayPtaPattern(Pix var0, Pix var1, Pta var2, Pix var3, int var4, int var5, int var6);

    public static native Pta ptaReplicatePattern(Pta var0, Pix var1, Pta var2, int var3, int var4, int var5, int var6);

    public static native Pix pixDisplayPtaa(Pix var0, Ptaa var1);

    public static native Pta ptaSort(Pta var0, int var1, int var2, PointerByReference var3);

    public static native int ptaGetSortIndex(Pta var0, int var1, int var2, PointerByReference var3);

    public static native Pta ptaSortByIndex(Pta var0, Numa var1);

    public static native Ptaa ptaaSortByIndex(Ptaa var0, Numa var1);

    public static native int ptaGetRankValue(Pta var0, float var1, Pta var2, int var3, FloatBuffer var4);

    public static native Pta ptaSort2d(Pta var0);

    public static native int ptaEqual(Pta var0, Pta var1, IntBuffer var2);

    public static native L_Rbtree l_asetCreateFromPta(Pta var0);

    public static native int ptaRemoveDupsByAset(Pta var0, PointerByReference var1);

    public static native int ptaUnionByAset(Pta var0, Pta var1, PointerByReference var2);

    public static native int ptaIntersectionByAset(Pta var0, Pta var1, PointerByReference var2);

    public static native L_Hashmap l_hmapCreateFromPta(Pta var0);

    public static native int ptaRemoveDupsByHmap(Pta var0, PointerByReference var1, PointerByReference var2);

    public static native int ptaUnionByHmap(Pta var0, Pta var1, PointerByReference var2);

    public static native int ptaIntersectionByHmap(Pta var0, Pta var1, PointerByReference var2);

    public static native L_Ptra ptraCreate(int var0);

    public static native void ptraDestroy(PointerByReference var0, int var1, int var2);

    public static native int ptraAdd(L_Ptra var0, Pointer var1);

    public static native int ptraInsert(L_Ptra var0, int var1, Pointer var2, int var3);

    public static native Pointer ptraRemove(L_Ptra var0, int var1, int var2);

    public static native Pointer ptraRemoveLast(L_Ptra var0);

    public static native Pointer ptraReplace(L_Ptra var0, int var1, Pointer var2, int var3);

    public static native int ptraSwap(L_Ptra var0, int var1, int var2);

    public static native int ptraCompactArray(L_Ptra var0);

    public static native int ptraReverse(L_Ptra var0);

    public static native int ptraJoin(L_Ptra var0, L_Ptra var1);

    public static native int ptraGetMaxIndex(L_Ptra var0, IntBuffer var1);

    public static native int ptraGetActualCount(L_Ptra var0, IntBuffer var1);

    public static native Pointer ptraGetPtrToItem(L_Ptra var0, int var1);

    public static native L_Ptraa ptraaCreate(int var0);

    public static native void ptraaDestroy(PointerByReference var0, int var1, int var2);

    public static native int ptraaGetSize(L_Ptraa var0, IntBuffer var1);

    public static native int ptraaInsertPtra(L_Ptraa var0, int var1, L_Ptra var2);

    public static native L_Ptra ptraaGetPtra(L_Ptraa var0, int var1, int var2);

    public static native L_Ptra ptraaFlattenToPtra(L_Ptraa var0);

    public static native int pixQuadtreeMean(Pix var0, int var1, Pix var2, PointerByReference var3);

    public static native int pixQuadtreeVariance(Pix var0, int var1, Pix var2, DPix var3, PointerByReference var4, PointerByReference var5);

    public static native int pixMeanInRectangle(Pix var0, Box var1, Pix var2, FloatBuffer var3);

    public static native int pixVarianceInRectangle(Pix var0, Box var1, Pix var2, DPix var3, FloatBuffer var4, FloatBuffer var5);

    public static native Boxaa boxaaQuadtreeRegions(int var0, int var1, int var2);

    public static native int quadtreeGetParent(FPixa var0, int var1, int var2, int var3, FloatBuffer var4);

    public static native int quadtreeGetChildren(FPixa var0, int var1, int var2, int var3, FloatBuffer var4, FloatBuffer var5, FloatBuffer var6, FloatBuffer var7);

    public static native int quadtreeMaxLevels(int var0, int var1);

    public static native Pix fpixaDisplayQuadtree(FPixa var0, int var1, int var2);

    public static native L_Queue lqueueCreate(int var0);

    public static native void lqueueDestroy(PointerByReference var0, int var1);

    public static native int lqueueAdd(L_Queue var0, Pointer var1);

    public static native Pointer lqueueRemove(L_Queue var0);

    public static native int lqueueGetCount(L_Queue var0);

    public static native int lqueuePrint(ILeptonica.FILE var0, L_Queue var1);

    public static native Pix pixRankFilter(Pix var0, int var1, int var2, float var3);

    public static native Pix pixRankFilterRGB(Pix var0, int var1, int var2, float var3);

    public static native Pix pixRankFilterGray(Pix var0, int var1, int var2, float var3);

    public static native Pix pixMedianFilter(Pix var0, int var1, int var2);

    public static native Pix pixRankFilterWithScaling(Pix var0, int var1, int var2, float var3, float var4);

    public static native L_Rbtree l_rbtreeCreate(int var0);

    public static native Rb_Type l_rbtreeLookup(L_Rbtree var0, Rb_Type.ByValue var1);

    public static native void l_rbtreeInsert(L_Rbtree var0, Rb_Type.ByValue var1, Rb_Type.ByValue var2);

    public static native void l_rbtreeDelete(L_Rbtree var0, Rb_Type.ByValue var1);

    public static native void l_rbtreeDestroy(PointerByReference var0);

    public static native L_Rbtree_Node l_rbtreeGetFirst(L_Rbtree var0);

    public static native L_Rbtree_Node l_rbtreeGetNext(L_Rbtree_Node var0);

    public static native L_Rbtree_Node l_rbtreeGetLast(L_Rbtree var0);

    public static native L_Rbtree_Node l_rbtreeGetPrev(L_Rbtree_Node var0);

    public static native int l_rbtreeGetCount(L_Rbtree var0);

    public static native void l_rbtreePrint(ILeptonica.FILE var0, L_Rbtree var1);

    public static native Sarray pixProcessBarcodes(Pix var0, int var1, int var2, PointerByReference var3, int var4);

    public static native Pixa pixExtractBarcodes(Pix var0, int var1);

    public static native Sarray pixReadBarcodes(Pixa var0, int var1, int var2, PointerByReference var3, int var4);

    public static native Numa pixReadBarcodeWidths(Pix var0, int var1, int var2);

    public static native Boxa pixLocateBarcodes(Pix var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixDeskewBarcode(Pix var0, Pix var1, Box var2, int var3, int var4, FloatBuffer var5, FloatBuffer var6);

    public static native Numa pixExtractBarcodeWidths1(Pix var0, float var1, float var2, PointerByReference var3, PointerByReference var4, int var5);

    public static native Numa pixExtractBarcodeWidths2(Pix var0, float var1, FloatBuffer var2, PointerByReference var3, int var4);

    public static native Numa pixExtractBarcodeWidths2(Pix var0, float var1, FloatByReference var2, PointerByReference var3, int var4);

    public static native Numa pixExtractBarcodeCrossings(Pix var0, float var1, int var2);

    public static native Numa numaQuantizeCrossingsByWidth(Numa var0, float var1, PointerByReference var2, PointerByReference var3, int var4);

    public static native Numa numaQuantizeCrossingsByWindow(Numa var0, float var1, FloatBuffer var2, FloatBuffer var3, PointerByReference var4, int var5);

    public static native Numa numaQuantizeCrossingsByWindow(Numa var0, float var1, FloatByReference var2, FloatByReference var3, PointerByReference var4, int var5);

    public static native Pixa pixaReadFiles(String var0, String var1);

    public static native Pixa pixaReadFilesSA(Sarray var0);

    public static native Pix pixRead(String var0);

    public static native Pix pixReadWithHint(String var0, int var1);

    public static native Pix pixReadIndexed(Sarray var0, int var1);

    public static native Pix pixReadStream(ILeptonica.FILE var0, int var1);

    public static native int pixReadHeader(String var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int findFileFormat(String var0, IntBuffer var1);

    public static native int findFileFormatStream(ILeptonica.FILE var0, IntBuffer var1);

    public static native int findFileFormatBuffer(ByteBuffer var0, IntBuffer var1);

    public static native int fileFormatIsTiff(ILeptonica.FILE var0);

    public static native Pix pixReadMem(ByteBuffer var0, NativeSize var1);

    public static native int pixReadHeaderMem(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7);

    public static native int writeImageFileInfo(String var0, ILeptonica.FILE var1, int var2);

    public static native int ioFormatTest(String var0);

    public static native L_Recog recogCreateFromRecog(L_Recog var0, int var1, int var2, int var3, int var4, int var5);

    public static native L_Recog recogCreateFromPixa(Pixa var0, int var1, int var2, int var3, int var4, int var5);

    public static native L_Recog recogCreateFromPixaNoFinish(Pixa var0, int var1, int var2, int var3, int var4, int var5);

    public static native L_Recog recogCreate(int var0, int var1, int var2, int var3, int var4);

    public static native void recogDestroy(PointerByReference var0);

    public static native int recogGetCount(L_Recog var0);

    public static native int recogSetParams(L_Recog var0, int var1, int var2, float var3, float var4);

    public static native int recogGetClassIndex(L_Recog var0, int var1, ByteBuffer var2, IntBuffer var3);

    public static native int recogStringToIndex(L_Recog var0, ByteBuffer var1, IntBuffer var2);

    public static native int recogGetClassString(L_Recog var0, int var1, PointerByReference var2);

    public static native int l_convertCharstrToInt(String var0, IntBuffer var1);

    public static native L_Recog recogRead(String var0);

    public static native L_Recog recogReadStream(ILeptonica.FILE var0);

    public static native L_Recog recogReadMem(ByteBuffer var0, NativeSize var1);

    public static native int recogWrite(String var0, L_Recog var1);

    public static native int recogWriteStream(ILeptonica.FILE var0, L_Recog var1);

    public static native int recogWriteMem(PointerByReference var0, NativeSizeByReference var1, L_Recog var2);

    public static native Pixa recogExtractPixa(L_Recog var0);

    public static native Boxa recogDecode(L_Recog var0, Pix var1, int var2, PointerByReference var3);

    public static native int recogCreateDid(L_Recog var0, Pix var1);

    public static native int recogDestroyDid(L_Recog var0);

    public static native int recogDidExists(L_Recog var0);

    public static native L_Rdid recogGetDid(L_Recog var0);

    public static native int recogSetChannelParams(L_Recog var0, int var1);

    public static native int recogIdentifyMultiple(L_Recog var0, Pix var1, int var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, int var7);

    public static native int recogSplitIntoCharacters(L_Recog var0, Pix var1, int var2, int var3, PointerByReference var4, PointerByReference var5, int var6);

    public static native int recogCorrelationBestRow(L_Recog var0, Pix var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, int var6);

    public static native int recogCorrelationBestChar(L_Recog var0, Pix var1, PointerByReference var2, FloatBuffer var3, IntBuffer var4, PointerByReference var5, PointerByReference var6);

    public static native int recogCorrelationBestChar(L_Recog var0, Pix var1, PointerByReference var2, FloatByReference var3, IntByReference var4, PointerByReference var5, PointerByReference var6);

    public static native int recogIdentifyPixa(L_Recog var0, Pixa var1, PointerByReference var2);

    public static native int recogIdentifyPix(L_Recog var0, Pix var1, PointerByReference var2);

    public static native int recogSkipIdentify(L_Recog var0);

    public static native void rchaDestroy(PointerByReference var0);

    public static native void rchDestroy(PointerByReference var0);

    public static native int rchaExtract(L_Rcha var0, PointerByReference var1, PointerByReference var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7);

    public static native int rchExtract(L_Rch var0, IntBuffer var1, FloatBuffer var2, PointerByReference var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7);

    public static native Pix recogProcessToIdentify(L_Recog var0, Pix var1, int var2);

    public static native Sarray recogExtractNumbers(L_Recog var0, Boxa var1, float var2, int var3, PointerByReference var4, PointerByReference var5);

    public static native Pixa showExtractNumbers(Pix var0, Sarray var1, Boxaa var2, Numaa var3, PointerByReference var4);

    public static native int recogTrainLabeled(L_Recog var0, Pix var1, Box var2, ByteBuffer var3, int var4);

    public static native int recogProcessLabeled(L_Recog var0, Pix var1, Box var2, ByteBuffer var3, PointerByReference var4);

    public static native int recogProcessLabeled(L_Recog var0, Pix var1, Box var2, Pointer var3, PointerByReference var4);

    public static native int recogAddSample(L_Recog var0, Pix var1, int var2);

    public static native Pix recogModifyTemplate(L_Recog var0, Pix var1);

    public static native int recogAverageSamples(L_Recog var0, int var1);

    public static native int pixaAccumulateSamples(Pixa var0, Pta var1, PointerByReference var2, FloatBuffer var3, FloatBuffer var4);

    public static native int pixaAccumulateSamples(Pixa var0, Pta var1, PointerByReference var2, FloatByReference var3, FloatByReference var4);

    public static native int recogTrainingFinished(PointerByReference var0, int var1, int var2, float var3);

    public static native Pixa recogFilterPixaBySize(Pixa var0, int var1, int var2, float var3, PointerByReference var4);

    public static native Pixaa recogSortPixaByClass(Pixa var0, int var1);

    public static native int recogRemoveOutliers1(PointerByReference var0, float var1, int var2, int var3, PointerByReference var4, PointerByReference var5);

    public static native Pixa pixaRemoveOutliers1(Pixa var0, float var1, int var2, int var3, PointerByReference var4, PointerByReference var5);

    public static native int recogRemoveOutliers2(PointerByReference var0, float var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native Pixa pixaRemoveOutliers2(Pixa var0, float var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native Pixa recogTrainFromBoot(L_Recog var0, Pixa var1, float var2, int var3, int var4);

    public static native int recogPadDigitTrainingSet(PointerByReference var0, int var1, int var2);

    public static native int recogIsPaddingNeeded(L_Recog var0, PointerByReference var1);

    public static native Pixa recogAddDigitPadTemplates(L_Recog var0, Sarray var1);

    public static native L_Recog recogMakeBootDigitRecog(int var0, int var1, int var2, int var3, int var4);

    public static native Pixa recogMakeBootDigitTemplates(int var0, int var1);

    public static native int recogShowContent(ILeptonica.FILE var0, L_Recog var1, int var2, int var3);

    public static native int recogDebugAverages(L_Recog var0, int var1);

    public static native int recogShowAverageTemplates(L_Recog var0);

    public static native int recogShowMatchesInRange(L_Recog var0, Pixa var1, float var2, float var3, int var4);

    public static native Pix recogShowMatch(L_Recog var0, Pix var1, Pix var2, Box var3, int var4, float var5);

    public static native int regTestSetup(int var0, PointerByReference var1, PointerByReference var2);

    public static native int regTestCleanup(L_RegParams var0);

    public static native int regTestCompareValues(L_RegParams var0, float var1, float var2, float var3);

    public static native int regTestCompareStrings(L_RegParams var0, ByteBuffer var1, NativeSize var2, ByteBuffer var3, NativeSize var4);

    public static native int regTestComparePix(L_RegParams var0, Pix var1, Pix var2);

    public static native int regTestCompareSimilarPix(L_RegParams var0, Pix var1, Pix var2, int var3, float var4, int var5);

    public static native int regTestCheckFile(L_RegParams var0, String var1);

    public static native int regTestCompareFiles(L_RegParams var0, int var1, int var2);

    public static native int regTestWritePixAndCheck(L_RegParams var0, Pix var1, int var2);

    public static native int regTestWriteDataAndCheck(L_RegParams var0, Pointer var1, NativeSize var2, String var3);

    public static native Pointer regTestGenLocalFilename(L_RegParams var0, int var1, int var2);

    public static native int l_pdfRenderFile(String var0, int var1, PointerByReference var2);

    public static native int l_pdfRenderFiles(String var0, Sarray var1, int var2, PointerByReference var3);

    public static native int pixRasterop(Pix var0, int var1, int var2, int var3, int var4, int var5, Pix var6, int var7, int var8);

    public static native int pixRasteropVip(Pix var0, int var1, int var2, int var3, int var4);

    public static native int pixRasteropHip(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixTranslate(Pix var0, Pix var1, int var2, int var3, int var4);

    public static native int pixRasteropIP(Pix var0, int var1, int var2, int var3);

    public static native int pixRasteropFullImage(Pix var0, Pix var1, int var2);

    public static native void rasteropUniLow(IntBuffer var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9);

    public static native void rasteropLow(IntBuffer var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, IntBuffer var10, int var11, int var12, int var13, int var14, int var15);

    public static native void rasteropVipLow(IntBuffer var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public static native void rasteropHipLow(IntBuffer var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixRotate(Pix var0, float var1, int var2, int var3, int var4, int var5);

    public static native Pix pixEmbedForRotation(Pix var0, float var1, int var2, int var3, int var4);

    public static native Pix pixRotateBySampling(Pix var0, int var1, int var2, float var3, int var4);

    public static native Pix pixRotateBinaryNice(Pix var0, float var1, int var2);

    public static native Pix pixRotateWithAlpha(Pix var0, float var1, Pix var2, float var3);

    public static native Pix pixRotateAM(Pix var0, float var1, int var2);

    public static native Pix pixRotateAMColor(Pix var0, float var1, int var2);

    public static native Pix pixRotateAMGray(Pix var0, float var1, byte var2);

    public static native Pix pixRotateAMCorner(Pix var0, float var1, int var2);

    public static native Pix pixRotateAMColorCorner(Pix var0, float var1, int var2);

    public static native Pix pixRotateAMGrayCorner(Pix var0, float var1, byte var2);

    public static native Pix pixRotateAMColorFast(Pix var0, float var1, int var2);

    public static native Pix pixRotateOrth(Pix var0, int var1);

    public static native Pix pixRotate180(Pix var0, Pix var1);

    public static native Pix pixRotate90(Pix var0, int var1);

    public static native Pix pixFlipLR(Pix var0, Pix var1);

    public static native Pix pixFlipTB(Pix var0, Pix var1);

    public static native Pix pixRotateShear(Pix var0, int var1, int var2, float var3, int var4);

    public static native Pix pixRotate2Shear(Pix var0, int var1, int var2, float var3, int var4);

    public static native Pix pixRotate3Shear(Pix var0, int var1, int var2, float var3, int var4);

    public static native int pixRotateShearIP(Pix var0, int var1, int var2, float var3, int var4);

    public static native Pix pixRotateShearCenter(Pix var0, float var1, int var2);

    public static native int pixRotateShearCenterIP(Pix var0, float var1, int var2);

    public static native Pix pixStrokeWidthTransform(Pix var0, int var1, int var2, int var3);

    public static native Pix pixRunlengthTransform(Pix var0, int var1, int var2, int var3);

    public static native int pixFindHorizontalRuns(Pix var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixFindVerticalRuns(Pix var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native Numa pixFindMaxRuns(Pix var0, int var1, PointerByReference var2);

    public static native int pixFindMaxHorizontalRunOnLine(Pix var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native int pixFindMaxVerticalRunOnLine(Pix var0, int var1, IntBuffer var2, IntBuffer var3);

    public static native int runlengthMembershipOnLine(IntBuffer var0, int var1, int var2, IntBuffer var3, IntBuffer var4, int var5);

    public static native IntByReference makeMSBitLocTab(int var0);

    public static native Sarray sarrayCreate(int var0);

    public static native Sarray sarrayCreateInitialized(int var0, String var1);

    public static native Sarray sarrayCreateWordsFromString(String var0);

    public static native Sarray sarrayCreateLinesFromString(String var0, int var1);

    public static native void sarrayDestroy(PointerByReference var0);

    public static native Sarray sarrayCopy(Sarray var0);

    public static native Sarray sarrayClone(Sarray var0);

    public static native int sarrayAddString(Sarray var0, String var1, int var2);

    public static native Pointer sarrayRemoveString(Sarray var0, int var1);

    public static native int sarrayReplaceString(Sarray var0, int var1, ByteBuffer var2, int var3);

    public static native int sarrayClear(Sarray var0);

    public static native int sarrayGetCount(Sarray var0);

    public static native PointerByReference sarrayGetArray(Sarray var0, IntBuffer var1, IntBuffer var2);

    public static native Pointer sarrayGetString(Sarray var0, int var1, int var2);

    public static native Pointer sarrayToString(Sarray var0, int var1);

    public static native Pointer sarrayToStringRange(Sarray var0, int var1, int var2, int var3);

    public static native Sarray sarrayConcatUniformly(Sarray var0, int var1, int var2);

    public static native int sarrayJoin(Sarray var0, Sarray var1);

    public static native int sarrayAppendRange(Sarray var0, Sarray var1, int var2, int var3);

    public static native int sarrayPadToSameSize(Sarray var0, Sarray var1, String var2);

    public static native Sarray sarrayConvertWordsToLines(Sarray var0, int var1);

    public static native int sarraySplitString(Sarray var0, String var1, String var2);

    public static native Sarray sarraySelectBySubstring(Sarray var0, String var1);

    public static native Sarray sarraySelectRange(Sarray var0, int var1, int var2);

    public static native int sarrayParseRange(Sarray var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, String var5, int var6);

    public static native Sarray sarrayRead(String var0);

    public static native Sarray sarrayReadStream(ILeptonica.FILE var0);

    public static native Sarray sarrayReadMem(ByteBuffer var0, NativeSize var1);

    public static native int sarrayWrite(String var0, Sarray var1);

    public static native int sarrayWriteStream(ILeptonica.FILE var0, Sarray var1);

    public static native int sarrayWriteStderr(Sarray var0);

    public static native int sarrayWriteMem(PointerByReference var0, NativeSizeByReference var1, Sarray var2);

    public static native int sarrayAppend(String var0, Sarray var1);

    public static native Sarray getNumberedPathnamesInDirectory(String var0, String var1, int var2, int var3, int var4);

    public static native Sarray getSortedPathnamesInDirectory(String var0, String var1, int var2, int var3);

    public static native Sarray convertSortedToNumberedPathnames(Sarray var0, int var1, int var2, int var3);

    public static native Sarray getFilenamesInDirectory(String var0);

    public static native Sarray sarraySort(Sarray var0, Sarray var1, int var2);

    public static native Sarray sarraySortByIndex(Sarray var0, Numa var1);

    public static native int stringCompareLexical(String var0, String var1);

    public static native L_Rbtree l_asetCreateFromSarray(Sarray var0);

    public static native int sarrayRemoveDupsByAset(Sarray var0, PointerByReference var1);

    public static native int sarrayUnionByAset(Sarray var0, Sarray var1, PointerByReference var2);

    public static native int sarrayIntersectionByAset(Sarray var0, Sarray var1, PointerByReference var2);

    public static native L_Hashmap l_hmapCreateFromSarray(Sarray var0);

    public static native int sarrayRemoveDupsByHmap(Sarray var0, PointerByReference var1, PointerByReference var2);

    public static native int sarrayUnionByHmap(Sarray var0, Sarray var1, PointerByReference var2);

    public static native int sarrayIntersectionByHmap(Sarray var0, Sarray var1, PointerByReference var2);

    public static native Sarray sarrayGenerateIntegers(int var0);

    public static native int sarrayLookupCSKV(Sarray var0, String var1, PointerByReference var2);

    public static native Pix pixScale(Pix var0, float var1, float var2);

    public static native Pix pixScaleToSizeRel(Pix var0, int var1, int var2);

    public static native Pix pixScaleToSize(Pix var0, int var1, int var2);

    public static native Pix pixScaleToResolution(Pix var0, float var1, float var2, FloatBuffer var3);

    public static native Pix pixScaleGeneral(Pix var0, float var1, float var2, float var3, int var4);

    public static native Pix pixScaleLI(Pix var0, float var1, float var2);

    public static native Pix pixScaleColorLI(Pix var0, float var1, float var2);

    public static native Pix pixScaleColor2xLI(Pix var0);

    public static native Pix pixScaleColor4xLI(Pix var0);

    public static native Pix pixScaleGrayLI(Pix var0, float var1, float var2);

    public static native Pix pixScaleGray2xLI(Pix var0);

    public static native Pix pixScaleGray4xLI(Pix var0);

    public static native Pix pixScaleGray2xLIThresh(Pix var0, int var1);

    public static native Pix pixScaleGray2xLIDither(Pix var0);

    public static native Pix pixScaleGray4xLIThresh(Pix var0, int var1);

    public static native Pix pixScaleGray4xLIDither(Pix var0);

    public static native Pix pixScaleBySampling(Pix var0, float var1, float var2);

    public static native Pix pixScaleBySamplingWithShift(Pix var0, float var1, float var2, float var3, float var4);

    public static native Pix pixScaleBySamplingToSize(Pix var0, int var1, int var2);

    public static native Pix pixScaleByIntSampling(Pix var0, int var1);

    public static native Pix pixScaleRGBToGrayFast(Pix var0, int var1, int var2);

    public static native Pix pixScaleRGBToBinaryFast(Pix var0, int var1, int var2);

    public static native Pix pixScaleGrayToBinaryFast(Pix var0, int var1, int var2);

    public static native Pix pixScaleSmooth(Pix var0, float var1, float var2);

    public static native Pix pixScaleSmoothToSize(Pix var0, int var1, int var2);

    public static native Pix pixScaleRGBToGray2(Pix var0, float var1, float var2, float var3);

    public static native Pix pixScaleAreaMap(Pix var0, float var1, float var2);

    public static native Pix pixScaleAreaMap2(Pix var0);

    public static native Pix pixScaleAreaMapToSize(Pix var0, int var1, int var2);

    public static native Pix pixScaleBinary(Pix var0, float var1, float var2);

    public static native Pix pixScaleBinaryWithShift(Pix var0, float var1, float var2, float var3, float var4);

    public static native Pix pixScaleToGray(Pix var0, float var1);

    public static native Pix pixScaleToGrayFast(Pix var0, float var1);

    public static native Pix pixScaleToGray2(Pix var0);

    public static native Pix pixScaleToGray3(Pix var0);

    public static native Pix pixScaleToGray4(Pix var0);

    public static native Pix pixScaleToGray6(Pix var0);

    public static native Pix pixScaleToGray8(Pix var0);

    public static native Pix pixScaleToGray16(Pix var0);

    public static native Pix pixScaleToGrayMipmap(Pix var0, float var1);

    public static native Pix pixScaleMipmap(Pix var0, Pix var1, float var2);

    public static native Pix pixExpandReplicate(Pix var0, int var1);

    public static native Pix pixScaleGrayMinMax(Pix var0, int var1, int var2, int var3);

    public static native Pix pixScaleGrayMinMax2(Pix var0, int var1);

    public static native Pix pixScaleGrayRankCascade(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixScaleGrayRank2(Pix var0, int var1);

    public static native int pixScaleAndTransferAlpha(Pix var0, Pix var1, float var2, float var3);

    public static native Pix pixScaleWithAlpha(Pix var0, float var1, float var2, Pix var3, float var4);

    public static native Pix pixSeedfillBinary(Pix var0, Pix var1, Pix var2, int var3);

    public static native Pix pixSeedfillBinaryRestricted(Pix var0, Pix var1, Pix var2, int var3, int var4, int var5);

    public static native Pix pixHolesByFilling(Pix var0, int var1);

    public static native Pix pixFillClosedBorders(Pix var0, int var1);

    public static native Pix pixExtractBorderConnComps(Pix var0, int var1);

    public static native Pix pixRemoveBorderConnComps(Pix var0, int var1);

    public static native Pix pixFillBgFromBorder(Pix var0, int var1);

    public static native Pix pixFillHolesToBoundingRect(Pix var0, int var1, float var2, float var3);

    public static native int pixSeedfillGray(Pix var0, Pix var1, int var2);

    public static native int pixSeedfillGrayInv(Pix var0, Pix var1, int var2);

    public static native int pixSeedfillGraySimple(Pix var0, Pix var1, int var2);

    public static native int pixSeedfillGrayInvSimple(Pix var0, Pix var1, int var2);

    public static native Pix pixSeedfillGrayBasin(Pix var0, Pix var1, int var2, int var3);

    public static native Pix pixDistanceFunction(Pix var0, int var1, int var2, int var3);

    public static native Pix pixSeedspread(Pix var0, int var1);

    public static native int pixLocalExtrema(Pix var0, int var1, int var2, PointerByReference var3, PointerByReference var4);

    public static native int pixSelectedLocalExtrema(Pix var0, int var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixFindEqualValues(Pix var0, Pix var1);

    public static native int pixSelectMinInConnComp(Pix var0, Pix var1, PointerByReference var2, PointerByReference var3);

    public static native Pix pixRemoveSeededComponents(Pix var0, Pix var1, Pix var2, int var3, int var4);

    public static native Sela selaCreate(int var0);

    public static native void selaDestroy(PointerByReference var0);

    public static native Sel selCreate(int var0, int var1, String var2);

    public static native void selDestroy(PointerByReference var0);

    public static native Sel selCopy(Pointer var0);

    public static native Sel selCreateBrick(int var0, int var1, int var2, int var3, int var4);

    public static native Sel selCreateComb(int var0, int var1, int var2);

    public static native PointerByReference create2dIntArray(int var0, int var1);

    public static native int selaAddSel(Sela var0, Pointer var1, String var2, int var3);

    public static native int selaGetCount(Sela var0);

    public static native Sel selaGetSel(Sela var0, int var1);

    public static native Pointer selGetName(Pointer var0);

    public static native int selSetName(Pointer var0, String var1);

    public static native int selaFindSelByName(Sela var0, String var1, IntBuffer var2, PointerByReference var3);

    public static native int selGetElement(Pointer var0, int var1, int var2, IntBuffer var3);

    public static native int selSetElement(Pointer var0, int var1, int var2, int var3);

    public static native int selGetParameters(Pointer var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int selSetOrigin(Pointer var0, int var1, int var2);

    public static native int selGetTypeAtOrigin(Pointer var0, IntBuffer var1);

    public static native Pointer selaGetBrickName(Sela var0, int var1, int var2);

    public static native Pointer selaGetCombName(Sela var0, int var1, int var2);

    public static native int getCompositeParameters(int var0, IntBuffer var1, IntBuffer var2, PointerByReference var3, PointerByReference var4, PointerByReference var5, PointerByReference var6);

    public static native Sarray selaGetSelnames(Sela var0);

    public static native int selFindMaxTranslations(Pointer var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native Sel selRotateOrth(Pointer var0, int var1);

    public static native Sela selaRead(String var0);

    public static native Sela selaReadStream(ILeptonica.FILE var0);

    public static native Sel selRead(String var0);

    public static native Sel selReadStream(ILeptonica.FILE var0);

    public static native int selaWrite(String var0, Sela var1);

    public static native int selaWriteStream(ILeptonica.FILE var0, Sela var1);

    public static native int selWrite(String var0, Pointer var1);

    public static native int selWriteStream(ILeptonica.FILE var0, Pointer var1);

    public static native Sel selCreateFromString(String var0, int var1, int var2, String var3);

    public static native Pointer selPrintToString(Pointer var0);

    public static native Sela selaCreateFromFile(String var0);

    public static native Sel selCreateFromPta(Pta var0, int var1, int var2, String var3);

    public static native Sel selCreateFromPix(Pix var0, int var1, int var2, String var3);

    public static native Sel selReadFromColorImage(String var0);

    public static native Sel selCreateFromColorPix(Pix var0, String var1);

    public static native Sela selaCreateFromColorPixa(Pixa var0, Sarray var1);

    public static native Pix selDisplayInPix(Pointer var0, int var1, int var2);

    public static native Pix selaDisplayInPix(Sela var0, int var1, int var2, int var3, int var4);

    public static native Sela selaAddBasic(Sela var0);

    public static native Sela selaAddHitMiss(Sela var0);

    public static native Sela selaAddDwaLinear(Sela var0);

    public static native Sela selaAddDwaCombs(Sela var0);

    public static native Sela selaAddCrossJunctions(Sela var0, float var1, float var2, int var3, int var4);

    public static native Sela selaAddTJunctions(Sela var0, float var1, float var2, int var3, int var4);

    public static native Sela sela4ccThin(Sela var0);

    public static native Sela sela8ccThin(Sela var0);

    public static native Sela sela4and8ccThin(Sela var0);

    public static native Sel selMakePlusSign(int var0, int var1);

    public static native Sel pixGenerateSelWithRuns(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, PointerByReference var9);

    public static native Sel pixGenerateSelRandom(Pix var0, float var1, float var2, int var3, int var4, int var5, int var6, int var7, PointerByReference var8);

    public static native Sel pixGenerateSelBoundary(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, PointerByReference var9);

    public static native Numa pixGetRunCentersOnLine(Pix var0, int var1, int var2, int var3);

    public static native Numa pixGetRunsOnLine(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pta pixSubsampleBoundaryPixels(Pix var0, int var1);

    public static native int adjacentOnPixelInRaster(Pix var0, int var1, int var2, IntBuffer var3, IntBuffer var4);

    public static native Pix pixDisplayHitMissSel(Pix var0, Pointer var1, int var2, int var3, int var4);

    public static native Pix pixHShear(Pix var0, Pix var1, int var2, float var3, int var4);

    public static native Pix pixVShear(Pix var0, Pix var1, int var2, float var3, int var4);

    public static native Pix pixHShearCorner(Pix var0, Pix var1, float var2, int var3);

    public static native Pix pixVShearCorner(Pix var0, Pix var1, float var2, int var3);

    public static native Pix pixHShearCenter(Pix var0, Pix var1, float var2, int var3);

    public static native Pix pixVShearCenter(Pix var0, Pix var1, float var2, int var3);

    public static native int pixHShearIP(Pix var0, int var1, float var2, int var3);

    public static native int pixVShearIP(Pix var0, int var1, float var2, int var3);

    public static native Pix pixHShearLI(Pix var0, int var1, float var2, int var3);

    public static native Pix pixVShearLI(Pix var0, int var1, float var2, int var3);

    public static native Pix pixDeskewBoth(Pix var0, int var1);

    public static native Pix pixDeskew(Pix var0, int var1);

    public static native Pix pixFindSkewAndDeskew(Pix var0, int var1, FloatBuffer var2, FloatBuffer var3);

    public static native Pix pixDeskewGeneral(Pix var0, int var1, float var2, float var3, int var4, int var5, FloatBuffer var6, FloatBuffer var7);

    public static native int pixFindSkew(Pix var0, FloatBuffer var1, FloatBuffer var2);

    public static native int pixFindSkewSweep(Pix var0, FloatBuffer var1, int var2, float var3, float var4);

    public static native int pixFindSkewSweepAndSearch(Pix var0, FloatBuffer var1, FloatBuffer var2, int var3, int var4, float var5, float var6, float var7);

    public static native int pixFindSkewSweepAndSearchScore(Pix var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, int var4, int var5, float var6, float var7, float var8, float var9);

    public static native int pixFindSkewSweepAndSearchScorePivot(Pix var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3, int var4, int var5, float var6, float var7, float var8, float var9, int var10);

    public static native int pixFindSkewOrthogonalRange(Pix var0, FloatBuffer var1, FloatBuffer var2, int var3, int var4, float var5, float var6, float var7, float var8);

    public static native int pixFindDifferentialSquareSum(Pix var0, FloatBuffer var1);

    public static native int pixFindNormalizedSquareSum(Pix var0, FloatBuffer var1, FloatBuffer var2, FloatBuffer var3);

    public static native Pix pixReadStreamSpix(ILeptonica.FILE var0);

    public static native int readHeaderSpix(String var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int freadHeaderSpix(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native int sreadHeaderSpix(IntBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6);

    public static native int pixWriteStreamSpix(ILeptonica.FILE var0, Pix var1);

    public static native Pix pixReadMemSpix(ByteBuffer var0, NativeSize var1);

    public static native int pixWriteMemSpix(PointerByReference var0, NativeSizeByReference var1, Pix var2);

    public static native int pixSerializeToMemory(Pix var0, PointerByReference var1, NativeSizeByReference var2);

    public static native Pix pixDeserializeFromMemory(IntBuffer var0, NativeSize var1);

    public static native L_Stack lstackCreate(int var0);

    public static native void lstackDestroy(PointerByReference var0, int var1);

    public static native int lstackAdd(L_Stack var0, Pointer var1);

    public static native Pointer lstackRemove(L_Stack var0);

    public static native int lstackGetCount(L_Stack var0);

    public static native int lstackPrint(ILeptonica.FILE var0, L_Stack var1);

    public static native L_StrCode strcodeCreate(int var0);

    public static native int strcodeCreateFromFile(String var0, int var1, String var2);

    public static native int strcodeGenerate(L_StrCode var0, String var1, String var2);

    public static native int strcodeFinalize(PointerByReference var0, String var1);

    public static native int strcodeFinalize(PointerByReference var0, Pointer var1);

    public static native int l_getStructStrFromFile(String var0, int var1, PointerByReference var2);

    public static native int pixFindStrokeLength(Pix var0, IntBuffer var1, IntBuffer var2);

    public static native int pixFindStrokeWidth(Pix var0, float var1, IntBuffer var2, FloatBuffer var3, PointerByReference var4);

    public static native int pixFindStrokeWidth(Pix var0, float var1, IntByReference var2, FloatByReference var3, PointerByReference var4);

    public static native Numa pixaFindStrokeWidth(Pixa var0, float var1, IntBuffer var2, int var3);

    public static native Pixa pixaModifyStrokeWidth(Pixa var0, float var1);

    public static native Pix pixModifyStrokeWidth(Pix var0, float var1, float var2);

    public static native Pixa pixaSetStrokeWidth(Pixa var0, int var1, int var2, int var3);

    public static native Pix pixSetStrokeWidth(Pix var0, int var1, int var2, int var3);

    public static native IntByReference sudokuReadFile(String var0);

    public static native IntByReference sudokuReadString(String var0);

    public static native L_Sudoku sudokuCreate(IntBuffer var0);

    public static native void sudokuDestroy(PointerByReference var0);

    public static native int sudokuSolve(L_Sudoku var0);

    public static native int sudokuTestUniqueness(IntBuffer var0, IntBuffer var1);

    public static native L_Sudoku sudokuGenerate(IntBuffer var0, int var1, int var2, int var3);

    public static native int sudokuOutput(L_Sudoku var0, int var1);

    public static native Pix pixAddSingleTextblock(Pix var0, L_Bmf var1, String var2, int var3, int var4, IntBuffer var5);

    public static native Pix pixAddTextlines(Pix var0, L_Bmf var1, String var2, int var3, int var4);

    public static native int pixSetTextblock(Pix var0, L_Bmf var1, String var2, int var3, int var4, int var5, int var6, int var7, IntBuffer var8);

    public static native int pixSetTextline(Pix var0, L_Bmf var1, String var2, int var3, int var4, int var5, IntBuffer var6, IntBuffer var7);

    public static native Pixa pixaAddTextNumber(Pixa var0, L_Bmf var1, Numa var2, int var3, int var4);

    public static native Pixa pixaAddTextlines(Pixa var0, L_Bmf var1, Sarray var2, int var3, int var4);

    public static native int pixaAddPixWithText(Pixa var0, Pix var1, int var2, L_Bmf var3, String var4, int var5, int var6);

    public static native Sarray bmfGetLineStrings(L_Bmf var0, String var1, int var2, int var3, IntBuffer var4);

    public static native Numa bmfGetWordWidths(L_Bmf var0, String var1, Sarray var2);

    public static native int bmfGetStringWidth(L_Bmf var0, String var1, IntBuffer var2);

    public static native Sarray splitStringToParagraphs(ByteBuffer var0, int var1);

    public static native Pix pixReadTiff(String var0, int var1);

    public static native Pix pixReadStreamTiff(ILeptonica.FILE var0, int var1);

    public static native int pixWriteTiff(String var0, Pix var1, int var2, String var3);

    public static native int pixWriteTiffCustom(String var0, Pix var1, int var2, String var3, Numa var4, Sarray var5, Sarray var6, Numa var7);

    public static native int pixWriteStreamTiff(ILeptonica.FILE var0, Pix var1, int var2);

    public static native int pixWriteStreamTiffWA(ILeptonica.FILE var0, Pix var1, int var2, String var3);

    public static native Pix pixReadFromMultipageTiff(String var0, NativeSizeByReference var1);

    public static native Pixa pixaReadMultipageTiff(String var0);

    public static native int pixaWriteMultipageTiff(String var0, Pixa var1);

    public static native int writeMultipageTiff(String var0, String var1, String var2);

    public static native int writeMultipageTiffSA(Sarray var0, String var1);

    public static native int fprintTiffInfo(ILeptonica.FILE var0, String var1);

    public static native int tiffGetCount(ILeptonica.FILE var0, IntBuffer var1);

    public static native int getTiffResolution(ILeptonica.FILE var0, IntBuffer var1, IntBuffer var2);

    public static native int readHeaderTiff(String var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7, IntBuffer var8);

    public static native int freadHeaderTiff(ILeptonica.FILE var0, int var1, IntBuffer var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7, IntBuffer var8);

    public static native int readHeaderMemTiff(ByteBuffer var0, NativeSize var1, int var2, IntBuffer var3, IntBuffer var4, IntBuffer var5, IntBuffer var6, IntBuffer var7, IntBuffer var8, IntBuffer var9);

    public static native int findTiffCompression(ILeptonica.FILE var0, IntBuffer var1);

    public static native int extractG4DataFromFile(String var0, PointerByReference var1, NativeSizeByReference var2, IntBuffer var3, IntBuffer var4, IntBuffer var5);

    public static native Pix pixReadMemTiff(ByteBuffer var0, NativeSize var1, int var2);

    public static native Pix pixReadMemFromMultipageTiff(ByteBuffer var0, NativeSize var1, NativeSizeByReference var2);

    public static native Pixa pixaReadMemMultipageTiff(ByteBuffer var0, NativeSize var1);

    public static native int pixaWriteMemMultipageTiff(PointerByReference var0, NativeSizeByReference var1, Pixa var2);

    public static native int pixWriteMemTiff(PointerByReference var0, NativeSizeByReference var1, Pix var2, int var3);

    public static native int pixWriteMemTiffCustom(PointerByReference var0, NativeSizeByReference var1, Pix var2, int var3, Numa var4, Sarray var5, Sarray var6, Numa var7);

    public static native int setMsgSeverity(int var0);

    public static native int returnErrorInt(String var0, String var1, int var2);

    public static native float returnErrorFloat(String var0, String var1, float var2);

    public static native Pointer returnErrorPtr(String var0, String var1, Pointer var2);

    public static native int returnErrorInt1(String var0, String var1, String var2, int var3);

    public static native float returnErrorFloat1(String var0, String var1, String var2, float var3);

    public static native Pointer returnErrorPtr1(String var0, String var1, String var2, Pointer var3);

    public static native void leptSetStderrHandler(ILeptonica.leptSetStderrHandler_handler_callback var0);

    public static native void lept_stderr(String var0, PointerByReference var1);

    public static native int filesAreIdentical(String var0, String var1, IntBuffer var2);

    public static native short convertOnLittleEnd16(short var0);

    public static native short convertOnBigEnd16(short var0);

    public static native int convertOnLittleEnd32(int var0);

    public static native int convertOnBigEnd32(int var0);

    public static native int fileCorruptByDeletion(String var0, float var1, float var2, String var3);

    public static native int fileCorruptByMutation(String var0, float var1, float var2, String var3);

    public static native int fileReplaceBytes(String var0, int var1, int var2, ByteBuffer var3, NativeSize var4, String var5);

    public static native int genRandomIntOnInterval(int var0, int var1, int var2, IntBuffer var3);

    public static native int lept_roundftoi(float var0);

    public static native int lept_floor(float var0);

    public static native int lept_ceiling(float var0);

    public static native int l_hashStringToUint64(String var0, LongBuffer var1);

    public static native int l_hashStringToUint64Fast(String var0, LongBuffer var1);

    public static native int l_hashPtToUint64(int var0, int var1, LongBuffer var2);

    public static native int l_hashFloat64ToUint64(double var0, LongBuffer var2);

    public static native int findNextLargerPrime(int var0, IntBuffer var1);

    public static native int lept_isPrime(long var0, IntBuffer var2, IntBuffer var3);

    public static native int convertIntToGrayCode(int var0);

    public static native int convertGrayCodeToInt(int var0);

    public static native Pointer getLeptonicaVersion();

    public static native void startTimer();

    public static native float stopTimer();

    public static native Pointer startTimerNested();

    public static native float stopTimerNested(Pointer var0);

    public static native void l_getCurrentTime(IntBuffer var0, IntBuffer var1);

    public static native L_WallTimer startWallTimer();

    public static native float stopWallTimer(PointerByReference var0);

    public static native Pointer l_getFormattedDate();

    public static native Pointer stringNew(String var0);

    public static native int stringCopy(ByteBuffer var0, String var1, int var2);

    public static native Pointer stringCopySegment(String var0, int var1, int var2);

    public static native int stringReplace(PointerByReference var0, String var1);

    public static native int stringLength(String var0, NativeSize var1);

    public static native int stringCat(ByteBuffer var0, NativeSize var1, String var2);

    public static native Pointer stringConcatNew(String var0, PointerByReference var1);

    public static native Pointer stringJoin(String var0, String var1);

    public static native int stringJoinIP(PointerByReference var0, String var1);

    public static native Pointer stringReverse(String var0);

    public static native Pointer strtokSafe(ByteBuffer var0, String var1, PointerByReference var2);

    public static native int stringSplitOnToken(ByteBuffer var0, String var1, PointerByReference var2, PointerByReference var3);

    public static native int stringCheckForChars(String var0, String var1, IntBuffer var2);

    public static native Pointer stringRemoveChars(String var0, String var1);

    public static native Pointer stringReplaceEachSubstr(String var0, String var1, String var2, IntBuffer var3);

    public static native Pointer stringReplaceSubstr(String var0, String var1, String var2, IntBuffer var3, IntBuffer var4);

    public static native L_Dna stringFindEachSubstr(String var0, String var1);

    public static native int stringFindSubstr(String var0, String var1, IntBuffer var2);

    public static native Pointer arrayReplaceEachSequence(ByteBuffer var0, NativeSize var1, ByteBuffer var2, NativeSize var3, ByteBuffer var4, NativeSize var5, NativeSizeByReference var6, IntBuffer var7);

    public static native L_Dna arrayFindEachSequence(ByteBuffer var0, NativeSize var1, ByteBuffer var2, NativeSize var3);

    public static native int arrayFindSequence(ByteBuffer var0, NativeSize var1, ByteBuffer var2, NativeSize var3, IntBuffer var4, IntBuffer var5);

    public static native Pointer reallocNew(PointerByReference var0, NativeSize var1, NativeSize var2);

    public static native Pointer l_binaryRead(String var0, NativeSizeByReference var1);

    public static native Pointer l_binaryReadStream(ILeptonica.FILE var0, NativeSizeByReference var1);

    public static native Pointer l_binaryReadSelect(String var0, NativeSize var1, NativeSize var2, NativeSizeByReference var3);

    public static native Pointer l_binaryReadSelectStream(ILeptonica.FILE var0, NativeSize var1, NativeSize var2, NativeSizeByReference var3);

    public static native int l_binaryWrite(String var0, String var1, Pointer var2, NativeSize var3);

    public static native NativeSize nbytesInFile(String var0);

    public static native NativeSize fnbytesInFile(ILeptonica.FILE var0);

    public static native Pointer l_binaryCopy(ByteBuffer var0, NativeSize var1);

    public static native int l_binaryCompare(ByteBuffer var0, NativeSize var1, ByteBuffer var2, NativeSize var3, IntBuffer var4);

    public static native int fileCopy(String var0, String var1);

    public static native int fileConcatenate(String var0, String var1);

    public static native int fileAppendString(String var0, String var1);

    public static native int fileSplitLinesUniform(String var0, int var1, int var2, String var3, String var4);

    public static native ILeptonica.FILE fopenReadStream(String var0);

    public static native ILeptonica.FILE fopenWriteStream(String var0, String var1);

    public static native ILeptonica.FILE fopenReadFromMemory(ByteBuffer var0, NativeSize var1);

    public static native ILeptonica.FILE fopenWriteWinTempfile();

    public static native ILeptonica.FILE lept_fopen(String var0, String var1);

    public static native int lept_fclose(ILeptonica.FILE var0);

    public static native Pointer lept_calloc(NativeSize var0, NativeSize var1);

    public static native void lept_free(Pointer var0);

    public static native int lept_mkdir(String var0);

    public static native int lept_rmdir(String var0);

    public static native void lept_direxists(String var0, IntBuffer var1);

    public static native int lept_rm_match(String var0, String var1);

    public static native int lept_rm(String var0, String var1);

    public static native int lept_rmfile(String var0);

    public static native int lept_mv(String var0, String var1, String var2, PointerByReference var3);

    public static native int lept_cp(String var0, String var1, String var2, PointerByReference var3);

    public static native int callSystemDebug(String var0);

    public static native int splitPathAtDirectory(String var0, PointerByReference var1, PointerByReference var2);

    public static native int splitPathAtExtension(String var0, PointerByReference var1, PointerByReference var2);

    public static native Pointer pathJoin(String var0, String var1);

    public static native Pointer appendSubdirs(String var0, String var1);

    public static native int convertSepCharsInPath(ByteBuffer var0, int var1);

    public static native Pointer genPathname(String var0, String var1);

    public static native int makeTempDirname(ByteBuffer var0, NativeSize var1, String var2);

    public static native int modifyTrailingSlash(ByteBuffer var0, NativeSize var1, int var2);

    public static native Pointer l_makeTempFilename();

    public static native int extractNumberFromFilename(String var0, int var1, int var2);

    public static native Pix pixSimpleCaptcha(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixRandomHarmonicWarp(Pix var0, float var1, float var2, float var3, float var4, int var5, int var6, int var7, int var8);

    public static native Pix pixWarpStereoscopic(Pix var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static native Pix pixStretchHorizontal(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixStretchHorizontalSampled(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixStretchHorizontalLI(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixQuadraticVShear(Pix var0, int var1, int var2, int var3, int var4, int var5);

    public static native Pix pixQuadraticVShearSampled(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixQuadraticVShearLI(Pix var0, int var1, int var2, int var3, int var4);

    public static native Pix pixStereoFromPair(Pix var0, Pix var1, float var2, float var3, float var4);

    public static native L_WShed wshedCreate(Pix var0, Pix var1, int var2, int var3);

    public static native void wshedDestroy(PointerByReference var0);

    public static native int wshedApply(L_WShed var0);

    public static native int wshedBasins(L_WShed var0, PointerByReference var1, PointerByReference var2);

    public static native Pix wshedRenderFill(L_WShed var0);

    public static native Pix wshedRenderColors(L_WShed var0);

    public static native int pixaWriteWebPAnim(String var0, Pixa var1, int var2, int var3, int var4, int var5);

    public static native int pixaWriteStreamWebPAnim(ILeptonica.FILE var0, Pixa var1, int var2, int var3, int var4, int var5);

    public static native int pixaWriteMemWebPAnim(PointerByReference var0, NativeSizeByReference var1, Pixa var2, int var3, int var4, int var5, int var6);

    public static native Pix pixReadStreamWebP(ILeptonica.FILE var0);

    public static native Pix pixReadMemWebP(ByteBuffer var0, NativeSize var1);

    public static native int readHeaderWebP(String var0, IntBuffer var1, IntBuffer var2, IntBuffer var3);

    public static native int readHeaderMemWebP(ByteBuffer var0, NativeSize var1, IntBuffer var2, IntBuffer var3, IntBuffer var4);

    public static native int pixWriteWebP(String var0, Pix var1, int var2, int var3);

    public static native int pixWriteStreamWebP(ILeptonica.FILE var0, Pix var1, int var2, int var3);

    public static native int pixWriteMemWebP(PointerByReference var0, NativeSizeByReference var1, Pix var2, int var3, int var4);

    public static native int l_jpegSetQuality(int var0);

    public static native void setLeptDebugOK(int var0);

    public static native int pixaWriteFiles(String var0, Pixa var1, int var2);

    public static native int pixWriteDebug(String var0, Pix var1, int var2);

    public static native int pixWrite(String var0, Pix var1, int var2);

    public static native int pixWriteAutoFormat(String var0, Pix var1);

    public static native int pixWriteStream(ILeptonica.FILE var0, Pix var1, int var2);

    public static native int pixWriteImpliedFormat(String var0, Pix var1, int var2, int var3);

    public static native int pixChooseOutputFormat(Pix var0);

    public static native int getFormatFromExtension(String var0);

    public static native int getImpliedFileFormat(String var0);

    public static native int pixGetAutoFormat(Pix var0, IntBuffer var1);

    public static native Pointer getFormatExtension(int var0);

    public static native int pixWriteMem(PointerByReference var0, NativeSizeByReference var1, Pix var2, int var3);

    public static native int l_fileDisplay(String var0, int var1, int var2, float var3);

    public static native int pixDisplay(Pix var0, int var1, int var2);

    public static native int pixDisplayWithTitle(Pix var0, int var1, int var2, String var3, int var4);

    public static native Pix pixMakeColorSquare(int var0, int var1, int var2, int var3, int var4);

    public static native void l_chooseDisplayProg(int var0);

    public static native void changeFormatForMissingLib(IntBuffer var0);

    public static native int pixDisplayWrite(Pix var0, int var1);

    public static native Pointer zlibCompress(ByteBuffer var0, NativeSize var1, NativeSizeByReference var2);

    public static native Pointer zlibUncompress(ByteBuffer var0, NativeSize var1, NativeSizeByReference var2);

    static {
        Native.register(LoadLibs.getLeptonicaLibName());
    }
}

