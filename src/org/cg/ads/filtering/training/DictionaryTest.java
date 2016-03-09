package org.cg.ads.filtering.training;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class DictionaryTest {

	@Test
	public void test() {
		List<Entry<String, Integer>> words = new ArrayList<Map.Entry<String, Integer>>();
		words.add(Dictionary.createEntry("aaaaaa", 1));
		words.add(Dictionary.createEntry("bbbbbb", 2));
		Dictionary d = Dictionary.rewriteInstance(words);
		Integer[] counts = d.createWordNumArray();
		d.countOccurence("bbbbbb", counts);
		d.countOccurence("aaaaax", counts);
		assertArrayEquals(new Integer[] { 0, 1 }, counts);
		
		int minWordSizeForFuzzyMatch = 7;
		counts = d.createWordCountsFuzzy("aaaaax bbbbbb", minWordSizeForFuzzyMatch);
		assertArrayEquals(new Integer[] { 0, 1 }, counts);
		
		minWordSizeForFuzzyMatch = 6;
		counts = d.createWordCountsFuzzy("aaaaax bbbbbb", minWordSizeForFuzzyMatch);
		assertArrayEquals(new Integer[] { 1, 1 }, counts);
		
	}
	
	@Test
	public void testMailAdressExtractor() {
		String source = " per E-Mail : ^^^^^ yasemin1976@crapmail.org ^^^itere Informationen : Philipp_Wulf@hotmail.com";
		Dictionary.getEmailAdresses(source);
	}
	

}
