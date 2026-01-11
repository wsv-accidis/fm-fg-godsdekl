package se.accidis.fmfg.app.services

import androidx.annotation.DrawableRes
import se.accidis.fmfg.app.R
import se.accidis.fmfg.app.model.Document
import se.accidis.fmfg.app.model.Label
import se.accidis.fmfg.app.model.Material
import java.util.Collections
import java.util.TreeSet

/**
 * Repository for labels. Static class, holds all data in memory.
 */
object LabelsRepository {
    private val labels: MutableMap<String, Label> = LinkedHashMap()
    private val miljoLabel: Label =
        Label("MILJÖFARLIGT", R.drawable.label_miljo, R.drawable.label_miljo_sm)

    @JvmStatic
    val allLabels: Collection<Label>
        get() = Collections.unmodifiableCollection(
            labels.values
        )

    @JvmStatic
    fun getLabelsByDocument(document: Document, smallImages: Boolean): List<Int> {
        var hasMiljo = false
        val klassKodSet = TreeSet<String>()
        for (material in document.getMaterialsSet()) {
            if (material.miljo) {
                hasMiljo = true
            }
            klassKodSet.addAll(material.klassKod)
        }

        val labelDrawables = ArrayList<Int>()
        for (klassKod in klassKodSet) {
            val label = getLabelByKlassKod(klassKod)
            if (null != label) {
                val drawable =
                    if (smallImages) label.smallDrawable else label.largeDrawable
                if (!labelDrawables.contains(drawable)) {
                    labelDrawables.add(drawable)
                }
            }
        }

        if (hasMiljo) {
            labelDrawables.add(if (smallImages) miljoLabel.smallDrawable else miljoLabel.largeDrawable)
        }

        return labelDrawables
    }

    @JvmStatic
    fun getLabelsByMaterial(material: Material, smallImages: Boolean): List<Int> {
        val labels = ArrayList<Int>()
        for (klassKod in material.klassKod) {
            val label = getLabelByKlassKod(klassKod)
            if (null != label) {
                val drawable =
                    if (smallImages) label.smallDrawable else label.largeDrawable
                if (!labels.contains(drawable)) {
                    labels.add(drawable)
                }
            }
        }

        if (material.miljo) {
            labels.add(if (smallImages) miljoLabel.smallDrawable else miljoLabel.largeDrawable)
        }

        return labels
    }

    private fun getLabelByKlassKod(klassKod: String): Label? {
        if(labels.containsKey(klassKod)) {
            return labels[klassKod]
        }

        // Fallback label in case we don't have an exact match (this shouldn't happen)
        var fallback = klassKod.dropLast(1)
        while("" != fallback) {
            if(labels.containsKey(klassKod)) {
                return labels[klassKod]
            }
            fallback = klassKod.dropLast(1)
        }

        return null
    }

    private fun putLabel(
        klassKod: String,
        @DrawableRes largeDrawable: Int,
        @DrawableRes smallDrawable: Int
    ) {
        val label = Label(klassKod, largeDrawable, smallDrawable)
        labels[klassKod] = label
    }

