/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.PropertyState;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

public interface XMLComponentManager {
    public boolean getFeature(String var1) throws XMLConfigurationException;

    public boolean getFeature(String var1, boolean var2);

    public Object getProperty(String var1) throws XMLConfigurationException;

    public Object getProperty(String var1, Object var2);

    public FeatureState getFeatureState(String var1);

    public PropertyState getPropertyState(String var1);
}

