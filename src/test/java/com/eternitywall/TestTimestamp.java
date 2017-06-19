package com.eternitywall;

import com.eternitywall.ots.Merkle;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Timestamp;
import com.eternitywall.ots.attestation.PendingAttestation;
import com.eternitywall.ots.attestation.TimeAttestation;
import com.eternitywall.ots.op.Op;
import com.eternitywall.ots.op.OpAppend;
import com.eternitywall.ots.op.OpPrepend;
import com.eternitywall.ots.op.OpSHA256;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import static org.junit.Assert.*;
import com.eternitywall.ots.Utils;

public class TestTimestamp {


  @Test
  public void addOp() {
    //Adding operations to timestamps
    Timestamp t = new Timestamp(Utils.toBytes("abcd", "UTF-8"));

    OpAppend opAppend = new OpAppend(Utils.toBytes("efgh", "UTF-8"));
    t.add(opAppend);

    // The second add should succeed with the timestamp unchanged
    t.add(opAppend);
    Timestamp tComplete = new Timestamp(Utils.toBytes("abcdefgh", "UTF-8"));
    assertTrue( t.ops.get(opAppend).equals( tComplete) );
  }

  @Test
  public void setResultTimestamp() {
    //Setting an op result timestamp
    Timestamp t1 = new Timestamp(Utils.toBytes("foo", "UTF-8"));
    OpAppend opAppend1 = new OpAppend(Utils.toBytes("bar", "UTF-8"));
    OpAppend opAppend2 = new OpAppend(Utils.toBytes("baz", "UTF-8"));
    Timestamp t2 = t1.add(opAppend1);
    Timestamp t3 = t2.add(opAppend2);
    assertTrue( Arrays.equals( t1.ops.get(opAppend1).ops.get(opAppend2).msg, Utils.toBytes("foobarbaz", "UTF-8")) );

    t1.ops.put(opAppend1, new Timestamp(Utils.toBytes("foobar", "UTF-8")) );
    for (Map.Entry<Op, Timestamp> entry : t1.ops.get(opAppend1).ops.entrySet()) {
      Timestamp timestamp = entry.getValue();
      Op op = entry.getKey();
      assertFalse(op.equals(opAppend2));
    }
  }


  void Tserialize(Timestamp expected_instance, byte[] expected_serialized){
    StreamSerializationContext ssc = new StreamSerializationContext();
    expected_instance.serialize(ssc);
    byte[] actual_serialized = ssc.getOutput();

    assertTrue(Arrays.equals(expected_serialized,actual_serialized));

    StreamDeserializationContext sdc = new StreamDeserializationContext(expected_serialized);
    Timestamp actual_instance = Timestamp.deserialize(sdc,expected_instance.msg);
    assertTrue(expected_instance.equals(actual_instance));
  }


  @Test
  public void serialization() throws IOException {
    // Timestamp serialization/deserialization

    Timestamp stamp = new Timestamp(Utils.toBytes("foo", "UTF-8"));
    stamp.attestations.add(new PendingAttestation(Utils.toBytes("foobar", "UTF-8")));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("foobar", "UTF-8"));
    Tserialize(stamp, baos.toByteArray());

    stamp.attestations.add(new PendingAttestation(Utils.toBytes("barfoo", "UTF-8")));

