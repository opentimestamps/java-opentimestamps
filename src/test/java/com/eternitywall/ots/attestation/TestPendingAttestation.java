package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import static com.eternitywall.ots.Utils.hexToBytes;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.assertArrayEquals;

public class TestPendingAttestation {

    @Test
    public void testSerializationOfPendingAttestations() throws IOException {
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

    @Test
    public void testInvalidUriDeserialization() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("fo%bar", "UTF-8"));
        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        TimeAttestation.deserialize(ctx);
        // TODO exception DeserializationError

        // Too long

        // Exactly 1000 bytes is ok
        baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "ea07" + "e807"));
        byte[] buffer = new byte[1000];
        Arrays.fill(buffer, (byte) 'x');
        baos.write(buffer);
        ctx = new StreamDeserializationContext(baos.toByteArray());
        TimeAttestation.deserialize(ctx);

        // But 1001 isn't
        baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "eb07" + "e907"));
        buffer = new byte[1001];
        Arrays.fill(buffer, (byte) 'x');
        baos.write(buffer);
        ctx = new StreamDeserializationContext(baos.toByteArray());
        TimeAttestation.deserialize(ctx);
        // TODO exception DeserializationError
    }

    @Test
    public void testDeserializationTrailingGarbage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "08" + "06"));
        baos.write(toBytes("foobarx", "UTF-8"));
        byte[] expectedSerialized = baos.toByteArray();

        StreamDeserializationContext ctx = new StreamDeserializationContext(expectedSerialized);
        TimeAttestation.deserialize(ctx);
        // TODO exception TrailingGarbageError
    }
}
