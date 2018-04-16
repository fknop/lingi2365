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

package minicp.reversible;


import minicp.util.IntStack;

import java.util.ArrayList;

public class ReversibleInt implements RevInt {

    @FunctionalInterface()
    public interface OnBacktrack {
        void call(int oldValue, int newValue);
    }

    private Trail trail;
    private int v;
    private long lastMagic = -1L;

    private ArrayList<OnBacktrack> listeners = new ArrayList<>();
    private IntStack trailEntries;

    public ReversibleInt(Trail trail, int initial) {
        this.trail = trail;
        v = initial;
        lastMagic = trail.magic;
        this.trailEntries = new IntStack();
    }

    private void trail() {
        long trailMagic = trail.magic;
        if (lastMagic != trailMagic) {
            lastMagic = trailMagic;
            this.trailEntries.push(v);
            trail.pushOnTrail(this);
        }
    }

    @Override
    public void restore() {
        int old = this.v;
        this.v = trailEntries.pop();
        notifyBacktrack(old, this.v);
    }


    public void onBacktrack(OnBacktrack listener) {
        listeners.add(listener);
    }

    private void notifyBacktrack(int oldValue, int newValue) {
        if (listeners.isEmpty()) {
            return;
        }

        int size = listeners.size();
        for (int i = 0; i < size; ++i) {
            listeners.get(i).call(oldValue, newValue);
        }
    }

    public int setValue(int v) {
        if (v != this.v) {
            trail();
            this.v = v;
        }
        return this.v;
    }

    public int increment() { return setValue(getValue()+1);}
    public int decrement() { return setValue(getValue()-1);}
    public int getValue()  { return this.v; }

    @Override
    public String toString() {
        return ""+v;
    }
}
