package com.android.gallery3d.exif;

import android.util.SparseIntArray;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

/* JADX INFO: loaded from: classes.dex */
public class ExifInterface {
    public static final ByteOrder DEFAULT_BYTE_ORDER;
    protected static HashSet<Short> sBannedDefines;
    private ExifData mData = new ExifData(DEFAULT_BYTE_ORDER);
    private final DateFormat mDateTimeStampFormat = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
    private final DateFormat mGPSDateStampFormat = new SimpleDateFormat("yyyy:MM:dd");
    private final Calendar mGPSTimeStampCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private SparseIntArray mTagInfo = null;
    public static final int TAG_IMAGE_WIDTH = defineTag(0, 256);
    public static final int TAG_IMAGE_LENGTH = defineTag(0, 257);
    public static final int TAG_BITS_PER_SAMPLE = defineTag(0, 258);
    public static final int TAG_COMPRESSION = defineTag(0, 259);
    public static final int TAG_PHOTOMETRIC_INTERPRETATION = defineTag(0, 262);
    public static final int TAG_IMAGE_DESCRIPTION = defineTag(0, 270);
    public static final int TAG_MAKE = defineTag(0, 271);
    public static final int TAG_MODEL = defineTag(0, 272);
    public static final int TAG_STRIP_OFFSETS = defineTag(0, 273);
    public static final int TAG_ORIENTATION = defineTag(0, 274);
    public static final int TAG_SAMPLES_PER_PIXEL = defineTag(0, 277);
    public static final int TAG_ROWS_PER_STRIP = defineTag(0, 278);
    public static final int TAG_STRIP_BYTE_COUNTS = defineTag(0, 279);
    public static final int TAG_X_RESOLUTION = defineTag(0, 282);
    public static final int TAG_Y_RESOLUTION = defineTag(0, 283);
    public static final int TAG_PLANAR_CONFIGURATION = defineTag(0, 284);
    public static final int TAG_RESOLUTION_UNIT = defineTag(0, 296);
    public static final int TAG_TRANSFER_FUNCTION = defineTag(0, 301);
    public static final int TAG_SOFTWARE = defineTag(0, 305);
    public static final int TAG_DATE_TIME = defineTag(0, 306);
    public static final int TAG_ARTIST = defineTag(0, 315);
    public static final int TAG_WHITE_POINT = defineTag(0, 318);
    public static final int TAG_PRIMARY_CHROMATICITIES = defineTag(0, 319);
    public static final int TAG_Y_CB_CR_COEFFICIENTS = defineTag(0, 529);
    public static final int TAG_Y_CB_CR_SUB_SAMPLING = defineTag(0, 530);
    public static final int TAG_Y_CB_CR_POSITIONING = defineTag(0, 531);
    public static final int TAG_REFERENCE_BLACK_WHITE = defineTag(0, 532);
    public static final int TAG_COPYRIGHT = defineTag(0, -32104);
    public static final int TAG_EXIF_IFD = defineTag(0, -30871);
    public static final int TAG_GPS_IFD = defineTag(0, -30683);
    public static final int TAG_JPEG_INTERCHANGE_FORMAT = defineTag(1, 513);
    public static final int TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = defineTag(1, 514);
    public static final int TAG_EXPOSURE_TIME = defineTag(2, -32102);
    public static final int TAG_F_NUMBER = defineTag(2, -32099);
    public static final int TAG_EXPOSURE_PROGRAM = defineTag(2, -30686);
    public static final int TAG_SPECTRAL_SENSITIVITY = defineTag(2, -30684);
    public static final int TAG_ISO_SPEED_RATINGS = defineTag(2, -30681);
    public static final int TAG_OECF = defineTag(2, -30680);
    public static final int TAG_EXIF_VERSION = defineTag(2, -28672);
    public static final int TAG_DATE_TIME_ORIGINAL = defineTag(2, -28669);
    public static final int TAG_DATE_TIME_DIGITIZED = defineTag(2, -28668);
    public static final int TAG_COMPONENTS_CONFIGURATION = defineTag(2, -28415);
    public static final int TAG_COMPRESSED_BITS_PER_PIXEL = defineTag(2, -28414);
    public static final int TAG_SHUTTER_SPEED_VALUE = defineTag(2, -28159);
    public static final int TAG_APERTURE_VALUE = defineTag(2, -28158);
    public static final int TAG_BRIGHTNESS_VALUE = defineTag(2, -28157);
    public static final int TAG_EXPOSURE_BIAS_VALUE = defineTag(2, -28156);
    public static final int TAG_MAX_APERTURE_VALUE = defineTag(2, -28155);
    public static final int TAG_SUBJECT_DISTANCE = defineTag(2, -28154);
    public static final int TAG_METERING_MODE = defineTag(2, -28153);
    public static final int TAG_LIGHT_SOURCE = defineTag(2, -28152);
    public static final int TAG_FLASH = defineTag(2, -28151);
    public static final int TAG_FOCAL_LENGTH = defineTag(2, -28150);
    public static final int TAG_SUBJECT_AREA = defineTag(2, -28140);
    public static final int TAG_MAKER_NOTE = defineTag(2, -28036);
    public static final int TAG_USER_COMMENT = defineTag(2, -28026);
    public static final int TAG_SUB_SEC_TIME = defineTag(2, -28016);
    public static final int TAG_SUB_SEC_TIME_ORIGINAL = defineTag(2, -28015);
    public static final int TAG_SUB_SEC_TIME_DIGITIZED = defineTag(2, -28014);
    public static final int TAG_FLASHPIX_VERSION = defineTag(2, -24576);
    public static final int TAG_COLOR_SPACE = defineTag(2, -24575);
    public static final int TAG_PIXEL_X_DIMENSION = defineTag(2, -24574);
    public static final int TAG_PIXEL_Y_DIMENSION = defineTag(2, -24573);
    public static final int TAG_RELATED_SOUND_FILE = defineTag(2, -24572);
    public static final int TAG_INTEROPERABILITY_IFD = defineTag(2, -24571);
    public static final int TAG_FLASH_ENERGY = defineTag(2, -24053);
    public static final int TAG_SPATIAL_FREQUENCY_RESPONSE = defineTag(2, -24052);
    public static final int TAG_FOCAL_PLANE_X_RESOLUTION = defineTag(2, -24050);
    public static final int TAG_FOCAL_PLANE_Y_RESOLUTION = defineTag(2, -24049);
    public static final int TAG_FOCAL_PLANE_RESOLUTION_UNIT = defineTag(2, -24048);
    public static final int TAG_SUBJECT_LOCATION = defineTag(2, -24044);
    public static final int TAG_EXPOSURE_INDEX = defineTag(2, -24043);
    public static final int TAG_SENSING_METHOD = defineTag(2, -24041);
    public static final int TAG_FILE_SOURCE = defineTag(2, -23808);
    public static final int TAG_SCENE_TYPE = defineTag(2, -23807);
    public static final int TAG_CFA_PATTERN = defineTag(2, -23806);
    public static final int TAG_CUSTOM_RENDERED = defineTag(2, -23551);
    public static final int TAG_EXPOSURE_MODE = defineTag(2, -23550);
    public static final int TAG_WHITE_BALANCE = defineTag(2, -23549);
    public static final int TAG_DIGITAL_ZOOM_RATIO = defineTag(2, -23548);
    public static final int TAG_FOCAL_LENGTH_IN_35_MM_FILE = defineTag(2, -23547);
    public static final int TAG_SCENE_CAPTURE_TYPE = defineTag(2, -23546);
    public static final int TAG_GAIN_CONTROL = defineTag(2, -23545);
    public static final int TAG_CONTRAST = defineTag(2, -23544);
    public static final int TAG_SATURATION = defineTag(2, -23543);
    public static final int TAG_SHARPNESS = defineTag(2, -23542);
    public static final int TAG_DEVICE_SETTING_DESCRIPTION = defineTag(2, -23541);
    public static final int TAG_SUBJECT_DISTANCE_RANGE = defineTag(2, -23540);
    public static final int TAG_IMAGE_UNIQUE_ID = defineTag(2, -23520);
    public static final int TAG_GPS_VERSION_ID = defineTag(4, 0);
    public static final int TAG_GPS_LATITUDE_REF = defineTag(4, 1);
    public static final int TAG_GPS_LATITUDE = defineTag(4, 2);
    public static final int TAG_GPS_LONGITUDE_REF = defineTag(4, 3);
    public static final int TAG_GPS_LONGITUDE = defineTag(4, 4);
    public static final int TAG_GPS_ALTITUDE_REF = defineTag(4, 5);
    public static final int TAG_GPS_ALTITUDE = defineTag(4, 6);
    public static final int TAG_GPS_TIME_STAMP = defineTag(4, 7);
    public static final int TAG_GPS_SATTELLITES = defineTag(4, 8);
    public static final int TAG_GPS_STATUS = defineTag(4, 9);
    public static final int TAG_GPS_MEASURE_MODE = defineTag(4, 10);
    public static final int TAG_GPS_DOP = defineTag(4, 11);
    public static final int TAG_GPS_SPEED_REF = defineTag(4, 12);
    public static final int TAG_GPS_SPEED = defineTag(4, 13);
    public static final int TAG_GPS_TRACK_REF = defineTag(4, 14);
    public static final int TAG_GPS_TRACK = defineTag(4, 15);
    public static final int TAG_GPS_IMG_DIRECTION_REF = defineTag(4, 16);
    public static final int TAG_GPS_IMG_DIRECTION = defineTag(4, 17);
    public static final int TAG_GPS_MAP_DATUM = defineTag(4, 18);
    public static final int TAG_GPS_DEST_LATITUDE_REF = defineTag(4, 19);
    public static final int TAG_GPS_DEST_LATITUDE = defineTag(4, 20);
    public static final int TAG_GPS_DEST_LONGITUDE_REF = defineTag(4, 21);
    public static final int TAG_GPS_DEST_LONGITUDE = defineTag(4, 22);
    public static final int TAG_GPS_DEST_BEARING_REF = defineTag(4, 23);
    public static final int TAG_GPS_DEST_BEARING = defineTag(4, 24);
    public static final int TAG_GPS_DEST_DISTANCE_REF = defineTag(4, 25);
    public static final int TAG_GPS_DEST_DISTANCE = defineTag(4, 26);
    public static final int TAG_GPS_PROCESSING_METHOD = defineTag(4, 27);
    public static final int TAG_GPS_AREA_INFORMATION = defineTag(4, 28);
    public static final int TAG_GPS_DATE_STAMP = defineTag(4, 29);
    public static final int TAG_GPS_DIFFERENTIAL = defineTag(4, 30);
    public static final int TAG_INTEROPERABILITY_INDEX = defineTag(3, 1);
    private static HashSet<Short> sOffsetTags = new HashSet<>();

