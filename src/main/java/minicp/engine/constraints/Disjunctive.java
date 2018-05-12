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
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.util.InconsistencyException;
import minicp.util.IntVarPair;
import minicp.util.SortUtils;

import java.util.Arrays;
import java.util.Comparator;

import static minicp.cp.Factory.*;
import static minicp.util.InconsistencyException.INCONSISTENCY;

public class Disjunctive extends Constraint {

    private final IntVar[] start;
    private final int[] duration;
    private final IntVar[] end;
    private final boolean postMirror;


    private final int nTask;

    // EST
    private final int[] currentMinStarts;

    // LST
    private final int[] currentMaxStarts;

    // ECT
    private final int[] currentMinEnds;

    // LCT
    private final int[] currentMaxEnds;

    // Ordered tasks
    private final int[] orderedByIncreasingMaxEnds;
    private final int[] orderedByIncreasingMinEnds;
    private final int[] orderedByIncreasingMinStarts;


    // Temporary array with the size of nTask
    private final int[] tmp;


    private final ThetaTree thetaTree;

    public Disjunctive(IntVar[] start, int[] duration) throws InconsistencyException {
        this(start, duration, true);
    }

    private Disjunctive(IntVar[] start, int[] duration, boolean postMirror) throws InconsistencyException {
        super(start[0].getSolver());

        this.nTask = start.length;
        this.start = start;
        this.duration = duration;
        this.end = makeIntVarArray(cp, nTask, i -> plus(start[i],duration[i]));
        this.postMirror = postMirror;

        currentMinStarts = new int[nTask];
        currentMaxStarts = new int[nTask];
        currentMinEnds = new int[nTask];
        currentMaxEnds = new int[nTask];

        orderedByIncreasingMaxEnds = new int[nTask];
        orderedByIncreasingMinEnds = new int[nTask];
        orderedByIncreasingMinStarts = new int[nTask];

        tmp = new int[nTask];

        thetaTree = new ThetaTree(nTask);
    }


    @Override
    public void post() throws InconsistencyException {

//        int [] demands = new int[start.length];
//        for (int i = 0; i < start.length; i++) {
//            demands[i] = 1;
//        }
//        cp.post(new Cumulative(start,duration,demands,1));
//

        // TODO 1: replace the cumulative by  posting  binary decomposition using IsLessOrEqualVar


//        ThetaTree tt = new ThetaTree(start.length);

        for (int i = 0; i < start.length; i++) {
            for (int j = i+1; j < start.length; j++) {
                // i before j or j before i
                BoolVar ij = makeBoolVar(cp);
                BoolVar ji = makeBoolVar(cp);
                cp.post(new IsLessOrEqualVar(ij, end[i], start[j]));
                cp.post(new IsLessOrEqualVar(ji, end[j], start[i]));
                cp.post(new NotEqual(ij, ji));
            }
        }
//
        // TODO 3: add the mirror filtering as done in the Cumulative Constraint
        if (postMirror) {
            IntVar[] startMirror = makeIntVarArray(cp, start.length, i -> minus(end[i]));
            cp.post(new Disjunctive(startMirror, duration, false), false);
        }

        for (int i = 0; i < nTask; ++i) {
            start[i].propagateOnBoundChange(this);
//            end[i].propagateOnBoundChange(this);
        }
//
        propagate();

        // TODO 4: add the OverLoadCheck algorithms

        // TODO 5: add the Detectable Precedences algorithm

        // TODO 6: add the Not-Last algorithm

        // TODO 7 (optional, for a bonus): implement the Lambda-Theta tree and implement the Edge-Finding
    }

    @Override
    public void propagate() throws InconsistencyException {

        for (int i = 0; i < nTask; ++i) {
            currentMinStarts[i] = start[i].getMin();
            currentMaxStarts[i] = start[i].getMax();
            currentMinEnds[i] = end[i].getMin();
            currentMaxEnds[i] = end[i].getMax();
        }

        overloadChecking();
//        detectablePrecedences();
    }

    private void sortActivities(int[] values, int[] orderedIndices) {
        SortUtils.quicksort(values, orderedIndices);
    }

    private void overloadChecking() throws InconsistencyException {

        thetaTree.reset();

        sortActivities(currentMinStarts, orderedByIncreasingMinStarts);

        sortActivities(currentMaxEnds, orderedByIncreasingMaxEnds);

        for (int i = 0; i < nTask; ++i) {

            int index = orderedByIncreasingMaxEnds[i];
            int value = currentMaxEnds[index];

            assert(value == end[index].getMax());

            thetaTree.insert(orderedByIncreasingMinStarts[i], end[index].getMin(), duration[index]);

            if (thetaTree.getECT() > end[index].getMax()) {
                throw INCONSISTENCY;
            }
        }
    }

    private void detectablePrecedences() throws InconsistencyException {

        thetaTree.reset();

        sortActivities(currentMaxEnds, orderedByIncreasingMaxEnds);
        sortActivities(currentMinStarts, orderedByIncreasingMinStarts);

        int j = 0;
        for (int i = 0; i < nTask; ++i) {
            int est = currentMinStarts[orderedByIncreasingMinStarts[i]] + duration[i];
            int lct = currentMaxEnds[orderedByIncreasingMaxEnds[j]] - duration[j];

            while (est > lct) {
                thetaTree.insert(orderedByIncreasingMinStarts[j], end[j].getMin(), duration[j]);
                if (j < nTask - 1) {
                    j++;
                    est = currentMinStarts[orderedByIncreasingMinStarts[i]] + duration[i];
                    lct = currentMaxEnds[orderedByIncreasingMaxEnds[j]] - duration[j];
                }
                else {
                    break;
                }
            }

            tmp[i] = Math.max(currentMinStarts[orderedByIncreasingMinStarts[i]], thetaTree.getECT());
        }

        for (int i = 0; i < nTask; ++i) {
            start[i].removeBelow(tmp[i]);
        }
    }


}
