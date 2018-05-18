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

import minicp.engine.constraints.Cumulative;
import minicp.engine.constraints.Element1D;
import minicp.engine.constraints.LessOrEqual;
import minicp.engine.constraints.Minimize;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.search.branching.AbstractBranching;
import minicp.search.branching.Branching;
import minicp.search.branching.FirstFailBranching;
import minicp.search.selector.value.IBS;
import minicp.search.selector.value.MinValue;
import minicp.search.selector.value.ValueSelector;
import minicp.search.selector.variable.ConflictOrderingSearch;
import minicp.search.selector.variable.DomDivDegree;
import minicp.search.selector.variable.VariableSelector;
import minicp.util.Box;
import minicp.util.InconsistencyException;
import minicp.util.InputReader;
import minicp.util.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;

import static minicp.cp.Factory.*;
import static minicp.cp.Heuristics.firstFail;

/**
 * Resource Constrained Project Scheduling Problem (RCPSP)
 * http://www.om-db.wi.tum.de/psplib/library.html
 */
public class RCPSP {



    public static void main(String[] args) throws InconsistencyException {
         class StartSnapshot implements Comparable<StartSnapshot> {
            int start;
            int i;
             StartSnapshot(int start, int i) {
                this.start = start;
                this.i = i;
            }

            @Override
            public int compareTo(StartSnapshot s) {
                return Integer.compare(this.start, s.start);
            }

             @Override
             public String toString() {
                 return "Activity: " + i + " starts at: " + start;
             }
         }

        // Reading the data


        String folder = "data/rcpsp/";
        String instance = null;
        String file = null;
        long timeout = 0;
        if (args.length > 0) {
            instance = args[0];
            file = instance;
            timeout = 120;
        }
        else {
            instance = "j30_1_1.rcp";
    //        instance = "j30_1_2.rcp";
    //        instance = "j30_1_3.rcp";
    //        instance = "j60_1_1.rcp";
    //        instance = "j60_1_2.rcp";
    //        instance = "j60_1_3.rcp";
    //        instance = "j90_1_1.rcp";
    //        instance = "j90_1_2.rcp";
    //        instance = "j90_1_3.rcp";
    //        instance = "j120_1_1.rcp";
    //        instance = "j120_1_2.rcp";
//            instance = "j120_1_3.rcp";

            file = folder + instance;
        }


        InputReader reader = new InputReader(file);

        int nActivities = reader.getInt();
        int nResources = reader.getInt();

        int[] capa = new int[nResources];
        for (int i = 0; i < nResources; i++) {
            capa[i] = reader.getInt();
        }

        int[] duration = new int[nActivities];
        int[][] consumption = new int[nResources][nActivities];
        int[][] successors = new int[nActivities][];


        int H = 0;
        for (int i = 0; i < nActivities; i++) {
            // durations, demand for each resource, successors
            duration[i] = reader.getInt();
            H += duration[i];
            for (int r = 0; r < nResources; r++) {
                consumption[r][i] = reader.getInt();
            }
            successors[i] = new int[reader.getInt()];
            for (int k = 0; k < successors[i].length; k++) {
                successors[i][k] = reader.getInt() - 1;
            }
        }


        // -------------------------------------------

        // The Model

        Solver cp = makeSolver();

        IntVar[] start = makeIntVarArray(cp, nActivities, H);
        IntVar[] end = new IntVar[nActivities];


        for (int i = 0; i < nActivities; i++) {
            end[i] = plus(start[i], duration[i]);
        }

        // TODO 1: add the cumulative constraint to model the resource
        // capa[r] is the capacity of resource r
        // consumption[r] is the consumption for each activity on the resource [r]
        // duration is the duration of each activity
        for (int i = 0; i < nResources; ++i) {
            cp.post(new Cumulative(start, duration, consumption[i], capa[i]));
        }



        for (int i = 0; i < nActivities; ++i) {
            for (int j = 0; j < successors[i].length; ++j) {
                int k = successors[i][j];
                cp.post(lessOrEqual(end[i], start[k]));
            }
        }


        IntVar makespan = maximum(end);

        FirstFailBranching branching = new FirstFailBranching(start);
        VariableSelector<IntVar> varSelector = new ConflictOrderingSearch(start, branching, new DomDivDegree());
//        ValueSelector valSelector = new IBS(makespan, start);

        branching.setVariableSelector(varSelector);
        branching.setValueSelector(new MinValue());

        DFSearch search = makeDfs(cp, branching.branch());

        // TODO 3: minimize the makespan
        cp.post(minimize(makespan, search));



        // TODO 4: implement the search


//        StartSnapshot[] startSnapshots = new StartSnapshot[start.length];
//        int[] activityIndices = new int[start.length];
//        ArrayList<IntArrayList> graph = new ArrayList<>();

//        for (int i = 0; i < start.length; ++i) {
//            startSnapshots[i] = new StartSnapshot(0, 0);
//            graph.add(new IntArrayList(4));
//        }

//        Box<Integer> last = new Box<>(0);

        int[] bests = new int[start.length];
        Box<Integer> best = new Box<>(null);

        search.onSolution(() -> {
            best.set(makespan.getMin());
            for (int i = 0; i < start.length; ++i) {
                bests[i] = start[i].getMin();
            }
//            int index = -1;
//            int lastValue = -1;


//            for (int i = 0; i < start.length; ++i) {
//                startSnapshots[i].i = i;
//                startSnapshots[i].start = start[i].getMin();
//                graph.get(i).clear();
//            }
//
//            Arrays.sort(startSnapshots);
//
//            for (int i = 0; i < start.length; ++i) {
//
//                if (lastValue != startSnapshots[i].start) {
//                    index++;
//                    last.set(index);
//                }

//                graph.get(index).add(startSnapshots[i].i);
//                lastValue = startSnapshots[i].start;
//
//
//                int j = startSnapshots[i].i;
//                activityIndices[j] = index;
//            }

//            System.out.println(Arrays.toString(startSnapshots));
//            System.out.println(Arrays.toString(activityIndices));

//           for (int i = 0; i < last.get(); ++i) {
//               for (int j = 0; j < graph.get(i).size(); ++j) {
//                   System.out.print(graph.get(i).get(j) + " ");
//               }
//               System.out.print(" | ");
//           }
//            System.out.println();
            // TODO: build tree


        });

        final long current = System.currentTimeMillis();
        // -2 to make sure it ends before 120
        final long t = timeout == 0L ? 0L : (timeout - 2) * 1000;
        search.start((stats) -> {
            if (t == 0L) {
                return false;
            }
            else {
                return System.currentTimeMillis() - current >= t;
            }
        });

        System.out.println(best.get());
        for (int i = 0; i < bests.length; ++i) {
            System.out.print(bests[i] + " ");
        }
        System.out.println();

    }

    boolean isSuccessor(int[][] successors, int i, int j) {
        for (int k = 0; k < successors[i].length; ++k) {
            if (successors[i][k] == j) {
                return true;
            }
        }

        return false;
    }
}