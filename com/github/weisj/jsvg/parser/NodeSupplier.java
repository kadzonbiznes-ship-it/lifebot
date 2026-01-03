/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.nodes.Circle
 *  com.github.weisj.jsvg.nodes.Defs
 *  com.github.weisj.jsvg.nodes.Desc
 *  com.github.weisj.jsvg.nodes.Ellipse
 *  com.github.weisj.jsvg.nodes.Line
 *  com.github.weisj.jsvg.nodes.LinearGradient
 *  com.github.weisj.jsvg.nodes.Metadata
 *  com.github.weisj.jsvg.nodes.Polygon
 *  com.github.weisj.jsvg.nodes.Polyline
 *  com.github.weisj.jsvg.nodes.RadialGradient
 *  com.github.weisj.jsvg.nodes.Rect
 *  com.github.weisj.jsvg.nodes.SolidColor
 *  com.github.weisj.jsvg.nodes.Stop
 *  com.github.weisj.jsvg.nodes.Symbol
 *  com.github.weisj.jsvg.nodes.Title
 *  com.github.weisj.jsvg.nodes.animation.Animate
 *  com.github.weisj.jsvg.nodes.animation.AnimateTransform
 *  com.github.weisj.jsvg.nodes.animation.Set
 *  com.github.weisj.jsvg.nodes.filter.DummyFilterPrimitive
 *  com.github.weisj.jsvg.nodes.filter.FeBlend
 *  com.github.weisj.jsvg.nodes.filter.FeColorMatrix
 *  com.github.weisj.jsvg.nodes.filter.FeComposite
 *  com.github.weisj.jsvg.nodes.filter.FeDisplacementMap
 *  com.github.weisj.jsvg.nodes.filter.FeFlood
 *  com.github.weisj.jsvg.nodes.filter.FeGaussianBlur
 *  com.github.weisj.jsvg.nodes.filter.FeMerge
 *  com.github.weisj.jsvg.nodes.filter.FeMergeNode
 *  com.github.weisj.jsvg.nodes.filter.FeOffset
 *  com.github.weisj.jsvg.nodes.filter.FeTurbulence
 *  com.github.weisj.jsvg.nodes.mesh.MeshGradient
 *  com.github.weisj.jsvg.nodes.mesh.MeshPatch
 *  com.github.weisj.jsvg.nodes.mesh.MeshRow
 *  com.github.weisj.jsvg.nodes.text.TextPath
 *  com.github.weisj.jsvg.nodes.text.TextSpan
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.Circle;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Defs;
import com.github.weisj.jsvg.nodes.Desc;
import com.github.weisj.jsvg.nodes.Ellipse;
import com.github.weisj.jsvg.nodes.Group;
import com.github.weisj.jsvg.nodes.Image;
import com.github.weisj.jsvg.nodes.Line;
import com.github.weisj.jsvg.nodes.LinearGradient;
import com.github.weisj.jsvg.nodes.Marker;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.Metadata;
import com.github.weisj.jsvg.nodes.Path;
import com.github.weisj.jsvg.nodes.Pattern;
import com.github.weisj.jsvg.nodes.Polygon;
import com.github.weisj.jsvg.nodes.Polyline;
import com.github.weisj.jsvg.nodes.RadialGradient;
import com.github.weisj.jsvg.nodes.Rect;
import com.github.weisj.jsvg.nodes.SVG;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.SolidColor;
import com.github.weisj.jsvg.nodes.Stop;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.Symbol;
import com.github.weisj.jsvg.nodes.Title;
import com.github.weisj.jsvg.nodes.Use;
import com.github.weisj.jsvg.nodes.View;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.AnimateTransform;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.filter.DummyFilterPrimitive;
import com.github.weisj.jsvg.nodes.filter.FeBlend;
import com.github.weisj.jsvg.nodes.filter.FeColorMatrix;
import com.github.weisj.jsvg.nodes.filter.FeComposite;
import com.github.weisj.jsvg.nodes.filter.FeDisplacementMap;
import com.github.weisj.jsvg.nodes.filter.FeFlood;
import com.github.weisj.jsvg.nodes.filter.FeGaussianBlur;
import com.github.weisj.jsvg.nodes.filter.FeMerge;
import com.github.weisj.jsvg.nodes.filter.FeMergeNode;
import com.github.weisj.jsvg.nodes.filter.FeOffset;
import com.github.weisj.jsvg.nodes.filter.FeTurbulence;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.mesh.MeshGradient;
import com.github.weisj.jsvg.nodes.mesh.MeshPatch;
import com.github.weisj.jsvg.nodes.mesh.MeshRow;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.nodes.text.TextPath;
import com.github.weisj.jsvg.nodes.text.TextSpan;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NodeSupplier {
    private final Map<String, Supplier<SVGNode>> constructorMap;

    public NodeSupplier() {
        this(new TreeMap<String, Supplier<SVGNode>>(String.CASE_INSENSITIVE_ORDER));
    }

    public NodeSupplier(@NotNull @NotNull Map<@NotNull String, @NotNull Supplier<@NotNull SVGNode>> mapImpl) {
        mapImpl.clear();
        this.constructorMap = mapImpl;
        this.constructorMap.put("a", () -> new Anchor());
        this.constructorMap.put("clippath", () -> new ClipPath());
        this.constructorMap.put("defs", () -> new Defs());
        this.constructorMap.put("g", () -> new Group());
        this.constructorMap.put("image", () -> new Image());
        this.constructorMap.put("marker", () -> new Marker());
        this.constructorMap.put("mask", () -> new Mask());
        this.constructorMap.put("svg", () -> new SVG());
        this.constructorMap.put("style", () -> new Style());
        this.constructorMap.put("symbol", () -> new Symbol());
        this.constructorMap.put("use", () -> new Use());
        this.constructorMap.put("view", () -> new View());
        this.populateShapeNodeConstructors();
        this.populatePaintNodeConstructors();
        this.populateTextNodeConstructors();
        this.populateFilterNodeConstructors();
        this.populateAnimationNodeConstructors();
        this.populateMetaNodeConstructors();
        this.populateDummyNodeConstructors();
    }

    @Nullable
    public SVGNode create(@NotNull String tagName) {
        @Nullable Supplier<SVGNode> supplier = this.constructorMap.get(tagName);
        if (supplier == null) {
            return null;
        }
        return supplier.get();
    }

    private void populateShapeNodeConstructors() {
        this.constructorMap.put("circle", () -> new Circle());
        this.constructorMap.put("ellipse", () -> new Ellipse());
        this.constructorMap.put("line", () -> new Line());
        this.constructorMap.put("path", () -> new Path());
        this.constructorMap.put("polygon", () -> new Polygon());
        this.constructorMap.put("polyline", () -> new Polyline());
        this.constructorMap.put("rect", () -> new Rect());
    }

    private void populatePaintNodeConstructors() {
        this.constructorMap.put("lineargradient", () -> new LinearGradient());
        this.constructorMap.put("meshgradient", () -> new MeshGradient());
        this.constructorMap.put("meshpatch", () -> new MeshPatch());
        this.constructorMap.put("meshrow", () -> new MeshRow());
        this.constructorMap.put("pattern", () -> new Pattern());
        this.constructorMap.put("radialgradient", () -> new RadialGradient());
        this.constructorMap.put("solidcolor", () -> new SolidColor());
        this.constructorMap.put("stop", () -> new Stop());
    }

    private void populateTextNodeConstructors() {
        this.constructorMap.put("text", () -> new Text());
        this.constructorMap.put("textpath", () -> new TextPath());
        this.constructorMap.put("tspan", () -> new TextSpan());
    }

    private void populateFilterNodeConstructors() {
        this.constructorMap.put("filter", () -> new Filter());
        this.constructorMap.put("feblend", () -> new FeBlend());
        this.constructorMap.put("fecolormatrix", () -> new FeColorMatrix());
        this.constructorMap.put("fecomposite", () -> new FeComposite());
        this.constructorMap.put("fedisplacementmap", () -> new FeDisplacementMap());
        this.constructorMap.put("feflood", () -> new FeFlood());
        this.constructorMap.put("fegaussianblur", () -> new FeGaussianBlur());
        this.constructorMap.put("feMerge", () -> new FeMerge());
        this.constructorMap.put("feMergeNode", () -> new FeMergeNode());
        this.constructorMap.put("feturbulence", () -> new FeTurbulence());
        this.constructorMap.put("feOffset", () -> new FeOffset());
    }

    private void populateAnimationNodeConstructors() {
        this.constructorMap.put("animate", () -> new Animate());
        this.constructorMap.put("animatetransform", () -> new AnimateTransform());
        this.constructorMap.put("set", () -> new Set());
    }

    private void populateMetaNodeConstructors() {
        this.constructorMap.put("desc", () -> new Desc());
        this.constructorMap.put("metadata", () -> new Metadata());
        this.constructorMap.put("title", () -> new Title());
    }

    private void populateDummyNodeConstructors() {
        this.constructorMap.put("feComponentTransfer", () -> new DummyFilterPrimitive("feComponentTransfer"));
        this.constructorMap.put("feConvolveMatrix", () -> new DummyFilterPrimitive("feConvolveMatrix"));
        this.constructorMap.put("feDiffuseLightning", () -> new DummyFilterPrimitive("feDiffuseLightning"));
        this.constructorMap.put("feDisplacementMap", () -> new DummyFilterPrimitive("feDisplacementMap"));
        this.constructorMap.put("feDropShadow", () -> new DummyFilterPrimitive("feDropShadow"));
        this.constructorMap.put("feFuncA", () -> new DummyFilterPrimitive("feFuncA"));
        this.constructorMap.put("feFuncB", () -> new DummyFilterPrimitive("feFuncB"));
        this.constructorMap.put("feFuncG", () -> new DummyFilterPrimitive("feFuncG"));
        this.constructorMap.put("feFuncR", () -> new DummyFilterPrimitive("feFuncR"));
        this.constructorMap.put("feImage", () -> new DummyFilterPrimitive("feImage"));
        this.constructorMap.put("feMorphology", () -> new DummyFilterPrimitive("feMorphology"));
        this.constructorMap.put("feSpecularLighting", () -> new DummyFilterPrimitive("feSpecularLighting"));
        this.constructorMap.put("feTile", () -> new DummyFilterPrimitive("feTile"));
    }
}

