package com.eternitywall.ots.op;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.bitcoinj.core.Utils;
import org.junit.Test;


public class TestOp {

  @Test
  public void testAppend() {
    // Append operation
    assertTrue(Arrays.equals(
        new OpAppend(Utils.toBytes("suffix", "UTF-8")).call(Utils.toBytes("msg", "UTF-8")),
        Utils.toBytes("msgsuffix", "UTF-8")));
  }


  @Test
  public void testPrepend() {
    // Prepend operation
    assertTrue(Arrays.equals(
        new OpPrepend(Utils.toBytes("prefix", "UTF-8")).call(Utils.toBytes("msg", "UTF-8")),
        Utils.toBytes("prefixmsg", "UTF-8")));
  }

  @Test
  public void testSha256() {
    // SHA256 operation
    OpSHA256 opSHA256 = new OpSHA256();
    byte[] msg = {};
    assertTrue(Arrays.equals(opSHA256.call(msg), DatatypeConverter
        .parseHexBinary("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")));
  }

  @Test
  public void testRIPEMD160() {
    // RIPEMD160 operation
    OpRIPEMD160 opRIPEMD160 = new OpRIPEMD160();
    byte[] msg = {};
    assertTrue(Arrays.equals(opRIPEMD160.call(msg), DatatypeConverter
        .parseHexBinary("9c1185a5c5e9fc54612808977ee8f548b2258d31")));
  }

  @Test
  public void testEquality() {
    // Operation equality

    //self.assertEqual(OpReverse(), OpReverse())
    //self.assertNotEqual(OpReverse(), OpSHA1())

    assertEquals ( new OpAppend(Utils.toBytes("foo", "UTF-8")), new OpAppend(Utils.toBytes("foo", "UTF-8")));
    assertNotSame ( new OpAppend(Utils.toBytes("foo", "UTF-8")), new OpAppend(Utils.toBytes("bar", "UTF-8")));
    assertNotSame ( new OpAppend(Utils.toBytes("foo", "UTF-8")), new OpPrepend(Utils.toBytes("foo", "UTF-8")));
  }


  @Test
  public void testOrdering() {
    // Operation ordering
    assertTrue((new OpSHA1()).compareTo(new OpRIPEMD160()) < 0);

    OpSHA1 op1 = new OpSHA1();
    OpSHA1 op2 = new OpSHA1();
    assertFalse(op1.compareTo(op2) < 0);
    assertFalse(op1.compareTo(op2) > 0);

    OpAppend op3 = new OpAppend(Utils.toBytes("00", "UTF-8"));
    OpAppend op4 = new OpAppend(Utils.toBytes("01", "UTF-8"));
    assertTrue(op3.compareTo(op4) < 0);
    assertFalse(op3.compareTo(op4) > 0);
  }

}
