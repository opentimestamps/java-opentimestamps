package com.eternitywall;

import com.eternitywall.ots.Merkle;
import com.eternitywall.ots.Timestamp;
import com.eternitywall.ots.attestation.PendingAttestation;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.xml.bind.DatatypeConverter;
import org.bitcoinj.core.Utils;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTimestamp {


  @Test
  public void merge() throws Exception {
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

    defTimestamp(2, DatatypeConverter.parseHexBinary("b413f47d13ee2fe6c845b2ee141af81de858df4ec549a58b7970bb96645bc8d2"));
    defTimestamp(3, DatatypeConverter.parseHexBinary("e6aa639123d8aac95d13d365ec3779dade4b49c083a8fed97d7bfc0d89bb6a5e"));
    defTimestamp(4, DatatypeConverter.parseHexBinary("7699a4fdd6b8b6908a344f73b8f05c8e1400f7253f544602c442ff5c65504b24"));
    defTimestamp(5, DatatypeConverter.parseHexBinary("aaa9609d0c949fee22c1c941a4432f32dc1c2de939e4af25207f0dc62df0dbd8"));
    defTimestamp(6, DatatypeConverter.parseHexBinary("ebdb4245f648b7e77b60f4f8a99a6d0529d1d372f98f35478b3284f16da93c06"));
    defTimestamp(7, DatatypeConverter.parseHexBinary("ba4603a311279dea32e8958bfb660c86237157bf79e6bfee857803e811d91b8f"));
  }

  @Test
  public void CatSha256(){
    Timestamp left = new Timestamp(Utils.toBytes("foo", "UTF-8") );
    Timestamp right = new Timestamp(Utils.toBytes("bar", "UTF-8"));
    Timestamp stampLeftRight = Merkle.catSha256(left, right);
    assertTrue(Arrays.equals(stampLeftRight.getDigest(),DatatypeConverter.parseHexBinary("c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2")));

    Timestamp righter = new Timestamp(Utils.toBytes("baz", "UTF-8"));
    Timestamp stampRighter = Merkle.catSha256(stampLeftRight, righter);
    assertTrue(Arrays.equals(stampRighter.getDigest(),DatatypeConverter.parseHexBinary("23388b16c66f1fa37ef14af8eb081712d570813e2afb8c8ae86efa726f3b7276")));
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