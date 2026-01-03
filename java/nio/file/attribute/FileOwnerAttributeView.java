/*
 * Decompiled with CFR 0.152.
 */
package java.nio.file.attribute;

import java.io.IOException;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.UserPrincipal;

public interface FileOwnerAttributeView
extends FileAttributeView {
    @Override
    public String name();

    public UserPrincipal getOwner() throws IOException;

    public void setOwner(UserPrincipal var1) throws IOException;
}

