import com.sun.tools.internal.ws.wsdl.document.jaxws.Exception;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by luca on 26/02/2017.
 */
public class Utils {


    public static byte[] ArraysConcat(byte[] array1, byte[] array2){
        byte[] array1and2 = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
        return array1and2;
    }

    public static String bytesToHex(byte[]bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    public static byte[] randBytes(int length) throws IOException {
        //Java 6 & 7:
        //SecureRandom random = new SecureRandom();
        //byte[] bytes = new byte[20];
        //random.nextBytes(bytes);

        //Java 8 (even more secure):
        byte[] bytes = new byte[length];
        try {
            SecureRandom.getInstanceStrong().nextBytes(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IOException();
        }
        return bytes;
    }

    public static byte[] arrayReverse(byte[] array){
        byte[] tmp = array.clone();
        Collections.reverse(Arrays.asList(tmp));
        return tmp;
    }

    public static byte[] hexToBytes(String hexString){
        byte[] yourBytes = new BigInteger(hexString, 16).toByteArray();
        return yourBytes;
    }
}
