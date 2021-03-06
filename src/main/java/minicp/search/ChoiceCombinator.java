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

package minicp.search;


import minicp.search.branching.Branch;
import minicp.search.branching.Branching;

public class ChoiceCombinator implements Choice {

    private minicp.search.Choice [] choices;

    public ChoiceCombinator(minicp.search.Choice ... choices) {
        this.choices = choices;
    }

    @Override
    public Branch[] call() {
        Branch[] alternatives = Branching.LEAF;
        for (Choice choice : choices) {
            Branch[] alts = choice.call();
            if (alts.length > 0) {
                alternatives = alts;
                break;
            }
        }

        return alternatives;
    }
}
