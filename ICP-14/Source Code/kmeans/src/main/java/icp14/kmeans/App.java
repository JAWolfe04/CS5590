package icp14.kmeans;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.evaluation.ClusteringEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;

public class App 
{
    public static void main( String[] args )
    {
    	SparkSession spark = SparkSession.builder().appName("ml").master("local[*]").getOrCreate();
    	Logger.getRootLogger().setLevel(Level.ERROR);
    	
    	// Load and parse data
    	Dataset<Row> data = spark.read().option("header", "true")
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-14\\Input\\diabetic_data.csv");
    	
    	StringIndexer labelIndexer = new StringIndexer().setInputCol("readmitted").setOutputCol("readmittedIdx");
    	data = labelIndexer.fit(data).transform(data);
    	
    	data = data.withColumn("time_in_hospital_int", data.col("time_in_hospital").cast(DataTypes.IntegerType))
    			   .withColumn("num_medications_int", data.col("num_medications").cast(DataTypes.IntegerType));
    	
    	VectorAssembler assembler = new VectorAssembler().setInputCols(
    			new String[]{"readmittedIdx", "time_in_hospital_int", "num_medications_int"})
    			.setOutputCol("features")
    			.setHandleInvalid("skip");
    	Dataset<Row> featurized = assembler.transform(data);

    	KMeansModel kmModel = new KMeans().setK(2).fit(featurized);
    	
    	Dataset<Row> predictions = kmModel.transform(featurized);
    	
    	ClusteringEvaluator evaluator = new ClusteringEvaluator();

    	double silhouette = evaluator.evaluate(predictions);
    	System.out.println("Silhouette with squared euclidean distance = " + silhouette);

    	Vector[] centers = kmModel.clusterCenters();
    	System.out.println("Cluster Centers: ");
    	for (Vector center: centers) {
    	  System.out.println(center);
    	}
    }
}
