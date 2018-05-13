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
import minicp.util.NotImplementedException;

import static minicp.cp.Factory.*;
import static minicp.util.InconsistencyException.INCONSISTENCY;

public class IsLessOrEqualVar extends Constraint { // b <=> x <= y

    private final BoolVar b;
    private final IntVar x;
    private final IntVar y;

    public IsLessOrEqualVar(BoolVar b, IntVar x, IntVar y) {
        super(x.getSolver());
        this.b = b;
        this.x = x;
        this.y = y;
        registerVariable(b, x, y);
    }

    @Override
    public void post() throws InconsistencyException {

//
//        if (x.isBound() || y.isBound()) {
//            whenVariableBound();
//        }

//        whenVariableChange();

        if (!b.isBound()) {
//            b.whenBind(this::whenBoolBound);
            b.propagateOnBind(this);
        }

        if (!x.isBound()) {
            x.propagateOnBoundChange(this);
//            x.whenDomainChange(this::whenVariableChange);
//            x.whenBind(this::whenVariableBound);
        }

        if (!y.isBound()) {
            y.propagateOnBoundChange(this);
//            y.whenDomainChange(this::whenVariableChange);
//            y.whenBind(this::whenVariableBound);
        }

//        if (b.isBound()) {
//            propagate();
//            whenBoolBound();
//        }

        propagate();
    }

    @Override
    public void propagate() throws InconsistencyException {
        if (b.isBound()) {
            if (b.isTrue()) {
                y.removeBelow(x.getMin());
                x.removeAbove(y.getMax());
            } else {
                x.removeBelow(y.getMin() + 1);
                y.removeAbove(x.getMax() - 1);
            }

//            if (b.isBound() && x.isBound() && y.isBound()) {
//                this.deactivate();
//            }
        }

        if (x.getMax() <= y.getMin()) {
            b.assign(true);
        }
        else if (x.getMin() > y.getMax()) {
            b.assign(false);
        }
    }
}
