package se.accidis.fmfg.app.model;

import androidx.annotation.DrawableRes;

/**
 * Model object for a label.
 */
public final class Label {
	private final String mKlassKod;
	private final int mLargeDrawable;
	private final int mSmallDrawable;

	public Label(String klassKod, @DrawableRes int largeDrawable, @DrawableRes int smallDrawable) {
		mKlassKod = klassKod;
		mLargeDrawable = largeDrawable;
		mSmallDrawable = smallDrawable;
	}

	public String getKlassKod() {
		return mKlassKod;
	}

	@DrawableRes
	public int getLargeDrawable() {
		return mLargeDrawable;
	}

	@DrawableRes
	public int getSmallDrawable() {
		return mSmallDrawable;
	}
}
