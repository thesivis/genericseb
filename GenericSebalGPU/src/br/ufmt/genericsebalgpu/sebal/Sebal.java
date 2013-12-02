/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;



import br.ufmt.preprocessing.utils.DataStructure;
import br.ufmt.preprocessing.utils.ParameterEnum;
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

}

