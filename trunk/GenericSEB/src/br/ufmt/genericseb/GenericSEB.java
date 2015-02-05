/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

import br.ufmt.genericlexerseb.ExpressionParser;
import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    private static final long MEGABYTE = 1024L * 1024L;
    private LanguageType language;
    private Object[] pars;
    private Class[] classes;
    private List<Equation> equations;
    private Equation index;
    private IndexEnum indexEnum;
    private Integer indexSEBALLine = null;
    private static final String[] varsIndexSEBTA = new String[]{"maxTsVet", "minIndexVet", "minTsVet", "maxIndexVet", "rnHotVet", "gHotVet", "saviHotVet"};
    private static final String[] varsIndexSEBAL = new String[]{"maxIndexVet", "minIndexVet", "rnHotVet", "gHotVet", "saviHotVet"};
    private static final String[] varsIndexSSEB = new String[]{"maxTsVet", "minIndexVet", "minTsVet", "maxIndexVet"};
    private static final String[] varsIndexSSEBI = new String[]{"indexMax", "indexVet", "TsVet", "TsMax", "indexMinBaixoVet", "TsMinEsqVet"};

    public GenericSEB(LanguageType language) {
        this.language = language;
    }

    public GenericSEB() {
        this(LanguageType.JAVA);
    }

    public static void calcularMemoria(String text) {
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        mem /= MEGABYTE;
        System.out.println(text + ":" + mem);
    }

    public boolean verifyEquations(String forms, List<String> variables, boolean verifyIF) throws Exception {

        if (forms == null || forms.isEmpty()) {
            throw new Exception("Equations are empty!");
        }
        if (verifyIF) {
            equations = new ArrayList<Equation>();
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
            if (!line.startsWith("//")) {
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
                        throw new RuntimeException("forVariables can't not have conditions");
                    }
//                System.exit(1);
                }
                line = vet[0] + "=" + vet[1];
                if (verifyIF) {
                    equation.setTerm(vet[0]);
                    equation.setForm(vet[1]);
                    if (isIndex(vet[0])) {
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

        }

        return false;
    }

    private boolean isIndex(String index) {
        boolean ret = false;
        if (index.equals("sebta")) {
            indexEnum = IndexEnum.SEBTA;
            ret = true;
        } else if (index.equals("ssebi")) {
            indexEnum = IndexEnum.SSEBI;
            ret = true;
        } else if (index.equals("sseb")) {
            indexEnum = IndexEnum.SSEB;
            ret = true;
        } else if (index.equals("sebal")) {
            indexEnum = IndexEnum.SEBAL;
            ret = true;
        }
        return ret;
    }

    public static List<String> getVariables() {
        List<String> variables = new ArrayList<String>();

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

    public Map<String, float[]> execute(String forVariables, String forEachValue, List<VariableValue> parameters, Map<String, Float> constants) throws Exception {
        return execute(forVariables, forEachValue, parameters, constants, null, null);
    }

    public Map<String, float[]> execute(String forVariables, String forEachValue, List<VariableValue> parameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {
//        calcularMemoria("Memoria1");
        long total = System.currentTimeMillis();
        long tempo;
        Map<String, float[]> ret = new HashMap<String, float[]>();
        Map<String, float[]> firstRet = null;
        String source = null;
        String sourceIndex = null;

        StringBuilder newBodyWithIndex = new StringBuilder();
        StringBuilder newBodyWithoutIndex = new StringBuilder();

        String vet[];
        String vets[] = forEachValue.split("\n");
        boolean hasIndex = false;
        boolean isLast = false;

        String line;
        Integer sebalIndex = null;
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
                } else {
                    if (!isIndex(vet[0])) {
                        newBodyWithoutIndex.append(vets[i]).append("\n");
                    } else if (indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SEBTA)) {
                        sebalIndex = i;
                    }
                }
            } else {
                newBodyWithoutIndex.append(vets[i]).append("\n");
            }

            if (isIndex(vet[0])) {
                hasIndex = true;
                isLast = (i == vets.length - 1);
            }
        }
//        System.out.println(newBodyWithIndex.toString());
//        System.out.println("--------------------");
//        System.out.println(newBodyWithoutIndex.toString());
//        System.exit(1);
        String exec = forEachValue;
        firstRet = new HashMap<String, float[]>();
        long compilacao = 0;
        long criacao = 0;
        long execucao = 0;
        if (hasIndex && !isLast) {
//            System.out.println("Index:" + newBodyWithoutIndex.toString());
            exec = newBodyWithoutIndex.toString();

            criacao = System.currentTimeMillis();
            if (language.equals(LanguageType.CUDA)) {
                sourceIndex = generateCUDA(forVariables, newBodyWithIndex.toString(), parameters, constants, constantsVetor, constantsMatrix);
            } else if (language.equals(LanguageType.OPENCL) || language.equals(LanguageType.OPENCL_CPU)) {
                sourceIndex = generateOpenCL(forVariables, newBodyWithIndex.toString(), parameters, constants, constantsVetor, constantsMatrix);
            } else {
                sourceIndex = generateJava(forVariables, newBodyWithIndex.toString(), parameters, constants, constantsVetor, constantsMatrix);
            }
            criacao = System.currentTimeMillis() - criacao;

            compilacao = System.currentTimeMillis();
            Object instanced = compile(sourceIndex, "Equation");
            compilacao = System.currentTimeMillis() - compilacao;
            try {
                execucao = System.currentTimeMillis();
                Method method = instanced.getClass().getDeclaredMethod("execute", classes);
                firstRet = (Map<String, float[]>) method.invoke(instanced, pars);
                execucao = System.currentTimeMillis() - execucao;
                if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL)) {
//                    constants.put("a", firstRet.get("coef")[0]);
//                    constants.put("b", firstRet.get("coef")[1]);

                    constants.put("indexMax", firstRet.get("indexMax")[0]);
                    constants.put("RnHot", firstRet.get("RnHot")[0]);
                    constants.put("SAVI_hot", firstRet.get("SAVI_hot")[0]);
                    constants.put("GHot", firstRet.get("GHot")[0]);
                    constants.put("indexMin", firstRet.get("indexMin")[0]);

//                    constants.put("z0m", 0f);
//                    constants.put("U_star", 0f);
//                    constants.put("H", 0f);
//                    constants.put("r_ah", 0f);
                    exec = "z0m=0\n" + "U_star=0\n" + "H=0\n" + "r_ah=0\n" + exec;
//                    constants.put("r_ah", 0f);
//                    constants.put("r_ah", 0f);
//                    constants.put("r_ah", 0f);

                } else if (indexEnum.equals(IndexEnum.SSEB)) {
                    constants.put("TH", firstRet.get("TH")[0]);
                    constants.put("TC", firstRet.get("TC")[0]);
                } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                    constants.put("aH", firstRet.get("aH")[0]);
                    constants.put("bH", firstRet.get("bH")[0]);
                    constants.put("aLE", firstRet.get("aLE")[0]);
                    constants.put("bLE", firstRet.get("bLE")[0]);
                }

                index = null;
                indexEnum = null;
                indexSEBALLine = sebalIndex;
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
        }

//        System.out.println(exec);
        tempo = System.currentTimeMillis();
        if (language.equals(LanguageType.CUDA)) {
            source = generateCUDA(forVariables, exec, parameters, constants, constantsVetor, constantsMatrix);
        } else if (language.equals(LanguageType.OPENCL) || language.equals(LanguageType.OPENCL_CPU)) {
            source = generateOpenCL(forVariables, exec, parameters, constants, constantsVetor, constantsMatrix);
        } else {
            source = generateJava(forVariables, exec, parameters, constants, constantsVetor, constantsMatrix);
        }
        tempo = System.currentTimeMillis() - tempo;
        criacao += tempo;
//
        tempo = System.currentTimeMillis();
