/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.tess4j.ITesseract$RenderedFormat
 *  net.sourceforge.tess4j.OCRResult
 *  net.sourceforge.tess4j.OSDResult
 *  net.sourceforge.tess4j.Tesseract$1
 *  net.sourceforge.tess4j.Word
 *  org.apache.commons.io.FilenameUtils
 */
package net.sourceforge.tess4j;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.sourceforge.lept4j.Box;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Leptonica1;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.util.LeptUtils;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.OCRResult;
import net.sourceforge.tess4j.OSDResult;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.tess4j.util.LoggHelper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tesseract
implements ITesseract {
    private String language = "eng";
    private String datapath;
    private int psm = -1;
    private int ocrEngineMode = 3;
    private final Properties prop = new Properties();
    private final List<String> configList = new ArrayList<String>();
    private TessAPI api;
    private ITessAPI.TessBaseAPI handle;
    private boolean alreadyInvoked;
    private static final Logger logger = LoggerFactory.getLogger(new LoggHelper().toString());

    public Tesseract() {
        try {
            this.datapath = System.getenv("TESSDATA_PREFIX");
        }
        catch (Exception exception) {
        }
        finally {
            if (this.datapath == null) {
                this.datapath = "./";
            }
        }
    }

    protected TessAPI getAPI() {
        return this.api;
    }

    protected ITessAPI.TessBaseAPI getHandle() {
        return this.handle;
    }

    @Override
    public void setDatapath(String datapath) {
        this.datapath = datapath;
    }

    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public void setOcrEngineMode(int ocrEngineMode) {
        this.ocrEngineMode = ocrEngineMode;
    }

    @Override
    public void setPageSegMode(int mode) {
        this.psm = mode;
    }

    @Override
    public void setVariable(String key, String value) {
        this.prop.setProperty(key, value);
    }

    @Override
    public void setConfigs(List<String> configs) {
        this.configList.clear();
        if (configs != null) {
            this.configList.addAll(configs);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String doOCR(File inputFile, List<Rectangle> rects) throws TesseractException {
        try {
            File imageFile = ImageIOHelper.getImageFile(inputFile);
            String imageFileFormat = ImageIOHelper.getImageFileFormat(imageFile);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imageFileFormat);
            if (!readers.hasNext()) {
                throw new RuntimeException("Unsupported image format. May need to install JAI Image I/O package.\nhttps://github.com/jai-imageio/jai-imageio-core");
            }
            ImageReader reader = readers.next();
            StringBuilder result = new StringBuilder();
            try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile);){
                reader.setInput(iis);
                int imageTotal = reader.getNumImages(true);
                if ("pdf".equals(FilenameUtils.getExtension((String)inputFile.getName()).toLowerCase())) {
                    this.setVariable("user_defined_dpi", "300");
                }
                this.init();
                this.setVariables();
                for (int i = 0; i < imageTotal; ++i) {
                    IIOImage oimage = reader.readAll(i, reader.getDefaultReadParam());
                    result.append(this.doOCR(oimage, inputFile.getPath(), rects, i + 1));
                }
                if (String.valueOf(1).equals(this.prop.getProperty("tessedit_create_hocr"))) {
                    result.insert(0, "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n<html>\n<head>\n<title></title>\n<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n<meta name='ocr-system' content='tesseract'/>\n</head>\n<body>\n").append("</body>\n</html>\n");
                }
            }
            finally {
                if (imageFile != null && imageFile.exists() && imageFile != inputFile && imageFile.getName().startsWith("multipage") && imageFile.getName().endsWith(".tif")) {
                    imageFile.delete();
                }
                reader.dispose();
                this.dispose();
            }
            return result.toString();
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new TesseractException(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String doOCR(List<IIOImage> imageList, String filename, List<List<Rectangle>> roiss) throws TesseractException {
        this.init();
        this.setVariables();
        try {
            StringBuilder sb = new StringBuilder();
            int pageNum = 0;
            for (IIOImage oimage : imageList) {
                List<Rectangle> rois = roiss == null || roiss.isEmpty() || pageNum >= roiss.size() ? null : roiss.get(pageNum);
                sb.append(this.doOCR(oimage, filename, rois, ++pageNum));
            }
            if (String.valueOf(1).equals(this.prop.getProperty("tessedit_create_hocr"))) {
                sb.insert(0, "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n<html>\n<head>\n<title></title>\n<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n<meta name='ocr-system' content='tesseract'/>\n</head>\n<body>\n").append("</body>\n</html>\n");
            }
            String string = sb.toString();
            return string;
        }
        finally {
            this.dispose();
        }
    }

    private String doOCR(IIOImage oimage, String filename, List<Rectangle> rois, int pageNum) throws TesseractException {
        StringBuilder sb = new StringBuilder();
        try {
            this.setImage(oimage.getRenderedImage());
            if (rois != null && !rois.isEmpty()) {
                for (Rectangle rect : rois) {
                    this.setROI(rect);
                    sb.append(this.getOCRText(filename, pageNum));
                }
            } else {
                sb.append(this.getOCRText(filename, pageNum));
            }
        }
        catch (IOException ioe) {
            logger.warn(ioe.getMessage(), ioe);
        }
        return sb.toString();
    }

    @Override
    public String doOCR(int xsize, int ysize, ByteBuffer buf, int bpp, String filename, List<Rectangle> rects) throws TesseractException {
        this.init();
        this.setVariables();
        try {
            StringBuilder sb = new StringBuilder();
            this.setImage(xsize, ysize, buf, bpp);
            if (rects != null && !rects.isEmpty()) {
                for (Rectangle rect : rects) {
                    this.setROI(rect);
                    sb.append(this.getOCRText(filename, 1));
                }
            } else {
                sb.append(this.getOCRText(filename, 1));
            }
            String string = sb.toString();
            return string;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new TesseractException(e);
        }
        finally {
            this.dispose();
        }
    }

    protected void init() {
        this.api = TessAPI.INSTANCE;
        this.handle = this.api.TessBaseAPICreate();
        StringArray sarray = new StringArray(this.configList.toArray(new String[0]));
        PointerByReference configs = new PointerByReference();
        configs.setPointer(sarray);
        this.api.TessBaseAPIInit1(this.handle, this.datapath, this.language, this.ocrEngineMode, configs, this.configList.size());
        if (this.psm > -1) {
            this.api.TessBaseAPISetPageSegMode(this.handle, this.psm);
        }
        this.validateDatapathAndLanguagePacks();
    }

    void validateDatapathAndLanguagePacks() {
        String dataPath = this.api.TessBaseAPIGetDatapath(this.handle);
        if (!new File(dataPath).exists()) {
            throw new IllegalArgumentException("Specified datapath " + dataPath + " does not exist.");
        }
        Pointer ptr = this.api.TessBaseAPIGetLoadedLanguagesAsVector(this.handle).getPointer();
        String[] loadedLangs = ptr.getStringArray(0L);
        PointerByReference pref = new PointerByReference();
        pref.setPointer(ptr);
        this.api.TessDeleteTextArray(pref);
        ptr = this.api.TessBaseAPIGetAvailableLanguagesAsVector(this.handle).getPointer();
        String[] availLangs = ptr.getStringArray(0L);
        pref.setPointer(ptr);
        this.api.TessDeleteTextArray(pref);
        if (!Arrays.asList(availLangs).containsAll(Arrays.asList(loadedLangs))) {
            throw new IllegalArgumentException("Specified language data does not exist.");
        }
    }

    protected void setVariables() {
        Enumeration<?> em = this.prop.propertyNames();
        while (em.hasMoreElements()) {
            String key = (String)em.nextElement();
            this.api.TessBaseAPISetVariable(this.handle, key, this.prop.getProperty(key));
        }
    }

    protected void setImage(RenderedImage image) throws IOException {
        Pix pix = null;
        try {
            pix = LeptUtils.convertImageToPix((BufferedImage)image);
            this.api.TessBaseAPISetImage2(this.handle, pix);
        }
        finally {
            LeptUtils.dispose(pix);
        }
    }

    protected void setImage(int xsize, int ysize, ByteBuffer buf, int bpp) {
        int bytespp = bpp / 8;
        int bytespl = (int)Math.ceil((double)(xsize * bpp) / 8.0);
        this.api.TessBaseAPISetImage(this.handle, buf, xsize, ysize, bytespp, bytespl);
    }

    protected void setROI(Rectangle rect) {
        if (rect != null && !rect.isEmpty()) {
            this.api.TessBaseAPISetRectangle(this.handle, rect.x, rect.y, rect.width, rect.height);
        }
    }

    protected String getOCRText(String filename, int pageNum) {
        if (filename != null && !filename.isEmpty()) {
            this.api.TessBaseAPISetInputName(this.handle, filename);
        }
        Pointer textPtr = String.valueOf(1).equals(this.prop.getProperty("tessedit_create_hocr")) ? this.api.TessBaseAPIGetHOCRText(this.handle, pageNum - 1) : (String.valueOf(1).equals(this.prop.getProperty("tessedit_write_unlv")) ? this.api.TessBaseAPIGetUNLVText(this.handle) : (String.valueOf(1).equals(this.prop.getProperty("tessedit_create_alto")) ? this.api.TessBaseAPIGetAltoText(this.handle, pageNum - 1) : (String.valueOf(1).equals(this.prop.getProperty("tessedit_create_page_xml")) ? this.api.TessBaseAPIGetPAGEText(this.handle, pageNum - 1) : (String.valueOf(1).equals(this.prop.getProperty("tessedit_create_lstmbox")) ? this.api.TessBaseAPIGetLSTMBoxText(this.handle, pageNum - 1) : (String.valueOf(1).equals(this.prop.getProperty("tessedit_create_tsv")) ? this.api.TessBaseAPIGetTsvText(this.handle, pageNum - 1) : (String.valueOf(1).equals(this.prop.getProperty("tessedit_create_wordstrbox")) ? this.api.TessBaseAPIGetWordStrBoxText(this.handle, pageNum - 1) : this.api.TessBaseAPIGetUTF8Text(this.handle)))))));
        String str = textPtr.getString(0L);
        this.api.TessDeleteText(textPtr);
        return str;
    }

    private ITessAPI.TessResultRenderer createRenderers(String outputbase, List<ITesseract.RenderedFormat> formats) {
        ITessAPI.TessResultRenderer renderer = null;
        for (ITesseract.RenderedFormat format : formats) {
            switch (1.$SwitchMap$net$sourceforge$tess4j$ITesseract$RenderedFormat[format.ordinal()]) {
                case 1: {
                    if (renderer == null) {
                        renderer = this.api.TessTextRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessTextRendererCreate(outputbase));
                    break;
                }
                case 2: {
                    if (renderer == null) {
                        renderer = this.api.TessHOcrRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessHOcrRendererCreate(outputbase));
                    break;
                }
                case 3: 
                case 4: {
                    boolean textonly;
                    String dataPath = this.api.TessBaseAPIGetDatapath(this.handle);
                    boolean bl = textonly = String.valueOf(1).equals(this.prop.getProperty("textonly_pdf")) || format == ITesseract.RenderedFormat.PDF_TEXTONLY;
                    if (renderer == null) {
                        renderer = this.api.TessPDFRendererCreate(outputbase, dataPath, textonly ? 1 : 0);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessPDFRendererCreate(outputbase, dataPath, textonly ? 1 : 0));
                    break;
                }
                case 5: {
                    if (renderer == null) {
                        renderer = this.api.TessBoxTextRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessBoxTextRendererCreate(outputbase));
                    break;
                }
                case 6: {
                    if (renderer == null) {
                        renderer = this.api.TessUnlvRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessUnlvRendererCreate(outputbase));
                    break;
                }
                case 7: {
                    if (renderer == null) {
                        renderer = this.api.TessAltoRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessAltoRendererCreate(outputbase));
                    break;
                }
                case 8: {
                    if (renderer == null) {
                        renderer = this.api.TessPAGERendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessPAGERendererCreate(outputbase));
                    break;
                }
                case 9: {
                    if (renderer == null) {
                        renderer = this.api.TessTsvRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessTsvRendererCreate(outputbase));
                    break;
                }
                case 10: {
                    if (renderer == null) {
                        renderer = this.api.TessLSTMBoxRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessLSTMBoxRendererCreate(outputbase));
                    break;
                }
                case 11: {
                    if (renderer == null) {
                        renderer = this.api.TessWordStrBoxRendererCreate(outputbase);
                        break;
                    }
                    this.api.TessResultRendererInsert(renderer, this.api.TessWordStrBoxRendererCreate(outputbase));
                }
            }
        }
        return renderer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void createDocuments(String[] filenames, String[] outputbases, List<ITesseract.RenderedFormat> formats) throws TesseractException {
        if (filenames.length != outputbases.length) {
            throw new RuntimeException("The two arrays must match in length.");
        }
        this.init();
        this.setVariables();
        try {
            for (int i = 0; i < filenames.length; ++i) {
                File inputFile = new File(filenames[i]);
                File imageFile = null;
                try {
                    imageFile = ImageIOHelper.getImageFile(inputFile);
                    ITessAPI.TessResultRenderer renderer = this.createRenderers(outputbases[i], formats);
                    this.createDocuments(imageFile.getPath(), renderer);
                    this.api.TessDeleteResultRenderer(renderer);
                    continue;
                }
                catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    continue;
                }
                finally {
                    if (imageFile != null && imageFile.exists() && imageFile != inputFile && imageFile.getName().startsWith("multipage") && imageFile.getName().endsWith(".tif")) {
                        imageFile.delete();
                    }
                }
            }
        }
        finally {
            this.dispose();
        }
    }

    private int createDocuments(String filename, ITessAPI.TessResultRenderer renderer) throws TesseractException {
        this.api.TessBaseAPISetInputName(this.handle, filename);
        int result = this.api.TessBaseAPIProcessPages(this.handle, filename, null, 0, renderer);
        return this.api.TessBaseAPIMeanTextConf(this.handle);
    }

    private int createDocuments(BufferedImage bi, String filename, ITessAPI.TessResultRenderer renderer) throws Exception {
        Pix pix = LeptUtils.convertImageToPix(bi);
        String title = this.api.TessBaseAPIGetStringVariable(this.handle, "document_title");
        this.api.TessResultRendererBeginDocument(renderer, title);
        int result = this.api.TessBaseAPIProcessPage(this.handle, pix, 0, filename, null, 0, renderer);
        this.api.TessResultRendererEndDocument(renderer);
        LeptUtils.dispose(pix);
        return this.api.TessBaseAPIMeanTextConf(this.handle);
    }

    @Override
    public List<Rectangle> getSegmentedRegions(BufferedImage bi, int pageIteratorLevel) throws TesseractException {
        this.init();
        this.setVariables();
        try {
            ArrayList<Rectangle> list = new ArrayList<Rectangle>();
            this.setImage(bi);
            Boxa boxes = this.api.TessBaseAPIGetComponentImages(this.handle, pageIteratorLevel, 1, null, null);
            int boxCount = Leptonica1.boxaGetCount(boxes);
            for (int i = 0; i < boxCount; ++i) {
                Box box = Leptonica1.boxaGetBox(boxes, i, 2);
                if (box == null) continue;
                list.add(new Rectangle(box.x, box.y, box.w, box.h));
                PointerByReference pRef = new PointerByReference();
                pRef.setValue(box.getPointer());
                Leptonica1.boxDestroy(pRef);
            }
            if (boxes != null) {
                PointerByReference pRef = new PointerByReference();
                pRef.setValue(boxes.getPointer());
                Leptonica1.boxaDestroy(pRef);
            }
            ArrayList<Rectangle> arrayList = list;
            return arrayList;
        }
        catch (IOException ioe) {
            logger.warn(ioe.getMessage(), ioe);
            throw new TesseractException(ioe);
        }
        finally {
            this.dispose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public List<Word> getWords(List<BufferedImage> biList, int pageIteratorLevel) {
        if (!this.alreadyInvoked) {
            this.init();
            this.setVariables();
        }
        String pageSeparator = this.api.TessBaseAPIGetStringVariable(this.handle, "page_separator");
        ArrayList<Word> words = new ArrayList<Word>();
        try {
            for (BufferedImage bi : biList) {
                this.setImage(bi);
                this.api.TessBaseAPIRecognize(this.handle, null);
                ITessAPI.TessResultIterator ri = this.api.TessBaseAPIGetIterator(this.handle);
                ITessAPI.TessPageIterator pi = this.api.TessResultIteratorGetPageIterator(ri);
                this.api.TessPageIteratorBegin(pi);
                do {
                    Pointer ptr;
                    if ((ptr = this.api.TessResultIteratorGetUTF8Text(ri, pageIteratorLevel)) == null) continue;
                    String text = ptr.getString(0L);
                    this.api.TessDeleteText(ptr);
                    float confidence = this.api.TessResultIteratorConfidence(ri, pageIteratorLevel);
                    IntBuffer leftB = IntBuffer.allocate(1);
                    IntBuffer topB = IntBuffer.allocate(1);
                    IntBuffer rightB = IntBuffer.allocate(1);
                    IntBuffer bottomB = IntBuffer.allocate(1);
                    this.api.TessPageIteratorBoundingBox(pi, pageIteratorLevel, leftB, topB, rightB, bottomB);
                    int left = leftB.get();
                    int top = topB.get();
                    int right = rightB.get();
                    int bottom = bottomB.get();
                    Word word = new Word(text, confidence, new Rectangle(left, top, right - left, bottom - top));
                    words.add(word);
                } while (this.api.TessPageIteratorNext(pi, pageIteratorLevel) == 1);
                this.api.TessResultIteratorDelete(ri);
                words.add(new Word(pageSeparator, 100.0f, new Rectangle()));
            }
            if (!words.isEmpty()) {
                words.remove(words.size() - 1);
            }
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        finally {
            if (!this.alreadyInvoked) {
                this.dispose();
            }
        }
        return words;
    }

    @Override
    public OCRResult createDocumentsWithResults(BufferedImage bi, String filename, String outputbase, List<ITesseract.RenderedFormat> formats, int pageIteratorLevel) throws TesseractException {
        List<OCRResult> results = this.createDocumentsWithResults(new BufferedImage[]{bi}, new String[]{filename}, new String[]{outputbase}, formats, pageIteratorLevel);
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public List<OCRResult> createDocumentsWithResults(BufferedImage[] bis, String[] filenames, String[] outputbases, List<ITesseract.RenderedFormat> formats, int pageIteratorLevel) throws TesseractException {
        if (bis.length != filenames.length || bis.length != outputbases.length) {
            throw new RuntimeException("The three arrays must match in length.");
        }
        this.init();
        this.setVariables();
        ArrayList<OCRResult> results = new ArrayList<OCRResult>();
        try {
            for (int i = 0; i < bis.length; ++i) {
                try {
                    ITessAPI.TessResultRenderer renderer = this.createRenderers(outputbases[i], formats);
                    int meanTextConfidence = this.createDocuments(bis[i], filenames[i], renderer);
                    this.api.TessDeleteResultRenderer(renderer);
                    List<Object> words = meanTextConfidence > 0 ? this.getRecognizedWords(pageIteratorLevel) : new ArrayList();
                    results.add(new OCRResult(meanTextConfidence, words));
                    continue;
                }
                catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        finally {
            this.dispose();
        }
        return results;
    }

    @Override
    public OCRResult createDocumentsWithResults(String filename, String outputbase, List<ITesseract.RenderedFormat> formats, int pageIteratorLevel) throws TesseractException {
        List<OCRResult> results = this.createDocumentsWithResults(new String[]{filename}, new String[]{outputbase}, formats, pageIteratorLevel);
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public List<OCRResult> createDocumentsWithResults(String[] filenames, String[] outputbases, List<ITesseract.RenderedFormat> formats, int pageIteratorLevel) throws TesseractException {
        if (filenames.length != outputbases.length) {
            throw new RuntimeException("The two arrays must match in length.");
        }
        this.init();
        this.setVariables();
        ArrayList<OCRResult> results = new ArrayList<OCRResult>();
        try {
            for (int i = 0; i < filenames.length; ++i) {
                File inputFile = new File(filenames[i]);
                File imageFile = null;
                try {
                    imageFile = ImageIOHelper.getImageFile(inputFile);
                    ITessAPI.TessResultRenderer renderer = this.createRenderers(outputbases[i], formats);
                    int meanTextConfidence = this.createDocuments(imageFile.getPath(), renderer);
                    this.api.TessDeleteResultRenderer(renderer);
                    List<Object> words = meanTextConfidence > 0 ? this.getRecognizedWords(imageFile, pageIteratorLevel) : new ArrayList();
                    results.add(new OCRResult(meanTextConfidence, words));
                    continue;
                }
                catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    continue;
                }
                finally {
                    if (imageFile != null && imageFile.exists() && imageFile != inputFile && imageFile.getName().startsWith("multipage") && imageFile.getName().endsWith(".tif")) {
                        imageFile.delete();
                    }
                }
            }
        }
        finally {
            this.dispose();
        }
        return results;
    }

    @Override
    public OSDResult getOSD(File imageFile) {
        try {
            imageFile = ImageIOHelper.getImageFile(imageFile);
            BufferedImage bi = ImageIO.read(new FileInputStream(imageFile));
            return this.getOSD(bi);
        }
        catch (IOException e) {
            logger.warn(e.getMessage(), e);
            return new OSDResult();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public OSDResult getOSD(BufferedImage bi) {
        this.init();
        this.setVariables();
        try {
            this.api.TessBaseAPIInit3(this.handle, this.datapath, "osd");
            this.setImage(bi);
            IntBuffer orient_degB = IntBuffer.allocate(1);
            FloatBuffer orient_confB = FloatBuffer.allocate(1);
            PointerByReference script_nameB = new PointerByReference();
            FloatBuffer script_confB = FloatBuffer.allocate(1);
            int result = this.api.TessBaseAPIDetectOrientationScript(this.handle, orient_degB, orient_confB, script_nameB, script_confB);
            if (result == 1) {
                int orient_deg = orient_degB.get();
                float orient_conf = orient_confB.get();
                String script_name = script_nameB.getValue().getString(0L);
                float script_conf = script_confB.get();
                OSDResult oSDResult = new OSDResult(orient_deg, orient_conf, script_name, script_conf);
                return oSDResult;
            }
        }
        catch (IOException ioe) {
            logger.warn(ioe.getMessage(), ioe);
        }
        finally {
            this.dispose();
        }
        return new OSDResult();
    }

    private List<Word> getRecognizedWords(int pageIteratorLevel) {
        ArrayList<Word> words = new ArrayList<Word>();
        try {
            ITessAPI.TessResultIterator ri = this.api.TessBaseAPIGetIterator(this.handle);
            ITessAPI.TessPageIterator pi = this.api.TessResultIteratorGetPageIterator(ri);
            this.api.TessPageIteratorBegin(pi);
            do {
                Pointer ptr;
                if ((ptr = this.api.TessResultIteratorGetUTF8Text(ri, pageIteratorLevel)) == null) continue;
                String text = ptr.getString(0L);
                this.api.TessDeleteText(ptr);
                float confidence = this.api.TessResultIteratorConfidence(ri, pageIteratorLevel);
                IntBuffer leftB = IntBuffer.allocate(1);
                IntBuffer topB = IntBuffer.allocate(1);
                IntBuffer rightB = IntBuffer.allocate(1);
                IntBuffer bottomB = IntBuffer.allocate(1);
                this.api.TessPageIteratorBoundingBox(pi, pageIteratorLevel, leftB, topB, rightB, bottomB);
                int left = leftB.get();
                int top = topB.get();
                int right = rightB.get();
                int bottom = bottomB.get();
                Word word = new Word(text, confidence, new Rectangle(left, top, right - left, bottom - top));
                words.add(word);
            } while (this.api.TessPageIteratorNext(pi, pageIteratorLevel) == 1);
            this.api.TessResultIteratorDelete(ri);
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return words;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private List<Word> getRecognizedWords(File inputFile, int pageIteratorLevel) {
        ArrayList<Word> words = new ArrayList<Word>();
        try {
            List<BufferedImage> biList = ImageIOHelper.getImageList(inputFile);
            if (biList.isEmpty()) {
                ArrayList<Word> arrayList = words;
                return arrayList;
            }
            if (biList.size() == 1) {
                List<Word> list = this.getRecognizedWords(pageIteratorLevel);
                return list;
            }
            this.alreadyInvoked = true;
            List<Word> list = this.getWords(biList, pageIteratorLevel);
            return list;
        }
        catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        finally {
            this.alreadyInvoked = false;
        }
        return words;
    }

    protected void dispose() {
        if (this.api != null && this.handle != null) {
            this.api.TessBaseAPIDelete(this.handle);
        }
    }
}

