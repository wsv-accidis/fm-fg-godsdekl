package se.accidis.fmfg.app.utils

import se.accidis.fmfg.app.model.Document
import se.accidis.fmfg.app.model.DocumentRow

/**
 * Helper object for validating if a document is in compliance with ADR 7.5.2.1 / 7.5.2.2 co-loading rules.
 */
object ColoadingHelper {
    private const val LABEL_1 = "1"
    private const val LABEL_14G = "1.4G"
    private const val LABEL_14S = "1.4S"
    private const val LABEL_41 = "4.1"
    private const val LABEL_52 = "5.2"
    private const val LABEL_9 = "9"
    private const val LABEL_9A = "9A"

    // Footnote b): Class 9 life-saving appliances permitted with Class 1
    private val FOOTNOTE_B_UN_NUMBERS = setOf("2990", "3072")

    // Footnote c): 1.4G safety devices (UN 0503) permitted with Class 9 safety devices (UN 3268)
    private const val UN_0503 = "0503"
    private const val UN_3268 = "3268"

    private val COMPATIBLE_GROUPS: Map<Char, Set<Char>> = mapOf(
        'A' to setOf('A'),
        'B' to setOf('B', 'D', 'S'),
        'C' to setOf('C', 'D', 'E', 'G', 'N', 'S'),
        'D' to setOf('B', 'C', 'D', 'E', 'G', 'N', 'S'),
        'E' to setOf('C', 'D', 'E', 'G', 'N', 'S'),
        'F' to setOf('F', 'S'),
        'G' to setOf('C', 'D', 'E', 'G', 'S'),
        'H' to setOf('H', 'S'),
        'J' to setOf('J', 'S'),
        'N' to setOf('C', 'D', 'E', 'N', 'S'),
        'S' to setOf('B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'N', 'S')
    )

    private sealed interface LoadCategory {
        data object Label41Plus1 : LoadCategory
        data object Label52Plus1 : LoadCategory
        data class Class1(
            val is14S: Boolean,
            val is14G: Boolean,
            val compatibilityGroups: Set<Char>,
            val unNr: String,
            val materialKey: String
        ) : LoadCategory

        data class Other(
            val isOnly52: Boolean,
            val isClass9: Boolean,
            val unNr: String
        ) : LoadCategory
    }

    fun isViolationOfColoadingRules(document: Document): Boolean {
        val items = document.rows.mapNotNull(::classifyRow)
        for (i in items.indices) {
            for (j in i + 1 until items.size) {
                if (!areCompatible(items[i], items[j])) {
                    return true
                }
            }
        }
        return false
    }

    private fun classifyRow(row: DocumentRow): LoadCategory? {
        val labels = row.material.klassKod.map { it.trim() }.filter { it.isNotEmpty() }
        if (labels.isEmpty()) {
            return null
        }

        val hasLabel1 = labels.contains(LABEL_1)
        if (hasLabel1 && labels.contains(LABEL_41)) {
            return LoadCategory.Label41Plus1
        }
        if (hasLabel1 && labels.contains(LABEL_52)) {
            return LoadCategory.Label52Plus1
        }

        val class1Labels = labels.filter { it.startsWith(LABEL_1) }
        if (class1Labels.isNotEmpty()) {
            val compatibilityGroups = class1Labels
                .mapNotNull { label -> label.lastOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() }
                .toSet()

            return LoadCategory.Class1(
                is14S = class1Labels.all { it == LABEL_14S },
                is14G = labels.all { it == LABEL_14G },
                compatibilityGroups = compatibilityGroups,
                unNr = row.material.UNnr.trim(),
                materialKey = row.material.uniqueKey
            )
        }

        return LoadCategory.Other(
            isOnly52 = labels.all { it == LABEL_52 },
            isClass9 = labels.all { it == LABEL_9 || it == LABEL_9A },
            unNr = row.material.UNnr.trim()
        )
    }

    private fun areCompatible(a: LoadCategory, b: LoadCategory): Boolean = when (a) {
        is LoadCategory.Label41Plus1 -> b is LoadCategory.Label41Plus1

        is LoadCategory.Label52Plus1 ->
            b is LoadCategory.Label52Plus1 || (b is LoadCategory.Other && b.isOnly52)

        is LoadCategory.Class1 -> when (b) {
            is LoadCategory.Label41Plus1, is LoadCategory.Label52Plus1 -> false
            is LoadCategory.Class1 -> areClass1Compatible(a, b)
            is LoadCategory.Other -> isClass1CompatibleWithOther(a, b)
        }

        is LoadCategory.Other -> when (b) {
            is LoadCategory.Label41Plus1 -> false
            is LoadCategory.Label52Plus1 -> a.isOnly52
            is LoadCategory.Class1 -> isClass1CompatibleWithOther(b, a)
            is LoadCategory.Other -> true
        }
    }

    /**
     * ADR 7.5.2.1 mixed loading between Class 1 and other classes (2.1–9).
     * Footnote d) (explosives + nitrates) is intentionally left as a warning so the user is aware of the extra requirements.
     */
    private fun isClass1CompatibleWithOther(class1: LoadCategory.Class1, other: LoadCategory.Other): Boolean {
        // Footnote a): 1.4S is allowed with classes 2.1–9
        if (class1.is14S) {
            return true
        }

        // Footnote b): Class 1 is allowed with Class 9 life-saving appliances (UN 2990, 3072)
        if (other.isClass9 && other.unNr in FOOTNOTE_B_UN_NUMBERS) {
            return true
        }

        // Footnote c): 1.4G (UN 0503) is allowed with Class 9 (UN 3268)
        if (class1.is14G && class1.unNr == UN_0503 && other.isClass9 && other.unNr == UN_3268) {
            return true
        }

        return false
    }

    /**
     * ADR 7.5.2.2 mixed loading between Class 1 compatibility groups.
     */
    private fun areClass1Compatible(a: LoadCategory.Class1, b: LoadCategory.Class1): Boolean {
        // Rows without a compatibility group letter cannot be evaluated against 7.5.2.2
        if (a.compatibilityGroups.isEmpty() || b.compatibilityGroups.isEmpty()) {
            return true
        }

        for (groupA in a.compatibilityGroups) {
            for (groupB in b.compatibilityGroups) {
                val allowed = if (groupA == 'L' || groupB == 'L') {
                    // Group L may only be co-loaded with the same type of substance/article in group L
                    groupA == 'L' && groupB == 'L' && a.materialKey == b.materialKey
                } else {
                    COMPATIBLE_GROUPS[groupA]?.contains(groupB) == true
                }
                if (!allowed) {
                    return false
                }
            }
        }
        return true
    }
}
