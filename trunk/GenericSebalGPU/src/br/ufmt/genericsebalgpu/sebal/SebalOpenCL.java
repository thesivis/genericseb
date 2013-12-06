/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import br.ufmt.jseriesgpu.JSeriesCL;
import br.ufmt.jseriesgpu.ParameterGPU;
import br.ufmt.preprocessing.utils.ParameterEnum;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    linha = lexer.analyse(equations.get(ParameterEnum.valueOf(vet[1])), structure, mapStructure, LanguageType.OPENCL) + ";";
                    mapStructure.put(structure.getToken(), structure);
                }
                codigo.append(linha + "\n");
                linha = bur.readLine();
            }
//            System.exit(1);
            openCL.execute(par, codigo.toString(), "SEBAL_EnergyBalance_Kernel");

            long tempo = openCL.getMeasures().get(4).getDiference().getTime();

            System.out.println("Tempo:" + tempo);
//            System.exit(1);

        } catch (IOException ex) {
            Logger.getLogger(SebalOpenCL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
