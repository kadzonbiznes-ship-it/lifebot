/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.hid.kfc.api;

import com.sun.jna.Callback;
import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef;

public interface KfcLibrary
extends Library {
    public static final int KFC_MAX_KEYBOARD = 10;
    public static final int KFC_MAX_MOUSE = 10;
    public static final int KFC_MAX_DEVICE = 20;
    public static final int KFC_KEY_DOWN = 0;
    public static final int KFC_KEY_UP = 1;
    public static final int KFC_KEY_E0 = 2;
    public static final int KFC_KEY_E1 = 4;
    public static final int KFC_KEY_TERMSRV_SET_LED = 8;
    public static final int KFC_KEY_TERMSRV_SHADOW = 16;
    public static final int KFC_KEY_TERMSRV_VKPACKET = 32;
    public static final int KFC_KEY_CTRL = 512;
    public static final int KFC_KEY_ALT = 1024;
    public static final int KFC_KEY_SHIFT = 2048;
    public static final int KFC_KEY_LEFT_SHIFT = 42;
    public static final int KFC_KEY_RIGHT_SHIFT = 54;
    public static final int KFC_KEY_LEFT_CTRL = 29;
    public static final int KFC_KEY_RIGHT_CTRL = 29;
    public static final int KFC_KEY_LEFT_ALT = 56;
    public static final int KFC_KEY_RIGHT_ALT = 56;
    public static final int KFC_MOUSE_DOWN = 1;
    public static final int KFC_MOUSE_UP = 2;
    public static final int KFC_MOUSE_BUTTON_LEFT = 1;
    public static final int KFC_MOUSE_BUTTON_RIGHT = 2;
    public static final int KFC_MOUSE_BUTTON_MIDDLE = 4;

    public static int KFC_KEYBOARD(int index) {
        return index + 1;
    }

    public static int KFC_MOUSE(int index) {
        return 10 + index + 1;
    }

    public WinDef.BOOL kfc_responsive();

    public KfcContext kfc_create_context();

    public void kfc_destroy_context(KfcContext var1);

    public KfcPrecedence kfc_get_precedence(KfcContext var1, KfcDevice var2);

    public void kfc_set_precedence(KfcContext var1, KfcDevice var2, KfcPrecedence var3);

    public KfcFilter kfc_get_filter(KfcContext var1, KfcDevice var2);

    public void kfc_set_filter(KfcContext var1, KfcPredicate var2, KfcFilter var3);

    public KfcDevice kfc_wait(KfcContext var1);

    public KfcDevice kfc_wait_with_timeout(KfcContext var1, long var2);

    public int kfc_send(KfcContext var1, KfcDevice var2, Pointer var3, int var4);

    public int kfc_receive(KfcContext var1, KfcDevice var2, Pointer var3, int var4);

    public int kfc_get_hardware_id(KfcContext var1, KfcDevice var2, Pointer var3, int var4);

    public int kfc_is_invalid(KfcDevice var1);

    public int kfc_is_keyboard(KfcDevice var1);

    public int kfc_is_mouse(KfcDevice var1);

    public byte LoadKfc(byte[] var1, int var2);

    public static interface KfcPredicate
    extends Callback {
        public int apply(KfcDevice var1);
    }

    public static class KfcFilter
    extends IntegerType {
        public KfcFilter() {
            super(2, 0L, true);
        }

        public KfcFilter(long value) {
            super(2, value, true);
        }
    }

    public static class KfcPrecedence
    extends IntegerType {
        public KfcPrecedence() {
            super(4);
        }

        public KfcPrecedence(long value) {
            super(4, value);
        }
    }

    public static class KfcDevice
    extends IntegerType {
        public KfcDevice() {
            super(4);
        }

        public KfcDevice(long value) {
            super(4, value);
        }
    }

    public static class KfcContext
    extends PointerType {
        public KfcContext() {
        }

        public KfcContext(Pointer p) {
            super(p);
        }
    }
}

