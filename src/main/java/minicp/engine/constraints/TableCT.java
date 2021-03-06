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
import minicp.reversible.ReversibleSparseSet;
import minicp.util.BitSetOperations;
import minicp.util.InconsistencyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static minicp.cp.Factory.minus;
import static minicp.util.InconsistencyException.INCONSISTENCY;
import static minicp.util.Sequence.sequence;


public class TableCT extends Constraint {
    protected IntVar[] x; //variables
    protected DeltaInt[] deltas;
    protected long[][][] supports;
    protected ReversibleSparseBitSet validTuples;
    protected int[][] residues;
    // Array to store domains of variables (avoid allocating multiple times)
    protected int domains[][];

    protected boolean firstPropagate = true;

    protected ReversibleSparseSet unboundVariables;
    protected int[] unboundIndices;

//    protected long[] bitset;
    protected int[] deltaValues;

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
        super(x[0].getSolver(), x.length);

        registerVariable(x);

        this.x = new IntVar[x.length];
        this.deltas = new DeltaInt[x.length];
        this.deltaValues = new int[Arrays.stream(x).map(IntVar::getSize).max(Integer::compareTo).get()];
        this.domains = new int[x.length][];
        unboundVariables = new ReversibleSparseSet(cp.getTrail(), x.length);
        unboundIndices = new int[x.length];

        // Allocate supports and residues
        this.supports = new long[x.length][][];
        this.residues = new int[x.length][];

        this.validTuples = new ReversibleSparseBitSet(this.cp.getTrail(), table.length, sequence(0, table.length));
//        this.bitset = new long[validTuples.numberWords()];


        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].getMin()); // map the variables domain to start at 0
            supports[i] = new long[x[i].getMax() - x[i].getMin() + 1][];
            residues[i] = new int[x[i].getMax() - x[i].getMin() + 1];

            for (int j = 0; j < supports[i].length; j++) {
                residues[i][j] = 0;
                supports[i][j] = new long[validTuples.numberWords()];
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
                    BitSetOperations.setBit(supports[j][table[i][j] - x[j].getMin()], i);
//                    supports[j][table[i][j] - x[j].getMin()].set(i);
                }
            }
        }
    }

    @Override
    public void post() throws InconsistencyException {
        for (int i = 0; i < x.length; ++i) {
            deltas[i] = x[i].propagateOnDomainChangeWithDelta(this);
            final int j = i;
            x[i].whenBind(() -> {
                unboundVariables.remove(j);
            });
        }

        propagate();
        updateDeltas();
    }


    @Override
    public void propagate() throws InconsistencyException {
        updateTuples();
        filterDomains();
        this.firstPropagate = false;
    }

    protected int updateDomain(int i) {
        if (domains[i] == null || domains[i].length < x[i].getSize()) {
            domains[i] = new int[x[i].getSize()];
        }

        return x[i].fillArray(domains[i]);
    }

    protected void incrementalUpdate(int i) {
        DeltaInt delta = deltas[i];
        if (delta.deltaSize() > 0) {

            int size = delta.fillArray(deltaValues);
            for (int j = 0; j < size; ++j) {
                int v = deltaValues[j];
                validTuples.addToMask(supports[i][v]);
//                validTuples.addToMask(supports[i][v], bitset);
            }

            validTuples.reverseMask();
            validTuples.intersectWithMask();
        }
    }

    protected void resetUpdate(int i) {
        for (int j = 0; j < x[i].getSize(); ++j) {
            int v = domains[i][j];
            validTuples.addToMask(supports[i][v]);
//            validTuples.addToMask(supports[i][v], bitset);
        }

        validTuples.intersectWithMask();
    }

    protected void updateTuples() throws InconsistencyException {
        for (int i = 0; i < x.length; ++i) {
            validTuples.clearMask();
            updateDomain(i);


            if (firstPropagate) {
                resetUpdate(i);
            }
            else if (deltas[i].changed()) {
                if (deltas[i].deltaSize() < x[i].getSize()) {
                    incrementalUpdate(i);
                }
                else {
                    resetUpdate(i);
                }
            }


            if (validTuples.isEmpty()) {
                throw INCONSISTENCY;
            }
        }

    }

    protected void filterDomains() throws InconsistencyException {
//        boolean allBound = true;

        int unbounds = unboundVariables.fillArray(unboundIndices);
        for (int k = 0; k < unbounds; ++k) {
            int i = unboundIndices[k];

//        for (int i = 0; i < x.length; i++) {
//            if (!x[i].isBound()) {
//                allBound = false;
                int size = x[i].getSize();
                for (int j = 0; j < size; j++) {

                    int v = domains[i][j];

                    int residue = residues[i][v];

                    if (validTuples.emptyIntersection(residue, supports[i][v][residue])) {
                        int index = validTuples.intersectIndex(supports[i][v]);
                        if (index == -1) {
                            x[i].remove(v);
                        }
                        else {
                            residues[i][v] = index;
                        }
                    }
                }
//            }
        }

        if (unboundVariables.isEmpty()) {
            this.deactivate();
        }
    }
}
