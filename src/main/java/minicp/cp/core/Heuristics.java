/*
 * This file is part of mini-cp.
 *
 * mini-cp is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mini-cp.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2016 L. Michel, P. Schaus, P. Van Hentenryck
 */

package minicp.cp.core;


import minicp.cp.constraints.DifferentVal;
import minicp.cp.constraints.EqualVal;
import minicp.search.Choice;

import static minicp.search.Selector.branch;
import static minicp.search.Selector.selectMin;

public class Heuristics {

    public static Choice firstFail(IntVar[] x) {
        Solver cp = x[0].getSolver();
        return selectMin(x,
                xi -> xi.getSize() > 1,
                xi -> xi.getSize(),
                xi -> {
                    int v = xi.getMin();
                    return branch(
                            () -> {
                                cp.add(new EqualVal(xi, v));
                            },
                            () -> {
                                cp.add(new DifferentVal(xi, v));
                            }
                    );
                }
        );
    }


}
