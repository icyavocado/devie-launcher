package com.evie.common.iconpack.shader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/* JADX INFO: loaded from: classes.dex */
public class IconPackShader {
    private static final String TAG = "IconPackShader";
    private ShaderRegister bufferRegister;
    private ShaderRegister iconRegister;
    private final Context mContext;
    private final String mPackageName;
    private List<ShaderInstruction> mShaderInstructions;
    private final int mVersionCode;
    private ShaderRegister outputRegister;
    private int[] pixels;
    private boolean usesBuffer;
    private boolean mLoaded = false;
    private boolean mDisabled = false;

    public IconPackShader(Context context, String str, int i) {
        this.mContext = context;
        this.mPackageName = str;
        this.mVersionCode = i;
        load();
    }

    private static ShaderInstruction parseInstruction(String str, String str2, String str3) {
        int i;
        int i2;
        int i3;
        boolean z;
        float f;
        int i4;
        int i5;
        int i6;
        if (str == null || str.length() != 2 || str2 == null || str2.length() != 1 || str3 == null || str3.isEmpty()) {
            return null;
        }
        char upperCase = Character.toUpperCase(str2.charAt(0));
        if (upperCase == 'A') {
            i = 3;
        } else if (upperCase == 'D') {
            i = 2;
        } else if (upperCase == 'M') {
            i = 1;
        } else if (upperCase == 'S') {
            i = 4;
        } else {
            if (upperCase != 'W') {
                return null;
            }
            i = 0;
        }
        char upperCase2 = Character.toUpperCase(str.charAt(0));
        char upperCase3 = Character.toUpperCase(str.charAt(1));
        if (upperCase2 == 'B') {
            i2 = 1;
        } else {
            if (upperCase2 != 'O') {
                return null;
            }
            i2 = 2;
        }
        if (upperCase3 == 'G') {
            i3 = 2;
        } else if (upperCase3 != 'R') {
            switch (upperCase3) {
                case 'A':
                    i3 = 0;
                    break;
                case 'B':
                    i3 = 3;
                    break;
                default:
                    return null;
            }
        } else {
            i3 = 1;
        }
        try {
            f = Float.parseFloat(str3);
            z = true;
        } catch (NumberFormatException unused) {
            z = false;
            f = 0.0f;
        }
        if (z) {
            i4 = -1;
            i5 = -1;
        } else {
            if (str3.length() != 2) {
                return null;
            }
            char upperCase4 = Character.toUpperCase(str3.charAt(0));
            char upperCase5 = Character.toUpperCase(str3.charAt(1));
            if (upperCase4 == 'B') {
                i6 = 1;
            } else if (upperCase4 == 'I') {
                i6 = 0;
            } else {
                if (upperCase4 != 'O') {
                    return null;
                }
                i6 = 2;
            }
            if (upperCase5 != 'R') {
                switch (upperCase5) {
                    case 'A':
                        i4 = i6;
                        i5 = 0;
                        break;
                    case 'B':
                        i4 = i6;
                        i5 = 3;
                        break;
                    default:
                        switch (upperCase5) {
                            case 'G':
                                i4 = i6;
                                i5 = 2;
                                break;
                            case 'H':
                                i4 = i6;
                                i5 = 5;
                                break;
                            case 'I':
                                i4 = i6;
                                i5 = 4;
                                break;
                            default:
                                return null;
                        }
                        break;
                }
            } else {
                i4 = i6;
                i5 = 1;
            }
        }
        return new ShaderInstruction(i2, i3, i, i4, i5, z, f);
    }

