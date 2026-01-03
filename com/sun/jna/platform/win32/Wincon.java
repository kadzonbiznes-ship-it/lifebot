/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.Wincon$COORD
 *  com.sun.jna.platform.win32.Wincon$INPUT_RECORD$Event
 *  com.sun.jna.platform.win32.Wincon$SMALL_RECT
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.ptr.IntByReference;

public interface Wincon {
    public static final int ATTACH_PARENT_PROCESS = -1;
    public static final int CTRL_C_EVENT = 0;
    public static final int CTRL_BREAK_EVENT = 1;
    public static final int STD_INPUT_HANDLE = -10;
    public static final int STD_OUTPUT_HANDLE = -11;
    public static final int STD_ERROR_HANDLE = -12;
    public static final int CONSOLE_FULLSCREEN = 1;
    public static final int CONSOLE_FULLSCREEN_HARDWARE = 2;
    public static final int ENABLE_PROCESSED_INPUT = 1;
    public static final int ENABLE_LINE_INPUT = 2;
    public static final int ENABLE_ECHO_INPUT = 4;
    public static final int ENABLE_WINDOW_INPUT = 8;
    public static final int ENABLE_MOUSE_INPUT = 16;
    public static final int ENABLE_INSERT_MODE = 32;
    public static final int ENABLE_QUICK_EDIT_MODE = 64;
    public static final int ENABLE_EXTENDED_FLAGS = 128;
    public static final int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
    public static final int DISABLE_NEWLINE_AUTO_RETURN = 8;
    public static final int ENABLE_VIRTUAL_TERMINAL_INPUT = 512;
    public static final int ENABLE_PROCESSED_OUTPUT = 1;
    public static final int ENABLE_WRAP_AT_EOL_OUTPUT = 2;
    public static final int MAX_CONSOLE_TITLE_LENGTH = 65536;

    public boolean AllocConsole();

    public boolean FreeConsole();

    public boolean AttachConsole(int var1);

    public boolean FlushConsoleInputBuffer(WinNT.HANDLE var1);

    public boolean GenerateConsoleCtrlEvent(int var1, int var2);

    public int GetConsoleCP();

    public boolean SetConsoleCP(int var1);

    public int GetConsoleOutputCP();

    public boolean SetConsoleOutputCP(int var1);

    public WinDef.HWND GetConsoleWindow();

    public boolean GetNumberOfConsoleInputEvents(WinNT.HANDLE var1, IntByReference var2);

    public boolean GetNumberOfConsoleMouseButtons(IntByReference var1);

    public WinNT.HANDLE GetStdHandle(int var1);

    public boolean SetStdHandle(int var1, WinNT.HANDLE var2);

    public boolean GetConsoleDisplayMode(IntByReference var1);

    public boolean GetConsoleMode(WinNT.HANDLE var1, IntByReference var2);

    public boolean SetConsoleMode(WinNT.HANDLE var1, int var2);

    public int GetConsoleTitle(char[] var1, int var2);

    public int GetConsoleOriginalTitle(char[] var1, int var2);

    public boolean SetConsoleTitle(String var1);

    public boolean GetConsoleScreenBufferInfo(WinNT.HANDLE var1, CONSOLE_SCREEN_BUFFER_INFO var2);

    public boolean ReadConsoleInput(WinNT.HANDLE var1, INPUT_RECORD[] var2, int var3, IntByReference var4);

    public boolean WriteConsole(WinNT.HANDLE var1, String var2, int var3, IntByReference var4, WinDef.LPVOID var5);

    @Structure.FieldOrder(value={"EventType", "Event"})
    public static class INPUT_RECORD
    extends Structure {
        public static final short KEY_EVENT = 1;
        public static final short MOUSE_EVENT = 2;
        public static final short WINDOW_BUFFER_SIZE_EVENT = 4;
        public short EventType;
        public Event Event;

        @Override
        public void read() {
            super.read();
            switch (this.EventType) {
                case 1: {
                    this.Event.setType("KeyEvent");
                    break;
                }
                case 2: {
                    this.Event.setType("MouseEvent");
                    break;
                }
                case 4: {
                    this.Event.setType("WindowBufferSizeEvent");
                }
            }
            this.Event.read();
        }

        @Override
        public String toString() {
            return String.format("INPUT_RECORD(%s)", this.EventType);
        }
    }

    @Structure.FieldOrder(value={"dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize"})
    public static class CONSOLE_SCREEN_BUFFER_INFO
    extends Structure {
        public COORD dwSize;
        public COORD dwCursorPosition;
        public short wAttributes;
        public SMALL_RECT srWindow;
        public COORD dwMaximumWindowSize;

        @Override
        public String toString() {
            return String.format("CONSOLE_SCREEN_BUFFER_INFO(%s,%s,%s,%s,%s)", this.dwSize, this.dwCursorPosition, this.wAttributes, this.srWindow, this.dwMaximumWindowSize);
        }
    }
}

