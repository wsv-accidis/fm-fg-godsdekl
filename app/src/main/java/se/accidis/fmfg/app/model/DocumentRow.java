package se.accidis.fmfg.app.model;

import java.math.BigDecimal;

/**
 * Model object for rows in a transport declaration.
 */
public final class DocumentRow {
    private final Material mMaterial;
    private int mAmount; // Antal enheter för beräkning av NEM
    private int mNumberOfPkgs; // Antal kolli
    private String mTypeOfPkgs; // Beskrivning av kolli
    private BigDecimal mWeightVolume; // Kvantitet farligt gods i liter / kg

    public DocumentRow(Material material) {
        mMaterial = material;
    }

    public int getAmount() {
        return mAmount;
    }

    public void setAmount(int amount) {
        mAmount = amount;
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
        mWeightVolume = weightVolume;
    }
}
