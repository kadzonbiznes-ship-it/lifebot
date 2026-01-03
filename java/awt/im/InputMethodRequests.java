/*
 * Decompiled with CFR 0.152.
 */
package java.awt.im;

import java.awt.Rectangle;
import java.awt.font.TextHitInfo;
import java.text.AttributedCharacterIterator;

public interface InputMethodRequests {
    public Rectangle getTextLocation(TextHitInfo var1);

    public TextHitInfo getLocationOffset(int var1, int var2);

    public int getInsertPositionOffset();

    public AttributedCharacterIterator getCommittedText(int var1, int var2, AttributedCharacterIterator.Attribute[] var3);

    public int getCommittedTextLength();

    public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] var1);

    public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] var1);
}

