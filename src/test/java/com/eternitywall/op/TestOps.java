package com.eternitywall.op;

import com.eternitywall.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by casatta on 06/03/17.
 */
public class TestOps {

    @Test
    public void testKeccak() {
        byte[] result = new OpKECCAK256().call(Utils.hexToBytes(""));
        assertEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", Utils.bytesToHex(result));

        byte[] result2 = new OpKECCAK256().call(Utils.hexToBytes("80"));
        assertEquals("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421", Utils.bytesToHex(result2));
    }

    @Test
    public void testSha1() {
        byte[] result = new OpSHA1().call(Utils.hexToBytes("0a"));
        assertEquals("adc83b19e793491b1c6ea0fd8b46cd9f32e592fc", Utils.bytesToHex(result));
    }

    @Test
    public void testSha256() {
        byte[] result = new OpSHA256().call(Utils.hexToBytes(""));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Utils.bytesToHex(result));
    }

    @Test
    public void testRipemd() {
        byte[] result = new OpRIPEMD160().call(Utils.hexToBytes(""));
        assertEquals("9c1185a5c5e9fc54612808977ee8f548b2258d31", Utils.bytesToHex(result));
    }

    @Test
    public void testAppend() {
        byte[] foos = Utils.hexToBytes("00");
        byte[] bars = Utils.hexToBytes("11");
        byte[] call = new OpAppend(foos).call(bars);
        assertEquals("1100", Utils.bytesToHex(call));
    }

    @Test
    public void testPrepend() {
        byte[] foos = Utils.hexToBytes("00");
        byte[] bars = Utils.hexToBytes("11");
        byte[] call = new OpPrepend(foos).call(bars);
        assertEquals("0011", Utils.bytesToHex(call));
    }

}
