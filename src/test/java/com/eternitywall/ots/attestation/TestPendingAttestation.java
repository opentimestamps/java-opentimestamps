package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import static com.eternitywall.ots.Utils.hexToBytes;

import com.eternitywall.ots.exceptions.DeserializationException;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.assertArrayEquals;

public class TestPendingAttestation {

    @Test
    public void testSerializationOfPendingAttestations() throws IOException, DeserializationException {
        PendingAttestation pendingAttestation = new PendingAttestation(toBytes("foobar", "UTF-8"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobar", "UTF-8"));
        byte[] expectedSerialized = baos.toByteArray();

        StreamSerializationContext ctx = new StreamSerializationContext();
        pendingAttestation.serialize(ctx);
        assertArrayEquals(expectedSerialized, ctx.getOutput());

        StreamDeserializationContext ctx1 = new StreamDeserializationContext(expectedSerialized);
        PendingAttestation pendingAttestation2 = (PendingAttestation) TimeAttestation.deserialize(ctx1);
        assertArrayEquals(pendingAttestation2._TAG(), PendingAttestation._TAG);
        assertArrayEquals(pendingAttestation2.getUri(), toBytes("foobar", "UTF-8"));
    }

    @Test
    public void testDeserializationOfAttestations() throws IOException {
        PendingAttestation pendingAttestation = new PendingAttestation(toBytes("foobar", "UTF-8"));
        StreamSerializationContext ctx = new StreamSerializationContext();
        pendingAttestation.serialize(ctx);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DatatypeConverter.parseHexBinary("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobar", "UTF-8"));
        byte[] expectedSerialized = baos.toByteArray();
        assertArrayEquals(expectedSerialized, ctx.getOutput());
    }

    @Test(expected = DeserializationException.class)
    public void testInvalidUriDeserialization1() throws IOException, DeserializationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("fo%bar", "UTF-8"));
        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        TimeAttestation.deserialize(ctx);
    }


    @Test
    public void testInvalidUriDeserialization2() throws IOException, DeserializationException {
        //This one is correct size and should throw no exception
        // Exactly 1000 bytes is ok
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "ea07" + "e807"));
        byte[] buffer = new byte[1000];
        Arrays.fill(buffer, (byte) 'x');
        baos.write(buffer);
        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        TimeAttestation.deserialize(ctx);
    }

    @Test(expected = DeserializationException.class)
    public void testInvalidUriDeserialization3() throws IOException, DeserializationException {
        // Exactly 1000 bytes was ok (in the previous test).
        // But 1001 isn't, so this test should raise an exception
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "eb07" + "e907"));
        byte[] buffer = new byte[1001];
        Arrays.fill(buffer, (byte) 'x');
        baos.write(buffer);
        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        TimeAttestation.deserialize(ctx);
    }


    @Test
    public void testDeserializationTrailingGarbage() throws IOException, DeserializationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "08" + "06"));
        baos.write(toBytes("foobarx", "UTF-8"));
        byte[] expectedSerialized = baos.toByteArray();

        StreamDeserializationContext ctx = new StreamDeserializationContext(expectedSerialized);
        TimeAttestation.deserialize(ctx);
        // TODO exception TrailingGarbageError
    }
}
