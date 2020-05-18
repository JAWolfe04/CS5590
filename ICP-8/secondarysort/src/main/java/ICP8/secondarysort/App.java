package ICP8.secondarysort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.spark.HashPartitioner;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;


public class App 
{
	public static void main(String[] args) {
		SparkSession spark = SparkSession.builder().appName("ICP8").master("local").getOrCreate();
		JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());

    	JavaRDD<String> data = jsc.textFile("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-8\\Input\\Temperature.txt", 1);

    	// Sort the input data into a tuple with a tuple of date and temperature and temperature 
    	// ((Date, Temperature), Temperature)
    	JavaPairRDD<Tuple2<String, Integer>,Integer> nestedData = data.mapToPair(e -> createNestedData(e));
    	JavaPairRDD<Tuple2<String, Integer>,Integer> partedData = nestedData.partitionBy(new HashPartitioner(2));
    	JavaPairRDD<String, List<Integer>> result = partedData
    		// Create a new tuple with the date and temperature for grouping
    		.mapToPair(s -> new Tuple2<>(s._1._1, s._2))
    		// Group similar keys
    		.groupByKey()
    		// Sort the values
    		.mapValues(s -> { 
    			List<Integer> val = new ArrayList<Integer>();
    			s.iterator().forEachRemaining(val::add);
    			Collections.sort(val);
    			return val; 
    		})
    		// Sort by key to have ordered keys
    		.sortByKey();
    	
    	result.coalesce(1).saveAsTextFile("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-8\\Output\\Part2");
    			
	}
	
	
	private static Tuple2<Tuple2<String, Integer>,Integer> createNestedData(String line) {
		String[] data = line.split(",");
		int temperature = Integer.parseInt(data[3]);
		String date = data[0] + "-" + data[1];
		Tuple2<String, Integer> out = new Tuple2<String, Integer>(date, temperature);
		return new Tuple2<>(out, temperature);
	}
}
