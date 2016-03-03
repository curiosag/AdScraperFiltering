package org.cg.ads.filtering;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.cg.common.check.Check;

public class LuceneQuery {
	private final static String anyField = "";

	private final int maxHits;
	private final IndexSearcher searcher;
	private boolean verbose = false;

	/**
	 * Defaults maxHits = 1
	 * 
	 * @param s
	 */
	public LuceneQuery(String s) {
		this(s, 1);
	}

	public LuceneQuery setVerbose() {
		verbose = true;
		return this;
	}

	/**
	 * 
	 * @param s
	 * @param maxHits default of 1 is what we need here. Lucene finds only 1 time in 1 document
	 */
	private LuceneQuery(String s, int maxHits) {
		Check.isTrue(maxHits > 0);
		searcher = createSearcher(s);
		this.maxHits = maxHits;
	}

	/**
	 * 
	 * @param terms
	 * @param fuzzy
	 * @return
	 */
	public float searchWeighted(List<String> terms, boolean fuzzy) {
		Check.isFalse(terms.isEmpty());
		
		float result = 0;

		for (String term : terms)
			if (search(getQueryString(term, fuzzy)) > 0)
				result++;

		return result / terms.size();
	}

	public String search(List<String> terms, boolean fuzzy) {
		Check.isFalse(terms.isEmpty());
		StringBuilder sb = new StringBuilder();

		for (String term : terms)
			if (search(getQueryString(term, fuzzy)) >= 1)
				sb.append("1");
			else
				sb.append("0");

		return sb.toString();
	}
	
	
	private String getQueryString(String term, boolean fuzzy) {
		return fuzzy ? term + "~" : term;
	}

	public int search(String query) {
		try {
			return search(searcher, query);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private IndexSearcher createSearcher(String s) {
		try {
			RAMDirectory dir = new RAMDirectory();
			IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
					new StandardAnalyzer()));

			writer.addDocument(createDocument(s));
			writer.commit();
			writer.close();

			IndexReader reader = DirectoryReader.open(dir);
			return new IndexSearcher(reader);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Document createDocument(String content) {
		Document doc = new Document();
		doc.add(new TextField(anyField, content, Field.Store.YES));
		return doc;
	}

	/**
	 * 
	 * @param searcher
	 * @param queryString
	 * @return	1 if word occurs, 0 otherwise
	 * @throws ParseException
	 * @throws IOException
	 */
	private int search(IndexSearcher searcher, String queryString)
			throws ParseException, IOException {

		Query query;
		try {
			query = new QueryParser(anyField, new StandardAnalyzer())
					.parse(queryString);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			throw new RuntimeException(e);
		}

		TopDocs docs = searcher.search(query, maxHits);
		ScoreDoc[] hits = docs.scoreDocs;

		if (verbose)
			feedback(queryString, docs);

		return hits.length;
	}

	private void feedback(String query, TopDocs docs) {
		ScoreDoc[] hits = docs.scoreDocs;
		System.out.println(hits.length + " hits querying: " + query);
		if (hits.length > 0)
			System.out.println(" weights: ");
		for (int i = 0; i < hits.length; ++i)
			System.out.println("\t" + Float.toString(hits[i].score));
	}

	private Document getDoc(int docId) {
		try {
			return searcher.doc(docId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int searchPhrase(String phrase, int slop) {
		try {
			return searcher.search(new QueryBuilder(new StandardAnalyzer())
					.createPhraseQuery(anyField, phrase, slop), maxHits).totalHits;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
