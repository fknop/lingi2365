package minicp.util;

import minicp.reversible.ReversibleLong;

public class BitSetOperations {
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

    public static void setBit(long[] bitset, int pos) {
        bitset[bitOffset(pos)] = bitset[bitOffset(pos)] | oneBitLong(bitPos(pos));
    }
}