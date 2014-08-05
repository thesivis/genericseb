/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing;

import br.ufmt.preprocessing.utils.DataFile;
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
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // TODO code application logic here
//        ExpressionParser expressionParser = new ExpressionParser();
//        expressionParser.evaluateExpr("transmissividade = 0.35 + 0.627 * exp((-0.00146 * P / (Kt * cosZ)) - 0.075 * pow((W / cosZ), 0.4))");
//        String[] terms = expressionParser.getOutput();
//        System.out.println(Arrays.toString(terms));
//        Map<String,Variable> sets = new HashMap<>();
//        sets.put("julianDay",new Variable("julianDay", 123));
//        sets.put("julianDay",new Variable("julianDay", 321));
////        List<Variable> variables = new ArrayList<>();
////        variables.add(new Variable("julianDay", 123));
////        variables.add(new Variable("julianDay", 321));
//        
//        for (Variable variable : sets.values()) {
//            System.out.println(variable.getValue());
//        }
//
//        System.exit(1);
        LandSat land = new LandSat();
        String path = "/home/raphael/Google/GenericGUI/Teste/landsat_5_tm_20070326_226_072_l2_empilhada_orto.tif";
        path = "/home/raphael/Google/GenericGUI/Teste/teste.tif";
        int julianDay = 157;
        float Z = 39.8911f;
        float P = 99.3f;
        float UR = 68.59f;
        float Ta = 20.53f;
        float latitude = -16.56f;
        float Rg_24h = 181.613186f;
        float Uref = 2.24f;
        float Tao_24h = 0.599304834f;
        StringBuilder equations = new StringBuilder();

        try {
            BufferedReader bur = new BufferedReader(new FileReader("/home/raphael/Google/GenericGUI/Teste/index.prop"));
            String linha = bur.readLine();
            while (linha != null) {
                equations.append(linha).append("\n");
                linha = bur.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        long time = System.currentTimeMillis();

//        double[][] pixel = new double[7][53931915];
        List<DataFile> datas2 = land.preprocessingLandSat5(path, equations.toString(), julianDay, Z, P, UR, Ta, latitude, Rg_24h, Uref, Tao_24h);
        time = System.currentTimeMillis() - time;
        System.out.println("Tempo:" + time);
    }
}
