/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InvocationEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodHighlight;
import java.awt.im.InputSubset;
import java.awt.im.spi.InputMethodContext;
import java.awt.peer.LightweightPeer;
import java.io.Serializable;
import java.text.Annotation;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import sun.awt.AWTAccessor;
import sun.awt.im.InputMethodAdapter;
import sun.awt.windows.WComponentPeer;
import sun.awt.windows.WInputMethodDescriptor;
import sun.awt.windows.WToolkit;

final class WInputMethod
extends InputMethodAdapter {
    private InputMethodContext inputContext;
    private Component awtFocussedComponent;
    private WComponentPeer awtFocussedComponentPeer = null;
    private WComponentPeer lastFocussedComponentPeer = null;
    private boolean isLastFocussedActiveClient = false;
    private boolean isActive;
    private int context = this.createNativeContext();
    private boolean open;
    private int cmode = this.getConversionStatus(this.context);
    private Locale currentLocale;
    private boolean statusWindowHidden = false;
    private boolean hasCompositionString = false;
    public static final byte ATTR_INPUT = 0;
    public static final byte ATTR_TARGET_CONVERTED = 1;
    public static final byte ATTR_CONVERTED = 2;
    public static final byte ATTR_TARGET_NOTCONVERTED = 3;
    public static final byte ATTR_INPUT_ERROR = 4;
    public static final int IME_CMODE_ALPHANUMERIC = 0;
    public static final int IME_CMODE_NATIVE = 1;
    public static final int IME_CMODE_KATAKANA = 2;
    public static final int IME_CMODE_LANGUAGE = 3;
    public static final int IME_CMODE_FULLSHAPE = 8;
    public static final int IME_CMODE_HANJACONVERT = 64;
    public static final int IME_CMODE_ROMAN = 16;
    private static final boolean COMMIT_INPUT = true;
    private static final boolean DISCARD_INPUT = false;
    private static Map<TextAttribute, Object>[] highlightStyles;

    public WInputMethod() {
        this.open = this.getOpenStatus(this.context);
        this.currentLocale = WInputMethod.getNativeLocale();
        if (this.currentLocale == null) {
            this.currentLocale = Locale.getDefault();
        }
    }

    protected void finalize() throws Throwable {
        if (this.context != 0) {
            this.destroyNativeContext(this.context);
            this.context = 0;
        }
        super.finalize();
    }

    @Override
    public synchronized void setInputMethodContext(InputMethodContext context) {
        this.inputContext = context;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object getControlObject() {
        return null;
    }

    @Override
    public boolean setLocale(Locale lang) {
        return this.setLocale(lang, false);
    }

    private boolean setLocale(Locale lang, boolean onActivate) {
        Locale[] available = WInputMethodDescriptor.getAvailableLocalesInternal();
        for (int i = 0; i < available.length; ++i) {
            Locale locale = available[i];
            if (!lang.equals(locale) && (!locale.equals(Locale.JAPAN) || !lang.equals(Locale.JAPANESE)) && (!locale.equals(Locale.KOREA) || !lang.equals(Locale.KOREAN))) continue;
            if (this.isActive) {
                WInputMethod.setNativeLocale(locale.toLanguageTag(), onActivate);
            }
            this.currentLocale = locale;
            return true;
        }
        return false;
    }

    @Override
    public Locale getLocale() {
        if (this.isActive) {
            this.currentLocale = WInputMethod.getNativeLocale();
            if (this.currentLocale == null) {
                this.currentLocale = Locale.getDefault();
            }
        }
        return this.currentLocale;
    }

    @Override
    public void setCharacterSubsets(Character.Subset[] subsets) {
        if (subsets == null) {
            this.setConversionStatus(this.context, this.cmode);
            this.setOpenStatus(this.context, this.open);
            return;
        }
        Character.Subset subset1 = subsets[0];
        Locale locale = WInputMethod.getNativeLocale();
        if (locale == null) {
            return;
        }
        if (locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            if (subset1 == Character.UnicodeBlock.BASIC_LATIN || subset1 == InputSubset.LATIN_DIGITS) {
                this.setOpenStatus(this.context, false);
            } else {
                if (subset1 == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || subset1 == InputSubset.KANJI || subset1 == Character.UnicodeBlock.HIRAGANA) {
                    newmode = 9;
                } else if (subset1 == Character.UnicodeBlock.KATAKANA) {
                    newmode = 11;
                } else if (subset1 == InputSubset.HALFWIDTH_KATAKANA) {
                    newmode = 3;
                } else if (subset1 == InputSubset.FULLWIDTH_LATIN) {
                    newmode = 8;
                } else {
                    return;
                }
                this.setOpenStatus(this.context, true);
                this.setConversionStatus(this.context, newmode |= this.getConversionStatus(this.context) & 0x10);
            }
        } else if (locale.getLanguage().equals(Locale.KOREAN.getLanguage())) {
            if (subset1 == Character.UnicodeBlock.BASIC_LATIN || subset1 == InputSubset.LATIN_DIGITS) {
                this.setOpenStatus(this.context, false);
                this.setConversionStatus(this.context, 0);
            } else {
                int newmode;
                if (subset1 == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || subset1 == InputSubset.HANJA || subset1 == Character.UnicodeBlock.HANGUL_SYLLABLES || subset1 == Character.UnicodeBlock.HANGUL_JAMO || subset1 == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                    newmode = 1;
                } else if (subset1 == InputSubset.FULLWIDTH_LATIN) {
                    newmode = 8;
                } else {
                    return;
                }
                this.setOpenStatus(this.context, true);
                this.setConversionStatus(this.context, newmode);
            }
        } else if (locale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
            if (subset1 == Character.UnicodeBlock.BASIC_LATIN || subset1 == InputSubset.LATIN_DIGITS) {
                this.setOpenStatus(this.context, false);
                int newmode = this.getConversionStatus(this.context);
                this.setConversionStatus(this.context, newmode &= 0xFFFFFFF7);
            } else {
                int newmode;
                if (subset1 == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || subset1 == InputSubset.TRADITIONAL_HANZI || subset1 == InputSubset.SIMPLIFIED_HANZI) {
                    newmode = 9;
                } else if (subset1 == InputSubset.FULLWIDTH_LATIN) {
                    newmode = 8;
                } else {
                    return;
                }
                this.setOpenStatus(this.context, true);
                this.setConversionStatus(this.context, newmode);
            }
        }
    }

    @Override
    public void dispatchEvent(AWTEvent e) {
        Component comp;
        if (e instanceof ComponentEvent && (comp = ((ComponentEvent)e).getComponent()) == this.awtFocussedComponent) {
            if (this.awtFocussedComponentPeer == null || this.awtFocussedComponentPeer.isDisposed()) {
                this.awtFocussedComponentPeer = this.getNearestNativePeer(comp);
            }
            if (this.awtFocussedComponentPeer != null) {
                this.handleNativeIMEEvent(this.awtFocussedComponentPeer, e);
            }
        }
    }

    @Override
    public void activate() {
        boolean isAc = this.haveActiveClient();
        if (this.lastFocussedComponentPeer != this.awtFocussedComponentPeer || this.isLastFocussedActiveClient != isAc) {
            if (this.lastFocussedComponentPeer != null) {
                this.disableNativeIME(this.lastFocussedComponentPeer);
            }
            if (this.awtFocussedComponentPeer != null) {
                this.enableNativeIME(this.awtFocussedComponentPeer, this.context, !isAc);
            }
            this.lastFocussedComponentPeer = this.awtFocussedComponentPeer;
            this.isLastFocussedActiveClient = isAc;
        }
        this.isActive = true;
        if (this.currentLocale != null) {
            this.setLocale(this.currentLocale, true);
        }
        if (this.hasCompositionString && !this.isCompositionStringAvailable(this.context)) {
            this.endCompositionNative(this.context, false);
            this.sendInputMethodEvent(1100, EventQueue.getMostRecentEventTime(), null, null, null, null, null, 0, 0, 0);
            this.hasCompositionString = false;
        }
        if (this.statusWindowHidden) {
            this.setStatusWindowVisible(this.awtFocussedComponentPeer, true);
            this.statusWindowHidden = false;
        }
    }

    @Override
    public void deactivate(boolean isTemporary) {
        this.getLocale();
        if (this.awtFocussedComponentPeer != null) {
            this.lastFocussedComponentPeer = this.awtFocussedComponentPeer;
            this.isLastFocussedActiveClient = this.haveActiveClient();
        }
        this.isActive = false;
        this.hasCompositionString = this.isCompositionStringAvailable(this.context);
        if (this.hasCompositionString) {
            this.endComposition();
        }
    }

    @Override
    public void disableInputMethod() {
        if (this.lastFocussedComponentPeer != null) {
            this.disableNativeIME(this.lastFocussedComponentPeer);
            this.lastFocussedComponentPeer = null;
            this.isLastFocussedActiveClient = false;
        }
    }

    @Override
    public String getNativeInputMethodInfo() {
        return this.getNativeIMMDescription();
    }

    @Override
    protected void stopListening() {
        this.disableInputMethod();
    }

    @Override
    protected void setAWTFocussedComponent(Component component) {
        if (component == null) {
            return;
        }
        WComponentPeer peer = this.getNearestNativePeer(component);
        if (this.isActive) {
            if (this.awtFocussedComponentPeer != null) {
                this.disableNativeIME(this.awtFocussedComponentPeer);
            }
            if (peer != null) {
                this.enableNativeIME(peer, this.context, !this.haveActiveClient());
            }
        }
        this.awtFocussedComponent = component;
        this.awtFocussedComponentPeer = peer;
    }

    @Override
    public void hideWindows() {
        if (this.awtFocussedComponentPeer != null) {
            this.setStatusWindowVisible(this.awtFocussedComponentPeer, false);
            this.statusWindowHidden = true;
        }
    }

    @Override
    public void removeNotify() {
        this.endCompositionNative(this.context, false);
        this.awtFocussedComponent = null;
        this.awtFocussedComponentPeer = null;
    }

    static Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) {
        int index;
        int state = highlight.getState();
        if (state == 0) {
            index = 0;
        } else if (state == 1) {
            index = 2;
        } else {
            return null;
        }
        if (highlight.isSelected()) {
            ++index;
        }
        return highlightStyles[index];
    }

    @Override
    protected boolean supportsBelowTheSpot() {
        return true;
    }

    @Override
    public void endComposition() {
        this.endCompositionNative(this.context, this.haveActiveClient());
    }

    @Override
    public void setCompositionEnabled(boolean enable) {
        this.setOpenStatus(this.context, enable);
    }

    @Override
    public boolean isCompositionEnabled() {
        return this.getOpenStatus(this.context);
    }

    public void sendInputMethodEvent(int id, long when, String text, int[] clauseBoundary, String[] clauseReading, int[] attributeBoundary, byte[] attributeValue, int commitedTextLength, int caretPos, int visiblePos) {
        Component source;
        AttributedCharacterIterator iterator = null;
        if (text != null) {
            int i;
            AttributedString attrStr = new AttributedString(text);
            attrStr.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, Locale.getDefault(), 0, text.length());
            if (clauseBoundary != null && clauseReading != null && clauseReading.length != 0 && clauseBoundary.length == clauseReading.length + 1 && clauseBoundary[0] == 0 && clauseBoundary[clauseReading.length] <= text.length()) {
                for (i = 0; i < clauseBoundary.length - 1; ++i) {
                    attrStr.addAttribute(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT, new Annotation(null), clauseBoundary[i], clauseBoundary[i + 1]);
                    attrStr.addAttribute(AttributedCharacterIterator.Attribute.READING, new Annotation(clauseReading[i]), clauseBoundary[i], clauseBoundary[i + 1]);
                }
            } else {
                attrStr.addAttribute(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT, new Annotation(null), 0, text.length());
                attrStr.addAttribute(AttributedCharacterIterator.Attribute.READING, new Annotation(""), 0, text.length());
            }
            if (attributeBoundary != null && attributeValue != null && attributeValue.length != 0 && attributeBoundary.length == attributeValue.length + 1 && attributeBoundary[0] == 0 && attributeBoundary[attributeValue.length] == text.length()) {
                for (i = 0; i < attributeBoundary.length - 1; ++i) {
                    attrStr.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, switch (attributeValue[i]) {
                        case 1 -> InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
                        case 2 -> InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
                        case 3 -> InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
                        default -> InputMethodHighlight.UNSELECTED_RAW_TEXT_HIGHLIGHT;
                    }, attributeBoundary[i], attributeBoundary[i + 1]);
                }
            } else {
                attrStr.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT, 0, text.length());
            }
            iterator = attrStr.getIterator();
        }
        if ((source = this.getClientComponent()) == null) {
            return;
        }
        InputMethodEvent event = new InputMethodEvent(source, id, when, iterator, commitedTextLength, TextHitInfo.leading(caretPos), TextHitInfo.leading(visiblePos));
        WToolkit.postEvent(WToolkit.targetToAppContext(source), event);
    }

    public void inquireCandidatePosition() {
        Component source = this.getClientComponent();
        if (source == null) {
            return;
        }
        Runnable r = new Runnable(){

            @Override
            public void run() {
                int x = 0;
                int y = 0;
                Component client = WInputMethod.this.getClientComponent();
                if (client != null) {
                    if (!client.isShowing()) {
                        return;
                    }
                    if (WInputMethod.this.haveActiveClient()) {
                        Rectangle rc = WInputMethod.this.inputContext.getTextLocation(TextHitInfo.leading(0));
                        x = rc.x;
                        y = rc.y + rc.height;
                    } else {
                        Point pt = client.getLocationOnScreen();
                        Dimension size = client.getSize();
                        x = pt.x;
                        y = pt.y + size.height;
                    }
                }
                WInputMethod.this.openCandidateWindow(WInputMethod.this.awtFocussedComponentPeer, x, y);
            }
        };
        WToolkit.postEvent(WToolkit.targetToAppContext(source), new InvocationEvent((Object)source, r));
    }

    private WComponentPeer getNearestNativePeer(Component comp) {
        if (comp == null) {
            return null;
        }
        AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
        Object peer = acc.getPeer(comp);
        if (peer == null) {
            return null;
        }
        while (peer instanceof LightweightPeer) {
            if ((comp = comp.getParent()) == null) {
                return null;
            }
            peer = acc.getPeer(comp);
            if (peer != null) continue;
            return null;
        }
        if (peer instanceof WComponentPeer) {
            return (WComponentPeer)peer;
        }
        return null;
    }

    private native int createNativeContext();

    private native void destroyNativeContext(int var1);

    private native void enableNativeIME(WComponentPeer var1, int var2, boolean var3);

    private native void disableNativeIME(WComponentPeer var1);

    private native void handleNativeIMEEvent(WComponentPeer var1, AWTEvent var2);

    private native void endCompositionNative(int var1, boolean var2);

    private native void setConversionStatus(int var1, int var2);

    private native int getConversionStatus(int var1);

    private native void setOpenStatus(int var1, boolean var2);

    private native boolean getOpenStatus(int var1);

    private native void setStatusWindowVisible(WComponentPeer var1, boolean var2);

    private native String getNativeIMMDescription();

    static native Locale getNativeLocale();

    static native boolean setNativeLocale(String var0, boolean var1);

    private native void openCandidateWindow(WComponentPeer var1, int var2, int var3);

    private native boolean isCompositionStringAvailable(int var1);

    static {
        Map[] styles = new Map[4];
        HashMap<TextAttribute, Serializable> map = new HashMap<TextAttribute, Serializable>(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
        styles[0] = Collections.unmodifiableMap(map);
        map = new HashMap(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_GRAY);
        styles[1] = Collections.unmodifiableMap(map);
        map = new HashMap(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
        styles[2] = Collections.unmodifiableMap(map);
        map = new HashMap(4);
        Color navyBlue = new Color(0, 0, 128);
        map.put(TextAttribute.FOREGROUND, navyBlue);
        map.put(TextAttribute.BACKGROUND, Color.white);
        map.put(TextAttribute.SWAP_COLORS, TextAttribute.SWAP_COLORS_ON);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        styles[3] = Collections.unmodifiableMap(map);
        highlightStyles = styles;
    }
}

