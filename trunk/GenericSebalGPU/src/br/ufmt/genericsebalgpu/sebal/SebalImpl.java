/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import static br.ufmt.preprocessing.utils.Constants.*;

/**
 *
 * @author raphael
 */
public class SebalImpl extends Sebal {

    @Override
    protected void calcular(
            int[] comptMask,
            float[] SWd,
            float[] LWd,
            float[] albedo,
            float[] emissivity,
            float[] LST_K,
            float[] NDVI,
            float[] Uref,
            float[] SAVI,
            float a,
            float b,
            float[] Rg_24h,
            float[] Tao_24h,
            float[] z0m,
            float[] Ustar,
            float[] r_ah,
            float[] Rn,
            float[] G0,
            float[] H,
            float[] LE,
            float[] evap_fr,
            float[] Rn_24h,
            float[] LE_24h,
            float[] ET_24h,
            int DataSize) {

        for (int i = 0; i < DataSize; i++) {

            if (comptMask[i] == 1) {

                calculated(SWd[i],
                        LWd[i],
                        albedo[i],
                        emissivity[i],
                        LST_K[i],
                        NDVI[i],
                        Uref[i],
                        SAVI[i],
                        a,
                        b,
                        Rg_24h[i],
                        Tao_24h[i],
                        z0m,
                        Ustar,
                        r_ah,
                        Rn,
                        G0,
                        H,
                        LE,
                        evap_fr,
                        Rn_24h,
                        LE_24h,
                        ET_24h,
                        i);
            } else {
                z0m[i] = -9999.0f;
                Ustar[i] = -9999.0f;
                r_ah[i] = -9999.0f;
                Rn[i] = -9999.0f;
                G0[i] = -9999.0f;
                H[i] = -9999.0f;
                LE[i] = -9999.0f;
                evap_fr[i] = -9999.0f;
                Rn_24h[i] = -9999.0f;
                LE_24h[i] = -9999.0f;
                ET_24h[i] = -9999.0f;
            }
        }
    }

    private void calculated(
            float SWd,
            float LWd,
            float albedo,
            float emissivity,
            float LST_K,
            float NDVI,
            float Uref,
            float SAVI,
            float a,
            float b,
            float Rg_24h,
            float Tao_24h,
            float[] z0m,
            float[] U_star,
            float[] r_ah,
            float[] Rn,
            float[] G0,
            float[] H,
            float[] LE,
            float[] evap_fr,
            float[] Rn_24h,
            float[] LE_24h,
            float[] ET_24h,
            int idx) {

        z0m[idx] = (float) Math.exp(-5.809f + 5.62f * SAVI);

        /* Classification */
        boolean I_snow = (NDVI < 0.0f) && (albedo > 0.47f);
        boolean I_water = (NDVI == -1.0f);

        /*	% NOTE: esat_WL is only used for the wet-limit. To get a true upperlimit for the sensible heat
         % the Landsurface Temperature is used as a proxy instead of air temperature.
         %% Net Radiation */
        float SWnet = (1.0f - albedo) * SWd; /* Shortwave Net Radiation [W/m2] */

        float LWnet = (float) (emissivity * LWd - emissivity * Sigma_SB * LST_K * LST_K * LST_K * LST_K); /* Longwave Net Radiation [W/m2] */

        Rn[idx] = SWnet + LWnet; /* Total Net Radiation [W/m2] */

        /* Ground Heat Flux */
        /* Kustas et al 1993 */
        /* Kustas, W.P., Daughtry, C.S.T. van Oevelen P.J., 
         Analatytical Treatment of Relationships between Soil heat flux/net radiation and Vegetation Indices, 
         Remote sensing of environment,46:319-330 (1993) */
        G0[idx] = (float) (Rn[idx] * (((LST_K - T0) / albedo) * (0.0038f * albedo + 0.0074f * albedo * albedo) * (1.0f - 0.98f * NDVI * NDVI * NDVI * NDVI)));

        if (I_water || I_snow) {
            G0[idx] = 0.3f * Rn[idx];
        }

        U_star[idx] = (float) (k * Uref / Math.log(z200 / z0m[idx]));

        r_ah[idx] = (float) (Math.log(z2 / z1) / (U_star[idx] * k));

        H[idx] = (float) (p * cp * (b + a * (LST_K - T0)) / r_ah[idx]);

        LE[idx] = Rn[idx] - H[idx] - G0[idx];

        /* Evaporative fraction */
        evap_fr[idx] = 0.0f;
        if ((Rn[idx] - G0[idx]) != 0.0f) {
            evap_fr[idx] = LE[idx] / (Rn[idx] - G0[idx]); /* evaporative fraction [] */

        } else {
            evap_fr[idx] = 1.0f; /* evaporative fraction upper limit [] (for negative available energy) */

        }

        Rn_24h[idx] = Rg_24h * (1.0f - albedo) - 110 * Tao_24h;
        LE_24h[idx] = evap_fr[idx] * Rn_24h[idx];
        ET_24h[idx] = (evap_fr[idx] * Rn_24h[idx] * 86.4f) / 2450.0f;

    }
}
