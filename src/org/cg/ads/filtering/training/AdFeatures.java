package org.cg.ads.filtering.training;

import java.util.List;

import org.cg.ads.aads.Ad;
import org.cg.common.util.CollectionUtil;
import org.cg.common.util.StringUtil;

public class AdFeatures {
	private final static boolean debug = false;

	public final int status;
	public final int statusPredicted;
	public final double prize;
	public final double size;
	public final int phone;
	public final int hasEmail;
	public final int substandard;
	public final int provision;
	public final int kaution;
	public final int ablose;
	public final Integer[] wordIndicators;

	public final Ad ad;

	// aggregate groups of words this way made recognition worse in fact
	// even to put prize and size in a single value prizePerM2 made it
	// significantly worse
	final static String[] negativeTerms = { "airbnb" };
	final static String[] positiveTerms = { "arbeiter", "pendler",
			"arbeitsverhaltnis", "ausweiskopi", "berufstatig",
			"genossenschaft", "besichtigung", "heizung", "makler", "lohnzettel" };
	final static String[] professionalTerms = { "brutto", "netto", "energi",
			"kwh", "ust", "umsatzsteu", "bk", "warmwasser", "strom" };

	public AdFeatures(Ad ad, Dictionary dict) {
		this.ad = ad;
		this.status = ad.status;
		this.statusPredicted = ad.getStatusPredicted() == 1 ? 1 : 0;
		prize = ad.prize;
		size = ad.size;
		phone = StringUtil.emptyOrNull(ad.phone) ? 0 : 1;
		List<String> tokens = Normalizer.normalize(ad.description);
		String norm = StringUtil.ToCsv(tokens, " ");
		LuceneQuery query = new LuceneQuery(norm);

		hasEmail = ad.description.indexOf('@') > 0 ? 1 : 0;
		wordIndicators = dict.createWordIndicators(tokens);

		if (debug) {
			debug(ad.id);
			debug(tokens);
			debug(wordIndicators);
		}

		kaution = transpositionMatch(query, "kaution", "<NUM>", 1);
		provision = transpositionMatch(query, "provision", "<NUM>", 1);
		ablose = transpositionMatch(query, "ablos", "<NUM>", 1);
		substandard = query.searchPhrase("wc gang", 2);
	}

	private void debug(int id) {
		System.out.println("Ad id " + Integer.toString(id) + " tokens");
	}

	private void debug(Integer[] wordIndicators2) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < wordIndicators2.length; i++)
			sb.append(wordIndicators2[i] + " ");

		System.out.println(sb.toString());
	}

	private void debug(List<String> tokens) {

		System.out.println("\n\nwords indicated\n");
		StringBuilder sb = new StringBuilder();
		for (String t : tokens)
			sb.append(t + " ");

		System.out.println(sb.toString());
	}

	public List<String> allTerms() {
		List<String> result = toList(negativeTerms);
		result.addAll(toList(positiveTerms));
		result.addAll(toList(professionalTerms));
		return result;
	}

	public List<String> toList(String[] arr) {
		return CollectionUtil.toList(arr);
	}

	private int transpositionMatch(LuceneQuery q, String s1, String s2, int slop) {
		return q.searchPhrase(s1 + " " + s2, slop) > 0 ? 1 : q.searchPhrase(s2
				+ " " + s1, slop);
	}

}
