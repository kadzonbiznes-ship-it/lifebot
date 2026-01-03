/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.HasClip;
import com.github.weisj.jsvg.nodes.prototype.HasFilter;
import com.github.weisj.jsvg.nodes.prototype.Transformable;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasGeometryContext
extends Transformable,
HasClip,
HasFilter {

    public static interface ByDelegate
    extends HasGeometryContext {
        @NotNull
        public HasGeometryContext geometryContextDelegate();

        @Override
        @Nullable
        default public ClipPath clipPath() {
            return this.geometryContextDelegate().clipPath();
        }

        @Override
        @Nullable
        default public Mask mask() {
            return this.geometryContextDelegate().mask();
        }

        @Override
        @Nullable
        default public Filter filter() {
            return this.geometryContextDelegate().filter();
        }

        @Override
        @Nullable
        default public AffineTransform transform() {
            return this.geometryContextDelegate().transform();
        }

        @Override
        @NotNull
        default public Point2D transformOrigin(@NotNull MeasureContext context) {
            return this.geometryContextDelegate().transformOrigin(context);
        }
    }
}

