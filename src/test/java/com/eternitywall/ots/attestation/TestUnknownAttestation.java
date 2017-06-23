package com.eternitywall.ots.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.junit.Test;
import org.slf4j.helpers.Util;


public class TestUnknownAttestation {

  @Test
  public void string() {
    UnknownAttestation a = new UnknownAttestation(
        Utils.hexToBytes("0102030405060708"),
        Utils.toBytes("Hello World!", "UTF-8"));

    String string = "UnknownAttestation " + Utils.bytesToHex(a._TAG()) + ' ' + Utils.bytesToHex(a.payload);
    assertEquals(a.toString(), string);
  }

  @Test
  public void serialization() throws IOException {
    // Serialization/deserialization of unknown attestations
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("0102030405060708"));
    baos.write(0x0c);
    baos.write(Utils.toBytes("Hello World!", "UTF-8"));
    byte[] expected_serialized = baos.toByteArray();

    StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
    UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
    assertTrue(Arrays.equals(a._TAG(), Utils.hexToBytes("0102030405060708")));
    assertTrue(Arrays.equals(a.payload, Utils.toBytes("Hello World!", "UTF-8")));

    StreamSerializationContext ctx1 = new StreamSerializationContext();
    a.serialize(ctx1);
    assertTrue(Arrays.equals(expected_serialized, ctx1.getOutput()));
  }


  @Test
  public void deserializationTooLong() throws IOException {
    // Deserialization of attestations with oversized payloads
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("0102030405060708"));
    baos.write(0x81);
    baos.write(0x40);
    baos.write('x' * 8193);
    StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
    UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
    // TODO: exception

    // pending attestation
    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e"));
    baos.write(0x81);
    baos.write(0x40);
    baos.write('x' * 8193);
    StreamDeserializationContext ctx1 = new StreamDeserializationContext(baos.toByteArray());
    UnknownAttestation a1 = (UnknownAttestation) TimeAttestation.deserialize(ctx1);
    // TODO: exception
  }


}