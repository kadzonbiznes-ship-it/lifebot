/*
 * Decompiled with CFR 0.152.
 */
package sun.swing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.PrintGraphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterGraphics;
import java.beans.PropertyChangeEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import sun.awt.AWTAccessor;
import sun.awt.AWTPermissions;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.font.FontDesignMetrics;
import sun.font.FontUtilities;
import sun.java2d.SunGraphicsEnvironment;
import sun.print.ProxyPrintGraphics;
import sun.swing.ImageIconUIResource;
import sun.swing.PrintColorUIResource;
import sun.swing.StringUIClientPropertyKey;

public class SwingUtilities2 {
    public static final Object LAF_STATE_KEY = new StringBuffer("LookAndFeel State");
    public static final Object MENU_SELECTION_MANAGER_LISTENER_KEY = new StringBuffer("MenuSelectionManager listener key");
    private static LSBCacheEntry[] fontCache;
    private static final int CACHE_SIZE = 6;
    private static int nextIndex;
    private static LSBCacheEntry searchKey;
    private static final int MIN_CHAR_INDEX = 87;
    private static final int MAX_CHAR_INDEX = 88;
    public static final FontRenderContext DEFAULT_FRC;
    public static final String IMPLIED_CR = "CR";
    private static final StringBuilder SKIP_CLICK_COUNT;
    public static final StringUIClientPropertyKey BASICMENUITEMUI_MAX_TEXT_OFFSET;
    private static final String UntrustedClipboardAccess = "UNTRUSTED_CLIPBOARD_ACCESS_KEY";
    private static final int CHAR_BUFFER_SIZE = 100;
    private static final Object charsBufferLock;
    private static char[] charsBuffer;
    private static final Object APP_CONTEXT_FRC_CACHE_KEY;

