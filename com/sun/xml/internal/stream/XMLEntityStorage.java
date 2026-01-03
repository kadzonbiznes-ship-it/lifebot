/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.xml.internal.stream.Entity;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import jdk.xml.internal.SecuritySupport;

public class XMLEntityStorage {
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
    protected boolean fWarnDuplicateEntityDef;
    protected Map<String, Entity> fEntities = new HashMap<String, Entity>();
    protected Entity.ScannedEntity fCurrentEntity;
    private XMLEntityManager fEntityManager;
    protected XMLErrorReporter fErrorReporter;
    protected PropertyManager fPropertyManager;
    protected boolean fInExternalSubset = false;
    private static String gUserDir;
    private static String gEscapedUserDir;
    private static boolean[] gNeedEscaping;
    private static char[] gAfterEscaping1;
    private static char[] gAfterEscaping2;
    private static char[] gHexChs;

    public XMLEntityStorage(PropertyManager propertyManager) {
        this.fPropertyManager = propertyManager;
    }

    public XMLEntityStorage(XMLEntityManager entityManager) {
        this.fEntityManager = entityManager;
    }

    public void reset(PropertyManager propertyManager) {
        this.fErrorReporter = (XMLErrorReporter)propertyManager.getProperty(ERROR_REPORTER);
        this.fEntities.clear();
        this.fCurrentEntity = null;
    }

