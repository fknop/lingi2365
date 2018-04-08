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
import minicp.engine.core.delta.DeltaInt;
import minicp.util.InconsistencyException;

public class Equal extends Constraint {

    private final IntVar x;
    private final IntVar y;

    private DeltaInt deltaX;
    private DeltaInt deltaY;

    private int[] deltaValues;

    public Equal(IntVar x, IntVar y) {
        super(x.getSolver());

        registerVariable(x, y);

        this.x = x;
        this.y = y;
        deltaValues = new int[Math.max(x.getSize(), y.getSize())];
    }

    @Override
    public void post() throws InconsistencyException {
        if (x.isBound()) {
            y.assign(x.getMin());
        }
        else if (y.isBound()) {
            x.assign(y.getMin());
        }
        else {
            int[] domainX = new int[x.getSize()];
            int[] domainY = new int[y.getSize()];
            int xSize = x.fillArray(domainX);
            for (int i = 0; i < xSize; ++i) {
                int v = domainX[i];
                if (!y.contains(v)) {
                    x.remove(v);
                }
            }

            int ySize = x.fillArray(domainY);
            for (int i = 0; i < ySize; ++i) {
                int v = domainY[i];
                if (!x.contains(v)) {
                    y.remove(v);
                }
            }
        }


        deltaX = x.propagateOnDomainChangeWithDelta(this);
        deltaY = y.propagateOnDomainChangeWithDelta(this);

        propagate();
    }

    @Override
    public void propagate() throws InconsistencyException {
        filter(x, y, deltaX);
        filter(y, x, deltaY);
    }

    private void filter(IntVar a, IntVar b, DeltaInt delta) throws InconsistencyException {
        if (a.isBound()) {
            b.assign(a.getMin());
            this.deactivate();
        }
        else if (delta.changed()) {
            int size = delta.fillArray(deltaValues);
            for (int i = 0; i < size; ++i) {
                b.remove(deltaValues[i]);
            }
        }
    }
}
