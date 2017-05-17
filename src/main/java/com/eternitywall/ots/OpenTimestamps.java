package com.eternitywall.ots;

import com.eternitywall.ots.attestation.BitcoinBlockHeaderAttestation;
import com.eternitywall.ots.attestation.PendingAttestation;
import com.eternitywall.ots.attestation.TimeAttestation;
import com.eternitywall.ots.op.Op;
import com.eternitywall.ots.op.OpAppend;
import com.eternitywall.ots.op.OpCrypto;
import com.eternitywall.ots.op.OpSHA256;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * com.eternitywall.ots.OpenTimestamps
 *
 * @author EternityWall
 * com.eternitywall.ots.OpenTimestamps
 * LPGL3
 */

public class OpenTimestamps {


    private static Logger log = Logger.getLogger(OpenTimestamps.class.getName());


    /**
     * Show information on a timestamp.
     *
     * @param ots The ots array buffer.
     * @return the string representation of the timestamp
     */
    public static String info(byte[] ots) {
        if (ots == null) {
            return "No ots file";
        }

        StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
        DetachedTimestampFile detachedTimestampFile = DetachedTimestampFile.deserialize(ctx);

        String fileHash = DatatypeConverter.printHexBinary(detachedTimestampFile.timestamp.msg).toLowerCase();
        String hashOp = ((OpCrypto) detachedTimestampFile.fileHashOp)._TAG_NAME();

        String firstLine = "File " + hashOp + " hash: " + fileHash + '\n';
        return firstLine + "Timestamp:\n" + detachedTimestampFile.timestamp.strTree(0);
    }
    /**
     * Show information on a timestamp.
     *
     * @param timestamp The timestamp buffered.
     * @return the string representation of the timestamp
     */
    public static String info(Timestamp timestamp) {
        if (timestamp == null) {
            return "No timestamp";
        }
        String fileHash = DatatypeConverter.printHexBinary(timestamp.msg).toLowerCase();
        String firstLine = "Hash: " + fileHash + '\n';
        return firstLine + "Timestamp:\n" + timestamp.strTree(0);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param inputStream The input stream to stamp.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @param privateCalendarsUrl The list of private calendar urls with signature.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    public static byte[] stamp(InputStream inputStream, List<String> calendarsUrl, Integer m, HashMap<String,String> privateCalendarsUrl) throws IOException {
        // Parse parameters
        if (inputStream == null) {
            throw new IOException();
        }
        // Read from file reader stream
        try {
            DetachedTimestampFile fileTimestamp;
            fileTimestamp = DetachedTimestampFile.from(new OpSHA256(), inputStream);
            return stamp(fileTimestamp, calendarsUrl, m, privateCalendarsUrl);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.severe("Invalid InputStream");
            throw new IOException();
        }
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param inputStream The input stream to stamp.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    public static byte[] stamp(InputStream inputStream) throws IOException {
        return OpenTimestamps.stamp(inputStream, null, 0, null);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param inputStream The input stream to stamp.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    public static byte[] stamp(InputStream inputStream, List<String> calendarsUrl, Integer m) throws IOException {
        return OpenTimestamps.stamp(inputStream, calendarsUrl, m, null);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     * @param content The sha 256 of the file to stamp.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @param privateCalendarsUrl The list of private calendar urls with signature.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */

    public static byte[] stamp(byte[] content, List<String> calendarsUrl, Integer m, HashMap<String,String> privateCalendarsUrl) throws IOException {
        return OpenTimestamps.stamp(new ByteArrayInputStream(content), calendarsUrl , m, privateCalendarsUrl);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     * @param content The sha 256 of the file to stamp.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */

    public static byte[] stamp(byte[] content) throws IOException {
        return OpenTimestamps.stamp(content,null , 0, null);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     * @param content The sha 256 of the file to stamp.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */

    public static byte[] stamp(byte[] content, List<String> calendarsUrl, Integer m) throws IOException {
        return OpenTimestamps.stamp(new ByteArrayInputStream(content), calendarsUrl , m, null);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param hash The sha 256 of the file to stamp.
     * @return The plain array buffer of stamped.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @param privateCalendarsUrl The list of private calendar urls with signature.
     * @throws IOException desc
     */
    public static byte[] stamp(Hash hash, List<String> calendarsUrl, Integer m, HashMap<String,String> privateCalendarsUrl) throws IOException {
        // Read from file reader stream
        DetachedTimestampFile fileTimestamp;
        fileTimestamp = DetachedTimestampFile.from(new OpSHA256(), hash);
        return stamp(fileTimestamp, calendarsUrl, m, privateCalendarsUrl);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param hash The sha 256 of the file to stamp.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    public static byte[] stamp(Hash hash) throws IOException {
        return OpenTimestamps.stamp(hash,null, 0, null);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param hash The sha 256 of the file to stamp.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    public static byte[] stamp(Hash hash, List<String> calendarsUrl, Integer m) throws IOException {
        // Read from file reader stream
        DetachedTimestampFile fileTimestamp;
        fileTimestamp = DetachedTimestampFile.from(new OpSHA256(), hash);
        return stamp(fileTimestamp, calendarsUrl, m, null);
    }


    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param fileTimestamp The Detached Timestamp File.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    private static byte[] stamp(DetachedTimestampFile fileTimestamp) throws IOException {
        return OpenTimestamps.stamp(fileTimestamp,null,0, null);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param fileTimestamp The Detached Timestamp File.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    private static byte[] stamp(DetachedTimestampFile fileTimestamp, List<String> calendarsUrl, Integer m) throws IOException {
        return OpenTimestamps.stamp(fileTimestamp,calendarsUrl,m, null);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     *
     * @param fileTimestamp The timestamp to stamp.
     * @param calendarsUrl The list of calendar urls.
     * @param m The number of calendar to use.
     * @param privateCalendarsUrl The list of private calendar urls with signature.
     * @return The plain array buffer of stamped.
     * @throws IOException desc
     */
    public static byte[] stamp(DetachedTimestampFile fileTimestamp,  List<String> calendarsUrl, Integer m, HashMap<String,String> privateCalendarsUrl) throws IOException {
        /**
         * Add nonce:
         * Remember that the files - and their timestamps - might get separated
         * later, so if we didn't use a nonce for every file, the timestamp
         * would leak information on the digests of adjacent files.
         */
        Timestamp merkleRoot;
        byte[] bytesRandom16 = new byte[16];
        try {
            bytesRandom16 = Utils.randBytes(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IOException();
        }

        // nonce_appended_stamp = file_timestamp.timestamp.ops.add(com.eternitywall.ots.op.OpAppend(os.urandom(16)))
        Op opAppend = new OpAppend(bytesRandom16);
        Timestamp nonceAppendedStamp = fileTimestamp.timestamp.ops.get(opAppend);
        if (nonceAppendedStamp == null) {
            nonceAppendedStamp = new Timestamp(opAppend.call(fileTimestamp.timestamp.msg));
            fileTimestamp.timestamp.ops.put(opAppend, nonceAppendedStamp);
        }

        // merkle_root = nonce_appended_stamp.ops.add(com.eternitywall.ots.op.OpSHA256())
        Op opSHA256 = new OpSHA256();
        merkleRoot = nonceAppendedStamp.ops.get(opSHA256);
        if (merkleRoot == null) {
            merkleRoot = new Timestamp(opSHA256.call(nonceAppendedStamp.msg));
            nonceAppendedStamp.ops.put(opSHA256, merkleRoot);
        }

        // Markle root
        Timestamp merkleTip = merkleRoot;

        // Parse parameters
        if(privateCalendarsUrl == null) {
            privateCalendarsUrl = new HashMap<>();
        }
        if((calendarsUrl==null || calendarsUrl.size()==0) && (privateCalendarsUrl.size() == 0) ) {
            calendarsUrl = new ArrayList<String>();
            calendarsUrl.add("https://alice.btc.calendar.opentimestamps.org");
            calendarsUrl.add("https://bob.btc.calendar.opentimestamps.org");
            calendarsUrl.add("https://finney.calendar.eternitywall.com");
        }
        if(m==null || m<=0){
            if(calendarsUrl.size() + privateCalendarsUrl.size() == 0 ) {
                m = 2;
            } else if(calendarsUrl.size() + privateCalendarsUrl.size() == 1 ) {
                m = 1;
            } else {
                m = calendarsUrl.size() + privateCalendarsUrl.size();
            }
        }
        if(m<0 || m > calendarsUrl.size() + privateCalendarsUrl.size()) {
            log.severe("m cannot be greater than available calendar neither less or equal 0");
            throw new IOException();
        }

        Timestamp resultTimestamp = OpenTimestamps.create(merkleTip, calendarsUrl, m, privateCalendarsUrl);

        if (resultTimestamp == null) {
            throw new IOException();
        }
        // com.eternitywall.ots.Timestamp serialization
        StreamSerializationContext css = new StreamSerializationContext();
        fileTimestamp.serialize(css);
        return css.getOutput();
    }

    /**
     * Create a timestamp
     *
     * @param timestamp The timestamp.
     * @param calendarUrls List of calendar's to use.
     * @param m Number of calendars to use.
     * @return The created timestamp.
     */
    private static Timestamp create(Timestamp timestamp, List<String> calendarUrls, Integer m, HashMap<String,String> privateCalendarUrls) {

        int capacity = calendarUrls.size()+privateCalendarUrls.size();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(capacity);


        // Submit to all private calendars with the signature key
        for(Map.Entry<String, String> entry : privateCalendarUrls.entrySet()) {
            String calendarUrl = "https://" + entry.getKey();
            String signature = entry.getValue();
            log.info("Submitting to remote private calendar "+calendarUrl);
            try {
                CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, timestamp.msg);
                ECKey key = null;
                try {
                    BigInteger privKey = new BigInteger(signature);
                    key = ECKey.fromPrivate(privKey);
                }catch (Exception e){
                    try {
                        DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(NetworkParameters.prodNet(), signature);
                        key = dumpedPrivateKey.getKey();
                    }catch (Exception err){
                        log.severe("Invalid private key");
                    }
                }
                task.setKey(key);
                task.setQueue(queue);
                executor.submit(task);
                capacity++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Submit to all public calendars
        for (final String calendarUrl : calendarUrls) {

            log.info("Submitting to remote calendar "+calendarUrl);

            try {
                CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, timestamp.msg);
                task.setQueue(queue);
                executor.submit(task);
                capacity++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int count=0;
        for (int i=0; i < capacity; i++) {

            try {
                Optional<Timestamp> optionalStamp = queue.take();
                if(optionalStamp.isPresent()) {
                    timestamp.merge(optionalStamp.get());
                    count++;
                }
                if(count >= m){
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(count < m ){
            log.severe("Failed to create timestamp: requested "+ String.valueOf(m)+" attestation" + ((m>1)?"s":"")+" but received only "+String.valueOf(count));
        }
        //shut down the executor service now
        executor.shutdown();

        return timestamp;
    }



    /**
     * Make Merkle Tree.
     * @param hashes The list of Hash to merklefy.
     * @return markle tip timestamp.
     */
    public static Timestamp makeMerkleTree(List<Hash> hashes, OpCrypto opCrypto){
        List<DetachedTimestampFile> fileTimestamps = new ArrayList<>();

        for (Hash hash : hashes){
            DetachedTimestampFile fileTimestamp = DetachedTimestampFile.from(opCrypto, hash);
            fileTimestamps.add(fileTimestamp);
        }

        return OpenTimestamps.makeMerkleTree(fileTimestamps);

    }

    /**
     * Make Merkle Tree.
     * @param fileTimestamps The list of DetachedTimestampFile.
     * @return markle tip timestamp.
     */
    public static Timestamp makeMerkleTree(List<DetachedTimestampFile> fileTimestamps){
        List<Timestamp> merkleRoots = new ArrayList<>();

        for (DetachedTimestampFile fileTimestamp : fileTimestamps) {

            byte[] bytesRandom16 = new byte[16];
            try {
                bytesRandom16 = Utils.randBytes(16);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            // nonce_appended_stamp = file_timestamp.timestamp.ops.add(com.eternitywall.ots.op.OpAppend(os.urandom(16)))
            Op opAppend = new OpAppend(bytesRandom16);
            Timestamp nonceAppendedStamp = fileTimestamp.timestamp.ops.get(opAppend);
            if (nonceAppendedStamp == null) {
                nonceAppendedStamp = new Timestamp(opAppend.call(fileTimestamp.timestamp.msg));
                fileTimestamp.timestamp.ops.put(opAppend, nonceAppendedStamp);
            }

            // merkle_root = nonce_appended_stamp.ops.add(com.eternitywall.ots.op.OpSHA256())
            Op opSHA256 = new OpSHA256();
            Timestamp merkleRoot = nonceAppendedStamp.ops.get(opSHA256);
            if (merkleRoot == null) {
                merkleRoot = new Timestamp(opSHA256.call(nonceAppendedStamp.msg));
                nonceAppendedStamp.ops.put(opSHA256, merkleRoot);
            }

            merkleRoots.add(merkleRoot);
        }

        Timestamp merkleTip = Merkle.makeMerkleTree(merkleRoots);
        return merkleTip;
    }

    /**
     * Verify a timestamp.
     * @param ots The ots array buffer containing the proof to verify.
     * @param stampedHash The plain array buffer to verify.
     * @return unix timestamp if verified, undefined otherwise.
     * @throws IOException desc
     */
    public static Long verify(byte[] ots, Hash stampedHash) throws IOException {
        DetachedTimestampFile detachedTimestamp = null;
        try {
            StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
            detachedTimestamp = DetachedTimestampFile.deserialize(ctx);
        } catch (Exception e) {
            System.err.print("com.eternitywall.ots.StreamDeserializationContext error");
        }

        // Call Verify
        return OpenTimestamps.verify(detachedTimestamp,stampedHash);
    }

    /**
     * Verify a timestamp.
     * @param ots The ots array buffer containing the proof to verify.
     * @param stamped The plain array buffer to verify.
     * @return unix timestamp if verified, undefined otherwise.
     * @throws IOException desc
     */
    public static Long verify(byte[] ots, byte[] stamped) throws IOException {
       return verify(ots, new ByteArrayInputStream(stamped));
    }

    /**
     * Verify a timestamp.
     * @param ots The ots array buffer containing the proof to verify.
     * @param inputStream The input stream to verify.
     * @return unix timestamp if verified, undefined otherwise.
     * @throws IOException desc
     */
    public static Long verify(byte[] ots, InputStream inputStream) throws IOException {

        // Read OTS
        DetachedTimestampFile detachedTimestamp = null;
        try {
            StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
            detachedTimestamp = DetachedTimestampFile.deserialize(ctx);
        } catch (Exception e) {
            System.err.print("com.eternitywall.ots.StreamDeserializationContext error");
        }

        // Read STAMPED
        byte[] actualFileDigest = new byte[0];
        try {
            actualFileDigest = ((OpCrypto) (detachedTimestamp.fileHashOp)).hashFd(inputStream);
        } catch (Exception e) {
            log.severe("com.eternitywall.ots.StreamDeserializationContext : inputStream error");
        }

        // Call Verify
        return OpenTimestamps.verify(detachedTimestamp,new Hash(actualFileDigest));
    }

    /**
     * Verify a timestamp.
     * @param ots The ots array buffer containing the proof to verify.
     * @param stamped The File to verify.
     * @return unix timestamp if verified, undefined otherwise.
     * @throws IOException desc
     */
    public static Long verify(byte[] ots, File stamped) throws IOException {

        // Read OTS
        DetachedTimestampFile detachedTimestamp = null;
        try {
            StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
            detachedTimestamp = DetachedTimestampFile.deserialize(ctx);
        } catch (Exception e) {
            System.err.print("com.eternitywall.ots.StreamDeserializationContext error");
        }

        // Read STAMPED
        byte[] actualFileDigest = new byte[0];
        try {
            actualFileDigest = ((OpCrypto) (detachedTimestamp.fileHashOp)).hashFd(stamped);
        } catch (Exception e) {
            log.severe("com.eternitywall.ots.StreamDeserializationContext : file stream error");
        }

        // Call Verify
        return OpenTimestamps.verify(detachedTimestamp, new Hash(actualFileDigest));
    }



    /**
     * Verify a timestamp.
     *
     * @param detachedTimestamp The ots containing the proof to verify.
     * @param actualFileDigest The plain array buffer stamped.
     * @return the timestamp in seconds from 1 Jamuary 1970
     */
    private static Long verify(DetachedTimestampFile detachedTimestamp, Hash actualFileDigest) {

        byte[] detachedFileDigest = detachedTimestamp.fileDigest();
        if (!Arrays.equals(actualFileDigest.getValue(), detachedFileDigest)) {
            log.severe("Expected digest " + DatatypeConverter.printHexBinary(detachedTimestamp.fileDigest()).toLowerCase());
            log.severe("File does not match original!");

        }

        return OpenTimestamps.verify(detachedTimestamp.timestamp);
    }

    /**
     * Verify a timestamp.
     *
     * @param timestamp The timestamp.
     * @return unix timestamp if verified, undefined otherwise.
     */
    public static Long verify(Timestamp timestamp) {
        Boolean found = false;

        for (Map.Entry<byte[], TimeAttestation> item : timestamp.allAttestations().entrySet()) {
            byte[] msg = item.getKey();
            TimeAttestation attestation = item.getValue();

            if (!found) { // Verify only the first com.eternitywall.ots.attestation.BitcoinBlockHeaderAttestation
                if (attestation instanceof PendingAttestation) {
                } else if (attestation instanceof BitcoinBlockHeaderAttestation) {
                    found = true;
                    Integer height = ((BitcoinBlockHeaderAttestation) attestation).getHeight();

                    BlockHeader blockInfo = null;

                    try {
                        Properties properties = BitcoinNode.readBitcoinConf();
                        BitcoinNode bitcoin = new BitcoinNode(properties);
                        blockInfo = bitcoin.getBlockHeader(height);
                    } catch (Exception e1) {
                        log.fine("There is no local node available");
                        try {
                            MultiInsight insight = new MultiInsight();
                            String blockHash = null;
                            blockHash = insight.blockHash(height);
                            blockInfo = insight.block(blockHash);
                            log.info("Lite-client verification, assuming block " + blockHash + " is valid");
                            insight.getExecutor().shutdown();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            return null;
                        }
                    }

                    byte[] merkle = DatatypeConverter.parseHexBinary(blockInfo.getMerkleroot());
                    byte[] message = Utils.arrayReverse(msg);

                    // One Bitcoin attestation is enought
                    if (Arrays.equals(merkle, message)) {
                        return blockInfo.getTime();
                    } else {
                        return null;
                    }
                }
            }
        }
        if (!found) {
            return null;
        }
        return null;
    }

    /**
     * Upgrade a timestamp.
     *
     * @param ots The ots array buffer containing the proof to verify.
     * @return the upgraded timestamp
     */
    public static byte[] upgrade(byte[] ots) {

        // Read OTS
        DetachedTimestampFile detachedTimestamp = null;
        try {
            StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
            detachedTimestamp = DetachedTimestampFile.deserialize(ctx);
        } catch (Exception e) {
            log.severe("com.eternitywall.ots.StreamDeserializationContext error");
        }
        if (detachedTimestamp.timestamp.isTimestampComplete()) {
            return ots;
        }

        // Upgrade timestamp
        boolean changed = OpenTimestamps.upgrade(detachedTimestamp.timestamp);

        if (changed) {
            log.info("Timestamp has been successfully upgraded!");
        }

        if (detachedTimestamp.timestamp.isTimestampComplete()) {
            log.info("Timestamp is complete");
        } else {
            log.info("Timestamp is not complete");
        }

        StreamSerializationContext css = new StreamSerializationContext();
        detachedTimestamp.serialize(css);
        return css.getOutput();
    }


    /**
     * Attempt to upgrade an incomplete timestamp to make it verifiable.
     * Note that this means if the timestamp that is already complete, False will be returned as nothing has changed.
     *
     * @param timestamp The timestamp.
     * @return a boolean represnting if the timestamp has changed
     */
    public static boolean upgrade(Timestamp timestamp) {
        // Check remote calendars for upgrades.
        // This time we only check PendingAttestations - we can't be as agressive.

        boolean upgraded = false;

        Set<TimeAttestation> existingAttestations = timestamp.getAttestations();
        for (Timestamp subStamp : timestamp.directlyVerified()) {
            for (TimeAttestation attestation : subStamp.attestations) {
                if (attestation instanceof PendingAttestation) {
                    String calendarUrl = new String(((PendingAttestation) attestation).getUri(), StandardCharsets.UTF_8);
                    // var calendarUrl = calendarUrls[0];
                    byte[] commitment = subStamp.msg;

                    Calendar calendar = new Calendar(calendarUrl);
                    Timestamp upgradedStamp = OpenTimestamps.upgrade(subStamp, calendar, commitment, existingAttestations);
                    if(upgradedStamp != null) {
                        subStamp.merge(upgradedStamp);
                        upgraded = true;
                        return upgraded;
                    }
                }
            }
        }
        return upgraded;
    }

    private static Timestamp upgrade(Timestamp subStamp, Calendar calendar, byte[] commitment, Set<TimeAttestation> existingAttestations) {
        Timestamp upgradedStamp = calendar.getTimestamp(commitment);
        if (upgradedStamp == null) {
            return null;
        }

        Set<TimeAttestation> attsFromRemote = upgradedStamp.getAttestations();
        if (attsFromRemote.size() > 0) {
            // log.info(attsFromRemote.size + ' attestation(s) from ' + calendar.url);
        }

        // Set difference from remote attestations & existing attestations
        Set<TimeAttestation> newAttestations = attsFromRemote;
        newAttestations.removeAll(existingAttestations);

        if (newAttestations.size() == 0) {
            return null;
        }
        // changed & found_new_attestations
        // foundNewAttestations = true;
        // log.info(attsFromRemote.size + ' attestation(s) from ' + calendar.url);

        // Set union of existingAttestations & newAttestations
        existingAttestations.addAll(newAttestations);

        return upgradedStamp;
        // subStamp.merge(upgradedStamp);
        // args.cache.merge(upgraded_stamp)
        // sub_stamp.merge(upgraded_stamp)
    }
}