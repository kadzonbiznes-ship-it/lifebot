/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.metal;

import java.awt.Font;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalFontDesktopProperty;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import sun.awt.AppContext;
import sun.security.action.GetPropertyAction;
import sun.swing.SwingUtilities2;

public class DefaultMetalTheme
extends MetalTheme {
    private static final boolean PLAIN_FONTS;
    private static final String[] fontNames;
    private static final int[] fontStyles;
    private static final int[] fontSizes;
    private static final String[] defaultNames;
    private static final ColorUIResource primary1;
    private static final ColorUIResource primary2;
    private static final ColorUIResource primary3;
    private static final ColorUIResource secondary1;
    private static final ColorUIResource secondary2;
    private static final ColorUIResource secondary3;
    private FontDelegate fontDelegate;

    static String getDefaultFontName(int key) {
        return fontNames[key];
    }

    static int getDefaultFontSize(int key) {
        return fontSizes[key];
    }

    static int getDefaultFontStyle(int key) {
        if (key != 4) {
            Object boldMetal = null;
            if (AppContext.getAppContext().get(SwingUtilities2.LAF_STATE_KEY) != null) {
                boldMetal = UIManager.get("swing.boldMetal");
            }
            if (boldMetal != null ? Boolean.FALSE.equals(boldMetal) : PLAIN_FONTS) {
                return 0;
            }
        }
        return fontStyles[key];
    }

    static String getDefaultPropertyName(int key) {
        return defaultNames[key];
    }

    @Override
    public String getName() {
        return "Steel";
    }

    public DefaultMetalTheme() {
        this.install();
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return primary3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return secondary3;
    }

    @Override
    public FontUIResource getControlTextFont() {
        return this.getFont(0);
    }

    @Override
    public FontUIResource getSystemTextFont() {
        return this.getFont(1);
    }

    @Override
    public FontUIResource getUserTextFont() {
        return this.getFont(2);
    }

    @Override
    public FontUIResource getMenuTextFont() {
        return this.getFont(3);
    }

    @Override
    public FontUIResource getWindowTitleFont() {
        return this.getFont(4);
    }

    @Override
    public FontUIResource getSubTextFont() {
        return this.getFont(5);
    }

    private FontUIResource getFont(int key) {
        return this.fontDelegate.getFont(key);
    }

    @Override
    void install() {
        this.fontDelegate = MetalLookAndFeel.isWindows() && MetalLookAndFeel.useSystemFonts() ? new WindowsFontDelegate() : new FontDelegate();
    }

    @Override
    boolean isSystemTheme() {
        return this.getClass() == DefaultMetalTheme.class;
    }

    static {
        fontNames = new String[]{"Dialog", "Dialog", "Dialog", "Dialog", "Dialog", "Dialog"};
        fontStyles = new int[]{1, 0, 0, 1, 1, 0};
        fontSizes = new int[]{12, 12, 12, 12, 12, 10};
        defaultNames = new String[]{"swing.plaf.metal.controlFont", "swing.plaf.metal.systemFont", "swing.plaf.metal.userFont", "swing.plaf.metal.controlFont", "swing.plaf.metal.controlFont", "swing.plaf.metal.smallFont"};
        String boldProperty = AccessController.doPrivileged(new GetPropertyAction("swing.boldMetal"));
        PLAIN_FONTS = boldProperty != null && "false".equals(boldProperty);
        primary1 = new ColorUIResource(102, 102, 153);
        primary2 = new ColorUIResource(153, 153, 204);
        primary3 = new ColorUIResource(204, 204, 255);
        secondary1 = new ColorUIResource(102, 102, 102);
        secondary2 = new ColorUIResource(153, 153, 153);
        secondary3 = new ColorUIResource(204, 204, 204);
    }

    private static class FontDelegate {
        private static int[] defaultMapping = new int[]{0, 1, 2, 0, 0, 5};
        FontUIResource[] fonts = new FontUIResource[6];

        public FontUIResource getFont(int type) {
            int mappedType = defaultMapping[type];
            if (this.fonts[type] == null) {
                Font f = this.getPrivilegedFont(mappedType);
                if (f == null) {
                    f = new Font(DefaultMetalTheme.getDefaultFontName(type), DefaultMetalTheme.getDefaultFontStyle(type), DefaultMetalTheme.getDefaultFontSize(type));
                }
                this.fonts[type] = new FontUIResource(f);
            }
            return this.fonts[type];
        }

        protected Font getPrivilegedFont(final int key) {
            return AccessController.doPrivileged(new PrivilegedAction<Font>(){

                @Override
                public Font run() {
                    return Font.getFont(DefaultMetalTheme.getDefaultPropertyName(key));
                }
            });
        }
    }

    private static class WindowsFontDelegate
    extends FontDelegate {
        private MetalFontDesktopProperty[] props = new MetalFontDesktopProperty[6];
        private boolean[] checkedPrivileged = new boolean[6];

        @Override
        public FontUIResource getFont(int type) {
            if (this.fonts[type] != null) {
                return this.fonts[type];
            }
            if (!this.checkedPrivileged[type]) {
                Font f = this.getPrivilegedFont(type);
                this.checkedPrivileged[type] = true;
                if (f != null) {
                    this.fonts[type] = new FontUIResource(f);
                    return this.fonts[type];
                }
            }
            if (this.props[type] == null) {
                this.props[type] = new MetalFontDesktopProperty(type);
            }
            return (FontUIResource)this.props[type].createValue(null);
        }
    }
}

