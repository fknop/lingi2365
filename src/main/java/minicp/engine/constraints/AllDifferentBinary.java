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
import minicp.util.InconsistencyException;

public class AllDifferentBinary extends Constraint {

    private IntVar [] x;
    private ReversibleInt limit;

    public AllDifferentBinary(IntVar ... x) {
        super(x[0].getSolver());
        this.x = new IntVar[x.length];
        this.limit = new ReversibleInt(x[0].getSolver().getTrail(), x.length);
        System.arraycopy(x, 0, this.x, 0, x.length);
    }

    @Override
    public void post() throws InconsistencyException {
//        Solver cp = x[0].getSolver();
//        for (int i = 0; i < x.length; i++) {
//            for (int j = i+1; j < x.length; j++) {
//                cp.post(new NotEqual(x[i],x[j]),false);
//            }
//        }
        for (IntVar var: x) {
            var.propagateOnBind(this);
        }
    }

    @Override
    public void propagate() throws InconsistencyException {

        for (int i = 0; i < limit.getValue();) {
            if (x[i].isBound()) {
                IntVar xi = x[i];
                x[i] = x[limit.getValue() - 1];
                x[limit.getValue() - 1] = xi;
                limit.decrement();


                for (int j = 0; j < limit.getValue(); ++j) {
                    x[j].remove(xi.getMin());
                }
            }
            else {
                ++i;
            }
        }
    }
}
