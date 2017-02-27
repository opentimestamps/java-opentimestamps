
/**
 * Detached Timestamp File module.
 * @module DetachedTimestampFile
 * @author EternityWall
 * @license LPGL3
 */

/**
 * Class representing Detached Timestamp File.
 * A file containing a timestamp for another file.
 * Contains a timestamp, along with a header and the digest of the file.
 */
class DetachedTimestampFile {

    /**
     * Header magic bytes
     * Designed to be give the user some information in a hexdump, while being identified as 'data' by the file utility.
     * @type {int[]}
     * @default \x00OpenTimestamps\x00\x00Proof\x00\xbf\x89\xe2\xe8\x84\xe8\x92\x94
     */
    static byte[] HEADER_MAGIC = {(byte)0x00, (byte)0x4f, (byte)0x70, (byte)0x65, (byte)0x6e, (byte)0x54, (byte)0x69,(byte)0x6d, (byte)0x65, (byte)0x73,
            (byte)0x74, (byte)0x61, (byte)0x6d, (byte)0x70, (byte)0x73, (byte)0x00, (byte)0x00, (byte)0x50, (byte)0x72, (byte)0x6f, (byte)0x6f, (byte)0x66, (byte)0x00,
            (byte)0xbf, (byte)0x89, (byte)0xe2, (byte)0xe8, (byte)0x84, (byte)0xe8, (byte)0x92, (byte)0x94};

    /**
     * While the git commit timestamps have a minor version, probably better to
     * leave it out here: unlike Git commits round-tripping is an issue when
     * timestamps are upgraded, and we could end up with bugs related to not
     * saving/updating minor version numbers correctly.
     * @type {int}
     * @default 1
     */
    static byte MAJOR_VERSION = 1;
    // const MIN_FILE_DIGEST_LENGTH = 20;
    // const MAX_FILE_DIGEST_LENGTH = 32;

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
     * Serialize a Timestamp File.
     * @param {StreamSerializationContext} ctx - The stream serialization context.
     * @return {byte[]} The serialized DetachedTimestampFile object.
     */
    public void serialize(StreamSerializationContext ctx) {
        ctx.writeBytes(HEADER_MAGIC);
        ctx.writeVaruint(MAJOR_VERSION);
        this.fileHashOp.serialize(ctx);
        ctx.writeBytes(this.timestamp.msg);
        this.timestamp.serialize(ctx);
    }

    /**
     * Deserialize a Timestamp File.
     * @param {StreamDeserializationContext} ctx - The stream deserialization context.
     * @return {DetachedTimestampFile} The generated DetachedTimestampFile object.
     */
    public static DetachedTimestampFile deserialize(StreamDeserializationContext ctx) {
        ctx.assertMagic(HEADER_MAGIC);
        ctx.readVaruint();

        OpCrypto fileHashOp = (OpCrypto) OpCrypto.deserialize(ctx) ;
        byte[] fileHash = ctx.readBytes(fileHashOp._DIGEST_LENGTH());
        Timestamp timestamp = Timestamp.deserialize(ctx, fileHash);

        ctx.assertEof();
        return new DetachedTimestampFile(fileHashOp, timestamp);
    }

    /**
     * Read the Detached Timestamp File from bytes.
     * @param {Op} fileHashOp - The file hash operation.
     * @param {StreamDeserializationContext} ctx - The stream deserialization context.
     * @return {DetachedTimestampFile} The generated DetachedTimestampFile object.
     */
    public static DetachedTimestampFile fromBytes(OpCrypto fileHashOp, StreamDeserializationContext ctx) {
        byte[] fdHash = fileHashOp.hashFd(ctx);
        return new DetachedTimestampFile(fileHashOp, new Timestamp(fdHash));
    }

    /**
     * Read the Detached Timestamp File from hash.
     * @param {Op} fileHashOp - The file hash operation.
     * @param {int[]} fdHash - The hash file.
     * @return {DetachedTimestampFile} The generated DetachedTimestampFile object.
     */
    public static DetachedTimestampFile fromHash(Op fileHashOp, byte[] fdHash) {
        return new DetachedTimestampFile(fileHashOp, new Timestamp(fdHash));
    }

    /**
     * Print the object.
     * @return {string} The output.
     */
    public String toString() {
        String output = "DetachedTimestampFile\n";
        output += "fileHashOp: " + this.fileHashOp.toString() + '\n';
        output += "timestamp: " + this.timestamp.toString() + '\n';
        return output;
    }

}
