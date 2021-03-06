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


import minicp.reversible.Trail;
import minicp.reversible.ReversibleInt;
import minicp.search.branching.Branch;
import minicp.util.NotImplementedException;
import minicp.util.InconsistencyException;

/**
 * Branching wrapper that ensures that
 * that the alternatives created are always within the
 * discrepancy limit
 */

public abstract class DiscrepancyChoice {

    ReversibleInt currentDiscrepancy;

    public DiscrepancyChoice(Trail state, Choice choice, int maxD) throws InconsistencyException {
        throw new NotImplementedException();
    }

    public Branch[] getAlternatives() throws InconsistencyException {
        // Hint:
        // Let b.alts denote b.call()
        // Filter-out alternatives from b.alts that would exceed maxDiscrepancy
        // Therefore wrap each alternative in b.alts
        // such that the call method of the wrapped alternatives
        // augment the currentDiscrepancy depending on its position
        // +0 for b.alts[0], ..., +i for b.alts[i]
        throw new NotImplementedException();
    }
}
