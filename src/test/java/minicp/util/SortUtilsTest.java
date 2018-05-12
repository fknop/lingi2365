package minicp.util;


import org.junit.Test;

import static org.junit.Assert.*;

public class SortUtilsTest {

    @Test
    public void testSortIndices() {
        int[] values = {4,5,0,1,2,3};
        int[] indices = new int[6];
        SortUtils.quicksort(values, indices);
        assertArrayEquals(values,  new int[]{4,5,0,1,2,3});
        assertArrayEquals(indices, new int[]{2, 3, 4, 5, 0, 1});
    }

    @Test
    public void testSortIndices2() {
        int[] values = {1, 2, 3};
        int[] indices = new int[3];
        SortUtils.quicksort(values, indices);
        assertArrayEquals(values,  new int[]{1, 2, 3});
        assertArrayEquals(indices, new int[]{0, 1, 2});
    }
}
