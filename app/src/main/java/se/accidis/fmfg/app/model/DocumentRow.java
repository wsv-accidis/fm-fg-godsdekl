package se.accidis.fmfg.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import se.accidis.fmfg.app.ui.materials.ValueHelper;

/**
 * Model object for rows in a transport declaration.
 */
public final class DocumentRow {
    private final Material mMaterial;
    private BigDecimal mAmount; // Antal enheter för beräkning av NEM
    private boolean mIsVolume; // Huruvida kvantitet är angiven i liter
    private int mNumberOfPkgs; // Antal kolli
    private String mTypeOfPkgs; // Beskrivning av kolli
    private BigDecimal mWeightVolume; // Kvantitet farligt gods i liter / kg
    private final BigDecimal mMultiplier;

    public DocumentRow(Material material) {
        mMaterial = material;
        mMultiplier = new BigDecimal(ValueHelper.getMultiplierByTpKat(mMaterial.getTpKat()));
        mWeightVolume = BigDecimal.ZERO;
    }

    public static DocumentRow fromJson(JSONObject json) throws JSONException {
        Material material = Material.fromJSON(json);
        DocumentRow row = new DocumentRow(material);
        row.mAmount = new BigDecimal(json.getString(Keys.AMOUNT));
        row.mIsVolume = json.getBoolean(Keys.IS_VOLUME);
        row.mNumberOfPkgs = json.getInt(Keys.NUMBER_OF_PKGS);
        row.mTypeOfPkgs = json.getString(Keys.TYPE_OF_PKGS);
        row.mWeightVolume = new BigDecimal(json.getString(Keys.WEIGHT_VOLUME));
        return row;
    }

    public void copyTo(DocumentRow other) {
        other.mAmount = mAmount;
        other.mIsVolume = mIsVolume;
        other.mNumberOfPkgs = mNumberOfPkgs;
        other.mTypeOfPkgs = mTypeOfPkgs;
        other.mWeightVolume = mWeightVolume;
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public void setAmount(BigDecimal amount) {
        mAmount = amount;
    }

    public BigDecimal getCalculatedValue() {
        BigDecimal value;
        if (0 != mMaterial.getNEMmg()) {
            value = mMaterial.getNEMkg().multiply(mAmount);
        } else {
            value = mWeightVolume;
        }
        return value.multiply(mMultiplier);
    }

    public Material getMaterial() {
        return mMaterial;
    }

    public int getNumberOfPackages() {
        return mNumberOfPkgs;
    }

    public void setNumberOfPackages(int numPackages) {
        mNumberOfPkgs = numPackages;
    }

    public String getTypeOfPackages() {
        return mTypeOfPkgs;
    }

    public void setTypeOfPackages(String typeOfPackages) {
        mTypeOfPkgs = typeOfPackages;
    }

    public BigDecimal getWeightVolume() {
        return mWeightVolume;
    }

    public void setWeightVolume(BigDecimal weightVolume) {
        mWeightVolume = (null != weightVolume ? weightVolume : BigDecimal.ZERO);
    }

    public boolean isVolume() {
        return mIsVolume;
    }

    public void setIsVolume(boolean isVolume) {
        mIsVolume = isVolume;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = mMaterial.toJson();
        json.put(Keys.AMOUNT, mAmount.toString());
        json.put(Keys.IS_VOLUME, mIsVolume);
        json.put(Keys.NUMBER_OF_PKGS, mNumberOfPkgs);
        json.put(Keys.TYPE_OF_PKGS, mTypeOfPkgs);
        json.put(Keys.WEIGHT_VOLUME, mWeightVolume.toString());
        return json;
    }

    public static class Keys {
        public static final String AMOUNT = "Amount";
        public static final String IS_VOLUME = "IsVolume";
        public static final String NUMBER_OF_PKGS = "NumberOfPkgs";
        public static final String TYPE_OF_PKGS = "TypeOfPkgs";
        public static final String WEIGHT_VOLUME = "WeightVolume";

        private Keys() {
        }
    }
}