    public static void putAATextInfo(boolean lafCondition, Map<Object, Object> map) {
        SunToolkit.setAAFontSettingsCondition(lafCondition);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Object desktopHints = tk.getDesktopProperty("awt.font.desktophints");
        if (desktopHints instanceof Map) {
            Map hints = (Map)desktopHints;
            Object aaHint = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (aaHint == null || aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF || aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {
                return;
            }
            map.put(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
            map.put(RenderingHints.KEY_TEXT_LCD_CONTRAST, hints.get(RenderingHints.KEY_TEXT_LCD_CONTRAST));
        }
    }

    private static int syncCharsBuffer(String s) {
        int length = s.length();
        if (charsBuffer == null || charsBuffer.length < length) {
            charsBuffer = s.toCharArray();
        } else {
            s.getChars(0, length, charsBuffer, 0);
        }
        return length;
    }

    public static final boolean isComplexLayout(char[] text, int start, int limit) {
        return FontUtilities.isComplexText(text, start, limit);
    }

    public static int getLeftSideBearing(JComponent c, FontMetrics fm, String string) {
        if (string == null || string.length() == 0) {
            return 0;
        }
        return SwingUtilities2.getLeftSideBearing(c, fm, string.charAt(0));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getLeftSideBearing(JComponent c, FontMetrics fm, char firstChar) {
        char charIndex = firstChar;
        if (charIndex < 'X' && charIndex >= 'W') {
            Object lsbs = null;
            FontRenderContext frc = SwingUtilities2.getFontRenderContext(c, fm);
            Font font = fm.getFont();
            Class<SwingUtilities2> clazz = SwingUtilities2.class;
            synchronized (SwingUtilities2.class) {
                LSBCacheEntry entry = null;
                if (searchKey == null) {
                    searchKey = new LSBCacheEntry(frc, font);
                } else {
                    searchKey.reset(frc, font);
                }
                for (LSBCacheEntry cacheEntry : fontCache) {
                    if (!searchKey.equals(cacheEntry)) continue;
                    entry = cacheEntry;
                    break;
                }
                if (entry == null) {
                    entry = searchKey;
                    SwingUtilities2.fontCache[SwingUtilities2.nextIndex] = searchKey;
                    searchKey = null;
                    nextIndex = (nextIndex + 1) % 6;
                }
                // ** MonitorExit[var7_7] (shouldn't be in output)
                return entry.getLeftSideBearing(firstChar);
            }
        }
        return 0;
    }

    public static FontMetrics getFontMetrics(JComponent c, Graphics g) {
        return SwingUtilities2.getFontMetrics(c, g, g.getFont());
    }

    public static FontMetrics getFontMetrics(JComponent c, Graphics g, Font font) {
        if (c != null) {
            return c.getFontMetrics(font);
        }
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    public static int stringWidth(JComponent c, FontMetrics fm, String string) {
        return (int)SwingUtilities2.stringWidth(c, fm, string, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static float stringWidth(JComponent c, FontMetrics fm, String string, boolean useFPAPI) {
        boolean needsTextLayout;
        if (string == null || string.isEmpty()) {
            return 0.0f;
        }
        boolean bl = needsTextLayout = c != null && c.getClientProperty(TextAttribute.NUMERIC_SHAPING) != null;
        if (needsTextLayout) {
            Object object = charsBufferLock;
            synchronized (object) {
                int length = SwingUtilities2.syncCharsBuffer(string);
                needsTextLayout = SwingUtilities2.isComplexLayout(charsBuffer, 0, length);
            }
        }
        if (needsTextLayout) {
            TextLayout layout = SwingUtilities2.createTextLayout(c, string, fm.getFont(), fm.getFontRenderContext());
            return layout.getAdvance();
        }
        return SwingUtilities2.getFontStringWidth(string, fm, useFPAPI);
    }

    public static String clipStringIfNecessary(JComponent c, FontMetrics fm, String string, int availTextWidth) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        int textWidth = SwingUtilities2.stringWidth(c, fm, string);
        if (textWidth > availTextWidth) {
            return SwingUtilities2.clipString(c, fm, string, availTextWidth);
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String clipString(JComponent c, FontMetrics fm, String string, int availTextWidth) {
        boolean needsTextLayout;
        String clipString = "...";
        if ((availTextWidth -= SwingUtilities2.stringWidth(c, fm, clipString)) <= 0) {
            return clipString;
        }
        Object object = charsBufferLock;
        synchronized (object) {
            int stringLength = SwingUtilities2.syncCharsBuffer(string);
            needsTextLayout = SwingUtilities2.isComplexLayout(charsBuffer, 0, stringLength);
            if (!needsTextLayout) {
                int width = 0;
                for (int nChars = 0; nChars < stringLength; ++nChars) {
                    if ((width += fm.charWidth(charsBuffer[nChars])) <= availTextWidth) continue;
                    string = string.substring(0, nChars);
                    break;
                }
            }
        }
        if (needsTextLayout) {
            AttributedString aString = new AttributedString(string);
            if (c != null) {
                aString.addAttribute(TextAttribute.NUMERIC_SHAPING, c.getClientProperty(TextAttribute.NUMERIC_SHAPING));
            }
            LineBreakMeasurer measurer = new LineBreakMeasurer(aString.getIterator(), BreakIterator.getCharacterInstance(), SwingUtilities2.getFontRenderContext(c, fm));
            string = string.substring(0, measurer.nextOffset(availTextWidth));
        }
        return string + clipString;
    }

    public static void drawString(JComponent c, Graphics g, String text, int x, int y) {
        SwingUtilities2.drawString(c, g, text, x, y, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawString(JComponent c, Graphics g, String text, float x, float y, boolean useFPAPI) {
        Graphics2D g2d;
        if (text == null || text.length() <= 0) {
            return;
        }
        if (SwingUtilities2.isPrinting(g) && (g2d = SwingUtilities2.getGraphics2D(g)) != null) {
            String trimmedText = text.stripTrailing();
            if (!trimmedText.isEmpty()) {
                Color col;
                float screenWidth = (float)g2d.getFont().getStringBounds(trimmedText, SwingUtilities2.getFontRenderContext(c)).getWidth();
                TextLayout layout = SwingUtilities2.createTextLayout(c, text, g2d.getFont(), g2d.getFontRenderContext());
                if ((float)SwingUtilities2.stringWidth(c, g2d.getFontMetrics(), trimmedText) > screenWidth) {
                    layout = layout.getJustifiedLayout(screenWidth);
                }
                if ((col = g2d.getColor()) instanceof PrintColorUIResource) {
                    g2d.setColor(((PrintColorUIResource)col).getPrintColor());
                }
                layout.draw(g2d, x, y);
                g2d.setColor(col);
            }
            return;
        }
        if (g instanceof Graphics2D) {
            Object aaHint;
            boolean needsTextLayout;
            Graphics2D g2 = (Graphics2D)g;
            boolean bl = needsTextLayout = c != null && c.getClientProperty(TextAttribute.NUMERIC_SHAPING) != null;
            if (needsTextLayout) {
                Object screenWidth = charsBufferLock;
                synchronized (screenWidth) {
                    int length = SwingUtilities2.syncCharsBuffer(text);
                    needsTextLayout = SwingUtilities2.isComplexLayout(charsBuffer, 0, length);
                }
            }
            Object object = aaHint = c == null ? null : c.getClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (aaHint != null) {
                Object oldContrast = null;
                Object oldAAValue = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
                if (aaHint != oldAAValue) {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
                } else {
                    oldAAValue = null;
                }
                Object lcdContrastHint = c.getClientProperty(RenderingHints.KEY_TEXT_LCD_CONTRAST);
                if (lcdContrastHint != null) {
                    oldContrast = g2.getRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST);
                    if (lcdContrastHint.equals(oldContrast)) {
                        oldContrast = null;
                    } else {
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, lcdContrastHint);
                    }
                }
                if (needsTextLayout) {
                    TextLayout layout = SwingUtilities2.createTextLayout(c, text, g2.getFont(), g2.getFontRenderContext());
                    layout.draw(g2, x, y);
                } else {
                    g2.drawString(text, x, y);
                }
                if (oldAAValue != null) {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAAValue);
                }
                if (oldContrast != null) {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, oldContrast);
                }
                return;
            }
            if (needsTextLayout) {
                TextLayout layout = SwingUtilities2.createTextLayout(c, text, g2.getFont(), g2.getFontRenderContext());
                layout.draw(g2, x, y);
                return;
            }
        }
        g.drawString(text, (int)x, (int)y);
    }

    public static void drawStringUnderlineCharAt(JComponent c, Graphics g, String text, int underlinedIndex, int x, int y) {
        SwingUtilities2.drawStringUnderlineCharAt(c, g, text, underlinedIndex, x, y, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawStringUnderlineCharAt(JComponent c, Graphics g, String text, int underlinedIndex, float x, float y, boolean useFPAPI) {
        if (text == null || text.length() <= 0) {
            return;
        }
        SwingUtilities2.drawString(c, g, text, x, y, useFPAPI);
        int textLength = text.length();
        if (underlinedIndex >= 0 && underlinedIndex < textLength) {
            float underlineRectY = y;
            int underlineRectHeight = 1;
            float underlineRectX = 0.0f;
            int underlineRectWidth = 0;
            boolean isPrinting = SwingUtilities2.isPrinting(g);
            boolean needsTextLayout = isPrinting;
            if (!needsTextLayout) {
                Object object = charsBufferLock;
                synchronized (object) {
                    SwingUtilities2.syncCharsBuffer(text);
                    needsTextLayout = SwingUtilities2.isComplexLayout(charsBuffer, 0, textLength);
                }
            }
            if (!needsTextLayout) {
                FontMetrics fm = g.getFontMetrics();
                underlineRectX = x + (float)SwingUtilities2.stringWidth(c, fm, text.substring(0, underlinedIndex));
                underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
            } else {
                Graphics2D g2d = SwingUtilities2.getGraphics2D(g);
                if (g2d != null) {
                    TextLayout layout = SwingUtilities2.createTextLayout(c, text, g2d.getFont(), g2d.getFontRenderContext());
                    if (isPrinting) {
                        float screenWidth = (float)g2d.getFont().getStringBounds(text, SwingUtilities2.getFontRenderContext(c)).getWidth();
                        if ((float)SwingUtilities2.stringWidth(c, g2d.getFontMetrics(), text) > screenWidth) {
                            layout = layout.getJustifiedLayout(screenWidth);
                        }
                    }
                    TextHitInfo leading = TextHitInfo.leading(underlinedIndex);
                    TextHitInfo trailing = TextHitInfo.trailing(underlinedIndex);
                    Shape shape = layout.getVisualHighlightShape(leading, trailing);
                    Rectangle rect = shape.getBounds();
                    underlineRectX = x + (float)rect.x;
                    underlineRectWidth = rect.width;
                }
            }
            g.fillRect((int)underlineRectX, (int)underlineRectY + 1, underlineRectWidth, underlineRectHeight);
        }
    }

    public static int loc2IndexFileList(JList<?> list, Point point) {
        Object bySize;
        int index = list.locationToIndex(point);
        if (index != -1 && (bySize = list.getClientProperty("List.isFileList")) instanceof Boolean && ((Boolean)bySize).booleanValue() && !SwingUtilities2.pointIsInActualBounds(list, index, point)) {
            index = -1;
        }
        return index;
    }

    private static <T> boolean pointIsInActualBounds(JList<T> list, int index, Point point) {
        ListCellRenderer<T> renderer = list.getCellRenderer();
        T value = list.getModel().getElementAt(index);
        Component item = renderer.getListCellRendererComponent(list, value, index, false, false);
        Dimension itemSize = item.getPreferredSize();
        Rectangle cellBounds = list.getCellBounds(index, index);
        if (!item.getComponentOrientation().isLeftToRight()) {
            cellBounds.x += cellBounds.width - itemSize.width;
        }
        cellBounds.width = itemSize.width;
        return cellBounds.contains(point);
    }

    public static boolean pointOutsidePrefSize(JTable table, int row, int column, Point p) {
        if (table.convertColumnIndexToModel(column) != 0 || row == -1) {
            return true;
        }
        TableCellRenderer tcr = table.getCellRenderer(row, column);
        Object value = table.getValueAt(row, column);
        Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, column);
        Dimension itemSize = cell.getPreferredSize();
        Rectangle cellBounds = table.getCellRect(row, column, false);
        cellBounds.width = itemSize.width;
        cellBounds.height = itemSize.height;
        assert (p.x >= cellBounds.x && p.y >= cellBounds.y);
        return p.x > cellBounds.x + cellBounds.width || p.y > cellBounds.y + cellBounds.height;
    }

    public static void setLeadAnchorWithoutSelection(ListSelectionModel model, int lead, int anchor) {
        if (anchor == -1) {
            anchor = lead;
        }
        if (lead == -1) {
            model.setAnchorSelectionIndex(-1);
            model.setLeadSelectionIndex(-1);
        } else {
            if (model.isSelectedIndex(lead)) {
                model.addSelectionInterval(lead, lead);
            } else {
                model.removeSelectionInterval(lead, lead);
            }
            model.setAnchorSelectionIndex(anchor);
        }
    }

    public static boolean shouldIgnore(MouseEvent me, JComponent c) {
        return c == null || !c.isEnabled() || !SwingUtilities.isLeftMouseButton(me) || me.isConsumed();
    }

    public static void adjustFocus(JComponent c) {
        if (!c.hasFocus() && c.isRequestFocusEnabled()) {
            c.requestFocus();
        }
    }

    public static int drawChars(JComponent c, Graphics g, char[] data, int offset, int length, int x, int y) {
        return (int)SwingUtilities2.drawChars(c, g, data, offset, length, x, y, false);
    }

    public static float drawChars(JComponent c, Graphics g, char[] data, int offset, int length, float x, float y) {
        return SwingUtilities2.drawChars(c, g, data, offset, length, x, y, true);
    }

    public static float drawChars(JComponent c, Graphics g, char[] data, int offset, int length, float x, float y, boolean useFPAPI) {
        Object aaHint;
        Graphics2D g2d;
        if (length <= 0) {
            return x;
        }
        float nextX = x + SwingUtilities2.getFontCharsWidth(data, offset, length, SwingUtilities2.getFontMetrics(c, g), useFPAPI);
        if (SwingUtilities2.isPrinting(g) && (g2d = SwingUtilities2.getGraphics2D(g)) != null) {
            FontRenderContext deviceFontRenderContext = g2d.getFontRenderContext();
            FontRenderContext frc = SwingUtilities2.getFontRenderContext(c);
            if (frc != null && !SwingUtilities2.isFontRenderContextPrintCompatible(deviceFontRenderContext, frc)) {
                String text = new String(data, offset, length);
                TextLayout layout = new TextLayout(text, g2d.getFont(), deviceFontRenderContext);
                String trimmedText = text.stripTrailing();
                if (!trimmedText.isEmpty()) {
                    Color col;
                    float screenWidth = (float)g2d.getFont().getStringBounds(trimmedText, frc).getWidth();
                    if ((float)SwingUtilities2.stringWidth(c, g2d.getFontMetrics(), trimmedText) > screenWidth) {
                        layout = layout.getJustifiedLayout(screenWidth);
                    }
                    if ((col = g2d.getColor()) instanceof PrintColorUIResource) {
                        g2d.setColor(((PrintColorUIResource)col).getPrintColor());
                    }
                    layout.draw(g2d, x, y);
                    g2d.setColor(col);
                }
                return nextX;
            }
        }
        Object object = aaHint = c == null ? null : c.getClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING);
        if (!(g instanceof Graphics2D)) {
            g.drawChars(data, offset, length, (int)x, (int)y);
            return nextX;
        }
        Graphics2D g2 = (Graphics2D)g;
        if (aaHint != null) {
            Object oldContrast = null;
            Object oldAAValue = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (aaHint != null && aaHint != oldAAValue) {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
            } else {
                oldAAValue = null;
            }
            Object lcdContrastHint = c.getClientProperty(RenderingHints.KEY_TEXT_LCD_CONTRAST);
            if (lcdContrastHint != null) {
                oldContrast = g2.getRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST);
                if (lcdContrastHint.equals(oldContrast)) {
                    oldContrast = null;
                } else {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, lcdContrastHint);
                }
            }
            g2.drawString(new String(data, offset, length), x, y);
            if (oldAAValue != null) {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAAValue);
            }
            if (oldContrast != null) {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, oldContrast);
            }
        } else {
            g2.drawString(new String(data, offset, length), x, y);
        }
        return nextX;
    }

    public static float getFontCharWidth(char c, FontMetrics fm, boolean useFPAPI) {
        return SwingUtilities2.getFontCharsWidth(new char[]{c}, 0, 1, fm, useFPAPI);
    }

    public static float getFontCharsWidth(char[] data, int offset, int len, FontMetrics fm, boolean useFPAPI) {
        if (len == 0) {
            return 0.0f;
        }
        if (useFPAPI) {
            Rectangle2D bounds = fm.getFont().getStringBounds(data, offset, offset + len, fm.getFontRenderContext());
            return (float)bounds.getWidth();
        }
        return fm.charsWidth(data, offset, len);
    }

    public static float getFontStringWidth(String data, FontMetrics fm, boolean useFPAPI) {
        if (useFPAPI) {
            Rectangle2D bounds = fm.getFont().getStringBounds(data, fm.getFontRenderContext());
            return (float)bounds.getWidth();
        }
        return fm.stringWidth(data);
    }

    public static float drawString(JComponent c, Graphics g, AttributedCharacterIterator iterator, int x, int y) {
        return SwingUtilities2.drawStringImpl(c, g, iterator, x, y);
    }

    public static float drawString(JComponent c, Graphics g, AttributedCharacterIterator iterator, float x, float y) {
        return SwingUtilities2.drawStringImpl(c, g, iterator, x, y);
    }

    private static float drawStringImpl(JComponent c, Graphics g, AttributedCharacterIterator iterator, float x, float y) {
        float retVal;
        Graphics2D g2d;
        boolean isPrinting = SwingUtilities2.isPrinting(g);
        Color col = g.getColor();
        if (isPrinting && col instanceof PrintColorUIResource) {
            g.setColor(((PrintColorUIResource)col).getPrintColor());
        }
        if ((g2d = SwingUtilities2.getGraphics2D(g)) == null) {
            g.drawString(iterator, (int)x, (int)y);
            retVal = x;
        } else {
            TextLayout layout;
            FontRenderContext frc;
            if (isPrinting) {
                frc = SwingUtilities2.getFontRenderContext(c);
                if (frc.isAntiAliased() || frc.usesFractionalMetrics()) {
                    frc = new FontRenderContext(frc.getTransform(), false, false);
                }
            } else {
                frc = SwingUtilities2.getFRCProperty(c);
                if (frc == null) {
                    frc = g2d.getFontRenderContext();
                }
            }
            if (isPrinting) {
                FontRenderContext deviceFRC = g2d.getFontRenderContext();
                if (!SwingUtilities2.isFontRenderContextPrintCompatible(frc, deviceFRC)) {
                    layout = new TextLayout(iterator, deviceFRC);
                    AttributedCharacterIterator trimmedIt = SwingUtilities2.getTrimmedTrailingSpacesIterator(iterator);
                    if (trimmedIt != null) {
                        float screenWidth = new TextLayout(trimmedIt, frc).getAdvance();
                        layout = layout.getJustifiedLayout(screenWidth);
                    }
                } else {
                    layout = new TextLayout(iterator, frc);
                }
            } else {
                layout = new TextLayout(iterator, frc);
            }
            layout.draw(g2d, x, y);
            retVal = layout.getAdvance();
        }
        if (isPrinting) {
            g.setColor(col);
        }
        return retVal;
    }

    public static void drawVLine(Graphics g, int x, int y1, int y2) {
        if (y2 < y1) {
            int temp = y2;
            y2 = y1;
            y1 = temp;
        }
        g.fillRect(x, y1, 1, y2 - y1 + 1);
    }

    public static void drawHLine(Graphics g, int x1, int x2, int y) {
        if (x2 < x1) {
            int temp = x2;
            x2 = x1;
            x1 = temp;
        }
        g.fillRect(x1, y, x2 - x1 + 1, 1);
    }

    public static void drawRect(Graphics g, int x, int y, int w, int h) {
        if (w < 0 || h < 0) {
            return;
        }
        if (h == 0 || w == 0) {
            g.fillRect(x, y, w + 1, h + 1);
        } else {
            g.fillRect(x, y, w, 1);
            g.fillRect(x + w, y, 1, h);
            g.fillRect(x + 1, y + h, w, 1);
            g.fillRect(x, y + 1, 1, h);
        }
    }

    private static TextLayout createTextLayout(JComponent c, String s, Font f, FontRenderContext frc) {
        Object shaper;
        Object object = shaper = c == null ? null : c.getClientProperty(TextAttribute.NUMERIC_SHAPING);
        if (shaper == null) {
            return new TextLayout(s, f, frc);
        }
        HashMap<TextAttribute, Object> a = new HashMap<TextAttribute, Object>();
        a.put(TextAttribute.FONT, f);
        a.put(TextAttribute.NUMERIC_SHAPING, shaper);
        return new TextLayout(s, a, frc);
    }

    private static boolean isFontRenderContextPrintCompatible(FontRenderContext frc1, FontRenderContext frc2) {
        if (frc1 == frc2) {
            return true;
        }
        if (frc1 == null || frc2 == null) {
            return false;
        }
        if (frc1.getFractionalMetricsHint() != frc2.getFractionalMetricsHint()) {
            return false;
        }
        if (!frc1.isTransformed() && !frc2.isTransformed()) {
            return true;
        }
        double[] mat1 = new double[4];
        double[] mat2 = new double[4];
        frc1.getTransform().getMatrix(mat1);
        frc2.getTransform().getMatrix(mat2);
        return mat1[0] == mat2[0] && mat1[1] == mat2[1] && mat1[2] == mat2[2] && mat1[3] == mat2[3];
    }

    public static Graphics2D getGraphics2D(Graphics g) {
        if (g instanceof Graphics2D) {
            return (Graphics2D)g;
        }
        if (g instanceof ProxyPrintGraphics) {
            return (Graphics2D)((ProxyPrintGraphics)g).getGraphics();
        }
        return null;
    }

    public static FontRenderContext getFontRenderContext(Component c) {
        assert (c != null);
        if (c == null) {
            return DEFAULT_FRC;
        }
        return c.getFontMetrics(c.getFont()).getFontRenderContext();
    }

    private static FontRenderContext getFontRenderContext(Component c, FontMetrics fm) {
        assert (fm != null || c != null);
        return fm != null ? fm.getFontRenderContext() : SwingUtilities2.getFontRenderContext(c);
    }

    public static FontMetrics getFontMetrics(JComponent c, Font font) {
        FontRenderContext frc = SwingUtilities2.getFRCProperty(c);
        if (frc == null) {
            frc = DEFAULT_FRC;
        }
        return FontDesignMetrics.getMetrics(font, frc);
    }

    private static FontRenderContext getFRCProperty(JComponent c) {
        if (c != null) {
            GraphicsConfiguration gc = c.getGraphicsConfiguration();
            AffineTransform tx = gc == null ? null : gc.getDefaultTransform();
            Object aaHint = c.getClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING);
            return SwingUtilities2.getFRCFromCache(tx, aaHint);
        }
        return null;
    }

