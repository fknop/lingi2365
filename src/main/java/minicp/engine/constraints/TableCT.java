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
import minicp.engine.core.IntVarImpl;
import minicp.reversible.ReversibleInt;
import minicp.reversible.ReversibleSparseBitSet;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import static minicp.cp.Factory.*;

import java.util.*;

// TODO: build incremental version
public class TableCT extends Constraint {
    private IntVar[] x; //variables
    private int[][] table; //the table
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] supports;
    private int[][] residues;
    private boolean firstPropagate = true;

    private ReversibleInt[] lastSizes;

    private ReversibleSparseBitSet supportedTuples;

    // Array to store domains of variables (avoid allocating multiple times)
    private int domains[][];

    /**
     * Table constraint.
     * Assignment of x_0=v_0, x_1=v_1,... only valid if there exists a
     * row (v_0, v_1, ...) in the table.
     *
     * @param x     variables to constraint. x.length must be > 0.
     * @param table array of valid solutions (second dimension must be of same size as the array x)
     */
    public TableCT(IntVar[] x, int[][] table) {
        super(x[0].getSolver());
        this.x = new IntVar[x.length];
        List<Integer> initial = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            initial.add(i);
        }

        this.domains = new int[x.length][];
        this.supportedTuples = new ReversibleSparseBitSet(x[0].getSolver().getTrail(), table.length, initial);
        this.table = table;

        // Allocate supports and residues
        supports = new BitSet[x.length][];
        residues = new int[x.length][];
        lastSizes = new ReversibleInt[x.length];


        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].getMin()); // map the variables domain to start at 0
            lastSizes[i] = new ReversibleInt(x[i].getSolver().getTrail(), x[i].getSize());
            supports[i] = new BitSet[x[i].getMax() - x[i].getMin() + 1];
            residues[i] = new int[x[i].getMax() - x[i].getMin() + 1];

            for (int j = 0; j < supports[i].length; j++) {
                residues[i][j] = 0;
                supports[i][j] = new BitSet(table.length);
            }
        }

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
        for (IntVar var : x)
            var.propagateOnDomainChange(this);
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

    private void updateTuples() throws InconsistencyException {
        for (int i = 0; i < x.length; ++i) {
            supportedTuples.clearMask();
            int size = updateDomain(i);

            int[] delta = x[i].delta(lastSizes[i].getValue());
            if (delta.length < x[i].getSize() && !firstPropagate) {
                for (int v: delta) {
                    supportedTuples.addToMask(supports[i][v]);
                }

                if (delta.length > 0) {
                    supportedTuples.reverseMask();
                    supportedTuples.intersectWithMask();
                }
            }
            else {
                for (int j = 0; j < size; ++j) {
                    int v = domains[i][j];
                    supportedTuples.addToMask(supports[i][v]);
                }

                supportedTuples.intersectWithMask();
            }

            lastSizes[i].setValue(x[i].getSize());

            if (supportedTuples.isEmpty()) {
                throw new InconsistencyException();
            }
        }
    }

    private void filterDomains() throws InconsistencyException {
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < domains[i].length; j++) {
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
