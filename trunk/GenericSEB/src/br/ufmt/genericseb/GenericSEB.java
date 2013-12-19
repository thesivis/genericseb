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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
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

    private LanguageType language;
    private Object[] pars;
    private Class[] classes;

    public GenericSEB(LanguageType language) {
        this.language = language;
    }

    public GenericSEB() {
        this(LanguageType.JAVA);
    }

    public boolean verifyEquations(String forms, List<String> variables) {

        ExpressionParser ex = new ExpressionParser();
        String vet[];
        String vets[] = forms.split("\n");

        String line;
        for (int i = 0; i < vets.length; i++) {
            line = vets[i];

//            System.out.println("Line:" + line);
            line = line.replaceAll("[ ]+", "");
            vet = line.split("=");
            if (vet[0].startsWith("O_")) {
                vet[0] = vet[0].substring(2);
                line = vet[0] + "=" + vet[1];
            }
            variables.add(vet[0]);
            try {
                ex.evaluateExpr(line, variables);

            } catch (IllegalArgumentException e) {
//                    System.out.println("Equation is wrong: " + line);
                e.printStackTrace();
                System.exit(1);
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
        String source = null;

        if (language.equals(LanguageType.CUDA_FLOAT) || language.equals(LanguageType.CUDA_DOUBLE)) {
            source = generateCUDA(header, body, parameters, constants, constantsVetor, constantsMatrix);
        } else if (language.equals(LanguageType.OPENCL)) {
            source = generateOpenCL(header, body, parameters, constants, constantsVetor, constantsMatrix);
        } else {
            source = generateJava(header, body, parameters, constants, constantsVetor, constantsMatrix);
        }

//        System.out.println(source);
//        System.exit(1);

        Object instanced = compile(source, "Equation");
        try {
            Method method = instanced.getClass().getDeclaredMethod("execute", classes);
            ret = (Map<String, double[]>) method.invoke(instanced, pars);
        } catch (NoSuchMethodException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (SecurityException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (IllegalAccessException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (IllegalArgumentException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (InvocationTargetException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        }


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

    private String generateCUDA(String header, String body, Map<String, double[]> parameters, Map<String, Double> constants, Map<String, double[]> constantsVetor, Map<String, double[][]> constantsMatrix) {

        List<String> variables = getVariables();
        ExpressionParser ex = new ExpressionParser();

        StringBuilder source = new StringBuilder();
        source.append("import static br.ufmt.genericseb.Constants.*;\n");
        source.append("import java.util.HashMap;\n");
        source.append("import java.util.Map;\n");
        source.append("import br.ufmt.jseriesgpu.ParameterGPU;\n");
        source.append("import java.util.ArrayList;\n");
        source.append("import br.ufmt.jseriesgpu.JSeriesCUDA;\n");
        source.append("import java.io.File;\n");
        source.append("import java.io.IOException;\n");
        source.append("import java.util.logging.Level;\n");
        source.append("import java.util.logging.Logger;\n");
        source.append("import java.util.List;\n\n");

        source.append("public class Equation{\n");

        source.append("    public Map<String, double[]> execute(");
        int size = 0;
        String vet1 = null;
        pars = new Object[parameters.size()];
        classes = new Class[parameters.size()];
        for (String string : parameters.keySet()) {
            vet1 = string;
            pars[size] = parameters.get(string);
            classes[size] = parameters.get(string).getClass();
            source.append("double[] ").append(string);
            size++;
            if (size < parameters.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, double[]> ret = new HashMap<>();\n\n");
        source.append("        List<ParameterGPU> par = new ArrayList<ParameterGPU>();\n\n");

        boolean first = true;
        for (String string : parameters.keySet()) {
            if (first) {
                source.append("        int[] N = new int[]{").append(string).append(".length};\n\n");
                source.append("        par.add(new ParameterGPU(").append(string).append(",true,false,true));\n");
                first = false;
            } else {
                source.append("        par.add(new ParameterGPU(").append(string).append(",true));\n");
            }
        }

        source.append("\n");

        //DECLARANDO OS VETORES QUE SERAO RETORNADOS
        String[] vet = body.split("\n");
        String[] terms;
        String term;
        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    variables.add("pixel");
                    for (int j = 1; j < 8; j++) {
                        if (terms[0].startsWith("O_")) {
                            source.append("        double[] banda").append(j).append(" = new double[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(banda").append(j).append(",true,true));\n");
                            source.append("        ret.put(\"banda").append(j).append("\",banda").append(j).append(");\n\n");
                        }
                        variables.add("banda" + j);
                    }
                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 1; j < 8; j++) {
                        variables.add("bandaRefletida" + j);
                        if (terms[0].startsWith("O_")) {
                            source.append("        double[] bandaRefletida").append(j).append(" = new double[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(bandaRefletida").append(j).append(",true,true));\n");
                            source.append("        ret.put(\"bandaRefletida").append(j).append("\",bandaRefletida").append(j).append(");\n\n");
                        }
                    }
                    break;
                default:
                    if (terms[0].startsWith("O_")) {
                        term = terms[0].substring(2);
                        source.append("        double[] ").append(term).append(" = new double[").append(vet1).append(".length];\n");
                        source.append("        par.add(new ParameterGPU(").append(term).append(",true,true));\n");
                        source.append("        ret.put(\"").append(term).append("\",").append(term).append(");\n\n");
                    }
                    break;
            }
        }

        source.append("        par.add(new ParameterGPU(N,true));\n\n");

        source.append("        String pathNvcc = \"/usr/local/cuda/bin/\";\n");
        source.append("        String source = \"code.cu\";\n");
        source.append("        try {\n");
        source.append("            JSeriesCUDA cuda = new JSeriesCUDA();\n");
        source.append("            cuda.setPathNvcc(pathNvcc);\n");
        source.append("            cuda.setPrint(true);\n");
        source.append("            cuda.setMeasure(true);\n");
        source.append("            cuda.execute(par, System.getProperty(\"user.dir\") + \"/source/\" + source, \"execute\");\n");
        source.append("            File newFile = new File(System.getProperty(\"user.dir\") + \"/source/\" + source);\n");
        source.append("            //newFile.delete();\n");
        source.append("        } catch (IOException ex) {\n");
        source.append("            Logger.getLogger(Equation.class.getName()).log(Level.SEVERE, null, ex);\n");
        source.append("        }\n");


        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");

        //gpu part

        StringBuilder gpuCode = new StringBuilder("#include \"Constants.h\"\n\n");
        gpuCode.append("extern \"C\"{\n\n");

        StringBuilder gpuCodeBody = new StringBuilder("    __global__ void execute(\n");

        if (constants != null) {
            for (String string : constants.keySet()) {
                gpuCode.append("    #define ").append(string).append(" ").append(constants.get(string)).append("f\n");
                variables.add(string);
            }
        }
        gpuCode.append("\n");

        vet = header.split("\n");
        GenericLexerSEB lexer = new GenericLexerSEB();
        Structure structure;
        Map<String, Double> vars = new HashMap<>();
        vars.putAll(constants);
        vars.putAll(Constants.variables);

        //COLOCANDO AS FORMULAS DO CABECALHOS
        double res;
        for (int i = 0; i < vet.length; i++) {
            structure = new Structure();
            structure.setToken(vet[i].split("=")[0].replace(" ", ""));
            String equation = lexer.analyse(vet[i], structure, null, LanguageType.PYTHON);
            res = lexer.getResults(equation, vars);
            vars.put(structure.getToken(), res);
            gpuCode.append("    #define ").append(structure.getToken()).append(" ").append(res).append("f\n");
            variables.add(structure.getToken());
        }
        gpuCode.append("\n");


        StringBuilder vf;
        boolean albedo = false;
        if (constantsVetor != null) {
            for (String string : constantsVetor.keySet()) {
                double[] doubleVet = constantsVetor.get(string);
                vf = new StringBuilder();
                for (int i = 0; i < doubleVet.length - 1; i++) {
                    vf.append(doubleVet[i]).append("f,");
                }
                vf.append(doubleVet[doubleVet.length - 1] + "f");
                gpuCode.append("    __constant__ double ").append(string).append("[] = {").append(vf.toString()).append("};\n");
                if (string.equals("parameterAlbedo")) {
                    albedo = true;
                }
            }
        }
        gpuCode.append("\n");

        if (constantsMatrix != null) {
            for (String string : constantsMatrix.keySet()) {
                double[][] doubleVet = constantsMatrix.get(string);
                for (int i = 0; i < doubleVet.length; i++) {
                    vf = new StringBuilder();
                    vf.append("{");
                    gpuCode.append("    __constant__ double ").append(string + (i + 1)).append("[] = ");
                    for (int j = 0; j < doubleVet[0].length - 1; j++) {
                        vf.append(doubleVet[i][j]).append(",");
                    }
                    vf.append(doubleVet[i][doubleVet[0].length - 1]);
                    vf.append("};\n");
                    gpuCode.append(vf.toString());
                }
                if (string.equals("calibration")) {
                    variables.add("coef_calib_a");
                    variables.add("coef_calib_b");
                    variables.add("irrad_espectral");
                }

            }
        }
        gpuCode.append("\n");


        gpuCode.append("    __device__ void execute_sub(\n");
        for (String string : parameters.keySet()) {
            gpuCode.append("        double ").append(string).append(",\n");
            gpuCodeBody.append("        double * ").append(string).append(",\n");
            variables.add(string);
        }
        gpuCode.append("\n");
        gpuCodeBody.append("\n");

        vet = body.split("\n");
        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    if (albedo) {
                        variables.add("sumBandas");
                    }
                    for (int j = 1; j < 8; j++) {
                        if (terms[0].startsWith("O_")) {
                            gpuCode.append("        double * banda").append(j);
                            gpuCodeBody.append("        double * banda").append(j).append(",\n");
                            variables.add("banda" + j);
                            if (i + 1 < vet.length && j < 8) {
                                gpuCode.append(",");
                            }
                            gpuCode.append("\n");
                        }
                    }
                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 1; j < 8; j++) {
                        if (terms[0].startsWith("O_")) {
                            gpuCode.append("        double * bandaRefletida").append(j);
                            gpuCodeBody.append("        double * bandaRefletida").append(j).append(",\n");
                            variables.add("bandaRefletida" + j);
                            if (i + 1 < vet.length && j < 8) {
                                gpuCode.append(",");
                            }
                            gpuCode.append("\n");
                        }
                    }
                    break;
                default:
                    if (terms[0].startsWith("O_")) {
                        term = terms[0].substring(2);
                        gpuCode.append("        double * ").append(term);
                        gpuCodeBody.append("        double * ").append(term).append(",\n");
                        if (i + 1 < vet.length) {
                            gpuCode.append(",");
                        }
                        gpuCode.append("\n");
                        variables.add(term);
                    }
                    break;
            }
        }


        gpuCodeBody.append("        int size");

        gpuCode.append("    ){\n");
        gpuCodeBody.append("){\n");

        verifyEquations(header + "\n" + body, variables);


        gpuCodeBody.append("        int idx = blockIdx.x*blockDim.x + threadIdx.x;\n");
        gpuCodeBody.append("        if(idx < size){\n");
        gpuCodeBody.append("            execute_sub(\n");
        for (String string : parameters.keySet()) {
            gpuCodeBody.append("                ").append(string).append("[idx],\n");
        }

        String equation;
        String[] outEquation;
        String t;
        boolean rad_espectral = false;

        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            structure = new Structure();
            structure.setToken(terms[0]);
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    if (albedo) {
                        gpuCode.append("        double sumBandas = 0;\n");
                    }
                    for (int j = 1; j < 8; j++) {

                        equation = "        " + ((terms[0].startsWith("O_")) ? "" : "double ") + lexer.analyse(vet[i], structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (j) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (j) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (j));
                        equation = equation.replace("irrad_espectral", "calibration" + (j) + "[2]");

                        if (terms[0].startsWith("O_")) {
                            equation = equation.replace("rad_espectral", " *banda" + j);
                            gpuCodeBody.append("                (banda").append(j).append("+idx)");
                            if (i + 1 < vet.length && j < 8) {
                                gpuCodeBody.append(",");
                            }
                            gpuCodeBody.append("\n");
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + j);

                            rad_espectral = true;
                        }
                        gpuCode.append(equation);
                    }


                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 1; j < 8; j++) {


                        equation = "        " + ((terms[0].startsWith("O_")) ? "" : "double ") + lexer.analyse(vet[i], structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (j) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (j) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (j) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration" + (j) + "[2]");
                        if (terms[0].startsWith("O_")) {
                            equation = equation.replace("reflectancia", " *bandaRefletida" + j);
                        } else {
                            equation = equation.replace("reflectancia", "bandaRefletida" + j);
                        }
                        if (rad_espectral) {
                            equation = equation.replace("rad_espectral", "banda" + j);
                        } else {
                            equation = equation.replace("rad_espectral", " *banda" + j);
                        }
                        gpuCode.append(equation);

                        if (albedo) {
                            if (terms[0].startsWith("O_")) {
                                gpuCode.append("        sumBandas += parameterAlbedo[").append(j - 1).append("]* *bandaRefletida").append(j).append(";\n");

                                gpuCodeBody.append("                (bandaRefletida").append(j).append("+idx)");
                                if (i + 1 < vet.length && j < 8) {
                                    gpuCodeBody.append(",");
                                }
                                gpuCodeBody.append("\n");
                            } else {
                                gpuCode.append("        sumBandas += parameterAlbedo[").append(j - 1).append("]*bandaRefletida").append(j).append(";\n");
                            }
                        }

                    }
                    break;
                default:


                    equation = "        " + lexer.analyse(vet[i], structure, null, language) + ";\n";
                    ex.evaluateExpr(equation);
                    outEquation = ex.getOutput();
                    if (terms[0].startsWith("O_")) {
                        term = terms[0].substring(2);
                        equation = "        *" + term + " = ";

                        gpuCodeBody.append("                (").append(term).append("+idx)");
                        if (i + 1 < vet.length) {
                            gpuCodeBody.append(",");
                        }
                        gpuCodeBody.append("\n");

                    } else {
                        equation = "        " + terms[0] + " = ";
                    }

                    for (int j = 0; j < outEquation.length; j++) {
                        String string = outEquation[j];
                        for (int k = 0; k < i; k++) {
                            t = vet[k].split("=")[0].replace(" ", "");
//                            System.out.println("T:"+t);
                            if (t.equals("O_" + string)) {
                                t = t.substring(2);
                                string = " *" + t;
                                break;
                            } else if (string.equals(t)) {
                                string = t;
                            } else if (string.equals("~")) {
                                string = "-";
                            } else if (t.contains("banda") && t.contains(string)) {
                                if (t.startsWith("O_")) {
                                    string = " *" + string;
                                }
                                break;
                            }
                        }
                        equation += string;
                    }
                    equation += "\n";
                    gpuCode.append(equation);

                    break;
            }
        }


        gpuCodeBody.append("            );\n");
        gpuCodeBody.append("        }\n");
        gpuCodeBody.append("    }\n");


        gpuCode.append("    }\n\n");

        gpuCode.append(gpuCodeBody.toString());

        gpuCode.append("}\n");

//        System.out.println(gpuCode.toString());
        try {
            PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/source/code.cu");
            pw.println(gpuCode.toString());
            pw.close();
        } catch (FileNotFoundException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        }

        return source.toString();
    }

    private String generateOpenCL(String header, String body, Map<String, double[]> parameters, Map<String, Double> constants, Map<String, double[]> constantsVetor, Map<String, double[][]> constantsMatrix) {
        List<String> variables = getVariables();
        ExpressionParser ex = new ExpressionParser();

        StringBuilder source = new StringBuilder();
        source.append("import static br.ufmt.genericseb.Constants.*;\n");
        source.append("import java.util.HashMap;\n");
        source.append("import java.util.Map;\n");
        source.append("import br.ufmt.jseriesgpu.ParameterGPU;\n");
        source.append("import java.util.ArrayList;\n");
        source.append("import br.ufmt.jseriesgpu.JSeriesCL;\n");
        source.append("import java.io.File;\n");
        source.append("import java.io.IOException;\n");
        source.append("import java.util.logging.Level;\n");
        source.append("import java.util.logging.Logger;\n");
        source.append("import java.io.BufferedReader;\n");
        source.append("import java.io.FileReader;\n");
        source.append("import java.util.List;\n\n");



        source.append("public class Equation{\n");

        source.append("    public Map<String, double[]> execute(");
        int size = 0;
        String vet1 = null;
        pars = new Object[parameters.size()];
        classes = new Class[parameters.size()];
        for (String string : parameters.keySet()) {
            vet1 = string;
            pars[size] = parameters.get(string);
            classes[size] = parameters.get(string).getClass();
            source.append("double[] ").append(string);
            size++;
            if (size < parameters.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, double[]> ret = new HashMap<>();\n\n");
        source.append("        List<ParameterGPU> par = new ArrayList<ParameterGPU>();\n\n");

        boolean first = true;
        for (String string : parameters.keySet()) {
            if (first) {
                source.append("        int[] N = new int[]{").append(string).append(".length};\n\n");
                source.append("        par.add(new ParameterGPU(").append(string).append(",true,false,true));\n");
                first = false;
            } else {
                source.append("        par.add(new ParameterGPU(").append(string).append(",true));\n");
            }
        }

        source.append("\n");

        //DECLARANDO OS VETORES QUE SERAO RETORNADOS
        String[] vet = body.split("\n");
        String[] terms;
        String term;
        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    variables.add("pixel");
                    for (int j = 1; j < 8; j++) {
                        if (terms[0].startsWith("O_")) {
                            source.append("        double[] banda").append(j).append(" = new double[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(banda").append(j).append(",true,true));\n");
                            source.append("        ret.put(\"banda").append(j).append("\",banda").append(j).append(");\n\n");
                        }
                        variables.add("banda" + j);
                    }
                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 1; j < 8; j++) {
                        variables.add("bandaRefletida" + j);
                        if (terms[0].startsWith("O_")) {
                            source.append("        double[] bandaRefletida").append(j).append(" = new double[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(bandaRefletida").append(j).append(",true,true));\n");
                            source.append("        ret.put(\"bandaRefletida").append(j).append("\",bandaRefletida").append(j).append(");\n\n");
                        }
                    }
                    break;
                default:
                    if (terms[0].startsWith("O_")) {
                        term = terms[0].substring(2);
                        source.append("        double[] ").append(term).append(" = new double[").append(vet1).append(".length];\n");
                        source.append("        par.add(new ParameterGPU(").append(term).append(",true,true));\n");
                        source.append("        ret.put(\"").append(term).append("\",").append(term).append(");\n\n");
                    }
                    break;
            }
        }

        source.append("        par.add(new ParameterGPU(N,true));\n\n");

        source.append("        String source = \"code.cl\";\n");
        source.append("        try {\n");
        source.append("            BufferedReader bur = new BufferedReader(new FileReader(System.getProperty(\"user.dir\") + \"/source/\" + source));\n");
        source.append("            JSeriesCL opencl = new JSeriesCL();\n");
        source.append("            opencl.setMeasure(true);\n");
        source.append("            String linha = bur.readLine();\n");
        source.append("            StringBuilder codigo = new StringBuilder(linha + \"\\n\");\n");
        source.append("            while (linha != null) {\n");
        source.append("                codigo.append(linha + \"\\n\");\n");
        source.append("                linha = bur.readLine();\n");
        source.append("            }\n");
        source.append("            bur.close();\n");
        source.append("            opencl.execute(par, codigo.toString(), \"execute\");\n");
        source.append("            File newFile = new File(System.getProperty(\"user.dir\") + \"/source/\" + source);\n");
        source.append("            //newFile.delete();\n");
        source.append("        } catch (IOException ex) {\n");
        source.append("            Logger.getLogger(Equation.class.getName()).log(Level.SEVERE, null, ex);\n");
        source.append("        }\n");


        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");

        //gpu part

        StringBuilder gpuCode = new StringBuilder("\n");
        gpuCode.append("#ifdef cl_khr_fp64\n");
        gpuCode.append("	#pragma OPENCL EXTENSION cl_khr_fp64: enable\n");
        gpuCode.append("#else\n");
        gpuCode.append("	#pragma OPENCL EXTENSION cl_amd_fp64: enable\n");
        gpuCode.append("#endif\n\n");
        gpuCode.append("\n#include \"./source/Constants.h\"\n\n");

        StringBuilder gpuCodeBody = new StringBuilder("    __kernel void execute(\n");

        if (constants != null) {
            for (String string : constants.keySet()) {
                gpuCode.append("    #define ").append(string).append(" ").append(constants.get(string)).append("f\n");
                variables.add(string);
            }
        }
        gpuCode.append("\n");

        vet = header.split("\n");
        GenericLexerSEB lexer = new GenericLexerSEB();
        Structure structure;
        Map<String, Double> vars = new HashMap<>();
        vars.putAll(constants);
        vars.putAll(Constants.variables);

        //COLOCANDO AS FORMULAS DO CABECALHOS
        double res;
        for (int i = 0; i < vet.length; i++) {
            structure = new Structure();
            structure.setToken(vet[i].split("=")[0].replace(" ", ""));
            String equation = lexer.analyse(vet[i], structure, null, LanguageType.PYTHON);
            res = lexer.getResults(equation, vars);
            vars.put(structure.getToken(), res);
            gpuCode.append("    #define ").append(structure.getToken()).append(" ").append(res).append("f\n");
            variables.add(structure.getToken());
        }
        gpuCode.append("\n");


        StringBuilder vf;
        boolean albedo = false;
        if (constantsVetor != null) {
            for (String string : constantsVetor.keySet()) {
                double[] doubleVet = constantsVetor.get(string);
                vf = new StringBuilder();
                for (int i = 0; i < doubleVet.length - 1; i++) {
                    vf.append(doubleVet[i]).append("f,");
                }
                vf.append(doubleVet[doubleVet.length - 1] + "f");
                gpuCode.append("    __constant double ").append(string).append("[] = {").append(vf.toString()).append("};\n");
                if (string.equals("parameterAlbedo")) {
                    albedo = true;
                }
            }
        }
        gpuCode.append("\n");

        if (constantsMatrix != null) {
            for (String string : constantsMatrix.keySet()) {
                double[][] doubleVet = constantsMatrix.get(string);
                for (int i = 0; i < doubleVet.length; i++) {
                    vf = new StringBuilder();
                    vf.append("{");
                    gpuCode.append("    __constant double ").append(string + (i + 1)).append("[] = ");
                    for (int j = 0; j < doubleVet[0].length - 1; j++) {
                        vf.append(doubleVet[i][j]).append(",");
                    }
                    vf.append(doubleVet[i][doubleVet[0].length - 1]);
                    vf.append("};\n");
                    gpuCode.append(vf.toString());
                }
                if (string.equals("calibration")) {
                    variables.add("coef_calib_a");
                    variables.add("coef_calib_b");
                    variables.add("irrad_espectral");
                }

            }
        }
        gpuCode.append("\n");


        gpuCode.append("    void execute_sub(\n");
        for (String string : parameters.keySet()) {
            gpuCode.append("        double ").append(string).append(",\n");
            gpuCodeBody.append("        __global double * ").append(string).append(",\n");
            variables.add(string);
        }
        gpuCode.append("\n");
        gpuCodeBody.append("\n");

        vet = body.split("\n");
        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    if (albedo) {
                        variables.add("sumBandas");
                    }
                    for (int j = 1; j < 8; j++) {
                        if (terms[0].startsWith("O_")) {
                            gpuCode.append("        __global double * banda").append(j).append(",\n");
                            gpuCodeBody.append("        __global double * banda").append(j).append(",\n");
                            variables.add("banda" + j);
                        }
                    }
                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 1; j < 8; j++) {
                        if (terms[0].startsWith("O_")) {
                            gpuCode.append("        __global double * bandaRefletida").append(j).append(",\n");
                            gpuCodeBody.append("        __global double * bandaRefletida").append(j).append(",\n");
                            variables.add("bandaRefletida" + j);
                        }
                    }
                    break;
                default:
                    if (terms[0].startsWith("O_")) {
                        term = terms[0].substring(2);
                        gpuCode.append("        __global double * ").append(term).append(",\n");
                        gpuCodeBody.append("        __global double * ").append(term).append(",\n");
                        variables.add(term);
                    }
                    break;
            }
        }


        gpuCode.append("        int idx");
        gpuCodeBody.append("        int size");

        gpuCode.append("){\n");
        gpuCodeBody.append("){\n");

        verifyEquations(header + "\n" + body, variables);


        gpuCodeBody.append("        int idx = get_global_id(0);\n");
        gpuCodeBody.append("        if(idx < size){\n");
        gpuCodeBody.append("            execute_sub(\n");
        for (String string : parameters.keySet()) {
            gpuCodeBody.append("                ").append(string).append("[idx],\n");
        }

        String equation;
        String[] outEquation;
        String t;
        boolean rad_espectral = false;

        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            structure = new Structure();
            structure.setToken(terms[0]);
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    if (albedo) {
                        gpuCode.append("        double sumBandas = 0;\n");
                    }
                    for (int j = 1; j < 8; j++) {

                        equation = "        " + ((terms[0].startsWith("O_")) ? "" : "double ") + lexer.analyse(vet[i], structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (j) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (j) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (j));
                        equation = equation.replace("irrad_espectral", "calibration" + (j) + "[2]");

                        if (terms[0].startsWith("O_")) {
                            equation = equation.replace("rad_espectral", " banda" + j+"[idx]");
                            gpuCodeBody.append("                banda").append(j).append(",\n");
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + j);

                            rad_espectral = true;
                        }
                        gpuCode.append(equation);
                    }


                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 1; j < 8; j++) {


                        equation = "        " + ((terms[0].startsWith("O_")) ? "" : "double ") + lexer.analyse(vet[i], structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (j) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (j) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (j) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration" + (j) + "[2]");
                        if (terms[0].startsWith("O_")) {
                            equation = equation.replace("reflectancia", " bandaRefletida" + j + "[idx]");
                        } else {
                            equation = equation.replace("reflectancia", "bandaRefletida" + j);
                        }
                        if (rad_espectral) {
                            equation = equation.replace("rad_espectral", "banda" + j);
                        } else {
                            equation = equation.replace("rad_espectral", " banda" + j + "[idx]");
                        }
                        gpuCode.append(equation);

                        if (albedo) {
                            if (terms[0].startsWith("O_")) {
                                gpuCode.append("        sumBandas += parameterAlbedo[").append(j - 1).append("]* bandaRefletida").append(j).append("[idx];\n");

                                gpuCodeBody.append("                bandaRefletida").append(j).append(",\n");
                            } else {
                                gpuCode.append("        sumBandas += parameterAlbedo[").append(j - 1).append("]*bandaRefletida").append(j).append(";\n");
                            }
                        }

                    }
                    break;
                default:


                    equation = "        " + lexer.analyse(vet[i], structure, null, language) + ";\n";
                    ex.evaluateExpr(equation);
                    outEquation = ex.getOutput();
                    if (terms[0].startsWith("O_")) {
                        term = terms[0].substring(2);
                        equation = "        " + term + "[idx] = ";

                        gpuCodeBody.append("                ").append(term).append(",\n");

                    } else {
                        equation = "        " + terms[0] + " = ";
                    }

                    for (int j = 0; j < outEquation.length; j++) {
                        String string = outEquation[j];
                        for (int k = 0; k < i; k++) {
                            t = vet[k].split("=")[0].replace(" ", "");
//                            System.out.println("T:"+t);
                            if (t.equals("O_" + string)) {
                                t = t.substring(2);
                                string = t + "[idx]";
                                break;
                            } else if (string.equals(t)) {
                                string = t;
                            } else if (string.equals("~")) {
                                string = "-";
                            } else if (t.contains("banda") && t.contains(string)) {
                                if (t.startsWith("O_")) {
                                    string = string + "[idx]";
                                }
                                break;
                            }
                        }
                        equation += string;
                    }
                    equation += "\n";
                    gpuCode.append(equation);

                    break;
            }
        }


        gpuCodeBody.append("            idx);\n");
        gpuCodeBody.append("        }\n");
        gpuCodeBody.append("    }\n");


        gpuCode.append("    }\n\n");

        gpuCode.append(gpuCodeBody.toString());


