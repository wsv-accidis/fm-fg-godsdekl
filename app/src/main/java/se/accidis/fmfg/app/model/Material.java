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

	private static final String UUID_PREFIX = "Uuid_";
	private static long sUuidCounter = 0;

	private final String mFben; // Förrådsbenämning
	private final String mFbet; // Förrådsbeteckning
	private final String mFrpGrp; // Förpackningsgrupp
	private final String mFullText;
	private final boolean mIsCustom;
	private final List<String> mKlassKod; // Klassificeringskod/etiketter
	private final String mLabelsText;
	private final boolean mMiljo; // Miljöfarligt
	private final boolean mMiljoDefined; // True if JSON explicitly specified value
	private final int mNEMmg; // NEM i mg
	private final boolean mHasPresetNEM;
	private final String mNamn; // Namn
	private final String mSearchText;
	private final int mTpKat; // Transportkategori
	private final String mTunnelkod; // Tunnelkod
	private final String mUNnr; // UN-nummer
	private final String mUuid;

	public static Material createCustom(String namn, List<String> klassKod, String uuid) {
		return new Material("", "", "", namn, klassKod, 0, false, TPKAT_NONE, "", "", false, false, true, uuid);
	}

	private Material(String fbet, String fben, String unNr, String namn, List<String> klassKod, int NEMmg, boolean hasPresetNEM, int tpKat, String frpGrp, String tunnelKod, boolean miljo, boolean miljoDefined, boolean isCustom, String uuid) {
		mFbet = fbet;
		mFben = fben;
		mUNnr = unNr;
		mNamn = namn;
		mKlassKod = Collections.unmodifiableList(klassKod);
		mNEMmg = NEMmg;
		mHasPresetNEM = hasPresetNEM;
		mTpKat = tpKat;
		mFrpGrp = frpGrp;
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
		final String fbet = bundle.getString(Keys.FBET);
		final String fben = bundle.getString(Keys.FBEN);
		final String unNr = bundle.getString(Keys.UNNR);
		final String namn = bundle.getString(Keys.NAMN);
		final int NEMmg = bundle.getInt(Keys.NEMMG);
		final boolean hasPresetNEM = bundle.getBoolean(Keys.HAS_NEM, bundle.containsKey(Keys.NEMMG));
		final int tpKat = bundle.getInt(Keys.TPKAT);
		final String frpGrp = bundle.getString(Keys.FRPGRP);
		final String tunnelkod = bundle.getString(Keys.TUNNELKOD);
		final boolean miljo = bundle.getBoolean(Keys.MILJO);
		final boolean miljoDefined = bundle.getBoolean(Keys.MILJO_DEFINED, true);
		final boolean isCustom = bundle.getBoolean(Keys.IS_CUSTOM, false);
		final String uuid = bundle.getString(Keys.UUID, null);

		final String[] klassKodArray = bundle.getStringArray(Keys.KLASSKOD);
		final List<String> klassKod = (null != klassKodArray ? Arrays.asList(klassKodArray) : new ArrayList<String>(0));

		return new Material(fbet, fben, unNr, namn, klassKod, NEMmg, hasPresetNEM, tpKat, frpGrp, tunnelkod, miljo, miljoDefined, isCustom, uuid);
	}

	public static Material fromJSON(JSONObject json) throws JSONException {
		final String fbet = JSONUtils.getStringOrNull(json, Keys.FBET);
		final String fben = JSONUtils.getStringOrNull(json, Keys.FBEN);
		final String unNr = JSONUtils.getStringOrNull(json, Keys.UNNR);
		final String namn = json.getString(Keys.NAMN);
		final boolean hasPresetNEM = json.has(Keys.HAS_NEM) ? json.optBoolean(Keys.HAS_NEM) : (json.has(Keys.NEMMG) && !json.isNull(Keys.NEMMG));
		final int NEMmg = json.optInt(Keys.NEMMG);
		final int tpKat = json.getInt(Keys.TPKAT);
		final String frpGrp = JSONUtils.getStringOrNull(json, Keys.FRPGRP);
		final String tunnelkod = JSONUtils.getStringOrNull(json, Keys.TUNNELKOD);
		final boolean miljoDefined = !json.isNull(Keys.MILJO);
		final boolean miljo = json.optBoolean(Keys.MILJO);
		final boolean isCustom = json.optBoolean(Keys.IS_CUSTOM);

		final JSONArray klassKodJson = json.optJSONArray(Keys.KLASSKOD);
		final List<String> klassKod = new ArrayList<>((null == klassKodJson) ? 0 : klassKodJson.length());
		if (null != klassKodJson) {
			for (int i = 0; i < klassKodJson.length(); i++) {
				klassKod.add(klassKodJson.getString(i));
			}
		}

		return new Material(fbet, fben, unNr, namn, klassKod, NEMmg, hasPresetNEM, tpKat, frpGrp, tunnelkod, miljo, miljoDefined, isCustom, null);
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
		return mFben;
	}

	public String getFbet() {
		return mFbet;
	}

	public String getFrpGrp() {
		return mFrpGrp;
	}

	public String getFullText() {
		return mFullText;
	}

	public List<String> getKlassKod() {
		return mKlassKod;
	}

	public String getKlassKodAsString() {
		return mLabelsText;
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
		return mSearchText.contains(search);
	}

	public Bundle toBundle() {
		final Bundle bundle = new Bundle();
		bundle.putString(Keys.FBET, mFbet);
		bundle.putString(Keys.FBEN, mFben);
		bundle.putString(Keys.UNNR, mUNnr);
		bundle.putString(Keys.NAMN, mNamn);
		bundle.putStringArray(Keys.KLASSKOD, mKlassKod.toArray(new String[mKlassKod.size()]));
		bundle.putInt(Keys.NEMMG, mNEMmg);
		bundle.putBoolean(Keys.HAS_NEM, mHasPresetNEM);
		bundle.putInt(Keys.TPKAT, mTpKat);
		bundle.putString(Keys.FRPGRP, mFrpGrp);
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
		json.put(Keys.FBET, mFbet);
		json.put(Keys.FBEN, mFben);
		json.put(Keys.UNNR, mUNnr);
		json.put(Keys.NAMN, mNamn);
		json.put(Keys.KLASSKOD, new JSONArray(mKlassKod));
		json.put(Keys.NEMMG, mNEMmg);
		json.put(Keys.HAS_NEM, mHasPresetNEM);
		json.put(Keys.TPKAT, mTpKat);
		json.put(Keys.FRPGRP, mFrpGrp);
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
		return (TextUtils.isEmpty(mFben) ? mNamn : mFben);
	}

	public String toUniqueKey() {
		return mNamn + '|' + mFben + '|' + mFbet;
	}

	private String createFullText() {
		final StringBuilder builder = new StringBuilder();

		if (!TextUtils.isEmpty(mUNnr)) {
			builder.append("UN ");
			builder.append(mUNnr);
			builder.append(' ');
		}

		builder.append(mNamn);

		if (!TextUtils.isEmpty(mLabelsText)) {
			builder.append(", ");
			builder.append(mLabelsText);
		}

		if (!TextUtils.isEmpty(mFrpGrp)) {
			builder.append(", ");
			builder.append(mFrpGrp);
		}

		if (!TextUtils.isEmpty(mTunnelkod)) {
			builder.append(" (");
			builder.append(mTunnelkod);
			builder.append(')');
		}

		return builder.toString();
	}

	private String createLabels() {
		if (mKlassKod.isEmpty()) {
			return null;
		} else if (1 == mKlassKod.size()) {
			return mKlassKod.get(0);
		}

		final StringBuilder builder = new StringBuilder();
		for (int i = 1; i < mKlassKod.size(); i++) {
			if (0 != builder.length()) {
				builder.append(", ");
			}
			builder.append(mKlassKod.get(i));
		}
		return String.format("%s (%s)", mKlassKod.get(0), builder.toString());
	}

	private String createSearchText() {
		final StringBuilder builder = new StringBuilder();
		builder.append(mNamn.toLowerCase());

		if (!TextUtils.isEmpty(mFbet)) {
			builder.append(' ');
			builder.append(mFbet.toLowerCase());
		}

		if (!TextUtils.isEmpty(mFben)) {
			builder.append(' ');
			builder.append(mFben.toLowerCase());
		}

		if(!TextUtils.isEmpty(mUNnr)) {
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
			return mNamn + "/" + mFbet + "/" + mFben;
		}
	}

	public static class Keys {
		public static final String FBEN = "Fben";
		public static final String FBET = "Fbet";
		public static final String FRPGRP = "FrpGrp";
		public static final String KLASSKOD = "KlassKod";
		public static final String IS_CUSTOM = "IsCustom";
		public static final String MILJO = "Miljo";
		public static final String MILJO_DEFINED = "MiljoDefined";
		public static final String NAMN = "Namn";
		public static final String NEMMG = "NEMmg";
		public static final String HAS_NEM = "HasNEM";
		public static final String TPKAT = "TpKat";
		public static final String TUNNELKOD = "TunnelKod";
		public static final String UNNR = "UNnr";
		public static final String UUID = "Uuid";

		private Keys() {
		}
	}
}
