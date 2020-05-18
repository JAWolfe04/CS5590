package icp13.icp13;

import java.util.ArrayList;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import static org.apache.spark.sql.functions.desc;
import org.graphframes.GraphFrame;

public class App 
{
    public static void main( String[] args )
    {    			
    	SparkSession spark = SparkSession.builder().appName("graphing").master("local[*]").getOrCreate();
    	
    	// Shortest path has type mismatches with non-strings
    	StructType edgeSchema = new StructType(new StructField[] {
    			new StructField("Trip ID", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Duration", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Start Date", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Start Station", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Start Terminal", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("End Date", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("End Station", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("End Terminal", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Bike #", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Subscriber Type", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("Zip Code", DataTypes.StringType, false, Metadata.empty())
    	});
    	
    	// Shortest path has type mismatches with non-strings
    	StructType vertexSchema = new StructType(new StructField[] {
    			new StructField("station_id", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("name", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("lat", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("long", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("dockcount", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("landmark", DataTypes.StringType, false, Metadata.empty()),
    			new StructField("installation", DataTypes.StringType, false, Metadata.empty())
    	});
    	
    	Dataset<Row> edgeDF = spark.read().option("header", "true").schema(edgeSchema)
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-13\\Input\\201508_trip_data.csv");
    	Dataset<Row> vertexDF = spark.read().option("header", "true").schema(vertexSchema)
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-13\\Input\\201508_station_data.csv");
    	
    	// Format to work with GraphX and GraphFrames
    	edgeDF = edgeDF.withColumnRenamed("Start Terminal", "src");
    	edgeDF = edgeDF.withColumnRenamed("End Terminal", "dst");
    	vertexDF = vertexDF.withColumnRenamed("station_id", "id");
    	
    	// Remove any possible duplicates
    	vertexDF = vertexDF.distinct();
    	edgeDF = edgeDF.distinct();
    	
    	GraphFrame graph = new GraphFrame(vertexDF, edgeDF);
    	
    	// Runs well and gives results quickly
    	Dataset<Row> triangles = graph.triangleCount().run();
    	
    	triangles.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-13\\Output\\TriangleCount.csv");
    	
    	// Requires the id to be strings or it will have a type mismatch
    	ArrayList<Object> ids = new ArrayList<Object>();
    	ids.add("50");
    	ids.add("76");
    	Dataset<Row> shortestPath = graph.shortestPaths().landmarks(ids).run().select("id", "distances");
    	
    	shortestPath.coalesce(1).write().option("header", "true")
		.json("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-13\\Output\\ShortestPath.json");
    	
    	// Using more than 3 max iterations takes a long time to run
    	GraphFrame result = graph.pageRank().resetProbability(0.15).maxIter(3).run();
    	
    	result.vertices().sort(desc("pagerank")).coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-13\\Output\\PageRank.csv");
    	
    	// Running 5 iterations takes a long time
    	Dataset<Row> labeling = graph.labelPropagation().maxIter(5).run().select("id", "label");
    	
    	labeling.coalesce(1).write().option("header", "true")
		.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-13\\Output\\LabelProp.csv");
    	
    	// BFS from 50 to 76 with max path length of 3. Max path of 4 runs out of memory
    	// Doing more than 1 id runs out of memory
    	Dataset<Row> bfs = graph.bfs().fromExpr("id = 50").toExpr("id = 76").maxPathLength(3).run();
    	
    	bfs.coalesce(1).write().option("header", "true")
		.json("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-13\\Output\\BFS.json");
    }
}
