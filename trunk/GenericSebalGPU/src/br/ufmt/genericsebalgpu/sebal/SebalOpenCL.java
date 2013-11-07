/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import br.ufmt.jseriesgpu.JSeriesCL;
import br.ufmt.jseriesgpu.ParameterGPU;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raphael
 */
public class SebalOpenCL extends SebalGPU {

    private String SEBAL_EnergyBalance_Kernel = "SEBAL_EnergyBalance.cl";

    @Override
    protected void calcularGPU(List<ParameterGPU> par) {
        try {
            JSeriesCL openCL = new JSeriesCL();
//            openCL.setPrint(true);
            openCL.setMeasure(true);

            BufferedReader bur = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/source/" + SEBAL_EnergyBalance_Kernel));
            String linha = bur.readLine();
            StringBuilder codigo = new StringBuilder(linha + "\n");
            while (linha != null) {
                codigo.append(linha + "\n");
                linha = bur.readLine();
            }
            openCL.execute(par, codigo.toString(), "SEBAL_EnergyBalance_Kernel");

            long tempo = openCL.getMeasures().get(4).getDiference().getTime();

            System.out.println("Tempo:" + tempo);
//            System.exit(1);

        } catch (IOException ex) {
            Logger.getLogger(SebalOpenCL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
