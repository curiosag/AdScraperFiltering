package org.cg.ads.filtering;

import static org.junit.Assert.*;
import java.util.Date;

import org.junit.Test;

public class AdFeaturesTest {

	@Test
	public void testPos() {
		Ad ad = new Ad(
				0,
				0,
				1,
				7,
				2,
				"url",
				"location",
				"phone",
				"la wc airbnb gang eine heizung bk keine kwh makler lohnzettel ust kaution 500 ter aer eaa 20 euro provision  naja@koma.com",
				new Date());
		AdFeatures features = new AdFeatures(ad);

		assertEquals(0.5, features.ppm2, 0.001);
		assertEquals(7, features.rooms, 0.001);
		assertEquals(1, features.hasEmail);

		assertEquals(1, features.factorNegativeTerms, 0.001);
		assertEquals(0.3, features.factorPositiveTerms, 0.001);
		assertEquals(0.33, features.factorProfessionalTerms, 0.01);

		assertEquals(1, features.substandard);
		assertEquals(1, features.kaution);
		assertEquals(1, features.provision);
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
				"am gang da isses wc  eine heizung bk keine kwh makler lohnzettel ust keine kaution provision viell nojo 4000 ",
				new Date());
		AdFeatures features = new AdFeatures(ad);
		
		assertEquals(0, features.hasEmail);
		assertEquals(0, features.substandard);
		assertEquals(0, features.kaution);
		assertEquals(0, features.provision);
	}

}
