package icp14.classifyml;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.classification.DecisionTreeClassificationModel;
import org.apache.spark.ml.classification.DecisionTreeClassifier;
import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.FeatureHasher;
import org.apache.spark.ml.feature.StringIndexer;
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
    			new StructField("temperature", DataTypes.DoubleType, true, Metadata.empty()),
    			new StructField("nausea", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("lumbar_pain", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("urine_pushing", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("micturition_pains", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("urethral_burning", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("uti_dx", DataTypes.StringType, true, Metadata.empty()),
    			new StructField("nephritis_dx", DataTypes.StringType, true, Metadata.empty())
    	});
    	
    	Dataset<Row> data = spark.read().option("header", "false").schema(dataSchema)
    			.csv("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-14\\Input\\diagnosis.csv");
    	
    	FeatureHasher hasher = new FeatureHasher()
    			.setInputCols(new String[]{"temperature", "nausea", "lumbar_pain", "urine_pushing", "urine_pushing",
    					"micturition_pains", "urethral_burning"})
    			.setOutputCol("features");
    	
    	Dataset<Row> featurized = hasher.transform(data);    	
    	
    	StringIndexer labelIndexer = new StringIndexer().setInputCol("nephritis_dx").setOutputCol("label");
    	Dataset<Row> labeledData = labelIndexer.fit(featurized).transform(featurized);
    	
    	Dataset<Row>[] splits = labeledData.randomSplit(new double[] {0.7, 0.3});
    	Dataset<Row> trainingData = splits[0];
    	Dataset<Row> testData = splits[1];
    	
    	
    	// Naive Bayes
        NaiveBayesModel nbModel = new NaiveBayes().fit(trainingData);
        
        Dataset<Row> nbPredictions = nbModel.transform(testData);
        
        // Decision Tree
        DecisionTreeClassificationModel dtModel = new DecisionTreeClassifier()
										          .setLabelCol("label")
										          .setFeaturesCol("features")
										          .fit(trainingData);       

        Dataset<Row> dtPredictions = dtModel.transform(testData);
        
        // Random Forest
        RandomForestClassificationModel rfModel = new RandomForestClassifier()
											        .setLabelCol("label")
											        .setFeaturesCol("features")
											        .setNumTrees(1)
											        .fit(trainingData);
        
        Dataset<Row> rfPredictions = rfModel.transform(testData);
        
        // AUC Calculations
        BinaryClassificationEvaluator AUCEvaluator = new BinaryClassificationEvaluator()
      		  .setLabelCol("label")
      		  .setRawPredictionCol("prediction")
      		  .setMetricName("areaUnderROC");
        
        System.out.println("\nNaive Bayes Model:");
        System.out.println("AUC = " + AUCEvaluator.evaluate(nbPredictions));
        
        System.out.println("\nDecision Tree Model:");
        System.out.println("AUC = " + AUCEvaluator.evaluate(dtPredictions));
        
        System.out.println("\nRandom Forest Model:");
        System.out.println("AUC = " + AUCEvaluator.evaluate(rfPredictions));
    }
}
