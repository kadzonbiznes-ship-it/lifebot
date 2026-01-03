/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.Extension;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.Utilities;
import sun.security.util.HexDumpEncoder;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public final class SSLLogger {
    private static final System.Logger logger;
    private static final String property;
    public static final boolean isOn;

    private static void help() {
        System.err.println();
        System.err.println("help           print the help messages");
        System.err.println("expand         expand debugging information");
        System.err.println();
        System.err.println("all            turn on all debugging");
        System.err.println("ssl            turn on ssl debugging");
        System.err.println();
        System.err.println("The following can be used with ssl:");
        System.err.println("\trecord       enable per-record tracing");
        System.err.println("\thandshake    print each handshake message");
        System.err.println("\tkeygen       print key generation data");
        System.err.println("\tsession      print session activity");
        System.err.println("\tdefaultctx   print default SSL initialization");
        System.err.println("\tsslctx       print SSLContext tracing");
        System.err.println("\tsessioncache print session cache tracing");
        System.err.println("\tkeymanager   print key manager tracing");
        System.err.println("\ttrustmanager print trust manager tracing");
        System.err.println("\tpluggability print pluggability tracing");
        System.err.println();
        System.err.println("\thandshake debugging can be widened with:");
        System.err.println("\tdata         hex dump of each handshake message");
        System.err.println("\tverbose      verbose handshake message printing");
        System.err.println();
        System.err.println("\trecord debugging can be widened with:");
        System.err.println("\tplaintext    hex dump of record plaintext");
        System.err.println("\tpacket       print raw SSL/TLS packets");
        System.err.println();
        System.exit(0);
    }

    public static boolean isOn(String checkPoints) {
        String[] options;
        if (property == null) {
            return false;
        }
        if (property.isEmpty()) {
            return true;
        }
        for (String option : options = checkPoints.split(",")) {
            if (SSLLogger.hasOption(option = option.trim())) continue;
            return false;
        }
        return true;
    }

    private static boolean hasOption(String option) {
        option = option.toLowerCase(Locale.ENGLISH);
        if (property.contains("all")) {
            return true;
        }
        String modified = property.replaceFirst("sslctx", "");
        if (modified.contains("ssl") && !option.equals("data") && !option.equals("packet") && !option.equals("plaintext")) {
            return true;
        }
        return property.contains(option);
    }

    public static void severe(String msg, Object ... params) {
        SSLLogger.log(System.Logger.Level.ERROR, msg, params);
    }

    public static void warning(String msg, Object ... params) {
        SSLLogger.log(System.Logger.Level.WARNING, msg, params);
    }

    public static void info(String msg, Object ... params) {
        SSLLogger.log(System.Logger.Level.INFO, msg, params);
    }

    public static void fine(String msg, Object ... params) {
        SSLLogger.log(System.Logger.Level.DEBUG, msg, params);
    }

    public static void finer(String msg, Object ... params) {
        SSLLogger.log(System.Logger.Level.TRACE, msg, params);
    }

    public static void finest(String msg, Object ... params) {
        SSLLogger.log(System.Logger.Level.ALL, msg, params);
    }

    private static void log(System.Logger.Level level, String msg, Object ... params) {
        if (logger != null && logger.isLoggable(level)) {
            if (params == null || params.length == 0) {
                logger.log(level, msg);
            } else {
                try {
                    String formatted = SSLSimpleFormatter.formatParameters(params);
                    logger.log(level, msg, formatted);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    static String toString(Object ... params) {
        try {
            return SSLSimpleFormatter.formatParameters(params);
        }
        catch (Exception exp) {
            return "unexpected exception thrown: " + exp.getMessage();
        }
    }

    public static boolean logWarning(String option, String s) {
        if (isOn && SSLLogger.isOn(option)) {
            SSLLogger.warning(s, new Object[0]);
        }
        return false;
    }

    static {
        String p = GetPropertyAction.privilegedGetProperty("javax.net.debug");
        if (p != null) {
            if (p.isEmpty()) {
                property = "";
                logger = System.getLogger("javax.net.ssl");
            } else {
                property = p.toLowerCase(Locale.ENGLISH);
                if (property.equals("help")) {
                    SSLLogger.help();
                }
                logger = new SSLConsoleLogger("javax.net.ssl", p);
            }
            isOn = true;
        } else {
            property = null;
            logger = null;
            isOn = false;
        }
    }

    private static class SSLSimpleFormatter {
        private static final String PATTERN = "yyyy-MM-dd kk:mm:ss.SSS z";
        private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss.SSS z", Locale.ENGLISH).withZone(ZoneId.systemDefault());
        private static final MessageFormat basicCertFormat = new MessageFormat("\"version\"            : \"v{0}\",\n\"serial number\"      : \"{1}\",\n\"signature algorithm\": \"{2}\",\n\"issuer\"             : \"{3}\",\n\"not before\"         : \"{4}\",\n\"not  after\"         : \"{5}\",\n\"subject\"            : \"{6}\",\n\"subject public key\" : \"{7}\"\n", Locale.ENGLISH);
        private static final MessageFormat extendedCertFormart = new MessageFormat("\"version\"            : \"v{0}\",\n\"serial number\"      : \"{1}\",\n\"signature algorithm\": \"{2}\",\n\"issuer\"             : \"{3}\",\n\"not before\"         : \"{4}\",\n\"not  after\"         : \"{5}\",\n\"subject\"            : \"{6}\",\n\"subject public key\" : \"{7}\",\n\"extensions\"         : [\n{8}\n]\n", Locale.ENGLISH);
        private static final MessageFormat messageFormatNoParas = new MessageFormat("'{'\n  \"logger\"      : \"{0}\",\n  \"level\"       : \"{1}\",\n  \"thread id\"   : \"{2}\",\n  \"thread name\" : \"{3}\",\n  \"time\"        : \"{4}\",\n  \"caller\"      : \"{5}\",\n  \"message\"     : \"{6}\"\n'}'\n", Locale.ENGLISH);
        private static final MessageFormat messageCompactFormatNoParas = new MessageFormat("{0}|{1}|{2}|{3}|{4}|{5}|{6}\n", Locale.ENGLISH);
        private static final MessageFormat messageFormatWithParas = new MessageFormat("'{'\n  \"logger\"      : \"{0}\",\n  \"level\"       : \"{1}\",\n  \"thread id\"   : \"{2}\",\n  \"thread name\" : \"{3}\",\n  \"time\"        : \"{4}\",\n  \"caller\"      : \"{5}\",\n  \"message\"     : \"{6}\",\n  \"specifics\"   : [\n{7}\n  ]\n'}'\n", Locale.ENGLISH);
        private static final MessageFormat messageCompactFormatWithParas = new MessageFormat("{0}|{1}|{2}|{3}|{4}|{5}|{6} (\n{7}\n)\n", Locale.ENGLISH);
        private static final MessageFormat keyObjectFormat = new MessageFormat("\"{0}\" : '{'\n{1}'}'\n", Locale.ENGLISH);

        private SSLSimpleFormatter() {
        }

        private static String format(SSLConsoleLogger logger, System.Logger.Level level, String message, Object ... parameters) {
            if (parameters == null || parameters.length == 0) {
                Object[] messageFields = new Object[]{logger.loggerName, level.getName(), Utilities.toHexString(Thread.currentThread().threadId()), Thread.currentThread().getName(), dateTimeFormat.format(Instant.now()), SSLSimpleFormatter.formatCaller(), message};
                if (logger.useCompactFormat) {
                    return messageCompactFormatNoParas.format(messageFields);
                }
                return messageFormatNoParas.format(messageFields);
            }
            Object[] messageFields = new Object[]{logger.loggerName, level.getName(), Utilities.toHexString(Thread.currentThread().threadId()), Thread.currentThread().getName(), dateTimeFormat.format(Instant.now()), SSLSimpleFormatter.formatCaller(), message, logger.useCompactFormat ? SSLSimpleFormatter.formatParameters(parameters) : Utilities.indent(SSLSimpleFormatter.formatParameters(parameters))};
            if (logger.useCompactFormat) {
                return messageCompactFormatWithParas.format(messageFields);
            }
            return messageFormatWithParas.format(messageFields);
        }

        private static String formatCaller() {
            return StackWalker.getInstance().walk(s -> s.dropWhile(f -> f.getClassName().startsWith("sun.security.ssl.SSLLogger") || f.getClassName().startsWith("java.lang.System")).map(f -> f.getFileName() + ":" + f.getLineNumber()).findFirst().orElse("unknown caller"));
        }

        private static String formatParameters(Object ... parameters) {
            StringBuilder builder = new StringBuilder(512);
            boolean isFirst = true;
            for (Object parameter : parameters) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(",\n");
                }
                if (parameter instanceof Throwable) {
                    builder.append(SSLSimpleFormatter.formatThrowable((Throwable)parameter));
                    continue;
                }
                if (parameter instanceof Certificate) {
                    builder.append(SSLSimpleFormatter.formatCertificate((Certificate)parameter));
                    continue;
                }
                if (parameter instanceof ByteArrayInputStream) {
                    builder.append(SSLSimpleFormatter.formatByteArrayInputStream((ByteArrayInputStream)parameter));
                    continue;
                }
                if (parameter instanceof ByteBuffer) {
                    builder.append(SSLSimpleFormatter.formatByteBuffer((ByteBuffer)parameter));
                    continue;
                }
                if (parameter instanceof byte[]) {
                    builder.append(SSLSimpleFormatter.formatByteArrayInputStream(new ByteArrayInputStream((byte[])parameter)));
                    continue;
                }
                if (parameter instanceof Map.Entry) {
                    Map.Entry mapParameter = (Map.Entry)parameter;
                    builder.append(SSLSimpleFormatter.formatMapEntry(mapParameter));
                    continue;
                }
                builder.append(SSLSimpleFormatter.formatObject(parameter));
            }
            return builder.toString();
        }

        private static String formatThrowable(Throwable throwable) {
            StringBuilder builder = new StringBuilder(512);
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            try (PrintStream out = new PrintStream(bytesOut);){
                throwable.printStackTrace(out);
                builder.append(Utilities.indent(bytesOut.toString()));
            }
            Object[] fields = new Object[]{"throwable", builder.toString()};
            return keyObjectFormat.format(fields);
        }

        private static String formatCertificate(Certificate certificate) {
            if (!(certificate instanceof X509Certificate)) {
                return Utilities.indent(certificate.toString());
            }
            StringBuilder builder = new StringBuilder(512);
            try {
                X509CertImpl x509 = X509CertImpl.toImpl((X509Certificate)certificate);
                X509CertInfo certInfo = x509.getInfo();
                CertificateExtensions certExts = certInfo.getExtensions();
                if (certExts == null) {
                    Object[] certFields = new Object[]{x509.getVersion(), Utilities.toHexString(x509.getSerialNumber().toByteArray()), x509.getSigAlgName(), x509.getIssuerX500Principal().toString(), dateTimeFormat.format(x509.getNotBefore().toInstant()), dateTimeFormat.format(x509.getNotAfter().toInstant()), x509.getSubjectX500Principal().toString(), x509.getPublicKey().getAlgorithm()};
                    builder.append(Utilities.indent(basicCertFormat.format(certFields)));
                } else {
                    StringBuilder extBuilder = new StringBuilder(512);
                    boolean isFirst = true;
                    for (Extension extension : certExts.getAllExtensions()) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            extBuilder.append(",\n");
                        }
                        extBuilder.append("{\n" + Utilities.indent(extension.toString()) + "\n}");
                    }
                    Object[] certFields = new Object[]{x509.getVersion(), Utilities.toHexString(x509.getSerialNumber().toByteArray()), x509.getSigAlgName(), x509.getIssuerX500Principal().toString(), dateTimeFormat.format(x509.getNotBefore().toInstant()), dateTimeFormat.format(x509.getNotAfter().toInstant()), x509.getSubjectX500Principal().toString(), x509.getPublicKey().getAlgorithm(), Utilities.indent(extBuilder.toString())};
                    builder.append(Utilities.indent(extendedCertFormart.format(certFields)));
                }
            }
            catch (Exception x509) {
                // empty catch block
            }
            Object[] fields = new Object[]{"certificate", builder.toString()};
            return Utilities.indent(keyObjectFormat.format(fields));
        }

        private static String formatByteArrayInputStream(ByteArrayInputStream bytes) {
            StringBuilder builder = new StringBuilder(512);
            try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();){
                HexDumpEncoder hexEncoder = new HexDumpEncoder();
                hexEncoder.encodeBuffer(bytes, (OutputStream)bytesOut);
                builder.append(Utilities.indent(bytesOut.toString()));
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return builder.toString();
        }

        private static String formatByteBuffer(ByteBuffer byteBuffer) {
            StringBuilder builder = new StringBuilder(512);
            try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();){
                HexDumpEncoder hexEncoder = new HexDumpEncoder();
                hexEncoder.encodeBuffer(byteBuffer.duplicate(), (OutputStream)bytesOut);
                builder.append(Utilities.indent(bytesOut.toString()));
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return builder.toString();
        }

        private static String formatMapEntry(Map.Entry<String, ?> entry) {
            String formatted;
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                formatted = "\"" + key + "\": \"" + value + "\"";
            } else if (value instanceof String[]) {
                String[] strings = (String[])value;
                StringBuilder builder = new StringBuilder(512);
                builder.append("\"" + key + "\": [\n");
                int len = strings.length;
                for (int i = 0; i < len; ++i) {
                    String string = strings[i];
                    builder.append("      \"" + string + "\"");
                    if (i != len - 1) {
                        builder.append(",");
                    }
                    builder.append("\n");
                }
                builder.append("      ]");
                formatted = builder.toString();
            } else {
                formatted = value instanceof byte[] ? "\"" + key + "\": \"" + Utilities.toHexString((byte[])value) + "\"" : (value instanceof Byte ? "\"" + key + "\": \"" + HexFormat.of().toHexDigits((Byte)value) + "\"" : "\"" + key + "\": \"" + value.toString() + "\"");
            }
            return Utilities.indent(formatted);
        }

        private static String formatObject(Object obj) {
            return obj.toString();
        }
    }

    private static class SSLConsoleLogger
    implements System.Logger {
        private final String loggerName;
        private final boolean useCompactFormat;

        SSLConsoleLogger(String loggerName, String options) {
            this.loggerName = loggerName;
            options = options.toLowerCase(Locale.ENGLISH);
            this.useCompactFormat = !options.contains("expand");
        }

        @Override
        public String getName() {
            return this.loggerName;
        }

        @Override
        public boolean isLoggable(System.Logger.Level level) {
            return level != System.Logger.Level.OFF;
        }

        @Override
        public void log(System.Logger.Level level, ResourceBundle rb, String message, Throwable thrwbl) {
            if (this.isLoggable(level)) {
                try {
                    String formatted = SSLSimpleFormatter.format(this, level, message, thrwbl);
                    System.err.write(formatted.getBytes(StandardCharsets.UTF_8));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }

        @Override
        public void log(System.Logger.Level level, ResourceBundle rb, String message, Object ... params) {
            if (this.isLoggable(level)) {
                try {
                    String formatted = SSLSimpleFormatter.format(this, level, message, params);
                    System.err.write(formatted.getBytes(StandardCharsets.UTF_8));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }
}

