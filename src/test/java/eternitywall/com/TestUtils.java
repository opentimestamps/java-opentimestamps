package eternitywall.com;

import com.eternitywall.Utils;
import org.junit.Test;

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
        assertTrue("foobar".equals(array3.toString()));

    }

    @Test
    public void testBytesToHex() {
        assertTrue(true);

    }
}
