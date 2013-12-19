/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericlexerseb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author raphael
 */
public class GenericLexerSEB {

    public final static HashMap<LanguageType, HashMap<String, String>> functions = new HashMap<>();

    static {

        BufferedReader bur = null;
        try {
            String path = GenericLexerSEB.class.getResource("/functions/functions.csv").getPath();
            bur = new BufferedReader(new FileReader(path));
            String line = bur.readLine();

            String vet[];
            HashMap<String, String> funcs;
            while (line != null) {
                vet = line.split(";");

                funcs = functions.get(LanguageType.OPENCL);
                if (funcs == null) {
                    funcs = new HashMap<>();
                    functions.put(LanguageType.OPENCL, funcs);
                }
                funcs.put(vet[0], vet[1]);

                funcs = functions.get(LanguageType.CUDA_FLOAT);
                if (funcs == null) {
                    funcs = new HashMap<>();
                    functions.put(LanguageType.CUDA_FLOAT, funcs);
                }
                funcs.put(vet[0], vet[2]);

                funcs = functions.get(LanguageType.CUDA_DOUBLE);
                if (funcs == null) {
                    funcs = new HashMap<>();
                    functions.put(LanguageType.CUDA_DOUBLE, funcs);
                }
                funcs.put(vet[0], vet[3]);

                funcs = functions.get(LanguageType.JAVA);
                if (funcs == null) {
                    funcs = new HashMap<>();
                    functions.put(LanguageType.JAVA, funcs);
                }
                funcs.put(vet[0], vet[4]);
                
                funcs = functions.get(LanguageType.PYTHON);
                if (funcs == null) {
                    funcs = new HashMap<>();
                    functions.put(LanguageType.PYTHON, funcs);
                }
                funcs.put(vet[0], vet[5]);


                line = bur.readLine();
            }
        } catch (Exception ex) {
            Logger.getLogger(GenericLexerSEB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bur.close();
            } catch (IOException ex) {
                Logger.getLogger(GenericLexerSEB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String analyse(String equation, Structure dependent, Map<String, Structure> variables, LanguageType language) {
        ExpressionParser expressionParser = new ExpressionParser();
        expressionParser.evaluateExpr(equation);
        String[] terms = expressionParser.getOutput();
        String function;
        HashMap<String, String> funcs = functions.get(language);
        Structure independent;

        if (dependent.isVector()) {
            if (dependent.getIndex().equalsIgnoreCase("*")) {
                equation = "*" + dependent.getToken() + "=";
            } else {
                equation = dependent.getToken() + "[" + dependent.getIndex() + "]" + "=";
            }
        } else {
            equation = dependent.getToken() + "=";
        }

        for (int i = 0; i < terms.length; i++) {
            function = funcs.get(terms[i]);
            if (function != null) {
                terms[i] = function;
            } else if (terms[i].equals("~")) {
                terms[i] = "-";
            } else if (terms[i].matches("(-?)[0-9]+[\\.][0-9]+") && !language.equals(LanguageType.PYTHON)) {
                terms[i] = terms[i] + "f";
            } else if (terms[i].equals("pi") && language.equals(LanguageType.JAVA)) {
                terms[i] = "Math.PI";
            } else if (terms[i].equals("pi") && language.equals(LanguageType.PYTHON)) {
                terms[i] = "math.pi";
            } else if (variables != null) {
                independent = variables.get(terms[i]);
                if (independent != null) {
                    if (independent.isVector()) {
                        if (independent.getIndex().equalsIgnoreCase("*")) {
                            terms[i] = " *" + independent.getToken();
                        } else {
                            terms[i] = independent.getToken() + "[" + independent.getIndex() + "]";
                        }
                    }
                }
            }
            equation += terms[i];
        }


//        System.out.println(equation);
        return equation;
    }

    public double getResult(String equation, Map<String, Variable> variables) {
        PythonInterpreter in = new PythonInterpreter();
        String[] vet = equation.split("=");


        for (Variable variable : variables.values()) {
            in.set(variable.getName(), variable.getValue());
        }

        in.exec("import math");
        in.exec(equation);
        PyObject o = in.get(vet[0]);
        return o.asDouble();
    }

    public double getResults(String equation, Map<String, Double> variables) {
        PythonInterpreter in = new PythonInterpreter();
        String[] vet = equation.split("=");
        vet[0] = vet[0].replace(" ", "");

        StringBuilder t = new StringBuilder("import math\n");
        for (String variable : variables.keySet()) {
            t.append(variable +"="+variables.get(variable)+"\n");
        }

        t.append(equation);
        t.append("\nres = str("+vet[0]+")\n");
        PyObject o = in.eval(in.compile(t.toString()));
        o = in.get(vet[0]);
        return o.asDouble();
    }
}
