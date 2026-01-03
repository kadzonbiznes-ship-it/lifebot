/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook.keyboard;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeInputEvent;
import java.awt.Toolkit;

public class NativeKeyEvent
extends NativeInputEvent {
    private static final long serialVersionUID = 8608981443834617646L;
    private int rawCode;
    private int keyCode;
    private char keyChar;
    private final int keyLocation;
    public static final int NATIVE_KEY_FIRST = 2400;
    public static final int NATIVE_KEY_LAST = 2402;
    public static final int NATIVE_KEY_TYPED = 2400;
    public static final int NATIVE_KEY_PRESSED = 2401;
    public static final int NATIVE_KEY_RELEASED = 2402;
    public static final int KEY_LOCATION_UNKNOWN = 0;
    public static final int KEY_LOCATION_STANDARD = 1;
    public static final int KEY_LOCATION_LEFT = 2;
    public static final int KEY_LOCATION_RIGHT = 3;
    public static final int KEY_LOCATION_NUMPAD = 4;
    public static final int VC_ESCAPE = 1;
    public static final int VC_F1 = 59;
    public static final int VC_F2 = 60;
    public static final int VC_F3 = 61;
    public static final int VC_F4 = 62;
    public static final int VC_F5 = 63;
    public static final int VC_F6 = 64;
    public static final int VC_F7 = 65;
    public static final int VC_F8 = 66;
    public static final int VC_F9 = 67;
    public static final int VC_F10 = 68;
    public static final int VC_F11 = 87;
    public static final int VC_F12 = 88;
    public static final int VC_F13 = 91;
    public static final int VC_F14 = 92;
    public static final int VC_F15 = 93;
    public static final int VC_F16 = 99;
    public static final int VC_F17 = 100;
    public static final int VC_F18 = 101;
    public static final int VC_F19 = 102;
    public static final int VC_F20 = 103;
    public static final int VC_F21 = 104;
    public static final int VC_F22 = 105;
    public static final int VC_F23 = 106;
    public static final int VC_F24 = 107;
    public static final int VC_BACKQUOTE = 41;
    public static final int VC_1 = 2;
    public static final int VC_2 = 3;
    public static final int VC_3 = 4;
    public static final int VC_4 = 5;
    public static final int VC_5 = 6;
    public static final int VC_6 = 7;
    public static final int VC_7 = 8;
    public static final int VC_8 = 9;
    public static final int VC_9 = 10;
    public static final int VC_0 = 11;
    public static final int VC_MINUS = 12;
    public static final int VC_EQUALS = 13;
    public static final int VC_BACKSPACE = 14;
    public static final int VC_TAB = 15;
    public static final int VC_CAPS_LOCK = 58;
    public static final int VC_A = 30;
    public static final int VC_B = 48;
    public static final int VC_C = 46;
    public static final int VC_D = 32;
    public static final int VC_E = 18;
    public static final int VC_F = 33;
    public static final int VC_G = 34;
    public static final int VC_H = 35;
    public static final int VC_I = 23;
    public static final int VC_J = 36;
    public static final int VC_K = 37;
    public static final int VC_L = 38;
    public static final int VC_M = 50;
    public static final int VC_N = 49;
    public static final int VC_O = 24;
    public static final int VC_P = 25;
    public static final int VC_Q = 16;
    public static final int VC_R = 19;
    public static final int VC_S = 31;
    public static final int VC_T = 20;
    public static final int VC_U = 22;
    public static final int VC_V = 47;
    public static final int VC_W = 17;
    public static final int VC_X = 45;
    public static final int VC_Y = 21;
    public static final int VC_Z = 44;
    public static final int VC_OPEN_BRACKET = 26;
    public static final int VC_CLOSE_BRACKET = 27;
    public static final int VC_BACK_SLASH = 43;
    public static final int VC_SEMICOLON = 39;
    public static final int VC_QUOTE = 40;
    public static final int VC_ENTER = 28;
    public static final int VC_COMMA = 51;
    public static final int VC_PERIOD = 52;
    public static final int VC_SLASH = 53;
    public static final int VC_SPACE = 57;
    public static final int VC_PRINTSCREEN = 3639;
    public static final int VC_SCROLL_LOCK = 70;
    public static final int VC_PAUSE = 3653;
    public static final int VC_INSERT = 3666;
    public static final int VC_DELETE = 3667;
    public static final int VC_HOME = 3655;
    public static final int VC_END = 3663;
    public static final int VC_PAGE_UP = 3657;
    public static final int VC_PAGE_DOWN = 3665;
    public static final int VC_UP = 57416;
    public static final int VC_LEFT = 57419;
    public static final int VC_CLEAR = 57420;
    public static final int VC_RIGHT = 57421;
    public static final int VC_DOWN = 57424;
    public static final int VC_NUM_LOCK = 69;
    public static final int VC_SEPARATOR = 83;
    public static final int VC_SHIFT = 42;
    public static final int VC_CONTROL = 29;
    public static final int VC_ALT = 56;
    public static final int VC_META = 3675;
    public static final int VC_CONTEXT_MENU = 3677;
    public static final int VC_POWER = 57438;
    public static final int VC_SLEEP = 57439;
    public static final int VC_WAKE = 57443;
    public static final int VC_MEDIA_PLAY = 57378;
    public static final int VC_MEDIA_STOP = 57380;
    public static final int VC_MEDIA_PREVIOUS = 57360;
    public static final int VC_MEDIA_NEXT = 57369;
    public static final int VC_MEDIA_SELECT = 57453;
    public static final int VC_MEDIA_EJECT = 57388;
    public static final int VC_VOLUME_MUTE = 57376;
    public static final int VC_VOLUME_UP = 57392;
    public static final int VC_VOLUME_DOWN = 57390;
    public static final int VC_APP_MAIL = 57452;
    public static final int VC_APP_CALCULATOR = 57377;
    public static final int VC_APP_MUSIC = 57404;
    public static final int VC_APP_PICTURES = 57444;
    public static final int VC_BROWSER_SEARCH = 57445;
    public static final int VC_BROWSER_HOME = 57394;
    public static final int VC_BROWSER_BACK = 57450;
    public static final int VC_BROWSER_FORWARD = 57449;
    public static final int VC_BROWSER_STOP = 57448;
    public static final int VC_BROWSER_REFRESH = 57447;
    public static final int VC_BROWSER_FAVORITES = 57446;
    public static final int VC_KATAKANA = 112;
    public static final int VC_UNDERSCORE = 115;
    public static final int VC_FURIGANA = 119;
    public static final int VC_KANJI = 121;
    public static final int VC_HIRAGANA = 123;
    public static final int VC_YEN = 125;
    public static final int VC_SUN_HELP = 65397;
    public static final int VC_SUN_STOP = 65400;
    public static final int VC_SUN_PROPS = 65398;
    public static final int VC_SUN_FRONT = 65399;
    public static final int VC_SUN_OPEN = 65396;
    public static final int VC_SUN_FIND = 65406;
    public static final int VC_SUN_AGAIN = 65401;
    public static final int VC_SUN_UNDO = 65402;
    public static final int VC_SUN_COPY = 65404;
    public static final int VC_SUN_INSERT = 65405;
    public static final int VC_SUN_CUT = 65403;
    public static final int VC_UNDEFINED = 0;
    public static final char CHAR_UNDEFINED = '\uffff';

    public NativeKeyEvent(int id, int modifiers, int rawCode, int keyCode, char keyChar, int keyLocation) {
        super(GlobalScreen.class, id, modifiers);
        this.rawCode = rawCode;
        this.keyCode = keyCode;
        this.keyChar = keyChar;
        this.keyLocation = keyLocation;
        if (id == 2400 && (keyChar == '\uffff' || keyCode != 0)) {
            throw new IllegalArgumentException();
        }
    }

    public NativeKeyEvent(int id, int modifiers, int rawCode, int keyCode, char keyChar) {
        this(id, modifiers, rawCode, keyCode, keyChar, 0);
    }

    public int getRawCode() {
        return this.rawCode;
    }

    public void setRawCode(int rawCode) {
        this.rawCode = rawCode;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public char getKeyChar() {
        return this.keyChar;
    }

    public void setKeyChar(char keyChar) {
        this.keyChar = keyChar;
    }

    public int getKeyLocation() {
        return this.keyLocation;
    }

    public static String getKeyText(int keyCode) {
        switch (keyCode) {
            case 1: {
                return Toolkit.getProperty("AWT.escape", "Escape");
            }
            case 59: {
                return Toolkit.getProperty("AWT.f1", "F1");
            }
            case 60: {
                return Toolkit.getProperty("AWT.f2", "F2");
            }
            case 61: {
                return Toolkit.getProperty("AWT.f3", "F3");
            }
            case 62: {
                return Toolkit.getProperty("AWT.f4", "F4");
            }
            case 63: {
                return Toolkit.getProperty("AWT.f5", "F5");
            }
            case 64: {
                return Toolkit.getProperty("AWT.f6", "F6");
            }
            case 65: {
                return Toolkit.getProperty("AWT.f7", "F7");
            }
            case 66: {
                return Toolkit.getProperty("AWT.f8", "F8");
            }
            case 67: {
                return Toolkit.getProperty("AWT.f9", "F9");
            }
            case 68: {
                return Toolkit.getProperty("AWT.f10", "F10");
            }
            case 87: {
                return Toolkit.getProperty("AWT.f11", "F11");
            }
            case 88: {
                return Toolkit.getProperty("AWT.f12", "F12");
            }
            case 91: {
                return Toolkit.getProperty("AWT.f13", "F13");
            }
            case 92: {
                return Toolkit.getProperty("AWT.f14", "F14");
            }
            case 93: {
                return Toolkit.getProperty("AWT.f15", "F15");
            }
            case 99: {
                return Toolkit.getProperty("AWT.f16", "F16");
            }
            case 100: {
                return Toolkit.getProperty("AWT.f17", "F17");
            }
            case 101: {
                return Toolkit.getProperty("AWT.f18", "F18");
            }
            case 102: {
                return Toolkit.getProperty("AWT.f19", "F19");
            }
            case 103: {
                return Toolkit.getProperty("AWT.f20", "F20");
            }
            case 104: {
                return Toolkit.getProperty("AWT.f21", "F21");
            }
            case 105: {
                return Toolkit.getProperty("AWT.f22", "F22");
            }
            case 106: {
                return Toolkit.getProperty("AWT.f23", "F23");
            }
            case 107: {
                return Toolkit.getProperty("AWT.f24", "F24");
            }
            case 41: {
                return Toolkit.getProperty("AWT.backQuote", "Back Quote");
            }
            case 2: {
                return "1";
            }
            case 3: {
                return "2";
            }
            case 4: {
                return "3";
            }
            case 5: {
                return "4";
            }
            case 6: {
                return "5";
            }
            case 7: {
                return "6";
            }
            case 8: {
                return "7";
            }
            case 9: {
                return "8";
            }
            case 10: {
                return "9";
            }
            case 11: {
                return "0";
            }
            case 12: {
                return Toolkit.getProperty("AWT.minus", "Minus");
            }
            case 13: {
                return Toolkit.getProperty("AWT.equals", "Equals");
            }
            case 14: {
                return Toolkit.getProperty("AWT.backSpace", "Backspace");
            }
            case 15: {
                return Toolkit.getProperty("AWT.tab", "Tab");
            }
            case 58: {
                return Toolkit.getProperty("AWT.capsLock", "Caps Lock");
            }
            case 30: {
                return "A";
            }
            case 48: {
                return "B";
            }
            case 46: {
                return "C";
            }
            case 32: {
                return "D";
            }
            case 18: {
                return "E";
            }
            case 33: {
                return "F";
            }
            case 34: {
                return "G";
            }
            case 35: {
                return "H";
            }
            case 23: {
                return "I";
            }
            case 36: {
                return "J";
            }
            case 37: {
                return "K";
            }
            case 38: {
                return "L";
            }
            case 50: {
                return "M";
            }
            case 49: {
                return "N";
            }
            case 24: {
                return "O";
            }
            case 25: {
                return "P";
            }
            case 16: {
                return "Q";
            }
            case 19: {
                return "R";
            }
            case 31: {
                return "S";
            }
            case 20: {
                return "T";
            }
            case 22: {
                return "U";
            }
            case 47: {
                return "V";
            }
            case 17: {
                return "W";
            }
            case 45: {
                return "X";
            }
            case 21: {
                return "Y";
            }
            case 44: {
                return "Z";
            }
            case 26: {
                return Toolkit.getProperty("AWT.openBracket", "Open Bracket");
            }
            case 27: {
                return Toolkit.getProperty("AWT.closeBracket", "Close Bracket");
            }
            case 43: {
                return Toolkit.getProperty("AWT.backSlash", "Back Slash");
            }
            case 39: {
                return Toolkit.getProperty("AWT.semicolon", "Semicolon");
            }
            case 40: {
                return Toolkit.getProperty("AWT.quote", "Quote");
            }
            case 28: {
                return Toolkit.getProperty("AWT.enter", "Enter");
            }
            case 51: {
                return Toolkit.getProperty("AWT.comma", "Comma");
            }
            case 52: {
                return Toolkit.getProperty("AWT.period", "Period");
            }
            case 53: {
                return Toolkit.getProperty("AWT.slash", "Slash");
            }
            case 57: {
                return Toolkit.getProperty("AWT.space", "Space");
            }
            case 3639: {
                return Toolkit.getProperty("AWT.printScreen", "Print Screen");
            }
            case 70: {
                return Toolkit.getProperty("AWT.scrollLock", "Scroll Lock");
            }
            case 3653: {
                return Toolkit.getProperty("AWT.pause", "Pause");
            }
            case 3666: {
                return Toolkit.getProperty("AWT.insert", "Insert");
            }
            case 3667: {
                return Toolkit.getProperty("AWT.delete", "Delete");
            }
            case 3655: {
                return Toolkit.getProperty("AWT.home", "Home");
            }
            case 3663: {
                return Toolkit.getProperty("AWT.end", "End");
            }
            case 3657: {
                return Toolkit.getProperty("AWT.pgup", "Page Up");
            }
            case 3665: {
                return Toolkit.getProperty("AWT.pgdn", "Page Down");
            }
            case 57416: {
                return Toolkit.getProperty("AWT.up", "Up");
            }
            case 57419: {
                return Toolkit.getProperty("AWT.left", "Left");
            }
            case 57420: {
                return Toolkit.getProperty("AWT.clear", "Clear");
            }
            case 57421: {
                return Toolkit.getProperty("AWT.right", "Right");
            }
            case 57424: {
                return Toolkit.getProperty("AWT.down", "Down");
            }
            case 69: {
                return Toolkit.getProperty("AWT.numLock", "Num Lock");
            }
            case 83: {
                return Toolkit.getProperty("AWT.separator", "NumPad ,");
            }
            case 42: {
                return Toolkit.getProperty("AWT.shift", "Shift");
            }
            case 29: {
                return Toolkit.getProperty("AWT.control", "Control");
            }
            case 56: {
                return Toolkit.getProperty("AWT.alt", "Alt");
            }
            case 3675: {
                return Toolkit.getProperty("AWT.meta", "Meta");
            }
            case 3677: {
                return Toolkit.getProperty("AWT.context", "Context Menu");
            }
            case 57438: {
                return Toolkit.getProperty("AWT.power", "Power");
            }
            case 57439: {
                return Toolkit.getProperty("AWT.sleep", "Sleep");
            }
            case 57443: {
                return Toolkit.getProperty("AWT.wake", "Wake");
            }
            case 57378: {
                return Toolkit.getProperty("AWT.play", "Play");
            }
            case 57380: {
                return Toolkit.getProperty("AWT.stop", "Stop");
            }
            case 57360: {
                return Toolkit.getProperty("AWT.previous", "Previous");
            }
            case 57369: {
                return Toolkit.getProperty("AWT.next", "Next");
            }
            case 57453: {
                return Toolkit.getProperty("AWT.select", "Select");
            }
            case 57388: {
                return Toolkit.getProperty("AWT.eject", "Eject");
            }
            case 57376: {
                return Toolkit.getProperty("AWT.mute", "Mute");
            }
            case 57392: {
                return Toolkit.getProperty("AWT.volup", "Volume Up");
            }
            case 57390: {
                return Toolkit.getProperty("AWT.voldn", "Volume Down");
            }
            case 57452: {
                return Toolkit.getProperty("AWT.app_mail", "App Mail");
            }
            case 57377: {
                return Toolkit.getProperty("AWT.app_calculator", "App Calculator");
            }
            case 57404: {
                return Toolkit.getProperty("AWT.app_music", "App Music");
            }
            case 57444: {
                return Toolkit.getProperty("AWT.app_pictures", "App Pictures");
            }
            case 57445: {
                return Toolkit.getProperty("AWT.search", "Browser Search");
            }
            case 57394: {
                return Toolkit.getProperty("AWT.homepage", "Browser Home");
            }
            case 57450: {
                return Toolkit.getProperty("AWT.back", "Browser Back");
            }
            case 57449: {
                return Toolkit.getProperty("AWT.forward", "Browser Forward");
            }
            case 57448: {
                return Toolkit.getProperty("AWT.stop", "Browser Stop");
            }
            case 57447: {
                return Toolkit.getProperty("AWT.refresh", "Browser Refresh");
            }
            case 57446: {
                return Toolkit.getProperty("AWT.favorites", "Browser Favorites");
            }
            case 112: {
                return Toolkit.getProperty("AWT.katakana", "Katakana");
            }
            case 115: {
                return Toolkit.getProperty("AWT.underscore", "Underscore");
            }
            case 119: {
                return Toolkit.getProperty("AWT.furigana", "Furigana");
            }
            case 121: {
                return Toolkit.getProperty("AWT.kanji", "Kanji");
            }
            case 123: {
                return Toolkit.getProperty("AWT.hiragana", "Hiragana");
            }
            case 125: {
                return Toolkit.getProperty("AWT.yen", Character.toString('\u00a5'));
            }
            case 65397: {
                return Toolkit.getProperty("AWT.sun_help", "Sun Help");
            }
            case 65400: {
                return Toolkit.getProperty("AWT.sun_stop", "Sun Stop");
            }
            case 65398: {
                return Toolkit.getProperty("AWT.sun_props", "Sun Props");
            }
            case 65399: {
                return Toolkit.getProperty("AWT.sun_front", "Sun Front");
            }
            case 65396: {
                return Toolkit.getProperty("AWT.sun_open", "Sun Open");
            }
            case 65406: {
                return Toolkit.getProperty("AWT.sun_find", "Sun Find");
            }
            case 65401: {
                return Toolkit.getProperty("AWT.sun_again", "Sun Again");
            }
            case 65402: {
                return Toolkit.getProperty("AWT.sun_undo", "Sun Undo");
            }
            case 65404: {
                return Toolkit.getProperty("AWT.sun_copy", "Sun Copy");
            }
            case 65405: {
                return Toolkit.getProperty("AWT.sun_insert", "Sun Insert");
            }
            case 65403: {
                return Toolkit.getProperty("AWT.sun_cut", "Sun Cut");
            }
            case 0: {
                return Toolkit.getProperty("AWT.undefined", "Undefined");
            }
        }
        return Toolkit.getProperty("AWT.unknown", "Unknown") + " keyCode: 0x" + Integer.toString(keyCode, 16);
    }

    public boolean isActionKey() {
        switch (this.keyCode) {
            case 29: 
            case 42: 
            case 56: 
            case 58: 
            case 59: 
            case 60: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 65: 
            case 66: 
            case 67: 
            case 68: 
            case 69: 
            case 70: 
            case 87: 
            case 88: 
            case 91: 
            case 92: 
            case 93: 
            case 99: 
            case 100: 
            case 101: 
            case 102: 
            case 103: 
            case 104: 
            case 105: 
            case 106: 
            case 107: 
            case 112: 
            case 119: 
            case 121: 
            case 123: 
            case 3639: 
            case 3655: 
            case 3657: 
            case 3663: 
            case 3665: 
            case 3666: 
            case 3675: 
            case 3677: 
            case 57360: 
            case 57369: 
            case 57376: 
            case 57377: 
            case 57378: 
            case 57380: 
            case 57388: 
            case 57390: 
            case 57392: 
            case 57394: 
            case 57404: 
            case 57416: 
            case 57419: 
            case 57420: 
            case 57421: 
            case 57424: 
            case 57438: 
            case 57439: 
            case 57443: 
            case 57444: 
            case 57445: 
            case 57446: 
            case 57447: 
            case 57448: 
            case 57449: 
            case 57450: 
            case 57452: 
            case 57453: 
            case 65396: 
            case 65397: 
            case 65398: 
            case 65399: 
            case 65400: 
            case 65401: 
            case 65402: 
            case 65403: 
            case 65404: 
            case 65405: 
            case 65406: {
                return true;
            }
        }
        return false;
    }

    @Override
    public String paramString() {
        StringBuilder param = new StringBuilder(255);
        switch (this.getID()) {
            case 2401: {
                param.append("NATIVE_KEY_PRESSED");
                break;
            }
            case 2402: {
                param.append("NATIVE_KEY_RELEASED");
                break;
            }
            case 2400: {
                param.append("NATIVE_KEY_TYPED");
                break;
            }
            default: {
                param.append("unknown type");
            }
        }
        param.append(',');
        param.append("keyCode=");
        param.append(this.keyCode);
        param.append(',');
        param.append("keyText=");
        param.append(NativeKeyEvent.getKeyText(this.keyCode));
        param.append(',');
        param.append("keyChar=");
        switch (this.keyChar) {
            case '\u0001': 
            case '\u000e': 
            case '\u000f': 
            case '\u001c': 
            case '\u0e53': {
                param.append(NativeKeyEvent.getKeyText(this.keyChar));
                break;
            }
            case '\uffff': {
                param.append(NativeKeyEvent.getKeyText(0));
                break;
            }
            default: {
                param.append('\'');
                param.append(this.keyChar);
                param.append('\'');
            }
        }
        param.append(',');
        if (this.getModifiers() != 0) {
            param.append("modifiers=");
            param.append(NativeKeyEvent.getModifiersText(this.getModifiers()));
            param.append(',');
        }
        param.append("keyLocation=");
        switch (this.keyLocation) {
            default: {
                param.append("KEY_LOCATION_UNKNOWN");
                break;
            }
            case 1: {
                param.append("KEY_LOCATION_STANDARD");
                break;
            }
            case 2: {
                param.append("KEY_LOCATION_LEFT");
                break;
            }
            case 3: {
                param.append("KEY_LOCATION_RIGHT");
                break;
            }
            case 4: {
                param.append("KEY_LOCATION_NUMPAD");
            }
        }
        param.append(',');
        param.append("rawCode=");
        param.append(this.rawCode);
        return param.toString();
    }
}

