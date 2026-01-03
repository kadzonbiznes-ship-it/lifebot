/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;

public class ColorConvertOp
implements BufferedImageOp,
RasterOp {
    ICC_Profile[] profileList;
    ColorSpace[] CSList;
    ColorTransform thisTransform;
    ColorTransform thisRasterTransform;
    ICC_Profile thisSrcProfile;
    ICC_Profile thisDestProfile;
    RenderingHints hints;
    boolean gotProfiles;
    float[] srcMinVals;
    float[] srcMaxVals;
    float[] dstMinVals;
    float[] dstMaxVals;

    public ColorConvertOp(RenderingHints hints) {
        this.profileList = new ICC_Profile[0];
        this.hints = hints;
    }

    public ColorConvertOp(ColorSpace cspace, RenderingHints hints) {
        if (cspace == null) {
            throw new NullPointerException("ColorSpace cannot be null");
        }
        if (cspace instanceof ICC_ColorSpace) {
            this.profileList = new ICC_Profile[1];
            this.profileList[0] = ((ICC_ColorSpace)cspace).getProfile();
        } else {
            this.CSList = new ColorSpace[1];
            this.CSList[0] = cspace;
        }
        this.hints = hints;
    }

    public ColorConvertOp(ColorSpace srcCspace, ColorSpace dstCspace, RenderingHints hints) {
        if (srcCspace == null || dstCspace == null) {
            throw new NullPointerException("ColorSpaces cannot be null");
        }
        if (srcCspace instanceof ICC_ColorSpace && dstCspace instanceof ICC_ColorSpace) {
            this.profileList = new ICC_Profile[2];
            this.profileList[0] = ((ICC_ColorSpace)srcCspace).getProfile();
            this.profileList[1] = ((ICC_ColorSpace)dstCspace).getProfile();
            this.getMinMaxValsFromColorSpaces(srcCspace, dstCspace);
        } else {
            this.CSList = new ColorSpace[2];
            this.CSList[0] = srcCspace;
            this.CSList[1] = dstCspace;
        }
        this.hints = hints;
    }

    public ColorConvertOp(ICC_Profile[] profiles, RenderingHints hints) {
        if (profiles == null) {
            throw new NullPointerException("Profiles cannot be null");
        }
        this.gotProfiles = true;
        this.profileList = new ICC_Profile[profiles.length];
        for (int i1 = 0; i1 < profiles.length; ++i1) {
            this.profileList[i1] = profiles[i1];
        }
        this.hints = hints;
    }

    public final ICC_Profile[] getICC_Profiles() {
        if (this.gotProfiles) {
            ICC_Profile[] profiles = new ICC_Profile[this.profileList.length];
            for (int i1 = 0; i1 < this.profileList.length; ++i1) {
                profiles[i1] = this.profileList[i1];
            }
            return profiles;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final BufferedImage filter(BufferedImage src, BufferedImage dest) {
        ColorSpace destColorSpace;
        BufferedImage savdest = null;
        if (src.getColorModel() instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)src.getColorModel();
            src = icm.convertToIntDiscrete(src.getRaster(), true);
        }
        ColorSpace srcColorSpace = src.getColorModel().getColorSpace();
        if (dest != null) {
            if (dest.getColorModel() instanceof IndexColorModel) {
                savdest = dest;
                dest = null;
                destColorSpace = null;
            } else {
                destColorSpace = dest.getColorModel().getColorSpace();
            }
        } else {
            destColorSpace = null;
        }
        dest = this.CSList != null || !(srcColorSpace instanceof ICC_ColorSpace) || dest != null && !(destColorSpace instanceof ICC_ColorSpace) ? this.nonICCBIFilter(src, srcColorSpace, dest, destColorSpace) : this.ICCBIFilter(src, srcColorSpace, dest, destColorSpace);
        if (savdest != null) {
            Graphics2D big = savdest.createGraphics();
            try {
                big.drawImage((Image)dest, 0, 0, null);
            }
            finally {
                big.dispose();
            }
            return savdest;
        }
        return dest;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private BufferedImage ICCBIFilter(BufferedImage src, ColorSpace srcColorSpace, BufferedImage dest, ColorSpace destColorSpace) {
        int nProfiles = this.profileList.length;
        ICC_Profile srcProfile = null;
        ICC_Profile destProfile = null;
        srcProfile = ((ICC_ColorSpace)srcColorSpace).getProfile();
        if (dest == null) {
            if (nProfiles == 0) {
                throw new IllegalArgumentException("Destination ColorSpace is undefined");
            }
            destProfile = this.profileList[nProfiles - 1];
            dest = this.createCompatibleDestImage(src, null);
        } else {
            if (src.getHeight() != dest.getHeight() || src.getWidth() != dest.getWidth()) {
                throw new IllegalArgumentException("Width or height of BufferedImages do not match");
            }
            destProfile = ((ICC_ColorSpace)destColorSpace).getProfile();
        }
        if (srcProfile == destProfile) {
            boolean noTrans = true;
            for (int i = 0; i < nProfiles; ++i) {
                if (srcProfile == this.profileList[i]) continue;
                noTrans = false;
                break;
            }
            if (noTrans) {
                Graphics2D g = dest.createGraphics();
                try {
                    g.drawImage((Image)src, 0, 0, null);
                }
                finally {
                    g.dispose();
                }
                return dest;
            }
        }
        if (this.thisTransform == null || this.thisSrcProfile != srcProfile || this.thisDestProfile != destProfile) {
            this.updateBITransform(srcProfile, destProfile);
        }
        this.thisTransform.colorConvert(src, dest);
        return dest;
    }

    private void updateBITransform(ICC_Profile srcProfile, ICC_Profile destProfile) {
        int nProfiles;
        boolean useSrc = false;
        boolean useDest = false;
        int nTransforms = nProfiles = this.profileList.length;
        if (nProfiles == 0 || srcProfile != this.profileList[0]) {
            ++nTransforms;
            useSrc = true;
        }
        if (nProfiles == 0 || destProfile != this.profileList[nProfiles - 1] || nTransforms < 2) {
            ++nTransforms;
            useDest = true;
        }
        ICC_Profile[] theProfiles = new ICC_Profile[nTransforms];
        int idx = 0;
        if (useSrc) {
            theProfiles[idx++] = srcProfile;
        }
        for (int i1 = 0; i1 < nProfiles; ++i1) {
            theProfiles[idx++] = this.profileList[i1];
        }
        if (useDest) {
            theProfiles[idx] = destProfile;
        }
        int renderingIntent = theProfiles[0].getProfileClass() == 2 ? 1 : 0;
        PCMM mdl = CMSManager.getModule();
        this.thisTransform = mdl.createTransform(renderingIntent, theProfiles);
        this.thisSrcProfile = srcProfile;
        this.thisDestProfile = destProfile;
    }

    @Override
    public final WritableRaster filter(Raster src, WritableRaster dest) {
        if (this.CSList != null) {
            return this.nonICCRasterFilter(src, dest);
        }
        int nProfiles = this.profileList.length;
        if (nProfiles < 2) {
            throw new IllegalArgumentException("Source or Destination ColorSpace is undefined");
        }
        if (src.getNumBands() != this.profileList[0].getNumComponents()) {
            throw new IllegalArgumentException("Numbers of source Raster bands and source color space components do not match");
        }
        if (dest == null) {
            dest = this.createCompatibleDestRaster(src);
        } else {
            if (src.getHeight() != dest.getHeight() || src.getWidth() != dest.getWidth()) {
                throw new IllegalArgumentException("Width or height of Rasters do not match");
            }
            if (dest.getNumBands() != this.profileList[nProfiles - 1].getNumComponents()) {
                throw new IllegalArgumentException("Numbers of destination Raster bands and destination color space components do not match");
            }
        }
        if (this.thisRasterTransform == null) {
            int renderingIntent = this.profileList[0].getProfileClass() == 2 ? 1 : 0;
            PCMM mdl = CMSManager.getModule();
            this.thisRasterTransform = mdl.createTransform(renderingIntent, this.profileList);
        }
        int srcTransferType = src.getTransferType();
        int dstTransferType = dest.getTransferType();
        if (srcTransferType == 4 || srcTransferType == 5 || dstTransferType == 4 || dstTransferType == 5) {
            if (this.srcMinVals == null) {
                this.getMinMaxValsFromProfiles(this.profileList[0], this.profileList[nProfiles - 1]);
            }
            this.thisRasterTransform.colorConvert(src, dest, this.srcMinVals, this.srcMaxVals, this.dstMinVals, this.dstMaxVals);
        } else {
            this.thisRasterTransform.colorConvert(src, dest);
        }
        return dest;
    }

    @Override
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return this.getBounds2D(src.getRaster());
    }

    @Override
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        ColorSpace cs = null;
        if (destCM == null) {
            if (this.CSList == null) {
                int nProfiles = this.profileList.length;
                if (nProfiles == 0) {
                    throw new IllegalArgumentException("Destination ColorSpace is undefined");
                }
                ICC_Profile destProfile = this.profileList[nProfiles - 1];
                cs = ColorConvertOp.createCompatibleColorSpace(destProfile);
            } else {
                int nSpaces = this.CSList.length;
                cs = this.CSList[nSpaces - 1];
            }
        }
        return this.createCompatibleDestImage(src, destCM, cs);
    }

    private static ColorSpace createCompatibleColorSpace(ICC_Profile profile) {
        if (profile == ICC_Profile.getInstance(1000)) {
            return ColorSpace.getInstance(1000);
        }
        if (profile == ICC_Profile.getInstance(1004)) {
            return ColorSpace.getInstance(1004);
        }
        if (profile == ICC_Profile.getInstance(1001)) {
            return ColorSpace.getInstance(1001);
        }
        if (profile == ICC_Profile.getInstance(1002)) {
            return ColorSpace.getInstance(1002);
        }
        if (profile == ICC_Profile.getInstance(1003)) {
            return ColorSpace.getInstance(1003);
        }
        return new ICC_ColorSpace(profile);
    }

    private BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM, ColorSpace destCS) {
        if (destCM == null) {
            ColorModel srcCM = src.getColorModel();
            int nbands = destCS.getNumComponents();
            boolean hasAlpha = srcCM.hasAlpha();
            if (hasAlpha) {
                ++nbands;
            }
            int[] nbits = new int[nbands];
            for (int i = 0; i < nbands; ++i) {
                nbits[i] = 8;
            }
            destCM = new ComponentColorModel(destCS, nbits, hasAlpha, srcCM.isAlphaPremultiplied(), srcCM.getTransparency(), 0);
        }
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage image = new BufferedImage(destCM, destCM.createCompatibleWritableRaster(w, h), destCM.isAlphaPremultiplied(), null);
        return image;
    }

    @Override
    public WritableRaster createCompatibleDestRaster(Raster src) {
        int ncomponents;
        if (this.CSList != null) {
            if (this.CSList.length != 2) {
                throw new IllegalArgumentException("Destination ColorSpace is undefined");
            }
            ncomponents = this.CSList[1].getNumComponents();
        } else {
            int nProfiles = this.profileList.length;
            if (nProfiles < 2) {
                throw new IllegalArgumentException("Destination ColorSpace is undefined");
            }
            ncomponents = this.profileList[nProfiles - 1].getNumComponents();
        }
        WritableRaster dest = Raster.createInterleavedRaster(0, src.getWidth(), src.getHeight(), ncomponents, new Point(src.getMinX(), src.getMinY()));
        return dest;
    }

    @Override
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    private int getRenderingIntent(ICC_Profile profile) {
        byte[] header = profile.getData(1751474532);
        int index = 64;
        return (header[index + 2] & 0xFF) << 8 | header[index + 3] & 0xFF;
    }

    @Override
    public final RenderingHints getRenderingHints() {
        return this.hints;
    }

    private BufferedImage nonICCBIFilter(BufferedImage src, ColorSpace srcColorSpace, BufferedImage dst, ColorSpace dstColorSpace) {
        boolean needSrcAlpha;
        int w = src.getWidth();
        int h = src.getHeight();
        ICC_ColorSpace ciespace = (ICC_ColorSpace)ColorSpace.getInstance(1001);
        if (dst == null) {
            dst = this.createCompatibleDestImage(src, null);
            dstColorSpace = dst.getColorModel().getColorSpace();
        } else if (h != dst.getHeight() || w != dst.getWidth()) {
            throw new IllegalArgumentException("Width or height of BufferedImages do not match");
        }
        WritableRaster srcRas = src.getRaster();
        WritableRaster dstRas = dst.getRaster();
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM = dst.getColorModel();
        int srcNumComp = srcCM.getNumColorComponents();
        int dstNumComp = dstCM.getNumColorComponents();
        boolean dstHasAlpha = dstCM.hasAlpha();
        boolean bl = needSrcAlpha = srcCM.hasAlpha() && dstHasAlpha;
        if (this.CSList == null && this.profileList.length != 0) {
            float[] dstColor;
            int iccDstNumComp;
            int iccSrcNumComp;
            ColorSpace cs;
            ICC_Profile dstProfile;
            boolean nonICCDst;
            ICC_Profile srcProfile;
            boolean nonICCSrc;
            if (!(srcColorSpace instanceof ICC_ColorSpace)) {
                nonICCSrc = true;
                srcProfile = ciespace.getProfile();
            } else {
                nonICCSrc = false;
                srcProfile = ((ICC_ColorSpace)srcColorSpace).getProfile();
            }
            if (!(dstColorSpace instanceof ICC_ColorSpace)) {
                nonICCDst = true;
                dstProfile = ciespace.getProfile();
            } else {
                nonICCDst = false;
                dstProfile = ((ICC_ColorSpace)dstColorSpace).getProfile();
            }
            if (this.thisTransform == null || this.thisSrcProfile != srcProfile || this.thisDestProfile != dstProfile) {
                this.updateBITransform(srcProfile, dstProfile);
            }
            float maxNum = 65535.0f;
            if (nonICCSrc) {
                cs = ciespace;
                iccSrcNumComp = 3;
            } else {
                cs = srcColorSpace;
                iccSrcNumComp = srcNumComp;
            }
            float[] srcMinVal = new float[iccSrcNumComp];
            float[] srcInvDiffMinMax = new float[iccSrcNumComp];
            for (int i = 0; i < iccSrcNumComp; ++i) {
                srcMinVal[i] = cs.getMinValue(i);
                srcInvDiffMinMax[i] = maxNum / (cs.getMaxValue(i) - srcMinVal[i]);
            }
            if (nonICCDst) {
                cs = ciespace;
                iccDstNumComp = 3;
            } else {
                cs = dstColorSpace;
                iccDstNumComp = dstNumComp;
            }
            float[] dstMinVal = new float[iccDstNumComp];
            float[] dstDiffMinMax = new float[iccDstNumComp];
            for (int i = 0; i < iccDstNumComp; ++i) {
                dstMinVal[i] = cs.getMinValue(i);
                dstDiffMinMax[i] = (cs.getMaxValue(i) - dstMinVal[i]) / maxNum;
            }
            if (dstHasAlpha) {
                size = dstNumComp + 1 > 3 ? dstNumComp + 1 : 3;
                dstColor = new float[size];
            } else {
                size = dstNumComp > 3 ? dstNumComp : 3;
                dstColor = new float[size];
            }
            short[] srcLine = new short[w * iccSrcNumComp];
            short[] dstLine = new short[w * iccDstNumComp];
            float[] alpha = null;
            if (needSrcAlpha) {
                alpha = new float[w];
            }
            for (int y = 0; y < h; ++y) {
                int i;
                int x;
                Object pixel = null;
                float[] color = null;
                int idx = 0;
                for (x = 0; x < w; ++x) {
                    pixel = srcRas.getDataElements(x, y, pixel);
                    color = srcCM.getNormalizedComponents(pixel, color, 0);
                    if (needSrcAlpha) {
                        alpha[x] = color[srcNumComp];
                    }
                    if (nonICCSrc) {
                        color = srcColorSpace.toCIEXYZ(color);
                    }
                    for (i = 0; i < iccSrcNumComp; ++i) {
                        srcLine[idx++] = (short)((color[i] - srcMinVal[i]) * srcInvDiffMinMax[i] + 0.5f);
                    }
                }
                this.thisTransform.colorConvert(srcLine, dstLine);
                pixel = null;
                idx = 0;
                for (x = 0; x < w; ++x) {
                    for (i = 0; i < iccDstNumComp; ++i) {
                        dstColor[i] = (float)(dstLine[idx++] & 0xFFFF) * dstDiffMinMax[i] + dstMinVal[i];
                    }
                    if (nonICCDst) {
                        color = dstColorSpace.fromCIEXYZ(dstColor);
                        for (i = 0; i < dstNumComp; ++i) {
                            dstColor[i] = color[i];
                        }
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = alpha[x];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    pixel = dstCM.getDataElements(dstColor, 0, pixel);
                    dstRas.setDataElements(x, y, pixel);
                }
            }
        } else {
            int numCS = this.CSList == null ? 0 : this.CSList.length;
            float[] dstColor = dstHasAlpha ? new float[dstNumComp + 1] : new float[dstNumComp];
            Object spixel = null;
            Object dpixel = null;
            float[] color = null;
            for (int y = 0; y < h; ++y) {
                for (int x = 0; x < w; ++x) {
                    int i;
                    spixel = srcRas.getDataElements(x, y, spixel);
                    color = srcCM.getNormalizedComponents(spixel, color, 0);
                    float[] tmpColor = srcColorSpace.toCIEXYZ(color);
                    for (i = 0; i < numCS; ++i) {
                        tmpColor = this.CSList[i].fromCIEXYZ(tmpColor);
                        tmpColor = this.CSList[i].toCIEXYZ(tmpColor);
                    }
                    tmpColor = dstColorSpace.fromCIEXYZ(tmpColor);
                    for (i = 0; i < dstNumComp; ++i) {
                        dstColor[i] = tmpColor[i];
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = color[srcNumComp];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    dpixel = dstCM.getDataElements(dstColor, 0, dpixel);
                    dstRas.setDataElements(x, y, dpixel);
                }
            }
        }
        return dst;
    }

    private WritableRaster nonICCRasterFilter(Raster src, WritableRaster dst) {
        int i;
        if (this.CSList.length != 2) {
            throw new IllegalArgumentException("Destination ColorSpace is undefined");
        }
        if (src.getNumBands() != this.CSList[0].getNumComponents()) {
            throw new IllegalArgumentException("Numbers of source Raster bands and source color space components do not match");
        }
        if (dst == null) {
            dst = this.createCompatibleDestRaster(src);
        } else {
            if (src.getHeight() != dst.getHeight() || src.getWidth() != dst.getWidth()) {
                throw new IllegalArgumentException("Width or height of Rasters do not match");
            }
            if (dst.getNumBands() != this.CSList[1].getNumComponents()) {
                throw new IllegalArgumentException("Numbers of destination Raster bands and destination color space components do not match");
            }
        }
        if (this.srcMinVals == null) {
            this.getMinMaxValsFromColorSpaces(this.CSList[0], this.CSList[1]);
        }
        SampleModel srcSM = src.getSampleModel();
        SampleModel dstSM = dst.getSampleModel();
        int srcTransferType = src.getTransferType();
        int dstTransferType = dst.getTransferType();
        boolean srcIsFloat = srcTransferType == 4 || srcTransferType == 5;
        boolean dstIsFloat = dstTransferType == 4 || dstTransferType == 5;
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumBands = src.getNumBands();
        int dstNumBands = dst.getNumBands();
        float[] srcScaleFactor = null;
        float[] dstScaleFactor = null;
        if (!srcIsFloat) {
            srcScaleFactor = new float[srcNumBands];
            for (i = 0; i < srcNumBands; ++i) {
                srcScaleFactor[i] = srcTransferType == 2 ? (this.srcMaxVals[i] - this.srcMinVals[i]) / 32767.0f : (this.srcMaxVals[i] - this.srcMinVals[i]) / (float)((1 << srcSM.getSampleSize(i)) - 1);
            }
        }
        if (!dstIsFloat) {
            dstScaleFactor = new float[dstNumBands];
            for (i = 0; i < dstNumBands; ++i) {
                dstScaleFactor[i] = dstTransferType == 2 ? 32767.0f / (this.dstMaxVals[i] - this.dstMinVals[i]) : (float)((1 << dstSM.getSampleSize(i)) - 1) / (this.dstMaxVals[i] - this.dstMinVals[i]);
            }
        }
        int ys = src.getMinY();
        int yd = dst.getMinY();
        float[] color = new float[srcNumBands];
        ColorSpace srcColorSpace = this.CSList[0];
        ColorSpace dstColorSpace = this.CSList[1];
        int y = 0;
        while (y < h) {
            int xs = src.getMinX();
            int xd = dst.getMinX();
            int x = 0;
            while (x < w) {
                float sample;
                int i2;
                for (i2 = 0; i2 < srcNumBands; ++i2) {
                    sample = src.getSampleFloat(xs, ys, i2);
                    if (!srcIsFloat) {
                        sample = sample * srcScaleFactor[i2] + this.srcMinVals[i2];
                    }
                    color[i2] = sample;
                }
                float[] tmpColor = srcColorSpace.toCIEXYZ(color);
                tmpColor = dstColorSpace.fromCIEXYZ(tmpColor);
                for (i2 = 0; i2 < dstNumBands; ++i2) {
                    sample = tmpColor[i2];
                    if (!dstIsFloat) {
                        sample = (sample - this.dstMinVals[i2]) * dstScaleFactor[i2];
                    }
                    dst.setSample(xd, yd, i2, sample);
                }
                ++x;
                ++xs;
                ++xd;
            }
            ++y;
            ++ys;
            ++yd;
        }
        return dst;
    }

    private void getMinMaxValsFromProfiles(ICC_Profile srcProfile, ICC_Profile dstProfile) {
        int type = srcProfile.getColorSpaceType();
        int nc = srcProfile.getNumComponents();
        this.srcMinVals = new float[nc];
        this.srcMaxVals = new float[nc];
        this.setMinMax(type, nc, this.srcMinVals, this.srcMaxVals);
        type = dstProfile.getColorSpaceType();
        nc = dstProfile.getNumComponents();
        this.dstMinVals = new float[nc];
        this.dstMaxVals = new float[nc];
        this.setMinMax(type, nc, this.dstMinVals, this.dstMaxVals);
    }

    private void setMinMax(int type, int nc, float[] minVals, float[] maxVals) {
        if (type == 1) {
            minVals[0] = 0.0f;
            maxVals[0] = 100.0f;
            minVals[1] = -128.0f;
            maxVals[1] = 127.0f;
            minVals[2] = -128.0f;
            maxVals[2] = 127.0f;
        } else if (type == 0) {
            minVals[2] = 0.0f;
            minVals[1] = 0.0f;
            minVals[0] = 0.0f;
            maxVals[2] = 1.9999695f;
            maxVals[1] = 1.9999695f;
            maxVals[0] = 1.9999695f;
        } else {
            for (int i = 0; i < nc; ++i) {
                minVals[i] = 0.0f;
                maxVals[i] = 1.0f;
            }
        }
    }

    private void getMinMaxValsFromColorSpaces(ColorSpace srcCspace, ColorSpace dstCspace) {
        int i;
        int nc = srcCspace.getNumComponents();
        this.srcMinVals = new float[nc];
        this.srcMaxVals = new float[nc];
        for (i = 0; i < nc; ++i) {
            this.srcMinVals[i] = srcCspace.getMinValue(i);
            this.srcMaxVals[i] = srcCspace.getMaxValue(i);
        }
        nc = dstCspace.getNumComponents();
        this.dstMinVals = new float[nc];
        this.dstMaxVals = new float[nc];
        for (i = 0; i < nc; ++i) {
            this.dstMinVals[i] = dstCspace.getMinValue(i);
            this.dstMaxVals[i] = dstCspace.getMaxValue(i);
        }
    }
}