//        calcularMemoria("Memoria2");
        Object instanced = compile(source, "Equation");
        tempo = System.currentTimeMillis() - tempo;
        compilacao += tempo;
        try {
//            System.out.println("source:" + exec);
//            tempo = System.currentTimeMillis();
            Method method = instanced.getClass().getDeclaredMethod("execute", classes);
////            calcularMemoria("Memoria3");
            ret = (Map<String, float[]>) method.invoke(instanced, pars);
////            calcularMemoria("Memoria4");
//            tempo = System.currentTimeMillis() - tempo;
//            execucao += tempo;
            if (hasIndex && !isLast) {
                ret.putAll(firstRet);
            }
            pars = null;
            classes = null;
            equations.clear();
            index = null;
            indexSEBALLine = null;
            indexEnum = null;
//        } catch (NoSuchMethodException ex1) {
//            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (SecurityException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
//        } catch (IllegalAccessException ex1) {
//            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (IllegalArgumentException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
//        } catch (InvocationTargetException ex1) {
//            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        }

//        System.out.println("Configuracao:" + conf);
        System.out.println("Criacao:" + criacao);
        System.out.println("Compilacao:" + compilacao);
        System.out.println("Execucao:" + execucao);
        System.out.println("Total:" + (System.currentTimeMillis() - total));
        return ret;
    }

    public Object compile(String source, String className) {
        try {
            PrintWriter fonte = new PrintWriter(className + ".java");
            fonte.println(source);
            fonte.close();
            URLClassLoader ucl;
            URL url;
            StringBuilder options = new StringBuilder();
            URL[] urls = new URL[1];
            url = new URL("file:" + System.getProperty("user.dir") + "/lib/");
            boolean isLib = false;
            try {
                File lib = new File(url.toURI());
                isLib = lib.exists();
                if (isLib) {
//                System.out.println("file:" + System.getProperty("user.dir") + "/lib/");
                    url = new URL("file:" + System.getProperty("user.dir") + "/lib/");
                    File dir = new File(url.toURI());
                    File[] jars = dir.listFiles();
                    urls = new URL[jars.length + 1];
                    for (int i = 0; i < jars.length; i++) {
                        File file = jars[i];
                        options.append(file.getPath()).append(":");
                        urls[i + 1] = jars[i].toURI().toURL();
                    }
                }

            } catch (SecurityException ex) {
                Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
            }
            options.append(".");

//            System.out.println("javac -cp " + options.toString() + " " + className + ".java");
            int compilar;
            if (isLib) {
                compilar = com.sun.tools.javac.Main.compile(new String[]{"-cp", options.toString(), className + ".java"});
            } else {
                compilar = com.sun.tools.javac.Main.compile(new String[]{className + ".java"});
            }
            File arq = new File(className + ".java");
//            arq.delete();
            if (compilar == 0) {
                url = new URL("file:" + System.getProperty("user.dir") + "/");
                urls[0] = url;

                ucl = URLClassLoader.newInstance(urls);
//                ucl.loadClass("br.ufmt.genericseb.Constants");
//                ucl.loadClass("br.ufmt.genericseb.GenericSEB");
//                ucl.loadClass("br.ufmt.genericlexerseb.Maths");
                Class classe = ucl.loadClass(className);
                Object instancia = classe.newInstance();
//                System.out.println("instancia:" + instancia);
//                System.out.println("OK");
//                System.exit(1);
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

    private String generateCUDA(String forVariables, String forEachValue, List<VariableValue> parametersList, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {

        List<String> variables = getVariables();
        ExpressionParser ex = new ExpressionParser();

        StringBuilder source = new StringBuilder();
        source.append("import static br.ufmt.genericseb.Constants.*;\n");
        source.append("import br.ufmt.genericseb.Constants;\n");
        source.append("import java.util.HashMap;\n");
        source.append("import java.util.Map;\n");
        source.append("import br.ufmt.jedigpu.ParameterGPU;\n");
        source.append("import java.util.ArrayList;\n");
        source.append("import br.ufmt.jedigpu.JSeriesCUDA;\n");
        source.append("import java.io.File;\n");
        source.append("import java.io.IOException;\n");
        source.append("import java.util.logging.Level;\n");
        source.append("import java.util.logging.Logger;\n");
        source.append("import br.ufmt.genericseb.GenericSEB;\n");
        source.append("import br.ufmt.genericlexerseb.Maths;\n");
        source.append("import java.util.List;\n\n");

        source.append("public class Equation{\n");

        source.append("    public Map<String, float[]> execute(");
        int size = 0;
        String vet1 = null;
        Map<String, VariableValue> parameters = new LinkedHashMap<String, VariableValue>();
        pars = new Object[parametersList.size()];
        classes = new Class[parametersList.size()];
        List<Integer> numbers = new ArrayList<Integer>();
        for (VariableValue value : parametersList) {
            String string = value.getName();
            parameters.put(string, value);
            if (string.startsWith("pixel")) {
                if (string.replace("pixel", "").matches("[0-9]+")) {
                    numbers.add(Integer.parseInt(string.replace("pixel", "")));
                } else {
                    throw new Exception("Miss number pixel name");
                }
            }
            if (vet1 == null) {
                vet1 = string;
            }
            pars[size] = value.getData();
            classes[size] = value.getData().getClass();
            source.append(classes[size].getCanonicalName()).append(" ").append(string);
            size++;
            if (size < parametersList.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL)) {
                source.append("        float Uref = ").append(constants.get("Uref")).append("f;\n");
            }

            source.append("        int width = (int)").append(constants.get("width")).append("f;\n");
            source.append("        int height = (int)").append(constants.get("height")).append("f;\n");

            int tam = (indexEnum.equals(IndexEnum.SSEBI) ? 40 : 3);
            source.append("        int col = " + tam + ";\n"
                    + "        int line = 5000000/col;\n"
                    + "        int w = 40*col;\n"
                    + "\n"
                    + "        int total = width * height;\n"
                    + "\n"
                    + "        for (int i = w; i >= 1; i--) {\n"
                    + "            if (total % i == 0 && (total / i) < line) {\n"
                    + "                height = i;\n"
                    + "                width = total / i;\n"
                    + "                break;\n"
                    + "            }\n"
                    + "        }\n");
        }

        source.append("        Map<String, float[]> ret = new HashMap<String, float[]>();\n\n");
        source.append("        List<ParameterGPU> par = new ArrayList<ParameterGPU>();\n\n");

        int sizeofNumber = 0;
        boolean first = true;
        for (String string : parameters.keySet()) {
            sizeofNumber++;
            if (first) {
                if (indexEnum != null) {
                    if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL)) {
                        source.append("        int[] N = new int[]{").append(string).append(".length, width, height};\n\n");
                    } else if (indexEnum.equals(IndexEnum.SSEBI) || indexEnum.equals(IndexEnum.SSEB)) {
                        source.append("        int[] N = new int[]{").append(string).append(".length, width, height, col};\n\n");
                    }
                } else {
                    source.append("        int[] N = new int[]{").append(string).append(".length};\n\n");
                }
                if (indexEnum == null) {
                    source.append("        par.add(new ParameterGPU(").append(string).append(",true,false,true));\n");
                } else {
                    source.append("        par.add(new ParameterGPU(").append(string).append(",true));\n");
                }
                first = false;
            } else {
                source.append("        par.add(new ParameterGPU(").append(string).append(",true));\n");
            }
        }

        source.append("\n");

        //DECLARANDO OS VETORES QUE SERAO RETORNADOS
        String[] vet = forEachValue.split("\n");
        String term;
        boolean vector;
        Set<String> variablesDeclared = new HashSet<String>();
        for (int i = 0; i < vet.length; i++) {
            if (vet[i].startsWith("rad_espectral") || vet[i].startsWith("O_rad_espectral")) {
                if (variablesDeclared.add("banda")) {
                    variables.add("pixel");
                    vector = vet[i].startsWith("O_rad_espectral");
                    for (int j = 0; j < numbers.size(); j++) {
                        if (vector) {
                            sizeofNumber++;
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
                            sizeofNumber++;
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
        
        if (indexSEBALLine != null) {
            gpuCode.append("    __device__ float X(float Zref_m, float L) {\n"
                    + "        return (float) (sqrtf(sqrtf((1.0f - 16.0f * Zref_m / L))));\n"
                    + "    }\n"
                    + "\n"
                    + "    __device__ float Psim(float L) {\n"
                    + "        if (L < 0.0f) {\n"
                    + "            /* unstable */\n"
                    + "            float x200 = X(200, L);\n"
                    + "            return (float) (2.0f * logf((1.0f + x200) / 2.0f) + logf((1.0f + x200 * x200) / (2.0f)) - 2.0f * atanf(x200) + 0.5f * pi);\n"
                    + "        } else if (L > 0.0f) {\n"
                    + "            /* stable */\n"
                    + "            return (-5 * (2 / L));\n"
                    + "        } else {\n"
                    + "            return (0);\n"
                    + "        }\n"
                    + "    }\n"
                    + "\n"
                    + "    __device__ float Psih(float Zref_h, float L) {\n"
                    + "        if (L < 0.0f) {\n"
                    + "            /* unstable */\n"
                    + "            float x = X(Zref_h, L);\n"
                    + "            return (float) (2.0f * logf((1.0f + x * x) / 2.0f));\n"
                    + "        } else if (L > 0.0f) {\n"
                    + "            /* stable */\n"
                    + "            return (-5 * (2 / L));\n"
                    + "        } else {\n"
                    + "            return (0);\n"
                    + "        }\n"
                    + "    }\n\n");
        }
        
        gpuCode.append("extern \"C\"{\n\n");

        StringBuilder gpuCodeBody = new StringBuilder("    __global__ void execute(\n");

        if (constants != null) {
            for (String string : constants.keySet()) {
                gpuCode.append("    #define ").append(string).append(" ").append(constants.get(string)).append("f\n");
                variables.add(string);
            }
        }
        gpuCode.append("\n");

        vet = forVariables.split("\n");
        GenericLexerSEB lexer = new GenericLexerSEB();
        Structure structure;
        Map<String, Float> vars = new HashMap<String, Float>();
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
        VariableValue value;
        String type;

        gpuCode.append("    __device__ void execute_sub(\n");
        for (String string : parameters.keySet()) {
            value = parameters.get(string);
            type = value.getData().getClass().getCanonicalName().substring(0, value.getData().getClass().getCanonicalName().length() - 2);

            gpuCode.append("        ").append(type).append(" ").append(string).append(",\n");
            gpuCodeBody.append("        ").append(type).append(" * ").append(string).append(",\n");
            variables.add(string);
        }
        gpuCode.append("\n");
        gpuCodeBody.append("\n");

        vet = forEachValue.split("\n");
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

        if (forVariables != null && !forVariables.isEmpty()) {
            verifyEquations(forVariables, variables, false);
        }
        if (forEachValue != null && !forEachValue.isEmpty()) {
            verifyEquations(forEachValue, variables, true);
        }

        if (index != null) {
            equations.add(index);
        }

        Equation eq;
        StringBuilder cudaVariables = new StringBuilder();
        boolean firstComa = true;
        for (int i = 0; i < equations.size(); i++) {
            eq = equations.get(i);
            if (!eq.getTerm().endsWith("rad_espectral") && !eq.getTerm().endsWith("reflectancia")) {
                if (variablesDeclared.add(eq.getTerm())) {
                    if (eq.getIndex() != null) {
                        term = eq.getTerm();
                        if (!firstComa) {
                            gpuCode.append(",\n");
                        }
                        firstComa = false;
                        gpuCode.append("        float * ").append(term);
                        gpuCodeBody.append("        float * ").append(term);

                        sizeofNumber++;
                        source.append("        float[] ").append(term).append(" = new float[").append(vet1).append(".length];\n");
                        source.append("        par.add(new ParameterGPU(").append(term).append(",true,true));\n");
                        source.append("        ret.put(\"").append(term).append("\",").append(term).append(");\n\n");

                        variables.add(term);
                    } else {
                        cudaVariables.append("        float ").append(eq.getTerm()).append(" = 0.0f;\n");
                    }
                }

            }
        }
        if (indexEnum != null) {
            String[] varsIndex = null;
            if (indexEnum.equals(IndexEnum.SEBTA)) {
                varsIndex = varsIndexSEBTA;
            } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                varsIndex = varsIndexSEBAL;
            } else if (indexEnum.equals(IndexEnum.SSEB)) {
                varsIndex = varsIndexSSEB;
            } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                varsIndex = varsIndexSSEBI;
            }
            if (!firstComa) {
                gpuCode.append(",\n");
                gpuCodeBody.append(",\n");
                firstComa = true;
            }
            for (int i = 0; i < varsIndex.length; i++) {
                String string = varsIndex[i];
                if (indexEnum.equals(IndexEnum.SSEBI) || indexEnum.equals(IndexEnum.SSEB)) {
                    sizeofNumber++;
                    if (indexEnum.equals(IndexEnum.SSEBI)) {
                        source.append("        float[] ").append(string).append(" = new float[col];\n");
                    } else {
                        source.append("        float[] ").append(string).append(" = new float[col * width];\n");
                    }
                    if (string.toLowerCase().contains("min")) {
                        source.append("        for (int i = 0; i < " + string + ".length; i++) {\n");
                        source.append("             " + string + "[i] = Float.MAX_VALUE;\n");
                        source.append("        }\n");
                    } else if (string.toLowerCase().contains("max")) {
                        source.append("        for (int i = 0; i < " + string + ".length; i++) {\n");
                        source.append("             " + string + "[i] = Float.MIN_VALUE;\n");
                        source.append("        }\n");
                    }
                } else {
                    source.append("        float[] ").append(string).append(" = new float[width];\n");
                }
                if (i == 0) {
                    if (!indexEnum.equals(IndexEnum.SSEB) && !indexEnum.equals(IndexEnum.SSEBI)) {
                        source.append("\n        par.add(new ParameterGPU(").append(string).append(", true, true, true));\n");
                    } else {
                        source.append("\n        par.add(new ParameterGPU(").append(string).append(", true, true));\n");
                    }
                } else {
                    source.append("        par.add(new ParameterGPU(").append(string).append(", true, true));\n");
                }
                source.append("\n");

                gpuCode.append("        float * ").append(string);
                gpuCodeBody.append("        float * ").append(string).append(",\n");
                if (i + 1 < varsIndex.length) {
                    gpuCode.append(",");
                }
                gpuCode.append("\n");
            }
        }
        if (indexEnum != null && (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI))) {
            source.append("        short[] gab = new short[width];\n"
                    + "        par.add(new ParameterGPU(gab, true, false, true));\n");
            source.append("        short[] gab2 = new short[col];\n"
                    + "        par.add(new ParameterGPU(gab2, true, false, true, false));\n");

        }
        source.append("        par.add(new ParameterGPU(N,true,false,false,false));\n\n");

        source.append("        String pathNvcc = \"/usr/local/cuda/bin/\";\n");
        source.append("        String source = \"code.cu\";\n");
        source.append("        try {\n");
        source.append("            JSeriesCUDA cuda = new JSeriesCUDA();\n");
        source.append("            cuda.setPathNvcc(pathNvcc);\n");
//        source.append("            cuda.setPrint(true);\n");
//        source.append("            cuda.setMeasure(true);\n");
        source.append("            cuda.execute(par, System.getProperty(\"user.dir\") + \"/source/\" + source, \"execute\");\n");
        source.append("            File newFile = new File(System.getProperty(\"user.dir\") + \"/source/\" + source);\n");
        source.append("            //newFile.delete();\n");
        source.append("        } catch (IOException ex) {\n");
        source.append("            Logger.getLogger(Equation.class.getName()).log(Level.SEVERE, null, ex);\n");
        source.append("        }\n");

        if (index != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL)) {

                source.append(
                        "        float maxIndex = maxIndexVet[0];\n"
                        + "        float minIndex = minIndexVet[0];\n"
                        + "        float rnHot = rnHotVet[0];\n"
                        + "        float gHot = gHotVet[0];\n"
                        + "        float saviHot = saviHotVet[0];\n");

                if (indexEnum.equals(IndexEnum.SEBTA)) {
                    source.append(
                            "        float maxTs = maxTsVet[0];\n"
                            + "        float minTs = minTsVet[0];\n");

                    source.append(
                            "        for (int i = 1; i < maxTsVet.length; i++) {\n"
                            + "            if (minIndexVet[i] <= minIndex) {\n"
                            + "                if (maxTsVet[i] >= maxTs) {\n"
                            + "                    maxTs = maxTsVet[i];\n"
                            + "                    minIndex = minIndexVet[i];\n"
                            + "                    rnHot = rnHotVet[i];\n"
                            + "                    gHot = gHotVet[i];\n"
                            + "                    saviHot = saviHotVet[i];\n"
                            + "                }\n"
                            + "            }\n"
                            + "\n"
                            + "            if (maxIndexVet[i] >= maxIndex) {\n"
                            + "                if (minTsVet[i] <= minTs) {\n"
                            + "                    minTs = minTsVet[i];\n"
                            + "                    maxIndex = maxIndexVet[i];\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }\n");
//                    source.append("        float[] coef = new float[2];\n"
//                            + "        GenericSEB.calculaAB(coef, rnHot, gHot, Uref, saviHot, maxTs, minTs);\n");
//                    source.append("        ret.put(\"coef\",coef);\n\n");

                    source.append("        ret.put(\"RnHot\",new float[]{rnHot});\n\n");
                    source.append("        ret.put(\"GHot\",new float[]{gHot});\n\n");
                    source.append("        ret.put(\"SAVI_hot\",new float[]{saviHot});\n\n");
                    source.append("        ret.put(\"indexMax\",new float[]{maxTs});\n\n");
                    source.append("        ret.put(\"indexMin\",new float[]{minTs});\n\n");

                } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                    source.append(
                            "        for (int i = 1; i < maxIndexVet.length; i++) {\n"
                            + "            if (minIndexVet[i] <= minIndex) {\n"
                            + "                minIndex = minIndexVet[i];\n"
                            + "            }\n"
                            + "\n"
                            + "            if (maxIndexVet[i] >= maxIndex) {\n"
                            + "                maxIndex = maxIndexVet[i];\n"
                            + "                rnHot = rnHotVet[i];\n"
                            + "                gHot = gHotVet[i];\n"
                            + "                saviHot = saviHotVet[i];\n"
                            + "            }\n"
                            + "        }\n");
//                    source.append("        float[] coef = new float[2];\n"
//                            + "        GenericSEB.calculaAB(coef, rnHot, gHot, Uref, saviHot, maxIndex, minIndex);\n");
//                    source.append("        ret.put(\"coef\",coef);\n\n");

                    source.append("        ret.put(\"RnHot\",new float[]{rnHot});\n\n");
                    source.append("        ret.put(\"GHot\",new float[]{gHot});\n\n");
                    source.append("        ret.put(\"SAVI_hot\",new float[]{saviHot});\n\n");
                    source.append("        ret.put(\"indexMax\",new float[]{maxIndex});\n\n");
                    source.append("        ret.put(\"indexMin\",new float[]{minIndex});\n\n");
                }

//                source.append("System.out.println(\"SEBTA CUDA\");");
//                source.append("System.out.println(rnHot);");
//                source.append("System.out.println(gHot);");
//                source.append("System.out.println(Uref);");
//                source.append("System.out.println(saviHot);");
//                source.append("System.out.println(maxIndex);");
//                source.append("System.out.println(minIndex);");
            } else if (indexEnum.equals(IndexEnum.SSEB)) {
                source.append("        float[] indexMax = new float[]{0.0f, 0.0f, 0.0f};\n"
                        + "        float[] indexMin = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};\n"
                        + "        float[] TsMax = new float[]{0.0f, 0.0f, 0.0f};\n"
                        + "        float[] TsMin = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};\n"
                        + "\n"
                        + "        for (int i = 0; i < maxTsVet.length; i ++) {\n"
                        + "            for (int idx = 0; idx < indexMax.length; idx++) {\n"
                        + "                if (maxIndexVet[i] >= indexMax[idx]) {\n"
                        + "                    if (minTsVet[i] <= TsMin[idx]) {\n"
                        + "                        for (int j = TsMin.length - 1; j > idx; j--) {\n"
                        + "                            TsMin[j] = TsMin[j - 1];\n"
                        + "                            indexMax[j] = indexMax[j - 1];\n"
                        + "                        }\n"
                        + "                        TsMin[idx] = minTsVet[i];\n"
                        + "                        indexMax[idx] = maxIndexVet[i];\n"
                        + "                        break;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "            }\n"
                        + "            for (int idx = 0; idx < indexMin.length; idx++) {\n"
                        + "                if (minIndexVet[i] <= indexMin[idx]) {\n"
                        + "                    if (maxTsVet[i] >= TsMax[idx]) {\n"
                        + "                        for (int j = TsMax.length - 1; j > idx; j--) {\n"
                        + "                            TsMax[j] = TsMax[j - 1];\n"
                        + "                            indexMin[j] = indexMin[j - 1];\n"
                        + "                        }\n"
                        + "                        TsMax[idx] = maxTsVet[i];\n"
                        + "                        indexMin[idx] = minIndexVet[i];\n"
                        + "                        break;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "\n"
                        + "        float TC = 0.0f;\n"
                        + "        float TH = 0.0f;\n"
                        + "        for (int i = 0; i < indexMax.length; i++) {\n"
                        + "            TC += TsMin[i];\n"
                        + "            TH += TsMax[i];\n"
                        + "        }\n"
                        + "        TC = TC / indexMax.length;\n"
                        + "        TH = TH / indexMax.length;\n"
                        //                        + "        System.out.println(\"SSEB CUDA\");\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(indexMax));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(TsMin));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(indexMin));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(TsMax));\n"
                        //                        + "        System.out.println(TC);\n"
                        //                        + "        System.out.println(TH);\n"
                        + "        ret.put(\"TC\", new float[]{TC});\n"
                        + "        ret.put(\"TH\", new float[]{TH});\n\n");
            } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                source.append("        float mediaSupEsqTs = 0.0f;\n"
                        + "        float mediaSupDirTs = 0.0f;\n"
                        + "        float mediaInfEsqTs = 0.0f;\n"
                        + "        float mediaInfDirTs = 0.0f;\n"
                        + "\n"
                        + "        float mediaSupEsqIndex = 0.0f;\n"
                        + "        float mediaSupDirIndex = 0.0f;\n"
                        + "        float mediaInfEsqIndex = 0.0f;\n"
                        + "        float mediaInfDirIndex = 0.0f;\n"
                        + "\n"
                        + "        float[] indexMaxCima = new float[10];\n"
                        + "        float[] indexMaxBaixo = new float[10];\n"
                        + "        float[] indexMinCima = new float[10];\n"
                        + "	   float[] indexMinBaixo = new float[10];\n"
                        + "\n"
                        + "        float[] TsMaxDir = new float[10];\n"
                        + "        float[] TsMaxEsq = new float[10];\n"
                        + "        float[] TsMinDir = new float[10];\n"
                        + "	   float[] TsMinEsq = new float[10];\n"
                        + "\n"
                        + "        for (int i = 0; i < 10; i++) {\n"
                        + "            indexMinCima[i] = Float.MAX_VALUE;\n"
                        + "            TsMinDir[i] = Float.MAX_VALUE;\n"
                        + "            indexMinBaixo[i] = Float.MAX_VALUE;\n"
                        + "            TsMinEsq[i] = Float.MAX_VALUE;\n"
                        + "        }\n"
                        + "\n"
                        + "        for (int i = 0; i < indexMax.length; i++) {\n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (TsVet[i] >= TsMaxDir[idx]) {\n"
                        + "                    for (int j = TsMaxDir.length - 1; j > idx; j--) {\n"
                        + "                        TsMaxDir[j] = TsMaxDir[j - 1];\n"
                        + "                        indexMaxCima[j] = indexMaxCima[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMaxDir[idx] = TsVet[i];\n"
                        + "                    indexMaxCima[idx] = indexMax[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "            \n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (TsVet[i] <= TsMinDir[idx]) {\n"
                        + "                    for (int j = TsMinDir.length - 1; j > idx; j--) {\n"
                        + "                        TsMinDir[j] = TsMinDir[j - 1];\n"
                        + "                        indexMaxBaixo[j] = indexMaxBaixo[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMinDir[idx] = TsVet[i];\n"
                        + "                    indexMaxBaixo[idx] = indexMax[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "            \n"
                        + "            for (int idx = 0; idx < TsMaxEsq.length; idx++) {\n"
                        + "                if (indexVet[i] <= indexMinCima[idx]) {\n"
                        + "                    for (int j = TsMaxEsq.length - 1; j > idx; j--) {\n"
                        + "                        TsMaxEsq[j] = TsMaxEsq[j - 1];\n"
                        + "                        indexMinCima[j] = indexMinCima[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMaxEsq[idx] = TsMax[i];\n"
                        + "                    indexMinCima[idx] = indexVet[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (indexMinBaixoVet[i] <= indexMinBaixo[idx]) {\n"
                        + "		     if (TsMinEsqVet[i] <= TsMinEsq[idx]) {\n"
                        + "                    for (int j = TsMinEsq.length - 1; j > idx; j--) {\n"
                        + "                        TsMinEsq[j] = TsMinEsq[j - 1];\n"
                        + "                        indexMinBaixo[j] = indexMinBaixo[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMinEsq[idx] = TsMinEsqVet[i];\n"
                        + "                    indexMinBaixo[idx] = indexMinBaixoVet[i];\n"
                        + "                    break;\n"
                        + "		     }\n"
                        + "                }\n"
                        + "            }"
                        + "        }\n"
                        + "\n"
                        + "        for (int i = 0; i < TsMinEsq.length; i++) {\n"
                        + "            mediaSupEsqTs += TsMaxEsq[i];\n"
                        + "            mediaSupDirTs += TsMaxDir[i];\n"
                        + "            mediaInfEsqTs += TsMinEsq[i];\n"
                        + "            mediaInfDirTs += TsMinDir[i];\n"
                        + "\n"
                        + "            mediaSupEsqIndex += indexMinCima[i];\n"
                        + "            mediaSupDirIndex += indexMaxCima[i];\n"
                        + "            mediaInfEsqIndex += indexMinBaixo[i];\n"
                        + "            mediaInfDirIndex += indexMaxBaixo[i];\n"
                        + "        }\n"
                        + "\n"
                        + "        mediaSupEsqTs /= TsMaxEsq.length;\n"
                        + "        mediaSupDirTs /= TsMaxDir.length;\n"
                        + "        mediaInfEsqTs /= TsMinEsq.length;\n"
                        + "        mediaInfDirTs /= TsMinDir.length;\n"
                        + "\n"
                        + "        mediaSupEsqIndex /= indexMinCima.length;\n"
                        + "        mediaSupDirIndex /= indexMaxCima.length;\n"
                        + "        mediaInfEsqIndex /= indexMinBaixo.length;\n"
                        + "        mediaInfDirIndex /= indexMaxBaixo.length;\n"
                        //                        + "System.out.println(\"SSEBI CUDA\");\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMaxBaixo));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMinBaixo));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMaxCima));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMinCima));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMaxEsq));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMaxDir));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMinDir));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMinEsq));\n"
                        //                        + "            System.out.println(\"mediaSupEsqTs:\"+mediaSupEsqTs);\n"
                        //                        + "            System.out.println(\"mediaSupDirTs:\"+mediaSupDirTs);\n"
                        //                        + "            System.out.println(\"mediaInfEsqTs:\"+mediaInfEsqTs);\n"
                        //                        + "            System.out.println(\"mediaInfDirTs:\"+mediaInfDirTs);\n"
                        //                        + "            System.out.println(\"mediaSupEsqIndex:\"+mediaSupEsqIndex);\n"
                        //                        + "            System.out.println(\"mediaSupDirIndex:\"+mediaSupDirIndex);\n"
                        //                        + "            System.out.println(\"mediaInfEsqIndex:\"+mediaInfEsqIndex);\n"
                        //                        + "            System.out.println(\"mediaInfDirIndex:\"+mediaInfDirIndex);\n"
                        + "\n"
                        + "        float aH = 0.0f;\n"
                        + "        float aLE = 0.0f;\n"
                        + "        float bH = 0.0f;\n"
                        + "        float bLE = 0.0f;\n"
                        + "\n"
                        + "        bH = (mediaSupDirTs - mediaSupEsqTs) / (mediaSupDirIndex - mediaSupEsqIndex);\n"
                        + "        aH = (mediaSupEsqTs * mediaSupDirIndex - mediaSupDirTs * mediaSupEsqIndex) / (mediaSupDirIndex - mediaSupEsqIndex);\n"
                        + "\n"
                        + "        bLE = (mediaInfDirTs - mediaInfEsqTs) / (mediaInfDirIndex - mediaInfEsqIndex);\n"
                        + "        aLE = (mediaInfEsqTs * mediaInfDirIndex - mediaInfDirTs * mediaInfEsqIndex) / (mediaInfDirIndex - mediaInfEsqIndex);\n"
                        //                        + "System.out.println(\"ah:\"+aH);\n"
                        //                        + "        System.out.println(\"bH:\"+bH);\n"
                        //                        + "        System.out.println(\"aLE:\"+aLE);\n"
                        //                        + "        System.out.println(\"bLE:\"+bLE);\n"
                        + "        ret.put(\"aH\", new float[]{aH});\n"
                        + "        ret.put(\"bH\", new float[]{bH});\n"
                        + "\n"
                        + "        ret.put(\"aLE\", new float[]{aLE});\n"
                        + "        ret.put(\"bLE\", new float[]{bLE});\n\n");
            }
        }

        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");

        if (indexEnum != null && (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI))) {
            gpuCodeBody.append("        short * gab,\n");
            gpuCodeBody.append("        short * gab2,\n");
        }
        if (!firstComa) {
            gpuCodeBody.append(",\n");
            firstComa = true;
        }
        gpuCodeBody.append("        int * parameters");

        gpuCode.append("    ){\n\n");

        gpuCode.append(cudaVariables.toString());

        gpuCodeBody.append("){\n");
        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
