package icp9.dfs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class App 
{
    public static void main( String[] args )
    {
    	// These are here for some reason in Dr. Lee's example but they are never used
    	// Not sure why, maybe to say it is "in Spark", but Spark is never used 
    	SparkSession spark = SparkSession.builder().appName("MergeSort").master("local").getOrCreate();
    	JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
    	
    	// According to instructions in class and in the examples, I've implemented the
    	// following code. Spark is never used, in scala the examples use flatmap and
    	// distinct to make it look like Spark methods are being used but they 
    	// are List methods in Scala and not Spark methods
    	// I'm not sure how this is "in Spark" but I followed their example anyway
    	Map<Integer, List<Integer>> graph = new HashMap<Integer, List<Integer>>();
    	graph.put(1, Arrays.asList(2,3,5,6,7));
    	graph.put(2, Arrays.asList(1,3,4,6,7));
    	graph.put(3, Arrays.asList(1,2));
    	graph.put(4, Arrays.asList(2,5,7));
    	// Graph from the example omitted 4 as an adjacent node of node 5
    	graph.put(5, Arrays.asList(1,4,6,7));
    	graph.put(6, Arrays.asList(1,2,5,7));
    	graph.put(7, Arrays.asList(1,2,4,5,6));
    	
    	// Send DFS output to a file
    	try {
    		FileWriter writer = new FileWriter("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590" + 
    				"\\ICP\\CS5590-ICP-9\\Output\\DFSOutput.txt");
    		writer.write(DFS(1, graph));
    		writer.close();
    	} catch(IOException e) {
    		System.out.print(e);
    	}
    }
    
    private static String DFS(Integer start, Map<Integer, List<Integer>> graph) {
    	List<Integer> visited = new ArrayList<Integer>();
    	String output = "";
    	
    	// Checks for nodes that were not visited
    	for(int i = 1; i <= graph.size(); ++i) {
    		if(!visited.contains(i)) {
    			output = DFSInner(i, visited, graph, output);
    		}
    	}
    	
    	return output;
    }
    
    private static String DFSInner(Integer v, List<Integer> visited, Map<Integer, 
    	List<Integer>> graph, String output) {
    	// Mark as visited to not process it again
    	visited.add(v);
    	// Process all adjacent nodes that have not been visited
    	for(Integer adj : graph.get(v)) {
    		if(!visited.contains(adj)) {
    			output += v + "->" + adj + " ";
    			output = DFSInner(adj, visited, graph, output);
    		}
    	}
    	return output;
    }
}
