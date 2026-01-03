/*
 * Decompiled with CFR 0.152.
 */
package java.awt.color;

import java.awt.color.CMMException;
import java.awt.color.ICC_ProfileGray;
import java.awt.color.ICC_ProfileRGB;
import java.awt.color.ProfileDataException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.util.Objects;
import java.util.StringTokenizer;
import sun.awt.AWTAccessor;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.Profile;
import sun.java2d.cmm.ProfileDataVerifier;
import sun.java2d.cmm.ProfileDeferralInfo;

public sealed class ICC_Profile
implements Serializable
permits ICC_ProfileGray, ICC_ProfileRGB {
    private static final long serialVersionUID = -3938515861990936766L;
    private volatile transient Profile cmmProfile;
    private volatile transient ProfileDeferralInfo deferralInfo;
    public static final int CLASS_INPUT = 0;
    public static final int CLASS_DISPLAY = 1;
    public static final int CLASS_OUTPUT = 2;
    public static final int CLASS_DEVICELINK = 3;
    public static final int CLASS_COLORSPACECONVERSION = 4;
    public static final int CLASS_ABSTRACT = 5;
    public static final int CLASS_NAMEDCOLOR = 6;
    public static final int icSigXYZData = 1482250784;
    public static final int icSigLabData = 1281450528;
    public static final int icSigLuvData = 1282766368;
    public static final int icSigYCbCrData = 1497588338;
    public static final int icSigYxyData = 1501067552;
    public static final int icSigRgbData = 1380401696;
    public static final int icSigGrayData = 1196573017;
    public static final int icSigHsvData = 1213421088;
    public static final int icSigHlsData = 1212961568;
    public static final int icSigCmykData = 1129142603;
    public static final int icSigCmyData = 1129142560;
    public static final int icSigSpace2CLR = 843271250;
    public static final int icSigSpace3CLR = 860048466;
    public static final int icSigSpace4CLR = 876825682;
    public static final int icSigSpace5CLR = 893602898;
    public static final int icSigSpace6CLR = 910380114;
    public static final int icSigSpace7CLR = 927157330;
    public static final int icSigSpace8CLR = 943934546;
    public static final int icSigSpace9CLR = 960711762;
    public static final int icSigSpaceACLR = 1094929490;
    public static final int icSigSpaceBCLR = 1111706706;
    public static final int icSigSpaceCCLR = 1128483922;
    public static final int icSigSpaceDCLR = 1145261138;
    public static final int icSigSpaceECLR = 1162038354;
    public static final int icSigSpaceFCLR = 1178815570;
    public static final int icSigInputClass = 1935896178;
    public static final int icSigDisplayClass = 1835955314;
    public static final int icSigOutputClass = 1886549106;
    public static final int icSigLinkClass = 1818848875;
    public static final int icSigAbstractClass = 1633842036;
    public static final int icSigColorSpaceClass = 1936744803;
    public static final int icSigNamedColorClass = 1852662636;
    public static final int icPerceptual = 0;
    public static final int icRelativeColorimetric = 1;
    public static final int icMediaRelativeColorimetric = 1;
    public static final int icSaturation = 2;
    public static final int icAbsoluteColorimetric = 3;
    public static final int icICCAbsoluteColorimetric = 3;
    public static final int icSigHead = 1751474532;
    public static final int icSigAToB0Tag = 1093812784;
    public static final int icSigAToB1Tag = 1093812785;
    public static final int icSigAToB2Tag = 1093812786;
    public static final int icSigBlueColorantTag = 1649957210;
    public static final int icSigBlueMatrixColumnTag = 1649957210;
    public static final int icSigBlueTRCTag = 1649693251;
    public static final int icSigBToA0Tag = 1110589744;
    public static final int icSigBToA1Tag = 1110589745;
    public static final int icSigBToA2Tag = 1110589746;
    public static final int icSigCalibrationDateTimeTag = 1667329140;
    public static final int icSigCharTargetTag = 1952543335;
    public static final int icSigCopyrightTag = 1668313716;
    public static final int icSigCrdInfoTag = 1668441193;
    public static final int icSigDeviceMfgDescTag = 1684893284;
    public static final int icSigDeviceModelDescTag = 0x646D6464;
    public static final int icSigDeviceSettingsTag = 1684371059;
    public static final int icSigGamutTag = 1734438260;
    public static final int icSigGrayTRCTag = 1800688195;
    public static final int icSigGreenColorantTag = 1733843290;
    public static final int icSigGreenMatrixColumnTag = 1733843290;
    public static final int icSigGreenTRCTag = 1733579331;
    public static final int icSigLuminanceTag = 1819635049;
    public static final int icSigMeasurementTag = 1835360627;
    public static final int icSigMediaBlackPointTag = 1651208308;
    public static final int icSigMediaWhitePointTag = 0x77747074;
    public static final int icSigNamedColor2Tag = 1852009522;
    public static final int icSigOutputResponseTag = 1919251312;
    public static final int icSigPreview0Tag = 1886545200;
    public static final int icSigPreview1Tag = 1886545201;
    public static final int icSigPreview2Tag = 1886545202;
    public static final int icSigProfileDescriptionTag = 1684370275;
    public static final int icSigProfileSequenceDescTag = 1886610801;
    public static final int icSigPs2CRD0Tag = 1886610480;
    public static final int icSigPs2CRD1Tag = 1886610481;
    public static final int icSigPs2CRD2Tag = 1886610482;
    public static final int icSigPs2CRD3Tag = 1886610483;
    public static final int icSigPs2CSATag = 1886597747;
    public static final int icSigPs2RenderingIntentTag = 1886597737;
    public static final int icSigRedColorantTag = 1918392666;
    public static final int icSigRedMatrixColumnTag = 1918392666;
    public static final int icSigRedTRCTag = 1918128707;
    public static final int icSigScreeningDescTag = 1935897188;
    public static final int icSigScreeningTag = 1935897198;
    public static final int icSigTechnologyTag = 1952801640;
    public static final int icSigUcrBgTag = 1650877472;
    public static final int icSigViewingCondDescTag = 1987405156;
    public static final int icSigViewingConditionsTag = 1986618743;
    public static final int icSigChromaticityTag = 1667789421;
    public static final int icSigChromaticAdaptationTag = 1667785060;
    public static final int icSigColorantOrderTag = 1668051567;
    public static final int icSigColorantTableTag = 1668051572;
    public static final int icHdrSize = 0;
    public static final int icHdrCmmId = 4;
    public static final int icHdrVersion = 8;
    public static final int icHdrDeviceClass = 12;
    public static final int icHdrColorSpace = 16;
    public static final int icHdrPcs = 20;
    public static final int icHdrDate = 24;
    public static final int icHdrMagic = 36;
    public static final int icHdrPlatform = 40;
    public static final int icHdrFlags = 44;
    public static final int icHdrManufacturer = 48;
    public static final int icHdrModel = 52;
    public static final int icHdrAttributes = 56;
    public static final int icHdrRenderingIntent = 64;
    public static final int icHdrIlluminant = 68;
    public static final int icHdrCreator = 80;
    public static final int icHdrProfileID = 84;
    public static final int icTagType = 0;
    public static final int icTagReserved = 4;
    public static final int icCurveCount = 8;
    public static final int icCurveData = 12;
    public static final int icXYZNumberX = 8;
    private int iccProfileSerializedDataVersion = 1;
    private transient ICC_Profile resolvedDeserializedProfile;

    ICC_Profile(Profile p) {
        this.cmmProfile = p;
    }

    ICC_Profile(ProfileDeferralInfo pdi) {
        this.deferralInfo = pdi;
    }

    public static ICC_Profile getInstance(byte[] data) {
        Profile p;
        ProfileDataVerifier.verify(data);
        try {
            p = CMSManager.getModule().loadProfile(data);
        }
        catch (CMMException c) {
            throw new IllegalArgumentException("Invalid ICC Profile Data");
        }
        try {
            if (ICC_Profile.getColorSpaceType(p) == 6 && ICC_Profile.getData(p, 0x77747074) != null && ICC_Profile.getData(p, 1800688195) != null) {
                return new ICC_ProfileGray(p);
            }
            if (ICC_Profile.getColorSpaceType(p) == 5 && ICC_Profile.getData(p, 0x77747074) != null && ICC_Profile.getData(p, 1918392666) != null && ICC_Profile.getData(p, 1733843290) != null && ICC_Profile.getData(p, 1649957210) != null && ICC_Profile.getData(p, 1918128707) != null && ICC_Profile.getData(p, 1733579331) != null && ICC_Profile.getData(p, 1649693251) != null) {
                return new ICC_ProfileRGB(p);
            }
        }
        catch (CMMException cMMException) {
            // empty catch block
        }
        return new ICC_Profile(p);
    }

    public static ICC_Profile getInstance(int cspace) {
        return switch (cspace) {
            case 1000 -> BuiltInProfile.SRGB;
            case 1004 -> BuiltInProfile.LRGB;
            case 1001 -> BuiltInProfile.XYZ;
            case 1002 -> BuiltInProfile.PYCC;
            case 1003 -> BuiltInProfile.GRAY;
            default -> throw new IllegalArgumentException("Unknown color space");
        };
    }

    public static ICC_Profile getInstance(String fileName) throws IOException {
        File f = ICC_Profile.getProfileFile(fileName);
        InputStream is = f != null ? new FileInputStream(f) : ICC_Profile.getStandardProfileInputStream(fileName);
        if (is == null) {
            throw new IOException("Cannot open file " + fileName);
        }
        try (InputStream inputStream = is;){
            ICC_Profile iCC_Profile = ICC_Profile.getInstance(is);
            return iCC_Profile;
        }
    }

    public static ICC_Profile getInstance(InputStream s) throws IOException {
        Objects.requireNonNull(s);
        return ICC_Profile.getInstance(ICC_Profile.getProfileDataFromStream(s));
    }

    static byte[] getProfileDataFromStream(InputStream s) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(s);
        bis.mark(128);
        byte[] header = bis.readNBytes(128);
        if (header.length < 128 || header[36] != 97 || header[37] != 99 || header[38] != 115 || header[39] != 112) {
            return null;
        }
        int profileSize = ICC_Profile.intFromBigEndian(header, 0);
        bis.reset();
        try {
            return bis.readNBytes(profileSize);
        }
        catch (OutOfMemoryError e) {
            throw new IOException("Color profile is too big");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Profile cmmProfile() {
        Profile p = this.cmmProfile;
        if (p != null) {
            return p;
        }
        ICC_Profile iCC_Profile = this;
        synchronized (iCC_Profile) {
            if (this.cmmProfile != null) {
                return this.cmmProfile;
            }
            InputStream is = ICC_Profile.getStandardProfileInputStream(this.deferralInfo.filename);
            if (is == null) {
                return null;
            }
            try (InputStream inputStream = is;){
                byte[] data = ICC_Profile.getProfileDataFromStream(is);
                if (data != null) {
                    p = this.cmmProfile = CMSManager.getModule().loadProfile(data);
                    this.deferralInfo = null;
                }
            }
            catch (CMMException | IOException exception) {
                // empty catch block
            }
        }
        return p;
    }

    public int getMajorVersion() {
        return this.getData(1751474532)[8];
    }

    public int getMinorVersion() {
        return this.getData(1751474532)[9];
    }

    public int getProfileClass() {
        ProfileDeferralInfo info = this.deferralInfo;
        if (info != null) {
            return info.profileClass;
        }
        byte[] theHeader = this.getData(1751474532);
        int theClassSig = ICC_Profile.intFromBigEndian(theHeader, 12);
        return switch (theClassSig) {
            case 1935896178 -> 0;
            case 1835955314 -> 1;
            case 1886549106 -> 2;
            case 1818848875 -> 3;
            case 1936744803 -> 4;
            case 1633842036 -> 5;
            case 1852662636 -> 6;
            default -> throw new IllegalArgumentException("Unknown profile class");
        };
    }

    public int getColorSpaceType() {
        ProfileDeferralInfo info = this.deferralInfo;
        if (info != null) {
            return info.colorSpaceType;
        }
        return ICC_Profile.getColorSpaceType(this.cmmProfile());
    }

    private static int getColorSpaceType(Profile p) {
        byte[] theHeader = ICC_Profile.getData(p, 1751474532);
        int theColorSpaceSig = ICC_Profile.intFromBigEndian(theHeader, 16);
        return ICC_Profile.iccCStoJCS(theColorSpaceSig);
    }

    public int getPCSType() {
        byte[] theHeader = this.getData(1751474532);
        int thePCSSig = ICC_Profile.intFromBigEndian(theHeader, 20);
        return ICC_Profile.iccCStoJCS(thePCSSig);
    }

    public void write(String fileName) throws IOException {
        try (FileOutputStream out = new FileOutputStream(fileName);){
            this.write(out);
        }
    }

    public void write(OutputStream s) throws IOException {
        s.write(this.getData());
    }

    public byte[] getData() {
        return CMSManager.getModule().getProfileData(this.cmmProfile());
    }

    public byte[] getData(int tagSignature) {
        byte[] t = ICC_Profile.getData(this.cmmProfile(), tagSignature);
        return t != null ? (byte[])t.clone() : null;
    }

    private static byte[] getData(Profile p, int tagSignature) {
        try {
            return CMSManager.getModule().getTagData(p, tagSignature);
        }
        catch (CMMException c) {
            return null;
        }
    }

    public void setData(int tagSignature, byte[] tagData) {
        CMSManager.getModule().setTagData(this.cmmProfile(), tagSignature, tagData);
    }

    public int getNumComponents() {
        ProfileDeferralInfo info = this.deferralInfo;
        if (info != null) {
            return info.numComponents;
        }
        byte[] theHeader = this.getData(1751474532);
        int theColorSpaceSig = ICC_Profile.intFromBigEndian(theHeader, 16);
        return switch (theColorSpaceSig) {
            case 1196573017 -> 1;
            case 843271250 -> 2;
            case 860048466, 1129142560, 1212961568, 1213421088, 1281450528, 1282766368, 1380401696, 1482250784, 1497588338, 1501067552 -> 3;
            case 876825682, 1129142603 -> 4;
            case 893602898 -> 5;
            case 910380114 -> 6;
            case 927157330 -> 7;
            case 943934546 -> 8;
            case 960711762 -> 9;
            case 1094929490 -> 10;
            case 1111706706 -> 11;
            case 1128483922 -> 12;
            case 1145261138 -> 13;
            case 1162038354 -> 14;
            case 1178815570 -> 15;
            default -> throw new ProfileDataException("invalid ICC color space");
        };
    }

    float[] getMediaWhitePoint() {
        return this.getXYZTag(0x77747074);
    }

    final float[] getXYZTag(int tagSignature) {
        byte[] theData = this.getData(tagSignature);
        float[] theXYZNumber = new float[3];
        int i1 = 0;
        int i2 = 8;
        while (i1 < 3) {
            int theS15Fixed16 = ICC_Profile.intFromBigEndian(theData, i2);
            theXYZNumber[i1] = (float)theS15Fixed16 / 65536.0f;
            ++i1;
            i2 += 4;
        }
        return theXYZNumber;
    }

    float getGamma(int tagSignature) {
        byte[] theTRCData = this.getData(tagSignature);
        if (ICC_Profile.intFromBigEndian(theTRCData, 8) != 1) {
            throw new ProfileDataException("TRC is not a gamma");
        }
        int theU8Fixed8 = ICC_Profile.shortFromBigEndian(theTRCData, 12) & 0xFFFF;
        return (float)theU8Fixed8 / 256.0f;
    }

    short[] getTRC(int tagSignature) {
        byte[] theTRCData = this.getData(tagSignature);
        int nElements = ICC_Profile.intFromBigEndian(theTRCData, 8);
        if (nElements == 1) {
            throw new ProfileDataException("TRC is not a table");
        }
        short[] theTRC = new short[nElements];
        int i1 = 0;
        int i2 = 12;
        while (i1 < nElements) {
            theTRC[i1] = ICC_Profile.shortFromBigEndian(theTRCData, i2);
            ++i1;
            i2 += 2;
        }
        return theTRC;
    }

    private static int iccCStoJCS(int theColorSpaceSig) {
        return switch (theColorSpaceSig) {
            case 1482250784 -> 0;
            case 1281450528 -> 1;
            case 1282766368 -> 2;
            case 1497588338 -> 3;
            case 1501067552 -> 4;
            case 1380401696 -> 5;
            case 1196573017 -> 6;
            case 1213421088 -> 7;
            case 1212961568 -> 8;
            case 1129142603 -> 9;
            case 1129142560 -> 11;
            case 843271250 -> 12;
            case 860048466 -> 13;
            case 876825682 -> 14;
            case 893602898 -> 15;
            case 910380114 -> 16;
            case 927157330 -> 17;
            case 943934546 -> 18;
            case 960711762 -> 19;
            case 1094929490 -> 20;
            case 1111706706 -> 21;
            case 1128483922 -> 22;
            case 1145261138 -> 23;
            case 1162038354 -> 24;
            case 1178815570 -> 25;
            default -> throw new IllegalArgumentException("Unknown color space");
        };
    }

    private static int intFromBigEndian(byte[] array, int index) {
        return (array[index] & 0xFF) << 24 | (array[index + 1] & 0xFF) << 16 | (array[index + 2] & 0xFF) << 8 | array[index + 3] & 0xFF;
    }

    private static short shortFromBigEndian(byte[] array, int index) {
        return (short)((array[index] & 0xFF) << 8 | array[index + 1] & 0xFF);
    }

    private static File getProfileFile(String fileName) {
        String fullPath;
        String dir;
        StringTokenizer st;
        String path;
        File f = new File(fileName);
        if (f.isAbsolute()) {
            return f.isFile() ? f : null;
        }
        if (!f.isFile() && (path = System.getProperty("java.iccprofile.path")) != null) {
            st = new StringTokenizer(path, File.pathSeparator);
            while (st.hasMoreTokens() && (f == null || !f.isFile())) {
                dir = st.nextToken();
                fullPath = dir + File.separatorChar + fileName;
                f = new File(fullPath);
                if (ICC_Profile.isChildOf(f, dir)) continue;
                f = null;
            }
        }
        if (!(f != null && f.isFile() || (path = System.getProperty("java.class.path")) == null)) {
            st = new StringTokenizer(path, File.pathSeparator);
            while (st.hasMoreTokens() && (f == null || !f.isFile())) {
                dir = st.nextToken();
                fullPath = dir + File.separatorChar + fileName;
                f = new File(fullPath);
            }
        }
        if (f != null && !f.isFile()) {
            f = null;
        }
        return f;
    }

    private static InputStream getStandardProfileInputStream(String fileName) {
        return AccessController.doPrivileged(() -> PCMM.class.getResourceAsStream("profiles/" + fileName), null, new FilePermission("<<ALL FILES>>", "read"), new RuntimePermission("accessSystemModules"));
    }

    private static boolean isChildOf(File f, String dirName) {
        try {
            File dir = new File(dirName);
            Object canonicalDirName = dir.getCanonicalPath();
            if (!((String)canonicalDirName).endsWith(File.separator)) {
                canonicalDirName = (String)canonicalDirName + File.separator;
            }
            String canonicalFileName = f.getCanonicalPath();
            return canonicalFileName.startsWith((String)canonicalDirName);
        }
        catch (IOException e) {
            return false;
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        String csName = null;
        if (this == BuiltInProfile.SRGB) {
            csName = "CS_sRGB";
        } else if (this == BuiltInProfile.XYZ) {
            csName = "CS_CIEXYZ";
        } else if (this == BuiltInProfile.PYCC) {
            csName = "CS_PYCC";
        } else if (this == BuiltInProfile.GRAY) {
            csName = "CS_GRAY";
        } else if (this == BuiltInProfile.LRGB) {
            csName = "CS_LINEAR_RGB";
        }
        byte[] data = null;
        if (csName == null) {
            data = this.getData();
        }
        s.writeObject(csName);
        s.writeObject(data);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        String csName = (String)s.readObject();
        byte[] data = (byte[])s.readObject();
        int cspace = 0;
        boolean isKnownPredefinedCS = false;
        if (csName != null) {
            isKnownPredefinedCS = true;
            if (csName.equals("CS_sRGB")) {
                cspace = 1000;
            } else if (csName.equals("CS_CIEXYZ")) {
                cspace = 1001;
            } else if (csName.equals("CS_PYCC")) {
                cspace = 1002;
            } else if (csName.equals("CS_GRAY")) {
                cspace = 1003;
            } else if (csName.equals("CS_LINEAR_RGB")) {
                cspace = 1004;
            } else {
                isKnownPredefinedCS = false;
            }
        }
        this.resolvedDeserializedProfile = isKnownPredefinedCS ? ICC_Profile.getInstance(cspace) : ICC_Profile.getInstance(data);
    }

    protected Object readResolve() throws ObjectStreamException {
        return this.resolvedDeserializedProfile;
    }

    static {
        AWTAccessor.setICC_ProfileAccessor(ICC_Profile::cmmProfile);
    }

    private static interface BuiltInProfile {
        public static final ICC_Profile SRGB = new ICC_ProfileRGB(new ProfileDeferralInfo("sRGB.pf", 5, 3, 1));
        public static final ICC_Profile LRGB = new ICC_ProfileRGB(new ProfileDeferralInfo("LINEAR_RGB.pf", 5, 3, 1));
        public static final ICC_Profile XYZ = new ICC_Profile(new ProfileDeferralInfo("CIEXYZ.pf", 0, 3, 5));
        public static final ICC_Profile PYCC = new ICC_Profile(new ProfileDeferralInfo("PYCC.pf", 13, 3, 4));
        public static final ICC_Profile GRAY = new ICC_ProfileGray(new ProfileDeferralInfo("GRAY.pf", 6, 1, 1));
    }
}

