package minicp.reversible;


import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Consumer;

class BitSetOperations {
    public static int bitLength(int size) {
        return (size + 63) >>> 6;
    }

    public static long oneBitLong(int pos) {
        return 1L << pos;
    }

    public static int bitOffset(int pos) {
        return pos >>> 6;
    }

    public static int bitPos(int pos) {
        return pos & 63;
    }

    public static void setBit(ReversibleLong[] bitSet, int pos) {
        ReversibleLong rl = bitSet[bitOffset(pos)];
        rl.setValue(rl.getValue() | oneBitLong(bitPos(pos)));
    }
}

public class ReversibleSparseBitSet {

    private ReversibleInt limit;
    private int[] indices;
    private ReversibleLong[] words;
    private long[] mask;
    private int nWords;


    public ReversibleSparseBitSet(Trail trail, int size, Iterable<Integer> initial) {
        this.nWords = BitSetOperations.bitLength(size);
        this.limit = new ReversibleInt(trail, nWords - 1);
        this.indices = new int[nWords];
        this.mask = new long[nWords];
        this.words = new ReversibleLong[nWords];
        for (int i = 0; i < nWords; ++i) {
            words[i] = new ReversibleLong(trail, 0L);
            indices[i] = i;
            mask[i] = 0;
        }


        initial.forEach((v) -> BitSetOperations.setBit(words, v));

        for (int i = limit.getValue(); i >= 0; --i) {
            if (words[indices[i]].isZero()) {
                limit.decrement();
                indices[i] = indices[limit.getValue()];
                indices[limit.getValue()] = i;
            }
        }
    }

    public boolean isEmpty() {
        return limit.getValue() == -1;
    }

    public void clearMask() {
        for (int i = 0; i <= limit.getValue(); ++i) {
            mask[indices[i]] = 0;
        }
    }

    public void reverseMask() {
        for (int i = 0; i <= limit.getValue(); ++i) {
            mask[indices[i]] = ~mask[indices[i]];
        }
    }

    public void addToMask(long[] m) {
        for (int i = 0; i <= limit.getValue(); ++i) {
            int offset = indices[i];
            mask[offset] |= m[offset];
        }
    }

    public long[] convert(BitSet m) {
        long[] words = new long[this.words.length];

        long[] mArray = m.toLongArray();

        for (int i = 0; i < mArray.length; ++i) {
            words[i] = mArray[i];
        }

        return words;
    }

    public void addToMask(BitSet m) {
        addToMask(convert(m));
    }

    public void intersectWithMask() {
        int limit = this.limit.getValue();
        for (int i = limit; i >= 0; --i) {
            int offset = indices[i];
            long w = words[offset].getValue() & mask[offset];
            words[offset].setValue(w);
            if (w == 0L) {
                indices[i] = indices[limit];
                indices[limit] = offset;
                this.limit.decrement();
            }
        }
    }

    public int intersectIndex(BitSet m) {
        return intersectIndex(convert(m));
    }

    public int intersectIndex(long[] m) {
        for (int i = 0; i <= limit.getValue(); ++i) {
            int offset = indices[i];
            if ((words[offset].getValue() & m[offset]) != 0L) {
                return offset;
            }
        }

        return -1;
    }

    public long get(int index) {
        return words[index].getValue();
    }

    @Override
    public String toString() {
        long[] words = new long[this.words.length];
        for (int i = 0; i < this.words.length; ++i) {
            words[i] = this.words[i].getValue();
        }

        return Arrays.toString(words);
    }
}