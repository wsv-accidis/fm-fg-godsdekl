package se.accidis.fmfg.app.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment which displays some information about the app.
 */
public final class AboutFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	private static final String EMAIL = "wilhelm.svenselius@gmail.com";
	private static final String TAG = AboutFragment.class.getSimpleName();

	@Override
	public int getItemId() {
		return R.id.nav_about;
	}

	@Override
	public String getTitle(Context context) {
		return context.getString(R.string.about_nav_title);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		TextView authorText = (TextView) view.findViewById(R.id.about_author);
		authorText.setOnClickListener(new AuthorClickedListener());

		TextView versionText = (TextView) view.findViewById(R.id.about_version);
		versionText.setText(String.format(getString(R.string.about_version), AndroidUtils.getAppVersionName(getContext())));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		AndroidUtils.hideSoftKeyboard(getContext(), getView());
	}

	private static void sendEmailTo(String email, Fragment fragment) {
		try {
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
			fragment.startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to send e-mail.", e);
		}
	}

	private final class AuthorClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			sendEmailTo(EMAIL, AboutFragment.this);
		}
	}
}
