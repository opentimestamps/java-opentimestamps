package com.eternitywall.ots;

import com.eternitywall.ots.attestation.BitcoinBlockHeaderAttestation;
import com.eternitywall.ots.attestation.TimeAttestation;
import com.eternitywall.ots.op.Op;
import com.eternitywall.ots.op.OpBinary;
import com.eternitywall.ots.op.OpSHA256;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

import java.util.*;
import java.util.Map.Entry;

/**
 * Class representing a blockchain Timestamp.
 * Proof that one or more attestations commit to a message.
 * The proof is in the form of a tree, with each node being a message, and the
 * edges being operations acting on those messages. The leafs of the tree are
 * attestations that attest to the time that messages in the tree existed prior.
 *
 * @license LPGL3
 */
public class Timestamp {
    public byte[] msg;
    public List<TimeAttestation> attestations;
    public HashMap<Op, Timestamp> ops;

    public Timestamp(byte[] msg) {
        this.msg = msg;
        this.attestations = new ArrayList<>();
        this.ops = new HashMap<>();
    }

    /**
     * Deserialize a com.eternitywall.ots.Timestamp.
     *
     * @param ots        - The serialized byte array.
     * @param initialMsg - The initial message.
     * @return The generated com.eternitywall.ots.Timestamp.
     */
    public static Timestamp deserialize(byte[] ots, byte[] initialMsg) {
        StreamDeserializationContext ctx = new StreamDeserializationContext(ots);

        return Timestamp.deserialize(ctx, initialMsg);
    }

    /**
     * Deserialize a com.eternitywall.ots.Timestamp.
     * Because the serialization format doesn't include the message that the
     * timestamp operates on, you have to provide it so that the correct
     * operation results can be calculated.
     * The message you provide is assumed to be correct; if it causes a op to
     * raise MsgValueError when the results are being calculated (done
     * immediately, not lazily) DeserializationError is raised instead.
     *
     * @param ctx        - The stream deserialization context.
     * @param initialMsg - The initial message.
     * @return The deserialized com.eternitywall.ots.Timestamp.
     */
    public static Timestamp deserialize(StreamDeserializationContext ctx, byte[] initialMsg) {
        Timestamp self = new Timestamp(initialMsg);
        byte tag = ctx.readBytes(1)[0];

        while ((tag & 0xff) == 0xff) {
            byte current = ctx.readBytes(1)[0];
            doTagOrAttestation(self, ctx, current, initialMsg);
            tag = ctx.readBytes(1)[0];
        }

        doTagOrAttestation(self, ctx, tag, initialMsg);

        return self;
    }

    private static void doTagOrAttestation(Timestamp self, StreamDeserializationContext ctx, byte tag, byte[] initialMsg) {
        if ((tag & 0xff) == 0x00) {
            TimeAttestation attestation = TimeAttestation.deserialize(ctx);
            self.attestations.add(attestation);
        } else {
            Op op = Op.deserializeFromTag(ctx, tag);
            byte[] result = op.call(initialMsg);
            Timestamp stamp = Timestamp.deserialize(ctx, result);
            self.ops.put(op, stamp);
        }
    }

    /**
     * Create a Serialize object.
     *
     * @return The byte array of the serialized timestamp
     */
    public byte[] serialize() {
        StreamSerializationContext ctx = new StreamSerializationContext();
        serialize(ctx);

        return ctx.getOutput();
    }

