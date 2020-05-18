package icp9.mergesort;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.spark.Partition;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import icp9.mergesort.MergeSort;

public class App 
{
    public static void main( String[] args )
    {
    	// Generate 'size' random numbers in an array to sort
    	int size = 100;
    	System.out.print("Generating " + size + " numbers\n");
    	Random rand = new Random();
    	List<Integer> input = new ArrayList<Integer>();
    	for(int i = 0; i < size; ++i) {
    		input.add(rand.nextInt(100));
    	}
    	
    	// Create a spark context to distribute the array among 10 nodes
    	SparkSession spark = SparkSession.builder().appName("MergeSort").master("local").getOrCreate();
    	JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());    	
    	JavaRDD<Integer> data = jsc.parallelize(input, 10);
    	
    	// The amount of nodes is collected as to avoid assuming the number of nodes
    	int partitionSize = data.partitions().size();
    	
    	// The data is then sorted within each partition
    	JavaRDD<Integer> sortedParts = data.mapPartitions(MergeSort::initSort);
    	
    	// The partition data are collected and merge sorted into a list 
    	List<Partition> partList = sortedParts.partitions();
    	List<Integer> sorted = new ArrayList<Integer>();
    	for(int i = 0; i < partitionSize; ++i) {
    		// To reduce the space overhead, partition data is collected one partition at a time
    		List<Integer> temp = sortedParts.collectPartitions(new int[]{partList.get(i).index()})[0];
    		
    		// To keep with the idea of merge sorting, the collected data is merged with each
    		// partition data into a new array then reassigned to the collected data
    		sorted = merge(sorted, temp);
    	}
    	
    	// The final sorted list is output to a file
    	try {
    		FileWriter writer = new FileWriter("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590" + 
    				"\\ICP\\CS5590-ICP-9\\Output\\MergeSortOutput.txt");
    		writer.write(sorted.toString());
    		writer.close();
    	} catch(IOException e) {
    		System.out.print(e);
    	}
    }
    
    // Augmented merge from GeeksforGeeks
    private static List<Integer> merge(List<Integer> sorted, List<Integer> temp) {
    	// Merge lists into a new array to be returned
    	List<Integer> newSorted = new ArrayList<Integer>();
    	
    	// Merge lists until one becomes empty
		int x = 0, z = 0;
		while (x < sorted.size() && z < temp.size()) {
			if (sorted.get(x) <= temp.get(z)) {
				newSorted.add(sorted.get(x));
				++x;
			}
			else {
				newSorted.add(temp.get(z));
				++z;
			}
		}		
		
		// Append the remaining list
		while (x < sorted.size()) {
			newSorted.add(sorted.get(x));
			++x;
		}
		
		while (z < temp.size()) {
			newSorted.add(temp.get(z));
			++z;
		}
		
		return newSorted;
    }
}
