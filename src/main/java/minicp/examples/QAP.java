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

package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.InconsistencyException;
import minicp.util.InputReader;

import static minicp.cp.Factory.*;
import static minicp.cp.Heuristics.firstFail;
import static minicp.search.Selector.branch;
import static minicp.search.Selector.selectMin;

public class QAP {

    public static void main(String[] args) throws InconsistencyException {

        // ---- read the instance -----

        // 9552
        InputReader reader = new InputReader("data/chr25a.txt");

        int n = reader.getInt();
        // Weights
        int [][] w = new int[n][n];
        for (int i = 0; i < n ; i++) {
            for (int j = 0; j < n; j++) {
                 w[i][j] = reader.getInt();
            }
        }
        // Distance
        int [][] d = new int[n][n];
        for (int i = 0; i < n ; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = reader.getInt();
            }
        }

        // ----- build the model ---

        Solver cp = makeSolver();
        IntVar[] x = makeIntVarArray(cp, n, n);

        cp.post(allDifferent(x));

//        DFSearch dfs = makeDfs(cp,firstFail(x));
        DFSearch dfs = makeDfs(cp,
                selectMin(x,
                        xi -> xi.getSize() > 1, // filter,
                        xi -> {
                            int max = Integer.MIN_VALUE;

                            int[] values = new int[xi.getSize()];
                            int[] valuesj = new int[n];
                            int size = xi.fillArray(values);

                            for (int i = 0; i < size; ++i) {
                                int valuei = values[i];
                                for (int j = 0; j < n; ++j) {
                                    int sizej = x[j].fillArray(valuesj);
                                    for (int k = 0; k < sizej; ++k) {
                                        int valuej = valuesj[k];
                                        int weight = w[valuei][valuej];
                                        if (weight > max) {
                                            max = weight;
                                        }
                                    }
                                }
                            }

                            return -max;
                        },
                        xi -> {
                            int min = Integer.MAX_VALUE;

                            int[] values = new int[xi.getSize()];
                            int[] valuesj = new int[n];

                            int size = xi.fillArray(values);

                            int bestLocation = xi.getMin(); // fallback

                            for (int i = 0; i < size; ++i) {
                                int location = values[i];
                                for (int j = 0; j < n; ++j) {
                                    int sizej = x[j].fillArray(valuesj);
                                    for (int k = 0; k < sizej; ++k) {
                                        int valuej = valuesj[k];
                                        int distance = d[location][valuej];
                                        if (distance < min) {
                                            min = distance;
                                        }
                                    }
                                }
                            }

                            return branch(
                                    () -> equal(xi, bestLocation),
                                    () -> notEqual(xi, bestLocation)
                            );
                        }
                )
        );
        // build the objective function
        IntVar[] weightedDist = new IntVar[n*n];
        int ind = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weightedDist[ind] = mul(element(d,x[i],x[j]),w[i][j]);
                ind++;
            }
        }
        IntVar objective = sum(weightedDist);
        cp.post(minimize(objective,dfs));



        dfs.onSolution(() -> System.out.println("objective:"+objective.getMin()));

        SearchStatistics stats = dfs.start();

        System.out.println(stats);

    }
}