    /**
     * Create a Serialize object.
     *
     * @param ctx - The stream serialization context.
     */
    public void serialize(StreamSerializationContext ctx) {
        // TODO: Not sure if this does what the author intended...
        List<TimeAttestation> sortedAttestations = attestations;
        Collections.sort(sortedAttestations);

        if (sortedAttestations.size() > 1) {
            for (int i = 0; i < sortedAttestations.size() - 1; i++) {
                ctx.writeBytes(new byte[]{(byte) 0xff, (byte) 0x00});
                sortedAttestations.get(i).serialize(ctx);
            }
        }

        if (ops.isEmpty()) {
            ctx.writeByte((byte) 0x00);

            if (!sortedAttestations.isEmpty()) {
                sortedAttestations.get(sortedAttestations.size() - 1).serialize(ctx);
            }
        } else {
            if (!sortedAttestations.isEmpty()) {
                ctx.writeBytes(new byte[]{(byte) 0xff, (byte) 0x00});
                sortedAttestations.get(sortedAttestations.size() - 1).serialize(ctx);
            }

            int counter = 0;
            List<Map.Entry<Op, Timestamp>> list = sortToList(ops.entrySet());

            for (Map.Entry<Op, Timestamp> entry : list) {
                Timestamp stamp = entry.getValue();
                Op op = entry.getKey();

                if (counter < ops.size() - 1) {
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
     *
     * @param other - Initial other com.eternitywall.ots.Timestamp to merge.
     * @throws Exception different timestamps messages
     */
    public void merge(Timestamp other) throws Exception {
        if (!Arrays.equals(msg, other.msg)) {
            //log.severe("Can\'t merge timestamps for different messages together");
            throw new Exception("Can\'t merge timestamps for different messages together");
        }

        attestations.addAll(other.attestations);

        for (Map.Entry<Op, Timestamp> entry : other.ops.entrySet()) {
            Timestamp otherOpStamp = entry.getValue();
            Op otherOp = entry.getKey();
            Timestamp ourOpStamp = ops.get(otherOp);

            if (ourOpStamp == null) {
                ourOpStamp = new Timestamp(otherOp.call(msg));
                ops.put(otherOp, ourOpStamp);
            }

            ourOpStamp.merge(otherOpStamp);
        }
    }

    /**
     * Shrink Timestamp.
     * Remove useless pending attestions if exist a full bitcoin attestation.
     *
     * @return TimeAttestation - the minimal attestation.
     * @throws Exception no attestion founds.
     */
    public TimeAttestation shrink() throws Exception {
        // Get all attestations
        HashMap<byte[], TimeAttestation> allAttestations = allAttestations();

        if (allAttestations.size() == 0) {
            throw new Exception("There are no attestations");
        } else if (allAttestations.size() == 1) {
            return allAttestations.values().iterator().next();
        } else if (ops.size() == 0) {
            throw new Exception();     // TODO: Need a descriptive exception string here
        }

        // Fore >1 attestations :
        // Search first BitcoinBlockHeaderAttestation
        TimeAttestation minAttestation = null;

        for (Map.Entry<Op, Timestamp> entry : ops.entrySet()) {
            Timestamp timestamp = entry.getValue();
            //Op op = entry.getKey();

            for (TimeAttestation attestation : timestamp.getAttestations()) {
                if (attestation instanceof BitcoinBlockHeaderAttestation) {
                    if (minAttestation == null) {
                        minAttestation = attestation;
                    } else {
                        if (minAttestation instanceof BitcoinBlockHeaderAttestation
                                && attestation instanceof BitcoinBlockHeaderAttestation
                                && ((BitcoinBlockHeaderAttestation) minAttestation).getHeight()
                                > ((BitcoinBlockHeaderAttestation) attestation).getHeight()) {
                            minAttestation = attestation;
                        }
                    }
                }
            }
        }

        // Only pending attestations : return the first
        if (minAttestation == null) {
            return allAttestations.values().iterator().next();
        }

        // Remove attestation if not min attestation
        boolean shrinked = false;

        for (Iterator<Entry<Op, Timestamp>> it = ops.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Op, Timestamp> entry = it.next();
            Timestamp timestamp = entry.getValue();
            Op op = entry.getKey();    // TODO: Never used...
            Set<TimeAttestation> attestations = timestamp.getAttestations();

            if (attestations.size() > 0 && attestations.contains(minAttestation) && !shrinked) {
                timestamp.shrink();
                shrinked = true;
            } else {
                it.remove();
            }
        }

        return minAttestation;

    }

    /**
     * Return the digest of the timestamp.
     *
     * @return The byte[] digest string.
     */
    public byte[] getDigest() {
        return msg;
    }

    /**
     * Print as memory hierarchical object.
     *
     * @param indent - Initial hierarchical indention.
     * @return The output string.
     */
    public String toString(int indent) {
        StringBuilder builder = new StringBuilder();
        builder.append(Timestamp.indention(indent)).append("msg: ").append(Utils.bytesToHex(msg).toLowerCase()).append("\n");
        builder.append(Timestamp.indention(indent)).append(attestations.size()).append(" attestations: \n");
        int i = 0;

        for (final TimeAttestation attestation : attestations) {
            builder.append(Timestamp.indention(indent)).append("[").append(i).append("] ").append(attestation.toString()).append("\n");
            i++;
        }

        i = 0;
        builder.append(Timestamp.indention(indent)).append(ops.size()).append(" ops: \n");

        for (Map.Entry<Op, Timestamp> entry : ops.entrySet()) {
            Timestamp stamp = entry.getValue();
            Op op = entry.getKey();

            builder.append(Timestamp.indention(indent)).append("[").append(i).append("] op: ").append(op.toString()).append("\n");
            builder.append(Timestamp.indention(indent)).append("[").append(i).append("] timestamp: \n");
            builder.append(stamp.toString(indent + 1));
            i++;
        }

        builder.append('\n');

        return builder.toString();
    }

    /**
     * Indention function for printing tree.
     *
     * @param pos - Initial hierarchical indention.
     * @return The output space string.
     */
    private static String indention(int pos) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < pos; i++) {
            builder.append("    ");
        }

        return builder.toString();
    }


    String strTree(int indent) {
        return strTree(indent, false);
    }

    private final String ANSI_HEADER = "\u001B[95m";
    private final String ANSI_OKBLUE = "\u001B[94m";
    private final String ANSI_OKGREEN = "\u001B[92m";
    private final String ANSI_WARNING = "\u001B[93m";
    private final String ANSI_FAIL = "\u001B[91m";
    private final String ANSI_ENDC = "\u001B[0m";
    private final String ANSI_BOLD = "\u001B[1m";
    private final String ANSI_UNDERLINE = "\u001B[4m";

    private String strResult(boolean verbosity, byte[] parameter, byte[] result) {
        String rr = "";       // TODO: Replace with StringBuilder

        if (verbosity && result != null) {
            rr += " == ";
            String resultHex = Utils.bytesToHex(result);

            if (parameter == null) {
                rr += resultHex;
            } else {
                String parameterHex = Utils.bytesToHex(parameter);

                try {
                    int index = resultHex.indexOf(parameterHex);
                    String parameterHexHighlight = ANSI_BOLD + parameterHex + ANSI_ENDC;

                    if (index == 0) {
                        rr += parameterHexHighlight + resultHex.substring(index + parameterHex.length(), resultHex.length());
                    } else {
                        rr += resultHex.substring(0, index) + parameterHexHighlight;
                    }
                } catch (Exception err) {
                    rr += resultHex;
                }
            }
        }

        return rr;
    }

    /**
     * Print as tree hierarchical object.
     *
     * @param indent    - Initial hierarchical indention.
     * @param verbosity - Verbose option.
     * @return The output string.
     */
    String strTree(int indent, boolean verbosity) {
        StringBuilder sb = new StringBuilder();

        if (!attestations.isEmpty()) {
            for (final TimeAttestation attestation : attestations) {
                sb.append(Timestamp.indention(indent));
                sb.append("verify ").append(attestation.toString()).append(strResult(verbosity, msg, null)).append("\n");

                if (attestation instanceof BitcoinBlockHeaderAttestation) {
                    String tx = Utils.bytesToHex(Utils.arrayReverse(msg));
                    sb.append(Timestamp.indention(indent)).append("# Bitcoin block merkle root ").append(tx.toLowerCase()).append("\n");
                }
            }
        }

        if (ops.size() > 1) {
            TreeMap<Op, Timestamp> ordered = new TreeMap<>(ops);

            for (Map.Entry<Op, Timestamp> entry : ordered.entrySet()) {
                Timestamp timestamp = entry.getValue();
                Op op = entry.getKey();

                try {
                    Transaction transaction = new Transaction(NetworkParameters.prodNet(), msg);    // TODO: Not used...
                    byte[] tx = Utils.arrayReverse(new OpSHA256().call(new OpSHA256().call(msg)));
                    sb.append(Timestamp.indention(indent)).append("# Bitcoin transaction id ").append(Utils.bytesToHex(tx).toLowerCase()).append("\n");
                } catch (Exception err) {
                    // TODO: Is this expected??
                }

                byte[] curRes = op.call(msg);
                byte[] curPar = null;

                if (op instanceof OpBinary) {
                    curPar = ((OpBinary) op).arg;
                }

                sb.append(Timestamp.indention(indent)).append(" -> ").append(op.toString().toLowerCase());
                sb.append(strResult(verbosity, curPar, curRes).toLowerCase()).append("\n");
                sb.append(timestamp.strTree(indent + 1, verbosity));
            }
        } else if (ops.size() > 0) {
            // output += com.eternitywall.ots.Timestamp.indention(indent);
            for (Map.Entry<Op, Timestamp> entry : ops.entrySet()) {
                Timestamp timestamp = entry.getValue();
                Op op = entry.getKey();

                try {
                    Transaction transaction = new Transaction(NetworkParameters.prodNet(), msg);
                    byte[] tx = Utils.arrayReverse(new OpSHA256().call(new OpSHA256().call(msg)));
                    sb.append(Timestamp.indention(indent)).append("# Bitcoin transaction id ");
                    sb.append(Utils.bytesToHex(tx).toLowerCase()).append("\n");
                } catch (Exception err) {
                    // TODO: Is this expected??
                }

                byte[] curRes = op.call(msg);
                byte[] curPar = null;

                if (op instanceof OpBinary) {
                    curPar = ((OpBinary) op).arg;
                }

                sb.append(Timestamp.indention(indent)).append(op.toString().toLowerCase());
                sb.append(strResult(verbosity, curPar, curRes).toLowerCase()).append("\n");
                sb.append(timestamp.strTree(indent, verbosity));
            }
        }

        return sb.toString();
    }

    /**
     * Set of all Attestations.
     *
     * @return Array of all sub timestamps with attestations.
     */
    List<Timestamp> directlyVerified() {
        if (!attestations.isEmpty()) {
            List<Timestamp> list = new ArrayList<>();
            list.add(this);

            return list;
        }

        List<Timestamp> list = new ArrayList<>();

        for (Map.Entry<Op, Timestamp> entry : ops.entrySet()) {
            Timestamp ts = entry.getValue();
            //Op op = entry.getKey();

            List<Timestamp> result = ts.directlyVerified();
            list.addAll(result);
        }

        return list;
    }

    /**
     * Set of all Attestations.
     *
     * @return Set of all timestamp attestations.
     */
    public Set<TimeAttestation> getAttestations() {
        Set<TimeAttestation> set = new HashSet<>();

        for (Map.Entry<byte[], TimeAttestation> item : allAttestations().entrySet()) {
            //byte[] msg = item.getKey();
            TimeAttestation attestation = item.getValue();
            set.add(attestation);
        }

        return set;
    }

    /**
     * Determine if timestamp is complete and can be verified.
     *
     * @return True if the timestamp is complete, False otherwise.
     */
    Boolean isTimestampComplete() {
        for (Map.Entry<byte[], TimeAttestation> item : allAttestations().entrySet()) {
            //byte[] msg = item.getKey();
            TimeAttestation attestation = item.getValue();

            if (attestation instanceof BitcoinBlockHeaderAttestation) {
                return true;
            }
        }

        return false;
    }

    /**
     * Iterate over all attestations recursively.
     *
     * @return Returns iterable of (msg, attestation)
     */
    public HashMap<byte[], TimeAttestation> allAttestations() {
        HashMap<byte[], TimeAttestation> map = new HashMap<>();

        for (TimeAttestation attestation: attestations) {
            map.put(msg, attestation);
        }

        for (Map.Entry<Op, Timestamp> entry: ops.entrySet()) {
            Timestamp ts = entry.getValue();
            //Op op = entry.getKey();

            HashMap<byte[], TimeAttestation> subMap = ts.allAttestations();

            for (Map.Entry<byte[], TimeAttestation> item : subMap.entrySet()) {
                byte[] msg = item.getKey();
                TimeAttestation attestation = item.getValue();
                map.put(msg, attestation);
            }
        }

        return map;
    }

    /**
     * Iterate over all tips recursively.
     *
     * @return Returns iterable of (msg, attestation)
     */
    public Set<byte[]> allTips() {
        Set<byte[]> set = new HashSet<>();

        if (ops.size() == 0) {
            set.add(msg);
        }

        for (Map.Entry<Op, Timestamp> entry : ops.entrySet()) {
            Timestamp ts = entry.getValue();
            //Op op = entry.getKey();

            set.addAll(ts.allTips());
        }

        return set;
    }

    /**
     * Compare timestamps.
     *
     * @param other the timestamp to compare with
     * @return Returns true if the timestamps are equals
     */
    public boolean equals(Timestamp other) {
        if (!Arrays.equals(getDigest(), other.getDigest())) {
            return false;
        }

        // Check attestations
        if (attestations.size() != other.attestations.size()) {
            return false;
        }

        for (int i = 0; i < attestations.size(); i++) {
            TimeAttestation ta1 = attestations.get(i);
            TimeAttestation ta2 = other.attestations.get(i);

            if (!ta1.equals(ta2)) {
                return false;
            }
        }

        // Check operations
        if (ops.size() != other.ops.size()) {
            return false;
        }

        // Order list of operations
        List<Map.Entry<Op, Timestamp>> list1 = sortToList(ops.entrySet());
        List<Map.Entry<Op, Timestamp>> list2 = sortToList(other.ops.entrySet());

        for (int i = 0; i < list1.size(); i++) {
            Op op1 = list1.get(i).getKey();
            Op op2 = list2.get(i).getKey();

            if (!op1.equals(op2)) {
                return false;
            }

            Timestamp t1 = list1.get(i).getValue();
            Timestamp t2 = list2.get(i).getValue();

            if (!t1.equals(t2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Add Op to current timestamp and return the sub stamp.
     *
     * @param op - The operation to insert
     * @return Returns the sub timestamp
     */
    public Timestamp add(Op op) {
        // nonce_appended_stamp = timestamp.ops.add(com.eternitywall.ots.op.OpAppend(os.urandom(16)))
        // Op opAppend = new OpAppend(bytes);

        if (ops.containsKey(op)) {
            return ops.get(op);
        }

        Timestamp stamp = new Timestamp(op.call(msg));
        ops.put(op, stamp);

        return stamp;
    }


    /**
     * Retrieve.
     *
     * @param setEntries - The entries set of ops hashmap
     * @return Returns the sorted list of map entries
     */
    private List<Map.Entry<Op, Timestamp>> sortToList(Set<Entry<Op, Timestamp>> setEntries) {
        List<Map.Entry<Op, Timestamp>> entries = new ArrayList<>(setEntries);

        Collections.sort(entries, new Comparator<Map.Entry<Op, Timestamp>>() {
            @Override
            public int compare(Entry<Op, Timestamp> a, Entry<Op, Timestamp> b) {
                return a.getKey().compareTo(b.getKey());
            }
        });

        return entries;
    }
}
