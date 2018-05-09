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

import java.util.Arrays;
import java.util.Comparator;

import static minicp.cp.Factory.*;
import static minicp.util.InconsistencyException.INCONSISTENCY;

public class Disjunctive extends Constraint {

    class OrderedActivities {
        IntVarPair[] end;

        OrderedActivities(IntVar[] end) {
            this.end = new IntVarPair[end.length];

            for (int i = 0; i < end.length; ++i) {
                this.end[i] = new IntVarPair(end[i], i);
            }
        }

        IntVarPair[] getOrderedLct() {
            Arrays.sort(this.end, Comparator.comparingInt(o -> o.variable.getMax()));
            return this.end;
        }

        IntVarPair[] getOrderedActivities(Comparator<IntVarPair> comparator) {
            Arrays.sort(this.end, comparator);
            return this.end;
        }
    }

    private final IntVar[] start;
    private final int[] duration;
    private final IntVar[] end;
    private final OrderedActivities orderedActivities1;
    private final OrderedActivities orderedActivities2;
    private final int[] est;
    private final boolean postMirror;

    Comparator<IntVarPair> lctMinusP = new Comparator<IntVarPair>() {
        @Override
        public int compare(IntVarPair o1, IntVarPair o2) {
            return Integer.compare(o1.variable.getMax() - duration[o1.index], o2.variable.getMax() - duration[o2.index]);
        }
    };

    Comparator<IntVarPair> estPlusP = new Comparator<IntVarPair>() {
        @Override
        public int compare(IntVarPair o1, IntVarPair o2) {
            return Integer.compare(o1.variable.getMin() + duration[o1.index], o2.variable.getMin() + duration[o2.index]);
        }
    };


    public Disjunctive(IntVar[] start, int[] duration) throws InconsistencyException {
        this(start, duration, true);
    }

    private Disjunctive(IntVar[] start, int[] duration, boolean postMirror) throws InconsistencyException {
        super(start[0].getSolver());
        this.start = start;
        this.duration = duration;
        this.end = makeIntVarArray(cp,start.length, i -> plus(start[i],duration[i]));
        this.orderedActivities1 = new OrderedActivities(this.end);
        this.orderedActivities2 = new OrderedActivities(this.end);
        this.est = new int[start.length];
        this.postMirror = postMirror;
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
            System.out.println("post mirror");
            IntVar[] startMirror = makeIntVarArray(cp, start.length, i -> minus(end[i]));
            cp.post(new Disjunctive(startMirror, duration, false), false);
        }

        for (int i = 0; i < start.length; ++i) {
            start[i].propagateOnBoundChange(this);
        }


        propagate();
        // HINT: for the TODO 1-4 you'll need the ThetaTree data-structure

        // TODO 4: add the OverLoadCheck algorithms

        // TODO 5: add the Detectable Precedences algorithm

        // TODO 6: add the Not-Last algorithm

        // TODO 7 (optional, for a bonus): implement the Lambda-Theta tree and implement the Edge-Finding
    }

    private void overloadChecking() throws InconsistencyException {
        IntVarPair[] end = orderedActivities1.getOrderedLct();
        ThetaTree tt = new ThetaTree(end.length);
        for (int i = 0; i < end.length; ++i) {
            int index = end[i].index;
            tt.insert(i, end[i].variable.getMin(), duration[index]);

            if (tt.getECT() > end[i].variable.getMax()) {
                throw INCONSISTENCY;
            }
        }
    }

    private void detectablePrecedences() throws InconsistencyException {
        IntVarPair[] lctMP = orderedActivities1.getOrderedActivities(lctMinusP);
        IntVarPair[] estPP = orderedActivities2.getOrderedActivities(estPlusP);
        ThetaTree tt = new ThetaTree(end.length);

        int Q = 0;
        for (int i = 0; i < estPP.length; ++i) {
            int index = estPP[i].index;
            int esti = estPP[i].variable.getMin();
            int pi = duration[index];

            if (Q < lctMP.length) {
                int lctQFirst = lctMP[Q].variable.getMax();
                int pQFirst = duration[lctMP[Q].index];

                while (esti + pi > lctQFirst - pQFirst && Q < lctMP.length) {
                    tt.insert(Q, lctMP[Q].variable.getMin(), duration[lctMP[Q].index]);
                    Q++;
                }
            }

            est[index] = Math.max(start[index].getMin(), tt.getECT());
        }

        for (int i = 0; i < start.length; ++i) {
            start[i].removeBelow(est[i]);
        }
    }

    @Override
    public void propagate() throws InconsistencyException {
        overloadChecking();
        detectablePrecedences();
    }
}
