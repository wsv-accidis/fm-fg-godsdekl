package se.accidis.fmfg.app.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model object for materials.
 */
public final class Material implements Parcelable {
    public static final Parcelable.Creator<Material> CREATOR = new Parcelable.Creator<Material>() {
        public Material createFromParcel(Parcel in) {
            return Material.fromParcel(in);
        }

        public Material[] newArray(int size) {
            return new Material[size];
        }
    };

    private String mFbet; // Förrådsbeteckning
    private String mFben; // Förrådsbenämning
    private String mUNnr; // UN-nummer
    private String mNamn; // Namn
    private List<String> mKlassKod; // Klassificeringskod/etiketter
    private int mNEMmg; // NEM i mg
    private int mTpKat; // Transportkategori
    private String mFrpGrp; // Förpackningsgrupp
    private String mTunnelkod; // Tunnelkod
    private boolean mMiljo; // Miljöfarligt
    private String mFullText;
    private String mSearchText;

    public static Material fromJSON(JSONObject json) throws JSONException {
        Material m = new Material();
        m.mFbet = getStringOrNull(json, Keys.FBET);
        m.mFben = getStringOrNull(json, Keys.FBEN);
        m.mUNnr = getStringOrNull(json, Keys.UNNR);
        m.mNamn = json.getString(Keys.NAMN);
        m.mNEMmg = json.optInt(Keys.NEMMG);
        m.mTpKat = json.getInt(Keys.TPKAT);
        m.mFrpGrp = getStringOrNull(json, Keys.FRPGRP);
        m.mTunnelkod = getStringOrNull(json, Keys.TUNNELKOD);
        m.mMiljo = json.optBoolean(Keys.MILJO);

        JSONArray klassKodJson = json.optJSONArray(Keys.KLASSKOD);
        List<String> klassKod = new ArrayList<>((null == klassKodJson) ? 0 : klassKodJson.length());
        if (null != klassKodJson) {
            for (int i = 0; i < klassKodJson.length(); i++) {
                klassKod.add(klassKodJson.getString(i));
            }
        }
        m.mKlassKod = Collections.unmodifiableList(klassKod);

        m.initFullText();
        m.initSearchText();
        return m;
    }

    private static Material fromParcel(Parcel parcel) {
        Material m = new Material();
        m.mFbet = parcel.readString();
        m.mFben = parcel.readString();
        m.mUNnr = parcel.readString();
        m.mNamn = parcel.readString();
        m.mKlassKod = Collections.unmodifiableList(new ArrayList<String>(parcel.readStringArray();))
        m.mNEMmg = parcel.readInt();
        m.mTpKat = parcel.readInt();
        m.mFrpGrp = parcel.readString();
        m.mTunnelkod = parcel.readString();
        m.mMiljo = ( 0 != parcel.readInt());
    }

    private static String getStringOrNull(JSONObject json, String key) throws JSONException {
        return (json.isNull(key) ? null : json.getString(key));
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

    public List<String> getKlassKod() {
        return mKlassKod;
    }

    public boolean getMiljo() {
        return mMiljo;
    }

    public String getNamn() {
        return mNamn;
    }

    public int getNEMmg() {
        return mNEMmg;
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

    private void initFullText() {
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

        mFullText = builder.toString();
    }

    public void initSearchText() {
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

        mSearchText = builder.toString();
    }

    public String getFullText() {
        return mFullText;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        // TODO Waaah

        dest.writeString(mFbet);
        dest.writeString(mFben);
        dest.writeString(mUNnr);
        dest.writeString(mNamn);
        dest.writeStringArray(mKlassKod.toArray(new String[mKlassKod.size()]));
        dest.writeInt(mNEMmg);
        dest.writeInt(mTpKat);
        dest.writeString(mFrpGrp);
        dest.writeString(mTunnelkod);
        dest.writeInt(mMiljo ? 1 : 0);
    }

    public static class Keys {
        public static final String FBET = "Fbet";
        public static final String FBEN = "Fben";
        public static final String UNNR = "UNnr";
        public static final String NAMN = "Namn";
        public static final String KLASSKOD = "KlassKod";
        public static final String NEMMG = "NEMmg";
        public static final String TPKAT = "TpKat";
        public static final String FRPGRP = "FrpGrp";
        public static final String TUNNELKOD = "TunnelKod";
        public static final String MILJO = "Miljo";

        private Keys() {
        }
    }
}