//        System.out.println(source.toString());
        try {
            PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/source/code.cl");
            pw.println(gpuCode.toString());
            pw.close();
        } catch (FileNotFoundException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        }
//        System.exit(1);

        return source.toString();
    }

    private String generateJava(String header, String body, Map<String, double[]> parameters, Map<String, Double> constants, Map<String, double[]> constantsVetor, Map<String, double[][]> constantsMatrix) {
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
        pars = new Object[parameters.size()];
        classes = new Class[parameters.size()];
        for (String string : parameters.keySet()) {
            vet1 = string;
            pars[size] = parameters.get(string);
            classes[size] = parameters.get(string).getClass();
            source.append("double[] ").append(string);
            size++;
            if (size < parameters.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, double[]> ret = new HashMap<>();\n\n");

        if (constants != null) {
            for (String string : constants.keySet()) {
                source.append("        double ").append(string).append(" = ").append(constants.get(string)).append("f;\n");
                variables.add(string);
            }
        }
        source.append("\n");

        StringBuilder vf;
        boolean albedo = false;
        if (constantsVetor != null) {
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
        }
        source.append("\n");

        if (constantsMatrix != null) {
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
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    variables.add("pixel");
                    if (albedo) {
                        source.append("        double sumBandas = 0.0;\n");
                        variables.add("sumBandas");
                    }
                    if (terms[0].startsWith("O_")) {
                        for (int j = 1; j < 8; j++) {
                            source.append("        double[] banda").append(j).append(" = new double[").append(vet1).append(".length];\n");
                            source.append("        ret.put(\"banda").append(j).append("\",banda").append(j).append(");\n\n");
                            variables.add("banda" + j);
                        }
                    } else {
                        for (int j = 1; j < 8; j++) {
                            variables.add("banda" + j);
                            source.append("        double banda").append(j).append(" = 0;\n");
                        }
                    }
                    break;
                case "reflectancia":
                case "O_reflectancia":
                    if (terms[0].startsWith("O_")) {
                        for (int j = 1; j < 8; j++) {
                            variables.add("bandaRefletida" + j);
                            source.append("        double[] bandaRefletida").append(j).append(" = new double[").append(vet1).append(".length];\n");
                            source.append("        ret.put(\"bandaRefletida").append(j).append("\",bandaRefletida").append(j).append(");\n\n");
                        }
                    } else {
                        for (int j = 1; j < 8; j++) {
                            variables.add("bandaRefletida" + j);
                            source.append("        double bandaRefletida").append(j).append(" = 0;\n");
                        }
                    }
                    break;
                default:
                    if (terms[0].startsWith("O_")) {
                        source.append("        double[] ").append(terms[0]).append(" = new double[").append(vet1).append(".length];\n");
                        source.append("        ret.put(\"").append(terms[0].substring(2)).append("\",").append(terms[0]).append(");\n\n");
                    } else {
                        source.append("        double ").append(terms[0]).append(" = 0;\n");
                    }
                    break;
            }
        }

        verifyEquations(header + "\n" + body, variables);


        source.append("        for(int i = 0;i < ").append(vet1).append(".length;i++){\n");

        String equation;
        String[] outEquation;
        String t;
        boolean rad_espectral = false;

        for (int i = 0; i < vet.length; i++) {
            terms = vet[i].split("=");
            terms[0] = terms[0].replace(" ", "");
            structure = new Structure();
            structure.setToken(terms[0]);
            switch (terms[0]) {
                case "rad_espectral":
                case "O_rad_espectral":
                    for (int j = 1; j < 8; j++) {
                        equation = "            " + lexer.analyse(vet[i], structure, null, LanguageType.JAVA) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration[" + (j - 1) + "][0]");
                        equation = equation.replace("coef_calib_b", "calibration[" + (j - 1) + "][1]");
                        equation = equation.replace("pixel", "pixel" + (j) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration[" + (j - 1) + "][2]");

                        if (terms[0].startsWith("O_")) {
                            equation = equation.replace("rad_espectral", "banda" + j + "[i]");
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + j);
                            rad_espectral = true;
                        }
                        source.append(equation);
                        variables.add("banda" + j);
                    }
                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 1; j < 8; j++) {
                        equation = "            " + lexer.analyse(vet[i], structure, null, LanguageType.JAVA) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration[" + (j - 1) + "][0]");
                        equation = equation.replace("coef_calib_b", "calibration[" + (j - 1) + "][1]");
                        equation = equation.replace("pixel", "pixel" + (j) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration[" + (j - 1) + "][2]");
                        if (terms[0].startsWith("O_")) {
                            equation = equation.replace("reflectancia", "bandaRefletida" + j + "[i]");
                        } else {
                            equation = equation.replace("reflectancia", "bandaRefletida" + j);
                        }
                        if (rad_espectral) {
                            equation = equation.replace("rad_espectral", "banda" + j);
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + j + "[i]");
                        }
                        source.append(equation);
                        variables.add("bandaRefletida" + j);

                        if (albedo) {
                            if (terms[0].startsWith("O_")) {
                                source.append("            sumBandas += parameterAlbedo[").append(j - 1).append("]*bandaRefletida").append(j).append("[i];\n");
                            } else {
                                source.append("            sumBandas += parameterAlbedo[").append(j - 1).append("]*bandaRefletida").append(j).append(";\n");
                            }
                        }
                    }
                    break;
                default:
                    equation = "            " + lexer.analyse(vet[i], structure, null, LanguageType.JAVA) + ";\n";
                    ex.evaluateExpr(equation);
                    outEquation = ex.getOutput();
//                    System.out.println("eq:"+equation);
                    if (terms[0].startsWith("O_")) {
                        equation = "            " + terms[0] + "[i] = ";
                    } else {
                        equation = "            " + terms[0] + " = ";
                    }

                    for (int j = 0; j < outEquation.length; j++) {
                        String string = outEquation[j];
                        for (int k = 0; k < i; k++) {
                            t = vet[k].split("=")[0].replace(" ", "");
//                            System.out.println("T:"+t);
                            if (t.equals("O_" + string)) {
                                string = t + "[i]";
                                break;
                            } else if (string.equals(t)) {
                                string = t;
                            } else if (string.equals("~")) {
                                string = "-";
                            } else if (t.contains("banda") && t.contains(string)) {
                                if (t.startsWith("O_")) {
                                    string = string + "[i]";
                                }
                                break;
                            }
                        }
                        equation += string;
                    }
                    equation += "\n";
                    variables.add(terms[0]);
                    source.append(equation);
                    break;
            }
        }

        source.append("        }\n");

        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");
        return source.toString();
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
        constants.put("reflectanciaAtmosfera", 0.03);
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
                + "O_albedo = (sumBandas - reflectanciaAtmosfera) / (transmissividade * transmissividade)\n"
                + "O_NDVI = (bandaRefletida4 - bandaRefletida3) / (bandaRefletida4 + bandaRefletida3)\n"
                + "O_SAVI = ((1.0 + L) * (bandaRefletida4 - bandaRefletida3)) / (L + bandaRefletida4 + bandaRefletida3)\n"
                + "O_IAF = (-ln((0.69 - SAVI) / 0.59) / 0.91)\n"
                + "O_emissividadeNB = 0.97 + 0.0033 * IAF\n"
                + "O_emissivity = 0.95 + 0.01 * IAF\n"
                + "O_Ts = K2/ln(((emissividadeNB * K1) / banda6) + 1.0)\n"
                + "O_LWd = emissivity * StefanBoltzman * (pow(Ts, 4))\n"
                + "O_Rn = ((1.0 - albedo) * SWd) + (emissivity * (LWdAtm) - LWd)";
        GenericSEB g = new GenericSEB(LanguageType.CUDA_FLOAT);
        g.execute(header, body, parameters, constants, constVetor, constMatrix);
    }
}
