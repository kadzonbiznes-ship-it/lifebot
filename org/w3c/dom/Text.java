/*
 * Decompiled with CFR 0.152.
 */
package org.w3c.dom;

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

public interface Text
extends CharacterData {
    public Text splitText(int var1) throws DOMException;

    public boolean isElementContentWhitespace();

    public String getWholeText();

    public Text replaceWholeText(String var1) throws DOMException;
}

