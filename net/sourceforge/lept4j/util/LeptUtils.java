/*
 * Decompiled with CFR 0.152.
 */
package net.sourceforge.lept4j.util;

import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import net.sourceforge.lept4j.Box;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Boxaa;
import net.sourceforge.lept4j.CCBorda;
import net.sourceforge.lept4j.DPix;
import net.sourceforge.lept4j.DoubleLinkedList;
import net.sourceforge.lept4j.FPix;
import net.sourceforge.lept4j.FPixa;
import net.sourceforge.lept4j.GPlot;
import net.sourceforge.lept4j.JbClasser;
import net.sourceforge.lept4j.JbData;
import net.sourceforge.lept4j.L_Bmf;
import net.sourceforge.lept4j.L_ByteBuffer;
import net.sourceforge.lept4j.L_Bytea;
import net.sourceforge.lept4j.L_Dewarp;
import net.sourceforge.lept4j.L_Dewarpa;
import net.sourceforge.lept4j.L_Dna;
import net.sourceforge.lept4j.L_DnaHash;
import net.sourceforge.lept4j.L_Dnaa;
import net.sourceforge.lept4j.L_Kernel;
import net.sourceforge.lept4j.L_Rbtree;
import net.sourceforge.lept4j.L_Recog;
import net.sourceforge.lept4j.L_Sudoku;
import net.sourceforge.lept4j.L_WShed;
import net.sourceforge.lept4j.Leptonica1;
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
import net.sourceforge.lept4j.Sarray;
import net.sourceforge.lept4j.Sel;
import net.sourceforge.lept4j.Sela;

public class LeptUtils {
    static final String JAI_IMAGE_WRITER_MESSAGE = "Need to install JAI Image I/O package.\nhttps://github.com/jai-imageio/jai-imageio-core";
    static final String TIFF_FORMAT = "tiff";
    static final float deg2rad = 0.017453277f;
    public static final String SEL_STR2 = "oooooC oo  ooooo";
    public static final String SEL_STR3 = "ooooooC  oo   oo   oooooo";

