package icp12.graphframes;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.graphframes.GraphFrame;

import static org.apache.spark.sql.functions.*;

public class App 
{
    public static void main( String[] args )
    {
    	SparkSession spark = SparkSession.builder().appName("graphing").master("local").getOrCreate();
    	Logger.getRootLogger().setLevel(Level.ERROR);
    	
    	StructType edgeSchema = new StructType(new StructField[] {
    			new StructField("Trip ID", DataTypes.IntegerType, false, Metadata.empty()),
    			new StructField("Duration", DataTypes.IntegerType, false, Metadata.empty()),
    			new StructField("Start Date", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Start Station", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Start Terminal", DataTypes.IntegerType, false, Metadata.empty()),
    			new StructField("End Date", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("End Station", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("End Terminal", DataTypes.IntegerType, false, Metadata.empty()),
    			new StructField("Bike #", DataTypes.IntegerType, false, Metadata.empty()),
    			new StructField("Subscriber Type", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Zip Code", DataTypes.IntegerType, false, Metadata.empty())
    	});
    	
    	StructType vertexSchema = new StructType(new StructField[] {
    			new StructField("station_id", DataTypes.IntegerType, false, Metadata.empty()),
    			new StructField("name", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("lat", DataTypes.DoubleType, false, Metadata.empty()),
    			new StructField("long", DataTypes.DoubleType, false, Metadata.empty()),
    			new StructField("dockcount", DataTypes.IntegerType, false, Metadata.empty()),
    			new StructField("landmark", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("installation", DataTypes.StringType, false, Metadata.empty())
    	});
    	
    	Dataset<Row> edgeDF = spark.read().option("header", "true").schema(edgeSchema)
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Input\\201508_trip_data.csv");
    	Dataset<Row> vertexDF = spark.read().option("header", "true").schema(vertexSchema)
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Input\\201508_station_data.csv");
    	
    	edgeDF = edgeDF.withColumnRenamed("Start Terminal", "src");
    	edgeDF = edgeDF.withColumnRenamed("End Terminal", "dst");
    	vertexDF = vertexDF.withColumnRenamed("station_id", "id");
    	
    	vertexDF = vertexDF.withColumn("Coords", concat(col("lat"), lit(','), col("long"))).drop("lat").drop("long");
    	vertexDF = vertexDF.distinct();
    	
    	vertexDF.coalesce(1).write().option("header", "true")
    		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\Part1q5");
    	
    	GraphFrame g = new GraphFrame(vertexDF, edgeDF);
    	
    	g.vertices()
    	.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\Part1q7");
    	
    	g.edges()
    	.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\Part1q8");
    	
    	g.inDegrees().orderBy(desc("inDegree"))
    	.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\Part1q9");
    	
    	g.outDegrees().orderBy(desc("outDegree"))
    	.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\Part1q10");
    	
    
    	g.find("(a)-[e1]->(b); (b)-[e2]->(c); (c)-[]->(a)").show();
    	
    	g.degrees().orderBy(desc("degree"))
    	.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\PartBonusq1");
    	
    	
    	g.edges().groupBy("dst").agg(count(lit(1)).alias("count")).orderBy(desc("count"))
    	.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\PartBonusq2");
    	
    	
    	Dataset<Row> sink = g.inDegrees().join(g.outDegrees(), 
    			g.inDegrees().col("id").equalTo(g.outDegrees().col("id"))).drop(g.inDegrees().col("id"));
    	sink = sink.withColumn("Sink_ratio", sink.col("inDegree").divide(sink.col("outDegree")))
    	.sort(desc("Sink_ratio"));
    	
    	sink.coalesce(1).write().option("header", "true")
    		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-12\\Output\\PartBonusq3");
    }
}
