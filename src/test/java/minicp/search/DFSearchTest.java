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

package minicp.search;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.reversible.ReversibleInt;
import java.util.Arrays;

import static minicp.cp.Factory.makeSolver;

import minicp.reversible.Trail;
import minicp.util.Counter;
import minicp.util.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicp.cp.Factory.*;
import static minicp.search.branching.Branching.LEAF;
import static minicp.search.branching.Branching.branch;
import static minicp.util.InconsistencyException.INCONSISTENCY;
import static org.junit.Assert.assertEquals;


public class DFSearchTest {

    @Test
    public void testExample1() {
        Trail tr = new Trail();
        ReversibleInt i = new ReversibleInt(tr,0);
        int [] values = new int[3];

        DFSearch dfs = new DFSearch(tr,() -> {
            if (i.getValue() >= values.length)
                return LEAF;
            else return branch(
                    ()-> { // left branch
                        values[i.getValue()] = 0;
                        i.increment();
                    },
                    ()-> { // right branch
                        values[i.getValue()] = 1;
                        i.increment();
                    }
            );
        });


        SearchStatistics stats = dfs.start();

        assert(stats.nSolutions == 8);
        assert(stats.nFailures == 0);
        assert(stats.nNodes == (8+4+2));
    }

    @Test
    public void testExample2() {
        Solver cp = makeSolver();
        IntVar[] values = makeIntVarArray(cp,3,2);

        DFSearch dfs = new DFSearch(cp.getTrail(),() -> {
            int sel = -1;
            for(int i = 0 ; i < values.length;i++)
                if (values[i].getSize() > 1 && sel == -1)
                    sel = i;
            final int i = sel;
            if (i == -1)
                return LEAF;
            else return branch(()-> equal(values[i],0),
                    ()-> equal(values[i],1));
        });


        SearchStatistics stats = dfs.start();

        assert(stats.nSolutions == 8);
        assert(stats.nFailures == 0);
        assert(stats.nNodes == (8+4+2));
    }

    @Test
    public void testExample3() {
        Trail tr = new Trail();
        ReversibleInt i = new ReversibleInt(tr,0);
        int [] values = new int[3];

        DFSearch dfs = new DFSearch(tr,() -> {
            if (i.getValue() >= values.length)
                return LEAF;
            else return branch(
                    ()-> { // left branch
                        values[i.getValue()] = 1;
                        i.increment();
                    },
                    ()-> { // right branch
                        values[i.getValue()] = 0;
                        i.increment();
                    }
            );
        });


        dfs.onSolution(() -> {
            assert(Arrays.stream(values).allMatch(v -> v == 1));
        });


        SearchStatistics stats = dfs.start(stat -> stat.nSolutions >= 1);

        assert(stats.nSolutions == 1);
    }




    @Test
    public void testDFS() {
        Trail tr = new Trail();
        ReversibleInt i = new ReversibleInt(tr,0);
        boolean [] values = new boolean[4];

        Counter nSols = new Counter();


        DFSearch dfs = new DFSearch(tr,() -> {
            if (i.getValue() >= values.length)
                return LEAF;
            else return branch (
                    ()-> {
                        // left branch
                        values[i.getValue()] = false;
                        i.increment();
                    },
                    ()-> {
                        // right branch
                        values[i.getValue()] = true;
                        i.increment();
                    }
            );
        });

        dfs.onSolution(() -> {
            nSols.incr();
        });



        SearchStatistics stats = dfs.start();


        assert(nSols.getValue() == 16);
        assert(stats.nSolutions == 16);
        assert(stats.nFailures == 0);
        assert(stats.nNodes == (16+8+4+2));

    }

    @Test
    public void testDFSSearchLimit() {
        Trail tr = new Trail();
        ReversibleInt i = new ReversibleInt(tr,0);
        boolean [] values = new boolean[4];

        DFSearch dfs = new DFSearch(tr,() -> {
            if (i.getValue() >= values.length) {
                return branch(() -> {throw INCONSISTENCY;});
            }
            else return branch (
                    ()-> {
                        // left branch
                        values[i.getValue()] = false;
                        i.increment();
                    },
                    ()-> {
                        // right branch
                        values[i.getValue()] = true;
                        i.increment();
                    }
            );
        });

        Counter nFails = new Counter();
        dfs.onFail(() -> {
            nFails.incr();
        });


        // stop search after 2 solutions
        SearchStatistics stats = dfs.start(stat -> stat.nFailures >= 3);

        assert(stats.nSolutions == 0);
        assert(stats.nFailures == 3);

    }


    @Test
    public void testDeepDFS() {
        Trail tr = new Trail();
        ReversibleInt i = new ReversibleInt(tr,0);
        boolean [] values = new boolean[10000];

        DFSearch dfs = new DFSearch(tr,() -> {
            if (i.getValue() >= values.length) {
                return LEAF;
            }
            else return branch (
                    ()-> {
                        // left branch
                        values[i.getValue()] = false;
                        i.increment();
                    },
                    ()-> {
                        // right branch
                        values[i.getValue()] = true;
                        i.increment();
                    }
            );
        });
        try {
            // stop search after 1 solutions (only left most branch)
            SearchStatistics stats = dfs.start(stat -> stat.nSolutions >= 1);
            assert(stats.nSolutions == 1);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


	@Test
    public void testStopExpandWhenFailure() {
        // ensures that when some constraints fail on a given node, we do not visit corresponding child nodes
        Trail tr = new Trail();
        ReversibleInt i = new ReversibleInt(tr,0);
        boolean [] values = new boolean[2];

        DFSearch dfs = new DFSearch(tr,() -> {
            if (i.getValue() >= values.length || i.getValue() < 0) {
                return branch(() -> {throw INCONSISTENCY;});
            }
            else return branch (
                    ()-> {
                        // left branch
                        values[i.getValue()] = false;
//                        i.decrement();
                        i.setValue(-1);
                    },
                    ()-> {
                        // right branch
                        values[i.getValue()] = true;
                        i.increment();
                    }
            );
        });
        SearchStatistics stats = dfs.start(stat -> stat.nNodes >= 8);

        // if we do not search deeper when there is a failure, 7 nodes only must be visited
        assertEquals(7, stats.nNodes);
//        assert(stats.nNodes == 7);
    }



}
