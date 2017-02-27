package com.eternitywall; /**
 * Created by luca on 25/02/2017.
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class OtsCli {

    public static void main(String[] args) {


        Path pathPlain = Paths.get("./examples/hello-world.txt");
        Path pathOts = Paths.get("./examples/hello-world.txt.ots");

         //INFO
        /*try {
            byte[] data = Files.readAllBytes(pathOts);
            String res = com.eternitywall.OpenTimestamps.info(data);
            System.out.print(res);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // STAMP
        /*try {
            byte[] data = Files.readAllBytes(pathPlain);
            byte[] ots = com.eternitywall.OpenTimestamps.stamp(data,true);
            System.out.print(com.eternitywall.Utils.bytesToHex(ots));
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
