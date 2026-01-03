/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.util;

import com.github.weisj.jsvg.attributes.FillRule;
import com.github.weisj.jsvg.geometry.FillRuleAwareAWTSVGShape;
import com.github.weisj.jsvg.geometry.MeasurableShape;
import com.github.weisj.jsvg.geometry.path.BuildHistory;
import com.github.weisj.jsvg.geometry.path.PathCommand;
import com.github.weisj.jsvg.geometry.path.PathParser;
import java.awt.geom.Path2D;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PathUtil {
    @Nullable
    private static final MethodHandle trimPathHandle = PathUtil.lookupTrimPathMethod();

    @Nullable
    private static MethodHandle lookupTrimPathMethod() {
        try {
            MethodType methodType = MethodType.methodType(Void.TYPE);
            return MethodHandles.lookup().findVirtual(Path2D.class, "trimToSize", methodType);
        }
        catch (IllegalAccessException | NoSuchMethodException e) {
            return null;
        }
    }

    private PathUtil() {
    }

    @NotNull
    public static MeasurableShape parseFromPathData(@NotNull String data, @NotNull FillRule fillRule) {
        PathCommand[] pathCommands = new PathParser(data).parsePathCommand();
        int nodeCount = 2;
        for (PathCommand pathCommand : pathCommands) {
            nodeCount += pathCommand.nodeCount() - 1;
        }
        Path2D.Float path = new Path2D.Float(fillRule.awtWindingRule, nodeCount);
        BuildHistory hist = new BuildHistory();
        for (PathCommand pathCommand : pathCommands) {
            pathCommand.appendPath(path, hist);
        }
        PathUtil.trimPathToSize(path);
        return new FillRuleAwareAWTSVGShape(path);
    }

    public static void trimPathToSize(@NotNull Path2D path) {
        if (trimPathHandle != null) {
            try {
                trimPathHandle.invokeExact(path);
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}

