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

import javafx.util.Pair;
import minicp.reversible.ReversibleInt;
import minicp.reversible.Trail;
import minicp.util.InconsistencyException;

import java.util.*;

public class DFSearch {

    private Choice choice;
    private Trail state;

    private List<SolutionListener> solutionListeners = new LinkedList<SolutionListener>();
    private List<FailListener> failListeners = new LinkedList<FailListener>();




    @FunctionalInterface
    public interface SolutionListener {
        void solutionFound();
    }
    public DFSearch onSolution(SolutionListener listener) {
        solutionListeners.add(listener);
        return this;
    }

    public void notifySolutionFound() {
        solutionListeners.forEach(s -> s.solutionFound());
    }

    @FunctionalInterface
    public interface FailListener {
        void failure();
    }

    public DFSearch onFail(FailListener listener) {
        failListeners.add(listener);
        return this;
    }

    public void notifyFailure() {
        failListeners.forEach(s -> s.failure());
    }

    public DFSearch(Trail state, Choice branching) {
        this.state = state;
        this.choice = branching;
    }

    public SearchStatistics start(SearchLimit limit) {
        SearchStatistics statistics = new SearchStatistics();
        int level = state.getLevel();
        try {
            dfs(statistics,limit);
        } catch (StopSearchException e) {}
        state.popUntil(level);
        return statistics;
    }

    public SearchStatistics start() {
        return start(statistics -> false);
    }


//    private void dfs(SearchStatistics statistics, SearchLimit limit) {
//        if (limit.stopSearch(statistics)) throw new StopSearchException();
//        Alternative [] alternatives = choice.call();
//        if (alternatives.length == 0) {
//            statistics.nSolutions++;
//            notifySolutionFound();
//        }
//        else {
//            for (Alternative alt : alternatives) {
//                state.push();
//                try {
//                    statistics.nNodes++;
//                    alt.call();
//                    dfs(statistics,limit);
//                } catch (InconsistencyException e) {
//                    notifyFailure();
//                    statistics.nFailures++;
//                }
//                state.pop();
//            }
//        }
//    }

    private void dfs(SearchStatistics statistics, SearchLimit limit) {

        Stack<Pair<Integer, Alternative>> alternatives = new Stack<>();

        int level = 0;

        do {
            if (limit.stopSearch(statistics)) {
                throw new StopSearchException();
            }

            if (alternatives.size() > 0) {
                state.push();
                try {
                    level++;
                    Alternative alternative = alternatives.pop().getValue();
                    alternative.call();
                    statistics.nNodes++;
                }
                catch (InconsistencyException e) {
                    statistics.nFailures++;
                    notifyFailure();
                    level = alternatives.size() > 0 ? alternatives.peek().getKey() : 0;
                    state.popUntil(level - 1);
                }
            }

            Alternative[] alt = choice.call();
            if (alt.length == 0) {
                statistics.nSolutions++;
                notifySolutionFound();

                level = alternatives.size() > 0 ? alternatives.peek().getKey() : 0;
                state.popUntil(level - 1);
            }
            else {
                for (Alternative alternative : alt) {
                    alternatives.push(new Pair<>(level, alternative));
                }
            }
        } while(alternatives.size() > 0);
    }
}



