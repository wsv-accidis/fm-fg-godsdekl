package se.accidis.fmfg.app.model

import kotlinx.serialization.Serializable

/**
 * Enum identifying the source of a material.
 */
@Serializable
enum class MaterialSource {
    NONE,
    AMKAT,
    ADR_S
}
