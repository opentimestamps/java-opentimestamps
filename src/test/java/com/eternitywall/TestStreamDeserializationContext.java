package com.eternitywall;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Timestamp;
import com.eternitywall.ots.Utils;
import com.eternitywall.ots.attestation.PendingAttestation;
import com.eternitywall.ots.attestation.TimeAttestation;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestStreamDeserializationContext {

    @Test
    public void testVaruint() {
        int value = 0x1;

        for (int i = 0; i < 20; i++) {
            value = value << 1;
            StreamSerializationContext ssc = new StreamSerializationContext();
            ssc.writeVaruint(value);

            StreamDeserializationContext sdc = new StreamDeserializationContext(ssc.getOutput());
            int read = sdc.readVaruint();
            assertEquals(value, read);
        }
    }

    @Test
    public void testReadvaruint() {
        final byte[] uri = "https://finney.calendar.eternitywall.com".getBytes(StandardCharsets.US_ASCII);
        PendingAttestation pendingAttestation = new PendingAttestation(uri);

        StreamSerializationContext streamSerializationContext = new StreamSerializationContext();
        pendingAttestation.serialize(streamSerializationContext);

        StreamDeserializationContext streamDeserializationContext = new StreamDeserializationContext(streamSerializationContext.getOutput());
        PendingAttestation pendingAttestationCheck = (PendingAttestation) TimeAttestation.deserialize(streamDeserializationContext);

        assertArrayEquals(uri, pendingAttestationCheck.getUri());
    }

    @Test
    public void testTimestamp() {
        byte[] ots = Utils.hexToBytes("F0105C3F2B3F8524A32854E07AD8ADDE9C1908F10458D95A36F008088D287213A8B9880083DFE30D2EF90C8E2C2B68747470733A2F2F626F622E6274632E63616C656E6461722E6F70656E74696D657374616D70732E6F7267");
        byte[] digest = Utils.hexToBytes("7aa9273d2a50dbe0cc5a6ccc444a5ca90c9491dd2ac91849e45195ae46f64fe352c3a63ba02775642c96131df39b5b85");

        StreamDeserializationContext streamDeserializationContext = new StreamDeserializationContext(ots);
        Timestamp timestamp = Timestamp.deserialize(streamDeserializationContext, digest);

        StreamSerializationContext streamSerializationContext = new StreamSerializationContext();
        timestamp.serialize(streamSerializationContext);
        byte[] otsSerialized = streamSerializationContext.getOutput();

        assertArrayEquals(ots, otsSerialized);
    }
}
