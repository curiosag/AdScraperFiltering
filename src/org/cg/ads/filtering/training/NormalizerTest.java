package org.cg.ads.filtering.training;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cg.common.io.FileUtil;
import org.cg.common.util.CollectionUtil;
import org.cg.common.util.StringUtil;
import org.junit.Test;

public class NormalizerTest {

	String val1 = "1   2   1.1   1,1   1.1,-   Privat schöne Sympathische 2 Zimmer Wohne Nur Privat kein Makler, Miete getrennt begehbar Gepflegte 2 helle Zimmerwohnung in den 3 Liften ne Ablöse, NUR 3 Monatesmiete Kaution, keine Haustiere,nrufen Ab 11.00 Uhr Auskünfte und Besichtigungen Tel. 068181119136 um ein klicken Sie hier";

	//@Test
	public void extractSingleWords() {
		String path = FileUtil.pwd() + "/resources/descriptions.txt";
		String descrriptions = FileUtil.readFromFile(path);
		String dest = path.replace(".txt", ".stem");
		List<String> unique = CollectionUtil.uniqueElements(Normalizer
				.normalize(descrriptions));
		FileUtil.writeToFile(StringUtil.ToCsv(sortByLength(unique), "\n"), dest);
		// System.out.println(Normalizer.normalize(val1));
	}

	private List<String> sortByLength(List<String> list) {
		Collections.sort(list, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		return list;
	}

	@Test
	public void testNorm() {
		for (String s : Normalizer.normalize(val1))
			System.out.println(s);
	}

	// @Test
	public void printTokens() {
		Normalizer.printTokens("aa bb cc 123 dd");
	}

}
