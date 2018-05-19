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
 * aint with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.util;

import java.util.*;
import java.util.function.BiConsumer;

// class written by Guillaume Derval
public class GraphUtil {
    public static interface Graph {
        /**
         * @return the number of nodes in this graph. They are indexed from 0 to n-1.
         */
        int n();

        /**
         * @param idx the node to consider
         * @return the nodes ids that have an edge going from then to node idx
         */
        Iterable<Integer> in(int idx);

        /**
         * @param idx the node to consider
         * @return the nodes ids that have an edge going from node idx to them.
         */
        Iterable<Integer> out(int idx);
    }

    public static class DirectedGraph implements Graph {

        private ArrayList<Set<Integer>> in;
        private ArrayList<Set<Integer>> out;
        private int n;

        public DirectedGraph(int n) {
            this.n = n;
            in = new ArrayList<>();
            out = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                in.add(new HashSet<>());
                out.add(new HashSet<>());
            }
        }

        public void clear() {
            for (int i = 0; i < n; ++i) {
                in.get(i).clear();
                out.get(i).clear();
            }
        }

        // Add an edge from i to j
        public void link(int i, int j) {
            out.get(i).add(j);
            in.get(j).add(i);
        }

        // Remove an edge from i to j
        public void unlink(int i, int j) {
            out.get(i).remove(j);
            in.get(j).remove(i);
        }

        /**
         * @return the number of nodes in this graph. They are indexed from 0 to n-1.
         */
        @Override
        public int n() {
            return n;
        }

        /**
         * @param idx the node to consider
         * @return the nodes ids that have an edge going from then to node idx
         */
        @Override
        public Iterable<Integer> in(int idx) {
            return in.get(idx);
        }

        /**
         * @param idx the node to consider
         * @return the nodes ids that have an edge going from node idx to them.
         */
        @Override
        public Iterable<Integer> out(int idx) {
            return out.get(idx);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < n; ++i) {
                builder.append("x[").append(i).append("];").append("\n");

                builder.append("in: ");

                for (int in: in(i)) {
                    builder.append(in).append(" ");
                }
                builder.append("\n");

                builder.append("out: ");
                for (int out: out(i)) {
                    builder.append(out).append(" ");
                }
                builder.append("\n");
            }

            return builder.toString();
        }
    }

    /**
     * Transpose the graph
     *
     * @param graph
     * @return
     */
    public static Graph transpose(Graph graph) {
        return new Graph() {
            @Override
            public int n() {
                return graph.n();
            }

            @Override
            public Iterable<Integer> in(int idx) {
                return graph.out(idx);
            }

            @Override
            public Iterable<Integer> out(int idx) {
                return graph.in(idx);
            }
        };
    }

    /**
     * Returns the SCC of the graph
     * For at each index, an integer representing the scc id of the node
     */
    public static int[] stronglyConnectedComponents(Graph graph) {
        //Compute the suffix order
        Stack<Integer> firstOrder = new Stack<>();
        int[] visited = new int[graph.n()];
        Arrays.fill(visited, 0);
        for (int i = 0; i < graph.n(); i++) {
            if (visited[i] == 0) {
                dfsNode(graph, (suffix, b) -> {if(suffix) firstOrder.push(b);}, visited, i);
            }
        }

        //Reverse the order, and do the dfs of the transposed graph
        Arrays.fill(visited, 0);
        int [] scc = new int[graph.n()];
        Counter cpt = new Counter();
        Graph tranposed = GraphUtil.transpose(graph);

        while (!firstOrder.empty()) {
            int next = firstOrder.pop();
            if(visited[next] == 0) {
                cpt.incr();
                dfsNode(tranposed, (suffix, x) -> {if(!suffix) scc[x] = cpt.getValue();}, visited, next);
            }
        }
        return scc;
    }

    private static void dfsNode(Graph graph, BiConsumer<Boolean, Integer> action, int[] visited, int start) {
        Stack<Integer> todo = new Stack<>();
        todo.add(start);

        // seen = 1
        // visited = 2
        // closed = 3
        visited[start] = 1; //seen
        while (!todo.isEmpty()) {
            int cur = todo.peek();
            if(visited[cur] == 1) {
                action.accept(false, cur);
                for (int next : graph.out(cur)) {
                    if (visited[next] == 0) {
                        todo.add(next);
                        visited[next] = 1; //seen
                    }
                }
                visited[cur] = 2; //visited
            }
            else if(visited[cur] == 2) {
                action.accept(true, cur);
                visited[cur] = 3; //closed
                todo.pop();
            }
        }
    }
}
