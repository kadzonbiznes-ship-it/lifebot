/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ApertureValue
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$BrightnessValue
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$CFAPattern
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ColorSpace
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ComponentsConfiguration
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$CompressedBitsPerPixel
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$Contrast
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$CustomRendered
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$DateTimeDigitized
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$DateTimeOriginal
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$DeviceSettingDescription
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$DigitalZoomRatio
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$EXIFVersion
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ExposureBiasValue
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ExposureIndex
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ExposureMode
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ExposureProgram
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ExposureTime
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FNumber
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FileSource
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$Flash
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FlashEnergy
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FlashPixVersion
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FocalLength
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FocalLengthIn35mmFilm
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FocalPlaneResolutionUnit
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FocalPlaneXResolution
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$FocalPlaneYResolution
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$GainControl
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ISOSpeedRatings
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ImageUniqueID
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$InteroperabilityIFD
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$LightSource
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$MakerNote
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$MaxApertureValue
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$MeteringMode
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$OECF
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$PixelXDimension
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$PixelYDimension
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$RelatedSoundFile
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$Saturation
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SceneCaptureType
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SceneType
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SensingMethod
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$Sharpness
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$ShutterSpeedValue
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SpatialFrequencyResponse
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SpectralSensitivity
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SubSecTime
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SubSecTimeDigitized
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SubSecTimeOriginal
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SubjectArea
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SubjectDistance
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SubjectDistanceRange
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$SubjectLocation
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$UserComment
 *  com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet$WhiteBalance
 */
package com.github.jaiimageio.plugins.tiff;

import com.github.jaiimageio.plugins.tiff.EXIFTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.TIFFTagSet;
import java.util.ArrayList;
import java.util.List;

