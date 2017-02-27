package com.eternitywall;
/**
 * com.eternitywall.Timestamp module.
 *
 * @module com.eternitywall.Timestamp
 * @author EternityWall
 * @license LPGL3
 */

import java.util.*;
import java.util.logging.Logger;

/**
 * Class representing com.eternitywall.Timestamp interface
 * Proof that one or more attestations commit to a message.
 * The proof is in the form of a tree, with each node being a message, and the
 * edges being operations acting on those messages. The leafs of the tree are
 * attestations that attest to the time that messages in the tree existed prior.
 */
class Timestamp {


    private static Logger log = Logger.getLogger(Timestamp.class.getName());

    byte[] msg;
    List<TimeAttestation> attestations;
    HashMap<Op, Timestamp> ops;

    /**
     * Create a com.eternitywall.Timestamp object.
     * @param {string} msg - The server url.
     */
    Timestamp(byte[] msg) {
        this.msg = msg;
        this.attestations = new ArrayList<TimeAttestation>();
        this.ops = new HashMap<Op, Timestamp>();
    }

    /**
     * Deserialize a com.eternitywall.Timestamp.
     * Because the serialization format doesn't include the message that the
     * timestamp operates on, you have to provide it so that the correct
     * operation results can be calculated.
     * The message you provide is assumed to be correct; if it causes a op to
     * raise MsgValueError when the results are being calculated (done
     * immediately, not lazily) DeserializationError is raised instead.
     * @param {com.eternitywall.StreamDeserializationContext} ctx - The stream deserialization context.
     * @param {initialMsg} initialMsg - The initial message.
     * @return {com.eternitywall.Timestamp} The generated com.eternitywall.Timestamp.
     */
    public static Timestamp deserialize(StreamDeserializationContext ctx, byte[] initialMsg) {
        // console.log('deserialize: ', com.eternitywall.Utils.bytesToHex(initialMsg));
        Timestamp self = new Timestamp(initialMsg);

        byte tag = ctx.readBytes(1)[0];
        while (tag == 0xff) {
            byte current = ctx.readBytes(1)[0];
            doTagOrAttestation(self, ctx, current, initialMsg);
            tag = ctx.readBytes(1)[0];
        }
        doTagOrAttestation(self, ctx, tag, initialMsg);

        return self;
    }

    private static void doTagOrAttestation(Timestamp self, StreamDeserializationContext ctx, byte tag, byte[] initialMsg) {
        if (tag == 0x00) {
            TimeAttestation attestation = TimeAttestation.deserialize(ctx);
            self.attestations.add(attestation);
            // log.info('attestation ', attestation);
        } else {
            Op op = Op.deserializeFromTag(ctx, tag);

            byte[] result = op.call(initialMsg);
            // log.info('result: ', com.eternitywall.Utils.bytesToHex(result));

            Timestamp stamp = Timestamp.deserialize(ctx, result);
            self.ops.put(op, stamp);
        }
    }

    /**
     * Create a Serialize object.
     * @param {com.eternitywall.StreamSerializationContext} ctx - The stream serialization context.
     */
    public void serialize(StreamSerializationContext ctx) {
        // console.log('SERIALIZE');
        // console.log(ctx.toString());

        // sort
        List<TimeAttestation> sortedAttestations = this.attestations;
        if (sortedAttestations.size() > 1) {
            for (int i = 0; i < sortedAttestations.size(); i++) {
                ctx.writeBytes(new byte[]{(byte) 0xff, (byte) 0x00});
                sortedAttestations.get(i).serialize(ctx);
            }
        }
        if (this.ops.size() == 0) {
            ctx.writeByte((byte) 0x00);
            if (sortedAttestations.size() > 0) {
                sortedAttestations.get(sortedAttestations.size() - 1).serialize(ctx);
            }
        } else if (this.ops.size() > 0) {
            if (sortedAttestations.size() > 0) {
                ctx.writeBytes(new byte[]{(byte) 0xff, (byte) 0x00});
                sortedAttestations.get(sortedAttestations.size() - 1).serialize(ctx);
            }

            // all op/stamp
            int counter = 0;

            for (Map.Entry<Op, Timestamp> entry : this.ops.entrySet()) {
                Timestamp stamp = entry.getValue();
                Op op = entry.getKey();

                if (counter < this.ops.size() - 1) {
                    ctx.writeBytes(new byte[]{(byte) 0xff});
                    counter++;
                }
                op.serialize(ctx);
                stamp.serialize(ctx);

            }

        }
    }

