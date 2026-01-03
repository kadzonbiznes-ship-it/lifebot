/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;

public abstract class SampleModel {
    protected int width;
    protected int height;
    protected int numBands;
    protected int dataType;

    private static native void initIDs();

    public SampleModel(int dataType, int w, int h, int numBands) {
        long size = (long)w * (long)h;
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Width (" + w + ") and height (" + h + ") must be > 0");
        }
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Dimensions (width=" + w + " height=" + h + ") are too large");
        }
        if (dataType < 0 || dataType > 5 && dataType != 32) {
            throw new IllegalArgumentException("Unsupported dataType: " + dataType);
        }
        if (numBands <= 0) {
            throw new IllegalArgumentException("Number of bands must be > 0");
        }
        this.dataType = dataType;
        this.width = w;
        this.height = h;
        this.numBands = numBands;
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    public final int getNumBands() {
        return this.numBands;
    }

    public abstract int getNumDataElements();

    public final int getDataType() {
        return this.dataType;
    }

    public int getTransferType() {
        return this.dataType;
    }

    public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
        int[] pixels = iArray != null ? iArray : new int[this.numBands];
        for (int i = 0; i < this.numBands; ++i) {
            pixels[i] = this.getSample(x, y, i, data);
        }
        return pixels;
    }

    public abstract Object getDataElements(int var1, int var2, Object var3, DataBuffer var4);

    public Object getDataElements(int x, int y, int w, int h, Object obj, DataBuffer data) {
        int type = this.getTransferType();
        int numDataElems = this.getNumDataElements();
        int cnt = 0;
        Object o = null;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        switch (type) {
            case 0: {
                byte[] bdata = obj == null ? new byte[numDataElems * w * h] : (byte[])obj;
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        o = this.getDataElements(j, i, o, data);
                        byte[] btemp = (byte[])o;
                        for (int k = 0; k < numDataElems; ++k) {
                            bdata[cnt++] = btemp[k];
                        }
                    }
                }
                obj = bdata;
                break;
            }
            case 1: 
            case 2: {
                short[] sdata = obj == null ? new short[numDataElems * w * h] : (short[])obj;
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        o = this.getDataElements(j, i, o, data);
                        short[] stemp = (short[])o;
                        for (int k = 0; k < numDataElems; ++k) {
                            sdata[cnt++] = stemp[k];
                        }
                    }
                }
                obj = sdata;
                break;
            }
            case 3: {
                int[] idata = obj == null ? new int[numDataElems * w * h] : (int[])obj;
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        o = this.getDataElements(j, i, o, data);
                        int[] itemp = (int[])o;
                        for (int k = 0; k < numDataElems; ++k) {
                            idata[cnt++] = itemp[k];
                        }
                    }
                }
                obj = idata;
                break;
            }
            case 4: {
                float[] fdata = obj == null ? new float[numDataElems * w * h] : (float[])obj;
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        o = this.getDataElements(j, i, o, data);
                        float[] ftemp = (float[])o;
                        for (int k = 0; k < numDataElems; ++k) {
                            fdata[cnt++] = ftemp[k];
                        }
                    }
                }
                obj = fdata;
                break;
            }
            case 5: {
                double[] ddata = obj == null ? new double[numDataElems * w * h] : (double[])obj;
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        o = this.getDataElements(j, i, o, data);
                        double[] dtemp = (double[])o;
                        for (int k = 0; k < numDataElems; ++k) {
                            ddata[cnt++] = dtemp[k];
                        }
                    }
                }
                obj = ddata;
            }
        }
        return obj;
    }

    public abstract void setDataElements(int var1, int var2, Object var3, DataBuffer var4);

    public void setDataElements(int x, int y, int w, int h, Object obj, DataBuffer data) {
        int cnt = 0;
        Object o = null;
        int type = this.getTransferType();
        int numDataElems = this.getNumDataElements();
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        switch (type) {
            case 0: {
                byte[] barray = (byte[])obj;
                byte[] btemp = new byte[numDataElems];
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        for (int k = 0; k < numDataElems; ++k) {
                            btemp[k] = barray[cnt++];
                        }
                        this.setDataElements(j, i, btemp, data);
                    }
                }
                break;
            }
            case 1: 
            case 2: {
                short[] sarray = (short[])obj;
                short[] stemp = new short[numDataElems];
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        for (int k = 0; k < numDataElems; ++k) {
                            stemp[k] = sarray[cnt++];
                        }
                        this.setDataElements(j, i, stemp, data);
                    }
                }
                break;
            }
            case 3: {
                int[] iArray = (int[])obj;
                int[] itemp = new int[numDataElems];
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        for (int k = 0; k < numDataElems; ++k) {
                            itemp[k] = iArray[cnt++];
                        }
                        this.setDataElements(j, i, itemp, data);
                    }
                }
                break;
            }
            case 4: {
                float[] fArray = (float[])obj;
                float[] ftemp = new float[numDataElems];
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        for (int k = 0; k < numDataElems; ++k) {
                            ftemp[k] = fArray[cnt++];
                        }
                        this.setDataElements(j, i, ftemp, data);
                    }
                }
                break;
            }
            case 5: {
                double[] dArray = (double[])obj;
                double[] dtemp = new double[numDataElems];
                for (int i = y; i < y1; ++i) {
                    for (int j = x; j < x1; ++j) {
                        for (int k = 0; k < numDataElems; ++k) {
                            dtemp[k] = dArray[cnt++];
                        }
                        this.setDataElements(j, i, dtemp, data);
                    }
                }
                break;
            }
        }
    }

    public float[] getPixel(int x, int y, float[] fArray, DataBuffer data) {
        float[] pixels = fArray != null ? fArray : new float[this.numBands];
        for (int i = 0; i < this.numBands; ++i) {
            pixels[i] = this.getSampleFloat(x, y, i, data);
        }
        return pixels;
    }

    public double[] getPixel(int x, int y, double[] dArray, DataBuffer data) {
        double[] pixels = dArray != null ? dArray : new double[this.numBands];
        for (int i = 0; i < this.numBands; ++i) {
            pixels[i] = this.getSampleDouble(x, y, i, data);
        }
        return pixels;
    }

    public int[] getPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        int[] pixels = iArray != null ? iArray : new int[this.numBands * w * h];
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    pixels[Offset++] = this.getSample(j, i, k, data);
                }
            }
        }
        return pixels;
    }

    public float[] getPixels(int x, int y, int w, int h, float[] fArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        float[] pixels = fArray != null ? fArray : new float[this.numBands * w * h];
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    pixels[Offset++] = this.getSampleFloat(j, i, k, data);
                }
            }
        }
        return pixels;
    }

    public double[] getPixels(int x, int y, int w, int h, double[] dArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        double[] pixels = dArray != null ? dArray : new double[this.numBands * w * h];
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    pixels[Offset++] = this.getSampleDouble(j, i, k, data);
                }
            }
        }
        return pixels;
    }

    public abstract int getSample(int var1, int var2, int var3, DataBuffer var4);

    public float getSampleFloat(int x, int y, int b, DataBuffer data) {
        float sample = this.getSample(x, y, b, data);
        return sample;
    }

    public double getSampleDouble(int x, int y, int b, DataBuffer data) {
        double sample = this.getSample(x, y, b, data);
        return sample;
    }

    public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x1 < x || x1 > this.width || y < 0 || y1 < y || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        int[] pixels = iArray != null ? iArray : new int[w * h];
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                pixels[Offset++] = this.getSample(j, i, b, data);
            }
        }
        return pixels;
    }

    public float[] getSamples(int x, int y, int w, int h, int b, float[] fArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x1 < x || x1 > this.width || y < 0 || y1 < y || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates");
        }
        float[] pixels = fArray != null ? fArray : new float[w * h];
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                pixels[Offset++] = this.getSampleFloat(j, i, b, data);
            }
        }
        return pixels;
    }

    public double[] getSamples(int x, int y, int w, int h, int b, double[] dArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x1 < x || x1 > this.width || y < 0 || y1 < y || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates");
        }
        double[] pixels = dArray != null ? dArray : new double[w * h];
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                pixels[Offset++] = this.getSampleDouble(j, i, b, data);
            }
        }
        return pixels;
    }

    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
        for (int i = 0; i < this.numBands; ++i) {
            this.setSample(x, y, i, iArray[i], data);
        }
    }

    public void setPixel(int x, int y, float[] fArray, DataBuffer data) {
        for (int i = 0; i < this.numBands; ++i) {
            this.setSample(x, y, i, fArray[i], data);
        }
    }

    public void setPixel(int x, int y, double[] dArray, DataBuffer data) {
        for (int i = 0; i < this.numBands; ++i) {
            this.setSample(x, y, i, dArray[i], data);
        }
    }

    public void setPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    this.setSample(j, i, k, iArray[Offset++], data);
                }
            }
        }
    }

    public void setPixels(int x, int y, int w, int h, float[] fArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    this.setSample(j, i, k, fArray[Offset++], data);
                }
            }
        }
    }

    public void setPixels(int x, int y, int w, int h, double[] dArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    this.setSample(j, i, k, dArray[Offset++], data);
                }
            }
        }
    }

    public abstract void setSample(int var1, int var2, int var3, int var4, DataBuffer var5);

    public void setSample(int x, int y, int b, float s, DataBuffer data) {
        int sample = (int)s;
        this.setSample(x, y, b, sample, data);
    }

    public void setSample(int x, int y, int b, double s, DataBuffer data) {
        int sample = (int)s;
        this.setSample(x, y, b, sample, data);
    }

    public void setSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                this.setSample(j, i, b, iArray[Offset++], data);
            }
        }
    }

    public void setSamples(int x, int y, int w, int h, int b, float[] fArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                this.setSample(j, i, b, fArray[Offset++], data);
            }
        }
    }

    public void setSamples(int x, int y, int w, int h, int b, double[] dArray, DataBuffer data) {
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i = y; i < y1; ++i) {
            for (int j = x; j < x1; ++j) {
                this.setSample(j, i, b, dArray[Offset++], data);
            }
        }
    }

    public abstract SampleModel createCompatibleSampleModel(int var1, int var2);

    public abstract SampleModel createSubsetSampleModel(int[] var1);

    public abstract DataBuffer createDataBuffer();

    public abstract int[] getSampleSize();

    public abstract int getSampleSize(int var1);

    static {
        ColorModel.loadLibraries();
        SampleModel.initIDs();
    }
}

