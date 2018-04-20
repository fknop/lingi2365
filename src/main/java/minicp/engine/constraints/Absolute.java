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
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import java.util.HashMap;

public class Absolute extends Constraint {

    enum Consistency {
        BOUND,
        DOMAIN
    }

    private IntVar x;
    private IntVar y;
    private Consistency consistency;

    private int[] yDomain;
    private int[] xDomain;

    /**
     * Build a constraint z = |x|
     *
     * @param x
     * @param y
     */
    public Absolute(IntVar x, IntVar y) {
        this(x, y, Consistency.DOMAIN);
    }

    public Absolute(IntVar x, IntVar y, Consistency consistency) {
        super(x.getSolver(), 2);

        registerVariable(x, y);

        this.x = x;
        this.y = y;
        this.yDomain = new int[y.getSize()];
        this.xDomain = new int[x.getSize()];
        this.consistency = consistency;
    }

    @Override
    public void post() throws InconsistencyException {

        y.removeBelow(0);

        y.propagateOnDomainChange(this);
        x.propagateOnDomainChange(this);

        propagate();
    }



    @Override
    public void propagate() throws InconsistencyException {

        // Remove value in z not in |x|

        if (x.isBound()) {
            y.assign(Math.abs(x.getMin()));
        }
        else {
            int size = y.fillArray(yDomain);
            for (int i = 0; i < size; ++i) {
                int value = yDomain[i];

                if (!x.contains(value) && !x.contains(-value)) {
                    y.remove(value);
                }
            }
        }

        if (this.consistency == Consistency.BOUND) {
            // Filter bounds of z
            int min = y.getMin();
            int max = y.getMax();

            if (min != 0) {
                for (int i = -min + 1; i < min; ++i) {
                    x.remove(i);
                }
            }

            if (max != 0) {
                x.removeAbove(max);
                x.removeBelow(-max);
            }
        }


        // Naive implementation: looping over domain of x
        if (this.consistency == Consistency.DOMAIN) {

            if (y.isBound()) {
                x.removeBelow(-y.getMin());
                x.removeAbove(y.getMin());

                int xSize = x.fillArray(xDomain);

                for (int i = 0; i < xSize; ++i) {
                    int value = xDomain[i];
                    if (Math.abs(value) != y.getMin()) {
                        x.remove(value);
                    }
                }
            }
            else {
                int xSize = x.fillArray(xDomain);

                for (int i = 0; i < xSize; ++i) {
                    int value = xDomain[i];
                    if (!y.contains(Math.abs(value))) {
                        x.remove(value);
                    }
                }
            }
        }
    }

}
