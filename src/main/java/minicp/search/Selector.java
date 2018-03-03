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

public class Selector {

    public static final Alternative[] TRUE = new Alternative[0];

    public static Alternative[] branch(Alternative... alternatives) {
        return alternatives;
    }

    @FunctionalInterface
    public interface Filter<T> {
        boolean call(T x);
    }

    @FunctionalInterface
    public interface ValueFun<T> {
        float call(T x);
    }

    @FunctionalInterface
    public interface ValueFunIndexed<T> {
        float call(T x, int i);
    }



    @FunctionalInterface
    public interface BranchOn<T> {
        Alternative[] call(T x);
    }

    @FunctionalInterface
    public interface BranchOnIndexed<T> {
        Alternative[] call(T x, int i);
    }

    public static <T> Choice selectMin(T[] x, Filter<T> p, ValueFun<T> f, BranchOn<T> body) {
        return () -> {
            T sel = null;
            for (T xi : x) {
                if (p.call(xi)) {
                    sel = sel == null || (f.call(xi) < f.call(sel)) ? xi : sel;
                }
            }
            if (sel == null) {
                return TRUE;
            } else {
                return body.call(sel);
            }
        };
    }

    public static <T> Choice selectMinIndexed(T[] x, Filter<T> p, ValueFunIndexed<T> f, BranchOnIndexed<T> body) {
        return () -> {
            T sel = null;
            float best = Float.MAX_VALUE;
            int selIndex = 0;

            for (int i = 0; i < x.length; ++i) {
                if (p.call(x[i])) {
                    float value = f.call(x[i], i);
                    if (value < best) {
                        best = value;
                        sel = x[i];
                        selIndex = i;
                    }
                }
            }
            if (sel == null) {
                return TRUE;
            } else {
                return body.call(sel, selIndex);
            }
        };
    }



}
