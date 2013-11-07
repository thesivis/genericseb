/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.utils;

/**
 *
 * @author raphael
 */
public enum ParameterEnum {

    A("a.dat"),
    B("b.dat"),
    COORDENATES("coordenates.dat"),
    NDVI("NDVI.dat"),
    LST_K("LST_K.dat"),
    Uref("UREF.dat"),
    EmissividadeNB("emissividadeNB.dat"),
    SAVI("SAVI.dat"),
    Uref_hot("UREF_HOT.dat"),
    Rn_hot("RN_HOT.dat"),
    G_hot("G_HOT.dat"),
    Ts_hot("TS_HOT.dat"),
    Ts_cold("TS_COLD.dat"),
    SAVI_hot("SAVI_HOT.dat"),
    Albedo("ALBEDO.dat"),
    Emissivity("EMISSIVITY.dat"),
    SWd("SWD.dat"),
    LWd("LWD.dat"),
    Rg_24h("RG_24H.dat"),
    Tao_24h("TAO_24H.dat"),
    R_AH("R_AH.dat"),
    d0("d0.dat"),
    z0h("z0h.dat"),
    z0m("z0m.dat"),
    Rn("RN.dat"),
    G0("G0.dat"),
    H("H.dat"),
    LE("LE.dat"),
    EF("EF.dat"),
    re_i("re_i.dat"),
    ustar("U_STAR.dat"),
    H_DL("H_DL.dat"),
    H_WL("H_WL.dat"),
    Rn_24h("RN_24H.dat"),
    ET_24h("ET_24H.dat"),
    LE_24h("LE_24H.dat"),
    fc("fc.dat"),
    LAI("LAI.dat"),
    hc("hc.dat"),
    Zref("Zref.dat"),
    Pref("Pref.dat"),
    P0("P0.dat"),
    Ps("Ps.dat"),
    Tref_K("Tref_K.dat"),
    qa_ref("qa_ref.dat"),
    hpbl("hpbl.dat"),
    SWd24("SWd24.dat"),
    lat_rad("lat_rad.dat"),
    Ta_av_K("Ta_av_K.dat"),
    Ns("Ns.dat"),
    ComptMask("ComptMask.dat");

    private ParameterEnum(String fileName) {
        this.fileName = fileName;
    }
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return this.fileName.replace(".dat", "");
    }
    
    
}
