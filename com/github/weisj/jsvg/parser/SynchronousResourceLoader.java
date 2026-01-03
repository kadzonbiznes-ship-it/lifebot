/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.parser.ValueUIFuture
 *  com.github.weisj.jsvg.util.ResourceUtil
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.parser.ResourceLoader;
import com.github.weisj.jsvg.parser.UIFuture;
import com.github.weisj.jsvg.parser.ValueUIFuture;
import com.github.weisj.jsvg.util.ResourceUtil;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public final class SynchronousResourceLoader
implements ResourceLoader {
    @Override
    @NotNull
    public UIFuture<BufferedImage> loadImage(@NotNull URI uri) throws IOException {
        return new ValueUIFuture((Object)ResourceUtil.loadImage((URI)uri));
    }
}

