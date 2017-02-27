import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * OpenTimestamps module.
 * @module OpenTimestamps
 * @author EternityWall
 * @license LPGL3
 */

public class OpenTimestamps {


    /**
     * Show information on a timestamp.
     * @exports OpenTimestamps/info
     * @param {byte[]} ots - The ots array buffer.
     */
    public static String info(byte[] ots) {
        if (ots == null) {
            return "No ots file";
        }

        StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
        DetachedTimestampFile detachedTimestampFile=DetachedTimestampFile.deserialize(ctx);

        String fileHash = Utils.bytesToHex(detachedTimestampFile.timestamp.msg);
        String hashOp = ((OpCrypto) detachedTimestampFile.fileHashOp)._HASHLIB_NAME();

        String firstLine = "File " + hashOp + " hash: " + fileHash + '\n';
        return firstLine + "Timestamp:\n" + detachedTimestampFile.timestamp.strTree(0);
    }

    /**
     * Create timestamp with the aid of a remote calendar. May be specified multiple times.
     * @exports OpenTimestamps/stamp
     * @param {byte[]} plain - The plain array buffer to stamp.
     * @param {Boolean} isHash - 1 = Hash , 0 = Data File
     */
    public static byte[] stamp(byte[]plain, Boolean isHash) throws IOException {
        DetachedTimestampFile fileTimestamp;
        if (isHash != null && isHash == true) {
            // Read Hash
            fileTimestamp = DetachedTimestampFile.fromHash(new OpSHA256(), plain);
        } else {
            // Read from file stream
            StreamDeserializationContext ctx = new StreamDeserializationContext(plain);
            fileTimestamp = DetachedTimestampFile.fromBytes(new OpSHA256(), ctx);
        }

         /* Add nonce:
       * Remember that the files - and their timestamps - might get separated
       * later, so if we didn't use a nonce for every file, the timestamp
       * would leak information on the digests of adjacent files.
       * */
        Timestamp merkleRoot;
        byte[] bytesRandom16 = new byte[16];
        try {
            bytesRandom16 = Utils.randBytes(16);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException();
        }

        // nonce_appended_stamp = file_timestamp.timestamp.ops.add(OpAppend(os.urandom(16)))
        Op opAppend = new OpAppend(bytesRandom16);
        Timestamp nonceAppendedStamp = fileTimestamp.timestamp.ops.get(opAppend);
        if (nonceAppendedStamp == null) {
            nonceAppendedStamp = new Timestamp(opAppend.call(fileTimestamp.timestamp.msg));
            fileTimestamp.timestamp.ops.put(opAppend, nonceAppendedStamp);
        }

        // merkle_root = nonce_appended_stamp.ops.add(OpSHA256())
        Op opSHA256 = new OpSHA256();
        merkleRoot = nonceAppendedStamp.ops.get(opSHA256);
        if (merkleRoot == null) {
            merkleRoot = new Timestamp(opSHA256.call(nonceAppendedStamp.msg));
            nonceAppendedStamp.ops.put(opSHA256, merkleRoot);
        }

        Timestamp merkleTip = merkleRoot;
        List<String> calendarUrls = new ArrayList<String>();
        calendarUrls.add("https://alice.btc.calendar.opentimestamps.org");
        // calendarUrls.append('https://b.pool.opentimestamps.org');
        calendarUrls.add("https://ots.eternitywall.it");

        Timestamp resultTimestamp = OpenTimestamps.createTimestamp(merkleTip, calendarUrls);

        if (resultTimestamp == null) {
            throw new IOException();
        }
        // Timestamp serialization
        StreamSerializationContext css = new StreamSerializationContext();
        fileTimestamp.serialize(css);
        return css.getOutput();
    }

