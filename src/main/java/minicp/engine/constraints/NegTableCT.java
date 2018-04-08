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
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.engine.constraints;

import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import static minicp.cp.Factory.minus;
import static minicp.util.InconsistencyException.INCONSISTENCY;

public class NegTableCT extends TableCT {

    private long[] bitset;

    /**
     * Negative Table constraint.
     * Assignment of x_0=v_0, x_1=v_1,... is forbidden
     * for every row (v_0, v_1, ...) in the table.
     *
     * @param x     variables to constraint. x.length must be > 0.
     * @param table array of forbidden solutions (second dimension must be of same size as the array x)
     */
    public NegTableCT(IntVar[] x, int[][] table) {
        super(x, table, false);

        bitset = new long[validTuples.numberWords()];


        // remove duplicate (the negative ct algo does not support it)
        ArrayList<int[]> tableList = new ArrayList<>();
        boolean [] duplicate = new boolean[table.length];
        for (int i = 0; i < table.length; i++) {
            if (!duplicate[i]) {
                tableList.add(table[i]);
                for (int j = i + 1; j < table.length; j++) {
                    if (i != j & !duplicate[j]) {
                        boolean same = true;
                        for (int k = 0; k < x.length; k++) {
                            same &= table[i][k] == table[j][k];
                        }
                        if (same) {
                            duplicate[j] = true;
                        }
                    }
                }
            }
        }

        setupSupports(x, tableList.toArray(new int[0][]));
    }


    @Override
    protected void filterDomains() throws InconsistencyException {

        int domainSizesProduct = computeDomainSizeProduct();
        int[] sizes = new int[x.length];
        for (int i = 0; i < x.length; i++) {
            if (!x[i].isBound()) {
                sizes[i] = domainSizesProduct / x[i].getSize();

                int size = x[i].getSize();
                for (int j = 0; j < size; j++) {

                    int v = domains[i][j];
                    validTuples.convert(supports[i][v], bitset);

                    if (validTuples.intersectionCardinality(bitset) == sizes[i]) {

                        x[i].remove(v);

                        validTuples.clearMask();
                        validTuples.addToMask(bitset);
                        validTuples.reverseMask();
                        validTuples.intersectWithMask();

                        domainSizesProduct = computeDomainSizeProduct();
                        sizes[i] = domainSizesProduct / x[i].getSize();
                    }
                }
            }
        }
    }


    @Override
    protected void updateTuples() throws InconsistencyException {


        for (int i = 0; i < x.length; ++i) {
            validTuples.clearMask();
            updateDomain(i);

            if (firstPropagate) {
                resetUpdate(i);
            }
            else {
                if (deltas[i].changed()) {
                    int deltaSize = deltas[i].deltaSize();
                    if (deltaSize < x[i].getSize()) {
                        incrementalUpdate(i);
                    }
                    else {
                        resetUpdate(i);
                    }
                }
            }
        }

        int domainSizesProduct = computeDomainSizeProduct();
        if (validTuples.cardinality() == domainSizesProduct) {
            throw INCONSISTENCY;
        }
    }

    private int numberBits1(BitSet set) {
        return set.cardinality();
    }

    private int numberBits1(long[] set) {
        int cpt = 0;
        for (int i = 0; i < set.length; ++i) {
            cpt += Long.bitCount(set[i]);
        }

        return cpt;
    }

    private int computeDomainSizeProduct() {
        int product = 1;
        for (IntVar var : x) {
            product *= var.getSize();
        }
        return product;
    }
}
