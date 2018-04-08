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

import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;

import java.util.ArrayList;
import java.util.Collections;

import static minicp.util.InconsistencyException.INCONSISTENCY;

public class Element1D extends Constraint {

    int[] T;
    IntVar x;
    IntVar z;

    private final int n;

    private final ReversibleInt low;
    private final ReversibleInt up;
    private final ArrayList<Pair> xy;

    private ReversibleInt[] nCol;

    private class Pair implements Comparable<Pair> {
        protected final int x,z;

        private Pair(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public int compareTo(Pair t) {
            return z - t.z;
        }
    }

    // T[x] = z
    public Element1D(int[] T, IntVar x, IntVar z) {
        super(z.getSolver());

        this.T = T;
        this.x = x;
        this.z = z;
        this.n = T.length;
        this.xy = new ArrayList<>();
        for (int i = 0; i < T.length; i++) {
            this.xy.add(new Pair(i, T[i]));
        }

        Collections.sort(this.xy);

        low = new ReversibleInt(cp.getTrail(),0);
        up = new ReversibleInt(cp.getTrail(), xy.size() - 1);
        nCol = new ReversibleInt[n];

        for (int i = 0; i < n; i++) {
            nCol[i] = new ReversibleInt(cp.getTrail(), 1);
        }


    }

    @Override
    public void post() throws InconsistencyException {
        x.removeBelow(0);
        x.removeAbove(n - 1);

        x.propagateOnDomainChange(this);
        z.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() throws InconsistencyException {
        int l = low.getValue();
        int u = up.getValue();

        int zMin = z.getMin();
        int zMax = z.getMax();

        while (xy.get(l).z < zMin || !x.contains(xy.get(l).x)) {
            if (nCol[xy.get(l).x].decrement() == 0) {
                x.remove(xy.get(l).x);
            }
//            z.remove(xy.get(l).z);
            l++;

            if (l > u) {
                throw INCONSISTENCY;
            }
        }

        while (xy.get(u).z > zMax || !x.contains(xy.get(u).x)) {
            if (nCol[xy.get(u).x].decrement() == 0) {
                x.remove(xy.get(u).x);
            }
//            z.remove(xy.get(u).z);

            u--;

            if (l > u)
                throw INCONSISTENCY;
        }


        z.removeBelow(xy.get(l).z);
        z.removeAbove(xy.get(u).z);
        low.setValue(l);
        up.setValue(u);


        if (x.isBound()) {
            z.assign(T[x.getMin()]);
        }
    }
}
