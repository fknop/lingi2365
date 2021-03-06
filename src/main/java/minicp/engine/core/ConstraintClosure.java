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

package minicp.engine.core;


import minicp.util.InconsistencyException;

public class ConstraintClosure extends Constraint {

    @FunctionalInterface
    public interface Filtering {
        void call() throws InconsistencyException;
    }

    private final Filtering filtering;

    public ConstraintClosure(Solver cp, Filtering filtering) {
        super(cp, 0);
        this.filtering = filtering;
    }

    @Override
    public void post() throws InconsistencyException {

    }

    @Override
    public void propagate() throws InconsistencyException {
        filtering.call();
    }
}
