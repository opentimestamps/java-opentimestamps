package com.eternitywall.ots.op;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.*;

public class TestOp {

    @Test
    public void testAppendOperation() {
        assertArrayEquals(new OpAppend(toBytes("suffix", "UTF-8")).call(toBytes("msg", "UTF-8")),
                          toBytes("msgsuffix", "UTF-8"));
    }

    @Test
    public void testPrependOperation() {
        assertArrayEquals(new OpPrepend(toBytes("prefix", "UTF-8")).call(toBytes("msg", "UTF-8")),
                          toBytes("prefixmsg", "UTF-8"));
    }

    @Test
    public void testSha256Operation() {
        OpSHA256 opSHA256 = new OpSHA256();
        byte[] msg = {};
        assertArrayEquals(opSHA256.call(msg),
                          DatatypeConverter.parseHexBinary("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
    }

    @Test
    public void testRIPEMD160Operation() {
        OpRIPEMD160 opRIPEMD160 = new OpRIPEMD160();
        byte[] msg = {};
        assertArrayEquals(opRIPEMD160.call(msg),
                          DatatypeConverter.parseHexBinary("9c1185a5c5e9fc54612808977ee8f548b2258d31"));
    }

    @Test
    public void testOperationEquality() {
        assertEquals(new OpAppend(toBytes("foo", "UTF-8")), new OpAppend(toBytes("foo", "UTF-8")));
        assertNotEquals(new OpAppend(toBytes("foo", "UTF-8")), new OpAppend(toBytes("bar", "UTF-8")));
        assertNotEquals(new OpAppend(toBytes("foo", "UTF-8")), new OpPrepend(toBytes("foo", "UTF-8")));
    }

    @Test
    public void testOperationOrdering() {
        assertTrue((new OpSHA1()).compareTo(new OpRIPEMD160()) < 0);

        OpSHA1 op1 = new OpSHA1();
        OpSHA1 op2 = new OpSHA1();
        assertFalse(op1.compareTo(op2) < 0);
        assertFalse(op1.compareTo(op2) > 0);

        OpAppend op3 = new OpAppend(toBytes("00", "UTF-8"));
        OpAppend op4 = new OpAppend(toBytes("01", "UTF-8"));
        assertTrue(op3.compareTo(op4) < 0);
        assertFalse(op3.compareTo(op4) > 0);
    }
}
