package icp11.portstreaming;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

public class App 
{
    @SuppressWarnings("resource")
	public static void main( String[] args ) throws InterruptedException
    {
    	SparkSession spark = SparkSession.builder().appName("portstreaming").master("local[*]").getOrCreate();
    	JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
    	JavaStreamingContext ssc = new JavaStreamingContext(jsc, Durations.seconds(5));
    	Logger.getRootLogger().setLevel(Level.ERROR);
    	
    	JavaReceiverInputDStream<String> data = ssc.socketTextStream("localhost",9999);
    	JavaPairDStream<String, Integer> counts = data.flatMap(l -> Arrays.asList(l.split(" ")).iterator())
        		.mapToPair(word -> new Tuple2<>(word, 1))
        		.reduceByKey((a, b) -> a + b);
        	counts.print();
    	ssc.start();
    	ssc.awaitTermination();
    }
}
