package se.accidis.fmfg.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model object for materials.
 */
public final class Material {
    private String mFbet; // Förrådsbeteckning
    private String mFben; // Förrådsbenämning
    private String mUNnr; // UN-nummer
    private String mNamn; // Namn
    private List<String> mKlassKod; // Klassificeringskod och etiketter
    private int mNEMmg; // NEM i mg
    private int mTpKat; // Transportkategori
    private String mFrpGrp; // Förpackningsgrupp
    private String mTunnelkod; // Tunnelkod
    private boolean mMiljo; // Miljöfarligt

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

        return m;
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
