/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.parser.DomProcessor
 *  com.github.weisj.jsvg.parser.css.StyleSheet
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.AttributeParser;
import com.github.weisj.jsvg.nodes.SVG;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.Use;
import com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.parser.DomProcessor;
import com.github.weisj.jsvg.parser.LoadHelper;
import com.github.weisj.jsvg.parser.NodeSupplier;
import com.github.weisj.jsvg.parser.ParsedElement;
import com.github.weisj.jsvg.parser.ParserProvider;
import com.github.weisj.jsvg.parser.ResourceLoader;
import com.github.weisj.jsvg.parser.css.CssParser;
import com.github.weisj.jsvg.parser.css.StyleSheet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class SVGDocumentBuilder {
    private static final int MAX_USE_NESTING_DEPTH = 15;
    private final Map<String, Object> namedElements = new HashMap<String, Object>();
    private final List<Use> useElements = new ArrayList<Use>();
    private final List<Style> styleElements = new ArrayList<Style>();
    private final List<StyleSheet> styleSheets = new ArrayList<StyleSheet>();
    private final Deque<ParsedElement> currentNodeStack = new ArrayDeque<ParsedElement>();
    @NotNull
    private final ParserProvider parserProvider;
    @NotNull
    private final LoadHelper loadHelper;
    @NotNull
    private final NodeSupplier nodeSupplier;
    private ParsedElement rootNode;

    public SVGDocumentBuilder(@NotNull ParserProvider parserProvider, @NotNull ResourceLoader resourceLoader, @NotNull NodeSupplier nodeSupplier) {
        this.parserProvider = parserProvider;
        this.loadHelper = new LoadHelper(new AttributeParser(parserProvider.createPaintParser()), resourceLoader);
        this.nodeSupplier = nodeSupplier;
    }

    public void startDocument() {
        if (this.rootNode != null) {
            throw new IllegalStateException("Document already started");
        }
    }

    public void endDocument() {
        if (this.rootNode == null) {
            throw new IllegalStateException("Document is empty");
        }
    }

    public boolean startElement(@NotNull String tagName, @NotNull Map<String, String> attributes) {
        SVGNode newNode;
        AttributeNode parentAttributeNode;
        ParsedElement parentElement = !this.currentNodeStack.isEmpty() ? this.currentNodeStack.peek() : null;
        AttributeNode attributeNode = parentAttributeNode = parentElement != null ? parentElement.attributeNode() : null;
        if (parentElement != null) {
            this.flushText(parentElement, true);
        }
        if ((newNode = this.nodeSupplier.create(tagName)) == null) {
            return false;
        }
        AttributeNode attributeNode2 = new AttributeNode(tagName, attributes, parentAttributeNode, this.namedElements, this.styleSheets, this.loadHelper);
        String id = attributes.get("id");
        ParsedElement parsedElement = new ParsedElement(id, attributeNode2, newNode);
        if (id != null && !this.namedElements.containsKey(id)) {
            this.namedElements.put(id, parsedElement);
        }
        if (parentElement != null) {
            parentElement.addChild(parsedElement);
        }
        if (this.rootNode == null) {
            this.rootNode = parsedElement;
        }
        if (parsedElement.node() instanceof Style) {
            this.styleElements.add((Style)parsedElement.node());
        }
        if (parsedElement.node() instanceof Use) {
            this.useElements.add((Use)parsedElement.node());
        }
        this.currentNodeStack.push(parsedElement);
        return true;
    }

    public void addTextContent(char @NotNull [] characterData, int startOffset, int endOffset) {
        if (this.currentNodeStack.isEmpty()) {
            throw new IllegalStateException("Adding text content without a current node");
        }
        ParsedElement currentElement = this.currentNodeStack.peek();
        if (currentElement.characterDataParser == null) {
            return;
        }
        currentElement.characterDataParser.append(characterData, startOffset, endOffset);
    }

    public void endElement(@NotNull String tagName) {
        if (this.currentNodeStack.isEmpty()) {
            throw new IllegalStateException("No current node to end");
        }
        ParsedElement currentElement = this.currentNodeStack.pop();
        String currentNodeTagName = currentElement.attributeNode().tagName();
        if (!currentNodeTagName.equals(tagName)) {
            throw new IllegalStateException(String.format("Closing tag %s doesn't match current node %s)", tagName, currentNodeTagName));
        }
        this.flushText(currentElement, false);
    }

    private void flushText(@NotNull ParsedElement element, boolean segmentBreak) {
        if (element.characterDataParser != null && element.characterDataParser.canFlush(segmentBreak)) {
            element.node().addContent(element.characterDataParser.flush(segmentBreak));
        }
    }

    @NotNull
    public SVGDocument build() {
        if (this.rootNode == null) {
            throw new IllegalStateException("No root node");
        }
        this.processStyleSheets();
        DomProcessor preProcessor = this.parserProvider.createPreProcessor();
        if (preProcessor != null) {
            preProcessor.process(this.rootNode);
        }
        this.rootNode.build();
        DomProcessor postProcessor = this.parserProvider.createPostProcessor();
        if (postProcessor != null) {
            postProcessor.process(this.rootNode);
        }
        this.validateUseElements();
        return new SVGDocument((SVG)this.rootNode.node());
    }

    private void processStyleSheets() {
        if (this.styleElements.isEmpty()) {
            return;
        }
        CssParser cssParser = this.parserProvider.createCssParser();
        for (Style styleElement : this.styleElements) {
            styleElement.parseStyleSheet(cssParser);
            this.styleSheets.add(styleElement.styleSheet());
        }
    }

    private void validateUseElements() {
        if (this.useElements.isEmpty()) {
            return;
        }
        for (Use useElement : this.useElements) {
            this.checkNestingDepth(useElement, 15);
        }
    }

    private void checkNestingDepth(@NotNull SVGNode node, int allowed_depth) {
        if (allowed_depth <= 0) {
            throw new IllegalStateException("Maximum nesting depth exceeded");
        }
        if (node instanceof Use) {
            SVGNode referenced = ((Use)node).referencedNode();
            if (referenced != null) {
                this.checkNestingDepth(referenced, allowed_depth - 1);
            }
        } else if (node instanceof CommonRenderableContainerNode) {
            for (SVGNode sVGNode : ((CommonRenderableContainerNode)node).children()) {
                this.checkNestingDepth(sVGNode, allowed_depth);
            }
        }
    }
}

