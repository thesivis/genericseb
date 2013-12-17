/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

import br.ufmt.genericlexerseb.ExpressionParser;
import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Utilities;

/**
 *
 * @author raphael
 */
public class GenericSEB {

    public static HashMap<String, String> equations = new HashMap<>();

    public boolean verifyEquations() {
        BufferedReader bur = null;
        try {
            String path = System.getProperty("user.dir") + "/source/landsat.prop";
            bur = new BufferedReader(new FileReader(path));
            String line = bur.readLine();
            List<String> variables = getVariables();

            ExpressionParser ex = new ExpressionParser();
            String vet[];

            while (line != null) {
                line = line.replaceAll("[ ]+", "");
                vet = line.split("=");
                try {
                    ex.evaluateExpr(line, variables);
                    equations.put(vet[0], line);
                } catch (IllegalArgumentException e) {
//                    System.out.println("Equation is wrong: " + line);
                    System.out.println(e.getMessage());
                }

                line = bur.readLine();
            }

        } catch (Exception ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bur.close();
            } catch (IOException ex) {
                Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public static List<String> getVariables() {
        List<String> variables = new ArrayList<>();

        variables.add("pi");
        variables.add("deg2rad");
        variables.add("kel2deg");
        variables.add("k");
        variables.add("Sigma_SB");
        variables.add("T0");
        variables.add("Rso");
        variables.add("g");
        variables.add("Rmax");
        variables.add("Rmin");
        variables.add("Rd");
        variables.add("Rv");
        variables.add("Cpw");
        variables.add("Cpd");
        variables.add("Cd");
        variables.add("Ct");
        variables.add("gammaConst");
        variables.add("Pr");
        variables.add("Pr_u");
        variables.add("Pr_s");
        variables.add("ri_i");
        variables.add("L_e");
        variables.add("rho_w");
        variables.add("PSI0");
        variables.add("DT");
        variables.add("MaxAllowedError");
        variables.add("dimx");
        variables.add("dimy");
        variables.add("SelectedDevice");
        variables.add("nThreadsPerBlock");
        variables.add("z200");
        variables.add("z2");
        variables.add("z1");
        variables.add("p");
        variables.add("cp");

        return variables;
    }

    public Map<String, double[]> execute(String header, String body, Map<String, double[]> parameters, Map<String, Double> constants) {
        return execute(header, body, parameters, constants, null, null);
    }

    public Map<String, double[]> execute(String header, String body, Map<String, double[]> parameters, Map<String, Double> constants, Map<String, double[]> constantsVetor, Map<String, double[][]> constantsMatrix) {

        Map<String, double[]> ret = new HashMap<>();
        List<String> variables = getVariables();
        ExpressionParser ex = new ExpressionParser();

        StringBuilder source = new StringBuilder();
        source.append("import static br.ufmt.genericseb.Constants.*;\n");
        source.append("import java.util.HashMap;\n");
        source.append("import java.util.Map;\n");
        source.append("import java.util.List;\n\n");

        source.append("public class Equation{\n");

        source.append("    public Map<String, double[]> execute(");
        int size = 0;
        String vet1 = null;
        for (String string : parameters.keySet()) {
            vet1 = string;
            source.append("double[] ").append(string);
            size++;
            if (size < parameters.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, double[]> ret = new HashMap<>();\n\n");

        for (String string : constants.keySet()) {
            source.append("        double ").append(string).append(" = ").append(constants.get(string)).append("f;\n");
            variables.add(string);
        }
        source.append("\n");

        StringBuilder vf;
        boolean albedo = false;
        for (String string : constantsVetor.keySet()) {
            double[] doubleVet = constantsVetor.get(string);
            vf = new StringBuilder();
            for (int i = 0; i < doubleVet.length - 1; i++) {
                vf.append(doubleVet[i]).append(",");
            }
            vf.append(doubleVet[doubleVet.length - 1]);
            source.append("        double[] ").append(string).append(" = new double[]{").append(vf.toString()).append("};\n");
            if (string.equals("parameterAlbedo")) {
                albedo = true;
            }
        }
        source.append("\n");

        for (String string : constantsMatrix.keySet()) {
            double[][] doubleVet = constantsMatrix.get(string);
            vf = new StringBuilder();
            for (int i = 0; i < doubleVet.length; i++) {
                vf.append("{");
                for (int j = 0; j < doubleVet[0].length - 1; j++) {
                    vf.append(doubleVet[i][j]).append(",");
                }
                vf.append(doubleVet[i][doubleVet[0].length - 1]);
                vf.append("}\n            ");
                if (i + 1 < doubleVet.length) {
                    vf.append(",");
                }
            }
            if (string.equals("calibration")) {
                variables.add("coef_calib_a");
                variables.add("coef_calib_b");
                variables.add("irrad_espectral");
            }
            source.append("        double[][] ").append(string).append(" = new double[][]{").append(vf.toString()).append("};\n");
        }
        source.append("\n");

        String[] vet = header.split("\n");
        GenericLexerSEB lexer = new GenericLexerSEB();
        Structure structure;

        //COLOCANDO AS FORMULAS DO CABECALHOS
        for (int i = 0; i < vet.length; i++) {
            structure = new Structure();
            structure.setToken(vet[i].split("=")[0]);
            source.append("        double ").append(lexer.analyse(vet[i], structure, null, LanguageType.JAVA)).append(";\n");
        }
        source.append("\n");

        //DECLARANDO OS VETORES QUE SERAO RETORNADOS
        vet = body.split("\n");
        String[] terms;
        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            if (terms[0].equals("rad_espectral")) {
                if (albedo) {
                    source.append("        double sumBandas = 0.0;\n");
                }
                for (int j = 1; j < 8; j++) {
                    source.append("        double[] banda").append(j).append(" = new double[").append(vet1).append(".length];\n");
                    source.append("        ret.put(\"banda").append(j).append("\",banda").append(j).append(");\n\n");
                }
            } else if (terms[0].equals("reflectancia")) {
                for (int j = 1; j < 8; j++) {
                    source.append("        double[] bandaRefletida").append(j).append(" = new double[").append(vet1).append(".length];\n");
                    source.append("        ret.put(\"bandaRefletida").append(j).append("\",bandaRefletida").append(j).append(");\n\n");
                }
            } else {
                source.append("        double[] ").append(terms[0]).append(" = new double[").append(vet1).append(".length];\n");
                source.append("        ret.put(\"").append(terms[0]).append("\",").append(terms[0]).append(");\n\n");
            }
        }

        source.append("        for(int i = 0;i < ").append(vet1).append(".length;i++){\n");

        String equation;
        String t;

        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            structure = new Structure();
            structure.setToken(terms[0]);
            equation = "            " + lexer.analyse(vet[i], structure, null, LanguageType.JAVA) + ";\n";
            for (int j = 0; j < i; j++) {
                t = vet[j].split("=")[0].replace(" ", "");
                ex.evaluateExpr(t);

                String[] out = ex.getOutput();
                for (int k = 0; k < out.length; k++) {
                    if (t.equals(out[k])) {
                        equation = equation.replace(t, t + "[i]");
                    }
                }
            }
            if (terms[0].equals("rad_espectral")) {

                for (int j = 1; j < 8; j++) {
                    equation = "            " + lexer.analyse(vet[i], structure, null, LanguageType.JAVA) + ";\n";
                    equation = equation.replace("coef_calib_a", "calibration[" + (j - 1) + "][0]");
                    equation = equation.replace("coef_calib_b", "calibration[" + (j - 1) + "][1]");
                    equation = equation.replace("pixel", "pixel" + (j) + "[i]");
                    equation = equation.replace("irrad_espectral", "calibration[" + (j - 1) + "][2]");

                    equation = equation.replace("rad_espectral", "banda" + j + "[i]");
                    source.append(equation);
                    variables.add("banda" + j);
                }
            } else if (terms[0].equals("reflectancia")) {
                for (int j = 1; j < 8; j++) {
                    equation = "            " + lexer.analyse(vet[i], structure, null, LanguageType.JAVA) + ";\n";
                    equation = equation.replace("coef_calib_a", "calibration[" + (j - 1) + "][0]");
                    equation = equation.replace("coef_calib_b", "calibration[" + (j - 1) + "][1]");
                    equation = equation.replace("pixel", "pixel" + (j) + "[i]");
                    equation = equation.replace("irrad_espectral", "calibration[" + (j - 1) + "][2]");
                    equation = equation.replace("reflectancia", "bandaRefletida" + j + "[i]");
                    equation = equation.replace("rad_espectral", "banda" + j + "[i]");
                    source.append(equation);
                    variables.add("bandaRefletida" + j);

                    if (albedo) {
                        source.append("            sumBandas += parameterAlbedo[").append(j - 1).append("]*bandaRefletida").append(j).append("[i];\n");
                    }
                }
            } else {
                equation = equation.replace(terms[0], terms[0] + "[i]");
                variables.add(terms[0]);
                source.append(equation);
            }
        }


        source.append("        }\n");
        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");

        System.out.println(source.toString());

        compile(source.toString(), "Equation");

        return ret;
    }

    public static Object compile(String source, String className) {
        try {
            PrintWriter fonte = new PrintWriter(className + ".java");
            fonte.println(source);
            fonte.close();

            int compilar = com.sun.tools.javac.Main.compile(new String[]{className + ".java"});
            File arq = new File(className + ".java");
            arq.delete();
            if (compilar == 0) {
                URL url = new URL("file:" + System.getProperty("user.dir") + "/");
                URLClassLoader ucl = URLClassLoader.newInstance(new URL[]{url});
                Class classe = ucl.loadClass(className);
                Object instancia = classe.newInstance();
                return instancia;
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) {
        // TODO code application logic here

        Map<String, double[]> parameters = new HashMap<>();
        parameters.put("pixel1", new double[]{1.0, 2.0});
        parameters.put("pixel2", new double[]{1.0, 2.0});
        parameters.put("pixel3", new double[]{1.0, 2.0});
        parameters.put("pixel4", new double[]{1.0, 2.0});
        parameters.put("pixel5", new double[]{1.0, 2.0});
        parameters.put("pixel6", new double[]{1.0, 2.0});
        parameters.put("pixel7", new double[]{1.0, 2.0});
        Map<String, Double> constants = new HashMap<>();
        constants.put("reflectancaAtmosfera", 0.03);
        constants.put("Kt", 1.0);
        constants.put("L", 0.1);
        constants.put("K1", 607.76);
        constants.put("K2", 1260.56);
        constants.put("S", 1367.0);
        constants.put("StefanBoltzman", (5.67 * Math.pow(10, -8)));
        constants.put("julianDay", 248.0);
        constants.put("Z", 50.24);
        constants.put("P", 99.3);
        constants.put("UR", 36.46);
        constants.put("Ta", 32.74);
        constants.put("latitude", -16.56);
        constants.put("Rg_24h", 243.949997);
        constants.put("Uref", 0.92071358);
        constants.put("Tao_24h", 0.63);

        Map<String, double[][]> constMatrix = new HashMap<>();

        constMatrix.put("calibration", new double[][]{
            {-1.52, 193.0, 1957.0},
            {-2.84, 365.0, 1826.0},
            {-1.17, 264.0, 1554.0},
            {-1.51, 221.0, 1036.0},
            {-0.37, 30.2, 215.0},
            {1.2378, 15.303, 1.0},
            {-0.15, 16.5, 80.67}});

        Map<String, double[]> constVetor = new HashMap<>();
        constVetor.put("parameterAlbedo", new double[]{0.293, 0.274, 0.233, 0.157, 0.033, 0.0, 0.011});

        String header = "dr = 1.0 + 0.033 * cos(julianDay * 2 * pi / 365)\n"
                + "cosZ = cos(((90.0 - Z) * pi) / 180.0)\n"
                + "declinacaoSolar = radians(23.45 * sin(radians(360.0 * (julianDay - 80) / 365)))\n"
                + "anguloHorarioNascerSol = acos(-tan(pi * latitude / 180.0) * tan(declinacaoSolar))\n"
                + "rad_solar_toa = 24.0 * 60.0 * 0.082 * dr * (anguloHorarioNascerSol * sin(pi * latitude / 180.0) * sin(declinacaoSolar) + cos(pi * latitude / 180.0) * cos(declinacaoSolar) * sin(anguloHorarioNascerSol)) / pi\n"
                + "Rg_24h_mj = 0.0864 * Rg_24h\n"
                + "transmissividade24h = Rg_24h_mj / rad_solar_toa\n"
                + "ea = (0.61078 * exp(17.269 * Ta / (237.3 + Ta))) * UR / 100\n"
                + "W = 0.14 * ea * P + 2.1\n"
                + "transmissividade = 0.35 + 0.627 * exp((-0.00146 * P / (Kt * cosZ)) - 0.075 * pow((W / cosZ), 0.4))\n"
                + "emissivityAtm = 0.625 * pow((1000.0 * ea / (Ta + T0)), 0.131)\n"
                + "SWd = (S * cosZ * cosZ) / (1.085 * cosZ + 10.0 * ea * (2.7 + cosZ) * 0.001 + 0.2)\n"
                + "LWdAtm = emissivityAtm * StefanBoltzman * (pow(Ta + T0, 4))";

        String body = "rad_espectral = coef_calib_a + ((coef_calib_b - coef_calib_a) / 255.0) * pixel\n"
                + "reflectancia = (pi * rad_espectral) / (irrad_espectral * cosZ * dr)\n"
                + "albedo = (sumBandas - reflectanciaAtmosfera) / (transmissividade * transmissividade)\n"
                + "NDVI = (bandaRefletida4 - bandaRefletida3) / (bandaRefletida4 + bandaRefletida3)\n"
                + "SAVI = ((1.0 + L) * (bandaRefletida4 - bandaRefletida3)) / (L + bandaRefletida4 + bandaRefletida3)\n"
                + "IAF = (-ln((0.69 - SAVI) / 0.59) / 0.91)\n"
                + "emissividadeNB = 0.97 + 0.0033 * IAF\n"
                + "emissivity = 0.95 + 0.01 * IAF\n"
                + "Ts = K2/ln(((emissividadeNB * K1) / banda6) + 1.0)\n"
                + "LWd = emissivity * StefanBoltzman * (pow(Ts, 4))\n"
                + "Rn = ((1.0 - albedo) * SWd) + (emissivity * (LWdAtm) - LWd)";
        GenericSEB g = new GenericSEB();
        g.execute(header, body, parameters, constants, constVetor, constMatrix);
    }
}
