/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.UnitType
 *  com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel
 *  com.github.weisj.jsvg.attributes.filter.LayoutBounds
 *  com.github.weisj.jsvg.attributes.filter.LayoutBounds$ComputeFlags
 *  com.github.weisj.jsvg.attributes.filter.LayoutBounds$Data
 *  com.github.weisj.jsvg.geometry.size.FloatInsets
 *  com.github.weisj.jsvg.geometry.util.GeometryUtil
 *  com.github.weisj.jsvg.nodes.animation.Animate
 *  com.github.weisj.jsvg.nodes.animation.Set
 *  com.github.weisj.jsvg.nodes.filter.Channel
 *  com.github.weisj.jsvg.nodes.filter.Filter$AlphaImageFilter
 *  com.github.weisj.jsvg.nodes.filter.Filter$FilterInfo
 *  com.github.weisj.jsvg.nodes.filter.FilterContext
 *  com.github.weisj.jsvg.nodes.filter.FilterLayoutContext
 *  com.github.weisj.jsvg.nodes.filter.FilterPrimitive
 *  com.github.weisj.jsvg.nodes.filter.IllegalFilterStateException
 *  com.github.weisj.jsvg.nodes.filter.ImageProducerChannel
 *  com.github.weisj.jsvg.util.BlittableImage
 *  com.github.weisj.jsvg.util.ImageUtil
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel;
import com.github.weisj.jsvg.attributes.filter.FilterChannelKey;
import com.github.weisj.jsvg.attributes.filter.LayoutBounds;
import com.github.weisj.jsvg.geometry.size.FloatInsets;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.container.ContainerNode;
import com.github.weisj.jsvg.nodes.filter.Channel;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.filter.FilterContext;
import com.github.weisj.jsvg.nodes.filter.FilterLayoutContext;
import com.github.weisj.jsvg.nodes.filter.FilterPrimitive;
import com.github.weisj.jsvg.nodes.filter.IllegalFilterStateException;
import com.github.weisj.jsvg.nodes.filter.ImageProducerChannel;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.util.BlittableImage;
import com.github.weisj.jsvg.util.ImageUtil;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Exception performing whole class analysis ignored.
 */
