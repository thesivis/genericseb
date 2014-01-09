/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing;

import br.ufmt.preprocessing.utils.DataFile;
import java.util.List;

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
        String path = "/media/raphael/DISK/Faculdade/Doutorado/Artigos/EnviromentModelingSoftware/GPUSensoriamento/TIFF/rppn.tif";
        int julianDay = 248;
        float Z = 50.24f;
        float P = 99.3f;
        float UR = 36.46f;
        float Ta = 32.74f;
        float latitude = -16.56f;
        float Rg_24h = 243.949997f;
        float Uref = 0.92071358f;
        float Tao_24h = 0.63f;

        List<DataFile> datas2 = land.preprocessingLandSat5(path, julianDay, Z, P, UR, Ta, latitude, Rg_24h, Uref);
    }
}