    public static BufferedImage convertPixToImage(Pix pix) throws IOException {
        BufferedImage bufferedImage;
        PointerByReference pointerByReference = new PointerByReference();
        NativeSizeByReference nativeSizeByReference = new NativeSizeByReference();
        int n = 4;
        Leptonica1.pixWriteMem(pointerByReference, nativeSizeByReference, pix, n);
        byte[] byArray = pointerByReference.getValue().getByteArray(0L, nativeSizeByReference.getValue().intValue());
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byArray);){
            bufferedImage = ImageIO.read(byteArrayInputStream);
        }
        Leptonica1.lept_free(pointerByReference.getValue());
        return bufferedImage;
    }

    @Deprecated
    public static Pix convertImageToPix(BufferedImage bufferedImage) throws IOException {
        return LeptUtils.convertImageToPix((RenderedImage)bufferedImage);
    }

    public static Pix convertImageToPix(RenderedImage renderedImage) throws IOException {
        ByteBuffer byteBuffer = LeptUtils.getImageByteBuffer(renderedImage);
        Pix pix = Leptonica1.pixReadMem(byteBuffer, new NativeSize((long)byteBuffer.capacity()));
        return pix;
    }

    public static ByteBuffer getImageByteBuffer(RenderedImage renderedImage) throws IOException {
        byte[] byArray;
        TIFFImageWriteParam tIFFImageWriteParam = new TIFFImageWriteParam(Locale.US);
        tIFFImageWriteParam.setCompressionMode(0);
        Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName(TIFF_FORMAT);
        if (!iterator.hasNext()) {
            throw new RuntimeException(JAI_IMAGE_WRITER_MESSAGE);
        }
        ImageWriter imageWriter = iterator.next();
        IIOMetadata iIOMetadata = imageWriter.getDefaultStreamMetadata(tIFFImageWriteParam);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (Object object = ImageIO.createImageOutputStream(byteArrayOutputStream);){
            imageWriter.setOutput(object);
            imageWriter.write(iIOMetadata, new IIOImage(renderedImage, null, null), tIFFImageWriteParam);
            imageWriter.dispose();
            object.seek(0L);
            byArray = new byte[(int)object.length()];
            object.read(byArray);
        }
        object = ByteBuffer.allocateDirect(byArray.length);
        ((ByteBuffer)object).order(ByteOrder.nativeOrder());
        ((ByteBuffer)object).put(byArray);
        ((Buffer)object).flip();
        return object;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Pix removeLines(Pix pix) {
        Pix pix2;
        Pix pix3 = null;
        Pix pix4 = null;
        Pix pix5 = null;
        Pix pix6 = null;
        Pix pix7 = null;
        Pix pix8 = null;
        Pix pix9 = null;
        Pix pix10 = null;
        try {
            pix3 = Leptonica1.pixThresholdToBinary(pix, 170);
            FloatBuffer floatBuffer = FloatBuffer.allocate(1);
            FloatBuffer floatBuffer2 = FloatBuffer.allocate(1);
            Leptonica1.pixFindSkew(pix3, floatBuffer, floatBuffer2);
            float f = floatBuffer.get();
            float f2 = floatBuffer2.get();
            pix4 = Leptonica1.pixRotateAMGray(pix, 0.017453277f * f, (byte)-1);
            pix5 = Leptonica1.pixCloseGray(pix4, 51, 1);
            pix6 = Leptonica1.pixErodeGray(pix5, 1, 5);
            pix7 = Leptonica1.pixThresholdToValue(null, pix6, 210, 255);
            pix8 = Leptonica1.pixThresholdToValue(null, pix7, 200, 0);
            pix9 = Leptonica1.pixThresholdToBinary(pix8, 210);
            Leptonica1.pixInvert(pix8, pix8);
            Pix pix11 = Leptonica1.pixAddGray(null, pix4, pix8);
            pix10 = Leptonica1.pixOpenGray(pix11, 1, 9);
            Leptonica1.pixCombineMasked(pix11, pix10, pix9);
            pix2 = pix11;
        }
        catch (Throwable throwable) {
            LeptUtils.disposePix(pix3);
            LeptUtils.disposePix(pix4);
            LeptUtils.disposePix(pix5);
            LeptUtils.disposePix(pix6);
            LeptUtils.disposePix(pix7);
            LeptUtils.disposePix(pix8);
            LeptUtils.disposePix(pix9);
            LeptUtils.disposePix(pix10);
            throw throwable;
        }
        LeptUtils.disposePix(pix3);
        LeptUtils.disposePix(pix4);
        LeptUtils.disposePix(pix5);
        LeptUtils.disposePix(pix6);
        LeptUtils.disposePix(pix7);
        LeptUtils.disposePix(pix8);
        LeptUtils.disposePix(pix9);
        LeptUtils.disposePix(pix10);
        return pix2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String removeLines(String string) throws IOException {
        Object object;
        Pixa pixa = null;
        Pixa pixa2 = null;
        try {
            if (string.toLowerCase().endsWith(".tif") || string.toLowerCase().endsWith(".tiff")) {
                pixa = Leptonica1.pixaReadMultipageTiff(string);
            } else {
                pixa = Leptonica1.pixaCreate(1);
                Pix pix = Leptonica1.pixRead(string);
                Leptonica1.pixaAddPix(pixa, pix, 1);
                LeptUtils.dispose(pix);
            }
            int n = Leptonica1.pixaGetCount(pixa);
            pixa2 = Leptonica1.pixaCreate(n);
            for (int i = 0; i < n; ++i) {
                Pix pix = null;
                object = null;
                Pix pix2 = null;
                Pix pix3 = null;
                Pix pix4 = null;
                Pix pix5 = null;
                try {
                    pix = Leptonica1.pixaGetPix(pixa, i, 1);
                    int n2 = Leptonica1.pixGetDepth(pix);
                    if (n2 != 8) {
                        pix5 = Leptonica1.pixConvertTo8(pix, 0);
                    }
                    object = LeptUtils.removeLines(pix5 != null ? pix5 : pix);
                    pix2 = Leptonica1.pixRotate90((Pix)object, 1);
                    pix3 = LeptUtils.removeLines(pix2);
                    pix4 = Leptonica1.pixRotate90(pix3, -1);
                    Leptonica1.pixaAddPix(pixa2, pix4, 1);
                }
                catch (Throwable throwable) {
                    LeptUtils.dispose(pix);
                    LeptUtils.dispose(pix5);
                    LeptUtils.dispose((Structure)object);
                    LeptUtils.dispose(pix2);
                    LeptUtils.dispose(pix3);
                    LeptUtils.dispose(pix4);
                    throw throwable;
                }
                LeptUtils.dispose(pix);
                LeptUtils.dispose(pix5);
                LeptUtils.dispose((Structure)object);
                LeptUtils.dispose(pix2);
                LeptUtils.dispose(pix3);
                LeptUtils.dispose(pix4);
            }
            File file = File.createTempFile("temp", ".tif");
            int n3 = Leptonica1.pixaWriteMultipageTiff(file.getPath(), pixa2);
            object = file.getPath();
        }
        catch (Throwable throwable) {
            LeptUtils.dispose(pixa);
            LeptUtils.dispose(pixa2);
            throw throwable;
        }
        LeptUtils.dispose(pixa);
        LeptUtils.dispose(pixa2);
        return object;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Pix despeckle(Pix pix, String string, int n) {
        Pix pix2;
        Pix pix3 = null;
        Pix pix4 = null;
        Pix pix5 = null;
        Pix pix6 = null;
        Pix pix7 = null;
        Sel sel = null;
        Sel sel2 = null;
        try {
            Pix pix8;
            pix3 = Leptonica1.pixBackgroundNormFlex(pix, 7, 7, 1, 1, 10);
            pix4 = Leptonica1.pixGammaTRCMasked(null, pix3, null, 1.0f, 100, 175);
            pix5 = Leptonica1.pixThresholdToBinary(pix4, 180);
            sel = Leptonica1.selCreateFromString(string, n + 2, n + 2, "speckle" + n);
            pix6 = Leptonica1.pixHMT(null, pix5, sel.getPointer());
            sel2 = Leptonica1.selCreateBrick(n, n, 0, 0, 1);
            pix7 = Leptonica1.pixDilate(null, pix6, sel2.getPointer());
            pix2 = pix8 = Leptonica1.pixSubtract(null, pix5, pix7);
        }
        catch (Throwable throwable) {
            LeptUtils.dispose(sel);
            LeptUtils.dispose(sel2);
            LeptUtils.dispose(pix3);
            LeptUtils.dispose(pix4);
            LeptUtils.dispose(pix5);
            LeptUtils.dispose(pix6);
            LeptUtils.dispose(pix7);
            throw throwable;
        }
        LeptUtils.dispose(sel);
        LeptUtils.dispose(sel2);
        LeptUtils.dispose(pix3);
        LeptUtils.dispose(pix4);
        LeptUtils.dispose(pix5);
        LeptUtils.dispose(pix6);
        LeptUtils.dispose(pix7);
        return pix2;
    }

    public static void disposePix(Pix pix) {
        if (pix == null) {
            return;
        }
        PointerByReference pointerByReference = new PointerByReference();
        pointerByReference.setValue(pix.getPointer());
        Leptonica1.pixDestroy(pointerByReference);
    }

    public static void dispose(Structure structure) {
        if (structure == null) {
            return;
        }
        PointerByReference pointerByReference = new PointerByReference();
        pointerByReference.setValue(structure.getPointer());
        if (structure instanceof Pix) {
            Leptonica1.pixDestroy(pointerByReference);
        } else if (structure instanceof Pixa) {
            Leptonica1.pixaDestroy(pointerByReference);
        } else if (structure instanceof Box) {
            Leptonica1.boxDestroy(pointerByReference);
        } else if (structure instanceof Boxa) {
            Leptonica1.boxaDestroy(pointerByReference);
        } else if (structure instanceof L_Bmf) {
            Leptonica1.bmfDestroy(pointerByReference);
        } else if (structure instanceof L_ByteBuffer) {
            Leptonica1.bbufferDestroy(pointerByReference);
        } else if (structure instanceof Boxaa) {
            Leptonica1.boxaaDestroy(pointerByReference);
        } else if (structure instanceof L_Bytea) {
            Leptonica1.l_byteaDestroy(pointerByReference);
        } else if (structure instanceof CCBorda) {
            Leptonica1.ccbaDestroy(pointerByReference);
        } else if (structure instanceof PixColormap) {
            Leptonica1.pixcmapDestroy(pointerByReference);
        } else if (structure instanceof L_Dewarp) {
            Leptonica1.dewarpDestroy(pointerByReference);
        } else if (structure instanceof L_Dewarpa) {
            Leptonica1.dewarpaDestroy(pointerByReference);
        } else if (structure instanceof L_Dna) {
            Leptonica1.l_dnaDestroy(pointerByReference);
        } else if (structure instanceof L_Dnaa) {
            Leptonica1.l_dnaaDestroy(pointerByReference);
        } else if (structure instanceof L_DnaHash) {
            Leptonica1.l_dnaHashDestroy(pointerByReference);
        } else if (structure instanceof FPix) {
            Leptonica1.fpixDestroy(pointerByReference);
        } else if (structure instanceof FPixa) {
            Leptonica1.fpixaDestroy(pointerByReference);
        } else if (structure instanceof DPix) {
            Leptonica1.dpixDestroy(pointerByReference);
        } else if (structure instanceof GPlot) {
            Leptonica1.gplotDestroy(pointerByReference);
        } else if (structure instanceof JbClasser) {
            Leptonica1.jbClasserDestroy(pointerByReference);
        } else if (structure instanceof JbData) {
            Leptonica1.jbDataDestroy(pointerByReference);
        } else if (structure instanceof L_Kernel) {
            Leptonica1.kernelDestroy(pointerByReference);
        } else if (structure instanceof Numa) {
            Leptonica1.numaDestroy(pointerByReference);
        } else if (structure instanceof Numaa) {
            Leptonica1.numaaDestroy(pointerByReference);
        } else if (structure instanceof Pixaa) {
            Leptonica1.pixaaDestroy(pointerByReference);
        } else if (structure instanceof Pixacc) {
            Leptonica1.pixaccDestroy(pointerByReference);
        } else if (structure instanceof PixComp) {
            Leptonica1.pixcompDestroy(pointerByReference);
        } else if (structure instanceof PixaComp) {
            Leptonica1.pixacompDestroy(pointerByReference);
        } else if (structure instanceof PixTiling) {
            Leptonica1.pixTilingDestroy(pointerByReference);
        } else if (structure instanceof Pta) {
            Leptonica1.ptaDestroy(pointerByReference);
        } else if (structure instanceof Ptaa) {
            Leptonica1.ptaaDestroy(pointerByReference);
        } else if (structure instanceof L_Recog) {
            Leptonica1.recogDestroy(pointerByReference);
        } else if (structure instanceof Sarray) {
            Leptonica1.sarrayDestroy(pointerByReference);
        } else if (structure instanceof Sel) {
            Leptonica1.selDestroy(pointerByReference);
        } else if (structure instanceof Sela) {
            Leptonica1.selaDestroy(pointerByReference);
        } else if (structure instanceof L_Sudoku) {
            Leptonica1.sudokuDestroy(pointerByReference);
        } else if (structure instanceof L_WShed) {
            Leptonica1.wshedDestroy(pointerByReference);
        } else if (structure instanceof DoubleLinkedList) {
            Leptonica1.listDestroy(pointerByReference);
        } else if (structure instanceof L_Rbtree) {
            Leptonica1.l_rbtreeDestroy(pointerByReference);
        } else {
            throw new RuntimeException("Not supported.");
        }
    }
}

