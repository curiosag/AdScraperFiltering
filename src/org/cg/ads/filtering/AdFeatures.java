package org.cg.ads.filtering;

import org.cg.common.util.CollectionUtil;
import org.cg.common.util.StringUtil;

public class AdFeatures {
	
	public final double ppm2;
	public final double rooms;
	public final int hasEmail;
	public final double factorPositiveTerms;
	public final double factorNegativeTerms;
	public final double factorProfessionalTerms;
	public final int substandard;
	public final int provision;
	public final int kaution;

	public final Ad ad;
	
	private final boolean fuzzy = true;
	
	final static String[] negativeTerms = { "airbnb" };
	final static String[] positiveTerms = { "arbeiter", "pendler",
			"arbeitsverhaltnis", "ausweiskopi", "berufstatig",
			"genossenschaft", "besichtigung", "heizung", "makler", "lohnzettel"};
	final static String[] professionalTerms = {"brutto", "netto",
		"energi", "kwh", "ust", "umsatzsteu", "bk", "warmwasser",  "strom"};

	
	public AdFeatures(Ad ad) {
		this.ad = ad;
		ppm2 = ad.prize / ad.size;
		rooms = ad.rooms;
		String norm = StringUtil.ToCsv(Normalizer.normalize(ad.description), " ");
		LuceneQuery query = new LuceneQuery(norm);

		System.out.println(norm);
		
		hasEmail = ad.description.indexOf('@') > 0 ? 1 : 0;
		factorNegativeTerms = query.search(CollectionUtil.toList(negativeTerms), fuzzy);
		factorPositiveTerms = query.search(CollectionUtil.toList(positiveTerms), fuzzy);
		factorProfessionalTerms = query.search(CollectionUtil.toList(professionalTerms), fuzzy);
		kaution = closerTranspositionPhraseQuery(query, "kaution", "<NUM>", 1);
		provision = closerTranspositionPhraseQuery(query, "provision", "<NUM>", 1);
		substandard = query.searchPhrase("wc gang", 2);
	}

	private int closerTranspositionPhraseQuery(LuceneQuery q, String s1, String s2, int slop) {
		return q.searchPhrase(s1 + " " + s2, slop) > 0 ? 1 : q.searchPhrase(s2 + " " + s1, slop);
	}
	
	
}
