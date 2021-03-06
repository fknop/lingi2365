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

package minicp.examples;

import minicp.engine.constraints.Element1D;
import minicp.engine.constraints.Element1DVar;
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.InconsistencyException;

import java.util.Arrays;

import static minicp.cp.Factory.*;
import static minicp.cp.Heuristics.and;
import static minicp.cp.Heuristics.firstFail;

/**
 * Stable Marriage problem:
 * Given n men and n women, where each person has ranked all members
 * of the opposite sex with a unique number between 1 and n in order of preference,
 * marry the men and women together such that there are no two people of opposite sex
 * who would both rather have each other than their current partners.
 * If there are no such people, all the marriages are "stable".
 * Wikipedia: http://en.wikipedia.org/wiki/Stable_marriage_problem
 */
public class StableMariage {


    public static void main(String[] args) throws InconsistencyException {


        // http://mathworld.wolfram.com/StableMarriageProblem.html
        // for each man, what is his ranking for the women (lower is better)
        int[][] rankWomen = new int[][]{
                {7, 3, 8, 9, 6, 4, 2, 1, 5},
                {5, 4, 8, 3, 1, 2, 6, 7, 9},
                {4, 8, 3, 9, 7, 5, 6, 1, 2},
                {9, 7, 4, 2, 5, 8, 3, 1, 6},
                {2, 6, 4, 9, 8, 7, 5, 1, 3},
                {2, 7, 8, 6, 5, 3, 4, 1, 9},
                {1, 6, 2, 3, 8, 5, 4, 9, 7},
                {5, 6, 9, 1, 2, 8, 4, 3, 7},
                {6, 1, 4, 7, 5, 8, 3, 9, 2}};

        // for each woman, what is her ranking for the men (lower is better)
        int[][] rankMen = new int[][]{
                {3, 1, 5, 2, 8, 7, 6, 9, 4},
                {9, 4, 8, 1, 7, 6, 3, 2, 5},
                {3, 1, 8, 9, 5, 4, 2, 6, 7},
                {8, 7, 5, 3, 2, 6, 4, 9, 1},
                {6, 9, 2, 5, 1, 4, 7, 3, 8},
                {2, 4, 5, 1, 6, 8, 3, 9, 7},
                {9, 3, 8, 2, 7, 5, 4, 6, 1},
                {6, 3, 2, 1, 8, 4, 5, 9, 7},
                {8, 2, 6, 4, 9, 1, 3, 7, 5}};

        // you should get six solutions:
        /*
        wife   :5,3,8,7,2,6,0,4,1
        husband:6,8,4,1,7,0,5,3,2

        wife   :5,4,8,7,2,6,0,3,1
        husband:6,8,4,7,1,0,5,3,2

        wife   :5,0,3,7,4,8,2,1,6
        husband:1,7,6,2,4,0,8,3,5

        wife   :5,0,3,7,4,6,2,1,8
        husband:1,7,6,2,4,0,5,3,8

        wife   :5,3,0,7,4,6,2,1,8
        husband:2,7,6,1,4,0,5,3,8

        wife   :6,4,8,7,2,5,0,3,1
        husband:6,8,4,7,1,5,0,3,2
        */

        int n = rankMen.length;

        Solver cp = new Solver();

        // wife[m] is the woman chosen for man m
        IntVar [] wife = makeIntVarArray(cp,n,n);
        // husband[w] is the man chosen for woman w
        IntVar [] husband = makeIntVarArray(cp,n,n);

        // wifePref[m] is the preference for the woman chosen for man m
        IntVar [] wifePref = makeIntVarArray(cp,n,n+1);
        // husbandPref[w] is the preference for the man chosen for woman w
        IntVar [] husbandPref = makeIntVarArray(cp,n,n+1);



        for (int m = 0; m < n; m++) {
            // the husband of the wife of man m is m
            cp.post(new Element1DVar(husband, wife[m], m));
            // TODO: model this with Element1DVar

            cp.post(new Element1D(rankWomen[m], wife[m], wifePref[m]));
            // rankWomen[m][wife[m]] == wifeFref[m]
            // TODO: model this with Element1D


        }

        for (int w = 0; w < n; w++) {
            cp.post(new Element1DVar(wife, husband[w], w));
            // the wife of the husband of woman w is w
            // TODO: model this with Element1DVar

            cp.post(new Element1D(rankMen[w], husband[w], husbandPref[w]));
            // rankMen[w][husband[w]] == husbandPref[w]
            // TODO: model this with Element1D
        }

        for (int m = 0; m < n; m++) {
            for (int w = 0; w < n; w++) {
                // if m prefers w than his wife, the opposite is not true i.e. w prefers her own husband than m
                // (wifePref[m] > rankWomen[m][w]) => (husbandPref[w] < rankMen[w][m])

                BoolVar mPrefersW = isLarger(wifePref[m],rankWomen[m][w]);
                BoolVar wDont = isLess(husbandPref[w],rankMen[w][m]);
                cp.post(implies(mPrefersW,wDont));

                BoolVar wPrefersM = isLarger(husbandPref[w], rankMen[w][m]);
                BoolVar mDont = isLess(wifePref[m], rankWomen[m][w]);
                cp.post(implies(wPrefersM, mDont));
                // if w prefers m than her husband, the opposite is not true i.e. m prefers his own woman than w
                // (husbandPref[w] > rankMen[w][m]) => (wifePref[m] < rankWomen[m][w])
                // TODO: model this constraint

            }
        }


        DFSearch dfs = makeDfs(cp,
                and(firstFail(wife), firstFail(husband)));

        dfs.onSolution(() -> {
                    System.out.println(Arrays.toString(wife));
                    System.out.println(Arrays.toString(husband));
                }
        );


        SearchStatistics stats = dfs.start();
        System.out.println(stats);

    }

    /**
     *
     * @param b1
     * @param b2
     * @return b equiv (b1 => b2) (logical implication)
     */
    public static BoolVar implies(BoolVar b1, BoolVar b2) throws InconsistencyException {
        IntVar not_b1 = plus(minus(b1),1);
        return isLargerOrEqual(sum(not_b1,b2),1);
    }
}

