package com.eternitywall.ots.attestation;

import com.eternitywall.ots.BlockHeader;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import com.eternitywall.ots.exceptions.VerificationException;

import java.util.Arrays;

/**
 * Litecoin Block Header Attestation.
 *
 * @see TimeAttestation
 */
public class LitecoinBlockHeaderAttestation extends TimeAttestation {

    public static byte[] _TAG = {(byte) 0x06, (byte) 0x86, (byte) 0x9a, (byte) 0x0d, (byte) 0x73, (byte) 0xd7, (byte) 0x1b, (byte) 0x45};
    public static String chain = "litecoin";

    @Override
    public byte[] _TAG() {
        return LitecoinBlockHeaderAttestation._TAG;
    }

    private int height = 0;

    public int getHeight() {
        return height;
    }

    public LitecoinBlockHeaderAttestation(int height) {
        super();
        this.height = height;
    }

    public static LitecoinBlockHeaderAttestation deserialize(StreamDeserializationContext ctxPayload) {
        int height = ctxPayload.readVaruint();
        return new LitecoinBlockHeaderAttestation(height);
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeVaruint(height);
    }

    public String toString() {
        return "LitecoinBlockHeaderAttestation(" + height + ")";
    }

    @Override
    public int compareTo(TimeAttestation other) {
        LitecoinBlockHeaderAttestation that = (LitecoinBlockHeaderAttestation) other;

        return height - that.height;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LitecoinBlockHeaderAttestation)) {
            return false;
        }

        LitecoinBlockHeaderAttestation otherAttestation = (LitecoinBlockHeaderAttestation) other;

        if (!Arrays.equals(_TAG(), otherAttestation._TAG())) {
            return false;
        }

        return height == otherAttestation.height;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_TAG()) ^ height;
    }

    /**
     * Verify attestation against a block header
     * Returns the block time on success; raises VerificationError on failure.
     */
    public Long verifyAgainstBlockheader(byte[] digest, BlockHeader block) throws VerificationException {
        if (digest.length != 32) {
            throw new VerificationException("Expected digest with length 32 bytes; got " + digest.length + " bytes");
        } else if (!Arrays.equals(digest, Utils.hexToBytes(block.getMerkleroot()))) {
            throw new VerificationException("Digest does not match merkleroot");
        }

        return block.getTime();
    }
}
