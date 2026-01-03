/*
 * Decompiled with CFR 0.152.
 */
package jdk.xml.internal;

import jdk.xml.internal.SecuritySupport;

public final class JdkConstants {
    public static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    @Deprecated(since="17")
    public static final String ORACLE_JAXP_PROPERTY_PREFIX = "http://www.oracle.com/xml/jaxp/properties/";
    @Deprecated(since="17")
    public static final String JDK_ENTITY_EXPANSION_LIMIT = "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit";
    @Deprecated(since="17")
    public static final String JDK_ELEMENT_ATTRIBUTE_LIMIT = "http://www.oracle.com/xml/jaxp/properties/elementAttributeLimit";
    @Deprecated(since="17")
    public static final String JDK_MAX_OCCUR_LIMIT = "http://www.oracle.com/xml/jaxp/properties/maxOccurLimit";
    @Deprecated(since="17")
    public static final String JDK_TOTAL_ENTITY_SIZE_LIMIT = "http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit";
    @Deprecated(since="17")
    public static final String JDK_GENERAL_ENTITY_SIZE_LIMIT = "http://www.oracle.com/xml/jaxp/properties/maxGeneralEntitySizeLimit";
    @Deprecated(since="17")
    public static final String JDK_ENTITY_REPLACEMENT_LIMIT = "http://www.oracle.com/xml/jaxp/properties/entityReplacementLimit";
    @Deprecated(since="17")
    public static final String JDK_PARAMETER_ENTITY_SIZE_LIMIT = "http://www.oracle.com/xml/jaxp/properties/maxParameterEntitySizeLimit";
    @Deprecated(since="17")
    public static final String JDK_XML_NAME_LIMIT = "http://www.oracle.com/xml/jaxp/properties/maxXMLNameLimit";
    @Deprecated(since="17")
    public static final String JDK_MAX_ELEMENT_DEPTH = "http://www.oracle.com/xml/jaxp/properties/maxElementDepth";
    @Deprecated(since="17")
    public static final String JDK_ENTITY_COUNT_INFO = "http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo";
    public static final String JDK_DEBUG_LIMIT = "jdk.xml.getEntityCountInfo";
    public static final String SP_ENTITY_EXPANSION_LIMIT = "jdk.xml.entityExpansionLimit";
    public static final String SP_ELEMENT_ATTRIBUTE_LIMIT = "jdk.xml.elementAttributeLimit";
    public static final String SP_MAX_OCCUR_LIMIT = "jdk.xml.maxOccurLimit";
    public static final String SP_TOTAL_ENTITY_SIZE_LIMIT = "jdk.xml.totalEntitySizeLimit";
    public static final String SP_GENERAL_ENTITY_SIZE_LIMIT = "jdk.xml.maxGeneralEntitySizeLimit";
    public static final String SP_ENTITY_REPLACEMENT_LIMIT = "jdk.xml.entityReplacementLimit";
    public static final String SP_PARAMETER_ENTITY_SIZE_LIMIT = "jdk.xml.maxParameterEntitySizeLimit";
    public static final String SP_XML_NAME_LIMIT = "jdk.xml.maxXMLNameLimit";
    public static final String SP_MAX_ELEMENT_DEPTH = "jdk.xml.maxElementDepth";
    public static final String XPATH_GROUP_LIMIT = "jdk.xml.xpathExprGrpLimit";
    public static final String XPATH_OP_LIMIT = "jdk.xml.xpathExprOpLimit";
    public static final String XPATH_TOTALOP_LIMIT = "jdk.xml.xpathTotalOpLimit";
    public static final String JDK_EXTENSION_CLASSLOADER = "jdk.xml.transform.extensionClassLoader";
    public static final String JDK_EXT_CLASSLOADER = "jdk.xml.extensionClassLoader";
    public static final String ENTITY_EXPANSION_LIMIT = "entityExpansionLimit";
    public static final String ELEMENT_ATTRIBUTE_LIMIT = "elementAttributeLimit";
    public static final String MAX_OCCUR_LIMIT = "maxOccurLimit";
    public static final String JDK_YES = "yes";
    @Deprecated(since="17")
    public static final String ORACLE_FEATURE_SERVICE_MECHANISM = "http://www.oracle.com/feature/use-service-mechanism";
    public static final String SP_ACCESS_EXTERNAL_STYLESHEET = "javax.xml.accessExternalStylesheet";
    public static final String SP_ACCESS_EXTERNAL_DTD = "javax.xml.accessExternalDTD";
    public static final String SP_ACCESS_EXTERNAL_SCHEMA = "javax.xml.accessExternalSchema";
    public static final String ACCESS_EXTERNAL_ALL = "all";
    public static final String EXTERNAL_ACCESS_DEFAULT_FSP = "";
    public static final String EXTERNAL_ACCESS_DEFAULT = "all";
    public static final String XML_SECURITY_PROPERTY_MANAGER = "jdk.xml.xmlSecurityPropertyManager";
    public static final String CONFIG_FILE = "java.xml.config.file";
    public static final String FEATURE_TRUE = "true";
    public static final String FEATURE_FALSE = "false";
    public static final String S_IS_STANDALONE = "isStandalone";
    @Deprecated(since="17")
    public static final String FQ_IS_STANDALONE = "http://www.oracle.com/xml/jaxp/properties/isStandalone";
    public static final String SP_IS_STANDALONE = "jdk.xml.isStandalone";
    @Deprecated(since="17")
    public static final String ORACLE_IS_STANDALONE = "http://www.oracle.com/xml/is-standalone";
    @Deprecated(since="17")
    public static final String JDK_IS_STANDALONE = "http://www.oracle.com/xml/jaxp/properties/xsltcIsStandalone";
    public static final String SP_XSLTC_IS_STANDALONE = "jdk.xml.xsltcIsStandalone";
    public static final String ORACLE_ENABLE_EXTENSION_FUNCTION = "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions";
    public static final String SP_ENABLE_EXTENSION_FUNCTION = "javax.xml.enableExtensionFunctions";
    public static final String SP_ENABLE_EXTENSION_FUNCTION_SPEC = "jdk.xml.enableExtensionFunctions";
    public static final String RESET_SYMBOL_TABLE = "jdk.xml.resetSymbolTable";
    public static final boolean RESET_SYMBOL_TABLE_DEFAULT = SecuritySupport.getJAXPSystemProperty(Boolean.class, "jdk.xml.resetSymbolTable", "false");
    public static final String OVERRIDE_PARSER = "jdk.xml.overrideDefaultParser";
    public static final boolean OVERRIDE_PARSER_DEFAULT = SecuritySupport.getJAXPSystemProperty(Boolean.class, "jdk.xml.overrideDefaultParser", "false");
    public static final String CDATA_CHUNK_SIZE = "jdk.xml.cdataChunkSize";
    public static final int CDATA_CHUNK_SIZE_DEFAULT = SecuritySupport.getJAXPSystemProperty(Integer.class, "jdk.xml.cdataChunkSize", "0");
}

