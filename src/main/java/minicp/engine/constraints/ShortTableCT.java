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
import minicp.engine.core.IntVar;
import minicp.engine.core.delta.DeltaInt;

import java.util.BitSet;

public class ShortTableCT extends TableCT {
    private BitSet[][] supportsStar;
    private int star;

    /**
     * Table constraint. Assignment of x_0=v_0, x_1=v_1,... only valid if there exists a
     * row (v_0|*,v_1|*, ...) in the table.
     *
     * @param x     variables to constraint. x.length must be > 0.
     * @param table array of valid solutions (second dimension must be of same size as the array x)
     * @param star the symbol representing "any" value in the table
     */
    public ShortTableCT(IntVar[] x, int[][] table, int star) {
        super(x, table, false);

        this.star = star;

        supportsStar = new BitSet[x.length][];

        for (int i = 0; i < x.length; i++) {
            supportsStar[i] = new BitSet[x[i].getMax() - x[i].getMin() + 1];

            for (int j = 0; j < supports[i].length; j++) {
                supportsStar[i][j] = new BitSet();
            }
        }

        setupSupports(x, table);
    }

    @Override
    public void setupSupports(IntVar[] x, int[][] table) {
        for (int i = 0; i < table.length; i++) { //i is the index of the tuple (in table)
            for (int j = 0; j < x.length; j++) { //j is the index of the current variable (in x)
                if (table[i][j] == star) {
                    // Set all tuple i for all supports of variable j
                    for (int k = this.x[j].getMin(); k <= this.x[j].getMax(); ++k) {
                        if (this.x[j].contains(k)) {
                            supports[j][k].set(i);
                        }
                    }
                }
                else if (x[j].contains(table[i][j])) {
                    supports[j][table[i][j] - x[j].getMin()].set(i);
                    supportsStar[j][table[i][j] - x[j].getMin()].set(i);
                }
            }
        }
    }

    protected void incrementalUpdate(int i) {
        DeltaInt delta = deltas[i];
        if (delta.deltaSize() > 0) {
            for (int v: delta.values()) {
                supportedTuples.addToMask(supportsStar[i][v]);
            }

            supportedTuples.reverseMask();
            supportedTuples.intersectWithMask();
        }
    }
}
