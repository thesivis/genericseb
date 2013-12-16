/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing.utils;

/**
 *
 * @author raphael
 */
public enum ParameterEnum {

    A("a.dat"),
    B("b.dat"),
    COORDENATES("coordenates.dat"),
    NDVI("NDVI.dat"),
    Ts("Ts.dat"),
    Uref("UREF.dat"),
    emissividadeNB("emissividadeNB.dat"),
    SAVI("SAVI.dat"),
    Uref_hot("UREF_HOT.dat"),
    Rn_hot("RN_HOT.dat"),
    G_hot("G_HOT.dat"),
    Ts_hot("TS_HOT.dat"),
    Ts_cold("TS_COLD.dat"),
    SAVI_hot("SAVI_HOT.dat"),
    albedo("albedo.dat"),
    emissivity("emissivity.dat"),
    SWnet("SWD.dat"),
    LWnet("LWD.dat"),
    Rg_24h("RG_24H.dat"),
    Tao_24h("TAO_24H.dat"),
    r_ah("R_AH.dat"),
    d0("d0.dat"),
    z0h("z0h.dat"),
    z0m("z0m.dat"),
    Rn("RN.dat"),
    G0("G0.dat"),
    G02("G02.dat"),
    H("H.dat"),
    LE("LE.dat"),
    EF("EF.dat"),
    re_i("re_i.dat"),
    U_star("U_STAR.dat"),
    H_DL("H_DL.dat"),
    H_WL("H_WL.dat"),
    Rn_24h("RN_24H.dat"),
    ET_24h("ET_24H.dat"),
    LE_24h("LE_24H.dat"),
    fc("fc.dat"),
    IAF("IAF.dat"),
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
    ComptMask("ComptMask.dat"),
    evap_fr("evap_fr.dat"),
    dr("dr.dat"),
    cosZ("cosZ.dat"),
    declinacaoSolar("declinacaoSolar"),
    anguloHorarioNascerSol("anguloHorarioNascerSol.dat"),
    rad_solar_toa("rad_solar_toa.dat"),
    Rg_24h_mj("Rg_24h_mj.dat"),
    transmissividade24h("transmissividade24h.dat"),
    ea("ea.dat"),
    W("W.dat"),
    transmissividade("transmissividade.dat"),
    emissivityAtm("emissivityAtm.dat"),
    SWd("SWd.dat"),
    LWd("LWd.dat"),
    LWdAtm("LWdAtm.dat"),
    reflectancia("reflectancia.dat"),
    banda1("banda1.dat"),
    banda2("banda2.dat"),
    banda3("banda3.dat"),
    banda4("banda4.dat"),
    banda5("banda5.dat"),
    banda6("banda6.dat"),
    banda7("banda7.dat"),
    bandaRefletida1("bandaRefletida1.dat"),
    bandaRefletida2("bandaRefletida2.dat"),
    bandaRefletida3("bandaRefletida3.dat"),
    bandaRefletida4("bandaRefletida4.dat"),
    bandaRefletida5("bandaRefletida5.dat"),
    bandaRefletida6("bandaRefletida6.dat"),
    bandaRefletida7("bandaRefletida7.dat"),
    irrad_espectral("irrad_espectral.dat"),
    rad_espectral("rad_espectral.dat"),
    coef_calib_a("coef_calib_a.dat"),
    coef_calib_b("coef_calib_b.dat"),
    pixel("pixel.dat"),
    sumBandas("sumBandas.dat");

    private ParameterEnum(String fileName) {
        this.fileName = fileName;
    }
    private String fileName;

    public String getFileName() {
        return fileName;
    }
    
    public String getName() {
        return fileName.replace(".dat", "");
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return this.fileName.replace(".dat", "");
    }
    
    
}
