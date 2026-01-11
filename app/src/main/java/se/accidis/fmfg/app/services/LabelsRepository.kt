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
        // If code ends with a letter (e g '1.1B'), remove it.
        var klassKod = klassKod
        if (Character.isLetter(klassKod[klassKod.length - 1])) {
            klassKod = klassKod.dropLast(1)
        }

        return labels[klassKod]
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
        putLabel("1.1", R.drawable.label_1, R.drawable.label_1_sm)
        putLabel("1.2", R.drawable.label_1, R.drawable.label_1_sm)
        putLabel("1.3", R.drawable.label_1, R.drawable.label_1_sm)

        putLabel("1.4", R.drawable.label_14, R.drawable.label_14_sm)
        putLabel("1.5", R.drawable.label_15, R.drawable.label_15_sm)
        putLabel("1.6", R.drawable.label_16, R.drawable.label_16_sm)

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
        putLabel("8", R.drawable.label_8, R.drawable.label_8_sm)
        putLabel("9", R.drawable.label_9, R.drawable.label_9_sm)
    }
}
