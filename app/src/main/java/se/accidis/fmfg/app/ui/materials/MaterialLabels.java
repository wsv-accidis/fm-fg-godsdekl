package se.accidis.fmfg.app.ui.materials;

import java.util.ArrayList;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Material;

/**
 * Simple helper for finding the labels corresponding to a certain material.
 */
public final class MaterialLabels {
    private MaterialLabels() {
    }

    public static List<Integer> getLabelsByMaterial(Material material) {
        List<Integer> labels = new ArrayList<>();
        for (String klassKod : material.getKlassKod()) {
            Integer label = getLabelByKlassKod(klassKod);
            if (0 != label && !labels.contains(label)) {
                labels.add(label);
            }
        }

        if (material.getMiljo()) {
            labels.add(R.drawable.label_miljo);
        }

        return labels;
    }

    private static int getLabelByKlassKod(String klassKod) {
        // If code ends with a letter (e g '1.1B'), remove it.
        if (Character.isLetter(klassKod.charAt(klassKod.length() - 1))) {
            klassKod = klassKod.substring(0, klassKod.length() - 1);
        }

        switch (klassKod) {
            case "1.1":
            case "1.2":
            case "1.3":
                return R.drawable.label_1;
            case "1.4":
                return R.drawable.label_14;
            case "1.5":
                return R.drawable.label_15;
            case "1.6":
                return R.drawable.label_16;
            case "2.1":
                return R.drawable.label_2;
            case "2.2":
                return R.drawable.label_22;
            case "2.3":
                return R.drawable.label_23;
            case "3":
                return R.drawable.label_3;
            case "4.1":
                return R.drawable.label_41;
            case "4.2":
                return R.drawable.label_42;
            case "4.3":
                return R.drawable.label_43;
            case "5.1":
                return R.drawable.label_51;
            case "5.2":
                return R.drawable.label_52;
            case "6.1":
                return R.drawable.label_61;
            case "6.2":
                return R.drawable.label_62;
            case "7":
                return R.drawable.label_7;
            case "8":
                return R.drawable.label_8;
            case "9":
                return R.drawable.label_9;
            default:
                return 0;
        }
    }
}
