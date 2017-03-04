package com.eternitywall;

import org.bouncycastle.util.Arrays;

import java.util.logging.Logger;

/**
 * Placeholder for attestations that don't support
 *
 * @extends com.eternitywall.TimeAttestation
 */
class UnknownAttestation extends TimeAttestation {

    private static Logger log = Logger.getLogger(UnknownAttestation.class.getName());

    byte[] payload;

    public byte[] _TAG = new byte[]{};

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
        return "com.eternitywall.UnknownAttestation " + this._TAG() + ' ' + this.payload;
    }


    @Override
    public int compareTo(TimeAttestation o) {
        UnknownAttestation ota = (UnknownAttestation) o;
        return Arrays.compareUnsigned(this.payload, ota.payload) ;
    }
}
