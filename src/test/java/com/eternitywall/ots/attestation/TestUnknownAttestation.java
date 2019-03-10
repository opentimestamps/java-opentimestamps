package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.*;

public class TestUnknownAttestation {

    @Test
    public void string() {
        UnknownAttestation a = new UnknownAttestation(
                Utils.hexToBytes("0102030405060708"),
                toBytes("Hello World!", "UTF-8"));

        String string = "UnknownAttestation " + Utils.bytesToHex(a._TAG()) + ' ' + Utils.bytesToHex(a.payload);
        assertEquals(a.toString(), string);
    }

    @Test
    public void serialization() throws IOException {
        // Serialization/deserialization of unknown attestations
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(Utils.hexToBytes("0102030405060708"));
        baos.write(0x0c);
        baos.write(toBytes("Hello World!", "UTF-8"));
        byte[] expectedSerialized = baos.toByteArray();

        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
        assertArrayEquals(a._TAG(), Utils.hexToBytes("0102030405060708"));
        assertArrayEquals(a.payload, toBytes("Hello World!", "UTF-8"));

        StreamSerializationContext ctx1 = new StreamSerializationContext();
        a.serialize(ctx1);
        assertArrayEquals(expectedSerialized, ctx1.getOutput());
    }

    @Test
    public void deserializationTooLong() throws IOException {
        // Deserialization of attestations with oversized payloads
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        baos1.write(Utils.hexToBytes("0102030405060708"));
        baos1.write(0x81);
        baos1.write(0x40);
        baos1.write('x' * 8193);
        StreamDeserializationContext ctx = new StreamDeserializationContext(baos1.toByteArray());
        UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
        // TODO: exception

        // pending attestation
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        baos2.write(Utils.hexToBytes("83dfe30d2ef90c8e"));
        baos2.write(0x81);
        baos2.write(0x40);
        baos2.write('x' * 8193);
        StreamDeserializationContext ctx1 = new StreamDeserializationContext(baos2.toByteArray());
        UnknownAttestation a1 = (UnknownAttestation) TimeAttestation.deserialize(ctx1);
        // TODO: exception
    }
}
