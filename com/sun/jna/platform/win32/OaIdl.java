/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.OaIdl$DISPID
 *  com.sun.jna.platform.win32.OaIdl$MEMBERID
 */
package com.sun.jna.platform.win32;

import com.sun.jna.IntegerType;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Dispatch;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.PointerByReference;
import java.io.Closeable;
import java.util.Calendar;
import java.util.Date;

public interface OaIdl {
    public static final long DATE_OFFSET = new Date(-1, 11, 30, 0, 0, 0).getTime();
    public static final DISPID DISPID_COLLECT = new DISPID(-8);
    public static final DISPID DISPID_CONSTRUCTOR = new DISPID(-6);
    public static final DISPID DISPID_DESTRUCTOR = new DISPID(-7);
    public static final DISPID DISPID_EVALUATE = new DISPID(-5);
    public static final DISPID DISPID_NEWENUM = new DISPID(-4);
    public static final DISPID DISPID_PROPERTYPUT = new DISPID(-3);
    public static final DISPID DISPID_UNKNOWN = new DISPID(-1);
    public static final DISPID DISPID_VALUE = new DISPID(0);
    public static final MEMBERID MEMBERID_NIL = new MEMBERID(DISPID_UNKNOWN.intValue());
    public static final int FADF_AUTO = 1;
    public static final int FADF_STATIC = 2;
    public static final int FADF_EMBEDDED = 4;
    public static final int FADF_FIXEDSIZE = 16;
    public static final int FADF_RECORD = 32;
    public static final int FADF_HAVEIID = 64;
    public static final int FADF_HAVEVARTYPE = 128;
    public static final int FADF_BSTR = 256;
    public static final int FADF_UNKNOWN = 512;
    public static final int FADF_DISPATCH = 1024;
    public static final int FADF_VARIANT = 2048;
    public static final int FADF_RESERVED = 61448;

    @Structure.FieldOrder(value={"wReserved", "decimal1", "Hi32", "decimal2"})
    public static class DECIMAL
    extends Structure {
        public short wReserved;
        public _DECIMAL1 decimal1;
        public NativeLong Hi32;
        public _DECIMAL2 decimal2;

        public DECIMAL() {
        }

        public DECIMAL(Pointer pointer) {
            super(pointer);
        }

        public static class _DECIMAL2
        extends Union {
            public WinDef.ULONGLONG Lo64;
            public _DECIMAL2_DECIMAL decimal2_DECIMAL;

            public _DECIMAL2() {
                this.setType("Lo64");
            }

            public _DECIMAL2(Pointer pointer) {
                super(pointer);
                this.setType("Lo64");
                this.read();
            }

            @Structure.FieldOrder(value={"Lo32", "Mid32"})
            public static class _DECIMAL2_DECIMAL
            extends Structure {
                public WinDef.BYTE Lo32;
                public WinDef.BYTE Mid32;

                public _DECIMAL2_DECIMAL() {
                }

                public _DECIMAL2_DECIMAL(Pointer pointer) {
                    super(pointer);
                }
            }
        }

        public static class _DECIMAL1
        extends Union {
            public WinDef.USHORT signscale;
            public _DECIMAL1_DECIMAL decimal1_DECIMAL;

            public _DECIMAL1() {
                this.setType("signscale");
            }

            public _DECIMAL1(Pointer pointer) {
                super(pointer);
                this.setType("signscale");
                this.read();
            }

            @Structure.FieldOrder(value={"scale", "sign"})
            public static class _DECIMAL1_DECIMAL
            extends Structure {
                public WinDef.BYTE scale;
                public WinDef.BYTE sign;

                public _DECIMAL1_DECIMAL() {
                }

                public _DECIMAL1_DECIMAL(Pointer pointer) {
                    super(pointer);
                }
            }
        }

        public static class ByReference
        extends DECIMAL
        implements Structure.ByReference {
        }
    }