//                gpuCodeBody.append("        int size = parameters[1];\n");
                gpuCodeBody.append("        int size = #SIZEOF" + sizeofNumber + "#;\n");

            }
        } else {
//            gpuCodeBody.append("        int size = parameters[0];\n");
            gpuCodeBody.append("        int size = #SIZEOF0#;\n");
        }
        gpuCodeBody.append("        int idx = blockIdx.x*blockDim.x + threadIdx.x;\n");
        gpuCodeBody.append("        int ind = idx;\n");

        if (indexEnum != null && (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI))) {
            gpuCodeBody.append("        int indY = blockIdx.y*blockDim.y + threadIdx.y;\n");
            gpuCodeBody.append("        if(idx < size && indY < parameters[3]){\n");
        } else {
            gpuCodeBody.append("        if(idx < size){\n");
        }

        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                String[] varsIndex = null;
                if (indexEnum.equals(IndexEnum.SEBTA)) {
                    varsIndex = varsIndexSEBTA;
                } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                    varsIndex = varsIndexSEBAL;
                } else if (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                    if (indexEnum.equals(IndexEnum.SSEB)) {
                        varsIndex = varsIndexSSEB;
                    } else {
                        varsIndex = varsIndexSSEBI;
                    }
                    gpuCodeBody.append("            int nind = ind;\n");
                    if (indexEnum.equals(IndexEnum.SSEB)) {
                        gpuCodeBody.append("            ind = ind*parameters[3]+indY;\n");
                    } else {
                        gpuCodeBody.append("            ind = indY;\n");
                    }
                }
                if (!(indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI))) {
                    for (int i = 0; i < varsIndex.length; i++) {
                        String string = varsIndex[i];
                        gpuCodeBody.append("            ").append(string).append("[ind]");
                        if (string.startsWith("min")) {
                            gpuCodeBody.append("=99999.0f;\n");
                        } else {
                            gpuCodeBody.append("=-99999.0f;\n");
                        }
                    }
                }

                if (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                    gpuCodeBody.append("            for(int i=indY;i<parameters[2];i+=parameters[3]){\n");
                    gpuCodeBody.append("                idx = nind*parameters[2]+i;\n");
                } else {
                    gpuCodeBody.append("            for(int i=0;i<parameters[2];i++){\n");
                    gpuCodeBody.append("                idx = ind*parameters[2]+i;\n");
                }

            }
        }

        if (numbers.size() > 0) {
//            gpuCodeBody.append("            if(idx < parameters[0] && !(");
            gpuCodeBody.append("            if(idx < #SIZEOF0# && !(");
            for (int j = 1; j < numbers.size() - 1; j++) {
                gpuCodeBody.append("pixel").append(numbers.get(0)).append("[idx] == ").append("pixel").append(numbers.get(j)).append("[idx]").append(" && ");
            }
            gpuCodeBody.append("pixel").append(numbers.get(0)).append("[idx] == ").append("pixel").append(numbers.get(numbers.size() - 1)).append("[idx])){\n");
        }

        gpuCodeBody.append("                execute_sub(\n");
        for (String string : parameters.keySet()) {
            gpuCodeBody.append("                    ").append(string).append("[idx],\n");
        }

        String equation;
        String[] outEquation;
        String t;
        boolean rad_espectral = false;
        String ident;
        Equation eq2;
        firstComa = true;
        variablesDeclared = new HashSet<String>();
        for (int i = 0; i < vet.length; i++) {
            eq = equations.get(i);
//            terms = vet[i].split("=");
            structure = new Structure();
            structure.setToken(eq.getTerm());
            equation = eq.getTerm() + "=" + eq.getForm();
            ident = "";
            if (eq.getCondition() != null) {
                gpuCode.append("            if(");
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
            if (eq.getTerm().endsWith("rad_espectral")) {

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
                            gpuCodeBody.append("                    (banda").append(numbers.get(j)).append("+idx)");
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

            } else if (eq.getTerm().endsWith("reflectancia")) {

                gpuCode.append(ident).append("        sumBandas = 0.0f;\n");
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
                                gpuCodeBody.append("                    (bandaRefletida").append(numbers.get(j)).append("+idx)");
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
            } else {
                //AQUI
                if (indexSEBALLine != null && i == indexSEBALLine) {

                    gpuCode.append(
                              "                z0m = (float) expf(-5.809f + 5.62f * SAVI);\n"
                            + "                U_star = (float) (k * Uref / logf(z200 / z0m));\n"
                            + "                r_ah = (float) (logf(z2 / z1) / (U_star * k));\n"
                            + "\n"
                            + "                float LHot = 0.0f;\n"
                            + "                float tm_200Hot = 0.0f;\n"
                            + "                float th_2Hot = 0.0f;\n"
                            + "                float th_0_1Hot = 0.0f;\n"
                            + "\n"
                            + "                float LPixel = 0.0f;\n"
                            + "                float tm_200Pixel = 0.0f;\n"
                            + "                float th_2Pixel = 0.0f;\n"
                            + "                float th_0_1Pixel = 0.0f;\n"
                            + "\n"
                            + "                float HHot = RnHot - GHot;\n"
                            + "                float a = 0.0f;\n"
                            + "                float b = 0.0f;\n"
                            + "                float errorH = 10.0f;\n"
                            + "                float r_ah_anteriorHot = 0.0f;\n"
                            + "                int step = 1;\n"
                            + "                float z0mHot = (float) expf(-5.809f + 5.62f * SAVI_hot);\n"
                            + "                float U_starHot = (float) (k * Uref / logf(z200 / z0mHot));\n"
                            + "                float r_ahHot = (float) (logf(z2 / z1) / (U_starHot * k));\n"
                            + "                while (errorH > MaxAllowedError && step < 15) {\n"
                            + "\n"
                            + "                    a = ((HHot) * r_ahHot) / (p * cp * (indexMax - indexMin));\n"
                            + "                    b = -a * (indexMin - T0);\n"
                            + "\n"
                            + "                    //PARTE DO PIXEL QUENTE\n"
                            + "                    HHot = p * cp * (b + a * (indexMax - T0)) / r_ahHot;\n"
                            + "                    LHot = (float) (-(p * cp * U_starHot * U_starHot * U_starHot * (indexMax)) / (k * g * HHot));\n"
                            + "\n"
                            + "                    tm_200Hot = Psim(LHot);\n"
                            + "                    th_2Hot = Psih(z2, LHot);\n"
                            + "                    th_0_1Hot = Psih(z1, LHot);\n"
                            + "\n"
                            + "                    U_starHot = (float) (k * Uref / (logf(z200 / z0mHot) - tm_200Hot));\n"
                            + "                    r_ah_anteriorHot = r_ahHot;\n"
                            + "                    r_ahHot = (float) ((logf(z2 / z1) - th_2Hot + th_0_1Hot) / (U_starHot * k));\n"
                            + "\n"
                            + "                    //PARTE DE CADA PIXEL\n"
                            + "                    H = p * cp * (b + a * (Ts - T0)) / r_ah;\n"
                            + "                    LPixel = (float) (-(p * cp * U_star * U_star * U_star * (Ts)) / (k * g * H));\n"
                            + "\n"
                            + "                    tm_200Pixel = Psim(LPixel);\n"
                            + "                    th_2Pixel = Psih(z2, LPixel);\n"
                            + "                    th_0_1Pixel = Psih(z1, LPixel);\n"
                            + "\n"
                            + "                    U_star = (float) (k * Uref / (logf(z200 / z0m) - tm_200Pixel));\n"
                            + "                    r_ah = (float) ((logf(z2 / z1) - th_2Pixel + th_0_1Pixel) / (U_star * k));\n"
                            + "\n"
                            + "                    errorH = fabsf(((r_ahHot - r_ah_anteriorHot) * 100) / r_ahHot);\n"
                            + "\n"
                            + "                    step++;\n"
                            + "                }\n\n");

                }
                equation = ident + "        " + lexer.analyse(equation, structure, null, language) + ";\n";
                ex.evaluateExpr(equation);
                outEquation = ex.getOutput();
                if (eq.getIndex() != null) {
                    term = eq.getTerm();
                    equation = ident + "            *" + term + " = ";

                    if (variablesDeclared.add(term)) {
//                        if (i + 1 < vet.length) {
                        if (!firstComa) {
                            gpuCodeBody.append(",\n");
                        }
                        firstComa = false;
//                        }
                        gpuCodeBody.append("                    (").append(term).append("+idx)");
//                        if (i + 1 < vet.length) {
//                            gpuCodeBody.append(",");
//                        }
//                        gpuCodeBody.append("\n");
                    }

                } else {
                    equation = ident + "            " + eq.getTerm() + " = ";
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

            }
            if (eq.getCondition() != null) {
                gpuCode.append("            }\n\n");
            }
        }

        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                String[] varsIndex = null;
                if (indexEnum.equals(IndexEnum.SEBTA)) {
                    varsIndex = varsIndexSEBTA;
                } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                    varsIndex = varsIndexSEBAL;
                } else if (indexEnum.equals(IndexEnum.SSEB)) {
                    varsIndex = varsIndexSSEB;
                } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                    varsIndex = varsIndexSSEBI;
                }
                for (int i = 0; i < varsIndex.length; i++) {
                    String string = varsIndex[i];
//                    if (i == 0) {
//                        gpuCodeBody.append(",");
//                    }
                    gpuCodeBody.append("                    (").append(string).append("+ind)");
                    if (i + 1 < varsIndex.length) {
                        gpuCodeBody.append(",");
                    }
                    gpuCodeBody.append("\n");
                }

                if (indexEnum.equals(IndexEnum.SEBTA)) {
                    gpuCode.append(
                            "            if(sebta <= *minIndexVet){\n"
                            + "                if(Ts >= *maxTsVet){\n"
                            + "                    *maxTsVet=Ts;\n"
                            + "                    *minIndexVet=sebta;\n"
                            + "                    *rnHotVet=Rn;\n"
                            + "                    *gHotVet=G0;\n"
                            + "                    *saviHotVet=SAVI;\n"
                            + "                }\n"
                            + "            }\n"
                            + "            if(sebta >= *maxIndexVet){\n"
                            + "                if(Ts <= *minTsVet){\n"
                            + "                    *minTsVet=Ts;\n"
                            + "                    *maxIndexVet=sebta;\n"
                            + "                }\n"
                            + "            }\n\n");
                } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                    gpuCode.append(
                            "            if(sebal <= *minIndexVet){\n"
                            + "                *minIndexVet=sebal;\n"
                            + "            }\n"
                            + "            if(sebal >= *maxIndexVet){\n"
                            + "                *maxIndexVet=sebal;\n"
                            + "                *rnHotVet=Rn;\n"
                            + "                *gHotVet=G0;\n"
                            + "                *saviHotVet=SAVI;\n"
                            + "            }\n\n");
                } else if (indexEnum.equals(IndexEnum.SSEB)) {
                    gpuCode.append(
                            "            if(sseb <= *minIndexVet){\n"
                            + "                if(Ts >= *maxTsVet){\n"
                            + "                    *maxTsVet=Ts;\n"
                            + "                    *minIndexVet=sseb;\n"
                            + "                }\n"
                            + "            }\n"
                            + "            if(sseb >= *maxIndexVet){\n"
                            + "                if(Ts <= *minTsVet){\n"
                            + "                    *minTsVet=Ts;\n"
                            + "                    *maxIndexVet=sseb;\n"
                            + "                }\n"
                            + "            }\n\n");
                } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                    gpuCode.append(
                            "              if(ssebi >= *indexMax){\n"
                            + "		       *TsVet=Ts;\n"
                            + "		       *indexMax=ssebi;\n"
                            + "            }\n"
                            + "	    	   if(Ts >= *TsMax){\n"
                            + "		       *TsMax=Ts;\n"
                            + "		       *indexVet=ssebi;\n"
                            + "	           }\n"
                            + "            if(ssebi <= *indexMinBaixoVet){\n"
                            + "                if(Ts <= *TsMinEsqVet){\n"
                            + "                    *TsMinEsqVet=Ts;\n"
                            + "                    *indexMinBaixoVet=ssebi;\n"
                            + "                }\n"
                            + "            }\n\n");
                }

            }
        }

        gpuCodeBody.append("                );\n");

        if (numbers.size() > 0) {
            gpuCodeBody.append("            }\n");
        }
        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                gpuCodeBody.append("            }\n");
            }
        }

        gpuCodeBody.append("        }\n");
        gpuCodeBody.append("    }\n");

        gpuCode.append("    }\n\n");

        gpuCode.append(gpuCodeBody.toString());

        gpuCode.append("}\n");

