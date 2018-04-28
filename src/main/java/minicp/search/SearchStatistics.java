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

package minicp.search;


public class SearchStatistics {

    public SearchStatistics merge(SearchStatistics stats) {
        nFailures += stats.nFailures;
        nSolutions += stats.nSolutions;
        nNodes += stats.nNodes;
        completed = stats.completed;
        return this;
    }

    public int nFailures = 0;
    public int nNodes = 0;
    public int nSolutions = 0;
    public boolean completed = false;
    public String toString() {
        return  "\n\t#choice: " + nNodes +
                "\n\t#fail: " + nFailures +
                "\n\t#sols : " + nSolutions +
                "\n\tcompleted : " + completed + "\n";
    }
}
