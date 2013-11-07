/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;


import br.ufmt.genericsebalgpu.utils.Constants;
import br.ufmt.genericsebalgpu.utils.DataStructure;
import br.ufmt.genericsebalgpu.utils.ParameterEnum;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author raphael
 */
public abstract class Sebal {

    protected long tempo;

    public List<DataStructure> executar(float a, float b, float[] SWd, float[] LWd, float[] Albedo, float[] Emissivity, float[] NDVI, float[] LST_K, float[] Uref, float[] SAVI, float[] Rg_24h, float[] Tao_24h) {

        int[] comptMask = new int[NDVI.length];
        for (int i = 0; i < comptMask.length; i++) {
            if (LST_K[i] == 0 && Emissivity[i] == 0 && NDVI[i] == 0 && SAVI[i] == 0) {
                comptMask[i] = 0;
            } else {
                comptMask[i] = 1;
            }
        }

        int NData = NDVI.length;

        float[] Rn, G0, H, LE, EF, z0m, Ustar, r_ah, Rn_24h, LE_24h, ET_24h;

        z0m = new float[NData];
        Ustar = new float[NData];
        r_ah = new float[NData];
        Rn = new float[NData];
        G0 = new float[NData];
        H = new float[NData];
        LE = new float[NData];
        EF = new float[NData];
        Rn_24h = new float[NData];
        LE_24h = new float[NData];
        ET_24h = new float[NData];

        calcular(comptMask, SWd, LWd, Albedo, Emissivity, LST_K, NDVI, Uref, SAVI, a, b, Rg_24h, Tao_24h, z0m, Ustar, r_ah, Rn, G0, H, LE, EF, Rn_24h, LE_24h, ET_24h, NData);

        List<DataStructure> outputs = new ArrayList<DataStructure>();
        outputs.add(new DataStructure(ParameterEnum.z0m, z0m));
        outputs.add(new DataStructure(ParameterEnum.ustar, Ustar));
        outputs.add(new DataStructure(ParameterEnum.R_AH, r_ah));
        outputs.add(new DataStructure(ParameterEnum.Rn, Rn));
        outputs.add(new DataStructure(ParameterEnum.G0, G0));
        outputs.add(new DataStructure(ParameterEnum.H, H));
        outputs.add(new DataStructure(ParameterEnum.LE, LE));
        outputs.add(new DataStructure(ParameterEnum.EF, EF));
        outputs.add(new DataStructure(ParameterEnum.Rn_24h, Rn_24h));
        outputs.add(new DataStructure(ParameterEnum.LE_24h, LE_24h));
        outputs.add(new DataStructure(ParameterEnum.ET_24h, ET_24h));

        return outputs;
    }

    protected abstract void calcular(int[] comptMask,
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
            int DataSize);

    public static void calculaAB(float[] coeficientes, float Rn_hot, float G_hot, float Uref, float SAVI_hot, float Ts_hot, float Ts_cold) {

        float z0m = (float) Math.exp(-5.809f + 5.62f * SAVI_hot);

        float U_star = (float) (Constants.k * Uref / Math.log(Constants.Zref / z0m));

        float r_ah = (float) (Math.log(Constants.z2 / Constants.z1) / (U_star * Constants.k));

        float H_hot = Rn_hot - G_hot;

        float a = 0.0f;
        float b = 0.0f;

        float L;

        float tm_200;
        float th_2;
        float th_0_1;

        float errorH = 10.0f;
        int step = 1;
        float r_ah_anterior;
        float H = H_hot;

        while (errorH > Constants.MaxAllowedError && step < 100) {

            a = ((H) * r_ah) / (Constants.p * Constants.cp * (Ts_hot - Ts_cold));
            b = -a * (Ts_cold - Constants.T0);


            H = Constants.p * Constants.cp * (b + a * (Ts_hot - Constants.T0)) / r_ah;

            L = (float) (-(Constants.p * Constants.cp * U_star * U_star * U_star * (Ts_hot)) / (Constants.k * Constants.g * H));

            tm_200 = Psim(L);
            th_2 = Psih(Constants.z2, L);
            th_0_1 = Psih(Constants.z1, L);

            U_star = (float) (Constants.k * Uref / (Math.log(Constants.Zref / z0m) - tm_200));
            r_ah_anterior = r_ah;
            r_ah = (float) ((Math.log(Constants.z2 / Constants.z1) - th_2 + th_0_1) / (U_star * Constants.k));

            errorH = Math.abs(((r_ah - r_ah_anterior) * 100) / r_ah);

            step++;
        }

//        System.out.println("Total de Interações:" + step);
        coeficientes[0] = a;
        coeficientes[1] = b;

    }

    protected static float X(float Zref_m, float L) {
        return (float) (Math.sqrt(Math.sqrt((1.0f - 16.0f * Zref_m / L))));
    }

    protected static float Psim(float L) {
        if (L < 0.0f) {
            /* unstable */
            float x200 = X(200, L);
            return (float) (2.0f * Math.log((1.0f + x200) / 2.0f) + Math.log((1.0f + x200 * x200) / (2.0f)) - 2.0f * Math.atan(x200) + 0.5f * Math.PI);
        } else if (L > 0.0f) {
            /* stable */
            return (-5 * (2 / L));
        } else {
            return (0);
        }
    }

    protected static float Psih(float Zref_h, float L) {
        if (L < 0.0f) {
            /* unstable */
            float x = X(Zref_h, L);
            return (float) (2.0f * Math.log((1.0f + x * x) / 2.0f));
        } else if (L > 0.0f) {
            /* stable */
            return (-5 * (2 / L));
        } else {
            return (0);
        }
    }
}

