package spark.seminararbeit.mitarbeiter;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;

/**
 * Created by 05020857 on 25.04.2017.
 */
public class AnalyseEmployees {

	public static void main(String[] args) {
		
		setSystemParameters();
		
		SparkSession spark = SparkSession
		  .builder()
		  .appName("EmployeeExample")
		  .master("local[4]")		  
		  .getOrCreate();

		Dataset<Row> people = spark.read().json(getPathToDataFile());
		//printSchema(people);
		
		// Creates a temporary view using the DataFrame
		people.createOrReplaceTempView("people");

		//Learning (accept only employees with Astrology interests)
		Dataset<Row> training = spark.createDataFrame(Arrays.asList(
			new JavaLabeledEmployeeDocument(0L, "Car Racing", 0.0),
			new JavaLabeledEmployeeDocument(3L, "Astrology", 1.0),
			new JavaLabeledEmployeeDocument(4L, "Bowling", 0.0)
		), JavaLabeledEmployeeDocument.class);

		// Configure an ML pipeline, which consists of three stages: tokenizer, hashingTF, and lr.
		Tokenizer tokenizer = new Tokenizer()
		  .setInputCol("interests")
		  .setOutputCol("words");

		HashingTF hashingTF = new HashingTF()
		  .setNumFeatures(1000)
		  .setInputCol(tokenizer.getOutputCol())
		  .setOutputCol("features");

		LogisticRegression lr = new LogisticRegression()
		  .setMaxIter(10)
		  .setRegParam(0.001);

		Pipeline pipeline = new Pipeline()
		  .setStages(new PipelineStage[] {tokenizer, hashingTF, lr});

		// Fit the pipeline to training documents.
		PipelineModel model = pipeline.fit(training);

		//show only hits
		//showOnlyPredictedEmployees(model, people);
		
		Dataset<Row> predictions = model.transform(people);
		
		JavaRDD<Row> predictedRowsRDD = predictions
			.select("FirstName", "interests", "probability", "prediction", "Age")
			.where("prediction=1.0").toJavaRDD();
		
		System.out.println("Count of predictedRows: " + 
				predictedRowsRDD.collect().size());
		
		//map row to age pairs
		JavaPairRDD<Integer, Integer> ageMap = predictedRowsRDD.mapToPair(
				new PairFunction<Row, Integer, Integer>() {		
			private static final long serialVersionUID = -4267715305026281029L;

			@Override
			public Tuple2<Integer, Integer> call(Row row) throws Exception {
				long age = row.getLong(row.fieldIndex("Age"));
				return new Tuple2<Integer, Integer>(Integer.parseInt(Long.toString(age)), 1);
			}
		});		
		JavaPairRDD<Integer, Integer> ageMapCounts = ageMap.reduceByKey((i1, i2) -> i1 + i2);
		boolean sortAscending = true;
		JavaPairRDD<Integer, Integer> ageMapCountsSorted = ageMapCounts.sortByKey(sortAscending);

		List<Tuple2<Integer, Integer>> output = ageMapCountsSorted.collect();			
		for (Tuple2<?, ?> tuple : output) {
			System.out.println("Alter: " + tuple._1() + "--> Personen: " + tuple._2());
		}
	
		spark.stop();	
	}

	private static String getPathToDataFile() {
		return "D:\\Programmierung\\apache_spark\\data\\Employees100K.json";		
	}

	private static void setSystemParameters() {
		System.setProperty("hadoop.home.dir", "D:\\Programmierung\\apache_spark\\hadoop");
	}

	private static void showOnlyPredictedEmployees(PipelineModel model, Dataset<Row> people) {
		Dataset<Row> predictions = model.transform(people);
		List<Row> foundPredictedRows = predictions.select("FirstName", "interests", "probability", "prediction").where("prediction=1.0").collectAsList();

		System.out.println("Count of predictedRows: " +  foundPredictedRows.size());
		showAllRows(foundPredictedRows);
	}

	private static void showAllRows(List<Row> foundPredictedRows) {
		for (Row r : foundPredictedRows) {
			System.out.println("---");
			System.out.println("(" + r.get(0) + ",  --> prob=" + r.get(2) + ", prediction=" + r.get(3));
			System.out.println("---");
		}
	}

	private static void printSchema(Dataset<Row> people) {
		// The inferred schema can be visualized using the printSchema() method
		people.printSchema();
		/*
		 root
		 |-- Address: string (nullable = true)
		 |-- Age: long (nullable = true)
		 |-- DateOfJoining: string (nullable = true)
		 |-- Designation: string (nullable = true)
		 |-- FirstName: string (nullable = true)
		 |-- Gender: string (nullable = true)
		 |-- interests: string (nullable = true)
		 |-- LastName: string (nullable = true)
		 |-- MaritalStatus: string (nullable = true)
		 |-- Salary: string (nullable = true)
		 |-- index: struct (nullable = true)
		 |    |-- _index: string (nullable = true)
		 |    |-- _type: string (nullable = true)
		 */
	}
	
	private static void showFirst100EmployessWithResults() {
		
		//zeige nur erste 100
		/*
		int counter = 0;
		// Make predictions on test documents.
		Dataset<Row> predictions = model.transform(people);
		for (Row r : predictions.select("FirstName", "interests", "probability", "prediction").collectAsList()) {
			System.out.println("---");
			System.out.println("(" + r.get(0) + ",  --> prob=" + r.get(2) + ", prediction=" + r.get(3));
			System.out.println("---");
			counter++;
			if (counter > 100) {
				break;
			}

		}

		*/
	}

}
