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

import minicp.reversible.Trail;
import minicp.reversible.ReversibleSparseSet;
import minicp.util.InconsistencyException;

import java.util.Iterator;

import static minicp.util.InconsistencyException.INCONSISTENCY;


public class SparseSetDomain extends IntDomain {
    private ReversibleSparseSet domain;
//    private int offset;


    public SparseSetDomain(Trail trail, int min, int max) {
//        System.out.println("sparse set min : " + min);
//        offset = min;
//        System.out.println("sparse set offset : " + offset);
        domain = new ReversibleSparseSet(trail, min, max);
    }

    public int getMin() {
        return domain.getMin(); // + offset;
    }

    public int getMax() {
        return domain.getMax(); //+ offset;
    }

    public int getSize() {
        return domain.getSize();
    }

    public boolean contains(int v) {
        return domain.contains(v); // - offset);
    }

    public boolean isBound() {
        return domain.getSize() == 1;
    }

    public void remove(int v, DomainListener x) throws InconsistencyException {
        if (domain.contains(v)) {
            boolean maxChanged = getMax() == v;
            boolean minChanged = getMin() == v;
            domain.remove(v);
            if (domain.getSize() == 0) throw INCONSISTENCY;
            x.change(domain.getSize());
            if (maxChanged) x.removeAbove(domain.getSize());
            if (minChanged) x.removeBelow(domain.getSize());
            if (domain.getSize() == 1) x.bind();
        }
    }

    public void removeAllBut(int v, DomainListener x) throws InconsistencyException {
        if (domain.contains(v)) {
            if (domain.getSize() != 1) {
                boolean maxChanged = getMax() != v;
                boolean minChanged = getMin() != v;
                domain.removeAllBut(v);
                x.bind();
                x.change(domain.getSize());
                if (maxChanged) x.removeAbove(domain.getSize());
                if (minChanged) x.removeBelow(domain.getSize());
            }
        }
        else {
            domain.removeAll();
            throw InconsistencyException.INCONSISTENCY;
        }
    }

    public int removeBelow(int value, DomainListener x) throws InconsistencyException {
        if (domain.getMin() < value) {
            domain.removeBelow(value);
            x.removeBelow(domain.getSize());
            x.change(domain.getSize());
            if (domain.getSize() == 1) x.bind();
        }
        if (domain.getSize() == 0) throw INCONSISTENCY;
        else return domain.getMin();
    }

    public int removeAbove(int value, DomainListener x) throws InconsistencyException {
        if (domain.getMax() > value) {
            domain.removeAbove(value);
            x.removeAbove(domain.getSize());
            x.change(domain.getSize());
            if (domain.getSize() == 1) x.bind();
        }
        if (domain.getSize() == 0) throw INCONSISTENCY;
        else return domain.getMax();
    }

    @Override
    public int fillArray(int[] dest) {
        int size = domain.fillArray(dest);
//        for(int i = 0 ; i < size ; i++) {
//            dest[i] += this.offset;
//        }
//
        return size;
    }

    public int[] delta(int oldSize) {
        return domain.delta(oldSize);
    }
    public int fillDelta(int[] values, int oldSize) {
        return domain.fillDelta(values, oldSize);
    }
}
