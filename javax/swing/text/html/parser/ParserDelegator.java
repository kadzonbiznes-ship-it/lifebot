/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import sun.awt.AppContext;

public class ParserDelegator
extends HTMLEditorKit.Parser
implements Serializable {
    private static final Object DTD_KEY = new Object();

    protected static void setDefaultDTD() {
        ParserDelegator.getDefaultDTD();
    }

    private static synchronized DTD getDefaultDTD() {
        AppContext appContext = AppContext.getAppContext();
        DTD dtd = (DTD)appContext.get(DTD_KEY);
        if (dtd == null) {
            DTD _dtd = null;
            String nm = "html32";
            try {
                _dtd = DTD.getDTD(nm);
            }
            catch (IOException e) {
                System.out.println("Throw an exception: could not get default dtd: " + nm);
            }
            dtd = ParserDelegator.createDTD(_dtd, nm);
            appContext.put(DTD_KEY, dtd);
        }
        return dtd;
    }

    protected static DTD createDTD(DTD dtd, String name) {
        InputStream in = null;
        boolean debug = true;
        try {
            String path = name + ".bdtd";
            in = ParserDelegator.getResourceAsStream(path);
            if (in != null) {
                dtd.read(new DataInputStream(new BufferedInputStream(in)));
                DTD.putDTDHash(name, dtd);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return dtd;
    }

    public ParserDelegator() {
        ParserDelegator.setDefaultDTD();
    }

    @Override
    public void parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharSet) throws IOException {
        new DocumentParser(ParserDelegator.getDefaultDTD()).parse(r, cb, ignoreCharSet);
    }

    static InputStream getResourceAsStream(final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<InputStream>(){

            @Override
            public InputStream run() {
                return ParserDelegator.class.getResourceAsStream(name);
            }
        });
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        ParserDelegator.setDefaultDTD();
    }
}

