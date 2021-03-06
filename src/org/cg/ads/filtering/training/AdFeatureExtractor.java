package org.cg.ads.filtering.training;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.cg.ads.aads.Ad;

public class AdFeatureExtractor {

	private final String inputFilePath;
	private final String featuresFilePath;
	private final String columnNamesFilePath;
	private final boolean arffFormat;
	private final String arffExtension = "arff";

	private static Dictionary dict = Dictionary.getInstance();

	public AdFeatureExtractor(String inputFilePath, String featuresFilePath) {
		this.inputFilePath = inputFilePath;
		this.featuresFilePath = featuresFilePath;
		this.columnNamesFilePath = featuresFilePath.replace(".csv", ".col");
		arffFormat = FilenameUtils.isExtension(featuresFilePath, arffExtension);

		checkFileExists(inputFilePath);
	}

	public void extract() {
		AdCsvReader reader = new AdCsvReader(inputFilePath);
		StringBuilder sb = new StringBuilder();

		if (arffFormat)
			appendArffHeader(sb);

		int count = 0;
		for (Ad ad : reader) {
			count++;
			sb.append(AdFeaturesFormat.fmt(new AdFeatures(ad, dict)) + "\r\n");
			if (count % 10 == 0)
				System.out.println(Integer.valueOf(count));
		}

		try {
			FileUtils.writeStringToFile(new File(featuresFilePath),
					sb.toString());
			FileUtils.writeStringToFile(new File(columnNamesFilePath),
					getColumnNames());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Features written to ");
		System.out.println("    " + featuresFilePath);
		System.out.println("    " + columnNamesFilePath);
	}

	private String getColumnNames() {
		return AdFeaturesFormat.getFeatureNames().replace(",", "\n");
	}

	private void appendArffHeader(StringBuilder sb) {
		sb.append("@relation AdFeatures\n\n");

		for (String feature : getColumnNames().split("\n"))
			sb.append(String.format("@attribute %s %s\n", feature,
					getRange(feature, AdFeaturesFormat.booleanFeatures)));

		sb.append("\n");
		sb.append("@data\n");
	}

	private String getRange(String feature, List<String> booleanfeatures) {
		if (booleanfeatures.indexOf(feature) >= 0)
			return "{0, 1}";
		else
			return "numeric";
	}

	private void checkFileExists(String filePath) {
		if (!new File(filePath).exists())
			throw new RuntimeException("File not found: " + filePath);
	}

	/**
	 * @param args
	 *  	1)  source file, e.g. classifiedAds.csv 
	 *  	2)  target file, e.g. AdFeatures.csv
	 *  
	 *  Subsequent processing relies on Dictionary
	 *  Use Dictionary.main to create a dictionary from fusion table exported csv
	 *  
	 */
	public static void main(String[] args) {
		if (!(args.length == 2))
			System.out
					.println("usage: extract <source file path> <dest file path> ");
		else
			try {
				new AdFeatureExtractor(args[0], args[1]).extract();
			} catch (Exception e) {
				System.out.println(e.getClass().getName() + " "
						+ e.getMessage());
				e.printStackTrace();
			}

	}
}
