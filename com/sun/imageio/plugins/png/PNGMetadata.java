/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.png;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

public class PNGMetadata
extends IIOMetadata
implements Cloneable {
    public static final String nativeMetadataFormatName = "javax_imageio_png_1.0";
    protected static final String nativeMetadataFormatClassName = "com.sun.imageio.plugins.png.PNGMetadataFormat";
    static final String[] IHDR_colorTypeNames = new String[]{"Grayscale", null, "RGB", "Palette", "GrayAlpha", null, "RGBAlpha"};
    static final int[] IHDR_numChannels = new int[]{1, 0, 3, 3, 2, 0, 4};
    static final String[] IHDR_bitDepths = new String[]{"1", "2", "4", "8", "16"};
    static final String[] IHDR_compressionMethodNames = new String[]{"deflate"};
    static final String[] IHDR_filterMethodNames = new String[]{"adaptive"};
    static final String[] IHDR_interlaceMethodNames = new String[]{"none", "adam7"};
    static final String[] iCCP_compressionMethodNames = new String[]{"deflate"};
    static final String[] zTXt_compressionMethodNames = new String[]{"deflate"};
    public static final int PHYS_UNIT_UNKNOWN = 0;
    public static final int PHYS_UNIT_METER = 1;
    static final String[] unitSpecifierNames = new String[]{"unknown", "meter"};
    static final String[] renderingIntentNames = new String[]{"Perceptual", "Relative colorimetric", "Saturation", "Absolute colorimetric"};
    static final String[] colorSpaceTypeNames = new String[]{"GRAY", null, "RGB", "RGB", "GRAY", null, "RGB"};
    public boolean IHDR_present;
    public int IHDR_width;
    public int IHDR_height;
    public int IHDR_bitDepth;
    public int IHDR_colorType;
    public int IHDR_compressionMethod;
    public int IHDR_filterMethod;
    public int IHDR_interlaceMethod;
    public boolean PLTE_present;
    public byte[] PLTE_red;
    public byte[] PLTE_green;
    public byte[] PLTE_blue;
    public int[] PLTE_order = null;
    public boolean bKGD_present;
    public int bKGD_colorType;
    public int bKGD_index;
    public int bKGD_gray;
    public int bKGD_red;
    public int bKGD_green;
    public int bKGD_blue;
    public boolean cHRM_present;
    public int cHRM_whitePointX;
    public int cHRM_whitePointY;
    public int cHRM_redX;
    public int cHRM_redY;
    public int cHRM_greenX;
    public int cHRM_greenY;
    public int cHRM_blueX;
    public int cHRM_blueY;
    public boolean gAMA_present;
    public int gAMA_gamma;
    public boolean hIST_present;
    public char[] hIST_histogram;
    public boolean iCCP_present;
    public String iCCP_profileName;
    public int iCCP_compressionMethod;
    public byte[] iCCP_compressedProfile;
    public ArrayList<String> iTXt_keyword = new ArrayList();
    public ArrayList<Boolean> iTXt_compressionFlag = new ArrayList();
    public ArrayList<Integer> iTXt_compressionMethod = new ArrayList();
    public ArrayList<String> iTXt_languageTag = new ArrayList();
    public ArrayList<String> iTXt_translatedKeyword = new ArrayList();
    public ArrayList<String> iTXt_text = new ArrayList();
    public boolean pHYs_present;
    public int pHYs_pixelsPerUnitXAxis;
    public int pHYs_pixelsPerUnitYAxis;
    public int pHYs_unitSpecifier;
    public boolean sBIT_present;
    public int sBIT_colorType;
    public int sBIT_grayBits;
    public int sBIT_redBits;
    public int sBIT_greenBits;
    public int sBIT_blueBits;
    public int sBIT_alphaBits;
    public boolean sPLT_present;
    public String sPLT_paletteName;
    public int sPLT_sampleDepth;
    public int[] sPLT_red;
    public int[] sPLT_green;
    public int[] sPLT_blue;
    public int[] sPLT_alpha;
    public int[] sPLT_frequency;
    public boolean sRGB_present;
    public int sRGB_renderingIntent;
    public ArrayList<String> tEXt_keyword = new ArrayList();
    public ArrayList<String> tEXt_text = new ArrayList();
    public boolean tIME_present;
    public int tIME_year;
    public int tIME_month;
    public int tIME_day;
    public int tIME_hour;
    public int tIME_minute;
    public int tIME_second;
    public boolean creation_time_present;
    public int creation_time_year;
    public int creation_time_month;
    public int creation_time_day;
    public int creation_time_hour;
    public int creation_time_minute;
    public int creation_time_second;
    public ZoneOffset creation_time_offset;
    public boolean tEXt_creation_time_present;
    private ListIterator<String> tEXt_creation_time_iter = null;
    public static final String tEXt_creationTimeKey = "Creation Time";
    public boolean tRNS_present;
    public int tRNS_colorType;
    public byte[] tRNS_alpha;
    public int tRNS_gray;
    public int tRNS_red;
    public int tRNS_green;
    public int tRNS_blue;
    public ArrayList<String> zTXt_keyword = new ArrayList();
    public ArrayList<Integer> zTXt_compressionMethod = new ArrayList();
    public ArrayList<String> zTXt_text = new ArrayList();
    public ArrayList<String> unknownChunkType = new ArrayList();
    public ArrayList<byte[]> unknownChunkData = new ArrayList();

    public PNGMetadata() {
        super(true, nativeMetadataFormatName, nativeMetadataFormatClassName, null, null);
    }

    public PNGMetadata(IIOMetadata metadata) {
    }

    public void initialize(ImageTypeSpecifier imageType, int numBands) {
        ColorModel colorModel = imageType.getColorModel();
        SampleModel sampleModel = imageType.getSampleModel();
        int[] sampleSize = sampleModel.getSampleSize();
        int bitDepth = sampleSize[0];
        for (int i = 1; i < sampleSize.length; ++i) {
            if (sampleSize[i] <= bitDepth) continue;
            bitDepth = sampleSize[i];
        }
        if (sampleSize.length > 1 && bitDepth < 8) {
            bitDepth = 8;
        }
        if (bitDepth > 2 && bitDepth < 4) {
            bitDepth = 4;
        } else if (bitDepth > 4 && bitDepth < 8) {
            bitDepth = 8;
        } else if (bitDepth > 8 && bitDepth < 16) {
            bitDepth = 16;
        } else if (bitDepth > 16) {
            throw new RuntimeException("bitDepth > 16!");
        }
        this.IHDR_bitDepth = bitDepth;
        if (colorModel instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)colorModel;
            int size = icm.getMapSize();
            byte[] reds = new byte[size];
            icm.getReds(reds);
            byte[] greens = new byte[size];
            icm.getGreens(greens);
            byte[] blues = new byte[size];
            icm.getBlues(blues);
            boolean isGray = false;
            if (!this.IHDR_present || this.IHDR_colorType != 3) {
                isGray = true;
                int scale = 255 / ((1 << this.IHDR_bitDepth) - 1);
                for (int i = 0; i < size; ++i) {
                    byte red = reds[i];
                    if (red == (byte)(i * scale) && red == greens[i] && red == blues[i]) continue;
                    isGray = false;
                    break;
                }
            }
            boolean hasAlpha = colorModel.hasAlpha();
            byte[] alpha = null;
            if (hasAlpha) {
                alpha = new byte[size];
                icm.getAlphas(alpha);
            }
            if (isGray && hasAlpha && (bitDepth == 8 || bitDepth == 16)) {
                this.IHDR_colorType = 4;
            } else if (isGray && !hasAlpha) {
                this.IHDR_colorType = 0;
            } else {
                this.IHDR_colorType = 3;
                this.PLTE_present = true;
                this.PLTE_order = null;
                this.PLTE_red = (byte[])reds.clone();
                this.PLTE_green = (byte[])greens.clone();
                this.PLTE_blue = (byte[])blues.clone();
                if (hasAlpha) {
                    this.tRNS_present = true;
                    this.tRNS_colorType = 3;
                    this.PLTE_order = new int[alpha.length];
                    byte[] newAlpha = new byte[alpha.length];
                    int newIndex = 0;
                    for (int i = 0; i < alpha.length; ++i) {
                        if (alpha[i] == -1) continue;
                        this.PLTE_order[i] = newIndex;
                        newAlpha[newIndex] = alpha[i];
                        ++newIndex;
                    }
                    int numTransparent = newIndex;
                    for (int i = 0; i < alpha.length; ++i) {
                        if (alpha[i] != -1) continue;
                        this.PLTE_order[i] = newIndex++;
                    }
                    byte[] oldRed = this.PLTE_red;
                    byte[] oldGreen = this.PLTE_green;
                    byte[] oldBlue = this.PLTE_blue;
                    int len = oldRed.length;
                    this.PLTE_red = new byte[len];
                    this.PLTE_green = new byte[len];
                    this.PLTE_blue = new byte[len];
                    for (int i = 0; i < len; ++i) {
                        this.PLTE_red[this.PLTE_order[i]] = oldRed[i];
                        this.PLTE_green[this.PLTE_order[i]] = oldGreen[i];
                        this.PLTE_blue[this.PLTE_order[i]] = oldBlue[i];
                    }
                    this.tRNS_alpha = new byte[numTransparent];
                    System.arraycopy(newAlpha, 0, this.tRNS_alpha, 0, numTransparent);
                }
            }
        } else if (numBands == 1) {
            this.IHDR_colorType = 0;
        } else if (numBands == 2) {
            this.IHDR_colorType = 4;
        } else if (numBands == 3) {
            this.IHDR_colorType = 2;
        } else if (numBands == 4) {
            this.IHDR_colorType = 6;
        } else {
            throw new RuntimeException("Number of bands not 1-4!");
        }
        this.IHDR_present = true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    private ArrayList<byte[]> cloneBytesArrayList(ArrayList<byte[]> in) {
        if (in == null) {
            return null;
        }
        ArrayList<byte[]> list = new ArrayList<byte[]>(in.size());
        for (byte[] b : in) {
            list.add(b == null ? null : (byte[])b.clone());
        }
        return list;
    }

    public Object clone() {
        PNGMetadata metadata;
        try {
            metadata = (PNGMetadata)super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
        metadata.unknownChunkData = this.cloneBytesArrayList(this.unknownChunkData);
        return metadata;
    }

    @Override
    public Node getAsTree(String formatName) {
        if (formatName.equals(nativeMetadataFormatName)) {
            return this.getNativeTree();
        }
        if (formatName.equals("javax_imageio_1.0")) {
            return this.getStandardTree();
        }
        throw new IllegalArgumentException("Not a recognized format!");
    }

    private Node getNativeTree() {
        IIOMetadataNode entry;
        int i;
        IIOMetadataNode node = null;
        IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);
        if (this.IHDR_present) {
            IIOMetadataNode IHDR_node = new IIOMetadataNode("IHDR");
            IHDR_node.setAttribute("width", Integer.toString(this.IHDR_width));
            IHDR_node.setAttribute("height", Integer.toString(this.IHDR_height));
            IHDR_node.setAttribute("bitDepth", Integer.toString(this.IHDR_bitDepth));
            IHDR_node.setAttribute("colorType", IHDR_colorTypeNames[this.IHDR_colorType]);
            IHDR_node.setAttribute("compressionMethod", IHDR_compressionMethodNames[this.IHDR_compressionMethod]);
            IHDR_node.setAttribute("filterMethod", IHDR_filterMethodNames[this.IHDR_filterMethod]);
            IHDR_node.setAttribute("interlaceMethod", IHDR_interlaceMethodNames[this.IHDR_interlaceMethod]);
            root.appendChild(IHDR_node);
        }
        if (this.PLTE_present) {
            IIOMetadataNode PLTE_node = new IIOMetadataNode("PLTE");
            int numEntries = this.PLTE_red.length;
            for (i = 0; i < numEntries; ++i) {
                entry = new IIOMetadataNode("PLTEEntry");
                entry.setAttribute("index", Integer.toString(i));
                entry.setAttribute("red", Integer.toString(this.PLTE_red[i] & 0xFF));
                entry.setAttribute("green", Integer.toString(this.PLTE_green[i] & 0xFF));
                entry.setAttribute("blue", Integer.toString(this.PLTE_blue[i] & 0xFF));
                PLTE_node.appendChild(entry);
            }
            root.appendChild(PLTE_node);
        }
        if (this.bKGD_present) {
            IIOMetadataNode bKGD_node = new IIOMetadataNode("bKGD");
            if (this.bKGD_colorType == 3) {
                node = new IIOMetadataNode("bKGD_Palette");
                node.setAttribute("index", Integer.toString(this.bKGD_index));
            } else if (this.bKGD_colorType == 0) {
                node = new IIOMetadataNode("bKGD_Grayscale");
                node.setAttribute("gray", Integer.toString(this.bKGD_gray));
            } else if (this.bKGD_colorType == 2) {
                node = new IIOMetadataNode("bKGD_RGB");
                node.setAttribute("red", Integer.toString(this.bKGD_red));
                node.setAttribute("green", Integer.toString(this.bKGD_green));
                node.setAttribute("blue", Integer.toString(this.bKGD_blue));
            }
            bKGD_node.appendChild(node);
            root.appendChild(bKGD_node);
        }
        if (this.cHRM_present) {
            IIOMetadataNode cHRM_node = new IIOMetadataNode("cHRM");
            cHRM_node.setAttribute("whitePointX", Integer.toString(this.cHRM_whitePointX));
            cHRM_node.setAttribute("whitePointY", Integer.toString(this.cHRM_whitePointY));
            cHRM_node.setAttribute("redX", Integer.toString(this.cHRM_redX));
            cHRM_node.setAttribute("redY", Integer.toString(this.cHRM_redY));
            cHRM_node.setAttribute("greenX", Integer.toString(this.cHRM_greenX));
            cHRM_node.setAttribute("greenY", Integer.toString(this.cHRM_greenY));
            cHRM_node.setAttribute("blueX", Integer.toString(this.cHRM_blueX));
            cHRM_node.setAttribute("blueY", Integer.toString(this.cHRM_blueY));
            root.appendChild(cHRM_node);
        }
        if (this.gAMA_present) {
            IIOMetadataNode gAMA_node = new IIOMetadataNode("gAMA");
            gAMA_node.setAttribute("value", Integer.toString(this.gAMA_gamma));
            root.appendChild(gAMA_node);
        }
        if (this.hIST_present) {
            IIOMetadataNode hIST_node = new IIOMetadataNode("hIST");
            for (int i2 = 0; i2 < this.hIST_histogram.length; ++i2) {
                IIOMetadataNode hist = new IIOMetadataNode("hISTEntry");
                hist.setAttribute("index", Integer.toString(i2));
                hist.setAttribute("value", Integer.toString(this.hIST_histogram[i2]));
                hIST_node.appendChild(hist);
            }
            root.appendChild(hIST_node);
        }
        if (this.iCCP_present) {
            IIOMetadataNode iCCP_node = new IIOMetadataNode("iCCP");
            iCCP_node.setAttribute("profileName", this.iCCP_profileName);
            iCCP_node.setAttribute("compressionMethod", iCCP_compressionMethodNames[this.iCCP_compressionMethod]);
            Object profile = this.iCCP_compressedProfile;
            if (profile != null) {
                profile = profile.clone();
            }
            iCCP_node.setUserObject(profile);
            root.appendChild(iCCP_node);
        }
        if (this.iTXt_keyword.size() > 0) {
            IIOMetadataNode iTXt_parent = new IIOMetadataNode("iTXt");
            for (int i3 = 0; i3 < this.iTXt_keyword.size(); ++i3) {
                IIOMetadataNode iTXt_node = new IIOMetadataNode("iTXtEntry");
                iTXt_node.setAttribute("keyword", this.iTXt_keyword.get(i3));
                iTXt_node.setAttribute("compressionFlag", this.iTXt_compressionFlag.get(i3) != false ? "TRUE" : "FALSE");
                iTXt_node.setAttribute("compressionMethod", this.iTXt_compressionMethod.get(i3).toString());
                iTXt_node.setAttribute("languageTag", this.iTXt_languageTag.get(i3));
                iTXt_node.setAttribute("translatedKeyword", this.iTXt_translatedKeyword.get(i3));
                iTXt_node.setAttribute("text", this.iTXt_text.get(i3));
                iTXt_parent.appendChild(iTXt_node);
            }
            root.appendChild(iTXt_parent);
        }
        if (this.pHYs_present) {
            IIOMetadataNode pHYs_node = new IIOMetadataNode("pHYs");
            pHYs_node.setAttribute("pixelsPerUnitXAxis", Integer.toString(this.pHYs_pixelsPerUnitXAxis));
            pHYs_node.setAttribute("pixelsPerUnitYAxis", Integer.toString(this.pHYs_pixelsPerUnitYAxis));
            pHYs_node.setAttribute("unitSpecifier", unitSpecifierNames[this.pHYs_unitSpecifier]);
            root.appendChild(pHYs_node);
        }
        if (this.sBIT_present) {
            IIOMetadataNode sBIT_node = new IIOMetadataNode("sBIT");
            if (this.sBIT_colorType == 0) {
                node = new IIOMetadataNode("sBIT_Grayscale");
                node.setAttribute("gray", Integer.toString(this.sBIT_grayBits));
            } else if (this.sBIT_colorType == 4) {
                node = new IIOMetadataNode("sBIT_GrayAlpha");
                node.setAttribute("gray", Integer.toString(this.sBIT_grayBits));
                node.setAttribute("alpha", Integer.toString(this.sBIT_alphaBits));
            } else if (this.sBIT_colorType == 2) {
                node = new IIOMetadataNode("sBIT_RGB");
                node.setAttribute("red", Integer.toString(this.sBIT_redBits));
                node.setAttribute("green", Integer.toString(this.sBIT_greenBits));
                node.setAttribute("blue", Integer.toString(this.sBIT_blueBits));
            } else if (this.sBIT_colorType == 6) {
                node = new IIOMetadataNode("sBIT_RGBAlpha");
                node.setAttribute("red", Integer.toString(this.sBIT_redBits));
                node.setAttribute("green", Integer.toString(this.sBIT_greenBits));
                node.setAttribute("blue", Integer.toString(this.sBIT_blueBits));
                node.setAttribute("alpha", Integer.toString(this.sBIT_alphaBits));
            } else if (this.sBIT_colorType == 3) {
                node = new IIOMetadataNode("sBIT_Palette");
                node.setAttribute("red", Integer.toString(this.sBIT_redBits));
                node.setAttribute("green", Integer.toString(this.sBIT_greenBits));
                node.setAttribute("blue", Integer.toString(this.sBIT_blueBits));
            }
            sBIT_node.appendChild(node);
            root.appendChild(sBIT_node);
        }
        if (this.sPLT_present) {
            IIOMetadataNode sPLT_node = new IIOMetadataNode("sPLT");
            sPLT_node.setAttribute("name", this.sPLT_paletteName);
            sPLT_node.setAttribute("sampleDepth", Integer.toString(this.sPLT_sampleDepth));
            int numEntries = this.sPLT_red.length;
            for (i = 0; i < numEntries; ++i) {
                entry = new IIOMetadataNode("sPLTEntry");
                entry.setAttribute("index", Integer.toString(i));
                entry.setAttribute("red", Integer.toString(this.sPLT_red[i]));
                entry.setAttribute("green", Integer.toString(this.sPLT_green[i]));
                entry.setAttribute("blue", Integer.toString(this.sPLT_blue[i]));
                entry.setAttribute("alpha", Integer.toString(this.sPLT_alpha[i]));
                entry.setAttribute("frequency", Integer.toString(this.sPLT_frequency[i]));
                sPLT_node.appendChild(entry);
            }
            root.appendChild(sPLT_node);
        }
        if (this.sRGB_present) {
            IIOMetadataNode sRGB_node = new IIOMetadataNode("sRGB");
            sRGB_node.setAttribute("renderingIntent", renderingIntentNames[this.sRGB_renderingIntent]);
            root.appendChild(sRGB_node);
        }
        if (this.tEXt_keyword.size() > 0) {
            IIOMetadataNode tEXt_parent = new IIOMetadataNode("tEXt");
            for (int i4 = 0; i4 < this.tEXt_keyword.size(); ++i4) {
                IIOMetadataNode tEXt_node = new IIOMetadataNode("tEXtEntry");
                tEXt_node.setAttribute("keyword", this.tEXt_keyword.get(i4));
                tEXt_node.setAttribute("value", this.tEXt_text.get(i4));
                tEXt_parent.appendChild(tEXt_node);
            }
            root.appendChild(tEXt_parent);
        }
        if (this.tIME_present) {
            IIOMetadataNode tIME_node = new IIOMetadataNode("tIME");
            tIME_node.setAttribute("year", Integer.toString(this.tIME_year));
            tIME_node.setAttribute("month", Integer.toString(this.tIME_month));
            tIME_node.setAttribute("day", Integer.toString(this.tIME_day));
            tIME_node.setAttribute("hour", Integer.toString(this.tIME_hour));
            tIME_node.setAttribute("minute", Integer.toString(this.tIME_minute));
            tIME_node.setAttribute("second", Integer.toString(this.tIME_second));
            root.appendChild(tIME_node);
        }
        if (this.tRNS_present) {
            IIOMetadataNode tRNS_node = new IIOMetadataNode("tRNS");
            if (this.tRNS_colorType == 3) {
                node = new IIOMetadataNode("tRNS_Palette");
                for (int i5 = 0; i5 < this.tRNS_alpha.length; ++i5) {
                    IIOMetadataNode entry2 = new IIOMetadataNode("tRNS_PaletteEntry");
                    entry2.setAttribute("index", Integer.toString(i5));
                    entry2.setAttribute("alpha", Integer.toString(this.tRNS_alpha[i5] & 0xFF));
                    node.appendChild(entry2);
                }
            } else if (this.tRNS_colorType == 0) {
                node = new IIOMetadataNode("tRNS_Grayscale");
                node.setAttribute("gray", Integer.toString(this.tRNS_gray));
            } else if (this.tRNS_colorType == 2) {
                node = new IIOMetadataNode("tRNS_RGB");
                node.setAttribute("red", Integer.toString(this.tRNS_red));
                node.setAttribute("green", Integer.toString(this.tRNS_green));
                node.setAttribute("blue", Integer.toString(this.tRNS_blue));
            }
            tRNS_node.appendChild(node);
            root.appendChild(tRNS_node);
        }
        if (this.zTXt_keyword.size() > 0) {
            IIOMetadataNode zTXt_parent = new IIOMetadataNode("zTXt");
            for (int i6 = 0; i6 < this.zTXt_keyword.size(); ++i6) {
                IIOMetadataNode zTXt_node = new IIOMetadataNode("zTXtEntry");
                zTXt_node.setAttribute("keyword", this.zTXt_keyword.get(i6));
                int cm = this.zTXt_compressionMethod.get(i6);
                zTXt_node.setAttribute("compressionMethod", zTXt_compressionMethodNames[cm]);
                zTXt_node.setAttribute("text", this.zTXt_text.get(i6));
                zTXt_parent.appendChild(zTXt_node);
            }
            root.appendChild(zTXt_parent);
        }
        if (this.unknownChunkType.size() > 0) {
            IIOMetadataNode unknown_parent = new IIOMetadataNode("UnknownChunks");
            for (int i7 = 0; i7 < this.unknownChunkType.size(); ++i7) {
                IIOMetadataNode unknown_node = new IIOMetadataNode("UnknownChunk");
                unknown_node.setAttribute("type", this.unknownChunkType.get(i7));
                unknown_node.setUserObject(this.unknownChunkData.get(i7));
                unknown_parent.appendChild(unknown_node);
            }
            root.appendChild(unknown_parent);
        }
        return root;
    }

    private int getNumChannels() {
        int numChannels = IHDR_numChannels[this.IHDR_colorType];
        if (this.IHDR_colorType == 3 && this.tRNS_present && this.tRNS_colorType == this.IHDR_colorType) {
            numChannels = 4;
        }
        return numChannels;
    }

    @Override
    public IIOMetadataNode getStandardChromaNode() {
        IIOMetadataNode chroma_node = new IIOMetadataNode("Chroma");
        IIOMetadataNode node = null;
        node = new IIOMetadataNode("ColorSpaceType");
        node.setAttribute("name", colorSpaceTypeNames[this.IHDR_colorType]);
        chroma_node.appendChild(node);
        node = new IIOMetadataNode("NumChannels");
        node.setAttribute("value", Integer.toString(this.getNumChannels()));
        chroma_node.appendChild(node);
        if (this.gAMA_present) {
            node = new IIOMetadataNode("Gamma");
            node.setAttribute("value", Float.toString((float)this.gAMA_gamma * 1.0E-5f));
            chroma_node.appendChild(node);
        }
        node = new IIOMetadataNode("BlackIsZero");
        node.setAttribute("value", "TRUE");
        chroma_node.appendChild(node);
        if (this.PLTE_present) {
            boolean hasAlpha = this.tRNS_present && this.tRNS_colorType == 3;
            node = new IIOMetadataNode("Palette");
            for (int i = 0; i < this.PLTE_red.length; ++i) {
                IIOMetadataNode entry = new IIOMetadataNode("PaletteEntry");
                entry.setAttribute("index", Integer.toString(i));
                entry.setAttribute("red", Integer.toString(this.PLTE_red[i] & 0xFF));
                entry.setAttribute("green", Integer.toString(this.PLTE_green[i] & 0xFF));
                entry.setAttribute("blue", Integer.toString(this.PLTE_blue[i] & 0xFF));
                if (hasAlpha) {
                    int alpha = i < this.tRNS_alpha.length ? this.tRNS_alpha[i] & 0xFF : 255;
                    entry.setAttribute("alpha", Integer.toString(alpha));
                }
                node.appendChild(entry);
            }
            chroma_node.appendChild(node);
        }
        if (this.bKGD_present) {
            if (this.bKGD_colorType == 3) {
                node = new IIOMetadataNode("BackgroundIndex");
                node.setAttribute("value", Integer.toString(this.bKGD_index));
            } else {
                int r;
                int g;
                int b;
                node = new IIOMetadataNode("BackgroundColor");
                if (this.bKGD_colorType == 0) {
                    g = b = this.bKGD_gray;
                    r = b;
                } else {
                    r = this.bKGD_red;
                    g = this.bKGD_green;
                    b = this.bKGD_blue;
                }
                node.setAttribute("red", Integer.toString(r));
                node.setAttribute("green", Integer.toString(g));
                node.setAttribute("blue", Integer.toString(b));
            }
            chroma_node.appendChild(node);
        }
        return chroma_node;
    }

    @Override
    public IIOMetadataNode getStandardCompressionNode() {
        IIOMetadataNode compression_node = new IIOMetadataNode("Compression");
        IIOMetadataNode node = null;
        node = new IIOMetadataNode("CompressionTypeName");
        node.setAttribute("value", "deflate");
        compression_node.appendChild(node);
        node = new IIOMetadataNode("Lossless");
        node.setAttribute("value", "TRUE");
        compression_node.appendChild(node);
        node = new IIOMetadataNode("NumProgressiveScans");
        node.setAttribute("value", this.IHDR_interlaceMethod == 0 ? "1" : "7");
        compression_node.appendChild(node);
        return compression_node;
    }

    private String repeat(String s, int times) {
        if (times == 1) {
            return s;
        }
        StringBuilder sb = new StringBuilder((s.length() + 1) * times - 1);
        sb.append(s);
        for (int i = 1; i < times; ++i) {
            sb.append(" ");
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public IIOMetadataNode getStandardDataNode() {
        IIOMetadataNode data_node = new IIOMetadataNode("Data");
        IIOMetadataNode node = null;
        node = new IIOMetadataNode("PlanarConfiguration");
        node.setAttribute("value", "PixelInterleaved");
        data_node.appendChild(node);
        node = new IIOMetadataNode("SampleFormat");
        node.setAttribute("value", this.IHDR_colorType == 3 ? "Index" : "UnsignedIntegral");
        data_node.appendChild(node);
        String bitDepth = Integer.toString(this.IHDR_bitDepth);
        node = new IIOMetadataNode("BitsPerSample");
        node.setAttribute("value", this.repeat(bitDepth, this.getNumChannels()));
        data_node.appendChild(node);
        if (this.sBIT_present) {
            node = new IIOMetadataNode("SignificantBitsPerSample");
            Object sbits = this.sBIT_colorType == 0 || this.sBIT_colorType == 4 ? Integer.toString(this.sBIT_grayBits) : this.sBIT_redBits + " " + this.sBIT_greenBits + " " + this.sBIT_blueBits;
            if (this.sBIT_colorType == 4 || this.sBIT_colorType == 6) {
                sbits = (String)sbits + " " + this.sBIT_alphaBits;
            }
            node.setAttribute("value", (String)sbits);
            data_node.appendChild(node);
        }
        return data_node;
    }

    @Override
    public IIOMetadataNode getStandardDimensionNode() {
        IIOMetadataNode dimension_node = new IIOMetadataNode("Dimension");
        IIOMetadataNode node = null;
        node = new IIOMetadataNode("PixelAspectRatio");
        float ratio = this.pHYs_present ? (float)this.pHYs_pixelsPerUnitXAxis / (float)this.pHYs_pixelsPerUnitYAxis : 1.0f;
        node.setAttribute("value", Float.toString(ratio));
        dimension_node.appendChild(node);
        node = new IIOMetadataNode("ImageOrientation");
        node.setAttribute("value", "Normal");
        dimension_node.appendChild(node);
        if (this.pHYs_present && this.pHYs_unitSpecifier == 1) {
            node = new IIOMetadataNode("HorizontalPixelSize");
            node.setAttribute("value", Float.toString(1000.0f / (float)this.pHYs_pixelsPerUnitXAxis));
            dimension_node.appendChild(node);
            node = new IIOMetadataNode("VerticalPixelSize");
            node.setAttribute("value", Float.toString(1000.0f / (float)this.pHYs_pixelsPerUnitYAxis));
            dimension_node.appendChild(node);
        }
        return dimension_node;
    }

    @Override
    public IIOMetadataNode getStandardDocumentNode() {
        IIOMetadataNode node;
        IIOMetadataNode document_node = null;
        if (this.tIME_present) {
            document_node = new IIOMetadataNode("Document");
            node = new IIOMetadataNode("ImageModificationTime");
            node.setAttribute("year", Integer.toString(this.tIME_year));
            node.setAttribute("month", Integer.toString(this.tIME_month));
            node.setAttribute("day", Integer.toString(this.tIME_day));
            node.setAttribute("hour", Integer.toString(this.tIME_hour));
            node.setAttribute("minute", Integer.toString(this.tIME_minute));
            node.setAttribute("second", Integer.toString(this.tIME_second));
            document_node.appendChild(node);
        }
        if (this.creation_time_present) {
            if (document_node == null) {
                document_node = new IIOMetadataNode("Document");
            }
            node = new IIOMetadataNode("ImageCreationTime");
            node.setAttribute("year", Integer.toString(this.creation_time_year));
            node.setAttribute("month", Integer.toString(this.creation_time_month));
            node.setAttribute("day", Integer.toString(this.creation_time_day));
            node.setAttribute("hour", Integer.toString(this.creation_time_hour));
            node.setAttribute("minute", Integer.toString(this.creation_time_minute));
            node.setAttribute("second", Integer.toString(this.creation_time_second));
            document_node.appendChild(node);
        }
        return document_node;
    }

    @Override
    public IIOMetadataNode getStandardTextNode() {
        int i;
        int numEntries = this.tEXt_keyword.size() + this.iTXt_keyword.size() + this.zTXt_keyword.size();
        if (numEntries == 0) {
            return null;
        }
        IIOMetadataNode text_node = new IIOMetadataNode("Text");
        IIOMetadataNode node = null;
        for (i = 0; i < this.tEXt_keyword.size(); ++i) {
            node = new IIOMetadataNode("TextEntry");
            node.setAttribute("keyword", this.tEXt_keyword.get(i));
            node.setAttribute("value", this.tEXt_text.get(i));
            node.setAttribute("encoding", "ISO-8859-1");
            node.setAttribute("compression", "none");
            text_node.appendChild(node);
        }
        for (i = 0; i < this.iTXt_keyword.size(); ++i) {
            node = new IIOMetadataNode("TextEntry");
            node.setAttribute("keyword", this.iTXt_keyword.get(i));
            node.setAttribute("value", this.iTXt_text.get(i));
            node.setAttribute("language", this.iTXt_languageTag.get(i));
            if (this.iTXt_compressionFlag.get(i).booleanValue()) {
                node.setAttribute("compression", "zip");
            } else {
                node.setAttribute("compression", "none");
            }
            text_node.appendChild(node);
        }
        for (i = 0; i < this.zTXt_keyword.size(); ++i) {
            node = new IIOMetadataNode("TextEntry");
            node.setAttribute("keyword", this.zTXt_keyword.get(i));
            node.setAttribute("value", this.zTXt_text.get(i));
            node.setAttribute("compression", "zip");
            text_node.appendChild(node);
        }
        return text_node;
    }

    @Override
    public IIOMetadataNode getStandardTransparencyNode() {
        IIOMetadataNode transparency_node = new IIOMetadataNode("Transparency");
        IIOMetadataNode node = null;
        node = new IIOMetadataNode("Alpha");
        boolean hasAlpha = this.IHDR_colorType == 6 || this.IHDR_colorType == 4 || this.IHDR_colorType == 3 && this.tRNS_present && this.tRNS_colorType == this.IHDR_colorType && this.tRNS_alpha != null;
        node.setAttribute("value", hasAlpha ? "nonpremultipled" : "none");
        transparency_node.appendChild(node);
        if (this.tRNS_present) {
            node = new IIOMetadataNode("TransparentColor");
            if (this.tRNS_colorType == 2) {
                node.setAttribute("value", this.tRNS_red + " " + this.tRNS_green + " " + this.tRNS_blue);
            } else if (this.tRNS_colorType == 0) {
                node.setAttribute("value", Integer.toString(this.tRNS_gray));
            }
            transparency_node.appendChild(node);
        }
        return transparency_node;
    }

    private void fatal(Node node, String reason) throws IIOInvalidTreeException {
        throw new IIOInvalidTreeException(reason, node);
    }

    private String getStringAttribute(Node node, String name, String defaultValue, boolean required) throws IIOInvalidTreeException {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr == null) {
            if (!required) {
                return defaultValue;
            }
            this.fatal(node, "Required attribute " + name + " not present!");
        }
        return attr.getNodeValue();
    }

    private int getIntAttribute(Node node, String name, int defaultValue, boolean required) throws IIOInvalidTreeException {
        String value = this.getStringAttribute(node, name, null, required);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private float getFloatAttribute(Node node, String name, float defaultValue, boolean required) throws IIOInvalidTreeException {
        String value = this.getStringAttribute(node, name, null, required);
        if (value == null) {
            return defaultValue;
        }
        return Float.parseFloat(value);
    }

    private int getIntAttribute(Node node, String name) throws IIOInvalidTreeException {
        return this.getIntAttribute(node, name, -1, true);
    }

    private float getFloatAttribute(Node node, String name) throws IIOInvalidTreeException {
        return this.getFloatAttribute(node, name, -1.0f, true);
    }

    private boolean getBooleanAttribute(Node node, String name, boolean defaultValue, boolean required) throws IIOInvalidTreeException {
        String value;
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr == null) {
            if (!required) {
                return defaultValue;
            }
            this.fatal(node, "Required attribute " + name + " not present!");
        }
        if ((value = attr.getNodeValue()).equals("TRUE") || value.equals("true")) {
            return true;
        }
        if (value.equals("FALSE") || value.equals("false")) {
            return false;
        }
        this.fatal(node, "Attribute " + name + " must be 'TRUE' or 'FALSE'!");
        return false;
    }

    private boolean getBooleanAttribute(Node node, String name) throws IIOInvalidTreeException {
        return this.getBooleanAttribute(node, name, false, true);
    }

    private int getEnumeratedAttribute(Node node, String name, String[] legalNames, int defaultValue, boolean required) throws IIOInvalidTreeException {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr == null) {
            if (!required) {
                return defaultValue;
            }
            this.fatal(node, "Required attribute " + name + " not present!");
        }
        String value = attr.getNodeValue();
        for (int i = 0; i < legalNames.length; ++i) {
            if (!value.equals(legalNames[i])) continue;
            return i;
        }
        this.fatal(node, "Illegal value for attribute " + name + "!");
        return -1;
    }

    private int getEnumeratedAttribute(Node node, String name, String[] legalNames) throws IIOInvalidTreeException {
        return this.getEnumeratedAttribute(node, name, legalNames, -1, true);
    }

    private String getAttribute(Node node, String name, String defaultValue, boolean required) throws IIOInvalidTreeException {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr == null) {
            if (!required) {
                return defaultValue;
            }
            this.fatal(node, "Required attribute " + name + " not present!");
        }
        return attr.getNodeValue();
    }

    private String getAttribute(Node node, String name) throws IIOInvalidTreeException {
        return this.getAttribute(node, name, null, true);
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        if (formatName.equals(nativeMetadataFormatName)) {
            if (root == null) {
                throw new IllegalArgumentException("root == null!");
            }
            this.mergeNativeTree(root);
        } else if (formatName.equals("javax_imageio_1.0")) {
            if (root == null) {
                throw new IllegalArgumentException("root == null!");
            }
            this.mergeStandardTree(root);
        } else {
            throw new IllegalArgumentException("Not a recognized format!");
        }
    }

    private void mergeNativeTree(Node root) throws IIOInvalidTreeException {
        Node node = root;
        if (!node.getNodeName().equals(nativeMetadataFormatName)) {
            this.fatal(node, "Root must be javax_imageio_png_1.0");
        }
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
            int index;
            Object[] blue;
            Object[] red;
            String name = node.getNodeName();
            if (name.equals("IHDR")) {
                this.IHDR_width = this.getIntAttribute(node, "width");
                this.IHDR_height = this.getIntAttribute(node, "height");
                this.IHDR_bitDepth = Integer.parseInt(IHDR_bitDepths[this.getEnumeratedAttribute(node, "bitDepth", IHDR_bitDepths)]);
                this.IHDR_colorType = this.getEnumeratedAttribute(node, "colorType", IHDR_colorTypeNames);
                this.IHDR_compressionMethod = this.getEnumeratedAttribute(node, "compressionMethod", IHDR_compressionMethodNames);
                this.IHDR_filterMethod = this.getEnumeratedAttribute(node, "filterMethod", IHDR_filterMethodNames);
                this.IHDR_interlaceMethod = this.getEnumeratedAttribute(node, "interlaceMethod", IHDR_interlaceMethodNames);
                this.IHDR_present = true;
                continue;
            }
            if (name.equals("PLTE")) {
                red = new byte[256];
                byte[] green = new byte[256];
                blue = new byte[256];
                int maxindex = -1;
                Node PLTE_entry = node.getFirstChild();
                if (PLTE_entry == null) {
                    this.fatal(node, "Palette has no entries!");
                }
                while (PLTE_entry != null) {
                    int index2;
                    if (!PLTE_entry.getNodeName().equals("PLTEEntry")) {
                        this.fatal(node, "Only a PLTEEntry may be a child of a PLTE!");
                    }
                    if ((index2 = this.getIntAttribute(PLTE_entry, "index")) < 0 || index2 > 255) {
                        this.fatal(node, "Bad value for PLTEEntry attribute index!");
                    }
                    if (index2 > maxindex) {
                        maxindex = index2;
                    }
                    red[index2] = (byte)this.getIntAttribute(PLTE_entry, "red");
                    green[index2] = (byte)this.getIntAttribute(PLTE_entry, "green");
                    blue[index2] = (byte)this.getIntAttribute(PLTE_entry, "blue");
                    PLTE_entry = PLTE_entry.getNextSibling();
                }
                int numEntries = maxindex + 1;
                this.PLTE_red = new byte[numEntries];
                this.PLTE_green = new byte[numEntries];
                this.PLTE_blue = new byte[numEntries];
                System.arraycopy(red, 0, this.PLTE_red, 0, numEntries);
                System.arraycopy(green, 0, this.PLTE_green, 0, numEntries);
                System.arraycopy(blue, 0, this.PLTE_blue, 0, numEntries);
                this.PLTE_present = true;
                continue;
            }
            if (name.equals("bKGD")) {
                String bKGD_name;
                this.bKGD_present = false;
                Node bKGD_node = node.getFirstChild();
                if (bKGD_node == null) {
                    this.fatal(node, "bKGD node has no children!");
                }
                if ((bKGD_name = bKGD_node.getNodeName()).equals("bKGD_Palette")) {
                    this.bKGD_index = this.getIntAttribute(bKGD_node, "index");
                    this.bKGD_colorType = 3;
                } else if (bKGD_name.equals("bKGD_Grayscale")) {
                    this.bKGD_gray = this.getIntAttribute(bKGD_node, "gray");
                    this.bKGD_colorType = 0;
                } else if (bKGD_name.equals("bKGD_RGB")) {
                    this.bKGD_red = this.getIntAttribute(bKGD_node, "red");
                    this.bKGD_green = this.getIntAttribute(bKGD_node, "green");
                    this.bKGD_blue = this.getIntAttribute(bKGD_node, "blue");
                    this.bKGD_colorType = 2;
                } else {
                    this.fatal(node, "Bad child of a bKGD node!");
                }
                if (bKGD_node.getNextSibling() != null) {
                    this.fatal(node, "bKGD node has more than one child!");
                }
                this.bKGD_present = true;
                continue;
            }
            if (name.equals("cHRM")) {
                this.cHRM_whitePointX = this.getIntAttribute(node, "whitePointX");
                this.cHRM_whitePointY = this.getIntAttribute(node, "whitePointY");
                this.cHRM_redX = this.getIntAttribute(node, "redX");
                this.cHRM_redY = this.getIntAttribute(node, "redY");
                this.cHRM_greenX = this.getIntAttribute(node, "greenX");
                this.cHRM_greenY = this.getIntAttribute(node, "greenY");
                this.cHRM_blueX = this.getIntAttribute(node, "blueX");
                this.cHRM_blueY = this.getIntAttribute(node, "blueY");
                this.cHRM_present = true;
                continue;
            }
            if (name.equals("gAMA")) {
                this.gAMA_gamma = this.getIntAttribute(node, "value");
                this.gAMA_present = true;
                continue;
            }
            if (name.equals("hIST")) {
                char[] hist = new char[256];
                int maxindex = -1;
                Node hIST_entry = node.getFirstChild();
                if (hIST_entry == null) {
                    this.fatal(node, "hIST node has no children!");
                }
                while (hIST_entry != null) {
                    int index3;
                    if (!hIST_entry.getNodeName().equals("hISTEntry")) {
                        this.fatal(node, "Only a hISTEntry may be a child of a hIST!");
                    }
                    if ((index3 = this.getIntAttribute(hIST_entry, "index")) < 0 || index3 > 255) {
                        this.fatal(node, "Bad value for histEntry attribute index!");
                    }
                    if (index3 > maxindex) {
                        maxindex = index3;
                    }
                    hist[index3] = (char)this.getIntAttribute(hIST_entry, "value");
                    hIST_entry = hIST_entry.getNextSibling();
                }
                int numEntries = maxindex + 1;
                this.hIST_histogram = new char[numEntries];
                System.arraycopy(hist, 0, this.hIST_histogram, 0, numEntries);
                this.hIST_present = true;
                continue;
            }
            if (name.equals("iCCP")) {
                this.iCCP_profileName = this.getAttribute(node, "profileName");
                this.iCCP_compressionMethod = this.getEnumeratedAttribute(node, "compressionMethod", iCCP_compressionMethodNames);
                Object compressedProfile = ((IIOMetadataNode)node).getUserObject();
                if (compressedProfile == null) {
                    this.fatal(node, "No ICCP profile present in user object!");
                }
                if (!(compressedProfile instanceof byte[])) {
                    this.fatal(node, "User object not a byte array!");
                }
                this.iCCP_compressedProfile = (byte[])((byte[])compressedProfile).clone();
                this.iCCP_present = true;
                continue;
            }
            if (name.equals("iTXt")) {
                for (Node iTXt_node = node.getFirstChild(); iTXt_node != null; iTXt_node = iTXt_node.getNextSibling()) {
                    String keyword;
                    if (!iTXt_node.getNodeName().equals("iTXtEntry")) {
                        this.fatal(node, "Only an iTXtEntry may be a child of an iTXt!");
                    }
                    if (!this.isValidKeyword(keyword = this.getAttribute(iTXt_node, "keyword"))) continue;
                    this.iTXt_keyword.add(keyword);
                    boolean compressionFlag = this.getBooleanAttribute(iTXt_node, "compressionFlag");
                    this.iTXt_compressionFlag.add(compressionFlag);
                    String compressionMethod = this.getAttribute(iTXt_node, "compressionMethod");
                    this.iTXt_compressionMethod.add(Integer.valueOf(compressionMethod));
                    String languageTag = this.getAttribute(iTXt_node, "languageTag");
                    this.iTXt_languageTag.add(languageTag);
                    String translatedKeyword = this.getAttribute(iTXt_node, "translatedKeyword");
                    this.iTXt_translatedKeyword.add(translatedKeyword);
                    String text = this.getAttribute(iTXt_node, "text");
                    this.iTXt_text.add(text);
                    if (!keyword.equals(tEXt_creationTimeKey)) continue;
                    index = this.iTXt_text.size() - 1;
                    this.decodeImageCreationTimeFromTextChunk(this.iTXt_text.listIterator(index));
                }
                continue;
            }
            if (name.equals("pHYs")) {
                this.pHYs_pixelsPerUnitXAxis = this.getIntAttribute(node, "pixelsPerUnitXAxis");
                this.pHYs_pixelsPerUnitYAxis = this.getIntAttribute(node, "pixelsPerUnitYAxis");
                this.pHYs_unitSpecifier = this.getEnumeratedAttribute(node, "unitSpecifier", unitSpecifierNames);
                this.pHYs_present = true;
                continue;
            }
            if (name.equals("sBIT")) {
                String sBIT_name;
                this.sBIT_present = false;
                Node sBIT_node = node.getFirstChild();
                if (sBIT_node == null) {
                    this.fatal(node, "sBIT node has no children!");
                }
                if ((sBIT_name = sBIT_node.getNodeName()).equals("sBIT_Grayscale")) {
                    this.sBIT_grayBits = this.getIntAttribute(sBIT_node, "gray");
                    this.sBIT_colorType = 0;
                } else if (sBIT_name.equals("sBIT_GrayAlpha")) {
                    this.sBIT_grayBits = this.getIntAttribute(sBIT_node, "gray");
                    this.sBIT_alphaBits = this.getIntAttribute(sBIT_node, "alpha");
                    this.sBIT_colorType = 4;
                } else if (sBIT_name.equals("sBIT_RGB")) {
                    this.sBIT_redBits = this.getIntAttribute(sBIT_node, "red");
                    this.sBIT_greenBits = this.getIntAttribute(sBIT_node, "green");
                    this.sBIT_blueBits = this.getIntAttribute(sBIT_node, "blue");
                    this.sBIT_colorType = 2;
                } else if (sBIT_name.equals("sBIT_RGBAlpha")) {
                    this.sBIT_redBits = this.getIntAttribute(sBIT_node, "red");
                    this.sBIT_greenBits = this.getIntAttribute(sBIT_node, "green");
                    this.sBIT_blueBits = this.getIntAttribute(sBIT_node, "blue");
                    this.sBIT_alphaBits = this.getIntAttribute(sBIT_node, "alpha");
                    this.sBIT_colorType = 6;
                } else if (sBIT_name.equals("sBIT_Palette")) {
                    this.sBIT_redBits = this.getIntAttribute(sBIT_node, "red");
                    this.sBIT_greenBits = this.getIntAttribute(sBIT_node, "green");
                    this.sBIT_blueBits = this.getIntAttribute(sBIT_node, "blue");
                    this.sBIT_colorType = 3;
                } else {
                    this.fatal(node, "Bad child of an sBIT node!");
                }
                if (sBIT_node.getNextSibling() != null) {
                    this.fatal(node, "sBIT node has more than one child!");
                }
                this.sBIT_present = true;
                continue;
            }
            if (name.equals("sPLT")) {
                this.sPLT_paletteName = this.getAttribute(node, "name");
                this.sPLT_sampleDepth = this.getIntAttribute(node, "sampleDepth");
                red = new int[256];
                int[] green = new int[256];
                blue = new int[256];
                int[] alpha = new int[256];
                int[] frequency = new int[256];
                int maxindex = -1;
                Node sPLT_entry = node.getFirstChild();
                if (sPLT_entry == null) {
                    this.fatal(node, "sPLT node has no children!");
                }
                while (sPLT_entry != null) {
                    if (!sPLT_entry.getNodeName().equals("sPLTEntry")) {
                        this.fatal(node, "Only an sPLTEntry may be a child of an sPLT!");
                    }
                    if ((index = this.getIntAttribute(sPLT_entry, "index")) < 0 || index > 255) {
                        this.fatal(node, "Bad value for PLTEEntry attribute index!");
                    }
                    if (index > maxindex) {
                        maxindex = index;
                    }
                    red[index] = this.getIntAttribute(sPLT_entry, "red");
                    green[index] = this.getIntAttribute(sPLT_entry, "green");
                    blue[index] = this.getIntAttribute(sPLT_entry, "blue");
                    alpha[index] = this.getIntAttribute(sPLT_entry, "alpha");
                    frequency[index] = this.getIntAttribute(sPLT_entry, "frequency");
                    sPLT_entry = sPLT_entry.getNextSibling();
                }
                int numEntries = maxindex + 1;
                this.sPLT_red = new int[numEntries];
                this.sPLT_green = new int[numEntries];
                this.sPLT_blue = new int[numEntries];
                this.sPLT_alpha = new int[numEntries];
                this.sPLT_frequency = new int[numEntries];
                System.arraycopy(red, 0, this.sPLT_red, 0, numEntries);
                System.arraycopy(green, 0, this.sPLT_green, 0, numEntries);
                System.arraycopy(blue, 0, this.sPLT_blue, 0, numEntries);
                System.arraycopy(alpha, 0, this.sPLT_alpha, 0, numEntries);
                System.arraycopy(frequency, 0, this.sPLT_frequency, 0, numEntries);
                this.sPLT_present = true;
                continue;
            }
            if (name.equals("sRGB")) {
                this.sRGB_renderingIntent = this.getEnumeratedAttribute(node, "renderingIntent", renderingIntentNames);
                this.sRGB_present = true;
                continue;
            }
            if (name.equals("tEXt")) {
                for (Node tEXt_node = node.getFirstChild(); tEXt_node != null; tEXt_node = tEXt_node.getNextSibling()) {
                    if (!tEXt_node.getNodeName().equals("tEXtEntry")) {
                        this.fatal(node, "Only an tEXtEntry may be a child of an tEXt!");
                    }
                    String keyword = this.getAttribute(tEXt_node, "keyword");
                    this.tEXt_keyword.add(keyword);
                    String text = this.getAttribute(tEXt_node, "value");
                    this.tEXt_text.add(text);
                    if (!keyword.equals(tEXt_creationTimeKey)) continue;
                    int index4 = this.tEXt_text.size() - 1;
                    this.decodeImageCreationTimeFromTextChunk(this.tEXt_text.listIterator(index4));
                }
                continue;
            }
            if (name.equals("tIME")) {
                this.tIME_year = this.getIntAttribute(node, "year");
                this.tIME_month = this.getIntAttribute(node, "month");
                this.tIME_day = this.getIntAttribute(node, "day");
                this.tIME_hour = this.getIntAttribute(node, "hour");
                this.tIME_minute = this.getIntAttribute(node, "minute");
                this.tIME_second = this.getIntAttribute(node, "second");
                this.tIME_present = true;
                continue;
            }
            if (name.equals("tRNS")) {
                String tRNS_name;
                this.tRNS_present = false;
                Node tRNS_node = node.getFirstChild();
                if (tRNS_node == null) {
                    this.fatal(node, "tRNS node has no children!");
                }
                if ((tRNS_name = tRNS_node.getNodeName()).equals("tRNS_Palette")) {
                    byte[] alpha = new byte[256];
                    int maxindex = -1;
                    Node tRNS_paletteEntry = tRNS_node.getFirstChild();
                    if (tRNS_paletteEntry == null) {
                        this.fatal(node, "tRNS_Palette node has no children!");
                    }
                    while (tRNS_paletteEntry != null) {
                        int index5;
                        if (!tRNS_paletteEntry.getNodeName().equals("tRNS_PaletteEntry")) {
                            this.fatal(node, "Only a tRNS_PaletteEntry may be a child of a tRNS_Palette!");
                        }
                        if ((index5 = this.getIntAttribute(tRNS_paletteEntry, "index")) < 0 || index5 > 255) {
                            this.fatal(node, "Bad value for tRNS_PaletteEntry attribute index!");
                        }
                        if (index5 > maxindex) {
                            maxindex = index5;
                        }
                        alpha[index5] = (byte)this.getIntAttribute(tRNS_paletteEntry, "alpha");
                        tRNS_paletteEntry = tRNS_paletteEntry.getNextSibling();
                    }
                    int numEntries = maxindex + 1;
                    this.tRNS_alpha = new byte[numEntries];
                    this.tRNS_colorType = 3;
                    System.arraycopy(alpha, 0, this.tRNS_alpha, 0, numEntries);
                } else if (tRNS_name.equals("tRNS_Grayscale")) {
                    this.tRNS_gray = this.getIntAttribute(tRNS_node, "gray");
                    this.tRNS_colorType = 0;
                } else if (tRNS_name.equals("tRNS_RGB")) {
                    this.tRNS_red = this.getIntAttribute(tRNS_node, "red");
                    this.tRNS_green = this.getIntAttribute(tRNS_node, "green");
                    this.tRNS_blue = this.getIntAttribute(tRNS_node, "blue");
                    this.tRNS_colorType = 2;
                } else {
                    this.fatal(node, "Bad child of a tRNS node!");
                }
                if (tRNS_node.getNextSibling() != null) {
                    this.fatal(node, "tRNS node has more than one child!");
                }
                this.tRNS_present = true;
                continue;
            }
            if (name.equals("zTXt")) {
                for (Node zTXt_node = node.getFirstChild(); zTXt_node != null; zTXt_node = zTXt_node.getNextSibling()) {
                    if (!zTXt_node.getNodeName().equals("zTXtEntry")) {
                        this.fatal(node, "Only an zTXtEntry may be a child of an zTXt!");
                    }
                    String keyword = this.getAttribute(zTXt_node, "keyword");
                    this.zTXt_keyword.add(keyword);
                    int compressionMethod = this.getEnumeratedAttribute(zTXt_node, "compressionMethod", zTXt_compressionMethodNames);
                    this.zTXt_compressionMethod.add(compressionMethod);
                    String text = this.getAttribute(zTXt_node, "text");
                    this.zTXt_text.add(text);
                    if (!keyword.equals(tEXt_creationTimeKey)) continue;
                    int index6 = this.zTXt_text.size() - 1;
                    this.decodeImageCreationTimeFromTextChunk(this.zTXt_text.listIterator(index6));
                }
                continue;
            }
            if (name.equals("UnknownChunks")) {
                for (Node unknown_node = node.getFirstChild(); unknown_node != null; unknown_node = unknown_node.getNextSibling()) {
                    if (!unknown_node.getNodeName().equals("UnknownChunk")) {
                        this.fatal(node, "Only an UnknownChunk may be a child of an UnknownChunks!");
                    }
                    String chunkType = this.getAttribute(unknown_node, "type");
                    Object chunkData = ((IIOMetadataNode)unknown_node).getUserObject();
                    if (chunkType.length() != 4) {
                        this.fatal(unknown_node, "Chunk type must be 4 characters!");
                    }
                    if (chunkData == null) {
                        this.fatal(unknown_node, "No chunk data present in user object!");
                    }
                    if (!(chunkData instanceof byte[])) {
                        this.fatal(unknown_node, "User object not a byte array!");
                    }
                    this.unknownChunkType.add(chunkType);
                    this.unknownChunkData.add((byte[])((byte[])chunkData).clone());
                }
                continue;
            }
            this.fatal(node, "Unknown child of root node!");
        }
    }

    private boolean isValidKeyword(String s) {
        int len = s.length();
        if (len < 1 || len >= 80) {
            return false;
        }
        if (s.startsWith(" ") || s.endsWith(" ") || s.contains("  ")) {
            return false;
        }
        return this.isISOLatin(s, false);
    }

    private boolean isISOLatin(String s, boolean isLineFeedAllowed) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c >= ' ' && c <= '\u00ff' && (c <= '~' || c >= '\u00a1') || isLineFeedAllowed && c == '\u0010') continue;
            return false;
        }
        return true;
    }

    private void mergeStandardTree(Node root) throws IIOInvalidTreeException {
        Node node = root;
        if (!node.getNodeName().equals("javax_imageio_1.0")) {
            this.fatal(node, "Root must be javax_imageio_1.0");
        }
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
            String name = node.getNodeName();
            if (name.equals("Chroma")) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    String childName = child.getNodeName();
                    if (childName.equals("Gamma")) {
                        float gamma = this.getFloatAttribute(child, "value");
                        this.gAMA_present = true;
                        this.gAMA_gamma = (int)((double)(gamma * 100000.0f) + 0.5);
                        continue;
                    }
                    if (childName.equals("Palette")) {
                        byte[] red = new byte[256];
                        byte[] green = new byte[256];
                        byte[] blue = new byte[256];
                        int maxindex = -1;
                        for (Node entry = child.getFirstChild(); entry != null; entry = entry.getNextSibling()) {
                            int index = this.getIntAttribute(entry, "index");
                            if (index < 0 || index > 255) continue;
                            red[index] = (byte)this.getIntAttribute(entry, "red");
                            green[index] = (byte)this.getIntAttribute(entry, "green");
                            blue[index] = (byte)this.getIntAttribute(entry, "blue");
                            if (index <= maxindex) continue;
                            maxindex = index;
                        }
                        int numEntries = maxindex + 1;
                        this.PLTE_red = new byte[numEntries];
                        this.PLTE_green = new byte[numEntries];
                        this.PLTE_blue = new byte[numEntries];
                        System.arraycopy(red, 0, this.PLTE_red, 0, numEntries);
                        System.arraycopy(green, 0, this.PLTE_green, 0, numEntries);
                        System.arraycopy(blue, 0, this.PLTE_blue, 0, numEntries);
                        this.PLTE_present = true;
                        continue;
                    }
                    if (childName.equals("BackgroundIndex")) {
                        this.bKGD_present = true;
                        this.bKGD_colorType = 3;
                        this.bKGD_index = this.getIntAttribute(child, "value");
                        continue;
                    }
                    if (!childName.equals("BackgroundColor")) continue;
                    int red = this.getIntAttribute(child, "red");
                    int green = this.getIntAttribute(child, "green");
                    int blue = this.getIntAttribute(child, "blue");
                    if (red == green && red == blue) {
                        this.bKGD_colorType = 0;
                        this.bKGD_gray = red;
                    } else {
                        this.bKGD_colorType = 2;
                        this.bKGD_red = red;
                        this.bKGD_green = green;
                        this.bKGD_blue = blue;
                    }
                    this.bKGD_present = true;
                }
                continue;
            }
            if (name.equals("Compression")) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    String childName = child.getNodeName();
                    if (!childName.equals("NumProgressiveScans")) continue;
                    int scans = this.getIntAttribute(child, "value");
                    this.IHDR_interlaceMethod = scans > 1 ? 1 : 0;
                }
                continue;
            }
            if (name.equals("Data")) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    StringTokenizer t;
                    String s;
                    String childName = child.getNodeName();
                    if (childName.equals("BitsPerSample")) {
                        s = this.getAttribute(child, "value");
                        t = new StringTokenizer(s);
                        int maxBits = -1;
                        while (t.hasMoreTokens()) {
                            int bits = Integer.parseInt(t.nextToken());
                            if (bits <= maxBits) continue;
                            maxBits = bits;
                        }
                        if (maxBits < 1) {
                            maxBits = 1;
                        }
                        if (maxBits == 3) {
                            maxBits = 4;
                        }
                        if (maxBits > 4 || maxBits < 8) {
                            maxBits = 8;
                        }
                        if (maxBits > 8) {
                            maxBits = 16;
                        }
                        this.IHDR_bitDepth = maxBits;
                        continue;
                    }
                    if (!childName.equals("SignificantBitsPerSample")) continue;
                    s = this.getAttribute(child, "value");
                    t = new StringTokenizer(s);
                    int numTokens = t.countTokens();
                    if (numTokens == 1) {
                        this.sBIT_colorType = 0;
                        this.sBIT_grayBits = Integer.parseInt(t.nextToken());
                    } else if (numTokens == 2) {
                        this.sBIT_colorType = 4;
                        this.sBIT_grayBits = Integer.parseInt(t.nextToken());
                        this.sBIT_alphaBits = Integer.parseInt(t.nextToken());
                    } else if (numTokens == 3) {
                        this.sBIT_colorType = 2;
                        this.sBIT_redBits = Integer.parseInt(t.nextToken());
                        this.sBIT_greenBits = Integer.parseInt(t.nextToken());
                        this.sBIT_blueBits = Integer.parseInt(t.nextToken());
                    } else if (numTokens == 4) {
                        this.sBIT_colorType = 6;
                        this.sBIT_redBits = Integer.parseInt(t.nextToken());
                        this.sBIT_greenBits = Integer.parseInt(t.nextToken());
                        this.sBIT_blueBits = Integer.parseInt(t.nextToken());
                        this.sBIT_alphaBits = Integer.parseInt(t.nextToken());
                    }
                    if (numTokens < 1 || numTokens > 4) continue;
                    this.sBIT_present = true;
                }
                continue;
            }
            if (name.equals("Dimension")) {
                int num;
                int denom;
                boolean gotWidth = false;
                boolean gotHeight = false;
                boolean gotAspectRatio = false;
                float width = -1.0f;
                float height = -1.0f;
                float aspectRatio = -1.0f;
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    String childName = child.getNodeName();
                    if (childName.equals("PixelAspectRatio")) {
                        aspectRatio = this.getFloatAttribute(child, "value");
                        gotAspectRatio = true;
                        continue;
                    }
                    if (childName.equals("HorizontalPixelSize")) {
                        width = this.getFloatAttribute(child, "value");
                        gotWidth = true;
                        continue;
                    }
                    if (!childName.equals("VerticalPixelSize")) continue;
                    height = this.getFloatAttribute(child, "value");
                    gotHeight = true;
                }
                if (gotWidth && gotHeight) {
                    this.pHYs_present = true;
                    this.pHYs_unitSpecifier = 1;
                    this.pHYs_pixelsPerUnitXAxis = (int)(width * 1000.0f + 0.5f);
                    this.pHYs_pixelsPerUnitYAxis = (int)(height * 1000.0f + 0.5f);
                    continue;
                }
                if (!gotAspectRatio) continue;
                this.pHYs_present = true;
                this.pHYs_unitSpecifier = 0;
                for (denom = 1; denom < 100 && !((double)Math.abs((float)((num = (int)(aspectRatio * (float)denom)) / denom) - aspectRatio) < 0.001); ++denom) {
                }
                this.pHYs_pixelsPerUnitXAxis = (int)(aspectRatio * (float)denom);
                this.pHYs_pixelsPerUnitYAxis = denom;
                continue;
            }
            if (name.equals("Document")) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    String childName = child.getNodeName();
                    if (childName.equals("ImageModificationTime")) {
                        this.tIME_present = true;
                        this.tIME_year = this.getIntAttribute(child, "year");
                        this.tIME_month = this.getIntAttribute(child, "month");
                        this.tIME_day = this.getIntAttribute(child, "day");
                        this.tIME_hour = this.getIntAttribute(child, "hour", 0, false);
                        this.tIME_minute = this.getIntAttribute(child, "minute", 0, false);
                        this.tIME_second = this.getIntAttribute(child, "second", 0, false);
                        continue;
                    }
                    if (!childName.equals("ImageCreationTime")) continue;
                    int year = this.getIntAttribute(child, "year");
                    int month = this.getIntAttribute(child, "month");
                    int day = this.getIntAttribute(child, "day");
                    int hour = this.getIntAttribute(child, "hour", 0, false);
                    int mins = this.getIntAttribute(child, "minute", 0, false);
                    int sec = this.getIntAttribute(child, "second", 0, false);
                    this.initImageCreationTime(year, month, day, hour, mins, sec);
                    this.encodeImageCreationTimeToTextChunk();
                }
                continue;
            }
            if (!name.equals("Text")) continue;
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                String childName = child.getNodeName();
                if (!childName.equals("TextEntry")) continue;
                String keyword = this.getAttribute(child, "keyword", "", false);
                String value = this.getAttribute(child, "value");
                String language = this.getAttribute(child, "language", "", false);
                String compression = this.getAttribute(child, "compression", "none", false);
                if (!this.isValidKeyword(keyword)) continue;
                if (this.isISOLatin(value, true)) {
                    if (compression.equals("zip")) {
                        this.zTXt_keyword.add(keyword);
                        this.zTXt_text.add(value);
                        this.zTXt_compressionMethod.add(0);
                        continue;
                    }
                    this.tEXt_keyword.add(keyword);
                    this.tEXt_text.add(value);
                    continue;
                }
                this.iTXt_keyword.add(keyword);
                this.iTXt_compressionFlag.add(compression.equals("zip"));
                this.iTXt_compressionMethod.add(0);
                this.iTXt_languageTag.add(language);
                this.iTXt_translatedKeyword.add(keyword);
                this.iTXt_text.add(value);
            }
        }
    }

    void initImageCreationTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            this.creation_time_present = true;
            this.creation_time_year = offsetDateTime.getYear();
            this.creation_time_month = offsetDateTime.getMonthValue();
            this.creation_time_day = offsetDateTime.getDayOfMonth();
            this.creation_time_hour = offsetDateTime.getHour();
            this.creation_time_minute = offsetDateTime.getMinute();
            this.creation_time_second = offsetDateTime.getSecond();
            this.creation_time_offset = offsetDateTime.getOffset();
        }
    }

    void initImageCreationTime(int year, int month, int day, int hour, int min, int second) {
        LocalDateTime locDT = LocalDateTime.of(year, month, day, hour, min, second);
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(locDT);
        OffsetDateTime offDateTime = OffsetDateTime.of(locDT, offset);
        this.initImageCreationTime(offDateTime);
    }

    void decodeImageCreationTimeFromTextChunk(ListIterator<String> iterChunk) {
        if (iterChunk != null && iterChunk.hasNext()) {
            this.setCreationTimeChunk(iterChunk);
            String encodedTime = this.getEncodedTime();
            this.initImageCreationTime(this.parseEncodedTime(encodedTime));
        }
    }

    void encodeImageCreationTimeToTextChunk() {
        if (this.creation_time_present) {
            if (!this.tEXt_creation_time_present) {
                this.tEXt_keyword.add(tEXt_creationTimeKey);
                this.tEXt_text.add("Creation Time Place Holder");
                int index = this.tEXt_text.size() - 1;
                this.setCreationTimeChunk(this.tEXt_text.listIterator(index));
            }
            OffsetDateTime offDateTime = OffsetDateTime.of(this.creation_time_year, this.creation_time_month, this.creation_time_day, this.creation_time_hour, this.creation_time_minute, this.creation_time_second, 0, this.creation_time_offset);
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            String encodedTime = offDateTime.format(formatter);
            this.setEncodedTime(encodedTime);
        }
    }

    private void setCreationTimeChunk(ListIterator<String> iter) {
        if (iter != null && iter.hasNext()) {
            this.tEXt_creation_time_iter = iter;
            this.tEXt_creation_time_present = true;
        }
    }

    private void setEncodedTime(String encodedTime) {
        if (this.tEXt_creation_time_iter != null && this.tEXt_creation_time_iter.hasNext() && encodedTime != null) {
            this.tEXt_creation_time_iter.next();
            this.tEXt_creation_time_iter.set(encodedTime);
            this.tEXt_creation_time_iter.previous();
        }
    }

    private String getEncodedTime() {
        String encodedTime = null;
        if (this.tEXt_creation_time_iter != null && this.tEXt_creation_time_iter.hasNext()) {
            encodedTime = this.tEXt_creation_time_iter.next();
            this.tEXt_creation_time_iter.previous();
        }
        return encodedTime;
    }

    private OffsetDateTime parseEncodedTime(String encodedTime) {
        OffsetDateTime retVal = null;
        boolean timeDecoded = false;
        try {
            retVal = OffsetDateTime.parse(encodedTime, DateTimeFormatter.RFC_1123_DATE_TIME);
            timeDecoded = true;
        }
        catch (DateTimeParseException dateTimeParseException) {
            // empty catch block
        }
        if (!timeDecoded) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                TemporalAccessor dt = formatter.parseBest(encodedTime, OffsetDateTime::from, LocalDateTime::from);
                if (dt instanceof OffsetDateTime) {
                    retVal = (OffsetDateTime)dt;
                } else if (dt instanceof LocalDateTime) {
                    LocalDateTime locDT = (LocalDateTime)dt;
                    retVal = OffsetDateTime.of(locDT, ZoneOffset.UTC);
                }
            }
            catch (DateTimeParseException dateTimeParseException) {
                // empty catch block
            }
        }
        return retVal;
    }

    boolean hasTransparentColor() {
        return this.tRNS_present && (this.tRNS_colorType == 2 || this.tRNS_colorType == 0);
    }

    @Override
    public void reset() {
        this.IHDR_present = false;
        this.PLTE_present = false;
        this.bKGD_present = false;
        this.cHRM_present = false;
        this.gAMA_present = false;
        this.hIST_present = false;
        this.iCCP_present = false;
        this.iTXt_keyword = new ArrayList();
        this.iTXt_compressionFlag = new ArrayList();
        this.iTXt_compressionMethod = new ArrayList();
        this.iTXt_languageTag = new ArrayList();
        this.iTXt_translatedKeyword = new ArrayList();
        this.iTXt_text = new ArrayList();
        this.pHYs_present = false;
        this.sBIT_present = false;
        this.sPLT_present = false;
        this.sRGB_present = false;
        this.tEXt_keyword = new ArrayList();
        this.tEXt_text = new ArrayList();
        this.tIME_present = false;
        this.tEXt_creation_time_present = false;
        this.tEXt_creation_time_iter = null;
        this.creation_time_present = false;
        this.tRNS_present = false;
        this.zTXt_keyword = new ArrayList();
        this.zTXt_compressionMethod = new ArrayList();
        this.zTXt_text = new ArrayList();
        this.unknownChunkType = new ArrayList();
        this.unknownChunkData = new ArrayList();
    }
}

