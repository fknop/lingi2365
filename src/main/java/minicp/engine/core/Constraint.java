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
import minicp.engine.core.delta.Delta;
import minicp.reversible.ReversibleBool;
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;

import java.util.ArrayList;
import java.util.List;

public abstract class Constraint {


    protected final Solver cp;
    protected boolean scheduled = false;
    protected final ReversibleBool active;

    private List<Delta> deltas = new ArrayList<>();
    private List<Var> variables = new ArrayList<>();
    protected int arity;

//    private ReversibleInt failureCount;
    private int failureCount = 0;

    public Constraint(Solver cp) {
        this(cp, 0);
    }

    public Constraint(Solver cp, int arity) {
        this.arity = arity;
        this.cp = cp;
        active = new ReversibleBool(cp.getTrail(),true);
    }

    public int getArity() {
        return arity;
    }

    void registerDelta(Delta delta) {
        deltas.add(delta);
        delta.update();
    }

    protected void registerVariable(Var var) {
        this.variables.add(var);
        var.register(this);

    }

    protected void registerVariable(Var... vars) {
        for (Var var: vars) {
            registerVariable(var);
        }
    }

    protected void updateDeltas() {
        int size = deltas.size();
        for (int i = 0; i < size; ++i) {
            deltas.get(i).update();
        }
    }


    public void notifyFailure() {
        ++this.failureCount;
        int size = variables.size();
        if (arity > 1) {
            for (int i = 0; i < size; ++i) {
                variables.get(i).addFailure();
            }
        }
    }

    public boolean isActive() {
        return active.getValue();
    }

    public void deactivate() {
        active.setValue(false);
    }

    public abstract void post() throws InconsistencyException;
    public void propagate() throws InconsistencyException {}



    void execute() throws InconsistencyException {
        propagate();
        updateDeltas();
    }
}