    init {
        putLabel("1", R.drawable.label_1, R.drawable.label_1_sm)

        putLabel("1.1A", R.drawable.label_11a, R.drawable.label_11a_sm)
        putLabel("1.1B", R.drawable.label_11b, R.drawable.label_11b_sm)
        putLabel("1.1C", R.drawable.label_11c, R.drawable.label_11c_sm)
        putLabel("1.1D", R.drawable.label_11d, R.drawable.label_11d_sm)
        putLabel("1.1E", R.drawable.label_11e, R.drawable.label_11e_sm)
        putLabel("1.1F", R.drawable.label_11f, R.drawable.label_11f_sm)
        putLabel("1.1G", R.drawable.label_11g, R.drawable.label_11g_sm)
        putLabel("1.1J", R.drawable.label_11j, R.drawable.label_11j_sm)
        putLabel("1.1L", R.drawable.label_11l, R.drawable.label_11l_sm)

        putLabel("1.2B", R.drawable.label_12b, R.drawable.label_12b_sm)
        putLabel("1.2C", R.drawable.label_12c, R.drawable.label_12c_sm)
        putLabel("1.2D", R.drawable.label_12d, R.drawable.label_12d_sm)
        putLabel("1.2E", R.drawable.label_12e, R.drawable.label_12e_sm)
        putLabel("1.2F", R.drawable.label_12f, R.drawable.label_12f_sm)
        putLabel("1.2G", R.drawable.label_12g, R.drawable.label_12g_sm)
        putLabel("1.2H", R.drawable.label_12h, R.drawable.label_12h_sm)
        putLabel("1.2J", R.drawable.label_12j, R.drawable.label_12j_sm)
        putLabel("1.2K", R.drawable.label_12k, R.drawable.label_12k_sm)
        putLabel("1.2L", R.drawable.label_12l, R.drawable.label_12l_sm)

        putLabel("1.3C", R.drawable.label_13c, R.drawable.label_13c_sm)
        putLabel("1.3G", R.drawable.label_13g, R.drawable.label_13g_sm)
        putLabel("1.3H", R.drawable.label_13h, R.drawable.label_13h_sm)
        putLabel("1.3J", R.drawable.label_13j, R.drawable.label_13j_sm)
        putLabel("1.3K", R.drawable.label_13k, R.drawable.label_13k_sm)
        putLabel("1.3L", R.drawable.label_13l, R.drawable.label_13l_sm)

        putLabel("1.4", R.drawable.label_14, R.drawable.label_14_sm)
        putLabel("1.4B", R.drawable.label_14b, R.drawable.label_14b_sm)
        putLabel("1.4C", R.drawable.label_14c, R.drawable.label_14c_sm)
        putLabel("1.4D", R.drawable.label_14d, R.drawable.label_14d_sm)
        putLabel("1.4E", R.drawable.label_14e, R.drawable.label_14e_sm)
        putLabel("1.4F", R.drawable.label_14f, R.drawable.label_14f_sm)
        putLabel("1.4G", R.drawable.label_14g, R.drawable.label_14g_sm)
        putLabel("1.4S", R.drawable.label_14s, R.drawable.label_14s_sm)

        putLabel("1.5", R.drawable.label_15, R.drawable.label_15_sm)
        putLabel("1.5D", R.drawable.label_15d, R.drawable.label_15d_sm)

        putLabel("1.6", R.drawable.label_16, R.drawable.label_16_sm)
        putLabel("1.6N", R.drawable.label_16n, R.drawable.label_16n_sm)

        putLabel("2.1", R.drawable.label_21, R.drawable.label_21_sm)
        putLabel("2.2", R.drawable.label_22, R.drawable.label_22_sm)
        putLabel("2.3", R.drawable.label_23, R.drawable.label_23_sm)

        putLabel("3", R.drawable.label_3, R.drawable.label_3_sm)

        putLabel("4.1", R.drawable.label_41, R.drawable.label_41_sm)
        putLabel("4.2", R.drawable.label_42, R.drawable.label_42_sm)
        putLabel("4.3", R.drawable.label_43, R.drawable.label_43_sm)

        putLabel("5.1", R.drawable.label_51, R.drawable.label_51_sm)
        putLabel("5.2", R.drawable.label_52, R.drawable.label_52_sm)

        putLabel("6.1", R.drawable.label_61, R.drawable.label_61_sm)
        putLabel("6.2", R.drawable.label_62, R.drawable.label_62_sm)

        putLabel("7", R.drawable.label_7, R.drawable.label_7_sm)
        putLabel("7E", R.drawable.label_7e, R.drawable.label_7e_sm)

        putLabel("8", R.drawable.label_8, R.drawable.label_8_sm)

        putLabel("9", R.drawable.label_9, R.drawable.label_9_sm)
        putLabel("9A", R.drawable.label_9a, R.drawable.label_9a_sm)
    }
}
