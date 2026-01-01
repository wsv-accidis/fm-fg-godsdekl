package se.accidis.fmfg.app.model;

import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import se.accidis.fmfg.app.utils.AndroidUtils;
import se.accidis.fmfg.app.utils.JSONUtils;

/**
 * Model object for materials.
 */
public final class Material {
	public static final int TPKAT_MAX = 3;
	public static final int TPKAT_MIN = 1;
	public static final int TPKAT_NONE = 0;
	private static final int NO_FM_SELECTED = -1;

	private static final String UUID_PREFIX = "Uuid_";
	private static long sUuidCounter = 0;

	private final List<FM> mFM; // Förrådsbeteckningar, Förrådsbenämningar, NEMmg
	private final String mPrimaryFben;
	private final String mPrimaryFbet;
	private final String mFrpGrp; // Förpackningsgrupp
	private final String mFullText;
	private final boolean mIsCustom;
	private final List<String> mEtiketter; // Etiketter
	private final String mLabelsText;
	private final String mKlass;
	private final String mKlassKod;
	private final boolean mMiljo; // Miljöfarligt
	private final boolean mMiljoDefined; // True if JSON explicitly specified value
	private final int mNEMmg; // NEM i mg
	private final boolean mHasPresetNEM;
	private final String mNamn; // Namn
	private final String mSearchText;
	private final int mSelectedFmIndex; // Selected FM entry, -1 if none
	private final int mTpKat; // Transportkategori
	private final String mTunnelkod; // Tunnelkod
	private final String mUNnr; // UN-nummer
	private final String mUuid;

	public static Material createCustom(String namn, List<String> etiketter, String uuid) {
		return new Material(Collections.<FM>emptyList(), "", namn, etiketter, NO_FM_SELECTED, false, TPKAT_NONE, "", "", "", "", false, false, true, uuid);
	}

	private Material(List<FM> fm, String unNr, String namn, List<String> etiketter, int selectedFmIndex, boolean hasPresetNEM, int tpKat, String frpGrp, String klass, String klassKod, String tunnelKod, boolean miljo, boolean miljoDefined, boolean isCustom, String uuid) {
		List<FM> fmList = (null != fm) ? fm : Collections.<FM>emptyList();
		mFM = Collections.unmodifiableList(new ArrayList<>(fmList));
		int fmIndex = sanitizeSelectedFmIndex(selectedFmIndex, mFM);
		int selectedNEMmg = getNEMmgFromSelection(mFM, fmIndex);
		if (NO_FM_SELECTED == fmIndex && !mFM.isEmpty()) {
			fmIndex = 0;
			selectedNEMmg = getNEMmgFromSelection(mFM, fmIndex);
		}
		mSelectedFmIndex = fmIndex;
		FM primary = getPrimaryFbetFben(mFM);
		mPrimaryFbet = (null != primary ? primary.getFbet() : null);
		mPrimaryFben = (null != primary ? primary.getFben() : null);
		mUNnr = unNr;
		mNamn = namn;
		mEtiketter = Collections.unmodifiableList(etiketter);
		mNEMmg = selectedNEMmg;
		mHasPresetNEM = !mFM.isEmpty();
		mTpKat = tpKat;
		mFrpGrp = frpGrp;
		mKlass = klass;
		mKlassKod = klassKod;
		mTunnelkod = tunnelKod;
		mMiljo = miljo;
		mMiljoDefined = miljoDefined;
		mIsCustom = isCustom;

		mLabelsText = createLabels();
		mFullText = createFullText();
		mSearchText = createSearchText();

		AndroidUtils.assertIsTrue(isCustom || TextUtils.isEmpty(uuid), "Non-custom material may not have custom UUID.");
		mUuid = !TextUtils.isEmpty(uuid) && isCustom ? uuid : createUuid();
	}

	public static Material fromBundle(Bundle bundle) {
		final List<FM> fm = getFbetFbenFromBundle(bundle);
		final String unNr = bundle.getString(Keys.UNNR);
		final String namn = bundle.getString(Keys.NAMN);
		final int selectedFmIndex = bundle.getInt(Keys.FM_SELECTED_INDEX, NO_FM_SELECTED);
		final boolean hasPresetNEM = !fm.isEmpty();
		final int tpKat = bundle.getInt(Keys.TPKAT);
		final String frpGrp = bundle.getString(Keys.FRPGRP);
		final String tunnelkod = bundle.getString(Keys.TUNNELKOD);
		final boolean miljo = bundle.getBoolean(Keys.MILJO);
		final boolean miljoDefined = bundle.getBoolean(Keys.MILJO_DEFINED, true);
		final boolean isCustom = bundle.getBoolean(Keys.IS_CUSTOM, false);
		final String uuid = bundle.getString(Keys.UUID, null);
		final String klass = bundle.getString(Keys.KLASS);
		final String klassKod = bundle.getString(Keys.KLASSKOD);

		final String[] etiketterArray = bundle.getStringArray(Keys.ETIKETTER);
		final List<String> etiketter = (null != etiketterArray ? Arrays.asList(etiketterArray) : new ArrayList<String>(0));

		return new Material(fm, unNr, namn, etiketter, selectedFmIndex, hasPresetNEM, tpKat, frpGrp, klass, klassKod, tunnelkod, miljo, miljoDefined, isCustom, uuid);
	}

