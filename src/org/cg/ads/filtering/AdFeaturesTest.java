package org.cg.ads.filtering;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class AdFeaturesTest {
	
	private static Dictionary dict = Dictionary.getInstance();
	
	private Ad createPositiveAd() {
		return new Ad(
				0,
				0,
				1,
				7,
				2,
				"url",
				"location",
				"phone",
				"la wc airbnb gang Ablöse 75.000.000.- eine heizung bk keine kwh makler lohnzettel ust kaution 500 ter aer eaa 20 euro provision  naja@koma.com",
				new Date());
	}
	
	@Test
	public void testFormat() {
		System.out.println(AdFeaturesFormat.fmt(new AdFeatures(createPositiveAd(), dict)));
		String formatted = AdFeaturesFormat.fmt(new AdFeatures(createPositiveAd(), dict));
		assertTrue(formatted.startsWith("0,0,1.00,2.00,7.00,1,1,1,1,1"));
		
	}
	
	@Test
	public void testPos() {
		Ad ad = createPositiveAd();
		AdFeatures features = new AdFeatures(ad, dict);

		assertEquals(1, features.prize, 0.001);
		assertEquals(2, features.size, 0.001);
		assertEquals(7, features.rooms, 0.001);
		assertEquals(1, features.hasEmail);

		
		assertEquals(1, features.substandard);
		assertEquals(1, features.kaution);
		assertEquals(1, features.provision);
		assertEquals(1, features.ablose);
	}

	@Test
	public void testNeg() {
		Ad ad = new Ad(
				0,
				0,
				1,
				7,
				2,
				"url",
				"location",
				"phone",
				"am gang, da isses wc  eine heizung Ablöse riesig bk keine kwh makler lohnzettel ust keine kaution provision viell nojo 4000 ",
				new Date());
		AdFeatures features = new AdFeatures(ad, dict);
		
		assertEquals(0, features.hasEmail);
		assertEquals(0, features.substandard);
		assertEquals(0, features.kaution);
		assertEquals(0, features.provision);
		assertEquals(0, features.ablose);
	}

}
