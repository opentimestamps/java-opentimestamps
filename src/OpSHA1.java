import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cryptographic SHA1 operation
 * Cryptographic operation tag numbers taken from RFC4880, although it's not
 * guaranteed that they'll continue to match that RFC in the future.
 * Remember that for timestamping, hash algorithms with collision attacks
 * *are* secure! We've still proven that both messages existed prior to some
 * point in time - the fact that they both have the same hash digest doesn't
 * change that.
 * Heck, even md5 is still secure enough for timestamping... but that's
 * pushing our luck...
 * @extends CryptOp
 */
class OpSHA1 extends OpCrypto {

    public static byte _TAG = 0x02;

    @Override
    public String _TAG_NAME() {
        return "sha1";
    }

    @Override
    public String _HASHLIB_NAME() {
        return "SHA-1";
    }

    @Override
    public int _DIGEST_LENGTH(){ return 20;}

    OpSHA1() {
        super();
        this.arg = new byte[]{};
    }
    OpSHA1(byte[] arg_) {
        super(new byte[]{});
        this.arg = arg_;
    }

    @Override
    public byte[] call(byte[] msg) {
        return super.call(msg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpCrypto.deserializeFromTag(ctx,tag);
    }

}