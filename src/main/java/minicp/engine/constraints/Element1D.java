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

import minicp.cp.Factory;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;

public class Element1D extends Constraint {

    int[] T;
    IntVar x;
    IntVar y;

    private final int n;

    private final ReversibleInt low;
    private final ReversibleInt up;
    private final ArrayList<Pair> xy;

    private class Pair implements Comparable<Pair> {
        protected final int x,y;

        private Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Pair t) {
            return y - t.y;
        }
    }

    // T[x] = y
    public Element1D(int[] T, IntVar x, IntVar y) {
        super(y.getSolver());

        this.T = T;
        this.x = x;
        this.y = y;
        this.n = T.length;
        this.xy = new ArrayList<>();
        for (int i = 0; i < T.length; i++) {
            this.xy.add(new Pair(i, T[i]));
        }

        Collections.sort(this.xy);

        low = new ReversibleInt(cp.getTrail(),0);
        up = new ReversibleInt(cp.getTrail(), xy.size() - 1);



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
        int l = low.getValue();
        int u = up.getValue();

        int yMin = y.getMin();
        int yMax = y.getMax();

        while (xy.get(l).y < yMin || !x.contains(xy.get(l).x)) {
            x.remove(xy.get(l).x);
            y.remove(xy.get(l).y);
            l++;

            if (l > u) {
                throw new InconsistencyException();
            }
        }

        while (xy.get(u).y > yMax || !x.contains(xy.get(u).x)) {
            x.remove(xy.get(u).x);
            y.remove(xy.get(u).y);

            u--;

            if (l > u) throw new InconsistencyException();
        }


        y.removeBelow(xy.get(l).y);
        y.removeAbove(xy.get(u).y);
        low.setValue(l);
        up.setValue(u);
    }
}
