package minicp.reversible;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.engine.core.delta.DeltaInt;
import minicp.util.InconsistencyException;
import org.junit.Test;

import java.util.BitSet;

import static minicp.cp.Factory.makeIntVar;
import static minicp.cp.Factory.makeSolver;
import static minicp.util.Sequence.sequence;
import static org.junit.Assert.*;

public class ReversibleDeltaIntTest {


    @Test
    public void testDeltaInt() {

        Solver solver = makeSolver();
        IntVar x = makeIntVar(solver, 0, 9);
        DeltaInt delta = new ReversibleDeltaInt(solver.getTrail(), x);

        assertFalse(delta.changed());

        try {
            delta.update();
            x.remove(7);

            assertTrue(delta.changed());
            assertEquals(1, delta.deltaSize());
            assertEquals(1, delta.values().length);
            assertEquals(7, delta.values()[0]);

            delta.update();
            x.remove(5);
            x.remove(6);

            assertTrue(delta.changed());
            assertEquals(2, delta.deltaSize());
            assertEquals(2, delta.values().length);
            assertEquals(6, delta.values()[0]);
            assertEquals(5, delta.values()[1]);

            int[] values = new int[10];
            int size = delta.fillArray(values);
            assertEquals(2, size);
            assertEquals(6, values[0]);
            assertEquals(5, values[1]);


            delta.update();

            assertFalse(delta.changed());
            assertEquals(0, delta.deltaSize());


        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }
}
