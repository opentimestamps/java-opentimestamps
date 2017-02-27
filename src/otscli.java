/**
 * Created by luca on 25/02/2017.
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class otscli {

    public static void main(String[] args) {


        Path pathPlain = Paths.get("./examples/hello-world.txt");
        Path pathOts = Paths.get("./examples/hello-world.txt.ots");

        /* INFO
        Path path = Paths.get("./examples/hello-world.txt.ots");
        try {
            byte[] data = Files.readAllBytes(path);
            String res = OpenTimestamps.info(data);
            System.out.print(res);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /* STAMP
        Path path = Paths.get("./examples/hello-world.txt");
        try {
            byte[] data = Files.readAllBytes(path);
            byte[] ots = OpenTimestamps.stamp(data,true);
            System.out.print(Utils.bytesToHex(ots));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /* VERIFY */
        try {
            byte[] bytesPlain = Files.readAllBytes(pathPlain);
            byte[] bytesOts = Files.readAllBytes(pathOts);
            String result = OpenTimestamps.verify(bytesOts,bytesPlain,false);
            System.out.print(result);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
