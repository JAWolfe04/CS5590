package icp11.streaming;

import org.apache.log4j.Logger;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;

import scala.Tuple2;

public class App 
{
    @SuppressWarnings("resource")
	public static void main( String[] args ) throws InterruptedException
    {
    	SparkSession spark = SparkSession.builder().appName("streaming").master("local[*]").getOrCreate();
    	JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
    	JavaStreamingContext ssc = new JavaStreamingContext(jsc, Durations.seconds(1));
    	Logger.getRootLogger().setLevel(Level.ERROR);
    	
    	JavaDStream<String> lines = ssc.textFileStream("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-11\\Input\\Logs");
    	JavaPairDStream<String, Integer> counts = lines.flatMap(l -> Arrays.asList(l.split(" ")).iterator())
    		.mapToPair(word -> new Tuple2<>(word, 1))
    		.reduceByKey((a, b) -> a + b);
    	counts.print();
    	ssc.start();
    	ssc.awaitTermination();
    }
}
