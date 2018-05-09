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

import java.util.Arrays;

import static minicp.cp.Factory.*;

public class Disjunctive extends Constraint {

    private final IntVar[] start;
    private final int[] duration;
    private final IntVar[] end;
    private final boolean postMirror;


    public Disjunctive(IntVar[] start, int[] duration) throws InconsistencyException {
        this(start, duration, true);
    }

    private Disjunctive(IntVar[] start, int[] duration, boolean postMirror) throws InconsistencyException {
        super(start[0].getSolver());
        this.start = start;
        this.duration = duration;
        this.end = makeIntVarArray(cp,start.length, i -> plus(start[i],duration[i]));
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


        ThetaTree tt = new ThetaTree(start.length);

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


        




        // HINT: for the TODO 1-4 you'll need the ThetaTree data-structure

        // TODO 4: add the OverLoadCheck algorithms

        // TODO 5: add the Detectable Precedences algorithm

        // TODO 6: add the Not-Last algorithm

        // TODO 7 (optional, for a bonus): implement the Lambda-Theta tree and implement the Edge-Finding
    }

}
