package minicp.reversible;

import org.junit.Test;

import java.util.BitSet;
import java.util.List;

import static minicp.util.Sequence.sequence;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReversibleSparseBitSetTest {


    @Test
    public void testConstructor() {

        Trail trail = new Trail();

        Iterable<Integer> initial = sequence(0, 63);
        ReversibleSparseBitSet set = new ReversibleSparseBitSet(trail, 64, initial);
        assertEquals(1, set.numberWords());
        assertEquals(1, set.getWords().length);
    }

    @Test
    public void testConstructor2() {
        Trail trail = new Trail();

        Iterable<Integer> initial = sequence(0, 64);
        ReversibleSparseBitSet set = new ReversibleSparseBitSet(trail, 65, initial);
        assertEquals(2, set.numberWords());
        assertEquals(2, set.getWords().length);
    }

    @Test
    public void testMask() {
        Trail trail = new Trail();

        Iterable<Integer> initial = sequence(0, 63);
        ReversibleSparseBitSet set = new ReversibleSparseBitSet(trail, 64, initial);

        set.addToMask(new long[]{ 0L });
        set.intersectWithMask();

        assertEquals(0, set.getWords()[0]);
        assertTrue(set.isEmpty());
    }


    @Test
    public void testMask2() {
        Trail trail = new Trail();

        Iterable<Integer> initial = sequence(0, 63);
        ReversibleSparseBitSet set = new ReversibleSparseBitSet(trail, 64, initial);

        long word = set.getWords()[0];

        set.addToMask(new long[]{ word });
        set.intersectWithMask();

        assertEquals(word, set.getWords()[0]);
        assertFalse(set.isEmpty());
    }

    @Test
    public void testClearMask() {
        Trail trail = new Trail();

        Iterable<Integer> initial = sequence(0, 63);
        ReversibleSparseBitSet set = new ReversibleSparseBitSet(trail, 64, initial);

        long word = set.getWords()[0];

        set.addToMask(new long[]{ word });
        set.clearMask();
        set.intersectWithMask();

        assertEquals(0, set.getWords()[0]);
        assertTrue(set.isEmpty());
    }


    public void testIntersection() {
        Trail trail = new Trail();

        Iterable<Integer> initial = sequence(0, 63);
        ReversibleSparseBitSet set = new ReversibleSparseBitSet(trail, 64, initial);

        long[] bitset = new long[] { 5L };
        long[] intersection = new long[set.numberWords()];
        set.intersection(bitset, intersection);
        assertEquals(5L, intersection[0]);
    }
}
