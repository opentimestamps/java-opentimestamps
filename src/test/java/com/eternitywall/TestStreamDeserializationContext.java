package com.eternitywall;

import com.eternitywall.attestation.PendingAttestation;
import com.eternitywall.attestation.TimeAttestation;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by casatta on 24/03/17.
 */
public class TestStreamDeserializationContext {
    @Test
    public void testReadvaruint() {
        final byte[] uri = "https://ots.eternitywall.it".getBytes(StandardCharsets.US_ASCII);
        PendingAttestation pendingAttestation=new PendingAttestation(uri);

        StreamSerializationContext streamSerializationContext = new StreamSerializationContext();
        pendingAttestation.serialize(streamSerializationContext);
        System.out.println( DatatypeConverter.printHexBinary(streamSerializationContext.getOutput() ).toLowerCase() );

        StreamDeserializationContext streamDeserializationContext =new StreamDeserializationContext(streamSerializationContext.getOutput());
        PendingAttestation pendingAttestationCheck = (PendingAttestation) TimeAttestation.deserialize(streamDeserializationContext);

        assertTrue(Arrays.equals(uri, pendingAttestationCheck.getUri()));
    }
}