public class EXIFTIFFTagSet
extends TIFFTagSet {
    private static EXIFTIFFTagSet theInstance = null;
    public static final int TAG_GPS_INFO_IFD_POINTER = 34853;
    public static final int TAG_INTEROPERABILITY_IFD_POINTER = 40965;
    public static final int TAG_EXIF_VERSION = 36864;
    public static byte[] EXIF_VERSION_2_1 = new byte[]{48, 50, 49, 48};
    public static byte[] EXIF_VERSION_2_2 = new byte[]{48, 50, 50, 48};
    public static final int TAG_FLASHPIX_VERSION = 40960;
    public static final int TAG_COLOR_SPACE = 40961;
    public static final int COLOR_SPACE_SRGB = 1;
    public static final int COLOR_SPACE_UNCALIBRATED = 65535;
    public static final int TAG_COMPONENTS_CONFIGURATION = 37121;
    public static final int COMPONENTS_CONFIGURATION_DOES_NOT_EXIST = 0;
    public static final int COMPONENTS_CONFIGURATION_Y = 1;
    public static final int COMPONENTS_CONFIGURATION_CB = 2;
    public static final int COMPONENTS_CONFIGURATION_CR = 3;
    public static final int COMPONENTS_CONFIGURATION_R = 4;
    public static final int COMPONENTS_CONFIGURATION_G = 5;
    public static final int COMPONENTS_CONFIGURATION_B = 6;
    public static final int TAG_COMPRESSED_BITS_PER_PIXEL = 37122;
    public static final int TAG_PIXEL_X_DIMENSION = 40962;
    public static final int TAG_PIXEL_Y_DIMENSION = 40963;
    public static final int TAG_MAKER_NOTE = 37500;
    public static final int TAG_MARKER_NOTE = 37500;
    public static final int TAG_USER_COMMENT = 37510;
    public static final int TAG_RELATED_SOUND_FILE = 40964;
    public static final int TAG_DATE_TIME_ORIGINAL = 36867;
    public static final int TAG_DATE_TIME_DIGITIZED = 36868;
    public static final int TAG_SUB_SEC_TIME = 37520;
    public static final int TAG_SUB_SEC_TIME_ORIGINAL = 37521;
    public static final int TAG_SUB_SEC_TIME_DIGITIZED = 37522;
    public static final int TAG_EXPOSURE_TIME = 33434;
    public static final int TAG_F_NUMBER = 33437;
    public static final int TAG_EXPOSURE_PROGRAM = 34850;
    public static final int EXPOSURE_PROGRAM_NOT_DEFINED = 0;
    public static final int EXPOSURE_PROGRAM_MANUAL = 1;
    public static final int EXPOSURE_PROGRAM_NORMAL_PROGRAM = 2;
    public static final int EXPOSURE_PROGRAM_APERTURE_PRIORITY = 3;
    public static final int EXPOSURE_PROGRAM_SHUTTER_PRIORITY = 4;
    public static final int EXPOSURE_PROGRAM_CREATIVE_PROGRAM = 5;
    public static final int EXPOSURE_PROGRAM_ACTION_PROGRAM = 6;
    public static final int EXPOSURE_PROGRAM_PORTRAIT_MODE = 7;
    public static final int EXPOSURE_PROGRAM_LANDSCAPE_MODE = 8;
    public static final int EXPOSURE_PROGRAM_MAX_RESERVED = 255;
    public static final int TAG_SPECTRAL_SENSITIVITY = 34852;
    public static final int TAG_ISO_SPEED_RATINGS = 34855;
    public static final int TAG_OECF = 34856;
    public static final int TAG_SHUTTER_SPEED_VALUE = 37377;
    public static final int TAG_APERTURE_VALUE = 37378;
    public static final int TAG_BRIGHTNESS_VALUE = 37379;
    public static final int TAG_EXPOSURE_BIAS_VALUE = 37380;
    public static final int TAG_MAX_APERTURE_VALUE = 37381;
    public static final int TAG_SUBJECT_DISTANCE = 37382;
    public static final int TAG_METERING_MODE = 37383;
    public static final int METERING_MODE_UNKNOWN = 0;
    public static final int METERING_MODE_AVERAGE = 1;
    public static final int METERING_MODE_CENTER_WEIGHTED_AVERAGE = 2;
    public static final int METERING_MODE_SPOT = 3;
    public static final int METERING_MODE_MULTI_SPOT = 4;
    public static final int METERING_MODE_PATTERN = 5;
    public static final int METERING_MODE_PARTIAL = 6;
    public static final int METERING_MODE_MIN_RESERVED = 7;
    public static final int METERING_MODE_MAX_RESERVED = 254;
    public static final int METERING_MODE_OTHER = 255;
    public static final int TAG_LIGHT_SOURCE = 37384;
    public static final int LIGHT_SOURCE_UNKNOWN = 0;
    public static final int LIGHT_SOURCE_DAYLIGHT = 1;
    public static final int LIGHT_SOURCE_FLUORESCENT = 2;
    public static final int LIGHT_SOURCE_TUNGSTEN = 3;
    public static final int LIGHT_SOURCE_FLASH = 4;
    public static final int LIGHT_SOURCE_FINE_WEATHER = 9;
    public static final int LIGHT_SOURCE_CLOUDY_WEATHER = 10;
    public static final int LIGHT_SOURCE_SHADE = 11;
    public static final int LIGHT_SOURCE_DAYLIGHT_FLUORESCENT = 12;
    public static final int LIGHT_SOURCE_DAY_WHITE_FLUORESCENT = 13;
    public static final int LIGHT_SOURCE_COOL_WHITE_FLUORESCENT = 14;
    public static final int LIGHT_SOURCE_WHITE_FLUORESCENT = 15;
    public static final int LIGHT_SOURCE_STANDARD_LIGHT_A = 17;
    public static final int LIGHT_SOURCE_STANDARD_LIGHT_B = 18;
    public static final int LIGHT_SOURCE_STANDARD_LIGHT_C = 19;
    public static final int LIGHT_SOURCE_D55 = 20;
    public static final int LIGHT_SOURCE_D65 = 21;
    public static final int LIGHT_SOURCE_D75 = 22;
    public static final int LIGHT_SOURCE_D50 = 23;
    public static final int LIGHT_SOURCE_ISO_STUDIO_TUNGSTEN = 24;
    public static final int LIGHT_SOURCE_OTHER = 255;
    public static final int TAG_FLASH = 37385;
    public static final int FLASH_DID_NOT_FIRE = 0;
    public static final int FLASH_FIRED = 1;
    public static final int FLASH_STROBE_RETURN_LIGHT_NOT_DETECTED = 5;
    public static final int FLASH_STROBE_RETURN_LIGHT_DETECTED = 7;
    public static final int FLASH_MASK_FIRED = 1;
    public static final int FLASH_MASK_RETURN_NOT_DETECTED = 4;
    public static final int FLASH_MASK_RETURN_DETECTED = 6;
    public static final int FLASH_MASK_MODE_FLASH_FIRING = 8;
    public static final int FLASH_MASK_MODE_FLASH_SUPPRESSION = 16;
    public static final int FLASH_MASK_MODE_AUTO = 24;
    public static final int FLASH_MASK_FUNCTION_NOT_PRESENT = 32;
    public static final int FLASH_MASK_RED_EYE_REDUCTION = 64;
    public static final int TAG_FOCAL_LENGTH = 37386;
    public static final int TAG_SUBJECT_AREA = 37396;
    public static final int TAG_FLASH_ENERGY = 41483;
    public static final int TAG_SPATIAL_FREQUENCY_RESPONSE = 41484;
    public static final int TAG_FOCAL_PLANE_X_RESOLUTION = 41486;
    public static final int TAG_FOCAL_PLANE_Y_RESOLUTION = 41487;
    public static final int TAG_FOCAL_PLANE_RESOLUTION_UNIT = 41488;
    public static final int FOCAL_PLANE_RESOLUTION_UNIT_NONE = 1;
    public static final int FOCAL_PLANE_RESOLUTION_UNIT_INCH = 2;
    public static final int FOCAL_PLANE_RESOLUTION_UNIT_CENTIMETER = 3;
    public static final int TAG_SUBJECT_LOCATION = 41492;
    public static final int TAG_EXPOSURE_INDEX = 41493;
    public static final int TAG_SENSING_METHOD = 41495;
    public static final int SENSING_METHOD_NOT_DEFINED = 1;
    public static final int SENSING_METHOD_ONE_CHIP_COLOR_AREA_SENSOR = 2;
    public static final int SENSING_METHOD_TWO_CHIP_COLOR_AREA_SENSOR = 3;
    public static final int SENSING_METHOD_THREE_CHIP_COLOR_AREA_SENSOR = 4;
    public static final int SENSING_METHOD_COLOR_SEQUENTIAL_AREA_SENSOR = 5;
    public static final int SENSING_METHOD_TRILINEAR_SENSOR = 7;
    public static final int SENSING_METHOD_COLOR_SEQUENTIAL_LINEAR_SENSOR = 8;
    public static final int TAG_FILE_SOURCE = 41728;
    public static final int FILE_SOURCE_DSC = 3;
    public static final int TAG_SCENE_TYPE = 41729;
    public static final int SCENE_TYPE_DSC = 1;
    public static final int TAG_CFA_PATTERN = 41730;
    public static final int TAG_CUSTOM_RENDERED = 41985;
    public static final int CUSTOM_RENDERED_NORMAL = 0;
    public static final int CUSTOM_RENDERED_CUSTOM = 1;
    public static final int TAG_EXPOSURE_MODE = 41986;
    public static final int EXPOSURE_MODE_AUTO_EXPOSURE = 0;
    public static final int EXPOSURE_MODE_MANUAL_EXPOSURE = 1;
    public static final int EXPOSURE_MODE_AUTO_BRACKET = 2;
    public static final int TAG_WHITE_BALANCE = 41987;
    public static final int WHITE_BALANCE_AUTO = 0;
    public static final int WHITE_BALANCE_MANUAL = 1;
    public static final int TAG_DIGITAL_ZOOM_RATIO = 41988;
    public static final int TAG_FOCAL_LENGTH_IN_35MM_FILM = 41989;
    public static final int TAG_SCENE_CAPTURE_TYPE = 41990;
    public static final int SCENE_CAPTURE_TYPE_STANDARD = 0;
    public static final int SCENE_CAPTURE_TYPE_LANDSCAPE = 1;
    public static final int SCENE_CAPTURE_TYPE_PORTRAIT = 2;
    public static final int SCENE_CAPTURE_TYPE_NIGHT_SCENE = 3;
    public static final int TAG_GAIN_CONTROL = 41991;
    public static final int GAIN_CONTROL_NONE = 0;
    public static final int GAIN_CONTROL_LOW_GAIN_UP = 1;
    public static final int GAIN_CONTROL_HIGH_GAIN_UP = 2;
    public static final int GAIN_CONTROL_LOW_GAIN_DOWN = 3;
    public static final int GAIN_CONTROL_HIGH_GAIN_DOWN = 4;
    public static final int TAG_CONTRAST = 41992;
    public static final int CONTRAST_NORMAL = 0;
    public static final int CONTRAST_SOFT = 1;
    public static final int CONTRAST_HARD = 2;
    public static final int TAG_SATURATION = 41993;
    public static final int SATURATION_NORMAL = 0;
    public static final int SATURATION_LOW = 1;
    public static final int SATURATION_HIGH = 2;
    public static final int TAG_SHARPNESS = 41994;
    public static final int SHARPNESS_NORMAL = 0;
    public static final int SHARPNESS_SOFT = 1;
    public static final int SHARPNESS_HARD = 2;
    public static final int TAG_DEVICE_SETTING_DESCRIPTION = 41995;
    public static final int TAG_SUBJECT_DISTANCE_RANGE = 41996;
    public static final int SUBJECT_DISTANCE_RANGE_UNKNOWN = 0;
    public static final int SUBJECT_DISTANCE_RANGE_MACRO = 1;
    public static final int SUBJECT_DISTANCE_RANGE_CLOSE_VIEW = 2;
    public static final int SUBJECT_DISTANCE_RANGE_DISTANT_VIEW = 3;
    public static final int TAG_IMAGE_UNIQUE_ID = 42016;
    private static List tags;

    private static void initTags() {
        tags = new ArrayList(42);
        tags.add(new EXIFVersion());
        tags.add(new FlashPixVersion());
        tags.add(new ColorSpace());
        tags.add(new ComponentsConfiguration());
        tags.add(new CompressedBitsPerPixel());
        tags.add(new PixelXDimension());
        tags.add(new PixelYDimension());
        tags.add(new MakerNote());
        tags.add(new UserComment());
        tags.add(new RelatedSoundFile());
        tags.add(new DateTimeOriginal());
        tags.add(new DateTimeDigitized());
        tags.add(new SubSecTime());
        tags.add(new SubSecTimeOriginal());
        tags.add(new SubSecTimeDigitized());
        tags.add(new ExposureTime());
        tags.add(new FNumber());
        tags.add(new ExposureProgram());
        tags.add(new SpectralSensitivity());
        tags.add(new ISOSpeedRatings());
        tags.add(new OECF());
        tags.add(new ShutterSpeedValue());
        tags.add(new ApertureValue());
        tags.add(new BrightnessValue());
        tags.add(new ExposureBiasValue());
        tags.add(new MaxApertureValue());
        tags.add(new SubjectDistance());
        tags.add(new MeteringMode());
        tags.add(new LightSource());
        tags.add(new Flash());
        tags.add(new FocalLength());
        tags.add(new SubjectArea());
        tags.add(new FlashEnergy());
        tags.add(new SpatialFrequencyResponse());
        tags.add(new FocalPlaneXResolution());
        tags.add(new FocalPlaneYResolution());
        tags.add(new FocalPlaneResolutionUnit());
        tags.add(new SubjectLocation());
        tags.add(new ExposureIndex());
        tags.add(new SensingMethod());
        tags.add(new FileSource());
        tags.add(new SceneType());
        tags.add(new CFAPattern());
        tags.add(new CustomRendered());
        tags.add(new ExposureMode());
        tags.add(new WhiteBalance());
        tags.add(new DigitalZoomRatio());
        tags.add(new FocalLengthIn35mmFilm());
        tags.add(new SceneCaptureType());
        tags.add(new GainControl());
        tags.add(new Contrast());
        tags.add(new Saturation());
        tags.add(new Sharpness());
        tags.add(new DeviceSettingDescription());
        tags.add(new SubjectDistanceRange());
        tags.add(new ImageUniqueID());
        tags.add(new InteroperabilityIFD());
    }

    private EXIFTIFFTagSet() {
        super(tags);
    }

    public static synchronized EXIFTIFFTagSet getInstance() {
        if (theInstance == null) {
            EXIFTIFFTagSet.initTags();
            theInstance = new EXIFTIFFTagSet();
            tags = null;
        }
        return theInstance;
    }
}

