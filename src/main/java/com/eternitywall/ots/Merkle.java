package com.eternitywall.ots;

import com.eternitywall.ots.op.OpAppend;
import com.eternitywall.ots.op.OpPrepend;
import com.eternitywall.ots.op.OpSHA256;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import static jdk.nashorn.internal.objects.Global.undefined;

public class Merkle {
    /** Concatenate left and right, then perform a unary operation on them left and right can be either timestamps or bytes.
     * Appropriate intermediary append/prepend operations will be created as needed for left and right.
     * */

    public static Timestamp catThenUnaryOp( Timestamp left, Timestamp right) {

        // rightPrependStamp = right.ops.add(OpPrepend(left.msg))
        OpPrepend opPrepend = new OpPrepend(left.msg);
        Timestamp rightPrependStamp = right.ops.get(opPrepend);
        if (rightPrependStamp == null) {
            rightPrependStamp = new Timestamp(opPrepend.call(right.msg));
            right.ops.put(opPrepend, rightPrependStamp);
        }

        // Left and right should produce the same thing, so we can set the timestamp of the left to the right.
        // left.ops[OpAppend(right.msg)] = right_prepend_stamp
        // leftAppendStamp = left.ops.add(OpAppend(right.msg))
        OpAppend opAppend = new OpAppend(right.msg);
        Timestamp leftPrependStamp = left.ops.get(opAppend);
        if (leftPrependStamp == null) {
            leftPrependStamp = new Timestamp(opAppend.call(left.msg));
            left.ops.put(opAppend, leftPrependStamp);
        }
        left.ops.put(opAppend, rightPrependStamp);

        // return rightPrependStamp.ops.add(unaryOpCls())
        OpSHA256 opUnary = new OpSHA256();
        Timestamp res = rightPrependStamp.ops.get(opUnary);
        if (res == null) {
            res = new Timestamp(opUnary.call(rightPrependStamp.msg));
            rightPrependStamp.ops.put(opUnary, res);
        }

        return res;
    }

    static Timestamp catSha256(Timestamp left, Timestamp right) {
        return Merkle.catThenUnaryOp( left, right);
    }

    static Timestamp catSha256d(Timestamp left, Timestamp right) {
        Timestamp sha256Timestamp = Merkle.catSha256(left, right);
        // res = sha256Timestamp.ops.add(OpSHA256());
        OpSHA256 opSHA256 = new OpSHA256();
        Timestamp res = sha256Timestamp.ops.get(opSHA256);
        if (res == null) {
            res = new Timestamp(opSHA256.call(sha256Timestamp.msg));
            sha256Timestamp.ops.put(opSHA256, res);
        }
        return res;
    }

    /** Merkelize a set of timestamps
     * A merkle tree of all the timestamps is built in-place using binop() to
     timestamp each pair of timestamps. The exact algorithm used is structurally
     identical to a merkle-mountain-range, although leaf sums aren't committed.
     As this function is under the consensus-critical core, it's guaranteed that
     the algorithm will not be changed in the future.
     Returns the timestamp for the tip of the tree.
     */
    public static Timestamp makeMerkleTree(List<Timestamp> timestamps) {
        List<Timestamp> stamps = timestamps;
        Timestamp prevStamp = null;
        boolean exit = false;

        while (!exit) {
            if(stamps.size()>0) {
                prevStamp = stamps.get(0);
            }

            List<Timestamp> subStamps = stamps.subList(1, stamps.size());
            List<Timestamp> nextStamps = new ArrayList<>();
            for (Timestamp stamp : subStamps) {
                if (prevStamp == null) {
                    prevStamp = stamp;
                } else {
                    nextStamps.add(Merkle.catSha256(prevStamp, stamp));
                    prevStamp = null;
                }
            }

            if (nextStamps.size() == 0) {
                exit = true;
            } else {
                if (prevStamp != null) {
                    nextStamps.add(prevStamp);
                }
                stamps = nextStamps;
            }
        }
        return prevStamp;
    }

}
