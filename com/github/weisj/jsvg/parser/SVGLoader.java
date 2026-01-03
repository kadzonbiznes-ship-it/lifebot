/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.DefaultParserProvider;
import com.github.weisj.jsvg.parser.NodeSupplier;
import com.github.weisj.jsvg.parser.ParserProvider;
import com.github.weisj.jsvg.parser.ResourceLoader;
import com.github.weisj.jsvg.parser.StaxSVGLoader;
import com.github.weisj.jsvg.parser.SynchronousResourceLoader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SVGLoader {
    static final Logger LOGGER = Logger.getLogger(SVGLoader.class.getName());
    @NotNull
    private static final NodeSupplier NODE_SUPPLIER = new NodeSupplier();
    private final StaxSVGLoader loader = new StaxSVGLoader(NODE_SUPPLIER);

    @Nullable
    public SVGDocument load(@NotNull URL xmlBase) {
        return this.load(xmlBase, (ParserProvider)new DefaultParserProvider());
    }

    @Nullable
    public SVGDocument load(@NotNull URL xmlBase, @NotNull ParserProvider parserProvider) {
        try {
            return this.load(xmlBase.openStream(), parserProvider);
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not read " + xmlBase, e);
            return null;
        }
    }

    @Nullable
    public SVGDocument load(@NotNull InputStream inputStream) {
        return this.load(inputStream, (ParserProvider)new DefaultParserProvider());
    }

    @Nullable
    public SVGDocument load(@NotNull InputStream inputStream, @NotNull ParserProvider parserProvider) {
        return this.load(inputStream, parserProvider, new SynchronousResourceLoader());
    }

    @Nullable
    public SVGDocument load(@NotNull InputStream inputStream, @NotNull ParserProvider parserProvider, @NotNull ResourceLoader resourceLoader) {
        try {
            return this.loader.load(this.createDocumentInputStream(inputStream), parserProvider, resourceLoader);
        }
        catch (Throwable e) {
            LOGGER.log(Level.WARNING, "Could not load SVG ", e);
            return null;
        }
    }

    @Nullable
    private InputStream createDocumentInputStream(@NotNull InputStream is) {
        try {
            BufferedInputStream bin = new BufferedInputStream(is);
            bin.mark(2);
            int b0 = bin.read();
            int b1 = bin.read();
            bin.reset();
            if ((b1 << 8 | b0) == 35615) {
                return new GZIPInputStream(bin);
            }
            return bin;
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
}

