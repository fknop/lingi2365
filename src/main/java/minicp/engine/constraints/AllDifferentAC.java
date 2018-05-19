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
import minicp.reversible.ReversibleSparseSet;
import minicp.util.GraphUtil;
import minicp.util.GraphUtil.*;
import minicp.util.InconsistencyException;

import static minicp.util.InconsistencyException.INCONSISTENCY;

/**
 * Implementation of the algorithm described in
 * "A filtering algorithm for constraints of difference in CSPs" J-C. Régin, AAAI-94
 * Hint: use MaximumMatching and GraphUtil.stronglyConnectedComponents
 */
public class AllDifferentAC extends Constraint {

    private IntVar[] x;
    private DirectedGraph graph;
    private MaximumMatching matching;
    private int[] match;

    private int[] domain;

    private int min;
    private int max;
    private int nVal;

    private ReversibleSparseSet unboundIndices;
    private int[] unbounds;

    public AllDifferentAC(IntVar... x) {
        super(x[0].getSolver());
        this.x = x;

        unboundIndices = new ReversibleSparseSet(cp.getTrail(), x.length);
        unbounds = new int[x.length];

        matching = new MaximumMatching(x);
        match = new int[x.length];

        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;

        int maxSize = Integer.MIN_VALUE;
        for (int i = 0; i < x.length; i++) {
            min = Math.min(min, x[i].getMin());
            max = Math.max(max, x[i].getMax());
            maxSize = Math.max(maxSize, x[i].getSize());
        }

        nVal = max - min + 1;
        domain = new int[maxSize];
        graph = new DirectedGraph(x.length + nVal + 1);
    }

    // Node ID - N + Min Value = Value
    // Node ID = Value + N - Min Value
    private int getNodeId(int value) {
        return value + x.length - min;
    }


    @Override
    public void post() throws InconsistencyException {
        for (int i = 0; i < x.length; ++i) {
            final int j = i;
            x[i].whenBind(() -> {
                unboundIndices.remove(j);
            });

            if (x[i].isBound()) {
                unboundIndices.remove(i);
            }

            x[i].propagateOnDomainChange(this);
        }

        propagate();
    }



    @Override
    public void propagate() throws InconsistencyException {

        int sizeMatching = matching.compute(match);
        if (sizeMatching < x.length) {
            throw INCONSISTENCY;
        }

        buildGraph();
        int[] components = GraphUtil.stronglyConnectedComponents(graph);

        int nUnbound = unboundIndices.fillArray(unbounds);
        for (int k = 0; k < nUnbound; ++k) {
            int i = unbounds[k];

            int size = x[i].fillArray(domain);
            for (int j = 0; j < size; ++j) {
                int v = domain[j];
                // If the value is not the value from the maximum matching and if the component
                // of the variable is different from the component of the value
                if (match[i] != v && components[i] != components[getNodeId(v)]) {
                    x[i].remove(v);
                }
            }
        }
    }

    private void buildGraph() {
        graph.clear();
        int dummy = (x.length + nVal);
        for (int v = min; v <= max; ++v) {
            int id = getNodeId(v);

            // Link value to dummy
            graph.link(id, dummy);
        }

        for (int i = 0; i < x.length; ++i) {
            int v = match[i];
            int id = getNodeId(v);
            // Link matching val to variable
            graph.link(id, i);

            // unlink matched value to dummy
            graph.unlink(id, dummy);

            // Link dummy to value
            graph.link(dummy, id);

            int size = x[i].fillArray(domain);
            for (int j = 0; j < size; ++j) {
                if (domain[j] != v) {

                    // Match variable to node value
                    graph.link(i, getNodeId(domain[j]));
                }
            }
        }
    }
}
