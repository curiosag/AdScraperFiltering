package org.cg.ads.filtering;

import java.util.List;

import org.cg.common.util.CollectionUtil;

public class AdFeaturesFormat {

	private final static String featureNames = "id,status,prize,size,rooms,hasEmail,substandard,provision,kaution,ablos";
	private static Dictionary dict = Dictionary.getInstance();

	/**
	 * 
	 * @return feature names as single csv line
	 */
	public static String getFeatureNames()
	{
		return featureNames + "," + dict.termsToString(",");
	}
	
	public final static List<String> booleanFeatures = CollectionUtil
			.toList("status,hasEmail,substandard,provision,kaution".split(","));

	// public static String fmt(AdFeatures f) {
	// return String.format("%d,%d,%.2f,%.2f,%.2f,%d,%.2f,%.2f,%.2f,%d,%d,%d",
	// f.ad.id, f.status, f.prize, f.size, f.rooms, f.hasEmail,
	// f.factorPositiveTerms, f.factorNegativeTerms,
	// f.factorProfessionalTerms, f.substandard, f.provision,
	// f.kaution)
	// + booleanIndocatorStringToCsv(f.wordIndicators);
	// };

	public static String fmt(AdFeatures f) {

		return String.format("%d,%d,%.2f,%.2f,%.2f,%d,%d,%d,%d,%d", f.ad.id,
				f.status, f.prize, f.size, f.rooms, f.hasEmail, f.substandard,
				f.provision, f.kaution, f.ablose)
				+ f.wordIndicators;

	};

	@SuppressWarnings("unused")
	private static String booleanIndocatorStringToCsv(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			sb.append("," + s.charAt(i));

		return sb.toString();
	}

}
