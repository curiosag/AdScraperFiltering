package org.cg.ads.filtering;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Attribute;
import org.cg.common.util.StringUtil;

public class Normalizer {

	private final static String anyField = "";
	private static String typeNum = "<NUM>";
	
	public static List<String> normalize(String value) {

		TokenStream stream = null;
		try {
			stream = new GermanAnalyzer().tokenStream(anyField, value);
			try {
				stream.reset();
				return normalize(stream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			if (stream != null)
				try {
					stream.end();
					stream.close();
				} catch (IOException e) {
				}
		}
	}

	private static List<String> normalize(TokenStream tokens) {
		CharTermAttribute termAtt = tokens
				.getAttribute(org.apache.lucene.analysis.tokenattributes.CharTermAttribute.class);
		TypeAttribute typeAtt = tokens.getAttribute(TypeAttribute.class);

		List<String> result = new LinkedList<String>();
		
		while (incToken(tokens))
			if (typeAtt.type().equals(typeNum))
				result.add(typeNum);
			else
				result.add(termAtt.toString());

		return result;
	}

	@SuppressWarnings("unused")
	private static String smthg(TokenStream stream, int charBase,
			List<Integer> requests, Map<Integer, String> results) {
		int defaultInc = 1;

		CharTermAttribute termAtt = stream
				.getAttribute(org.apache.lucene.analysis.tokenattributes.CharTermAttribute.class);
		OffsetAttribute offsetAtt = stream
				.getAttribute(org.apache.lucene.analysis.tokenattributes.OffsetAttribute.class);
		PositionIncrementAttribute incAtt = null;
		if (stream
				.hasAttribute(org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute.class)) {
			incAtt = stream
					.getAttribute(org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute.class);
		}

		int currIdx = 0;
		while (incToken(stream)) {
			currIdx += (incAtt != null) ? incAtt.getPositionIncrement()
					: defaultInc;

			currIdx += (incAtt != null) ? incAtt.getPositionIncrement()
					: defaultInc;
			if (requests.contains(currIdx)) {
				int startOffset = offsetAtt.startOffset() + charBase;
				int endOffset = offsetAtt.endOffset() + charBase;
				results.put(startOffset, termAtt.toString());
			}
			if (currIdx > requests.size() - 1) {

				try {
					stream.end();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				try {
					stream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return "OK";
			}
		}
		return null;
	}

	private static boolean incToken(TokenStream stream) {
		try {
			return stream.incrementToken();
		} catch (IOException e) {
			return false;
		}
	}

	@SuppressWarnings("resource")
	public static void printTokens(String s) {
		printTokenStream(new GermanAnalyzer().tokenStream(anyField, s));
	}

	private static void printTokenStream(TokenStream ts) {
		Iterator<Class<? extends Attribute>> it = ts
				.getAttributeClassesIterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}

		CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
		PositionIncrementAttribute posIncrAtt = ts
				.getAttribute(PositionIncrementAttribute.class);
		PositionLengthAttribute posLenAtt = ts
				.getAttribute(PositionLengthAttribute.class);
		TypeAttribute typeAtt = ts.getAttribute(TypeAttribute.class);
		OffsetAttribute offsetAtt = ts.getAttribute(OffsetAttribute.class);
		TermToBytesRefAttribute byteRefAtt = ts
				.getAttribute(TermToBytesRefAttribute.class);

		try {
			ts.reset();
			while (ts.incrementToken()) {
				int start = offsetAtt.startOffset();
				int end = offsetAtt.endOffset();
				System.out.printf(
						"%3d ~ %3d : %15s : %3d : %3d : '%s' - '%s' : %n",
						start, end, typeAtt.type(),
						posIncrAtt.getPositionIncrement(),
						posLenAtt.getPositionLength(),
						new String(byteRefAtt.getBytesRef().bytes),
						termAtt.toString());

			}
		} catch (IOException e) {
			System.out.println(e.getClass().getSimpleName() + " "
					+ e.getMessage());
		}

	}

}