//        System.out.println(gpuCode.toString());
//        System.out.println("------------------------------------------------------------------------------------------------");
//        System.out.println(source.toString());
        try {
            File dir = new File(System.getProperty("user.dir") + "/source");
            dir.mkdirs();
            PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/source/code.cu");
            pw.println(gpuCode.toString());
            pw.close();
        } catch (FileNotFoundException ex1) {
            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex1);
        }
//        System.exit(1);
        return source.toString();
    }

    private String generateOpenCL(String forVariables, String forEachValue, List<VariableValue> parametersList, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {
        List<String> variables = getVariables();
        ExpressionParser ex = new ExpressionParser();

        StringBuilder source = new StringBuilder();
        source.append("import static br.ufmt.genericseb.Constants.*;\n");
        source.append("import br.ufmt.genericseb.Constants;\n");
        source.append("import java.util.HashMap;\n");
        source.append("import java.util.Map;\n");
        source.append("import br.ufmt.jedigpu.ParameterGPU;\n");
        source.append("import java.util.ArrayList;\n");
        source.append("import br.ufmt.jedigpu.JSeriesCL;\n");
        source.append("import br.ufmt.jedigpu.OpenCLEnum;\n");
        source.append("import java.io.File;\n");
        source.append("import java.io.IOException;\n");
        source.append("import java.util.logging.Level;\n");
        source.append("import java.util.logging.Logger;\n");
        source.append("import java.io.BufferedReader;\n");
        source.append("import java.io.FileReader;\n");
        source.append("import br.ufmt.genericseb.GenericSEB;\n");
        source.append("import br.ufmt.genericlexerseb.Maths;\n");
        source.append("import java.util.List;\n\n");

        source.append("public class Equation{\n");

        source.append("    public Map<String, float[]> execute(");
        int size = 0;
        String vet1 = null;
        Map<String, VariableValue> parameters = new LinkedHashMap<String, VariableValue>();
        pars = new Object[parametersList.size()];
        classes = new Class[parametersList.size()];
        List<Integer> numbers = new ArrayList<Integer>();
        for (VariableValue value : parametersList) {
            String string = value.getName();
            parameters.put(string, value);
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
            source.append(classes[size].getCanonicalName()).append(string);
            size++;
            if (size < parametersList.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL)) {
                source.append("        float Uref = ").append(constants.get("Uref")).append("f;\n");
            }
            source.append("        int width = (int)").append(constants.get("width")).append("f;\n");
            source.append("        int height = (int)").append(constants.get("height")).append("f;\n");

            int tam = (indexEnum.equals(IndexEnum.SSEBI) ? 40 : 3);
            source.append("        int col = " + tam + ";\n");
        }

        source.append("        Map<String, float[]> ret = new HashMap<String, float[]>();\n\n");
        source.append("        List<ParameterGPU> par = new ArrayList<ParameterGPU>();\n\n");

        int sizeofNumber = 0;
        boolean first = true;
        for (String string : parameters.keySet()) {
            sizeofNumber++;
            if (first) {
                if (indexEnum != null) {
                    if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL)) {
                        source.append("        int[] N = new int[]{").append(string).append(".length, width, height};\n\n");
                    } else if (indexEnum.equals(IndexEnum.SSEBI) || indexEnum.equals(IndexEnum.SSEB)) {
                        source.append("        int[] N = new int[]{").append(string).append(".length, width, height, col};\n\n");
                    }
                } else {
                    source.append("        int[] N = new int[]{").append(string).append(".length};\n\n");
                }
                if (indexEnum == null) {
                    source.append("        par.add(new ParameterGPU(").append(string).append(",true,false,true));\n");
                } else {
                    source.append("        par.add(new ParameterGPU(").append(string).append(",true));\n");
                }
                first = false;
            } else {
                source.append("        par.add(new ParameterGPU(").append(string).append(",true));\n");
            }
        }

        source.append("\n");

        //DECLARANDO OS VETORES QUE SERAO RETORNADOS
        String[] vet = forEachValue.split("\n");
        String term;
        boolean vector;
        Set<String> variablesDeclared = new HashSet<String>();

        for (int i = 0; i < vet.length; i++) {
            if (vet[i].startsWith("rad_espectral") || vet[i].startsWith("O_rad_espectral")) {
                if (variablesDeclared.add("banda")) {
                    variables.add("pixel");
                    vector = vet[i].startsWith("O_rad_espectral");
                    for (int j = 0; j < numbers.size(); j++) {
                        if (vector) {
                            sizeofNumber++;
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
                                sizeofNumber++;
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
        gpuCode.append("\n#include \"" + System.getProperty("user.dir") + "/source/Constants.h\"\n\n");

        StringBuilder gpuCodeBody = new StringBuilder("    __kernel void execute(\n");

        if (constants != null) {
            for (String string : constants.keySet()) {
                gpuCode.append("    #define ").append(string).append(" ").append(constants.get(string)).append("f\n");
                variables.add(string);
            }
        }
        gpuCode.append("\n");

        vet = forVariables.split("\n");
        GenericLexerSEB lexer = new GenericLexerSEB();
        Structure structure;
        Map<String, Float> vars = new HashMap<String, Float>();
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

        String type;
        gpuCode.append("    void execute_sub(\n");

        VariableValue value;
        for (String string : parameters.keySet()) {
            value = parameters.get(string);
            type = value.getData().getClass().getCanonicalName().substring(0, value.getData().getClass().getCanonicalName().length() - 2);

            gpuCode.append("        ").append(type).append(" ").append(string).append(",\n");
            gpuCodeBody.append("        __global ").append(type).append(" * ").append(string).append(",\n");
            variables.add(string);
        }
        gpuCode.append("\n");
        gpuCodeBody.append("\n");

        vet = forEachValue.split("\n");
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

        if (forVariables != null && !forVariables.isEmpty()) {
            verifyEquations(forVariables, variables, false);
        }
        if (forEachValue != null && !forEachValue.isEmpty()) {
            verifyEquations(forEachValue, variables, true);
        }

        if (index != null) {
            equations.add(index);
        }

        Equation eq;
        StringBuilder openclVariables = new StringBuilder();
        for (int i = 0; i < equations.size(); i++) {
            eq = equations.get(i);
            if (!eq.getTerm().endsWith("rad_espectral") && !eq.getTerm().endsWith("reflectancia")) {

                if (variablesDeclared.add(eq.getTerm())) {
                    if (eq.getIndex() != null) {
                        term = eq.getTerm();
                        gpuCode.append("        __global float * ").append(term).append(",\n");
                        gpuCodeBody.append("        __global float * ").append(term).append(",\n");
                        variables.add(term);

                        sizeofNumber++;
                        source.append("        float[] ").append(term).append(" = new float[").append(vet1).append(".length];\n");
                        source.append("        par.add(new ParameterGPU(").append(term).append(",true,true));\n");
                        source.append("        ret.put(\"").append(term).append("\",").append(term).append(");\n\n");
                    } else {
                        openclVariables.append("        float ").append(eq.getTerm()).append(" = 0.0f;\n");
                    }
                }

            }
        }

        if (indexEnum != null) {

            String[] varsIndex = null;
            if (indexEnum.equals(IndexEnum.SEBTA)) {
                varsIndex = varsIndexSEBTA;
            } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                varsIndex = varsIndexSEBAL;
            } else if (indexEnum.equals(IndexEnum.SSEB)) {
                varsIndex = varsIndexSSEB;
            } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                varsIndex = varsIndexSSEBI;
            }

            for (int i = 0; i < varsIndex.length; i++) {
                String string = varsIndex[i];
                if (indexEnum.equals(IndexEnum.SSEBI) || indexEnum.equals(IndexEnum.SSEB)) {
                    sizeofNumber++;
                    if (indexEnum.equals(IndexEnum.SSEBI)) {
                        source.append("        float[] ").append(string).append(" = new float[col];\n");
                    } else {
                        source.append("        float[] ").append(string).append(" = new float[col * width];\n");
                    }
                    if (string.toLowerCase().contains("min")) {
                        source.append("        for (int i = 0; i < " + string + ".length; i++) {\n");
                        source.append("             " + string + "[i] = Float.MAX_VALUE;\n");
                        source.append("        }\n");
                    } else if (string.toLowerCase().contains("max")) {
                        source.append("        for (int i = 0; i < " + string + ".length; i++) {\n");
                        source.append("             " + string + "[i] = Float.MIN_VALUE;\n");
                        source.append("        }\n");
                    }
                } else {
                    source.append("        float[] ").append(string).append(" = new float[width];\n");
                }

                if (i == 0) {
                    gpuCode.append("\n");
                    if (!indexEnum.equals(IndexEnum.SSEB) && !indexEnum.equals(IndexEnum.SSEBI)) {
                        source.append("\n        par.add(new ParameterGPU(").append(string).append(", true, true, true));\n");
                    } else {
                        source.append("\n        par.add(new ParameterGPU(").append(string).append(", true, true));\n");
                    }
                } else {
                    source.append("        par.add(new ParameterGPU(").append(string).append(", true, true));\n");
                }
                source.append("\n");

                gpuCode.append("        __global float * ").append(string).append(",\n");
                gpuCodeBody.append("        __global float * ").append(string).append(",\n");
            }

        }

        if (indexEnum != null && (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI))) {
            source.append("        short[] gab = new short[width];\n"
                    + "        par.add(new ParameterGPU(gab, true, false, true));\n");
            source.append("        short[] gab2 = new short[col];\n"
                    + "        par.add(new ParameterGPU(gab2, true, false, true));\n");

        }
        source.append("        par.add(new ParameterGPU(N,true,false,false,false));\n\n");

        source.append("        String source = \"code.cl\";\n");
        source.append("        try {\n");
        source.append("            BufferedReader bur = new BufferedReader(new FileReader(System.getProperty(\"user.dir\") + \"/source/\" + source));\n");
        if (language.equals(LanguageType.OPENCL_CPU)) {
            source.append("            JSeriesCL opencl = new JSeriesCL(OpenCLEnum.CPU);\n");
        } else {
            source.append("            JSeriesCL opencl = new JSeriesCL();\n");
        }
//        source.append("            opencl.setMeasure(true);\n");
//        source.append("            opencl.setPrint(true);\n");
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

        if (index != null) {
            if (indexEnum.equals(IndexEnum.SEBTA)) {

                source.append(
                        "        float maxTs = maxTsVet[0];\n"
                        + "        float minTs = minTsVet[0];\n"
                        + "        float maxIndex = maxIndexVet[0];\n"
                        + "        float minIndex = minIndexVet[0];\n"
                        + "        float rnHot = rnHotVet[0];\n"
                        + "        float gHot = gHotVet[0];\n"
                        + "        float saviHot = saviHotVet[0];\n");

                source.append(
                        "        for (int i = 1; i < maxTsVet.length; i++) {\n"
                        + "            if (minIndexVet[i] <= minIndex) {\n"
                        + "                if (maxTsVet[i] >= maxTs) {\n"
                        + "                    maxTs = maxTsVet[i];\n"
                        + "                    minIndex = minIndexVet[i];\n"
                        + "                    rnHot = rnHotVet[i];\n"
                        + "                    gHot = gHotVet[i];\n"
                        + "                    saviHot = saviHotVet[i];\n"
                        + "                }\n"
                        + "            }\n"
                        + "\n"
                        + "            if (maxIndexVet[i] >= maxIndex) {\n"
                        + "                if (minTsVet[i] <= minTs) {\n"
                        + "                    minTs = minTsVet[i];\n"
                        + "                    maxIndex = maxIndexVet[i];\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n");

//                source.append("System.out.println(\"SEBTA OPENCL\");");
//                source.append("System.out.println(rnHot);");
//                source.append("System.out.println(gHot);");
//                source.append("System.out.println(Uref);");
//                source.append("System.out.println(saviHot);");
//                source.append("System.out.println(maxTs);");
//                source.append("System.out.println(minTs);");
                source.append("        float[] coef = new float[2];\n"
                        + "        GenericSEB.calculaAB(coef, rnHot, gHot, Uref, saviHot, maxTs, minTs);\n");
                source.append("        ret.put(\"coef\",coef);\n\n");
            } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                source.append(
                        "        float maxIndex = maxIndexVet[0];\n"
                        + "        float minIndex = minIndexVet[0];\n"
                        + "        float rnHot = rnHotVet[0];\n"
                        + "        float gHot = gHotVet[0];\n"
                        + "        float saviHot = saviHotVet[0];\n");

                source.append(
                        "        for (int i = 1; i < maxIndexVet.length; i++) {\n"
                        + "            if (minIndexVet[i] <= minIndex) {\n"
                        + "                minIndex = minIndexVet[i];\n"
                        + "            }\n"
                        + "\n"
                        + "            if (maxIndexVet[i] >= maxIndex) {\n"
                        + "                maxIndex = maxIndexVet[i];\n"
                        + "                rnHot = rnHotVet[i];\n"
                        + "                gHot = gHotVet[i];\n"
                        + "                saviHot = saviHotVet[i];\n"
                        + "            }\n"
                        + "        }\n");

                source.append("        float[] coef = new float[2];\n"
                        + "        GenericSEB.calculaAB(coef, rnHot, gHot, Uref, saviHot, maxIndex, minIndex);\n");
                source.append("        ret.put(\"coef\",coef);\n\n");
            } else if (indexEnum.equals(IndexEnum.SSEB)) {
                source.append("        float[] indexMax = new float[]{0.0f, 0.0f, 0.0f};\n"
                        + "        float[] indexMin = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};\n"
                        + "        float[] TsMax = new float[]{0.0f, 0.0f, 0.0f};\n"
                        + "        float[] TsMin = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};\n"
                        + "\n"
                        + "        for (int i = 0; i < maxTsVet.length; i ++) {\n"
                        + "            for (int idx = 0; idx < indexMax.length; idx++) {\n"
                        + "                if (maxIndexVet[i] >= indexMax[idx]) {\n"
                        + "                    if (minTsVet[i] <= TsMin[idx]) {\n"
                        + "                        for (int j = TsMin.length - 1; j > idx; j--) {\n"
                        + "                            TsMin[j] = TsMin[j - 1];\n"
                        + "                            indexMax[j] = indexMax[j - 1];\n"
                        + "                        }\n"
                        + "                        TsMin[idx] = minTsVet[i];\n"
                        + "                        indexMax[idx] = maxIndexVet[i];\n"
                        + "                        break;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "            }\n"
                        + "            for (int idx = 0; idx < indexMin.length; idx++) {\n"
                        + "                if (minIndexVet[i] <= indexMin[idx]) {\n"
                        + "                    if (maxTsVet[i] >= TsMax[idx]) {\n"
                        + "                        for (int j = TsMax.length - 1; j > idx; j--) {\n"
                        + "                            TsMax[j] = TsMax[j - 1];\n"
                        + "                            indexMin[j] = indexMin[j - 1];\n"
                        + "                        }\n"
                        + "                        TsMax[idx] = maxTsVet[i];\n"
                        + "                        indexMin[idx] = minIndexVet[i];\n"
                        + "                        break;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "\n"
                        + "        float TC = 0.0f;\n"
                        + "        float TH = 0.0f;\n"
                        + "        for (int i = 0; i < indexMax.length; i++) {\n"
                        + "            TC += TsMin[i];\n"
                        + "            TH += TsMax[i];\n"
                        + "        }\n"
                        + "        TC = TC / indexMax.length;\n"
                        + "        TH = TH / indexMax.length;\n"
                        //                        + "        System.out.println(\"SSEB OPENCL\");\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(indexMax));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(TsMin));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(indexMin));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(TsMax));\n"
                        //                        + "        System.out.println(TC);\n"
                        //                        + "        System.out.println(TH);\n"
                        + "        ret.put(\"TC\", new float[]{TC});\n"
                        + "        ret.put(\"TH\", new float[]{TH});\n\n");
            } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                source.append("        float mediaSupEsqTs = 0.0f;\n"
                        + "        float mediaSupDirTs = 0.0f;\n"
                        + "        float mediaInfEsqTs = 0.0f;\n"
                        + "        float mediaInfDirTs = 0.0f;\n"
                        + "\n"
                        + "        float mediaSupEsqIndex = 0.0f;\n"
                        + "        float mediaSupDirIndex = 0.0f;\n"
                        + "        float mediaInfEsqIndex = 0.0f;\n"
                        + "        float mediaInfDirIndex = 0.0f;\n"
                        + "\n"
                        + "        float[] indexMaxCima = new float[10];\n"
                        + "        float[] indexMaxBaixo = new float[10];\n"
                        + "        float[] indexMinCima = new float[10];\n"
                        + "        float[] indexMinBaixo = new float[10];\n"
                        + "\n"
                        + "        float[] TsMaxDir = new float[10];\n"
                        + "        float[] TsMaxEsq = new float[10];\n"
                        + "        float[] TsMinDir = new float[10];\n"
                        + "        float[] TsMinEsq = new float[10];\n"
                        + "\n"
                        + "        for (int i = 0; i < 10; i++) {\n"
                        + "            indexMinCima[i] = Float.MAX_VALUE;\n"
                        + "            TsMinDir[i] = Float.MAX_VALUE;\n"
                        + "            indexMinBaixo[i] = Float.MAX_VALUE;\n"
                        + "            TsMinEsq[i] = Float.MAX_VALUE;\n"
                        + "        }\n"
                        + "\n"
                        + "        for (int i = 0; i < indexMax.length; i++) {\n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (TsVet[i] >= TsMaxDir[idx]) {\n"
                        + "                    for (int j = TsMaxDir.length - 1; j > idx; j--) {\n"
                        + "                        TsMaxDir[j] = TsMaxDir[j - 1];\n"
                        + "                        indexMaxCima[j] = indexMaxCima[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMaxDir[idx] = TsVet[i];\n"
                        + "                    indexMaxCima[idx] = indexMax[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "\n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (TsVet[i] <= TsMinDir[idx]) {\n"
                        + "                    for (int j = TsMinDir.length - 1; j > idx; j--) {\n"
                        + "                        TsMinDir[j] = TsMinDir[j - 1];\n"
                        + "                        indexMaxBaixo[j] = indexMaxBaixo[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMinDir[idx] = TsVet[i];\n"
                        + "                    indexMaxBaixo[idx] = indexMax[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "\n"
                        + "            for (int idx = 0; idx < TsMaxEsq.length; idx++) {\n"
                        + "                if (indexVet[i] <= indexMinCima[idx]) {\n"
                        + "                    for (int j = TsMaxEsq.length - 1; j > idx; j--) {\n"
                        + "                        TsMaxEsq[j] = TsMaxEsq[j - 1];\n"
                        + "                        indexMinCima[j] = indexMinCima[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMaxEsq[idx] = TsMax[i];\n"
                        + "                    indexMinCima[idx] = indexVet[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (indexMinBaixoVet[i] <= indexMinBaixo[idx]) {\n"
                        + "                    if (TsMinEsqVet[i] <= TsMinEsq[idx]) {\n"
                        + "                        for (int j = TsMinEsq.length - 1; j > idx; j--) {\n"
                        + "                            TsMinEsq[j] = TsMinEsq[j - 1];\n"
                        + "                            indexMinBaixo[j] = indexMinBaixo[j - 1];\n"
                        + "                        }\n"
                        + "                        TsMinEsq[idx] = TsMinEsqVet[i];\n"
                        + "                        indexMinBaixo[idx] = indexMinBaixoVet[i];\n"
                        + "                        break;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }"
                        + "        for (int i = 0; i < TsMinEsq.length; i++) {\n"
                        + "            mediaSupEsqTs += TsMaxEsq[i];\n"
                        + "            mediaSupDirTs += TsMaxDir[i];\n"
                        + "            mediaInfEsqTs += TsMinEsq[i];\n"
                        + "            mediaInfDirTs += TsMinDir[i];\n"
                        + "\n"
                        + "            mediaSupEsqIndex += indexMinCima[i];\n"
                        + "            mediaSupDirIndex += indexMaxCima[i];\n"
                        + "            mediaInfEsqIndex += indexMinBaixo[i];\n"
                        + "            mediaInfDirIndex += indexMaxBaixo[i];\n"
                        + "        }\n"
                        + "        mediaSupEsqTs /= TsMaxEsq.length;\n"
                        + "        mediaSupDirTs /= TsMaxDir.length;\n"
                        + "        mediaInfEsqTs /= TsMinEsq.length;\n"
                        + "        mediaInfDirTs /= TsMinDir.length;\n"
                        + "\n"
                        + "        mediaSupEsqIndex /= indexMinCima.length;\n"
                        + "        mediaSupDirIndex /= indexMaxCima.length;\n"
                        + "        mediaInfEsqIndex /= indexMinBaixo.length;\n"
                        + "        mediaInfDirIndex /= indexMaxBaixo.length;\n"
                        //                        + "System.out.println(\"SSEBI OPENCL\");\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMaxBaixo));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMinBaixo));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMaxCima));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMinCima));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMaxEsq));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMaxDir));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMinDir));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMinEsq));\n"
                        //                        + "            System.out.println(\"mediaSupEsqTs:\"+mediaSupEsqTs);\n"
                        //                        + "            System.out.println(\"mediaSupDirTs:\"+mediaSupDirTs);\n"
                        //                        + "            System.out.println(\"mediaInfEsqTs:\"+mediaInfEsqTs);\n"
                        //                        + "            System.out.println(\"mediaInfDirTs:\"+mediaInfDirTs);\n"
                        //                        + "            System.out.println(\"mediaSupEsqIndex:\"+mediaSupEsqIndex);\n"
                        //                        + "            System.out.println(\"mediaSupDirIndex:\"+mediaSupDirIndex);\n"
                        //                        + "            System.out.println(\"mediaInfEsqIndex:\"+mediaInfEsqIndex);\n"
                        //                        + "            System.out.println(\"mediaInfDirIndex:\"+mediaInfDirIndex);\n"
                        + "\n"
                        + "        float aH = 0.0f;\n"
                        + "        float aLE = 0.0f;\n"
                        + "        float bH = 0.0f;\n"
                        + "        float bLE = 0.0f;\n"
                        + "\n"
                        + "        bH = (mediaSupDirTs - mediaSupEsqTs) / (mediaSupDirIndex - mediaSupEsqIndex);\n"
                        + "        aH = (mediaSupEsqTs * mediaSupDirIndex - mediaSupDirTs * mediaSupEsqIndex) / (mediaSupDirIndex - mediaSupEsqIndex);\n"
                        + "\n"
                        + "        bLE = (mediaInfDirTs - mediaInfEsqTs) / (mediaInfDirIndex - mediaInfEsqIndex);\n"
                        + "        aLE = (mediaInfEsqTs * mediaInfDirIndex - mediaInfDirTs * mediaInfEsqIndex) / (mediaInfDirIndex - mediaInfEsqIndex);\n"
                        + "\n"
                        //                        + "System.out.println(\"ah:\"+aH);\n"
                        //                        + "        System.out.println(\"bH:\"+bH);\n"
                        //                        + "        System.out.println(\"aLE:\"+aLE);\n"
                        //                        + "        System.out.println(\"bLE:\"+bLE);\n"
                        + "        ret.put(\"aH\", new float[]{aH});\n"
                        + "        ret.put(\"bH\", new float[]{bH});\n"
                        + "\n"
                        + "        ret.put(\"aLE\", new float[]{aLE});\n"
                        + "        ret.put(\"bLE\", new float[]{bLE});\n\n");
            }
        }

        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");

        if (indexEnum != null) {
            gpuCode.append("\n        int ind");
            for (int i = 0; i < vet.length; i++) {
                eq = equations.get(i);
                if (eq.getIndex() != null) {
                    gpuCode.append(",\n        int idx");
                    break;
                }
            }
            if (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                gpuCodeBody.append("        __global short * gab,\n");
                gpuCodeBody.append("        __global short * gab2,\n");
            }
        } else {
            gpuCode.append("        int idx");
        }
        gpuCodeBody.append("        __global int * parameters");

        gpuCode.append("){\n\n");
        gpuCodeBody.append("){\n");

        gpuCode.append(openclVariables.toString());

        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
