package com.eternitywall;

import com.eternitywall.attestation.PendingAttestation;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Created by casatta on 24/03/17.
 */
public class TestStreamDeserializationContext {
    @Test
    public void testReadvaruint() {
        final byte[] uri = "https://ots.eternitywall.it".getBytes(StandardCharsets.US_ASCII);
        final byte[] varint = {(byte) 0x1c, (byte) 0x1b };
        StreamDeserializationContext streamDeserializationContext =new StreamDeserializationContext(varint);
        //assertEquals( uri.length, streamDeserializationContext.readVaruint() );

        PendingAttestation pendingAttestation=new PendingAttestation(uri);

        StreamSerializationContext streamSerializationContext = new StreamSerializationContext();
        pendingAttestation.serialize(streamSerializationContext);
        System.out.println( DatatypeConverter.printHexBinary(streamSerializationContext.getOutput() ).toLowerCase() );

    }
}