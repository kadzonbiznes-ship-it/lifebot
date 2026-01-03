/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.NativeLibLoader;
import java.io.IOException;
import java.io.ObjectInputStream;
import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;

public non-sealed class MouseEvent
extends InputEvent {
    public static final int MOUSE_FIRST = 500;
    public static final int MOUSE_LAST = 507;
    public static final int MOUSE_CLICKED = 500;
    public static final int MOUSE_PRESSED = 501;
    public static final int MOUSE_RELEASED = 502;
    public static final int MOUSE_MOVED = 503;
    public static final int MOUSE_ENTERED = 504;
    public static final int MOUSE_EXITED = 505;
    public static final int MOUSE_DRAGGED = 506;
    public static final int MOUSE_WHEEL = 507;
    public static final int NOBUTTON = 0;
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;
    int x;
    int y;
    private int xAbs;
    private int yAbs;
    int clickCount;
    private boolean causedByTouchEvent;
    int button;
    boolean popupTrigger = false;
    private static final long serialVersionUID = -991214153494842848L;
    private static int cachedNumberOfButtons;
    private transient boolean shouldExcludeButtonFromExtModifiers = false;

    private static native void initIDs();

    public Point getLocationOnScreen() {
        return new Point(this.xAbs, this.yAbs);
    }

    public int getXOnScreen() {
        return this.xAbs;
    }

    public int getYOnScreen() {
        return this.yAbs;
    }

    public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int button) {
        this(source, id, when, modifiers, x, y, 0, 0, clickCount, popupTrigger, button);
        Point eventLocationOnScreen = new Point(0, 0);
        try {
            eventLocationOnScreen = source.getLocationOnScreen();
            this.xAbs = eventLocationOnScreen.x + x;
            this.yAbs = eventLocationOnScreen.y + y;
        }
        catch (IllegalComponentStateException e) {
            this.xAbs = 0;
            this.yAbs = 0;
        }
    }

    public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger) {
        this(source, id, when, modifiers, x, y, clickCount, popupTrigger, 0);
    }

    @Override
    public int getModifiersEx() {
        int tmpModifiers = this.modifiers;
        if (this.shouldExcludeButtonFromExtModifiers) {
            tmpModifiers &= ~InputEvent.getMaskForButton(this.getButton());
        }
        return tmpModifiers & 0xFFFFFFC0;
    }

    public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger, int button) {
        super(source, id, when, modifiers);
        this.x = x;
        this.y = y;
        this.xAbs = xAbs;
        this.yAbs = yAbs;
        this.clickCount = clickCount;
        this.popupTrigger = popupTrigger;
        if (button < 0) {
            throw new IllegalArgumentException("Invalid button value :" + button);
        }
        if (button > 3) {
            if (!Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled()) {
                throw new IllegalArgumentException("Extra mouse events are disabled " + button);
            }
            if (button > cachedNumberOfButtons) {
                throw new IllegalArgumentException("Nonexistent button " + button);
            }
            if (this.getModifiersEx() != 0 && (id == 502 || id == 500)) {
                this.shouldExcludeButtonFromExtModifiers = true;
            }
        }
        this.button = button;
        if (this.getModifiers() != 0 && this.getModifiersEx() == 0) {
            this.setNewModifiers();
        } else if (this.getModifiers() == 0 && (this.getModifiersEx() != 0 || button != 0) && button <= 3) {
            this.setOldModifiers();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Point getPoint() {
        int y;
        int x;
        MouseEvent mouseEvent = this;
        synchronized (mouseEvent) {
            x = this.x;
            y = this.y;
        }
        return new Point(x, y);
    }

    public synchronized void translatePoint(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public int getClickCount() {
        return this.clickCount;
    }

    public int getButton() {
        return this.button;
    }

    public boolean isPopupTrigger() {
        return this.popupTrigger;
    }

    public static String getMouseModifiersText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & 8) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append("+");
        }
        if ((modifiers & 4) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & 2) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & 1) != 0) {
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append("+");
        }
        if ((modifiers & 0x20) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append("+");
        }
        if ((modifiers & 0x10) != 0) {
            buf.append(Toolkit.getProperty("AWT.button1", "Button1"));
            buf.append("+");
        }
        if ((modifiers & 8) != 0) {
            buf.append(Toolkit.getProperty("AWT.button2", "Button2"));
            buf.append("+");
        }
        if ((modifiers & 4) != 0) {
            buf.append(Toolkit.getProperty("AWT.button3", "Button3"));
            buf.append("+");
        }
        for (int i = 1; i <= cachedNumberOfButtons; ++i) {
            int mask = InputEvent.getMaskForButton(i);
            if ((modifiers & mask) == 0 || buf.indexOf(Toolkit.getProperty("AWT.button" + i, "Button" + i)) != -1) continue;
            buf.append(Toolkit.getProperty("AWT.button" + i, "Button" + i));
            buf.append("+");
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    @Override
    public String paramString() {
        StringBuilder str = new StringBuilder(80);
        switch (this.id) {
            case 501: {
                str.append("MOUSE_PRESSED");
                break;
            }
            case 502: {
                str.append("MOUSE_RELEASED");
                break;
            }
            case 500: {
                str.append("MOUSE_CLICKED");
                break;
            }
            case 504: {
                str.append("MOUSE_ENTERED");
                break;
            }
            case 505: {
                str.append("MOUSE_EXITED");
                break;
            }
            case 503: {
                str.append("MOUSE_MOVED");
                break;
            }
            case 506: {
                str.append("MOUSE_DRAGGED");
                break;
            }
            case 507: {
                str.append("MOUSE_WHEEL");
                break;
            }
            default: {
                str.append("unknown type");
            }
        }
        str.append(",(").append(this.x).append(",").append(this.y).append(")");
        str.append(",absolute(").append(this.xAbs).append(",").append(this.yAbs).append(")");
        if (this.id != 506 && this.id != 503) {
            str.append(",button=").append(this.getButton());
        }
        if (this.getModifiers() != 0) {
            str.append(",modifiers=").append(MouseEvent.getMouseModifiersText(this.modifiers));
        }
        if (this.getModifiersEx() != 0) {
            str.append(",extModifiers=").append(MouseEvent.getModifiersExText(this.getModifiersEx()));
        }
        str.append(",clickCount=").append(this.clickCount);
        return str.toString();
    }

    private void setNewModifiers() {
        if ((this.modifiers & 0x10) != 0) {
            this.modifiers |= 0x400;
        }
        if ((this.modifiers & 8) != 0) {
            this.modifiers |= 0x800;
        }
        if ((this.modifiers & 4) != 0) {
            this.modifiers |= 0x1000;
        }
        if (this.id == 501 || this.id == 502 || this.id == 500) {
            if ((this.modifiers & 0x10) != 0) {
                this.button = 1;
                this.modifiers &= 0xFFFFFFF3;
                if (this.id != 501) {
                    this.modifiers &= 0xFFFFFBFF;
                }
            } else if ((this.modifiers & 8) != 0) {
                this.button = 2;
                this.modifiers &= 0xFFFFFFEB;
                if (this.id != 501) {
                    this.modifiers &= 0xFFFFF7FF;
                }
            } else if ((this.modifiers & 4) != 0) {
                this.button = 3;
                this.modifiers &= 0xFFFFFFE7;
                if (this.id != 501) {
                    this.modifiers &= 0xFFFFEFFF;
                }
            }
        }
        if ((this.modifiers & 8) != 0) {
            this.modifiers |= 0x200;
        }
        if ((this.modifiers & 4) != 0) {
            this.modifiers |= 0x100;
        }
        if ((this.modifiers & 1) != 0) {
            this.modifiers |= 0x40;
        }
        if ((this.modifiers & 2) != 0) {
            this.modifiers |= 0x80;
        }
        if ((this.modifiers & 0x20) != 0) {
            this.modifiers |= 0x2000;
        }
    }

    private void setOldModifiers() {
        if (this.id == 501 || this.id == 502 || this.id == 500) {
            switch (this.button) {
                case 1: {
                    this.modifiers |= 0x10;
                    break;
                }
                case 2: {
                    this.modifiers |= 8;
                    break;
                }
                case 3: {
                    this.modifiers |= 4;
                }
            }
        } else {
            if ((this.modifiers & 0x400) != 0) {
                this.modifiers |= 0x10;
            }
            if ((this.modifiers & 0x800) != 0) {
                this.modifiers |= 8;
            }
            if ((this.modifiers & 0x1000) != 0) {
                this.modifiers |= 4;
            }
        }
        if ((this.modifiers & 0x200) != 0) {
            this.modifiers |= 8;
        }
        if ((this.modifiers & 0x100) != 0) {
            this.modifiers |= 4;
        }
        if ((this.modifiers & 0x40) != 0) {
            this.modifiers |= 1;
        }
        if ((this.modifiers & 0x80) != 0) {
            this.modifiers |= 2;
        }
        if ((this.modifiers & 0x2000) != 0) {
            this.modifiers |= 0x20;
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.getModifiers() != 0 && this.getModifiersEx() == 0) {
            this.setNewModifiers();
        }
    }

    static {
        Toolkit tk;
        NativeLibLoader.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            MouseEvent.initIDs();
        }
        cachedNumberOfButtons = (tk = Toolkit.getDefaultToolkit()) instanceof SunToolkit ? ((SunToolkit)tk).getNumberOfButtons() : 3;
        AWTAccessor.setMouseEventAccessor(new AWTAccessor.MouseEventAccessor(){

            @Override
            public boolean isCausedByTouchEvent(MouseEvent ev) {
                return ev.causedByTouchEvent;
            }

            @Override
            public void setCausedByTouchEvent(MouseEvent ev, boolean causedByTouchEvent) {
                ev.causedByTouchEvent = causedByTouchEvent;
            }
        });
    }
}

