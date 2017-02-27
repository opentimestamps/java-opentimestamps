package eternitywall.com;

import com.eternitywall.Utils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

/**
 * Created by casatta on 27/02/17.
 */
public class TestUtils {

    @Test
    public void testArraysConcat() {
        byte[] array = "foo".getBytes();
        byte[] array2 = "bar".getBytes();
        byte[] array3 = Utils.arraysConcat(array,array2);
        String str = new String(array3, StandardCharsets.UTF_8);
        assertTrue("foobar".equals(str));

    }

    @Test
    public void testBytesToHex() {
        assertTrue(true);

    }
}
