/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.ByReference;
import java.awt.Rectangle;

public interface WinDef {
    public static final int MAX_PATH = 260;

    public static class CHARByReference
    extends ByReference {
        public CHARByReference() {
            this(new CHAR(0L));
        }

        public CHARByReference(CHAR value) {
            super(1);
            this.setValue(value);
        }

        public void setValue(CHAR value) {
            this.getPointer().setByte(0L, value.byteValue());
        }

        public CHAR getValue() {
            return new CHAR(this.getPointer().getByte(0L));
        }
    }

    public static class CHAR
    extends IntegerType
    implements Comparable<CHAR> {
        public static final int SIZE = 1;

        public CHAR() {
            this(0L);
        }

        public CHAR(byte ch) {
            this((long)(ch & 0xFF));
        }

        public CHAR(long value) {
            super(1, value, false);
        }

        @Override
        public int compareTo(CHAR other) {
            return CHAR.compare(this, other);
        }
    }

    public static class BYTE
    extends UCHAR {
        public BYTE() {
            this(0L);
        }

        public BYTE(long value) {
            super(value);
        }
    }

    public static class UCHAR
    extends IntegerType
    implements Comparable<UCHAR> {
        public static final int SIZE = 1;

        public UCHAR() {
            this(0L);
        }

        public UCHAR(char ch) {
            this((long)(ch & 0xFF));
        }

        public UCHAR(long value) {
            super(1, value, true);
        }

        @Override
        public int compareTo(UCHAR other) {
            return UCHAR.compare(this, other);
        }
    }

    public static class BOOLByReference
    extends ByReference {
        public BOOLByReference() {
            this(new BOOL(0L));
        }

        public BOOLByReference(BOOL value) {
            super(4);
            this.setValue(value);
        }

        public void setValue(BOOL value) {
            this.getPointer().setInt(0L, value.intValue());
        }

        public BOOL getValue() {
            return new BOOL((long)this.getPointer().getInt(0L));
        }
    }

    public static class BOOL
    extends IntegerType
    implements Comparable<BOOL> {
        public static final int SIZE = 4;

        public BOOL() {
            this(0L);
        }

        public BOOL(boolean value) {
            this(value ? 1L : 0L);
        }

        public BOOL(long value) {
            super(4, value, false);
            assert (value == 0L || value == 1L);
        }

        public boolean booleanValue() {
            return this.intValue() > 0;
        }

        @Override
        public String toString() {
            return Boolean.toString(this.booleanValue());
        }

        @Override
        public int compareTo(BOOL other) {
            return BOOL.compare(this, other);
        }

        public static int compare(BOOL v1, BOOL v2) {
            if (v1 == v2) {
                return 0;
            }
            if (v1 == null) {
                return 1;
            }
            if (v2 == null) {
                return -1;
            }
            return BOOL.compare(v1.booleanValue(), v2.booleanValue());
        }

        public static int compare(BOOL v1, boolean v2) {
            if (v1 == null) {
                return 1;
            }
            return BOOL.compare(v1.booleanValue(), v2);
        }

        public static int compare(boolean v1, boolean v2) {
            if (v1 == v2) {
                return 0;
            }
            if (v1) {
                return 1;
            }
            return -1;
        }
    }

    public static class LCID
    extends DWORD {
        public LCID() {
            super(0L);
        }

        public LCID(long value) {
            super(value);
        }
    }

    public static class SCODEByReference
    extends ByReference {
        public SCODEByReference() {
            this(new SCODE(0L));
        }

        public SCODEByReference(SCODE value) {
            super(SCODE.SIZE);
            this.setValue(value);
        }

        public void setValue(SCODE value) {
            this.getPointer().setInt(0L, value.intValue());
        }

        public SCODE getValue() {
            return new SCODE((long)this.getPointer().getInt(0L));
        }
    }

    public static class SCODE
    extends ULONG {
        public SCODE() {
            this(0L);
        }

        public SCODE(long value) {
            super(value);
        }
    }

    public static class UINTByReference
    extends ByReference {
        public UINTByReference() {
            this(new UINT(0L));
        }

        public UINTByReference(UINT value) {
            super(4);
            this.setValue(value);
        }

        public void setValue(UINT value) {
            this.getPointer().setInt(0L, value.intValue());
        }

        public UINT getValue() {
            return new UINT((long)this.getPointer().getInt(0L));
        }
    }

    public static class UINT
    extends IntegerType
    implements Comparable<UINT> {
        public static final int SIZE = 4;

        public UINT() {
            this(0L);
        }

        public UINT(long value) {
            super(4, value, true);
        }

        @Override
        public int compareTo(UINT other) {
            return UINT.compare(this, other);
        }
    }

    public static class SHORT
    extends IntegerType
    implements Comparable<SHORT> {
        public static final int SIZE = 2;

        public SHORT() {
            this(0L);
        }

        public SHORT(long value) {
            super(2, value, false);
        }

        @Override
        public int compareTo(SHORT other) {
            return SHORT.compare(this, other);
        }
    }

