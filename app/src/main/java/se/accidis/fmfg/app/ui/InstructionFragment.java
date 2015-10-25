package se.accidis.fmfg.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Fragment which displays instructions for ADR.
 */
public final class InstructionFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
    private WebView mWebView;

    @Override
    public NavigationItem getItem() {
        return NavigationItem.INSTRUCTIONS_ITEM;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.instructions_nav_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != mWebView) {
            mWebView.destroy();
        }

        mWebView = new WebView(getActivity());
        mWebView.loadUrl("file:///android_asset/www/instr_adr.html");

        return mWebView;
    }

    @Override
    public void onDestroy() {
        if (null != mWebView) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        mWebView.onResume();
        AndroidUtils.hideSoftKeyboard(getContext(), mWebView);
        super.onResume();
    }
}
