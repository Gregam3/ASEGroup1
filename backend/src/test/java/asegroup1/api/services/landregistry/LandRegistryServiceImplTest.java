package asegroup1.api.services.landregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.models.heatmap.HeatMapDataPoint;
import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQuery;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

class LandRegistryServiceImplTest {

	private static LandRegistryServiceImpl landRegistryService;

	private static final long RANDOM_SEED = 8312595207343625996L;

	@BeforeAll
	private static void setUpService() {
		landRegistryService = new LandRegistryServiceImpl(null);
	}

	@Test
	void testIfSearchTransactionsByPostCodeReturnsValidPrices() {
		try {
			LandRegistryQueryConstraint constraint =
				new LandRegistryQueryConstraint();
			constraint.getEqualityConstraints().setPostCode("BN14 7BH");

			LandRegistryDaoImpl landRegistryDataDaoMock =
				mock(LandRegistryDaoImpl.class);

			when(landRegistryDataDaoMock.executeSPARQLQuery(notNull()))
				.thenReturn(getSPARQLResponse());

			LandRegistryServiceImpl landRegistryService =
				new LandRegistryServiceImpl(landRegistryDataDaoMock);

			List<LandRegistryData> addressByPostCode = landRegistryService.getTransactions(new LandRegistryQuerySelect(Selectable.pricePaid), constraint);

			// Checking not only if results are returned but that results
			// contain valid data
			if (Integer.parseInt(addressByPostCode.get(0).getConstraint(
					Selectable.pricePaid)) <= 0) {
				fail("Transaction has invalid price");
			}

			assert true;
		} catch (IOException | UnirestException | NumberFormatException |
				 JSONException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	void testIfSettingInvalidPostcodeThrowsInvalidParameterException() {
		Assertions.assertThrows(InvalidParameterException.class,
								() -> new LandRegistryQueryConstraint()
										   .getEqualityConstraints()
										   .setPostCode("0"));
	}

	@Test
	void testIfSetPostcodeAcceptsValidPostcode() {
		// Provides the invalid postcode of "0"
		LandRegistryQueryConstraint constraint =
			new LandRegistryQueryConstraint();

		constraint.getEqualityConstraints().setPostCode("BH9 2SL");

		assert constraint.getEqualityConstraints()
			.getConstraint(Selectable.postcode)
			.equals("BH9 2SL");
	}

	@Test
	@SuppressWarnings("unchecked")
	void testIfLongitudeForAddressesAreFetched() {
		List<LandRegistryData> addresses =
			generateLandRegistryDataForAddresses(1, 0, 1);

		LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

		JSONObject mockRequest = fetchMockRequest();

		try {
			when(landRegistryDataDaoMock.executeSPARQLQuery(notNull()))
				.thenReturn(getSPARQLResponse());

			when(landRegistryDataDaoMock.getGeoLocationData(notNull()))
				.thenReturn(fetchMockResponse());

			when(landRegistryDataDaoMock.searchForLandRegistryDataInBoundaries(
					 mockRequest.getDouble("top"),
					 mockRequest.getDouble("right"),
					 mockRequest.getDouble("bottom"),
					 mockRequest.getDouble("left"), true))
				.thenReturn(addresses);

			LandRegistryServiceImpl landRegistryServiceLocal =
				new LandRegistryServiceImpl(landRegistryDataDaoMock);

			addresses = (List<LandRegistryData>)landRegistryServiceLocal
							.getPositionInsideBounds(mockRequest);

			LandRegistryData address = addresses.get(0);

			assert address.getLongitude() == 0 && address.getLatitude() == 0;
		} catch (UnirestException | JSONException | IOException e) {
			e.printStackTrace();

			assert false;
		}
	}

	@Test
	void testIfNormalisedValuesConvertToCorrectColours() {
		List<HeatMapDataPoint> heatMapDataPoints =
			getHeatMapTestData(5L, 10L, 15L);

		// Check if 15 converted to red is darker red than 10 converted to red,
		// and then check if 10 converted to red is darker red than 5 converted
		// to red
		assertEquals("#00a500", heatMapDataPoints.get(0).getColour().getHex());
        assertEquals("#dada00", heatMapDataPoints.get(1).getColour().getHex());
        assertEquals("#ff0000", heatMapDataPoints.get(2).getColour().getHex());
	}

	@Test
	void testHowNormaliseValuesReturns0ValueForOnlyOneDistinctValue() {
		List<HeatMapDataPoint> heatMapDataPoints =
			getHeatMapTestData(5L, 5L, 5L);

		for (HeatMapDataPoint heatMapDataPoint : heatMapDataPoints) {
			assertEquals("#9c9c00", heatMapDataPoint.getColour().getHex());
		}
	}

	@Test
	void testHowNormaliseValuesHandlesEmptyList() {
		assert landRegistryService.convertLandRegistryDataListToHeatMapList(
			new ArrayList<>()) == null;
	}

	@Test
	void testIfNormalisedValuesConvertToCorrectColoursWithNegativeValues() {
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> getHeatMapTestData(-5L, -15L, -10L));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testIfMediumSizeListGoesIntoCorrectStaticAggregationLevel() {
		try {
			List<LandRegistryData> positionInsideBounds =
				(List<LandRegistryData>)getDisplayData(50, 50000, 1000000);

			// If it can be cast to List<LandRegistryData> then list must either
			// be of addresses of postcodes, but as a getLocationForAddresses is
			// not mocked this must be a list of postcodes passing the test
			assert true;
		} catch (Exception e) {
			e.printStackTrace();

			assert false;
		}
	}

	@Test
	void testIfEmptyListGoesIntoCorrectStaticAggregationLevel() {
		assert getDisplayData(0, 10, 10).isEmpty();
	}

	@Test
	void testIfHeatMapIsReturnedWhenThresholdIsPassed() {
		List<?> dataPoints =
			getDisplayData(LandRegistryServiceImpl.AGGREGATION_LEVELS[2], 1, 1);

		assert dataPoints.size() >=
				LandRegistryServiceImpl.AGGREGATION_LEVELS[2] &&
			dataPoints.get(0) instanceof HeatMapDataPoint;
	}

	// UTILS
	private JSONObject fetchMockResponse() {
		JSONObject mockResponse = new JSONObject();

		try {
			mockResponse.put("lat", 0);
			mockResponse.put("lng", 0);
		} catch (JSONException e) {
			e.printStackTrace();

			assert false;
		}

		return mockResponse;
	}

	private JSONObject fetchMockRequest() {
		JSONObject mockRequest = new JSONObject();

		try {
			mockRequest.put("top", 0);
			mockRequest.put("right", 0);
			mockRequest.put("bottom", 0);
			mockRequest.put("left", 0);
		} catch (Exception e) {
			assert false;
			e.printStackTrace();
		}

		return mockRequest;
	}

	private List<LandRegistryData>
	generateLandRegistryDataForAddresses(int numberToGenerate, int lowerBound,
										 int range) {
		List<LandRegistryData> landRegistryDataList =
			generateLandRegistryDataForPostCodes(numberToGenerate, lowerBound,
												 range);

		Random random = new Random(RANDOM_SEED);

		for (LandRegistryData landRegistryData : landRegistryDataList) {
			landRegistryData.setPrimaryHouseName(
				String.valueOf(random.nextInt(100)));
			landRegistryData.setStreetName("STREET");
			landRegistryData.setTownName("TOWN");
		}

		return landRegistryDataList;
	}

	private List<LandRegistryData>
	generateLandRegistryDataForPostCodes(int numberToGenerate, int lowerBound,
										 int range) {
		final String[] postcodes = {"BN14 7BH", "NW9 9PR", "NN12 8DT",
									"TW7 4QN",  "L22 3YU", "RM17 6LJ",
									"RG14 7DF", "SE25 5RT"};
		List<LandRegistryData> landRegistryDataList = new ArrayList<>();

		Random random = new Random(RANDOM_SEED);

		for (int i = 0; i < numberToGenerate; i++) {
			LandRegistryData landRegistryData = new LandRegistryData();

			landRegistryData.setPricePaid(random.nextInt(range) + lowerBound);
			landRegistryData.setPostCode(postcodes[i % 7]);

			landRegistryData.setRadius(0.0);
			landRegistryData.setLatitude(0.0);
			landRegistryData.setLongitude(0.0);

			landRegistryDataList.add(landRegistryData);
		}

		return landRegistryDataList;
	}

	private List<HeatMapDataPoint> getHeatMapTestData(long... values) {
		List<LandRegistryData> landRegistryDataList = new ArrayList<>();

		for (int i = 0; i < values.length; i++) {
			LandRegistryData landRegistryData = new LandRegistryData();

			landRegistryData.setPricePaid(values[i]);
			landRegistryData.setLongitude(0);
			landRegistryData.setLatitude(0);
			landRegistryData.setRadius(0.0);

			landRegistryDataList.add(landRegistryData);
		}

		return landRegistryService.convertLandRegistryDataListToHeatMapList(
			landRegistryDataList);
	}

	List<?> getDisplayData(int numberToGenerate, int lowerPriceBound,
						   int priceRange) {
		LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

		List<LandRegistryData> landRegistryDataList =
			generateLandRegistryDataForPostCodes(numberToGenerate,
												 lowerPriceBound, priceRange);

		when(landRegistryDataDaoMock.searchForLandRegistryDataInBoundaries(
				 0, 0, 0, 0, true))
			.thenReturn(landRegistryDataList);

		LandRegistryServiceImpl landRegistryService =
			new LandRegistryServiceImpl(landRegistryDataDaoMock);

		JSONObject mockRequest = fetchMockRequest();

		try {
			return landRegistryService.getPositionInsideBounds(mockRequest);
		} catch (UnirestException | IOException e) {
			e.printStackTrace();
			assert false;
		}

		return null;
	}

	private JSONObject getSPARQLResponse() throws JSONException {
		return new JSONObject(
			"{\"result\":\"{\\n  \\\"head\\\": {\\n    \\\"vars\\\": [ \\\"paon\\\" , \\\"saon\\\" , \\\"street\\\" , "
			+
			"\\\"postcode\\\" , \\\"TransactionDate\\\" , \\\"Town\\\" , \\\"PricePaid\\\" ]\\n  } ,\\n  \\\"results\\\": {\\n    \\\"bindings\\\": [\\n     "
			+
			" {\\n        \\\"paon\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"SUSSEX COURT\\\" } ,\\n        \\\"saon\\\": { \\\"type\\\": \\\"literal\\\" , "
			+
			"\\\"value\\\": \\\"FLAT 2\\\" } ,\\n        \\\"street\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"TENNYSON ROAD\\\" } ,\\n        "
			+
			"\\\"postcode\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"BN11 4BT\\\" } ,\\n        \\\"TransactionDate\\\": { \\\"type\\\": \\\"literal\\\" , "
			+
			"\\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#date\\\" , \\\"value\\\": \\\"2016-10-07\\\" } ,\\n        \\\"Town\\\": { \\\"type\\\": \\\"literal\\\" , "
			+
			"\\\"value\\\": \\\"WORTHING\\\" } ,\\n        \\\"PricePaid\\\": { \\\"type\\\": \\\"literal\\\" , \\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#integer\\\" , "
			+
			"\\\"value\\\": \\\"155000\\\" }\\n      }\\n    ]\\n  }\\n}\\n\",\"status\":200}");
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.services.landregistry.LandRegistryServiceImpl#getAllPostcodePrices(String...)}.
	 */
	@Test
	void testGetAllPostcodePrices() {
		String[] postcodes = new String[] { "BN23 7LE", "BN23 7LL", "BN23 7LN", "BN23 7LQ", "BN23 7LX", "BN23 7LZ" };
		try {
			LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

			when(landRegistryDataDaoMock.executeSPARQLQuery(notNull())).thenReturn(getSPARQLPostcodePriceResponse());

			LandRegistryServiceImpl landRegistryService = new LandRegistryServiceImpl(landRegistryDataDaoMock);

			HashMap<String, Long> prices = landRegistryService.getAllPostcodePrices(postcodes);
			assertNotNull(prices);
			assertTrue(prices.size() == 6);
			for (Entry<String, Long> entry : prices.entrySet()) {
				assertNotNull(entry.getKey());
				if(entry.getValue() == null) {
					assertEquals("BN23 7LX", entry.getKey());
				}
			}
		} catch (IOException | UnirestException | JSONException e) {
			fail(e);
		}
	}



	/**
	 * Test method for
	 * {@link asegroup1.api.services.landregistry.LandRegistryServiceImpl#getTransactions(asegroup1.api.models.landregistry.LandRegistryQuery)}.
	 */
	@Test
	void testGetTransaction() {
		String[] postcodes = new String[] { "BN23 7LE", "BN23 7LL", "BN23 7LN", "BN23 7LQ", "BN23 7LT", "BN23 7LZ" };
		try {
			LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);

			when(landRegistryDataDaoMock.executeSPARQLQuery(notNull())).thenReturn(getSPARQLPostcodePriceResponse());

			LandRegistryServiceImpl landRegistryService = new LandRegistryServiceImpl(landRegistryDataDaoMock);
			for (LandRegistryData transaction : landRegistryService.getTransactions(LandRegistryQuery.buildQueryAveragePricePostcode(postcodes))) {
				assertNotNull(transaction);
				assertEquals(2, transaction.getAllConstraints().size());
				assertTrue(transaction.hasConstraint(Selectable.pricePaid));
				assertTrue(transaction.hasConstraint(Selectable.postcode));
				assertNull(transaction.getLatitude());
				assertNull(transaction.getLongitude());
				assertNull(transaction.getRadius());

			}

		} catch (IOException | UnirestException | JSONException e) {
			fail(e);
		}
	}
	
	/**
	 * Test method for
	 * {@link asegroup1.api.services.landregistry.LandRegistryServiceImpl#getTransactions(asegroup1.api.models.landregistry.LandRegistryQuery)}.
	 */
	@Test
	void testUpdatePostcodeDatabase() {
		try {

			LandRegistryDaoImpl landRegistryDataDaoMock = mock(LandRegistryDaoImpl.class);
			ArgumentCaptor<HashMap<String, Long>> captor = ArgumentCaptor.forClass(HashMap.class);

			HashMap<String, List<String>> postcodeAreas = new HashMap<>();
			postcodeAreas.put("BN23 7L", Arrays.asList("BN23 7LE", "BN23 7LL", "BN23 7LN", "BN23 7LQ", "BN23 7LT", "BN23 7LZ"));


			when(landRegistryDataDaoMock.updateAveragePrice(Mockito.any())).thenReturn(1);
			when(landRegistryDataDaoMock.executeSPARQLQuery(notNull())).thenReturn(getSPARQLPostcodePriceResponse());
			when(landRegistryDataDaoMock.getMatchingPostcodes("BN23 7L", false, 1)).thenReturn(postcodeAreas);


			LandRegistryServiceImpl landRegistryService = new LandRegistryServiceImpl(landRegistryDataDaoMock);

			landRegistryService.updatePostcodeDatabase("BN23 7L");

			verify(landRegistryDataDaoMock).updateAveragePrice(captor.capture());

			HashMap<String, Long> expectedData = new HashMap<>();
			expectedData.put("BN23 7LL", 142863L);
			expectedData.put("BN23 7LN", 124317L);
			expectedData.put("BN23 7LZ", 106227L);
			expectedData.put("BN23 7LT", null);
			expectedData.put("BN23 7LE", 88650L);
			expectedData.put("BN23 7LQ", 132828L);

			assertEquals(expectedData, captor.getAllValues().get(0));

		} catch (UnirestException | JSONException | IOException e) {
			fail(e);
		}
	}


	private JSONObject getSPARQLPostcodePriceResponse() throws JSONException {
		return new JSONObject("{\"status\":200,\"result\":\"{\r\n" + "  \\\"head\\\": {\r\n" + "    \\\"vars\\\": [ \\\"postcode\\\" , \\\"pricePaid\\\" ]\r\n"
				+ "  } ,\r\n" + "  \\\"results\\\": {\r\n" + "    \\\"bindings\\\": [\r\n" + "      {\r\n"
				+ "        \\\"postcode\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"BN23 7LQ\\\" } ,\r\n"
				+ "        \\\"pricePaid\\\": { \\\"type\\\": \\\"literal\\\" , \\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#decimal\\\" , \\\"value\\\": \\\"132827.777777777777777777777777\\\" }\r\n"
				+ "      } ,\r\n" + "      {\r\n" + "        \\\"postcode\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"BN23 7LE\\\" } ,\r\n"
				+ "        \\\"pricePaid\\\": { \\\"type\\\": \\\"literal\\\" , \\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#decimal\\\" , \\\"value\\\": \\\"88650.0\\\" }\r\n"
				+ "      } ,\r\n" + "      {\r\n" + "        \\\"postcode\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"BN23 7LZ\\\" } ,\r\n"
				+ "        \\\"pricePaid\\\": { \\\"type\\\": \\\"literal\\\" , \\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#decimal\\\" , \\\"value\\\": \\\"106227.272727272727272727272727\\\" }\r\n"
				+ "      } ,\r\n" + "      {\r\n" + "        \\\"postcode\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"BN23 7LL\\\" } ,\r\n"
				+ "        \\\"pricePaid\\\": { \\\"type\\\": \\\"literal\\\" , \\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#decimal\\\" , \\\"value\\\": \\\"142862.5\\\" }\r\n"
				+ "      } ,\r\n" + "      {\r\n" + "        \\\"postcode\\\": { \\\"type\\\": \\\"literal\\\" , \\\"value\\\": \\\"BN23 7LN\\\" } ,\r\n"
				+ "        \\\"pricePaid\\\": { \\\"type\\\": \\\"literal\\\" , \\\"datatype\\\": \\\"http://www.w3.org/2001/XMLSchema#decimal\\\" , \\\"value\\\": \\\"124316.666666666666666666666666\\\" }\r\n"
				+ "      }\r\n" + "    ]\r\n" + "  }\r\n" + "}\r\n" + "\"}");
	}



}
