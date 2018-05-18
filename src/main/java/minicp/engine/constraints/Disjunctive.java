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
import minicp.util.SortUtils;

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

    // lct - p
    private final int[] currentLctMinDuration;

    // est + p
    private final int[] currentEstPlusDuration;

    // Ordered tasks
    private final int[] orderedByIncreasingMaxEnds;
    private final int[] orderedByIncreasingMinStarts;

    private final int[] orderedByIncreasingLctMinDuration;
    private final int[] orderedByIncreasingEstPlusDuration;

    private final int[] indicesOrder;


    // Temporary array with the size of nTask
    private final int[] tmp;

    // Theta tree
    private final ThetaLambdaTree thetaLambdaTree;
//    private final ThetaLambdaTree thetaLambdaTree;

    // Flags for propagation
    private boolean changed = true;
    private boolean failure = false;

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
        currentLctMinDuration = new int[nTask];
        currentEstPlusDuration = new int[nTask];

        orderedByIncreasingMaxEnds = new int[nTask];
        orderedByIncreasingMinStarts = new int[nTask];

        orderedByIncreasingLctMinDuration = new int[nTask];
        orderedByIncreasingEstPlusDuration = new int[nTask];

        indicesOrder = new int[nTask];

        tmp = new int[nTask];


//        thetaLambdaTree = new ThetaTree(nTask);
        thetaLambdaTree = new ThetaLambdaTree(nTask);
    }


    @Override
    public void post() throws InconsistencyException {

        for (int i = 0; i < nTask; i++) {
            for (int j = i+1; j < nTask; j++) {
                // i before j or j before i
                BoolVar ij = makeBoolVar(cp);
                BoolVar ji = makeBoolVar(cp);
                cp.post(new IsLessOrEqualVar(ij, end[i], start[j]));
                cp.post(new IsLessOrEqualVar(ji, end[j], start[i]));
                cp.post(new NotEqual(ij, ji), false);
            }
        }

        //  add the mirror filtering as done in the Cumulative Constraint
        if (postMirror) {
            IntVar[] startMirror = makeIntVarArray(cp, start.length, i -> minus(end[i]));
            cp.post(new Disjunctive(startMirror, duration, false), false);
        }

        for (int i = 0; i < nTask; ++i) {
            start[i].propagateOnBoundChange(this);
//            end[i].propagateOnBoundChange(this);
        }

        propagate();

        // TODO 7 (optional, for a bonus): implement the Lambda-Theta tree and implement the Edge-Finding
    }



    @Override
    public void propagate() throws InconsistencyException {

        failure = false;
        changed = true;

        setupCurrentValues();

        while(!failure && changed) {
            changed = overloadChecking() || detectablePrecedences() || notLast() || edgeFinding();
            if (changed) {
                setupCurrentValues();
            }
        }

        if (failure) {
            throw INCONSISTENCY;
        }
    }

    private void setupCurrentValues() {
        for (int i = 0; i < nTask; ++i) {
            currentMinStarts[i] = start[i].getMin();
            currentMaxStarts[i] = start[i].getMax();
            currentMinEnds[i] = end[i].getMin();
            currentMaxEnds[i] = end[i].getMax();
            currentEstPlusDuration[i] = start[i].getMin() + duration[i];
            currentLctMinDuration[i] = end[i].getMax() - duration[i];
        }
    }

    private void sortActivities(int[] values, int[] orderedIndices) {
        SortUtils.quicksort(values, orderedIndices);
    }

    private void getIndicesOrder(int[] indices, int[] order) {
        for (int i = 0; i < indices.length; ++i) {
            order[indices[i]] = i;
        }
    }

    private boolean overloadChecking() {

        thetaLambdaTree.reset();

        sortActivities(currentMinStarts, orderedByIncreasingMinStarts);
        sortActivities(currentMaxEnds, orderedByIncreasingMaxEnds);

        getIndicesOrder(orderedByIncreasingMinStarts, indicesOrder);


        for (int i = 0; i < nTask; ++i) {

            int index = orderedByIncreasingMaxEnds[i];

            thetaLambdaTree.insert(indicesOrder[index], end[index].getMin(), duration[index]);

            if (thetaLambdaTree.getECT() > end[index].getMax()) {
                failure = true;
                return true;
            }
        }

        return false;
    }

    private boolean detectablePrecedences() throws InconsistencyException {

        thetaLambdaTree.reset();

        // sort for inserting into tree
        sortActivities(currentMinStarts, orderedByIncreasingMinStarts);
        getIndicesOrder(orderedByIncreasingMinStarts, indicesOrder);

        sortActivities(currentLctMinDuration, orderedByIncreasingLctMinDuration);
        sortActivities(currentEstPlusDuration, orderedByIncreasingEstPlusDuration);

        int j = 0;
        for (int i = 0; i < nTask; ++i) {

            int index = orderedByIncreasingEstPlusDuration[i];
            int jIndex = orderedByIncreasingLctMinDuration[j];

            while (currentEstPlusDuration[index] > currentLctMinDuration[jIndex]) {
                thetaLambdaTree.insert(indicesOrder[jIndex], end[jIndex].getMin(), duration[jIndex]);
                if (j < nTask - 1) {
                    jIndex = orderedByIncreasingLctMinDuration[++j];
                }
                else {
                    break;
                }
            }


            if (thetaLambdaTree.isPresent(indicesOrder[index])) {
                thetaLambdaTree.remove(indicesOrder[index]);
                tmp[index] = Math.max(start[index].getMin(), thetaLambdaTree.getECT());
                thetaLambdaTree.insert(indicesOrder[index], end[index].getMin(), duration[index]);
            }
            else {
                tmp[index] = Math.max(start[index].getMin(), thetaLambdaTree.getECT());
            }


        }

        boolean changed = false;
        for (int i = 0; i < nTask; ++i) {
            changed = changed || tmp[i] != start[i].getMin();

            start[i].removeBelow(tmp[i]);
        }

        return changed;
    }

    private boolean notLast() throws InconsistencyException {
        thetaLambdaTree.reset();

        // sort for inserting into tree
        sortActivities(currentMinStarts, orderedByIncreasingMinStarts);
        getIndicesOrder(orderedByIncreasingMinStarts, indicesOrder);

        sortActivities(currentLctMinDuration, orderedByIncreasingLctMinDuration);
        sortActivities(currentMaxEnds, orderedByIncreasingMaxEnds);

        for (int i = 0; i < nTask; ++i) {
            tmp[i] = end[i].getMax();
        }

        int j = 0;
        for (int i = 0; i < nTask; ++i) {

            int index = orderedByIncreasingMaxEnds[i];
            int jIndex = orderedByIncreasingLctMinDuration[j];

            while (currentMaxEnds[index] > currentLctMinDuration[jIndex]) {

                thetaLambdaTree.insert(indicesOrder[jIndex], end[jIndex].getMin(), duration[jIndex]);
                if (j < nTask - 1) {
                    jIndex = orderedByIncreasingLctMinDuration[++j];
                }
                else {
                    break;
                }
            }

            int ectWithout = thetaLambdaTree.getECT();
            if (thetaLambdaTree.isPresent(indicesOrder[index])) {
                thetaLambdaTree.remove(indicesOrder[index]);
                ectWithout = thetaLambdaTree.getECT();
                thetaLambdaTree.insert(indicesOrder[index], end[index].getMin(), duration[index]);
            }

            if (ectWithout > currentLctMinDuration[index]) {
                tmp[index] = Math.min(currentLctMinDuration[jIndex], tmp[index]);
            }
        }


        boolean changed = false;
        for (int i = 0; i < nTask; ++i) {
            changed = changed || tmp[i] != end[i].getMax();
            end[i].removeAbove(tmp[i]);
        }

        return changed;
    }

    private boolean edgeFinding() throws InconsistencyException {

        thetaLambdaTree.reset();
        thetaLambdaTree.useGrayNodes();

        sortActivities(currentMinStarts, orderedByIncreasingMinStarts);
        getIndicesOrder(orderedByIncreasingMinStarts, indicesOrder);

        sortActivities(currentMaxEnds, orderedByIncreasingMaxEnds);


        // orderedByIncreasingMinStarts[i]:
        // indicesOrder[i]: for activity i, gives the position of activity i

        for (int i = 0; i < nTask; ++i) {
            thetaLambdaTree.insert(indicesOrder[i], end[i].getMin(), duration[i]);
        }

        int j = nTask - 1;
        boolean changed = false;

        while (j > 1) {
            int index = orderedByIncreasingMaxEnds[j];
            if (thetaLambdaTree.getECT() > end[index].getMax()) {
                failure = true;
                return true;
            }

            thetaLambdaTree.setGrayActivity(indicesOrder[index]);
            j--;

            index = orderedByIncreasingMaxEnds[j];
            while (thetaLambdaTree.getECTBar() > end[index].getMax()) {
                int responsible = thetaLambdaTree.responsibleEctBar();
                int i = orderedByIncreasingMinStarts[responsible];
                int est = Math.max(end[i].getMin(), thetaLambdaTree.getECT());

                if (est != start[i].getMin()) {
                    changed = true;
                }

                start[i].removeBelow(est);
                thetaLambdaTree.remove(responsible);
            }
        }

        return changed;
    }
}
