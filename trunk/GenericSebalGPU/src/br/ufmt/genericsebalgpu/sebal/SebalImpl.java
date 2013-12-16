/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import br.ufmt.genericlexerseb.Variable;
import br.ufmt.preprocessing.utils.ParameterEnum;
import br.ufmt.preprocessing.utils.Utilities;
import java.util.Map;

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
        String equation = lexer.analyse(equations.get(ParameterEnum.z0m), structure, null, LanguageType.JAVA);
        Map<String,Variable> variables = Utilities.getVariable();
        variables.put("SWd",new Variable("SWd", SWd));
        variables.put("LWd",new Variable("LWd", LWd));
        variables.put("albedo",new Variable("albedo", albedo));
        variables.put("emissivity",new Variable("emissivity", emissivity));
        variables.put("LST_K",new Variable("LST_K", LST_K));
        variables.put("NDVI",new Variable("NDVI", NDVI));
        variables.put("Uref",new Variable("Uref", Uref));
        variables.put("SAVI",new Variable("SAVI", SAVI));
        variables.put("a",new Variable("a", a));
        variables.put("b",new Variable("b", b));
        variables.put("Rg_24h",new Variable("Rg_24h", Rg_24h));
        variables.put("Tao_24h",new Variable("Tao_24h", Tao_24h));

        //z0m
        z0m[idx] = (float) lexer.getResult(equation, variables);
        variables.put("z0m",new Variable("z0m", z0m[idx]));


        /* Classification */
        boolean I_snow = (NDVI < 0.0f) && (albedo > 0.47f);
        boolean I_water = (NDVI == -1.0f);

        /*	% NOTE: esat_WL is only used for the wet-limit. To get a true upperlimit for the sensible heat
         % the Landsurface Temperature is used as a proxy instead of air temperature.
         %% Net Radiation */

        //SWnet
        structure = new Structure();
        structure.setToken("SWnet");
        equation = lexer.analyse(equations.get(ParameterEnum.SWnet), structure, null, LanguageType.JAVA);

        float SWnet = (float) lexer.getResult(equation, variables);  /* Shortwave Net Radiation [W/m2] */
        variables.put("SWnet",new Variable("SWnet", SWnet));

        //LWnet
        structure = new Structure();
        structure.setToken("LWnet");
        equation = lexer.analyse(equations.get(ParameterEnum.LWnet), structure, null, LanguageType.JAVA);

        float LWnet = (float) lexer.getResult(equation, variables);  /* Longwave Net Radiation [W/m2] */
        variables.put("LWnet",new Variable("LWnet", LWnet));

        //Rn
        structure = new Structure();
        structure.setToken("Rn");
        equation = lexer.analyse(equations.get(ParameterEnum.Rn), structure, null, LanguageType.JAVA);

        Rn[idx] = (float) lexer.getResult(equation, variables); /* Total Net Radiation [W/m2] */
        variables.put("Rn",new Variable("Rn", Rn[idx]));

        /* Ground Heat Flux */
        /* Kustas et al 1993 */
        /* Kustas, W.P., Daughtry, C.S.T. van Oevelen P.J., 
         Analatytical Treatment of Relationships between Soil heat flux/net radiation and Vegetation Indices, 
         Remote sensing of environment,46:319-330 (1993) */
        //G0
        structure = new Structure();
        structure.setToken("G0");
        equation = lexer.analyse(equations.get(ParameterEnum.G0), structure, null, LanguageType.JAVA);

        G0[idx] = (float) lexer.getResult(equation, variables); /* Total Net Radiation [W/m2] */
        variables.put("G0",new Variable("G0", G0[idx]));

        if (I_water || I_snow) {
            structure = new Structure();
            structure.setToken("G0");
            equation = lexer.analyse(equations.get(ParameterEnum.G02), structure, null, LanguageType.JAVA);

            G0[idx] = (float) lexer.getResult(equation, variables); /* Total Net Radiation [W/m2] */
            variables.put("G0",new Variable("G0", G0[idx]));
        }
        //U_star
        structure = new Structure();
        structure.setToken("U_star");
        equation = lexer.analyse(equations.get(ParameterEnum.U_star), structure, null, LanguageType.JAVA);

        U_star[idx] = (float) lexer.getResult(equation, variables);
        variables.put("U_star",new Variable("U_star", U_star[idx]));

        //r_ah
        structure = new Structure();
        structure.setToken("r_ah");
        equation = lexer.analyse(equations.get(ParameterEnum.r_ah), structure, null, LanguageType.JAVA);

        r_ah[idx] = (float) lexer.getResult(equation, variables);
        variables.put("r_ah",new Variable("r_ah", r_ah[idx]));

        //H
        structure = new Structure();
        structure.setToken("H");
        equation = lexer.analyse(equations.get(ParameterEnum.H), structure, null, LanguageType.JAVA);

        H[idx] = (float) lexer.getResult(equation, variables);
        variables.put("H",new Variable("H", H[idx]));

        //LE
        structure = new Structure();
        structure.setToken("LE");
        equation = lexer.analyse(equations.get(ParameterEnum.LE), structure, null, LanguageType.JAVA);

        LE[idx] = (float) lexer.getResult(equation, variables);
        variables.put("LE",new Variable("LE", LE[idx]));

        /* Evaporative fraction */
        evap_fr[idx] = 0.0f;
        if ((Rn[idx] - G0[idx]) != 0.0f) {
            //evap_fr
            structure = new Structure();
            structure.setToken("evap_fr");
            equation = lexer.analyse(equations.get(ParameterEnum.evap_fr), structure, null, LanguageType.JAVA);/* evaporative fraction [] */

            evap_fr[idx] = (float) lexer.getResult(equation, variables);
            variables.put("evap_fr",new Variable("evap_fr", evap_fr[idx]));

        } else {
            evap_fr[idx] = 1.0f; /* evaporative fraction upper limit [] (for negative available energy) */

        }

        //Rn_24h
        structure = new Structure();
        structure.setToken("Rn_24h");
        equation = lexer.analyse(equations.get(ParameterEnum.Rn_24h), structure, null, LanguageType.JAVA);/* evaporative fraction [] */

        Rn_24h[idx] = (float) lexer.getResult(equation, variables);
        variables.put("Rn_24h",new Variable("Rn_24h", Rn_24h[idx]));


        //LE_24h
        structure = new Structure();
        structure.setToken("LE_24h");
        equation = lexer.analyse(equations.get(ParameterEnum.LE_24h), structure, null, LanguageType.JAVA);/* evaporative fraction [] */

        LE_24h[idx] = (float) lexer.getResult(equation, variables);
        variables.put("LE_24h",new Variable("LE_24h", LE_24h[idx]));

        //ET_24h
        structure = new Structure();
        structure.setToken("ET_24h");
        equation = lexer.analyse(equations.get(ParameterEnum.ET_24h), structure, null, LanguageType.JAVA);/* evaporative fraction [] */

        ET_24h[idx] = (float) lexer.getResult(equation, variables);
        variables.put("ET_24h",new Variable("ET_24h", ET_24h[idx]));
    }
}
