package com.eternitywall.ots;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TestUtils {
    @Test
    public void testArraysCopy() throws Exception {
        byte[] original = Utils.hexToBytes("001100");
        byte[] copy = Utils.arraysCopy(original);
        assertTrue(Utils.compare(original, copy) == 0);
        assertFalse(original == copy);
        original[0] = (byte) 13;
        assertTrue(Utils.compare(original, copy) > 0);
        assertNull(Utils.arraysCopy(null));
    }

    @Test
    public void testCoalesce() {
        assertEquals("abc", Utils.coalesce("abc"));
        assertEquals("abc", Utils.coalesce("abc", "def"));

        String aNull = null;
        assertNull(Utils.coalesce(aNull));
        assertNull(Utils.coalesce(aNull, aNull, aNull));
        assertEquals("abc", Utils.coalesce(aNull, aNull, aNull, "abc"));
    }

    @Test
    public void testArraysConcat() {
        byte[] array = "foo".getBytes();
        byte[] array2 = "bar".getBytes();
        byte[] array3 = Utils.arraysConcat(array, array2);
        String str = new String(array3, StandardCharsets.UTF_8);
        assertEquals("foobar", str);
    }

    @Test
    public void testBytesToHex() {
        byte[] array = " 0aZ".getBytes();
        String anObject = Utils.bytesToHex(array).toLowerCase();
        assertEquals("2030615a", anObject);
    }

    @Test
    public void testRandBytes() {
        try {
            byte[] array1 = Utils.randBytes(8);
            byte[] array2 = Utils.randBytes(8);
            assertEquals(8, array1.length);
            assertEquals(8, array2.length);
            assertFalse(Arrays.equals(array1, array2));
        } catch (NoSuchAlgorithmException e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testArrayReverse() {
        byte[] array1 = "0125121512512".getBytes();
        byte[] array2 = Utils.arrayReverse(array1);
        byte[] array3 = Utils.arrayReverse(array2);
        assertFalse(Arrays.equals(array1, array2));
        assertArrayEquals(array1, array3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexBytesOf_0_ShouldThrow() {
        Utils.hexToBytes("0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexBytesOf_xx_ShouldThrow() {
        Utils.hexToBytes("xx");
    }

    @Test
    public void testValidHexBytes() {
        String zeroes = "0000";
        byte[] arr1 = Utils.hexToBytes(zeroes);
        assertEquals(zeroes, Utils.bytesToHex(arr1).toLowerCase());

        String zeroaef = "0aef";
        byte[] arr2 = Utils.hexToBytes(zeroaef);
        assertEquals(zeroaef, Utils.bytesToHex(arr2).toLowerCase());
    }

    @Test
    public void testUppercase() {
        assertEquals("Hello", Utils.toUpperFirstLetter(("hello")));
        assertEquals("Hello", Utils.toUpperFirstLetter(("Hello")));
    }

    @Test
    public void testArrayFill() {
        byte[] a = new byte[10];
        Utils.arrayFill(a, (byte) 0);
        assertEquals("00000000000000000000", Utils.bytesToHex(a).toLowerCase());
    }
}
