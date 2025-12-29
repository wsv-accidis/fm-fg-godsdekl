package se.accidis.fmfg.app.model;

import androidx.annotation.DrawableRes;

/**
 * Model object for a label.
 */
public final class Label {
	private final String mEtiketter;
	private final int mLargeDrawable;
	private final int mSmallDrawable;

	public Label(String etiketter, @DrawableRes int largeDrawable, @DrawableRes int smallDrawable) {
		mEtiketter = etiketter;
		mLargeDrawable = largeDrawable;
		mSmallDrawable = smallDrawable;
	}

	public String getEtiketter() {
		return mEtiketter;
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