//                gpuCodeBody.append("        int size = parameters[1];\n");
                gpuCodeBody.append("        int size = #SIZEOF" + sizeofNumber + "#;\n");
            }
        } else {
//            gpuCodeBody.append("        int size = parameters[0];\n");
            gpuCodeBody.append("        int size = #SIZEOF0#;\n");
        }

        gpuCodeBody.append("        int idx = get_global_id(0);\n");
        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                gpuCodeBody.append("        int ind = idx;\n");
            }
        }
        if (indexEnum != null && (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI))) {
            gpuCodeBody.append("        int indY = get_global_id(1);\n");
            gpuCodeBody.append("        if(idx < size && indY < parameters[3]){\n");
        } else {
            gpuCodeBody.append("        if(idx < size){\n");
        }

        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {

                String[] varsIndex = null;
                if (indexEnum.equals(IndexEnum.SEBTA)) {
                    varsIndex = varsIndexSEBTA;
                } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                    varsIndex = varsIndexSEBAL;
                } else if (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                    if (indexEnum.equals(IndexEnum.SSEB)) {
                        varsIndex = varsIndexSSEB;
                    } else {
                        varsIndex = varsIndexSSEBI;
                    }
                    gpuCodeBody.append("            int nind = ind;\n");
                    gpuCodeBody.append("            ind = indY;\n");
                }

                if (!(indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI))) {
                    for (int i = 0; i < varsIndex.length; i++) {
                        String string = varsIndex[i];
                        gpuCodeBody.append("            ").append(string).append("[ind]");
                        if (string.startsWith("min")) {
                            gpuCodeBody.append("=99999.0f;\n");
                        } else {
                            gpuCodeBody.append("=-99999.0f;\n");
                        }
                    }
                }

                if (indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                    gpuCodeBody.append("            for(int i=indY;i<parameters[2];i+=parameters[3]){\n");
                    gpuCodeBody.append("                idx = nind*parameters[2]+i;\n");
                } else {
                    gpuCodeBody.append("            for(int i=0;i<parameters[2];i++){\n");
                    gpuCodeBody.append("                idx = ind*parameters[2]+i;\n");
                }
            }
        }

        if (numbers.size() > 0) {
//            gpuCodeBody.append("        if(idx < parameters[0] && !(");
            gpuCodeBody.append("        if(idx < #SIZEOF0# && !(");
            for (int j = 1; j < numbers.size() - 1; j++) {
                gpuCodeBody.append("pixel").append(numbers.get(0)).append("[idx] == ").append("pixel").append(numbers.get(j)).append("[idx]").append(" && ");
            }
            gpuCodeBody.append("pixel").append(numbers.get(0)).append("[idx] == ").append("pixel").append(numbers.get(numbers.size() - 1)).append("[idx])){\n");
        }

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

        variablesDeclared = new HashSet<String>();
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
                        gpuCode.append(condition[j].trim());
                        if (parameters.get(condition[j]) != null) {
                            gpuCode.append("[idx]");
                        }
