/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.synth;

import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;

public interface SynthUI
extends SynthConstants {
    public SynthContext getContext(JComponent var1);

    public void paintBorder(SynthContext var1, Graphics var2, int var3, int var4, int var5, int var6);
}

