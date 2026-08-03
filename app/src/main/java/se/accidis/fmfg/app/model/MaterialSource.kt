package se.accidis.fmfg.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Enum identifying the source of a material.
 */
@Serializable
@Parcelize
enum class MaterialSource : Parcelable {
    NONE,
    AMKAT,
    ADR_S
}
