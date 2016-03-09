package org.cg.ads.filtering.training;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.cg.ads.aads.Ad;
import org.cg.common.util.CollectionUtil;

public class AdCsvReader implements Iterable<Ad> {
	private final List<String> colNames = CollectionUtil
			.toList("id,status,statusPredicted,prize,rooms,size,url,location,phone,description,timestamp"
					.split(","));

	private final Iterator<CSVRecord> records;
	private boolean ready = false;

	public AdCsvReader(String filePath) {
		records = load(filePath);
	}

	public Iterator<CSVRecord> load(String filePath) {
		Reader in;
		try {
			in = new FileReader(filePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		try {
			CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(in);
			checkColumns(colNames, parser.getHeaderMap());
			Iterator<CSVRecord> result = parser.iterator();
			ready = true;
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void checkColumns(List<String> colNames,
			Map<String, Integer> headerMap) {
		for (String s : colNames)
			if (!headerMap.containsKey(s))
				throw new RuntimeException("column not found in source: " + s);
	}

	private void checkReady() {
		if (!ready)
			throw new RuntimeException("source not initialized");
	}

	@Override
	public Iterator<Ad> iterator() {
		return new Iterator<Ad>() {

			@Override
			public boolean hasNext() {
				if (!ready)
					return false;

				return records.hasNext();
			}

			@Override
			public Ad next() {
				checkReady();

				if (!hasNext())
					throw new RuntimeException("eof hit");

				return decodeAd(records.next());
			}

			@Override
			public void remove() {
				throw new RuntimeException("not supported");
			}
		};
	}

	private Ad decodeAd(CSVRecord next) {
		return new Ad(next.get("id"), next.get("status"),  next.get("statusPredicted"), next.get("prize"),
				next.get("rooms"), next.get("size"), next.get("url"),
				next.get("location"), next.get("phone"),
				next.get("description"), next.get("timestamp"));
	}

}
