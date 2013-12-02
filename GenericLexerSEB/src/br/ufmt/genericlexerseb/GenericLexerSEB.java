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
import java.util.List;
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

    public static HashMap<LanguageType, HashMap<String, String>> functions = new HashMap<>();

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

                funcs = functions.get(LanguageType.PYTHON);
                if (funcs == null) {
                    funcs = new HashMap<>();
                    functions.put(LanguageType.PYTHON, funcs);
                }
                funcs.put(vet[0], vet[4]);


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
        
        if (language.equals(LanguageType.PYTHON)) {
            //REPLACE FUNCTIONS
            HashMap<String, String> funcs = functions.get(LanguageType.PYTHON);

            equation = dependent.getToken() + (dependent.isVector() ? "[" + dependent.getIndex() + "]" : "") + "=";
            for (int i = 0; i < terms.length; i++) {
                function = funcs.get(terms[i]);
                if (function != null) {
                    terms[i] = function;
                } else if (terms[i].equals("~")) {
                    terms[i] = "-";
                }
                equation += terms[i];
            }
            System.out.println(equation);
        }

        return equation;
    }

    public double getResult(String equation, List<Variable> variables) {
        PythonInterpreter in = new PythonInterpreter();
        String[] vet = equation.split("=");

        for (Variable variable : variables) {
            in.set(variable.getName(), variable.getValue());
        }

        in.exec("import math");
        in.exec(equation);
        PyObject o = in.get(vet[0]);
        return o.asDouble();
    }
}
