/*
 * Decompiled with CFR 0.152.
 */
package sun.security.smartcardio;

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactorySpi;
import sun.security.smartcardio.PCSC;
import sun.security.smartcardio.PCSCException;
import sun.security.smartcardio.PCSCTerminals;
import sun.security.util.SecurityConstants;

public final class SunPCSC
extends Provider {
    private static final long serialVersionUID = 6168388284028876579L;

    public SunPCSC() {
        super("SunPCSC", SecurityConstants.PROVIDER_VER, "Sun PC/SC provider");
        final SunPCSC p = this;
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                SunPCSC.this.putService(new ProviderService(p, "TerminalFactory", "PC/SC", "sun.security.smartcardio.SunPCSC$Factory"));
                return null;
            }
        });
    }

    public static final class Factory
    extends TerminalFactorySpi {
        public Factory(Object obj) throws PCSCException {
            if (obj != null) {
                throw new IllegalArgumentException("SunPCSC factory does not use parameters");
            }
            PCSC.checkAvailable();
            PCSCTerminals.initContext();
        }

        @Override
        protected CardTerminals engineTerminals() {
            return new PCSCTerminals();
        }
    }

    private static final class ProviderService
    extends Provider.Service {
        ProviderService(Provider p, String type, String algo, String cn) {
            super(p, type, algo, cn, null, null);
        }

        @Override
        public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException {
            String type = this.getType();
            String algo = this.getAlgorithm();
            try {
                if (type.equals("TerminalFactory") && algo.equals("PC/SC")) {
                    return new Factory(ctrParamObj);
                }
            }
            catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using SunPCSC", ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }
}

