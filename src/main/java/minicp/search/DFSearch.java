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

import minicp.reversible.ReversibleInt;
import minicp.reversible.Trail;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import java.util.*;

public class DFSearch {

    private Choice choice;
    private Trail trail;

    private List<SolutionListener> solutionListeners = new LinkedList<>();
    private List<FailListener> failListeners = new LinkedList<>();

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
        this.trail = state;
        this.choice = branching;
    }

    public SearchStatistics start(SearchLimit limit) {
        SearchStatistics statistics = new SearchStatistics();
        int level = trail.getLevel();
        try {
            dfs(statistics,limit);
        }
        catch (StopSearchException e) {}
        catch (StackOverflowError e) {
            throw new NotImplementedException("dfs with explicit stack needed");
        }
        trail.popUntil(level);
        return statistics;
    }

    public SearchStatistics start() {
        return start(statistics -> false);
    }

    public void dfs(SearchStatistics statistics, SearchLimit limit) {
        Stack<Alternative> alternatives = new Stack<>();
        expandNode(alternatives, statistics);

        while(!alternatives.isEmpty()) {
            if (limit.stopSearch(statistics)) throw new StopSearchException();
            try {
                alternatives.pop().call();
            }
            catch (InconsistencyException e) {
                notifyFailure();
                statistics.nFailures++;
            }
        }
     }

     private void expandNode(Stack<Alternative> alternatives, SearchStatistics statistics) {
        Alternative[] alts = choice.call();

        if (alts.length == 0) {
            alternatives.push(() -> {
                statistics.nSolutions++;
                notifySolutionFound();
            });
        }

        for (int i = alts.length - 1; i >= 0; i--) {
            Alternative alt = alts[i];

            alternatives.push(() -> {
                trail.pop();
            });

            alternatives.push(() -> {
                alt.call();
                expandNode(alternatives, statistics);
                statistics.nNodes++;
            });

            alternatives.push(() -> {
                trail.push();
            });
        }
     }
}