    /**
     * Add all operations and attestations from another timestamp to this one.
     * @param {com.eternitywall.Timestamp} other - Initial other com.eternitywall.Timestamp to merge.
     */
    void merge(Timestamp other) {
        if (!(other instanceof Timestamp)) {
            log.severe("Can only merge Timestamps together");
            return;
        }
        if (!Arrays.equals(this.msg, other.msg)) {
            log.severe("Can\'t merge timestamps for different messages together");
            return;
        }

        for (final TimeAttestation attestation : other.attestations) {
            this.attestations.add(attestation);
        }

        for (Map.Entry<Op, Timestamp> entry : other.ops.entrySet()) {
            Timestamp otherOpStamp = entry.getValue();
            Op otherOp = entry.getKey();

            Timestamp ourOpStamp = this.ops.get(otherOp);
            if (ourOpStamp == null) {
                ourOpStamp = new Timestamp(otherOp.call(this.msg));
                this.ops.put(otherOp, ourOpStamp);
            }
            ourOpStamp.merge(otherOpStamp);
        }
    }

    /**
     * Print as memory hierarchical object.
     * @param {int} indent - Initial hierarchical indention.
     * @return {string} The output string.
     */
    public String toString(int indent) {
        String output = "";
        output += Timestamp.indention(indent) + "msg: " + Utils.bytesToHex(this.msg) + "\n";
        output += Timestamp.indention(indent) + this.attestations.size() + " attestations: \n";
        int i = 0;
        for (final TimeAttestation attestation : this.attestations) {
            output += Timestamp.indention(indent) + "[" + i + "] " + attestation.toString() + "\n";
            i++;
        }

        i = 0;
        output += Timestamp.indention(indent) + this.ops.size() + " ops: \n";

        for (Map.Entry<Op, Timestamp> entry : this.ops.entrySet()) {
            Timestamp stamp = entry.getValue();
            Op op = entry.getKey();

            output += Timestamp.indention(indent) + "[" + i + "] op: " + op.toString() + "\n";
            output += Timestamp.indention(indent) + "[" + i + "] timestamp: \n";
            output += stamp.toString(indent + 1);
            i++;
        }

        output += '\n';
        return output;
    }

    /**
     * Indention function for printing tree.
     * @param {int} pos - Initial hierarchical indention.
     * @return {string} The output space string.
     */
    public static String indention(int pos) {
        String output = "";
        for (int i = 0; i < pos; i++) {
            output += "    ";
        }
        return output;
    }

    /**
     * Print as tree hierarchical object.
     * @param {int} indent - Initial hierarchical indention.
     * @return {string} The output string.
     */
    public String strTree(int indent) {
        String output = "";
        if (this.attestations.size() > 0) {
            for (final TimeAttestation attestation : this.attestations) {
                output += Timestamp.indention(indent);
                output += "verify " + attestation.toString() + '\n';

            }
        }

        if (this.ops.size() > 1) {

            for (Map.Entry<Op, Timestamp> entry : this.ops.entrySet()) {
                Timestamp timestamp = entry.getValue();
                Op op = entry.getKey();
                output += Timestamp.indention(indent);
                output += " -> ";
                output += op.toString() + '\n';
                output += timestamp.strTree(indent + 1);
            }
        } else if (this.ops.size() > 0) {
            // output += com.eternitywall.Timestamp.indention(indent);
            for (Map.Entry<Op, Timestamp> entry : this.ops.entrySet()) {
                Timestamp timestamp = entry.getValue();
                Op op = entry.getKey();
                output += Timestamp.indention(indent);
                output += op.toString() + '\n';
                // output += ' ( ' + com.eternitywall.Utils.bytesToHex(this.msg) + ' ) ';
                // output += '\n';
                output += timestamp.strTree(indent);
            }
        }
        return output;
    }

