package com.eternitywall.ots.op;

import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.Utils;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by casatta on 06/03/17.
 */
public class TestOps {

    @Test
    public void testKeccak() {
        byte[] result = new OpKECCAK256().call(DatatypeConverter.parseHexBinary(""));
        assertEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", DatatypeConverter.printHexBinary(result).toLowerCase());

        byte[] result2 = new OpKECCAK256().call(DatatypeConverter.parseHexBinary("80"));
        assertEquals("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421", DatatypeConverter.printHexBinary(result2).toLowerCase());
    }

    @Test
    public void testSha1() {
        byte[] result = new OpSHA1().call(DatatypeConverter.parseHexBinary("0a"));
        assertEquals("adc83b19e793491b1c6ea0fd8b46cd9f32e592fc", DatatypeConverter.printHexBinary(result).toLowerCase());
    }

    @Test
    public void testSha256() {
        byte[] result = new OpSHA256().call(DatatypeConverter.parseHexBinary(""));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", DatatypeConverter.printHexBinary(result).toLowerCase());
    }

    @Test
    public void testRipemd() {
        byte[] result = new OpRIPEMD160().call(DatatypeConverter.parseHexBinary(""));
        assertEquals("9c1185a5c5e9fc54612808977ee8f548b2258d31", DatatypeConverter.printHexBinary(result).toLowerCase());
    }

    @Test
    public void testAppend() {
        byte[] foos = DatatypeConverter.parseHexBinary("00");
        byte[] bars = DatatypeConverter.parseHexBinary("11");
        byte[] call = new OpAppend(foos).call(bars);
        assertEquals("1100", DatatypeConverter.printHexBinary(call).toLowerCase());
    }

    @Test
    public void testPrepend() {
        byte[] foos = DatatypeConverter.parseHexBinary("00");
        byte[] bars = DatatypeConverter.parseHexBinary("11");
        byte[] call = new OpPrepend(foos).call(bars);
        assertEquals("0011", DatatypeConverter.printHexBinary(call).toLowerCase());
    }

    @Test
    public void test1M() throws Exception{
        String hash = "30e14955ebf1352266dc2ff8067e68104607e750abb9d3b36582b8af909fcb58";
        int size = 1 * 1024 * 1024;
        byte[] buffer = new byte[size];
        StreamDeserializationContext ctx = new StreamDeserializationContext(buffer);
        DetachedTimestampFile timestampFile = DetachedTimestampFile.fromBytes(new OpSHA256(), ctx);
        byte[] fileDigest = timestampFile.fileDigest();
        assertTrue(Arrays.equals(DatatypeConverter.parseHexBinary(hash), fileDigest));
    }

    @Test
    public void test10M() throws Exception{
        String hash = "e5b844cc57f57094ea4585e235f36c78c1cd222262bb89d53c94dcb4d6b3e55d";
        int size = 10 * 1024 * 1024;
        byte[] buffer = new byte[size];
        StreamDeserializationContext ctx = new StreamDeserializationContext(buffer);
        DetachedTimestampFile timestampFile = DetachedTimestampFile.fromBytes(new OpSHA256(), ctx);
        byte[] fileDigest = timestampFile.fileDigest();
        assertTrue(Arrays.equals(DatatypeConverter.parseHexBinary(hash), fileDigest));
    }

    @Test
    public void test100M() throws Exception{
        String hash = "20492a4d0d84f8beb1767f6616229f85d44c2827b64bdbfb260ee12fa1109e0e";
        int size = 100 * 1024 * 1024;
        byte[] buffer = new byte[size];
        StreamDeserializationContext ctx = new StreamDeserializationContext(buffer);
        DetachedTimestampFile timestampFile = DetachedTimestampFile.fromBytes(new OpSHA256(), ctx);
        byte[] fileDigest = timestampFile.fileDigest();
        assertTrue(Arrays.equals(DatatypeConverter.parseHexBinary(hash), fileDigest));
    }

}
