/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.crypto.BadPaddingException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Authenticator;
import sun.security.ssl.ContentType;
import sun.security.ssl.DTLSRecord;
import sun.security.ssl.HandshakeHash;
import sun.security.ssl.InputRecord;
import sun.security.ssl.Plaintext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLContextImpl;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.TransportContext;

final class DTLSInputRecord
extends InputRecord
implements DTLSRecord {
    private DTLSReassembler reassembler = null;
    private int readEpoch = 0;
    private SSLContextImpl sslContext;

    DTLSInputRecord(HandshakeHash handshakeHash) {
        super(handshakeHash, SSLCipher.SSLReadCipher.nullDTlsReadCipher());
    }

    public void setTransportContext(TransportContext tc) {
        this.tc = tc;
    }

    public void setSSLContext(SSLContextImpl sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    void changeReadCiphers(SSLCipher.SSLReadCipher readCipher) {
        this.readCipher = readCipher;
        ++this.readEpoch;
    }

    @Override
    public void close() throws IOException {
        if (!this.isClosed) {
            super.close();
        }
    }

    @Override
    boolean isEmpty() {
        return this.reassembler == null || this.reassembler.isEmpty();
    }

    @Override
    int estimateFragmentSize(int packetSize) {
        if (packetSize > 0) {
            return this.readCipher.estimateFragmentSize(packetSize, 13);
        }
        return 16384;
    }

    @Override
    void expectingFinishFlight() {
        if (this.reassembler != null) {
            this.reassembler.expectingFinishFlight();
        }
    }

    @Override
    void finishHandshake() {
        this.reassembler = null;
    }

    @Override
    Plaintext acquirePlaintext() throws SSLProtocolException {
        if (this.reassembler != null) {
            return this.reassembler.acquirePlaintext();
        }
        return null;
    }

    @Override
    Plaintext[] decode(ByteBuffer[] srcs, int srcsOffset, int srcsLength) throws IOException, BadPaddingException {
        if (srcs == null || srcs.length == 0 || srcsLength == 0) {
            Plaintext[] plaintextArray;
            Plaintext pt = this.acquirePlaintext();
            if (pt == null) {
                plaintextArray = new Plaintext[]{};
            } else {
                Plaintext[] plaintextArray2 = new Plaintext[1];
                plaintextArray = plaintextArray2;
                plaintextArray2[0] = pt;
            }
            return plaintextArray;
        }
        if (srcsLength == 1) {
            return this.decode(srcs[srcsOffset]);
        }
        ByteBuffer packet = DTLSInputRecord.extract(srcs, srcsOffset, srcsLength, 13);
        return this.decode(packet);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Plaintext[] decode(ByteBuffer packet) throws SSLProtocolException {
        ByteBuffer plaintextFragment;
        if (this.isClosed) {
            return null;
        }
        if (SSLLogger.isOn && SSLLogger.isOn("packet")) {
            SSLLogger.fine("Raw read", packet);
        }
        int srcPos = packet.position();
        int srcLim = packet.limit();
        byte contentType = packet.get();
        byte majorVersion = packet.get();
        byte minorVersion = packet.get();
        byte[] recordEnS = new byte[8];
        packet.get(recordEnS);
        int recordEpoch = (recordEnS[0] & 0xFF) << 8 | recordEnS[1] & 0xFF;
        long recordSeq = ((long)recordEnS[2] & 0xFFL) << 40 | ((long)recordEnS[3] & 0xFFL) << 32 | ((long)recordEnS[4] & 0xFFL) << 24 | ((long)recordEnS[5] & 0xFFL) << 16 | ((long)recordEnS[6] & 0xFFL) << 8 | (long)recordEnS[7] & 0xFFL;
        int contentLen = (packet.get() & 0xFF) << 8 | packet.get() & 0xFF;
        if (SSLLogger.isOn && SSLLogger.isOn("record")) {
            SSLLogger.fine("READ: " + ProtocolVersion.nameOf(majorVersion, minorVersion) + " " + ContentType.nameOf(contentType) + ", length = " + contentLen, new Object[0]);
        }
        int recLim = Math.addExact(srcPos, 13 + contentLen);
        if (this.readEpoch > recordEpoch) {
            packet.position(recLim);
            if (SSLLogger.isOn && SSLLogger.isOn("record")) {
                SSLLogger.fine("READ: discard this old record", new Object[]{recordEnS});
            }
            return null;
        }
        if (this.readEpoch < recordEpoch) {
            Plaintext[] plaintextArray;
            if (contentType != ContentType.HANDSHAKE.id && contentType != ContentType.CHANGE_CIPHER_SPEC.id || this.reassembler == null && contentType != ContentType.HANDSHAKE.id || this.readEpoch < recordEpoch - 1) {
                packet.position(recLim);
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Premature record (epoch), discard it.", new Object[0]);
                }
                return null;
            }
            byte[] fragment = new byte[contentLen];
            packet.get(fragment);
            RecordFragment buffered = new RecordFragment(fragment, contentType, majorVersion, minorVersion, recordEnS, recordEpoch, recordSeq, true);
            if (this.reassembler == null) {
                this.reassembler = new DTLSReassembler(recordEpoch);
            }
            this.reassembler.queueUpFragment(buffered);
            packet.position(recLim);
            Plaintext pt = this.reassembler.acquirePlaintext();
            if (pt == null) {
                plaintextArray = null;
            } else {
                Plaintext[] plaintextArray2 = new Plaintext[1];
                plaintextArray = plaintextArray2;
                plaintextArray2[0] = pt;
            }
            return plaintextArray;
        }
        packet.limit(recLim);
        packet.position(srcPos + 13);
        try {
            Plaintext plaintext = this.readCipher.decrypt(contentType, packet, recordEnS);
            plaintextFragment = plaintext.fragment;
            contentType = plaintext.contentType;
        }
        catch (GeneralSecurityException gse) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("Discard invalid record: " + gse, new Object[0]);
            }
            Plaintext[] plaintextArray = null;
            return plaintextArray;
        }
        finally {
            packet.limit(srcLim);
            packet.position(recLim);
        }
        if (contentType != ContentType.CHANGE_CIPHER_SPEC.id && contentType != ContentType.HANDSHAKE.id) {
            if (this.reassembler != null && this.reassembler.handshakeEpoch < recordEpoch) {
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Cleanup the handshake reassembler", new Object[0]);
                }
                this.reassembler = null;
            }
            return new Plaintext[]{new Plaintext(contentType, majorVersion, minorVersion, recordEpoch, Authenticator.toLong(recordEnS), plaintextFragment)};
        }
        if (contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
            if (this.reassembler == null) {
                this.reassembler = new DTLSReassembler(recordEpoch);
            }
            this.reassembler.queueUpChangeCipherSpec(new RecordFragment(plaintextFragment, contentType, majorVersion, minorVersion, recordEnS, recordEpoch, recordSeq, false));
        } else {
            while (plaintextFragment.remaining() > 0) {
                HandshakeFragment hsFrag = DTLSInputRecord.parseHandshakeMessage(contentType, majorVersion, minorVersion, recordEnS, recordEpoch, recordSeq, plaintextFragment);
                if (hsFrag == null) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Invalid handshake message, discard it.", new Object[0]);
                    }
                    return null;
                }
                if (this.reassembler == null) {
                    this.reassembler = new DTLSReassembler(recordEpoch);
                }
                this.reassembler.queueUpHandshake(hsFrag);
            }
        }
        if (this.reassembler != null) {
            Plaintext[] plaintextArray;
            Plaintext pt = this.reassembler.acquirePlaintext();
            if (pt == null) {
                plaintextArray = null;
            } else {
                Plaintext[] plaintextArray3 = new Plaintext[1];
                plaintextArray = plaintextArray3;
                plaintextArray3[0] = pt;
            }
            return plaintextArray;
        }
        if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
            SSLLogger.fine("The reassembler is not initialized yet.", new Object[0]);
        }
        return null;
    }

    @Override
    int bytesInCompletePacket(ByteBuffer[] srcs, int srcsOffset, int srcsLength) throws IOException {
        return this.bytesInCompletePacket(srcs[srcsOffset]);
    }

    private int bytesInCompletePacket(ByteBuffer packet) throws SSLException {
        byte minorVersion;
        if (packet.remaining() < 13) {
            return -1;
        }
        int pos = packet.position();
        byte contentType = packet.get(pos);
        if (ContentType.valueOf(contentType) == null) {
            throw new SSLException("Unrecognized SSL message, plaintext connection?");
        }
        byte majorVersion = packet.get(pos + 1);
        if (!ProtocolVersion.isNegotiable(majorVersion, minorVersion = packet.get(pos + 2), true, false)) {
            throw new SSLException("Unrecognized record version " + ProtocolVersion.nameOf(majorVersion, minorVersion) + " , plaintext connection?");
        }
        int fragLen = ((packet.get(pos + 11) & 0xFF) << 8) + (packet.get(pos + 12) & 0xFF) + 13;
        if (fragLen > 18432) {
            throw new SSLException("Record overflow, fragment length (" + fragLen + ") MUST not exceed " + 18432);
        }
        return fragLen;
    }

    private static HandshakeFragment parseHandshakeMessage(byte contentType, byte majorVersion, byte minorVersion, byte[] recordEnS, int recordEpoch, long recordSeq, ByteBuffer plaintextFragment) throws SSLProtocolException {
        int remaining = plaintextFragment.remaining();
        if (remaining < 12) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("Discard invalid record: too small record to hold a handshake fragment", new Object[0]);
            }
            return null;
        }
        byte handshakeType = plaintextFragment.get();
        if (!SSLHandshake.isKnown(handshakeType)) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("Discard invalid record: unknown handshake type size, Handshake.msg_type = " + (handshakeType & 0xFF), new Object[0]);
            }
            return null;
        }
        int messageLength = (plaintextFragment.get() & 0xFF) << 16 | (plaintextFragment.get() & 0xFF) << 8 | plaintextFragment.get() & 0xFF;
        if (messageLength > SSLConfiguration.maxHandshakeMessageSize) {
            throw new SSLProtocolException("The size of the handshake message (" + messageLength + ") exceeds the maximum allowed size (" + SSLConfiguration.maxHandshakeMessageSize + ")");
        }
        int messageSeq = (plaintextFragment.get() & 0xFF) << 8 | plaintextFragment.get() & 0xFF;
        int fragmentOffset = (plaintextFragment.get() & 0xFF) << 16 | (plaintextFragment.get() & 0xFF) << 8 | plaintextFragment.get() & 0xFF;
        int fragmentLength = (plaintextFragment.get() & 0xFF) << 16 | (plaintextFragment.get() & 0xFF) << 8 | plaintextFragment.get() & 0xFF;
        if (remaining - 12 < fragmentLength) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("Discard invalid record: not a complete handshake fragment in the record", new Object[0]);
            }
            return null;
        }
        byte[] fragment = new byte[fragmentLength];
        plaintextFragment.get(fragment);
        return new HandshakeFragment(fragment, contentType, majorVersion, minorVersion, recordEnS, recordEpoch, recordSeq, handshakeType, messageLength, messageSeq, fragmentOffset, fragmentLength);
    }

    private static HandshakeFragment truncateChFragment(HandshakeFragment srcFrag, int limit) {
        return new HandshakeFragment(Arrays.copyOf(srcFrag.fragment, limit), srcFrag.contentType, srcFrag.majorVersion, srcFrag.minorVersion, srcFrag.recordEnS, srcFrag.recordEpoch, srcFrag.recordSeq, srcFrag.handshakeType, limit, srcFrag.messageSeq, srcFrag.fragmentOffset, limit);
    }

    final class DTLSReassembler {
        final int handshakeEpoch;
        TreeSet<RecordFragment> bufferedFragments = new TreeSet();
        HandshakeFlight handshakeFlight = new HandshakeFlight();
        HandshakeFlight precedingFlight = null;
        int nextRecordEpoch;
        long nextRecordSeq = 0L;
        boolean expectCCSFlight = false;
        boolean flightIsReady = false;
        boolean needToCheckFlight = false;

        DTLSReassembler(int handshakeEpoch) {
            this.handshakeEpoch = handshakeEpoch;
            this.nextRecordEpoch = handshakeEpoch;
            this.handshakeFlight.flightEpoch = handshakeEpoch;
        }

        void expectingFinishFlight() {
            this.expectCCSFlight = true;
        }

        void queueUpHandshake(HandshakeFragment hsf) throws SSLProtocolException {
            if (!this.isDesirable(hsf)) {
                return;
            }
            this.cleanUpRetransmit(hsf);
            boolean isMinimalFlightMessage = false;
            if (this.handshakeFlight.minMessageSeq == hsf.messageSeq) {
                isMinimalFlightMessage = true;
            } else if (this.precedingFlight != null && this.precedingFlight.minMessageSeq == hsf.messageSeq) {
                isMinimalFlightMessage = true;
            }
            if (isMinimalFlightMessage && hsf.fragmentOffset == 0 && hsf.handshakeType != SSLHandshake.FINISHED.id) {
                this.handshakeFlight.handshakeType = hsf.handshakeType;
                this.handshakeFlight.flightEpoch = hsf.recordEpoch;
                this.handshakeFlight.minMessageSeq = hsf.messageSeq;
            }
            if (hsf.handshakeType == SSLHandshake.FINISHED.id) {
                this.handshakeFlight.maxMessageSeq = hsf.messageSeq;
                this.handshakeFlight.maxRecordEpoch = hsf.recordEpoch;
                this.handshakeFlight.maxRecordSeq = hsf.recordSeq;
            } else {
                int n;
                if (this.handshakeFlight.maxMessageSeq < hsf.messageSeq) {
                    this.handshakeFlight.maxMessageSeq = hsf.messageSeq;
                }
                if ((n = hsf.recordEpoch - this.handshakeFlight.maxRecordEpoch) > 0) {
                    this.handshakeFlight.maxRecordEpoch = hsf.recordEpoch;
                    this.handshakeFlight.maxRecordSeq = hsf.recordSeq;
                } else if (n == 0 && this.handshakeFlight.maxRecordSeq < hsf.recordSeq) {
                    this.handshakeFlight.maxRecordSeq = hsf.recordSeq;
                }
            }
            boolean fragmented = hsf.fragmentOffset != 0 || hsf.fragmentLength != hsf.messageLength;
            List<HoleDescriptor> holes = this.handshakeFlight.holesMap.get(hsf.handshakeType);
            if (holes == null) {
                if (!fragmented) {
                    holes = Collections.emptyList();
                } else {
                    holes = new LinkedList<HoleDescriptor>();
                    holes.add(new HoleDescriptor(0, hsf.messageLength));
                }
                this.handshakeFlight.holesMap.put(hsf.handshakeType, holes);
                this.handshakeFlight.messageSeqMap.put(hsf.handshakeType, hsf.messageSeq);
            } else if (holes.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Have got the full message, discard it.", new Object[0]);
                }
                return;
            }
            if (fragmented) {
                int fragmentLimit = hsf.fragmentOffset + hsf.fragmentLength;
                for (int i = 0; i < holes.size(); ++i) {
                    HoleDescriptor hole = holes.get(i);
                    if (hole.limit <= hsf.fragmentOffset || hole.offset >= fragmentLimit) continue;
                    if (hole.offset > hsf.fragmentOffset || hole.limit < fragmentLimit) {
                        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                            SSLLogger.fine("Discard invalid record: handshake fragment ranges are overlapping", new Object[0]);
                        }
                        return;
                    }
                    holes.remove(i);
                    if (hsf.fragmentOffset > hole.offset) {
                        holes.add(new HoleDescriptor(hole.offset, hsf.fragmentOffset));
                    }
                    if (fragmentLimit >= hole.limit) break;
                    holes.add(new HoleDescriptor(fragmentLimit, hole.limit));
                    break;
                }
            }
            if (hsf.handshakeType == SSLHandshake.FINISHED.id) {
                this.bufferedFragments.add(hsf);
            } else {
                this.bufferFragment(hsf);
            }
        }

        void queueUpChangeCipherSpec(RecordFragment rf) throws SSLProtocolException {
            if (!this.isDesirable(rf)) {
                return;
            }
            this.cleanUpRetransmit(rf);
            if (this.expectCCSFlight) {
                this.handshakeFlight.handshakeType = HandshakeFlight.HF_UNKNOWN;
                this.handshakeFlight.flightEpoch = rf.recordEpoch;
            }
            if (this.handshakeFlight.maxRecordSeq < rf.recordSeq) {
                this.handshakeFlight.maxRecordSeq = rf.recordSeq;
            }
            this.bufferFragment(rf);
        }

        void queueUpFragment(RecordFragment rf) throws SSLProtocolException {
            if (!this.isDesirable(rf)) {
                return;
            }
            this.cleanUpRetransmit(rf);
            this.bufferFragment(rf);
        }

        private void bufferFragment(RecordFragment rf) {
            this.bufferedFragments.add(rf);
            if (this.flightIsReady) {
                this.flightIsReady = false;
            }
            if (!this.needToCheckFlight) {
                this.needToCheckFlight = true;
            }
        }

        private void cleanUpRetransmit(RecordFragment rf) {
            boolean isNewFlight = false;
            if (this.precedingFlight != null) {
                if (this.precedingFlight.flightEpoch < rf.recordEpoch) {
                    isNewFlight = true;
                } else if (rf instanceof HandshakeFragment) {
                    HandshakeFragment hsf = (HandshakeFragment)rf;
                    if (this.precedingFlight.maxMessageSeq < hsf.messageSeq) {
                        isNewFlight = true;
                    }
                } else if (rf.contentType != ContentType.CHANGE_CIPHER_SPEC.id && this.precedingFlight.maxRecordEpoch < rf.recordEpoch) {
                    isNewFlight = true;
                }
            }
            if (!isNewFlight) {
                return;
            }
            Iterator<RecordFragment> it = this.bufferedFragments.iterator();
            while (it.hasNext()) {
                RecordFragment frag = it.next();
                boolean isOld = false;
                if (frag.recordEpoch < this.precedingFlight.maxRecordEpoch) {
                    isOld = true;
                } else if (frag.recordEpoch == this.precedingFlight.maxRecordEpoch && frag.recordSeq <= this.precedingFlight.maxRecordSeq) {
                    isOld = true;
                }
                if (!isOld && frag instanceof HandshakeFragment) {
                    HandshakeFragment hsf = (HandshakeFragment)frag;
                    boolean bl = isOld = hsf.messageSeq <= this.precedingFlight.maxMessageSeq;
                }
                if (!isOld) break;
                it.remove();
            }
            this.precedingFlight = null;
        }

        private boolean isDesirable(RecordFragment rf) throws SSLProtocolException {
            int previousEpoch = this.nextRecordEpoch - 1;
            if (rf.recordEpoch < previousEpoch) {
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Too old epoch to use this record, discard it.", new Object[0]);
                }
                return false;
            }
            if (rf.recordEpoch == previousEpoch) {
                boolean isDesired = true;
                if (this.precedingFlight == null) {
                    isDesired = false;
                } else if (rf instanceof HandshakeFragment) {
                    HandshakeFragment hsf = (HandshakeFragment)rf;
                    if (this.precedingFlight.minMessageSeq > hsf.messageSeq) {
                        isDesired = false;
                    }
                } else if (rf.contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                    if (this.precedingFlight.flightEpoch != rf.recordEpoch) {
                        isDesired = false;
                    }
                } else if (rf.recordEpoch < this.precedingFlight.maxRecordEpoch || rf.recordEpoch == this.precedingFlight.maxRecordEpoch && rf.recordSeq <= this.precedingFlight.maxRecordSeq) {
                    isDesired = false;
                }
                if (!isDesired) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Too old retransmission to use, discard it.", new Object[0]);
                    }
                    return false;
                }
            } else if (rf.recordEpoch == this.nextRecordEpoch && this.nextRecordSeq > rf.recordSeq) {
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Lagging behind record (sequence), discard it.", new Object[0]);
                }
                return false;
            }
            if (rf.recordEpoch == this.handshakeEpoch && rf instanceof HandshakeFragment) {
                Integer cachedMsgSeq;
                HandshakeFragment hsf = (HandshakeFragment)rf;
                if (this.handshakeFlight.holesMap.containsKey(hsf.handshakeType) && (cachedMsgSeq = this.handshakeFlight.messageSeqMap.get(hsf.handshakeType)) != null && cachedMsgSeq != hsf.messageSeq) {
                    throw new SSLProtocolException("Two message sequence numbers are used for the same handshake message (" + SSLHandshake.nameOf(hsf.handshakeType) + ")");
                }
            }
            return true;
        }

        private boolean isEmpty() {
            return this.bufferedFragments.isEmpty() || !this.flightIsReady && !this.needToCheckFlight || this.needToCheckFlight && !this.flightIsReady();
        }

        Plaintext acquirePlaintext() throws SSLProtocolException {
            Plaintext plaintext;
            if (this.bufferedFragments.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("No received handshake messages", new Object[0]);
                }
                return null;
            }
            if (!this.flightIsReady && this.needToCheckFlight) {
                this.flightIsReady = this.flightIsReady();
                if (this.flightIsReady && this.handshakeFlight.isRetransmitOf(this.precedingFlight)) {
                    this.bufferedFragments.clear();
                    this.resetHandshakeFlight(this.precedingFlight);
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Received a retransmission flight.", new Object[0]);
                    }
                    return Plaintext.PLAINTEXT_NULL;
                }
                this.needToCheckFlight = false;
            }
            if (!this.flightIsReady) {
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("The handshake flight is not ready to use: " + this.handshakeFlight.handshakeType, new Object[0]);
                }
                return null;
            }
            RecordFragment rFrag = this.bufferedFragments.first();
            if (!rFrag.isCiphertext) {
                plaintext = this.acquireHandshakeMessage();
                if (this.bufferedFragments.isEmpty()) {
                    this.handshakeFlight.holesMap.clear();
                    this.precedingFlight = (HandshakeFlight)this.handshakeFlight.clone();
                    this.resetHandshakeFlight(this.precedingFlight);
                    if (this.expectCCSFlight && this.precedingFlight.handshakeType == HandshakeFlight.HF_UNKNOWN) {
                        this.expectCCSFlight = false;
                    }
                }
            } else {
                plaintext = this.acquireCachedMessage();
            }
            return plaintext;
        }

        private void resetHandshakeFlight(HandshakeFlight prev) {
            this.handshakeFlight.handshakeType = HandshakeFlight.HF_UNKNOWN;
            this.handshakeFlight.flightEpoch = prev.maxRecordEpoch;
            this.handshakeFlight.minMessageSeq = prev.flightEpoch != prev.maxRecordEpoch ? 0 : prev.maxMessageSeq + 1;
            this.handshakeFlight.maxMessageSeq = 0;
            this.handshakeFlight.maxRecordEpoch = this.handshakeFlight.flightEpoch;
            this.handshakeFlight.maxRecordSeq = prev.maxRecordSeq + 1L;
            this.handshakeFlight.holesMap.clear();
            this.handshakeFlight.messageSeqMap.clear();
            this.flightIsReady = false;
            this.needToCheckFlight = false;
        }

        private Plaintext acquireCachedMessage() throws SSLProtocolException {
            ByteBuffer plaintextFragment;
            RecordFragment rFrag = this.bufferedFragments.first();
            if (DTLSInputRecord.this.readEpoch != rFrag.recordEpoch) {
                if (DTLSInputRecord.this.readEpoch > rFrag.recordEpoch) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Discard old buffered ciphertext fragments.", new Object[0]);
                    }
                    this.bufferedFragments.remove(rFrag);
                }
                if (this.flightIsReady) {
                    this.flightIsReady = false;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Not yet ready to decrypt the cached fragments.", new Object[0]);
                }
                return null;
            }
            this.bufferedFragments.remove(rFrag);
            ByteBuffer fragment = ByteBuffer.wrap(rFrag.fragment);
            try {
                Plaintext plaintext = DTLSInputRecord.this.readCipher.decrypt(rFrag.contentType, fragment, rFrag.recordEnS);
                plaintextFragment = plaintext.fragment;
                rFrag.contentType = plaintext.contentType;
            }
            catch (GeneralSecurityException gse) {
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Discard invalid record: ", gse);
                }
                return null;
            }
            if (rFrag.contentType == ContentType.HANDSHAKE.id) {
                while (plaintextFragment.remaining() > 0) {
                    HandshakeFragment hsFrag = DTLSInputRecord.parseHandshakeMessage(rFrag.contentType, rFrag.majorVersion, rFrag.minorVersion, rFrag.recordEnS, rFrag.recordEpoch, rFrag.recordSeq, plaintextFragment);
                    if (hsFrag == null) {
                        if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                            SSLLogger.fine("Invalid handshake fragment, discard it", plaintextFragment);
                        }
                        return null;
                    }
                    this.queueUpHandshake(hsFrag);
                    if (hsFrag.handshakeType == SSLHandshake.FINISHED.id) continue;
                    this.flightIsReady = false;
                    this.needToCheckFlight = true;
                }
                return this.acquirePlaintext();
            }
            return new Plaintext(rFrag.contentType, rFrag.majorVersion, rFrag.minorVersion, rFrag.recordEpoch, Authenticator.toLong(rFrag.recordEnS), plaintextFragment);
        }

        private Plaintext acquireHandshakeMessage() {
            RecordFragment rFrag = this.bufferedFragments.first();
            if (rFrag.contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                this.nextRecordEpoch = rFrag.recordEpoch + 1;
                this.nextRecordSeq = 0L;
                this.bufferedFragments.remove(rFrag);
                return new Plaintext(rFrag.contentType, rFrag.majorVersion, rFrag.minorVersion, rFrag.recordEpoch, Authenticator.toLong(rFrag.recordEnS), ByteBuffer.wrap(rFrag.fragment));
            }
            HandshakeFragment hsFrag = (HandshakeFragment)rFrag;
            if (hsFrag.messageLength == hsFrag.fragmentLength && hsFrag.fragmentOffset == 0) {
                this.bufferedFragments.remove(rFrag);
                this.nextRecordSeq = hsFrag.recordSeq + 1L;
                byte[] recordFrag = new byte[hsFrag.messageLength + 4];
                Plaintext plaintext = new Plaintext(hsFrag.contentType, hsFrag.majorVersion, hsFrag.minorVersion, hsFrag.recordEpoch, Authenticator.toLong(hsFrag.recordEnS), ByteBuffer.wrap(recordFrag));
                recordFrag[0] = hsFrag.handshakeType;
                recordFrag[1] = (byte)(hsFrag.messageLength >>> 16 & 0xFF);
                recordFrag[2] = (byte)(hsFrag.messageLength >>> 8 & 0xFF);
                recordFrag[3] = (byte)(hsFrag.messageLength & 0xFF);
                System.arraycopy(hsFrag.fragment, 0, recordFrag, 4, hsFrag.fragmentLength);
                this.handshakeHashing(hsFrag, plaintext);
                return plaintext;
            }
            byte[] recordFrag = new byte[hsFrag.messageLength + 4];
            Plaintext plaintext = new Plaintext(hsFrag.contentType, hsFrag.majorVersion, hsFrag.minorVersion, hsFrag.recordEpoch, Authenticator.toLong(hsFrag.recordEnS), ByteBuffer.wrap(recordFrag));
            recordFrag[0] = hsFrag.handshakeType;
            recordFrag[1] = (byte)(hsFrag.messageLength >>> 16 & 0xFF);
            recordFrag[2] = (byte)(hsFrag.messageLength >>> 8 & 0xFF);
            recordFrag[3] = (byte)(hsFrag.messageLength & 0xFF);
            int msgSeq = hsFrag.messageSeq;
            long maxRecodeSN = hsFrag.recordSeq;
            HandshakeFragment hmFrag = hsFrag;
            do {
                System.arraycopy(hmFrag.fragment, 0, recordFrag, hmFrag.fragmentOffset + 4, hmFrag.fragmentLength);
                this.bufferedFragments.remove(rFrag);
                if (maxRecodeSN < hmFrag.recordSeq) {
                    maxRecodeSN = hmFrag.recordSeq;
                }
                if (this.bufferedFragments.isEmpty()) continue;
                rFrag = this.bufferedFragments.first();
                if (rFrag.contentType != ContentType.HANDSHAKE.id) break;
                hmFrag = (HandshakeFragment)rFrag;
            } while (!this.bufferedFragments.isEmpty() && msgSeq == hmFrag.messageSeq);
            this.handshakeHashing(hsFrag, plaintext);
            this.nextRecordSeq = maxRecodeSN + 1L;
            return plaintext;
        }

        boolean flightIsReady() {
            byte flightType = this.handshakeFlight.handshakeType;
            if (flightType == HandshakeFlight.HF_UNKNOWN) {
                if (this.expectCCSFlight) {
                    boolean isReady = this.hasFinishedMessage(this.bufferedFragments);
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Has the final flight been received? " + isReady, new Object[0]);
                    }
                    return isReady;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("No flight is received yet.", new Object[0]);
                }
                return false;
            }
            if (flightType == SSLHandshake.CLIENT_HELLO.id || flightType == SSLHandshake.HELLO_REQUEST.id || flightType == SSLHandshake.HELLO_VERIFY_REQUEST.id) {
                boolean isReady = this.hasCompleted(flightType);
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Is the handshake message completed? " + isReady, new Object[0]);
                }
                return isReady;
            }
            if (flightType == SSLHandshake.SERVER_HELLO.id) {
                if (!this.hasCompleted(flightType)) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("The ServerHello message is not completed yet.", new Object[0]);
                    }
                    return false;
                }
                if (this.hasFinishedMessage(this.bufferedFragments)) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("It's an abbreviated handshake.", new Object[0]);
                    }
                    return true;
                }
                List<HoleDescriptor> holes = this.handshakeFlight.holesMap.get(SSLHandshake.SERVER_HELLO_DONE.id);
                if (holes == null || !holes.isEmpty()) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Not yet got the ServerHelloDone message", new Object[0]);
                    }
                    return false;
                }
                boolean isReady = this.hasCompleted(this.bufferedFragments, this.handshakeFlight.minMessageSeq, this.handshakeFlight.maxMessageSeq);
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Is the ServerHello flight (message " + this.handshakeFlight.minMessageSeq + "-" + this.handshakeFlight.maxMessageSeq + ") completed? " + isReady, new Object[0]);
                }
                return isReady;
            }
            if (flightType == SSLHandshake.CERTIFICATE.id || flightType == SSLHandshake.CLIENT_KEY_EXCHANGE.id) {
                if (!this.hasCompleted(flightType)) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("The ClientKeyExchange or client Certificate message is not completed yet.", new Object[0]);
                    }
                    return false;
                }
                if (flightType == SSLHandshake.CERTIFICATE.id && this.needClientVerify(this.bufferedFragments) && !this.hasCompleted(SSLHandshake.CERTIFICATE_VERIFY.id)) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Not yet have the CertificateVerify message", new Object[0]);
                    }
                    return false;
                }
                if (!this.hasFinishedMessage(this.bufferedFragments)) {
                    if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                        SSLLogger.fine("Not yet have the ChangeCipherSpec and Finished messages", new Object[0]);
                    }
                    return false;
                }
                boolean isReady = this.hasCompleted(this.bufferedFragments, this.handshakeFlight.minMessageSeq, this.handshakeFlight.maxMessageSeq);
                if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                    SSLLogger.fine("Is the ClientKeyExchange flight (message " + this.handshakeFlight.minMessageSeq + "-" + this.handshakeFlight.maxMessageSeq + ") completed? " + isReady, new Object[0]);
                }
                return isReady;
            }
            if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                SSLLogger.fine("Need to receive more handshake messages", new Object[0]);
            }
            return false;
        }

        private boolean hasFinishedMessage(Set<RecordFragment> fragments) {
            boolean hasCCS = false;
            boolean hasFin = false;
            for (RecordFragment fragment : fragments) {
                if (fragment.contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                    if (hasFin) {
                        return true;
                    }
                    hasCCS = true;
                    continue;
                }
                if (fragment.contentType != ContentType.HANDSHAKE.id || !fragment.isCiphertext) continue;
                if (hasCCS) {
                    return true;
                }
                hasFin = true;
            }
            return false;
        }

        private boolean needClientVerify(Set<RecordFragment> fragments) {
            for (RecordFragment rFrag : fragments) {
                if (rFrag.contentType != ContentType.HANDSHAKE.id || rFrag.isCiphertext) break;
                HandshakeFragment hsFrag = (HandshakeFragment)rFrag;
                if (hsFrag.handshakeType != SSLHandshake.CERTIFICATE.id) continue;
                return rFrag.fragment != null && rFrag.fragment.length > 28;
            }
            return false;
        }

        private boolean hasCompleted(byte handshakeType) {
            List<HoleDescriptor> holes = this.handshakeFlight.holesMap.get(handshakeType);
            if (holes == null) {
                return false;
            }
            return holes.isEmpty();
        }

        private boolean hasCompleted(Set<RecordFragment> fragments, int presentMsgSeq, int endMsgSeq) {
            for (RecordFragment rFrag : fragments) {
                if (rFrag.contentType != ContentType.HANDSHAKE.id || rFrag.isCiphertext) break;
                HandshakeFragment hsFrag = (HandshakeFragment)rFrag;
                if (hsFrag.messageSeq == presentMsgSeq) continue;
                if (hsFrag.messageSeq != presentMsgSeq + 1) break;
                if (!this.hasCompleted(hsFrag.handshakeType)) {
                    return false;
                }
                presentMsgSeq = hsFrag.messageSeq;
            }
            return presentMsgSeq >= endMsgSeq;
        }

        private void handshakeHashing(HandshakeFragment hsFrag, Plaintext plaintext) {
            byte hsType = hsFrag.handshakeType;
            if (!DTLSInputRecord.this.handshakeHash.isHashable(hsType)) {
                return;
            }
            plaintext.fragment.position(4);
            byte[] temporary = new byte[plaintext.fragment.remaining() + 12];
            temporary[0] = hsFrag.handshakeType;
            temporary[1] = (byte)(hsFrag.messageLength >> 16 & 0xFF);
            temporary[2] = (byte)(hsFrag.messageLength >> 8 & 0xFF);
            temporary[3] = (byte)(hsFrag.messageLength & 0xFF);
            temporary[4] = (byte)(hsFrag.messageSeq >> 8 & 0xFF);
            temporary[5] = (byte)(hsFrag.messageSeq & 0xFF);
            temporary[6] = 0;
            temporary[7] = 0;
            temporary[8] = 0;
            temporary[9] = temporary[1];
            temporary[10] = temporary[2];
            temporary[11] = temporary[3];
            plaintext.fragment.get(temporary, 12, plaintext.fragment.remaining());
            DTLSInputRecord.this.handshakeHash.receive(temporary);
            plaintext.fragment.position(0);
        }
    }

    private static class RecordFragment
    implements Comparable<RecordFragment> {
        boolean isCiphertext;
        byte contentType;
        byte majorVersion;
        byte minorVersion;
        int recordEpoch;
        long recordSeq;
        byte[] recordEnS;
        byte[] fragment;

        RecordFragment(ByteBuffer fragBuf, byte contentType, byte majorVersion, byte minorVersion, byte[] recordEnS, int recordEpoch, long recordSeq, boolean isCiphertext) {
            this((byte[])null, contentType, majorVersion, minorVersion, recordEnS, recordEpoch, recordSeq, isCiphertext);
            this.fragment = new byte[fragBuf.remaining()];
            fragBuf.get(this.fragment);
        }

        RecordFragment(byte[] fragment, byte contentType, byte majorVersion, byte minorVersion, byte[] recordEnS, int recordEpoch, long recordSeq, boolean isCiphertext) {
            this.isCiphertext = isCiphertext;
            this.contentType = contentType;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.recordEpoch = recordEpoch;
            this.recordSeq = recordSeq;
            this.recordEnS = recordEnS;
            this.fragment = fragment;
        }

        @Override
        public int compareTo(RecordFragment o) {
            if (this.contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                if (o.contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                    return Integer.compare(this.recordEpoch, o.recordEpoch);
                }
                if (this.recordEpoch == o.recordEpoch && o.contentType == ContentType.HANDSHAKE.id) {
                    return 1;
                }
            } else if (o.contentType == ContentType.CHANGE_CIPHER_SPEC.id) {
                if (this.recordEpoch == o.recordEpoch && this.contentType == ContentType.HANDSHAKE.id) {
                    return -1;
                }
                return this.compareToSequence(o.recordEpoch, o.recordSeq);
            }
            return this.compareToSequence(o.recordEpoch, o.recordSeq);
        }

        int compareToSequence(int epoch, long seq) {
            if (this.recordEpoch > epoch) {
                return 1;
            }
            if (this.recordEpoch == epoch) {
                return Long.compare(this.recordSeq, seq);
            }
            return -1;
        }
    }

    private static final class HandshakeFragment
    extends RecordFragment {
        byte handshakeType;
        int messageSeq;
        int messageLength;
        int fragmentOffset;
        int fragmentLength;

        HandshakeFragment(byte[] fragment, byte contentType, byte majorVersion, byte minorVersion, byte[] recordEnS, int recordEpoch, long recordSeq, byte handshakeType, int messageLength, int messageSeq, int fragmentOffset, int fragmentLength) {
            super(fragment, contentType, majorVersion, minorVersion, recordEnS, recordEpoch, recordSeq, false);
            this.handshakeType = handshakeType;
            this.messageSeq = messageSeq;
            this.messageLength = messageLength;
            this.fragmentOffset = fragmentOffset;
            this.fragmentLength = fragmentLength;
        }

        @Override
        public int compareTo(RecordFragment o) {
            if (o instanceof HandshakeFragment) {
                HandshakeFragment other = (HandshakeFragment)o;
                if (this.messageSeq != other.messageSeq) {
                    return this.messageSeq - other.messageSeq;
                }
                if (this.fragmentOffset != other.fragmentOffset) {
                    return this.fragmentOffset - other.fragmentOffset;
                }
                if (this.fragmentLength == other.fragmentLength) {
                    return 0;
                }
                return this.compareToSequence(o.recordEpoch, o.recordSeq);
            }
            return super.compareTo(o);
        }
    }

    private static final class HandshakeFlight
    implements Cloneable {
        static final byte HF_UNKNOWN = SSLHandshake.NOT_APPLICABLE.id;
        byte handshakeType = HF_UNKNOWN;
        int flightEpoch = 0;
        int minMessageSeq = 0;
        int maxMessageSeq = 0;
        int maxRecordEpoch = 0;
        long maxRecordSeq = -1L;
        HashMap<Byte, List<HoleDescriptor>> holesMap = new HashMap(5);
        HashMap<Byte, Integer> messageSeqMap = new HashMap(5);

        HandshakeFlight() {
        }

        boolean isRetransmitOf(HandshakeFlight hs) {
            return hs != null && this.handshakeType == hs.handshakeType && this.minMessageSeq == hs.minMessageSeq;
        }

        public Object clone() {
            HandshakeFlight hf = new HandshakeFlight();
            hf.handshakeType = this.handshakeType;
            hf.flightEpoch = this.flightEpoch;
            hf.minMessageSeq = this.minMessageSeq;
            hf.maxMessageSeq = this.maxMessageSeq;
            hf.maxRecordEpoch = this.maxRecordEpoch;
            hf.maxRecordSeq = this.maxRecordSeq;
            hf.holesMap = new HashMap<Byte, List<HoleDescriptor>>(this.holesMap);
            hf.messageSeqMap = new HashMap<Byte, Integer>(this.messageSeqMap);
            return hf;
        }
    }

    private static final class HoleDescriptor {
        int offset;
        int limit;

        HoleDescriptor(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }
    }
}

