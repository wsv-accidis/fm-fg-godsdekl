package se.accidis.fmfg.app.services;

import androidx.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.model.Label;
import se.accidis.fmfg.app.model.Material;

/**
 * Repository for labels. Static class, holds all data in memory.
 */
public final class LabelsRepository {
	private static final Map<String, Label> sLabels;
	private static final Label sMiljoLabel;

	private LabelsRepository() {
	}

	public static Collection<Label> getAllLabels() {
		return Collections.unmodifiableCollection(sLabels.values());
	}

	public static List<Integer> getLabelsByDocument(Document document, boolean smallImages) {
		boolean hasMiljo = false;
		SortedSet<String> etiketterSet = new TreeSet<>();
		for (DocumentRow row : document.getRows()) {
			Material material = row.getMaterial();
			if (row.isMiljoFarligt()) {
				hasMiljo = true;
			}
			etiketterSet.addAll(getDisplayEtiketter(material));
		}

		List<Integer> labelDrawables = new ArrayList<>();
		for (String etiketter : etiketterSet) {
			Label label = getLabelByEtiketter(etiketter);
			if (null != label) {
				int drawable = smallImages ? label.getSmallDrawable() : label.getLargeDrawable();
				if (!labelDrawables.contains(drawable)) {
					labelDrawables.add(drawable);
				}
			}
		}

		if (hasMiljo) {
			labelDrawables.add(smallImages ? sMiljoLabel.getSmallDrawable() : sMiljoLabel.getLargeDrawable());
		}

		return labelDrawables;
	}

	public static List<Integer> getLabelsByMaterial(Material material, boolean smallImages) {
		List<Integer> labels = new ArrayList<>();
		for (String etiketter : getDisplayEtiketter(material)) {
			Label label = getLabelByEtiketter(etiketter);
			if (null != label) {
				int drawable = smallImages ? label.getSmallDrawable() : label.getLargeDrawable();
				if (!labels.contains(drawable)) {
					labels.add(drawable);
				}
			}
		}

		if (material.getMiljo()) {
			labels.add(smallImages ? sMiljoLabel.getSmallDrawable() : sMiljoLabel.getLargeDrawable());
		}

		return labels;
	}

	private static List<String> getDisplayEtiketter(Material material) {
		List<String> display = material.getDisplayEtiketter();
		if (display.isEmpty() && "1".equals(material.getKlass()) && null != material.getKlassKod() && !material.getKlassKod().isEmpty()) {
			return Collections.singletonList(material.getKlassKod());
		}
		return display;
	}

	private static Label getLabelByEtiketter(String etiketter) {

		return sLabels.get(etiketter);
	}

	private static void putLabel(String etiketter, @DrawableRes int largeDrawable, @DrawableRes int smallDrawable) {
		Label label = new Label(etiketter, largeDrawable, smallDrawable);
		sLabels.put(etiketter, label);
	}

