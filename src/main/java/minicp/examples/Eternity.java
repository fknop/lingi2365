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
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.examples;

import minicp.engine.constraints.TableCT;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.SearchStatistics;
import minicp.util.InconsistencyException;
import minicp.util.InputReader;

import java.util.Arrays;

import static minicp.cp.Factory.*;
import static minicp.cp.Heuristics.and;
import static minicp.cp.Heuristics.firstFail;

/**
 * https://en.wikipedia.org/wiki/Eternity_II_puzzle
 * The Eternity II puzzle is an edge-matching puzzle which involves placing nxm square puzzle pieces
 * into a n by m grid, constrained by the requirement to match adjacent edges.
 */
public class Eternity {

    public static IntVar[] flatten(IntVar [][] x) {
        return Arrays.stream(x).flatMap(Arrays::stream).toArray(IntVar[]::new);
    }

    public static int[][] generatePermutations(int[][] pieces, int i) {
        int[][] possibilities = new int[4][4];

        for (int j = 0; j < 4; ++j) {
            possibilities[j] = new int[4];
            for (int k = 0; k < pieces[k].length; ++k) {
                possibilities[j][k] = pieces[i][(j + k) % 4];
            }
        }

        return possibilities;
    }



    public static void main(String[] args) throws InconsistencyException {

        // Read the data

        InputReader reader = new InputReader("data/eternity8x8.txt");

        int n = reader.getInt();
        int m = reader.getInt();

        int [][] pieces = new int[n*m][4];
        int max_ = 0;

        for (int i = 0; i < n*m; i++) {
            for (int j = 0; j < 4; j++) {
                pieces[i][j] = reader.getInt();
                if (pieces[i][j] > max_)
                    max_ = pieces[i][j];
            }
        }
        final int max = max_;

        // ------------------------

        // Table with all pieces and for each their 4 possible rotations

        int [][] table = new int[4 * n * m][5];

        for (int j = 0; j < n * m; ++j) {
            int[][] permutations = generatePermutations(pieces, j);
            int start = j * 4;
            int end = ((j + 1) * 4) - 1;
            for (int i = start; i <= end; ++i) {
                table[i][0] = j;
                for (int k = 0; k < 4; ++k) {
                    table[i][k + 1] = permutations[i % 4][k];
                }
            }
        }

        Solver cp = makeSolver();

        // Create the variables making sure that common edges share the same instance variable)

        //   |         |
        // - +---------+- -
        //   |    u    |
        //   | l  i  r |
        //   |    d    |
        // - +---------+- -
        //   |         |


        IntVar[][] id = new IntVar[n][m]; // id variables
        IntVar[][] u = new IntVar[n][m];  // up side variables
        IntVar[][] r = new IntVar[n][m];  // right side variables
        IntVar[][] d = new IntVar[n][m];  // down side variables
        IntVar[][] l = new IntVar[n][m];  // left side variable


        for (int i = 0; i < n; i++) {
            u[i] = makeIntVarArray(cp, m, j -> makeIntVar(cp,0, max));
            id[i] = makeIntVarArray(cp, m,n*m);
        }

        for (int k = 0; k < n; k++) {
            final int i = k;
            if (i < n-1) d[i] = u[i+1];
            else d[i] = makeIntVarArray(cp,m,j -> makeIntVar(cp,0,max));
        }

        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                l[i][j] = makeIntVar(cp,0,max);
            }
        }

        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                if (j < m-1) r[i][j] = l[i][j+1];
                else r[i][j] = makeIntVar(cp,0,max);
            }
        }


        // TODO: State the constraints of the problem

        // Constraint1: all the pieces placed are different
        cp.post(allDifferent(flatten(id)));

        // Constraint2: all the pieces placed are valid ones i.e. one of the given mxn pieces possibly rotated
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                IntVar[] tuple = {id[i][j], u[i][j], r[i][j], d[i][j], l[i][j]};
                cp.post(new TableCT(tuple, table));
            }
        }

        // Constraint3: place "0" one all external side of the border (gray color)
        for (int i = 0; i < m; ++i) {
            u[0][i].assign(0);
            d[n - 1][i].assign(0);
        }

        for (int i = 0; i < n; ++i) {
            l[i][0].assign(0);
            r[i][m - 1].assign(0);
        }

        // The search using the and combinator

        SearchStatistics stats = makeDfs(cp,
                /* TODO: continue, are you branching on all the variables ? */
                and(
                    firstFail(flatten((id))),
                    firstFail(flatten(u)),
                    firstFail(flatten(l))
//                    firstFail(flatten(r)),
//                    firstFail(flatten(d))
                )
        ).onSolution(() -> {
            // Pretty Print
            for (int i = 0; i < n; i++) {
                String line = "   ";
                for (int j = 0; j < m; j++) {
                    line += u[i][j].getMin() + "   ";
                }
                System.out.println(line);
                line = " ";
                for (int j = 0; j < m; j++) {
                    line += l[i][j].getMin() + "   ";
                }
                line += r[i][m - 1].getMin();
                System.out.println(line);
            }
            String line = "   ";
            for (int j = 0; j < m; j++) {
                line += d[n - 1][j].getMin() + "   ";
            }
            System.out.println(line);

        }).onFail(() -> {
        }).start(statistics -> statistics.nSolutions == 1);

        System.out.format("#Solutions: %s\n", stats.nSolutions);
        System.out.format("Statistics: %s\n", stats);

    }


}