//                        if (condition[j].matches("(-?)[0-9]+[\\.][0-9]+")) {
//                            gpuCode.append("f");
//                        }
                    }
                    gpuCode.append(" ");
                }
                gpuCode.append("){\n");
                ident = "    ";
            }
            if (eq.getTerm().endsWith("rad_espectral")) {
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

            } else if (eq.getTerm().endsWith("reflectancia")) {

                gpuCode.append(ident).append("        sumBandas = 0.0f;\n");
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
            } else {

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
//                    System.out.println(equation);
                equation += "\n\n";
                gpuCode.append(equation);

            }
            if (eq.getCondition() != null) {
                gpuCode.append("        }\n\n");
            }
        }

        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                String[] varsIndex = null;
                if (indexEnum.equals(IndexEnum.SEBTA)) {
                    varsIndex = varsIndexSEBTA;
                } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                    varsIndex = varsIndexSEBAL;
                } else if (indexEnum.equals(IndexEnum.SSEB)) {
                    varsIndex = varsIndexSSEB;
                } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                    varsIndex = varsIndexSSEBI;
                }
                for (int i = 0; i < varsIndex.length; i++) {
                    String string = varsIndex[i];
//                    if (i == 0) {
//                        gpuCodeBody.append(",");
//                    }
                    gpuCodeBody.append("                ").append(string).append(",\n");

                }

                if (indexEnum.equals(IndexEnum.SEBTA)) {
                    gpuCode.append(
                            "        if(sebta <= minIndexVet[ind]){\n"
                            + "            if(Ts >= maxTsVet[ind]){\n"
                            + "                maxTsVet[ind]=Ts;\n"
                            + "                minIndexVet[ind]=sebta;\n"
                            + "                rnHotVet[ind]=Rn;\n"
                            + "                gHotVet[ind]=G0;\n"
                            + "                saviHotVet[ind]=SAVI;\n"
                            + "            }\n"
                            + "        }\n"
                            + "        if(sebta >= maxIndexVet[ind]){\n"
                            + "            if(Ts <= minTsVet[ind]){\n"
                            + "                minTsVet[ind]=Ts;\n"
                            + "                maxIndexVet[ind]=sebta;\n"
                            + "            }\n"
                            + "        }\n\n");
                } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                    gpuCode.append(
                            "        if(sebal <= minIndexVet[ind]){\n"
                            + "            minIndexVet[ind]=sebal;\n"
                            + "        }\n"
                            + "        if(sebal >= maxIndexVet[ind]){\n"
                            + "            maxIndexVet[ind]=sebal;\n"
                            + "            rnHotVet[ind]=Rn;\n"
                            + "            gHotVet[ind]=G0;\n"
                            + "            saviHotVet[ind]=SAVI;\n"
                            + "        }\n\n");
                } else if (indexEnum.equals(IndexEnum.SSEB)) {
                    gpuCode.append(
                            "            if(sseb <= minIndexVet[ind]){\n"
                            + "                if(Ts >= maxTsVet[ind]){\n"
                            + "                    maxTsVet[ind]=Ts;\n"
                            + "                    minIndexVet[ind]=sseb;\n"
                            + "                }\n"
                            + "            }\n"
                            + "            if(sseb >= maxIndexVet[ind]){\n"
                            + "                if(Ts <= minTsVet[ind]){\n"
                            + "                    minTsVet[ind]=Ts;\n"
                            + "                    maxIndexVet[ind]=sseb;\n"
                            + "                }\n"
                            + "            }\n\n");
                } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                    gpuCode.append("            if(ssebi >= indexMax[ind]){\n"
                            + "		TsVet[ind]=Ts;\n"
                            + "		indexMax[ind]=ssebi;\n"
                            + "            }\n"
                            + "	    if(Ts >= TsMax[ind]){\n"
                            + "		TsMax[ind]=Ts;\n"
                            + "		indexVet[ind]=ssebi;\n"
                            + "	    }\n"
                            + "            if(ssebi <= indexMinBaixoVet[ind]){\n"
                            + "                if(Ts <= TsMinEsqVet[ind]){\n"
                            + "                    TsMinEsqVet[ind]=Ts;\n"
                            + "                    indexMinBaixoVet[ind]=ssebi;\n"
                            + "                }\n"
                            + "            }\n\n");

                }

            }
        }

        if (indexEnum != null) {
//            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB)) {
//                gpuCodeBody.append(",\n                ind");
//            }
            gpuCodeBody.append("                ind");
            for (int i = 0; i < vet.length; i++) {
                eq = equations.get(i);
                if (eq.getIndex() != null) {
                    gpuCodeBody.append(",\n                idx");
                    break;
                }
            }
        } else {
            gpuCodeBody.append("                idx");
        }
        gpuCodeBody.append(");\n");

        if (numbers.size() > 0) {
            gpuCodeBody.append("        }\n");
        }
        if (indexEnum != null) {
            if (indexEnum.equals(IndexEnum.SEBTA) || indexEnum.equals(IndexEnum.SEBAL) || indexEnum.equals(IndexEnum.SSEB) || indexEnum.equals(IndexEnum.SSEBI)) {
                gpuCodeBody.append("            }\n");
            }
        }

        gpuCodeBody.append("        }\n");
        gpuCodeBody.append("    }\n");

        gpuCode.append("    }\n\n");

        gpuCode.append(gpuCodeBody.toString());

