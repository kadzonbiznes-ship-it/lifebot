/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.parser.CharacterDataParser
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.prototype.Container;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.parser.CharacterDataParser;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParsedElement {
    @Nullable
    private final String id;
    @NotNull
    private final AttributeNode attributeNode;
    @NotNull
    private final SVGNode node;
    @NotNull
    private final @NotNull List<@NotNull ParsedElement> children = new ArrayList<ParsedElement>();
    final CharacterDataParser characterDataParser;
    @NotNull
    private BuildStatus buildStatus = BuildStatus.NOT_BUILT;

    ParsedElement(@Nullable String id, @NotNull AttributeNode element, @NotNull SVGNode node) {
        this.attributeNode = element;
        this.node = node;
        this.id = id;
        PermittedContent permittedContent = node.getClass().getAnnotation(PermittedContent.class);
        if (permittedContent == null) {
            throw new IllegalStateException("Element <" + node.tagName() + "> doesn't specify permitted content");
        }
        this.characterDataParser = permittedContent.charData() ? new CharacterDataParser() : null;
    }

    public void registerNamedElement(@NotNull String name, @NotNull Object element) {
        this.attributeNode.namedElements().put(name, element);
    }

    @Nullable
    public String id() {
        return this.id;
    }

    @NotNull
    public List<ParsedElement> children() {
        return this.children;
    }

    @NotNull
    public SVGNode node() {
        return this.node;
    }

    @NotNull
    public SVGNode nodeEnsuringBuildStatus() {
        if (this.buildStatus == BuildStatus.IN_PROGRESS) {
            this.cyclicDependencyDetected();
        } else if (this.buildStatus == BuildStatus.NOT_BUILT) {
            this.build();
        }
        return this.node;
    }

    @NotNull
    public AttributeNode attributeNode() {
        return this.attributeNode;
    }

    void addChild(ParsedElement parsedElement) {
        this.children.add(parsedElement);
        if (this.node instanceof Container) {
            ((Container)((Object)this.node)).addChild(parsedElement.id, parsedElement.node);
        }
    }

    void build() {
        if (this.buildStatus == BuildStatus.FINISHED) {
            return;
        }
        if (this.buildStatus == BuildStatus.IN_PROGRESS) {
            this.cyclicDependencyDetected();
            return;
        }
        this.buildStatus = BuildStatus.IN_PROGRESS;
        this.attributeNode.prepareForNodeBuilding(this);
        for (ParsedElement child : this.children) {
            child.build();
        }
        this.node.build(this.attributeNode);
        this.buildStatus = BuildStatus.FINISHED;
    }

    public String toString() {
        return "ParsedElement{node=" + this.node + '}';
    }

    private void cyclicDependencyDetected() {
        throw new IllegalStateException("Cyclic dependency involving node '" + this.id + "' detected.");
    }

    private static enum BuildStatus {
        NOT_BUILT,
        IN_PROGRESS,
        FINISHED;

    }
}