    public void reset() {
        this.fEntities.clear();
        this.fCurrentEntity = null;
    }

    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        this.fWarnDuplicateEntityDef = componentManager.getFeature(WARN_ON_DUPLICATE_ENTITYDEF, false);
        this.fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        this.fEntities.clear();
        this.fCurrentEntity = null;
    }

    public Entity getEntity(String name) {
        return this.fEntities.get(name);
    }

    public Map<String, Entity> getEntities() {
        return this.fEntities;
    }

    public void addInternalEntity(String name, String text) {
        if (!this.fEntities.containsKey(name)) {
            Entity.InternalEntity entity = new Entity.InternalEntity(name, text, this.fInExternalSubset);
            this.fEntities.put(name, entity);
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{name}, (short)0);
        }
    }

    public void addExternalEntity(String name, String publicId, String literalSystemId, String baseSystemId) {
        if (!this.fEntities.containsKey(name)) {
            if (baseSystemId == null && this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null) {
                baseSystemId = this.fCurrentEntity.entityLocation.getExpandedSystemId();
            }
            this.fCurrentEntity = this.fEntityManager.getCurrentEntity();
            Entity.ExternalEntity entity = new Entity.ExternalEntity(name, new XMLResourceIdentifierImpl(publicId, literalSystemId, baseSystemId, XMLEntityStorage.expandSystemId(literalSystemId, baseSystemId)), null, this.fInExternalSubset);
            this.fEntities.put(name, entity);
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{name}, (short)0);
        }
    }

    public boolean isExternalEntity(String entityName) {
        Entity entity = this.fEntities.get(entityName);
        if (entity == null) {
            return false;
        }
        return entity.isExternal();
    }

    public boolean isEntityDeclInExternalSubset(String entityName) {
        Entity entity = this.fEntities.get(entityName);
        if (entity == null) {
            return false;
        }
        return entity.isEntityDeclInExternalSubset();
    }

    public void addUnparsedEntity(String name, String publicId, String systemId, String baseSystemId, String notation) {
        this.fCurrentEntity = this.fEntityManager.getCurrentEntity();
        if (!this.fEntities.containsKey(name)) {
            Entity.ExternalEntity entity = new Entity.ExternalEntity(name, new XMLResourceIdentifierImpl(publicId, systemId, baseSystemId, null), notation, this.fInExternalSubset);
            this.fEntities.put(name, entity);
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{name}, (short)0);
        }
    }

    public boolean isUnparsedEntity(String entityName) {
        Entity entity = this.fEntities.get(entityName);
        if (entity == null) {
            return false;
        }
        return entity.isUnparsed();
    }

    public boolean isDeclaredEntity(String entityName) {
        Entity entity = this.fEntities.get(entityName);
        return entity != null;
    }

    public static String expandSystemId(String systemId) {
        return XMLEntityStorage.expandSystemId(systemId, null);
    }

    private static synchronized String getUserDir() {
        int i;
        int ch;
        String userDir = "";
        try {
            userDir = SecuritySupport.getSystemProperty("user.dir");
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        if (userDir.length() == 0) {
            return "";
        }
        if (userDir.equals(gUserDir)) {
            return gEscapedUserDir;
        }
        gUserDir = userDir;
        char separator = File.separatorChar;
        userDir = userDir.replace(separator, '/');
        int len = userDir.length();
        StringBuilder buffer = new StringBuilder(len * 3);
        if (len >= 2 && userDir.charAt(1) == ':' && (ch = Character.toUpperCase(userDir.charAt(0))) >= 65 && ch <= 90) {
            buffer.append('/');
        }
        for (i = 0; i < len && (ch = userDir.charAt(i)) < 128; ++i) {
            if (gNeedEscaping[ch]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
                continue;
            }
            buffer.append((char)ch);
        }
        if (i < len) {
            byte[] bytes = null;
            try {
                bytes = userDir.substring(i).getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                return userDir;
            }
            for (byte b : bytes) {
                if (b < 0) {
                    ch = b + 256;
                    buffer.append('%');
                    buffer.append(gHexChs[ch >> 4]);
                    buffer.append(gHexChs[ch & 0xF]);
                    continue;
                }
                if (gNeedEscaping[b]) {
                    buffer.append('%');
                    buffer.append(gAfterEscaping1[b]);
                    buffer.append(gAfterEscaping2[b]);
                    continue;
                }
                buffer.append((char)b);
            }
        }
        if (!userDir.endsWith("/")) {
            buffer.append('/');
        }
        gEscapedUserDir = buffer.toString();
        return gEscapedUserDir;
    }

    public static String expandSystemId(String systemId, String baseSystemId) {
        if (systemId == null || systemId.length() == 0) {
            return systemId;
        }
        try {
            new URI(systemId);
            return systemId;
        }
        catch (URI.MalformedURIException malformedURIException) {
            String id = XMLEntityStorage.fixURI(systemId);
            URI base = null;
            URI uri = null;
            try {
                block11: {
                    if (baseSystemId == null || baseSystemId.length() == 0 || baseSystemId.equals(systemId)) {
                        String dir = XMLEntityStorage.getUserDir();
                        base = new URI("file", "", dir, null, null);
                    } else {
                        try {
                            base = new URI(XMLEntityStorage.fixURI(baseSystemId));
                        }
                        catch (URI.MalformedURIException e) {
                            if (baseSystemId.indexOf(58) != -1) {
                                base = new URI("file", "", XMLEntityStorage.fixURI(baseSystemId), null, null);
                                break block11;
                            }
                            Object dir = XMLEntityStorage.getUserDir();
                            dir = (String)dir + XMLEntityStorage.fixURI(baseSystemId);
                            base = new URI("file", "", (String)dir, null, null);
                        }
                    }
                }
                uri = new URI(base, id);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (uri == null) {
                return systemId;
            }
            return uri.toString();
        }
    }

    protected static String fixURI(String str) {
        if (((String)(str = ((String)str).replace(File.separatorChar, '/'))).length() >= 2) {
            char ch1 = ((String)str).charAt(1);
            if (ch1 == ':') {
                char ch0 = Character.toUpperCase(((String)str).charAt(0));
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    str = "/" + (String)str;
                }
            } else if (ch1 == '/' && ((String)str).charAt(0) == '/') {
                str = "file:" + (String)str;
            }
        }
        return str;
    }

    public void startExternalSubset() {
        this.fInExternalSubset = true;
    }

    public void endExternalSubset() {
        this.fInExternalSubset = false;
    }

    static {
        gNeedEscaping = new boolean[128];
        gAfterEscaping1 = new char[128];
        gAfterEscaping2 = new char[128];
        gHexChs = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i <= 31; ++i) {
            XMLEntityStorage.gNeedEscaping[i] = true;
            XMLEntityStorage.gAfterEscaping1[i] = gHexChs[i >> 4];
            XMLEntityStorage.gAfterEscaping2[i] = gHexChs[i & 0xF];
        }
        XMLEntityStorage.gNeedEscaping[127] = true;
        XMLEntityStorage.gAfterEscaping1[127] = 55;
        XMLEntityStorage.gAfterEscaping2[127] = 70;
        for (char ch : new char[]{' ', '<', '>', '#', '%', '\"', '{', '}', '|', '\\', '^', '~', '[', ']', '`'}) {
            XMLEntityStorage.gNeedEscaping[ch] = true;
            XMLEntityStorage.gAfterEscaping1[ch] = gHexChs[ch >> 4];
            XMLEntityStorage.gAfterEscaping2[ch] = gHexChs[ch & 0xF];
        }
    }
}

