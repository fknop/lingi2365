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

        int limit = this.limit.getValue();
        for (int i = limit; i >= 0; --i) {
            if (words[indices[i]].isZero()) {
                indices[i] = indices[this.limit.getValue()];
                indices[this.limit.getValue()] = i;
                this.limit.decrement();
            }
        }
    }

    public int numberWords() {
        return nWords;
    }

    public long[] getWords() {
        long[] words = new long[this.words.length];
        for (int i = 0; i < words.length; ++i) {
            words[i] = this.words[i].getValue();
        }

        return words;
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
            mask[offset] = mask[offset] | m[offset];
        }
    }

    public void convert(BitSet m, long[] words) {
        assert(words.length == nWords);
        long[] mArray = m.toLongArray();
        for (int i = 0; i < words.length; ++i) {
            if (i >= mArray.length) {
                words[i] = 0L;
            }
            else {
                words[i] = mArray[i];
            }
        }
    }

    public void addToMask(BitSet m) {
        long[] words = new long[this.words.length];
        convert(m, words);
        addToMask(words);
    }

    public void intersectWithMask() {

        for (int i = limit.getValue(); i >= 0; i--) {
            int offset = indices[i];
            long w = words[offset].getValue() & mask[offset];
            words[offset].setValue(w);
            if (w == 0L) {
                indices[i] = indices[limit.getValue()];
                indices[limit.getValue()] = offset;
                limit.decrement();
            }
        }
    }


    public int intersectIndex(long[] m) {
        for (int i = limit.getValue(); i >= 0; --i) {
            int offset = indices[i];
            if ((words[offset].getValue() & m[offset]) != 0L) {
                return offset;
            }
        }

        return -1;
    }

    public boolean emptyIntersection(int index, long word) {
        return (words[index].getValue() & word) == 0L;
    }

    public void intersection(long[] bs, long[] intersection) {
        for (int i = 0; i < nWords; ++i) {
            intersection[i] = this.words[i].getValue() & bs[i];
        }
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
