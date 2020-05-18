package ICP8.ICP8;

import java.util.Arrays;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;

public class App 
{	
    public static void main( String[] args )
    {
    	SparkSession spark = SparkSession.builder().appName("ICP8").master("local").getOrCreate();
    	JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
    	
    	JavaRDD<String> data = jsc.textFile("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-8\\Input", 1);
    	
    	JavaPairRDD<String, Integer> wordCount = data
    			// remove , . | ( ) : ' ? - ! ;
    			.map(s -> s.replaceAll("[\\,\\.\\|\\(\\)\\:\\'\\?\\-\\!\\;]",""))
    			
    			// split by spaces
    			.flatMap(s -> Arrays.asList(s.split(" ")).iterator())
    			
    			// split by tabs
    			.flatMap(s -> Arrays.asList(s.split("\t")).iterator())
    			
    			// remove empty entries
    			.filter(s -> s.length() > 0)
    			
    			// convert to lowercase to make case insensitive
    			.map(s -> s.toLowerCase())
    			
    			// return counts of words beginning with z
    			.filter(s -> s.charAt(0) == 'z')
    			
    			// map words
    			.mapToPair(s -> new Tuple2<>(s, 1))
    			
    			// reduce like words
    			.reduceByKey((a, b) -> a + b)
    			
    			// exchange word and count
    			.mapToPair(x -> x.swap())
    			
    			// sort with highest count first
    			.sortByKey(false)
    			
    			// swap again
    			.mapToPair(x -> x.swap());
    	
    	wordCount.coalesce(1).saveAsTextFile("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-8\\Output\\Part1");
    }
}
