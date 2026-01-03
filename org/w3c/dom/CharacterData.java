/*
 * Decompiled with CFR 0.152.
 */
package org.w3c.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public interface CharacterData
extends Node {
    public String getData() throws DOMException;

    public void setData(String var1) throws DOMException;

    public int getLength();

    public String substringData(int var1, int var2) throws DOMException;

    public void appendData(String var1) throws DOMException;

    public void insertData(int var1, String var2) throws DOMException;

    public void deleteData(int var1, int var2) throws DOMException;

    public void replaceData(int var1, int var2, String var3) throws DOMException;
}

