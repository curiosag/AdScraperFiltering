package org.cg.ads.filtering;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ad {

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public final int id;
	public final int status;
	public final double prize;
	public final double rooms;
	public final double size;
	public final String url;
	public final String location;
	public final String phone;
	public final String description;
	public final Date timestamp;

	public Ad(int id, int status, double prize, double rooms, double size, String url, String location, String phone,
			String description, Date timestamp) {
		this.id = id;
		this.status = status;
		this.prize = prize;
		this.rooms = rooms;
		this.size = size;
		this.url = url;
		this.location = location;
		this.phone = phone;
		this.description = description;
		this.timestamp = timestamp;
	}

	public Ad(String id, String status, String prize, String rooms, String size, String url, String location,
			String phone, String description, String timestamp) {
		this.id = Integer.parseInt(id);
		this.status = Integer.parseInt(status);
		this.prize = Double.parseDouble(prize);
		this.rooms = Double.parseDouble(rooms);
		this.size = Double.parseDouble(size);
		this.url = url;
		this.location = location;
		this.phone = phone;
		this.description = description;
		this.timestamp = getDate(timestamp);
	}

	private Date getDate(String timestamp) {
		try {
			return DATE_FORMAT.parse(timestamp);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private final static String valuesTemplate = " (%d, %d, %.2f, %.2f, %.2f, '%s', '%s', '%s', '%s', '%s')";

	@SuppressWarnings("boxing")
	public String getInsertStatement(String storageTableId) {
		return "insert into "
				+ storageTableId
				+ " (id, status, prize, rooms, size, url, location, phone, description, timestamp) values "
				+ String.format(valuesTemplate, id, status, prize, rooms, size, url, location, phone, description,
						DATE_FORMAT.format(timestamp));
	}
}