	public static Material fromJSON(JSONObject json) throws JSONException {
		final List<FM> fm = getFbetFbenFromJson(json);
		final String unNr = JSONUtils.getStringOrNull(json, Keys.UNNR);
		final String namn = json.getString(Keys.NAMN);
		final int selectedFmIndex = json.optInt(Keys.FM_SELECTED_INDEX, NO_FM_SELECTED);
		final boolean hasPresetNEM = !fm.isEmpty();
		final int tpKat = json.isNull(Keys.TPKAT) ? TPKAT_NONE : json.optInt(Keys.TPKAT, TPKAT_NONE);
		final String frpGrp = JSONUtils.getStringOrNull(json, Keys.FRPGRP);
		final String klass = JSONUtils.getStringOrNull(json, Keys.KLASS);
		final String klassKod = JSONUtils.getStringOrNull(json, Keys.KLASSKOD);
		final String tunnelkod = JSONUtils.getStringOrNull(json, Keys.TUNNELKOD);
		final boolean miljoDefined = !json.isNull(Keys.MILJO);
		final boolean miljo = json.optBoolean(Keys.MILJO);
		final boolean isCustom = json.optBoolean(Keys.IS_CUSTOM);

		final JSONArray etiketterJson = json.optJSONArray(Keys.ETIKETTER);
		final List<String> etiketter = new ArrayList<>((null == etiketterJson) ? 0 : etiketterJson.length());
		if (null != etiketterJson) {
			for (int i = 0; i < etiketterJson.length(); i++) {
				etiketter.add(etiketterJson.getString(i));
			}
		}

		return new Material(fm, unNr, namn, etiketter, selectedFmIndex, hasPresetNEM, tpKat, frpGrp, klass, klassKod, tunnelkod, miljo, miljoDefined, isCustom, null);
	}

	private static List<FM> getFbetFbenFromBundle(Bundle bundle) {
		ArrayList<String> fbetList = bundle.getStringArrayList(Keys.FBET_LIST);
		ArrayList<String> fbenList = bundle.getStringArrayList(Keys.FBEN_LIST);
		ArrayList<Integer> nemList = bundle.getIntegerArrayList(Keys.FM_NEM_LIST);

		List<FM> fm = new ArrayList<>();
		int count = Math.max((null != fbetList ? fbetList.size() : 0), (null != fbenList ? fbenList.size() : 0));
		count = Math.max(count, (null != nemList ? nemList.size() : 0));
		for (int i = 0; i < count; i++) {
			String fbet = (null != fbetList && i < fbetList.size()) ? fbetList.get(i) : null;
			String fben = (null != fbenList && i < fbenList.size()) ? fbenList.get(i) : null;
			Integer nemMg = (null != nemList && i < nemList.size()) ? nemList.get(i) : null;
			if (!TextUtils.isEmpty(fbet) || !TextUtils.isEmpty(fben) || null != nemMg) {
				fm.add(new FM(fbet, fben, nemMg));
			}
		}

		return fm;
	}

