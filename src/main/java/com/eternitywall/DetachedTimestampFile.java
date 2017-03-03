package com.eternitywall;
/**
 * Detached com.eternitywall.Timestamp File module.
 *
 * @module com.eternitywall.DetachedTimestampFile
 * @author EternityWall
 * @license LPGL3
 */

import com.eternitywall.op.Op;
import com.eternitywall.op.OpCrypto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Class representing Detached com.eternitywall.Timestamp File.
 * A file containing a timestamp for another file.
 * Contains a timestamp, along with a header and the digest of the file.
 */
class DetachedTimestampFile {

    private static Logger log = Logger.getLogger(DetachedTimestampFile.class.getName());


    /**
     * Header magic bytes
     * Designed to be give the user some information in a hexdump, while being identified as 'data' by the file utility.
     * @type {int[]}
     * @default \x00OpenTimestamps\x00\x00Proof\x00\xbf\x89\xe2\xe8\x84\xe8\x92\x94
     */
    static byte[] HEADER_MAGIC = {(byte) 0x00, (byte) 0x4f, (byte) 0x70, (byte) 0x65, (byte) 0x6e, (byte) 0x54, (byte) 0x69, (byte) 0x6d, (byte) 0x65, (byte) 0x73,
            (byte) 0x74, (byte) 0x61, (byte) 0x6d, (byte) 0x70, (byte) 0x73, (byte) 0x00, (byte) 0x00, (byte) 0x50, (byte) 0x72, (byte) 0x6f, (byte) 0x6f, (byte) 0x66, (byte) 0x00,
            (byte) 0xbf, (byte) 0x89, (byte) 0xe2, (byte) 0xe8, (byte) 0x84, (byte) 0xe8, (byte) 0x92, (byte) 0x94};

    /**
     * While the git commit timestamps have a minor version, probably better to
     * leave it out here: unlike Git commits round-tripping is an issue when
     * timestamps are upgraded, and we could end up with bugs related to not
     * saving/updating minor version numbers correctly.
     * @type {int}
     * @default 1
     */
    static byte MAJOR_VERSION = 1;

    Op fileHashOp;
    Timestamp timestamp;

    DetachedTimestampFile(Op fileHashOp, Timestamp timestamp) {
        this.fileHashOp = fileHashOp;
        this.timestamp = timestamp;
    }

    /**
     * The digest of the file that was timestamped.
     * @return {byte} The message inside the timestamp.
     */
    public byte[] fileDigest() {
        return this.timestamp.msg;
    }

    /**
     * Serialize a com.eternitywall.Timestamp File.
     * @param {com.eternitywall.StreamSerializationContext} ctx - The stream serialization context.
     * @return {byte[]} The serialized com.eternitywall.DetachedTimestampFile object.
     */
    public void serialize(StreamSerializationContext ctx) {
        ctx.writeBytes(HEADER_MAGIC);
        ctx.writeVaruint(MAJOR_VERSION);
        this.fileHashOp.serialize(ctx);
        ctx.writeBytes(this.timestamp.msg);
        this.timestamp.serialize(ctx);
    }

    /**
     * Deserialize a com.eternitywall.Timestamp File.
     * @param {com.eternitywall.StreamDeserializationContext} ctx - The stream deserialization context.
     * @return {com.eternitywall.DetachedTimestampFile} The generated com.eternitywall.DetachedTimestampFile object.
     */
    public static DetachedTimestampFile deserialize(StreamDeserializationContext ctx) {
        ctx.assertMagic(HEADER_MAGIC);
        ctx.readVaruint();

        OpCrypto fileHashOp = (OpCrypto) OpCrypto.deserialize(ctx);
        byte[] fileHash = ctx.readBytes(fileHashOp._DIGEST_LENGTH());
        Timestamp timestamp = Timestamp.deserialize(ctx, fileHash);

        ctx.assertEof();
        return new DetachedTimestampFile(fileHashOp, timestamp);
    }

    /**
     * Read the Detached com.eternitywall.Timestamp File from bytes.
     * @param {com.eternitywall.op.Op} fileHashOp - The file hash operation.
     * @param {com.eternitywall.StreamDeserializationContext} ctx - The stream deserialization context.
     * @return {com.eternitywall.DetachedTimestampFile} The generated com.eternitywall.DetachedTimestampFile object.
     */
    public static DetachedTimestampFile fromBytes(OpCrypto fileHashOp, StreamDeserializationContext ctx) throws NoSuchAlgorithmException {
        byte[] fdHash = fileHashOp.hashFd(ctx);
        return new DetachedTimestampFile(fileHashOp, new Timestamp(fdHash));
    }

    /**
     * Read the Detached com.eternitywall.Timestamp File from hash.
     * @param {com.eternitywall.op.Op} fileHashOp - The file hash operation.
     * @param {byte[]} fdHash - The hash file.
     * @return {com.eternitywall.DetachedTimestampFile} The generated com.eternitywall.DetachedTimestampFile object.
     */
    public static DetachedTimestampFile from(OpCrypto fileHashOp, Hash hash) {
        return new DetachedTimestampFile(fileHashOp, new Timestamp(hash.getValue()));
    }

    /**
     * Read the Detached com.eternitywall.Timestamp File from File.
     * @param {com.eternitywall.op.Op} fileHashOp - The file hash operation.
     * @param {File} fdHash - The hash file.
     * @return {com.eternitywall.DetachedTimestampFile} The generated com.eternitywall.DetachedTimestampFile object.
     */
    public static DetachedTimestampFile from(OpCrypto fileHashOp, File file) throws IOException, NoSuchAlgorithmException {
        byte[] fdHash = fileHashOp.hashFd(file);
        return new DetachedTimestampFile(fileHashOp, new Timestamp(fdHash));
    }

    /**
     * Read the Detached com.eternitywall.Timestamp File from InputStream.
     * @param {com.eternitywall.op.Op} fileHashOp - The file hash operation.
     * @param {File} fdHash - The hash file.
     * @return {com.eternitywall.DetachedTimestampFile} The generated com.eternitywall.DetachedTimestampFile object.
     */
    public static DetachedTimestampFile from(OpCrypto fileHashOp, InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        byte[] fdHash = fileHashOp.hashFd(inputStream);
        return new DetachedTimestampFile(fileHashOp, new Timestamp(fdHash));
    }

    /**
     * Print the object.
     * @return {string} The output.
     */
    @Override
    public String toString() {
        String output = "com.eternitywall.DetachedTimestampFile\n";
        output += "fileHashOp: " + this.fileHashOp.toString() + '\n';
        output += "timestamp: " + this.timestamp.toString() + '\n';
        return output;
    }

}
