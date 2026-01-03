/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.attributes.HasMatchName;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.EnumSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum VectorEffect implements HasMatchName
{
    None(0),
    NonScalingStroke("non-scaling-stroke", 0),
    NonScalingSize("non-scaling-size", 1),
    NonRotation("non-rotation", 2),
    FixedPosition("fixed-position", 4);

    @NotNull
    private final String matchName;
    private final int flag;

    private VectorEffect(int flag) {
        this.matchName = this.name();
        this.flag = flag;
    }

    private VectorEffect(String matchName, int flag) {
        this.matchName = matchName;
        this.flag = flag;
    }

    @NotNull
    public static Set<VectorEffect> parse(@NotNull AttributeNode attributeNode) {
        @NotNull String[] vectorEffectsRaw = attributeNode.getStringList("vector-effect");
        EnumSet<VectorEffect> vectorEffects = EnumSet.noneOf(VectorEffect.class);
        for (String effect : vectorEffectsRaw) {
            vectorEffects.add(attributeNode.parser().parseEnum(effect, None));
        }
        return vectorEffects;
    }

    @Override
    @NotNull
    public String matchName() {
        return this.matchName;
    }

    private static int flags(@NotNull Set<VectorEffect> effects) {
        int flag = 0;
        for (VectorEffect effect : effects) {
            flag |= effect.flag;
        }
        return flag;
    }

    public static void applyEffects(@NotNull Set<VectorEffect> effects, @NotNull Graphics2D g, @NotNull RenderContext context, @Nullable AffineTransform elementTransform) {
        int flags = VectorEffect.flags(effects);
        if (flags == 0) {
            return;
        }
        AffineTransform shapeTransform = new AffineTransform(context.userSpaceTransform());
        double x0 = elementTransform != null ? elementTransform.getTranslateX() : 0.0;
        double y0 = elementTransform != null ? elementTransform.getTranslateY() : 0.0;
        VectorEffect.updateTransformForFlags(flags, shapeTransform, x0, y0);
        g.setTransform(context.rootTransform());
        g.transform(shapeTransform);
    }

    @NotNull
    public static Shape applyNonScalingStroke(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape) {
        g.setTransform(context.rootTransform());
        return context.userSpaceTransform().createTransformedShape(shape);
    }

    private static void updateTransformForFlags(int flags, @NotNull AffineTransform transform, double x0, double y0) {
        switch (flags) {
            case 1: {
                double detRoot = Math.sqrt(Math.abs(transform.getDeterminant()));
                if (detRoot == 0.0) {
                    return;
                }
                double detRootInv = 1.0 / detRoot;
                transform.setTransform(transform.getScaleX() * detRootInv, transform.getShearY() * detRootInv, transform.getShearX() * detRootInv, transform.getScaleY() * detRootInv, transform.getTranslateX(), transform.getTranslateY());
                break;
            }
            case 2: {
                double detRoot = Math.sqrt(Math.abs(transform.getDeterminant()));
                transform.setTransform(detRoot, 0.0, 0.0, detRoot, transform.getTranslateX(), transform.getTranslateY());
                break;
            }
            case 3: {
                transform.setTransform(1.0, 0.0, 0.0, 1.0, transform.getTranslateX(), transform.getTranslateY());
                break;
            }
            case 4: {
                transform.setTransform(transform.getScaleX(), transform.getShearY(), transform.getShearX(), transform.getScaleY(), x0, y0);
                break;
            }
            case 5: {
                double detRoot = Math.sqrt(Math.abs(transform.getDeterminant()));
                if (detRoot == 0.0) {
                    return;
                }
                double detRootInv = 1.0 / detRoot;
                transform.setTransform(transform.getScaleX() * detRootInv, transform.getShearY() * detRootInv, transform.getShearX() * detRootInv, transform.getScaleY() * detRootInv, x0, y0);
                break;
            }
            case 6: {
                double detRoot = Math.sqrt(Math.abs(transform.getDeterminant()));
                transform.setTransform(detRoot, 0.0, 0.0, detRoot, x0, y0);
                break;
            }
            case 7: {
                transform.setTransform(1.0, 0.0, 0.0, 1.0, x0, y0);
                break;
            }
        }
    }
}

