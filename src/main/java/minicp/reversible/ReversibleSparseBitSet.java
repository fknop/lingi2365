package minicp.reversible;


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
    private int nWords;


    public ReversibleSparseBitSet(Trail trail, int size, Iterable<Integer> initial) {
        this.nWords = BitSetOperations.bitLength(size);

        this.limit = new ReversibleInt(trail, nWords);
        this.indices = new int[nWords];
        words = new ReversibleLong[nWords];
        for (int i = 0; i < nWords; ++i) {
            words[i] = new ReversibleLong(trail, 0L);
            indices[i] = i;
        }


        initial.forEach((v) -> BitSetOperations.setBit(words, v));

        for (int i = limit.getValue(); i > 0; --i) {
            if (words[indices[i]].isZero()) {
                limit.decrement();
                indices[i] = indices[limit.getValue()];
                indices[limit.getValue()] = i;
            }
        }
    }

}
