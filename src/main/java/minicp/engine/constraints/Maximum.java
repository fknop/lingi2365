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
        super(x[0].getSolver(), x.length + 1);
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

        int min = Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < x.length; ++i) {
            x[i].removeAbove(y.getMax());

            int xmin = x[i].getMin();
            int xmax = x[i].getMax();

            if (xmin > min) {
                min = xmin;
            }

            if (xmax > max) {
                max = xmax;
            }
        }

        y.removeAbove(max);
        y.removeBelow(min);

        for (int i = 0; i < x.length; ++i) {
            if (x[i].isBound() && x[i].getMin() == max) {
                y.assign(max);
                this.deactivate();
                break;
            }
        }
    }
}
