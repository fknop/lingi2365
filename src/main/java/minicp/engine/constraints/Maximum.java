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
     * y = maximum(x[0],x[1],...,x[n])
     * @param x
     * @param y
     */
    public Maximum(IntVar[] x, IntVar y) {
        super(x[0].getSolver());
        assert (x.length > 0);
        this.x = x;
        this.y = y;
    }


    @Override
    public void post() throws InconsistencyException {

        for (IntVar var: x) {
            // Bound consistency
            var.propagateOnBoundChange(this);
        }

        propagate();
    }

    @Override
    public void propagate() throws InconsistencyException {
        for (IntVar var: x) {
            var.removeAbove(y.getMax());
            var.removeBelow(y.getMin());
        }

        Optional<Integer> max = Arrays.stream(x).map(IntVar::getMax).max(Integer::compareTo);
        Optional<Integer> min = Arrays.stream(x).map(IntVar::getMin).min(Integer::compareTo);
        if (max.isPresent()) {
            y.removeAbove(max.get());
            boolean allBound = Arrays.stream(x).allMatch(IntVar::isBound);
            if (allBound) {
                y.assign(max.get());
            }
        }

        if (min.isPresent()) {
            y.removeBelow(min.get());
        }


    }
}
