package icp9.mergesort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MergeSort {

	public static Iterator<Integer> initSort(Iterator<Integer> itr) {
		// Populate a list to pass on to merge sort because an
		// iterator cannot be split
		List<Integer> data = new ArrayList<Integer>();
		while(itr.hasNext()) {
			data.add(itr.next());
		}

		sort(data, 0, data.size() - 1);
		return data.iterator();
	}
	
	// Simple Merge sort from GreeksforGeeks
	private static void sort(List<Integer> list, int l, int r) {
		if(l < r) {
			int m = (l + r) / 2;
			
			sort(list, l, m);
			sort(list , m + 1, r); 
			
			merge(list, l, m, r);
		}
	}
	
	// Simple Merge sort from GreeksforGeeks
	private static void merge(List<Integer> list, int l, int m, int r) {
		int n1 = m - l + 1;
		int n2 = r - m;
		
		int L[] = new int [n1];
		int R[] = new int [n2]; 
		
		for (int i=0; i<n1; ++i)
			L[i] = list.get(l + i);
		for (int j=0; j<n2; ++j)
			R[j] = list.get(m + 1+ j);
		
		int i = 0, j = 0;
		int k = l;
		while (i < n1 && j < n2) {
			if (L[i] <= R[j]) {
				list.set(k, L[i]);
				++i;
			}
			else {
				list.set(k, R[j]);
				++j;
			}
			++k;
		}		
		
		while (i < n1) {
			list.set(k, L[i]);
			++i;
			++k;
		}
		
		while (j < n2) {
			list.set(k, R[j]);
			++j;
			++k;
		}
	}
}
