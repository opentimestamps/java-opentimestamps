package com.eternitywall.ots; /**
 * Created by luca on 25/02/2017.
 */

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class OtsCli {

    private static Logger log = Logger.getLogger(OtsCli.class.getName());
    private static String title = "OtsCli";
    private static String version = "1.0";
    private static List<String> calendarsUrl = new ArrayList<>();
    private static List<String> files = new ArrayList<>();
    private static String cmd = null;
    private static Integer m = 0;
    private static String signatureFile = "";

    public static void main(String[] args) {

        // Create the Options
        Options options = new Options();
        options.addOption( "c", "calendar", true, "Create timestamp with the aid of a remote calendar. May be specified multiple times." );
        options.addOption( "k", "key", true, "Signature key file of private remote calendars." );
        options.addOption( "m", "", true, "Commitments are sent to remote calendars in the event of timeout the timestamp is considered done if at least M calendars replied." );
        options.addOption( "V", "version", false, "print " + title + " version." );
        options.addOption( "h", "help", false, "print this help." );

        // Parse the args to retrieve options & command
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            // args are the arguments passed to the  the application via the main method

            if(line.hasOption("c")) {
                String[] cals = line.getOptionValues("c");
                calendarsUrl.addAll( Arrays.asList(cals) );
            }
            if(line.hasOption("m")) {
                m = Integer.valueOf(line.getOptionValue("m"));
            }
            if(line.hasOption("k")) {
                signatureFile = line.getOptionValue("k");
                calendarsUrl.clear();
            }
            if(line.hasOption("V")) {
                System.out.println("Version: " + title + " v." + version + '\n');
                return;
            }
            if(line.hasOption("h")) {
                showHelp();
                return;
            }

            if(line.getArgList().size()<=1){
                showHelp();
                return;
            }

            cmd = line.getArgList().get(0);
            files = line.getArgList().subList(1,line.getArgList().size());

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(title + ": invalid parameters ");
        }

        // Parse the command
        switch (cmd) {
            case "info":
            case "i":
                if (files.size() == 0) {
                    System.out.println("Show information on a timestamp given as argument.\n");
                    System.out.println(title + " info: bad options number ");
                    break;
                }
                info(files.get(0));
                break;
            case "stamp":
            case "s":
                if (files.size() == 0) {
                    System.out.println("Create timestamp with the aid of a remote calendar.\n");
                    System.out.println(title + ": bad options number ");
                    break;
                }
                stamp(files.get(0), calendarsUrl, m, signatureFile);
                break;
            case "verify":
            case "v":
                if (files.size() == 0) {
                    System.out.println("Verify the timestamp attestations given as argument.\n");
                    System.out.println(title + ": bad options number ");
                    break;
                }
                verify(files.get(0));
                break;
            case "upgrade":
            case "u":
                if (files.size() == 0) {
                    System.out.println("Upgrade remote calendar timestamps to be locally verifiable.\n");
                    System.out.println(title + ": bad options number ");
                    break;
                }
                upgrade(files.get(0));
                break;
            default:
                System.out.println(title + ": bad option: " + cmd);
        }

    }

    private static HashMap<String,String> readSignature(String file) throws Exception {
        Path path = Paths.get("signature.key");
        if(!Files.exists(path)){
            throw new Exception();
        }
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        HashMap<String,String> privateUrls = new HashMap<>();
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key,value);
        }
        return privateUrls;
    }


    public static void info (String argsOts) {
        try {
            Path pathOts = Paths.get(argsOts);
            byte[] byteOts = Files.readAllBytes(pathOts);
            String infoResult = OpenTimestamps.info(byteOts);
            System.out.println(infoResult);
        } catch (IOException e) {
            e.printStackTrace();
            log.severe("No valid file");
        }
    }

    private static void stamp(String argsFile, List<String> calendarsUrl, Integer m, String signatureFile) {
        FileInputStream fis = null;
        try {
            String argsOts = argsFile + ".ots";
            Path path = Paths.get(argsOts);
            if(Files.exists(path)) {
                System.out.println("File '" + argsOts + "' already exist");
                return;
            }
            File file = new File(argsFile);
            fis = new FileInputStream(file);

            HashMap<String, String> privateUrls = new HashMap<String, String>();
            if(signatureFile != null && signatureFile != "") {
                try {
                    privateUrls = readSignature(signatureFile);
                } catch (Exception e) {
                    log.severe("No valid signature file");
                }
            }
            byte[] stampResult = OpenTimestamps.stamp(fis, calendarsUrl, m, privateUrls);

            Files.write(path, stampResult);
            System.out.println("The timestamp proof '" + argsOts + "' has been created!");
        } catch (IOException e) {
            e.printStackTrace();
            log.severe("No valid file");
        } finally {
            try {
                if(fis!=null)
                    fis.close();
            }catch  (IOException e) {
                log.severe("No valid file");
            }
        }
    }

    public static void verify (String argsOts) {
        FileInputStream fis = null;
        try {

            Path pathOts = Paths.get(argsOts);
            byte[] byteOts = Files.readAllBytes(pathOts);

            String argsFile = argsOts.replace(".ots","");
            File file = new File(argsFile);
            fis = new FileInputStream(file);
            System.out.println("Assuming target filename is '" + argsFile + "'");
            Long timestamp = OpenTimestamps.verify(byteOts,fis);
            if(timestamp==null){
                System.out.println("Pending or Bad attestation");
            }else {
                System.out.println("Success! Bitcoin attests data existed as of "+ new Date(timestamp*1000) );
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.severe("No valid file");
        } finally {
            try {
                fis.close();
            }catch  (IOException e) {
                log.severe("No valid file");
            }
        }
    }

    public static void upgrade (String argsOts) {
        try {
            Path pathOts = Paths.get(argsOts);
            byte[] byteOts = Files.readAllBytes(pathOts);
            byte[] upgradeResult = OpenTimestamps.upgrade(byteOts);
            if(Arrays.equals(byteOts, upgradeResult)) {
                System.out.println("Timestamp not upgraded");
            } else {
                byte[] byteBak = Files.readAllBytes(pathOts);
                Path pathBak = Paths.get(argsOts+".bak");
                Files.write(pathBak, byteBak);

                // Write new Upgrade Result
                Files.write(pathOts, upgradeResult);
            }
            //System.out.println(Utils.bytesToHex(upgradeResult));

            // Copy Bak File

        } catch (IOException e) {
            e.printStackTrace();
            log.severe("No valid file");
        }
    }

    public static void showVersion() {
        System.out.println("Version: " + title + " v." + version );

    }
    public static void showHelp() {
        System.out.println(
                "Usage: " + title + " [options] {stamp,s,upgrade,u,verify,v,info} [arguments]\n\n" +
                "Subcommands:\n" +
                "s, stamp FILE\tCreate timestamp with the aid of a remote calendar, the output receipt will be saved with .ots\n" +
                "S, multistamp FILES\tCreate timestamp with the aid of a remote calendar, the output receipt will be saved with .ots\n" +
                "i, info FILE_OTS \tShow information on a timestamp.\n" +
                "v, verify FILE_OTS\tVerify the timestamp attestations, expect original file present in the same directory without .ots\n" +
                "u, upgrade FILE_OTS\tUpgrade remote calendar timestamps to be locally verifiable.\n\n" +
                "Options:\n" +
                        "-c, --calendar \tCreate timestamp with the aid of a remote calendar. May be specified multiple times.\n" +
                        "-k, --key \tSignature key file of private remote calendars.\n"+
                        "-m     \t\tCommitments are sent to remote calendars in the event of timeout the timestamp is considered done if at least M calendars replied.\n" +
                        "-V, --version  \tprint " + title + " version.\n" +
                        "-h, --help     \tprint this help.\n" +
                        "\nLicense: LGPL."
        );
    }

}
