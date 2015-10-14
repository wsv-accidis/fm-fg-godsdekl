package se.accidis.fmfg.app.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Model object for materials.
 */
public final class Material implements Parcelable {
    public static final Parcelable.Creator<Material> CREATOR = new Parcelable.Creator<Material>() {
        public Material createFromParcel(Parcel in) {
            Bundle bundle = in.readBundle();
            return fromBundle(bundle);
        }

        public Material[] newArray(int size) {
            return new Material[size];
        }
    };

    private final String mFben; // Förrådsbenämning
    private final String mFbet; // Förrådsbeteckning
    private final String mFrpGrp; // Förpackningsgrupp
    private final String mFullText;
    private final List<String> mKlassKod; // Klassificeringskod/etiketter
    private final boolean mMiljo; // Miljöfarligt
    private final int mNEMmg; // NEM i mg
    private final String mNamn; // Namn
    private final String mSearchText;
    private final int mTpKat; // Transportkategori
    private final String mTunnelkod; // Tunnelkod
    private final String mUNnr; // UN-nummer

    public Material(String fbet, String fben, String unNr, String namn, List<String> klassKod, int NEMmg, int tpKat, String frpGrp, String tunnelKod, boolean miljo) {
        mFbet = fbet;
        mFben = fben;
        mUNnr = unNr;
        mNamn = namn;
        mKlassKod = Collections.unmodifiableList(klassKod);
        mNEMmg = NEMmg;
        mTpKat = tpKat;
        mFrpGrp = frpGrp;
        mTunnelkod = tunnelKod;
        mMiljo = miljo;

        mFullText = createFullText();
        mSearchText = createSearchText();
    }

    public static Material fromBundle(Bundle bundle) {
        String fbet = bundle.getString(Keys.FBET);
        String fben = bundle.getString(Keys.FBEN);
        String unNr = bundle.getString(Keys.UNNR);
        String namn = bundle.getString(Keys.NAMN);
        int NEMmg = bundle.getInt(Keys.NEMMG);
        int tpKat = bundle.getInt(Keys.TPKAT);
        String frpGrp = bundle.getString(Keys.FRPGRP);
        String tunnelkod = bundle.getString(Keys.TUNNELKOD);
        boolean miljo = bundle.getBoolean(Keys.MILJO);

        String[] klassKodArray = bundle.getStringArray(Keys.KLASSKOD);
        List<String> klassKod = (null != klassKodArray ? Arrays.asList(klassKodArray) : new ArrayList<String>(0));

        return new Material(fbet, fben, unNr, namn, klassKod, NEMmg, tpKat, frpGrp, tunnelkod, miljo);
    }

    public static Material fromJSON(JSONObject json) throws JSONException {
        String fbet = getStringOrNull(json, Keys.FBET);
        String fben = getStringOrNull(json, Keys.FBEN);
        String unNr = getStringOrNull(json, Keys.UNNR);
        String namn = json.getString(Keys.NAMN);
        int NEMmg = json.optInt(Keys.NEMMG);
        int tpKat = json.getInt(Keys.TPKAT);
        String frpGrp = getStringOrNull(json, Keys.FRPGRP);
        String tunnelkod = getStringOrNull(json, Keys.TUNNELKOD);
        boolean miljo = json.optBoolean(Keys.MILJO);

        JSONArray klassKodJson = json.optJSONArray(Keys.KLASSKOD);
        List<String> klassKod = new ArrayList<>((null == klassKodJson) ? 0 : klassKodJson.length());
        if (null != klassKodJson) {
            for (int i = 0; i < klassKodJson.length(); i++) {
                klassKod.add(klassKodJson.getString(i));
            }
        }

        return new Material(fbet, fben, unNr, namn, klassKod, NEMmg, tpKat, frpGrp, tunnelkod, miljo);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public boolean getMiljo() {
        return mMiljo;
    }

    public int getNEMmg() {
        return mNEMmg;
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

    public boolean matches(CharSequence search) {
        return mSearchText.contains(search);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(Keys.FBET, mFbet);
        bundle.putString(Keys.FBEN, mFben);
        bundle.putString(Keys.UNNR, mUNnr);
        bundle.putString(Keys.NAMN, mNamn);
        bundle.putStringArray(Keys.KLASSKOD, mKlassKod.toArray(new String[mKlassKod.size()]));
        bundle.putInt(Keys.NEMMG, mNEMmg);
        bundle.putInt(Keys.TPKAT, mTpKat);
        bundle.putString(Keys.FRPGRP, mFrpGrp);
        bundle.putString(Keys.TUNNELKOD, mTunnelkod);
        bundle.putBoolean(Keys.MILJO, mMiljo);
        return bundle;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = toBundle();
        dest.writeBundle(bundle);
    }

    private String createFullText() {
        StringBuilder builder = new StringBuilder();

        if (!TextUtils.isEmpty(mUNnr)) {
            builder.append("UN ");
            builder.append(mUNnr);
            builder.append(' ');
        }

        builder.append(mNamn);

        String labels = getLabels();
        if (!TextUtils.isEmpty(labels)) {
            builder.append(", ");
            builder.append(getLabels());
        }

        if (!TextUtils.isEmpty(mFrpGrp)) {
            builder.append(", ");
            builder.append(mFrpGrp);
        }

        if (!TextUtils.isEmpty(mTunnelkod)) {
            builder.append(" (");
            builder.append(mTunnelkod);
            builder.append(")");
        }

        return builder.toString();
    }

    private String createSearchText() {
        StringBuilder builder = new StringBuilder();
        builder.append(mNamn.toLowerCase());

        if (!TextUtils.isEmpty(mFbet)) {
            builder.append(' ');
            builder.append(mFbet.toLowerCase());
        }

        if (!TextUtils.isEmpty(mFben)) {
            builder.append(' ');
            builder.append(mFben.toLowerCase());
        }

        return builder.toString();
    }

    private String getLabels() {
        if (mKlassKod.isEmpty()) {
            return null;
        } else if (1 == mKlassKod.size()) {
            return mKlassKod.get(0);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < mKlassKod.size(); i++) {
            if (0 != builder.length()) {
                builder.append(", ");
            }
            builder.append(mKlassKod.get(i));
        }
        return String.format("%s (%s)", mKlassKod.get(0), builder.toString());
    }

    private static String getStringOrNull(JSONObject json, String key) throws JSONException {
        return (json.isNull(key) ? null : json.getString(key));
    }

    public static class Keys {
        public static final String FBEN = "Fben";
        public static final String FBET = "Fbet";
        public static final String FRPGRP = "FrpGrp";
        public static final String KLASSKOD = "KlassKod";
        public static final String MILJO = "Miljo";
        public static final String NAMN = "Namn";
        public static final String NEMMG = "NEMmg";
        public static final String TPKAT = "TpKat";
        public static final String TUNNELKOD = "TunnelKod";
        public static final String UNNR = "UNnr";

        private Keys() {
        }
    }
}
