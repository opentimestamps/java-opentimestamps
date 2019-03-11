package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Pending attestations.
 * Commitment has been recorded in a remote calendar for future attestation,
 * and we have a URI to find a more complete timestamp in the future.
 * Nothing other than the URI is recorded, nor is there provision made to add
 * extra metadata (other than the URI) in future upgrades. The rational here
 * is that remote calendars promise to keep commitments indefinitely, so from
 * the moment they are created it should be possible to find the commitment in
 * the calendar. Thus if you're not satisfied with the local verifiability of
 * a timestamp, the correct thing to do is just ask the remote calendar if
 * additional attestations are available and/or when they'll be available.
 * While we could additional metadata like what types of attestations the
 * remote calendar expects to be able to provide in the future, that metadata
 * can easily change in the future too. Given that we don't expect timestamps
 * to normally have more than a small number of remote calendar attestations,
 * it'd be better to have verifiers get the most recent status of such
 * information (possibly with appropriate negative response caching).
 *
 * @see com.eternitywall.ots.attestation.TimeAttestation
 */
public class PendingAttestation extends TimeAttestation {

    private static Logger log = Utils.getLogger(PendingAttestation.class.getName());

    public static byte[] _TAG = {(byte) 0x83, (byte) 0xdf, (byte) 0xe3, (byte) 0x0d, (byte) 0x2e, (byte) 0xf9, (byte) 0x0c, (byte) 0x8e};
    public static int _MAX_URI_LENGTH = 1000;
    public static String _ALLOWED_URI_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._/:";

    @Override
    public byte[] _TAG() {
        return PendingAttestation._TAG;
    }

    private byte[] uri;

    public byte[] getUri() {
        return uri;
    }

    public PendingAttestation(byte[] uri) {
        super();
        this.uri = uri;
    }

    public static boolean checkUri(byte[] uri) {
        if (uri.length > PendingAttestation._MAX_URI_LENGTH) {
            System.err.print("URI exceeds maximum length");
            return false;
        }

        for (byte b : uri) {
            Character c = String.format("%c", b).charAt(0);
            if (PendingAttestation._ALLOWED_URI_CHARS.indexOf(c) < 0) {
                log.severe("URI contains invalid character ");
                return false;
            }
        }

        return true;
    }

    public static PendingAttestation deserialize(StreamDeserializationContext ctxPayload) {
        byte[] utf8Uri = ctxPayload.readVarbytes(PendingAttestation._MAX_URI_LENGTH);

        if (!PendingAttestation.checkUri(utf8Uri)) {
            log.severe("Invalid URI: ");
            return null;
        }

        return new PendingAttestation(utf8Uri);
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeVarbytes(uri);
    }

    public String toString() {
        return "PendingAttestation(\'" + new String(uri, StandardCharsets.UTF_8) + "\')";
    }

    @Override
    public int compareTo(TimeAttestation other) {
        PendingAttestation otherAttestation = (PendingAttestation) other;

        return Utils.compare(uri, otherAttestation.uri);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PendingAttestation)) {
            return false;
        }

        PendingAttestation otherAttestation = (PendingAttestation) other;

        if (!Arrays.equals(_TAG(), otherAttestation._TAG())) {
            return false;
        }

        return Arrays.equals(uri, otherAttestation.uri);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_TAG()) ^ Arrays.hashCode(uri);
    }
}
