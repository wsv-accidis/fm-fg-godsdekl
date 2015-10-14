package se.accidis.fmfg.app.model;

import java.util.List;

/**
 * Model object for rows in a transport declaration.
 */
public final class DocumentRow {
    private String mFben; // Förrådsbenämning
    private String mUNnr; // UN-nummer
    private String mNamn; // Namn
    private List<String> mKlassKod; // Klassificeringskod/etiketter
    private int mNEMmg; // NEM i mg (per enhet)
    private int mNEMEnheter; // Antal enheter för beräkning av NEM
    private int mAntalKolli; // Antal kolli
    private String mTypKolli; // Beskrivning av kolli
    private int mTpKat; // Transportkategori
    private String mFrpGrp; // Förpackningsgrupp
    private String mTunnelkod; // Tunnelkod
    private boolean mMiljo; // Miljöfarligt
    private int mKvantitet; // Kvantitet fg i ml eller mg
}