	private static List<FM> getFbetFbenFromJson(JSONObject json) throws JSONException {
		JSONArray fmJson = json.optJSONArray(Keys.FM);
		List<FM> fm = new ArrayList<>((null == fmJson) ? 0 : fmJson.length());
		if (null != fmJson) {
			for (int i = 0; i < fmJson.length(); i++) {
				JSONObject entry = fmJson.optJSONObject(i);
				if (null != entry) {
					String fbet = JSONUtils.getStringOrNull(entry, Keys.FBET);
					String fben = JSONUtils.getStringOrNull(entry, Keys.FBEN);
					Integer nemMg = (entry.has(Keys.NEMMG) && !entry.isNull(Keys.NEMMG)) ? entry.optInt(Keys.NEMMG) : null;
					if (!TextUtils.isEmpty(fbet) || !TextUtils.isEmpty(fben) || null != nemMg) {
						fm.add(new FM(fbet, fben, nemMg));
					}
				}
			}
		}

		return fm;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Material)) {
			return false;
		}

		final Material other = (Material) o;
		return TextUtils.equals(mUuid, other.mUuid);
	}

	public String getFben() {
		FM display = getDisplayFm();
		if (null != display && !TextUtils.isEmpty(display.getFben())) {
			return display.getFben();
		}
		return mPrimaryFben;
	}

	public String getFbet() {
		FM display = getDisplayFm();
		if (null != display && !TextUtils.isEmpty(display.getFbet())) {
			return display.getFbet();
		}
		return mPrimaryFbet;
	}

	public List<FM> getFM() {
		return mFM;
	}

	public int getSelectedFmIndex() {
		return mSelectedFmIndex;
	}

	public FM getSelectedFm() {
		return isValidFmIndex(mSelectedFmIndex) ? mFM.get(mSelectedFmIndex) : null;
	}

	public boolean requiresFmSelection() {
		return mFM.size() > 1;
	}

	public String getFrpGrp() {
		return mFrpGrp;
	}

	public String getFullText() {
		return createFullText();
	}

	public List<String> getEtiketter() {
		return mEtiketter;
	}

	public String getEtiketterAsString() {
		return mLabelsText;
	}

	public String getKlass() {
		return mKlass;
	}

	public String getKlassKod() {
		return mKlassKod;
	}

	public boolean getMiljo() {
		return mMiljo;
	}

	public boolean hasMiljoValue() {
		return mMiljoDefined;
	}

	public BigDecimal getNEMkg() {
		final BigDecimal value = new BigDecimal(mNEMmg);
		return value.divide(new BigDecimal(1000000), 6, BigDecimal.ROUND_FLOOR);
	}

	public int getNEMmg() {
		return mNEMmg;
	}

	public boolean hasPresetNEMValue() {
		return mHasPresetNEM;
	}

	public String getNamn() {
		return mNamn;
	}

	public Material withSelectedFmIndex(int selectedFmIndex) {
		int sanitizedIndex = sanitizeSelectedFmIndex(selectedFmIndex, mFM);
		if (sanitizedIndex == mSelectedFmIndex) {
			return this;
		}
		return new Material(mFM, mUNnr, mNamn, mEtiketter, sanitizedIndex, mHasPresetNEM, mTpKat, mFrpGrp, mKlass, mKlassKod, mTunnelkod, mMiljo, mMiljoDefined, mIsCustom, (mIsCustom ? mUuid : null));
	}

	public int getTpKat() {
		return mTpKat;
	}

	public String getTunnelkod() {
		return mTunnelkod;
	}

	public String getUNnr() {
		return mUNnr;
	}

	public String getUuid() {
		return mUuid;
	}

	public boolean hasNEM() {
		return (0 != mNEMmg);
	}

	public boolean isCustom() {
		return mIsCustom;
	}

	@Override
	public int hashCode() {
		return mUuid.hashCode();
	}

	public boolean matches(CharSequence search) {
		if (TextUtils.isEmpty(search)) {
			return true;
		}
		String normalizedSearch = search.toString().toLowerCase().trim();
		return TextUtils.isEmpty(normalizedSearch) || mSearchText.contains(normalizedSearch);
	}

	public Bundle toBundle() {
		final Bundle bundle = new Bundle();
		ArrayList<String> fbetList = new ArrayList<>(mFM.size());
		ArrayList<String> fbenList = new ArrayList<>(mFM.size());
		ArrayList<Integer> nemList = new ArrayList<>(mFM.size());
		for (FM entry : mFM) {
			fbetList.add(entry.getFbet());
			fbenList.add(entry.getFben());
			nemList.add(entry.getNEMmg());
		}
		bundle.putStringArrayList(Keys.FBET_LIST, fbetList);
		bundle.putStringArrayList(Keys.FBEN_LIST, fbenList);
		bundle.putIntegerArrayList(Keys.FM_NEM_LIST, nemList);
		bundle.putString(Keys.FBET, mPrimaryFbet);
		bundle.putString(Keys.FBEN, mPrimaryFben);
		bundle.putString(Keys.UNNR, mUNnr);
		bundle.putString(Keys.NAMN, mNamn);
		bundle.putStringArray(Keys.ETIKETTER, mEtiketter.toArray(new String[mEtiketter.size()]));
		bundle.putInt(Keys.NEMMG, mNEMmg);
		bundle.putBoolean(Keys.HAS_NEM, mHasPresetNEM);
		bundle.putInt(Keys.FM_SELECTED_INDEX, mSelectedFmIndex);
		bundle.putInt(Keys.TPKAT, mTpKat);
		bundle.putString(Keys.FRPGRP, mFrpGrp);
		bundle.putString(Keys.KLASS, mKlass);
		bundle.putString(Keys.KLASSKOD, mKlassKod);
		bundle.putString(Keys.TUNNELKOD, mTunnelkod);
		bundle.putBoolean(Keys.MILJO, mMiljo);
		bundle.putBoolean(Keys.MILJO_DEFINED, mMiljoDefined);
		if (mIsCustom) {
			bundle.putBoolean(Keys.IS_CUSTOM, true);
			bundle.putString(Keys.UUID, mUuid);
		}
		return bundle;
	}

	public JSONObject toJson() throws JSONException {
		final JSONObject json = new JSONObject();
		JSONArray fbetFbenJson = new JSONArray();
		for (FM entry : mFM) {
			JSONObject fbetFbenEntry = new JSONObject();
			fbetFbenEntry.put(Keys.FBET, entry.getFbet());
			fbetFbenEntry.put(Keys.FBEN, entry.getFben());
			fbetFbenEntry.put(Keys.NEMMG, entry.getNEMmg());
			fbetFbenJson.put(fbetFbenEntry);
		}
		json.put(Keys.FM, fbetFbenJson);
		json.put(Keys.UNNR, mUNnr);
		json.put(Keys.NAMN, mNamn);
		json.put(Keys.ETIKETTER, new JSONArray(mEtiketter));
		json.put(Keys.HAS_NEM, mHasPresetNEM);
		if (NO_FM_SELECTED != mSelectedFmIndex) {
			json.put(Keys.FM_SELECTED_INDEX, mSelectedFmIndex);
		}
		json.put(Keys.TPKAT, mTpKat);
		json.put(Keys.FRPGRP, mFrpGrp);
		json.put(Keys.KLASS, mKlass);
		json.put(Keys.KLASSKOD, mKlassKod);
		json.put(Keys.TUNNELKOD, mTunnelkod);
		if (mMiljoDefined) {
			json.put(Keys.MILJO, mMiljo);
		} else {
			json.put(Keys.MILJO, JSONObject.NULL);
		}
		json.put(Keys.IS_CUSTOM, mIsCustom);
		// UUID is not persisted
		return json;
	}

	@Override
	public String toString() {
		String fben = getFben();
		return (TextUtils.isEmpty(fben) ? mNamn : fben);
	}

	public String toUniqueKey() {
		return mNamn + '|' + mPrimaryFben + '|' + mPrimaryFbet;
	}

	private static int getNEMmgFromSelection(List<FM> list, int selectedIndex) {
		if (selectedIndex >= 0 && selectedIndex < list.size()) {
			return list.get(selectedIndex).getNEMmgAsInt();
		}
		return 0;
	}

	private static FM getPrimaryFbetFben(List<FM> list) {
		FM primary = null;
		for (FM entry : list) {
			if (!TextUtils.isEmpty(entry.getFbet()) || !TextUtils.isEmpty(entry.getFben())) {
				primary = entry;
				break;
			}
		}
		if (null == primary && !list.isEmpty()) {
			primary = list.get(0);
		}
		return primary;
	}

	private static int sanitizeSelectedFmIndex(int selectedFmIndex, List<FM> list) {
		return (selectedFmIndex >= 0 && selectedFmIndex < list.size()) ? selectedFmIndex : NO_FM_SELECTED;
	}

	private boolean isValidFmIndex(int index) {
		return (index >= 0 && index < mFM.size());
	}

	private FM getDisplayFm() {
		if (isValidFmIndex(mSelectedFmIndex)) {
			return mFM.get(mSelectedFmIndex);
		}
		FM primary = getPrimaryFbetFben(mFM);
		if (null != primary) {
			return primary;
		}
		return (!mFM.isEmpty() ? mFM.get(0) : null);
	}

	private String createFullText() {
		final StringBuilder builder = new StringBuilder();

		// UN-nummer
		if (!TextUtils.isEmpty(mUNnr)) {
			builder.append("UN ");
			builder.append(mUNnr);
			builder.append(' ');
		}

		// Transportbenämning
		builder.append(mNamn);

		// Etiketter
		if (!TextUtils.isEmpty(mLabelsText)) {
			builder.append(", ");
			builder.append(mLabelsText);
		}

		// Förpackningsgrupp
		if (!TextUtils.isEmpty(mFrpGrp)) {
			builder.append(", ");
			builder.append(mFrpGrp);
		}

		// Tunnelkod
		if (!TextUtils.isEmpty(mTunnelkod)) {
			builder.append(" (");
			builder.append(mTunnelkod);
			builder.append(')');
		}

		return builder.toString();
	}

