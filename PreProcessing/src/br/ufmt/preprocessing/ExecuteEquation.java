/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing;

import java.util.HashMap;

/**
 *
 * @author raphael
 */
public abstract class ExecuteEquation {

    protected float[] albedo = null;
    protected float[] NDVI = null;
    protected float[] Rn = null;
    protected float[] SAVI = null;
    protected float[] Ts = null;
    protected float[] emissivity = null;
    protected float[] IAF = null;
    protected float[] emissividadeNB = null;
    protected float[] LWd = null;
    protected float[][] calibration = null;
    protected float banda4, banda3, banda1, banda2, banda5, banda6, banda7;
    protected float bandaRefletida4, bandaRefletida3, bandaRefletida1, bandaRefletida2, bandaRefletida5, bandaRefletida7;
    protected float dr;
    protected float cosZ;
    protected float declinacaoSolar;
    protected float anguloHorarioNascerSol;
    protected float rad_solar_toa;
    protected float Rg_24h_mj;
    protected float transmissividade24h;
    protected float ea;
    protected float W;
    protected float transmissividade;
    protected float emissivityAtm;
    protected int julianDay;
    protected float Z;
    protected float reflectanciaAtmosfera;
    protected float P;
    protected float UR;
    protected float Ta;
    protected float Kt;
    protected float L;
    protected float K1;
    protected float K2;
    protected float S;
    protected float StefanBoltzman;
    protected float latitude;
    protected float Rg_24h;
    protected float Uref;
    protected float SWd;
    protected float LWdAtm;

    public void setParameters(float[] albedo, float[] NDVI, float[] Rn, float[] SAVI, float[] Ts, float[] emissivity, float[] IAF, float[] emissividadeNB, float[] LWd, float[][] calibration, float dr, float cosZ, float declinacaoSolar, float anguloHorarioNascerSol, float rad_solar_toa, float Rg_24h_mj, float transmissividade24h, float ea, float W, float transmissividade, float emissivityAtm, int julianDay, float Z, float reflectanciaAtmosfera, float P, float UR, float Ta, float Kt, float L, float K1, float K2, float S, float StefanBoltzman, float latitude, float Rg_24h, float Uref, float SWd, float LWdAtm) {
        this.albedo = albedo;
        this.NDVI = NDVI;
        this.Rn = Rn;
        this.SAVI = SAVI;
        this.Ts = Ts;
        this.emissivity = emissivity;
        this.IAF = IAF;
        this.emissividadeNB = emissividadeNB;
        this.LWd = LWd;
        this.calibration = calibration;
        this.dr = dr;
        this.cosZ = cosZ;
        this.declinacaoSolar = declinacaoSolar;
        this.anguloHorarioNascerSol = anguloHorarioNascerSol;
        this.rad_solar_toa = rad_solar_toa;
        this.Rg_24h_mj = Rg_24h_mj;
        this.transmissividade24h = transmissividade24h;
        this.ea = ea;
        this.W = W;
        this.transmissividade = transmissividade;
        this.emissivityAtm = emissivityAtm;
        this.julianDay = julianDay;
        this.Z = Z;
        this.reflectanciaAtmosfera = reflectanciaAtmosfera;
        this.P = P;
        this.UR = UR;
        this.Ta = Ta;
        this.Kt = Kt;
        this.L = L;
        this.K1 = K1;
        this.K2 = K2;
        this.S = S;
        this.StefanBoltzman = StefanBoltzman;
        this.latitude = latitude;
        this.Rg_24h = Rg_24h;
        this.Uref = Uref;
        this.SWd = SWd;
        this.LWdAtm = LWdAtm;
    }

    public abstract void execute(double[] valor, int idx);
}
