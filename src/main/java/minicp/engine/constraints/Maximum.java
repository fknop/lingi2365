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


package minicp.engine.constraints;

import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import java.util.Arrays;
import java.util.Optional;

public class Maximum extends Constraint {

    private final IntVar[] x;
    private final IntVar y;

    /**
     * z = maximum(x[0],x[1],...,x[n])
     * @param x
     * @param y
     */
    public Maximum(IntVar[] x, IntVar y) {
        super(x[0].getSolver());
        assert (x.length > 0);

        registerVariable(x);
        registerVariable(y);

        this.x = x;
        this.y = y;
    }


    @Override
    public void post() throws InconsistencyException {

        for (IntVar var: x) {
            // Bound consistency
            var.propagateOnBoundChange(this);
        }

        y.propagateOnBoundChange(this);

        propagate();
    }

    @Override
    public void propagate() throws InconsistencyException {

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (IntVar var: x) {
            var.removeAbove(y.getMax());

            if (var.getMax() >= y.getMin()) {
                var.removeBelow(y.getMin());

                if (var.getMin() < min) {
                    min = var.getMin();
                }

                if (var.getMax() > max) {
                    max = var.getMax();
                }
            }
        }


        y.removeAbove(max);
        y.removeBelow(min);

        boolean allBound = Arrays.stream(x).allMatch(IntVar::isBound);
        if (allBound) {
            y.assign(max);
            this.deactivate();
        }

    }
}
