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

public class Element2D extends Constraint {


    private final int[][] T;
    private final IntVar x, y, z;
    private int n, m;
    private final ReversibleInt[] nRowsSup;
    private final ReversibleInt[] nColsSup;

    private final ReversibleInt low;
    private final ReversibleInt up;
    private final ArrayList<Triple> xyz;

    private class Triple implements Comparable<Triple> {
        protected final int x,y,z;

        private Triple(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public int compareTo(Triple t) {
            return z - t.z;
        }
    }

    /**
     * T[x][z] = z
     * @param T
     * @param x
     * @param y
     * @param z
     */
    public Element2D(int[][] T, IntVar x, IntVar y, IntVar z) {
        super(x.getSolver(), 3);

        registerVariable(x, y, z);

        this.T = T;
        this.x = x;
        this.y = y;
        this.z = z;
        n = T.length;
        m = T[0].length;

        this.xyz = new ArrayList<Triple>();
        for (int i = 0; i < T.length; i++) {
            assert (T[i].length == m); // n x m matrix expected
            for (int j = 0; j < T[i].length; j++) {
                xyz.add(new Triple(i,j,T[i][j]));
            }
        }
        Collections.sort(xyz);
        low = new ReversibleInt(cp.getTrail(),0);
        up = new ReversibleInt(cp.getTrail(),xyz.size()-1);

        nColsSup = new ReversibleInt[n];
        nRowsSup = new ReversibleInt[m];
        for (int i = 0; i < n; i++) {
            nColsSup[i] = new ReversibleInt(cp.getTrail(),m);
        }
        for (int j = 0; j < m; j++) {
            nRowsSup[j] = new ReversibleInt(cp.getTrail(),n);
        }
    }

    @Override
    public void post() throws InconsistencyException {
        x.removeBelow(0);
        x.removeAbove(n-1);
        y.removeBelow(0);
        y.removeAbove(m-1);

        x.propagateOnDomainChange(this);
        y.propagateOnDomainChange(this);
        z.propagateOnBoundChange(this);
        propagate();
    }

    private void updateSupports(int lostPos) throws InconsistencyException {
        if (nColsSup[xyz.get(lostPos).x].decrement() == 0) {
            x.remove(xyz.get(lostPos).x);
        }
        if (nRowsSup[xyz.get(lostPos).y].decrement() == 0) {
            y.remove(xyz.get(lostPos).y);
        }
    }

    @Override
    public void propagate() throws InconsistencyException {
        int l = low.getValue();
        int u = up.getValue();
        int zMin = z.getMin();
        int zMax = z.getMax();

        while (xyz.get(l).z < zMin || !x.contains(xyz.get(l).x) || !y.contains(xyz.get(l).y)) {
            updateSupports(l);
            l++;
            if (l > u) throw INCONSISTENCY;
        }
        while (xyz.get(u).z > zMax || !x.contains(xyz.get(u).x) || !y.contains(xyz.get(u).y)) {
            updateSupports(u);
            u--;
            if (l > u) throw INCONSISTENCY;
        }
        z.removeBelow(xyz.get(l).z);
        z.removeAbove(xyz.get(u).z);
        low.setValue(l);
        up.setValue(u);
    }

}
