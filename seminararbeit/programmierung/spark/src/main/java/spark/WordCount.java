package spark;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;

public class WordCount {

	 private static final Pattern SPACE = Pattern.compile(" ");

	  public static void main(String[] args) throws Exception {

	    SparkSession spark = SparkSession
	      .builder()
	      .appName("JavaWordCount")
	      .master("local[1]")
	      .getOrCreate();

	    //SparkContext sparkContext = new SparkContext();
	    //sparkContext.p
	    
	    //String textfile = "D:\\Programmierung\\apache_spark\\text.txt";
	    String filePath = "D:\\Eigene Dateien\\09 Studium\\01912_Seminar\\git\\seminar\\seminararbeit\\seminararbeit.pdf";
	    String filePathTxt = "D:\\Eigene Dateien\\09 Studium\\01912_Seminar\\git\\seminar\\seminararbeit\\seminararbeit.txt";
	    
	    
	    JavaSparkContext javaSparkContext = new JavaSparkContext(spark.sparkContext());
	    
	    
	    //javaSparkContext
	    
	    File file = new File(filePath);

		PDDocument pdf = null;
		String parsedText = null;
		PDFTextStripper stripper;
		pdf = PDDocument.load(file);
		stripper = new PDFTextStripper();
		parsedText = stripper.getText(pdf);
		
		
		File txtFile = new File(filePathTxt);
		FileUtils.writeStringToFile(txtFile, parsedText);		
		
		JavaRDD<String> pdfStringRDD = javaSparkContext.parallelize(Arrays.asList(parsedText));
		
		//stripper.get
		
		//stripper.getT
		
		SparkContext sparkContext = spark.sparkContext();
		//sparkContext.ha
		
		//System.out.println(parsedText);
		//JavaRDD<Row> javaRDDWithPdfText = spark.read().text(parsedText).toJavaRDD();
		//txtFile
		//JavaRDD<String> map2 = javaRDDWithPdfText.map(row -> row.toString());
		
	    //JavaRDD<String> lines = spark.read().textFile(textfile).javaRDD();
		//spark.
		//spark.sparkContext().parallelize(seq, numSlices, evidence$1)
		//spark.sparkContext().parallelize;
		
		//new JavaRDD<>(new RDD, classTag)
		
		//List<String> data = Arrays.asList("Hello","world","!!","!!","!!"); 
		//JavaRDD<String>Data1 = spark.parallelize(data);
		
		//JavaRDD<Row> text = spark.read().text(parsedText).javaRDD();
	    
		//javaRDDWithPdfText.
		
		
	    JavaRDD<String> lines = spark.read().textFile(filePathTxt).javaRDD();
	
	    JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());
/*
	    JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));
	    JavaPairRDD<String, Integer> counts = ones.reduceByKey((i1, i2) -> i1 + i2);
*/
	    JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));
	    JavaPairRDD<String, Integer> counts = ones.reduceByKey((i1, i2) -> i1 + i2);
	    
	    //counts.s
	    
	    JavaPairRDD<Integer,String> map = counts.mapToPair(item -> item.swap());
	    
	    boolean ascending = false;
	    JavaPairRDD<Integer,String> sortByKey = map.sortByKey(ascending);
	    List<Tuple2<Integer, String>> output = sortByKey.collect();
	    
	    int laengeDerListe = 20;
	    int counter = 0; 
	    for (Tuple2<?,?> tuple : output) {
	      System.out.println(tuple._1() + ": " + tuple._2());	      
	      counter++;	      
	      if (counter >= laengeDerListe) {
	    	  break;
	      }
	      
	    }
	    
	    /*
	    List<Tuple2<String, Integer>> output = counts.collect();
	    for (Tuple2<?,?> tuple : output) {
	      System.out.println(tuple._1() + ": " + tuple._2());
	    }
	    */
	    
	    //to hold the web ui open
	    Thread.sleep(30000);	   
	    //spark.stop();
	  }
	
}
