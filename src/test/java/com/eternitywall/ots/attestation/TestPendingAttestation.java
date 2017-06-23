package com.eternitywall.ots.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.junit.Test;


public class TestPendingAttestation {


  @Test
  public void serialization() throws IOException {
    // Serialization of pending attestations
    PendingAttestation pendingAttestation = new PendingAttestation(Utils.toBytes("foobar", "UTF-8"));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e"+"07"+"06"));
    baos.write(Utils.toBytes("foobar", "UTF-8"));
    byte[] expected_serialized = baos.toByteArray();

    StreamSerializationContext ctx = new StreamSerializationContext();
    pendingAttestation.serialize(ctx);
    assertTrue(Arrays.equals(expected_serialized, ctx.getOutput()));

    StreamDeserializationContext ctx1 = new StreamDeserializationContext(expected_serialized);
    PendingAttestation pendingAttestation2 = (PendingAttestation) TimeAttestation.deserialize(ctx1);

    assertTrue(Arrays.equals(pendingAttestation2._TAG(), PendingAttestation._TAG));
    assertTrue(Arrays.equals(pendingAttestation2.getUri(), Utils.toBytes("foobar", "UTF-8")));

  }


  @Test
  public void deserialization() throws IOException {
    // Deserialization of attestations

    PendingAttestation pendingAttestation = new PendingAttestation(Utils.toBytes("foobar", "UTF-8"));
    StreamSerializationContext ctx = new StreamSerializationContext();
    pendingAttestation.serialize(ctx);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(DatatypeConverter.parseHexBinary("83dfe30d2ef90c8e"+"07"+"06"));
    baos.write(Utils.toBytes("foobar", "UTF-8"));
    byte[] expected_serialized = baos.toByteArray();
    assertTrue(Arrays.equals(expected_serialized, ctx.getOutput()));
  }

  @Test
  public void invalidUriDeserialization() throws IOException {
    // illegal character

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e"+"07"+"06"));
    baos.write(Utils.toBytes("fo%bar", "UTF-8"));
    StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
    TimeAttestation.deserialize(ctx);
    // TODO exception DeserializationError

    // Too long

    // Exactly 1000 bytes is ok
    baos = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e"+"ea07"+"e807"));
    byte[] buffer = new byte[1000];
    Arrays.fill(buffer,(byte)'x');
    baos.write(buffer);
    ctx = new StreamDeserializationContext(baos.toByteArray());
    TimeAttestation.deserialize(ctx);

    // But 1001 isn't
    baos = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e"+"eb07"+"e907"));
    buffer = new byte[1001];
    Arrays.fill(buffer,(byte)'x');
    baos.write(buffer);
    ctx = new StreamDeserializationContext(baos.toByteArray());
    TimeAttestation.deserialize(ctx);
    // TODO exception DeserializationError

  }

  @Test
  public void deserializationTrailingGarbage() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e"+"08"+"06"));
    baos.write(Utils.toBytes("foobarx", "UTF-8"));
    byte[] expected_serialized = baos.toByteArray();

    StreamDeserializationContext ctx = new StreamDeserializationContext(expected_serialized);
    TimeAttestation.deserialize(ctx);
    // TODO exception TrailingGarbageError
  }

}