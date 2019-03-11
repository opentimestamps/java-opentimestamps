package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;

import java.util.Arrays;

/**
 * Ethereum Block Header Attestation.
 *
 * @see com.eternitywall.ots.attestation.TimeAttestation
 */
public class EthereumBlockHeaderAttestation extends TimeAttestation {

    public static byte[] _TAG = {(byte) 0x30, (byte) 0xfe, (byte) 0x80, (byte) 0x87, (byte) 0xb5, (byte) 0xc7, (byte) 0xea, (byte) 0xd7};
    public static String chain = "ethereum";

    @Override
    public byte[] _TAG() {
        return EthereumBlockHeaderAttestation._TAG;
    }

    private int height = 0;

    public int getHeight() {
        return height;
    }

    EthereumBlockHeaderAttestation(int height) {
        super();
        this.height = height;
    }

    public static EthereumBlockHeaderAttestation deserialize(StreamDeserializationContext ctxPayload) {
        int height = ctxPayload.readVaruint();
        return new EthereumBlockHeaderAttestation(height);
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeVaruint(height);
    }

    public String toString() {
        return "EthereumBlockHeaderAttestation(" + height + ")";
    }

    @Override
    public int compareTo(TimeAttestation other) {
        EthereumBlockHeaderAttestation that = (EthereumBlockHeaderAttestation) other;

        return height - that.height;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EthereumBlockHeaderAttestation)) {
            return false;
        }

        EthereumBlockHeaderAttestation otherAttestation = (EthereumBlockHeaderAttestation) other;

        if (!Arrays.equals(_TAG(), otherAttestation._TAG())) {
            return false;
        }

        return height == otherAttestation.height;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_TAG()) ^ height;
    }
}
