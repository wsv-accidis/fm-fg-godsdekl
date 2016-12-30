package se.accidis.fmfg.app.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

/**
 * Tokenizer for use with MultiAutoCompleteTextView which uses a space for separation.
 */
public final class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
	public int findTokenEnd(CharSequence text, int cursor) {
		int i = cursor;
		final int len = text.length();

		while (i < len) {
			if (text.charAt(i) == ' ') {
				return i;
			} else {
				i++;
			}
		}

		return len;
	}

	public int findTokenStart(CharSequence text, int cursor) {
		int i = cursor;

		while (i > 0 && text.charAt(i - 1) != ' ') {
			i--;
		}
		while (i < cursor && text.charAt(i) == ' ') {
			i++;
		}

		return i;
	}

	public CharSequence terminateToken(CharSequence text) {
		int i = text.length();

		while (i > 0 && text.charAt(i - 1) == ' ') {
			i--;
		}

		if (i > 0 && text.charAt(i - 1) == ' ') {
			return text;
		} else {
			if (text instanceof Spanned) {
				final SpannableString sp = new SpannableString(text + " ");
				TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
				return sp;
			} else {
				return text + " ";
			}
		}
	}
}
