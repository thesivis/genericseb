/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.sebal;

import br.ufmt.jgpusebal.utils.Constants;

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
                /* Classification */
                boolean I_snow = (NDVI[i] < 0.0f) && (albedo[i] > 0.47f);
                boolean I_water = (NDVI[i] == -1.0f);

                /*	% NOTE: esat_WL is only used for the wet-limit. To get a true upperlimit for the sensible heat
                % the Landsurface Temperature is used as a proxy instead of air temperature.
                %% Net Radiation */
                float SWnet = (1.0f - albedo[i]) * SWd[i]; /* Shortwave Net Radiation [W/m2] */
                float LWnet = (float) (emissivity[i] * LWd[i] - emissivity[i] * Constants.Sigma_SB * LST_K[i] * LST_K[i] * LST_K[i] * LST_K[i]); /* Longwave Net Radiation [W/m2] */
                Rn[i] = SWnet + LWnet; /* Total Net Radiation [W/m2] */

                /* Ground Heat Flux */
                /* Kustas et al 1993 */
                /* Kustas, W.P., Daughtry, C.S.T. van Oevelen P.J., 
                Analatytical Treatment of Relationships between Soil heat flux/net radiation and Vegetation Indices, 
                Remote sensing of environment,46:319-330 (1993) */
                G0[i] = (float) (Rn[i] * (((LST_K[i] - Constants.T0) / albedo[i]) * (0.0038f * albedo[i] + 0.0074 * albedo[i] * albedo[i]) * (1.0f - 0.98f * NDVI[i] * NDVI[i] * NDVI[i] * NDVI[i])));

                if (I_water || I_snow) {
                    G0[i] = 0.3f * Rn[i];
                }

                z0m[i] = (float) Math.exp(-5.809f + 5.62f * SAVI[i]);

                Ustar[i] = (float) (Constants.k * Uref[i] / Math.log(Constants.Zref / z0m[i]));

                r_ah[i] = (float) (Math.log(Constants.z2 / Constants.z1) / (Ustar[i] * Constants.k));

                H[i] = (float) (Constants.p * Constants.cp * (b + a * (LST_K[i] - Constants.T0)) / r_ah[i]);

                LE[i] = Rn[i] - H[i] - G0[i];

                /* Evaporative fraction */
                evap_fr[i] = 0.0f;
                if ((Rn[i] - G0[i]) != 0.0f) {
                    evap_fr[i] = LE[i] / (Rn[i] - G0[i]); /* evaporative fraction [] */
                } else {
                    evap_fr[i] = 1.0f; /* evaporative fraction upper limit [] (for negative available energy) */
                }

                Rn_24h[i] = Rg_24h[i] * (1.0f - albedo[i]) - 110 * Tao_24h[i];
                LE_24h[i] = evap_fr[i] * Rn_24h[i];
                ET_24h[i] = (evap_fr[i] * Rn_24h[i] * 86.4f) / 2450.0f;
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
}

