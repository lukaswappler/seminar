package spark;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

public class SeminararbeitTop20Woerter implements FilePaths {

	private static final Pattern SPACE = Pattern.compile(" ");

	public static void main(String[] args) throws Exception {

		System.setProperty("hadoop.home.dir", "D:\\Programmierung\\apache_spark\\hadoop");
		
		//create spark context
		String master = "local[4]";
		String appName = "SeminararbeitTop20Woerter";
		JavaSparkContext javaSparkContext = new JavaSparkContext(master, appName);		

		convertPdfToTextfile(PATH_TO_PDF_FILE, PATH_TO_TXT_FILE);

		//read lines and split to words
		JavaRDD<String> lines = javaSparkContext.textFile(PATH_TO_TXT_FILE);
		JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());
		JavaRDD<String> filteredWords = words.filter(word -> word.length() != 0);
		filteredWords = filteredWords.filter(word -> !word.equals("."));
		
		//count words
		JavaPairRDD<String, Integer> ones = filteredWords.mapToPair(s -> new Tuple2<>(s, 1));
		JavaPairRDD<String, Integer> counts = ones.reduceByKey((i1, i2) -> i1 + i2);

		//swap the tupel
		JavaPairRDD<Integer, String> map = counts.mapToPair(item -> item.swap());

		//sort the key (word count)
		boolean ascending = false;
		JavaPairRDD<Integer, String> sortByKey = map.sortByKey(ascending);
		List<Tuple2<Integer, String>> output = sortByKey.collect();
		
		int numberOfLines = 20;
		printToConsole(output, numberOfLines);
		
		javaSparkContext.close();
	}

	private static void convertPdfToTextfile(String pathToPdfFile, String pathToTxtFile) throws IOException {
		File file = new File(pathToPdfFile);

		PDDocument pdf = null;
		String parsedText = null;
		PDFTextStripper stripper;
		pdf = PDDocument.load(file);
		stripper = new PDFTextStripper();
		parsedText = stripper.getText(pdf);

		pdf.close();
		
		File txtFile = new File(pathToTxtFile);
		FileUtils.writeStringToFile(txtFile, parsedText);
	}

	private static void printToConsole(List<Tuple2<Integer, String>> output, int numberOfLines) {
		
		int counter = 1;
		for (Tuple2<?, ?> tuple : output) {
			System.out.println(counter + ": " + tuple._2() + " (" + tuple._1() + ")");
			counter++;
			if (counter > numberOfLines) {
				break;
			}
		}
	}

}
