package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Placeholder for attestations that don't support
 *
 * @see com.eternitywall.ots.attestation.TimeAttestation
 */
public class UnknownAttestation extends TimeAttestation {
    byte[] payload;

    public static byte[] _TAG = new byte[]{};

    @Override
    public byte[] _TAG() {
        return _TAG;
    }

    UnknownAttestation(byte[] tag, byte[] payload) {
        super();
        this._TAG = tag;
        this.payload = payload;
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeBytes(this.payload);
    }

    public static UnknownAttestation deserialize(StreamDeserializationContext ctxPayload, byte[] tag) {
        byte[] payload = ctxPayload.readVarbytes(_MAX_PAYLOAD_SIZE);

        return new UnknownAttestation(tag, payload);
    }

    public String toString() {
        return "UnknownAttestation " + Utils.bytesToHex(_TAG()) + ' ' + Utils.bytesToHex(payload);
    }

    @Override
    public int compareTo(TimeAttestation other) {
        UnknownAttestation otherAttestation = (UnknownAttestation) other;

        return Utils.compare(payload, otherAttestation.payload);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UnknownAttestation)) {
            return false;
        }

        UnknownAttestation otherAttestation = (UnknownAttestation) other;

        if (!Arrays.equals(_TAG(), otherAttestation._TAG())) {
            return false;
        }

        return Arrays.equals(payload, otherAttestation.payload);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_TAG()) ^ Arrays.hashCode(payload);
    }
}
