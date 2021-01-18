package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import static com.eternitywall.ots.Utils.hexToBytes;
import static com.eternitywall.ots.Utils.bytesToHex;

import com.eternitywall.ots.exceptions.DeserializationException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestUnknownAttestation {

    @Test
    public void string() {
        UnknownAttestation a = new UnknownAttestation(hexToBytes("0102030405060708"),
                                                      toBytes("Hello World!", "UTF-8"));

        String string = "UnknownAttestation " + bytesToHex(a._TAG()) + ' ' + bytesToHex(a.payload);
        assertEquals(a.toString(), string);
    }

    @Test
    public void testSerializationDeserializationOfUnknownAttestations()
        throws IOException, DeserializationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("0102030405060708"));
        baos.write(0x0c);
        baos.write(toBytes("Hello World!", "UTF-8"));
        byte[] expectedSerialized = baos.toByteArray();

        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
        assertArrayEquals(a._TAG(), hexToBytes("0102030405060708"));
        assertArrayEquals(a.payload, toBytes("Hello World!", "UTF-8"));

        StreamSerializationContext ctx1 = new StreamSerializationContext();
        a.serialize(ctx1);
        assertArrayEquals(expectedSerialized, ctx1.getOutput());
    }

    @Test(expected = DeserializationException.class )
    public void deserializationTooLong1() throws IOException, DeserializationException {
        // Deserialization of attestations with oversized payloads
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        baos1.write(hexToBytes("0102030405060708"));
        baos1.write(0x81);
        baos1.write(0x40);
        for(int i=0;i<8193;i++)
            baos1.write('x');
        StreamDeserializationContext ctx = new StreamDeserializationContext(baos1.toByteArray());
        UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
    }

    @Test(expected = DeserializationException.class )
    public void deserializationTooLong2() throws IOException, DeserializationException {
        // Pending attestation
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        baos2.write(hexToBytes("83dfe30d2ef90c8e"));
        baos2.write(0x81);
        baos2.write(0x40);
        for(int i=0;i<8193;i++)
            baos2.write('x');
        StreamDeserializationContext ctx1 = new StreamDeserializationContext(baos2.toByteArray());
        PendingAttestation a1 = (PendingAttestation) TimeAttestation.deserialize(ctx1);
    }
}