    public void load() {
        /* REWORK: jadx lost an exception-flow local across catch scopes. */
        Throwable eReflow = null;
        XmlPullParser xmlPullParserNewPullParser;
        ShaderInstruction instruction;
        if (this.mDisabled || this.mLoaded) {
            return;
        }
        try {
            try {
                Resources resourcesForApplication = this.mContext.getPackageManager().getResourcesForApplication(this.mPackageName);
                int identifier = resourcesForApplication.getIdentifier("shader", "xml", this.mPackageName);
                if (identifier > 0) {
                    xmlPullParserNewPullParser = resourcesForApplication.getXml(identifier);
                } else {
                    try {
                        InputStream inputStreamOpen = resourcesForApplication.getAssets().open("shader.xml");
                        XmlPullParserFactory xmlPullParserFactoryNewInstance = XmlPullParserFactory.newInstance();
                        xmlPullParserFactoryNewInstance.setNamespaceAware(true);
                        xmlPullParserNewPullParser = xmlPullParserFactoryNewInstance.newPullParser();
                        try {
                            xmlPullParserNewPullParser.setInput(inputStreamOpen, "utf-8");
                        } catch (Exception e) {
                            /* REWORK: setInput throws XmlPullParserException, not IOException */
                            e = e;
                            Log.d(TAG, "No shader.xml file", e);
                        }
                    } catch (IOException e2) {
                        eReflow = e2;
                        xmlPullParserNewPullParser = null;
                    }
                }
                ArrayList arrayList = new ArrayList();
                if (xmlPullParserNewPullParser != null) {
                    for (int eventType = xmlPullParserNewPullParser.getEventType(); eventType != 1; eventType = xmlPullParserNewPullParser.next()) {
                        if (eventType == 2 && xmlPullParserNewPullParser.getName().compareTo("exec") == 0 && xmlPullParserNewPullParser.getAttributeCount() == 3 && (instruction = parseInstruction(xmlPullParserNewPullParser.getAttributeValue(null, "t"), xmlPullParserNewPullParser.getAttributeValue(null, "m"), xmlPullParserNewPullParser.getAttributeValue(null, "v"))) != null && instruction.verify()) {
                            arrayList.add(instruction);
                            if (instruction.inputRegister == 1 || instruction.targetRegister == 1) {
                                this.usesBuffer = true;
                            }
                        }
                    }
                }
                this.mShaderInstructions = arrayList;
                this.mLoaded = true;
            } catch (IOException e3) {
                Log.e(TAG, "IOException during loading of shader.xml", e3);
                this.mDisabled = true;
            }
        } catch (PackageManager.NameNotFoundException e4) {
            Log.d(TAG, "Cannot load shader", e4);
            this.mDisabled = true;
        } catch (XmlPullParserException e5) {
            Log.d(TAG, "Cannot parse shader.xml", e5);
            this.mDisabled = true;
        }
    }

