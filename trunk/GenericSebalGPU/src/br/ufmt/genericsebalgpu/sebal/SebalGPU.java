/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import br.ufmt.jseriesgpu.ParameterGPU;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author raphael
 */
public abstract class SebalGPU extends Sebal {

    @Override
    protected void calcular(int[] comptMask, float[] SWd, float[] LWd, float[] albedo, float[] emissivity, float[] LST_K, float[] NDVI, float[] Uref, float[] SAVI, float a, float b, float[] Rg_24h, float[] Tao_24h, float[] z0m, float[] Ustar, float[] r_ah, float[] Rn, float[] G0, float[] H, float[] LE, float[] evap_fr, float[] Rn_24h, float[] LE_24h, float[] ET_24h, int DataSize) {

        ParameterGPU aux;
        List<ParameterGPU> par = new ArrayList<ParameterGPU>();

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataInt(comptMask);
        aux.setDefineThreads(true);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(SWd);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(LWd);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(albedo);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(emissivity);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(LST_K);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(NDVI);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(Uref);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(SAVI);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(new float[]{a});
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(new float[]{b});
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(Rg_24h);
        par.add(aux);

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataFloat(Tao_24h);
        par.add(aux);

        /* SAIDAS */
        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setWrite(true);
        aux.setDataFloat(z0m);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(Ustar);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(r_ah);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setWrite(true);
        aux.setDataFloat(Rn);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(G0);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(H);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(LE);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(evap_fr);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(Rn_24h);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(LE_24h);
        aux.setWrite(true);
        par.add(aux);

        aux = new ParameterGPU();
//            aux.setRead(true);
        aux.setDataFloat(ET_24h);
        aux.setWrite(true);
        par.add(aux);
        /* FIM SAIDAS */

        int[] N = new int[]{DataSize};

        aux = new ParameterGPU();
        aux.setRead(true);
        aux.setDataInt(N);
        par.add(aux);

//        System.out.println("Calculando na GPU");
//        for (int i = 0; i < 20; i++) {
            calcularGPU(par);
//        }
//        System.out.println("Fim do Calculo na GPU");

    }

    protected abstract void calcularGPU(List<ParameterGPU> par);
}
