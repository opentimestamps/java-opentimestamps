package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.Utils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestBitcoinBlockHeaderAttestation {

    @Test
    public void deserializationTrailingGarbage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(Utils.hexToBytes("0588960d73d71901" +
                "02" + // two bytes of payload
                "00" + // genesis block!
                "ff")); // one byte of trailing garbage

        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        TimeAttestation.deserialize(ctx);
        // TODO exception TrailingGarbageError
    }
}
