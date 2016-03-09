package org.cg.ads.filtering.training;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.cg.ads.filtering.training.LuceneFactory.AnalyzerType;
import org.cg.common.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class LuceneQueryTest {

	final static int matchExactly = 0;
	final static int matchTranspositions = 2;

	@Before
	public void setUp() throws Exception {
		LuceneFactory.instance().setAnalyzerType(AnalyzerType.standard);
	}
	
	@Test
	public void testEditDistance() {
		
		String val100 = "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyz";

		String val1Edit = "a1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyz";
		String val2Edits = "ab234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyz";
		String val3Edits = "abc34567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyz";

		LuceneQuery q = new LuceneQuery(val100).setVerbose();

		assertEquals(1, q.search(val1Edit + "~"));
		assertEquals(1, q.search(val2Edits + "~")); // matches max 2 edits
		assertEquals(0, q.search(val3Edits + "~")); // and fails for 3
	}

	@Test
	public void testSingleLetter() {
		
		String val = "a bb";
		LuceneQuery q = new LuceneQuery(val).setVerbose();

		assertEquals(0, q.search("a")); // won't match a single letter
		assertEquals(1, q.search("bb"));
	}

	@Test
	public void testMultipleWildcards() {
		
		String val = "avva  bc bxd ee ff gg";
		LuceneQuery q = new LuceneQuery(val).setVerbose();

		// gives higher weights for more matches. order doesen't matter
		assertEquals(1, q.search("a*a"));
		assertEquals(0, q.search("a?a"));
		assertEquals(1, q.search("a*a AND b*d"));
		
		
	}
	
	@Test
	public void testMultipleWords() {
		
		String val = "aa bb cc dd ee ff gg";
		LuceneQuery q = new LuceneQuery(val).setVerbose();

		// gives higher weights for more matches. order doesen't matter
		assertEquals(1, q.search("aa")); // 0.11
		assertEquals(1, q.search("aa xx [aa TO xx]")); // 0.47
		assertEquals(1, q.search("aa dd ff xx [aa TO xx]")); // 0.58
		assertEquals(1, q.search("aa cc bb dd ff ee xx [aa TO xx]")); // 0.66
		assertEquals(1, q.search("ee ff gg aa bb cc dd [aa TO xx]")); // 0.96
		assertEquals(1, q.search("aa bb cc dd ee ff gg [aa TO xx]")); // 0.96
	}

	@Test
	public void testMultipleWordsExactWeight() {
		
		final double delta = 0.001;
		final boolean fuzzy = true;
		String val = "aa bb cc dd ee ff gg hh ii jj";
		LuceneQuery q = new LuceneQuery(val).setVerbose();

		// gives higher weights for more matches. order doesen't matter
		assertEquals(0, q.searchWeighted(fromCsv("xx"), !fuzzy), delta);
		assertEquals(0, q.searchWeighted(fromCsv("xx, yy"), !fuzzy), delta);
		
		assertEquals(0, q.searchWeighted(fromCsv("xa, xx"), !fuzzy), delta);
		assertEquals(0.5, q.searchWeighted(fromCsv("xa, xx"), fuzzy), delta);
		
		assertEquals(0.1, q.searchWeighted(
				fromCsv("aa, zz, yy, xx, ww, vv, uu, tt, ss, rr"), false),
				delta);
	}

	private List<String> fromCsv(String csv) {
		LinkedList<String> result = new LinkedList<String>();
		for (String s : csv.split(","))
			result.add(s.trim());

		return result;
	}

	 @Test
	public void testGermanPhrases() {
		
		LuceneQuery q = new LuceneQuery(
				"im fruhtau zu berge wir gehn falleraaaaaaah!").setVerbose();

		assertEquals(0, q.search("onk"));
		assertEquals(1, q.search("wir"));

		assertEquals(1, q.search(quote("zu wir") + "~1"));
		assertEquals(0, q.search(quote("zu gehn") + "~1"));
		assertEquals(1, q.search(quote("zu gehn") + "~2"));

		assertEquals(1, q.search("gehn"));
		assertEquals(0, q.search("gen"));
		assertEquals(1, q.search("gen~")); // Edit Distance match
		assertEquals(0, q.search(quote("zu gen~") + "~2")); // phrase + fuzzy
															// doesen't work

		assertEquals(0, q.searchPhrase("onk wir", 1));
		assertEquals(1, q.searchPhrase("wir gehn", matchExactly));
		assertEquals(1, q.searchPhrase("gehn wir", matchTranspositions)); // ??

		assertEquals(0, q.searchPhrase("zu wir", matchExactly));
		assertEquals(1, q.searchPhrase("zu wir", 1));
		assertEquals(0, q.searchPhrase("wir zu", 1));
		assertEquals(0, q.searchPhrase("wir zu", matchTranspositions)); // ??
		
		String val = "la wc airbnb gang eine heizung bk keine kwh makler lohnzettel ust kaution 500 ter aer eaa 20 euro provision  naja@koma.com";

		q = new LuceneQuery(StringUtil.ToCsv(Normalizer.normalize(val), " "));
		assertEquals(1, q.search("kaution <NUM>"));
		assertEquals(1, q.searchPhrase("kaution <NUM>", 2));
	}

	private String quote(String s) {
		String quote = "\"";
		return quote + s + quote;
	}

}