//        System.out.println(gpuCode.toString());
//        System.out.println("-------------------------------------------------------------------------------------------");
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

    private String generateJava(String forVariables, String forEachValue, List<VariableValue> parametersList, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {
        List<String> variables = getVariables();
        ExpressionParser ex = new ExpressionParser();

        StringBuilder source = new StringBuilder();
        source.append("import static br.ufmt.genericseb.Constants.*;\n");
        source.append("import br.ufmt.genericseb.Constants;\n");
        source.append("import java.util.HashMap;\n");
        source.append("import java.util.Map;\n");
        source.append("import br.ufmt.genericseb.GenericSEB;\n");
        source.append("import br.ufmt.genericlexerseb.Maths;\n");

        source.append("import java.util.List;\n\n");

        source.append("public class Equation{\n");

        source.append("    public Map<String, float[]> execute(");
        int size = 0;
        String vet1 = null;
        Map<String, String> parameters = new LinkedHashMap<String, String>();
        pars = new Object[parametersList.size()];
        classes = new Class[parametersList.size()];
        List<Integer> numbers = new ArrayList<Integer>();
        for (VariableValue value : parametersList) {
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
            source.append(classes[size].getCanonicalName()).append(string);
            size++;
            if (size < parametersList.size()) {
                source.append(",");
            }
        }
        source.append("){\n\n");

        source.append("        Map<String, float[]> ret = new HashMap<String, float[]>();\n\n");

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

        if (forVariables != null && !forVariables.isEmpty()) {
            vet = forVariables.split("\n");
            //COLOCANDO AS FORMULAS DO CABECALHOS
            for (int i = 0; i < vet.length; i++) {
                structure = new Structure();
                structure.setToken(vet[i].split("=")[0]);
                source.append("        float ").append(lexer.analyse(vet[i], structure, null, LanguageType.JAVA)).append(";\n\n");
            }
            source.append("\n");
        }

        //DECLARANDO OS VETORES QUE SERAO RETORNADOS
        vet = forEachValue.split("\n");
        Equation eq;
        boolean vector;
        Set<String> variablesDeclared = new HashSet<String>();

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
//        System.out.println("Verify");
        if (forVariables != null && !forVariables.isEmpty()) {
            verifyEquations(forVariables, variables, false);
        }
        if (forEachValue != null && !forEachValue.isEmpty()) {
            verifyEquations(forEachValue, variables, true);
        }

        if (index != null) {
            if (indexEnum.equals(IndexEnum.SEBTA)) {
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
            } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                source.append("        float indexMax, indexMin;\n"
                        + "        float RnHot = 0.0f, GHot = 0.0f;\n"
                        + "        float SAVI_hot = 0.0f;\n"
                        + "\n"
                        + "        indexMax = 0.0f;\n"
                        + "\n"
                        + "        indexMin = Float.MAX_VALUE;\n");
            } else if (indexEnum.equals(IndexEnum.SSEB)) {
                source.append("        float[] indexMax = new float[]{0.0f, 0.0f, 0.0f};\n"
                        + "        float[] indexMin = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};\n"
                        + "        float[] TsMax = new float[]{0.0f, 0.0f, 0.0f};\n"
                        + "        float[] TsMin = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};\n");
            } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                source.append("\n"
                        + "        float[] indexMax = new float[30];\n"
                        + "        float[] indexVet = new float[30];\n"
                        + "        float[] indexMinBaixo = new float[10];\n"
                        + "        float[] TsVet = new float[30];\n"
                        + "        float[] TsMax = new float[30];\n"
                        + "        float[] TsMinEsq = new float[10];\n"
                        + "\n"
                        + "        for (int i = 0; i < 30; i++) {\n"
                        + "            indexVet[i] = Float.MAX_VALUE;\n"
                        + "        }\n"
                        + "        for (int i = 0; i < TsMinEsq.length; i++) {\n"
                        + "            TsMinEsq[i] = Float.MAX_VALUE;\n"
                        + "            indexMinBaixo[i] = Float.MAX_VALUE;\n"
                        + "        }\n\n");
            }
            equations.add(index);
        } else if (indexSEBALLine != null) {
            source.append(""
                    //                    + "                float z0m;\n"
                    //                    + "                float U_star;\n"
                    //                    + "                float r_ah;\n"
                    //                    + "\n"
                    //                    + "                float H = 0;\n"
                    + "\n"
                    + "        float z0mHot;\n"
                    + "        float U_starHot;\n"
                    + "        float r_ahHot;\n"
                    + "        float LHot;\n"
                    + "        float tm_200Hot;\n"
                    + "        float th_2Hot;\n"
                    + "        float th_0_1Hot;\n"
                    + "\n"
                    + "        float LPixel;\n"
                    + "        float tm_200Pixel;\n"
                    + "        float th_2Pixel;\n"
                    + "        float th_0_1Pixel;\n"
                    + "\n"
                    + "        float HHot = RnHot - GHot;\n"
                    + "        float a = 0.0f;\n"
                    + "        float b = 0.0f;\n"
                    + "        float errorH = 10.0f;\n"
                    + "        int step = 1;\n"
                    + "        float r_ah_anteriorHot;\n");
        }

        for (int i = 0; i < equations.size(); i++) {
            eq = equations.get(i);
            if (!eq.getTerm().endsWith("rad_espectral") && !eq.getTerm().endsWith("reflectancia")) {
                if (variablesDeclared.add(eq.getTerm())) {
                    if (eq.getIndex() != null) {
                        source.append("        float[] ").append(eq.getTerm()).append(" = new float[").append(vet1).append(".length];\n");
                        source.append("        ret.put(\"").append(eq.getTerm()).append("\",").append(eq.getTerm()).append(");\n\n");
                    } else {
                        source.append("        float ").append(eq.getTerm()).append(" = 0;\n");
                    }
                }

            }
        }

        source.append("        for(int i = 0;i < ").append(vet1).append(".length;i++){\n");

        if (numbers.size() > 0) {
            source.append("            if(!(");
            for (int j = 1; j < numbers.size() - 1; j++) {
                source.append("pixel").append(numbers.get(0)).append("[i] == ").append("pixel").append(numbers.get(j)).append("[i]").append(" && ");
            }
            source.append("pixel").append(numbers.get(0)).append("[i] == ").append("pixel").append(numbers.get(numbers.size() - 1)).append("[i])){\n");
        }

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
            ident = "    ";
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
                ident = "        ";
            }
            if (eq.getTerm().endsWith("rad_espectral")) {

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
            } else if (eq.getTerm().endsWith("reflectancia")) {

                source.append(ident).append("            sumBandas = 0.0f;\n");
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
            } else {
//                boolean print = false;
                if (indexSEBALLine != null && i == indexSEBALLine) {
                    String ts = "Ts", savi = "SAVI";
                    for (int j = 0; j < equations.size(); j++) {
                        Equation eq3 = equations.get(j);
                        if (eq3.getIndex() != null) {
                            if (eq3.getTerm().equals("Ts")) {
                                ts = "Ts[i]";
                            } else if (eq3.getTerm().equals("SAVI")) {
                                savi = "SAVI[i]";
                            }
                        }

                    }
                    source.append(
                            "                z0m = (float) Math.exp(-5.809f + 5.62f * " + savi + ");\n"
                            + "                U_star = (float) (k * Uref / Math.log(z200 / z0m));\n"
                            + "                r_ah = (float) (Math.log(z2 / z1) / (U_star * k));\n"
                            + "\n"
                            + "                H = 0f;\n"
                            + "                LHot = 0f;\n"
                            + "                tm_200Hot = 0f;\n"
                            + "                th_2Hot = 0f;\n"
                            + "                th_0_1Hot = 0f;\n"
                            + "\n"
                            + "                LPixel = 0f;\n"
                            + "                tm_200Pixel = 0f;\n"
                            + "                th_2Pixel = 0f;\n"
                            + "                th_0_1Pixel = 0f;\n"
                            + "\n"
                            + "                HHot = RnHot - GHot;\n"
                            + "                a = 0.0f;\n"
                            + "                b = 0.0f;\n"
                            + "                errorH = 10.0f;\n"
                            + "                step = 1;\n"
                            + "                z0mHot = (float) Math.exp(-5.809f + 5.62f * SAVI_hot);\n"
                            + "                U_starHot = (float) (k * Uref / Math.log(z200 / z0mHot));\n"
                            + "                r_ahHot = (float) (Math.log(z2 / z1) / (U_starHot * k));\n"
                            + "                while (errorH > MaxAllowedError && step < 15) {\n"
                            + "\n"
                            + "                    a = ((HHot) * r_ahHot) / (p * cp * (indexMax - indexMin));\n"
                            + "                    b = -a * (indexMin - T0);\n"
                            + "\n"
                            + "                    //PARTE DO PIXEL QUENTE\n"
                            + "                    HHot = p * cp * (b + a * (indexMax - T0)) / r_ahHot;\n"
                            + "                    LHot = (float) (-(p * cp * U_starHot * U_starHot * U_starHot * (indexMax)) / (k * g * HHot));\n"
                            + "\n"
                            + "                    tm_200Hot = GenericSEB.Psim(LHot);\n"
                            + "                    th_2Hot = GenericSEB.Psih(z2, LHot);\n"
                            + "                    th_0_1Hot = GenericSEB.Psih(z1, LHot);\n"
                            + "\n"
                            + "                    U_starHot = (float) (k * Uref / (Math.log(z200 / z0mHot) - tm_200Hot));\n"
                            + "                    r_ah_anteriorHot = r_ahHot;\n"
                            + "                    r_ahHot = (float) ((Math.log(z2 / z1) - th_2Hot + th_0_1Hot) / (U_starHot * k));\n"
                            + "\n"
                            + "                    //PARTE DE CADA PIXEL\n"
                            + "                    H = p * cp * (b + a * (" + ts + " - T0)) / r_ah;\n"
                            + "                    LPixel = (float) (-(p * cp * U_star * U_star * U_star * (" + ts + ")) / (k * g * H));\n"
                            + "\n"
                            + "                    tm_200Pixel = GenericSEB.Psim(LPixel);\n"
                            + "                    th_2Pixel = GenericSEB.Psih(z2, LPixel);\n"
                            + "                    th_0_1Pixel = GenericSEB.Psih(z1, LPixel);\n"
                            + "\n"
                            + "                    U_star = (float) (k * Uref / (Math.log(z200 / z0m) - tm_200Pixel));\n"
                            + "                    r_ah = (float) ((Math.log(z2 / z1) - th_2Pixel + th_0_1Pixel) / (U_star * k));\n"
                            + "\n"
                            + "                    errorH = Math.abs(((r_ahHot - r_ah_anteriorHot) * 100) / r_ahHot);\n"
                            + "\n"
                            + "                    step++;\n"
                            + "                }\n\n");

//                    System.out.println(ident + "            " + lexer.analyse(equation, structure, null, LanguageType.JAVA) + ";\n");
//                    print = true;
                }
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
//                    if (print) {
//                        System.out.println(string);
//                        System.out.println(equation);
//                    }
                }

                equation += "\n\n";
                variables.add(eq.getTerm());
                source.append(equation);

            }
            if (eq.getCondition() != null) {
                source.append("            }\n\n");
            }
        }

        if (index != null) {
            if (indexEnum.equals(IndexEnum.SEBTA)) {
                String ts = "Ts";
                String rn = "Rn", g = "G0", savi = "SAVI";
                for (int i = 0; i < equations.size(); i++) {
                    eq = equations.get(i);
                    if (eq.getIndex() != null) {
                        if (eq.getTerm().equals("Ts")) {
                            ts = "Ts[i]";
                        } else if (eq.getTerm().equals("Rn")) {
                            rn = "Rn[i]";
                        } else if (eq.getTerm().equals("G0")) {
                            g = "G0[i]";
                        } else if (eq.getTerm().equals("SAVI")) {
                            savi = "SAVI[i]";
                        }
                    }

                }

                source.append(
                        "            if (sebta >= indexMax) {\n"
                        + "                if (" + ts + " <= tMin && " + ts + " > -300) {\n"
                        + "                    tMin = " + ts + ";\n"
                        + "                    indexMax = sebta;\n"
                        + "                }\n"
                        + "            } else if (sebta <= indexMin) {\n"
                        + "                if (" + ts + " >= tMax && " + ts + " < 10000) {\n"
                        + "                    tMax = " + ts + ";\n"
                        + "                    indexMin = sebta;\n"
                        + "                    RnHot = " + rn + ";\n"
                        + "                    SAVI_hot = " + savi + ";\n"
                        + "                    GHot = " + g + ";\n"
                        + "                }\n"
                        + "            }\n");
            } else if (indexEnum.equals(IndexEnum.SEBAL)) {
                String rn = "Rn", g = "G0", savi = "SAVI";
                for (int i = 0; i < equations.size(); i++) {
                    eq = equations.get(i);
                    if (eq.getIndex() != null) {
                        if (eq.getTerm().equals("Rn")) {
                            rn = "Rn[i]";
                        } else if (eq.getTerm().equals("G0")) {
                            g = "G0[i]";
                        } else if (eq.getTerm().equals("SAVI")) {
                            savi = "SAVI[i]";
                        }
                    }

                }

                source.append(
                        "            if (sebal >= indexMax) {\n"
                        + "                indexMax = sebal;\n"
                        + "                RnHot = " + rn + ";\n"
                        + "                SAVI_hot = " + savi + ";\n"
                        + "                GHot = " + g + ";\n"
                        + "            } else if (sebal <= indexMin) {\n"
                        + "                indexMin = sebal;\n"
                        + "            }\n");
            } else if (indexEnum.equals(IndexEnum.SSEB)) {
                String ts = "Ts";
                for (int i = 0; i < equations.size(); i++) {
                    eq = equations.get(i);
                    if (eq.getIndex() != null) {
                        if (eq.getTerm().equals("Ts")) {
                            ts = "Ts[i]";
                        }
                    }

                }

                source.append(
                        "                for (int idx = 0; idx < indexMax.length; idx++) {\n"
                        + "                    if (sseb >= indexMax[idx]) {\n"
                        + "                        if (" + ts + " <= TsMin[idx]) {\n"
                        + "                            for (int j = TsMin.length - 1; j > idx; j--) {\n"
                        + "                                TsMin[j] = TsMin[j - 1];\n"
                        + "                                indexMax[j] = indexMax[j - 1];\n"
                        + "                            }\n"
                        + "                            TsMin[idx] = " + ts + ";\n"
                        + "                            indexMax[idx] = sseb;\n"
                        + "                            break;\n"
                        + "                        }\n"
                        + "                    }\n"
                        + "                }\n"
                        + "                for (int idx = 0; idx < indexMin.length; idx++) {\n"
                        + "                    if (sseb <= indexMin[idx]) {\n"
                        + "                        if (" + ts + " >= TsMax[idx]) {\n"
                        + "                            for (int j = TsMax.length - 1; j > idx; j--) {\n"
                        + "                                TsMax[j] = TsMax[j - 1];\n"
                        + "                                indexMin[j] = indexMin[j - 1];\n"
                        + "                            }\n"
                        + "                            TsMax[idx] = " + ts + ";\n"
                        + "                            indexMin[idx] = sseb;\n"
                        + "                            break;\n"
                        + "                        }\n"
                        + "                    }\n"
                        + "                }\n");

            } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                String ts = "Ts";
                for (int i = 0; i < equations.size(); i++) {
                    eq = equations.get(i);
                    if (eq.getIndex() != null) {
                        if (eq.getTerm().equals("Ts")) {
                            ts = "Ts[i]";
                        }
                    }

                }

                source.append(
                        "                for (int idx = 0; idx < indexMax.length; idx++) {\n"
                        + "                    if (ssebi >= indexMax[idx]) {\n"
                        + "                        for (int j = TsVet.length - 1; j > idx; j--) {\n"
                        + "                            TsVet[j] = TsVet[j - 1];\n"
                        + "                            indexMax[j] = indexMax[j - 1];\n"
                        + "                        }\n"
                        + "                        TsVet[idx] = " + ts + ";\n"
                        + "                        indexMax[idx] = ssebi;\n"
                        + "                        break;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "                for (int idx = 0; idx < indexVet.length; idx++) {\n"
                        + "                    if (" + ts + " >= TsMax[idx]) {\n"
                        + "                        for (int j = TsMax.length - 1; j > idx; j--) {\n"
                        + "                            TsMax[j] = TsMax[j - 1];\n"
                        + "                            indexVet[j] = indexVet[j - 1];\n"
                        + "                        }\n"
                        + "                        TsMax[idx] = " + ts + ";\n"
                        + "                        indexVet[idx] = ssebi;\n"
                        + "                        break;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "                for (int idx = 0; idx < indexMinBaixo.length; idx++) {\n"
                        + "                    if (ssebi <= indexMinBaixo[idx]) {\n"
                        + "                        if (" + ts + " <= TsMinEsq[idx]) {\n"
                        + "                            for (int j = TsMinEsq.length - 1; j > idx; j--) {\n"
                        + "                                TsMinEsq[j] = TsMinEsq[j - 1];\n"
                        + "                                indexMinBaixo[j] = indexMinBaixo[j - 1];\n"
                        + "                            }\n"
                        + "                            TsMinEsq[idx] = " + ts + ";\n"
                        + "                            indexMinBaixo[idx] = ssebi;\n"
                        + "                            break;\n"
                        + "                        }\n"
                        + "                    }\n"
                        + "                }\n\n");
            }
        }

//        source.append("        System.out.println(\"banda3:\"+banda3);\n");
//        source.append("        System.out.println(\"banda4:\"+banda4);\n");
//        source.append("        System.out.println(\"bandaRefletida3:\"+bandaRefletida3);\n");
//        source.append("        System.out.println(\"bandaRefletida4:\"+bandaRefletida4);\n");
//        source.append("        System.out.println(\"SAVI:\"+SAVI);\n");
//        source.append("        System.out.println(\"NDVI:\"+NDVI);\n");
//        source.append("        System.out.println(\"Ts:\"+Ts);\n");
//        source.append("        System.out.println(\"IAF:\"+IAF);\n");
//        source.append("        System.out.println(\"albedo:\"+albedo);\n");
//        source.append("        System.out.println(\"emissivity:\"+emissivity);\n");
//        source.append("        System.out.println(\"Rn:\"+Rn);\n");
//        source.append("        System.out.println(\"emissividadeNB:\"+emissividadeNB);\n");
//        source.append("        System.out.println(\"mSavi:\"+index);\n");
//        source.append("        System.out.println(\"G0:\"+Ts);\n");
//        source.append("        System.out.println(\"LWd:\"+LWd+\"\\n\");\n");
        if (numbers.size() > 0) {
            source.append("            }\n");
        }

        source.append("        }\n");

        if (index != null) {
            if (indexEnum.equals(IndexEnum.SEBTA)) {
//                source.append("System.out.println(\"SEBTA JAVA\");");
//                source.append("System.out.println(RnHot);");
//                source.append("System.out.println(GHot);");
//                source.append("System.out.println(Uref);");
//                source.append("System.out.println(SAVI_hot);");
//                source.append("System.out.println(tMax);");
//                source.append("System.out.println(tMin);");
//                source.append("        float[] coef = new float[2];\n"
//                        + "        GenericSEB.calculaAB(coef, RnHot, GHot, Uref, SAVI_hot, tMax, tMin);\n");
//            source.append("        System.out.println(\"pixel1:\"+pixel1[10057582]);\n");
//            source.append("        System.out.println(\"pixel2:\"+pixel2[10057582]);\n");
//            source.append("        System.out.println(\"pixel3:\"+pixel3[10057582]);\n");
//            source.append("        System.out.println(\"pixel4:\"+pixel4[10057582]);\n");
//            source.append("        System.out.println(\"pixel5:\"+pixel5[10057582]);\n");
//            source.append("        System.out.println(\"pixel6:\"+pixel6[10057582]);\n");
//            source.append("        System.out.println(\"pixel7:\"+pixel7[10057582]);\n\n");
//            
//            source.append("        System.out.println(\"pixel1:\"+pixel1[45724667]);\n");
//            source.append("        System.out.println(\"pixel2:\"+pixel2[45724667]);\n");
//            source.append("        System.out.println(\"pixel3:\"+pixel3[45724667]);\n");
//            source.append("        System.out.println(\"pixel4:\"+pixel4[45724667]);\n");
//            source.append("        System.out.println(\"pixel5:\"+pixel5[45724667]);\n");
//            source.append("        System.out.println(\"pixel6:\"+pixel6[45724667]);\n");
//            source.append("        System.out.println(\"pixel7:\"+pixel7[45724667]);\n");
//            if (index != null) {
//                source.append("        System.out.println(\"cosZ:\"+cosZ);\n");
//                source.append("        System.out.println(\"dr:\"+dr);\n");
//                source.append("        System.out.println(\"RnHot:\"+RnHot);\n");
//                source.append("        System.out.println(\"GHot:\"+GHot);\n");
//                source.append("        System.out.println(\"SAVI_hot:\"+SAVI_hot);\n");
//                source.append("        System.out.println(\"A:\"+coef[0]);\n");
//                source.append("        System.out.println(\"B:\"+coef[1]);\n");
//                source.append("        System.out.println(\"index:\"+index);\n");
//                source.append("        System.out.println(\"indexMax:\"+indexMax);\n");
//                source.append("        System.out.println(\"indexMin:\"+indexMin);\n");
//                source.append("        System.out.println(\"tMax:\"+tMax);\n");
//                source.append("        System.out.println(\"tMin:\"+tMin);\n");
//            }
//                source.append("        ret.put(\"RnHot\",RnHot);\n\n");
//                source.append("        ret.put(\"GHot\",GHot);\n\n");
//                source.append("        ret.put(\"SAVI_hot\",SAVI_hot);\n\n");
//                source.append("        ret.put(\"tMax\",tMax);\n\n");
//                source.append("        ret.put(\"tMin\",tMin);\n\n");
                source.append("        ret.put(\"RnHot\",new float[]{RnHot});\n\n");
                source.append("        ret.put(\"GHot\",new float[]{GHot});\n\n");
                source.append("        ret.put(\"SAVI_hot\",new float[]{SAVI_hot});\n\n");
                source.append("        ret.put(\"indexMax\",new float[]{tMax});\n\n");
                source.append("        ret.put(\"indexMin\",new float[]{tMin});\n\n");
//            source.append("        System.exit(1);\n");
//                source.append("        ret.put(\"coef\",coef);\n\n");
            } else if (indexEnum.equals(IndexEnum.SEBAL)) {
//                source.append("        float[] coef = new float[2];\n"
//                        + "        GenericSEB.calculaAB(coef, RnHot, GHot, Uref, SAVI_hot, indexMax, indexMin);\n");
//                source.append("        ret.put(\"coef\",coef);\n\n");
                source.append("        ret.put(\"RnHot\",new float[]{RnHot});\n\n");
                source.append("        ret.put(\"GHot\",new float[]{GHot});\n\n");
                source.append("        ret.put(\"SAVI_hot\",new float[]{SAVI_hot});\n\n");
                source.append("        ret.put(\"indexMax\",new float[]{indexMax});\n\n");
                source.append("        ret.put(\"indexMin\",new float[]{indexMin});\n\n");
            } else if (indexEnum.equals(IndexEnum.SSEB)) {
                source.append(
                        //                        "            System.out.println(java.util.Arrays.toString(indexMax));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMin));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMax));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMin));\n"
                        //                        + "            System.exit(1);\n"
                        "        float TC = 0.0f;\n"
                        + "        float TH = 0.0f;\n"
                        + "        for (int i = 0; i < indexMax.length; i++) {\n"
                        + "            TC += TsMin[i];\n"
                        + "            TH += TsMax[i];\n"
                        + "        }\n"
                        + "        TC = TC / indexMax.length;\n"
                        + "        TH = TH / indexMax.length;\n"
                        //                        + "        System.out.println(\"SSEB JAVA\");\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(indexMax));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(TsMin));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(indexMin));\n"
                        //                        + "        System.out.println(java.util.Arrays.toString(TsMax));\n"
                        //                        + "        System.out.println(TC);\n"
                        //                        + "        System.out.println(TH);\n"
                        + "        ret.put(\"TC\", new float[]{TC});\n"
                        + "        ret.put(\"TH\", new float[]{TH});\n\n");
            } else if (indexEnum.equals(IndexEnum.SSEBI)) {
                source.append("        float mediaSupEsqTs = 0.0f;\n"
                        + "        float mediaSupDirTs = 0.0f;\n"
                        + "        float mediaInfEsqTs = 0.0f;\n"
                        + "        float mediaInfDirTs = 0.0f;\n"
                        + "\n"
                        + "        float mediaSupEsqIndex = 0.0f;\n"
                        + "        float mediaSupDirIndex = 0.0f;\n"
                        + "        float mediaInfEsqIndex = 0.0f;\n"
                        + "        float mediaInfDirIndex = 0.0f;\n"
                        + "\n"
                        + "        float[] indexMaxCima = new float[10];\n"
                        + "        float[] indexMaxBaixo = new float[10];\n"
                        + "        float[] indexMinCima = new float[10];\n"
                        //                        + "	   float[] indexMinBaixo = new float[10];\n"
                        + "\n"
                        + "        float[] TsMaxDir = new float[10];\n"
                        + "        float[] TsMaxEsq = new float[10];\n"
                        + "        float[] TsMinDir = new float[10];\n"
                        //                        + "	   float[] TsMinEsq = new float[10];\n"
                        + "\n"
                        + "        for (int i = 0; i < 10; i++) {\n"
                        + "            indexMinCima[i] = Float.MAX_VALUE;\n"
                        + "            TsMinDir[i] = Float.MAX_VALUE;\n"
                        + "        }\n"
                        + "\n"
                        + "        for (int i = 0; i < indexMax.length; i++) {\n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (TsVet[i] >= TsMaxDir[idx]) {\n"
                        + "                    for (int j = TsMaxDir.length - 1; j > idx; j--) {\n"
                        + "                        TsMaxDir[j] = TsMaxDir[j - 1];\n"
                        + "                        indexMaxCima[j] = indexMaxCima[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMaxDir[idx] = TsVet[i];\n"
                        + "                    indexMaxCima[idx] = indexMax[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "            \n"
                        + "            for (int idx = 0; idx < TsMinEsq.length; idx++) {\n"
                        + "                if (TsVet[i] <= TsMinDir[idx]) {\n"
                        + "                    for (int j = TsMinDir.length - 1; j > idx; j--) {\n"
                        + "                        TsMinDir[j] = TsMinDir[j - 1];\n"
                        + "                        indexMaxBaixo[j] = indexMaxBaixo[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMinDir[idx] = TsVet[i];\n"
                        + "                    indexMaxBaixo[idx] = indexMax[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "            \n"
                        + "            for (int idx = 0; idx < TsMaxEsq.length; idx++) {\n"
                        + "                if (indexVet[i] <= indexMinCima[idx]) {\n"
                        + "                    for (int j = TsMaxEsq.length - 1; j > idx; j--) {\n"
                        + "                        TsMaxEsq[j] = TsMaxEsq[j - 1];\n"
                        + "                        indexMinCima[j] = indexMinCima[j - 1];\n"
                        + "                    }\n"
                        + "                    TsMaxEsq[idx] = TsMax[i];\n"
                        + "                    indexMinCima[idx] = indexVet[i];\n"
                        + "                    break;\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "\n"
                        + "        for (int i = 0; i < TsMinEsq.length; i++) {\n"
                        + "            mediaSupEsqTs += TsMaxEsq[i];\n"
                        + "            mediaSupDirTs += TsMaxDir[i];\n"
                        + "            mediaInfEsqTs += TsMinEsq[i];\n"
                        + "            mediaInfDirTs += TsMinDir[i];\n"
                        + "\n"
                        + "            mediaSupEsqIndex += indexMinCima[i];\n"
                        + "            mediaSupDirIndex += indexMaxCima[i];\n"
                        + "            mediaInfEsqIndex += indexMinBaixo[i];\n"
                        + "            mediaInfDirIndex += indexMaxBaixo[i];\n"
                        + "        }\n"
                        + "\n"
                        + "        mediaSupEsqTs /= TsMaxEsq.length;\n"
                        + "        mediaSupDirTs /= TsMaxDir.length;\n"
                        + "        mediaInfEsqTs /= TsMinEsq.length;\n"
                        + "        mediaInfDirTs /= TsMinDir.length;\n"
                        + "\n"
                        + "        mediaSupEsqIndex /= indexMinCima.length;\n"
                        + "        mediaSupDirIndex /= indexMaxCima.length;\n"
                        + "        mediaInfEsqIndex /= indexMinBaixo.length;\n"
                        + "        mediaInfDirIndex /= indexMaxBaixo.length;\n"
                        //                        + "System.out.println(\"SSEBI JAVA\");\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMaxBaixo));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMinBaixo));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMaxCima));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(indexMinCima));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMaxEsq));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMaxDir));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMinDir));\n"
                        //                        + "            System.out.println(java.util.Arrays.toString(TsMinEsq));\n"
                        //                        + "            System.out.println(\"mediaSupEsqTs:\"+mediaSupEsqTs);\n"
                        //                        + "            System.out.println(\"mediaSupDirTs:\"+mediaSupDirTs);\n"
                        //                        + "            System.out.println(\"mediaInfEsqTs:\"+mediaInfEsqTs);\n"
                        //                        + "            System.out.println(\"mediaInfDirTs:\"+mediaInfDirTs);\n"
                        //                        + "            System.out.println(\"mediaSupEsqIndex:\"+mediaSupEsqIndex);\n"
                        //                        + "            System.out.println(\"mediaSupDirIndex:\"+mediaSupDirIndex);\n"
                        //                        + "            System.out.println(\"mediaInfEsqIndex:\"+mediaInfEsqIndex);\n"
                        //                        + "            System.out.println(\"mediaInfDirIndex:\"+mediaInfDirIndex);\n"
                        + "\n"
                        + "        float aH = 0.0f;\n"
                        + "        float aLE = 0.0f;\n"
                        + "        float bH = 0.0f;\n"
                        + "        float bLE = 0.0f;\n"
                        + "\n"
                        + "        bH = (mediaSupDirTs - mediaSupEsqTs) / (mediaSupDirIndex - mediaSupEsqIndex);\n"
                        + "        aH = (mediaSupEsqTs * mediaSupDirIndex - mediaSupDirTs * mediaSupEsqIndex) / (mediaSupDirIndex - mediaSupEsqIndex);\n"
                        + "\n"
                        + "        bLE = (mediaInfDirTs - mediaInfEsqTs) / (mediaInfDirIndex - mediaInfEsqIndex);\n"
                        + "        aLE = (mediaInfEsqTs * mediaInfDirIndex - mediaInfDirTs * mediaInfEsqIndex) / (mediaInfDirIndex - mediaInfEsqIndex);\n\n"
                        //                        + "System.out.println(\"ah:\"+aH);\n"
                        //                        + "        System.out.println(\"bH:\"+bH);\n"
                        //                        + "        System.out.println(\"aLE:\"+aLE);\n"
                        //                        + "        System.out.println(\"bLE:\"+bLE);\n"
                        + "        ret.put(\"aH\", new float[]{aH});\n"
                        + "        ret.put(\"bH\", new float[]{bH});\n"
                        + "        ret.put(\"aLE\", new float[]{aLE});\n"
                        + "        ret.put(\"bLE\", new float[]{bLE});\n\n");
            }
        }

        source.append("        return ret;\n");
        source.append("    }\n");
        source.append("}\n");
//        System.out.println(source.toString());
//        System.exit(1);
        return source.toString();
    }

    public static void main(String[] args) {
        // TODO code application logic here

        List<VariableValue> parameters = new ArrayList<VariableValue>();
        Map<String, Float> constants = new HashMap<String, Float>();
        GenericSEB g;

//        parameters.add(new VariableValue("hora", new float[]{0.0f, 30.0f, 100.0f, 130.0f}));
//        parameters.add(new VariableValue("dj", new float[]{293.0f, 293.0f, 293.0f, 293.0f}));
//        parameters.add(new VariableValue("temp", new float[]{276.58f, 270.25f, 268.09f, 266.08f}));
//        parameters.add(new VariableValue("ed", new float[]{0.0f, -20.455844f, -21.494523f, -21.202263f}));
//        parameters.add(new VariableValue("rn", new float[]{3704.7237151078f, 3570.1247122853f, 3525.1109277698f, 3483.7200791505f}));
//
//        constants.put("albedo", 0.4f);
//        constants.put("razaoInsolacao", 0.05f);
//        constants.put("latitude", -0.05266f);
//        constants.put("a2", 0.5f);
//        constants.put("a3", 0.1f);
//        constants.put("b2", 0.05f);
//        constants.put("b3", 0.8f);
//        constants.put("stefan", 5.6697E-8f);
//        constants.put("pascal", 133.3224f);
//
//        String form = "O_dj2=dj\n"
//                + "//TESTANDO COMENTARIO\n"
//                + "O_hora2=hora //TESTANDO COMENTARIO\n"
//                + "O_nh=floor(hora/100) //TESTANDO COMENTARIO\n"
//                + "O_nh_(mod(hora,100) == 30)=nh+0.5\n"
//                + "O_constanteSolar = 1369.0*(1+cos((dj+84.0))/360.0)\n"
//                + "O_f = 2*pi*dj/365.2425\n"
//                + "O_h = ((15.0*(nh-12.0))/180.0)*pi\n"
//                + "O_declinacaoSolar=(pi/180.0)*(0.3964 + 3.631*sin(f)-22.97*cos(f) + 0.03838*sin(2*f)-0.3885*cos(2*f)+ 0.07659*sin(3*f)-0.1587*cos(3*f)- 0.01021*cos(4*f))   \n"
//                + "O_cosZ = sin(latitude)*sin(declinacaoSolar)+cos(latitude)*cos(declinacaoSolar)*cos(h)\n"
//                + "O_rn2 = rn\n"
//                + "O_rn2_(cosZ < 0) = 0\n"
//                + "O_rg=(rn2+stefan*(pow((temp+273.15),4))*(a2+b2*sqrt(ed/pascal))*(a3+b3*razaoInsolacao))/((1.0-albedo))\n"
//                + "O_transmitancia=pow((rg/(constanteSolar*cosZ)),(cosZ))";
//        try {
//            Map<String, float[]> ret = g.execute("", form, parameters, constants);
//            float[] resp = ret.get("declinacaoSolar");
//            for (int i = 0; i < resp.length; i++) {
//                float f = resp[i];
//                System.out.println(f);
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(GenericSEB.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        float v = (float) Math.floor(30.0);
//        if (Maths.mod(v, 100.0f) == 30.0f) {
//            v = (float) (v + 0.5);
//        }
//        System.out.println(v);
//        System.exit(1);
//[64.0, 41.0, 62.0, 34.0, 8.0, 160.0, 3.0]
//[59.0, 28.0, 20.0, 105.0, 70.0, 0.0, 20.0]
        parameters.add(new VariableValue("pixel1", new float[]{64.0f, 59.0f}));
        parameters.add(new VariableValue("pixel2", new float[]{41.0f, 28.0f}));
        parameters.add(new VariableValue("pixel3", new float[]{62.0f, 20.0f}));
        parameters.add(new VariableValue("pixel4", new float[]{34.0f, 105.0f}));
        parameters.add(new VariableValue("pixel5", new float[]{8.0f, 70.0f}));
        parameters.add(new VariableValue("pixel6", new float[]{160.0f, 0.0f}));
        parameters.add(new VariableValue("pixel7", new float[]{3.0f, 20.0f}));

        constants.put("width", 1000f);
        constants.put("height", 1000f);
        constants.put("reflectanciaAtmosfera", 0.03f);
        constants.put("Kt", 1.0f);
        constants.put("L", 0.1f);
        constants.put("K1", 607.76f);
        constants.put("K2", 1260.56f);
        constants.put("S", 1367.0f);
        constants.put("StefanBoltzman", (float) (5.67 * Math.pow(10, -8)));
        constants.put("julianDay", 85.0f);
        constants.put("Z", 53.178f);
        constants.put("P", 99.9f);
        constants.put("UR", 74.01f);
        constants.put("Ta", 31.03f);
        constants.put("latitude", -16.56f);
        constants.put("Rg_24h", 243.7708132f);
        constants.put("Uref", 1.63f);
        constants.put("Tao_24h", 0.592380438f);

        Map<String, float[][]> constMatrix = new HashMap<String, float[][]>();

        constMatrix.put("calibration", new float[][]{
            {-1.52f, 193.0f, 1957.0f},
            {-2.84f, 365.0f, 1826.0f},
            {-1.17f, 264.0f, 1554.0f},
            {-1.51f, 221.0f, 1036.0f},
            {-0.37f, 30.2f, 215.0f},
            {1.2378f, 15.303f, 1.0f},
            {-0.15f, 16.5f, 80.67f}});

        Map<String, float[]> constVetor = new HashMap<String, float[]>();
        constVetor.put("parameterAlbedo", new float[]{0.293f, 0.274f, 0.233f, 0.157f, 0.033f, 0.0f, 0.011f});

        String forVariables = "dr = 1.0 + 0.033 * cos(julianDay * 2 * pi / 365)\n"
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

        String forEachValue = "rad_espectral = coef_calib_a + ((coef_calib_b - coef_calib_a) / 255.0) * pixel\n"
                + "reflectancia = (pi * rad_espectral) / (irrad_espectral * cosZ * dr)\n"
                + "albedo = (sumBandas - reflectanciaAtmosfera) / (transmissividade * transmissividade)\n"
                + "NDVI = (bandaRefletida4 - bandaRefletida3) / (bandaRefletida4 + bandaRefletida3)\n"
                + "SAVI = ((1.0 + L) * (bandaRefletida4 - bandaRefletida3)) / (L + bandaRefletida4 + bandaRefletida3)\n"
                + "IAF = (-ln((0.69 - SAVI) / 0.59) / 0.91)\n"
                + "IAF_(SAVI <= 0.1) = 0\n"
                + "IAF_(SAVI >= 0.687) = 6\n"
                + "emissividadeNB = 0.97 + 0.0033 * IAF\n"
                + "emissividadeNB_(IAF >= 3) = 0.98\n"
                + "emissividadeNB_(NDVI <= 0) = 0.99\n"
                + "emissivity = 0.95 + 0.01 * IAF\n"
                + "emissivity_(IAF >= 3) = 0.98\n"
                + "emissivity_(NDVI <= 0) = 0.985\n"
                + "Ts = K2/ln(((emissividadeNB * K1) / banda6) + 1.0)\n"
                + "LWd = emissivity * StefanBoltzman * (pow(Ts, 4))\n"
                + "Rn = ((1.0 - albedo) * SWd) + (emissivity * (LWdAtm) - LWd)\n"
                + "G0 = Rn * (((Ts - T0) / albedo) * (0.0038 * albedo + 0.0074 * albedo * albedo) * (1.0 - 0.98 * pow(NDVI, 4)))\n"
                + "sebta = (0.5) * ((2.0 * bandaRefletida4 + 1) - sqrt((pow((2 * bandaRefletida4 + 1), 2) - 8 * (bandaRefletida4 - bandaRefletida3))))";

        g = new GenericSEB(LanguageType.CUDA);
        try {
            g.execute(forVariables, forEachValue, parameters, constants, constVetor, constMatrix);
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

    public static float X(float Zref_m, float L) {
        return (float) (Math.sqrt(Math.sqrt((1.0f - 16.0f * Zref_m / L))));
    }

    public static float Psim(float L) {
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

    public static float Psih(float Zref_h, float L) {
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
