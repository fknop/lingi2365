/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.engine.constraints;

import minicp.engine.core.BoolVar;
import minicp.engine.core.Solver;
import minicp.search.SearchStatistics;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicp.cp.Factory.*;
import static minicp.cp.Heuristics.firstFail;
import static org.junit.Assert.*;


public class IsOrTest {

    @Test
    public void isOr1() {
        try {

            Solver cp = new Solver();
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsOr(b, x));

            for (BoolVar xi : x) {
                assertTrue(!xi.isBound());
            }

            cp.push();
            equal(x[1], 0);
            equal(x[2], 0);
            equal(x[3], 0);
            assertTrue(!b.isBound());
            equal(x[0], 0);
            assertTrue(b.isFalse());
            cp.pop();

            cp.push();
            equal(x[1], 0);
            equal(x[2], 1);
            assertTrue(b.isTrue());
            cp.pop();

            cp.push();
            equal(b, 1);
            equal(x[1], 0);
            equal(x[2], 0);
            assertTrue(!x[0].isBound());
            equal(x[3], 0);
            assertTrue(x[0].isTrue());
            cp.pop();


            cp.push();
            equal(b, 0);
            assertTrue(x[0].isFalse());
            assertTrue(x[1].isFalse());
            assertTrue(x[2].isFalse());
            assertTrue(x[3].isFalse());
            cp.pop();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);

        }

    }

    @Test
    public void isOr2() {
        try {

            Solver cp = new Solver();
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsOr(b, x));

            SearchStatistics stats = makeDfs(cp, firstFail(x)).onSolution(() -> {
                        int nTrue = 0;
                        for (BoolVar xi : x) {
                            if (xi.isTrue()) nTrue++;
                        }

                        assertTrue((nTrue > 0 && b.isTrue()) || (nTrue == 0 && b.isFalse()));

                    }
            ).start();
            assertEquals(16, stats.nSolutions);

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


}