public List<String> getDisplayEtiketter() {
		/**
		 * Om materialet är klass 1 och det finns en klasskod,
		 * så ska klasskoden ersätta första etiketten i etikettlistan.
		 */
		if ("1".equals(mKlass) && !TextUtils.isEmpty(mKlassKod) && !mEtiketter.isEmpty()) {
			List<String> display = new ArrayList<>(mEtiketter);
			display.set(0, mKlassKod);
			return Collections.unmodifiableList(display);
		}
		return mEtiketter;
	}

	private String createLabels() {
		List<String> displayEtiketter = getDisplayEtiketter();
		if (displayEtiketter.isEmpty()) {
			return null;
		} else if (1 == displayEtiketter.size()) {
			return displayEtiketter.get(0);
		}

		final StringBuilder builder = new StringBuilder();
		for (int i = 1; i < displayEtiketter.size(); i++) {
			if (0 != builder.length()) {
				builder.append(", ");
			}
			builder.append(displayEtiketter.get(i));
		}
		return String.format("%s (%s)", displayEtiketter.get(0), builder.toString());
	}

	private String createSearchText() {
		final StringBuilder builder = new StringBuilder();
		builder.append(mNamn.toLowerCase());

		for (FM entry : mFM) {
			if (!TextUtils.isEmpty(entry.getFbet())) {
				builder.append(' ');
				builder.append(entry.getFbet().toLowerCase());
			}
			if (!TextUtils.isEmpty(entry.getFben())) {
				builder.append(' ');
				builder.append(entry.getFben().toLowerCase());
			}
		}

		FM display = getDisplayFm();
		if (null != display) {
			if (!TextUtils.isEmpty(display.getFbet())) {
				builder.append(' ');
				builder.append(display.getFbet().toLowerCase());
			}
			if (!TextUtils.isEmpty(display.getFben())) {
				builder.append(' ');
				builder.append(display.getFben().toLowerCase());
			}
		}

		if (!TextUtils.isEmpty(mUNnr)) {
			builder.append(' ');
			builder.append(mUNnr);
		}

		return builder.toString();
	}

	private String createUuid() {
		/*
		 * UUIDs identify materials but function very differently for custom (user-defined) and built-in (loaded from ADR.json) materials.
		 * Custom materials have a UUID which is generated on instantiation and has nothing to do with the contents. This is really just an
		 * index which helps the app keep track of the row within the document. Built-in materials have a UUID based on their contents.
		 *
		 * UUIDs are never persisted, they are only used to match document rows with materials while the app is running. Therefore it is safe
		 * to change this method over time as doing so will not break saved documents.
		 */
		if (mIsCustom) {
			return UUID_PREFIX + (++sUuidCounter);
		} else {
			return mNamn + "/" + mPrimaryFbet + "/" + mPrimaryFben;
		}
	}

	public static final class FM {
		private final String mFbet;
		private final String mFben;
		private final Integer mNEMmg;

		public FM(String fbet, String fben, Integer NEMmg) {
			mFbet = fbet;
			mFben = fben;
			mNEMmg = NEMmg;
		}

		public String getFbet() {
			return mFbet;
		}

		public String getFben() {
			return mFben;
		}

		public Integer getNEMmg() {
			return mNEMmg;
		}

		public int getNEMmgAsInt() {
			return (null != mNEMmg ? mNEMmg : 0);
		}
	}

	public static class Keys {
		public static final String FBEN = "Fben";
		public static final String FBET = "Fbet";
		public static final String FM = "FM";
		public static final String FM_NEM_LIST = "FmNemList";
		public static final String FM_SELECTED_INDEX = "FmSelectedIndex";
		public static final String FBEN_LIST = "FbenList";
		public static final String FBET_LIST = "FbetList";
		public static final String FRPGRP = "FrpGrp";
		public static final String ETIKETTER = "Etiketter";
		public static final String KLASS = "Klass";
		public static final String KLASSKOD = "KlassKod";
		public static final String IS_CUSTOM = "IsCustom";
		public static final String MILJO = "Miljo";
		public static final String MILJO_DEFINED = "MiljoDefined";
		public static final String NAMN = "Tpben";
		public static final String NEMMG = "NEMmg";
		public static final String HAS_NEM = "HasNEM";
		public static final String TPKAT = "TpKat";
		public static final String TUNNELKOD = "TunnelKod";
		public static final String UNNR = "UN";
		public static final String UUID = "Uuid";

		private Keys() {
		}
	}
}
