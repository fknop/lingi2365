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
import minicp.reversible.ReversibleInt;
import minicp.reversible.ReversibleSparseSet;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

public class IsOr extends Constraint { // b <=> x1 or x2 or ... xn

    private final BoolVar b;
    private final BoolVar[] x;
    private final int n;

    private int[] unBounds;
//    private ReversibleInt nUnBounds;

    private ReversibleSparseSet set;

    private final Or or;

    public IsOr(BoolVar b, BoolVar[] x) {
        super(b.getSolver());
        this.b = b;
        this.x = x;
        this.n = x.length;
        or = new Or(x);
        set = new ReversibleSparseSet(cp.getTrail(), n);

//        nUnBounds = new ReversibleInt(cp.getTrail(), n);
        unBounds = new int[n];
//        for (int i = 0; i < n; i++) {
//            unBounds[i] = i;
//        }
    }

    @Override
    public void post() throws InconsistencyException {
        b.propagateOnBind(this);

        for (int i = 0; i < n; i++) {
            int j = i;

            if (x[i].isFalse()) {
                set.remove(i);
            }

            x[i].whenBind(() -> {
                if (x[j].isTrue()) {
                    b.assign(true);
                    this.deactivate();
                }
                else {
                    set.remove(j);
                }

                if (set.isEmpty()) {
                    b.assign(false);
                }
            });
        }
    }

    @Override
    public void propagate() throws InconsistencyException {

        if (b.isBound()) {
            if (b.isTrue()) {
                cp.post(or);
                this.deactivate();
            }
            else {
                int size = set.fillArray(unBounds);
                for (int i = 0; i < size; ++i) {
                    x[i].assign(false);
                }
            }
        }
    }
}
