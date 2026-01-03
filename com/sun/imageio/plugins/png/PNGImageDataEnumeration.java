/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.png;

import com.sun.imageio.plugins.common.InputStreamAdapter;
import com.sun.imageio.plugins.common.SubImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.imageio.stream.ImageInputStream;

class PNGImageDataEnumeration
implements Enumeration<InputStream> {
    boolean firstTime = true;
    ImageInputStream stream;
    int length;

    public PNGImageDataEnumeration(ImageInputStream stream) throws IOException {
        this.stream = stream;
        this.length = stream.readInt();
        int type = stream.readInt();
    }

    @Override
    public InputStream nextElement() {
        try {
            this.firstTime = false;
            SubImageInputStream iis = new SubImageInputStream(this.stream, this.length);
            return new InputStreamAdapter(iis);
        }
        catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean hasMoreElements() {
        if (this.firstTime) {
            return true;
        }
        try {
            int crc = this.stream.readInt();
            this.length = this.stream.readInt();
            int type = this.stream.readInt();
            return type == 1229209940;
        }
        catch (IOException e) {
            return false;
        }
    }
}

