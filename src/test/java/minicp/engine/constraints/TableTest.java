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

import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.SearchStatistics;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static minicp.cp.Factory.*;
import static minicp.cp.Heuristics.firstFail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableTest {

    private static List<BiFunction<IntVar[], int[][], Constraint>> getAlgos() {
        List<BiFunction<IntVar[], int[][], Constraint>> algos = new ArrayList<>();
        algos.add(TableDecomp::new);
        algos.add(TableCT::new);
        return algos;
    }

    private int[][] randomTuples(Random rand, int arity, int nTuples, int minvalue, int maxvalue) {
        int[][] r = new int[nTuples][arity];
        for (int i = 0; i < nTuples; i++)
            for (int j = 0; j < arity; j++)
                r[i][j] = rand.nextInt(maxvalue - minvalue) + minvalue;
        return r;
    }

    @Test
    public void randomTest() {
        Random rand = new Random(67292);

        for (int i = 0; i < 1000; i++) {
            int[][] tuples1 = randomTuples(rand, 3, 100, 2, 8);
            int[][] tuples2 = randomTuples(rand, 3, 100, 1, 7);
            int[][] tuples3 = randomTuples(rand, 3, 100, 0, 6);

            for (BiFunction<IntVar[], int[][], Constraint> algo : getAlgos()) {
                try {
                    testTable(algo, tuples1, tuples2, tuples3);
                } catch (NotImplementedException e) {
                    // pass
                }
            }
        }
    }

    public void testTable(BiFunction<IntVar[], int[][], Constraint> tc, int[][] t1, int[][] t2, int[][] t3) {

        SearchStatistics statsDecomp;
        SearchStatistics statsAlgo;

        try {
            Solver cp = makeSolver();
            IntVar[] x = makeIntVarArray(cp, 5, 9);
            cp.post(allDifferent(x));
            cp.post(new TableDecomp(new IntVar[]{x[0], x[1], x[2]}, t1));
            cp.post(new TableDecomp(new IntVar[]{x[2], x[3], x[4]}, t2));
            cp.post(new TableDecomp(new IntVar[]{x[0], x[2], x[4]}, t3));
            statsDecomp = makeDfs(cp, firstFail(x)).start();
        } catch (InconsistencyException e) {
            statsDecomp = null;
        }

        try {
            Solver cp = makeSolver();
            IntVar[] x = makeIntVarArray(cp, 5, 9);
            cp.post(allDifferent(x));
            cp.post(tc.apply(new IntVar[]{x[0], x[1], x[2]}, t1));
            cp.post(tc.apply(new IntVar[]{x[2], x[3], x[4]}, t2));
            cp.post(tc.apply(new IntVar[]{x[0], x[2], x[4]}, t3));
            statsAlgo = makeDfs(cp, firstFail(x)).start();
        } catch (InconsistencyException e) {
            statsAlgo = null;
        }

        assertTrue((statsDecomp == null && statsAlgo == null) || (statsDecomp != null && statsAlgo != null));
        if (statsDecomp != null) {
            assertEquals(statsDecomp.nSolutions, statsAlgo.nSolutions);
            assertEquals(statsDecomp.nFailures, statsAlgo.nFailures);
            assertEquals(statsDecomp.nNodes, statsAlgo.nNodes);
        }
    }
}