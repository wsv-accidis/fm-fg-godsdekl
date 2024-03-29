package se.accidis.fmfg.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.util.Random;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.ui.documents.DocumentFragment;
import se.accidis.fmfg.app.ui.documents.DocumentsListFragment;
import se.accidis.fmfg.app.ui.instructions.ColoadingFragment;
import se.accidis.fmfg.app.ui.instructions.InstructionFragment;
import se.accidis.fmfg.app.ui.materials.MaterialsListFragment;
import se.accidis.fmfg.app.ui.preferences.PreferencesFragment;
import se.accidis.fmfg.app.utils.AndroidUtils;

public final class MainActivity extends AppCompatActivity {
	private static final String STATE_LAST_OPENED_FRAGMENT = "openMainActivityFragment";
	private static final String TAG = MainActivity.class.getSimpleName();
	private DrawerLayout mNavigationDrawer;
	private NavigationView mNavigationView;
	private HasMenu mOptionsMenu;
	private CharSequence mTitle;

	@Override
	public void onBackPressed() {
		if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
			mNavigationDrawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (null != mOptionsMenu) {
			getMenuInflater().inflate(mOptionsMenu.getMenu(), menu);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (null != mOptionsMenu && mOptionsMenu.onMenuItemSelected(item)) || super.onOptionsItemSelected(item);
	}

	public void openFragment(Fragment fragment, boolean addToBackStack) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

		if (addToBackStack) {
			transaction.addToBackStack(null);
		}

		transaction.commit();
		updateViewFromFragment(fragment);
	}

	public void openFragment(Fragment fragment) {
		openFragment(fragment, true);
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

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mNavigationDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mNavigationDrawer.addDrawerListener(toggle);
		mNavigationDrawer.addDrawerListener(new NavigationDrawerListener());
		toggle.syncState();

		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(new NavigationListener());

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.addOnBackStackChangedListener(new BackStackChangedListener());

		if (null != savedInstanceState) {
			Fragment lastFragment = fragmentManager.getFragment(savedInstanceState, STATE_LAST_OPENED_FRAGMENT);
			openFragment(lastFragment, false);
		} else {
			openFragment(getFragmentByNavigationItem(R.id.nav_materials), false);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentById(R.id.container);
		if (null != fragment) {
			fragmentManager.putFragment(outState, STATE_LAST_OPENED_FRAGMENT, fragment);
		}
	}

	private Fragment getFragmentByNavigationItem(int navId) {
		switch (navId) {
			case R.id.nav_about:
				return new AboutFragment();
			case R.id.nav_coloading:
				return new ColoadingFragment();
			case R.id.nav_document:
				return new DocumentFragment();
			case R.id.nav_documents_list:
				return new DocumentsListFragment();
			case R.id.nav_instructions:
				return new InstructionFragment();
			case R.id.nav_materials:
				return new MaterialsListFragment();
			case R.id.nav_preferences:
				return new PreferencesFragment();
		}

		return null;
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
			mNavigationView.setCheckedItem(fragmentWithNavItem.getItemId());
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
		int getItemId();
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

	private final class NavigationDrawerListener implements DrawerLayout.DrawerListener {
		private final int[] mHeaderImages = new int[]{R.drawable.img_bv206, R.drawable.img_tgb40, R.drawable.img_pb8, R.drawable.img_sb90};
		private final Random mRandom = new Random();
		private int mLastImage = 0;

		@Override
		public void onDrawerClosed(View drawerView) {
			ImageView imageView = (ImageView) drawerView.findViewById(R.id.nav_header_image);
			if (null != imageView) {
				int index = mRandom.nextInt(mHeaderImages.length);
				if (mLastImage == index) {
					index = (1 + index) % mHeaderImages.length;
				}
				imageView.setImageResource(mHeaderImages[index]);
				mLastImage = index;
			}
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			AndroidUtils.hideSoftKeyboard(MainActivity.this, getCurrentFocus());
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
		}

		@Override
		public void onDrawerStateChanged(int newState) {
		}
	}

	private final class NavigationListener implements NavigationView.OnNavigationItemSelectedListener {
		@Override
		public boolean onNavigationItemSelected(MenuItem item) {
			Fragment fragment = getFragmentByNavigationItem(item.getItemId());
			if (null != fragment) {
				openFragment(fragment);
			} else {
				Log.e(TAG, "Trying to navigate to unrecognized fragment.");
			}

			DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
			if (null != drawer) {
				drawer.closeDrawer(GravityCompat.START);
			}
			return true;
		}
	}
}
