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
			etiketterSet.addAll(material.getEtiketter());
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
		for (String etiketter : material.getEtiketter()) {
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

	private static Label getLabelByEtiketter(String etiketter) {
		// If code ends with a letter (e g '1.1B'), remove it.
		if (Character.isLetter(etiketter.charAt(etiketter.length() - 1))) {
			etiketter = etiketter.substring(0, etiketter.length() - 1);
		}

		return sLabels.get(etiketter);
	}

	private static void putLabel(String etiketter, @DrawableRes int largeDrawable, @DrawableRes int smallDrawable) {
		Label label = new Label(etiketter, largeDrawable, smallDrawable);
		sLabels.put(etiketter, label);
	}

	static {
		sMiljoLabel = new Label("MILJÖFARLIGT", R.drawable.label_miljo, R.drawable.label_miljo_sm);
		sLabels = new LinkedHashMap<>();

		putLabel("1.1", R.drawable.label_1, R.drawable.label_1_sm);
		putLabel("1.2", R.drawable.label_1, R.drawable.label_1_sm);
		putLabel("1.3", R.drawable.label_1, R.drawable.label_1_sm);

		putLabel("1.4", R.drawable.label_14, R.drawable.label_14_sm);
		putLabel("1.5", R.drawable.label_15, R.drawable.label_15_sm);
		putLabel("1.6", R.drawable.label_16, R.drawable.label_16_sm);

		putLabel("2.1", R.drawable.label_2, R.drawable.label_2_sm);
		putLabel("2.2", R.drawable.label_22, R.drawable.label_22_sm);
		putLabel("2.3", R.drawable.label_23, R.drawable.label_23_sm);

		putLabel("3", R.drawable.label_3, R.drawable.label_3_sm);

		putLabel("4.1", R.drawable.label_41, R.drawable.label_41_sm);
		putLabel("4.2", R.drawable.label_42, R.drawable.label_42_sm);
		putLabel("4.3", R.drawable.label_43, R.drawable.label_43_sm);

		putLabel("5.1", R.drawable.label_51, R.drawable.label_51_sm);
		putLabel("5.2", R.drawable.label_52, R.drawable.label_52_sm);

		putLabel("6.1", R.drawable.label_61, R.drawable.label_61_sm);
		putLabel("6.2", R.drawable.label_62, R.drawable.label_62_sm);

		putLabel("7", R.drawable.label_7, R.drawable.label_7_sm);
		putLabel("8", R.drawable.label_8, R.drawable.label_8_sm);
		putLabel("9", R.drawable.label_9, R.drawable.label_9_sm);
	}
}