    public static class CURRENCY
    extends Union {
        public _CURRENCY currency;
        public WinDef.LONGLONG int64;

        public CURRENCY() {
        }

        public CURRENCY(Pointer pointer) {
            super(pointer);
            this.read();
        }

        @Structure.FieldOrder(value={"Lo", "Hi"})
        public static class _CURRENCY
        extends Structure {
            public WinDef.ULONG Lo;
            public WinDef.LONG Hi;

            public _CURRENCY() {
            }

            public _CURRENCY(Pointer pointer) {
                super(pointer);
                this.read();
            }
        }

        public static class ByReference
        extends CURRENCY
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"cElements", "lLbound"})
    public static class SAFEARRAYBOUND
    extends Structure {
        public WinDef.ULONG cElements;
        public WinDef.LONG lLbound;

        public SAFEARRAYBOUND() {
        }

        public SAFEARRAYBOUND(Pointer pointer) {
            super(pointer);
            this.read();
        }

        public SAFEARRAYBOUND(int cElements, int lLbound) {
            this.cElements = new WinDef.ULONG((long)cElements);
            this.lLbound = new WinDef.LONG((long)lLbound);
            this.write();
        }
    }

    @Structure.FieldOrder(value={"pSAFEARRAY"})
    public static class SAFEARRAYByReference
    extends Structure
    implements Structure.ByReference {
        public SAFEARRAY.ByReference pSAFEARRAY;

        public SAFEARRAYByReference() {
        }

        public SAFEARRAYByReference(Pointer p) {
            super(p);
            this.read();
        }

        public SAFEARRAYByReference(SAFEARRAY.ByReference safeArray) {
            this.pSAFEARRAY = safeArray;
        }
    }

    @Structure.FieldOrder(value={"cDims", "fFeatures", "cbElements", "cLocks", "pvData", "rgsabound"})
    public static class SAFEARRAY
    extends Structure
    implements Closeable {
        public WinDef.USHORT cDims;
        public WinDef.USHORT fFeatures;
        public WinDef.ULONG cbElements;
        public WinDef.ULONG cLocks;
        public WinDef.PVOID pvData;
        public SAFEARRAYBOUND[] rgsabound = new SAFEARRAYBOUND[]{new SAFEARRAYBOUND()};

        public SAFEARRAY() {
        }

        public SAFEARRAY(Pointer pointer) {
            super(pointer);
            this.read();
        }

        @Override
        public void read() {
            super.read();
            this.rgsabound = this.cDims.intValue() > 0 ? (SAFEARRAYBOUND[])this.rgsabound[0].toArray(this.cDims.intValue()) : new SAFEARRAYBOUND[]{new SAFEARRAYBOUND()};
        }

        public static ByReference createSafeArray(int ... size) {
            return SAFEARRAY.createSafeArray(new WTypes.VARTYPE(12), size);
        }

        public static ByReference createSafeArray(WTypes.VARTYPE vartype, int ... size) {
            SAFEARRAYBOUND[] rgsabound = (SAFEARRAYBOUND[])new SAFEARRAYBOUND().toArray(size.length);
            for (int i = 0; i < size.length; ++i) {
                rgsabound[i].lLbound = new WinDef.LONG(0L);
                rgsabound[i].cElements = new WinDef.ULONG((long)size[size.length - i - 1]);
            }
            ByReference data = OleAuto.INSTANCE.SafeArrayCreate(vartype, new WinDef.UINT((long)size.length), rgsabound);
            return data;
        }

        public void putElement(Object arg, int ... indices) {
            WinDef.LONG[] paramIndices = new WinDef.LONG[indices.length];
            for (int i = 0; i < indices.length; ++i) {
                paramIndices[i] = new WinDef.LONG((long)indices[indices.length - i - 1]);
            }
            switch (this.getVarType().intValue()) {
                case 11: {
                    Memory mem = new Memory(2L);
                    if (arg instanceof Boolean) {
                        mem.setShort(0L, (short)((Boolean)arg != false ? 65535 : 0));
                    } else {
                        mem.setShort(0L, (short)(((Number)arg).intValue() > 0 ? 65535 : 0));
                    }
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 16: 
                case 17: {
                    Memory mem = new Memory(1L);
                    mem.setByte(0L, ((Number)arg).byteValue());
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 2: 
                case 18: {
                    Memory mem = new Memory(2L);
                    mem.setShort(0L, ((Number)arg).shortValue());
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 3: 
                case 19: 
                case 22: 
                case 23: {
                    Memory mem = new Memory(4L);
                    mem.setInt(0L, ((Number)arg).intValue());
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 10: {
                    Memory mem = new Memory(4L);
                    mem.setInt(0L, ((Number)arg).intValue());
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 4: {
                    Memory mem = new Memory(4L);
                    mem.setFloat(0L, ((Number)arg).floatValue());
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 5: {
                    Memory mem = new Memory(8L);
                    mem.setDouble(0L, ((Number)arg).doubleValue());
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 7: {
                    Memory mem = new Memory(8L);
                    mem.setDouble(0L, ((DATE)arg).date);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    break;
                }
                case 8: {
                    if (arg instanceof String) {
                        WTypes.BSTR bstr = OleAuto.INSTANCE.SysAllocString((String)arg);
                        WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, bstr.getPointer());
                        OleAuto.INSTANCE.SysFreeString(bstr);
                        COMUtils.checkRC(hr);
                        break;
                    }
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, ((WTypes.BSTR)arg).getPointer());
                    COMUtils.checkRC(hr);
                    break;
                }
                case 12: {
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, ((Variant.VARIANT)arg).getPointer());
                    COMUtils.checkRC(hr);
                    break;
                }
                case 13: {
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, ((Unknown)arg).getPointer());
                    COMUtils.checkRC(hr);
                    break;
                }
                case 9: {
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, ((Dispatch)arg).getPointer());
                    COMUtils.checkRC(hr);
                    break;
                }
                case 6: {
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, ((CURRENCY)arg).getPointer());
                    COMUtils.checkRC(hr);
                    break;
                }
                case 14: {
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPutElement(this, paramIndices, ((DECIMAL)arg).getPointer());
                    COMUtils.checkRC(hr);
                    break;
                }
                default: {
                    throw new IllegalStateException("Can't parse array content - type not supported: " + this.getVarType().intValue());
                }
            }
        }

        public Object getElement(int ... indices) {
            Object result;
            WinDef.LONG[] paramIndices = new WinDef.LONG[indices.length];
            for (int i = 0; i < indices.length; ++i) {
                paramIndices[i] = new WinDef.LONG((long)indices[indices.length - i - 1]);
            }
            switch (this.getVarType().intValue()) {
                case 11: {
                    Memory mem = new Memory(2L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = mem.getShort(0L) != 0;
                    break;
                }
                case 16: 
                case 17: {
                    Memory mem = new Memory(1L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = mem.getByte(0L);
                    break;
                }
                case 2: 
                case 18: {
                    Memory mem = new Memory(2L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = mem.getShort(0L);
                    break;
                }
                case 3: 
                case 19: 
                case 22: 
                case 23: {
                    Memory mem = new Memory(4L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = mem.getInt(0L);
                    break;
                }
                case 10: {
                    Memory mem = new Memory(4L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = new WinDef.SCODE((long)mem.getInt(0L));
                    break;
                }
                case 4: {
                    Memory mem = new Memory(4L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = Float.valueOf(mem.getFloat(0L));
                    break;
                }
                case 5: {
                    Memory mem = new Memory(8L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = mem.getDouble(0L);
                    break;
                }
                case 7: {
                    Memory mem = new Memory(8L);
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, mem);
                    COMUtils.checkRC(hr);
                    result = new DATE(mem.getDouble(0L));
                    break;
                }
                case 8: {
                    PointerByReference pbr = new PointerByReference();
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, pbr.getPointer());
                    COMUtils.checkRC(hr);
                    WTypes.BSTR bstr = new WTypes.BSTR(pbr.getValue());
                    result = bstr.getValue();
                    OleAuto.INSTANCE.SysFreeString(bstr);
                    break;
                }
                case 12: {
                    Variant.VARIANT holder = new Variant.VARIANT();
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, holder.getPointer());
                    COMUtils.checkRC(hr);
                    result = holder;
                    break;
                }
                case 13: {
                    PointerByReference pbr = new PointerByReference();
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, pbr.getPointer());
                    COMUtils.checkRC(hr);
                    result = new Unknown(pbr.getValue());
                    break;
                }
                case 9: {
                    PointerByReference pbr = new PointerByReference();
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, pbr.getPointer());
                    COMUtils.checkRC(hr);
                    result = new Dispatch(pbr.getValue());
                    break;
                }
                case 6: {
                    CURRENCY currency = new CURRENCY();
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, currency.getPointer());
                    COMUtils.checkRC(hr);
                    result = currency;
                    break;
                }
                case 14: {
                    DECIMAL decimal = new DECIMAL();
                    WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayGetElement(this, paramIndices, decimal.getPointer());
                    COMUtils.checkRC(hr);
                    result = decimal;
                    break;
                }
                default: {
                    throw new IllegalStateException("Can't parse array content - type not supported: " + this.getVarType().intValue());
                }
            }
            return result;
        }

        public Pointer ptrOfIndex(int ... indices) {
            WinDef.LONG[] paramIndices = new WinDef.LONG[indices.length];
            for (int i = 0; i < indices.length; ++i) {
                paramIndices[i] = new WinDef.LONG((long)indices[indices.length - i - 1]);
            }
            PointerByReference pbr = new PointerByReference();
            WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayPtrOfIndex(this, paramIndices, pbr);
            COMUtils.checkRC(hr);
            return pbr.getValue();
        }

        public void destroy() {
            WinNT.HRESULT res = OleAuto.INSTANCE.SafeArrayDestroy(this);
            COMUtils.checkRC(res);
        }

        @Override
        public void close() {
            this.destroy();
        }

        public int getLBound(int dimension) {
            int targetDimension = this.getDimensionCount() - dimension;
            WinDef.LONGByReference bound = new WinDef.LONGByReference();
            WinNT.HRESULT res = OleAuto.INSTANCE.SafeArrayGetLBound(this, new WinDef.UINT((long)targetDimension), bound);
            COMUtils.checkRC(res);
            return bound.getValue().intValue();
        }

        public int getUBound(int dimension) {
            int targetDimension = this.getDimensionCount() - dimension;
            WinDef.LONGByReference bound = new WinDef.LONGByReference();
            WinNT.HRESULT res = OleAuto.INSTANCE.SafeArrayGetUBound(this, new WinDef.UINT((long)targetDimension), bound);
            COMUtils.checkRC(res);
            return bound.getValue().intValue();
        }

        public int getDimensionCount() {
            return OleAuto.INSTANCE.SafeArrayGetDim(this).intValue();
        }

        public Pointer accessData() {
            PointerByReference pbr = new PointerByReference();
            WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayAccessData(this, pbr);
            COMUtils.checkRC(hr);
            return pbr.getValue();
        }

        public void unaccessData() {
            WinNT.HRESULT hr = OleAuto.INSTANCE.SafeArrayUnaccessData(this);
            COMUtils.checkRC(hr);
        }

        public void lock() {
            WinNT.HRESULT res = OleAuto.INSTANCE.SafeArrayLock(this);
            COMUtils.checkRC(res);
        }

        public void unlock() {
            WinNT.HRESULT res = OleAuto.INSTANCE.SafeArrayUnlock(this);
            COMUtils.checkRC(res);
        }

        public void redim(int cElements, int lLbound) {
            WinNT.HRESULT res = OleAuto.INSTANCE.SafeArrayRedim(this, new SAFEARRAYBOUND(cElements, lLbound));
            COMUtils.checkRC(res);
        }

        public WTypes.VARTYPE getVarType() {
            WTypes.VARTYPEByReference resultHolder = new WTypes.VARTYPEByReference();
            WinNT.HRESULT res = OleAuto.INSTANCE.SafeArrayGetVartype(this, resultHolder);
            COMUtils.checkRC(res);
            return resultHolder.getValue();
        }

        public long getElemsize() {
            return OleAuto.INSTANCE.SafeArrayGetElemsize(this).longValue();
        }

        public static class ByReference
        extends SAFEARRAY
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"date"})
    public static class DATE
    extends Structure {
        private static final double MILLISECONDS_PER_DAY = 8.64E7;
        public double date;

        public DATE() {
        }

        public DATE(double date) {
            this.date = date;
        }

        public DATE(Date javaDate) {
            this.setFromJavaDate(javaDate);
        }

        public Date getAsJavaDate() {
            WinBase.SYSTEMTIME systemtime = new WinBase.SYSTEMTIME();
            OleAuto.INSTANCE.VariantTimeToSystemTime(this.date, systemtime);
            Calendar calendar = systemtime.toCalendar();
            int millis = (int)((long)(Math.abs(this.date) * 8.64E7 + 0.5) % 1000L);
            if (this.date > 0.0 && millis > 500 || this.date < 0.0 && millis > 499) {
                millis -= 1000;
            }
            calendar.set(14, millis);
            return calendar.getTime();
        }

        public void setFromJavaDate(Date javaDate) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(javaDate);
            DoubleByReference pvtime = new DoubleByReference();
            OleAuto.INSTANCE.SystemTimeToVariantTime(new WinBase.SYSTEMTIME(calendar), pvtime);
            double value = pvtime.getValue();
            this.date = value + Math.signum(value) * (double)calendar.get(14) / 8.64E7;
        }

        public static class ByReference
        extends DATE
        implements Structure.ByReference {
        }
    }

    public static class _VARIANT_BOOLByReference
    extends ByReference {
        public _VARIANT_BOOLByReference() {
            this(new VARIANT_BOOL(0L));
        }

        public _VARIANT_BOOLByReference(VARIANT_BOOL value) {
            super(2);
            this.setValue(value);
        }

        public void setValue(VARIANT_BOOL value) {
            this.getPointer().setShort(0L, value.shortValue());
        }

        public VARIANT_BOOL getValue() {
            return new VARIANT_BOOL((long)this.getPointer().getShort(0L));
        }
    }

    public static class VARIANT_BOOLByReference
    extends ByReference {
        public VARIANT_BOOLByReference() {
            this(new VARIANT_BOOL(0L));
        }

        public VARIANT_BOOLByReference(VARIANT_BOOL value) {
            super(2);
            this.setValue(value);
        }

        public void setValue(VARIANT_BOOL value) {
            this.getPointer().setShort(0L, value.shortValue());
        }

        public VARIANT_BOOL getValue() {
            return new VARIANT_BOOL((long)this.getPointer().getShort(0L));
        }
    }

    public static class VARIANT_BOOL
    extends IntegerType {
        private static final long serialVersionUID = 1L;
        public static final int SIZE = 2;

        public VARIANT_BOOL() {
            this(0L);
        }

        public VARIANT_BOOL(long value) {
            super(2, value);
        }

        public VARIANT_BOOL(boolean value) {
            this(value ? 65535L : 0L);
        }

        public boolean booleanValue() {
            return this.shortValue() != 0;
        }
    }
}

