package org.cg.ads.filtering.training;

import java.util.LinkedList;
import java.util.List;

import org.cg.ads.aads.Ad;
import org.cg.common.check.Check;

public class Classifier {

	private final static int standardFeatureCount = "INTERCEPT;prize;size;phone;hasEmail;substandard;provision;kaution;ablose;ppm2".split(";").length;
	private static final double INTERCEPT = 1;
	private final List<Double> theta;
	private final Dictionary dict;

	public Classifier(List<Double> theta, Dictionary dict) {
		this.theta = theta;
		this.dict = dict;
	}

	public Double getCost(Ad ad) {
		
		AdFeatures features = new AdFeatures(ad, dict);
		
		//System.out.println(String.format("Feature count expected %d actual %d wordindcount %d", standardFeatureCount, theta.size(), features.wordIndicators.length));

		Check.isTrue(theta.size() == standardFeatureCount
				+ features.wordIndicators.length);

		List<Double> values = new LinkedList<Double>();
		addValue(values, INTERCEPT);
		addValue(values, features.prize);
		addValue(values, features.size);
		addValue(values, features.phone);
		addValue(values, features.hasEmail);
		addValue(values, features.substandard);
		addValue(values, features.provision);
		addValue(values, features.kaution);
		addValue(values, features.ablose);
		addValue(values, features.size / features.prize);
		for (Integer v : features.wordIndicators)
			addValue(values, v);

		Check.isTrue(theta.size() == values.size());

		Double result = (double) 0;
		for (int i = 0; i < values.size(); i++)
			result += theta.get(i) * values.get(i);

		return result;
	}

	public boolean predict(Ad ad, Double threshold) {
		return predict(getCost(ad), threshold);
	}

	public boolean predict(Double cost, Double threshold) {
		return sigmoid(cost) >= threshold;
	}

	public Double sigmoid(Double cost) {
		return 1 / (1 + Math.exp(cost * -1));
	}

	private void addValue(List<Double> values, double value) {
		values.add(value);
	}

	public static List<Double> fromCsv(String csv) {
		List<Double> result = new LinkedList<Double>();
		for (String s : csv.split(";"))
			result.add(Double.valueOf(s.trim()));
		return result;
	}

	public int thetaSize(){
		return theta.size();
	}
	
}
