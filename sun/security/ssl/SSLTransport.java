/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.net.ssl.SSLHandshakeException;
import sun.security.ssl.Alert;
import sun.security.ssl.ContentType;
import sun.security.ssl.Plaintext;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.TransportContext;

interface SSLTransport {
    public String getPeerHost();

    public int getPeerPort();

    default public void shutdown() throws IOException {
    }

    public boolean useDelegatedTask();

    public static Plaintext decode(TransportContext context, ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength) throws IOException {
        Plaintext[] plaintexts;
        try {
            plaintexts = context.inputRecord.decode(srcs, srcsOffset, srcsLength);
        }
        catch (UnsupportedOperationException unsoe) {
            if (!context.sslContext.isDTLS()) {
                context.outputRecord.encodeV2NoCipher();
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.finest("may be talking to SSLv2", new Object[0]);
                }
            }
            throw context.fatal(Alert.UNEXPECTED_MESSAGE, unsoe);
        }
        catch (AEADBadTagException bte) {
            throw context.fatal(Alert.BAD_RECORD_MAC, bte);
        }
        catch (BadPaddingException bpe) {
            Alert alert = context.handshakeContext != null ? Alert.HANDSHAKE_FAILURE : Alert.BAD_RECORD_MAC;
            throw context.fatal(alert, bpe);
        }
        catch (SSLHandshakeException she) {
            throw context.fatal(Alert.HANDSHAKE_FAILURE, she);
        }
        catch (EOFException eofe) {
            throw eofe;
        }
        catch (InterruptedIOException | SocketException se) {
            throw se;
        }
        catch (IOException ioe) {
            throw context.fatal(Alert.UNEXPECTED_MESSAGE, ioe);
        }
        if (plaintexts == null || plaintexts.length == 0) {
            return Plaintext.PLAINTEXT_NULL;
        }
        Plaintext finalPlaintext = Plaintext.PLAINTEXT_NULL;
        for (Plaintext plainText : plaintexts) {
            if (plainText == Plaintext.PLAINTEXT_NULL) {
                if (context.handshakeContext != null && context.handshakeContext.sslConfig.enableRetransmissions && context.sslContext.isDTLS()) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,verbose")) {
                        SSLLogger.finest("retransmitted handshake flight", new Object[0]);
                    }
                    context.outputRecord.launchRetransmission();
                }
            } else if (plainText != null && plainText.contentType != ContentType.APPLICATION_DATA.id) {
                context.dispatch(plainText);
            }
            if (plainText == null) {
                plainText = Plaintext.PLAINTEXT_NULL;
            } else if (plainText.contentType == ContentType.APPLICATION_DATA.id) {
                if (!context.isNegotiated) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,verbose")) {
                        SSLLogger.warning("unexpected application data before handshake completion", new Object[0]);
                    }
                    throw context.fatal(Alert.UNEXPECTED_MESSAGE, "Receiving application data before handshake complete");
                }
                if (dsts != null && dstsLength > 0) {
                    ByteBuffer fragment = plainText.fragment;
                    int remains = fragment.remaining();
                    int limit = dstsOffset + dstsLength;
                    for (int i = dstsOffset; i < limit && remains > 0; ++i) {
                        int amount = Math.min(dsts[i].remaining(), remains);
                        fragment.limit(fragment.position() + amount);
                        dsts[i].put(fragment);
                        remains -= amount;
                        if (dsts[i].hasRemaining()) continue;
                        ++dstsOffset;
                    }
                    if (remains > 0) {
                        throw context.fatal(Alert.INTERNAL_ERROR, "no sufficient room in the destination buffers");
                    }
                }
            }
            finalPlaintext = plainText;
        }
        return finalPlaintext;
    }
}

