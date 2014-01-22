/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import static br.ufmt.genericsebalgpu.sebal.Sebal.equations;
import br.ufmt.jseriesgpu.JSeriesCUDA;
import br.ufmt.jseriesgpu.ParameterGPU;
import br.ufmt.preprocessing.utils.ParameterEnum;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raphael
 */
public class SebalCUDA extends SebalGPU {

    private String SEBAL_EnergyBalance_Kernel = "SEBAL_EnergyBalance.cu";
    private String SEBAL_EnergyBalance_Kernel_Modify = "SEBAL_EnergyBalance_Modify.cu";
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
            
            
            BufferedReader bur = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/source/" + SEBAL_EnergyBalance_Kernel));
            String linha = bur.readLine();
            StringBuilder codigo = new StringBuilder(linha + "\n");
            GenericLexerSEB lexer = new GenericLexerSEB();
            Map<String, Structure> mapStructure = new HashMap<>();
            while (linha != null) {
                if (linha.contains("equation:")) {
                    String[] vet = linha.split(":");
                    Structure structure = new Structure();
                    if (vet[1].equals("G02")) {
                        structure.setToken("G0");
                    } else {
                        structure.setToken(vet[1]);
                    }
                    if (vet.length > 2) {
                        structure.setVector(true);
                        structure.setIndex(vet[2]);
                    }
                    linha = lexer.analyse(equations.get(vet[1]), structure, mapStructure, LanguageType.CUDA) + ";";
                    mapStructure.put(structure.getToken(), structure);
                }
                codigo.append(linha + "\n");
                linha = bur.readLine();
            }
            
            System.out.println(codigo.toString());
            PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/source/" + SEBAL_EnergyBalance_Kernel_Modify);
            pw.println(codigo.toString());
            pw.close();
            
            cuda.execute(par, System.getProperty("user.dir") + "/source/" + SEBAL_EnergyBalance_Kernel_Modify, "SEBAL_EnergyBalance_Kernel");
            
            File newFile = new File(System.getProperty("user.dir") + "/source/" + SEBAL_EnergyBalance_Kernel_Modify);
            newFile.delete();
            
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
