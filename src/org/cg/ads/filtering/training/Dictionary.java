package org.cg.ads.filtering.training;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.cg.common.check.Check;
import org.cg.common.io.FileUtil;
import org.cg.common.util.StringUtil;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class Dictionary {
	// LinkedHashMap preserves order of insertion
	private final LinkedHashMap<String, Integer> words = new LinkedHashMap<String, Integer>();
	private final String path;

	private final static String dictionaryPath = FileUtil.pwd()
			+ "/resources/dict.csv";
	private final static String ignorePath = FileUtil.pwd()
			+ "/resources/dictIgnore.csv";

	private static Dictionary instance = null;

	private Dictionary(String path) {
		this.path = path;

		System.out.println("Loading dictionary from " + path);
		System.out.println("using terms to ignore from " + ignorePath);

		load();
	}

	public Dictionary(List<Entry<String, Integer>> wordFrequencies) {
		for (Entry<String, Integer> e : sortByFrequency(wordFrequencies))
			words.put(e.getKey(), e.getValue());
		path = null;
	}

	private Dictionary(String dest, List<Entry<String, Integer>> wordFrequencies) {
		this.path = dest;
		System.out.println("Writing dictionary to " + path);
		save(sortByFrequency(wordFrequencies));
		load();
	}

	public Integer[] createWordCounts() {
		Integer[] result = new Integer[words.size()];
		for (int i = 0; i < words.size(); i++)
			result[i] = new Integer(0);
		return result;
	}

	/**
	 * 
	 * @param words
	 *            words to look up in the dictionary. each hit gets counted at
	 *            the array element of the index of the word in the dictionary
	 *            in the return value. If a word is not in the dictionary, it
	 *            won't get counted.
	 * @return int[]
	 */
	public Integer[] createWordCounts(List<String> words) {
		Integer[] result = createWordCounts();
		for (String s : words)
			countOccurence(s, result);
		return result;
	}

	/**
	 * 
	 * @param text
	 *            the text whose tokens should be counted against the dictionary
	 * @param sizeThreshold
	 *            has to be greater than 3. token.length() >= sizeThreshold
	 *            apply fuzzy match, otherwise apply exact match. for words of
	 *            length 2 there would be always a fuzzy match, since Lucene
	 *            allows for an edit distance of 2 for a match.
	 * @return
	 */

	public Integer[] createWordCountsFuzzy(String text, int sizeThreshold) {
		Check.isTrue(sizeThreshold >= 3);

		LuceneQuery fuzzyQuery = new LuceneQuery(text);
		Integer[] result = createWordCounts();
		List<String> shortWords = getShorts(text, sizeThreshold);

		Iterator<String> keys = this.words.keySet().iterator();
		int i = 0;
		while (keys.hasNext()) {
			String current = keys.next();
			boolean isLong = current.length() >= sizeThreshold;
			if ((isLong && fuzzyQuery.search(current + "~") > 0)
					|| (!isLong && shortWords.indexOf(current) >= 0))
				result[i]++;
			i++;
		}
		return result;
	}

	private List<String> getShorts(String text, int sizeThreshold) {
		List<String> result = new ArrayList<String>();
		for (String s : text.split(" "))
			if (s.trim().length() < sizeThreshold)
				result.add(s.trim());
		return result;
	}

	public Integer[] countOccurence(String value, Integer[] wordCounts) {
		Check.notNull(wordCounts);
		Check.isTrue(wordCounts.length == words.size());
		Integer idx = words.get(value);
		if (idx != null)
			wordCounts[idx]++;
		return wordCounts;
	}

	public static List<Entry<String, Integer>> filterDictionary(
			List<Entry<String, Integer>> freq,
			Predicate<Entry<String, Integer>> decide) {
		LinkedList<Entry<String, Integer>> result = new LinkedList<Entry<String, Integer>>();
		for (Entry<String, Integer> entry : freq) {
			if (decide.apply(entry))
				result.add(entry);
		}
		return result;
	}

	private String dictionaryToString(List<Entry<String, Integer>> freq) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> e : freq)
			sb.append(String.format("%d,%s\n", e.getValue(), e.getKey()));
		return sb.toString();
	}

	public String termsToString(String termChar) {
		List<String> out = new LinkedList<String>();
		for (String w : words.keySet())
			out.add(w);

		return StringUtil.ToCsv(out, termChar);
	}

	private void save(List<Entry<String, Integer>> wordFrequencies) {
		String lst = dictionaryToString(wordFrequencies);
		try {
			FileUtils.touch(new File(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		FileUtil.writeToFile(lst, path);
	}

	private Dictionary load() {
		words.clear();

		List<String> lines = readLines(path);
		final List<String> ignore = readLines(ignorePath);
		Collection<String> linesFiltered = Collections2.filter(lines,
				new Predicate<String>() {

					@Override
					public boolean apply(String input) {
						return ignore.indexOf(input) < 0;
					}
				});

		int i = 0;
		for (String s : linesFiltered) {
			String[] parts = s.split(",");
			words.put(parts[1], i);
			i++;
		}

		return this;
	}

	private List<String> readLines(String path) {
		List<String> result;
		try {
			result = FileUtils.readLines(new File(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * generate dictionary from file with descriptions
	 * 
	 * @param args
	 *            args[0] source file with descriptions
	 */

	public static void main(String[] args) {
		if (!(args.length == 1))
			System.out.println("usage: extract <source file path> ");
		else
			try {
				int minFrequency = Integer.MAX_VALUE; // no words for now
				generateFromFile(args[0], minFrequency);
			} catch (Exception e) {
				System.out.println(e.getClass().getName() + " "
						+ e.getMessage());
				e.printStackTrace();
			}
	}

	/**
	 * 
	 * @param srcPath
	 * @param minFrequency
	 *            minimum times a word has to appear in source text to get
	 *            included in dictionary
	 */
	private static void generateFromFile(String srcPath, final int minFrequency) {
		String descriptions = FileUtil.readFromFile(srcPath);
		String dest = dictionaryPath;

		final String regexpWordsOnly = "[a-z]*";

		Predicate<Entry<String, Integer>> filterPredicate = new Predicate<Entry<String, Integer>>() {

			@Override
			public boolean apply(Entry<String, Integer> entry) {
				String word = entry.getKey();
				return entry.getValue() > minFrequency && word.length() > 1
						&& word.matches(regexpWordsOnly);
			}
		};

		List<Entry<String, Integer>> words = filterDictionary(
				aggregateByFrequency(Normalizer.normalize(descriptions)),
				filterPredicate);

		addEmailAdresses(words, descriptions);

		new Dictionary(dest, words);
	}

	private static List<Entry<String, Integer>> addEmailAdresses(
			List<Entry<String, Integer>> words, String descriptions) {

		for (String address : getEmailAdresses(descriptions))
			words.add(createEntry(address, 1));

		return words;
	}

	public static List<String> getEmailAdresses(String source) {
		List<String> result = new LinkedList<String>();

		String patternEmailRFC822 = "(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)";
		Pattern regexEmailRFC822 = Pattern.compile(patternEmailRFC822);

		for (String word : source.split("\\s"))
			if (regexEmailRFC822.matcher(word).matches())
				addAddress(result, word.split("@")[1]);

		return result;
	}

	private static void addAddress(List<String> result, String address) {
		if (result.indexOf(address) < 0)
			result.add(address);
	}

	private static List<Entry<String, Integer>> sortByFrequency(
			List<Entry<String, Integer>> words) {

		List<Entry<String, Integer>> result = new ArrayList<Map.Entry<String, Integer>>();
		result.addAll(words);
		Collections.sort(result, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				return o1.getValue() - o2.getValue();
			}
		});
		return result;
	}

	private static List<Entry<String, Integer>> aggregateByFrequency(
			List<String> list) {
		Map<String, Integer> m = new HashMap<String, Integer>();
		for (String s : list)
			if (m.containsKey(s))
				m.put(s, m.get(s) + 1);
			else
				m.put(s, 1);

		ArrayList<Entry<String, Integer>> result = new ArrayList<Entry<String, Integer>>(
				m.entrySet());
		Collections.sort(result, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				return o1.getValue() - o2.getValue();
			}
		});

		return result;
	}

	public static Map.Entry<String, Integer> createEntry(final String k,
			final Integer v) {
		return new Map.Entry<String, Integer>() {

			private String key = k;
			private Integer value = v;

			@Override
			public String getKey() {
				return key;
			}

			@Override
			public Integer getValue() {
				return value;
			}

			@Override
			public Integer setValue(Integer value) {
				return this.value = value;
			}
		};
	}

	public static Dictionary getInstance() {
		if (instance == null)
			instance = new Dictionary(dictionaryPath);

		return instance;
	}

	public static Dictionary rewriteInstance(
			List<Entry<String, Integer>> wordFrequencies) {
		instance = new Dictionary(dictionaryPath, wordFrequencies);
		return instance;
	}

	public int size()
	{
		return words.size();
	}
	
	public static Dictionary fromCsv(String value) {
		List<Entry<String, Integer>> content = new LinkedList<Map.Entry<String, Integer>>();
		for (String s : value.split(";")) {
			String[] pair = s.split(",");
			if (pair.length == 2)
				content.add(createEntry(pair[1].trim(), Integer.valueOf(pair[0].trim())));
		}
		return new Dictionary(content);
	}

}
