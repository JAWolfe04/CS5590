package icp10.icp10;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import static org.apache.spark.sql.functions.*;

import java.util.ArrayList;
import java.util.List;

public class App 
{
    public static void main( String[] args )
    {
    	// Setup Spark
    	SparkSession spark = SparkSession.builder().appName("MergeSort").master("local").getOrCreate();
    	JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
    	Logger.getLogger("org").setLevel(Level.ERROR);
    	
    	// Part 1 question 1 - Load required dataframe and rdd data
    	Dataset<Row> data = spark.read().option("header", "true")
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Input\\survey.csv");
    	
    	JavaRDD<String> csvData = jsc.textFile("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Input\\survey.csv");
    	
    	// Part 1 question 2 -
    	Dataset<Row> q2 = data.filter("state = 'AL'");
    	
    	q2.write().option("header", "true").csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Output\\part1q2");
    	
    	// Part 1 question 3 -
    	long distinctCount = data.distinct().count();
    	boolean hasDuplicates = (data.count() - distinctCount) == 0 ? false : true;
    	System.out.print("Q3. Has duplicates: " + hasDuplicates + "\n");
    	
    	// Part 1 question 4 -
    	Dataset<Row> q4a = data.filter("country <> 'United States'");
    	
    	Dataset<Row> q4 = q2.union(q4a).sort("country").coalesce(1);
    	
    	q4.write().option("header", "true").csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Output\\part1q4");
    	
    	// Part 1 question 5 -
    	Dataset<Row> q5 = data.groupBy("treatment").count().coalesce(1);
    	
    	q5.write().option("header", "true").csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Output\\part1q5");
    	
    	// Part 2 question 1 -
    	Dataset<Row> q61 = data.select("Timestamp", "Age", "state", "treatment", "work_interfere", "no_employees");
    	Dataset<Row> q62 = data.select("Timestamp", "Age", "state", "wellness_program", "seek_help", "anonymity");
    	Dataset<Row> q6a = q61.join(q62, q61.col("Timestamp").equalTo(q62.col("Timestamp")).and(
    			q61.col("Age").equalTo(q62.col("Age")).and(q61.col("state").equalTo(q62.col("state")))), "inner")
    			.drop(q62.col("state")).drop(q62.col("Timestamp")).drop(q62.col("Age"));
    	
    	q6a.write().option("header", "true").csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Output\\part2q1a");
    	
    	data = data.withColumn("Age", data.col("Age").cast("int"));
    	Dataset<Row> q6b = data.groupBy("treatment").agg(count(lit(1)), min("Age"), max("Age"), avg("Age")).coalesce(1);
    	
    	q6b.write().option("header", "true").csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Output\\part2q1b");
    	
    	// Part 2 question 2 -
    	Row dataRow = data.takeAsList(13).get(12);
    	System.out.print(dataRow);
    	
    	// Part 3 bonus question -
    	JavaRDD<Row> rowRDD = csvData.filter(s -> !s.contains("TimeStamp")).map(App::parseLine);
    	
    	List<StructField> fields = new ArrayList<>();
    	fields.add(DataTypes.createStructField("Timestamp", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("Age", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("Gender", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("Country", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("state", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("self_employed", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("family_history", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("treatment", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("work_interfere", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("no_employees", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("remote_work", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("tech_company", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("benefits", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("care_options", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("wellness_program", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("seek_help", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("anonymity", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("leave", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("mental_health_consequence", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("phys_health_consequence", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("coworkers", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("supervisor", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("mental_health_interview", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("phys_health_interview", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("mental_vs_physical", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("obs_consequence", DataTypes.StringType, false));
    	fields.add(DataTypes.createStructField("comments", DataTypes.StringType, false));
    	StructType schema = DataTypes.createStructType(fields);
    	
    	Dataset<Row> bonusDF = spark.createDataFrame(rowRDD, schema).toDF();
    	
    	bonusDF.write().csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-10\\Output\\bonus");
    }
    
    private static Row parseLine(String line) {
    	String[] values = line.replaceAll("\"", "").split(",");
    	return RowFactory.create(values[0], values[1], values[2], values[3], values[4], 
    			values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], 
    			values[13], values[14], values[15], values[16], values[17], values[18], values[19], 
    			values[20], values[21], values[22], values[23], values[24], values[25], values[26]);
    }
}
