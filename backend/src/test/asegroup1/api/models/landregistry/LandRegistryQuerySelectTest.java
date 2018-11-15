/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable;

/**
 * @author Richousrick
 *
 */
class LandRegistryQuerySelectTest {

	private EnumSet<Selectable> selectables;
	private LandRegistryQuerySelect querySelect;

	private static long randomSeed = 8312595207343625996L;


	@BeforeEach
	void initTests() {
		querySelect = new LandRegistryQuerySelect();
		selectables = EnumSet.allOf(Selectable.class);
	}


	private void testQuerySelectEmpty() {
		selectables.forEach(v -> {
			assertFalse(querySelect.hasSelectable(v));
		});
	}

	private void testQuerySelectFull() {
		selectables.forEach(v -> {
			assertTrue(querySelect.hasSelectable(v));
		});
	}

	private void testQuerySelectMatches(List<Selectable> selected) {
		ArrayList<Selectable> nonExistant = new ArrayList<>(selectables);
		nonExistant.removeAll(selected);
		for (Selectable select : selected) {
			assertTrue(querySelect.hasSelectable(select));
		}

		for (Selectable select : nonExistant) {
			assertFalse(querySelect.hasSelectable(select));
		}
	}

	List<Selectable> genRandomSelectables() {
		Random r = new Random(randomSeed);
		ArrayList<Selectable> unSelected = new ArrayList<>(selectables);
		ArrayList<Selectable> selected = new ArrayList<>();
		int reps = r.nextInt(selectables.size() - 1) + 1;
		for (int i = 0; i < reps; i++) {
			selected.add(unSelected.remove(r.nextInt(unSelected.size())));
		}

		return selected;
	}

	Selectable[] buildRandomSelectablesArray(List<Selectable> select) {
		return select.toArray(new Selectable[select.size()]);
	}

	private List<Selectable> fillWithRandomData() {
		List<Selectable> select = genRandomSelectables();
		Selectable[] selectArr = buildRandomSelectablesArray(select);
		querySelect.select(selectArr);
		return select;
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#LandRegistryQuerySelect(asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable[])}.
	 * with no parameters provided.
	 */
	@Test
	void testLandRegistryQuerySelect() {
		testQuerySelectEmpty();
	}


	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#LandRegistryQuerySelect(asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable[])}.
	 */
	@Test
	void testLandRegistryQuerySelectSelectableArray() {
		List<Selectable> select = genRandomSelectables();
		querySelect = new LandRegistryQuerySelect(buildRandomSelectablesArray(select));
		testQuerySelectMatches(select);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#LandRegistryQuerySelect(boolean, asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable[])}.
	 */
	@Test
	void testLandRegistryQuerySelectBooleanSelectableArray() {
		List<Selectable> select = genRandomSelectables();
		querySelect = new LandRegistryQuerySelect(false, buildRandomSelectablesArray(select));
		testQuerySelectMatches(select);


		select = genRandomSelectables();
		ArrayList<Selectable> inverse = new ArrayList<>(selectables);
		inverse.removeAll(select);
		querySelect = new LandRegistryQuerySelect(true, buildRandomSelectablesArray(select));
		testQuerySelectMatches(inverse);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#select(asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable[])}.
	 */
	@Test
	void testSelectDeselectOne() {
		List<Selectable> select = genRandomSelectables();
		for (Selectable selectable : select) {
			querySelect.select(selectable);
			assertTrue(querySelect.hasSelectable(selectable));
			querySelect.deselect(selectable);
			assertFalse(querySelect.hasSelectable(selectable));
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#select(asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable[])}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#deselect(asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable[])}.
	 */
	void testSelectDeselectMany() {
		List<Selectable> select = fillWithRandomData();
		testQuerySelectMatches(select);
		querySelect.deselect(buildRandomSelectablesArray(select));
		testQuerySelectEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#selectAll()}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#deselectAll()}.
	 */
	@Test
	void testSelectDeselectAllEmpty() {
		querySelect.selectAll();
		testQuerySelectFull();
		querySelect.deselectAll();
		testQuerySelectEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#selectAll()}
	 * when there is some data in select
	 */
	@Test
	void testSelectAllMixed() {
		fillWithRandomData();
		querySelect.selectAll();
		testQuerySelectFull();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#deselectAll()}
	 * when there is some data in select
	 */
	@Test
	void testDeselectAllMixed() {
		fillWithRandomData();
		querySelect.deselectAll();
		testQuerySelectEmpty();
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#toggleSelectable(asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable)}.
	 */
	@Test
	void testToggleSelectable() {
		List<Selectable> initial = fillWithRandomData();
		List<Selectable> toToggle = genRandomSelectables();
		List<Selectable> expected = new ArrayList<Selectable>(initial);
		toToggle.forEach(v -> {
			if (expected.contains(v)) {
				expected.remove(v);
			} else {
				expected.add(v);
			}
		});

		for (Selectable selectable : toToggle) {
			querySelect.toggleSelectable(selectable);
		}

		testQuerySelectMatches(expected);

	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelect()}.
	 */
	@Test
	void testBuildQuerySelect() {
		List<Selectable> initial = fillWithRandomData();
		String regex;
		StringBuilder regexBuilder = new StringBuilder("SELECT(\\s\\?(");
		initial.forEach(v -> {
			regexBuilder.append("(" + v.toString() + ")|");
		});
		regexBuilder.deleteCharAt(regexBuilder.length() - 1);
		regexBuilder.append("))+");
		regex = regexBuilder.toString();

		assertTrue(querySelect.buildQuerySelect().matches(regex));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelectUnique()}.
	 */
	@Test
	void testBuildQuerySelectUniqueFilled() {
		ArrayList<Selectable> usedSelectables = new ArrayList<>(
				Arrays.asList(new Selectable[] { Selectable.paon, Selectable.saon, Selectable.street, Selectable.postcode, Selectable.transactionDate }));
		selectables.removeAll(usedSelectables);
		List<Selectable> initial = fillWithRandomData();
		testBuildQuerySelectUnique(initial);
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelectUnique()}.
	 */
	@Test
	void testBuildQuerySelectUniqueEmpty() {
		testBuildQuerySelectUnique(new ArrayList<>());
	}


	void testBuildQuerySelectUnique(List<Selectable> initial) {
		String regex = buildQuerySelectRegex(initial);

		System.out.println(querySelect.buildQuerySelectUnique());

		assertTrue(querySelect.buildQuerySelectUnique().matches(regex));
	}



	String buildQuerySelectRegex(List<Selectable> selectables) {
		StringBuilder select = new StringBuilder("SELECT \\?paon \\?saon \\?street \\?postcode \\(max\\(\\?transactionDate\\) AS \\?TransactionDate\\)");



		if (!selectables.isEmpty()) {
			StringBuilder extention = new StringBuilder();
			StringBuilder sample = new StringBuilder("SAMPLE\\(\\?");
			StringBuilder variableLower = new StringBuilder();
			StringBuilder variableUpper = new StringBuilder();
			selectables.forEach(v -> {
				variableLower.append("(" + v.toString() + ")|");
				variableUpper.append("(" + v.toString().substring(0, 1).toUpperCase() + v.toString().substring(1) + ")|");
			});
			variableLower.deleteCharAt(variableLower.length() - 1);
			variableUpper.deleteCharAt(variableUpper.length() - 1);

			sample.append("(" + variableLower + ")\\) AS \\?(" + variableUpper + ")");
			System.out.println(sample);

			extention.append("(\\(" + sample + "\\)\\s*)+");
			select.append("\\s*" + extention);
		}
		
		return select.toString();

	}
}