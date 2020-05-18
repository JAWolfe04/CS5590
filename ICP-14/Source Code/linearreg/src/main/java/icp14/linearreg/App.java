package icp14.linearreg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.feature.FeatureHasher;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.regression.LinearRegressionTrainingSummary;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class App 
{
    public static void main( String[] args )
    {
    	SparkSession spark = SparkSession.builder().appName("ml").master("local[*]").getOrCreate();
    	Logger.getRootLogger().setLevel(Level.ERROR);
    	
    	StructType dataSchema = new StructType(new StructField[] {
    			new StructField("symboling", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("normalized_losses", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("make", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("fuel_type", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("aspiration", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("num_of_doors", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("body_style", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("drive_wheels", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("engine_location", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("wheel_base", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("length", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("width", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("height", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("curb_weight", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("engine_type", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("num_of_cylinders", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("engine_size", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("fuel_system", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("bore", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("stroke", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("compression_ratio", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("horsepower", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("peak_rpm", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("city_mpg", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("highway_mpg", DataTypes.IntegerType, true, Metadata.empty()),
    			new StructField("price", DataTypes.IntegerType, true, Metadata.empty()),
    	});
    	
    	// Load and parse data
    	Dataset<Row> data = spark.read().option("header", "false").schema(dataSchema)
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-14\\Input\\imports-85.csv");
    	
    	FeatureHasher hasher = new FeatureHasher()
    			.setInputCols(new String[]{"make", "fuel_type", "aspiration", "num_of_doors", "body_style", 
    					"drive_wheels", "engine_location", "wheel_base", "length", "width", "height", 
    					"curb_weight", "engine_type", "num_of_cylinders", "engine_size", "fuel_system", 
    					"bore", "stroke", "compression_ratio", "horsepower", "peak_rpm", "city_mpg", 
    					"highway_mpg"})
    			.setOutputCol("features");
    	
    	Dataset<Row> featurized = hasher.transform(data);    	
    	
    	StringIndexer labelIndexer = new StringIndexer().setInputCol("price").setOutputCol("label").setHandleInvalid("skip");
    	Dataset<Row> labeledData = labelIndexer.fit(featurized).transform(featurized);
    	
    	LinearRegressionModel lrModel = new LinearRegression().fit(labeledData); 
    	
    	System.out.println("Coefficients: " + lrModel.coefficients() + " Intercept: " + lrModel.intercept());

		// Summarize the model over the training set and print out some metrics.
		LinearRegressionTrainingSummary trainingSummary = lrModel.summary();
		System.out.println("numIterations: " + trainingSummary.totalIterations());
		System.out.println("objectiveHistory: " + Vectors.dense(trainingSummary.objectiveHistory()));
		trainingSummary.residuals().show();
		System.out.println("RMSE: " + trainingSummary.rootMeanSquaredError());
		System.out.println("r2: " + trainingSummary.r2());
    }
}