    /**
     * Create a timestamp
     * @param {timestamp} timestamp - The timestamp.
     * @param {List<String>} calendarUrls - List of calendar's to use.
     */
    public static Timestamp createTimestamp(Timestamp timestamp, List<String>calendarUrls) {
        List<Calendar> calendars = new ArrayList<Calendar>();
        /*for (final String calendarUrl : calendarUrls) {
            Calendar calendar = new Calendar(calendarUrl);
            calendars.add(calendar.submit(timestamp.msg));
        }*/
        Calendar calendar = new Calendar(calendarUrls.get(0));
        Timestamp resultTimestamp = calendar.submit(timestamp.msg);
        timestamp.merge(resultTimestamp);
        return timestamp;
    }


    /**
     * Verify a timestamp.
     * @exports OpenTimestamps/verify
     * @param {byte[]} ots - The ots array buffer containing the proof to verify.
     * @param {byte[]} plain - The plain array buffer to verify.
     * @param {Boolean} isHash - 1 = Hash , 0 = Data File
     */
    public static String verify(byte[]ots, byte[]plain, Boolean isHash) {
        // Read OTS
        DetachedTimestampFile detachedTimestamp = null;
        try {
            StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
            detachedTimestamp = DetachedTimestampFile.deserialize(ctx);
        } catch (Exception e) {

        }

        byte[] actualFileDigest = new byte[0];
        if (isHash == null || !isHash) {
            // Read from file stream
            try {
                StreamDeserializationContext ctxHashfd = new StreamDeserializationContext(plain);
                actualFileDigest = ((OpCrypto)(detachedTimestamp.fileHashOp)).hashFd(ctxHashfd);
            } catch (Exception e) {

            }
        } else {
            // Read Hash
            try {
                actualFileDigest = plain.clone();
            } catch (Exception e) {

            }
        }

        byte[] detachedFileDigest = detachedTimestamp.fileDigest();
        if (!Arrays.equals(actualFileDigest, detachedFileDigest)) {
            System.err.print("Expected digest " + Utils.bytesToHex(detachedTimestamp.fileDigest()));
            System.err.print("File does not match original!");
            
        }

        // console.log(Timestamp.strTreeExtended(detachedTimestamp.timestamp, 0));
        return OpenTimestamps.verifyTimestamp(detachedTimestamp.timestamp);
    }

    /** Verify a timestamp.
     * @param {Timestamp} timestamp - The timestamp.
     * @return {int} unix timestamp if verified, undefined otherwise.
     */
    public static String verifyTimestamp(Timestamp timestamp) {
        Boolean found = false;

        for (Map.Entry<byte[], TimeAttestation> item : timestamp.allAttestations().entrySet()) {
            byte[] msg = item.getKey();
            TimeAttestation attestation = item.getValue();

            if (!found) { // Verify only the first BitcoinBlockHeaderAttestation
                if (attestation instanceof PendingAttestation) {
                    // console.log('PendingAttestation: pass ');
                } else if (attestation instanceof BitcoinBlockHeaderAttestation) {
                    found = true;
                    // console.log('Request to insight ');
                    Insight insight = new Insight("https://insight.bitpay.com/api");

                    String height = String.valueOf(((BitcoinBlockHeaderAttestation) attestation).height&0xff);
                    InsightResponse blockHash = insight.blockhash(height);
                    InsightResponse blockInfo = insight.block(blockHash.getBlockHash());

                    byte[] merkle = Utils.hexToBytes(blockInfo.getMerkleroot());
                    byte[] message = Utils.arrayReverse(msg);

                    // console.log('merkleroot: ' + Utils.bytesToHex(merkle));
                    // console.log('msg: ' + Utils.bytesToHex(message));
                    // console.log('Time: ' + (new Date(blockInfo.time * 1000)));

                    // One Bitcoin attestation is enought
                    if (Arrays.equals(merkle, message)) {
                        return blockInfo.getTime();
                    } else {
                        return "";
                    }
                }
            }
        }
        if (!found) {
            return "";
        }
        return "";
    }

    /** Upgrade a timestamp.
     * @param {byte[]} ots - The ots array buffer containing the proof to verify.
     * @return {Promise} resolve(changed) : changed = True if the timestamp has changed, False otherwise.
     */
    public static void upgrade(byte[] ots) {

    }
}


