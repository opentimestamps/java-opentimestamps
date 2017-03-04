package com.eternitywall;
/**
 * com.eternitywall.TimeAttestation module.
 *
 * @module com.eternitywall.TimeAttestation
 * @author EternityWall
 * @license LPGL3
 */


import com.eternitywall.op.Op;

import java.util.Arrays;
import java.util.logging.Logger;

/** Class representing com.eternitywall.Timestamp signature verification */
class TimeAttestation  implements Comparable<TimeAttestation> {


    private static Logger log = Logger.getLogger(TimeAttestation.class.getName());

    public static int _TAG_SIZE = 8;

    public static int _MAX_PAYLOAD_SIZE = 8192;


    public byte[] _TAG;

    public byte[] _TAG() {
        return new byte[]{};
    }


    /**
     * Deserialize a general Time Attestation to the specific subclass Attestation.
     * @param {com.eternitywall.StreamDeserializationContext} ctx - The stream deserialization context.
     * @return {Attestation} The specific subclass Attestation.
     */
    public static TimeAttestation deserialize(StreamDeserializationContext ctx) {
        // console.log('attestation deserialize');

        byte[] tag = ctx.readBytes(_TAG_SIZE);
        // console.log('tag: ', com.eternitywall.Utils.bytesToHex(tag));

        byte[] serializedAttestation = ctx.readVarbytes(_MAX_PAYLOAD_SIZE);
        // console.log('serializedAttestation: ', com.eternitywall.Utils.bytesToHex(serializedAttestation));

        StreamDeserializationContext ctxPayload = new StreamDeserializationContext(serializedAttestation);

    /* eslint no-use-before-define: ["error", { "classes": false }] */
        if (Arrays.equals(tag, PendingAttestation._TAG) == true) {
            // console.log('tag(com.eternitywall.PendingAttestation)');
            return PendingAttestation.deserialize(ctxPayload);
        } else if (Arrays.equals(tag, BitcoinBlockHeaderAttestation._TAG) == true) {
            // console.log('tag(com.eternitywall.BitcoinBlockHeaderAttestation)');
            return BitcoinBlockHeaderAttestation.deserialize(ctxPayload);
        }
        return UnknownAttestation.deserialize(ctxPayload, tag);
    }

    /**
     * Serialize a a general Time Attestation to the specific subclass Attestation.
     * @param {com.eternitywall.StreamSerializationContext} ctx - The output stream serialization context.
     */
    public void serialize(StreamSerializationContext ctx) {
        ctx.writeBytes(this._TAG());
        StreamSerializationContext ctxPayload = new StreamSerializationContext();
        serializePayload(ctxPayload);
        ctx.writeVarbytes(ctxPayload.getOutput());
    }

    public void serializePayload(StreamSerializationContext ctxPayload) {

    }

    @Override
    public int compareTo(TimeAttestation o) {
        int deltaTag = org.bouncycastle.util.Arrays.compareUnsigned(this._TAG(),o._TAG());
        if( deltaTag == 0){
            return this.compareTo(o);
        } else {
            return deltaTag;
        }
    }
}
