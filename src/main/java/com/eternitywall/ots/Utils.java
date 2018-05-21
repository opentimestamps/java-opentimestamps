package com.eternitywall.ots;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.bitcoinj.core.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by luca on 26/02/2017.
 */
public class Utils {

    public static void arrayFill(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++)
        {
            array[i] = value;
        }
    }

    public static <T> T coalesce(T ...items) {
        for(T i : items) if(i != null) return i;
        return null;
    }

    public static byte[] arraysCopy(byte[] data)
    {
        if (data == null) {
            return null;
        }
        byte[] copy = new byte[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);

        return copy;
    }

    public static byte[] arraysConcat(byte[] array1, byte[] array2) {
        byte[] array1and2 = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
        return array1and2;
    }

    public static byte[] randBytes(int length) throws NoSuchAlgorithmException {
        //Java 6 & 7:
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);

        //Java 8 (even more secure):
        /*byte[] bytes = new byte[length];
        SecureRandom.getInstanceStrong().nextBytes(bytes);*/

        return bytes;
    }

    public static byte[] arrayReverse(byte[] array) {
        byte[] reversedArray = new byte[array.length];
        for (int i = array.length - 1, j = 0; i >= 0; i--, j++) {
            reversedArray[j] = array[i];
        }
        return reversedArray;
    }

    public static int compare(byte[] left, byte[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String s) throws IllegalArgumentException{
        int len = s.length();
        if(len%2==1){
            throw new IllegalArgumentException();
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            if ((Character.digit(s.charAt(i), 16) == -1) || (Character.digit(s.charAt(i+1), 16) == -1)) {
                throw new IllegalArgumentException();
            }
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] toBytes(String str, String encode) {
        return org.bitcoinj.core.Utils.toBytes(str, encode);
    }

    public static String toUpperFirstLetter(String string){
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public static Logger getLogger(String name) {
        Logger log = Logger.getLogger( name );
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord lr) {
                return lr.getMessage() + "\r\n";
            }
        });
        log.setUseParentHandlers(false);
        log.addHandler(handler);
        return log;
    }
}