    /**
     * Print as tree extended hierarchical object.
     * @param {int} indent - Initial hierarchical indention.
     * @return {string} The output string.
     */
    public static String strTreeExtended(Timestamp timestamp, int indent) {
        String output = "";

        if (timestamp.attestations.size() > 0) {
            for (final TimeAttestation attestation : timestamp.attestations) {
                output += Timestamp.indention(indent);
                output += "verify " + attestation.toString();
                output += " (" + Utils.bytesToHex(timestamp.msg) + ") ";
                // output += " ["+com.eternitywall.Utils.bytesToHex(timestamp.msg)+"] ";
                output += '\n';
            }
        }

        if (timestamp.ops.size() > 1) {

            for (Map.Entry<Op, Timestamp> entry : timestamp.ops.entrySet()) {
                Timestamp ts = entry.getValue();
                Op op = entry.getKey();
                output += Timestamp.indention(indent);
                output += " -> ";
                output += op.toString();
                output += " (" + Utils.bytesToHex(timestamp.msg) + ") ";
                output += '\n';
                output += Timestamp.strTreeExtended(ts, indent + 1);
            }
        } else if (timestamp.ops.size() > 0) {
            output += Timestamp.indention(indent);
            for (Map.Entry<Op, Timestamp> entry : timestamp.ops.entrySet()) {
                Timestamp ts = entry.getValue();
                Op op = entry.getKey();
                output += Timestamp.indention(indent);
                output += op.toString();

                output += " ( " + Utils.bytesToHex(timestamp.msg) + " ) ";
                output += '\n';
                output += Timestamp.strTreeExtended(ts, indent);
            }
        }
        return output;
    }

    /** Set of al Attestations.
     * @return {Array} Array of all sub timestamps with attestations.
     */
    public List<Timestamp> directlyVerified() {
        if (this.attestations.size() > 0) {
            List<Timestamp> list = new ArrayList<Timestamp>();
            list.add(this);
            return list;
        }
        List<Timestamp> list = new ArrayList<Timestamp>();

        for (Map.Entry<Op, Timestamp> entry : this.ops.entrySet()) {
            Timestamp ts = entry.getValue();
            Op op = entry.getKey();

            List<Timestamp> result = ts.directlyVerified();
            list.addAll(result);
        }
        return list;
    }

    /** Set of al Attestations.
     * @return {Set} Set of all timestamp attestations.
     */
    public Set<TimeAttestation> getAttestations() {
        Set set = new HashSet<TimeAttestation>();
        for (Map.Entry<byte[], TimeAttestation> item : this.allAttestations().entrySet()) {
            byte[] msg = item.getKey();
            TimeAttestation attestation = item.getValue();
            set.add(attestation);
        }
        return set;
    }

    /** Determine if timestamp is complete and can be verified.
     * @return {boolean} True if the timestamp is complete, False otherwise.
     */
    public Boolean isTimestampComplete() {
        for (Map.Entry<byte[], TimeAttestation> item : this.allAttestations().entrySet()) {
            byte[] msg = item.getKey();
            TimeAttestation attestation = item.getValue();
            if (attestation instanceof BitcoinBlockHeaderAttestation) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterate over all attestations recursively
     * @return {HashMap} Returns iterable of (msg, attestation)
     */
    public HashMap<byte[], TimeAttestation> allAttestations() {
        HashMap<byte[], TimeAttestation> map = new HashMap<byte[], TimeAttestation>();
        for (TimeAttestation attestation : this.attestations) {
            map.put(this.msg, attestation);
        }
        for (Map.Entry<Op, Timestamp> entry : this.ops.entrySet()) {
            Timestamp ts = entry.getValue();
            Op op = entry.getKey();

            HashMap<byte[], TimeAttestation> subMap = ts.allAttestations();
            for (Map.Entry<byte[], TimeAttestation> item : subMap.entrySet()) {
                byte[] msg = item.getKey();
                TimeAttestation attestation = item.getValue();
                map.put(msg, attestation);
            }
        }
        return map;
    }

}