@ElementCategories(value={})
@PermittedContent(categories={Category.Descriptive, Category.FilterPrimitive}, anyOf={Animate.class, Set.class})
public final class Filter
extends ContainerNode {
    private static final boolean DEBUG = false;
    public static final String TAG = "filter";
    private static final Length DEFAULT_FILTER_COORDINATE = Unit.PERCENTAGE.valueOf(-10.0f);
    private static final Length DEFAULT_FILTER_SIZE = Unit.PERCENTAGE.valueOf(120.0f);
    private Length x;
    private Length y;
    private Length width;
    private Length height;
    private UnitType filterUnits;
    private UnitType filterPrimitiveUnits;
    private boolean isValid;

    @Override
    @NotNull
    public String tagName() {
        return "filter";
    }

    public boolean hasEffect() {
        return this.isValid && !this.children().isEmpty();
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.isValid = true;
        for (SVGNode sVGNode : this.children()) {
            FilterPrimitive filterPrimitive = (FilterPrimitive)sVGNode;
            if (filterPrimitive.isValid()) continue;
            this.isValid = false;
            break;
        }
        this.filterUnits = attributeNode.getEnum("filterUnits", UnitType.ObjectBoundingBox);
        this.filterPrimitiveUnits = attributeNode.getEnum("primitiveUnits", UnitType.UserSpaceOnUse);
        this.x = attributeNode.getLength("x", DEFAULT_FILTER_COORDINATE);
        this.y = attributeNode.getLength("y", DEFAULT_FILTER_COORDINATE);
        this.width = attributeNode.getLength("width", DEFAULT_FILTER_SIZE);
        this.height = attributeNode.getLength("height", DEFAULT_FILTER_SIZE);
        if (this.filterUnits == UnitType.ObjectBoundingBox) {
            this.x = this.coerceToPercentage(this.x);
            this.y = this.coerceToPercentage(this.y);
            this.width = this.coerceToPercentage(this.width);
            this.height = this.coerceToPercentage(this.height);
        }
    }

    @NotNull
    private Length coerceToPercentage(@NotNull Length length) {
        if (length.unit() == Unit.PERCENTAGE) {
            return length;
        }
        return new Length(Unit.PERCENTAGE, length.raw() * 100.0f);
    }

    @NotNull
    public FilterInfo createFilterInfo(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Rectangle2D elementBounds) {
        Rectangle2D.Double filterRegion = this.filterUnits.computeViewBounds(context.measureContext(), elementBounds, this.x, this.y, this.width, this.height);
        Rectangle graphicsClipBounds = g.getClipBounds();
        FilterLayoutContext filterLayoutContext = new FilterLayoutContext(this.filterPrimitiveUnits, elementBounds, (Rectangle2D)graphicsClipBounds);
        Rectangle2D clippedElementBounds = elementBounds.createIntersection(graphicsClipBounds);
        Rectangle2D effectiveFilterRegion = filterRegion.createIntersection(graphicsClipBounds);
        LayoutBounds elementLayoutBounds = new LayoutBounds(effectiveFilterRegion, new FloatInsets());
        LayoutBounds clippedElementLayoutBounds = new LayoutBounds(clippedElementBounds, new FloatInsets());
        LayoutBounds sourceDependentBounds = elementLayoutBounds.transform((data, flags) -> flags.operatesOnWholeFilterRegion ? data : clippedElementLayoutBounds.resolve(flags));
        filterLayoutContext.resultChannels().addResult((FilterChannelKey)DefaultFilterChannel.LastResult, (Object)elementLayoutBounds);
        filterLayoutContext.resultChannels().addResult((FilterChannelKey)DefaultFilterChannel.SourceGraphic, (Object)sourceDependentBounds);
        filterLayoutContext.resultChannels().addResult((FilterChannelKey)DefaultFilterChannel.SourceAlpha, (Object)sourceDependentBounds);
        for (SVGNode sVGNode : this.children()) {
            try {
                FilterPrimitive filterPrimitive = (FilterPrimitive)sVGNode;
                filterPrimitive.layoutFilter(context, filterLayoutContext);
            }
            catch (IllegalFilterStateException filterPrimitive) {}
        }
        LayoutBounds.Data clipHeuristic = ((LayoutBounds)filterLayoutContext.resultChannels().get((FilterChannelKey)DefaultFilterChannel.LastResult)).resolve(LayoutBounds.ComputeFlags.INITIAL);
        FloatInsets floatInsets = clipHeuristic.clipBoundsEscapeInsets();
        Rectangle2D clipHeuristicBounds = clipHeuristic.bounds().createIntersection(GeometryUtil.grow((Rectangle2D)graphicsClipBounds, (FloatInsets)floatInsets));
        BlittableImage blitImage = BlittableImage.create(ImageUtil::createCompatibleTransparentImage, (RenderContext)context, (Rectangle2D)clipHeuristicBounds, (Rectangle2D)filterRegion, (Rectangle2D)elementBounds, (UnitType)UnitType.UserSpaceOnUse);
        return new FilterInfo(g, blitImage, elementBounds, null);
    }

    public void applyFilter(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull FilterInfo filterInfo) {
        ImageProducer producer = FilterInfo.access$100((FilterInfo)filterInfo).image().getSource();
        FilterContext filterContext = new FilterContext(filterInfo, this.filterPrimitiveUnits, g.getRenderingHints());
        ImageProducerChannel sourceChannel = new ImageProducerChannel(producer);
        filterContext.resultChannels().addResult((FilterChannelKey)DefaultFilterChannel.SourceGraphic, (Object)sourceChannel);
        filterContext.resultChannels().addResult((FilterChannelKey)DefaultFilterChannel.LastResult, (Object)sourceChannel);
        filterContext.resultChannels().addResult((FilterChannelKey)DefaultFilterChannel.SourceAlpha, () -> Filter.lambda$applyFilter$1((Channel)sourceChannel));
        for (SVGNode sVGNode : this.children()) {
            try {
                FilterPrimitive filterPrimitive = (FilterPrimitive)sVGNode;
                filterPrimitive.applyFilter(context, filterContext);
            }
            catch (IllegalFilterStateException illegalFilterStateException) {}
        }
        FilterInfo.access$202((FilterInfo)filterInfo, (ImageProducer)Objects.requireNonNull(filterContext.getChannel((FilterChannelKey)DefaultFilterChannel.LastResult)).producer());
    }

    @Override
    protected boolean acceptChild(@Nullable String id, @NotNull SVGNode node) {
        return node instanceof FilterPrimitive && super.acceptChild(id, node);
    }

    private static /* synthetic */ Channel lambda$applyFilter$1(Channel sourceChannel) {
        return sourceChannel.applyFilter((ImageFilter)new AlphaImageFilter(null));
    }
}