    private static FontRenderContext getFRCFromCache(AffineTransform tx, Object aaHint) {
        Object key;
        FontRenderContext frc;
        if (tx == null && aaHint == null) {
            return null;
        }
        HashMap<Object, FontRenderContext> cache = (HashMap<Object, FontRenderContext>)AppContext.getAppContext().get(APP_CONTEXT_FRC_CACHE_KEY);
        if (cache == null) {
            cache = new HashMap<Object, FontRenderContext>();
            AppContext.getAppContext().put(APP_CONTEXT_FRC_CACHE_KEY, cache);
        }
        if ((frc = (FontRenderContext)cache.get(key = tx == null ? aaHint : (aaHint == null ? tx : new KeyPair(tx, aaHint)))) == null) {
            aaHint = aaHint == null ? RenderingHints.VALUE_TEXT_ANTIALIAS_OFF : aaHint;
            frc = new FontRenderContext(tx, aaHint, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
            cache.put(key, frc);
        }
        return frc;
    }

    static boolean isPrinting(Graphics g) {
        return g instanceof PrinterGraphics || g instanceof PrintGraphics;
    }

    private static AttributedCharacterIterator getTrimmedTrailingSpacesIterator(AttributedCharacterIterator iterator) {
        int curIdx = iterator.getIndex();
        char c = iterator.last();
        while (c != '\uffff' && Character.isWhitespace(c)) {
            c = iterator.previous();
        }
        if (c != '\uffff') {
            int endIdx = iterator.getIndex();
            if (endIdx == iterator.getEndIndex() - 1) {
                iterator.setIndex(curIdx);
                return iterator;
            }
            AttributedString trimmedText = new AttributedString(iterator, iterator.getBeginIndex(), endIdx + 1);
            return trimmedText.getIterator();
        }
        return null;
    }

    public static boolean useSelectedTextColor(Highlighter.Highlight h, JTextComponent c) {
        Highlighter.HighlightPainter painter = h.getPainter();
        String painterClass = painter.getClass().getName();
        if (painterClass.indexOf("javax.swing.text.DefaultHighlighter") != 0 && painterClass.indexOf("com.sun.java.swing.plaf.windows.WindowsTextUI") != 0) {
            return false;
        }
        try {
            DefaultHighlighter.DefaultHighlightPainter defPainter = (DefaultHighlighter.DefaultHighlightPainter)painter;
            if (defPainter.getColor() != null && !defPainter.getColor().equals(c.getSelectionColor())) {
                return false;
            }
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    public static boolean canAccessSystemClipboard() {
        boolean canAccess = false;
        if (!GraphicsEnvironment.isHeadless()) {
            SecurityManager sm = System.getSecurityManager();
            if (sm == null) {
                canAccess = true;
            } else {
                try {
                    sm.checkPermission(AWTPermissions.ACCESS_CLIPBOARD_PERMISSION);
                    canAccess = true;
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                if (canAccess && !SwingUtilities2.isTrustedContext()) {
                    canAccess = SwingUtilities2.canCurrentEventAccessSystemClipboard(true);
                }
            }
        }
        return canAccess;
    }

    public static boolean canCurrentEventAccessSystemClipboard() {
        return SwingUtilities2.isTrustedContext() || SwingUtilities2.canCurrentEventAccessSystemClipboard(false);
    }

    public static boolean canEventAccessSystemClipboard(AWTEvent e) {
        return SwingUtilities2.isTrustedContext() || SwingUtilities2.canEventAccessSystemClipboard(e, false);
    }

    private static boolean isAccessClipboardGesture(InputEvent ie) {
        boolean allowedGesture = false;
        if (ie instanceof KeyEvent) {
            KeyEvent ke = (KeyEvent)ie;
            int keyCode = ke.getKeyCode();
            int keyModifiers = ke.getModifiers();
            switch (keyCode) {
                case 67: 
                case 86: 
                case 88: {
                    allowedGesture = keyModifiers == 2;
                    break;
                }
                case 155: {
                    allowedGesture = keyModifiers == 2 || keyModifiers == 1;
                    break;
                }
                case 65485: 
                case 65487: 
                case 65489: {
                    allowedGesture = true;
                    break;
                }
                case 127: {
                    allowedGesture = keyModifiers == 1;
                }
            }
        }
        return allowedGesture;
    }

    private static boolean canEventAccessSystemClipboard(AWTEvent e, boolean checkGesture) {
        if (EventQueue.isDispatchThread()) {
            if (e instanceof InputEvent && (!checkGesture || SwingUtilities2.isAccessClipboardGesture((InputEvent)e))) {
                return AWTAccessor.getInputEventAccessor().canAccessSystemClipboard((InputEvent)e);
            }
            return false;
        }
        return true;
    }

    public static void checkAccess(int modifiers) {
        if (System.getSecurityManager() != null && !Modifier.isPublic(modifiers)) {
            throw new SecurityException("Resource is not accessible");
        }
    }

    private static boolean canCurrentEventAccessSystemClipboard(boolean checkGesture) {
        AWTEvent event = EventQueue.getCurrentEvent();
        return SwingUtilities2.canEventAccessSystemClipboard(event, checkGesture);
    }

    private static boolean isTrustedContext() {
        return System.getSecurityManager() == null || AppContext.getAppContext().get(UntrustedClipboardAccess) == null;
    }

    public static String displayPropertiesToCSS(Font font, Color fg) {
        StringBuilder rule = new StringBuilder("body {");
        if (font != null) {
            rule.append(" font-family: ");
            rule.append(font.getFamily());
            rule.append(" ; ");
            rule.append(" font-size: ");
            rule.append(font.getSize());
            rule.append("pt ;");
            if (font.isBold()) {
                rule.append(" font-weight: 700 ; ");
            }
            if (font.isItalic()) {
                rule.append(" font-style: italic ; ");
            }
        }
        if (fg != null) {
            rule.append(" color: #");
            if (fg.getRed() < 16) {
                rule.append('0');
            }
            rule.append(Integer.toHexString(fg.getRed()));
            if (fg.getGreen() < 16) {
                rule.append('0');
            }
            rule.append(Integer.toHexString(fg.getGreen()));
            if (fg.getBlue() < 16) {
                rule.append('0');
            }
            rule.append(Integer.toHexString(fg.getBlue()));
            rule.append(" ; ");
        }
        rule.append(" }");
        return rule.toString();
    }

    public static Object makeIcon(Class<?> baseClass, Class<?> rootClass, String imageFile) {
        return SwingUtilities2.makeIcon(baseClass, rootClass, imageFile, true);
    }

    public static Object makeIcon_Unprivileged(Class<?> baseClass, Class<?> rootClass, String imageFile) {
        return SwingUtilities2.makeIcon(baseClass, rootClass, imageFile, false);
    }

    private static Object makeIcon(Class<?> baseClass, Class<?> rootClass, String imageFile, boolean enablePrivileges) {
        return table -> {
            byte[] buffer;
            byte[] byArray = buffer = enablePrivileges ? AccessController.doPrivileged(() -> SwingUtilities2.getIconBytes(baseClass, rootClass, imageFile)) : SwingUtilities2.getIconBytes(baseClass, rootClass, imageFile);
            if (buffer == null) {
                return null;
            }
            if (buffer.length == 0) {
                System.err.println("warning: " + imageFile + " is zero-length");
                return null;
            }
            return new ImageIconUIResource(buffer);
        };
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static byte[] getIconBytes(Class<?> baseClass, Class<?> rootClass, String imageFile) {
        Class<?> srchClass = baseClass;
        while (srchClass != null) {
            try {
                InputStream resource = srchClass.getResourceAsStream(imageFile);
                try {
                    byte[] byArray;
                    if (resource == null) {
                        if (srchClass != rootClass) {
                            srchClass = srchClass.getSuperclass();
                            continue;
                        }
                        break;
                    }
                    try (BufferedInputStream in = new BufferedInputStream(resource);){
                        byArray = in.readAllBytes();
                    }
                    return byArray;
                }
                finally {
                    if (resource == null) continue;
                    resource.close();
                }
            }
            catch (IOException ioe) {
                System.err.println(ioe.toString());
            }
        }
        return null;
    }

    public static boolean isLocalDisplay() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        boolean isLocal = ge instanceof SunGraphicsEnvironment ? ((SunGraphicsEnvironment)ge).isDisplayLocal() : true;
        return isLocal;
    }

    public static int getUIDefaultsInt(Object key) {
        return SwingUtilities2.getUIDefaultsInt(key, 0);
    }

    public static int getUIDefaultsInt(Object key, Locale l) {
        return SwingUtilities2.getUIDefaultsInt(key, l, 0);
    }

    public static int getUIDefaultsInt(Object key, int defaultValue) {
        return SwingUtilities2.getUIDefaultsInt(key, null, defaultValue);
    }

    public static int getUIDefaultsInt(Object key, Locale l, int defaultValue) {
        Object value = UIManager.get(key, l);
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    public static Component compositeRequestFocus(Component component) {
        if (component instanceof Container) {
            FocusTraversalPolicy policy;
            Component comp;
            FocusTraversalPolicy policy2;
            Component comp2;
            Container container = (Container)component;
            if (container.isFocusCycleRoot() && (comp2 = (policy2 = container.getFocusTraversalPolicy()).getDefaultComponent(container)) != null) {
                comp2.requestFocus();
                return comp2;
            }
            Container rootAncestor = container.getFocusCycleRootAncestor();
            if (rootAncestor != null && (comp = (policy = rootAncestor.getFocusTraversalPolicy()).getComponentAfter(rootAncestor, container)) != null && SwingUtilities.isDescendingFrom(comp, container)) {
                comp.requestFocus();
                return comp;
            }
        }
        if (component.isFocusable()) {
            component.requestFocus();
            return component;
        }
        return null;
    }

    public static boolean tabbedPaneChangeFocusTo(Component comp) {
        if (comp != null) {
            if (comp.isFocusTraversable()) {
                SwingUtilities2.compositeRequestFocus(comp);
                return true;
            }
            if (comp instanceof JComponent && ((JComponent)comp).requestDefaultFocus()) {
                return true;
            }
        }
        return false;
    }

    public static <V> Future<V> submit(Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        FutureTask<V> future = new FutureTask<V>(task);
        SwingUtilities2.execute(future);
        return future;
    }

    public static <V> Future<V> submit(Runnable task, V result) {
        if (task == null) {
            throw new NullPointerException();
        }
        FutureTask<V> future = new FutureTask<V>(task, result);
        SwingUtilities2.execute(future);
        return future;
    }

    private static void execute(Runnable command) {
        SwingUtilities.invokeLater(command);
    }

    public static void setSkipClickCount(Component comp, int count) {
        if (comp instanceof JTextComponent && ((JTextComponent)comp).getCaret() instanceof DefaultCaret) {
            ((JTextComponent)comp).putClientProperty(SKIP_CLICK_COUNT, count);
        }
    }

    public static int getAdjustedClickCount(JTextComponent comp, MouseEvent e) {
        int cc = e.getClickCount();
        if (cc == 1) {
            comp.putClientProperty(SKIP_CLICK_COUNT, null);
        } else {
            Integer sub = (Integer)comp.getClientProperty(SKIP_CLICK_COUNT);
            if (sub != null) {
                return cc - sub;
            }
        }
        return cc;
    }

    private static Section liesIn(Rectangle rect, Point p, boolean horizontal, boolean ltr, boolean three) {
        boolean forward;
        int length;
        int pComp;
        int p0;
        if (horizontal) {
            p0 = rect.x;
            pComp = p.x;
            length = rect.width;
            forward = ltr;
        } else {
            p0 = rect.y;
            pComp = p.y;
            length = rect.height;
            forward = true;
        }
        if (three) {
            int boundary;
            int n = boundary = length >= 30 ? 10 : length / 3;
            if (pComp < p0 + boundary) {
                return forward ? Section.LEADING : Section.TRAILING;
            }
            if (pComp >= p0 + length - boundary) {
                return forward ? Section.TRAILING : Section.LEADING;
            }
            return Section.MIDDLE;
        }
        int middle = p0 + length / 2;
        if (forward) {
            return pComp >= middle ? Section.TRAILING : Section.LEADING;
        }
        return pComp < middle ? Section.TRAILING : Section.LEADING;
    }

    public static Section liesInHorizontal(Rectangle rect, Point p, boolean ltr, boolean three) {
        return SwingUtilities2.liesIn(rect, p, true, ltr, three);
    }

    public static Section liesInVertical(Rectangle rect, Point p, boolean three) {
        return SwingUtilities2.liesIn(rect, p, false, false, three);
    }

    public static int convertColumnIndexToModel(TableColumnModel cm, int viewColumnIndex) {
        if (viewColumnIndex < 0) {
            return viewColumnIndex;
        }
        return cm.getColumn(viewColumnIndex).getModelIndex();
    }

    public static int convertColumnIndexToView(TableColumnModel cm, int modelColumnIndex) {
        if (modelColumnIndex < 0) {
            return modelColumnIndex;
        }
        for (int column = 0; column < cm.getColumnCount(); ++column) {
            if (cm.getColumn(column).getModelIndex() != modelColumnIndex) continue;
            return column;
        }
        return -1;
    }

    public static int setAltGraphMask(int modifier) {
        return modifier | 0x2000;
    }

    public static int getSystemMnemonicKeyMask() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof SunToolkit) {
            return ((SunToolkit)toolkit).getFocusAcceleratorKeyMask();
        }
        return 8;
    }

    public static TreePath getTreePath(TreeModelEvent event, TreeModel model) {
        Object root;
        TreePath path = event.getTreePath();
        if (path == null && model != null && (root = model.getRoot()) != null) {
            path = new TreePath(root);
        }
        return path;
    }

    public static boolean isScaledGraphics(Graphics g) {
        if (g instanceof Graphics2D) {
            AffineTransform tx = ((Graphics2D)g).getTransform();
            return (tx.getType() & 0xFFFFFFBE) != 0;
        }
        return false;
    }

    public static Object getAndSetAntialisingHintForScaledGraphics(Graphics g) {
        if (SwingUtilities2.isScaledGraphics(g) && SwingUtilities2.isLocalDisplay()) {
            Graphics2D g2d = (Graphics2D)g;
            Object hint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            return hint;
        }
        return null;
    }

    public static void setAntialiasingHintForScaledGraphics(Graphics g, Object hint) {
        if (hint != null) {
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, hint);
        }
    }

    public static boolean isFloatingPointScale(AffineTransform tx) {
        int type = tx.getType() & 0xFFFFFFBE;
        if (type == 0) {
            return false;
        }
        if ((type & 0xFFFFFFF9) == 0) {
            double scaleX = tx.getScaleX();
            double scaleY = tx.getScaleY();
            return scaleX != (double)((int)scaleX) || scaleY != (double)((int)scaleY);
        }
        return false;
    }

    public static boolean getBoolean(JComponent component, String key) {
        Object clientProperty = component.getClientProperty(key);
        if (clientProperty instanceof Boolean) {
            return Boolean.TRUE.equals(clientProperty);
        }
        return UIManager.getBoolean(key);
    }

    public static boolean isScaleChanged(PropertyChangeEvent ev) {
        return SwingUtilities2.isScaleChanged(ev.getPropertyName(), ev.getOldValue(), ev.getNewValue());
    }

    public static boolean isScaleChanged(String name, Object oldValue, Object newValue) {
        if (oldValue == newValue || !"graphicsConfiguration".equals(name)) {
            return false;
        }
        GraphicsConfiguration newGC = (GraphicsConfiguration)oldValue;
        GraphicsConfiguration oldGC = (GraphicsConfiguration)newValue;
        AffineTransform newTx = newGC != null ? newGC.getDefaultTransform() : null;
        AffineTransform oldTx = oldGC != null ? oldGC.getDefaultTransform() : null;
        return !Objects.equals(newTx, oldTx);
    }

    static {
        DEFAULT_FRC = new FontRenderContext(null, false, false);
        SKIP_CLICK_COUNT = new StringBuilder("skipClickCount");
        BASICMENUITEMUI_MAX_TEXT_OFFSET = new StringUIClientPropertyKey("maxTextOffset");
        charsBufferLock = new Object();
        charsBuffer = new char[100];
        fontCache = new LSBCacheEntry[6];
        APP_CONTEXT_FRC_CACHE_KEY = new Object();
    }

    private static class LSBCacheEntry {
        private static final byte UNSET = 127;
        private static final char[] oneChar = new char[1];
        private byte[] lsbCache = new byte[1];
        private Font font;
        private FontRenderContext frc;

        public LSBCacheEntry(FontRenderContext frc, Font font) {
            this.reset(frc, font);
        }

        public void reset(FontRenderContext frc, Font font) {
            this.font = font;
            this.frc = frc;
            for (int counter = this.lsbCache.length - 1; counter >= 0; --counter) {
                this.lsbCache[counter] = 127;
            }
        }

        public int getLeftSideBearing(char aChar) {
            int index = aChar - 87;
            assert (index >= 0 && index < 1);
            byte lsb = this.lsbCache[index];
            if (lsb == 127) {
                Object aaHint;
                LSBCacheEntry.oneChar[0] = aChar;
                GlyphVector gv = this.font.createGlyphVector(this.frc, oneChar);
                lsb = (byte)gv.getGlyphPixelBounds((int)0, (FontRenderContext)this.frc, (float)0.0f, (float)0.0f).x;
                if (lsb < 0 && ((aaHint = this.frc.getAntiAliasingHint()) == RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB || aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR)) {
                    lsb = (byte)(lsb + 1);
                }
                this.lsbCache[index] = lsb;
            }
            return lsb;
        }

        public boolean equals(Object entry) {
            if (entry == this) {
                return true;
            }
            if (!(entry instanceof LSBCacheEntry)) {
                return false;
            }
            LSBCacheEntry oEntry = (LSBCacheEntry)entry;
            return this.font.equals(oEntry.font) && this.frc.equals(oEntry.frc);
        }

        public int hashCode() {
            int result = 17;
            if (this.font != null) {
                result = 37 * result + this.font.hashCode();
            }
            if (this.frc != null) {
                result = 37 * result + this.frc.hashCode();
            }
            return result;
        }
    }

    private static class KeyPair {
        private final Object key1;
        private final Object key2;

        public KeyPair(Object key1, Object key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof KeyPair)) {
                return false;
            }
            KeyPair that = (KeyPair)obj;
            return this.key1.equals(that.key1) && this.key2.equals(that.key2);
        }

        public int hashCode() {
            return this.key1.hashCode() + 37 * this.key2.hashCode();
        }
    }

    public static enum Section {
        LEADING,
        MIDDLE,
        TRAILING;

    }

    public static interface RepaintListener {
        public void repaintPerformed(JComponent var1, int var2, int var3, int var4, int var5);
    }
}

