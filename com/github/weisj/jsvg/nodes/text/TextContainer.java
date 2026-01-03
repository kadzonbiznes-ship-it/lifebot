/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.text.LengthAdjust
 *  com.github.weisj.jsvg.nodes.text.GlyphAdvancement
 *  com.github.weisj.jsvg.nodes.text.GlyphCursor
 *  com.github.weisj.jsvg.nodes.text.GlyphRenderer
 *  com.github.weisj.jsvg.nodes.text.StringTextSegment
 *  com.github.weisj.jsvg.nodes.text.TextContainer$1
 *  com.github.weisj.jsvg.nodes.text.TextContainer$IntermediateTextMetrics
 *  com.github.weisj.jsvg.nodes.text.TextMetrics
 *  com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.attributes.PaintOrder;
import com.github.weisj.jsvg.attributes.VectorEffect;
import com.github.weisj.jsvg.attributes.font.AttributeFontSpec;
import com.github.weisj.jsvg.attributes.font.FontParser;
import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.attributes.text.LengthAdjust;
import com.github.weisj.jsvg.attributes.text.TextAnchor;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.container.BaseContainerNode;
import com.github.weisj.jsvg.nodes.prototype.HasContext;
import com.github.weisj.jsvg.nodes.prototype.HasShape;
import com.github.weisj.jsvg.nodes.prototype.HasVectorEffects;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.prototype.impl.HasContextImpl;
import com.github.weisj.jsvg.nodes.text.GlyphAdvancement;
import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import com.github.weisj.jsvg.nodes.text.GlyphRenderer;
import com.github.weisj.jsvg.nodes.text.StringTextSegment;
import com.github.weisj.jsvg.nodes.text.TextContainer;
import com.github.weisj.jsvg.nodes.text.TextMetrics;
import com.github.weisj.jsvg.nodes.text.TextSegment;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.FontRenderContext;
import com.github.weisj.jsvg.renderer.NodeRenderer;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class TextContainer
extends BaseContainerNode<TextSegment>
implements TextSegment.RenderableSegment,
HasShape,
HasContext.ByDelegate,
HasVectorEffects,
Renderable {
    private final List<@NotNull TextSegment> segments = new ArrayList<TextSegment>();
    private PaintOrder paintOrder;
    protected AttributeFontSpec fontSpec;
    protected LengthAdjust lengthAdjust;
    protected Length textLength;
    private boolean isVisible;
    private HasContext context;
    private Set<VectorEffect> vectorEffects;

    TextContainer() {
    }

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.paintOrder = PaintOrder.parse(attributeNode);
        this.fontSpec = FontParser.parseFontSpec(attributeNode);
        this.lengthAdjust = attributeNode.getEnum("lengthAdjust", LengthAdjust.Spacing);
        this.textLength = attributeNode.getLength("textLength", Length.UNSPECIFIED);
        if (this.textLength.raw() < 0.0f) {
            this.textLength = Length.UNSPECIFIED;
        }
        this.isVisible = this.parseIsVisible(attributeNode);
        this.context = HasContextImpl.parse(attributeNode);
        this.vectorEffects = VectorEffect.parse(attributeNode);
    }

    @Override
    @NotNull
    public Set<VectorEffect> vectorEffects() {
        return this.vectorEffects;
    }

    @Override
    @NotNull
    public HasContext contextDelegate() {
        return this.context;
    }

    @Override
    protected boolean acceptChild(@Nullable String id, @NotNull SVGNode node) {
        return node instanceof TextSegment;
    }

    @Override
    protected void doAdd(@NotNull SVGNode node) {
        this.segments.add((TextSegment)((Object)node));
    }

    @Override
    public final void addContent(char[] content) {
        if (content.length == 0) {
            return;
        }
        this.segments.add((TextSegment)new StringTextSegment(this, this.segments.size(), content));
    }

    @Override
    public List<? extends @NotNull TextSegment> children() {
        return this.segments;
    }

    protected abstract GlyphCursor createLocalCursor(@NotNull RenderContext var1, @NotNull GlyphCursor var2);

    protected abstract void cleanUpLocalCursor(@NotNull GlyphCursor var1, @NotNull GlyphCursor var2);

    protected final void renderSegment(@NotNull GlyphCursor cursor, @NotNull RenderContext context, @NotNull Graphics2D g) {
        this.prepareSegmentForRendering(cursor, context);
        double offset = this.textAnchorOffset(context.fontRenderContext().textAnchor(), cursor);
        context.translate(g, -offset, 0.0);
        this.renderSegmentWithoutLayout(cursor, context, g);
    }

    private double textAnchorOffset(@NotNull TextAnchor textAnchor, @NotNull GlyphCursor glyphCursor) {
        switch (1.$SwitchMap$com$github$weisj$jsvg$attributes$text$TextAnchor[textAnchor.ordinal()]) {
            default: {
                return 0.0;
            }
            case 2: {
                return glyphCursor.completeGlyphRunBounds.getWidth() / 2.0;
            }
            case 3: 
        }
        return glyphCursor.completeGlyphRunBounds.getWidth();
    }

    private void forEachSegment(@NotNull RenderContext context, @NotNull BiConsumer<StringTextSegment, RenderContext> onStringTextSegment, @NotNull BiConsumer<TextSegment.RenderableSegment, RenderContext> onRenderableSegment) {
        for (TextSegment textSegment : this.children()) {
            RenderContext currentContext = context;
            if (textSegment instanceof Renderable) {
                currentContext = NodeRenderer.setupRenderContext(textSegment, context);
            }
            if (textSegment instanceof StringTextSegment) {
                onStringTextSegment.accept((StringTextSegment)textSegment, currentContext);
                continue;
            }
            if (textSegment instanceof TextSegment.RenderableSegment) {
                onRenderableSegment.accept((TextSegment.RenderableSegment)textSegment, currentContext);
                continue;
            }
            throw new IllegalStateException("Unexpected segment " + textSegment);
        }
    }

    @Override
    @NotNull
    public TextMetrics computeTextMetrics(@NotNull RenderContext context, @NotNull TextSegment.RenderableSegment.UseTextLengthForCalculation flag) {
        if (flag == TextSegment.RenderableSegment.UseTextLengthForCalculation.YES && this.hasFixedLength()) {
            return new TextMetrics(0.0, 0.0, 0, (double)this.textLength.resolveLength(context.measureContext()), 0);
        }
        SVGFont font = context.font();
        float letterSpacing = context.fontRenderContext().letterSpacing().resolveLength(context.measureContext());
        IntermediateTextMetrics metrics = new IntermediateTextMetrics(null);
        int index = 0;
        for (TextSegment textSegment : this.children()) {
            RenderContext currentContext = context;
            if (textSegment instanceof Renderable) {
                currentContext = NodeRenderer.setupRenderContext(textSegment, context);
            }
            if (textSegment instanceof StringTextSegment) {
                StringTextSegment stringTextSegment = (StringTextSegment)textSegment;
                this.accumulateSegmentMetrics(metrics, stringTextSegment, font, letterSpacing, index);
            } else if (textSegment instanceof TextSegment.RenderableSegment) {
                this.accumulateRenderableSegmentMetrics((TextSegment.RenderableSegment)textSegment, metrics, currentContext);
            } else {
                throw new IllegalStateException("Unexpected segment " + textSegment);
            }
            ++index;
        }
        return new TextMetrics(metrics.letterSpacingLength, metrics.glyphLength, metrics.glyphCount, metrics.fixedGlyphLength, metrics.controllableLetterSpacingCount);
    }

    private void accumulateRenderableSegmentMetrics(@NotNull TextSegment.RenderableSegment segment, @NotNull IntermediateTextMetrics metrics, @NotNull RenderContext currentContext) {
        TextMetrics textMetrics = segment.computeTextMetrics(currentContext, TextSegment.RenderableSegment.UseTextLengthForCalculation.YES);
        metrics.letterSpacingLength += textMetrics.letterSpacingLength();
        metrics.glyphLength += textMetrics.glyphLength();
        metrics.glyphCount += textMetrics.glyphCount();
        metrics.fixedGlyphLength += textMetrics.fixedGlyphLength();
        metrics.controllableLetterSpacingCount += textMetrics.controllableLetterSpacingCount();
    }

    private void accumulateSegmentMetrics(@NotNull IntermediateTextMetrics metrics, @NotNull StringTextSegment segment, @NotNull SVGFont font, float letterSpacing, int index) {
        int glyphCount = segment.codepoints().length;
        boolean lastSegment = index == this.children().size() - 1;
        int whiteSpaceCount = lastSegment ? glyphCount - 1 : glyphCount;
        metrics.glyphCount += glyphCount;
        metrics.letterSpacingLength += (double)((float)whiteSpaceCount * letterSpacing);
        metrics.controllableLetterSpacingCount += whiteSpaceCount;
        for (char codepoint : segment.codepoints()) {
            metrics.glyphLength += (double)font.codepointGlyph(codepoint).advance();
        }
    }

    @Override
    public boolean hasFixedLength() {
        return this.textLength.isSpecified();
    }

    @Override
    public void renderSegmentWithoutLayout(@NotNull GlyphCursor cursor, @NotNull RenderContext context, @NotNull Graphics2D g) {
        this.forEachSegment(context, (segment, ctx) -> {
            if (this.isVisible((RenderContext)ctx)) {
                GlyphRenderer.renderGlyphRun((Graphics2D)g, (PaintOrder)this.paintOrder, this.vectorEffects(), (StringTextSegment)segment, (Rectangle2D)cursor.completeGlyphRunBounds);
            }
        }, (segment, ctx) -> segment.renderSegmentWithoutLayout(cursor, (RenderContext)ctx, g));
    }

    @Override
    public void prepareSegmentForRendering(@NotNull GlyphCursor cursor, @NotNull RenderContext context) {
        SVGFont font = context.font();
        GlyphCursor localCursor = this.createLocalCursor(context, cursor);
        localCursor.setAdvancement(this.localGlyphAdvancement(context, cursor));
        this.forEachSegment(context, (segment, ctx) -> GlyphRenderer.prepareGlyphRun((StringTextSegment)segment, (GlyphCursor)localCursor, (SVGFont)font, (RenderContext)ctx), (segment, ctx) -> segment.prepareSegmentForRendering(localCursor, (RenderContext)ctx));
        this.cleanUpLocalCursor(cursor, localCursor);
    }

    @Override
    public void appendTextShape(@NotNull GlyphCursor cursor, @NotNull Path2D textShape, @NotNull RenderContext context) {
        SVGFont font = context.font();
        GlyphCursor localCursor = this.createLocalCursor(context, cursor);
        localCursor.setAdvancement(this.localGlyphAdvancement(context, cursor));
        this.forEachSegment(context, (segment, ctx) -> textShape.append(GlyphRenderer.layoutGlyphRun((StringTextSegment)segment, (GlyphCursor)localCursor, (SVGFont)font, (MeasureContext)ctx.measureContext(), (FontRenderContext)ctx.fontRenderContext()), false), (segment, ctx) -> segment.appendTextShape(localCursor, textShape, (RenderContext)ctx));
        this.cleanUpLocalCursor(cursor, localCursor);
    }

    @NotNull
    private GlyphAdvancement localGlyphAdvancement(@NotNull RenderContext context, @NotNull GlyphCursor cursor) {
        if (this.hasFixedLength()) {
            return new GlyphAdvancement(this.computeTextMetrics(context, TextSegment.RenderableSegment.UseTextLengthForCalculation.NO), this.textLength.resolveWidth(context.measureContext()), this.lengthAdjust);
        }
        return cursor.advancement();
    }

    @Override
    @NotNull
    public Rectangle2D untransformedElementBounds(@NotNull RenderContext context) {
        return this.untransformedElementShape(context).getBounds2D();
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return this.isVisible;
    }
}

