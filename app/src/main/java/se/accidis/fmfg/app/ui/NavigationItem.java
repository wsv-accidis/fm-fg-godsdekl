package se.accidis.fmfg.app.ui;

import android.content.res.Resources;
import android.support.v4.app.Fragment;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.ui.documents.DocumentFragment;
import se.accidis.fmfg.app.ui.materials.MaterialsListFragment;

/**
 * Holds the set of navigable items used by the navigation drawer and main activity.
 */
public enum NavigationItem {
    MATERIALS_ITEM(0),
    CURRENT_DOCUMENT_ITEM(1);

    private final int mPosition;

    NavigationItem(int position) {
        mPosition = position;
    }

    public static String[] asTitles(Resources resources) {
        // The index of each item in this array _MUST_ match its position in the enum
        return new String[]{
                resources.getString(R.string.materials_nav_title),
                resources.getString(R.string.document_nav_title)
        };
    }

    public static Fragment createFragment(NavigationItem item) {
        switch (item) {
            case MATERIALS_ITEM:
                return new MaterialsListFragment();
            case CURRENT_DOCUMENT_ITEM:
                return new DocumentFragment();
        }

        return null;
    }

    public static NavigationItem fromPosition(int position) {
        for (NavigationItem item : NavigationItem.values()) {
            if (position == item.getPosition()) {
                return item;
            }
        }

        return null;
    }

    public static NavigationItem getDefault() {
        return MATERIALS_ITEM;
    }

    public int getPosition() {
        return mPosition;
    }
}
