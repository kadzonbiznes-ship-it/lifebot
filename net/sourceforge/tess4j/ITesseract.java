/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.tess4j.ITesseract$RenderedFormat
 *  net.sourceforge.tess4j.OCRResult
 *  net.sourceforge.tess4j.OSDResult
 *  net.sourceforge.tess4j.Word
 */
package net.sourceforge.tess4j;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import javax.imageio.IIOImage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.OCRResult;
import net.sourceforge.tess4j.OSDResult;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageIOHelper;

public interface ITesseract {
    public static final String htmlBeginTag = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n<html>\n<head>\n<title></title>\n<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n<meta name='ocr-system' content='tesseract'/>\n</head>\n<body>\n";
    public static final String htmlEndTag = "</body>\n</html>\n";
    public static final String PAGE_SEPARATOR = "page_separator";
    public static final String DOCUMENT_TITLE = "document_title";

    default public String doOCR(File imageFile) throws TesseractException {
        return this.doOCR(imageFile, null);
    }

    public String doOCR(File var1, List<Rectangle> var2) throws TesseractException;

    default public String doOCR(BufferedImage bi) throws TesseractException {
        return this.doOCR(bi, null, (List<Rectangle>)null);
    }

    default public String doOCR(BufferedImage bi, String filename, List<Rectangle> rects) throws TesseractException {
        return this.doOCR(Arrays.asList(ImageIOHelper.getIIOImage(bi)), filename, Arrays.asList(rects));
    }

    public String doOCR(List<IIOImage> var1, String var2, List<List<Rectangle>> var3) throws TesseractException;

    public String doOCR(int var1, int var2, ByteBuffer var3, int var4, String var5, List<Rectangle> var6) throws TesseractException;

    public void setDatapath(String var1);

    public void setLanguage(String var1);

    public void setOcrEngineMode(int var1);

    public void setPageSegMode(int var1);

    public void setVariable(String var1, String var2);

    public void setConfigs(List<String> var1);

    default public void createDocuments(String filename, String outputbase, List<RenderedFormat> formats) throws TesseractException {
        this.createDocuments(new String[]{filename}, new String[]{outputbase}, formats);
    }

    public void createDocuments(String[] var1, String[] var2, List<RenderedFormat> var3) throws TesseractException;

    public OCRResult createDocumentsWithResults(BufferedImage var1, String var2, String var3, List<RenderedFormat> var4, int var5) throws TesseractException;

    public List<OCRResult> createDocumentsWithResults(BufferedImage[] var1, String[] var2, String[] var3, List<RenderedFormat> var4, int var5) throws TesseractException;

    public OCRResult createDocumentsWithResults(String var1, String var2, List<RenderedFormat> var3, int var4) throws TesseractException;

    public List<OCRResult> createDocumentsWithResults(String[] var1, String[] var2, List<RenderedFormat> var3, int var4) throws TesseractException;

    public List<Rectangle> getSegmentedRegions(BufferedImage var1, int var2) throws TesseractException;

    default public List<Word> getWords(BufferedImage bi, int pageIteratorLevel) {
        return this.getWords(Arrays.asList(bi), pageIteratorLevel);
    }

    public List<Word> getWords(List<BufferedImage> var1, int var2);

    public OSDResult getOSD(File var1);

    public OSDResult getOSD(BufferedImage var1);
}