    public Bitmap processBitmap(Bitmap bitmap) {
        List<ShaderInstruction> list;
        ShaderRegister shaderRegister;
        ShaderRegister shaderRegister2;
        int i;
        if (bitmap == null) {
            return null;
        }
        if (this.mDisabled || (list = this.mShaderInstructions) == null || list.isEmpty()) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int i2 = width * height;
        int[] iArr = this.pixels;
        if (iArr == null || iArr.length < i2) {
            this.pixels = new int[i2];
        }
        if (this.iconRegister == null) {
            this.iconRegister = new ShaderRegister();
        }
        this.iconRegister.setSize(i2);
        if (this.outputRegister == null) {
            this.outputRegister = new ShaderRegister();
        }
        this.outputRegister.setSize(i2);
        if (this.usesBuffer) {
            if (this.bufferRegister == null) {
                this.bufferRegister = new ShaderRegister();
            }
            this.bufferRegister.setSize(i2);
        }
        bitmap.getPixels(this.pixels, 0, width, 0, 0, width, height);
        this.iconRegister.setPixels(this.pixels, i2);
        Arrays.fill(this.pixels, 0);
        this.outputRegister.writeValue(0, 0.0f, i2);
        this.outputRegister.writeValue(1, 0.0f, i2);
        this.outputRegister.writeValue(3, 0.0f, i2);
        this.outputRegister.writeValue(2, 0.0f, i2);
        ShaderRegister shaderRegister3 = this.bufferRegister;
        if (shaderRegister3 != null) {
            shaderRegister3.writeValue(0, 0.0f, i2);
            this.bufferRegister.writeValue(1, 0.0f, i2);
            this.bufferRegister.writeValue(3, 0.0f, i2);
            this.bufferRegister.writeValue(2, 0.0f, i2);
        }
        for (ShaderInstruction shaderInstruction : this.mShaderInstructions) {
            boolean z = shaderInstruction.hasValue;
            float brightness = shaderInstruction.value;
            switch (shaderInstruction.targetRegister) {
                case 1:
                    shaderRegister = this.bufferRegister;
                    break;
                case 2:
                    shaderRegister = this.outputRegister;
                    break;
                default:
                    throw new IllegalArgumentException("illegal target register: " + shaderInstruction.targetRegister);
            }
            int i3 = shaderInstruction.targetChannel;
            if (!shaderInstruction.hasValue) {
                switch (shaderInstruction.inputRegister) {
                    case 0:
                        shaderRegister2 = this.iconRegister;
                        break;
                    case 1:
                        shaderRegister2 = this.bufferRegister;
                        break;
                    case 2:
                        shaderRegister2 = this.outputRegister;
                        break;
                    default:
                        throw new IllegalArgumentException("illegal target register: " + shaderInstruction.targetRegister);
                }
                switch (shaderInstruction.inputChannel) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        i = shaderInstruction.inputChannel;
                        break;
                    case 5:
                        brightness = shaderRegister2.getBrightness();
                        z = true;
                        i = -1;
                        break;
                    default:
                        i = -1;
                        break;
                }
            } else {
                shaderRegister2 = null;
                i = -1;
            }
            switch (shaderInstruction.mode) {
                case 0:
                    if (z) {
                        shaderRegister.writeValue(i3, brightness, i2);
                    } else {
                        shaderRegister.writeChannel(i3, shaderRegister2.getChannel(i), i2);
                    }
                    break;
                case 1:
                    if (z) {
                        shaderRegister.multiplyValue(i3, brightness, i2);
                    } else {
                        shaderRegister.multiplyChannel(i3, shaderRegister2.getChannel(i), i2);
                    }
                    break;
                case 2:
                    if (z) {
                        shaderRegister.divideValue(i3, brightness, i2);
                    } else {
                        shaderRegister.divideChannel(i3, shaderRegister2.getChannel(i), i2);
                    }
                    break;
                case 3:
                    if (z) {
                        shaderRegister.addValue(i3, brightness, i2);
                    } else {
                        shaderRegister.addChannel(i3, shaderRegister2.getChannel(i), i2);
                    }
                    break;
                case 4:
                    if (z) {
                        shaderRegister.subtractValue(i3, brightness, i2);
                    } else {
                        shaderRegister.subtractChannel(i3, shaderRegister2.getChannel(i), i2);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid mode: " + shaderInstruction.mode);
            }
        }
        float[] channel = this.outputRegister.getChannel(0);
        float[] channel2 = this.outputRegister.getChannel(1);
        float[] channel3 = this.outputRegister.getChannel(2);
        float[] channel4 = this.outputRegister.getChannel(3);
        for (int i4 = 0; i4 < i2; i4++) {
            int iRound = Math.round(channel[i4]);
            int iRound2 = Math.round(channel2[i4]);
            int iRound3 = Math.round(channel3[i4]);
            int iRound4 = Math.round(channel4[i4]);
            this.pixels[i4] = (((((Math.max(0, Math.min(iRound, 255)) << 8) | Math.max(0, Math.min(iRound2, 255))) << 8) | Math.max(0, Math.min(iRound3, 255))) << 8) | Math.max(0, Math.min(iRound4, 255));
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(this.pixels, width, height, bitmap.getConfig() == null ? Bitmap.Config.ARGB_8888 : bitmap.getConfig());
        bitmapCreateBitmap.setDensity(0);
        return bitmapCreateBitmap;
    }

    public void releaseRegisters() {
        this.iconRegister = null;
        this.bufferRegister = null;
        this.outputRegister = null;
        this.pixels = null;
    }
}
