/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm;

import java.awt.color.CMMException;
import java.awt.color.ICC_Profile;
import java.security.AccessController;
import sun.java2d.cmm.CMMServiceProvider;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.Profile;
import sun.java2d.cmm.lcms.LcmsServiceProvider;
import sun.security.action.GetPropertyAction;

public final class CMSManager {
    private static volatile PCMM cmmImpl;

    public static PCMM getModule() {
        PCMM loc = cmmImpl;
        return loc != null ? loc : CMSManager.createModule();
    }

    private static synchronized PCMM createModule() {
        if (cmmImpl != null) {
            return cmmImpl;
        }
        GetPropertyAction gpa = new GetPropertyAction("sun.java2d.cmm");
        String cmmProviderClass = AccessController.doPrivileged(gpa);
        CMMServiceProvider provider = null;
        if (cmmProviderClass != null) {
            try {
                Class<?> cls = Class.forName(cmmProviderClass);
                provider = (CMMServiceProvider)cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (ReflectiveOperationException cls) {
                // empty catch block
            }
        }
        if (provider == null) {
            provider = new LcmsServiceProvider();
        }
        if ((cmmImpl = provider.getColorManagementModule()) == null) {
            throw new CMMException("Cannot initialize Color Management System.No CM module found");
        }
        gpa = new GetPropertyAction("sun.java2d.cmm.trace");
        String cmmTrace = AccessController.doPrivileged(gpa);
        if (cmmTrace != null) {
            cmmImpl = new CMMTracer(cmmImpl);
        }
        return cmmImpl;
    }

    static synchronized boolean canCreateModule() {
        return cmmImpl == null;
    }

    public static class CMMTracer
    implements PCMM {
        PCMM tcmm;
        String cName;

        public CMMTracer(PCMM tcmm) {
            this.tcmm = tcmm;
            this.cName = tcmm.getClass().getName();
        }

        @Override
        public Profile loadProfile(byte[] data) {
            System.err.print(this.cName + ".loadProfile");
            Profile p = this.tcmm.loadProfile(data);
            System.err.printf("(ID=%s)\n", p.toString());
            return p;
        }

        @Override
        public byte[] getProfileData(Profile p) {
            System.err.print(this.cName + ".getProfileData(ID=" + String.valueOf(p) + ") ");
            byte[] data = this.tcmm.getProfileData(p);
            System.err.println("requested " + data.length + " byte(s)");
            return data;
        }

        @Override
        public byte[] getTagData(Profile p, int tagSignature) {
            System.err.printf(this.cName + ".getTagData(ID=%x, TagSig=%s)", p, CMMTracer.signatureToString(tagSignature));
            byte[] data = this.tcmm.getTagData(p, tagSignature);
            System.err.println(" requested " + data.length + " byte(s)");
            return data;
        }

        @Override
        public void setTagData(Profile p, int tagSignature, byte[] data) {
            System.err.print(this.cName + ".setTagData(ID=" + String.valueOf(p) + ", TagSig=" + tagSignature + ")");
            System.err.println(" sending " + data.length + " byte(s)");
            this.tcmm.setTagData(p, tagSignature, data);
        }

        @Override
        public ColorTransform createTransform(int renderingIntent, ICC_Profile ... profiles) {
            System.err.println(this.cName + ".createTransform(int, ICC_Profile...)");
            return this.tcmm.createTransform(renderingIntent, profiles);
        }

        private static String signatureToString(int sig) {
            return String.format("%c%c%c%c", Character.valueOf((char)(0xFF & sig >> 24)), Character.valueOf((char)(0xFF & sig >> 16)), Character.valueOf((char)(0xFF & sig >> 8)), Character.valueOf((char)(0xFF & sig)));
        }
    }
}

