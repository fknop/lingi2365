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

public class Element1DVar extends Constraint {

    enum Consistency {
        BOUND,
        DOMAIN
    }

    private final Consistency consistency;
    private final IntVar[] T;
    private final IntVar x;
    private final IntVar y;
    private final int n;



    public Element1DVar(IntVar[] T, IntVar x, IntVar y) {
        this(T, x, y, Consistency.BOUND);
    }

    public Element1DVar(IntVar[] T, IntVar x, IntVar y, Consistency consistency) {
        super(y.getSolver());
        System.out.println("Element1DVar");
        this.T = T;
        this.n = T.length;
        this.x = x;
        this.y = y;
        this.consistency = consistency;
    }

    @Override
    public void post() throws InconsistencyException {
        x.removeBelow(0);
        x.removeAbove(n - 1);

        x.propagateOnDomainChange(this);
        y.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() throws InconsistencyException {
        if (this.consistency == Consistency.BOUND) {
            this.propagateBoundConsistent();
        }

        if (this.consistency == Consistency.DOMAIN) {
            this.propagateDomainConsistent();
        }
    }

    private void propagateDomainConsistent() throws InconsistencyException {
        throw new NotImplementedException("Element1DVar");
    }

    private void propagateBoundConsistent() throws InconsistencyException {

        int[] xDomain = new int[x.getSize()];
        int xSize = x.fillArray(xDomain);

        for (int i = 0; i < xSize; ++i) {
            IntVar t = T[xDomain[i]];
            int min = t.getMin();
            int max = y.getMax();

            if (min > max) {
                x.remove(xDomain[i]);
            }

            min = y.getMin();
            max = t.getMax();

            if (min > max) {
                x.remove(xDomain[i]);
            }
        }

        if (xSize != x.getSize()) {
            xSize = x.fillArray(xDomain);
        }

        int tMin = Integer.MAX_VALUE;
        int tMax = Integer.MIN_VALUE;
        for (int i = 0; i < xSize; ++i) {
            IntVar t = T[xDomain[i]];
            if (t.getMin() < tMin) {
                tMin = t.getMin();
            }

            if (t.getMax() > tMax) {
                tMax = t.getMax();
            }
        }

        int yMin = Math.max(y.getMin(), tMin);
        int yMax = Math.min(y.getMax(), tMax);

        y.removeBelow(yMin);
        y.removeAbove(yMax);


        if (x.isBound()) {
            IntVar t = T[x.getMin()];
            y.removeBelow(Math.max(y.getMin(), t.getMin()));
            y.removeAbove(Math.min(y.getMax(), t.getMax()));

            t.removeBelow(Math.max(y.getMin(), t.getMin()));
            t.removeAbove(Math.min(y.getMax(), t.getMax()));
        }

    }
}