    baos = new ByteArrayOutputStream();
    baos.write(0xff);
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("barfoo", "UTF-8"));
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("foobar", "UTF-8"));
    Tserialize(stamp, baos.toByteArray());

    stamp.attestations.add(new PendingAttestation(Utils.toBytes("foobaz", "UTF-8")));

    baos = new ByteArrayOutputStream();
    baos.write(0xff);
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("barfoo", "UTF-8"));
    baos.write(0xff);
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("foobar", "UTF-8"));
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("foobaz", "UTF-8"));
    Tserialize(stamp, baos.toByteArray());

    //Timestamp sha256Stamp = stamp.ops.put(new OpSHA256(), null);
    // Should fail - empty timestamps can't be serialized
    //StreamSerializationContext ssc = new StreamSerializationContext();
    //stamp.serialize(ssc);
    OpSHA256 opSHA256 = new OpSHA256();
    Timestamp sha256Stamp = stamp.add(opSHA256);
    // TODO: check serialization

    PendingAttestation pendingAttestation = new PendingAttestation(Utils.toBytes("deeper", "UTF-8"));
    sha256Stamp.attestations.add( pendingAttestation );

    baos = new ByteArrayOutputStream();
    baos.write(0xff);
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("barfoo", "UTF-8"));
    baos.write(0xff);
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("foobar", "UTF-8"));
    baos.write(0xff);
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("foobaz", "UTF-8"));
    baos.write(0x08);
    baos.write(0x00);
    baos.write(Utils.hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
    baos.write(Utils.toBytes("deeper", "UTF-8"));
    Tserialize(stamp, baos.toByteArray());

  }

  @Test
  public void merge() {
    //Merging timestamps

    Timestamp stampA = new Timestamp(Utils.toBytes("a", "UTF-8"));
    Timestamp stampB = new Timestamp(Utils.toBytes("b", "UTF-8"));
    Exception err = null;
    try {
      stampA.merge(stampB);
    } catch (Exception e) {
      err = e;
    }
    assertNotNull(err);

    Timestamp stamp1 = new Timestamp(Utils.toBytes("a", "UTF-8"));
    Timestamp stamp2 = new Timestamp(Utils.toBytes("a", "UTF-8"));
    stamp2.attestations.add(new PendingAttestation(Utils.toBytes("foobar", "UTF-8")));
    err = null;
    try {
      stamp1.merge(stamp2);
      assertTrue(stamp1.equals(stamp2));
    } catch (Exception e) {
      e.printStackTrace();
      err = e;
    }
    assertNull(err);
  }

  @Test
  public void makeMerkleTree()
      throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {

    defTimestamp(2, Utils.hexToBytes("b413f47d13ee2fe6c845b2ee141af81de858df4ec549a58b7970bb96645bc8d2"));
    defTimestamp(3, Utils.hexToBytes("e6aa639123d8aac95d13d365ec3779dade4b49c083a8fed97d7bfc0d89bb6a5e"));
    defTimestamp(4, Utils.hexToBytes("7699a4fdd6b8b6908a344f73b8f05c8e1400f7253f544602c442ff5c65504b24"));
    defTimestamp(5, Utils.hexToBytes("aaa9609d0c949fee22c1c941a4432f32dc1c2de939e4af25207f0dc62df0dbd8"));
    defTimestamp(6, Utils.hexToBytes("ebdb4245f648b7e77b60f4f8a99a6d0529d1d372f98f35478b3284f16da93c06"));
    defTimestamp(7, Utils.hexToBytes("ba4603a311279dea32e8958bfb660c86237157bf79e6bfee857803e811d91b8f"));
  }

  @Test
  public void CatSha256(){
    Timestamp left = new Timestamp(Utils.toBytes("foo", "UTF-8") );
    Timestamp right = new Timestamp(Utils.toBytes("bar", "UTF-8"));
    Timestamp stampLeftRight = Merkle.catSha256(left, right);
    assertTrue(Arrays.equals(stampLeftRight.getDigest(),Utils.hexToBytes("c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2")));

    Timestamp righter = new Timestamp(Utils.toBytes("baz", "UTF-8"));
    Timestamp stampRighter = Merkle.catSha256(stampLeftRight, righter);
    assertTrue(Arrays.equals(stampRighter.getDigest(),Utils.hexToBytes("23388b16c66f1fa37ef14af8eb081712d570813e2afb8c8ae86efa726f3b7276")));
  }

  public void defTimestamp(int n, byte[] expected_merkle_root){
    List<Timestamp> roots = new ArrayList<>();

    for (int i=0; i<n; i++){
      byte[] bytes = {(byte) i};
      roots.add( new Timestamp( bytes ));
    }
    Timestamp merkleTip = Merkle.makeMerkleTree(roots);
    assertTrue(Arrays.equals(merkleTip.getDigest(),expected_merkle_root));

    for (Timestamp root : roots){
      Set<byte[]> tips = root.allTips();
      for (byte[] tip : tips){
        assertTrue( Arrays.equals(tip, merkleTip.getDigest()) );
      }
    }
  }
}