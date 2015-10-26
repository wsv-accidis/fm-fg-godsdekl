package se.accidis.fmfg.app.ui.instructions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Base class for fragments based on webviews.
 */
public abstract class WebViewFragmentBase extends Fragment {
    private WebView mWebView;

    protected abstract String getUrl();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != mWebView) {
            mWebView.destroy();
        }

        mWebView = new WebView(getActivity());
        mWebView.loadUrl(getUrl());

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
