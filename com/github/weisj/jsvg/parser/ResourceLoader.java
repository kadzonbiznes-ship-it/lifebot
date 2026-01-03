/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.parser.UIFuture;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ResourceLoader {
    @Nullable
    public UIFuture<BufferedImage> loadImage(@NotNull URI var1) throws IOException;
}

