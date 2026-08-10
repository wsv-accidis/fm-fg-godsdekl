package se.accidis.fmfg.app.model

import androidx.annotation.DrawableRes

/**
 * Model object for a label.
 */
data class Label(
    val klassKod: String,
    @get:DrawableRes @param:DrawableRes val largeDrawable: Int,
    @get:DrawableRes @param:DrawableRes val smallDrawable: Int
)
