package asegroup1.api.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.models.landregistry.LandRegistryData;
import asegroup1.api.models.landregistry.LandRegistryQuery;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint;
import asegroup1.api.services.landregistry.LandRegistryServiceImpl;


@RestController
@RequestMapping("/land-registry/")
public class LandRegistryController {

	private LandRegistryServiceImpl landRegistryService;

	@Autowired
	public LandRegistryController(LandRegistryServiceImpl landRegistryService) {
		this.landRegistryService = landRegistryService;
	}

	@GetMapping("get-addresses/{post-code}")
	public ResponseEntity<?> getAddressDataForPostCode(@PathVariable("post-code") String postCode) {
		try {
			return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getAddressesForPostCode(formatPostCode(postCode))), HttpStatus.OK);
		} catch (UnirestException e) {
			return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("get-transactions/{post-code}")
	public ResponseEntity<?> getTransactionDataForPostCode(@PathVariable("post-code") String postCode) {
		LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
		constraint.getEqualityConstraints().setPostCode(formatPostCode(postCode));
		constraint.setMinDate(LocalDate.now().minusYears(5));

		try {
			return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getLatestTransactions(new ArrayList<>(EnumSet.allOf(Selectable.class)), constraint)),
					HttpStatus.OK);
		} catch (IOException | UnirestException | ParseException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("get-transactionsPC/{post-code}")
	public ResponseEntity<?> getTransactionDataForPostCodePC(@PathVariable("post-code") String postCode) {
		LandRegistryQueryConstraint body = new LandRegistryQueryConstraint();
		body.setPostcodes(postCode);
		body.setMinDate(LocalDate.now().minusYears(5));
		List<Selectable> selectables = Collections.singletonList(Selectable.pricePaid);

		LandRegistryQuery query = LandRegistryQuery.buildQueryAggrigatePostCode(LandRegistryQuery.buildQueryLatestSalesOnly(body, selectables), "postcode", "PricePaid",
				"AveragePrice");

		try {

			return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getTransactions(query)), HttpStatus.OK);
		} catch (IOException | UnirestException e) {
			return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("get-transactionsTown/{town}")
	public ResponseEntity<?> getTransactionFromTown(@PathVariable("town") String town) {
		LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
		constraint.getEqualityConstraints().setTownName(town);
		constraint.setMinDate(LocalDate.now().minusYears(5));

		try {
			return new ResponseEntity<>(getLocationDataKeys(landRegistryService.getLatestTransactions(constraint)),
					HttpStatus.OK);
		} catch (IOException | UnirestException | ParseException e) {
			return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}
	}


	private List<HashMap<String, String>> getLocationDataKeys(List<LandRegistryData> landRegistryDataList) {
		List<HashMap<String, String>> keys = new ArrayList<>();
		for (LandRegistryData data : landRegistryDataList) {
			keys.add(data.getMappings());
		}
		return keys;
	}

	private String formatPostCode(String postCode) {
		if (postCode.charAt(postCode.length() - 4) == 32) {
			return postCode.toUpperCase();
		} else {
			return (postCode.substring(0, postCode.length() - 3) + " " + postCode.substring(postCode.length() - 3)).toUpperCase();
		}
	}
}
