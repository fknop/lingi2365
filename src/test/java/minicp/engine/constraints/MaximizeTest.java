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

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicp.cp.Factory.*;
import static minicp.search.branching.Branching.LEAF;
import static minicp.search.branching.Branching.branch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MaximizeTest {

    @Test
    public void maximizeTest() {
        try {
            try {

                Solver cp = makeSolver();
                IntVar y = makeIntVar(cp, 10,20);

                IntVar[] x = new IntVar[]{y};
                DFSearch dfs = makeDfs(cp,() -> y.isBound() ? LEAF : branch(
                        () -> {
                            System.out.println("equal");
                            equal(y, y.getMin());
                        },
                        () -> {System.out.println("not equal"); notEqual(y, y.getMin());}
                    )
                );

                cp.post(maximize(y,dfs));

                SearchStatistics stats = dfs.start();

                assertEquals(stats.nSolutions,11);


            } catch (InconsistencyException e) {
                fail("should not fail");
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }






}