    public static class USHORTByReference
    extends ByReference {
        public USHORTByReference() {
            this(new USHORT(0L));
        }

        public USHORTByReference(USHORT value) {
            super(2);
            this.setValue(value);
        }

        public USHORTByReference(short value) {
            super(2);
            this.setValue(new USHORT((long)value));
        }

        public void setValue(USHORT value) {
            this.getPointer().setShort(0L, value.shortValue());
        }

        public USHORT getValue() {
            return new USHORT((long)this.getPointer().getShort(0L));
        }
    }

    public static class USHORT
    extends IntegerType
    implements Comparable<USHORT> {
        public static final int SIZE = 2;

        public USHORT() {
            this(0L);
        }

        public USHORT(long value) {
            super(2, value, true);
        }

        @Override
        public int compareTo(USHORT other) {
            return USHORT.compare(this, other);
        }
    }

    @Structure.FieldOrder(value={"x", "y"})
    public static class POINT
    extends Structure {
        public int x;
        public int y;

        public POINT() {
        }

        public POINT(Pointer memory) {
            super(memory);
            this.read();
        }

        public POINT(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public static class ByValue
        extends POINT
        implements Structure.ByValue {
            public ByValue() {
            }

            public ByValue(Pointer memory) {
                super(memory);
            }

            public ByValue(int x, int y) {
                super(x, y);
            }
        }
    }

    public static class LPVOID
    extends PointerType {
        public LPVOID() {
        }

        public LPVOID(Pointer p) {
            super(p);
        }
    }

    public static class PVOID
    extends PointerType {
        public PVOID() {
        }

        public PVOID(Pointer pointer) {
            super(pointer);
        }
    }

    public static class ATOM
    extends WORD {
        public ATOM() {
            this(0L);
        }

        public ATOM(long value) {
            super(value);
        }
    }

    public static class ULONGLONGByReference
    extends ByReference {
        public ULONGLONGByReference() {
            this(new ULONGLONG(0L));
        }

        public ULONGLONGByReference(ULONGLONG value) {
            super(8);
            this.setValue(value);
        }

        public void setValue(ULONGLONG value) {
            this.getPointer().setLong(0L, value.longValue());
        }

        public ULONGLONG getValue() {
            return new ULONGLONG(this.getPointer().getLong(0L));
        }
    }

    public static class ULONGLONG
    extends IntegerType
    implements Comparable<ULONGLONG> {
        public static final int SIZE = 8;

        public ULONGLONG() {
            this(0L);
        }

        public ULONGLONG(long value) {
            super(8, value, true);
        }

        @Override
        public int compareTo(ULONGLONG other) {
            return ULONGLONG.compare(this, other);
        }
    }

    public static class ULONGByReference
    extends ByReference {
        public ULONGByReference() {
            this(new ULONG(0L));
        }

        public ULONGByReference(ULONG value) {
            super(ULONG.SIZE);
            this.setValue(value);
        }

        public void setValue(ULONG value) {
            this.getPointer().setInt(0L, value.intValue());
        }

        public ULONG getValue() {
            return new ULONG((long)this.getPointer().getInt(0L));
        }
    }

    public static class ULONG
    extends IntegerType
    implements Comparable<ULONG> {
        public static final int SIZE = Native.LONG_SIZE;

        public ULONG() {
            this(0L);
        }

        public ULONG(long value) {
            super(SIZE, value, true);
        }

        @Override
        public int compareTo(ULONG other) {
            return ULONG.compare(this, other);
        }
    }

    @Structure.FieldOrder(value={"left", "top", "right", "bottom"})
    public static class RECT
    extends Structure {
        public int left;
        public int top;
        public int right;
        public int bottom;

        public Rectangle toRectangle() {
            return new Rectangle(this.left, this.top, this.right - this.left, this.bottom - this.top);
        }

        @Override
        public String toString() {
            return "[(" + this.left + "," + this.top + ")(" + this.right + "," + this.bottom + ")]";
        }
    }

    public static class WPARAM
    extends UINT_PTR {
        public WPARAM() {
            this(0L);
        }

        public WPARAM(long value) {
            super(value);
        }
    }

    public static class UINT_PTR
    extends IntegerType {
        public UINT_PTR() {
            super(Native.POINTER_SIZE);
        }

        public UINT_PTR(long value) {
            super(Native.POINTER_SIZE, value, true);
        }

        public Pointer toPointer() {
            return Pointer.createConstant(this.longValue());
        }
    }

    public static class LRESULT
    extends BaseTSD.LONG_PTR {
        public LRESULT() {
            this(0L);
        }

        public LRESULT(long value) {
            super(value);
        }
    }

    public static class LPARAM
    extends BaseTSD.LONG_PTR {
        public LPARAM() {
            this(0L);
        }

        public LPARAM(long value) {
            super(value);
        }
    }

    public static class HKL
    extends WinNT.HANDLE {
        public HKL() {
        }

        public HKL(Pointer p) {
            super(p);
        }

        public HKL(int i) {
            super(Pointer.createConstant(i));
        }

        public int getLanguageIdentifier() {
            return (int)(Pointer.nativeValue(this.getPointer()) & 0xFFFFL);
        }

        public int getDeviceHandle() {
            return (int)(Pointer.nativeValue(this.getPointer()) >> 16 & 0xFFFFL);
        }

        @Override
        public String toString() {
            return String.format("%08x", Pointer.nativeValue(this.getPointer()));
        }
    }

    public static class HMODULE
    extends HINSTANCE {
    }

    public static class HINSTANCE
    extends WinNT.HANDLE {
    }

    public static class HWND
    extends WinNT.HANDLE {
        public HWND() {
        }

        public HWND(Pointer p) {
            super(p);
        }
    }

    public static class HRGN
    extends WinNT.HANDLE {
        public HRGN() {
        }

        public HRGN(Pointer p) {
            super(p);
        }
    }

    public static class HRSRC
    extends WinNT.HANDLE {
        public HRSRC() {
        }

        public HRSRC(Pointer p) {
            super(p);
        }
    }

    public static class HMENU
    extends WinNT.HANDLE {
        public HMENU() {
        }

        public HMENU(Pointer p) {
            super(p);
        }
    }

    public static class HICON
    extends WinNT.HANDLE {
        public HICON() {
        }

        public HICON(WinNT.HANDLE handle) {
            this(handle.getPointer());
        }

        public HICON(Pointer p) {
            super(p);
        }
    }

    public static class HDC
    extends WinNT.HANDLE {
        public HDC() {
        }

        public HDC(Pointer p) {
            super(p);
        }
    }

    public static class LONGLONGByReference
    extends ByReference {
        public LONGLONGByReference() {
            this(new LONGLONG(0L));
        }

        public LONGLONGByReference(LONGLONG value) {
            super(LONGLONG.SIZE);
            this.setValue(value);
        }

        public void setValue(LONGLONG value) {
            this.getPointer().setLong(0L, value.longValue());
        }

        public LONGLONG getValue() {
            return new LONGLONG(this.getPointer().getLong(0L));
        }
    }

    public static class LONGLONG
    extends IntegerType
    implements Comparable<LONGLONG> {
        public static final int SIZE = Native.LONG_SIZE * 2;

        public LONGLONG() {
            this(0L);
        }

        public LONGLONG(long value) {
            super(8, value, false);
        }

        @Override
        public int compareTo(LONGLONG other) {
            return LONGLONG.compare(this, other);
        }
    }

    public static class LONGByReference
    extends ByReference {
        public LONGByReference() {
            this(new LONG(0L));
        }

        public LONGByReference(LONG value) {
            super(LONG.SIZE);
            this.setValue(value);
        }

        public void setValue(LONG value) {
            this.getPointer().setInt(0L, value.intValue());
        }

        public LONG getValue() {
            return new LONG((long)this.getPointer().getInt(0L));
        }
    }

    public static class LONG
    extends IntegerType
    implements Comparable<LONG> {
        public static final int SIZE = Native.LONG_SIZE;

        public LONG() {
            this(0L);
        }

        public LONG(long value) {
            super(SIZE, value);
        }

        @Override
        public int compareTo(LONG other) {
            return LONG.compare(this, other);
        }
    }

    public static class DWORDByReference
    extends ByReference {
        public DWORDByReference() {
            this(new DWORD(0L));
        }

        public DWORDByReference(DWORD value) {
            super(4);
            this.setValue(value);
        }

        public void setValue(DWORD value) {
            this.getPointer().setInt(0L, value.intValue());
        }

        public DWORD getValue() {
            return new DWORD((long)this.getPointer().getInt(0L));
        }
    }

    public static class DWORD
    extends IntegerType
    implements Comparable<DWORD> {
        public static final int SIZE = 4;

        public DWORD() {
            this(0L);
        }

        public DWORD(long value) {
            super(4, value, true);
        }

        public WORD getLow() {
            return new WORD(this.longValue() & 0xFFFFL);
        }

        public WORD getHigh() {
            return new WORD(this.longValue() >> 16 & 0xFFFFL);
        }

        @Override
        public int compareTo(DWORD other) {
            return DWORD.compare(this, other);
        }
    }

    public static class WORD
    extends IntegerType
    implements Comparable<WORD> {
        public static final int SIZE = 2;

        public WORD() {
            this(0L);
        }

        public WORD(long value) {
            super(2, value, true);
        }

        @Override
        public int compareTo(WORD other) {
            return WORD.compare(this, other);
        }
    }
}