	static {
		sMiljoLabel = new Label("MILJÖFARLIGT", R.drawable.label_miljo, R.drawable.label_miljo_sm);
		sLabels = new LinkedHashMap<>();

		// Klass 1
		putLabel("1.1A", R.drawable.label_1_1a_explosive, R.drawable.label_1_1a_explosive_sm);
		putLabel("1.1B", R.drawable.label_1_1b_explosive, R.drawable.label_1_1b_explosive_sm);
		putLabel("1.1C", R.drawable.label_1_1c_explosive, R.drawable.label_1_1c_explosive_sm);
		putLabel("1.1D", R.drawable.label_1_1d_explosive, R.drawable.label_1_1d_explosive_sm);
		putLabel("1.1E", R.drawable.label_1_1e_explosive, R.drawable.label_1_1e_explosive_sm);
		putLabel("1.1F", R.drawable.label_1_1f_explosive, R.drawable.label_1_1f_explosive_sm);
		putLabel("1.1G", R.drawable.label_1_1g_explosive, R.drawable.label_1_1g_explosive_sm);
		putLabel("1.1J", R.drawable.label_1_1j_explosive, R.drawable.label_1_1j_explosive_sm);
		putLabel("1.1L", R.drawable.label_1_1l_explosive, R.drawable.label_1_1l_explosive_sm);

		putLabel("1.2B", R.drawable.label_1_2b_explosive, R.drawable.label_1_2b_explosive_sm);
		putLabel("1.2C", R.drawable.label_1_2c_explosive, R.drawable.label_1_2c_explosive_sm);
		putLabel("1.2D", R.drawable.label_1_2d_explosive, R.drawable.label_1_2d_explosive_sm);
		putLabel("1.2E", R.drawable.label_1_2e_explosive, R.drawable.label_1_2e_explosive_sm);
		putLabel("1.2F", R.drawable.label_1_2f_explosive, R.drawable.label_1_2f_explosive_sm);
		putLabel("1.2G", R.drawable.label_1_2g_explosive, R.drawable.label_1_2g_explosive_sm);
		putLabel("1.2H", R.drawable.label_1_2h_explosive, R.drawable.label_1_2h_explosive_sm);
		putLabel("1.2J", R.drawable.label_1_2j_explosive, R.drawable.label_1_2j_explosive_sm);
		putLabel("1.2K", R.drawable.label_1_2k_explosive, R.drawable.label_1_2k_explosive_sm);
		putLabel("1.2L", R.drawable.label_1_2l_explosive, R.drawable.label_1_2l_explosive_sm);

		putLabel("1.3C", R.drawable.label_1_3c_explosive, R.drawable.label_1_3c_explosive_sm);
		putLabel("1.3F", R.drawable.label_1_3f_explosive, R.drawable.label_1_3f_explosive_sm); // Finns ej i JSON
		putLabel("1.3G", R.drawable.label_1_3g_explosive, R.drawable.label_1_3g_explosive_sm);
		putLabel("1.3H", R.drawable.label_1_3h_explosive, R.drawable.label_1_3h_explosive_sm);
		putLabel("1.3J", R.drawable.label_1_3j_explosive, R.drawable.label_1_3j_explosive_sm);
		putLabel("1.3K", R.drawable.label_1_3k_explosive, R.drawable.label_1_3k_explosive_sm);
		putLabel("1.3L", R.drawable.label_1_3l_explosive, R.drawable.label_1_3l_explosive_sm);

		putLabel("1.4B", R.drawable.label_1_4b_explosive, R.drawable.label_1_4b_explosive_sm);
		putLabel("1.4C", R.drawable.label_1_4c_explosive, R.drawable.label_1_4c_explosive_sm);
		putLabel("1.4D", R.drawable.label_1_4d_explosive, R.drawable.label_1_4d_explosive_sm);
		putLabel("1.4E", R.drawable.label_1_4e_explosive, R.drawable.label_1_4e_explosive_sm);
		putLabel("1.4F", R.drawable.label_1_4f_explosive, R.drawable.label_1_4f_explosive_sm);
		putLabel("1.4G", R.drawable.label_1_4g_explosive, R.drawable.label_1_4g_explosive_sm);
		putLabel("1.4S", R.drawable.label_1_4s_explosive, R.drawable.label_1_4s_explosive_sm);

		putLabel("1.5D", R.drawable.label_1_5d_blasting_agent, R.drawable.label_1_5d_blasting_agent_sm);
		putLabel("1.6N", R.drawable.label_1_6n_explosive, R.drawable.label_1_6n_explosive_sm);

		// Klass 2
		putLabel("2.1", R.drawable.label_2_1_flammable_gas, R.drawable.label_2_1_flammable_gas_sm);
		putLabel("2.2", R.drawable.label_2_2_non_flammable_gas, R.drawable.label_2_2_non_flammable_gas_sm);
		putLabel("2.3", R.drawable.label_2_3_poison_gas, R.drawable.label_2_3_poison_gas_sm);

		// Klass 3
		putLabel("3", R.drawable.label_3_flammable_liquid, R.drawable.label_3_flammable_liquid_sm);

		// Klass 4
		putLabel("4.1", R.drawable.label_4_1_flammable_solid, R.drawable.label_4_1_flammable_solid_sm);
		putLabel("4.2", R.drawable.label_4_2_spontaneously_combustible, R.drawable.label_4_2_spontaneously_combustible_sm);
		putLabel("4.3", R.drawable.label_4_3_dangerous_when_wet, R.drawable.label_4_3_dangerous_when_wet_sm);

		// Klass 5
		putLabel("5.1", R.drawable.label_5_1_oxidizer, R.drawable.label_5_1_oxidizer_sm);
		putLabel("5.2", R.drawable.label_5_2_organic_peroxide, R.drawable.label_5_2_organic_peroxide_sm);

		// Klass 6
		putLabel("6.1", R.drawable.label_6_1_poison, R.drawable.label_6_1_poison_sm);
		putLabel("6.2", R.drawable.label_6_2_infectious_substance, R.drawable.label_6_2_infectious_substance_sm);

		// Klass 7
		//putLabel("7I", R.drawable.label_7_a_radioactive, R.drawable.label_7_a_radioactive_sm); // Finns ej i JSON
		//putLabel("7II", R.drawable.label_7_b_radioactive, R.drawable.label_7_b_radioactive_sm); // Finns ej i JSON
		//putLabel("7III", R.drawable.label_7_c_radioactive, R.drawable.label_7_c_radioactive_sm); // Finns ej i JSON
		putLabel("7X", R.drawable.label_7_d_radioactive, R.drawable.label_7_d_radioactive_sm); // Egentligen fordonsskylt, men JSON specificerar ej underklass
		putLabel("7E", R.drawable.label_7_e_radioactive, R.drawable.label_7_e_radioactive_sm);

		// Klass 8
		putLabel("8", R.drawable.label_8_corrosive, R.drawable.label_8_corrosive_sm);

		// Klass 9
		putLabel("9", R.drawable.label_9_other_dangerous_goods, R.drawable.label_9_other_dangerous_goods_sm);
		putLabel("9A", R.drawable.label_9_a_other_dangerous_goods, R.drawable.label_9_a_other_dangerous_goods_sm);
	}
}
