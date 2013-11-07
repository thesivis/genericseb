/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.sebal;

import br.ufmt.jseriesgpu.JSeriesCUDA;
import br.ufmt.jseriesgpu.ParameterGPU;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raphael
 */
public class SebalCUDA extends SebalGPU {

    private String SEBAL_EnergyBalance_Kernel = "SEBAL_EnergyBalance.cu";
    private String option;
    private String pathNvcc = "/usr/local/cuda/bin/";

    @Override
    protected void calcularGPU(List<ParameterGPU> par) {
        JSeriesCUDA cuda = new JSeriesCUDA();
        cuda.setPathNvcc(pathNvcc);
        if (option != null) {
            cuda.setCompileOptions(option);
        }

        cuda.setPrint(true);
        cuda.setMeasure(true);

//        try {
//            PrintWriter pw = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/OutputSebal/TempoCuda20.dat", true));
//            for (int i = 32; i >= 1; i--) {

        try {
//            if (option != null && option.equals(" -arch=sm_20 ")) {
//                cuda.setThreads(20);
//            } else {
//                cuda.setThreads(24);
//            }
            cuda.execute(par, System.getProperty("user.dir") + "/source/" + SEBAL_EnergyBalance_Kernel, "SEBAL_EnergyBalance_Kernel");
        } catch (IOException ex) {
            Logger.getLogger(SebalCUDA.class.getName()).log(Level.SEVERE, null, ex);
        }

        long tempo = cuda.getMeasures().get(4).getDiference().getTime();
        System.out.println("Tempo:" + tempo);
//                System.exit(1);
//                return ;
//                pw.println(tempo);
//            }
//            pw.close();
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(SebalCUDA.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.exit(1);

    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
