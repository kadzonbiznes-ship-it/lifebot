/*
 * Decompiled with CFR 0.152.
 */
package sun.text.resources;

import java.util.ResourceBundle;
import sun.text.resources.BreakIteratorInfo;
import sun.util.resources.BreakIteratorResourceBundle;

public class BreakIteratorResources
extends BreakIteratorResourceBundle {
    @Override
    protected ResourceBundle getBreakIteratorInfo() {
        return new BreakIteratorInfo();
    }
}

