package minicp.util;

import java.util.Arrays;

public class SortUtils {
    // https://stackoverflow.com/questions/951848/java-array-sort-quick-way-to-get-a-sorted-list-of-indices-of-an-array?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    public static void quicksort(int[] values, int[] index) {
        for (int i = 0; i < index.length; ++i) {
            index[i] = i;
        }

        quicksort(values, index, 0, index.length - 1);
    }

    // quicksort a[left] to a[right]
    public static void quicksort(int[] a, int[] index, int left, int right) {
        if (right <= left) return;
        int i = partition(a, index, left, right);
        quicksort(a, index, left, i-1);
        quicksort(a, index, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(int[] a, int[] index,
                                 int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(a[index[++i]], a[index[right]]))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (less(a[index[right]], a[index[--j]]))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, index, i, j);               // swap two elements into place
        }
        exch(a, index, i, right);               // swap with partition element
        return i;
    }

    // is x < y ?
    private static boolean less(int x, int y) {
        return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(int[] a, int[] index, int i, int j) {
//        int swap = a[i];
//        a[i] = a[j];
//        a[j] = swap;
        int b = index[i];
        index[i] = index[j];
        index[j] = b;

//        int b = i;
//        index[i] = i;
//        index[j] = j;
    }

    public static void main(String[] args) {
        int[] values = {4,5,6, 1, 2, 3};
        int[] indices = {0,0,0,0,0,0};
        quicksort(values, indices);
        System.out.println(Arrays.toString(values));
        System.out.println(Arrays.toString(indices));
    }
}
