/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XML11EntityScanner;
import com.sun.org.apache.xerces.internal.impl.XMLEntityHandler;
import com.sun.org.apache.xerces.internal.impl.XMLEntityScanner;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.io.UCSReader;
import com.sun.org.apache.xerces.internal.impl.io.UTF16Reader;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import com.sun.org.apache.xerces.internal.util.EncodingMap;
import com.sun.org.apache.xerces.internal.util.HTTPInputSource;
import com.sun.org.apache.xerces.internal.util.MessageFormatter;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLEntityDescriptionImpl;
import com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.xml.internal.stream.Entity;
import com.sun.xml.internal.stream.StaxEntityResolverWrapper;
import com.sun.xml.internal.stream.StaxXMLInputSource;
import com.sun.xml.internal.stream.XMLEntityStorage;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.transform.Source;
import jdk.xml.internal.JdkConstants;
import jdk.xml.internal.JdkXmlUtils;
import jdk.xml.internal.SecuritySupport;
import org.xml.sax.InputSource;

public class XMLEntityManager
implements XMLComponent,
XMLEntityResolver {
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int DEFAULT_XMLDECL_BUFFER_SIZE = 64;
    public static final int DEFAULT_INTERNAL_BUFFER_SIZE = 1024;
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected boolean fStrictURI;
    protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
    protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String STANDARD_URI_CONFORMANT = "http://apache.org/xml/features/standard-uri-conformant";
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String STAX_ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/stax-entity-resolver";
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final String BUFFER_SIZE = "http://apache.org/xml/properties/input-buffer-size";
    protected static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "jdk.xml.xmlSecurityPropertyManager";
    static final String EXTERNAL_ACCESS_DEFAULT = "all";
    private static final String[] RECOGNIZED_FEATURES = new String[]{"http://xml.org/sax/features/validation", "http://xml.org/sax/features/external-general-entities", "http://xml.org/sax/features/external-parameter-entities", "http://apache.org/xml/features/allow-java-encodings", "http://apache.org/xml/features/warn-on-duplicate-entitydef", "http://apache.org/xml/features/standard-uri-conformant", "http://javax.xml.XMLConstants/feature/useCatalog"};
    private static final Boolean[] FEATURE_DEFAULTS = new Boolean[]{null, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, JdkXmlUtils.USE_CATALOG_DEFAULT};
    private static final String[] RECOGNIZED_PROPERTIES = new String[]{"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/internal/validation-manager", "http://apache.org/xml/properties/input-buffer-size", "http://apache.org/xml/properties/security-manager", "jdk.xml.xmlSecurityPropertyManager", JdkXmlUtils.CATALOG_DEFER, JdkXmlUtils.CATALOG_FILES, JdkXmlUtils.CATALOG_PREFER, JdkXmlUtils.CATALOG_RESOLVE, "jdk.xml.cdataChunkSize"};
    private static final Object[] PROPERTY_DEFAULTS = new Object[]{null, null, null, null, 8192, null, null, null, null, null, null, JdkConstants.CDATA_CHUNK_SIZE_DEFAULT};
    private static final String XMLEntity = "[xml]".intern();
    private static final String DTDEntity = "[dtd]".intern();
    private static final boolean DEBUG_BUFFER = false;
    protected boolean fWarnDuplicateEntityDef;
    private static final boolean DEBUG_ENTITIES = false;
    private static final boolean DEBUG_ENCODINGS = false;
    private static final boolean DEBUG_RESOLVER = false;
    protected boolean fValidation;
    protected boolean fExternalGeneralEntities;
    protected boolean fExternalParameterEntities;
    protected boolean fAllowJavaEncodings = true;
    protected boolean fLoadExternalDTD = true;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityResolver fEntityResolver;
    protected StaxEntityResolverWrapper fStaxEntityResolver;
    protected PropertyManager fPropertyManager;
    boolean fSupportDTD = true;
    boolean fReplaceEntityReferences = true;
    boolean fSupportExternalEntities = true;
    protected String fAccessExternalDTD = "all";
    protected ValidationManager fValidationManager;
    protected int fBufferSize = 8192;
    protected XMLSecurityManager fSecurityManager = null;
    protected XMLLimitAnalyzer fLimitAnalyzer = null;
    protected int entityExpansionIndex;
    protected boolean fStandalone;
    protected boolean fInExternalSubset = false;
    protected XMLEntityHandler fEntityHandler;
    protected XMLEntityScanner fEntityScanner;
    protected XMLEntityScanner fXML10EntityScanner;
    protected XMLEntityScanner fXML11EntityScanner;
    protected int fEntityExpansionCount = 0;
    protected Map<String, Entity> fEntities = new HashMap<String, Entity>();
    protected Stack<Entity> fEntityStack = new Stack();
    protected Entity.ScannedEntity fCurrentEntity = null;
    boolean fISCreatedByResolver = false;
    protected XMLEntityStorage fEntityStorage;
    protected final Object[] defaultEncoding = new Object[]{"UTF-8", null};
    private final XMLResourceIdentifierImpl fResourceIdentifier = new XMLResourceIdentifierImpl();
    private final Augmentations fEntityAugs = new AugmentationsImpl();
    private boolean fUseCatalog = true;
    CatalogFeatures fCatalogFeatures;
    CatalogResolver fCatalogResolver;
    private String fCatalogFile;
    private String fDefer;
    private String fPrefer;
    private String fResolve;
    protected Stack<Reader> fReaderStack = new Stack();
    private static String gUserDir;
    private static URI gUserDirURI;
    private static boolean[] gNeedEscaping;
    private static char[] gAfterEscaping1;
    private static char[] gAfterEscaping2;
    private static char[] gHexChs;

    public XMLEntityManager() {
        this.fSecurityManager = new XMLSecurityManager(true);
        this.fEntityStorage = new XMLEntityStorage(this);
        this.setScannerVersion((short)1);
    }

    public XMLEntityManager(PropertyManager propertyManager) {
        this.fPropertyManager = propertyManager;
        this.fEntityStorage = new XMLEntityStorage(this);
        this.fEntityScanner = new XMLEntityScanner(propertyManager, this);
        this.reset(propertyManager);
    }

    public void addInternalEntity(String name, String text) {
        if (!this.fEntities.containsKey(name)) {
            Entity.InternalEntity entity = new Entity.InternalEntity(name, text, this.fInExternalSubset);
            this.fEntities.put(name, entity);
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{name}, (short)0);
        }
    }

    public void addExternalEntity(String name, String publicId, String literalSystemId, String baseSystemId) throws IOException {
        if (!this.fEntities.containsKey(name)) {
            if (baseSystemId == null) {
                int size = this.fEntityStack.size();
                if (size == 0 && this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null) {
                    baseSystemId = this.fCurrentEntity.entityLocation.getExpandedSystemId();
                }
                for (int i = size - 1; i >= 0; --i) {
                    Entity.ScannedEntity externalEntity = (Entity.ScannedEntity)this.fEntityStack.get(i);
                    if (externalEntity.entityLocation == null || externalEntity.entityLocation.getExpandedSystemId() == null) continue;
                    baseSystemId = externalEntity.entityLocation.getExpandedSystemId();
                    break;
                }
            }
            Entity.ExternalEntity entity = new Entity.ExternalEntity(name, new XMLEntityDescriptionImpl(name, publicId, literalSystemId, baseSystemId, XMLEntityManager.expandSystemId(literalSystemId, baseSystemId, false)), null, this.fInExternalSubset);
            this.fEntities.put(name, entity);
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{name}, (short)0);
        }
    }

    public void addUnparsedEntity(String name, String publicId, String systemId, String baseSystemId, String notation) {
        if (!this.fEntities.containsKey(name)) {
            Entity.ExternalEntity entity = new Entity.ExternalEntity(name, new XMLEntityDescriptionImpl(name, publicId, systemId, baseSystemId, null), notation, this.fInExternalSubset);
            this.fEntities.put(name, entity);
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{name}, (short)0);
        }
    }

    public XMLEntityStorage getEntityStore() {
        return this.fEntityStorage;
    }

    public XMLEntityScanner getEntityScanner() {
        if (this.fEntityScanner == null) {
            if (this.fXML10EntityScanner == null) {
                this.fXML10EntityScanner = new XMLEntityScanner();
            }
            this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
            this.fEntityScanner = this.fXML10EntityScanner;
        }
        return this.fEntityScanner;
    }

    public void setScannerVersion(short version) {
        if (version == 1) {
            if (this.fXML10EntityScanner == null) {
                this.fXML10EntityScanner = new XMLEntityScanner();
            }
            this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
            this.fEntityScanner = this.fXML10EntityScanner;
            this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
        } else {
            if (this.fXML11EntityScanner == null) {
                this.fXML11EntityScanner = new XML11EntityScanner();
            }
            this.fXML11EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
            this.fEntityScanner = this.fXML11EntityScanner;
            this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
        }
    }

    public String setupCurrentEntity(boolean reference, String name, XMLInputSource xmlInputSource, boolean literal, boolean isExternal) throws IOException, XNIException {
        String publicId = xmlInputSource.getPublicId();
        String literalSystemId = xmlInputSource.getSystemId();
        String baseSystemId = xmlInputSource.getBaseSystemId();
        String encoding = xmlInputSource.getEncoding();
        boolean encodingExternallySpecified = encoding != null;
        Boolean isBigEndian = null;
        InputStream stream = null;
        Reader reader = xmlInputSource.getCharacterStream();
        String expandedSystemId = XMLEntityManager.expandSystemId(literalSystemId, baseSystemId, this.fStrictURI);
        if (baseSystemId == null) {
            baseSystemId = expandedSystemId;
        }
        if (reader == null) {
            stream = xmlInputSource.getByteStream();
            if (stream == null) {
                URL location = new URL(expandedSystemId);
                URLConnection connect = location.openConnection();
                if (!(connect instanceof HttpURLConnection)) {
                    stream = connect.getInputStream();
                } else {
                    String redirect;
                    boolean followRedirects = true;
                    if (xmlInputSource instanceof HTTPInputSource) {
                        HttpURLConnection urlConnection = (HttpURLConnection)connect;
                        HTTPInputSource httpInputSource = (HTTPInputSource)xmlInputSource;
                        Iterator<Map.Entry<String, String>> propIter = httpInputSource.getHTTPRequestProperties();
                        while (propIter.hasNext()) {
                            Map.Entry<String, String> entry = propIter.next();
                            urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                        }
                        followRedirects = httpInputSource.getFollowHTTPRedirects();
                        if (!followRedirects) {
                            urlConnection.setInstanceFollowRedirects(followRedirects);
                        }
                    }
                    stream = connect.getInputStream();
                    if (followRedirects && !(redirect = connect.getURL().toString()).equals(expandedSystemId)) {
                        literalSystemId = redirect;
                        expandedSystemId = redirect;
                    }
                }
            }
            RewindableInputStream rewindableStream = new RewindableInputStream(stream);
            stream = rewindableStream;
            if (encoding == null) {
                b4 = new byte[4];
                for (count = 0; count < 4; ++count) {
                    b4[count] = (byte)rewindableStream.readAndBuffer();
                }
                if (count == 4) {
                    EncodingInfo info = this.getEncodingInfo((byte[])b4, count);
                    encoding = info.autoDetectedEncoding;
                    String readerEncoding = info.readerEncoding;
                    isBigEndian = info.isBigEndian;
                    stream.reset();
                    if (info.hasBOM) {
                        if ("UTF-8".equals(readerEncoding)) {
                            stream.skip(3L);
                        } else if ("UTF-16".equals(readerEncoding)) {
                            stream.skip(2L);
                        }
                    }
                    reader = this.createReader(stream, readerEncoding, isBigEndian);
                } else {
                    reader = this.createReader(stream, encoding, isBigEndian);
                }
            } else {
                if ("UTF-8".equals(encoding = encoding.toUpperCase(Locale.ENGLISH))) {
                    int[] b3 = new int[3];
                    for (count = 0; count < 3; ++count) {
                        b3[count] = rewindableStream.readAndBuffer();
                        if (b3[count] == -1) break;
                    }
                    if (count == 3) {
                        if (b3[0] != 239 || b3[1] != 187 || b3[2] != 191) {
                            stream.reset();
                        }
                    } else {
                        stream.reset();
                    }
                } else if ("UTF-16".equals(encoding)) {
                    b4 = new int[4];
                    for (count = 0; count < 4; ++count) {
                        b4[count] = rewindableStream.readAndBuffer();
                        if (b4[count] == -1) break;
                    }
                    stream.reset();
                    if (count >= 2) {
                        byte b0 = b4[0];
                        byte b1 = b4[1];
                        if (b0 == 254 && b1 == 255) {
                            isBigEndian = Boolean.TRUE;
                            stream.skip(2L);
                        } else if (b0 == 255 && b1 == 254) {
                            isBigEndian = Boolean.FALSE;
                            stream.skip(2L);
                        } else if (count == 4) {
                            byte b2 = b4[2];
                            byte b3 = b4[3];
                            if (b0 == 0 && b1 == 60 && b2 == 0 && b3 == 63) {
                                isBigEndian = Boolean.TRUE;
                            }
                            if (b0 == 60 && b1 == 0 && b2 == 63 && b3 == 0) {
                                isBigEndian = Boolean.FALSE;
                            }
                        }
                    }
                } else if ("ISO-10646-UCS-4".equals(encoding)) {
                    b4 = new int[4];
                    for (count = 0; count < 4; ++count) {
                        b4[count] = rewindableStream.readAndBuffer();
                        if (b4[count] == -1) break;
                    }
                    stream.reset();
                    if (count == 4) {
                        if (b4[0] == 0 && b4[1] == 0 && b4[2] == 0 && b4[3] == 60) {
                            isBigEndian = Boolean.TRUE;
                        } else if (b4[0] == 60 && b4[1] == 0 && b4[2] == 0 && b4[3] == 0) {
                            isBigEndian = Boolean.FALSE;
                        }
                    }
                } else if ("ISO-10646-UCS-2".equals(encoding)) {
                    b4 = new int[4];
                    for (count = 0; count < 4; ++count) {
                        b4[count] = rewindableStream.readAndBuffer();
                        if (b4[count] == -1) break;
                    }
                    stream.reset();
                    if (count == 4) {
                        if (b4[0] == 0 && b4[1] == 60 && b4[2] == 0 && b4[3] == 63) {
                            isBigEndian = Boolean.TRUE;
                        } else if (b4[0] == 60 && b4[1] == 0 && b4[2] == 63 && b4[3] == 0) {
                            isBigEndian = Boolean.FALSE;
                        }
                    }
                }
                reader = this.createReader(stream, encoding, isBigEndian);
            }
        }
        this.fReaderStack.push(reader);
        if (this.fCurrentEntity != null) {
            this.fEntityStack.push(this.fCurrentEntity);
        }
        this.fCurrentEntity = new Entity.ScannedEntity(reference, name, new XMLResourceIdentifierImpl(publicId, literalSystemId, baseSystemId, expandedSystemId), stream, reader, encoding, literal, encodingExternallySpecified, isExternal);
        this.fCurrentEntity.setEncodingExternallySpecified(encodingExternallySpecified);
        this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
        this.fResourceIdentifier.setValues(publicId, literalSystemId, baseSystemId, expandedSystemId);
        if (this.fLimitAnalyzer != null) {
            this.fLimitAnalyzer.startEntity(name);
        }
        return encoding;
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

    public void setStandalone(boolean standalone) {
        this.fStandalone = standalone;
    }

    public boolean isStandalone() {
        return this.fStandalone;
    }

    public boolean isDeclaredEntity(String entityName) {
        Entity entity = this.fEntities.get(entityName);
        return entity != null;
    }

    public boolean isUnparsedEntity(String entityName) {
        Entity entity = this.fEntities.get(entityName);
        if (entity == null) {
            return false;
        }
        return entity.isUnparsed();
    }

    public XMLResourceIdentifier getCurrentResourceIdentifier() {
        return this.fResourceIdentifier;
    }

    public void setEntityHandler(XMLEntityHandler entityHandler) {
        this.fEntityHandler = entityHandler;
    }

    public StaxXMLInputSource resolveEntityAsPerStax(XMLResourceIdentifier resourceIdentifier) throws IOException {
        boolean needExpand;
        if (resourceIdentifier == null) {
            return null;
        }
        String publicId = resourceIdentifier.getPublicId();
        String literalSystemId = resourceIdentifier.getLiteralSystemId();
        String baseSystemId = resourceIdentifier.getBaseSystemId();
        String expandedSystemId = resourceIdentifier.getExpandedSystemId();
        boolean bl = needExpand = expandedSystemId == null;
        if (baseSystemId == null && this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null && (baseSystemId = this.fCurrentEntity.entityLocation.getExpandedSystemId()) != null) {
            needExpand = true;
        }
        if (needExpand) {
            expandedSystemId = XMLEntityManager.expandSystemId(literalSystemId, baseSystemId, false);
        }
        StaxXMLInputSource staxInputSource = null;
        XMLInputSource xmlInputSource = null;
        XMLResourceIdentifierImpl ri = null;
        if (resourceIdentifier instanceof XMLResourceIdentifierImpl) {
            ri = (XMLResourceIdentifierImpl)resourceIdentifier;
        } else {
            this.fResourceIdentifier.clear();
            ri = this.fResourceIdentifier;
        }
        ri.setValues(publicId, literalSystemId, baseSystemId, expandedSystemId);
        this.fISCreatedByResolver = false;
        if (this.fStaxEntityResolver != null && (staxInputSource = this.fStaxEntityResolver.resolveEntity(ri)) != null) {
            this.fISCreatedByResolver = true;
        }
        if (this.fEntityResolver != null && (xmlInputSource = this.fEntityResolver.resolveEntity(ri)) != null) {
            this.fISCreatedByResolver = true;
        }
        if (xmlInputSource != null) {
            staxInputSource = new StaxXMLInputSource(xmlInputSource, this.fISCreatedByResolver);
        }
        if (staxInputSource == null && this.fUseCatalog) {
            if (this.fCatalogFeatures == null) {
                this.fCatalogFeatures = JdkXmlUtils.getCatalogFeatures(this.fDefer, this.fCatalogFile, this.fPrefer, this.fResolve);
            }
            this.fCatalogFile = this.fCatalogFeatures.get(CatalogFeatures.Feature.FILES);
            if (this.fCatalogFile != null) {
                try {
                    InputSource is;
                    if (this.fCatalogResolver == null) {
                        this.fCatalogResolver = CatalogManager.catalogResolver(this.fCatalogFeatures, new java.net.URI[0]);
                    }
                    if ((is = this.fCatalogResolver.resolveEntity(publicId, literalSystemId)) != null && !is.isEmpty()) {
                        staxInputSource = new StaxXMLInputSource(new XMLInputSource(is, true), true);
                    }
                }
                catch (CatalogException e) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "CatalogException", new Object[]{SecuritySupport.sanitizePath(this.fCatalogFile)}, (short)2, e);
                }
            }
        }
        if (staxInputSource == null) {
            staxInputSource = new StaxXMLInputSource(new XMLInputSource(publicId, literalSystemId, baseSystemId, true), false);
        } else if (staxInputSource.hasXMLStreamOrXMLEventReader()) {
            // empty if block
        }
        return staxInputSource;
    }

    @Override
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws IOException, XNIException {
        boolean needExpand;
        if (resourceIdentifier == null) {
            return null;
        }
        String publicId = resourceIdentifier.getPublicId();
        String literalSystemId = resourceIdentifier.getLiteralSystemId();
        String baseSystemId = resourceIdentifier.getBaseSystemId();
        String expandedSystemId = resourceIdentifier.getExpandedSystemId();
        boolean bl = needExpand = expandedSystemId == null;
        if (baseSystemId == null && this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null && (baseSystemId = this.fCurrentEntity.entityLocation.getExpandedSystemId()) != null) {
            needExpand = true;
        }
        if (needExpand) {
            expandedSystemId = XMLEntityManager.expandSystemId(literalSystemId, baseSystemId, false);
        }
        XMLInputSource xmlInputSource = null;
        if (this.fEntityResolver != null) {
            resourceIdentifier.setBaseSystemId(baseSystemId);
            resourceIdentifier.setExpandedSystemId(expandedSystemId);
            xmlInputSource = this.fEntityResolver.resolveEntity(resourceIdentifier);
        }
        if (xmlInputSource == null && this.fUseCatalog) {
            if (this.fCatalogFeatures == null) {
                this.fCatalogFeatures = JdkXmlUtils.getCatalogFeatures(this.fDefer, this.fCatalogFile, this.fPrefer, this.fResolve);
            }
            this.fCatalogFile = this.fCatalogFeatures.get(CatalogFeatures.Feature.FILES);
            if (this.fCatalogFile != null) {
                InputSource is = null;
                try {
                    String pid;
                    if (this.fCatalogResolver == null) {
                        this.fCatalogResolver = CatalogManager.catalogResolver(this.fCatalogFeatures, new java.net.URI[0]);
                    }
                    String string = pid = publicId != null ? publicId : resourceIdentifier.getNamespace();
                    if (pid != null || literalSystemId != null) {
                        is = this.fCatalogResolver.resolveEntity(pid, literalSystemId);
                    }
                }
                catch (CatalogException pid) {
                    // empty catch block
                }
                if (is != null && !is.isEmpty()) {
                    xmlInputSource = new XMLInputSource(is, true);
                } else if (literalSystemId != null) {
                    if (this.fCatalogResolver == null) {
                        this.fCatalogResolver = CatalogManager.catalogResolver(this.fCatalogFeatures, new java.net.URI[0]);
                    }
                    Source source = null;
                    try {
                        source = this.fCatalogResolver.resolve(literalSystemId, baseSystemId);
                    }
                    catch (CatalogException e) {
                        throw new XNIException(e);
                    }
                    if (source != null && !source.isEmpty()) {
                        xmlInputSource = new XMLInputSource(publicId, source.getSystemId(), baseSystemId, true);
                    }
                }
            }
        }
        if (xmlInputSource == null) {
            xmlInputSource = new XMLInputSource(publicId, literalSystemId, baseSystemId, false);
        }
        return xmlInputSource;
    }

    public void startEntity(boolean isGE, String entityName, boolean literal) throws IOException, XNIException {
        int size;
        Entity entity = this.fEntityStorage.getEntity(entityName);
        if (entity == null) {
            if (this.fEntityHandler != null) {
                String encoding = null;
                this.fResourceIdentifier.clear();
                this.fEntityAugs.removeAllItems();
                this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                this.fEntityHandler.startEntity(entityName, this.fResourceIdentifier, encoding, this.fEntityAugs);
                this.fEntityAugs.removeAllItems();
                this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                this.fEntityHandler.endEntity(entityName, this.fEntityAugs);
            }
            return;
        }
        boolean external = entity.isExternal();
        Entity.ExternalEntity externalEntity = null;
        String extLitSysId = null;
        String extBaseSysId = null;
        String expandedSystemId = null;
        if (external) {
            boolean general;
            externalEntity = (Entity.ExternalEntity)entity;
            extLitSysId = externalEntity.entityLocation != null ? externalEntity.entityLocation.getLiteralSystemId() : null;
            extBaseSysId = externalEntity.entityLocation != null ? externalEntity.entityLocation.getBaseSystemId() : null;
            expandedSystemId = XMLEntityManager.expandSystemId(extLitSysId, extBaseSysId, this.fStrictURI);
            boolean unparsed = entity.isUnparsed();
            boolean parameter = entityName.startsWith("%");
            boolean bl = general = !parameter;
            if (unparsed || general && !this.fExternalGeneralEntities || parameter && !this.fExternalParameterEntities || !this.fSupportDTD || !this.fSupportExternalEntities) {
                if (this.fEntityHandler != null) {
                    this.fResourceIdentifier.clear();
                    String encoding = null;
                    this.fResourceIdentifier.setValues(externalEntity.entityLocation != null ? externalEntity.entityLocation.getPublicId() : null, extLitSysId, extBaseSysId, expandedSystemId);
                    this.fEntityAugs.removeAllItems();
                    this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                    this.fEntityHandler.startEntity(entityName, this.fResourceIdentifier, encoding, this.fEntityAugs);
                    this.fEntityAugs.removeAllItems();
                    this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                    this.fEntityHandler.endEntity(entityName, this.fEntityAugs);
                }
                return;
            }
        }
        for (int i = size = this.fEntityStack.size(); i >= 0; --i) {
            Entity activeEntity;
            Entity entity2 = activeEntity = i == size ? this.fCurrentEntity : (Entity)this.fEntityStack.get(i);
            if (activeEntity.name != entityName) continue;
            Object path = entityName;
            for (int j = i + 1; j < size; ++j) {
                activeEntity = (Entity)this.fEntityStack.get(j);
                path = (String)path + " -> " + activeEntity.name;
            }
            path = (String)path + " -> " + this.fCurrentEntity.name;
            path = (String)path + " -> " + entityName;
            this.fErrorReporter.reportError(this.getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "RecursiveReference", new Object[]{entityName, path}, (short)2);
            if (this.fEntityHandler != null) {
                this.fResourceIdentifier.clear();
                String encoding = null;
                if (external) {
                    this.fResourceIdentifier.setValues(externalEntity.entityLocation != null ? externalEntity.entityLocation.getPublicId() : null, extLitSysId, extBaseSysId, expandedSystemId);
                }
                this.fEntityAugs.removeAllItems();
                this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                this.fEntityHandler.startEntity(entityName, this.fResourceIdentifier, encoding, this.fEntityAugs);
                this.fEntityAugs.removeAllItems();
                this.fEntityAugs.putItem("ENTITY_SKIPPED", Boolean.TRUE);
                this.fEntityHandler.endEntity(entityName, this.fEntityAugs);
            }
            return;
        }
        StaxXMLInputSource staxInputSource = null;
        XMLInputSource xmlInputSource = null;
        if (external) {
            String accessError;
            staxInputSource = this.resolveEntityAsPerStax(externalEntity.entityLocation);
            xmlInputSource = staxInputSource.getXMLInputSource();
            if (!this.fISCreatedByResolver && (accessError = SecuritySupport.checkAccess(expandedSystemId, this.fAccessExternalDTD, EXTERNAL_ACCESS_DEFAULT)) != null) {
                this.fErrorReporter.reportError(this.getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "AccessExternalEntity", new Object[]{SecuritySupport.sanitizePath(expandedSystemId), accessError}, (short)2);
            }
        } else {
            Entity.InternalEntity internalEntity = (Entity.InternalEntity)entity;
            StringReader reader = new StringReader(internalEntity.text);
            xmlInputSource = new XMLInputSource(null, null, null, reader, null);
        }
        this.startEntity(isGE, entityName, xmlInputSource, literal, external);
    }

    public void startDocumentEntity(XMLInputSource xmlInputSource) throws IOException, XNIException {
        this.startEntity(false, XMLEntity, xmlInputSource, false, true);
    }

    public void startDTDEntity(XMLInputSource xmlInputSource) throws IOException, XNIException {
        this.startEntity(false, DTDEntity, xmlInputSource, false, true);
    }

    public void startExternalSubset() {
        this.fInExternalSubset = true;
    }

    public void endExternalSubset() {
        this.fInExternalSubset = false;
    }

    public void startEntity(boolean isGE, String name, XMLInputSource xmlInputSource, boolean literal, boolean isExternal) throws IOException, XNIException {
        String encoding = this.setupCurrentEntity(isGE, name, xmlInputSource, literal, isExternal);
        ++this.fEntityExpansionCount;
        if (this.fLimitAnalyzer != null) {
            this.fLimitAnalyzer.addValue(this.entityExpansionIndex, name, 1);
        }
        if (this.fSecurityManager != null && this.fSecurityManager.isOverLimit(this.entityExpansionIndex, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EntityExpansionLimit", new Object[]{this.fSecurityManager.getLimitValueByIndex(this.entityExpansionIndex)}, (short)2);
            this.fEntityExpansionCount = 0;
        }
        if (this.fEntityHandler != null) {
            this.fEntityHandler.startEntity(name, this.fResourceIdentifier, encoding, null);
        }
    }

    public Entity.ScannedEntity getCurrentEntity() {
        return this.fCurrentEntity;
    }

    public Entity.ScannedEntity getTopLevelEntity() {
        return (Entity.ScannedEntity)(this.fEntityStack.empty() ? null : (Entity)this.fEntityStack.get(0));
    }

    public void closeReaders() {
        while (!this.fReaderStack.isEmpty()) {
            try {
                this.fReaderStack.pop().close();
            }
            catch (IOException iOException) {}
        }
    }

    public void endEntity() throws IOException, XNIException {
        Entity.ScannedEntity entity;
        Entity.ScannedEntity scannedEntity = entity = this.fEntityStack.size() > 0 ? (Entity.ScannedEntity)this.fEntityStack.pop() : null;
        if (this.fCurrentEntity != null) {
            try {
                if (this.fLimitAnalyzer != null) {
                    this.fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, this.fCurrentEntity.name);
                    if (this.fCurrentEntity.name.equals("[xml]")) {
                        this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
                    }
                }
                this.fCurrentEntity.close();
            }
            catch (IOException ex) {
                throw new XNIException(ex);
            }
        }
        if (!this.fReaderStack.isEmpty()) {
            this.fReaderStack.pop();
        }
        if (this.fEntityHandler != null) {
            if (entity == null) {
                this.fEntityAugs.removeAllItems();
                this.fEntityAugs.putItem("LAST_ENTITY", Boolean.TRUE);
                this.fEntityHandler.endEntity(this.fCurrentEntity.name, this.fEntityAugs);
                this.fEntityAugs.removeAllItems();
            } else {
                this.fEntityHandler.endEntity(this.fCurrentEntity.name, null);
            }
        }
        boolean documentEntity = this.fCurrentEntity.name == XMLEntity;
        this.fCurrentEntity = entity;
        this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
        if (this.fCurrentEntity == null & !documentEntity) {
            throw new EOFException();
        }
    }

    public void reset(PropertyManager propertyManager) {
        this.fSymbolTable = (SymbolTable)propertyManager.getProperty(SYMBOL_TABLE);
        this.fErrorReporter = (XMLErrorReporter)propertyManager.getProperty(ERROR_REPORTER);
        try {
            this.fStaxEntityResolver = (StaxEntityResolverWrapper)propertyManager.getProperty(STAX_ENTITY_RESOLVER);
        }
        catch (XMLConfigurationException e) {
            this.fStaxEntityResolver = null;
        }
        this.fSupportDTD = (Boolean)propertyManager.getProperty("javax.xml.stream.supportDTD");
        this.fReplaceEntityReferences = (Boolean)propertyManager.getProperty("javax.xml.stream.isReplacingEntityReferences");
        this.fSupportExternalEntities = (Boolean)propertyManager.getProperty("javax.xml.stream.isSupportingExternalEntities");
        this.fLoadExternalDTD = (Boolean)propertyManager.getProperty("http://java.sun.com/xml/stream/properties/ignore-external-dtd") == false;
        this.fUseCatalog = (Boolean)propertyManager.getProperty("http://javax.xml.XMLConstants/feature/useCatalog");
        this.fCatalogFile = (String)propertyManager.getProperty(JdkXmlUtils.CATALOG_FILES);
        this.fDefer = (String)propertyManager.getProperty(JdkXmlUtils.CATALOG_DEFER);
        this.fPrefer = (String)propertyManager.getProperty(JdkXmlUtils.CATALOG_PREFER);
        this.fResolve = (String)propertyManager.getProperty(JdkXmlUtils.CATALOG_RESOLVE);
        XMLSecurityPropertyManager spm = (XMLSecurityPropertyManager)propertyManager.getProperty(XML_SECURITY_PROPERTY_MANAGER);
        this.fAccessExternalDTD = spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fSecurityManager = (XMLSecurityManager)propertyManager.getProperty(SECURITY_MANAGER);
        this.fLimitAnalyzer = new XMLLimitAnalyzer();
        this.fEntityStorage.reset(propertyManager);
        this.fEntityScanner.reset(propertyManager);
        this.fEntities.clear();
        this.fEntityStack.removeAllElements();
        this.fCurrentEntity = null;
        this.fValidation = false;
        this.fExternalGeneralEntities = true;
        this.fExternalParameterEntities = true;
        this.fAllowJavaEncodings = true;
    }

    @Override
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        boolean parser_settings = componentManager.getFeature(PARSER_SETTINGS, true);
        if (!parser_settings) {
            this.reset();
            if (this.fEntityScanner != null) {
                this.fEntityScanner.reset(componentManager);
            }
            if (this.fEntityStorage != null) {
                this.fEntityStorage.reset(componentManager);
            }
            return;
        }
        this.fValidation = componentManager.getFeature(VALIDATION, false);
        this.fExternalGeneralEntities = componentManager.getFeature(EXTERNAL_GENERAL_ENTITIES, true);
        this.fExternalParameterEntities = componentManager.getFeature(EXTERNAL_PARAMETER_ENTITIES, true);
        this.fAllowJavaEncodings = componentManager.getFeature(ALLOW_JAVA_ENCODINGS, false);
        this.fWarnDuplicateEntityDef = componentManager.getFeature(WARN_ON_DUPLICATE_ENTITYDEF, false);
        this.fStrictURI = componentManager.getFeature(STANDARD_URI_CONFORMANT, false);
        this.fLoadExternalDTD = componentManager.getFeature(LOAD_EXTERNAL_DTD, true);
        this.fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        this.fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        this.fEntityResolver = (XMLEntityResolver)componentManager.getProperty(ENTITY_RESOLVER, null);
        this.fStaxEntityResolver = (StaxEntityResolverWrapper)componentManager.getProperty(STAX_ENTITY_RESOLVER, null);
        this.fValidationManager = (ValidationManager)componentManager.getProperty(VALIDATION_MANAGER, null);
        this.fSecurityManager = (XMLSecurityManager)componentManager.getProperty(SECURITY_MANAGER, null);
        this.entityExpansionIndex = this.fSecurityManager.getIndex("jdk.xml.entityExpansionLimit");
        this.fSupportDTD = true;
        this.fReplaceEntityReferences = true;
        this.fSupportExternalEntities = true;
        XMLSecurityPropertyManager spm = (XMLSecurityPropertyManager)componentManager.getProperty(XML_SECURITY_PROPERTY_MANAGER, null);
        if (spm == null) {
            spm = new XMLSecurityPropertyManager();
        }
        this.fAccessExternalDTD = spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fUseCatalog = componentManager.getFeature("http://javax.xml.XMLConstants/feature/useCatalog", true);
        this.fCatalogFile = (String)componentManager.getProperty(JdkXmlUtils.CATALOG_FILES);
        this.fDefer = (String)componentManager.getProperty(JdkXmlUtils.CATALOG_DEFER);
        this.fPrefer = (String)componentManager.getProperty(JdkXmlUtils.CATALOG_PREFER);
        this.fResolve = (String)componentManager.getProperty(JdkXmlUtils.CATALOG_RESOLVE);
        this.reset();
        this.fEntityScanner.reset(componentManager);
        this.fEntityStorage.reset(componentManager);
    }

    public void reset() {
        this.fLimitAnalyzer = new XMLLimitAnalyzer();
        this.fStandalone = false;
        this.fEntities.clear();
        this.fEntityStack.removeAllElements();
        this.fEntityExpansionCount = 0;
        this.fCurrentEntity = null;
        if (this.fXML10EntityScanner != null) {
            this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
        }
        if (this.fXML11EntityScanner != null) {
            this.fXML11EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
        }
        this.fEntityHandler = null;
    }

    @Override
    public String[] getRecognizedFeatures() {
        return (String[])RECOGNIZED_FEATURES.clone();
    }

    @Override
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
        if (featureId.startsWith("http://apache.org/xml/features/")) {
            int suffixLength = featureId.length() - "http://apache.org/xml/features/".length();
            if (suffixLength == "allow-java-encodings".length() && featureId.endsWith("allow-java-encodings")) {
                this.fAllowJavaEncodings = state;
            }
            if (suffixLength == "nonvalidating/load-external-dtd".length() && featureId.endsWith("nonvalidating/load-external-dtd")) {
                this.fLoadExternalDTD = state;
                return;
            }
        } else if (featureId.equals("http://javax.xml.XMLConstants/feature/useCatalog")) {
            this.fUseCatalog = state;
        }
    }

    @Override
    public void setProperty(String propertyId, Object value) {
        if (propertyId.startsWith("http://apache.org/xml/properties/")) {
            Integer bufferSize;
            int suffixLength = propertyId.length() - "http://apache.org/xml/properties/".length();
            if (suffixLength == "internal/symbol-table".length() && propertyId.endsWith("internal/symbol-table")) {
                this.fSymbolTable = (SymbolTable)value;
                return;
            }
            if (suffixLength == "internal/error-reporter".length() && propertyId.endsWith("internal/error-reporter")) {
                this.fErrorReporter = (XMLErrorReporter)value;
                return;
            }
            if (suffixLength == "internal/entity-resolver".length() && propertyId.endsWith("internal/entity-resolver")) {
                this.fEntityResolver = (XMLEntityResolver)value;
                return;
            }
            if (suffixLength == "input-buffer-size".length() && propertyId.endsWith("input-buffer-size") && (bufferSize = (Integer)value) != null && bufferSize > 64) {
                this.fBufferSize = bufferSize;
                this.fEntityScanner.setBufferSize(this.fBufferSize);
            }
            if (suffixLength == "security-manager".length() && propertyId.endsWith("security-manager")) {
                this.fSecurityManager = (XMLSecurityManager)value;
            }
        }
        if (propertyId.equals(XML_SECURITY_PROPERTY_MANAGER)) {
            XMLSecurityPropertyManager spm = (XMLSecurityPropertyManager)value;
            this.fAccessExternalDTD = spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
            return;
        }
        if (propertyId.equals(JdkXmlUtils.CATALOG_FILES)) {
            this.fCatalogFile = (String)value;
        } else if (propertyId.equals(JdkXmlUtils.CATALOG_DEFER)) {
            this.fDefer = (String)value;
        } else if (propertyId.equals(JdkXmlUtils.CATALOG_PREFER)) {
            this.fPrefer = (String)value;
        } else if (propertyId.equals(JdkXmlUtils.CATALOG_RESOLVE)) {
            this.fResolve = (String)value;
        }
    }

    public void setLimitAnalyzer(XMLLimitAnalyzer fLimitAnalyzer) {
        this.fLimitAnalyzer = fLimitAnalyzer;
    }

    @Override
    public String[] getRecognizedProperties() {
        return (String[])RECOGNIZED_PROPERTIES.clone();
    }

    @Override
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; ++i) {
            if (!RECOGNIZED_FEATURES[i].equals(featureId)) continue;
            return FEATURE_DEFAULTS[i];
        }
        return null;
    }

    @Override
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; ++i) {
            if (!RECOGNIZED_PROPERTIES[i].equals(propertyId)) continue;
            return PROPERTY_DEFAULTS[i];
        }
        return null;
    }

    public static String expandSystemId(String systemId) {
        return XMLEntityManager.expandSystemId(systemId, null);
    }

    private static synchronized URI getUserDir() throws URI.MalformedURIException {
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
            return new URI("file", "", "", null, null);
        }
        if (gUserDirURI != null && userDir.equals(gUserDir)) {
            return gUserDirURI;
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
                return new URI("file", "", userDir, null, null);
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
        gUserDirURI = new URI("file", "", buffer.toString(), null, null);
        return gUserDirURI;
    }

    public static OutputStream createOutputStream(String uri) throws IOException {
        String expanded = XMLEntityManager.expandSystemId(uri, null, true);
        URL url = new URL(expanded != null ? expanded : uri);
        OutputStream out = null;
        String protocol = url.getProtocol();
        String host = url.getHost();
        if (protocol.equals("file") && (host == null || host.length() == 0 || host.equals("localhost"))) {
            File parent;
            File file = new File(XMLEntityManager.getPathWithoutEscapes(url.getPath()));
            if (!file.exists() && (parent = file.getParentFile()) != null && !parent.exists()) {
                parent.mkdirs();
            }
            out = new FileOutputStream(file);
        } else {
            URLConnection urlCon = url.openConnection();
            urlCon.setDoInput(false);
            urlCon.setDoOutput(true);
            urlCon.setUseCaches(false);
            if (urlCon instanceof HttpURLConnection) {
                HttpURLConnection httpCon = (HttpURLConnection)urlCon;
                httpCon.setRequestMethod("PUT");
            }
            out = urlCon.getOutputStream();
        }
        return out;
    }

    private static String getPathWithoutEscapes(String origPath) {
        if (origPath != null && origPath.length() != 0 && origPath.indexOf(37) != -1) {
            StringTokenizer tokenizer = new StringTokenizer(origPath, "%");
            StringBuilder result = new StringBuilder(origPath.length());
            int size = tokenizer.countTokens();
            result.append(tokenizer.nextToken());
            for (int i = 1; i < size; ++i) {
                String token = tokenizer.nextToken();
                result.append((char)Integer.valueOf(token.substring(0, 2), 16).intValue());
                result.append(token.substring(2));
            }
            return result.toString();
        }
        return origPath;
    }

    public static void absolutizeAgainstUserDir(URI uri) throws URI.MalformedURIException {
        uri.absolutize(XMLEntityManager.getUserDir());
    }

    public static String expandSystemId(String systemId, String baseSystemId) {
        if (systemId == null || systemId.length() == 0) {
            return systemId;
        }
        try {
            URI uri = new URI(systemId);
            if (uri != null) {
                return systemId;
            }
        }
        catch (URI.MalformedURIException uri) {
            // empty catch block
        }
        String id = XMLEntityManager.fixURI(systemId);
        URI base = null;
        URI uri = null;
        try {
            if (baseSystemId == null || baseSystemId.length() == 0 || baseSystemId.equals(systemId)) {
                String dir = XMLEntityManager.getUserDir().toString();
                base = new URI("file", "", dir, null, null);
            } else {
                try {
                    base = new URI(XMLEntityManager.fixURI(baseSystemId));
                }
                catch (URI.MalformedURIException e) {
                    if (baseSystemId.indexOf(58) != -1) {
                        base = new URI("file", "", XMLEntityManager.fixURI(baseSystemId), null, null);
                    }
                    Object dir = XMLEntityManager.getUserDir().toString();
                    dir = (String)dir + XMLEntityManager.fixURI(baseSystemId);
                    base = new URI("file", "", (String)dir, null, null);
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

    public static String expandSystemId(String systemId, String baseSystemId, boolean strict) throws URI.MalformedURIException {
        if (systemId == null) {
            return null;
        }
        if (strict) {
            try {
                new URI(systemId);
                return systemId;
            }
            catch (URI.MalformedURIException malformedURIException) {
                URI base = null;
                if (baseSystemId == null || baseSystemId.length() == 0) {
                    base = new URI("file", "", XMLEntityManager.getUserDir().toString(), null, null);
                } else {
                    try {
                        base = new URI(baseSystemId);
                    }
                    catch (URI.MalformedURIException e) {
                        Object dir = XMLEntityManager.getUserDir().toString();
                        dir = (String)dir + baseSystemId;
                        base = new URI("file", "", (String)dir, null, null);
                    }
                }
                URI uri = new URI(base, systemId);
                return uri.toString();
            }
        }
        try {
            return XMLEntityManager.expandSystemIdStrictOff(systemId, baseSystemId);
        }
        catch (URI.MalformedURIException e) {
            try {
                return XMLEntityManager.expandSystemIdStrictOff1(systemId, baseSystemId);
            }
            catch (URISyntaxException uri) {
                if (systemId.length() == 0) {
                    return systemId;
                }
                String id = XMLEntityManager.fixURI(systemId);
                URI base = null;
                URI uri2 = null;
                try {
                    if (baseSystemId == null || baseSystemId.length() == 0 || baseSystemId.equals(systemId)) {
                        base = XMLEntityManager.getUserDir();
                    } else {
                        try {
                            base = new URI(XMLEntityManager.fixURI(baseSystemId).trim());
                        }
                        catch (URI.MalformedURIException e2) {
                            base = baseSystemId.indexOf(58) != -1 ? new URI("file", "", XMLEntityManager.fixURI(baseSystemId).trim(), null, null) : new URI(XMLEntityManager.getUserDir(), XMLEntityManager.fixURI(baseSystemId));
                        }
                    }
                    uri2 = new URI(base, id.trim());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (uri2 == null) {
                    return systemId;
                }
                return uri2.toString();
            }
        }
    }

    private static String expandSystemIdStrictOn(String systemId, String baseSystemId) throws URI.MalformedURIException {
        URI systemURI = new URI(systemId, true);
        if (systemURI.isAbsoluteURI()) {
            return systemId;
        }
        URI baseURI = null;
        if (baseSystemId == null || baseSystemId.length() == 0) {
            baseURI = XMLEntityManager.getUserDir();
        } else {
            baseURI = new URI(baseSystemId, true);
            if (!baseURI.isAbsoluteURI()) {
                baseURI.absolutize(XMLEntityManager.getUserDir());
            }
        }
        systemURI.absolutize(baseURI);
        return systemURI.toString();
    }

    private static String expandSystemIdStrictOff(String systemId, String baseSystemId) throws URI.MalformedURIException {
        URI systemURI = new URI(systemId, true);
        if (systemURI.isAbsoluteURI()) {
            if (systemURI.getScheme().length() > 1) {
                return systemId;
            }
            throw new URI.MalformedURIException();
        }
        URI baseURI = null;
        if (baseSystemId == null || baseSystemId.length() == 0) {
            baseURI = XMLEntityManager.getUserDir();
        } else {
            baseURI = new URI(baseSystemId, true);
            if (!baseURI.isAbsoluteURI()) {
                baseURI.absolutize(XMLEntityManager.getUserDir());
            }
        }
        systemURI.absolutize(baseURI);
        return systemURI.toString();
    }

    private static String expandSystemIdStrictOff1(String systemId, String baseSystemId) throws URISyntaxException, URI.MalformedURIException {
        java.net.URI systemURI = new java.net.URI(systemId);
        if (systemURI.isAbsolute()) {
            if (systemURI.getScheme().length() > 1) {
                return systemId;
            }
            throw new URISyntaxException(systemId, "the scheme's length is only one character");
        }
        URI baseURI = null;
        if (baseSystemId == null || baseSystemId.length() == 0) {
            baseURI = XMLEntityManager.getUserDir();
        } else {
            baseURI = new URI(baseSystemId, true);
            if (!baseURI.isAbsoluteURI()) {
                baseURI.absolutize(XMLEntityManager.getUserDir());
            }
        }
        systemURI = new java.net.URI(baseURI.toString()).resolve(systemURI);
        return systemURI.toString();
    }

    protected EncodingInfo getEncodingInfo(byte[] b4, int count) {
        if (count < 2) {
            return EncodingInfo.UTF_8;
        }
        int b0 = b4[0] & 0xFF;
        int b1 = b4[1] & 0xFF;
        if (b0 == 254 && b1 == 255) {
            return EncodingInfo.UTF_16_BIG_ENDIAN_WITH_BOM;
        }
        if (b0 == 255 && b1 == 254) {
            return EncodingInfo.UTF_16_LITTLE_ENDIAN_WITH_BOM;
        }
        if (count < 3) {
            return EncodingInfo.UTF_8;
        }
        int b2 = b4[2] & 0xFF;
        if (b0 == 239 && b1 == 187 && b2 == 191) {
            return EncodingInfo.UTF_8_WITH_BOM;
        }
        if (count < 4) {
            return EncodingInfo.UTF_8;
        }
        int b3 = b4[3] & 0xFF;
        if (b0 == 0 && b1 == 0 && b2 == 0 && b3 == 60) {
            return EncodingInfo.UCS_4_BIG_ENDIAN;
        }
        if (b0 == 60 && b1 == 0 && b2 == 0 && b3 == 0) {
            return EncodingInfo.UCS_4_LITTLE_ENDIAN;
        }
        if (b0 == 0 && b1 == 0 && b2 == 60 && b3 == 0) {
            return EncodingInfo.UCS_4_UNUSUAL_BYTE_ORDER;
        }
        if (b0 == 0 && b1 == 60 && b2 == 0 && b3 == 0) {
            return EncodingInfo.UCS_4_UNUSUAL_BYTE_ORDER;
        }
        if (b0 == 0 && b1 == 60 && b2 == 0 && b3 == 63) {
            return EncodingInfo.UTF_16_BIG_ENDIAN;
        }
        if (b0 == 60 && b1 == 0 && b2 == 63 && b3 == 0) {
            return EncodingInfo.UTF_16_LITTLE_ENDIAN;
        }
        if (b0 == 76 && b1 == 111 && b2 == 167 && b3 == 148) {
            return EncodingInfo.EBCDIC;
        }
        return EncodingInfo.UTF_8;
    }

    protected Reader createReader(InputStream inputStream, String encoding, Boolean isBigEndian) throws IOException {
        String javaEncoding;
        String enc = encoding != null ? encoding : "UTF-8";
        enc = enc.toUpperCase(Locale.ENGLISH);
        MessageFormatter f = this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210");
        Locale l = this.fErrorReporter.getLocale();
        switch (enc) {
            case "UTF-8": {
                return new UTF8Reader(inputStream, this.fBufferSize, f, l);
            }
            case "UTF-16": {
                if (isBigEndian == null) break;
                return new UTF16Reader(inputStream, this.fBufferSize, (boolean)isBigEndian, f, l);
            }
            case "UTF-16BE": {
                return new UTF16Reader(inputStream, this.fBufferSize, true, f, l);
            }
            case "UTF-16LE": {
                return new UTF16Reader(inputStream, this.fBufferSize, false, f, l);
            }
            case "ISO-10646-UCS-4": {
                if (isBigEndian != null) {
                    if (isBigEndian.booleanValue()) {
                        return new UCSReader(inputStream, 8);
                    }
                    return new UCSReader(inputStream, 4);
                }
                this.fErrorReporter.reportError(this.getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{encoding}, (short)2);
                break;
            }
            case "ISO-10646-UCS-2": {
                if (isBigEndian != null) {
                    if (isBigEndian.booleanValue()) {
                        return new UCSReader(inputStream, 2);
                    }
                    return new UCSReader(inputStream, 1);
                }
                this.fErrorReporter.reportError(this.getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{encoding}, (short)2);
            }
        }
        boolean validIANA = XMLChar.isValidIANAEncoding(encoding);
        boolean validJava = XMLChar.isValidJavaEncoding(encoding);
        if (!validIANA || this.fAllowJavaEncodings && !validJava) {
            this.fErrorReporter.reportError(this.getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{encoding}, (short)2);
            encoding = "ISO-8859-1";
        }
        if ((javaEncoding = EncodingMap.getIANA2JavaMapping(enc)) == null) {
            if (this.fAllowJavaEncodings) {
                javaEncoding = encoding;
            } else {
                this.fErrorReporter.reportError(this.getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{encoding}, (short)2);
                javaEncoding = "ISO8859_1";
            }
        }
        return new BufferedReader(new InputStreamReader(inputStream, javaEncoding));
    }

    public String getPublicId() {
        return this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null ? this.fCurrentEntity.entityLocation.getPublicId() : null;
    }

    public String getExpandedSystemId() {
        if (this.fCurrentEntity != null) {
            if (this.fCurrentEntity.entityLocation != null && this.fCurrentEntity.entityLocation.getExpandedSystemId() != null) {
                return this.fCurrentEntity.entityLocation.getExpandedSystemId();
            }
            int size = this.fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
                Entity.ScannedEntity externalEntity = (Entity.ScannedEntity)this.fEntityStack.get(i);
                if (externalEntity.entityLocation == null || externalEntity.entityLocation.getExpandedSystemId() == null) continue;
                return externalEntity.entityLocation.getExpandedSystemId();
            }
        }
        return null;
    }

    public String getLiteralSystemId() {
        if (this.fCurrentEntity != null) {
            if (this.fCurrentEntity.entityLocation != null && this.fCurrentEntity.entityLocation.getLiteralSystemId() != null) {
                return this.fCurrentEntity.entityLocation.getLiteralSystemId();
            }
            int size = this.fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
                Entity.ScannedEntity externalEntity = (Entity.ScannedEntity)this.fEntityStack.get(i);
                if (externalEntity.entityLocation == null || externalEntity.entityLocation.getLiteralSystemId() == null) continue;
                return externalEntity.entityLocation.getLiteralSystemId();
            }
        }
        return null;
    }

    public int getLineNumber() {
        if (this.fCurrentEntity != null) {
            if (this.fCurrentEntity.isExternal()) {
                return this.fCurrentEntity.lineNumber;
            }
            int size = this.fEntityStack.size();
            for (int i = size - 1; i > 0; --i) {
                Entity.ScannedEntity firstExternalEntity = (Entity.ScannedEntity)this.fEntityStack.get(i);
                if (!firstExternalEntity.isExternal()) continue;
                return firstExternalEntity.lineNumber;
            }
        }
        return -1;
    }

    public int getColumnNumber() {
        if (this.fCurrentEntity != null) {
            if (this.fCurrentEntity.isExternal()) {
                return this.fCurrentEntity.columnNumber;
            }
            int size = this.fEntityStack.size();
            for (int i = size - 1; i > 0; --i) {
                Entity.ScannedEntity firstExternalEntity = (Entity.ScannedEntity)this.fEntityStack.get(i);
                if (!firstExternalEntity.isExternal()) continue;
                return firstExternalEntity.columnNumber;
            }
        }
        return -1;
    }

    protected static String fixURI(String str) {
        int pos;
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
        if ((pos = ((String)str).indexOf(32)) >= 0) {
            int i;
            StringBuilder sb = new StringBuilder(((String)str).length());
            for (i = 0; i < pos; ++i) {
                sb.append(((String)str).charAt(i));
            }
            sb.append("%20");
            for (i = pos + 1; i < ((String)str).length(); ++i) {
                if (((String)str).charAt(i) == ' ') {
                    sb.append("%20");
                    continue;
                }
                sb.append(((String)str).charAt(i));
            }
            str = sb.toString();
        }
        return str;
    }

    final void print() {
    }

    public void test() {
        this.fEntityStorage.addExternalEntity("entityUsecase1", null, "/space/home/stax/sun/6thJan2004/zephyr/data/test.txt", "/space/home/stax/sun/6thJan2004/zephyr/data/entity.xml");
        this.fEntityStorage.addInternalEntity("entityUsecase2", "<Test>value</Test>");
        this.fEntityStorage.addInternalEntity("entityUsecase3", "value3");
        this.fEntityStorage.addInternalEntity("text", "Hello World.");
        this.fEntityStorage.addInternalEntity("empty-element", "<foo/>");
        this.fEntityStorage.addInternalEntity("balanced-element", "<foo></foo>");
        this.fEntityStorage.addInternalEntity("balanced-element-with-text", "<foo>Hello, World</foo>");
        this.fEntityStorage.addInternalEntity("balanced-element-with-entity", "<foo>&text;</foo>");
        this.fEntityStorage.addInternalEntity("unbalanced-entity", "<foo>");
        this.fEntityStorage.addInternalEntity("recursive-entity", "<foo>&recursive-entity2;</foo>");
        this.fEntityStorage.addInternalEntity("recursive-entity2", "<bar>&recursive-entity3;</bar>");
        this.fEntityStorage.addInternalEntity("recursive-entity3", "<baz>&recursive-entity;</baz>");
        this.fEntityStorage.addInternalEntity("ch", "&#x00A9;");
        this.fEntityStorage.addInternalEntity("ch1", "&#84;");
        this.fEntityStorage.addInternalEntity("% ch2", "param");
    }

    static {
        gNeedEscaping = new boolean[128];
        gAfterEscaping1 = new char[128];
        gAfterEscaping2 = new char[128];
        gHexChs = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i <= 31; ++i) {
            XMLEntityManager.gNeedEscaping[i] = true;
            XMLEntityManager.gAfterEscaping1[i] = gHexChs[i >> 4];
            XMLEntityManager.gAfterEscaping2[i] = gHexChs[i & 0xF];
        }
        XMLEntityManager.gNeedEscaping[127] = true;
        XMLEntityManager.gAfterEscaping1[127] = 55;
        XMLEntityManager.gAfterEscaping2[127] = 70;
        for (char ch : new char[]{' ', '<', '>', '#', '%', '\"', '{', '}', '|', '\\', '^', '~', '[', ']', '`'}) {
            XMLEntityManager.gNeedEscaping[ch] = true;
            XMLEntityManager.gAfterEscaping1[ch] = gHexChs[ch >> 4];
            XMLEntityManager.gAfterEscaping2[ch] = gHexChs[ch & 0xF];
        }
    }

    protected final class RewindableInputStream
    extends InputStream {
        private InputStream fInputStream;
        private byte[] fData = new byte[64];
        private int fStartOffset;
        private int fEndOffset;
        private int fOffset;
        private int fLength;
        private int fMark;

        public RewindableInputStream(InputStream is) {
            this.fInputStream = is;
            this.fStartOffset = 0;
            this.fEndOffset = -1;
            this.fOffset = 0;
            this.fLength = 0;
            this.fMark = 0;
        }

        public void setStartOffset(int offset) {
            this.fStartOffset = offset;
        }

        public void rewind() {
            this.fOffset = this.fStartOffset;
        }

        public int readAndBuffer() throws IOException {
            int b;
            if (this.fOffset == this.fData.length) {
                byte[] newData = new byte[this.fOffset << 1];
                System.arraycopy(this.fData, 0, newData, 0, this.fOffset);
                this.fData = newData;
            }
            if ((b = this.fInputStream.read()) == -1) {
                this.fEndOffset = this.fOffset;
                return -1;
            }
            this.fData[this.fLength++] = (byte)b;
            ++this.fOffset;
            return b & 0xFF;
        }

        @Override
        public int read() throws IOException {
            if (this.fOffset < this.fLength) {
                return this.fData[this.fOffset++] & 0xFF;
            }
            if (this.fOffset == this.fEndOffset) {
                return -1;
            }
            if (XMLEntityManager.this.fCurrentEntity.mayReadChunks) {
                return this.fInputStream.read();
            }
            return this.readAndBuffer();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int bytesLeft = this.fLength - this.fOffset;
            if (bytesLeft == 0) {
                if (this.fOffset == this.fEndOffset) {
                    return -1;
                }
                if (XMLEntityManager.this.fCurrentEntity.mayReadChunks || !XMLEntityManager.this.fCurrentEntity.xmlDeclChunkRead) {
                    if (!XMLEntityManager.this.fCurrentEntity.xmlDeclChunkRead) {
                        XMLEntityManager.this.fCurrentEntity.xmlDeclChunkRead = true;
                        len = 28;
                    }
                    return this.fInputStream.read(b, off, len);
                }
                int returnedVal = this.readAndBuffer();
                if (returnedVal == -1) {
                    this.fEndOffset = this.fOffset;
                    return -1;
                }
                b[off] = (byte)returnedVal;
                return 1;
            }
            if (len < bytesLeft) {
                if (len <= 0) {
                    return 0;
                }
            } else {
                len = bytesLeft;
            }
            if (b != null) {
                System.arraycopy(this.fData, this.fOffset, b, off, len);
            }
            this.fOffset += len;
            return len;
        }

        @Override
        public long skip(long n) throws IOException {
            if (n <= 0L) {
                return 0L;
            }
            int bytesLeft = this.fLength - this.fOffset;
            if (bytesLeft == 0) {
                if (this.fOffset == this.fEndOffset) {
                    return 0L;
                }
                return this.fInputStream.skip(n);
            }
            if (n <= (long)bytesLeft) {
                this.fOffset = (int)((long)this.fOffset + n);
                return n;
            }
            this.fOffset += bytesLeft;
            if (this.fOffset == this.fEndOffset) {
                return bytesLeft;
            }
            return this.fInputStream.skip(n -= (long)bytesLeft) + (long)bytesLeft;
        }

        @Override
        public int available() throws IOException {
            int bytesLeft = this.fLength - this.fOffset;
            if (bytesLeft == 0) {
                if (this.fOffset == this.fEndOffset) {
                    return -1;
                }
                return XMLEntityManager.this.fCurrentEntity.mayReadChunks ? this.fInputStream.available() : 0;
            }
            return bytesLeft;
        }

        @Override
        public void mark(int howMuch) {
            this.fMark = this.fOffset;
        }

        @Override
        public void reset() {
            this.fOffset = this.fMark;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void close() throws IOException {
            if (this.fInputStream != null) {
                this.fInputStream.close();
                this.fInputStream = null;
            }
        }
    }

    private static class EncodingInfo {
        public static final String STR_UTF8 = "UTF-8";
        public static final String STR_UTF16 = "UTF-16";
        public static final String STR_UTF16BE = "UTF-16BE";
        public static final String STR_UTF16LE = "UTF-16LE";
        public static final String STR_UCS4 = "ISO-10646-UCS-4";
        public static final String STR_UCS2 = "ISO-10646-UCS-2";
        public static final String STR_CP037 = "CP037";
        public static final EncodingInfo UTF_8 = new EncodingInfo("UTF-8", null, false);
        public static final EncodingInfo UTF_8_WITH_BOM = new EncodingInfo("UTF-8", null, true);
        public static final EncodingInfo UTF_16_BIG_ENDIAN = new EncodingInfo("UTF-16BE", "UTF-16", Boolean.TRUE, false);
        public static final EncodingInfo UTF_16_BIG_ENDIAN_WITH_BOM = new EncodingInfo("UTF-16BE", "UTF-16", Boolean.TRUE, true);
        public static final EncodingInfo UTF_16_LITTLE_ENDIAN = new EncodingInfo("UTF-16LE", "UTF-16", Boolean.FALSE, false);
        public static final EncodingInfo UTF_16_LITTLE_ENDIAN_WITH_BOM = new EncodingInfo("UTF-16LE", "UTF-16", Boolean.FALSE, true);
        public static final EncodingInfo UCS_4_BIG_ENDIAN = new EncodingInfo("ISO-10646-UCS-4", Boolean.TRUE, false);
        public static final EncodingInfo UCS_4_LITTLE_ENDIAN = new EncodingInfo("ISO-10646-UCS-4", Boolean.FALSE, false);
        public static final EncodingInfo UCS_4_UNUSUAL_BYTE_ORDER = new EncodingInfo("ISO-10646-UCS-4", null, false);
        public static final EncodingInfo EBCDIC = new EncodingInfo("CP037", null, false);
        public final String autoDetectedEncoding;
        public final String readerEncoding;
        public final Boolean isBigEndian;
        public final boolean hasBOM;

        private EncodingInfo(String autoDetectedEncoding, Boolean isBigEndian, boolean hasBOM) {
            this(autoDetectedEncoding, autoDetectedEncoding, isBigEndian, hasBOM);
        }

        private EncodingInfo(String autoDetectedEncoding, String readerEncoding, Boolean isBigEndian, boolean hasBOM) {
            this.autoDetectedEncoding = autoDetectedEncoding;
            this.readerEncoding = readerEncoding;
            this.isBigEndian = isBigEndian;
            this.hasBOM = hasBOM;
        }
    }
}

