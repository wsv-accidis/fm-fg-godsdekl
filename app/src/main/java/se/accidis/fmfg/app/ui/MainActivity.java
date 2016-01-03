package se.accidis.fmfg.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import se.accidis.fmfg.app.R;

public final class MainActivity extends AppCompatActivity {
	private static final String STATE_LAST_OPENED_FRAGMENT = "openMainActivityFragment";
	private static final String STATE_LAST_OPENED_FRAGMENT_POS = "openMainActivityFragmentPos";
	private static final String TAG = MainActivity.class.getSimpleName();
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private NavigationItem mOpenFragmentItem;
	private HasMenu mOptionsMenu;
	private CharSequence mTitle;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen if the drawer is not showing. Otherwise, let the drawer decide what to show in the action bar.
			int menuId = (null == mOptionsMenu ? R.menu.main : mOptionsMenu.getMenu());
			getMenuInflater().inflate(menuId, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (null != mOptionsMenu && mOptionsMenu.onMenuItemSelected(item)) || super.onOptionsItemSelected(item);
	}

	public void openFragment(Fragment fragment) {
		openFragment(fragment, true);
	}

	public void openFragment(Fragment fragment, boolean addToBackStack) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment oldFragment = fragmentManager.findFragmentById(R.id.container);

		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

		if (addToBackStack && !isSameFragment(oldFragment, fragment)) {
			transaction.addToBackStack(null);
		}

		transaction.commit();
		updateViewFromFragment(fragment);
	}

	public void popFragmentFromBackStack() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.popBackStack();
	}

	public void updateFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentById(R.id.container);
		updateViewFromFragment(fragment);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = getTitle();

		final FragmentManager fragmentManager = getSupportFragmentManager();
		mNavigationDrawerFragment = (NavigationDrawerFragment) fragmentManager.findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setNavigationDrawerCallbacks(new NavigationDrawerCallbacks());
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

		if (null != savedInstanceState) {
			mOpenFragmentItem = NavigationItem.fromPosition(savedInstanceState.getInt(STATE_LAST_OPENED_FRAGMENT_POS));
			if (null == mOpenFragmentItem) {
				mOpenFragmentItem = NavigationItem.getDefault();
				openNavigationItem(mOpenFragmentItem, false);
			} else {
				Fragment lastFragment = fragmentManager.getFragment(savedInstanceState, STATE_LAST_OPENED_FRAGMENT);
				openFragment(lastFragment, false);
			}
		} else if (null == mOpenFragmentItem) {
			mOpenFragmentItem = NavigationItem.getDefault();
			openNavigationItem(mOpenFragmentItem, false);
		}

		fragmentManager.addOnBackStackChangedListener(new BackStackChangedListener());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentById(R.id.container);
		if (null != fragment) {
			fragmentManager.putFragment(outState, STATE_LAST_OPENED_FRAGMENT, fragment);
		}

		outState.putInt(STATE_LAST_OPENED_FRAGMENT_POS, mOpenFragmentItem.getPosition());
	}

	private boolean isSameFragment(Fragment oldFragment, Fragment newFragment) {
		return null != oldFragment && oldFragment.getClass().getName().equals(newFragment.getClass().getName());
	}

	private void openNavigationItem(NavigationItem item, boolean addToBackStack) {
		Fragment nextFragment = NavigationItem.createFragment(item);
		if (null != nextFragment) {
			mOpenFragmentItem = item;
			openFragment(nextFragment, addToBackStack);
		} else {
			Log.e(TAG, "Trying to navigate to unrecognized fragment.");
		}
	}

	private void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setTitle(mTitle);
		}
	}

	private void setMainTitle(String title) {
		if (TextUtils.isEmpty(title)) {
			mTitle = getString(R.string.app_name);
		} else {
			mTitle = title;
		}
	}

	private void updateViewFromFragment(Fragment fragment) {
		if (fragment instanceof HasNavigationItem) {
			HasNavigationItem fragmentWithNavItem = (HasNavigationItem) fragment;
			mNavigationDrawerFragment.setSelectedItem(fragmentWithNavItem.getItem());
		}

		if (fragment instanceof HasTitle) {
			HasTitle fragmentWithTitle = (HasTitle) fragment;
			setMainTitle(fragmentWithTitle.getTitle(this));
		} else {
			setMainTitle(null);
		}

		if (fragment instanceof HasMenu) {
			mOptionsMenu = (HasMenu) fragment;
			fragment.setHasOptionsMenu(true);
		} else {
			mOptionsMenu = null;
			fragment.setHasOptionsMenu(false);
		}

		restoreActionBar();
	}

	public interface HasMenu {
		int getMenu();

		boolean onMenuItemSelected(MenuItem item);
	}

	public interface HasNavigationItem {
		NavigationItem getItem();
	}

	public interface HasTitle {
		String getTitle(Context context);
	}

	private final class BackStackChangedListener implements FragmentManager.OnBackStackChangedListener {
		@Override
		public void onBackStackChanged() {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
			if (null != fragment) {
				updateViewFromFragment(fragment);
			}
		}
	}

	private final class NavigationDrawerCallbacks implements NavigationDrawerFragment.NavigationDrawerCallbacks {
		@Override
		public void onNavigationDrawerItemSelected(NavigationItem item) {
			openNavigationItem(item, true);
		}
	}
}
