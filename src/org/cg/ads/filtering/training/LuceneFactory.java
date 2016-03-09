package org.cg.ads.filtering.training;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import com.googlecode.luceneappengine.GaeDirectory;
import com.googlecode.luceneappengine.GaeLuceneUtil;

import org.apache.lucene.store.RAMDirectory;

import static com.googlecode.luceneappengine.GaeLuceneUtil.getIndexWriterConfig;

public class LuceneFactory {

	private boolean appEngineCompatible = false;
	private AnalyzerType analyzerType = AnalyzerType.german;

	private static LuceneFactory instance = null;

	public enum AnalyzerType {
		standard, german
	};

	public Directory createDirectory() {
		if (appEngineCompatible)
			return new GaeDirectory("ads");
		else
			return new RAMDirectory();
	}

	public Analyzer createAnalyzer() {
		switch (analyzerType) {
		case standard:
			return new StandardAnalyzer();

		case german:
			return new GermanAnalyzer();

		default:
			throw new RuntimeException("invalid analyzer type "
					+ analyzerType.name());
		}

	}

	public boolean isAppEngineCompatible() {
		return appEngineCompatible;
	}

	public void setAppEngineCompatible(boolean value) {
		appEngineCompatible = value;
	}

	public AnalyzerType getAnalyzerType() {
		return analyzerType;
	}

	public void setAnalyzerType(AnalyzerType analyzerType) {
		this.analyzerType = analyzerType;
	}

	public IndexWriter createIndexWriter()
	{
		IndexWriterConfig config = getIndexWriterConfig(createAnalyzer());
		try {
			return new IndexWriter(createDirectory(), config);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static LuceneFactory instance() {
		if (instance == null)
			instance = new LuceneFactory();

		return instance;
	}

}
