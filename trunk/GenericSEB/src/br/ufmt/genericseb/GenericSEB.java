/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

import br.ufmt.genericlexerseb.ExpressionParser;
import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import br.ufmt.genericlexerseb.Variable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raphael
 */
public class GenericSEB {

    private LanguageType language;
    private Object[] pars;
    private Class[] classes;
    private List<Equation> equations;
    private Equation index;

    public GenericSEB(LanguageType language) {
        this.language = language;
    }

    public GenericSEB() {
        this(LanguageType.JAVA);
    }

    public boolean verifyEquations(String forms, List<String> variables, boolean verifyIF) throws Exception {

        if (forms == null || forms.isEmpty()) {
            throw new Exception("Equations are empty!");
        }
        if (verifyIF) {
            equations = new ArrayList<>();
        }
        Equation equation = null;

        ExpressionParser ex = new ExpressionParser();
        String vet[];
        String vets[] = forms.split("\n");
        GenericLexerSEB lexer = new GenericLexerSEB();

        String line;
        for (int i = 0; i < vets.length; i++) {
            line = vets[i];

            if (verifyIF) {
                equation = new Equation();
            }
//            System.out.println("Line:" + line);
            line = line.replaceAll("[ ]+", "");
            if (line.contains(")=")) {
                int idx = line.indexOf(")=");
                while (line.indexOf(")=", idx + 1) != -1) {
                    idx = line.indexOf(")=", idx + 1);
                }
                vet = new String[2];
                vet[0] = line.substring(0, idx) + ")";
                vet[1] = line.substring(idx + 2);

            } else {
                vet = line.split("=");
            }
            if (vet[0].startsWith("O_")) {
                vet[0] = vet[0].substring(2);
                if (verifyIF) {
                    equation.setIndex("idx");
                }
            }
            if (vet[0].contains("_(")) {
                if (verifyIF) {
                    String ifTest = vet[0].substring(vet[0].indexOf("_(") + 2, vet[0].length() - 1);
                    vet[0] = vet[0].substring(0, vet[0].indexOf("_("));
//                    System.out.println(ifTest + " " + vet[0]);

                    try {
                        if (!ex.evaluateExprIf(ifTest, variables)) {
                            throw new Exception("Equation " + ifTest + " is wrong!");
                        }
                        if (verifyIF) {
                            ifTest = lexer.analyse(ifTest, language);

                            equation.setCondition(ex.tokenizeIf(ifTest));
                        }
                    } catch (IllegalArgumentException e) {
//                    System.out.println("Equation is wrong: " + line);
                        e.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    throw new RuntimeException("Header can't not have conditions");
                }
//                System.exit(1);
            }
            line = vet[0] + "=" + vet[1];
            if (verifyIF) {
                equation.setTerm(vet[0]);
                equation.setForm(vet[1]);
                if (vet[0].equals("index")) {
                    index = equation;
                    equation.setIndex(null);
                } else {
                    equations.add(equation);
                }
            }
            variables.add(vet[0]);
            try {
                if (!ex.evaluateExpr(line, variables)) {
                    throw new Exception("Equation " + line + " is wrong!");
                }

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

    public Map<String, float[]> execute(String header, String body, List<Value> parameters, Map<String, Float> constants) throws Exception {
        return execute(header, body, parameters, constants, null, null);
    }

    public Map<String, float[]> execute(String header, String body, List<Value> parameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {

        Map<String, float[]> ret = new HashMap<>();
        Map<String, float[]> firstRet = null;
        String source = null;


        StringBuilder newBodyWithIndex = new StringBuilder();
        StringBuilder newBodyWithoutIndex = new StringBuilder();

        String vet[];
        String vets[] = body.split("\n");
        boolean hasIndex = false;

        String line;
        for (int i = 0; i < vets.length; i++) {
            line = vets[i];

            line = line.replaceAll("[ ]+", "");
            if (line.contains(")=")) {
                int idx = line.indexOf(")=");
                while (line.indexOf(")=", idx + 1) != -1) {
                    idx = line.indexOf(")=", idx + 1);
                }
                vet = new String[2];
                vet[0] = line.substring(0, idx) + ")";
                vet[1] = line.substring(idx + 2);
            } else {
                vet = line.split("=");
            }
            if (vet[0].startsWith("O_")) {
                vet[0] = vet[0].substring(2);
            }
            if (vet[0].contains("_(")) {
                vet[0] = vet[0].substring(0, vet[0].indexOf("_("));
            }

            if (!hasIndex) {
                newBodyWithIndex.append(vets[i]).append("\n");
                if (vets[i].startsWith("O_")) {
                    newBodyWithoutIndex.append(vets[i].substring(2)).append("\n");
                } else if (!vet[0].equals("index")) {
                    newBodyWithoutIndex.append(vets[i]).append("\n");
                }
            } else {
                newBodyWithoutIndex.append(vets[i]).append("\n");
            }

            if (vet[0].equals("index")) {
                hasIndex = true;
            }
        }
//        System.out.println(newBodyWithIndex.toString());
//        System.out.println("--------------------");
//        System.out.println(newBodyWithoutIndex.toString());
//        System.exit(1);

        if (language.equals(LanguageType.CUDA)) {
            if (hasIndex) {
                throw new Exception("Index not yet implemented with CUDA");
            }
            source = generateCUDA(header, body, parameters, constants, constantsVetor, constantsMatrix);
        } else if (language.equals(LanguageType.OPENCL)) {
            if (hasIndex) {
                throw new Exception("Index not yet implemented with OpenCL");
            }
            source = generateOpenCL(header, body, parameters, constants, constantsVetor, constantsMatrix);
        } else {
            String exec = body;
            if (hasIndex) {
                firstRet = new HashMap<>();
                source = generateJava(header, newBodyWithIndex.toString(), parameters, constants, constantsVetor, constantsMatrix);
                Object instanced = compile(source, "Equation");
                try {
                    Method method = instanced.getClass().getDeclaredMethod("execute", classes);
                    firstRet = (Map<String, float[]>) method.invoke(instanced, pars);
                    constants.put("a", firstRet.get("coef")[0]);
                    constants.put("b", firstRet.get("coef")[1]);
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
                exec = newBodyWithoutIndex.toString();
                pars = null;
                classes = null;
                equations.clear();
                index = null;
            }
//            System.out.println("Exec:" + exec);
//            System.exit(1);
            source = generateJava(header, exec, parameters, constants, constantsVetor, constantsMatrix);
        }

//        System.out.println(source);
//        System.exit(1);

        Object instanced = compile(source, "Equation");
        try {
            Method method = instanced.getClass().getDeclaredMethod("execute", classes);
            ret = (Map<String, float[]>) method.invoke(instanced, pars);
            if (hasIndex) {
                ret.putAll(firstRet);
            }
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

    private String generateCUDA(String header, String body, List<Value> parametersList, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {

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
        source.append("import br.ufmt.genericlexerseb.Maths;\n");
        source.append("import java.util.List;\n\n");

        source.append("public class Equation{\n");

        source.append("    public Map<String, float[]> execute(");
        int size = 0;
        String vet1 = null;
        Map<String, String> parameters = new HashMap<>();
        pars = new Object[parametersList.size()];
        classes = new Class[parametersList.size()];
        List<Integer> numbers = new ArrayList<>();
        for (Value value : parametersList) {
            String string = value.getName();
            parameters.put(string, string);
            if (string.startsWith("pixel")) {
                if (string.replace("pixel", "").matches("[0-9]+")) {
                    numbers.add(Integer.parseInt(string.replace("pixel", "")));
                } else {
                    throw new Exception("Miss number pixel name");
                }
            }
            pars[size] = value.getData();
            classes[size] = value.getData().getClass();
            source.append("float[] ").append(string);
            size++;
            if (size < parametersList.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, float[]> ret = new HashMap<>();\n\n");
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
        String term;
        boolean vector;
        Set<String> variablesDeclared = new HashSet<>();
        for (int i = 0; i < vet.length; i++) {
            if (vet[i].startsWith("rad_espectral") || vet[i].startsWith("O_rad_espectral")) {
                if (variablesDeclared.add("banda")) {
                    variables.add("pixel");
                    vector = vet[i].startsWith("O_rad_espectral");
                    for (int j = 0; j < numbers.size(); j++) {
                        if (vector) {
                            source.append("        float[] banda").append(numbers.get(j)).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(banda").append(numbers.get(j)).append(",true,true));\n");
                            source.append("        ret.put(\"banda").append(numbers.get(j)).append("\",banda").append(numbers.get(j)).append(");\n\n");
                        }
                        variables.add("banda" + numbers.get(j));
                    }
                }
            } else if (vet[i].startsWith("reflectancia") || vet[i].startsWith("O_reflectancia")) {
                if (variablesDeclared.add("bandaRefletida")) {
                    vector = vet[i].startsWith("O_reflectancia");

                    for (int j = 0; j < numbers.size(); j++) {
                        variables.add("bandaRefletida" + numbers.get(j));
                        if (vector) {
                            source.append("        float[] bandaRefletida").append(numbers.get(j)).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(bandaRefletida").append(numbers.get(j)).append(",true,true));\n");
                            source.append("        ret.put(\"bandaRefletida").append(numbers.get(j)).append("\",bandaRefletida").append(numbers.get(j)).append(");\n\n");
                        }
                    }
                }
            }

        }

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
        Map<String, Float> vars = new HashMap<>();
        vars.putAll(constants);
        vars.putAll(Constants.variables);

        //COLOCANDO AS FORMULAS DO CABECALHOS
        float res;
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
                float[] floatVet = constantsVetor.get(string);
                vf = new StringBuilder();
                for (int i = 0; i < floatVet.length - 1; i++) {
                    vf.append(floatVet[i]).append("f,");
                }
                vf.append(floatVet[floatVet.length - 1] + "f");
                gpuCode.append("    __constant__ float ").append(string).append("[] = {").append(vf.toString()).append("};\n");
                if (string.equals("parameterAlbedo")) {
                    albedo = true;
                }
            }
        }
        gpuCode.append("\n");

        if (constantsMatrix != null) {
            for (String string : constantsMatrix.keySet()) {
                float[][] floatVet = constantsMatrix.get(string);
                for (int i = 0; i < floatVet.length; i++) {
                    vf = new StringBuilder();
                    vf.append("{");
                    if (string.equals("calibration")) {
                        gpuCode.append("    __constant__ float ").append(string + (numbers.get(i))).append("[] = ");
                    } else {
                        gpuCode.append("    __constant__ float ").append(string + (i + 1)).append("[] = ");
                    }
                    for (int j = 0; j < floatVet[0].length - 1; j++) {
                        vf.append(floatVet[i][j]).append("f,");
                    }
                    vf.append(floatVet[i][floatVet[0].length - 1]).append("f");
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
            gpuCode.append("        float ").append(string).append(",\n");
            gpuCodeBody.append("        float * ").append(string).append(",\n");
            variables.add(string);
        }
        gpuCode.append("\n");
        gpuCodeBody.append("\n");


        vet = body.split("\n");
        for (int i = 0; i < vet.length; i++) {
            if (vet[i].startsWith("rad_espectral") || vet[i].startsWith("O_rad_espectral")) {
                vector = vet[i].startsWith("O_rad_espectral");
                if (albedo) {
                    variables.add("sumBandas");
                }
                for (int j = 0; j < numbers.size(); j++) {
                    if (vector) {
                        gpuCode.append("        float * banda").append(numbers.get(j));
                        gpuCodeBody.append("        float * banda").append(numbers.get(j)).append(",\n");
                        variables.add("banda" + numbers.get(j));
                        if (i + 1 < vet.length && j + 1 < numbers.size()) {
                            gpuCode.append(",");
                        }
                        gpuCode.append("\n");
                    }
                }
            } else if (vet[i].startsWith("reflectancia") || vet[i].startsWith("O_reflectancia")) {
                vector = vet[i].startsWith("O_reflectancia");
                for (int j = 0; j < numbers.size(); j++) {
                    if (vector) {
                        gpuCode.append("        float * bandaRefletida").append(numbers.get(j));
                        gpuCodeBody.append("        float * bandaRefletida").append(numbers.get(j)).append(",\n");
                        variables.add("bandaRefletida" + numbers.get(j));
                        if (i + 1 < vet.length && j + 1 < numbers.size()) {
                            gpuCode.append(",");
                        }
                        gpuCode.append("\n");
                    }
                }
            }
        }
        if (header != null && !header.isEmpty()) {
            verifyEquations(header, variables, false);
        }
        if (body != null && !body.isEmpty()) {
            verifyEquations(body, variables, true);
        }
        Equation eq;
        for (int i = 0; i < equations.size(); i++) {
            eq = equations.get(i);
            switch (eq.getTerm()) {
                case "rad_espectral":
                case "O_rad_espectral":
                case "reflectancia":
                case "O_reflectancia":
                    break;
                default:
                    if (variablesDeclared.add(eq.getTerm())) {
                        if (eq.getIndex() != null) {
                            term = eq.getTerm();
                            gpuCode.append("        float * ").append(term);
                            gpuCodeBody.append("        float * ").append(term).append(",\n");
                            if (i + 1 < vet.length) {
                                gpuCode.append(",");
                            }
                            gpuCode.append("\n");


                            source.append("        float[] ").append(term).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(").append(term).append(",true,true));\n");
                            source.append("        ret.put(\"").append(term).append("\",").append(term).append(");\n\n");


                            variables.add(term);
                        }
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


        gpuCodeBody.append("        int size");

        gpuCode.append("    ){\n");
        gpuCodeBody.append("){\n");


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
        String ident;
        Equation eq2;

        variablesDeclared = new HashSet<>();
        for (int i = 0; i < vet.length; i++) {
            eq = equations.get(i);
//            terms = vet[i].split("=");
            structure = new Structure();
            structure.setToken(eq.getTerm());
            equation = eq.getTerm() + "=" + eq.getForm();
            ident = "";
            if (eq.getCondition() != null) {
                gpuCode.append("        if(");
                String[] condition = eq.getCondition();
                boolean find;
                for (int j = 0; j < condition.length; j++) {
                    find = false;
                    for (int k = 0; k < i; k++) {
                        if (condition[j].equals(equations.get(k).getTerm())) {
                            if (equations.get(k).getIndex() != null) {
                                gpuCode.append(" *");
                            }
                            gpuCode.append(equations.get(k).getTerm());
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        if (parameters.get(condition[j]) != null) {
                            gpuCode.append(" *");
                        }
                        gpuCode.append(condition[j]);
                        if (condition[j].matches("(-?)[0-9]+[\\.][0-9]+")) {
                            gpuCode.append("f");
                        }
                    }
                    gpuCode.append(" ");
                }
                gpuCode.append("){\n");
                ident = "    ";
            }
            switch (eq.getTerm()) {
                case "rad_espectral":
                case "O_rad_espectral":
                    if (albedo) {
                        gpuCode.append("        float sumBandas = 0.0f;\n");
                    }
                    for (int j = 0; j < numbers.size(); j++) {
                        equation = eq.getTerm() + "=" + eq.getForm();
                        equation = ident + "        " + ((eq.getIndex() != null) ? "" : "float ") + lexer.analyse(equation, structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (numbers.get(j)) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (numbers.get(j)) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (numbers.get(j)));
                        equation = equation.replace("irrad_espectral", "calibration" + (numbers.get(j)) + "[2]");

                        if (eq.getIndex() != null) {
                            equation = equation.replace("rad_espectral", " *banda" + numbers.get(j));
                            if (variablesDeclared.add("banda")) {
                                gpuCodeBody.append("                (banda").append(numbers.get(j)).append("+idx)");
                                if (i + 1 < vet.length && j + 1 < numbers.size()) {
                                    gpuCodeBody.append(",");
                                }
                                gpuCodeBody.append("\n");
                            }
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j));

                            rad_espectral = true;
                        }
                        gpuCode.append(equation).append("\n");
                    }


                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 0; j < numbers.size(); j++) {

                        equation = eq.getTerm() + "=" + eq.getForm();
                        equation = ident + "        " + ((eq.getIndex() != null) ? "" : "float ") + lexer.analyse(equation, structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (numbers.get(j)) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (numbers.get(j)) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (numbers.get(j)) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration" + (numbers.get(j)) + "[2]");
                        if (eq.getIndex() != null) {
                            equation = equation.replace("reflectancia", " *bandaRefletida" + numbers.get(j));
                        } else {
                            equation = equation.replace("reflectancia", "bandaRefletida" + numbers.get(j));
                        }
                        if (rad_espectral) {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j));
                        } else {
                            equation = equation.replace("rad_espectral", " *banda" + numbers.get(j));
                        }
                        gpuCode.append(equation).append("\n");

                        if (albedo) {
                            if (eq.getIndex() != null) {
                                gpuCode.append(ident).append("        sumBandas += parameterAlbedo[").append(j).append("]* *bandaRefletida").append(numbers.get(j)).append(";\n");

                                if (variablesDeclared.add("bandaRefletida")) {
                                    gpuCodeBody.append("                (bandaRefletida").append(numbers.get(j)).append("+idx)");
                                    if (i + 1 < vet.length && j + 1 < numbers.size()) {
                                        gpuCodeBody.append(",");
                                    }
                                    gpuCodeBody.append("\n");
                                }
                            } else {
                                gpuCode.append(ident).append("        sumBandas += parameterAlbedo[").append(j).append("]*bandaRefletida").append(numbers.get(j)).append(";\n");
                            }
                        }

                    }
                    break;
                default:

                    equation = ident + "        " + lexer.analyse(equation, structure, null, language) + ";\n";
                    ex.evaluateExpr(equation);
                    outEquation = ex.getOutput();
                    if (eq.getIndex() != null) {
                        term = eq.getTerm();
                        equation = ident + "        *" + term + " = ";

                        if (variablesDeclared.add(term)) {
                            gpuCodeBody.append("                (").append(term).append("+idx)");
                            if (i + 1 < vet.length) {
                                gpuCodeBody.append(",");
                            }
                            gpuCodeBody.append("\n");
                        }

                    } else {
                        equation = ident + "        " + eq.getTerm() + " = ";
                    }

                    for (int j = 0; j < outEquation.length; j++) {
                        String string = outEquation[j];
                        if (parameters.get(string) != null) {
                            string = " *" + string;
                        } else {
                            for (int k = 0; k < i; k++) {
                                eq2 = equations.get(k);
                                t = eq2.getTerm().replace(" ", "");
//                            System.out.println("T:"+t);

                                if (t.equals(string)) {
                                    if (eq2.getIndex() != null) {
                                        string = " *" + t;
                                    } else {
                                        string = t;
                                    }
                                    break;
                                } else if (string.equals("~")) {
                                    string = "-";
                                } else if (t.contains("banda") && t.contains(string)) {
                                    if (eq2.getIndex() != null) {
                                        string = " *" + string;
                                    }
                                    break;
                                }
                            }
                        }
                        equation += string;
                    }
                    equation += "\n\n";
                    gpuCode.append(equation);

                    break;
            }
            if (eq.getCondition() != null) {
                gpuCode.append("        }\n\n");
            }
        }


        gpuCodeBody.append("            );\n");
        gpuCodeBody.append("        }\n");
        gpuCodeBody.append("    }\n");


        gpuCode.append("    }\n\n");

        gpuCode.append(gpuCodeBody.toString());

        gpuCode.append("}\n");

        System.out.println(source.toString());
//        System.exit(1);
        try {
            File dir = new File(System.getProperty("user.dir") + "/source");
            dir.mkdirs();
            PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/source/code.cu");
            pw.println(gpuCode.toString());
            pw.close();
        } catch (FileNotFoundException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        }

        return source.toString();
    }

    private String generateOpenCL(String header, String body, List<Value> parametersList, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {
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
        source.append("import br.ufmt.genericlexerseb.Maths;\n");
        source.append("import java.util.List;\n\n");



        source.append("public class Equation{\n");

        source.append("    public Map<String, float[]> execute(");
        int size = 0;
        String vet1 = null;
        Map<String, String> parameters = new HashMap<>();
        pars = new Object[parametersList.size()];
        classes = new Class[parametersList.size()];
        List<Integer> numbers = new ArrayList<>();
        for (Value value : parametersList) {
            String string = value.getName();
            parameters.put(string, string);
            if (string.startsWith("pixel")) {
                if (string.replace("pixel", "").matches("[0-9]+")) {
                    numbers.add(Integer.parseInt(string.replace("pixel", "")));
                } else {
                    throw new Exception("Miss number pixel name");
                }
            }
            vet1 = string;
            pars[size] = value.getData();
            classes[size] = value.getData().getClass();
            source.append("float[] ").append(string);
            size++;
            if (size < parametersList.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, float[]> ret = new HashMap<>();\n\n");
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
        String term;
        boolean vector;
        Set<String> variablesDeclared = new HashSet<>();

        for (int i = 0; i < vet.length; i++) {
            if (vet[i].startsWith("rad_espectral") || vet[i].startsWith("O_rad_espectral")) {
                if (variablesDeclared.add("banda")) {
                    variables.add("pixel");
                    vector = vet[i].startsWith("O_rad_espectral");
                    for (int j = 0; j < numbers.size(); j++) {
                        if (vector) {
                            source.append("        float[] banda").append(numbers.get(j)).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(banda").append(numbers.get(j)).append(",true,true));\n");
                            source.append("        ret.put(\"banda").append(numbers.get(j)).append("\",banda").append(numbers.get(j)).append(");\n\n");
                        }
                        variables.add("banda" + numbers.get(j));
                    }
                } else if (vet[i].startsWith("reflectancia") || vet[i].startsWith("O_reflectancia")) {
                    if (variablesDeclared.add("bandaRefletida")) {
                        vector = vet[i].startsWith("O_reflectancia");
                        for (int j = 0; j < numbers.size(); j++) {
                            variables.add("bandaRefletida" + numbers.get(j));
                            if (vector) {
                                source.append("        float[] bandaRefletida").append(numbers.get(j)).append(" = new float[").append(vet1).append(".length];\n");
                                source.append("        par.add(new ParameterGPU(bandaRefletida").append(numbers.get(j)).append(",true,true));\n");
                                source.append("        ret.put(\"bandaRefletida").append(numbers.get(j)).append("\",bandaRefletida").append(numbers.get(j)).append(");\n\n");
                            }
                        }
                    }
                }
            }
        }

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
        Map<String, Float> vars = new HashMap<>();
        vars.putAll(constants);
        vars.putAll(Constants.variables);

        //COLOCANDO AS FORMULAS DO CABECALHOS
        float res;
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
                float[] floatVet = constantsVetor.get(string);
                vf = new StringBuilder();
                for (int i = 0; i < floatVet.length - 1; i++) {
                    vf.append(floatVet[i]).append("f,");
                }
                vf.append(floatVet[floatVet.length - 1] + "f");
                gpuCode.append("    __constant float ").append(string).append("[] = {").append(vf.toString()).append("};\n");
                if (string.equals("parameterAlbedo")) {
                    albedo = true;
                }
            }
        }
        gpuCode.append("\n");

        if (constantsMatrix != null) {
            for (String string : constantsMatrix.keySet()) {
                float[][] floatVet = constantsMatrix.get(string);
                for (int i = 0; i < floatVet.length; i++) {
                    vf = new StringBuilder();
                    vf.append("{");
                    if (string.equals("calibration")) {
                        gpuCode.append("    __constant float ").append(string + (numbers.get(i))).append("[] = ");
                    } else {
                        gpuCode.append("    __constant float ").append(string + (i + 1)).append("[] = ");
                    }
                    for (int j = 0; j < floatVet[0].length - 1; j++) {
                        vf.append(floatVet[i][j]).append("f,");
                    }
                    vf.append(floatVet[i][floatVet[0].length - 1]).append("f");
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
            gpuCode.append("        float ").append(string).append(",\n");
            gpuCodeBody.append("        __global float * ").append(string).append(",\n");
            variables.add(string);
        }
        gpuCode.append("\n");
        gpuCodeBody.append("\n");

        vet = body.split("\n");
        for (int i = 0; i < vet.length; i++) {
            if (vet[i].startsWith("rad_espectral") || vet[i].startsWith("O_rad_espectral")) {
                vector = vet[i].startsWith("O_rad_espectral");
                if (albedo) {
                    variables.add("sumBandas");
                }
                for (int j = 0; j < numbers.size(); j++) {
                    if (vector) {
                        gpuCode.append("        __global float * banda").append(numbers.get(j)).append(",\n");
                        gpuCodeBody.append("        __global float * banda").append(numbers.get(j)).append(",\n");
                    }
                    variables.add("banda" + numbers.get(j));
                }
            } else if (vet[i].startsWith("reflectancia") || vet[i].startsWith("O_reflectancia")) {
                vector = vet[i].startsWith("O_reflectancia");
                for (int j = 0; j < numbers.size(); j++) {
                    if (vector) {
                        gpuCode.append("        __global float * bandaRefletida").append(numbers.get(j)).append(",\n");
                        gpuCodeBody.append("        __global float * bandaRefletida").append(numbers.get(j)).append(",\n");
                    }
                    variables.add("bandaRefletida" + numbers.get(j));
                }
            }
        }

        if (header != null && !header.isEmpty()) {
            verifyEquations(header, variables, false);
        }
        if (body != null && !body.isEmpty()) {
            verifyEquations(body, variables, true);
        }
        Equation eq;
        for (int i = 0; i < equations.size(); i++) {
            eq = equations.get(i);
            switch (eq.getTerm()) {
                case "rad_espectral":
                case "O_rad_espectral":
                case "reflectancia":
                case "O_reflectancia":
                    break;
                default:
                    if (variablesDeclared.add(eq.getTerm())) {
                        if (eq.getIndex() != null) {
                            term = eq.getTerm();
                            gpuCode.append("        __global float * ").append(term).append(",\n");
                            gpuCodeBody.append("        __global float * ").append(term).append(",\n");
                            variables.add(term);

                            source.append("        float[] ").append(term).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        par.add(new ParameterGPU(").append(term).append(",true,true));\n");
                            source.append("        ret.put(\"").append(term).append("\",").append(term).append(");\n\n");

                            variables.add(term);
                        }
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

        gpuCode.append("        int idx");
        gpuCodeBody.append("        __global int * size");

        gpuCode.append("){\n");
        gpuCodeBody.append("){\n");

        gpuCodeBody.append("        int idx = get_global_id(0);\n");
        gpuCodeBody.append("        if(idx < size[0]){\n");
        gpuCodeBody.append("            execute_sub(\n");
        for (String string : parameters.keySet()) {
            gpuCodeBody.append("                ").append(string).append("[idx],\n");
        }

        String equation;
        String[] outEquation;
        String t;
        boolean rad_espectral = false;
        String ident;
        Equation eq2;

        variablesDeclared = new HashSet<>();
        for (int i = 0; i < vet.length; i++) {
            eq = equations.get(i);
            structure = new Structure();
            structure.setToken(eq.getTerm());
            equation = eq.getTerm() + "=" + eq.getForm();
            ident = "";
            if (eq.getCondition() != null) {
                gpuCode.append("        if(");
                String[] condition = eq.getCondition();
                boolean find;
                for (int j = 0; j < condition.length; j++) {
                    find = false;
                    for (int k = 0; k < i; k++) {
                        if (condition[j].equals(equations.get(k).getTerm())) {
                            gpuCode.append(equations.get(k).getTerm());
                            if (equations.get(k).getIndex() != null) {
                                gpuCode.append("[idx]");
                            }
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        gpuCode.append(condition[j]);
                        if (parameters.get(condition[j]) != null) {
                            gpuCode.append("[idx]");
                        }
                        if (condition[j].matches("(-?)[0-9]+[\\.][0-9]+")) {
                            gpuCode.append("f");
                        }
                    }
                    gpuCode.append(" ");
                }
                gpuCode.append("){\n");
                ident = "    ";
            }
            switch (eq.getTerm()) {
                case "rad_espectral":
                case "O_rad_espectral":
                    if (albedo) {
                        gpuCode.append("        float sumBandas = 0.0f;\n");
                    }
                    for (int j = 0; j < numbers.size(); j++) {

                        equation = eq.getTerm() + "=" + eq.getForm();
                        equation = ident + "        " + ((eq.getIndex() != null) ? "" : "float ") + lexer.analyse(equation, structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (numbers.get(j)) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (numbers.get(j)) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (numbers.get(j)));
                        equation = equation.replace("irrad_espectral", "calibration" + (numbers.get(j)) + "[2]");

                        if (eq.getIndex() != null) {
                            equation = equation.replace("rad_espectral", " banda" + numbers.get(j) + "[idx]");
                            if (variablesDeclared.add("banda")) {
                                gpuCodeBody.append("                banda").append(numbers.get(j)).append(",\n");
                            }
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j));

                            rad_espectral = true;
                        }
                        gpuCode.append(equation).append("\n");
                    }


                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 0; j < numbers.size(); j++) {

                        equation = eq.getTerm() + "=" + eq.getForm();
                        equation = ident + "        " + ((eq.getIndex() != null) ? "" : "float ") + lexer.analyse(equation, structure, null, language) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration" + (numbers.get(j)) + "[0]");
                        equation = equation.replace("coef_calib_b", "calibration" + (numbers.get(j)) + "[1]");
                        equation = equation.replace("pixel", "pixel" + (numbers.get(j)) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration" + (numbers.get(j)) + "[2]");
                        if (eq.getIndex() != null) {
                            equation = equation.replace("reflectancia", " bandaRefletida" + numbers.get(j) + "[idx]");
                        } else {
                            equation = equation.replace("reflectancia", "bandaRefletida" + numbers.get(j));
                        }
                        if (rad_espectral) {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j));
                        } else {
                            equation = equation.replace("rad_espectral", " banda" + numbers.get(j) + "[idx]");
                        }
                        gpuCode.append(equation).append("\n");

                        if (albedo) {
                            if (eq.getIndex() != null) {
                                gpuCode.append(ident + "        sumBandas += parameterAlbedo[").append(j).append("]* bandaRefletida").append(numbers.get(j)).append("[idx];\n");

                                if (variablesDeclared.add("bandaRefletida")) {
                                    gpuCodeBody.append("                bandaRefletida").append(numbers.get(j)).append(",\n");
                                }
                            } else {
                                gpuCode.append(ident + "        sumBandas += parameterAlbedo[").append(j).append("]*bandaRefletida").append(numbers.get(j)).append(";\n");
                            }
                        }

                    }
                    break;
                default:


                    equation = ident + "        " + lexer.analyse(equation, structure, null, language) + ";\n";
                    ex.evaluateExpr(equation);
                    outEquation = ex.getOutput();
                    if (eq.getIndex() != null) {
                        term = eq.getTerm();
                        equation = ident + "        " + term + "[idx] = ";

                        if (variablesDeclared.add(term)) {
                            gpuCodeBody.append("                ").append(term).append(",\n");
                        }

                    } else {
                        equation = ident + "        " + eq.getTerm() + " = ";
                    }

                    for (int j = 0; j < outEquation.length; j++) {
                        String string = outEquation[j];
                        if (parameters.get(string) != null) {
                            string = string + "[idx]";
                        } else {
                            for (int k = 0; k < i; k++) {

                                eq2 = equations.get(k);
                                t = eq2.getTerm().replace(" ", "");
//                            System.out.println("T:"+t);

                                if (t.equals(string)) {
                                    if (eq2.getIndex() != null) {
                                        string = t + "[idx]";
                                    } else {
                                        string = t;
                                    }
                                    break;
                                } else if (string.equals("~")) {
                                    string = "-";
                                } else if (t.contains("banda") && t.contains(string)) {
                                    if (eq2.getIndex() != null) {
                                        string = string + "[idx]";
                                    }
                                    break;
                                }
                            }
                        }
                        equation += string;
                    }
                    equation += "\n\n";
                    gpuCode.append(equation);

                    break;
            }
            if (eq.getCondition() != null) {
                gpuCode.append("        }\n\n");
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

    private String generateJava(String header, String body, List<Value> parametersList, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {
        List<String> variables = getVariables();
        ExpressionParser ex = new ExpressionParser();

        StringBuilder source = new StringBuilder();
        source.append("import static br.ufmt.genericseb.Constants.*;\n");
        source.append("import java.util.HashMap;\n");
        source.append("import java.util.Map;\n");
        source.append("import br.ufmt.genericseb.GenericSEB;\n");
        source.append("import br.ufmt.genericlexerseb.Maths;\n");

        source.append("import java.util.List;\n\n");

        source.append("public class Equation{\n");

        source.append("    public Map<String, float[]> execute(");
        int size = 0;
        String vet1 = null;
        Map<String, String> parameters = new HashMap<>();
        pars = new Object[parametersList.size()];
        classes = new Class[parametersList.size()];
        List<Integer> numbers = new ArrayList<>();
        for (Value value : parametersList) {
            String string = value.getName();
            parameters.put(string, string);
            if (string.startsWith("pixel")) {
                if (string.replace("pixel", "").matches("[0-9]+")) {
                    numbers.add(Integer.parseInt(string.replace("pixel", "")));
                } else {
                    throw new Exception("Miss number pixel name");
                }
            }
            vet1 = string;
            pars[size] = value.getData();
            classes[size] = value.getData().getClass();
            variables.add(string);
            source.append("float[] ").append(string);
            size++;
            if (size < parametersList.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, float[]> ret = new HashMap<>();\n\n");

        if (constants != null) {
            for (String string : constants.keySet()) {
                source.append("        float ").append(string).append(" = ").append(constants.get(string)).append("f;\n");
                variables.add(string);
            }
            source.append("\n");
        }

        StringBuilder vf;
        boolean albedo = false;
        if (constantsVetor != null) {
            for (String string : constantsVetor.keySet()) {
                float[] floatVet = constantsVetor.get(string);
                vf = new StringBuilder();
                for (int i = 0; i < floatVet.length - 1; i++) {
                    vf.append(floatVet[i]).append("f,");
                }
                vf.append(floatVet[floatVet.length - 1]).append("f");
                source.append("        float[] ").append(string).append(" = new float[]{").append(vf.toString()).append("};\n");
                if (string.equals("parameterAlbedo")) {
                    albedo = true;
                }
            }
            source.append("\n");
        }

        if (constantsMatrix != null) {
            for (String string : constantsMatrix.keySet()) {
                float[][] floatVet = constantsMatrix.get(string);
                vf = new StringBuilder();
                for (int i = 0; i < floatVet.length; i++) {
                    vf.append("{");
                    for (int j = 0; j < floatVet[0].length - 1; j++) {
                        vf.append(floatVet[i][j]).append("f,");
                    }
                    vf.append(floatVet[i][floatVet[0].length - 1]).append("f");
                    vf.append("}\n            ");
                    if (i + 1 < floatVet.length) {
                        vf.append(",");
                    }
                }
                if (string.equals("calibration")) {
                    variables.add("coef_calib_a");
                    variables.add("coef_calib_b");
                    variables.add("irrad_espectral");
                }
                source.append("        float[][] ").append(string).append(" = new float[][]{").append(vf.toString()).append("};\n");
            }
            source.append("\n");
        }

        String[] vet;

        GenericLexerSEB lexer = new GenericLexerSEB();
        Structure structure;

        if (header != null && !header.isEmpty()) {
            vet = header.split("\n");
            //COLOCANDO AS FORMULAS DO CABECALHOS
            for (int i = 0; i < vet.length; i++) {
                structure = new Structure();
                structure.setToken(vet[i].split("=")[0]);
                source.append("        float ").append(lexer.analyse(vet[i], structure, null, LanguageType.JAVA)).append(";\n\n");
            }
            source.append("\n");
        }

        //DECLARANDO OS VETORES QUE SERAO RETORNADOS
        vet = body.split("\n");
        Equation eq;
        boolean vector;
        Set<String> variablesDeclared = new HashSet<>();

        for (int i = 0; i < vet.length; i++) {
            if (vet[i].startsWith("rad_espectral") || vet[i].startsWith("O_rad_espectral")) {
                if (variablesDeclared.add("banda")) {
                    variables.add("pixel");
                    vector = vet[i].startsWith("O_rad_espectral");
                    if (albedo) {
                        source.append("        float sumBandas = 0.0f;\n");
                        variables.add("sumBandas");
                    }
                    if (vector) {
                        for (int j = 0; j < numbers.size(); j++) {
                            source.append("        float[] banda").append(numbers.get(j)).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        ret.put(\"banda").append(numbers.get(j)).append("\",banda").append(numbers.get(j)).append(");\n\n");
                            variables.add("banda" + numbers.get(j));
                        }
                    } else {
                        for (int j = 0; j < numbers.size(); j++) {
                            variables.add("banda" + numbers.get(j));
                            source.append("        float banda").append(numbers.get(j)).append(" = 0.0f;\n");
                        }
                    }
                }
            } else if (vet[i].startsWith("reflectancia") || vet[i].startsWith("O_reflectancia")) {
                if (variablesDeclared.add("bandaRefletida")) {
                    vector = vet[i].startsWith("O_reflectancia");
                    if (vector) {
                        for (int j = 0; j < numbers.size(); j++) {
                            variables.add("bandaRefletida" + j);
                            source.append("        float[] bandaRefletida").append(numbers.get(j)).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        ret.put(\"bandaRefletida").append(numbers.get(j)).append("\",bandaRefletida").append(numbers.get(j)).append(");\n\n");
                        }
                    } else {
                        for (int j = 0; j < numbers.size(); j++) {
                            variables.add("bandaRefletida" + j);
                            source.append("        float bandaRefletida").append(numbers.get(j)).append(" = 0.0f;\n");
                        }
                    }
                }
            }
        }
        System.out.println("Verify");
        if (header != null && !header.isEmpty()) {
            verifyEquations(header, variables, false);
        }
        if (body != null && !body.isEmpty()) {
            verifyEquations(body, variables, true);
        }

        if (index != null) {
            source.append("        float tMax, tMin;\n"
                    + "        float indexMax, indexMin;\n"
                    + "        float RnHot = 0.0f, GHot = 0.0f;\n"
                    + "        float SAVI_hot = 0.0f;\n"
                    + "\n"
                    + "        tMax = 0.0f;\n"
                    + "        indexMax = 0.0f;\n"
                    + "\n"
                    + "        indexMin = Float.MAX_VALUE;\n"
                    + "        tMin = Float.MAX_VALUE;\n");
            equations.add(index);
        }

        for (int i = 0; i < equations.size(); i++) {
            eq = equations.get(i);
            switch (eq.getTerm()) {
                case "rad_espectral":
                case "O_rad_espectral":
                case "reflectancia":
                case "O_reflectancia":
                    break;
                default:
                    if (variablesDeclared.add(eq.getTerm())) {
                        if (eq.getIndex() != null) {
                            source.append("        float[] ").append(eq.getTerm()).append(" = new float[").append(vet1).append(".length];\n");
                            source.append("        ret.put(\"").append(eq.getTerm()).append("\",").append(eq.getTerm()).append(");\n\n");
                        } else {
                            source.append("        float ").append(eq.getTerm()).append(" = 0;\n");
                        }
                    }
                    break;
            }
        }


        source.append("        for(int i = 0;i < ").append(vet1).append(".length;i++){\n");

        String equation;
        String[] outEquation;
        String t;
        Equation eq2;
        boolean rad_espectral = false;
        String ident;


        for (int i = 0; i < equations.size(); i++) {
//            terms = vet[i].split("=");
            eq = equations.get(i);
            structure = new Structure();
            structure.setToken(eq.getTerm());
            equation = eq.getTerm() + "=" + eq.getForm();
            ident = "";
            if (eq.getCondition() != null) {
                source.append("            if(");
                String[] condition = eq.getCondition();
                boolean find;
                for (int j = 0; j < condition.length; j++) {
                    find = false;
                    for (int k = 0; k < i; k++) {
                        if (condition[j].equals(equations.get(k).getTerm())) {
                            source.append(equations.get(k).getTerm());
                            if (equations.get(k).getIndex() != null) {
                                source.append("[i]");
                            }
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        source.append(condition[j]);
                        if (parameters.get(condition[j]) != null) {
                            source.append("[i]");
                        }
                        if (condition[j].matches("(-?)[0-9]+[\\.][0-9]+")) {
                            source.append("f");
                        }
                    }
                    source.append(" ");
                }
                source.append("){\n");
                ident = "    ";
            }
            switch (eq.getTerm()) {
                case "rad_espectral":
                case "O_rad_espectral":
                    for (int j = 0; j < numbers.size(); j++) {
                        equation = eq.getTerm() + "=" + eq.getForm();
                        equation = ident + "            " + lexer.analyse(equation, structure, null, LanguageType.JAVA) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration[" + (j) + "][0]");
                        equation = equation.replace("coef_calib_b", "calibration[" + (j) + "][1]");
                        equation = equation.replace("pixel", "pixel" + (numbers.get(j)) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration[" + (j) + "][2]");

                        if (eq.getIndex() != null) {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j) + "[i]");
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j));
                            rad_espectral = true;
                        }
                        source.append(equation).append("\n");
                        variables.add("banda" + numbers.get(j));
                    }
                    break;
                case "reflectancia":
                case "O_reflectancia":
                    for (int j = 0; j < numbers.size(); j++) {
                        equation = eq.getTerm() + "=" + eq.getForm();
                        equation = ident + "            " + lexer.analyse(equation, structure, null, LanguageType.JAVA) + ";\n";
                        equation = equation.replace("coef_calib_a", "calibration[" + (j) + "][0]");
                        equation = equation.replace("coef_calib_b", "calibration[" + (j) + "][1]");
                        equation = equation.replace("pixel", "pixel" + (numbers.get(j)) + "[i]");
                        equation = equation.replace("irrad_espectral", "calibration[" + (j) + "][2]");
                        if (eq.getIndex() != null) {
                            equation = equation.replace("reflectancia", "bandaRefletida" + numbers.get(j) + "[i]");
                        } else {
                            equation = equation.replace("reflectancia", "bandaRefletida" + numbers.get(j));
                        }
                        if (rad_espectral) {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j));
                        } else {
                            equation = equation.replace("rad_espectral", "banda" + numbers.get(j) + "[i]");
                        }
                        source.append(equation).append("\n");
                        variables.add("bandaRefletida" + numbers.get(j));

                        if (albedo) {
                            if (eq.getIndex() != null) {
                                source.append(ident).append("            sumBandas += parameterAlbedo[").append(j).append("]*bandaRefletida").append(numbers.get(j)).append("[i];\n");
                            } else {
                                source.append(ident).append("            sumBandas += parameterAlbedo[").append(j).append("]*bandaRefletida").append(numbers.get(j)).append(";\n");
                            }
                        }
                    }
                    break;
                default:
                    equation = ident + "            " + lexer.analyse(equation, structure, null, LanguageType.JAVA) + ";\n";
                    ex.evaluateExpr(equation);
                    outEquation = ex.getOutput();
//                    System.out.println("eq:"+equation);
                    if (eq.getIndex() != null) {
                        equation = ident + "            " + eq.getTerm() + "[i] = ";
                    } else {
                        equation = ident + "            " + eq.getTerm() + " = ";
                    }

                    for (int j = 0; j < outEquation.length; j++) {
                        String string = outEquation[j];
                        if (parameters.get(string) != null) {
                            string = string + "[i]";
                        } else {
                            for (int k = 0; k < i; k++) {
                                eq2 = equations.get(k);
                                t = eq2.getTerm().replace(" ", "");
//                            System.out.println("T:"+t);
                                if (t.equals(string)) {
                                    if (eq2.getIndex() != null) {
                                        string = t + "[i]";
                                    } else {
                                        string = t;
                                    }
                                    break;
                                } else if (string.equals("~")) {
                                    string = "-";
                                } else if (t.contains("banda") && t.contains(string)) {
                                    if (eq2.getIndex() != null) {
                                        string = string + "[i]";
                                    }
                                    break;
                                }
                            }
                        }
                        equation += string;
                    }
                    equation += "\n\n";
                    variables.add(eq.getTerm());
                    source.append(equation);
                    break;
            }
            if (eq.getCondition() != null) {
                source.append("            }\n\n");
            }
        }


        if (index != null) {
            String ts = "Ts";
            String rn = "Rn", g = "G0", savi = "SAVI";
            for (int i = 0; i < equations.size(); i++) {
                eq = equations.get(i);
                if (eq.getIndex() != null) {
                    switch (eq.getTerm()) {
                        case "Ts":
                            ts = "Ts[i]";
                            break;
                        case "Rn":
                            rn = "Rn[i]";
                            break;
                        case "G0":
                            g = "G0[i]";
                            break;
                        case "SAVI":
                            savi = "SAVI[i]";
                            break;
                    }
                }

            }

            source.append(
                    "            if (index > indexMax) {\n"
                    + "                if (" + ts + " < tMin) {\n"
                    + "                    tMin = " + ts + ";\n"
                    + "                    indexMax = index;\n"
                    + "                }\n"
                    + "            } else if (index < indexMin) {\n"
                    + "                if (" + ts + " > tMax) {\n"
                    + "                    tMax = " + ts + ";\n"
                    + "                    indexMin = index;\n"
                    + "                    RnHot = " + rn + ";\n"
                    + "                    SAVI_hot = " + savi + ";\n"
                    + "                    GHot = " + g + ";\n"
                    + "                }\n"
                    + "            }\n");
        }



        source.append("        }\n");

        if (index != null) {
            source.append("        float[] coef = new float[2];\n"
                    + "        GenericSEB.calculaAB(coef, RnHot, GHot, Uref, SAVI_hot, tMax, tMin);\n");
            source.append("        ret.put(\"coef\",coef);\n\n");
        }

        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");
        System.out.println(source.toString());
//        System.exit(1);
        return source.toString();
    }

    public static void main(String[] args) {
        // TODO code application logic here

        List<Value> parameters = new ArrayList<>();
        parameters.add(new Value("pixel21", new float[]{1.0f, 2.0f}));
        parameters.add(new Value("pixel3", new float[]{1.0f, 2.0f}));
        parameters.add(new Value("pixel4", new float[]{1.0f, 2.0f}));
        parameters.add(new Value("pixel5", new float[]{1.0f, 2.0f}));
        parameters.add(new Value("pixel6", new float[]{1.0f, 2.0f}));
        parameters.add(new Value("pixel7", new float[]{1.0f, 2.0f}));
        parameters.add(new Value("pixel8", new float[]{1.0f, 2.0f}));


        Map<String, Float> constants = new HashMap<>();
        constants.put("reflectanciaAtmosfera", 0.03f);
        constants.put("Kt", 1.0f);
        constants.put("L", 0.1f);
        constants.put("K1", 607.76f);
        constants.put("K2", 1260.56f);
        constants.put("S", 1367.0f);
        constants.put("StefanBoltzman", (float) (5.67 * Math.pow(10, -8)));
        constants.put("julianDay", 248.0f);
        constants.put("Z", 50.24f);
        constants.put("P", 99.3f);
        constants.put("UR", 36.46f);
        constants.put("Ta", 32.74f);
        constants.put("latitude", -16.56f);
        constants.put("Rg_24h", 243.949997f);
        constants.put("Uref", 0.92071358f);
        constants.put("Tao_24h", 0.63f);

        Map<String, float[][]> constMatrix = new HashMap<>();

        constMatrix.put("calibration", new float[][]{
            {-1.52f, 193.0f, 1957.0f},
            {-2.84f, 365.0f, 1826.0f},
            {-1.17f, 264.0f, 1554.0f},
            {-1.51f, 221.0f, 1036.0f},
            {-0.37f, 30.2f, 215.0f},
            {1.2378f, 15.303f, 1.0f},
            {-0.15f, 16.5f, 80.67f}});

        Map<String, float[]> constVetor = new HashMap<>();
        constVetor.put("parameterAlbedo", new float[]{0.293f, 0.274f, 0.233f, 0.157f, 0.033f, 0.0f, 0.011f});

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
                + "O_IAF_(SAVI <= 0.1) = 0\n"
                + "O_IAF_(SAVI >= 0.687) = 6\n"
                + "O_emissividadeNB = 0.97 + 0.0033 * IAF\n"
                + "O_emissividadeNB_(IAF >= 3) = 0.98\n"
                + "O_emissividadeNB_(NDVI <= 0) = 0.99\n"
                + "O_emissivity = 0.95 + 0.01 * IAF\n"
                + "O_emissivity_(IAF >= 3) = 0.98\n"
                + "O_emissivity_(NDVI <= 0) = 0.985\n"
                + "O_Ts = K2/ln(((emissividadeNB * K1) / banda6) + 1.0)\n"
                + "O_LWd = emissivity * StefanBoltzman * (pow(Ts, 4))\n"
                + "O_Rn = ((1.0 - albedo) * SWd) + (emissivity * (LWdAtm) - LWd)\n"
                + "O_G0 = Rn * (((Ts - T0) / albedo) * (0.0038 * albedo + 0.0074 * albedo * albedo) * (1.0 - 0.98 * pow(NDVI, 4)))\n"
                + "index = (0.5) * ((2.0 * bandaRefletida4 + 1) - sqrt((pow((2 * bandaRefletida4 + 1), 2) - 8 * (bandaRefletida4 - bandaRefletida3))))\n"
                + "O_z0m=exp(-5.809+5.62*SAVI)\n"
                + "O_SWnet=(1.0 - albedo) * SWd\n"
                + "O_U_star= k * Uref / ln(z200 / z0m)\n"
                + "O_r_ah=ln(z2 / z1) / (U_star * k)\n"
                + "O_H = p * cp * (b + a * (Ts - T0)) / r_ah\n"
                + "O_LE = Rn - H - G0\n"
                + "O_evap_fr = LE / (Rn - G0)\n"
                + "O_Rn_24h= Rg_24h * (1.0 - albedo) - 110.0 * Tao_24h\n"
                + "O_LE_24h= evap_fr * Rn_24h\n"
                + "O_ET_24h = (evap_fr * Rn_24h * 86.4) / 2450.0";

        GenericSEB g = new GenericSEB(LanguageType.JAVA);
        try {
            g.execute(header, body, parameters, constants, constVetor, constMatrix);
        } catch (Exception ex) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void calculaAB(float[] coeficientes, float Rn_hot, float G_hot, float Uref, float SAVI_hot, float Ts_hot, float Ts_cold) {

        float z0m = (float) Math.exp(-5.809f + 5.62f * SAVI_hot);

        float U_star = (float) (Constants.k * Uref / Math.log(Constants.z200 / z0m));

        float r_ah = (float) (Math.log(Constants.z2 / Constants.z1) / (U_star * Constants.k));

        float H_hot = Rn_hot - G_hot;

        float a = 0.0f;
        float b = 0.0f;

        float L;

        float tm_200;
        float th_2;
        float th_0_1;

        float errorH = 10.0f;
        int step = 1;
        float r_ah_anterior;
        float H = H_hot;

        while (errorH > Constants.MaxAllowedError && step < 100) {

            a = ((H) * r_ah) / (Constants.p * Constants.cp * (Ts_hot - Ts_cold));
            b = -a * (Ts_cold - Constants.T0);


            H = Constants.p * Constants.cp * (b + a * (Ts_hot - Constants.T0)) / r_ah;

            L = (float) (-(Constants.p * Constants.cp * U_star * U_star * U_star * (Ts_hot)) / (Constants.k * Constants.g * H));

            tm_200 = Psim(L);
            th_2 = Psih(Constants.z2, L);
            th_0_1 = Psih(Constants.z1, L);

            U_star = (float) (Constants.k * Uref / (Math.log(Constants.z200 / z0m) - tm_200));
            r_ah_anterior = r_ah;
            r_ah = (float) ((Math.log(Constants.z2 / Constants.z1) - th_2 + th_0_1) / (U_star * Constants.k));

            errorH = Math.abs(((r_ah - r_ah_anterior) * 100) / r_ah);

            step++;
        }

//        System.out.println("Total de Interações:" + step);
        coeficientes[0] = a;
        coeficientes[1] = b;

    }

    protected static float X(float Zref_m, float L) {
        return (float) (Math.sqrt(Math.sqrt((1.0f - 16.0f * Zref_m / L))));
    }

    protected static float Psim(float L) {
        if (L < 0.0f) {
            /* unstable */
            float x200 = X(200, L);
            return (float) (2.0f * Math.log((1.0f + x200) / 2.0f) + Math.log((1.0f + x200 * x200) / (2.0f)) - 2.0f * Math.atan(x200) + 0.5f * Math.PI);
        } else if (L > 0.0f) {
            /* stable */
            return (-5 * (2 / L));
        } else {
            return (0);
        }
    }

    protected static float Psih(float Zref_h, float L) {
        if (L < 0.0f) {
            /* unstable */
            float x = X(Zref_h, L);
            return (float) (2.0f * Math.log((1.0f + x * x) / 2.0f));
        } else if (L > 0.0f) {
            /* stable */
            return (-5 * (2 / L));
        } else {
            return (0);
        }
    }
}