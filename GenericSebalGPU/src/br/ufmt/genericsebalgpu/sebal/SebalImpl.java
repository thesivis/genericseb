/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import br.ufmt.genericlexerseb.Variable;
import static br.ufmt.preprocessing.utils.Constants.*;
import br.ufmt.preprocessing.utils.ParameterEnum;
import br.ufmt.preprocessing.utils.Utilities;
import java.util.List;

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

        GenericLexerSEB lexer = new GenericLexerSEB();
        Structure structure = new Structure();
        structure.setToken("z0m");
        String equation = lexer.analyse(equations.get(ParameterEnum.z0m), structure, null, LanguageType.PYTHON);
        List<Variable> variables = Utilities.getVariable();
        variables.add(new Variable("SWd", SWd));
        variables.add(new Variable("LWd", LWd));
        variables.add(new Variable("albedo", albedo));
        variables.add(new Variable("emissivity", emissivity));
        variables.add(new Variable("LST_K", LST_K));
        variables.add(new Variable("NDVI", NDVI));
        variables.add(new Variable("Uref", Uref));
        variables.add(new Variable("SAVI", SAVI));
        variables.add(new Variable("a", a));
        variables.add(new Variable("b", b));
        variables.add(new Variable("Rg_24h", Rg_24h));
        variables.add(new Variable("Tao_24h", Tao_24h));

        z0m[idx] = (float) lexer.getResult(equation, variables);
        variables.add(new Variable("z0m", z0m[idx]));


        /* Classification */
        boolean I_snow = (NDVI < 0.0f) && (albedo > 0.47f);
        boolean I_water = (NDVI == -1.0f);

        /*	% NOTE: esat_WL is only used for the wet-limit. To get a true upperlimit for the sensible heat
         % the Landsurface Temperature is used as a proxy instead of air temperature.
         %% Net Radiation */

        structure = new Structure();
        structure.setToken("SWnet");
        equation = lexer.analyse(equations.get(ParameterEnum.SWnet), structure, null, LanguageType.PYTHON);

        float SWnet = (float) lexer.getResult(equation, variables);  /* Shortwave Net Radiation [W/m2] */
        variables.add(new Variable("SWnet", SWnet));
        
        
        structure = new Structure();
        structure.setToken("LWnet");
        equation = lexer.analyse(equations.get(ParameterEnum.LWnet), structure, null, LanguageType.PYTHON);

        float LWnet = (float) lexer.getResult(equation, variables);  /* Longwave Net Radiation [W/m2] */
        variables.add(new Variable("LWnet", LWnet));

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
