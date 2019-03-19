package com.eternitywall;

import com.eternitywall.ots.Merkle;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Timestamp;
import static com.eternitywall.ots.Utils.hexToBytes;
import com.eternitywall.ots.attestation.PendingAttestation;
import com.eternitywall.ots.op.Op;
import com.eternitywall.ots.op.OpAppend;
import com.eternitywall.ots.op.OpSHA256;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.*;

public class TestTimestamp {

    @Test
    public void testAddingOperationsToTimestamps() {
        Timestamp t = new Timestamp(toBytes("abcd", "UTF-8"));

        OpAppend opAppend = new OpAppend(toBytes("efgh", "UTF-8"));
        t.add(opAppend);

        // The second add should succeed with the timestamp unchanged
        t.add(opAppend);
        Timestamp tComplete = new Timestamp(toBytes("abcdefgh", "UTF-8"));
        assertTrue(t.ops.get(opAppend).equals(tComplete));
    }

    @Test
    public void testSettingAnOpResultTimestamp() {
        Timestamp t1 = new Timestamp(toBytes("foo", "UTF-8"));
        OpAppend opAppend1 = new OpAppend(toBytes("bar", "UTF-8"));
        OpAppend opAppend2 = new OpAppend(toBytes("baz", "UTF-8"));
        Timestamp t2 = t1.add(opAppend1);
        Timestamp t3 = t2.add(opAppend2);
        assertArrayEquals(t1.ops.get(opAppend1).ops.get(opAppend2).msg, toBytes("foobarbaz", "UTF-8"));

        t1.ops.put(opAppend1, new Timestamp(toBytes("foobar", "UTF-8")));

        for (Map.Entry<Op, Timestamp> entry : t1.ops.get(opAppend1).ops.entrySet()) {
            Timestamp timestamp = entry.getValue();
            Op op = entry.getKey();
            assertNotEquals(op, opAppend2);
        }
    }

    private void tSerialize(Timestamp expectedInstance, byte[] expectedSerialized) {
        StreamSerializationContext ssc = new StreamSerializationContext();
        expectedInstance.serialize(ssc);
        byte[] actualSerialized = ssc.getOutput();

        assertArrayEquals(expectedSerialized, actualSerialized);

        StreamDeserializationContext sdc = new StreamDeserializationContext(expectedSerialized);
        Timestamp actualInstance = Timestamp.deserialize(sdc, expectedInstance.msg);
        assertTrue(expectedInstance.equals(actualInstance));
    }

    @Test
    public void testTimestampSerializationDeserialization() throws IOException {
        Timestamp stamp = new Timestamp(toBytes("foo", "UTF-8"));
        stamp.attestations.add(new PendingAttestation(toBytes("foobar", "UTF-8")));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobar", "UTF-8"));
        tSerialize(stamp, baos.toByteArray());

        stamp.attestations.add(new PendingAttestation(toBytes("barfoo", "UTF-8")));

        baos = new ByteArrayOutputStream();
        baos.write(0xff);
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("barfoo", "UTF-8"));
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobar", "UTF-8"));
        tSerialize(stamp, baos.toByteArray());

        stamp.attestations.add(new PendingAttestation(toBytes("foobaz", "UTF-8")));

        baos = new ByteArrayOutputStream();
        baos.write(0xff);
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("barfoo", "UTF-8"));
        baos.write(0xff);
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobar", "UTF-8"));
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobaz", "UTF-8"));
        tSerialize(stamp, baos.toByteArray());

        //Timestamp sha256Stamp = stamp.ops.put(new OpSHA256(), null);
        // Should fail - empty timestamps can't be serialized
        //StreamSerializationContext ssc = new StreamSerializationContext();
        //stamp.serialize(ssc);
        OpSHA256 opSHA256 = new OpSHA256();
        Timestamp sha256Stamp = stamp.add(opSHA256);
        // TODO: check testTimestampSerializationDeserialization

        PendingAttestation pendingAttestation = new PendingAttestation(toBytes("deeper", "UTF-8"));
        sha256Stamp.attestations.add(pendingAttestation);

        baos = new ByteArrayOutputStream();
        baos.write(0xff);
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("barfoo", "UTF-8"));
        baos.write(0xff);
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobar", "UTF-8"));
        baos.write(0xff);
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("foobaz", "UTF-8"));
        baos.write(0x08);
        baos.write(0x00);
        baos.write(hexToBytes("83dfe30d2ef90c8e" + "07" + "06"));
        baos.write(toBytes("deeper", "UTF-8"));
        tSerialize(stamp, baos.toByteArray());
    }

    @Test
    public void testMergingTimestamps() {
        Timestamp stampA = new Timestamp(toBytes("a", "UTF-8"));
        Timestamp stampB = new Timestamp(toBytes("b", "UTF-8"));

        try {
            stampA.merge(stampB);
        } catch (Exception e) {
            // Expected
        }

        Timestamp stamp1 = new Timestamp(toBytes("a", "UTF-8"));
        Timestamp stamp2 = new Timestamp(toBytes("a", "UTF-8"));
        stamp2.attestations.add(new PendingAttestation(toBytes("foobar", "UTF-8")));

        try {
            stamp1.merge(stamp2);
            assertTrue(stamp1.equals(stamp2));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testMakeMerkleTree() {
        defTimestamp(2, hexToBytes("b413f47d13ee2fe6c845b2ee141af81de858df4ec549a58b7970bb96645bc8d2"));
        defTimestamp(3, hexToBytes("e6aa639123d8aac95d13d365ec3779dade4b49c083a8fed97d7bfc0d89bb6a5e"));
        defTimestamp(4, hexToBytes("7699a4fdd6b8b6908a344f73b8f05c8e1400f7253f544602c442ff5c65504b24"));
        defTimestamp(5, hexToBytes("aaa9609d0c949fee22c1c941a4432f32dc1c2de939e4af25207f0dc62df0dbd8"));
        defTimestamp(6, hexToBytes("ebdb4245f648b7e77b60f4f8a99a6d0529d1d372f98f35478b3284f16da93c06"));
        defTimestamp(7, hexToBytes("ba4603a311279dea32e8958bfb660c86237157bf79e6bfee857803e811d91b8f"));
    }

    @Test
    public void testCatSha256() {
        Timestamp left = new Timestamp(toBytes("foo", "UTF-8"));
        Timestamp right = new Timestamp(toBytes("bar", "UTF-8"));
        Timestamp stampLeftRight = Merkle.catSha256(left, right);
        assertArrayEquals(stampLeftRight.getDigest(), hexToBytes("c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2"));

        Timestamp righter = new Timestamp(toBytes("baz", "UTF-8"));
        Timestamp stampRighter = Merkle.catSha256(stampLeftRight, righter);
        assertArrayEquals(stampRighter.getDigest(), hexToBytes("23388b16c66f1fa37ef14af8eb081712d570813e2afb8c8ae86efa726f3b7276"));
    }

    private void defTimestamp(int n, byte[] expectedMerkleRoot) {
        List<Timestamp> roots = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            byte[] bytes = {(byte) i};
            roots.add(new Timestamp(bytes));
        }

        Timestamp merkleTip = Merkle.makeMerkleTree(roots);
        assertArrayEquals(merkleTip.getDigest(), expectedMerkleRoot);

        for (Timestamp root : roots) {
            Set<byte[]> tips = root.allTips();

            for (byte[] tip : tips) {
                assertArrayEquals(tip, merkleTip.getDigest());
            }
        }
    }
}