    static {
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_GPS_IFD)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_EXIF_IFD)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_INTEROPERABILITY_IFD)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_STRIP_OFFSETS)));
        sBannedDefines = new HashSet<>(sOffsetTags);
        sBannedDefines.add(Short.valueOf(getTrueTagKey(-1)));
        sBannedDefines.add(Short.valueOf(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)));
        sBannedDefines.add(Short.valueOf(getTrueTagKey(TAG_STRIP_BYTE_COUNTS)));
        DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    }

    public ExifInterface() {
        this.mGPSDateStampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static int defineTag(int i, int s) {
        return (i << 16) | (s & 65535);
    }

    protected static int getAllowedIfdFlagsFromInfo(int i) {
        return i >>> 24;
    }

    protected static int getFlagsFromAllowedIfds(int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            return 0;
        }
        int[] ifds = IfdData.getIfds();
        int i = 0;
        for (int i2 = 0; i2 < 5; i2++) {
            for (int i3 : iArr) {
                if (ifds[i2] == i3) {
                    i |= 1 << i2;
                    break;
                }
            }
        }
        return i;
    }

    public static int getRotationForOrientationValue(short s) {
        if (s == 1) {
            return 0;
        }
        if (s == 3) {
            return 180;
        }
        if (s != 6) {
            return s != 8 ? 0 : 270;
        }
        return 90;
    }

    public static int getTrueIfd(int i) {
        return i >>> 16;
    }

    public static short getTrueTagKey(int i) {
        return (short) i;
    }

    private void initTagInfo() {
        int flagsFromAllowedIfds = getFlagsFromAllowedIfds(new int[]{0, 1}) << 24;
        int i = flagsFromAllowedIfds | 131072;
        int i2 = i | 0;
        this.mTagInfo.put(TAG_MAKE, i2);
        int i3 = flagsFromAllowedIfds | 262144;
        int i4 = i3 | 1;
        this.mTagInfo.put(TAG_IMAGE_WIDTH, i4);
        this.mTagInfo.put(TAG_IMAGE_LENGTH, i4);
        int i5 = flagsFromAllowedIfds | 196608;
        this.mTagInfo.put(TAG_BITS_PER_SAMPLE, i5 | 3);
        int i6 = i5 | 1;
        this.mTagInfo.put(TAG_COMPRESSION, i6);
        this.mTagInfo.put(TAG_PHOTOMETRIC_INTERPRETATION, i6);
        this.mTagInfo.put(TAG_ORIENTATION, i6);
        this.mTagInfo.put(TAG_SAMPLES_PER_PIXEL, i6);
        this.mTagInfo.put(TAG_PLANAR_CONFIGURATION, i6);
        this.mTagInfo.put(TAG_Y_CB_CR_SUB_SAMPLING, i5 | 2);
        this.mTagInfo.put(TAG_Y_CB_CR_POSITIONING, i6);
        int i7 = flagsFromAllowedIfds | 327680;
        int i8 = i7 | 1;
        this.mTagInfo.put(TAG_X_RESOLUTION, i8);
        this.mTagInfo.put(TAG_Y_RESOLUTION, i8);
        this.mTagInfo.put(TAG_RESOLUTION_UNIT, i6);
        int i9 = i3 | 0;
        this.mTagInfo.put(TAG_STRIP_OFFSETS, i9);
        this.mTagInfo.put(TAG_ROWS_PER_STRIP, i4);
        this.mTagInfo.put(TAG_STRIP_BYTE_COUNTS, i9);
        this.mTagInfo.put(TAG_TRANSFER_FUNCTION, i5 | 768);
        this.mTagInfo.put(TAG_WHITE_POINT, i7 | 2);
        int i10 = i7 | 6;
        this.mTagInfo.put(TAG_PRIMARY_CHROMATICITIES, i10);
        this.mTagInfo.put(TAG_Y_CB_CR_COEFFICIENTS, i7 | 3);
        this.mTagInfo.put(TAG_REFERENCE_BLACK_WHITE, i10);
        this.mTagInfo.put(TAG_DATE_TIME, i | 20);
        this.mTagInfo.put(TAG_IMAGE_DESCRIPTION, i2);
        this.mTagInfo.put(TAG_MAKE, i2);
        this.mTagInfo.put(TAG_MODEL, i2);
        this.mTagInfo.put(TAG_SOFTWARE, i2);
        this.mTagInfo.put(TAG_ARTIST, i2);
        this.mTagInfo.put(TAG_COPYRIGHT, i2);
        this.mTagInfo.put(TAG_EXIF_IFD, i4);
        this.mTagInfo.put(TAG_GPS_IFD, i4);
        int flagsFromAllowedIfds2 = (getFlagsFromAllowedIfds(new int[]{1}) << 24) | 262144 | 1;
        this.mTagInfo.put(TAG_JPEG_INTERCHANGE_FORMAT, flagsFromAllowedIfds2);
        this.mTagInfo.put(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, flagsFromAllowedIfds2);
        int flagsFromAllowedIfds3 = getFlagsFromAllowedIfds(new int[]{2}) << 24;
        int i11 = flagsFromAllowedIfds3 | 458752;
        int i12 = i11 | 4;
        this.mTagInfo.put(TAG_EXIF_VERSION, i12);
        this.mTagInfo.put(TAG_FLASHPIX_VERSION, i12);
        int i13 = flagsFromAllowedIfds3 | 196608;
        int i14 = i13 | 1;
        this.mTagInfo.put(TAG_COLOR_SPACE, i14);
        this.mTagInfo.put(TAG_COMPONENTS_CONFIGURATION, i12);
        int i15 = flagsFromAllowedIfds3 | 327680 | 1;
        this.mTagInfo.put(TAG_COMPRESSED_BITS_PER_PIXEL, i15);
        int i16 = 262144 | flagsFromAllowedIfds3 | 1;
        this.mTagInfo.put(TAG_PIXEL_X_DIMENSION, i16);
        this.mTagInfo.put(TAG_PIXEL_Y_DIMENSION, i16);
        int i17 = i11 | 0;
        this.mTagInfo.put(TAG_MAKER_NOTE, i17);
        this.mTagInfo.put(TAG_USER_COMMENT, i17);
        int i18 = flagsFromAllowedIfds3 | 131072;
        this.mTagInfo.put(TAG_RELATED_SOUND_FILE, i18 | 13);
        int i19 = i18 | 20;
        this.mTagInfo.put(TAG_DATE_TIME_ORIGINAL, i19);
        this.mTagInfo.put(TAG_DATE_TIME_DIGITIZED, i19);
        int i20 = i18 | 0;
        this.mTagInfo.put(TAG_SUB_SEC_TIME, i20);
        this.mTagInfo.put(TAG_SUB_SEC_TIME_ORIGINAL, i20);
        this.mTagInfo.put(TAG_SUB_SEC_TIME_DIGITIZED, i20);
        this.mTagInfo.put(TAG_IMAGE_UNIQUE_ID, i18 | 33);
        this.mTagInfo.put(TAG_EXPOSURE_TIME, i15);
        this.mTagInfo.put(TAG_F_NUMBER, i15);
        this.mTagInfo.put(TAG_EXPOSURE_PROGRAM, i14);
        this.mTagInfo.put(TAG_SPECTRAL_SENSITIVITY, i20);
        int i21 = i13 | 0;
        this.mTagInfo.put(TAG_ISO_SPEED_RATINGS, i21);
        this.mTagInfo.put(TAG_OECF, i17);
        int i22 = flagsFromAllowedIfds3 | 655360 | 1;
        this.mTagInfo.put(TAG_SHUTTER_SPEED_VALUE, i22);
        this.mTagInfo.put(TAG_APERTURE_VALUE, i15);
        this.mTagInfo.put(TAG_BRIGHTNESS_VALUE, i22);
        this.mTagInfo.put(TAG_EXPOSURE_BIAS_VALUE, i22);
        this.mTagInfo.put(TAG_MAX_APERTURE_VALUE, i15);
        this.mTagInfo.put(TAG_SUBJECT_DISTANCE, i15);
        this.mTagInfo.put(TAG_METERING_MODE, i14);
        this.mTagInfo.put(TAG_LIGHT_SOURCE, i14);
        this.mTagInfo.put(TAG_FLASH, i14);
        this.mTagInfo.put(TAG_FOCAL_LENGTH, i15);
        this.mTagInfo.put(TAG_SUBJECT_AREA, i21);
        this.mTagInfo.put(TAG_FLASH_ENERGY, i15);
        this.mTagInfo.put(TAG_SPATIAL_FREQUENCY_RESPONSE, i17);
        this.mTagInfo.put(TAG_FOCAL_PLANE_X_RESOLUTION, i15);
        this.mTagInfo.put(TAG_FOCAL_PLANE_Y_RESOLUTION, i15);
        this.mTagInfo.put(TAG_FOCAL_PLANE_RESOLUTION_UNIT, i14);
        this.mTagInfo.put(TAG_SUBJECT_LOCATION, 2 | i13);
        this.mTagInfo.put(TAG_EXPOSURE_INDEX, i15);
        this.mTagInfo.put(TAG_SENSING_METHOD, i14);
        int i23 = i11 | 1;
        this.mTagInfo.put(TAG_FILE_SOURCE, i23);
        this.mTagInfo.put(TAG_SCENE_TYPE, i23);
        this.mTagInfo.put(TAG_CFA_PATTERN, i17);
        this.mTagInfo.put(TAG_CUSTOM_RENDERED, i14);
        this.mTagInfo.put(TAG_EXPOSURE_MODE, i14);
        this.mTagInfo.put(TAG_WHITE_BALANCE, i14);
        this.mTagInfo.put(TAG_DIGITAL_ZOOM_RATIO, i15);
        this.mTagInfo.put(TAG_FOCAL_LENGTH_IN_35_MM_FILE, i14);
        this.mTagInfo.put(TAG_SCENE_CAPTURE_TYPE, i14);
        this.mTagInfo.put(TAG_GAIN_CONTROL, i15);
        this.mTagInfo.put(TAG_CONTRAST, i14);
        this.mTagInfo.put(TAG_SATURATION, i14);
        this.mTagInfo.put(TAG_SHARPNESS, i14);
        this.mTagInfo.put(TAG_DEVICE_SETTING_DESCRIPTION, i17);
        this.mTagInfo.put(TAG_SUBJECT_DISTANCE_RANGE, i14);
        this.mTagInfo.put(TAG_INTEROPERABILITY_IFD, i16);
        int flagsFromAllowedIfds4 = getFlagsFromAllowedIfds(new int[]{4}) << 24;
        int i24 = 65536 | flagsFromAllowedIfds4;
        this.mTagInfo.put(TAG_GPS_VERSION_ID, i24 | 4);
        int i25 = flagsFromAllowedIfds4 | 131072;
        int i26 = i25 | 2;
        this.mTagInfo.put(TAG_GPS_LATITUDE_REF, i26);
        this.mTagInfo.put(TAG_GPS_LONGITUDE_REF, i26);
        int i27 = flagsFromAllowedIfds4 | 655360 | 3;
        this.mTagInfo.put(TAG_GPS_LATITUDE, i27);
        this.mTagInfo.put(TAG_GPS_LONGITUDE, i27);
        this.mTagInfo.put(TAG_GPS_ALTITUDE_REF, i24 | 1);
        int i28 = 327680 | flagsFromAllowedIfds4;
        int i29 = i28 | 1;
        this.mTagInfo.put(TAG_GPS_ALTITUDE, i29);
        this.mTagInfo.put(TAG_GPS_TIME_STAMP, i28 | 3);
        int i30 = i25 | 0;
        this.mTagInfo.put(TAG_GPS_SATTELLITES, i30);
        this.mTagInfo.put(TAG_GPS_STATUS, i26);
        this.mTagInfo.put(TAG_GPS_MEASURE_MODE, i26);
        this.mTagInfo.put(TAG_GPS_DOP, i29);
        this.mTagInfo.put(TAG_GPS_SPEED_REF, i26);
        this.mTagInfo.put(TAG_GPS_SPEED, i29);
        this.mTagInfo.put(TAG_GPS_TRACK_REF, i26);
        this.mTagInfo.put(TAG_GPS_TRACK, i29);
        this.mTagInfo.put(TAG_GPS_IMG_DIRECTION_REF, i26);
        this.mTagInfo.put(TAG_GPS_IMG_DIRECTION, i29);
        this.mTagInfo.put(TAG_GPS_MAP_DATUM, i30);
        this.mTagInfo.put(TAG_GPS_DEST_LATITUDE_REF, i26);
        this.mTagInfo.put(TAG_GPS_DEST_LATITUDE, i29);
        this.mTagInfo.put(TAG_GPS_DEST_BEARING_REF, i26);
        this.mTagInfo.put(TAG_GPS_DEST_BEARING, i29);
        this.mTagInfo.put(TAG_GPS_DEST_DISTANCE_REF, i26);
        this.mTagInfo.put(TAG_GPS_DEST_DISTANCE, i29);
        int i31 = 458752 | flagsFromAllowedIfds4 | 0;
        this.mTagInfo.put(TAG_GPS_PROCESSING_METHOD, i31);
        this.mTagInfo.put(TAG_GPS_AREA_INFORMATION, i31);
        this.mTagInfo.put(TAG_GPS_DATE_STAMP, i25 | 11);
        this.mTagInfo.put(TAG_GPS_DIFFERENTIAL, flagsFromAllowedIfds4 | 196608 | 11);
        this.mTagInfo.put(TAG_INTEROPERABILITY_INDEX, (getFlagsFromAllowedIfds(new int[]{3}) << 24) | 131072 | 0);
    }

    protected static boolean isIfdAllowed(int i, int i2) {
        int[] ifds = IfdData.getIfds();
        int allowedIfdFlagsFromInfo = getAllowedIfdFlagsFromInfo(i);
        for (int i3 = 0; i3 < ifds.length; i3++) {
            if (i2 == ifds[i3] && ((allowedIfdFlagsFromInfo >> i3) & 1) == 1) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isOffsetTag(short s) {
        return sOffsetTags.contains(Short.valueOf(s));
    }

    public int getDefinedTagDefaultIfd(int i) {
        if (getTagInfo().get(i) == 0) {
            return -1;
        }
        return getTrueIfd(i);
    }

    public ExifTag getTag(int i, int i2) {
        if (ExifTag.isValidIfd(i2)) {
            return this.mData.getTag(getTrueTagKey(i), i2);
        }
        return null;
    }

    protected SparseIntArray getTagInfo() {
        if (this.mTagInfo == null) {
            this.mTagInfo = new SparseIntArray();
            initTagInfo();
        }
        return this.mTagInfo;
    }

    public Integer getTagIntValue(int i) {
        return getTagIntValue(i, getDefinedTagDefaultIfd(i));
    }

    public Integer getTagIntValue(int i, int i2) {
        int[] tagIntValues = getTagIntValues(i, i2);
        if (tagIntValues == null || tagIntValues.length <= 0) {
            return null;
        }
        return Integer.valueOf(tagIntValues[0]);
    }

    public int[] getTagIntValues(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        if (tag == null) {
            return null;
        }
        return tag.getValueAsInts();
    }

    public void readExif(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        try {
            this.mData = new ExifReader(this).read(inputStream);
        } catch (ExifInvalidFormatException e) {
            throw new IOException("Invalid exif format : " + e);
        }
    }
}
