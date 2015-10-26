package se.accidis.fmfg.app.ui;

import android.content.res.Resources;
import android.support.v4.app.Fragment;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.ui.documents.DocumentFragment;
import se.accidis.fmfg.app.ui.documents.DocumentsListFragment;
import se.accidis.fmfg.app.ui.instructions.ColoadingFragment;
import se.accidis.fmfg.app.ui.instructions.InstructionFragment;
import se.accidis.fmfg.app.ui.materials.MaterialsListFragment;

/**
 * Holds the set of navigable items used by the navigation drawer and main activity.
 */
public enum NavigationItem {
    MATERIALS_ITEM(0),
    CURRENT_DOCUMENT_ITEM(1),
    DOCUMENTS_ITEM(2),
    INSTRUCTIONS_ITEM(3),
    COLOADING_ITEM(4),
    ABOUT_ITEM(5);

    private final int mPosition;

    NavigationItem(int position) {
        mPosition = position;
    }

    public static String[] asTitles(Resources resources) {
        // The index of each item in this array _MUST_ match its position in the enum
        return new String[]{
                resources.getString(R.string.materials_nav_title),
                resources.getString(R.string.document_nav_title),
                resources.getString(R.string.documents_list_nav_title),
                resources.getString(R.string.instructions_nav_title),
                resources.getString(R.string.coloading_nav_title),
                resources.getString(R.string.about_nav_title)
        };
    }

    public static Fragment createFragment(NavigationItem item) {
        switch (item) {
            case ABOUT_ITEM:
                return new AboutFragment();
            case COLOADING_ITEM:
                return new ColoadingFragment();
            case CURRENT_DOCUMENT_ITEM:
                return new DocumentFragment();
            case DOCUMENTS_ITEM:
                return new DocumentsListFragment();
            case INSTRUCTIONS_ITEM:
                return new InstructionFragment();
            case MATERIALS_ITEM:
                return new MaterialsListFragment();
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
