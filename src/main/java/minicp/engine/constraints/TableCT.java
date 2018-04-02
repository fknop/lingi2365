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
import minicp.engine.core.delta.DeltaInt;
import minicp.reversible.ReversibleSparseBitSet;
import minicp.util.InconsistencyException;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static minicp.cp.Factory.minus;
import static minicp.util.InconsistencyException.INCONSISTENCY;

// TODO: build incremental version
public class TableCT extends Constraint {
    protected IntVar[] x; //variables
    protected DeltaInt[] deltas;
    //supports[i][v] is the set of tuples supported by x[i]=v
    protected BitSet[][] supports;

    protected int[][] residues;
    protected boolean firstPropagate = true;
    protected ReversibleSparseBitSet supportedTuples;

    // Array to store domains of variables (avoid allocating multiple times)
    protected int domains[][];



    public TableCT(IntVar[] x, int[][] table) {
        this(x, table, true);
    }
    /**
     * Table constraint.
     * Assignment of x_0=v_0, x_1=v_1,... only valid if there exists a
     * row (v_0, v_1, ...) in the table.
     *
     * @param x     variables to constraint. x.length must be > 0.
     * @param table array of valid solutions (second dimension must be of same size as the array x)
     */
    protected TableCT(IntVar[] x, int[][] table, boolean setupSupports) {
        super(x[0].getSolver());
        this.x = new IntVar[x.length];
        this.deltas = new DeltaInt[x.length];

        List<Integer> initial = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            initial.add(i);
        }

        this.domains = new int[x.length][];
        this.supportedTuples = new ReversibleSparseBitSet(x[0].getSolver().getTrail(), table.length, initial);

        // Allocate supports and residues
        supports = new BitSet[x.length][];
        residues = new int[x.length][];

        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].getMin()); // map the variables domain to start at 0
            supports[i] = new BitSet[x[i].getMax() - x[i].getMin() + 1];
            residues[i] = new int[x[i].getMax() - x[i].getMin() + 1];

            for (int j = 0; j < supports[i].length; j++) {
                residues[i][j] = 0;
                supports[i][j] = new BitSet(table.length);
            }
        }

        if (setupSupports) {
            setupSupports(x, table);
        }
    }

    protected void setupSupports(IntVar[] x, int[][] table) {
        // Set values in supports, which contains all the tuples supported by each var-val pair
        for (int i = 0; i < table.length; i++) { //i is the index of the tuple (in table)
            for (int j = 0; j < x.length; j++) { //j is the index of the current variable (in x)
                if (x[j].contains(table[i][j])) {
                    supports[j][table[i][j] - x[j].getMin()].set(i);
                }
            }
        }
    }

    @Override
    public void post() throws InconsistencyException {
        for (int i = 0; i < x.length; ++i) {
            deltas[i] = x[i].propagateOnDomainChangeWithDelta(this);
        }

        propagate();
    }


    @Override
    public void propagate() throws InconsistencyException {

        // Store domains to iterate over them twice.
        updateTuples();
        filterDomains();
        this.firstPropagate = false;
    }

    private int updateDomain(int i) {
        if (domains[i] == null || domains[i].length < x[i].getSize()) {
            domains[i] = new int[x[i].getSize()];
        }

        return x[i].fillArray(domains[i]);
    }

    protected void incrementalUpdate(int i) {
        DeltaInt delta = deltas[i];
        if (delta.deltaSize() > 0) {
            for (int v: delta.values()) {
                supportedTuples.addToMask(supports[i][v]);
            }

            supportedTuples.reverseMask();
            supportedTuples.intersectWithMask();
        }
    }

    protected void resetUpdate(int i) {
        for (int j = 0; j < x[i].getSize(); ++j) {
            int v = domains[i][j];
            supportedTuples.addToMask(supports[i][v]);
        }

        supportedTuples.intersectWithMask();
    }

    protected void updateTuples() throws InconsistencyException {
        for (int i = 0; i < x.length; ++i) {
            supportedTuples.clearMask();
            updateDomain(i);

            int deltaSize = deltas[i].deltaSize();
            if (deltaSize < x[i].getSize() && !firstPropagate) {
                incrementalUpdate(i);
            }
//            int[] delta = x[i].delta(lastSizes[i].getValue());
//            if (delta.length < x[i].getSize() && !firstPropagate) {
//            }
            else {
                resetUpdate(i);
            }

            if (supportedTuples.isEmpty()) {
                throw INCONSISTENCY;
            }
        }
    }

    private void filterDomains() throws InconsistencyException {
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].getSize(); j++) {
                int v = domains[i][j];
                int index = supportedTuples.intersectIndex(supports[i][v]);
                if (index == -1) {
                    x[i].remove(v);
                }
                else {
                    residues[i][v] = index;
                }
            }
        }
    }
}
