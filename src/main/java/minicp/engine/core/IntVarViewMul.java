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


package minicp.engine.core;


import minicp.engine.core.delta.DeltaInt;
import minicp.reversible.ReversibleDeltaInt;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import static minicp.util.InconsistencyException.INCONSISTENCY;

public class IntVarViewMul implements IntVar {

    private final int a;
    private final IntVar x;

    public IntVarViewMul(IntVar x, int a) {
        assert(a > 0);
        this.a = a;
        this.x = x;
    }

    @Override
    public Solver getSolver() {
        return x.getSolver();
    }

    @Override
    public void whenDomainChange(ConstraintClosure.Filtering c) {
        x.whenDomainChange(c);
    }

    @Override
    public void whenBind(ConstraintClosure.Filtering c) {
        x.whenBind(c);
    }

    @Override
    public void whenBoundsChange(ConstraintClosure.Filtering c) {
        x.whenBoundsChange(c);
    }

    @Override
    public void propagateOnDomainChange(Constraint c) {
        x.propagateOnDomainChange(c);
    }

    @Override
    public DeltaInt propagateOnDomainChangeWithDelta(Constraint c) {
        DeltaInt delta = new ReversibleDeltaInt(getSolver().getTrail(), this);
        c.registerDelta(delta);
        x.propagateOnDomainChange(c);
        return delta;
    }

    @Override
    public void propagateOnBind(Constraint c) {
        x.propagateOnBind(c);
    }

    @Override
    public void propagateOnBoundChange(Constraint c) {
        x.propagateOnBoundChange(c);
    }

    @Override
    public int fillArray(int[] dest) {
        int size = x.fillArray(dest);
        for (int i = 0; i < size; ++i) {
            dest[i] *= a;
        }

        return size;
    }

    @Override
    public int getMin() {
        return a * x.getMin();
    }

    @Override
    public int getMax() {
        return a * x.getMax();
    }

    @Override
    public int getSize() {
        return x.getSize();
    }



    @Override
    public boolean isBound() {
        return x.isBound();
    }

    @Override
    public boolean contains(int v) {
        return (v % a != 0) ? false : x.contains(v / a);
    }

    @Override
    public void remove(int v) throws InconsistencyException {
        if (v % a == 0) {
            x.remove(v / a);
        }
    }

    @Override
    public void assign(int v) throws InconsistencyException {
        if (v % a == 0) {
            x.assign(v / a);
        } else {
            throw INCONSISTENCY;
        }
    }

    @Override
    public int removeBelow(int v) throws InconsistencyException {
        return (x.removeBelow(ceilDiv(v, a))) * a;
    }

    @Override
    public int removeAbove(int v) throws InconsistencyException {
        return (x.removeAbove(floorDiv(v, a))) * a;
    }

    @Override
    public int[] delta(int oldSize) {
        int[] delta = x.delta(oldSize);
        for (int i = 0; i < delta.length; ++i) {
            delta[i] *= a;
        }

        return delta;
    }

    @Override
    public int fillDelta(int[] values, int oldSize) {
        int size = x.fillDelta(values, oldSize);
        for (int i = 0; i < size; ++i) {
            values[i] *= a;
        }

        return size;
    }


    // Java's division always rounds to the integer closest to zero, but we need flooring/ceiling versions.
    private int floorDiv(int a, int b) {
        int q = a / b;
        return (a < 0 && q * b != a) ? q - 1 : q;
    }

    private int ceilDiv(int a, int b) {
        int q = a / b;
        return (a > 0 && q * b != a) ? q + 1 : q;
    }
}
