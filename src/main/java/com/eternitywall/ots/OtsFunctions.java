package com.eternitywall.ots; /**
 * Created by luca on 25/02/2017.
 */

import java.security.NoSuchAlgorithmException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;


import com.eternitywall.ots.op.Op;
import com.eternitywall.ots.op.OpCrypto;
import com.eternitywall.ots.op.OpSHA256;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class OtsFunctions {

//    private static Logger log = Logger.getLogger(OtsCli.class.getName());
    private static String title = "OtsFunctions";
    private static String version = "1.0";
//    private static List<String> calendarsUrl = new ArrayList<>();
//    private static List<String> files = new ArrayList<>();
//    private static String cmd = null;
//    private static Integer m = 0;
//    private static String signatureFile = "";
    private static byte[] shasum;
//    private static String[] algorithms = new String[]{"SHA256","SHA1","RIPEMD160"};
//    private static String algorithm = "SHA256";
//    private static boolean shrink = false;

    public static void main(String[] args) {

//        // Create the Options
//        Options options = new Options();
//        options.addOption( "c", "calendar", true, "Create timestamp with the aid of a remote calendar. May be specified multiple times." );
//        options.addOption( "k", "key", true, "Signature key file of private remote calendars." );
//        options.addOption( "H", "hash", true, "Pass the hash string of the document to timestamp." );
//        options.addOption( "a", "algorithm", true, "Pass the hashing algorithm of the document to timestamp: SHA256(default), SHA1, RIPEMD160." );
//        options.addOption( "m", "", true, "Commitments are sent to remote calendars in the event of timeout the timestamp is considered done if at least M calendars replied." );
//        options.addOption( "s", "shrink", false, "Shrink upgraded timestamp." );
//        options.addOption( "V", "version", false, "print " + title + " version." );
//        options.addOption( "h", "help", false, "print this help." );
//
//        // Parse the args to retrieve options & command
//        CommandLineParser parser = new BasicParser();
//        try {
//            CommandLine line = parser.parse(options, args);
//            // args are the arguments passed to the  the application via the main method
//
//            if(line.hasOption("c")) {
//                String[] cals = line.getOptionValues("c");
//                calendarsUrl.addAll( Arrays.asList(cals) );
//            }
//            if(line.hasOption("m")) {
//                m = Integer.valueOf(line.getOptionValue("m"));
//            }
//            if(line.hasOption("k")) {
//                signatureFile = line.getOptionValue("k");
//                calendarsUrl.clear();
//            }
//            if(line.hasOption("s")) {
//                shrink = true;
//            }
//            if(line.hasOption("V")) {
//                System.out.println("Version: " + title + " v." + version + '\n');
//                return;
//            }
//            if(line.hasOption("h")) {
//                showHelp();
//                return;
//            }
//            if(line.hasOption("H")) {
//                shasum = Utils.hexToBytes(line.getOptionValue("H"));
//            }
//            if(line.hasOption("a")) {
//                algorithm = line.getOptionValue("a");
//                if(!Arrays.asList(algorithms).contains(algorithm.toUpperCase())){
//                    System.out.println("Algorithm: " + algorithm + " not supported\n");
//                    return;
//                }
//            }
//            if(line.getArgList().isEmpty()){
//                showHelp();
//                return;
//            }
//
//            cmd = line.getArgList().get(0);
//            files = line.getArgList().subList(1,line.getArgList().size());
//
//        } catch(Exception e) {
//            System.out.println(title + ": invalid parameters ");
//            return;
//        }
//
//        // Parse the command
//        switch (cmd) {
//            case "info":
//            case "i":
//                if (files.isEmpty()) {
//                    System.out.println("Show information on a timestamp given as argument.\n");
//                    System.out.println(title + " info: bad options number ");
//                    break;
//                }
//                info(files.get(0));
//                break;
//            case "stamp":
//            case "s":
//                if(!files.isEmpty()) {
//                    multistamp(files, calendarsUrl, m, signatureFile, algorithm);
//                } else if (shasum != null){
//                    Hash hash = new Hash(shasum, algorithm);
//                    stamp(hash, calendarsUrl, m, signatureFile);
//                } else {
//                    System.out.println("Create timestamp with the aid of a remote calendar.\n");
//                    System.out.println(title + ": bad options number ");
//                }
//                break;
//            case "verify":
//            case "v":
//                if(!files.isEmpty()) {
//                    verify(files.get(0), null);
//                } else if (shasum != null){
//                    Hash hash = new Hash(shasum, algorithm);
//                    verify(files.get(0), hash);
//                } else {
//                    System.out.println("Verify the timestamp attestations given as argument.\n");
//                    System.out.println(title + ": bad options number ");
//                }
//                break;
//            case "upgrade":
//            case "u":
//                if (files.isEmpty()) {
//                    System.out.println("Upgrade remote calendar timestamps to be locally verifiable.\n");
//                    System.out.println(title + ": bad options number ");
//                    break;
//                }
//                upgrade(files.get(0), shrink);
//                break;
//            default:
//                System.out.println(title + ": bad option: " + cmd);
//        }

    }

    private static HashMap<String,String> readSignature(String file) throws Exception {
        Path path = Paths.get(file);
        if(!path.toFile().exists()){
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


    public static String info (String filePath) {
        try {
            Path pathOts = Paths.get(filePath);
            byte[] byteOts = Files.readAllBytes(pathOts);
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(byteOts);
            String infoResult = OpenTimestamps.info(detached);
//            System.out.println(infoResult);
            return infoResult;
        } catch (IOException e) {
//            log.severe("No valid file");
            return "No valid file";
        }
    }
    
    
    public static List<String> multistamp(List<String> argsFiles, List<String> calendarsUrl, Integer m, String signatureFile, String algorithm){
        //Create return message object
        List<String> messages = new ArrayList<>();
        // Parse input privateUrls
        HashMap<String, String> privateUrls = new HashMap<>();
        if(signatureFile != null && signatureFile != "") {
            try {
                privateUrls = readSignature(signatureFile);
            } catch (Exception e) {
                //log.severe("No valid signature file");
                messages.add("No valid signature file");
            }
        }

        // Make list of detached files
        HashMap<String, DetachedTimestampFile> mapFiles = new HashMap<>();
        for (String argsFile : argsFiles){
            try {
                File file = new File(argsFile);
                Hash hash = Hash.from( file, Hash.getOp(algorithm)._TAG());
                mapFiles.put( argsFile, DetachedTimestampFile.from(hash) );
            } catch (IOException e) {
                messages.add(e.getMessage());
                //log.severe("File read error");
                messages.add("File read error");
                return messages;
            } catch (NoSuchAlgorithmException e) {
                messages.add(e.getMessage());
                //log.severe("Crypto error");
                messages.add("Crypto error");
                return messages;
            }
        }

        // Stamping
        Timestamp stampResult;
        try {
            List<DetachedTimestampFile> detaches = new ArrayList(mapFiles.values());
            stampResult = OpenTimestamps.stamp(detaches, calendarsUrl, m, privateUrls);
            if(stampResult == null){
               throw new IOException();
            }
        } catch (IOException e) {
            messages.add(e.getMessage());
            messages.add("Stamp error");
            //log.severe("Stamp error");
            return messages;
        }

        // Generate ots output files
        for (Map.Entry<String, DetachedTimestampFile> entry : mapFiles.entrySet()){

            String argsFile = entry.getKey();
            DetachedTimestampFile detached = entry.getValue();
            String argsOts = argsFile + ".ots";
            try {
                Path path = Paths.get(argsOts);
                if (Files.exists(path)) {
                    //System.out.println("File '" + argsOts + "' already exist");
                    messages.add("File '" + argsOts + "' already exist");
                } else {
                    Files.write(path, detached.serialize());
                    //System.out.println("The timestamp proof '" + argsOts + "' has been created!");
                    messages.add("The timestamp proof '" + argsOts + "' has been created!");
                }
            }catch (Exception e){
                messages.add(e.getMessage());
                messages.add("File '" + argsOts + "' writing error");
                //log.severe("File '" + argsOts + "' writing error");
            }
        }
        return messages;
    }

    private static String stamp(Hash hash, List<String> calendarsUrl, Integer m, String signatureFile) {
        HashMap<String, String> privateUrls = new HashMap<>();
        if (signatureFile != null && signatureFile != "") {
            try {
                privateUrls = readSignature(signatureFile);
            } catch (Exception e) {
                //log.severe("No valid signature file");
                return "No valid signature file";
            }
        }

        String argsOts = Utils.bytesToHex(shasum) + ".ots";
        Path path = Paths.get(argsOts);
        if(path.toFile().exists()) {
            //System.out.println("File '" + argsOts + "' already exist");
            return "File '" + argsOts + "' already exist";
        }

        try {
            DetachedTimestampFile detached = DetachedTimestampFile.from(hash);
            Timestamp stampResult = OpenTimestamps.stamp(detached, calendarsUrl, m, privateUrls);
            Files.write(path, stampResult.serialize());
            //System.out.println("The timestamp proof '" + argsOts + "' has been created!");
            return "The timestamp proof '" + argsOts + "' has been created!";
        } catch (Exception e) {
            //log.severe("Invalid shasum");
            return "Invalid shasum";
        }
    }

    public static String verify (String argsOts, Hash hash) {
        FileInputStream fis = null;
        try {

            Path pathOts = Paths.get(argsOts);
            byte[] byteOts = Files.readAllBytes(pathOts);
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(byteOts);
            Long timestamp = null;

            if (shasum == null){
                // Read from file
                String argsFile = argsOts.replace(".ots","");
                File file = new File(argsFile);
                //System.out.println("Assuming target filename is '" + argsFile + "'");
                DetachedTimestampFile detached = DetachedTimestampFile.from(new OpSHA256(), file);
                timestamp = OpenTimestamps.verify(detachedOts,detached);
            } else {
                // Read from hash option
                //System.out.println("Assuming target hash is '" + Utils.bytesToHex(hash.getValue()) + "'");
                DetachedTimestampFile detached = DetachedTimestampFile.from(hash);
                timestamp = OpenTimestamps.verify(detachedOts,detached);
            }

            if(timestamp == null){
                //System.out.println("Pending or Bad attestation");
                return "Pending or Bad attestation";
            }else {
                //System.out.println("Success! Bitcoin attests data existed as of "+ new Date(timestamp*1000) );
                return "Success! Bitcoin attests data existed as of "+ new Date(timestamp*1000);
            }

        } catch (Exception e) {
            //log.severe("No valid file");
            return "No valid file";
        } finally {
            try {
                fis.close();
            }catch  (IOException e) {
                //log.severe("No valid file");
                return "No valid file";
            }
        }
    }

    public static String upgrade (String argsOts, boolean shrink) {
        try {
            Path pathOts = Paths.get(argsOts);
            byte[] byteOts = Files.readAllBytes(pathOts);
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(byteOts);

            boolean changed = OpenTimestamps.upgrade(detachedOts);
            if(shrink == true) {
                detachedOts.getTimestamp().shrink();
            }

            if(!shrink && !changed) {
                return "Timestamp not upgraded";
            } else {
                // Copy Bak File
                byte[] byteBak = Files.readAllBytes(pathOts);
                Path pathBak = Paths.get(argsOts+".bak");
                Files.write(pathBak, byteBak);

                // Write new Upgrade Result
                Files.write(pathOts, detachedOts.serialize());
                
                return "Timestamp successfully upgraded";
            }

        } catch (IOException e) {
            return "No valid file";
        } catch (Exception e) {
            //e.printStackTrace();
            return "Shrink error";
        }
    }

    public static String showVersion() {
        return "Version: " + title + " v." + version;

    }
//    public static void showHelp() {
//        System.out.println(
//                "Usage: " + title + " [options] {stamp,s,upgrade,u,verify,v,info,i} [arguments]\n\n" +
//                "Subcommands:\n" +
//                "s, stamp FILES\tCreate timestamp with the aid of a remote calendar, the output receipt will be saved with .ots\n" +
//                "i, info FILE_OTS \tShow information on a timestamp.\n" +
//                "v, verify FILE_OTS\tVerify the timestamp attestations, expect original file present in the same directory without .ots\n" +
//                "u, upgrade FILE_OTS\tUpgrade remote calendar timestamps to be locally verifiable.\n\n" +
//                "Options:\n" +
//                        "-c, --calendar \tCreate timestamp with the aid of a remote calendar. May be specified multiple times.\n" +
//                        "-k, --key \tSignature key file of private remote calendars.\n"+
//                        "-H, --hash \tPass the hash string of the document to timestamp.\n"+
//                        "-a, --algorithm\tPass the hashing algorithm of the document to timestamp: SHA256(default), SHA1, RIPEMD160.\n"+
//                        "-m     \t\tCommitments are sent to remote calendars in the event of timeout the timestamp is considered done if at least M calendars replied.\n" +
//                        "-s, --shrink   \tShrink upgraded timestamp.\n"+
//                        "-V, --version  \tprint " + title + " version.\n" +
//                        "-h, --help     \tprint this help.\n" +
//                        "\nLicense: LGPL."
//        );
//    }

}
