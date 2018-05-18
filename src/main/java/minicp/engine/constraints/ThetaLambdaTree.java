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

package minicp.engine.constraints;

/**
 * Data Structure described in
 * Global Constraints in Scheduling, 2008 Petr Vilim, PhD thesis
 * http://vilim.eu/petr/disertace.pdf
 */
public class ThetaLambdaTree {

    private class Node {

        private int sump;
        private int ect;

        private int sumpBar;
        private int ectBar;

        private int responsibleEctBar;
        private int responsibleSumpBar;

        public Node() {
            reset();
        }

        public void reset() {
            setECT(Integer.MIN_VALUE);
            setSUMP(0);

            setECTBar(Integer.MIN_VALUE);
            setSUMPbar(0);
        }

        public int getECT() {
            return ect;
        }

        public int getECTBar() {
            return ectBar;
        }

        public int getSUMP() {
            return sump;
        }

        public int getSUMPBar() {
            return sumpBar;
        }

        public void setECT(int ect) {
            this.ect = ect;
        }

        public void setECTBar(int ect) {
            this.ectBar = ect;
        }

        public void setSUMP(int sump) {
            this.sump = sump;
        }


        public void setSUMPbar(int sump) {
            this.sumpBar = sump;
        }

        public boolean isEmpty() {
            return ect == Integer.MIN_VALUE && sump == 0;
        }

        public int getResponsibleEctBar() {
            return responsibleEctBar;
        }

        public void setResponsibleEctBar(int responsibleEctBar) {
            this.responsibleEctBar = responsibleEctBar;
        }

        public int getResponsibleSumpBar() {
            return responsibleSumpBar;
        }

        public void setResponsibleSumpBar(int responsibleSumpBar) {
            this.responsibleSumpBar = responsibleSumpBar;
        }
    }

    private Node [] nodes;
    private int isize; //number of internal nodes
    private int size;
    private boolean useGrayNodes = false;

    /**
     * Create a theta-tree with a number the least number leaf-nodes >= size
     * @param size the number of activities that can be inserted in the leaf nodes
     */
    public ThetaLambdaTree(int size) {
        // http://en.wikipedia.org/wiki/Binary_heap#Adding_to_the_heap
        this.size = size;
        isize = 1;
        //enumerate multiples of two 2, 4, 6, 8 ... until isize larger than size
        while (isize < size) {
            isize <<= 1; //shift the pattern to the left by 1 (i.e. multiplies by 2)
        }
        //number of nodes in a complete  binary tree with isize leaf nodes is (isize*2)-1
        nodes = new Node[(isize * 2) -1];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node();
        }
        isize--;
    }

    /**
     * Remove all the information in the theta-tree
     */
    public void reset() {
        for (Node n: nodes) {
            n.reset();
        }

        useGrayNodes = false;
    }

    public void useGrayNodes() {
        this.useGrayNodes = true;
    }

    /**
     * Insert activity in leaf nodes at position pos
     * @param pos index of the leaf nodes (assumed to start at 0 from left to right)
     * @param ect earliest completion time
     * @param dur duration
     */
    public void insert(int pos, int ect, int dur) {
        //the last size nodes are the leaf nodes so the first one is isize (the number of internal nodes)
        int currPos = isize + pos;
        Node node = nodes[currPos];
        node.setECT(ect);
        node.setSUMP(dur);
        reCompute(getFather(currPos));
    }

    public void setGrayActivity(int pos) {
        int currPos = isize + pos;
        nodes[currPos].setSUMP(0);
        nodes[currPos].setECT(Integer.MIN_VALUE);
        nodes[currPos].setResponsibleEctBar(currPos);
        nodes[currPos].setResponsibleSumpBar(currPos);

        reCompute(getFather(currPos));
    }

    /**
     * Remove activity at position pos such that it has no impact on the earliest completion time computation
     * @param pos index of the leaf nodes (assumed to start at 0 from left to right)
     */
    public void remove(int pos) {
        int currPos = isize + pos;
        Node node = nodes[currPos];
        node.reset();
        reCompute(getFather(currPos));
    }

    public boolean isPresent(int pos) {
        int currPos = isize + pos;
        return !nodes[currPos].isEmpty();
    }

    private int getECT(int pos) {
        return nodes[pos].getECT();
    }



    /**
     * The earliest completion time of the activities present in the theta-tree
     * @return
     */
    public int getECT() {
        return getECT(0);
    }

    public int getECTBar() {
        return nodes[0].getECTBar();
    }

    public int responsibleEctBar() {
        return nodes[0].getResponsibleEctBar();
    }

    private int getSUMP(int pos) {
        return nodes[pos].getSUMP();
    }

    private int getFather(int pos) {
        //the father of node in pos is (pos-1)/2
        return (pos - 1) >> 1;
    }

    private int getLeft(int pos) {
        //the left child of pos is pos*2+1
        return (pos << 1) + 1;
    }

    private int getRight(int pos) {
        //the right child of pos is (pos+1)*2
        return (pos + 1) << 1;
    }

    private void reComputeAux(int pos) {

        int left = getLeft(pos);
        int right = getRight(pos);

        int pl = getSUMP(left);
        int pr = getSUMP(right);
        nodes[pos].setSUMP(pl + pr);

        int el = getECT(left);
        int er = getECT(right);
        int en = Math.max(er, el + pr);
        nodes[pos].setECT(en);

        if (useGrayNodes) {
            int sumLeftGray = nodes[left].getSUMPBar() + pr;
            int sumRightGray = pl + nodes[right].getSUMPBar();

            int ectBarRightResponsible = nodes[right].getECTBar();
            int ectBarRightDurationResponsible = el + nodes[right].getSUMPBar();
            int ectBarLeftResponsible = nodes[left].getECTBar() + pr;

            if (sumLeftGray > sumRightGray) {
                nodes[pos].setSUMPbar(sumLeftGray);
                nodes[pos].setResponsibleSumpBar(nodes[left].getResponsibleSumpBar());
            }
            else {
                nodes[pos].setSUMPbar(sumRightGray);
                nodes[pos].setResponsibleSumpBar(nodes[right].getResponsibleSumpBar());
            }

            if (ectBarRightResponsible >= ectBarRightDurationResponsible && ectBarRightResponsible >= ectBarLeftResponsible) {
                nodes[pos].setECTBar(ectBarRightResponsible);
                nodes[pos].setResponsibleEctBar(nodes[right].getResponsibleEctBar());
            }
            else if (ectBarRightDurationResponsible >= ectBarRightResponsible && ectBarRightDurationResponsible >= ectBarLeftResponsible) {
                nodes[pos].setECTBar(ectBarRightDurationResponsible);
                nodes[pos].setResponsibleEctBar(nodes[right].getResponsibleEctBar());
            }
            else {
                nodes[pos].setECTBar(ectBarLeftResponsible);
                nodes[pos].setResponsibleEctBar(nodes[left].getResponsibleEctBar());
            }
        }
    }




    private void reCompute(int pos) {
        while (pos > 0) {
            reComputeAux(pos);
            pos = getFather(pos);
        }
        // Fast recompute the top node. We do not need all info.
        nodes[0].setECT(Math.max(nodes[2].getECT(),  nodes[1].getECT() + nodes[2].getSUMP()));
    }


}









