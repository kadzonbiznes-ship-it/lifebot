/*
 * Decompiled with CFR 0.152.
 */
package org.jcp.xml.dsig.internal.dom;

import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jcp.xml.dsig.internal.dom.DOMBase64Transform;
import org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method;
import org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod;
import org.jcp.xml.dsig.internal.dom.DOMEnvelopedTransform;
import org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod;
import org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory;
import org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory;
import org.jcp.xml.dsig.internal.dom.DOMXPathFilter2Transform;
import org.jcp.xml.dsig.internal.dom.DOMXPathTransform;
import org.jcp.xml.dsig.internal.dom.DOMXSLTTransform;

public final class XMLDSigRI
extends Provider {
    static final long serialVersionUID = -5049765099299494554L;
    private static final String INFO = "XMLDSig (DOM XMLSignatureFactory; DOM KeyInfoFactory; C14N 1.0, C14N 1.1, Exclusive C14N, Base64, Enveloped, XPath, XPath2, XSLT TransformServices)";
    private static final String VER = AccessController.doPrivileged(new PrivilegedAction<String>(){

        @Override
        public String run() {
            return System.getProperty("java.specification.version");
        }
    });

    public XMLDSigRI() {
        super("XMLDSig", VER, INFO);
        final XMLDSigRI p = this;
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                HashMap<String, String> MECH_TYPE = new HashMap<String, String>();
                MECH_TYPE.put("MechanismType", "DOM");
                XMLDSigRI.this.putService(new ProviderService(p, "XMLSignatureFactory", "DOM", "org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory"));
                XMLDSigRI.this.putService(new ProviderService(p, "KeyInfoFactory", "DOM", "org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory"));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315", "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod", new String[]{"INCLUSIVE"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments", "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod", new String[]{"INCLUSIVE_WITH_COMMENTS"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/2006/12/xml-c14n11", "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method", null, (Map<String, String>)MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/2006/12/xml-c14n11#WithComments", "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method", null, (Map<String, String>)MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/2001/10/xml-exc-c14n#", "org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod", new String[]{"EXCLUSIVE"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/2001/10/xml-exc-c14n#WithComments", "org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod", new String[]{"EXCLUSIVE_WITH_COMMENTS"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/2000/09/xmldsig#base64", "org.jcp.xml.dsig.internal.dom.DOMBase64Transform", new String[]{"BASE64"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/2000/09/xmldsig#enveloped-signature", "org.jcp.xml.dsig.internal.dom.DOMEnvelopedTransform", new String[]{"ENVELOPED"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/2002/06/xmldsig-filter2", "org.jcp.xml.dsig.internal.dom.DOMXPathFilter2Transform", new String[]{"XPATH2"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/TR/1999/REC-xpath-19991116", "org.jcp.xml.dsig.internal.dom.DOMXPathTransform", new String[]{"XPATH"}, MECH_TYPE));
                XMLDSigRI.this.putService(new ProviderService(p, "TransformService", "http://www.w3.org/TR/1999/REC-xslt-19991116", "org.jcp.xml.dsig.internal.dom.DOMXSLTTransform", new String[]{"XSLT"}, MECH_TYPE));
                return null;
            }
        });
    }

    private static final class ProviderService
    extends Provider.Service {
        ProviderService(Provider p, String type, String algo, String cn) {
            super(p, type, algo, cn, null, null);
        }

        ProviderService(Provider p, String type, String algo, String cn, String[] aliases) {
            super(p, type, algo, cn, aliases == null ? null : Arrays.asList(aliases), null);
        }

        ProviderService(Provider p, String type, String algo, String cn, String[] aliases, Map<String, String> attrs) {
            super(p, type, algo, cn, aliases == null ? null : Arrays.asList(aliases), attrs);
        }

        @Override
        public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException {
            String type = this.getType();
            if (ctrParamObj != null) {
                throw new InvalidParameterException("constructorParameter not used with " + type + " engines");
            }
            String algo = this.getAlgorithm();
            try {
                if ("XMLSignatureFactory".equals(type)) {
                    if ("DOM".equals(algo)) {
                        return new DOMXMLSignatureFactory();
                    }
                } else if ("KeyInfoFactory".equals(type)) {
                    if ("DOM".equals(algo)) {
                        return new DOMKeyInfoFactory();
                    }
                } else if ("TransformService".equals(type)) {
                    if (algo.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315") || algo.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments")) {
                        return new DOMCanonicalXMLC14NMethod();
                    }
                    if ("http://www.w3.org/2006/12/xml-c14n11".equals(algo) || "http://www.w3.org/2006/12/xml-c14n11#WithComments".equals(algo)) {
                        return new DOMCanonicalXMLC14N11Method();
                    }
                    if (algo.equals("http://www.w3.org/2001/10/xml-exc-c14n#") || algo.equals("http://www.w3.org/2001/10/xml-exc-c14n#WithComments")) {
                        return new DOMExcC14NMethod();
                    }
                    if (algo.equals("http://www.w3.org/2000/09/xmldsig#base64")) {
                        return new DOMBase64Transform();
                    }
                    if (algo.equals("http://www.w3.org/2000/09/xmldsig#enveloped-signature")) {
                        return new DOMEnvelopedTransform();
                    }
                    if (algo.equals("http://www.w3.org/2002/06/xmldsig-filter2")) {
                        return new DOMXPathFilter2Transform();
                    }
                    if (algo.equals("http://www.w3.org/TR/1999/REC-xpath-19991116")) {
                        return new DOMXPathTransform();
                    }
                    if (algo.equals("http://www.w3.org/TR/1999/REC-xslt-19991116")) {
                        return new DOMXSLTTransform();
                    }
                }
            }
            catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using XMLDSig", ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }
}

