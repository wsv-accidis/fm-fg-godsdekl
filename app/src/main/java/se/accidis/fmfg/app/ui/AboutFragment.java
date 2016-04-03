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
	private static final String EMAIL = "wilhelm.svenselius@accidis.se";
	private static final String GITHUB_URL = "https://github.com/wsv-accidis/fm-fg-godsdekl/";
	private static final String TAG = AboutFragment.class.getSimpleName();
	private static final String WEBSITE_URL = "http://godsdeklaration.se";

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

		TextView webSiteText = (TextView) view.findViewById(R.id.about_website);
		webSiteText.setOnClickListener(new WebsiteOnClickedListener());

		TextView gitHubText = (TextView) view.findViewById(R.id.about_github);
		gitHubText.setOnClickListener(new GitHubOnClickedListener());

		TextView versionText = (TextView) view.findViewById(R.id.about_version);
		versionText.setText(String.format(getString(R.string.about_version), AndroidUtils.getAppVersionName(getContext())));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		AndroidUtils.hideSoftKeyboard(getContext(), getView());
	}

	private void openWebPage(String uri) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to send open web page.", e);
		}
	}

	private void sendEmailTo(String email) {
		try {
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
			startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to send e-mail.", e);
		}
	}

	private final class AuthorClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			sendEmailTo(EMAIL);
		}
	}

	private final class GitHubOnClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			openWebPage(GITHUB_URL);
		}
	}

	private final class WebsiteOnClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			openWebPage(WEBSITE_URL);
		}
	}
}
