package icp11.charactercount;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import icp11.charactercount.App;

public class App 
{
	@SuppressWarnings("resource")
	public static void main( String[] args ) throws InterruptedException
    {
    	SparkSession spark = SparkSession.builder().appName("charactercount").master("local[*]").getOrCreate();
    	JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
    	JavaStreamingContext ssc = new JavaStreamingContext(jsc, Durations.seconds(5));
    	Logger.getRootLogger().setLevel(Level.ERROR);
    	
    	JavaReceiverInputDStream<String> data = ssc.socketTextStream("localhost",9999);
    	JavaDStream<String> words = data.flatMap(l -> Arrays.asList(l.split(" ")).iterator());
    	System.out.print("Character Count\t=> List of Words (Distinct)\n");
    	words.foreachRDD(App::showWordLength);
    	ssc.start();
    	ssc.awaitTermination();
    }
    
    private static void showWordLength(JavaRDD<String> word) {
    	List<String> distinctWords = word.distinct().collect();
    	Long chrCount = word.flatMap(l -> Arrays.asList(l.split("")).iterator()).count();
    	Long count = word.count();
    	
    	if(chrCount > 0)// tin ten too cat dog // to in an be me by of it to be or be be
    		System.out.print(chrCount / count + "\t\t=> " + distinctWords + "\n");
    }
}
