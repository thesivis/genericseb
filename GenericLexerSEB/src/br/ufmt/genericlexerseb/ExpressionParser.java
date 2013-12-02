/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericlexerseb;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Bartosz Borucki (babor@icm.edu.pl), University of Warsaw, ICM based
 * on:
 * http://www.technical-recipes.com/2011/a-mathematical-expression-parser-in-java-and-cpp
 *
 * @author Raphael de Souza Rosa Gomes (raphael@ic.ufmt.br) University Federal
 * of Mato Grosso, Brazil
 */
public class ExpressionParser {
    // Associativity constants for operators  

    private boolean debug = false;
    private static final int LEFT_ASSOC = 0;
    private static final int RIGHT_ASSOC = 1;
    // Operators      
    private static final Map<String, int[]> OPERATORS = new HashMap<>();
    private List<String> variables = new ArrayList<>();

    static {
        // Map<"token", []{precendence, associativity, number of arguments}>          
        OPERATORS.put("+", new int[]{1, LEFT_ASSOC, 2});
        OPERATORS.put("-", new int[]{1, LEFT_ASSOC, 2});
        OPERATORS.put("*", new int[]{5, LEFT_ASSOC, 2});
        OPERATORS.put("/", new int[]{5, LEFT_ASSOC, 2});
        OPERATORS.put("^", new int[]{10, RIGHT_ASSOC, 2});
        OPERATORS.put("~", new int[]{12, LEFT_ASSOC, 1});
        OPERATORS.put("sqrt", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("ln", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("log", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("logb", new int[]{15, LEFT_ASSOC, 2});
        OPERATORS.put("exp", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("abs", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("sin", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("cos", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("tan", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("asin", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("acos", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("atan", new int[]{15, LEFT_ASSOC, 1});
        OPERATORS.put("signum", new int[]{15, LEFT_ASSOC, 1});
    }

    public ExpressionParser() {
    }

    public ExpressionParser(boolean debug) {
        this(null, debug);
    }

    public ExpressionParser(List<String> variables) {
        this(variables, false);
    }

    public ExpressionParser(List<String> variables, boolean debug) {
        this.debug = debug;
        this.variables.addAll(variables);
    }

    private boolean isOperator(String token) {
        return OPERATORS.containsKey(token);
    }
    
    private boolean isNumeric(String token){
        try {
            double d = Double.parseDouble(token);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isVariable(String variable) {
        if (variables.isEmpty()) {
            return true;
        }
        if(!variables.contains(variable)){
            throw new IllegalArgumentException("Variable '" + variable+"' doesn't exist!");
        }
        return true;
    }

    private boolean isAssociative(String token, int type) {
        if (!isOperator(token)) {
            throw new IllegalArgumentException("Invalid token: " + token);
        }

        if (OPERATORS.get(token)[1] == type) {
            return true;
        }
        return false;
    }

    private int cmpPrecedence(String token1, String token2) {
        if (!isOperator(token1) || !isOperator(token2)) {
            throw new IllegalArgumentException("Invalid tokens: " + token1
                    + " " + token2);
        }
        return OPERATORS.get(token1)[0] - OPERATORS.get(token2)[0];
    }

    private String[] infixToRPN(String[] inputTokens) {
        ArrayList<String> out = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        if (debug) {
            System.out.println("Input:" + Arrays.toString(inputTokens));
        }

        for (String token : inputTokens) {
            if (debug) {
                System.out.println("Token:" + token);
            }
            if (isOperator(token)) {
                while (!stack.empty() && isOperator(stack.peek())) {
                    if ((isAssociative(token, LEFT_ASSOC)
                            && cmpPrecedence(token, stack.peek()) <= 0)
                            || (isAssociative(token, RIGHT_ASSOC)
                            && cmpPrecedence(token, stack.peek()) < 0)) {
                        out.add(stack.pop());
                        continue;
                    }
                    break;
                }
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);  //   
            } else if (token.equals(")")) {
                while (!stack.empty() && !stack.peek().equals("(")) {
                    out.add(stack.pop());
                }
                stack.pop();
            } else {
                if(!isNumeric(token)){
                    isVariable(token);
                }
                out.add(token);
            }
        }
        while (!stack.empty()) {
            out.add(stack.pop());
        }
        String[] output = new String[out.size()];
        return out.toArray(output);
    }

    private boolean RPNtoDouble(String[] tokens) {
        try {
            Stack<String> stack = new Stack<String>();
            if (debug) {
                System.out.println(Arrays.toString(tokens));
            }
            for (String token : tokens) {
                if (!isOperator(token)) {
                    stack.push(token);
                } else {

                    int[] op = OPERATORS.get(token);
                    for (int i = 0; i < op[2]; i++) {
                        stack.pop();
                    }

                    stack.push("1.0");
                }
            }

            if (debug) {
                System.out.println("Stack Size:" + stack.size());
            }

            return (stack.size() == 1);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean evaluateExpr(String expr) {
        String str = preprocessExpr(expr);
        if (debug) {
            System.out.println("After:" + str);
        }
        String[] input = tokenize(str);

        String[] output = infixToRPN(input);
        if (debug) {
            for (String token : output) {
                System.out.print(token + " ");
            }
            System.out.println("");
        }
        boolean result = RPNtoDouble(output);
        return result;
    }

    private String fixUnaryMinus(String input, String pattern) {
        String str = new String(input);
        String tmp2;
        Matcher m = Pattern.compile(pattern).matcher(str);
        while (m.find()) {
            tmp2 = m.group();
            tmp2 = tmp2.replaceFirst(pattern, pattern.substring(0, pattern.length() - 1) + "~");
            str = str.substring(0, m.start()) + tmp2 + str.substring(m.end(), str.length());
        }
        return str;
    }

    private String preprocessExpr(String input) {
        String str = new String(input);
        str = str.replaceAll("\\s", "");
        str = str.replaceAll(",", " ");
        if (debug) {
            System.out.println(str);
        }

        //case #1 - minus at the beginning
        if (str.startsWith("-")) {
            str = str.replaceFirst("-", "~");
        }

        //case #2 - minus after (
        str = str.replaceAll("\\(-", "\\(~");

        //case #3 - minus after operators
        str = fixUnaryMinus(str, "\\+-");
        str = fixUnaryMinus(str, "--");
        str = fixUnaryMinus(str, "\\*-");
        str = fixUnaryMinus(str, "/-");
        str = fixUnaryMinus(str, "\\^-");
        if (debug) {
            System.out.println("Before:" + str);
        }

        str = str.trim();
        return str;
    }

    public String[] tokenize(String s) {
        try {
            String equation = s;
            if (s.contains("=")) {
                String[] vet = s.split("=");
                isVariable(vet[0]);
                equation = vet[1];
            }
            StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(equation));
            tokenizer.ordinaryChar('/');  // Don't parse div as part of numbers.
            tokenizer.ordinaryChar('-');// Don't parse minus as part of numbers.
            List<String> tokBuf = new ArrayList<String>();
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tokenizer.ttype) {
                    case StreamTokenizer.TT_NUMBER:
                        tokBuf.add(String.valueOf(tokenizer.nval));
                        break;
                    case StreamTokenizer.TT_WORD:
                        tokBuf.add(tokenizer.sval);
                        break;
                    default:  // operator
                        tokBuf.add(String.valueOf((char) tokenizer.ttype));
                }
            }
            String[] ret = new String[tokBuf.size()];
            ret = tokBuf.toArray(ret);
            return ret;
        } catch (IOException ex) {
            Logger.getLogger(ExpressionParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) {

//        String str = "-2*ln(2)-(a-(b^-2))";
        String str = "( 1.0 + 2.123)*(2*a/b)-sqrt(5.0+6)";
        //String str = "a^-2";  
        ExpressionParser parser = new ExpressionParser(true);
        boolean result = parser.evaluateExpr(str);
        System.out.println("result = " + result);

    }
}
