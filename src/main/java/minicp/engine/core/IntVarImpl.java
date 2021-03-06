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
import minicp.reversible.ReversibleStack;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Set;

public class IntVarImpl implements IntVar {

    private Solver cp;
    private IntDomain domain;
    private ReversibleStack<Constraint> onDomain;
    private ReversibleStack<Constraint> onBind;
    private ReversibleStack<Constraint> onBounds;
    private DomainListener domListener = new DomainListener() {
        @Override
        public void bind() {
            scheduleAll(onBind);
        }

        @Override
        public void change(int domainSize) {
            scheduleAll(onDomain);
        }

        @Override
        public void removeBelow(int domainSize) {
            scheduleAll(onBounds);
        }

        @Override
        public void removeAbove(int domainSize) {
            scheduleAll(onBounds);
        }
    };

    /**
     * Create a variable with the elements {0,...,n-1}
     * as initial domain
     * @param cp
     * @param n > 0
     */
    public IntVarImpl(Solver cp, int n) {
        this(cp,0,n-1);
    }

    /**
     * Create a variable with the elements {min,...,max}
     * as initial domain
     * @param cp
     * @param min
     * @param max >= min
     */
    public IntVarImpl(Solver cp, int min, int max) {
        if (min > max) throw new InvalidParameterException("at least one value in the domain");
        this.cp = cp;
        cp.registerVar(this);
        domain = new SparseSetDomain(cp.getTrail(),min,max);
        onDomain = new ReversibleStack<>(cp.getTrail());
        onBind  = new ReversibleStack<>(cp.getTrail());
        onBounds = new ReversibleStack<>(cp.getTrail());
    }

    /**
     * Create a variable with values as initial domain
     * @param cp
     * @param values
     */
    public IntVarImpl(Solver cp, Set<Integer> values) {
        if (values.isEmpty()) {
            throw new InvalidParameterException("The values must not be an empty set");
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int value : values) {
            if (value < min) {
                min = value;
            }

            if (value > max) {
                max = value;
            }
        }

        this.cp = cp;
        cp.registerVar(this);
        onDomain = new ReversibleStack<>(cp.getTrail());
        onBind  = new ReversibleStack<>(cp.getTrail());
        onBounds = new ReversibleStack<>(cp.getTrail());
        domain = new SparseSetDomain(cp.getTrail(), min, max);

        for (int i = min; i <= max; ++i) {
            if (!values.contains(i)) {
                try {
                    domain.remove(i, domListener);
                } catch (InconsistencyException e) {
                    assert(false);
                }
            }
        }
    }


    public Solver getSolver() {
        return cp;
    }



    public boolean isBound() {
        return domain.getSize() == 1;
    }

    @Override
    public String toString() {
        return domain.toString();
    }

    public void whenDomainChange(ConstraintClosure.Filtering c) {
        onDomain.push(new ConstraintClosure(cp,c));
    }

    public void whenBind(ConstraintClosure.Filtering c) {
        onBind.push(new ConstraintClosure(cp,c));
    }

    public void whenBoundsChange(ConstraintClosure.Filtering c) {
        onBounds.push(new ConstraintClosure(cp,c));
    }

    public void propagateOnDomainChange(Constraint c) {
        onDomain.push(c);
    }

    public DeltaInt propagateOnDomainChangeWithDelta(Constraint c) {
        DeltaInt delta = new ReversibleDeltaInt(getSolver().getTrail(), this);
        c.registerDelta(delta);
        onDomain.push(c);
        return delta;
    }

    public void propagateOnBind(Constraint c) {
        onBind.push(c);
    }

    public void propagateOnBoundChange(Constraint c) { onBounds.push(c);}

    @Override
    public int fillArray(int[] dest) {
        return domain.fillArray(dest);
    }

    @Override
    public int fillArrayN(int[] dest, int n) {
        return domain.fillArrayN(dest, n);
    }

    private void scheduleAll(ReversibleStack<Constraint> constraints) {
        for (int i = 0; i < constraints.size(); i++)
            cp.schedule(constraints.get(i));
    }

    public int getMin() {
        return domain.getMin();
    }

    public int getMax() {
        return domain.getMax();
    }

    public int getSize() {
        return domain.getSize();
    }

    public boolean contains(int v) {
        return domain.contains(v);
    }

    public void remove(int v) throws InconsistencyException {
        domain.remove(v, domListener);
    }

    public void assign(int v) throws InconsistencyException {
        domain.removeAllBut(v, domListener);
    }

    public int removeBelow(int v) throws InconsistencyException {
        return domain.removeBelow(v, domListener);
    }

    public int removeAbove(int v) throws InconsistencyException {
        return domain.removeAbove(v, domListener);
    }

    public int[] delta(int oldSize) {
        return ((SparseSetDomain) domain).delta(oldSize);
    }
    public int fillDelta(int[] values, int oldSize) {
        return ((SparseSetDomain) domain).fillDelta(values, oldSize);
    }
}
