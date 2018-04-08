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
import minicp.reversible.ReversibleInt;
import minicp.reversible.ReversibleSparseSet;
import minicp.util.InconsistencyException;

import java.util.ArrayList;

public class AllDifferentBinary extends Constraint {

    private IntVar [] x;
//    private int[] indices;

//    private ReversibleSparseSet set;

    public AllDifferentBinary(IntVar ... x) {
        super(x[0].getSolver());
        this.x = x;
//        set = new ReversibleSparseSet(cp.getTrail(), x.length);
//        indices = new int[x.length];
    }

    @Override
    public void post() throws InconsistencyException {
        Solver cp = x[0].getSolver();
        for (int i = 0; i < x.length; i++) {
            for (int j = i+1; j < x.length; j++) {
                cp.post(new NotEqual(x[i],x[j]),false);
            }
        }


//        for (int i = 0; i < x.length; ++i) {
//            final int j = i;
//            x[i].whenBind(() -> {
//                onBind(j);
//            });
//
//            if (x[i].isBound()) {
//                onBind(i);
//            }
//        }
//    }
//
//    private void onBind(int i) throws InconsistencyException {
//
//        assert(x[i].isBound());
//
//        IntVar xi = x[i];
//        set.remove(i);
//
//        int remaining = set.fillArray(indices);
//
//        for (int j = 0; j < remaining; ++j) {
//            int k = indices[j];
//            x[k].remove(xi.getMin());
//        }
//
//        if (set.isEmpty()) {
//            this.deactivate();
//        }
    }
}
